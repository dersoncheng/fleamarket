package com.wandoujia.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.IMediaScannerListener;
import android.media.IMediaScannerService;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

public class MediaScannerConnectionEx implements ServiceConnection {
  private Context context;
  private MediaScannerConnectionClient client;
  private IMediaScannerService service;
  private boolean connected;

  private IMediaScannerListener.Stub listener = new IMediaScannerListener.Stub() {

    public void scanCompleted(String path, Uri uri) throws RemoteException {
      MediaScannerConnectionClient c = client;
      if (c != null) {
        c.onScanCompleted(path, uri);
      }
    }
  };

  public interface OnScanCompletedListener {
    public void onScanCompleted(String path, Uri uri);
  }

  public interface MediaScannerConnectionClient extends OnScanCompletedListener {
    public void onMediaScannerConnected();

    public void onScanCompleted(String path, Uri uri);
  }

  public MediaScannerConnectionEx(Context context, MediaScannerConnectionClient client) {
    this.context = context;
    this.client = client;
  }

  public void connect() {
    synchronized (this) {
      if (!connected) {
        Intent intent = new Intent(IMediaScannerService.class.getName());
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
        connected = true;
      }
    }
  }

  public void disconnect() {
    synchronized (this) {
      if (connected) {
        try {
          context.unbindService(this);
        } catch (IllegalArgumentException ex) {
          ex.printStackTrace();
        }
        connected = false;
      }
    }
  }

  public synchronized boolean isConnected() {
    return (service != null && connected);
  }

  public void scanFile(String path, String mimeType) {
    synchronized (this) {
      if (service == null || !connected) {
        throw new IllegalStateException("not connected to MediaScannerService");
      }
      try {
        service.requestScanFile(path, mimeType, listener);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }

  static class ClientProxy implements MediaScannerConnectionClient {
    final String[] paths;
    final String[] mimeTypes;
    final OnScanCompletedListener client;
    MediaScannerConnectionEx connection;
    int nextPath;

    ClientProxy(String[] paths, String[] mimeTypes, OnScanCompletedListener client) {
      this.paths = paths;
      this.mimeTypes = mimeTypes;
      this.client = client;
    }

    public void onMediaScannerConnected() {
      scanNextPath();
    }

    public void onScanCompleted(String path, Uri uri) {
      if (client != null) {
        client.onScanCompleted(path, uri);
      }
      scanNextPath();
    }

    void scanNextPath() {
      if (nextPath >= paths.length) {
        connection.disconnect();
        return;
      }
      String mimeType = mimeTypes != null ? mimeTypes[nextPath] : null;
      connection.scanFile(paths[nextPath], mimeType);
      nextPath++;
    }
  }

  public static void scanFile(Context context, String[] paths, String[] mimeTypes,
      OnScanCompletedListener callback) {
    ClientProxy client = new ClientProxy(paths, mimeTypes, callback);
    MediaScannerConnectionEx connection = new MediaScannerConnectionEx(context, client);
    client.connection = connection;
    connection.connect();
  }

  public void onServiceConnected(ComponentName name, IBinder service) {
    synchronized (this) {
      this.service = IMediaScannerService.Stub.asInterface(service);
      if (service != null && client != null) {
        client.onMediaScannerConnected();
      }
    }
  }

  public void onServiceDisconnected(ComponentName name) {
    synchronized (this) {
      this.service = null;
    }
  }
}
