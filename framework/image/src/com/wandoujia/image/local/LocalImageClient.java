package com.wandoujia.image.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.android.volley.ServerError;
import com.android.volley.VolleyUtil;
import com.android.volley.toolbox.ByteArrayPool;
import com.wandoujia.base.utils.IOUtils;
import com.wandoujia.base.utils.ImageUtil;
import com.wandoujia.base.utils.SystemUtil;

/**
 * Local image client to get local image on device.
 * 
 * @author liuchunyu@wandoujia.com
 */
public class LocalImageClient {
  private static final int MAX_SIDE_LEN = 240;
  private final Context context;
  private final ByteArrayPool byteArrayPool;

  public LocalImageClient(Context context, ByteArrayPool byteArrayPool) {
    this.context = context;
    this.byteArrayPool = byteArrayPool;
  }

  /**
   * Gets icon of application which is installed locally.
   * 
   * @param packageName
   * @return app icon
   */
  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
  public Bitmap getAppIcon(String packageName) {
    PackageManager packageManager = context.getPackageManager();
    Drawable iconDrawable = null;
    try {
      ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName,
          PackageManager.GET_META_DATA);
      Resources resources = packageManager.getResourcesForApplication(applicationInfo);
      if (SystemUtil.aboveApiLevel(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)) {
        iconDrawable = resources.getDrawableForDensity(applicationInfo.icon,
            DisplayMetrics.DENSITY_XHIGH);
      } else {
        iconDrawable = resources.getDrawable(applicationInfo.icon);
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    if (iconDrawable != null) {
      return ImageUtil.drawableToBitmap(iconDrawable);
    }
    return null;
  }

  public Bitmap getApkIcon(String filePath) {
    if (TextUtils.isEmpty(filePath)) {
      return null;
    }
    try {
      File file = new File(filePath);
      PackageManager packageManager = context.getPackageManager();
      PackageInfo packageInfo = packageManager.getPackageArchiveInfo(file.getAbsolutePath(), 0);
      if (packageInfo == null) {
        return null;
      }
      if (packageInfo.applicationInfo != null) {
        packageInfo.applicationInfo.sourceDir = file.getAbsolutePath();
        packageInfo.applicationInfo.publicSourceDir = file.getAbsolutePath();
        return ((BitmapDrawable) packageInfo.applicationInfo.loadIcon(packageManager)).getBitmap();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets image identified by file path, locally.
   * 
   * @param filePath file path
   * @param maxWidth the max-width of the loaded bitmap, 0 means no limit, should not be negative
   * @param maxHeight The max-height of the loaded bitmap, 0 means no limit, should not be negative
   * @return bitmap
   */
  public Bitmap getImage(String filePath, final int maxWidth, final int maxHeight) {
    InputStream is = null;
    try {
      File file = new File(filePath);
      if (!file.exists()) {
        return null;
      }
      is = new FileInputStream(filePath);
      byte[] data = readInputStream(is, byteArrayPool, (int) file.length());
      return ImageUtil.decodeBitmap(data, maxWidth, maxHeight);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.close(is);
    }
    return null;
  }

  /**
   * Gets video thumbnail identified by file path, locally.
   * 
   * @param filePath file path
   * @return bitmap
   */
  public Bitmap getVideoThumbnail(String filePath) {
    long id = -1;
    String escapedPath = filePath.replace("'", "''");
    Cursor cursor = context.getContentResolver().query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.Video.Media._ID},
        MediaStore.MediaColumns.DATA + "='" + escapedPath + "'", null,
        null);
    if (cursor != null && cursor.moveToFirst()) {
      try {
        int idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
        id = cursor.getLong(idIndex);
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
    }

    Bitmap thumbnail;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
      thumbnail = android.media.ThumbnailUtils.createVideoThumbnail(
          filePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
      if (thumbnail != null) {
        thumbnail = android.media.ThumbnailUtils.extractThumbnail(
            thumbnail, MAX_SIDE_LEN, MAX_SIDE_LEN);
      }
    } else {
      thumbnail = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
          id, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND, null);
      if (thumbnail != null) {
        thumbnail = ImageUtil.scaleDown(thumbnail, MAX_SIDE_LEN, MAX_SIDE_LEN, true);
      }
    }
    return thumbnail;
  }

  /**
   * Reads input stream to buffer and updates progress.
   */
  private static byte[] readInputStream(InputStream is, ByteArrayPool byteArrayPool,
      int totalLength)
      throws IOException, InterruptedException {
    try {
      return VolleyUtil.getByteArrayFromInputStream(byteArrayPool, is, totalLength, null, true);
    } catch (ServerError e) {
      e.printStackTrace();
      return null;
    } finally {}
  }
}
