package com.sap.engine.services.webservices.common;

import java.io.Serializable;
/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public abstract class Request implements Serializable {
  
  protected int requestId;

  public Request() {

  }
  
  public byte[] getBytes() throws Exception {
    return new byte[0];
  }

  public int getRequestId() {
    return requestId;
  }

  public abstract String getRequestName();

}