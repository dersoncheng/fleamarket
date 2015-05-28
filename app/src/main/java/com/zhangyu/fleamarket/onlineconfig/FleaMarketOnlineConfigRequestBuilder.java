package com.zhangyu.fleamarket.onlineconfig;

import com.wandoujia.rpc.http.request.AbstractHttpRequestBuilder;
import com.zhangyu.fleamarket.configs.ApiConfig;
import com.zhangyu.fleamarket.http.FleaMarketAuthorizedRequestBuilder;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketOnlineConfigRequestBuilder extends FleaMarketAuthorizedRequestBuilder {
  @Override
  protected void setParams(AbstractHttpRequestBuilder.Params params) {
    super.setParams(params);
  }

  @Override
  protected String getUrl() {
    return ApiConfig.getOnlineConfigApiUrl();
  }
}
