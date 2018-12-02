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
package com.sap.engine.services.webservices.espbase.wsdl.exceptions;

import com.sap.exception.BaseException;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-21
 */
public class WSDLException extends BaseException {
  public static final String MISSING_ENTITY  =  "webservices_2006";
  public static final String DUPLICATE_ENTITIES  =  "webservices_2012";
  public static final String ENTITY_CANNOT_BE_APPEND  =  "webservices_2013";
  public static final String UNSUPPORTED_WSDL_STYLE  =  "webservices_2019";
  public static final String ENTITY_ALREADY_ATTACHED_TO_TREE  =  "webservices_2020";
  public static final String UNKNOWN_BINDING_TYPE  =  "webservices_2022";
  
  private static final Location LOC = Location.getLocation(WSDLException.class);
  
  public WSDLException(Throwable cause) {
    super(LOC, cause);
  }

  public WSDLException(String pattern) {
    super(LOC, WSDLResourceAccessor.getResourceAccessor(), pattern);
  }

  public WSDLException(String pattern, Throwable cause) {
    super(LOC, WSDLResourceAccessor.getResourceAccessor(), pattern, cause);
  }

  public WSDLException(String pattern, Object[] params) {
    super(LOC, WSDLResourceAccessor.getResourceAccessor(), pattern, params);
  }

  public WSDLException(String pattern, Object[] params, Throwable cause) {
    super(LOC, WSDLResourceAccessor.getResourceAccessor(), pattern, params, cause);
  }

}
