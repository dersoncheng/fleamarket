package com.wandoujia.base.http;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * @author chenxingrun@wandoujia.com (Xingrun CHEN)
 */
public class WdjSSLSocketFactory extends SSLSocketFactory {
  private static volatile SSLSocketFactory defaultInstance = null;

  private SSLContext sslContext = SSLContext.getInstance("TLS");

  /**
   * Create a instance with KeyStore.
   *
   * @param truststore
   * @throws NoSuchAlgorithmException e
   * @throws KeyManagementException e
   * @throws KeyStoreException e
   * @throws UnrecoverableKeyException e
   */
  public WdjSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
      KeyManagementException, KeyStoreException, UnrecoverableKeyException {
    super(truststore);
    TrustManager tm = new WdjTrustManager(truststore);
    sslContext.init(null, new TrustManager[]{tm}, null);
  }

  @Override
  public Socket createSocket(Socket socket, String host, int port,
                             boolean autoClose) throws IOException {
    return sslContext.getSocketFactory().createSocket(socket, host, port,
        autoClose);
  }

  @Override
  public Socket createSocket() throws IOException {
    return sslContext.getSocketFactory().createSocket();
  }

  /**
   * A thread-safe factory method to get the SSLSocketFactory instance with wdjssl keystore.
   *
   * @see WdjSSLKeyStoreFactory
   *
   * @return SSLSocketFactory
   * @throws UnrecoverableKeyException e
   * @throws NoSuchAlgorithmException e
   * @throws KeyStoreException e
   * @throws KeyManagementException e
   * @throws IOException e
   * @throws CertificateException e
   */
  public static SSLSocketFactory getSSLSocketFactory() throws UnrecoverableKeyException,
      NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException,
      CertificateException {
    if (defaultInstance != null) {
      return defaultInstance;
    } else {
      synchronized (WdjSSLSocketFactory.class) {
        if (defaultInstance != null) {
          return defaultInstance;
        } else {
          SSLSocketFactory sslSocketFactory = null;
          try {
            sslSocketFactory = new WdjSSLSocketFactory(WdjSSLKeyStoreFactory.getKeyStore());
            sslSocketFactory.setHostnameVerifier(
                SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            WdjSSLSocketFactory.defaultInstance = sslSocketFactory;
          } catch (Exception e) {
            e.printStackTrace();
          }

          // If create SSLSocketFactory failed, fallback to EasySSLSocketFactory.
          // It may happen when load keystore failed (which should not happen).
          if (sslSocketFactory == null) {
            KeyStore trustStore;
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            sslSocketFactory = new EasySSLSocketFactory(trustStore);
            sslSocketFactory.setHostnameVerifier(
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
          }

          return sslSocketFactory;
        }
      }
    }
  }
}
