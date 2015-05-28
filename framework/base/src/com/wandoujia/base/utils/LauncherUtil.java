package com.wandoujia.base.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

/**
 * provide function to fetch launcher infos.
 * 
 * moved to base by yunjie on 14-7-29
 */
public class LauncherUtil {

  public static final String READ_SETTINGS = "READ_SETTINGS";
  public static final String WRITE_SETTINGS = "WRITE_SETTINGS";

  private LauncherUtil() {}

  /**
   * send intent to request back to launcher, so that we can check respond activity from
   * resloveInfo.
   * 
   * @param context application context
   * @return launcher name
   */
  public static String getDefaultLauncher(Context context) {
    String defaultLauncher = null;
    try {
      final Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.addCategory(Intent.CATEGORY_HOME);
      final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
      if (!res.activityInfo.packageName.equals("android")) {
        defaultLauncher = res.activityInfo.packageName;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return defaultLauncher;
  }

  /**
   * Get authority based on permission.
   * 
   * @param context application context
   * @param permission read or write permission.
   * @return authority if found.
   */
  public static String getAuthorityFromPermission(Context context, String permission) {
    if (TextUtils.isEmpty(permission) || context == null) {
      return null;
    }
    String launcher = getDefaultLauncher(context);
    if (TextUtils.isEmpty(launcher)) {
      // unable to fetch launcher user chosen, return
      return null;
    }
    try {
      // user may have multi launchers installed, thus here need to ensure package name
      // equals with launcher name, otherwise may return wrong authority.
      final PackageInfo packageInfo =
          context.getPackageManager().getPackageInfo(launcher, PackageManager.GET_PROVIDERS);
      if (packageInfo == null) {
        return null;
      }
      ProviderInfo[] providers = packageInfo.providers;
      if (providers == null) {
        return null;
      }
      for (ProviderInfo provider : providers) {
        if ((!TextUtils.isEmpty(provider.readPermission)
            && provider.readPermission.contains(permission))
            || (!TextUtils.isEmpty(provider.writePermission)
            && provider.writePermission.contains(permission))) {
          return provider.authority;
        }
      }
    } catch (Exception e) {
      // may have kind of error, try catch it.
      e.printStackTrace();
    }
    return null;
  }

}
