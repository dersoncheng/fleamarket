package com.wandoujia.image.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileLock;
import java.util.HashSet;
import java.util.Set;

import android.os.FileObserver;
import android.util.Log;

import com.android.volley.VolleyLog;
import com.android.volley.toolbox.DiskBasedCache;
import com.wandoujia.image.BuildConfig;

/**
 * This class is adapted from {@link com.android.volley.toolbox.DiskBasedCache}, and equipped with
 * synchronization logic between multi-processes.
 * 
 * @author yingyixu@wandoujia.com (Yingyi Xu)
 */
public class ConcurrentDiskBasedCache extends DiskBasedCache {

  private static final String TAG = ConcurrentDiskBasedCache.class.getSimpleName();
  private Set<String> selfAddedFileSet = new HashSet<String>();
  private FileObserver fileObserver;

  /**
   * Constructs an instance of the DiskBasedCache at the specified directory.
   * 
   * @param rootDirectory The root directory of the cache.
   * @param maxCacheSizeInBytes The maximum size of the cache in bytes.
   */
  public ConcurrentDiskBasedCache(File rootDirectory, int maxCacheSizeInBytes) {
    super(rootDirectory, maxCacheSizeInBytes);
    initFileObserver();
  }

  /**
   * Constructs an instance of the DiskBasedCache at the specified directory using
   * the default maximum cache size of 5MB.
   * 
   * @param rootDirectory The root directory of the cache.
   */
  public ConcurrentDiskBasedCache(File rootDirectory) {
    super(rootDirectory);
    initFileObserver();
  }

  private void initFileObserver() {
    fileObserver = new FileObserver(mRootDirectory.getAbsolutePath()) {
      @Override
      public void onEvent(int event, String path) {
        if ((event & FileObserver.CLOSE_WRITE) != 0) {
          synchronized (ConcurrentDiskBasedCache.this) {
            if (!selfAddedFileSet.contains(path)) {
              if (BuildConfig.DEBUG) {
                Log.e(TAG, "cache added from other process. [filename: " + path + "]");
              }
              final File file = new File(mRootDirectory, path);
              addEntryFromFile(file);
            }
          }
        }
      }
    };
    fileObserver.startWatching();
  }

  /**
   * Clears the cache. Deletes all cached files from disk.
   */
  @Override
  public synchronized void clear() {
    super.clear();
    selfAddedFileSet.clear();
  }

  /**
   * Returns the cache entry with the specified key if it exists, null otherwise.
   */
  @Override
  public synchronized Entry get(String key) {
    CacheHeader entry = mEntries.get(key);
    // if the entry does not exist, return.
    if (entry == null) {
      return null;
    }

    File file = getFileForKey(key);
    CountingInputStream cis = null;
    FileLock sharedFileLock = null;
    try {
      final FileInputStream fio = new FileInputStream(file);
      sharedFileLock = fio.getChannel().lock(0L, Long.MAX_VALUE, true);
      cis = new CountingInputStream(fio);
      CacheHeader.readHeader(cis); // eat header
      byte[] data = streamToBytes(cis, (int) (file.length() - cis.bytesRead));
      return entry.toCacheEntry(data);
    } catch (IOException e) {
      VolleyLog.d("%s: %s", file.getAbsolutePath(), e.toString());
      remove(key);
      return null;
    } catch (OutOfMemoryError e) {
      VolleyLog.d("%s: %s", file.getAbsolutePath(), e.toString());
      remove(key);
      return null;
    } finally {
      if (sharedFileLock != null) {
        try {
          sharedFileLock.release();
        } catch (ClosedChannelException ignored) {
          // do nothing
        } catch (IOException ignored) {} catch (IllegalMonitorStateException ignored) {}
      }
      if (cis != null) {
        try {
          cis.close();
        } catch (IOException ignored) {}
      }
    }
  }

  /**
   * Initializes the DiskBasedCache by scanning for all files currently in the
   * specified root directory. Creates the root directory if necessary.
   */
  @Override
  public synchronized void initialize() {
    if (!mRootDirectory.exists()) {
      if (!mRootDirectory.mkdirs()) {
        VolleyLog.e("Unable to create cache dir %s", mRootDirectory.getAbsolutePath());
      }
      return;
    }

    File[] files = mRootDirectory.listFiles();
    if (files == null) {
      return;
    }
    for (File file : files) {
      addEntryFromFile(file);
    }
  }

  private void addEntryFromFile(File file) {
    FileInputStream fis = null;
    FileLock sharedFileLock = null;
    try {
      fis = new FileInputStream(file);
      sharedFileLock = fis.getChannel().lock(0L, Long.MAX_VALUE, true);
      CacheHeader entry = CacheHeader.readHeader(fis);
      entry.size = file.length();
      putEntry(entry.key, entry);
    } catch (IOException e) {
      if (file != null) {
        file.delete();
      }
    } finally {
      if (sharedFileLock != null) {
        try {
          sharedFileLock.release();
        } catch (ClosedChannelException ignored) {
          // do nothing
        } catch (IOException ignored) {} catch (IllegalMonitorStateException ignored) {}
      }
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException ignored) {}
      }
    }
  }

  /**
   * Puts the entry with the specified key into the cache.
   */
  @Override
  public synchronized void put(String key, Entry entry) {
    pruneIfNeeded(entry.data.length);
    File file = getFileForKey(key);
    FileLock exclusiveFileLock = null;
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file);
      exclusiveFileLock = fos.getChannel().lock();
      CacheHeader e = new CacheHeader(key, entry);
      e.writeHeader(fos);
      fos.write(entry.data);
      selfAddedFileSet.add(file.getName());
      putEntry(key, e);
      return;
    } catch (IOException e) {} finally {
      if (exclusiveFileLock != null) {
        try {
          exclusiveFileLock.release();
        } catch (ClosedChannelException ignored) {
          // do nothing
        } catch (IOException ignored) {} catch (IllegalMonitorStateException ignored) {}
      }
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException ignored) {}
      }
    }
    boolean deleted = file.delete();
    if (!deleted) {
      VolleyLog.d("Could not clean up file %s", file.getAbsolutePath());
    }
  }

  /**
   * Removes the specified key from the cache if it exists.
   */
  @Override
  public synchronized void remove(String key) {
    super.remove(key);
    selfAddedFileSet.remove(getFilenameForKey(key));
  }
}
