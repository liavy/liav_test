package com.sap.engine.services.ssl.keystore;


import com.sap.engine.services.keystore.impl.pse.PSEViewManager;
import com.sap.engine.services.keystore.impl.pse.SSLNotificationCallback;
import com.sap.engine.services.ssl.exception.KeyStoreConnectorException;
import com.sap.engine.services.ssl.exception.SSLConfigurationException;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.engine.services.ssl.factory.ClientSocketFactory;
import com.sap.engine.services.ssl.factory.Credentials;
import com.sap.engine.services.ssl.factory.ServerSocket;
import com.sap.engine.services.ssl.factory.ServerSocketFactory;
import com.sap.engine.services.ssl.server.ServerService;
import com.sap.engine.services.ssl.server.LockManager;
import com.sap.engine.services.ssl.server.ClusterEventListenerImpl;
import com.sap.engine.services.ssl.server.MessageListenerImpl;
import com.sap.engine.services.ssl.server.SAPStartupAccessor;
import com.sap.tc.logging.Severity;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.File;

/**
 *  The class provides SSL connections with the credentials available to
 * the user that creates such a connection. The class uses an "EBSDKS" KeyStore.
 *
 * @author  Stephan Zlatarev, Svetlana Stancheva
 * @version 4.0.2
 */
public class KeyStoreConnector implements SSLNotificationCallback {

  public static final String KV_ALIAS_SSL = "service_ssl";
  public static final String KV_ALIAS_TRUSTED_CA = "TrustedCAs";
  private static final String[] NO_ALIASES = new String[0];


  /**
   *  This is the minimal size of the ssl's keystore view - if there is less than MINIMAL_CREDENTIALS_COUNT, the ssl service will not starts!!!
   *
   *
   */
  public static final int MINIMAL_CREDENTIALS_COUNT = 1;

  /**
   *  The field contains a reference to the KeyStore used by the SSL package.
   * It is of "EBSDKS" type.
   */
  private static KeyStore credentialsView;
  private static KeyStore thrustedCAsView;
  private static boolean isKeystoreReady = false;

  public static void initConnector(KeyStore credentialsKeystoreView, KeyStore thrustedCAsKeystoreView) throws Exception {
    credentialsView = credentialsKeystoreView;
    thrustedCAsView = thrustedCAsKeystoreView;

    initializeActiveSockets();
    ServerSocketFactory.setKeyStoreIsReady(true);
  }

  public static KeyStore getCredentialsView() throws KeyStoreConnectorException {
    checkKeystoreStatus();
    return credentialsView;
  }

  private static void checkKeystoreStatus() throws KeyStoreConnectorException {
    int size = 0;

    if (!isKeystoreReady) {
      if (credentialsView == null) {
        throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_IS_NOT_INITIALIZED);
      }

      try {
        size = credentialsView.size();
        isKeystoreReady = true;
      } catch (KeyStoreException ke) {
        throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_CANNOT_BE_BROWSED, ke);
      }

