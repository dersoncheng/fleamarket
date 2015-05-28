package com.wandoujia.base.utils;


import java.util.List;

public class StringUtil {

  public static String toUpperCase(String string) {
    int length = string.length();
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      builder.append(toUpperCase(string.charAt(i)));
    }
    return builder.toString();
  }

  public static char toUpperCase(char c) {
    return isLowerCase(c) ? (char) (c & 0x5f) : c;
  }

  public static boolean isLowerCase(char c) {
    return (c >= 'a') && (c <= 'z');
  }

  public static String join(List<String> stringList, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < stringList.size(); i++) {
      String s = stringList.get(i);
      if (s != null) {
        sb.append(s);
      }
      if (i < stringList.size() - 1) {
        sb.append(separator);
      }
    }

    return sb.toString();
  }
}
