package com.wandoujia.image;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import android.graphics.Bitmap;

import com.wandoujia.base.concurrent.CachedThreadPoolExecutorWithCapacity;

class ImageThreadPool {

  private final CachedThreadPoolExecutorWithCapacity executor;
  private static final long CACHE_TIME_MS = 60 * 1000L;

  public ImageThreadPool(int maxThreadNum) {
    executor = new CachedThreadPoolExecutorWithCapacity(maxThreadNum, CACHE_TIME_MS);
  }

  public Future<Bitmap> execute(Callable<Bitmap> callable) {
    return executor.submit(callable);
  }

  public void execute(Runnable task) {
    executor.execute(task);
  }

  public boolean cancel(Runnable runnable, boolean mayInterruptIfRunning) {
    return executor.cancel(runnable, mayInterruptIfRunning);
  }
}
