package com.wandoujia.rpc.http.client;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRouteParams;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.wandoujia.base.config.GlobalConfig;
import com.wandoujia.rpc.http.callback.Callback;
import com.wandoujia.rpc.http.delegate.ApiDelegate;

/**
 * Data client implementation.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class DataClient implements DataApi {
  private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
  private HttpClient httpClient;
  private final ExecutorService threadPool;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  protected boolean isWorking = true;
  private HttpHost proxyHost = null;

  public DataClient() {
    this(Executors.newCachedThreadPool());
  }

  public DataClient(ExecutorService executor) {
    threadPool = executor;
    lock.writeLock().lock();
    httpClient = new PhoenixHttpClient();
    ConnRouteParams.setDefaultProxy(httpClient.getParams(), proxyHost);
    lock.writeLock().unlock();
  }

  @Override
  public <T, E extends Exception> T execute(ApiDelegate<T, E> delegate) throws ExecutionException {
    if (!isWorking) {
      throw new ExecutionException(
          new IllegalStateException("The client has been shut down."));
    }
    HttpUriRequest request = delegate.getHttpRequest();
    if (request == null) {
      throw new ExecutionException(
          new IllegalStateException("Request cannot be null, some error happens."));
    }
    HttpResponse response = executeHttpRequest(request);
    try {
      return delegate.processResponse(response);
    } catch (Exception e) {
      // We can only catch Exception type here, because we cannot catch generic type E.
      throw new ExecutionException(e);
    }
  }

  @Override
  public <T, E extends Exception> Future<T> executeAsync(ApiDelegate<T, E> delegate,
      Callback<T, ExecutionException> callback) {
    return executeAsync(delegate, callback, null);
  }

  @Override
  public <T, E extends Exception> Future<T> executeAsync(final ApiDelegate<T, E> delegate,
      final Callback<T, ExecutionException> callback, final Handler handler) {
    return threadPool.submit(new Callable<T>() {

      @Override
      public T call() {
        T result = null;
        try {
          result = execute(delegate);
          onSuccess(callback, result, handler);
        } catch (ExecutionException e) {
          onError(callback, e, handler);
        }
        return result;
      }

    });
  }

  @Override
  public void start() {
    isWorking = true;
  }

  @Override
  public void shutdown() {
    isWorking = false;
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        lock.readLock().lock();
        if (httpClient != null) {
          try { // This try-catch is to fix a crash on some roms.
            httpClient.getConnectionManager().shutdown();
          } catch (Exception e) {}
          lock.readLock().unlock();
          lock.writeLock().lock();
          httpClient = null;
          lock.writeLock().unlock();
        } else {
          lock.readLock().unlock();
        }
      }
    });
  }

  protected HttpResponse executeHttpRequest(HttpUriRequest request) throws ExecutionException {
    lock.readLock().lock();
    if (httpClient == null) {
      lock.readLock().unlock();
      lock.writeLock().lock();
      if (httpClient == null) {
        httpClient = new PhoenixHttpClient();
        ConnRouteParams.setDefaultProxy(httpClient.getParams(), proxyHost);
      }
      lock.readLock().lock();
      lock.writeLock().unlock();
    }
    try {
      if (GlobalConfig.isDebug()) {
        Log.d("HTTP", request.getURI().toString());
      }
      return httpClient.execute(request);
    } catch (IOException e) {
      try {
        // java.lang.AssertionError: libcore.io.ErrnoException: getsockname failed:
        // EBADF (Bad file number) in abort
        request.abort();
        throw new ExecutionException(e);
      } catch (Throwable e2) {
        throw new ExecutionException(e2);
      }
    } catch (Throwable e) {
      // Maybe catch "llegalStateException: Connection pool shut down" or
      // "IllegalStateException: Connection is not open",
      // or "java.lang.AssertionError: libcore.io.ErrnoException: getsockname failed:
      // EBADF (Bad file number)", when HttpClient is shut down
      try {
        // java.lang.AssertionError: libcore.io.ErrnoException:
        // getsockname failed: EBADF (Bad file number) in abort
        request.abort();
        throw new ExecutionException(e);
      } catch (Throwable e2) {
        throw new ExecutionException(e2);
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  private static <T> void onSuccess(final Callback<T, ExecutionException> callback,
      final T result, Handler handler) {
    if (callback != null) {
      Handler callbackHandler = handler == null ? UI_HANDLER : handler;
      callbackHandler.post(new Runnable() {
        @Override
        public void run() {
          callback.onSuccess(result);
        }
      });
    }
  }

  private static <T> void onError(final Callback<T, ExecutionException> callback,
      final ExecutionException e, Handler handler) {
    if (callback != null) {
      Handler callbackHandler = handler == null ? UI_HANDLER : handler;
      callbackHandler.post(new Runnable() {
        @Override
        public void run() {
          callback.onError(e);
        }
      });
    }
  }

  @Override
  public void setProxyHttpHost(HttpHost host) {
    this.proxyHost = host;
    lock.readLock().lock();
    if (httpClient != null) {
      ConnRouteParams.setDefaultProxy(httpClient.getParams(), proxyHost);
    }
    lock.readLock().unlock();
  }
}
