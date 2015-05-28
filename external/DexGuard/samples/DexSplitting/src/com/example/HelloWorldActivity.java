/*
 * Sample application to illustrate adding reflection using DexGuard.
 *
 * Copyright (c) 2012-2014 Saikoa / Itsana BVBA
 */
package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

/**
 * Sample activity that displays "Hello world!".
 */
public class HelloWorldActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Construct the "Hello world!" message with some external classes,
        // which DexGuard will package in a separate dex file.
        String message =
            new Message1().getMessage() +
            new Message2().getMessage();

        // Display the message.
        TextView view = new TextView(this);
        view.setText(message);
        view.setGravity(Gravity.CENTER);
        setContentView(view);

        // Briefly display a comment.
        Toast.makeText(this, "DexGuard has automatically split the dex files inside the application", Toast.LENGTH_LONG).show();
    }
}
