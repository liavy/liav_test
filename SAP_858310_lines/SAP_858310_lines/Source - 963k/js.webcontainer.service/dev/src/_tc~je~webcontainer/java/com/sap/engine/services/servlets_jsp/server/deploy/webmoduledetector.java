/**
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http:////www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf.. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy;

import java.io.File;

import com.sap.engine.services.deploy.container.rtgen.GenerationException;
import com.sap.engine.services.deploy.container.rtgen.ModuleDetector;
import com.sap.engine.services.deploy.ear.Module;
import com.sap.engine.services.deploy.ear.modules.Web;
import com.sap.lib.javalang.tool.ReadResult;


/**
 * Checks wheter the specified module is a module that can be processed by the 
 * WebContainer. Also calculetes the context root.
 *@author Luchesar Cekov
 */
public class WebModuleDetector implements ModuleDetector {
  public Module detectModule(File aTempDir, String aModuleRelativeFileUri) throws GenerationException {
    if (!aModuleRelativeFileUri.endsWith(".war")) return null;
    
    StringBuffer contextRoot = new StringBuffer(aModuleRelativeFileUri);
    contextRoot.delete(contextRoot.length() - ".war".length(), contextRoot.length());
    return new Web(aTempDir, aModuleRelativeFileUri, contextRoot.toString());
  }
}