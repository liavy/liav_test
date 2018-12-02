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

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerRegistry;

/**
 * Implementation of <code>com.sap.engine.services.webservices.jaxrpc.handlers.HandlerRegistry</code> interface.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-3-11
 */
public class HandlerRegistryImpl implements HandlerRegistry {
  
  private Hashtable hChains = new Hashtable();
  
  public List getHandlerChain(QName portQName) {
    if (portQName == null) {
      throw new IllegalArgumentException("Invalid port name 'null'");
    }
    return (List) hChains.get(portQName);
  }

  public void setHandlerChain(QName portQName, List list) {
    if (portQName == null || list == null) {
      throw new IllegalArgumentException("Invalid port/list value 'null'");
    }
    hChains.put(portQName, list);
  }
  /**
   * Removes list from reqistry.
   * @param portQName port's name
   * @return removed list, or null if there is no port with such name.
   */
  public List removeHandlerChain(QName portQName) {
    return (List) hChains.remove(portQName);
  }
  /**
   * Returns a Map representing the internal mapping table.
   * The map is unmodifiable.
   */
  public Map getInternalTable() {
    return hChains; 
  }
}
