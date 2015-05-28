package com.wandoujia.rpc.http.util;

import java.io.IOException;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;

import com.wandoujia.base.utils.CipherUtil;
import com.wandoujia.base.utils.MD5Utils;
import com.wandoujia.rpc.http.request.AbstractHttpRequestBuilder;

/**
 * Utility to generate authorizing params
 *
 * @author yilin
 *
 */
public class PhoenixAuthorizeUtil {

  private static final String PARAM_ID = "id";
  private static final String PARAM_TOKEN = "token";
  private static final String PARAM_TIMESTAMP = "timestamp";

  private PhoenixAuthorizeUtil() {}

  /**
   * Append authorizing params to origin params.
   *
   * @param originParams
   */
  public static void appendAuthorizeParams(
      AbstractHttpRequestBuilder.Params originParams, Context context) {
    long timestamp = System.currentTimeMillis();
    originParams.put(PARAM_ID, CipherUtil.getAndroidId(context.getApplicationContext()), false);
    originParams.put(PARAM_TOKEN, generateToken(timestamp, context), false);
    originParams.put(PARAM_TIMESTAMP, String.valueOf(timestamp), false);
  }

  /**
   * Append authorizing params to origin url.
   *
   * @param originUrl
   * @return modified url
   */
  public static String appendAuthorizeParam(String originUrl, Context context) {
    long timestamp = System.currentTimeMillis();
    Builder builder = Uri.parse(originUrl).buildUpon();
    builder.appendQueryParameter(PARAM_ID,
        CipherUtil.getAndroidId(context.getApplicationContext()));
    builder.appendQueryParameter(PARAM_TOKEN, generateToken(timestamp, context));
    builder.appendQueryParameter(PARAM_TIMESTAMP, String.valueOf(timestamp));
    return builder.toString();
  }

  /**
   * Generate token using timestamp.
   *
   * @param timestamp
   * @return
   */
  public static String generateToken(long timestamp, Context context) {
    try {
      StringBuilder builder = new StringBuilder();
      builder.append(CipherUtil.getAndroidId(context.getApplicationContext()));
      builder.append(CipherUtil.getAuthKey(context.getApplicationContext()));
      builder.append(timestamp);
      String token = MD5Utils.md5Digest(builder.toString());
      return token;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (UnsatisfiedLinkError e) {
      e.printStackTrace();
    }
    return null;
  }
}
