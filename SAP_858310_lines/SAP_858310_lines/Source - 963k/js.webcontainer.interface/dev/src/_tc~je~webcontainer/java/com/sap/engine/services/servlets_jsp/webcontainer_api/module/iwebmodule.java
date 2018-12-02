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

/**
 * Representation of a web module deployed in the web container.
 *
 * @author Maria Jurova
 * @version 7.10
 */
public interface IWebModule {

  /**
   * Returns the name of the application that contains the web module.
   *
   * @return name of the application that contains the web module.
   */
  public String getApplicationName();

  /**
   * Returns the web module name.
   * The web module name is unique in the whole J2EE Engine.
   *
   * @return the name of the web module.
   */
  public String getModuleName();

  /**
   * Returns the vendor name of the application that contains the web module.
   *
   * @return the vendor name of the application that contains the web module.
   */
  public String getVendor();

  /**
   * Returns the object representing a particular descriptor for a web module
   * if the descriptor file exists, otherwise returns null.
   *
   * @param descriptorName the descriptor name - web.xml, portlet.xml, ...
   * @return an object representing a particular descriptor for a web module
   *         if the descriptor file exists, otherwise returns null.
   */
  public IModuleDescriptor getDescriptor(String descriptorName);

}//end of interface
