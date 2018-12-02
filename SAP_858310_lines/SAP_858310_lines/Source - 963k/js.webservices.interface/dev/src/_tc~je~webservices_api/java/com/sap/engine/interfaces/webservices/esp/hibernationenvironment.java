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
package com.sap.engine.interfaces.webservices.esp;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface contains methods for dealing with hibernation.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-5-9
 */
public interface HibernationEnvironment {
  /**
   * Hibernates the runtime. As part of this hibernation <code>Hibernation.beforeHibernation()</code> methods of 
   * the configured protocols are invoked, as well as <code>Hibernation.finishHibernation()</code> after <code>ctx</code> 
   * has been serialized into <code>output</code>.
   * 
   * @param ctx the context associated with the call.
   * @param output stream into which serialized content of <code>ctx</code> is to be written.
   * @throws HibernationEnvironmentException
   */
  public void hibernate(ConfigurationContext ctx, OutputStream output) throws HibernationEnvironmentException;  
  /**
   * Builds configuration context from <code>input</code>. The static data is loaded in the context, the <code>Hibernation.afterHibernation()</code>
   * methods of the configured protocols are invoked. The returned context parameter is ready to be used runtime.
   * 
   * @param input  
   * @return ready-to-use configuration context instance.
   * @throws HibernationEnvironmentException
   */
  public ConfigurationContext restore(InputStream input) throws HibernationEnvironmentException;  
  /**
   * Forces the runtime to continue processing from the point where previously it has been hibernated.
   * Before invoking this method, the {@link #wakeUp} method should be invoked in order to restore the information in the context.
   * @param ctx
   * @throws HibernationEnvironmentException
   */
  public void restart(ConfigurationContext ctx) throws HibernationEnvironmentException;  
}
