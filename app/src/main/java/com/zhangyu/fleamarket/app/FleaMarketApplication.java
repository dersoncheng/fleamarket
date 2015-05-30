package com.zhangyu.fleamarket.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.Volley;
import com.wandoujia.base.config.GlobalConfig;
import com.wandoujia.base.storage.StorageManager;
import com.wandoujia.base.utils.SystemUtil;
import com.wandoujia.image.ImageManager;
import com.wandoujia.image.view.AsyncImageView;
import com.zhangyu.fleamarket.activity.BaseActivity;
import com.zhangyu.fleamarket.configs.Config;
import com.zhangyu.fleamarket.configs.Const;
import com.zhangyu.fleamarket.http.client.FleaMarketDataClient;
import com.zhangyu.fleamarket.utils.FileCacheStrategy;
import com.zhangyu.fleamarket.utils.FleaMarketUtils;

import java.io.File;
import java.lang.ref.WeakReference;

public class FleaMarketApplication extends Application {
  private static WeakReference<Activity> currentActivity;
  private static Context context;
  private static ImageManager imageManager;
  private static FleaMarketDataClient dataClient;
  private static ByteArrayPool byteArrayPool;
  private RequestQueue volleyRequestQueue;
  private static final String DATA_CACHE_FOLDER = "DataCache";
  private static final String IMAGE_CACHE_FOLDER = "ImageCache";
  private static Handler handler = new Handler(Looper.getMainLooper());

  private static final int BITMAP_MAX_FILE_CACHE_SIZE = 64 * 1024 * 1024; // 64M
  private static final float BITMAP_MEMORY_CACHE_SIZE_SCALE_BELOW_64 = 0.05f;
  private static final float BITMAP_MEMORY_CACHE_SIZE_SCALE_ABOVE_64 = 0.1f;
  private static final int IMAGE_NETWORK_THREAD_POOL_SIZE = 3;
  private static final int IMAGE_LOCAL_THREAD_POOL_SIZE = 1;
  private static final int BYTE_ARRAY_MAX_SIZE = 128 * 1024; // 128K

  @Override
  public void onCreate() {
    super.onCreate();
    Const.FLEAMARKET_PACKAGE_NAME = getPackageName();
    doInit();
  }

  private void doInit() {
    context = this;
    String processName = FleaMarketUtils.getProcessName(context);
    if (FleaMarketUtils.isFleaMarket(context, processName)) {
      initMainProcess();
    }
  }

  private void initMainProcess() {
    // This is to avoid the AsyncTask.class being loaded at background thread and causing some
    // error.
    AsyncTask.class.hashCode();

    GlobalConfig.setAppContext(context);
    GlobalConfig.setAppRootDir(Config.getRootDirectory());
    initByteArrayPool();
    initImageView();
    new Thread(new Runnable() {
      @Override
      public void run() {
        StorageManager.getInstance();
      }
    }).start();
  }

  private static void initByteArrayPool() {
    byteArrayPool = new ByteArrayPool(BYTE_ARRAY_MAX_SIZE);
  }

  public static Context getAppContext() {
    return context;
  }

  public static FleaMarketApplication getInstance() {
    return (FleaMarketApplication) context;
  }

  public static Handler getUIThreadHandler() {
    return handler;
  }

  public static void setCurrentActivity(BaseActivity baseActivity) {
    currentActivity = new WeakReference<Activity>(baseActivity);
    //getInstance().getLogListener().logActivityStart(baseActivity);
  }

  public static synchronized FleaMarketDataClient getDataApi() {
    if (dataClient == null) {
      String cacheDir;
      if (FileCacheStrategy.getStrategy().useExternalStorage()) {
        File externalCacheDir = SystemUtil.getDeviceExternalCacheDir();
        if (externalCacheDir != null) {
          cacheDir = externalCacheDir.getAbsolutePath() + "/" + DATA_CACHE_FOLDER;
        } else {
          cacheDir = context.getCacheDir() + "/" + DATA_CACHE_FOLDER;
        }
      } else {
        cacheDir = context.getCacheDir() + "/" + DATA_CACHE_FOLDER;
      }
      dataClient = new FleaMarketDataClient(cacheDir);
    }

    return dataClient;
  }

  private void initImageView() {
    AsyncImageView.setImageManagerHolder(new AsyncImageView.ImageManagerHolder() {
      @Override
      public ImageManager getImageManager() {
        return FleaMarketApplication.getImageManager();
      }
    });
  }

  public static synchronized ImageManager getImageManager() {
    if (imageManager == null) {
      com.wandoujia.image.Config config = new com.wandoujia.image.Config() {
        @Override
        public Context getContext() {
          return context;
        }

        @Override
        public String getFileCacheDir() {
          File cacheDir = null;
          if (FileCacheStrategy.getStrategy().useExternalStorage()) {
            cacheDir = SystemUtil.getDeviceExternalCacheDir();
          }
          if (cacheDir == null) {
            cacheDir = context.getCacheDir();
          }
          return cacheDir.getPath() + "/" + IMAGE_CACHE_FOLDER;
        }

        @Override
        public Resources getResources() {
          return null;
        }

        @Override
        public int getFileCacheSize() {
          return BITMAP_MAX_FILE_CACHE_SIZE;
        }

        @Override
        public int getMemoryCacheSize() {
          int memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
            .getMemoryClass();
          if (memoryClass <= 64) {
            return Math.round(memoryClass * Const.MB * BITMAP_MEMORY_CACHE_SIZE_SCALE_BELOW_64);
          } else {
            return Math.round(memoryClass * Const.MB * BITMAP_MEMORY_CACHE_SIZE_SCALE_ABOVE_64);
          }
        }

        @Override
        public int getNetworkThreadPoolSize() {
          return IMAGE_NETWORK_THREAD_POOL_SIZE;
        }

        @Override
        public int getLocalThreadPoolSize() {
          return IMAGE_LOCAL_THREAD_POOL_SIZE;
        }
      };
      imageManager = new ImageManager(context, config, byteArrayPool);
    }
    return imageManager;
  }

  public RequestQueue getVolleyQueue() {
    if (volleyRequestQueue == null) {
      synchronized (this) {
        if (volleyRequestQueue == null) {
          volleyRequestQueue = Volley.newRequestQueue(this);
        }
      }
    }
    return volleyRequestQueue;
  }
}
