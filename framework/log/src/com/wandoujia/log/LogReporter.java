package com.wandoujia.log;

import android.content.Context;
import android.util.Log;

import com.wandoujia.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.wandoujia.base.config.GlobalConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * This class is used to report Muce log, this will append common parameters into log event and
 * then write it to specific log storage, it does not send log to server.
 * 
 * @author xubin@wandoujia.com
 */
@Deprecated
public class LogReporter {
  private static final String TAG = LogReporter.class.getSimpleName();
  private static final long LOG_REPORTER_THREAD_CACHE_TIME = 10 * 60 * 1000L;
  private static final String LOG_REPORTER_THREAD = "log-reporter-thread";

  private final Context context;
  private final Executor reporterExecutor;
  private final LogStorage logStorage;
  private final LogConfiguration logConfiguration;
  private final LogSender logSender;

  private final Map<String, String> commonParams = new HashMap<String, String>();

  /**
   * This can be called in main thread.
   * 
   * @param logStorage the log storage to save the log events.
   */
  LogReporter(Context context, LogStorage logStorage, LogConfiguration configuration,
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
        commonParams.putAll(configuration.buildStableCommonParams(context));
      }
    });
  }

  /**
   * Report and specific log event, can be called in any thread.
   * 
   * @param event the event name.
   */
  public void onEvent(String event) {
    onEvent(event, null);
  }

  /**
   * Report and specific log event, can be called in any thread.
   * 
   * @param event the event name.
   * @param params the parameters for this event, not include the common parameters like udid.
   */
  public void onEvent(String event, Map<String, String> params) {
    onEvent(event, params, LogEventModel.Priority.NORMAL);
  }

  /**
   * Report and specific log event on real time, can be called in any thread. Real time means
   * this will cause the {@link LogSender} to send ALL the events saved in the log storage to
   * server instantly, <b>SO BE REALLY CAREFUL ABOUT CALLING THIS!<b/>
   * 
   * @param event the event name.
   */
  public void onEventRealTime(String event) {
    onEvent(event, null, LogEventModel.Priority.REAL_TIME);
  }

  /**
   * Report and specific log event on real time, can be called in any thread. Real time means
   * this will cause the {@link LogSender} to send ALL the events saved in the log storage to
   * server instantly, <b>SO BE REALLY CAREFUL ABOUT CALLING THIS!<b/>
   * 
   * @param event the event name.
   * @param params the parameters for this event, not include the common parameters like udid.
   */
  public void onEventRealTime(String event, Map<String, String> params) {
    onEvent(event, params, LogEventModel.Priority.REAL_TIME);
  }

  /**
   * Report and specific log event, can be called in any thread.
   */
  public void onEvent(String namespace, String category, String action) {
    onEvent(namespace, category, action, null);
  }

  /**
   * Report and specific log event, can be called in any thread.
   * 
   * @param params the parameters for this event, not include the common parameters like udid.
   */
  public void onEvent(String namespace, String category, String action, Map<String,
      String> params) {
    onEvent(buildEventName(namespace, category, action), params, LogEventModel.Priority.NORMAL);
  }

  /**
   * Report and specific log event on real time, can be called in any thread. Real time means
   * this will cause the {@link LogSender} to send ALL the events saved in the log storage to
   * server instantly, <b>SO BE REALLY CAREFUL ABOUT CALLING THIS!<b/>
   * 
   */
  public void onEventRealTime(String namespace, String category, String action) {
    onEvent(buildEventName(namespace, category, action), null, LogEventModel.Priority.REAL_TIME);
  }

  /**
   * Report and specific log event on real time, can be called in any thread. Real time means
   * this will cause the {@link LogSender} to send ALL the events saved in the log storage to
   * server instantly, <b>SO BE REALLY CAREFUL ABOUT CALLING THIS!<b/>
   * 
   * @param params the parameters for this event, not include the common parameters like udid.
   */
  public void onEventRealTime(String namespace, String category, String action,
      Map<String, String> params) {
    onEvent(buildEventName(namespace, category, action), params, LogEventModel.Priority.REAL_TIME);
  }

  private void onEvent(String event, Map<String, String> params,
      LogEventModel.Priority priority) {
    if (params == null) {
      params = new HashMap<String, String>();
    }
    // ensure compatibility
    LogEventModel logEventModel = new LogEventModel(event, new HashMap<String, String>(params),
        System.currentTimeMillis(), priority);
    reporterExecutor.execute(new WriteToStorageTask(logEventModel));
    if (GlobalConfig.isDebug()) {
      StringBuilder builder = new StringBuilder();
      builder.append("[event:").append(event).append("]");
      for (Map.Entry<String, String> e : params.entrySet()) {
        builder.append("[").append(e.getKey()).append(":").append(e.getValue()).append("]");
      }
      Log.i(TAG, builder.toString());
    }
  }

  /**
   * This function is always used for log crash detail.
   * If use asynchronous way, the process may be crashed and the detail exception is lost.
   * 
   * @param event event name
   * @param params the parameters for this event, not include the common parameters like udid.
   * @param priority real time or normal, {@link LogEventModel.Priority}
   */
  public void onEventSync(String event, Map<String, String> params,
      LogEventModel.Priority priority) {
    if (params == null) {
      params = new HashMap<String, String>();
    }
    LogEventModel logEventModel = new LogEventModel(event, new HashMap<String, String>(params),
        System.currentTimeMillis(), priority);
    new WriteToStorageTask(logEventModel).run();
  }

  private String buildEventName(String namespace, String category, String action) {
    return namespace + "." + category + "." + action;
  }

  private void addCommonParams(Map<String, String> params) {
    // refresh volatile values in common params
    commonParams.putAll(logConfiguration.buildVolatileCommonParams(context));
    params.putAll(commonParams);
  }

  private class WriteToStorageTask implements Runnable {
    private final LogEventModel logEventModel;

    private WriteToStorageTask(LogEventModel logEvent) {
      this.logEventModel = logEvent;
    }

    @Override
    public void run() {
      addCommonParams(logEventModel.params);
      logStorage.addEvent(logEventModel);
      boolean isForceRealTime =
          logEventModel.priority == LogEventModel.Priority.REAL_TIME;
      logSender.triggerSend(isForceRealTime);
    }
  }

}
