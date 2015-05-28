package com.wandoujia.shared_storage;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by liuyaxin on 13-8-19.
 */
public abstract class ListSharedStorage<T extends StorageLine> extends BaseSharedStorage {

  @Override
  protected void onFileChanged() {
    synchronized (this) {
      cacheMap.clear();
      try {
        Gson gson = new Gson();
        Type collectionType = getTypeToken(); // new TypeToken<Collection<T>>(){}.getType();
        Collection<T> inArray = gson.fromJson(loadContent(), collectionType);
        if (inArray != null) {
          Iterator<T> iter = inArray.iterator();
          while (iter.hasNext()) {
            T element = iter.next();
            cacheMap.put(element.getKey(), element);
          }
        }
      } catch (JsonSyntaxException e) {
        onFileSyntaxError();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {}
    }
  }

  private final HashMap<String, T> cacheMap = new HashMap<String, T>();

  protected ListSharedStorage() {
    new Thread() {
      @Override
      public void run() {
        onFileChanged();
      }
    }.start();
  }

  protected void addOrUpdateItems(final Collection<T> items) {
    synchronized (this) {
      for (T item : items) {
        cacheMap.put(item.getKey(), item);
      }
    }
    new Thread() {
      @Override
      public void run() {
        doAddOrUpdateItems(items);
      }
    }.start();
  }

  protected abstract Type getTypeToken();

  private synchronized void doAddOrUpdateItems(Collection<T> items) {
    try {
      Gson gson = new Gson();
      Type collectionType = getTypeToken(); // new TypeToken<Collection<T>>(){}.getType();
      Collection<T> inArray = gson.fromJson(loadContent(), collectionType);
      Collection<T> outArray = new ArrayList<T>();
      HashSet<String> insertedKey = new HashSet<String>();
      for (T item : items) {
        insertedKey.add(item.getKey());
      }
      outArray.addAll(items);
      if (inArray != null) {
        Iterator<T> iter = inArray.iterator();
        while (iter.hasNext()) {
          T element = iter.next();
          if (!insertedKey.contains(element.getKey())) {
            outArray.add(element);
          }
        }
      }
      saveContent(gson.toJson(outArray, collectionType));
    } catch (JsonSyntaxException e) {
      onFileSyntaxError();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {}
  }

  protected void addOrUpdateItem(final T item) {
    synchronized (this) {
      cacheMap.put(item.getKey(), item);
    }
    new Thread() {
      @Override
      public void run() {
        doAddOrUpdateItem(item);
      }
    }.start();;
  }

  private synchronized void doAddOrUpdateItem(T item) {
    try {
      Gson gson = new Gson();
      Type collectionType = getTypeToken(); // new TypeToken<Collection<T>>(){}.getType();
      Collection<T> inArray = gson.fromJson(loadContent(), collectionType);
      Collection<T> outArray = new ArrayList<T>();
      outArray.add(item);
      if (inArray != null) {
        Iterator<T> iter = inArray.iterator();
        while (iter.hasNext()) {
          T element = iter.next();
          if (!element.getKey().equals(item.getKey())) {
            outArray.add(element);
          }
        }
      }
      saveContent(gson.toJson(outArray, collectionType));
    } catch (JsonSyntaxException e) {
      onFileSyntaxError();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {}
  }

  protected void removeItem(final String key) {
    if (!TextUtils.isEmpty(key)) {
      synchronized (this) {
        if (!cacheMap.containsKey(key)) {
          return;
        }
        cacheMap.remove(key);
      }
      new Thread() {
        @Override
        public void run() {
          doRemoveItem(key);
        }
      }.start();;
    }
  }

  protected synchronized void doRemoveItem(String key) {
    try {
      Gson gson = new Gson();
      Type collectionType = getTypeToken(); // new TypeToken<Collection<T>>(){}.getType();
      Collection<T> inArray = gson.fromJson(loadContent(), collectionType);
      Collection<T> outArray = new ArrayList<T>();
      if (inArray != null) {
        Iterator<T> iter = inArray.iterator();
        while (iter.hasNext()) {
          T element = iter.next();
          if (!element.getKey().equals(key)) {
            outArray.add(element);
          }
        }
      }
      saveContent(gson.toJson(outArray, collectionType));
    } catch (JsonSyntaxException e) {
      onFileSyntaxError();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {}
  }

  protected Set<String> getItemKeys() {
    synchronized (this) {
      return cacheMap.keySet();
    }
  }

  protected boolean containsItem(String key) {
    synchronized (this) {
      return cacheMap.containsKey(key);
    }
  }

  protected T getItem(String key) {
    synchronized (this) {
      return cacheMap.get(key);
    }
  }

}
