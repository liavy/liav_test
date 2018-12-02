package com.sap.engine.services.webservices.server.deploy.migration.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceState;
import com.sap.engine.lib.descriptors.ws04wsd.WebServiceDefinitionState;
import com.sap.engine.lib.descriptors.ws04wsdd.NameDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSConfigurationDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSDeploymentDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSDescriptor;
import com.sap.engine.lib.descriptors.ws04wsrt.WSRuntimeConfigurationDescriptor;
import com.sap.engine.lib.descriptors.ws04wsrt.WSRuntimeDescriptor;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationInfo;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationResult;
import com.sap.engine.services.deploy.container.migration.utils.CMigrationStatus;
import com.sap.engine.services.webservices.additions.soaphttp.TransportBindingIDs;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.MappingFactory;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.exceptions.WSConfigurationException;
import com.sap.engine.services.webservices.server.container.metadata.module.ModuleRuntimeData;
import com.sap.engine.services.webservices.server.container.ws.metaData.WSApplicationMetaDataContext;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.SchemaStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.SchemaTypeType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.TypeMappingFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlFileType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlStyleType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WsdlTypeType;
import com.sap.engine.services.webservices.server.deploy.migration.ws.exception.ConversionException;
import com.sap.engine.services.webservices.server.deploy.ws.WSDeployProcess;
import com.sap.engine.services.webservices.tools.ExceptionManager;
import com.sap.engine.services.webservices.webservices630.server.deploy.WSConfigurationHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleDeployGenerator;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.ModuleFileStorageHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.WSBaseConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSAppDeployResult;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDeployGenerator;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSRuntimeActivator;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSServerDeploySettingsProvider;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WebServicesConstants;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * MigrationController for controlling the migration process either for already deployed
 * webservices or if a webservice in old format is deployed.
 * Company: Sap Labs Sofia 
 * @author aneta-a
 */

public class MigrationController {

  private static final Location LOCATION = Location.getLocation(MigrationController.class); 

  private String WS_SOURCE_TMP_DIR;
  private String DESTINATION_CONFIG_DIR;  
  private WSDirsHandler wsDirsHandler;
  private String wsSourceDir;
  private String wsDestinationDir;
  private static NW04ConversionTool convertionTool = new NW04ConversionTool();
  
  public MigrationController(String wsSourceDir, String wsDestinationDir) {
    this.wsSourceDir      = wsSourceDir; 
    LOCATION.debugT("Webservice source directory in MigrationController is " + wsSourceDir);
    this.wsDestinationDir = wsDestinationDir;    
    LOCATION.debugT("Webservice destination directory in MigrationController is " + wsDestinationDir);
  }
  
  //called when an old archive is deployed
  private void generateTmpDirs(boolean migrate) {
    if (migrate) {
      WS_SOURCE_TMP_DIR = wsSourceDir;
    } else {
      WS_SOURCE_TMP_DIR = wsSourceDir + "/mgr/nw04";
    }
  }
  
