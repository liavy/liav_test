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
 
package com.sap.engine.services.webservices.server.deploy.wsclients;

import java.lang.annotation.Annotation;
import java.util.Enumeration;

import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.WsClientsExtType;
import com.sap.engine.services.webservices.server.container.configuration.InterfaceDefinitionRegistry;
import com.sap.engine.services.webservices.server.container.descriptors.ConfigurationDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.mapping.ServiceMappingRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.ServiceRefGroupDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.ServiceRefGroupExtDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsJ2EEEngineExtDescriptorRegistry;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WSClientsAbstractStartProcess
 * Description: WSClientsAbstractStartProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public abstract class WSClientsAbstractStartProcess extends WSClientsAbstractDProcess {
 
  protected ClassLoader appLoader; 
     
  protected void loadWSClientsJ2EEEngineDescriptors() throws WSDeploymentException {            
    super.loadWSClientsJ2EEEngineDescriptors();    
    try {
      //TODO - move jaxws properties during jee5 conversion
      addJAXWSProperties(new PropertyType[]{PropertyType.newInitializedInstance(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getNamespaceURI(), BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME.getLocalPart(), BuiltInConfigurationConstants.SOAPAPPLICATION_CLIENT_JAXWS_VALUE)});   	
    } catch(Exception e) {     
      // $JL-EXC$ 
    }
    loadWSClientsJ2EEEngineExtDescriptors();          
  }
    
  private void loadWSClientsJ2EEEngineExtDescriptors() throws WSDeploymentException {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      return; 
    }
 
    WSClientsJ2EEEngineExtDescriptorRegistry wsClientsJ2EEEngineExtDescriptorRegistry = wsClientsApplicationDescriptorContext.getWsClientsJ2EEEngineExtDescriptorRegistry();      
        
    Enumeration enum1 = wsClientsJ2EEEngineExtDescriptorRegistry.getWsClientsJ2EEEngineExtDescriptors().keys();    
    String moduleName;  
    WsClientsExtType wsClientsJ2EEEngineExtDescriptor;          
    while(enum1.hasMoreElements()) {
      moduleName = (String)enum1.nextElement(); 
      wsClientsJ2EEEngineExtDescriptor = wsClientsJ2EEEngineExtDescriptorRegistry.getWsClientsJ2EEEngineDescriptor(moduleName);   
      loadWSClientsJ2EEEngineExtDescriptors(moduleName, wsClientsJ2EEEngineExtDescriptor);           
    }          
  } 
  
  private void loadWSClientsJ2EEEngineExtDescriptors(String moduleName, WsClientsExtType wsClientsJ2EEEngineExtDescriptor) throws WSDeploymentException {
    ServiceRefGroupDescriptionType[] serviceRefGroupDescriptors = wsClientsJ2EEEngineExtDescriptor.getServiceRefGroupDescription();
    if(serviceRefGroupDescriptors == null || serviceRefGroupDescriptors.length == 0) {
      return; 
    }    
    
    try {    
      loadServiceRefGroupExtDescriptors(serviceRefGroupDescriptors);
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_LOAD_2, new Object[]{applicationName, moduleName}, e);
    }          
  }
  
  private void loadServiceRefGroupExtDescriptors(ServiceRefGroupDescriptionType[] serviceRefGroupExtDescriptors) throws Exception {
    if(serviceRefGroupExtDescriptors == null || serviceRefGroupExtDescriptors.length == 0) {
      return;
    }     
    
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    ServiceRefGroupExtDescriptorRegistry serviceRefGroupExtDescriptorRegistry = wsClientsApplicationDescriptorContext.getServiceRefGroupExtDescriptorRegistry();  
     
    ServiceRefGroupDescriptionType serviceRefGroupDescriptor;        
    for(int i = 0; i < serviceRefGroupExtDescriptors.length; i++) {
      serviceRefGroupDescriptor = serviceRefGroupExtDescriptors[i];      
      serviceRefGroupExtDescriptorRegistry.putServiceRefGroupExtDescriptor(serviceRefGroupDescriptor.getServiceRefGroupName().trim(), serviceRefGroupDescriptor);
    }    
  }
  
  private void addJAXWSProperties(PropertyType[] properties) throws Exception {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext  = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      return;
    }
   
    ServiceRefGroupDescriptorRegistry serviceRefGroupDescriptorRegistry = wsClientsApplicationDescriptorContext.getServiceRefGroupDescriptorRegistry();
    ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsClientsApplicationDescriptorContext.getConfigurationDescriptorRegistry(); 
    
    Enumeration enumer = serviceRefGroupDescriptorRegistry.getServiceRefGroupDescriptors().elements();
    com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ServiceRefGroupDescriptionType serviceRefGroupDescriptor;
    ConfigurationRoot configurationDescriptor; 
    Service[] services; 
    while(enumer.hasMoreElements()) {
      serviceRefGroupDescriptor = (com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ServiceRefGroupDescriptionType)enumer.nextElement();
      configurationDescriptor = configurationDescriptorRegistry.getConfigurationDescriptor(serviceRefGroupDescriptor.getServiceRefGroupName().trim()); 
      services = configurationDescriptor.getRTConfig().getService(); 
      if(containsJAXWSService(services, appLoader)) {
        addJAXWSProperties(services, properties);  
      }
    }    
  }
  
  private void addJAXWSProperties(Service[] services, PropertyType[] properties) {
    if(services == null || services.length == 0) {
      return; 	
    }
    
    InterfaceDefinitionRegistry interfaceDefinitionRegistry = getServiceContext().getConfigurationContext().getInterfaceDefinitionRegistry(); 
    
    BindingData[] bindingDatas;
    InterfaceDefinition interfaceDefinition; 
    for(Service service: services) {
      bindingDatas = service.getServiceData().getBindingData(); 
      if(bindingDatas != null && bindingDatas.length != 0) {
        for(BindingData bindingData: bindingDatas) {
          interfaceDefinition = interfaceDefinitionRegistry.getInterfaceDefinition(bindingData.getInterfaceId().trim()); 
          addProperties(interfaceDefinition.getVariant(), properties);
        }	  
      }      
    }	      
  }
    
  private void addProperties(Variant[] intDefinitionVariants, PropertyType[] properties) {
    if(intDefinitionVariants == null || intDefinitionVariants.length == 0) {
      return; 
    } 	  
    
    for(Variant intDefinitionVariant: intDefinitionVariants) {
      addProperties(intDefinitionVariant, properties);	
    }
  }
  
  private void addProperties(Variant intDefinitionVariant, PropertyType[] properties) {
    if(properties == null || properties.length == 0) {
      return;	
    }    
    
    for(PropertyType property: properties) {
      intDefinitionVariant.getInterfaceData().getSinglePropertyList().addProperty(property); 	
    }       
  } 
  
  private void addProperty(PropertyListType properties, PropertyType property) {
    if(properties.getProperty(property.getNamespace(), property.getName()) == null) {
      properties.addProperty(property); 	
    }	  
  }
  
  protected boolean containsService(int type, Service[] services) {         
    if(services == null || services.length == 0) {
      return false; 
    }
     
    Service service;      
    for(int i = 0; i < services.length; i++) {
      service = services[i];      
      if(service.getType() != null && service.getType().equals(type)) {
        return true;
      }        
    }
     
    return false;         
  }  
  
  protected boolean containsJAXWSService(Service[] services, ClassLoader loader) throws ClassNotFoundException {
    int type = 3; 
    String genericSI = "javax.xml.ws.Service"; 
    String[] annotationClassNames = new String[]{"javax.xml.ws.WebServiceClient"}; 
	
    if(services == null || services.length == 0) {
      return false; 	
    }
    
    ServiceMappingRegistry serviceMappingRegistry = getServiceContext().getMappingContext().getServiceMappingRegistry();
   
    ServiceMapping serviceMapping; 
    Class siClass = null; 
    for(Service service: services) {
      if(service.getType() != null && service.getType().equals(type)) {
        return true; 
      } 
      
      serviceMapping = serviceMappingRegistry.getServiceMapping(service.getServiceMappingId().trim());
      
      if(serviceMapping.getSIName().trim().equals(genericSI)) {
        return true; 
      }
      
      try {
        siClass = loader.loadClass(serviceMapping.getSIName().trim());
      } catch(ClassNotFoundException e) {        	
        // $JL-EXC$
      }
            
      if(siClass != null && containsAnnotation(annotationClassNames, siClass.getAnnotations())) {
        return true; 	  
      }
    }
    
    return false;     
  }  
 
  private boolean containsAnnotation(String[] annotationClassNames, Annotation[] annotations) {
    if(annotationClassNames == null || annotationClassNames.length == 0) {
      return false; 	
    }
        
    for(String annotationClassName: annotationClassNames) {
      if(!containsAnnotation(annotationClassName, annotations)) {
        return false; 	  
      }	
    }
    
    return true; 
  } 
  
  private boolean containsAnnotation(String annotationClassName, Annotation[] annotations) {    
	if(annotations == null || annotations.length == 0) {
      return false; 	
    }	  
    
    for(Annotation annotation: annotations) {
      if(annotation.annotationType().getName().equals(annotationClassName)) {
        return true; 	  
      }
    }
    
    return false;     
  }
  
}
