package com.zhangyu.fleamarket.http.client;

import android.os.Handler;

import com.wandoujia.rpc.http.cache.Cacheable;
import com.wandoujia.rpc.http.callback.Callback;
import com.wandoujia.rpc.http.client.DataApi;
import com.wandoujia.rpc.http.delegate.ApiDelegate;
import com.wandoujia.rpc.http.delegate.GZipHttpDelegate;

import org.apache.http.HttpHost;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketDataClient implements DataApi {
  private ProtoDataClient stub;
  private boolean isStubOn = true;
  private String cacheDir;
  private HttpHost proxyHost;

  public FleaMarketDataClient(String cacheDir) {
    this.cacheDir = cacheDir;
  }

  @Override
  public <T, E extends Exception> T execute(ApiDelegate<T, E> delegate) throws ExecutionException {
    return getDataApi().execute(delegate);
  }

  @Override
  public <T, E extends Exception> Future<T> executeAsync(
    ApiDelegate<T, E> delegate, Callback<T, ExecutionException> callback) {
    return getDataApi().executeAsync(delegate, callback);
  }

  @Override
  public <T, E extends Exception> Future<T> executeAsync(
    ApiDelegate<T, E> delegate, Callback<T, ExecutionException> callback, Handler handler) {
    return getDataApi().executeAsync(delegate, callback, handler);
  }

  public <T, M extends GZipHttpDelegate<?, T> & Cacheable<T>> ProtoDataClient.CacheResult<T>
  executeByCache(final M delegate) {
    return getDataApi().executeByCache(delegate);
  }

  public <T, M extends GZipHttpDelegate<?, T> & Cacheable<T>> T
  executeByNetwork(M delegate) throws ExecutionException {
    return getDataApi().executeByNetwork(delegate);
  }

  public synchronized void start() {
    if (stub == null) {
      isStubOn = true;
    } else {
      stub.start();
    }
  }

  @Override
  public synchronized void shutdown() {
    if (stub == null) {
      isStubOn = false;
    } else {
      stub.shutdown();
    }
  }

  @Override
  public void setProxyHttpHost(HttpHost host) {
    if (stub == null) {
      proxyHost = host;
    } else {
      stub.setProxyHttpHost(host);
    }
  }

  // For lazy initialization purpose
  private synchronized ProtoDataClient getDataApi() {
    if (stub == null) {
      stub = new ProtoDataClient(cacheDir);
      if (proxyHost != null){
        stub.setProxyHttpHost(proxyHost);
      }
      if (isStubOn) {
        stub.start();
      } else {
        stub.shutdown();
      }
    }
    return stub;
  }
}
