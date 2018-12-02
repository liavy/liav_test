/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.jspruntime;

import com.sap.engine.boot.SystemProperties;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.JspIndexOutOfBoundsException;

import java.io.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * Write text to a character-output stream, buffering characters so as
 * to provide for the efficient writing of single characters, arrays,
 * and strings. Provide support for discarding for the output that has been buffered.
 *
 * @author Maria Jurova, Mladen Markov
 */
public class BodyContentImpl extends BodyContent {
  public static final String lineSeparator = SystemProperties.getProperty("line.separator");
  private static final int K = 1024;
  private static final int DEFAULT_BUFFER_SIZE = 8 * K;
  private int bufferSize = DEFAULT_BUFFER_SIZE;
  private char[] cb = new char[bufferSize];
  private int nextChar = 0;
  private boolean closed;

  // Enclosed writer to which any output is written
  private Writer writer;
  // See comment in setWriter()
  private int bufferSizeSave;

  /**
   * Initiates the instance with a refference to some JspWriter.
   *
   * @param   writer  a refference to JspWriter
   */
  public BodyContentImpl(JspWriter writer) {
    super(writer);
    closed = false;
  }

  /**
   * Writes a single character.
   *
   * @param   c  a char that will be written represented by its integer value
   */
  public void write(int c) throws IOException {
    if (writer != null) {
      writer.write(c);
    } else {
      synchronized (lock) {
        ensureOpen();
        if (nextChar >= bufferSize) {
          reAllocBuff(0);
        }

        cb[nextChar++] = (char) c;
      }
    }
  }

