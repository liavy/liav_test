package com.sap.engine.services.webservices.server.lcr.api.cimclient;

import com.sap.engine.services.webservices.server.lcr.ObjectWrapper;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class LcrException extends ObjectWrapper {

  public static final String LCR_EXCEPTION_CLASS_NAME = "com.sap.lcr.api.cimclient.LcrException";

  public LcrException(Object obj) {
    super(obj);
  }

  public static int getErrorCode(String errorFieldName) throws Exception {
    initClass(LcrException.class.getClassLoader(), LCR_EXCEPTION_CLASS_NAME);
    Object obj = getStaticField(errorFieldName);
    return ((Integer)obj).intValue();
  }

  public int getStatusCode() throws Exception {
    Object obj = invokeMethod("getStatusCode", null, null);
    return ((Integer)obj).intValue();
  }

}
