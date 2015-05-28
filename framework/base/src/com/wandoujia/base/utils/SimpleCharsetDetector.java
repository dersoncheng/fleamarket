package com.wandoujia.base.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class SimpleCharsetDetector {

  public static final String GBK = "GBK";
  public static final String UTF_8 = "UTF-8";
  private static final int MAX_BYTES = 400;

  public static String detectBytes(byte[] bytes, String defaultCharset) {
    if (bytes == null) {
      return defaultCharset;
    }
    return detectInputStream(new ByteArrayInputStream(bytes), bytes.length);
  }

  private static String detectInputStream(InputStream input, int detectByteCount) {
    boolean isUtf8 = true;
    try {
      int readed = 0;
      int max = detectByteCount;
      int chr;

      /**
       * UTF-8 shoud be this format:
       * 1 byte： 0xxxxxxx
       * 2 bytes：110xxxxx 10xxxxxx
       * 3 bytes：1110xxxx 10xxxxxx 10xxxxxx
       */

      while ((chr = input.read()) != -1) {
        readed++;

        // Out of detect count
        if (readed > max) {
          break;
        }

        // [1111 xxxx] More than 3 bytes, not UTF-8
        if (chr >= 0xF0) {
          isUtf8 = false;
          break;
        }

        // [10xx xxxx] Shoud be 0xxx xxxx if single byte
        if (0x80 <= chr && chr <= 0xBF) {
          isUtf8 = false;
          break;
        }

        // [110x xxxx] This is double bytes, detect next byte
        if (0xC0 <= chr && chr <= 0xDF) {
          chr = input.read();
          // [10xx xxxx] The second byte shoud be this
          if (0x80 <= chr && chr <= 0xBF) {
            continue;
          } else {
            isUtf8 = false;
            break;
          }
        }

        // [1110 xxxx] This is triple bytes, detect next two bytes
        if (0xE0 <= chr && chr <= 0xEF) {
          chr = input.read();
          // [10xx xxxx] The second byte shoud be this
          if (0x80 <= chr && chr <= 0xBF) {
            chr = input.read();
            // [10xx xxxx] The third byte shoud be this
            if (0x80 <= chr && chr <= 0xBF) {
              continue;
            } else {
              isUtf8 = false;
              break;
            }
          } else {
            isUtf8 = false;
            break;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return isUtf8 ? UTF_8 : GBK;
  }

  public static void trySkipBomHeader(InputStream inputStream) {
    try {
      if (inputStream.available() >= 3 && inputStream.markSupported()) {
        byte[] first3Bytes = new byte[3];
        inputStream.mark(3);
        inputStream.read(first3Bytes);
        if (first3Bytes[0] != (byte) 0xEF || first3Bytes[1] != (byte) 0xBB ||
            first3Bytes[2] != (byte) 0xBF) {
          inputStream.reset();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Charset detectFile(File file, String defaultCharset) {
    // String charset = "GBK";
    String charset = defaultCharset;
    if (file == null) {
      return Charset.forName(defaultCharset);
    }
    try {
      BufferedInputStream bis = new BufferedInputStream(
          new FileInputStream(file));
      byte[] first3Bytes = new byte[3];

      bis.mark(first3Bytes.length);
      int read = bis.read(first3Bytes, 0, first3Bytes.length);
      if (read != -1) {
        if (first3Bytes[0] == (byte) 0xFF
            && first3Bytes[1] == (byte) 0xFE) {
          charset = "UTF-16LE";
        } else if (first3Bytes[0] == (byte) 0xFE
            && first3Bytes[1] == (byte) 0xFF) {
          charset = "UTF-16BE";
        } else if (first3Bytes[0] == (byte) 0xEF
            && first3Bytes[1] == (byte) 0xBB
            && first3Bytes[2] == (byte) 0xBF) {
          charset = "UTF-8";
        } else {
          bis.reset();
          return Charset.forName(detectInputStream(bis, MAX_BYTES));
        }
      }
      bis.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Charset.forName(charset);
  }
}
