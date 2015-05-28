package com.wandoujia.log;

import java.io.OutputStream;

/**
 * Implements this to store the muce log event.
 * 
 * @author xubin@wandoujia.com
 */
interface LogStorage {
  /**
   * Append the LogEventModel to storage.
   * 
   * @param logEvent the model of the log event.
   */
  void addEvent(LogEventModel logEvent);

  /**
   * Output ALL log events in the storage to specific output stream,
   * storage should temporarily save the output content and wait the calling of
   * {@link LogStorage#deleteOutput(long)} or {@link LogStorage#restoreOutput(long)}.
   * 
   * @param outputStream the output stream to output to.
   * @return the id of this output, used as parameter of deleteOutput
   */
  long output(OutputStream outputStream);

  /**
   * The output is send to server successfully, so the storage should delete the output content
   * of the specific output id here.
   * 
   * @param outputId the id of the success output.
   */
  void deleteOutput(long outputId);

  /**
   * The output is failed to send to server, so the storage should restore the output to send
   * again later.
   * 
   * @param outputId the id of the failed output.
   */
  void restoreOutput(long outputId);
}
