package com.zhangyu.fleamarket.http.request;

import com.wandoujia.base.utils.FreeHttpUtils;
import com.wandoujia.base.utils.NetworkUtil;
import com.wandoujia.base.utils.SystemUtil;
import com.wandoujia.rpc.http.request.AbstractHttpRequestBuilder;
import com.wandoujia.udid.UDIDUtil;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.configs.Config;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public abstract class FleaMarketHttpRequestBuilder extends BaseHttpRequestBuilder {
  private static final String FROM = "f";
  private static final String VERSION = "v";
  private static final String VERSION_CODE = "vc";
  private static final String USER = "u";
  private static final String CHANNEL = "ch";
  private static final String NET = "net";
  private static final String FROM_SOURCE = "flea";
  private static final String NET_FREE = "free";

  @Override
  protected void setParams(AbstractHttpRequestBuilder.Params params) {
    super.setParams(params);
    AbstractHttpRequestBuilder.Params newParams = new AbstractHttpRequestBuilder.Params();
    newParams.putAll(params);
    params.clear();
    for (Map.Entry<String, AbstractHttpRequestBuilder.Value> entry : newParams.entrySet()) {
      try {
        String key = URLEncoder.encode(entry.getKey(), HTTP.UTF_8);
        String value = URLEncoder.encode(entry.getValue().value, HTTP.UTF_8);
        params.put(key, value, entry.getValue().isCacheableParam);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    params.put(FROM, FROM_SOURCE, false);
    params.put(VERSION, SystemUtil.getVersionName(FleaMarketApplication.getAppContext()), false);
    params.put(VERSION_CODE,
      String.valueOf(SystemUtil.getVersionCode(FleaMarketApplication.getAppContext())), false);
    params.put(USER, UDIDUtil.getUDID(FleaMarketApplication.getAppContext()), false);
    params.put(CHANNEL, Config.getFirstChannel(), false);
    if (FreeHttpUtils.isInFreeMode()) {
      params.put(NET, NET_FREE, true);
    } else {
      params.put(NET, NetworkUtil.getNetworkTypeName(FleaMarketApplication.getAppContext()), false);
    }
  }
}
