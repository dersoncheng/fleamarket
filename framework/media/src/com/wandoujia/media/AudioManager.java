package com.wandoujia.media;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.squareup.wire.ByteString;
import com.squareup.wire.Wire;
import com.wandoujia.base.utils.PathAdjustUtil;
import com.wandoujia.base.utils.ThreadUtil;
import com.wandoujia.media.pmp.models.AudioAlbum;
import com.wandoujia.media.pmp.models.AudioAlbums;
import com.wandoujia.media.pmp.models.AudioArtist;
import com.wandoujia.media.pmp.models.AudioArtists;
import com.wandoujia.media.pmp.models.AudioGenre;
import com.wandoujia.media.pmp.models.AudioGenres;
import com.wandoujia.media.pmp.models.AudioPlayList;
import com.wandoujia.media.pmp.models.AudioPlayLists;
import com.wandoujia.media.pmp.models.MediaAudio;
import com.wandoujia.media.pmp.models.MediaAudios;
import com.wandoujia.media.pmp.models.Thumbnail;

public class AudioManager implements MediaConstants {
  ContentResolver cr = null;

  private ContentValues[] contentValuesCache;

  // These constants are not part of the public API
  public static final String EXTERNAL_VOLUME_NAME = "external";
  public static final String INTERNAL_VOLUME_NAME = "internal";

  private Context ctx;

  private AudioManager(Context ctx) {
    this.ctx = ctx;
    cr = ctx.getContentResolver();
  }

  private static AudioManager instance;

  public static AudioManager getInstance(Context context) {
    if (instance == null) {
      instance = new AudioManager(context.getApplicationContext());
    }

    return instance;
  }

  private static class AudioColumnIndex {
    public int Album = -1;
    public int Composer = -1;
    public int Artist = -1;
    public int Album_Id = -1;
    public int Artist_Id = -1;
    public int Track = -1;
    public int Year = -1;
    public int Bookmark = -1;
    public int Duration = -1;
    public int IsAlarm = -1;
    public int IsMusic = -1;
    public int IsNotification = -1;
    public int IsPodcast = -1;
    public int IsRingtone = -1;
    public int Id = -1;
    public int Path = -1;
    public int DisplayName = -1;
    public int Size = -1;
    public int MimeType = -1;
    public int DateAdded = -1;
    public int DateModified = -1;
    public int Title = -1;

