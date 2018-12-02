package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;

public class PortInfoImpl implements PortInfo {
  private QName serviceName = null;
  private QName portName = null;
  private String bindingID = SOAPBinding.SOAP11HTTP_BINDING;  
  private String interfaceMappingId;
  
  public String getInterfacemappingId() {
    return this.interfaceMappingId;
  }
  
  public PortInfoImpl(QName service, QName port, String binding, String interfaceMappingId) {
    serviceName = service;
    portName = port;
    bindingID = binding;    
    this.interfaceMappingId = interfaceMappingId;
  }
  
  public PortInfoImpl(QName service, QName port) {
    serviceName = service;
    portName = port;
  }  
  
  public QName getServiceName() {
    return serviceName;
  }

  public QName getPortName() {
    return portName;
  }

  public String getBindingID() {
    return bindingID;
  }

}
