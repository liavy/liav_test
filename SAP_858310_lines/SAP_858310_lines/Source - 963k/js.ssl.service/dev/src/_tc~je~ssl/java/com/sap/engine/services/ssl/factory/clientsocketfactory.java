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
import java.net.InetAddress;
import java.rmi.server.RMIClientSocketFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Vector;
import java.lang.reflect.InvocationTargetException;
import javax.net.ssl.SSLSocketFactory;
import iaik.security.jsse.net.JSSESessionManager;
import iaik.security.ssl.ChainVerifier;
import iaik.security.ssl.SSLServerContext;
import iaik.security.ssl.CipherSuite;
import iaik.security.ssl.SessionManager;
import com.sap.engine.services.ssl.keystore.KeyStoreConnector;
import com.sap.engine.services.ssl.util.CipherSuitesUtility;
import com.sap.engine.services.ssl.util.Utility;
import com.sap.engine.services.ssl.exception.LoggerPrintStream;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.tc.logging.Severity;

/**
 * This class generates client sockets and sets the credentials and the cipher suites for the context.
 *
 * @author Svetlana Stancheva
 * @version 4.0.2
 */
public class ClientSocketFactory extends SSLSocketFactory implements RMIClientSocketFactory {


  private SSLServerContext defaultContext;
  private SSLServerContext context;
  private static ClientSocketFactory defaultFactory;

  public static void stop() {
    defaultFactory = null;
    CipherSuitesUtility.stop();
  }

  /**
   * Creates ClientSocketFactory with the specified context and initializes the default credentials
   *
   * @param ctx  the SSLServerContext to be set as context for this ClientSocketFactory
   */
  public ClientSocketFactory(SSLServerContext ctx) throws IOException {
    initializeCredentials();
    context = ctx;
    SessionManager manager = null;
    try {
      manager = (SessionManager) Class.forName("com.sap.engine.services.ssl.factory.session.JSSELimitedCache").getDeclaredMethod("getInstance", new Class[0]).invoke(null, null);
    } catch (ClassNotFoundException e) {
      //$JL-EXC$
      //visual admin
    } catch (NoClassDefFoundError e) {
//    $JL-EXC$
      //visual admin
    } catch (NoSuchMethodException e) {
//    $JL-EXC$
      //visual admin
    } catch (IllegalAccessException e) {
//    $JL-EXC$
      //visual admin
    } catch (InvocationTargetException e) {
//    $JL-EXC$
      //visual admin
    }
    if (manager == null) {
      manager = new JSSESessionManager();
    }
    context.setDebugStream(new LoggerPrintStream());
    context.setSessionManager(manager);//new JSSESessionManager());
    context.setChainVerifier(null);
  }

  /**
   * Adds a Credentials object with the specified keystoreAliase to the context and the defaultContext
   *
   * @param keystoreAliase  the name of the Credentials object to be added
   */
  public void addCredentials(String keystoreAliase)  {
    addCredentials(new String[] {keystoreAliase});
  }

  private static final String NO_INITIAL_CIPHER_SUITES = "ssl_client_socket_factory_no_initial_cipher_suites"; //"No available cipher suites found for the socket."
  private static final String NO_AVAILABLE_CIPHER_SUITES = "ssl_client_socket_factory_no_available_cipher_suites"; //"No available cipher suites found for the socket."

  /**
   * Adds Credentials objects with the specified keystoreAliase to the context and the defaultContext
   *
   * @param keystoreAliases  the names of the Credentials objects to be added
   */
  public void addCredentials(String[] keystoreAliases) {
    synchronized (context) {
      Credentials[] additional = null;

      additional = KeyStoreConnector.getCredentials(keystoreAliases);

      for (int i = 0; i < additional.length; i++) {
        context.addServerCredentials(additional[i]);
        defaultContext.addServerCredentials(additional[i]);
        context.addClientCredentials(additional[i]);
        defaultContext.addClientCredentials(additional[i]);
      }
      CipherSuite[] suites = CipherSuitesUtility.getAllCipherSuites();
      if (suites.length == 0) {
        SSLResourceAccessor.log(Severity.ERROR, "", NO_INITIAL_CIPHER_SUITES);
        return;
      }

      context.setEnabledCipherSuites(suites);
      defaultContext.setEnabledCipherSuites(suites);
      context.updateCipherSuites();
      defaultContext.updateCipherSuites();

      if (context.getEnabledCipherSuites().length == 0) {
        SSLResourceAccessor.log(Severity.ERROR, "", NO_AVAILABLE_CIPHER_SUITES);
      }
    }
  }

