package com.sap.engine.services.ssl.factory;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import iaik.security.ssl.KeyAndCert;

/**
 *  The class provides SSL connections with the credentials available to
 * the user that creates such a connection. The class uses an "EBSDKS" KeyStore.
 *
 * @author  Stephan Zlatarev
 * @version 4.0.2
 */
public class Credentials extends KeyAndCert {

  private String keystoreAlias = null;

  public Credentials(String alias, X509Certificate[] chain, PrivateKey key) {
    super(chain, key);
    keystoreAlias = alias;
  }

  public String getAlias() {
    return keystoreAlias;
  }

}

