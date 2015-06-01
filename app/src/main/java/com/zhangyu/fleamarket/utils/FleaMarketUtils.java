package com.zhangyu.fleamarket.utils;

import android.app.ActivityManager;
import android.content.Context;

import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.app.FleaMarketApplication;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketUtils {
  private static final Context applictionContext = FleaMarketApplication.getAppContext();
  private static final int ONE_THOUSAND = 1000;
  private static final int ONE_MILLION = 1000000;

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

  public static String formarVideoCount(long count) {
    String countString = "";
    if (count <= 0) {
      return countString;
    }
    if (count > ONE_MILLION) {
      StringBuilder formatBuilder = new StringBuilder();
      Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
      countString =
              formatter.format("%2.0f",
                      ((float) count) / (float) ONE_MILLION).toString();
      return String.format(applictionContext.getString(
              R.string.flea_one_million), countString);
    }else if(count > ONE_THOUSAND){
      StringBuilder formatBuilder = new StringBuilder();
      Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
      countString =
              formatter.format("%2.0f",
                      ((float) count) / (float) ONE_THOUSAND).toString();
      return String.format(applictionContext.getString(
              R.string.flea_one_thound), countString);
    } else {
      countString =
              String.format(applictionContext.getString(
                              R.string.flea_count_one_thousand),
                      String.valueOf(count));
      return countString;
    }
  }

  public static String formatTime(long updateDate) {
    long interval = System.currentTimeMillis() - updateDate;
    return intervalToTime(interval);
  }

  private static String intervalToTime(long interval) {
    int day = (int) (interval / (24 * 60 * 60 * 1000));
    if (day == 0) {
      return FleaMarketApplication.getAppContext().getString(R.string.video_today);
    } else if (day > 0 && day < 7) {
      return day + " " + FleaMarketApplication.getAppContext().getString(R.string.video_days_ago);
    } else if (day >= 7 && day < 30) {
      return ((day / 7) + 1) + " " + FleaMarketApplication.getAppContext().getString(R.string.video_weeks_ago);
    } else if (day >= 30 && day < 360) {
      return ((day / 30) + 1) + " " + FleaMarketApplication.getAppContext().getString(R.string.video_month_ago);
    } else {
      return ((day / 360) + 1) + " " + FleaMarketApplication.getAppContext().getString(R.string.video_years_ago);
    }
  }
}
