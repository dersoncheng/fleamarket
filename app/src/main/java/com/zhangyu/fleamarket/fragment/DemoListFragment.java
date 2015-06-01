package com.zhangyu.fleamarket.fragment;

import android.os.Bundle;
import android.view.ViewGroup;

import com.wandoujia.base.utils.NetworkUtil;
import com.wandoujia.mvc.BaseController;
import com.wandoujia.mvc.BaseView;
import com.zhangyu.fleamarket.adapter.BaseCardAdapter;
import com.zhangyu.fleamarket.adapter.DataAdapter;
import com.zhangyu.fleamarket.controller.DemoCardController;
import com.zhangyu.fleamarket.http.fetcher.BaseFetcher;
import com.zhangyu.fleamarket.http.fetcher.DemoListFetcher;
import com.zhangyu.fleamarket.http.fetcher.ModelFetcherWrapper;
import com.zhangyu.fleamarket.http.protoapi.DemoCardProtoModelConverter;
import com.zhangyu.fleamarket.model.DemoCardModel;
import com.zhangyu.fleamarket.view.ContentCardView;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class DemoListFragment
  extends NetworkListAsyncloadFragment<DemoCardModel> {

  private static final String KEY_SPECIAL_ID = "special_id";
  private static final int WIFI_PAGE_SIZE = 10;
  private static final int MOBILE_PAGE_SIZE = 5;

  private static long specialId;

  public static DemoListFragment newInstance(long specialId) {
    DemoListFragment demoListFragment = new DemoListFragment();
    Bundle bundle = new Bundle();
    bundle.putLong(KEY_SPECIAL_ID, specialId);
    demoListFragment.setArguments(bundle);
    return demoListFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null && getArguments().containsKey(KEY_SPECIAL_ID)) {
      this.specialId = getArguments().getLong(KEY_SPECIAL_ID);
    }
  }

  @Override
  protected int getFirstFetchSize() {
    if (NetworkUtil.isWifiConnected(getActivity())) {
      return WIFI_PAGE_SIZE;
    }
    return MOBILE_PAGE_SIZE;
  }

  @Override
  protected int getPageSize() {
    if (NetworkUtil.isWifiConnected(getActivity())) {
      return WIFI_PAGE_SIZE;
    }
    return MOBILE_PAGE_SIZE;
  }

  @Override
  protected BaseFetcher<DemoCardModel> newFetcher() {
    return new ModelFetcherWrapper(new DemoListFetcher(specialId),
      new DemoCardProtoModelConverter());
  }

  @Override
  protected DataAdapter<DemoCardModel> newContentAdapter() {
    return new BaseCardAdapter<DemoCardModel>() {
      @Override
      protected BaseView newView(int position, DemoCardModel Model, ViewGroup parent) {
        ContentCardView contentCardView = ContentCardView.newInstanceShortVideo(parent);
        return contentCardView;
      }

      @Override
      protected BaseController newController(int position, DemoCardModel model) {
        return new DemoCardController();
      }

      @Override
      protected boolean needLogCardShow() {
        return true;
      }
    };
  }

  @Override
  protected void onNoFetchResult() {

  }
}
