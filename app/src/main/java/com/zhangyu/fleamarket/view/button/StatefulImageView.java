package com.zhangyu.fleamarket.view.button;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by niejunhong on 14-11-24.
 */
public class StatefulImageView extends ImageView {

  private static final String TAG = "StatefulImageView";
  private static final int EXTEND_TOUCH_LENGTH = 50;

  protected ButtonState state;

  public StatefulImageView(Context context) {
    super(context);
  }

  public StatefulImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public StatefulImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public ButtonState getState() {
    return state;
  }

  public void setState(final ButtonState state) {
    if (state == null) {
      Log.e(TAG, "The state cannot be null when setState.");
      return;
    }
    this.state = state;
    setEnabled(state.isEnable());
    setImageResource(state.getImageResour());
    setVisibility(state.getVisible());
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (state.getAction() != null) {
          state.getAction().execute();
        }
      }
    });
    refreshDrawableState();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    ((View) StatefulImageView.this.getParent()).post(new Runnable() {
      @Override
      public void run() {
        Rect delegateArea = new Rect();
        getHitRect(delegateArea);

        delegateArea.top -= EXTEND_TOUCH_LENGTH;
        delegateArea.left -= EXTEND_TOUCH_LENGTH;
        delegateArea.right += EXTEND_TOUCH_LENGTH;
        delegateArea.bottom += EXTEND_TOUCH_LENGTH;

        TouchDelegate touchDelegate = new TouchDelegate(delegateArea,
            StatefulImageView.this);

        if (View.class.isInstance(StatefulImageView.this.getParent())) {
          ((View) StatefulImageView.this.getParent()).setTouchDelegate(touchDelegate);
        }
      }
    });
  }

}

