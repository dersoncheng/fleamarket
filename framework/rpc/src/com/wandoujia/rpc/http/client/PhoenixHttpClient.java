package com.wandoujia.rpc.http.client;

import com.wandoujia.base.http.HttpClientWrapper;

/**
 * Phoenix http client.
 * 
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class PhoenixHttpClient extends HttpClientWrapper {

  private static final int SOCKET_TIMEOUT_MS = 60 * 1000;
  private static final int CONNECTION_TIMEOUT_MS = 30 * 1000;

  public PhoenixHttpClient() {
    super(HttpClientFactory.newInstance(SOCKET_TIMEOUT_MS, CONNECTION_TIMEOUT_MS));
  }
}
