package com.wandoujia.image.cache;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

/**
 * Composite image cache, which can combine multiple cache into one. Thread safe for
 * {@link ImageCompositeCache#get(String) and ImageCompositeCache#put(String, Bitmap)}.
 *
 * @author @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public final class ImageCompositeCache implements ImageCache {

  private final List<ImageCache> caches = new ArrayList<ImageCache>();

  @Override
  public Bitmap get(String key) {
    Bitmap image = null;

    for (int i = 0; i < caches.size(); ++i) {
      ImageCache cache = caches.get(i);
      synchronized (cache) {
        image = cache.get(key);
      }
      if (image != null) {
        cacheImage(key, image, i);
        break;
      }
    }

    return image;
  }

  private void cacheImage(String url, Bitmap image, int count) {
    for (int i = count - 1; i >= 0; --i) {
      ImageCache cache = caches.get(i);
      synchronized (cache) {
        cache.put(url, image);
      }
    }
  }

  @Override
  public void put(String url, Bitmap bitmap) {
    for (ImageCache cache : caches) {
      synchronized (cache) {
        cache.put(url, bitmap);
      }
    }
  }

  @Override
  public boolean exists(String url) {
    for (ImageCache cache : caches) {
      if (cache.exists(url)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void clear() {
    for (ImageCache cache : caches) {
      synchronized (cache) {
        cache.clear();
      }
    }
  }

  /**
   * Adds image cache into the end of collection. Image cache will be queried by the order in the
   * collection.
   *
   * @param cache Image cache to add
   */
  public void add(ImageCache cache) {
    caches.add(cache);
  }

  /**
   * Removes image cache from collection.
   *
   * @param cache Image cache to remove
   */
  public void remove(ImageCache cache) {
    caches.remove(cache);
  }

  @Override
  public long size() {
    return 0;
  }

}
