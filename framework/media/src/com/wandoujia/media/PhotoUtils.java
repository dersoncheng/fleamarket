/**
 * @(#)PhotoUtils.java, 2012-4-10.
 * 
 *                      Copyright (c) 2011, Wandou Labs and/or its affiliates.
 *                      All rights reserved. WANDOU LABS
 *                      PROPRIETARY/CONFIDENTIAL. Use is subject to license
 *                      terms.
 */
package com.wandoujia.media;

import android.os.Environment;
import android.text.TextUtils;
import com.wandoujia.media.pmp.models.PhotoFolderInfo;
import com.wandoujia.media.pmp.models.PhotoFolderInfos;

import java.util.ArrayList;
import java.util.List;


/**
 * @author toy
 */
public class PhotoUtils {

  private static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory()
      .getAbsolutePath().toLowerCase();

  private static final String[] SCREENSHOTS_FOLDER_PATH = {
      "/pictures/screenshots",
      "/dcim/screenshots",
      "/screencapture",
      "/camera/screenshots",
      "/photo/screenshots",
      "/截屏图片",
      "/qq_screenshot" // QQ screenshots
  };

  private static final String[] SCREENSHOTS_FOLDER_ALIAS = {
      "SCREENSHOTS", "SCREENSHOTS", "SCREENSHOTS", "SCREENSHOTS", "SCREENSHOTS", "SCREENSHOTS",
      "QQ_SCREENSHOTS"
  };

  // Need to sync with PHOTO_FOLDER_ALIAS
  public static final String[] PHOTO_FOLDER_RELATIVE_PATH = {"/dcim/",
      "/camera/", "/pictures/camera/", "/images/", "/我的相机/",
      "/wandoujia/capture/", "/pictures/instagram/", // Instagram
      "/path/", // Path
      "/linecamera/", // Line Camera
      "/mtxx/", // 美图秀秀
      "/photowonder/", // 魔图精灵
      "/puddingcamera/", // Pudding Camera
      "/tuding/", // Tuding
      "/cymera/", // Cymera
      "/paper pictures/", // Paper Camera
      "/retrocamera/", // Retro Camera
      "/jiepang/", // 街旁
      "/我的照片/",
      "/pictures/papa/", // Papa
      "/tencent/micromsg/camera/", // weixin
      "/myxj/", // 美颜相机
      "/photo/"
  };

  // Need to sync with PHOTO_FOLDER_RELATIVE_PATH
  public static final String[] PHOTO_FOLDER_ALIAS = {"DCIM", "CAMERA",
      "PICTURES_CAMERA", "IMAGES", "MY_CAMERA", "WANDOUJIA_CAPTURE",
      "INSTAGRAM", "PATH", "LINE", "MTXX", "PHTOWONDER",
      "PUDDING", "TUDING", "CYMERA", "PAPER", "RETRO",
      "JIEPANG", "MY_PHOTO", "PAPA", "WEIXIN", "MYXJ","PHOTO"
  };

  public static boolean inScreenshotsFolder(String path) {
    return positionAt(path, SCREENSHOTS_FOLDER_PATH) != -1;
  }

  public static boolean inPhotoFolder(String path) {
    return inPhotoFolder(path, true);
  }

  public static boolean inPhotoFolder(String path, boolean includeScreenshots) {
    if (includeScreenshots && inScreenshotsFolder(path)) {
      return true;
    }

    return positionAt(path, PHOTO_FOLDER_RELATIVE_PATH) != -1;
  }

  /**
   * if the path of images is under the specific camera folder ,return the
   * alias of that folder, else return an empty string
   * 
   * @param path
   * @return
   */
  public static final String getFolderAlias(String path) {
    int position = positionAt(path, SCREENSHOTS_FOLDER_PATH);
    if (position != -1) {
      return SCREENSHOTS_FOLDER_ALIAS[position];
    }

    position = positionAt(path, PHOTO_FOLDER_RELATIVE_PATH);
    return (position == -1) ? "" : PHOTO_FOLDER_ALIAS[position];
  }

  public static PhotoFolderInfos getPhotoFolderInfoList() {
    PhotoFolderInfos.Builder infosBuilder = new PhotoFolderInfos.Builder();
    List<PhotoFolderInfo> infoList = new ArrayList<PhotoFolderInfo>();
    infosBuilder.photo_folder_info(infoList);
    for (int i = 0; i < PHOTO_FOLDER_RELATIVE_PATH.length; i++) {
      PhotoFolderInfo.Builder builder = new PhotoFolderInfo.Builder();
      builder.path(PHOTO_FOLDER_RELATIVE_PATH[i]);
      builder.name(PHOTO_FOLDER_ALIAS[i]);
      infoList.add(builder.build());
    }
    return infosBuilder.build();
  }

  private static int positionAt(String path, String[] folders) {
    if (TextUtils.isEmpty(path) || folders == null) {
      return -1;
    }

    String lowerPath = path.toLowerCase();
    for (int i = 0; i < folders.length; i++) {
      if (lowerPath.indexOf(getBasePath(path) + folders[i]) != -1) {
        return i;
      }
    }
    return -1;
  }

  private static String getBasePath(String path) {
    return fromDefaultSdcard(path) ? EXTERNAL_STORAGE_PATH : "";
  }

  private static boolean fromDefaultSdcard(String path) {
    String lowerPath = path.toLowerCase();
    return lowerPath.startsWith(EXTERNAL_STORAGE_PATH);
  }
}
