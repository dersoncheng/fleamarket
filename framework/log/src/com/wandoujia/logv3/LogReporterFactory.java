package com.wandoujia.logv3;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.wandoujia.base.config.GlobalConfig;

/**
 * @author xubin@wandoujia.com
 * @author yangkai@wandoujia.com
 */
public class LogReporterFactory {
  // For now we only need one LogReporter with one profile name in the whole application,
  // if we need multiple profiles, then we should change this into a map whose key is the profile
  // name.
  private static WeakReference<LogReporter> logReporterRef;

  public static synchronized LogReporter newLogReporter(Context context,
      LogConfiguration configuration) {
    // This is to avoid the LogReporter holding an activity and prevent it from being cleaned out.
    context = context.getApplicationContext();
    if (logReporterRef != null && logReporterRef.get() != null) {
      if (GlobalConfig.isDebug()) {
        throw new RuntimeException("The LogReporter is already initialized");
      } else {
        return logReporterRef.get();
      }
    }
    String profileName = configuration.getProfileName();
    DatabaseLogStorage logStorage = new DatabaseLogStorage(context, profileName);
    LogSender logSender = new LogSender(context, logStorage, configuration);
    LogReporter logReporter = new LogReporter(context, logStorage, configuration, logSender);
    logReporterRef = new WeakReference<LogReporter>(logReporter);
    return logReporter;
  }

  public static synchronized LogReporter getLogReporterV3() {
    if (logReporterRef == null) {
      return null;
    }
    return logReporterRef.get(); // TODO (yangkai) maybe is null?
  }
}
