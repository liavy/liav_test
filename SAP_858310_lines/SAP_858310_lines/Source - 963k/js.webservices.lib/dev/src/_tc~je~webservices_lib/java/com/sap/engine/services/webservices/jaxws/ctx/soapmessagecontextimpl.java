/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.ctx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Element;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 24, 2006
 */
public class SOAPMessageContextImpl implements javax.xml.ws.handler.soap.SOAPMessageContext {
  private MessageContextImpl msgCtxImpl;
  
  public MessageContextImpl getWrappedContext() {
    return msgCtxImpl;
  }
  
  public SOAPMessageContextImpl(MessageContextImpl msgCtx) {
    this.msgCtxImpl = msgCtx;
  }

  public Object[] getHeaders(QName arg0, JAXBContext arg1, boolean arg2) {
    try {
      SOAPMessage msg = getMessage();
      ArrayList res = new ArrayList();
      Iterator itr  = msg.getSOAPHeader().getChildElements(arg0);
      if (itr.hasNext()) {
        Unmarshaller unm = arg1.createUnmarshaller();
        while (itr.hasNext()) {
          Element h = (Element) itr.next();
          Object o = unm.unmarshal(h);
          res.add(o);
        }
      }
      return res.toArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  } 

  public SOAPMessage getMessage() {
    return msgCtxImpl.getInternalSOAPMessage();
  }

  public void setMessage(SOAPMessage arg0) {
    msgCtxImpl.setInternalSOAPMessage(arg0);
  }

  public Set<String> getRoles() {
    return new HashSet<String>();
  }

  public Scope getScope(String arg0) {
    return msgCtxImpl.getScope(arg0);
  }

  public void setScope(String arg0, Scope arg1) {
    msgCtxImpl.setScope(arg0, arg1);
  }

  public void clear() {
    msgCtxImpl.clear();
  }

  public boolean containsKey(Object key) {
    return msgCtxImpl.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return msgCtxImpl.containsValue(value);
  }

  public Set<Entry<String, Object>> entrySet() {
    return msgCtxImpl.entrySet();
  }

  public Object get(Object key) {
    return msgCtxImpl.get(key);
  }

  public boolean isEmpty() {
    return msgCtxImpl.isEmpty();
  }

  public Set<String> keySet() {
    return msgCtxImpl.keySet();
  }

  public Object put(String key, Object value) {
    return msgCtxImpl.put(key, value);
  }

  public void putAll(Map<? extends String, ? extends Object> t) {
    msgCtxImpl.putAll(t);
  }

  public Object remove(Object key) {
    return msgCtxImpl.remove(key);
  }

  public int size() {
    return msgCtxImpl.size();
  }

  public Collection<Object> values() {
    return msgCtxImpl.values();
  }
}
