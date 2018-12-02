/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.interfaces.webservices.server.wsclient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import com.sap.engine.boot.SystemProperties;
import com.sap.engine.interfaces.webservices.server.WebServicesContainerManipulator;

/**
 * Title: ServiceOjbectFactory  
 * Description: ServiceObjectFactory
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class ServiceObjectFactory implements ObjectFactory {
  
  private static String WSCLIENTS_PROXIES_CONTEXT = "wsclients/proxies";
   
  public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception {   	  
    Reference referenceObj = (Reference)obj;
    ServiceRefAddr serviceRefAddr = (ServiceRefAddr)referenceObj.get(0);
    
    if(environment == null) {  	
  	  environment = new Properties(); 
  	}
    
    if(SystemProperties.getProperty("server") == null) {
      environment.put("appclient", "true");
    } else {
      environment.put("domain", "true");      
    }   
    
    InitialContext ctx = new InitialContext(environment);
    Object serviceInstance = null; 
    try {   
      serviceInstance = ctx.lookup(WSCLIENTS_PROXIES_CONTEXT + "/" + serviceRefAddr.getApplicationName() + "/" + serviceRefAddr.getModifiedModuleName() + "/" +  serviceRefAddr.getComponentName() + "/" + serviceRefAddr.getServiceRefName());
    } catch(Exception e) {
      // $JL-EXC$  	
    }   
    
    if(serviceInstance != null) {
      return serviceInstance; 
    }
    
    Class siClass = serviceRefAddr.getServiceInterface(); 
    if(siClass != null && SystemProperties.getProperty("server") != null) {    
      WebServicesContainerManipulator wsClientsContainerManipulator = (WebServicesContainerManipulator)ctx.lookup("wsclients/WSClientsManipulator"); 
      serviceInstance = wsClientsContainerManipulator.getServiceImplInstance(serviceRefAddr.getApplicationName(), siClass.getName()); 
    }
    
    if(serviceInstance != null) {
      return serviceInstance; 	
    }
        
    if(serviceRefAddr.getServiceInterface() != null) {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(serviceRefAddr.getServiceInterface().getClassLoader());
      try {
        ThreadLocal sapProviderBridge = obtainSAPProviderThreadLocalBridge(serviceRefAddr.getServiceInterface());
        if (sapProviderBridge != null) {
          String srvRef = serviceRefAddr.getServiceRefName();
          sapProviderBridge.set(srvRef);
        }
        try {
          serviceInstance = serviceRefAddr.getServiceInterface().newInstance();
        } finally {
          if (sapProviderBridge != null) {
            sapProviderBridge.remove();
          }
        }
        if(serviceRefAddr.getServiceEndpointInterface() != null) {
          Method getPortMethod = serviceInstance.getClass().getMethod("getPort", new Class[]{Class.class});
          return getPortMethod.invoke(serviceInstance, new Object[]{serviceRefAddr.getServiceEndpointInterface()});
          //return ((Service)serviceInstance)).getPort(serviceRefAddr.getServiceEndpointInterface());	
        } else {
          setPredefinedHandlerChain(serviceInstance);
          return serviceInstance; 
        }
      } finally {
        Thread.currentThread().setContextClassLoader(cl);        
      }
    }
    
    return null; 
  }
  
  private void setPredefinedHandlerChain(Object o) throws Exception {
    ClassLoader loader = o.getClass().getClassLoader(); //this should be app loader
    Class utilClass = null;
    try {
      utilClass = loader.loadClass("com.sap.engine.services.webservices.jaxws.handlers.PredefinedCTSHandlerResolverUtil");
    } catch (ClassNotFoundException cnfE) {
    //$JL-EXC$
      return;
    }
    Method m = utilClass.getMethod("setHandlerResolverIfNeeded", new Class[]{Object.class});
    m.invoke(null, new Object[]{o});
  }
  
  private ThreadLocal obtainSAPProviderThreadLocalBridge(Class cl) throws Exception {
    ClassLoader loader = cl.getClassLoader(); //this should be app loader
    Class sapProvider = null;
    try {
      sapProvider = loader.loadClass("com.sap.engine.services.webservices.espbase.client.jaxws.cts.CTSProvider");
    } catch (ClassNotFoundException cnfE) {
      //$JL-EXC$
      return null;
    }
    Field f = sapProvider.getField("BRIDGE");
    return (ThreadLocal) f.get(null);
  }
}
