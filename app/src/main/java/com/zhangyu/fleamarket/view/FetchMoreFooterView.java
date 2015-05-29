package com.zhangyu.fleamarket.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.utils.ViewUtils;


/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class FetchMoreFooterView extends FrameLayout {

  private View loadingView;
  private ImageView noMoreView;

  public FetchMoreFooterView(Context context) {
    super(context);
  }

  public FetchMoreFooterView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FetchMoreFooterView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    loadingView = findViewById(R.id.loading);
    noMoreView = (ImageView) findViewById(R.id.no_more);
  }

  public static FetchMoreFooterView newInstance(ViewGroup parent) {
    return (FetchMoreFooterView) ViewUtils.newInstance(parent, R.layout.flea_load_more_list_footer);
  }

  public void showLoading() {
    loadingView.setVisibility(View.VISIBLE);
    noMoreView.setVisibility(View.GONE);
    noMoreView.setImageDrawable(null);
  }

  public void showNoMore() {
    loadingView.setVisibility(View.GONE);
    noMoreView.setVisibility(View.VISIBLE);
    noMoreView.setImageResource(R.drawable.ic_explore_end);
  }
}
