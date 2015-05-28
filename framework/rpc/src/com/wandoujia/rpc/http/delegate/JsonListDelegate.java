package com.wandoujia.rpc.http.delegate;

import java.util.List;

import com.wandoujia.rpc.http.delegate.GZipHttpDelegate;
import com.wandoujia.rpc.http.processor.Processor;
import com.wandoujia.rpc.http.request.HttpRequestBuilder;

/**
 * @author liujinyu@wandoujia.com (Jinyu Liu)
 */
public class JsonListDelegate<U extends HttpRequestBuilder, T>
    extends GZipHttpDelegate<U, List<T>> {

  public JsonListDelegate(U builder, Processor<String, List<T>, ? extends Exception> processor) {
    super(builder, processor);
  }
}
