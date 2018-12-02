package com.sap.engine.services.webservices.espbase.xi.util;

public class XIReceiver {
  
  private String receiverPartyName;
  private String receiverPartyAgency;
  private String receiverPartyScheme;
  private String receiverService;
  
  public XIReceiver(String receiverPartyName, String receiverPartyAgency, String receiverPartyScheme, String receiverService) {
    this.receiverPartyName = receiverPartyName;
    this.receiverPartyAgency = receiverPartyAgency;
    this.receiverPartyScheme= receiverPartyScheme;
    this.receiverService = receiverService;
  }
  
  public String getReceiverPartyName() {
    return(receiverPartyName);
  }

  public String getReceiverPartyAgency() {
    return(receiverPartyAgency);
  }

  public String getReceiverPartyScheme() {
    return(receiverPartyScheme);
  }

  public String getReceiverService() {
    return(receiverService);
  }
}
