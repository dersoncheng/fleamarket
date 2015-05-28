package com.wandoujia.shared_storage;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by liuyaxin on 13-8-24.
 */
public class IgnoreWashApps extends ListSharedStorage<IgnoreWashApps.IgnoreWashApp> {

  @Override
  protected String getStorageFileName() {
    return "ignore_wash_apps";
  }

  @Override
  protected Type getTypeToken() {
    return new TypeToken<Collection<IgnoreWashApp>>(){}.getType();
  }

  public enum IgnoreType {
    WASH, USELESS_APP
  }

  public static class IgnoreWashApp extends StorageLine {
    private String pkgName;
    private HashMap<IgnoreType, String> ignoreMap = new HashMap<IgnoreType, String>();

    // For json deserialization purpose.
    public IgnoreWashApp() {
      this(null);
    }

    public IgnoreWashApp(String pkgName) {
      this.pkgName = pkgName;
    }
    public String getKey() {
      return this.pkgName;
    }
    public boolean isIgnored(IgnoreType type) {
      return ignoreMap.containsKey(type);
    }
    public String getMd5(IgnoreType type) {
      return ignoreMap.get(type);
    }
    public void ignore(IgnoreType type, String md5) {
      ignoreMap.put(type, md5);
    }
  }

  private static IgnoreWashApps instance;

  private IgnoreWashApps() {
  }

  public synchronized static IgnoreWashApps getInstance() {
    if (instance == null) {
      instance = new IgnoreWashApps();
    }
    return instance;
  }

  public synchronized void ignore(String packageName, String md5, IgnoreType ignoreType) {
    if (TextUtils.isEmpty(packageName)) {
      return;
    }
    switch (ignoreType) {
      case WASH:
        ignoreWithMd5(packageName, md5, ignoreType);
        break;
      case USELESS_APP:
        ignoreWithoutMd5(packageName, ignoreType);
        break;
      default:
        break;
    }
  }

  public synchronized void updatePackageMd5(String packageName, String md5, IgnoreType ignoreType) {
    if (TextUtils.isEmpty(packageName)) {
      return;
    }
    IgnoreWashApp item = (IgnoreWashApp) getItem(packageName);
    if (item != null && item.isIgnored(ignoreType)) {
      item.ignore(ignoreType, md5);
    }
  }

  public synchronized  void ignoreAll(Collection<IgnoreWashApp> apps) {
    addOrUpdateItems(apps);
  }

  public synchronized void unIgnore(String packageName) {
    removeItem(packageName);
  }

  public boolean isIgnored(String packageName, String md5, IgnoreType ignoreType) {
    switch (ignoreType) {
      case WASH:
        if (TextUtils.isEmpty(md5)) {
          return isIgnoredWithoutMd5Check(packageName, ignoreType);
        } else {
          return isIgnoredWithMd5Check(packageName, md5, ignoreType);
        }
      case USELESS_APP:
        return isIgnoredWithoutMd5Check(packageName, ignoreType);
      default:
        return false;
    }
  }

  public synchronized Set<String> getIgnoredPackageNameSet() {
    return getItemKeys();
  }

  private void ignoreWithMd5(String packageName, String md5, IgnoreType ignoreType) {
    if (TextUtils.isEmpty(md5)) {
      return;
    }
    IgnoreWashApp item = (IgnoreWashApp) getItem(packageName);
    if (item == null) {
      item = new IgnoreWashApp(packageName);
    }
    item.ignore(ignoreType, md5);
    addOrUpdateItem(item);
  }

  private void ignoreWithoutMd5(String packageName, IgnoreType ignoreType) {
    IgnoreWashApp item = (IgnoreWashApp) getItem(packageName);
    if (item == null) {
      item = new IgnoreWashApp(packageName);
    }
    item.ignore(ignoreType, "");
    addOrUpdateItem(item);
  }

  private boolean isIgnoredWithMd5Check(String packageName, String md5, IgnoreType ignoreType) {
    IgnoreWashApp item = (IgnoreWashApp) getItem(packageName);
    if (item == null || !item.isIgnored(ignoreType)) {
      return false;
    }
    String ignoredMd5 = item.getMd5(ignoreType);
    String currentMd5 = md5;
    return TextUtils.equals(ignoredMd5, currentMd5);
  }

  private boolean isIgnoredWithoutMd5Check(String packageName, IgnoreType ignoreType) {
    IgnoreWashApp item = (IgnoreWashApp) getItem(packageName);
    if (item != null && item.isIgnored(ignoreType)) {
      return true;
    }
    return false;
  }

}
