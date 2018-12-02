package com.sap.engine.services.webservices.espbase.messaging;

import java.util.Enumeration;

import javax.xml.namespace.QName;

import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.services.webservices.espbase.attachment.ESPXIAttachment;

public interface ESPXIMessage extends Message {
  
  public void clear();
  
  public void setData(String data);
  
  public String getData();
  
  public boolean isFault();
  
  public void setFault(boolean isFault);
  
  public void setServiceInterface(QName interfaceName);
  
  public QName getServiceInterfaceName();
  
  public void setAsync(boolean async);
  
  public boolean isAsync();
  
  public void addAttachment(ESPXIAttachment attachment);
  
  public ESPXIAttachment addAttachment(String name, String type, byte[] data);
  
  public ESPXIAttachment createAttachment(String name, String type, byte[] data);
  
  public ESPXIAttachment getAttachment(String name);
  
  public Enumeration<ESPXIAttachment> getAttachments();
  
  public void clearAttachments();
  
  public boolean removeAttachment(ESPXIAttachment attachment);
  
  public void setApplicationAckRequested(String ackListenerName);
  
  public String getApplicationAckRequested();
  
  public void setSystemAckRequested(String ackListenerName);
  
  public String getSystemAckRequested();
  
  public void setApplicationErrorAckRequested(String ackListenerName);

  public String getApplicationErrorAckRequested();

  public void setSystemErrorAckRequested(String ackListenerName);
  
  public String getSystemErrorAckRequested();
  
  public void setSenderPartyName(String senderPartyName);
  
  public String getSenderPartyName();
  
  public void setSenderService(String senderService);
  
  public String getSenderService();
  
  public void setQueueId(String queueId);
  
  public String getQueueId();
  
  public void addReceiver(String receiverPartyName, String receiverPartyAgency, String receiverPartyScheme, String receiverService);
  
  /**
   * Used to insert metering information in the message,
   * called once for each header. 
   * More than one value can be set for a given header
   * (not applicable for service metering headers)  
   * @param headerName 
   * @param headerValue
   */
  public void addHeader(QName headerName, String headerValue);
  
  /**
   * Used to extract metering information from the message
   * @param headerName 
   */
  public String[] getHeader(QName headerName);
}
