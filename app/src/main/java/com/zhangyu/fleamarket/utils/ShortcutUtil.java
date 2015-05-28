package com.zhangyu.fleamarket.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.wandoujia.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.wandoujia.log.toolkit.LaunchLogger;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.configs.Config;
import com.zhangyu.fleamarket.log.LaunchLogExecutor;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class ShortcutUtil {
  public static final String[] LAUNCHER_BLACKLIST = {
    "com.miui.home",
  };
  public static final String OLD_DEFAULT_SHORTCUT_COMPONENT =
    "com.zhangyu.fleamarket.NewWelcomeActivity";

  private static final String ACTION_INSTALL_SHORTCUT =
    "com.android.launcher.action.INSTALL_SHORTCUT";
  private static final String ACTION_UNINSTALL_SHORTCUT =
    "com.android.launcher.action.UNINSTALL_SHORTCUT";
  private static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

  public enum ShortcutType {
    DEFAULT
  }

  private static CachedThreadPoolExecutorWithCapacity singleCachedThread =
    new CachedThreadPoolExecutorWithCapacity(1);

  public static boolean isLauncherInBlackList(String authority) {
    if (TextUtils.isEmpty(authority)) {
      return true;
    }
    for (String launcher : LAUNCHER_BLACKLIST) {
      if (authority.contains(launcher)) {
        return true;
      }
    }
    return false;
  }

  public static void createDefaultShortcut(final Context context) {
    singleCachedThread.execute(new Runnable() {

      @Override
      public void run() {
        delShortcut(context, R.string.app_name, OLD_DEFAULT_SHORTCUT_COMPONENT);
        addDefaultShortcut(context, R.drawable.icon, R.string.app_name);
        Config.setShortcutExisted(
          context, ShortcutType.DEFAULT, true);
      }
    });
  }

  private static void delShortcut(Context context, int nameRes, String cls) {
    Intent shortcut = new Intent(ACTION_UNINSTALL_SHORTCUT);
    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(nameRes));
    ComponentName comp = new ComponentName(context.getPackageName(), cls);
    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN)
      .setComponent(comp));
    context.sendBroadcast(shortcut);
  }

  private static void addDefaultShortcut(Context context, int iconResId, int nameResId) {
    Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
    shortcutIntent.setPackage(context.getPackageName());
    shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    shortcutIntent.putExtra(LaunchLogger.EXTRA_LAUNCH_FROM, LaunchLogExecutor.LaunchSource.SHORTCUT);
    addShortcut(context, iconResId, nameResId, shortcutIntent);
  }

  public static void addShortcut(Context context, int iconResId, int nameResId, Intent shortcutIntent) {
    Intent intent = new Intent(ACTION_INSTALL_SHORTCUT);
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
      context.getString(nameResId));
    intent.putExtra(EXTRA_SHORTCUT_DUPLICATE, false);
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
      Intent.ShortcutIconResource.fromContext(context, iconResId));
    context.sendBroadcast(intent);
  }
}
