package com.sap.engine.services.webservices.jaxrpc.exceptions;

import com.sap.localization.ResourceAccessor;

public class WSResourceAccessor extends ResourceAccessor {
  
  private static String BUNDLE_NAME = "com.sap.engine.services.webservices.jaxrpc.exceptions.WSBundle";
  private static ResourceAccessor resourceAccessor = null;

  public WSResourceAccessor()  {
    super(BUNDLE_NAME);
  }

  public static synchronized ResourceAccessor getResourceAccessor() {
    if (resourceAccessor == null) {
      resourceAccessor = new WSResourceAccessor();
    }
    return resourceAccessor;
  }
  
}
