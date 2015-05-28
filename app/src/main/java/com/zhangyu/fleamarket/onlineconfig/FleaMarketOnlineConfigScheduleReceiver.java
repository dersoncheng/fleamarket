package com.zhangyu.fleamarket.onlineconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketOnlineConfigScheduleReceiver extends BroadcastReceiver {
  public static final String ACTION_CHECK_ONLINE_CONFIG =
    "phoenix.intent.action.CHECK_ONLINE_CONFIG";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (ACTION_CHECK_ONLINE_CONFIG.equals(intent.getAction())) {
      FleaMarketOnlineConfigController.getInstance().updateOnlineConfig();
      FleaMarketOnlineConfigController.scheduleUpgradeOnlineConfig();
    }
  }
}
