package com.wandoujia.base.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Utility class to handle I/O operations.
 *
 * @author liuchunyu@wandoujia.com (Chunyu Liu)
 */
public class IOUtils {

  public static final String DEFAULT_ENCODING = "utf-8";

  private static final int BUFFER_SIZE = 8 * 1024;
  private static final int EOF = -1;
  /**
   * The default buffer size to use for the skip() methods.
   */
  private static final int SKIP_BUFFER_SIZE = 2048;
  private static byte[] SKIP_BYTE_BUFFER;

  private IOUtils() {}

  /**
   * Read the Stream content as a string (use utf-8).
   *
   * @param is The stream to read
   * @return The String content
   * @throws IOException IOException
   */
  public static String readString(InputStream is) throws IOException {
    return readString(is, DEFAULT_ENCODING);
  }

  /**
   * Read the Stream content as a string.
   *
   * @param is The stream to read
   * @return The String content
   * @throws IOException IOException
   */
  public static String readString(InputStream is, String encoding) throws IOException {
    StringWriter sw = new StringWriter();
    try {
      copy(is, sw, encoding);
      return sw.toString();
    } finally {
      close(is);
      close(sw);
    }
  }

  /**
   * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
   * <p>
   * This method buffers the input internally, so there is no need to use a
   * <code>BufferedInputStream</code>.
   * <p>
   * Large streams (over 2GB) will return a bytes copied value of <code>-1</code> after the copy has
   * completed since the correct number of bytes cannot be returned as an int. For large streams use
   * the <code>copyLarge(InputStream, OutputStream)</code> method.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @return the number of bytes copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @throws ArithmeticException if the byte count is too large
   * @since Commons IO 1.1
   */
  public static int copy(InputStream input, OutputStream output) throws IOException {
    long count = copyLarge(input, output);
    if (count > Integer.MAX_VALUE) {
      return -1;
    }
    return (int) count;
  }

  /**
   * Copy bytes from a large (over 2GB) <code>InputStream</code> to an <code>OutputStream</code>.
   * <p>
   * This method buffers the input internally, so there is no need to use a
   * <code>BufferedInputStream</code>.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @return the number of bytes copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since Commons IO 1.3
   */
  public static long copyLarge(InputStream input, OutputStream output)
      throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    long count = 0;
    int n;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  /**
   * Copy some or all bytes from a large (over 2GB) <code>InputStream</code> to an
   * <code>OutputStream</code>, optionally skipping input bytes.
   * <p>
   * This method buffers the input internally, so there is no need to use a
   * <code>BufferedInputStream</code>.
   * <p>
   * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @param inputOffset : number of bytes to skip from input before copying
   *          -ve values are ignored
   * @param length : number of bytes to copy. -ve means all
   * @return the number of bytes copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 2.2
   */
  public static long copyLarge(InputStream input, OutputStream output, long inputOffset, long length)
      throws IOException {
    return copyLarge(input, output, inputOffset, length, new byte[BUFFER_SIZE]);
  }

  /**
   * Copy some or all bytes from a large (over 2GB) <code>InputStream</code> to an
   * <code>OutputStream</code>, optionally skipping input bytes.
   * <p>
   * This method uses the provided buffer, so there is no need to use a
   * <code>BufferedInputStream</code>.
   * <p>
   *
   * @param input the <code>InputStream</code> to read from
   * @param output the <code>OutputStream</code> to write to
   * @param inputOffset : number of bytes to skip from input before copying
   *          -ve values are ignored
   * @param length : number of bytes to copy. -ve means all
   * @param buffer the buffer to use for the copy
   *
   * @return the number of bytes copied
   * @throws NullPointerException if the input or output is null
   * @throws IOException if an I/O error occurs
   * @since 2.2
   */
  public static long copyLarge(InputStream input, OutputStream output,
      final long inputOffset, final long length, byte[] buffer) throws IOException {
    if (inputOffset > 0) {
      skipFully(input, inputOffset);
    }
    if (length == 0) {
      return 0;
    }
    final int bufferLength = buffer.length;
    int bytesToRead = bufferLength;
    if (length > 0 && length < bufferLength) {
      bytesToRead = (int) length;
    }
    int read;
    long totalRead = 0;
    while (bytesToRead > 0 && EOF != (read = input.read(buffer, 0, bytesToRead))) {
      output.write(buffer, 0, read);
      totalRead += read;
      if (length > 0) { // only adjust length if not reading to the end
        // Note the cast must work because buffer.length is an integer
        bytesToRead = (int) Math.min(length - totalRead, bufferLength);
      }
    }
    return totalRead;
  }

