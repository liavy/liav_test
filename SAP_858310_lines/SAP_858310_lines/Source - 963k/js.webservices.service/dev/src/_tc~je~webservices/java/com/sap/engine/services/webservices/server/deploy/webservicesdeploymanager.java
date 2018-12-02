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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.container.monitor.SystemMonitor;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.ConfigurationLockedException;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.ws.WebServicesDeploymentInterface;
import com.sap.engine.services.deploy.container.AdditionalAppInfo;
import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.deploy.container.ContainerDeploymentInfo;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.ContainerInterfaceExtension;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.FileUpdateInfo;
import com.sap.engine.services.deploy.container.ProgressListener;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.deploy.container.op.start.ApplicationStartInfo;
import com.sap.engine.services.deploy.container.op.start.ContainerStartInfo;
import com.sap.engine.services.deploy.container.rtgen.Generator;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.WebServicesContainer;
import com.sap.engine.services.webservices.server.container.OfflineBaseServiceContext;
import com.sap.engine.services.webservices.server.container.metadata.InterfaceDefMetaData;
import com.sap.engine.services.webservices.server.container.metadata.InterfaceDefMetaDataRegistry;
import com.sap.engine.services.webservices.server.deploy.jee5.WebServicesModuleDetector;
import com.sap.engine.services.webservices.server.deploy.migration.ws.MigrationController;
import com.sap.engine.services.webservices.server.deploy.preprocess.WebServicesGenerator;
import com.sap.engine.services.webservices.server.deploy.ws.WS630DeployProcess;
import com.sap.engine.services.webservices.server.deploy.ws.WSDeployProcess;
import com.sap.engine.services.webservices.server.deploy.ws.WSInitialStartProcess;
import com.sap.engine.services.webservices.server.deploy.ws.WSRemoveProcess;
import com.sap.engine.services.webservices.server.deploy.ws.WSStartProcess;
import com.sap.engine.services.webservices.server.deploy.ws.WSStopProcess;
import com.sap.engine.services.webservices.server.deploy.ws.WSUpdateProcess;
import com.sap.engine.services.webservices.server.deploy.wsclients.WSClientsDeployProcess;
import com.sap.engine.services.webservices.server.deploy.wsclients.WSClientsInitialStartProcess;
import com.sap.engine.services.webservices.server.deploy.wsclients.WSClientsStartProcess;
import com.sap.engine.services.webservices.server.deploy.wsclients.WSClientsStopProcess;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.AppDeployInfo;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDeployGenerator;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleFileStorageHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClients630InitialStartProcess;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsDeployGenerator;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsDeployManager;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsRuntimeActivator;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.deploy.WSClientsAppDeployResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.update.WSClientsUpdateManager;

