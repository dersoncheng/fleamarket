package com.wandoujia.media;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;

import com.squareup.wire.ByteString;
import com.wandoujia.base.utils.DateUtil;
import com.wandoujia.base.utils.ImageUtil;
import com.wandoujia.base.utils.PathAdjustUtil;
import com.wandoujia.base.utils.ThreadUtil;
import com.wandoujia.media.pmp.models.MediaImage;
import com.wandoujia.media.pmp.models.MediaImages;
import com.wandoujia.media.pmp.models.Thumbnail;
import android.media.ThumbnailUtils;

public class ImageManager implements MediaConstants {
  private ContentResolver cr;
  private Context ctx;

  private ImageManager(Context ctx) {
    this.ctx = ctx;
    this.cr = ctx.getContentResolver();
  }

  private static ImageManager instance;

  public static ImageManager getInstance(Context context) {
    if (instance == null) {
      instance = new ImageManager(context.getApplicationContext());
    }

    return instance;
  }

  private static class ImageColumnIndex {
    public int Id = -1;
    public int Desc = -1;
    public int Picasa = -1;
    public int IsPrivate = -1;
    public int Latitude = -1;
    public int Longitude = -1;
    public int DateTaken = -1;
    public int Orientation = -1;
    public int BucketName = -1;
    public int Path = -1;
    public int DisplayName = -1;
    public int Size = -1;
    public int MimeType = -1;
    public int DateAdded = -1;
    public int DateModified = -1;
    public int Title = -1;
    public int Width = -1;
    public int Height = -1;

    private ImageColumnIndex(Cursor cursor) {
      Id = cursor.getColumnIndex(Media._ID);
      Desc = cursor.getColumnIndex(Media.DESCRIPTION);
      Picasa = cursor.getColumnIndex(Media.PICASA_ID);
      IsPrivate = cursor
          .getColumnIndex(Media.IS_PRIVATE);
      Latitude = cursor.getColumnIndex(Media.LATITUDE);
      Longitude = cursor
          .getColumnIndex(Media.LONGITUDE);
      DateTaken = cursor
          .getColumnIndex(Media.DATE_TAKEN);
      Orientation = cursor
          .getColumnIndex(Media.ORIENTATION);
      BucketName = cursor
          .getColumnIndex(Media.BUCKET_DISPLAY_NAME);

      Path = cursor.getColumnIndex(MediaColumns.DATA);
      DisplayName = cursor
          .getColumnIndex(MediaColumns.DISPLAY_NAME);
      Size = cursor.getColumnIndex(MediaColumns.SIZE);
      MimeType = cursor.getColumnIndex(MediaColumns.MIME_TYPE);
      DateAdded = cursor
          .getColumnIndex(MediaColumns.DATE_ADDED);
      DateModified = cursor
          .getColumnIndex(MediaColumns.DATE_MODIFIED);
      Title = cursor.getColumnIndex(MediaColumns.TITLE);
      Width = cursor.getColumnIndex(Media.WIDTH);
      Height = cursor.getColumnIndex(Media.HEIGHT);
    }

    private static ImageColumnIndex instance = null;

    public static ImageColumnIndex getInstance(Cursor cursor) {
      if (instance == null) {
        instance = new ImageColumnIndex(cursor);
      }

      return instance;
    }
  }

