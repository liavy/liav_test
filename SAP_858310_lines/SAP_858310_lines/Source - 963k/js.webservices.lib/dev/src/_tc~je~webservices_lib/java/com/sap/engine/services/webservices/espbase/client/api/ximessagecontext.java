package com.sap.engine.services.webservices.espbase.client.api;

public interface XIMessageContext {

  public void setApplicationAckRequested(String ackListenerName);
  
  public void setSystemAckRequested(String ackListenerName);
  
  public void setApplicationErrorAckRequested(String ackListenerName);
  
  public void setSystemErrorAckRequested(String ackListenerName);
  
  public void setSenderPartyName(String senderPartyName);
  
  public void setSenderService(String senderService);
  
  public void setQueueId(String queueId);
  
  public void addReceiver(String receiverPartyName, String receiverPartyAgency, String receiverPartyScheme, String receiverService);
  
  public void clearReceivers();
}
