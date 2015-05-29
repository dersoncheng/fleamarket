package com.zhangyu.fleamarket.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.wandoujia.image.view.AsyncImageView;
import com.zhangyu.fleamarket.R;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class ViewUtils {
  private ViewUtils() {
  }

  public static void enableScrollToTop(AbsListView listView) {
    if (listView == null) {
      return;
    }
    listView.setTag(R.id.list_view_scroll_to_top, true);
  }

  public static View newInstance(ViewGroup parent, int resId) {
    return LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
  }

  public static View newInstance(Context context, int resId) {
    return LayoutInflater.from(context).inflate(resId, null);
  }

  public static void resumeAsyncImagesLoading(Activity activity) {
    View rootView = activity.findViewById(android.R.id.content);
    processAsyncImagesLoadingInternal(rootView, true);
  }

  public static void pauseAsyncImagesLoading(Activity activity) {
    View rootView = activity.findViewById(android.R.id.content);
    processAsyncImagesLoadingInternal(rootView, false);
  }

  private static void processAsyncImagesLoadingInternal(View view, boolean pause) {
    if (view instanceof AsyncImageView) {
      AsyncImageView imageView = (AsyncImageView) view;
      if (pause && imageView.getStatus() != AsyncTask.Status.FINISHED) {
        imageView.resumeLoading();
      } else if (imageView.getStatus() != AsyncTask.Status.FINISHED) {
        imageView.pauseLoading();
      }
    } else if (view instanceof ViewGroup) {
      ViewGroup parent = (ViewGroup) view;
      for (int i = 0; i < parent.getChildCount(); ++i) {
        View child = parent.getChildAt(i);
        processAsyncImagesLoadingInternal(child, pause);
      }
    }
  }

  @SuppressWarnings("deprecation")
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static void setBackground(View view, Drawable background) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      view.setBackground(background);
    } else {
      view.setBackgroundDrawable(background);
    }
  }
}
