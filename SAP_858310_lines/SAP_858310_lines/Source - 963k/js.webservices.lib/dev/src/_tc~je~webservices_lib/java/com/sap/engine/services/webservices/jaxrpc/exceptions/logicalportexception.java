/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.exceptions;

import com.sap.exception.BaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class LogicalPortException extends BaseException {

  public static final String PARSER_ERROR = "webservices_3400";
  public static final String DESERIALIZATION_ERROR = "webservices_3401";
  public static final String SAVE_ERROR = "webservices_3402";
  public static final String MISSING_BINDING_IMPL = "webservices_3403";
  public static final String UREACHABLE_BINDING_IMPL = "webservices_3404";
  public static final String UPDATEIMPOSSIBLE = "webservices_3405";
  public static final String BUGGY_FEATURE = "webservices_3406";
  public static final String STREAM_CLOSE = "webservices_3407";
  public static final String IOERROR = "webservices_3408";
  
  private static final Location LOC = Location.getLocation(LogicalPortException.class);
  
  public LogicalPortException() {
    super(LOC);
  }

  public LogicalPortException(String patternKey) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey));
  }

  public LogicalPortException(String patternKey, Throwable cause) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey),cause);
  }

  public LogicalPortException(String patternKey, Object[] args) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  args));
  }

  public LogicalPortException(String patternKey, String arg1) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1}));
  }

  public LogicalPortException(String patternKey, String arg1, String arg2) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1,arg2}));
  }

  public LogicalPortException(String patternKey, Throwable cause ,String arg1) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1}),cause);
  }

  public LogicalPortException(String patternKey, Throwable cause ,String arg1, String arg2) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1,arg2}),cause);
  }


}
