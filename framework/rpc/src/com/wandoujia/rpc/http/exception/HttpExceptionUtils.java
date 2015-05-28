package com.wandoujia.rpc.http.exception;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpStatus;

import android.text.TextUtils;

import com.wandoujia.gson.Gson;
import com.wandoujia.gson.JsonSyntaxException;
import com.wandoujia.gson.reflect.TypeToken;

/**
 * @author zhulantian@wandoujia.com (Lantian Zhu)
 */
public class HttpExceptionUtils {

  private static final int STATUS_CODE_SERVER_UNAVAILABLE = 259;
  private static final String KEY_SERVER_UNAVAILABLE_INFO = "info";

  /* 2xx,3xx,4xx, >510 && can retry */
  private static final Set<Integer> STATUS_CODE_WHITE_LIST;
  /* 500 - 510 && can not retry */
  private static final Set<Integer> STATUS_CODE_BLACK_LIST;

  static {
    STATUS_CODE_WHITE_LIST = new HashSet<Integer>();
    STATUS_CODE_BLACK_LIST = new HashSet<Integer>();
  }

  public static boolean canRetry(Throwable t) {
    t = getRealCause(t);
    if (t == null) {
      return false; // not http exception, not retry
    }
    if (t instanceof HttpException) {
      int statusCode = ((HttpException) t).getStatusCode();
      if (STATUS_CODE_WHITE_LIST.contains(statusCode)) { // in white names
        return true;
      }
      if (STATUS_CODE_BLACK_LIST.contains(statusCode)) { // in black name
        return false;
      }
      return statusCode >= 500 && statusCode <= 510; // retry when code [500, 510] (default)
    }
    return false;
  }

  public static String getMessage(Throwable t, String defaultVal) {
    t = getRealCause(t);
    if (t == null) {
      return defaultVal;
    }
    if (t instanceof HttpException) {
      return getHttpExceptionMessage((HttpException) t, defaultVal);
    }
    return defaultVal;
  }

  public static boolean isServerUnavailable(Throwable t) {
    t = getRealCause(t);
    if (t == null) {
      return false;
    }
    if (t instanceof HttpException) {
      return ((HttpException) t).getStatusCode() == STATUS_CODE_SERVER_UNAVAILABLE;
    }
    return false;
  }

  public static boolean is404NotFound(Throwable t) {
    t = getRealCause(t);
    if (t == null) {
      return false;
    }
    if (t instanceof HttpException) {
      return ((HttpException) t).getStatusCode() == HttpStatus.SC_NOT_FOUND;
    }
    return false;
  }

  private static String getHttpExceptionMessage(HttpException e, String defaultVal) {
    int statusCode = e.getStatusCode();
    String message = null;
    switch (statusCode) {
      case STATUS_CODE_SERVER_UNAVAILABLE:
        message = getServerUnavailableMessage(e.getMessage(), defaultVal);
        break;
    }
    return TextUtils.isEmpty(message) ? defaultVal : message;
  }

  private static String getServerUnavailableMessage(String originMessage, String defaultVal) {
    String message;
    try {
      HashMap<String, String> map = new Gson().fromJson(originMessage,
          new TypeToken<HashMap<String, String>>() {}.getType());
      message = map.get(KEY_SERVER_UNAVAILABLE_INFO);
      if (TextUtils.isEmpty(message)) {
        message = defaultVal;
      }
    } catch (JsonSyntaxException e) {
      message = defaultVal;
    }
    return message;
  }

  private static Throwable getRealCause(Throwable t) {
    while (t != null && t instanceof ExecutionException) {
      t = t.getCause();
    }
    return t;
  }
}
