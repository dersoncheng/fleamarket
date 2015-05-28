// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./thumbnail.proto
package com.wandoujia.media.pmp.models;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;
import java.util.Collections;
import java.util.List;

import static com.squareup.wire.Message.Label.REPEATED;

public final class Thumbnails extends Message {

  public static final List<ThumbnailItem> DEFAULT_THUMBNAILS = Collections.emptyList();

  @ProtoField(tag = 1, label = REPEATED)
  public final List<ThumbnailItem> thumbnails;

  private Thumbnails(Builder builder) {
    super(builder);
    this.thumbnails = immutableCopyOf(builder.thumbnails);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Thumbnails)) return false;
    return equals(thumbnails, ((Thumbnails) other).thumbnails);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = thumbnails != null ? thumbnails.hashCode() : 1);
  }

  public static final class Builder extends Message.Builder<Thumbnails> {

    public List<ThumbnailItem> thumbnails;

    public Builder() {
    }

    public Builder(Thumbnails message) {
      super(message);
      if (message == null) return;
      this.thumbnails = copyOf(message.thumbnails);
    }

    public Builder thumbnails(List<ThumbnailItem> thumbnails) {
      this.thumbnails = checkForNulls(thumbnails);
      return this;
    }

    @Override
    public Thumbnails build() {
      return new Thumbnails(this);
    }
  }
}
