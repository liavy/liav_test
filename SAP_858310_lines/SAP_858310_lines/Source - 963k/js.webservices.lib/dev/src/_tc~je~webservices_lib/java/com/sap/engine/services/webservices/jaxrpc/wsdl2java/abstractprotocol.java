/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;


/**
 * This class is parent of all protocol implementations.
 * It is based on the abstract perception for webservice request/response/fault as described in wsdl specification working
 * with abstract messages. It's binding implementation resposibility to call and handle protocols in correct manner
 * and to provide the protocol implementor with nessesary base class for implementing new protocols and also binding specific 
 * message class. The protocol methods are called by default before processing the message. 
 *  
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */

public interface AbstractProtocol extends FeatureProvider {

  public static final String OPERATION_SUBCONTEXT = "operations";
  /**
   * Valled for protocol initialization.
   * @param context - feature configuration.
   * @throws ClientProtocolException
   */
  public void init(PropertyContext context) throws ClientProtocolException;
  /**
   * Called when request is sent to service.
   * Return false to block other contexts from calling. 
   */ 
  public boolean handleRequest(AbstractMessage message, PropertyContext context) throws ClientProtocolException;
  
  /**
   * Called when response is recieved from service.
   * Return false to block other contexts from calling. 
   */ 
  public boolean handleResponse(AbstractMessage message, PropertyContext context) throws ClientProtocolException;
  
  /**
   * Called when fault is recieved from service.
   * Return false to block other contexts from calling.   
   */
  public boolean handleFault(AbstractMessage message, PropertyContext context) throws ClientProtocolException;
    
}
