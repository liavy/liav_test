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

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.wsdas.impl.WSDASFactoryImpl;
import commonj.sdo.helper.HelperContext;

/**
 * WSDASFactory is the entry point for creating and using Web Services Data Access Service.
 * It provides methods for WSDAS creation for both single and mass configuration cases.    
 * 
 * Copyright (c) 2007, SAP-AG
 * @author Dimitar Angelov
 * @author Mariela Todorova
 * @version 1.0, May 2, 2007
 */
public abstract class WSDASFactory {

  /**
   * Returns new instance of WSDASFactory.
   *  
   * @return wsdas factory
   */
  public static WSDASFactory newInstance() {
    return new WSDASFactoryImpl();
  }

  /**
   * Returns new instance of WSDASFactory with internal caching turned on.
   *  
   * @return wsdas factory
   */
  public static WSDASFactory newInstanceWithCache(){
    return new WSDASFactoryImpl(true);
  }

  /**
   * Creates WSDAS for the single configuration case. 
   * 
   * @param logicDestName destination name
   * @param intfQName port type QName
   * @param sdoContext helper context 
   * @return wsdas for the specified destination, port type and helper context   
   * @throws Exception if wsdas creation encounters a problem
   */
  public abstract WSDAS createWSDAS(String logicDestName, QName intfQName, HelperContext sdoContext) throws Exception;  

  /**
   * Creates WSDAS for the mass configuration case.  
   * 
   * @param appName application name
   * @param serviceRefID service reference ID
   * @param sdoContext helper context 
   * @return wsdas for the specified application, service reference and helper context 
   * @throws Exception if wsdas creation encounters a problem
   */
  public abstract WSDAS createWSDAS(String appName, String serviceRefID, HelperContext sdoContext) throws Exception;


  /**
   * Clears the internal cache.
   *  
   */
  public abstract void clearCache();

  /**
   * Removes the service specified by appName, serviceRefID from the internal cache
   *  
   */
  public abstract void purgeServiceFromCache(String appName, String serviceRefID);


  /**
   * Removes the service specified by logicDestName, intfQName from the internal cache
   *  
   */
  public abstract void purgeServiceFromCache(String logicDestName, QName intfQName);


  /**
   * Sets the size of the internal cache
   *  
   */
  public abstract void setCacheCapacity(int capacity);

} 

