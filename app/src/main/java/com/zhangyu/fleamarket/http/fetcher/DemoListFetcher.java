package com.zhangyu.fleamarket.http.fetcher;

import com.wandoujia.em.common.proto.GetVideoTopListResultResp;
import com.wandoujia.em.common.proto.Video;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.http.delegate.DemoListDelegate;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class DemoListFetcher extends BaseFetcher<Video> {

  private Long specialId;
  private int nextoffset = -1;

  public DemoListFetcher(Long specialId) {
    this.specialId = specialId;
  }

  @Override
  protected List<Video> fetchHttpData(int start, int size) throws ExecutionException {
    DemoListDelegate delegate = new DemoListDelegate();
    if (nextoffset == -1) {
      delegate.getRequestBuilder().setSpecialId(specialId).setStart(start).setNumber(size);
    } else {
      delegate.getRequestBuilder().setSpecialId(specialId).setStart(nextoffset).setNumber(size);
    }
    GetVideoTopListResultResp videoListResult = FleaMarketApplication.getDataApi().execute(delegate);
    if (videoListResult != null && videoListResult.getNextOffset() != null) {
      nextoffset = videoListResult.getNextOffset();
      return videoListResult.getVideosList();
    }
    return null;
  }

  @Override
  protected String getCacheKey() {
    return DemoListFetcher.class.getName() + specialId;
  }
}
