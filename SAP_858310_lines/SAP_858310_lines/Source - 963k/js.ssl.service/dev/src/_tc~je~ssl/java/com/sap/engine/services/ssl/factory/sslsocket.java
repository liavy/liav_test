/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 *
 */
package com.sap.engine.services.ssl.factory;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImplFactory;
import java.util.Vector;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLSessionContext;
import java.security.cert.Certificate;
import iaik.security.jsse.net.JSSESessionManager;
import iaik.security.ssl.CipherSuiteList;
import iaik.security.ssl.SSLContext;
import iaik.security.ssl.SSLServerContext;
import iaik.security.ssl.SessionManager;
import com.sap.engine.services.ssl.util.CipherSuitesUtility;
import com.sap.engine.services.ssl.exception.*;
import com.sap.engine.services.ssl.dispatcher.DispatcherService;
import com.sap.tc.logging.Severity;

public class SSLSocket extends javax.net.ssl.SSLSocket {

  private iaik.security.ssl.SSLSocket iaikSocket;
  private Socket innerSocket = null;
  private boolean autoClose = true;
  private Vector listener = new Vector();
  private boolean isSessionCreationEnabled = true;
  private boolean isHandshakeDone = false;
  private SSLContext context;
  private SSLSession session = null;
  private SSLSessionContext sessionManager = null;
  private Certificate[] clientCertificate = null;

  public Certificate[] getClientCertificate() {
    return clientCertificate;
  }

  private static final String CLIENT_CERT_NOT_AVAILABLE = "ssl_ssl_socket_client_cert_not_available";
  private void notifyListener() {
    HandshakeCompletedEvent event = new HandshakeCompletedEvent(this, getSession());
    try {
      clientCertificate = iaik.security.jsse.utils.Util.convert(event.getPeerCertificateChain());
    } catch (Throwable e) {
      SSLResourceAccessor.log(Severity.WARNING, e, CLIENT_CERT_NOT_AVAILABLE);
      SSLResourceAccessor.traceThrowable(Severity.WARNING, "getClientCertificate() failed", e);
    }

    for (int i = 0; i < listener.size(); i++) {
      HandshakeCompletedListener l = (HandshakeCompletedListener) listener.elementAt(i);
      l.handshakeCompleted(event);
    }
  }

  protected SSLSocket(InetAddress host, int port, InetAddress clientHost, int clientPort, SSLContext context) throws IOException, java.net.UnknownHostException {
    iaikSocket = new iaik.security.ssl.SSLSocket(host, port, clientHost, clientPort, context);
    this.context = context;
  }

  protected SSLSocket(InetAddress host, int port, SSLContext context) throws IOException {
    iaikSocket = new iaik.security.ssl.SSLSocket(host, port, context);
    this.context = context;
  }

  protected SSLSocket(String host, int port, InetAddress clientHost, int clientPort, SSLContext context) throws IOException {
    iaikSocket = new iaik.security.ssl.SSLSocket(host, port, clientHost, clientPort, context);
    this.context = context;
  }

  protected SSLSocket(String host, int port, SSLContext context) throws IOException {
    iaikSocket = new iaik.security.ssl.SSLSocket(host, port, context);
    this.context = context;
  }

  /**
   *  Creates a socket layered over an existing socket to a ServerSocket on the named host,
   * at the given port. This constructor can be used when tunneling SSL through a proxy.
   * The host and port information are needed for session caching.
   *
   * @param proxySocket the socket connected to the proxy server
   * @param context the SSLContext for the new socket
   * @param host the name of the logical destination server
   * @param port the port of the logical destination server
   */
  protected SSLSocket(java.net.Socket proxySocket, String host, int port, boolean autoClose, SSLContext context) throws IOException {
    this.autoClose = autoClose;
    this.context = context;
    iaikSocket = new iaik.security.ssl.SSLSocket(proxySocket, context, host, port);
  }

