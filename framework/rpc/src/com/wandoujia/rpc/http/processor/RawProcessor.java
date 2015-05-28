package com.wandoujia.rpc.http.processor;

/**
 * liuchunyu@wandoujia.com (Chunyu Liu)
 *
 * Raw processor to return input directly.
 */
public class RawProcessor<T> implements Processor<T, T, Exception> {

  @Override
  public T process(T input) throws Exception {
    return input;
  }
}
