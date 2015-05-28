package com.wandoujia.base.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

import com.wandoujia.base.R;
import com.wandoujia.base.config.GlobalConfig;

public class DateUtil {
  public static long MIN_SECOND_LONG = 1000000000L;
  public static long MIN_MILLISECOND_LONG = 1000000000000L;
  public static long MIN_MICROSECOND_LONG = 1000000000000000L;
  public static long DAY = 24 * 60 * 60 * 1000;
  public static long WEEK = 7 * DAY;
  public static long MONTH = 30 * DAY;
  private static int MONTHS_OF_YEAR = 12;
  private static int DAYS_OF_WEEK = 7;

  private static boolean isSecond(long second) {
    if (second < MIN_SECOND_LONG || second > MIN_MILLISECOND_LONG) {
      return false;
    } else {
      return true;
    }
  }

  private static boolean isMillisecond(long millisecond) {
    if (millisecond < MIN_MILLISECOND_LONG
        || millisecond > MIN_MICROSECOND_LONG) {
      return false;
    } else {
      return true;
    }
  }

  private static boolean isMicrosecond(long microsecond) {
    if (microsecond < MIN_MICROSECOND_LONG) {
      return false;
    } else {
      return true;
    }
  }

  public static long checkLongIsSecond(long oldtimes) {
    if (isSecond(oldtimes)) {
      return oldtimes;
    } else {
      if (isMillisecond(oldtimes)) {
        return oldtimes / 1000;
      } else {
        return oldtimes * 1000;
      }
    }
  }

  public static long checkLongIsMillisecond(long oldtimes) {
    if (isMillisecond(oldtimes)) {
      return oldtimes;
    } else {
      if (isMicrosecond(oldtimes)) {
        return oldtimes / 1000;
      } else {
        return oldtimes * 1000;
      }
    }
  }

  /**
   * 
   * @param timestampString
   *          Unix time stamp 13 bit
   * @return
   */
  public static String timeStamp2Date(String timestampString) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    return timeStamp2Date(timestampString, sdf);
  }

  public static String timeStamp2Date(String timestampString,
      SimpleDateFormat sdf) {
    Long timestamp = Long.parseLong(timestampString);
    return timeStamp2Date(timestamp, sdf);
  }

  public static String timeStamp2Date(long timestamp,
      SimpleDateFormat sdf) {
    return sdf.format(new Date(timestamp));
  }

  /**
   * 
   * @param timestamp
   * @return -2:the day before yesterday -1:yesterday 0:today 1:tomorrow 2:the
   *         day after tomorrow
   */
  public static int distanceToToday(long timestamp) {
    Calendar today = Calendar.getInstance();

    Date date = new Date(timestamp);
    Calendar someDay = Calendar.getInstance();
    someDay.setTime(date);

    trimCalendar(today);
    trimCalendar(someDay);

    long interval = someDay.getTimeInMillis() - today.getTimeInMillis();
    return (int) (interval / (24 * 60 * 60 * 1000));
  }

  public static String distanceToTodayString(long timestamp) {
    int distance = distanceToToday(timestamp);
    if (distance == 0) {
      return GlobalConfig.getAppContext().getString(R.string.today);
    } else if (distance == -1) {
      return GlobalConfig.getAppContext().getString(R.string.yesterday);
    } else if (distance < 0) {
      return String.format(GlobalConfig.getAppContext().getString(R.string.days_ago),
          Math.abs(distance));
    } else {
      return String.format(GlobalConfig.getAppContext().getString(R.string.days_later),
          distance);
    }
  }

  public static String formatDistanceToToday(long timestamp) {
    return formatDistanceToToday(timestamp, false);
  }

  /**
   * 
   * @param timestamp
   * @param exactTimeAfterOneMonth return exact time(yyyy-mm-dd) if timestamp is more then one month
   *          before
   * @return
   */
  public static String formatDistanceToToday(long timestamp, boolean exactTimeAfterOneMonth) {
    Calendar today = Calendar.getInstance();
    Date date = new Date(timestamp);
    Calendar someDay = Calendar.getInstance();
    someDay.setTime(date);
    trimCalendar(today);
    trimCalendar(someDay);

    int distance = distanceToToday(timestamp);
    if (distance > 0) { // xx days later.
      return String.format(GlobalConfig.getAppContext().getString(R.string.days_later),
          distance);
    } else if (distance == 0) { // today.
      return GlobalConfig.getAppContext().getString(R.string.today);
    } else if (distance == -1) { // yesterday.
      return GlobalConfig.getAppContext().getString(R.string.yesterday);
    } else {
      int monthDistance = getMonthDistance(someDay, today);
      if (monthDistance == 0) { // within one month.
        if (Math.abs(distance) < DAYS_OF_WEEK) { // with in one week.
          if (someDay.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)) {
            return getWeekString(someDay);
          } else {
            return GlobalConfig.getAppContext().getString(R.string.last_week_prefix)
                + getWeekString(someDay);
          }
        } else if (Math.abs(distance) < DAYS_OF_WEEK * 2) { // within two week.
          return GlobalConfig.getAppContext().getString(R.string.one_week)
              + GlobalConfig.getAppContext().getString(R.string.ago);
        } else { // more than two week.
          return GlobalConfig.getAppContext().getString(R.string.two_weeks)
              + GlobalConfig.getAppContext().getString(R.string.ago);
        }
      } else { // more than one month.
        if (exactTimeAfterOneMonth) {
          return timeStamp2Date(String.valueOf(timestamp));
        } else {
          return GlobalConfig.getAppContext().getString(R.string.one_month)
              + GlobalConfig.getAppContext().getString(R.string.ago);
        }
      }
    }
  }

  public static void trimCalendar(Calendar today) {
    today.set(Calendar.HOUR_OF_DAY, 0);
    today.set(Calendar.AM_PM, Calendar.AM);
    today.set(Calendar.HOUR, 0);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    today.set(Calendar.MILLISECOND, 0);
  }

  private static String getWeekString(Calendar day) {
    Context context = GlobalConfig.getAppContext();
    switch (day.get(Calendar.DAY_OF_WEEK)) {
      case 1:
        return context.getString(R.string.sunday);
      case 2:
        return context.getString(R.string.monday);
      case 3:
        return context.getString(R.string.tuesday);
      case 4:
        return context.getString(R.string.wednesday);
      case 5:
        return context.getString(R.string.thursday);
      case 6:
        return context.getString(R.string.friday);
      case 7:
        return context.getString(R.string.saturday);
      default:
        return "";
    }
  }

  private static int getMonthDistance(Calendar someDay, Calendar today) {
    int factor = 1;
    if (someDay.after(today)) {
      Calendar temp = someDay;
      someDay = today;
      today = temp;
      factor = -1;
    }
    int yearDistance = today.get(Calendar.YEAR) - someDay.get(Calendar.YEAR);
    int monthDistance = today.get(Calendar.MONTH) - someDay.get(Calendar.MONTH);
    int dayDistance = today.get(Calendar.DAY_OF_MONTH) - someDay.get(Calendar.DAY_OF_MONTH);
    return (monthDistance + yearDistance * MONTHS_OF_YEAR + (dayDistance >= 0 ? 0 : -1)) * factor;
  }

  public static boolean isCurrentYear(long timestamp) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(timestamp);
    int year = cal.get(Calendar.YEAR);

    cal.setTimeInMillis(System.currentTimeMillis());
    int currentYear = cal.get(Calendar.YEAR);
    return year == currentYear;
  }

}
