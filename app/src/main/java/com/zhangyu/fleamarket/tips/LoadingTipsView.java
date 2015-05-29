package com.zhangyu.fleamarket.tips;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zhangyu.fleamarket.R;


/**
 * @author zhulantian@wandoujia.com (Lantian)
 */
public class LoadingTipsView extends FrameLayout {

  private Animation rotateAnim;
  private ImageView loadingImage;

  public LoadingTipsView(Context context) {
    super(context);
  }

  public LoadingTipsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public LoadingTipsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onFinishInflate() {
    rotateAnim = AnimationUtils.loadAnimation(getContext(), R.anim.cycle_rotate);
    rotateAnim.setInterpolator(new LinearInterpolator());
    loadingImage = (ImageView) findViewById(R.id.loading_imageview);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    updateAnim();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    clearAnimation();
  }

  private void updateAnim() {
    if (isShown()) {
      loadingImage.startAnimation(rotateAnim);
    } else {
      loadingImage.clearAnimation();
    }
  }


  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);
    updateAnim();
  }
}
