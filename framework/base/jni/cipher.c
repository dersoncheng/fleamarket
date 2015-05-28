#include <string.h>
#include <jni.h>

/*
 *  @Author yingyi
 *  @Author zhangnan
 *  Doc: https://docs.google.com/a/wandoujia.com/document/d/1eaxyAMBNzFoXimnUSerxSdE6yFBjSZYKNStlKZuLU7Y/edit#heading=h.mpwttvokz2l7
 */
#include "cipher.h"
#include "md5.h"

typedef unsigned char byte;

static char * CONFUSION_FAKE_KEY = "fake key";

static byte TRUE_AES_KEY[] = {
    120, 150, 68, 189, 82, 198, 226, 223, 111, 107, 81, 217, 78, 208, 80, 69
};

static char * CONFUSION_TRUE_KEY = "true key";

static byte FAKE_AES_KEY[] = {
    45, 96, 32, 92, 78, 10, 15, 93, 119, 86, 54, 111, 116, 56, 42, 84
};

static byte TRUE_AES_KEY1[] = {
    61, 17, 237, 209, 1, 42, 84, 60, 167, 109, 255, 124, 62, 10, 41, 122
};

#ifndef AUTH_KEY
static char *TRUE_AUTH_KEY = "YVBED0PiSU4fWnFulqnqMnmVtGVMRTlM";
#else
static char *TRUE_AUTH_KEY = AUTH_KEY;
#endif

static byte TRUE_AES_KEY2[] = {
    230, 230, 91, 78, 235, 163, 52, 228, 189, 52, 93, 247, 114, 39, 15, 125
};

static char *FAKE_AUTH_KEY = "XYBED1PiSU5aWnfulqmqMnmcrJRCJTlT";

static byte TRUE_AES_KEY3[] = {
    134, 143, 228, 64, 100, 98, 187, 50, 122, 63, 253, 92, 100, 87, 123, 32
};

#ifndef ANDROID_ID
static char *TRUE_ANDROID_ID = "wandoujia_android";
#else
static char *TRUE_ANDROID_ID = ANDROID_ID;
#endif

#define MD5_LEN 16
static unsigned char P4_SIGNATURE_MD5[MD5_LEN] = {
    117, 22, 132, 244, 236, 254, 88, 130, 27, 58, 166, 68, 254, 80, 46, 156
};
static unsigned char ASTUBE_SIGNATURE_MD5[MD5_LEN] = {
    0x85, 0xee, 0xe8, 0xa0, 0xb9, 0x65, 0x7c, 0xff, 0x21, 0x0a, 0x7b, 0x20, 0xf7, 0xd2, 0x86, 0x34
};

static unsigned char *SIGNATURES[] = {
    P4_SIGNATURE_MD5,
    ASTUBE_SIGNATURE_MD5
};

static char *FAKE_ANDROID_ID = "android_wandou";

static unsigned char *cur_signature_md5 = NULL;

static int isOfficialSignature(unsigned char * cur_signature_md5) {
    return 1;
// XXX: For collecting signatures, !!! NEVER !!! uncomment it unless you know what you are doing
//    int i, j;
//    for (j=0; j<MD5_LEN; j++) {
//        unsigned char c = cur_signature_md5[j];
//        __android_log_print(ANDROID_LOG_INFO,"cipher", "[%d] 0x%02x", j, c);
//    }

//    int i;
//    for (i=0; i<sizeof(SIGNATURES)/sizeof(SIGNATURES[0]); i++) {
//        if (memcmp(cur_signature_md5, SIGNATURES[i], MD5_LEN) == 0) {
//            return 1;
//        }
//    }
//    return 0;
}

static int isPhoenix2(char *packageName) {
    // XXX: Just return true here since checking signature is enough
    return 1;
}

static int isOfficialPackageName(char *packagename) {
    if (isPhoenix2(packagename)) {
        return 1;
    }
    return 0;
}

