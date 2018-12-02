/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.services.webservices.wsdl.WSDLPortType;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface InterfaceHandler {

  /**
   * Called when the generation process is started.
   */
  public boolean startGenreration(GeneratorEnvironment environment);
  /**
   * Called when the generation process is finished.
   */
  public boolean endGeneration(GeneratorEnvironment environment);
  /**
   * Called to process portType.
   */
  public boolean processPortType(WSDLPortType portType, GeneratorEnvironment environment);

}
