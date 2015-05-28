package com.wandoujia.base.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * AlarmService for schedule alarm , which default will restart every one hour.
 * <p/>
 * The Application that wanna use AlarmService must extends this service, and override abstract
 * method {@link #initCheckerList()} to declare checker which want to be alarm.
 * <p/>
 * The method {@link #scheduleAlarm(android.content.Context, String, Class)} should be invoke in
 * application start (network/application change is optional)
 * <p/>
 * The Event what to be alarmed must be wrapped in
 * {@link com.wandoujia.base.services.AlarmService.ScheduleChecker}
 * <p/>
 * Created by huwei on 13-11-30.
 * move to base by xiaobo on 14-03-12
 */
public abstract class AlarmService extends Service {
  private static final String TAG = "AlarmService";

  private static final long DEFAULT_DURATION = 60L * 60 * 1000; // one hour milliseconds

  private static final int DEFAULT_TIMEOUT_MILL_SECONDS = 60 * 10 * 1000; // 10 minutes

  private static final int DEFAULT_CHECK_INTERVAL = 1000 * 10; // 10 seconds

  private static final int ONE_MINUTE_DELAY = 60 * 1000;

  private static final int CHECK_COMPLETED = 1000;

  private static final int NONE_CHECKER = 0;

  private static final String ALARM_ACTION = "ALARM_ACTION";

  private CountDownTimer countDownTimer;

  private Thread checkThread;

  private int checkNum;

  private String action;

  // It's used for control countTimer finished, for countTimer can't stop self immediately
  private boolean finished = false;

  // a list save the checker to schedule call.
  // someone who want to add checker, must add a constructor in here
  private static List<ScheduleChecker> checkerList;

  private final Handler handler = new Handler() {

    public void handleMessage(Message message) {
      switch (message.what) {
        case CHECK_COMPLETED:
          checkNum--;
          if (checkNum == NONE_CHECKER) {
            stopSelf();
          }
          break;
        default:
          break;
      }
    }
  };

  /**
   * a callback for checker.
   * the caller can call in any thread.
   */
  public interface Callback {
    /**
     * Gets call when the operation has completed.
     * invoker must call this onCompleted after has executed, if not call, will be closed
     * after 10 minutes
     */
    void onCompleted();
  }

  /**
   * a ScheduleChecker for some one want use this alarm.
   */
  public interface ScheduleChecker {
    /**
     * alarm service will schedule invoke this method.
     * the implement call sync or async this method either.
     *
     * @param context
     * @param callback the callback checker must invoke after the operation has completed
     */
    void scheduleCheck(Context context, Callback callback);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "on Create");
    checkerList = initCheckerList();
    if (checkerList == null) {
      checkerList = new ArrayList<ScheduleChecker>();
    }
  }

  /**
   * @return
   */
  protected abstract List<ScheduleChecker> initCheckerList();

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "on Start");
    scheduleNextAlarm();
    handleIntent(intent);
    return START_STICKY;
  }

  private void handleIntent(Intent intent) {
    if (intent == null) {
      Log.d(TAG, "start alarm service for intent is null");
      stopSelf();
      return;
    }

    action = intent.getAction();
    // if action is null, the service maybe start in error method. like startService
    if (TextUtils.isEmpty(action)) {
      Log.d(TAG, "start alarm service from null action");
      stopSelf();
      return;
    }
    Log.d(TAG, "start alarm service from action " + action);

    // the service is running now.
    if (countDownTimer != null || checkThread != null) {
      Log.d(TAG, "service is running now by action from " + action);
      return;
    }

    // get all checker num.
    checkNum = checkerList.size();

    // create a thread to execute the checker, then the invoker can execute in any method, contains
    // sync or async method
    checkThread = new CheckThread();
    checkThread.start();

    countDownTimer = new CountDownTimer(DEFAULT_TIMEOUT_MILL_SECONDS, DEFAULT_CHECK_INTERVAL) {
      @Override
      public void onTick(long l) {
        // if timer hasn't finished and all scheduleCheck doesn't completed
        if (!finished && checkNum == NONE_CHECKER) {
          this.cancel();
          onFinish();
        }
      }

      @Override
      public void onFinish() {
        finished = true;
        Log.d(TAG, "scheduleCheck completed");
        stopSelf();
      }
    };
    countDownTimer.start();
  }

  /**
   * A thread use to execute all checker.
   */
  class CheckThread extends Thread {
    @Override
    public void run() {
      handleChecker();
    }
  }

  static class CheckCallback implements Callback {
    private WeakReference<AlarmService> alarmServiceWeakReference;

    public CheckCallback(AlarmService alarmService) {
      alarmServiceWeakReference = new WeakReference<AlarmService>(alarmService);
    }

    @Override
    public void onCompleted() {
      final AlarmService alarmService = alarmServiceWeakReference.get();
      if (alarmService == null) {
        Log.d(TAG, "alarmService has destroyed.");
        return;
      }

      alarmService.handler.sendEmptyMessage(CHECK_COMPLETED);
    }
  }

  private void handleChecker() {
    CheckCallback checkCallback = new CheckCallback(this);
    for (ScheduleChecker scheduleChecker : checkerList) {
      scheduleChecker.scheduleCheck(this, checkCallback);
    }
  }

  @Override
  public void onDestroy() {
    if (!hasAlarmService(this, this.getClass())) {
      scheduleNextAlarm();
    }
    if (countDownTimer != null) {
      countDownTimer = null;
    }
    if (checkThread != null && checkThread.isAlive()) {
      checkThread.interrupt();
    }
    checkThread = null;
    Log.d(TAG, "alarm service destroy");
    super.onDestroy();
  }

  private void scheduleNextAlarm() {
    startAlarmService(this, DEFAULT_DURATION, ALARM_ACTION, this.getClass());
  }

  /**
   * the method start alarm service with one minute delay.
   * if alarm service has set in alarmManager, which will return immediately.
   *
   * @param context
   * @param action
   */
  public static void scheduleAlarm(Context context, String action,
                                   Class<? extends AlarmService> claz) {

    if (context == null) {
      return;
    }
    if (hasAlarmService(context, claz)) {
      Log.d(TAG, "Alarm has been set");
      return;
    }

    startAlarmService(context, ONE_MINUTE_DELAY, action, claz);
    Log.d(TAG, "Alarm will start after one minute");
  }

  /**
   * start a alarm access to the system alarm services.
   * These allow you to schedule your application to be run at some point in the future.
   *
   * @param context
   * @param milliseconds interval in milliseconds between subsequent repeats of
   *                     the alarm or start the alarm
   * @param action       the action set for intent
   */
  private static void startAlarmService(Context context, long milliseconds, String action,
                                        Class<? extends AlarmService> claz) {
    // get AlarmManager service
    AlarmManager manager = (AlarmManager) context
        .getSystemService(Context.ALARM_SERVICE);

    Intent intent = new Intent(context, claz);
    intent.setAction(action);
    PendingIntent pendingIntent = PendingIntent.getService(context, 0,
        intent, PendingIntent.FLAG_UPDATE_CURRENT);

    manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + milliseconds,
        pendingIntent);
  }

  /**
   * return is alarm has exist.
   *
   * @param context
   * @return true alarm has exist,false not create
   */
  private static boolean hasAlarmService(Context context, Class claz) {
    Intent intent = new Intent(context, claz);
    intent.setAction(ALARM_ACTION);
    return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE) != null;
  }
}
