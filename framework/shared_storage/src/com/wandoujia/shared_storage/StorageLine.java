package com.wandoujia.shared_storage;

import java.io.Serializable;

/**
 * Created by liuyaxin on 13-8-21.
 */
public abstract class StorageLine implements Serializable {
  public abstract String getKey();
}
