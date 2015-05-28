package com.wandoujia.base.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.TargetApi;
import android.content.SharedPreferences.Editor;
import android.os.Build;

public class SharePrefSubmitor {
  private static final ExecutorService pool = Executors
      .newSingleThreadExecutor();

  @TargetApi(Build.VERSION_CODES.GINGERBREAD)
  public static void submit(final Editor editor) {
    if (SystemUtil.aboveApiLevel(Build.VERSION_CODES.GINGERBREAD)) {
      editor.apply();
    } else {
      pool.execute(new Runnable() {
        @Override
        public void run() {
          editor.commit();
        }
      });
    }
  }
}
