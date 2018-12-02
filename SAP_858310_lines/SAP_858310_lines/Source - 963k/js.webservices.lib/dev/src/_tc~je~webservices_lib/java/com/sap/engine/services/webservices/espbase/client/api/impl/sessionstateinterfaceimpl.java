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
package com.sap.engine.services.webservices.espbase.client.api.impl;
import java.util.Iterator;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.api.SessionState;
import com.sap.engine.services.webservices.espbase.client.api.SessionStateInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;

public class SessionStateInterfaceImpl implements SessionStateInterface {
  
  public static final String[] STORED_DYNAMIC_PROPS = {
    PublicProperties.F_SESSION_COOKIE
  };
    
  private ClientConfigurationContext clientContext;
  
  public SessionStateInterfaceImpl(ClientConfigurationContext context) {
    this.clientContext = context;
  }  
  
  public SessionState exportState() {
    SessionStateImpl sessionState = new SessionStateImpl();    
    ConfigurationContext dynamicContext = clientContext.getDynamicContext();
    for (int i=0; i<STORED_DYNAMIC_PROPS.length;i++) {
      sessionState.dynamicContext.put(STORED_DYNAMIC_PROPS[i],dynamicContext.getProperty(STORED_DYNAMIC_PROPS[i]));
    }
    ConfigurationContext persistableContext = clientContext.getPersistableContext();
    Iterator props = persistableContext.properties();
    while (props.hasNext()) {
      String key = (String) props.next();
      String value = (String) persistableContext.getProperty(key);
      sessionState.persistableContext.put(key,value);
    }
    return sessionState;
  }
  
  public void importState(SessionState state) {
    SessionStateImpl stateImpl = (SessionStateImpl) state;
    ConfigurationContext dynamicContext = this.clientContext.getDynamicContext();
    Iterator keys = stateImpl.dynamicContext.keySet().iterator();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      Object value = stateImpl.dynamicContext.get(key);
      dynamicContext.setProperty(key,value);
    }
    ConfigurationContext persistableContext = this.clientContext.getPersistableContext();
    keys = stateImpl.persistableContext.keySet().iterator();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      Object value = stateImpl.persistableContext.get(key);
      persistableContext.setProperty(key,value);
    }    
  }
  
}
