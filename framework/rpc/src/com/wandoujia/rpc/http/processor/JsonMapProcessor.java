package com.wandoujia.rpc.http.processor;

import java.util.Map;

import com.wandoujia.gson.Gson;
import com.wandoujia.gson.JsonSyntaxException;
import com.wandoujia.gson.reflect.TypeToken;
import com.wandoujia.rpc.http.exception.ContentParseException;

public class JsonMapProcessor<T, U> implements Processor<String, Map<T, U>, ContentParseException> {
  private Gson gson;
  private final TypeToken<Map<T, U>> typeToken;

  /**
   * Constructor.
   *
   * <p>Subclass must pass in a concrete TypeToken, because TypeToken doesn't support generic List.
   * </p>
   *
   * @param gson {@link Gson}
   * @param typeToken {@link TypeToken}
   */
  public JsonMapProcessor(Gson gson, TypeToken<Map<T, U>> typeToken) {
    this.gson = gson;
    this.typeToken = typeToken;
  }

  @Override
  public Map<T, U> process(String input) throws ContentParseException {
    try {
      return gson.fromJson(input, typeToken.getType());
    } catch (JsonSyntaxException e) {
      throw new ContentParseException(e.getMessage(), input);
    }
  }

}
