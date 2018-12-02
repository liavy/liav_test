package com.sap.engine.services.webservices.espbase.client.api;

import javax.naming.NamingException;

import com.sap.engine.services.webservices.espbase.xi.ESPXIMessageProcessor;

public interface XIManagementInterface {

  public void useXITransport(boolean useXICommunication);
  
  public boolean getUseXITransport();
  
  public void setESPXIMessageProcessor(ESPXIMessageProcessor xiMessageProcessor);
  
  public ESPXIMessageProcessor getESPXIMessageProcessor() throws NamingException;
  
  public XIMessageContext getRequestXIMessageContext();
}
