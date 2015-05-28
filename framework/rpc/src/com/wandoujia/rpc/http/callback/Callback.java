package com.wandoujia.rpc.http.callback;

/**
 * Callback class.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 *
 * @param <V> result type
 * @param <E> error type
 */
public interface Callback<V, E extends Exception> {
  /**
   * Gets call when result arrives.
   *
   * @param result result
   */
  void onSuccess(V result);

  /**
   * Gets called when error happens.
   *
   * @param e error
   */
  void onError(E e);
}
