package com.wandoujia.base.utils;

import java.util.ArrayList;
import java.util.List;

import android.os.Build;

/**
 * created by xubowen@wandoujia.com
 * moved from FolderBlacklistUtil.java to base by yunjie on 14-7-29
 */
public class AppKillBlacklistUtil {

  private static final List<String> appKillWhiteList = new ArrayList<String>();

  static {
    // for app kill
    appKillWhiteList.add("com.wandoujia.phoenix2");
    appKillWhiteList.add("android");
    appKillWhiteList.add("com.android.phone");
    appKillWhiteList.add("com.android.mms");
    appKillWhiteList.add("com.android.systemui");
    appKillWhiteList.add("com.android.providers.settings");
    appKillWhiteList.add("com.android.providers.applications");
    appKillWhiteList.add("com.android.providers.contacts");
    appKillWhiteList.add("com.android.providers.userdictionary");
    appKillWhiteList.add("com.android.providers.telephony");
    appKillWhiteList.add("com.android.providers.drm");
    appKillWhiteList.add("com.android.providers.downloads");
    appKillWhiteList.add("com.android.providers.media");

    String manufacturer = Build.MANUFACTURER;
    if (manufacturer.equalsIgnoreCase("HTC")) {
      appKillWhiteList.add("com.android.htccontacts");
      appKillWhiteList.add("com.android.htcdialer");
      appKillWhiteList.add("com.htc.messagecs");
      appKillWhiteList.add("com.htc.idlescreen.shortcut");
      appKillWhiteList.add("com.android.providers.htcCheckin");
    } else if (manufacturer.equalsIgnoreCase("ZTE")) {
      appKillWhiteList.add("zte.com.cn.alarmclock");
      appKillWhiteList.add("com.android.utk");
    } else if (manufacturer.equalsIgnoreCase("huawei")) {
      appKillWhiteList.add("com.huawei.widget.localcityweatherclock");
    } else if (manufacturer.equalsIgnoreCase("Sony Ericsson")) {
      appKillWhiteList.add("com.sonyericsson.provider.useragent");
      appKillWhiteList.add("com.sonyericsson.provider.customization");
      appKillWhiteList.add("com.sonyericsson.secureclockservice");
      appKillWhiteList.add("com.sonyericsson.widget.digitalclock");
      appKillWhiteList.add("com.sonyericsson.digitalclockwidget");
    } else if (manufacturer.equalsIgnoreCase("samsung")) {
      appKillWhiteList.add("com.samsung.inputmethod");
      appKillWhiteList.add("com.sec.android.app.controlpanel");
      appKillWhiteList.add("com.sonyericsson.provider.customization");
    } else if (manufacturer.equalsIgnoreCase("motorola")) {
      appKillWhiteList.add("com.motorola.numberlocation");
      appKillWhiteList.add("com.motorola.android.fota");
      appKillWhiteList.add("com.motorola.atcmd");
      appKillWhiteList.add("com.motorola.locationsensor");
      appKillWhiteList.add("com.motorola.blur.conversations");
      appKillWhiteList.add("com.motorola.blur.alarmclock");
      appKillWhiteList.add("com.motorola.blur.providers.contacts");
    } else if (manufacturer.equalsIgnoreCase("LGE")) {
      appKillWhiteList.add("com.lge.clock");
    } else if (manufacturer.equalsIgnoreCase("magnum2x")) {
      appKillWhiteList.add("ty.com.android.TYProfileSetting");
    }

    String model = Build.MODEL;
    if (model.equalsIgnoreCase("HTC Sensation Z710e")
        || model.equalsIgnoreCase("HTC Sensation G14")
        || model.equalsIgnoreCase("HTC Z710e")) {
      appKillWhiteList.add("android.process.acore");
    } else if (model.equalsIgnoreCase("LT18i")) {
      appKillWhiteList.add("com.sonyericsson.provider.customization");
      appKillWhiteList.add("com.sonyericsson.provider.useragent");
    } else if (model.equalsIgnoreCase("U8500") || model.equalsIgnoreCase("U8500 HiQQ")) {
      appKillWhiteList.add("android.process.launcherdb");
      appKillWhiteList.add("com.motorola.process.system");
      appKillWhiteList.add("com.nd.assistance.ServerService");
    } else if (model.equalsIgnoreCase("MT15I")) {
      appKillWhiteList.add("com.sonyericsson.eventstream.calllogplugin");
    } else if (model.equalsIgnoreCase("GT-I9100") || model.equalsIgnoreCase("GT-I9100G")) {
      appKillWhiteList.add("com.samsung.inputmethod");
      appKillWhiteList.add("com.sec.android.app.controlpanel");
      appKillWhiteList.add("com.sec.android.app.FileTransferManager");
      appKillWhiteList.add("com.sec.android.providers.downloads");
      appKillWhiteList.add("com.android.providers.downloads.ui");
    } else if (model.equalsIgnoreCase("DROIDX")) {
      appKillWhiteList.add("com.motorola.blur.contacts.data");
      appKillWhiteList.add("com.motorola.blur.contacts");
    } else if (model.equalsIgnoreCase("DROID2") || model.equalsIgnoreCase("DROID2 GLOBA")) {
      appKillWhiteList.add("com.motorola.blur.contacts");
    } else if (model.startsWith("U8800")) {
      appKillWhiteList.add("com.huawei.android.gpms");
      appKillWhiteList.add("com.android.hwdrm");
      appKillWhiteList.add("com.huawei.omadownload");
    } else if (model.equalsIgnoreCase("LG-P503")) {
      appKillWhiteList.add("com.lge.simcontacts");
    } else if (model.equalsIgnoreCase("XT702")) {
      appKillWhiteList.add("com.motorola.usb");
      appKillWhiteList.add("com.android.alarmclock");
    } else if (model.equalsIgnoreCase("e15i")) {
      appKillWhiteList.add("com.sec.ccl.csp.app.secretwallpaper.themetwo");
    } else if (model.equalsIgnoreCase("zte-c n600")) {
      appKillWhiteList.add("com.android.wallpaper");
      appKillWhiteList.add("com.android.musicvis");
      appKillWhiteList.add("com.android.magicsmoke");
    } else if (model.startsWith("GT-5830") || model.startsWith("HTC Velocity 4G")) {
      appKillWhiteList.add("com.android.providers.downloads.ui");
    }
  }

  public static boolean isAppInWhiteList(String packageName) {
    return appKillWhiteList.contains(packageName);
  }

}
