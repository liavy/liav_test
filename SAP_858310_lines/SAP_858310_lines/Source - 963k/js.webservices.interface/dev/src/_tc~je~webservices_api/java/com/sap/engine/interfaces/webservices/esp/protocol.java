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

import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;

/**
 * <p> Base interface for pluggable protocols on provider and consumer side.
 * The <code>handleRequest()<code>, <code>handleResponse()<code> and <code>handleFault()</code> methods throw
 * <code>ProtocolException</code> denoting an exception in processing. When use on the Provider side, the protocol 
 * can influence the data contained in the SoapFault message that is returned by the runtime, when <code>ProtocolException<code>   
 * is thrown. This can be achieved by creating new exception class that inherits <code>ProtocolException</code> and implements
 * {@link com.sap.engine.interfaces.webservices.runtime.soaphttp.ISoapFaultException} interface. When such an exception is thrown by the protocol's methods, the runtime uses the
 * {@link com.sap.engine.interfaces.webservices.runtime.soaphttp.ISoapFaultException} methods to fulfil SoapFault data fields.</p>
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public interface Protocol {
  /**
   * Continues further processing.
   */
  public static int CONTINUE  =  1;
  
  /**
   * Stops the current protocol processing, and continues backwards.
   */  
  public static int BACK  =  2;
  
  /**
   * Stops the whole runtime.
   */  
  public static int STOP  =  3;
  
  /**
   * Skips the next protocols in the chain and continues with next runtime processing steps.
   */  
  public static int SKIP_NEXT = 4;
  
  
  /**
   * Returns the protocol name(ID).
   */
  public String getProtocolName();
  
  /**
   * Invoked by the runtime to handle the request.
   * If <code>BACK</code> is returned handleResponse() of this and the previous protocols
   * are invoked back way, starting from this instance.
   * If <code>STOP</code> is returned the runtime processing stops - usually used with hibernation.
   * In case of <code>CONTINUE</code> the processing continues with next <code>handleRequest()</code> method in the chain.
   * 
   * @throws ProtocolException  In this case the runtime takes care to send back error message.
   * @throws MessageException   In this case <code>handleFault()</code> of this an the previous protocols are 
   *                            invoked back way starting from this instance. The MessageException is stored in the
   *                            <code>ConfigurationContext</code>.
   */
  public int handleRequest(ConfigurationContext context) throws ProtocolException, MessageException;
  
  /**
   * Invoked by the runtime to handle the response.
   * In case of <code>STOP</code> the runtime processing stops.
   * In case of <code>SKIP_NEXT</code> the runtime skips the next protocols in the chain and continues sending response message.
   * In case <code>CONTINUE</code> the processing continues with next <code>handleResponse()</code> method in the chain.
   * 
   * @throws ProtocolException  In this case the runtime takes care to send back error message.
   */
  public int handleResponse(ConfigurationContext context) throws ProtocolException;

  /**
   * Invoked by the runtime when MessageException in handleRequest() is thrown.
   * In case of <code>STOP</code> the runtime processing stops.
   * In case of <code>SKIP_NEXT</code> the runtime skips the next protocols in the chain and continues sending response message.
   * In case of <code>CONTINUE</code> the processing continues with next <code>handleFault()</code> method in the chain.
   * 
   * @throws ProtocolException  In this case the runtime takes care to send back error message.
   */
  public int handleFault(ConfigurationContext context) throws ProtocolException;
     
}
