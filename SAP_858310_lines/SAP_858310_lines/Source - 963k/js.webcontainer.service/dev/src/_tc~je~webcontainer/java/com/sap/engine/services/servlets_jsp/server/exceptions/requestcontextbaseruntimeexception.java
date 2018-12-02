/*
 * Copyright (c) 2006 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.server.exceptions;

import com.sap.exception.BaseExceptionInfo;
import com.sap.exception.BaseRuntimeException;
import com.sap.localization.LocalizableTextFormatter;

/**
 * @author diyan-y
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RequestContextBaseRuntimeException extends BaseRuntimeException {
  public static final String PROBLEMS_WHEN_SETTING_ID = "servlet_jsp_0601";
  public static final String PROBLEMS_WHEN_GETTING_ID =  "servlet_jsp_0602";  
  public static final String PROBLEMS_WHEN_PUSHING_ID = "servlet_jsp_0603";
  public static final String PROBLEMS_WHEN_POPPING_ID =  "servlet_jsp_0604";  
  
  private Throwable linkedException;

  public RequestContextBaseRuntimeException(String msg, Throwable rootCause) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), msg), rootCause);
    this.linkedException = linkedException;
  }
  
  public final Throwable getLinkedException() {
    return linkedException;
  }

}
