package com.wandoujia.base.infos;

import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;

public class FeatureInfos {

  private static final long FEATURE_TELEVISION = 1l << 37;
  private static final long FEATURE_CAMERA_ANY = 1l << 36;
  private static final long FEATURE_AUDIO_LOW_LATENCY = 1l << 35;
  private static final long FEATURE_BLUETOOTH = 1l << 34;
  private static final long FEATURE_CAMERA = 1l << 33;
  private static final long FEATURE_CAMERA_AUTOFOCUS = 1l << 32;
  private static final long FEATURE_CAMERA_FLASH = 1l << 31;
  private static final long FEATURE_CAMERA_FRONT = 1l << 30;
  private static final long FEATURE_FAKETOUCH = 1l << 29;
  private static final long FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT = 1l << 28;
  private static final long FEATURE_FAKETOUCH_MULTITOUCH_JAZZHAND = 1l << 27;
  private static final long FEATURE_LIVE_WALLPAPER = 1l << 26;
  private static final long FEATURE_LOCATION = 1l << 25;
  private static final long FEATURE_LOCATION_GPS = 1l << 24;
  private static final long FEATURE_LOCATION_NETWORK = 1l << 23;
  private static final long FEATURE_MICROPHONE = 1l << 22;
  private static final long FEATURE_NFC = 1l << 21;
  private static final long FEATURE_SCREEN_LANDSCAPE = 1l << 20;
  private static final long FEATURE_SCREEN_PORTRAIT = 1l << 19;
  private static final long FEATURE_SENSOR_ACCELEROMETER = 1l << 18;
  private static final long FEATURE_SENSOR_BAROMETER = 1l << 17;
  private static final long FEATURE_SENSOR_COMPASS = 1l << 16;
  private static final long FEATURE_SENSOR_GYROSCOPE = 1l << 15;
  private static final long FEATURE_SENSOR_LIGHT = 1l << 14;
  private static final long FEATURE_SENSOR_PROXIMITY = 1l << 13;
  private static final long FEATURE_SIP = 1l << 12;
  private static final long FEATURE_SIP_VOIP = 1l << 11;
  private static final long FEATURE_TELEPHONY = 1l << 10;
  private static final long FEATURE_TELEPHONY_CDMA = 1l << 9;
  private static final long FEATURE_TELEPHONY_GSM = 1l << 8;
  private static final long FEATURE_TOUCHSCREEN = 1l << 7;
  private static final long FEATURE_TOUCHSCREEN_MULTITOUCH = 1l << 6;
  private static final long FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT = 1l << 5;
  private static final long FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND = 1l << 4;
  private static final long FEATURE_USB_ACCESSORY = 1l << 3;
  private static final long FEATURE_USB_HOST = 1l << 2;
  private static final long FEATURE_WIFI = 1l << 1;
  private static final long FEATURE_WIFI_DIRECT = 1;

  public static long genFeatureInfos(FeatureInfo[] features) {
    if (features == null || features.length == 0) {
      return 0;
    }
    HashMap<String, Long> flagMap = new HashMap<String, Long>();
    flagMap.put(PackageManager.FEATURE_TELEVISION, FEATURE_TELEVISION);
    flagMap.put(PackageManager.FEATURE_CAMERA_ANY, FEATURE_CAMERA_ANY);
    flagMap.put(PackageManager.FEATURE_AUDIO_LOW_LATENCY, FEATURE_AUDIO_LOW_LATENCY);
    flagMap.put(PackageManager.FEATURE_BLUETOOTH, FEATURE_BLUETOOTH);
    flagMap.put(PackageManager.FEATURE_CAMERA, FEATURE_CAMERA);
    flagMap.put(PackageManager.FEATURE_CAMERA_AUTOFOCUS, FEATURE_CAMERA_AUTOFOCUS);
    flagMap.put(PackageManager.FEATURE_CAMERA_FLASH, FEATURE_CAMERA_FLASH);
    flagMap.put(PackageManager.FEATURE_CAMERA_FRONT, FEATURE_CAMERA_FRONT);
    flagMap.put(PackageManager.FEATURE_FAKETOUCH, FEATURE_FAKETOUCH);
    flagMap.put(PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT,
        FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT);
    flagMap.put(PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_JAZZHAND,
        FEATURE_FAKETOUCH_MULTITOUCH_JAZZHAND);
    flagMap.put(PackageManager.FEATURE_LIVE_WALLPAPER, FEATURE_LIVE_WALLPAPER);
    flagMap.put(PackageManager.FEATURE_LOCATION, FEATURE_LOCATION);
    flagMap.put(PackageManager.FEATURE_LOCATION_GPS, FEATURE_LOCATION_GPS);
    flagMap.put(PackageManager.FEATURE_LOCATION_NETWORK, FEATURE_LOCATION_NETWORK);
    flagMap.put(PackageManager.FEATURE_MICROPHONE, FEATURE_MICROPHONE);
    flagMap.put(PackageManager.FEATURE_NFC, FEATURE_NFC);
    flagMap.put(PackageManager.FEATURE_SCREEN_LANDSCAPE, FEATURE_SCREEN_LANDSCAPE);
    flagMap.put(PackageManager.FEATURE_SCREEN_PORTRAIT, FEATURE_SCREEN_PORTRAIT);
    flagMap.put(PackageManager.FEATURE_SENSOR_ACCELEROMETER, FEATURE_SENSOR_ACCELEROMETER);
    flagMap.put(PackageManager.FEATURE_SENSOR_BAROMETER, FEATURE_SENSOR_BAROMETER);
    flagMap.put(PackageManager.FEATURE_SENSOR_COMPASS, FEATURE_SENSOR_COMPASS);
    flagMap.put(PackageManager.FEATURE_SENSOR_GYROSCOPE, FEATURE_SENSOR_GYROSCOPE);
    flagMap.put(PackageManager.FEATURE_SENSOR_LIGHT, FEATURE_SENSOR_LIGHT);
    flagMap.put(PackageManager.FEATURE_SENSOR_PROXIMITY, FEATURE_SENSOR_PROXIMITY);
    flagMap.put(PackageManager.FEATURE_SIP, FEATURE_SIP);
    flagMap.put(PackageManager.FEATURE_SIP_VOIP, FEATURE_SIP_VOIP);
    flagMap.put(PackageManager.FEATURE_TELEPHONY, FEATURE_TELEPHONY);
    flagMap.put(PackageManager.FEATURE_TELEPHONY_CDMA, FEATURE_TELEPHONY_CDMA);
    flagMap.put(PackageManager.FEATURE_TELEPHONY_GSM, FEATURE_TELEPHONY_GSM);
    flagMap.put(PackageManager.FEATURE_TOUCHSCREEN, FEATURE_TOUCHSCREEN);
    flagMap.put(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH, FEATURE_TOUCHSCREEN_MULTITOUCH);
    flagMap.put(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT,
        FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT);
    flagMap.put(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND,
        FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND);
    flagMap.put(PackageManager.FEATURE_USB_ACCESSORY, FEATURE_USB_ACCESSORY);
    flagMap.put(PackageManager.FEATURE_USB_HOST, FEATURE_USB_HOST);
    flagMap.put(PackageManager.FEATURE_WIFI, FEATURE_WIFI);
    flagMap.put(PackageManager.FEATURE_WIFI_DIRECT, FEATURE_WIFI_DIRECT);
    long flag = 0;
    for (FeatureInfo featureInfo : features) {
      if (flagMap.containsKey(featureInfo.name)) {
        flag |= flagMap.get(featureInfo.name);
      }
    }
    return flag;
  }
}
