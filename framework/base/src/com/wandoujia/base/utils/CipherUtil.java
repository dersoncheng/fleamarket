package com.wandoujia.base.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import com.wandoujia.base.utils.LibraryLoaderHelper;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtil {

  private static native byte[] getAESKeyNative(Context context);

  public static byte[] getAESKey(Context context) {
    LibraryLoaderHelper.loadLibrarySafety(context, "cipher");
    byte[] ret = null;
    try {
      ret = getAESKeyNative(context);
    } catch (UnsatisfiedLinkError error) {
      ret = null;
    }
    return ret;
  }

  private static native String getAuthKeyNative(Context context);

  public static String getAuthKey(Context context) {
    LibraryLoaderHelper.loadLibrarySafety(context, "cipher");
    return getAuthKeyNative(context);
  }

  private static native String getAndroidIdNative(Context context);

  public static String getAndroidId(Context context) {
    LibraryLoaderHelper.loadLibrarySafety(context, "cipher");
    return getAndroidIdNative(context);
  }

  public static byte[] encrypt(String sSrc, byte[] raw)
          throws GeneralSecurityException {
    return encrypt(sSrc.getBytes(), raw);
  }

  public static byte[] encrypt(byte[] sSrc, byte[] raw)
          throws GeneralSecurityException {
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    IvParameterSpec iv = new IvParameterSpec(ivBytes);
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
    byte[] encrypted = cipher.doFinal(sSrc);
    return encrypted;
  }

  public static void encrypt(InputStream input, OutputStream output, byte[] raw)
          throws GeneralSecurityException {
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    IvParameterSpec iv = new IvParameterSpec(ivBytes);
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

    CipherOutputStream cipherOutput = new CipherOutputStream(output, cipher);
    byte[] buffer = new byte[1024];
    try {
      while (true) {
        int readed = input.read(buffer);
        if (readed == -1) {
          break;
        }
        cipherOutput.write(buffer, 0, readed);
      }
      cipherOutput.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void decrypt(InputStream input, OutputStream output, byte[] raw)
          throws GeneralSecurityException {
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    IvParameterSpec iv = new IvParameterSpec(ivBytes);
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

    CipherInputStream cipherInput = new CipherInputStream(input, cipher);
    byte[] buffer = new byte[1024];
    try {
      while (true) {
        int readed = cipherInput.read(buffer);
        if (readed == -1) {
          break;
        }
        output.write(buffer, 0, readed);
      }
      cipherInput.close();
      output.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static byte[] decrypt(byte[] encrypted, byte[] raw)
          throws GeneralSecurityException {
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    IvParameterSpec iv = new IvParameterSpec(ivBytes);
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
    byte[] decrypted = cipher.doFinal(encrypted);
    return decrypted;
  }
}
