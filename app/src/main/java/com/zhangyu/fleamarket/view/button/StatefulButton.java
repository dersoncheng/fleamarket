package com.zhangyu.fleamarket.view.button;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.TextView;

/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class StatefulButton extends TextView {

  private static final String TAG = "StatefulButton";
  private static final int EXTEND_TOUCH_LENGTH = 50;

  protected ButtonState state;

  public StatefulButton(Context context) {
    super(context);
  }

  public StatefulButton(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public StatefulButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (state != null) {
      mergeDrawableStates(drawableState, new int[] {state.getStateAttr()});
    }
    return drawableState;
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
    setText(state.getText());
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

    ((View) StatefulButton.this.getParent()).post(new Runnable() {
      @Override
      public void run() {
        Rect delegateArea = new Rect();
        getHitRect(delegateArea);

        delegateArea.top -= EXTEND_TOUCH_LENGTH;
        delegateArea.left -= EXTEND_TOUCH_LENGTH;
        delegateArea.right += EXTEND_TOUCH_LENGTH;
        delegateArea.bottom += EXTEND_TOUCH_LENGTH;

        TouchDelegate touchDelegate = new TouchDelegate(delegateArea,
            StatefulButton.this);

        if (View.class.isInstance(StatefulButton.this.getParent())) {
          ((View) StatefulButton.this.getParent()).setTouchDelegate(touchDelegate);
        }
      }
    });
  }

}
