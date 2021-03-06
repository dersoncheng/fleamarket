// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./log_transition.proto
package com.wandoujia.logv3.model.packages;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.STRING;

/**
 * 对于服务端的服务线的描述。如IAS服务，startpage服务等等。
 */
public final class ServicePackage extends Message {

  public static final String DEFAULT_NAME = "";

  @ProtoField(tag = 1, type = STRING)
  public final String name;

  private ServicePackage(Builder builder) {
    super(builder);
    this.name = builder.name;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof ServicePackage)) return false;
    return equals(name, ((ServicePackage) other).name);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    return result != 0 ? result : (hashCode = name != null ? name.hashCode() : 0);
  }

  public static final class Builder extends Message.Builder<ServicePackage> {

    public String name;

    public Builder() {
    }

    public Builder(ServicePackage message) {
      super(message);
      if (message == null) return;
      this.name = message.name;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    @Override
    public ServicePackage build() {
      return new ServicePackage(this);
    }
  }
}
