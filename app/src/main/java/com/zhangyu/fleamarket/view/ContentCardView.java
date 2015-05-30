package com.zhangyu.fleamarket.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wandoujia.image.view.AsyncImageView;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.card.BaseCardView;
import com.zhangyu.fleamarket.card.BaseContentCardView;
import com.zhangyu.fleamarket.card.BaseDownloadProgressView;
import com.zhangyu.fleamarket.utils.ViewUtils;
import com.zhangyu.fleamarket.view.button.BaseButton;
import com.zhangyu.fleamarket.view.button.BaseImageView;
import com.zhangyu.fleamarket.view.button.StatefulButton;
import com.zhangyu.fleamarket.view.button.StatefulImageView;
import com.zhangyu.fleamarket.view.button.SubActionButton;


/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class ContentCardView extends RelativeLayout implements BaseContentCardView {

  private TextView title;
  private TextView subTitle;
  private AsyncImageView icon;
  private AsyncImageView banner;
  private TextView description;
  private TextView tag;
  private ImageView badge;
  protected StatefulButton actionButton;
  protected SubActionButton subActionButton;
  private TextView downloadSpeed;
  private TextView downloadStatus;
  private TextView downloadSize;
  private TextView iconTips;
  private StatefulImageView actionImageView;
  private ProgressBar downloadProgress;
  private int progressAnchorId;

  public ContentCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs,
        R.styleable.DownloadableProgressView, 0, 0);
    progressAnchorId = a.getResourceId(
        R.styleable.DownloadableProgressView_progress_anchorView, 0);
    a.recycle();
  }




  /**
   * New instance inflated with R.layout.card_item_downloadable_mini. It's used for downloads and
   * apps
   * in my things page. It has download progress.
   *
   * @param parent parent
   * @return ContentCardView
   */
  public static ContentCardView newInstanceMiniDownloadable(ViewGroup parent) {
    return (ContentCardView) ViewUtils.newInstance(parent,
      R.layout.card_item_downloadable_mini);
  }


  /**
   * New instance inflated with R.layout.card_item_cover_mini. It's used for videos and
   * books in my things page. It doesn't have download progress.
   *
   * @param parent parent
   * @return ContentCardView
   */
  public static ContentCardView newInstanceCoverMini(ViewGroup parent) {
    return (ContentCardView) ViewUtils.newInstance(parent,
        R.layout.card_item_cover_mini);
  }


  /**
   * New instance inflated with R.layout.card_item_short_video. It's used for video other tab which
   * has no download progress.
   *
   * @param parent parent
   * @return ContentCardView
   */
  public static ContentCardView newInstanceShortVideo(ViewGroup parent) {
    return (ContentCardView) ViewUtils.newInstance(parent, R.layout.card_item_short_video);
  }

  public static ContentCardView newInstanceTopVideo(ViewGroup parent) {
    return (ContentCardView) ViewUtils.newInstance(parent, R.layout.card_item_top_video);
  }


  /**
   * New instance inflated with R.layout.card_item_youtube_video. It's used for youtube videos.
   *
   * @param parent parent
   * @return ContentCardView
   */
  public static ContentCardView newInstanceYoutubeVideo(ViewGroup parent) {
    return (ContentCardView) ViewUtils.newInstance(parent,
        R.layout.card_item_youtube_video);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    title = (TextView) findViewById(R.id.title);
    subTitle = (TextView) findViewById(R.id.sub_title);
    icon = (AsyncImageView) findViewById(R.id.icon);
    iconTips = (TextView) findViewById(R.id.icon_tips);
    banner = (AsyncImageView) findViewById(R.id.banner);
    badge = (ImageView) findViewById(R.id.badge);
    actionButton = (StatefulButton) findViewById(R.id.action_button);
    actionImageView = (StatefulImageView) findViewById(R.id.action_imageview);
    subActionButton = (SubActionButton) findViewById(R.id.sub_action_button);
    downloadSpeed = (TextView) findViewById(R.id.download_speed);
    downloadStatus = (TextView) findViewById(R.id.download_status);
    downloadSize = (TextView) findViewById(R.id.download_size);
    downloadProgress = (ProgressBar) findViewById(R.id.download_progress);
    enlargeTagTouchArea();
    // enlargeButtonTouchArea();
  }

  // private void enlargeButtonTouchArea() {
  // if (actionButton == null) {
  // return;
  // }
  // final View parent = (View) actionButton.getParent();
  // parent.post(new Runnable() {
  // @Override
  // public void run() {
  // final Rect r = new Rect();
  // actionButton.getHitRect(r);
  // r.top -= r.height() * 0.5f;
  // r.bottom += r.height() * 0.5f;
  // parent.setTouchDelegate(new TouchDelegate(r, actionButton));
  // }
  // });
  // }

  private void enlargeTagTouchArea() {
    if (tag == null) {
      return;
    }
    final View parent = (View) tag.getParent();
    parent.post(new Runnable() {
      @Override
      public void run() {
        final Rect r = new Rect();
        tag.getHitRect(r);
        r.top -= r.height() * 2;
        parent.setTouchDelegate(new TouchDelegate(r, tag));
      }
    });
  }


  @Override
  public BaseCardView getCardView() {
    return new BaseCardView() {
      @Override
      public TextView getTitleView() {
        return title;
      }

      @Override
      public TextView getSubTitleView() {
        return subTitle;
      }

      @Override
      public AsyncImageView getIconView() {
        return icon;
      }

      @Override
      public TextView getIconTipsView() {
        return iconTips;
      }

      @Override
      public AsyncImageView getBannerView() {
        return banner;
      }

      @Override
      public TextView getDescriptionView() {
        return description;
      }

      @Override
      public TextView getTagView() {
        return tag;
      }

      @Override
      public ImageView getBadgeView() {
        return badge;
      }

      @Override
      public SubActionButton getSubActionButtonView() {
        return subActionButton;
      }

      @Override
      public View getView() {
        return ContentCardView.this;
      }
    };
  }

  @Override
  public BaseButton getButton() {
    return new BaseButton() {
      @Override
      public StatefulButton getButtonView() {
        return actionButton;
      }

      @Override
      public View getView() {
        return ContentCardView.this;
      }
    };
  }

  @Override
  public BaseImageView getImageView() {
    return new BaseImageView() {
      @Override
      public StatefulImageView getImageView() {
        return actionImageView;
      }

      @Override
      public View getView() {
        return ContentCardView.this;
      }
    };
  }

  @Override
  public BaseDownloadProgressView getDownloadProgress() {
    return new BaseDownloadProgressView() {
      @Override
      public TextView getSpeedView() {
        return downloadSpeed;
      }

      @Override
      public TextView getSizeView() {
        return downloadSize;
      }

      @Override
      public ProgressBar getProgressView() {
        return downloadProgress;
      }

      @Override
      public TextView getStatusView() {
        return downloadStatus;
      }

      @Override
      public View getAnchorView() {
        return findViewById(progressAnchorId);
      }

      @Override
      public View getView() {
        return ContentCardView.this;
      }
    };
  }

  @Override
  public View getView() {
    return this;
  }

  @Override
  protected void dispatchSetPressed(boolean pressed) {
    int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      // Children that are clickable on their own should not
      // show a pressed state when their parent view does.
      // Clearing a pressed state always propagates.
      if (!pressed || (!child.isClickable() && !child.isLongClickable())) {
        child.setPressed(pressed);
      }
    }
  }

}
