package com.wandoujia.base.dexs;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.wandoujia.base.reflect.JavaCalls;
import com.wandoujia.base.utils.ArrayUtil;
import dalvik.system.DexFile;

/**
 * A helper to combine extra sources to the target context.
 * For api below 14, we use: {@link #injectBelowV14(android.content.Context, String[])} ,
 * for api 14 and above, we use: {@link #injectV14(android.content.Context, String[])}.
 * 
 * @author zhulantian@wandoujia.com (Lantian Zhu)
 */
@SuppressLint("NewApi")
public class ContextClassLoaderUtility {

  private static final String LOG_TAG = "ContextClassLoaderUtility";

  private ContextClassLoaderUtility() {
    // for utility
  }

  /**
   * Inject dex/apk/jar/zip/so to the context's classloader,
   * after this inject, you application will can use all the
   * classes in your paths.
   * </p>
   * Make sure the filename is endwith 'dex/apk/jar/zip/so',
   * and apk/jar/zip/so should contains a 'classes.dex' in it's
   * content, just like the apk file.
   * </p>
   * You can call this method many time, for example, at
   * Application#attachBaseContext you can inject some must-have dexs,
   * and add qr.apk when user click qr button.
   * 
   * @param context the context you want to inject.
   * @param paths source's paths.
   */
  public static void inject(Context context, Set<String> paths) {
    if (paths == null || paths.isEmpty()) {
      return;
    }
    Log.v(LOG_TAG, "doing inject, paths:" + paths);
    String[] pathArray = paths.toArray(new String[paths.size()]);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      injectV14(context, pathArray);
    } else {
      injectBelowV14(context, pathArray);
    }
  }

  private static void injectV14(Context context, String[] paths) {
    // create new dex elements
    File optDir = getOptDir(context);
    Object[] newDexElements = createEmptyDexElements(paths.length);
    for (int i = 0; i < paths.length; ++i) {
      resetValueV14(new File(paths[i]), optDir, newDexElements[i]);
    }
    // get host's dex elememts
    ClassLoader hostLoader = context.getClassLoader();
    Object[] hostDexElements = getDexElements(hostLoader);
    // combine elements
    setDexElements(hostLoader, ArrayUtil.combineArray(hostDexElements, newDexElements));
  }

  private static void injectBelowV14(Context context, String[] paths) {
    // create and get new classloader values
    File optDir = getOptDir(context);
    Object[] mPathNew = paths;
    Object[] mFileNew = new File[paths.length];
    Object[] mZipsNew = new ZipFile[paths.length];
    Object[] mDexsNew = new DexFile[paths.length];
    for (int i = 0; i < paths.length; ++i) {
      resetValueBelowV14(new File(paths[i]), optDir, mFileNew, mZipsNew, mDexsNew, i);
    }
    // create and get host classloader values
    ClassLoader hostLoader = context.getClassLoader();
    Object[] mFilesHost = JavaCalls.getField(hostLoader, "mFiles");
    Object[] mPathHost = JavaCalls.getField(hostLoader, "mPaths");
    Object[] mZipsHost = JavaCalls.getField(hostLoader, "mZips");
    Object[] mDexsHost = JavaCalls.getField(hostLoader, "mDexs");
    // combine values
    JavaCalls.setField(hostLoader, "mPaths", ArrayUtil.combineArray(mPathHost, mPathNew));
    JavaCalls.setField(hostLoader, "mFiles", ArrayUtil.combineArray(mFilesHost, mFileNew));
    JavaCalls.setField(hostLoader, "mZips", ArrayUtil.combineArray(mZipsHost, mZipsNew));
    JavaCalls.setField(hostLoader, "mDexs", ArrayUtil.combineArray(mDexsHost, mDexsNew));
  }

  private static void resetValueV14(File file, File optDir, Object target) {
    Element element = createElement(file, optDir);
    if (element != null) {
      JavaCalls.setField(target, "file", element.file);
      JavaCalls.setField(target, "zipFile", element.zip);
      JavaCalls.setField(target, "dexFile", element.dex);
    }
  }

  private static void resetValueBelowV14(File file, File optDir,
      Object[] mFiles, Object[] mZips, Object[] mDexs, int pos) {
    Element element = createElement(file, optDir);
    if (element != null) {
      if (pos < mFiles.length) {
        mFiles[pos] = element.file;
      }
      if (pos < mZips.length) {
        mZips[pos] = element.zip;
      }
      if (pos < mDexs.length) {
        mDexs[pos] = element.dex;
      }
    }
  }

  private static Element createElement(File file, File optDir) {
    ZipFile zip = null;
    DexFile dex = null;
    String name = file.getName();

    if (name.endsWith(".dex")) {
      // Raw dex file (not inside a zip/jar).
      dex = createDexFile(file, optDir);
    } else if (name.endsWith(".apk") || name.endsWith(".jar")
        // here we add .so, others is supportted by android sdk
        // so file should contains a 'classes.dex' file inside.
        || name.endsWith(".zip") || name.endsWith(".so")) {
      try {
        zip = new ZipFile(file);
      } catch (IOException ex) {
        /*
         * Note: ZipException (a subclass of IOException)
         * might get thrown by the ZipFile constructor
         * (e.g. if the file isn't actually a zip/jar
         * file).
         */
        ex.printStackTrace();
      }

      dex = createDexFile(file, optDir);
    } else {
      throw new IllegalArgumentException("Unknown file type for: " + file);
    }
    return new Element(file, zip, dex);
  }

  private static DexFile createDexFile(File file, File optDir) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      return JavaCalls.callStaticMethod("dalvik.system.DexPathList", "loadDexFile", file, optDir);
    }
    String sourcePath = file.getAbsolutePath();
    String outputName = JavaCalls.callStaticMethod(
        "dalvik.system.DexClassLoader", "generateOutputName",
        sourcePath, optDir.getAbsolutePath());
    try {
      return DexFile.loadDex(sourcePath, outputName, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static Object[] createEmptyDexElements(int len) {
    Class<?> dexElementClass;
    try {
      dexElementClass = Class.forName("dalvik.system.DexPathList$Element");
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Can't find class: dalvik.system.DexPathList$Element");
    }
    Object[] results = new Object[len];
    for (int i = 0; i < len; ++i) {
      results[i] = JavaCalls.newEmptyInstance(dexElementClass);
    }
    return results;
  }

  private static Object[] getDexElements(ClassLoader loader) {
    Object pathList = JavaCalls.getField(loader, "pathList");
    return JavaCalls.getField(pathList, "dexElements");
  }

  private static void setDexElements(ClassLoader loader, Object[] elements) {
    Object pathList = JavaCalls.getField(loader, "pathList");
    JavaCalls.setField(pathList, "dexElements", elements);
  }

  private static File getOptDir(Context context) {
    File optDir = new File(context.getFilesDir(), "opt");
    if (!optDir.exists()) { // target not exist
      if (optDir.mkdirs()) {
        return optDir;
      }
    } else if (optDir.isFile()) { // target is not folder
      if (optDir.delete() && optDir.mkdirs()) {
        return optDir;
      }
    } else if (optDir.isDirectory()) {
      return optDir;
    }
    throw new IllegalArgumentException("create opt dir meet ex.");
  }

  private static class Element {

    public final File file;
    public final ZipFile zip;
    public final DexFile dex;

    private Element(File file, ZipFile zip, DexFile dex) {
      this.file = file;
      this.zip = zip;
      this.dex = dex;
    }
  }
}
