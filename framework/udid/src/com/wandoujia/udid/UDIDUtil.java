package com.wandoujia.udid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.wandoujia.base.utils.DigestUtils;
import com.wandoujia.base.utils.FileUtil;
import com.wandoujia.base.utils.LibraryLoaderHelper;
import com.wandoujia.base.utils.SharePrefSubmitor;
import com.wandoujia.base.utils.SystemUtil;

public class UDIDUtil {

  private static final String UDID_KEY = "udid";
  private static final String UDID_FILE_NAME = ".udid";
  private static final String INNER_ROOT_DIR = "/data/local/tmp";
  private static final String OLD_INNER_UDID_DIR_PATH = "/data/local/tmp/.config";
  private static final String OLD_INNER_UDID_FILE_PATH = "/data/local/tmp/.config/.udid";
  private static final String INNER_UDID_DIR_PATH = "/data/local/tmp/.wdj_config";
  private static final String INNER_UDID_FILE_PATH = "/data/local/tmp/.wdj_config/.udid";
  private static String UDID_FILE_PATH = getUDIDFilePath();
  private static Handler mainHandler = new Handler(Looper.getMainLooper());
  private static final long DELAY_WRITE_FS_TIME = 5000l;

  private static native String generateUDIDNative(String uuid);

  public static String generateUDID(Context context, String uuid) {
    LibraryLoaderHelper.loadLibrarySafety(context, "wdj_udid");
    return generateUDIDNative(uuid);
  }

  private static native boolean isUDIDValidNative(String udid);

  public static boolean isUDIDValid(Context context, String uuid) {
    if (LibraryLoaderHelper.loadLibrarySafety(context, "wdj_udid")) {
      return isUDIDValidNative(uuid);
    } else {
      return false;
    }
  }

  private static String UDID = null;

  private static String loadUDIDFromFS(Context context) {
    String filePath = getUDIDFilePath(context);
    if (!TextUtils.isEmpty(filePath) && FileUtil.exists(filePath)) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();
        reader.close();

        if (TextUtils.isEmpty(line)) {
          return "";
        }
        String udid = "";
        String[] parts = line.split("\t");
        if (parts.length >= 2) {
          String imei = SystemUtil.getImei(context);
          if (TextUtils.isEmpty(imei)) {
            if (TextUtils.isEmpty(parts[1])
                && !TextUtils.isEmpty(parts[0])) {
              udid = parts[0];
            }
          } else {
            if (parts[1].equals(DigestUtils.getStringMD5(imei))
                && !TextUtils.isEmpty(parts[0])) {
              udid = parts[0];
            }
          }
        } else {
          udid = line;
        }
        return udid;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return "";
  }

  private static String getUDIDFilePath(Context context) {
    return UDID_FILE_PATH;
  }

  public static void setUDIDFilePath(String filePath) {
    if (!TextUtils.isEmpty(filePath)) {
      UDID_FILE_PATH = filePath;
    }
  }

  private static String getUDIDFilePath() {
    if (SystemUtil.isSDCardMounted()) {
      return Environment.getExternalStorageDirectory().getAbsolutePath()
          + "/wandoujia/.config/" + UDID_FILE_NAME;
    } else {
      return "";
    }
  }

  private static String loadUDIDFromInternalStorage(Context context) {
    changeInnerRootDirPermission();
    if (FileUtil.exists(INNER_UDID_FILE_PATH)) {
      changeInnerFilePermission();
      FileUtil.deletePath(OLD_INNER_UDID_DIR_PATH);
      return FileUtil.readFileFirstLine(INNER_UDID_FILE_PATH);
    } else if (FileUtil.exists(OLD_INNER_UDID_FILE_PATH)) {
      String oldUdid = FileUtil.readFileFirstLine(OLD_INNER_UDID_FILE_PATH);
      if (!TextUtils.isEmpty(UDID) && isUDIDValid(context, oldUdid)) {
        asyncSaveUDIDToInternalStorage(oldUdid);
        return oldUdid;
      } else {
        FileUtil.deletePath(OLD_INNER_UDID_DIR_PATH);
      }
    }
    return "";
  }

