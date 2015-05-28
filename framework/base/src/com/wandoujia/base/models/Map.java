// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./base.proto
package com.wandoujia.base.models;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;
import java.util.Collections;
import java.util.List;

import static com.squareup.wire.Message.Label.REPEATED;

public final class Map extends Message {

  public static final List<Pair> DEFAULT_VAL = Collections.emptyList();

  @ProtoField(tag = 1, label = REPEATED)
  public final List<Pair> val;

  private Map(Builder builder) {
    super(builder);
    this.val = immutableCopyOf(builder.val);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Map)) return false;
    return equals(val, ((Map) other).val);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = val != null ? val.hashCode() : 1);
  }

  public static final class Builder extends Message.Builder<Map> {

    public List<Pair> val;

    public Builder() {
    }

    public Builder(Map message) {
      super(message);
      if (message == null) return;
      this.val = copyOf(message.val);
    }

    public Builder val(List<Pair> val) {
      this.val = val;
      return this;
    }

    @Override
    public Map build() {
      return new Map(this);
    }
  }
}
