package com.zhangyu.fleamarket.controller;

import android.text.TextUtils;

import com.wandoujia.em.common.proto.Video;
import com.wandoujia.em.common.proto.VideoEpisode;
import com.zhangyu.fleamarket.http.model.DemoBean;
import com.zhangyu.fleamarket.model.DemoEpisode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class DemoDetailController {

  public static List<DemoEpisode> getEpisodeList(long videoId) {
    return getEpisodeList(getVideoDetail(videoId));
  }


  public static DemoBean getVideoDetail(long videoId) {
    DemoBean videoBean = null;
//    VideoDetailDelegate delegate = new VideoDetailDelegate();
//    delegate.getRequestBuilder().setVideoId(videoId);
//    try {
//      videoBean = FleaMarketApplication.getDataApi().execute(delegate);
//    } catch (ExecutionException e) {
//      e.printStackTrace();
//    }
    return videoBean;
  }

  public static List<DemoEpisode> getEpisodeList(DemoBean videoBean) {
    List<DemoEpisode> episodeList = new ArrayList<DemoEpisode>();
    if (videoBean == null) {
      return episodeList;
    }
//    List<VideoEpisodeBean> episodeBeans = videoBean.getVideoEpisodes();
//    if (episodeBeans == null) {
//      return episodeList;
//    }
//    for (VideoEpisodeBean episodeBean : episodeBeans) {
//      List<PlayInfo> playInfos = episodeBean.getPlayInfos();
//      if (playInfos != null && !playInfos.isEmpty()) {
//        List<List<String>> urlLists = playInfos.get(0).getUrls();
//        if (urlLists != null && !urlLists.isEmpty()) {
//          List<String> urlList = urlLists.get(0);
//          if (urlList != null) {
//            if (urlList.size() == 1) {
//              episodeList.add(new DemoEpisode(videoBean.getId() == null ? 0
//                  : videoBean.getId(), videoBean.getTitle(), String
//                  .valueOf(episodeBean
//                      .getEpisodeNum()),
//                  YoutubeUtil.getYoutubeIdFromUrl(urlList.get(0))));
//            } else {
//              int index = 0;
//              for (String url : urlList) {
//                episodeList.add(new DemoEpisode(videoBean.getId(), videoBean.getTitle(),
//                    episodeBean.getEpisodeNum() + "-" + (++index),
//                    YoutubeUtil.getYoutubeIdFromUrl(url)));
//              }
//            }
//          }
//        }
//      }
//    }
    return episodeList;
  }

  public static List<DemoEpisode> getEpisodeList(Video video) {
    List<DemoEpisode> episodeList = new ArrayList<DemoEpisode>();
    if (video == null) {
      return episodeList;
    }
    List<VideoEpisode> episodeBeans = video.getVideoEpisodesList();
    if (episodeBeans == null) {
      return episodeList;
    }
    for (VideoEpisode episode : episodeBeans) {
      List<com.wandoujia.em.common.proto.PlayInfo> playInfos = episode.getPlayInfosList();
      if (playInfos != null && !playInfos.isEmpty()) {
        List<String> urlLists = playInfos.get(0).getUrlsList();
        if (urlLists != null && !urlLists.isEmpty()) {
          String url = urlLists.get(0);
          if (!TextUtils.isEmpty(url)) {
            episodeList.add(new DemoEpisode(video.getId() == null ? 0
                : video.getId(), video.getTitle(), String
                .valueOf(episode
                    .getEpisodeNum()),
                url));
          }
        }
      }
    }
    return episodeList;
  }


}
