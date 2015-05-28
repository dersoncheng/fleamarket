package com.wandoujia.logv3;

import android.content.Context;

import com.wandoujia.logv3.model.packages.CommonPackage;
import com.wandoujia.logv3.model.packages.DevicePackage;
import com.wandoujia.logv3.model.packages.LaunchSourcePackage;

/**
 * Application can implement this and pass it as parameter to build
 * {@link com.wandoujia.log.LogSender} and {@link LogReporter}
 * 
 * @author xubin@wandoujia.com
 */
public interface LogConfiguration {
  /**
   * @return the muce log version
   */
  String getLogVersion();

  /**
   * @return the muce log key version
   */
  String getKeyVersion();

  /**
   * @return the Muce profile name for this log reporter.
   */
  String getProfileName();

  /**
   * build device package
   * 
   * @param context
   */
  DevicePackage buildDevicePackage(Context context);

  /**
   * Set the common parameters which all log events will have here.
   * These parameters should be stable and never change.
   * 
   * @return a map contains the stable common parameters.
   */
  CommonPackage buildCommonPackageStableParams(Context context);

  CommonPackage updateLaunchSource(LaunchSourcePackage launchSourcePackage,
      CommonPackage stableCommonPackage);

  /**
   * Set the common parameters which all log events will have here.
   * These parameters can be changeable and will rebuild when the log event happens.
   * 
   * @param context
   * @param stableCommonPackage package return by
   *          {@link #buildCommonPackageStableParams(android.content.Context)}
   * 
   * @return CommonPackage that combine stable Common package and current states
   */
  CommonPackage buildCommonParamsVolatileParams(Context context, CommonPackage stableCommonPackage);

  /**
   * Set the send policy in wifi.
   * 
   * @return the send policy in wifi
   */
  LogSender.SenderPolicyModel getWifiSendPolicy();

  /**
   * Set the send policy in mobile network.
   * 
   * @return the send policy in mobile network.
   */
  LogSender.SenderPolicyModel getMobileSendPolicy();
}
