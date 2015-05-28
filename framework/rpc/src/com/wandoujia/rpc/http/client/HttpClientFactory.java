package com.wandoujia.rpc.http.client;

import java.security.KeyStore;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.Handler;
import android.os.Looper;

import com.wandoujia.base.http.EasySSLSocketFactory;

/**
 * Http client factory.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class HttpClientFactory {
  private static final int SOCKET_BUFFER_SIZE = 8192;
  private static final String HTTP_SCHEME = "http";
  private static final String HTTPS_SCHEME = "https";
  private static final int HTTP_PORT = 80;
  private static final int HTTPS_PORT = 443;
  private static final String PHOENIX2 = "phoenix2";

  private HttpClientFactory() {}

  public static HttpClient newInstance(int soTimeout, int connTimeout) {
    HttpParams params = new BasicHttpParams();
    HttpConnectionParams.setStaleCheckingEnabled(params, false);
    HttpConnectionParams.setConnectionTimeout(params, connTimeout);
    HttpConnectionParams.setSoTimeout(params, soTimeout);
    HttpConnectionParams.setSocketBufferSize(params, SOCKET_BUFFER_SIZE);
    params.setParameter(CoreProtocolPNames.USER_AGENT, PHOENIX2);

    // Don't handle redirects -- return them to the caller. Our code
    // often wants to re-POST after a redirect, which we must do ourselves.
    HttpClientParams.setRedirecting(params, true);
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(
        new Scheme(HTTP_SCHEME, PlainSocketFactory.getSocketFactory(), HTTP_PORT));

    try {
      KeyStore trustStore;
      trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      SSLSocketFactory sslSocketFactory = new EasySSLSocketFactory(trustStore);
      sslSocketFactory.setHostnameVerifier(
          SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      schemeRegistry.register(new Scheme(HTTPS_SCHEME, sslSocketFactory, HTTPS_PORT));
    } catch (Exception e) {
      // We just catch exception here, since there are too many exception types.
      e.printStackTrace();
    }

    ClientConnectionManager manager =
        new ThreadSafeClientConnManager(params, schemeRegistry);

    try {
      return createHttpClient(manager, params);
    } catch (Exception e) {
      // Some android devices, like the T-Mobile MyTouch Q, use a custom implementation of the
      // Apache HttpClient library. On such devices, creating the first HttpClient of your
      // application not in the main thread will result in an exception being thrown
      final HttpClient[] clients = new HttpClient[1];
      new Handler(Looper.getMainLooper()).post(new Runnable() {

        @Override
        public void run() {
          clients[0] = new PhoenixHttpClient();
          synchronized (clients) {
            clients.notifyAll();
          }
        }
      });
      synchronized (clients) {
        try {
          clients.wait();
        } catch (InterruptedException e1) {
          e.printStackTrace();
        }
      }
      return clients[0];
    }
  }

  private static HttpClient createHttpClient(ClientConnectionManager manager, HttpParams params) {
    return new DefaultHttpClient(manager, params) {
      @Override
      protected HttpContext createHttpContext() {
        // Same as DefaultHttpClient.createHttpContext() minus the
        // cookie store.
        HttpContext context = new BasicHttpContext();
        context.setAttribute(
            ClientContext.AUTHSCHEME_REGISTRY,
            getAuthSchemes());
        context.setAttribute(
            ClientContext.COOKIESPEC_REGISTRY,
            getCookieSpecs());
        context.setAttribute(
            ClientContext.CREDS_PROVIDER,
            getCredentialsProvider());
        return context;
      }
    };
  }

}
