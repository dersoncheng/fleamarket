package com.wandoujia.base.utils;

/**
 * @author zhulantian@wandoujia.com (Lantian Zhu)
 */
public class HardwareUtil {

  private HardwareUtil() {
    // for utility class
  }

  public static boolean isArmArch() {
    String arch = System.getProperty("os.arch");
    if (arch == null) {
      return true;
    }
    return arch.toLowerCase().startsWith("arm");
  }
}
