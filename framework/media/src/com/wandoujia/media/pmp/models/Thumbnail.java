// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./thumbnail.proto
package com.wandoujia.media.pmp.models;

import com.squareup.wire.ByteString;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.BYTES;

public final class Thumbnail extends Message {

  public static final ByteString DEFAULT_THUMBNAIL = ByteString.EMPTY;

  @ProtoField(tag = 1, type = BYTES)
  public final ByteString thumbnail;

  private Thumbnail(Builder builder) {
    super(builder);
    this.thumbnail = builder.thumbnail;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Thumbnail)) return false;
    return equals(thumbnail, ((Thumbnail) other).thumbnail);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = thumbnail != null ? thumbnail.hashCode() : 0);
  }

  public static final class Builder extends Message.Builder<Thumbnail> {

    public ByteString thumbnail;

    public Builder() {
    }

    public Builder(Thumbnail message) {
      super(message);
      if (message == null) return;
      this.thumbnail = message.thumbnail;
    }

    public Builder thumbnail(ByteString thumbnail) {
      this.thumbnail = thumbnail;
      return this;
    }

    @Override
    public Thumbnail build() {
      return new Thumbnail(this);
    }
  }
}