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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarFile;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.sap.bc.proj.jstartup.sadm.ShmAccessPoint;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaConfig;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGeneratorNew;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.container.configuration.ApplicationConfigurationContext;
import com.sap.engine.services.webservices.server.container.configuration.InterfaceDefinitionRegistry;
import com.sap.engine.services.webservices.server.container.configuration.ServiceRegistry;
import com.sap.engine.services.webservices.server.container.mapping.InterfaceMappingRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.ApplicationServiceTypeMappingContext;
import com.sap.engine.services.webservices.server.container.ws.ServiceContext;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.ServiceExtDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.ws.descriptors.WSApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaData;
import com.sap.engine.services.webservices.server.container.ws.metaData.ServiceMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.ApplicationWSDLContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.WSDLContext;
import com.sap.engine.services.webservices.server.container.ws.wsdl.WSDLRegistry;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.InterfaceDefinitionDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.SchemaStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.SchemaTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineExtFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebservicesExtType;
import com.sap.engine.services.webservices.server.deploy.j2ee.ws.J2EE14Convertor;
import com.sap.engine.services.webservices.server.deploy.ws.notification.WSDeployNotificationHandler;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.lib.javalang.tool.ReadResult;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WSDeployProcess
 * Description: WSDeployProcess 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSDeployProcess extends WSAbstractDProcess {  
    
  private String classPath;
  private File[] archiveFiles; 
  private JarUtil jarUtil;
  private JarUtils jarUtils;
  private PackageBuilder packageBuilder;
  private ConfigurationHandler configurationHandler; 
  private J2EE14Convertor j2ee14Convertor;  
  private WSAltConvertor wsAltConvertor;
  private ApplicationDeployInfo applicationDeployInfo;  

  public WSDeployProcess(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, String classPath, File[] archiveFiles, Configuration appConfiguration, RuntimeProcessingEnvironment runtimeProcessingEnvironment, Hashtable webModuleMappings, ReadResult annotationsResult) {    
    this.applicationName = applicationName; 
    this.webServicesContainerDir = webServicesContainerDir; 
    this.webServicesContainerTempDir = webServicesContainerTempDir;    
    this.appConfiguration = appConfiguration;  
    this.serviceContext = new ServiceContext();
    this.wsDNotificationHandler = new WSDeployNotificationHandler(this.applicationName, (ServiceContext)this.serviceContext, runtimeProcessingEnvironment);    
    this.classPath = classPath; 
    this.archiveFiles = archiveFiles;
    this.jarUtil = new JarUtil();
    this.jarUtils = new JarUtils();
    this.packageBuilder = new PackageBuilder();       
    this.j2ee14Convertor = new J2EE14Convertor(applicationName, annotationsResult, webModuleMappings);    
  }

  /**
   * @return ApplicationDeployInfo
   */
  public ApplicationDeployInfo getApplicationDeployInfo() {    
    return this.applicationDeployInfo;        
  }
  
  private WSAltConvertor getWSAltConvertor() throws Exception {
   if(wsAltConvertor == null) {
      wsAltConvertor = new WSAltConvertor(WSContainer.createInitializedServerCFGFactory()); 	
    }
    
    return wsAltConvertor;     
  }
  
  protected ConfigurationHandler getConfigurationHandler() throws ConfigurationException {
    if(this.configurationHandler == null) {
      this.configurationHandler = WSContainer.getServiceContext().getCoreContext().getConfigurationHandlerFactory().getConfigurationHandler();  	
    }  
    
    return this.configurationHandler;
  }
   
  public void preProcess() throws WSDeploymentException, WSWarningException {    
  
  }
    
  public void init() throws WSDeploymentException {                     
    extractWebServicesJ2EEEngineDescriptors();            
  }

  public void execute() throws WSDeploymentException {    	  
    loadWebServicesJ2EEEngineDescriptors();      
    generateSerializationFramework();
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
      publishWSDL(); 
    } catch(Exception e) {
      // TODO - add message  
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));
      warnings.add(strWriter.toString());                             
    }         
    
    try {    
      IOUtil.deleteDir(new File(webServicesContainerTempDir));
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
    } catch(Exception e) {                  
      // $JL-EXC$
      // TODO - trace    	
    }                     
      
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException(); 
      e.setWarning(warnings.toArray(new String[warnings.size()]));
      throw e;  
    }        
  }
 
  protected void extractWebServicesJ2EEEngineDescriptors() throws WSDeploymentException {    
    if(archiveFiles == null || archiveFiles.length == 0)  {
      return;
    }
    
    for (int i = 0; i < archiveFiles.length; i++) {           
      extractWebServicesJ2EEEngineDescriptors(applicationName, archiveFiles[i]);      
    }                  
  }
  
  protected void extractWebServicesJ2EEEngineDescriptors(String applicationName, File archiveFile) throws WSDeploymentException {
    if(archiveFile == null) {
      return; 
    } 
    
    String moduleName = archiveFile.getName(); 
    String moduleType = (String)ModuleRuntimeData.moduleMappingTable.get(moduleName.substring(moduleName.lastIndexOf(".")));
    
    JarFile archiveJarFileOrig = null; 
    JarFile archiveJarFile = null;    
    try {
      archiveJarFileOrig = new JarFile(archiveFile);
      archiveJarFile = archiveJarFileOrig; 
      
      if(moduleType.equals(ModuleRuntimeData.EJB_SUFFIX) || moduleType.equals(ModuleRuntimeData.WEB_SUFFIX)) {
        archiveJarFile = j2ee14Convertor.convertJ2EEModule(archiveJarFileOrig, webServicesContainerTempDir);
      }
      
      File archiveFileNew = getWSAltConvertor().convert(archiveFile, webServicesContainerTempDir);
      if(archiveFileNew != null) {
        archiveJarFile = new JarFile(archiveFileNew); 	  
      }      
                   
      if(archiveJarFile.getEntry(META_INF + "/" + WEBSERVICES_J2EE_ENGINE_DESCRIPTOR) != null) {                                      
        ModuleRuntimeData moduleRuntimeData = new ModuleRuntimeData(moduleName, moduleName, webServicesContainerDir, webServicesContainerTempDir, true);
        String moduleDir = moduleRuntimeData.getModuleDir();             
    
        File backupArchiveFile = new File(webServicesContainerDir + "/" + WSApplicationMetaDataContext.BACKUP + "/" + moduleRuntimeData.getArchiveFileRelPath()); 
        backupArchiveFile.getParentFile().mkdirs();          
        IOUtil.copyFile(archiveFile, backupArchiveFile);        

        extractWebServicesJ2EEEngineDescriptors(applicationName, moduleRuntimeData, archiveJarFile);
      }    
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_EXTRACT, new Object[]{applicationName, moduleName}, e);
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
  
  protected void extractWebServicesJ2EEEngineDescriptors(String applicationName, ModuleRuntimeData moduleRuntimeData, JarFile archiveFile) throws IOException, TypeMappingException {                         
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
    
    String moduleName = moduleRuntimeData.getModuleName();      
    String moduleDir = moduleRuntimeData.getModuleDir();           
    applicationMetaDataContext.getModuleRuntimeDataRegistry().putModuleRuntimeData(moduleRuntimeData.getModuleName(), moduleRuntimeData);        
                  
    String webServicesJ2EEEngineDescriptorRelPath = META_INF + "/" + WEBSERVICES_J2EE_ENGINE_DESCRIPTOR;           
    jarUtil.extractFile(archiveFile, webServicesJ2EEEngineDescriptorRelPath, moduleDir);
    
    String srPublicationMetaDataDescriptorFileRelPath = META_INF + "/" + SR_PUBLICATION_METADATA_DESCRIPTOR;
    if(archiveFile.getEntry(srPublicationMetaDataDescriptorFileRelPath) != null) {
      jarUtil.extractFile(archiveFile, srPublicationMetaDataDescriptorFileRelPath, moduleDir);	
    }
                      
    WebservicesType webServicesJ2EEEngineDescriptor = WebServicesJ2EEEngineFactory.load(moduleDir + "/" + webServicesJ2EEEngineDescriptorRelPath);
    wsApplicationDescriptorContext.getWebServicesJ2EEEngineDescriptorRegistry().putWebServicesJ2EEEngineDescriptor(moduleName, webServicesJ2EEEngineDescriptor);       
    
    String webServicesJ2EEEngineExtDescriptorRelPath = META_INF + "/" + WEBSERVICES_J2EE_ENGINE_EXT_DESCRIPTOR;  
    if(archiveFile.getEntry(webServicesJ2EEEngineExtDescriptorRelPath) == null) {
      webServicesJ2EEEngineExtDescriptorRelPath = "meta-inf" + "/" + WEBSERVICES_J2EE_ENGINE_EXT_DESCRIPTOR;
      if(archiveFile.getEntry(webServicesJ2EEEngineExtDescriptorRelPath) == null) {
        webServicesJ2EEEngineExtDescriptorRelPath = "web-inf" + "/" + WEBSERVICES_J2EE_ENGINE_EXT_DESCRIPTOR;
        if(archiveFile.getEntry(webServicesJ2EEEngineExtDescriptorRelPath) == null) {
          webServicesJ2EEEngineExtDescriptorRelPath = "WEB-INF" + "/" + WEBSERVICES_J2EE_ENGINE_EXT_DESCRIPTOR;
        }
      }
    }
    
    if(archiveFile.getEntry(webServicesJ2EEEngineExtDescriptorRelPath) != null) {
      jarUtil.extractFile(archiveFile, webServicesJ2EEEngineExtDescriptorRelPath, moduleDir); 
      WebservicesExtType webServicesJ2EEEngineExtDescriptor = WebServicesJ2EEEngineExtFactory.load(moduleDir + "/" + webServicesJ2EEEngineExtDescriptorRelPath);
      wsApplicationDescriptorContext.getWebServicesJ2EEEngineExtDescriptorRegistry().putWebServicesJ2EEEngineExtDescriptor(moduleName, webServicesJ2EEEngineExtDescriptor);
    } 
    
    if(webServicesJ2EEEngineDescriptor.getConfigurationFile() != null) {
      jarUtil.extractFile(archiveFile, webServicesJ2EEEngineDescriptor.getConfigurationFile(), moduleDir);
    }    
    
    if(webServicesJ2EEEngineDescriptor.getWsdlMappingFile() != null) {
      jarUtil.extractFile(archiveFile, webServicesJ2EEEngineDescriptor.getWsdlMappingFile(), moduleDir);
    }         
    
    extractServiceDescriptors(moduleDir, webServicesJ2EEEngineDescriptor.getWebserviceDescription(), archiveFile);
    extractInterfaceDefinitionDescriptors(moduleDir, webServicesJ2EEEngineDescriptor.getInterfaceDefinitionDescription(), archiveFile);
    
    jarUtil.extractFileWithPrefix(archiveFile, META_INF + "/" + WSDL_DIR, moduleDir);
    jarUtil.extractFileWithPrefix(archiveFile, WEB_INF + "/" + WSDL_DIR, moduleDir);
  }  
  
  private void extractServiceDescriptors(String destinationDir, WebserviceDescriptionType[] serviceDescriptors, JarFile archiveFile) throws IOException {
    if(serviceDescriptors == null) {
      return;
    }   
    
    WebserviceDescriptionType serviceDescriptor;
    for(int i = 0; i < serviceDescriptors.length; i++) {
      serviceDescriptor = serviceDescriptors[i];      
      extractServiceDescriptors(destinationDir, serviceDescriptor, archiveFile);                                   
    }
  } 
  
  private void extractServiceDescriptors(String destinationDir, WebserviceDescriptionType serviceDescriptior, JarFile archiveFile) throws IOException {        
    if(serviceDescriptior.getTypeMappingFile() != null) {
      TypeMappingFileType[] typeMappingFileDescriptors = serviceDescriptior.getTypeMappingFile();
      TypeMappingFileType typeMappingFileDescriptor;
      for(int i = 0; i < typeMappingFileDescriptors.length; i++) {                   
        typeMappingFileDescriptor = typeMappingFileDescriptors[i];
        if(!(typeMappingFileDescriptor.get_value().trim().indexOf("#")!= -1)) {                                               
          jarUtil.extractFile(archiveFile, typeMappingFileDescriptor.get_value().trim(), destinationDir);
        }
      }
    }
    
    if(serviceDescriptior.getTypesArchiveFile()!= null) {
      TypeMappingFileType[] typesArchiveFileDescriptors = serviceDescriptior.getTypesArchiveFile();
      TypeMappingFileType typesArchiveFileDescriptor; 
      for(int i = 0; i < typesArchiveFileDescriptors.length; i++) {
        typesArchiveFileDescriptor = typesArchiveFileDescriptors[i];    
        if(!(typesArchiveFileDescriptor.get_value().trim().indexOf("#") != -1)) {                
          jarUtil.extractFile(archiveFile, typesArchiveFileDescriptors[i].get_value().trim(), destinationDir);
        }
      }
    }                             
  } 
  
  private void extractInterfaceDefinitionDescriptors(String destinationDir, InterfaceDefinitionDescriptionType[] interfaceDefinitionDescriptors, JarFile archiveFile) throws IOException {
    if(interfaceDefinitionDescriptors == null || interfaceDefinitionDescriptors.length == 0) {
      return; 	
    }	 
    
    for(InterfaceDefinitionDescriptionType interfaceDefinitionDescriptor: interfaceDefinitionDescriptors) {
      extractInterfaceDefinitionDescriptor(destinationDir, interfaceDefinitionDescriptor, archiveFile);	
    }	  
  }
  
  private void extractInterfaceDefinitionDescriptor(String destinationDir, InterfaceDefinitionDescriptionType interfaceDefinitionDescriptor, JarFile archiveFile) throws IOException {
    String srPublicationFileRelPath = interfaceDefinitionDescriptor.getSrPublicationFile();
    
    if(srPublicationFileRelPath != null) {
      if(srPublicationFileRelPath.startsWith("/")) {
        srPublicationFileRelPath = srPublicationFileRelPath.substring(1);
      }
      jarUtil.extractFile(archiveFile, srPublicationFileRelPath, destinationDir);  	
    }   	 
  }
  
  protected void loadWebServicesJ2EEEngineDescriptors(String applicationName, WebservicesType webServicesJ2EEEngineDescriptor, WebservicesExtType webServicesJ2EEEngineExtDescriptor, ModuleRuntimeData moduleRuntimeData) throws WSDeploymentException {                   
    super.loadWebServicesJ2EEEngineDescriptors(applicationName, webServicesJ2EEEngineDescriptor, webServicesJ2EEEngineExtDescriptor, moduleRuntimeData);       
    //TODO - check granularity             
  }
  
  private void generateSerializationFramework() throws WSDeploymentException {    
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return;
    }
    
    ApplicationConfigurationContext wsApplicationConfigurationContext = (ApplicationConfigurationContext)getServiceContext().getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);                 
    WSApplicationMetaDataContext applicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);       
    ServiceRegistry serviceRegistry = wsApplicationConfigurationContext.getServiceRegistry();       
    ServiceMetaDataRegistry serviceMetaDataRegistry = applicationMetaDataContext.getServiceMetaDataRegistry();                      
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = applicationMetaDataContext.getModuleRuntimeDataRegistry();     
    ServiceDescriptorRegistry serviceDescriptorRegistry = wsApplicationDescriptorContext.getServiceDescriptorRegistry();
    ServiceExtDescriptorRegistry serviceExtDescriptorRegistry = wsApplicationDescriptorContext.getServiceExtDescriptorRegistry();
                
    WSDLLoader wsdlLoader = new WSDLLoader(); 
    Enumeration enumer = serviceDescriptorRegistry.getServiceDescriptors().elements();
    WebserviceDescriptionType serviceDescriptor;
    String serviceName; 
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor;     
    Service service; 
    ServiceMetaData serviceMetaData;
    ModuleRuntimeData moduleRuntimeData;                
    try {          
      while(enumer.hasMoreElements()) {
    	serviceDescriptor = (WebserviceDescriptionType)enumer.nextElement();     
        serviceName = serviceDescriptor.getWebserviceName().trim();  
        serviceExtDescriptor = serviceExtDescriptorRegistry.getServiceExtDescriptor(serviceName);
        service = serviceRegistry.getService(serviceName);
        if((service.getType() == null || service.getType() != 3) && (serviceExtDescriptor == null || serviceExtDescriptor.getType() == null || serviceExtDescriptor.getType() != 3)) {
          serviceMetaData = serviceMetaDataRegistry.getServiceMetaData(serviceDescriptor.getWebserviceName().trim());         
          moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(serviceMetaData.getModuleName());  
          generateSerializationFramework(applicationName, serviceDescriptor, serviceMetaData, moduleRuntimeData, wsdlLoader, service);
          generateWSDLs(moduleRuntimeData, serviceDescriptor, service);          
        }
      }      
      
      saveWebServicesJ2EEEngineDescriptors(applicationName);
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_GEN_FRM, new Object[]{applicationName}, e);
    }           
  }     
     
  private void generateSerializationFramework(String applicationName, WebserviceDescriptionType serviceDescriptor, ServiceMetaData serviceMetaData, ModuleRuntimeData moduleRuntimeData, WSDLLoader wsdlLoader, Service service) throws Exception {                       
    String serviceName = serviceDescriptor.getWebserviceName().trim(); 
   
    if(isGalaxyOrSCA(moduleRuntimeData, serviceDescriptor, service)){
    	return;
    }
    
    try {      
      Hashtable wsdlByXSDUse = loadWSDLByXSDUse(applicationName, serviceName, moduleRuntimeData, wsdlLoader);
      Hashtable typeMappingConfigFileByUse = loadTypeMappingConfigFileByUse(serviceDescriptor.getTypeMappingFile());        
      generateSerializationFramework(serviceDescriptor, serviceMetaData, moduleRuntimeData, wsdlByXSDUse, typeMappingConfigFileByUse, new SchemaToJavaGeneratorNew());
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_D_EXCEPTION_GEN_FRM_2 , new Object[]{applicationName, serviceName}, e); 
    }        
  }
  
  private void generateSerializationFramework(WebserviceDescriptionType serviceDescriptor, ServiceMetaData serviceMetaData, ModuleRuntimeData moduleRuntimeData, Hashtable wsdlByXSDUse, Hashtable typeMappingConfigFileByUse, SchemaToJavaGeneratorNew schemaToJavaGenerator) throws FileNotFoundException, SchemaToJavaGeneratorException, ParserException, IOException {                
    SchemaToJavaConfig schemaToJavaConfig = new SchemaToJavaConfig();    
    
    File outputDir = new File(webServicesContainerTempDir + "/ws" + System.currentTimeMillis());
    outputDir.mkdirs(); 
    String classPath = this.classPath + File.pathSeparator + outputDir;
                                  
    Enumeration enum1 = wsdlByXSDUse.keys(); 
    String use; 
    Definitions wsdlDefinitions; 
    TypeMappingFileType typeMappingConfigFileDescriptor;
    FileInputStream typeMappingConfigIn = null;
    String frmTypesPackage;    
    while(enum1.hasMoreElements()) {      
      try {
        use = (String)enum1.nextElement();         
        wsdlDefinitions = (Definitions)wsdlByXSDUse.get(use);        
        typeMappingConfigFileDescriptor = ((TypeMappingFileType)typeMappingConfigFileByUse.get(use));
        schemaToJavaConfig.setSchemaSources((ArrayList)wsdlDefinitions.getXSDTypeContainer().getSchemas());
        schemaToJavaConfig.setSchemaResolver(wsdlDefinitions.getXSDTypeContainer().getURIResolver());          
        schemaToJavaConfig.setOutputDir(outputDir);                     
        if(typeMappingConfigFileDescriptor != null) {
          typeMappingConfigIn = new FileInputStream(moduleRuntimeData.getFilePathName(typeMappingConfigFileDescriptor.get_value().trim())); 
          schemaToJavaConfig.getTypeSet().loadSettings(typeMappingConfigIn);
        }    
        frmTypesPackage = serviceMetaData.getFrmTypesPackage(use); 
        schemaToJavaConfig.setGenerationMode(SchemaToJavaConfig.FRAMEWORK_MODE);      
        schemaToJavaConfig.setBasePackageName(frmTypesPackage);                 
        schemaToJavaGenerator.generateAll(schemaToJavaConfig);               
      } finally {
        try {
          if(typeMappingConfigIn != null) {                          
            typeMappingConfigIn.close();                      
          }
        } catch(Exception iExc) {
          // $JL-EXC$
        }
      } 
    }
    
    FileOutputStream frmTypeMappingOut = null;        
    try {                               
      String frmTypeMappingFileRelPath = serviceMetaData.getFrmTypeMappingRelPath(SchemaStyleType._defaultTemp);
      String frmTypeMappingFilePath = outputDir + "/" + frmTypeMappingFileRelPath;      
      new File(frmTypeMappingFilePath).getParentFile().mkdirs();      
      frmTypeMappingOut = new FileOutputStream(frmTypeMappingFilePath);            
      schemaToJavaConfig.getTypeSet().saveSettings(frmTypeMappingOut);                  
                      
      packageBuilder.compileExternal(classPath, outputDir);
    
      String typesArchiveFileRelPath = null;            
      if(outputDir.exists() && outputDir.listFiles() != null) {
        typesArchiveFileRelPath = serviceMetaData.getTypesArchiveFileRelPath(SchemaStyleType._defaultTemp);
        String typesArchiveFilePath = moduleRuntimeData.getFilePathName(typesArchiveFileRelPath);
        new File(typesArchiveFilePath).getParentFile().mkdirs();        
        Vector filters = new Vector();        
        filters.add("class");
        filters.add("xml");        
        jarUtils.makeJarFromDir(typesArchiveFilePath, new String[]{outputDir.getAbsolutePath()}, filters);
    
        TypeMappingFileType typesArchiveFileDescriptor = new TypeMappingFileType();
        typesArchiveFileDescriptor.setType(new SchemaTypeType(SchemaTypeType._framework));
        typesArchiveFileDescriptor.setStyle(new SchemaStyleType(SchemaStyleType._defaultTemp));
        typesArchiveFileDescriptor.set_value(typesArchiveFileRelPath); 
        if(serviceDescriptor.getTypesArchiveFile() != null) {       
          TypeMappingFileType[] typesArchiveFileDescriptors = serviceDescriptor.getTypesArchiveFile(); 
          TypeMappingFileType[] typesArchiveFileDescriptorsAll = new TypeMappingFileType[typesArchiveFileDescriptors.length  + 1];
          System.arraycopy(typesArchiveFileDescriptors, 0, typesArchiveFileDescriptorsAll, 0, typesArchiveFileDescriptors.length);
          typesArchiveFileDescriptorsAll[typesArchiveFileDescriptorsAll.length - 1] = typesArchiveFileDescriptor; 
          serviceDescriptor.setTypesArchiveFile(typesArchiveFileDescriptorsAll);
        } else {
          serviceDescriptor.setTypesArchiveFile(new TypeMappingFileType[]{typesArchiveFileDescriptor});
        }             
      }
                         
      TypeMappingFileType typeMappingFileDescriptor = new TypeMappingFileType();            
      typeMappingFileDescriptor.setType(new SchemaTypeType(SchemaTypeType._framework));
      typeMappingFileDescriptor.setStyle(new SchemaStyleType(SchemaStyleType._defaultTemp));
      typeMappingFileDescriptor.set_value(typesArchiveFileRelPath + "#" + frmTypeMappingFileRelPath);
      if(serviceDescriptor.getTypeMappingFile() != null) {       
        TypeMappingFileType[] typeMappingFileDescriptors = serviceDescriptor.getTypeMappingFile(); 
        TypeMappingFileType[] typeMappingFileDescriptorsAll = new TypeMappingFileType[typeMappingFileDescriptors.length + 1];
        System.arraycopy(typeMappingFileDescriptors, 0, typeMappingFileDescriptorsAll, 0, typeMappingFileDescriptors.length);
        typeMappingFileDescriptorsAll[typeMappingFileDescriptorsAll.length - 1] = typeMappingFileDescriptor;               
        serviceDescriptor.setTypeMappingFile(typeMappingFileDescriptorsAll);
      } else {
        serviceDescriptor.setTypeMappingFile(new TypeMappingFileType[]{typeMappingFileDescriptor});
      }    
    } finally {
      try {
        if(frmTypeMappingOut != null) {         
          frmTypeMappingOut.close();
        }
      } catch(Exception iExc) {
        // $JL-EXC$
      }      
    }        
  }
  
  private Hashtable loadTypeMappingConfigFileByUse(TypeMappingFileType[] typeMappingFileTypes) {
    if(typeMappingFileTypes == null || typeMappingFileTypes.length == 0) {
      return new Hashtable(); 
    }
    
    Hashtable typeMappingConfigFileByUse = new Hashtable(); 
    
    TypeMappingFileType typeMappingFileType; 
    for(int i = 0; i < typeMappingFileTypes.length; i++) {
      typeMappingFileType = typeMappingFileTypes[i];
      if(typeMappingFileType.getType().getValue().equals(SchemaTypeType._config)) {
        typeMappingConfigFileByUse.put(typeMappingFileType.getStyle().getValue(), typeMappingFileType);
      }
    }    
    
    return typeMappingConfigFileByUse;
  }    
      
  private Hashtable loadWSDLByXSDUse(String applicationName, String serviceName, ModuleRuntimeData moduleRuntimeData, WSDLLoader wsdlLoader) throws WSDLException {
    if(!getServiceContext().getGlobalWSDLContext().getApplicationWSDLContexts().containsKey(applicationName)) {
      return new Hashtable();  
    }

    ApplicationWSDLContext applicationWSDLContext = (ApplicationWSDLContext)getServiceContext().getGlobalWSDLContext().getApplicationWSDLContexts().get(applicationName);    
    Hashtable wsdlByXSDUse = loadWSDLByXSDUse(serviceName, moduleRuntimeData, applicationWSDLContext, wsdlLoader);
       
    return wsdlByXSDUse; 
  }
    
  private Hashtable loadWSDLByXSDUse(String serviceName, ModuleRuntimeData moduleRuntimeData, ApplicationWSDLContext applicationWSDLContext, WSDLLoader wsdlLoader) throws WSDLException {        
    Hashtable wsdlByXSDUse = new Hashtable(); 
        
    Definitions wsdlDefaultUse = loadWSDLDefinitionsWithXSD(serviceName, moduleRuntimeData, applicationWSDLContext, wsdlLoader, WSDLContext.DEFAULT);
    if(wsdlDefaultUse != null) {               
      wsdlByXSDUse.put(WSDLContext.DEFAULT, wsdlDefaultUse);
      return wsdlByXSDUse;        
    } 
         
    Definitions wsdlLiteralUse = loadLiteralWSDL(serviceName, moduleRuntimeData, applicationWSDLContext, wsdlLoader);
    if(wsdlLiteralUse != null) {        
      wsdlByXSDUse.put(ApplicationServiceTypeMappingContext.LITERAL_USE, wsdlLiteralUse);
    }   
    
//    try {    
//      Definitions wsdlEncodedUse = loadWSDLDefinitionsWithXSD(serviceName, moduleRuntimeData, applicationWSDLContext, wsdlLoader, WSDLContext.RPC_ENC);
//      if(wsdlEncodedUse != null) {
//        wsdlByXSDUse.put(ApplicationServiceTypeMappingContext.ENCODED_USE, wsdlEncodedUse);
//      }
//    } catch(Exception e) {
//      //TODO
//      e.printStackTrace(); 
//      throw e; 
//    }     
                          
    return wsdlByXSDUse;                          
  }  
  
  private Definitions loadLiteralWSDL(String serviceName, ModuleRuntimeData moduleRuntimeData, ApplicationWSDLContext applicationWSDLContext, WSDLLoader wsdlLoader) throws WSDLException {
    Definitions wsdlDefinitions = loadWSDLDefinitionsWithXSD(serviceName, moduleRuntimeData, applicationWSDLContext, wsdlLoader, WSDLContext.DOCUMENT);
    if(wsdlDefinitions == null) {       
      wsdlDefinitions = loadWSDLDefinitionsWithXSD(serviceName, moduleRuntimeData, applicationWSDLContext, wsdlLoader, WSDLContext.RPC);        
    } 
     
    return wsdlDefinitions; 
  }
  
  private Definitions loadWSDLDefinitionsWithXSD(String serviceName, ModuleRuntimeData moduleRuntimeData, ApplicationWSDLContext applicationWSDLContext, WSDLLoader wsdlLoader, String style) throws WSDLException {
    Definitions wsdlDefinitions = loadWSDLDefinitions(serviceName, moduleRuntimeData, applicationWSDLContext, wsdlLoader, ApplicationWSDLContext.ROOT, style); 
    if(wsdlDefinitions == null) {
      wsdlDefinitions = loadWSDLDefinitions(serviceName, moduleRuntimeData, applicationWSDLContext, wsdlLoader, ApplicationWSDLContext.PORTTYPE, style);
    }    
    
    return wsdlDefinitions;   
  }  
   
  private Definitions loadWSDLDefinitions(String serviceName, ModuleRuntimeData moduleRuntimeData, ApplicationWSDLContext appilcationWSDLContext, WSDLLoader wsdlLoader, String type, String style) throws WSDLException {                                                                                                              
    WSDLContext wsdlContext_Type;
    wsdlContext_Type = appilcationWSDLContext.getWSDLContext(type);
    if(wsdlContext_Type == null) {
      return null; 
    } 
    
    WSDLRegistry wsdlRegistry_Style;
    wsdlRegistry_Style = wsdlContext_Type.getWSDLRegistry(style);  
    if(wsdlRegistry_Style == null) {
      return null;  
    }               
     
    Definitions wsdlDefinitions; 
    WsdlType wsdlDescriptor = (WsdlType)wsdlRegistry_Style.getWsdlDescriptor(serviceName);
    String wsdlFilePath = moduleRuntimeData.getFilePathName(wsdlDescriptor.get_value().trim());
    wsdlDefinitions = wsdlLoader.load(wsdlFilePath);            
                
    return wsdlDefinitions;           
  }
  
  private void generateWSDLs(ModuleRuntimeData moduleRuntimeData, WebserviceDescriptionType serviceDescriptor, Service service) throws Exception {
	if(isGalaxyOrSCA(moduleRuntimeData, serviceDescriptor, service)){
      WSInitialStartProcess.saveJEEBindingAndPortTypeTemplates(moduleRuntimeData.getModuleDir() + "/" + META_INF, moduleRuntimeData.getModuleDir(), serviceDescriptor);          	
    }
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
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = applicationMetaDataContext.getModuleRuntimeDataRegistry();        
    ServiceMetaDataRegistry serviceMetaDataRegistry = applicationMetaDataContext.getServiceMetaDataRegistry();                      
    ServiceDescriptorRegistry serviceDescriptorRegistry = (ServiceDescriptorRegistry)wsApplicationDescriptorContext.getServiceDescriptorRegistry();
        
    Enumeration enum1 = serviceDescriptorRegistry.getServiceDescriptors().keys();
    String[] serviceNames = new String[serviceDescriptorRegistry.getServiceDescriptors().size()];
    String[] filesForClassLoader = new String[0];
    WebserviceDescriptionType serviceDescriptor; 
    ModuleRuntimeData moduleRuntimeData;      
    ServiceMetaData serviceMetaData;   
    int i = 0;    
    while(enum1.hasMoreElements()) {
      String serviceName = (String)enum1.nextElement();
      serviceDescriptor = (WebserviceDescriptionType)serviceDescriptorRegistry.getServiceDescriptor(serviceName);       
      serviceMetaData = serviceMetaDataRegistry.getServiceMetaData(serviceName);   
      if(serviceMetaData.getApplicationName().equals(applicationName)) {        
        moduleRuntimeData = (ModuleRuntimeData)moduleRuntimeDataRegistry.getModuleRuntimeData(serviceMetaData.getModuleName());
        serviceNames[i++] = serviceName;  
        String[] serviceFilesForClassLoader = getServiceFilesForClassLoader(serviceDescriptor, moduleRuntimeData);
        String[] filesForClassLoaderAll = new String[filesForClassLoader.length + serviceFilesForClassLoader.length];
        System.arraycopy(filesForClassLoader, 0, filesForClassLoaderAll, 0, filesForClassLoader.length);
        System.arraycopy(serviceFilesForClassLoader, 0, filesForClassLoaderAll, filesForClassLoader.length, serviceFilesForClassLoader.length);
        filesForClassLoader = filesForClassLoaderAll;    
      }                                                                             
    } 
    
    String[] moduleFilesForClassLoader = getModuleFilesForClassLoader(moduleRuntimeDataRegistry); 
    
    ApplicationDeployInfo applicationDeployInfo = new ApplicationDeployInfo();    
    applicationDeployInfo.setDeployedComponentNames(serviceNames);
    applicationDeployInfo.setFilesForClassloader(WSUtil.unifyStrings(new String[][]{filesForClassLoader, moduleFilesForClassLoader}));
    
    return applicationDeployInfo; 
  }  
  
  private void publishWSDL() throws Exception {
    WSApplicationDescriptorContext wsApplicationDescriptorContext = (WSApplicationDescriptorContext)getServiceContext().getWebServicesDescriptorContext().getWSApplicationDescriptorContexts().get(applicationName);
    if(wsApplicationDescriptorContext == null) {
      return; 
    }
    
    ShmAccessPoint[] shmAccessPoints = ShmAccessPoint.getAllAccessPoints(ShmAccessPoint.PID_HTTP); 
    String host = ""; 
    if(shmAccessPoints != null && shmAccessPoints.length > 0) {      
      host = shmAccessPoints[0].getAddress().getHostName();       
      shmAccessPoints[0].getPort(); 
    }
    
    TransformerFactory tf = TransformerFactory.newInstance();
    
    Transformer transformer = tf.newTransformer();     
    
    WSApplicationMetaDataContext wsApplicationMetaDataContext = (WSApplicationMetaDataContext)getServiceContext().getMetaDataContext().getWSApplicationMetaDataContexts().get(applicationName);
    ServiceMetaDataRegistry serviceMetaDataRegistry = wsApplicationMetaDataContext.getServiceMetaDataRegistry(); 
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsApplicationMetaDataContext.getModuleRuntimeDataRegistry(); 
    
    ServiceDescriptorRegistry serviceDescriptorRegistry = wsApplicationDescriptorContext.getServiceDescriptorRegistry(); 
    ServiceExtDescriptorRegistry serviceExtDescriptorRegistry = wsApplicationDescriptorContext.getServiceExtDescriptorRegistry(); 
    
    ApplicationConfigurationContext applicationConfigurationContext = (ApplicationConfigurationContext)getServiceContext().getConfigurationContext().getApplicationConfigurationContexts().get(applicationName);
    ServiceRegistry serviceRegistry = applicationConfigurationContext.getServiceRegistry();       
    
    Enumeration enum1 = serviceDescriptorRegistry.getServiceDescriptors().keys();
    String serviceName; 
    WebserviceDescriptionType serviceDescriptor; 
    com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor;
    ServiceMetaData serviceMetaData;  
    ModuleRuntimeData moduleRuntimeData;
    Service service;
    Hashtable publishedModules = new Hashtable();              
    while(enum1.hasMoreElements()) {
      serviceName = (String)enum1.nextElement(); 
      service = serviceRegistry.getService(serviceName);
      serviceDescriptor = serviceDescriptorRegistry.getServiceDescriptor(serviceName); 
      serviceExtDescriptor = serviceExtDescriptorRegistry.getServiceExtDescriptor(serviceName);
      serviceMetaData = serviceMetaDataRegistry.getServiceMetaData(serviceName);
      moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(serviceMetaData.getModuleName());            
      publishWSDL(service, moduleRuntimeData, serviceDescriptor, serviceExtDescriptor, host, transformer, publishedModules);                  
    }          
  }
  
  private void publishWSDL(Service service, ModuleRuntimeData moduleRuntimeData, WebserviceDescriptionType serviceDescriptor, com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.WebserviceDescriptionType serviceExtDescriptor, String host, Transformer transformer, Hashtable publishedModules) throws Exception {
    String serviceName = serviceDescriptor.getWebserviceName().trim();        
    ApplicationWSDLContext wsApplicationWsdlContext = (ApplicationWSDLContext)getServiceContext().getGlobalWSDLContext().getApplicationWSDLContexts().get(applicationName);
    if(wsApplicationWsdlContext == null) {
      return; 	
    }
    
    WSDLContext wsdlContext = wsApplicationWsdlContext.getWSDLContext(ApplicationWSDLContext.ROOT); 
    WsdlType wsdlRootDescriptor = null; 
    if(wsdlContext != null) {
      WSDLRegistry wsdlRegistry = wsdlContext.getWSDLRegistry(WSDLContext.DEFAULT);
      if(wsdlRegistry != null) {      
        wsdlRootDescriptor = wsdlRegistry.getWsdlDescriptor(serviceName);
      }
    }
    
    if(wsdlRootDescriptor == null) {
      return; 
    }
     
    WsdlFileType wsdlFileDescriptor = serviceDescriptor.getWsdlFile();
    publishWSDL(service, moduleRuntimeData, wsdlRootDescriptor, serviceDescriptor.getWsdlFile().getWsdlPublication(), host, transformer, publishedModules);
    if(serviceExtDescriptor != null) {
      publishWSDL(service, moduleRuntimeData, wsdlRootDescriptor, serviceExtDescriptor.getWsdlPublication(), host, transformer, publishedModules);
    }           
  }  
  
  private void publishWSDL(Service service, ModuleRuntimeData moduleRuntimeData, WsdlType wsdlDescriptor, String[] wsdlPublishLocations, String host, Transformer transformer, Hashtable publishedModules) throws Exception {
    if(wsdlPublishLocations == null || wsdlPublishLocations.length == 0) {    
      return; 
    }
           
    String moduleName = moduleRuntimeData.getModuleName();    
    String wsdlRootRelPath = wsdlDescriptor.get_value().trim(); 
    wsdlRootRelPath = wsdlRootRelPath.replace('\\', '/');
    String wsdlRootRelPath2 = wsdlRootRelPath;
    boolean isWSDLDirMode = false;  
    if(wsdlRootRelPath2.startsWith(META_INF + "/")) {
      wsdlRootRelPath2 = wsdlRootRelPath2.substring((META_INF + "/").length());
    }
    if(wsdlRootRelPath2.startsWith(WEB_INF + "/")) {
      wsdlRootRelPath2 = wsdlRootRelPath2.substring((WEB_INF + "/").length());
    }
    if(wsdlRootRelPath2.startsWith(WSDL_DIR + "/")) {
      isWSDLDirMode = true; 
      wsdlRootRelPath2 = wsdlRootRelPath2.substring((WSDL_DIR + "/").length());
    }
    
    String wsdlSourceDir = new File(moduleRuntimeData.getFilePathName(wsdlRootRelPath)).getAbsolutePath();
    wsdlSourceDir = wsdlSourceDir.replace('\\', '/');          
    wsdlSourceDir = wsdlSourceDir.substring(0, wsdlSourceDir.indexOf(wsdlRootRelPath2));                                    
   
    String wsdlPublishParentDir; 
    String wsdlPublishLocation;
    HashSet wsdlPublishLocationSet = new HashSet();  
    try {    
      for(int i = 0; i < wsdlPublishLocations.length; i++) {        
        wsdlPublishLocation = wsdlPublishLocations[i];        
        if(!wsdlPublishLocation.startsWith("file:") || wsdlPublishLocationSet.contains(wsdlPublishLocation)) {
          continue; 
        }
                
        wsdlPublishLocationSet.add(wsdlPublishLocation);
        wsdlPublishLocation = wsdlPublishLocation.substring("file:".length());
        wsdlPublishLocation = wsdlPublishLocation.replace('\\', '/');
        
        if(wsdlPublishLocation.endsWith(wsdlRootRelPath2)) {
          int j = wsdlPublishLocation.indexOf(wsdlRootRelPath2);          
          wsdlPublishParentDir = wsdlPublishLocation.substring(0, wsdlPublishLocation.indexOf(wsdlRootRelPath2));          
          IOUtil.copyDir(new File(moduleRuntimeData.getFilePathName(META_INF + "/" + WSDL_DIR)), new File(wsdlPublishParentDir));
        } else {                                      
          if(isWSDLDirMode) {          
            HashSet modulePublications = (HashSet)publishedModules.get(moduleRuntimeData.getModuleName());             
            if(modulePublications == null || !modulePublications.contains(wsdlPublishLocation)) {                                              
              IOUtil.copyDir(new File(wsdlSourceDir), new File(wsdlPublishLocation), false);              
              if(modulePublications == null) {
                modulePublications = new HashSet(); 
                modulePublications.add(wsdlPublishLocation);
                publishedModules.put(moduleName, modulePublications);
              } else {
                modulePublications.add(wsdlPublishLocation);
              }              
            }            
          } else {
            IOUtil.copyDir(new File(wsdlSourceDir), new File(wsdlPublishLocation), false);
          }          
                    
          String wsdlDestFilePath = wsdlPublishLocation + "/" + wsdlRootRelPath2;
          File wsdlDestFile = new File(wsdlDestFilePath);                   
          Document wsdlDocument = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, wsdlDestFile);
          WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().updateWSDLPorts(wsdlDocument.getDocumentElement(), host, service.getServiceData());          
          transformer.transform(new DOMSource(wsdlDocument), new StreamResult(wsdlDestFile));                               
        }
      }
    } catch(Exception e) {
      //TODO 
      e.printStackTrace(); 
      throw e; 
    }
  }
    
  private Hashtable loadXSD(String applicationName, String serviceName, ModuleRuntimeData moduleRuntimeData) throws Exception {
    String[] types = new String[]{ApplicationWSDLContext.ROOT, ApplicationWSDLContext.PORTTYPE};
            
    Hashtable xsdByUse = new Hashtable();    
  
    List defaultXSD = loadXSD(applicationName, serviceName, moduleRuntimeData, types, WSDLContext.DEFAULT);              
    if(defaultXSD != null && defaultXSD.size() != 0) {     
      xsdByUse.put(WSDLContext.DEFAULT, defaultXSD);
      return xsdByUse; 
    }
       
    List literalXSD = loadLiteralXSD(applicationName, serviceName, moduleRuntimeData, types);       
    if(literalXSD != null && literalXSD.size() != 0) {      
      xsdByUse.put(ApplicationServiceTypeMappingContext.LITERAL_USE, literalXSD);            
    }
      
    List encodedXSD = loadXSD(applicationName, serviceName, moduleRuntimeData, types, WSDLContext.RPC_ENC);
    if(encodedXSD != null && encodedXSD.size() != 0) {
      if(!(literalXSD != null && literalXSD.size() != 0)) {               
        xsdByUse.put(ApplicationServiceTypeMappingContext.ENCODED_USE, encodedXSD);
      } 
    }    
    
    if(xsdByUse.isEmpty()) {      
      xsdByUse.put(ApplicationServiceTypeMappingContext.DEFAULT_USE, new ArrayList());
    }
    
    return xsdByUse; 
  }
  
  private List loadLiteralXSD(String applicationName, String serviceName, ModuleRuntimeData moduleRuntimeData, String[] types) throws Exception {
    List xsd = null; 
    
    xsd = loadXSD(applicationName, serviceName, moduleRuntimeData, types, WSDLContext.DOCUMENT);                        
    if(xsd == null || xsd.size() == 0) {
     xsd = loadXSD(applicationName, serviceName, moduleRuntimeData, types, WSDLContext.RPC);
    }             
              
    return xsd;      
  }
  
  private List loadXSD(String applicationName, String serviceName, ModuleRuntimeData moduleRuntimeData, String[] types, String style) throws Exception {                           
    if(types == null) {
      return new ArrayList(); 
    }
    
    if(!getServiceContext().getGlobalWSDLContext().getApplicationWSDLContexts().containsKey(applicationName)) {
     return new ArrayList(); 
    }
    
    ApplicationWSDLContext applicationWSDLContext = (ApplicationWSDLContext)getServiceContext().getGlobalWSDLContext().getApplicationWSDLContexts().get(applicationName);
    
    WSDLLoader wsdlLoader = new WSDLLoader();                           
        
    List xsd = new ArrayList();          
    WSDLContext wsdlContext = null; 
    for(int i = 0; i < types.length; i++) {
      String type = types[i];              
      if(!applicationWSDLContext.containsWSDLContextKey(type)) {
        continue;
      }        
      wsdlContext = (WSDLContext)applicationWSDLContext.getWSDLContext(types[i]);
      xsd.addAll(loadXSD(serviceName, moduleRuntimeData, wsdlContext, wsdlLoader, style));                       
    }
    
    return xsd;           
  }
  
  private List loadXSD(String serviceName, ModuleRuntimeData moduleRuntimeData, WSDLContext wsdlContext, WSDLLoader wsdlLoader, String style) throws WSDLException {                                                                                                              
    if(!wsdlContext.containsWSDLRegistryKey(style)) {
      return new ArrayList();  
    }               
    
    WSDLRegistry wsdlRegistry = wsdlContext.getWSDLRegistry(style);
    if(!wsdlRegistry.containsWsdlDescriptorID(serviceName)) {
      new ArrayList();
    }
    
    List xsd;           
    WsdlType wsdlDescriptor = (WsdlType)wsdlRegistry.getWsdlDescriptor(serviceName);
    String wsdlFilePath = moduleRuntimeData.getFilePathName(wsdlDescriptor.get_value().trim());
    Definitions wsdlDefinitions = wsdlLoader.load(wsdlFilePath);
    xsd = wsdlDefinitions.getXSDTypeContainer().getSchemas();                
              
    return xsd;           
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
  
  private boolean isGalaxyOrSCA(ModuleRuntimeData moduleRuntimeData, WebserviceDescriptionType serviceDescriptor, Service service) throws Exception {
    InterfaceDefinitionRegistry interfaceDefinitionRegistry = getServiceContext().getConfigurationContext().getInterfaceDefinitionRegistry();
    InterfaceMappingRegistry interfaceMappingRegistry = getServiceContext().getMappingContext().getInterfaceMappingRegistry(); 
    
    BindingData[] bindingDatas = service.getServiceData().getBindingData();
    if(bindingDatas == null || bindingDatas.length == 0) {
      return false; 	
    }
    
    InterfaceDefinition interfaceDefinition = interfaceDefinitionRegistry.getInterfaceDefinition(bindingDatas[0].getInterfaceId());  
    InterfaceMapping interfaceMapping = interfaceMappingRegistry.getInterfaceMapping(interfaceDefinition.getInterfaceMappingId()); 
    ImplementationLink implLink = interfaceMapping.getImplementationLink();
    if(implLink.getImplementationContainerID().equals(WSAltConvertor.GALAXY_IMPL_LINK)
    	|| implLink.getImplementationContainerID().equals(WSAltConvertor.SCA_IMPL_LINK)){
    	return true;
    } else {
    	return false;
    }
  }
  
}
