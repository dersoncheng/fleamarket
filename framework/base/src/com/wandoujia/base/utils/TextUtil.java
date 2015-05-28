package com.wandoujia.base.utils;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import com.pinyin4android.PinyinUtil;
import com.wandoujia.base.R;

public final class TextUtil {

  private static final String MAGIC_SPLITTER = "#splitter#";
  private static final int SECONDS_OF_HOUR = 60 * 60;
  private static final int SECONDS_OF_MINUTE = 60;
  private static final int MILLIS_OF_SECOND = 1000;

  private static final long WAN = 10000L;

  private final static ThreadLocalDecimalFormat DECIMAL_FORMAT = new ThreadLocalDecimalFormat();

  private static final long DEFAULT_STORAGE_SIZE_BASE = 1024;

  public static final class StorageSize {
    public static final long KILO = DEFAULT_STORAGE_SIZE_BASE;
    public static final long MEGA = DEFAULT_STORAGE_SIZE_BASE * DEFAULT_STORAGE_SIZE_BASE;
    public static final long GIGA = DEFAULT_STORAGE_SIZE_BASE * DEFAULT_STORAGE_SIZE_BASE
        * DEFAULT_STORAGE_SIZE_BASE;
  }

  /**
   * Get formatted date.
   * 
   * @param time time
   * @return formatted date like "yyyy-MM-dd hh:mm:ss"
   */
  public static String formatDateInfoToSecond(long time) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    return sdf.format(new Date(time));
  }

  /**
   * Get formatted date.
   * 
   * @param time time
   * @return formatted date like "yyyy-MM-dd"
   */
  public static String formatDateInfoToDay(long time) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    return sdf.format(new Date(time));
  }

  /**
   * Get formatted date.
   * 
   * @param time time
   * @return formatted date like "MM-dd"
   */
  public static String formatDateInfoToMonthDay(long time) {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
    return sdf.format(new Date(time));
  }

  public static boolean checkGlVersion(String glVersion) {
    if (TextUtils.isEmpty(glVersion)) {
      return false;
    }
    String glVersionRegExp = "[0-9].[0-9]";
    return glVersion.matches(glVersionRegExp);
  }

  public static boolean checkEmail(String email) {
    if (TextUtils.isEmpty(email)) {
      return false;
    }
    String emailRegExp = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
    return email.matches(emailRegExp);
  }


  public static boolean checkEmailOrPhone(String account) {
    if (TextUtils.isEmpty(account)) {
      return false;
    }
    String mobileRegExp = "(\\+86)?1[0-9]{10}";
    String emailRegExp = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
    return account.matches(mobileRegExp) || account.matches(emailRegExp);
  }

  public static String formatTimeInfo(long time) {
    long HOUR_LEN = 60 * 60 * 1000;
    long MIN_LEN = 60 * 1000;
    long SECOND_LEN = 1000;
    StringBuilder timeText = new StringBuilder();
    if (time >= HOUR_LEN) {
      long hour = time / HOUR_LEN;
      if (hour < 10) {
        timeText.append('0');
      }
      timeText.append(hour).append(':');
    }
    if (time >= MIN_LEN) {
      long minute = (time % HOUR_LEN) / MIN_LEN;
      if (minute < 10) {
        timeText.append('0');
      }
      timeText.append(minute).append(':');
    } else {
      timeText.append("00:");
    }
    if (time >= 0) {
      long second = ((time % HOUR_LEN) % MIN_LEN) / SECOND_LEN;
      if (second < 10) {
        timeText.append('0');
      }
      timeText.append(second);
    }
    return timeText.toString();
  }

  public static String formatTimeInfoWithHour(long time) {
    long HOUR_LEN = 60 * 60 * 1000;
    long MIN_LEN = 60 * 1000;
    long SECOND_LEN = 1000;
    StringBuilder timeText = new StringBuilder();
    long hour = time / HOUR_LEN;
    if (hour < 10) {
      timeText.append('0');
    }
    timeText.append(hour).append(':');
    long minute = (time % HOUR_LEN) / MIN_LEN;
    if (minute < 10) {
      timeText.append('0');
    }
    timeText.append(minute).append(':');
    long second = ((time % HOUR_LEN) % MIN_LEN) / SECOND_LEN;
    if (second < 10) {
      timeText.append('0');
    }
    timeText.append(second);
    return timeText.toString();
  }

  public static String formatSizeInfo(double size) {
    return formatSizeInfo(size, DECIMAL_FORMAT.get());
  }

  public static String formatSizeInfo(double size, DecimalFormat format) {
    return formatSizeInfo(size, format, DEFAULT_STORAGE_SIZE_BASE);
  }

  /**
   * 
   * @param size storage size to format to "XXMB" "XXGB"
   * @param format can be null. if null, use default format "0.0"
   * @param sizeBase can be 0. if 0, use default size base 1024
   * 
   * @return
   */
  public static String formatSizeInfo(double size, DecimalFormat format, long sizeBase) {
    long storageSizeBase = sizeBase == 0 ? DEFAULT_STORAGE_SIZE_BASE : sizeBase;
    final long KILO = storageSizeBase;
    final long MEGA = storageSizeBase * storageSizeBase;
    final long GIGA = storageSizeBase * storageSizeBase * storageSizeBase;
    DecimalFormat decimalFormat = (format == null ? DECIMAL_FORMAT.get() : format);
    StringBuilder infoText = new StringBuilder();
    if (size <= 0) {
      infoText.append("0KB");
      return infoText.toString();
    }
    if (size > GIGA) {
      infoText.append(decimalFormat.format((size / GIGA))).append("GB");
      return infoText.toString();
    } else if (size > MEGA) {
      infoText.append(decimalFormat.format((size / MEGA))).append("MB");
      return infoText.toString();
    } else if (size > KILO) {
      infoText.append(decimalFormat.format((size / KILO))).append("KB");
    } else {
      infoText.append((int) size).append("B");
    }
    return infoText.toString();
  }

  public static String convert2Pinyin(Context context, String label) {
    return PinyinUtil.toPinyin(context, label);
  }

  /**
   * Convert each chinese char of this string into their first letter of pinyin.
   * For example, "周杰伦" -> "ZJL".
   * 
   * @param context
   * @param label
   * @return
   */
  public static String convert2PinyinFirstLetters(Context context, String label) {
    if (TextUtils.isEmpty(label)) {
      return null;
    }
    String normalizedLabel = label.trim();
    if (TextUtils.isEmpty(normalizedLabel)) {
      return null;
    }

    StringBuilder pinyinSB = new StringBuilder();
    for (final char letter : normalizedLabel.toCharArray()) {
      if (isChinese(letter)) {
        final String letterPinyin = PinyinUtil.toPinyin(context, letter);
        if (!TextUtils.isEmpty(letterPinyin)) {
          pinyinSB.append(letterPinyin.substring(0, 1));
        } else {
          pinyinSB.append(letter);
        }
      } else {
        pinyinSB.append(letter);
      }
    }
    return pinyinSB.toString().toUpperCase();
  }

  // public static String convert2Pinyin(String label) {
  // char[] chars = label.toCharArray();
  //
  // StringBuilder labelPinyin = new StringBuilder();
  //
  // for (char c : chars) {
  // String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c);
  // if (pinyin != null) {
  // for (String str : pinyin) {
  // labelPinyin.append(str).append(" ");
  // }
  // } else {
  // labelPinyin.append(c).append(" ");
  // }
  // }
  //
  // return labelPinyin.toString();
  // }

  public static List<Spanned> fromHtml(List<String> result) {
    List<Spanned> spannedList = new ArrayList<Spanned>();
    if (result != null) {
      for (String aResult : result) {
        spannedList.add(Html.fromHtml(aResult));
      }
    }
    return spannedList;
  }

  public static boolean isChinese(char c) {
    Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
    return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
        || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
        || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
        || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
        || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
        || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
  }

  public static String makeAppNamePair(String packageName, String appName) {
    return packageName + MAGIC_SPLITTER + appName;
  }

  public static String getAppNameFromAppNamePair(String namePair) {
    if (namePair == null || android.text.TextUtils.isEmpty(namePair)) {
      return null;
    }
    String[] fields = namePair.split(MAGIC_SPLITTER);
    if (fields == null || fields.length < 2) {
      return null;
    }
    return fields[1];
  }

  public static String getPakageNameFromAppNamePair(String namePair) {
    if (namePair == null || android.text.TextUtils.isEmpty(namePair)) {
      return null;
    }
    int index = namePair.indexOf(MAGIC_SPLITTER);
    if (index <= 0) {
      return null;
    }
    return namePair.substring(0, index);
  }

  public static String formatNumString(String nums, Context applicationContext) {
    if (TextUtils.isEmpty(nums)) {
      return "";
    }
    String demi = "";
    int levelBaseNum = Integer.valueOf(applicationContext
        .getString(R.string.num_split_level_base));
    int numLegth = nums.length();
    int resNum = numLegth % levelBaseNum;
    numLegth = numLegth <= 1 ? numLegth : numLegth - 1;
    int resLevel = numLegth / levelBaseNum;
    if (resNum == 0) {
      resNum = levelBaseNum;
    }

    if (resLevel != 0) {
      demi = String.valueOf(nums.charAt(resNum));
      if ("0".equals(demi)) {
        demi = "";
      } else {
        demi = "." + demi;
      }
    }
    switch (resLevel) {
      case 0:
        return nums;
      case 1:
        return nums.substring(0, resNum) + demi
            + applicationContext.getString(R.string.num_split_level_base_one);
      case 2:
        return nums.substring(0, resNum) + demi
            + applicationContext.getString(R.string.num_split_level_base_two);
      case 3:
        return nums.substring(0, resNum) + demi
            + applicationContext.getString(R.string.num_split_level_base_three);
      default:
        return nums;
    }
  }

  public static String formatNumString(long num, Context applicationContext) {
    if (num > WAN * WAN * WAN) {
      return (DECIMAL_FORMAT.get().format((float) num / (WAN * WAN * WAN)))
          + applicationContext.getString(R.string.num_split_level_base_three);
    } else if (num > WAN * WAN) {
      return (DECIMAL_FORMAT.get().format((float) num / (WAN * WAN)))
          + applicationContext.getString(R.string.num_split_level_base_two);
    } else if (num > WAN) {
      return (DECIMAL_FORMAT.get().format((float) num / WAN))
          + applicationContext.getString(R.string.num_split_level_base_one);
    } else {
      return String.valueOf(num);
    }
  }

  /**
   * get the (long) time in ms to formatted date just like "hh:mm:ss"
   * 
   * @param timeMs (long) time in ms or System.currentTimeMillis
   * @return a time string like "hh:mm:ss"
   */
  public static String stringForTime(long timeMs) {
    StringBuilder formatBuilder = new StringBuilder();
    Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
    long totalSeconds = timeMs / MILLIS_OF_SECOND;
    long seconds = totalSeconds % SECONDS_OF_MINUTE;
    long minutes = (totalSeconds / SECONDS_OF_MINUTE) % SECONDS_OF_MINUTE;
    long hours = totalSeconds / SECONDS_OF_HOUR;
    formatBuilder.setLength(0);
    return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
  }

  /**
   * get the (long) time in ms to formatted date just like "mm:ss"
   * 
   * @param timeMs (long) time in ms or System.currentTimeMillis
   * @return a time string like "mm:ss"
   */
  public static String stringForTimeInMinutes(long timeMs) {
    StringBuilder formatBuilder = new StringBuilder();
    Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
    long totalSeconds = timeMs / MILLIS_OF_SECOND;
    long seconds = totalSeconds % SECONDS_OF_MINUTE;
    long minutes = totalSeconds / SECONDS_OF_MINUTE;
    formatBuilder.setLength(0);
    return formatter.format("%02d:%02d", minutes, seconds).toString();
  }

  /**
   * get random number
   * 
   * @return a random number string
   */
  public static String getRandomNumber() {
    Random random = new Random();
    return String.valueOf(random.nextInt());
  }

  private static class ThreadLocalDecimalFormat extends ThreadLocal<DecimalFormat> {

    @Override
    protected DecimalFormat initialValue() {
      return new DecimalFormat("0.0");
    }
  }

  /**
   * Make up some items of a string-array into a string seperated with seperator mark.
   * 
   * @param values
   * @param max pass 0 or less to return all values
   * @return
   */
  public static String generateArrayInfo(Context context, List<String> values, int max) {
    if (max <= 0 || max > values.size()) {
      max = values.size();
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < max; i++) {
      sb.append(values.get(i));
      if (i != (max - 1)) {
        sb.append(context.getString(R.string.seperator_mark));
      } else if (max < values.size()) {
        sb.append(context.getString(R.string.and_so_on));
      }
    }
    return sb.toString();
  }

  public static CharSequence fromHtml(String str) {
    if (TextUtils.isEmpty(str)) {
      return str;
    }
    return Html.fromHtml(str);
  }
}
