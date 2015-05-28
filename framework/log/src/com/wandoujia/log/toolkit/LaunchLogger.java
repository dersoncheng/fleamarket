package com.wandoujia.log.toolkit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wandoujia.log.toolkit.model.LaunchPackage;
import com.wandoujia.logv3.model.packages.ApplicationStartEvent;
import com.wandoujia.logv3.model.packages.LaunchSourcePackage;

/**
 * Log the launch events for an application. Not thread-safe, call from main thread.
 * 
 * Created by duguguiyu on 13-11-15.
 */
public class LaunchLogger {

  /**
   * Provider for source.
   */
  public interface Executor {

    /**
     * If the activity and intent in the launch source list, return the source, else return null.
     * 
     * @param activity the launched activity
     * @param intent the intent
     * @return the LaunchSourcePackage contains the source and keyword.
     */
    com.wandoujia.log.toolkit.model.LaunchSourcePackage getLaunchSource(Activity activity,
        Intent intent);

    /**
     * Log as launch, implement in application.
     */
    void logAsLaunch(LaunchPackage launchSource);
  }

  /**
   * Provider for source.
   */
  public interface ExecutorV3 {

    /**
     * If the activity and intent in the launch source list, return the source, else return null.
     * 
     * @param activity the launched activity
     * @param intent the intent
     * @return the LaunchSourcePackage contains the source and keyword.
     */
    LaunchSourcePackage getLaunchSource(Activity activity, Intent intent);

    /**
     * Log as launch, implement in application.
     */
    void logAsLaunch(LaunchSourcePackage source, ApplicationStartEvent event);
  }

  /**
   * Launch state of foreground activity.
   */
  private enum LaunchState {
    NOT_LAUNCH,
    LAUNCHED,
    USER_WANNA_CLOSE
  }

  private static final boolean DEBUG = false;

  public static final String EXTRA_LAUNCH_FROM = "launch_from";
  public static final String EXTRA_LAUNCH_KEYWORD = "launch_keyword";

  public static final String REASON_LAUNCH_AS_NEW = "root";
  public static final String REASON_LAUNCH_AS_RELOAD = "reload";
  public static final String REASON_LAUNCH_AS_TO_FOREGROUND = "background";

  private String tag;
  private Executor executor;
  private ExecutorV3 executorV3;

  // The launch state and launch activity, always record the top activity's state. The
  private LaunchState launchState;
  private String launchActivity;
  private long taskCreatedTime;

  @Deprecated
  public LaunchLogger(String tag, Executor executor) {
    this.tag = tag;
    this.executor = executor;

    this.launchState = LaunchState.NOT_LAUNCH;
    this.launchActivity = null;
  }

  public LaunchLogger(String tag, ExecutorV3 executor) {
    this.tag = tag;
    this.executorV3 = executor;

    this.launchState = LaunchState.NOT_LAUNCH;
    this.launchActivity = null;
  }

  /**
   * Call at activity on create, if the activity is root task, log it as launch, else check white
   * list.
   */
  public void activityOnCreate(Activity activity, Intent intent, Bundle bundle) {
    // If new activity, set state as launched.
    if (DEBUG) {
      Log.d(tag, "New activity create, " + activity.getClass().getName() + ", intent is " + intent
          + ", bundle is " + bundle);
    }
    changeActivityState(activity, LaunchState.LAUNCHED);

    // If restart, do not need log as launch.
    if (bundle != null) {
      // Ignore restart.
      return;
    }

    // If root task, always log as launch.
    if (!activity.isTaskRoot()) {
      // Not root, check reload
      tryLogAsLaunch(activity, intent, ApplicationStartEvent.Reason.RELOAD);
      return;
    }
    taskCreatedTime = System.currentTimeMillis();
    logAsLaunch(activity, intent, ApplicationStartEvent.Reason.NEW);
  }

  /**
   * Call at activity on new intent, check the white list.
   */
  public void activityOnNewIntent(Activity activity, Intent intent) {
    if (DEBUG) {
      Log.d(tag, "Activity new intent, " + activity.getClass().getName() + ", intent is " + intent);
    }
    changeActivityState(activity, LaunchState.LAUNCHED);

    tryLogAsLaunch(activity, intent, ApplicationStartEvent.Reason.RELOAD);
  }

  /**
   * Call at activity on start, check whether switch to foreground.
   */
  public void activityOnRestart(Activity activity, Intent intent) {
    if (DEBUG) {
      Log.d(tag, "Activity restart, " + activity.getClass().getName() + ", intent is " + intent);
    }

    // If the activity latest state is USER_WANNA_CLOSE, means the activity is switch from
    // background to foreground, mark as launch.
    boolean backToForeground = isSameActivityState(activity, LaunchState.USER_WANNA_CLOSE);
    changeActivityState(activity, LaunchState.LAUNCHED);
    if (!backToForeground) {
      return;
    }

    logAsLaunch(activity, intent, ApplicationStartEvent.Reason.RESTART);
  }

  /**
   * Call at activity on destroy, to record state.
   */
  public void activityOnDestroy(Activity activity) {
    if (DEBUG) {
      Log.d(tag, "Activity destroy, " + activity.getClass().getName());
    }

    // Dump the task running time, for debug.
    if (activity.isTaskRoot() && taskCreatedTime != 0L) {
      Log.d(tag, "Task is closed, duration is " + (System.currentTimeMillis() - taskCreatedTime));
      taskCreatedTime = 0L;
    }

    // Change state.
    // NOTE: If back to last activity, the last activity's restart would be callback first, then
    // current activity's on destroy would be callback. So MUST check the activity name first.
    if (activity.getClass().getName().equals(launchActivity)) {
      changeActivityState(activity, LaunchState.NOT_LAUNCH);
    }
  }

