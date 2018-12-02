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

package com.sap.engine.services.webservices.server.container;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ConfigurationContext;
import com.sap.engine.services.webservices.server.container.mapping.MappingContext;

/**
 * Title: BaseServiceContext 
 * Description: BaseServiceContext
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class BaseServiceContext {

  private ConfigurationContext configurationContext;  
  private MappingContext mappingContext; 
   
  public BaseServiceContext() {   
	  
  }
  
  /**
   * @return ConfigurationContext
   */
  public synchronized ConfigurationContext getConfigurationContext() {
    if(configurationContext == null) {
      configurationContext = new ConfigurationContext();
    }
    
    return configurationContext;
  }
  
  /**
   * @return MappingContext
   */
  public synchronized MappingContext getMappingContext() {
    if(mappingContext == null) {
      mappingContext = new MappingContext();
    }
    
    return mappingContext;
  } 
  
  
  public static String constructBindingDataURLPath(String contextRoot, String bindingDataURL) {    
    if(!contextRoot.equals("") && !contextRoot.startsWith("/")) {
      contextRoot = "/" + contextRoot; 	
    }
    
    if(!bindingDataURL.equals("") && !bindingDataURL.startsWith("/")) {
      bindingDataURL = "/" + bindingDataURL; 	
    }
    
    return contextRoot + bindingDataURL; 
  }
  
  public InterfaceDefinition getInterfaceDefinitionForBindingData(String bindingDataURL) {
    BindingData bindingData = getBindingData(bindingDataURL);
    if(bindingData == null) {
      //TODO - throw exception as alternative
      return null; 
    }  
      
    return getConfigurationContext().getInterfaceDefinitionRegistry().getInterfaceDefinition(bindingData.getInterfaceId().trim());        
  }
  
  public BindingData getBindingData(String bindingDataURL) {
    BindingData bindingData = getConfigurationContext().getGlobalBindingDataRegistry().getBindingData(bindingDataURL); 
	try {
      if(bindingData == null) {	  
	    bindingData = getConfigurationContext().getGlobalBindingDataRegistry().getBindingData(URLDecoder.decode(bindingDataURL, "UTF-8"));
      }
	  if(bindingData == null) {
	    bindingData = getConfigurationContext().getGlobalBindingDataRegistry().getBindingData(encodeURL(bindingDataURL)); 	  
	  }      
	} catch(UnsupportedEncodingException e) {
      // $JL-EXC$	
	}
	
    return bindingData; 
  }
  
  public ServiceMapping getServiceMapping(String serviceMappingId) {
    return this.getMappingContext().getServiceMappingRegistry().getServiceMapping(serviceMappingId);	  	  
  }
  
  public Service getService(String serviceURL) throws UnsupportedEncodingException {
    Service service = getConfigurationContext().getGlobalServiceRegistry().getService(serviceURL);
    
    if(service == null) {
      service = getConfigurationContext().getGlobalServiceRegistry().getService(URLDecoder.decode(serviceURL, "UTF-8")); 	
    }
    if(service == null) {
      service = getConfigurationContext().getGlobalServiceRegistry().getService(encodeURL(serviceURL));	
    }
    
    return service; 
  }
  
  public Service[] getServicesForApplication(String applicationName) {
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);
    if(applicationConfigurationContext == null) {
      return new Service[0]; 	
    }
    
    return applicationConfigurationContext.getServiceRegistry().listServices(); 
  }
  
  public static String encodeURL(String url) throws UnsupportedEncodingException {	    
    String decodedURL = URLDecoder.decode(url, "UTF-8");
	
    StringBuffer strBuffer = new StringBuffer(decodedURL.length());
	StringTokenizer tokenizer = new StringTokenizer(decodedURL, "/");	
	if(decodedURL.charAt(0) == '/'){
	  strBuffer.append("/");
	}	
    String token;
	while(tokenizer.hasMoreTokens()) {
	  token = tokenizer.nextToken();
	  strBuffer.append(URLEncoder.encode(token, "UTF-8"));
	  if (tokenizer.hasMoreElements()) {
	    strBuffer.append("/");
	  }
	}
	
	return strBuffer.toString();		
  }
  
}
