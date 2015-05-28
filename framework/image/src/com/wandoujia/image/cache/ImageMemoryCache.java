package com.wandoujia.image.cache;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import com.android.volley.toolbox.ImageLoader;

/**
 * Memory cache for displaying recent bitmap.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public final class ImageMemoryCache implements ImageCache, ImageLoader.ImageCache {

  private static final long RELEASE_RESOURCES_INTERVAL_MS = 60 * 1000L;
  private LruCache<String, BitmapWrapper> cache;

  public ImageMemoryCache(int capacity) {
    cache = new LruCache<String, BitmapWrapper>(capacity) {
      @Override
      protected int sizeOf(String key, BitmapWrapper value) {
        return value.bitmap.getRowBytes() * value.bitmap.getHeight();
      }
    };
    new Timer().scheduleAtFixedRate(new TimerTask() {

        @Override
        public void run() {
          Map<String, BitmapWrapper> snapShot = cache.snapshot();
          long currentTime = System.currentTimeMillis();
          for (Map.Entry<String, BitmapWrapper> entry : snapShot.entrySet()) {
            if (currentTime - entry.getValue().lastHitTimeMs > RELEASE_RESOURCES_INTERVAL_MS) {
              cache.remove(entry.getKey());
            }
          }
        }
      },
      RELEASE_RESOURCES_INTERVAL_MS, RELEASE_RESOURCES_INTERVAL_MS);
  }

  @Override
  public Bitmap get(String key) {
    BitmapWrapper bitmapWrapper = cache.get(key);
    if (bitmapWrapper != null) {
      bitmapWrapper.lastHitTimeMs = System.currentTimeMillis();
      return bitmapWrapper.bitmap;
    }
    return null;
  }

  @Override
  public void put(String key, Bitmap bitmap) {
    BitmapWrapper bitmapWrapper = new BitmapWrapper();
    bitmapWrapper.bitmap = bitmap;
    bitmapWrapper.lastHitTimeMs = System.currentTimeMillis();
    cache.put(key, bitmapWrapper);
  }

  @Override
  public boolean exists(String key) {
    return cache.get(key) != null;
  }

  @Override
  public void clear() {
    cache.evictAll();
  }

  @Override
  public long size() {
    return cache.size();
  }

  @Override
  public Bitmap getBitmap(String url) {
    return get(url);
  }

  @Override
  public void putBitmap(String url, Bitmap bitmap) {
    put(url, bitmap);
  }

  private static class BitmapWrapper {
    private Bitmap bitmap;
    private long lastHitTimeMs;
  }
}
