package com.wandoujia.base.http;

import android.util.Log;

import com.wandoujia.base.utils.FreeHttpUtils;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * @author yingyixu@wandoujia.com (Yingyi Xu)
 */
public class HttpClientWrapper implements HttpClient {

  public interface ErrorCodeHandler {
    void onResponse(int statusCode);
  }

  public static final int HTTP_ERROR_PARAMETER = 411;
  public static final int HTTP_EXPIRED = 412;
  public static final int HTTP_MODULAR_FAILED = 413;
  public static final int HTTP_SIZE_OVERFLOW = 414;
  public static final int HTTP_SIGNATURE_FAILED = 419;
  public static ErrorCodeHandler errorCodeHandler;

  private HttpClient httpClient;

  public HttpClientWrapper(HttpClient client) {
    setWrappedHttpClient(client);
  }

  public static void setErrorCodeHandler(ErrorCodeHandler handler) {
    errorCodeHandler = handler;
  }

  public void setWrappedHttpClient(HttpClient client) {
    this.httpClient = client;

    if (httpClient instanceof AbstractHttpClient) {
      ((AbstractHttpClient) httpClient).setRedirectHandler(new RedirectHandler() {
        @Override
        public boolean isRedirectRequested(HttpResponse httpResponse, HttpContext httpContext) {
          int status = httpResponse.getStatusLine().getStatusCode();
          return status == HttpURLConnection.HTTP_MOVED_TEMP
              || status == HttpURLConnection.HTTP_MOVED_PERM
              || status == HttpURLConnection.HTTP_SEE_OTHER;
        }

        @Override
        public URI getLocationURI(HttpResponse httpResponse, HttpContext httpContext)
            throws ProtocolException {
          if (httpResponse.containsHeader("Location")) {
            Header[] locations = httpResponse.getHeaders("Location");
            if (locations != null && locations.length > 0) {
              return FreeHttpUtils.buildFreeURIIfNeed(URI.create(locations[0].getValue()));
            }
          }
          return null;
        }
      });
    }
  }

  public static HttpClientWrapper newInstance(HttpClient client) {
    return new HttpClientWrapper(client);
  }

  @Override
  public HttpResponse execute(HttpUriRequest request) throws IOException {
    return execute(request, (HttpContext) null);
  }

  @Override
  public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
    if (request == null) {
      throw new IllegalArgumentException("Request must not be null.");
    }

    return execute(determineTarget(request), request, context);
  }

  private HttpHost determineTarget(HttpUriRequest request) {
    // A null target may be acceptable if there is a default target.
    // Otherwise, the null target is detected in the director.
    HttpHost target = null;

    URI requestURI = request.getURI();
    if (requestURI.isAbsolute()) {
      target = new HttpHost(
          requestURI.getHost(),
          requestURI.getPort(),
          requestURI.getScheme());
    }
    return target;
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
    return execute(target, request, (HttpContext) null);
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
      throws IOException {
    if (FreeHttpUtils.isInFreeMode()) {
      target = FreeHttpUtils.FREE_HTTP_HOST;
      request = FreeHttpUtils.buildFreeRequest(request);
      Log.d("Download", "Change target " + target + ", and " + ((HttpRequestBase) request).getURI());
    }
    HttpResponse httpResponse = httpClient.execute(target, request, context);
    if (FreeHttpUtils.isInFreeMode() && errorCodeHandler != null
        && httpResponse.getStatusLine() != null) {
      errorCodeHandler.onResponse(httpResponse.getStatusLine().getStatusCode());
    }
    return httpResponse;
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> handler)
      throws IOException {
    return execute(request, handler, null);
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> handler,
      HttpContext context)
      throws IOException {
    HttpHost target = determineTarget(request);
    return execute(target, request, handler, context);
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> handler)
      throws IOException {
    return execute(target, request, handler, null);
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request,
      final ResponseHandler<? extends T> handler,
      HttpContext context) throws IOException {
    if (FreeHttpUtils.isInFreeMode()) {
      target = FreeHttpUtils.FREE_HTTP_HOST;
      request = FreeHttpUtils.buildFreeRequest(request);
      Log.d("Download", "Change target " + target + ", and " + ((HttpRequestBase) request).getURI());
    }
    if (FreeHttpUtils.isInFreeMode() && errorCodeHandler != null) {
      return httpClient.execute(target, request, new ResponseHandler<T>() {
        @Override
        public T handleResponse(HttpResponse httpResponse) throws ClientProtocolException,
            IOException {
          if (httpResponse.getStatusLine() != null) {
            errorCodeHandler.onResponse(httpResponse.getStatusLine().getStatusCode());
          }
          if (handler != null) {
            return handler.handleResponse(httpResponse);
          }
          return null;
        }
      }, context);
    } else {
      return httpClient.execute(target, request, handler, context);
    }
  }

  @Override
  public ClientConnectionManager getConnectionManager() {
    return httpClient.getConnectionManager();
  }

  @Override
  public HttpParams getParams() {
    return httpClient.getParams();
  }
}
