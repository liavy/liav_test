package com.sap.engine.services.webservices.espbase.wsdas.impl;

import java.util.Map;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.client.dynamic.GenericServiceFactory;
import com.sap.engine.services.webservices.espbase.wsdas.WSDAS;
import com.sap.engine.services.webservices.espbase.wsdas.WSDASFactory;
import commonj.sdo.helper.HelperContext;

public class WSDASFactoryImpl extends WSDASFactory {
  
  private GenericServiceFactory genServiceFactory = null; 
  
  public WSDASFactoryImpl() {
    genServiceFactory = GenericServiceFactory.newInstance(false); // shall we use internal cache or not?
  }
  
  public WSDASFactoryImpl(boolean cache){
    genServiceFactory = GenericServiceFactory.newInstance(cache); 
  }
  
  @Override
  public WSDAS createWSDAS(String logicDestName, QName intfQName, HelperContext sdoContext) throws Exception {
    if (sdoContext == null) {
      throw new IllegalArgumentException("Helper Context cannot be NULL");
    }
    
    return new WSDASImpl(genServiceFactory, logicDestName, intfQName, sdoContext);
  }
  
  @Override
  public WSDAS createWSDAS(String appName, String serviceRefID, HelperContext sdoContext) throws Exception {    
    if (sdoContext == null) {
      throw new IllegalArgumentException("Helper Context cannot be NULL");
    }
    
    return new WSDASImpl(genServiceFactory, appName, serviceRefID, sdoContext);
  }
  
  public WSDAS createWSDAS(String wsdl, QName intfQName, QName portName, HelperContext sdoContext, Map properties) throws Exception {
    if (sdoContext == null) {
      throw new IllegalArgumentException("Helper Context cannot be NULL");
    }
    
    return new WSDASImpl(genServiceFactory, wsdl, intfQName, portName, sdoContext, properties);
  }
  
  @Override
  public void clearCache(){
    genServiceFactory.stop();
  }

  @Override
  public void purgeServiceFromCache(String appName, String serviceRefID){
    genServiceFactory.purgeServiceFromCache(appName, serviceRefID);
  }
  
  @Override
  public void purgeServiceFromCache(String logicDestName, QName intfQName){
    genServiceFactory.purgeServiceFromCache(logicDestName, intfQName);
  }
  
  public void purgeServiceFromCache(String wsdlUrl){
    genServiceFactory.purgeServiceFromCache(wsdlUrl);
  }
  
  public void setCacheCapacity(int capacity){
   genServiceFactory.setInternalCacheCapacity(capacity); 
  }
}  
