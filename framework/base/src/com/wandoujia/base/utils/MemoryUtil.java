package com.wandoujia.base.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.text.TextUtils;

/**
 * Util for memory calculation and release
 * 
 * @author yunjie@wandoujia.com
 * 
 * @date 14-7-29.
 */
public class MemoryUtil {

  private static final String MEMINFO_FILE = "/proc/meminfo";
  private static final long KB = 1024;
  public static final long MB = 1024 * KB;
  public static final int BUFFER_SIZE = 8 * 1024;

  /**
   * (moved from GameFolderUtil.java)
   * 
   * @param context
   * @return available memory in MB
   */
  public static long getAvailMemory(Context context) {
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    am.getMemoryInfo(mi);
    return mi.availMem / MB;
  }

  /**
   * @param context
   * @return used memory in MB
   */
  public static long getUsedMemory(Context context) {
    return getTotalMemory() - getAvailMemory(context);
  }

  /**
   * (moved from GameFolderUtil.java)
   * 
   * @return total memory in MB
   */
  public static long getTotalMemory() {
    String[] memInfoArray;
    long totalMemory = 0;

    try {
      FileReader localFileReader = new FileReader(MEMINFO_FILE);
      BufferedReader localBufferedReader = new BufferedReader(
          localFileReader, BUFFER_SIZE);
      String memInfo = localBufferedReader.readLine();
      memInfoArray = memInfo.split("\\s+");
      totalMemory = Integer.valueOf(memInfoArray[1]) * KB;
      localBufferedReader.close();
    } catch (IOException ignored) {}
    return totalMemory / MB;
  }

  /**
   * Kill all processes except for the supplied packageName.Note that if the packageName
   * represents a launcher, it will be left; if the packageName is in white list, it will be left.
   * 
   * @param context
   * @param packageName packageName that want to be left, can be null. If null, kill all.
   */
  @TargetApi(8)
  public static void killAllProcesses(Context context, String packageName) {
    ActivityManager activityManger =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> processList =
        activityManger.getRunningAppProcesses();
    if (processList != null)
      for (ActivityManager.RunningAppProcessInfo processInfo : processList) {
        if (processInfo != null && processInfo.pkgList != null) {
          String[] pkgList = processInfo.pkgList;
          if (processInfo.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
            for (String pkgName : pkgList) {
              if (!TextUtils.isEmpty(packageName) && pkgName.equals(packageName)) {
                continue;
              }

              if (!isAppKillable(context, packageName)) {
                continue;
              }

              if (SystemUtil.aboveApiLevel(Build.VERSION_CODES.FROYO)) {
                activityManger.killBackgroundProcesses(pkgName);
              } else {
                activityManger.restartPackage(pkgName);
              }
            }
          }
        }
      }
  }

  /**
   * kill process corresponding to the packageName. Note that if the packageName
   * represents a launcher or if the packageName is in white list, do nothing.
   * 
   * @param context
   * @param packageName packageName of the process to be killed
   */
  @TargetApi(8)
  public static void killProcess(Context context, String packageName) {
    if (!isAppKillable(context, packageName)) {
      return;
    }
    ActivityManager activityManger =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (SystemUtil.aboveApiLevel(Build.VERSION_CODES.FROYO)) {
      activityManger.killBackgroundProcesses(packageName);
    } else {
      activityManger.restartPackage(packageName);
    }
  }

  /**
   * return used memory of the specified app in Byte
   * 
   * @param context
   * @param packageName packageName of the app
   * @return
   */
  @TargetApi(5)
  public static long getUsedMemory(Context context, String packageName) {
    ActivityManager activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> processList =
        activityManager.getRunningAppProcesses();
    if (processList == null) {
      return 0L;
    }
    for (ActivityManager.RunningAppProcessInfo processInfo : processList) {
      String[] pkgList = processInfo.pkgList;
      if (pkgList == null || pkgList.length == 0) {
        continue;
      }
      for (String pkgName : pkgList) {
        if (pkgName.equals(packageName)) {
          Debug.MemoryInfo[] memoryInfos = activityManager.getProcessMemoryInfo(new
              int[] {processInfo.pid});
          return memoryInfos[0].getTotalPss() * KB;
        }
      }
    }
    return 0L;
  }

  public static boolean isAppKillable(Context context, String packageName) {
    String launcher = LauncherUtil.getDefaultLauncher(context);
    boolean isKillable = true;
    if (!TextUtils.isEmpty(launcher) && launcher.equals(packageName)) {
      isKillable = false;
    }
    if (AppKillBlacklistUtil.isAppInWhiteList(packageName)) {
      isKillable = false;
    }
    return isKillable;
  }
}
