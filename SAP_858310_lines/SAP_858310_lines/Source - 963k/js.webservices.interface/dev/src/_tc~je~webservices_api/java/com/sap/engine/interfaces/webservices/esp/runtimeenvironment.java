/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.interfaces.webservices.esp;

import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;

/**
 *  <p>This interface provides methos for dealing with the provider runtime.
 * Instance of this inteface could be lookuped from the root JNDI context, 
 * using <code>JNDI_NAME</code>.</p>
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-15
 */
public interface RuntimeEnvironment extends HibernationEnvironment {  
  /**
   * This is the JNDI name under which instance implementing this inteface could be lookuped from
   * 'wsContext' JNDI context.
   */
  public static final String JNDI_NAME  =  "RuntimeEnvironment";   
  /**
   * Registers protocol instance in the runtime.
   */
  public void registerProviderProtocol(ProviderProtocol protocol) throws RuntimeProcessException;
  /**
   * Removes, from internal registry, protocol with id <code>protocolID</code>.
   * @return removed protocol instane, or null if there was no registered protocol with id <code>protocolID</code>.
   */
  public ProviderProtocol unregisterProviderProtocol(String protocolID) throws RuntimeProcessException;
  /**
   * Sends <code>msg</code> message as request to <code>endpointURL</code> endpoint address.
   * The method is supposed to be used by WS-RM protocol in the cases when an http request. 
   * 
   * @param endpointURL endpoint address to which the <code>msg</code> should be requested.
   * @param msg a ready-to-send message object.
   * 
   * @throws RuntimeProcessException in case anything goes wrong.
   * @deprecated  
   */
  public void sendMessageOneWay(String endpointURL, Message msg) throws RuntimeProcessException;
  /**
   * Sends <code>msg</code> message as request to <code>endpointURL</code> endpoint address.
   * The method is supposed to be used by WS-RM protocol in the cases when an http request. 
   * 
   * @param endpointURL endpoint address to which the <code>msg</code> should be requested.
   * @param msg a ready-to-send message object.
   * @param action the value that should be used for 'SOAPAction' http header for SOAP1.1 and for 'action' content-type 
   *        parameter for SOAP1.2. If there is no value, <code>null</code> must be passed.
   * 
   * @throws RuntimeProcessException in case anything goes wrong.  
   */
  public void sendMessageOneWay(String endpointURL, Message msg, String action) throws RuntimeProcessException;
}
