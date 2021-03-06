// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./log_transition.proto
package com.wandoujia.logv3.model.packages;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.STRING;

public final class GroupPackage extends Message {

  public static final String DEFAULT_ID = "";

  @ProtoField(tag = 1, type = STRING)
  public final String id;

  private GroupPackage(Builder builder) {
    super(builder);
    this.id = builder.id;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof GroupPackage)) return false;
    return equals(id, ((GroupPackage) other).id);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = id != null ? id.hashCode() : 0);
  }

  public static final class Builder extends Message.Builder<GroupPackage> {

    public String id;

    public Builder() {
    }

    public Builder(GroupPackage message) {
      super(message);
      if (message == null) return;
      this.id = message.id;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    @Override
    public GroupPackage build() {
      return new GroupPackage(this);
    }
  }
}
