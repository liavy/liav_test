/*
 * Copyright (c) 2003 by SAP Labs Bulgaria.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.espbase.client.wsa;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.esp.Hibernation;
import com.sap.engine.interfaces.webservices.esp.Protocol;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.wsa.common.AddressingConfiguration;
import com.sap.engine.services.webservices.espbase.client.wsa.common.AddressingConstants;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.EndpointReference;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUIDGenerator;

/**
 * WS-Addressing protocol implementation.
 * @author Vladimir Videlov (vladimir.videlov@sap.com)
 * @version 1.0
 */
public class ClientAddressingProtocol implements ConsumerProtocol, Hibernation, ProtocolExtensions {
  public static final String VERSION_ID = "Id: //engine/j2ee.core.libs/dev/src/tc~je~webservices_lib/_tc~je~webservices_lib/java/com/sap/engine/services/webservices/espbase/client/wsa/ClientAddressingProtocol.java_1 ";
  //private static final Trace TRACE = new Trace(VERSION_ID);

  /**
   * Property for applying SOAP Action into HTTP headers from a WS client proxy call
   */
  public static String HTTP_SOAP_ACTION = new QName(NS.SOAPENC, "SOAPAction").toString();

  /**
   * Constant denoting the protocol name.
   */
  public final static String PROTOCOL_NAME  =  "AddressingProtocol";

  /**
   * GUID generator
   */
  protected IGUIDGenerator generator = null;

  /**
   * Document for creating DOM elements
   */
  protected Document document;

  /**
   * WS-Addressing Protocol Provider
   */
  private ClientAddressingProtocolProvider provider = ClientAddressingProtocolProvider.getInstance();

  /**
   * Default constructor
   */
  public ClientAddressingProtocol() {
    generator = GUIDGeneratorFactory.getInstance().createGUIDGenerator();
  }

