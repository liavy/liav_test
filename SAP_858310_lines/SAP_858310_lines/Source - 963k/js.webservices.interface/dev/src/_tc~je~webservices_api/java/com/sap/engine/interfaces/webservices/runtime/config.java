/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import java.io.Serializable;

/**
 *   This interface is use to obtain configuration information
 * in run time which has been loaded from the descriptors in deploy time.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface Config extends Serializable {

  /**
   *   Returns the specfied descriptor mapped under the 'key' param.
   * If there is no mapped descriptor null is returned.
   *
   * @param key  the key of the property desciptor.
   */
  public PropertyDescriptor getProperty(String key);

  /**
   *   Returns all the descriptors contained in this object.
   * If it has no descriptors the array lenght is 0.
   */
  public PropertyDescriptor[] getProperties();

}

