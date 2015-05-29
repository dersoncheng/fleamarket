package com.zhangyu.fleamarket.tips;

import android.view.View;
import android.widget.TextView;

import com.wandoujia.rpc.http.exception.HttpExceptionUtils;
import com.zhangyu.fleamarket.R;

/**
 * @author zhulantian@wandoujia.com (Lantian)
 */
public class FetchFailedTipsUtil {

  /**
   * Show fetch failed tips view, the tips view will show at the buttom of the target view.
   *
   * @param targetView the target view to show tips.
   * @param listener   retry listener.
   * @param t          fetch failed reason.
   */
  public static void showFetchFailedTipsFloating(View targetView,
                                                 View.OnClickListener listener, Throwable t) {
    View tipsView = TipsViewUtil.showTipsView(targetView, TipsType.FETCH_FAILED_FLOATING);
    TextView messageView = (TextView) tipsView.findViewById(R.id.content_message);
    String defaultVal = targetView.getContext().getString(R.string.tips_refresh_failed);
    String message = HttpExceptionUtils.getMessage(t, defaultVal);
    messageView.setText(message);
    tipsView.setOnClickListener(listener);
  }

  public static void hideFetchFailedFloatingTipsView(View targetView) {
    TipsViewUtil.hideTipsView(targetView, TipsType.FETCH_FAILED_FLOATING);
  }
}
