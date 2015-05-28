package com.wandoujia.rpc.http.client;

import com.google.gson.JsonParseException;
import com.wandoujia.rpc.http.cache.CacheItemWrapper;
import com.wandoujia.rpc.http.cache.Cacheable;
import com.wandoujia.rpc.http.cache.DataCache;
import com.wandoujia.rpc.http.cache.FileCache;
import com.wandoujia.rpc.http.delegate.ApiDelegate;
import com.wandoujia.rpc.http.delegate.CacheableGZipHttpDelegate;
import com.wandoujia.rpc.http.delegate.GZipHttpDelegate;
import com.wandoujia.rpc.http.exception.HttpException;
import com.wandoujia.rpc.http.processor.GZipHttpResponseProcessor;
import com.wandoujia.rpc.http.processor.Processor;

import org.apache.http.HttpResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Data client with cache.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class DataClientCache extends DataClient {
  private final DataCache cache;
  private final ExecutorService threadPool;

  public DataClientCache(String cacheDir) {
    this(cacheDir, Executors.newCachedThreadPool());
  }

  public DataClientCache(String cacheDir, ExecutorService executor) {
    cache = new FileCache(cacheDir);
    threadPool = executor;
  }

  /**
   * Executes request, and return cache if found.
   *
   * @param delegate delegate. If want to make result the {@code delegate} returns can be cached,
   *          {@code delegate} needs to implement {@link Cacheable}
   * @return result
   * @throws java.util.concurrent.ExecutionException when error happens
   */
  @Override
  public <T, E extends Exception> T execute(final ApiDelegate<T, E> delegate)
      throws ExecutionException {
    if (!isWorking) {
      throw new ExecutionException(
          new IllegalStateException("The client has been shut down."));
    }
    String key = null;
    if (delegate instanceof Cacheable && delegate instanceof GZipHttpDelegate) {
      key = ((Cacheable<T>) delegate).getCacheKey();
      CacheItemWrapper item = cache.get(key);
      if (item != null) {
        if (System.currentTimeMillis() - item.getLastModificationTime() <= item.getTimeout()) {
          try {
            T result = ((Processor<String, T, E>) ((GZipHttpDelegate) delegate)
                .getContentProcessor()).process(item.getContent());
            if (result != null) {
              return result;
            }
          } catch (JsonParseException e) {
            e.printStackTrace();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }

    // Miss cache
    if (delegate instanceof Cacheable && delegate instanceof GZipHttpDelegate) {
      // For cacheable case, we need to cache content string from http response
      HttpResponse response = super.executeHttpRequest(delegate.getHttpRequest());
      final String content;
      try {
        content = new GZipHttpResponseProcessor().process(response);
      } catch (HttpException e) {
        e.printStackTrace();
        throw new ExecutionException(e);
      }
      try {
        final T result = ((Processor<String, T, E>) ((GZipHttpDelegate) delegate)
            .getContentProcessor()).process(content);
        final String finalKey = key;
        threadPool.execute(new Runnable() {
          @Override
          public void run() {
            putItemToCache(finalKey, (Cacheable<T>) delegate, content);
          }
        });
        return result;
      } catch (JsonParseException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    } else {
      // Non-cacheable
      return super.execute(delegate);
    }
  }

  /**
   * Executes request from cache. If miss, return null.
   *
   * @param delegate normally, it's {@link CacheableGZipHttpDelegate}
   * @param <T> result type
   * @param <M> delegate type
   * @return {@link CacheResult} which contains cache data and info
   */
  public <T, M extends GZipHttpDelegate<?, T> & Cacheable<T>> CacheResult<T>
      executeByCache(final M delegate) {
    String key = delegate.getCacheKey();
    CacheItemWrapper item = cache.get(key);
    if (item != null) {
      try {
        T result = delegate.getContentProcessor().process(item.getContent());
        if (result != null) {
          boolean isTimeout =
              System.currentTimeMillis() - item.getLastModificationTime() > item.getTimeout();
          return new CacheResult<T>(result, item.getLastModificationTime(), isTimeout);
        }
      } catch (JsonParseException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  /**
   * Executes request from network, and save response as cache.
   *
   * @param delegate normally, it's {@link CacheableGZipHttpDelegate}
   * @param <T> result type
   * @param <M> delegate type
   * @return {@link CacheResult} which contains cache data and info
   */
  public <T, M extends GZipHttpDelegate<?, T> & Cacheable<T>> T
      executeByNetwork(final M delegate) throws ExecutionException {
    if (!isWorking) {
      throw new ExecutionException(
          new IllegalStateException("The client has been shut down."));
    }
    String key = delegate.getCacheKey();
    HttpResponse response = super.executeHttpRequest(delegate.getHttpRequest());
    // Miss cache
    final String content;
    try {
      content = new GZipHttpResponseProcessor().process(response);
    } catch (HttpException e) {
      e.printStackTrace();
      throw new ExecutionException(e);
    }
    try {
      final T result = delegate.getContentProcessor().process(content);
      final String finalKey = key;
      threadPool.execute(new Runnable() {
        @Override
        public void run() {
          putItemToCache(finalKey, delegate, content);
        }
      });
      return result;
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private <T> void putItemToCache(String uri, Cacheable<T> cacheable, String content) {
    CacheItemWrapper item = CacheItemWrapper.from(content, cacheable.getTimeoutInterval(),
        System.currentTimeMillis());
    cache.put(uri, item);
  }

  /**
   * Cache result.
   *
   * @param <T> reslt type
   */
  public static class CacheResult<T> {
    public final T data;
    public final long timestamp;
    public final boolean isTimeout;

    public CacheResult(T data, long timestamp, boolean isTimeout) {
      this.data = data;
      this.timestamp = timestamp;
      this.isTimeout = isTimeout;
    }
  }
}
