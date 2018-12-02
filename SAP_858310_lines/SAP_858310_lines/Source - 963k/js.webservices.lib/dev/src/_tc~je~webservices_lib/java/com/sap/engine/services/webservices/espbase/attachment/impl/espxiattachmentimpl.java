package com.sap.engine.services.webservices.espbase.attachment.impl;

import com.sap.engine.services.webservices.espbase.attachment.ESPXIAttachment;

public class ESPXIAttachmentImpl implements ESPXIAttachment {

  private byte[] data;
  private String name;
  private String type;
  
  public byte[] getData() {
    return(data);
  }
  
  public void setData(byte[] data) {
    this.data = data;
  }
  
  public String getName() {
    return(name);
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getType() {
    return(type);
  }
  
  public void setType(String type) {
    this.type = type;
  }
}
