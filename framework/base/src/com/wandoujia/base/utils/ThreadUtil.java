package com.wandoujia.base.utils;

public class ThreadUtil {

  public static void throwIfInterrupted() throws InterruptedException {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }
  }

  public static boolean isInterrupted() {
    return Thread.currentThread().isInterrupted();
  }

}
