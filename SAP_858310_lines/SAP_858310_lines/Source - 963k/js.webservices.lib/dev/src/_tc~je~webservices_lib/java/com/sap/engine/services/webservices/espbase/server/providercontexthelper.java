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

import java.util.Map;

import javax.xml.namespace.QName;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.esp.RuntimeEnvironment;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;

/**
 * <p> This interface provides utility methods that could be used by the <code>ProviderProtocol</code>.
 * Implementation of this interface is passes to the provider protolol's methods, thus only a
 * typecast of <code>ConfigurationContext</code> parameter to this inteface is needed.</p> 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public interface ProviderContextHelper extends ConfigurationContext {
  /**
   * Constant denoting that the message semantic is fault message
   */  
  public static final int FAULT_MSG  =  1;
  /**
   * Constant denoting that the message semantic is normal response message
   */  
  public static final int NORMAL_RESPONSE_MSG  =  2;
  /**
   * Constant denoting that the message semantic is fault message and for http transport 401 response code should be
   * returned. 
   */
  public static final int BASIC_AUTHENTICATION_EXPECTED = 3;
  
  public ConfigurationContext getDynamicContext();
  
  public StaticConfigurationContext getStaticContext();
  
  public ConfigurationContext getPersistableContext();
  /**
   * Returns the transport object.
   * 
   */  
  public Transport getTransport();
  /**
   * Returns the message object, or <code>null</code>.
   */
  public Message getMessage() throws RuntimeProcessException;
  /**
   * Sets message object.
   * 
   * @param msg the new messae object
   * @return the previous message or <code>null</code> if none.
   */
  public Message setMessage(Message msg) throws RuntimeProcessException;
  /**
   * Returns reference to a protocol instance. Only references to protocols associated
   * with this call will be returned.
   *  
   * @param protocolID the <code>ID</code> of the protocol.
   * @return protocol instance or <code>null</code> if there is no such protocol.
   */
  public ProviderProtocol getProtocol(String protocolID);
  /**
   * Returns the operation mapping object of the requested operation.
   * If the reqested operation could not be resolved an exception is thrown.
   * 
   * @throws RuntimeProcessException
   */
  public OperationMapping getOperation() throws RuntimeProcessException;   
  /**
   * Creates error message based on the Throwble parameter. The message is returned
   * and it should be set using <code>setMessage()</code> in case to be handled by the runtime.
   * 
   * @param thr
   * @throws RuntimeProcessException
   */
  public Message createErrorMessage(Throwable thr) throws RuntimeProcessException;
  /**
   * Returns the <code>MessageException</code> thrown by <code>Protocol.handleRequest()</code> method.
   * Usually this method is used in <code>Protocol.handleFault()</code>.
   */
  public MessageException getMessageException();  
  /**
   * Returns the ImplLink object that should be used by the <code>ImplementationContainer</code> objects.
   * It first checks for dynamically set <code>ImplementationLink<code> object, and if such is available it is returned.
   * Otherwise the <code>ImplementationLink</code> object from <code>InterfaceMapping</code> object that
   * are available from <code>StaticConfigurationContext</code> instance is returned.  
   */
  public ImplementationLink getImplementationLink();  
  /**
   * Returns the session id associated with the current call, or <code>null</code> in case no session is associated.
   */
  public String getSessionID();  
  /**
   * Returns the RuntimeEnvironment object.
   * 
   */
  public RuntimeEnvironment getEnvironment();
  /**
   * This method is used when a response message
   * is about to be sent. According to this flag
   * the transportBinding knows how to transport 
   * the message via http.
   * For example - for soap over http, fault messages are replied with HTTP code 500.
   *  
   * @return the message semantic type. If not set, -1 is returned. 
   */
  public int getMessageSemantic();
  /**
   * This method is used for setting the semantic
   * of response message.
   * Protocols that have created message
   * and have set it in the context using <code>setMessage()</code> mehtod, 
   * should also set the message semantic. The set value will be accesssed
   * via <code>getMessageSemantic</code> method by transportBinding
   * at the time of sending.
   * 
   * @param n semantic type
   * @return the previous value of the semantic
   */
  public int setMessageSemantic(int n);
  /**
   * Instructs the Runtime not to send reply to the client. By default reply is sent, unless hibernation has taken place.
   * 
   * @param b flag which determines whether reply will be send or not. 
   */
  void sendNoReply(boolean b);
  /**
   * @return true if the Runtime will send reply to the client, false otherwise
   */
  boolean isSendNoReply();
  /**
   * Sends the message which is mapped inside this context as synchronous response
   * message. For http transport the message is send as http response
   * with response code determined by the value returned from <code>getMessageSemantic()</code> method.
   * The method is supposed to be used by the WS-RM protocol in case of acknowledgements messages for synchronous sequence 
   * related to one-way business method.
   */
  public void sendMessageAsSynchronousResponse() throws RuntimeProcessException;
  /**
   * Sends back an asynchronous response. For the http case '202 Accepted' is returned with empty http body.
   * The method is supposed to be used by the WS-RM protocol in case of acknowledgements for asynchronous sequence 
   * related to one-way business method.
   */
  public void sendAsynchronousResponse() throws RuntimeProcessException;
  /**
   * Marks that soap header(s) with qualified name <code>soapHeader</code> is 
   * understood and processed by some of the pluggable protocols. This method needs to be
   * used by all protocol implementations which deal with soap headers - Security, ReliableMessaging, WS-Addressing, etc.
   * If in the soap message a soap header with attribute mustUnderstand='1' is present, and this header is not marked as understood
   * and processed via this method, the Runtime at deserialization time will generate 'MustUnderstand' soap fault targeting
   * that soap header. 
   * @param soapHeader soap header qualified name
   */
  public void markSOAPHeaderAsUnderstood(QName soapHeader);
  
  // i044259
  public commonj.sdo.helper.XMLHelper getSdoHelper();
  public void setSdoHelper(commonj.sdo.helper.XMLHelper sxh);
  public void setSdoHelper(commonj.sdo.helper.XMLHelper sxh, Map options);
}
