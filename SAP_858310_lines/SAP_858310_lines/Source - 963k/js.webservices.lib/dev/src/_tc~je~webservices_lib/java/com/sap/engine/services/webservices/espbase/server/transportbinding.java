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
package com.sap.engine.services.webservices.espbase.server;

import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;

/**
 * This interface provides abstraction of the methods, 
 * which are called by runtime processing mechanism.
 * Via different implementations of this interface, it is possible
 * one and same runtime to support different web services communication
 * protocols - SOAP, MIME, HTTP,... 
 * Implementations of this class must be stateless.
 * Any state spefic data, associated with currect processing, can be stored
 * in the ProviderContextHelper instance, which is passed
 * as parameter to each method.
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-30
 */
public interface TransportBinding {
  /**
   * Denotes synchronous communication.
   */
  public final int SYNC_COMMUNICATION  =  1;
  /**
   * Denotes asynchronous communication.
   */
  public final int ASYNC_COMMUNICATION  =  2;
  
  /**
   * Implementation of this method is supposed to 
   * create and return message instance, which to be used
   * in the processing of the request.
   * The message is created from the request data (stream, headers, ...).
   * The returned instance is mapped in the context by 
   * the runtime, and can be accessed via ProviderContextHelper.getMessage() method. 
   */
  public Message createInputMessage(ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * Invoked by the runtime in order to instruct the 
   * concrete implementation to find out which operation
   * is supposed to be invoked. Based on the message context
   * the target business operation should be resolved.
   * The resolved OperationMapping object is mapped in the context by the Runtime,
   * and can be accessed via ProviderContextHelper.getOperation() method.
   * 
   * @return OperationMapping object if resolved. 
   * @throws RuntimeProcessException in case any processing problem occur, or no operation is resolved.
   */
  public OperationMapping resolveOperation(ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * This is next method to be invoked after <b>resolveOperation</b> method.
   * Creates and returns the parameters Objects.
   * The result array is used by the runtime.
   */
  public Object[] getParameters(Class[] methodClass, ClassLoader loader, ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * Implementation of this method is supposed to 
   * create and return message instance in state
   * ready to be used as output message.
   * The returned instance is mapped in the context by 
   * the runtime, and can be accessed via ProviderContextHelper.getMessage() method. 
   */
  public Message initOutputMessage(ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * Invoked when constructing response message, after a successful
   * endpoint method invocation.
   * Given the return object, and paramets of the operation to create the response
   * message.
   */
  public Message createResponseMessage(Object returnObject, Class returnObjectClass, Object[] resultParams, Class[] resultParamsClasses, ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * Invoked by the runtime if an mehtod exception has been occured in order to create a fault message.
   * The message is set in the context by the runtime and can be accessed via ProviderContextHelper.getMessage().
   */
  public Message createFaultMessage(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * Invoked by the runtime.
   * Sends the message in this transportbinding object.
   * 
   * @param comPattern the communication pattern which is valid when sending the response message
   */
  public void sendResponseMessage(ProviderContextHelper ctx, int comPattern) throws RuntimeProcessException;
  /**
   * Invoked by the runtime when an exception in the runtime occurs.
   * The exception is wrapped in message specific format (SOAP-fault)
   * and send back to the client.
   */
  public void sendServerError(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * Invoked by the runtime before cleaning the context object.
   * If transportBinding has set any objects that it wants
   * to reuse, in this method it can take them from the context.
   */ 
  public void onContextReuse(ProviderContextHelper ctx); 
  /**
   * @return the communication pattern that is associated with the current request.  
   */
  public int getCommunicationPattern(ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * In case of <code>ASYNC_COMMUNICATION</code>, this method is invoked by the runtime
   * to send the asynchronous response. For example '202 Accepted'.
   */
  public void sendAsynchronousResponse(ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * @return the value of the 'Action' property of the input message or null if such property
   *         is not available or not applicable.
   */
  public String getAction(ProviderContextHelper ctx) throws RuntimeProcessException;
  /**
   * Sends the <code>msg</code> message as a request to the <code>endpointURL</code> endpoint address, 
   * and no response message is expected to be received.
   * The method is supposed to be used by WS-RM protocol in the 'resend' case. The method
   * has no ProviderContextHelper parameter because the method is supposed to be used
   * in cases in which no request related context is available.
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
