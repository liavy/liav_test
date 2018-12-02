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

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.AbstractMessage;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientProtocolException;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;

/**
 * Default Mime Protocol Implementation.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public abstract class MimeProtocolImpl implements MimeProtocol {

  /**
   * Called when request is sent to service.
   * Return false to block other contexts from calling.
   */
  public boolean handleRequest(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    if (message instanceof ClientMimeMessage) {
      return handleRequest((ClientMimeMessage) message, context);      
    } else {
      throw new ClientProtocolException("Only SOAP Messages are accepted by SOAP Protocol !");
    }
  }

  /**
   * Called when response is recieved from service.
   */
  public boolean handleResponse(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    if (message instanceof  ClientMimeMessage) {
      return handleResponse((ClientMimeMessage) message, context);
    } else {
      throw new ClientProtocolException("Only SOAP Messages are accepted by SOAP Protocol !");
    }
  }

  /**
   * Called when fault is recieved from service.
   */
  public boolean handleFault(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    if (message instanceof  ClientMimeMessage) {
      return handleFault((ClientMimeMessage) message, context);
    } else {
      throw new ClientProtocolException("Only SOAP Messages are accepted by SOAP Protocol !");
    }    
  }
  
  
}
