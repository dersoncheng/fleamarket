package com.zhangyu.fleamarket.http.delegate;

import com.wandoujia.em.common.proto.GetVideoTopListResultResp;
import com.zhangyu.fleamarket.http.protoapi.GZipProtoHttpDelegate;
import com.zhangyu.fleamarket.http.protoapi.ProtobufProcessor;
import com.zhangyu.fleamarket.http.request.DemoListRequestBuilder;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class DemoListDelegate
    extends GZipProtoHttpDelegate<DemoListRequestBuilder, GetVideoTopListResultResp> {

  public DemoListDelegate() {
    super(new DemoListRequestBuilder(), new DemoProcessor());
  }

  private static final class DemoProcessor
      extends ProtobufProcessor<GetVideoTopListResultResp> {

  }
}
