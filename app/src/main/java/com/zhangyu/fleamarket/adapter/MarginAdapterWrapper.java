package com.zhangyu.fleamarket.adapter;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.zhangyu.fleamarket.stickylistheaders.StickyListHeadersAdapter;


/**
 * Margin adapter wrapper.
 * 
 * @author match@wandoujia.com (Diao Liu)
 */
public abstract class MarginAdapterWrapper extends BaseAdapter
    implements StickyListHeadersAdapter {

  private static final int EXTRA_VIEW_TYPE_COUNT = 1;
  private static final int MARGIN_VIEW_TYPE_OFFSET = 0;
  private int marginViewType = 1;
  public static final int COUNT_MULTIPLIER = 3;

  private BaseAdapter delegate;

  public MarginAdapterWrapper(BaseAdapter delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getCount() {
    return delegate.getCount() * COUNT_MULTIPLIER;
  }

  protected int translateListViewPosition(int position) {
    return position / COUNT_MULTIPLIER;
  }

  @Override
  public int getItemViewType(int position) {
    if (position % COUNT_MULTIPLIER == 1) {
      return delegate.getItemViewType(translateListViewPosition(position));
    } else {
      return marginViewType;
    }
  }

  @Override
  public int getViewTypeCount() {
    marginViewType = delegate.getViewTypeCount() + MARGIN_VIEW_TYPE_OFFSET;
    return delegate.getViewTypeCount() + EXTRA_VIEW_TYPE_COUNT;
  }

  @Override
  public Object getItem(int position) {
    return delegate.getItem(translateListViewPosition(position));
  }

  @Override
  public long getItemId(int position) {
    return delegate.getItemId(translateListViewPosition(position));
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (getItemViewType(position) == marginViewType) {
      return getMarginView(position, convertView, parent);
    } else {
      return delegate.getView(translateListViewPosition(position), convertView, parent);
    }
  }

  private boolean isMarginTop(int position) {
    return position % COUNT_MULTIPLIER == 0;
  }

  private boolean isMarginBottom(int position) {
    return position % COUNT_MULTIPLIER == 2;
  }

  private View getMarginView(int position, View convertView, ViewGroup parent) {
    if (getItemViewType(position) != marginViewType) {
      throw new IllegalStateException(
          "The view type at the position must be margin view type");
    }
    int marginHeight = 0;
    if (isMarginTop(position)) {
      marginHeight = getItemMarginTop(translateListViewPosition(position));
    } else if (isMarginBottom(position)) {
      marginHeight = getItemMarginBottom(translateListViewPosition(position));
    } else {
      throw new IllegalStateException(
          "The view at the position must be margin top or margin bottom.");
    }
    if (convertView == null) {
      convertView = new View(parent.getContext());
      AbsListView.LayoutParams params = new AbsListView.LayoutParams(
          AbsListView.LayoutParams.MATCH_PARENT, marginHeight);
      convertView.setLayoutParams(params);
    } else {
      AbsListView.LayoutParams params = (AbsListView.LayoutParams) convertView.getLayoutParams();
      params.height = marginHeight;
      convertView.setLayoutParams(params);
    }
    return convertView;
  }

  @Override
  public void registerDataSetObserver(DataSetObserver observer) {
    super.registerDataSetObserver(observer);
    delegate.registerDataSetObserver(observer);
  }

  @Override
  public void unregisterDataSetObserver(DataSetObserver observer) {
    super.unregisterDataSetObserver(observer);
    delegate.unregisterDataSetObserver(observer);
  }

  @Override
  public View getHeaderView(int position, View convertView, ViewGroup parent) {
    return ((StickyListHeadersAdapter) delegate).getHeaderView(translateListViewPosition(position),
        convertView, parent);
  }

  @Override
  public long getHeaderId(int position) {
    return ((StickyListHeadersAdapter) delegate).getHeaderId(translateListViewPosition(position));
  }

  public abstract int getItemMarginTop(int position);

  public abstract int getItemMarginBottom(int position);
}