  /**
   * Skip the requested number of bytes or fail if there are not enough left.
   * <p>
   * This allows for the possibility that {@link InputStream#skip(long)} may not skip as many bytes
   * as requested (most likely because of reaching EOF).
   *
   * @param input stream to skip
   * @param toSkip the number of bytes to skip
   * @see InputStream#skip(long)
   *
   * @throws IOException if there is a problem reading the file
   * @throws IllegalArgumentException if toSkip is negative
   * @throws EOFException if the number of bytes skipped was incorrect
   * @since 2.0
   */
  public static void skipFully(InputStream input, long toSkip) throws IOException {
    if (toSkip < 0) {
      throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
    }
    long skipped = skip(input, toSkip);
    if (skipped != toSkip) {
      throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
    }
  }

  /**
   * Skip bytes from an input byte stream.
   * This implementation guarantees that it will read as many bytes
   * as possible before giving up; this may not always be the case for
   * subclasses of {@link Reader}.
   *
   * @param input byte stream to skip
   * @param toSkip number of bytes to skip.
   * @return number of bytes actually skipped.
   *
   * @see InputStream#skip(long)
   *
   * @throws IOException if there is a problem reading the file
   * @throws IllegalArgumentException if toSkip is negative
   * @since 2.0
   */
  public static long skip(InputStream input, long toSkip) throws IOException {
    if (toSkip < 0) {
      throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
    }
    /*
     * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple
     * times (the data
     * is ignored) - we always use the same size buffer, so if it it is recreated it will still be
     * OK (if the buffer
     * size were variable, we would need to synch. to ensure some other thread did not create a
     * smaller one)
     */
    if (SKIP_BYTE_BUFFER == null) {
      SKIP_BYTE_BUFFER = new byte[SKIP_BUFFER_SIZE];
    }
    long remain = toSkip;
    while (remain > 0) {
      long n = input.read(SKIP_BYTE_BUFFER, 0, (int) Math.min(remain, SKIP_BUFFER_SIZE));
      if (n < 0) { // EOF
        break;
      }
      remain -= n;
    }
    return toSkip - remain;
  }

  /**
   *
   * @param input
   * @param output
   * @param encoding Charset name
   * @throws IOException
   */
  public static void copy(InputStream input, Writer output, String encoding)
      throws IOException {
    InputStreamReader in =
        new InputStreamReader(input, encoding == null ? DEFAULT_ENCODING : encoding);
    char[] buffer = new char[BUFFER_SIZE];
    int n = 0;
    while (-1 != (n = in.read(buffer))) {
      output.write(buffer, 0, n);
    }
  }

  public static void copy(InputStream input, OutputStream output, String encoding)
      throws IOException {
    InputStreamReader in =
        new InputStreamReader(input, encoding == null ? DEFAULT_ENCODING : encoding);
    char[] buffer = new char[BUFFER_SIZE];
    int n = 0;
    while (-1 != (n = in.read(buffer))) {
      byte[] bytes = getBytes(buffer);
      output.write(bytes, 0, bytes.length);
    }
  }

  private static byte[] getBytes(char[] chars) {
    Charset cs = Charset.forName("UTF-8");
    CharBuffer cb = CharBuffer.allocate(chars.length);
    cb.put(chars);
    cb.flip();
    ByteBuffer bb = cs.encode(cb);
    return bb.array();
  }

  /**
   * Read file content to a String (always use utf-8).
   *
   * @param file The file to read
   * @return The String content
   * @throws IOException IOException
   */
  public static String readString(File file) throws IOException {
    return readString(file, DEFAULT_ENCODING);
  }

