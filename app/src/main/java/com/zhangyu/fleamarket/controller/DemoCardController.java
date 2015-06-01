package com.zhangyu.fleamarket.controller;

import com.wandoujia.em.common.proto.Video;
import com.wandoujia.mvc.BaseController;
import com.zhangyu.fleamarket.card.controllers.HideCoverCardViewController;
import com.zhangyu.fleamarket.model.DemoCardModel;
import com.zhangyu.fleamarket.view.ContentCardView;

public class DemoCardController implements
  BaseController<ContentCardView, DemoCardModel> {

  private HideCoverCardViewController cardViewController = new HideCoverCardViewController();
//  private DownloadableProgressController downloadableProgressController =
//      new DownloadableProgressController();
//  private DemoButtonController demoButtonController = new DemoButtonController();


  @Override
  public void bind(ContentCardView view, DemoCardModel model) {
    if (view.getCardView() != null && model != null) {
      cardViewController.bind(view.getCardView(), model.getCardViewModel());
//      view.getCardView().getIconTipsView().setText(getDuration(model.getVideoModel().getVideo()));
    }
//    if (view.getDownloadProgress() != null && model != null) {
//      downloadableProgressController.bind(view.getDownloadProgress(), model.getDownloadableModel());
//    }
//    if (view.getButton() != null && model != null) {
//      demoButtonController.bind(view.getImageView(), model.getVideoModel());
//    }
  }

  private String getDuration(Video video) {
    if (video != null && video.getVideoEpisodesList() != null && video.getVideoEpisodesList().get(0) != null) {
      return video.getVideoEpisodesList().get(0).getDuration();
    }
    return null;
  }
}
