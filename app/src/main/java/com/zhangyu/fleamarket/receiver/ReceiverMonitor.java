package com.zhangyu.fleamarket.receiver;

import android.content.Intent;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class ReceiverMonitor {
  private static ReceiverMonitor monitor;

  static {
    monitor = new ReceiverMonitor();
  }

  private final List<WeakReference<AppChangeListener>> appListeners;

  private final List<WeakReference<NetworkChangeListener>> networkListeners;

  private final List<WeakReference<MediaMountListener>> mediaListeners;

  private final List<WeakReference<ReverseProxyStateListener>> proxyListeners;

  public static ReceiverMonitor getInstance() {
    return monitor;
  }

  private ReceiverMonitor() {
    appListeners = new ArrayList<WeakReference<AppChangeListener>>();
    networkListeners = new ArrayList<WeakReference<NetworkChangeListener>>();
    mediaListeners = new ArrayList<WeakReference<MediaMountListener>>();
    proxyListeners = new ArrayList<WeakReference<ReverseProxyStateListener>>();
  }

  /**
   * The listener is used to observe application change.
   */
  public interface AppChangeListener {
    /**
     * If an application is changed, it can deal with this callback.
     *
     * @param type {@link AppActionType}
     * @param packageNames package names
     */
    void onPackageChanged(AppActionType type, List<String> packageNames);
  }

  /**
   * The Listener is used to observe media mounted.
   */
  public interface MediaMountListener {
    void onMediaStateChange(MediaState state);
  }

  public interface NetworkChangeListener {
    void onNetworkChange(NetworkInfo info);
  }

  public interface ReverseProxyStateListener {
    void onStateChange(ProxyState state);
  }

  public enum ProxyState {
    ON, OFF
  }

  public enum MediaState {

    /* media Mounted */
    MOUNTED(Intent.ACTION_MEDIA_MOUNTED),
    /* media unMounted */
    UNMOUNTED(Intent.ACTION_MEDIA_UNMOUNTED);
    private final String state;

    private MediaState(String state) {
      this.state = state;
    }

    public String getMediaState() {
      return this.state;
    }
  }

  public enum AppActionType {

    /* package removed */
    REMOVED(Intent.ACTION_PACKAGE_REMOVED),
    /* package replaced */
    REPLACED(Intent.ACTION_PACKAGE_REPLACED),
    /* package added */
    ADDED(Intent.ACTION_PACKAGE_ADDED),
    /* package changed */
    CHANGED(Intent.ACTION_PACKAGE_CHANGED),
    /* package available */
    READY(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);

    private final String action;

    private AppActionType(String action) {
      this.action = action;
    }

    public String getActionType() {
      return this.action;
    }
  }

  /**
   * add appchangelistener to observer list to catch app change events.
   *
   * @param listener
   */
  public void addAppChangeListener(AppChangeListener listener) {
    if (listener == null) {
      return;
    }
    synchronized (appListeners) {
      for (WeakReference<AppChangeListener> ref : appListeners) {
        AppChangeListener appPackageListener = ref.get();
        if (listener.equals(appPackageListener)) {
          return;
        }
      }
      appListeners.add(new WeakReference<AppChangeListener>(listener));
    }
  }

  void notifyAppChange(final AppActionType type, final List<String> packages) {
    List<AppChangeListener> listeners = new ArrayList<AppChangeListener>();
    synchronized (appListeners) {
      Iterator<WeakReference<AppChangeListener>> iterator = appListeners.iterator();
      while (iterator.hasNext()) {
        final AppChangeListener listener = iterator.next().get();
        if (listener != null) {
          listeners.add(listener);
        } else {
          iterator.remove();
        }
      }
    }
    for (AppChangeListener listener : listeners) {
      listener.onPackageChanged(type, packages);
    }
  }

  public void addMediaStateListener(MediaMountListener listener) {
    if (listener == null) {
      return;
    }
    synchronized (mediaListeners) {
      for (WeakReference<MediaMountListener> ref : mediaListeners) {
        MediaMountListener mediaListener = ref.get();
        if (listener.equals(mediaListener)) {
          return;
        }
      }
      mediaListeners.add(new WeakReference<MediaMountListener>(listener));
    }
  }

  public void removeMediaStateListener(MediaMountListener listener) {
    if (listener == null) {
      return;
    }
    synchronized (mediaListeners) {
      for (int i = 0; i < mediaListeners.size(); i++) {
        MediaMountListener next = mediaListeners.get(i).get();
        if (listener.equals(next)) {
          mediaListeners.remove(i);
          return;
        }
      }
    }
  }

  void notifyMediaStateChange(MediaState state) {
    List<MediaMountListener> listeners = new ArrayList<MediaMountListener>();
    synchronized (mediaListeners) {
      Iterator<WeakReference<MediaMountListener>> iterator = mediaListeners.iterator();
      while (iterator.hasNext()) {
        final MediaMountListener listener = iterator.next().get();
        if (listener != null) {
          listeners.add(listener);
        } else {
          iterator.remove();
        }
      }
    }
    for (MediaMountListener listener : listeners) {
      listener.onMediaStateChange(state);
    }
  }

  public void addProxyStateListener(ReverseProxyStateListener listener) {
    if (listener == null) {
      return;
    }
    synchronized (proxyListeners) {
      for (WeakReference<ReverseProxyStateListener> ref : proxyListeners) {
        ReverseProxyStateListener netListener = ref.get();
        if (listener.equals(netListener)) {
          return;
        }
      }
      proxyListeners.add(new WeakReference<ReverseProxyStateListener>(listener));
    }
  }

  public void removeProxyStateListener(ReverseProxyStateListener listener) {
    if (listener == null) {
      return;
    }
    synchronized (proxyListeners) {
      for (int i = 0; i < proxyListeners.size(); i++) {
        ReverseProxyStateListener next = proxyListeners.get(i).get();
        if (listener.equals(next)) {
          proxyListeners.remove(i);
          return;
        }
      }
    }
  }

  public void addNetworkChangeListener(NetworkChangeListener listener) {
    if (listener == null) {
      return;
    }
    synchronized (networkListeners) {
      for (WeakReference<NetworkChangeListener> ref : networkListeners) {
        NetworkChangeListener netListener = ref.get();
        if (listener.equals(netListener)) {
          return;
        }
      }
      networkListeners.add(new WeakReference<NetworkChangeListener>(listener));
    }
  }

  public void removeNetworkChangeListener(NetworkChangeListener listener) {
    if (listener == null) {
      return;
    }
    synchronized (networkListeners) {
      for (int i = 0; i < networkListeners.size(); i++) {
        NetworkChangeListener netListener = networkListeners.get(i).get();
        if (listener.equals(netListener)) {
          networkListeners.remove(i);
          return;
        }
      }
    }
  }

  public void notifyNetworkChange(NetworkInfo info) {
    List<NetworkChangeListener> listeners = new ArrayList<NetworkChangeListener>();
    synchronized (networkListeners) {
      Iterator<WeakReference<NetworkChangeListener>> iterator = networkListeners.iterator();
      while (iterator.hasNext()) {
        final NetworkChangeListener listener = iterator.next().get();
        if (listener != null) {
          listeners.add(listener);
        } else {
          iterator.remove();
        }
      }
    }
    for (NetworkChangeListener listener : listeners) {
      listener.onNetworkChange(info);
    }
  }
}
