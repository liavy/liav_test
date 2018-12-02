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

import java.io.InputStream;

/**
 * Interface for creating WebInfo objects which are used to represent
 * application modules of type web service. Each WebInfo object holds
 * information about the WAR file name and
 * context root of the web service module.
 *
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public interface WebInfoCreator {

  /**
   * Creates WebInfo objects with the specified working directory and web service descriptor.
   *
   * @param workingDir                 the working directory.
   * @param wsDeploymentDescriptorIs   the input stream of the web service descriptor.
   *
   * @return                           an array of WebInfo objects.
   *
   * @throws Exception                 thrown if some problem occurs during the process.
   */
  public WebInfo[] createSingleWebInfo(String workingDir, InputStream wsDeploymentDescriptorIs) throws Exception;

  /**
   * Creates WebInfo objects with the specified working directory and web service descriptor.
   *
   * @param workingDir                  the working directory.
   * @param wsDeploymentDescriptorPath  the path to the xml file storing the web service descriptor.
   *
   * @return                            an array of WebInfo objects.
   *
   * @throws Exception                  thrown if some problem occurs during the process.
   */
  public WebInfo[] createSingleWebInfo(String workingDir, String wsDeploymentDescriptorPath) throws Exception;

  /**
   * Creates WebInfo objects with the specified working directory and web service descriptors.
   *
   * @param workingDir                  the working directory.
   * @param wsDeploymentDescriptorPaths a String array containing the paths to the
   *                                    xml files storing the web service descriptors.
   *
   * @return                            an array of WebInfo objects.
   *
   * @throws Exception                  thrown if some problem occurs during the process.
   */
  public WebInfo[] createWebInfo(String workingDir, String[] wsDeploymentDescriptorPaths) throws Exception;

}