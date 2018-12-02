/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
 
package com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630;   

/**
 * WebInfo class represents a single application module of type web service.
 * It holds information about the path to the WAR file and its context root
 * in the application.
 *
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WebInfo {

  private String contextRoot;
  private String warModulePath;

  /**
   * Empty constructor of the class.
   */
  public WebInfo() {

  }

  /**
   * Constructs WebInfo object with the specified context root
   * and path to the WAR file.
   *
   * @param contextRoot    the context root of the module.
   * @param warModulePath  the path of the WAR file.
   */
  public WebInfo(String contextRoot, String warModulePath) {
    this.contextRoot = contextRoot;
    this.warModulePath = warModulePath;
  }

  /**
   * Gets the context root of the module.
   *
   * @return  the context root.
   */
  public String getContextRoot() {
    return contextRoot;
  }

  /**
   * Sets the context root of the module.
   *
   * @param contextRoot  the context root.
   */
  public void setContextRoot(String contextRoot) {
    this.contextRoot = contextRoot;
  }

  /**
   * Gets the path to the WAR file of the web service module.
   *
   * @return  the path to the WAR file.
   */
  public String getWarModulePath() {
    return warModulePath;
  }

  /**
   * Sets the path to the WAR file of the web service module.
   *
   * @param warModulePath  the path to the WAR file.
   */
  public void setWarModulePath(String warModulePath) {
    this.warModulePath = warModulePath;
  }

  /**
   * Returns a String representation of this object, including
   * the context root and the path to the WAR file of the module.
   *
   * @return  String representing this object.
   */
  public String toString() {
    return  "context root: " + contextRoot + "\n" + "war path: " + warModulePath;
  }

}