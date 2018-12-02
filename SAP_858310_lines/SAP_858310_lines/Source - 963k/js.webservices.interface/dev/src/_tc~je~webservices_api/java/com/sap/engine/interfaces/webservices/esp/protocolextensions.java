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
 * <p> This interface should be implemented by protocols that need additional notifications.</p> 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public interface ProtocolExtensions extends Hibernation {
  
  /**
   * Invoked by the runtime after the deserialization of the message as been completed.This is valid
   * both for the Consumer and Provider Runtimes and happens when processing an Inbound Message.
   * If {@link Protocol#BACK} is returned handleResponse() of all protocols are invoked.
   * If {@link Protocol#STOP} is returned the runtime processing stops (will not invoke the endpoint).
   * In case of {@link Protocol#CONTINUE} the processing continues with next <code>afterDeserialization()</code> method in the chain.
   * 
   * @throws ProtocolException  In this case the runtime takes care to send back error message.
   * @throws MessageException   In this case <code>handleFault()</code> of this an the previous protocols are 
   *                            invoked back way starting from this instance. The MessageException is stored in the
   *                            <code>ConfigurationContext</code>.
   */  
  public int afterDeserialization(ConfigurationContext ctx) throws ProtocolException, MessageException;
  
  /**
   * Invoked before the runtime serializes the Java Objects for an Outbound Message. This event allows a protocol to 
   * register XMLTokenWriter instances, that would dynamically modify the output. Currently this will be used mostly by
   * the WS-Security Protocol, to allow for streaming encryption and signature.
   * 
   * @param ctx
   * @throws ProtocolException
   */
  public void beforeSerialization(ConfigurationContext ctx) throws ProtocolException;
  
}
