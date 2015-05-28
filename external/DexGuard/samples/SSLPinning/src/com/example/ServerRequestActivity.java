/*
 * Sample application to illustrate SSL pinning with DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa BVBA
 */
package com.example;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This sample activity illustrates how to perform HTTPS requests with SSL
 * pinning. SSL pinning ensures that the application is talking to the
 * intended server. If the server provides a pinned certificate (in this
 * case the known certificate of wikipedia.org), the application displays
 * a downloaded image (the Wikipedia logo).
 *
 * The application shows three buttons, corresponding to different Android APIs
 * to perform the requests. The two first techniques pin the certificates;
 * the third technique pins the public keys of the certificates.
 * You can pick one technique for your own application.
 */
public class ServerRequestActivity extends Activity
{
    private static final String HTTPS_URL = "https://upload.wikimedia.org/wikipedia/meta/0/08/Wikipedia-logo-v2_1x.png";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sample certificate pinning with org.apache.http.client.HttpClient.
        final Button button1 = (Button)findViewById(com.example.R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new PinnedSSLTask(
                    new PinnedCertificateHttpClientInputStreamFactory(
                            ServerRequestActivity.this))
                        .execute(HTTPS_URL);
            }
        });

        // Sample certificate pinning with javax.net.ssl.HttpsURLConnection.
        final Button button2 = (Button)findViewById(com.example.R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new PinnedSSLTask(
                    new PinnedCertificateHttpsUrlConnectionInputStreamFactory(
                            ServerRequestActivity.this))
                        .execute(HTTPS_URL);
            }
        });

        // Sample public key pinning with javax.net.ssl.HttpsURLConnection.
        final Button button3 = (Button)findViewById(com.example.R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new PinnedSSLTask(
                     new PinnedPublicKeyHttpsUrlConnectionInputStreamFactory())
                         .execute(HTTPS_URL);
            }
        });
    }


    /**
     * This task loads an image in the background and displays it.
     */
    private class PinnedSSLTask extends AsyncTask<String, Integer, Bitmap>
    {
        private InputStreamFactory inputStreamFactory;
        private TextView           resultHeaderView;
        private ImageView          resultView;


        /**
         * Creates a new PinnedSSLTask that displays an image from the given
         * input stream factory.
         */
        public PinnedSSLTask(InputStreamFactory inputStreamFactory)
        {
            this.inputStreamFactory = inputStreamFactory;
            this.resultHeaderView   = (TextView)ServerRequestActivity.this.findViewById(com.example.R.id.resultHeaderView);
            this.resultView         = (ImageView)ServerRequestActivity.this.findViewById(com.example.R.id.resultView);
        }


        // Implementations of AsyncTask.

        @Override
        protected void onPreExecute()
        {
            resultHeaderView.setText("Loading image...");
            resultView.setImageDrawable(null);
        }

        @Override
        protected Bitmap doInBackground(String... urls)
        {
            if (urls.length == 0)
            {
                return null;
            }

            InputStream inputStream = inputStreamFactory.createInputStream(urls[0]);
            if (inputStream != null)
            {
                try
                {
                    return BitmapFactory.decodeStream(inputStream);
                }
                finally
                {
                    try
                    {
                        inputStream.close();
                    }
                    catch (IOException ignore) {}
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            if (bitmap == null)
            {
                resultHeaderView.setText("Could not load the image");
                resultView.setImageDrawable(null);
            }
            else
            {
                resultHeaderView.setText("");
                resultView.setImageBitmap(bitmap);
            }
        }
    }
}
