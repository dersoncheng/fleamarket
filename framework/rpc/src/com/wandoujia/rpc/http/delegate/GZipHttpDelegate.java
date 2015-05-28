package com.wandoujia.rpc.http.delegate;

import com.wandoujia.rpc.http.processor.GZipHttpResponseProcessor;
import com.wandoujia.rpc.http.processor.Processor;
import com.wandoujia.rpc.http.processor.ProcessorConnector;
import com.wandoujia.rpc.http.request.HttpRequestBuilder;

/**
 * Delegate which can process GZip compressed http response.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 *
 * @param <U> HttpRequestBuilder type
 * @param <T> output type
 */
public class GZipHttpDelegate<U extends HttpRequestBuilder, T> extends HttpDelegate<U, T> {

  private final Processor<String, T, ? extends Exception> contentProcessor;

  public GZipHttpDelegate(U requestBuilder,
      Processor<String, T, ? extends Exception> processor) {
    super(requestBuilder,
      ProcessorConnector.connect(new GZipHttpResponseProcessor(), processor));
    this.contentProcessor = processor;
  }

  public Processor<String, T, ? extends Exception> getContentProcessor() {
    return contentProcessor;
  }
}
