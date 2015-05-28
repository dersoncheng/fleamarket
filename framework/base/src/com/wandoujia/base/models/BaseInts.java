// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./base.proto
package com.wandoujia.base.models;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;
import java.util.Collections;
import java.util.List;

import static com.squareup.wire.Message.Datatype.INT32;
import static com.squareup.wire.Message.Label.REPEATED;

public final class BaseInts extends Message {

  public static final List<Integer> DEFAULT_VAL = Collections.emptyList();

  @ProtoField(tag = 1, type = INT32, label = REPEATED)
  public final List<Integer> val;

  private BaseInts(Builder builder) {
    super(builder);
    this.val = immutableCopyOf(builder.val);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof BaseInts)) return false;
    return equals(val, ((BaseInts) other).val);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = val != null ? val.hashCode() : 1);
  }

  public static final class Builder extends Message.Builder<BaseInts> {

    public List<Integer> val;

    public Builder() {
    }

    public Builder(BaseInts message) {
      super(message);
      if (message == null) return;
      this.val = copyOf(message.val);
    }

    public Builder val(List<Integer> val) {
      this.val = val;
      return this;
    }

    @Override
    public BaseInts build() {
      return new BaseInts(this);
    }
  }
}
