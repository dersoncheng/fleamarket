package com.wandoujia.rpc.http.processor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.wandoujia.rpc.http.exception.ContentParseException;

import java.util.List;

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
