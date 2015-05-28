// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: ./log_transition.proto
package com.wandoujia.logv3.model.packages;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoField;

import static com.squareup.wire.Message.Datatype.INT32;
import static com.squareup.wire.Message.Datatype.STRING;

public final class SectionCardPackage extends Message {

  public static final String DEFAULT_SECTION_ID = "";
  public static final String DEFAULT_SECTION_TITLE = "";
  public static final Integer DEFAULT_SECTION_INDEX = 0;
  public static final String DEFAULT_SECTION_CARD_ID = "";
  public static final String DEFAULT_SECTION_EXTRA = "";

  /**
   * section 卡片的 id
   */
  @ProtoField(tag = 1, type = STRING)
  public final String section_id;

  /**
   * section 卡片的标题
   */
  @ProtoField(tag = 2, type = STRING)
  public final String section_title;

  /**
   * section 卡片在卡片上的排序位置
   */
  @ProtoField(tag = 3, type = INT32)
  public final Integer section_index;

  /**
   * section card 的id
   */
  @ProtoField(tag = 4, type = STRING)
  public final String section_card_id;

  /**
   * 程序用
   */
  @ProtoField(tag = 5, type = STRING)
  public final String section_extra;

  private SectionCardPackage(Builder builder) {
    super(builder);
    this.section_id = builder.section_id;
    this.section_title = builder.section_title;
    this.section_index = builder.section_index;
    this.section_card_id = builder.section_card_id;
    this.section_extra = builder.section_extra;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof SectionCardPackage)) return false;
    SectionCardPackage o = (SectionCardPackage) other;
    return equals(section_id, o.section_id)
        && equals(section_title, o.section_title)
        && equals(section_index, o.section_index)
        && equals(section_card_id, o.section_card_id)
        && equals(section_extra, o.section_extra);
  }

  @Override
  public int hashCode() {
    int result = hashCode;
    if (result == 0) {
      result = section_id != null ? section_id.hashCode() : 0;
      result = result * 37 + (section_title != null ? section_title.hashCode() : 0);
      result = result * 37 + (section_index != null ? section_index.hashCode() : 0);
      result = result * 37 + (section_card_id != null ? section_card_id.hashCode() : 0);
      result = result * 37 + (section_extra != null ? section_extra.hashCode() : 0);
      hashCode = result;
    }
    return result;
  }

  public static final class Builder extends Message.Builder<SectionCardPackage> {

    public String section_id;
    public String section_title;
    public Integer section_index;
    public String section_card_id;
    public String section_extra;

    public Builder() {
    }

    public Builder(SectionCardPackage message) {
      super(message);
      if (message == null) return;
      this.section_id = message.section_id;
      this.section_title = message.section_title;
      this.section_index = message.section_index;
      this.section_card_id = message.section_card_id;
      this.section_extra = message.section_extra;
    }

    /**
     * section 卡片的 id
     */
    public Builder section_id(String section_id) {
      this.section_id = section_id;
      return this;
    }

    /**
     * section 卡片的标题
     */
    public Builder section_title(String section_title) {
      this.section_title = section_title;
      return this;
    }

    /**
     * section 卡片在卡片上的排序位置
     */
    public Builder section_index(Integer section_index) {
      this.section_index = section_index;
      return this;
    }

    /**
     * section card 的id
     */
    public Builder section_card_id(String section_card_id) {
      this.section_card_id = section_card_id;
      return this;
    }

    /**
     * 程序用
     */
    public Builder section_extra(String section_extra) {
      this.section_extra = section_extra;
      return this;
    }

    @Override
    public SectionCardPackage build() {
      return new SectionCardPackage(this);
    }
  }
}