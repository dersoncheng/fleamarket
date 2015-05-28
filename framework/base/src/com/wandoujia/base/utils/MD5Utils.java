package com.wandoujia.base.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;

public class MD5Utils {

  private static String MD5 = "MD5";

  private static final int STREAM_BUFFER_LENGTH = 128 * 1024;
  private static final long MIN_SEGMENT_FILE_LENGTH = STREAM_BUFFER_LENGTH * 3;

  static MessageDigest getDigest(String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Compute the input data's md5 value.
   *
   * @param inputStream
   * @return
   * @throws IOException
   */
  public static String md5Digest(InputStream inputStream) throws IOException {
    MessageDigest digest = getDigest(MD5);
    byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
    int read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);

    while (read > -1) {
      digest.update(buffer, 0, read);
      read = inputStream.read(buffer, 0, STREAM_BUFFER_LENGTH);
    }

    return getHexString(digest.digest());
  }

  public static String md5Digest(InputStream inputStream, byte[] buffer) throws IOException {
    MessageDigest digest = getDigest(MD5);
    int read = inputStream.read(buffer, 0, buffer.length);
    while (read > -1) {
      digest.update(buffer, 0, read);
      read = inputStream.read(buffer, 0, buffer.length);
    }
    return getHexString(digest.digest());
  }

  /**
   * Calculate file md5 with segment.
   * Divide the file to start, middle and end three segments. Then append the three segments and
   * file length to byte array. And calculate the byte array's md5.
   *
   * @param filePath file path.
   * @return The segmental md5.
   */
  public static String calculateFileSegmentMd5(String filePath) {
    RandomAccessFile randomAccessFile = null;
    try {
      byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
      randomAccessFile = new RandomAccessFile(filePath, "r");
      MessageDigest digest = getDigest(MD5);
      int offset = 0;
      // start segment.
      randomAccessFile.seek(offset);
      int read = randomAccessFile.read(buffer, 0, buffer.length);
      if (read > -1) {
        digest.update(buffer, 0, read);
      }
      // middle segment.
      offset = (int) ((randomAccessFile.length() - STREAM_BUFFER_LENGTH) / 2);
      randomAccessFile.seek(offset);
      read = randomAccessFile.read(buffer, 0, buffer.length);
      if (read > -1) {
        digest.update(buffer, 0, read);
      }
      // end segment.
      offset = (int) ((randomAccessFile.length() - STREAM_BUFFER_LENGTH));
      randomAccessFile.seek(offset);
      read = randomAccessFile.read(buffer, 0, buffer.length);
      if (read > -1) {
        digest.update(buffer, 0, read);
      }
      // file size.
      byte[] fileLengthBytes = String.valueOf(randomAccessFile.length()).getBytes();
      digest.update(fileLengthBytes, 0, fileLengthBytes.length);
      return getHexString(digest.digest());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.close(randomAccessFile);
    }
    return null;
  }


  /**
   * Compute the input string's md5 value.
   *
   * @param input
   * @return
   * @throws IOException
   */
  public static String md5Digest(String input) throws IOException {
    if (input == null) {
      return null;
    }
    MessageDigest digest = getDigest(MD5);
    digest.update(input.getBytes());
    return getHexString(digest.digest());
  }

  private static String getHexString(byte[] digest) {
    BigInteger bi = new BigInteger(1, digest);
    return String.format("%032x", bi);
  }

  public static boolean checkMd5(String filePath, String targetMd5, StringBuilder md5Builder) {
    File file = new File(filePath);
    FileInputStream fileInputStream = null;
    BufferedInputStream bufferedInputStream = null;
    try {
      fileInputStream = new FileInputStream(file);
      bufferedInputStream = new BufferedInputStream(fileInputStream);
      String md5 = MD5Utils.md5Digest(bufferedInputStream);
      if (md5 != null && md5.equalsIgnoreCase(targetMd5)) {
        if (md5Builder != null) {
          md5Builder.append(md5);
        }
        return true;
      } else {
        if (md5Builder != null) {
          if (TextUtils.isEmpty(md5)) {
            md5Builder.append("empty");
          } else {
            md5Builder.append(md5);
          }
        }
        return false;
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bufferedInputStream != null) {
        try {
          bufferedInputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return false;
  }
}
