package com.zhangyu.fleamarket.http.fetcher;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import com.wandoujia.rpc.http.exception.HttpExceptionUtils;
import com.zhangyu.fleamarket.utils.ThreadPool;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A helper class to make getting content from network more convenient.
 * <p/>
 * <p>
 * BaseFetcher is designed with a simple cache manager, if you don't want to use cache, provide
 * 'null' as cache key
 * </p>
 *
 * @author zhulantian@wandoujia.com.
 */
public abstract class BaseFetcher<T> {

  private static final int DEFAULT_RETRY = 3;

  private Handler handler;
  private final Set<String> runningSet;
  private final Set<Runnable> runnables;

  protected BaseFetcher() {
    runningSet = new HashSet<String>();
    runnables = new HashSet<Runnable>();
    handler = new Handler(Looper.getMainLooper());
  }

  public synchronized void fetch(final int start, final int size, final Callback<T> callback) {
    fetch(start, size, callback, false);
  }

  /**
   * Fetches data. If {@code twoPhraseLoading == true}, it will fetch from cache first, and then
   * network.
   */
  public synchronized void fetch(final int start, final int size, final Callback<T> callback,
                                 final boolean twoPhraseLoading) {
    // avoid repeat call fetch
    // TODO(zhulantian): It's a temporary implementation to avoid duplicated invoke. We should
    // prevent upper layer from calling this function beyond need.
    final String requestId = genRequestId(start, size, callback);
    if (runningSet.contains(requestId)) {
      return;
    }
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          if (start == 0 && twoPhraseLoading) {
            final List<T> cacheResult = fetchByMemoryCache(start, size);
            ResultList cachedData = null;
            if (cacheResult.size() < size) {
              int newStart = start + cacheResult.size();
              int newSize = size - cacheResult.size();
              cachedData = fetchCacheData(newStart, newSize);
              if (cachedData != null && cachedData.data != null) {
                cacheResult.addAll(cachedData.data);
              }
            }
            boolean isTimeout = true;
            final ResultList resultList;
            if (!cacheResult.isEmpty()) {
              if (cachedData == null) {
                resultList = new ResultList(cacheResult, null);
                isTimeout = false;
              } else {
                resultList =
                    new ResultList(cacheResult, cachedData.isTimeout);
                isTimeout = cachedData.isTimeout;
              }
            } else {
              resultList = new ResultList(null, true);
            }
            if (callback != null) {
              handler.post(new Runnable() {
                @Override
                public void run() {
                  callback.onFetched(start, size, resultList);
                }
              });
            }
            if (isTimeout) {
              try {
                final List<T> httpResult = fetchHttpData(start, size);
                if (callback != null) {
                  handler.post(new Runnable() {
                    @Override
                    public void run() {
                      callback.onFetched(start, size, new ResultList<T>(httpResult));
                    }
                  });
                }
              } catch (final ExecutionException e) {
                if (callback != null) {
                  handler.post(new Runnable() {
                    @Override
                    public void run() {
                      callback.onFailed(start, e);
                    }
                  });
                }
              }
            }
          } else {
            try {
              final List<T> result = doFetch(start, size);
              if (callback != null) {
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    callback.onFetched(start, size, new ResultList<T>(result));
                  }
                });
              }
            } catch (final ExecutionException e) {
              if (callback != null) {
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    callback.onFailed(start, e);
                  }
                });
              }
            }
          }
        } finally {
          synchronized (BaseFetcher.this) {
            runningSet.remove(requestId);
            runnables.remove(this);
          }
        }
      }
    };
    runningSet.add(requestId);
    runnables.add(runnable);
    ThreadPool.execute(runnable);
  }

  public synchronized void close() {
    for (Runnable runnable : runnables) {
      ThreadPool.cancel(runnable);
    }
  }

  // generate an id which can represent the request
  private static String genRequestId(int start, int size, Object obj) {
    return start + "." + size + "." + String.valueOf(obj);
  }

  /**
   * Synchronize to keep "first in, first out".
   */
  protected List<T> doFetch(int start, int size) throws ExecutionException {
    List<T> result = fetchByMemoryCache(start, size);
    if (result.size() < size) {
      start += result.size();
      size -= result.size();
      List<T> list = loadAndUpdate(start, size);
      if (list != null) {
        result.addAll(list);
      }
    }
    return result;
  }

  /**
   * Fetches data from network.
   *
   * @param start the request start index
   * @param size  if size < 0 means fetch all
   * @return return result list, can be null
   */
  protected abstract List<T> fetchHttpData(int start, int size) throws ExecutionException;

  /**
   * Fetches data from cache.
   * <p/>
   * <p>Sub-class can override it to implement its own logic</p>
   *
   * @param start the request start index
   * @param size  if size < 0 means fetch all
   * @return return result list, can be null
   */
  protected ResultList<T> fetchCacheData(int start, int size) {
    return null;
  }

  /**
   * @return result maybe null
   * @throws java.util.concurrent.ExecutionException when meet exception
   */
  public List<T> loadAndUpdate(int start, int size) throws ExecutionException {
    List<T> result = null;
    int retryCount = DEFAULT_RETRY;
    while (retryCount-- > 0) {
      try {
        result = fetchHttpData(start, size);
        break;
      } catch (ExecutionException e) {
        if (HttpExceptionUtils.canRetry(e) && retryCount > 0) {
          int retriedCount = DEFAULT_RETRY - retryCount;
          long delay = (1 << (retriedCount - 1)) * 1000L; // 1, 2, 4
          try {
            Thread.sleep(delay);
          } catch (InterruptedException ie) {
            // ignore
          }
        } else if (!HttpExceptionUtils.is404NotFound(e)) { // 404 means no more result
          throw e;
        }
      }
    }
    updateCache(start, size, result);
    return result;
  }

  private Cache<T> getCache() {
    String cacheId = getCacheId(getCacheKey(), getClass());
    if (cacheId != null) {
      return CacheManager.getCache(cacheId);
    }
    return null;
  }

  public void clearCache() {
    clearCache(getCacheKey(), getClass());
  }

  private static void clearCache(String cacheKey, Class<? extends BaseFetcher> fetchClass) {
    String cacheId = getCacheId(cacheKey, fetchClass);
    if (cacheId != null) {
      Cache<?> cache = CacheManager.getCache(cacheId);
      if (cache != null) {
        cache.clear();
      }
    }
  }

  private static String getCacheId(String cacheKey, Class<? extends BaseFetcher> clazz) {
    return cacheKey == null ? null : clazz.getName() + '*' + cacheKey;
  }

  /**
   * <p>
   * if you don't want to share one cache between difference instances, you can override this method
   * to create difference cache. the same key wills shares same cache.
   * </p>
   * <p/>
   * <p>
   * examples:
   * </p>
   * <p>
   * return String.valueOf(this.hasCode()) means every instance has it's own cache;</>
   * <p>
   * return category.name() means the same category shares one cache;
   * </p>
   * <p>
   * return null if you don't want to use cache;
   * </p>
   */
  // (change in 20140313:) if give a default value like a empty string, sub class maybe not
  // override this method and get mistake. and most case of our project need to override
  // this method, so change this method abstract.
  protected abstract String getCacheKey();

  protected Reference<T> wrapData(T data) {
    return new WeakReference<T>(data);
  }

  private void updateCache(int start, int size, List<T> list) {
    Cache<T> cache = getCache();
    if (cache == null) {
      return;
    }
    for (int i = 0; i < size; ++i) {
      T data = (list != null && i < list.size()) ? list.get(i) : null;
      if (data != null) {
        cache.put(start + i, wrapData(data));
      } else if (i < cache.size()) {
        cache.remove(start + i);
      } else {
        break;
      }
    }
  }

  private List<T> fetchByMemoryCache(int start, int size) {
    Cache<T> cache = getCache();
    List<T> result = new ArrayList<T>();
    if (cache == null) {
      return result;
    }
    int end = start + size;
    if (end < 0) { // overflow of int
      end = cache.size();
    }
    for (int i = start; i < end; ++i) {
      T data = cache.get(i);
      if (data == null) {
        break;
      }
      result.add(data);
    }
    return result;
  }

  /**
   * Callback class to be invoked on result fetched.
   *
   * @param <T> type of result list element
   */
  public interface Callback<T> {

    /**
     * Gets called when result is fetched.
     *
     * @param start  start
     * @param size   request size
     * @param result result list, at lease an empty list, never null
     */
    void onFetched(int start, int size, ResultList<T> result);

    void onFailed(int start, ExecutionException e);
  }

  private static class CacheManager {

    private static Map<String, Cache> caches = new HashMap<String, Cache>();

    private CacheManager() {
    }

    private static synchronized <T> Cache<T> getCache(String cacheId) {
      Cache cache = caches.get(cacheId);
      if (cache == null) {
        cache = new Cache();
      }
      caches.put(cacheId, cache);
      return cache;
    }

    public static synchronized void clear() {
      caches.clear();
    }
  }

  private static class Cache<T> {

    private SparseArray<Reference<T>> cache;

    public Cache() {
      cache = new SparseArray<Reference<T>>();
    }

    public synchronized void put(int pos, Reference<T> ref) {
      if (ref != null) {
        cache.put(pos, ref);
      }
    }

    public synchronized void remove(int pos) {
      cache.remove(pos);
    }

    public synchronized T get(int pos) {
      Reference<T> ref = cache.get(pos);
      return ref == null ? null : ref.get();
    }

    public synchronized int size() {
      return cache.size();
    }

    public synchronized void clear() {
      cache.clear();
    }
  }

  /**
   * Result list.
   *
   * @param <T> element type
   */
  public static class ResultList<T> implements Cloneable {
    public final List<T> data;
    public final Boolean isTimeout;

    public ResultList(List<T> data, Boolean isTimeout) {
      if (data == null) {
        data = new LinkedList<T>();
      }
      this.data = data;
      this.isTimeout = isTimeout;
    }

    public ResultList(List<T> data) {
      if (data == null) {
        data = new LinkedList<T>();
      }
      this.data = data;
      this.isTimeout = null;
    }

    /**
     * Checks whether this is a cache result.
     *
     * @return true for cache result
     */
    public boolean isCache() {
      return isTimeout != null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }
  }
}
