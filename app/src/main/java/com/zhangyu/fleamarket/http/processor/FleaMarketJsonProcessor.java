package com.zhangyu.fleamarket.http.processor;

import com.wandoujia.rpc.http.processor.JsonProcessor;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketJsonProcessor<T> extends JsonProcessor<T> {
  public FleaMarketJsonProcessor() {
    super(GsonFactory.getGson());
  }
}
