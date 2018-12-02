/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxrpc.handlers;

import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPMessage;

/**
 * Implementation of <code>javax.xml.rpc.handler.soap.SOAPMessageContext</code> interface.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-3-11
 */
public class SOAPMessageContextImpl implements SOAPMessageContext {
  
  private SOAPMessage message;
  private String[] roles;
  private Hashtable properties = new Hashtable();
  
  public SOAPMessage getMessage() {
    return message;
  }

  public String[] getRoles() {
    return roles;
  }

  public void setRoles(String[] roles) {
    this.roles = roles;
  }

  public void setMessage(SOAPMessage message) {
    this.message = message;
  }

  public boolean containsProperty(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Illegal property name 'null'");
    }
    return properties.containsKey(name);
  }

  public Object getProperty(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Illegal property name 'null'");
    }
    return properties.get(name);
  }

  public Iterator getPropertyNames() {
    return properties.keySet().iterator();
  }

  public void removeProperty(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Illegal property name 'null'");
    }
    properties.remove(name);
  }

  public void setProperty(String name, Object value) {
    if (name == null) {
      throw new IllegalArgumentException("Illegal property name 'null'");
    }
    if (value == null) {
      throw new IllegalArgumentException("Illegal value 'null'");
    }
    properties.put(name, value);
  }

}
