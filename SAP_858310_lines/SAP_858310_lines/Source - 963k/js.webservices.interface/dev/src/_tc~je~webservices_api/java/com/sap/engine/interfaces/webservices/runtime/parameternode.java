/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import java.io.Serializable;

/**
 *   Represents the VI parameters in the Runtime
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public interface ParameterNode extends Serializable {

  public static final int IN    = 0;
  public static final int INOUT = 1;
  public static final int OUT   = 2;

  /**
   * Return the VI parameter name
   */
  public abstract String getParameterName();

//  public abstract String getJavaParameterName();

  /**
   * Return the wsdl java-Class name for this parameter.
   */
  public abstract String getJavaClassName();

  /**
   * Return one of the parameter modes(IN, OUT, INOUT)
   */
  public abstract int getParameterMode();

  /**
   * If the parameter is show in the WSDL file
   */
  public abstract boolean isExposed();

  /**
   * If the parameter has devaul value.
   * If true - it has default value.
   * If false - it has not. isExposed() must return true in this case!
   * I.e isOptional() == false and isExposed() == false never appears.
   */
  public abstract boolean isOptional();

  /**
   * Returns the default value.
   * If it is not set IlleagalStateException is thrown.
   */
  public abstract String getDefaultValue();

  /**
   * Returns the original name ot the java class
   * Used for semantical mapping of simple types.
   */
  public abstract String getOriginalClassName();

  /**
   * Returns true if this parameter is
   * exposed and transported as header.
   */
  public abstract boolean isHeader();

  /**
   *
   * Return the namespace of the header wrapper element.
   * Null if it is not a header.
   */
  public abstract String getHeaderElementNamespace();

}
