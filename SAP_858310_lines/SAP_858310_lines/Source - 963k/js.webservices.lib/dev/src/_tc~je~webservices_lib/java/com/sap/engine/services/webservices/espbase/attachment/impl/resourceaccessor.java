package com.sap.engine.services.webservices.espbase.attachment.impl;


class ResourceAccessor extends com.sap.localization.ResourceAccessor {
  
  static final String INVALID_PARAMETER_NULL = "webservices_2000";
  static final String MISSING_CONTENTID_HEADER = "webservices_2001";
  static final String ATTACHMENTE_IS_ALREADY_PRESENT = "webservices_2002";
  static final String INVALID_CID_URL = "webservices_2003";
  
  private static final String BUNDLE_NAME = "com.sap.engine.services.webservices.espbase.attachment.impl.ResourceBundle";
  private static ResourceAccessor resourceAccessor;
  
  static {
    resourceAccessor = new ResourceAccessor();
  }

  public ResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public static ResourceAccessor getResourceAccessor() {
    return resourceAccessor;
  }
}
