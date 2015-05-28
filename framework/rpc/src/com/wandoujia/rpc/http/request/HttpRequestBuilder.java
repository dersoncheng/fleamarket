package com.wandoujia.rpc.http.request;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * A builder interface which can get {@link org.apache.http.client.methods.HttpUriRequest} from.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public interface HttpRequestBuilder {

  /**
   * Builds a HttpUriRequest.
   *
   * @return HttpUriRequest. Returns null if error happens
   */
  HttpUriRequest build();

  /**
   * Returns cache key if the response is cacheable. Otherwise, return null.
   *
   * @return
   */
  String getCacheKey();
}
