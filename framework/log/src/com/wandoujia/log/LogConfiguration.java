package com.wandoujia.log;

import android.content.Context;

import java.util.Map;

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
   * Set the common parameters which all log events will have here.
   * These parameters should be stable and never change.
   * 
   * @return a map contains the stable common parameters.
   */
  Map<String, String> buildStableCommonParams(Context context);

  /**
   * Set the common parameters which all log events will have here.
   * These parameters can be changeable and will rebuild when the log event happens.
   * 
   * @return a map contains the volatile common parameter.
   */
  Map<String, String> buildVolatileCommonParams(Context context);

  /**
   * This is for old implementation in which the header parameters will be set as header when a
   * bunch of log event is sent to muce server.
   * These parameters should also be stable.
   * 
   * @return a map contains the header parameters.
   */
  Map<String, String> buildHeaderParams(Context context);

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
