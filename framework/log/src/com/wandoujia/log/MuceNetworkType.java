package com.wandoujia.log;

/**
 * @author xubin@wandoujia.com
 */
public enum MuceNetworkType {
  NONE(0),
  WIFI(1),
  MOBILE(2);

  private int intValue;

  private MuceNetworkType(int intValue) {
    this.intValue = intValue;
  }

  public int getIntValue() {
    return intValue;
  }
}
