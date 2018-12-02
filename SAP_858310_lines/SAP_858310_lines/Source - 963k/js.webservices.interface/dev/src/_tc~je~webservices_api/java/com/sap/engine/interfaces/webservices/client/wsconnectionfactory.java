/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.client;

import java.io.IOException;

/**
 * This interface is used by the Web Services Library in order to get an implementation
 * of the WSConnectionFactory.
 *
 * Using the WSConnectionFactory you can open a client socket from the dispatcher node,
 * which prevents any firewalls that are between the dispatcher and the server nodes.
 *
 * @author Alexander Zubev
 */
public interface WSConnectionFactory {

  /**
   * The name under which the WSConnectionFactory is bound in the naming.
   * This name is relative to the wsContext naming root.
   */
  public static final String NAME = "WSConnectionFactory";

  /**
   * A method for openning a normal socket.
   *
   * @param host The hostname of the server, where the socket will try to connect
   * @param port The port number of the server
   * @param proxyHost The hostname of a proxy server. If no proxy server is used, <code>null</code> must be specified
   * @param proxyPort The port number of the proxy server.
   * @return A WSConnection object representing the real socket.
   * @throws IOException in case of I/O Errors. The exception will be logged in the webservices service log files.
   */
  public WSConnection getConnection(String host, int port, String proxyHost, int proxyPort) throws IOException;

  /**
   * A method for openning a secure socket (over SSL).
   * Note, when this method is used, the security provider should be present
   *
   * @param host The hostname of the server, where the socket will try to connect
   * @param port The port number of the server
   * @param proxyHost The hostname of a proxy server. If no proxy server is used, <code>null</code> must be specified
   * @param proxyPort The port number of the proxy server.
   * @return A WSConnection object representing the real socket.
   * @throws IOException in case of I/O Errors. The exception will be logged in the webservices service log files.
   */
  public WSConnection getSSLConnection(String host, int port, String proxyHost, int proxyPort) throws IOException;
}
