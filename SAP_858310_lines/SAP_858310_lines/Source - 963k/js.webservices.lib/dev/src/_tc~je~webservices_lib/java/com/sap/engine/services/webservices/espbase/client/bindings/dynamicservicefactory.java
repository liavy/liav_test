/*
 * Created on 2005-4-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;

import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorConfigNew;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorNew;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DynamicServiceFactory extends ServiceFactory {
  
  public Service createService(URL wsdlURL, QName serviceName) throws ServiceException {
    try {
      ProxyGeneratorConfigNew proxyGeneratorConfig = new ProxyGeneratorConfigNew();
      proxyGeneratorConfig.setGenerationMode(ProxyGeneratorConfigNew.LOAD_MODE);
      proxyGeneratorConfig.setWsdlPath(wsdlURL.toExternalForm());
      ProxyGeneratorNew proxyGenerator = new ProxyGeneratorNew();
      proxyGenerator.generateAll(proxyGeneratorConfig);
      DynamicServiceImpl dynamicService = new DynamicServiceImpl();
      dynamicService.init(serviceName, new TypeMappingRegistryImpl(), proxyGeneratorConfig.getMappingRules(), proxyGeneratorConfig.getProxyConfig(), getClass().getClassLoader());
      dynamicService.setServiceMode(DynamicServiceImpl.JAXRPC_MODE);
      return(dynamicService);
    } catch(Exception exc) {
      throw new ServiceException(exc); 
    }
  }

  public Service createService(QName serviceName) throws ServiceException {
    return(null);
  }
  /* (non-Javadoc)
   * @see javax.xml.rpc.ServiceFactory#loadService(java.lang.Class)
   */
  public Service loadService(Class arg0) throws ServiceException {
    // TODO Auto-generated method stub
    return null;
  }
  /* (non-Javadoc)
   * @see javax.xml.rpc.ServiceFactory#loadService(java.net.URL, java.lang.Class, java.util.Properties)
   */
  public Service loadService(URL arg0, Class arg1, Properties arg2) throws ServiceException {
    // TODO Auto-generated method stub
    return null;
  }
  /* (non-Javadoc)
   * @see javax.xml.rpc.ServiceFactory#loadService(java.net.URL, javax.xml.namespace.QName, java.util.Properties)
   */
  public Service loadService(URL arg0, QName arg1, Properties arg2) throws ServiceException {
    // TODO Auto-generated method stub
    return null;
  }
}
