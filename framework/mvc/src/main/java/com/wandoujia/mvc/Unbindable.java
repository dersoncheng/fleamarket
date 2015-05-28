package com.wandoujia.mvc;

/**
 * @author liujinyu@wandoujia.com (Liu Jinyu)
 */
public interface Unbindable {
  /**
   * initialize/reset the view and model for reuse, especially for view in ListView
   */
  void unbind();
}
