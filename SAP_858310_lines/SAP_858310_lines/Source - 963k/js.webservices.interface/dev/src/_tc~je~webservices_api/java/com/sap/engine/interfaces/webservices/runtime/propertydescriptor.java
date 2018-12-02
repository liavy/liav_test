/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import java.io.Serializable;
import java.util.Hashtable;

/**
 *   Represents an extendable property structure.
 * Instead of the standard value field it has simpleContent
 * field which is a container long string(used instead of
 * value) and it could have multiple private sub-properties.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public interface PropertyDescriptor extends Serializable {

  /**
   *
   * @return the property name
   */
  public String getPropertyDescriptorName();

  /**
   *
   * @return true if a value field is set, false otherwise
   */
  public boolean hasValueAttrib();

  /**
   *
   * @return the value field content
   */
  public String getValue();

  /**
   *
   * @return true if the simpleContent field is set, false otherwise.
   */
  public boolean hasSimpleContent();

  /**
   *
   * @return the simpleContent field content.
   */
  public String getSimpleContent();

  /**
   *
   * @param propertyName the sub-property name to search for
   * @return the descriptor for the sub-property if there is
   *         a sub-property with such name, null otherwise.
   */
  public PropertyDescriptor getInternalDescriptor(String propertyName);

  /**
   *
   * @return a Hashtable containing all sub-property descriptors
   *         mapped under their names.
   */
  public Hashtable getInternalDescriptors();

}
