package com.zhangyu.fleamarket.configs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gaoxiong on 2015/5/28.
 */
public class FleaMarketOnlineConfigResult implements Serializable {
  private String searchHit = "";
  private List<String> tab = new ArrayList<String>();

  public String getSearchHit() {
    return searchHit;
  }

  public List<String> getTab() {
    return tab;
  }

  public String getTabString() {
    StringBuilder tabstring = new StringBuilder();
    Iterator<String> iterator = tab.iterator();
    while (iterator.hasNext()) {
      tabstring.append(iterator.next());
      if (iterator.hasNext()) {
        tabstring.append(",");
      }
    }
    return tabstring.toString();
  }
}