      if (size < MINIMAL_CREDENTIALS_COUNT) {
        throw new KeyStoreConnectorException(KeyStoreConnectorException.SERVER_IDENTITY_NOT_FOUND);
      }
    }
  }

  /**
   *  Returns all the credentials accessible from this thread.
   * See documentation of EBSDKS KeyStore for information on security restrictions.
   *
   * @return  all credentials accessible from current thread;
   *          if there are no available credentials the method returns an emtpy array.
   */
  public synchronized static Credentials[] getAvailableCredentials() throws KeyStoreConnectorException {
    Enumeration enumeration = null;
    Credentials[] credentials = null;
    Vector entries = new Vector(10, 10);
    String alias = null;

    checkKeystoreStatus();

    try {
      enumeration = credentialsView.aliases();
    } catch (KeyStoreException ke) {
      throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_CANNOT_BE_BROWSED, ke);
    }

    if (enumeration != null) {
      while (enumeration.hasMoreElements()) {
        alias = (String) enumeration.nextElement();
        try {
          if (credentialsView.isKeyEntry(alias)) {
            entries.add(getCredentials(alias));
          }
        } catch (KeyStoreException kse) {
          throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_CANNOT_BE_BROWSED, kse);
        }
      }
    }
    credentials = new Credentials[entries.size()];

    for (int i = 0; i < credentials.length; i++) {
      credentials[i] = (Credentials) entries.elementAt(i);
    }

    return credentials;
  }


  /**
   *  Returns the credentials with the given alias if accessible from this thread.
   * See documentation of EBSDKS KeyStore for information on security restrictions.
   *
   * @return  credentials with the given alias.
   */
  public synchronized static Credentials getCredentials(String alias) throws KeyStoreConnectorException {
    checkKeystoreStatus();

    X509Certificate[] x509chain = null;
    Certificate[] chain = null;

    try {
      chain = credentialsView.getCertificateChain(alias);
    } catch (KeyStoreException e) {
      throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_CANNOT_BE_BROWSED, e);
    }

    if (chain != null) {
      if (chain instanceof X509Certificate[]) {
        x509chain = (X509Certificate[]) chain;
      } else {
        x509chain = new X509Certificate[chain.length];
        System.arraycopy(chain, 0, x509chain, 0, chain.length);
      }
      PrivateKey privateKey = null;
      try {
        privateKey = (PrivateKey) credentialsView.getKey(alias, null);
      } catch (KeyStoreException kse) {
        throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_CANNOT_BE_BROWSED, kse);
      } catch (NoSuchAlgorithmException nsae) {
        throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_ENTRY_CANNOT_BE_CONSTRUCTED, nsae);
      } catch (UnrecoverableKeyException uke) {
        throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_ENTRY_CANNOT_BE_CONSTRUCTED, uke);
      }
      return new Credentials(alias, x509chain, privateKey);
    } else {
      throw new KeyStoreConnectorException(KeyStoreConnectorException.MISSING_CERTIFICATE_CHAIN);
    }
  }

  /**
   *  Returns the credentials with the given aliases if accessible from this thread.
   * See documentation of EBSDKS KeyStore for information on security restrictions.
   *
   * @return  all credentials accessible from current thread;
   *          if there are no available credentials the method returns an emtpy array.
   */
  public synchronized static Credentials[] getCredentials(String[] aliases) throws KeyStoreConnectorException {
    Vector entries = new Vector(10, 10);
    Credentials[] credentials = null;

    checkKeystoreStatus();


    for (int i = 0; i < aliases.length; i++) {
      entries.add(getCredentials(aliases[i]));
    }
    credentials = new Credentials[entries.size()];
    for (int i = 0; i < credentials.length; i++) {
      credentials[i] = (Credentials) entries.elementAt(i);
    }

    return credentials;
  }

  /**
   *  Returns the certificates with the given aliases if accessible from this thread.
   * See documentation of EBSDKS KeyStore for information on security restrictions.
   *
   * @return  all certificates accessible from current thread;
   *          if there are no available certificates the method returns an emtpy array.
   */
  public synchronized static Certificate[] getCertificates(String[] aliases) throws KeyStoreConnectorException {
    Vector entries = new Vector(10, 10);
    Certificate certificate = null;
    Certificate[] certificates = null;

    checkKeystoreStatus();

    for (int i = 0; i < aliases.length; i++) {
      if ((aliases[i] == null) || (aliases[i].trim().length() == 0)) {
        continue;
      }

      try {
        certificate = thrustedCAsView.getCertificate(aliases[i]);
      } catch (KeyStoreException e) {
        throw new KeyStoreConnectorException(KeyStoreConnectorException.MISSING_CERTIFICATE, e);
      }

      if (certificate != null) {
        entries.add(certificate);
      } else {
        throw new KeyStoreConnectorException(KeyStoreConnectorException.MISSING_CERTIFICATE);
      }
    }
    certificates = new Certificate[entries.size()];
    for (int i = 0; i < certificates.length; i++) {
      certificates[i] = (Certificate) entries.elementAt(i);
    }

    return certificates;
  }

  public synchronized static Certificate getCertificate(String alias) throws KeyStoreConnectorException {
    Certificate certificate = null;

    checkKeystoreStatus();
    try {
      certificate = thrustedCAsView.getCertificate(alias);
    } catch (KeyStoreException e) {
      throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_CANNOT_BE_BROWSED, e);
    }

    return certificate;
  }

  public static String getInformationForActiveSockets() {
    String info = "\n--------------------------------------  active SSL sockets  --------------------------------------\n";
    try {
      ServerSocket socket = null;
      ServerSocketFactory factory = (ServerSocketFactory) ServerSocketFactory.getDefault();
      String[][] sockets = factory.getActiveServerSockets();
      String[] credentials;
      String[] suites;
      info += "active sockets: " + sockets.length + "\n";
      for (int i = 0; i < sockets.length; i++) {
        socket = ServerSocketFactory.getServerSocket(sockets[i][0], Integer.valueOf(sockets[i][1]).intValue());
        info += socket.toString() + "\n";
        credentials = socket.getCredentials();

        info += "credentials {\n";
        for (int j = 0; j < credentials.length; j++) {
          info += "  cred: " + credentials[j] + "\n";
        }
        info += "}\n";
        suites = socket.getEnabledCipherSuites();
        info += "enabled suites {\n";
        for (int j = 0; j < suites.length; j++) {
          info += "  suit: " + suites[j] + "\n";
        }
        info += "}\n";
      }
      info += "-----------------------------------------------------------------------------------------------------------------";
      return info;
    } catch (Exception e) {
      SSLResourceAccessor.traceThrowable(Severity.DEBUG, "getInformationForActiveSockets()", e);
      return e.toString();
    } catch (NoClassDefFoundError err) {
      SSLResourceAccessor.traceThrowable(Severity.DEBUG, "getInformationForActiveSockets()", err);
      return err.toString();
    }
  }

  private static final String ACTIVE_SOCKETS_NOT_CHECKED="ssl_keystore_connector_check_active_sockets";

  public static void checkActiveSockets() throws SSLConfigurationException {
    ServerSocket socket = null;
    ServerSocketFactory factory = (ServerSocketFactory) ServerSocketFactory.getDefault();
    String[][] sockets = factory.getActiveServerSockets();
    String[] credentials = new String[]{};
    String[] suites = new String[]{};;

    for (int i = 0; i < sockets.length; i++) {
      Throwable throwable = null;

      try {
        socket = ServerSocketFactory.getServerSocket(sockets[i][0], Integer.valueOf(sockets[i][1]).intValue());
        credentials = socket.getCredentials();
        suites = socket.getEnabledCipherSuites();
      } catch (Exception e) {
        SSLResourceAccessor.log(Severity.WARNING, e, ACTIVE_SOCKETS_NOT_CHECKED);
      } catch (NoClassDefFoundError err) {
        SSLResourceAccessor.traceThrowable(Severity.WARNING, ACTIVE_SOCKETS_NOT_CHECKED, null, err);
      }


      if (credentials.length == 0) {
        if (throwable != null) {
          throw new SSLConfigurationException(SSLConfigurationException.NO_ENABLED_CERTIFICATES, sockets[i], throwable);
        } else {
          throw new SSLConfigurationException(SSLConfigurationException.NO_ENABLED_CERTIFICATES, sockets[i]);
        }
      }
      if (suites.length == 0) {
        if (throwable != null) {
          throw new SSLConfigurationException(SSLConfigurationException.NO_ENABLED_CIPHER_SUITES, sockets[i], throwable);
        } else {
          throw new SSLConfigurationException(SSLConfigurationException.NO_ENABLED_CIPHER_SUITES, sockets[i]);
        }
      }
    }
  }
  private static final String SSL_FACTORY_CONFIGURATION_FOUND="ssl_keystore_connector_factory_configuration_start";
  private static final String ENABLED_SERVER_CREDENTIAL="ssl_keystore_connector_enabled_credential";
  private static final String INVALID_CONFIGURATION_FILE="ssl_keystore_connector_invalid_configuration_file";
  private static final String ENABLED_CIPHER_SUITE="ssl_keystore_connector_enabled_cipher_suite";
  private static final String USED_CIPHER_SUITEs="ssl_keystore_connector_used_cipher_suites";
  private static final String USED_TRUSTED_CA="ssl_keystore_connector_used_trusted_CA";
  private static final String SSL_FACTORY_CONFIGURATION_NOT_FOUND="ssl_keystore_connector_configuration_file_not_found";

  private static void initializeActiveSockets() throws SSLConfigurationException, KeyStoreConnectorException {
    try {
      int trustedCertificates = 0;
      ServerSocketFactory factory = (ServerSocketFactory) ServerSocketFactory.getDefault();
      String[][] sockets = factory.getActiveServerSockets();
      Properties properties = factory.getSocketsProperties();
      Credentials[] available = getAvailableCredentials();
      String[] credentials = new String[available.length];

      for (int i = 0; i < credentials.length; i++) {
        credentials[i] = available[i].getAlias();
      }

      if (ServerSocketFactory.getPropertiesLoaded()) {
        SSLResourceAccessor.log(Severity.INFO, "", SSL_FACTORY_CONFIGURATION_FOUND);
        boolean propertiesFound = false;
        int count = 0;
        String value = "";
        String[] ciphers = new String[0];
        int[] types = factory.getAllowedCertificateTypes();

        for (int i = 0; i < types.length; i++) {
          String key = ServerSocketFactory.CERT_KEY + types[i];
          value = properties.getProperty(key, "");

          if (!"".equals(value.trim())) {
            factory.initCredentials(value);
            ClientSocketFactory defaultClientSocketFactory = (ClientSocketFactory) ClientSocketFactory.getDefault();
            defaultClientSocketFactory.addCredentials(value);
            SSLResourceAccessor.log(Severity.INFO, "", ENABLED_SERVER_CREDENTIAL, new String[] {value});
            propertiesFound = true;
          }
        }

        if (!propertiesFound) { // the ssl service is started without fill IAIK package!
          SSLResourceAccessor.log(Severity.INFO, "", INVALID_CONFIGURATION_FILE);
          factory.addCredentials(credentials);

          factory.setNeedClientAuth(false);
          factory.addTrustedCertificates("");

          for (int i = 0; i < sockets.length; i++) {
            ServerSocket socket = ServerSocketFactory.getServerSocket(sockets[i][0], Integer.valueOf(sockets[i][1]).intValue());
            socket.addCredentials(credentials);
            socket.setNeedClientAuth(false);
            socket.addTrustedCertificates("");
          }
          return;
        }

        count = 0;
        do {
          String key = ServerSocketFactory.CIPHER_KEY + count;
          value = properties.getProperty(key, "");

          if (!"".equals(value.trim())) {
            String[] temp = ciphers;
            ciphers = new String[ciphers.length + 1];
            System.arraycopy(temp, 0, ciphers, 0, temp.length);
            ciphers[temp.length] = value;
            propertiesFound = true;
            SSLResourceAccessor.log(Severity.INFO, "", ENABLED_CIPHER_SUITE, new String[] {value});
          } else {
            SSLResourceAccessor.log(Severity.INFO, "", USED_CIPHER_SUITEs, new String[] {Integer.toString(count)});
            if (count == 0) { // no active cipher suites!!!!
              throw new SSLConfigurationException(SSLConfigurationException.NO_ENABLED_CIPHER_SUITES);
            }
          }

          count++;
        } while (!"".equals(value));
        factory.setDefaultCipherSuites(ciphers);

        count = 0;
        do {
          String key =  ServerSocketFactory.TRUST_KEY + count;
          value = properties.getProperty(key, "").trim();
          if (!"".equals(value)) {
            factory.addTrustedCertificates(new String[] {value}, false);
            propertiesFound = true;

            if (value.length() > 0) {
              trustedCertificates++;
            }

            SSLResourceAccessor.log(Severity.INFO, "", USED_TRUSTED_CA, new String[] {value});
          }

          count++;
        } while (!"".equals(value));

        if (trustedCertificates == 0) {
          factory.setNeedClientAuth(false);
        } else {
          value = properties.getProperty(ServerSocketFactory.AUTH_KEY, "");

          if ("false".equals(value)) {
            factory.setNeedClientAuth(false);
            propertiesFound = true;
          } else {
            factory.setNeedClientAuth(true);
          }
        }

        if (!propertiesFound) {
          factory.addCredentials(credentials);
          factory.setNeedClientAuth(false);
        }

        for (int i = 0; i < sockets.length; i++) {
          int port = Integer.valueOf(sockets[i][1]).intValue();
          ServerSocket socket = ServerSocketFactory.getServerSocket(sockets[i][0], port);
          socket.initialize(false);
        }

      } else {
        SSLResourceAccessor.log(Severity.INFO, "", SSL_FACTORY_CONFIGURATION_NOT_FOUND);
        factory.addCredentials(credentials);

        factory.setNeedClientAuth(false);
        factory.addTrustedCertificates("");

        for (int i = 0; i < sockets.length; i++) {
          ServerSocket socket = ServerSocketFactory.getServerSocket(sockets[i][0], Integer.valueOf(sockets[i][1]).intValue());
          socket.addCredentials(credentials);
          socket.setNeedClientAuth(false);
          socket.addTrustedCertificates("");
        }
      }
    } catch (Exception e) {
      throw new SSLConfigurationException(SSLConfigurationException.UNABLE_TO_CONFIGURE_SOCKETS, e);
    } catch (NoClassDefFoundError err) {
      throw new SSLConfigurationException(SSLConfigurationException.UNABLE_TO_CONFIGURE_SOCKETS, err);
    }
  }


  public static String[] getCertificates() throws KeyStoreConnectorException {
    Enumeration aliases = null;
    ArrayList temp = new ArrayList();
    String[] certificateAliases = NO_ALIASES;

    checkKeystoreStatus();

    try {
      aliases = thrustedCAsView.aliases();
    } catch (KeyStoreException ke) {
      throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_CANNOT_BE_BROWSED, ke);
    }

    while (aliases.hasMoreElements()) {
      String alias = (String) aliases.nextElement();
      boolean isCertificate = false;
      try {
        isCertificate = thrustedCAsView.isCertificateEntry(alias);
      } catch (KeyStoreException e) {
        throw new KeyStoreConnectorException(KeyStoreConnectorException.KEYSTORE_CANNOT_BE_BROWSED, e);
      }
      if (isCertificate) {
        temp.add(alias);
      }
    }
    certificateAliases = new String[temp.size()];
    for (int i = 0; i < certificateAliases.length; i++) {
      certificateAliases[i] = (String) temp.get(i);
    }

    return certificateAliases;
  }

