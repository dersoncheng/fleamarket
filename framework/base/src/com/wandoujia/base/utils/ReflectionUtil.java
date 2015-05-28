package com.wandoujia.base.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static Object invokeStaticMethod(Class clazz, String method, Object... params) {
    try {
      Method m = clazz.getMethod(method);
      return m.invoke(clazz, params);
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Object invokeMethod(Object owner, String methodName, Object[] args)
      throws Exception {
    Class ownerClazz = owner.getClass();

    Class[] argsClazz = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      argsClazz[i] = args[i].getClass();
    }

    Method method = ownerClazz.getMethod(methodName, argsClazz);
    return method.invoke(owner, args);
  }

  public static Object invokeMethod(Object owner, String methodName, Class[] clazz, Object[] args)
      throws Exception {
    Class ownerClazz = owner.getClass();
    Method method = ownerClazz.getMethod(methodName, clazz);
    return method.invoke(owner, args);
  }

}
