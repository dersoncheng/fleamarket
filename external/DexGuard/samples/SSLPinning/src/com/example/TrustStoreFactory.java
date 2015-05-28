/*
 * Sample application to illustrate SSL pinning with DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa BVBA
 */
package com.example;

import java.io.InputStream;
import java.security.KeyStore;

import android.content.Context;

/**
 * This factory creates a trust store from a key store bks-file in the assets.
 *
 * You can create your own trust store file as follows.
 *
 * If your application connects to an existing external server, you can
 * retrieve the certificates from the service with the openssl tool.
 * For instance, on Unix/Linux, for www.wikipedia.com:
 *
 *     openssl s_client -showcerts -connect www.wikipedia.com:443 </dev/null 2>/dev/null | openssl x509 -outform PEM > wikipedia.pem
 *
 * Download:
 *
 *     http://www.bouncycastle.org/download/bcprov-jdk15on-146.jar
 *
 * Don't download a later release, otherwise the Android runtime will throw an
 * exception ("java.io.IOException: Wrong version of key store.").
 *
 * Create a trust store with the certificate(s) that you trust. For instance:
 *
 *     keytool -importcert -file wikipedia.pem -keystore myapptruststore.bks -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-146.jar -storetype BKS
 *
 * List the certificates in your trust store:
 *
 *     keytool -list -v -keystore myapptruststore.bks -storepass mysecretpassword -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-146.jar
 *
 * Copy the trust store .bks file to the assets directory.
 */
public class TrustStoreFactory
{
    // The name and password of the trust store.
    //
    // You should let DexGuard encrypt this name and password in the code, and
    // the trust store file in the assets. In dexguard-project.txt:
    //
    //     -encryptstrings class com.example.TrustStoreFactory {
    //         java.lang.String TRUST_STORE_ASSET_NAME;
    //         java.lang.String TRUST_STORE_PASSWORD;
    //     }
    //
    //     -encryptassetfiles assets/myapptruststore.bks

    private static final String TRUST_STORE_ASSET_NAME = "myapptruststore.bks";
    private static final String TRUST_STORE_PASSWORD   = "mysecretpassword";


    private Context context;


    public TrustStoreFactory(Context context)
    {
        this.context = context;
    }


    public KeyStore createTrustStore() throws Throwable
    {
        // Retrieve the trust store file from the assets.
        InputStream inputStream = context.getAssets().open(TRUST_STORE_ASSET_NAME);
        try
        {
            // Create a key store with the retrieved input stream.
            KeyStore trustStore = KeyStore.getInstance("BKS");

            trustStore.load(inputStream, TRUST_STORE_PASSWORD.toCharArray());

            return trustStore;
        }
        finally
        {
                inputStream.close();
        }
    }
}
