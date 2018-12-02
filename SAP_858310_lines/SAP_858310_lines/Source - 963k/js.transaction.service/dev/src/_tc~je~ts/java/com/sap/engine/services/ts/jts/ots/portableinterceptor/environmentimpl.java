/**
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ts.jts.ots.PortableInterceptor;

/**
 * A container (holder) for an exception that is used in <code>RequestImpl</code>
 * operations to make exceptions available to the client.  An
 * <code>Environment</code> object is created with the <code>ORB</code>
 * method <code>create_environment</code>.
 *
 * @author Georgy Stanev
 * @version 4.0
 */
public class EnvironmentImpl extends org.omg.CORBA.Environment {

  private Exception exc;

  public EnvironmentImpl() {
    exc = null;
  }

  public EnvironmentImpl(Exception e) {
    exc = e;
  }

  public Exception exception() {
    return exc;
  }

  public void exception(Exception e) {
    exc = e;
  }

  public void clear() {
    exc = null;
  }

}

