package com.wandoujia.image.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.wandoujia.base.utils.SystemUtil;
import com.wandoujia.image.ImageManager;
import com.wandoujia.image.LoadImageCallback;

/**
 * Image view to perform asynchronous image loading.
 * 
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 * @author xubin@wandoujia.com
 */
public class AsyncImageView extends ImageView {
  private static final long SHOW_ANIMATION_THRESHOLD = 100L;
  private static final int SHOW_ANIMATION_INTERVAL = 200;
  private static ImageManagerHolder imageManagerHolder;
  private final AlphaAnimation animation;
  {
    animation = new AlphaAnimation(0.4f, 1.0f);
    animation.setDuration(SHOW_ANIMATION_INTERVAL);
  }
  private ImageManager.ImageContainer imageContainer;
  private int defaultRes;
  private String requestKey;
  private ImageRequestType requestType;
  private int rotate;
  private Status status;
  private OnStatusChangedListener onStatusChangedListener;
  private OnImageLoadedListener onImageLoadedListener;
  private boolean defaultImmediately = true;
  private int requiredWidth = 0;
  private int requiredHeight = 0;

  public AsyncImageView(Context context) {
    super(context);
  }

  public AsyncImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  /**
   * Sets {@link ImageManagerHolder} before any other method is called.
   * 
   * @param imageManagerHolder holder of image manager
   */
  public static void setImageManagerHolder(ImageManagerHolder imageManagerHolder) {
    AsyncImageView.imageManagerHolder = imageManagerHolder;
  }

  public interface ImageManagerHolder {
    ImageManager getImageManager();
  }

  @Override
  protected void onDetachedFromWindow() {
    if (need2PauseLoadingOnDetach()) {
      pauseLoading();
    }
    super.onDetachedFromWindow();
  }

  @Override
  protected void onAttachedToWindow() {
    // TODO @xubin onLayout is not called when re-attach ofter detach, check this logic
    resumeLoading();
    super.onAttachedToWindow();
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();
    invalidate();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    resumeLoading(true);
  }

  /**
   * You *** MUST *** handle the loading life circle on your own and call {@link #pauseLoading()}
   * manually if you return false.
   * 
   * @return
   */
  protected boolean need2PauseLoadingOnDetach() {
    return true;
  }

  /**
   * Stops loading, and reset its status.
   */
  public void reset() {
    requestKey = null;
    requestType = null;
    rotate = 0;
    if (imageContainer != null) {
      imageContainer.cancelRequest();
      imageContainer = null;
    }
    setStatus(null);
  }

  /**
   * Pauses loading.
   */
  public void pauseLoading() {
    if (status == null || status == Status.FINISHED) {
      return;
    }
    if (imageContainer != null) {
      imageContainer.cancelRequest();
      imageContainer = null;
    }
    setStatus(Status.PENDING);
  }

  public void resumeLoading() {
    resumeLoading(false);
  }

  /**
   * start loading.
   */
  protected void resumeLoading(boolean isInLayout) {
    if (status != Status.PENDING) {
      return;
    }
    int width = getWidth();
    int height = getHeight();

    // if the view's bounds aren't known yet, hold off on loading the image.
    if (width == 0 && height == 0) {
      return;
    }

    // if the requestKey to be loaded in this view is empty, stop loading
    if (TextUtils.isEmpty(requestKey) || requestType == null) {
      setImageResource(defaultRes);
      return;
    }
    // if there was an old request in this view, check if it needs to be canceled.
    if (imageContainer != null && imageContainer.getRequestKey() != null) {
      if (imageContainer.getRequestKey().equals(requestKey)) {
        // if the request is from the same requestKey, return.
        return;
      } else {
        // if there is a pre-existing request, cancel it if it's fetching a different URL.
        imageContainer.cancelRequest();
        if (defaultImmediately) {
          setImageResource(defaultRes);
        }
      }
    }
    setStatus(Status.RUNNING);
    switch (requestType) {
      case NETWORK:
        loadNetImageAsync(isInLayout);
        break;
      case APP_ICON:
        loadAppIconAsync(isInLayout);
        break;
      case VIDEO_THUMBNAIL:
        loadVideoThumbnailAsync(isInLayout);
        break;
      case APK_ICON:
        loadLocalApkIconAsync(isInLayout);
        break;
      case LOCAL_IMAGE:
        loadLocalImageAsync(isInLayout);
        break;
      default:
        break;
    }
  }

  // edit start by chengye at 2014.6.5
  /**
   * whether show default image while loading. set false if you want default image only show when
   * loading failed. using when continuous image replacement happens and don`t show default image
   * each time
   * 
   * @param defaultImmediately false if show default only when loading failed
   */
  public void setDefaultImmediately(boolean defaultImmediately) {
    this.defaultImmediately = defaultImmediately;
  }

  protected void setRequireSize(int requiredWidth, int requiredHeight) {
    this.requiredWidth = requiredWidth;
    this.requiredHeight = requiredHeight;
  }

  // edit end

