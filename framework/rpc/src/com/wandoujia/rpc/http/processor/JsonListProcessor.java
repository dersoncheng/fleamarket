package com.wandoujia.rpc.http.processor;

import java.util.List;

import com.wandoujia.gson.Gson;
import com.wandoujia.gson.JsonSyntaxException;
import com.wandoujia.gson.reflect.TypeToken;
import com.wandoujia.rpc.http.exception.ContentParseException;

/**
 * Json parser to parse string to java object list.
 *
 * @author liuchunyu@wandouaji.com (Chunyu Liu)
 *
 * @param <T> list element type
 */
public class JsonListProcessor<T> implements Processor<String, List<T>, ContentParseException> {

  protected final Gson gson;
  private final TypeToken<List<T>> typeToken;

  /**
   * Constructor.
   *
   * <p>Subclass must pass in a concrete TypeToken, because TypeToken doesn't support generic List.
   * </p>
   *
   * @param gson {@link Gson}
   * @param typeToken {@link TypeToken}
   */
  public JsonListProcessor(Gson gson, TypeToken<List<T>> typeToken) {
    this.gson = gson;
    this.typeToken = typeToken;
  }

  @Override
  public List<T> process(String input) throws ContentParseException {
    try {
      return gson.fromJson(input, typeToken.getType());
    } catch (JsonSyntaxException e) {
      throw new ContentParseException(e.getMessage(), input);
    }
  }

}
