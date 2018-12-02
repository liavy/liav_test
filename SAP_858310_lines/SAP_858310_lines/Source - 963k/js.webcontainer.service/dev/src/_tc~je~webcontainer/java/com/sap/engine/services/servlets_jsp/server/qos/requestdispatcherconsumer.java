package com.sap.engine.services.servlets_jsp.server.qos;

import com.sap.engine.lib.rcm.ResourceConsumer;

public class RequestDispatcherConsumer implements ResourceConsumer {
  public static final String REQUEST_DISPATCHER_CONSUMER = "Request Dispatcher Consumer";
  public static final String WCE_REQUEST_DISPATCHER_CONSUMER = "WCE Request Dispatcher Consumer";
  private String type;
  private String id = "empty";
  
  public RequestDispatcherConsumer(String type) {
    super();    
    this.type = type;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {   
    return id;
  }

  public String getType() {    
    return type;
  }
}
