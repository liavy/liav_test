package com.sap.engine.services.webservices.espbase.client.bindings.impl;

import com.sap.sdo.api.helper.ErrorHandler;
import com.sap.tc.logging.Location;

public class SDOErrorHandler implements ErrorHandler {
  
  private static Location LOC = Location.getLocation(SDOErrorHandler.class);  
  protected static SDOErrorHandler instance = null;

  private SDOErrorHandler() {
  }
  
  public synchronized static SDOErrorHandler getInstance() {
    if (instance == null) {
      instance = new SDOErrorHandler();
    }

    return instance;
  }
  
  public void handleInvalidValue(RuntimeException pException) {
    LOC.catching(pException);
  }
    
  public void handleUnknownProperty(RuntimeException pException) {
    LOC.catching(pException);
  }
  
}

