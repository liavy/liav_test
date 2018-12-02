/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxm.soap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import com.sap.engine.lib.xml.parser.helpers.CharArray;

/**
 * Class that serves as a BufferedInputStreamWrapper that is able to Log (Copy) Input.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class InputStreamLogger extends BufferedInputStream {

  private OutputStream log;
  private CharArray buf = new CharArray();
  private boolean parseHeaders = true;
  private OutputStream realLogger;

  public InputStreamLogger(InputStream in, OutputStream log) {
    super(in);
    this.log = log;
  }

  public InputStreamLogger(InputStream in, int size,OutputStream log) {
    super(in,size);
    this.log = log;
  }

  public synchronized int read() throws IOException {
    int res = super.read();    
    if (res != -1) {
      if (parseHeaders) {
        if (res != '\n' && res != '\r') {
          buf.append((char) res);
        } else if (res == '\r') {
          if (buf.length() == 0) {
            log.write(res);
            log.flush();
          }
        } else {
          String headerLine = buf.toString();
          buf.clear();
          if (headerLine.toLowerCase(Locale.ENGLISH).startsWith("set-cookie:")) {
            log.write("Set-Cookie: <value is hidden>".getBytes()); //$JL-I18N$
          } else {
            log.write(headerLine.getBytes()); //$JL-I18N$
          }
          log.write(res);
          log.flush();
        }
      } else {
        log.write(res);
        log.flush();
      }
    }
    return res;
  }

  public synchronized int read(byte b[], int off, int len) throws IOException {
    int res = super.read(b,off,len);
    if (res>0) {
      log.write(b,off,res);
      log.flush();
    }
    return res;
  }

  public void close() throws IOException {
    super.close();
    flushMarkedData();
    log.flush();
  }

  public void setParseHeaders(boolean parseHeaders) {
    this.parseHeaders = parseHeaders;
  }
  
  private void flushMarkedData() throws IOException {
    if (realLogger != null && log != realLogger) {
      byte[] data = ((ByteArrayOutputStream) log).toByteArray();
      log = realLogger;
      realLogger = null;
      log.write(data);
    }
  }
  
  /* (non-Javadoc)
   * @see java.io.InputStream#mark(int)
   */
  public synchronized void mark(int readlimit) {
    super.mark(readlimit);
    try {
      flushMarkedData();
    } catch (IOException ioe) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      pw.flush();
      String s = sw.toString();
      pw.close();
      throw new RuntimeException(s);
    }
    if (realLogger == null) {
      realLogger = log;
    }
    log = new ByteArrayOutputStream(readlimit);
  }

  /* (non-Javadoc)
   * @see java.io.InputStream#reset()
   */
  public synchronized void reset() throws IOException {
    super.reset();
    ((ByteArrayOutputStream) log).reset();
  }

}