  public int getImageCount() {
    Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI,
        new String[] {Media._ID}, null, null, null);
    int count = 0;
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    return count;
  }

  public long getAllImageSize() throws InterruptedException {
    Cursor cursor = null;
    try {
      cursor = cr.query(Media.EXTERNAL_CONTENT_URI,
          new String[] {Media.SIZE}, null, null,
          null);
      if (cursor != null) {
        long sizeAll = 0;
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          if (isColumnValid(cursor, 0)) {
            sizeAll += cursor.getLong(0);
          }
        }
        cursor.close();
        return sizeAll;
      }
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }
    return 0;
  }

  public MediaImage getImageById(long id) {
    return getImageById(id, true);
  }

  public MediaImage getImageById(long id, boolean needFolder) {
    Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI,
        null, Media._ID + "=?", new String[] {""
            + id
        }, null);
    if (cursor != null) {
      MediaImage image = null;
      if (cursor.moveToNext()) {
        image = parseImage(cursor, needFolder);
      }
      cursor.close();
      return image;
    }
    return null;
  }

  public HashSet<Long> getImageIdSet() {
    HashSet<Long> idSet = new HashSet<Long>();
    Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI,
        new String[] {Media._ID}, null, null, null);
    if (cursor != null) {
      int idIndex = cursor.getColumnIndex(Media._ID);
      while (cursor.moveToNext()) {
        long id = cursor.getLong(idIndex);
        idSet.add(id);
      }
      cursor.close();
    }
    return idSet;
  }

  public MediaImages getImages() throws InterruptedException {
    return getImages(OFFSET_BEGIN);
  }

  public MediaImages getImages(int offset)
      throws InterruptedException {
    return getImages(offset, MAX_LENGTH);
  }

  public boolean deleteImage(long id) {
    MediaImage image = getImageById(id);
    if (image != null) {
      String whereClause = MediaColumns._ID + "=" + id;
      int count = cr.delete(Media.EXTERNAL_CONTENT_URI,
          whereClause, null);
      if (count > 0) {
        if (image.path != null) {
          File file = new File(image.path);
          file.delete();
        }
        return true;
      }
    }
    return false;
  }

  public boolean updateImage(MediaImage image) {
    // TODO
    return false;
  }

  public boolean rotate(long id, int degree)
      throws UnsupportRatoteDegreeException {
    if (degree == 0 || degree == 90 || degree == 180 || degree == 270) {
      try {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.ImageColumns.ORIENTATION, degree);

        int rotatedCount = cr.update(
            Media.EXTERNAL_CONTENT_URI, cv,
            Media._ID + "=?", new String[] {""
                + id
            });
        return rotatedCount > 0;
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    } else {
      throw new UnsupportRatoteDegreeException();
    }
  }

  public Thumbnail getImageThumbnail(long id) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 8;
    Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, id,
        MediaStore.Images.Thumbnails.MICRO_KIND, options);

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

  public Thumbnail getImageThumbnail(long id, int width, int height)
      throws MediaManageException {
    return getImageThumbnail(id, width, height, 100);
  }

  public Thumbnail getImageThumbnail(long id, int width, int height, int quality)
      throws MediaManageException {
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, id,
        MediaStore.Images.Thumbnails.MINI_KIND, options);

    if (thumbnail != null) {
      Bitmap scaledThumbnail = ImageUtil.scaleDown(thumbnail, width,
          height);

      Thumbnail.Builder builder = new Thumbnail.Builder();

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      scaledThumbnail.compress(CompressFormat.PNG, quality, bos);
      byte[] data = bos.toByteArray();
      builder.thumbnail(ByteString.of(data, 0, data.length));

      return builder.build();
    } else {
      throw new MediaManageException(MediaManageException.
          ErrorCode.FILE_NOT_FOUND);
    }
  }

  public Thumbnail getImageThumbnailByPath(String path)
    throws MediaManageException {
    BitmapFactory.Options options = new BitmapFactory.Options();
    return getImageThumbnailByPath(path, options.outWidth, options.outHeight, 100);
  }

  public Thumbnail getImageThumbnailByPath(String path, int width, int height)
    throws MediaManageException {
    return getImageThumbnailByPath(path, width, height, 100);
  }

  public Thumbnail getImageThumbnailByPath(String path, int width, int height, int quality)
    throws MediaManageException {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    Bitmap thumbnail = BitmapFactory.decodeFile(path, options);
    options.inJustDecodeBounds = false;
    int h = options.outHeight;
    int w = options.outWidth;
    int beWidth = w / width;
    int beHeight = h / height;
    int be = 1;
    if (beWidth < beHeight) {
      be = beWidth;
    } else {
      be = beHeight;
    }
    if (be <= 0) {
      be = 1;
    }
    options.inSampleSize = be;
    thumbnail = BitmapFactory.decodeFile(path, options);
    thumbnail = ThumbnailUtils.extractThumbnail(thumbnail, width, height,
      ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

    if (thumbnail != null) {
      Bitmap scaledThumbnail = ImageUtil.scaleDown(thumbnail, width, height);
      Thumbnail.Builder builder = new Thumbnail.Builder();

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      scaledThumbnail.compress(CompressFormat.PNG, quality, bos);
      byte[] data = bos.toByteArray();
      builder.thumbnail(ByteString.of(data, 0, data.length));

      return builder.build();
    } else {
      throw new MediaManageException(MediaManageException.
        ErrorCode.FILE_NOT_FOUND);
    }
  }

  public byte[] getImageThumbnailBytes(long id, int width, int height)
      throws MediaManageException {
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, id,
        MediaStore.Images.Thumbnails.MINI_KIND, options);
    if (thumbnail != null) {
      Bitmap scaledThumbnail = ImageUtil.scaleDown(thumbnail, width,
          height);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      scaledThumbnail.compress(CompressFormat.PNG, 100, bos);

      return bos.toByteArray();
    } else {
      throw new MediaManageException(
          MediaManageException.ErrorCode.FILE_NOT_FOUND);
    }
  }

  public MediaImages getImages(long start, boolean direction, long length) {
    return getImages(start, direction, length, true);
  }

  public MediaImages getImages(long start, boolean direction, long length,
      boolean needFolder) {
    MediaImages.Builder imagesBuilder = new MediaImages.Builder();
    List<MediaImage> imageList = new ArrayList<MediaImage>();
    imagesBuilder.image(imageList);
    Cursor cursor = null;
    try {
      String dir = direction ? "DESC" : "ASC";
      String limit = " LIMIT " + start + "," + length;
      cursor = cr.query(Media.EXTERNAL_CONTENT_URI, null,
          null, null, MediaColumns.DATE_ADDED + " " + dir + limit);
      if (cursor != null) {
        int count = 0;
        while (cursor.moveToNext()) {
          imageList.add(parseImage(cursor, needFolder));
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
    return imagesBuilder.build();
  }

  public MediaImages getImagesSince(long since, boolean direction, long length) {
    MediaImages.Builder imagesBuilder = new MediaImages.Builder();
    List<MediaImage> imageList = new ArrayList<MediaImage>();
    imagesBuilder.image(imageList);
    Cursor cursor = null;
    try {
      String dir = direction ? "DESC" : "ASC";
      String where = direction ? MediaColumns.DATE_ADDED + "<=" + since
          : MediaColumns.DATE_ADDED + ">=" + since;
      String limit = length > 0 ? " LIMIT " + length : "";
      cursor = cr.query(Media.EXTERNAL_CONTENT_URI, null,
          where, null, MediaColumns.DATE_ADDED + " " + dir + limit);
      if (cursor != null) {
        int count = 0;
        while (cursor.moveToNext()) {
          imageList.add(parseImage(cursor, false));
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
    return imagesBuilder.build();
  }

  public MediaImages getImages(int offset, int length)
      throws InterruptedException {
    MediaImages.Builder imagesBuilder = new MediaImages.Builder();
    List<MediaImage> imageList = new ArrayList<MediaImage>();
    imagesBuilder.image(imageList);

    Cursor cursor = null;
    try {
      cursor = cr.query(Media.EXTERNAL_CONTENT_URI,
          null, null, null, MediaColumns.DATE_ADDED
              + " DESC LIMIT " + length + " OFFSET " + offset);
      if (cursor != null) {
        int count = 0;
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          imageList.add(parseImage(cursor));
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

    return imagesBuilder.build();
  }

  public static Bitmap getThumbnail(Context ctx, long id) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 8;
    Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(ctx
        .getContentResolver(), id,
        MediaStore.Images.Thumbnails.MICRO_KIND, options);
    return thumbnail;
  }


  public byte[] getBigImageThumbnail(long id) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 3;
    Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, id,
        MediaStore.Images.Thumbnails.MINI_KIND, options);
    if (thumbnail != null) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      thumbnail.compress(CompressFormat.PNG, 100, bos);
      return bos.toByteArray();
    } else {
      return null;
    }
  }

  public boolean ignoreFolder(String path) {
    if (TextUtils.isEmpty(path)) {
      return false;
    }

    Cursor cursor = null;
    try {
      cursor = ctx.getContentResolver().query(
          CONTENT_IMAGE_FOLDERS_URI,
          new String[] {ImageFoldersColumns._ID,
              ImageFoldersColumns.FOLDER_PATH
          },
          ImageFoldersColumns.FOLDER_PATH + "='"
              + path + "'", null, null);
      if (cursor.moveToFirst()) {
        return true;
      } else {
        ContentValues cv = new ContentValues();
        cv.put(ImageFoldersColumns.FOLDER_PATH, path);
        Uri uri = ctx.getContentResolver().insert(CONTENT_IMAGE_FOLDERS_URI, cv);
        return uri != null;
      }
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }
  }

  public boolean unignoreFolder(String path) {
    if (TextUtils.isEmpty(path)) {
      return false;
    }

    try {
      ctx.getContentResolver().delete(
          CONTENT_IMAGE_FOLDERS_URI,
          ImageFoldersColumns.FOLDER_PATH + "='"
              + path + "'", null);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public int getFolderCount() {
    Cursor cursor = null;
    try {
      cursor = ctx.getContentResolver().query(
          CONTENT_IMAGE_FOLDERS_URI, null, null,
          null, null);
      return cursor.getCount();
    } catch (Exception e) {
      return 0;
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }
  }

  public List<String> listFolders(int offset, int length) {
    Cursor cursor = ctx.getContentResolver().query(
        CONTENT_IMAGE_FOLDERS_URI,
        new String[] {ImageFoldersColumns._ID,
            ImageFoldersColumns.FOLDER_PATH
        },
        null,
        null,
        ImageFoldersColumns._ID + " LIMIT " + length + " OFFSET "
            + offset);

    List<String> ignoredFolders = new ArrayList<String>();
    if (cursor != null) {
      while (cursor.moveToNext()) {
        String folderPath = cursor.getString(1);
        if (!TextUtils.isEmpty(folderPath)
            && !ignoredFolders.contains(folderPath)) {
          ignoredFolders.add(folderPath);
        }
      }

      cursor.close();
    }
    return ignoredFolders;
  }

  /**
   * get specified image's orientation
   * 
   * @param id
   * @return 0/90/180/270, 0 as default(exception and not exist)
   */
  public int getOrientation(long id) {
    Cursor cursor = Media.query(ctx.getContentResolver(),
        ContentUris.withAppendedId(
            Media.EXTERNAL_CONTENT_URI, id),
        new String[] {MediaStore.Images.ImageColumns.ORIENTATION});

    int orientation = 0;
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        orientation = cursor.getInt(0);
      }
      cursor.close();
    }

    return orientation;
  }

  /**
   * 
   * @param title
   * @param size
   * @return the Id in image database, -1 for not exist
   */
  public long exist(String title, long size) {
    Cursor cursor = null;
    try {
      cursor = ctx.getContentResolver().query(
          Media.EXTERNAL_CONTENT_URI,
          new String[] {Media._ID},
          Media.DATA + " Like '%" + title + "%' and " + Media.SIZE
              + " = ?", new String[] {size + ""}, null);
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          return cursor.getLong(0);
        } else {
          return -1;
        }
      }
      return -1;
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  public static MediaImage parseImage(Cursor cursor) {
    return parseImage(cursor, true);
  }

  public static MediaImage parseImage(Cursor cursor, boolean needFolder) {
    if (cursor == null || cursor.isClosed()) {
      return null;
    }
    MediaImage.Builder builder = new MediaImage.Builder();
    ImageColumnIndex columnIndex = ImageColumnIndex.getInstance(cursor);
    String path = "";

    if (isColumnValid(cursor, columnIndex.Id)) {
      builder.id(cursor.getLong(columnIndex.Id));
    }
    if (isColumnValid(cursor, columnIndex.Desc)) {
      builder.description(cursor.getString(columnIndex.Desc));
    }
    if (isColumnValid(cursor, columnIndex.Picasa)) {
      builder.picasa(cursor.getString(columnIndex.Picasa));
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
    if (isColumnValid(cursor, columnIndex.DateTaken))
      builder.date_taken(DateUtil.checkLongIsMillisecond(cursor.getLong(
          columnIndex.DateTaken)));
    if (isColumnValid(cursor, columnIndex.Orientation)) {
      builder.orientation(cursor.getInt(columnIndex.Orientation));
    }
    if (isColumnValid(cursor, columnIndex.BucketName)) {
      builder.bucket_name(cursor.getString(columnIndex.BucketName));
    }
    if (isColumnValid(cursor, columnIndex.Path)) {
      path = cursor.getString(columnIndex.Path);
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
    if (isColumnValid(cursor, columnIndex.DateAdded))
      builder.date_added(DateUtil.checkLongIsSecond(cursor.getLong(
          columnIndex.DateAdded)));
    if (isColumnValid(cursor, columnIndex.DateModified))
      builder.date_modified(DateUtil.checkLongIsSecond(cursor.getLong(
          columnIndex.DateModified)));
    if (isColumnValid(cursor, columnIndex.Title)) {
      builder.title(cursor.getString(columnIndex.Title));
    }
    if (needFolder) {
      if (!TextUtils.isEmpty(path)) {
        builder.folder_alias(PhotoUtils.getFolderAlias(path));
      } else {
        builder.folder_alias(PhotoUtils.getFolderAlias(""));
      }
    }
    if (isColumnValid(cursor, columnIndex.Width)) {
      builder.width(cursor.getInt(columnIndex.Width));
    }
    if (isColumnValid(cursor, columnIndex.Height)) {
      builder.height(cursor.getInt(columnIndex.Height));
    }
    return builder.build();
  }

  private static boolean isColumnValid(Cursor cursor, int index) {
    return index != -1 && !cursor.isNull(index);
  }

  public static void hideFolder(String folderString, Context context) {
    Cursor cursor = context.getContentResolver().query(
        CONTENT_IMAGE_FOLDERS_URI,
        new String[] {ImageFoldersColumns._ID},
        ImageFoldersColumns.FOLDER_PATH + "='"
            + folderString + "'", null, null);
    if (cursor == null) {
      return;
    }
    if (cursor.moveToFirst()) {
      cursor.close();
      return;
    }
    ContentValues value = new ContentValues();
    value.put(ImageFoldersColumns.FOLDER_PATH,
        folderString);
    context.getContentResolver().insert(
        CONTENT_IMAGE_FOLDERS_URI, value);
    cursor.close();
  }

  public static void showFolder(String folderString, Context context) {
    if (folderString == null) {
      return;
    }
    Cursor cursor = context
        .getContentResolver()
        .query(CONTENT_IMAGE_FOLDERS_URI,
            new String[] {ImageFoldersColumns.FOLDER_PATH},
            ImageFoldersColumns.FOLDER_PATH
                + "='" + folderString + "'", null, null);
    if (cursor == null) {
      return;
    }
    if (!cursor.moveToFirst()) {
      cursor.close();
      return;
    }
    context.getContentResolver().delete(
        CONTENT_IMAGE_FOLDERS_URI,
        ImageFoldersColumns.FOLDER_PATH + "='"
            + folderString + "'", null);
    cursor.close();
  }

}
