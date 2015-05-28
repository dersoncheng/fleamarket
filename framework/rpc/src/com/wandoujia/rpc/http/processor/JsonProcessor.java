package com.wandoujia.rpc.http.processor;

import java.lang.reflect.ParameterizedType;

import com.wandoujia.gson.Gson;
import com.wandoujia.gson.JsonSyntaxException;
import com.wandoujia.rpc.http.exception.ContentParseException;

/**
 * Json parser to parse string to java object.
 * 
 * <p>
 * Usage: define a processor which extends this base class, and the generic type T can be inferred
 * automatically.
 * 
 * <pre>
 * public class AppSearchProcessor extends JsonProcessor&lt;AppSearchResult&gt; {}
 * </pre>
 * 
 * </p>
 * 
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 * 
 * @param <T> java object type
 */
public class JsonProcessor<T> implements Processor<String, T, ContentParseException> {
  protected final Gson gson;

  public JsonProcessor(Gson gson) {
    this.gson = gson;
  }

  @Override
  public T process(String input) throws ContentParseException {
    @SuppressWarnings("unchecked")
    Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
    try {
      return gson.fromJson(input, entityClass);
    } catch (JsonSyntaxException e) {
      throw new ContentParseException(e.getMessage(), input);
    }
  }

}
