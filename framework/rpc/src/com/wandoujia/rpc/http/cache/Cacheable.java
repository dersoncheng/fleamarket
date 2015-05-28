package com.wandoujia.rpc.http.cache;

import com.wandoujia.gson.reflect.TypeToken;

/**
 * Interface to mark an object is cacheable.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 *
 * @param <T> cacheable class
 */
public interface Cacheable<T> {

  /**
   * Gets type token of cacheable class.
   *
   * @return type token
   */
  TypeToken<T> getTypeToken();

  /**
   * Gets cache key of request.
   *
   * @return cache key
   */
  String getCacheKey();

  /**
   * Gets timeout interval.
   *
   * @return timeout interval, in ms
   */
  long getTimeoutInterval();
}
