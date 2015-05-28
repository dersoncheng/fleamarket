package com.wandoujia.base.utils;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.os.Looper;

public class TaskFlow {

  public interface Task {
    void run(TaskCallback callback);
  }

  public interface TaskCallback {
    void reject(Exception e);
  }

  public interface ErrorHandler {
    boolean handle(Exception e);
  }

  private Exception error;
  private CountDownLatch counter;
  private boolean terminating;

  public TaskFlow() {
    counter = new CountDownLatch(0);
    terminating = false;
  }

  private void reject(Exception e) {
    this.terminating = true;
    this.error = e;
  }

  private TaskFlow(int count) {
    counter = new CountDownLatch(count);
  }

  /**
   * parallelize multiple synchronized jobs, will continue to next job when all the jobs finished.
   *
   * This method will block current thread so it has to be called in non-UI thread
   *
   * @param jobs
   *          The job list need to run in then
   * @return
   */
  public TaskFlow then(Task[] jobs) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
      throw new IllegalStateException("Cannot be called in UI thread.");
    }

    try {
      this.counter.await();
    } catch (InterruptedException e) {
      reject(e);
    }

    // skip current jobs and following jobs if interrupted or upstream is terminating,
    // return a fake object to make sure the calling chain won't break
    if (terminating) {
      TaskFlow taskFlow = new TaskFlow(0);
      taskFlow.reject(error);
      return taskFlow;
    }

    final TaskFlow taskFlow = new TaskFlow(jobs.length);

    for (final Task job : jobs) {
      ThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          try {
            if (job != null) {
              job.run(new TaskCallback() {
                @Override
                public void reject(Exception e) {
                  TaskFlow.this.reject(e);
                }
              });
            }
          } catch (Exception e) {
            reject(e);
          } finally {
            taskFlow.counter.countDown();
          }
        }
      });
    }
    return taskFlow;
  };

  public TaskFlow then(List<Task> jobs) {
    return then(jobs.toArray(new Task[0]));
  }

  public TaskFlow then(final Task job) {
    return then(new Task[] {job});
  };

  /**
   * Handle exception in upperstream
   *
   * @param handler exception handler
   * @return
   *         true: exception has been processed and task flow can continue to run
   *         false: exception has not been processed, task flow will keep in terminating mode
   */
  public TaskFlow except(ErrorHandler handler) {
    try {
      this.counter.await();
    } catch (InterruptedException e) {
      reject(e);
    }

    TaskFlow taskFlow = new TaskFlow(0);
    if (terminating && error != null) {
      if (handler.handle(error)) {
        terminating = false;
        error = null;
      }
    }

    return taskFlow;
  }

  /**
   * the final step for task flow. this will be executed no matter whatever happened before
   *
   * @param job
   */
  public void eventually(final Task job) {
    error = null;
    terminating = false;
    then(new Task[] {job});
  }

}
