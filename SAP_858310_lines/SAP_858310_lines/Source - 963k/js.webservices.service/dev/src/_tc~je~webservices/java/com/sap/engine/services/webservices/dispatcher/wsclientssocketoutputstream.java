/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.dispatcher;

import java.io.OutputStream;
import java.io.IOException;

/**
 * This class represents the real output stream of the socket.
 * It is required due to, the WSClientsSocket.getOutputStream is invoked before
 * initializing the WSClientsSocket with the real socket.
 *
 * @author Alexander Zubev
 */
public class WSClientsSocketOutputStream extends OutputStream {
  private OutputStream realOutputStream;

  public WSClientsSocketOutputStream() {
  }

  public void initialize(OutputStream out) {
    realOutputStream = out;
  }

  public void write(int b) throws IOException {
    realOutputStream.write(b);
  }

  public void write(byte[] b) throws IOException {
    realOutputStream.write(b);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    realOutputStream.write(b, off, len);
  }

  public void flush() throws IOException {
    if (realOutputStream != null) {
      realOutputStream.flush();
    }
  }

  public void close() throws IOException {
    if (realOutputStream != null) {
      realOutputStream.close();
    }
  }

}
