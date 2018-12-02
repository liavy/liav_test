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
package com.sap.engine.services.webservices.espbase.client.jaxws.cts;

import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class SAPSSOHandler implements SOAPHandler<SOAPMessageContext> {    
  
  /**
   * Constant to pass the interface_definition_id to the SAP Handler.
   */
  public static final String INTERFACE_DEF_ID = "interface_def_id";
  /**
   * Constant to pass the logical port name to the SAP Handler.
   */
  public static final String PORT_NAME = "port_name";
  
  private static final Location LOC = Location.getLocation(SAPSSOHandler.class);

  private static String handlerName;
  
  public SAPSSOHandler() {
    if (LOC != null) {
      LOC.debugT("Creation of SOAP Handler ["+this.getClass().getName()+"].");
    }
    handlerName = this.getClass().getName(); 
  }

  public Set getHeaders() {
    return null;
  }

  public void close(MessageContext arg0) {
  }

  public boolean handleFault(SOAPMessageContext arg0) {
    return true;
  }
  
  /**
   * Handles the request message sent and adds the SSO Header.
   */
  public boolean handleMessage(SOAPMessageContext arg0) {    
    if (Boolean.TRUE.equals(arg0.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) { 
      // This is outbound SOAP Message
      QName serviceName = (QName) arg0.get(SOAPMessageContext.WSDL_SERVICE);
      QName operationName = (QName) arg0.get(SOAPMessageContext.WSDL_OPERATION);              
      if (LOC != null) {
        LOC.debugT("Handler ["+handlerName+"] processing request message for client ["+serviceName+"] and operation ["+operationName+"].");
      }
      String interfaceDefId = (String) arg0.get(SAPSSOHandler.INTERFACE_DEF_ID);
      String logicalPortName = (String) arg0.get(SAPSSOHandler.PORT_NAME);               
      try {
        String ticket = com.sap.engine.services.webservices.espbase.client.dynamic.DestinationsHelper.getClientAssertionTicket(interfaceDefId, logicalPortName);
        if (ticket != null) {
          MimeHeaders mimeHeaders = arg0.getMessage().getMimeHeaders();          
          mimeHeaders.addHeader("mysapsso2",ticket);          
        } else {
          throw new ProtocolException("JAX-WS Handler ["+handlerName+"] could not create SSO ticket. Could not find destination for ["+logicalPortName+"] and interface_id ["+interfaceDefId+"] for interface ["+serviceName+"].");
        }
      } catch (Throwable t) {
        if (LOC != null) {
          LOC.traceThrowableT(Severity.DEBUG, "JAX-WS Handler ["+this.getClass().getName()+"] has thrown exception in [DestinationsHelper.getClientAssertionTicket(\""+interfaceDefId+"\",\""+logicalPortName+")] method. Cause: "+t.getLocalizedMessage()+".", t);
        }                  
        throw new ProtocolException("JAX-WS Handler ["+this.getClass().getName()+"] has thrown exception in [DestinationsHelper.getClientAssertionTicket(\""+interfaceDefId+"\",\""+logicalPortName+")] method. Cause: "+t.getLocalizedMessage()+".",t);
      }
    } else {
      System.out.println(arg0.get(MessageContext.HTTP_RESPONSE_HEADERS));
      System.out.println("Processing Response");
    }
    return true;
  }


}
