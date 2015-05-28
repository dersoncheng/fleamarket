package com.wandoujia.log.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import com.squareup.wire.Message;

/**
 * Created by yanguang on 13-12-19.
 */
public class MessageAdapter {

  public static HashMap<String, String> buildLogInfo(Message message) {
    HashMap<String, String> logInfos = new HashMap<String, String>();
    if (message == null) {
      return logInfos;
    }
    for (Field messageField : message.getClass().getDeclaredFields()) {
      if (messageField == null) {
        continue;
      }
      if (Modifier.isStatic(messageField.getModifiers())) {
        continue;
      }
      Object valueObject = null;
      try {
        valueObject = messageField.get(message);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (valueObject == null) {
        continue;
      }
      if (valueObject instanceof Message) {
        Message subMessage = (Message) valueObject;
        HashMap<String, String> subLogInfo = buildLogInfo(subMessage);
        if (subLogInfo != null) {
          logInfos.putAll(subLogInfo);
        }
      } else {
        String key = messageField.getName();
        String value = valueObject.toString();
        // If hashMap contains the key already, throw runtime exception.
        if (logInfos.containsKey(key)) {
          throw new RuntimeException("LogInfo contains the key already, key is " + key);
        }
        logInfos.put(key, value);
      }
    }
    return logInfos;
  }

}
