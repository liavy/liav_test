/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.webcontainer_api.module;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * The object representing a particular descriptor for a web module.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public interface IModuleDescriptor {

  /**
   * Returns a stream on the named descriptor (a stream to portlet.xml for example).
   *
   * @return a stream on the named descriptor (a stream to portlet.xml for example).
   * @throws FileNotFoundException thrown if there is no such descriptor on the file system.
   */
  public InputStream getInputStream() throws FileNotFoundException;

  /**
   * Returns the name of the descriptor (portlet.xml for example).
   *
   * @return the name of the descriptor (portlet.xml for example).
   */
  public String getName();

}//end of interface
