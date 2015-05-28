package com.wandoujia.base.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread pool which has a limited number of threads at most, where threads will be released if all
 * tasks are executed. This is more effective than fixed-size thread pool.
 * 
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class CachedThreadPoolExecutorWithCapacity implements ExecutorService {

  private final BlockingQueue<Runnable> queue;
  private final int maxThreadNum;
  private final List<RecyclableThread> runningThreads = new LinkedList<RecyclableThread>();
  private final long cacheTimeMs;
  private final AtomicInteger threadNum = new AtomicInteger(0);
  private final String threadPoolName;
  private boolean isShutdown;

  /**
   * Constructor.
   * 
   * @param maxThreadNum maximum number of threads
   */
  public CachedThreadPoolExecutorWithCapacity(int maxThreadNum) {
    this(maxThreadNum, 0, null);
  }

  /**
   * Constructor.
   * 
   * @param maxThreadNum maximum number of threads
   * @param cacheTimeMs how long a thread will live if there is no task, if it <= 0, means quit
   *          immediately
   */
  public CachedThreadPoolExecutorWithCapacity(int maxThreadNum, long cacheTimeMs) {
    this(maxThreadNum, new LinkedBlockingQueue<Runnable>(), cacheTimeMs, null);
  }

  /**
   * Constructor.
   * 
   * @param maxThreadNum maximum number of threads
   * @param cacheTimeMs how long a thread will live if there is no task, if it <= 0, means quit
   *          immediately
   * @param threadPoolName thread pool name, all thread will be named as "threadPoolName-number",
   *          can be null
   */
  public CachedThreadPoolExecutorWithCapacity(int maxThreadNum, long cacheTimeMs,
      String threadPoolName) {
    this(maxThreadNum, new LinkedBlockingQueue<Runnable>(), cacheTimeMs, threadPoolName);
  }

  /**
   * Constructor.
   * 
   * @param maxThreadNum maximum number of threads
   * @param queue queue for waiting tasks. Can pass in a customized queue, such as
   *          PriorityBlockingQueue to meet different needs
   * @param cacheTimeMs how long a thread will live if there is no task, if it <= 0, means quit
   *          immediately
   * @param threadPoolName thread pool name, all thread will be named as "threadPoolName-number",
   *          can be null
   */
  public CachedThreadPoolExecutorWithCapacity(
      int maxThreadNum, BlockingQueue<Runnable> queue, long cacheTimeMs, String threadPoolName) {
    this.maxThreadNum = maxThreadNum;
    this.queue = queue;
    this.cacheTimeMs = cacheTimeMs;
    this.threadPoolName = threadPoolName;
  }

  @Override
  public void execute(Runnable runnable) {
    if (isShutdown) {
      return;
    }
    synchronized (queue) {
      queue.add(runnable);
    }
    synchronized (runningThreads) {
      if (runningThreads.size() < maxThreadNum) {
        RecyclableThread thread;
        if (threadPoolName != null) {
          thread = new RecyclableThread(threadPoolName + "-" + threadNum.getAndIncrement());
        } else {
          thread = new RecyclableThread();
        }

        runningThreads.add(thread);
        thread.start();
      }
    }
  }

  /**
   * Cancels a task.
   * 
   * @param runnable the task to cancel
   * @param mayInterruptIfRunning true if the thread executing this task should be interrupted;
   *          otherwise, in-progress tasks are allowed to complete. Note that even if it's true, the
   *          running thread may not be interrupted. Caller should implement additional logic to
   *          stop
   *          pending operations, such as InputStream.close()
   * @return false if the task could not be cancelled, typically because it has already completed
   *         normally; true otherwise
   */
  public boolean cancel(Runnable runnable, boolean mayInterruptIfRunning) {
    if (mayInterruptIfRunning) {
      RecyclableThread thread;
      synchronized (runningThreads) {
        Iterator<RecyclableThread> iterator = runningThreads.iterator();
        while (iterator.hasNext()) {
          thread = iterator.next();
          if (thread.getRunnable() == runnable) {
            thread.interrupt();
            return true;
          }
        }
      }
    }
    synchronized (queue) {
      return queue.remove(runnable);
    }
  }

  @Override
  public void shutdown() {
    synchronized (queue) {
      queue.clear();
    }
    synchronized (runningThreads) {
      for (Thread thread : runningThreads) {
        thread.interrupt();
      }
    }
    isShutdown = true;
  }

  @Override
  public List<Runnable> shutdownNow() {
    List<Runnable> unExecutedRunnables = new LinkedList<Runnable>();
    synchronized (queue) {
      queue.drainTo(unExecutedRunnables);
      queue.clear();
    }
    synchronized (runningThreads) {
      for (Thread thread : runningThreads) {
        thread.interrupt();
      }
    }
    isShutdown = true;
    return unExecutedRunnables;
  }

  @Override
  public boolean isShutdown() {
    return isShutdown;
  }

  @Override
  public boolean isTerminated() {
    return isShutdown && runningThreads.isEmpty();
  }

  @Override
  public boolean awaitTermination(long paramLong, TimeUnit paramTimeUnit)
      throws InterruptedException {
    synchronized (runningThreads) {
      while (!runningThreads.isEmpty()) {
        long tick = System.currentTimeMillis();
        long timeout = paramTimeUnit.toMillis(paramLong);
        runningThreads.wait(timeout);
        if (System.currentTimeMillis() - tick >= timeout) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public <T> Future<T> submit(final Callable<T> callable) {
    if (isShutdown) {
      throw new RejectedExecutionException("This executive service is shut down already.");
    }
    final CustomFuture<T> future = new CustomFuture<T>();
    Runnable runnable = new Runnable() {

      @Override
      public void run() {
        try {
          T result = callable.call();
          future.setResult(result);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    future.setRunnable(runnable);
    execute(runnable);
    return future;
  }

  @Override
  public <T> Future<T> submit(final Runnable runnable, final T result) {
    if (isShutdown) {
      throw new RejectedExecutionException("This executive service is shut down already.");
    }
    final CustomFuture<T> future = new CustomFuture<T>();
    Runnable newRunnable = new Runnable() {

      @Override
      public void run() {
        try {
          runnable.run();
          future.setResult(result);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    future.setRunnable(newRunnable);
    execute(newRunnable);
    return future;
  }

  @Override
  public Future<?> submit(final Runnable runnable) {
    if (isShutdown) {
      throw new RejectedExecutionException("This executive service is shut down already.");
    }
    @SuppressWarnings("rawtypes")
    final CustomFuture<?> future = new CustomFuture();
    Runnable newRunnable = new Runnable() {

      @Override
      public void run() {
        try {
          runnable.run();
          future.setResult(null);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    future.setRunnable(newRunnable);
    execute(newRunnable);
    return future;
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection)
      throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection,
      long timeout, TimeUnit timeUnit)
      throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> paramCollection)
      throws InterruptedException, ExecutionException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> paramCollection, long paramLong,
      TimeUnit paramTimeUnit)
      throws InterruptedException, ExecutionException, TimeoutException {
    throw new UnsupportedOperationException();
  }


  private final class RecyclableThread extends Thread {
    private Runnable runnable;

    RecyclableThread() {
      super();
    }

    RecyclableThread(String name) {
      super(name);
    }

    @Override
    public void run() {
      while (!isShutdown) {
        try {
          Thread.interrupted();
          if (cacheTimeMs > 0) {
            runnable = queue.poll(cacheTimeMs, TimeUnit.MILLISECONDS);
          } else {
            runnable = queue.poll();
          }
          if (runnable != null) {
            runnable.run();
            runnable = null;
          } else {
            break;
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      synchronized (runningThreads) {
        runningThreads.remove(this);
        if (runningThreads.isEmpty()) {
          runningThreads.notifyAll();
        }
      }
    }

    private Runnable getRunnable() {
      return runnable;
    }
  }

  private final class CustomFuture<T> implements Future<T> {

    private Runnable runnable;
    private boolean isCancelled;
    private T result;
    private final CountDownLatch latch = new CountDownLatch(1);

    private void setRunnable(Runnable runnable) {
      this.runnable = runnable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      isCancelled =
          CachedThreadPoolExecutorWithCapacity.this.cancel(runnable, mayInterruptIfRunning);
      return isCancelled;
    }

    @Override
    public boolean isCancelled() {
      return isCancelled;
    }

    @Override
    public boolean isDone() {
      return latch.getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
      latch.await();
      return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException,
        ExecutionException, TimeoutException {
      latch.await(timeout, unit);
      return result;
    }

    private void setResult(T result) {
      this.result = result;
      latch.countDown();
    }
  }
}
