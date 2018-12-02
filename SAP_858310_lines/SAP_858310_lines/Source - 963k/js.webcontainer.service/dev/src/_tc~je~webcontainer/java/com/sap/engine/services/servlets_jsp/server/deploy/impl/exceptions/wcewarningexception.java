/*
* Copyright (c) 2005-2006 by SAP AG, Walldorf.,
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
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionWarningException;
import com.sap.localization.LocalizableTextFormatter;

/**
 * @author Violeta Georgieva
 * @version 7.1
 */
public class WCEWarningException extends WebContainerExtensionWarningException {
  public static final String REQUIRED_WCE_IS_NOT_REGISTERED = "servlet_jsp_0540";

  public WCEWarningException(String s, Object[] args, Throwable t) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args), t);
  } //end of constructor

  public WCEWarningException(String s, Throwable t) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s), t);
  } //end of constructor

  public WCEWarningException(String s, Object[] args) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args));
  } //end of constructor

  public WCEWarningException(String s) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s));
  } //end of constructor

} //end of class
