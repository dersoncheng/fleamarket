#include <string.h>
#include <jni.h>
#include "udid.h"

#include "udid.h"

typedef unsigned int uint;

char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

uint getInt(char c) {
  if (c >= '0' && c <= '9') {
      return c - '0';
  } else if (c >= 'a' && c <= 'f') {
      return c - 'a' + 10;
  } else if (c >= 'A' && c <= 'F') {
      return c - 'A' + 10;
  } else {
      return 0;
  }
}

char getChar(uint n) {
  uint in = n % 16;
  return hexDigits[in];
}

JNIEXPORT jstring JNICALL Java_com_wandoujia_udid_UDIDUtil_generateUDIDNative(
    JNIEnv* env, jclass this, jstring uuid)
{
  char* uuidArray = (char*)(*env)->GetStringUTFChars(env, uuid, NULL);
  char udid[41] = {0};
  uint len = strlen(uuidArray), over;
  uint first, second, third, fourth, sum;
  int i;

  if (len > 32) len = 32;
  for (i = 0; i < len; i++) {
      udid[i] = uuidArray[i];
  }
  for (i = len; i < 32; i++) {
      udid[i] = '0';
  }

  over = 0;
  for (i = 7; i >= 0; i--) {
      first = getInt(udid[i]);
      second = getInt(udid[i+8]);
      third = getInt(udid[i+16]);
      fourth = getInt(udid[i+24]);
      sum = first + second + third + fourth + over;
      over = sum / 16;
      udid[i+32] = getChar(sum);
  }
  (*env)->ReleaseStringUTFChars(env, uuid, uuidArray);
  return (*env)->NewStringUTF(env, udid);
}

JNIEXPORT jboolean JNICALL Java_com_wandoujia_udid_UDIDUtil_isUDIDValidNative (
    JNIEnv* env, jclass this, jstring udid)
{
  if (udid == NULL) {
      return JNI_FALSE;
  }
  char* udidArray = (char*)(*env)->GetStringUTFChars(env, udid, NULL);
  uint len = strlen(udidArray), over;
  uint first, second, third, fourth, sum;
  int i;

  if (len != 40) {
      (*env)->ReleaseStringUTFChars(env, udid, udidArray);
      return JNI_FALSE;
  }
  over = 0;
  for (i = 7; i >= 0; i--) {
      first = getInt(udidArray[i]);
      second = getInt(udidArray[i+8]);
      third = getInt(udidArray[i+16]);
      fourth = getInt(udidArray[i+24]);
      sum = first + second + third + fourth + over;
      over  = sum / 16;
      if (getInt(udidArray[i+32]) != (sum % 16)) {
          (*env)->ReleaseStringUTFChars(env, udid, udidArray);
          return JNI_FALSE;
      }
  }
  (*env)->ReleaseStringUTFChars(env, udid, udidArray);
  return JNI_TRUE;
}
