package com.zhangyu.fleamarket.card.controllers;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;

import com.wandoujia.image.ImageUri;
import com.wandoujia.image.util.AsyncImageViewUtil;
import com.wandoujia.mvc.BaseController;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.action.Action;
import com.zhangyu.fleamarket.card.BaseCardView;
import com.zhangyu.fleamarket.card.CardViewModel;
import com.zhangyu.fleamarket.view.button.SubActionButton;

import java.util.List;

/**
 * This controller used for binding CardViewBase and CardModel includes
 * setText、setOnclickListener、setImage for view with model.
 * 
 * @author match@wandoujia.com (Diao Liu)
 */
public class CardViewController implements BaseController<BaseCardView, CardViewModel> {

  /**
   * Bind the view and model. This method helps to setText/setImage/setOnclickListener for views.
   * 
   * @param view view
   * @param model model
   */
  public void bind(final BaseCardView view, final CardViewModel model) {
    setLogTag(view, model);
    fillTitle(view, model);
    fillSubTitle(view, model);
    fillDescription(view, model);
    fillBadge(view, model);
    fillIcon(view, model);
    fillBanner(view, model);
    fillSubAction(view, model);
    if (view.getTagView() != null) {
      if (TextUtils.isEmpty(model.getTag())) {
        view.getTagView().setVisibility(View.GONE);
      } else {
        view.getTagView().setVisibility(View.VISIBLE);
        view.getTagView().setText(model.getTag());
        view.getTagView().setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Action action = model.getTagAction(view.getView());
            if (action != null) {
              action.execute();
            }
          }
        });
      }
    }
    if (view.getView() != null) {
      view.getView().setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          Action action = model.getCardAction(view.getView());
          if (action != null) {
            action.execute();
          }
        }
      });
    }
  }

  /**
   * Fill view's title with model.
   * 
   * @param view view
   * @param model model
   */
  private void fillTitle(BaseCardView view, CardViewModel model) {
    if (view.getTitleView() != null) {
      view.getTitleView().setText(model.getTitle(view.getTitleView()));
    }
  }

  /**
   * Fill view's subTitle with model.
   * 
   * @param view view
   * @param model model
   */
  protected void fillSubTitle(BaseCardView view, CardViewModel model) {
    if (view.getSubTitleView() != null) {
      SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();
      final List<CardViewModel.SubBadgeType> subBadgeTypes = model.getSubBadges();
      if (subBadgeTypes != null && !subBadgeTypes.isEmpty()) {
        spannableBuilder.append(CardControllerUtil.getSubBadgeSpannable(
            view.getSubTitleView().getContext(),
            (int) view.getSubTitleView().getTextSize(), subBadgeTypes));
      }
      final CharSequence subTitle = model.getSubTitle(view.getSubTitleView());
      if (subTitle != null) {
        spannableBuilder.append(subTitle);
      }
      view.getSubTitleView().setText(spannableBuilder);
    }
  }

  /**
   * Fill view's description with model.
   * 
   * @param view view
   * @param model model
   */
  private void fillDescription(BaseCardView view, CardViewModel model) {
    if (view.getDescriptionView() != null) {
      view.getDescriptionView().setText(model.getDescription());
    }
  }

  /**
   * Fill view's badge with model.
   * 
   * @param view view
   * @param model model
   */
  private void fillBadge(BaseCardView view, CardViewModel model) {
    if (view.getBadgeView() == null) {
      return;
    }
    CardViewModel.BadgeType type = model.getBadgeType();
    if (type != null) {
      view.getBadgeView().setVisibility(View.VISIBLE);
      view.getBadgeView().setImageResource(type.getImageResId());
    } else {
      view.getBadgeView().setVisibility(View.GONE);
    }
  }

  /**
   * Fill view's icon with model.
   * 
   * @param view view
   * @param model model
   */
  protected void fillIcon(BaseCardView view, CardViewModel model) {
    if (view.getIconView() != null) {
      String defIcon = String.valueOf(R.drawable.default_icon);
      String icon = model.getIcon();
      ImageUri uri = null;
      int defResId = R.drawable.aa_version_check_dialog;
      if (!TextUtils.isEmpty(icon) && !TextUtils.equals(icon, defIcon)) {
        uri = new ImageUri(icon, ImageUri.ImageUriType.UNSPECIFIED);
        defResId = R.color.bg_image_default;
      }
      AsyncImageViewUtil.loadImage(view.getIconView(), uri, defResId);
    }
  }

  /**
   * Fill view's banner with model.
   * 
   * @param view view
   * @param model model
   */
  protected void fillBanner(BaseCardView view, CardViewModel model) {
    if (view.getBannerView() != null) {
      view.getBannerView().loadNetworkImage(model.getBanner(),
          R.color.bg_image_default);
    }
  }

  protected void fillSubAction(BaseCardView view, CardViewModel model) {
    if (view.getSubActionButtonView() != null) {
      List<SubActionButton.SubActionItem> items =
          model.getSubActions(view.getSubActionButtonView());
      if (items == null || items.isEmpty()) {
        view.getSubActionButtonView().setVisibility(View.GONE);
      } else {
        view.getSubActionButtonView().setVisibility(View.VISIBLE);
        view.getSubActionButtonView().setData(items);
      }
    }
  }

  private void setLogTag(BaseCardView view, CardViewModel model) {
  }
}
