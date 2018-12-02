/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf.. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ssl.factory.session;

import iaik.security.ssl.Session;
import iaik.security.ssl.SessionID;
import iaik.security.ssl.SSLTransport;

import java.util.Date;

import com.sap.engine.lib.util.cache.StableCacheObject;

/**
 * This class represents a wrapper of iaik.security.ssl.Session.
 * It also keeps track of the resume time of the session and the
 * remotePeerId the created it.
 *
 * @author Jako Blagoev
 */
class SSLSessionWrapper implements StableCacheObject {
  private Session reference = null;
  private String  transport = null;

  private long lastAccess;
  private long resumePeriod;

  public SSLSessionWrapper(Session session, SSLTransport transport) {
    this.reference = session;
    this.transport = transport.getRemotePeerId().toString();
    resume();
  }

  public final void resume() {
    this.lastAccess = System.currentTimeMillis();
  }

  public void setResumePeriod(long resumePeriod) {
    this.resumePeriod = resumePeriod;
  }

  public Session getSession() {
    return reference;
  }

  public String getTransport() {
    return transport;
  }

  public byte[] getId() {
    return ((SessionID) reference.getID()).getID();
  }

  public boolean isActive() {
    return ((System.currentTimeMillis() < lastAccess + resumePeriod * 1000)  && (reference.isValid()));
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("SSL Session with ");

    sb.append(" remote peer ");
    sb.append(transport);
//    sb.append("\r\n");

    sb.append(", created on ");
    sb.append(new Date(reference.getCreationTime()).toString());
//    sb.append("\r\n");

//    sb.append("Resumable until: ");
//    sb.append(new Date(lastAccess + resumePeriod * 1000).toString());
//    sb.append("\r\n");

    sb.append(", cipher suite : ");
    sb.append(reference.getCipherSuite().getName());
 //   sb.append("\r\n");

    sb.append(", compression method : ");
    sb.append(reference.getCompressionMethod().getName());
    sb.append("\r\n");

    sb.append(reference.getID().toString());
    sb.append("\r\n");

    return sb.toString();
  }

  public void cacheFinalization() {
    reference = null;
    transport = null;
  }
}
