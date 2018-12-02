/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

/**
 *   This class is a container for the Java-to-Schema
 * mapping sets. Currenly only two sets of mappings
 * are supported
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public interface JavaToQNameMappingRegistry {

  /**
   * Returns the mappings for enoded mode
   */
  public JavaToQNameMappings getEncodedMappings();

  /**
   * Returns the mappings for literal mode
   */
  public JavaToQNameMappings getLiteralMappings();

}