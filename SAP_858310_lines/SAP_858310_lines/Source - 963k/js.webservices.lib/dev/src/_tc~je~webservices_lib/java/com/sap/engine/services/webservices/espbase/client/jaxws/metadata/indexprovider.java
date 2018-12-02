package com.sap.engine.services.webservices.espbase.client.jaxws.metadata;

public class IndexProvider {
  
  private int index;
  
  protected IndexProvider() {
    index = 0;
  }
  
  protected void reset() {
    index = 0;
  }
  
  protected int provide() {
    return(index++);
  }
}
