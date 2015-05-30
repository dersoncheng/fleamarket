package com.zhangyu.fleamarket.model;

import com.wandoujia.mvc.BaseModel;

/**
 * @author qisen (tangqisen@wandoujia.com)
 */
public interface Checkable extends BaseModel {
  public boolean isChecked();

  public void onChecked(boolean checked);
}
