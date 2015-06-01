package com.zhangyu.fleamarket.model;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class DemoEpisode {
  String videoTitle;
  String title;
  String youtubeId;
  long id;
  String detailParam;

  public DemoEpisode(long id, String videoTitle, String title, String youtubeId) {
    this.id = id;
    this.videoTitle = videoTitle;
    this.title = title;
    this.youtubeId = youtubeId;
  }

  public long getId() {
    return id;
  }

  public String getVideoTitle() {
    return videoTitle;
  }

  public String getTitle() {
    return title;
  }

  public String getYoutubeId() {
    return youtubeId;
  }

  public String getDetailParam() {
    return detailParam;
  }

  public void setDetailParam(String detailParam) {
    this.detailParam = detailParam;
  }
}
