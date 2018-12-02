/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.server.additions.wsa;

import com.sap.engine.services.webservices.espbase.server.additions.exceptions.BaseProtocolException;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.exc.WSAResourceAccessor;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2005, SAP-AG
 * 
 * @author Dimitar Angelov
 * @version 1.0, 2005-12-7
 */
public class WSAddressingException extends BaseProtocolException {
  
  public static final String UNSUPPORTED_REPLYTO_FAULTTO_COMBINATION  =  "webservices_2000";
  
  public WSAddressingException(String pattern) {
    super(WSAResourceAccessor.getResourceAccessor(), pattern);
  }
  public WSAddressingException(String pattern, Object[] obj) {
    super(WSAResourceAccessor.getResourceAccessor(), pattern, obj);
  }
  public WSAddressingException(String pattern, Object[] obj, Throwable cause) {
    super(WSAResourceAccessor.getResourceAccessor(), pattern, obj, cause);
  }
  public WSAddressingException(String pattern, Throwable cause) {
    super(WSAResourceAccessor.getResourceAccessor(), pattern, cause);
  }
  public WSAddressingException(Throwable cause) {
    super(cause);
  }
}