package com.zhangyu.fleamarket.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;

import com.wandoujia.image.view.AsyncImageView;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class ViewUtils {
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
}
