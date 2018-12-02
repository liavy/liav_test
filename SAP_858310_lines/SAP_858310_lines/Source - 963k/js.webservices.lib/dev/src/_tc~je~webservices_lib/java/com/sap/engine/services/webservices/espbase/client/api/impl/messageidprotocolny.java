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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.sap.engine.interfaces.sca.logtrace.CallEntry;
import com.sap.engine.interfaces.sca.logtrace.ESBTracer;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUID;
import com.sap.guid.IGUIDGenerator;
import com.sap.guid.IGUIDGeneratorFactory;

/**
 * Implementation of the message id protocol using the NY client runtime.
 * @version 1.0 (2006-1-20)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class MessageIDProtocolNY implements ConsumerProtocol, ProtocolExtensions {

  public static final String NAME = "MessageIdProtocol";
  public static final String HEADER_NAME = "messageId";
  public static final String HEADER_NAMESPACE = "http://www.sap.com/webas/640/soap/features/messageId/";
  public static final String PREFIX = "uuid:";
  public static final String PERSISTABLE_GUID = "MessageGUID";

  private IGUIDGeneratorFactory factory;
  private IGUIDGenerator generator;
  
  /**
   * Default constructor
   *
   */
  public MessageIDProtocolNY() {
    factory = GUIDGeneratorFactory.getInstance();
    generator = factory.createGUIDGenerator();    
  }
  
  /**
   * @param arg0
   * @return
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   * @throws com.sap.engine.interfaces.webservices.runtime.MessageException
   */
  public int afterDeserialization(ConfigurationContext arg0) throws ProtocolException, MessageException {
    return CONTINUE;
  }

  /**
   * This method is called before message content serialization.
   * @param arg0
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void beforeSerialization(ConfigurationContext arg0) throws ProtocolException {    
    ClientConfigurationContext clientContext = (ClientConfigurationContext) arg0;
    // Clears old dynamic property
    clientContext.getPersistableContext().removeProperty(PERSISTABLE_GUID);    
    BindingData bData = clientContext.getStaticContext().getRTConfig();
    PropertyListType propertyList = bData.getSinglePropertyList();
    PropertyType staticValue = propertyList.getProperty(PublicProperties.F_MESSAGEID_NAMESPACE,PublicProperties.F_MESSAGEID_LOCALNAME);
    String dynamicValue = (String) clientContext.getPersistableContext().getProperty(PublicProperties.C_MESSAGEID_QNAME.toString());
    boolean isEnabled = false;
    if (dynamicValue != null && ("true".equals(dynamicValue) || "yes".equals(dynamicValue))) {
      isEnabled = true;
    }
    if (dynamicValue == null && staticValue != null) {
      if ("true".equals(staticValue.get_value()) || "yes".equals(staticValue.get_value())) {
        isEnabled = true;
      }
    }
    if (!isEnabled) {
      // The feature is disabled so do not add message id header      
      return;
    }
    Message message = clientContext.getMessage();        
//    if (message instanceof MIMEMessage) {
//      message = ((MIMEMessage) message).getSOAPMessage();
//    }
    if (message != null && message instanceof SOAPMessage) {
      // Generates and adds the message guid header
      SOAPMessage soapMessage = (SOAPMessage) message;
      SOAPHeaderList soapHeaders = soapMessage.getSOAPHeaders();
      IGUID guid = generator.createGUID();
      String guidValue = PREFIX + guid.toString();
      
      WSLogTrace.setHeader(CallEntry.HEADER_REQ_MESSAGE_ID, guidValue);
      
      Element header = soapHeaders.createHeader(new QName(HEADER_NAMESPACE,HEADER_NAME));
      Text content = header.getOwnerDocument().createTextNode(guidValue);
      header.appendChild(content);
      soapHeaders.addHeader(header);
      // Adds the current guid in the message context
      clientContext.getPersistableContext().setProperty(PERSISTABLE_GUID,PREFIX+guid.toString());
    }     
  }

  /**
   * Returns protocol name.
   * @return
   */
  public String getProtocolName() {
    return NAME;
  }

  /**
   * @param arg0
   * @return
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   * @throws com.sap.engine.interfaces.webservices.runtime.MessageException
   */
  public int handleRequest(ConfigurationContext arg0) throws ProtocolException, MessageException {
    return CONTINUE;
  }

  /**
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
    return CONTINUE;
  }

  /**
   * @param arg0
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void beforeHibernation(ConfigurationContext arg0) throws ProtocolException {
  }
  
  /**
   * Called after message restore.
   * @param arg0
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void afterHibernation(ConfigurationContext arg0) throws ProtocolException {
  }
  

  /**
   * @param arg0
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void finishMessageDeserialization(ConfigurationContext arg0) throws ProtocolException {
  }

  /**
   * @param arg0
   * @throws com.sap.engine.interfaces.webservices.runtime.ProtocolException
   */
  public void finishHibernation(ConfigurationContext arg0) throws ProtocolException {
  }


}
