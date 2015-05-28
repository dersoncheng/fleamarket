package com.wandoujia.rpc.http.cache;

/**
 * Data cache interface.
 * 
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public interface DataCache {

  /**
   * Gets JSON string from cache, according to key.
   * 
   * @param key key. Cannot be null
   * @return {@link CacheItemWrapper} object, can be null if there is no such item or
   *     some error happens
   */
  CacheItemWrapper get(String key);

  /**
   * Puts JSON string to cache.
   * 
   * @param key key. Cannot be null
   * @param wrapper {@link CacheItemWrapper} object
   */
  void put(String key, CacheItemWrapper wrapper);

  /**
   * Remove JSON string from cache, according to key.
   *
   * @param key key. Cannot be null
   */
  void remove(String key);

  /**
   * Checks whether cache exists.
   * 
   * @param key key. Cannot be null
   * @return exist or not
   */
  boolean exists(String key);

  /**
   * Removes all items from cache.
   */
  void clear();

  /**
   * Returns the total bytes used in cache.
   * 
   * @return size
   */
  long size();

}
