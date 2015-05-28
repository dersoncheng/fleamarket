/*
 * Sample application to illustrate SSL pinning with DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa BVBA
 */
package com.example;

import java.io.InputStream;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.util.Log;

/**
 * This input stream factory performs HTTPS requests with
 * javax.net.ssl.HttpsURLConnection, pinning the server certificates.
 *
 * @see PinnedCertificateHttpsURLConnectionFactory
 */
public class PinnedCertificateHttpsUrlConnectionInputStreamFactory
implements   InputStreamFactory
{
    private Context context;

    public PinnedCertificateHttpsUrlConnectionInputStreamFactory(Context context)
    {
        this.context = context;
    }


    // Implementations for InputStreamFactory.

    @Override
    public InputStream createInputStream(String url)
    {
        try
        {
            // Create the URL connection.
            HttpsURLConnection urlConnection =
                new PinnedCertificateHttpsURLConnectionFactory(context).createHttpsURLConnection(url);

            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Return the input stream.
            return urlConnection.getInputStream();
        }
        catch (Throwable throwable)
        {
            Log.d(PinnedCertificateHttpsUrlConnectionInputStreamFactory.class.getSimpleName(),
                  "Could not create the input stream.",
                  throwable);
        }

        return null;
    }
}
