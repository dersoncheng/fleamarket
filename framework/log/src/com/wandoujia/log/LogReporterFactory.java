package com.wandoujia.log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.wandoujia.base.config.GlobalConfig;

/**
 * @author xubin@wandoujia.com
 */
public class LogReporterFactory {
  // For now we only need one LogReporter with one profile name in the whole application,
  // if we need multiple profiles, then we should change this into a map whose key is the profile
  // name.
  private static WeakReference<LogReporter> logReporterRef;
  private static Map<String, WeakReference<LogReporter>> logReporterMap;

  public static synchronized LogReporter newLogReporter(Context context,
      LogConfiguration configuration) {
    // This is to avoid the LogReporter holding an activity and prevent it from being cleaned out.
    context = context.getApplicationContext();
    if (logReporterMap != null
        && logReporterMap.get(configuration.getProfileName()) != null
        && logReporterMap.get(configuration.getProfileName()).get() != null) {
      if (GlobalConfig.isDebug()) {
        throw new RuntimeException("The LogReporter is already initialized");
      } else {
        return logReporterMap.get(configuration.getProfileName()).get();
      }
    }

    String profileName = configuration.getProfileName();
    LogStorage logStorage = new FileLogStorage(context, profileName);
    LogSender logSender = new LogSender(context, logStorage, configuration);
    LogReporter logReporter = new LogReporter(context, logStorage, configuration, logSender);
    logReporterRef = new WeakReference<LogReporter>(logReporter);
    if (logReporterMap == null) {
      logReporterMap = new HashMap<String, WeakReference<LogReporter>>();
    }
    logReporterMap.put(profileName, logReporterRef);
    return logReporter;
  }

  public static synchronized LogReporter getLogReporter() {
    if (logReporterRef == null) {
      return null;
    }
    return logReporterRef.get(); // TODO (yangkai) maybe is null?
  }

  /**
   * Get logReport.
   * 
   * @param profileName
   * @return LogReporter.
   */
  public static synchronized LogReporter getLogReporter(String profileName) {
    if (logReporterMap == null
        || logReporterMap.get(profileName) == null
        || logReporterMap.get(profileName).get() == null) {
      return null;
    }

    return logReporterMap.get(profileName).get();
  }
}
