/*
 * Sample application to illustrate SSL pinning with DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa BVBA
 */
package com.example;

import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;

/**
 * This HttpsURLConnection factory creates connections with certificate pinning.
 * The trusted certificates are in a trust store.
 *
 * @see TrustStoreFactory
 */
public class PinnedCertificateHttpsURLConnectionFactory
{
    private Context context;

    public PinnedCertificateHttpsURLConnectionFactory(Context context)
    {
        this.context = context;
    }

    public HttpsURLConnection createHttpsURLConnection(String urlString)
    throws Throwable
    {
        // Initialize the trust manager factory instance with our trust store
        // as source of certificate authorities and trust material.
        KeyStore trustStore = new TrustStoreFactory(context).createTrustStore();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(trustStore);

        // Initialize the SSL context.
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        // Create the https URL connection.
        URL url = new URL(urlString);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

        return urlConnection;
    }
}
