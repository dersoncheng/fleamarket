package com.wandoujia.image.rpc;

/**
 * Interface to update progress of a procedure.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public interface ProgressCallback {

  /**
   * Sets progress it changes. Max progress is 100.
   *
   * @param progress
   */
  void onProgressChanged(int progress);
}