    private AudioColumnIndex(Cursor cursor) {
      Album = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
      Composer = cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);
      Artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
      Album_Id = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
      Artist_Id = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
      Track = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
      Year = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
      Bookmark = cursor.getColumnIndex(MediaStore.Audio.Media.BOOKMARK);
      Duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
      IsAlarm = cursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM);
      IsMusic = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
      IsNotification = cursor
          .getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION);
      IsPodcast = cursor
          .getColumnIndex(MediaStore.Audio.Media.IS_PODCAST);
      IsRingtone = cursor
          .getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE);

      Id = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
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

    private static AudioColumnIndex instance = null;

    public static AudioColumnIndex getInstance(Cursor cursor) {
      if (instance == null) {
        instance = new AudioColumnIndex(cursor);
      }

      return instance;
    }
  }

  private static class AlbumColumnIndex {
    public int Id = -1;
    public int Album = -1;
    public int Artist = -1;
    public int FirstYear = -1;
    public int LastYear = -1;
    public int NumberOfSongs = -1;
    public int NumberForArtist = -1;
    public int Album_Id = -1;
    public int Album_Art = -1;

    private AlbumColumnIndex(Cursor cursor) {
      Id = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
      Album = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
      Artist = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
      FirstYear = cursor
          .getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR);
      LastYear = cursor.getColumnIndex(MediaStore.Audio.Albums.LAST_YEAR);
      NumberOfSongs = cursor
          .getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
      NumberForArtist = cursor
          .getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
      Album_Id = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);
      Album_Art = cursor
          .getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
    }

    private static AlbumColumnIndex instance = null;

    public static AlbumColumnIndex getInstance(Cursor cursor) {
      if (instance == null) {
        instance = new AlbumColumnIndex(cursor);
      }

      return instance;
    }
  }

  private static class GenreColumnIndex {
    public int Id = -1;
    public int Name = -1;

    private GenreColumnIndex(Cursor cursor) {
      Id = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
      Name = cursor.getColumnIndex(MediaStore.Audio.Genres.NAME);
    }

    private static GenreColumnIndex instance = null;

    public static GenreColumnIndex getInstance(Cursor cursor) {
      if (instance == null) {
        instance = new GenreColumnIndex(cursor);
      }

      return instance;
    }
  }

  private static class ArtistColumnIndex {
    public int Id = -1;
    public int Art = -1;
    public int NumAlbum = -1;
    public int NumTrack = -1;

    private ArtistColumnIndex(Cursor cursor) {
      Id = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
      Art = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
      NumAlbum = cursor
          .getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
      NumTrack = cursor
          .getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
    }

    private static ArtistColumnIndex instance = null;

    public static ArtistColumnIndex getInstance(Cursor cursor) {
      if (instance == null) {
        instance = new ArtistColumnIndex(cursor);
      }

      return instance;
    }
  }

  private static class PlayListColumnIndex {
    public int Id = -1;
    public int Name = -1;
    public int DateAdded = -1;
    public int DateModified = -1;

    private PlayListColumnIndex(Cursor cursor) {
      Id = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
      Name = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
      DateAdded = cursor
          .getColumnIndex(MediaStore.Audio.Playlists.DATE_ADDED);
      DateModified = cursor
          .getColumnIndex(MediaStore.Audio.Playlists.DATE_MODIFIED);
    }

    private static PlayListColumnIndex instance = null;

    public static PlayListColumnIndex getInstance(Cursor cursor) {
      if (instance == null) {
        instance = new PlayListColumnIndex(cursor);
      }

      return instance;
    }
  }

  public int getAudioCount() {
    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.Audio.Media._ID}, null, null, null);
    int count = 0;
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    return count;
  }

  public long getAllAudioSize() throws InterruptedException {
    Cursor cursor = null;
    try {
      cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
          new String[] {MediaStore.Audio.Media.SIZE}, null, null,
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
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }

    return 0;
  }

  public MediaAudio getAudioById(long id) {
    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Media._ID + "=?",
        new String[] {"" + id}, null);
    if (cursor != null) {
      MediaAudio audio = null;
      if (cursor.moveToNext()) {
        audio = parseAudio(ctx, cursor);
      }
      cursor.close();
      return audio;
    }
    return null;
  }

  public MediaAudios getAudios() throws InterruptedException {
    return getAudios(OFFSET_BEGIN);
  }

  public MediaAudios getAudios(int offset)
      throws InterruptedException {
    return getAudios(offset, MAX_LENGTH);
  }

  public MediaAudios getAudios(int offset, int length)
      throws InterruptedException {
    MediaAudios.Builder audios = new MediaAudios.Builder();

    Cursor cursor = null;
    try {
      cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
          null, null, null, MediaStore.MediaColumns.DATE_ADDED
              + " DESC LIMIT " + length + " OFFSET " + offset);
      if (cursor != null) {
        int count = 0;
        audios.audio = new LinkedList<MediaAudio>();
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();
          audios.audio.add(parseAudio(ctx, cursor));
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

    return audios.build();
  }

  public boolean deleteAudio(long id) {
    // TODO check if it is necessary to delete related datas such as Album,
    // PlayList etc.
    MediaAudio audio = getAudioById(id);
    if (audio != null) {
      String whereClause = MediaStore.MediaColumns._ID + "=" + id;
      int count = cr.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
          whereClause, null);
      if (count > 0) {
        if (audio.path != null) {
          File file = new File(audio.path);
          file.delete();
        }
        sendDelBroadCast();
        return true;
      }
    }
    return false;
  }

  public boolean updateAudio(MediaAudio audio) {
    // TODO
    return false;
  }

  public int getAlbumCount() {
    Cursor cursor = cr.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.Audio.Albums._ID}, null, null, null);
    int count = 0;
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    return count;
  }

  public AudioAlbum getAlbumById(long id) {
    Cursor cursor = cr.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Albums._ID + "=?", new String[] {""
            + id
        }, null);
    if (cursor != null) {
      AudioAlbum album = null;
      if (cursor.moveToNext()) {
        album = parseAlbum(cursor);
      }
      cursor.close();
      return album;
    }
    return null;
  }

  public static AudioAlbum getAlbumByName(Context ctx, String name) {
    Cursor cursor = ctx.getContentResolver().query(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
        MediaStore.Audio.Albums.ALBUM + "=?", new String[] {name},
        null);
    if (cursor != null) {
      AudioAlbum album = null;
      if (cursor.moveToNext()) {
        album = parseAlbum(cursor);
      }
      cursor.close();
      return album;
    }
    return null;
  }

  public AudioAlbums getAudioAlbums() throws InterruptedException {
    return getAlbums(OFFSET_BEGIN);
  }

  public AudioAlbums getAlbums(int offset)
      throws InterruptedException {
    return getAlbums(offset, MAX_LENGTH);
  }

  public AudioAlbums getAlbums(int offset, int length)
      throws InterruptedException {
    AudioAlbums.Builder albums = new AudioAlbums.Builder();

    Cursor cursor = null;
    try {
      cursor = cr.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
          null, null, null, MediaStore.Audio.Albums._ID + " LIMIT "
              + length + " OFFSET " + offset);
      if (cursor != null) {
        int count = 0;
        albums.album = new LinkedList<AudioAlbum>();
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();
          albums.album.add(parseAlbum(cursor));
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

    return albums.build();
  }

  public boolean deleteAudioAlbum(long albumId) {
    int count = 0;
    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Media.ALBUM_ID + "=?", new String[] {""
            + albumId
        }, null);

    if (cursor != null) {
      int pathCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

      if (pathCol != -1) {
        while (cursor.moveToNext()) {
          String path = cursor.getString(pathCol);
          new File(path).delete();
        }
      }
      cursor.close();
    }
    count = cr.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Audio.Media.ALBUM_ID + "=?", new String[] {""
            + albumId
        });
    sendDelBroadCast();
    return count > 0;
  }

  // FIXME
  public boolean deleteAudioAlbums(List<Long> albumIds) {
    int count = 0;
    StringBuilder ids = new StringBuilder("-1");
    for (Long albumId : albumIds) {
      ids.append(",").append(albumId);
    }
    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Media.ALBUM_ID + " in (?)",
        new String[] {ids.toString()}, null);
    if (cursor != null) {
      int pathCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
      if (pathCol != -1) {
        while (cursor.moveToNext()) {
          String path = cursor.getString(pathCol);
          new File(path).delete();
        }
      }
      cursor.close();
    }
    count = cr.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Audio.Media.ALBUM_ID + " in (?)", new String[] {ids
            .toString()
        });
    sendDelBroadCast();
    return count > 0;
  }

  public MediaAudios getAudioByAlbum(long albumId, int scanType)
      throws InterruptedException {
    MediaAudios.Builder audios = new MediaAudios.Builder();

    Cursor cursor = null;
    try {
      cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
          null, MediaStore.Audio.Media.ALBUM_ID + "=?",
          new String[] {"" + albumId},
          MediaStore.MediaColumns.DATE_ADDED + " DESC");
      if (cursor != null) {
        audios.audio = new LinkedList<MediaAudio>();
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          audios.audio.add(parseAudio(ctx, cursor));
        }
        cursor.close();
      }
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }

    return audios.build();
  }

  public boolean updateAlbum(AudioAlbum album) {
    // TODO
    return false;
  }

  public int getGenreCount() {
    Cursor cursor = cr.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.Audio.Genres._ID}, null, null, null);
    int count = 0;
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    return count;
  }

  public AudioGenre getGenreById(long id) {
    Cursor cursor = cr.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Genres._ID + "=?", new String[] {""
            + id
        }, null);
    if (cursor != null) {
      AudioGenre genre = null;
      if (cursor.moveToNext()) {
        genre = parseGenre(cursor);
      }
      cursor.close();
      return genre;
    }
    return null;
  }

  public AudioGenres getAudioGenres() throws InterruptedException {
    return getGenres(OFFSET_BEGIN);
  }

  public AudioGenres getGenres(int offset)
      throws InterruptedException {
    return getGenres(offset, MAX_LENGTH);
  }

  public AudioGenres getGenres(int offset, int length)
      throws InterruptedException {
    AudioGenres.Builder genres = new AudioGenres.Builder();

    Cursor cursor = null;
    try {
      cursor = cr.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
          null, null, null, MediaStore.Audio.Genres._ID + " LIMIT "
              + length + " OFFSET " + offset);
      if (cursor != null) {
        int count = 0;
        genres.genre = new LinkedList<AudioGenre>();
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          genres.genre.add(parseGenre(cursor));
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

    return genres.build();
  }

  public static int getAudioCountByGenre(Context ctx, long genreId) {
    Uri uri = MediaStore.Audio.Genres.Members.getContentUri(
        EXTERNAL_VOLUME_NAME, genreId);
    Cursor cur = ctx.getContentResolver()
        .query(uri, null, null, null, null);
    int count = 0;
    if (cur != null) {
      count = cur.getCount();
      cur.close();
    }
    return count;
  }

  public static int getAudioCountByPlayList(Context ctx, long playListId) {
    Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
        EXTERNAL_VOLUME_NAME, playListId);
    Cursor cur = ctx.getContentResolver()
        .query(uri, null, null, null, null);
    int count = 0;
    if (cur != null) {
      count = cur.getCount();
      cur.close();
    }
    return count;
  }

  public MediaAudios getAudioByGenre(long genreId, int scanType)
      throws InterruptedException {
    MediaAudios.Builder audios = new MediaAudios.Builder();

    Uri uri = MediaStore.Audio.Genres.Members.getContentUri(
        EXTERNAL_VOLUME_NAME, genreId);

    Cursor cur = null;
    try {
      cur = cr.query(uri, null, null, null,
          MediaStore.Audio.Genres.Members.DEFAULT_SORT_ORDER);
      if (cur != null) {
        audios.audio = new LinkedList<MediaAudio>();
        while (cur.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          audios.audio.add(parseAudio(ctx, cur));
        }
        cur.close();
      }
    } finally {
      if (cur != null && !cur.isClosed()) {
        cur.close();
      }
    }

    return audios.build();
  }

  public boolean updateGenre(AudioGenre genre) {
    // TODO
    return false;
  }

  public int getArtistCount() {
    Cursor cursor = cr
        .query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            new String[] {MediaStore.Audio.Artists._ID}, null,
            null, null);
    int count = 0;
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    return count;
  }

  public AudioArtist getArtistById(long id) {
    Cursor cursor = cr.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Artists._ID + "=?", new String[] {""
            + id
        }, null);
    if (cursor != null) {
      AudioArtist artist = null;
      if (cursor.moveToNext()) {
        artist = parseArtist(cursor);
      }
      cursor.close();
      return artist;
    }
    return null;
  }

  public AudioArtists getAudioArtists()
      throws InterruptedException {
    return getArtists(OFFSET_BEGIN);
  }

  public AudioArtists getArtists(int offset)
      throws InterruptedException {
    return getArtists(offset, MAX_LENGTH);
  }

  public AudioArtists getArtists(int offset, int length)
      throws InterruptedException {
    AudioArtists.Builder artists = new AudioArtists.Builder();

    Cursor cursor = null;
    try {
      cursor = cr.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
          null, null, null, MediaStore.Audio.Artists._ID + " LIMIT "
              + length + " OFFSET " + offset);
      if (cursor != null) {
        int count = 0;
        artists.artist = new LinkedList<AudioArtist>();
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          artists.artist.add(parseArtist(cursor));
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

    return artists.build();
  }

  public int getArtistAudioCount(long artistId) {
    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Media.ARTIST_ID + " =?",
        new String[] {"" + artistId}, null);
    if (cursor != null) {
      int count = cursor.getCount();
      cursor.close();
      return count;
    } else {
      return 0;
    }
  }

  public int getAlbumAudioCount(long artistId) {
    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Media.ALBUM_ID + " =?",
        new String[] {"" + artistId}, null);
    if (cursor != null) {
      int count = cursor.getCount();
      cursor.close();
      return count;
    } else {
      return 0;
    }
  }

  public boolean deleteAudioArtist(long artistId) {
    int count = 0;
    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Media.ARTIST_ID + " =?",
        new String[] {"" + artistId}, null);

    if (cursor != null) {
      int pathCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

      if (pathCol != -1) {
        while (cursor.moveToNext()) {
          String path = cursor.getString(pathCol);
          new File(path).delete();
        }
      }
      cursor.close();
    }
    count = cr.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Audio.Media.ARTIST_ID + "=?", new String[] {""
            + artistId
        });
    sendDelBroadCast();
    return count > 0;
  }

  // FIXME
  public boolean deleteAudioArtists(List<Long> artistIds) {
    int count = 0;
    StringBuilder ids = new StringBuilder("-1");
    for (Long artistId : artistIds) {
      ids.append(",").append(artistId);
    }
    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Media.ARTIST_ID + " in (?)",
        new String[] {ids.toString()}, null);
    if (cursor != null) {
      int pathCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
      if (pathCol != -1) {
        while (cursor.moveToNext()) {
          String path = cursor.getString(pathCol);
          new File(path).delete();
        }
      }
      cursor.close();
    }
    count = cr.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Audio.Media.ARTIST_ID + " in (?)",
        new String[] {ids.toString()});
    sendDelBroadCast();
    return count > 0;
  }

  public MediaAudios getAudioByArtist(long artistId, int scanType)
      throws InterruptedException {
    MediaAudios.Builder audios = new MediaAudios.Builder();

    AudioArtist artist = getArtistById(artistId);
    if (artist != null) {
      Cursor cursor = null;
      try {
        cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null, MediaStore.Audio.Media.ARTIST + "=?",
            new String[]{Wire.get(artist.artist, artist.DEFAULT_ARTIST)},
            MediaStore.MediaColumns.DATE_ADDED + " DESC");
        if (cursor != null) {
          audios.audio = new LinkedList<MediaAudio>();
          while (cursor.moveToNext()) {
            ThreadUtil.throwIfInterrupted();

            audios.audio.add(parseAudio(ctx, cursor));
          }
          cursor.close();
        }
      } finally {
        if (cursor != null && !cursor.isClosed()) {
          cursor.close();
        }
      }
    }

    return audios.build();
  }

  public boolean updateArtist(AudioArtist artist) {
    // TODO
    return false;
  }

  public int getPlayListCount() {
    Cursor cursor = cr.query(
        MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.Audio.Playlists._ID}, null, null,
        null);
    int count = 0;
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    return count;
  }

  public AudioPlayList getPlayListById(long id) {
    Cursor cursor = cr.query(
        MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null,
        MediaStore.Audio.Playlists._ID + "=?",
        new String[] {"" + id}, null);
    if (cursor != null) {
      AudioPlayList playList = null;
      if (cursor.moveToNext()) {
        playList = parsePlayList(cursor);
      }
      cursor.close();
      return playList;
    }
    return null;
  }

  public AudioPlayLists getPlayLists() throws InterruptedException {
    return getPlayLists(OFFSET_BEGIN);
  }

  public AudioPlayLists getPlayLists(int offset)
      throws InterruptedException {
    return getPlayLists(offset, MAX_LENGTH);
  }

  public AudioPlayLists getPlayLists(int offset, int length)
      throws InterruptedException {
    AudioPlayLists.Builder playLists = new AudioPlayLists.Builder();

    Cursor cursor = null;
    try {
      cursor = cr.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
          null, null, null, MediaStore.Audio.Playlists._ID
              + " LIMIT " + length + " OFFSET " + offset);
      if (cursor != null) {
        int count = 0;
        playLists.playList = new LinkedList<AudioPlayList>();
        while (cursor.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          playLists.playList.add(parsePlayList(cursor));
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

    return playLists.build();
  }

  public MediaAudios getAudioByPlayList(long playListId,
      int scanType) throws InterruptedException {
    MediaAudios.Builder audios = new MediaAudios.Builder();

    Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
        EXTERNAL_VOLUME_NAME, playListId);

    Cursor cur = null;
    try {
      cur = cr.query(uri, null, null, null,
          MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
      if (cur != null) {
        audios.audio = new LinkedList<MediaAudio>();
        while (cur.moveToNext()) {
          ThreadUtil.throwIfInterrupted();

          audios.audio.add(parseAudio(ctx, cur));
        }
        cur.close();
      }
    } finally {
      if (cur != null && !cur.isClosed()) {
        cur.close();
      }
    }

    return audios.build();
  }

  public boolean updatePlayList(AudioPlayList playList) {
    if (playList.id != null && playList.name != null) {
      ContentValues values = new ContentValues();
      values.put(MediaStore.Audio.Playlists.NAME, playList.name);

      int count = cr.update(
          MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values,
          MediaStore.Audio.Playlists._ID + "=?", new String[] {""
              + playList.id
          });
      return count > 0;
    }
    return false;
  }

  public static Bitmap getAudioThumbnail(Context ctx, long audioId,
      long albumId) {
    return MusicUtils.getArtwork(ctx, audioId, albumId);
  }

  public static Bitmap getAlbumThumbnail(String albumArt) {
    return BitmapFactory.decodeFile(albumArt);
  }

  public static Bitmap getDefaultThumbnail(Context context) {
    return MusicUtils.getDefaultArtwork(context);
  }

  public Thumbnail getAudioThumbnail(long audioId) {
    MediaAudio audio = getAudioById(audioId);

    if (audio != null) {
      Bitmap artwork = MusicUtils.getArtwork(ctx, audioId,
          Wire.get(audio.album_id, audio.DEFAULT_ALBUM_ID));
      if (artwork != null) {
        Thumbnail.Builder builder = new Thumbnail.Builder();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        artwork.compress(CompressFormat.PNG, 100, bos);
        byte[] data = bos.toByteArray();
        builder.thumbnail = ByteString.of(data, 0, data.length);

        return builder.build();
      }
    }
    return null;
  }

  public Thumbnail getAlbumThumbnail(long albumId) {
    AudioAlbum album = getAlbumById(albumId);

    if (album.album_art != null) {
      Bitmap artwork = BitmapFactory.decodeFile(
          Wire.get(album.album_art, album.DEFAULT_ALBUM_ART));
      if (artwork != null) {
        Thumbnail.Builder builder = new Thumbnail.Builder();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        artwork.compress(CompressFormat.PNG, 100, bos);
        byte[] data = bos.toByteArray();
        builder.thumbnail = ByteString.of(data, 0, data.length);

        return builder.build();
      }
    }
    return null;
  }

  public static ArtistInfo getArtistInfo(Context context, String artist) {
    ArtistInfo artistInfo = new ArtistInfo();
    Cursor cursor = context.getContentResolver().query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
        MediaStore.Audio.Media.ARTIST + "=?", new String[] {artist},
        MediaStore.MediaColumns.DATE_ADDED + " DESC");
    if (cursor != null) {
      HashSet<String> albumNames = new HashSet<String>();
      artistInfo.audioNumber = cursor.getCount();
      while (cursor.moveToNext()) {
        MediaAudio audio = parseAudio(context, cursor);
        if (audio.album_name != null) {
          albumNames.add(audio.album_name);
          AudioAlbum album = getAlbumByName(context, audio.album_name);
          if (album != null && album.album_art != null
              && artistInfo.thumbnail == null) {
            artistInfo.thumbnail = BitmapFactory.decodeFile(album.album_art);
          }
        }
      }
      artistInfo.albumNumber = albumNames.size();
      cursor.close();
    }
    return artistInfo;
  }

  public Thumbnail getArtistThumbnail(long artistId) {
    AudioArtist artist = getArtistById(artistId);
    return getArtistThumbnail(Wire.get(artist.artist, artist.DEFAULT_ARTIST));
  }

  public Thumbnail getArtistThumbnail(String artist) {
    Thumbnail thumbnail = null;

    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Audio.Media.ARTIST + "=?",
        new String[] {artist}, MediaStore.MediaColumns.DATE_ADDED
            + " DESC");

    if (cursor == null) {
      return null;
    }

    while (cursor.moveToNext()) {
      MediaAudio audio = parseAudio(ctx, cursor);
      if (audio.album_name != null) {
        AudioAlbum album = getAlbumByName(ctx, audio.album_name);
        if (album != null && album.album_art != null) {
          Bitmap artwork = BitmapFactory.decodeFile(album.album_art);
          if (artwork != null) {
            Thumbnail.Builder builder = new Thumbnail.Builder();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            artwork.compress(CompressFormat.PNG, 100, bos);
            byte[] data = bos.toByteArray();
            builder.thumbnail = ByteString.of(data, 0, data.length);

            thumbnail = builder.build();
            break;
          }
        }
      }
    }
    cursor.close();

    return thumbnail;
  }

  public static AudioPlayList parsePlayList(Cursor cursor) {
    AudioPlayList.Builder builder = new AudioPlayList.Builder();

    if (isColumnValid(cursor, PlayListColumnIndex.getInstance(cursor).Id))
      builder.id = cursor.getLong(PlayListColumnIndex
          .getInstance(cursor).Id);
    if (isColumnValid(cursor, PlayListColumnIndex.getInstance(cursor).Name))
      builder.name = cursor.getString(PlayListColumnIndex
          .getInstance(cursor).Name);
    if (isColumnValid(cursor,
        PlayListColumnIndex.getInstance(cursor).DateAdded))
      builder.date_added = cursor.getLong(PlayListColumnIndex
          .getInstance(cursor).DateAdded);
    if (isColumnValid(cursor,
        PlayListColumnIndex.getInstance(cursor).DateModified))
      builder.date_modified = cursor.getLong(PlayListColumnIndex
          .getInstance(cursor).DateModified);

    return builder.build();
  }

  public static AudioArtist parseArtist(Cursor cursor) {
    AudioArtist.Builder builder = new AudioArtist.Builder();

    if (isColumnValid(cursor, ArtistColumnIndex.getInstance(cursor).Id))
      builder.id = cursor
          .getLong(ArtistColumnIndex.getInstance(cursor).Id);
    if (isColumnValid(cursor, ArtistColumnIndex.getInstance(cursor).Art))
      builder.artist = cursor.getString(ArtistColumnIndex
          .getInstance(cursor).Art);
    if (isColumnValid(cursor,
        ArtistColumnIndex.getInstance(cursor).NumAlbum))
      builder.number_of_albums = cursor.getInt(ArtistColumnIndex
          .getInstance(cursor).NumAlbum);
    if (isColumnValid(cursor,
        ArtistColumnIndex.getInstance(cursor).NumTrack))
      builder.number_of_tracks = cursor.getInt(ArtistColumnIndex
          .getInstance(cursor).NumTrack);

    return builder.build();
  }

  public static AudioGenre parseGenre(Cursor cursor) {
    AudioGenre.Builder builder = new AudioGenre.Builder();

    if (isColumnValid(cursor, GenreColumnIndex.getInstance(cursor).Id))
      builder.id = cursor
          .getLong(GenreColumnIndex.getInstance(cursor).Id);
    if (isColumnValid(cursor, GenreColumnIndex.getInstance(cursor).Name))
      builder.name = cursor.getString(GenreColumnIndex
          .getInstance(cursor).Name);

    return builder.build();
  }

  public static AudioAlbum parseAlbum(Cursor cursor) {
    AudioAlbum.Builder builder = new AudioAlbum.Builder();

    if (isColumnValid(cursor, AlbumColumnIndex.getInstance(cursor).Id))
      builder.id = cursor
          .getLong(AlbumColumnIndex.getInstance(cursor).Id);
    if (isColumnValid(cursor, AlbumColumnIndex.getInstance(cursor).Album))
      builder.album = cursor.getString(AlbumColumnIndex
          .getInstance(cursor).Album);
    if (isColumnValid(cursor, AlbumColumnIndex.getInstance(cursor).Artist))
      builder.artist = cursor.getString(AlbumColumnIndex
          .getInstance(cursor).Artist);
    if (isColumnValid(cursor,
        AlbumColumnIndex.getInstance(cursor).FirstYear))
      builder.first_year = cursor.getInt(AlbumColumnIndex
          .getInstance(cursor).FirstYear);
    if (isColumnValid(cursor, AlbumColumnIndex.getInstance(cursor).LastYear))
      builder.last_year = cursor.getInt(AlbumColumnIndex
          .getInstance(cursor).LastYear);
    if (isColumnValid(cursor,
        AlbumColumnIndex.getInstance(cursor).NumberOfSongs))
      builder.number_of_songs = cursor.getInt(AlbumColumnIndex
          .getInstance(cursor).NumberOfSongs);
    if (isColumnValid(cursor,
        AlbumColumnIndex.getInstance(cursor).NumberForArtist))
      builder.number_of_songs_for_artist = cursor.getInt(AlbumColumnIndex
          .getInstance(cursor).NumberForArtist);
    if (isColumnValid(cursor, AlbumColumnIndex.getInstance(cursor).Album_Id))
      builder.album_id = cursor.getInt(AlbumColumnIndex
          .getInstance(cursor).Album_Id);
    if (isColumnValid(cursor,
        AlbumColumnIndex.getInstance(cursor).Album_Art))
      builder.album_art = cursor.getString(AlbumColumnIndex
          .getInstance(cursor).Album_Art);

    return builder.build();
  }

  public static MediaAudio parseAudio(Context context,
      Cursor cursor) {
    MediaAudio.Builder builder = new MediaAudio.Builder();

    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Id))
      builder.id = cursor.getLong(AudioColumnIndex.getInstance(cursor).Id);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Path)) {
      String path = cursor.getString(AudioColumnIndex.getInstance(cursor).Path);
      builder.path = PathAdjustUtil.adjustSdcardPathForAdb(path);
    }
    if (isColumnValid(cursor,
        AudioColumnIndex.getInstance(cursor).DisplayName))
      builder.display_name = cursor.getString(AudioColumnIndex
          .getInstance(cursor).DisplayName);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Size))
      builder.size = cursor
          .getLong(AudioColumnIndex.getInstance(cursor).Size);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).MimeType))
      builder.mime_type = cursor.getString(AudioColumnIndex
          .getInstance(cursor).MimeType);
    if (isColumnValid(cursor,
        AudioColumnIndex.getInstance(cursor).DateAdded))
      builder.date_added = cursor.getLong(AudioColumnIndex
          .getInstance(cursor).DateAdded);
    if (isColumnValid(cursor,
        AudioColumnIndex.getInstance(cursor).DateModified))
      builder.date_modified = cursor.getLong(AudioColumnIndex
          .getInstance(cursor).DateModified);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Title))
      builder.title = cursor.getString(AudioColumnIndex
          .getInstance(cursor).Title);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Album))
      builder.album_name = cursor.getString(AudioColumnIndex
          .getInstance(cursor).Album);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Composer))
      builder.composer_name = cursor.getString(AudioColumnIndex
          .getInstance(cursor).Composer);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Artist))
      builder.artist_name = cursor.getString(AudioColumnIndex
          .getInstance(cursor).Artist);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Album_Id))
      builder.album_id = cursor.getLong(AudioColumnIndex
          .getInstance(cursor).Album_Id);
    if (isColumnValid(cursor,
        AudioColumnIndex.getInstance(cursor).Artist_Id))
      builder.artist_id = cursor.getLong(AudioColumnIndex
          .getInstance(cursor).Artist_Id);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Track))
      builder.track_no = cursor.getInt(AudioColumnIndex
          .getInstance(cursor).Track);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Year))
      builder.year = cursor
          .getInt(AudioColumnIndex.getInstance(cursor).Year);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Bookmark))
      builder.bookmark = cursor.getLong(AudioColumnIndex
          .getInstance(cursor).Bookmark);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).Duration))
      builder.duration = cursor.getLong(AudioColumnIndex
          .getInstance(cursor).Duration);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).IsAlarm))
      builder.is_alarm = cursor.getInt(AudioColumnIndex
          .getInstance(cursor).IsAlarm);
    if (isColumnValid(cursor, AudioColumnIndex.getInstance(cursor).IsMusic))
      builder.is_music = cursor.getInt(AudioColumnIndex
          .getInstance(cursor).IsMusic);
    if (isColumnValid(cursor,
        AudioColumnIndex.getInstance(cursor).IsNotification))
      builder.is_notification = cursor.getInt(AudioColumnIndex
          .getInstance(cursor).IsNotification);
    if (isColumnValid(cursor,
        AudioColumnIndex.getInstance(cursor).IsPodcast))
      builder.is_podcast = cursor.getInt(AudioColumnIndex
          .getInstance(cursor).IsPodcast);
    if (isColumnValid(cursor,
        AudioColumnIndex.getInstance(cursor).IsRingtone))
      builder.is_ringtone = cursor.getInt(AudioColumnIndex
          .getInstance(cursor).IsRingtone);

    return builder.build();
  }

  private static boolean isColumnValid(Cursor cursor, int index) {
    return index != -1 && !cursor.isNull(index);
  }

  public static class ArtistInfo {
    public Bitmap thumbnail;
    public int albumNumber;
    public int audioNumber;
  }

  private void sendDelBroadCast() {
    ctx.sendBroadcast(new Intent(ACTION_DEL_AUDIO_SUCCESS));
  }

  public List<Long> insertPlaylist(String playlistName, List<Long> audioIds)
      throws MediaManageException {
    long playlistId = insertPlaylist(playlistName);

    if (audioIds != null && audioIds.size() > 0) {
      return addAudiosToPlaylist(playlistId, audioIds);
    } else {
      return new ArrayList<Long>();
    }
  }

  public List<Long> addAudiosToPlaylist(long playlistId, List<Long> audioIds)
      throws MediaManageException {
    if (audioIds == null) {
      throw new MediaManageException(MediaManageException.ErrorCode.INVALID_ID);
    } else {
      int size = audioIds.size();
      ContentResolver resolver = ctx.getContentResolver();
      // need to determine the number of items currently in the playlist,
      // so the play_order field can be maintained.
      String[] cols = new String[] {"count(*)"};
      Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
          "external", playlistId);
      Cursor cur = resolver.query(uri, cols, null, null, null);
      cur.moveToFirst();
      int base = cur.getInt(0);
      cur.close();
      int numInserted = 0;

      for (int i = 0; i < size; i += 1000) {
        makeInsertItems(audioIds.toArray(new Long[audioIds.size()]), i,
            1000, base);
        numInserted += resolver.bulkInsert(uri, contentValuesCache);
      }

      String idStr = TextUtils.join(",", audioIds);
      Cursor cursor = ctx
          .getContentResolver()
          .query(uri,
              new String[] {MediaStore.Audio.Playlists.Members.AUDIO_ID},
              MediaStore.Audio.Playlists.Members.AUDIO_ID
                  + " in (" + idStr + ")", null, null);
      List<Long> failedIds = new ArrayList<Long>();
      HashSet<Long> idMap = new HashSet<Long>();
      for (Long id : audioIds) {
        idMap.add(id);
      }
      if (cursor != null) {
        try {
          int idCol = cursor
              .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
          while (cursor.moveToNext()) {
            Long id = cursor.getLong(idCol);
            idMap.remove(id);
          }
        } finally {
          cursor.close();
        }
      }
      Iterator<Long> iter = idMap.iterator();
      while (iter.hasNext()) {
        failedIds.add(iter.next());
      }
      return failedIds;
    }

  }

  /**
   * @param ids
   *          The source array containing all the ids to be added to the
   *          playlist
   * @param offset
   *          Where in the 'ids' array we start reading
   * @param len
   *          How many items to copy during this pass
   * @param base
   *          The play order offset to use for this pass
   */
  private void makeInsertItems(Long[] ids, int offset, int len, int base) {
    // adjust 'len' if would extend beyond the end of the source array
    if (offset + len > ids.length) {
      len = ids.length - offset;
    }
    // allocate the ContentValues array, or reallocate if it is the wrong
    // size
    if (contentValuesCache == null || contentValuesCache.length != len) {
      contentValuesCache = new ContentValues[len];
    }
    // fill in the ContentValues array with the right values for this pass
    for (int i = 0; i < len; i++) {
      if (contentValuesCache[i] == null) {
        contentValuesCache[i] = new ContentValues();
      }

      contentValuesCache[i].put(
          MediaStore.Audio.Playlists.Members.PLAY_ORDER, base
              + offset + i);
      contentValuesCache[i].put(
          MediaStore.Audio.Playlists.Members.AUDIO_ID,
          ids[offset + i]);
    }
  }

  private long insertPlaylist(String playlistName)
      throws MediaManageException {
    if (playlistName != null && playlistName.length() > 0) {
      ContentResolver resolver = ctx.getContentResolver();
      int id = idForplaylist(playlistName);
      if (id >= 0) {
        throw new MediaManageException(
            MediaManageException.ErrorCode.NAME_EXIST);
      } else {
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, playlistName);
        Uri insertedUri = resolver
            .insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                values);
        if (insertedUri != null) {
          return Long.valueOf(insertedUri.getLastPathSegment());
        } else {
          throw new MediaManageException(
              MediaManageException.ErrorCode.GENERIC_FAILURE);
        }
      }
    } else {
      throw new MediaManageException(
          MediaManageException.ErrorCode.INVALID_NAME);
    }

  }

  // return the id of the playlist, if playlist not exist return -1
  private int idForplaylist(String playlistName) {
    Cursor c = query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
        new String[] {MediaStore.Audio.Playlists._ID},
        MediaStore.Audio.Playlists.NAME + "=?",
        new String[] {playlistName}, MediaStore.Audio.Playlists.NAME);
    int id = -1;
    if (c != null) {
      c.moveToFirst();
      if (!c.isAfterLast()) {
        id = c.getInt(0);
      }
      c.close();
    }
    return id;
  }

  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    return query(uri, projection, selection, selectionArgs, sortOrder, 0);
  }

  private Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder, int limit) {
    try {
      ContentResolver resolver = ctx.getContentResolver();
      if (resolver == null) {
        return null;
      }
      if (limit > 0) {
        uri = uri.buildUpon().appendQueryParameter("limit", "" + limit)
            .build();
      }
      return resolver.query(uri, projection, selection, selectionArgs,
          sortOrder);
    } catch (UnsupportedOperationException ex) {
      return null;
    }

  }
}
