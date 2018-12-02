/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.Remote;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.handler.HandlerRegistry;

import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.interfaces.webservices.runtime.component.ClientComponentFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterface;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;
import com.sap.engine.services.webservices.jaxrpc.util.IteratorImpl;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortFactory;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortType;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;

/**
 * Default service provider implementation.
 * Implements jax.rpc.Service interface for compability most of the methods are not implemented.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ServiceBase implements Service {

  protected HTTPProxyResolver proxyResolver;//$JL-SER$
  protected SLDConnection sldConnection;//$JL-SER$
  protected LogicalPorts logicalPorts = null;
  protected Hashtable protocols = new Hashtable();
  protected com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl _typeRegistry = null;
  private static LogicalPortFactory factory = null;
  private WSDLDefinitions definitions = null;
  protected ClientComponentFactory componentFactory;//$JL-SER$

  public void _setWSDLDefinitions(WSDLDefinitions wsdlDefinitions) {
    this.definitions = wsdlDefinitions;
  }

  public WSDLDefinitions _getWSDefinitions() {
    return this.definitions;
  }

  public void _setComponentFactory(ClientComponentFactory componentFactory) {
    this.componentFactory = componentFactory;
  }

  private static synchronized LogicalPortFactory getLPFactory() {
    if (factory == null) {
      factory = new LogicalPortFactory();
    }
    return factory;
  }


  public ServiceBase() {
    // Default constructor
  }

  public ServiceBase(InputStream input) throws Exception {
    LogicalPortFactory factory = getLPFactory();
    logicalPorts = factory.loadLogicalPorts(input);
    input.close();
  }

  public ServiceBase(File input) throws Exception {
    LogicalPortFactory factory = getLPFactory();
    logicalPorts = factory.loadLogicalPorts(input);
  }

  public ServiceBase(String input) throws Exception {
    LogicalPortFactory factory = getLPFactory();
    logicalPorts = factory.loadLogicalPorts(input);
  }

  public void init(InputStream input) throws Exception {
    LogicalPortFactory factory = getLPFactory();
    logicalPorts = factory.loadLogicalPorts(input);
    input.close();
  }

  public void init(LogicalPorts lports) throws Exception {
    logicalPorts = lports;
  }

  public Call createCall() throws ServiceException {
    return null;
  }

  public Call createCall(QName name) throws ServiceException {
    return null;
  }

  public Call createCall(QName name, QName name1) throws ServiceException {
    return null;
  }

  public Call createCall(QName name, String s) throws ServiceException {
    return null;
  }

  public Call[] getCalls(QName name) throws ServiceException {
    return new Call[0];
  }

  public HandlerRegistry getHandlerRegistry() {
    return null;
  }

  public Remote getPort(Class aClass) throws ServiceException {
    return getLogicalPort(aClass);
  }

  public Remote getPort(QName name, Class aClass) throws ServiceException {
    Object stub;
    try {
      //LogicalPortFactory factory = new LogicalPortFactory();
      if (aClass != null && DInterface.class.isAssignableFrom(aClass)) {
        // Hack to capture case when dynamic proxy is used on the j2ee engine but standalone protocols should be used
        stub = factory.getConfiguredStub(logicalPorts,name.getLocalPart(), protocols,null, this.getClass().getClassLoader(),this._typeRegistry);
      } else {
      stub = factory.getConfiguredStub(logicalPorts,name.getLocalPart(), protocols,componentFactory, this.getClass().getClassLoader(),this._typeRegistry);
      }
    } catch (Exception e) {
      throw new ServiceException(e);
    }
    if (stub == null) {
      throw new ServiceException("Port with name '"+name.getLocalPart()+"' does not exists in client configuration !");
    }
//    if (aClass.isInstance(stub)) {
//      throw new ServiceException("Requested port does not implement given SEI !");
//    }
    BaseGeneratedStub baseGenStub = (BaseGeneratedStub) stub;
    if (proxyResolver != null) {
      baseGenStub._setHTTPProxyResolver(proxyResolver);
    }        
    return (Remote) stub;
  }

  /**
   * Server Managed Port Access.
   * @param aClass
   * @return
   * @throws ServiceException
   */
  public Remote getLogicalPort(Class aClass) throws ServiceException {
    Object stub = null;
    // Selects default locgal port
    LogicalPortType[] ports = logicalPorts.getLogicalPort();
    String defaultName = null;
    if (ports.length>0) { // Select first found by default
      defaultName = ports[0].getName();
    }
    for (int i=0; i<ports.length; i++) {
      if (ports[i].getDefault()) {
        defaultName = ports[i].getName();
      }
    }
    try {
      //LogicalPortFactory factory = new LogicalPortFactory();

      if (defaultName != null) {
        if (DInterface.class.isAssignableFrom(aClass)) {
          // Hack to capture case when dynamic proxy is used on the j2ee engine but standalone protocols should be used
          stub = factory.getConfiguredStub(logicalPorts,defaultName, protocols,null, aClass.getClassLoader(),this._typeRegistry);
        } else {
        stub = factory.getConfiguredStub(logicalPorts,defaultName, protocols,componentFactory, aClass.getClassLoader(),this._typeRegistry);
      }
      }
    } catch (Exception e) {
      throw new WebserviceClientException(WebserviceClientException.STUB_INSTANTIATION_ERR,e);
    }
    if (stub == null) {
      throw new WebserviceClientException(WebserviceClientException.NO_PORT_AVAILABLE,this.getClass().getName());
    }
    BaseGeneratedStub baseGenStub = (BaseGeneratedStub) stub;
    if (proxyResolver != null) {
      baseGenStub._setHTTPProxyResolver(proxyResolver);
    }
    return (Remote) stub;
  }

  public Iterator getPorts() {
    LogicalPortType[] ports = this.logicalPorts.getLogicalPort();
    IteratorImpl iterator = new IteratorImpl();
    for (int i=0; i<ports.length; i++) {
      iterator.addObject(new QName(ports[i].getName()));
    }    
    return iterator;
  }

  public QName getServiceName() {
    return new QName(this.logicalPorts.getName());
  }

  public TypeMappingRegistry getTypeMappingRegistry() {
    return this._typeRegistry;
  }

  public URL getWSDLDocumentLocation() {
    return null;
  }

  protected void loadProtocolsFromPropertyFile(InputStream input) throws ServiceException {
    Properties p = new Properties();
    try {
      p.load(input);
    } catch (Exception e) {
      try {
        input.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      return; // Property file not loaded at all
    }
    Enumeration e = p.keys();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      String value = p.getProperty(key);
      Class protocol = null;
      try {
        protocol = this.getClass().getClassLoader().loadClass(value);
        Object protocolInst = protocol.newInstance();
        if (!(protocolInst instanceof AbstractProtocol)) {
          System.out.println("Warning ! Protocol Implementation ["+value+"] could not be loaded it is not Implementation of AbstractProtocol interface !");
        } else {
          protocols.put(key,protocolInst);
        }
      } catch (NoClassDefFoundError e1) {
        System.out.println("Warning ! Protocol Implementation ["+value+"] could not be loaded (NoClassDefFoundError) !");
        System.out.println("Error Message is :"+e1.getMessage());
      } catch (ClassNotFoundException e1) {
        System.out.println("Warning ! Protocol Implementation ["+value+"] could not be loaded (ClassNotFound) !");
        System.out.println("Error Message is :"+e1.getMessage());
        if (e1.getException() != null) {
          System.out.println("Nested Exception is :");
          e1.getException().printStackTrace(System.out);
        }
      } catch (InstantiationException e1) {
        System.out.println("Warning ! Protocol Implementation ["+value+"] could not be loaded (InstantiationException) !");
        System.out.println("Error Message is :"+e1.getMessage());
      } catch (IllegalAccessException e1) {
        System.out.println("Warning ! Protocol Implementation ["+value+"] could not be loaded (IllegalAccess) !");
        System.out.println("Error Message is :"+e1.getMessage());
      }
    }
  }

  /**
   * Returns a list of all logical ports that are available.
   * @return
   */
  public String[] getLogicalPortNames() {
    LogicalPortType[] logicalPorts = this.logicalPorts.getLogicalPort();
    int length = 0;
    if (logicalPorts != null) {
      length = logicalPorts.length;
    }
    String[] result = new String[length];
    for (int i=0; i<result.length; i++) {
      result[i] = logicalPorts[i].getName();
    }
    return result;
  }

  /**
   * Returns logical port configuration information for selected logical port.
   * Note that at runtime Logical Port Information can not be changed.
   * @param lpName
   * @return returns null if logical port with this name can not be found.
   */
  public LogicalPortType getLogicalPortConfiguration(String lpName) {
    LogicalPortType[] logicalPorts = this.logicalPorts.getLogicalPort();
    if (logicalPorts == null) {
      return null;
    }
    for (int i=0; i<logicalPorts.length; i++) {
      if (logicalPorts[i].getName().equals(lpName)) {
        return (LogicalPortType) logicalPorts[i].clone();
      }
    }
    return null;
  }

  public void setHTTPProxyResolver(HTTPProxyResolver proxyResolver) {
    this.proxyResolver = proxyResolver;
  }
      
  public void setSLDConnection(SLDConnection sldConnection) {
    this.sldConnection = sldConnection;
  }
  
  public SLDConnection getSLDConnection() {
    return sldConnection;
  }
}
