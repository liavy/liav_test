/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.server.dynamic;

import java.io.File;

import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.interfaces.webservices.runtime.component.ClientComponentFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.J2EEEngineHelper;
import com.sap.engine.services.webservices.espbase.client.dynamic.ServiceFactoryConfig;
import com.sap.engine.services.webservices.espbase.discovery.ServiceDiscovery;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.tools.WSDLDownloadResolver;
import com.sap.tc.logging.Location;

/**
 * 
 * @version 1.0 (2006-1-11)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class J2EEEngineHelperImpl implements J2EEEngineHelper {

  ServiceDiscovery serviceDiscovery;
  private String absolutePath = null;
  private String jarPath = null; 
  private boolean isInitialized = false;
  
  /**
   * Needs to be synchronized, since it is not known by how many threads the init would be used.
   *
   */
  private synchronized void init() {
    if (isInitialized) {
      return;
    }
    jarPath = WebServicesDeployManager.getJarsPath(WSContainer.getServiceContext());
    File tempDir = new File(WSContainer.getServiceContext().getServiceState().getWorkingDirectoryName(), "dyn");    
      tempDir.mkdirs();
    absolutePath = tempDir.getAbsolutePath();
    isInitialized = true;
  }
  
  /**
   * @return
   */
  public ClientComponentFactory getClientComponentFactory() {
    // TODO Auto-generated method stub
    return null;
  }
  /**
   * @return
   */
  public ServiceDiscovery getServiceDiscovery() {
    return serviceDiscovery.newInstance();
  }
  
  public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
	this.serviceDiscovery = serviceDiscovery;
  }  
  
  /**
   * @return
   */
  public ServiceFactoryConfig getServiceFactoryConfig() {
    init();
    WSDLDownloadResolver wsdlResolver = new WSDLDownloadResolver();
    HTTPProxyResolver proxyResolver = WSContainer.getHTTPProxyResolver();
    wsdlResolver.setHTTPProxyResolver(proxyResolver);
    
    ServiceFactoryConfig config = new ServiceFactoryConfig();
    config.setAdditionalClassPath(jarPath);        
    config.setTemporaryDir(absolutePath);    
    config.put(ServiceFactoryConfig.ENTITY_RESOLVER, wsdlResolver);
    config.put(ServiceFactoryConfig.HTTP_PROXY_RESOLVER, proxyResolver);
    try {
      config.put(ServiceFactoryConfig.CONFIGURATION_FACTORY, WSContainer.createInitializedServerCFGFactory());
    } catch (Exception e) {
      //for now ingore this exception since it could break backwards compatibility. Also it shouldn't happen to reach to here.
      Location.getLocation(J2EEEngineHelperImpl.class).catching("", e);
    }
    //config.put(ServiceFactoryConfig.HTTP_PROXY_RESOLVER,WSContainer.getHTTPProxyResolver());
    return config;
  }
}
