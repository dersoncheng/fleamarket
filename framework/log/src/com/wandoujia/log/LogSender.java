package com.wandoujia.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.wandoujia.base.concurrent.CachedThreadPoolExecutorWithCapacity;
import com.wandoujia.base.config.GlobalConfig;
import com.wandoujia.base.utils.CipherUtil;
import com.wandoujia.base.utils.IOUtils;
import com.wandoujia.base.utils.NetworkUtil;
import com.wandoujia.base.utils.SharePrefSubmitor;
import com.wandoujia.base.utils.SystemUtil;
import com.wandoujia.rpc.http.client.PhoenixHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.zip.GZIPOutputStream;

/**
 * @author xubin@wandoujia.com
 */
public class LogSender {
  public enum TimePolicy {
    NONE, ON_LAUNCH, REAL_TIME, SCHEDULE
  }

  // private static final String MUCE_URL_RAW =
  // "http://l.wandoujia.com/p/%1$s?vc=%2$s&vn=%3$s&key_version=2";
  private static final String MUCE_URL_RAW = LogConfig.MUCE_URL_RAW;
  private static final String MUCE_URL_RAW_DEBUG = "http://10.0.29.57:8095/muce/data/sink?" +
      "profile=%1$s&vc=%2$s&vn=%3$s&gzip=true&encrypt=true&key_version=%4$s&log_version=%5$s";

  private static final int BUFFER_SIZE = 1024;
  private static final long LOG_SENDER_THREAD_CACHE_TIME = 10 * 60 * 1000L;
  private static final String LOG_SENDER_THREAD_NAME = "log-sender-thread";

  private static final String PREF_NAME = "log_module";
  private static final String PREF_KEY_LAST_SEND_SUCCESS_TIME = "log_sender_last_success_time";

  private final Context context;
  private final Executor senderExecutor;
  private final LogStorage logStorage;
  private final LogConfiguration logConfiguration;
  private HttpClient httpClient;

  private final SenderPolicyModel wifiSenderPolicy;
  private final SenderPolicyModel mobileSenderPolicy;

  private boolean isJustLaunch = true;

  private String headerLine;
  private String muceUrl;

  LogSender(Context context, LogStorage logStorage, LogConfiguration configuration) {
    this.senderExecutor = new CachedThreadPoolExecutorWithCapacity(1, LOG_SENDER_THREAD_CACHE_TIME,
        LOG_SENDER_THREAD_NAME);
    this.logStorage = logStorage;
    this.logConfiguration = configuration;

    this.wifiSenderPolicy = logConfiguration.getWifiSendPolicy();
    this.mobileSenderPolicy = logConfiguration.getMobileSendPolicy();
    this.context = context;

    asyncInit();
  }

  private void asyncInit() {
    senderExecutor.execute(new Runnable() {
      @Override
      public void run() {
        muceUrl = buildMuceUrl(logConfiguration.getProfileName(), logConfiguration.getKeyVersion(),
            logConfiguration.getLogVersion());
        headerLine = buildHeaderLine();

      }
    });
  }

  public void triggerSend(boolean isForceRealTime) {
    if (isForceRealTime || checkPolicy()) {
      senderExecutor.execute(new SendLogTask());
    }
  }

  private boolean checkPolicy() {
    if (GlobalConfig.isDebug()) {
      return true;
    }
    int networkType = NetworkUtil.getNetworkType();
    SenderPolicyModel policyModel;
    switch (networkType) {
      case NetworkUtil.NETWORK_TYPE_MOBILE:
        policyModel = mobileSenderPolicy;
        break;
      case NetworkUtil.NETWORK_TYPE_WIFI:
        policyModel = wifiSenderPolicy;
        break;
      case NetworkUtil.NETWORK_TYPE_NONE:
        return false;
      default:
        return false;
    }
    switch (policyModel.timePolicy) {
      case NONE:
        return false;
      case REAL_TIME:
        return true;
      case ON_LAUNCH:
        if (isJustLaunch) {
          isJustLaunch = false;
          return true;
        } else {
          return false;
        }
      case SCHEDULE:
        long duration = policyModel.duration;
        long lastSendSuccessTime = context.getSharedPreferences(PREF_NAME,
            Context.MODE_PRIVATE).getLong(PREF_KEY_LAST_SEND_SUCCESS_TIME, 0);
        return (lastSendSuccessTime + duration <= System.currentTimeMillis());
      default:
        return false;
    }
  }

