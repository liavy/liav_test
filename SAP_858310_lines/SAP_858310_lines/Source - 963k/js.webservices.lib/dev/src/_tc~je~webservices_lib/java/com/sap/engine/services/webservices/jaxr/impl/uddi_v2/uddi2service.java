﻿package com.sap.engine.services.webservices.jaxr.impl.uddi_v2;

/**
 * Service Interface (generated by SAP WSDL to Java generator).
 */

public interface UDDI2Service extends javax.xml.rpc.Service {

  public java.rmi.Remote getLogicalPort(String portName, Class seiClass) throws javax.xml.rpc.ServiceException;
  public java.rmi.Remote getLogicalPort(Class seiClass) throws javax.xml.rpc.ServiceException;
  public String[] getLogicalPortNames();
  public com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortType getLogicalPortConfiguration(String lpName);

}