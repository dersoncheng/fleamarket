package com.wandoujia.base.view;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * see http://stackoverflow.com/questions/3130654/memory-leak-in-webview/8949378#8949378 and
 * http://code.google.com/p/android/issues/detail?id=9375
 * Note that the bug does NOT appear to be fixed in android 2.2 as romain claims
 * <b>
 * Also, you must call {@link #destroy()} from your activity's onDestroy method.
 * </b>
 * 
 * <p>
 * sub class can override {@link #shouldJumpToBrowser} to decide if a url need to open in the web
 * browser(default is false)
 * </p>
 * 
 * @author yangkai@wandoujia.com
 */
public class NonLeakingWebView extends WebView {

  private static final String TAG = "NonLeakingWebView";

  public NonLeakingWebView(Context context) {
    super(context.getApplicationContext());
    init();
  }

  public NonLeakingWebView(Context context, AttributeSet attrs) {
    super(context.getApplicationContext(), attrs);
    init();
  }

  public NonLeakingWebView(Context context, AttributeSet attrs, int defStyle) {
    super(context.getApplicationContext(), attrs, defStyle);
    init();
  }

  private void init() {
    setWebViewClient(new NonLeakingWebViewClient());

    // WebView的context需要是activity，否则在onSavePassword时，试图弹出Dialog，而此时的context的 window
    // token无效。
    //
    // 但是这个BUG与http://stackoverflow.com/questions/3130654/memory-leak-in-webview/8949378#8949378
    // 是矛盾的
    // 也就是说，为了解决WebView在4.2以下都存在的内存泄露问题，建议将WebView的context设为applicationContext；
    // 但这个bug却要求context必须是activity
    //
    // 采取折中方案：关闭webview的自动保存密码功能(https://code.google.com/p/android/issues/detail?id=9375)
    //
    // 但从webview的代码上来看，仍有BUG： 当webview试图渲染http的select标签时，会生成一个WebView#InvokeListBox，它其实也是一个dialog
    // <strong>因此，本类的webview无法正确渲染带select标签的页面。</strong>
    getSettings().setSavePassword(false);

    // Set scrollbar style here to fix a android bug, see
    // http://stackoverflow.com/questions/3998916/android-webview-leaves-space-for-scrollbar
    // for more details
    setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
  }

  /**
   * if a url need to open in the web browser(default is false)
   * 
   * @return return<code>true</code> to open in browser, <code>false</code>，open in webView self
   */
  protected boolean shouldJumpToBrowser(String url) {
    return false;
  }

  protected class NonLeakingWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
      if (shouldJumpToBrowser(url)) {
        try {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          getContext().startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
          Log.e(TAG, "fail to go to url in NonLeakingWebViewClient.shouldOverrideUrlLoading",
              ignored);
        } catch (RuntimeException ignored) {
          Log.e(TAG, "fail to go to url in NonLeakingWebViewClient.shouldOverrideUrlLoading",
              ignored);
        }
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    try {
      super.onDraw(canvas);
    } catch (WindowManager.BadTokenException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
  }
}
