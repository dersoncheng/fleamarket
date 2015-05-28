package com.wandoujia.shared_storage;

import android.os.FileObserver;

import com.wandoujia.base.config.GlobalConfig;
import com.wandoujia.base.utils.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by liuyaxin on 13-8-19.
 */
public abstract class BaseSharedStorage {

  private static final String INNER_STORAGE_PATH = "/data/local/tmp/.wdj_config/";
  private static final String APP_STORAGE_PATH = ".storage";

  protected abstract String getStorageFileName();
  protected abstract void onFileChanged();
  private FileObserver fileObserver;


  protected BaseSharedStorage() {
    fileObserver = new FileObserver(getInnerStoragePath(),
        FileObserver.CLOSE_WRITE | FileObserver.DELETE) {
      @Override
      public void onEvent(int i, String s) {
        int action = i & FileObserver.ALL_EVENTS;
        if (action == FileObserver.CLOSE_WRITE
            || action == FileObserver.DELETE) {
          onFileChanged();
        }
      }
    };
    fileObserver.startWatching();
  }

  protected String loadContent() {
    File innerFile = new File(getInnerStoragePath());
    if (innerFile.exists() && innerFile.length() > 0) {
      String content = loadFromFile(innerFile);
      return content;
    } else {
      File appStorageFile = new File(getAppStoragePath());
      if (appStorageFile.exists() && appStorageFile.length() > 0) {
        String content = loadFromFile(appStorageFile);
        return content;
      }
    }
    return "";
  }

  protected void saveContent(String content) {
    File innerFile = new File(getInnerStoragePath());
    writeToFile(innerFile, content);
    changeInnerFilePermission();
    File appStorageFile = new File(getAppStoragePath());
    appStorageFile.getParentFile().mkdirs();
    writeToFile(appStorageFile, content);
  }

  protected void onFileSyntaxError() {
    FileUtil.deleteFile(getInnerStoragePath());
  }

  private String loadFromFile(File file) {
    BufferedReader reader = null;
    try {
      StringBuilder contentBuilder = new StringBuilder();
      reader = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = reader.readLine()) != null) {
        contentBuilder.append(line);
      }
      return contentBuilder.toString();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return "";
  }

  private void writeToFile(File file, String content) {
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(file);
      output.write(content.getBytes());
      output.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private String getInnerStoragePath() {
    return INNER_STORAGE_PATH + getStorageFileName();
  }

  private String getAppStoragePath() {
    return GlobalConfig.getAppContext().getFilesDir().getAbsolutePath()
        + "/" + APP_STORAGE_PATH + "/" + getStorageFileName();
  }

  private void changeInnerFilePermission() {
    try {
      Runtime.getRuntime().exec("chmod 777 " + INNER_STORAGE_PATH);
      Runtime.getRuntime().exec("chmod 666 " + getInnerStoragePath());
    } catch (Exception e) {}
  }

}
