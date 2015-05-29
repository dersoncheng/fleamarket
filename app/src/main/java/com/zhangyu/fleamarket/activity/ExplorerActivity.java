package com.zhangyu.fleamarket.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.fragment.PictureListFragment;

public class ExplorerActivity extends BaseActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleIntent(getIntent());
  }

  private void handleIntent(Intent intent) {
    if (intent == null) {
      return;
    }
    navigateToItemInternal(null);
  }

  private void navigateToItemInternal(Bundle extras) {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    Fragment currentFragment = fm.findFragmentById(android.R.id.content);
    if (currentFragment instanceof PictureListFragment) {
      return;
    } else {
      PictureListFragment pictureListFragment = PictureListFragment.newInstance();
      if (extras != null) {
        pictureListFragment.setArguments(extras);
      }
      ft.replace(android.R.id.content, pictureListFragment);
    }
    ft.commitAllowingStateLoss();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, R.id.menu_share, Menu.FIRST, R.string.share)
      .setIcon(R.drawable.menu_icon_share)
      .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return super.onCreateOptionsMenu(menu);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_share) {
    }
    return super.onOptionsItemSelected(item);
  }
}
