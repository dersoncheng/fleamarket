package com.wandoujia.log;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wandoujia.base.utils.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * @author xubin@wandoujia.com
 */
public class FileLogStorage implements LogStorage {
  private static final String TAG = "Log";
  private static final String PHOENIX_LOG_STORAGE_DIR_NAME = ".log";
  private static final String STORAGE_FILE_POST_FIX = ".log";
  private static final String OLD_STORAGE_FILE_POST_FIX = "-v2.log";
  private static final long STORAGE_FILE_MAX_SIZE = 1024 * 1024; // 1M
  private final Gson gson;
  private final String profileName;
  private final String processName;
  private final String storageFilePath;
  private final byte[] storageFileLock = new byte[0];
  private boolean isInitialized = false;

  FileLogStorage(Context context, String profileName) {
    this.gson = new Gson();
    this.profileName = profileName;
    this.processName = getProcessName(context);
    this.storageFilePath = getStorageFilePath(context);
  }

  @Override
  public void addEvent(LogEventModel logEvent) {
    synchronized (storageFileLock) {
      if (!isInitialized) {
        restoreInterruptedOutput();
        isInitialized = true;
      }
    }
    if (logEvent == null) {
      return;
    }
    if (!checkStorageFileLimit()) {
      return;
    }
    String eventLine = convertLogEventToString(logEvent);
    synchronized (storageFileLock) {
      try {
        IOUtils.writeString(eventLine + "\n", new FileOutputStream(storageFilePath, true));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public long output(OutputStream outputStream) {
    String outputFileName;
    long outputId;
    synchronized (storageFileLock) {
      outputId = generateOutputId();
      outputFileName = generateOutputFile(outputId);
    }
    if (outputFileName == null) {
      return -1;
    }
    try {
      IOUtils.write(new FileInputStream(outputFileName), true, outputStream, false);
      return outputId;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return -1;
  }

  @Override
  public void deleteOutput(long outputId) {
    String outputFileName = getOutputFileName(outputId);
    File outputFile = new File(outputFileName);
    outputFile.delete();
  }

  @Override
  public void restoreOutput(long outputId) {
    String outputFileName = getOutputFileName(outputId);
    File outputFile = new File(outputFileName);
    restoreOutputFile(outputFile);
  }

  private void restoreOutputFile(File outputFile) {
    if (!outputFile.exists()) {
      return;
    }
    synchronized (storageFileLock) {
      try {
        IOUtils.write(new FileInputStream(outputFile), new FileOutputStream(new File(
            storageFilePath), true));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    outputFile.delete();
  }

  private long generateOutputId() {
    return System.currentTimeMillis();
  }

  private String getOutputFileName(long outputId) {
    return storageFilePath + "." + outputId;
  }

  private String generateOutputFile(long outputId) {
    String outputFileName = getOutputFileName(outputId);
    File storageFile = new File(storageFilePath);
    File outputFile = new File(outputFileName);
    if (!storageFile.exists()) {
      return null;
    }
    if (outputFile.exists()) {
      if (!outputFile.delete()) {
        return null;
      }
    }
    if (!storageFile.renameTo(new File(outputFileName))) {
      return null;
    }
    return outputFileName;
  }

  private String convertLogEventToString(LogEventModel logEvent) {
    String paramsJson = gson.toJson(logEvent.params, new TypeToken<HashMap<String,
        String>>() {}.getType());
    paramsJson = paramsJson.replaceAll("\t", " ");
    StringBuilder builder = new StringBuilder();
    builder.append(logEvent.event).append("\t");
    builder.append(paramsJson).append("\t");
    builder.append(logEvent.createdAt).append("\t");
    builder.append(logEvent.priority.getLevel());
    return builder.toString();
  }

  private String getStorageFilePath(Context context) {
    String storageDirPath = context.getFilesDir().getAbsolutePath() + "/" +
        PHOENIX_LOG_STORAGE_DIR_NAME + "/" + profileName;
    File storageDir = new File(storageDirPath);
    if (!storageDir.exists()) {
      storageDir.mkdirs();
    }

    String storageFileName = processName.replaceAll("[.:]", "_")
        + "_" + profileName + "_" + STORAGE_FILE_POST_FIX;
    return storageDirPath + "/" + storageFileName;
  }

  private static String getProcessName(Context context) {
    String processName = context.getPackageName();
    int pid = Process.myPid();
    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (manager != null) {
      List<ActivityManager.RunningAppProcessInfo> list = manager.getRunningAppProcesses();
      if (list != null) {
        for (ActivityManager.RunningAppProcessInfo process : list) {
          if (process != null && process.pid == pid) {
            processName = process.processName;
            break;
          }
        }
      }
    }
    return processName;
  }

  private void restoreInterruptedOutput() {
    File dirFile = new File(storageFilePath).getParentFile();
    File[] storageFiles = dirFile.listFiles();
    for (File file : storageFiles) {
      String fileName = file.getPath();
      if (fileName.endsWith(OLD_STORAGE_FILE_POST_FIX)) {
        // for old implementation
        restoreOutputFile(file);
      } else if (!fileName.equals(storageFilePath)
          && fileName.contains(processName)
          && fileName.contains(profileName)) {
        restoreOutputFile(file);
      }
    }
  }

  private boolean checkStorageFileLimit() {
    File file = new File(storageFilePath);
    if (file.length() >= STORAGE_FILE_MAX_SIZE) {
      removeNormalEvent();
      if (file.length() >= STORAGE_FILE_MAX_SIZE) {
        return false;
      }
    }
    return true;
  }

  private void removeNormalEvent() {
    File oldFile = new File(storageFilePath);
    File tmpFile = new File(storageFilePath + ".tmp");
    BufferedReader reader = null;
    BufferedWriter writer = null;
    synchronized (storageFileLock) {
      try {
        reader = new BufferedReader(new FileReader(oldFile));
        writer = new BufferedWriter(new FileWriter(tmpFile));
        String line;
        while ((line = reader.readLine()) != null) {
          if (isNormalEvent(line)) {
            continue;
          }
          writer.write(line + "\n");
        }
        reader.close();
        writer.close();

        oldFile.delete();
        tmpFile.renameTo(oldFile);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        IOUtils.close(reader);
        IOUtils.close(writer);
      }
    }
  }

  private boolean isNormalEvent(String logEventString) {
    String[] parts = logEventString.split("\t");
    return parts.length >= 4 && parts[3].equals("0");
  }
}
