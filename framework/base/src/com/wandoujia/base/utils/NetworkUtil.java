package com.wandoujia.base.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.wandoujia.base.config.GlobalConfig;

public final class NetworkUtil {

  public static final int NETWORK_TYPE_NONE = -1;
  public static final int NETWORK_TYPE_PC = -2;
  public static final int NETWORK_TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
  public static final int NETWORK_TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
  private static boolean reverseProxyOn = false;

  private static final String ANDROID_HOTSPOT_IP_ADDRESS = "192.168.43.1";
  private static final String IOS_HOTSPOT_IP_ADDRESS = "172.20.10.1";

  private NetworkUtil() {}

  public static boolean isNetworkConnected(Context context) {
    if (reverseProxyOn) {
      return true;
    }
    ConnectivityManager connManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = null;
    try {
      activeNetworkInfo = connManager.getActiveNetworkInfo();
    } catch (Exception e) {
      // in some roms, here maybe throw a exception(like nullpoint).
      e.printStackTrace();
    }
    return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
  }

  public static NetworkInfo getNetworkInfo(Context context) {
    ConnectivityManager connManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    return connManager.getActiveNetworkInfo();
  }

  public static boolean isMobileNetworkConnected(Context context) {
    ConnectivityManager connManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo =
        connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    return networkInfo != null && networkInfo.isConnected();
  }

  public static boolean isWifiConnected(Context context) {
    ConnectivityManager connManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connManager == null) {
      return false;
    }
    NetworkInfo networkInfo = null;
    try {
      // maybe throw exception in android framework
      networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // can not use pingSupplicant (), on cm9 or some other roms it will
    // block whole wifi network!
    return (networkInfo != null && networkInfo.isConnected());
  }

