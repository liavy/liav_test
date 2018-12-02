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

import java.io.*;
import java.rmi.server.RMIServerSocketFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.SSLServerSocketFactory;
import iaik.security.ssl.ChainVerifier;
import iaik.security.ssl.SSLServerContext;
import iaik.security.jsse.net.JSSESessionManager;
import com.sap.engine.services.ssl.dispatcher.DispatcherService;
import com.sap.engine.services.ssl.dispatcher.CertificateExpirationTracker;
import com.sap.engine.services.ssl.keystore.KeyStoreConnector;
import com.sap.engine.services.ssl.util.CipherSuitesUtility;
import com.sap.engine.services.ssl.util.Utility;
import com.sap.engine.services.ssl.exception.LoggerPrintStream;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.tc.logging.Severity;

/**
 * This class generates server sockets and sets the credentials and the cipher suites for the context.
 *
 * @author Svetlana Stancheva
 * @version 4.0.2
 */
public class ServerSocketFactory extends SSLServerSocketFactory implements RMIServerSocketFactory {

  private static Vector activeServerSockets;
  private static ServerSocketFactory defaultFactory;
  private Properties properties;
  private SSLServerContext defaultContext;
  private SSLServerContext context;
  private Vector trustedCertificates = new Vector();
  private OutputStream out;
  private File file;
  private static boolean keyStoreIsReady = false;
  private static boolean propertiesLoaded = false;
  public static final String CERT_KEY = "factory.cert.";
  public static final String CIPHER_KEY = "factory.cipher.";
  public static final String TRUST_KEY = "factory.trust.";
  public static final String AUTH_KEY = "factory.need.auth";
  public static final String SOCKET_CONFIGURATION = "socketConfiguration.properties";


  public static final void stop() {
    activeServerSockets = null;
    defaultFactory = null;
    CipherSuitesUtility.stop();
  }

  private static synchronized Vector getActiveSockets() {
    if (activeServerSockets == null) {
      activeServerSockets = new Vector(10, 10);
    }

    return activeServerSockets;
  }

  /**
   * Creates ServerSocketFactory and initializes the default credentials
   */

  private static final String SOCKET_CFG_FILE_NOT_AVAILABLE = "ssl_srv_socket_factory_cfg_file_not_available";
  public ServerSocketFactory() {
    try {
      file = DispatcherService.getPersistentContainer().getPersistentEntryFile(SOCKET_CONFIGURATION, false);
      if (file == null) {
        ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        DispatcherService.getPersistentContainer().setPersistentEntryStream(SOCKET_CONFIGURATION, emptyStream, false);
        file = DispatcherService.getPersistentContainer().getPersistentEntryFile(SOCKET_CONFIGURATION, false);
      }
    } catch (Exception e) {
      SSLResourceAccessor.log(Severity.FATAL, e, SOCKET_CFG_FILE_NOT_AVAILABLE);
      SSLResourceAccessor.traceThrowable(Severity.FATAL, SOCKET_CFG_FILE_NOT_AVAILABLE, e);
      SSLResourceAccessor.trace(Severity.FATAL, "SSL port configuration file not found: " + file);
    } catch (NoClassDefFoundError e) {
      SSLResourceAccessor.log(Severity.FATAL, e, SOCKET_CFG_FILE_NOT_AVAILABLE);
      SSLResourceAccessor.traceThrowable(Severity.FATAL, SOCKET_CFG_FILE_NOT_AVAILABLE, e);
      SSLResourceAccessor.trace(Severity.FATAL, "SSL port configuration file not found: " + file);
    }
    getSocketsProperties();
    initializeCredentials();
  }

  /**
   * Adds a Credentials object with the specified keystoreAliase to the context and the defaultContext
   *
   * @param keystoreAliase  the name of the Credentials object to be added
   */
  public void addCredentials(String keystoreAliase) {
    addCredentials(new String[] {keystoreAliase});
  }

