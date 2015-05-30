package com.zhangyu.fleamarket.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import com.actionbarsherlock.internal.widget.IcsListPopupWindow;
import com.zhangyu.fleamarket.BuildConfig;
import com.zhangyu.fleamarket.R;

/**
 * The default popup window for nirvana, with some attributes set.
 * 
 * @author xubin@wandoujia.com
 */
public class NirvanaListPopupWindow extends IcsListPopupWindow {
  // Only measure this many items to get a decent max width.
  private static final int TAG_KEY_POPUP_WINDOW = R.id.key_popup_window;
  private static final int MAX_ITEMS_MEASURED = 15;
  private final Context context;
  private int dropDownWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
  private ListAdapter adapter;
  private View anchorView;

  public NirvanaListPopupWindow(Context context) {
    this(context, null, R.attr.listPopupWindowStyle);
  }

  public NirvanaListPopupWindow(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.listPopupWindowStyle);
  }

  public NirvanaListPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.context = context;
    TypedArray a = context.obtainStyledAttributes(attrs,
        R.styleable.SherlockSpinner, defStyleAttr, 0);

    dropDownWidth = a.getLayoutDimension(R.styleable.SherlockSpinner_android_dropDownWidth,
        dropDownWidth);
    int verticalOffset =
        a.getDimensionPixelOffset(R.styleable.SherlockSpinner_android_dropDownVerticalOffset, 0);
    int horizontalOffset =
        a.getDimensionPixelOffset(R.styleable.SherlockSpinner_android_dropDownHorizontalOffset, 0);
    Drawable bgDrawable = a.getDrawable(R.styleable.SherlockSpinner_android_popupBackground);

    setContentWidth(dropDownWidth);
    setVerticalOffset(verticalOffset);
    setHorizontalOffset(horizontalOffset);
    setBackgroundDrawable(bgDrawable);

    a.recycle();
    setModal(true);
  }

  @Override
  public void setAdapter(ListAdapter adapter) {
    this.adapter = adapter;
    super.setAdapter(adapter);
  }

  @Override
  public void show() {
    try {
      int width = dropDownWidth;
      if (width == ViewGroup.LayoutParams.WRAP_CONTENT && adapter != null) {
        width = measureContentWidth(adapter);
      }
      super.setContentWidth(width);
      super.show();

      setTagToDropDownView((DropDownListView) getListView());
    } catch (RuntimeException e) {
      // Sometimes super.show() may cause this exception:
      // "android.view.WindowManager$BadTokenException: Unable to add window -- token null is not
      // valid; is your activity running"
      // so catch it here
      if (BuildConfig.DEBUG) {
        throw e;
      }
    }
  }

  @Override
  public void setContentWidth(int width) {
    this.dropDownWidth = width;
    super.setContentWidth(width);
  }

  private int measureContentWidth(ListAdapter adapter) {
    if (adapter == null) {
      return 0;
    }

    int width = 0;
    View itemView = null;
    final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
        View.MeasureSpec.UNSPECIFIED);
    final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
        View.MeasureSpec.UNSPECIFIED);

    // Make sure the number of items we'll measure is capped. If it's a
    // huge data set
    // with wildly varying sizes, oh well.
    int start = 0;
    final int end = Math.min(adapter.getCount(), start + MAX_ITEMS_MEASURED);
    final int count = end - start;
    start = Math.max(0, start - (MAX_ITEMS_MEASURED - count));
    FrameLayout measureParent = new FrameLayout(context);
    for (int i = start; i < end; i++) {
      itemView = adapter.getView(i, itemView, measureParent);
      if (itemView.getLayoutParams() == null) {
        itemView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
      }
      itemView.measure(widthMeasureSpec, heightMeasureSpec);
      width = Math.max(width, itemView.getMeasuredWidth());
    }

    return width;
  }

  @Override
  public void setAnchorView(View anchor) {
    super.setAnchorView(anchor);
    this.anchorView = anchor;
  }

  public View getAnchorView() {
    return anchorView;
  }

  private void setTagToDropDownView(DropDownListView dropDownListView) {
    dropDownListView.setTag(TAG_KEY_POPUP_WINDOW, this);
  }

  public static NirvanaListPopupWindow getPopupWindow(DropDownListView dropDownListView) {
    return (NirvanaListPopupWindow) dropDownListView.getTag(TAG_KEY_POPUP_WINDOW);
  }

}
