package com.zhangyu.fleamarket.card;


import com.wandoujia.mvc.BaseView;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public interface BaseContentCardView extends BaseView {
  BaseCardView getCardView();

  com.zhangyu.fleamarket.view.button.BaseButton getButton();

  com.zhangyu.fleamarket.view.button.BaseImageView getImageView();

  BaseDownloadProgressView getDownloadProgress();

}
