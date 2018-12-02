/*
 * Copyright (c) 2002 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.dispatcher;

import com.sap.engine.frame.cluster.transport.TransportFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Alexander Zubev
 */
public class WSClientsTransportFactory implements TransportFactory {

  /**
   * Opens ServerSocket.
   *
   * @param   port  The socket's port.
   * @return     The ServerSocket object.
   * @exception   java.io.IOException  - if I/O Error occures.
   */
  public ServerSocket getServerSocket(int port) throws IOException {
    return null; //WS Clients does not need to open a server socket
  }


  /**
   * Opens ServerSocket with set buffer accept size.
   *
   * @param   port  The socket's port.
   * @param   acceptSize The socket's accept size.
   * @return     The ServerSocket object.
   * @exception   java.io.IOException  - if I/O Error occures.
   */
  public ServerSocket getServerSocket(int port, int acceptSize) throws IOException {
    return null; //WS Clients does not need to open a server socket
  }


  /**
   * Opens ServerSocket with set buffer accept size.
   *
   * @param   port  The socket's port.
   * @param   acceptSize The socket's accept size.
   * @param   bindAddr the local InetAddress the server socket will bind to.
   *
   * @return     The ServerSocket object.
   *
   * @exception   java.io.IOException  - if I/O Error occures.
   */
  public ServerSocket getServerSocket(int port, int acceptSize, String bindAddr) throws IOException {
    return null; //WS Clients does not need to open a server socket
  }

  public Socket getSocket(String host, int port) throws UnknownHostException, IOException {
    return new WSClientsSocket(host, port);

/*    WSClientsRequest request = WSClientsRequest.fromString(host);
    String realHost = request.getHost();
    String proxyHost = request.getProxyHost();
    int proxyPort = request.getProxyPort();

    if (request.isOverSSL()) {
      if (proxyHost != null) {
        System.out.println("WSClientsTransportFactory.getSocket @@@@@@@@@@@@@");
        SSLSocketUtilInterface sslUtil;
        try {
          Class sslUtilsClass = Class.forName("com.sap.engine.services.webservices.jaxm.soap.SSLUtilImpl");
          sslUtil = (SSLSocketUtilInterface) sslUtilsClass.newInstance();
        } catch (Exception ex) {
          throw new IOException("SSL not available:" + ex.getMessage());
        }
        return sslUtil.createSSLSocket(realHost,
                                       port,
                                       proxyHost,
                                       proxyPort,
                                       null, //clientCertificateList,
                                       null, //serverCertificateList,
                                       true);
      } else {
				SSLSocketUtilInterface sslUtil = null;
				try {
          Class sslUtilsClass = Class.forName("com.sap.engine.services.webservices.jaxm.soap.SSLUtilImpl");
          sslUtil = (SSLSocketUtilInterface) sslUtilsClass.newInstance();
				} catch (Exception ex) {
					throw new IOException("SSL not available:" + ex.getMessage());
				}
			  return sslUtil.createSSLSocket(realHost,
                                       port,
                                       null, //clientCertificateList,
                                       null, //serverCertificateList,
                                       true);
      }
    } else {
      if (proxyHost == null) {
        return new Socket(realHost, port);
      } else {
        return new Socket(proxyHost, proxyPort);
      }
    } */
  }


  public Socket getSocket(String host, int port, Properties props) throws UnknownHostException, IOException {
    return getSocket(host, port);
  }


  public void setFactory(TransportFactory factory) {
    //do noting
  }
}
