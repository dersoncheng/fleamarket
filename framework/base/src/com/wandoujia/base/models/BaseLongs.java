// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./base.proto
package com.wandoujia.base.models;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;
import java.util.Collections;
import java.util.List;

import static com.squareup.wire.Message.Datatype.INT64;
import static com.squareup.wire.Message.Label.REPEATED;

public final class BaseLongs extends Message {

  public static final List<Long> DEFAULT_VAL = Collections.emptyList();

  @ProtoField(tag = 1, type = INT64, label = REPEATED)
  public final List<Long> val;

  private BaseLongs(Builder builder) {
    super(builder);
    this.val = immutableCopyOf(builder.val);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof BaseLongs)) return false;
    return equals(val, ((BaseLongs) other).val);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = val != null ? val.hashCode() : 1);
  }

  public static final class Builder extends Message.Builder<BaseLongs> {

    public List<Long> val;

    public Builder() {
    }

    public Builder(BaseLongs message) {
      super(message);
      if (message == null) return;
      this.val = copyOf(message.val);
    }

    public Builder val(List<Long> val) {
      this.val = val;
      return this;
    }

    @Override
    public BaseLongs build() {
      return new BaseLongs(this);
    }
  }
}
