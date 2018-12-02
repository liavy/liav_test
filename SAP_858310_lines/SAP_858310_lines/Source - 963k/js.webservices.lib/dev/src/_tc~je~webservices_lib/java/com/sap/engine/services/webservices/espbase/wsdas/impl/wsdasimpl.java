package com.sap.engine.services.webservices.espbase.wsdas.impl;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.dynamic.DGenericService;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterface;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterfaceInvoker;
import com.sap.engine.services.webservices.espbase.client.dynamic.GenericServiceFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.ServiceFactoryConfig;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.impl.ObjectFactoryImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.wsdas.OperationConfig;
import com.sap.engine.services.webservices.espbase.wsdas.WSDAS;
import commonj.sdo.helper.HelperContext;

public class WSDASImpl implements WSDAS {
  private DGenericService service = null;
  private DInterface interf = null;
  private DInterfaceInvoker invoker = null; 
  private HelperContext helperContext = null; 
     
  public WSDASImpl(GenericServiceFactory gsFactory, String appName, String serviceRefID, HelperContext hContext) throws Exception {
    HashMap<String, Object> options = new HashMap<String, Object>();
    helperContext = hContext;
    options.put(ServiceFactoryConfig.HELPER_CONTEXT, helperContext);
    service = gsFactory.createService(appName, serviceRefID, options);
    interf = service.getInterfaceMetadata();
    invoker = interf.getInterfaceInvoker();
  }
  
  public WSDASImpl(GenericServiceFactory gsFactory, String destName, QName intQName, HelperContext hContext) throws Exception {
    HashMap<String, Object> options = new HashMap<String, Object>();
    helperContext = hContext;
    options.put(ServiceFactoryConfig.HELPER_CONTEXT, helperContext);
    service = gsFactory.createService(destName, intQName, options);
    interf = service.getInterfaceMetadata(intQName);
    invoker = interf.getInterfaceInvoker(destName);
  }
  
  public WSDASImpl(GenericServiceFactory gsFactory, String wsdl, QName intQName, QName portName, HelperContext hContext, Map properties) throws Exception {
    helperContext = hContext;
    ServiceFactoryConfig config = new ServiceFactoryConfig();
    config.put(ServiceFactoryConfig.HELPER_CONTEXT, helperContext);
    
    if (properties != null && properties.size() > 0) {
      String property = (String) properties.get(Stub.USERNAME_PROPERTY);
      
      if (property != null && !property.equals("")) {
        config.setUser(property);
      }
      
      property = (String) properties.get(Stub.PASSWORD_PROPERTY);
      
      if (property != null && !property.equals("")) {
        config.setPassword(property);
      } 
    }        
    
    service = gsFactory.createService(wsdl, config);
    interf = service.getInterfaceMetadata(intQName);
    invoker = interf.getInterfaceInvoker(portName);
    
    if (properties != null && properties.size() > 0) {
      String property = (String) properties.get(Stub.USERNAME_PROPERTY);
      
      if (property != null && !property.equals("")) {
        invoker.setProperty(Stub.USERNAME_PROPERTY, property);
      }
      
      property = (String) properties.get(Stub.PASSWORD_PROPERTY);
      
      if (property != null && !property.equals("")) {
        invoker.setProperty(Stub.PASSWORD_PROPERTY, property);
      }
      
      property = (String) properties.get(Stub.ENDPOINT_ADDRESS_PROPERTY);
      
      if (property != null && !property.equals("")) {
        invoker.setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, property);
      }      
    }    
  }
  
  public OperationConfig getOperationCfg(String opName) {
    return new OperationConfigImpl(opName, interf.getOperation(opName), invoker.getParametersConfiguration(opName));    
  }

  public void invokeOperation(OperationConfig opCfg) throws RemoteException, InvocationTargetException {
    OperationConfigImpl config = (OperationConfigImpl) opCfg;
    invoker.invokeOperation(config.getOperationName(), config.getParametersConfiguration(), new ObjectFactoryImpl(service.getTypeMetadata()));
  }
  
  public void invokeOperation(OperationConfig opCfg, Map options) throws RemoteException, InvocationTargetException {
    OperationConfigImpl config = (OperationConfigImpl) opCfg;
    invoker.invokeOperation(config.getOperationName(), config.getParametersConfiguration(), new ObjectFactoryImpl(service.getTypeMetadata()));
    _getConfigurationContext().setProperty(ServiceFactoryConfig.OPTIONS, options);
  }
  
  public HelperContext getHelperContext() {
    return helperContext;
  }

  public String[] getOperationNames() {
    return interf.getOperationNames(); 
  }

  public  ClientConfigurationContext _getConfigurationContext() {
	  if(invoker instanceof DInterfaceInvokerImpl){
		  return ((DInterfaceInvokerImpl)invoker)._getConfigurationContext();
	  } else {
		  return null;
	  }
  }

}
