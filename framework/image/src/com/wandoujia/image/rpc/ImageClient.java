package com.wandoujia.image.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;

import com.android.volley.ServerError;
import com.android.volley.VolleyUtil;
import com.android.volley.VolleyUtil.ReadByteArrayCallback;
import com.android.volley.toolbox.ByteArrayPool;
import com.wandoujia.base.http.HttpClientWrapper.ErrorCodeHandler;
import com.wandoujia.base.utils.FreeHttpUtils;
import com.wandoujia.base.utils.ImageUtil;
import com.wandoujia.image.rpc.RequestLatchController.RequestLatch;

/**
 * Image client to get images from network.
 */
public class ImageClient {

  public static final int LOAD_PROGRESS_INTERVAL = 10;
  private static final String CACHE_CONTROL = "Cache-control";
  private static final long MAX_AGE = 30 * 24 * 3600L;
  private static final int TIME_OUT = 30 * 1000;
  private static final String CACHE_CONTROL_MAX_AGE = "max-age";
  private static final String COOKIE = "Cookie";
  private final ImageResource emptyImageResource = new ImageResource(null, false);
  private final RequestLatchController requestLatchController = new RequestLatchController();
  private final Set<Thread> runningThreads = new HashSet<Thread>();
  private final Map<Object, HttpURLConnection> workingConnections =
      new HashMap<Object, HttpURLConnection>();
  private final Set<String> cacheableWhitelist = new HashSet<String>();
  private final ByteArrayPool byteArrayPool;
  public static ErrorCodeHandler errorCodeHandler;

  private boolean enable = true;

  public ImageClient(ByteArrayPool byteArrayPool) {
    this.byteArrayPool = byteArrayPool;
  }

  public static void setErrorCodeHandler(ErrorCodeHandler handler) {
    errorCodeHandler = handler;
  }

  /**
   * Gets image from network, and updates progress and do post processing if callback
   * provided.
   * 
   * @param urlString url
   * @param maxWidth the max-width of the loaded bitmap, 0 means no limit, should not be negative
   * @param maxHeight The max-height of the loaded bitmap, 0 means no limit, should not be negative
   * @param cookie cookie of request, can be null
   * @param progressCallback progress callback to update progress. Can be null
   * @param handler handler to notify progress changed. If null, call back on UI thread
   * @param token a token by which caller can cancel this request by {@link #cancel(Object)}.
   *          Can be null
   * @return {@BitmapResource}, not null
   */
  public ImageResource getImage(String urlString, final int maxWidth, final int maxHeight,
      String cookie, ProgressCallback progressCallback, Handler handler, Object token) {
    if (!enable) {
      return emptyImageResource;
    }
    synchronized (runningThreads) {
      runningThreads.add(Thread.currentThread());
    }
    HttpURLConnection connection = null;
    Bitmap bitmap = null;
    RequestLatch requestLatch = null;
    try {
      // If attach cookie, we do not merge the request, to avoid potential error
      if (cookie == null) {
        requestLatch = requestLatchController.latch(urlString, progressCallback, handler);
        if (!requestLatch.isLatched()) {
          bitmap = requestLatch.getBitmap();
          if (bitmap != null) {
            return new ImageResource(bitmap, false);
          }
        }
      }
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      URL url = new URL(urlString);
      connection = openConnection(url);
      if (cookie != null) {
        connection.addRequestProperty(COOKIE, cookie);
      }
      connection.connect();
      synchronized (workingConnections) {
        workingConnections.put(token, connection);
      }
      boolean isCacheable = isCacheable(connection);
      int numBytes = connection.getContentLength();
      InputStream is = connection.getInputStream();
      byte[] buffer = readInputStream(is, numBytes, requestLatch, progressCallback != null);
      bitmap = ImageUtil.decodeBitmap(buffer, maxWidth, maxHeight);
      if (bitmap == null) {
        return emptyImageResource;
      }
      return new ImageResource(bitmap, isCacheable);
    } catch (InterruptedException e) {
      return emptyImageResource;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      if (requestLatch != null && requestLatch.isLatched()) {
        requestLatch.unlatch(bitmap);
      }
      synchronized (runningThreads) {
        runningThreads.remove(Thread.currentThread());
      }
      synchronized (workingConnections) {
        workingConnections.remove(token);
      }
    }
    return emptyImageResource;
  }