  /**
   * Adds a Certificate object with the specified alias to the context
   *
   * @param alias  the name of the Certificate object to be added
   */
  public boolean addTrustedCertificates(String alias, boolean newCnainVerifierNeeded) {
    synchronized (context) {
      if ((alias == null) || (alias.trim().length() == 0)) {
        if (newCnainVerifierNeeded) {
          context.setChainVerifier(new ChainVerifier());
        }
        context.getChainVerifier().addTrustedCertificate(null);
        return true;
      } else {
        Certificate certificate = KeyStoreConnector.getCertificate(alias);

        if ((certificate != null) && (certificate instanceof X509Certificate)) {
          if (newCnainVerifierNeeded) {
            context.setChainVerifier(new ChainVerifier());
          }

          context.getChainVerifier().addTrustedCertificate((X509Certificate) certificate);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Creates SSLSocket with the specified host, port and the context of this ClientSocketFactory
   *
   * @param host  the host of the created socket
   * @param port  the port of the created socket
   */
  public java.net.Socket createSocket(InetAddress host, int port) throws IOException {
    return new SSLSocket(host, port, (SSLServerContext) context.clone());
  }

  /**
   * Creates SSLSocket with the specified host, port and the context of this ClientSocketFactory
   *
   * @param host  the host of the created socket
   * @param port  the port of the created socket
   */
  public java.net.Socket createSocket(String host, int port) throws IOException {
    return new SSLSocket(host, port, (SSLServerContext) context.clone());
  }

  /**
   * Creates SSLSocket with the specified host, port, clientHost, clientPort
   * and the context of this ClientSocketFactory
   *
   * @param host  the host of the created socket
   * @param port  the port of the created socket
   * @param clientHost  the clientHost of the created socket
   * @param clientPort  the clientPort of the created socket
   */
  public java.net.Socket createSocket(InetAddress host, int port, InetAddress clientHost, int clientPort) throws IOException {
    return new SSLSocket(host, port, clientHost, clientPort, (SSLServerContext) context.clone());
  }

  /**
   * Creates SSLSocket with the specified host, port, clientHost, clientPort
   * and the context of this ClientSocketFactory
   *
   * @param host  the host of the created socket
   * @param port  the port of the created socket
   * @param clientHost  the clientHost of the created socket
   * @param clientPort  the clientPort of the created socket
   */
  public java.net.Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
    return new SSLSocket(host, port, clientHost, clientPort, (SSLServerContext) context.clone());
  }

  /**
   * Creates SSLSocket with the specified socket, host, port, autoClose option
   * and the context of this ClientSocketFactory
   *
   * @param host  the host of the created socket
   * @param port  the port of the created socket
   * @param autoClose  spesifies whether the created socket can close automaticaly
   */
  public java.net.Socket createSocket(java.net.Socket socket, InetAddress host, int port, boolean autoClose) throws IOException {
    return new SSLSocket(socket, host.getHostAddress(), port, autoClose, (SSLServerContext) context.clone());
  }

  /**
   * Creates SSLSocket with the specified socket, host, port, autoClose option
   * and the context of this ClientSocketFactory
   *
   * @param host  the host of the created socket
   * @param port  the port of the created socket
   * @param autoClose  spesifies whether the created socket can close automaticaly
   */
  public java.net.Socket createSocket(java.net.Socket socket, String host, int port, boolean autoClose) throws IOException {
    return new SSLSocket(socket, host, port, autoClose, (SSLServerContext) context.clone());
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
   * Gets a string representation of the default cipher suites
   *
   * @return  a string array with the default cipher suites
   */
  public String[] getDefaultCipherSuites() {
    return getEnabledCipherSuites();
  }

  private static final String GET_DEFAULT_FACTORY = "ssl_client_socket_factory_no_default_factory";

  /**
   * Gets the default server socket factory
   *
   * @return the default server socket factory
   */
  public static synchronized javax.net.SocketFactory getDefault() {
    if (defaultFactory == null) {
      try {
        SSLServerContext serverContext = new SSLServerContext();
        serverContext.setDebugStream(new LoggerPrintStream());
        defaultFactory = new ClientSocketFactory(serverContext);
      } catch (Exception e) {
        SSLResourceAccessor.log(Severity.ERROR, e, GET_DEFAULT_FACTORY);
        SSLResourceAccessor.traceThrowable(Severity.WARNING, GET_DEFAULT_FACTORY, null, e);
      }
    }

    return defaultFactory;
  }

  /**
   * Gets a string representation of the enabled cipher suites from the context
   *
   * @return  a string array with the enabled cipher suites
   */
  public String[] getEnabledCipherSuites() {
    return CipherSuitesUtility.convertToStringArray(context.getEnabledCipherSuiteList());
  }

  public String[] getAvailableCertificates() {
    return KeyStoreConnector.getCertificates();
  }

  public boolean getNeedClientAuth() {
    return context.getRequestClientCertificate();
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
    defaultContext.setDebugStream(new LoggerPrintStream());
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
   * Removes a single certificate with the specified alias
   *
   * @param alias  the name of the certificate to be removed
   */
  void removeTrustedCertificates(String alias, boolean newCnainVerifierNeeded) {
    removeTrustedCertificates(new String[] {alias}, newCnainVerifierNeeded);
  }

  /**
   * Removes a couple of certificates with the specified keystore aliases
   *
   * @param aliases  the names of the certificates to be removed
   */
  void removeTrustedCertificates(String[] aliases, boolean newCnainVerifierNeeded) {
    Certificate[] certificates = KeyStoreConnector.getCertificates(aliases);
    synchronized (context) {
      for (int i = 0; i < aliases.length; i++) {
        if ((aliases[i] == null) || (aliases[i].trim().length() == 0)) {
          context.getChainVerifier().removeTrustedCertificate(null);
        }
      }

      if (certificates != null) {
        for (int i = 0; i < certificates.length; i++) {
          if (certificates[i] instanceof X509Certificate) {
            context.getChainVerifier().removeTrustedCertificate((X509Certificate) certificates[i]);
          }
        }
      }

      if (newCnainVerifierNeeded) {
        context.setChainVerifier(null);
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
      addCredentials(keystoreAliases);
    }
  }

  /**
   * Sets the default cipher suites for the context
   *
   * @param suites  the names of the cipher suites to be set as default for the context
   */
  public void setDefaultCipherSuites(String[] suites) {
    setEnabledCipherSuites(suites);
  }

  /**
   * Sets the enabled cipher suites for the context
   *
   * @param suites  the names of the cipher suites to be set as enabled for the context
   */
  public void setEnabledCipherSuites(String[] suites) {
    context.setEnabledCipherSuiteList(CipherSuitesUtility.convertToList(suites));
    context.updateCipherSuites();
  }

  public void setNeedClientAuth(boolean flag) {
    context.setRequestClientCertificate(flag);
  }

}

