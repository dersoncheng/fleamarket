/*
 * Sample application to illustrate SSL pinning with DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa BVBA
 */
package com.example;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.util.Log;

/**
 * This input stream factory performs HTTPS requests with
 * org.apache.http.client.HttpClient, pinning the server certificates.
 *
 * @see PinnedCertificateHttpsClient
 */
public class PinnedCertificateHttpClientInputStreamFactory
implements   InputStreamFactory
{
    private Context context;

    public PinnedCertificateHttpClientInputStreamFactory(Context context)
    {
        this.context = context;
    }


    // Implementations for InputStreamFactory.

    @Override
    public InputStream createInputStream(String url)
    {
        try
        {
            HttpClient httpClient = new PinnedCertificateHttpsClient(context);
            HttpGet httpGet = new HttpGet(url);

            // Get the response.
            HttpResponse response = httpClient.execute(httpGet);

            return response.getEntity().getContent();
        }
        catch (Throwable throwable)
        {
            Log.d(PinnedCertificateHttpClientInputStreamFactory.class.getSimpleName(),
                  "Could not create the input stream.",
                  throwable);
        }

        return null;
    }
}
