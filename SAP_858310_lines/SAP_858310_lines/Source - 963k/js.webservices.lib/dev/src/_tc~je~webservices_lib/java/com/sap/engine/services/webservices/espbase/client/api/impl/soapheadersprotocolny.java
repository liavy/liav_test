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

import java.util.ArrayList;
import org.w3c.dom.Element;
import com.sap.engine.interfaces.webservices.esp.*;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.messaging.*;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;

/**
 * SOAP Headers protocol Implementation.
 * The protocom uses two dynamic properties. One that contains the outgoing headers and one that contains the incoming headers.
 * The task of the protocol is to appeng the outgoing heades upon message serialziation and to extract the incoming soap headers.
 * The two dynamic properties used are hastables that map header element QName to Header contents. 
 * @version 1.0 (2006-1-13)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class SOAPHeadersProtocolNY implements ConsumerProtocol, ProtocolExtensions {
  
  public static final String SOAP_HEADER_PROTOCOL = "SOAPHeaderProtocol";
  
  public static final String DYNAMIC_OUTGOING_HEADERS = "OutgoingHeaders";
  public static final String DYNAMIC_INCOMING_HEADERS = "IncomingHeaders";
  
  /**
   * Default constructor.
   */
  public SOAPHeadersProtocolNY() {
    super();
  }
  
  /**
   * Returns protocol name.
   * @return
   */
  public String getProtocolName() {
    return SOAP_HEADER_PROTOCOL;
  }

  /**
   * The SOAP Headers are attached before message serialization so this methods just continues the message processing.
   * @param arg0
   * @return
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   * @throws com.sap.engine.interfaces.webservices.runtime.MessageException
   */
  public int handleRequest(ConfigurationContext arg0) throws ProtocolException, MessageException {
    return CONTINUE;
  }

  /**
   * The message is deserialized and response objects are parsed. 
   * @param arg0
   * @return
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public int handleResponse(ConfigurationContext arg0) throws ProtocolException {
    return CONTINUE;
  }

  /**
   * @param arg0
   * @return
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public int handleFault(ConfigurationContext arg0) throws ProtocolException {
    // TODO Auto-generated method stub
    return CONTINUE;
  }

  /**
   * This method is called after message body deserialization. All left headers from the message are loaded into dynamic context 
   * property for later inspection. 
   * @param arg0
   * @return
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   * @throws com.sap.engine.interfaces.webservices.runtime.MessageException
   */
  public int afterDeserialization(ConfigurationContext arg0) throws ProtocolException, MessageException {
    ClientConfigurationContext clientContext = (ClientConfigurationContext) arg0;
    ConfigurationContext dynamicContext = clientContext.getDynamicContext();
    Message message = clientContext.getMessage();        
//    if (message instanceof MIMEMessage) {
//      message = ((MIMEMessage) message).getSOAPMessage();
//    }
    if (message != null && message instanceof SOAPMessage) {
      // SOAP Message case
      SOAPMessage soapMessage = (SOAPMessage) message;
      SOAPHeaderList soapHeaders = soapMessage.getSOAPHeaders();
      ArrayList incomingHeaders = (ArrayList) dynamicContext.getProperty(DYNAMIC_INCOMING_HEADERS);
      if (incomingHeaders == null) {
        incomingHeaders = new ArrayList();
        dynamicContext.setProperty(DYNAMIC_INCOMING_HEADERS,incomingHeaders);
      }
      Element[] elements = soapHeaders.getHeaders();
	    for (int i=0; i < elements.length; i++) {
	      incomingHeaders.add(elements[i]);
	    }
	    // Clears outgoing headers
      ArrayList outgoingHeaders = (ArrayList) dynamicContext.getProperty(DYNAMIC_OUTGOING_HEADERS);
      if (outgoingHeaders != null) {
        outgoingHeaders.clear();
      }
    }
    return CONTINUE;
  }
  
  /**
   * This method is called before message body serialization. This method adds the additional soap headers to the message before the message
   * body is serialized. The dynamic context contains a list of all outgoing headers. All incoming headers are cleared before
   * method invocation.
   * @param arg0 
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void beforeSerialization(ConfigurationContext arg0) throws ProtocolException {
    ClientConfigurationContext clientContext = (ClientConfigurationContext) arg0;
    ConfigurationContext dynamicContext = clientContext.getDynamicContext();
    Message message = clientContext.getMessage();        
//    if (message instanceof MIMEMessage) {
//      message = ((MIMEMessage) message).getSOAPMessage();
//    }
    if (message != null && message instanceof SOAPMessage) {
      // SOAP Message case
      SOAPMessage soapMessage = (SOAPMessage) message;
      SOAPHeaderList soapHeaders = soapMessage.getSOAPHeaders();
      ArrayList outgoingHeaders = (ArrayList) dynamicContext.getProperty(DYNAMIC_OUTGOING_HEADERS);
      if (outgoingHeaders != null) {
        for (int i=0; i < outgoingHeaders.size(); i++) {
          Element soapHeader = (Element) outgoingHeaders.get(i);
          soapHeaders.addHeader(soapHeader);
        }
        // Clears outgoing headers
        outgoingHeaders.clear();        
      }
      ArrayList incomingHeaders = (ArrayList) dynamicContext.getProperty(DYNAMIC_INCOMING_HEADERS);
      if (incomingHeaders != null) {
        incomingHeaders.clear();
      }
    }
  }
  
  /**
   * This method is invoked by the client runtime when the client runtime is restarted from hybernation state.
   * @param arg0
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void afterHibernation(ConfigurationContext arg0) throws ProtocolException {
    // Restores the required dynamic property
    ClientConfigurationContext clientContext = (ClientConfigurationContext) arg0;
    ConfigurationContext dynamicContext = clientContext.getDynamicContext();
    dynamicContext.setProperty(DYNAMIC_OUTGOING_HEADERS,new ArrayList());
    dynamicContext.setProperty(DYNAMIC_INCOMING_HEADERS,new ArrayList());    
  }
  /**
   * This method is invoked by the client runtime upon client runtime hybernation.
   * @param arg0
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void beforeHibernation(ConfigurationContext arg0) throws ProtocolException {
    // Clean ups the used dynamic properties.
    ClientConfigurationContext clientContext = (ClientConfigurationContext) arg0;
    ConfigurationContext dynamicContext = (ConfigurationContext) clientContext.getDynamicContext();
    dynamicContext.removeProperty(DYNAMIC_INCOMING_HEADERS);
    dynamicContext.removeProperty(DYNAMIC_OUTGOING_HEADERS);
  }
  
  /**
   * This event is not used int web services clients. 
   * @param arg0
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void finishHibernation(ConfigurationContext arg0) throws ProtocolException {
  }
    
  /**
   * This event is not used in web services clients.
   * @param arg0
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void finishMessageDeserialization(ConfigurationContext arg0) throws ProtocolException {
  }
}
