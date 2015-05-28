package com.zhangyu.fleamarket.http.provider;

import com.wandoujia.rpc.http.provider.CookieProvider;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketCookieProvider implements CookieProvider {
  private static final String FLEA_AUTH = "flea_auth=";

  @Override
  public String getDefaultCookie() {
    return FLEA_AUTH;
  }
}
