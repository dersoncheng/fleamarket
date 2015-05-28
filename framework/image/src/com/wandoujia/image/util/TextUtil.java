package com.wandoujia.image.util;

import android.text.TextUtils;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class TextUtil {
  private TextUtil() {}

  public static boolean isPackageName(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return false;
    }
    String packageNameRegExp = "^([a-zA-Z]+[.][a-zA-Z]+)[.]*.*";
    return packageName.matches(packageNameRegExp);
  }

  public static boolean isNumber(String numString) {
    if (TextUtils.isEmpty(numString)) {
      return false;
    }
    String numberRegExp = "^[1-9]\\d*$";
    return numString.matches(numberRegExp);
  }

  public static boolean isNetUrl(String url) {
    if (TextUtils.isEmpty(url)) {
      return false;
    }
    return url.startsWith("http://") || url.startsWith("https://");
  }

  public static boolean isApkFilePath(String filePath) {
    if (TextUtils.isEmpty(filePath)) {
      return false;
    }
    return filePath.startsWith("/") || filePath.endsWith(".apk");
  }
}
