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
package com.sap.engine.services.webservices.jaxws.j2w;

import com.sap.exception.BaseException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * @author Dimitar Velichkov dimitar.velichkov@sap.com
 */
public class JaxWsInsideOutException extends BaseException {

  public static final Location LOC                       = Location.getLocation(JaxWsInsideOutException.class);

  public static final String   MISSING_ANNOTATION        = "temp_001";
  public static final String   CLASS_REQUIRED            = "temp_002";
  public static final String   ABST_CLASS_MISSING        = "temp_003";
  public static final String   WS_CLASS_NULL             = "temp_004";
  public static final String   TARGET_NS_UNDEFINED       = "temp_005";
  public static final String   MEP_ONEWAY_HOLDER         = "temp_006";
  public static final String   MEP_ONEWAY_NONVOID        = "temp_007";
  public static final String   MEP_ONEWAY_THROWS_EX      = "temp_008";
  public static final String   ENCODED_NOT_SUPPORTED     = "temp_010";
  public static final String   RPC_REQ_WRAPPED           = "temp_011";
  public static final String   CLASS_NOT_FOUND_USING_CL  = "temp_018";
  public static final String   BEAN_GENERATION_ERROR     = "temp_020";
  public static final String   BEAN_COMPILATION_ERROR    = "temp_021";
  public static final String   BEAN_CLASSLOAD_ERROR      = "temp_022";
  public static final String   HOLDER_IN_PARAM           = "temp_023";
  public static final String   BARE_PARAM_ERROR          = "temp_024";
  public static final String   OPERATION_BAD_SOAPBINDING = "temp_025";
  public static final String   XML_TYPE_NO_NAME          = "temp_026";
  public static final String   JAXB_ERROR                = "temp_027";
  public static final String   JAXWS_NOT_SUPPORTED_YET   = "temp_028";
  public static final String   HEADER_PARAM_ERROR        = "temp_029";

  public JaxWsInsideOutException() {
    super(LOC);
  }

  public JaxWsInsideOutException(String patternKey) {
    super(LOC, new LocalizableTextFormatter(TempAccessor.getResourceAccessor(), patternKey));
  }

  public JaxWsInsideOutException(String patternKey, String arg1) {
    super(LOC, new LocalizableTextFormatter(TempAccessor.getResourceAccessor(), patternKey, new Object[] { arg1 }));
  }

  public JaxWsInsideOutException(String patternKey, String... strings) {
    super(LOC, new LocalizableTextFormatter(TempAccessor.getResourceAccessor(), patternKey, new Object[] { strings }));
  }

  public JaxWsInsideOutException(String patternKey, Throwable cause, String... args) {
    super(LOC, new LocalizableTextFormatter(TempAccessor.getResourceAccessor(), patternKey, new Object[] { args }), cause);
  }

  public JaxWsInsideOutException(String patternKey, Throwable cause) {
    super(LOC, new LocalizableTextFormatter(TempAccessor.getResourceAccessor(), patternKey), cause);
  }

}
