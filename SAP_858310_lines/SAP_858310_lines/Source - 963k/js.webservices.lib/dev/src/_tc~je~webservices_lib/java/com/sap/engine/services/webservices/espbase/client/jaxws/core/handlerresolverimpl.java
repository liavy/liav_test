package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.jws.HandlerChain;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientServiceContext;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.metadata.InterfaceMetadata;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.jaxws.handlers.JAXWSHandlersEngine;
import com.sap.engine.services.webservices.jaxws.handlers.JAXWSHandlersException;

public class HandlerResolverImpl implements HandlerResolver {
  private Class sClass = null; 
  private ClientServiceContext context = null;
  private Hashtable<String, List<Class>> seiToHandlerClasses  = null;
  private List<Class> serviceHandlerChain = null;
  
  public HandlerResolverImpl(Class serviceClass, Hashtable<Class, InterfaceMetadata> seiToInterfaceMetadata, ClientServiceContextImpl clientServiceCtx) {
    sClass = serviceClass;
    context = clientServiceCtx;
    initServiceHandlers();
    initPortHandlers(seiToInterfaceMetadata);    
  }   
  
  public List getHandlerChain(PortInfo pInfo) {
    QName portName = pInfo.getPortName();
    BindingData bindingData = context.getServiceData().getBindingData(portName);
    
    if (bindingData == null) { // Dynamically defined port
      return new ArrayList<Handler>();
    }
    InterfaceMapping interfaceMap = null;
    if (pInfo instanceof PortInfoImpl) {
      PortInfoImpl portInfo = (PortInfoImpl) pInfo;
      String interfaceMappingId = portInfo.getInterfacemappingId();
      interfaceMap = context.getMappingRules().getInterface(interfaceMappingId);      
    } else {
    QName bindingQName = new QName(bindingData.getBindingNamespace(), bindingData.getBindingName());
      interfaceMap = SAPServiceDelegate.getInterfaceMapping(bindingQName, context);               
    }
    if (interfaceMap == null) {
      return new ArrayList<Handler>();
    }
    String seiName = interfaceMap.getSEIName();
    List<Class> handlerClasses = seiToHandlerClasses.get(seiName);
    List<Handler> handlers = null; 
    
    try {
      handlers = JAXWSHandlersEngine.initializedHandlerChain(handlerClasses);
    } catch (JAXWSHandlersException jaxwshe) {
      throw new WebServiceException(jaxwshe);
    }
    
    return handlers; 
  }
  
  private void initServiceHandlers() {
    serviceHandlerChain = obtainHandlerChainClassesFromClass(sClass);    
  }
  
  private void initPortHandlers(Hashtable<Class, InterfaceMetadata> seiToInterfaceMetadata) {
    seiToHandlerClasses = new Hashtable<String, List<Class>>();        
    Enumeration<Class> keyClasses = seiToInterfaceMetadata.keys();
    Class seiClass = null;    
    List<Class> handlerClasses = null;
    
    while (keyClasses.hasMoreElements()) {
      seiClass = keyClasses.nextElement();
      handlerClasses = JAXWSHandlersEngine.mergeServiceAndPortHandlerChains(serviceHandlerChain, obtainHandlerChainClassesFromClass(seiClass));
      
      if (handlerClasses != null) {
        seiToHandlerClasses.put(seiClass.getName(), handlerClasses);
      }
    }     
  }
  
  public void appendSEI(Class seiClass) {
    seiToHandlerClasses.put(seiClass.getName(), obtainHandlerChainClassesFromClass(seiClass));
  }
  
  private InputStream getHandlerResourceFromClass(Class annotatedClass, String filePath) {
    String packageName = annotatedClass.getPackage().getName();
    String fileClassPath = packageName.replace('.','/')+"/"+filePath;
    InputStream result = annotatedClass.getClassLoader().getResourceAsStream(fileClassPath);
    return result;
  }
  
  private List<Class> obtainHandlerChainClassesFromClass(Class annotatedClass) {
    List<Class> handlerClasses = new ArrayList<Class>();    
    HandlerChain handlerAnnotation = (HandlerChain) annotatedClass.getAnnotation(HandlerChain.class);    
    if (handlerAnnotation == null) {
      return handlerClasses; // no handler chain defined via annotation
    }
        
    String handlerFilePath = handlerAnnotation.file();
    
    if (handlerFilePath == null || handlerFilePath.equals("")) {
      return handlerClasses; // no handler chain file specified
    }
    InputStream inStream = null;
    inStream = getHandlerResourceFromClass(annotatedClass,handlerFilePath);
    if (inStream == null) {
      File handlerFile = new File(handlerFilePath);
      
      if (!handlerFile.isFile() || !handlerFile.canRead()) {
        return handlerClasses; // the specified file does not exist or cannot be read
      }
      try {
        inStream = new FileInputStream(handlerFile);
      } catch (Exception x) {
        x.printStackTrace();
        return handlerClasses;
      }
    }        
    try {      
      handlerClasses = JAXWSHandlersEngine.getHandlersClassesFromHandlerChains(inStream, annotatedClass.getClassLoader());            
    } catch (JAXWSHandlersException jaxwshe) {
      throw new WebServiceException(jaxwshe);     
    } finally {
      if (inStream != null) {
        try {
          inStream.close();
        } catch (IOException ie) {
          // nothing to do
          ie.printStackTrace();
        }
      }        
    }
    
    return handlerClasses; 
  }
  
}