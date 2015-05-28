package com.zhangyu.fleamarket.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.wandoujia.base.utils.SystemUtil;
import com.zhangyu.fleamarket.activity.BaseActivity;
import com.zhangyu.fleamarket.configs.Const;
import com.zhangyu.fleamarket.http.client.FleaMarketDataClient;
import com.zhangyu.fleamarket.utils.FileCacheStrategy;
import com.zhangyu.fleamarket.utils.FleaMarketUtils;

import java.io.File;
import java.lang.ref.WeakReference;

public class FleaMarketApplication extends Application {
  private static WeakReference<Activity> currentActivity;
  private static Context context;
  private static FleaMarketDataClient dataClient;
  private static final String DATA_CACHE_FOLDER = "DataCache";
  private static final String IMAGE_CACHE_FOLDER = "ImageCache";

  @Override
  public void onCreate() {
    super.onCreate();
    Const.FLEAMARKET_PACKAGE_NAME = getPackageName();
    doInit();
  }

  private void doInit() {
    context = this;
    String processName = FleaMarketUtils.getProcessName(context);
    if (FleaMarketUtils.isFleaMarket(context, processName)) {
      initMainProcess();
    }
  }

  private void initMainProcess() {
  }

  public static Context getAppContext() {
    return context;
  }

  public static FleaMarketApplication getInstance() {
    return (FleaMarketApplication) context;
  }

  public static void setCurrentActivity(BaseActivity baseActivity) {
    currentActivity = new WeakReference<Activity>(baseActivity);
    //getInstance().getLogListener().logActivityStart(baseActivity);
  }

  public static synchronized FleaMarketDataClient getDataApi() {
    if (dataClient == null) {
      String cacheDir;
      if (FileCacheStrategy.getStrategy().useExternalStorage()) {
        File externalCacheDir = SystemUtil.getDeviceExternalCacheDir();
        if (externalCacheDir != null) {
          cacheDir = externalCacheDir.getAbsolutePath() + "/" + DATA_CACHE_FOLDER;
        } else {
          cacheDir = context.getCacheDir() + "/" + DATA_CACHE_FOLDER;
        }
      } else {
        cacheDir = context.getCacheDir() + "/" + DATA_CACHE_FOLDER;
      }
      dataClient = new FleaMarketDataClient(cacheDir);
    }

    return dataClient;
  }
}