  private String buildMuceUrl(String profileName, String keyVersion, String logVersion) {
    String vc = String.valueOf(SystemUtil.getVersionCode(context));
    String vn = SystemUtil.getVersionName(context);
    if (GlobalConfig.isDebug()) {
      return String.format(MUCE_URL_RAW_DEBUG, profileName, vc, vn, keyVersion, logVersion);
    } else {
      return String.format(MUCE_URL_RAW, profileName, vc, vn, keyVersion, logVersion);
    }
  }


  private String buildHeaderLine() {
    Map<String, String> headerParams = logConfiguration.buildHeaderParams(context);
    List<String> headerParamList = new ArrayList<String>();
    for (Map.Entry<String, String> e : headerParams.entrySet()) {
      headerParamList.add(e.getKey() + "=" + e.getValue());
    }
    return TextUtils.join(",", headerParamList) + "\n";
  }

  private class SendLogTask implements Runnable {

    @Override
    public void run() {
      GZIPOutputStream gzipOutputStream = null;
      ByteArrayOutputStream outputStream = null;
      long outputId = -1;
      boolean success = false;
      HttpEntity httpEntity = null;
      try {
        outputStream = new ByteArrayOutputStream();
        gzipOutputStream = new GZIPOutputStream(outputStream, BUFFER_SIZE);
        gzipOutputStream.write(headerLine.getBytes());
        outputId = logStorage.output(gzipOutputStream);
        gzipOutputStream.finish();
        byte[] encryptData = CipherUtil.encrypt(outputStream.toByteArray(),
            CipherUtil.getAESKey(context));

        HttpPost post = new HttpPost(muceUrl);
        ByteArrayEntity entity = new ByteArrayEntity(encryptData);
        post.setEntity(entity);
        if (httpClient == null) {
          httpClient = new PhoenixHttpClient();
        }
        HttpResponse response = httpClient.execute(post);
        httpEntity = response.getEntity();
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
          success = true;
          SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME,
              Context.MODE_PRIVATE).edit();
          editor.putLong(PREF_KEY_LAST_SEND_SUCCESS_TIME, System.currentTimeMillis());
          SharePrefSubmitor.submit(editor);
        }
      } catch (OutOfMemoryError e) {
        e.printStackTrace();
      } catch (GeneralSecurityException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (Exception e) {
        // Sometimes when execute this request, server may cause a redirect will null url,
        // so it will throw NullPointerException. Catch Exception here to avoid crash and try
        // to resend later.
        e.printStackTrace();
      } finally {
        IOUtils.close(gzipOutputStream);
        IOUtils.close(outputStream);
        if (outputId > 0) {
          if (success) {
            logStorage.deleteOutput(outputId);
          } else {
            logStorage.restoreOutput(outputId);
          }
        }
        if (httpEntity != null) {
          try {
            httpEntity.consumeContent();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  public static class SenderPolicyModel implements Serializable {
    private TimePolicy timePolicy;
    private long duration;

    public SenderPolicyModel() {
    }

    /**
     * @param timePolicy the send policy
     * @param duration   the duration for send policy, which will only work when the timePolicy is
     *                   TimePolicy.SCHEDULE
     */
    public SenderPolicyModel(TimePolicy timePolicy, long duration) {
      this.timePolicy = timePolicy;
      this.duration = duration;
    }
  }

}
