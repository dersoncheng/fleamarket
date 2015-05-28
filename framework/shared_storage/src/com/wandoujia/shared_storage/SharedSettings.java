package com.wandoujia.shared_storage;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by liuyx on 13-11-1.
 */
public class SharedSettings extends ListSharedStorage<SharedSettings.SettingLine> {

  private static SharedSettings instance;

  public synchronized static SharedSettings getInstance() {
    if (instance == null) {
      instance = new SharedSettings();
    }
    return instance;
  }
  @Override
  public String getStorageFileName() {
    return "shared_settings";
  }

  @Override
  protected Type getTypeToken() {
    return new TypeToken<Collection<SettingLine>>() {}.getType();
  }

  private SharedSettings() {}

  public boolean getSetting(String key, boolean defaultValue) {
    SettingLine line = getItem(key);
    try {
      return Boolean.parseBoolean(line.getValue());
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public void setSetting(String key, boolean value) {
    SettingLine line = new SettingLine(key, String.valueOf(value));
    addOrUpdateItem(line);
  }

  public long getSetting(String key, long defaultValue) {
    SettingLine line = getItem(key);
    try {
      return Long.parseLong(line.getValue());
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public void setSetting(String key, long value) {
    SettingLine line = new SettingLine(key, String.valueOf(value));
    addOrUpdateItem(line);
  }

  public String getSetting(String key, String defaultValue) {
    SettingLine line = getItem(key);
    try {
      if (line.getValue() != null) {
        return line.getValue();
      } else {
        return defaultValue;
      }
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public void setSetting(String key, String value) {
    SettingLine line = new SettingLine(key, value);
    addOrUpdateItem(line);
  }

  /**
   * Structure to store a line of settings.
   */
  public static class SettingLine extends StorageLine {
    private final String key;
    private final String value;

    // For json deserialization purpose.
    public SettingLine() {
      this(null, null);
    }

    public SettingLine(String key, String value) {
      this.key = key;
      this.value = value;
    }
    @Override
    public String getKey() {
      return key;
    }
    public String getValue() {
      return value;
    }
  }
}
