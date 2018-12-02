package com.sap.engine.services.webservices.espbase.xi.impl;

import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.services.webservices.espbase.messaging.ESPXIMessage;
import com.sap.engine.services.webservices.espbase.xi.ESPXIMessageProcessor;

public class ESPXITransport extends Transport {

  private static final String TRANSPORT_ID = "ESP XI Transport";
  
  private ESPXIMessage requestXIMessage;
  private ESPXIMessage responseXIMessage;
  private ESPXIMessageProcessor xiMessageProcessor;
  private Throwable severError;
  
  public void setXIMessageProcessor(ESPXIMessageProcessor xiMessageProcessor) {
    this.xiMessageProcessor = xiMessageProcessor;
  }
  
  public ESPXIMessageProcessor getXIMessageProcessor() {
    return(xiMessageProcessor);
  }
  
  public boolean sendServerError(Throwable severError) {
    this.severError = severError;
    return(severError !=  null);
  }
  
  protected Throwable getServerError() {
    return(severError);
  }

  public String getTransportID() {
    return(TRANSPORT_ID);
  }
  
  protected void setRequestXIMessage(ESPXIMessage requestXIMessage) {
    this.requestXIMessage = requestXIMessage;
  }
  
  public ESPXIMessage getRequestXIMessage() {
    return(requestXIMessage);
  }

  public ESPXIMessage getResponseXIMessage() {
    return(responseXIMessage);
  }

  public void setResponseXIMessage(ESPXIMessage responseXIMessage) {
    this.responseXIMessage = responseXIMessage;
  }
}
