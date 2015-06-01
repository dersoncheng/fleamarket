package com.zhangyu.fleamarket.http.protoapi;

import android.content.Context;

import com.wandoujia.em.common.proto.Video;
import com.zhangyu.fleamarket.action.Action;

/**
 * Created by niejunhong on 14-10-30.
 */
public class ProtoOpenVideoAction implements Action {

  private Context context;
  private Video video;
  private boolean autoDownload = false;

  public ProtoOpenVideoAction(Context context, Video videoBean) {
    this.context = context;
    this.video = videoBean;
  }

  public ProtoOpenVideoAction setAutoDownload(boolean autoDownload) {
    this.autoDownload = autoDownload;
    return this;
  }

  @Override
  public void execute() {
    // If there is only one episode , open detail page directly.
//    if (video.getTotalEpisodesNum() == 1) {
//      List<DemoEpisode> episodeList = DemoDetailController.getEpisodeList(video);
//      if (episodeList != null && episodeList.size() == 1) {
//        String provider = video.getVideoEpisodesList().get(0).getPlayInfosList().get(0).
//            getProvider();
//        if (TextUtils.equals(provider, "youtube")) {
//          DemoEpisode episode = episodeList.get(0);
//          new OpenEpisodeDetailAction(context, episode.getVideoTitle(),
//              episode.getYoutubeId(), String.valueOf(episode.getId()))
//              .setDetailParam(video.getDetailParam()).setAutoDownload(autoDownload)
//              .execute();
//        } else {
//          new OpenEpisodeDetailAction(context,
//              video.getTitle(),
//              video.getVideoEpisodesList().get(0).getPlayInfosList().get(0).getUrlsList().get(0))
//          .setDetailParam(video.getDetailParam()).setAutoDownload(autoDownload).execute();
//        }
//      }
//    }
  }
}

