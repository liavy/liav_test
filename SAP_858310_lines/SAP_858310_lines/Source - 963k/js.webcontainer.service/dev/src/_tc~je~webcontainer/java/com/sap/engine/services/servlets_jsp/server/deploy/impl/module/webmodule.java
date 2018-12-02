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
package com.sap.engine.services.servlets_jsp.server.deploy.impl.module;

import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IModuleDescriptor;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule;

import java.io.File;
import java.util.Vector;

/**
 * @author Maria Jurova
 * @author Violeta Georgieva
 * @version 7.0
 */
public class WebModule implements IWebModule {
  private String wholeApplicationName = "";
  private String moduleName = "";
  private File rootDirectory = null;
  /** A list of all WCEs that participated in the application deploy */
  private Vector<String> wceInDeploy = new Vector<String>();
  private String applicationName = "";
  private String vendor = "";

  /**
   * Constructor
   *
   * @param wholeApplicationName the whole application name which identifies the application.
   * @param moduleName           the module name which identifies the web application.
   * @param rootDirectory        this is "root" directory where .war file is extracted.
   */
  public WebModule(String wholeApplicationName, String moduleName, File rootDirectory) {
    this.wholeApplicationName = wholeApplicationName;
    this.moduleName = moduleName;
    this.rootDirectory = rootDirectory;
    this.applicationName = wholeApplicationName.substring(wholeApplicationName.indexOf("/") + 1);
    this.vendor = wholeApplicationName.substring(0, wholeApplicationName.indexOf("/"));
  } //end of constructor

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule#getApplicationName()
   */
  public String getApplicationName() {
    return applicationName;
  } //end of getApplicationName()

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule#getModuleName()
   */
  public String getModuleName() {
    return moduleName;
  } //end of getModuleName()

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule#getVendor()
   */
  public String getVendor() {
    return vendor;
  } //end of getVendor()

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule#getDescriptor(String)
   */
  public IModuleDescriptor getDescriptor(String descriptorName) {
    if (new File(rootDirectory.getAbsolutePath() + File.separator + "WEB-INF" + File.separator + descriptorName).exists()) {
      return new ModuleDescriptor(rootDirectory.getAbsolutePath() + File.separator + "WEB-INF" + File.separator + descriptorName);
    } else {
      return null;
    }
  } //end of getDescriptor(String descriptorName)

  /**
   * Return the whole application name which identifies the application.
   *
   * @return the whole application name which identifies the application.
   */
  public String getWholeApplicationName() {
    return wholeApplicationName;
  }//end of getWholeApplicationName()

  /**
   * Returns "root" directory where .war file is extracted.
   *
   * @return a "root" directory where .war file is extracted.
   */
  public File getRootDirectory() {
    return rootDirectory;
  }//end of getRootDirectory()

  /**
   * Returns a list of names of all the WCE Providers that participated in the deploy of this web module.
   * @return
   */
  public Vector<String> getWceInDeploy() {
    return wceInDeploy;
  }//end of getWceInDeploy()

  /**
   * Sets which WCE's have participated in the deploy of this web module.
   * @param wceInDeploy
   */
  public void setWceInDeploy(Vector<String> wceInDeploy) {
    this.wceInDeploy = wceInDeploy;
  }//end of setWceInDeploy(Vector wceInDeploy)

} //end of class
