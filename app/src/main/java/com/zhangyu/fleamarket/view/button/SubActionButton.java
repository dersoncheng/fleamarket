package com.zhangyu.fleamarket.view.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.action.Action;
import com.zhangyu.fleamarket.view.NirvanaListPopupWindow;

import java.util.List;

/**
 * This custom view is used in card.
 * <p>
 * The view is similar with the menu in ActionBar. It holds a set of buttons. If there is only one
 * button in the set, it shown as the button. If there is more than one button is the set, it shown
 * like the menu in ActionBar which looks as "more" on default and could be expanded a to menu list.
 * </p>
 * 
 * @author match@wandoujia.com (Diao Liu)
 */
public class SubActionButton extends ImageButton {
  private List<SubActionItem> items;
  private PopupListAdapter adapter;
  private NirvanaListPopupWindow listPopupWindow;
  private boolean alwaysShowAsAction;
  private int actionIconResId;

  private final OnClickListener expandMenuListener = new OnClickListener() {

    @Override
    public void onClick(View v) {
      listPopupWindow.show();
    }
  };
  private final AdapterView.OnItemClickListener itemClickListener =
      new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          SubActionItem item = items.get(position);
          if (item.getAction() != null) {
            item.getAction().execute();
          }
          listPopupWindow.dismiss();
        }
      };

  public SubActionButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  public SubActionButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public SubActionButton(Context context) {
    super(context);
    init(context, null);
  }

  @SuppressWarnings("deprecation")
  private void init(Context context, AttributeSet attrs) {
    setBackgroundDrawable(null);
    listPopupWindow = new NirvanaListPopupWindow(getContext());
    listPopupWindow.setAnchorView(this);
    adapter = new PopupListAdapter();
    listPopupWindow.setAdapter(adapter);
    listPopupWindow.setOnItemClickListener(itemClickListener);
    if (attrs != null) {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SubActionButton, 0, 0);
      this.alwaysShowAsAction =
          a.getBoolean(R.styleable.SubActionButton_always_show_as_action, false);
      this.actionIconResId =
          a.getResourceId(R.styleable.SubActionButton_action_icon,
              R.drawable.ic_mything_detail_overflow);
      a.recycle();
    }
  }

  /**
   * Sets the action set.
   * 
   * @param items action set.
   */
  public void setData(final List<SubActionItem> items) {
    this.items = items;
    adapter.notifyDataSetChanged();
    if (items != null && !items.isEmpty()) {
      if (alwaysShowAsAction || items.size() > 1) {
        this.setImageResource(actionIconResId);
        this.setOnClickListener(expandMenuListener);
      } else {
        this.setImageResource(items.get(0).drawableId);
        this.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            if (!items.isEmpty()) {
              SubActionItem item = items.get(0);
              if (item.action != null) {
                item.action.execute();
              }
            }
          }
        });
      }
    }
  }

  /**
   * Set whether the action is always in the dropdown menu.
   * 
   * @param isAlwaysShowAsAction true if the sub action is always in the dropdown menu,
   *          false otherwise
   */
  public void setAlwaysShowAsAction(boolean isAlwaysShowAsAction) {
    this.alwaysShowAsAction = isAlwaysShowAsAction;
  }

  public static class SubActionItem {
    String text;
    int drawableId;
    Action action;

    public SubActionItem(String text, int drawableId, Action action) {
      super();
      this.text = text;
      this.drawableId = drawableId;
      this.action = action;
    }

    public String getText() {
      return text;
    }

    public int getDrawable() {
      return drawableId;
    }

    public Action getAction() {
      return action;
    }

  }

  public class PopupListAdapter extends BaseAdapter {

    @Override
    public int getCount() {
      return items != null ? items.size() : 0;
    }

    @Override
    public SubActionItem getItem(int position) {
      return items.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      if (convertView == null) {
        convertView = inflater.inflate(R.layout.flea_dropdown_item_view,
            parent, false);
      }
      final SubActionItem item = getItem(position);
      TextView title = (TextView) convertView;
      title.setText(item.text);
      return convertView;
    }

  }

}
