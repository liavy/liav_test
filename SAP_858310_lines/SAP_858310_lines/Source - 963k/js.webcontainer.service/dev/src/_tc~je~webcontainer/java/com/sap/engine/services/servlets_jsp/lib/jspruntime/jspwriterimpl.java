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

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import java.io.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.JspIOException;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.JspIllegalArgumentException;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.JspIndexOutOfBoundsException;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.boot.SystemProperties;

/**
 * This class emulates some of the functionality found in the java.io.BufferedWriter
 * and java.io.PrintWriter classes, however it differs in that it throws java.io.IOException
 * from the print methods with PrintWriter does not.
 */
public class JspWriterImpl extends JspWriter {
  public static final String lineSeparator = SystemProperties.getProperty("line.separator");
  /**
   * originel Writer
   */
  private Writer out;
  /**
   * Servlet response
   */
  private ServletRequest servletRequest = null;
  private ServletResponse response;
  /**
   * buffer
   */
  private char cb[];
  /**
   * Max length of buffer
   */
  private int nChars;
  /**
   * current posicion in buffer
   */
  private int nextChar;
  /**
   * is Autoflush
   */
  private boolean autoflush;
  /**
   * is buffered
   */
  private boolean isBuffered = true;
  private boolean isClosed = false;
  private boolean flushed = false;

  /**
   * Construnct new JspWriterImpl
   *
   * @param   servletresponse  	servlet response
   * @param   i  								buffer of writer
   * @param   flag  						outoflush
   */
  public JspWriterImpl(ServletResponse servletresponse, ServletRequest servletRequest, int i, boolean flag) throws IOException {
    super(i, flag);
    if (i <= 0) {
      if (i == 0) {
        i = 1;
        isBuffered = false;
      } else {
        throw new JspIllegalArgumentException(JspIllegalArgumentException.BUFFER_LENGTH_IS_NEGATIVE_OR_NULL);
      }
    }

    response = servletresponse;
    cb = new char[i];
    nChars = i;
    nextChar = 0;
    autoflush = flag;
    isClosed = false;
    this.servletRequest = servletRequest;
    FilterUtils.unWrapResponse(response).markWriter();
  }

  /**
   * Init writer
   */
  public final void init(ServletResponse servletresponse, ServletRequest servletRequest, int i, boolean flag) throws IOException {
    out = null;
    this.lock = this;
    this.bufferSize = i;
    this.autoFlush = flag;
    response = servletresponse;
    this.servletRequest = servletRequest;
    if (i == 0) {
      i = 1;
      isBuffered = false;
    } else {
      isBuffered = true;
    }

    if (i > 0 && (cb == null || i > cb.length)) {
      cb = new char[i];
    }

    nChars = i;
    nextChar = 0;
    autoflush = flag;
    isClosed = false;
    flushed = false;
    FilterUtils.unWrapResponse(response).markWriter();
  }

  /**
   * Flush buffer to output sream
   *
   * @exception   IOException
   */
  public void flushBuffer() throws IOException {
    synchronized (lock) {
      if (!ensureOpen()) {
        return;
      }

      if (nextChar == 0) {
        return;
      }

      if (out == null) {
        Object includedOut = servletRequest.getAttribute("com.sap.engine.internal.jsp.includeInOut");
        if (includedOut != null) {
          servletRequest.removeAttribute("com.sap.engine.internal.jsp.includeInOut");
        }
        /*

					JSP.4.2 Response Character Encoding
					 For JSP pages in standard syntax, it is the character encoding specified by the
					BOM, by the pageEncoding attribute of the page directive, or by a JSP configuration
					element page-encoding whose URL pattern matches the page. Only the
					character encoding specified for the requested page is used; the encodings of
					files included via the include directive are not taken into consideration. If
					there's no such specification, no initial response character encoding is passed
					to ServletResponse.setContentType() - the ServletResponse object's default,
					ISO-8859-1, is used.        
					*/
        if( response.getContentType() == null || response.getContentType().indexOf("charset")<0 ){
          if (includedOut == null) {
            response.setCharacterEncoding("ISO-8859-1");
          } else { //fixed for Siemens DSA application bug
            response.setCharacterEncoding("UTF-8");
          }
        }
        out = response.getWriter();
        if (includedOut != null) {
          servletRequest.setAttribute("com.sap.engine.internal.jsp.includeInOut", includedOut);
        }
      }

      out.write(cb, 0, nextChar);
      nextChar = 0;
      flushed = true;
    }
  }

  /**
   * Uses when buffer is full and must send data to client.
   *
   * @exception   IOException
   */
  private void flushToClient() throws IOException {
    if (out != null && isBuffered) {
      out.flush();
    }
  }