/**
 * Title: WebServicesDeployManager
 * Description: WebServicesDeployManager   
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WebServicesDeployManager implements ContainerInterfaceExtension {   
       
  public static final String WEBSERVICES_CONTAINER_NAME = "webservices_container";    
  
  private Hashtable<String, Properties> deployProperties; 

  private Hashtable<String, WSDeployProcess> wsDeployProcesses;
  private Hashtable<String, WS630DeployProcess> ws630DeployProcesses; 
  private Hashtable<String, WSClientsDeployProcess> wsClientsDeployProcesses;  
  
  private Hashtable<String, WSStartProcess> wsStartProcesses;      
  private Hashtable<String, WSClients630InitialStartProcess> wsClients630InitialStartProcesses; 
  private Hashtable<String, WSClientsStartProcess> wsClientsStartProcesses;
                      
  private String serviceTempDir;        
  private ApplicationServiceContext appServiceContext; 
  private RuntimeProcessingEnvironment runtimeProcessingEnvironment; 
  private DeployCommunicator deployCommunicator;
  private WSClientsDeployManager wsClientsDeployManager630;      
  private WSClientsRuntimeActivator wsClientsRuntimeActivator630;
  private WSClientsUpdateManager wsClientsUpdateManager630; 
  private Generator webServicesGenerator; 
   
  public WebServicesDeployManager() {   
    this.webServicesGenerator = new WebServicesGenerator();
  }
       
  public ContainerInfo getContainerInfo() {
    ContainerInfo containerInfo = new ContainerInfo();    
    containerInfo.setJ2EEContainer(false);
    containerInfo.setFileExtensions(new String[]{".jar", ".par", ".war", ".wsar"});    
    containerInfo.setName(WEBSERVICES_CONTAINER_NAME);
    containerInfo.setServiceName("tc~je~webservices~srv");
    containerInfo.setModuleName("webservices");               
    containerInfo.setSupportingParallelism(true);
    containerInfo.setNeedStartInitially(true);
    containerInfo.setModuleDetector(new WebServicesModuleDetector());
    containerInfo.setGenerator(webServicesGenerator);          
    containerInfo.setPriority(77);
    return containerInfo;    
  }
  
  public void init(ApplicationServiceContext appServiceContext, RuntimeProcessingEnvironment runtimeProcessingEnvironment, DeployCommunicator deployCommunicator) {
    this.appServiceContext = appServiceContext; 
    this.runtimeProcessingEnvironment = runtimeProcessingEnvironment; 
    this.deployCommunicator = deployCommunicator;         
    this.serviceTempDir = appServiceContext.getServiceState().getWorkingDirectoryName();
    this.wsClientsDeployManager630 = new WSClientsDeployManager(new WSClientsDeployGenerator(new ModuleDeployGenerator(), new ModuleFileStorageHandler())); 
    this.wsClientsRuntimeActivator630 = new WSClientsRuntimeActivator();
    this.wsClientsUpdateManager630 = new WSClientsUpdateManager(wsClientsDeployManager630, null, null, null);
  }
  
  /**
   * @return DeployCommunicator
   */
  public DeployCommunicator getDeployCommunicator() {
    return deployCommunicator;
  }
  
  /**
   * @return - a hashtable of deploy properties
   */
  public Hashtable<String, Properties> getDeployProperties() {
    if(deployProperties == null) {
      deployProperties = new Hashtable<String, Properties>();
    }
    
    return deployProperties; 
  }
  
  /**
   * @return - a hashtable of web services deploy processes
   */
  public synchronized Hashtable<String, WSDeployProcess> getWsDeployProcesses() {
    if(wsDeployProcesses == null) {
      wsDeployProcesses = new Hashtable<String, WSDeployProcess>();
    }
    
    return wsDeployProcesses;
  }
    
  /**
   * @return - a hashtable of web services 630 deploy processes
   */
  public synchronized Hashtable<String, WS630DeployProcess> getWs630DeployProcesses() {
    if(ws630DeployProcesses == null) {
      ws630DeployProcesses = new Hashtable<String, WS630DeployProcess>();
    }
    
    return ws630DeployProcesses;
  }
  
  /**
   * @return - a hashtable of ws clients deploy processes
   */
  public synchronized Hashtable<String, WSClientsDeployProcess> getWsClientsDeployProcesses() {
    if(wsClientsDeployProcesses == null) {
      wsClientsDeployProcesses = new Hashtable<String, WSClientsDeployProcess>();
    }
    
    return wsClientsDeployProcesses;
  }
  
  /**
   * @return - a hashtable of web service start processes
   */ 
  public synchronized Hashtable<String, WSStartProcess> getWsStartProcesses() {
    if(wsStartProcesses == null) {
      wsStartProcesses = new Hashtable<String, WSStartProcess>(); 
    }
    
    return wsStartProcesses;
  }

  /**
   * @return - a hashtable of ws clients 630 initial start processes
   */
  public synchronized Hashtable<String, WSClients630InitialStartProcess> getWsClients630InitialStartProcesses() {
    if(wsClients630InitialStartProcesses == null) {
      wsClients630InitialStartProcesses = new Hashtable<String, WSClients630InitialStartProcess>();
    }
    
    return wsClients630InitialStartProcesses;
  } 
  
  /**
   * @return - a hashtable of ws clients start processes
   */
  public synchronized Hashtable<String, WSClientsStartProcess> getWsClientsStartProcesses() {
    if(wsClientsStartProcesses == null) {
      wsClientsStartProcesses = new Hashtable<String, WSClientsStartProcess>();
    }
    
    return wsClientsStartProcesses;
  }
      
  public String getApplicationName(File standaloneFile) throws DeploymentException {
    //TODO
    return null; 
  }
  
  public ApplicationDeployInfo deploy(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {
    String applicationName = dInfo.getApplicationName();              
              
    ApplicationDeployInfo applicationDeployInfoWebServices; 
    ApplicationDeployInfo applicationDeployInfoWebServices630;
    WSClientsAppDeployResult wsClientsAppDeployResult; 
    ApplicationDeployInfo wsClientsApplicationDeployInfo;    
    try {                  	
      String webServicesContainerDir = deployCommunicator.getMyWorkDirectory(applicationName);       
      String webServicesContainerTempDir = serviceTempDir + "/apps/" + applicationName;    
      Hashtable webModuleMappings = getWebModuleMappings(dInfo); 
      getDeployProperties().put(applicationName, props);        
       
      applicationDeployInfoWebServices = deployWebServices(applicationName, webServicesContainerDir, webServicesContainerTempDir, dInfo, webModuleMappings, archiveFiles);       
      applicationDeployInfoWebServices630 = deployWebServices630(applicationName, webServicesContainerDir, webServicesContainerTempDir, dInfo, archiveFiles);
      wsClientsAppDeployResult = deployWebServicesClients630(applicationName, webServicesContainerDir, webServicesContainerTempDir, dInfo, archiveFiles);           
      wsClientsApplicationDeployInfo = deployWSClients(applicationName, webServicesContainerDir, webServicesContainerTempDir, dInfo, webModuleMappings, archiveFiles);         
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      //TODO - use pattern keys
      throw new WSDeploymentException(WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{"Exception occurred during web services deployment for application " + applicationName + "." }, e); 
    }        
                      
    ApplicationDeployInfo applicationDeployInfo = makeApplicationDeployInfo(new ApplicationDeployInfo[]{applicationDeployInfoWebServices, applicationDeployInfoWebServices630, wsClientsApplicationDeployInfo}, wsClientsAppDeployResult);       
    if(applicationDeployInfo == null || applicationDeployInfo.getDeployedComponentNames() == null || applicationDeployInfo.getDeployedComponentNames().length == 0) {
      getDeployProperties().remove(applicationName);   
    }    
    
    return applicationDeployInfo; 
  }
  
  public void prepareDeploy(String applicationName, Configuration appConfiguration) throws DeploymentException, WarningException {        
    ArrayList<String> warnings = new ArrayList<String>(); 
    
	try {    
      WSDeployProcess wsDeployProcess = ((WSDeployProcess)getWsDeployProcesses().get(applicationName));
      if(wsDeployProcess != null) {
        wsDeployProcess.postProcess();	  
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);   
    }
    
    try {
      WS630DeployProcess ws630DeployProcess = ((WS630DeployProcess)getWs630DeployProcesses().get(applicationName));
      if(ws630DeployProcess != null) {
        ws630DeployProcess.postProcess(); 	  
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);   
    }
    
    try {    
      WSClientsDeployProcess wsClientsDeployProcess = ((WSClientsDeployProcess)getWsClientsDeployProcesses().get(applicationName));
      if(wsClientsDeployProcess != null) {
        wsClientsDeployProcess.postProcess();   	  
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);   
    }
            
    try {
      wsClientsDeployManager630.postDeploy(applicationName);
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings); 
    } 
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    }                    
  }
  
  public void notifyDeployedComponents(String applicationName, Properties props) throws WarningException {
    ArrayList<String> warnings = new ArrayList<String>(); 
    
	try {    
      WSDeployProcess wsDeployProcess = (WSDeployProcess)getWsDeployProcesses().get(applicationName);
      if(wsDeployProcess != null) {
        wsDeployProcess.notifyProcess(); 	  
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);     	
    }
    
    try {    
      WS630DeployProcess ws630DeployProcess = (WS630DeployProcess)getWs630DeployProcesses().get(applicationName);
      if(ws630DeployProcess != null) {
        ws630DeployProcess.notifyProcess(); 	  
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);         
    }
    
    try {    
      WSClientsDeployProcess wsClientsDeployProcess = (WSClientsDeployProcess)getWsClientsDeployProcesses().get(applicationName);
      if(wsClientsDeployProcess != null) {
        wsClientsDeployProcess.notifyProcess();
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings); 	      
    }
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    }
  }

  public void commitDeploy(String applicationName) throws WarningException {     
    ArrayList<String> warnings = new ArrayList<String>(); 
    
	try {    
      WSDeployProcess wsDeployProcess = ((WSDeployProcess)getWsDeployProcesses().remove(applicationName));
      if(wsDeployProcess != null) {
        wsDeployProcess.commitProcess(); 	  
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);       
    }
    
    try {    
      WS630DeployProcess ws630DeployProcess = ((WS630DeployProcess)getWs630DeployProcesses().remove(applicationName));
      if(ws630DeployProcess != null) {
        ws630DeployProcess.commitProcess(); 	  
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }
    
    try {    
      WSClientsDeployProcess wsClientsDeployProcess = ((WSClientsDeployProcess)getWsClientsDeployProcesses().remove(applicationName));
      if(wsClientsDeployProcess != null) {
        wsClientsDeployProcess.commitProcess(); 	  
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }
    
    try {
      wsClientsDeployManager630.commitDeploy(applicationName);
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings); 
    }  
    
    getDeployProperties().remove(applicationName);			
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    }    
  }
  
  public void rollbackDeploy(String applicationName) throws WarningException {             
    ArrayList<String> warnings = new ArrayList<String>();
     
	try {
      WSDeployProcess wsDeployProcess = ((WSDeployProcess)getWsDeployProcesses().remove(applicationName)); 
      if(wsDeployProcess != null) {
        wsDeployProcess.rollbackProcess();      
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }
    
    try {    
      WS630DeployProcess ws630deployProcess = ((WS630DeployProcess)getWs630DeployProcesses().remove(applicationName)); 
      if(ws630deployProcess != null) {      
        ws630deployProcess.rollbackProcess();
      }      
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }
       
    try {    
      WSClientsDeployProcess wsClientsDeployProcess = ((WSClientsDeployProcess)getWsClientsDeployProcesses().remove(applicationName));
      if(wsClientsDeployProcess != null) {
        wsClientsDeployProcess.rollbackProcess();
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }
    
    try {
      String webServicesContainerDir = deployCommunicator.getMyWorkDirectory(applicationName);       
      wsClientsDeployManager630.rollbackDeploy(applicationName, webServicesContainerDir);
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings); 
    } catch(Exception e ) {
      //TODO - add message 	
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));
      warnings.add(strWriter.toString()); 
    }
    
    getDeployProperties().remove(applicationName);		
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()])); 
      throw e;
    }
  }
  
  public boolean needUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException, WarningException {
    return true; 
  }

  public boolean needStopOnUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException, WarningException {    
    return true; 
  }
  
  public ApplicationDeployInfo makeUpdate(File[] archiveFiles, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {	    
    ArrayList<String> warnings = new ArrayList<String>(); 
    
	String applicationName = dInfo.getApplicationName(); 
    Configuration appConfiguration = dInfo.getConfiguration(); 
    WebServicesDeploymentInterface.PROCESS_TYPE.set(WebServicesDeploymentInterface.UPDATE_PROCESS);     
    ApplicationDeployInfo applicationDeployInfo = null; 
    
    try {
      commitRemove(applicationName);
    } catch(WarningException e) {
	  //TODO - add message 
      WSUtil.addStrings(((WSWarningException)e).getWarnings(), warnings); 
    }

    try {
      appConfiguration.deleteSubConfigurations(new String[]{WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME});    
    } catch(Exception e) {
      // TODO - add message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));
      warnings.add(strWriter.toString()); 	
    }    
    
    try {   
      applicationDeployInfo = deploy(archiveFiles, dInfo, props);
      //TODO - add message (warnings)
    } catch(WSDeploymentException e) {
      //TODO - use pattern keys 
      throw new WSDeploymentException(WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{"Exception occurred during web services update for application " + applicationName + "." }, e);
    }
    
    if(warnings != null && warnings.size() != 0) {
      applicationDeployInfo.addWarnings(warnings.toArray(new String[warnings.size()]));	
    }
       
	return applicationDeployInfo;
  }
  
  public void prepareUpdate(String applicationName) throws DeploymentException, WarningException {
    ArrayList<String> warnings = new ArrayList<String>();  
	
    try {
      try {
	    prepareDeploy(applicationName, null);
      } catch(WSWarningException e) {
        //TODO - add message 
    	WSUtil.addStrings(e.getWarnings(), warnings);  
      }
    } catch(WSDeploymentException e) {
      //TODO - use pattern keys
      throw new WSDeploymentException(WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{"Exception occurred during web services update for application " + applicationName + "." }, e); 
    }
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()])); 
      throw e;
    }
  }
  
  public void notifyUpdatedComponents(String applicationName, Configuration applicationConfig, Properties props) throws WarningException {
    ArrayList<String> warnings = new ArrayList<String>(); 
    
	try {
	  notifyDeployedComponents(applicationName, props);
    } catch(WSWarningException e) {
      //TODO - add warning message
      WSUtil.addStrings(e.getWarnings(), warnings); 
    } 
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()])); 
      throw e;
    }    
  }

  public ApplicationDeployInfo commitUpdate(String applicationName) throws WarningException {
    ArrayList<String> warnings = new ArrayList<String>();
    
    try {
      commitDeploy(applicationName);
    } catch(WarningException e) {
      //TODO - add warning message 
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }  
       
    WebServicesDeploymentInterface.PROCESS_TYPE.remove();
    
    //TODO - warnings
    
    return null; 
  }

  public void rollbackUpdate(String applicationName, Configuration appConfiguration, Properties props) throws WarningException {
    ArrayList<String> warnings = new ArrayList<String>(); 
    
    try {
      commitRemove(applicationName);
    } catch(WSWarningException e) {
      //TODO - add warning message 
      WSUtil.addStrings(e.getWarnings(), warnings); 
    }
    
    String webServicesContainerDir = null;
    String webServicesContainerTempDir = null; 
    try {
      webServicesContainerDir = deployCommunicator.getMyWorkDirectory(applicationName);       
      webServicesContainerTempDir = serviceTempDir + "/apps/" + applicationName;      
    } catch(Exception e) {
      //TODO - add warning message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));
      warnings.add(strWriter.toString());    
    }

    try {
      new WSStartProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, null, appConfiguration, null, null).preProcess();          
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);	
    } catch(Exception e) {
      //TODO - add warning message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));
      warnings.add(strWriter.toString());	
    }
             
    try {
      (new WSUpdateProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, appConfiguration, runtimeProcessingEnvironment)).rollbackProcess();  
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }         
    
    try {
      wsClientsUpdateManager630.rollbackUpdate(applicationName, webServicesContainerDir, appConfiguration, props, false);                     
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);  
    }       
    
    WebServicesDeploymentInterface.PROCESS_TYPE.remove();
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    } 
  }

  public void remove(String applicationName) throws DeploymentException, WarningException {
	  
  }
  
  public void remove(String applicationName, ConfigurationHandler configHandler, Configuration appConfig) throws DeploymentException, WarningException {
	  
  }

  public void notifyRemove(String applicationName) throws WarningException {              
  
  }

  public void commitRemove(String applicationName) throws WarningException {
    //TODO - add implementation for J2EE Engine 7.1       
	ArrayList<String> warnings = new ArrayList<String>(); 
  
    String webServicesContainerDir = null; 
    try {    
      webServicesContainerDir = deployCommunicator.getMyWorkDirectory(applicationName);           
    } catch(Exception e) {
      //TODO - add message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));
      warnings.add(strWriter.toString()); 
    }     
  
    try {      
      new WebServicesRemoveProcess(applicationName, webServicesContainerDir, new WSRemoveProcess(applicationName, runtimeProcessingEnvironment)).commitProcess();
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings); 
    }
    
    try {     
      wsClientsDeployManager630.remove(applicationName, webServicesContainerDir);
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);     
    } 
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    }  
  }   

  public void downloadApplicationFiles(String applicationName, Configuration appConfiguration) throws DeploymentException, WarningException {   
    ArrayList<String> warnings = new ArrayList<String>(); 
    
    try {    
      String webServicesContainerDir = deployCommunicator.getMyWorkDirectory(applicationName); 
      String webServicesContainerTempDir = serviceTempDir + "/apps/" + applicationName;                                  
             
      try {
        new WSStartProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, null, appConfiguration, WebServicesContainer.getServiceContext(), runtimeProcessingEnvironment).preProcess();      
      } catch(WSWarningException e) {
        WSUtil.addStrings(e.getWarnings(), warnings); 
      }
    
      WSStartProcess.download630(applicationName, webServicesContainerDir, appConfiguration);
      try {        
        wsClientsRuntimeActivator630.downloadApplicationFiles(applicationName, webServicesContainerDir, appConfiguration);      
      } catch(WSWarningException e) {
        WSUtil.addStrings(e.getWarnings(), warnings);     	
      }
    } catch(WSDeploymentException e) {
      throw e; 
    } catch(Exception e) {
      //TODO - use pattern keys
      throw new WSDeploymentException(WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{"Exception occurred during web services files download for application " + applicationName + "." }, e); 
    }
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    }    
  }
  
  public ApplicationStartInfo makeStartInitially(ContainerStartInfo cInfo) throws DeploymentException {
    String applicationName = cInfo.getApplicationName();        
    
    ApplicationStartInfo wsApplicationStartInfo = null;
    ApplicationStartInfo wsClientsApplicationStartInfo = null;
    ApplicationStartInfo wsClientsApplicationStartInfo630 = null;
    try {    
      String webServicesContainerDir = deployCommunicator.getMyWorkDirectory(applicationName);       
      String webServicesContainerTempDir = serviceTempDir + "/apps/" + applicationName;                                  
                   
      WSInitialStartProcess wsInitialStartProcess = new WSInitialStartProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, cInfo.getConfiguration(), cInfo.getLoader(), "");      
      wsInitialStartProcess.makeProcess();
      wsApplicationStartInfo = wsInitialStartProcess.getApplicationStartInfo();     
                        
      WSClientsInitialStartProcess wsClientsInitialStartProcess = new WSClientsInitialStartProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, cInfo.getConfiguration(), cInfo.getLoader(), getJarsPath());      
      wsClientsInitialStartProcess.makeProcess();
      wsClientsApplicationStartInfo = wsClientsInitialStartProcess.getApplicationStartInfo(); 
               
      WSClients630InitialStartProcess wsClients630InitialStartProcess = new WSClients630InitialStartProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, cInfo.getConfiguration(), cInfo.getLoader());
      getWsClients630InitialStartProcesses().put(applicationName, wsClients630InitialStartProcess); 
      wsClients630InitialStartProcess.makeProcess();
      wsClientsApplicationStartInfo630 = wsClients630InitialStartProcess.getApplicationStartInfo();     
    } catch(WSDeploymentException e) {
      throw e;  
    } catch(Exception e) {
      //TODO - use pattern keys
      throw new WSDeploymentException(WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{"Exception occurred during web services initial start for application " + applicationName + "." }, e); 
    }
    
    ApplicationStartInfo applicationStartInfo = unifyApplicationStartInfo(new ApplicationStartInfo[]{wsApplicationStartInfo, wsClientsApplicationStartInfo, wsClientsApplicationStartInfo630});
    
    return applicationStartInfo;         
  }  

  public void prepareStart(String applicationName, Configuration appConfiguration) throws DeploymentException, WarningException {                        
    ArrayList<String> warnings = new ArrayList<String>(); 
    	    
    try {    
      String webServicesContainerDir = deployCommunicator.getMyWorkDirectory(applicationName); 
      String webServicesContainerTempDir = serviceTempDir + "/apps/" + applicationName;                             
                
      WSStartProcess wsStartProcess = prepareStartWebServices(applicationName, webServicesContainerDir, webServicesContainerTempDir, appConfiguration);
      if(wsStartProcess != null) {
        if(wsStartProcess.getWarnings() != null && wsStartProcess.getWarnings().size() != 0) {
          warnings.addAll(wsStartProcess.getWarnings()); 	
        }	  
      }        
      
      WSClientsStartProcess wsClientsStartProcess = prepareStartWsClients(applicationName, webServicesContainerDir, webServicesContainerTempDir, appConfiguration);
      if(wsClientsStartProcess != null) {
        if(wsClientsStartProcess.getWarnings() != null &&  wsClientsStartProcess.getWarnings().size() != 0) {
          warnings.addAll(wsClientsStartProcess.getWarnings()); 	
        }	  
      }
      
      try {              
        WSClients630InitialStartProcess wsClients630StartProcess = (WSClients630InitialStartProcess)getWsClients630InitialStartProcesses().get(applicationName);          
        if(wsClients630StartProcess != null) {
          wsClients630StartProcess.startWSClients(applicationName, WSClientDirsHandler.getWSClientsDir(webServicesContainerDir), appConfiguration);
        } else {    	  
          wsClientsRuntimeActivator630.startWSClients(applicationName, WSClientDirsHandler.getWSClientsDir(webServicesContainerDir), appConfiguration);
        }
      } catch(WSWarningException e) {
        WSUtil.addStrings(e.getWarnings(), warnings);
      }  
    } catch(WSDeploymentException e) {
      throw e; 	
    } catch(Exception e) {
      //TODO - use pattern keys
      throw new WSDeploymentException(WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{"Exception occurred during web services start for application " + applicationName + "." }, e); 
    }
       
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    }      
  }

  public void commitStart(String applicationName) throws WarningException {   
    ArrayList<String> warnings = new ArrayList<String>();

    try {    
      ((WSStartProcess)getWsStartProcesses().remove(applicationName)).commitProcess();       
    } catch(WSWarningException e) {      
      WSUtil.addStrings(e.getWarnings(), warnings);  
    }
    
    try {    
      ((WSClientsStartProcess)getWsClientsStartProcesses().remove(applicationName)).commitProcess();            
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);      
    }     
         
    try {          
      WSClients630InitialStartProcess wsClients630StartProcess = (WSClients630InitialStartProcess)getWsClients630InitialStartProcesses().remove(applicationName);             
      if(wsClients630StartProcess != null) {
        wsClients630StartProcess.commitProcess();
      } else {
        wsClientsRuntimeActivator630.commitStart(applicationName);
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);       
    }
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    } 
  }

  public void rollbackStart(String applicationName) throws WarningException {
    ArrayList<String> warnings = new ArrayList<String>(); 
   
	try {    
      WSStartProcess wsStartProcess = ((WSStartProcess)getWsStartProcesses().remove(applicationName));
      if(wsStartProcess != null) {
        wsStartProcess.rollbackProcess();      
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);        
    }
            
    try {    
      WSClientsStartProcess wsClientsStartProcess = ((WSClientsStartProcess)getWsClientsStartProcesses().remove(applicationName));
      if(wsClientsStartProcess != null) {
        wsClientsStartProcess.rollbackProcess();      
      }
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);         
    }                              

    try {      
      WSClients630InitialStartProcess wsClients630StartProcess = (WSClients630InitialStartProcess)getWsClients630InitialStartProcesses().remove(applicationName);            
      if(wsClients630StartProcess != null) {
        wsClients630StartProcess.rollbackProcess();
      } else {
        wsClientsRuntimeActivator630.rollbackStart(applicationName);
      }      
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);
    }
     
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    } 
  }

  public void prepareStop(String applicationName, Configuration applicationConfig) throws DeploymentException, WarningException {        
	  
  }

  public void commitStop(String applicationName) throws WarningException {           
	ArrayList<String> warnings = new ArrayList<String>(); 
	
	try {    
      WSContainer.getImplementationContainerManager().stopApplication(applicationName); 
    } catch(Exception e) {
      //TODO - add message
      StringWriter strWriter = new StringWriter(); 
      e.printStackTrace(new PrintWriter(strWriter));
      warnings.add(strWriter.toString()); 
    }
    
    try {      
      new WSStopProcess(applicationName, WebServicesContainer.getServiceContext(), runtimeProcessingEnvironment).commitProcess(); 
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);             
    }
    
    try {    
      new WSClientsStopProcess(applicationName, WebServicesContainer.getServiceRefContext()).commitProcess();  
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);       
    }
     
    try {
      wsClientsRuntimeActivator630.stop(applicationName);
    } catch(WSWarningException e) {
      WSUtil.addStrings(e.getWarnings(), warnings);     	
    } 
    
    if(warnings != null && warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings.toArray(new String[warnings.size()]));
      throw e;
    }   
  }
  
  public void rollbackStop(String applicationName) throws WarningException {   
     
  }
   
  public void notifyRuntimeChanges(String applicationName, Configuration appConfig) throws WarningException {
        
  }

  public void prepareRuntimeChanges(String applicationName) throws DeploymentException, WarningException {
         
  }
  
  public ApplicationDeployInfo commitRuntimeChanges(String applicationName) throws WarningException {    
    return null;      
  }

  public void rollbackRuntimeChanges(String applicationName) throws WarningException {
    
  }

  public File[] getClientJar(String applicationName) {    
    String[] appLoaderResources = appServiceContext.getCoreContext().getLoadContext().getResourceNames(applicationName);     
    if(appLoaderResources == null || appLoaderResources.length == 0) {
      return new File[0];  
    }
    
    Vector appLoaderJarResources = new Vector();   
    File appLoaderJarResource;      
    for(int i = 0; i < appLoaderResources.length; i++) {  
      appLoaderJarResource = new File(appLoaderResources[i]);
      if(appLoaderJarResource.isFile()) {
        appLoaderJarResources.add(appLoaderJarResource); 
      }      
    }
    
    return (File[])appLoaderJarResources.toArray(new File[]{});      
  }

  public void addProgressListener(ProgressListener listener) {    
   
  }

  public void removeProgressListener(ProgressListener listener) {    
   
  }

  public boolean needStopOnSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException, WarningException {   
    return false;      
  }

  public ApplicationDeployInfo makeSingleFileUpdate(FileUpdateInfo[] files, ContainerDeploymentInfo dInfo, Properties props) throws DeploymentException {    
    return null;      
  }

  public void notifySingleFileUpdate(String applicationName, Configuration config, Properties props) throws WarningException {
   
  }
  
  public void prepareSingleFileUpdate(String applicationName) throws DeploymentException, WarningException {
         
  }

  public ApplicationDeployInfo commitSingleFileUpdate(String applicationName) throws WarningException {   
    return null;      
  }
  
  public void rollbackSingleFileUpdate(String applicationName, Configuration config) throws WarningException {
    
  }
  
  public void applicationStatusChanged(String applicationName, byte status) {
          
  }

  public String[] getResourcesForTempLoader(String applicationName) throws DeploymentException {    
    return null;      
  }

  public boolean acceptedAppInfoChange(String appName, AdditionalAppInfo addAppInfo) throws DeploymentException {    
    return false;      
  }

  public boolean needStopOnAppInfoChanged(String appName, AdditionalAppInfo addAppInfo) {    
    return false;       
  }
  
  public void makeAppInfoChange(String appName, AdditionalAppInfo addAppInfo, Configuration configuration) throws WarningException, DeploymentException {
           
  }
  
  public void notifyAppInfoChanged(String appName) throws WarningException {
      
  }

  public void appInfoChangedCommit(String appName) throws WarningException {
            
  }

  public void appInfoChangedRollback(String appName) throws WarningException {
          
  }
  
  private ApplicationDeployInfo deployWebServices(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, ContainerDeploymentInfo dInfo, Hashtable webModuleMappings, File[] archiveFiles) throws WSDeploymentException {      
    WSDeployProcess wsDeployProcessor = new WSDeployProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, getJarsPath(), archiveFiles, dInfo.getConfiguration(), runtimeProcessingEnvironment, webModuleMappings, dInfo.getAnnotations()); 
	getWsDeployProcesses().put(applicationName, wsDeployProcessor);	
	wsDeployProcessor.makeProcess();
		
    ApplicationDeployInfo applicationDeployInfo = wsDeployProcessor.getApplicationDeployInfo();    
    if(applicationDeployInfo.getDeployedComponentNames() == null || applicationDeployInfo.getDeployedComponentNames().length == 0) {
      getWsDeployProcesses().remove(applicationName); 
    }
    
    return applicationDeployInfo;    
  }
  
  private ApplicationDeployInfo deployWebServices630(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, ContainerDeploymentInfo dInfo, File[] archiveFiles) throws WSDeploymentException {
    WS630DeployProcess ws630DeployProcess = new WS630DeployProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, archiveFiles, dInfo.getConfiguration(), runtimeProcessingEnvironment, new MigrationController(webServicesContainerTempDir, webServicesContainerDir));         
    getWs630DeployProcesses().put(applicationName, ws630DeployProcess);    
    ws630DeployProcess.makeProcess();
    
    
    ApplicationDeployInfo applicationDeployInfo = ws630DeployProcess.getApplicationDeployInfo();    
    if(applicationDeployInfo.getDeployedComponentNames() == null || applicationDeployInfo.getDeployedComponentNames().length == 0) {
      getWs630DeployProcesses().remove(applicationName);
    }
    
    return applicationDeployInfo;   
  }
  
  private ApplicationDeployInfo deployWSClients(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, ContainerDeploymentInfo dInfo, Hashtable webModuleMappings, File[] archiveFiles) throws WSDeploymentException {
    WSClientsDeployProcess wsClientsDeployProcess = new WSClientsDeployProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, getJarsPath(), archiveFiles, dInfo.getConfiguration(), dInfo.getAnnotations(), webModuleMappings);          
    getWsClientsDeployProcesses().put(applicationName, wsClientsDeployProcess);        
    wsClientsDeployProcess.makeProcess();
        
    ApplicationDeployInfo applicationDeployInfo = wsClientsDeployProcess.getApplicationDeployInfo();    
    if(applicationDeployInfo.getDeployedComponentNames() == null || applicationDeployInfo.getDeployedComponentNames().length == 0) {
      getWsClientsDeployProcesses().remove(applicationName);
    }
    
    return applicationDeployInfo;    
  }

  private WSClientsAppDeployResult deployWebServicesClients630(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, ContainerDeploymentInfo dInfo, File[] archiveFiles) throws WSDeploymentException {    
    return wsClientsDeployManager630.deploy(webServicesContainerDir, webServicesContainerTempDir, getAppDeployInfo(dInfo), archiveFiles);            
  }
      
  private WSStartProcess prepareStartWebServices(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, Configuration appConfiguration) throws WSDeploymentException {
    ClassLoader appLoader = appServiceContext.getCoreContext().getLoadContext().getClassLoader(applicationName);
    
    WSStartProcess wsStartProcess = new WSStartProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, appLoader, appConfiguration, WebServicesContainer.getServiceContext(), runtimeProcessingEnvironment); 
    getWsStartProcesses().put(applicationName, wsStartProcess);
         
    wsStartProcess.makeProcess();
    
    return wsStartProcess; 
  }  
 
  private WSClientsStartProcess prepareStartWsClients(String applicationName, String webServicesContainerDir, String webServicesContainerTempDir, Configuration appConfiguration) throws WSDeploymentException {
    ClassLoader appLoader = appServiceContext.getCoreContext().getLoadContext().getClassLoader(applicationName);    
        
    WSClientsStartProcess wsClientsStartProcess = new WSClientsStartProcess(applicationName, webServicesContainerDir, webServicesContainerTempDir, appConfiguration, WebServicesContainer.getServiceRefContext(), appLoader); 
    getWsClientsStartProcesses().put(applicationName, wsClientsStartProcess);
         
    wsClientsStartProcess.makeProcess();
    
    return wsClientsStartProcess; 
  }
 
  private AppDeployInfo getAppDeployInfo(ContainerDeploymentInfo containerDeploymentInfo) {
    AppDeployInfo appDeployInfo = new AppDeployInfo();

    appDeployInfo.setApplicationName(containerDeploymentInfo.getApplicationName());    
    appDeployInfo.setLoader(containerDeploymentInfo.getLoader());  
    appDeployInfo.setAppConfiguration(containerDeploymentInfo.getConfiguration());
    appDeployInfo.setWebMappings(getWebModuleMappings(containerDeploymentInfo));

    return appDeployInfo;
  }

  private Hashtable getWebModuleMappings(ContainerDeploymentInfo containerDeploymentInfo) {
    String aliases[] = containerDeploymentInfo.getAliases();
    if(aliases == null) {
      return new Hashtable();
    }

    Hashtable webMappings = new Hashtable();
    for(int i = 0; i < aliases.length; i++) {
      webMappings.put(containerDeploymentInfo.getUri(aliases[i]), aliases[i]);
    }

    return webMappings;
  }
 
  private ApplicationDeployInfo makeApplicationDeployInfo(ApplicationDeployInfo[] appDeployInfoes, WSClientsAppDeployResult wsClientsAppDeployResult) {   
    if(appDeployInfoes == null) {    
      appDeployInfoes = new ApplicationDeployInfo[0];      
    } 
	  
	ApplicationDeployInfo applicationDeployInfo = new ApplicationDeployInfo();
        
    ApplicationDeployInfo currentApplicationDeployInfo;
    ArrayList<String> currentWarnings; 
    for(int i = 0; i < appDeployInfoes.length; i++) {
      currentApplicationDeployInfo = appDeployInfoes[i];
      applicationDeployInfo.setDeployedComponentNames(WSUtil.unifyStrings(new String[][]{applicationDeployInfo.getDeployedComponentNames(), currentApplicationDeployInfo.getDeployedComponentNames()}));
      applicationDeployInfo.setFilesForClassloader(WSUtil.unifyStrings(new String[][]{applicationDeployInfo.getFilesForClassloader(), currentApplicationDeployInfo.getFilesForClassloader()}));
      currentWarnings = currentApplicationDeployInfo.getWarnings(); 
      if(currentWarnings != null && currentWarnings.size() != 0) {        
        applicationDeployInfo.addWarnings(currentWarnings.toArray(new String[currentWarnings.size()])); 	  
      }      
    }
    
    applicationDeployInfo.setDeployedComponentNames(WSUtil.unifyStrings(new String[][]{applicationDeployInfo.getDeployedComponentNames(), wsClientsAppDeployResult.getWsClientsDeployResult().getDeployedComponentNames()}));     
    applicationDeployInfo.setFilesForClassloader(WSUtil.unifyStrings(new String[][]{applicationDeployInfo.getFilesForClassloader(), wsClientsAppDeployResult.getModuleDeployResult().getFilesForClassLoader(), wsClientsAppDeployResult.getWsClientsDeployResult().getFilesForClassLoader()}));
    Vector<String> warnings = wsClientsAppDeployResult.getWarnings();
    if(warnings != null && warnings.size() != 0) {
      applicationDeployInfo.addWarnings(warnings.toArray(new String[warnings.size()]));	
    }
    
    return applicationDeployInfo;  
  } 
  
  private ApplicationStartInfo unifyApplicationStartInfo(ApplicationStartInfo[] applicationStartInfoes) {
    if(applicationStartInfoes == null || applicationStartInfoes.length == 0) {
      return new ApplicationStartInfo(true); 
    }
    
    ApplicationStartInfo applicationStartInfo = new ApplicationStartInfo(true); 
    ApplicationStartInfo currentApplicationStartInfo;   
    ArrayList<String> warnings; 
    for(int i = 0; i < applicationStartInfoes.length; i++) {
      currentApplicationStartInfo = applicationStartInfoes[i];
      if(currentApplicationStartInfo != null) {        
        applicationStartInfo.setDeployedComponentNames(WSUtil.unifyStrings(new String[][]{applicationStartInfo.getDeployedComponentNames(), currentApplicationStartInfo.getDeployedComponentNames()})); 
        applicationStartInfo.setFilesForClassloader(WSUtil.unifyStrings(new String[][]{applicationStartInfo.getFilesForClassloader(), currentApplicationStartInfo.getFilesForClassloader()}));
        warnings = currentApplicationStartInfo.getWarnings();
        if(warnings != null && warnings.size() != 0) {
          applicationStartInfo.addWarnings(warnings.toArray(new String[warnings.size()]));    	
        }                 
      }     
    }
    
    return applicationStartInfo;     
  }
  
  public OfflineBaseServiceContext getOfflineServiceContext() throws ConfigurationException {
    OfflineBaseServiceContext offlineBaseServiceContext = new OfflineBaseServiceContext(); 
    loadOfflineWebServicesData(offlineBaseServiceContext);
    
    return offlineBaseServiceContext; 
  }
  
  public void loadOfflineWebServicesData(OfflineBaseServiceContext offlineBaseServiceContext) throws ConfigurationException {
    ConfigurationHandler configurationHandler = WSContainer.getServiceContext().getCoreContext().getConfigurationHandlerFactory().getConfigurationHandler();  
    
    try {
      Configuration appsConfiguration = null; 
      try {
        appsConfiguration = configurationHandler.openConfiguration(OfflineBaseServiceContext.APPS_CONFIGURATION_PATH, ConfigurationHandler.READ_ACCESS); 
      } catch(NameNotFoundException e) {
        // $JL-EXC$  	
      } catch(ConfigurationLockedException e) {
        //TODO 	
        throw e; 
      }
    
      if(appsConfiguration == null) {
        return; 	
      }    
    
      Iterator<Entry<String, Configuration>> iterator = appsConfiguration.getAllSubConfigurations().entrySet().iterator();
      Entry<String, Configuration>  entry;     
      while(iterator.hasNext()) {
        entry = iterator.next();  
        loadOfflineWebServicesData(entry.getKey(), entry.getValue(), offlineBaseServiceContext); 
      }    
    } finally {
      try {
        configurationHandler.closeAllConfigurations(); 	  
      }	catch(ConfigurationException e) {
    	// $JL-EXC$	  
      }
    }
  }
    
  private void loadOfflineWebServicesData(String providerName, Configuration providerConfiguration, OfflineBaseServiceContext offlineBaseServiceContext) throws ConfigurationException {
    Iterator<Entry<String, Configuration>> iterator = providerConfiguration.getAllSubConfigurations().entrySet().iterator();	
    Entry<String, Configuration> entry;      
    while(iterator.hasNext()) {  	
      entry = iterator.next(); 	  
	  loadWebServicesData(providerName + "/" + entry.getKey(), entry.getValue(), offlineBaseServiceContext); 
	}  
  }
  
  private void loadWebServicesData(String applicationName, Configuration appConfiguration, OfflineBaseServiceContext offlineBaseServiceContext) throws ConfigurationException {    
	Iterator<Entry<String, Configuration>> iterator = appConfiguration.getAllSubConfigurations().entrySet().iterator();
    Entry<String, Configuration> entry;
    String configurationName; 
    Configuration moduleConfiguration; 
    Configuration wsConfiguration;  
    while(iterator.hasNext()) {
      entry = iterator.next();
      configurationName = entry.getKey(); 
      moduleConfiguration = entry.getValue(); 
      if(moduleConfiguration.existsSubConfiguration(OfflineBaseServiceContext.WS_CONFIGURATION_NAME)) {
        wsConfiguration = moduleConfiguration.getSubConfiguration(OfflineBaseServiceContext.WS_CONFIGURATION_NAME);
        loadWSData(applicationName, configurationName, wsConfiguration, offlineBaseServiceContext); 
      }           
    }           
  } 
  
  private void loadWSData(String applicationName, String moduleName, Configuration wsConfiguration, OfflineBaseServiceContext offlineBaseServiceContext) throws ConfigurationException {
    loadInterfaceDefMetaDatas(applicationName, moduleName, (String[])wsConfiguration.getConfigEntry(OfflineBaseServiceContext.INTERFACE_DEFINITIONS_NAME), offlineBaseServiceContext.getInterfaceDefMetaDataRegistry());
  }
  
  private static void loadInterfaceDefMetaDatas(String applicationName, String moduleName, String[] interfaceDefinitionIds, InterfaceDefMetaDataRegistry interfaceDefMetaDataRegistry) {
    if(interfaceDefinitionIds == null || interfaceDefinitionIds.length == 0) {
      return; 	
    }
    
    for(String interfaceDefinitionId: interfaceDefinitionIds) {
      interfaceDefMetaDataRegistry.putInterfaceDefMetaData(interfaceDefinitionId, new InterfaceDefMetaData(applicationName, moduleName, interfaceDefinitionId));
    }    
  }
  
  public static String[] getWSReferences() {
    return new String[]{"library:sapxmltoolkit",
                        "library:webservices_lib",
                        "library:tc~bl~base_webservices_lib",
                        "interface:webservices",
                        "library:jaxrpc_api",
                        "library:jaxr_api"};
  }  
  
  public String getJarsPath() {
    return getJarsPath(appServiceContext);
  }
  
  public static String getJarsPath(ApplicationServiceContext appServiceContext) {
    String[] resourceNames = getWSReferences();
    LoadContext loadContext = appServiceContext.getCoreContext().getLoadContext();
    SystemMonitor systemMonitor = appServiceContext.getContainerContext().getSystemMonitor();
     
    ArrayList<String> jars = new ArrayList<String>();     
    String resource; 
    String resourceType;
    String resourceName; 
    for(int i = 0; i < resourceNames.length; i++) {
      resource = resourceNames[i];
      resourceType = resource.substring(0, resource.indexOf(":"));
      resourceName = resource.substring(resource.indexOf(":") + 1);     
      if(resourceType.equals("library")) {
        WSUtil.addStrings(systemMonitor.getLibrary(resourceName).getJars(), jars);
      }      
      if(resourceType.equals("interface")) {
        WSUtil.addStrings(systemMonitor.getInterface(resourceName).getJars(), jars); 
      }
      if(resourceType.equals("service")) {
        WSUtil.addStrings(systemMonitor.getService(resourceName).getJars(), jars); 
      }      
    }
    
    WSUtil.addStrings(loadContext.getResourceNames(WebServicesDeployManager.class.getClassLoader().getParent()), jars);  

    return WSUtil.concatStrings(jars, File.pathSeparator);
  }
  
}
