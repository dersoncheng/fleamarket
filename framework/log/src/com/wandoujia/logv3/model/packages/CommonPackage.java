// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./log_common_package.proto
package com.wandoujia.logv3.model.packages;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Label.REQUIRED;

/**
 * 每次上报时候都需要带上的信息
 */
public final class CommonPackage extends Message {

  @ProtoField(tag = 1, label = REQUIRED)
  public final IdPackage id_package;

  @ProtoField(tag = 2, label = REQUIRED)
  public final ClientPackage client_package;

  @ProtoField(tag = 3, label = REQUIRED)
  public final TimePackage time_package;

  @ProtoField(tag = 4)
  public final NetworkPackage network_package;

  @ProtoField(tag = 5)
  public final AccountPackage account_package;

  @ProtoField(tag = 6)
  public final LaunchSourcePackage launch_source_package;

  private CommonPackage(Builder builder) {
    super(builder);
    this.id_package = builder.id_package;
    this.client_package = builder.client_package;
    this.time_package = builder.time_package;
    this.network_package = builder.network_package;
    this.account_package = builder.account_package;
    this.launch_source_package = builder.launch_source_package;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof CommonPackage)) return false;
    CommonPackage o = (CommonPackage) other;
    return equals(id_package, o.id_package)
        && equals(client_package, o.client_package)
        && equals(time_package, o.time_package)
        && equals(network_package, o.network_package)
        && equals(account_package, o.account_package)
        && equals(launch_source_package, o.launch_source_package);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == 0) {
      result = id_package != null ? id_package.hashCode() : 0;
      result = result * 37 + (client_package != null ? client_package.hashCode() : 0);
      result = result * 37 + (time_package != null ? time_package.hashCode() : 0);
      result = result * 37 + (network_package != null ? network_package.hashCode() : 0);
      result = result * 37 + (account_package != null ? account_package.hashCode() : 0);
      result = result * 37 + (launch_source_package != null ? launch_source_package.hashCode() : 0);
      hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<CommonPackage> {

    public IdPackage id_package;
    public ClientPackage client_package;
    public TimePackage time_package;
    public NetworkPackage network_package;
    public AccountPackage account_package;
    public LaunchSourcePackage launch_source_package;

    public Builder() {
    }

    public Builder(CommonPackage message) {
      super(message);
      if (message == null) return;
      this.id_package = message.id_package;
      this.client_package = message.client_package;
      this.time_package = message.time_package;
      this.network_package = message.network_package;
      this.account_package = message.account_package;
      this.launch_source_package = message.launch_source_package;
    }

    public Builder id_package(IdPackage id_package) {
      this.id_package = id_package;
      return this;
    }

    public Builder client_package(ClientPackage client_package) {
      this.client_package = client_package;
      return this;
    }

    public Builder time_package(TimePackage time_package) {
      this.time_package = time_package;
      return this;
    }

    public Builder network_package(NetworkPackage network_package) {
      this.network_package = network_package;
      return this;
    }

    public Builder account_package(AccountPackage account_package) {
      this.account_package = account_package;
      return this;
    }

    public Builder launch_source_package(LaunchSourcePackage launch_source_package) {
      this.launch_source_package = launch_source_package;
      return this;
    }

    @Override
    public CommonPackage build() {
      checkRequiredFields();
      return new CommonPackage(this);
    }
  }
}