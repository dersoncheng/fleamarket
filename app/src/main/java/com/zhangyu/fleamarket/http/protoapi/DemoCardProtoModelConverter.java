package com.zhangyu.fleamarket.http.protoapi;

import com.wandoujia.em.common.proto.Video;
import com.zhangyu.fleamarket.card.CardViewModel;
import com.zhangyu.fleamarket.model.DemoCardModel;
import com.zhangyu.fleamarket.model.DemoCardModelUtils;
import com.zhangyu.fleamarket.model.FetcherModelConverter;

/**
 * Created by niejunhong on 14-10-30.
 */
public class DemoCardProtoModelConverter implements
  FetcherModelConverter<Video, DemoCardModel> {
  @Override
  public DemoCardModel convert(Video video) {
    return DemoCardModelUtils.convertFromVideo(video, CardViewModel.ModelType.COMMON);
  }

  @Override
  public String getIdentity() {
    return this.getClass().getName();
  }
}
