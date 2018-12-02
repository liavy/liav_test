/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream for use with MIME implementation that does not implement the close() method properly.
 * The MIME Multipart implementation parses the message and closes the message stream.
 * 
 * @version 1.0 (2006-7-25)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class UnclosableInputStream extends InputStream {
  
  private InputStream inside = null;
  
  public UnclosableInputStream(InputStream wrapped) {
    this.inside = wrapped;
  }
  
  /**
   * Reads byte from the stream.
   * @return
   * @throws IOException
   */
  public int read() throws IOException {
    return inside.read();
  }

  public void  close() throws IOException {
    // This method schould no be implemented
  }

  @Override
  public int available() throws IOException {
    return inside.available();
  }

  @Override
  public synchronized void mark(int readlimit) {
    inside.mark(readlimit);
  }

  @Override
  public boolean markSupported() {
    return inside.markSupported();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return super.read(b, off, len);
  }

  @Override
  public int read(byte[] b) throws IOException {
    return super.read(b);
  }

  @Override
  public synchronized void reset() throws IOException {
    inside.reset();
  }

  @Override
  public long skip(long n) throws IOException {
    return inside.skip(n);
  }
}
