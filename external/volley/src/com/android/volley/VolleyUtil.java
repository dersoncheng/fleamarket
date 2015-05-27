package com.android.volley;

import java.io.IOException;
import java.io.InputStream;

import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.PoolingByteArrayOutputStream;

/**
 * util methods of volley
 * 
 * @author yangkai@wandoujia.com
 * 
 */
public class VolleyUtil {

  private static final int DEFAULT_BUFFER_SIZE = 1024;
  private static final int MAX_PROGRESS = 100;

  /**
   * get byte array from a input stream
   * 
   * @param pool
   * @param in
   * @param length
   * @param callback if length <= 0, callback will be ignored because we can't calculate progress
   * @param careThreadInterrupted check if current thread is interrupted while reading
   * @return
   * @throws IOException
   * @throws ServerError
   * @throws InterruptedException
   */
  public static byte[] getByteArrayFromInputStream(ByteArrayPool pool, InputStream in, int length,
      ReadByteArrayCallback callback, boolean careThreadInterrupted) throws IOException,
      ServerError, InterruptedException {
    if (length >= 0) {
      return getByteArrayByNewBytes(pool, in, length, callback, careThreadInterrupted);
    } else {
      return getByteArrayByBytePool(pool, in, length, callback, careThreadInterrupted);
    }
  }

  private static byte[] getByteArrayByNewBytes(ByteArrayPool pool, InputStream is, int length,
      ReadByteArrayCallback callback, boolean careThreadInterrupted) throws IOException,
      ServerError, InterruptedException {
    byte[] byteArr = new byte[length];
    byte[] buffer = null;
    try {
      buffer = pool.getBuf(DEFAULT_BUFFER_SIZE);
      int len = 0;
      int count;
      boolean interrupted = false;

      while ((!careThreadInterrupted || !(interrupted = Thread.interrupted()))
          && (count = is.read(buffer)) != -1) {
        // make sure byteArr is large enough
        if (count + len > byteArr.length) {
          // length is incorrect, set minimum buffer size avoid too many allocate
          byte[] newbuf = new byte[Math.max((count + len) * 2, DEFAULT_BUFFER_SIZE)];
          System.arraycopy(byteArr, 0, newbuf, 0, len);
          byteArr = newbuf;
        }
        System.arraycopy(buffer, 0, byteArr, len, count);
        len += count;

        if (callback != null && length > 0) {
          int progress = Math.min(MAX_PROGRESS, len * MAX_PROGRESS / length);
          callback.notifyProgressChanged(progress);
        }
      }
      if (interrupted) {
        throw new InterruptedException();
      }

      // if length is incorrect. new a correct byte array
      if (byteArr.length > len) {
        byte[] newbuf = new byte[len];
        System.arraycopy(byteArr, 0, newbuf, 0, len);
        byteArr = newbuf;
      }
      return byteArr;
    } finally {
      pool.returnBuf(buffer);
    }
  }

  private static byte[] getByteArrayByBytePool(ByteArrayPool pool, InputStream is, int length,
      ReadByteArrayCallback callback, boolean careThreadInterrupted) throws IOException,
      ServerError, InterruptedException {
    PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(pool, length);
    byte[] buffer = null;
    try {
      buffer = pool.getBuf(DEFAULT_BUFFER_SIZE);
      boolean interrupted = false;
      int len = 0;
      int count;

      while ((!careThreadInterrupted || !(interrupted = Thread.interrupted()))
          && (count = is.read(buffer)) != -1) {
        bytes.write(buffer, 0, count);
        len += count;

        if (length > 0 && callback != null) {
          int progress = Math.min(MAX_PROGRESS, len * MAX_PROGRESS / length);
          callback.notifyProgressChanged(progress);
        }
      }
      if (interrupted) {
        throw new InterruptedException();
      }

      return bytes.toByteArray();
    } finally {
      pool.returnBuf(buffer);
      bytes.close();
    }
  }

  /**
   * call back of read bytes
   * @author yangkai@wandoujia.com
   *
   */
  public static interface ReadByteArrayCallback {
    public void notifyProgressChanged(int progress);
  }
}