  /**
   * Write a portion of an array of characters.
   *
   * Ordinarily this method stores characters from the given array into
   * this stream's buffer, flushing the buffer to the underlying stream as
   * needed.  If the requested length is at least as large as the buffer,
   * however, then this method will flush the buffer and write the characters
   * directly to the underlying stream.  Thus redundant DiscardableBufferedWriter
   * s will not copy data unnecessarily.
   *
   * @param  cbuf  A character array
   * @param  off   Offset from which to start reading characters
   * @param  len   Number of characters to write
   *
   */
  public void write(char cbuf[], int off, int len) throws IOException {
    if (writer != null) {
      writer.write(cbuf, off, len);
    } else {
      synchronized (lock) {
        ensureOpen();
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
          throw new JspIndexOutOfBoundsException(JspIndexOutOfBoundsException.Incorrect_parameters_in_array_of_charactes_for_writing);
        } else if (len == 0) {
          return;
        }

        if (len >= bufferSize - nextChar) {
          reAllocBuff(len);
        }

        System.arraycopy(cbuf, off, cb, nextChar, len);
        nextChar += len;
      }
    }
  }

  /**
   * Write an array of characters.  This method cannot be inherited from the
   * Writer class because it must suppress I/O exceptions.
   *
   * @param  buf  A character array
   */
  public void write(char buf[]) throws IOException {
    if (writer != null) {
      writer.write(buf);
    } else {
      write(buf, 0, buf.length);
    }
  }

  /**
   * Write a portion of a String.
   *
   * @param  s     String to be written
   * @param  off   Offset from which to start reading characters
   * @param  len   Number of characters to be written
   *
   */
  public void write(String s, int off, int len) throws IOException {
    if (writer != null) {
      writer.write(s, off, len);
    } else {
      synchronized (lock) {
        ensureOpen();
        if (len >= bufferSize - nextChar) {
          reAllocBuff(len);
        }

        s.getChars(off, off + len, cb, nextChar);
        nextChar += len;
      }
    }
  }

  /**
   * Write a string.  This method cannot be inherited from the Writer class
   * because it must suppress I/O exceptions.
   *
   * @param  s     String to be written
   */
  public void write(String s) throws IOException {
    if (writer != null) {
      writer.write(s);
    } else {
      write(s, 0, s.length());
    }
  }

  /**
   * Write a line separator.  The line separator string is defined by the
   * system property line.separator, and is not necessarily a single
   * newline ('\n') character.
   *
   * @exception  IOException  If an I/O error occurs
   */
  public void newLine() throws IOException {
    if (writer != null) {
      writer.write(lineSeparator);
    } else {
      synchronized (lock) {
        write(lineSeparator);
      }
    }
  }

  /**
   * Print a boolean value.  The string produced by java.lang.String valueOf(boolean) is translated into bytes
   * according to the platform's default character encoding, and these bytes
   * are written in exactly the manner of the write(int) method.
   *
   * @param      b   The boolean to be printed
   * @throws	   java.io.IOException
   */
  public void print(boolean b) throws IOException {
    write(b ? "true" : "false");
  }

  /**
   * Print a character.  The character is translated into one or more bytes
   * according to the platform's default character encoding, and these bytes
   * are written in exactly the manner of the write(int) method.
   *
   * @param      c   The char to be printed
   * @throws	   java.io.IOException
   */
  public void print(char c) throws IOException {
    write(String.valueOf(c));
  }

  /**
   * Print an integer.  The string produced by java.lang.String valueOf(int) is translated into bytes according
   * to the platform's default character encoding, and these bytes are
   * written in exactly the manner of the write(int) method.
   *
   * @param      i   The int to be printed
   * @see        java.lang.Integer toString(int)
   * @throws	   java.io.IOException
   */
  public void print(int i) throws IOException {
    write(String.valueOf(i));
  }

  /**
   * Print a long integer.  The string produced by java.lang.String#valueOf(long) is translated into bytes
   * according to the platform's default character encoding, and these bytes
   * are written in exactly the manner of the write(int) method.
   *
   * @param      l   The long to be printed
   * @see        java.lang.Long#toString(long)
   * @throws	   java.io.IOException
   */
  public void print(long l) throws IOException {
    write(String.valueOf(l));
  }

  /**
   * Print a floating-point number.  The string produced by java.lang.String#valueOf(float) is translated into bytes
   * according to the platform's default character encoding, and these bytes
   * are written in exactly the manner of the write(int) method.
   *
   * @param      f   The float to be printed
   * @see        java.lang.Float toString(float)
   * @throws	   java.io.IOException
   */
  public void print(float f) throws IOException {
    write(String.valueOf(f));
  }

  /**
   * Print a double-precision floating-point number. The string produced by java.lang.String#valueOf(double)
   * is translated into bytes according to the platform's default character encoding, and these
   * bytes are written in exactly the manner of the write(int) method.
   *
   * @param      d   The double to be printed
   * @see        java.lang.Double toString(double)
   * @throws	   java.io.IOException
   */
  public void print(double d) throws IOException {
    write(String.valueOf(d));
  }

  /**
   * Print an array of characters.  The characters are converted into bytes
   * according to the platform's default character encoding, and these bytes
   * are written in exactly the manner of the write(int) method.
   *
   * @param      s   The array of chars to be printed
   *
   * @throws  NullPointerException  If s is null
   * @throws	   java.io.IOException
   */
  public void print(char s[]) throws IOException {
    write(s);
  }

  /**
   * Print a string. If the argument is null then the string "null" is printed.
   * Otherwise, the string's characters are converted into bytes according to the platform's default character
   * encoding, and these bytes are written in exactly the manner of the write(int) method.
   *
   * @param      s   The String to be printed
   * @throws	   java.io.IOException
   */
  public void print(String s) throws IOException {
    if (s == null) {
      s = "null";
    }

    write(s);
  }

  /**
   * Print an object.  The string produced by the java.lang.String#valueOf(Object) method is translated into bytes
   * according to the platform's default character encoding, and these bytes
   * are written in exactly the manner of the write(int) method.
   *
   * @param      obj   The Object to be printed
   * @throws	   java.io.IOException
   */
  public void print(Object obj) throws IOException {
    write(String.valueOf(obj));
  }

  /**
   * Terminate the current line by writing the line separator string.  The
   * line separator string is defined by the system property
   * line.separator, and is not necessarily a single newline character '\n'.
   *
   * @throws	   java.io.IOException
   */
  public void println() throws IOException {
    newLine();
  }

  /**
   * Print a boolean value and then terminate the line.  This method behaves
   * as though it invokes print(boolean) and then println().
   *
   * @param      x   The boolean to be printed
   * @throws	   java.io.IOException
   */
  public void println(boolean x) throws IOException {
    synchronized (lock) {
      print(x);
      println();
    }
  }

  /**
   * Print a character and then terminate the line.  This method behaves as
   * though it invokes print(char) and then println().
   *
   * @param      x   The char to be printed
   * @throws	   java.io.IOException
   */
  public void println(char x) throws IOException {
    synchronized (lock) {
      print(x);
      println();
    }
  }

  /**
   * Print an integer and then terminate the line.  This method behaves as
   * though it invokes print(int) and then println().
   *
   * @param      x   The int to be printed
   * @throws	   java.io.IOException
   */
  public void println(int x) throws IOException {
    synchronized (lock) {
      print(x);
      println();
    }
  }

  /**
   * Print a long integer and then terminate the line.  This method behaves
   * as though it invokes print(long) and then println().
   *
   * @param      x   The long to be printed
   * @throws	   java.io.IOException
   */
  public void println(long x) throws IOException {
    synchronized (lock) {
      print(x);
      println();
    }
  }

  /**
   * Print a floating-point number and then terminate the line.  This method
   * behaves as though it invokes print(float) and then println().
   *
   * @param      x   The float to be printed
   * @throws	   java.io.IOException
   */
  public void println(float x) throws IOException {
    synchronized (lock) {
      print(x);
      println();
    }
  }

  /**
   * Print a double-precision floating-point number and then terminate the
   * line.  This method behaves as though it invokes print(double) and then println().
   *
   * @param      x   The double to be printed
   * @throws	   java.io.IOException
   */
  public void println(double x) throws IOException {
    synchronized (lock) {
      print(x);
      println();
    }
  }

  /**
   * Print an array of characters and then terminate the line.  This method
   * behaves as though it invokes print(char[]) and then println().
   *
   * @param      x   The char array to be printed
   * @throws	   java.io.IOException
   */
  public void println(char x[]) throws IOException {
    synchronized (lock) {
      print(x);
      println();
    }
  }

  /**
   * Print a String and then terminate the line.  This method behaves as
   * though it invokes  print(String) and then println().
   *
   * @param      x   The String to be printed
   * @throws	   java.io.IOException
   */
  public void println(String x) throws IOException {
    synchronized (lock) {
      print(x);
      println();
    }
  }

  /**
   * Print an Object and then terminate the line.  This method behaves as
   * though it invokes print(Object) and then println().
   *
   * @param      x   the object that will be printed
   * @throws	   java.io.IOException
   */
  public void println(Object x) throws IOException {
    synchronized (lock) {
      print(x);
      println();
    }
  }

  /**
   * Clear the contents of the buffer. If the buffer has been already
   * been flushed then the clear operation shall throw an IOException
   * to signal the fact that some data has already been irrevocably
   * written to the client response stream.
   *
   * @throws IOException		If an I/O error occurs
   */
  public void clear() throws IOException {
    if (writer != null) {
      throw new IOException();
    } else {
      synchronized (lock) {
        cb = new char[DEFAULT_BUFFER_SIZE];
        bufferSize = DEFAULT_BUFFER_SIZE;
        nextChar = 0;
      }
    }
  }

  /**
   * Clears the current contents of the buffer. Unlike clear(), this
   * mehtod will not throw an IOException if the buffer has already been
   * flushed. It merely clears the current content of the buffer and
   * returns.
   *
   * @throws IOException		If an I/O error occurs
   */
  public void clearBuffer() throws IOException {
    if (writer == null) {
      this.clear();
    }
  }

  public void flushBuffer() throws IOException {
    synchronized (lock) {
      if (nextChar == 0) {
        return;
      }
      writeOut(getEnclosingWriter());
      nextChar = 0;
      Object obj = getEnclosingWriter();
      if (obj instanceof JspWriterImpl) {
        ((JspWriterImpl)obj).flushBuffer();
      } else {
        ((BodyContentImpl)obj).flushBuffer();
      }
    }
  }

  /**
   * Close the stream, flushing it first.  Once a stream has been closed,
   * further write() or flush() invocations will cause an IOException to be
   * thrown.  Closing a previously-closed stream, however, has no effect.
   *
   * @exception  IOException  If an I/O error occurs
   */
  public void close() throws IOException {
    if (writer != null) {
        writer.close();
    } else {
      synchronized (lock) {
        flushBuffer();
        cb = null;
        closed = true;
      }
    }
  }

  /**
   * Returns the number of bytes unused in the buffer.
   *
   * @return the number of bytes unused in the buffer
   */
  public int getRemaining() {
    return (writer == null) ? bufferSize - nextChar : 0;
  }

  /**
   * Returns the value of this BodyJspWriter as a Reader.
   * Note: this is after evaluation!!  There are no scriptlets,
   * etc in this stream.
   *
   * @return the value of this BodyJspWriter as a Reader
   */
  public Reader getReader() {
    return (writer == null) ? new CharArrayReader(cb, 0, nextChar) : null;
  }

  /**
   * Return the value of the BodyJspWriter as a String.
   * Note: this is after evaluation!!  There are no scriptlets,
   * etc in this stream.
   *
   * @return the value of the BodyJspWriter as a String
   */
  public String getString() {
    return (writer == null) ? new String(cb, 0, nextChar) : null;
  }

  /**
   * Write the contents of this BodyJspWriter into a Writer.
   * Subclasses are likely to do interesting things with the
   * implementation so some things are extra efficient.
   *
   * @param out The writer into which to place the contents of
   * this body evaluation
   */
  public void writeOut(Writer out) throws IOException {
    if (writer == null) {
      out.write(cb, 0, nextChar);
      // Flush not called as the writer passed could be a BodyContent and
      // it doesn't allow to flush.
    }
  }

  /**
   * Sets the writer to which all output is written.
   */
  void setWriter(Writer writer) {
    this.writer = writer;
    if (writer != null) {
      // According to the spec, the JspWriter returned by
      // JspContext.pushBody(java.io.Writer writer) must behave as
      // though it were unbuffered. This means that its getBufferSize()
      // must always return 0. The implementation of
      // JspWriter.getBufferSize() returns the value of JspWriter's
      // 'bufferSize' field, which is inherited by this class.
      // Therefore, we simply save the current 'bufferSize' (so we can
      // later restore it should this BodyContentImpl ever be reused by
      // a call to PageContext.pushBody()) before setting it to 0.
      if (bufferSize != 0) {
        bufferSizeSave = bufferSize;
        bufferSize = 0;
      }
    } else {
      bufferSize = bufferSizeSave;
      clearBody();
    }
  }

  private void ensureOpen() throws IOException {
    if (closed) throw new IOException("Stream closed");
  }

  private void reAllocBuff(int len) {
    char[] tmp = new char[bufferSize];
    System.arraycopy(cb, 0, tmp, 0, cb.length);
    if (len <= DEFAULT_BUFFER_SIZE) {
      cb = new char[bufferSize + DEFAULT_BUFFER_SIZE];
      bufferSize += DEFAULT_BUFFER_SIZE;
    } else {
      cb = new char[bufferSize + len];
      bufferSize += len;
    }
    System.arraycopy(tmp, 0, cb, 0, tmp.length);
  }
}