  public void cancel(Object token) {
    final HttpURLConnection conn;
    synchronized (workingConnections) {
      conn = workingConnections.get(token);
    }
    if (conn != null) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          conn.disconnect();
        }
      });
    }
  }

  /**
   * Adds an url to the cacheable whitelist, so the image will always cacheable.
   * 
   * @param url image url which is cacheable
   */
  public void addCacheableUrl(String url) {
    cacheableWhitelist.add(url);
  }

  /**
   * Shuts down all on-going requests, and disable future requests until invoking {@link #open()}.
   */
  public void shutdown() {
    enable = false;
    synchronized (runningThreads) {
      for (Thread thread : runningThreads) {
        thread.interrupt();
      }
    }
    synchronized (workingConnections) {
      for (Map.Entry<Object, HttpURLConnection> entry : workingConnections.entrySet()) {
        HttpURLConnection conn = entry.getValue();
        if (conn != null) {
          conn.disconnect();
        }
      }
      workingConnections.clear();
    }
  }

  /**
   * Opens client to enable network image request.
   */
  public void open() {
    enable = true;
  }

  /**
   * Reads input stream to buffer and updates progress.
   */
  private byte[] readInputStream(InputStream is, int totalLength, final RequestLatch requestLatch,
      boolean notifyProgressChanged) throws IOException, InterruptedException {
    try {
      byte[] byteArr =
          VolleyUtil.getByteArrayFromInputStream(byteArrayPool, is, totalLength,
              notifyProgressChanged ? new ReadByteArrayCallback() {

                int previewProgress = 0;

                @Override
                public void notifyProgressChanged(int progress) {
                  if (progress / LOAD_PROGRESS_INTERVAL > previewProgress) {
                    if (requestLatch != null) {
                      requestLatch.notifyProgressChanged(progress);
                    }
                    previewProgress = progress / LOAD_PROGRESS_INTERVAL;
                  }
                }
              } : null, true);
      return byteArr;
    } catch (ServerError e) {
      // ignore
      e.printStackTrace();
      return null;
    }
  }

  private boolean isCacheable(HttpURLConnection connection) {
    if (cacheableWhitelist.contains(connection.getURL().toString())) {
      return true;
    }

    String value = connection.getHeaderField(CACHE_CONTROL);
    if (TextUtils.isEmpty(value)) {
      return true;
    }
    int index = value.indexOf(CACHE_CONTROL_MAX_AGE);
    if (index >= 0) {
      try {
        long maxAge = Long.parseLong(value.substring(index + CACHE_CONTROL_MAX_AGE.length() + 1));
        if (maxAge > MAX_AGE) {
          return true;
        }
      } catch (NumberFormatException e) {
        return false;
      }
    }
    return false;
  }

  private static HttpURLConnection openConnection(URL url) throws IOException {
    return openConnection(url, null);
  }

  private static HttpURLConnection openConnection(URL url, String cookies) throws IOException {
    url = FreeHttpUtils.buildFreeURLIfNeed(url);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setConnectTimeout(TIME_OUT);
    if (!TextUtils.isEmpty(cookies)) {
      conn.setRequestProperty("Cookie", cookies);
    }

    // normally, 3xx is redirect
    int status = conn.getResponseCode();
    if (FreeHttpUtils.isInFreeMode() && errorCodeHandler != null) {
      errorCodeHandler.onResponse(status);
    }
    if (status == HttpURLConnection.HTTP_MOVED_TEMP
        || status == HttpURLConnection.HTTP_MOVED_PERM
        || status == HttpURLConnection.HTTP_SEE_OTHER) {
      // do redirect
      String newUrl = conn.getHeaderField("Location");
      String newCookies = conn.getHeaderField("Set-Cookie");
      return openConnection(new URL(newUrl), newCookies);
    }
    return conn;
  }

  /**
   * Image resource.
   */
  public static class ImageResource {

    private final Bitmap bitmap;
    private final boolean isCacheable;

    ImageResource(Bitmap bitmap, boolean isCacheable) {
      this.bitmap = bitmap;
      this.isCacheable = bitmap != null && isCacheable;
    }

    public Bitmap getBitmap() {
      return bitmap;
    }

    public boolean isCacheable() {
      return isCacheable;
    }
  }

}
