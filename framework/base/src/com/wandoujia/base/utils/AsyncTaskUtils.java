package com.wandoujia.base.utils;

import android.os.AsyncTask;

/**
 * Created by xubin on 13-9-18.
 */
public class AsyncTaskUtils {
  public static boolean isRunning(AsyncTask task) {
    if (task == null) {
      return false;
    }
    if (task.getStatus().equals(AsyncTask.Status.PENDING)
        || task.getStatus().equals(AsyncTask.Status.RUNNING)) {
      return true;
    }
    return false;
  }
}
