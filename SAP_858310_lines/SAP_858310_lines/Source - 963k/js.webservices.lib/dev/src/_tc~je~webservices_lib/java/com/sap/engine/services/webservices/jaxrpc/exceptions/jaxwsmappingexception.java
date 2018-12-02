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
package com.sap.engine.services.webservices.jaxrpc.exceptions;

import com.sap.exception.BaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * @author Dimitar Velichkov  dimitar.velichkov@sap.com
 * 
 *
 */
public class JaxWsMappingException extends BaseException {

	public static final Location LOC = Location.getLocation(JaxWsMappingException.class);
	
	public static final String BAD_JAXWS_VERSION = "webservices_4200";
  public static final String ILLEGAL_JAXWS_DECL = "webservices_4201";
	public static final String EXTERNAL_BINDINGF_NOTFORMED = "webservices_4202";
	public static final String NODE_NOT_FOUND = "webservices_4203";
	public static final String CHILD_ATTR_MISSING_NOT_QNAME = "webservices_4204";
	public static final String BAD_BINDING_DOC_PATH = "webservices_4206";
	public static final String WSDL_SERIALIZATION_ERROR = "webservices_4208";
	public static final String BINDING_ELEM_MISSING_CHILD = "webservices_4209";
	public static final String EXT_DOC_PARSING_ERROR = "webservices_4210";
	public static final String PACKAGE_EXT_MISSING_NAME = "webservices_4211";
	public static final String MISSING_CHILD_ELEM = "webservices_4212";
	public static final String MISSING_NAME_ATTR = "webservices_4213";
	public static final String WSDL_DOM_ERROR = "webservices_4214";
	public static final String EXTWSDL_BINDING_ERROR = "webservices_4215";
	public static final String UNEXPECTED_EXT_ELEMENT = "webservices_4216";
	public static final String UNSUPPORTED_EXTENSION = "webservices_4217";
	
	public JaxWsMappingException() {
    super(LOC);
  }

  public JaxWsMappingException(String patternKey) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey));
  }

  public JaxWsMappingException(String patternKey, Throwable cause) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey),cause);
  }

  public JaxWsMappingException(String patternKey, Object[] args) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  args));
  }

  public JaxWsMappingException(String patternKey, String arg1) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1}));
  }

  public JaxWsMappingException(String patternKey, Throwable cause ,String arg1) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1}),cause);
  }

  public JaxWsMappingException(String patternKey ,String arg1, String arg2) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1,arg2}));
  }	
	
  public JaxWsMappingException(String patternKey , Throwable cause ,String arg1, String arg2) {
    super(LOC, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1,arg2}),cause);
  } 
  
}
