package com.zhangyu.fleamarket.view.button;

import android.view.View;

import com.zhangyu.fleamarket.action.Action;
import com.zhangyu.fleamarket.app.FleaMarketApplication;


/**
 * @author match@wandoujia.com (Diao Liu)
 */
public class ButtonState {
  private int stateAttr;
  private CharSequence text;
  private Action action;
  private boolean enable;
  private int visible;
  private int imageResour;

  /**
   * Constructor of ButtonState.
   *
   * @param stateAttr state attr defined in attrs.xml.
   * @param text      text of button.
   * @param action    action performed by click.
   * @param enable    whether enable the button.
   * @param visible   whether to show the button
   */
  public ButtonState(int stateAttr, CharSequence text, Action action, boolean enable, int visible) {
    this.stateAttr = stateAttr;
    this.text = text;
    this.action = action;
    this.enable = enable;
    this.visible = visible;
  }

  /**
   * Constructor of ButtonState.
   *
   * @param stateAttr state attr defined in attrs.xml.
   * @param textResId      text of button.
   * @param imageResour  state img
   * @param action    action performed by click.
   */
  public ButtonState(int stateAttr, int textResId, int imageResour, Action action) {
    this.stateAttr = stateAttr;
    this.text = FleaMarketApplication.getAppContext().getString(textResId);
    this.imageResour = imageResour;
    this.action = action;
    this.enable = true;
    this.visible = View.VISIBLE;
  }


  /**
   * Constructor of ButtonState.
   * <p>
   * Default enable the button.
   * </p>
   *
   * @param stateAttr state attr defined in attrs.xml.
   * @param text      text of button.
   * @param action    action performed by click.
   */
  public ButtonState(int stateAttr, CharSequence text, Action action) {
    this(stateAttr, text, action, true, View.VISIBLE);
  }

  /**
   * Constructor of ButtonState.
   * <p>
   * Default enable the button.
   * </p>
   *
   * @param stateAttr state attr defined in attrs.xml.
   * @param textResId resource id of the text.
   * @param action    action performed by click.
   */
  public ButtonState(int stateAttr, int textResId, Action action, boolean enable) {
    this(stateAttr, FleaMarketApplication.getAppContext().getString(textResId), action, enable,
        View.VISIBLE);
  }

  /**
   * Constructor of ButtonState.
   * <p>
   * Default enable the button.
   * </p>
   *
   * @param stateAttr state attr defined in attrs.xml.
   * @param textResId resource id of the text.
   * @param action    action performed by click.
   * @param visible   whether to show the button
   */
  public ButtonState(int stateAttr, int textResId, Action action, boolean enable, int visible) {
    this(stateAttr, FleaMarketApplication.getAppContext().getString(textResId), action, enable,
        visible);
  }

  /**
   * Constructor of ButtonState.
   * <p>
   * Default enable the button.
   * </p>
   *
   * @param stateAttr state attr defined in attrs.xml.
   * @param textResId resource id of the text.
   * @param action    action performed by click.
   */
  public ButtonState(int stateAttr, int textResId, Action action) {
    this(stateAttr, FleaMarketApplication.getAppContext().getString(textResId), action, true,
        View.VISIBLE);
  }

  public int getStateAttr() {
    return stateAttr;
  }

  public CharSequence getText() {
    return text;
  }

  public Action getAction() {
    return action;
  }

  public boolean isEnable() {
    return enable;
  }

  public int getVisible() {
    return visible;
  }

  public int getImageResour() {
    return imageResour;
  }
}
