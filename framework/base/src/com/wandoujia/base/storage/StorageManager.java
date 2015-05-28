package com.wandoujia.base.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.wandoujia.base.config.GlobalConfig;
import com.wandoujia.base.utils.FileUtil;
import com.wandoujia.base.utils.SharePrefSubmitor;

/**
 * The class is used for operating the storage.
 * 
 * @author luyinchen@wandoujia.com.
 */
public class StorageManager {

  private static final String ROOT_DIR = "/snaptube/";
  private static final String GENERIC_CONFIG_PREFERENCE_NAME = "com.wandoujia.phoenix2";
  private static final String KEY_LAST_USED_DIRECTORY = "key_last_used_directory";
  // we'll change the path when the current path's space is below LIMIT_SIZE
  private static final long LIMIT_SIZE = 50 * 1024 * 1024L;
  private static final int MIN_SDK = 14;
  private static final int FORTH_LINE = 4;

  private static StorageManager instance;
  private List<String> availableStoragesPathList;
  private String kitkatExtStoragePath;
  private final List<WeakReference<OnExternalStorageDirectoryChangedListener>> rdcListeners;
  private SharedPreferences genericSharedPrefs;
  private String defaultExternalStorageDirectory;

  private StorageManager() {
    genericSharedPrefs = GlobalConfig.getAppContext()
        .getSharedPreferences(GENERIC_CONFIG_PREFERENCE_NAME, Context.MODE_PRIVATE);
    registerReceiver();
    rdcListeners = new ArrayList<WeakReference<OnExternalStorageDirectoryChangedListener>>();
    availableStoragesPathList = getAvailableStorages();
    defaultExternalStorageDirectory = genericSharedPrefs.getString(KEY_LAST_USED_DIRECTORY, null);
    checkDefaultPathAvailable();
  }

  private void checkDefaultPathAvailable() {
    if (TextUtils.isEmpty(defaultExternalStorageDirectory)
        || !FileUtil.canWrite(new File(defaultExternalStorageDirectory))) {
      defaultExternalStorageDirectory = getAvailableDiretory(availableStoragesPathList, 0);
      saveAndNotifyDefaultExternalStorageDiretory(null, defaultExternalStorageDirectory);
    }
  }

