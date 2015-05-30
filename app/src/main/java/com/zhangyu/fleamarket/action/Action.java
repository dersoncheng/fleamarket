package com.zhangyu.fleamarket.action;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public interface Action {
  void execute();

  public static class EmptyAction implements Action {
    @Override
    public void execute() {}
  }
}
