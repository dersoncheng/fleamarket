package com.wandoujia.base.http;

import android.util.Log;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * A custom TrustManager that delegates certificate checks to the system default one and
 * uses the local trust store if verification fails.
 * copy from https://github.com/nelenkov/custom-cert-https
 *
 * @author chenxingrun@wandoujia.com (Xingrun CHEN)
 */
public class WdjTrustManager implements X509TrustManager {
  private static final String TAG = WdjTrustManager.class.getSimpleName();


  static class LocalStoreX509TrustManager implements X509TrustManager {
    private X509TrustManager trustManager;

    LocalStoreX509TrustManager(KeyStore localTrustStore) {
      try {
        TrustManagerFactory tmf = TrustManagerFactory
            .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(localTrustStore);

        trustManager = findX509TrustManager(tmf);
        if (trustManager == null) {
          throw new IllegalStateException(
              "Couldn't find X509TrustManager");
        }
      } catch (GeneralSecurityException e) {
        throw new RuntimeException(e);
      }

    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
      trustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
      trustManager.checkServerTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return trustManager.getAcceptedIssuers();
    }
  }

  static X509TrustManager findX509TrustManager(TrustManagerFactory tmf) {
    TrustManager[] tms = tmf.getTrustManagers();
    for (int i = 0; i < tms.length; i++) {
      if (tms[i] instanceof X509TrustManager) {
        return (X509TrustManager) tms[i];
      }
    }

    return null;
  }

  private X509TrustManager defaultTrustManager;
  private X509TrustManager localTrustManager;

  private X509Certificate[] acceptedIssuers;

  /**
   *
   * @param localKeyStore the custom keystore.
   */
  public WdjTrustManager(KeyStore localKeyStore) {
    try {
      TrustManagerFactory tmf = TrustManagerFactory
          .getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init((KeyStore) null);

      defaultTrustManager = findX509TrustManager(tmf);
      if (defaultTrustManager == null) {
        throw new IllegalStateException(
            "Couldn't find X509TrustManager");
      }

      localTrustManager = new LocalStoreX509TrustManager(localKeyStore);

      List<X509Certificate> allIssuers = new ArrayList<X509Certificate>();
      for (X509Certificate cert : defaultTrustManager
          .getAcceptedIssuers()) {
        allIssuers.add(cert);
      }
      for (X509Certificate cert : localTrustManager.getAcceptedIssuers()) {
        allIssuers.add(cert);
      }
      acceptedIssuers = allIssuers.toArray(new X509Certificate[allIssuers
          .size()]);
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }


  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType)
    throws CertificateException {
    try {
      defaultTrustManager.checkClientTrusted(chain, authType);
    } catch (Exception e) {
      Log.d(TAG, "checkClientTrusted() with local trust manager...");
      localTrustManager.checkClientTrusted(chain, authType);
    }
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType)
    throws CertificateException {
    try {
      defaultTrustManager.checkServerTrusted(chain, authType);
    } catch (Exception e) {
      Log.d(TAG, "checkServerTrusted() with local trust manager...");
      localTrustManager.checkServerTrusted(chain, authType);
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return acceptedIssuers;
  }
}
