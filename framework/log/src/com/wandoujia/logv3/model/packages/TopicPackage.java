// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./log_transition.proto
package com.wandoujia.logv3.model.packages;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.STRING;

public final class TopicPackage extends Message {

  public static final String DEFAULT_ID = "";

  @ProtoField(tag = 1, type = STRING)
  public final String id;

  private TopicPackage(Builder builder) {
    super(builder);
    this.id = builder.id;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof TopicPackage)) return false;
    return equals(id, ((TopicPackage) other).id);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = id != null ? id.hashCode() : 0);
  }

  public static final class Builder extends Message.Builder<TopicPackage> {

    public String id;

    public Builder() {
    }

    public Builder(TopicPackage message) {
      super(message);
      if (message == null) return;
      this.id = message.id;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    @Override
    public TopicPackage build() {
      return new TopicPackage(this);
    }
  }
}
