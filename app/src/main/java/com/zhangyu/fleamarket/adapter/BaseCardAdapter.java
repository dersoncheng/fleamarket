package com.zhangyu.fleamarket.adapter;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.wandoujia.base.utils.SystemUtil;
import com.wandoujia.mvc.BaseController;
import com.wandoujia.mvc.BaseModel;
import com.wandoujia.mvc.BaseView;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.utils.ViewUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param <M> model type.
 * @author match@wandoujia.com (Diao Liu)
 */
public abstract class BaseCardAdapter<M extends BaseModel>
    extends DataAdapter<M> {

  public static final String TAG = BaseCardAdapter.class.getSimpleName();

  private static final int TAG_KEY_CONTROLLER = R.id.card_controller;
  private static final long MIN_CARD_SHOW_INTERVAL_MS = 10000;

  public Map<Long, Long> cardShownMap = new HashMap<Long, Long>();

  private void logCardShowIfEligible(final View view, final M model, final int position) {
    if (!needLogCardShow()) {
      return;
    }
    if (!SystemUtil.aboveApiLevel(Build.VERSION_CODES.HONEYCOMB)) {
      // @match: give up devices below 3.0 because of a bad performance
      return;
    }
    final long itemId = getItemId(position);
    final Long lastTime = cardShownMap.get(itemId);
    final long currentTime = System.currentTimeMillis();
    if (lastTime == null || currentTime - lastTime > MIN_CARD_SHOW_INTERVAL_MS) {
      // @match: do a post because the view maybe is not attached to decorView right now
      FleaMarketApplication.getUIThreadHandler().post(new Runnable() {
        @Override
        public void run() {
          if (ViewUtils.isViewAttachedToDecorView(view)) {
            try {
              doSendCardShowLog(view, model, position);
            } catch (RuntimeException e) {
              throw new RuntimeException(e.getMessage() + "\n" + "itemId is : " + itemId);
            }
          }
        }
      });
      cardShownMap.put(itemId, currentTime);
    }
  }

  @Override
  public void setData(List<M> modelList) {
    super.setData(modelList);
    if (modelList == null || modelList.isEmpty()) {
      Log.d(TAG, "set data : list is null or empty");
    } else {
      Log.d(TAG, "set data : model class is " + modelList.get(0).getClass().getSimpleName());
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    BaseController cardController;
    BaseView baseView;
    if (convertView instanceof BaseView) {
      baseView = (BaseView) convertView;
      cardController = (BaseController) baseView.getView().getTag(TAG_KEY_CONTROLLER);
    } else {
      baseView = newView(position, getItem(position), parent);
      cardController = newController(position, getItem(position));
      baseView.getView().setTag(TAG_KEY_CONTROLLER, cardController);
    }
    Log.d("mixed search", "get view position ：" + position
        + " model : " + getItem(position).getClass() + " controller : " + cardController.getClass());
    cardController.bind(baseView, getItem(position));
//    LogManager.setIndexPackage(baseView.getView(), new ViewLogPackage.IndexPackage.Builder().index(position)
//      .build());

    logCardShowIfEligible(baseView.getView(), getItem(position), position);
    return baseView.getView();
  }

  /**
   * Whether need to log card show.<br/>
   * If return true, you must make sure the {@link android.widget.Adapter#getItemId(int)} method
   * returns a unique id for every position.
   */
  protected boolean needLogCardShow() {
    return false;
  }

  /**
   * This method is called in UI thread.
   *
   * @param showingView
   * @param model
   * @param position
   */
  protected void doSendCardShowLog(View showingView, M model, int position) {
    //FleaMarketApplication.getLogManager().logCardShow(showingView);
  }

  protected abstract BaseView newView(int position, M model, ViewGroup parent);

  protected abstract BaseController newController(int position, M model);
}
