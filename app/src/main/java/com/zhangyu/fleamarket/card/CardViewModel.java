package com.zhangyu.fleamarket.card;

import android.view.View;
import android.widget.TextView;

import com.wandoujia.mvc.BaseModel;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.action.Action;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.view.button.SubActionButton;

import java.util.List;

/**
 * This is the card view model. Each content filled in
 * {@link com.zhangyu.fleamarket.card.BaseCardView}, should be convert to
 * <code>CardViewModel</code> and be bind by
 * {@link com.zhangyu.fleamarket.card.controllers.CardViewController}.
 *
 * @author match@wandoujia.com (Diao Liu)
 */
public interface CardViewModel extends BaseModel {

  public static final String TEXT_DIVIDER = "  ";

  /**
   * Badge type.
   */
  public static enum BadgeType {
    NEW_ARRIVAL(R.drawable.ic_mything_badge_new);
    private int imageResId;

    BadgeType(int imageResId) {
      this.imageResId = imageResId;
    }

    public int getImageResId() {
      return imageResId;
    }
  }

  /**
   * Sub badge type.
   */
  public static enum SubBadgeType {

    NOT_DOWNLOADED(FleaMarketApplication.getAppContext().getString(R.string.about_dialog_version));

    private boolean isText;
    private boolean isVerticalColor = false;
    private String text;
    private int textColor;
    private int imageResId;

    SubBadgeType(String text, int textColor) {
      this.isText = true;
      this.textColor = textColor;
      this.text = text;
    }

    SubBadgeType(String text) {
      this.text = text;
      this.isText = true;
      isVerticalColor = true;
    }

    SubBadgeType(int imageResId) {
      this.isText = false;
      this.imageResId = imageResId;
    }

    public boolean isText() {
      return isText;
    }

    public int getTextColor() {
      return textColor;
    }

    public String getText() {
      return text;
    }

    public int getImageResId() {
      return imageResId;
    }

    public boolean isVerticalColor() {
      return isVerticalColor;
    }
  }

  public static enum TagType {
    VERTICAL,
    TAG,
    NONE
  }

  public static enum ModelType {
    COMMON, MUSIC,SEARCH,MP3DOWNLOAD
  }

  CharSequence getTitle(TextView titleView);

  CharSequence getSubTitle(TextView subTitleView);

  String getIcon();

  CharSequence getDescription();

  String getBanner();

  CharSequence getTag();

  Action getTagAction(View tagView);

  Action getCardAction(View cardView);

  List<SubActionButton.SubActionItem> getSubActions(View subActionButton);

  BadgeType getBadgeType();

  List<SubBadgeType> getSubBadges();
}
