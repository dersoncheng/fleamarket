package com.wandoujia.logv3;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.wandoujia.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.wandoujia.base.config.GlobalConfig;
import com.wandoujia.base.utils.SharePrefSubmitor;
import com.wandoujia.logv3.model.packages.CommonPackage;
import com.wandoujia.logv3.model.packages.DevicePackage;
import com.wandoujia.logv3.model.packages.LaunchSourcePackage;
import com.wandoujia.logv3.model.packages.LogReportEvent;

import java.util.concurrent.Executor;

/**
 * This class is used to report Muce log, this will append common parameters into log event and
 * then write it to specific log storage, it does not send log to server.
 * 
 * @author xubin@wandoujia.com
 * @author yangkai@wandoujia.com
 */
public class LogReporter {
  private static final String TAG = LogReporter.class.getSimpleName() + "v3";
  private static final long LOG_REPORTER_THREAD_CACHE_TIME = 10 * 60 * 1000L;
  private static final String LOG_REPORTER_THREAD = "log-reporterv3-thread";

  private final Context context;
  private final Executor reporterExecutor;
  private final DatabaseLogStorage logStorage;
  private final LogConfiguration logConfiguration;
  private final LogSender logSender;

  private CommonPackage stableCommonPackage;
  private DevicePackage devicePackage;

  /**
   * This can be called in main thread.
   * 
   * @param logStorage the log storage to save the log events.
   */
  LogReporter(Context context, DatabaseLogStorage logStorage, LogConfiguration configuration,
      LogSender logSender) {
    this.context = context;
    this.reporterExecutor = new CachedThreadPoolExecutorWithCapacity(1,
        LOG_REPORTER_THREAD_CACHE_TIME,
        LOG_REPORTER_THREAD);
    this.logStorage = logStorage;
    this.logConfiguration = configuration;
    this.logSender = logSender;
    asyncInit(context, configuration);
  }

  private void asyncInit(final Context context, final LogConfiguration configuration) {
    reporterExecutor.execute(new Runnable() {
      @Override
      public void run() {
        stableCommonPackage = configuration.buildCommonPackageStableParams(context);
      }
    });
  }

  /**
   * update LaunchSourcePackage in reporterExecutor so that stableCommonPackage must be inited.
   * @param launchSourcePackage
   */
  public void updateLaunchSource(final LaunchSourcePackage launchSourcePackage) {
    reporterExecutor.execute(new Runnable() {
      @Override
      public void run() {
        stableCommonPackage =
            logConfiguration.updateLaunchSource(launchSourcePackage, stableCommonPackage);
      }
    });
  }

  /**
   * get DevicePackage which is needed by P4's launch event only now, so if any event need this
   * package, add by itself
   * 
   * @return
   */
  public DevicePackage getDevicePackage() {
    if (devicePackage == null) {
      devicePackage = logConfiguration.buildDevicePackage(context);
    }
    return devicePackage;
  }

  public void onEvent(LogReportEvent.Builder eventBuilder) {
    reporterExecutor.execute(new WriteToStorageTask(eventBuilder));
  }

  /**
   * This function is always used for log crash detail.
   * If use asynchronous way, the process may be crashed and the detail exception is lost.
   * 
   * @param eventBuilder event builder
   */
  public void onEventSync(LogReportEvent.Builder eventBuilder) {
    new WriteToStorageTask(eventBuilder.real_time(true)).run();
  }

  private class WriteToStorageTask implements Runnable {
    private static final String PREF_KEY_LOG_SEND_ID = "last_send_id";

    private final LogReportEvent.Builder reportEventBuilder;

    private WriteToStorageTask(LogReportEvent.Builder logEvent) {
      this.reportEventBuilder = logEvent;
    }

    @Override
    public void run() {
      reportEventBuilder.common_package(logConfiguration.buildCommonParamsVolatileParams(context,
          stableCommonPackage));

      // use monotonic increase long as random id
      final long lastSendSuccessTime = context.getSharedPreferences(LogSender.PREF_NAME,
          Context.MODE_PRIVATE).getLong(PREF_KEY_LOG_SEND_ID, 0);
      reportEventBuilder.local_increment_id(lastSendSuccessTime);
      reportEventBuilder.proto_version(LogReportEvent.DEFAULT_PROTO_VERSION);
      final SharedPreferences.Editor editor = context.getSharedPreferences(LogSender.PREF_NAME,
          Context.MODE_PRIVATE).edit();
      editor.putLong(PREF_KEY_LOG_SEND_ID, lastSendSuccessTime + 1);
      SharePrefSubmitor.submit(editor);

      final LogReportEvent event = reportEventBuilder.build();
      if (GlobalConfig.isDebug()) {
        Log.i(TAG, event.toString());
      }
      logStorage.addEvent(event);
      logSender.triggerSend(reportEventBuilder.real_time);
    }
  }

}
