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

import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;
import com.sap.engine.services.webservices.jaxm.soap.SSLSocketUtilInterface;
import com.sap.engine.services.webservices.jaxm.soap.SSLUtilImpl;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.tc.logging.Location;

import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * This class is used in order to open a client socket outside the dispatcher.
 * This class is needed because we need to specify proxy and certificates, but there is no
 * chance to do this in the current Transport implementation.
 *
 * @author Alexander Zubev
 */
public class WSClientsSocket extends Socket {
  private String host;
  private int port;
  private String proxyHost;
  private int proxyPort;
  private Socket socket;
  private boolean secure = false;
  private List clientCertificateList;
  private List serverCertificateList;

  private WSClientsSocketInputStream inputStream;
  private WSClientsSocketOutputStream outputStream;

  private int soTimeout = -1;

  public WSClientsSocket(String host, int port) {
    this.host = host;
    this.port = port;
    inputStream = new WSClientsSocketInputStream();
    outputStream = new WSClientsSocketOutputStream();
  }

  public void setProxy(String proxyHost, int proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public void setClientCertificateList(List clientCertificateList) {
    this.clientCertificateList = clientCertificateList;
  }

  public void setServerCertificateList(List serverCertificateList) {
    this.serverCertificateList = serverCertificateList;
  }

  public void initialize() throws IOException {
    if (port == -1) {
      if (secure) {
        port = 443;
      } else {
        port = 80;
      }
    }

    socket = HTTPSocket.createSocket(host, 
                                     port, 
                                     0, 
                                     proxyHost, 
                                     proxyPort, 
                                     null, 
                                     null, 
                                     secure,
                                     secure ? new SSLUtilImpl() : null,
                                     clientCertificateList, 
                                     serverCertificateList, 
                                     false);
    if (this.soTimeout != -1) {
      socket.setSoTimeout(soTimeout);
    }
    outputStream.initialize(socket.getOutputStream());
    inputStream.initialize(socket.getInputStream());
  }

  public InputStream getInputStream() throws IOException {
    return inputStream;
  }

  public OutputStream getOutputStream() throws IOException {
    return outputStream;
  }

  //Overridden methods for compatibility
  public InetAddress getInetAddress() {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getInetAddress();
    }
  }

  public InetAddress getLocalAddress() {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getLocalAddress();
    }
  }

  public int getPort() {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getPort();
    }
  }

  public int getLocalPort() {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getLocalPort();
    }
  }

  public void setTcpNoDelay(boolean on) throws SocketException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      socket.setTcpNoDelay(on);
    }
  }

  public boolean getTcpNoDelay() throws SocketException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getTcpNoDelay();
    }
  }

  public void setSoLinger(boolean on, int linger) throws SocketException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      socket.setSoLinger(on, linger);
    }
  }

  public int getSoLinger() throws SocketException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getSoLinger();
    }
  }

  public synchronized void setSoTimeout(int timeout) throws SocketException {
    if (socket == null) {
      this.soTimeout = timeout;
    } else {
      socket.setSoTimeout(timeout);
    }
  }

  public synchronized int getSoTimeout() throws SocketException {
    if (socket == null) {
      if (this.soTimeout != -1) {
        return this.soTimeout;
      }
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getSoTimeout();
    }
  }

  public synchronized void setSendBufferSize(int size) throws SocketException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      socket.setSendBufferSize(size);
    }
  }

  public synchronized int getSendBufferSize() throws SocketException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getSendBufferSize();
    }
  }

  public synchronized void setReceiveBufferSize(int size) throws SocketException{
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      socket.setReceiveBufferSize(size);
    }
  }

  public synchronized int getReceiveBufferSize() throws SocketException{
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getReceiveBufferSize();
    }
  }

  public void setKeepAlive(boolean on) throws SocketException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      socket.setKeepAlive(on);
    }
  }

  public boolean getKeepAlive() throws SocketException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      return socket.getKeepAlive();
    }
  }

  public synchronized void close() throws IOException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      socket.close();
    }
  }

  public void shutdownInput() throws IOException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      socket.shutdownInput();
    }
  }

  public void shutdownOutput() throws IOException {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      throw new IllegalStateException("First initialize the socket and then call this method. ");
    } else {
      socket.shutdownOutput();
    }
  }

  public String toString() {
    if (socket == null) {
      try {
        throw new Exception("First initialize the socket and then call this method. ");
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.WEBSERVICES_LOCATION);
        wsLocation.catching(e);
      }
      return "Disconnected WSClientsSocket to host = " + host + ", port = " + port;
    } else {
      return "WSClientsSocket connected to " + socket.toString();
    }
  }
}
