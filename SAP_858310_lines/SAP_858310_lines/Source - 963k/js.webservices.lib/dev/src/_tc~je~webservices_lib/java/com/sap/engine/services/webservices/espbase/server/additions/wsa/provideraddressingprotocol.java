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
package com.sap.engine.services.webservices.espbase.server.additions.wsa;

import java.util.Set;

import org.w3c.dom.Element;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-12-6
 */
public interface ProviderAddressingProtocol extends ProviderProtocol {
  /**
   * Constant denoting the protocol name.
   */
  public final String PROTOCOL_NAME  =  "AddressingProtocol";
  /**
   * @return the value of the request 'To' soap header or null if the header is not present.
   */
  public String getRequestTo(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of the request 'ReplyTo' soap header or a default value.
   */
  public EndpointReference getRequestReplyTo(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of the request 'FaultTo' soap header or a default value.
   */
  public EndpointReference getRequestFaultTo(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of the request 'Action' soap header or null if the header is not present.
   */
  public String getRequestAction(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of the request 'MessageID' soap header or null if the header is not present.
   */
  public String getRequestMessageID(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of the request 'RelatesTo' soap header or null if the header is not present
   */
  public String getRequestRelatesTo(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of the request 'RelatesTo'@RelationshipType soap header attribute or null if the attribute is not present
   */
  public String getRequestRelationshipType(ConfigurationContext ctx) throws WSAddressingException;
  
  /**
   * @return the value of the response 'To' soap header.
   */
  public String getResponseTo(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * Sets the value of response 'To' soap header.
   * 
   * @param to the 'To' response value.
   */
  public void setResponseTo(String to, ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of the response 'Action' soap header.
   */
  public String getResponseAction(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * Sets the value of response 'Action' soap header.
   * 
   * @param action the 'Action' response value.
   */
  public void setResponseAction(String action, ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of the response 'MessageID' soap header.
   */
  public String getResponseMessageID(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * Sets the value of response 'MessageID' soap header.
   * 
   * @param msgID the 'MessageID' response value.
   */
  public void setResponseMessageID(String msgID, ConfigurationContext ctx) throws WSAddressingException;
  /**
   * This method returns a reference to unmodifiable Set object which contains response reference parameters. 
   * @return Set containing org.w3c.dom.Element objects, which represent response reference parameters.
   */
  public Set getResponseReferenceParameters(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * Sets <code>refParam</code> as response reference parameter
   * @param refParam a response reference parameter
   * @param ctx
   */
  public void setResponseReferenceParameter(Element refParam, ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @param relationshipType the value of response 'RelatesTo'@RelationshipType soap header attribute. 
   */
  public void setResponseRelationshipType(String relationshipType, ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of response 'RelatesTo'@RelationshipType soap header attribute.
   */
  public String getResponseRelationshipType(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @param relatesTo the value of response 'RelatesTo' soap header.
   */
  public void setResponseRelatesTo(String relatesTo, ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of response 'RelatesTo' soap header. 
   */
  public String getResponseRelatesTo(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * @return the value of the response 'ReplyTo' soap header set with <code>setResponseReplyTo</code> or the default value.
   */
  public EndpointReference getResponseReplyTo(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * Sets the value of response 'ReplyTo' soap header.
   *
   * @param replyTo the 'ReplyTo' response value.
   */
  public void setResponseReplyTo(EndpointReference replyTo, ConfigurationContext ctx) throws WSAddressingException;  
  /**
   * @return the value of the response 'FaultTo' soap header set with <code>setResponseFaultTo</code> or the default value.
   */
  public EndpointReference getResponseFaultTo(ConfigurationContext ctx) throws WSAddressingException;
  /**
   * Sets the value of response 'FaultTo' soap header.
   *
   * @param faultTo the 'FaultTo' response value.
   */
  public void setResponseFaultTo(EndpointReference faultTo, ConfigurationContext ctx) throws WSAddressingException;
}
