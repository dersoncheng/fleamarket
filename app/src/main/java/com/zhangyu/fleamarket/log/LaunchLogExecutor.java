package com.zhangyu.fleamarket.log;

import android.app.Activity;
import android.content.Intent;

import com.wandoujia.log.toolkit.LaunchLogger;
import com.wandoujia.log.toolkit.model.LaunchPackage;
import com.wandoujia.log.toolkit.model.LaunchSourcePackage;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class LaunchLogExecutor implements LaunchLogger.Executor {
  public class LaunchSource {
    public static final String SHORTCUT = "shortcut";
  }

  @Override
  public LaunchSourcePackage getLaunchSource(Activity activity, Intent intent) {
    return null;
  }

  @Override
  public void logAsLaunch(LaunchPackage launchPackage) {
    //FleaMarketApplication.getLogManager().logLaunch(launchPackage);
  }
}
