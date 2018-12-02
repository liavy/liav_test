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

import com.sap.tc.logging.Location;
import com.sap.engine.services.webservices.exceptions.WSLogging;

import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;

/**
 * This class represents the real input stream of the socket.
 * It is required due to, the WSClientsSocket.getInputStream is invoked before
 * initializing WSClientsSocket with the real socket.
 *
 * @author Alexander Zubev
 */
public class WSClientsSocketInputStream extends InputStream {
  private static final int DELAY_TIME = 100;

  private InputStream realInputStream;

  public WSClientsSocketInputStream() {
  }

  public void initialize(InputStream in) {
//    System.out.println("WSClientsSocketInputStream.initialize: " + this.hashCode());
    realInputStream = in;
  }

  public int read() throws IOException {
//    System.out.println("WSClientsSocketInputStream.read: realInputStream = null: " + this.hashCode());
    try {
      try {
        int i = 0;
        while (realInputStream == null) {
          if (i++ >= 10) {
            IOException ie = new IOException("Socket initialization time out received!");
            Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
            wsLocation.catching(ie);
            throw ie;
          }
          Thread.sleep(DELAY_TIME);
        }
      } catch (InterruptedException ie) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(ie);
        if (realInputStream != null) {
          return realInputStream.read();
        } else {
          throw new IOException("InputStream not initialized!");
        }
      }
      return realInputStream.read();
    } catch (EOFException eofe) { //thrown by IAIK streams instead of returning -1
      return -1;
    }
  }

  public int read(byte[] b) throws IOException {
    try {
      try {
        int i = 0;
        while (realInputStream == null) {
          if (i++ >= 10) {
            IOException ie = new IOException("Socket initialization time out received!");
            Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
            wsLocation.catching(ie);
            throw ie;
          }
          Thread.sleep(DELAY_TIME);
        }
      } catch (InterruptedException ie) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(ie);
        if (realInputStream != null) {
          return realInputStream.read();
        } else {
          throw new IOException("InputStream not initialized!");
        }
      }
      return realInputStream.read(b);
    } catch (EOFException eofe) { //thrown by IAIK streams
      return -1;
    }
  }

  public int read(byte[] b, int off, int len) throws IOException {
    try {
      try {
        int i = 0;
        while (realInputStream == null) {
          if (i++ >= 10) {
            IOException ie = new IOException("Socket initialization time out received!");
            Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
            wsLocation.catching(ie);
            throw ie;
          }
          Thread.sleep(DELAY_TIME);
        }
      } catch (InterruptedException ie) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(ie);
        if (realInputStream != null) {
          return realInputStream.read();
        } else {
          throw new IOException("InputStream not initialized!");
        }
      }
      int l = realInputStream.read(b, off, len);
//      System.out.println("WSClientsSocketInputStream.read: DATA is = " + new String(b, off, l));
      return l;
    } catch (EOFException eofe) { //thrown by IAIK streams
      return -1;
    }
  }

  public long skip(long n) throws IOException {
    return realInputStream.skip(n);
  }

  public int available() throws IOException {
    return realInputStream.available();
  }

  public void close() throws IOException {
    realInputStream.close();
  }

  public void mark(int readlimit) {
    realInputStream.mark(readlimit);
  }

  public void reset() throws IOException {
    realInputStream.reset();
  }

  public boolean markSupported() {
    return realInputStream.markSupported();
  }
}