  /**
   * Read file content to a String.
   *
   * @param file The file to read
   * @return The String content
   * @throws IOException IOException
   */
  public static String readString(File file, String encoding) throws IOException {
    return readString(new FileInputStream(file), encoding);
  }

  /**
   * Read binary content of a file.
   * <p>
   * <b>Warning: Do not use on large file !</b>
   * </p>
   *
   * @param file The file te read
   * @return The binary data
   * @throws IOException IOException
   */
  public static byte[] readBytes(File file) throws IOException {
    InputStream is = null;
    try {
      is = new FileInputStream(file);
      byte[] result = new byte[(int) file.length()];
      is.read(result);
      return result;
    } finally {
      close(is);
    }
  }

  /**
   * Read binary content of a stream.
   * <p>
   * <b>Warning: Do not use on large stream !</b>
   * </p>
   *
   * @param is The stream to read
   * @return The binary data
   * @throws IOException IOException
   */
  public static byte[] readBytes(InputStream is) throws IOException {
    ByteArrayOutputStream baos = null;
    try {
      baos = new ByteArrayOutputStream();
      int read = 0;
      byte[] buffer = new byte[BUFFER_SIZE];
      while ((read = is.read(buffer)) > 0) {
        baos.write(buffer, 0, read);
      }
      return baos.toByteArray();
    } finally {
      close(is);
      close(baos);
    }
  }

  /**
   * Write String content to a stream (always use utf-8).
   *
   * @param content The content to read
   * @param os The stream to write
   * @throws IOException IOException
   */
  public static void writeString(String content, OutputStream os) throws IOException {
    writeString(content, os, DEFAULT_ENCODING);
  }

  /**
   * Write String content to a stream (always use utf-8).
   *
   * @param content The content to read
   * @param os The stream to write
   * @throws IOException IOException
   */
  public static void writeString(String content, OutputStream os, String encoding)
      throws IOException {
    try {
      PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(os, encoding));
      printWriter.write(content);
      printWriter.flush();
      os.flush();
    } finally {
      close(os);
    }
  }

  /**
   * Write stream to another stream.
   *
   * @param is The stream to read
   * @param os The stream to write
   * @throws IOException IOException
   */
  public static void write(InputStream is, OutputStream os) throws IOException {
    write(is, true, os, true);
  }

  /**
   * Write stream to another stream.
   * 
   * @param is The stream to read
   * @param closeInputStream
   * @param os The stream to write
   * @param closeOutputStream
   * @throws IOException IOException
   */
  public static void write(InputStream is, boolean closeInputStream, OutputStream os,
      boolean closeOutputStream) throws IOException {
    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int count;
      while ((count = is.read(buffer)) != -1) {
        os.write(buffer, 0, count);
      }
      os.flush();
    } finally {
      if (closeInputStream) {
        close(is);
      }
      if (closeOutputStream) {
        close(os);
      }
    }
  }

  /**
   * Write stream to a file.
   *
   * @param is The stream to read
   * @param file The file to write
   * @throws IOException
   */
  public static void write(InputStream is, File file) throws IOException {
    OutputStream os = null;
    try {
      os = new FileOutputStream(file);
      byte[] buffer = new byte[BUFFER_SIZE];
      int count;
      while ((count = is.read(buffer)) != -1) {
        os.write(buffer, 0, count);
      }
      os.flush();
    } finally {
      close(is);
      close(os);
    }
  }

  /**
   * Write binay data to a file.
   *
   * @param data The binary data to write
   * @param file The file to write
   * @throws IOException
   */
  public static void write(byte[] data, File file) throws IOException {
    write(data, file, false);
  }

  /**
   * Write binay data to a file.
   *
   * @param data The binary data to write
   * @param file The file to write
   * @throws IOException
   */
  public static void write(byte[] data, File file, boolean append) throws IOException {
    OutputStream os = null;
    try {
      os = new FileOutputStream(file, append);
      os.write(data);
      os.flush();
    } finally {
      close(os);
    }
  }

  /**
   * Close stream.
   *
   * @param is The stream to close
   */
  public static void close(Closeable is) {
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
