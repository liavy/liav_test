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

import com.sap.engine.interfaces.webservices.runtime.ProtocolException;

/**
 * Client Protocol notify interface.
 * Client protocols implementing this interface are called by the client 
 * service instance to be notified on service initialization and destroy.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public interface ClientProtocolNotify {
  
  /**
   * Called by the client service for all service level protocols
   * that implement this interface.
   * @param serviceContext
   * @throws ProtocolException
   */
  public void serviceInit(ClientServiceContext serviceContext) throws ProtocolException;
  
  /**
   * Called by the client service for all service level protocols
   * that implement this interface.
   * @param serviceContext
   * @throws ProtocolException
   */
  public void serviceDestroy(ClientServiceContext serviceContext) throws ProtocolException;
  
  /**
   * This event is called when new port instance is created by the client application.
   * @param portContext
   * @throws ProtocolException
   */
  public void portCreate(ClientConfigurationContext  portContext) throws ProtocolException;
  
  /**
   * The event is called when port instance is destroyed.
   * @param portContext
   * @throws ProtocolException
   */
  public void portDestroy(ClientConfigurationContext portContext) throws ProtocolException;
  
}
