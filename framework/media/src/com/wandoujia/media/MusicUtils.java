/*
 * Copyright (C) 2008 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wandoujia.media;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class MusicUtils {

  public interface Defs {
    public final static int OPEN_URL = 0;
    public final static int ADD_TO_PLAYLIST = 1;
    public final static int USE_AS_RINGTONE = 2;
    public final static int PLAYLIST_SELECTED = 3;
    public final static int NEW_PLAYLIST = 4;
    public final static int PLAY_SELECTION = 5;
    public final static int GOTO_START = 6;
    public final static int GOTO_PLAYBACK = 7;
    public final static int PARTY_SHUFFLE = 8;
    public final static int SHUFFLE_ALL = 9;
    public final static int DELETE_ITEM = 10;
    public final static int SCAN_DONE = 11;
    public final static int QUEUE = 12;
    public final static int EFFECTS_PANEL = 13;
    public final static int CHILD_MENU_BASE = 14; // this should be the last
    // item
  }

  public static class ServiceToken {
    ContextWrapper mWrappedContext;

    ServiceToken(ContextWrapper context) {
      mWrappedContext = context;
    }
  }

  private final static long[] sEmptyList = new long[0];

  public static long[] getSongListForCursor(Cursor cursor) {
    if (cursor == null) {
      return sEmptyList;
    }
    int len = cursor.getCount();
    long[] list = new long[len];
    cursor.moveToFirst();
    int colidx = -1;
    try {
      colidx = cursor
          .getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
    } catch (IllegalArgumentException ex) {
      colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
    }
    for (int i = 0; i < len; i++) {
      list[i] = cursor.getLong(colidx);
      cursor.moveToNext();
    }
    return list;
  }

  // A really simple BitmapDrawable-like class, that doesn't do
  // scaling, dithering or filtering.
  private static class FastBitmapDrawable extends Drawable {
    private final Bitmap mBitmap;

    public FastBitmapDrawable(Bitmap b) {
      mBitmap = b;
    }

    @Override
    public void draw(Canvas canvas) {
      canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    public int getOpacity() {
      return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter cf) {}
  }

  private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
  private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
  private static final Uri sArtworkUri = Uri
      .parse("content://media/external/audio/albumart");
  private static final HashMap<Long, Drawable> sArtCache = new HashMap<Long, Drawable>();

  static {
    // for the cache,
    // 565 is faster to decode and display
    // and we don't want to dither here because the image will be scaled
    // down later
    sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
    sBitmapOptionsCache.inDither = false;

    sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
    sBitmapOptions.inDither = false;
  }

  public static void clearAlbumArtCache() {
    synchronized (sArtCache) {
      sArtCache.clear();
    }
  }

  public static Drawable getCachedArtwork(Context context, long artIndex,
      BitmapDrawable defaultArtwork) {
    Drawable d = null;
    synchronized (sArtCache) {
      d = sArtCache.get(artIndex);
    }
    if (d == null) {
      d = defaultArtwork;
      final Bitmap icon = defaultArtwork.getBitmap();
      int w = icon.getWidth();
      int h = icon.getHeight();
      Bitmap b = MusicUtils.getArtworkQuick(context, artIndex, w, h);
      if (b != null) {
        d = new FastBitmapDrawable(b);
        synchronized (sArtCache) {
          // the cache may have changed since we checked
          Drawable value = sArtCache.get(artIndex);
          if (value == null) {
            sArtCache.put(artIndex, d);
          } else {
            d = value;
          }
        }
      }
    }
    return d;
  }

  // Get album art for specified album. This method will not try to
  // fall back to getting artwork directly from the file, nor will
  // it attempt to repair the database.
  public static Bitmap getArtworkQuick(Context context, long album_id,
      int w, int h) {
    // NOTE: There is in fact a 1 pixel border on the right side in the
    // ImageView
    // used to display this drawable. Take it into account now, so we don't
    // have to
    // scale later.
    w -= 1;
    ContentResolver res = context.getContentResolver();
    Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
    if (uri != null) {
      ParcelFileDescriptor fd = null;
      try {
        fd = res.openFileDescriptor(uri, "r");
        int sampleSize = 1;

        // Compute the closest power-of-two scale factor
        // and pass that to sBitmapOptionsCache.inSampleSize, which will
        // result in faster decoding and better quality
        sBitmapOptionsCache.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),
            null, sBitmapOptionsCache);
        int nextWidth = sBitmapOptionsCache.outWidth >> 1;
        int nextHeight = sBitmapOptionsCache.outHeight >> 1;
        while (nextWidth > w && nextHeight > h) {
          sampleSize <<= 1;
          nextWidth >>= 1;
          nextHeight >>= 1;
        }

        sBitmapOptionsCache.inSampleSize = sampleSize;
        sBitmapOptionsCache.inJustDecodeBounds = false;
        Bitmap b = BitmapFactory.decodeFileDescriptor(
            fd.getFileDescriptor(), null, sBitmapOptionsCache);

        if (b != null) {
          // finally rescale to exactly the size we need
          if (sBitmapOptionsCache.outWidth != w
              || sBitmapOptionsCache.outHeight != h) {
            Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
            // Bitmap.createScaledBitmap() can return the same
            // bitmap
            b = tmp;
          }
        }

        return b;
      } catch (FileNotFoundException e) {} finally {
        try {
          if (fd != null) {
            fd.close();
          }
        } catch (IOException e) {}
      }
    }
    return null;
  }

  /**
   * Get album art for specified album. You should not pass in the album id
   * for the "unknown" album here (use -1 instead) This method always returns
   * the default album art icon when no album art is found.
   */
  public static Bitmap getArtwork(Context context, long song_id, long album_id) {
    return getArtwork(context, song_id, album_id, true);
  }

  /**
   * Get album art for specified album. You should not pass in the album id
   * for the "unknown" album here (use -1 instead)
   */
  public static Bitmap getArtwork(Context context, long song_id,
      long album_id, boolean allowdefault) {

    if (album_id < 0) {
      // This is something that is not in the database, so get the album
      // art directly
      // from the file.
      if (song_id >= 0) {
        Bitmap bm = getArtworkFromFile(context, song_id, -1);
        if (bm != null) {
          return bm;
        }
      }
      if (allowdefault) {
        return getDefaultArtwork(context);
      }
      return null;
    }

    ContentResolver res = context.getContentResolver();
    Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
    if (uri != null) {
      InputStream in = null;
      try {
        in = res.openInputStream(uri);
        return BitmapFactory.decodeStream(in, null, sBitmapOptions);
      } catch (FileNotFoundException ex) {
        // The album art thumbnail does not actually exist. Maybe the
        // user deleted it, or
        // maybe it never existed to begin with.
        Bitmap bm = getArtworkFromFile(context, song_id, album_id);
        if (bm != null) {
          if (bm.getConfig() == null) {
            bm = bm.copy(Bitmap.Config.RGB_565, false);
            if (bm == null && allowdefault) {
              return getDefaultArtwork(context);
            }
          }
        } else if (allowdefault) {
          bm = getDefaultArtwork(context);
        }
        return bm;
      } finally {
        try {
          if (in != null) {
            in.close();
          }
        } catch (IOException ex) {}
      }
    }

    return null;
  }

  // get album art for specified file
  private static Bitmap getArtworkFromFile(Context context, long songid,
      long albumid) {
    Bitmap bm = null;

    if (albumid < 0 && songid < 0) {
      throw new IllegalArgumentException(
          "Must specify an album or a song id");
    }

    try {
      if (albumid < 0) {
        Uri uri = Uri.parse("content://media/external/audio/media/"
            + songid + "/albumart");
        ParcelFileDescriptor pfd = context.getContentResolver()
            .openFileDescriptor(uri, "r");
        if (pfd != null) {
          FileDescriptor fd = pfd.getFileDescriptor();
          bm = BitmapFactory.decodeFileDescriptor(fd);
        }
      } else {
        Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
        ParcelFileDescriptor pfd = context.getContentResolver()
            .openFileDescriptor(uri, "r");
        if (pfd != null) {
          FileDescriptor fd = pfd.getFileDescriptor();
          bm = BitmapFactory.decodeFileDescriptor(fd);
        }
      }
    } catch (IllegalStateException ex) {} catch (FileNotFoundException ex) {}
    return bm;
  }

  public static Bitmap getDefaultArtwork(Context context) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
    return BitmapFactory.decodeStream(context.getResources()
        .openRawResource(R.drawable.aa_account_icon), null, opts);
  }
}
