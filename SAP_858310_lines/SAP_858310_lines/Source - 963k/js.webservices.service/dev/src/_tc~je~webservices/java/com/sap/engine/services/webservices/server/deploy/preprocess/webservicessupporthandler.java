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
 
package com.sap.engine.services.webservices.server.deploy.preprocess;

import java.io.File;
import java.util.jar.JarFile;

import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfo;  
import com.sap.lib.javalang.tool.ReadResult;

/**
 * Title: WebServicesWebSupportHandler
 * Description: WebServicesWebSupportHandler
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public interface WebServicesSupportHandler {

  public String[] getWebServicesEntry(); 
  public WebInfo[] generateWebSupport(String workingDir, File[] archiveFiles, ReadResult parsedAnnotations) throws Exception;
  public WebInfo[] generateWebSupport(String workingDir, JarFile archiveJarFile, ReadResult parsedAnnotations) throws Exception;  

}
