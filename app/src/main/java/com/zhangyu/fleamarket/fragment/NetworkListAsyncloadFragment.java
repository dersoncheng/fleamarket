package com.zhangyu.fleamarket.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wandoujia.base.utils.CollectionUtils;
import com.wandoujia.mvc.BaseModel;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.adapter.DataAdapter;
import com.zhangyu.fleamarket.adapter.HeaderViewAdapter;
import com.zhangyu.fleamarket.adapter.MarginAdapterWrapper;
import com.zhangyu.fleamarket.adapter.RefreshHeaderViewAdapter;
import com.zhangyu.fleamarket.adapter.SimpleMarginAdapterWrapper;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.configs.Intents;
import com.zhangyu.fleamarket.http.fetcher.BaseFetcher;
import com.zhangyu.fleamarket.http.fetcher.FetchHelper;
import com.zhangyu.fleamarket.tips.FetchFailedTipsUtil;
import com.zhangyu.fleamarket.tips.TipsType;
import com.zhangyu.fleamarket.tips.TipsViewUtil;
import com.zhangyu.fleamarket.utils.ViewUtils;
import com.zhangyu.fleamarket.view.ContentListView;
import com.zhangyu.fleamarket.view.FetchMoreFooterView;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A base fragment for common network content display, M is the model type in list.
 *
 * @author wenchengye@wandoujia.com
 */
