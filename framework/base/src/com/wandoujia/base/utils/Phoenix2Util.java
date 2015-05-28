package com.wandoujia.base.utils;

import android.content.Context;

/**
 * Created by wanzheng on 2/6/14.
 */
public class Phoenix2Util {
  private static final String PHOENIX2_PREFIX = "com.wandoujia.phoenix2";

  public static boolean isPhoenix2(String packageName) {
    return (packageName != null && packageName.startsWith(PHOENIX2_PREFIX));
  }

  public static String resolvePhoenix2PackageName(Context context) {
    // TODO:
    //   resolve the package name of Phoenix2 App, by using BroadcastReceiver for instance
    //   and return the default one if failed
    return PHOENIX2_PREFIX;
  }
}
