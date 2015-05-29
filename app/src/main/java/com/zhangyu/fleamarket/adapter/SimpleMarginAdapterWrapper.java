package com.zhangyu.fleamarket.adapter;

import android.widget.BaseAdapter;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class SimpleMarginAdapterWrapper extends MarginAdapterWrapper {

  protected int topItemMarginTop;
  protected int bottomItemMarginBottom;
  private BaseAdapter delegate;

  public SimpleMarginAdapterWrapper(BaseAdapter delegate, int topItemMarginTop,
                                    int bottomItemMarginBottom) {
    super(delegate);
    this.delegate = delegate;
    this.topItemMarginTop = topItemMarginTop;
    this.bottomItemMarginBottom = bottomItemMarginBottom;
  }

  public void setTopItemMarginTop(int topItemMarginTop) {
    this.topItemMarginTop = topItemMarginTop;
  }

  public void setBottomItemMarginBottom(int bottomItemMarginBottom) {
    this.bottomItemMarginBottom = bottomItemMarginBottom;
  }

  @Override
  public int getItemMarginTop(int position) {
    if (position == 0) {
      return topItemMarginTop;
    }
    return 0;
  }

  @Override
  public int getItemMarginBottom(int position) {
    if (position == delegate.getCount() - 1) {
      return bottomItemMarginBottom;
    }
    return 0;
  }

}
