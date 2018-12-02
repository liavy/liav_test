package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.wsdl.WSDLPortType;
import com.sap.engine.services.webservices.wsdl.WSDLOperation;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;

import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.JAXRPCException;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.rmi.Remote;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-8-18
 * Time: 13:40:23
 * To change this template use Options | File Templates.
 */
public class BasicService extends AbstractService {

  public BasicService(QName serviceName, TypeMappingRegistryImpl typeMappingRegistry) {
    super(serviceName, typeMappingRegistry);
  }

  public Call[] getCalls(QName portName) throws ServiceException {
    throw new UnsupportedOperationException("No wsdl location is defined.");
  }

  protected AbstractCall createCall(QName portName, QName portTypeName, QName operationName) throws ServiceException {
    AbstractCall call = new BasicCall(typeMappingRegistry);
    call.setOperationName(operationName);
    return(call);
  }

  public Remote getPort(Class aClass) throws ServiceException {
    throw new ServiceException("No wsdl location is defined.");
  }

  public Remote getPort(QName name, Class aClass) throws ServiceException {
    throw new ServiceException("No wsdl location is defined.");
  }

  public Iterator getPorts() {
    throw new UnsupportedOperationException("No wsdl location is defined.");
  }

  public URL getWSDLDocumentLocation() {
    throw new UnsupportedOperationException("No wsdl location is defined.");
  }
}
