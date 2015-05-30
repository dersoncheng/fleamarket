package com.zhangyu.fleamarket.http.protoapi;


public class ApiException
    extends RuntimeException {
  private static final long serialVersionUID = 1824171233603511793L;
  private int errorCode = ApiErrorCodes.UNKNOWN.getValue();

  public ApiException() {
  }

  public ApiException(String message) {
    super(message);
  }

  public ApiException(Throwable cause) {
    super(cause);

    setCause(cause);
  }

  private void setCause(Throwable cause) {
    if ((cause instanceof ApiException)) {
      this.errorCode = ((ApiException) cause).getErrorCode();
    }
  }

  public ApiException(String message, Throwable cause) {
    super(message, cause);

    setCause(cause);
  }

  public ApiException(int errorCode, String message, Throwable cause) {
    super(message, cause);

    this.errorCode = errorCode;
  }

  public ApiException(int errorCode, String message) {
    super(message);

    this.errorCode = errorCode;
  }

  public ApiException(ApiErrorCodes error, String message) {
    this(error.getValue(), message);
  }

  public ApiException(ApiErrorCodes error, String message, Throwable cause) {
    this(error.getValue(), message, cause);
  }

  public int getErrorCode() {
    return this.errorCode;
  }
}
