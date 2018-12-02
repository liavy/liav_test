/*
 * Copyright (c) 2005 by SAP Labs Bulgaria.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.espbase.client.api.impl;

import com.sap.engine.services.webservices.espbase.client.api.AddressingInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.wsa.ClientAddressingProtocol;
import com.sap.engine.services.webservices.espbase.client.wsa.common.AddressingConstants;
//import com.sap.engine.services.webservices.espbase.client.wsa.common.Trace;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.EndpointReference;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.WSAddressingException;

/**
 * Addressing API interface implementation
 * @author Vladimir Videlov (vladimir.videlov@sap.com)
 * @version 1.0
 */
public class AddressingInterfaceImpl implements AddressingInterface {
  public static final String VERSION_ID = "Id: //engine/j2ee.core.libs/dev/src/tc~je~webservices_lib/_tc~je~webservices_lib/java/com/sap/engine/services/webservices/espbase/client/api/impl/AddressingInterfaceImpl.java_1 ";
  //private static final Trace TRACE = new Trace(VERSION_ID);

  ClientConfigurationContext clientCtx;
  private boolean enabledWSA = false;

  public AddressingInterfaceImpl(ClientConfigurationContext context) {
    this.clientCtx = context;
  }

  public String getRequestTo() throws WSAddressingException {
    //final String SIGNATURE = "getRequestTo()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getRequestTo(clientCtx);
  }

  public void setRequestTo(String to) throws WSAddressingException {
    //final String SIGNATURE = "setRequestTo(String)";
    //TRACE.entering(SIGNATURE);
    setWSAEnabled();
    ClientAddressingProtocol.setRequestTo(to, clientCtx);
    
    //TRACE.exiting(SIGNATURE);
  }

  public EndpointReference getRequestReplyTo() throws WSAddressingException {
    //final String SIGNATURE = "getRequestReplyTo()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getRequestReplyTo(clientCtx);
  }

  public void setRequestReplyTo(EndpointReference replyTo) throws WSAddressingException {
    //final String SIGNATURE = "setRequestReplyTo(EndpointReference)";
    //TRACE.entering(SIGNATURE);
    setWSAEnabled();
    ClientAddressingProtocol.setRequestReplyTo(replyTo, clientCtx);
    
    //TRACE.exiting(SIGNATURE);
  }

  public EndpointReference getRequestFaultTo() throws WSAddressingException {
    //final String SIGNATURE = "getRequestFaultTo()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getRequestFaultTo(clientCtx);
  }

  public void setRequestFaultTo(EndpointReference faultTo) throws WSAddressingException {
    //final String SIGNATURE = "setRequestFaultTo(EndpointReference)";
    //TRACE.entering(SIGNATURE);
    setWSAEnabled();
    ClientAddressingProtocol.setRequestFaultTo(faultTo, clientCtx);
    
    //TRACE.exiting(SIGNATURE);
  }

  public String getRequestAction() throws WSAddressingException {
    //final String SIGNATURE = "getRequestAction()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getRequestAction(clientCtx);
  }

  public void setRequestAction(String action) throws WSAddressingException {
    //final String SIGNATURE = "setRequestAction(String)";
    //TRACE.entering(SIGNATURE);
    setWSAEnabled();
    ClientAddressingProtocol.setRequestAction(action, clientCtx);    

    //TRACE.exiting(SIGNATURE);
  }

  public String getRequestMessageID() throws WSAddressingException {
    //final String SIGNATURE = "getRequestMessageID)";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getRequestMessageID(clientCtx);
  }

  public void setRequestMessageID(String msgID) throws WSAddressingException {
    //final String SIGNATURE = "setRequestMessageID(String)";
    //TRACE.entering(SIGNATURE);
    setWSAEnabled();
    ClientAddressingProtocol.setRequestMessageID(msgID, clientCtx);
    
    //TRACE.exiting(SIGNATURE);
  }

  public String getRequestRelatesTo() throws WSAddressingException {
    //final String SIGNATURE = "getRequestRelatesTo()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getRequestRelatesTo(clientCtx);
  }

  public void setRequestRelatesTo(String relatesTo) throws WSAddressingException {
    //final String SIGNATURE = "setRequestRelatesTo(String)";
    //TRACE.entering(SIGNATURE);
    setWSAEnabled();
    ClientAddressingProtocol.setRequestRelatesTo(relatesTo, clientCtx);
    
    //TRACE.exiting(SIGNATURE);
  }

  public String getRequestRelationshipType() throws WSAddressingException {
    //final String SIGNATURE = "getRequestRelationshipType()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getRequestRelationshipType(clientCtx);
  }

  public void setRequestRelationshipType(String relationshipType) throws WSAddressingException {
    //final String SIGNATURE = "setRequestRelationshipType(String)";
    //TRACE.entering(SIGNATURE);
    setWSAEnabled();
    ClientAddressingProtocol.setRequestRelationshipType(relationshipType, clientCtx);
    
    //TRACE.exiting(SIGNATURE);
  }

  public String getResponseTo() throws WSAddressingException {
    //final String SIGNATURE = "getResponseTo()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getResponseTo(clientCtx);
  }

  public String getResponseAction() throws WSAddressingException {
    //final String SIGNATURE = "getResponseAction()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getResponseAction(clientCtx);
  }

  public String getResponseMessageID() throws WSAddressingException {
    //final String SIGNATURE = "getResponseMessageID()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getResponseMessageID(clientCtx);
  }

  public String getResponseRelatesTo() throws WSAddressingException {
    //final String SIGNATURE = "getResponseRelatesTo()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getResponseRelatesTo(clientCtx);
  }

  public String getResponseRelationshipType() throws WSAddressingException {
    //final String SIGNATURE = "getResponseRelationshipType()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getResponseRelationshipType(clientCtx);
  }

  public EndpointReference getResponseReplyTo() throws WSAddressingException {
    //final String SIGNATURE = "getResponseReplyTo()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getResponseReplyTo(clientCtx);
  }

  public EndpointReference getResponseFaultTo() throws WSAddressingException {
    //final String SIGNATURE = "getResponseFaultTo()";
    //TRACE.entering(SIGNATURE);
    //TRACE.exiting(SIGNATURE);
    return ClientAddressingProtocol.getResponseFaultTo(clientCtx);
  }
  
  private void setWSAEnabled() {
    if (!enabledWSA) {
      enabledWSA = true;
      clientCtx.getPersistableContext().setProperty("{" + AddressingConstants.NS_WSA_FEATURE + "}" + AddressingConstants.CONF_WSA_ENABLED, AddressingConstants.CONST_TRUE);
    }
  }
  
}