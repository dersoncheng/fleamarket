package com.zhangyu.fleamarket.card;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wandoujia.mvc.BaseView;


/**
 * @author match@wandoujia.com (Diao Liu)
 */
public interface BaseDownloadProgressView extends BaseView {
  TextView getSpeedView();

  TextView getSizeView();

  ProgressBar getProgressView();

  TextView getStatusView();

  View getAnchorView();
}
