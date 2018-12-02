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
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.AbstractProtocol;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientProtocolException;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;

/**
 * AbstractProtocol extension used by soap with attachments binding. To write a soap protocol extend default abstract 
 * implementation extend MimeProtocolImpl.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */

public interface MimeProtocol extends AbstractProtocol {

  /**
   * Called when request is sent to service.
   * Return false to block other contexts from calling.
   */ 
  public boolean handleRequest(ClientMimeMessage message, PropertyContext context) throws ClientProtocolException;
  
  /**
   * Called when response is recieved from service.
   */ 
  public boolean handleResponse(ClientMimeMessage message, PropertyContext context) throws ClientProtocolException;

  /**
   * Called then fault is recieved from service.   
   */ 
  public boolean handleFault(ClientMimeMessage message, PropertyContext context) throws ClientProtocolException;
  
}
