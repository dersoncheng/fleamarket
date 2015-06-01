package com.zhangyu.fleamarket.http.protoapi;

import android.view.View;
import android.widget.TextView;

import com.wandoujia.em.common.proto.Video;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.action.Action;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.card.CardViewModel;
import com.zhangyu.fleamarket.card.models.SimpleActionCardViewModel;
import com.zhangyu.fleamarket.utils.FleaMarketUtils;
import com.zhangyu.fleamarket.view.button.ButtonState;
import com.zhangyu.fleamarket.view.button.StatefulButton;

/**
 * Created by niejunhong on 14-10-30.
 */
public class ActionCardViewProtoModelDemoImpl extends SimpleActionCardViewModel {

  private Video video;
  private CardViewModel.ModelType type;

  public ActionCardViewProtoModelDemoImpl(Video video) {
    this.video = video;
  }

  public ActionCardViewProtoModelDemoImpl(Video video, CardViewModel.ModelType type) {
    this.video = video;
    this.type = type;
  }

  @Override
  public CharSequence getTitle(TextView titleView) {
    return video.getTitle();
  }

  @Override
  public CharSequence getSubTitle(TextView subTitleView) {
    if (this.type != null && (this.type == ModelType.MUSIC || this.type == ModelType.SEARCH)) {
      String result = String.format(
          FleaMarketApplication.getAppContext().getResources()
              .getQuantityString(R.plurals.view_count, video.getPlayCount().intValue()),
              FleaMarketUtils.formarVideoCount(video.getPlayCount()));
      return result + " " + FleaMarketUtils.formatTime(video.getUpdateDate());
    } else if (this.type != null && this.type == ModelType.MP3DOWNLOAD) {
      String result = String.format(
          FleaMarketApplication.getAppContext().getResources()
              .getQuantityString(R.plurals.view_count, video.getMp3WeeklyDownloadCount().intValue()),
              FleaMarketUtils.formarVideoCount(video.getPlayCount()));
      return result;
    } else {
      String result = String.format(
          FleaMarketApplication.getAppContext().getResources()
              .getQuantityString(R.plurals.view_count, video.getPlayCount().intValue()),
              FleaMarketUtils.formarVideoCount(video.getPlayCount()));
      return result;
    }
  }

  @Override
  public String getBanner() {
    if (video.getPictures() != null && video.getPictures().getLargesList() != null
        && !video.getPictures().getLargesList().isEmpty()) {
      return video.getPictures().getLargesList().get(0);
    } else {
      return null;
    }
  }

  @Override
  public String getIcon() {
    if (video.getPictures() != null && video.getPictures().getSmallsList() != null
        && !video.getPictures().getSmallsList().isEmpty()) {
      return video.getPictures().getSmallsList().get(0);
    } else {
      return null;
    }
  }

  @Override
  public Action getCardAction(View cardView) {
    return new ProtoOpenVideoAction(cardView.getContext(), video);
  }

  @Override
  public ButtonState getButtonState(StatefulButton button) {
    return new ButtonState(R.attr.state_highLight, R.string.download, new ProtoOpenVideoAction(
        button.getContext(), video).setAutoDownload(true));
  }

  @Override
  public String getID() {
    return String.valueOf(video.getId());
  }
}

