package com.wandoujia.base.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;

public class PackageUtils {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static ApkPackageInfo getApkPakcageInfo(Context context, String apkPath) {
    ApkPackageInfo apkInfo = new ApkPackageInfo();
    try {
      PackageInfo pkgInfo = context.getPackageManager().getPackageArchiveInfo(apkPath, 0);
      if (pkgInfo == null) {
        return null;
      }
      apkInfo.packageName = pkgInfo.packageName;
      apkInfo.versionCode = pkgInfo.versionCode;
      apkInfo.versionName = pkgInfo.versionName;
      CharSequence nameCharSeq = pkgInfo.applicationInfo.loadLabel(context.getPackageManager());
      apkInfo.name = nameCharSeq == null ? "" : nameCharSeq.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return apkInfo;
  }

  public static String readApkAssetFile(String apkPath, String filePath) {
    BufferedInputStream input = null;
    try {
      String classAssetManager = "android.content.res.AssetManager";
      Class assetMgrCls = Class.forName(classAssetManager);
      Constructor assetMgrCt = assetMgrCls.getConstructor((Class[]) null);
      AssetManager assetMgr = (AssetManager) assetMgrCt.newInstance();
      Method addAssetPath =
          assetMgrCls.getDeclaredMethod("addAssetPath", new Class[] {String.class});
      addAssetPath.invoke(assetMgr, new Object[] {apkPath});
      input = new BufferedInputStream(assetMgr.open(filePath));
      byte[] buffer = new byte[input.available()];
      int count = input.read(buffer);
      return new String(buffer, 0, count);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (input != null) {
          input.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return "";
  }

  public static class ApkPackageInfo {
    public String name;
    public String packageName;
    public int versionCode = -1;
    public String versionName;
  }

}
