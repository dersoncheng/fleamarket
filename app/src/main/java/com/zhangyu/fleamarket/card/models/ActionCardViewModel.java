package com.zhangyu.fleamarket.card.models;

import com.zhangyu.fleamarket.card.CardViewModel;
import com.zhangyu.fleamarket.view.button.ButtonState;
import com.zhangyu.fleamarket.view.button.StatefulButton;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public interface ActionCardViewModel extends CardViewModel {
  ButtonState getButtonState(StatefulButton button);
}
