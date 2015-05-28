package com.wandoujia.image.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.ServerError;
import com.android.volley.VolleyUtil;
import com.android.volley.toolbox.ByteArrayPool;
import com.jakewharton.disklrucache.DiskLruCache;
import com.wandoujia.base.utils.IOUtils;
import com.wandoujia.base.utils.ImageUtil;

/**
 * Image file cache.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class ImageFileCache implements ImageCache {

  private static final String TAG = ImageFileCache.class.getSimpleName();
  // Version number used in cache, not necessarily equals to the app version in manifest XML file.
  private static final int CACHE_APP_VERSION = 1;
  private static final int BITMAP_COMPRESS_HIGHEST_QUALITY = 100;

  private final long maxSize;
  private final String cacheDirPath;
  private DiskLruCache cache;
  private final byte[] cacheLock = new byte[0];
  private final ByteArrayPool byteArrayPool;


  public ImageFileCache(String cacheDirPath, long maxSizeInByte, ByteArrayPool byteArrayPool) {
    this.cacheDirPath = cacheDirPath;
    this.byteArrayPool = byteArrayPool;
    maxSize = maxSizeInByte;
  }

  private DiskLruCache getCache() {
    synchronized (cacheLock) {
      if (cache == null) {
        File cacheDir = new File(cacheDirPath);
        if (cacheDir != null) {
          try {
            cache = DiskLruCache.open(cacheDir, CACHE_APP_VERSION, 1, maxSize);
          } catch (IOException e) {
            Log.e(TAG, "Failed to open cache directory");
            e.printStackTrace();
          }
        }
      }
    }
    return cache;
  }

  @Override
  protected void finalize() throws Throwable {
    if (cache != null) {
      cache.close();
    }
    super.finalize();
  }

  @Override
  public Bitmap get(String key) {
    DiskLruCache cache = getCache();
    if (cache == null) {
      return null;
    }
    DiskLruCache.Snapshot snapshot = null;
    byte[] buffer = null;

    try {
      snapshot = cache.get(encodeImageKey(key));
      if (snapshot == null) {
        return null;
      }
      int totalLength = (int) snapshot.getLength(0);
      InputStream is = snapshot.getInputStream(0);
      byte[] byteArr = VolleyUtil.getByteArrayFromInputStream(byteArrayPool, is, totalLength,
        null, false);
      return ImageUtil.decodeBitmap(byteArr, 0, 0);
    } catch (IOException e) {
      Log.e(TAG, "Failed to get file cached image: " + key);
      e.printStackTrace();
      return null;
    } catch (ServerError e) {
      e.printStackTrace();
      return null;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    } finally {
      byteArrayPool.returnBuf(buffer);
      IOUtils.close(snapshot);
    }
  }

  @Override
  public void put(String key, Bitmap bitmap) {
    try {
      DiskLruCache cache = getCache();
      if (cache == null) {
        return;
      }
      DiskLruCache.Editor editor = cache.edit(encodeImageKey(key));
      if (editor == null) {
        // Another editor is editing the same entry,
        // which probably means another thread is caching the same image.
        return;
      }

      OutputStream outputStream = new BufferedOutputStream(editor.newOutputStream(0));
      bitmap.compress(Bitmap.CompressFormat.PNG, BITMAP_COMPRESS_HIGHEST_QUALITY, outputStream);
      outputStream.flush();
      editor.commit();
    } catch (IOException e) {
      Log.e(TAG, "Failed to cache bitmap from key: " + key);
      e.printStackTrace();
    }
  }

  @Override
  public boolean exists(String key) {
    try {
      DiskLruCache cache = getCache();
      if (cache == null) {
        return false;
      }
      return cache.get(encodeImageKey(key)) != null;
    } catch (IOException e) {
      Log.e(TAG, "Failed to check file cached image from key: " + key);
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public void clear() {
    synchronized (cacheLock) {
      try {
        cache.delete();
      } catch (IOException e) {
        Log.e(TAG, "Failed to clear file cached image.");
        e.printStackTrace();
      } finally {
        cache = null;
      }
    }
  }

  @Override
  public long size() {
    DiskLruCache cache = getCache();
    if (cache == null) {
      return 0;
    }
    return cache.size();
  }

  private static String encodeImageKey(String key) {
    MessageDigest md5 = null;
    try {
      md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
    return convertToHex(md5.digest(key.getBytes()));
  }

  private static String convertToHex(byte[] byteData) {
    Formatter formatter = new Formatter();
    for (byte b : byteData) {
      formatter.format("%02x", b);
    }
    String ret = formatter.out().toString();
    formatter.close();
    return ret;
  }

}