  /**
   * Clear buffer of the writer
   *
   * @exception   IOException  throws it
   */
  public final void clear() throws IOException {
    synchronized (lock) {
      if (!isBuffered) {
        throw new JspIOException(JspIOException.JSP_WRITER_NOT_BUFFERED);
      }

      if (flushed) {
        throw new JspIOException(JspIOException.JSP_WRITER_ALREADY_FLUSHED);
      }

      clearBuffer();
    }
  }

  private final void bufferOverflow() throws IOException {
    throw new JspIOException(JspIOException.BUFFER_OVERFLOW);
  }

  /**
   * Flush buffer to output sream
   *
   * @exception   IOException  throws it
   */
  public void flush() throws IOException {
    synchronized (lock) {
      if (isClosed) {
        throw new IOException("Stream closed");
      }
      flushBuffer();

      if (out != null) {
        out.flush();
      }
			//CTS 5.0
     // response.flushBuffer();
    }
  }

  /**
   * Flush buffer and close writer
   *
   * @exception   IOException  throws it
   */
  public void close() throws IOException {
    synchronized (lock) {
      if (response == null || isClosed) {
        // multiple calls to close is OK
        return;
      }
      flushBuffer();
      if (out != null) {
        out.close();
      }

      out = null;
      isClosed = true;
      //      cb = null;
    }
  }

  /**
   * Return buffer size
   *
   * @return     buffer size
   */
  public int getBufferSize() {
    return (isBuffered) ? nChars : 0;
  }

  /**
   * Return free buffer space
   *
   * @return     remining buffer space
   */
  public int getRemaining() {
    if (!isBuffered) {
      return 0;
    }

    return nChars - nextChar;
  }

  /**
   * Check if autoflush
   *
   * @return     true if autoflaush
   */
  public boolean isAutoFlush() {
    return autoflush;
  }

  /**
   * If the stream is already closed throws an exception.
   *
   * @exception   IOException
   */
  private boolean ensureOpen() throws IOException {
    if (response == null || isClosed) {
      throw new JspIOException(JspIOException.STREAM_IS_CLOSED);
    }
    return true;
  }

  /**
   * Write int
   *
   * @param   i  int to be writen
   * @exception   IOException
   */
  public void write(int i) throws IOException {
    synchronized (lock) {
      if (!ensureOpen()) {
        return;
      }

      if (nextChar >= nChars) {
        if (autoflush) {
          flushBuffer();
          flushToClient();
        } else {
          bufferOverflow();
        }
      }

      cb[nextChar++] = (char) i;
    }
  }

  private int min(int i, int j) {
    if (i < j) {
      return i;
    } else {
      return j;
    }
  }

  /**
   * Write array of char
   *
   * @param   ac  array of char
   * @param   i  start index of array
   * @param   j  end index of array
   * @exception   IOException  throws it
   */
  public void write(char ac[], int i, int j) throws IOException {
    synchronized (lock) {
      if (!ensureOpen()) {
        return;
      }

      if (i < 0 || i > ac.length || j < 0 || i + j > ac.length || i + j < 0) {
        throw new JspIndexOutOfBoundsException(JspIndexOutOfBoundsException.Incorrect_parameters_in_array_of_charactes_for_writing);
      }

      if (j == 0) {
        return;
      }

      if (j >= nChars) {
        if (autoflush) {
          flushBuffer();
          flushToClient();
        } else {
          bufferOverflow();
        }

        out.write(ac, i, j);
        return;
      }

      int k = i;

      for (int l = i + j; k < l;) {
        int i1 = min(nChars - nextChar, l - k);
        System.arraycopy(ac, k, cb, nextChar, i1);
        k += i1;
        nextChar += i1;

        if (nextChar >= nChars) {
          if (autoflush) {
            flushBuffer();
            flushToClient();
          } else {
            bufferOverflow();
          }
        }
      }
    }
  }

  /**
   * Write array of char
   *
   * @param   ac  array of char
   * @exception   IOException  throws it
   */
  public void write(char ac[]) throws IOException {
    write(ac, 0, ac.length);
  }

  /**
   * Write part of String
   *
   * @param   s  String to be writen
   * @param   i  start index of String
   * @param   j  end index of String
   * @exception   IOException  throws it
   */
  public void write(String s, int i, int j) throws IOException {
    synchronized (lock) {
      if (!ensureOpen()) {
        return;
      }
      int k = i;

      for (int l = i + j; k < l;) {
        int i1 = min(nChars - nextChar, l - k);
        s.getChars(k, k + i1, cb, nextChar);
        k += i1;
        nextChar += i1;

        if (nextChar >= nChars) {
          if (autoflush) {
            flushBuffer();
            flushToClient();
          } else {
            bufferOverflow();
          }
        }
      }
    }
  }

