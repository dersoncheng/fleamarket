package com.wandoujia.base.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

// TODO(zhulantian) add function to putObject() and getObject(), use gson to parse object to string.
public class Preferences implements SharedPreferences {

  public static final String PREFERENCE_PREFIX = "pref_id_";

  public static Preferences getById(Context context, String id) {
    final String prefName = PREFERENCE_PREFIX + id;
    SharedPreferences body = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    return new Preferences(body);
  }

  private static String bundleToString(Bundle bundle) {
    if (bundle == null) {
      return null;
    }
    Parcel parcel = Parcel.obtain();
    try {
      bundle.writeToParcel(parcel, 0);
      return Base64.encodeToString(parcel.marshall(), Base64.DEFAULT);
    } finally {
      parcel.recycle();
    }
  }

  private static Bundle stringToBundle(String source) {
    byte[] input = Base64.decode(source, Base64.DEFAULT);
    Parcel parcel = Parcel.obtain();
    try {
      parcel.unmarshall(input, 0, input.length);
      parcel.setDataPosition(0);
      return Bundle.CREATOR.createFromParcel(parcel);
    } finally {
      parcel.recycle();
    }
  }

  private static String objectToString(Object obj) {
    if (obj == null) {
      return null;
    }
    return new Gson().toJson(obj);
  }

  private static <T> T stringToObject(String json, Class<T> classOfT) {
    Gson gson = new Gson();
    return gson.fromJson(json, classOfT);
  }

  private static <T> T stringToObject(String json, Type type) {
    Gson gson = new Gson();
    return gson.fromJson(json, type);
  }

  private SharedPreferences mPreference;

  public Preferences(SharedPreferences preference) {
    this.mPreference = preference;
  }

  @Override
  public boolean contains(String key) {
    return mPreference.contains(key);
  }

  @Override
  public CustomEditor edit() {
    return new CustomEditor(mPreference.edit());
  }

  @Override
  public Map<String, ?> getAll() {
    return mPreference.getAll();
  }

  @Override
  public boolean getBoolean(String key, boolean defValue) {
    return mPreference.getBoolean(key, defValue);
  }

  public Bundle getBundle(String key, Bundle defValue) {
    String source = mPreference.getString(key, null);
    if (source == null) {
      return defValue;
    }
    return stringToBundle(source);
  }

  public <T> T getObject(String key, Class<T> classOfT, T defVal) {
    String source = mPreference.getString(key, null);
    if (source == null) {
      return defVal;
    }
    return stringToObject(source, classOfT);
  }

  public <T> T getObject(String key, Type type, T defVal) {
    String source = mPreference.getString(key, null);
    if (source == null) {
      return defVal;
    }
    return stringToObject(source, type);
  }

  @Override
  public float getFloat(String key, float defValue) {
    return mPreference.getFloat(key, defValue);
  }

  @Override
  public int getInt(String key, int defValue) {
    return mPreference.getInt(key, defValue);
  }

  @Override
  public long getLong(String key, long defValue) {
    return mPreference.getLong(key, defValue);
  }

  @Override
  public String getString(String key, String defValue) {
    return mPreference.getString(key, defValue);
  }

  @Override
  public Set<String> getStringSet(String key, Set<String> defValues) {
    return mPreference.getStringSet(key, defValues);
  }

  @Override
  public void registerOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
    mPreference.registerOnSharedPreferenceChangeListener(listener);
  }

  @Override
  public void unregisterOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
    mPreference.unregisterOnSharedPreferenceChangeListener(listener);
  }

  public static class CustomEditor implements Editor {
    private Editor mEditor;

    public CustomEditor(Editor editor) {
      this.mEditor = editor;
    }

    @Override
    public Editor clear() {
      mEditor.clear();
      return this;
    }

    @Override
    public boolean commit() {
      return mEditor.commit();
    }

    @Override
    public Editor putBoolean(String key, boolean value) {
      mEditor.putBoolean(key, value);
      return this;
    }

    public Editor putBundle(String key, Bundle value) {
      mEditor.putString(key, bundleToString(value));
      return this;
    }

    public Editor putObject(String key, Object obj) {
      mEditor.putString(key, objectToString(obj));
      return this;
    }

    @Override
    public Editor putFloat(String key, float value) {
      mEditor.putFloat(key, value);
      return this;
    }

    @Override
    public Editor putInt(String key, int value) {
      mEditor.putInt(key, value);
      return this;
    }

    @Override
    public Editor putLong(String key, long value) {
      mEditor.putLong(key, value);
      return this;
    }

    @Override
    public Editor putString(String key, String value) {
      mEditor.putString(key, value);
      return this;
    }

    @Override
    public Editor remove(String key) {
      mEditor.remove(key);
      return this;
    }

    @Override
    public Editor putStringSet(String key, Set<String> values) {
      mEditor.putStringSet(key, values);
      return this;
    }

    @Override
    public void apply() {
      mEditor.apply();
    }
  }
}
