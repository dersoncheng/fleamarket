package com.zhangyu.fleamarket.card.models;

import android.view.View;
import android.widget.TextView;

import com.zhangyu.fleamarket.action.Action;
import com.zhangyu.fleamarket.view.button.ButtonState;
import com.zhangyu.fleamarket.view.button.StatefulButton;
import com.zhangyu.fleamarket.view.button.SubActionButton;

import java.util.List;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class SimpleActionCardViewModel implements ActionCardViewModel {

  @Override
  public CharSequence getTitle(TextView titleView) {
    return null;
  }

  @Override
  public CharSequence getSubTitle(TextView subTitleView) {
    return null;
  }

  @Override
  public String getIcon() {
    return null;
  }

  @Override
  public CharSequence getDescription() {
    return null;
  }

  @Override
  public String getBanner() {
    return null;
  }

  @Override
  public CharSequence getTag() {
    return null;
  }

  @Override
  public Action getTagAction(View tagView) {
    return null;
  }

  @Override
  public Action getCardAction(View cardView) {
    return null;
  }

  @Override
  public List<SubActionButton.SubActionItem> getSubActions(View subActionButton) {
    return null;
  }

  @Override
  public BadgeType getBadgeType() {
    return null;
  }

  @Override
  public List<SubBadgeType> getSubBadges() {
    return null;
  }

  @Override
  public ButtonState getButtonState(StatefulButton button) {
    return null;
  }

  public String getID() {
    return null;
  };

}
