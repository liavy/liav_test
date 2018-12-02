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

import com.sap.engine.interfaces.webservices.runtime.ApplicationWebServiceContext;

/**
 * 
 * <p> This interface extends the functionality of <code>com.sap.engine.interfaces.webservices.runtime.ApplicationWebServiceContext</code> with methods which
 * can change in the next releases. Currently it ensures access to the <code>ConfigurationContext</code> associated
 * with the request.  An instance implementing this interface is bound in the
 * naming under "wsContext" subcontext using {@link #APPLICATION_WSCONTEXT} jndi name.
 * </p>
 * 
 * Copyright (c) 2004, SAP-AG
 * @author  Dimitar Angelov
 * @version 1.0, 2004-9-21
 */
public interface ApplicationWebServiceContextExt extends ApplicationWebServiceContext {
  /**
   *  Returns reference to the <code>ConfigurationContext</code> object associated with current
   * request and used by the Provider Runtime.
   */
  public ConfigurationContext getConfigurationContext();
}