  /**
   * Adds Credentials objects with the specified keystoreAliase to the context and the defaultContext
   *
   * @param keystoreAliases  the names of the Credentials objects to be added
   */
  public void addCredentials(String[] keystoreAliases) {
    synchronized (context) {
      Credentials[] additional = KeyStoreConnector.getCredentials(keystoreAliases);

      for (int i = 0; i < additional.length; i++) {
        context.addServerCredentials(additional[i]);
        defaultContext.addServerCredentials(additional[i]);
        properties.setProperty(CERT_KEY + additional[i].getCertificateType(), keystoreAliases[i]);
      }

      store();
      context.setEnabledCipherSuites(CipherSuitesUtility.getAllCipherSuites());
      defaultContext.setEnabledCipherSuites(CipherSuitesUtility.getAllCipherSuites());
      context.updateCipherSuites();
      defaultContext.updateCipherSuites();
    }
    writeCipherSuites();
  }

  public void initCredentials(String keystoreAliase) {
    synchronized (context) {
      Credentials additional = KeyStoreConnector.getCredentials(keystoreAliase);

      if (additional != null) {
        context.addServerCredentials(additional);
        defaultContext.addServerCredentials(additional);
        properties.setProperty(CERT_KEY + additional.getCertificateType(), keystoreAliase);
        store();
        context.setEnabledCipherSuites(CipherSuitesUtility.getAllCipherSuites());
        defaultContext.setEnabledCipherSuites(CipherSuitesUtility.getAllCipherSuites());
        context.updateCipherSuites();
        defaultContext.updateCipherSuites();
      }
    }
  }

  /**
   * Adds a certificate object with the specified alias to the context and the defaultContext
   *
   * @param alias  the name of the certificate object to be added
   */
  public void addTrustedCertificates(String alias) {
    addTrustedCertificates(new String[] {alias});
  }

  /**
   * Adds trusted certificate objects with the specified aliases to the context and the defaultContext
   *
   * @param aliases  the names of the Certificate objects to be added
   */
  public Vector addTrustedCertificates(String[] aliases) {
    return addTrustedCertificates(aliases, true);
  }

  /**
   * Adds trusted certificate objects with the specified aliases to the context and the defaultContext
   *
   * @param aliases  the names of the Certificate objects to be added
   */
  public Vector addTrustedCertificates(String[] aliases, boolean store) {
    Vector addedCertificates = new Vector();
    int count;
    synchronized (context) {
      ChainVerifier chainVerifier = context.getChainVerifier();
      count = trustedCertificates.size();

      for (int i = 0; i < aliases.length; i++) {
        if ((aliases[i] == null) || (aliases[i].trim().length() == 0)) {
          chainVerifier.addTrustedCertificate(null);
          trustedCertificates.add("");
          addedCertificates.add("");
        } else {
          Certificate certificate = KeyStoreConnector.getCertificate(aliases[i]);

          if (!trustedCertificates.contains(aliases[i]) && (certificate != null) && (certificate instanceof X509Certificate)) {
            chainVerifier.addTrustedCertificate((X509Certificate) certificate);
            trustedCertificates.add(aliases[i]);
            addedCertificates.add(aliases[i]);
          }
        }
      }
    }

    if (store) {
      for (int i = 0; i < addedCertificates.size(); i++, count++) {
        properties.setProperty(TRUST_KEY + count, (String) addedCertificates.elementAt(i));
      }

      store();
    }

    return addedCertificates;
  }

  /**
   * Creates ServerSocket with the specified port and the context of this ServerSocketFactory
   *
   * @param port  the port of the created socket
   */
  public java.net.ServerSocket createServerSocket(int port) throws IOException {
    return new ServerSocket(this, port, (SSLServerContext) context.clone());
  }

  /**
   * Creates ServerSocket with the specified port, backlog and the context of this ServerSocketFactory
   *
   * @param port  the port of the created socket
   * @param backlog  the maximum possible number of connections for the created socket
   */
  public java.net.ServerSocket createServerSocket(int port, int backlog) throws IOException {
    return new ServerSocket(this, port, backlog, (SSLServerContext) context.clone());
  }

  /**
   * Creates ServerSocket with the specified port, backlog, address and the context of this ServerSocketFactory
   *
   * @param port  the port of the created socket
   * @param backlog  the maximum possible number of connections for the created socket
   * @param address  the InetAddress of the created socket
   */
  public java.net.ServerSocket createServerSocket(int port, int backlog, java.net.InetAddress address) throws IOException {
    return new ServerSocket(this, port, backlog, address, (SSLServerContext) context.clone());
  }

