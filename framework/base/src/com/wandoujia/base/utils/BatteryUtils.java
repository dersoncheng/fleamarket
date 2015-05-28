package com.wandoujia.base.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryUtils extends BroadcastReceiver {

  private int current = 100;
  private int total = 100;
  private boolean isCharging = false;

  private static BatteryUtils instance = null;

  private BatteryUtils() {}

  public static BatteryUtils getInstance() {
    if (instance == null) {
      instance = new BatteryUtils();
    }
    return instance;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
      current = intent.getExtras().getInt(BatteryManager.EXTRA_LEVEL);
      total = intent.getExtras().getInt(BatteryManager.EXTRA_SCALE);
      boolean plugged = (intent.getExtras().getInt(
          BatteryManager.EXTRA_PLUGGED, 0) != 0);
      if (isCharging && !plugged) {
        isCharging = false;
      } else if (!isCharging && plugged) {
        isCharging = true;
      }
    }
  }

}
