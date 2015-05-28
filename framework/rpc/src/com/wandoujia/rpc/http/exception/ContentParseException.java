package com.wandoujia.rpc.http.exception;

/**
 * Exception class to show a content parse error.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class ContentParseException extends Exception {

  private static final long serialVersionUID = 1397363257477243232L;
  private final String contentString;

  public ContentParseException(String errorMessage, String contentString) {
    super(errorMessage);
    this.contentString = contentString;
  }

  public String getContentString() {
    return contentString;
  }
}