  /**
   * Write String
   *
   * @param   s  String to be writen
   * @exception   IOException  throws it
   */
  public void write(String s) throws IOException {
    write(s, 0, s.length());
  }

  /**
   * Write a new line
   *
   * @exception   IOException  throws it
   */
  public void newLine() throws IOException {
    synchronized (lock) {
      write(lineSeparator);
    }
  }

  /**
   *Print boolean
   *
   * @param   flag  boolean value
   * @exception   IOException  throws it
   */
  public void print(boolean flag) throws IOException {
    write(flag ? "true" : "false");
  }

  /**
   *Print char
   *
   * @param   c  char value
   * @exception   IOException  throws it
   */
  public void print(char c) throws IOException {
    write(String.valueOf(c));
  }

  /**
   *Print int
   *
   * @param   i  int value
   * @exception   IOException  throws it
   */
  public void print(int i) throws IOException {
    write(String.valueOf(i));
  }

  /**
   *Print long
   *
   * @param   l  long value
   * @exception   IOException  throws it
   */
  public void print(long l) throws IOException {
    write(String.valueOf(l));
  }

  /**
   *Print float
   *
   * @param   f  float value
   * @exception   IOException  throws it
   */
  public void print(float f) throws IOException {
    write(String.valueOf(f));
  }

  /**
   *Print double
   *
   * @param   d  double value
   * @exception   IOException  throws it
   */
  public void print(double d) throws IOException {
    write(String.valueOf(d));
  }

  /**
   *Print char []
   *
   * @param   ac  char [] value
   * @exception   IOException  throws it
   */
  public void print(char ac[]) throws IOException {
    write(ac);
  }

  /**
   *Print String
   *
   * @param   s  String value
   * @exception   IOException  throws it
   */
  public void print(String s) throws IOException {
    if (s == null) {
      s = "null";
    }

    write(s);
  }

  /**
   *Print Object
   *
   * @param   obj  Objrct value
   * @exception   IOException  throws it
   */
  public void print(Object obj) throws IOException {
    write(String.valueOf(obj));
  }

  /**
   *Print new line
   *
   * @exception   IOException  throws it
   */
  public void println() throws IOException {
    newLine();
  }

  /**
   *Print boolean and new line
   *
   * @param   flag  boolean value
   * @exception   IOException  throws it
   */
  public void println(boolean flag) throws IOException {
    synchronized (lock) {
      print(flag);
      println();
    }
  }

  /**
   *Print char and new line
   *
   * @param   c  char value
   * @exception   IOException  throws it
   */
  public void println(char c) throws IOException {
    synchronized (lock) {
      print(c);
      println();
    }
  }

  /**
   *Print int and new line
   *
   * @param   i  int value
   * @exception   IOException  throws it
   */
  public void println(int i) throws IOException {
    synchronized (lock) {
      print(i);
      println();
    }
  }

  /**
   *Print long and new line
   *
   * @param   l  long value
   * @exception   IOException  throws it
   */
  public void println(long l) throws IOException {
    synchronized (lock) {
      print(l);
      println();
    }
  }

  /**
   * Print float and new line
   *
   * @param   f  float value
   * @exception   IOException  throws it
   */
  public void println(float f) throws IOException {
    synchronized (lock) {
      print(f);
      println();
    }
  }

  /**
   * Print double and new line
   *
   * @param   d  double value
   * @exception   IOException  throws it
   */
  public void println(double d) throws IOException {
    synchronized (lock) {
      print(d);
      println();
    }
  }

  /**
   * Print char [] and new line
   *
   * @param   ac char [] value
   * @exception   IOException  throws it
   */
  public void println(char ac[]) throws IOException {
    synchronized (lock) {
      print(ac);
      println();
    }
  }

  /**
   * Print String and new line
   *
   * @param   s  String value
   * @exception   IOException  throws it
   */
  public void println(String s) throws IOException {
    synchronized (lock) {
      print(s);
      println();
    }
  }

  /**
   * Print Object and new line
   *
   * @param   obj  Object value
   * @exception   IOException  throws it
   */
  public void println(Object obj) throws IOException {
    synchronized (lock) {
      print(obj);
      println();
    }
  }

  /**
   * Clear buffer of the writer
   *
   * @exception   IOException
   */
  public void clearBuffer() throws IOException {
    synchronized (lock) {
      ensureOpen();
      nextChar = 0;
    }
  }

  public String toString() {
    return super.toString();
  }
}

