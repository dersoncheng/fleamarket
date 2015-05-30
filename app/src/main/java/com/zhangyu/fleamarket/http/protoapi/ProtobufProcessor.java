package com.zhangyu.fleamarket.http.protoapi;

import com.wandoujia.rpc.http.exception.ContentParseException;
import com.wandoujia.rpc.http.processor.Processor;

import java.lang.reflect.ParameterizedType;

/**
 * Created by niejunhong on 14-10-29.
 */
public class ProtobufProcessor<T> implements Processor<byte[], T, ContentParseException> {

  protected static Serializer sSerializer = new FleaMarketProtobufSerializer();

  @Override
  public T process(byte[] input) throws ContentParseException {
    try {
      Class<T> entityClass;
      entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
          .getActualTypeArguments()[0];
      T t = sSerializer.deserialize(entityClass, input);
      return t;
    } catch (Exception e) {
      throw new ContentParseException(e.getMessage(), new String(input));
    }
  }
}

