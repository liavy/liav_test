/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 *   Holds the Java-to-QName schema mappings.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public interface JavaToQNameMappings {

  /**
   * Returns the corresponding QName for the parameter.
   */
  public QName getMappedQName(String javaClassName);

  /**
   * Returns the corresponding java class for the parameter.
   */
  public String getMappedJavaClass(QName qname);

  /**
   * Returns copy of the internal mappings.
   * Keys are java-class names, values are complexTypes QNames
   */
  public HashMap getJavaToQNameMappings();

  /**
   * Returns copy of the internal mappings.
   * Keys are complexTypes QNames, values are java-class names
   */
  public HashMap getQNameToJavaMappings();

}
