package com.wandoujia.image;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.wandoujia.base.http.HttpClientWrapper;
import com.wandoujia.base.utils.FreeHttpUtils;
import com.wandoujia.base.utils.ImageUtil;
import com.wandoujia.image.cache.ConcurrentDiskBasedCache;
import com.wandoujia.image.cache.ImageCacheUtils;
import com.wandoujia.image.cache.ImageMemoryCache;
import com.wandoujia.image.local.LocalImageClient;

/**
 * Image client to get images from local or network.
 * 
 * <p>
 * Usage 1:
 * 
 * <pre>
 *     LoadImageCallback loadImageCallback = new loadImageCallback() {
 * 
 *       @Override
 *       public void onResponse(Bitmap bitmap, boolean isImmediate) {
 *         // if the bitmap is null, set default image
 *         // isImmediate means hit the cache
 *       }
 *     }
 *     ImageContainer requestContainer = imageManager.getNetworkImage(
 *         url, loadImageCallback);
 *     ....
 *     // If you want to cancel loading
 *     requestContainer.cancelRequest();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * Usage 2:
 * 
 * <pre>
 *     ImageContainer requestContainer = imageManager.getNetworkImage(url);
 *     ....
 *     // Wait for finishing loading
 *     Bitmap bitmap = requestContainer.getBitmap();
 * </pre>
 * 
 * </p>
 * 
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 * @author xubin@wandoujia.com (Bin Xu)
 */
public class ImageManager {

  private static final int DEFAULT_BYTE_ARRAY_SIZE = 128 * 1024;
  private static final String VOLLEY_CACHE_DIR = "volley";
  /** Amount of time to wait after first response arrives before delivering all responses. */
  private static final int BATCH_RESPONSE_DELAY_MS = 100;
  private static final long IMAGE_RENDER_DELAY_WHILE_SCROLLING = 200L;
  private final ImageMemoryCache memoryCache;
  private final ImageThreadPool localImageThreadPool;
  private final ImageLoader networkImageLoader;
  private final LocalImageClient localImageClient;
  private final Handler uiHandler = new Handler(Looper.getMainLooper());
  private final HashMap<String, BatchedImageRequest> mWaitingRequests = new
      LinkedHashMap<String, BatchedImageRequest>();
  /**
   * HashMap of Cache keys -> BatchedImageRequest used to track in-flight requests so
   * that we can coalesce multiple requests to the same URL into a single network request.
   */
  private final HashMap<String, BatchedImageRequest> inFlightRequests =
      new HashMap<String, BatchedImageRequest>();
  /** HashMap of the currently pending responses (waiting to be delivered). */
  private final HashMap<String, BatchedImageRequest> batchedResponses =
      new HashMap<String, BatchedImageRequest>();
  private boolean enableNetwork = true;
  private boolean isWaiting = false;
  /** Runnable for in-flight response delivery. */
  private Runnable responseRunnable;
  /** Resources for plugin use */
  private Resources resources;

  public ImageManager(Context context, Config config) {
    this(context, config, null);
  }

  public ImageManager(Context context, Config config, ByteArrayPool byteArrayPool) {
    if (byteArrayPool == null) {
      byteArrayPool = new ByteArrayPool(DEFAULT_BYTE_ARRAY_SIZE);
    }
    localImageThreadPool = new ImageThreadPool(config.getLocalThreadPoolSize());
    memoryCache = new ImageMemoryCache(config.getMemoryCacheSize());
    networkImageLoader = newImageLoader(context, config, byteArrayPool, new NoCache());
    localImageClient = new LocalImageClient(config.getContext(), byteArrayPool);
    resources = config.getResources();
  }

