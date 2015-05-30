package com.zhangyu.fleamarket.model;

import com.wandoujia.em.common.proto.Channel;
import com.wandoujia.em.common.proto.PlayList;
import com.wandoujia.em.common.proto.Video;
import com.zhangyu.fleamarket.card.CardViewModel;


/**
 * utils of VideoCardModel.
 *
 * @author wheam@wandoujia.com (Qi Zhang)
 */
public class DemoCardModelUtils {

  private DemoCardModelUtils() {
  }

  /**
   * convert video card model which uses CardViewModelYoutubeVideoImpl.
   *
   * @param video the current NetVideoInfo
   * @param type  the ModelType
   * @return VideoCardModel instance
   */
  public static DemoCardModel convertFromVideo(final Video video, final CardViewModel.ModelType type) {
    return new DemoCardModel() {
      @Override
      public CardViewModel getCardViewModel() {
        return null;//new ActionCardViewProtoModelVideoImpl(video, type);
      }

//      @Override
//      public DownloadableModel getDownloadableModel() {
//        return DownloadableModelUtil.convertFromVideo(video);
//      }
//
//      @Override
//      public DemoCardModel getVideoModel() {
//        return VideoModelUtil.convertFromVideo(video);
//      }
//
//      @Override
//      public DemoCardModel getVideoLogModel() {
//        return VideoLogModelUtil.convertFromVideo(video);
//      }
    };
  }

  public static DemoCardModel convertFromVideoDownload(final Video video, final CardViewModel.ModelType type) {
    return new DemoCardModel() {
      @Override
      public CardViewModel getCardViewModel() {
        return null;
//        return new ActionCardViewProtoModelVideoImpl(video) {
//          @Override
//          public CharSequence getSubTitle(TextView subTitleView) {
//            if (type == CardViewModel.ModelType.MP3DOWNLOAD) {
//              return String.format(
//                  FleaMarketApplication.getAppContext().getResources()
//                      .getQuantityString(R.plurals.download_count,
//                          video.getWeeklyDownloadCount().intValue()),
//                  SnapTubeUtils.formatEnglishNumString(video.getMp3WeeklyDownloadCount(),
//                      FleaMarketApplication.getAppContext()));
//            }
//            return String.format(
//                FleaMarketApplication.getAppContext().getResources()
//                    .getQuantityString(R.plurals.download_count,
//                        video.getWeeklyDownloadCount().intValue()),
//                SnapTubeUtils.formatEnglishNumString(video.getWeeklyDownloadCount(),
//                    FleaMarketApplication.getAppContext()));
//          }
//        };
      }

//      @Override
//      public DownloadableModel getDownloadableModel() {
//        return DownloadableModelUtil.convertFromVideo(video);
//      }
//
//      @Override
//      public DemoCardModel getVideoModel() {
//        return VideoModelUtil.convertFromVideo(video);
//      }
//
//      @Override
//      public DemoCardModel getVideoLogModel() {
//        return VideoLogModelUtil.convertFromVideo(video);
//      }
    };
  }

  /**
   * convert video card model which uses CardViewModelYoutubeVideoImpl.
   *
   * @param playList
   * @return VideoCardModel instance
   */
  public static DemoCardModel convertFromPlayList(final PlayList playList) {
    return new DemoCardModel() {
      @Override
      public CardViewModel getCardViewModel() {
        return null;//new CardViewModelPlayListImpl(playList);
      }

//      @Override
//      public DownloadableModel getDownloadableModel() {
//        return null;
//      }
//
//      @Override
//      public VideoModel getVideoModel() {
//        return VideoModelUtil.convertFromPlayList(playList);
//      }
//
//      @Override
//      public VideoLogModel getVideoLogModel() {
//        return null;
//      }
    };
  }

  /**
   * convert video card model which uses CardViewModelYoutubeVideoImpl.
   *
   * @param channels
   * @return VideoCardModel instance
   */
  public static DemoCardModel convertFromChannel(final Channel channels) {
    return new DemoCardModel() {
      @Override
      public CardViewModel getCardViewModel() {
        return null;//new CardViewModelChannelsImpl(channels);
      }

//      @Override
//      public DownloadableModel getDownloadableModel() {
//        return null;
//      }
//
//      @Override
//      public VideoModel getVideoModel() {
//        return VideoModelUtil.convertFromChannels(channels);
//      }
//
//      @Override
//      public VideoLogModel getVideoLogModel() {
//        return null;
//      }
    };
  }

}
