package com.sap.engine.services.webservices.common;

import java.util.List;

/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class OpenSocketRequest extends Request {

  private String host;
  private int port;
  private boolean sslSocket;
  private String proxyHost;
  private int proxyPort;
  private List clientCertificateList;//$JL-SER$
  private List serverCertificateList;//$JL-SER$

  public OpenSocketRequest() {
    requestId = WSConnectionConstants.OPEN_SOCKET_REQUEST;
  }

  public OpenSocketRequest(String host, int port, boolean sslSocket) {
    this.host = host;
    this.port = port;
    this.sslSocket = sslSocket;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getSize() {
    return  host.length() + 4;
  }

  public String getRequestName() {
    return "OPEN_SOCKET_REQUEST";
  }

  public boolean isSslSocket() {
    return sslSocket;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }

  public boolean isUsingProxy() {
    return proxyHost != null;
  }

  public List getClientCertificateList() {
    return clientCertificateList;
  }

  public void setClientCertificateList(List clientCertificateList) {
    this.clientCertificateList = clientCertificateList;
  }

  public List getServerCertificateList() {
    return serverCertificateList;
  }

  public void setServerCertificateList(List serverCertificateList) {
    this.serverCertificateList = serverCertificateList;
  }
}