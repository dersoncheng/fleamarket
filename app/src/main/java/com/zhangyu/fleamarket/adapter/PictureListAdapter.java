package com.zhangyu.fleamarket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhangyu.fleamarket.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by gaoxiong on 2015/5/26.
 */
public class PictureListAdapter extends BaseAdapter {
  final class ViewHolder {
    ImageView image;
    TextView title;
    TextView text;
  }

  private Context mContext;
  private List<HashMap<String, Object>> dataList;

  public PictureListAdapter(Context context, List<HashMap<String, Object>> dataList) {
    this.mContext = context;
    this.dataList = dataList;
  }

  @Override
  public int getCount() {
    return dataList.size();
  }

  @Override
  public HashMap<String, Object> getItem(int i) {
    return dataList.get(i);
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    ViewHolder holder;
    if (view == null) {
      holder = new ViewHolder();
      view = LayoutInflater.from(mContext).inflate(R.layout.picture_list_item, null);
      holder.image = (ImageView) view.findViewById(R.id.ItemImage);
      holder.title = (TextView) view.findViewById(R.id.ItemTitle);
      holder.text = (TextView) view.findViewById(R.id.ItemText);
      view.setTag(holder);
    } else {
      holder = (ViewHolder) view.getTag();
    }

    holder.image.setImageResource((Integer) getItem(i).get("ItemImage"));
    holder.title.setText((String) getItem(i).get("ItemTitle"));
    holder.text.setText((String) getItem(i).get("ItemText"));

//    int ColorPos = i % colors.length;
//    view.setBackgroundColor(colors[ColorPos]);

    return view;
  }
}
