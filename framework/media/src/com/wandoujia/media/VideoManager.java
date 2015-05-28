package com.wandoujia.media;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import com.squareup.wire.ByteString;
import com.wandoujia.base.config.GlobalConfig;
import com.wandoujia.base.utils.ImageUtil;
import com.wandoujia.base.utils.PathAdjustUtil;
import com.wandoujia.base.utils.ThreadUtil;
import com.wandoujia.media.pmp.models.MediaVideo;
import com.wandoujia.media.pmp.models.MediaVideos;
import com.wandoujia.media.pmp.models.Thumbnail;

public class VideoManager implements MediaConstants {

  private static final int MAX_SIDE_LEN = 240;

  private ContentResolver cr;
  private Context ctx;

  private VideoManager(Context ctx) {
    this.ctx = ctx;
    this.cr = ctx.getContentResolver();
  }

  private static VideoManager instance;

  public static VideoManager getInstance(Context context) {
    if (instance == null) {
      instance = new VideoManager(context.getApplicationContext());
    }

    return instance;
  }

  private static class VideoColumnIndex {
    public int Id = -1;
    public int Desc = -1;
    public int IsPrivate = -1;
    public int Latitude = -1;
    public int Longitude = -1;
    public int DateTaken = -1;
    public int BucketName = -1;
    public int Resolution = -1;
    public int Album = -1;
    public int Artist = -1;
    public int Duration = -1;
    public int Bookmark = -1;
    public int Path = -1;
    public int DisplayName = -1;
    public int Size = -1;
    public int MimeType = -1;
    public int DateAdded = -1;
    public int DateModified = -1;
    public int Title = -1;