  /**
   * Returns status of this view. Null for not started.
   * 
   * @return {@link android.os.AsyncTask.Status}
   */
  public Status getStatus() {
    return status;
  }

  private void setStatus(Status status) {
    if (this.status == status) {
      return;
    }
    Status originStatus = this.status;
    this.status = status;
    if (onStatusChangedListener != null) {
      onStatusChangedListener.onStatusChanged(originStatus, status);
    }
  }

  public void setOnStatusChangedListener(OnStatusChangedListener onStatusChangedListener) {
    this.onStatusChangedListener = onStatusChangedListener;
  }

  public void setImageLoadedListener(OnImageLoadedListener onImageLoadedListener1) {
    this.onImageLoadedListener = onImageLoadedListener1;
  }

  @Override
  public void setImageResource(int resId) {
    Bitmap bitmap = imageManagerHolder.getImageManager()
        .getResourceImageBitmap(getContext(), resId);
    if (bitmap != null) {
      setImageBitmap(bitmap);
    } else {
      super.setImageResource(resId);
    }
  }

  /**
   * Sets static image by resource id. The difference between this and
   * {@link #setImageResource(int)} is that this clears previous state before setting image.
   * 
   * @param resId image resource id
   */
  public void setStaticImageResource(int resId) {
    reset();
    setImageResource(resId);
  }

  /**
   * Sets static image by bitmap. The difference between this and
   * {@link #setImageBitmap(android.graphics.Bitmap)} is that this clears previous state before
   * setting image.
   * 
   * @param bitmap bitmap
   */
  public void setStaticImageBitmap(Bitmap bitmap) {
    reset();
    setImageBitmap(bitmap);
  }

  /**
   * Loads image from network using a http url in asynchronous mode.
   * 
   * @param url image url
   * @param defaultImageResId default image resource id, which is shown before loading
   */
  public void loadNetworkImage(String url, int defaultImageResId) {
    defaultRes = defaultImageResId;
    requestKey = url;
    requestType = ImageRequestType.NETWORK;
    setStatus(Status.PENDING);
    resumeLoading(false);
  }

  /**
   * Loads application icon from local using its package name in asynchronous mode.
   * 
   * @param packageName the app must be installed already, otherwise, it fails to load
   * @param defaultImageResId default image resource id, which is shown before loading
   */
  public void loadLocalAppIcon(String packageName, int defaultImageResId) {
    defaultRes = defaultImageResId;
    requestKey = packageName;
    requestType = ImageRequestType.APP_ICON;
    setStatus(Status.PENDING);
    resumeLoading(false);
  }

  /**
   * Loads apk icon from the apk filepath given in asynchronous mode.
   * 
   * @param filePath path of apk file.
   * @param defaultImageResId default image resource id, which is shown before loading.
   */
  public void loadLocalApkIcon(String filePath, int defaultImageResId) {
    defaultRes = defaultImageResId;
    requestKey = filePath;
    requestType = ImageRequestType.APK_ICON;
    setStatus(Status.PENDING);
    resumeLoading(false);
  }

  /**
   * Loads video thumbnail from local in asynchronous mode.
   * 
   * @param videoFilePath file path of video
   * @param defaultImageResId default image resource id, which is shown before loading
   */
  public void loadVideoThumbnail(String videoFilePath, int defaultImageResId) {
    defaultRes = defaultImageResId;
    requestKey = videoFilePath;
    requestType = ImageRequestType.VIDEO_THUMBNAIL;
    setStatus(Status.PENDING);
    resumeLoading(false);
  }

  /**
   * Loads local image in asynchronous mode.
   * 
   * @param filePath file path of image
   * @param defaultImageResId default image resource id, which is shown before loading
   */
  public void loadLocalImage(String filePath, int defaultImageResId) {
    loadLocalImage(filePath, 0, defaultImageResId);
  }

  /**
   * Loads local image in asynchronous mode.
   * 
   * @param filePath file path of image
   * @param rotate rotate degree of image
   * @param defaultImageResId default image resource id, which is shown before loading
   */
  public void loadLocalImage(String filePath, int rotate, int defaultImageResId) {
    this.defaultRes = defaultImageResId;
    this.rotate = rotate;
    requestKey = filePath;
    requestType = ImageRequestType.LOCAL_IMAGE;
    setStatus(Status.PENDING);
    resumeLoading(false);
  }

  protected void loadNetImageAsync(final boolean isInLayout) {
    final long startLoadingTimeMs = System.currentTimeMillis();
    LoadImageCallback callback = new LoadImageCallback() {
      @Override
      public void onResponse(final Bitmap bitmap, boolean isImmediate) {
        handleResponse(bitmap, isImmediate, isInLayout, startLoadingTimeMs);
      }
    };

    if (requiredHeight != 0 && requiredWidth != 0) {
      imageContainer = imageManagerHolder.getImageManager()
          .getNetworkImage(requestKey, callback, requiredWidth, requiredHeight);
    } else if (SystemUtil.aboveApiLevel(Build.VERSION_CODES.HONEYCOMB)) {
      // save memory in api 2.3, by yangkai@wandoujia.com
      imageContainer = imageManagerHolder.getImageManager().getNetworkImage(requestKey, callback);
    } else {
      imageContainer = imageManagerHolder.getImageManager()
          .getNetworkImage(requestKey, callback, getWidth(), getHeight());
    }
  }

