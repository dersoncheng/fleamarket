package com.zhangyu.fleamarket.http.model;

import android.graphics.Picture;

import java.io.Serializable;
import java.util.List;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class DemoBean implements Serializable {

  private Long id;

  private long videoSeriesId;

  private String title;

  private List<String> alias;

  private List<String> directors;

  private List<String> actors;

  private List<String> screenWriters;

  private List<String> presenters;

  private List<String> roles;

  private List<String> dubbings;

  private String version;

  private String region;

  private String metaRegion;

  private int year;

  private Picture cover;

  private Picture pictures;

  private String language;

  private String description;

  private String station;

//  private Category category;

  private List<String> tags;

  private long releaseDate;

  private long createdDate;

  private long updatedDate;

  private int itemStatus;

  private int seasonNum;

  // 最新Episode的集数,可下载或者可播放
  private int latestEpisodeNum;

  // 最新Episode的期数,可下载或者可播放
  private long latestEpisodeDate;

  // 总集数
  private int totalEpisodesNum;

  private List<String> providerNames;

  private long downloadCount;

  private long playCount;

  private int estart;

  private int emax;

//  private List<VideoEpisodeBean> videoEpisodes;

  private boolean hasPlayInfos = false;// 是否有在线播放资源

  private boolean hasDownloadUrls = false; // 是否有下载资源

  private long updateFrequency = 0L;

  private long estimatedNextReleaseDate; // 估算出来的该视频下个剧集的更新时间

  public Long getId() {
    return id;
  }

  public long getVideoSeriesId() {
    return videoSeriesId;
  }


  public String getTitle() {
    return title;
  }

  public List<String> getAlias() {
    return alias;
  }

  public List<String> getDirectors() {
    return directors;
  }

  public List<String> getActors() {
    return actors;
  }

  public List<String> getScreenWriters() {
    return screenWriters;
  }

  public List<String> getPresenters() {
    return presenters;
  }

  public List<String> getRoles() {
    return roles;
  }

  public List<String> getDubbings() {
    return dubbings;
  }

  public String getVersion() {
    return version;
  }

  public String getRegion() {
    return region;
  }

  public String getMetaRegion() {
    return metaRegion;
  }

  public int getYear() {
    return year;
  }

  public Picture getCover() {
    return cover;
  }

  public Picture getPictures() {
    return pictures;
  }

  public String getLanguage() {
    return language;
  }

  public String getDescription() {
    return description;
  }

  public String getStation() {
    return station;
  }

//  public Category getCategory() {
//    return category;
//  }

  public List<String> getTags() {
    return tags;
  }

  public long getReleaseDate() {
    return releaseDate;
  }

  public long getCreatedDate() {
    return createdDate;
  }

  public long getUpdatedDate() {
    return updatedDate;
  }

  public int getItemStatus() {
    return itemStatus;
  }

  public int getSeasonNum() {
    return seasonNum;
  }

  public int getLatestEpisodeNum() {
    return latestEpisodeNum;
  }

  public long getLatestEpisodeDate() {
    return latestEpisodeDate;
  }

  public int getTotalEpisodesNum() {
    return totalEpisodesNum;
  }

  public List<String> getProviderNames() {
    return providerNames;
  }

  public long getDownloadCount() {
    return downloadCount;
  }

  public long getPlayCount() {
    return playCount;
  }

  public int getEstart() {
    return estart;
  }

  public int getEmax() {
    return emax;
  }

//  public List<VideoEpisodeBean> getVideoEpisodes() {
//    return videoEpisodes;
//  }

  public boolean isHasPlayInfos() {
    return hasPlayInfos;
  }

  public boolean isHasDownloadUrls() {
    return hasDownloadUrls;
  }

  public long getUpdateFrequency() {
    return updateFrequency;
  }

  public long getEstimatedNextReleaseDate() {
    return estimatedNextReleaseDate;
  }
}
