package com.wandoujia.base.log;

import android.annotation.TargetApi;
import android.os.Build;

import com.wandoujia.base.config.GlobalConfig;

/**
 * A log utility for replacing {@link android.util.Log}.
 * Using this log, we can get controll all of our logs, manager tags,
 * or close log output when release.
 *
 * @author zhulantian@wandoujia.com (Lantian Zhu)
 */
public class Log {

  private static String tagPrefix = "";

  private Log() {
    // empty
  }

  public static void i(String tag, String msg, Object... args) {
    if (GlobalConfig.isDebug()) {
      android.util.Log.i(tagPrefix + tag, formatString(msg, args));
    }
  }

  public static void v(String tag, String msg, Object... args) {
    if (GlobalConfig.isDebug()) {
      android.util.Log.v(tagPrefix + tag, formatString(msg, args));
    }
  }

  public static void d(String tag, String msg, Object... args) {
    if (GlobalConfig.isDebug()) {
      android.util.Log.d(tagPrefix + tag, formatString(msg, args));
    }
  }

  public static void w(String tag, String msg, Object... args) {
    if (GlobalConfig.isDebug()) {
      android.util.Log.w(tagPrefix + tag, formatString(msg, args));
    }
  }

  public static void w(String tag, Throwable th) {
    if (GlobalConfig.isDebug()) {
      android.util.Log.w(tagPrefix + tag, th);
    }
  }

  public static void e(String tag, String msg, Object... args) {
    if (GlobalConfig.isDebug()) {
      android.util.Log.e(tagPrefix + tag, formatString(msg, args));
    }
  }

  public static void e(String tag, String msg, Throwable th) {
    if (GlobalConfig.isDebug()) {
      android.util.Log.e(tagPrefix + tag, msg, th);
    }
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  public static void wtf(String tag, String msg, Object... args) {
    if (GlobalConfig.isDebug()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        android.util.Log.wtf(tagPrefix + tag, formatString(msg, args));
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  public static void wtf(String tag, String msg, Throwable th) {
    if (GlobalConfig.isDebug()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        android.util.Log.wtf(tagPrefix + tag, msg, th);
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  public static void wtfStack(String tag, String msg, Throwable th) {
    if (GlobalConfig.isDebug()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        android.util.Log.wtf(tagPrefix + tag, formatString(msg));
        android.util.Log.wtf(tagPrefix + tag, android.util.Log.getStackTraceString(th));
      }
    }
  }

  public static void printStackTrace(Throwable th) {
    if (th != null && GlobalConfig.isDebug()) {
      th.printStackTrace();
    }
  }

  private static String formatString(String msg, Object... args) {
    if (args.length > 0) {
      return String.format(msg, args);
    } else {
      return msg;
    }
  }

  public static String tag(Class clazz) {
    return clazz.getSimpleName();
  }

  public static void setTagPrefix(String prefix) {
    tagPrefix = prefix;
  }
}
