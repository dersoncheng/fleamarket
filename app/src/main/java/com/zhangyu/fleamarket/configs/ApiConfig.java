package com.zhangyu.fleamarket.configs;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class ApiConfig {
  public static final String API_HOST = "http://api.snappea.com";
  public static String getOnlineConfigApiUrl() {
    return API_HOST + "/v1/video/config";
  }
}
