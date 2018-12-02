package com.sap.engine.services.webservices.espbase.xi;

import com.sap.engine.services.webservices.espbase.messaging.ESPXIMessage;
import com.sap.engine.services.webservices.espbase.xi.exceptions.ESPXIException;

public interface ESPXIMessageProcessor {

  public ESPXIMessage process(ESPXIMessage message) throws ESPXIException;
  
  public ESPXIMessage createMessage();
}
