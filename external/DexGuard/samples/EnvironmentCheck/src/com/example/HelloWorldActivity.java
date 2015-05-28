/*
 * Sample application to illustrate debug detection, emulator detection, and
 * root detectiom with DexGuard.
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
 * Sample activity that displays "Hello world!".
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
     * This utility class performs debug detection, emulator detection, and
     * root detection, and sets up the view. If the environment is okay, the
     * application runs normally and displays "Hello world!". Otherwise, it
     * displays information about the environment.
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

            // Let the DexGuard utility library detect whether the application
            // is debuggable. The return code is non-zero if it is.
            int isDebuggable = DebugDetector.isDebuggable(context, OK);

            // Let the DexGuard utility library detect whether the a debugger
            // is attached to the application. The return code is non-zero if
            // so.
            int isDebuggerConnected = DebugDetector.isDebuggerConnected(OK);

            // Let the DexGuard utility library detect whether the app is
            // signed with a debug key. The return code is non-zero if so.
            int isSignedWithDebugKey = DebugDetector.isSignedWithDebugKey(context, OK);

            // Let the DexGuard utility library detect whether the app is
            // running in an emulator. The return code is non-zero if so.
            int isRunningInEmulator = EmulatorDetector.isRunningInEmulator(context, OK);

            // Let the DexGuard utility library detect whether the app is
            // running on a rooted device. The return code is non-zero if
            // so.
            int isDeviceRooted = RootDetector.isDeviceRooted(OK);

            // Display a message.
            TextView view = new TextView(context);
            view.setText(isDebuggable         == OK &&
                         isDebuggerConnected  == OK &&
                         isSignedWithDebugKey == OK &&
                         isRunningInEmulator  == OK &&
                         isDeviceRooted       == OK ?
                             "Environment alert!" :
                             "Environment alert!");
            view.setGravity(Gravity.CENTER);

            // Change the background color if there is an alert.
            if (isDebuggable         != OK ||
                isDebuggerConnected  != OK ||
                isSignedWithDebugKey != OK ||
                isRunningInEmulator  != OK ||
                isDeviceRooted       != OK)
            {
                view.setBackgroundColor(Color.RED);
            }

            setContentView(view);

            // Briefly display a comment.
            String comment =
                isDebuggable         != OK ? "The application is debuggable"              :
                isSignedWithDebugKey != OK ? "The application is signed with a debug key" :
                isDebuggerConnected  != OK ? "A debugger is connected"                    :
                isRunningInEmulator  != OK ? "The application is running in an emulator"  :
                isDeviceRooted       != OK ? "The device is rooted"                       :
                                             "The environment is okay";

            Toast.makeText(context, comment, Toast.LENGTH_LONG).show();
        }
    }
}
