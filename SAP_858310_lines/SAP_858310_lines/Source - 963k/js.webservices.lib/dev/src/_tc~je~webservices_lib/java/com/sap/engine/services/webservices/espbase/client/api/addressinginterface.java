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
package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.espbase.server.additions.wsa.EndpointReference;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.WSAddressingException;

/**
 * Addressing API interface
 * @author Vladimir Videlov (vladimir.videlov@sap.com)
 * @version 1.0
 */
public interface AddressingInterface {
  /**
   * @return the value of the request 'To' soap header or null if the header is not present.
   */
  public String getRequestTo() throws WSAddressingException;

  /**
   * Sets the value of request 'To' soap header.
   * @param to the 'To' request value.
   */
  public void setRequestTo(String to) throws WSAddressingException;

  /**
   * @return the value of the request 'ReplyTo' soap header or a default value.
   */
  public EndpointReference getRequestReplyTo() throws WSAddressingException;

  /**
   * Sets the value of request 'ReplyTo' soap header.
   * @param replyTo the 'ReplyTo' request value.
   */
  public void setRequestReplyTo(EndpointReference replyTo) throws WSAddressingException;

  /**
   * @return the value of the request 'FaultTo' soap header or a default value.
   */
  public EndpointReference getRequestFaultTo() throws WSAddressingException;

  /**
   * Sets the value of request 'FaultTo' soap header.
   * @param faultTo the 'FaultTo' request value.
   */
  public void setRequestFaultTo(EndpointReference faultTo) throws WSAddressingException;

  /**
   * @return the value of the request 'Action' soap header or null if the header is not present.
   */
  public String getRequestAction() throws WSAddressingException;

  /**
   * Sets the value of request 'Action' soap header.
   * @param action the 'Action' request value.
   */
  public void setRequestAction(String action) throws WSAddressingException;

  /**
   * @return the value of the request 'MessageID' soap header or null if the header is not present.
   */
  public String getRequestMessageID() throws WSAddressingException;

  /**
   * Sets the value of response 'MessageID' soap header.
   *
   * @param msgID the 'MessageID' response value.
   */
  public void setRequestMessageID(String msgID) throws WSAddressingException;

  /**
   * @return the value of the request 'RelatesTo' soap header or null if the header is not present
   */
  public String getRequestRelatesTo() throws WSAddressingException;

  /**
   * @param relatesTo the value of response 'RelatesTo' soap header.
   */
  public void setRequestRelatesTo(String relatesTo) throws WSAddressingException;

  /**
   * @return the value of the request 'RelatesTo'@RelationshipType soap header attribute or null if the attribute is not present
   */
  public String getRequestRelationshipType() throws WSAddressingException;

  /**
   * @param relationshipType the value of response 'RelatesTo'@RelationshipType soap header attribute.
   */
  public void setRequestRelationshipType(String relationshipType) throws WSAddressingException;

  /**
   * @return the value of the response 'To' soap header.
   */
  public String getResponseTo() throws WSAddressingException;

  /**
   * @return the value of the response 'Action' soap header.
   */
  public String getResponseAction() throws WSAddressingException;

  /**
   * @return the value of the response 'MessageID' soap header.
   */
  public String getResponseMessageID() throws WSAddressingException;

  /**
   * @return the value of response 'RelatesTo' soap header.
   */
  public String getResponseRelatesTo() throws WSAddressingException;

  /**
   * @return the value of response 'RelatesTo'@RelationshipType soap header attribute.
   */
  public String getResponseRelationshipType() throws WSAddressingException;

  /**
   * @return the value of the response 'ReplyTo' soap header.
   */
  public EndpointReference getResponseReplyTo() throws WSAddressingException;

  /**
   * @return the value of the response 'FaultTo' soap header.
   */
  public EndpointReference getResponseFaultTo() throws WSAddressingException;
}