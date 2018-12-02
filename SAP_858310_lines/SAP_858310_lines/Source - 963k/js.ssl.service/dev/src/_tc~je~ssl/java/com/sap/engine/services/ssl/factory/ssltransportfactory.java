/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf.. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ssl.factory;

import com.sap.engine.frame.cluster.transport.TransportFactory;
import com.sap.engine.services.ssl.exception.BaseIOException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 *  This factory provided by the SSL transport supplier.
 *
 * @author  Stephan Zlatarev
 * @version 4.0.3
 */
public class SSLTransportFactory implements TransportFactory {

  private TransportFactory base = null;
  private static ClientSocketFactory clientSocketFactory = null;
  private static ServerSocketFactory serverSocketFactory = null;

  public static synchronized final void stop() {
    if (clientSocketFactory != null) {
      clientSocketFactory.stop();
    }

    if (serverSocketFactory != null) {
      serverSocketFactory.stop();
    }
  }

  public SSLTransportFactory() {
    // do nothing
  }

  public SSLTransportFactory(TransportFactory base) {
    this.base = base;

  }

  public void setFactory(TransportFactory factory) {
    this.base = factory;
  }

  protected synchronized ClientSocketFactory getClientSocketFactory() throws IOException {
    try {
      if (clientSocketFactory == null) {
        clientSocketFactory = (ClientSocketFactory) ClientSocketFactory.getDefault();
      }
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
    return clientSocketFactory;
  }

  public ServerSocket getServerSocket(int port, int accSize, String bindAddr) throws IOException {
    ServerSocket result = null;
    try {
      getServerSocketFactory();
      result = serverSocketFactory.createServerSocket(base.getServerSocket(port, accSize, bindAddr));
    } catch (IOException io_e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(io_e);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
    return result;
  }

  public ServerSocket getServerSocket(int port, int accSize) throws IOException {
    ServerSocket result = null;
    try {
      getServerSocketFactory();
      result = serverSocketFactory.createServerSocket(base.getServerSocket(port, accSize));
    } catch (IOException io_e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(io_e);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
    return result;
  }

  public ServerSocket getServerSocket(int port) throws IOException {
    ServerSocket result = null;
    try {
      getServerSocketFactory();
      result = serverSocketFactory.createServerSocket(base.getServerSocket(port));
    } catch (IOException io_e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(io_e);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
    return result;
  }

  private synchronized void getServerSocketFactory() throws IOException {
    if (serverSocketFactory == null) {
      try {
        serverSocketFactory = (ServerSocketFactory) ServerSocketFactory.getDefault();
      } catch (NoClassDefFoundError e) {
        //$JL-EXC$
        throw BaseIOException.wrapException(e);
      } catch (Exception exc) {
        //$JL-EXC$
        throw BaseIOException.wrapException(exc);
      }
    }
  }

  public Socket getSocket(String host, int port, Properties props) throws IOException {
    Socket socket = null;
    try {
      getClientSocketFactory();
      socket = clientSocketFactory.createSocket(base.getSocket(host, port, props), host, port, false);
    } catch (IOException io_e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(io_e);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
    return socket;
  }
  

  public Socket getSocket(String host, int port) throws IOException {
    Socket socket = null;
    try {
      getClientSocketFactory();
      socket = clientSocketFactory.createSocket(base.getSocket(host, port), host, port, false);
    } catch (IOException io_e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(io_e);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
    return socket;
  }

}

