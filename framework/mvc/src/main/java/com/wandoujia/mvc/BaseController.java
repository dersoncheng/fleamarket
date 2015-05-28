package com.wandoujia.mvc;

/**
 * Base for all controllers.
 * This interface is to make a standard for all controllers. Each controller should be bind with
 * BaseView and BaseModel.
 *
 * @author match@wandoujia.com (Diao Liu)
 */
public interface BaseController<V extends BaseView, M extends BaseModel> {
  /**
   * Bind the view and model.
   *
   * @param v view
   * @param m model
   */
  void bind(V v, M m);
}
