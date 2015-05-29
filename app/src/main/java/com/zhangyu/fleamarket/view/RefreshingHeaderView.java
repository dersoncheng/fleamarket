package com.zhangyu.fleamarket.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.internal.nineoldandroids.animation.Animator;
import com.actionbarsherlock.internal.nineoldandroids.animation.ValueAnimator;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.utils.ViewUtils;

import java.util.Calendar;

/**
 * Header view in refresh list view, which show a "refreshing message"
 */
public class RefreshingHeaderView extends RelativeLayout {

  private TextView cacheTimeTextView;
  private View refreshHeaderContent;
  private int height;

  public RefreshingHeaderView(Context context) {
    super(context);
  }

  public RefreshingHeaderView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    cacheTimeTextView = (TextView) findViewById(R.id.last_cache_time_text);
    refreshHeaderContent = findViewById(R.id.refresh_header_content);
    measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    height = getMeasuredHeight();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    height = getMeasuredHeight();
  }

  public static RefreshingHeaderView newInstance(ViewGroup parent) {
    return (RefreshingHeaderView) ViewUtils.newInstance(parent, R.layout.flea_refresh_header);
  }

  public void setLastCacheTime(long timestamp) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timestamp);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    String text = String.format(getContext().getString(R.string.flea_last_modify_time),
        month + 1, day);
    cacheTimeTextView.setText(text);
  }

  public void hide(final OnHideListener onHideListener) {
    if (height <= 0) {
      return;
    }
    ValueAnimator animator = ValueAnimator.ofInt(0, -height);
    animator.setDuration(300L);
    if (onHideListener != null) {
      animator.addListener(new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
          onHideListener.onHide();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
      });
    }
    final LayoutParams layoutParams = (LayoutParams) refreshHeaderContent.getLayoutParams();
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {
        layoutParams.topMargin = (Integer) animator.getAnimatedValue();
        requestLayout();
      }
    });
    animator.start();
  }

  public interface OnHideListener {
    void onHide();
  }

}
