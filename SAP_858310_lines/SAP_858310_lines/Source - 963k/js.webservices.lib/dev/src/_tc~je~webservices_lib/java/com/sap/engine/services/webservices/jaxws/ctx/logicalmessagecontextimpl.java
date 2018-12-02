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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.soap.SOAPBody;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sap.engine.services.webservices.espbase.messaging.impl.LogicalMessageImpl;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 24, 2006
 */
public class LogicalMessageContextImpl implements LogicalMessageContext {
  private MessageContextImpl msgCtxImpl;
  
  public MessageContextImpl getWrappedContext() {
    return msgCtxImpl;
  }

  public LogicalMessageContextImpl(MessageContextImpl msgCtx) {
    this.msgCtxImpl = msgCtx;
  }
  
  public LogicalMessage getMessage() {
    LogicalMessageImpl lm = new LogicalMessageImpl();
    SOAPBody sBody;
    try {
      sBody = msgCtxImpl.getInternalSOAPMessage().getSOAPBody();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Element bodyContent = null;
    NodeList ch_nodes = sBody.getChildNodes();
    for (int i = 0; i < ch_nodes.getLength(); i++) {
      if (ch_nodes.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
        bodyContent = (Element) ch_nodes.item(i);
        break;
      }
    }
    if (bodyContent != null) { //in case of notempty body  
      lm.init(sBody);
    }
    return lm;
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
