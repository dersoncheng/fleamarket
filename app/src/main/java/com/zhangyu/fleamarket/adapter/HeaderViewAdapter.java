package com.zhangyu.fleamarket.adapter;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by xubin on 13-7-12.
 */
public class HeaderViewAdapter extends BaseAdapter {

  private final ListAdapter mAdapter;
  protected final List<View> mHeaderViews;
  private final List<View> mFooterViews;

  public HeaderViewAdapter(List<View> headerViews, List<View> footerViews, ListAdapter adapter) {
    mAdapter = adapter;

    if (headerViews == null) {
      mHeaderViews = new ArrayList<View>();
    } else {
      mHeaderViews = headerViews;
    }

    if (footerViews == null) {
      mFooterViews = new ArrayList<View>();
    } else {
      mFooterViews = footerViews;
    }
  }

  public void setHeaders(Collection<? extends View> headers) {
    mHeaderViews.clear();
    mHeaderViews.addAll(headers);
    notifyDataSetChanged();
  }

  public void clearHeaders() {
    mHeaderViews.clear();
    notifyDataSetChanged();
  }

  public void clearFooters() {
    mFooterViews.clear();
    notifyDataSetChanged();
  }

  public int getHeadersCount() {
    return mHeaderViews.size();
  }

  public int getFootersCount() {
    return mFooterViews.size();
  }

  public boolean isEmpty() {
    return mAdapter == null || mAdapter.isEmpty();
  }

  public void addHeader(View v) {
    if (!mHeaderViews.contains(v)) {
      mHeaderViews.add(v);
      notifyDataSetChanged();
    }
  }

  public void addHeader(int position, View v) {
    if (!mHeaderViews.contains(v)) {
      if (position > mHeaderViews.size()) {
        position = mHeaderViews.size();
      }
      mHeaderViews.add(position, v);
      notifyDataSetChanged();
    }
  }

  public void addFooter(View v) {
    if (!mFooterViews.contains(v)) {
      mFooterViews.add(v);
      notifyDataSetChanged();
    }
  }

  public void addFooter(int postion, View v) {
    if (!mFooterViews.contains(v)) {
      mFooterViews.add(postion, v);
      notifyDataSetChanged();
    }
  }

  public boolean removeHeader(View v) {
    for (int i = 0; i < mHeaderViews.size(); i++) {
      View headerView = mHeaderViews.get(i);
      if (headerView == v) {
        mHeaderViews.remove(i);
        notifyDataSetChanged();
        return true;
      }
    }
    return false;
  }

  public boolean removeFooter(View v) {
    for (int i = 0; i < mFooterViews.size(); i++) {
      View footerView = mFooterViews.get(i);
      if (footerView == v) {
        mFooterViews.remove(i);
        notifyDataSetChanged();
        return true;
      }
    }
    return false;
  }

  public int getCount() {
    if (mAdapter != null) {
      return getFootersCount() + getHeadersCount() + mAdapter.getCount();
    } else {
      return getFootersCount() + getHeadersCount();
    }
  }

  public Object getItem(int position) {
    int numHeaders = getHeadersCount();
    if (position < numHeaders) {
      return null;
    }

    final int adjPosition = position - numHeaders;
    int adapterCount = 0;
    if (mAdapter != null) {
      adapterCount = mAdapter.getCount();
      if (adjPosition < adapterCount) {
        return mAdapter.getItem(adjPosition);
      }
    }

    return null;
  }

  public long getItemId(int position) {
    int numHeaders = getHeadersCount();
    if (mAdapter != null && position >= numHeaders) {
      int adjPosition = position - numHeaders;
      int adapterCount = mAdapter.getCount();
      if (adjPosition < adapterCount) {
        return mAdapter.getItemId(adjPosition);
      }
    }
    return -1;
  }

  public boolean hasStableIds() {
    if (mAdapter != null) {
      return mAdapter.hasStableIds();
    }
    return false;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    // Header (negative positions will throw an ArrayIndexOutOfBoundsException)
    int numHeaders = getHeadersCount();
    if (position < numHeaders) {
      return mHeaderViews.get(position);
    }

    // Adapter
    final int adjPosition = position - numHeaders;
    int adapterCount = 0;
    if (mAdapter != null) {
      adapterCount = mAdapter.getCount();
      if (adjPosition < adapterCount) {
        return mAdapter.getView(adjPosition, convertView, parent);
      }
    }

    // Footer (off-limits positions will throw an ArrayIndexOutOfBoundsException)
    return mFooterViews.get(adjPosition - adapterCount);
  }

  public int getItemViewType(int position) {
    int numHeaders = getHeadersCount();
    if (mAdapter != null && position >= numHeaders) {
      int adjPosition = position - numHeaders;
      int adapterCount = mAdapter.getCount();
      if (adjPosition < adapterCount) {
        return mAdapter.getItemViewType(adjPosition);
      }
    }

    // This means that header or footer view do not need convert view
    return IGNORE_ITEM_VIEW_TYPE;
  }

  public int getViewTypeCount() {
    if (mAdapter != null) {
      return mAdapter.getViewTypeCount();
    }
    return 1;
  }

  public void registerDataSetObserver(DataSetObserver observer) {
    super.registerDataSetObserver(observer);
    if (mAdapter != null) {
      mAdapter.registerDataSetObserver(observer);
    }
  }

  public void unregisterDataSetObserver(DataSetObserver observer) {
    super.unregisterDataSetObserver(observer);
    if (mAdapter != null) {
      mAdapter.unregisterDataSetObserver(observer);
    }
  }

  public ListAdapter getWrappedAdapter() {
    return mAdapter;
  }
}
