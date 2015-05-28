package com.wandoujia.base.http;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build.VERSION_CODES;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wandoujia.base.config.GlobalConfig;
import com.wandoujia.base.utils.FreeHttpUtils;

/**
 * @author yingyixu@wandoujia.com (Yingyi Xu)
 */
public class FreeWebViewClientWrapper extends WebViewClient {
  private final String TAG = FreeWebViewClientWrapper.class.getName();
  private WebViewClient delegate;
  private String originHostName;

  public static WebViewClient newInstance(WebViewClient delegate, String originHostName) {
    FreeWebViewClientWrapper newClient = new FreeWebViewClientWrapper();
    newClient.delegate = delegate;
    newClient.originHostName = originHostName;
    return newClient;
  }

  private FreeWebViewClientWrapper() {}

  private String buildFreeUrl(String url) {
    if (FreeHttpUtils.isProxyUrl(url)) {
      return url;
    }
    URI uri = URI.create(url);
    if (FreeHttpUtils.FREE_HOST_NAME.equalsIgnoreCase(uri.getHost())
        && !FreeHttpUtils.isProxyUrl(url)) {
      url = url.replace(FreeHttpUtils.FREE_HOST_NAME, originHostName);
    }
    return FreeHttpUtils.buildFreeURLIfNeed(url);
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String originUrl) {
    if (!FreeHttpUtils.isInFreeMode()) {
      return delegate.shouldOverrideUrlLoading(view, originUrl);
    }
    final String proxyUrl = buildFreeUrl(originUrl);
    if (GlobalConfig.isDebug()) {
      Log.i(TAG, "Override [originUrl:" + originUrl + "] [proxyUrl: " + proxyUrl + "]");
    }
    view.loadUrl(proxyUrl);
    return false;
  }

  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    delegate.onPageStarted(view, url, favicon);
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    delegate.onPageFinished(view, url);
  }

  @Override
  public void onLoadResource(WebView view, String url) {
    delegate.onLoadResource(view, url);
  }

  @TargetApi(VERSION_CODES.HONEYCOMB)
  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, String originUrl) {
    if (!FreeHttpUtils.isInFreeMode()) {
      return super.shouldInterceptRequest(view, originUrl);
    }
    final String proxyUrl = buildFreeUrl(originUrl);
    final String type = FreeHttpUtils.getMimeType(originUrl);
    if (GlobalConfig.isDebug()) {
      Log.i(TAG, "Intercept [originUrl:" + originUrl + "] [proxyUrl: "
          + proxyUrl + "] [type:" + type + "]");
    }
    try {
      return new WebResourceResponse(type, null, new URL(proxyUrl).openStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return super.shouldInterceptRequest(view, originUrl);
  }

  @Override
  public void onTooManyRedirects(WebView view, Message cancelMsg,
      Message continueMsg) {
    delegate.onTooManyRedirects(view, cancelMsg, continueMsg);
  }

  @Override
  public void onReceivedError(WebView view, int errorCode,
      String description, String failingUrl) {
    delegate.onReceivedError(view, errorCode, description, failingUrl);
  }

  @Override
  public void onFormResubmission(WebView view, Message dontResend,
      Message resend) {
    delegate.onFormResubmission(view, dontResend, resend);
  }

  @Override
  public void doUpdateVisitedHistory(WebView view, String url,
      boolean isReload) {
    delegate.doUpdateVisitedHistory(view, url, isReload);
  }

  @TargetApi(VERSION_CODES.FROYO)
  @Override
  public void onReceivedSslError(WebView view, SslErrorHandler handler,
      SslError error) {
    delegate.onReceivedSslError(view, handler, error);
  }

  @Override
  public void onReceivedHttpAuthRequest(WebView view,
      HttpAuthHandler handler, String host, String realm) {
    delegate.onReceivedHttpAuthRequest(view, handler, host, realm);
  }

  @Override
  public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
    return delegate.shouldOverrideKeyEvent(view, event);
  }

  @Override
  public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
    delegate.onUnhandledKeyEvent(view, event);
  }

  @Override
  public void onScaleChanged(WebView view, float oldScale, float newScale) {
    delegate.onScaleChanged(view, oldScale, newScale);
  }

  @TargetApi(VERSION_CODES.HONEYCOMB_MR1)
  @Override
  public void onReceivedLoginRequest(WebView view, String realm,
      String account, String args) {
    delegate.onReceivedLoginRequest(view, realm, account, args);
  }
}
