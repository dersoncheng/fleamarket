package com.wandoujia.base.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.util.Log;

/**
 * @author liuyx
 */
public class ZipUtil {

  public static void unZipFolder(String zipFileString, String outPathString)
      throws IOException {
    Log.v("ZipUtils", "unZipFolder(String, String)");

    ZipInputStream inZip = new ZipInputStream(new FileInputStream(
        zipFileString));
    ZipEntry zipEntry;
    String entryName = "";

    String outPath = outPathString;
    if (!outPath.endsWith(File.separator)) {
      outPath += File.separator;
    }

    while ((zipEntry = inZip.getNextEntry()) != null) {
      entryName = zipEntry.getName();

      if (zipEntry.isDirectory()) {
        entryName = entryName.substring(0, entryName.length() - 1);

        File folder = new File(outPath + entryName);
        folder.mkdirs();
      } else {
        File file = new File(outPath + entryName);

        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
          parentFile.mkdirs();
        }

        file.createNewFile();

        FileOutputStream out = new FileOutputStream(file);
        int len;
        byte[] buffer = new byte[1024];
        while ((len = inZip.read(buffer)) != -1) {
          out.write(buffer, 0, len);
          out.flush();
        }
        out.close();
      }
    }

    inZip.close();
  }

  public static InputStream getZipInput(String zipFileName, String entryName)
      throws IOException {
    ZipFile zipFile = new ZipFile(zipFileName);
    ZipEntry zipEntry = zipFile.getEntry(entryName);
    return zipFile.getInputStream(zipEntry);
  }

  public static void zipFolder(String srcFileString, String zipFileString,
      boolean includeFolder) throws FileNotFoundException, IOException {
    Log.v("ZipUtils", "zipFolder(String, String)");

    ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(
        zipFileString));
    outZip.setMethod(ZipOutputStream.DEFLATED);

    File file = new File(srcFileString);
    if (!file.isDirectory()) {
      return;
    }

    String dirPath = srcFileString;
    if (!srcFileString.endsWith(File.separator)) {
      dirPath = srcFileString + File.separator;
    }

    if (includeFolder) {
      zipFiles(file.getParent() + File.separator, file.getName(), outZip);
    } else {
      zipFiles(dirPath, "", outZip);
    }

    outZip.finish();
    outZip.close();

  }

  private static void zipFiles(String folderString, String fileString,
      ZipOutputStream zipOutputSteam) throws FileNotFoundException,
      IOException {
    Log.v("ZipUtils", "zipFiles(String, String, ZipOutputStream)");

    if (zipOutputSteam == null) {
      return;
    }

    File file = new File(folderString + fileString);

    if (file.isFile()) {

      ZipEntry zipEntry = new ZipEntry(fileString);
      FileInputStream inputStream = new FileInputStream(file);
      zipOutputSteam.putNextEntry(zipEntry);

      int len;
      byte[] buffer = new byte[4096];

      while ((len = inputStream.read(buffer)) != -1) {
        zipOutputSteam.write(buffer, 0, len);
      }

      zipOutputSteam.closeEntry();
    } else {

      String fileList[] = file.list();
      String folderStr = fileString.length() > 0 ? fileString
          + File.separator : fileString;

      if (fileList.length <= 0) {
        ZipEntry zipEntry = new ZipEntry(folderStr);
        zipOutputSteam.putNextEntry(zipEntry);
        zipOutputSteam.closeEntry();
      }

      for (int i = 0; i < fileList.length; i++) {
        zipFiles(folderString, folderStr + fileList[i], zipOutputSteam);
      }
    }

  }
}
