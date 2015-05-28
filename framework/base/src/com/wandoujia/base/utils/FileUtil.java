package com.wandoujia.base.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.wandoujia.base.config.GlobalConfig;
import com.wandoujia.base.reflect.JavaCalls;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

  private final static String PATH_PLACEHOLDER_EXTERNAL = "%EXTERNAL%";
  private final static String PATH_PLACEHOLDER_HOME = "%HOME%";
  private final static String PATH_PREFIX_EXTERNAL = "/mnt/sdcard";
  private final static String PATH_PREFIX_EXTERNAL_SDCARD = "/sdcard";

  public static void deleteFile(String path) {
    if (TextUtils.isEmpty(path)) {
      return;
    }
    File file = new File(path);
    if (!file.exists() || !file.isFile()) {
      return;
    }
    file.delete();
  }

  public static boolean exists(String path) {
    if (TextUtils.isEmpty(path)) {
      return false;
    }
    File file = new File(path);
    return file.exists();
  }

  public static boolean mkdir(String path) {
    File dir = new File(path);
    return dir.mkdirs();
  }

  public static boolean copyFile(File srcFile, File dstFile) {
    if (srcFile.exists() && srcFile.isFile()) {
      if (dstFile.isDirectory()) {
        return false;
      }
      if (dstFile.exists()) {
        dstFile.delete();
      }
      try {
        byte[] buffer = new byte[2048];
        BufferedInputStream input = new BufferedInputStream(
            new FileInputStream(srcFile));
        BufferedOutputStream output = new BufferedOutputStream(
            new FileOutputStream(dstFile));
        while (true) {
          int count = input.read(buffer);
          if (count == -1) {
            break;
          }
          output.write(buffer, 0, count);
        }
        input.close();
        output.flush();
        output.close();
        return true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public static boolean copyFile(String srcPath, String dstPath) {
    return copyFile(new File(srcPath), new File(dstPath));
  }

  public static String readFileFirstLine(String filePath) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(filePath));
      String line = reader.readLine();
      reader.close();
      return line;
    } catch (Exception e) {
      return "";
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void deletePath(String path) {
    File file = new File(path);
    if (!file.exists()) {
      return;
    }
    if (file.isFile()) {
      file.delete();
      return;
    }
    String[] tmpList = file.list();
    if (tmpList == null) {
      return;
    }
    for (String fileName : tmpList) {
      if (fileName == null) {
        continue;
      }
      String tmpPath = null;
      if (path.endsWith(File.separator)) {
        tmpPath = path + fileName;
      } else {
        tmpPath = path + File.separator + fileName;
      }
      File tmpFile = new File(tmpPath);
      if (tmpFile.isFile()) {
        tmpFile.delete();
      }
      if (tmpFile.isDirectory()) {
        deletePath(tmpPath);
      }
    }
    file.delete();
  }

  public static void clearPath(String path) {
    File file = new File(path);
    if (!file.exists() || !file.isDirectory()) {
      return;
    }
    String[] tmpList = file.list();
    for (String fileName : tmpList) {
      String tmpPath = null;
      if (path.endsWith(File.separator)) {
        tmpPath = path + fileName;
      } else {
        tmpPath = path + File.separator + fileName;
      }
      File tmpFile = new File(tmpPath);
      if (tmpFile.isFile()) {
        tmpFile.delete();
      }
      if (tmpFile.isDirectory()) {
        deletePath(tmpPath);
      }
    }
  }

  public static long getFileSize(String path) {
    if (TextUtils.isEmpty(path)) {
      return 0;
    }
    File file = new File(path);
    if (file == null || !file.exists()) {
      return 0;
    }
    long size = file.length();
    if (file.isDirectory()) {
      File[] childList = file.listFiles();
      if (childList != null) {
        for (File childFile : childList) {
          try {
            size += getFileSize(childFile.getAbsolutePath());
          } catch (StackOverflowError e) {
            // too many recursion may cause stack over flow
            e.printStackTrace();
            return 0;
          } catch (OutOfMemoryError e2) {
            // too many call filenamesToFiles method
            e2.printStackTrace();
            return 0;
          }
        }
      }
    }
    return size;
  }

  /**
   * This method is used to transform %EXTERNAL% to external SD path
   * especially large files. This method should be used for video, music,
   * image, app related methods.
   *
   * @param path
   * @return
   */
  public static String formatExternalPath(String path) {
    String formatedPath = formatExternalPath(path,
        Environment.getExternalStorageDirectory().getAbsolutePath());
    if (!FileUtil.exists(formatedPath)) {
      return path;
    } else {
      return formatedPath;
    }
  }

  private static String formatExternalPath(String path, String replaced) {
    if (!TextUtils.isEmpty(path)) {
      if (path.startsWith(PATH_PLACEHOLDER_EXTERNAL)) {
        return path.replace(PATH_PLACEHOLDER_EXTERNAL, replaced);
      } else if (path.startsWith(PATH_PREFIX_EXTERNAL)) {
        return path.replace(PATH_PREFIX_EXTERNAL, replaced);
      } else if (path.startsWith(PATH_PREFIX_EXTERNAL_SDCARD)) {
        return path.replace(PATH_PREFIX_EXTERNAL_SDCARD, replaced);
      } else {
        return PathAdjustUtil.adjustSdcardPathForApp(path);
      }
    }
    return path;
  }

  /**
   * This method is used for formatting path using '%HOME%', '%EXTERNAL'.
   *
   * @param context
   * @param path
   * @return
   */
  public static String formatPath(Context context, String path) {
    if (path.startsWith(PATH_PLACEHOLDER_HOME)) {
      return formatHomePath(context, path);
    } else {
      return formatExternalPath(path);
    }
  }

  public static boolean isExternalPath(String path) {
    if (path.startsWith(PATH_PLACEHOLDER_HOME)) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * This method is used for transform '%HOME%' to available paths whether sd
   * card is mounted or not. This method should be only used for small files
   * from import/export, backup/restore.
   *
   * @param context
   * @param path
   * @return
   */
  public static String formatHomePath(Context context, String path) {
    if (path.startsWith(PATH_PLACEHOLDER_HOME)) {
      if (Environment.getExternalStorageState().equals(
          Environment.MEDIA_MOUNTED)) {
        return path.replace(PATH_PLACEHOLDER_HOME, Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + "/"
            + GlobalConfig.getAppRootDir() + "/");

      } else {
        return path.replace(PATH_PLACEHOLDER_HOME, context
            .getFilesDir().getAbsolutePath());
      }
    }
    return path;
  }

  public static boolean isExternalFile(Context context, String path) {
    if (!TextUtils.isEmpty(path)) {
      return path.startsWith(Environment.getExternalStorageDirectory()
          .getAbsolutePath());
    } else {
      return false;
    }
  }

  public static String getFileName(String path) {
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    int lastIndex = path.lastIndexOf("/");
    if (lastIndex > 0 && lastIndex < path.length() - 1) {
      return path.substring(lastIndex + 1);
    } else {
      return path;
    }
  }

  public static String getParentFilePath(String filePath) {
    if (filePath.endsWith("/")) {
      filePath = filePath.substring(0, filePath.length() - 1);
    }
    int lastSplitIndex = filePath.lastIndexOf("/");
    if (lastSplitIndex >= 0) {
      return filePath.substring(0, lastSplitIndex);
    }
    return null;
  }

  public static String getFileNameWithoutExtension(String path) {
    String filename = getFileName(path);
    if (filename != null && filename.length() > 0) {
      int dotPos = filename.lastIndexOf('.');
      if (0 < dotPos) {
        return filename.substring(0, dotPos);
      }
    }
    return filename;
  }

  public static String getFileExtension(String path) {
    int index = path.lastIndexOf(".");
    if (index >= 0 && index < path.length() - 1) {
      return path.substring(index + 1).toUpperCase();
    }
    return "";
  }

  public static boolean renameFile(String originPath, String destPath) {
    File origin = new File(originPath);
    File dest = new File(destPath);
    if (!origin.exists()) {
      return false;
    }
    return origin.renameTo(dest);
  }

  /**
   * <b>return the available size of filesystem<b/>
   *
   * @return the number of bytes available on the filesystem rooted at the
   * given File
   */
  public static long getAvailableBytes(String root) {
    try {
      if (!TextUtils.isEmpty(root) && new File(root).exists()) {
        StatFs stat = new StatFs(root);
        long availableBlocks = (long) stat.getAvailableBlocks();
        return stat.getBlockSize() * availableBlocks;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * the size of all bytes of current filesystem where root path inside.<br>
   * <b>return the size of filesystem<b/>
   *
   * @param root
   * @return all bytes.
   */
  public static long getAllBytes(String root) {
    if (TextUtils.isEmpty(root)) {
      return 0L;
    }
    try {
      if (!TextUtils.isEmpty(root) && new File(root).exists()) {
        StatFs stat = new StatFs(root);
        long blocks = (long) stat.getBlockCount();
        return stat.getBlockSize() * blocks;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0L;
  }

  /**
   * Check If the storage is able to download.
   *
   * @param file
   * @return able to write
   */
  public static boolean canWrite(File file) {
    if (file == null || !file.exists()) {
      return false;
    }

    String testName = "." + System.currentTimeMillis();
    File testFile = new File(file, testName);

    boolean result = testFile.mkdir();
    if (result) {
      result = testFile.delete();
    }
    return result;
  }

  public static void setPermissions(String path, int permissions) {
    JavaCalls.callStaticMethod("android.os.FileUtils", path, permissions, -1, -1);
  }

  public static void write(String path, String content, boolean append) {
    FileWriter writer = null;
    try {
      File output = new File(path);
      if (!output.exists()) {
        output.getParentFile().mkdirs();
        output.createNewFile();
      }
      writer = new FileWriter(path, append);
      writer.write(content);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Throwable th) {
      th.printStackTrace();
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }

}
