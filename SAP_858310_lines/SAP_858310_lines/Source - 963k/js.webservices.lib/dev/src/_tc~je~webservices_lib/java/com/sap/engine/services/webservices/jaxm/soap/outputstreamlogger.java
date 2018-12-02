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

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Class that serves as BufferedOutputStream wrapper and log (copy) everything output.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class OutputStreamLogger extends BufferedOutputStream {
  private OutputStream log;

  public OutputStreamLogger(OutputStream out, OutputStream log) {
    super(out);
    this.log = log;
  }

  public OutputStreamLogger(OutputStream out, int size, OutputStream log) {
    super(out,size);
    this.log = log;
  }

  public synchronized void write(int b) throws IOException {
    super.write(b);
    log.write(b);
    log.flush();
  }

  public synchronized void write(byte b[], int off, int len) throws IOException {
    super.write(b,off,len);
    log.write(b,off,len);
    log.flush();
  }

  public synchronized void flush() throws IOException {
    super.flush();
    log.flush();
  }

  public synchronized void writeHidden(String normalText, String hiddenText) throws IOException {
    byte[] normalTextBytes = normalText.getBytes(); //$JL-I18N$ 
    super.write(normalTextBytes, 0, normalTextBytes.length);
    log.write(hiddenText.getBytes()); //$JL-I18N$
    log.flush();
  }
}
