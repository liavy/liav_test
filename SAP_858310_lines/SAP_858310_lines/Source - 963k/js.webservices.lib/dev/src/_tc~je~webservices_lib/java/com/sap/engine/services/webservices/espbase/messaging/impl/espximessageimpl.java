package com.sap.engine.services.webservices.espbase.messaging.impl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.attachment.ESPXIAttachment;
import com.sap.engine.services.webservices.espbase.attachment.impl.ESPXIAttachmentImpl;
import com.sap.engine.services.webservices.espbase.messaging.ESPXIMessage;

public class ESPXIMessageImpl implements ESPXIMessage {

  private String data;
  private QName interfaceName;
  private boolean isAsync;
  private Hashtable nameToAttachmentsMap;
  private String partyName;
  private String service;
  private String queueId;
  private boolean isFault;
  private String appAckListener;
  private String appErrorAckListener;
  private String sysAckListener;
  private String sysErrorAckListener;
  private Map<QName, String[]> headers = new HashMap<QName, String[]>();

  public ESPXIMessageImpl() {
    nameToAttachmentsMap = new Hashtable();
  }

  public void clear() {
    data = null;
    interfaceName = null;
    isAsync = false;
    clearAttachments();
    partyName = null;
    service = null;
    queueId = null;
    isFault = false;
    appAckListener = null;
    appErrorAckListener = null;
    sysAckListener = null;
    sysErrorAckListener = null;
    headers.clear();
  }

  public String getData() {
    return(data);
  }

  public void setData(String data) {
    this.data = data;
  }

  public boolean isFault() {
    return(isFault);
  }

  public void setFault(boolean isFault) {
    this.isFault = isFault;
  }

  public void setServiceInterface(QName interfaceName) {
    this.interfaceName = interfaceName;
  }

  public QName getServiceInterfaceName() {
    return(interfaceName);
  }

  public void addAttachment(ESPXIAttachment attachment) {
    nameToAttachmentsMap.put(attachment.getName(), attachment);
  }

  public ESPXIAttachment addAttachment(String name, String type, byte[] data) {
    ESPXIAttachment attachment = createAttachment(name, type, data);
    addAttachment(attachment);
    return(attachment);
  }

  public ESPXIAttachment createAttachment(String name, String type, byte[] data) {
    ESPXIAttachmentImpl attachment = new ESPXIAttachmentImpl();
    attachment.setName(name);
    attachment.setType(type);
    attachment.setData(data);
    return(attachment);
  }

  public ESPXIAttachment getAttachment(String name) {
    return((ESPXIAttachment)(nameToAttachmentsMap.get(name)));
  }

  public Enumeration<ESPXIAttachment> getAttachments() {
    return(nameToAttachmentsMap.elements());
  }

  public void clearAttachments() {
    nameToAttachmentsMap.clear();
  }

  public boolean removeAttachment(ESPXIAttachment attachment) {
    nameToAttachmentsMap.remove(attachment.getName());
    return(true);
  }

  public void addReceiver(String receiverPartyName, String receiverPartyAgency, String receiverPartyScheme, String receiverService) {
  }

  public void setApplicationAckRequested(String appAckListener) {
    this.appAckListener = appAckListener;
  }

  public String getApplicationAckRequested() {
    return(appAckListener);
  }

  public void setSystemAckRequested(String sysAckListener) {
    this.sysAckListener = sysAckListener;
  }

  public String getSystemAckRequested() {
    return(sysAckListener);
  }

  public void setApplicationErrorAckRequested(String appErrorAckListener) {
    this.appErrorAckListener = appErrorAckListener;
  }

  public String getApplicationErrorAckRequested() {
    return(appErrorAckListener);
  }

  public void setSystemErrorAckRequested(String sysErrorAckListener) {
    this.sysErrorAckListener = sysErrorAckListener;
  }

  public String getSystemErrorAckRequested() {
    return(sysErrorAckListener);
  }

  public void setSenderPartyName(String partyName) {
    this.partyName = partyName;
  }

  public String getSenderPartyName() {
    return(partyName);
  }

  public void setSenderService(String service) {
    this.service = service;
  }

  public String getSenderService() {
    return(service);
  }

  public void setQueueId(String queueId) {
    this.queueId = queueId;
  }

  public String getQueueId() {
    return(queueId);
  }

  public void setAsync(boolean isAsync) {
    this.isAsync = isAsync;
  }

  public boolean isAsync() {
    return(isAsync);
  }

  public void addHeader(QName headerName, String headerValue){
    String[] existingHeader = (String[]) headers.get(headerName);
    String[] newHeader = null;

    if (existingHeader == null) {
      newHeader = new String[] {headerValue};
    } else {
      newHeader = new String[existingHeader.length + 1];
      System.arraycopy(existingHeader, 0, newHeader, 0, existingHeader.length);
      newHeader[existingHeader.length] = headerValue;
    }
    headers.put(headerName, newHeader);
  }

  public String[] getHeader(QName headerName){
    String[] existingHeader = headers.get(headerName);
    return existingHeader == null ? new String[] {} : existingHeader;
  }
}
