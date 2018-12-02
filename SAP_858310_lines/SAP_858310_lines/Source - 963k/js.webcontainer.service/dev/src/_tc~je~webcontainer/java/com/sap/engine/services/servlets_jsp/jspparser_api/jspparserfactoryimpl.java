/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api;



/**
 * This class provides access to "environment variable" specific for the webcontainer service - service classloader, 
 * webcontainer properties 
 * @author Todor Mollov, Bojidar Kadrev
 * DEV_tc_je_webcontainer
 * 2005-4-22
 * 
 */
public class JspParserFactoryImpl extends JspParserFactory {
  
  /**
   * returns the WebContainerParameters which are initialized duriung service startup 
   * @return
   */
  public WebContainerParameters getContainerProperties() {
    return containerParameters;
  }

}