  // Utility operations
  /**
   * @param ctx Configuration context.
   * @return Dynamic request configuration context for WS-Addressing
   */
  protected static ConfigurationContext getDynamicRequestContext(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getDynamicRequestContext(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext bC = ctx.getDynamicContext().createSubContext(AddressingConstants.CTX_BASE);

    //TRACE.exiting(SIGNATURE);
    return bC.createSubContext(AddressingConstants.CTX_REQUEST);
  }

  /**
   * @param ctx Configuration context.
   * @return Dynamic response configuration context for WS-Addressing
   */
  protected static ConfigurationContext getDynamicResponseContext(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getDynamicResponseContext(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext bC = ctx.getDynamicContext().createSubContext(AddressingConstants.CTX_BASE);

    //TRACE.exiting(SIGNATURE);
    return bC.createSubContext(AddressingConstants.CTX_RESPONSE);
  }

  /**
   * @param ctx Configuration context.
   * @return Persistent request configuration context for WS-Addressing
   */
  protected static ConfigurationContext getPersistableRequestContext(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getPersistableRequestContext(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext bC = ctx.getPersistableContext().createSubContext(AddressingConstants.CTX_BASE);

    //TRACE.exiting(SIGNATURE);
    return bC.createSubContext(AddressingConstants.CTX_REQUEST);
  }

  /**
   * Updates the appropriate MEP regarding current WS-RM scenario
   * @param clientCtx Client configuration context
   */
  protected void updateOperationMEP(ClientConfigurationContext clientCtx) {
    //final String SIGNATURE = "updateOperationMEP(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    String operationName = clientCtx.getOperationName();
    OperationMapping oMapping = clientCtx.getStaticContext().getInterfaceData().getOperationByJavaName(operationName);

    if (oMapping != null) {
      String oMEP = oMapping.getProperty(OperationMapping.OPERATION_MEP);

      //TRACE.debugT(SIGNATURE, "Operation name: " + operationName);
      //TRACE.debugT(SIGNATURE, "Operation MEP:  " + oMEP);

      if (oMEP.equals(OperationMapping.MEP_REQ_RESP) && isAsyncCommunication(clientCtx)) {
        //TRACE.debugT(SIGNATURE, "The operation is with request-response MEP and there is non-anon ReplyTo. Setting MEP to one-way.");
        clientCtx.getPersistableContext().setProperty(OperationMapping.OPERATION_MEP, OperationMapping.MEP_ONE_WAY);
      }
    }

    //TRACE.exiting(SIGNATURE);
  }

  // ConsumerProtocol impl
  public String getProtocolName() {
    return PROTOCOL_NAME;
  }

  public int handleRequest(ConfigurationContext configurationContext) throws ProtocolException, MessageException {
    //final String SIGNATURE = "handleRequest(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ClientConfigurationContext clientCtx = (ClientConfigurationContext) configurationContext;

    if (!AddressingConfiguration.isEnabled(clientCtx)) {
      return Protocol.CONTINUE;
    }

    int result = Protocol.CONTINUE;
    ConsumerProtocol wsaProtocol = provider.getVersionedProtocol(clientCtx);

    if (wsaProtocol != null) {
      result = wsaProtocol.handleRequest(configurationContext);
    }

    //TRACE.exiting(SIGNATURE);
    return result;
  }

  public int handleResponse(ConfigurationContext configurationContext) throws ProtocolException {
    //final String SIGNATURE = "handleResponse(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ClientConfigurationContext clientCtx = (ClientConfigurationContext) configurationContext;

    if (!AddressingConfiguration.isEnabled(clientCtx)) {
      return Protocol.CONTINUE;
    }

    int result = Protocol.CONTINUE;
    ConsumerProtocol wsaProtocol = provider.getVersionedProtocol(clientCtx);

    if (wsaProtocol != null) {
      result = wsaProtocol.handleResponse(configurationContext);
    }

    //TRACE.exiting(SIGNATURE);
    return result;
  }

  public int handleFault(ConfigurationContext configurationContext) throws ProtocolException {
    //final String SIGNATURE = "handleFault(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ClientConfigurationContext clientCtx = (ClientConfigurationContext) configurationContext;

    if (!AddressingConfiguration.isEnabled(clientCtx)) {
      return Protocol.CONTINUE;
    }

    int result = Protocol.CONTINUE;
    ConsumerProtocol wsaProtocol = provider.getVersionedProtocol(clientCtx);

    if (wsaProtocol != null) {
      result = wsaProtocol.handleFault(configurationContext);
    }

    //TRACE.exiting(SIGNATURE);
    return result;
  }

  private static final String IS_HIBERNATED = "is-hibernated";
  
  private static boolean isHibernated(ClientConfigurationContext cctx) {
    ConfigurationContext dReqCtx = getDynamicRequestContext(cctx);
    if (dReqCtx != null) {
      return Boolean.valueOf((String) dReqCtx.getProperty(IS_HIBERNATED));
    }
    return false;
  }
  
  // Hibernation impl
  /*** Hibernation i-face impl ***/
  public void beforeHibernation(ConfigurationContext ctx) throws ProtocolException {
    //final String SIGNATURE = "beforeHibernation(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ClientConfigurationContext clientCtx = (ClientConfigurationContext) ctx;

    ConfigurationContext dReqCtx = getDynamicRequestContext(clientCtx);
    ConfigurationContext pReqCtx = getPersistableRequestContext(clientCtx);

    Iterator itr = dReqCtx.properties();
    String pName;
    Object pValue;

    while (itr.hasNext()) {
      pName = (String) itr.next();
      pValue = dReqCtx.getProperty(pName);
      pReqCtx.setProperty(pName, pValue.toString());
    }

    //clear this since if not cleared, the context values would be used for the next sync call. This issue caused failure of JP 711SP1 test cycle.
	  dReqCtx.clear();
	  dReqCtx.setProperty(IS_HIBERNATED, "true");
    //TRACE.exiting(SIGNATURE);
  }

  public void finishMessageDeserialization(ConfigurationContext ctx) throws ProtocolException {
    //final String SIGNATURE = "finishMessageDeserialization(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
  }

  public void finishHibernation(ConfigurationContext ctx) throws ProtocolException {
    //final String SIGNATURE = "finishHibernation(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
  }

  public void afterHibernation(ConfigurationContext ctx) throws ProtocolException {
    //final String SIGNATURE = "afterHibernation(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ClientConfigurationContext clientCtx = (ClientConfigurationContext) ctx;

    ConfigurationContext pReqCtx = getPersistableRequestContext(clientCtx);
    ConfigurationContext dReqCtx = getDynamicRequestContext(clientCtx);

    try {
      Iterator itr = pReqCtx.properties();
      String pName;
      Object pValue;

      while (itr.hasNext()) {
        pName = (String) itr.next();
        pValue = pReqCtx.getProperty(pName);

        if (AddressingConstants.WSA_REPLY_TO.equals(pName) || AddressingConstants.WSA_FAULT_TO.equals(pName)) {
          pValue = EndpointReference.valueOf((String) pValue);
        }

        dReqCtx.setProperty(pName, pValue);
      }
    } catch (Exception ex) {
      throw new ProtocolException(ex);
    }

    //TRACE.exiting(SIGNATURE);
  }

  // API (2004/08)
  /**
   * @return true if the request /ReplyTo/Address value are equal to anonymous.
   * @param clientCtx Client configuration context
   */
  public boolean isAsyncCommunication(ClientConfigurationContext clientCtx) {
    //final String SIGNATURE = "isAsyncCommunication(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    EndpointReference replyTo = getRequestReplyTo(clientCtx);
    boolean result = (replyTo != null && !replyTo.getAddress().equals(getAnonymousURI(clientCtx)) && !replyTo.getAddress().startsWith(AddressingConstants.PREFIX_RM_ANON_URI));

    //TRACE.exiting(SIGNATURE);
    return result;
  }

  /**
   * @param ctx Configuration context.
   * @return the value of the request 'To' soap header or null if the header is not present.
   */
  public static String getRequestTo(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getRequestTo(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = null;
    if (isHibernated(ctx)) {
      reqCtx = getPersistableRequestContext(ctx);
    } else {
      reqCtx = getDynamicRequestContext(ctx);
    }

    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_TO);
  }

  /**
   * Sets the value of request 'To' soap header.
   * @param to the 'To' request value.
   * @param ctx Configuration context.
   */
  public static void setRequestTo(String to, ClientConfigurationContext ctx) {
    //final String SIGNATURE = "setRequestTo(String, ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext respCtx = getDynamicRequestContext(ctx);
    respCtx.setProperty(AddressingConstants.WSA_TO, to);

    //TRACE.exiting(SIGNATURE);
  }

  /**
   * @return the value of the request 'ReplyTo' soap header or a default value.
   * @param ctx Client configuration context
   */
  public static EndpointReference getRequestReplyTo(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getRequestReplyTo(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);
    ConfigurationContext reqCtx = null;
    if (isHibernated(ctx)) {
      reqCtx = getPersistableRequestContext(ctx);
    } else {
      reqCtx = getDynamicRequestContext(ctx);
    }

    //TRACE.exiting(SIGNATURE);
    return (EndpointReference) reqCtx.getProperty(AddressingConstants.WSA_REPLY_TO);
  }

  /**
   * Sets the value of request 'ReplyTo' soap header.
   * @param replyTo the 'ReplyTo' request value.
   * @param ctx Configuration context.
   */
  public static void setRequestReplyTo(EndpointReference replyTo, ClientConfigurationContext ctx) {
    //final String SIGNATURE = "setRequestReplyTo(EndpointReference, ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext respCtx = getDynamicRequestContext(ctx);
    respCtx.setProperty(AddressingConstants.WSA_REPLY_TO, replyTo);

    //TRACE.exiting(SIGNATURE);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of the request 'FaultTo' soap header or a default value.
   */
  public static EndpointReference getRequestFaultTo(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getRequestFaultTo(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = null;
    if (isHibernated(ctx)) {
      reqCtx = getPersistableRequestContext(ctx);
    } else {
      reqCtx = getDynamicRequestContext(ctx);
    }

    //TRACE.exiting(SIGNATURE);
    return (EndpointReference) reqCtx.getProperty(AddressingConstants.WSA_FAULT_TO);
  }

  /**
   * Sets the value of request 'FaultTo' soap header.
   * @param faultTo the 'FaultTo' request value.
   * @param ctx Configuration context.
   */
  public static void setRequestFaultTo(EndpointReference faultTo, ClientConfigurationContext ctx) {
    //final String SIGNATURE = "setRequestFaultTo(EndpointReference, ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext respCtx = getDynamicRequestContext(ctx);
    respCtx.setProperty(AddressingConstants.WSA_FAULT_TO, faultTo);

    //TRACE.exiting(SIGNATURE);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of the request 'Action' soap header or null if the header is not present.
   */
  public static String getRequestAction(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getRequestAction(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);
    ConfigurationContext reqCtx = null;
    if (isHibernated(ctx)) {
      reqCtx = getPersistableRequestContext(ctx);
    } else {
      reqCtx = getDynamicRequestContext(ctx);
    }

    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_ACTION);
  }

  /**
   * Sets the value of request 'Action' soap header.
   * @param action the 'Action' request value.
   * @param ctx Configuration context.
   */
  public static void setRequestAction(String action, ClientConfigurationContext ctx) {
    //final String SIGNATURE = "setRequestAction(String, ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext respCtx = getDynamicRequestContext(ctx);
    respCtx.setProperty(AddressingConstants.WSA_ACTION, action);

    //TRACE.exiting(SIGNATURE);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of the request 'MessageID' soap header or null if the header is not present.
   */
  public static String getRequestMessageID(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getRequestMessageID(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = null;
    if (isHibernated(ctx)) {
      reqCtx = getPersistableRequestContext(ctx);
    } else {
      reqCtx = getDynamicRequestContext(ctx);
    }

    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_MESSAGE_ID);
  }

  /**
   * Sets the value of response 'MessageID' soap header.
   * @param msgID the 'MessageID' response value.
   * @param ctx Configuration context.
   */
  public static void setRequestMessageID(String msgID, ClientConfigurationContext ctx) {
    //final String SIGNATURE = "setRequestMessageID(String, ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext respCtx = getDynamicRequestContext(ctx);
    respCtx.setProperty(AddressingConstants.WSA_MESSAGE_ID, msgID);

    //TRACE.exiting(SIGNATURE);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of the request 'RelatesTo' soap header or null if the header is not present
   */
  public static String getRequestRelatesTo(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getRequestRelatesTo(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = null;
    if (isHibernated(ctx)) {
      reqCtx = getPersistableRequestContext(ctx);
    } else {
      reqCtx = getDynamicRequestContext(ctx);
    }

    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_RELATES_TO);
  }

  /**
   * @param relatesTo the value of response 'RelatesTo' soap header.
   * @param ctx Configuration context.
   */
  public static void setRequestRelatesTo(String relatesTo, ClientConfigurationContext ctx) {
    //final String SIGNATURE = "setRequestRelatesTo(String, ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext respCtx = getDynamicRequestContext(ctx);
    respCtx.setProperty(AddressingConstants.WSA_RELATES_TO, relatesTo);

    //TRACE.exiting(SIGNATURE);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of the request 'RelatesTo'@RelationshipType soap header attribute or null if the attribute is not present
   */
  public static String getRequestRelationshipType(ClientConfigurationContext ctx) {
    //final String SIGNATURE = "getRequestRelationshipType(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);
    ConfigurationContext reqCtx = null;
    if (isHibernated(ctx)) {
      reqCtx = getPersistableRequestContext(ctx);
    } else {
      reqCtx = getDynamicRequestContext(ctx);
    }

    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_RELATIONSHIP_TYPE);
  }

  /**
   * @param relationshipType the value of response 'RelatesTo'@RelationshipType soap header attribute.
   * @param ctx Configuration context.
   */
  public static void setRequestRelationshipType(String relationshipType, ClientConfigurationContext ctx) {
    //final String SIGNATURE = "setRequestRelationshipType(String, ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext respCtx = getDynamicRequestContext(ctx);
    respCtx.setProperty(AddressingConstants.WSA_RELATIONSHIP_TYPE, relationshipType);

    //TRACE.exiting(SIGNATURE);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of the response 'To' soap header.
   */
  public static String getResponseTo(ClientConfigurationContext ctx) {
  	//final String SIGNATURE = "getResponseTo(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = getDynamicResponseContext(ctx);

    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_TO);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of the response 'Action' soap header.
   */
  public static String getResponseAction(ClientConfigurationContext ctx) {
  	//final String SIGNATURE = "getResponseAction(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = getDynamicResponseContext(ctx);
    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_ACTION);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of the response 'MessageID' soap header.
   */
  public static String getResponseMessageID(ClientConfigurationContext ctx) {
  	//final String SIGNATURE = "getResponseMessageID(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = getDynamicResponseContext(ctx);

    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_MESSAGE_ID);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of response 'RelatesTo' soap header.
   */
  public static String getResponseRelatesTo(ClientConfigurationContext ctx) {
  	//final String SIGNATURE = "getResponseRelatesTo(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = getDynamicResponseContext(ctx);

    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_RELATES_TO);
  }

  /**
   * @param ctx Configuration context.
   * @return the value of response 'RelatesTo'@RelationshipType soap header attribute.
   */
  public static String getResponseRelationshipType(ClientConfigurationContext ctx) {
  	//final String SIGNATURE = "getResponseRelationshipType(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = getDynamicResponseContext(ctx);

    //TRACE.exiting(SIGNATURE);
    return (String) reqCtx.getProperty(AddressingConstants.WSA_RELATIONSHIP_TYPE);
  }

  /**
   * @param ctx Client configuration context
   * @return the value of the response 'ReplyTo' soap header.
   */
  public static EndpointReference getResponseReplyTo(ClientConfigurationContext ctx) {
  	//final String SIGNATURE = "getResponseReplyTo(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    ConfigurationContext reqCtx = getDynamicResponseContext(ctx);

    //TRACE.exiting(SIGNATURE);
    return (EndpointReference) reqCtx.getProperty(AddressingConstants.WSA_REPLY_TO);
  }

  /**
   * @param ctx Client configuration context.
   * @return the value of the response 'FaultTo' soap header.
   */
  public static EndpointReference getResponseFaultTo(ClientConfigurationContext ctx) {
  	//final String SIGNATURE = "getResponseFaultTo(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);
    ConfigurationContext reqCtx = getDynamicResponseContext(ctx);

    //TRACE.exiting(SIGNATURE);
    return (EndpointReference) reqCtx.getProperty(AddressingConstants.WSA_FAULT_TO);
  }

  /**
   * Returns anonymous URI depending on the WS-Addressing version
   * @param ctx Client Configuration Context
   * @return Anonymous URI
   */
  public static String getAnonymousURI(ClientConfigurationContext ctx) {
  	//final String SIGNATURE = "getAnonymousURI(ClientConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    String result = AddressingConstants.URI_200408_ANONYMOUS_ENDPOINT;
    String versionNS = AddressingConfiguration.getConfigValue(ctx, AddressingConstants.CONF_WSA_PROTOCOL);

    if (AddressingConstants.NS_WSA_200508.equals(versionNS)) {
      result = AddressingConstants.URI_200508_ANONYMOUS_ENDPOINT;
    }

    //TRACE.exiting(SIGNATURE);
    return result;
  }

  public int afterDeserialization(ConfigurationContext arg0)
      throws ProtocolException, MessageException {
    // TODO Auto-generated method stub
    return CONTINUE;
  }

  public void beforeSerialization(ConfigurationContext arg0)
      throws ProtocolException {
    ConfigurationContext ctx = getDynamicRequestContext((ClientConfigurationContext) arg0); 
    ctx.setProperty(IS_HIBERNATED, "false");
  }
  
  
}