public abstract class NetworkListAsyncloadFragment<M extends BaseModel>
    extends NetworkAsyncLoadFragment {

  private static final long TOAST_TIME = 1000L;
  private static final long SHOW_REFRESHING_DELAY_MS = 5000L;
  private static final long SHOW_REFRESH_SLOW_DELAY_MS = 10 * 1000L;
  private ListView contentListView;
  private FetchMoreFooterView footerView;
  private RefreshHeaderViewAdapter headerViewAdapter;
  private DataAdapter<M> contentAdapter;
  private FetchHelper<M> fetchHelper;
  private List<M> newData;

  private int lastTryFetch;
  private Parcelable listState;
  private boolean loadedGapData;

  protected static final int FIRST_FETCH_COUNT = 15;
  protected static final int PAGE_SIZE = 15;
  private static final int DEFAULT_GAP_SIZE = 15;
  private boolean isScrolling;
  private ToastHelper toastHelper;

  protected BaseFetcher.Callback<M> fetchCallback =
      new BaseFetcher.Callback<M>() {
        @Override
        public void onFetched(int start, int size, BaseFetcher.ResultList<M> result) {
          NetworkListAsyncloadFragment.this.onFetched(start, size, result);
        }

        @Override
        public void onFailed(int start, ExecutionException e) {
          NetworkListAsyncloadFragment.this.onFailed(start, e);
        }
      };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      listState = savedInstanceState.getParcelable(Intents.EXTRA_LIST_STATE);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (outState != null && contentListView != null) {
      outState.putParcelable(Intents.EXTRA_LIST_STATE, contentListView.onSaveInstanceState());
    }
    super.onSaveInstanceState(outState);
  }

  public void cancelLoadingToast() {
    if (toastHelper != null) {
      toastHelper.cancelAllMessage();
    }
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.aa_common_listview_card;
  }

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    contentListView = newListView(contentView);
    ViewUtils.enableScrollToTop(contentListView);
    footerView = FetchMoreFooterView.newInstance(contentListView);
    contentListView.setAdapter(newListAdapter());
    contentListView.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                           int totalItemCount) {

        if (needPreLoad()) {
          int countMultiplier = 1;
          // for margin adapter tadd header and footer for every item, so the total item is
          // the three multiple of the data adapter.
          if (contentListView.getAdapter() instanceof MarginAdapterWrapper) {
            countMultiplier = MarginAdapterWrapper.COUNT_MULTIPLIER;
          }

          if (visibleItemCount + firstVisibleItem == totalItemCount
              && contentAdapter.getCount() > 0) {
            // if user scrolls the listView too fast, fetcher has fetched the data and hasn't
            // notify the data has changed, need update the listView when scroll to the end.
            if (newData != null) {
              contentAdapter.setData(newData);
              newData = null;
            }

            // if the first fetched total size is smaller than the visible item,
            // need load more data to show.
            if (!loadedGapData) {
              onLoadingMore();
            }
          } else if (visibleItemCount + firstVisibleItem >= totalItemCount
              - getPreLoadGap() * countMultiplier
              && contentAdapter.getCount() > 0) {

            if (totalItemCount == lastTryFetch) {
              return;
            }
            // user has scroll the list view and reach the gap item, first loadedGap set true.
            // doesn't need load more data when reach the end next time.
            loadedGapData = true;
            lastTryFetch = totalItemCount;
            onLoadingMore();
          }
        } else {
          if (visibleItemCount + firstVisibleItem == totalItemCount
              && contentAdapter.getCount() > 0) {
            if (totalItemCount == lastTryFetch) {
              return;
            }
            lastTryFetch = totalItemCount;
            onLoadingMore();
          }
        }
      }

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (needPreLoad()) {
          if (SCROLL_STATE_IDLE == scrollState) {
            if (newData != null) {
              contentAdapter.setData(newData);
              newData = null;
            }
            isScrolling = false;
          } else {
            isScrolling = true;
          }
        }
      }
    });

    isInflated = true;
    isScrolling = false;
    loadedGapData = false;
  }

  protected ListAdapter newListAdapter() {
    contentAdapter = newContentAdapter();
    headerViewAdapter = newWrappedHeaderViewAdapter(null, null,
        contentAdapter);
    return newWrappedMarginAdapter(headerViewAdapter);
  }

  @Override
  protected void onPrepareLoading() {
    lastTryFetch = 0;
    if (!isTwoPhraseLoading() && getListView() != null) {
      TipsViewUtil.showTipsView(getListView(), TipsType.LOADING);
    }
  }

  @Override
  protected void onStartLoading() {
    if (!isInflated) {
      return;
    }
    getFetchHelper().fetch();
  }

  @Override
  public void onPause() {
    super.onPause();
    cancelLoadingToast();
  }

  private void onLoadingMore() {
    if (needToLoadMore()) {
      if (!needPreLoad()) {
        getFetchHelper().fetchMore();
      } else {
        int pageSize = getPageSize() == 0 ? FIRST_FETCH_COUNT : getPageSize();
        getFetchHelper().fetchMore(pageSize);
      }
    }
  }

  private boolean needToLoadMore() {
    // Call super here is to avoid subclass to override needToLoadData and cause some error.
    return super.needToLoadData();
  }

  protected void onFetched(int start, int size, BaseFetcher.ResultList<M> result) {
    TipsViewUtil.hideTipsView(getListView(), TipsType.LOADING);
    hideFetchFailedTipsView();

    if (result.isCache()) { // Data from cache
      if (start == 0) {
        if (result.data.isEmpty()) { // Miss cache, we show loading tip
          onMissCache();
        } else { // Hit cache
          if (result.isTimeout) {
            headerViewAdapter.showRefreshHeader(contentListView);
            getToastHelper().showToastDelayed(Message.REFRESHING, SHOW_REFRESHING_DELAY_MS);
            getToastHelper().showToastDelayed(Message.REFRESH_IS_SLOW, SHOW_REFRESH_SLOW_DELAY_MS);
          }
        }
      }
    } else { // Data from server
      if (result.data.isEmpty()) { // No data
        if (start == 0) { // First page
          onNoFetchResult();
        } else { // Non-first page
          headerViewAdapter.addFooter(footerView);
          footerView.showNoMore();
        }
      } else { // Has data
        if (start == 0) { // First page
          headerViewAdapter.hideRefreshHeader();
          if (contentAdapter.getData() != null && !contentAdapter.getData().isEmpty()) {
            contentAdapter.getData().clear();
            if (isCurrentFragment() && isResumed() && headerViewAdapter.isRefreshHeaderShown()
                && getToastHelper().isRefreshingToastShown) {
              getToastHelper().showToast(Message.REFRESH_SUCCESS);
            }
          }
        }
      }
    }

    if (!result.data.isEmpty()) {
      headerViewAdapter.addFooter(footerView);
      footerView.showLoading();
      newData =
          CollectionUtils.replaceFromPosition(contentAdapter.getData(), result.data, start);
      if (needPreLoad()) {
        // need notification data has changed as following scenarios
        // 1. The first time fetched data need to notification the data has changed right now.
        // 2. fetched data when scroll is idle.
        if (start == 0 || !isScrolling) {
          contentAdapter.setData(newData);
          newData = null;
        }
      } else {
        contentAdapter.setData(newData);
        newData = null;
      }
      headerViewAdapter.notifyDataSetChanged();
    }
    if (listState != null) {
      contentListView.onRestoreInstanceState(listState);
      listState = null;
    }
    if (!result.isCache() && start == 0 && !result.data.isEmpty()) { // First page
      if (contentListView.getSelectedItemPosition() != 0) {
        contentListView.setSelection(0);
      }
    }
  }

  protected void onFailed(final int start, ExecutionException e) {
    TipsViewUtil.hideTipsView(getListView(), TipsType.LOADING);
    hideFetchFailedTipsView();
    showFetchFailedTipsView(start, e);
    if (start == 0) {
      headerViewAdapter.hideRefreshHeader();
      getToastHelper().showToast(Message.REFRESH_FAILED);
    }
  }

  protected FetchHelper<M> getFetchHelper() {
    if (fetchHelper == null) {
      fetchHelper = newFetchHelper();
    }
    return fetchHelper;
  }

  protected void resetFetchHelper() {
    fetchHelper = null;
  }

  protected DataAdapter<M> getContentAdapter() {
    return contentAdapter;
  }

  protected FetchHelper<M> newFetchHelper() {
    if (getFirstFetchSize() != 0 && getPageSize() != 0) {
      return new FetchHelper<M>(newFetcher(), fetchCallback, getFirstFetchSize(), getPageSize(),
          isTwoPhraseLoading());
    }
    return new FetchHelper<M>(newFetcher(), fetchCallback, isTwoPhraseLoading());
  }

  protected HeaderViewAdapter getAdapter() {
    return headerViewAdapter;
  }

  protected ListView getListView() {
    return contentListView;
  }

  protected ListView newListView(View contentView) {
    return (ContentListView) contentView.findViewById(R.id.listview);
  }

  protected boolean isInflated() {
    return isInflated;
  }

  protected RefreshHeaderViewAdapter newWrappedHeaderViewAdapter(List<View> headerViews,
      List<View> footerViews, BaseAdapter adapter) {
    return new RefreshHeaderViewAdapter(headerViews, footerViews, adapter);
  }

  /**
   * Wrap adapter to margin adapter.
   *
   * @param adapter adapter
   * @return Default returns SimpleMarginAdapterWrapper, and the topItemMarginTop and
   *         bottomItemMarginBottom is R.dimen.card_list_padding. If you don't need wrap to margin
   *         adapter, return origin adapter.
   */
  protected BaseAdapter newWrappedMarginAdapter(BaseAdapter adapter) {
    int padding = FleaMarketApplication.getAppContext().getResources()
        .getDimensionPixelSize(R.dimen.card_list_padding);
    return new SimpleMarginAdapterWrapper(adapter, padding, padding);
  }

  protected void showFetchFailedTipsView(final int start, ExecutionException e) {
    View.OnClickListener retryClickListener = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        FetchFailedTipsUtil.hideFetchFailedFloatingTipsView(getListView());
        if (start > 0) {
          onLoadingMore();
        } else {
          requestLoad();
        }
      }
    };
    FetchFailedTipsUtil.showFetchFailedTipsFloating(getContentView(), retryClickListener, e);
  }

  protected void hideFetchFailedTipsView() {
    FetchFailedTipsUtil.hideFetchFailedFloatingTipsView(getContentView());
  }

  protected int getFirstFetchSize() {
    return FIRST_FETCH_COUNT;
  }

  protected int getPageSize() {
    return PAGE_SIZE;
  }

  public FetchMoreFooterView getFooterView() {
    return footerView;
  }

  protected void reload() {
    lastTryFetch = 0;
    if (fetchHelper != null && fetchHelper.getFetcher() != null) {
      fetchHelper.getFetcher().clearCache();
    }

    if (contentListView != null) {
      contentListView.post(new Runnable() {
        @Override
        public void run() {
          // Because we post this, so we need to check if this fragment is not attached
          if (!isAdded()) {
            return;
          }
          onStartLoading();
        }
      });
    }
  }

  protected abstract BaseFetcher<M> newFetcher();

  protected abstract DataAdapter<M> newContentAdapter();

  protected abstract void onNoFetchResult();

  /**
   * Gets called when miss cache on the first page.
   */
  protected void onMissCache() {
    TipsViewUtil.showTipsView(getListView(), TipsType.LOADING);
  }

  /**
   * Returns whether it is two-phrase loading, which from cache and network separately.
   *
   * <p>
   * Subclass should override it to return true, if it is two-phrase loading
   * </p>
   *
   * @return
   */
  protected boolean isTwoPhraseLoading() {
    return false;
  }

  /**
   * return the gap for preLoad.
   *
   * @return
   */
  protected int getPreLoadGap() {
    return DEFAULT_GAP_SIZE;
  }

  /**
   * Returns whether need to pre-load items when user scrolls the listView.
   *
   * @return true will preLoad the item.
   */
  protected boolean needPreLoad() {
    return false;
  }

  private boolean isCurrentFragment() {
    if (getActivity() != null && !getActivity().isFinishing()) {
      Fragment parentFragment = getParentFragment();
      return true;
//      if (parentFragment instanceof TabHostFragment) {
//        TabHostFragment tabHostFragment = (TabHostFragment) parentFragment;
//        return tabHostFragment.getFragment(tabHostFragment.getCurrentItem()) == this;
//      } else {
//        return true;
//      }
    }
    return false;
  }

  private ToastHelper getToastHelper() {
    if (toastHelper == null) {
      toastHelper = new ToastHelper(FleaMarketApplication.getAppContext());
    }
    return toastHelper;
  }

  private enum Message {
    REFRESHING,
    REFRESH_IS_SLOW,
    REFRESH_SUCCESS,
    REFRESH_FAILED
  }

  /**
   * Helper class to show data loading toast
   *
   * <p>The logic is: if there is cache and timeout, we refresh data from network, and
   *   <ul>
   *     <li>if data does not return in 5s, show "refreshing"</li>
   *     <li>if data does not return in 10s, show "data loading is slow"</li>
   *     <li>if fail to load, show "refresh failed"</li>
   *     <li>if success, show "refresh success"</li>
   *   </ul>
   * </p>
   */
  private final class ToastHelper {
    private Toast refreshingToast;
    private Toast refreshSlowToast;
    private Toast refreshSuccessToast;
    private Toast refreshFailedToast;
    private boolean isRefreshingToastShown;
    private Context context;

    public ToastHelper(Context context) {
      this.context = context;
    }

    private final Handler handler = new Handler() {
      @Override
      public void handleMessage(android.os.Message msg) {
        int msgWhat = msg.what;
        Message message = Message.values()[msgWhat];
        switch (message) {
          case REFRESHING:
            refreshingToast = Toast.makeText(
                context, R.string.flea_refreshing, Toast.LENGTH_SHORT);
            refreshingToast.show();
            isRefreshingToastShown = true;
            break;
          case REFRESH_IS_SLOW:
            refreshSlowToast = Toast.makeText(
                context, R.string.flea_refresh_is_slow, Toast.LENGTH_SHORT);
            refreshSlowToast.show();
            break;
          case REFRESH_SUCCESS:
            refreshSuccessToast = Toast.makeText(
                context, R.string.flea_already_refreshed, Toast.LENGTH_SHORT);
            refreshSuccessToast.show();
            break;
          case REFRESH_FAILED:
            refreshFailedToast = Toast.makeText(
                context, R.string.flea_refresh_failed, Toast.LENGTH_SHORT);
            refreshFailedToast.show();
            break;
          default:
            break;
        }
        FleaMarketApplication.getUIThreadHandler().postDelayed(new Runnable() {
          @Override
          public void run() {
            cancelAllToasts();
          }
        }, TOAST_TIME);
      }
    };

    private void cancelAllToasts() {
      if (refreshingToast != null) {
        refreshingToast.cancel();
        refreshingToast = null;
      }
      if (refreshSlowToast != null) {
        refreshSlowToast.cancel();
        refreshSlowToast = null;
      }
      if (refreshSuccessToast != null) {
        refreshSuccessToast.cancel();
        refreshSuccessToast = null;
      }
      if (refreshFailedToast != null) {
        refreshFailedToast.cancel();
        refreshFailedToast = null;
      }
    }

    // Shows toast after a period of time.
    public void showToastDelayed(Message message, long delayMs) {
      if (isCurrentFragment() && isResumed()) {
        handler.sendEmptyMessageDelayed(message.ordinal(), delayMs);
      }
    }

    // Shows toast at once.
    public void showToast(Message message) {
      if (isCurrentFragment() && isResumed()) {
        // Clear all message on-the-fly
        cancelAllMessage();
        handler.sendEmptyMessage(message.ordinal());
      }
    }

    // Cancels all toast showing or will show
    public void cancelAllMessage() {
      cancelAllToasts();
      for (Message message : Message.values()) {
        handler.removeMessages(message.ordinal());
      }
    }
  }
}
