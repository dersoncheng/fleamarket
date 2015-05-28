// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./log_transition.proto
package com.wandoujia.logv3.model.packages;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.STRING;

public final class SubscribeItemPackage extends Message {

  public static final String DEFAULT_ITEM_TYPE = "";
  public static final String DEFAULT_ITEM_ID = "";
  public static final String DEFAULT_ITEM_CREATETIME = "";
  public static final String DEFAULT_ITEM_UPDATETIME = "";
  public static final String DEFAULT_ITEM_FEEDNAME = "";

  @ProtoField(tag = 1, type = STRING)
  public final String item_type;

  @ProtoField(tag = 2, type = STRING)
  public final String item_id;

  @ProtoField(tag = 3, type = STRING)
  public final String item_createTime;

  @ProtoField(tag = 4, type = STRING)
  public final String item_updateTime;

  @ProtoField(tag = 5, type = STRING)
  public final String item_feedName;

  private SubscribeItemPackage(Builder builder) {
    super(builder);
    this.item_type = builder.item_type;
    this.item_id = builder.item_id;
    this.item_createTime = builder.item_createTime;
    this.item_updateTime = builder.item_updateTime;
    this.item_feedName = builder.item_feedName;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof SubscribeItemPackage)) return false;
    SubscribeItemPackage o = (SubscribeItemPackage) other;
    return equals(item_type, o.item_type)
        && equals(item_id, o.item_id)
        && equals(item_createTime, o.item_createTime)
        && equals(item_updateTime, o.item_updateTime)
        && equals(item_feedName, o.item_feedName);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == 0) {
      result = item_type != null ? item_type.hashCode() : 0;
      result = result * 37 + (item_id != null ? item_id.hashCode() : 0);
      result = result * 37 + (item_createTime != null ? item_createTime.hashCode() : 0);
      result = result * 37 + (item_updateTime != null ? item_updateTime.hashCode() : 0);
      result = result * 37 + (item_feedName != null ? item_feedName.hashCode() : 0);
      hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<SubscribeItemPackage> {

    public String item_type;
    public String item_id;
    public String item_createTime;
    public String item_updateTime;
    public String item_feedName;

    public Builder() {
    }

    public Builder(SubscribeItemPackage message) {
      super(message);
      if (message == null) return;
      this.item_type = message.item_type;
      this.item_id = message.item_id;
      this.item_createTime = message.item_createTime;
      this.item_updateTime = message.item_updateTime;
      this.item_feedName = message.item_feedName;
    }

    public Builder item_type(String item_type) {
      this.item_type = item_type;
      return this;
    }

    public Builder item_id(String item_id) {
      this.item_id = item_id;
      return this;
    }

    public Builder item_createTime(String item_createTime) {
      this.item_createTime = item_createTime;
      return this;
    }

    public Builder item_updateTime(String item_updateTime) {
      this.item_updateTime = item_updateTime;
      return this;
    }

    public Builder item_feedName(String item_feedName) {
      this.item_feedName = item_feedName;
      return this;
    }

    @Override
    public SubscribeItemPackage build() {
      return new SubscribeItemPackage(this);
    }
  }
}
