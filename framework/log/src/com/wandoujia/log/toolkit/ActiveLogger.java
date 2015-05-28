package com.wandoujia.log.toolkit;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.wandoujia.base.services.AlarmService;

/**
 * Created by duguguiyu on 13-12-24.
 */
public class ActiveLogger implements AlarmService.ScheduleChecker {

  private static final String TAG = "Active";
  private static final boolean DEBUG = false;

  private static final String KEY_LAST_ACTIVE_TIME = "last_active_time";

  // 11 Hours (two time a day)
  private static final long LOG_ACTIVE_DURATION = DEBUG ? 10000L : 11L * 60L * 60L * 1000L;
  private static final long LOG_SEND_DURATION = 15000L;

  private Executor executor;

  public ActiveLogger(Executor executor) {
    this.executor = executor;
  }

  @Override
  public void scheduleCheck(Context context, final AlarmService.Callback callback) {
    if (!logActiveEvent(context)) {
      callback.onCompleted();
      return;
    }

    // Just wait a moment, for the log sent completed.
    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
      @Override
      public void run() {
        if (DEBUG) {
          Log.d(TAG, "Wait completed, the log may be sent.");
        }
        callback.onCompleted();
      }
    }, LOG_SEND_DURATION);
  }

  /**
   * Log the active event.
   * 
   * @param context
   * @return event is sent indeed
   */
  public boolean logActiveEvent(Context context) {
    // Check condition.
    if (!needLogActive(context)) {
      if (DEBUG) {
        Log.d(TAG, "Cancel active, the duration was not reach.");
      }
      return false;
    }

    // Log it.
    executor.logAsActive();

    // Save log time.
    saveLastActiveTime(context);
    return true;
  }

  private static boolean needLogActive(Context context) {
    long duration = System.currentTimeMillis() - getLastActiveTime(context);
    if (duration < 0) {
      // If duration was wrong, REFRESH it at once.
      Log.w(TAG, "The time has been changed, duration is " + duration);
      return true;
    }
    return duration > LOG_ACTIVE_DURATION;
  }

  private static long getLastActiveTime(Context context) {
    return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getLong(
        KEY_LAST_ACTIVE_TIME, 0L);
  }

  private static void saveLastActiveTime(Context context) {
    context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit()
        .putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis()).commit();
  }

  public interface Executor {
    void logAsActive();
  }
}
