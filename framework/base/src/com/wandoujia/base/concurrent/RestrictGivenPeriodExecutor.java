package com.wandoujia.base.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Process;
import android.util.Log;

/**
 * Restrict too many tasks to be executed at the same moment, and avoid leading to low memory.
 *
 * @author QisenTang (tangqisen@wandoujia.com)
 */
public class RestrictGivenPeriodExecutor {

  private static final String TAG = RestrictGivenPeriodExecutor.class.getSimpleName();
  private static final int THREAD_POOL_CACHE_TIME = 3000; //3s

  // executor has been asked to quit.
  private boolean mQuit = false;

  // min internal time.
  private final long givenIntervalTime;

  // the moment last task executed.
  private long lastExecuteMoment;

  private final CachedThreadPoolExecutorWithCapacity threadPool;

  private class ScheduleRunnable implements Runnable {

    private final Runnable task;

    ScheduleRunnable(Runnable runnable) {
      this.task = runnable;
    }

    @Override
    public void run() {
      android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
      try {
        // Ensure the time between two task executing is more than givenIntervalTime
        long now = System.currentTimeMillis();
        if (now - lastExecuteMoment < givenIntervalTime) {
          Thread.sleep(givenIntervalTime - (now - lastExecuteMoment));
        }
        lastExecuteMoment = now;

        // Execute task.
        task.run();
      } catch (InterruptedException e) {
        if (mQuit) {
          Log.d(TAG, "executor has been closed");
        }
      }
    }

  }

  /**
   * Constructor of RestrictGivenPeriodExecutor.
   *
   * @param time min internal time.
   */
  public RestrictGivenPeriodExecutor(long time) {
    this.givenIntervalTime = time;
    this.lastExecuteMoment = System.currentTimeMillis();
    // limit only one thread to execute.
    BlockingQueue<Runnable> waitingQueue = new LinkedBlockingQueue<Runnable>(1);
    threadPool = new CachedThreadPoolExecutorWithCapacity(
      1, waitingQueue, THREAD_POOL_CACHE_TIME, "RestrictGivenPeriodExecutor");
  }

  /**
   * submit task, and if has task waiting to execute, this command will be ignored.
   * And if scheduleRunnable has be recycled, then new a new scheduleRunnable to schedule
   * submitted task.
   */
  public void execute(Runnable runnable) {
    try {
      // as LinkedBlockingQueue using add() function may throw IllegalStateException when queue is
      // full, so here just catch exception and do nothing.ß
      threadPool.execute(new ScheduleRunnable(runnable));
    } catch (IllegalStateException e) {
      e.printStackTrace();
    }
  }

  /**
   * close executor.
   */
  public void shutdown() {
    mQuit = true;
    threadPool.shutdown();
  }

}
