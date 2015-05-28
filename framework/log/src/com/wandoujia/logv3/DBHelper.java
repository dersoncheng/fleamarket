package com.wandoujia.logv3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper {

  static final String DATABASE_NAME_SUFFIX = "-log.db";
  private static final int DATABASE_VERSION = 2;
  static final String LOG_TABLE_NAME = "log_data";

  public interface LogColumns extends BaseColumns {
    public static final String IS_REAL_TIME = "is_real_time";
    public static final String LOG_CONTENT = "log_content";
  }

  public DBHelper(Context context, String profileName) {
    super(context, profileName + DATABASE_NAME_SUFFIX, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + LOG_TABLE_NAME + " ("
        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + LogColumns.IS_REAL_TIME + " BIT NOT NULL,"
        + LogColumns.LOG_CONTENT + " BLOB" + ");");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + LOG_TABLE_NAME);
    onCreate(db);
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + LOG_TABLE_NAME);
    onCreate(db);
  }
}