    private VideoColumnIndex(Cursor cursor) {
      Id = cursor.getColumnIndex(MediaStore.Video.Media._ID);
      Desc = cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION); // TEXT
      IsPrivate = cursor
          .getColumnIndex(MediaStore.Video.Media.IS_PRIVATE); // INTEGER
      Latitude = cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE); // DOUBLE
      Longitude = cursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE); // DOUBLE
      DateTaken = cursor
          .getColumnIndex(MediaStore.Video.Media.DATE_TAKEN); // INTEGER
      BucketName = cursor
          .getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME); // TEXT
      Resolution = cursor
          .getColumnIndex(MediaStore.Video.Media.RESOLUTION);

      Album = cursor.getColumnIndex(MediaStore.Video.Media.ALBUM); // STRING
      Artist = cursor.getColumnIndex(MediaStore.Video.Media.ARTIST); // STRING
      Duration = cursor.getColumnIndex(MediaStore.Video.Media.DURATION); // INTEGER
      Bookmark = cursor.getColumnIndex(MediaStore.Video.Media.BOOKMARK);

      Path = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
      DisplayName = cursor
          .getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
      Size = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
      MimeType = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
      DateAdded = cursor
          .getColumnIndex(MediaStore.MediaColumns.DATE_ADDED);
      DateModified = cursor
          .getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
      Title = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE);
    }

    private static VideoColumnIndex instance = null;

    public static VideoColumnIndex getInstance(Cursor cursor) {
      if (instance == null) {
        instance = new VideoColumnIndex(cursor);
      }

      return instance;
    }
  }

  public int getVideoCount() {
    Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.Video.Media._ID}, null, null, null);
    int count = 0;
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    return count;
  }

  public long getAllVideoSize() throws InterruptedException {
    Cursor cursor = null;
    try {
      cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
          new String[] {MediaStore.Video.Media.SIZE}, null, null,
          null);
      if (cursor != null) {
        long sizeAll = 0;
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          sizeAll += cursor.getLong(0);
        }
        cursor.close();
        return sizeAll;
      }
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }
    return 0;
  }

  public MediaVideo getVideoById(long id) {
    Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Video.Media._ID + "=?",
        new String[] {"" + id}, null);
    if (cursor != null) {
      MediaVideo video = null;
      if (cursor.moveToNext()) {
        video = parseVideo(cursor);
      }
      cursor.close();
      return video;
    }
    return null;
  }

  public Thumbnail getVideoThumbnail(long id) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 8;

    Bitmap thumbnail = getVideoThumbnailBitmap(ctx, id,
        MediaStore.Video.Thumbnails.MICRO_KIND, options);

    if (thumbnail != null) {
      Thumbnail.Builder builder = new Thumbnail.Builder();

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      thumbnail.compress(CompressFormat.PNG, 100, bos);
      byte[] data = bos.toByteArray();
      builder.thumbnail(ByteString.of(data, 0, data.length));

      return builder.build();
    } else {
      return null;
    }
  }

  @TargetApi(8)
  public Thumbnail getVideoThumbnail(long id, int width, int height) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap thumbnail = getVideoThumbnailBitmap(ctx, id,
        MediaStore.Video.Thumbnails.MINI_KIND, options);
    if (thumbnail != null) {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
        thumbnail = android.media.ThumbnailUtils.extractThumbnail(
            thumbnail, width, height);
      } else {
        thumbnail = ImageUtil
            .scaleDown(thumbnail, width, height, false);
      }
      Thumbnail.Builder builder = new Thumbnail.Builder();

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      thumbnail.compress(CompressFormat.PNG, 100, bos);
      byte[] data = bos.toByteArray();
      builder.thumbnail(ByteString.of(data, 0, data.length));

      return builder.build();
    } else {
      return null;
    }
  }

  @TargetApi(8)
  public static Bitmap getThumbnail(Context ctx, long id) {
    Bitmap thumbnail = getVideoThumbnailBitmap(ctx, id,
        MediaStore.Video.Thumbnails.MINI_KIND, null);

    if (thumbnail != null) {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
        Bitmap bitmap = thumbnail;
        thumbnail = android.media.ThumbnailUtils.extractThumbnail(
            thumbnail, MAX_SIDE_LEN, MAX_SIDE_LEN);
      } else {
        thumbnail = ImageUtil.scaleDown(thumbnail, MAX_SIDE_LEN,
            MAX_SIDE_LEN, true);
      }
    }
    return thumbnail;
  }

  @TargetApi(8)
  public static Bitmap getThumbnail(Context ctx, String path) {
    Bitmap thumbnail = getVideoThumbnailBitmap(ctx, path,
        MediaStore.Video.Thumbnails.FULL_SCREEN_KIND, null);

    if (thumbnail != null) {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
        Bitmap bitmap = thumbnail;
        thumbnail = android.media.ThumbnailUtils.extractThumbnail(
            thumbnail, MAX_SIDE_LEN, MAX_SIDE_LEN);
      } else {
        thumbnail = ImageUtil.scaleDown(thumbnail, MAX_SIDE_LEN,
            MAX_SIDE_LEN, true);
      }
    }
    return thumbnail;
  }

  @SuppressLint("NewApi")
  private static Bitmap getVideoThumbnailBitmap(Context ctx, long id,
      int kind, BitmapFactory.Options options) {
    String videoPath = null;
    Cursor cursor = ctx.getContentResolver().query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.MediaColumns.DATA},
        MediaStore.Video.Media._ID + "=?", new String[] {"" + id},
        null);
    if (cursor != null) {
      if (cursor.moveToNext()) {
        videoPath = cursor.getString(cursor
            .getColumnIndex(MediaStore.MediaColumns.DATA));
      }
      cursor.close();
    }

    Bitmap thumbnail = null;
    if (videoPath != null) {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
        thumbnail = android.media.ThumbnailUtils.createVideoThumbnail(
            videoPath, kind);
      } else {
        // TODO: need to implement createVideoThumbnail，getThumbnail is
        // not correct sometimes (G9)
        thumbnail = MediaStore.Video.Thumbnails.getThumbnail(ctx
            .getContentResolver(), id, kind, options);
      }
    }
    return thumbnail;
  }

  @SuppressLint("NewApi")
  private static Bitmap getVideoThumbnailBitmap(Context ctx, String videoPath,
      int kind, BitmapFactory.Options options) {
    long id = -1;
    String escapedPath = videoPath.replace("'", "''");
    Cursor cursor = ctx.getContentResolver().query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.Video.Media._ID},
        MediaStore.MediaColumns.DATA + "='" + escapedPath + "'", null,
        null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        int idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
        id = cursor.getLong(idIndex);
      }
      cursor.close();
    }

    Bitmap thumbnail = null;
    if (videoPath != null) {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
        thumbnail = android.media.ThumbnailUtils.createVideoThumbnail(
            videoPath, kind);
      } else {
        // TODO: need to implement createVideoThumbnail，getThumbnail is
        // not correct sometimes (G9)
        thumbnail = MediaStore.Video.Thumbnails.getThumbnail(ctx
            .getContentResolver(), id, kind, options);
      }
    }
    return thumbnail;
  }

  public static Bitmap getGalleryThumbnail(Context ctx, long id) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(ctx
        .getContentResolver(), id,
        MediaStore.Video.Thumbnails.MINI_KIND, options);
    return thumbnail;
  }

  public boolean deleteVideo(long id) {
    MediaVideo video = getVideoById(id);
    if (video != null) {
      String whereClause = MediaStore.MediaColumns._ID + "=" + id;
      int count = cr.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
          whereClause, null);
      if (count > 0) {
        if (video.path != null) {
          File file = new File(video.path);
          file.delete();
        }
        return true;
      }
    }
    return false;
  }

  public boolean updateVideo(MediaVideo video) {
    // TODO
    return false;
  }

  public MediaVideos getVideos() throws InterruptedException {
    return getVideos(OFFSET_BEGIN);
  }

  public MediaVideos getVideos(int offset)
      throws InterruptedException {
    return getVideos(offset, MAX_LENGTH);
  }

  public MediaVideos getVideos(int offset, int length)
      throws InterruptedException {
    MediaVideos.Builder videos = new MediaVideos.Builder();
    videos.video = new ArrayList<MediaVideo>();

    Cursor cursor = null;
    try {
      cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
          null, null, null, MediaStore.MediaColumns.DATE_ADDED
              + " DESC LIMIT " + length + " OFFSET " + offset);
      if (cursor != null) {
        int count = 0;
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          videos.video.add(parseVideo(cursor));
          count++;
          if (length != MAX_LENGTH && count >= length) {
            break;
          }
        }
        cursor.close();
      }
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }

    return videos.build();
  }

  public static long getVideoIdByPath(String url) {
    long id = -1;
    String escapedUrl = url.replace("'", "''");
    Cursor cursor = GlobalConfig.getAppContext()
        .getContentResolver().query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            new String[] {MediaStore.Video.Media._ID},
            MediaStore.MediaColumns.DATA + "='" + escapedUrl + "'", null,
            null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        int idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
        id = cursor.getLong(idIndex);
      }
      cursor.close();
    }
    return id;
  }

  public static MediaVideo parseVideo(Cursor cursor) {
    if (cursor == null || cursor.isClosed()) {
      return null;
    }
    VideoColumnIndex columnIndex = VideoColumnIndex.getInstance(cursor);

    MediaVideo.Builder builder = new MediaVideo.Builder();

    if (isColumnValid(cursor, columnIndex.Id)) {
      builder.id(cursor.getLong(columnIndex.Id));
    }
    if (isColumnValid(cursor, columnIndex.Desc)) {
      builder.description(cursor.getString(columnIndex.Desc));
    }
    if (isColumnValid(cursor, columnIndex.IsPrivate)) {
      builder.is_private(cursor.getInt(columnIndex.IsPrivate));
    }
    if (isColumnValid(cursor, columnIndex.Latitude)) {
      builder.latitude(cursor.getDouble(columnIndex.Latitude));
    }
    if (isColumnValid(cursor, columnIndex.Longitude)) {
      builder.longitude(cursor.getDouble(columnIndex.Longitude));
    }
    if (isColumnValid(cursor, columnIndex.DateTaken)) {
      builder.date_taken(cursor.getLong(columnIndex.DateTaken));
    }
    if (isColumnValid(cursor, columnIndex.BucketName)) {
      builder.bucket_name(cursor.getString(columnIndex.BucketName));
    }
    if (isColumnValid(cursor, columnIndex.Resolution)) {
      builder.resolution(cursor.getString(columnIndex.Resolution));
    }
    if (isColumnValid(cursor, columnIndex.Album)) {
      builder.album(cursor.getString(columnIndex.Album));
    }
    if (isColumnValid(cursor, columnIndex.Artist)) {
      builder.artist(cursor.getString(columnIndex.Artist));
    }
    if (isColumnValid(cursor, columnIndex.Duration)) {
      builder.duration(cursor.getLong(columnIndex.Duration));
    }
    if (isColumnValid(cursor, columnIndex.Bookmark)) {
      builder.bookmark(cursor.getLong(columnIndex.Bookmark));
    }
    if (isColumnValid(cursor, columnIndex.Path)) {
      String path = cursor.getString(columnIndex.Path);
      builder.path(PathAdjustUtil.adjustSdcardPathForAdb(path));
    }
    if (isColumnValid(cursor, columnIndex.DisplayName)) {
      builder.display_name(cursor.getString(columnIndex.DisplayName));
    }
    if (isColumnValid(cursor, columnIndex.Size)) {
      builder.size(cursor.getLong(columnIndex.Size));
    }
    if (isColumnValid(cursor, columnIndex.MimeType)) {
      builder.mime_type(cursor.getString(columnIndex.MimeType));
    }
    if (isColumnValid(cursor, columnIndex.DateAdded)) {
      builder.date_added(cursor.getLong(columnIndex.DateAdded));
    }
    if (isColumnValid(cursor, columnIndex.DateModified)) {
      builder.date_modified(cursor.getLong(columnIndex.DateModified));
    }
    if (isColumnValid(cursor, columnIndex.Title)) {
      builder.title(cursor.getString(columnIndex.Title));
    }

    return builder.build();
  }

  private static boolean isColumnValid(Cursor cursor, int index) {
    return index != -1 && !cursor.isNull(index);
  }

  // public static void playVideo(final Context context, String filePath) {
  // if (filePath != null) {
  // File file = new File(filePath);
  // if (file.exists()) {
  // String mimeType = "video/" + filePath.substring(filePath.lastIndexOf(".") + 1);
  // VideoManager.open(context, filePath, mimeType,
  // MediaManager.TYPE_VIDEO);
  // return;
  // }
  // }
  // AlertDialog.Builder builder = new AlertDialog.Builder(context);
  // builder.setTitle(context.getString(R.string.tips));
  // builder.setMessage(context.getString(
  // R.string.video_open_failed_message));
  // builder.setPositiveButton(context.getString(R.string.video_manage),
  // new DialogInterface.OnClickListener() {
  //
  // @Override
  // public void onClick(DialogInterface dialog, int which) {
  // Intent intent =
  // new Intent(context, VideoManagementActivity.class);
  // context.startActivity(intent);
  // }
  // });
  // builder.setNegativeButton(context.getString(R.string.cancel),
  // new DialogInterface.OnClickListener() {
  //
  // @Override
  // public void onClick(DialogInterface dialog, int which) {}
  // });
  // builder.create().show();
  // }

}
