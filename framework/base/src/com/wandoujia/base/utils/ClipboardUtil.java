package com.wandoujia.base.utils;

import android.app.Service;
import android.content.ClipData;
import android.os.Build;

import com.wandoujia.base.config.GlobalConfig;

public class ClipboardUtil {
  public static void copyText(String text) {
    if (SystemUtil.aboveApiLevel(Build.VERSION_CODES.HONEYCOMB)) {
      android.content.ClipboardManager clipboard =
          (android.content.ClipboardManager) GlobalConfig.getAppContext().getSystemService(
              Service.CLIPBOARD_SERVICE);
      ClipData textCd = ClipData.newPlainText("phoenix", text);
      clipboard.setPrimaryClip(textCd);
    } else {
      android.text.ClipboardManager clipboard =
          (android.text.ClipboardManager) GlobalConfig.getAppContext().getSystemService(
              Service.CLIPBOARD_SERVICE);
      clipboard.setText(text);
    }
  }
}
