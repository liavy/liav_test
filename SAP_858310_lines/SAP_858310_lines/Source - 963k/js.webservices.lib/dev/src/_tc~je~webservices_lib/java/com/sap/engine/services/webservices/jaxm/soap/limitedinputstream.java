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
import java.io.InputStream;
import java.io.IOException;

/**
 * InputStream that reads limited account of data from the input stream and then returns EOF
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class LimitedInputStream extends BufferedInputStream {

  private int contentCount;

  public LimitedInputStream(InputStream inputStream, int contentCount) {
    super(inputStream);
    this.contentCount = contentCount;
  }

  /**
   * Reads a single byte from the input stream.
   * @return
   * @throws IOException
   */
  public synchronized int read() throws IOException {
    if (contentCount == 0) {
      return -1;
    } else {
      -- contentCount;
      return super.read();
    }
  }

  /**
   * Reads from input stream up to [len] bytes.
   * @param b
   * @param off
   * @param len
   * @return
   * @throws IOException
   */
  public synchronized int read(byte b[], int off, int len) throws IOException {
    if (contentCount == 0) {
      return -1;
    }
    if (len > contentCount) {
      len = contentCount;
    }
    int readBytes = super.read(b,off,len);
    if (readBytes == -1) {
      contentCount = 0;
      return -1;
    } else {
      contentCount -= readBytes;
      return readBytes;
    }
  }

  /**
   * Returns byte available in the buffer.
   * @return
   * @throws IOException
   */
  public synchronized int available() throws IOException {
	  int aval = super.available();
    if (aval >= contentCount) {
      return contentCount;
    } else {
      return aval;
    }
  }
}
