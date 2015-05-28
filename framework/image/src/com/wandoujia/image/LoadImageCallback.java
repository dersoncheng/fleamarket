package com.wandoujia.image;

import android.graphics.Bitmap;

import com.wandoujia.image.rpc.ProgressCallback;

/**
 * The callback to notify image loading event.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public interface LoadImageCallback {
  /**
   * Gets called when image is loaded.
   * 
   * @param bitmap bitmap image, can be null
   * @param isImmediate True if this was called during ImageManager.getXXX variants.
   * This can be used to differentiate between a cached image loading and a network
   * image loading in order to, for example, run an animation to fade in network loaded
   * images.
   */
  void onResponse(Bitmap bitmap, boolean isImmediate);

}
