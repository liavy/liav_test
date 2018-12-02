/*
* Copyright (c) 2005-2008 by SAP AG, Walldorf.,
* http://www.sap.com
* All rights reserved.
*
* This software is the confidential and proprietary information
* of SAP AG, Walldorf. You shall not disclose such Confidential
* Information and shall use it only in accordance with the terms
* of the license agreement you entered into with SAP.
*/
package com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions;

import com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.localization.LocalizableTextFormatter;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public class WCEDeploymentException extends WebContainerExtensionDeploymentException {
  public static final String STATUS_UNKNOWN = "servlet_jsp_0520";
  public static final String CANNOT_GET_FAILOVER = "servlet_jsp_0521";
  public static final String FAILED_EXPLICIT_STOP = "servlet_jsp_0522";
  public static final String REQUIRED_WCE_IS_NOT_REGISTERED = "servlet_jsp_0523";
  public static final String CANNOT_INIT_AND_REGISTER_SERVLET = "servlet_jsp_0524";
  public static final String WCENAME_ALREADY_IN_THE_LIST = "servlet_jsp_0525";
  public static final String WCENAME_OR_DESCRIPTORNAMES_ARE_NULL = "servlet_jsp_0526";
  public static final String WCE_CANNOT_BE_INITIALIZED = "servlet_jsp_0527";
  public static final String REFERENCES_CANNOT_BE_REGISTERED = "servlet_jsp_0528";
  public static final String CANNOT_RETRIEVE_INFO_FOR_APPLICATION = "servlet_jsp_0529";

  public WCEDeploymentException(String s, Object[] args, Throwable t) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args), t);
  }//end of constructor

  public WCEDeploymentException(String s, Throwable t) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s), t);
  }//end of constructor

  public WCEDeploymentException(String s, Object[] args) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args));
  }//end of constructor

  public WCEDeploymentException(String s) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s));
  }//end of constructor

}//end of class
