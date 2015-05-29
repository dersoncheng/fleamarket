package com.zhangyu.fleamarket.fragment;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.wandoujia.base.utils.NetworkUtil;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.receiver.ReceiverMonitor;
import com.zhangyu.fleamarket.tips.TipsType;
import com.zhangyu.fleamarket.tips.TipsViewUtil;

/**
 * AsyncLoadFragment that can handle network change and show network tips.
 *
 * @author xubin@wandoujia.com
 */
public abstract class NetworkAsyncLoadFragment extends AsyncLoadFragment {

  /**
   * flag of is loaded, when needToLoad() return true, the flag comes true.
   */
  private boolean isLoaded;

  private final ReceiverMonitor.NetworkChangeListener networkChangeListener =
      new ReceiverMonitor.NetworkChangeListener() {
        @Override
        public void onNetworkChange(NetworkInfo info) {
          // if not loaded, try load, otherwise show or update tips view directlty.
          if (!isLoaded) {
            requestLoad();
          } else {
            showOrHideNetworkTipView();
          }
        }
      };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ReceiverMonitor.getInstance().addNetworkChangeListener(networkChangeListener);
  }

  @Override
  protected boolean needToLoadData() {
    boolean needToLoad = false;
    final Context context = FleaMarketApplication.getAppContext();
    if (NetworkUtil.isWifiConnected(context) || NetworkUtil.isReverseProxyOn()) {
      needToLoad = true;
    } else if (NetworkUtil.isMobileNetworkConnected(context)) {
      needToLoad = true;
    }
    // show tips view at here, only first time to load data.
    if (!isLoaded) {
      showOrHideNetworkTipView();
    }
    isLoaded = needToLoad || isLoaded;
    return needToLoad;
  }

  protected void showOrHideNetworkTipView() {
    final Context context = FleaMarketApplication.getAppContext();
    if (NetworkUtil.isReverseProxyOn()) { // usb proxy on
      hideNoNetworkTipsView();
    } else if (NetworkUtil.isWifiConnected(context)) { // wifi connect
      hideNoNetworkTipsView();
    } else if (NetworkUtil.isMobileNetworkConnected(context)) { // mobile connect
      hideNoNetworkTipsView();
    } else { // no network
      showNoNetworkTipsView();
    }
  }

  protected void showNoNetworkTipsView() {
    if (getContentView() == null) {
      return;
    }
    TipsViewUtil.showTipsView(getContentView(), TipsType.NO_NETWORK_FLOATING);
  }

  protected void hideNoNetworkTipsView() {
    if (getContentView() == null) {
      return;
    }
    TipsViewUtil.hideTipsView(getContentView(), TipsType.NO_NETWORK_FLOATING);
  }
}
