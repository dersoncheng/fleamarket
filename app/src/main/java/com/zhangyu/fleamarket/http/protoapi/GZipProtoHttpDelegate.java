package com.zhangyu.fleamarket.http.protoapi;

import com.wandoujia.rpc.http.delegate.HttpDelegate;
import com.wandoujia.rpc.http.processor.Processor;
import com.wandoujia.rpc.http.processor.ProcessorConnector;
import com.wandoujia.rpc.http.request.HttpRequestBuilder;

/**
 * Created by niejunhong on 14-11-3.
 */
public class GZipProtoHttpDelegate<U extends HttpRequestBuilder, T> extends HttpDelegate<U, T> {

  private final Processor<byte[], T, ? extends Exception> contentProcessor;

  public GZipProtoHttpDelegate(U requestBuilder,
                               Processor<byte[], T, ? extends Exception> processor) {
    super(requestBuilder,
        ProcessorConnector.connect(new GZipProtoHttpResponseProcessor(), processor));
    this.contentProcessor = processor;
  }

  public Processor<byte[], T, ? extends Exception> getContentProcessor() {
    return contentProcessor;
  }
}

