package com.wandoujia.shared_storage;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by liuyx on 13-9-18.
 */
public class AppIgnoreUpdateStorage
    extends ListSharedStorage<AppIgnoreUpdateStorage.IgnoreUpdateApp> {

  private static AppIgnoreUpdateStorage instance;

  public synchronized static AppIgnoreUpdateStorage getInstance() {
    if (instance == null) {
      instance = new AppIgnoreUpdateStorage();
    }
    return instance;
  }

  @Override
  public String getStorageFileName() {
    return "ignore_update_apps";
  }

  @Override
  protected Type getTypeToken() {
    return new TypeToken<Collection<IgnoreUpdateApp>>() {}.getType();
  }

  private AppIgnoreUpdateStorage() {}

  public void ignoreUpdates(List<String> pkgNames) {
    List<IgnoreUpdateApp> items = new ArrayList<IgnoreUpdateApp>();
    for (String pkgName : pkgNames) {
      items.add(new IgnoreUpdateApp(pkgName));
    }
    addOrUpdateItems(items);
  }

  public void ignoreUpdate(String pkgName) {
    if (!TextUtils.isEmpty(pkgName)) {
      addOrUpdateItem(new IgnoreUpdateApp(pkgName));
    }
  }

  public void unignoreUpdate(String pkgName) {
    removeItem(pkgName);
  }

  public Set<String> getIgnoreUpdateApps() {
    return getItemKeys();
  }

  public boolean isUpdateIgnored(String pkgName) {
    return containsItem(pkgName);
  }

  /**
   * App record which is ignored upgrade.
   */
  public static class IgnoreUpdateApp extends StorageLine {
    private final String pkgName;

    // For json deserialization purpose.
    public IgnoreUpdateApp() {
      this(null);
    }

    public IgnoreUpdateApp(String pkgName) {
      this.pkgName = pkgName;
    }

    @Override
    public String getKey() {
      return this.pkgName;
    }
  }

}
