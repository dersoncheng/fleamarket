package com.zhangyu.fleamarket.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * All fragment need to async loading data should override this. This class will make sure that
 * if you put your fragment into a ViewPager, your fragment will load data after the scrolling of
 * the ViewPager is finished rather than still scrolling, so the UI will not lag when scrolling.
 *
 * @author xubin@wandoujia.com
 */
public abstract class AsyncLoadFragment extends SherlockFragment {
  private boolean allowLoading = true;
  private boolean pendingToLoad = false;
  private View contentView;
  protected boolean isInflated;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    if (contentView == null) {
      contentView = inflater.inflate(getLayoutResId(), container, false);
    } else {
      ViewGroup parent = (ViewGroup) contentView.getParent();
      if (parent != null) {
        parent.removeView(contentView);
      }
    }
    return contentView;
  }

  @Override
  public final void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (contentView != null) {
      onInflated(contentView, savedInstanceState);
      isInflated = true;
    }
    if (!needToLoadData()) {
      return;
    }
    onPrepareLoading();
    // We need to post it to make sure scroll state of ViewPager is changed before requestLoad,
    // so we can check the param allowLoading
    contentView.post(new Runnable() {
      @Override
      public void run() {
        // Because we post this, so we need to check if this fragment is not attached
        if (!isAdded()) {
          return;
        }
        if (allowLoading) {
          onStartLoading();
        } else {
          pendingToLoad = true;
        }
      }
    });
  }

  /**
   * Called when the Fragment is inflated
   *
   * @param contentView
   * @param savedInstanceState
   */
  protected abstract void onInflated(View contentView, Bundle savedInstanceState);

  /**
   * @return the layout resource id of the fragment content
   */
  protected abstract int getLayoutResId();

  /**
   * Called before loading, subclass should override this if sometimes it needn't to load
   * data. For example, if the network is disconnected and you need network to load data,
   * then you should show network disconnect tip here and return false.
   *
   * @return true if need to load, false otherwise
   */
  protected boolean needToLoadData() {
    return true;
  }

  /**
   * Called before loading, you should show loading tips here.
   * <p>
   * <b>Do not start to load data here.</b>
   * </p>
   */
  protected abstract void onPrepareLoading();

  /**
   * Called after onPrepareLoading, you should do actual loading here, like starting the fetcher.
   */
  protected abstract void onStartLoading();

  /**
   * This will request loading data, and needToLoadData will not being checked.
   * Normally you don't need to call this, unless you need to manually refresh your UI.
   */
  protected final void requestLoad() {
    if (!isInflated || !needToLoadData()) {
      return;
    }
    onPrepareLoading();
    if (allowLoading) {
      onStartLoading();
    } else {
      pendingToLoad = true;
    }
  }

  /**
   * This is used to notify the fragment that if it should load data.
   *
   * @param allowLoading whether the fragment should start loading
   */
  final void setAllowLoading(boolean allowLoading) {
    this.allowLoading = allowLoading;
    if (allowLoading && pendingToLoad) {
      pendingToLoad = false;
      requestLoad();
    }
  }

  /**
   * Get the root view of the fragment.
   *
   * @deprecated use {@link android.support.v4.app.Fragment#getView()} instead,
   *             but need to check that getView will return null after onDetached, so be careful.
   * @return the root view
   */
  @Deprecated
  // TODO(xubin):Delete this and use {@link android.support.v4.app.Fragment#getView()} instead.
  public View getContentView() {
    return contentView;
  }

}
