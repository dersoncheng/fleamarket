adb root
adb remount
adb shell mkdir /data/data/com.wandoujia.phoenix2/files/plugins
adb push $2 /data/data/com.wandoujia.phoenix2/files/plugins/$1.apk