  public java.net.ServerSocket createServerSocket(java.net.ServerSocket lower) throws IOException {
    return new ServerSocket(this, lower, (SSLServerContext) context.clone());
  }

  public void freeSocket(ServerSocket socket) {
    synchronized (getActiveSockets()) {
      if (activeServerSockets.contains(socket)) {
        activeServerSockets.remove(socket);
      }
    }
  }

  /**
   * Gets the activeServerSockets for the ServerSocketFactory
   *
   * @return  a string array, containing pairs of addresses and ports
   */
  public static String[][] getActiveServerSockets() {
    synchronized (getActiveSockets()) {
      int length = activeServerSockets.size();
      String[][] sockets = new String[length][2];
      ServerSocket socket = null;

      for (int i = 0; i < length; i++) {
        socket = (ServerSocket) activeServerSockets.elementAt(i);
        sockets[i][0] = socket.getInetAddress().getHostAddress();
        sockets[i][1] = "" + socket.getLocalPort();
      }

      return sockets;
    }
  }

  public int[] getAllowedCertificateTypes() {
    return context.getAllowedCertificateTypes();
  }

  /**
   * Gets the credentials from the context
   *
   * @return  a string array with the credentials
   */
  public String[] getCredentials() {
    int[] types = context.getAllowedCertificateTypes();
    Vector credentials = new Vector(6, 10);
    String[] aliases = null;

    for (int i = 0; i < types.length; i++) {
      try {
        credentials.add(context.getServerCredentials(types[i]));
      } catch (NullPointerException e) {
        // IAIK throws it when the context has no credentials for the type
        continue;
      }
    }

    aliases = new String[credentials.size()];

    for (int i = 0; i < aliases.length; i++) {
      aliases[i] = ((Credentials) credentials.elementAt(i)).getAlias();
    }

    return aliases;
  }

  /**
   * Gets the certificates from the context
   *
   * @return  a string array with the certificates
   */
  public String[] getTrustedCertificates() {
    String[] certificates = new String[trustedCertificates.size()];

    for (int i = 0; i < certificates.length; i++) {
      certificates[i] = (String) trustedCertificates.elementAt(i);
    }

    return certificates;
  }

  public String[] getAvailableCertificates() {
    return KeyStoreConnector.getCertificates();
  }

  public SSLServerContext getContext() {
    return (SSLServerContext) context.clone();
  }

  public boolean getNeedClientAuth() {
    return context.getRequestClientCertificate();
  }

  /**
   * Gets the default server socket factory
   *
   * @return the default server socket factory
   */
  public synchronized static javax.net.ServerSocketFactory getDefault() {
    if (defaultFactory == null) {
      defaultFactory = new ServerSocketFactory();
    }

    return defaultFactory;
  }

  /**
   * Gets a string representation of the default cipher suites
   *
   * @return  a string array with the default cipher suites
   */
  public String[] getDefaultCipherSuites() {
    return CipherSuitesUtility.convertToStringArray(context.getEnabledCipherSuiteList());
  }

  /**
   * Gets a server socket at the specified host and port from activeServerSockets
   *
   * @param host  the host of the wanted socket
   * @param port  the port of the wanted socket
   *
   * @return the server socket at the specified host and port if it is among the activeServerSockets
   */
  public static ServerSocket getServerSocket(String host, int port) {
    synchronized (getActiveSockets()) {
      int length = activeServerSockets.size();
      ServerSocket socket = null;

      for (int i = 0; i < length; i++) {
        socket = (ServerSocket) activeServerSockets.elementAt(i);

        if ((socket.getLocalPort() == port) && socket.getInetAddress().getHostAddress().equals(host)) {
          return socket;
        }
      }
    }
    return null;
  }

  /**
   * Gets a string representation of the supported cipher suites from the default context
   *
   * @return  a string array with the supported cipher suites
   */
  public String[] getSupportedCipherSuites() {
    return CipherSuitesUtility.convertToStringArray(defaultContext.getEnabledCipherSuiteList());
  }

  /**
   * Initializes the defaultContext, the context and the defaultCredentials
   */
  private void initializeCredentials() {
    defaultContext = new SSLServerContext();
    context = new SSLServerContext();
    defaultContext.setDebugStream(new LoggerPrintStream());

    context.setSessionManager(new JSSESessionManager());
    context.setDebugStream(new LoggerPrintStream());

  }

