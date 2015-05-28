package com.zhangyu.fleamarket.onlineconfig;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.wandoujia.base.utils.JsonSerializer;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.configs.Config;
import com.zhangyu.fleamarket.configs.FleaMarketOnlineConfigResult;
import com.zhangyu.fleamarket.utils.ThreadPool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketOnlineConfigController {
  private static final int RANDOM_CHECK_ONLINE_CONFIG_SPAN_MS = 7 * 3600 * 1000;

  private static FleaMarketOnlineConfigController configController;
  private final List<WeakReference<OnlineConfigListener>> listeners;
  public static final String FLEAMARKET_ONLINE_CONFIG = "fleamarket_online_config";

  private FleaMarketOnlineConfigController() {
    listeners = new ArrayList<WeakReference<OnlineConfigListener>>();
    scheduleUpgradeOnlineConfig();
  }

  public static synchronized void createInstance() {
    if (configController != null) {
      return;
    }
    configController = new FleaMarketOnlineConfigController();
  }

  public static synchronized FleaMarketOnlineConfigController getInstance() {
    if (configController == null) {
      createInstance();
    }
    return configController;
  }

  public void updateOnlineConfig() {
    new Thread(new UpdateConfigRunnable()).start();
  }

  class UpdateConfigRunnable implements Runnable {

    @Override
    public void run() {
      fetchOnlineConfig();
    }
  }

  public synchronized FleaMarketOnlineConfigResult fetchOnlineConfig() {
    FleaMarketOnlineConfigResult result = null;
    FleaMarketOnlineConfigDelegate delegate = new FleaMarketOnlineConfigDelegate();
    try {
      result = FleaMarketApplication.getDataApi().execute(delegate);
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    if (result != null) {
      saveConfig(result);
    }
    return result;
  }

  private void saveConfig(FleaMarketOnlineConfigResult snaptubeOnlineConfigResult) {
    if (snaptubeOnlineConfigResult != null) {
      Config.setSearchHint(snaptubeOnlineConfigResult.getSearchHit());
      String resultstring = JsonSerializer.toJson(snaptubeOnlineConfigResult);
      Map<String, String> map = new HashMap<String, String>();
      map.put(FLEAMARKET_ONLINE_CONFIG, resultstring);
      Config.saveOnlineSetting(map, false);
    }
  }

  public static void scheduleUpgradeOnlineConfig() {
    ThreadPool.execute(new Runnable() {

      @Override
      public void run() {
        Intent checkOnlineConfigIntent =
          new Intent(FleaMarketOnlineConfigScheduleReceiver.ACTION_CHECK_ONLINE_CONFIG);
        PendingIntent pendingIntent =
          PendingIntent.getBroadcast(
            FleaMarketApplication.getAppContext(), 0, checkOnlineConfigIntent,
            PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent == null) {
          pendingIntent =
            PendingIntent.getBroadcast(
              FleaMarketApplication.getAppContext(), 0, checkOnlineConfigIntent,
              PendingIntent.FLAG_ONE_SHOT);
          Calendar tomorrowCalendar = Calendar.getInstance();
          tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);
          tomorrowCalendar.set(Calendar.HOUR_OF_DAY, 15);
          tomorrowCalendar.set(Calendar.MINUTE, 0);
          tomorrowCalendar.set(Calendar.SECOND, 0);
          tomorrowCalendar.set(Calendar.MILLISECOND, 0);
          long randomTimeInterval = new Random().nextInt(RANDOM_CHECK_ONLINE_CONFIG_SPAN_MS);
          AlarmManager am = (AlarmManager)
            FleaMarketApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
          am.set(AlarmManager.RTC_WAKEUP, tomorrowCalendar.getTimeInMillis() + randomTimeInterval,
            pendingIntent);
        }
      }
    });
  }

  public interface OnlineConfigListener {
    void onOnlineConfigChanged(Map<String, String> changedMap);
  }
}
