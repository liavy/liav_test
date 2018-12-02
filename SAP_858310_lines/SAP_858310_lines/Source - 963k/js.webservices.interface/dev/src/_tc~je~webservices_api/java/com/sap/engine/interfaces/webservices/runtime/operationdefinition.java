/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;


import java.io.Serializable;

/**
 *   This interface represents a webservice operation in
 * the server environment.
 *
 * @author       Dimiter Angelov
 * @version      1.0
 */
public interface OperationDefinition extends Serializable {

  /**
   *
   * @return the wsdl operation name
   */
  public String getOperationName();

  /**
   *
   * @return the actual java implementation method name.
   */
  public abstract String getJavaOperationName();

  /**
   *
   * @return the operation input parameters.
   *         Array of length 0 indicates no parameters
   */
  public abstract ParameterNode[] getInputParameters();

  /**
   *
   * @return the operation output parameters.
   *         Array of length 0 indicates void return type.
   */
  public abstract ParameterNode[] getOutputParameters();

  /**
   *
   * @return the operation general configuration
   */
  public abstract Config getGeneralConfiguration();

  /**
   *
   * @return the operation output configuration
   */
  public abstract Config getOutputConfiguration();

  /**
   *
   * @return the operation input configuration
   */
  public abstract Config getInputConfiguration();

  /**
   *
   * @return the faults which this operation declares
   *         to throw. Array of length 0 indicates that
   *         no faults are thrown.
   */
  public abstract Fault[] getFaults();

  /**
   *
   * @return the operation specific features.
   *         Array of length 0 indicates that
   *         there are no features.
   */
  public abstract Feature[] getFeatures();

  /**
   * Returns the operation feature with the specified
   * name. If not found, null is returned.
   *
   * @param featureName the name of the feature
   * @return
   *
   */
  public abstract Feature getFeature(String featureName);

  /**
   *
   * @return the operation descrition if any,
   *         null otherwise
   */
  public abstract String getDescription();

}