  private static RequestQueue newVolleyRequestQueue(Context context, Config config,
      ByteArrayPool byteArrayPool) {
    // init file cache
    File cacheDir = new File(config.getFileCacheDir(), VOLLEY_CACHE_DIR);
    Cache diskCache = new ConcurrentDiskBasedCache(cacheDir, config.getFileCacheSize());
    // init network
    String userAgent = context.getPackageName();
    HttpStack stack;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      stack = new HurlStack(new HurlStack.UrlRewriter() {

        @Override
        public String rewriteUrl(String originalUrl) {
          return FreeHttpUtils.buildFreeURLIfNeed(originalUrl);
        }
      });
    } else {
      // Prior to Gingerbread, HttpUrlConnection was unreliable.
      // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
      stack =
          new HttpClientStack(HttpClientWrapper.newInstance(AndroidHttpClient
              .newInstance(userAgent)));
    }
    Network network = new BasicNetwork(stack, byteArrayPool);
    // build RequestQueue and start it
    RequestQueue queue = new RequestQueue(diskCache, network, config.getNetworkThreadPoolSize());
    queue.start();
    return queue;
  }

  private static ImageLoader newImageLoader(Context context, Config config,
      ByteArrayPool byteArrayPool, ImageLoader.ImageCache cache) {
    RequestQueue requestQueue = newVolleyRequestQueue(context, config, byteArrayPool);
    return new ImageLoader(requestQueue, cache);
  }

  /**
   * Creates a cache key for use with the L1 cache.
   * 
   * @param uri The URI of the request, can be URL of a network request,
   *          or path of a local request.
   * @param maxWidth The max-width of the output.
   * @param maxHeight The max-height of the output.
   */
  private static String getCacheKey(String uri, int maxWidth, int maxHeight) {
    return new StringBuilder().append("#W").append(maxWidth)
        .append("#H").append(maxHeight).append(uri).toString();
  }

  private static void throwIfNotOnMainThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
    }
  }

  /**
   * Gets network image.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param url url
   * @return requestContainer
   */
  public ImageContainer getNetworkImage(String url) {
    return getNetworkImage(url, null, 0, 0);
  }

  /**
   * Gets network image.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param url url
   * @param callback callback for loading events, can be null
   * @return requestContainer
   */
  public ImageContainer getNetworkImage(String url, LoadImageCallback callback) {
    return getNetworkImage(url, callback, 0, 0);
  }

  /**
   * Gets network image.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param url url
   * @param callback callback for loading events
   * @param maxWidth the max-width of the loaded bitmap, 0 means no limit, should not be negative
   * @param maxHeight The max-height of the loaded bitmap, 0 means no limit, should not be negative
   * @return ImageContainer
   */
  public ImageContainer getNetworkImage(final String url, LoadImageCallback callback,
      final int maxWidth, final int maxHeight) {
    ImageContainer container;
    if (!enableNetwork) {
      container = new ImageContainer(url, null, callback);
      container.notifyImageLoaded(null);
      return container;
    }
    ImageRequestBuilder builder = new ImageRequestBuilder() {
      @Override
      public ImageRequest buildImageRequest(ImageRequestListener listener) {
        return new NetworkImageRequest(url, maxWidth, maxHeight, listener);
      }

      @Override
      public String getCacheKey() {
        return ImageManager.getCacheKey(url, maxWidth, maxHeight);
      }
    };
    container = getImage(url, callback, builder);
    return container;
  }

  /**
   * Gets local image.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param filePath file path
   * @return requestContainer
   */
  public ImageContainer getLocalImage(final String filePath) {
    return getLocalImage(filePath, null, 0, 0);
  }

  /**
   * Gets local image.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param filePath file path
   * @param callback callback for loading events, can be null
   * @return requestContainer
   */
  public ImageContainer getLocalImage(String filePath, LoadImageCallback callback) {
    return getLocalImage(filePath, callback, 0, 0);
  }

  /**
   * Gets local image.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param filePath file path
   * @param callback callback for loading events
   * @param maxWidth the max-width of the loaded bitmap, 0 means no limit, should not be negative
   * @param maxHeight The max-height of the loaded bitmap, 0 means no limit, should not be negative
   * @return requestContainer
   */
  public ImageContainer getLocalImage(final String filePath, LoadImageCallback callback,
      final int maxWidth, final int maxHeight) {
    ImageRequestBuilder builder = new ImageRequestBuilder() {
      @Override
      public ImageRequest buildImageRequest(ImageRequestListener listener) {
        return new LocalImageRequest(filePath, maxWidth, maxHeight, listener);
      }

      @Override
      public String getCacheKey() {
        return ImageManager.getCacheKey(filePath, maxWidth, maxHeight);
      }
    };
    return getImage(filePath, callback, builder);
  }

  /**
   * Gets app icon.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param packageName app package name
   * @return requestContainer
   */
  public ImageContainer getAppIcon(String packageName) {
    return getAppIcon(packageName, null);
  }

  /**
   * Gets app icon.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param packageName app package name
   * @param callback callback for loading events
   * @return requestContainer
   */
  public ImageContainer getAppIcon(final String packageName, LoadImageCallback callback) {
    ImageRequestBuilder builder = new ImageRequestBuilder() {
      @Override
      public ImageRequest buildImageRequest(ImageRequestListener listener) {
        return new AppIconImageRequest(packageName, listener);
      }

      @Override
      public String getCacheKey() {
        return packageName;
      }
    };
    return getImage(packageName, callback, builder);
  }

  /**
   * Gets apk icon.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param filePath apk file path.
   * @return requestContainer
   */
  public ImageContainer getApkIcon(final String filePath) {
    return getApkIcon(filePath, null);
  }

  /**
   * Gets apk icon.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param filePath apk file path.
   * @param callback callback for loading events
   * @return requestContainer
   */
  public ImageContainer getApkIcon(final String filePath, LoadImageCallback callback) {
    ImageRequestBuilder builder = new ImageRequestBuilder() {
      @Override
      public ImageRequest buildImageRequest(ImageRequestListener listener) {
        return new ApkIconImageRequest(filePath, listener);
      }

      @Override
      public String getCacheKey() {
        return filePath;
      }
    };
    return getImage(filePath, callback, builder);
  }

  /**
   * Gets video thumbnail.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param filePath file path
   * @return requestContainer
   */
  public ImageContainer getVideoThumbnail(String filePath) {
    return getVideoThumbnail(filePath, null);
  }

  /**
   * Gets video thumbnail.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param filePath file path
   * @param callback callback for loading events
   * @return requestContainer
   */
  public ImageContainer getVideoThumbnail(final String filePath, LoadImageCallback callback) {
    ImageRequestBuilder builder = new ImageRequestBuilder() {
      @Override
      public ImageRequest buildImageRequest(ImageRequestListener listener) {
        return new VideoThumbnailRequest(filePath, listener);
      }

      @Override
      public String getCacheKey() {
        return filePath;
      }
    };
    return getImage(filePath, callback, builder);
  }

  /**
   * Gets bitmap from resource. Providing cache to reduce memory load.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   * 
   * @param context context
   * @param resId resource id
   * @return bitmap
   */
  public Bitmap getResourceImageBitmap(Context context, int resId) {
    final String key = String.valueOf(resId);
    if (memoryCache != null) {
      Bitmap bitmap = memoryCache.get(key);
      if (bitmap != null) {
        return bitmap;
      }
    }
    Drawable drawable;
    try {
      if (resources != null) {
        drawable = resources.getDrawable(resId);
      } else {
        drawable = context.getResources().getDrawable(resId);
      }
    } catch (Resources.NotFoundException e) {
      e.printStackTrace();
      return null;
    }
    final Bitmap bitmap = ImageUtil.drawableToBitmap(drawable);
    if (bitmap != null) {
      localImageThreadPool.execute(new Callable<Bitmap>() {
        @Override
        public Bitmap call() throws Exception {
          ImageCacheUtils.cacheImage(memoryCache, key, bitmap);
          return null;
        }
      });
    }
    return bitmap;
  }

  public void clearMemoryCache() {
    memoryCache.clear();
  }

  /**
   * Shuts down network stub to disable network image loading.
   */
  public void shutdownNetworkStub() {
    enableNetwork = false;
  }

  /**
   * Opens network stub to enable network image loading.
   */
  public void openNetworkStub() {
    enableNetwork = true;
  }

  /**
   * Resume image loading.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>
   */
  public void resumeImageLoading() {
    // only fulfill requests that were initiated from the main thread.
    throwIfNotOnMainThread();
    if (!isWaiting) {
      return;
    }
    isWaiting = false;
    // Check to see if a request is already in-flight.
    for (Map.Entry<String, BatchedImageRequest> entry : mWaitingRequests.entrySet()) {
      String cacheKey = entry.getKey();
      BatchedImageRequest waitingRequest = entry.getValue();
      waitingRequest.request.execute();
      inFlightRequests.put(cacheKey, waitingRequest);
    }
    mWaitingRequests.clear();
  }

  /**
   * Pause image loading, all the requests during paused will execute when {@code resumeImageLoading()) is called, unless it's already canceled.
   * <p>
   * <b>Should only call in UI thread</b>
   * </p>

   */
  public void pauseImageLoading() {
    // only fulfill requests that were initiated from the main thread.
    throwIfNotOnMainThread();
    isWaiting = true;
  }

  private ImageContainer getImage(String requestKey, LoadImageCallback callback,
      ImageRequestBuilder requestBuilder) {
    throwIfNotOnMainThread();

    final String cacheKey = requestBuilder.getCacheKey();
    ImageContainer container = new ImageContainer(requestKey, cacheKey, callback);
    Bitmap cachedBitmap = memoryCache.getBitmap(cacheKey);
    if (cachedBitmap != null) {
      container.notifyImageLoaded(cachedBitmap);
      return container;
    }

    container.notifyCacheMissed();

    BatchedImageRequest batchedRequest = inFlightRequests.get(cacheKey);
    if (batchedRequest != null) {
      batchedRequest.addContainer(container);
      return container;
    }

    ImageRequestListener listener = new ImageRequestListener() {
      @Override
      public void onImageLoaded(Bitmap bitmap) {
        ImageManager.this.onImageLoaded(cacheKey, bitmap);
      }
    };
    ImageRequest newRequest = requestBuilder.buildImageRequest(listener);
    if (isWaiting) {
      batchedRequest = mWaitingRequests.get(cacheKey);
      if (batchedRequest != null) {
        batchedRequest.addContainer(container);
        return container;
      } else {
        mWaitingRequests.put(cacheKey, new BatchedImageRequest(newRequest, container));
      }
    } else {
      newRequest.execute();
      inFlightRequests.put(cacheKey, new BatchedImageRequest(newRequest, container));
    }
    return container;
  }

  /**
   * Called when bitmap is loaded.
   * 
   * @param cacheKey the key for cache the bitmap
   * @param bitmap the loaded bitmap
   */
  private void onImageLoaded(final String cacheKey, final Bitmap bitmap) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      uiHandler.post(new Runnable() {
        @Override
        public void run() {
          onImageLoaded(cacheKey, bitmap);
        }
      });
      return;
    }

    // don't render bitmap while is waiting
    if (isWaiting) {
      uiHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          if (isWaiting) {
            uiHandler.postDelayed(this, IMAGE_RENDER_DELAY_WHILE_SCROLLING);
          } else {
            onImageLoaded(cacheKey, bitmap);
          }
        }
      }, IMAGE_RENDER_DELAY_WHILE_SCROLLING);
      return;
    }

    if (bitmap != null) {
      ImageCacheUtils.cacheImage(memoryCache, cacheKey, bitmap);
    }

    // remove the request from the list of in-flight requests.
    BatchedImageRequest batchedRequest = inFlightRequests.remove(cacheKey);

    if (batchedRequest != null) {
      // Send the batched response
      batchedRequest.responseBitmap = bitmap;
      batchResponse(cacheKey, batchedRequest);
    }
  }

  /**
   * Starts the runnable for batched delivery of responses if it is not already started.
   * 
   * @param cacheKey The cacheKey of the response being delivered.
   * @param request The BatchedImageRequest to be delivered.
   */
  private void batchResponse(String cacheKey, BatchedImageRequest request) {
    batchedResponses.put(cacheKey, request);
    // If we don't already have a batch delivery runnable in flight, make a new one.
    // Note that this will be used to deliver responses to all callers in batchedResponses.
    if (responseRunnable == null) {
      responseRunnable = new Runnable() {
        @Override
        public void run() {
          Set<BatchedImageRequest> requests =
              new HashSet<BatchedImageRequest>(batchedResponses.values());
          for (BatchedImageRequest batchedRequest : requests) {
            for (ImageContainer container : batchedRequest.imageContainers) {
              // If one of the callers in the batched request canceled the request
              // after the response was received but before it was delivered,
              // skip them.
              if (container.isCanceled()) {
                continue;
              }
              container.notifyImageLoaded(batchedRequest.responseBitmap);
            }
          }
          batchedResponses.clear();
          responseRunnable = null;
        }

      };
      // Post the runnable.
      uiHandler.postDelayed(responseRunnable, BATCH_RESPONSE_DELAY_MS);
    }
  }

  private interface ImageRequestBuilder {
    ImageRequest buildImageRequest(ImageRequestListener listener);

    String getCacheKey();
  }

  private interface ImageRequestListener {
    void onImageLoaded(Bitmap bitmap);
  }

  /**
   * Wrapper class used to map a Request to the set of active ImageContainer objects that are
   * interested in its results.
   */
  private static class BatchedImageRequest {
    /** The request being tracked */
    private final ImageRequest request;
    /** List of all of the active ImageContainers that are interested in the request */
    private final LinkedList<ImageContainer> imageContainers = new LinkedList<ImageContainer>();
    /** The result of the request being tracked by this item */
    private Bitmap responseBitmap;

    /**
     * Constructs a new BatchedImageRequest object
     * 
     * @param request The request being tracked
     * @param container The ImageContainer of the person who initiated the request.
     */
    public BatchedImageRequest(ImageRequest request, ImageContainer container) {
      this.request = request;
      imageContainers.add(container);
    }

    /**
     * Adds another ImageContainer to the list of those interested in the results of
     * the request.
     */
    public void addContainer(ImageContainer container) {
      imageContainers.add(container);
    }

    /**
     * Detaches the bitmap container from the request and cancels the request if no one is
     * left listening.
     * 
     * @param container The container to remove from the list
     * @return True if the request was canceled, false otherwise.
     */
    public boolean removeContainerAndCancelIfNecessary(ImageContainer container) {
      imageContainers.remove(container);
      if (imageContainers.isEmpty()) {
        request.cancel();
        return true;
      }
      return false;
    }
  }

  /**
   * Empty cache that will always miss.
   */
  private static class NoCache implements ImageLoader.ImageCache {

    @Override
    public Bitmap getBitmap(String url) {
      return null;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {}
  }

  private abstract class ImageRequest {
    protected final int maxWidth;
    protected final int maxHeight;
    protected final ImageRequestListener listener;

    ImageRequest(ImageRequestListener listener, int maxWidth, int maxHeight) {
      this.maxWidth = maxWidth;
      this.maxHeight = maxHeight;
      this.listener = listener;
    }

    abstract void execute();

    abstract void cancel();

  }

  private class NetworkImageRequest extends ImageRequest {
    private final String url;
    private ImageLoader.ImageContainer innerImageContainer;

    NetworkImageRequest(String url, int maxWidth, int maxHeight,
        ImageRequestListener listener) {
      super(listener, maxWidth, maxHeight);
      this.url = url;
    }

    @Override
    void execute() {
      ImageLoader.ImageListener imageListener = new ImageLoader.ImageListener() {
        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
          Bitmap bitmap = response == null ? null : response.getBitmap();
          if (bitmap == null && isImmediate) {
            return;
          }
          listener.onImageLoaded(bitmap);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
          // TODO should tell caller when loading error.
          listener.onImageLoaded(null);
        }
      };
      innerImageContainer = networkImageLoader.get(url, imageListener,
          maxWidth, maxHeight);
    }

    @Override
    void cancel() {
      if (innerImageContainer != null) {
        innerImageContainer.cancelRequest();
      }
    }
  }

  private class AppIconImageRequest extends ImageRequest {
    private final String packageName;
    private FutureTask future;

    AppIconImageRequest(String packageName,
        ImageRequestListener listener) {
      super(listener, 0, 0);
      this.packageName = packageName;
    }

    @Override
    void execute() {
      future = new FutureTask<Void>(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          Bitmap bitmap = localImageClient.getAppIcon(packageName);
          listener.onImageLoaded(bitmap);
          return null;
        }
      });
      localImageThreadPool.execute(future);
    }

    @Override
    void cancel() {
      if (future != null) {
        future.cancel(true);
        localImageThreadPool.cancel(future, true);
      }
    }
  }

  private class ApkIconImageRequest extends ImageRequest {
    private final String filePath;
    private FutureTask future;

    ApkIconImageRequest(String filePath,
        ImageRequestListener listener) {
      super(listener, 0, 0);
      this.filePath = filePath;
    }

    @Override
    void execute() {
      future = new FutureTask<Void>(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          Bitmap bitmap = localImageClient.getApkIcon(filePath);
          listener.onImageLoaded(bitmap);
          return null;
        }
      });
      localImageThreadPool.execute(future);
    }

    @Override
    void cancel() {
      if (future != null) {
        future.cancel(true);
        localImageThreadPool.cancel(future, true);
      }
    }
  }


  private class LocalImageRequest extends ImageRequest {
    private final String filePath;
    private FutureTask future;

    LocalImageRequest(String filePath, int maxWidth, int maxHeight,
        ImageRequestListener listener) {
      super(listener, maxWidth, maxHeight);
      this.filePath = filePath;
    }

    @Override
    void execute() {
      future = new FutureTask<Void>(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          Bitmap bitmap = localImageClient.getImage(filePath, maxWidth, maxHeight);
          listener.onImageLoaded(bitmap);
          return null;
        }
      });
      localImageThreadPool.execute(future);
    }

    @Override
    void cancel() {
      if (future != null) {
        future.cancel(true);
        localImageThreadPool.cancel(future, true);
      }
    }
  }

  private class VideoThumbnailRequest extends ImageRequest {
    private final String filePath;
    private FutureTask future;

    VideoThumbnailRequest(String filePath, ImageRequestListener listener) {
      super(listener, 0, 0);
      this.filePath = filePath;
    }

    @Override
    void execute() {
      future = new FutureTask<Void>(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          Bitmap bitmap = localImageClient.getVideoThumbnail(filePath);
          listener.onImageLoaded(bitmap);
          return null;
        }
      });
      localImageThreadPool.execute(future);
    }

    @Override
    void cancel() {
      if (future != null) {
        future.cancel(true);
        localImageThreadPool.cancel(future, true);
      }
    }
  }

  /**
   * Container object for all of the data surrounding an image request.
   */
  public class ImageContainer {
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private final String requestKey;
    private final String cacheKey;
    private final LoadImageCallback callback;
    private Bitmap bitmap;

    ImageContainer(String requestKey, String cacheKey, LoadImageCallback callback) {
      this.requestKey = requestKey;
      this.cacheKey = cacheKey;
      this.callback = callback;
    }

    public void notifyImageLoaded(Bitmap bitmap) {
      this.bitmap = bitmap;
      if (callback != null) {
        callback.onResponse(bitmap, false);
      }
      synchronized (loaded) {
        loaded.set(true);
        loaded.notifyAll();
      }
    }

    public void notifyCacheMissed() {
      if (callback != null) {
        callback.onResponse(null, true);
      }
    }

    public Bitmap getBitmap() {
      try {
        synchronized (loaded) {
          while (!loaded.get()) {
            loaded.wait();
          }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return bitmap;
    }

    public String getRequestKey() {
      return requestKey;
    }

    /**
     * @return true if the request is canceled, false otherwise
     */
    public boolean isCanceled() {
      return canceled.get();
    }

    /**
     * Cancel the image request.
     */
    public void cancelRequest() {
      if (canceled.get()) {
        return;
      }
      BatchedImageRequest waitingRequest = mWaitingRequests.get(cacheKey);
      if (waitingRequest != null) {
        boolean canceled = waitingRequest.removeContainerAndCancelIfNecessary(this);
        if (canceled) {
          mWaitingRequests.remove(cacheKey);
        }
        return;
      }
      BatchedImageRequest request = inFlightRequests.get(cacheKey);
      if (request != null) {
        boolean canceled = request.removeContainerAndCancelIfNecessary(this);
        if (canceled) {
          inFlightRequests.remove(cacheKey);
        }
      } else {
        // check to see if it is already batched for delivery.
        request = batchedResponses.get(cacheKey);
        if (request != null) {
          request.removeContainerAndCancelIfNecessary(this);
          if (request.imageContainers.size() == 0) {
            batchedResponses.remove(cacheKey);
          }
        }
      }
      canceled.set(true);
    }
  }
}
