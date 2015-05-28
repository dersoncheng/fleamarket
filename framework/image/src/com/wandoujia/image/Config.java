package com.wandoujia.image;

import android.content.Context;
import android.content.res.Resources;

/**
 * Image manager configurations.
 *
 * liuchunyu@wandoujia.com (Chunyu Liu)
 */
public interface Config {
  Context getContext();
  String getFileCacheDir();
  Resources getResources();
  int getFileCacheSize();
  int getMemoryCacheSize();
  int getNetworkThreadPoolSize();
  int getLocalThreadPoolSize();
}
