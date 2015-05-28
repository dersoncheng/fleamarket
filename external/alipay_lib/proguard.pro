-dontshrink
-dontpreverify 
-dontoptimize 
-dontusemixedcaseclassnames 

-flattenpackagehierarchy
-allowaccessmodification 
-printmapping map.txt 

-optimizationpasses 7 
-verbose 
-keepattributes Exceptions,InnerClasses
-dontskipnonpubliclibraryclasses 
-dontskipnonpubliclibraryclassmembers 

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends java.lang.Throwable {*;}
-keep public class * extends java.lang.Exception {*;}

-libraryjars libs/android-support-v13.jar
-libraryjars libs/picasso-1.1.1.jar
-libraryjars libs/MobileSecSdk.jar
-libraryjars libs/trobotexternalinterface.jar
-libraryjars libs/utdid.jar

-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.android.app.pay.IAlixPay{*;}
-keep class com.alipay.android.app.pay.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.pay.IAlixPayCallback{*;}
-keep class com.alipay.android.app.pay.IAlixPayCallback$Stub{*;}
-keep class com.alipay.android.app.script.**{*;}
-keep class com.alipay.android.app.pay.PayTask{*;}
-keep class com.alipay.android.app.pay.PayTask$OnPayListener{*;}
-keep class com.alipay.android.app.encrypt.*{*;}


-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# adding this in to preserve line numbers so that the stack traces
# can be remapped
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable