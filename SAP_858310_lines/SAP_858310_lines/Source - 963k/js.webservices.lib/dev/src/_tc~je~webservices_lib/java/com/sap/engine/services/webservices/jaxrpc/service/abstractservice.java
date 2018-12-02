package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;

import javax.xml.rpc.Service;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.handler.HandlerRegistry;
import javax.xml.namespace.QName;
import java.util.Iterator;
import java.rmi.Remote;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-8-18
 * Time: 13:17:04
 * To change this template use Options | File Templates.
 */
public abstract class AbstractService implements Service {

  protected QName serviceName;
  protected TypeMappingRegistryImpl typeMappingRegistry;

  public AbstractService(QName serviceName, TypeMappingRegistryImpl typeMappingRegistry) {
    this.serviceName = serviceName;
    this.typeMappingRegistry = typeMappingRegistry;
  }

  public Call createCall() throws ServiceException {
    return(createCall(null, null, null));
  }

  public Call createCall(QName portName) throws ServiceException {
    return(createCall(portName, null, null));
  }

  public Call createCall(QName portName, QName operationName) throws ServiceException {
    return(createCall(portName, null, operationName));
  }

  public Call createCall(QName portName, String operationName) throws ServiceException {
    return(createCall(portName, null, new QName(portName.getNamespaceURI(), operationName)));
  }

  public abstract Call[] getCalls(QName portName) throws ServiceException;

  protected abstract AbstractCall createCall(QName portName, QName portTypeName, QName operationName) throws ServiceException;

  public HandlerRegistry getHandlerRegistry() {
    throw new JAXRPCException("Not supported");
  }

  public abstract Remote getPort(QName name, Class aClass) throws ServiceException;

  public abstract Iterator getPorts();

  public QName getServiceName() {
    return(serviceName);
  }

  public TypeMappingRegistry getTypeMappingRegistry() {
    return(typeMappingRegistry);
  }

  public abstract URL getWSDLDocumentLocation();

  public abstract Remote getPort(Class aClass) throws ServiceException;
}
