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

import com.sap.localization.ResourceAccessor;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * @author Dimitar Velichkov  dimitar.velichkov@sap.com
 */
public class TempAccessor extends ResourceAccessor {

  private static String BUNDLE_NAME = "com.sap.engine.services.webservices.jaxws.j2w.SerializationBundle";
  private static ResourceAccessor resourceAccessor = null;
  public static Category category = null;
  public static Location location = null;

  public TempAccessor() {
    super(BUNDLE_NAME);
  }

  public static synchronized ResourceAccessor getResourceAccessor() {
    if (resourceAccessor == null) {
      resourceAccessor = new TempAccessor();
    }
    return resourceAccessor;
  }
  
}