  private void loadAppIconAsync(final boolean isInLayout) {
    final long startLoadingTimeMs = System.currentTimeMillis();
    imageContainer = imageManagerHolder.getImageManager().getAppIcon(
        requestKey, new LoadImageCallback() {

          @Override
          public void onResponse(final Bitmap bitmap, boolean isImmediate) {
            setStatus(Status.FINISHED);
            handleResponse(bitmap, isImmediate, isInLayout, startLoadingTimeMs);
          }

        });
  }

  private void loadVideoThumbnailAsync(final boolean isInLayout) {
    final long startLoadingTimeMs = System.currentTimeMillis();
    imageContainer = imageManagerHolder.getImageManager().getVideoThumbnail(requestKey,
        new LoadImageCallback() {

          @Override
          public void onResponse(final Bitmap bitmap, boolean isImmediate) {
            setStatus(Status.FINISHED);
            handleResponse(bitmap, isImmediate, isInLayout, startLoadingTimeMs);
          }

        });
  }

  private void loadLocalImageAsync(final boolean isInLayout) {
    final long startLoadingTimeMs = System.currentTimeMillis();
    imageContainer = imageManagerHolder.getImageManager().getLocalImage(requestKey,
        new LoadImageCallback() {

          @Override
          public void onResponse(final Bitmap bitmap, boolean isImmediate) {
            setStatus(Status.FINISHED);
            handleResponse(bitmap, isImmediate, isInLayout, startLoadingTimeMs);
          }

        }, getWidth(), getHeight());
  }

  private void loadLocalApkIconAsync(final boolean isInLayout) {
    final long startLoadingTimeMs = System.currentTimeMillis();
    imageContainer = imageManagerHolder.getImageManager().getApkIcon(
        requestKey, new LoadImageCallback() {

          @Override
          public void onResponse(final Bitmap bitmap, boolean isImmediate) {
            setStatus(Status.FINISHED);
            handleResponse(bitmap, isImmediate, isInLayout, startLoadingTimeMs);
          }

        });
  }

  protected void transitionToBitmap(Bitmap bitmap) {
    startAnimation(animation);
    setImageBitmap(bitmap);
  }

  protected void onImageLoaded(Bitmap bitmap, final long startLoadingTimeMs) {
    if (System.currentTimeMillis() - startLoadingTimeMs > SHOW_ANIMATION_THRESHOLD) {
      transitionToBitmap(bitmap);
    } else {
      setImageBitmap(bitmap);
    }

    if (onImageLoadedListener != null) {
      onImageLoadedListener.onLoaded(this, bitmap);
    }
  }

  private void handleResponse(final Bitmap bitmap, boolean isImmediate, final boolean isInLayout,
      final long startLoadingTimeMs) {
    // If this was an immediate response that was delivered inside of a layout
    // pass do not set the image immediately as it will trigger a requestLayout
    // inside of a layout. Instead, defer setting the image by posting back to
    // the main thread.
    if (isImmediate && isInLayout) {
      post(new Runnable() {
        @Override
        public void run() {
          handleResponse(bitmap, true, false, startLoadingTimeMs);
        }
      });
      return;
    }
    /**
     * {@link com.android.volley.toolbox.ImageLoader#get (String, com.android.volley.toolbox.ImageLoader.ImageListener, int, int)}
     * at line 193
     */
    if (!isImmediate || bitmap != null) {
      setStatus(Status.FINISHED);
    }
    if (bitmap != null) {
      if (requestType == ImageRequestType.LOCAL_IMAGE && rotate != 0) {
        AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

          @Override
          protected Bitmap doInBackground(Void... params) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
          }

          @Override
          protected void onPostExecute(Bitmap image) {
            onImageLoaded(image, startLoadingTimeMs);
          }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
          task.execute();
        }
      } else {
        onImageLoaded(bitmap, startLoadingTimeMs);
      }
    } else if (defaultRes != 0) {
      if (!isImmediate || defaultImmediately) {
        // set image resource directly, no need to find in cache
        super.setImageResource(defaultRes);
      }
    }
  }

  private enum ImageRequestType {
    NETWORK, APP_ICON, LOCAL_IMAGE, VIDEO_THUMBNAIL, APK_ICON
  }

  public interface OnStatusChangedListener {

    /**
     * Get called when the loading status changed.
     * 
     * @param originStatus origin status before the change.
     * @param targetStatus target status after the change.
     */
    void onStatusChanged(Status originStatus, Status targetStatus);
  }

  public interface OnImageLoadedListener {
    void onLoaded(AsyncImageView imageView, Bitmap bitmap);
  }
}
