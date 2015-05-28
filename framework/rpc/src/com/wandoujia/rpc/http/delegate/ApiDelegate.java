package com.wandoujia.rpc.http.delegate;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Class to generate http request, and process http response.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 *
 * @param <T> result type
 * @param <E> exception type
 */
public interface ApiDelegate<T, E extends Exception> {

  /**
   * Creates a http request.
   *
   * @return HttpUriRequest
   */
  HttpUriRequest getHttpRequest();

  /**
   * Processes http response.
   *
   * @param httpResponse HttpResponse
   * @return process result
   * @throws E exception
   */
  T processResponse(HttpResponse httpResponse) throws E;
}
