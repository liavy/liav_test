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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarFile;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.services.deploy.container.op.start.ApplicationStartInfo;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorConfigNew;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorNew;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ImplArchiveFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ImplTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.SchemaTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ServiceRefGroupDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WSClientsJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.WsClientsType;
import com.sap.engine.services.webservices.server.container.descriptors.ConfigurationDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeDataRegistry;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.container.wsclients.ServiceRefContext;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.ServiceRefGroupDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.ServiceRefGroupExtDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsApplicationDescriptorContext;
import com.sap.engine.services.webservices.server.container.wsclients.descriptors.WSClientsJ2EEEngineDescriptorRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefGroupMetaData;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.ServiceRefGroupMetaDataRegistry;
import com.sap.engine.services.webservices.server.container.wsclients.metadata.WSClientsApplicationMetaDataContext;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title: WSClientsInitialStartProcess
 * Description: WSClientsInitialProcess
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WSClientsInitialStartProcess extends WSClientsAbstractStartProcess {
	
  private String classPath;       
  protected JarUtil jarUtil; 
  protected JarUtils jarUtils; 
  protected PackageBuilder packageBuilder;
  protected ProxyGeneratorNew proxyGenerator;  
  private ApplicationStartInfo applicationStartInfo; 
  
  public WSClientsInitialStartProcess(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, Configuration appConfiguration, ClassLoader appLoader, String baseClassPath) {
    this.applicationName = applicationName; 
    this.webServicesContainerDir = webServicesContainerDir; 
    this.webServicesContainerTempDir = webServicesContainerTempDir;
    this.serviceContext = new ServiceRefContext(); 
    this.appConfiguration = appConfiguration;  
    this.appLoader = appLoader;
    this.classPath = baseClassPath + File.pathSeparator + getApplicationClassPath();       
    this.jarUtil = new JarUtil();
    this.jarUtils = new JarUtils();  
    this.packageBuilder = new PackageBuilder();
    this.proxyGenerator = new ProxyGeneratorNew();       
  } 
  
  /**
   * @return ApplicationStartInfo 
   */
  public ApplicationStartInfo getApplicationStartInfo() {    
    return applicationStartInfo;        
  }
 
  public void preProcess() throws WSDeploymentException, WSWarningException {
	  
  }
    
  public void init() throws WSDeploymentException {    
    loadWSClientsJ2EEEngineDescriptorsInitially();           
  }
  
  public void execute() throws WSDeploymentException {    
    loadWSClientsJ2EEEngineDescriptors(); 
    generateImplementationFramework();    
  }
  
  public void finish() throws WSDeploymentException {    
    upload();      
    
    setApplicationStartInfo();    
  }
      
  public void postProcess() throws WSDeploymentException, WSWarningException {
	  
  }
  
  public void notifyProcess() throws WSWarningException {   

  }
 
  public void commitProcess() throws WSWarningException {    
    try {      
      IOUtil.deleteDir(webServicesContainerTempDir);
    } catch(Exception e) {
      // $JL-EXC$ 
      // TODO - add trace       
    }
  }
   
  public void rollbackProcess() throws WSWarningException {    
    try {
      IOUtil.deleteDir(webServicesContainerTempDir);
    } catch(Exception e) {
      // TODO - add trace       
      // $JL-EXC$     	
    }
  }
  
  private void generateImplementationFramework() throws WSDeploymentException {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName); 
    if(wsClientsApplicationDescriptorContext == null) {
      return; 
    } 
    
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName); 
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry(); 
    
    WSClientsJ2EEEngineDescriptorRegistry wsClientsJ2EEEngineDescriptorRegistry = wsClientsApplicationDescriptorContext.getWsClientsJ2EEEngineDescriptorRegistry();     
    Enumeration enum1 = wsClientsJ2EEEngineDescriptorRegistry.getWsClientsJ2EEEngineDescriptors().keys(); 
    String moduleName; 
    WsClientsType wsClientsJ2EEEngineDescriptor; 
    ModuleRuntimeData moduleRuntimeData;             
    try {    	
      while(enum1.hasMoreElements()) {
        moduleName = (String)enum1.nextElement(); 
        wsClientsJ2EEEngineDescriptor = wsClientsJ2EEEngineDescriptorRegistry.getWsClientsJ2EEEngineDescriptor(moduleName);         
        moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(moduleName);
        String archiveFileRelPath = moduleRuntimeData.getArchiveFileRelPath();
        if(archiveFileRelPath == null) {
          archiveFileRelPath = moduleRuntimeData.getModuleName();
        }
        generateImplementationFramework(moduleRuntimeData, wsClientsJ2EEEngineDescriptor, classPath + File.pathSeparator + getModuleClassPath(moduleRuntimeData, new File(webServicesContainerDir + "/" + WSApplicationMetaDataContext.BACKUP + "/" + archiveFileRelPath)));        
      }
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_GEN_IMPL_FRM, new Object[]{applicationName}, e);
    }
  }
  
  private void generateImplementationFramework(ModuleRuntimeData moduleRuntimeData, WsClientsType wsClientsJ2EEEngineDescriptor, String classPath) throws WSDeploymentException {       
    ServiceRefGroupDescriptionType[] serviceRefGroupDescriptors = wsClientsJ2EEEngineDescriptor.getServiceRefGroupDescription();     
    if(serviceRefGroupDescriptors == null || serviceRefGroupDescriptors.length == 0) {
      return; 
    }
   
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    ConfigurationDescriptorRegistry configurationDescriptorRegistry = wsClientsApplicationDescriptorContext.getConfigurationDescriptorRegistry(); 
    ServiceRefGroupExtDescriptorRegistry serviceRefGroupExtDescriptorRegistry = wsClientsApplicationDescriptorContext.getServiceRefGroupExtDescriptorRegistry();         
    try {                  
      ServiceRefGroupDescriptionType serviceRefGroupDescriptor;
      String serviceRefGroupName; 
      com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType serviceRefGroupExtDescriptor;  
      ConfigurationRoot configurationDescriptor;                    
      for (int i = 0; i < serviceRefGroupDescriptors.length; i++) {
        serviceRefGroupDescriptor = serviceRefGroupDescriptors[i];
        serviceRefGroupName = serviceRefGroupDescriptor.getServiceRefGroupName().trim(); 
        configurationDescriptor = configurationDescriptorRegistry.getConfigurationDescriptor(serviceRefGroupName); 
        if(!containsJAXWSService(configurationDescriptor.getRTConfig().getService(), appLoader)) {          
          serviceRefGroupExtDescriptor = serviceRefGroupExtDescriptorRegistry.getServiceRefGroupExtDescriptor(serviceRefGroupName);           
          generateImplementationFramework(moduleRuntimeData, serviceRefGroupDescriptor, serviceRefGroupExtDescriptor, configurationDescriptor, classPath);          
        }       
      }                    
      //TODO - optimize - skip if not necessary
      WSClientsJ2EEEngineFactory.save(wsClientsJ2EEEngineDescriptor, moduleRuntimeData.getFilePathName(moduleRuntimeData.getMetaInfRelDir() + "/" + WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR));      
    } catch(WSDeploymentException e) {
      throw e;  
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_GEN_IMPL_FRM_2, new Object[]{applicationName, moduleRuntimeData.getModuleName()}, e);
    }
  }
  
  private void generateImplementationFramework(ModuleRuntimeData moduleRuntimeData, ServiceRefGroupDescriptionType serviceRefGroupDescriptor, com.sap.engine.services.webservices.server.deploy.descriptors.wsclients.ext.ServiceRefGroupDescriptionType serviceRefGroupExtDescriptor, ConfigurationRoot configurationDescriptor, String classPath) throws WSDeploymentException {
    if(serviceRefGroupDescriptor.getWsdlFile() == null) {
      return; 
    }  
    
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);
    String serviceRefGroupName = serviceRefGroupDescriptor.getServiceRefGroupName().trim();    
    
    String srcArchiveFileRelPath = null;  
    ImplArchiveFileType[] implArchiveFileRelPaths = serviceRefGroupDescriptor.getImplArchiveFile();    
    if(implArchiveFileRelPaths != null && implArchiveFileRelPaths.length != 0) {
      for(ImplArchiveFileType implArchiveFileRelPath: implArchiveFileRelPaths) {    	
    	if(implArchiveFileRelPath.getType().getValue().equals(ImplTypeType._src)) {
          srcArchiveFileRelPath = implArchiveFileRelPath.get_value();
          break; 
        }	  
      } 	
    } 
    
    JarFile srcArchiveJarFile = null; 
    InputStream typeMappingConfigIn = null;  
    OutputStream typeMappingConfigOut = null; 
    try {    
      String outputDir = webServicesContainerTempDir + "/wscl" + System.currentTimeMillis();             
      String implArchiveFileRelPath = "jars/jar" + System.currentTimeMillis() + ".jar";
      
      if(srcArchiveFileRelPath != null) {
    	srcArchiveJarFile = new JarFile(moduleRuntimeData.getModuleDir() + "/" + srcArchiveFileRelPath); 
        new JarUtil().extractFiles(srcArchiveJarFile, new String[0], new String[0], new String[0], new String[0], outputDir, false);	  
      } else {
	    String typeMappingConfigFileRelPath = null; 
	    TypeMappingFileType[] typeMappingFileDescriptors = serviceRefGroupDescriptor.getTypeMappingFile();
	    TypeMappingFileType typeMappingFileDescriptor; 
	    if(typeMappingFileDescriptors != null) {
	      for(int i = 0; i < typeMappingFileDescriptors.length; i++) {
	        typeMappingFileDescriptor = typeMappingFileDescriptors[i];
	        if(typeMappingFileDescriptor.getType().getValue().trim().equals(SchemaTypeType._config)) {          
	          typeMappingConfigFileRelPath = typeMappingFileDescriptor.get_value().trim();
	          break;     
	        }
	      }
	    } 
    	  
    	String outputPackage = "p" + System.currentTimeMillis();        
    	
	    ProxyGeneratorConfigNew proxyGeneratorConfig = new ProxyGeneratorConfigNew();     
	    if(typeMappingConfigFileRelPath != null) {
	      typeMappingConfigIn = new FileInputStream(moduleRuntimeData.getFilePathName(typeMappingConfigFileRelPath));                
	      proxyGeneratorConfig.getSchemaConfig().getTypeSet().loadSettings(typeMappingConfigIn);  
	    }
	                      
	    proxyGeneratorConfig.setGenerationMode(ProxyGeneratorConfigNew.IMPLEMENTATION_MODE);
	    if(serviceRefGroupExtDescriptor != null && serviceRefGroupExtDescriptor.getWsdlFile() != null) {
	      proxyGeneratorConfig.setWsdlPath(serviceRefGroupExtDescriptor.getWsdlFile().trim());
	    } else {           
	      proxyGeneratorConfig.setWsdlPath(moduleRuntimeData.getFilePathName(serviceRefGroupDescriptor.getWsdlFile().trim()));
	    }
	            
	    proxyGeneratorConfig.setOutputPackage(outputPackage);               
	    if(containsService(Service.J2EE14_SERVICE_TYPE.intValue(), configurationDescriptor.getRTConfig().getService())) {
	      proxyGeneratorConfig.setGenerateSerializable(true); 
	    } else {
	      proxyGeneratorConfig.setGenerateSerializable(false);
	    }      
	    proxyGeneratorConfig.setProxyConfig(configurationDescriptor);
	    proxyGeneratorConfig.setMappingRules(wsClientsApplicationDescriptorContext.getMappingDescriptorRegistry().getMappingDescriptor(serviceRefGroupName));                        
	    proxyGeneratorConfig.setOutputPath(outputDir);
	    proxyGenerator.generateAll(proxyGeneratorConfig);
	         
	    ConfigurationFactory.save(proxyGeneratorConfig.getProxyConfig(), moduleRuntimeData.getFilePathName(serviceRefGroupDescriptor.getConfigurationFile().trim()));
	    MappingFactory.save(proxyGeneratorConfig.getMappingRules(), moduleRuntimeData.getFilePathName(serviceRefGroupDescriptor.getWsdlMappingFile().trim())); 
	    String typeMappingFrmFileRelPath = outputPackage.replace('.', '/') + "/frm/types.xml";        
	    String typeMappingFrmFilePath = outputDir + "/" + outputPackage.replace('.', '/') + "/frm/types.xml"; 
	    new File(typeMappingFrmFilePath).getParentFile().mkdirs();            
	    typeMappingConfigOut = new FileOutputStream(typeMappingFrmFilePath); 
	    proxyGeneratorConfig.getSchemaConfig().getTypeSet().saveSettings(typeMappingConfigOut);
	    TypeMappingFileType typeMappingFrmFileDescriptor = new TypeMappingFileType(); 
	    typeMappingFrmFileDescriptor.set_value(implArchiveFileRelPath + "#" + typeMappingFrmFileRelPath);
	    typeMappingFrmFileDescriptor.setType(SchemaTypeType.framework);
	    serviceRefGroupDescriptor.setTypeMappingFile(unifyTypeMappingFileDescriptors(new TypeMappingFileType[][]{typeMappingFileDescriptors, new TypeMappingFileType[]{typeMappingFrmFileDescriptor}}));  
      }
      
      packageBuilder.compileExternal(classPath + File.pathSeparator + outputDir, new File(outputDir));
      
      Vector<String> filters = new Vector<String>();        
      filters.add("class");
      filters.add("xml");       
      String implArchiveFilePath = moduleRuntimeData.getFilePathName(implArchiveFileRelPath); 
      new File(implArchiveFilePath).getParentFile().mkdirs();  
      jarUtils.makeJarFromDir(implArchiveFilePath, new String[]{outputDir}, filters);                            
          
      ImplArchiveFileType implArchiveFileRelPathType = new ImplArchiveFileType(); 
      implArchiveFileRelPathType.setType(ImplTypeType.bin);
      implArchiveFileRelPathType.set_value(implArchiveFileRelPath);
      
      ImplArchiveFileType[] implArchiveFileRelPathTypes = serviceRefGroupDescriptor.getImplArchiveFile();
      ImplArchiveFileType[] implArchiveFileRelPathsAll = new ImplArchiveFileType[0]; 
      if(implArchiveFileRelPathTypes == null || implArchiveFileRelPathTypes.length == 0) {
        implArchiveFileRelPathsAll = new ImplArchiveFileType[]{implArchiveFileRelPathType}; 	  
      } else {
        implArchiveFileRelPathsAll = new ImplArchiveFileType[implArchiveFileRelPathTypes.length + 1];
        System.arraycopy(implArchiveFileRelPathTypes, 0, implArchiveFileRelPathsAll, 0, implArchiveFileRelPathTypes.length);
        implArchiveFileRelPathsAll[implArchiveFileRelPathTypes.length] = implArchiveFileRelPathType;         
      }
      
      serviceRefGroupDescriptor.setImplArchiveFile(implArchiveFileRelPathsAll);      
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_GEN_IMPL_FRM_3, new Object[]{applicationName, serviceRefGroupName}, e);       
    } finally {
      try {
        if(typeMappingConfigIn != null) {
          typeMappingConfigIn.close(); 
        }
      } catch(Exception e) { 
        // $JL-EXC$      
      }
      try {
        if(typeMappingConfigOut != null) {
          typeMappingConfigOut.close(); 
        }
      } catch(Exception e) {  
        // $JL-EXC$    
      }
      try {
        if(srcArchiveJarFile != null) {
          srcArchiveJarFile.close(); 
        }
      } catch(Exception e) {  
        // $JL-EXC$    
      }      
    }                    
  }
  
  private void upload() throws WSDeploymentException {
    try {    
      if(!appConfiguration.existsSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME)) {    
        return;        
      }  
      uploadImplementationFramework(appConfiguration.getSubConfiguration(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME));
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_UPLOAD, new Object[]{applicationName}, e);        
    }    
  } 
  
  private void uploadImplementationFramework(Configuration webServicesConfiguration) throws WSDeploymentException {    
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName); 
    if(wsClientsApplicationDescriptorContext == null) {
      return; 
    } 
    
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName); 
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry(); 
    
    WSClientsJ2EEEngineDescriptorRegistry wsClientsJ2EEEngineRegistry = wsClientsApplicationDescriptorContext.getWsClientsJ2EEEngineDescriptorRegistry(); 
    Enumeration enum1 = wsClientsJ2EEEngineRegistry.getWsClientsJ2EEEngineDescriptors().keys(); 
    String moduleName; 
    File moduleDir; 
    WsClientsType wsClientsJ2EEEngineDescriptor; 
    ModuleRuntimeData moduleRuntimeData; 
    Configuration moduleConfiguration; 
    try {    
      while(enum1.hasMoreElements()) {
        moduleName = (String)enum1.nextElement();         
        wsClientsJ2EEEngineDescriptor = wsClientsJ2EEEngineRegistry.getWsClientsJ2EEEngineDescriptor(moduleName); 
        moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(moduleName);
        moduleDir = new File(moduleRuntimeData.getModuleDir());
        moduleConfiguration = webServicesConfiguration.getSubConfiguration(moduleDir.getName());
        uploadFile(moduleDir, moduleRuntimeData.getMetaInfRelDir() + "/"  + WS_CLIENTS_J2EE_ENGINE_DESCRIPTOR, moduleConfiguration, true);
        uploadImplArchiveFiles(moduleRuntimeData, wsClientsJ2EEEngineDescriptor.getServiceRefGroupDescription(), moduleConfiguration);
      }
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_UPLOAD, new Object[]{applicationName}, e);       
     }
  }
  
  private void uploadImplArchiveFiles(ModuleRuntimeData moduleRuntimeData, ServiceRefGroupDescriptionType[] serviceRefGroupDescriptors, Configuration moduleConfiguration) throws Exception {
    if(serviceRefGroupDescriptors == null || serviceRefGroupDescriptors.length == 0) {
      return; 
    } 
                
    for(int i = 0; i < serviceRefGroupDescriptors.length; i++) {                
      uploadImplArchiveFiles(moduleRuntimeData, serviceRefGroupDescriptors[i], moduleConfiguration);      
    }    
  }
  
  private void uploadImplArchiveFiles(ModuleRuntimeData moduleRuntimeData, ServiceRefGroupDescriptionType serviceRefGroupDescriptor, Configuration moduleConfiguration) throws Exception {
    if(serviceRefGroupDescriptor.getWsdlFile() == null) {
      return; 
    }
     
    File moduleDir = new File(moduleRuntimeData.getModuleDir());        
    try {    
      uploadFile(moduleDir, serviceRefGroupDescriptor.getConfigurationFile().trim(), moduleConfiguration, true);                     
      uploadFile(moduleDir, serviceRefGroupDescriptor.getWsdlMappingFile().trim(), moduleConfiguration, true);      
      ImplArchiveFileType[] implArchiveFileRelPaths = serviceRefGroupDescriptor.getImplArchiveFile(); 
      if(implArchiveFileRelPaths != null && implArchiveFileRelPaths.length != 0) {
        for(ImplArchiveFileType implArchiveFileRelPath: implArchiveFileRelPaths) {
          if(implArchiveFileRelPath.getType().getValue().equals(ImplTypeType._bin)) {
    	    uploadFile(moduleDir, implArchiveFileRelPath.get_value(), moduleConfiguration, true);
          }
        } 
      }                        
    } catch(Exception e) {
      Location.getLocation(WSLogging.DEPLOY_LOCATION).traceThrowableT(Severity.PATH, "", e);           
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_CL_D_EXCEPTION_UPLOAD_2, new Object[]{applicationName, serviceRefGroupDescriptor.getServiceRefGroupName().trim()}, e); 
    }          
  }
      
  private String getApplicationClassPath() {
    LoadContext loadContext = WSContainer.getServiceContext().getCoreContext().getLoadContext(); 
    String appLoaderName = loadContext.getName(appLoader); 

    if(appLoaderName == null) {
      return ""; 
    } 
        
    StringBuffer appClassPath = new StringBuffer();
    String[] appLoaderResources = loadContext.getResourceNames(appLoader);
    if(appLoaderResources != null && appLoaderResources.length != 0) {
      for (int i = 0; i < appLoaderResources.length; i++) {
        appClassPath.append(appLoaderResources[i]);
        appClassPath.append(File.pathSeparatorChar);
      }
    }

    String[] refLoaderNames = loadContext.getReferences(appLoader);
    if(refLoaderNames != null && refLoaderNames.length != 0) {
      ClassLoader refLoader;
      String[] refLoaderResources;  
      for(int i = 0; i < refLoaderNames.length; i++) {
        refLoader = loadContext.getClassLoader(refLoaderNames[i]);
        if(refLoader == null) {
          continue;
        }          
        
        refLoaderResources = loadContext.getResourceNames(refLoader);
        if(refLoaderResources != null && refLoaderResources.length != 0) {
          for(int j = 0; j < refLoaderResources.length; j++) {
            appClassPath.append(refLoaderResources[j]);
            appClassPath.append(File.pathSeparatorChar);
          }
        }        
      }
    }
    
    return appClassPath.toString();        
  }
   
  private String getModuleClassPath(ModuleRuntimeData moduleRuntimeData, File archiveFile) throws Exception {
    String moduleType = moduleRuntimeData.getType();               
    try {
      if(moduleType.equals(ModuleRuntimeData.EJB_SUFFIX)) {
        return archiveFile.getAbsolutePath() + File.pathSeparator; 
      } else if(moduleType.equals(ModuleRuntimeData.WS_SUFFIX) || moduleType.equals(ModuleRuntimeData.WEB_SUFFIX)) {            
        String archiveFileName = archiveFile.getName();
        String tmpArchiveFilePath = moduleRuntimeData.getModuleWorkingDir() + "/" + archiveFileName.substring(0, archiveFileName.lastIndexOf(".")) + "_tmp.jar";
        new File(tmpArchiveFilePath).getParentFile().mkdirs();               
        if(moduleType.equals(ModuleRuntimeData.WEB_SUFFIX)) {        
          jarUtil.makeJarFile(archiveFile, new String[]{"WEB-INF/classes"}, new String[0], new String[0], new String[0], tmpArchiveFilePath);        
        } else {
          jarUtil.makeJarFile(archiveFile, new String[0], new String[0], new String[0], new String[0], tmpArchiveFilePath);
        }                                                    
        return tmpArchiveFilePath + File.pathSeparator;  
      }                               
    } catch (Exception e) {
      //TODO
      e.printStackTrace(); 
      throw e; 
    } 
    
    return ""; 
  }
  
  private void setApplicationStartInfo() {
    this.applicationStartInfo = makeApplicationStartInfo();
  }
  
  private ApplicationStartInfo makeApplicationStartInfo() {
    WSClientsApplicationDescriptorContext wsClientsApplicationDescriptorContext = (WSClientsApplicationDescriptorContext)getServiceContext().getWsClientsApplicationDescriptorContexts().get(applicationName);       
    if(wsClientsApplicationDescriptorContext == null) {      
      return new ApplicationStartInfo(true);
    }
    
    WSClientsApplicationMetaDataContext wsClientsApplicationMetaDataContext = (WSClientsApplicationMetaDataContext)getServiceContext().getWsClientsMetaDataContext().getWsClientsApplicationMetaDataContexts().get(applicationName);
    ServiceRefGroupMetaDataRegistry serviceRefGroupMetaDataRegistry = wsClientsApplicationMetaDataContext.getServiceRefGroupMetaDataRegistry();
    ModuleRuntimeDataRegistry moduleRuntimeDataRegistry = wsClientsApplicationMetaDataContext.getModuleRuntimeDataRegistry(); 
    
    ServiceRefGroupDescriptorRegistry serviceRefGroupDescriptorRegistry = wsClientsApplicationDescriptorContext.getServiceRefGroupDescriptorRegistry();    
    Enumeration<ServiceRefGroupDescriptionType> enumer = serviceRefGroupDescriptorRegistry.getServiceRefGroupDescriptors().elements(); 
    String serviceRefGroupName; 
    ServiceRefGroupDescriptionType serviceRefGroupDescriptor; 
    ServiceRefGroupMetaData serviceRefGroupMetaData;
    ModuleRuntimeData moduleRuntimeData;
    ArrayList<String[]> serviceFilesForClassLoader = new ArrayList<String[]>(); 
    while(enumer.hasMoreElements()) {
      serviceRefGroupDescriptor = enumer.nextElement();  
      serviceRefGroupName = serviceRefGroupDescriptor.getServiceRefGroupName();  
      serviceRefGroupDescriptor = serviceRefGroupDescriptorRegistry.getServiceRefGroupDescriptor(serviceRefGroupName);
      serviceRefGroupMetaData = serviceRefGroupMetaDataRegistry.getServiceRefGroupMetaData(serviceRefGroupName);
      moduleRuntimeData = moduleRuntimeDataRegistry.getModuleRuntimeData(serviceRefGroupMetaData.getModuleName());
//      if(serviceRefGroupDescriptor.getImplArchiveFile() != null) {          
//        serviceRefGroupFilesForClassLoader.add(moduleRuntimeData.getFilePathName(serviceRefGroupDescriptor.getImplArchiveFile().trim()));
//      }      
//      addImplArchiveFileRelPaths(moduleRuntimeData.getModuleDir(), serviceRefGroupDescriptor.getImplArchiveFileExt(), serviceRefGroupFilesForClassLoader);
      serviceFilesForClassLoader.add(getServiceRefGroupFilesForClassLoader(moduleRuntimeData, serviceRefGroupDescriptor)); 
    }
    
//    String[] serviceRefGroupFilesForClassLoaderArr = new String[serviceRefGroupFilesForClassLoader.size()];
//    serviceRefGroupFilesForClassLoader.toArray(serviceRefGroupFilesForClassLoaderArr);
    
    ApplicationStartInfo applicationStartInfo = new ApplicationStartInfo(true); 
    //applicationStartInfo.addFilesForClassloader(serviceRefGroupFilesForClassLoader.toArray(new String[serviceRefGroupFilesForClassLoader.size()]));
    applicationStartInfo.addFilesForClassloader(WSUtil.unifyStrings(serviceFilesForClassLoader));
    
    return applicationStartInfo;
  }
    
  private String[] getServiceRefGroupFilesForClassLoader(ModuleRuntimeData moduleRuntimeData, ServiceRefGroupDescriptionType serviceRefGroupDescriptor) {
    ImplArchiveFileType[] implArchiveFileRelPaths = serviceRefGroupDescriptor.getImplArchiveFile(); 
    if(implArchiveFileRelPaths == null || implArchiveFileRelPaths.length == 0) {
      return new String[0]; 	
    }
    
    ArrayList<String> implArchiveFilePaths = new ArrayList<String>(); 
    for(ImplArchiveFileType implArchiveFileRelPath: implArchiveFileRelPaths) {
      if(implArchiveFileRelPath.getType().getValue().equals(ImplTypeType._bin)) {
        implArchiveFilePaths.add(moduleRuntimeData.getModuleDir() + "/" + implArchiveFileRelPath.get_value());   
      }   	
    }
    
    return implArchiveFilePaths.toArray(new String[implArchiveFilePaths.size()]);     
  }
  
  private TypeMappingFileType[] unifyTypeMappingFileDescriptors(TypeMappingFileType[][] typeMappingFileTypes) {
    if(typeMappingFileTypes == null || typeMappingFileTypes.length == 0) {
      return new TypeMappingFileType[0];
    } 
      
    if(typeMappingFileTypes.length == 1) {
      return typeMappingFileTypes[0]; 
    }
       
    ArrayList<TypeMappingFileType> typeMappingFileTypesList = new ArrayList<TypeMappingFileType>(); 
    TypeMappingFileType[] currentTypeMappingFileTypes; 
    for(int i = 0; i < typeMappingFileTypes.length; i++) {
      currentTypeMappingFileTypes = typeMappingFileTypes[i]; 
      if(currentTypeMappingFileTypes == null) {
        continue;
      }  
      addTypeMappingFileTypes(currentTypeMappingFileTypes, typeMappingFileTypesList);  
    } 
    
    return typeMappingFileTypesList.toArray(new TypeMappingFileType[typeMappingFileTypesList.size()]); 
  }
  
  private void addTypeMappingFileTypes(TypeMappingFileType[] typeMappingFileTypes, ArrayList<TypeMappingFileType> typeMappingFileTypesList) {
    if(typeMappingFileTypes == null || typeMappingFileTypes.length == 0) {
      return; 	
    }	  
    
    for(TypeMappingFileType typeMappingFileType: typeMappingFileTypes) {
      typeMappingFileTypesList.add(typeMappingFileType); 	
    }
  }  
  
}
