package com.zhangyu.fleamarket.tips;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zhangyu.fleamarket.utils.ViewUtils;
import com.zhangyu.fleamarket.view.CoverViewContainer;


/**
 * @author zhulantian@wandoujia.com (Lantian)
 */
public class Tips {

  public final View view;
  public final boolean hideTarget;
  public final FrameLayout.LayoutParams layoutParams;

  public Tips(Context context, int layoutRes) {
    this(context, layoutRes, true);
  }

  public Tips(View view) {
    this(view, true);
  }

  public Tips(Context context, int layoutRes, boolean hideTarget) {
    this(ViewUtils.newInstance(new FrameLayout(context), layoutRes), hideTarget);
  }

  public Tips(View view, boolean hideTarget) {
    this.hideTarget = hideTarget;
    this.view = view;
    // add tag to this view.
    this.view.setTag(this);
    ViewGroup.LayoutParams lp = view.getLayoutParams();
    if (lp instanceof FrameLayout.LayoutParams) {
      this.layoutParams = (FrameLayout.LayoutParams) lp;
    } else {
      this.layoutParams =
          new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT);
    }
  }

  /**
   * Apply the tips view to the target view.
   * 
   * @param target target view to show at.
   * @param tipsId tips view id.
   * @return
   */
  public View applyTo(View target, int tipsId) {
    ViewGroup parent = (ViewGroup) target.getParent();
    if (parent == null) {
      return null;
    }
    View tipsView;
    if (parent instanceof CoverViewContainer) {
      tipsView = addTipsViewToContainer(target, parent, tipsId);
    } else {
      CoverViewContainer tipsContainerView = new CoverViewContainer(target.getContext());
      ViewGroup.LayoutParams targetParams = target.getLayoutParams();
      int index = parent.indexOfChild(target);
      parent.removeViewAt(index);
      parent.addView(tipsContainerView, index, targetParams);
      Drawable backgroud = target.getBackground();
      if (backgroud != null) {
        ViewUtils.setBackground(tipsContainerView, backgroud);
      }
      tipsView = addTipsViewToContainer(target, tipsContainerView, tipsId);
    }
    return tipsView;
  }

  private View addTipsViewToContainer(View target, ViewGroup tipContainer, int tipsId) {
    View tipsView = TipsViewUtil.findChildViewById(tipContainer, tipsId);
    if (tipsView != null) {
      tipsView.bringToFront();
      return tipsView;
    } else {
      view.setId(tipsId);
      FrameLayout.LayoutParams targetViewLayoutParams = new FrameLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      if (hideTarget) {
        target.setVisibility(View.INVISIBLE);
      }
      if (tipContainer.indexOfChild(target) == -1) {
        tipContainer.addView(target, targetViewLayoutParams);
      } else {
        target.setLayoutParams(targetViewLayoutParams);
      }
      tipContainer.addView(view, layoutParams);
      return view;
    }
  }
}
