// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./base.proto
package com.wandoujia.base.models;

import com.squareup.wire.ByteString;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.BYTES;

public final class BaseBytes extends Message {

  public static final ByteString DEFAULT_VAL = ByteString.EMPTY;

  @ProtoField(tag = 1, type = BYTES)
  public final ByteString val;

  private BaseBytes(Builder builder) {
    super(builder);
    this.val = builder.val;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof BaseBytes)) return false;
    return equals(val, ((BaseBytes) other).val);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = val != null ? val.hashCode() : 0);
  }

  public static final class Builder extends Message.Builder<BaseBytes> {

    public ByteString val;

    public Builder() {
    }

    public Builder(BaseBytes message) {
      super(message);
      if (message == null) return;
      this.val = message.val;
    }

    public Builder val(ByteString val) {
      this.val = val;
      return this;
    }

    @Override
    public BaseBytes build() {
      return new BaseBytes(this);
    }
  }
}
