package com.sap.engine.services.webservices.espbase.client.api.impl;

import com.sap.engine.services.webservices.espbase.client.api.XIMessageContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;

public class XIMessageContextImpl implements XIMessageContext {
  
  private ClientConfigurationContext clientCfgCtx;
  
  protected XIMessageContextImpl(ClientConfigurationContext clientCfgCtx) {
    this.clientCfgCtx = clientCfgCtx;
  }

  public void setApplicationAckRequested(String ackListenerName) {
    PublicProperties.setXIApplicationAckRequested(ackListenerName, clientCfgCtx);
  }
  
  public void setSystemAckRequested(String ackListenerName) {
    PublicProperties.setXISystemAckRequested(ackListenerName, clientCfgCtx);
  }
  
  public void setApplicationErrorAckRequested(String ackListenerName) {
    PublicProperties.setXIApplicationErrorAckRequested(ackListenerName, clientCfgCtx);
  }
  
  public void setSystemErrorAckRequested(String ackListenerName) {
    PublicProperties.setXISystemErrorAckRequested(ackListenerName, clientCfgCtx);
  }
  
  public void setSenderPartyName(String senderPartyName) {
    PublicProperties.setXISenderPartyName(senderPartyName, clientCfgCtx);
  }
  
  public void setSenderService(String senderService) {
    PublicProperties.setXISenderService(senderService, clientCfgCtx);
  }
  
  public void setQueueId(String queueId) {
    PublicProperties.setXIQueueId(queueId, clientCfgCtx);
  }
  
  public void addReceiver(String receiverPartyName, String receiverPartyAgency, String receiverPartyScheme, String receiverService) {
    PublicProperties.addXIReceiver(receiverPartyName, receiverPartyAgency, receiverPartyScheme, receiverService, clientCfgCtx);
  }
  
  public void clearReceivers() {
    PublicProperties.clearXIReceivers(clientCfgCtx);
  }
}
