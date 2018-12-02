/*
 * Copyright (c) 2004-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.impl;

import com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtension;
import com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext;

import java.util.Arrays;
import java.util.Vector;

/**
 * @author Violeta Georgieva
 * @version 7.0
 */
public class WebContainerExtensionWrapper {

  private IWebContainerExtension webContainerExtension = null;
  private IWebContainerExtensionContext webContainerExtensionContext = null;
  private String webContainerExtensionName = "";
  private Vector<String> descriptorNames = new Vector<String>();

  public WebContainerExtensionWrapper(IWebContainerExtension webContainerExtension,
                                      IWebContainerExtensionContext webContainerExtensionContext,
                                      String webContainerExtensionName, String[] descriptorNames) {
    this.webContainerExtension = webContainerExtension;
    this.webContainerExtensionContext = webContainerExtensionContext;
    this.webContainerExtensionName = webContainerExtensionName;
    this.descriptorNames.addAll(Arrays.asList(descriptorNames));
  } //end of constructor

  /**
   * @return
   */
  public Vector<String> getDescriptorNames() {
    return descriptorNames;
  } //end of getDescriptorNames()

  /**
   * @return
   */
  public String getWebContainerExtensionName() {
    return webContainerExtensionName;
  } //end of getWebContainerExtensionName()

  /**
   * @return
   */
  public IWebContainerExtension getWebContainerExtension() {
    return webContainerExtension;
  } //end of getWebContainerExtension()

  /**
   * @return
   */
  public IWebContainerExtensionContext getWebContainerExtensionContext() {
    return webContainerExtensionContext;
  } //end of getWebContainerExtensionContext()

} //end of class
