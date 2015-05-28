package com.wandoujia.base.utils;

import android.telephony.ServiceState;

public class TelephonySignalStrengthUtil {
  public static final int SIGNAL_STRENGTH_UNKNOWN = -1;
  public static final int SIGNAL_STRENGTH_NONE = 0;
  public static final int SIGNAL_STRENGTH_POOR = 1;
  public static final int SIGNAL_STRENGTH_MODERATE = 2;
  public static final int SIGNAL_STRENGTH_GOOD = 3;
  public static final int SIGNAL_STRENGTH_GREAT = 4;

  /**
   * Get Gsm signal strength as dBm
   *
   */
  public static int getGsmDbm(int gsmSignalStrength) {
    int dBm;

    int asu = (gsmSignalStrength == 99 ? -1 : gsmSignalStrength);
    if (asu != -1) {
      dBm = -113 + (2 * asu);
    } else {
      dBm = -1;
    }
    return dBm;
  }

  /**
   * Get gsm as level 0..4
   *
   */
  public static int getGsmLevel(int gsmSignalStrength) {
    int level;

    // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
    // asu = 0 (-113dB or less) is very weak
    // signal, its better to show 0 bars to the user in such cases.
    // asu = 99 is a special case, where the signal strength is unknown.
    int asu = gsmSignalStrength;
    if (asu == 99) {
      level = SIGNAL_STRENGTH_UNKNOWN;
    } else if (asu <= 2) {
      level = SIGNAL_STRENGTH_NONE;
    } else if (asu >= 12) {
      level = SIGNAL_STRENGTH_GREAT;
    } else if (asu >= 8) {
      level = SIGNAL_STRENGTH_GOOD;
    } else if (asu >= 5) {
      level = SIGNAL_STRENGTH_MODERATE;
    } else {
      level = SIGNAL_STRENGTH_POOR;
    }
    return level;
  }

  /**
   * Get cdma as level 0..4
   *
   */
  public static int getCdmaLevel(int cdmaDbm, int cdmaEcio) {
    int levelDbm;
    int levelEcio;

    if (cdmaDbm >= -75) {
      levelDbm = SIGNAL_STRENGTH_GREAT;
    } else if (cdmaDbm >= -85) {
      levelDbm = SIGNAL_STRENGTH_GOOD;
    } else if (cdmaDbm >= -95) {
      levelDbm = SIGNAL_STRENGTH_MODERATE;
    } else if (cdmaDbm >= -100) {
      levelDbm = SIGNAL_STRENGTH_POOR;
    } else {
      levelDbm = SIGNAL_STRENGTH_NONE;
    }

    // Ec/Io are in dB*10
    if (cdmaEcio >= -90) {
      levelEcio = SIGNAL_STRENGTH_GREAT;
    } else if (cdmaEcio >= -110) {
      levelEcio = SIGNAL_STRENGTH_GOOD;
    } else if (cdmaEcio >= -130) {
      levelEcio = SIGNAL_STRENGTH_MODERATE;
    } else if (cdmaEcio >= -150) {
      levelEcio = SIGNAL_STRENGTH_POOR;
    } else {
      levelEcio = SIGNAL_STRENGTH_NONE;
    }

    int level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
    return level;
  }

  /**
   * Get the cdma signal level as an asu value between 0..31, 99 is unknown
   *
   */
  public static int getCdmaAsuLevel(int cdmaDbm, int cdmaEcio) {
    int cdmaAsuLevel;
    int ecioAsuLevel;

    if (cdmaDbm >= -75) {
      cdmaAsuLevel = 16;
    } else if (cdmaDbm >= -82) {
      cdmaAsuLevel = 8;
    } else if (cdmaDbm >= -90) {
      cdmaAsuLevel = 4;
    } else if (cdmaDbm >= -95) {
      cdmaAsuLevel = 2;
    } else if (cdmaDbm >= -100) {
      cdmaAsuLevel = 1;
    } else {
      cdmaAsuLevel = 99;
    }

    // Ec/Io are in dB*10
    if (cdmaEcio >= -90) {
      ecioAsuLevel = 16;
    } else if (cdmaEcio >= -100) {
      ecioAsuLevel = 8;
    } else if (cdmaEcio >= -115) {
      ecioAsuLevel = 4;
    } else if (cdmaEcio >= -130) {
      ecioAsuLevel = 2;
    } else if (cdmaEcio >= -150) {
      ecioAsuLevel = 1;
    } else {
      ecioAsuLevel = 99;
    }

    int level = (cdmaAsuLevel < ecioAsuLevel) ? cdmaAsuLevel : ecioAsuLevel;
    return level;
  }

  /**
   * Get Evdo as level 0..4
   *
   */
  public static int getEvdoLevel(int evdoDbm, int evdoSnr) {
    int levelEvdoDbm;
    int levelEvdoSnr;

    if (evdoDbm >= -65) {
      levelEvdoDbm = SIGNAL_STRENGTH_GREAT;
    } else if (evdoDbm >= -75) {
      levelEvdoDbm = SIGNAL_STRENGTH_GOOD;
    } else if (evdoDbm >= -90) {
      levelEvdoDbm = SIGNAL_STRENGTH_MODERATE;
    } else if (evdoDbm >= -105) {
      levelEvdoDbm = SIGNAL_STRENGTH_POOR;
    } else {
      levelEvdoDbm = SIGNAL_STRENGTH_NONE;
    }

    if (evdoSnr >= 7) {
      levelEvdoSnr = SIGNAL_STRENGTH_GREAT;
    } else if (evdoSnr >= 5) {
      levelEvdoSnr = SIGNAL_STRENGTH_GOOD;
    } else if (evdoSnr >= 3) {
      levelEvdoSnr = SIGNAL_STRENGTH_MODERATE;
    } else if (evdoSnr >= 1) {
      levelEvdoSnr = SIGNAL_STRENGTH_POOR;
    } else {
      levelEvdoSnr = SIGNAL_STRENGTH_NONE;
    }

    int level = (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
    return level;
  }

  /**
   * Get the evdo signal level as an asu value between 0..31, 99 is unknown
   *
   */
  public static int getEvdoAsuLevel(int evdoDbm, int evdoSnr) {
    int levelEvdoDbm;
    int levelEvdoSnr;

    if (evdoDbm >= -65) {
      levelEvdoDbm = 16;
    } else if (evdoDbm >= -75) {
      levelEvdoDbm = 8;
    } else if (evdoDbm >= -85) {
      levelEvdoDbm = 4;
    } else if (evdoDbm >= -95) {
      levelEvdoDbm = 2;
    } else if (evdoDbm >= -105) {
      levelEvdoDbm = 1;
    } else {
      levelEvdoDbm = 99;
    }

    if (evdoSnr >= 7) {
      levelEvdoSnr = 16;
    } else if (evdoSnr >= 6) {
      levelEvdoSnr = 8;
    } else if (evdoSnr >= 5) {
      levelEvdoSnr = 4;
    } else if (evdoSnr >= 3) {
      levelEvdoSnr = 2;
    } else if (evdoSnr >= 1) {
      levelEvdoSnr = 1;
    } else {
      levelEvdoSnr = 99;
    }

    int level = (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
    return level;
  }

  public static boolean hasService(ServiceState serviceState) {
    if (serviceState != null) {
      switch (serviceState.getState()) {
        case ServiceState.STATE_OUT_OF_SERVICE:
        case ServiceState.STATE_EMERGENCY_ONLY:
        case ServiceState.STATE_POWER_OFF:
          return false;
        default:
          return true;
      }
    } else {
      return false;
    }
  }
}
