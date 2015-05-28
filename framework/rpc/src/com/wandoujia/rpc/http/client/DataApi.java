package com.wandoujia.rpc.http.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpHost;

import android.os.Handler;

import com.wandoujia.rpc.http.callback.Callback;
import com.wandoujia.rpc.http.delegate.ApiDelegate;

/**
 * Data interface to access network.
 * 
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public interface DataApi {
  /**
   * Executes request.
   * 
   * @param delegate delegate
   * @return result
   * @throws java.util.concurrent.ExecutionException when error happens
   */
  <T, E extends Exception> T execute(ApiDelegate<T, E> delegate) throws ExecutionException;

  /**
   * Executes request asynchronously.
   * 
   * @param delegate delegate
   * @param callback callback being called asynchronously on UI thread, can be null
   * @return future
   */
  <T, E extends Exception> Future<T> executeAsync(ApiDelegate<T, E> delegate,
      Callback<T, ExecutionException> callback);

  /**
   * Executes request asynchronously.
   * 
   * @param delegate delegate
   * @param callback callback being called asynchronously, can be null
   * @param handler on which thread callback is called. If null, call back on UI thread
   * @return future
   */
  <T, E extends Exception> Future<T> executeAsync(ApiDelegate<T, E> delegate,
      Callback<T, ExecutionException> callback,
      Handler handler);

  /**
   * Makes data api be able to work. Calling it for more than once take no further effect. If not
   * start, any request will cause an {@link ExecutionException}.
   */
  void start();

  /**
   * Shuts down to make all running and further requests stop.
   */
  void shutdown();

  /**
   * Set http proxy host.
   * 
   * @param host
   */
  void setProxyHttpHost(HttpHost host);
}
