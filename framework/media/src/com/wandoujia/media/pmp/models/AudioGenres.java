// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./audio.proto
package com.wandoujia.media.pmp.models;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;
import java.util.Collections;
import java.util.List;

import static com.squareup.wire.Message.Label.REPEATED;

public final class AudioGenres extends Message {

  public static final List<AudioGenre> DEFAULT_GENRE = Collections.emptyList();

  @ProtoField(tag = 1, label = REPEATED)
  public final List<AudioGenre> genre;

  private AudioGenres(Builder builder) {
    super(builder);
    this.genre = immutableCopyOf(builder.genre);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof AudioGenres)) return false;
    return equals(genre, ((AudioGenres) other).genre);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = genre != null ? genre.hashCode() : 1);
  }

  public static final class Builder extends Message.Builder<AudioGenres> {

    public List<AudioGenre> genre;

    public Builder() {
    }

    public Builder(AudioGenres message) {
      super(message);
      if (message == null) return;
      this.genre = copyOf(message.genre);
    }

    public Builder genre(List<AudioGenre> genre) {
      this.genre = checkForNulls(genre);
      return this;
    }

    @Override
    public AudioGenres build() {
      return new AudioGenres(this);
    }
  }
}
