package com.zhangyu.fleamarket.http.fetcher;

import java.util.concurrent.ExecutionException;

/**
 * Usage: Helps ui to show fetch data.
 * 
 * @author zhulantian@wandoujia.com.
 */
public class FetchHelper<T> {
  private static final int DEFAULT_FIRST_SIZE = 40;
  private static final int DEFAULT_PAGE_SIZE = 20;

  private final int firstSize;
  private final int pageSize;
  protected int currentIndex;
  private final boolean twoPhraseLoading;
  private boolean hasMore;

  private final BaseFetcher<T> fetcher;
  private final BaseFetcher.Callback<T> callback;
  private final BaseFetcher.Callback<T> callbackProxy;

  public FetchHelper(BaseFetcher<T> fetcher, BaseFetcher.Callback<T> callback) {
    this(fetcher, callback, DEFAULT_FIRST_SIZE, DEFAULT_PAGE_SIZE, false);
  }

  /**
   * Constructor. If {@code twoPhraseLoading == true}, it will fetch data from cache first, and then
   * network.
   */
  public FetchHelper(BaseFetcher<T> fetcher, BaseFetcher.Callback<T> callback,
                     boolean twoPhraseLoading) {
    this(fetcher, callback, DEFAULT_FIRST_SIZE, DEFAULT_PAGE_SIZE, twoPhraseLoading);
  }

  public FetchHelper(BaseFetcher<T> fetcher, BaseFetcher.Callback<T> callback, int firstSize,
                     boolean twoPhraseLoading) {
    this(fetcher, callback, firstSize, DEFAULT_PAGE_SIZE, twoPhraseLoading);
  }

  public FetchHelper(BaseFetcher<T> fetcher, BaseFetcher.Callback<T> callback,
                     int firstSize, int pageSize) {
    this(fetcher, callback, firstSize, pageSize, false);
  }

  public FetchHelper(BaseFetcher<T> fetcher, BaseFetcher.Callback<T> callback,
                     int firstSize, int pageSize, boolean twoPhraseLoading) {
    this.fetcher = fetcher;
    this.callback = callback;
    this.firstSize = firstSize;
    this.pageSize = pageSize;
    this.callbackProxy = new CustomCallback();
    this.hasMore = true;
    this.twoPhraseLoading = twoPhraseLoading;
  }

  public void fetch() {
    final int size = currentIndex != 0 ? currentIndex : firstSize;
    fetcher.fetch(0, size, callbackProxy, twoPhraseLoading);
  }

  public void fetchMore() {
    int size = currentIndex == 0 ? firstSize : pageSize;
    fetcher.fetch(currentIndex, size, callbackProxy);

  }

  /**
   * Move current index to a new position.
   * 
   * @param pos new position.
   */
  public void moveToPosition(int pos) {
    this.currentIndex = pos;
  }

  /**
   * fetch more data.
   * 
   * @param size the size want to fetch, begin with the last fetched num.
   */
  public void fetchMore(int size) {
    fetcher.fetch(currentIndex, size, callbackProxy);
  }


  public BaseFetcher<T> getFetcher() {
    return fetcher;
  }

  public boolean hasMore() {
    return hasMore;
  }

  private class CustomCallback implements BaseFetcher.Callback<T> {
    @Override
    public void onFetched(int start, int size, BaseFetcher.ResultList<T> result) {
      hasMore = (result.data != null && !result.data.isEmpty());
      currentIndex = start + result.data.size();
      callback.onFetched(start, size, result);
    }

    @Override
    public void onFailed(int start, ExecutionException e) {
      callback.onFailed(start, e);
    }
  }

}
