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
package com.sap.engine.services.servlets_jsp.server.runtime.client;

import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalStateException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;

import java.io.IOException;
import java.io.Writer;

public class JspWriterToPrintWriterWrapper extends PrintWriterImpl {
  private Writer out = null;

  public JspWriterToPrintWriterWrapper(Writer out) {
    super(out);
    this.out = out;
  }

  public void init(Writer out) {
    this.out = out;
  }

  public void reset() {
    this.out = null;
  }

  public void write(int c) {
    try {
      out.write(c);
    } catch (IOException io) {
      throw new WebIllegalStateException(WebIllegalStateException.CANNOT_WRITE_TO_STREAM, io);
    }
  }

  public void write(char buf[], int off, int len) {
    try {
      out.write(buf, off, len);
    } catch (IOException io) {
      throw new WebIllegalStateException(WebIllegalStateException.CANNOT_WRITE_TO_STREAM, io);
    }
  }

  public void write(String s, int off, int len) {
    try {
      out.write(s, off, len);
    } catch (IOException io) {
      throw new WebIllegalStateException(WebIllegalStateException.CANNOT_WRITE_TO_STREAM, io);
    }
  }

  public void println() {
    try {
      out.write(Constants.lineSeparator);
    } catch (IOException io) {
      throw new WebIllegalStateException(WebIllegalStateException.CANNOT_WRITE_TO_STREAM, io);
    }
  }

  public void close() {
    try {
      out.close();
    } catch (IOException t) {
      throw new WebIllegalStateException(WebIllegalStateException.Error_while_closing_stream, t);
    }
  }

  public void flush() {
    try {
      out.flush();
    } catch (IOException io) {
      throw new WebIllegalStateException(WebIllegalStateException.CANNOT_FLUSH_STREAM, io);
    }
  }
}
