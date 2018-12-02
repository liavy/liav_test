/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server;

import java.io.IOException;

import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.JEEImplicitTlds;
import com.sap.engine.services.servlets_jsp.server.lib.ParserPropertiesFile;

public class JspContext {

  private JEEImplicitTlds jeeImplicitTlds;
  
  public JspContext(WebContainerProperties webContainerProperties) throws IOException {
    String externalCompilerName = webContainerProperties.getExternalCompiler();
    String encoding = webContainerProperties.javaEncoding();
    ParserPropertiesFile.getInstance(null, null, externalCompilerName, encoding);
    jeeImplicitTlds = JEEImplicitTlds.getInstance();
  }
  
  public JEEImplicitTlds getJeeImplicitTlds() {
    return jeeImplicitTlds;
  }
}
