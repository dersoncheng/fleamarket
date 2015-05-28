package com.zhangyu.fleamarket.http;

import android.text.TextUtils;

import com.wandoujia.rpc.http.request.AbstractHttpRequestBuilder;
import com.wandoujia.rpc.http.util.PhoenixAuthorizeUtil;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.configs.Config;
import com.zhangyu.fleamarket.http.request.FleaMarketHttpRequestBuilder;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public abstract class FleaMarketAuthorizedRequestBuilder extends FleaMarketHttpRequestBuilder {
  private static final String PARAM_REGION = "region";
  @Override
  protected void setParams(AbstractHttpRequestBuilder.Params params) {
    super.setParams(params);
    // add region
    try {
      String region = Config.getTestRegion();
      if (!TextUtils.isEmpty(region)) {
        params.put(PARAM_REGION, region, false);
      }
    } catch (Exception e) {

    }
    PhoenixAuthorizeUtil.appendAuthorizeParams(params, FleaMarketApplication.getAppContext());
  }
}
