package com.wandoujia.media;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by liuyaxin on 13-8-22.
 */
public interface MediaConstants {

  public static final int MAX_LENGTH = -1;
  public static final int OFFSET_BEGIN = 0;

  public static final String ACTION_DEL_AUDIO_SUCCESS = "pheonix.intent.action.DEL_AUDIO_SUC";

  /**
   * BuildConfig.PROVIDER_AUTHORITY_PREFIX is automatically generated by build.gradle.
   * You must initialize PROVIDER_AUTHORITY_PREFIX property for the project in the build.gradle
   * of the depended project, or you would get a compile error.
   *
   * For example, add codes below to the build.gradle of the depended project.
   *
   * <pre>
   * {@code
   *      dependencies {
   *        compile project(':framework:media') {
   *          ext.authorityPrefix = "com.wandoujia.asus"
   *        };
   *      }
   * }
   * </pre>
   *
   * Modified by match@wandoujia.com (Liu Diao)
   */
  public static final String IMAGE_AUTHORITY = BuildConfig.PROVIDER_AUTHORITY_PREFIX + ".image";
  public static final Uri IMAGE_AUTHORITY_URI = Uri.parse("content://" + IMAGE_AUTHORITY);
  public static final Uri CONTENT_IMAGE_FOLDERS_URI = Uri.withAppendedPath(IMAGE_AUTHORITY_URI,
      "folders");

  public interface ImageFoldersColumns extends BaseColumns {
    public static final String FOLDER_PATH = "folder_path";
  }
}
