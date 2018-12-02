package com.sap.engine.services.ssl.factory;

import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.security.Principal;
import java.security.cert.Certificate;
import javax.security.cert.X509Certificate;

import iaik.security.ssl.Session;
import iaik.security.ssl.SessionID;
import iaik.security.jsse.utils.Util;

public class SSLSession implements javax.net.ssl.SSLSession {

  /**
   * a reference to the iaik class
   */
  private Session iaikSession_;
  /**
   * the responsible session manager
   */
  private SSLSessionContext sessionContext_;
  private Hashtable bindings_;
  private long lastAccessedTime_;

  public SSLSession(Session iaikSession, SSLSessionContext manager) {
    iaikSession_ = iaikSession;
    sessionContext_ = manager;
    bindings_ = new Hashtable();
    lastAccessedTime_ = System.currentTimeMillis();
  }

  public String getCipherSuite() {
    return iaikSession_.getCipherSuite().getName();
  }

  public long getCreationTime() {
    return iaikSession_.getCreationTime();
  }

  public byte[] getId() {
    SessionID id = (SessionID) iaikSession_.getID();
    return id.getID();
  }

  public long getLastAccessedTime() {
    return lastAccessedTime_;
  }

  public X509Certificate[] getPeerCertificateChain() {
    java.security.cert.X509Certificate[] certificates = iaikSession_.getPeerCertificateChain();

    return (certificates != null) ? Util.convert(certificates) : new X509Certificate[0];
  }

  public String getPeerHost() {
    return null;
  }

  public SSLSessionContext getSessionContext() {
    return sessionContext_;
  }

  public Object getValue(String name) {
    //should be used the bindings from the iaikSession not like implemented????
    return bindings_.get(name);
  }

  public String[] getValueNames() {
    String[] names = new String[bindings_.size()];
    Enumeration e = bindings_.keys();

    if (e != null) {
      int i = 0;

      while (e.hasMoreElements()) {
        names[i] = (String) e.nextElement();
        i++;
      }
    }

    return names;
  }

  public void invalidate() {
    iaikSession_.invalidate();
  }

  public void putValue(String name, Object value) {
    //should be used the bindings from the iaikSession not like implemented????
    bindings_.put(name, value);

    if (value instanceof javax.net.ssl.SSLSessionBindingListener) {
      // notify
      SSLSessionBindingListener ear = (SSLSessionBindingListener) value;
      ear.valueBound(new SSLSessionBindingEvent(this, name));
    }
  }

  public void removeValue(String name) {
    Object value = bindings_.remove(name);

    if (value != null) {
      if (value instanceof javax.net.ssl.SSLSessionBindingListener) {
        SSLSessionBindingListener ear = (SSLSessionBindingListener) value;
        ear.valueUnbound(new SSLSessionBindingEvent(this, name));
      }
    }
  }

  public int hashCode() {
    //The method added for JLin Prio1 compatibility. It used to work with 
    // return super.hashCode()
    // so if you need performance - uncomment previous line (and comment the following one) 
    return getClass().getName().hashCode();
  }
  
  public boolean equals(Object alien) {
    if (alien instanceof SSLSession) {
      return (iaikSession_.equals(((SSLSession) alien).getSession()));
    }

    return false;
  }

  Session getSession() {
    return iaikSession_;
  }

  void setSession(Session session) {
    iaikSession_ = session;
  }

  void setLastAccessTime(long time) {
    lastAccessedTime_ = time;
  }

  public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
    return iaikSession_.getPeerCertificateChain();
  }

  public Certificate[] getLocalCertificates() {
    // todo: jdk 1.4
    return null;
  }

  public String getProtocol() {
    // todo: jdk 1.4
    return null;
  }

  /* (non-Javadoc)
   * @see javax.net.ssl.SSLSession#isValid()
   */
  public boolean isValid() {
    // TODO: verify with Java 5.0
    return iaikSession_.isValid();
  }

  /* (non-Javadoc)
   * @see javax.net.ssl.SSLSession#getPeerPrincipal()
   */
  public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
    // TODO: verify with Java 5.0
    if (getPeerCertificateChain().length == 0) {
      throw new SSLPeerUnverifiedException("No certificates provided by peer.");
    } else {
      return iaikSession_.getPeerCertificateChain()[0].getSubjectDN();
    }
  }

  /* (non-Javadoc)
   * @see javax.net.ssl.SSLSession#getLocalPrincipal()
   */
  public Principal getLocalPrincipal() {
    // TODO: implement for Java 5.0
    return null;
  }

  /* (non-Javadoc)
   * @see javax.net.ssl.SSLSession#getPeerPort()
   */
  public int getPeerPort() {
    // TODO: implement for Java 5.0
    return 0;
  }

  /* (non-Javadoc)
   * @see javax.net.ssl.SSLSession#getPacketBufferSize()
   */
  public int getPacketBufferSize() {
    // TODO: implement for Java 5.0
    return 0;
  }

  /* (non-Javadoc)
   * @see javax.net.ssl.SSLSession#getApplicationBufferSize()
   */
  public int getApplicationBufferSize() {
    // TODO: implement for Java 5.0
    return 0;
  }

}

