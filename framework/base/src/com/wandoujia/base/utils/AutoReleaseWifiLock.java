package com.wandoujia.base.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import com.wandoujia.base.config.GlobalConfig;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by liuyaxin on 14-2-24.
 */
public class AutoReleaseWifiLock {

  private final static String WAKE_LOCK_TAG = "lock.power";
  private final static String WIFI_LOCK_TAG = "lock.wifi";
  private static final long DEFAULT_TIMEOUT = 10 * 60 * 1000;

  private Context context;
  private long timeout;

  private PowerManager.WakeLock wakeLock;
  private WifiManager.WifiLock wifiLock;

  private Timer timer;
  private TimerTask timeoutTask;

  public AutoReleaseWifiLock() {
    this(DEFAULT_TIMEOUT);
  }

  public AutoReleaseWifiLock(long timeout) {
    this.context = GlobalConfig.getAppContext();
    this.timeout = timeout;
  }

  private boolean isLocked() {
    return wakeLock != null && wakeLock.isHeld() && wifiLock != null && wifiLock.isHeld();
  }

  public synchronized void acquire() {
    if (!isLocked()) {
      release();
      acquireWakeLock();
      acquireWifiLock();
    }
    scheduleTimeout();
  }

  public synchronized void release() {
    releaseWifiLock();
    releaseWakeLock();
  }

  private void acquireWakeLock() {
    PowerManager powerManager =
        (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
    if (wakeLock == null) {
      // Not support
      return;
    }
    wakeLock.setReferenceCounted(true);
    wakeLock.acquire();
  }

  private void acquireWifiLock() {
    WifiManager wifiManager =
        (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,
        WIFI_LOCK_TAG);
    if (wifiLock == null) {
      // Not support.
      return;
    }
    wifiLock.setReferenceCounted(true);
    wifiLock.acquire();
  }

  private void scheduleTimeout() {
    if (timer == null) {
      timer = new Timer();
    }
    if (timeoutTask != null) {
      timeoutTask.cancel();
      timer.purge();
    }
    timeoutTask = new TimerTask() {
      @Override
      public void run() {
        release();
      }
    };
    try {
      timer.schedule(timeoutTask, timeout);
    } catch (Exception e) {
      // Eat all exception
    }
  }

  private void releaseWifiLock() {
    if (wifiLock != null && wifiLock.isHeld()) {
      wifiLock.release();
      wifiLock = null;
      return;
    }
    wifiLock = null;
  }

  private void releaseWakeLock() {
    if (wakeLock != null && wakeLock.isHeld()) {
      wakeLock.release();
      wakeLock = null;
      return;
    }
    wakeLock = null;
  }

}
