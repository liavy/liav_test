package com.sap.engine.services.ssl.dispatcher;

import com.sap.engine.interfaces.keystore.KeystoreManager;

import com.sap.engine.services.ssl.keystore.KeyStoreConnector;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.engine.frame.cluster.transport.TransportContext;
import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.tc.logging.Severity;

import java.security.KeyStore;
import java.util.Enumeration;

/**
 *
 *
 *
 */
public class KeystoreListenerImpl implements Runnable {

  private final static String INTERFACE_KEYSTORE = "keystore";

  private boolean isRegistred = false;
  private TransportContext transportContext = null;
  private ThreadSystem threadSystem = null;
  private static final int TIMEOUT = 100;

  public KeystoreListenerImpl(TransportContext transportContext, ThreadSystem threadSystem) {
    this.transportContext = transportContext;
    this.threadSystem = threadSystem;
  }

  public synchronized void keystoreIsAvailable() {
    if (!isRegistred) {
      threadSystem.startThread(this, true);
    }
  }

  private static final String TRANSPORT_SUPPLIER_REGISTER_BEGIN = "ssl_keystore_listener_register_supplier_begin";
  private static final String TRANSPORT_SUPPLIER_REGISTER_END_OK = "ssl_keystore_listener_register_supplier_end_ok";
  private static final String TRANSPORT_SUPPLIER_REGISTER_END_EXC = "ssl_keystore_listener_register_supplier_end_exc";
  private static final String KEYSTORE_NOT_AVAILABLE = "ssl_keystore_listener_keystore_lookup_exc";
  private static final String KEYSTORE_IS_NULL = "ssl_keystore_listener_received_null_keystore";
  private static final String SSL_VIEW_MISSING = "ssl_keystore_listener_server_view_missing";
  private static final String TRUSTED_CA_VIEW_MISSING = "ssl_keystore_listener_trustedca_view_missing";
  private static final String SSL_VIEW_EMPTY = "ssl_keystore_listener_server_view_empty";
  private static final String UNEXPECTED_PROBLEM = "ssl_keystore_listener_unexpected_error";
  private static final String TRANSPORT_SUPPLIER_UNREGISTER_BEGIN = "ssl_keystore_listener_transport_supplier_unregister_begin";
  private static final String TRANSPORT_SUPPLIER_UNREGISTER_END_OK = "ssl_keystore_listener_transport_supplier_unregistered_ok";
  private static final String TRANSPORT_SUPPLIER_UNREGISTER_END_EXC = "ssl_keystore_listener_transport_supplier_unregistered_exc";

  public void run() {
    SSLResourceAccessor.log(Severity.INFO, "", TRANSPORT_SUPPLIER_REGISTER_BEGIN);
    KeystoreManager keystoreManager = null;
    KeyStore sslKeystoreView = null;
    KeyStore caKeystoreView = null;

    try {
      keystoreManager = (KeystoreManager) DispatcherService.getServiceContext().getContainerContext().getObjectRegistry().getServiceInterface(INTERFACE_KEYSTORE);
    } catch (Exception e) {
      SSLResourceAccessor.log(Severity.FATAL, e, KEYSTORE_NOT_AVAILABLE);
      SSLResourceAccessor.traceThrowable(Severity.FATAL, KEYSTORE_NOT_AVAILABLE, null, e);
      return;
    }

    if (keystoreManager == null) {
      SSLResourceAccessor.log(Severity.FATAL, "", KEYSTORE_IS_NULL);
      return;
    }

    try {
      sslKeystoreView = keystoreManager.getKeystore(KeyStoreConnector.KV_ALIAS_SSL);
    } catch (Exception e) {
      SSLResourceAccessor.log(Severity.FATAL, e , SSL_VIEW_MISSING, new Object[]{KeyStoreConnector.KV_ALIAS_SSL});
      SSLResourceAccessor.traceThrowable(Severity.FATAL, SSL_VIEW_MISSING, new Object[]{KeyStoreConnector.KV_ALIAS_SSL}, e);
      return;
    }
    try {
      caKeystoreView = keystoreManager.getKeystore(KeyStoreConnector.KV_ALIAS_TRUSTED_CA);
    } catch (Exception e) {
      SSLResourceAccessor.log(Severity.FATAL, e, TRUSTED_CA_VIEW_MISSING, new Object[]{KeyStoreConnector.KV_ALIAS_TRUSTED_CA});
      SSLResourceAccessor.traceThrowable(Severity.FATAL, TRUSTED_CA_VIEW_MISSING, new Object[]{KeyStoreConnector.KV_ALIAS_TRUSTED_CA}, e);
      return;
    }

    synchronized (this) {
      while (true) {
        try {
          wait(TIMEOUT);
        } catch (Exception e) {
          /* skipped  */
          continue;
        }
        try {
          SSLResourceAccessor.trace(Severity.INFO, "SSL server identity keystore view [{0}] has {1} entries.", new String[] {KeyStoreConnector.KV_ALIAS_SSL, String.valueOf(sslKeystoreView.size())});

          if (sslKeystoreView.size() >= KeyStoreConnector.MINIMAL_CREDENTIALS_COUNT) {
            boolean isKeyFound = false;
            Enumeration aliases = sslKeystoreView.aliases();

            while (aliases.hasMoreElements()) {
              String entryAlias = (String) aliases.nextElement();
              if (sslKeystoreView.isKeyEntry(entryAlias)) {
                isKeyFound = true;
                break;
              }
            }

            if (!isKeyFound) {
              SSLResourceAccessor.log(Severity.FATAL, "", SSL_VIEW_EMPTY);
              return;
            }
            break;
          }
        } catch (Exception ke) {
          SSLResourceAccessor.log(Severity.FATAL, ke, UNEXPECTED_PROBLEM);
          SSLResourceAccessor.traceThrowable(Severity.FATAL, UNEXPECTED_PROBLEM, null, ke);
          return;
        }
      }
    }

    try {
      KeyStoreConnector.initConnector(sslKeystoreView, caKeystoreView);
      SSLTransportSupplier transportSupplier =  new SSLTransportSupplier();
      transportContext.registerTransportSupplier("ssl", transportSupplier);
      isRegistred = true;
      SSLResourceAccessor.log(Severity.INFO, "", TRANSPORT_SUPPLIER_REGISTER_END_OK);
    } catch (Exception e) {
      SSLResourceAccessor.log(Severity.FATAL, e, TRANSPORT_SUPPLIER_REGISTER_END_EXC);
      SSLResourceAccessor.traceThrowable(Severity.FATAL, TRANSPORT_SUPPLIER_REGISTER_END_EXC, null,  e);
      stop();
    }
  }


  public synchronized void stop() {
    SSLResourceAccessor.log(Severity.INFO, "", TRANSPORT_SUPPLIER_UNREGISTER_BEGIN);
    try {
      transportContext.unregisterTransportSupplier("ssl");
      isRegistred = false;
    } catch (Exception e) {
      SSLResourceAccessor.log(Severity.WARNING, e, TRANSPORT_SUPPLIER_UNREGISTER_END_EXC);
      SSLResourceAccessor.traceThrowable(Severity.WARNING, TRANSPORT_SUPPLIER_UNREGISTER_END_EXC, null,  e);
    }
    SSLResourceAccessor.log(Severity.INFO, "", TRANSPORT_SUPPLIER_UNREGISTER_END_OK);
  }
}

