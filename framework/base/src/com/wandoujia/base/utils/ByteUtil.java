/**
 * @(#)ArrayUtils.java, 2011-7-25.
 */
package com.wandoujia.base.utils;



/**
 * @author toy
 */
public class ByteUtil {

  public static byte[] getBytes(int val) {
    byte[] result = new byte[4];
    result[0] = (byte) ((val >> 24) & 0xFF);
    result[1] = (byte) ((val >> 16) & 0xFF);
    result[2] = (byte) ((val >> 8) & 0xFF);
    result[3] = (byte) (val & 0xFF);
    return result;
  }

  public static int fromBytes(byte[] bytes) {
    int value = 0;
    for (int i = 0; i < 4; i++) {
      int shift = (3 - i) * 8;
      value += (bytes[i] & 0x000000FF) << shift;
    }
    return value;
  }

  private static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  public static String bytesToHex(byte[] bytes) {
    return bytesToHex(bytes, 0, bytes.length);
  }

  private static String bytesToHex(byte bytes[], int m, int n) {
    StringBuffer stringbuffer = new StringBuffer(2 * n);
    int k = m + n;
    for (int l = m; l < k; l++) {
      appendHexPair(bytes[l], stringbuffer);
    }
    return stringbuffer.toString();
  }

  private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
    char c0 = hexDigits[(bt & 0xf0) >> 4];
    char c1 = hexDigits[bt & 0xf];
    stringbuffer.append(c0);
    stringbuffer.append(c1);
  }

}
