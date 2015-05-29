package com.zhangyu.fleamarket.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.widget.ScrollView;


/**
 * @author yingyixu@wandoujia.com (Yingyi Xu)
 */
public class ContentScrollView extends ScrollView {

  public interface OnScrollChangedListener {
    void onScrollChanaged(int l, int t, int oldl, int oldt);
  }

  private OnScrollChangedListener listener;

  public ContentScrollView(Context context) {
    super(context);
  }

  public ContentScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ContentScrollView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setOnScrollChangeListener(OnScrollChangedListener listener) {
    this.listener = listener;
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    listener.onScrollChanaged(l, t, oldl, oldt);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    ViewParent parent = this.getParent();
    while (parent != null) {
      if (parent instanceof ScrollDownLayout) {
        ((ScrollDownLayout) parent).setAssociatedScrollView(this);
        break;
      }
      parent = parent.getParent();
    }
  }
}