static void get_pkg_signature_md5(JNIEnv* env, jclass this, jobject context, jstring packageName, unsigned char digest[16]){
    if(context == NULL || packageName == NULL){
        return;
    }

    /* get packageManager */
    jclass android_content_Context = (*env)->GetObjectClass(env, context);
    jmethodID midGetPackageManager = (*env)->GetMethodID(env, android_content_Context, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject packageManager = (*env)->CallObjectMethod(env, context, midGetPackageManager);
    if(packageManager == NULL){
        return;
    }

    /* get packageInfo */
    jclass android_content_pm_PackageManager = (*env)->FindClass(env, "android/content/pm/PackageManager");
    jmethodID midGetPackageInfo = (*env)->GetMethodID(env, android_content_pm_PackageManager, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jfieldID fidSignatureFlag = (*env)->GetStaticFieldID(env, android_content_pm_PackageManager, "GET_SIGNATURES", "I");
    jint fignatureFlag = (*env)->GetStaticIntField(env, android_content_pm_PackageManager, fidSignatureFlag);
    jobject packageInfo = (jobject)(*env)->CallObjectMethod(env, packageManager, midGetPackageInfo, packageName, fignatureFlag);
    if(packageInfo == NULL) {
        return;
    }

    /* get signature */
    jclass android_content_pm_PackageInfo = (*env)->FindClass(env, "android/content/pm/PackageInfo");
    jfieldID fidSignatures = (*env)->GetFieldID(env, android_content_pm_PackageInfo, "signatures", "[Landroid/content/pm/Signature;");
    jobjectArray signatures = (*env)->GetObjectField(env, packageInfo, fidSignatures);
    jobject signature = (*env)->GetObjectArrayElement(env, signatures, 0);

    /* get signature string */
    jclass android_content_pm_Signature = (*env)->FindClass(env, "android/content/pm/Signature");
    jmethodID midToCharString = (*env)->GetMethodID(env, android_content_pm_Signature, "toCharsString", "()Ljava/lang/String;");
    jstring signatureStr = (jstring)(*env)->CallObjectMethod(env, signature, midToCharString);

    /* get signature md5 */
    char *p_sig = (*env)->GetStringUTFChars(env, signatureStr, NULL);
    md5_vector(p_sig, digest);

    (*env)->ReleaseStringUTFChars(env, signatureStr, p_sig);
}

/* The function below is for test. Do not delete please. */
/*
jcharArray Java_com_wandoujia_base_utils_CipherUtil_getPkgSignatureMD5(JNIEnv* env, jclass this, jobject context, jstring packageName){
    unsigned char digest[16];
    jcharArray jarray = (*env)->NewCharArray(env, 16);
    jchar *jcharBuffer;
    int i = 0;

    get_pkg_signature_md5(env, this, context, packageName, digest);

    jcharBuffer = (jchar *)calloc(sizeof(jchar), 16);
    for (i = 0; i < 16; i ++) {
        jcharBuffer[i] = (jchar)digest[i];
    }
    (*env)->SetCharArrayRegion(env, jarray, 0, 16, jcharBuffer);

    free(jcharBuffer);
    return jarray;
}
*/

JNIEXPORT jbyteArray JNICALL Java_com_wandoujia_base_utils_CipherUtil_getAESKeyNative(JNIEnv* env, jclass this, jobject context)
{
    /* check PackageName */
    jclass android_content_Context = (*env)->GetObjectClass(env, context);
    jmethodID midGetPackageName = (*env)->GetMethodID(env, android_content_Context, "getPackageName", "()Ljava/lang/String;");
    jstring jpackageName = (jstring)(*env)->CallObjectMethod(env, context, midGetPackageName);
    char *packagename = (*env)->GetStringUTFChars(env, jpackageName, NULL);

    byte *trueKeyStrore[4] = { TRUE_AES_KEY, TRUE_AES_KEY1, TRUE_AES_KEY2, TRUE_AES_KEY3};
    int i = 0;
    jbyte *keybyte;

    if (!isOfficialPackageName(packagename)) {
        keybyte = (jbyte *)FAKE_AES_KEY;
    } else {
        /* check signature */
        if(cur_signature_md5 == NULL){
           cur_signature_md5 = (unsigned char *)malloc(sizeof(char) * 16);
           get_pkg_signature_md5(env, this, context, jpackageName, cur_signature_md5);
        }

        if(isOfficialSignature(cur_signature_md5)) {
            keybyte = (jbyte *)malloc(sizeof(jbyte) * 16);
            for(i = 0; i < 16; i++){
                keybyte[i] = (jbyte)trueKeyStrore[i % 4][i];
            }
        } else {
            keybyte = (jbyte *)FAKE_AES_KEY;
        }
    }

    jbyteArray jarray = (*env)->NewByteArray(env, 16);
    (*env)->SetByteArrayRegion(env, jarray, 0, 16, keybyte);

    if (FAKE_AES_KEY != keybyte) {
        free(keybyte);
    }
    (*env)->ReleaseStringUTFChars(env, jpackageName, packagename);

    return jarray;
}

JNIEXPORT jstring JNICALL Java_com_wandoujia_base_utils_CipherUtil_getAuthKeyNative(JNIEnv* env, jclass this, jobject context)
{
    jclass android_content_Context = (*env)->GetObjectClass(env, context);
    jmethodID midGetPackageName = (*env)->GetMethodID(env, android_content_Context, "getPackageName", "()Ljava/lang/String;");
    jstring jpackageName = (jstring)(*env)->CallObjectMethod(env, context, midGetPackageName);
    char *packagename = (*env)->GetStringUTFChars(env, jpackageName, NULL);

    char *key;
    if(isOfficialPackageName(packagename)) {
        key = TRUE_AUTH_KEY;
    } else {
        key = FAKE_AUTH_KEY;
    }

    jstring jauthkey = (*env)->NewStringUTF(env, key);
    return jauthkey;
}

JNIEXPORT jstring JNICALL Java_com_wandoujia_base_utils_CipherUtil_getAndroidIdNative(JNIEnv* env, jclass this, jobject context)
{
    jclass android_content_Context = (*env)->GetObjectClass(env, context);
    jmethodID midGetPackageName = (*env)->GetMethodID(env, android_content_Context, "getPackageName", "()Ljava/lang/String;");
    jstring jpackageName = (jstring)(*env)->CallObjectMethod(env, context, midGetPackageName);
    char *packagename = (*env)->GetStringUTFChars(env, jpackageName, NULL);

    char *id;
    if (isPhoenix2(packagename)) {
        id = TRUE_ANDROID_ID;
    } else {
        id = FAKE_ANDROID_ID;
    }

    jstring jandroidid = (*env)->NewStringUTF(env, id);
    return jandroidid;
}
