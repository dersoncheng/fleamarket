package com.zhangyu.fleamarket.model;

/**
 * Converter interface.
 * It is used for type converting.
 * 
 * @param <S> source type
 * @param <T> target type.
 * 
 * @author match@wandoujia.com (Diao Liu)
 */
public interface Converter<S, T> {
  /**
   * Converter S to T
   * 
   * @param s
   * @return t
   */
  T convert(S s);
}
