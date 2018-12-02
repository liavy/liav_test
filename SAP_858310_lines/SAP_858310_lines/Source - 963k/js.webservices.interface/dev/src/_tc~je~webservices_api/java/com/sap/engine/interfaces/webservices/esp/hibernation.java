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

import com.sap.engine.interfaces.webservices.runtime.ProtocolException;

/**
 * <p>The methods of this interface are invoked by the Runtime when it prepares to go into
 * hibernation state, and after restore its state from hibernatin.
 * Protocols that need to receive the <code>beforeHibernation<code> and <code>afterHibernation</code>
 * events must implement this interface. Normally if the protocls stores something into DynamicConfigurationContext,
 * it will be necessary to implement this interface.</p>
 *  
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-28
 */
public interface Hibernation {
  /**
   *  Invoked when the runtime is preparing to go hibernate. Usually in this method
   * the protocol fills the <code>PersistableConfigurationContext</code> with data
   * that should be persisted.
   *   
   * @param ctx
   * @throws ProtocolException
   */
  public void beforeHibernation(ConfigurationContext ctx) throws ProtocolException;
  /**
   * Invoked when the hiberantion process has finished deserializing the message instance.
   * Invoked before finishHibernation() method.
   */
  public void finishMessageDeserialization(ConfigurationContext ctx) throws ProtocolException;
  /**
   * Invoked when the hiberantion process has finished.
   * In this method, the protocols can access some data that has occured in the 
   * process of hibernation.
   * Example - when security protocol checks message intergrity streaming, 
   * the actual message is read and stored because of the hibernation.
   * In this case the security protocol can save some important data, which later to be used. 
   */
  public void finishHibernation(ConfigurationContext ctx) throws ProtocolException;
  /**
   *  Invoked when the runtime wakes after hibernation. The <code>PersistableConfigurationContext</code> object
   * contains the data stored before hibernation. In this method the protocol restores its evironment for further processing.  
   *   
   * @param ctx
   * @throws ProtocolException
   */
  public void afterHibernation(ConfigurationContext ctx) throws ProtocolException;
}
