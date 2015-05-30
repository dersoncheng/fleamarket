package com.zhangyu.fleamarket.http;

import com.wandoujia.rpc.http.request.AbstractHttpRequestBuilder;

/**
 * Request builder to get single start and number info.
 *
 * @author luyinchen@wandoujia.com (Yinchen Lu)
 */
public abstract class PaginationRequestBuilder extends FleaMarketAuthorizedRequestBuilder {

  private static final String START_PARAM = "start";
  private static final String NUM_PARAM = "max";
  private static final int DEFAULT_NUM_PER_PAGE = 20;

  private int start = 0;
  private int number = DEFAULT_NUM_PER_PAGE;

  public PaginationRequestBuilder setStart(int start) {
    this.start = start;
    return this;
  }

  public PaginationRequestBuilder setNumber(int number) {
    this.number = number;
    return this;
  }

  @Override
  protected void setParams(AbstractHttpRequestBuilder.Params params) {
    super.setParams(params);
    params.put(START_PARAM, String.valueOf(start));
    params.put(NUM_PARAM, String.valueOf(number));
  }

  @Override
  protected String getUrl() {
    return null;
  }
}
