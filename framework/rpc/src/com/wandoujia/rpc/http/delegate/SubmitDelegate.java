package com.wandoujia.rpc.http.delegate;

import com.wandoujia.rpc.http.processor.EmptyResponseProcessor;
import com.wandoujia.rpc.http.request.HttpRequestBuilder;

/**
 * Delegate to submit request.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 *
 * @param <U> request builder
 */
public class SubmitDelegate<U extends HttpRequestBuilder> extends HttpDelegate<U, Void> {

  public SubmitDelegate(U requestBuilder) {
    super(requestBuilder, new EmptyResponseProcessor());
  }

}
