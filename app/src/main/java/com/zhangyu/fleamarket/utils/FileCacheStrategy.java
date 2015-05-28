package com.zhangyu.fleamarket.utils;

import android.os.Environment;
import android.os.StatFs;

import com.zhangyu.fleamarket.configs.Config;

/**
 * Strategy helper to determine file cache location.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class FileCacheStrategy {

  private static final long BITMAP_MIN_INTERNAL_REQUIREMENT_SIZE = 256 * 1024 * 1024; // 256MB

  /**
   * Interface for getting decision whether to save cache to external storage.
   */
  public interface CacheLocation {
    /**
     * Returns whether to save cache to external storage.
     *
     * @return True if saving cache to external storage.
     */
    boolean useExternalStorage();
  }

  public static final CacheLocation getStrategy() {
    return new PreferInternal();
  }

  private FileCacheStrategy() {
  }

  /**
   * Strategy to prefer saving cache to internal storage.
   */
  public static class PreferInternal implements CacheLocation {

    @Override
    public boolean useExternalStorage() {
      if (!Config.doesExternalStorageForCacheExist()) {
        boolean useExternal =
            getAvailableInternalSpaces() < BITMAP_MIN_INTERNAL_REQUIREMENT_SIZE
                && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        Config.setUseExternalStorageForCache(useExternal);
        return useExternal;
      } else {
        return Config.getUseExternalStorageForCache();
      }
    }

    private static long getAvailableInternalSpaces() {
      StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
      return (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
    }
  }
}
