package com.zhangyu.fleamarket.onlineconfig;

import com.wandoujia.rpc.http.delegate.GZipHttpDelegate;
import com.zhangyu.fleamarket.configs.FleaMarketOnlineConfigResult;
import com.zhangyu.fleamarket.http.processor.FleaMarketJsonProcessor;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketOnlineConfigDelegate
  extends GZipHttpDelegate<FleaMarketOnlineConfigRequestBuilder, FleaMarketOnlineConfigResult> {

  public FleaMarketOnlineConfigDelegate() {
    super(new FleaMarketOnlineConfigRequestBuilder(), new OnlineConfigProcessor());
  }

  private static final class OnlineConfigProcessor
    extends FleaMarketJsonProcessor<FleaMarketOnlineConfigResult> {
  }
}
