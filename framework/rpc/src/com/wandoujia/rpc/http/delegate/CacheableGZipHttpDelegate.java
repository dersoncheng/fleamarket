package com.wandoujia.rpc.http.delegate;

import com.wandoujia.rpc.http.cache.Cacheable;
import com.wandoujia.rpc.http.processor.Processor;
import com.wandoujia.rpc.http.request.HttpRequestBuilder;

/**
 * Delegate which can process GZip compressed http response, and the result is cacheable.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 *
 * @param <U> HttpRequestBuilder type
 * @param <T> output type
 */
public abstract class CacheableGZipHttpDelegate<U extends HttpRequestBuilder, T>
    extends GZipHttpDelegate<U, T> implements Cacheable<T> {
  private static final long TIMEOUT_INTERVAL_MS = 5 * 60 * 1000L;

  public CacheableGZipHttpDelegate(U requestBuilder,
      Processor<String, T, ? extends Exception> processor) {
    super(requestBuilder, processor);
  }

  @Override
  public long getTimeoutInterval() {
    return TIMEOUT_INTERVAL_MS;
  }

  @Override
  public String getCacheKey() {
    return getRequestBuilder().getCacheKey();
  }
}
