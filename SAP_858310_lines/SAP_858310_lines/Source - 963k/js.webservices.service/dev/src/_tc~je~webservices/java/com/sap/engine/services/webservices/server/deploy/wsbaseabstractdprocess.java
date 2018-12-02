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

package com.sap.engine.services.webservices.server.deploy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.ConfigurationLockedException;
import com.sap.engine.frame.core.configuration.InconsistentReadException;
import com.sap.engine.frame.core.configuration.NameAlreadyExistsException;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.frame.core.configuration.NoWriteAccessException;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.lib.io.hash.HashUtils;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.EJBImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.server.container.BaseServiceContext;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.BindingDataRegistry;
import com.sap.engine.services.webservices.server.container.configuration.ConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.InterfaceDefinitionRegistry;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.mapping.InterfaceMappingRegistry;
import com.sap.engine.services.webservices.server.container.mapping.MappingContext;
import com.sap.engine.services.webservices.server.container.mapping.OperationMappingRegistry;
import com.sap.engine.services.webservices.server.container.mapping.ServiceMappingRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.metadata.service.BindingDataMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;

/**
 * Title: WSBaseAbstractDProcess
 * Description: WSBaseAbstractDProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public abstract class WSBaseAbstractDProcess implements WebServicesDInterface {
	
  protected static final String WSDL_DIR = "wsdl";
  
  protected String applicationName; 
  protected String webServicesContainerDir;
  protected String webServicesContainerTempDir; 
  protected Configuration appConfiguration;    
  protected BaseServiceContext serviceContext;
  
  protected void loadConfigurationDescriptor(ConfigurationRoot serviceConfigurationDescriptor) throws Exception {                         
    loadInterfaceDefinitionConfigurationDescriptors(serviceConfigurationDescriptor.getDTConfig().getInterfaceDefinition());
    loadServiceConfigurationDescriptors(serviceConfigurationDescriptor.getRTConfig().getService());    
  }    
  
  private void loadInterfaceDefinitionConfigurationDescriptors(InterfaceDefinition[] interfaceDefinitions) throws Exception {    
    if(interfaceDefinitions == null || interfaceDefinitions.length == 0) {
      return; 
    }
    
    ConfigurationContext configurationContext = serviceContext.getConfigurationContext();
    InterfaceDefinitionRegistry interfaceDefinitionRegistry = configurationContext.getInterfaceDefinitionRegistry();             
    for(int i = 0; i < interfaceDefinitions.length; i++) {
      InterfaceDefinition interfaceDefinition = interfaceDefinitions[i];      
      interfaceDefinitionRegistry.putInterfaceDefinition(interfaceDefinition.getId().trim(), interfaceDefinition);       
    }    
  }  
  
  public void unregisterInterfaceDefinitionDescriptors(InterfaceDefinition[] interfaceDefinitionDescriptors) {
    ConfigurationContext configurationContext = serviceContext.getConfigurationContext(); 
    InterfaceDefinitionRegistry interfaceDefinitionRegistry = configurationContext.getInterfaceDefinitionRegistry();     
    if(interfaceDefinitionDescriptors != null && interfaceDefinitionDescriptors.length != 0) {
      for(int i = 0; i < interfaceDefinitionDescriptors.length; i++) {
        interfaceDefinitionRegistry.removeInterfaceDefinition(interfaceDefinitionDescriptors[i].getId().trim());
      }
    } 
  }
 
  private void loadServiceConfigurationDescriptors(Service[] services) throws Exception {
    if(services == null || services.length == 0) {
      return;
    }
       
    ConfigurationContext configurationContext = serviceContext.getConfigurationContext();    
    BindingDataRegistry globalBindingDataRegistry = configurationContext.getGlobalBindingDataRegistry(); 
    ServiceRegistry globalServiceRegistry = configurationContext.getGlobalServiceRegistry();     
    if(!configurationContext.getApplicationConfigurationContexts().containsKey(applicationName)) {
      configurationContext.getApplicationConfigurationContexts().put(applicationName, new ApplicationConfigurationContext());          
    }    
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().get(applicationName);
       
    for(int i = 0; i < services.length; i++) {      
      Service service = services[i];      
      String serviceName = service.getName().trim();
      String serviceContextRoot = service.getServiceData().getContextRoot().trim();         
      if(!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
        serviceContextRoot = "/" + serviceContextRoot; 
      }       
      globalServiceRegistry.putService(serviceContextRoot, service);
      applicationConfigurationContext.getServiceRegistry().putService(service.getName().trim(), service);
     
      BindingData[] bindingDatas = service.getServiceData().getBindingData();
      BindingDataRegistry serviceBindingDataRegistry = null; 
      if(bindingDatas != null && bindingDatas.length != 0) {                  
        serviceBindingDataRegistry = new BindingDataRegistry(); 
        for(int j = 0; j < bindingDatas.length; j++) {
          BindingData bindingData = bindingDatas[j];
          String bindingDataUrl = bindingData.getUrl().trim();            
          if(!bindingDataUrl.startsWith("/")) {
            bindingDataUrl = "/" + bindingDataUrl;
          }
          globalBindingDataRegistry.putBindingData(serviceContextRoot + bindingDataUrl, bindingData);
          serviceBindingDataRegistry.putBindingData(bindingData.getName().trim(), bindingData);
          //validity check
          if (configurationContext.getInterfaceDefinitionRegistry().getInterfaceDefinition(bindingData.getInterfaceId().trim()) == null) {
            throw new Exception("BindingData '" + bindingData.getName().trim() + "' with url '" + serviceContextRoot + bindingDataUrl + "' does not refer to interface definition via id '" + bindingData.getInterfaceId().trim() + "'");
          }
        }
        applicationConfigurationContext.getBindingDataRegistries().put(serviceName, serviceBindingDataRegistry); 
      }      
    }            
  }
  
  protected void unregisterServiceConfigurationDescriptors() {
    ConfigurationContext configurationContext = serviceContext.getConfigurationContext(); 
    if(!configurationContext.getApplicationConfigurationContexts().containsKey(applicationName)) {
      return; 
    }
  
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)configurationContext.getApplicationConfigurationContexts().remove(applicationName);
    ServiceRegistry globalServiceRegistry = configurationContext.getGlobalServiceRegistry();
    BindingDataRegistry globalBindingDataRegistry = configurationContext.getGlobalBindingDataRegistry();
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();   

    Enumeration enum1 = serviceRegistry.getServices().keys();        
    while(enum1.hasMoreElements()) {
      String serviceName = (String)enum1.nextElement();      
      Service service = (Service)serviceRegistry.getService(serviceName);      
      unregisterGlobalServiceAndBindingDataCDescriptors(service, globalServiceRegistry, globalBindingDataRegistry);      
    }
  }      
   
  public void unregisterGlobalServiceAndBindingDataCDescriptors(Service service, ServiceRegistry globalServiceRegistry, BindingDataRegistry globalBindingDataRegistry) {
    String serviceContextRoot = service.getServiceData().getContextRoot().trim();
    if(!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
      serviceContextRoot = "/" + serviceContextRoot; 
    } 
    globalServiceRegistry.removeService(serviceContextRoot); 

    BindingData[] bindingDatas = service.getServiceData().getBindingData(); 
    if(bindingDatas != null && bindingDatas.length != 0) {
      for(int i = 0; i < bindingDatas.length; i++) {        
        BindingData bindingData = bindingDatas[i];
        String bindingDataUrl = bindingData.getUrl().trim();            
        if(!bindingDataUrl.startsWith("/")) {
          bindingDataUrl = "/" + bindingDataUrl;
        }
        globalBindingDataRegistry.removeBindingData(serviceContextRoot + bindingDataUrl);
      }
    }   
  }    
  
  protected void loadMappingDescriptor(String moduleName, MappingRules mappingDescriptor) {    
    loadInterfaceMappingDescriptors(moduleName, mappingDescriptor.getInterface());
    loadServiceMappingDescriptors(mappingDescriptor.getService());       
  }           
  
  private void loadInterfaceMappingDescriptors(String moduleName, InterfaceMapping[] interfaceMappings) {
    if(interfaceMappings == null || interfaceMappings.length == 0) {
      return; 
    }
    
    MappingContext mappingContext = serviceContext.getMappingContext(); 
    InterfaceMappingRegistry interfaceMappingRegistry = mappingContext.getInterfaceMappingRegistry();           
    for(int i = 0; i < interfaceMappings.length; i++) {
      InterfaceMapping interfaceMapping = interfaceMappings[i];      
      String interfaceMappingName = interfaceMapping.getInterfaceMappingID().trim();;           
      interfaceMappingRegistry.putInterfaceMapping(interfaceMappingName, interfaceMapping);   
      if(interfaceMapping.getImplementationLink() != null) {                         
        if(interfaceMapping.getImplementationLink().getImplementationContainerID() != null && interfaceMapping.getImplementationLink().getImplementationContainerID().trim().equals(EJBImplementationLink.IMPLEMENTATION_CONTAINER_ID)) {      
          interfaceMapping.getImplementationLink().setProperty(EJBImplementationLink.JAR_NAME, moduleName);    
        }
      }
          
      OperationMapping[] operationMappings = interfaceMapping.getOperation();
      if(operationMappings != null && operationMappings.length!= 0) {
        OperationMappingRegistry operationMappingRegistry = new OperationMappingRegistry();
        for(int j = 0; j < operationMappings.length; j++) {
          OperationMapping operationMapping = operationMappings[j];
          operationMappingRegistry.putOperationMapping(operationMapping.getWSDLOperationName().trim(), operationMapping);             
        }
          
        mappingContext.getInterfaceOperationMappingRegistries().put(interfaceMappingName, operationMappingRegistry);
      }                              
    }     
  }
  
  private void loadServiceMappingDescriptors(ServiceMapping[] serviceMappings) {
    if(serviceMappings == null || serviceMappings.length == 0) {
      return; 
    }
    
    MappingContext mappingContext = serviceContext.getMappingContext();        
    ServiceMappingRegistry serviceMappingRegistry = mappingContext.getServiceMappingRegistry();
        
    ServiceMapping serviceMapping; 
    String serviceMappingId ; 
    for(int i = 0; i < serviceMappings.length; i++) {
      serviceMapping = serviceMappings[i];
      serviceMappingId = serviceMapping.getServiceMappingId();           
      if(serviceMappingId != null) {      
        serviceMappingRegistry.putServiceMapping(serviceMappingId.trim(), serviceMapping);
      }  
    }          
  }
    
  public void unregisterInterfaceMappingDescriptors(InterfaceMapping[] interfaceMappingDescriptors) {
    if(interfaceMappingDescriptors == null || interfaceMappingDescriptors.length == 0) {
      return; 
    }
  
    MappingContext mappingContext = serviceContext.getMappingContext(); 
    InterfaceMappingRegistry interfaceMappingRegistry = mappingContext.getInterfaceMappingRegistry();
    Hashtable operationMappingRegistry = mappingContext.getInterfaceOperationMappingRegistries(); 
      
    for(int i = 0; i < interfaceMappingDescriptors.length; i++) {
      InterfaceMapping interfaceMapping = interfaceMappingDescriptors[i];
      String interfaceName = interfaceMapping.getInterfaceMappingID().trim(); 
      interfaceMappingRegistry.removeInterfaceMapping(interfaceName);
      operationMappingRegistry.remove(interfaceName);           
    }          
  }

  public void unregisterServiceMappingDescriptors(ServiceMapping[] serviceMappingDescriptors) {
    if(serviceMappingDescriptors == null || serviceMappingDescriptors.length == 0) {
      return;  
    }
   
    MappingContext mappingContext = serviceContext.getMappingContext(); 
    ServiceMappingRegistry serviceMappingRegistry = mappingContext.getServiceMappingRegistry(); 
   
    String serviceMappingId = null; 
    ServiceMapping serviceMapping = null; 
    for(int i = 0; i < serviceMappingDescriptors.length; i++) {
      serviceMappingId = serviceMappingDescriptors[i].getServiceMappingId(); 
      if(serviceMappingId != null) {      
        serviceMapping = serviceMappingRegistry.removeServiceMapping(serviceMappingId.trim());
        System.out.println("");
      }
    }   
  }
  
  protected void loadBindingDataMetaDatas(String applicationName, Service services[]) {
    if(services == null || services.length == 0) {    
      return;
    }    
  
    Service service; 
    for(int i = 0; i < services.length; i++) {
      service = services[i];
      loadBindingDataMetaDatas(applicationName, service, service.getServiceData().getBindingData());
    }
  }
 
  protected abstract void loadBindingDataMetaDatas(String applicationName, Service service, BindingData[] bindingDatas); 
   
  public void unregisterGlobalBindingDataMetaData(Service service, BindingDataMetaDataRegistry bindingDataMetaDataRegistry) {
    String serviceContextRoot = service.getServiceData().getContextRoot().trim();
    if(!serviceContextRoot.equals("") && !serviceContextRoot.startsWith("/")) {
      serviceContextRoot = "/" + serviceContextRoot; 
    } 
          
    BindingData[] bindingDatas = service.getServiceData().getBindingData(); 
    if(bindingDatas != null && bindingDatas.length !=0) {
      for(int i = 0; i < bindingDatas.length; i++) {        
        BindingData bindingData = bindingDatas[i];
        String bindingDataUrl = bindingData.getUrl().trim();            
        if(!bindingDataUrl.startsWith("/")) {
          bindingDataUrl = "/" + bindingDataUrl;
        }
        bindingDataMetaDataRegistry.removeBindingDataMetaData(serviceContextRoot + bindingDataUrl);
      }
    }      
  } 
  
  public void makeProcess() throws WSDeploymentException {          
    init();
    
    execute();
    
    finish();    
  }
  
  protected void loadApplicationMetaData(Configuration webServicesContainerCofiguration, ModuleRuntimeDataRegistry moduleRuntimeDataRegistry) throws ConfigurationException {            
    if(!webServicesContainerCofiguration.existsSubConfiguration(WSApplicationMetaDataContext.METADATA)) {
      return; 
    }

    Configuration metaDataConfiguration = webServicesContainerCofiguration.getSubConfiguration(WSApplicationMetaDataContext.METADATA);
    String[] subConfigurationNames = metaDataConfiguration.getAllSubConfigurationNames(); 
    if(subConfigurationNames == null) {
      return; 
    } 
 
    Configuration subConfiguration;       
    String subConfigurationName; 
    String moduleDirName; 
    ModuleRuntimeData moduleRuntimeData; 
    for(int i = 0; i < subConfigurationNames.length; i++) {      
      subConfigurationName = subConfigurationNames[i];
      subConfiguration = metaDataConfiguration.getSubConfiguration(subConfigurationName);
      moduleDirName = (String)subConfiguration.getConfigEntry(ModuleRuntimeData.MODULE_DIR_NAME);
      moduleRuntimeData = new ModuleRuntimeData(subConfigurationName, webServicesContainerDir + "/" + moduleDirName, webServicesContainerTempDir + "/" + moduleDirName); 
      moduleRuntimeDataRegistry.putModuleRuntimeData(subConfigurationName, moduleRuntimeData);        
      if(subConfiguration.existsConfigEntry(ModuleRuntimeData.ARCHIVE_FILE)) {
        moduleRuntimeData.setArchiveFileRelPath((String)subConfiguration.getConfigEntry(ModuleRuntimeData.ARCHIVE_FILE));
      }
    }            
  }
  
  protected String[] getModuleFilesForClassLoader(ModuleRuntimeDataRegistry moduleRuntimeDataRegistry) {    
    Enumeration enum1 = moduleRuntimeDataRegistry.getModuleRuntimeDatas().elements(); 
    ModuleRuntimeData moduleRuntimeData; 
    Vector moduleFilesForClassLoader = new Vector();
    while(enum1.hasMoreElements()) {
      moduleRuntimeData = (ModuleRuntimeData)enum1.nextElement(); 
      if(moduleRuntimeData.getType().equals(ModuleRuntimeData.WS_SUFFIX)) {
        moduleFilesForClassLoader.add(webServicesContainerDir + "/" + WSApplicationMetaDataContext.BACKUP + "/" + moduleRuntimeData.getArchiveFileRelPath());
      }     
    }
  
    String[] moduleFilesForClassLoaderArr = new String[moduleFilesForClassLoader.size()];
    moduleFilesForClassLoader.toArray(moduleFilesForClassLoaderArr);
    return moduleFilesForClassLoaderArr;  
  }
    
  protected void uploadMetaData(Configuration webServicesContainerConfiguration, ModuleRuntimeDataRegistry moduleRuntimeDataRegistry) throws ConfigurationException {  
    Configuration metaDataConfiguration;    
    if(!webServicesContainerConfiguration.existsSubConfiguration(WSApplicationMetaDataContext.METADATA)) {
      metaDataConfiguration = webServicesContainerConfiguration.createSubConfiguration(WSApplicationMetaDataContext.METADATA);
      metaDataConfiguration.addConfigEntry(WSApplicationMetaDataContext.VERSION, WSApplicationMetaDataContext.VERSION_71);
    } else {
      metaDataConfiguration = webServicesContainerConfiguration.getSubConfiguration(WSApplicationMetaDataContext.METADATA);
    }  
                  
    Enumeration enum1 = moduleRuntimeDataRegistry.getModuleRuntimeDatas().elements(); 
    ModuleRuntimeData moduleRuntimeData; 
    String moduleName; 
    Configuration subConfiguration; 
    while(enum1.hasMoreElements()) {
      moduleRuntimeData = (ModuleRuntimeData)enum1.nextElement();
      moduleName = moduleRuntimeData.getModuleName();
      if(!metaDataConfiguration.existsSubConfiguration(moduleName)) {          
        subConfiguration = metaDataConfiguration.createSubConfiguration(moduleRuntimeData.getModuleName());
        subConfiguration.addConfigEntry(ModuleRuntimeData.MODULE_DIR_NAME, new File(moduleRuntimeData.getModuleDir()).getName());
        subConfiguration.addConfigEntry(ModuleRuntimeData.ARCHIVE_FILE, moduleRuntimeData.getArchiveFileRelPath()); 
      }          
    }   
  }  
  
  protected void uploadBackUp(Configuration webServicesConfiguration, ModuleRuntimeDataRegistry moduleRuntimeDataRegistry) throws ConfigurationException, IOException {     
    if(moduleRuntimeDataRegistry == null || moduleRuntimeDataRegistry.getModuleRuntimeDatas().size() == 0) {
      return; 
    }   
       
    Configuration backUpConfiguration; 
    if(webServicesConfiguration.existsSubConfiguration(WSApplicationMetaDataContext.BACKUP)) {
      backUpConfiguration = webServicesConfiguration.getSubConfiguration(WSApplicationMetaDataContext.BACKUP); 
    } else {
      backUpConfiguration = webServicesConfiguration.createSubConfiguration(WSApplicationMetaDataContext.BACKUP); 
    }
    
    Enumeration enum1 = moduleRuntimeDataRegistry.getModuleRuntimeDatas().keys(); 
    ModuleRuntimeData moduleRuntimeData;     
    String moduleName; 
    String archiveFileRelPath;
    Configuration moduleConfiguration = null;
    while(enum1.hasMoreElements()) {     
      moduleName = (String)enum1.nextElement();
      moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(moduleName);       
      archiveFileRelPath = moduleRuntimeData.getArchiveFileRelPath();
      uploadFile(new File(webServicesContainerDir + "/" + WSApplicationMetaDataContext.BACKUP), archiveFileRelPath, backUpConfiguration, false); 
    }         
  }
  
  public static Configuration createSubConfiguration(ConfigurationHandler configurationHandler, String configurationPath) throws ConfigurationLockedException, NameNotFoundException, NameAlreadyExistsException, InconsistentReadException, NoWriteAccessException, ConfigurationException {
    configurationPath = normalizePath(configurationPath); 
    
    String rootConfigurationName = configurationPath; 
    String configurationRelPath = ""; 
    int cutIndex = configurationPath.indexOf("/");
    if(cutIndex != -1) {
      rootConfigurationName = configurationPath.substring(0, cutIndex);
      configurationRelPath = configurationPath.substring(cutIndex + 1); 
    }
    
    Configuration rootConfiguration = null; 
    try {   
      rootConfiguration = configurationHandler.openConfiguration(rootConfigurationName, ConfigurationHandler.WRITE_ACCESS);
    } catch(NameNotFoundException e) {
      // $JL-EXC$	   	
	}
    
    if(rootConfiguration == null) {
      rootConfiguration = configurationHandler.createRootConfiguration(rootConfigurationName); 	
    }
    
    if(configurationRelPath.equals("")) {
      return rootConfiguration; 	
    }    
    
    return createSubConfiguration(rootConfiguration, configurationRelPath); 
  }
  
  
  public static Configuration getSubConfiguration(Configuration configuration, String configurationRelPath) throws InconsistentReadException, NameNotFoundException, ConfigurationException {
    configurationRelPath = normalizePath(configurationRelPath); 
    
    StringTokenizer tokenizer = new StringTokenizer(configurationRelPath, "/"); 
    String token; 
    while(tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken(); 
      configuration = configuration.getSubConfiguration(token); 
    }
    
    return configuration;     
  }
  
  public static Configuration createSubConfiguration(Configuration configuration, String configurationRelPath) throws InconsistentReadException, NameNotFoundException, NameAlreadyExistsException, NoWriteAccessException, ConfigurationException {
    configurationRelPath = normalizePath(configurationRelPath); 
      
    StringTokenizer tokenizer = new StringTokenizer(configurationRelPath, "/");
    String token;            
    while(tokenizer.hasMoreTokens()) { 
      token = tokenizer.nextToken();          
      if(configuration.existsSubConfiguration(token)) {
        configuration = configuration.getSubConfiguration(token);                     
      } else {
        configuration = configuration.createSubConfiguration(token);        
      }
    }
	  
    return configuration; 
  }
  
  public static void deleteSubConfiguration(Configuration configuration, String configurationRelPath) throws InconsistentReadException, NoWriteAccessException, ConfigurationException {
    configurationRelPath = normalizePath(configurationRelPath); 
    
    String parentConfigurationRelPath = ""; 
    String configurationName = configurationRelPath;
    int cutIndex = configurationRelPath.indexOf("/"); 
    if(cutIndex != -1) {
      parentConfigurationRelPath = configurationRelPath.substring(0, cutIndex);
      configurationName = configurationRelPath.substring(cutIndex + 1); 
    }
    
    Configuration parentConfiguration = null; 
    try {
      parentConfiguration = getSubConfiguration(configuration, parentConfigurationRelPath);     	
    } catch(NameNotFoundException e) {
      // $JL-EXC$	
    }    
    
    if(parentConfiguration == null) {
      return; 	
    }
    
    parentConfiguration.deleteSubConfigurations(new String[]{configurationName});     
  }
  
  public static void uploadDirectory(File dir, Configuration parentConfiguration, boolean toParent) throws IOException, ConfigurationException {
    if(toParent) {      
      String configName = dir.getName();
      Configuration configuration = parentConfiguration.createSubConfiguration(configName);
      uploadDirectory(dir, configuration);    
    } else {      
      uploadDirectory(dir, parentConfiguration);      
    }
  }
  
  public static void uploadDirectory(File dir, Configuration configuration) throws IOException, ConfigurationException {    
    uploadDirectory(dir, configuration, new HashSet<String>());     
  }
  
  public static void uploadDirectory(File dir, String dirRelPath, Configuration configuration, Set<String> skippedChildDirs) throws IOException, ConfigurationException {
    dirRelPath = normalizePath(dirRelPath);        
    
    StringTokenizer tokenizer = new StringTokenizer(dirRelPath, "/");    
    String token;
    while(tokenizer.hasMoreTokens()) { 
      token = tokenizer.nextToken();          
      if(configuration.existsSubConfiguration(token)) {
        configuration = configuration.getSubConfiguration(token);                     
      } else {
        configuration = configuration.createSubConfiguration(token);
      }
    }
   
    uploadDirectory(new File(dir, dirRelPath), configuration, skippedChildDirs);
  }
  
  public static void uploadDirectory(File dir, Configuration configuration, Set<String> skippedChildDirs) throws IOException, ConfigurationException {      
    uploadDirectory(dir, configuration, skippedChildDirs, new String[]{".nfs"});  	  
  }
  
  public static void uploadDirectory(File dir, Configuration configuration, Set<String> skippedChildDirs, String[] prefixExcludeList) throws IOException, ConfigurationException {
    if(dir == null) {
      return;
    }
    
    if(!dir.exists() || !dir.isDirectory()) {
      return; 
    }
    
    String dirPath = dir.getAbsolutePath();    
    File[] files = dir.listFiles();     
    String filePath = "";
    String fileRelPath = ""; 
    File file;
    Configuration subConfiguration = null; 
    for(int i = 0; i < files.length; i++) {
      file = files[i];     
      if(!skippedChildDirs.contains(file.getName()) && !WSUtil.startsWith(file.getName(), prefixExcludeList)) {            
        if(file.isDirectory()) {
          subConfiguration = null; 
          try {
            subConfiguration = configuration.getSubConfiguration(file.getName());
          } catch(NameNotFoundException e) {
            // $JL-EXC$	  
          }
          if(subConfiguration == null) {
            subConfiguration = configuration.createSubConfiguration(file.getName());  	  
          }
          uploadDirectory(file, subConfiguration, new HashSet(), prefixExcludeList);
        } else if(file.isFile()) {                  
          filePath = files[i].getAbsolutePath();
          fileRelPath = filePath.substring(filePath.indexOf(dirPath) + dirPath.length());      
          uploadFile(dir, fileRelPath, configuration, true);        
        }
      }       
    }          
  }
  
  public static void uploadFile(File parentDir, String fileRelPath, Configuration configuration, boolean toBeModifiedIfExists) throws IOException, ConfigurationException {    
    fileRelPath = fileRelPath.replace('\\', '/');
    if(fileRelPath.startsWith("/")) {
      fileRelPath = fileRelPath.substring(1);
    }
    if(fileRelPath.endsWith("/")) {
      fileRelPath = fileRelPath.substring(0, fileRelPath.length() - 1);
    }    
    String fileRelPathName = "";
    if(fileRelPath.lastIndexOf("/") != -1) {    
      fileRelPathName = fileRelPath.substring(0, fileRelPath.lastIndexOf("/"));
    } 
                
    StringTokenizer tokenizer = new StringTokenizer(fileRelPathName, "/");    
    String token;
    while(tokenizer.hasMoreTokens()) { 
      token = tokenizer.nextToken();          
      if(configuration.existsSubConfiguration(token)) {
        configuration = configuration.getSubConfiguration(token);                     
      } else {
        configuration = configuration.createSubConfiguration(token);
      }
    }    
    
    File file = new File(parentDir, fileRelPath);
    if(configuration.existsConfigEntry(file.getName())){
      if(toBeModifiedIfExists) {        
        configuration.modifyConfigEntry(file.getName(), HashUtils.generateFileHash(file)); 
        configuration.updateFile(file);
      }
    } else {      
      configuration.addConfigEntry(file.getName(), HashUtils.generateFileHash(file)); 
      configuration.addFileEntry(file);
    }   
  }  
  
  public static boolean downloadDirectory(File parentDir, Configuration configuration, boolean toParent) throws IOException, ConfigurationException {
    return downloadDirectory(parentDir, configuration, toParent, new HashSet()); 
  }
  
  public static boolean downloadDirectory(File parentDir, Configuration configuration, boolean toParent, Set skippedChildDirs) throws IOException, ConfigurationException {    
    if(toParent) {      
      String dirName = configuration.getMetaData().getName(); 
      return downloadDirectory(new File(parentDir, dirName), configuration, skippedChildDirs);      
    } else {     
      return downloadDirectory(parentDir, configuration);       
    }   
  }
  
  public static boolean downloadDirectory(File dir, Configuration configuration) throws IOException, ConfigurationException {
    return downloadDirectory(dir, configuration, new HashSet());      
  }
  
  public static boolean downloadDirectory(File dir, Configuration configuration, Set skippedChildDirs) throws IOException, ConfigurationException {       
    boolean result = false; 
	
    result = downloadFiles(dir, configuration) || result;
    result = downloadSubDirectories(dir, configuration, skippedChildDirs) || result;
    
    return result; 
  }
  
  protected static boolean downloadFiles(File dir, Configuration configuration) throws IOException, ConfigurationException {   
    boolean result = false;      
	File[] files = dir.listFiles();         
    
    if(files == null) {
      files = new File[0];
    }
    
    File file;            
    for(int i = 0; i < files.length; i++) {      
      file = files[i];           
      if(!file.isDirectory()) {         
        if(!configuration.existsFile(file.getName()))  {
          result = true; 
          if(!file.delete()) {
            //TODO - throw exception             
          }
        }
      }
    }
    
    String[] fileEntryNames = configuration.getAllFileEntryNames();
    if(fileEntryNames == null) {
      return result; 
    } 
     
    for (int i = 0; i < fileEntryNames.length; i++) {                
      result = downloadFile(dir, fileEntryNames[i], configuration) || result;        
    }
    
    return result; 
  }
 
  private static boolean downloadSubDirectories(File dir, Configuration configuration, Set skippedChildDirs) throws IOException, ConfigurationException {
	boolean result = false; 
	File[] files = dir.listFiles();    
    
    if(files == null) {
      files = new File[0];
    }
    
    File file;     
    for(int i = 0; i < files.length; i++) {       
      file = files[i];                        
      if(file.isDirectory()) {                             
        if(!configuration.existsSubConfiguration(file.getName())) {                     
          result = true; 
          if(!IOUtil.deleteDir(file)) {             
            //TODO - throw exception                           
          } 
        } 
      }        
    }
        
    String[] subConfigurationNames = configuration.getAllSubConfigurationNames();
    if(subConfigurationNames == null) {
      return result;
    }
    
    String subConfigurationName = ""; 
    Configuration subConfiguration; 
    for(int i = 0; i < subConfigurationNames.length; i++) {
      subConfigurationName = subConfigurationNames[i];
      if(!skippedChildDirs.contains(subConfigurationName)) {        
        subConfiguration = configuration.getSubConfiguration(subConfigurationName);
        result = downloadDirectory(new File(dir, subConfigurationName), subConfiguration) || result;
      }
    }              
    
    return result; 
  } 
       
  protected static boolean downloadFile(File dir, String fileRelPath, Configuration configuration) throws IOException, ConfigurationException {
    boolean result = false; 
    
	fileRelPath = fileRelPath.replace('\\', '/');
    if(fileRelPath.startsWith("/")) {
      fileRelPath = fileRelPath.substring(1);
    }
    if(fileRelPath.endsWith("/")) {
      fileRelPath = fileRelPath.substring(0, fileRelPath.length() - 1);
    }    
    String fileRelPathName = "";
    if(fileRelPath.lastIndexOf("/") != -1) {    
      fileRelPathName = fileRelPath.substring(0, fileRelPath.lastIndexOf("/"));
    }
    
    FileOutputStream fileOut = null;    
    try {     
      StringTokenizer tokenizer = new StringTokenizer(fileRelPathName, "/");        
      while(tokenizer.hasMoreTokens()) {                   
        String token = tokenizer.nextToken();
        configuration = configuration.getSubConfiguration(token);                             
      }    
      
      File file = new File(dir, fileRelPath); 
      String fileName = file.getName();                          
      if(!file.exists()) {
    	result = true; 
        file.getParentFile().mkdirs();
        fileOut = new FileOutputStream(file);
        IOUtil.copy(configuration.getFile(fileName), fileOut);
      } else {
        byte[] crcDB = (byte[])configuration.getConfigEntry(fileName);
        byte[] crc = HashUtils.generateFileHash(file);
        if(!HashUtils.compareHash(crcDB, crc)) {
          result = true; 
          fileOut = new FileOutputStream(file);
          IOUtil.copy(configuration.getFile(fileName), fileOut);
        }        
      }
    } finally {
      try {        
        if(fileOut != null) {
          fileOut.close();
        }
      } catch(Exception iExc) {
        // $JL-EXC$
      } 
    }   
    
    return result; 
  } 

  public static String normalizePath(String path) {
    path = path.replace("\\", "/"); 
    
    if(path.startsWith("/")) {
      path = path.substring(1);	
    }
    
    if(path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    
    return path; 
  }  
  
}
