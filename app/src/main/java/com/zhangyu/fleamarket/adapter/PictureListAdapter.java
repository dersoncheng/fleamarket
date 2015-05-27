package com.zhangyu.fleamarket.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
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
//  private ImageDownloader imageDownloader;
  private ImageLoader imageLoader;

  public PictureListAdapter(Context context, List<HashMap<String, Object>> dataList) {
    this.mContext = context;
    this.dataList = dataList;
    RequestQueue mQueue = Volley.newRequestQueue(context);
    imageLoader = new ImageLoader(mQueue, new BitmapCache());
//    imageDownloader = new ImageDownloader();
//    imageDownloader.setMode(ImageDownloader.Mode.CORRECT);
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
    ViewHolder holder = null;
    final String imageUrl = (String) getItem(i).get("ItemImageUrl");
    if (view == null) {
      holder = new ViewHolder();
      view = LayoutInflater.from(mContext).inflate(R.layout.picture_list_item, null);
      holder.image = (ImageView) view.findViewById(R.id.ItemImage);
      holder.title = (TextView) view.findViewById(R.id.ItemTitle);
      holder.text = (TextView) view.findViewById(R.id.ItemText);
      //imageDownloader.download(imageUrl, holder.image);

      view.setTag(holder);
    } else {
      holder = (ViewHolder) view.getTag();
    }

    holder.title.setText((String) getItem(i).get("ItemTitle"));
    holder.text.setText((String) getItem(i).get("ItemText"));

    ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(holder.image,
      android.R.drawable.ic_menu_rotate, android.R.drawable.ic_delete);
    imageLoader.get(imageUrl, imageListener);

    return view;
  }

  private class BitmapCache implements ImageLoader.ImageCache {
    private LruCache<String, Bitmap> bitmapLruCache;

    public BitmapCache() {
      int maxSize = 10 * 1024 * 1024;
      bitmapLruCache = new LruCache<String, Bitmap>(maxSize) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
          return value.getRowBytes() * value.getHeight();
        }
      };
    }

    @Override
    public Bitmap getBitmap(String url) {
      return bitmapLruCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
      bitmapLruCache.put(url, bitmap);
    }
  }
}
