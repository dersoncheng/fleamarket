package com.wandoujia.base.utils;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yingyixu@wandoujia.com (Yingyi Xu)
 */
public class FreeHttpUtils {
  public static final String FREE_HOST_NAME = "vip.wandoujia.com";
  public static final HttpHost FREE_HTTP_HOST = new HttpHost(FREE_HOST_NAME, -1, "http");

  private static final String PARAM_URL = "url";
  private static final String FREE_URL = "http://vip.wandoujia.com/proxy";
  private static final String FREE_URL_EXPRESSION = FREE_URL + "\\?url=[^\\s]*";

  private static List<NameValuePair> freeParams;
  private static boolean inFreeMode = false;

  private FreeHttpUtils() {}

  public static boolean isInFreeMode() {
    return inFreeMode;
  }

  public static void setInFreeMode(boolean in) {
    inFreeMode = in;
  }

  public static void setFreeHttpParams(List<NameValuePair> params) {
    freeParams = params;
  }

  public static HttpRequest buildFreeRequest(HttpRequest request) {
    if (request instanceof HttpRequestBase) {
      HttpRequestBase httpRequestBase = (HttpRequestBase) request;
      httpRequestBase.setURI(generateFreeURI(httpRequestBase.getURI()));
    }
    return request;
  }

  public static boolean isProxyUrl(String url) {
    return url.matches(FREE_URL_EXPRESSION);
  }

  private static URI generateFreeURI(URI originURI) {
    if (originURI == null || isProxyUrl(originURI.toString())) {
      return originURI;
    }
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair(PARAM_URL, originURI.toString()));
    params.addAll(freeParams);
    return URI.create(FREE_URL + "?" + URLEncodedUtils.format(params, "utf-8"));
  }

  public static URL buildFreeURLIfNeed(URL originURL) {
    if (isInFreeMode()) {
      try {
        return generateFreeURI(originURL.toURI()).toURL();
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      return originURL;
    } else {
      return originURL;
    }
  }

  public static URI buildFreeURIIfNeed(URI originURI) {
    if (isInFreeMode()) {
      return generateFreeURI(originURI);
    } else {
      return originURI;
    }
  }

  public static String buildFreeURLIfNeed(String url) {
    if (isInFreeMode()) {
      return generateFreeURI(URI.create(url)).toString();
    }
    return url;
  }

  public static String getMimeType(String url) {
    String type = null;
    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
    if (extension != null) {
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      type = mime.getMimeTypeFromExtension(extension);
    }
    return type;
  }
}
