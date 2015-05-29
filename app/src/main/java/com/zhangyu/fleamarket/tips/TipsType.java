package com.zhangyu.fleamarket.tips;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wandoujia.base.utils.SystemUtil;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.utils.ViewUtils;

/**
 * @author zhulantian@wandoujia.com (Lantian)
 */
public enum TipsType {

  LOADING {

    @Override
    protected Tips createTips(Context context) {
      View tipsView = LayoutInflater.from(context)
          .inflate(R.layout.flea_cycle_loading, new FrameLayout(context), false);
      return new Tips(tipsView);
    }
  },
  LOADING_TOP {
    @Override
    protected Tips createTips(Context context) {
      View tipsView = LayoutInflater.from(context)
          .inflate(R.layout.flea_cycle_loading_top, new FrameLayout(context), false);
      return new Tips(tipsView);
    }
  },
  NO_NETWORK {

    @Override
    protected Tips createTips(Context context) {
      View tipsView = LayoutInflater.from(context)
          .inflate(R.layout.aa_no_network, null, false);
      Button networkSettingButton = (Button) tipsView.findViewById(R.id.set_network);
      networkSettingButton.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          if (SystemUtil.aboveApiLevel(Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
            v.getContext().startActivity(new Intent(Settings.ACTION_SETTINGS));
          } else {
            v.getContext().startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
          }
        }
      });
      return new Tips(tipsView);
    }
  },
  NO_NETWORK_FLOATING {

    @Override
    protected Tips createTips(Context context) {
      Tips tips = new Tips(context, R.layout.flea_tips_no_network, false);
      tips.view.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {}
      });
      return tips;
    }
  },
  NO_SEARCH_RESULT {

    @Override
    protected Tips createTips(Context context) {
      View noSearchResultView = LayoutInflater.from(context).inflate(R.layout.aa_no_search_result,
          null, false);
      return new Tips(noSearchResultView);
    }
  },
  FETCH_FAILED_FLOATING {

    @Override
    protected Tips createTips(Context context) {
      return new Tips(context, R.layout.flea_tips_refresh_failed, false);
    }
  };

  private int layoutRes;

  private TipsType() {}

  private TipsType(int layoutRes) {
    this.layoutRes = layoutRes;
  }

  protected Tips createTips(Context context) {
    return new Tips(context, layoutRes);
  }
}
