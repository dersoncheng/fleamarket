package com.wandoujia.rpc.http.cache;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.wandoujia.gson.Gson;
import com.wandoujia.gson.JsonIOException;
import com.wandoujia.gson.JsonParseException;
import com.wandoujia.gson.stream.JsonReader;
import com.wandoujia.gson.stream.JsonWriter;

/**
 * Data file cache.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class FileCache implements DataCache {

  private static final String TAG = FileCache.class.getSimpleName();
  // Version number used in cache, not necessarily equals to the app version in manifest XML file.
  private static final int CACHE_APP_VERSION = 1;
  private static final long MAX_FILE_CACHE_SIZE = 10L * 1024L * 1024L; // 10M

  private final String cacheDirPath;
  private final byte[] cacheLock = new byte[0];
  private final Gson gson = new Gson();
  private DiskLruCache cache;

  public FileCache(String cacheDir) {
    cacheDirPath = cacheDir;
  }

  private DiskLruCache getCache() {
    synchronized (cacheLock) {
      if (cache == null) {
        File cacheDir = new File(cacheDirPath);
        if (cacheDir.exists()) {
          if (cacheDir.isFile()) {
            cacheDir.delete();
            cacheDir.mkdir();
          }
        } else {
          cacheDir.mkdir();
        }
        try {
          cache = DiskLruCache.open(cacheDir, CACHE_APP_VERSION, 1, MAX_FILE_CACHE_SIZE);
        } catch (IOException e) {
          Log.e(TAG, "Failed to open cache directory");
          e.printStackTrace();
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
  public void remove(String key) {
    try {
      DiskLruCache cache = getCache();
      if (cache != null) {
        cache.remove(encodeImageKey(key));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public CacheItemWrapper get(String key) {
    DiskLruCache.Snapshot snapshot = null;
    try {
      DiskLruCache cache = getCache();
      if (cache != null) {
        snapshot = cache.get(encodeImageKey(key));
        if (snapshot != null) {
          JsonReader reader = new JsonReader(
              new BufferedReader(new InputStreamReader(snapshot.getInputStream(0))));
          return gson.fromJson(reader, CacheItemWrapper.class);
        }
      }
    } catch (IOException e) {
      Log.e(TAG, "Failed to get file cached image: " + key);
      e.printStackTrace();
    } catch (JsonParseException e) {
      Log.e(TAG, "Failed to get file cached image: " + key);
      e.printStackTrace();
    } finally {
      if (snapshot != null) {
        snapshot.close();
      }
    }
    return null;
  }

  @Override
  public void put(String key, CacheItemWrapper cacheItem) {
    JsonWriter writer = null;
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

      writer = new JsonWriter(new OutputStreamWriter(
          new BufferedOutputStream(editor.newOutputStream(0))));
      gson.toJson(cacheItem, CacheItemWrapper.class, writer);
      writer.flush();
      editor.commit();
    } catch (IOException e) {
      Log.e(TAG, "Failed to cache bitmap from key: " + key);
      e.printStackTrace();
    } catch (JsonIOException e) {
      Log.e(TAG, "Failed to cache bitmap from key: " + key);
      e.printStackTrace();
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
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
      if (cache != null) {
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
