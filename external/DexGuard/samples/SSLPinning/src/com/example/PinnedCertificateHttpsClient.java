/*
 * Sample application to illustrate SSL pinning with DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa BVBA
 */
package com.example;

import java.security.KeyStore;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.content.Context;
import android.util.Log;

/**
 * This HttpClient applies certificate pinning to HTTPS connections.
 * The trusted certificates are in a trust store.
 *
 * @see TrustStoreFactory
 */
public class PinnedCertificateHttpsClient extends DefaultHttpClient
{
    private Context context;

    public PinnedCertificateHttpsClient(Context context)
    {
        this.context = context;
    }


    // Implementations for DefaultHttpClient.

    @Override
    protected ClientConnectionManager createClientConnectionManager()
    {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        //schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        try
        {
            // Create a trust store and register it for HTTPS.
            KeyStore trustStore = new TrustStoreFactory(context).createTrustStore();
            schemeRegistry.register(new Scheme("https", new SSLSocketFactory(trustStore), 443));
        }
        catch (Throwable throwable)
        {
            Log.e(PinnedCertificateHttpsClient.class.getSimpleName(),
                  "Could not add HTTPS scheme",
                  throwable);
        }

        return new SingleClientConnManager(getParams(), schemeRegistry);
    }
}
