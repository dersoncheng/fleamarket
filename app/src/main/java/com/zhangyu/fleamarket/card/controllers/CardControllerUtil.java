package com.zhangyu.fleamarket.card.controllers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;

import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.card.CardViewModel;

import java.util.List;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class CardControllerUtil {
  private CardControllerUtil() {}

  public static CharSequence getSubBadgeSpannable(final Context context, final int textSize,
      List<CardViewModel.SubBadgeType> subBadges) {
    if (subBadges != null && !subBadges.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (CardViewModel.SubBadgeType subBadgeType : subBadges) {
        if (subBadgeType.isText()) {
          builder.append(subBadgeType.getText());
        } else {
          builder.append(" ");
        }
        builder.append(" ");
      }
      Spannable spannable = new SpannableStringBuilder(builder.toString());
      int start = 0;
      for (final CardViewModel.SubBadgeType subBadgeType : subBadges) {
        if (subBadgeType.isText()) {
          int textColor;
          if (subBadgeType.isVerticalColor()) {
            TypedArray typedArray =
                context.obtainStyledAttributes(R.styleable.NirvanaTheme);
            final int colorRes =
                typedArray.getResourceId(R.styleable.NirvanaTheme_verticalNormalColor,
                    R.color.explore_home_color_normal);
            textColor = context.getResources().getColor(colorRes);
            typedArray.recycle();
          } else {
            textColor = subBadgeType.getTextColor();
          }
          spannable.setSpan(new ForegroundColorSpan(textColor), start,
              start + subBadgeType.getText().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
          start += subBadgeType.getText().length() + 1;
        } else {
          spannable.setSpan(getDrawableSpan(textSize, subBadgeType.getImageResId()), start,
              start + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
          start += 2;
        }
      }
      return spannable;
    }
    return "";
  }

  public static CharSequence getForegroundColorSpan(String text, int color) {
    SpannableString spannableString = new SpannableString(text);
    spannableString.setSpan(new ForegroundColorSpan(color), 0,
        text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    return spannableString;
  }

  public static DynamicDrawableSpan getDrawableSpan(final int textSize, final int drawableId) {
    return new DynamicDrawableSpan(DynamicDrawableSpan.ALIGN_BASELINE) {
      @Override
      public Drawable getDrawable() {
        Drawable drawable =
            FleaMarketApplication.getAppContext().getResources().getDrawable(drawableId);
        if (drawable != null && drawable.getIntrinsicHeight() != 0) {
          float height = textSize;
          float width =
              drawable.getIntrinsicWidth() * height / drawable.getIntrinsicHeight();
          drawable.setBounds(0, 0, (int) width, (int) height);
        }
        return drawable;
      }
    };
  }

}
