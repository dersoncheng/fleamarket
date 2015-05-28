package com.wandoujia.image.rpc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

/**
 * The latch to merge all requests of the same url into one.
 *
 * liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class RequestLatchController {
  private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
  private final Map<String, RequestLatch> requestLatchs = new HashMap<String, RequestLatch>();

  /**
   * Adds latch to a request.
   *
   * <p>If there is request on-going, this call will block until that request
   * returns; otherwise, the latch will return immediately. One can differentiate block/unblock by
   * {@link RequestLatch#isLatched()}.</p>
   *
   * @param url request url
   * @param progressCallback progress callback, can be null
   * @param handler in which thread the progress callback is invoked, null for UI thread
   * @return {@link RequestLatch}
   * @throws InterruptedException when thread is interrupted
   */
  public RequestLatch latch(String url, ProgressCallback progressCallback, Handler handler)
      throws InterruptedException {
    RequestLatch latch;
    synchronized (requestLatchs) {
      latch = requestLatchs.get(url);
      if (latch == null) {
        latch = new RequestLatch(url);
        latch.addProgressCallback(progressCallback, handler);
        requestLatchs.put(url, latch);
        return latch;
      }
    }
    latch.addProgressCallback(progressCallback, handler);
    latch.waitForUnlatch();
    return latch;
  }

  public class RequestLatch {
    private final String url;
    private final CountDownLatch latch = new CountDownLatch(1);
    private Bitmap bitmap;
    private byte[] callbackLock = new byte[0];
    private List<ProgressWrapper> progressCallbacks;

    private RequestLatch(String url) {
      this.url = url;
    }

    private void addProgressCallback(
        ProgressCallback progressCallback, Handler handler) {
      if (progressCallback != null) {
        synchronized (callbackLock) {
          if (progressCallbacks == null) {
            progressCallbacks = new LinkedList<ProgressWrapper>();
          }
          progressCallbacks.add(new ProgressWrapper(progressCallback, handler));
        }
      }
    }

    public void notifyProgressChanged(final int progress) {
      if (latch.getCount() == 0) {
        throw new IllegalStateException("Already unlatched, no progress needed.");
      }
      synchronized (callbackLock) {
        if (progressCallbacks != null) {
          for (final ProgressWrapper progressWrapper : progressCallbacks) {
            Handler handler = progressWrapper.handler == null
                ? UI_HANDLER : progressWrapper.handler;
            handler.post(new Runnable() {

              @Override
              public void run() {
                progressWrapper.progressCallback.onProgressChanged(progress);
              }

            });
          }
        }
      }
    }

    /**
     * Returns whether it is latched. True for latched already, and further request will block;
     * false for released by previous same request.
     *
     * @return true for latched
     */
    public boolean isLatched() {
      return latch.getCount() == 1L;
    }

    public void unlatch(Bitmap bitmap) {
      if (latch.getCount() == 0L) {
        throw new IllegalStateException("Already unlatched.");
      }
      synchronized (requestLatchs) {
        requestLatchs.remove(url);
      }
      this.bitmap = bitmap;
      latch.countDown();
    }

    public Bitmap getBitmap() {
      return bitmap;
    }

    private void waitForUnlatch() throws InterruptedException {
      latch.await();
    }

    private class ProgressWrapper {
      ProgressWrapper(ProgressCallback progressCallback, Handler handler) {
        this.progressCallback = progressCallback;
        this.handler = handler;
      }

      private final ProgressCallback progressCallback;
      private final Handler handler;
    }
  }
}
