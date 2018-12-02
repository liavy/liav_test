/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;

/**
 * Interface used by Bindings to obtain protocol instances out of protocol 
 * list configuration.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public interface ConsumerProtocolProvider {
  
  public ConsumerProtocol[] getProtocols(String protocolList);
  
  public void registerProtocol(String protocolName, ConsumerProtocol protocol);
  
  public boolean unregisterProtocol(String protocolName);
    
}
