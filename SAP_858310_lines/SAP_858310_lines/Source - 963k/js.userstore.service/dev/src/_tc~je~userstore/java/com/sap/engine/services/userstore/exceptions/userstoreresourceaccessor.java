package com.sap.engine.services.userstore.exceptions;

import com.sap.localization.ResourceAccessor;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

public class UserstoreResourceAccessor extends ResourceAccessor {
  private static String BUNDLE_NAME = "com.sap.engine.services.userstore.exceptions.UserstoreResourceBundle";
  private static ResourceAccessor resourceAccessor = null;
  public static Category category = null;
  public static Location location = null;

  public UserstoreResourceAccessor() {
    super(BUNDLE_NAME);
  }

  public void init(Category category, Location location) {
    UserstoreResourceAccessor.category = category;
    UserstoreResourceAccessor.location = location;
  }

  public static synchronized ResourceAccessor getResourceAccessor() {
    if (resourceAccessor == null) {
      resourceAccessor = new UserstoreResourceAccessor();
    }
    return resourceAccessor;
  }
}
