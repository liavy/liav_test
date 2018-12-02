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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebservicesExtType;
import com.sap.engine.services.webservices.server.deploy.migration.ws.MigrationController;
import com.sap.engine.services.webservices.server.deploy.migration.ws.WSModuleMigrationResult;
import com.sap.engine.services.webservices.server.deploy.ws.notification.WSDeployNotificationHandler;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WSApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WebServicesJ2EEEngineDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaData;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaDataRegistry;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WS630DeployProcess
 * Description: WS630DeployProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WS630DeployProcess extends WSAbstractDProcess {
      
  private File[] archiveFiles;  
  private ConfigurationHandler configurationHandler; 
  private MigrationController migrationController;  
  private Hashtable moduleMigrationResults;
  private ApplicationDeployInfo applicationDeployInfo;

  public WS630DeployProcess(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, File[] archiveFiles, Configuration appConfiguration, RuntimeProcessingEnvironment runtimeProcessingEnvironment, MigrationController migrationController) {    
    this.applicationName = applicationName;
    this.webServicesContainerDir = webServicesContainerDir; 
    this.webServicesContainerTempDir = webServicesContainerTempDir;     
    this.appConfiguration = appConfiguration;   
    this.serviceContext = new ServiceContext(); 
    this.wsDNotificationHandler = new WSDeployNotificationHandler(applicationName, (ServiceContext)serviceContext, runtimeProcessingEnvironment);
    this.archiveFiles = archiveFiles;
    this.migrationController = migrationController;     
  }

  /**
   * @return - a hashtable of module migration results
   */
  public Hashtable getModuleMigrationResults() {
    if(moduleMigrationResults == null) {
      moduleMigrationResults = new Hashtable(); 
    }
    
    return moduleMigrationResults;
  } 
  
  /**
   * @return AppliationDeployInfo 
   */
  public ApplicationDeployInfo getApplicationDeployInfo() {
    return applicationDeployInfo;
  }    
  
  private ConfigurationHandler getConfigurationHandler() throws ConfigurationException {
    if(this.configurationHandler == null) {
      this.configurationHandler = WSContainer.getServiceContext().getCoreContext().getConfigurationHandlerFactory().getConfigurationHandler();  	
    }  
    
    return this.configurationHandler;
  }

  public void preProcess() throws WSDeploymentException, WSWarningException {    
  
  }
    
  public void init() throws WSDeploymentException {            
  
  }
  
  public void execute() throws WSDeploymentException {       
    generateAndRegisterDeployFiles();   
    loadApplicationMetaData();
    loadWebServicesJ2EEEngineDescriptors();        
  }

  public void finish() throws WSDeploymentException {           
    upload();           
    
    //uploadWSData();
      
    setApplicationDeployInfo();
    
    try {
      wsDNotificationHandler.onExecutePhase();
    } catch(WSWarningException e) {
      getApplicationDeployInfo().addWarnings(e.getWarnings()); 	
    }
  } 
   
  public void postProcess() throws WSDeploymentException, WSWarningException {      
    wsDNotificationHandler.onPostPhase();            
  }
  
  public void notifyProcess() throws WSWarningException {
	  
  }  
  
  public void commitProcess() throws WSWarningException {    
    ArrayList<String> warnings = new ArrayList<String>(); 
    
	try {     
	  if(configurationHandler != null) {
	    configurationHandler.commit(); 	  
	  }   	     
	} catch(ConfigurationException e) {
	  Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.WARNING, "", e);	
    } finally {
      try { 	
    	if(configurationHandler != null) {
    	  configurationHandler.closeAllConfigurations(); 	  
    	}	
      } catch(ConfigurationException e) {
        // $JL-EXC$  	  
      }	
    } 
	  
    try {     
      wsDNotificationHandler.onCommitPhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);           
    }
        
    try {
      IOUtil.deleteDir(new File(webServicesContainerTempDir));
    } catch(IOException e) {
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
  	  if(configurationHandler != null) {
  	    configurationHandler.rollback(); 	  
  	  }   	     
  	} catch(ConfigurationException e) {
  	  Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.WARNING, "", e);	
    } finally {
      try { 	
        if(configurationHandler != null) {
          configurationHandler.closeAllConfigurations(); 	  
        }	
      } catch(ConfigurationException e) {
        // $JL-EXC$  	  
      }	
    }
	  
	try {    
      wsDNotificationHandler.onRollbackPhase();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);       
    }    
        
    try {
      IOUtil.deleteDir(new File(webServicesContainerTempDir));
    } catch(IOException e) {
        // $JL-EXC$
    	// TODO - trace	
    }
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException(); 
      e.setWarning(warnings.toArray(new String[warnings.size()])); 
      throw e; 
    }
  }
  
  protected void loadWebServicesJ2EEEngineDescriptors(String applicationName, WebservicesType webServicesJ2EEEngineDescriptor, WebservicesExtType webServicesJ2EEEngineExtDescriptor, ModuleRuntimeData moduleRuntimeData) throws WSDeploymentException {                
    Hashtable moduleMigrationResults = getModuleMigrationResults(); 
    WSModuleMigrationResult moduleMigrationResult = (WSModuleMigrationResult)moduleMigrationResults.get(moduleRuntimeData.getModuleName());  
    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    
    try {
      ConfigurationRoot configurationDescriptor = moduleMigrationResult.getConfigurationDescriptor();
      wsApplicationDescriptorContext.getConfigurationDescriptorRegistry().putConfigurationDescriptor(moduleRuntimeData.getModuleName(), configurationDescriptor);
      
      MappingRules mappingDescriptor  = moduleMigrationResult.getMappingDescriptor();
      wsApplicationDescriptorContext.getMappingDescriptorRegistry().putMappingDescriptor(moduleRuntimeData.getModuleName(), mappingDescriptor);
         
      loadWebServicesJ2EEEngineDescriptors(applicationName, webServicesJ2EEEngineDescriptor, webServicesJ2EEEngineExtDescriptor, configurationDescriptor, mappingDescriptor, moduleRuntimeData);
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);   
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_LOAD_630, new Object[]{applicationName, moduleRuntimeData.getModuleName()}, e);
    }   
  }
  
  private void generateAndRegisterDeployFiles() throws WSDeploymentException {
    Hashtable moduleMigrationResults = generateDeployFiles(applicationName, archiveFiles); 
    
    if(moduleMigrationResults != null && moduleMigrationResults.size() != 0) {
      getModuleMigrationResults().putAll(moduleMigrationResults);
    }      
  }
     
  private Hashtable generateDeployFiles(String applicationName, File[] archiveFiles) throws WSDeploymentException {
    if(archiveFiles == null || archiveFiles.length == 0) {
      return new Hashtable(); 
    } 
  
    Hashtable moduleMigrationResults = null; 
    try {
      moduleMigrationResults = migrationController.deployNW04Archives(applicationName, webServicesContainerDir, archiveFiles);
      saveArchiveFiles(webServicesContainerDir + "/" + WSApplicationMetaDataContext.BACKUP, archiveFiles, moduleMigrationResults);       
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_GEN_630, new Object[]{applicationName}, e);
    } 
    
    return moduleMigrationResults;             
  }   
    
  private void loadApplicationMetaData() {    
    Hashtable moduleMigrationResults = getModuleMigrationResults();
    if(moduleMigrationResults == null || moduleMigrationResults.size() == 0) {
      return;      
    }
    
    Enumeration enum1 = moduleMigrationResults.elements(); 
    WSModuleMigrationResult moduleMigrationResult; 
    while(enum1.hasMoreElements()) {
      moduleMigrationResult =(WSModuleMigrationResult)enum1.nextElement();       
      loadModuleMetaData(moduleMigrationResult);   
    }
  } 
  
  private void loadModuleMetaData(WSModuleMigrationResult moduleMigrationResult) {    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().put(applicationName, new WSApplicationDescriptorContext());
      wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    }
    
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    if(applicationMetaDataContext == null) {
      getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().put(applicationName, new WSApplicationMetaDataContext());
      applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    } 
 
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = applicationMetaDataContext.getModuleRuntimeDataRegistry();
    WebServicesJ2EEEngineDescriptorRegistry webServicesJ2EEEngineDescriptorRegistry = wsApplicationDescriptorContext.getWebServicesJ2EEEngineDescriptorRegistry(); 
    
    String moduleName = moduleMigrationResult.getModuleName();
    ModuleRuntimeData moduleRuntimeData = new ModuleRuntimeData(moduleName, moduleMigrationResult.getModuleDir(), webServicesContainerTempDir + "/" + ModuleRuntimeData.getModuleDirName(moduleName));
    moduleRuntimeData.setArchiveFileRelPath(moduleName);
    moduleRuntimeDataRegistry.putModuleRuntimeData(moduleName, moduleRuntimeData);
    webServicesJ2EEEngineDescriptorRegistry.putWebServicesJ2EEEngineDescriptor(moduleName, moduleMigrationResult.getWebservicesJ2EEEngineDescriptor());      
  }  
  
  private void setApplicationDeployInfo() {
    this.applicationDeployInfo = makeApplicationDeployInfo(applicationName); 
  }
  
  private ApplicationDeployInfo makeApplicationDeployInfo(String applicationName) {    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return new ApplicationDeployInfo();
    }
       
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);       
    ServiceMetaDataRegistry serviceMetaDataRegistry = applicationMetaDataContext.getServiceMetaDataRegistry();                      
    ServiceDescriptorRegistry serviceDescriptorRegistry = wsApplicationDescriptorContext.getServiceDescriptorRegistry();
    Vector serviceNames = new Vector();
    String[] filesForClassLoader = new String[0];
    
    Enumeration enum1 = serviceDescriptorRegistry.getServiceDescriptors().keys();
    WebserviceDescriptionType serviceDescriptor; 
    ModuleRuntimeData moduleRuntimeData;      
    ServiceMetaData serviceMetaData;      
    while(enum1.hasMoreElements()) {
      String serviceName = (String)enum1.nextElement();
      serviceDescriptor = (WebserviceDescriptionType)serviceDescriptorRegistry.getServiceDescriptor(serviceName);       
      serviceMetaData = serviceMetaDataRegistry.getServiceMetaData(serviceName);   
      if(serviceMetaData.getApplicationName().equals(applicationName)) {        
        moduleRuntimeData = (ModuleRuntimeData)applicationMetaDataContext.getModuleRuntimeDataRegistry().getModuleRuntimeData(serviceMetaData.getModuleName());
        serviceNames.add(serviceName);  
        String[] serviceFilesForClassLoader = getServiceFilesForClassLoader(serviceDescriptor, moduleRuntimeData);
        String[] filesForClassLoaderAll = new String[filesForClassLoader.length + serviceFilesForClassLoader.length];
        System.arraycopy(filesForClassLoader, 0, filesForClassLoaderAll, 0, filesForClassLoader.length);
        System.arraycopy(serviceFilesForClassLoader, 0, filesForClassLoaderAll, filesForClassLoader.length, serviceFilesForClassLoader.length);
        filesForClassLoader = filesForClassLoaderAll;    
      }                                                                             
    } 
    
    ApplicationDeployInfo applicationDeployInfo = new ApplicationDeployInfo();
    String[] serviceNamesArr = new String[serviceNames.size()];
    serviceNames.copyInto(serviceNamesArr);      
    applicationDeployInfo.setDeployedComponentNames(serviceNamesArr);
    applicationDeployInfo.setFilesForClassloader(filesForClassLoader);
    
    return applicationDeployInfo; 
  }     
  
  private void saveArchiveFiles(String destinationDir,  File[] archiveFiles, Hashtable moduleMigrationResults) throws IOException {
    if(moduleMigrationResults == null || moduleMigrationResults.size() == 0) {
      return; 
    }	  
     
    WSModuleMigrationResult moduleMigrationResult;
    File backupArchiveFile; 
    for(File archiveFile: archiveFiles) {
      moduleMigrationResult = (WSModuleMigrationResult)moduleMigrationResults.get(archiveFile.getName());
      if(moduleMigrationResult != null) {
    	backupArchiveFile = new File(destinationDir + "/" + moduleMigrationResult.getModuleName());
    	backupArchiveFile.getParentFile().mkdirs(); 
        IOUtil.copyFile(archiveFile, backupArchiveFile);
      }
    }    
    
  }
  
  private void uploadWSData() throws WSDeploymentException {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
     
    try {
      ConfigurationHandler configurationHandler = getConfigurationHandler();	             
      Configuration appConfiguration = createApplicationConfiguration(applicationName, configurationHandler); 
      uploadWSData(applicationName, appConfiguration);             	               	      
    } catch(ConfigurationException e) {
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_UPLOAD, new Object[]{applicationName}, e);  	
    } 
  }
  
}
