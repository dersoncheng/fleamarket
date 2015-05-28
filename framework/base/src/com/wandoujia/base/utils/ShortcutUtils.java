package com.wandoujia.base.utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.wandoujia.base.log.Log;

/**
 * Created by zhangxiaobo on 14-03-01.
 */
public class ShortcutUtils {

  private static final String ACTION_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
  private static final String KEY_DUPLICATE = "duplicate";

  private static final String TAG = "ShortcutUtil";

  private static final int POOL_SIZE = 2;

  private static ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(POOL_SIZE);

  private ShortcutUtils() {}

  public interface ActionListener {
    /** The operation succeeded */
    public void onSuccess();

    /**
     * The operation failed
     */
    public void onFailure(int reason);
  }

  /**
   * Create a shortcut with given information
   *
   *
   * @param name
   * @param iconId
   * @param intent
   * @param context
   */
  public static void createShortcut(final String name, final int iconId, final Intent intent,
      final Context context) {
    createShortcut(name, iconId, intent, context, null);
  }

  public static void createShortcut(final String name, final int iconId, final Intent intent,
      final Context context, final ActionListener actionListener) {
    EXECUTOR.execute(new Runnable() {
      @Override
      public void run() {
        boolean exists = ShortcutUtils.hasShortcut(context, name);
        if (!exists) {
          addShortcut(name, iconId, context, intent);
          if (actionListener != null) {
            actionListener.onSuccess();
          }
        } else {
          Log.d(TAG, "shortcut exists");
          if (actionListener != null) {
            actionListener.onFailure(-1);
          }
        }
      }
    });
  }

  public static boolean addShortcut(String title, int iconId, Context context, Intent intent) {
    Intent shortcut = new Intent(ACTION_SHORTCUT);
    shortcut.putExtra(KEY_DUPLICATE, false);
    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
    shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON,
        BitmapFactory.decodeResource(context.getResources(), iconId));
    shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
        Intent.ShortcutIconResource.fromContext(context, iconId));

    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    Log.d(TAG, "add shortcut " + title);
    context.sendBroadcast(shortcut);
    return true;
  }

  public static boolean hasShortcut(Context cx, String title) {
    boolean result = false;

    if (TextUtils.isEmpty(title)) {
      Log.w(TAG, "empty shortcut title");
      return false;
    }

    final String authority =
        LauncherUtil.getAuthorityFromPermission(cx, LauncherUtil.READ_SETTINGS);
    if (TextUtils.isEmpty(authority)) {
      // if exception return true, so it don't create shortcut
      return false;
    }

    Log.d(TAG, authority);

    Cursor cursor = null;
    try {
      final Uri CONTENT_URI = Uri.parse("content://" + authority + "/favorites?notify=true");
      cursor = cx.getContentResolver().query(CONTENT_URI, null,
          "title=?", new String[] {title}, null);

      if (cursor != null && cursor.getCount() > 0) {
        result = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return result;
  }

}
