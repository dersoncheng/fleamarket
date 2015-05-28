package com.wandoujia.logv3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.provider.BaseColumns;

import com.wandoujia.logv3.DBHelper.LogColumns;

/**
 * static access of {@link com.wandoujia.logv3.DBHelper}
 * 
 * @author yangkai@wandoujia.com
 * 
 */
public class LogDatabaseHelper {

  private DBHelper helper;

  LogDatabaseHelper(Context context, String profileName) {
    helper = new DBHelper(context, profileName);
  }

  public boolean addLog(byte[] logText, boolean isRealTime) {
    ContentValues values = new ContentValues();
    values.put(LogColumns.IS_REAL_TIME, isRealTime ? 1 : 0);
    values.put(LogColumns.LOG_CONTENT, logText);
    return helper.getWritableDatabase().insert(DBHelper.LOG_TABLE_NAME, null, values) > 0;
  }

  /**
   * delete top size log
   * 
   * @return
   */
  public boolean removeTopLog(long size) {
    try {
      // android sqlite not support: "delete from table order by XX limit XX " or
      // " delete top XX from table order by XX"
      helper.getWritableDatabase()
          .execSQL(
              "DELETE FROM " + DBHelper.LOG_TABLE_NAME + " WHERE " + LogColumns._ID
                  + " IN ( SELECT " + LogColumns._ID
                  + " FROM " + DBHelper.LOG_TABLE_NAME + " ORDER BY "
                  + LogColumns._ID + " LIMIT " + size + ")"
          );
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * delete no real time log
   * 
   * @return
   */
  public boolean removeNotRealTimeLog() {
    return helper.getWritableDatabase().delete(DBHelper.LOG_TABLE_NAME,
        LogColumns.IS_REAL_TIME + " = 0", null) > 0;
  }

  /**
   * get all current logs
   * 
   * @param context
   * @param helper
   * @return
   */
  public Cursor getAllLogs() {
    // after send success, log will delete from top, so here must order by _id
    return helper.getWritableDatabase().query(DBHelper.LOG_TABLE_NAME,
        new String[] {BaseColumns._ID, LogColumns.LOG_CONTENT}, null, null, null, null,
        LogColumns._ID);
  }

  /**
   * get database file path
   * 
   * @return
   */
  public String getDBPath() {
    return helper.getReadableDatabase().getPath();
  }
}