  private static void saveUDIDToFS(Context context, String udid) {
    String filePath = getUDIDFilePath(context);
    if (!TextUtils.isEmpty(filePath)) {
      File file = new File(filePath);
      file.getParentFile().mkdirs();

      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        StringBuilder line = new StringBuilder();
        String imei = SystemUtil.getImei(context);

        line.append(udid);
        if (!TextUtils.isEmpty(imei)) {
          String imeiHashed = DigestUtils.getStringMD5(imei);
          if (!TextUtils.isEmpty(imeiHashed)) {
            line.append("\t");
            line.append(imeiHashed);
          }
        }
        writer.write(line.toString());
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static String generateUDID(Context context) {
    String uuid = UUID.randomUUID().toString();
    uuid = uuid.replaceAll("-", "");
    return generateUDID(context, uuid);
  }

  private static String loadUDIDFromSP(Context context) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    return pref.getString(UDID_KEY, "");
  }

  private static void asyncSaveUDIDToSP(final Context context, final String udid) {
    (new Thread() {
      @Override
      public void run() {
        if (!TextUtils.isEmpty(udid)) {
          SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
          Editor editor = pref.edit();
          editor.putString(UDID_KEY, udid);
          SharePrefSubmitor.submit(editor);
        }
      }
    }).start();
  }

  public static void resetUDID() {
    UDID = null;
  }

  public static String getUDID(Context context) {
    if (TextUtils.isEmpty(UDID)) {
      synchronized (UDIDUtil.class) {
        UDID = loadUDIDFromInternalStorage(context);
        if (!TextUtils.isEmpty(UDID) && isUDIDValid(context, UDID)) {
          asyncSaveUDIDToSP(context, UDID);
          asyncWriteUDIDToFSDelayed(context.getApplicationContext(), UDID);
        } else {
          UDID = loadUDIDFromSP(context);
          if (!TextUtils.isEmpty(UDID) && isUDIDValid(context, UDID)) {
            asyncSaveUDIDToInternalStorage(UDID);
            asyncWriteUDIDToFSDelayed(context.getApplicationContext(), UDID);
          } else {
            UDID = loadUDIDFromFS(context);
            if (!TextUtils.isEmpty(UDID) && isUDIDValid(context, UDID)) {
              asyncSaveUDIDToInternalStorage(UDID);
              asyncSaveUDIDToSP(context, UDID);
            } else {
              UDID = generateUDID(context);
              asyncSaveUDIDToInternalStorage(UDID);
              asyncSaveUDIDToSP(context, UDID);
              asyncWriteUDIDToFSDelayed(context.getApplicationContext(), UDID);
            }
          }
        }
      }
    }
    return UDID;
  }

  /**
   * Delay Write UDID to FS because it will cost 80 ms, and delay the UI show time.
   * @param context
   * @param udid
   */
  private static void asyncWriteUDIDToFSDelayed(final Context context,
                                                final String udid) {
    mainHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        asyncWriteUDIDToFS(context, udid);
      }
    }, DELAY_WRITE_FS_TIME);
  }

  private static void asyncWriteUDIDToFS(final Context context, final String udid) {
    new Thread() {
      @Override
      public void run() {
        synchronized (UDIDUtil.class) {
          saveUDIDToFS(context, udid);
        }
      }
    }.start();
  }

  public static void saveUDIDToStorage(Context context) {
    String udid = getUDID(context);
    synchronized (UDIDUtil.class) {
      asyncSaveUDIDToInternalStorage(udid);
    }
  }

  private static void changeInnerFilePermission() {
    try {
      Runtime.getRuntime().exec("chmod 777 " + INNER_UDID_DIR_PATH);
      Runtime.getRuntime().exec("chmod 666 " + INNER_UDID_FILE_PATH);
    } catch (Exception e) {}
  }

  private static void changeInnerRootDirPermission() {
    try {
      Runtime.getRuntime().exec("chmod 777 " + INNER_ROOT_DIR);
    } catch (Exception e) {}
  }

  private static void asyncSaveUDIDToInternalStorage(final String udid) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          changeInnerRootDirPermission();
          FileUtil.deletePath(OLD_INNER_UDID_DIR_PATH);
          FileUtil.deleteFile(INNER_UDID_FILE_PATH);
          File file = new File(INNER_UDID_FILE_PATH);
          file.getParentFile().mkdirs();
          BufferedWriter writer = new BufferedWriter(new FileWriter(file));
          writer.write(udid);
          writer.close();
          changeInnerFilePermission();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();

  }

}
