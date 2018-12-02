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

import iaik.security.ssl.SessionManager;
import iaik.security.ssl.Session;
import iaik.security.ssl.SSLTransport;
import iaik.security.ssl.SessionID;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import com.sap.engine.lib.lang.SWMRG;
import com.sap.engine.lib.util.cache.Cache;

/**
 * Implementation of iaik.security.ssl.SessionManager and javax.net.ssl.SSLSessionContext.
 * It uses a limited LRU Cache to hold the ssl sessions. The cache size and the resume period are
 * configured as service properties of the ssl service. The cache is registered as a timeout listener.
 * On a 5 minutes timeout the invalidated sessions and the sessions that are not resumable are deleted
 * from the cache.
 * Note: Only one instance of the JSSELimitedCache is used on the dispatcher. The session can be resumed
 * only by the same remotePeerId that has activated it.
 * RemotePeerId = dispatcher_host:dispatcher_port:caller_host.
 *
 * @author Jako Blagoev
 */
public class JSSELimitedCache extends SessionManager implements com.sap.engine.services.timeout.TimeoutListener, SSLSessionContext {
  private int cache_size    = 1000;
  private int resume_period = 2 * 60 * 60;

  private Cache cache = null;
  private SWMRG guard = null;

  private static JSSELimitedCache reference = null;

  private JSSELimitedCache(int cache_size) {
    if (cache_size > 10) {
      this.cache_size = cache_size;
    }
    cache = new Cache(10, this.cache_size);
    guard = new SWMRG();
  }

  public static JSSELimitedCache initialize(int size, int resume_period) {
    if (reference != null) {
      return reference;
    }
    reference = new JSSELimitedCache(size);
    reference.setSessionTimeout(resume_period);
    return reference;
  }

  public static JSSELimitedCache getInstance() {
    return reference;
  }

  public static void stop() {
    reference.cache = null;
    reference = null;
  }
//////////////////////////////////////////////////////////////////////////
////////////////iaik.security.ssl.SessionManager
//////////////////////////////////////////////////////////////////////////
  protected Session getSession(SSLTransport sslTransport, Object o) {
    guard.startRead();
    try {
      if (o == null) {
        return null;
      }
      byte[] sessionID = ((SessionID) o).getID();
      SSLSessionWrapper session = (SSLSessionWrapper) cache.lookupCache(new KeyWrapper(sessionID));
      if (session == null) {
        return null;
      }
      if (!session.getTransport().equals(sslTransport.getRemotePeerId())) {
        return null;
      }
      session.resume();
      return session.getSession();
    } finally {
      guard.endRead();
    }
  }

  protected void cacheSession(SSLTransport sslTransport, Session session) {
    guard.startWrite();
    try {
      SSLSessionWrapper wrapper = new SSLSessionWrapper(session, sslTransport);
      wrapper.setResumePeriod(resume_period);
      cache.addCache(new KeyWrapper(((SessionID)session.getID()).getID()), wrapper);
    } finally {
      guard.endWrite();
    }
  }

//////////////////////////////////////////////////////////////////////////
////////////////javax.net.ssl.SSLSessionContext
//////////////////////////////////////////////////////////////////////////
  public Enumeration getIds() {
    guard.startRead();
    try {
      Iterator iter = cache.keyIterator();
      Vector v = new Vector(10);
      while (iter.hasNext()) {
        v.addElement(((KeyWrapper) iter.next()).getKey());
      }
      return v.elements();
    } finally {
      guard.endRead();
    }
  }

  public SSLSession getSession(byte[] bytes) {
    guard.startRead();
    try {
      SSLSessionWrapper session = (SSLSessionWrapper) cache.lookupCache(new KeyWrapper(bytes));
      if (session == null) {
        return null;
      }
      return new com.sap.engine.services.ssl.factory.SSLSession(session.getSession(), this);
    } finally {
      guard.endRead();
    }
  }

//////////////////////////////////////////////////////////////////////////
////////////////Management
//////////////////////////////////////////////////////////////////////////
  public int getSessionCacheSize() {
    return cache_size;
  }

  public int getSessionTimeout() {
    return resume_period;
  }

  public void setSessionCacheSize(int size) throws IllegalArgumentException {
    throw new IllegalStateException();
  }

  public void setSessionTimeout(int sec) throws IllegalArgumentException {
    if (sec < 5 * 60) {
      throw new IllegalArgumentException();
    }
    this.resume_period = sec;
  }

  protected void removeSession(byte[] sessionId) {
    guard.startWrite();
    try {
      KeyWrapper key = new KeyWrapper(sessionId);
      cache.remove(key);
    } finally {
      guard.endWrite();
    }
  }

///////////////////////////////////////////////////////////////////////
/////////////////////// TimeoutListener
///////////////////////////////////////////////////////////////////////
  public void timeout() {
    clearInvalidSessions();
  }

  public boolean check() {
    return (cache.stableSize() != 0);
  }

  protected void clearInvalidSessions() {
    guard.startWrite();
    try {
      Iterator keys = cache.keyIterator();
      Object key = null;
      SSLSessionWrapper value = null;
      Vector keysToRemove = new Vector();
      while (keys.hasNext()) {
        key = keys.next();
        value = (SSLSessionWrapper) cache.getByKey(key);
        if (!value.isActive()) {
          keysToRemove.addElement(key);
        }
      }
      for (int i = 0; i < keysToRemove.size(); i++) {
        cache.remove(keysToRemove.elementAt(i));
      }
    } finally {
      guard.endWrite();
    }
  }

  public String toString() {
    clearInvalidSessions();
    guard.startWrite();
    try {
      StringBuffer sb = new StringBuffer();
      sb.append(cache.stableSize() + " sessions kept.\r\n");
      Iterator values = cache.valueIterator();
      SSLSessionWrapper session = null;
      int i = 0;
      while (values.hasNext()) {
        session = (SSLSessionWrapper) values.next();
        sb.append(++i + ") ");
        sb.append(session.toString() + "\r\n");
      }
      return sb.toString();
    } finally {
      guard.endWrite();
    }
  }

  public String toString(byte[] sessionKey) {
    guard.startRead();
    try {
      SSLSessionWrapper session = (SSLSessionWrapper) cache.getByKey(new KeyWrapper(sessionKey));
      return (session==null) ? null : session.toString();
    } finally {
      guard.endRead();
    }
  }

  public void clear() {
    guard.startWrite();
    try {
      cache = new Cache(10, this.cache_size);
    } finally {
      guard.endWrite();
    }
  }
}
