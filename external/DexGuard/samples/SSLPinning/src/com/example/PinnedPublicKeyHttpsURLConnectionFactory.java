/*
 * Sample application to illustrate SSL pinning with DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa BVBA
 */
package com.example;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * This sample class creates a javax.net.ssl.HttpsURLConnection with public key
 * pinning.
 *
 * If your application connects to an existing external server, you can
 * print out Java code with the MD5 hashes of the keys of the server, with
 * DexGuard's public key pinning tool. For example, for www.wikipedia.org:
 *
 *     tools/server_public_key_pinning_code.sh "https://www.wikipedia.org"
 *
 * You can then copy/paste the Java code from the output, to create an instance
 * of dexguard.util.PublicKeyTrustManager for the given server(s).
 *
 *
 * On the other hand, if you have your own X509 certificates, you can print
 * out the public keys, for instance with:
 *
 *     openssl x509 -inform pem -in wikipedia.pem -pubkey -noout
 * or
 *     openssl x509 -inform der -in wikipedia.der -pubkey -noout
 *
 * You can then print out the Java code with the MD5 hashes of these public
 * keys, for instance with:
 *
 *     tools/public_key_pinning_code.sh "MII.....lHy"
 *
 * The tool ignores spaces and newlines in the public key strings, for easier
 * copy/pasting. You can then again copy/paste the Java code from the output,
 * to create an instance of dexguard.util.PublicKeyTrustManager for the given
 * key(s).
 */
public class PinnedPublicKeyHttpsURLConnectionFactory
{
    public HttpsURLConnection createHttpsURLConnection(String urlString)
    throws Throwable
    {
        // Code copied from running tools/server_public_key_pinning_code.sh.
        // In this example, we are choosing to only trust and pin the public
        // key of wikipedia.org.

        // Create a TrustManager that only accepts servers with the specified public key hashes.
        dexguard.util.PublicKeyTrustManager trustManager = new dexguard.util.PublicKeyTrustManager(new int[] {
            0xb575e093, 0x5306f804, 0x312bc89c, 0x206ba93a, // Public key hash of CN=*.wikipedia.org, O="Wikimedia Foundation, Inc.", L=San Francisco, ST=California, C=US
        //  0xd47d0cc5, 0x7030c66b, 0xd2b8edfe, 0x8121cdc0, // Public key hash of CN=DigiCert High Assurance CA-3, OU=www.digicert.com, O=DigiCert Inc, C=US
        });

        TrustManager[] trustManagers = new TrustManager[] { trustManager };

        // Initialize the SSL context.
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, null);

        // Create the URL connection.
        URL url = new URL(urlString);
        HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

        return urlConnection;
    }
}