  // method called when a request for deploying old archive comes
  public Hashtable deployNW04Archives(String applicationName, String wsDestDir, File[] moduleArchives) throws Exception {
    Hashtable moduleResult = null;  
    generateTmpDirs(false);
    String nw04TempDir = wsSourceDir + "/mgr/nw04_tmp";  
  
    ModuleDeployGenerator moduleDeployGenerator       = new ModuleDeployGenerator();
    ModuleFileStorageHandler moduleFileStorageHandler = new ModuleFileStorageHandler();
    WSServerDeploySettingsProvider wsServerDeploySettingsProvider = new WSServerDeploySettingsProvider();
    WSDeployGenerator wsDeployGenerator = new WSDeployGenerator(wsServerDeploySettingsProvider, wsServerDeploySettingsProvider, moduleDeployGenerator, moduleFileStorageHandler);  
     
    try {
	  WSAppDeployResult appDeployResult = wsDeployGenerator.generateDeployFiles(applicationName, getWebServicesDir(WS_SOURCE_TMP_DIR), nw04TempDir, moduleArchives);
	  if (appDeployResult.getDeployedComponentNames() == null || appDeployResult.getDeployedComponentNames().length == 0) {
	    return moduleResult;//returns null and then the result is checked, this happens when there are no old webservices for migration
	  } else {
	    moduleResult = gatherContentForApplication(applicationName, getWebServicesDir(WS_SOURCE_TMP_DIR), wsDestDir); 
	  }
	} catch (WSDeploymentException wsex) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "deployNW04Archives(String applicationName, String wsDestDir, File[] moduleArchives)", "Deployment in old format is unsuccessful. The application will not be started.", wsex);
	  throw wsex;
    } catch (Exception e) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "deployNW04Archives(String applicationName, String wsDestDir, File[] moduleArchives)", "Migration of " + applicationName + " is unsuccessful. The application will not be started.", e);
	  throw e;
	}
    return moduleResult;
  }

  private Hashtable gatherContentForApplication(String applicationName, String webservicesDir, String wsDestDir) throws Exception {     
    Hashtable dataForMigration = new Hashtable();              
    File wsDir = new File(webservicesDir);
    String[] files = wsDir.list();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (!WSBaseConstants.APP_JARS_NAME.equals(files[i])) {
          gatherContentForConfiguration(applicationName, webservicesDir, wsDestDir, files[i], dataForMigration);           
        } else {
          IOUtil.copyDir(IOUtil.getFilePath(webservicesDir, files[i]), IOUtil.getFilePath(wsDestinationDir, files[i]));       
        }         
      }
    }     
    return saveXMLFiles(dataForMigration, applicationName, wsDestDir);     
  }
  
  private Hashtable saveXMLFiles(Hashtable dataForMigration, String applicationName, String wsDestDir) throws Exception {
    Hashtable wsModuleResult = new Hashtable();    
    Enumeration keys = dataForMigration.keys();    
    while(keys.hasMoreElements()) {
      String jarName = (String)keys.nextElement();
      DESTINATION_CONFIG_DIR = getDestinationDir(wsDestDir, ModuleRuntimeData.getModuleDirName(jarName));
      //String configuration = new ModuleRuntimeData().getModuleDirName(jarName);
      WSMigrationData wsMigrationData = (WSMigrationData)dataForMigration.get(jarName); 
      
      Hashtable parsed;

        try {
          parsed = convertionTool.nw04ToParisConversion( wsMigrationData.getWsDeploymentDescriptor(), wsMigrationData.getWsRuntimeDescriptor(), wsMigrationData.getVi_wsdStructures(), applicationName);
        } catch (Exception parseExc) {
          ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "saveXMLFiles(Hashtable dataForMigration, String applicationName)", "Parsing ws-deployment-descriptor, Vitrual Interfaces and WebService Definitions failed. The application will not be started.", parseExc);
          throw new ConversionException(MigrationConstants.PARSING_PROBLEM, parseExc);
        }
                     
        try {
          MappingFactory.save((MappingRules)parsed.get("mappings"), DESTINATION_CONFIG_DIR + WSBaseConstants.SEPARATOR + "mappings.xml");
        } catch (Exception mappingExc) {
          ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "saveXMLFiles(Hashtable dataForMigration, String applicationName)", "Saving mappings.xml file failed. The application will not be started.", mappingExc);
          throw new ConversionException(MigrationConstants.SAVE_MAPPINGSFILE, mappingExc);
        }   
        try {
          ConfigurationFactory.save((ConfigurationRoot)parsed.get("ConfigurationRoot"), DESTINATION_CONFIG_DIR + WSBaseConstants.SEPARATOR + "configurations.xml");             
        } catch (Exception configExc) {
          ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "saveXMLFiles(Hashtable dataForMigration, String applicationName)", "Saving configurations.xml file failed. The application will not be started.", configExc);
          throw new ConversionException(MigrationConstants.SAVE_CONFIGURATIONSFILE, configExc);
        } 
        
        try {
          WebServicesJ2EEEngineFactory.save(wsMigrationData.getWebservicesType(), getMetaInfPath(DESTINATION_CONFIG_DIR) + WSBaseConstants.SEPARATOR + MigrationConstants.WEBSERVICES_J2EE_ENGINE);     
        } catch (Exception j2eeExc) {
          ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "saveXMLFiles(Hashtable dataForMigration, String applicationName)", "Saving webservices-j2ee-engine.xml file failed. The application will not be started.", j2eeExc);
          throw new ConversionException(MigrationConstants.SAVE_WEBSERVICESJ2EEENGINEFILE, j2eeExc);
        } 

        WSModuleMigrationResult result = new WSModuleMigrationResult();
        result.setModuleName(jarName);
        result.setModuleDir(DESTINATION_CONFIG_DIR);
        result.setConfigurationDescriptor((ConfigurationRoot)parsed.get("ConfigurationRoot"));
        result.setMappingDescriptor((MappingRules)parsed.get("mappings"));
        result.setWebservicesJ2EEEngineDescriptor(wsMigrationData.getWebservicesType());     
        wsModuleResult.put(jarName, result);
     }
     
    if (DESTINATION_CONFIG_DIR != null && dataForMigration.size() != 0) {
	  try {
	    IOUtil.copyDir(wsDestinationDir + "/app_jars", DESTINATION_CONFIG_DIR + "/app_jars");   
	    IOUtil.deleteDir(wsDestinationDir + "/app_jars");    
	  } catch (IOException e1) {
	    // $JL-EXC$
	    Category.SYS_SERVER.infoT(LOCATION, "Directory app_jars was not copied.");      
	  }  
    }          
    
    return wsModuleResult;
  }
    
  //method called from WebServicesMigrator, appsDirs references the real apps dir on the server
  public CMigrationResult migrateNW04DeployedContent(CMigrationInfo cMigInfo, String appsDir) throws Exception  {
    generateTmpDirs(true);
    return migrateNW04DeployedContent(cMigInfo.getAppName(), cMigInfo.getAppConfig(), appsDir);
  }
    
  public CMigrationResult migrateNW04DeployedContent(String applicationName, Configuration applicationConfiguration, String appsDir) throws Exception {
    LOCATION.entering("migrateNW04DeployedContent(String applicationName, Configuration applicationConfiguration, String appsDir)");

    Vector migrationResult = new Vector();
    Hashtable moduleMigrationResult = gatherDeployedContentForApplication(applicationName, applicationConfiguration, migrationResult, appsDir);        
    uploadConfiguration(applicationConfiguration, moduleMigrationResult);
    LOCATION.pathT("After uploading configuration {0} to Database.", new Object[] { applicationConfiguration });

    CMigrationResult resultMigration = new CMigrationResult(); 
    String[] result = new String[migrationResult.size()];
    migrationResult.copyInto(result);
    resultMigration.setFilesForClassLoader(result);   

    LOCATION.exiting("migrateNW04DeployedContent(String applicationName, Configuration applicationConfiguration, String appsDir)");
    return resultMigration;
  }
 
  //method called for the current application, returns WSModuleMigrationResult, needs Vector object for CMigrationResult
  public Hashtable gatherDeployedContentForApplication(String applicationName, Configuration applicationConfiguration, Vector migrationResult, String appsDir) throws Exception {
    Hashtable dataForMigration = new Hashtable();       
    mkDir(WS_SOURCE_TMP_DIR); 
    LOCATION.debugT("WS_SOURCE_TMP_DIR value is {0}.", new Object[] { WS_SOURCE_TMP_DIR } );
    
    String webServicesDir = getWebServicesDir(WS_SOURCE_TMP_DIR);
    LOCATION.debugT("webServicesDir value is {0}.", new Object[] { webServicesDir });

    WSDeploymentDescriptor wsDeploymentDescriptor = null;          
	try {
	  WSConfigurationHandler.downloadWebServicesConfiguration(webServicesDir, applicationConfiguration);
	} catch (WSConfigurationException e) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "gatherDeployedContentForApplication(String applicationName, Configuration applicationConfiguration, Vector migrationResult, String appsDir)", "Impossible to download " + applicationConfiguration + " configuration. Application can't be migrated.", e);
      throw e;
	}
    File[] filesInWebservicesDir = new File(webServicesDir).listFiles();
    String fileName;
    for (int i = 0; i < filesInWebservicesDir.length; i++) {
      fileName = filesInWebservicesDir[i].getName();
      if (filesInWebservicesDir[i].isDirectory()) {
        if (!WSBaseConstants.APP_JARS_NAME.equals(fileName)) {
          // configuration different from app_jars        
	  	  String resultJar;
		  try {
			resultJar = appsDir + WSBaseConstants.SEPARATOR + gatherContentForConfiguration(applicationName, webServicesDir, wsDestinationDir, fileName, dataForMigration);                 
		  } catch (Exception e) {
            ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "gatherDeployedContentForApplication(String applicationName, Configuration applicationConfiguration, Vector migrationResult, String appsDir)", "Migration of " + applicationName + " is unsucceful. The application will not be started.", e);
            throw e;
		  }
            LOCATION.debugT("Result jar is {0}.", new Object[] { resultJar });              
            migrationResult.addElement(resultJar);        
          } else {             
			try {
            IOUtil.copyDir(IOUtil.getFilePath(webServicesDir, fileName), IOUtil.getFilePath(wsDestinationDir, fileName));       
		  } catch (IOException ioe) {
            //$JL-EXC$
            LOCATION.debugT("Configuration app_jars was not copied.");
		  }
        }
      }
    }

    return saveXMLFiles(dataForMigration, applicationName, wsDestinationDir);
  } 
  
  // returns relative path to the jar that has to be loaded, i.e. ws_0/types/*.jar
  public String gatherContentForConfiguration(String applicationName, String webServicesDir, String destDir, String configuration, Hashtable dataForMigration) throws Exception { 
    //  this directory is in the following format: ../applicationName/webservices_container/ws_0 (ws_0 is the current configuration)
    String wsConfigDir = IOUtil.getFilePath(webServicesDir, configuration);       
    Properties mappings = null;
    if (hasMappings(wsConfigDir)) {
      mappings = WSRuntimeActivator.loadMappingsFile(WSDirsHandler.getMappingsPath(wsConfigDir));
    } else {
      mappings = WSDirsHandler.generateBaseDefaultMappings();
    } 
    wsDirsHandler = new WSDirsHandler(mappings, configuration);   
    WSDeploymentDescriptor wsDeploymentDescriptor = NW04ConversionTool.loadWSDeploymentDescriptor(wsDirsHandler.getWSDeploymentDescriptorPath(wsConfigDir, mappings));
    
    LOCATION.debugT("ws-deployment-descriptor.xml path is " + wsDirsHandler.getWSDeploymentDescriptorPath(wsConfigDir, mappings));
    WSDescriptor[] webServices = wsDeploymentDescriptor.getWebservice();  
    
    WSRuntimeDescriptor wsRuntimeDescriptor = NW04ConversionTool.loadWSRuntimeDescriptor(wsDirsHandler.getWSRuntimeDescriptorPath(wsConfigDir, mappings));
    WSRuntimeConfigurationDescriptor[] newRuntimeCongigs = wsRuntimeDescriptor.getWsRuntimeConfiguration();   
    
    String jarName = wsRuntimeDescriptor.getJarName();
    LOCATION.debugT("Name of the jar from the ws-runtime-descriptor is " + jarName);
      
    DESTINATION_CONFIG_DIR = getDestinationDir(destDir, ModuleRuntimeData.getModuleDirName(jarName));
    if (!new File(DESTINATION_CONFIG_DIR).exists()) mkDir(getMetaInfPath(DESTINATION_CONFIG_DIR));           
    
    LOCATION.debugT("DESTINATION_CONFIG_DIR value is " + DESTINATION_CONFIG_DIR);
    WSMigrationData migrationData = (WSMigrationData)dataForMigration.get(jarName);
    if (migrationData != null) {   
      LOCATION.infoT("WSMigrationData object already exists."); 
      //ws-deployment-descriptor   
      Vector newWebservicesData = new Vector();
      WSDescriptor[] oldWebservices =  migrationData.getWsDeploymentDescriptor().getWebservice();
      for (int i = 0; i < oldWebservices.length; i++) {
        newWebservicesData.addElement(oldWebservices[i]);
      }
      for (int i = 0; i < webServices.length; i++) {
        newWebservicesData.addElement(webServices[i]);
      }
      WSDescriptor[] newWebservices = new WSDescriptor[newWebservicesData.size()];
      newWebservicesData.copyInto(newWebservices);
      migrationData.getWsDeploymentDescriptor().setWebservice(newWebservices);

      //ws-runtime-descriptor
      Vector newRuntimeConfigs = new Vector();
      WSRuntimeConfigurationDescriptor[] oldRuntimeConfigs = migrationData.getWsRuntimeDescriptor().getWsRuntimeConfiguration();
      for (int i = 0; i < oldRuntimeConfigs.length; i++) {
        newRuntimeConfigs.addElement(oldRuntimeConfigs[i]);
      }
      for (int i = 0; i < newRuntimeCongigs.length; i++) {
        newRuntimeConfigs.addElement(newRuntimeCongigs[i]);
      }
      WSRuntimeConfigurationDescriptor[] runtimeConfigurations = new WSRuntimeConfigurationDescriptor[newRuntimeConfigs.size()];
      newRuntimeConfigs.copyInto(runtimeConfigurations);
      migrationData.getWsRuntimeDescriptor().setWsRuntimeConfiguration(runtimeConfigurations);
      
      // vis and wsds
      for (int i = 0; i < webServices.length; i++) {
        migrationData.setVi_wsdStructures(getVI_WSD(webServices[i], migrationData.getVi_wsdStructures(), wsConfigDir, mappings));				
			}    
     
      // webservices-j2ee-engine
      Vector newJ2eeEngine = new Vector();
      WebserviceDescriptionType[] oldDescrType = migrationData.getWebservicesType().getWebserviceDescription();
      for (int i = 0; i < oldDescrType.length; i++) {
        newJ2eeEngine.addElement(oldDescrType[i]);
      }
      WebserviceDescriptionType[] newDescrType = getWebserviceDescription(webServices, wsConfigDir, mappings);
      for (int i = 0; i < newDescrType.length; i++) {
        newJ2eeEngine.addElement(newDescrType[i]);
      }
      WebserviceDescriptionType[] resultJ2EE = new WebserviceDescriptionType[newJ2eeEngine.size()];
      newJ2eeEngine.copyInto(resultJ2EE);
      migrationData.getWebservicesType().setWebserviceDescription(resultJ2EE);
      
    } else {
      LOCATION.infoT("WSMigrationData object is created.");
      migrationData = new WSMigrationData(jarName);
      //set runtime descriptor 
      migrationData.setWsRuntimeDescriptor(wsRuntimeDescriptor);
      //set ws-deployment-descriptor
      migrationData.setWsDeploymentDescriptor(wsDeploymentDescriptor);
      //set vis and wsds
      for (int i = 0; i < webServices.length; i++) {
        migrationData.setVi_wsdStructures(getVI_WSD(webServices[i], new Hashtable(), wsConfigDir, mappings));		
			}
      //set webservices-j2ee-engine.xml content
      WebservicesType webservicesType = new WebservicesType();
      webservicesType.setConfigurationFile(MigrationConstants.CONFIGURATION_FILE);
      webservicesType.setWsdlMappingFile(MigrationConstants.MAPPING_FILE);
      webservicesType.setWebserviceDescription(getWebserviceDescription(webServices, wsConfigDir, mappings));
      migrationData.setWebservicesType(webservicesType);
      
      dataForMigration.put(jarName, migrationData);
    }
    String webserviceName = "";  
    for (int i = 0; i < webServices.length; i++) {
      webserviceName = webServices[i].getWebserviceInternalName();
    }
          
    return ModuleRuntimeData.getModuleDirName(jarName) + "/types/" + webserviceName + "/" + wsDirsHandler.getJarFileName(mappings);
  } 
  
  private Hashtable getVI_WSD(WSDescriptor webService, Hashtable vi_wsd, String wsConfigDir, Properties mappings) throws ConversionException {             
      WSConfigurationDescriptor[] wsConfiguration = webService.getWsConfiguration();
      for (int j = 0; j < wsConfiguration.length; j++) {
        NameDescriptor name = wsConfiguration[j].getServiceEndpointViRef();
        String viRelPath = generatePath(name, ".videf");
        if (vi_wsd.get(name.getName()) == null) {
          FileInputStream viStream = null;
					try {
            viStream = new FileInputStream(wsDirsHandler.getViPath(wsConfigDir, mappings, viRelPath));
						VirtualInterfaceState viObject = (VirtualInterfaceState)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04VI).parse(viStream);
						vi_wsd.put(name.getName(), viObject);
					} catch (Exception e) {
            String message = "Can't load Virtual Interface file from " + wsDirsHandler.getViPath(wsConfigDir, mappings, viRelPath);           
            ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, message, e);
            throw new ConversionException(MigrationConstants.LOAD_VIINTERFACE, new Object[] { wsDirsHandler.getViPath(wsConfigDir, mappings, viRelPath) }, e);
					} finally {
            if (viStream != null) {
             try {
               viStream.close(); 
             } catch(IOException e) {
               //$JL-EXC$
             }
           }
         }
        }
                  
        name = wsConfiguration[j].getWebserviceDefinitionRef(); 
        String wsdRelPath = generatePath(name , ".wsdef");
        if (vi_wsd.get(name.getName()) == null) {
          FileInputStream wsdStream = null;  
					try {
            wsdStream = new FileInputStream(wsDirsHandler.getWsdPath(wsConfigDir, mappings, wsdRelPath)); 
						WebServiceDefinitionState wsdObject = (WebServiceDefinitionState)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSD).parse(wsdStream);
						vi_wsd.put(name.getName(), wsdObject); 
					} catch (Exception e) {
            String message = "Can't load Webservice Definition file from " + wsDirsHandler.getWsdPath(wsConfigDir, mappings, wsdRelPath);           
            ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, message, e);
            throw new ConversionException(MigrationConstants.LOAD_WSD, new Object[] { wsDirsHandler.getWsdPath(wsConfigDir, mappings, wsdRelPath) }, e);
					} finally {
            if (wsdStream != null) {
             try {
               wsdStream.close(); 
             } catch(IOException e) {
               //$JL-EXC$
             }
           }
          }      				
        }
      } 
    return vi_wsd;
  }  
    
  public void uploadConfiguration(Configuration applicationConfiguration, Hashtable migrationResult) throws Exception {     
    LOCATION.entering("uploadConfiguration(Configuration applicationConfiguration, Hashtable migrationResult)");
    Configuration webServicesContainerConfiguration;    
    if(!applicationConfiguration.existsSubConfiguration(WSBaseConstants.WS_CONTAINER_NAME)) {
      webServicesContainerConfiguration = applicationConfiguration.createSubConfiguration(WSBaseConstants.WS_CONTAINER_NAME);
    } else {
      webServicesContainerConfiguration = applicationConfiguration.getSubConfiguration(WSBaseConstants.WS_CONTAINER_NAME);
    } 
  
    if (webServicesContainerConfiguration.existsSubConfiguration(WebServicesConstants.WEBSERVICES_CONFIG_NAME)) {
      try {
        webServicesContainerConfiguration.deleteSubConfigurations(new String[]{ WebServicesConstants.WEBSERVICES_CONFIG_NAME });
      } catch(Exception e) {
        //$JL-EXC$
        LOCATION.infoT("Configuration webservices was not deleted from Database.");

      }
    }   
    if (migrationResult.size() == 0) {
      return; 
    }
    
   WSModuleMigrationResult moduleResult;
   Configuration metaDataConfiguration = null;
   try {
     metaDataConfiguration = webServicesContainerConfiguration.createSubConfiguration(WSApplicationMetaDataContext.METADATA);
     metaDataConfiguration.addConfigEntry(WSApplicationMetaDataContext.VERSION, WSApplicationMetaDataContext.VERSION_71);
   } catch(Exception e) {
     //$JL-EXC$
     Category.SYS_SERVER.errorT(LOCATION, "metadata sub configuration and config entry 7.1 were not added to Database.");

   }

   Enumeration keys = migrationResult.keys();
   while (keys.hasMoreElements()) {
     moduleResult = (WSModuleMigrationResult)migrationResult.get(keys.nextElement());
     try {
       Configuration subConfiguration = metaDataConfiguration.createSubConfiguration(moduleResult.getModuleName()); 
       subConfiguration.addConfigEntry(ModuleRuntimeData.MODULE_DIR_NAME, new File(moduleResult.getModuleDir()).getName());      
     } catch (Exception e) {
       //$JL-EXC$
       Category.SYS_SERVER.errorT(LOCATION, "module_dir_name config entry was not added to Database.");       

     }          
   } 
   LOCATION.debugT("Directory for uploading to Database is " + wsDestinationDir);
   WSDeployProcess.uploadDirectory(new File(wsDestinationDir), webServicesContainerConfiguration);
   LOCATION.exiting("uploadConfiguration(Configuration applicationConfiguration, Hashtable migrationResult)");
  }
    
  private WebserviceDescriptionType[] getWebserviceDescription(WSDescriptor[] webServices, String sourceDir, Properties mappings) {   
    WebserviceDescriptionType[] wsDescription = new WebserviceDescriptionType[webServices.length];
    for (int i = 0; i < webServices.length; i++) {
      wsDescription[i] = new WebserviceDescriptionType(); 
      String serviceName = webServices[i].getWebserviceInternalName();
      wsDescription[i].setWebserviceName(serviceName);
      try {
       if (!IOUtil.isEmptyDir(IOUtil.getFilePath(sourceDir, wsDirsHandler.getWsdlRelDir(mappings)))) {
         WSConfigurationDescriptor[] configDir = webServices[i].getWsConfiguration();
         String bindingType = "";
         if (configDir != null && configDir.length > 0) {
           bindingType = getBindingType(webServices[i].getWsConfiguration()[0]); 
         }         
         wsDescription[i].setWsdlFile(loadWsdlFileType(IOUtil.getFilePath(sourceDir, wsDirsHandler.getWsdlRelDir(mappings)) + "/alone", serviceName + "/" + wsDirsHandler.getWsdlRelDir(mappings), bindingType, serviceName));
       }
      } catch (IOException e) {
        // $JL-EXC$
      }

      String typesDestinationPath = DESTINATION_CONFIG_DIR + "/types/" + webServices[i].getWebserviceInternalName();
      mkDir(typesDestinationPath);
      wsDescription[i].setTypesArchiveFile(loadTypesArchiveFile(wsDirsHandler.getJarsDir(sourceDir, mappings), typesDestinationPath,  wsDirsHandler.getJarFileName(mappings), webServices[i].getWebserviceInternalName()));    
      wsDescription[i].setTypeMappingFile(loadTypeMappingFile(wsDirsHandler.getDescriptorsDir(sourceDir, mappings), typesDestinationPath, webServices[i].getWebserviceInternalName()));
    }    
    return wsDescription;
  } 
  
  private WsdlFileType loadWsdlFileType(String sourceWsdlDir, String wsdlRelDir, String bindingType, String serviceName) {
    WsdlFileType wsdlFileType = new WsdlFileType();
    Vector wsdlTypes = new Vector();
    mkDir(getWsdlPath(DESTINATION_CONFIG_DIR, wsdlRelDir));
    loadWsdlType(sourceWsdlDir, wsdlTypes, getMetaInfPath(DESTINATION_CONFIG_DIR), wsdlRelDir, wsdlRelDir, bindingType, serviceName);   
    
    WsdlType[] wsTypeArr = new WsdlType[wsdlTypes.size()];
    wsdlTypes.copyInto(wsTypeArr);
    wsdlFileType.setWsdl(wsTypeArr);    
    return wsdlFileType;
  }
  
  private void loadWsdlType(String sourceDir, Vector wsdlTypes, String destDir, String wsdlRelDir, String wsdlConstant, String bindingType, String serviceName) {
    
    File[] files = new File(sourceDir).listFiles();
    for (int i = 0; i < files.length; i++) {      
      String wsdlExt = ".wsdl";
      String fileName = files[i].getName();
      if(files[i].isFile() && wsdlExt.equals(IOUtil.getFileExtension(fileName))) {    
        WsdlType wsdlType = new WsdlType();   
        String nameWithoutExt = IOUtil.getFileNameWithoutExt(fileName);
      
        try {
          if (!nameWithoutExt.endsWith("_sap")) IOUtil.copyFile(files[i].toString(), IOUtil.getFilePath(destDir, wsdlRelDir), files[i].getName());    
        } catch (IOException e) {
          // $JL-EXC$
        }    
      
        String endPATH = getWSDLSubDirectory(sourceDir);
        if (endPATH.indexOf(MigrationConstants.BINDINGS) != -1) {
          wsdlType.set_value(MigrationConstants.META_INF + "/" + serviceName + "/wsdl/" + IOUtil.getFilePath(endPATH, fileName));
          wsdlType.setType(new WsdlTypeType(WsdlTypeType._binding));
        } else if (endPATH.indexOf(MigrationConstants.PORTTYPES) != -1) {
          wsdlType.set_value(MigrationConstants.META_INF + "/" + serviceName + "/wsdl/" + IOUtil.getFilePath(endPATH, fileName));
          wsdlType.setType(new WsdlTypeType(WsdlTypeType._porttype));
        } 
      
        if (!nameWithoutExt.endsWith("_sap")) {
          if ( bindingType.equals(InterfaceMapping.MIMEBINDING) 
              || bindingType.equals(InterfaceMapping.HTTPGETBINDING) 
              || bindingType.equals(InterfaceMapping.HTTPPOSTBINDING)  ) {
             wsdlType.setStyle(new WsdlStyleType(WsdlStyleType._defaultTemp)); 
          } else if (bindingType.equals(InterfaceMapping.SOAPBINDING)) {
            if (nameWithoutExt.endsWith("_document")) {
              wsdlType.setStyle(new WsdlStyleType(WsdlStyleType._document));      
            } else if (nameWithoutExt.endsWith("_rpc")) {
              wsdlType.setStyle(new WsdlStyleType(WsdlStyleType._rpc)); 
            } else if (nameWithoutExt.endsWith("_rpc_enc")) {
              wsdlType.setStyle(new WsdlStyleType(WsdlStyleType._rpc_enc));  
            } 
          } 
        } 

      
        if (wsdlType.getStyle() != null) wsdlTypes.addElement(wsdlType);    
        } else if (files[i].isDirectory()) {
          String endPath = getWSDLSubDirectory(files[i].toString());
          if (endPath.indexOf(MigrationConstants.BINDINGS) != -1 || endPath.indexOf(MigrationConstants.PORTTYPES) != -1) {
            wsdlRelDir = serviceName + "/wsdl/" + endPath;
          }     
        loadWsdlType(sourceDir + WSBaseConstants.SEPARATOR + files[i].getName(), wsdlTypes, destDir, wsdlRelDir, wsdlConstant, bindingType, serviceName);
      }
    }
  }
  
  private TypeMappingFileType[] loadTypeMappingFile(String descriptorsDir, String destDir, String webserviceName) {
    File type_mappingFile = new File(IOUtil.getFilePath(descriptorsDir, WebServicesConstants.TYPE_MAPPING_DESCRIPTOR));
    try {
      IOUtil.copyFile(type_mappingFile.toString(), destDir, type_mappingFile.getName());
    } catch (IOException e) {
      //$JL-EXC$
      Category.SYS_SERVER.errorT(LOCATION, "Coping of file type-mapping.xml to directory " + destDir + " is unsuccessful.");     
    }
  
    TypeMappingFileType[] typeMappingFileType = new TypeMappingFileType[1];
    typeMappingFileType[0] = new TypeMappingFileType();
    typeMappingFileType[0].set_value(IOUtil.getFilePath("types/" + webserviceName, WebServicesConstants.TYPE_MAPPING_DESCRIPTOR));
    typeMappingFileType[0].setType(new SchemaTypeType(SchemaTypeType._framework));
    typeMappingFileType[0].setStyle(new SchemaStyleType(SchemaStyleType._defaultTemp));
    return typeMappingFileType;  
  }
  
  private TypeMappingFileType[] loadTypesArchiveFile(String jarsDir, String destDir, String jarsFileName, String webserviceName) { 
    try {
      IOUtil.copyFile(IOUtil.getFilePath(jarsDir, jarsFileName), destDir, jarsFileName);
    } catch (IOException e) {
      //$JL-EXC$
      Category.SYS_SERVER.errorT(LOCATION, "Coping of jar files to directory " + destDir + " is unsuccessful.");    

    }
  
    TypeMappingFileType[] typeMappingFileType = new TypeMappingFileType[1];
    typeMappingFileType[0] = new TypeMappingFileType();
    typeMappingFileType[0].set_value(IOUtil.getFilePath("types/" + webserviceName, jarsFileName));
    typeMappingFileType[0].setType(new SchemaTypeType(SchemaTypeType._framework));
    typeMappingFileType[0].setStyle(new SchemaStyleType(SchemaStyleType._defaultTemp));
    return typeMappingFileType; 
  }
    
  //generates path for wsd or vi files in webservice
  private String generatePath(NameDescriptor nameDescriptor, String extention) {
    String name = nameDescriptor.getName();
    String packageName = nameDescriptor.getPackage().trim();    
    if (!"".equals(packageName)) {
      packageName = packageName.replace('.', WSBaseConstants.SEPARATOR);
      return  packageName + WSBaseConstants.SEPARATOR + name;
    }
    return name;
  }
  
  private String getWSContainerDir(String rootDir) {    
    return rootDir + WSBaseConstants.SEPARATOR + WSBaseConstants.WS_CONTAINER_NAME;
  }
    
  private String getWebServicesDir(String rootDir) {
    return wsDirsHandler.getWebServicesDir(getWSContainerDir(rootDir));
  }
  
  private boolean hasMappings(String wsDirectory){
    return (new File(WSDirsHandler.getMappingsPath(wsDirectory)).exists());
  }
  
  private String getMetaInfPath(String wsDir) {
    return IOUtil.getFilePath(wsDir, MigrationConstants.META_INF);
  }

  private String getWsdlPath(String wsDir, String wsdlRelDir) {
    return IOUtil.getFilePath(getMetaInfPath(wsDir), wsdlRelDir);  
  } 

  private String getDestinationDir(String wsContainerDir, String configurationName) {
    return IOUtil.getFilePath(wsContainerDir, configurationName);
  }
  
  public void notifyForMigrationResult(CMigrationStatus[] cmStatus) {}
  
  private boolean mkDir(String dir) {
    File dirFile = new File(dir);
    return dirFile.mkdirs();
  }

  private String getWSDLSubDirectory(String sourceDir) {
    int indexBindings = sourceDir.indexOf(MigrationConstants.BINDINGS); 
    int indexPorttypes = sourceDir.indexOf(MigrationConstants.PORTTYPES); 
    if(indexBindings != -1) {
      return sourceDir.substring(indexBindings);
    } else if (indexPorttypes != -1) {
      return sourceDir.substring(indexPorttypes);
    } else {
      return "";
    }
  }  
  
  private String getBindingType(WSConfigurationDescriptor configDescriptor) {
    String bindingType = configDescriptor.getTransportBinding().getName();
    if (TransportBindingIDs.SOAPHTTP_TRANSPORTBINDING.equals(bindingType)) {
      bindingType = InterfaceMapping.SOAPBINDING; 
    } else if (TransportBindingIDs.MIME_TRANSPORTBINDING.equals(bindingType)) {
      bindingType = InterfaceMapping.MIMEBINDING; 
    } else if (TransportBindingIDs.HTTP_TRANSPORTBINDING.equals(bindingType)) {
      PropertyDescriptor[] propsDescriptor = configDescriptor.getTransportBinding().getProperty();
      if (propsDescriptor != null) {
        for (int k = 0; k < propsDescriptor.length; k++) {
          if ("method".equals(propsDescriptor[k].getName())) {
            if ("GET".equals(propsDescriptor[k].getValue())) {
              bindingType = InterfaceMapping.HTTPGETBINDING;
            } else if ("POST".equals(propsDescriptor[k].getValue())) {
              bindingType = InterfaceMapping.HTTPPOSTBINDING;
            }
          }
        }
      }
    }
    return bindingType;
  }
}
