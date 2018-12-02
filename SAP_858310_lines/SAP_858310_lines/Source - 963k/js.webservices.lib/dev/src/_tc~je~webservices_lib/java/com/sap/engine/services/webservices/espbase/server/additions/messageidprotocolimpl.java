/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.server.additions;

import org.w3c.dom.Element;

import com.sap.engine.interfaces.sca.logtrace.CallEntry;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.MessageIDProtocol;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.BaseProtocolException;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.TBindingResourceAccessor;

/**
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class MessageIDProtocolImpl implements MessageIDProtocol/*, ProtocolFactory*/ {
  //The messageId header localName
  static final String MSID_HEADER_LOCALNAME  =  "messageId";
  //The messageId header ns
  static final String MSID_HEADER_NS  =  "http://www.sap.com/webas/640/soap/features/messageId/";
  //The messageId property name, under which value is mapped in persistent context
  static final String PERSISTENT_MSGID_PROPERTY =  MESSAGEID_PROTOCOLNAME + "_" + "messageID";

  public String getMessageID(ConfigurationContext ctx) {
    ProviderContextHelper context = (ProviderContextHelper) ctx;
    ConfigurationContext persistent = context.getPersistableContext();
    return (String) persistent.getProperty(PERSISTENT_MSGID_PROPERTY);
  }

  public String getProtocolName() {
    return MESSAGEID_PROTOCOLNAME;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.esp.Protocol#handleFault(com.sap.engine.interfaces.webservices.esp.ConfigurationContext)
   */
  public int handleFault(ConfigurationContext ctx) throws ProtocolException {
    ProviderContextHelper context = (ProviderContextHelper) ctx;
    MessageException mE = context.getMessageException(); 
    try {
      Message msg = context.createErrorMessage(mE);
      context.setMessage(msg);
      context.setMessageSemantic(ProviderContextHelper.FAULT_MSG);
    } catch (RuntimeProcessException rtpE) {
      throw new BaseProtocolException(rtpE);
    }
    return ProviderProtocol.CONTINUE;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.esp.Protocol#handleRequest(com.sap.engine.interfaces.webservices.esp.ConfigurationContext)
   */
  public int handleRequest(ConfigurationContext ctx) throws ProtocolException, MessageException {
    ProviderContextHelper context = (ProviderContextHelper) ctx;
    
    //check whether transport binding is POST or GET. In this case this protocol has nothing to do (no soap content) - return CONTINUE.
    InterfaceMapping mapping = context.getStaticContext().getInterfaceMapping();
    String bType = mapping.getProperty(InterfaceMapping.BINDING_TYPE);
    if (InterfaceMapping.HTTPGETBINDING.equals(bType) || InterfaceMapping.HTTPPOSTBINDING.equals(bType)) {
      return CONTINUE;
    }
    
    SOAPMessage msg = null;
    try {
      Message tmpMsg = context.getMessage();
//      if (tmpMsg instanceof InternalMIMEMessage) {
//        msg = ((InternalMIMEMessage) tmpMsg).getSOAPMessage();        
//      } else 
      if (tmpMsg instanceof SOAPMessage) {
        msg = (SOAPMessage) tmpMsg;
      } else {
        throw new BaseProtocolException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.UNKNOW_MESSAGE_INSTANCE, new Object[]{tmpMsg});
      }
    } catch (Exception e) {
      throw new BaseProtocolException(e);
    }

    Element[] headers = msg.getSOAPHeaders().getHeaders();

    Element curHeader;
    for (int i = 0; i < headers.length; i++) {
      curHeader = (Element) headers[i];
      if (curHeader.getLocalName().equals(MSID_HEADER_LOCALNAME) &&
          curHeader.getNamespaceURI().equals(MSID_HEADER_NS)) {
        //setting the value in persistent context
        ConfigurationContext persistent = context.getPersistableContext(); 
        String messageId = curHeader.getFirstChild().getNodeValue();
        persistent.setProperty(PERSISTENT_MSGID_PROPERTY, messageId);

        WSLogTrace.setHeader(CallEntry.HEADER_REQ_MESSAGE_ID, messageId);
        
        return CONTINUE;
      }
    }
    return CONTINUE;
//TODO - check for protocols configuration.
//    //in case MessageId header is missing, return fault message
//    BaseMessageException mE = new BaseMessageException(TBindingResourceAccessor.getResourceAccessor(),
//            ExceptionConstants.MISSING_MESSAGEID_HEADER, new Object[]{MSID_HEADER_NS, MSID_HEADER_LOCALNAME});
//    return BACK;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.esp.Protocol#handleResponse(com.sap.engine.interfaces.webservices.esp.ConfigurationContext)
   */
  public int handleResponse(ConfigurationContext context) throws ProtocolException {
    return ProviderProtocol.CONTINUE;
  }


//============================= OLD implementations
//  public Config getRequiredFeatures() {
//    return null;
//  }
//
//  public void init() {
//  }
//
//  public boolean handleFault(ProtocolContext context) throws ProtocolException {
//    return false;
//  }
//
//  public boolean handleRequest(ProtocolContext context) throws ProtocolException, MessageException {
//    InternalSOAPMessage msg = null;
//    try {
//      msg = (InternalSOAPMessage) context.getRuntimeContext().getRuntimeTransportBinding().getRawMessage();
//    } catch (Exception e) {
//      throw new BaseProtocolException(e);
//    }
//
//    ArrayList headers = msg.getHeaders();
//
//    Element curHeader;
//    for (int i = 0; i < headers.size(); i++) {
//      curHeader = (Element) headers.get(i);
//      if (curHeader.getLocalName().equals(MSID_HEADER_LOCALNAME) &&
//              curHeader.getNamespaceURI().equals(MSID_HEADER_NS)) {
//        this.messageId = curHeader.getFirstChild().getNodeValue();
//        return true;
//      }
//    }
//
//    //in case MessageId header is missing, return fault message
//    BaseMessageException mE = new BaseMessageException(TBindingResourceAccessor.getResourceAccessor(),
//            ExceptionConstants.MISSING_MESSAGEID_HEADER, new Object[]{MSID_HEADER_NS, MSID_HEADER_LOCALNAME});
//    try {
//      context.getRuntimeContext().getRuntimeTransportBinding().createFaultMessage(mE);
//    } catch (TransportBindingException e) {
//      throw new BaseProtocolException(e);
//    }
//
//    return false;
//  }
//
//  public boolean handleResponse(ProtocolContext context) throws ProtocolException {
//    return false;
//  }
    
}
