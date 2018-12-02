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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.jar.JarFile;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLMarshalException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ServiceRefGroupDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WSClientsJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WsClientsType;
import com.sap.engine.services.webservices.server.deploy.j2ee.ws.J2EE14WSClientConvertor;
import com.sap.engine.services.webservices.server.deploy.wsclients.notification.WSClientsDeployNotificationHandler;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.mapping.ServiceMappingRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.wsclients.ServiceRefContext;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.ServiceRefGroupDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.WSClientsApplicationMetaDataContext;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.lib.javalang.tool.ReadResult;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WSClientsDeployProcess  
 * Description: WSClientsDeployProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSClientsDeployProcess extends WSClientsAbstractDProcess {
  
  protected File[] archiveFiles; 
  protected JarUtil jarUtil;   
  private Hashtable webModuleMappings; 
  private ApplicationDeployInfo applicationDeployInfo;
  private J2EE14WSClientConvertor wsClientsJ2EE14Converter;  
  private WSClients630Convertor wsClients630Convertor; 
  
  public WSClientsDeployProcess(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, String classPath, File[] archiveFiles, Configuration appConfiguration, ReadResult annotationsResult, Hashtable webModuleMappings) {    
    this.applicationName = applicationName; 
    this.webServicesContainerDir = webServicesContainerDir; 
    this.webServicesContainerTempDir = webServicesContainerTempDir;    
    this.appConfiguration = appConfiguration;  
    this.serviceContext = new ServiceRefContext();
    this.wsClientsDNotificationHandler = new WSClientsDeployNotificationHandler(applicationName, WSContainer.getRuntimeProcessingEnv());
    this.archiveFiles = archiveFiles;
    this.jarUtil = new JarUtil();
    this.webModuleMappings = webModuleMappings; 
    this.wsClientsJ2EE14Converter = new J2EE14WSClientConvertor(annotationsResult, webModuleMappings);       
  }
  
  /**
   * @return ApplicationDeployInfo
   */
  public ApplicationDeployInfo getApplicationDeployInfo() {    
    return applicationDeployInfo;        
  }
  
  private WSClients630Convertor getWSClients630Convertor() throws WSDLMarshalException {
    if(wsClients630Convertor == null) {
      wsClients630Convertor = new WSClients630Convertor(); 	
    }  
    
    return wsClients630Convertor; 
  }
   
  public void preProcess() throws WSDeploymentException, WSWarningException {

  }

  public void init() throws WSDeploymentException {    
    extractWSClientsJ2EEEngineDescriptors(archiveFiles);       
  }

  public void execute() throws WSDeploymentException {    
    loadWSClientsJ2EEEngineDescriptors();
    checkServiceRefJNDINames(); 
  }

  public void finish() throws WSDeploymentException {
    upload();   
        
    setApplicationDeployInfo(); 
    
    try {
      wsClientsDNotificationHandler.onExecutePhase();
    } catch(WSWarningException e) {
      getApplicationDeployInfo().addWarnings(e.getWarnings()); 	
    }
  }
  
  public void postProcess() throws WSDeploymentException, WSWarningException {    
    wsClientsDNotificationHandler.onPostPhase();   	 
  }
  
  public void notifyProcess() throws WSWarningException {
  
  }

  public void commitProcess() throws WSWarningException {    
    ArrayList<String> warnings = new ArrayList<String>();  
	
    try {
      wsClientsDNotificationHandler.onCommitPhase();
    } catch(WSWarningException e)  {
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }  
    
    try {    
      IOUtil.deleteDir(webServicesContainerTempDir);
    } catch(Exception e) {
      // $JL-EXC$
      // TODO - add trace
    } 
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException(); 
      e.setWarning(warnings.toArray(new String[warnings.size()])); 
      throw e; 
    }        
  }

  public void rollbackProcess() throws WSWarningException {
    ArrayList<String> warnings = new ArrayList<String>();   
    
	try {
      wsClientsDNotificationHandler.onRollbackPhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings); 	      
    }  
    
    try {    
      IOUtil.deleteDir(webServicesContainerTempDir);
    } catch(Exception e) {
      // $JL-EXC$
      //TODO - trace      
    }
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException(); 
      e.setWarning(warnings.toArray(new String[warnings.size()])); 
      throw e; 
    }    
  }

  private void extractWSClientsJ2EEEngineDescriptors(File[] archiveFiles) throws WSDeploymentException {
    if(archiveFiles == null || archiveFiles.length == 0) {
      return; 
    }  
       
    for(int i = 0; i < archiveFiles.length; i++) {      
      extractWSClientsJ2EEEngineDescriptors(archiveFiles[i]);
    }     
  }
  
  private void extractWSClientsJ2EEEngineDescriptors(File archiveFile) throws WSDeploymentException {
    if(archiveFile == null) {
      return; 
    } 
      
    JarFile archiveJarFileOrig = null;
    JarFile archiveJarFile = null;  
    try {
      archiveJarFileOrig = new JarFile(archiveFile);      
      archiveJarFile = wsClientsJ2EE14Converter.convert(archiveJarFileOrig, new File(webServicesContainerTempDir), applicationName);             
      
      File archiveFileNew = getWSClients630Convertor().convert(webServicesContainerTempDir, archiveFile, applicationName, (String)webModuleMappings.get(archiveFile.getName())); 
      if(archiveFileNew != null) {
        archiveJarFile = new JarFile(archiveFileNew); 	  
      }
      
      if(isWSClientsJ2EEEngineArchive(archiveJarFile)) {            
        WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
        if(wsClientsApplicationMetaDataContext == null) {
          wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().put(applicationName, new WSClientsApplicationMetaDataContext());      
          wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName); 
        }
         
        String moduleName = archiveFile.getName(); 
        ModuleRuntimeData moduleRuntimeData = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry().getModuleRuntimeData(moduleName);      
        if(moduleRuntimeData == null) {
          moduleRuntimeData = new ModuleRuntimeData(moduleName, moduleName, webServicesContainerDir, webServicesContainerTempDir, true);                 
          wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry().putModuleRuntimeData(moduleRuntimeData.getModuleName(), moduleRuntimeData);
        }
                
        File backupArchiveFile = new File(webServicesContainerDir + "/" + WSApplicationMetaDataContext.BACKUP + "/" + moduleRuntimeData.getArchiveFileRelPath()); 
        if(!backupArchiveFile.exists()) {        
          backupArchiveFile.getParentFile().mkdirs(); 
          backupArchiveFile.createNewFile();  
          IOUtil.copyFile(archiveFile, backupArchiveFile);        
        }         
        
        extractWSClientsJ2EEEngineDescriptors(moduleRuntimeData, archiveJarFile);    
      } 
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_EXTRACT, new Object[]{applicationName, archiveFile.getName()}, e);
    } finally {
      try {
        if(archiveJarFileOrig != null) {
          archiveJarFileOrig.close();         
        }
      } catch(IOException e) {
        // $JL-EXC$
      }  
      try {
        if(archiveJarFile != null) {
          archiveJarFile.close();         
        }
      } catch(IOException e) {
        // $JL-EXC$
      }
    }
  }
    
  private void extractWSClientsJ2EEEngineDescriptors(ModuleRuntimeData moduleRuntimeData, JarFile archiveJarFile) throws IOException, TypeMappingException {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().put(applicationName, new WSClientsApplicationDescriptorContext());      
      wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName); 
    }
       
    String moduleDir = moduleRuntimeData.getModuleDir();
                       
    String wsClientsJ2EEEngineDescriptorRelPath = moduleRuntimeData.getMetaInfRelDir() + "/" + WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR;     
    jarUtil.extractFile(archiveJarFile, wsClientsJ2EEEngineDescriptorRelPath, moduleDir);           
    String wsClientsJ2EEEngineExtDescriptorRelPath = moduleRuntimeData.getMetaInfRelDir() + "/" + WS_CLIENTS_J2EE_ENGINE_EXT_DESCRIPTOR; 
    if(archiveJarFile.getEntry(wsClientsJ2EEEngineExtDescriptorRelPath) != null) {
      jarUtil.extractFile(archiveJarFile, wsClientsJ2EEEngineExtDescriptorRelPath, moduleDir); 
    }
        
    WsClientsType wsClientsJ2EEEngineDescriptor = WSClientsJ2EEEngineFactory.load(moduleDir + "/" + wsClientsJ2EEEngineDescriptorRelPath);
    wsClientsApplicationDescriptorContext.getWsClientsJ2EEEngineDescriptorRegistry().putWsClientsJ2EEEngineDescriptor(moduleRuntimeData.getModuleName(), wsClientsJ2EEEngineDescriptor); 
    
    extractServiceRefGroupDescriptors(moduleDir, wsClientsJ2EEEngineDescriptor.getServiceRefGroupDescription(), archiveJarFile);      
    
    jarUtil.extractFileWithPrefix(archiveJarFile, moduleRuntimeData.getMetaInfRelDir() + "/" + WSDL_DIR, moduleDir);
    
    if(moduleRuntimeData.getBinRelDir().equals("")) {
      jarUtil.extractFileWithPrefix(archiveJarFile, WSDL_DIR, moduleDir);    	
    } else {
      jarUtil.extractFileWithPrefix(archiveJarFile, moduleRuntimeData.getBinRelDir() + "/" + WSDL_DIR, moduleDir);  	
    }
    
    jarUtil.extractFileWithPrefix(archiveJarFile, "jars", moduleDir); 
  } 
  
  private void extractServiceRefGroupDescriptors(String moduleDir, ServiceRefGroupDescriptionType[] wsClientGroupDescriptors, JarFile archiveJarFile) throws IOException {
    if(wsClientGroupDescriptors == null || wsClientGroupDescriptors.length == 0) {
       return;
    }
    
    for(int i = 0; i < wsClientGroupDescriptors.length; i++) {        
      extractServiceRefGroupDescriptors(moduleDir, wsClientGroupDescriptors[i], archiveJarFile);      
    }
  }
  
  private void extractServiceRefGroupDescriptors(String destinationDir, ServiceRefGroupDescriptionType serviceRefGroupDescriptor, JarFile archiveFile) throws IOException {
    if(serviceRefGroupDescriptor.getConfigurationFile() != null) {
      jarUtil.extractFile(archiveFile, serviceRefGroupDescriptor.getConfigurationFile(), destinationDir);
    }    
      
    if(serviceRefGroupDescriptor.getWsdlMappingFile() != null) {
      jarUtil.extractFile(archiveFile, serviceRefGroupDescriptor.getWsdlMappingFile(), destinationDir);
    }                             
    
    TypeMappingFileType[] typeMappingFileDescriptors = serviceRefGroupDescriptor.getTypeMappingFile();       
    if(typeMappingFileDescriptors != null || typeMappingFileDescriptors.length != 0) {
	  for(int i = 0; i < typeMappingFileDescriptors.length; i++) {                  
        jarUtil.extractFile(archiveFile, typeMappingFileDescriptors[i].get_value().trim(), destinationDir);      
      }    
    }    
  }
    
  private void upload() throws WSDeploymentException {    
    try {   
      WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName); 
      if(wsClientsApplicationDescriptorContext == null) {
        return; 
      }      
            
      Configuration webServicesContainerConfiguration; 
      if(appConfiguration.existsSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME)) {
        webServicesContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME);      
      } else {
        webServicesContainerConfiguration = appConfiguration.createSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME);
      }
    
      uploadMetaData(webServicesContainerConfiguration);
      uploadBackUp(webServicesContainerConfiguration);       
      uploadFiles(webServicesContainerConfiguration);
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_UPLOAD, new Object[]{applicationName}, e);
    }
  }
  
  private void uploadBackUp(Configuration webServicesConfiguration) throws IOException, ConfigurationException {  
    if(archiveFiles == null || archiveFiles.length == 0) {
      return;
    }
    
    Configuration backUpConfiguration; 
    if(webServicesConfiguration.existsSubConfiguration(WSApplicationMetaDataContext.BACKUP)) {
      backUpConfiguration = webServicesConfiguration.getSubConfiguration(WSApplicationMetaDataContext.BACKUP); 
    } else {
      backUpConfiguration = webServicesConfiguration.createSubConfiguration(WSApplicationMetaDataContext.BACKUP); 
    }
    
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry(); 
    
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
  
  private void uploadMetaData(Configuration webServicesContainerConfiguration) throws ConfigurationException {    
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    if(wsClientsApplicationMetaDataContext == null) {
      return; 
    }
        
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry();
    
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

  private void uploadFiles(Configuration webServicesConfiguration) throws IOException, ConfigurationException {
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    if(wsClientsApplicationMetaDataContext == null) {
      return; 
    }    
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry();       
    
    Enumeration enum1 = moduleRuntimeDataRegistry.getModuleRuntimeDatas().elements(); 
    ModuleRuntimeData moduleRuntimeData; 
    String moduleDirName; 
    File moduleDir;     
    while(enum1.hasMoreElements()) {
      moduleRuntimeData = (ModuleRuntimeData)enum1.nextElement();        
      moduleDir = new File(moduleRuntimeData.getModuleDir()); 
      moduleDirName = moduleDir.getName();
      String moduleDirPath = moduleDir.getAbsolutePath();                             
      if(webServicesConfiguration.existsSubConfiguration(moduleDirName)) {
        webServicesConfiguration.deleteSubConfigurations(new String[]{moduleDirName});        
      }       
      uploadDirectory(moduleDir, webServicesConfiguration, true);      
    } 
  }   
  
  private void setApplicationDeployInfo() {
    this.applicationDeployInfo = makeApplicationDeployInfo();
  }
  
  private ApplicationDeployInfo makeApplicationDeployInfo() {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      return new ApplicationDeployInfo(); 
    }  
    
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);  
    ServiceRefGroupDescriptorRegistry serviceRefGroupDescriptorsRegistry = wsClientsApplicationDescriptorContext.getServiceRefGroupDescriptorRegistry(); 
    String[] serviceRefGroupNames = new String[serviceRefGroupDescriptorsRegistry.getServiceRefGroupDescriptors().size()];
    Enumeration enumer = serviceRefGroupDescriptorsRegistry.getServiceRefGroupDescriptors().keys();       
    int i = 0; 
    while(enumer.hasMoreElements()) {
      serviceRefGroupNames[i++] = (String)enumer.nextElement();       
    }
        
    ApplicationDeployInfo applicationDeployInfo = new ApplicationDeployInfo(); 
    applicationDeployInfo.setDeployedComponentNames(serviceRefGroupNames);       
    applicationDeployInfo.setFilesForClassloader(getModuleFilesForClassLoader(wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry()));      
      
    return applicationDeployInfo; 
  } 
  
  private String[] getServiceRefGroupNames() {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      return new String[0]; 
    }  
  
    ServiceRefGroupDescriptorRegistry serviceRefGroupDescriptorsRegistry = wsClientsApplicationDescriptorContext.getServiceRefGroupDescriptorRegistry(); 
    String[] serviceRefGroupNames = new String[serviceRefGroupDescriptorsRegistry.getServiceRefGroupDescriptors().size()];
    Enumeration enum1 = serviceRefGroupDescriptorsRegistry.getServiceRefGroupDescriptors().keys();       
    int i = 0; 
    while(enum1.hasMoreElements()) {
      serviceRefGroupNames[i++] = (String)enum1.nextElement();       
    }
    
    return serviceRefGroupNames;    
  }
  
  private void checkServiceRefJNDINames() throws WSDeploymentException {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    if(wsClientsApplicationDescriptorContext == null) {
      return;  
    } 
    
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)getServiceContext().getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();
    ServiceMappingRegistry serviceMappingRegistry = getServiceContext().getMappingContext().getServiceMappingRegistry();  
   
    Set<String> serviceRefJNDINames = new HashSet<String>();  
    Enumeration<ServiceMapping> enumer = serviceMappingRegistry.getServiceMappings().elements();         
    ServiceMapping serviceMapping; 
    String serviceRefJNDIName; 
    while(enumer.hasMoreElements()) {
      serviceMapping = enumer.nextElement(); 
      serviceRefJNDIName = serviceMapping.getImplementationLink().getServiceRefJNDIName(); 
      if(serviceRefJNDINames.contains(serviceRefJNDIName)) {
        throw new WSDeploymentException(""); 	  
      } else {
        serviceRefJNDINames.add(serviceRefJNDIName); 	  
      }
    }
            
    return; 
  }
  
  private boolean isWSClientsJ2EEEngineArchive(JarFile archiveJarFile) {
    String moduleType = ModuleRuntimeData.getType(archiveJarFile.getName());
    if(moduleType.equals(ModuleRuntimeData.EJB_SUFFIX)) {
      return archiveJarFile.getEntry(ModuleRuntimeData.META_INF + "/" + WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR) != null;
    } 
    if(moduleType.equals(ModuleRuntimeData.WEB_SUFFIX)) {
      return archiveJarFile.getEntry(ModuleRuntimeData.WEB_INF + "/" + WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR) != null;
    }
    if(moduleType.equals(ModuleRuntimeData.WS_SUFFIX)) {
      return archiveJarFile.getEntry(ModuleRuntimeData.META_INF + "/" + WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR) != null;
    }
    
    return archiveJarFile.getEntry(ModuleRuntimeData.META_INF + "/" + WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR) != null;      
  }
  
}
