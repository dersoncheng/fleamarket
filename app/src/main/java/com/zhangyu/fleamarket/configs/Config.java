package com.zhangyu.fleamarket.configs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;

import com.wandoujia.base.utils.IOUtils;
import com.wandoujia.base.utils.SharePrefSubmitor;
import com.wandoujia.shared_storage.SharedSettings;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.utils.ShortcutUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class Config {
  private static SharedPreferences genericSharedPrefs;
  private static SharedPreferences onlineSharedPrefs;
  private static final String NEW_KEY_SHORTCUT_CREATED = "NEW_SHORTCUT_CREATED";
  private static final String KEY_SHORTCUT_EXISTED = "SHORTCUT_EXISTED";
  private static final String KEY_FIRST_LAUNCH_APP_TIME = "first_launch_app_time";
  private static final String KEY_TEST_REGION = "KEY_TEST_REGION";
  private static String sFirstChannel = null;
  private static String sLastChannel = null;
  private static final String KEY_FIRST_CHANNEL = "first_channel";
  private static final String ASSETS_CHANNEL_FILE_NAME = "channel.mf";
  private static final String DEFAULT_CHANNEL = "debug";
  private static final String KEY_SEARCH_HINT = "KEY_SEARCH_HINT";
  private static final String KEY_USER_EXTERNAL_STORAGE_FOR_CACHE =
    "USER_EXTERNAL_STORAGE_FOR_CACHE";
  public static Class<? extends Activity> CONFIG_MAIN_ACTIVITY_CLASS = null;

  public static boolean isShortcutCreated() {
    return getGenericSharedPrefs().getBoolean(NEW_KEY_SHORTCUT_CREATED, false);
  }

  public static synchronized SharedPreferences getGenericSharedPrefs() {
    if (genericSharedPrefs == null) {
      genericSharedPrefs = getSharedPreferences(OverridableConfig.GENERIC_CONFIG_PREFERENCE_NAME);
    }
    return genericSharedPrefs;
  }

  private static SharedPreferences getSharedPreferences(String name) {
    return FleaMarketApplication.getAppContext().getSharedPreferences(name, Context.MODE_PRIVATE);
  }

  public static void saveOnlineSetting(Map<String, String> map, boolean needclear) {
    SharedPreferences.Editor editor = getOnlineSharedPrefs().edit();
    if (needclear) {
      editor.clear();
    }
    for (Map.Entry<String, String> e : map.entrySet()) {
      editor.putString(e.getKey(), e.getValue());
    }
    SharePrefSubmitor.submit(editor);
  }

  public static void setSearchHint(String hint) {
    SharedPreferences.Editor editor = getOnlineSharedPrefs().edit();
    editor.putString(KEY_SEARCH_HINT, hint);
    submit(editor);
  }

  @TargetApi(Build.VERSION_CODES.GINGERBREAD)
  public static void submit(final SharedPreferences.Editor editor) {
    SharePrefSubmitor.submit(editor);
  }

  public static void setShortcutCreated(boolean value) {
    SharedPreferences.Editor editor = getGenericSharedPrefs().edit();
    editor.putBoolean(NEW_KEY_SHORTCUT_CREATED, value);
    SharePrefSubmitor.submit(editor);
  }

  public static void setShortcutExisted(Context context, ShortcutUtil.ShortcutType type,
                                        boolean value) {
    SharedPreferences.Editor editor = getGenericSharedPrefs().edit();
    editor.putBoolean(KEY_SHORTCUT_EXISTED + type.name(), value);
    SharePrefSubmitor.submit(editor);
  }

  public static long getFirstLaunchAppTime() {
    return getGenericSharedPrefs().getLong(KEY_FIRST_LAUNCH_APP_TIME, 0);
  }

  public static void setFirstLaunchAppTime(long time) {
    SharedPreferences.Editor editor = getGenericSharedPrefs().edit();
    editor.putLong(KEY_FIRST_LAUNCH_APP_TIME, time);
    SharePrefSubmitor.submit(editor);
  }

  public static void preLoadPreferences() {
    getGenericSharedPrefs();
    getOnlineSharedPrefs();
  }

  public static String getTestRegion() {
    return getGenericSharedPrefs().getString(KEY_TEST_REGION, null);
  }

  public static synchronized String getFirstChannel() {
    if (TextUtils.isEmpty(sFirstChannel)) {
      sFirstChannel = SharedSettings.getInstance().getSetting(KEY_FIRST_CHANNEL, "");
      if (TextUtils.isEmpty(sFirstChannel)) {
        sFirstChannel = loadChannelFromAssets(FleaMarketApplication.getAppContext());
        sLastChannel = sFirstChannel;
        SharedSettings.getInstance().setSetting(KEY_FIRST_CHANNEL, sFirstChannel);
      }
    }
    return sFirstChannel;
  }

  public static boolean getUseExternalStorageForCache() {
    return getGenericSharedPrefs().getBoolean(
      KEY_USER_EXTERNAL_STORAGE_FOR_CACHE, false);
  }

  public static void setUseExternalStorageForCache(boolean external) {
    SharedPreferences.Editor editor = getGenericSharedPrefs().edit();
    editor.putBoolean(KEY_USER_EXTERNAL_STORAGE_FOR_CACHE, external);
    SharePrefSubmitor.submit(editor);
  }

  public static boolean doesExternalStorageForCacheExist() {
    return getGenericSharedPrefs().contains(KEY_USER_EXTERNAL_STORAGE_FOR_CACHE);
  }

  private static String loadChannelFromAssets(Context context) {
    BufferedReader reader = null;
    try {
      // TODO (lantian) maybe save channel in preference and return in exception
      AssetManager assetManager = context.getAssets();
      reader = new BufferedReader(new InputStreamReader(
        assetManager.open(ASSETS_CHANNEL_FILE_NAME)));
      String channel = reader.readLine();
      if (TextUtils.isEmpty(channel)) {
        return DEFAULT_CHANNEL;
      }
      return channel;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      IOUtils.close(reader);
    }
    return DEFAULT_CHANNEL;
  }

  private static synchronized SharedPreferences getOnlineSharedPrefs() {
    if (onlineSharedPrefs == null) {
      onlineSharedPrefs = getSharedPreferences(OverridableConfig.ONLINE_CONFIG_PREFERENCE_NAME);
    }
    return onlineSharedPrefs;
  }
}
