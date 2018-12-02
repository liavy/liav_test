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

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-21
 */
public class WSDLMarshalException extends WSDLException {
  
  public static final String DEFINITIONS_VERSION_NOT_SUPPORTED  =  "webservices_2018";
  public static final String MORE_THAN_ONE_LOCATIONS_FOR_NS  =  "webservices_2021";
  public static final String INVALID_IN_SERIALIZATION_HTTPOPERATION  =  "webservices_2028";
  public static final String INVALID_OUT_SERIALIZATION_HTTPOPERATION  =  "webservices_2029";
  public static final String INTERFACE_OPERATION_IS_MISSING_REQUIRED_PROPERTY  =  "webservices_2030";
  
  public WSDLMarshalException(Throwable cause) {
    super(cause);
  }

  public WSDLMarshalException(String pattern) {
    super(pattern);
  }

  public WSDLMarshalException(String pattern, Throwable cause) {
    super(pattern, cause);
  }

  public WSDLMarshalException(String pattern, Object[] params) {
    super(pattern, params);
  }

  public WSDLMarshalException(String pattern, Object[] params, Throwable cause) {
    super(pattern, params, cause);
  }

}
