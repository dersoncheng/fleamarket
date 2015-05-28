package com.wandoujia.base.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @author chenxingrun@wandoujia.com (Xingrun CHEN)
 */
public class WdjSSLKeyStoreFactory {
  private static volatile KeyStore keyStore;
  private static final String SSL_KEYSTORE_PASSWORD = "aiEQx4NmaaJ9or";

  private WdjSSLKeyStoreFactory() {}

  /**
   * A thread-safe factory method to get the java.security.KeyStore singleton.
   * Which include the CA chains for *.wandoujia.com.
   *
   * @return keystore
   * @throws KeyStoreException e
   * @throws CertificateException e
   * @throws NoSuchAlgorithmException e
   * @throws IOException e
   */
  public static KeyStore getKeyStore() throws KeyStoreException, CertificateException,
      NoSuchAlgorithmException, IOException {
    if (keyStore != null) {
      return keyStore;
    } else {
      synchronized (WdjSSLKeyStoreFactory.class) {
        if (keyStore != null) {
          return keyStore;
        } else {
          InputStream in = null;
          try {
            // Create a KeyStore containing our trusted CAs
            keyStore = KeyStore.getInstance("BKS");
            in = WdjSSLKeyStoreFactory.class.getClassLoader()
                .getResourceAsStream("res/raw/wdjssl.bks");
            // Initialize the keystore with the provided trusted certificates
            // Also provide the password of the keystore
            keyStore.load(in, SSL_KEYSTORE_PASSWORD.toCharArray());

          } finally {
            if (in != null) {
              try {
                in.close();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
          return keyStore;
        }
      }
    }
  }
}
