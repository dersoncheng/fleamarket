package com.wandoujia.base.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;
import com.squareup.wire.Wire;

/**
 * @author xubin@wandoujia.com
 */
public class ProtobufUtils {
  public static int getFieldTag(Class<? extends Message> protoClass, String fieldName) {
    if (protoClass == null) {
      throw new RuntimeException("protoClass should not be null");
    }
    try {
      Field field = protoClass.getField(fieldName);
      ProtoField protoField = field.getAnnotation(ProtoField.class);
      if (protoField == null) {
        throw new RuntimeException("the field do not have ProtoField annotation, " +
            "field name=" + fieldName);
      }
      return protoField.tag();
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  public static int readRawVarint32(InputStream input) throws IOException {
    final int firstByte = input.read();
    if (firstByte == -1) {
      throw new IOException();
    }
    if ((firstByte & 0x80) == 0) {
      return firstByte;
    }

    int result = firstByte & 0x7f;
    int offset = 7;
    for (; offset < 32; offset += 7) {
      final int b = input.read();
      if (b == -1) {
        throw new IOException();
      }
      result |= (b & 0x7f) << offset;
      if ((b & 0x80) == 0) {
        return result;
      }
    }
    // Keep reading up to 64 bits.
    for (; offset < 64; offset += 7) {
      final int b = input.read();
      if (b == -1) {
        throw new IOException();
      }
      if ((b & 0x80) == 0) {
        return result;
      }
    }
    throw new IOException();
  }

  public static <T extends Message> T parse(InputStream input, Class<T> clazz) throws IOException {
    int size = readRawVarint32(input);
    byte[] buffer = new byte[size];
    int count = input.read(buffer);
    return new Wire().parseFrom(buffer, 0, count, clazz);
  }

  public static void writeRawVarint32(OutputStream output, int value) throws IOException {
    while (true) {
      if ((value & ~0x7F) == 0) {
        output.write((byte) value);
        return;
      } else {
        output.write((byte) ((value & 0x7F) | 0x80));
        value >>>= 7;
      }
    }
  }

  public static <T extends Message> void write(OutputStream output, T object) throws IOException {
    int size = object.getSerializedSize();
    writeRawVarint32(output, size);
    output.write(object.toByteArray());
  }
}
