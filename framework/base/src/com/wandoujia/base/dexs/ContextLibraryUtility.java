package com.wandoujia.base.dexs;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.os.Build;

import com.wandoujia.base.reflect.JavaCalls;
import com.wandoujia.base.utils.ArrayUtil;

/**
 * Inject for native library, call {@link #inject(android.content.Context, java.io.File)}.
 * 
 * @author zhulantian@wandoujia.com (Lantian Zhu)
 */
public class ContextLibraryUtility {

  private ContextLibraryUtility() {
    // for utility class
  }

  public static void inject(Context context, File libDir) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      injectV14(context, libDir);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      injectV9(context, libDir);
    } else {
      injectBelowV9(context, libDir);
    }
  }

  private static void injectV14(Context context, File libDir) {
    ClassLoader loader = context.getClassLoader();
    Object pathList = JavaCalls.getField(loader, "pathList");
    File[] oldNativeLibs = JavaCalls.getField(pathList, "nativeLibraryDirectories");
    File[] resultNativeLibs = oldNativeLibs.length >= 1
        ? ArrayUtil.insert(oldNativeLibs, 1, libDir) : new File[] {libDir};
    JavaCalls.setField(pathList, "nativeLibraryDirectories", resultNativeLibs);
  }

  private static void injectV9(Context context, File libDir) {
    ClassLoader loader = context.getClassLoader();
    List<String> oldNativeLibs = JavaCalls.getField(loader, "libraryPathElements");
    // make sure add '/'
    String libDirPath = libDir.getAbsolutePath() + "/";
    if (oldNativeLibs.size() >= 1) {
      oldNativeLibs.add(1, libDirPath);
    } else {
      oldNativeLibs.add(libDirPath);
    }
  }

  private static void injectBelowV9(Context context, File libDir) {
    ClassLoader loader = context.getClassLoader();
    String[] oldNativeLibs = JavaCalls.getField(loader, "mLibPaths");
    // make sure add '/'
    String libDirPath = libDir.getAbsolutePath() + "/";
    String[] resultNativeLibs = oldNativeLibs.length >= 1
        ? ArrayUtil.insert(oldNativeLibs, 1, libDirPath) : new String[] {libDirPath};
    JavaCalls.setField(loader, "mLibPaths", resultNativeLibs);
  }
}
