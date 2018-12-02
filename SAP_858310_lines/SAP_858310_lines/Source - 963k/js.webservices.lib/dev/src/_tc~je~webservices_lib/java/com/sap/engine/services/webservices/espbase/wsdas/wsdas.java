/*
 * Copyright (c) 2007 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.wsdas;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Map;

import commonj.sdo.helper.HelperContext;

/**
 * Web Services Data Access Service for invoking operations of a specific port.
 * 
 * Copyright (c) 2007, SAP-AG
 * @author Dimitar Angelov
 * @author Mariela Todorova
 * @version 1.0, May 2, 2007
 */
public interface WSDAS {
  
  /**
   * Returns operation configuration for the specified operation.
   * 
   * @param opName operation name
   * @return operation configuration
   */
  public OperationConfig getOperationCfg(String opName);
  
  /**
   * Invokes the operation associated with the operation configuration.
   * 
   * @param opCfg operation configuration
   * @throws RemoteException if communication error occurs
   * @throws InvocationTargetException if application error occurs
   */
  public void invokeOperation(OperationConfig opCfg) throws RemoteException, InvocationTargetException;  

  /**
   * Invokes the operation associated with the operation configuration.
   * 
   * @param opCfg operation configuration
   * @param options map of options 
   * @throws RemoteException if communication error occurs
   * @throws InvocationTargetException if application error occurs
   */
  public void invokeOperation(OperationConfig opCfg, Map options) throws RemoteException, InvocationTargetException;  
  
  /**
   * Returns the helper context for this wsdas.
   * 
   * @return helper context
   */
  public HelperContext getHelperContext();
  
  /**
   * Returns all operations for this wsdas.
   * 
   * @return array of operation names
   */
  public String[] getOperationNames();
  
}
