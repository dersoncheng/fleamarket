package com.wandoujia.base.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.wandoujia.base.config.GlobalConfig;

/**
 * Created by liuyaxin on 13-8-25.
 */
public class AppUtils {

  private static final int BUFFER_SIZE = 128 * 1024;
  private static final int MAX_ICON_SIZE = 300;

  public static String caculateAppMd5(String packageName) {
    Context ctx = GlobalConfig.getAppContext();
    try {
      PackageInfo pkgInfo = ctx.getPackageManager().getPackageInfo(packageName, 0);
      File file = new File(pkgInfo.applicationInfo.sourceDir);
      if (file.exists()) {
        BufferedInputStream input = null;
        String md5 = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
          input = new BufferedInputStream(new FileInputStream(file));
          md5 = MD5Utils.md5Digest(input, buffer);
          return md5;
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          if (input != null) {
            try {
              input.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    } catch (RuntimeException e) {
    }
    return "";
  }

  public static PackageInfo getPackageInfo(Context context, String packageName, int flag) {
    PackageManager packageManager = context.getPackageManager();
    PackageInfo packageInfo = null;
    try {
      packageInfo = packageManager.getPackageInfo(packageName, flag);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    } catch (RuntimeException e) {
      // In some ROM, there will be a PackageManager has died exception. So we catch it here.
      e.printStackTrace();
    }
    return packageInfo;
  }

  public static byte[] getIconBytesFromPkgInfo(ApplicationInfo info,
                                               PackageManager packageManager) {
    if (info != null) {
      try {
        Drawable drawable = info.loadIcon(packageManager);
        if (drawable == null || drawable.getIntrinsicHeight() > MAX_ICON_SIZE
            || drawable.getIntrinsicWidth() > MAX_ICON_SIZE) {
          return null;
        }
        return drawableToBytes(drawable);
      } catch (OutOfMemoryError error) {
      }
    }
    return null;
  }

  public static byte[] drawableToBytes(Drawable drawable) {
    Bitmap bitmap = ImageUtil.drawableToBitmap(drawable);
    if (bitmap != null) {
      return bitmapToPNGBytes(bitmap);
    } else {
      return null;
    }
  }

  private static byte[] bitmapToPNGBytes(Bitmap bm) {
    if (bm != null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
      return baos.toByteArray();
    }
    return null;
  }

  public static String convertFirstCharToPinyin(String label) {
    if (TextUtils.isEmpty(label)) {
      return null;
    }
    String normalizedLabel = label.trim();
    if (TextUtils.isEmpty(normalizedLabel)) {
      return null;
    }

    String pinyin;
    char firstLetter = normalizedLabel.charAt(0);
    if (TextUtil.isChinese(firstLetter)) {
      pinyin = TextUtil.convert2Pinyin(
          GlobalConfig.getAppContext(), normalizedLabel.substring(0, 1));
      pinyin = pinyin.trim().toUpperCase() + normalizedLabel.substring(1);
    } else {
      pinyin = normalizedLabel;
    }
    pinyin = pinyin.trim();
    pinyin = pinyin.replaceAll("^[\\s ]*|[\\s ]*$", "");
    return pinyin.toUpperCase();
  }

}
