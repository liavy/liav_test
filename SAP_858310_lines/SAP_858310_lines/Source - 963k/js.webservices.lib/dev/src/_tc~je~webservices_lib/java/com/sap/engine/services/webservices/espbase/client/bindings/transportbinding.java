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

import java.rmi.RemoteException;

/**
 * Web Services client framework transport binding interface
 * @version 2.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public interface TransportBinding {
  
  /**
   * Invokes web operation. The parameters include also fault and in/out params. 
   * OUT and INOUT params can not be null.
   * Also the parameters must correspond to the described in the operation mapping
   * parameters.
   * @param parameters
   * @param opMapping
   */
  public void call(ClientConfigurationContext context) throws java.rmi.RemoteException;   
  
  public void sendMessage(ClientConfigurationContext context) throws RemoteException;  
}