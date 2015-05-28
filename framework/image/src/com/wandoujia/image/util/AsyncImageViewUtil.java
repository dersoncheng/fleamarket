package com.wandoujia.image.util;

import com.wandoujia.image.BuildConfig;
import com.wandoujia.image.ImageUri;
import com.wandoujia.image.view.AsyncImageView;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class AsyncImageViewUtil {
  private AsyncImageViewUtil() {}

  private static void loadImage(AsyncImageView imageView, String uri, int defaultImageResId) {
    if (TextUtil.isNetUrl(uri)) {
      imageView.loadNetworkImage(uri, defaultImageResId);
    } else if (TextUtil.isNumber(uri)) {
      try {
        int id = Integer.parseInt(uri);
        imageView.setStaticImageResource(id);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        imageView.setStaticImageResource(defaultImageResId);
      }
    } else if (TextUtil.isPackageName(uri)) {
      imageView.loadLocalAppIcon(uri, defaultImageResId);
    } else if (TextUtil.isApkFilePath(uri)) {
      imageView.loadLocalApkIcon(uri, defaultImageResId);
    } else {
      if (BuildConfig.DEBUG) {
        throw new RuntimeException(
            "Can't recognize uri type from the supplied uri, use default image");
      }
      imageView.setStaticImageResource(defaultImageResId);
    }
  }

  /**
   * load images
   * 
   * @param imageView ImageView that need to load image
   * @param imageUri key and key type to load image content,key can be http url, or image resource
   *          id,or package name, or local video path,or local apk path. Each type of url
   *          corresponds to a ImageUrlType.
   * @param defaultImageResId default image resource id, which is shown before loading
   */
  public static void loadImage(AsyncImageView imageView, ImageUri imageUri, int defaultImageResId) {
    if (imageUri == null || imageUri.getImageUri() == null || imageUri.getImageUriType() == null) {
      imageView.setStaticImageResource(defaultImageResId);
      return;
    }
    String uri = imageUri.getImageUri();
    ImageUri.ImageUriType uriType = imageUri.getImageUriType();
    switch (uriType) {
      case NETWORK:
        imageView.loadNetworkImage(uri, defaultImageResId);
        break;
      case LOCAL_IMAGE_RES:
        try {
          int id = Integer.parseInt(uri);
          imageView.setStaticImageResource(id);
        } catch (NumberFormatException e) {
          e.printStackTrace();
          imageView.setStaticImageResource(defaultImageResId);
        }
        break;
      case VIDEO_THUMBNAIL:
        imageView.loadVideoThumbnail(uri, defaultImageResId);
        break;
      case APK_ICON:
        imageView.loadLocalApkIcon(uri, defaultImageResId);
        break;
      case APP_ICON:
        imageView.loadLocalAppIcon(uri, defaultImageResId);
        break;
      case UNSPECIFIED:
        loadImage(imageView, imageUri.getImageUri(), defaultImageResId);
        break;
      default:
        imageView.setStaticImageResource(defaultImageResId);
        break;
    }
  }
}
