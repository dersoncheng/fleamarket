package com.zhangyu.fleamarket.http.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Gson factory to get customized gson.
 * 
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class GsonFactory {

  private static Gson gson;

  static {
    final Gson defaultGson = new Gson();
    GsonBuilder gsonBuilder = new GsonBuilder();
    gson = gsonBuilder.create();
  }

  private GsonFactory() {}

  /**
   * Gets gson object.
   * 
   * @return {@link com.google.gson.Gson} object
   */
  public static Gson getGson() {
    return gson;
  }
}
