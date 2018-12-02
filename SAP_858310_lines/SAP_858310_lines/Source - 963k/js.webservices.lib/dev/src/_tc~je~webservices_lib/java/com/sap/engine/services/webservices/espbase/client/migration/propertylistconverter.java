package com.sap.engine.services.webservices.espbase.client.migration;

import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;

public interface PropertyListConverter {
  
  public PropertyListType convertProperties(PropertyListType inputProperties);
  
}
