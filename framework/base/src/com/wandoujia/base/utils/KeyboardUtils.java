package com.wandoujia.base.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Chaojun Wang on 5/21/14.
 */
public class KeyboardUtils {
  private KeyboardUtils() {}

  public static void showKeyBoard(final EditText editText) {
    if (editText == null) {
      return;
    }
    editText.requestFocus();
    editText.post(new Runnable() {

      @TargetApi(Build.VERSION_CODES.CUPCAKE)
      @Override
      public void run() {
        InputMethodManager imm = (InputMethodManager) editText.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
      }
    });
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  public static void hideSoftInput(EditText editText) {
    if (editText == null) {
      return;
    }
    InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext()
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
  }
}
