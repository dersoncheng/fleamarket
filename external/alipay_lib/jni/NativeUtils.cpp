#include "NativeUtils.h"

/*
 * Class:     com_alipay_android_app_util_NativeUtils
 * Method:    getClientKey
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL getClientKey(JNIEnv *env, jclass clazz) {
	return 0;
}

/*
 * Class:     com_alipay_android_app_util_NativeUtils
 * Method:    serializeRequest
 * Signature: (Lcom/alipay/android/app/net/Request;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL serializeRequest(JNIEnv *env, jclass clazz,
		jobject request) {
	return 0;
}

/*
 * Class:     com_alipay_android_app_util_NativeUtils
 * Method:    deserializeResponse
 * Signature: (Ljava/lang/String;)Lcom/alipay/android/app/net/Response;
 */
JNIEXPORT jobject JNICALL deserializeResponse(JNIEnv *env, jclass clazz,
		jstring response) {
	return 0;
}

static JNINativeMethod methods[] = { { "getClientKey", "()Ljava/lang/String;",
		(void*) getClientKey }, { "serializeRequest",
		"(Lcom/alipay/android/app/net/Request;)Ljava/lang/String;",
		(void*) serializeRequest }, { "deserializeResponse",
		"(Ljava/lang/String;)Lcom/alipay/android/app/net/Response;",
		(void*) deserializeResponse } };

static int RegisterNatives(JNIEnv *env) {
	jclass clazz = env->FindClass(
			"com/alipay/android/app/util/NativeUtils");
	if (!clazz)
		return JNI_FALSE;

	int result = env->RegisterNatives(clazz, methods,
			(sizeof(methods) / sizeof(methods[0])));

	env->DeleteLocalRef(clazz);

	return result;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	JNIEnv *env;

	/* Get environment */
	if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		return JNI_FALSE;
	}

	RegisterNatives(env);

	return JNI_VERSION_1_6;
}
