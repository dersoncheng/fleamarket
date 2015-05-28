// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./base.proto
package com.wandoujia.base.models;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.STRING;
import static com.squareup.wire.Message.Label.REQUIRED;

public final class Pair extends Message {

  public static final String DEFAULT_KEY = "";
  public static final String DEFAULT_VALUE = "";

  @ProtoField(tag = 1, type = STRING, label = REQUIRED)
  public final String key;

  @ProtoField(tag = 2, type = STRING, label = REQUIRED)
  public final String value;

  private Pair(Builder builder) {
    super(builder);
    this.key = builder.key;
    this.value = builder.value;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Pair)) return false;
    Pair o = (Pair) other;
    return equals(key, o.key)
        && equals(value, o.value);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == 0) {
      result = key != null ? key.hashCode() : 0;
      result = result * 37 + (value != null ? value.hashCode() : 0);
      hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<Pair> {

    public String key;
    public String value;

    public Builder() {
    }

    public Builder(Pair message) {
      super(message);
      if (message == null) return;
      this.key = message.key;
      this.value = message.value;
    }

    public Builder key(String key) {
      this.key = key;
      return this;
    }

    public Builder value(String value) {
      this.value = value;
      return this;
    }

    @Override
    public Pair build() {
      checkRequiredFields();
      return new Pair(this);
    }
  }
}
