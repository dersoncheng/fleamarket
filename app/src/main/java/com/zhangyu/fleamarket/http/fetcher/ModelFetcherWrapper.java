package com.zhangyu.fleamarket.http.fetcher;

import com.zhangyu.fleamarket.model.FetcherModelConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class ModelFetcherWrapper<T, M> extends BaseFetcher<M> {

  private BaseFetcher<T> fetcher;
  private FetcherModelConverter<T, M> converter;

  public ModelFetcherWrapper(BaseFetcher<T> fetcher, FetcherModelConverter<T, M> converter) {
    this.fetcher = fetcher;
    this.converter = converter;
  }

  @Override
  protected String getCacheKey() {
    return converter.getIdentity() + '*' + fetcher.getCacheKey();
  }

  @Override
  public void clearCache() {
    fetcher.clearCache();
  }

  @Override
  protected List<M> doFetch(int start, int size) throws ExecutionException {
    return convert(fetcher.doFetch(start, size));
  }

  @Override
  protected List<M> fetchHttpData(int start, int size) throws ExecutionException {
    return convert(fetcher.fetchHttpData(start, size));
  }

  @Override
  protected ResultList<M> fetchCacheData(int start, int size) {
    ResultList<T> cacheData = fetcher.fetchCacheData(start, size);
    if (cacheData == null) {
      return null;
    }
    List<M> result = convert(cacheData.data);
    return new ResultList<M>(result, cacheData.isTimeout);
  }

  private List<M> convert(List<T> data) {
    if (data == null) {
      return null;
    }
    List<M> result = new ArrayList<M>();
    for (T t : data) {
      result.add(converter.convert(t));
    }
    return result;
  }

}