  /**
   * Convert a IPv4 address from an integer to an InetAddress.
   * 
   * @param hostAddress is an Int corresponding to the IPv4 address in network byte order
   * @return the IP address as an {@code InetAddress}, returns null if
   *         unable to convert or if the int is an invalid address.
   */
  private static InetAddress intToInetAddress(int hostAddress) {
    InetAddress inetAddress = null;
    byte[] addressBytes = {(byte) (0xff & hostAddress),
        (byte) (0xff & (hostAddress >> 8)),
        (byte) (0xff & (hostAddress >> 16)),
        (byte) (0xff & (hostAddress >> 24))};

    try {
      inetAddress = InetAddress.getByAddress(addressBytes);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return inetAddress;
  }

  /**
   * Check wifi is hotSpot or not.
   * 
   * @return whether wifi is hotSpot or not.
   */
  public static boolean checkWifiIsHotSpot(Context context) {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    if (wifiManager == null) {
      return false;
    }
    DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
    if (dhcpInfo == null) {
      return false;
    }
    InetAddress address = intToInetAddress(dhcpInfo.gateway);
    if (address == null) {
      return false;
    }
    String currentGateway = address.getHostAddress();
    return TextUtils.equals(currentGateway, ANDROID_HOTSPOT_IP_ADDRESS)
        || TextUtils.equals(currentGateway, IOS_HOTSPOT_IP_ADDRESS);
  }

  public static boolean isReverseProxyOn() {
    return reverseProxyOn;
  }

  public static void setUsbReverseProxyState(boolean proxyOn) {
    reverseProxyOn = proxyOn;
  }

  public static int getNetworkType() {
    ConnectivityManager connManager =
        (ConnectivityManager) GlobalConfig.getAppContext().getSystemService(
            Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo;
    try {
      networkInfo = connManager.getActiveNetworkInfo();
    } catch (NullPointerException e) {
      // get some crash that getActiveNetworkInfo() may throw NullPointerException in some ROM...
      // so catch it here
      e.printStackTrace();
      return NETWORK_TYPE_NONE;
    }
    return parseNetworkType(networkInfo);
  }

  public static int parseNetworkType(NetworkInfo networkInfo) {
    if (networkInfo == null || !networkInfo.isConnected()) {
      return NETWORK_TYPE_NONE;
    }
    if (networkInfo.getType() == NETWORK_TYPE_MOBILE) {
      return NETWORK_TYPE_MOBILE;
    } else {
      return NETWORK_TYPE_WIFI;
    }
  }

  /**
   * Get the network type name. If currently connected to a mobile network, the detail mobile
   * network type name will be returned.
   * 
   * @param context
   * @return
   */
  public static String getNetworkTypeName(Context context) {
    ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(
        Context.CONNECTIVITY_SERVICE);
    if (connManager == null) {
      return null;
    }
    try {
      // in some rom and a special time, it maybe throw NullPointer ex,
      // we have to catch it, and return a null value.
      final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
      return getNetworkTypeName(context, networkInfo);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Get the network type name. If currently connected to a mobile network, the detail mobile
   * network type name will be returned.
   * 
   * @param context
   * @param networkInfo
   * @return the network type name.
   */
  public static String getNetworkTypeName(Context context, NetworkInfo networkInfo) {
    if (networkInfo == null) {
      if (reverseProxyOn) {
        return "PC";
      }
    } else if (networkInfo.getType() == NETWORK_TYPE_MOBILE) {
      TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
          Context.TELEPHONY_SERVICE);
      return getNetworkTypeName(telephonyManager.getNetworkType());
    } else {
      return "WIFI";
    }
    return "NONE"; // no network connected. change null to "NONE", so it can be known.
  }

  private static String getNetworkTypeName(int type) {
    switch (type) {
      case TelephonyManager.NETWORK_TYPE_GPRS:
        return "GPRS";
      case TelephonyManager.NETWORK_TYPE_EDGE:
        return "EDGE";
      case TelephonyManager.NETWORK_TYPE_UMTS:
        return "UMTS";
      case TelephonyManager.NETWORK_TYPE_HSDPA:
        return "HSDPA";
      case TelephonyManager.NETWORK_TYPE_HSUPA:
        return "HSUPA";
      case TelephonyManager.NETWORK_TYPE_HSPA:
        return "HSPA";
      case TelephonyManager.NETWORK_TYPE_CDMA:
        return "CDMA";
      case TelephonyManager.NETWORK_TYPE_EVDO_0:
        return "CDMA - EvDo rev. 0";
      case TelephonyManager.NETWORK_TYPE_EVDO_A:
        return "CDMA - EvDo rev. A";
      case TelephonyManager.NETWORK_TYPE_EVDO_B:
        return "CDMA - EvDo rev. B";
      case TelephonyManager.NETWORK_TYPE_1xRTT:
        return "CDMA - 1xRTT";
      case TelephonyManager.NETWORK_TYPE_LTE:
        return "LTE";
      case TelephonyManager.NETWORK_TYPE_EHRPD:
        return "CDMA - eHRPD";
      case TelephonyManager.NETWORK_TYPE_IDEN:
        return "iDEN";
      case TelephonyManager.NETWORK_TYPE_HSPAP:
        return "HSPA+";
      default:
        return "UNKNOWN";
    }
  }

  public static int[] getNetworkTypeInfo(Context context) {
    ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(
        Context.CONNECTIVITY_SERVICE);
    int[] result = new int[] {NETWORK_TYPE_NONE, TelephonyManager.NETWORK_TYPE_UNKNOWN};
    if (connManager == null) {
      return result;
    }
    try {
      // in some rom and a special time, it maybe throw NullPointer ex,
      // we have to catch it, and return a null value.
      final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
      if (networkInfo == null) {
        if (reverseProxyOn) {
          result[0] = NETWORK_TYPE_PC;
        }
        return result;
      } else if (networkInfo.getType() == NETWORK_TYPE_WIFI && networkInfo.isConnected()) {
        result[0] = NETWORK_TYPE_WIFI;
        return result;
      } else if (networkInfo.getType() == NETWORK_TYPE_MOBILE && networkInfo.isConnected()) {
        result[0] = NETWORK_TYPE_MOBILE;
        result[1] = networkInfo.getSubtype();
        return result;
      }
    } catch (Exception e) {
      return result;
    }
    return result;
  }

  public static String getISP(Context context) {
    try {
      TelephonyManager manager =
          (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      if (manager == null) {
        return "unknown";
      }
      return manager.getNetworkOperatorName();
    } catch (Exception e) {
      return "unknown";
    }
  }

  /**
   * Get IP address from first non-localhost interface
   * 
   * @param ipv4 true=return ipv4, false=return ipv6
   * @return address or empty string
   */
  public static String getIPAddress(boolean useIPv4) {
    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface intf : interfaces) {
        List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
        for (InetAddress addr : addrs) {
          if (!addr.isLoopbackAddress()) {
            String sAddr = addr.getHostAddress().toUpperCase();
            boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
            if (useIPv4) {
              if (isIPv4)
                return sAddr;
            } else {
              if (!isIPv4) {
                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                return delim < 0 ? sAddr : sAddr.substring(0, delim);
              }
            }
          }
        }
      }
    } catch (Exception ex) {} // for now eat exceptions
    return "";
  }

}
