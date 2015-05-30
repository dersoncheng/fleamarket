package com.zhangyu.fleamarket.http.protoapi;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by niejunhong on 14-10-29.
 */
public class FleaMarketProtobufSerializer implements Serializer {

  private Map<Class, Schema> cachedSchemas = new HashMap<Class, Schema>();

  private LinkedBuffer linkedBuffer = LinkedBuffer.allocate(1024 * 128);

  @Override
  public byte[] serialize(Object obj) {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      serialize(obj, output);

      return output.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      try {
        output.close();
      } catch (IOException e) {
      }
    }
  }

  @Override
  public <T> T deserialize(Class<T> paramClass, byte[] paramArrayOfByte) {
    ByteArrayInputStream input = new ByteArrayInputStream(paramArrayOfByte);
    try {
      return deserialize(paramClass, input);
    } finally {
      try {
        input.close();
      } catch (IOException e) {
      }
    }
  }

  private Schema getSchema(Class klass) {

    Schema schema = cachedSchemas.get(klass);
    if (schema == null) {
      if (Schema.class.isAssignableFrom(klass)) {
        try {
          schema = (Schema) klass.newInstance();
        } catch (Exception e) {
          throw new ApiException(ApiErrorCodes.SYSTEM_ERROR.getValue(), "instantiate class failed:" + e.getMessage(), e);
        }
      } else {
        throw new ApiException(ApiErrorCodes.SYSTEM_ERROR.getValue(), "bad class:" + klass.getCanonicalName());
      }
      cachedSchemas.put(klass, schema);
    }

    return schema;
  }

  @Override
  public int serialize(Object obj, OutputStream outputStream) {
    synchronized (linkedBuffer) {
      linkedBuffer.clear();
      Schema schema = getSchema(obj.getClass());
      try {
        return ProtostuffIOUtil.writeTo(outputStream, obj,
          schema, linkedBuffer);
      } catch (Exception e) {
        throw new ApiException(ApiErrorCodes.SYSTEM_ERROR.getValue(), "serialize error:" + e.getMessage(), e);
      }
    }
  }

  @Override
  public <T> T deserialize(Class<T> paramClass, InputStream inputStream) {
    synchronized (linkedBuffer) {
      linkedBuffer.clear();
      Schema schema = getSchema(paramClass);
      try {
        T obj = paramClass.newInstance();
        ProtostuffIOUtil.mergeFrom(inputStream, obj,
          schema, linkedBuffer);
        return obj;
      } catch (Exception e) {
        throw new ApiException(ApiErrorCodes.SYSTEM_ERROR.getValue(), "deserialize error:" + e.getMessage(), e);
      }
    }
  }
}
