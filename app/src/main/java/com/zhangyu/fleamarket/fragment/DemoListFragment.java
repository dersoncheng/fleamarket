package com.zhangyu.fleamarket.fragment;

import com.zhangyu.fleamarket.adapter.DataAdapter;
import com.zhangyu.fleamarket.http.fetcher.BaseFetcher;
import com.zhangyu.fleamarket.model.DemoCardModel;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class DemoListFragment
  extends NetworkListAsyncloadFragment<DemoCardModel> {
  @Override
  protected BaseFetcher<DemoCardModel> newFetcher() {
    return null;
  }

  @Override
  protected DataAdapter<DemoCardModel> newContentAdapter() {
    return null;
  }

  @Override
  protected void onNoFetchResult() {

  }
}
