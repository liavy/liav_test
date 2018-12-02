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
import java.io.InputStream;
import java.rmi.Remote;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;
import com.sap.engine.interfaces.webservices.runtime.component.ClientComponentFactory;
import com.sap.engine.interfaces.webservices.server.SLDLogicalPort;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.exceptions.LogicalPortException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.*;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.tc.logging.Location;

/**
 * Service implementation needed for server side.
 * Initializes with Client Component Factory to obtain deployed client protocols.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ServiceBaseServer extends  ServiceBase {

  //private RuntimeInformation runtime = null;
  private Hashtable lportProtocols = null;
  private Hashtable lportRuntimeInfo = null;
  protected ClassLoader applicationLoader = null;//$JL-SER$
  protected ClientComponentFactory componentFactory;//$JL-SER$
  private LogicalPortFactory factory = new LogicalPortFactory();//$JL-SER$
  //private TypeMappingRegistry registry = null;
  private String logLocation = null;

  public ServiceBaseServer() {
  }

  public ServiceBaseServer(InputStream input, ClientComponentFactory factory, ClassLoader applicationLoader) throws Exception {
    super(input);
    this.applicationLoader = applicationLoader;
    this.componentFactory = factory;
  }
  
  public ServiceBaseServer(File input, ClientComponentFactory factory, ClassLoader applicationLoader) throws Exception {
    super(input);
    this.applicationLoader = applicationLoader;
    this.componentFactory = factory;
  }
  
  public ServiceBaseServer(String input, ClientComponentFactory factory, ClassLoader applicationLoader) throws Exception {
    super(input);
    this.applicationLoader = applicationLoader;
    this.componentFactory = factory;
  }

  public ServiceBaseServer(File input, ClientComponentFactory factory, ClassLoader applicationLoader, TypeMappingRegistry registry) throws Exception {
    super(input);
    this.applicationLoader = applicationLoader;
    this.componentFactory = factory;
    this._typeRegistry = (TypeMappingRegistryImpl) registry;
  }

  public ServiceBaseServer(String input, ClientComponentFactory factory, ClassLoader applicationLoader, TypeMappingRegistry registry) throws Exception {
    super(input);
    this.applicationLoader = applicationLoader;
    this.componentFactory = factory;
    this._typeRegistry = (TypeMappingRegistryImpl) registry;
  }


  public void init(InputStream input, ClientComponentFactory factory, ClassLoader applicationLoader) throws Exception {
    super.init(input);
    this.applicationLoader = applicationLoader;
    this.componentFactory = factory;
  }

  public void init(LogicalPorts lports, ClientComponentFactory factory, ClassLoader applicationLoader) throws Exception {
    super.init(lports);
    this.applicationLoader = applicationLoader;
    this.componentFactory = factory;
  }

  public void init(LogicalPorts lports, ClientComponentFactory factory, ClassLoader applicationLoader, String location) throws Exception {
    super.init(lports);
    this.applicationLoader = applicationLoader;
    this.componentFactory = factory;
    this.logLocation = location;
  }


  public void init(InputStream input, ClientComponentFactory factory, ClassLoader applicationLoader, TypeMappingRegistry registry) throws Exception {
    this.init(input,factory,applicationLoader);
    this._typeRegistry = (TypeMappingRegistryImpl) registry;
  }

  public void init(LogicalPorts lports, ClientComponentFactory factory, ClassLoader applicationLoader, TypeMappingRegistry registry) throws Exception {
    this.init(lports,factory,applicationLoader);
    this._typeRegistry = (TypeMappingRegistryImpl) registry;
  }


  private void processFeatures(FeatureType[] features, PropertyContext pContext, HashSet usedProtocols, Hashtable protocols) {
    for (int i=0; i<features.length; i++) {
      loadProperties(features[i].getProperty(),pContext.getSubContext(features[i].getName()));
      String providerName = features[i].getProvider();
      if (providerName != null) {
        Object aProtocol = protocols.get(providerName);
        if (aProtocol != null) {
          usedProtocols.add(aProtocol);
        }
      }
    }
  }

  private void initRuntimeInformation(RuntimeInformation runtimeInformation) {
    lportProtocols = new Hashtable();
    lportRuntimeInfo = new Hashtable();
    LogicalPortType[] logicalPorts = this.logicalPorts.getLogicalPort();
    for (int i=0; i<logicalPorts.length; i++) {
      String lportName = logicalPorts[i].getName();
      String endpoint = logicalPorts[i].getEndpoint();
      RuntimeInformation runtimeInfonew = new RuntimeInformation();
      runtimeInfonew.setApplicationName(runtimeInformation.getApplicationName());
      runtimeInfonew.setEndpoint(endpoint);
      runtimeInfonew.setLogicalPortName(lportName);
      runtimeInfonew.setSLDSystemName(logicalPorts[i].getSystemName());
      runtimeInfonew.setSLDWServiceName(logicalPorts[i].getSLDWS());
      runtimeInfonew.setSLDWServicePort(logicalPorts[i].getSLDWSPort());
      runtimeInfonew.setLogicalPortType(logicalPorts[i]);
      runtimeInfonew.setJndiName(runtimeInformation.getJndiName());
      runtimeInfonew.setArchiveName(runtimeInformation.getArchiveName());
      lportRuntimeInfo.put(lportName,runtimeInfonew);
      ClientFeatureProvider[] protocols = componentFactory.listClientProtocolInterfaces();
      lportProtocols.put(lportName,protocols);
    }
  }

  /**
   * This method is called on server to call onApplicationStart of protocols.
   * Only Global Settings are used.
   * @param runtimeInformation
   */
  public void setRuntimeInformation(RuntimeInformation runtimeInformation) {
    initRuntimeInformation(runtimeInformation);
    Hashtable perm = new Hashtable();
    LogicalPortType[] logicalPorts = this.logicalPorts.getLogicalPort();
    HashSet usedProtocols = new HashSet();
    PropertyContext propertyContext = new PropertyContext();
    for (int i=0; i<logicalPorts.length; i++) {
      perm.clear();
      usedProtocols.clear();
      propertyContext.clear();
      LogicalPortType logicalPort = logicalPorts[i];
      ClientFeatureProvider[] protocols = (ClientFeatureProvider[]) lportProtocols.get(logicalPort.getName());
      for (int j=0; j<protocols.length; j++) {
        perm.put(((AbstractProtocol)protocols[j]).getName(),protocols[j]);
      }
      RuntimeInformation runtimeInfo = (RuntimeInformation) lportRuntimeInfo.get(logicalPort.getName());
      GlobalFeatures globalFeatures = logicalPort.getGlobalFeatures();
      processFeatures(globalFeatures.getFeature(),propertyContext,usedProtocols,perm);
      propertyContext.setProperty(ClientProtocolStartAppEvent.RUNTIMEINFO,runtimeInfo);
      Iterator it = usedProtocols.iterator();
      while (it.hasNext()) {
        Object obj = it.next();
        if (obj instanceof ClientProtocolStartAppEvent) {
          ((ClientProtocolStartAppEvent) obj).onStartApplication(propertyContext);
        }
        if(obj instanceof ClientProtocolAppStateEvents) {
          ((ClientProtocolAppStateEvents) obj).onStartApplication(propertyContext);
        }
      }
    }
  }

  /**
   * Extracts the provider fields from all features.
   * @param features
   * @param providers
   */
  private void extractProviders(FeatureType[] features, HashSet providers) {
    if (features != null) {
      for (int i=0; i<features.length; i++) {
        if (features[i].getProvider() != null) {
          providers.add(features[i].getProvider());
        }
      }
    }
  }

  /**
   * Returns used protocol providers for every logical port in a HashSet.
   * @return
   */
  public Hashtable getUsedProtocols() {
    Hashtable result = new Hashtable();
    LogicalPortType[] logicalPorts = this.logicalPorts.getLogicalPort();
    for (int i=0; i<logicalPorts.length; i++) {
      HashSet protocols = new HashSet();
      LogicalPortType port  = logicalPorts[i];
      GlobalFeatures global = port.getGlobalFeatures();
      if  (global != null) {
        FeatureType[] features = global.getFeature();
        extractProviders(features,protocols);
      }
      LocalFeatures local = port.getLocalFeatures();
      if (local != null) {
        OperationType[] operations = local.getOperation();
        for (int j=0; j>operations.length; j++) {
          FeatureType[] features = operations[j].getFeature();
          extractProviders(features,protocols);
        }
      }
      result.put(port.getName(),protocols);
    }
    return result;
  }

  /**
   * Returns the webservice client application data for different logical ports.
   * @param runtimeInformation
   * @return
   */
  public Hashtable getConfigurationData(RuntimeInformation runtimeInformation) {
    Hashtable result = new Hashtable();
    initRuntimeInformation(runtimeInformation);
    LogicalPortType[] logicalPorts = this.logicalPorts.getLogicalPort();
    for (int i=0; i<logicalPorts.length; i++) {
      PropertyContext propertyContext = new PropertyContext();
      LogicalPortType logicalPort = logicalPorts[i];
      RuntimeInformation runtimeInfo = (RuntimeInformation) lportRuntimeInfo.get(logicalPort.getName());
      GlobalFeatures globalFeatures = logicalPort.getGlobalFeatures();
      if (globalFeatures != null) {
        FeatureType[] features = globalFeatures.getFeature();
        for (int j=0; j<features.length; j++) {
          loadProperties(features[j].getProperty(),propertyContext.getSubContext(features[j].getName()));
        }
      }
      propertyContext.setProperty(ClientProtocolStartAppEvent.RUNTIMEINFO,runtimeInfo);
      PropertyContext operationsContext = propertyContext.getSubContext(AbstractProtocol.OPERATION_SUBCONTEXT);
      LocalFeatures localFeatures = logicalPort.getLocalFeatures();
      if (localFeatures != null) {
        OperationType[] operations = localFeatures.getOperation();
        for (int j=0; j<operations.length; j++) {
          PropertyContext opContext = operationsContext.getSubContext(operations[j].getName());
          FeatureType features[] = operations[j].getFeature();
          for (int k=0; k<features.length; k++) {
            loadProperties(features[k].getProperty(),opContext.getSubContext(features[k].getName()));
          }
        }
      }
      result.put(logicalPort.getName(),propertyContext);
    }
    return result;
  }

  public void onStopApplication(RuntimeInformation runtimeInformation) {
    initRuntimeInformation(runtimeInformation);
    Hashtable perm = new Hashtable();
    LogicalPortType[] logicalPorts = this.logicalPorts.getLogicalPort();
    HashSet usedProtocols = new HashSet();
    PropertyContext propertyContext = new PropertyContext();
    for (int i=0; i<logicalPorts.length; i++) {
      perm.clear();
      usedProtocols.clear();
      propertyContext.clear();
      LogicalPortType logicalPort = logicalPorts[i];
      ClientFeatureProvider[] protocols = (ClientFeatureProvider[]) lportProtocols.get(logicalPort.getName());
      for (int j=0; j<protocols.length; j++) {
        perm.put(((AbstractProtocol)protocols[j]).getName(),protocols[j]);
      }
      RuntimeInformation runtimeInfo = (RuntimeInformation) lportRuntimeInfo.get(logicalPort.getName());
      GlobalFeatures globalFeatures = logicalPort.getGlobalFeatures();
      processFeatures(globalFeatures.getFeature(),propertyContext,usedProtocols,perm);
      propertyContext.setProperty(ClientProtocolStartAppEvent.RUNTIMEINFO,runtimeInfo);
      Iterator it = usedProtocols.iterator();
      while (it.hasNext()) {
        Object obj = it.next();
        if (obj instanceof ClientProtocolAppStateEvents) {
          ((ClientProtocolAppStateEvents)obj).onStopApplication(propertyContext);
        }
      }
    }
  }

  public void onDeployApplication(RuntimeInformation runtimeInformation) {
    initRuntimeInformation(runtimeInformation);
    Hashtable perm = new Hashtable();
    LogicalPortType[] logicalPorts = this.logicalPorts.getLogicalPort();
    HashSet usedProtocols = new HashSet();
    PropertyContext propertyContext = new PropertyContext();
    for (int i=0; i<logicalPorts.length; i++) {
      perm.clear();
      usedProtocols.clear();
      propertyContext.clear();
      LogicalPortType logicalPort = logicalPorts[i];
      ClientFeatureProvider[] protocols = (ClientFeatureProvider[]) lportProtocols.get(logicalPort.getName());
      for (int j=0; j<protocols.length; j++) {
        perm.put(((AbstractProtocol)protocols[j]).getName(),protocols[j]);
      }
      RuntimeInformation runtimeInfo = (RuntimeInformation) lportRuntimeInfo.get(logicalPort.getName());
      GlobalFeatures globalFeatures = logicalPort.getGlobalFeatures();
      processFeatures(globalFeatures.getFeature(),propertyContext,usedProtocols,perm);
      propertyContext.setProperty(ClientProtocolStartAppEvent.RUNTIMEINFO,runtimeInfo);
      Iterator it = usedProtocols.iterator();
      while (it.hasNext()) {
        Object obj = it.next();
        if (obj instanceof ClientProtocolDeployAppEvents) {
          ((ClientProtocolDeployAppEvents)obj).onDeployApplication(propertyContext);
        }
      }
    }
  }

  public void loadProperties(PropertyType[] properties, PropertyContext context) {
    for (int i=0; i<properties.length; i++) {
      if (properties[i].getName() != null) {
        if (properties[i].getValue() != null) {
          context.setProperty(properties[i].getName(),properties[i].getValue());
        }
        if (properties[i].getProperty().length != 0) {
          loadProperties(properties[i].getProperty(),context.getSubContext(properties[i].getName()));
        }
      }
    }
  }

  /**
   * Returns client proxy for this webservice client.
   * @param portName
   * @param sdiInterface
   * @return
   * @throws ServiceException
   */
  public synchronized Remote getPort(QName portName, Class sdiInterface) throws ServiceException {
    if (!lportRuntimeInfo.containsKey(portName.getLocalPart())) {
      // check if this port exists
      throw new WebserviceClientException(WebserviceClientException.UNAVAILABLE_PORT,portName.getLocalPart(),this.getClass().getName());
    }
    Object stub = null;
    LogicalPortType[] lports = logicalPorts.getLogicalPort();
    for (int i=0; i<lports.length; i++) {
      if (lports[i].getName().equals(portName.getLocalPart())) {
        LogicalPortType lport = lports[i];
        if (lport.getValid()==false) {
          // this port is not valid
          throw new WebserviceClientException(WebserviceClientException.INVALID_PORT,lport.getName(),this.getClass().getName());
        }
        RuntimeInformation rinfo = (RuntimeInformation) lportRuntimeInfo.get(lport.getName());
        try {
          stub = factory.getConfiguredStub(lport,rinfo,componentFactory,applicationLoader,this._typeRegistry);
        } catch (Exception e) {
          throw new WebserviceClientException(WebserviceClientException.STUB_INSTANTIATION_ERR,e);
        }
      }
    }
    if (stub == null) {
      throw new WebserviceClientException(WebserviceClientException.UNAVAILABLE_PORT,portName.getLocalPart(),this.getClass().getName());
    }
    BaseGeneratedStub baseGenStub = (BaseGeneratedStub) stub;
    if (proxyResolver != null) {
      baseGenStub._setHTTPProxyResolver(proxyResolver);
    }
    if (logLocation != null) {
      baseGenStub._setLogLocation(Location.getLocation(logLocation));
    }
    return (Remote) stub;
  }

  /**
   * Server Managed Port Access.
   * @param aClass
   * @return
   * @throws ServiceException
   */
  public synchronized Remote getLogicalPort(Class aClass) throws ServiceException {
    // Selects default locgal port
    LogicalPortType[] lports = logicalPorts.getLogicalPort();
    if (lports.length == 0) {
      throw new WebserviceClientException(WebserviceClientException.NO_PORT_AVAILABLE,this.getClass().getName());
    }
    int defaultIndex = 0;
    for (int i=0; i<lports.length; i++) {
      if (lports[i].getDefault()) {
        defaultIndex = i;
      }
    }
    LogicalPortType lport = lports[defaultIndex];
    Object stub = null;
    if (lport.getValid()==false) {
      throw new WebserviceClientException(WebserviceClientException.INVALID_PORT,lport.getName(),this.getClass().getName());
    }
    RuntimeInformation rinfo = (RuntimeInformation) lportRuntimeInfo.get(lport.getName());
    if (lport.getSystemName() != null && lport.getSLDWS()!= null && lport.getSLDWSPort()!= null && getSLDConnection()!= null) {
      // SLD Logical Port
      SLDPort port = null;
      try {
        port = sldConnection.getFromSLD(lport.getSystemName(),lport.getSLDWS(),lport.getSLDWSPort());
      } catch (Exception e) {
        throw new WebserviceClientException(WebserviceClientException.SLD_CONNECTION_FAIL,e,lport.getName(),this.getClass().getName(),lport.getSystemName(),lport.getSLDWS(),lport.getSLDWSPort());
      }
      if (port == null) {
        throw new WebserviceClientException(WebserviceClientException.SLD_MISSING_ENTRY,lport.getName(),this.getClass().getName(),lport.getSystemName(),lport.getSLDWS(),lport.getSLDWSPort());
      }
      WSDLDefinitions definitions = null;
      try {
        definitions = port.getAsDefinitions(proxyResolver);
      } catch (Exception e) {
        throw new WebserviceClientException(WebserviceClientException.SLD_WSDL_DOWNLOAD_ERR,port.getWsdl(),lport.getName(),this.getClass().getName());
      }
      try {
        QName wsdlPortName = QName.valueOf(port.getPortName());
        LogicalPortFactory.updateLogicalPort(lport,definitions,wsdlPortName,componentFactory);
      } catch (LogicalPortException e) {
        throw new WebserviceClientException(WebserviceClientException.SLD_UPDATE_ERR,e,lport.getName(),this.getClass().getName());
      }
      rinfo.setEndpoint(lport.getEndpoint());
    }
    try {
      stub = factory.getConfiguredStub(lport,rinfo,componentFactory,applicationLoader,this._typeRegistry);
    } catch (Exception e) {
      throw new WebserviceClientException(WebserviceClientException.STUB_INSTANTIATION_ERR,e);
    }
    if (stub == null) {
      throw new WebserviceClientException(WebserviceClientException.UNAVAILABLE_PORT,lport.getName(),this.getClass().getName());
    }
    BaseGeneratedStub baseGenStub = (BaseGeneratedStub) stub;
    if (proxyResolver != null) {
      baseGenStub._setHTTPProxyResolver(proxyResolver);
    }
    if (logLocation != null) {
      baseGenStub._setLogLocation(Location.getLocation(logLocation));
    }
    return (Remote) stub;
  }
  
  private LogicalPortType getLP(String lpName) throws WebserviceClientException {
    LogicalPortType[] lports = logicalPorts.getLogicalPort();
    for (int i = 0; i < lports.length; i++) {
      if (lpName.equals(lports[i].getName())) {
        return lports[i];
      }
    }
    throw new WebserviceClientException("webservices_4101", lpName);
  }
  
  public LogicalPortType updateLPTypeFromSLD(String lpName, SLDLogicalPort sldLP) throws WebserviceClientException {
    LogicalPortType lport = getLP(lpName);
    SLDPort port = null;
    try {
      port = sldConnection.getFromSLD(sldLP.getSystemName(), sldLP.getWSName(), sldLP.getWSPortName());
    } catch (Exception e) {
      throw new WebserviceClientException(WebserviceClientException.SLD_CONNECTION_FAIL, e, lpName, this.getClass().getName(), sldLP.getSystemName(), sldLP.getWSName(), sldLP.getWSPortName());
    }
    if (port == null) {
      throw new WebserviceClientException(WebserviceClientException.SLD_MISSING_ENTRY, lpName, this.getClass().getName(), sldLP.getSystemName(), sldLP.getWSName(), sldLP.getWSPortName());
    }
    WSDLDefinitions definitions = null;
    try {
      definitions = port.getAsDefinitions(proxyResolver);
    } catch (Exception e) {
      throw new WebserviceClientException(WebserviceClientException.SLD_WSDL_DOWNLOAD_ERR,port.getWsdl(),lport.getName(),this.getClass().getName());
    }
    try {
      QName wsdlPortName = QName.valueOf(port.getPortName());
      LogicalPortFactory.updateLogicalPort(lport, definitions, wsdlPortName, componentFactory);
    } catch (LogicalPortException e) {
      throw new WebserviceClientException(WebserviceClientException.SLD_UPDATE_ERR, e, lport.getName(), this.getClass().getName());
    }
    RuntimeInformation rinfo = (RuntimeInformation) lportRuntimeInfo.get(lpName);
    rinfo.setEndpoint(lport.getEndpoint());
    
    return lport;
  }

  public void changeEndpointURL(String lpName, String newURL) throws WebserviceClientException {
    LogicalPortType lport = getLP(lpName);
    lport.setEndpoint(newURL);
  }

  public LogicalPortType[] getLogicalPorts() {
    return this.logicalPorts.getLogicalPort();
  }
}
