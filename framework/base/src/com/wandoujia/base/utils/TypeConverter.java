package com.wandoujia.base.utils;

public class TypeConverter {
  public static byte[] int2Bytes(int i) {
    byte[] b = new byte[4];

    b[0] = (byte) (0xff & i);
    b[1] = (byte) ((0xff00 & i) >> 8);
    b[2] = (byte) ((0xff0000 & i) >> 16);
    b[3] = (byte) ((0xff000000 & i) >> 24);

    return b;
  }

  public static int bytes2Int(byte[] bytes) throws NumberFormatException {
    if (bytes.length != 4) {
      throw new NumberFormatException();
    }

    int num = bytes[0] & 0xFF;
    num |= ((bytes[1] << 8) & 0xFF00);
    num |= ((bytes[2] << 16) & 0xFF0000);
    num |= ((bytes[3] << 24) & 0xFF000000);
    return num;
  }

  public static long bytes2Long(byte[] bytes) throws NumberFormatException {
    if (bytes.length != 8) {
      throw new NumberFormatException();
    }

    long num = bytes[0];
    num |= ((long) bytes[1] << 8);
    num |= ((long) bytes[2] << 16);
    num |= ((long) bytes[3] << 24);
    num |= ((long) bytes[4] << 32);
    num |= ((long) bytes[5] << 40);
    num |= ((long) bytes[6] << 48);
    num |= ((long) bytes[7] << 56);
    return num;
  }

  public static byte[] long2Bytes(long number) {
    // TODO
    return null;
  }

  public static String bytes2str(byte[] bytes) {
    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < bytes.length; ++i) {
      int v = bytes[i];
      if (v < 0) {
        v += 256;
      }
      buffer.append((char) v);
    }

    return buffer.toString();
  }

  public static byte[] str2bytes(String str) {
    char[] chars = str.toCharArray();
    byte[] bytes = new byte[chars.length];

    for (int i = 0; i < chars.length; i++) {
      int v = chars[i];
      if (v >= 128) {
        v -= 256;
      }
      bytes[i] = (byte) v;
    }

    return bytes;
  }

}
