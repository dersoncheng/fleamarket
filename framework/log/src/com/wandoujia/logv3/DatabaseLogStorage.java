package com.wandoujia.logv3;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.squareup.wire.Wire;
import com.wandoujia.logv3.model.packages.LogReportBatchEvent;
import com.wandoujia.logv3.model.packages.LogReportEvent;

/**
 * @author xubin@wandoujia.com
 * @author yangkai@wandoujia.com
 */
public class DatabaseLogStorage {

  private static final long STORAGE_FILE_MAX_SIZE = 1024 * 1024; // 1M
  private LogDatabaseHelper helper;

  public DatabaseLogStorage(Context context, String profileName) {
    helper = new LogDatabaseHelper(context, profileName);
  }

  /**
   * Append the LogEventModel to storage.
   *
   * @param logEvent the model of the log event.
   */
  public void addEvent(LogReportEvent logEvent) {
    if (!checkStorageFileLimit()) {
      return;
    }
    // add to db
    byte[] logText = logEvent.toByteArray();
    helper.addLog(logText, logEvent.real_time);
  }

  /**
   * Output ALL log events in the storage to specific output stream,
   * storage should temporarily save the output content and wait the calling of
   * {@link #onOutputSuccess(long)}
   * {@link #onOutputFail(long)}
   *
   * @param outputStream the output stream to output to.
   * @return count of logs in this output.
   */
  public long output(OutputStream outputStream) {
    Cursor cursor = helper.getAllLogs();
    if (cursor == null) {
      return -1;
    }

    long size = 0;
    try {
      Wire wire = new Wire();
      LogReportBatchEvent.Builder logReportBatchEvent = new LogReportBatchEvent.Builder();
      List<LogReportEvent> event = new ArrayList<LogReportEvent>();

      while (cursor.moveToNext()) {
        byte[] logText =
            cursor.getBlob(cursor.getColumnIndex(DBHelper.LogColumns.LOG_CONTENT));
        LogReportEvent reportEvent = wire.parseFrom(logText, LogReportEvent.class);
        event.add(reportEvent);
        size++;
      }
      logReportBatchEvent.event(event);

      outputStream.write(logReportBatchEvent.build().toByteArray());
    } catch (SQLiteException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return size;
  }

  /**
   * The output is send to server successfully, so the storage should delete the output content
   * of the specific output id here.
   *
   * @param outputLogCount the count of the log.
   */
  public void onOutputSuccess(long outputLogCount) {
    // delete id which small than outputId
    helper.removeTopLog(outputLogCount);
  }

  /**
   * The output is failed to send to server
   *
   * @param outputLogCount the count of the log.
   */
  public void onOutputFail(long outputLogCount) {
    // do nothing
  }

  private boolean checkStorageFileLimit() {
    File file = new File(helper.getDBPath());
    if (file.length() >= STORAGE_FILE_MAX_SIZE) {
      removeNormalEvent();
      if (file.length() >= STORAGE_FILE_MAX_SIZE) {
        return false;
      }
    }
    return true;
  }

  private void removeNormalEvent() {
    helper.removeNotRealTimeLog();
  }
}