  /**
   * the function is used for register the media receiver.
   * it just can be called once.
   */
  private void registerReceiver() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
    filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
    filter.addDataScheme("file");
    GlobalConfig.getAppContext().registerReceiver(new MediaReceiver(), filter);
  }

  /**
   * get the only instance of the class.
   * 
   * return instance
   */
  public static synchronized StorageManager getInstance() {
    if (instance == null) {
      instance = new StorageManager();
    }
    return instance;
  }

  private void saveAndNotifyDefaultExternalStorageDiretory(String lastPath, String currentPath) {
    if (!TextUtils.isEmpty(currentPath)) {
      SharedPreferences.Editor editor = genericSharedPrefs.edit();
      editor.putString(KEY_LAST_USED_DIRECTORY, currentPath);
      SharePrefSubmitor.submit(editor);
      notifyPathChange(lastPath, currentPath);
    }
  }

  private List<String> getAvailableStorages() {
    List<String> pathList = new ArrayList<String>();
    String kitkatExtStoragePathTemp = "";
    try {
      // if the sdk level is above 14,we'll check the function hidden in android.os.StorageManager.
      if (Build.VERSION.SDK_INT >= MIN_SDK) {
        Object sdManager = GlobalConfig.getAppContext().getSystemService(Activity.STORAGE_SERVICE);
        if (sdManager != null) {
          Method getVolumeList = sdManager.getClass().getMethod("getVolumeList", (Class[]) null);
          Method getVolumeState = sdManager.getClass().getMethod("getVolumeState", String.class);
          if (getVolumeList != null) {
            Object[] sdVolumes = (Object[]) getVolumeList.invoke(sdManager, (Object[]) null);
            if (sdVolumes != null && sdVolumes.length > 0) {
              Object sdVolume = sdVolumes[0];
              Method getPath = sdVolume.getClass().getMethod("getPath", (Class[]) null);
              // add paths
              for (int i = 0; i < sdVolumes.length; i++) {
                sdVolume = sdVolumes[i];
                String path = (String) getPath.invoke(sdVolume, (Object[]) null);
                if (android.os.Environment.MEDIA_MOUNTED.equals(getVolumeState.invoke(sdManager,
                    path))) {
                  pathList.add(path);
                }
              }
            }
          }
        }
        if (pathList.size() < 1) {
          String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath();
          if (!TextUtils.isEmpty(defaultPath)) {
            pathList.add(defaultPath);
          }
        }

        // 对Android v4.4+ 的外置SD卡做特殊处理
//        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//        if (currentapiVersion >= Build.VERSION_CODES.KITKAT) {
//          File[] dirs = GlobalConfig.getAppContext().getExternalFilesDirs(null);
//          if (dirs.length > 1 && dirs[1] != null) {
//            kitkatExtStoragePathTemp = dirs[1].toString();
//            pathList.add(kitkatExtStoragePathTemp);
//          }
//        }
      }
      // if the sdk level is below 14,we'll cat the /proc/mounts and find the corrent line.
      else {
        String defaultPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!TextUtils.isEmpty(defaultPath)) {
          pathList.add(defaultPath);
        }
        BufferedReader br = null;
        try {
          br =
              new BufferedReader(new InputStreamReader(
                  new FileInputStream(new File("/proc/mounts"))));
          String path;
          while (!TextUtils.isEmpty(path = br.readLine())) {
            if (path.contains("uid=1000") && path.contains("gid=1015")
                && !path.contains("asec")) {
              String[] devideStrings = path.split(" ");
              if (devideStrings.length >= FORTH_LINE) {
                String sdPath = devideStrings[1];
                if (!pathList.contains(sdPath)) {
                  pathList.add(sdPath);
                }
              }
            }
          }
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          try {
            br.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

      }
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    String timeStamp = "." + System.currentTimeMillis();
    Iterator<String> iterator = pathList.iterator();
    while (iterator.hasNext()) {
      String path = iterator.next();
      File file = new File(path, timeStamp);
      if (FileUtil.canWrite(new File(path)) && !file.exists()) {
        file.mkdir();
      } else {
        iterator.remove();
      }
    }
    for (String path : pathList) {
      File file = new File(path, timeStamp);
      if (file.exists()) {
        file.delete();
      }
      if (TextUtils.equals(path, kitkatExtStoragePathTemp)) {
        kitkatExtStoragePath = kitkatExtStoragePathTemp;
      }
    }
    return pathList;
  }

  /**
   * get available root directory without size.
   * 
   * @return defaultRootDirectory.
   */
  public String getExternalStorageDirectory() {
    return getExternalStorageDirectory(0);
  }

  /**
   * function to compare with list and choose the most available directory return.
   * 
   * @param size to check if there has enough space
   * @return the most suitable external storage directory,according to some rules
   */
  public String getExternalStorageDirectory(long size) {
    if (FileUtil.getAvailableBytes(defaultExternalStorageDirectory) < size + LIMIT_SIZE) {
      String tempPath = defaultExternalStorageDirectory;
      List<String> notUsedFilePaths = new ArrayList<String>();
      List<String> usedFilePaths = new ArrayList<String>();
      for (String path : availableStoragesPathList) {
        // remove self
        if (!TextUtils.isEmpty(defaultExternalStorageDirectory)
            && defaultExternalStorageDirectory.equals(path)) {
          continue;
        }
        if (new File(path + ROOT_DIR).exists()) {
          usedFilePaths.add(path);
        } else {
          notUsedFilePaths.add(path);
        }
      }
      String path = getAvailableDiretory(usedFilePaths, size);
      if (TextUtils.isEmpty(path)) {
        String path2 = getAvailableDiretory(notUsedFilePaths, size);
        if (TextUtils.isEmpty(path2)) {
          return defaultExternalStorageDirectory;
        }
        defaultExternalStorageDirectory = path2;
      } else {
        defaultExternalStorageDirectory = path;
      }
      saveAndNotifyDefaultExternalStorageDiretory(tempPath, defaultExternalStorageDirectory);
    }
    return defaultExternalStorageDirectory;
  }

  private String getAvailableDiretory(List<String> paths, long needSize) {
    String availableDirectory = null;
    long maxSize = -1;
    for (String path : paths) {
      long currentSize = FileUtil.getAvailableBytes(path);
      if (currentSize > maxSize && FileUtil.getAvailableBytes(path) > needSize) {
        maxSize = currentSize;
        availableDirectory = path;
      }
    }
    return availableDirectory;
  }

  /**
   * function to get available directory list.
   * 
   * @return path list,all of the external storage directorys.
   */
  public List<String> getExternalStorageDirectorys() {
    return availableStoragesPathList;
  }

  public String getKitkatExtStoragePath() {
    return kitkatExtStoragePath;
  }

  /**
   * function to judge if there has an available storage directory
   * 
   * @return if there has an available storage directory
   */
  public boolean isStorageMounted() {
    List<String> paths = StorageManager.getInstance().getExternalStorageDirectorys();
    return paths != null && paths.size() != 0;
  }

  /**
   * Watch media change and refresh storage files.
   */
  private class MediaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action != null) {
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
            || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
          availableStoragesPathList = getAvailableStorages();
          checkDefaultPathAvailable();
        }
      }
    }
  }

  /**
   * the listener will notify you when the last path has changed.
   */
  public interface OnExternalStorageDirectoryChangedListener {
    void onChanged(String lastPath, String currentPath);
  }

  /**
   * add external storage directory changed listener.
   */
  public void addExternalStorageDirectoryChangedListener(
      OnExternalStorageDirectoryChangedListener listener) {
    if (listener == null) {
      return;
    }
    synchronized (rdcListeners) {
      for (WeakReference<OnExternalStorageDirectoryChangedListener> reference : rdcListeners) {
        OnExternalStorageDirectoryChangedListener pkgListener = reference.get();
        if (listener.equals(pkgListener)) {
          return;
        }
      }
      rdcListeners.add(new WeakReference<OnExternalStorageDirectoryChangedListener>(listener));
    }
  }

  private void notifyPathChange(final String lastPath, final String currentPath) {
    final List<OnExternalStorageDirectoryChangedListener> listeners =
        new ArrayList<OnExternalStorageDirectoryChangedListener>();
    synchronized (rdcListeners) {
      Iterator<WeakReference<OnExternalStorageDirectoryChangedListener>> iterator =
          rdcListeners.iterator();
      while (iterator.hasNext()) {
        final OnExternalStorageDirectoryChangedListener listener = iterator.next().get();
        if (listener != null) {
          listeners.add(listener);
        } else {
          iterator.remove();
        }
      }
    }
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        for (OnExternalStorageDirectoryChangedListener listener : listeners) {
          listener.onChanged(lastPath, currentPath);
        }
      }
    });
  }


}
