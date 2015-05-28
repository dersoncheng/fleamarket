package com.zhangyu.fleamarket.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.wandoujia.log.toolkit.LaunchLogger;
import com.zhangyu.fleamarket.app.ApplicationInitor;
import com.zhangyu.fleamarket.app.FleaMarketApplication;
import com.zhangyu.fleamarket.configs.Config;
import com.zhangyu.fleamarket.utils.NavigationManager;
import com.zhangyu.fleamarket.utils.ViewUtils;

public abstract class BaseActivity extends SherlockFragmentActivity {

  private LaunchLogger launcherLogger;
  private boolean isDisplayed = false;
  private boolean hasActionBar = true;
  public static int activitiesInStack;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    ++activitiesInStack;

    //launcherLogger = FleaMarketApplication.getLogManager().getLaunchLogger();
    //launcherLogger.activityOnCreate(this, getIntent(), savedInstanceState);

    super.onCreate(savedInstanceState);
    hasActionBar = (getSherlock().getActionBar() != null);
    if (hasActionBar) {
      //LogManager.setModuleTag(getSupportActionBar().getActionView(), LogModule.ACTION_BAR);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    //FleaMarketApplication.getLogManager().logPageShow(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onRestart() {
    //launcherLogger.activityOnRestart(this, getIntent());
    super.onRestart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    FleaMarketApplication.setCurrentActivity(this);
    ViewUtils.resumeAsyncImagesLoading(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    FleaMarketApplication.setCurrentActivity(null);
    ViewUtils.pauseAsyncImagesLoading(this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    launcherLogger.activityOnNewIntent(this, intent);

    super.onNewIntent(intent);
  }

  @Override
  protected void onUserLeaveHint() {
    launcherLogger.activityOnUserLeave(this);

    super.onUserLeaveHint();
  }

  @Override
  protected void onDestroy() {
    --activitiesInStack;
    launcherLogger.activityOnDestroy(this);

    super.onDestroy();
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (!isDisplayed && hasFocus) {
      isDisplayed = true;
      onWindowDisplayed();
    }
  }

  /**
   * The windows was displayed.
   */
  public void onWindowDisplayed() {
    ApplicationInitor.initApplication();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
//    if (hasActionBar) {
//      View actionView = getSupportActionBar().getActionView();
//      ViewPackage.Element element;
//      String name;
//      if (item.hasSubMenu()) {
//        element = ViewPackage.Element.SPINNER;
//      } else {
//        element = ViewPackage.Element.MENU_ITEM;
//      }
//      if (item.getItemId() == android.R.id.home) {
//        name = "BACK";
//      } else {
//        name = item.getTitle().toString();
//      }
//      LogManager.setViewPackageTag(actionView, element, null, name);
//      FleaMarketApplication.getLogManager().logClick(actionView);
//    }
    return onMenuItemSelected(item);
  }

  public boolean onMenuItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
        break;
    }
    return false;
  }

  @Override
  public void onBackPressed() {
    if (hasActionBar) {
      View actionView = getSupportActionBar().getActionView();
      //LogManager.setViewPackageTag(actionView, ViewPackage.Element.BACK, null, "BACK");
      //FleaMarketApplication.getLogManager().logClick(actionView);
    }
    try {
      /**
       * If this activity is root activity, try to goto the ENTRY activity.
       */
      if (this.isTaskRoot()) {
        Intent backIntent = createBackIntent();
        if (backIntent != null) {
          NavigationManager.safeStartActivity(this, backIntent);
          finish();
          return;
        }
      }
      super.onBackPressed();
    } catch (IllegalStateException e) {
      // as a known issue about support library, just try catch here
      // http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-
      // not-perform-this-action-after-onsa
      e.printStackTrace();
    }
  }

  /**
   * Create intent for root activity's leave-activity.
   *
   * @return If return null, means leave at once, else return the intent to goto leave-activity.
   */
  protected Intent createBackIntent() {
    if (Config.CONFIG_MAIN_ACTIVITY_CLASS == null) {
      return null;
    }
    if (Config.CONFIG_MAIN_ACTIVITY_CLASS.equals(getClass())) {
      // TODO(duguguiyu): Need check action or intent?
      return null;
    }

    // TODO(duguguiyu) Add more stat info, such as original source, special action ...
    Intent backIntent = new Intent(this, Config.CONFIG_MAIN_ACTIVITY_CLASS);
    backIntent.putExtra(LaunchLogger.EXTRA_LAUNCH_FROM, "force_launch");
    backIntent.putExtra(LaunchLogger.EXTRA_LAUNCH_KEYWORD, getClass().getSimpleName());
    return backIntent;
  }
}
