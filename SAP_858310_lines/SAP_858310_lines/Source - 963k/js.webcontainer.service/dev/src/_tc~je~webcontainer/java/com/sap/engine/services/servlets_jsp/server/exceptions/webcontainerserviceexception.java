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

package com.sap.engine.services.servlets_jsp.server.exceptions;

import com.sap.engine.frame.ServiceException;
import com.sap.localization.LocalizableTextFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author Violeta Uzunova
 * @version 6.30
 */
public class WebContainerServiceException extends ServiceException {

  public WebContainerServiceException(String msg) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg));
    //super.setLogSettings(WebResourceAccessor.category, Severity.ERROR, WebResourceAccessor.location);
  }

  public WebContainerServiceException(String msg, Throwable linkedException) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg), linkedException);
    //super.setLogSettings(WebResourceAccessor.category, Severity.ERROR, WebResourceAccessor.location);
  }

  private Object writeReplace(){
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new ServiceException(stringWriter.toString());
  }

}
