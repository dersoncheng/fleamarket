package com.wandoujia.rpc.http.processor;

import java.util.concurrent.ExecutionException;

/**
 * Connector to connect processors.
 *
 * @author liuchunyu@wandouaji.com (Chunyu Liu)
 *
 * @param <U> input type
 * @param <V> intermediate type
 * @param <T> output type
 */
public class ProcessorConnector<U, V, T> implements Processor<U, T, ExecutionException> {

  private final Processor<U, V, ? extends Exception> upstreamProcessor;
  private final Processor<V, T, ? extends Exception> downstreamProcessor;

  public static <U, V, T> ProcessorConnector<U, V, T> connect(
      Processor<U, V, ? extends Exception> upstreamProcessor,
      Processor<V, T, ? extends Exception> downstreamProcessor) {
    return new ProcessorConnector<U, V, T>(upstreamProcessor, downstreamProcessor);
  }

  private ProcessorConnector(Processor<U, V, ? extends Exception> upstreamProcessor,
                             Processor<V, T, ? extends Exception> downstreamProcessor) {
    this.upstreamProcessor = upstreamProcessor;
    this.downstreamProcessor = downstreamProcessor;
  }

  @Override
  public T process(U input) throws ExecutionException {
    try {
      return downstreamProcessor.process(upstreamProcessor.process(input));
    } catch (Exception e) {
      throw new ExecutionException(e);
    }
  }

  /**
   * Connects to another processor.
   *
   * @param downstreamProcessor downstream processor
   * @return a new ProcessorConnector
   */
  public <R> ProcessorConnector<U, T, R> connect(
      Processor<T, R, ? extends Exception> downstreamProcessor) {
    return new ProcessorConnector<U, T, R>(this, downstreamProcessor);
  }
}
