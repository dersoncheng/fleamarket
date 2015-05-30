package com.zhangyu.fleamarket.http.request;

import com.wandoujia.rpc.http.request.AbstractHttpRequestBuilder;
import com.zhangyu.fleamarket.configs.ApiConfig;
import com.zhangyu.fleamarket.http.PaginationRequestBuilder;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class DemoListRequestBuilder extends PaginationRequestBuilder {

  public static final String URL = ApiConfig.API_HOST + "/v2/video/MV/special/detail";

  private static final String ID = "id";

  private Long specialId;

  public DemoListRequestBuilder setSpecialId(Long specialId) {
    this.specialId = specialId;
    return this;
  }

  @Override
  protected void setParams(AbstractHttpRequestBuilder.Params params) {
    super.setParams(params);
    if (specialId != null) {
      params.put(ID, String.valueOf(specialId));
    }
  }

  @Override
  protected String getUrl() {
    return URL;
  }
}