//******************************************************************************************
  private static final String defaultServerPSEFileNamePrefix = "./../../../sec/SAPSSLS";

  private static final String currentServerPSEFileName = defaultServerPSEFileNamePrefix + ".pse";
  private static final String currentClientPSEFileName = "./../../../sec/SAPSSLC.pse";
  private static final String SAPSSLS_VIEW_PREFIX = "ICM_SSL_";
  private static final String SAPSSLC_VIEW_PREFIX = "CLIENT_ICM_SSL_";
  private static final String SAPSSLA_VIEW_PREFIX = "ANON-CLIENT_ICM_SSL_";

  private static int currentGroupID = -1;
  static PSEDescriptor[] clientSidePSEs = null;

  static PSEDescriptor[] getServerSidePSEs() {
    Vector<String> httpsPorts = SAPStartupAccessor.getHTTPSPorts();
    PSEDescriptor[] result = new PSEDescriptor[httpsPorts.size() + 1];
    result[0] = getDefaultSAPSSL_S_Descriptor();
    String viewName = null;
    String pseFileName = null;

    for (int i = 0; i < httpsPorts.size(); i++) {

      viewName = SAPSSLS_VIEW_PREFIX + Integer.toString(currentGroupID) + "_" + httpsPorts.get(i);
      pseFileName = defaultServerPSEFileNamePrefix + "_" + httpsPorts.get(i) + ".pse";
      result[i + 1] = new PSEDescriptor(PSEDescriptor.TYPE_SERVER, viewName, pseFileName);
      ServerService.dump("[" + i + "]: " + result[i + 1]);
    }

    return result;
  }

  private static boolean initialCheckHasDone = false;
  private static boolean autoExportToPSE = false;


  public static final void initialPseCheckDone() {
    initialCheckHasDone = true;
  }

  private static PSEDescriptor getDefaultSAPSSL_S_Descriptor() {
    return new PSEDescriptor(PSEDescriptor.TYPE_SERVER,
                             generateViewName(PSEDescriptor.TYPE_SERVER, Integer.toString(currentGroupID)),
                             currentServerPSEFileName);
  }

  private static PSEDescriptor getDefaultSAPSSL_C_Descriptor() {
    return new PSEDescriptor(PSEDescriptor.TYPE_AUTH_CLIENT,
                            generateViewName(PSEDescriptor.TYPE_AUTH_CLIENT, Integer.toString(currentGroupID)),
                            currentClientPSEFileName);
  }

  public static void setCurrentGroupID(int group_id) {
    currentGroupID = group_id;
    clientSidePSEs = new PSEDescriptor[] {getDefaultSAPSSL_C_Descriptor()};
  }

  public static final synchronized void start(boolean autoSynchPSE) throws Exception {
    autoExportToPSE = autoSynchPSE;
    ServerService.dump(" keystore connector start() {");
    LockManager.start();

    PSEViewManager.registerSSLNotificationCallback(new KeyStoreConnector());
    ServerService.dump(" register SSLNotificationCallback OK");


    MessageListenerImpl.start();
    ServerService.dump(" register MessageListener OK");

    ClusterEventListenerImpl.start();
    ServerService.dump(" register ClusterEventListener OK");


    if (autoSynchPSE) {
      initialPseCheck();
    } else {
      ServerService.dump(" the automatic PSE file synchronization has been suppresed");
    }

    ServerService.dump(" keystore connector start() } ok");
  }

  static final void initialPseCheck() throws KeyStoreException {
    ServerService.dump(" initialPseCheck { " );

    try {
      LockManager.getClusterLock();
    } catch (Exception e) {
      ServerService.dump("initialPseCheck() } get lock", e);
      return;
    }

    try {
      if (initialCheckHasDone) {
        ServerService.dump(" initialPseCheck } ok: checked " );
        return;
      }
      checkAllPSEs();
      ServerService.dump(" initialPseCheck } ok " );

      MessageListenerImpl.notify_for_successfull_initial_check();
    } finally {
      try {
        LockManager.releaseClusterLock();
      } catch (Exception e) {
        ServerService.dump("initialPseCheck() } end - release lock", e);
      }
    }
  }

  static private void checkAllPSEs() throws KeyStoreException {
    PSEDescriptor[] serverSidePSEs = getServerSidePSEs();
    for (PSEDescriptor pse: serverSidePSEs) {
      PSEViewManager.verifyAndUpdateState(pse.getViewName(), pse.getFile());
    }
    for (PSEDescriptor pse: clientSidePSEs) {
      PSEViewManager.verifyAndUpdateState(pse.getViewName(), pse.getFile());
    }
  }


  public static synchronized void stop() {
    ClusterEventListenerImpl.stop();
    MessageListenerImpl.stop();
    LockManager.stop();
  }

  public static void preparePSE() {
    ServerService.dump(" preparePSE { " );

    if (!autoExportToPSE) {
      ServerService.dump("preparePSE() } autoExport is Off");
      return;
    }

    try {
      LockManager.getClusterLock();
    } catch (Exception e) {
      ServerService.dump("preparePSE() } get lock", e);
      return;
    }
    try {
      PSEDescriptor[] serverSidePSEs = getServerSidePSEs();
      for (PSEDescriptor pse: serverSidePSEs) {
        preparePSE(pse);
      }
      for (PSEDescriptor pse: clientSidePSEs) {
        preparePSE(pse);
      }
    } finally {
      try {
        LockManager.releaseClusterLock();
      } catch (Exception e) {
        ServerService.dump("preparePSE() } end - release lock", e);
      }
    }
  }

  public static void preparePSE(PSEDescriptor pse) {
    ServerService.dump(" preparePSE { " );
    ServerService.dump("   :: " + pse);

    String currentPSEViewName = pse.getViewName();
    File currentPSEFile = pse.getFile();

    try {
      int verificationState = PSEViewManager.verifyPSEFile(currentPSEViewName, currentPSEFile);
      String state = (verificationState == PSEViewManager.OK)? "OK"
                   : (verificationState == PSEViewManager.MISSING_FILE)? "MISSING_FILE"
                   : (verificationState == PSEViewManager.MISSING_VIEW)? "MISSING_VIEW"
                   : "FILE_NOT_UPDATED";

      ServerService.dump(" verification state: " + state);
      switch (verificationState) {
        case PSEViewManager.OK: {
          ServerService.dump(" preparePSE } ok - return " );
          break;
        }
        case PSEViewManager.MISSING_FILE: {
          // export view -> file
          try {
            PSEViewManager.exportPSEViewToFile(currentPSEViewName);
            ServerService.dump(" preparePSE } MISSING_FILE ok - exportToFile " );
            MessageListenerImpl.notify_ICM_for_PSE_change();
          } catch (KeyStoreException e) {
            if (pse.isServerPSE()) {
              ServerService.dump("preparePSE() } MISSING_FILE err", e);
              SSLResourceAccessor.traceThrowable(Severity.ERROR, "exportPSEViewToFile(" + currentPSEViewName + ") err", e);
              SSLResourceAccessor.log(Severity.ERROR, new Object[]{currentPSEViewName}, "PREPARE_PSE");
            } else {
              ServerService.dump("preparePSE() } MISSING_FILE warning", e);
              SSLResourceAccessor.traceThrowable(Severity.WARNING, "exportPSEViewToFile(" + pse + ") failed", e);
            }
          }
          break;
        }
        case PSEViewManager.MISSING_VIEW: {
          // create & init view
          if (pse.isServerPSE()) {
            PSEViewManager.createNewPSEView(currentPSEViewName
                    , KV_ALIAS_SSL
                    , null   /*KV_ALIAS_TRUSTED_CA*/
                    , currentPSEFile.getPath()
                    , " ICM Server SSL credentials store");
            ServerService.dump(" preparePSE } ok - create_new_server " );
            MessageListenerImpl.notify_ICM_for_PSE_change();
          } else {
            PSEViewManager.createNewEmptyPSEView(currentPSEViewName
                                                 , currentPSEFile.getPath()
                                                 , " ICM Client SSL credentials store");
            ServerService.dump(" preparePSE } ok - create_empty_client " );
            MessageListenerImpl.notify_ICM_for_PSE_change();
          }
          break;
        }
        case PSEViewManager.FILE_NOT_UPDATED: {
          // export view
          try {
            PSEViewManager.exportPSEViewToFile(currentPSEViewName);
            ServerService.dump(" preparePSE } FILE_NOT_UPDATED ok - exportToFile " );
            MessageListenerImpl.notify_ICM_for_PSE_change();
          } catch (KeyStoreException e) {
            if (pse.isServerPSE()) {
              ServerService.dump("preparePSE() } FILE_NOT_UPDATED err", e);
              SSLResourceAccessor.traceThrowable(Severity.ERROR, "exportPSEViewToFile(" + currentPSEViewName + ") err", e);
              SSLResourceAccessor.log(Severity.ERROR, new Object[]{currentPSEViewName}, "PREPARE_PSE");
            } else {
              ServerService.dump("preparePSE() } FILE_NOT_UPDATED warning", e);
              SSLResourceAccessor.traceThrowable(Severity.WARNING, "exportPSEViewToFile(" + pse + ") failed", e);
            }
          }
          break;
        }
      }

    } catch (KeyStoreException e) {
      ServerService.dump("preparePSE() } err", e);
      SSLResourceAccessor.traceThrowable(Severity.ERROR, "preparePSE(" + currentPSEViewName + ") err", e);
      SSLResourceAccessor.log(Severity.ERROR, new Object[]{currentPSEViewName}, "PREPARE_PSE");
    }
  }


  private static final String generateViewName(int pseType, String groupID) {
    switch (pseType) {
      case PSEDescriptor.TYPE_AUTH_CLIENT: return "CLIENT_ICM_SSL_" + groupID;
      case PSEDescriptor.TYPE_SERVER: return "ICM_SSL_" + groupID;
      case PSEDescriptor.TYPE_ANON_CLIENT: return "ANON-CLIENT_ICM_SSL_" + groupID;
    }
    return "???PSE_ICM_SSL_" + groupID;
  }

  private static String getGroupID(String pseViewName) {
    String result = null;

    if (!pseViewName.startsWith(SAPSSLS_VIEW_PREFIX)
         && !pseViewName.startsWith(SAPSSLC_VIEW_PREFIX)
         && !pseViewName.startsWith(SAPSSLA_VIEW_PREFIX)) {

      return null;
    }

    try {
      String[] nameParts = pseViewName.split("_");
      result = ("ICM".equals(nameParts[0]))? nameParts[2]: nameParts[3];
    } catch (Exception e) {
      result = null;
    }

    return result;
  }

  public static void main(String[] args) {
    String c =  "CLIENT_ICM_SSL_" + 10000 + "_port";
    String s =  "ICM_SSL_" + 10000 + "_port";
    System.out.println(" s: " + s + ", c: " + c);
    System.out.println(" 1: " + getGroupID(s));
    System.out.println(" 2: " + getGroupID(c));

  }



  public boolean isUsedByICM(String pseViewName) {
    boolean result = getGroupID(pseViewName) != null;
    ServerService.dump("isUsedByICM(" + pseViewName + ") = " + result);
    return result;
  }

  public void exportToFile(String pseViewName) throws KeyStoreException {
    ServerService.dump("exportToFile(" + pseViewName + ") {");

    String groupID = getGroupID(pseViewName);
    ServerService.dump("  target cluster group: " + groupID);

    if (groupID != null) {
      if (Integer.parseInt(groupID) == currentGroupID) {
        PSEViewManager.exportPSEViewToFile(pseViewName);
        ServerService.dump("  Current PSE updated");
        MessageListenerImpl.notify_ICM_for_PSE_change();
        ServerService.dump("exportToFile() } ok: notify_ICM_for_PSE_change");
      } else {
        ServerService.dump("  PSE for goup " + groupID + " updated");
        MessageListenerImpl.notify_other_group_for_PSE_change(Integer.parseInt(groupID), pseViewName);
        ServerService.dump("exportToFile() } ok: notify_other_group_for_PSE_change");
      }
    }
  }
}

