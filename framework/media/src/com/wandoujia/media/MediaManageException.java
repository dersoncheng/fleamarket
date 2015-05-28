package com.wandoujia.media;

/**
 * Created by liuyaxin on 13-8-22.
 */
public class MediaManageException extends Exception {

  public static interface ErrorCode {
    public static final int INVALID_ID = -1;
    public static final int NAME_EXIST = -2;
    public static final int GENERIC_FAILURE = -3;
    public static final int INVALID_NAME = -4;
    public static final int FILE_NOT_FOUND = -5;
  }

  private int error;

  public MediaManageException(int error) {
    this.error = error;
  }

  public int getError() {
    return error;
  }

}