  public static boolean isKeyStoreReady() {
    return keyStoreIsReady;
  }

  public static boolean getPropertiesLoaded() {
    return propertiesLoaded;
  }

  public void registerSocket(ServerSocket socket) {
    synchronized (getActiveSockets()) {
      if (!activeServerSockets.contains(socket)) {
        activeServerSockets.add(socket);
      }
    }
  }

  /**
   * Removes a single credential with the specified keystoreAliase
   *
   * @param keystoreAliase  the name of the credential to be removed
   */
  public void removeCredentials(String keystoreAliase) {
    removeCredentials(new String[] {keystoreAliase});
  }

  /**
   * Removes a couple of credentials with the specified keystore aliases
   *
   * @param keystoreAliases  the names of the credentials to be removed
   */
  public void removeCredentials(String[] keystoreAliases) {
    setCredentials(Utility.remove(keystoreAliases, getCredentials()));
  }

  /**
   * Removes a certificate with the specified alias
   *
   * @param alias  the name of the certificate to be removed
   */
  public void removeTrustedCertificates(String alias) {
    removeTrustedCertificates(new String[] {alias});
  }

  /**
   * Removes a couple of certificates with the specified keystore aliases
   *
   * @param aliases  the names of the certificates to be removed
   */
  public void removeTrustedCertificates(String[] aliases) {
    Certificate[] certificates = KeyStoreConnector.getCertificates(aliases);

    for (int i = 0; i < aliases.length; i++) {
      if ((aliases[i] == null) || (aliases[i].trim().length() == 0)) {
        context.getChainVerifier().removeTrustedCertificate(null);
        trustedCertificates.remove("");
      }
    }

    if (certificates != null) {
      synchronized (context) {
        ChainVerifier chainVerifier = context.getChainVerifier();

        for (int i = 0; i < certificates.length; i++) {
          if (certificates[i] instanceof X509Certificate) {
            int index;
            chainVerifier.removeTrustedCertificate((X509Certificate) certificates[i]);
            index = trustedCertificates.indexOf(aliases[i]);
            trustedCertificates.remove(aliases[i]);

            if (index >= 0) {
              int ind = index;
              Object value;

              do {
                value = properties.remove(TRUST_KEY + ind);
                ind++;
              } while (value != null);

              for (; index < trustedCertificates.size(); index++) {
                properties.setProperty(TRUST_KEY + index, (String) trustedCertificates.elementAt(index));
              }

              store();
            }
          }
        }
      }
    }
  }

  /**
   * Sets the credentials with the specified keystore aliases for the context and the defaultContext
   *
   * @param keystoreAliases  the names of the credentials to be set
   */
  public void setCredentials(String[] keystoreAliases) {
    synchronized (defaultContext) {
      context.clearServerCredentials();
      defaultContext.clearServerCredentials();
      removeCredentialsFromProperties();
      addCredentials(keystoreAliases);
    }
  }

  /**
   * Sets the default credentials with the specified keystore aliases and the enabled cipher suites for the defaultContext
   *
   * @param keystoreAliases  the names of the credentials to be set
   */
  public void setDefaultCredentials(String[] keystoreAliases) {
    synchronized (defaultContext) {
      removeCredentialsFromProperties();
      Credentials[] additional = KeyStoreConnector.getCredentials(keystoreAliases);

      for (int i = 0; i < additional.length; i++) {
        defaultContext.addServerCredentials(additional[i]);
        properties.setProperty(CERT_KEY + additional[i].getCertificateType(), keystoreAliases[i]);
      }

      store();
      defaultContext.setEnabledCipherSuites(CipherSuitesUtility.getAllCipherSuites());
      defaultContext.updateCipherSuites();
      writeCipherSuites();
    }
  }

  /**
   * Sets the default cipher suites for the context
   *
   * @param suites  the names of the cipher suites to be set as default for the context
   */
  public void setDefaultCipherSuites(String[] suites) {
    context.setEnabledCipherSuiteList(CipherSuitesUtility.convertToList(suites));
    context.updateCipherSuites();
    writeCipherSuites();
  }

  public void setNeedClientAuth(boolean flag) {
    context.setRequestClientCertificate(flag);
    properties.setProperty(AUTH_KEY, "" + flag);
    store();
  }

