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

import java.io.IOException;
import java.net.*;
import java.util.*;
import iaik.security.ssl.SSLServerContext;
import com.sap.engine.services.ssl.keystore.KeyStoreConnector;
import com.sap.engine.services.ssl.exception.LoggerPrintStream;
import com.sap.engine.services.ssl.exception.BaseIOException;
import com.sap.engine.services.ssl.exception.SSLConfigurationException;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.engine.services.ssl.util.CipherSuitesUtility;
import com.sap.tc.logging.Severity;

public class ServerSocket extends javax.net.ssl.SSLServerSocket {

  private boolean useClientMode = false;
  private boolean isSessionCreationEnabled = true;
  private java.net.InetAddress inetAddress = null;
  private java.net.ServerSocket server = null;
  private ClientSocketFactory socketFactory = null;
  private String host = null;
  private int port;
  private ServerSocketFactory factory = null;
  private String[] trustedCertificates = new String[0];
  private Properties properties = null;
  private SSLServerContext context;
  private boolean newContextSet = false;
  private String certKey;
  private String cipherKey;
  private String trustKey;
  private String authKey;

  protected ServerSocket(ServerSocketFactory factory, int port, SSLServerContext ctx) throws IOException {
    super(port);
    this.host = "localhost";
    this.port = port;
    this.factory = factory;
    factory.registerSocket(this);
    context = ctx;
    socketFactory = new ClientSocketFactory(context);
    init();

    try {
      initialize(true);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
  }

  protected ServerSocket(ServerSocketFactory factory, int port, int backlog, SSLServerContext ctx) throws IOException {
    super(port, backlog);
    this.host = "localhost";
    this.port = port;
    this.factory = factory;
    factory.registerSocket(this);
    context = ctx;
    socketFactory = new ClientSocketFactory(context);
    init();

    try {
      initialize(true);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
  }

  protected ServerSocket(ServerSocketFactory factory, int port, int backlog, InetAddress address, SSLServerContext ctx) throws IOException {
    super(port, backlog, address);
    this.host = address.getHostAddress();
    this.port = port;
    this.factory = factory;
    factory.registerSocket(this);
    context = ctx;
    socketFactory = new ClientSocketFactory(context);
    init();

    try {
      initialize(true);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
  }

  protected ServerSocket(ServerSocketFactory factory, java.net.ServerSocket server, SSLServerContext ctx) throws IOException {
    super(0);
    super.close();
    this.factory = factory;
    this.server = server;
    this.host = server.getInetAddress().getHostAddress();
    this.port = server.getLocalPort();
    factory.registerSocket(this);
    context = ctx;
    socketFactory = new ClientSocketFactory(context);
    init();

    try {
      initialize(true);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
  }

  public Socket accept() throws IOException {
    Socket socket = null;
    SSLSocket sslSocket = null;

    try {
      if (server == null) {
        socket = super.accept();
      } else {
        socket = server.accept();
      }

      sslSocket = (SSLSocket) socketFactory.createSocket(socket, socket.getInetAddress(), socket.getPort(), false);
      sslSocket.init(socket, sslSocket.getSSLContext(), false);
      sslSocket.setEnableSessionCreation(isSessionCreationEnabled);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }

    return sslSocket;
  }

  public void addCredentials(String keystoreAlias) {
    socketFactory.addCredentials(keystoreAlias);
    writeCredentials(keystoreAlias);
    writeEnabledCipherSuites();
  }

  public void addCredentials(String[] keystoreAliases) {
    socketFactory.addCredentials(keystoreAliases);

    for (int i = 0; i < keystoreAliases.length; i++) {
      writeCredentials(keystoreAliases[i]);
    }

    writeEnabledCipherSuites();
  }

  public void addTrustedCertificates(String alias) {
    String key;
    synchronized (trustedCertificates) {
      for (int i = 0; i < trustedCertificates.length; i++) {
        if (trustedCertificates[i].equals(alias)) {
          return;
        }
      }

      if (socketFactory.addTrustedCertificates(alias, (trustedCertificates.length == 0))) {
        String[] temp = trustedCertificates;
        trustedCertificates = new String[trustedCertificates.length + 1];
        System.arraycopy(temp, 0, trustedCertificates, 0, temp.length);
        trustedCertificates[temp.length] = alias;
        key = trustKey + temp.length;
        properties.setProperty(key, alias);
        factory.store();
      }
    }
  }

  public void addTrustedCertificates(String[] aliases) {
    for (int i = 0; i < aliases.length; i++) {
      addTrustedCertificates(aliases[i]);
    }
  }

  public void close() throws IOException {
    if (server != null) {
      server.close();
    } else {
      super.close();
    }

    factory.freeSocket(this);
  }

  public String[] getCredentials() {
    return socketFactory.getCredentials();
  }

  public String[] getAvailableCertificates() {
    return socketFactory.getAvailableCertificates();
  }

  public String[] getTrustedCertificates() {
    return trustedCertificates;
  }

  public String[] getEnabledCipherSuites() {
    return socketFactory.getEnabledCipherSuites();
  }

  public boolean getEnableSessionCreation() {
    return isSessionCreationEnabled;
  }

  public InetAddress getInetAddress() {
    if (inetAddress == null) {
      if (server != null) {
        inetAddress = server.getInetAddress();
      } else {
        inetAddress = super.getInetAddress();
      }

      if ("0.0.0.0".equals(inetAddress.getHostAddress())) {
        try {
          inetAddress = InetAddress.getLocalHost();
        } catch (Exception e) {
          SSLResourceAccessor.trace(Severity.WARNING, "getInetAddress(): " + e);
          SSLResourceAccessor.traceThrowable(Severity.WARNING, "ServerSocket.getInetAddress()", e);
          /////
          //  leave it be "0.0.0.0"
          return inetAddress;
        }
      }
    }

    return inetAddress;
  }

  public int getLocalPort() {
    if (server != null) {
      return server.getLocalPort();
    } else {
      return super.getLocalPort();
    }
  }

  public boolean getNeedClientAuth() {
    return socketFactory.getNeedClientAuth();
  }

  public int getSoTimeout() throws IOException {
    if (server != null) {
      return server.getSoTimeout();
    } else {
      return super.getSoTimeout();
    }
  }

  public String[] getSupportedCipherSuites() {
    return socketFactory.getSupportedCipherSuites();
  }

  public boolean getUseClientMode() {
    return useClientMode;
  }

  public void initCredentials(String[] keystoreAliases) {
    for (int i = 0; i < keystoreAliases.length; i++) {
      initCredentials(keystoreAliases[i]);
    }
  }

  public void initCredentials(String keystoreAliase) {
    socketFactory.addCredentials(keystoreAliase);
    writeCredentials(keystoreAliase);
  }

  public void removeCredentials(String keystoreAlias) {
    socketFactory.removeCredentials(keystoreAlias);
    removeCredentialsFromProperties(keystoreAlias);
    writeEnabledCipherSuites();
  }

  public void removeCredentials(String[] keystoreAliases) {
    socketFactory.removeCredentials(keystoreAliases);

    for (int i = 0; i < keystoreAliases.length; i++) {
      removeCredentialsFromProperties(keystoreAliases[i]);
    }

    writeEnabledCipherSuites();
  }

  public void removeTrustedCertificates(String alias) {
    synchronized (trustedCertificates) {
      if ((alias == null) || (alias.trim().length() == 0)) {
        context.getChainVerifier().removeTrustedCertificate(null);
      }

      for (int i = 0; i < trustedCertificates.length; i++) {
        if (trustedCertificates[i].equals(alias)) {
          String[] temp = trustedCertificates;
          trustedCertificates = new String[trustedCertificates.length - 1];
          System.arraycopy(temp, 0, trustedCertificates, 0, i);
          System.arraycopy(temp, (i + 1), trustedCertificates, i, trustedCertificates.length - i);
          int ind = i;
          String key = trustKey;
          Object value;

          do {
            value = properties.remove(key + ind);
            ind++;
          } while (value != null);

          for (; i < trustedCertificates.length; i++) {
            properties.setProperty(key + i, trustedCertificates[i]);
          }

          factory.store();
          break;
        }
      }

      socketFactory.removeTrustedCertificates(alias, (trustedCertificates.length == 0));
    }
  }

  public void removeTrustedCertificates(String[] aliases) {
    synchronized (trustedCertificates) {
      for (int j = 0; j < aliases.length; j++) {
        for (int i = 0; i < aliases.length; i++) {
          if ((aliases[i] == null) || (aliases[i].trim().length() == 0)) {
            context.getChainVerifier().removeTrustedCertificate(null);
          }
        }

        for (int i = 0; i < trustedCertificates.length; i++) {
          if (trustedCertificates[i].equals(aliases[j])) {
            String[] temp = trustedCertificates;
            trustedCertificates = new String[trustedCertificates.length - 1];
            System.arraycopy(temp, 0, trustedCertificates, 0, i);
            System.arraycopy(temp, (i + 1), trustedCertificates, i, trustedCertificates.length - i);
            int ind = i;
            String key = trustKey;
            Object value;

            do {
              value = properties.remove(key + ind);
              ind++;
            } while (value != null);

            for (; i < trustedCertificates.length; i++) {
              properties.setProperty(key + i, trustedCertificates[i]);
            }

            factory.store();
            break;
          }
        }
      }

      socketFactory.removeTrustedCertificates(aliases, (trustedCertificates.length == 0));
    }
  }

  public void setCredentials(String[] keystoreAliases) {
    socketFactory.setCredentials(keystoreAliases);
    removeCredentialsFromProperties();

    for (int i = 0; i < keystoreAliases.length; i++) {
      writeCredentials(keystoreAliases[i]);
    }

    writeEnabledCipherSuites();
  }

  public void setEnabledCipherSuites(String[] suites) {
    socketFactory.setEnabledCipherSuites(suites);
    writeEnabledCipherSuites();
  }

  public void setEnableSessionCreation(boolean flag) {
    isSessionCreationEnabled = flag;
  }

  public void setNeedClientAuth(boolean flag) {
    socketFactory.setNeedClientAuth(flag);
    properties.setProperty(authKey, "" + flag);
    factory.store();
  }

  public void setSoTimeout(int timeout) throws SocketException {
    if (server != null) {
      server.setSoTimeout(timeout);
    } else {
      super.setSoTimeout(timeout);
    }
  }

  public void setUseClientMode(boolean flag) {
    useClientMode = flag;
  }

  public String toString() {
    return "SSLServerSocket[host=" + host + ",port=" + port + "]";
  }

  public void initialize(boolean contextInitialized) {
    boolean propertiesFound = false;
    int trustedCertificates = 0;

    if (ServerSocketFactory.isKeyStoreReady()) {
      if (ServerSocketFactory.getPropertiesLoaded()) {
        int[] types = factory.getAllowedCertificateTypes();
        String value;
        int count = 0;
        String[] ciphers = new String[0];

        for (int i = 0; i < types.length; i++) {
          value = properties.getProperty(certKey + types[i], "");

          if (!"".equals(value)) {
            if (!newContextSet) {
              newContextSet = true;
              context = new SSLServerContext();
              context.setDebugStream(new LoggerPrintStream());
            }

            initCredentials(value);
            propertiesFound = true;
          }
        }

        do {
          value = properties.getProperty(cipherKey + count, "");

          if (!"".equals(value)) {
            if (!newContextSet) {
              newContextSet = true;
              context = new SSLServerContext();
              context.setDebugStream(new LoggerPrintStream());
            }

            String[] temp = ciphers;
            ciphers = new String[ciphers.length + 1];
            System.arraycopy(temp, 0, ciphers, 0, temp.length);
            ciphers[temp.length] = value;
            propertiesFound = true;
          }

          count++;
        } while (!"".equals(value));

        if (propertiesFound) {
          setEnabledCipherSuites(ciphers);
        }

        count = 0;
        trustedCertificates = 0;

        do {
          value = properties.getProperty(trustKey + count);

          if (value != null) {
            if (!newContextSet) {
              newContextSet = true;
              context = new SSLServerContext();
              context.setDebugStream(new LoggerPrintStream());
            }

            addTrustedCertificates(value);
            propertiesFound = true;

            if (value.trim().length() > 0) {
              trustedCertificates++;
            }
          }

          count++;
        }  while (value != null);

        if (trustedCertificates == 0) {
          setNeedClientAuth(false);
        } else {
          value = properties.getProperty(authKey);

          if (value != null) {
            if (!newContextSet) {
              newContextSet = true;
              context = new SSLServerContext();
              context.setDebugStream(new LoggerPrintStream());
            }

            if ("false".equals(value)) {
              setNeedClientAuth(false);
              propertiesFound = true;
            } else {
              setNeedClientAuth(true);
            }
          }
        }

        if (!propertiesFound) {
          if (contextInitialized) {
            String[] credentials = getCredentials();

            for (int i = 0; i < credentials.length; i++) {
              writeCredentials(credentials[i]);
            }

            writeEnabledCipherSuites();
            String[] trusted = getTrustedCertificates();

            for (int i = 0; i < trusted.length; i++) {
              properties.setProperty(trustKey + i, trusted[i]);
            }

            properties.setProperty(authKey, "" + getNeedClientAuth());
            factory.store();
          } else {
            initCredentials(factory.getCredentials());
            setEnabledCipherSuites(factory.getDefaultCipherSuites());
            setNeedClientAuth(factory.getNeedClientAuth());
          }

          addTrustedCertificates(factory.getTrustedCertificates());
        }
      }
    }
  }

  private void init() {
    properties = factory.getSocketsProperties();
    certKey = host + ":" + port + ".cert.";
    cipherKey = host + ":" + port + ".cipher.";
    trustKey = host + ":" + port + ".trust.";
    authKey = host + ":" + port + ".need.auth";
  }

  private void writeEnabledCipherSuites() throws SSLConfigurationException {
    String[] cipherSuites = getEnabledCipherSuites();

    if (cipherSuites.length == 0) {
      throw new SSLConfigurationException(SSLConfigurationException.NO_ENABLED_CIPHER_SUITES);
    }

    int index = 0;
    Object value;
    do {
      value = properties.remove(cipherKey + index);
      index++;
    } while (value != null);

    for (int i = 0; i < cipherSuites.length; i++) {
      properties.setProperty(cipherKey + i, cipherSuites[i]);
    }

    factory.store();
  }

  private void writeCredentials(String keystoreAlias) {
    Credentials certificate = KeyStoreConnector.getCredentials(keystoreAlias);
    properties.setProperty(certKey + certificate.getCertificateType(), keystoreAlias);
    factory.store();
  }

  private void removeCredentialsFromProperties(String keystoreAlias) {
    Credentials credentials = KeyStoreConnector.getCredentials(keystoreAlias);
    properties.remove(certKey + credentials.getCertificateType());
    factory.store();
  }

  private void removeCredentialsFromProperties() {
    int[] types = factory.getAllowedCertificateTypes();

    for (int i = 0; i < types.length; i++) {
      properties.remove(certKey + types[i]);
    }

    factory.store();
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