  public void init(Socket s, SSLContext ctx, boolean useClientMode) throws IOException {
    innerSocket = s;
    iaikSocket.init(s, ctx, useClientMode);
    SessionManager sessionManager = ctx.getSessionManager();
    if (sessionManager instanceof JSSESessionManager) {
      ((JSSESessionManager) sessionManager).setResumePeriod(DispatcherService.RESUME_SESSION_PERIOD);
    } else {
      try {
        if (sessionManager instanceof com.sap.engine.services.ssl.factory.session.JSSELimitedCache) {
          ((com.sap.engine.services.ssl.factory.session.JSSELimitedCache) sessionManager).setSessionTimeout((int) DispatcherService.RESUME_SESSION_PERIOD);
        }
      } catch (NoClassDefFoundError e) {
//      $JL-EXC$
        //visual admin
      }
    }
  }

  public void startHandshake() throws IOException {
    try {
      if (!isSessionCreationEnabled) {
        throw new IOException(" Creation of SSL sessions is not allowed.");
      }

      iaikSocket.startHandshake();
      isHandshakeDone = true;
      notifyListener();
    } catch (IOException ioe) {
      SSLResourceAccessor.traceThrowable(Severity.INFO, "Handshake failed", ioe);
      throw ioe;
    } catch (Exception e) {
      SSLResourceAccessor.traceThrowable(Severity.INFO, "Handshake failed", e);
      throw new IOException("SSL Handshake failed: " + e);
    }
  }

  public void setUseClientMode(boolean clientMode) throws SSLConfigurationException {
    try {
      iaikSocket.setUseClientMode(clientMode);
    } catch (IOException e) {
      throw new SSLConfigurationException(BaseIOException.GENERAL_IO_EXCEPTION, e);
    }
  }

  public boolean getNeedClientAuth() {
    if (!iaikSocket.getUseClientMode()) {
      // server side
      try {
        SSLServerContext ctx = (SSLServerContext) iaikSocket.getContext();
        return ctx.getRequestClientCertificate();
      } catch (Exception e) {
        SSLResourceAccessor.traceThrowable(Severity.WARNING, "getNeedClientAuth() failed:", e);
      }
    }

    return false;
  }

  public void setEnabledCipherSuites(String[] suites) {
    context.setEnabledCipherSuiteList(CipherSuitesUtility.convertToList(suites));
    context.updateCipherSuites();
  }

  public void setEnableSessionCreation(boolean create) {
    isSessionCreationEnabled = create;
  }