  public static void setKeyStoreIsReady(boolean ready) {
    keyStoreIsReady = ready;
  }


  private static final String SSL_SOCKET_CONFIGURATION_NOT_LOADED = "ssl_srv_socket_factory_socket_cfg_not_loaded";
  private static final String SSL_SOCKET_CFG_FILE_NOT_CREATED = "ssl_srv_socket_factory_cfg_file_not_created";
  public Properties getSocketsProperties() {
    synchronized (this) {
      if (properties == null) {
        try {
          properties = new Properties();
          if (file.exists()) {
            properties.load(new FileInputStream(file));
            propertiesLoaded = true;
          } else {
            if (!file.createNewFile()) {
              SSLResourceAccessor.log(Severity.FATAL, "", SSL_SOCKET_CFG_FILE_NOT_CREATED);
            }
          }
          synchVersionOfProperties();
        } catch (Exception e) {
          SSLResourceAccessor.log(Severity.FATAL, e, SSL_SOCKET_CONFIGURATION_NOT_LOADED);
          SSLResourceAccessor.traceThrowable(Severity.ERROR, "Unable to load SSL port configuration.", e);
        }
      }
    }

    return (properties != null) ? properties : new Properties();
  }


  private static final String SSL_SOCKET_CONFIGURATION_NOT_SAVED = "ssl_srv_socket_factory_socket_modification_not_stored";
  void store() {
    try {
      file.delete();
      out = new FileOutputStream(file);
      properties.store(out, "");
      out.close();
      DispatcherService.getPersistentContainer().setPersistentEntryFile(SOCKET_CONFIGURATION, file, false);
      CertificateExpirationTracker.portsConfigurationChanged();
    } catch (Exception e) {
      SSLResourceAccessor.log(Severity.FATAL, e, SSL_SOCKET_CONFIGURATION_NOT_SAVED);
      SSLResourceAccessor.traceThrowable(Severity.ERROR, "Unable to save SSL port configuration",  e);
    }
  }

  private void removeCredentialsFromProperties() {
    int[] types = getAllowedCertificateTypes();

    for (int i = 0; i < types.length; i++) {
      properties.remove(CERT_KEY + types[i]);
    }

    store();
  }

  private void writeCipherSuites() {
    int count = 0;
    Object value = null;

    do {
      value = properties.remove(CIPHER_KEY + count);
      count++;
    } while (value != null);


    String[] cipherSuites = getDefaultCipherSuites();

    for (int i = 0; i < cipherSuites.length; i++) {
      properties.setProperty(CIPHER_KEY + i, cipherSuites[i]);
    }

    store();
  }
  private static final String UNEXPECTED_CONFIGURATION_PROBLEM = "ssl_srv_socket_factory_socket_update_problem";
  private void synchVersionOfProperties() {
    final String VERSION = "Version";

    double version = 0;
    try {
      version = Double.valueOf(properties.getProperty(VERSION)).doubleValue();
    } catch (Exception e) {
      // version is still 0
      SSLResourceAccessor.traceThrowable(Severity.INFO,  "getCurrentVersion():", e);
    }

    if (version < 6.20) {
      try {
        SSLResourceAccessor.trace(Severity.FATAL, "Updating socket configuration for the current version.");
        int count = 0;
        int position = 0;
        String key = null;
        String entry = null;
        Object[] keys = properties.keySet().toArray();

        for (int i = 0; i < keys.length; i++) {
          key = (String) keys[i];
          position = key.indexOf(".need.auth");

          if (position >= 0) {
            count = 0;
            entry = key.substring(0, position) + ".trust.";

            while (properties.getProperty(entry + count) != null) {
              if (properties.getProperty(entry + count).trim().length() == 0) {
                break;
              } else {
                count++;
              }
            }

            if (properties.getProperty(entry + count) == null) {
              properties.setProperty(entry + count, "");
            }
          }
        }

        properties.setProperty(VERSION, "6.20");
        store();
        SSLResourceAccessor.trace(Severity.FATAL, "Successfully updated socket configuration for the current version.");
      } catch (Throwable t) {
        SSLResourceAccessor.log(Severity.FATAL, t, UNEXPECTED_CONFIGURATION_PROBLEM);
        SSLResourceAccessor.traceThrowable(Severity.FATAL, "Unable to update SSL port configuration.", t);
      }
    }
  }
}

