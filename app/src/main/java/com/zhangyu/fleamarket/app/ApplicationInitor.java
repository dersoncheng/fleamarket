package com.zhangyu.fleamarket.app;

import android.content.Context;

import com.wandoujia.base.utils.LauncherUtil;
import com.zhangyu.fleamarket.configs.Config;
import com.zhangyu.fleamarket.utils.ShortcutUtil;

public class ApplicationInitor {
  private static boolean isInited = false;

  public static boolean isInited() {
    return isInited;
  }

  private ApplicationInitor() {
  }

  public static synchronized void initApplication() {
    if (isInited) {
      return;
    }
    isInited = true;

//    new Thread(new Runnable() {
//      @Override
//      public void run() {
//        FleaMarketOnlineConfigController.getInstance().updateOnlineConfig();
//        Config.preLoadPreferences();
//        createShortcutIfNecessary();
//        //NotifyCardManager.getInstance();
//        //StatisticsTools.initParam();
//        if (Config.getFirstLaunchAppTime() == 0) {
//          Config.setFirstLaunchAppTime(System.currentTimeMillis());
//        }
//      }
//    }).start();
  }

  private static void createShortcutIfNecessary() {
    Context context = FleaMarketApplication.getAppContext();
    String current_launcher = LauncherUtil.getAuthorityFromPermission(context, LauncherUtil.READ_SETTINGS);
    if (!Config.isShortcutCreated() && !ShortcutUtil.isLauncherInBlackList(current_launcher)) {
      ShortcutUtil.createDefaultShortcut(context);
      Config.setShortcutCreated(true);
    }
  }
}
