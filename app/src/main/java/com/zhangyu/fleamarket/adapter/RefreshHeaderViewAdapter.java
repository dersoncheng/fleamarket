package com.zhangyu.fleamarket.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.zhangyu.fleamarket.view.RefreshingHeaderView;

import java.util.List;

/**
 * Adapter which has a refreshing region as a header.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class RefreshHeaderViewAdapter extends HeaderViewAdapter {
  protected RefreshingHeaderView refreshView;
  public RefreshHeaderViewAdapter(List<View> headerViews,
                                  List<View> footerViews,
                                  ListAdapter adapter) {
    super(headerViews, footerViews, adapter);
  }

  public void showRefreshHeader(ViewGroup parent) {
    if (refreshView == null) {
      refreshView = RefreshingHeaderView.newInstance(parent);
      mHeaderViews.add(0, refreshView);
      notifyDataSetChanged();
    }
  }

  public void hideRefreshHeader() {
    if (refreshView != null) {
      refreshView.hide(new RefreshingHeaderView.OnHideListener() {
        @Override
        public void onHide() {
          mHeaderViews.remove(refreshView);
          refreshView = null;
          notifyDataSetChanged();
        }
      });
    }
  }

  @Override
  public void addHeader(int position, View v) {
    if (refreshView != null) {
      ++position;
    }
    super.addHeader(position, v);
  }

  public boolean isRefreshHeaderShown() {
    return refreshView != null;
  }
}
