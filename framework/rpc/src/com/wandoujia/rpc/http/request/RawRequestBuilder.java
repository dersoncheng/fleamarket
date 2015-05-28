package com.wandoujia.rpc.http.request;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * Raw request builder to return request set by caller.
 *
 * liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class RawRequestBuilder implements HttpRequestBuilder {

  private HttpUriRequest request;

  public void setHttpRequest(HttpUriRequest request) {
    this.request = request;
  }

  @Override
  public HttpUriRequest build() {
    return request;
  }

  @Override
  public String getCacheKey() {
    return null;
  }
}
