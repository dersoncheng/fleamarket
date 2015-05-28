package com.wandoujia.rpc.http.exception;

/**
 * Http exception.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class HttpException extends Exception {

  private static final long serialVersionUID = -3072988987189952939L;

  private final int statusCode;

  public HttpException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
