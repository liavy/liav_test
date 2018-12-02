/*
 * Copyright (c) 2004-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieConfigType;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieType;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieTypeType;
import com.sap.engine.lib.descriptors.webj2eeengine.WebJ2EeEngineType;
import com.sap.engine.lib.descriptors5.javaee.XsdIntegerType;
import com.sap.engine.lib.descriptors5.web.SessionConfigType;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.services.deploy.container.ApplicationDeployInfo;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.deploy.ear.common.EqualUtils;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.admin.ITSAMWebModule;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.deploy.util.XmlUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.tc.logging.Location;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public final class RuntimeChangesAction extends ActionBase {

  private static final Location traceLocation = LogContext.getLocationDeploy();

  /**
   * @param iWebContainer
   * @param containerInfo
   * @param servletsAndJsp
   * @param runtimeInterface
   */
  public RuntimeChangesAction(IWebContainer iWebContainer, ContainerInfo containerInfo, WebContainerHelper servletsAndJsp,
                              WebContainerInterface runtimeInterface) {
    super(iWebContainer, containerInfo, servletsAndJsp, runtimeInterface);
  }//end of constructor

  /**
   * When some runtime changes from container side for an application are made, the container
   * calls the proper methods in DeployCommunicator to denote it needs to update its information
   * and this method is called from deploy service to all the other servers.
   *
   * @param applicationName
   * @param appConfig
   * @throws WarningException
   */
  public void notifyRuntimeChanges(String applicationName, Configuration appConfig) throws WarningException {
    String[] aliasesCanonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    for (int i = 0; aliasesCanonicalized != null && i < aliasesCanonicalized.length; i++) {
      try {
        ServiceContext.getServiceContext().getHttpProvider().clearCacheByAlias(aliasesCanonicalized[i]);      
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        throw new WebWarningException(WebWarningException.CANNOT_CLEAR_HTTP_CACHE,
          new Object[]{aliasesCanonicalized[i], e.toString()});
      }
    }
  }//end of notifyRuntimeChanges(String applicationName, Configuration appConfig)

  /**
   * @param applicationName
   * @return
   * @throws WarningException
   */
  public ApplicationDeployInfo commitRuntimeChanges(String applicationName) throws WarningException {
    ApplicationDeployInfo info = new ApplicationDeployInfo(true);
    info.setDeployedComponentNames(ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName));
    info.addFilesForClassloader(getFilesForClassPath(applicationName, ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName), false));
    return info;
  }//end of commitRuntimeChanges(String applicationName)

  /**
   * @param applicationName
   * @param webDeploymentDescriptor
   * @param lastModified
   * @param alias
   * @param overwrite               if overwrite is false and lastModified differs from the modificationFlag of the created subconfiguration 'admin'
   *                                changes will be rolled-back and exception will be thrown ;
   *                                if overwrite is true, the created subconfiguration 'admin' will have newly generated modificationFlag with the current time milliseconds
   * @throws RemoteException
   * @throws DeploymentException
   */
  public static Properties makeRuntimeChanges(String applicationName, WebDeploymentDescriptor webDeploymentDescriptor, String lastModified, String alias, boolean overwrite) throws RemoteException, DeploymentException {
    Properties changes = new Properties();
  	DeployCommunicator deployCommunicator = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator();
    try {
      //Get configurations
      Configuration appConfig = deployCommunicator.startRuntimeChanges(applicationName);
      Configuration servlet_jspConfig = ConfigurationUtils.getSubConfiguration(appConfig, Constants.CONTAINER_NAME, applicationName, true);
      Configuration servlet_jspAdmin = ConfigurationUtils.createSubConfiguration(servlet_jspConfig, Constants.ADMIN, applicationName, true);

      //Check modification flag
      checkModificationFlag(lastModified, servlet_jspAdmin, overwrite);

      //Parse the original web DDs
      String aliasDir = WebContainerHelper.getAliasDirName(ParseUtils.convertAlias(alias));
      String webInfDirectory = WebContainerHelper.getDirName(
        new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir.replace('/', File.separatorChar),
          "root", "WEB-INF"});
      String xmlFile = webInfDirectory + "web.xml";
      if (!(new File(xmlFile)).exists()) {
        throw new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
          new Object[]{alias});
      }

      String additonalXmlFile = webInfDirectory + "web-j2ee-engine.xml";
      if (!new File(additonalXmlFile).exists()) {
        additonalXmlFile = null;
      }

      Thread currentThread = Thread.currentThread();
      ClassLoader currentThreadLoader = currentThread.getContextClassLoader();
      currentThread.setContextClassLoader(ServiceContext.getServiceContext().getServiceLoader());
      WebDeploymentDescriptor oldWebDeploymentDescriptor = null;
      try {
        FileInputStream additonalXmlInputFile = null;
        if (additonalXmlFile != null && (new File(additonalXmlFile)).exists()) {
          additonalXmlInputFile = new FileInputStream(additonalXmlFile);
        }

        //TODO : Vily G : tuka zashto ne se wzema Object descriptor-a ot configuration-a, a se parse-wa pak?
        //TODO: Do not move this! parse descriptor from FS - avoid incorrect calculations of the isMaxSessionsConfigured and similar flags (local vs. previous NWA configs) 
         oldWebDeploymentDescriptor = XmlUtils.parseXml(new FileInputStream(xmlFile), additonalXmlInputFile, alias, xmlFile, additonalXmlFile, false);
      } catch (IOException io) {
        throw new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
          new Object[]{alias}, io);
      } finally {
        currentThread.setContextClassLoader(currentThreadLoader);
      }

      //Handle url session tracking value
      Boolean urlSessionTracking = webDeploymentDescriptor.getWebJ2EEEngine().getUrlSessionTracking();
      String urlSessionTrackingProperty = alias + ":" + Constants.URL_SESSION_TRACKING;
      ConfigurationUtils.addConfigEntry(servlet_jspAdmin, urlSessionTrackingProperty, urlSessionTracking.toString(), alias, true, true);
      String oldUrlSessionTracking = oldWebDeploymentDescriptor.getWebJ2EEEngine().getUrlSessionTracking().toString();
      if (!oldUrlSessionTracking.equals(urlSessionTracking.toString())) {
      	changes.put("url-session-tracking", oldUrlSessionTracking + "," + urlSessionTracking.toString());
      }
      oldWebDeploymentDescriptor.getWebJ2EEEngine().setUrlSessionTracking(urlSessionTracking);

      //Handle max sessions value
      Integer maxSessions = webDeploymentDescriptor.getWebJ2EEEngine().getMaxSessions();
      String maxSessionsProperty = alias + ":" + Constants.MAX_SESSIONS;
      ConfigurationUtils.addConfigEntry(servlet_jspAdmin, maxSessionsProperty, maxSessions.toString(), alias, true, true);
      String oldMaxSessions = oldWebDeploymentDescriptor.getWebJ2EEEngine().getMaxSessions().toString();
      if (!oldMaxSessions.equals(maxSessions.toString())) {
      	changes.put("max-sessions", oldMaxSessions + "," + maxSessions.toString());
      	oldWebDeploymentDescriptor.setMaxSessionsTagConfigured(true);   
      }
      oldWebDeploymentDescriptor.getWebJ2EEEngine().setMaxSessions(maxSessions); 

      applyCookieConfig(webDeploymentDescriptor, oldWebDeploymentDescriptor, servlet_jspAdmin, alias, changes);

      //Handle session timeout value
      if (webDeploymentDescriptor.getSessionConfig() != null && webDeploymentDescriptor.getSessionConfig().getSessionTimeout() != null) {
        XsdIntegerType sessionTimeout = webDeploymentDescriptor.getSessionConfig().getSessionTimeout();
        String sessionTimeoutProperty = alias + ":" + Constants.SESSION_TIMEOUT;
        ConfigurationUtils.addConfigEntry(servlet_jspAdmin, sessionTimeoutProperty, sessionTimeout.get_value().toString(), alias, true, true);

        if (oldWebDeploymentDescriptor.getSessionConfig() != null) {
        	XsdIntegerType oldSessionTimeout = oldWebDeploymentDescriptor.getSessionConfig().getSessionTimeout();
        	if (oldSessionTimeout != null && !(oldSessionTimeout.get_value().toString()).equals(sessionTimeout.get_value().toString())) {
        		changes.put("session-timeout", oldSessionTimeout.get_value().toString() + "," + sessionTimeout.get_value().toString());
        	} else if (oldSessionTimeout == null) {
        		changes.put("session-timeout", "null," + sessionTimeout.get_value().toString());
        	}
        	
          oldWebDeploymentDescriptor.getSessionConfig().setSessionTimeout(sessionTimeout);
        } else {
          SessionConfigType sessionConfigType = new SessionConfigType();
          sessionConfigType.setSessionTimeout(sessionTimeout);
          oldWebDeploymentDescriptor.setSessionConfig(sessionConfigType);
          
      		changes.put("session-timeout", "null," + sessionTimeout.get_value().toString());
        }
      } else {
      	String sessionTimeoutProperty = alias + ":" + Constants.SESSION_TIMEOUT;
      	ConfigurationUtils.addConfigEntry(servlet_jspAdmin, sessionTimeoutProperty, "", alias, true, true);
      	
      	if (oldWebDeploymentDescriptor.getSessionConfig() != null && oldWebDeploymentDescriptor.getSessionConfig().getSessionTimeout() != null) {
      		changes.put("session-timeout", oldWebDeploymentDescriptor.getSessionConfig().getSessionTimeout().get_value().toString() + ",null");
      	}
      	
        oldWebDeploymentDescriptor.setSessionConfig(null);
      }

      //merge web DD with the information from annotations before storing the object into the configuration
      //TODO : Vily G : tuka zashto ne se wzema Object descriptor-a ot configuration-a, a se parse-wa pak?
      if (oldWebDeploymentDescriptor.getWebAppVersion() != null && oldWebDeploymentDescriptor.getWebAppVersion().getValue().equals("2.5")) {
        mergeDescriptors(oldWebDeploymentDescriptor, servlet_jspConfig, alias, aliasDir);
      }

      //Store updated web.xml to the configuration and file system
      ByteArrayOutputStream webOS = new ByteArrayOutputStream();
      oldWebDeploymentDescriptor.writeStandartDescriptorToStream(webOS); //TODO: check whether validation should be ON or OFF; now is ON
      File webXmlFile = new File(xmlFile);
      FileUtils.writeToFile(new ByteArrayInputStream(webOS.toByteArray()), webXmlFile);
      ConfigurationUtils.addFileEntryByKey(servlet_jspAdmin, Constants.WEB_DD + aliasDir, webXmlFile, alias, true, true);
      
      //Store updated web-j2ee-engine.xml to the configuration and file system
      ByteArrayOutputStream addWebOS = new ByteArrayOutputStream();
      oldWebDeploymentDescriptor.writeAdditionalDescriptorToStream(addWebOS);

      if (additonalXmlFile == null) {
        additonalXmlFile = webInfDirectory + "web-j2ee-engine.xml";
      }
      File addWebXmlFile = new File(additonalXmlFile);
      FileUtils.writeToFile(new ByteArrayInputStream(addWebOS.toByteArray()), addWebXmlFile);
      ConfigurationUtils.addFileEntryByKey(servlet_jspAdmin, Constants.ADD_WEB_DD + aliasDir, addWebXmlFile, alias, true, true);
      
      //Store web model as a structure in the configuration
      storeWebDDObject2DBase(oldWebDeploymentDescriptor, servlet_jspConfig, aliasDir);

      //Store "url-session-tracking" value in the configuration
      ConfigurationUtils.addConfigEntry(servlet_jspConfig, Constants.URL_SESSION_TRACKING + aliasDir,
        oldWebDeploymentDescriptor.getWebJ2EEEngine().getUrlSessionTracking(), aliasDir, true, true);
      //Update "url-session-tracking" value in the AppMetaData object
      AppMetaData appMetaData = loadAppMetaDataObjectFromDBase(servlet_jspConfig, applicationName);
      if (appMetaData != null) {
        HashMap<String, Boolean> urlSessionTrackingPerModule = appMetaData.getUrlSessionTrackingPerModule();
        urlSessionTrackingPerModule.put(aliasDir, oldWebDeploymentDescriptor.getWebJ2EEEngine().getUrlSessionTracking());
        appMetaData.setUrlSessionTrackingPerModule(urlSessionTrackingPerModule);
        storeAppMetaDataObject2DBase(appMetaData, servlet_jspConfig, applicationName);
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      try {
				deployCommunicator.rollbackRuntimeChanges(applicationName);
			} catch (Exception rbe) {
				if (traceLocation.beWarning()) {
					//If rollback is not possible, the exception still has to be thrown:
					LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000242", 
						"Rollback of runtime changes failed.", rbe, null, null);
				}
			}
      throw new WebDeploymentException(WebDeploymentException.ROLLBACK_RUNTIME_CHANGES, new Object[]{applicationName}, e);
    }
    
    deployCommunicator.makeRuntimeChanges(applicationName, true);
    
    return changes;
  }//end of makeRuntimeChanges(String applicationName, WebDeploymentDescriptor webDeploymentDescriptor, WebModule webModule, CompositeData cd, boolean overwrite)  

  private static void checkModificationFlag(String modelFlag, Configuration servlet_jspAdmin, boolean overwrite) throws DeploymentException, ConfigurationException {
    if (!overwrite) {
      String dbFlag = null;
      if (servlet_jspAdmin.existsConfigEntry(Constants.MODIFICATION_FLAG)) {
        dbFlag = (String) servlet_jspAdmin.getConfigEntry(Constants.MODIFICATION_FLAG);
      } else {
        //dbFlag = WebModule.DEFAULT_MODIFICATION_FLAG; //removed since CIM
        dbFlag = ITSAMWebModule.DEFAULT_MODIFICATION_FLAG;
      }

      if (!dbFlag.equals(modelFlag)) {
        throw new WebDeploymentException(WebDeploymentException.MODEL_ALREADY_CHANGED, new Object[]{modelFlag, dbFlag});
      }
    }

    String newFlag = ServiceContext.getServiceContext().getServerId() + "_" + System.currentTimeMillis();
    ConfigurationUtils.addConfigEntry(servlet_jspAdmin, Constants.MODIFICATION_FLAG, newFlag, "", true, true);
  }//end of checkModificationFlag(WebModule webModule, Configuration servlet_jspAdmin, boolean overwrite)


  /**
   * Apply new values to cookie config and write them in the configuration.
   * Handle cookie config values - replace the old values with the new ones.
   * Note that cookies can be removed by setting them to an empty array or by setting them to null.
   * When removing cookies, empty string values are written in the configuration.
   */
  private static void applyCookieConfig(WebDeploymentDescriptor webDeploymentDescriptor, WebDeploymentDescriptor oldWebDeploymentDescriptor,
  		Configuration servlet_jspAdmin, String alias, Properties changes) throws DeploymentException {
    CookieConfigType cookieConfig = webDeploymentDescriptor.getWebJ2EEEngine().getCookieConfig();
    if (cookieConfig != null) {
      CookieType[] cookieTypes = cookieConfig.getCookie(); //new cookies to be set
      if (cookieTypes != null) { //update (or remove if length == 0) cookies
        WebJ2EeEngineType webJ2EeEngine = oldWebDeploymentDescriptor.getWebJ2EEEngine();
        if (webJ2EeEngine.getCookieConfig() != null) {
        	CookieType[] oldCookieTypes = webJ2EeEngine.getCookieConfig().getCookie();
        	if (oldCookieTypes != null && !EqualUtils.equalUnOrderedArrays(cookieTypes, oldCookieTypes)) {
       			String newACT = "null";
       			String newSCT = "null";
        		for (CookieType currentCT : cookieTypes) {
        			if (currentCT.getType().getValue().equals(CookieTypeType._APPLICATION)) {
        				newACT = currentCT.getPath() + ":" + currentCT.getDomain() + ":" + currentCT.getMaxAge().toString();
        			} else {
        				newSCT = currentCT.getPath() + ":" + currentCT.getDomain() + ":" + currentCT.getMaxAge().toString();
        			}
        		}
       			String oldACT = "null";
       			String oldSCT = "null";
						for (CookieType oldCT : oldCookieTypes) {
        			if (oldCT.getType().getValue().equals(CookieTypeType._APPLICATION)) {
        				oldACT = oldCT.getPath() + ":" + oldCT.getDomain() + ":" + oldCT.getMaxAge().toString();
        			} else {
        				oldSCT = oldCT.getPath() + ":" + oldCT.getDomain() + ":" + oldCT.getMaxAge().toString();
        			}
						}
						if (!oldACT.equals(newACT)) {
							changes.put("cookie-config-" + CookieTypeType._APPLICATION, oldACT + "," + newACT);
						}
						if (!oldSCT.equals(newSCT)) {
							changes.put("cookie-config-" + CookieTypeType._SESSION, oldSCT + "," + newSCT);
						}
					} else {
            for (CookieType currentCT : cookieTypes) {
            	changes.put("cookie-config-" + currentCT.getType().getValue(), "null," + currentCT.getPath() + ":" + 
              currentCT.getDomain() + ":" + currentCT.getMaxAge().toString());
            }        		
        	}
          webJ2EeEngine.getCookieConfig().setCookie(cookieTypes);
        } else {
          CookieConfigType newCookieCofigType = new CookieConfigType();
          newCookieCofigType.setCookie(cookieTypes);
          webJ2EeEngine.setCookieConfig(newCookieCofigType);
          for (CookieType currentCT : cookieTypes) {
          	changes.put("cookie-config-" + currentCT.getType().getValue(), "null," + currentCT.getPath() + ":" + 
            currentCT.getDomain() + ":" + currentCT.getMaxAge().toString());
          	if("APPLICAITON".equals(currentCT.getType())){
          		oldWebDeploymentDescriptor.setAppCookieConfigured(true);
          	}else{
          		oldWebDeploymentDescriptor.setSessCookieConfigured(true);
          	}
          }
        }
        //First remove the old ones and write them in the config as removed (empty values).        
        ConfigurationUtils.addConfigEntry(servlet_jspAdmin, alias + ":" + Constants.COOKIE_CONFIG + CookieTypeType._APPLICATION, "", alias, true, true);
        ConfigurationUtils.addConfigEntry(servlet_jspAdmin, alias + ":" + Constants.COOKIE_CONFIG + CookieTypeType._SESSION, "", alias, true, true);
        //Next write the new values if any
        for (CookieType currentCT : cookieTypes) {
        	String value = currentCT.getPath() + ":" + currentCT.getDomain() + ":" + currentCT.getMaxAge().toString();
          String cookieConfigProperty = alias + ":" + Constants.COOKIE_CONFIG + currentCT.getType().getValue();
          ConfigurationUtils.addConfigEntry(servlet_jspAdmin, cookieConfigProperty, value, alias, true, true);
        }
        oldWebDeploymentDescriptor.getWebJ2EEEngine().setCookieConfig(cookieConfig);
      }
    } else { //here also remove the cookies
    	ConfigurationUtils.addConfigEntry(servlet_jspAdmin, alias + ":" + Constants.COOKIE_CONFIG + CookieTypeType._APPLICATION, "", alias, true, true);
      ConfigurationUtils.addConfigEntry(servlet_jspAdmin, alias + ":" + Constants.COOKIE_CONFIG + CookieTypeType._SESSION, "", alias, true, true);
    	if (oldWebDeploymentDescriptor.getWebJ2EEEngine().getCookieConfig() != null && 
    			oldWebDeploymentDescriptor.getWebJ2EEEngine().getCookieConfig().getCookie() != null) {
    		CookieType[] cookieTypes = oldWebDeploymentDescriptor.getWebJ2EEEngine().getCookieConfig().getCookie();
        for (CookieType currentCT : cookieTypes) {
        	changes.put("cookie-config-" + currentCT.getType().getValue(), currentCT.getPath() + ":" + 
        	currentCT.getDomain() + ":" + currentCT.getMaxAge().toString() + ",null");
        }
    	}
      oldWebDeploymentDescriptor.getWebJ2EEEngine().setCookieConfig(null);
    }
  }//applyCookieConfig() method

}//end of class
