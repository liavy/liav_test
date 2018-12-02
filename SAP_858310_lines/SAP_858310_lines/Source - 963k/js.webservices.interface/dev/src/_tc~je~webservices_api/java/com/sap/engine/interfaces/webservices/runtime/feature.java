/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 *   This interface is used to access feature information which is loaded from
 * the runtime at deploy time
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface Feature extends java.io.Serializable {

  /**
   *
   * @return the unique name ot the feature
   */
  public abstract String getFeatureName();

  /**
   *
   * @return the ID of the protocol to which this feature is bound
   */
  public abstract String getProtocolID();

  /**
   *
   * @return the feature configuration
   */
  public abstract Config getConfiguration();

}
