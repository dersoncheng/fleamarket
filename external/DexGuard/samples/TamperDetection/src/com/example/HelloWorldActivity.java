/*
 * Sample application to illustrate tamper detection with DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa / Itsana BVBA
 */
package com.example;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import dexguard.util.*;

/**
 * Sample activity that displays "Hello world!". It displays a different
 * message if someone has tampered with the application archive after it has
 * been created by DexGuard.
 *
 * You can experiment with it by first building, installing, and trying the
 * original version:
 *   ant release install
 * You can then tamper with it in some trivial way:
 *   zipalign -f 4 bin/HelloWorld-release.apk HelloWorld-tampered.apk
 *   adb install -r HelloWorld-tampered.apk
 * If you try the application again, you'll see that it detects that it has
 * been modified.
 *
 * Note: some custom firmwares for rooted devices (notably "AvatarRom") apply
 * zipalign when the application is installed, thus triggering the tamper
 * detection. This may or may not be desirable for your application.
 */
public class HelloWorldActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        new Delegate().checkAndInitialize();
    }


    /**
     * This utility class performs the tamper detection and creates the view.
     *
     * We're putting this functionality in a separate class so we can encrypt
     * it, as an extra layer of protection around the tamper detection and
     * some essential code. We can't encrypt the activity itself, for
     * technical reasons, but an inner class or another class are fine.
     */
    private class Delegate
    {
        public void checkAndInitialize()
        {
            // We need a context for most methods.
            final Context context = HelloWorldActivity.this;

            // You can pick your own value or values for OK,
            // to make the code less predictable.
            final int OK = 1;

            // Let the DexGuard utility library detect whether the apk has
            // been modified or repackaged in any way (with jar, zip,
            // jarsigner, zipalign, or any other tool), after DexGuard has
            // packaged it. The return value is the value of the optional
            // integer argument OK (default=0) if the apk is unchanged.
            int apkChanged =
                TamperDetector.checkApk(context, OK);

            // Let the DexGuard utility library detect whether the apk has
            // been re-signed with a different certificate, after DexGuard has
            // packaged it.  The return value is the value of the optional
            // integer argument OK (default=0) if the certificate is still
            // the same.
            int certificateChanged =
                CertificateChecker.checkCertificate(context, OK);

            // You can also explicitly pass the MD5 hash of a certificate, if
            // the application is only signed after DexGuard has packaged it.
            // You can print out the MD5 hash of the certificate of your key
            // store with
            //   keytool -list -keystore my.keystore
            //
            // If you are publishing on the Amazon Store, you can find the MD5
            // hash in
            //   Amazon Apps & Games Developer Portal
            //     > Binary File(s) Tab > Settings > My Account.
            //
            // With your MD5 hash, you can then use one of
            //   CertificateChecker.checkCertificate(context,
            //     "FA:F8:0A:CB:26:C9:08:DD:3F:E4:A4:76:1B:37:3E:C1", OK);
            //   CertificateChecker.checkCertificate(context,
            //     "FAF80ACB26C908DD3FE4A4761B373EC1", OK);
            //   CertificateChecker.checkCertificate(context,
            //     0xFAF80ACB, 0x26C908DD, 0x3FE4A476, 0x1B373EC1, OK);
            //   CertificateChecker.checkCertificate(context,
            //     0xFAF80ACB26C908DDL, 0x3FE4A4761B373EC1L, OK);
            //
            // If you specify a string, you should make sure it is encrypted,
            // for good measure.

            // Display a message.
            TextView view = new TextView(context);
            view.setText(apkChanged         == OK &&
                         certificateChanged == OK ?
                             "Hello world!" :
                             "Tamper alert!");
            view.setGravity(Gravity.CENTER);

            // Change the background color if someone has tampered with the
            // application archive.
            if (apkChanged         != OK ||
                certificateChanged != OK)
            {
                view.setBackgroundColor(Color.RED);
            }

            setContentView(view);

            // Briefly display a comment.
            String comment =
                certificateChanged != OK ? "The certificate is not the expected certificate" :
                apkChanged         != OK ? "The application archive has been modified"       :
                                           "The application has not been modified";

            Toast.makeText(context, comment, Toast.LENGTH_LONG).show();
        }
    }
}
