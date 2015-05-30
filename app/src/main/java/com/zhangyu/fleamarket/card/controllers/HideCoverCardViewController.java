package com.zhangyu.fleamarket.card.controllers;

import android.view.View;

import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.card.BaseCardView;
import com.zhangyu.fleamarket.card.CardViewModel;

/**
 * A CardViewController which can hide cover when the cover info is null.
 *
 * @author wheam@wandoujia.com (Qi Zhang)
 */
public class HideCoverCardViewController extends CardViewController {
  @Override
  protected void fillIcon(BaseCardView view, CardViewModel model) {
    view.getIconView().setVisibility(View.VISIBLE);
    if (model.getIcon() != null) {
      super.fillIcon(view, model);
    } else {
      view.getIconView().setImageDrawable(FleaMarketApplication.getAppContext().getResources().
          getDrawable(R.drawable.default_icon));
    }
  }
}
