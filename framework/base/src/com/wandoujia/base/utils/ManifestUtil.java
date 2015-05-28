package com.wandoujia.base.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

/**
 * Created by zhangnan on 2/28/14.
 */
public class ManifestUtil {
  public static String getMetaInfo(Context context, String key) {
    try {
      ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
          context.getPackageName(),
          PackageManager.GET_META_DATA);
      Bundle bundle = appInfo.metaData;
      String channel = bundle.getString(key);
      if (channel != null) {
        return channel;
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    } catch (NullPointerException e) {
      e.printStackTrace();
    } catch (RuntimeException e) {
      // In some ROM, there will be a PackageManager has died exception. So we catch it here.
      e.printStackTrace();
    }
    return "";
  }
}
