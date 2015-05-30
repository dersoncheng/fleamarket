package com.zhangyu.fleamarket.card.views;

import android.widget.ImageView;
import android.widget.TextView;

import com.wandoujia.image.view.AsyncImageView;
import com.wandoujia.mvc.BaseView;
import com.zhangyu.fleamarket.view.button.SubActionButton;

/**
 * Basic view structure of card.
 * 
 * @author match@wandoujia.com (Diao Liu)
 */
public interface BaseCardView extends BaseView {
  /**
   * Get title of card.
   * 
   * @return title.
   */
  TextView getTitleView();

  /**
   * Get sub-title of card.
   * 
   * @return sub-title.
   */
  TextView getSubTitleView();

  /**
   * Get icon of card.
   * 
   * @return icon.
   */
  AsyncImageView getIconView();

  /**
   * Get iconTips of card.
   *
   * @return icon.
   */
  TextView getIconTipsView();

  /**
   * Get banner of card.
   * 
   * @return banner.
   */
  AsyncImageView getBannerView();

  /**
   * Get description of card.
   * 
   * @return description.
   */
  TextView getDescriptionView();

  /**
   * Get tag of card.
   * 
   * @return tag.
   */
  TextView getTagView();

  /**
   * Get badge of card.
   * 
   * @return badge.
   */
  ImageView getBadgeView();

  /**
   * Get sub-action button of card.
   *
   * @return sub-action button.
   */
  SubActionButton getSubActionButtonView();

}
