package com.zhangyu.fleamarket.http.protoapi;

public enum ApiErrorCodes {
  SUCCESS(0), UNKNOWN(-1), SYSTEM_ERROR(-2), ILLEGAL_SESSION(-3), WAPSESSION_EXPIRED(-4), BAD_REQUEST(-5);

  private int value;

  private ApiErrorCodes(int value) {
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }
}