  public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
    while (this.listener.removeElement(listener)) {
      ;
    }
  }

  public boolean getUseClientMode() {
    return iaikSocket.getUseClientMode();
  }

  public String[] getSupportedCipherSuites() {
    CipherSuiteList list = new CipherSuiteList(CipherSuiteList.L_IMPLEMENTED);
    String[] s = new String[list.size()];

    for (int i = 0; i < list.size(); i++) {
      s[i] = list.elementAt(i).getName();
    }

    return s;
  }

  private static final String GET_SESSION = "ssl_ssl_socket_get_new_session"; // new SSL session not created for unknown reason
  public synchronized javax.net.ssl.SSLSession getSession() {
    if (session == null) {
      try {
        sessionManager = (SSLSessionContext) context.getSessionManager();
        session = new SSLSession(iaikSocket.getSession(), sessionManager);
      } catch (Exception e) {
        SSLResourceAccessor.log(Severity.WARNING, e, GET_SESSION);
        SSLResourceAccessor.traceThrowable(Severity.WARNING, "getSession() ", e);
        SSLResourceAccessor.trace(Severity.WARNING, "sessionManager: " + sessionManager + ", session: " + session);
      }
    }

    return session;
  }

  public void setNeedClientAuth(boolean needed) {
    if (iaikSocket.getUseClientMode()) {
      throw new IllegalArgumentException("Not a server side socket");
    }

    SSLServerContext ctx = (SSLServerContext) iaikSocket.getContext();
    ctx.setRequestClientCertificate(needed);
  }

  public String[] getEnabledCipherSuites() {
    return CipherSuitesUtility.convertToStringArray(context.getEnabledCipherSuiteList());
  }

  public boolean getEnableSessionCreation() {
    return isSessionCreationEnabled;
  }

  public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
    this.listener.addElement(listener);
  }

  public InputStream getInputStream() throws IOException {
    if (!isHandshakeDone) {
      startHandshake();
    }

    return iaikSocket.getInputStream();
  }

  public OutputStream getOutputStream() throws IOException {
    if (!isHandshakeDone) {
      startHandshake();
    }

    return iaikSocket.getOutputStream();
  }

  public InetAddress getInetAddress() {
    return iaikSocket.getInetAddress();
  }

  public InetAddress getLocalAddress() {
    return iaikSocket.getLocalAddress();
  }

  public int getPort() {
    return iaikSocket.getPort();
  }

  public int getLocalPort() {
    return iaikSocket.getLocalPort();
  }

  public void setKeepAlive(boolean keepAlive) throws SocketException {
    if (innerSocket == null) {
      iaikSocket.setKeepAlive(keepAlive);
    } else {
      innerSocket.setKeepAlive(keepAlive);
    }
  }

  public boolean getKeepAlive() throws SocketException {
    if (innerSocket == null) {
      return iaikSocket.getKeepAlive();
    } else {
      return innerSocket.getKeepAlive();
    }
  }

  public void setTcpNoDelay(boolean on) throws SocketException {
    if (innerSocket == null) {
      iaikSocket.setTcpNoDelay(on);
    } else {
      innerSocket.setTcpNoDelay(on);
    }
  }

  public boolean getTcpNoDelay() throws SocketException {
    if (innerSocket == null) {
      return iaikSocket.getTcpNoDelay();
    } else {
      return innerSocket.getTcpNoDelay();
    }
  }

  public void setSoLinger(boolean on, int linger) throws SocketException {
    if (innerSocket == null) {
      iaikSocket.setSoLinger(on, linger);
    } else {
      innerSocket.setSoLinger(on, linger);
    }
  }

  public int getSoLinger() throws SocketException {
    if (innerSocket == null) {
      return iaikSocket.getSoLinger();
    } else {
      return innerSocket.getSoLinger();
    }
  }

  public void setSoTimeout(int timeout) throws SocketException {
    if (innerSocket == null) {
      iaikSocket.setSoTimeout(timeout);
    } else {
      innerSocket.setSoTimeout(timeout);
    }
  }

  public int getSoTimeout() throws SocketException {
    if (innerSocket == null) {
      return iaikSocket.getSoTimeout();
    } else {
      return innerSocket.getSoTimeout();
    }
  }

  public void setSendBufferSize(int size) throws SocketException {
    if (innerSocket == null) {
      iaikSocket.setSendBufferSize(size);
    } else {
      innerSocket.setSendBufferSize(size);
    }
  }

  public int getSendBufferSize() throws SocketException {
    if (innerSocket == null) {
      return iaikSocket.getSendBufferSize();
    } else {
      return innerSocket.getSendBufferSize();
    }
  }

  public void setReceiveBufferSize(int size) throws SocketException {
    if (innerSocket == null) {
      iaikSocket.setReceiveBufferSize(size);
    } else {
      innerSocket.setReceiveBufferSize(size);
    }
  }

  public int getReceiveBufferSize() throws SocketException {
    if (innerSocket == null) {
      return iaikSocket.getReceiveBufferSize();
    } else {
      return innerSocket.getReceiveBufferSize();
    }
  }

  public void close() throws IOException {
    if (autoClose) {
      iaikSocket.close();
    }
    isHandshakeDone = false;
  }

  public String toString() {
    return "Socket[addr=" + getInetAddress() + ",port=" + getPort() + ",localport=" + getLocalPort() + "]"; //iaikSocket.toString();
  }

  public static void setSocketImplFactory(SocketImplFactory fac) throws IOException {
    SSLSocket.setSocketImplFactory(fac);
  }

  SSLContext getSSLContext() {
    return context;
  }

  void setSSLContext(SSLContext context) {
    this.context = context;
  }

  public String[] getSupportedProtocols() {
    return CipherSuitesUtility.convertToStringArray(CipherSuitesUtility.getAllCipherSuites());
  }

  public String[] getEnabledProtocols() {
    return CipherSuitesUtility.convertToStringArray(context.getEnabledCipherSuites());
  }

  public void setEnabledProtocols(String[] protocols) {
    context.setEnabledCipherSuiteList(CipherSuitesUtility.convertToList(protocols));
  }

  public void setWantClientAuth(boolean flag) {
    // todo: jdk 1.4
  }

  public boolean getWantClientAuth() {
    // todo: jdk 1.4
    return false;
  }
}

