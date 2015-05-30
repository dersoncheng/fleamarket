package com.zhangyu.fleamarket.http.protoapi;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by niejunhong on 14-10-29.
 */

public abstract interface Serializer {
  public abstract byte[] serialize(Object paramObject);

  public abstract <T> T deserialize(Class<T> paramClass, byte[] paramArrayOfByte);

  public abstract int serialize(Object paramObject, OutputStream paramOutputStream);

  public abstract <T> T deserialize(Class<T> paramClass, InputStream paramInputStream);
}