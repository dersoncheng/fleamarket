package com.zhangyu.fleamarket.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketUtils {
  public static boolean isFleaMarket(Context context, String packageName) {
    return packageName != null && packageName.equals(context.getPackageName());
  }

  public static String getProcessName(Context context) {
    int myPid = android.os.Process.myPid();
    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
    for (ActivityManager.RunningAppProcessInfo process : processes) {
      if (process.pid == myPid) {
        return process.processName;
      }
    }
    return "";
  }
}
