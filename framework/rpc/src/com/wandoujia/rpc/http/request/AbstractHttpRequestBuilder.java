package com.wandoujia.rpc.http.request;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;
import android.util.Log;

import com.wandoujia.base.utils.CipherUtil;
import com.wandoujia.base.utils.GZipUtil;
import com.wandoujia.base.utils.IOUtils;
import com.wandoujia.base.utils.StringUtil;
import com.wandoujia.rpc.http.provider.CookieProvider;

/**
 * Abstract base class to generate http request.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public abstract class AbstractHttpRequestBuilder implements HttpRequestBuilder {

  private static final String TAG = AbstractHttpRequestBuilder.class.getSimpleName();

  public enum Method {
    GET, POST
  }

  private static final String ACCEPT_ENCODING = "Accept-Encoding";
  private static final String GZIP_ENCODING = "gzip, deflate";
  private static final String COOKIE = "Cookie";
  private static final String CONTENT_ENCODING = "Content-Encoding";
  private static final String GZIP = "gzip";
  private final CookieProvider cookieProvider;
  private boolean attachDefaultCookie;
  private String additionalParams;
  private Method method = Method.GET;
  private boolean isCallingParams = false;
  private boolean isCallingHeaders = false;
  private boolean compress = false;
  private boolean encrypt = false;
  private boolean paramsInUrl = false;
  private Context context;

  public AbstractHttpRequestBuilder() {
    this.cookieProvider = null;
  }

  public AbstractHttpRequestBuilder(CookieProvider cookieProvider) {
    this.cookieProvider = cookieProvider;
  }

  public AbstractHttpRequestBuilder setAttachDefaultCookie(boolean attachDefaultCookie) {
    this.attachDefaultCookie = attachDefaultCookie;
    return this;
  }

  public AbstractHttpRequestBuilder setMethod(Method method) {
    this.method = method;
    return this;
  }

  public AbstractHttpRequestBuilder setAdditionalParams(String additionalParams) {
    this.additionalParams = additionalParams;
    return this;
  }

  public AbstractHttpRequestBuilder enableCompressRequestContent() {
    this.compress = true;
    if (encrypt && compress) {
      throw new IllegalStateException("Already encrypt, could not compress");
    }
    return this;
  }

  protected void setCompress(boolean compress) {
    this.compress = compress;
  }

  public AbstractHttpRequestBuilder enableParamsInUrl() {
    this.paramsInUrl = true;
    return this;
  }

  protected void setParamsInUrl(boolean paramsInUrl) {
    this.paramsInUrl = paramsInUrl;
  }

  public AbstractHttpRequestBuilder enableEncryptRequestContent(Context context) {
    this.encrypt = true;
    this.context = context.getApplicationContext();
    if (encrypt && compress) {
      throw new IllegalStateException("Already compress, could not encrypt");
    }
    return this;
  }

  @Override
  public final HttpUriRequest build() {
    String url = getUrl();
    if (url == null) {
      return null;
    }
    Params params = new Params();
    setParams(params);
    HttpUriRequest request = null;
    switch (method) {
      case GET:
        Builder uriBuilder = Uri.parse(url).buildUpon();
        for (Map.Entry<String, Value> entry : params.params.entrySet()) {
          if (entry.getValue() == null) {
            Log.w(TAG, entry.getKey() + " has null value");
          } else {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue().value);
          }
        }
        url = createUrl(uriBuilder.toString(), additionalParams);
        HttpGet getRequest = new HttpGet(url);
        if (getTimeout() > 0) {
          getRequest.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, getTimeout());
          getRequest.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
              getTimeout());
        }
        request = getRequest;
        break;
      case POST:
        url = createUrl(url, additionalParams);
        if (paramsInUrl) {
          url = createUrl(url, params.toString());
          params.clear();
        }

        HttpPost postRequest = new HttpPost(url);
        try {
          HttpEntity entity = genEntity(params);
          if (compress) {
            byte[] contentBytes = IOUtils.readBytes(entity.getContent());
            byte[] zipedBytes = GZipUtil.zipBytes(contentBytes);
            entity = new ByteArrayEntity(zipedBytes);
          }
          if (encrypt) {
            byte[] contentBytes = IOUtils.readBytes(entity.getContent());
            byte[] encryptBytes = CipherUtil.encrypt(contentBytes, CipherUtil.getAESKey(context));
            entity = new ByteArrayEntity(encryptBytes);
          }
          postRequest.setEntity(entity);
        } catch (UnsupportedEncodingException exception) {
          exception.printStackTrace();
          return null;
        } catch (IllegalStateException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (GeneralSecurityException e) {
          e.printStackTrace();
        }
        request = postRequest;
        break;
      default:
        break;
    }
    if (request != null) {
      Params headers = new Params();
      setHeaders(headers);
      for (Map.Entry<String, Value> entry : headers.params.entrySet()) {
        request.setHeader(entry.getKey(), entry.getValue().value);
      }
    }

    return request;
  }


  protected HttpEntity genEntity(Params params) throws UnsupportedEncodingException {
    HttpEntity entity = new UrlEncodedFormEntity(createNamePairs(params), HTTP.UTF_8);
    return entity;
  }

  @Override
  public String getCacheKey() {
    StringBuilder builder = new StringBuilder(method.name()).append("_")
        .append(getUrl()).append("?");
    Params params = new Params();
    setParams(params);
    generateCacheKey(builder, params);
    Params headers = new Params();
    setHeaders(headers);
    generateCacheKey(builder, headers);
    return builder.toString();
  }

  private static void generateCacheKey(StringBuilder stringBuilder, Params params) {
    for (Map.Entry<String, Value> entry : params.params.entrySet()) {
      if (entry.getValue().isCacheableParam) {
        stringBuilder.append(entry.getKey()).append("=").append(entry.getValue().value).append("&");
      }
    }
  }

  /**
   * Gets the url of request.
   *
   * @return url, e.x a request like: http://www.company.com/download?key1=value1&key2=value2
   *         the url is http://www.company.com/download. Can be null, which means error happens
   */
  protected abstract String getUrl();

  /**
   * Set request parameters in format of key-value in map.
   * NOTE: Sub-class should call super.setParams() if it overrides this function
   *
   * @param params key-value map
   */
  protected void setParams(Params params) {
    isCallingParams = true;
  }

  /**
   * Gets the timeout of request, in ms.
   *
   * @return int
   */
  protected int getTimeout() {
    return -1;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (!isCallingParams) {
      throw new IllegalStateException(
          getClass().getSimpleName() + ".setParams() must call super.setParams()");
    }
    if (!isCallingHeaders) {
      throw new IllegalStateException(
          getClass().getSimpleName() + ".setHeaders() must call super.setHeaders()");
    }
  }

  /**
   * Gets request headers. Any header returned by sub-class will override the same header value
   * in this base class.
   *
   * @return request header map, can be null
   * @param headers
   */
  protected void setHeaders(Params headers) {
    headers.put(ACCEPT_ENCODING, GZIP_ENCODING, false);
    if (attachDefaultCookie && cookieProvider != null) {
      headers.put(COOKIE, cookieProvider.getDefaultCookie());
    }
    if (compress) {
      headers.put(CONTENT_ENCODING, GZIP, false);
    }
    isCallingHeaders = true;
  }

  private static List<NameValuePair> createNamePairs(Params params) {
    List<NameValuePair> result = new ArrayList<NameValuePair>();
    for (Map.Entry<String, Value> entry : params.params.entrySet()) {
      if (entry.getValue() != null) {
        result.add(new BasicNameValuePair(entry.getKey(), entry.getValue().value));
      }
    }
    return result;
  }

  private static String createUrl(String url, String additionalParams) {
    if (additionalParams == null) {
      return url;
    }
    StringBuilder builder = new StringBuilder(url);
    if (url.indexOf("?") >= 0) {
      if (!url.endsWith("&")) {
        builder.append("&");
      }
    } else {
      builder.append("?");
    }
    builder.append(additionalParams);
    return builder.toString();
  }

  /**
   * Request params.
   */
  public static final class Params implements Serializable {
    private static final long serialVersionUID = -3710306448939240660L;
    private final Map<String, Value> params = new HashMap<String, Value>();

    public void put(String key, String value) {
      params.put(key, new Value(true, value));
    }

    /**
     * Puts a parameter.
     *
     * @param key key
     * @param value value
     * @param isCacheableParam whether this parameter can be a part of cache key
     */
    public void put(String key, String value, boolean isCacheableParam) {
      params.put(key, new Value(isCacheableParam, value));
    }

    public void putAll(Map<String, String> mapParams) {
      for (Map.Entry<String, String> entry : mapParams.entrySet()) {
        params.put(entry.getKey(), new Value(true, entry.getValue()));
      }
    }

    public void putAll(Params params) {
      this.params.putAll(params.params);
    }

    public Value get(String key) {
      return params.get(key);
    }

    public Map<String, String> getParamMap() {
      Map<String, String> result = new HashMap<String, String>();
      for (Map.Entry<String, Value> entry : params.entrySet()) {
        result.put(entry.getKey(), entry.getValue().value);
      }
      return result;
    }

    public void clear() {
      params.clear();
    }

    public Set<Map.Entry<String, Value>> entrySet() {
      return params.entrySet();
    }

    public String toString() {
      List<String> paramStrings = new ArrayList<String>();
      for (Map.Entry<String, Value> entry : params.entrySet()) {
        if (entry.getValue() != null && !TextUtils.isEmpty(entry.getValue().value)
            && !TextUtils.isEmpty(entry.getKey())) {
          try {
            String key = URLEncoder.encode(entry.getKey(), HTTP.UTF_8);
            String value = URLEncoder.encode(entry.getValue().value, HTTP.UTF_8);
            paramStrings.add(String.format("%s=%s", key, value));
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
        }
      }
      return StringUtil.join(paramStrings, "&");
    }
  }

  /**
   * Parameter value.
   */
  public static final class Value implements Serializable {
    private static final long serialVersionUID = -2274761554167685968L;
    public final boolean isCacheableParam;
    public final String value;

    public Value(boolean isCacheableParam, String value) {
      this.isCacheableParam = isCacheableParam;
      this.value = value;
    }
  }
}
