package com.zhangyu.fleamarket.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class NavigationManager {
  public static boolean safeStartActivity(Context context, Intent intent) {
    try {
      context.startActivity(intent);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
