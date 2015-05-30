package com.zhangyu.fleamarket.model;

import com.wandoujia.mvc.BaseModel;
import com.zhangyu.fleamarket.card.CardViewModel;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public interface DemoCardModel extends BaseModel{
  CardViewModel getCardViewModel();
//  DownloadableModel getDownloadableModel();
//  VideoModel getVideoModel();
//  VideoLogModel getVideoLogModel();
}