  /**
   * Call at activity on user leave, to record state.
   */
  public void activityOnUserLeave(Activity activity) {
    if (DEBUG) {
      Log.d(tag, "Activity user leave, " + activity.getClass().getName());
    }
    changeActivityState(activity, LaunchState.USER_WANNA_CLOSE);
  }

  private void changeActivityState(Activity activity, LaunchState state) {
    if (DEBUG) {
      Log.d(tag, "Launch state was changed, last activity is " + launchActivity
          + ", last state is " + launchState + "; new activity is " + activity.getClass().getName()
          + ", new state is " + state);
    }
    launchActivity = activity.getClass().getName();
    launchState = state;
  }

  private boolean tryLogAsLaunch(Activity activity, Intent intent,
      ApplicationStartEvent.Reason reason) {
    if (executor != null) {
      LaunchPackage launchPackage = getLaunchPackage(activity, intent, reason);
      if (launchPackage.sourcePackage == null ||
          launchPackage.sourcePackage.launch_source == null ||
          launchPackage.sourcePackage.launch_source.equals(
              com.wandoujia.log.toolkit.model.LaunchSourcePackage.DEFAULT_LAUNCH_SOURCE)) {
        // No source extra in intent, or not in white list, not log.
        return false;
      }
      executor.logAsLaunch(launchPackage);
    }
    if (executorV3 != null) {
      LaunchSourcePackage launchSourcePackage = executorV3.getLaunchSource(activity, intent);
      if (launchSourcePackage == null ||
          launchSourcePackage.source == null ||
          launchSourcePackage.source == LaunchSourcePackage.DEFAULT_SOURCE) {
        // No source extra in intent, or not in white list, not log.
        return false;
      }
      executorV3.logAsLaunch(executorV3.getLaunchSource(activity, intent),
          getApplicationStartEvent(activity, intent, reason));
    }
    return true;
  }

  private void logAsLaunch(Activity activity, Intent intent, ApplicationStartEvent.Reason reason) {
    if (executor != null) {
      executor.logAsLaunch(getLaunchPackage(activity, intent, reason));
    }
    if (executorV3 != null) {
      executorV3.logAsLaunch(executorV3.getLaunchSource(activity, intent),
          getApplicationStartEvent(activity, intent, reason));
    }
  }

  /**
   * If latest activity state is same as given state, return true, else return false.
   * 
   * @param activity Current activity, if not same activity with latest one, return false.
   * @param state Wanna state, if not same, return false.
   * @return true if the given state is same as the latest activity state, false otherwise.
   */
  private boolean isSameActivityState(Activity activity, LaunchState state) {
    return activity.getClass().getName().equals(launchActivity) && state.equals(launchState);
  }

  /**
   * Try to get source from intent and activity, if failed, return null (means unknown source).
   * 
   * @param activity the launched activity
   * @param intent the intent
   * @return a LaunchSourcePackage contains the source and the keyword, would not be null
   */
  private LaunchPackage getLaunchPackage(Activity activity, Intent intent,
      ApplicationStartEvent.Reason reason) {
    LaunchPackage.Builder builder = new LaunchPackage.Builder();
    builder.activity(activity.getClass().getSimpleName())
        .action(intent.getAction())
        .data(intent.getDataString());
    switch (reason) {
      case NEW:
        builder.reason(REASON_LAUNCH_AS_NEW);
        break;
      case RELOAD:
        builder.reason(REASON_LAUNCH_AS_RELOAD);
        break;
      case RESTART:
        builder.reason(REASON_LAUNCH_AS_TO_FOREGROUND);
        break;
      default:
        break;
    }
    // Try to get launch extra.
    String source = intent.getStringExtra(EXTRA_LAUNCH_FROM);
    if (source != null) {
      String keyword = intent.getStringExtra(EXTRA_LAUNCH_KEYWORD);
      builder.sourcePackage(
          new com.wandoujia.log.toolkit.model.LaunchSourcePackage.Builder()
              .launch_source(source).launch_keyword(keyword)
              .build());
      return builder.build();
    }

    // Else, check source provider
    com.wandoujia.log.toolkit.model.LaunchSourcePackage sourcePackage =
        executor.getLaunchSource(activity, intent);
    if (sourcePackage != null) {
      builder.sourcePackage(sourcePackage);
    } else {
      builder.sourcePackage(
          new com.wandoujia.log.toolkit.model.LaunchSourcePackage.Builder()
              .launch_source(
                  com.wandoujia.log.toolkit.model.LaunchSourcePackage.DEFAULT_LAUNCH_SOURCE)
              .build());
    }
    return builder.build();
  }

  private ApplicationStartEvent getApplicationStartEvent(Activity activity, Intent intent,
      ApplicationStartEvent.Reason reason) {
    ApplicationStartEvent.Builder builder = new ApplicationStartEvent.Builder();
    builder.activity(activity.getClass().getSimpleName())
        .action(intent.getAction())
        .data(intent.getDataString())
        .reason(reason);
    return builder.build();
  }
}
