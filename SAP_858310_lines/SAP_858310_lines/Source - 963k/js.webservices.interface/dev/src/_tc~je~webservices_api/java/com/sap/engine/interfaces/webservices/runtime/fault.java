/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import java.io.Serializable;

/**
 *   This interface represents the method fault
 * described in the VI
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface Fault extends Serializable {

  /**
   *
   * @return the java name of the exception class
   */
  public abstract String getJavaClassName();

  /**
   *
   * @return the wsdl name of the fault
   */
  public abstract String getFaultName();

  /**
   *
   * @return the fault configuration
   */
  public abstract Config getFaultConfiguration();

}

