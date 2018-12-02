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

package com.sap.engine.services.webservices.server.deploy.ws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.namespace.QName;

import com.sap.engine.services.sca.plugins.ws.WebServicePluginConstants;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorConfigNew;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorNew;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinitionCollection;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.ServiceCollection;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.configuration.URLSchemeType;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineAltFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.PortComponentType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.PropertyType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.QNameType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.WebservicesType;
import com.sap.engine.services.webservices.server.deploy.j2ee.ws.J2EE14Convertor;
import com.sap.engine.services.webservices.server.deploy.j2ee.ws.ZipEntryOutputStream;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;

/**
 * Title: WSAltConvertor
 * Description: WSAltConvertor 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSAltConvertor {
	
  private static final String META_INF = "META-INF";	
  public static final String WEBSERVICES_J2EE_ENGINE_ALT_DESCRIPTOR = "webservices-j2ee-engine-alt.xml";
  public static final String GALAXY_IMPL_LINK = "GalaxyImpl";
  public static final String SCA_IMPL_LINK = WebServicePluginConstants.SCA_IMPL_CONTAINER_NAME;
	
  private String tempDir;
  private File archiveFile;
  private String appName;  

  private WSDLLoader wsdlLoader;
  private ProxyGeneratorNew proxyGenerator;
  private ConfigurationMarshallerFactory configurationMarshallerFactory; 
  
  public WSAltConvertor(ConfigurationMarshallerFactory configurationMarshallerFactory) {
    this.wsdlLoader = new WSDLLoader(); 
    this.proxyGenerator = new ProxyGeneratorNew(); 
    this.configurationMarshallerFactory = configurationMarshallerFactory;  
  }
  
  private void init(File archiveFile, String tempDir) {	
	this.archiveFile = archiveFile;
	this.tempDir = tempDir;
	if (tempDir != null ) {
		int index = tempDir.lastIndexOf("/");
		if(index > 0){
			this.appName = tempDir.substring(index+1);
		}
	}
	
  } 
  
  public File convert(File archiveFile, String tempDir) throws Exception {
    init(archiveFile, tempDir);    
   
    JarFile archiveJarFile = null;
    InputStream webServicesJ2EEEngineAltDescriptorIn = null;
    File archiveFileNew = null;             
    OutputStream out = null; 
    JarOutputStream jarOut = null;
    try {
      archiveJarFile = new JarFile(archiveFile);                      
      ZipEntry webServicesJ2EEEngineAltDescriptorEntry = archiveJarFile.getEntry(META_INF + "/" + WEBSERVICES_J2EE_ENGINE_ALT_DESCRIPTOR); 
      if(webServicesJ2EEEngineAltDescriptorEntry != null) {         
        webServicesJ2EEEngineAltDescriptorIn = archiveJarFile.getInputStream(webServicesJ2EEEngineAltDescriptorEntry);            
        WebservicesType webServicesJ2EEEngineAltDescriptor = WebServicesJ2EEEngineAltFactory.load(webServicesJ2EEEngineAltDescriptorIn); 
        ConfigurationRoot configurationDescriptor = new ConfigurationRoot(); 
        MappingRules mappingDescriptor = new MappingRules();
        com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType webServicesJ2EEEngineDescriptor = convertWebServicesJ2EEEngineAltDescriptors(webServicesJ2EEEngineAltDescriptor, archiveFile, configurationDescriptor, mappingDescriptor);         
        
        archiveFileNew = new File(tempDir + "/" + archiveFile.getName());          
        archiveFileNew.getParentFile().mkdirs();
        out = new FileOutputStream(archiveFileNew); 
        jarOut = new JarOutputStream(out);              
        JarUtil.copyEntries(archiveJarFile, new String[0], new String[0], new String[0], new String[0], false, jarOut);
        addWebServicesJ2EEEngineDescriptors(webServicesJ2EEEngineDescriptor, configurationDescriptor, mappingDescriptor, jarOut);        
      }
    } catch(Exception e) {
      //TODO 
      throw e; 
    } finally {          
      try {
        if(webServicesJ2EEEngineAltDescriptorIn != null) {
          webServicesJ2EEEngineAltDescriptorIn.close(); 	
        }	  
      } catch(Exception e) {
    	// $JL-EXC$	   
      }
      try {
        if(archiveJarFile != null) {
          archiveJarFile.close(); 	 
        } 
      } catch(Exception e) {
        // $JL-EXC$	   
      }            
      try {
        if(jarOut != null) {
          jarOut.close(); 	 
        } 
      } catch(Exception e) {
        // $JL-EXC$	   
      }
      try {
        if(out != null) {
          out.close(); 	 
        } 
      } catch(Exception e) {
        // $JL-EXC$	   
      }
    } 
    
    return archiveFileNew;
  }  
  
  private com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType convertWebServicesJ2EEEngineAltDescriptors(WebservicesType webServicesJ2EEEngineAltDescriptor, File archiveFile, ConfigurationRoot configurationDescriptor, MappingRules mappingDescriptor) throws WSDLException, ProxyGeneratorException {       
    WebserviceDescriptionType[] serviceAltDescriptors = webServicesJ2EEEngineAltDescriptor.getWebserviceDescription(); 
    if(serviceAltDescriptors == null || serviceAltDescriptors.length == 0) {
      return new com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType();  	
    }
        
    ArrayList<ConfigurationRoot> configurationDescriptors = new ArrayList<ConfigurationRoot>();
    ArrayList<MappingRules> mappingDescriptors = new ArrayList<MappingRules>();        
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType[] serviceDescriptors = convertServiceAltDescriptors(webServicesJ2EEEngineAltDescriptor.getWebserviceDescription(), archiveFile, configurationDescriptors, mappingDescriptors);    
        
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType webServicesJ2EEEngineDescriptor = new com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType();
    webServicesJ2EEEngineDescriptor.setWebserviceDescription(serviceDescriptors); 
    webServicesJ2EEEngineDescriptor.setConfigurationFile(META_INF + "/configurations.xml");
    webServicesJ2EEEngineDescriptor.setWsdlMappingFile(META_INF + "/mappings.xml");
    mergeConfigurationDescriptors(configurationDescriptors, configurationDescriptor);
    mergeMappingDescriptors(mappingDescriptors, mappingDescriptor);  
    
    return webServicesJ2EEEngineDescriptor; 
  }  
  
  private com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType[] convertServiceAltDescriptors(WebserviceDescriptionType[] serviceAltDescriptors, File archiveFile, ArrayList<ConfigurationRoot> configurationDescriptors, ArrayList<MappingRules> mappingDescriptors) throws WSDLException, ProxyGeneratorException {
    if(serviceAltDescriptors == null || serviceAltDescriptors.length == 0) {
      return new com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType[0];  	
    }  
         
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType[] serviceDescriptors = new com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType[serviceAltDescriptors.length];
    int i = 0;
    for(WebserviceDescriptionType serviceAltDescriptor: serviceAltDescriptors) {
      serviceDescriptors[i++] = convertServiceAltDescriptor(serviceAltDescriptor, archiveFile, configurationDescriptors, mappingDescriptors);
    }    
    
    return serviceDescriptors;
  } 
  
  private com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType convertServiceAltDescriptor(WebserviceDescriptionType serviceAltDescriptor, File archiveFile, ArrayList<ConfigurationRoot> configurationDescriptors, ArrayList<MappingRules> mappingDescriptors) throws WSDLException, ProxyGeneratorException {
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType serviceDescriptor = new com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType(); 
        
    serviceDescriptor.setWebserviceName(serviceAltDescriptor.getWebserviceName());
   
    String wsdlFileRelPath = serviceAltDescriptor.getWsdlFile(); 
    WsdlFileType wsdlFileDescriptor = new WsdlFileType(); 
    WsdlType wsdlDescriptor = new WsdlType(); 
    wsdlDescriptor.setType(new WsdlTypeType(WsdlTypeType._root));
    wsdlDescriptor.setStyle(new WsdlStyleType(WsdlStyleType._defaultTemp));
    wsdlDescriptor.set_value(wsdlFileRelPath);   
    wsdlFileDescriptor.setWsdl(new WsdlType[]{wsdlDescriptor});      
    serviceDescriptor.setWsdlFile(wsdlFileDescriptor);
    
    Definitions wsdlDefinitions = wsdlLoader.load(J2EE14Convertor.makeJarURL(archiveFile.getAbsolutePath(), wsdlFileRelPath)); 
    ProxyGeneratorConfigNew proxyGeneratorConfig = new ProxyGeneratorConfigNew();
    proxyGeneratorConfig.setGenerationMode(ProxyGeneratorConfigNew.LOAD_MODE);
    proxyGeneratorConfig.setProvider(true);
    proxyGeneratorConfig.setWsdl(wsdlDefinitions);
    proxyGeneratorConfig.setConfigMarshaller(configurationMarshallerFactory);
    proxyGenerator.generateAll(proxyGeneratorConfig);
           
    ConfigurationRoot configurationDescriptor = proxyGeneratorConfig.getProxyConfig();     
    MappingRules mappingDescriptor = proxyGeneratorConfig.getMappingRules();
    QNameType serviceQName = serviceAltDescriptor.getWsdlService(); 
    overrideService(serviceAltDescriptor, 
    		wsdlDefinitions.getService(new QName(serviceQName.getNamespace(), serviceQName.get_value())), 
    		configurationDescriptor.getRTConfig().getService()[0],
    		configurationDescriptor.getDTConfig().getInterfaceDefinition(),
    		loadInterfaceMappings(mappingDescriptor.getInterface()),    		
    		configurationDescriptor);   
    
    configurationDescriptors.add(configurationDescriptor);
    mappingDescriptors.add(mappingDescriptor);
     
    return serviceDescriptor; 
  }
  
  private void overrideService(
		  WebserviceDescriptionType serviceAltDescriptor, 
		  com.sap.engine.services.webservices.espbase.wsdl.Service serviceWSDL, 
		  Service service, 
		  InterfaceDefinition[] interfaceDefinitions, 
		  Hashtable<String, InterfaceMapping> interfaceMappings, 
		  ConfigurationRoot configurationDescriptor) {
    service.setName(serviceAltDescriptor.getWebserviceName());
    service.setType(Service.J2EE14_SERVICE_TYPE);
   
    PortComponentType[] portComponents = serviceAltDescriptor.getPortComponent(); 
    Hashtable<QName, BindingData> bindingDatas = loadBindingDatas(service.getServiceData().getBindingData());
    overrideBindingDatas(portComponents, serviceWSDL, bindingDatas);
    overrideInterfaceDefinitionIds( service, configurationDescriptor);
    overrideInterfaceMappings(portComponents, bindingDatas, interfaceDefinitions, interfaceMappings); 
  }
  
  private void overrideInterfaceDefinitionIds(
	Service service, 
	ConfigurationRoot configurationDescriptor) {
  
  	ServiceData serviceData = service.getServiceData();
	BindingData[] bindingDatas = serviceData.getBindingData();
	if (bindingDatas == null || bindingDatas.length == 0) {
	    return;
	}
	  
	InterfaceDefinition[] iDefs=  configurationDescriptor.getDTConfig().getInterfaceDefinition();
	if (iDefs == null || iDefs.length == 0) {
	    return;
	}
	  
	  
	HashMap<String, String> interfaceDefIdsOldNew = new HashMap<String, String>(); 
	for (int i = 0; i < iDefs.length; i++) {
		String iterfaceDefName =  iDefs[i].getName();
		String oldIterfaceDefName =  iDefs[i].getId();
		String newInterfaceDefinitionID = appName + "_" + service.getName() + "_" + iterfaceDefName; 
		iDefs[i].setId(newInterfaceDefinitionID);
		 
		interfaceDefIdsOldNew.put(oldIterfaceDefName, newInterfaceDefinitionID);
	}
	
	String oldInterfaceDefinitionID;
	for (int i = 0; i < bindingDatas.length; i++) {
	  oldInterfaceDefinitionID = bindingDatas[i].getInterfaceId();
	  String newInterfaceDefId = interfaceDefIdsOldNew.get(oldInterfaceDefinitionID);
	  
	  if(newInterfaceDefId != null) {
		  bindingDatas[i].setInterfaceId(newInterfaceDefId);
		  
	  }
	}
  }

private void overrideBindingDatas(PortComponentType[] portComponents, com.sap.engine.services.webservices.espbase.wsdl.Service serviceWSDL, Hashtable<QName, BindingData> bindingDatas) {
    if(portComponents == null || portComponents.length == 0) {
      return; 	
	}  
	
    QNameType qName;
    QName qn;
    Endpoint portWSDL;
    BindingData bindingData;    
    String urlWSDL;
    Map<QName, Object[]> processedBindingDatas = new HashMap<QName, Object[]>();
    for(PortComponentType portComponent: portComponents) {
      qName = portComponent.getWsdlPort();
      qn = new QName(qName.getNamespace(), qName.get_value());
      portWSDL = serviceWSDL.getEndpoint(qName.get_value());
      bindingData = bindingDatas.get(qn);
      bindingData = bindingDatas.get(new QName(qName.getNamespace(), qName.get_value()));      
      bindingData.setConfigurationId(portComponent.getPortName());      
      bindingData.setUrl(portComponent.getUrl());
      urlWSDL = portWSDL.getProperty(Endpoint.URL);
      bindingData.setUrlScheme(new URLSchemeType(urlWSDL.substring(0, urlWSDL.indexOf(":")).toLowerCase(Locale.ENGLISH)));processedBindingDatas.put(new QName(bindingData.getBindingNamespace(), bindingData.getBindingName()), new Object[]{portComponent, bindingData});
    }
    
    // process unlisted binding datas, but only if they belong to some of the ports in webservices-j2ee-engine-alt.xml
    Set<QName> bdNames = bindingDatas.keySet();
    PortComponentType portComponent;
    Object[] o;
    Map<PortComponentType, Integer> portComponentAppendIndexMap = new HashMap<PortComponentType, Integer>();
    Integer index;
    for (QName q : bdNames){
      bindingData = bindingDatas.get(q);
      o = processedBindingDatas.get(new QName(bindingData.getBindingNamespace(), bindingData.getBindingName()));
      if (o == null){ // stray binding data
        continue;
      }
      portComponent = (PortComponentType)o[0];
      if (bindingData == o[1]){ // already processed
        continue;
      }
      portWSDL = serviceWSDL.getEndpoint(portComponent.getWsdlPort().get_value());      
      index = portComponentAppendIndexMap.get(portComponent);
      if (index == null){
        index = 0;
      }
      index ++;
      portComponentAppendIndexMap.put(portComponent, index);
      bindingData.setConfigurationId(portComponent.getPortName() + index);      
      bindingData.setUrl(portComponent.getUrl() + "/" + index);
      urlWSDL = portWSDL.getProperty(Endpoint.URL);
      bindingData.setUrlScheme(new URLSchemeType(urlWSDL.substring(0, urlWSDL.indexOf(":")).toLowerCase()));
    }
  }
  
  private void overrideInterfaceMappings(PortComponentType[] portComponents, Hashtable<QName, BindingData> bindingDatas, InterfaceDefinition[] interfaceDefinitionsArr, Hashtable<String, InterfaceMapping> interfaceMappings) {
    if(portComponents == null || portComponents.length == 0) {
      return; 	
    }
    
    QNameType qName;
    BindingData bindingData; 
    InterfaceDefinition interfaceDefinition;  
    InterfaceMapping interfaceMapping; 

    Hashtable<String, InterfaceDefinition> interfaceDefinitions = loadInterfaceDefinition(interfaceDefinitionsArr);
    for(PortComponentType portComponent: portComponents) {
      qName = portComponent.getWsdlPort(); 
      bindingData = bindingDatas.get(new QName(qName.getNamespace(), qName.get_value()));
      interfaceDefinition = interfaceDefinitions.get(bindingData.getInterfaceId());
      interfaceMapping = interfaceMappings.get(interfaceDefinition.getInterfaceMappingId());
      overrideInterfaceMapping(portComponent, interfaceMapping);      
    }    
  }
  
  private void overrideInterfaceMapping(PortComponentType portComponent, InterfaceMapping interfaceMapping) {
	ImplementationLink implLink = interfaceMapping.getImplementationLink();
    if(implLink == null) {      
      interfaceMapping.setImplementationLink(new ImplementationLink());
      implLink =  interfaceMapping.getImplementationLink();
    }
    
    com.sap.engine.services.webservices.espbase.mappings.PropertyType[] properties = convertProperties(portComponent.getImplementationLink().getProperty());
    if(properties != null && properties.length != 0) {
      for(com.sap.engine.services.webservices.espbase.mappings.PropertyType property: properties) {
        implLink.setProperty(property.getName(), property.get_value());	  
      }	
    }    
    
    if(implLink.getImplementationContainerID().equals(GALAXY_IMPL_LINK)) {
      interfaceMapping.setGalaxyInterfaceFlag(true);      
    } else {
      interfaceMapping.setSDOInterfaceFlag(true);	
    }       
  }
  
  private Hashtable<QName, BindingData> loadBindingDatas(BindingData[] bindingDatas) {	  
	if(bindingDatas == null || bindingDatas.length == 0) {
      return new Hashtable<QName, BindingData>();	
    }  
    
    Hashtable<QName, BindingData> bindingDatasTable = new Hashtable<QName, BindingData>();  
    for(BindingData bindingData: bindingDatas) {
      bindingDatasTable.put(new QName(bindingData.getBindingNamespace(), bindingData.getName()), bindingData);   	
    }       
    
    return bindingDatasTable; 
  }
  
  private Hashtable<String, InterfaceDefinition> loadInterfaceDefinition(InterfaceDefinition[] interfaceDefinitions) { 
    if(interfaceDefinitions == null || interfaceDefinitions.length == 0) {
      return new Hashtable<String, InterfaceDefinition>(); 	
    }
    
    Hashtable<String, InterfaceDefinition> interfaceDefinitionsTable = new Hashtable<String, InterfaceDefinition>(); 
    for(InterfaceDefinition interfaceDefinition: interfaceDefinitions) {
      interfaceDefinitionsTable.put(interfaceDefinition.getId(), interfaceDefinition); 
    }
    
    return interfaceDefinitionsTable; 	  
  }
  
  private Hashtable<String, InterfaceMapping> loadInterfaceMappings(InterfaceMapping[] interfaceMappings) {
    if(interfaceMappings == null || interfaceMappings.length == 0) {
      return new Hashtable<String, InterfaceMapping>(); 	
    }
    
    Hashtable<String, InterfaceMapping> interfaceMappingsTable = new Hashtable<String, InterfaceMapping>();
    for(InterfaceMapping interfaceMapping: interfaceMappings) {
      interfaceMappingsTable.put(interfaceMapping.getInterfaceMappingID(), interfaceMapping);	
    }	  
    
    return interfaceMappingsTable; 
  }
  
  private com.sap.engine.services.webservices.espbase.mappings.PropertyType[] convertProperties(PropertyType[] properties) {
    if(properties == null || properties.length == 0) {
      return new com.sap.engine.services.webservices.espbase.mappings.PropertyType[0];  	
    }
    
    com.sap.engine.services.webservices.espbase.mappings.PropertyType[] propertiesNew = new com.sap.engine.services.webservices.espbase.mappings.PropertyType[properties.length];
    com.sap.engine.services.webservices.espbase.mappings.PropertyType propertyNew; 
    int i = 0;
    for(PropertyType property: properties) {
      propertyNew = new com.sap.engine.services.webservices.espbase.mappings.PropertyType();
      propertyNew.setName(property.getName());
      propertyNew.set_value(property.get_value());
      propertiesNew[i++] = propertyNew;
    }    
    
    return propertiesNew;
  }
  
  private void mergeConfigurationDescriptors(ArrayList<ConfigurationRoot> configurationDescriptors, ConfigurationRoot configurationDescriptor) {
    if(configurationDescriptors == null || configurationDescriptors.size() == 0) {
      return;        	
    }
    
    if(configurationDescriptor.getDTConfig() == null) {
      configurationDescriptor.setDTConfig(new InterfaceDefinitionCollection()); 	
    }
    if(configurationDescriptor.getRTConfig() == null) {
      configurationDescriptor.setRTConfig(new ServiceCollection()); 
    }
    
    ArrayList<InterfaceDefinition> interfaceDefinitions = new ArrayList<InterfaceDefinition>();
    ArrayList<Service> services = new ArrayList<Service>();
    addInterfaceDefinitions(configurationDescriptor.getDTConfig().getInterfaceDefinition(), interfaceDefinitions);         
    addServices(configurationDescriptor.getRTConfig().getService(), services); 
    for(ConfigurationRoot currentConfigurationDescriptor: configurationDescriptors) {
      addInterfaceDefinitions(currentConfigurationDescriptor.getDTConfig().getInterfaceDefinition(), interfaceDefinitions);
      addServices(currentConfigurationDescriptor.getRTConfig().getService(), services); 
    }

    configurationDescriptor.getDTConfig().setInterfaceDefinition(interfaceDefinitions.toArray(new InterfaceDefinition[interfaceDefinitions.size()]));
    configurationDescriptor.getRTConfig().setService(services.toArray(new Service[services.size()]));     
  }
  
  private void mergeMappingDescriptors(ArrayList<MappingRules> mappingDescriptors, MappingRules mappingDescriptor) {
    if(mappingDescriptors == null || mappingDescriptors.size() == 0) {
      return; 	
    }
    
    ArrayList<InterfaceMapping> interfaceMappings = new ArrayList<InterfaceMapping>();      
    ArrayList<ServiceMapping> serviceMappings = new ArrayList<ServiceMapping>();
    addInterfaceMappings(mappingDescriptor.getInterface(), interfaceMappings);
    addServiceMappings(mappingDescriptor.getService(), serviceMappings);
    for(MappingRules currentMappingDescriptor: mappingDescriptors) {
      addInterfaceMappings(currentMappingDescriptor.getInterface(), interfaceMappings);
      addServiceMappings(currentMappingDescriptor.getService(), serviceMappings); 
    }
              
    mappingDescriptor.setInterface(interfaceMappings.toArray(new InterfaceMapping[interfaceMappings.size()]));
    mappingDescriptor.setService(serviceMappings.toArray(new ServiceMapping[serviceMappings.size()]));    
        
    return;   
  }
  
  private void addInterfaceDefinitions(InterfaceDefinition[] srcInterfaceDefinitions, ArrayList<InterfaceDefinition> destInterfaceDefinitions) {
    if(srcInterfaceDefinitions == null || srcInterfaceDefinitions.length == 0) {
      return; 	
    }  
    
    for(InterfaceDefinition interfaceDefinition: srcInterfaceDefinitions) {
      destInterfaceDefinitions.add(interfaceDefinition);
    }    
  } 
  
  private void addServices(Service[] srcServices, ArrayList<Service> destServices) {
    if(srcServices == null || srcServices.length == 0) {
      return; 	
    }  
   
    for(Service service: srcServices) {
      destServices.add(service);	  
    }    
  }
  
  private void addInterfaceMappings(InterfaceMapping[] srcInterfaceMappings, ArrayList<InterfaceMapping> destInterfaceMappings) {
    if(srcInterfaceMappings == null || srcInterfaceMappings.length == 0) {
      return; 	
    } 
    
    for(InterfaceMapping interfaceMapping: srcInterfaceMappings) {
      destInterfaceMappings.add(interfaceMapping);	
    }    
  }
  
  private void addServiceMappings(ServiceMapping[] srcServiceMappings, ArrayList<ServiceMapping> destServiceMappings) {
    if(srcServiceMappings == null || srcServiceMappings.length == 0) {    	
      return; 	
    }
    
    for(ServiceMapping serviceMapping: srcServiceMappings) {
      destServiceMappings.add(serviceMapping);     	
    }	  
  }  
  
  private void addWebServicesJ2EEEngineDescriptors(com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType webServicesJ2EEEngineDescriptor, ConfigurationRoot configurationDescriptor, MappingRules mappingDescriptor, JarOutputStream jarOut) throws IOException, TypeMappingException {           
    ZipEntryOutputStream webServicesJ2EEEngineDescriptorOut = null;
    try {                 
      webServicesJ2EEEngineDescriptorOut = new ZipEntryOutputStream(jarOut, META_INF + "/" + WSDeployProcess.WEBSERVICES_J2EE_ENGINE_DESCRIPTOR); 
      WebServicesJ2EEEngineFactory.save(webServicesJ2EEEngineDescriptor, webServicesJ2EEEngineDescriptorOut);
    } finally {
	  try {
        if(webServicesJ2EEEngineDescriptorOut != null) {
          webServicesJ2EEEngineDescriptorOut.close(); 
        }	  
      } catch(IOException e) {
    	// $JL-EXC$		  
      }		
    }
    
    ZipEntryOutputStream configurationDescriptorOut = null; 
    try {            
      configurationDescriptorOut = new ZipEntryOutputStream(jarOut, webServicesJ2EEEngineDescriptor.getConfigurationFile());                   
      ConfigurationFactory.save(configurationDescriptor, configurationDescriptorOut);                                                         
    } finally {
      try {
        if(configurationDescriptorOut != null) {
          configurationDescriptorOut.close(); 
        }	  
      } catch(IOException e) {
  	    // $JL-EXC$		  
      }	
    }
    
    ZipEntryOutputStream mappingDescriptorOut = null;     
    try {
      mappingDescriptorOut = new ZipEntryOutputStream(jarOut, webServicesJ2EEEngineDescriptor.getWsdlMappingFile());      
      MappingFactory.save(mappingDescriptor, mappingDescriptorOut);  	
    } finally {           
      try {
        if(mappingDescriptorOut != null) {
          mappingDescriptorOut.close(); 
        }	  
      } catch(IOException e) {
  	    // $JL-EXC$		  
      }	
    } 
  }
  
}