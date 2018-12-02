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
package com.sap.engine.services.servlets_jsp.server.deploy.impl.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IModuleDescriptor;

/**
 * The object representing a particular descriptor for a module.
 *
 * @author Violeta Georgieva
 * @version 7.0
 */
public class ModuleDescriptor implements IModuleDescriptor {
  private String descriptorAbsolutePath = "";

  /**
   * Constructor
   *
   * @param descriptorAbsolutePath
   */
  public ModuleDescriptor(String descriptorAbsolutePath) {
    this.descriptorAbsolutePath = descriptorAbsolutePath;
  } //end of constructor

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IModuleDescriptor#getInputStream()
   */
  public InputStream getInputStream() throws FileNotFoundException {
    return new FileInputStream(descriptorAbsolutePath);
  } //end of getInputStream()

  /**
   * Returns a file for the named descriptor (a file that represents the portlet.xml for example).
   *
   * @return a file for the named descriptor (a file that represents the portlet.xml for example).
   */
  public File getFile() {
    return new File(descriptorAbsolutePath);
  }//end of getFile()

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IModuleDescriptor#getName()
   */
  public String getName() {
    int index = descriptorAbsolutePath.lastIndexOf(File.separator);
    if (index != -1) {
      return descriptorAbsolutePath.substring(index + 1);
    } else {
      return descriptorAbsolutePath;
    }
  } //end of getName()

} //end of class
