package com.wandoujia.base.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class JsonSerializer {

  /**
   * Serialize an object to json string.
   *
   * @param obj
   * @return
   * @throws IOException
   */
  public static String toJson(Object obj) {
    return toJsonObject(obj).toString();
  }

  public static String toJson(Collection list) {
    return toJsonArray(list).toString();
  }

  private static Field[] getAllFields(Class<?> clazz) {
    if (clazz == null) {
      return new Field[0];
    }
    List<Field> fieldList = new ArrayList<Field>();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      fieldList.add(field);
    }
    Field[] superFields = getAllFields(clazz.getSuperclass());
    for (Field field : superFields) {
      fieldList.add(field);
    }
    return fieldList.toArray(new Field[0]);
  }

  @SuppressWarnings("rawtypes")
  public static JSONObject toJsonObject(Object obj) {
    JSONObject json = new JSONObject();
    Field[] fields = getAllFields(obj.getClass());

    for (Field field : fields) {
      // Discard static fields
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      field.setAccessible(true);
      try {
        String name = field.getName();
        // Discard parent class reference, support dalvik & art.
        if (name.equals("this$0") || name.startsWith("shadow$_")) {
          continue;
        }
        Class type = field.getType();
        if (isSimpleClassType(type)) {
          Object value = field.get(obj);
          if (value != null) {
            json.put(name, field.get(obj));
          }
        } else if (type == boolean.class) {
          json.put(name, field.getBoolean(obj));
        } else if (type == int.class) {
          json.put(name, field.getInt(obj));
        } else if (type == long.class) {
          json.put(name, field.getLong(obj));
        } else if (type == double.class) {
          json.put(name, field.getDouble(obj));
        } else if (type == byte.class) {
          json.put(name, field.getByte(obj));
        } else if (type == short.class) {
          json.put(name, field.getShort(obj));
        } else if (type == float.class) {
          json.put(name, field.getFloat(obj));
        } else if (type == char.class) {
          json.put(name, field.getChar(obj));
        } else if (Collection.class.isAssignableFrom(type)) {
          Object value = field.get(obj);
          if (value != null) {
            json.put(name, toJsonArray((Collection) value));
          }
        } else if (!type.isArray()) {
          Object value = field.get(obj);
          if (value != null) {
            json.put(name, toJsonObject(value));
          }
        }// TODO support array int[] object[]
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (JSONException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return json;
  }

  @SuppressWarnings("rawtypes")
  public static JSONArray toJsonArray(Collection list) {
    JSONArray array = new JSONArray();
    Iterator iter = list.iterator();

    while (iter.hasNext()) {
      Object obj = iter.next();
      Class clazz = obj.getClass();

      if (isSimpleClassType(clazz)) {
        array.put(obj);
      } else if (Collection.class.isAssignableFrom(clazz)) {
        Collection childList = (Collection) obj;
        array.put(toJsonArray(childList));
      } else if (!clazz.isArray()) {
        array.put(toJsonObject(obj));
      }
    }
    return array;
  }

  @SuppressWarnings({"rawtypes"})
  public static Object fromJson(String json, Class valueType) {
    Object obj = null;
    try {
      JSONObject jsonObj = new JSONObject(json);
      obj = fromJsonObject(jsonObj, valueType);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return obj;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static Object fromJsonObject(JSONObject jsonObj, Class valueType) {
    Object obj = null;
    try {
      Field[] fields = valueType.getDeclaredFields();

      obj = valueType.newInstance();
      for (Field field : fields) {
        String name = field.getName();
        Class type = field.getType();

        field.setAccessible(true);
        if (jsonObj.has(name) && !jsonObj.isNull(name)) {
          if (TextUtils.isEmpty(jsonObj.getString(name))) {
            continue;
          }
          if (isSimpleClassType(type)) {
            field.set(obj, jsonObj.get(name));
          } else if (type == Boolean.class) {
            if (TextUtils.isEmpty(jsonObj.getString(name))) {
              continue;
            } else {
              field.set(obj, jsonObj.getBoolean(name));
            }
          } else if (type == Integer.class) {
            field.set(obj, Integer.valueOf(jsonObj.getInt(name)));
          } else if (type == Long.class) {
            field.set(obj, Long.valueOf(jsonObj.getLong(name)));
          } else if (type == boolean.class) {
            field.setBoolean(obj, jsonObj.getBoolean(name));
          } else if (type == int.class) {
            field.setInt(obj, jsonObj.getInt(name));
          } else if (type == long.class) {
            field.setLong(obj, jsonObj.getLong(name));
          } else if (type == double.class) {
            field.setDouble(obj, jsonObj.getDouble(name));
          } else if (Collection.class.isAssignableFrom(type)) {
            Type genericType = field.getGenericType();
            if (genericType == null) {
              continue;
            }
            if (genericType instanceof ParameterizedType) {
              ParameterizedType parameterizedType = (ParameterizedType) genericType;
              Class genericClazz = (Class) parameterizedType.getActualTypeArguments()[0];
              if (genericClazz == String.class) {
                JSONArray jsonArray = jsonObj.getJSONArray(name);
                List<String> strs = new ArrayList<String>();
                for (int i = 0; i < jsonArray.length(); i++) {
                  strs.add(jsonArray.getString(i));
                }
                field.set(obj, strs);
              } else {
                JSONArray jsonArray = jsonObj.getJSONArray(name);
                if (List.class.isAssignableFrom(type)) {
                  List list = new ArrayList();
                  for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(fromJsonObject(jsonArray.getJSONObject(i), genericClazz));
                  }
                  field.set(obj, list);
                }

              }
            }
          } else if (!type.isArray()) {
            field.set(obj, fromJsonObject(jsonObj.getJSONObject(name), type));
          }
        }
      }
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return obj;
  }

  @SuppressWarnings("rawtypes")
  private static boolean isSimpleClassType(Class clazz) {
    if (clazz == String.class || clazz == Double.class) {
      return true;
    } else {
      return false;
    }
  }

}
