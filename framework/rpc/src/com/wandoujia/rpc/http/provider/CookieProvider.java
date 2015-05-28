package com.wandoujia.rpc.http.provider;

/**
 * Interface to provide cookie.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public interface CookieProvider {
  /**
   * Gets default cookie.
   *
   * @return default cookie
   */
  String getDefaultCookie();
}
