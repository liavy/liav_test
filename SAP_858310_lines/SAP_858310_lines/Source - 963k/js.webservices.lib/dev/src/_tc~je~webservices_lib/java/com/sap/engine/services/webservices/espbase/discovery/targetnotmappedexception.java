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
package com.sap.engine.services.webservices.espbase.discovery;

import com.sap.engine.services.webservices.jaxrpc.exceptions.accessors.XmlSerializationResourceAccessor;
import com.sap.exception.BaseException;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2004, SAP-AG
 * @author Alexander Zubev
 * @version 1.1, 2005-10-3
 */
public class TargetNotMappedException extends BaseException {
  private static final String TARGET_NOT_MAPPED = "webservices_3702"; 
  
  public TargetNotMappedException(Location location, String targetName) {
    super(location, XmlSerializationResourceAccessor.getResourceAccessor(), TARGET_NOT_MAPPED, new Object[] {targetName});
  }

  public TargetNotMappedException(Location location, Throwable cause) {
    super(location, cause);
  }

  public TargetNotMappedException(Location location, String targetName, Throwable cause) {
    super(location, XmlSerializationResourceAccessor.getResourceAccessor(), TARGET_NOT_MAPPED, new Object[] {targetName});
  }
}
