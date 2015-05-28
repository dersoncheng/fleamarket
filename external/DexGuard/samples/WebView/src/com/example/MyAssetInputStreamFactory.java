package com.example;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

/**
 * Creates input streams for assets, internally specifying asset files that
 * should be encrypted.
 */
public class MyAssetInputStreamFactory
implements   dexguard.util.AssetInputStreamFactory
{
    /**
     * Specifies the asset files that have to be encrypted.
     */
    @Override
    public InputStream createInputStream(AssetManager assetManager,
                                         String       assetFileName)
    throws IOException
    {
        // Call assetManager.open("myAssetName") for each asset file that
        // should be encrypted. DexGuard recognizes the string literal in the
        // invocation and encrypts the corresponding file for you, with an
        // option like
        //     -encryptassetfiles assets/**
        return
            assetFileName.equals("index.html") ? assetManager.open("index.html") :
            assetFileName.equals("index.css")  ? assetManager.open("index.css") :
            assetFileName.equals("droid.png")  ? assetManager.open("droid.png") :
            assetFileName.equals("index.js")   ? assetManager.open("index.js") :
                                                 assetManager.open(assetFileName);
    }
}
