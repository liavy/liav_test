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
import com.sap.localization.LocalizableText;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ProxyGeneratorException extends BaseException {

  public static final String CANT_CREATEPATH = "webservices_3200";
  public static final String PATH_NOT_DIR = "webservices_3201";
  public static final String NO_WSDLPATH = "webservices_3202";
  public static final String WSDL_PARSING_PROBLEM = "webservices_3203";
  public static final String INVALID_CONFIG = "webservices_3204";
  public static final String GENERATION_PROBLEM = "webservices_3205";
  public static final String NOT_A_FILE = "webservices_3206";
  public static final String CREATE_FILE_ERROR = "webservices_3207";
  public static final String MISSING_MESSAGE = "webservices_3208";
  public static final String INOUTPARAMS = "webservices_3209";
  public static final String MISSING_SCHEMA_NODE = "webservices_3210";
  public static final String WRONG_FAULT = "webservices_3211";
  public static final String WRONG_FAULT_ELEMENT = "webservices_3212";
  public static final String SCHEMA_PROCESS_PROBLEM = "webservices_3213";
  public static final String MISSING_WSDL_INTERFACE = "webservices_3214";
  public static final String PARAMETER_POS_MISSING = "webservices_3215";
  public static final String PARAMETERS_SCRAMBLED = "webservices_3216";
  public static final String JAXWS_MAPPING_ERROR = "webservices_3217";
  public static final String JAXWS_EXTFILE_PROP_NOT_SET = "webservices_3218";
  public static final String UNSUPPORTED_WSDL = "webservices_3219";
  public static final String UNSUPPORTED_WSDL_OPERATION_OVERLOAD = "webservices_3222";
  public static final String DUBLICATE_PARAMETER_NAME = "webservices_3223";
  public static final String WSDL_IS_ENCODED_JAXWS = "webservices_3224";
  public static final String WSDL_IS_ENCODED_DYNAMIC = "webservices_3225";
  public static final String HTTP_BINDING_WRONG_IN_PARAMETER = "webservices_3226";
  public static final String HTTP_BINDING_WRONG_RETURN_PARAMETER = "webservices_3227";
  public static final String HTTP_BINDING_WRONG_PARAMETER = "webservices_3228";
  public static final String WRONG_INTERFACE_NAME = "webservices_3229";
  public static final String WRONG_METHOD_NAME = "webservices_3230";
  public static final String BAD_RPC_LIT_WSDL = "webservices_3231";
  public static final String BAD_DOC_LIT_WSDL = "webservices_3232";
  public static final String BAD_HEADERS = "webservices_3233";
  public static final String MISSING_OPERATION_BINDING = "webservices_3234";
  
  private static final Location LOC = Location.getLocation(ProxyGeneratorException.class);
  
  private String patternKey;
  
  public ProxyGeneratorException() {
    super(LOC);
  }
  
  public ProxyGeneratorException(String patternKey, LocalizableText localizableText) {
    super(LOC, localizableText);
    this.patternKey = patternKey;
  }
  
  public ProxyGeneratorException(String patternKey, LocalizableText localizableText, Throwable cause) {
    super(LOC, localizableText, cause);
    this.patternKey = patternKey;
  }
  
  public ProxyGeneratorException(String patternKey) {
    this(patternKey, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey));
  }

  public ProxyGeneratorException(String patternKey, Throwable cause) {
    this(patternKey, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey), cause);
  }

  public ProxyGeneratorException(String patternKey, Object[] args) {
    this(patternKey, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  args));
  }

  public ProxyGeneratorException(String patternKey, String arg1) {
    this(patternKey, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1}));
  }

  public ProxyGeneratorException(String patternKey, Throwable cause ,String arg1) {
    this(patternKey, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1}), cause);
  }

  public ProxyGeneratorException(String patternKey, Throwable cause ,String arg1, String arg2) {
    this(patternKey, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1,arg2}), cause);
  }

  public ProxyGeneratorException(String patternKey ,String arg1, String arg2) {
    this(patternKey, new LocalizableTextFormatter(WSResourceAccessor.getResourceAccessor(), patternKey,  new Object[] {arg1,arg2}));
  }
  
  public String getPatternKey() {
    return(patternKey);
  }

  public String getMessage() {
    return super.getLocalizedMessage();
  }

}
