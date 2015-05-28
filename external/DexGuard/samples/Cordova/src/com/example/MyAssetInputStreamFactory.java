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
            assetFileName.equals("www/index.html")    ? assetManager.open("www/index.html") :
            assetFileName.equals("www/css/index.css") ? assetManager.open("www/css/index.css") :
            assetFileName.equals("www/img/logo.png")  ? assetManager.open("www/img/logo.png") :
            assetFileName.equals("www/js/index.js")   ? assetManager.open("www/js/index.js") :
                                                        assetManager.open(assetFileName);
    }
}
