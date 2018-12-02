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
package com.sap.engine.services.webservices.jaxm.soap.accessor;

import com.sap.localization.ResourceAccessor;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * This class is resource accessor for all SOAP Implementation errors.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SOAPAccessor extends ResourceAccessor {

  private static String BUNDLE_NAME = "com.sap.engine.services.webservices.jaxm.soap.accessor.SOAPBundle";
  private static ResourceAccessor resourceAccessor = null;
  public static Category category = null;
  public static Location location = null;

  public SOAPAccessor() {
    super(BUNDLE_NAME);
  }

  public static synchronized ResourceAccessor getResourceAccessor() {
    if (resourceAccessor == null) {
      resourceAccessor = new SOAPAccessor();
    }
    return resourceAccessor;
  }

}
