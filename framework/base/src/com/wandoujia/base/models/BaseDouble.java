// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./base.proto
package com.wandoujia.base.models;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.DOUBLE;
import static com.squareup.wire.Message.Label.REQUIRED;

public final class BaseDouble extends Message {

  public static final Double DEFAULT_VAL = 0D;

  @ProtoField(tag = 1, type = DOUBLE, label = REQUIRED)
  public final Double val;

  private BaseDouble(Builder builder) {
    super(builder);
    this.val = builder.val;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof BaseDouble)) return false;
    return equals(val, ((BaseDouble) other).val);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = val != null ? val.hashCode() : 0);
  }

  public static final class Builder extends Message.Builder<BaseDouble> {

    public Double val;

    public Builder() {
    }

    public Builder(BaseDouble message) {
      super(message);
      if (message == null) return;
      this.val = message.val;
    }

    public Builder val(Double val) {
      this.val = val;
      return this;
    }

    @Override
    public BaseDouble build() {
      checkRequiredFields();
      return new BaseDouble(this);
    }
  }
}