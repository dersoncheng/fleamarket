package com.zhangyu.fleamarket.http.request;

import com.wandoujia.rpc.http.request.AbstractHttpRequestBuilder;
import com.zhangyu.fleamarket.http.provider.FleaMarketCookieProvider;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public abstract class BaseHttpRequestBuilder extends AbstractHttpRequestBuilder {
  public BaseHttpRequestBuilder() {
    super(new FleaMarketCookieProvider());
  }
}
