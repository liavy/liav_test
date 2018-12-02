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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.frame.core.configuration.addons.PropertySheet;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieConfigType;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieType;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieTypeType;
import com.sap.engine.lib.descriptors5.javaee.EjbLocalRefType;
import com.sap.engine.lib.descriptors5.javaee.EjbRefType;
import com.sap.engine.lib.descriptors5.javaee.EnvEntryType;
import com.sap.engine.lib.descriptors5.javaee.FullyQualifiedClassType;
import com.sap.engine.lib.descriptors5.javaee.LifecycleCallbackType;
import com.sap.engine.lib.descriptors5.javaee.MessageDestinationRefType;
import com.sap.engine.lib.descriptors5.javaee.PersistenceContextRefType;
import com.sap.engine.lib.descriptors5.javaee.PersistenceUnitRefType;
import com.sap.engine.lib.descriptors5.javaee.ResourceEnvRefType;
import com.sap.engine.lib.descriptors5.javaee.ResourceRefType;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleType;
import com.sap.engine.lib.descriptors5.javaee.ServiceRefType;
import com.sap.engine.lib.descriptors5.javaee.XsdIntegerType;
import com.sap.engine.lib.descriptors5.web.SessionConfigType;
import com.sap.engine.lib.descriptors5.web.WebAppType;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.lib.io.hash.FolderCompareResult;
import com.sap.engine.lib.io.hash.Index;
import com.sap.engine.lib.io.hash.PathNotFoundException;
import com.sap.engine.lib.jar.JarExtractor;
import com.sap.engine.lib.util.iterators.ArrayEnumeration;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.deploy.container.op.util.StartUp;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.interfaces.exceptions.HttpShmException;
import com.sap.engine.services.httpserver.interfaces.exceptions.IllegalHostArgumentException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.deploy.util.SecurityUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.XmlUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebCMigrationException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.engine.services.servlets_jsp.server.lib.ParserPropertiesFile;
import com.sap.engine.session.SessionDomain;
import com.sap.engine.system.ThreadWrapper;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.jtools.jlinee.web.model.AnnotationContainer;
import com.sap.tc.jtools.jlinee.web.model.WebAppMerger;
import com.sap.tc.logging.Location;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public abstract class ActionBase {
  private static final Location currentLocation = Location.getLocation(ActionBase.class);
  private static final Location traceLocation = LogContext.getLocationDeploy();

  protected ContainerInfo containerInfo = null;
  protected WebContainerHelper webContainerHelper = null;
  protected WebContainerInterface runtimeInterface = null;
  protected SecurityUtils securityUtils = null;
  protected IWebContainer iWebContainer = null;

  public static Hashtable<String, Properties> mapAliasResourceRef = new Hashtable<String, Properties>();
  /**
   * Mapping between alias and set of roles used in previous version (for update).
   * TODO check special case when updating and trying to deploy applications having the same alias
   */
  public static Hashtable<String, HashMap> mapAliasSecurityRes = new Hashtable<String, HashMap>();
  protected static HashMap<String, HashMap<String, Boolean>> urlSessionTrackingPerModulePerApplication =
  	new HashMap<String, HashMap<String, Boolean>>();

  /**
   * @param iWebContainer
   * @param containerInfo
   * @param webContainerHelper
   * @param runtimeInterface
   */
  public ActionBase(IWebContainer iWebContainer, ContainerInfo containerInfo, WebContainerHelper webContainerHelper,
                    WebContainerInterface runtimeInterface) {
    this.iWebContainer = iWebContainer;
    this.containerInfo = containerInfo;
    this.webContainerHelper = webContainerHelper;
    this.runtimeInterface = runtimeInterface;
    this.securityUtils = new SecurityUtils();
  }//end of constructor


  //_____Stopping application

  /**
   * @param servletContextFacade
   */
  public static void destroyWebAppComponents(ApplicationContext servletContextFacade) {
    String aliasCanonicalized = servletContextFacade.getAliasName();

    servletContextFacade.setDestroyingMode(true);

    if (servletContextFacade.getAllCurrentRequests() > 0) {
      final String tagName = "destroyWebAppComponents (" + servletContextFacade.getAliasName() + ")";
      try {
        if (Accounting.isEnabled()) {
          Accounting.beginMeasure(tagName, ActionBase.class);
        }

        synchronized (servletContextFacade.getSynchObject()) {
          long startTime = System.currentTimeMillis();
          long delta = 0;
          while (servletContextFacade.getAllCurrentRequests() > 0 && delta < ServiceContext.getServiceContext().getWebContainerProperties().getDestroyTimeout()) {
            try {
              servletContextFacade.getSynchObject().wait(ServiceContext.getServiceContext().getWebContainerProperties().getDestroyTimeout());
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000023",
                "Thread interrupted while waiting the destroy time out. Application may stop before all requests are processed." +
                  "Request processing on some requests may fail or synchronization errors may occur.", e, null, null);
            }
            delta = System.currentTimeMillis() - startTime;
          }
        }
      } finally {
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }
    }

    //context is no more capable of serving request
    servletContextFacade.setStarted(false);

    try {
      ApplicationThreadDestroyer destroyer = new ApplicationThreadDestroyer(servletContextFacade);
      boolean beDebug = traceLocation.beDebug();
      if (beDebug) {
      	traceLocation.debugT("ApplicationThreadDestroyer for web application [" + aliasCanonicalized + "] is initialized.");
      }

      ThreadWrapper.pushSubtask("Destroying web application [" + aliasCanonicalized + "]", ThreadWrapper.TS_PROCESSING);

      if (beDebug) {
        traceLocation.debugT(
          "ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().execute(destroyer) for web application [" + aliasCanonicalized + "].");
      }

      if (Accounting.isEnabled()) {
        Accounting.beginMeasure("Execute ApplicationThreadDestroyer", DeployCommunicator.class);
      }

      ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().execute(destroyer);
    } catch (InterruptedException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000054",
        "Cannot destroy web application [{0}].", new Object[]{aliasCanonicalized}, e, null, null);
    } finally {
      ThreadWrapper.popSubtask();
      if (Accounting.isEnabled()) {
        Accounting.endMeasure("Execute ApplicationThreadDestroyer");
      }
    }
  }//end of destroyWebAppComponents(ApplicationContext servletContextFacade)

  /**
   * @param warnings
   * @throws WarningException
   */
   protected void makeWarningException(Vector warnings) throws WarningException {
    if (warnings != null && warnings.size() != 0) {
      WarningException warningExc = new WarningException();
      for (int i = 0; i < warnings.size(); i++) {
        warningExc.addWarning(warnings.elementAt(i).toString());
      }
      throw warningExc;
    }
  }//end of makeWarningException(Vector warnings)

   //do the same as the previous method - makeWarningException(Vector warnings); the only difference is - ArrayList
   /**
   * @param warnings - an array of warnings. for each of its elements WarningException will be thrown
   * @throws WarningException
    */
   protected void makeWarningException(ArrayList<LocalizableTextFormatter> warnings) throws WarningException {
	    if (warnings != null && warnings.size() != 0) {
	      WarningException warningExc = new WarningException();
	      for(LocalizableTextFormatter warning: warnings){
	    	  warningExc.addWarning(warning.toString());
	      }
	      throw warningExc;
	    }
   }//end of makeWarningException(ArrayList<LocalizableTextFormatter> warnings)

  protected void checkAppAliasesInHttp() {
    Vector httpAliases = ServiceContext.getServiceContext().getHttpProvider().getAllApplicationAliases();

    //This vector contains all deployed applications, which have components for our container.
    String[] deployedApplications = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getMyApplications();

    for (int j = 0; deployedApplications != null && j < deployedApplications.length; j++) {
      String applicationName = deployedApplications[j];
      //get canonicalized aliases
      String[] deployedAliases = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
      for (int k = 0; deployedAliases != null && k < deployedAliases.length; k++) {
        String aliasCanonicalized = deployedAliases[k];
        if (!httpAliases.contains(aliasCanonicalized)) {
          registerAliasInHttp(applicationName, aliasCanonicalized, true);
        } else {
          httpAliases.remove(aliasCanonicalized);
        }
      }
    }

    for (int i = 0; i < httpAliases.size(); i++) {
      removeApplicationAlias((String) httpAliases.get(i));
    }
  }//end of checkAppAliasesInHttp()

  /**
   * @param applicationName    the application name
   * @param aliasCanonicalized canonicalized alias name
   * @param persistent if true updates the configuration, else - only runtime structures.
   */
  protected void registerAliasInHttp(String applicationName, String aliasCanonicalized, boolean persistent) {
    try {
      ServiceContext.getServiceContext().getHttpProvider().addApplicationAlias(aliasCanonicalized, persistent);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000055",
        "Cannot set alias for the application [{0}] in HTTP Provider service.", new Object[]{applicationName}, e, null, null);
    }

    webContainerHelper.removeStartedApplication(new MessageBytes(aliasCanonicalized.getBytes()), applicationName);
  }//end of registerAliasInHttp(String applicationName, String aliasCanonicalized)

  /**
   *
   * @param applicationName
   * @param aliasesCanonicalized all aliases of the given application in canonicalized format, i.e. replaced "\" and '//" with "/"
   * @param persistent
   */
  protected void registerAllAliasesInHttp(String applicationName, String[] aliasesCanonicalized, boolean persistent) {
    try {
      ServiceContext.getServiceContext().getHttpProvider().addAllApplicationAliases(applicationName, aliasesCanonicalized, persistent);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000056",
        "Cannot set the web aliases for the application [{0}] in HTTP Provider service.", new Object[]{applicationName}, e, null, null);
    }
    //There is no need to combine the following (CSN 2799735 2006), because each of them removes an item from ConcurrentReadHashMap:
    for (String currentAlias : aliasesCanonicalized) {
      webContainerHelper.removeStartedApplication(new MessageBytes(currentAlias.getBytes()), applicationName);
    }
  }//end of registerAllAliasesInHttp(String applicationName, String[] aliasesCanonicalized, boolean persistent)

  /**
   * Checks if an alias is registered in http (in the hosts only) and remove it if it is registered.
   *
   * @param applicationName
   * @param aliasCanonicalized
   */
  protected void checkAliasInHttp(String applicationName, String aliasCanonicalized) {
    try {
      ServiceContext.getServiceContext().getHttpProvider().checkApplicationAlias(aliasCanonicalized);
    } catch (IllegalHostArgumentException er) {
      //$JL-EXC$
      if (traceLocation.bePath()) {
      	traceLocation.pathT(
          "Alias for the application [" + applicationName + "] already exists in HTTP Provider service." + er.toString());
      }
      // VillyU
      if (aliasCanonicalized.equalsIgnoreCase("/") && applicationName.equalsIgnoreCase(WebContainer.sapDefaultApplName)) {
        //TODO check why this is needed: what will happen if all application aliases has to be removed;
        // check what will happen if an exception is thrown when sapDefault is deploying
        // do not remove the application alias
        return;
      }
      removeApplicationAlias(aliasCanonicalized);
    }
  }//end of checkAliasInHttp(String applicationName, String aliasCanonicalized)

  /**
   * @param aliasCanonicalized
   */
  private void removeApplicationAlias(String aliasCanonicalized) {
    try {
      Vector<WebModule> deployedWebModules = ((WebContainerProvider) iWebContainer).getDeployedAppls(aliasCanonicalized);
      if (deployedWebModules != null && deployedWebModules.size() == 2) {
        // there are two modules for that alias; do not remove the alias from http
        return;
      }
      ServiceContext.getServiceContext().getHttpProvider().removeApplicationAlias(aliasCanonicalized, true);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000057",
        "Cannot remove application alias. Possible reason: cannot access database.", e, null, null);
    }
  }//end of removeApplicationAlias(String aliasCanonicalized)


  //_____DataBase_____

  /**
   * @param alias
   * @param applicationName
   * @param config
   * @param oldWebDeploymentDescriptor
   * @throws Exception
   */
  protected void store2DBase(String alias, String applicationName, Configuration config, WebDeploymentDescriptor oldWebDeploymentDescriptor) throws Exception {
    String aliasDir = WebContainerHelper.getAliasDirName(ParseUtils.convertAlias(alias));

    boolean hasSubstitutionVariables = oldWebDeploymentDescriptor.hasSubstitutionVariables();
    if (!hasSubstitutionVariables) {
      //Handle url session tracking value
      String urlSessionTrackingProperty = alias + ":" + Constants.URL_SESSION_TRACKING;
      if (config.existsConfigEntry(urlSessionTrackingProperty)) {
        String urlSessionTracking = (String) ConfigurationUtils.getConfigEntry(config, urlSessionTrackingProperty, applicationName);
        oldWebDeploymentDescriptor.getWebJ2EEEngine().setUrlSessionTracking(new Boolean(urlSessionTracking));
      }

      //Handle max sessions value
      String maxSessionsProperty = alias + ":" + Constants.MAX_SESSIONS;
      if (config.existsConfigEntry(maxSessionsProperty)) {
        String maxSessions = (String) ConfigurationUtils.getConfigEntry(config, maxSessionsProperty, applicationName);
        oldWebDeploymentDescriptor.getWebJ2EEEngine().setMaxSessions(new Integer(maxSessions));
      }

      //Handle cookie config values
      String cookieConfigProperty = alias + ":" + Constants.COOKIE_CONFIG + CookieTypeType._APPLICATION;
      if (config.existsConfigEntry(cookieConfigProperty)) {
        String value = (String) ConfigurationUtils.getConfigEntry(config, cookieConfigProperty, applicationName);
        setCookies(oldWebDeploymentDescriptor, value, CookieTypeType._APPLICATION);
      }
      cookieConfigProperty = alias + ":" + Constants.COOKIE_CONFIG + CookieTypeType._SESSION;
      if (config.existsConfigEntry(cookieConfigProperty)) {
        String value = (String) ConfigurationUtils.getConfigEntry(config, cookieConfigProperty, applicationName);
        setCookies(oldWebDeploymentDescriptor, value, CookieTypeType._SESSION);
      }

      //Handle session timeout value
      String sessionTimeoutProperty = alias + ":" + Constants.SESSION_TIMEOUT;
      if (config.existsConfigEntry(sessionTimeoutProperty)) {
        String value = (String) ConfigurationUtils.getConfigEntry(config, sessionTimeoutProperty, applicationName);
        XsdIntegerType sessionTimeout = new XsdIntegerType();
        if ("".equals(value)) {
        	//if value is empty string this means the property is removed from local web.xml and should remain removed
					sessionTimeout = null;
					oldWebDeploymentDescriptor.setSessionConfig(null);
        } else {
        	sessionTimeout.set_value(new BigInteger(value));
        	if (oldWebDeploymentDescriptor.getSessionConfig() != null) {
            oldWebDeploymentDescriptor.getSessionConfig().setSessionTimeout(sessionTimeout);
          } else {
            SessionConfigType sessionConfigType = new SessionConfigType();
            sessionConfigType.setSessionTimeout(sessionTimeout);
            oldWebDeploymentDescriptor.setSessionConfig(sessionConfigType);
          }
        }
      }

      String webInfDirectory = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName),
        aliasDir, "root", "WEB-INF"});

      String webXmlFileName = webInfDirectory + "web.xml";
      if (!(new File(webXmlFileName)).exists()) {
        throw new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
          new Object[]{alias});
      }

      String webXmlFileName2 = webInfDirectory + "web-j2ee-engine.xml";

      //Store updated web.xml to the configuration and file system
      ByteArrayOutputStream webOS = new ByteArrayOutputStream();
      oldWebDeploymentDescriptor.writeStandartDescriptorToStream(webOS);
      File webXmlFile = new File(webXmlFileName);
      FileUtils.writeToFile(new ByteArrayInputStream(webOS.toByteArray()), webXmlFile);
      ConfigurationUtils.addFileEntryByKey(config, Constants.WEB_DD + aliasDir, webXmlFile, alias, true, true);

      //Store updated web-j2ee-engine.xml to the configuration and file system
      ByteArrayOutputStream addWebOS = new ByteArrayOutputStream();
      oldWebDeploymentDescriptor.writeAdditionalDescriptorToStream(addWebOS);
      File addWebXmlFile = new File(webXmlFileName2);
      FileUtils.writeToFile(new ByteArrayInputStream(addWebOS.toByteArray()), addWebXmlFile);
      ConfigurationUtils.addFileEntryByKey(config, Constants.ADD_WEB_DD + aliasDir, addWebXmlFile, alias, true, true);
    } else {
      deleteCustomSettings(alias, applicationName, config, aliasDir);
    }
  }//end of store2DBase(String alias, String applicationName, Configuration config, WebDeploymentDescriptor oldWebDeploymentDescriptor)

  /**
   * @param alias
   * @param applicationName
   * @param config
   * @param aliasDir
   * @throws ConfigurationException
   */
  protected void deleteCustomSettings(String alias, String applicationName, Configuration config, String aliasDir) throws ConfigurationException {
    String urlSessionTrackingProperty = alias + ":" + Constants.URL_SESSION_TRACKING;
    if (config.existsConfigEntry(urlSessionTrackingProperty)) {
      ConfigurationUtils.deleteConfigEntry(config, urlSessionTrackingProperty, applicationName);
    }

    String maxSessionsProperty = alias + ":" + Constants.MAX_SESSIONS;
    if (config.existsConfigEntry(maxSessionsProperty)) {
      ConfigurationUtils.deleteConfigEntry(config, maxSessionsProperty, applicationName);
    }

    String cookieConfigProperty = alias + ":" + Constants.COOKIE_CONFIG + CookieTypeType._APPLICATION;
    if (config.existsConfigEntry(cookieConfigProperty)) {
      ConfigurationUtils.deleteConfigEntry(config, cookieConfigProperty, applicationName);
    }
    cookieConfigProperty = alias + ":" + Constants.COOKIE_CONFIG + CookieTypeType._SESSION;
    if (config.existsConfigEntry(cookieConfigProperty)) {
      ConfigurationUtils.deleteConfigEntry(config, cookieConfigProperty, applicationName);
    }

    String sessionTimeoutProperty = alias + ":" + Constants.SESSION_TIMEOUT;
    if (config.existsConfigEntry(sessionTimeoutProperty)) {
      ConfigurationUtils.deleteConfigEntry(config, sessionTimeoutProperty, applicationName);
    }

    if (config.existsConfigEntry(Constants.WEB_DD + aliasDir)) {
      ConfigurationUtils.deleteConfigEntry(config, Constants.WEB_DD + config, applicationName);
    }

    if (config.existsConfigEntry(Constants.ADD_WEB_DD + aliasDir)) {
      ConfigurationUtils.deleteConfigEntry(config, Constants.ADD_WEB_DD + aliasDir, applicationName);
    }
  }//end of deleteCustomSettings(String alias, String applicationName, Configuration config, String aliasDir)

  /**
   * @param webDeploymentDescriptor
   * @param value
   * @param type
   */
  private void setCookies(WebDeploymentDescriptor webDeploymentDescriptor, String value, String type) {
    int firstIndex = value.indexOf(":");
    int lastIndex = value.lastIndexOf(":");
    if (firstIndex < 0) {
    	//The value is empty, i.e. the cookie has been removed from the administration
    	webDeploymentDescriptor.getWebJ2EEEngine().setCookieConfig(new CookieConfigType());
    } else {
	    String path = value.substring(0, firstIndex);
	    String domain = value.substring(firstIndex + 1, lastIndex);
	    String maxAge = value.substring(lastIndex + 1);

	    if (webDeploymentDescriptor.getWebJ2EEEngine().getCookieConfig() != null) {
	      CookieType[] oldCookieTypes = webDeploymentDescriptor.getWebJ2EEEngine().getCookieConfig().getCookie();
	      if (oldCookieTypes != null) {
	        boolean found = false;
	        for (int j = 0; j < oldCookieTypes.length; j++) {
	          if (oldCookieTypes[j].getType() != null) {
	            if (oldCookieTypes[j].getType().getValue().equals(type)) {
	              oldCookieTypes[j].setPath(path);
	              oldCookieTypes[j].setDomain(domain);
	              oldCookieTypes[j].setMaxAge(new Integer(maxAge));
	              found = true;
	              break;
	            }
	          } else {
	            // cookie type is null
	            // I think that this case is impossible
	          }
	        }

	        if (!found) {
	          CookieType cookieType = new CookieType();
	          cookieType.setType(new CookieTypeType(type));
	          cookieType.setPath(path);
	          cookieType.setDomain(domain);
	          cookieType.setMaxAge(new Integer(maxAge));
	          Vector<CookieType> all = new Vector<CookieType>(Arrays.asList(oldCookieTypes));
	          all.add(cookieType);
	          webDeploymentDescriptor.getWebJ2EEEngine().getCookieConfig().setCookie((CookieType[]) all.toArray(new CookieType[0]));
	        }
	      }
	    } else {
	      CookieType cookieType = new CookieType();
	      cookieType.setType(new CookieTypeType(type));
	      cookieType.setPath(path);
	      cookieType.setDomain(domain);
	      cookieType.setMaxAge(new Integer(maxAge));
	      CookieConfigType cookieCofigType = new CookieConfigType();
	      cookieCofigType.setCookie(new CookieType[]{cookieType});
	      webDeploymentDescriptor.getWebJ2EEEngine().setCookieConfig(cookieCofigType);
	    }
    }
  }//end of setCookies(WebDeploymentDescriptor webDeploymentDescriptor, String value, String type)

  private Index getContainerIndex(Index index, String name) {
    try {
      return index.getFolder(name);
    } catch (PathNotFoundException e) {
      // this method is used during update but it is possible
      // that the specific container has not participated in the
      // deployment so far
      // and it has no indexed subconfiguration
      return null;
    }
  }//end of getContainerIndex(Index index, String name)

  private Index getIndexDB(Configuration config) {
    try {
      final int type = config.getConfigurationType();
      if ((type & Configuration.CONFIG_TYPE_INDEXED) != 0) {
        return config.getIndex();
      } else {
        // it is not indexed, because this application is
        // deployed before AS Java to be updated to 7.11
        return null;
      }
    } catch (ConfigurationException e) {
      throw new IllegalStateException("Cannot get configuration index from [" + config.getPath() + "].", e);
    }
  }//end of getIndexDB(Configuration config)

  /**
   *
   * @param config
   * @param downloadAppFilesInfo
   * @param caller
   * @throws IOException
   * @throws ConfigurationException
   * @throws DeploymentException
   */
  private void loadFromDBase(Configuration config, DownloadAppFilesInfo downloadAppFilesInfo, String caller) throws IOException, ConfigurationException, DeploymentException {
    String applicationName = downloadAppFilesInfo.getApplicationName();

    String tagName = null;

    if (Accounting.isEnabled()) { //ACCOUNTING.start - BEGIN
      tagName = caller + "/getAliases";
      Accounting.beginMeasure(tagName, DeployCommunicator.class);
    }//ACCOUNTING.start - END
    String[] allAliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
    if (Accounting.isEnabled()) { //ACCOUNTING.end - BEGIN
      Accounting.endMeasure(tagName);
    } //ACCOUNTING.end - END

    if (allAliases == null) {
    	LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000058",
    		"Cannot get alias names for [{0}] application in order to download application files.", new Object[]{applicationName}, null, null);
      return;
    }

    String aliasCanonicalized = null;
    String aliasDir = null;
    String rootDir = null;
    String webInfDir = null;
    for (int i = 0; i < allAliases.length; i++) {
      downloadAppFilesInfo.setAllAliases(allAliases);

      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        tagName = caller + "/initDirNames (" + allAliases[i] + ")";
        Accounting.beginMeasure(tagName, ActionBase.class);
      }//ACCOUNTING.start - END

      aliasCanonicalized = ParseUtils.convertAlias(allAliases[i]);
      aliasDir = WebContainerHelper.getAliasDirName(aliasCanonicalized);
      rootDir = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir});
      webInfDir = rootDir + "root" + File.separator + "WEB-INF" + File.separator;

      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(tagName);
        tagName = caller + "/downloadWarFromDB(" + aliasCanonicalized + ")";
        Accounting.beginMeasure(tagName, ActionBase.class);
      }//ACCOUNTING.start - END

      downloadWarFromDB(downloadAppFilesInfo, allAliases[i], rootDir, config, tagName);

      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(tagName);
        tagName = caller + "/downloadDDFromDB(" + aliasCanonicalized + ")";
        Accounting.beginMeasure(tagName, ActionBase.class);
      }//ACCOUNTING.start - END

      downloadDDFromDB(downloadAppFilesInfo, aliasDir, webInfDir, config, tagName);

      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(tagName);
        tagName = caller + "/downloadAddDDFromDB(" + aliasCanonicalized + ")";
        Accounting.beginMeasure(tagName, ActionBase.class);
      }//ACCOUNTING.start - END

      downloadAddDDFromDB(downloadAppFilesInfo, aliasDir, webInfDir, config, tagName);

      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(tagName);
        tagName = caller + "/downloadMergedDDFromDB(" + aliasCanonicalized + ")";
        Accounting.beginMeasure(tagName, ActionBase.class);
      }//ACCOUNTING.start - END

      downloadMergedDDFromDB(downloadAppFilesInfo, aliasDir, webInfDir, config, tagName);

      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(tagName);
      }//ACCOUNTING.end - END
    }
  }//end of loadFromDBase(String applicationName, Configuration config)

  private void downloadWarFromDB(DownloadAppFilesInfo downloadAppFilesInfo, String alias, String rootDir, Configuration config, String caller) throws IOException, ConfigurationException, DeploymentException {
	boolean beDebug = traceLocation.beDebug();
    String applicationName = downloadAppFilesInfo.getApplicationName();

    String tagName = null;

    // Load war file from DB
    boolean download = false;
    String warUri = null;
    String warName = null;

    if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
      tagName = caller + "/getWarUri";
      Accounting.beginMeasure(tagName, ActionBase.class);
    }//ACCOUNTING.start - END

    warUri = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getWarName(alias, applicationName);
    warUri = warUri.replace('\\', '/');
    warName = warUri.substring(warUri.lastIndexOf('/') + 1);
    warUri = warUri.replace('/', '_');

    if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
      Accounting.endMeasure(tagName);
    }//ACCOUNTING.end - END

    //Check whether downloading is needed
    if (downloadAppFilesInfo.isDownloadAll()) {
      download = true;
    } else if (downloadAppFilesInfo.isDownloadPartially()) {
      ArrayEnumeration filesForDownload = downloadAppFilesInfo.getFolderCompareResult().getFilesForDownload();
      while (filesForDownload.hasNext()) {
        String modifiedFile = (String) filesForDownload.next();
        if (beDebug){
        	traceLocation.debugT("ActionBase.downloadWarFromDB; application["+applicationName+"]. The modifiedFile["+modifiedFile+"] will be compared with warUri ["+File.separator +warUri+"].");
        }
        if ((File.separator + warUri).equals(modifiedFile)) {
          download = true;
          break;
        }
      }
    }

    if (beDebug){
    	traceLocation.debugT("ActionBase.downloadWarFromDB; application["+applicationName+"]. download flag is ["+download+"].");
    }

    if (download) {
      FileInputStream hddFileInputStream = null;
      File hddFile = new File(rootDir + warName);
      try {
        if (Accounting.isEnabled()) {
          tagName = caller + "/checkHddFile (" + hddFile.getName() + ")";
          Accounting.beginMeasure(tagName, ActionBase.class);
        }//ACCOUNTING.start - END
        hddFileInputStream = new FileInputStream(hddFile);
      } catch (FileNotFoundException fnfe) {
        hddFileInputStream = null;
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      if (hddFileInputStream != null) {
        try {
          hddFileInputStream.close();
        } catch (Exception e) {
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000705",
              "Cannot close file [{0}]. This may lead to file handlers leak.", new Object[] {hddFile.getName()}, e, null, null);
          }
        }

        if (!hddFile.delete()) {
          throw new WebIOException(WebIOException.CANNOT_DELETE_FILE, new Object[]{hddFile, applicationName});
        }
      }

      try {//ACCOUNTING.start - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/download";
          Accounting.beginMeasure(tagName, ActionBase.class);
        }//ACCOUNTING.start - END
        downloadWriteFiles(applicationName, alias, rootDir, config, warUri, warName, tagName);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END
    }
  } //end of downloadWarFromDB(DownloadAppFilesInfo downloadAppFilesInfo, String alias, String rootDir, Configuration config, String caller)

  private void downloadWriteFiles(String applicationName, String alias,
      String rootDir, Configuration config, String warUri, String warName, String caller)
      throws ConfigurationException, IOException, DeploymentException {

     boolean beDebug = traceLocation.beDebug();
     String tagName = null;

    File f = null;
    try {//ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) {
        tagName = caller + "/createRootDir";
        Accounting.beginMeasure(tagName, ActionBase.class);
      }//ACCOUNTING.start - END
      f = (new File(rootDir));
      if (!f.exists()) {
        if (!f.mkdirs()) {
          if (!f.mkdirs()) {
            throw new WebIOException(WebIOException.CANNOT_MAKE_DIRS, new Object[]{f.getAbsolutePath()});
          }
        }
      }
      if(beDebug){
    	  traceLocation.debugT("ActionBase.downloadWriteFiles; application[{0}]. file ["+rootDir+"] exists; Its lastModified time = ["+f.lastModified()+"].");
      }

    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(tagName);
      }
    }//ACCOUNTING.end - END

    String tempWarName = null;
    File tempHddFile = null;
    InputStream warUriStream = null;
    boolean beWarning = traceLocation.beWarning();

    try {//ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) {
        tagName = caller + "/config.getFile(" + warUri + ")";
        Accounting.beginMeasure(tagName, Configuration.class);
      }//ACCOUNTING.start - END
      tempWarName = "temp_" + warName;
      tempHddFile = new File(rootDir + tempWarName);
      if (tempHddFile.exists()) {
        if (beWarning) {
        	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000497",
        	  "WAR file [{0}] exists on the file system.", new Object[]{tempHddFile.getName()}, null, null);
        }
      }
      warUriStream = config.getFile(warUri);
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(tagName);
      }
    }//ACCOUNTING.end - END

    try {//ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) {
        tagName = caller + "/writeToFile";
        Accounting.beginMeasure(tagName, FileUtils.class);
      }//ACCOUNTING.start - END
      FileUtils.writeToFile(warUriStream, tempHddFile);
      if(beDebug){
              traceLocation.debugT("ActionBase.downldownloadWriteFiles application ["+applicationName+"]; writeToFile completed.");
      }
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(tagName);
      }
    }//ACCOUNTING.end - END

    if (traceLocation.beDebug()) {
    	traceLocation.debugT("WAR file for [" + alias + "] web application is copied to the file system.");
    }
    if (!tempHddFile.canWrite()) {
      if (beWarning) {
    	  LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000498",
    			"File.canWrite() returns false for file [{0}] after FileUtils.copyFile() invocation.", new Object[]{tempHddFile.getName()},	null, null);
     }
    }
    extractWar(applicationName, rootDir, tempHddFile, caller);

    try {//ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) {
        tagName = caller + "/renameWar";
        Accounting.beginMeasure(tagName, ActionBase.class);
      }//ACCOUNTING.start - END
      if (traceLocation.beDebug()) {
      	traceLocation.debugT("WAR file for [" + alias + "] web application is extracted to the file system.");
      }
      if (!tempHddFile.canWrite()) {
         if (beWarning) {
        	 LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000499",
        	   "File.canWrite() returns false for file [{0}] after JarExtractor.extractJar() invocation.", new Object[]{tempHddFile.getName()}, null, null);
         }
      }
      File warFile = new File(rootDir + warName);
      if (warFile.exists()) {
      	if (traceLocation.beInfo()) {
      		traceLocation.infoT("WAR file [" + warFile.getName()
      			+ "] exists on the file system. Web Container service will try to delete it.", applicationName);
      	}
      	if (!warFile.delete()) {
        	 if (beWarning) {
        		 LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000437",
        		   "Cannot delete [{0}] file.", new Object[]{warFile.getName()}, null, null);
        	 }
        }
      }
      if (!tempHddFile.renameTo(warFile)) {
        if (!tempHddFile.canWrite()) {
        	if (beWarning) {
        		LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000501",
      					"File.canWrite() returns false for file [{0}] after JarExtractor.extractJar() invocation.", new Object[]{tempHddFile.getName()}, null, null);
        	}
        }
        if (!tempHddFile.renameTo(warFile)) {
            throw new WebIOException(WebIOException.CANNOT_RENAME_WAR_FILE, new Object[]{tempWarName, applicationName});
        }
      }
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(tagName);
      }
    }//ACCOUNTING.end - END
  }//end of downloadWriteFiles(String applicationName, String alias,
  //String rootDir, Configuration config, String warUri, String warName, String caller)

  private void downloadDDFromDB(DownloadAppFilesInfo downloadAppFilesInfo, String aliasDir, String webInfDir, Configuration config, String caller) throws IOException, ConfigurationException {
    String tagName = null;

    boolean download = false;
    //Check whether downloading is needed
    if (downloadAppFilesInfo.isDownloadAll()) {
      download = true;
    } else if (downloadAppFilesInfo.isDownloadPartially()) {
      ArrayEnumeration filesForDownload = downloadAppFilesInfo.getFolderCompareResult().getFilesForDownload();
      while (filesForDownload.hasNext()) {
        String modifiedFile = (String) filesForDownload.next();
        if (traceLocation.beDebug()){
        	traceLocation.debugT("ActionBase.downloadDDFromDB; aliasDir["+aliasDir+"]; modifiedFile ["+modifiedFile+"] will be compared with ["+
        			File.separator + "admin" +File.separator + Constants.WEB_DD + aliasDir+"].");
        }
       if (config.getPath().endsWith("admin")) {
          if ((File.separator + "admin" +File.separator + Constants.WEB_DD + aliasDir).equals(modifiedFile)) {
            download = true;
            break;
          }
        } else {
          if ((File.separator + Constants.WEB_DD + aliasDir).equals(modifiedFile)) {
            download = true;
            break;
          }
        }
      }
    }

    if (traceLocation.beDebug()){
    	traceLocation.debugT("ActionBase.downloadDDFromDB; aliasDir["+aliasDir+"]; download flag = ["+download+"].");
    }
    if (download) {
      File webDD = new File(webInfDir + "web.xml");
      FileInputStream webDDFileInputStream = null;
      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          tagName = caller + "/checkWebDDFile (" + webDD.getName() + ")";
          Accounting.beginMeasure(tagName, ActionBase.class);
        }//ACCOUNTING.start - END
        webDDFileInputStream = new FileInputStream(webDD);
      } catch (FileNotFoundException fnfe) {
        webDDFileInputStream = null;
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      if (webDDFileInputStream != null) {
        try {
          webDDFileInputStream.close();
        } catch(Exception e) {
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000706",
              "Cannot close file [{0}]. This may lead to file handlers leak.", new Object[] {webDD.getName()}, e, null, null);
          }
        }

        if (!webDD.delete()) {
          throw new WebIOException(WebIOException.CANNOT_DELETE_FILE, new Object[]{"web.xml", downloadAppFilesInfo.getApplicationName()});
        }
      }

      InputStream webDDStream = null;
      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/config.getFile(" + Constants.WEB_DD + aliasDir + ")";
          Accounting.beginMeasure(tagName, Configuration.class);
        }//ACCOUNTING.start - END
        webDDStream = config.getFile(Constants.WEB_DD + aliasDir);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/writeToFile";
          Accounting.beginMeasure(tagName, FileUtils.class);
        }//ACCOUNTING.start - END
        FileUtils.writeToFile(webDDStream, webDD);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END
    }
  }//downloadDDFromDB(DownloadAppFilesInfo downloadAppFilesInfo, String aliasDir, String webInfDir, Configuration config, String caller)

  private void downloadAddDDFromDB(DownloadAppFilesInfo downloadAppFilesInfo, String aliasDir, String webInfDir, Configuration config, String caller) throws IOException, ConfigurationException {
    String tagName = null;

    boolean download = false;
    //Check whether downloading is needed
    if (downloadAppFilesInfo.isDownloadAll()) {
      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/existsConfigEntry (" + Constants.ADD_WEB_DD + aliasDir + ")";
          Accounting.beginMeasure(tagName, Configuration.class);
        }//ACCOUNTING.start - END
        if (config.existsConfigEntry(Constants.ADD_WEB_DD + aliasDir)) {
          download = true;
        }
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END
    } else if (downloadAppFilesInfo.isDownloadPartially()) {
      ArrayEnumeration filesForDownload = downloadAppFilesInfo.getFolderCompareResult().getFilesForDownload();
      while (filesForDownload.hasNext()) {
        String modifiedFile = (String) filesForDownload.next();
        if (config.getPath().endsWith("admin")) {
          if (traceLocation.beDebug()){
             	traceLocation.debugT("ActionBase.downloadAddDDFromDB; aliasDir["+aliasDir+"]; modifiedFile ["+modifiedFile+"] will be compared with ["+File.separator +
             			"admin" + File.separator + Constants.ADD_WEB_DD + aliasDir+"].");
          }
          if ((File.separator + "admin" + File.separator + Constants.ADD_WEB_DD + aliasDir).equals(modifiedFile)) {
            download = true;
            break;
          }
        } else {
          if ((File.separator + Constants.ADD_WEB_DD + aliasDir).equals(modifiedFile)) {
            download = true;
            break;
          }
        }
      }
    }

    if (traceLocation.beDebug()){
    	traceLocation.debugT("ActionBase.downloadAddDDFromDB; aliasDir["+aliasDir+"]; download flag = ["+download+"].");
	}

    if (download) {
      File addWebDD = new File(webInfDir + "web-j2ee-engine.xml");
      FileInputStream addWebDDFileInputStream = null;
      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          tagName = caller + "/checkAddWebDDFile (" + addWebDD.getName() + ")";
          Accounting.beginMeasure(tagName, ActionBase.class);
        }//ACCOUNTING.start - END
        addWebDDFileInputStream = new FileInputStream(addWebDD);
      } catch (FileNotFoundException fnfe) {
        addWebDDFileInputStream = null;
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      if (addWebDDFileInputStream != null) {
        try {
          addWebDDFileInputStream.close();
        } catch(Exception e) {
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000088",
              "Cannot close file [{0}]. This may lead to file handlers leak.", new Object[] {addWebDD.getName()}, e, null, null);
          }
        }

        if (!addWebDD.delete()) {
          throw new WebIOException(WebIOException.CANNOT_DELETE_FILE, new Object[]{"web-j2ee-engine.xml", downloadAppFilesInfo.getApplicationName()});
        }
      }

      InputStream addWebDDStream = null;
      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/config.getFile(" + Constants.ADD_WEB_DD + aliasDir + ")";
          Accounting.beginMeasure(tagName, Configuration.class);
        }//ACCOUNTING.start - END
        addWebDDStream = config.getFile(Constants.ADD_WEB_DD + aliasDir);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/writeToFile";
          Accounting.beginMeasure(tagName, FileUtils.class);
        }//ACCOUNTING.start - END
        FileUtils.writeToFile(addWebDDStream, addWebDD);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END
    }
  }//downloadAddDDFromDB(DownloadAppFilesInfo downloadAppFilesInfo, String aliasDir, String webInfDir, Configuration config, String caller)

  private void downloadMergedDDFromDB(DownloadAppFilesInfo downloadAppFilesInfo, String aliasDir, String webInfDir, Configuration config, String caller) throws IOException, ConfigurationException {
    String tagName = null;

    boolean download = false;
    //Check whether downloading is needed
    if (downloadAppFilesInfo.isDownloadAll()) {
      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/config.existsFile (" + Constants.MERGED_WEB_DD + aliasDir + ")";
          Accounting.beginMeasure(tagName, Configuration.class);
        }//ACCOUNTING.start - END
        if (config.existsFile(Constants.MERGED_WEB_DD + aliasDir)) {
          download = true;
        }
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END
    } else if (downloadAppFilesInfo.isDownloadPartially()) {
      ArrayEnumeration filesForDownload = downloadAppFilesInfo.getFolderCompareResult().getFilesForDownload();
      while (filesForDownload.hasNext()) {
        String modifiedFile = (String) filesForDownload.next();
        if (traceLocation.beDebug()){
        	traceLocation.debugT("ActionBase.downloadMergedDDFromDB; aliasDir["+aliasDir+"]; modifiedFile ["+modifiedFile+"] will be compared with ["+
        			File.separator + Constants.MERGED_WEB_DD + aliasDir+"].");
        }
        if ((File.separator + Constants.MERGED_WEB_DD + aliasDir).equals(modifiedFile)) {
          download = true;
          break;
        }
      }
    }

    if (traceLocation.beDebug()){
    	traceLocation.debugT("ActionBase.downloadMergedDDFromDB; aliasDir["+aliasDir+"]; download flag = ["+download+"].");
	}
    if (download) {
      File mergedWebDD = new File(webInfDir + "web.xml");
      FileInputStream mergedWebDDFileInputStream = null;
      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          tagName = caller + "/checkMergedWebDDFile (" + mergedWebDD.getName() + ")";
          Accounting.beginMeasure(tagName, ActionBase.class);
        }//ACCOUNTING.start - END
        mergedWebDDFileInputStream = new FileInputStream(mergedWebDD);
      } catch (FileNotFoundException fnfe) {
        mergedWebDDFileInputStream = null;
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      if (mergedWebDDFileInputStream != null) {
        try {
          mergedWebDDFileInputStream.close();
        } catch(Exception e) {
          if (traceLocation.beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000096",
              "Cannot close file [{0}]. This may lead to file handlers leak.", new Object[] {mergedWebDD.getName()}, e, null, null);
          }
        }

        if (!mergedWebDD.delete()) {
          throw new WebIOException(WebIOException.CANNOT_DELETE_FILE, new Object[]{"web.xml", downloadAppFilesInfo.getApplicationName()});
        }
      }

      InputStream mergedWebDDStream = null;
      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/config.getFile(" + Constants.MERGED_WEB_DD + aliasDir + ")";
          Accounting.beginMeasure(tagName, Configuration.class);
        }//ACCOUNTING.start - END
        mergedWebDDStream = config.getFile(Constants.MERGED_WEB_DD + aliasDir);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/writeToFile";
          Accounting.beginMeasure(tagName, FileUtils.class);
        }//ACCOUNTING.start - END
        FileUtils.writeToFile(mergedWebDDStream, mergedWebDD);
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END
    }
  }//downloadMergedDDFromDB(DownloadAppFilesInfo downloadAppFilesInfo, String aliasDir, String webInfDir, Configuration config, String caller)

  /**
   *
   * @param config
   * @param downloadAppFilesInfo
   * @param caller
   * @throws IOException
   * @throws ConfigurationException
   */
  private void loadUpdatesFromDBase(Configuration config, DownloadAppFilesInfo downloadAppFilesInfo, String caller) throws IOException, ConfigurationException {
    String tagName = null;

    if (downloadAppFilesInfo.isDownloadAll() || downloadAppFilesInfo.isDownloadPartially()) {
      long fileId;
      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/config.getConfigEntry (" + Constants.FILE_COUNTER + ")";
          Accounting.beginMeasure(tagName, Configuration.class);
        }//ACCOUNTING.start - END
        fileId = Long.parseLong(((String) config.getConfigEntry(Constants.FILE_COUNTER)));
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.beginMeasure(caller + "/update custom changed files", ActionBase.class);
        }//ACCOUNTING.start - END

        for (int i = 1; i < fileId; i++) {
          String nextFilename = "#" + i;
          boolean download = false;
          if (downloadAppFilesInfo.getFolderCompareResult() == null) {
            download = true;
          } else {
            ArrayEnumeration filesForDownload = downloadAppFilesInfo.getFolderCompareResult().getFilesForDownload();
            while (filesForDownload.hasNext()) {
              String modifiedFile = (String) filesForDownload.next();
              if (traceLocation.beDebug()){
              	traceLocation.debugT("ActionBase.loadUpdatesFromDBase; modifiedFile ["+modifiedFile+"] will be compared with ["
              			+File.separator + "update" + File.separator + nextFilename+"].");
              }
              if ((File.separator + "update" + File.separator + nextFilename).equals(modifiedFile)) {
                download = true;
                break;
              }
            }
          }

      	if (traceLocation.beDebug()){
        	traceLocation.debugT("ActionBase.loadUpdatesFromDBase; download flag = ["+download+"].");
    	}

          if (download) {
            String file = null;
            try {//ACCOUNTING.end - BEGIN
              if (Accounting.isEnabled()) {
                tagName = caller + "/config.getConfigEntry (" + nextFilename.substring(1) + ")";
                Accounting.beginMeasure(tagName, Configuration.class);
              }//ACCOUNTING.start - END
              file = (String) config.getConfigEntry(nextFilename.substring(1));
            } finally {//ACCOUNTING.end - BEGIN
              if (Accounting.isEnabled()) {
                Accounting.endMeasure(tagName);
              }
            }//ACCOUNTING.end - END

            File hddFile = new File(WebContainerHelper.getDeployTempDir(downloadAppFilesInfo.getApplicationName()), file);
            FileInputStream hddFileInputStream = null;
            try {
              if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
                tagName = caller + "/checkHddFile (" + hddFile.getName() + ")";
                Accounting.beginMeasure(tagName, ActionBase.class);
              }//ACCOUNTING.start - END
              hddFileInputStream = new FileInputStream(hddFile);
            } catch (FileNotFoundException fnfe) {
              hddFileInputStream = null;
            } finally {//ACCOUNTING.end - BEGIN
              if (Accounting.isEnabled()) {
                Accounting.endMeasure(tagName);
              }
            }//ACCOUNTING.end - END

            if (hddFileInputStream != null) {
              try {
                hddFileInputStream.close();
              } catch(Exception e) {
                if (traceLocation.beWarning()) {
                  LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000170",
                    "Cannot close file [{0}]. This may lead to file handlers leak.", new Object[] {hddFile.getName()}, e, null, null);
                }
              }

              if (!hddFile.delete()) {
                throw new WebIOException(WebIOException.CANNOT_DELETE_FILE, new Object[]{hddFile.getName(), downloadAppFilesInfo.getApplicationName()});
              }
            }

            File f = null;
            try {//ACCOUNTING.end - BEGIN
              if (Accounting.isEnabled()) {
                tagName = caller + "/createRootDir";
                Accounting.beginMeasure(tagName, ActionBase.class);
              }//ACCOUNTING.start - END
              f = new File(hddFile.getParent());
              if (!f.exists()) {
                if (!f.mkdirs()) {
                  if (!f.mkdirs()) {
                    throw new WebIOException(WebIOException.CANNOT_MAKE_DIRS, new Object[]{f.getAbsolutePath()});
                  }
                }
              }
            } finally {//ACCOUNTING.end - BEGIN
              if (Accounting.isEnabled()) {
                Accounting.endMeasure(tagName);
              }
            }//ACCOUNTING.end - END

            InputStream nextFilenameStream = null;
            try {//ACCOUNTING.end - BEGIN
              if (Accounting.isEnabled()) {
                tagName = caller + "/config.getFile(" + nextFilename + ")";
                Accounting.beginMeasure(tagName, Configuration.class);
              }//ACCOUNTING.start - END
              nextFilenameStream = config.getFile(nextFilename);
            } finally {//ACCOUNTING.end - BEGIN
              if (Accounting.isEnabled()) {
                Accounting.endMeasure(tagName);
              }
            }//ACCOUNTING.end - END

            try {//ACCOUNTING.end - BEGIN
              if (Accounting.isEnabled()) {
                tagName = caller + "/writeToFile";
                Accounting.beginMeasure(tagName, FileUtils.class);
              }//ACCOUNTING.start - END
              FileUtils.writeToFile(nextFilenameStream, hddFile);
            } finally {//ACCOUNTING.end - BEGIN
              if (Accounting.isEnabled()) {
                Accounting.endMeasure(tagName);
              }
            }//ACCOUNTING.end - END
          }
        }
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(caller + "/update custom changed files");
        }
      }//ACCOUNTING.end - END
    }
  }//end of loadUpdatesFromDBase(Configuration config, DownloadAppFilesInfo downloadAppFilesInfo, String caller)

  /**
   *
   * @param config
   * @param downloadAppFilesInfo
   * @param caller
   * @throws ConfigurationException
   * @throws IOException
   */
  private void loadAdminUpdatesFromDBase(Configuration config, DownloadAppFilesInfo downloadAppFilesInfo, String caller) throws ConfigurationException, IOException {
    if (config == null) {
      return;
    }

    String applicationName = downloadAppFilesInfo.getApplicationName();
    String[] allAliases = downloadAppFilesInfo.getAllAliases();
    String tagName = null;
    for (int i = 0; allAliases != null && i < allAliases.length; i++) {
      String aliasCanonicalized = null;
      String aliasDir = null;
      String rootDir = null;
      String webInfDir = null;

      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/initDirNames";
          Accounting.beginMeasure(tagName, ActionBase.class);
        }//ACCOUNTING.start - END
        aliasCanonicalized = ParseUtils.convertAlias(allAliases[i]);
        aliasDir = WebContainerHelper.getAliasDirName(aliasCanonicalized);
        rootDir = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir, "root"});
        webInfDir = rootDir + "WEB-INF" + File.separator;
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }//ACCOUNTING.end - END

      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        tagName = caller + "/downloadDDFromDB(" + aliasCanonicalized + ")";
        Accounting.beginMeasure(tagName, ActionBase.class);
      }//ACCOUNTING.start - END

      downloadDDFromDB(downloadAppFilesInfo, aliasDir, webInfDir, config, tagName);

      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(tagName);
        tagName = caller + "/downloadAddDDFromDB(" + aliasCanonicalized + ")";
        Accounting.beginMeasure(tagName, ActionBase.class);
      }//ACCOUNTING.start - END

      downloadAddDDFromDB(downloadAppFilesInfo, aliasDir, webInfDir, config, tagName);

      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(tagName);
      }//ACCOUNTING.end - END
    }
  }//end of loadAdminUpdatesFromDBase(Configuration config, DownloadAppFilesInfo downloadAppFilesInfo, String caller)


  //_____Classpath_____

  /**
   * @param applicationName
   * @param aliasesCanonicalized
   * @param privateCL
   * @return
   */
  protected String[] getFilesForClassPath(String applicationName, String[] aliasesCanonicalized, boolean privateCL) {
    Vector<String> result = new Vector<String>();
    Vector<String> temp = null;

    for (int i = 0; aliasesCanonicalized != null && i < aliasesCanonicalized.length; i++) {
      temp = getFilesForClassPath(applicationName, aliasesCanonicalized[i], privateCL);
      for (int j = 0; j < temp.size(); j++) {
        result.add(temp.elementAt(j));
      }
    }

    return (String[]) result.toArray(new String[result.size()]);
  }//end of getFilesForClassPath(String applicationName, String[] aliasesCanonicalized, boolean privateCL)

  /**
   * Collects the resources for the application in the proper order.
   *
   * @param applicationName
   * @param aliasCanonicalized - application alias
   * @param privateCL          - if true - generates private classloader classpath else - public.
   * @return
   */
  private Vector<String> getFilesForClassPath(String applicationName, String aliasCanonicalized, boolean privateCL) {
    Vector<String> tmp = new Vector<String>();

    String aliasDir = WebContainerHelper.getAliasDirName(aliasCanonicalized);
    String webAppDir = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir, "root"});

      if (!privateCL) {
        String classesDir = webAppDir + WebContainerHelper.getDirName(new String[]{"WEB-INF", "classes"});
        if ((new File(classesDir).exists())) {
          tmp.addElement(classesDir);
        }

        addLibClassPath(tmp, webAppDir + "WEB-INF");
      } else {
        String classesDir = webAppDir + WebContainerHelper.getDirName(new String[]{"WEB-INF", "private", "classes"});
        if ((new File(classesDir).exists())) {
          tmp.addElement(classesDir);
        }

        String workDir = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir, "work"});
        File f = new File(workDir);
        if (!f.exists()) {
          if (!f.mkdir()) {
            if (!f.mkdir()) {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000059",
                "Cannot create directory with path [{0}] due to file system access problem . Possible reason: file access denied, file in use by other thread or process.", new Object[]{f.getAbsolutePath()}, null, null);
            }
          }
        }
        tmp.addElement(workDir);

        addLibClassPath(tmp, webAppDir + "WEB-INF" + File.separator + "private");
    }

    return tmp;
  }//end of getFilesForClassPath(String applicationName, String aliasCanonicalized, boolean privateCL)

  /**
   * Adds classpath to all jar-files in \web-inf\lib
   * that will be used from classloader to search class files.
   *
   * @param vectorClassPath
   * @param directory
   */
  private void addLibClassPath(Vector<String> vectorClassPath, String directory) {
    String libDir = WebContainerHelper.getDirName(new String[]{directory, "lib"});
    File f = new File(libDir);

    String[] list = null;
    if (f.exists()) {
      list = f.list();
    }

    if (list != null) {
      for (int i = 0; i < list.length; i++) {
        if (list[i].endsWith(".jar") || list[i].endsWith(".JAR") || list[i].endsWith(".zip") || list[i].endsWith(".ZIP")) {
          vectorClassPath.add(libDir + list[i]);
        }
      }
    }
  }//end of addLibClassPath(Vector vectorClassPath, String directory)


  //_____File operations_____

  /**
   * Invoked during deploy update and download application files.
   * @param applicationName
   * @param webApplWorkDirName
   * @param warFile
   * @throws DeploymentException
   */
  protected void extractWar(String applicationName, String webApplWorkDirName, File warFile, String caller) throws DeploymentException {
    try {//ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.beginMeasure(caller + "/extractWar (" + warFile.getName() + ")", JarExtractor.class);
      }//ACCOUNTING.start - END

      File tempDir = new File(webApplWorkDirName + "root" + File.separator);

      if (!warFile.exists()) {
        return;
      }

      if (warFile.getName().startsWith("temp_") && tempDir.exists()) {
        FileUtils.deleteDirectory(tempDir);
      }

      JarExtractor jarExtractor = new JarExtractor(warFile.getAbsolutePath(), tempDir.getAbsolutePath());
      jarExtractor.extractJar();

    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_EXTRACT_THE_WAR_FILE_OF_THE_APPLICATION,
        new Object[]{warFile.getName(), applicationName}, e);
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(caller + "/extractWar (" + warFile.getName() + ")");
      }
    }//ACCOUNTING.end - END
  }//end of extractWar(String applicationName, String webApplWorkDirName, File warFile)

  /**
   * This method will be invoked in the beginning of start application, before prepareStart().
   * The purpose is downloading from DB the files, needed by the container for the start operation
   * and after that prepareStart() will be invoked.
   *
   * @param applicationName
   * @param appConfig
   * @param caller
   * @throws DeploymentException
   * @throws WarningException
   */
  protected void downloadAppFiles(String applicationName, Configuration appConfig, String caller) throws DeploymentException, WarningException {
	boolean beDebug = traceLocation.beDebug();
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        caller += "/downloadAppFiles(" + applicationName + ")";
        Accounting.beginMeasure(caller, ActionBase.class);
      }//ACCOUNTING.start - END

      String tagName = null;

      FolderCompareResult fcr = null;
      boolean downloadAll = false;
      boolean downloadPartially = false;
      try {
        if (Accounting.isEnabled()) {
          tagName = caller + "/getIndex";
          Accounting.beginMeasure(tagName, ActionBase.class);
        }
        Index appIndexDB = getIndexDB(appConfig);
        if (beDebug){
        	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"]. appIndexDB: ["+appIndexDB+"].");
        }
        if (appIndexDB != null) {
          final Index appIndexFS = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getIndexFS(applicationName);
          if (beDebug){
          	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"].  appIndexFS: ["+appIndexFS+"].");
          }
          if (appIndexFS != null) {
            // Index (on container level) - depends where each container stores application binaries
            Index containerIndexDB = getContainerIndex(appIndexDB, containerInfo.getName());
            if (beDebug){
              	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"].  containerIndexDB: ["+containerIndexDB+"].");
            }
           if (containerIndexDB != null) {
              Index containerIndexFS = getContainerIndex(appIndexFS, containerInfo.getName());
              if (containerIndexFS != null) {
                // What is the difference between FS and DB
                fcr = containerIndexFS.compare(containerIndexDB, true);
                // Equalize them
                if (fcr.isChanged()) {
                  if (beDebug){
                   	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"].  The container index on FS and DB is changed. downloadPatially = true");
                  }
                  downloadPartially = true;
                }
              } else {
                // containerIndexFS is null
                downloadAll = true;
              }
            } else {
              // containerIndexDB is null
              downloadAll = true;
            }
          } else {
            // appIndexFS is null
            downloadAll = true;
          }
        } else {
          // appIndexDB is null
          downloadAll = true;
        }
      } finally {
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      }
      if (beDebug){
         	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"]. downloadAll = ["+downloadAll+"]; downloadPartially = ["+downloadPartially+"].");
      }
      if (downloadAll || downloadPartially) {
        //Download if there is something for downloading, otherwise skip it
        DownloadAppFilesInfo downloadAppFilesInfo = new DownloadAppFilesInfo(applicationName, fcr, downloadAll, downloadPartially);
        Configuration servlet_jspConfig = null;
        Configuration servlet_jspUpdate = null;
        Configuration servlet_jspAdmin = null;
        boolean isAdminExists = false;
        try { //ACCOUNTING.start - BEGIN
          if (Accounting.isEnabled()) {
            tagName = caller + "/getSubConfiguration (" + containerInfo.getName() + ")";
            Accounting.beginMeasure(tagName, Configuration.class);
          } //ACCOUNTING.start - END
          servlet_jspConfig = appConfig.getSubConfiguration(containerInfo.getName());
          if (beDebug){
           	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"]. /getSubConfiguration(containerInfo.getName())step completed.");
          }
        } finally { //ACCOUNTING.end - BEGIN
          if (Accounting.isEnabled()) {
            Accounting.endMeasure(tagName);
          }
        } //ACCOUNTING.end - END
        try { //ACCOUNTING.start - BEGIN
          if (Accounting.isEnabled()) {
            tagName = caller + "/getSubConfiguration (" + Constants.UPDATE + ")";
            Accounting.beginMeasure(tagName, Configuration.class);
          } //ACCOUNTING.start - END
          servlet_jspUpdate = servlet_jspConfig.getSubConfiguration(Constants.UPDATE);
          if (beDebug){
             	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"]. /getSubConfiguration(Constants.UPDATE) step completed.");
          }
        } finally { //ACCOUNTING.end - BEGIN
          if (Accounting.isEnabled()) {
            Accounting.endMeasure(tagName);
          }
        } //ACCOUNTING.end - END
        try { //ACCOUNTING.start - BEGIN
          if (Accounting.isEnabled()) {
            tagName = caller + "/getSubConfiguration (" + Constants.ADMIN + ")";
            Accounting.beginMeasure(tagName, Configuration.class);
          } //ACCOUNTING.start - END
          isAdminExists = servlet_jspConfig.existsSubConfiguration(Constants.ADMIN);
          if (beDebug){
           	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"]. isAdminExists = ["+isAdminExists+"].");
          }
          if (isAdminExists) {
            servlet_jspAdmin = servlet_jspConfig.getSubConfiguration(Constants.ADMIN);
          }
        } finally { //ACCOUNTING.end - BEGIN
          if (Accounting.isEnabled()) {
            Accounting.endMeasure(tagName);
          }
        } //ACCOUNTING.end - END

        try { //ACCOUNTING.start - BEGIN
          if (Accounting.isEnabled()) {
            tagName = caller + "/loadFromDBase";
            Accounting.beginMeasure(tagName, ActionBase.class);
          } //ACCOUNTING.start - END
          loadFromDBase(servlet_jspConfig, downloadAppFilesInfo, tagName);
          if (beDebug){
             	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"]. /loadFromDB step completed.");
          }
        } finally { //ACCOUNTING.end - BEGIN
          if (Accounting.isEnabled()) {
            Accounting.endMeasure(tagName);
          }
        } //ACCOUNTING.end - END
        try { //ACCOUNTING.start - BEGIN
          if (Accounting.isEnabled()) {
            tagName = caller + "/loadUpdatesFromDBase";
            Accounting.beginMeasure(tagName, ActionBase.class);
          } //ACCOUNTING.start - END
          loadUpdatesFromDBase(servlet_jspUpdate, downloadAppFilesInfo, tagName);
          if (beDebug){
           	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"]. /loadUpdatesFromDBase step completed.");
          }
        } finally { //ACCOUNTING.end - BEGIN
          if (Accounting.isEnabled()) {
            Accounting.endMeasure(tagName);
          }
        } //ACCOUNTING.end - END
        try { //ACCOUNTING.start - BEGIN
          if (Accounting.isEnabled()) {
            tagName = caller + "/loadAdminUpdatesFromDBase";
            Accounting.beginMeasure(tagName, ActionBase.class);
          } //ACCOUNTING.start - END
          loadAdminUpdatesFromDBase(servlet_jspAdmin, downloadAppFilesInfo, tagName);
          if (beDebug){
             	traceLocation.debugT("ActionBase.downLoadAppFiles; application["+applicationName+"]. /loadAdminUpdatesFromDBase step completed.");
          }
        } finally { //ACCOUNTING.end - BEGIN
          if (Accounting.isEnabled()) {
            Accounting.endMeasure(tagName);
          }
        } //ACCOUNTING.end - END
      }

      //Create work directories
      try { //ACCOUNTING.start - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/createWorkDirectories";
          Accounting.beginMeasure(tagName, ActionBase.class);
        } //ACCOUNTING.start - END
        createWorkDirectories(applicationName, caller);
        if(beDebug){
        	traceLocation.debugT("ActionBase.downLoadAppFileliacation["+applicationName+"]. /createWorkDirectories step is completed.");
        }
      } finally { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      } //ACCOUNTING.end - END
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_DOWNLOAD_THE_INFORMATION_OF_THE_APPLICATION_FROM_DATABASE,
        new Object[]{applicationName}, e);
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(caller);
      }
    } //ACCOUNTING.end - END
  }//end of downloadAppFiles(String applicationName, Configuration appConfig, String caller)

  /**
   * @param applicationName
   */
  private void createWorkDirectories(String applicationName, String caller) throws IOException {
    String tagName = null;
    String[] aliasesCanonicalized = null;
    String deployDir = null;

    aliasesCanonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    deployDir = WebContainerHelper.getDeployTempDir(applicationName);

    for (int i = 0; aliasesCanonicalized != null && i < aliasesCanonicalized.length; i++) {
      String aliasDir = WebContainerHelper.getAliasDirName(aliasesCanonicalized[i]);

      String workDir = WebContainerHelper.getDirName(new String[]{deployDir, aliasDir, "work"});
      File f = new File(workDir);
      if (!f.exists()) {
        if (!f.mkdirs()) {
          if (!f.mkdirs()) {
            throw new WebIOException(WebIOException.CANNOT_MAKE_DIRS, new Object[]{f.getAbsolutePath()});
          }
        }
      }

      try {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = caller + "/createWorkDirectories/processParserVersion (" + aliasesCanonicalized[i].replace("/", "_") + ")";
          Accounting.beginMeasure(tagName, ActionBase.class);
        }//ACCOUNTING.start - END
        processParserVersion(f, applicationName, tagName);
      } finally { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      } //ACCOUNTING.end - END

      // create work directory for parsed tag files placed under WEB-INF/tags.
      String webTags = WebContainerHelper.getDirName(new String[]{workDir, "web"});
      f = new File(webTags);
      if (!f.exists()) {
        if (!f.mkdirs()) {
          if (!f.mkdirs()) {
            throw new WebIOException(WebIOException.CANNOT_MAKE_DIRS, new Object[]{f.getAbsolutePath()});
          }
        }
      }

      // create work directory for parsed tag files packaged in jars.
      String jarTags = WebContainerHelper.getDirName(new String[]{workDir, "jar"});
      f = new File(jarTags);
      if (!f.exists()) {
        if (!f.mkdirs()) {
          if (!f.mkdirs()) {
            throw new WebIOException(WebIOException.CANNOT_MAKE_DIRS, new Object[]{f.getAbsolutePath()});
          }
        }
      }

      f = new File(WebContainerHelper.getDirName(new String[]{deployDir, aliasDir, "tempwork"}));
      if (!f.exists()) {
        if (!f.mkdirs()) {
          if (!f.mkdirs()) {
            throw new WebIOException(WebIOException.CANNOT_MAKE_DIRS, new Object[]{f.getAbsolutePath()});
          }
        }
      }
    }
  }//end of createWorkDirectories(String applicationName, String caller)

  /**
   * seeks parser.version file
   * if not found all content of the work directory is deleted and the file is created
   * if the version in the file is different than current version of the parser
   * file is updated and all content of the work directory is deleted
   */
  private void processParserVersion(File workDir, String applicationName, String operation) throws IOException {
    String tagName = null;
    String externalCompilerName = null;
    String encoding = null;
    ParserPropertiesFile parserPropertiesFile = null;
    boolean isParserPropertiesChanged = false;

    try { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        tagName = operation + "/getExternalCompiler";
        Accounting.beginMeasure(tagName, ActionBase.class);
      } //ACCOUNTING.start - END
      externalCompilerName = ServiceContext.getServiceContext().getWebContainerProperties().getExternalCompiler();
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(tagName);
      }
    } //ACCOUNTING.end - END
    try { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        tagName = operation + "/javaEncoding";
        Accounting.beginMeasure(tagName, ActionBase.class);
      } //ACCOUNTING.start - END
      encoding = ServiceContext.getServiceContext().getWebContainerProperties().javaEncoding();
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(tagName);
      }
    } //ACCOUNTING.end - END
    try { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        tagName = operation + "/ParserPropertiesFile.getInstance()";
        Accounting.beginMeasure(tagName, ActionBase.class);
      } //ACCOUNTING.start - END
      parserPropertiesFile = ParserPropertiesFile.getInstance(workDir, applicationName, externalCompilerName, encoding);
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(tagName);
      }
    } //ACCOUNTING.end - END
    try { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        tagName = operation + "/parserPropertiesFile.isParserPropertiesChanged()";
        Accounting.beginMeasure(tagName, ParserPropertiesFile.class);
      } //ACCOUNTING.start - END
      isParserPropertiesChanged = parserPropertiesFile.isParserPropertiesChanged();
    } finally { //ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(tagName);
      }
    } //ACCOUNTING.end - END
    if (isParserPropertiesChanged) {
      try { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = operation + "/parserPropertiesFile.deleteWork()";
          Accounting.beginMeasure(tagName, ParserPropertiesFile.class);
        } //ACCOUNTING.start - END
        parserPropertiesFile.deleteWork();
      } finally { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      } //ACCOUNTING.end - END
      try { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = operation + "/parserPropertiesFile.createParserPropertiesFile()";
          Accounting.beginMeasure(tagName, ParserPropertiesFile.class);
        } //ACCOUNTING.start - END
        parserPropertiesFile.createParserPropertiesFile();
      } finally { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      } //ACCOUNTING.end - END
    }
  }//end of processParserVersion(File workDir, String applicationName)


  //_____Remove application_____

  /**
   * Removes the specified components from this container.
   *
   * @param applicationName
   * @param isUpdate        if isUpdate is true then security resources will not be removed, else all security resources will be removed.
   * @throws DeploymentException
   * @throws WarningException
   */
  protected void remove(String applicationName, boolean isUpdate, Configuration appConfiguration) throws DeploymentException, WarningException {
    if (applicationName == null) {
      return;
    }
    ArrayList<LocalizableTextFormatter> warnings = new ArrayList<LocalizableTextFormatter>();
    String tagName = null;
    String debugInfo = (!isUpdate) ? "remove" : "makeUpdate";

    String[] aliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);

    if (aliases != null) {
      String[] deployedApplications = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getMyApplications();

      String[] aliasesCannonicalized = new String[aliases.length];
      boolean onRemove = true;
      boolean removingDoubledAlias = false;
      boolean isHigherPrioRemoving = true; // the value of this variable is meaningful only if the value of removingDoubledAlias is true
      for (int i = 0; i < aliases.length; i++) {
        aliasesCannonicalized[i] = ParseUtils.convertAlias(aliases[i]);
        Vector<WebModule> webModules = ((WebContainerProvider)iWebContainer).getDeployedAppls(aliasesCannonicalized[i]);
        if (webModules != null && webModules.size() == 2) {
          removingDoubledAlias = true;
          onRemove = true;
          // there are two web modules for aliasesCannonicalized[i] alias
          if (applicationName.equalsIgnoreCase(((WebContainerProvider)iWebContainer).getDeployedAppl(aliasesCannonicalized[i]).getWholeApplicationName())) {
            // removing the alias with higher priority -> used to remove the alias from http service in order to register the other one later
            isHigherPrioRemoving = true;
            if (traceLocation.beInfo()){
              LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceInfo("Removing alias " + aliasesCannonicalized[i] + " for which there are two applications ([" +
                  webModules.elementAt(0).getWholeApplicationName() + "] and [" + webModules.elementAt(1).getWholeApplicationName()+ "]), defining it. Currently it is removing [" +
                  applicationName + "] which is with HIGHER priority than [" + webModules.elementAt(1).getWholeApplicationName() + "];", aliasesCannonicalized[i]);
            }
          } else {
            isHigherPrioRemoving = false;
            if (traceLocation.beInfo()){
              LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceInfo("Removing alias " + aliasesCannonicalized[i] + " for which there are two applications ([" +
                  webModules.elementAt(0).getWholeApplicationName() + "] and [" + webModules.elementAt(1).getWholeApplicationName()+ "]), defining it. Currently it is removing [" +
                  applicationName + "] which is with LOWER priority than [" + webModules.elementAt(1).getWholeApplicationName() + "];", aliasesCannonicalized[i]);
            }
          }
        } else {
          //Call onRemove() only when it is a real remove, but not when it is roll back deploy due to already used alias!! (the same alias but different application name)
          for (int j = 0; deployedApplications != null && j < deployedApplications.length; j++) {
            //get canonicalized aliases
            String[] deployedAliases = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(deployedApplications[j]);
            for (int k = 0; deployedAliases != null && k < deployedAliases.length; k++) {
              if (aliasesCannonicalized[i].equals(deployedAliases[k])) {
                if (!applicationName.equals(deployedApplications[j])) {
                  //roll-back due to already used alias!! (the same alias but different application name)
                  onRemove = false;
                } else {
                  break;
                }
              }
            }
          }
        }
      }

      if (onRemove) {
        try {
          Configuration servlet_jspConfig = null;
        	if (appConfiguration != null) {
        		servlet_jspConfig = ConfigurationUtils.getSubConfiguration(appConfiguration, Constants.CONTAINER_NAME, applicationName, false);
        	}
          iWebContainer.remove(applicationName, aliasesCannonicalized, isUpdate, servlet_jspConfig);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000060",
            "Error occurred when invoking remove methods of web container extensions for [{0}] application.", new Object[]{applicationName}, e, null, null);
        }
      }

      for (int i = 0; i < aliases.length; i++) {
        String aliasCanonicalized = aliasesCannonicalized[i];

        if (!isUpdate) {
          try {
            if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
              tagName = debugInfo + "/removeSecurityResources (" + aliasCanonicalized + ")";
              Accounting.beginMeasure(tagName, ServiceContext.getServiceContext().getSecurityContext().getClass());
            } //ACCOUNTING.start - END
           	securityUtils.removeSecurityResources(applicationName, aliasCanonicalized);
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable e) {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000061",
              "Error while trying to remove security resources for web application [{0}].", new Object[]{aliasCanonicalized}, e, null, null);
          } finally { //ACCOUNTING.end - BEGIN
            if (Accounting.isEnabled()) {
              Accounting.endMeasure(tagName);
            }
          } //ACCOUNTING.end - END
        }

        boolean removeAlias = true;
        boolean aliasFoundInDeployedApps = false;
        for (int j = 0; deployedApplications != null && j < deployedApplications.length; j++) {
          //get canonicalized aliases
          String[] deployedAliases = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(deployedApplications[j]);

          for (int k = 0; deployedAliases != null && k < deployedAliases.length; k++) {
            if (aliasCanonicalized.equals(deployedAliases[k])) {
              aliasFoundInDeployedApps = true;
              if (applicationName.equals(deployedApplications[j])) {
                //remove_app!! (the same alias and application name)
                removeAlias = true;
                ApplicationContext scf = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(aliasCanonicalized.getBytes()));
                webContainerHelper.removeStartedApplication(new MessageBytes(aliasCanonicalized.getBytes()), applicationName);
                if (scf != null) {
                	if (traceLocation.beWarning()){
                		LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000502",
                				"Web application [{0}] was not stopped before remove.", new Object[]{aliasCanonicalized}, null, null);
                	}
                	scf.destroy();
                }
                try {
                  if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
                    tagName = debugInfo + "/sessionDomain.destroy (" + aliasCanonicalized + ")";
                    Accounting.beginMeasure(tagName, SessionDomain.class);
                  } //ACCOUNTING.start - END
                  SessionDomain sessionDomain = webContainerHelper.getSessionContext().findSessionDomain("/" + aliasCanonicalized);
                  if (sessionDomain != null) {
                    sessionDomain.destroy();
                  }
                } catch (OutOfMemoryError e) {
                  throw e;
                } catch (ThreadDeath e) {
                  throw e;
                } catch (Throwable e) {
                	LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000062",
                    "Error while destroying session domain for web application [{0}].", new Object[]{aliasCanonicalized}, e, null, null);
                } finally { //ACCOUNTING.end - BEGIN
                  if (Accounting.isEnabled()) {
                    Accounting.endMeasure(tagName);
                  }
                } //ACCOUNTING.end - END
              } else {
                removeAlias = false;
                //roll-back due to already used alias!! (the same alias but different application name)
              }
              break;
            }
          }// for each deployed alias and canonicalized
        }

        if ((removeAlias || !aliasFoundInDeployedApps) && !removingDoubledAlias) {
          // !aliasFoundInDeployedApps => rollback but not due to the same alias name
          try {
            if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
              tagName = debugInfo + "/removeApplicationAlias (" + aliasCanonicalized + ")";
              Accounting.beginMeasure(tagName, HttpProvider.class);
            } //ACCOUNTING.start - END
          	//Clear cache by alias
            ServiceContext.getServiceContext().getHttpProvider().removeApplicationAlias(aliasCanonicalized, true);
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.CANNOT_REMOVE_ALIAS_FROM_HTTP_WHILE_REMOVING_APPLICATION, new Object[]{aliasCanonicalized, applicationName}));
          } finally { //ACCOUNTING.end - BEGIN
            if (Accounting.isEnabled()) {
              Accounting.endMeasure(tagName);
            }
          } //ACCOUNTING.end - END
        }
        //Clear cache by alias
        //TODO: the following is not really necessary, because is done above when (removeAlias || !aliasFoundInDeployedApps)
        //but we keep it just in case for backwards compatibility
        ServiceContext.getServiceContext().getHttpProvider().clearCacheByAlias(aliasCanonicalized);
      } //for each web alias

      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.beginMeasure(debugInfo + "/changeLoadBalance", HttpProvider.class);
        } //ACCOUNTING.start - END

        //TODO This is only a workaround and MUST be think about correct way to unregister 'lazy' applications from HTTP provider and ICM.
        if (ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(applicationName) == StartUp.LAZY.getId().byteValue()) {
          if (removingDoubledAlias) {
            if (isHigherPrioRemoving) {
              // remove the current removing application from the http runtime structure
              ServiceContext.getServiceContext().getHttpProvider().changeLoadBalance(applicationName, aliases, false);
              // register the other application in http provider and SHM
              // NOTE: if this code is used for applications different than / (default application), change the aliasName
              // and have in mind that one application can have more than one alias
              String otherApplName = ((WebContainerProvider)iWebContainer).getDeployedAppl(ParseUtils.separator).getWholeApplicationName();

              // update the http hosts
              String webApplWorkDirName = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(otherApplName), Constants.defaultAliasDir});
              String rootDir = webApplWorkDirName + "root" + File.separator;
              ServiceContext.getServiceContext().getHttpProvider().startApplicationAlias(ParseUtils.separator, rootDir);

              // register in the SHM
              String[] otherAliases = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(otherApplName);
              ServiceContext.getServiceContext().getHttpProvider().changeLoadBalance(otherApplName, otherAliases, true);
            } else {
              // do not change anything in http - in http the active application is default application which is NOT removing;
            }
          } else {
            // check if default application
            boolean isDefaultApplication = false;
            for (String alias : aliases) {
              if (alias.equals("/")) {
                isDefaultApplication = true;
                break;
              }
            }
            if (isDefaultApplication) {
              WebModule webModule = ((WebContainerProvider)iWebContainer).getDeployedAppl("/");
              if (webModule == null) {
                // there is no more applications left for default alias - remove it from http as well
                ServiceContext.getServiceContext().getHttpProvider().changeLoadBalance(applicationName, aliases, false);
                if (traceLocation.beInfo()){
                  LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceInfo("Removing/Rollbacking default application [" +
                      applicationName + "] and no more applications which are defining / alias will be left.", applicationName);
                }
              } else if (!webModule.getWholeApplicationName().equalsIgnoreCase(applicationName)) {
                // do nothing - do not remove the default application from http
                if (traceLocation.beInfo()){
                  LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceInfo("Removing/Rollbacking default application [" +
                      applicationName + "]. Application [" + webModule.getWholeApplicationName() + "] is left to define /.", applicationName);
                }
              }
            } else {
              // it is not default application: remove it from http because even if this is a step from rollback procedure,
              // they will be filled in when the application is requested
              ServiceContext.getServiceContext().getHttpProvider().changeLoadBalance(applicationName, aliases, false);
            }
          }
          ServiceContext.getServiceContext().getWebContainer().startedApplications.remove(applicationName);
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000063",
          "Cannot clear HTTP cache. Possible reason: cannot clear local cache or cannot clear ICM cache.", e, null, null);
      } finally { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(debugInfo + "/changeLoadBalance");
        }
      } //ACCOUNTING.end - END
    } else { //aliases == null
    	LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000064",
        "Cannot stop load balancing for [{0}] application.", new Object[]{applicationName}, null, null);
    }


    try {
      runtimeInterface.removeApp(applicationName);
    } catch (RemoteException e) {
    	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            	WebWarningException.CANNOT_NOTIFY_FOR_REMOVING_APPLICATION,
            	new Object[]{applicationName, e.toString()}));
    } catch (RuntimeException e) {
    	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            	WebWarningException.CANNOT_NOTIFY_FOR_REMOVING_APPLICATION,
            	new Object[]{applicationName, e.toString()}));

    }

    makeWarningException(warnings);
  }//end of remove(String applicationName, boolean isUpdate)


  //_____Resource references_____

  /**
   * Only for the resource-ref element
   * @param aliasCanonicalized canonicalized
   * @param webDesc
   */
  protected void setResourceReferences(String aliasCanonicalized, WebDeploymentDescriptor webDesc) {
    Properties resourceReferences = null;
    if (mapAliasResourceRef.get(aliasCanonicalized) != null) {
      resourceReferences = (Properties) ActionBase.mapAliasResourceRef.get(aliasCanonicalized);
    } else {
      resourceReferences = new Properties();
    }

    ResourceRefType[] resources = webDesc.getResReferences();

    if (resources != null) {
      for (int k = 0; k < resources.length; k++) {
        String resourceType = resources[k].getResType().get_value();
        com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType ref = webDesc.getResourceReferenceFromAdditional(resources[k]);
        String resourceName = (ref != null) ? ref.getResLink() : null;
        if (resourceName == null) {
          resourceName = resources[k].getResRefName().get_value();
        }
        if (resourceType.equals("javax.mail.Session") || resourceType.equals("java.net.URL")) {
          continue;
        }
        resourceReferences.setProperty(resourceName, resourceType);
      }
    }
    mapAliasResourceRef.put(aliasCanonicalized, resourceReferences);
  }//end of setResourceReferences(String aliasCanonicalized, WebDeploymentDescriptor webDesc)


  //_____Used when notifying for deploy/update or used when initial start happens_____

  /**
   * Prepare runtime structures for WCEs.
   * This method is called during initial server startup and when notifying for deploy/update.
   */
  protected void prepareApplicationInfo(String applicationName, boolean serviceStart) {
    String[] aliasesCanonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    if (aliasesCanonicalized != null && aliasesCanonicalized.length > 0) {
      try {
        File[] aliasesRootDirs = new File[aliasesCanonicalized.length];
        for (int i = 0; i < aliasesCanonicalized.length; i++) {
          String aliasDir = WebContainerHelper.getAliasDirName(aliasesCanonicalized[i]);
          String webApplWorkDirName = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir});
          aliasesRootDirs[i] = new File(webApplWorkDirName + "root" + File.separator);
        }

        // reads info from DB
        long time1 = System.currentTimeMillis();
        AppMetaData appMetaData = loadAppMetaData(applicationName);
        long time2 = System.currentTimeMillis();
        boolean beDebug = traceLocation.beDebug();
				if (beDebug) {
					traceLocation.debugT("prepareApplicationInfo/loadAppMetaData(" + applicationName + ") >>> " + (time2 - time1));
				}
				if (!serviceStart) {
					registerAliases(applicationName, aliasesCanonicalized, appMetaData.getUrlSessionTrackingPerModule());
				} else {
					urlSessionTrackingPerModulePerApplication.put(applicationName, appMetaData.getUrlSessionTrackingPerModule());
				}
				time1 = System.currentTimeMillis();
				if (beDebug) {
					traceLocation.debugT("prepareApplicationInfo/registerAliases(" + applicationName + ") >>> " + (time1 - time2));
				}
        loadDeployedResourcesPerModule(applicationName, aliasesCanonicalized, appMetaData);
        time2 = System.currentTimeMillis();

				if (beDebug) {
					traceLocation.debugT("prepareApplicationInfo/loadDeployedResourcesPerModule(" + applicationName + ") >>> " + (time2 - time1));
				}
        loadWceInDeployPerModule(applicationName, aliasesCanonicalized, aliasesRootDirs, appMetaData);
        time1 = System.currentTimeMillis();
				if (beDebug) {
					traceLocation.debugT("prepareApplicationInfo/appDeployedButNotStarted(" + applicationName + ") >>> " + (time1 - time2));
				}

      } catch (DeploymentException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000065",
        		"Cannot notify web container extensions that application [{0}] is deployed.", new Object[]{applicationName}, e, null, null);
      }
    }
  }//end of prepareApplicationInfo(String applicationName)

  protected void registerAliases(String applicationName, String[] aliasesCanonicalized, HashMap<String, Boolean> urlSessionTrackingPerModule) {
    long time1, time2;
    for (int i = 0; i < aliasesCanonicalized.length; i++) {
      time1 = System.currentTimeMillis();
      try {
        if (ServiceContext.getServiceContext().getClusterContext().getClusterElement(ServiceContext.getServiceContext().getServerId()).getRealState() != ClusterElement.RUNNING
            && ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(applicationName) == StartUp.LAZY.getId().byteValue()) {
          //During offline->online deployment, deploy controller starts deployment and
          //starting of the applications after service startup and before regular start of the applications.
          //Web Container prepares applications' meta data during service startup also the info in the <code>urlSessionTrackingPerModule</code>,
          //because of this the applications' meta data is missing when deploy controller deploys and starts new applications.
          //Because of this <code>urlSessionTrackingPerModule</code> is null.
          //We are not interesting for the new applications so just check for null and do nothing.
          if (urlSessionTrackingPerModule != null) {
            String aliasDir = WebContainerHelper.getAliasDirName(aliasesCanonicalized[i]);

            if (urlSessionTrackingPerModule.containsKey(aliasDir)) {
              boolean urlSessionTracking = urlSessionTrackingPerModule.get(aliasDir).booleanValue();
              if (ServiceContext.getServiceContext().getHttpProvider() != null) {
                try {
                  ServiceContext.getServiceContext().getHttpProvider().urlSessionTracking(applicationName, aliasesCanonicalized[i], urlSessionTracking);
                } catch (HttpShmException e) {
                  LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000066",
                    "Cannot start load balancing for 'Lazy' application [{0}].", new Object[]{applicationName}, e, null, null);
                }
              }
            } else {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000067",
                "Cannot get urlsessiontracking entry from configuration for [{0}] web application. The entry does not exist.",
                new Object[]{aliasesCanonicalized[i]}, null, null);
            }
          }
        }
      } catch (Exception e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000068",
          "Cannot notify HTTP Provider for 'Lazy' application [{0}].", new Object[]{applicationName}, e, null, null);
      }
      time2 = System.currentTimeMillis();
			if (traceLocation.beDebug()) {
        traceLocation.debugT("registerAliases(" + applicationName + ", " + aliasesCanonicalized[i] + ") >>> " + (time2 - time1));
      }
    }
  }//end of registerAliases(String applicationName, String[] aliasesCanonicalized, HashMap<String, Boolean> urlSessionTrackingPerModule)

  private AppMetaData loadAppMetaData(String applicationName) throws DeploymentException {
    Configuration appConfig = null;
    Configuration config = null;
    AppMetaData appMetaData = null;
    try {
      //get application configuration - apps/vendor/application_name/containername(servlet_jsp)
      appConfig = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getAppConfigurationForReadAccess(applicationName);
      config = ConfigurationUtils.getSubConfiguration(appConfig, containerInfo.getName(), applicationName, true);
    } catch (ConfigurationException e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_CONFIGURATION_FOR_APPLICATION,
          new Object[]{applicationName, applicationName}, e);
    }

    appMetaData = loadAppMetaDataObjectFromDBase(config, applicationName);
    if (appMetaData == null) {
      long time1 = System.currentTimeMillis();
      appMetaData = createAppMetaData(applicationName, config);
      long time2 = System.currentTimeMillis();
			if (traceLocation.beDebug()) {
				traceLocation.debugT("createAppMetaData(" + applicationName + ") >>> " + (time2 - time1));
			}
    }
    return appMetaData;
  }


  private AppMetaData createAppMetaData(String applicationName, Configuration config) throws DeploymentException {
    HashMap<String, Boolean> urlSessionTrackingPerModule = new HashMap<String, Boolean>();
    Hashtable<String, Hashtable<String, Vector<String>>> resourcesPerModule = new Hashtable<String, Hashtable<String, Vector<String>>>();
    Hashtable<String, Vector<String>> wceInDeployPerModule = new Hashtable<String, Vector<String>>();
    AppMetaData appMetaData = new AppMetaData();
    String[] aliasesCanonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);

    for (int i = 0; aliasesCanonicalized !=null && i < aliasesCanonicalized.length; i++) {
      try {
        //if server is not in running mode AND application startup mode is LAZY check URL_SESSION_TRACKING
        if (ServiceContext.getServiceContext().getClusterContext().getClusterElement(ServiceContext.getServiceContext().getServerId()).getRealState() != ClusterElement.RUNNING
            && ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(applicationName) == StartUp.LAZY.getId().byteValue()) {
          String aliasDir = WebContainerHelper.getAliasDirName(aliasesCanonicalized[i]);
          if (config.existsConfigEntry(Constants.URL_SESSION_TRACKING + aliasDir)) {
            Boolean urlSessionTracking = (Boolean) ConfigurationUtils.getConfigEntry(config, Constants.URL_SESSION_TRACKING + aliasDir, aliasesCanonicalized[i]);
            urlSessionTrackingPerModule.put(aliasDir, urlSessionTracking);
          } else {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000069",
                "Cannot get urlsessiontracking entry from configuration for [{0}] web application. The entry does not exist.",
                new Object[]{aliasesCanonicalized[i]}, null, null);
          }
        }
      } catch (Exception e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000070",
            "Cannot notify HTTP Provider for 'Lazy' application [{0}].", new Object[]{applicationName}, e, null, null);
      }

      boolean loadProperties = true;

      //Load deployed resources
      Configuration deployedResConfig = null;
      try {
        deployedResConfig = config.getSubConfiguration((WebContainerHelper.getAliasDirName(aliasesCanonicalized[i])).replace('/', '_').replace('\\', '_'));
        if (deployedResConfig != null && ((deployedResConfig.getConfigurationType() & Configuration.CONFIG_TYPE_PROPERTYSHEET) == 0)) {
          loadProperties = false;
        }
      } catch (NameNotFoundException e) {
        //$JL-EXC$ OK there are no deployed resources
        continue;
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_CONFIGURATION_FOR_APPLICATION,
            new Object[]{aliasesCanonicalized[i], applicationName}, e);
      }

      if (loadProperties) {
        try {
          Hashtable<String, Vector<String>> deployedResources = new Hashtable<String, Vector<String>>();

          PropertySheet propertySheet = deployedResConfig.getPropertySheetInterface();
          Properties props = propertySheet.getProperties();
          Enumeration enumeration = props.keys();
          while (enumeration.hasMoreElements()) {
            //Add info to the local cache
            String resourceName = (String) enumeration.nextElement();
            String resourceType = props.getProperty(resourceName);

            resourceName = resourceName.substring(2);
            Vector<String> types = (Vector<String>) deployedResources.get(resourceName);
            if (types == null) {
              deployedResources.put(resourceName, new Vector<String>(Arrays.asList(new String[]{resourceType})));
            } else {
              types.add(resourceType);
              deployedResources.put(resourceName, types);
            }
          }

          if (!deployedResources.isEmpty()) {
            resourcesPerModule.put(aliasesCanonicalized[i], deployedResources);
          }
        } catch (ConfigurationException e) {
          throw new WebDeploymentException(WebDeploymentException.CANNOT_LOAD_PROVIDED_RESOURCES, new Object[]{applicationName}, e);
        }
      }
    }

    //Load WCEs information
    boolean loadWceInDeploy = true;
    Configuration wceInDeployConfig = null;
    try {
      wceInDeployConfig = config.getSubConfiguration(Constants.WCE_IN_DEPLOY);
    } catch (NameNotFoundException e) {
      //$JL-EXC$ OK there are no wce information
      loadWceInDeploy = false;
    } catch (ConfigurationException e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_CONFIGURATION_FOR_APPLICATION,
          new Object[]{Constants.WCE_IN_DEPLOY, applicationName}, e);
    }

    if (loadWceInDeploy) {
      try {
        PropertySheet propertySheet = wceInDeployConfig.getPropertySheetInterface();
        Properties props = propertySheet.getProperties();
        Enumeration enumeration = props.keys();
        while (enumeration.hasMoreElements()) {
          //Add info to the local cache
          String moduleName = (String) enumeration.nextElement();
          String wceString = props.getProperty(moduleName);
          Pattern p = Pattern.compile(";");
          String[] items = p.split(wceString);
          Vector wces = (items != null && items.length > 0) ? new Vector(Arrays.asList(items)) : new Vector();
          wceInDeployPerModule.put(moduleName, wces);
        }
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_LOAD_WCE_IN_DEPLOY, new Object[]{applicationName}, e);
      }
    }

    appMetaData.setUrlSessionTrackingPerModule(urlSessionTrackingPerModule);
    appMetaData.setResourcesPerModule(resourcesPerModule);
    appMetaData.setWceInDeployPerModule(wceInDeployPerModule);
    return appMetaData;
  }

  /**
   *
   * @param applicationName
   * @param aliasesCanonicalized
   * @return mapping between web module's aliases and Vector objects containing names of the WCEs
   *    that have participated in the deployment of this web module
   * @throws DeploymentException
   */
  private void loadDeployedResourcesPerModule(String applicationName, String[] aliasesCanonicalized, AppMetaData appMetaData) {
    Hashtable<String, Hashtable<String, Vector<String>>> resourcesPerModule = new Hashtable<String, Hashtable<String, Vector<String>>>();
    for (int i = 0; i < aliasesCanonicalized.length; i++) {
      //Load deployed resources
      String aliasDir = (WebContainerHelper.getAliasDirName(aliasesCanonicalized[i])).replace('/', '_').replace('\\', '_');
      if (appMetaData.getResourcesPerModule().contains(aliasDir)) {
        Hashtable<String, Vector<String>> deployedResources = appMetaData.getResourcesPerModule().get(aliasDir);
        if (!deployedResources.isEmpty()) {
          resourcesPerModule.put(aliasesCanonicalized[i], deployedResources);
        }
      }
    }

    if (!resourcesPerModule.isEmpty()) {
      ((WebContainerProvider) iWebContainer).getResourcesPerApplication().put(applicationName, resourcesPerModule);
    }

  }

  private void loadWceInDeployPerModule(String applicationName, String[] aliasesCanonicalized, File[] aliasesRootDirs, AppMetaData appMetaData) {
    //Load WCEs information
    Hashtable wceInDeployPerModule = appMetaData.getWceInDeployPerModule();
    iWebContainer.appDeployedButNotStarted(applicationName, aliasesCanonicalized, aliasesRootDirs, wceInDeployPerModule);
  }//end of loadDeployedResourcesPerModule(String applicationName, String[] aliasesCanonicalized)

  public static void storeWebDDObject2DBase(WebDeploymentDescriptor webDesc, Configuration config, String aliasDir) throws DeploymentException {
    ObjectOutputStream objectOutputStream = null;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      objectOutputStream = new ObjectOutputStream(baos);
      objectOutputStream.writeObject(webDesc);
      ConfigurationUtils.addFileAsStream(config, Constants.WEB_DD_AS_OBJECT + aliasDir, new ByteArrayInputStream(baos.toByteArray()), aliasDir, true, true);
    } catch (IOException e) {
      throw new WebDeploymentException(WebCMigrationException.CANNOT_STORE_WEB_DD_OBJECT_2_DBASE, new Object[]{aliasDir}, e);
    } finally {
      if (objectOutputStream != null) {
        try {
          objectOutputStream.close();
        } catch (IOException e) {
          if (traceLocation.beWarning()) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000503",
								"Error while closing ObjectOutputStream for web.xml while deploying [{0}] web application.", new Object[]{aliasDir}, e, null, null);
          }
        }
      }
    }
  }//end of storeWebDDObject2DBase(WebDeploymentDescriptor webDesc, Configuration config, String aliasDir)

  public static WebDeploymentDescriptor loadWebDDObjectFromDBase(Configuration config, String aliasDir) throws DeploymentException {
    WebDeploymentDescriptor webDesc = null;
    InputStream inputStream = null;
    ObjectInputStream objectInputStream = null;

    String accountingTag = "loadWebDDObjectFromDBase(" + aliasDir + ")";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.beginMeasure(accountingTag, Configuration.class);
      }//ACCOUNTING.start - END

      if (config.existsFile(Constants.WEB_DD_AS_OBJECT + aliasDir)) {
        inputStream = ConfigurationUtils.getFile(config, Constants.WEB_DD_AS_OBJECT + aliasDir, aliasDir);
        objectInputStream = new ObjectInputStream(inputStream);
        webDesc = (WebDeploymentDescriptor) objectInputStream.readObject();
      } else {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_WEB_DD_OBJECT_FROM_DB,
          new Object[]{aliasDir});
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable ex) {
      throw new WebDeploymentException(WebDeploymentException.ERROR_IN_STARTING_APPLICATION,
        new Object[]{aliasDir}, ex);
    } finally {
      if (objectInputStream != null) {
        try {
          objectInputStream.close();
        } catch (IOException e) {
          if (traceLocation.beWarning()) {
        		LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000504",
        				"Cannot close ObjectInputStream.", e, null, null);
        	}
        }
      }
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          if (traceLocation.beWarning()) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000505",
								"Cannot close InputStream.", e, null, null);
          }
        }
      }
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END
    }
    return webDesc;
  }//end of loadWebDDObjectFromDBase(Configuration config, String aliasDir)

  //loads the stored to configuration servlet based ws end points for the given application
    public static ArrayList<String> loadWsEndPointsFromDBase(Configuration config, String aliasDir){
    	ArrayList<String> wsEndPoints = null;
        InputStream inputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
          if (config.existsFile(Constants.WS_END_POINTS + aliasDir)) {
            inputStream = ConfigurationUtils.getFile(config, Constants.WS_END_POINTS + aliasDir, aliasDir);
            objectInputStream = new ObjectInputStream(inputStream);
            wsEndPoints = (ArrayList<String>) objectInputStream.readObject();
          } else {
            	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000675",
						"Cannot load from the database the stored servlet based ws end points for web application [{0}].",new Object[]{aliasDir}, null, null);
          }
        } catch (Throwable ex) {
        	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError( "ASJ.web.000676",
					"Cannot load from the database the stored servlet based ws end points for web application [{0}].",new Object[]{aliasDir}, ex,  null, null);
        } finally {
          if (objectInputStream != null) {
            try {
              objectInputStream.close();
            } catch (IOException e) {
              if (traceLocation.beWarning()) {
            		LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000671",
            				"Error while closing ObjectOutputStream during deployment of [{0}] web application.", new Object[]{aliasDir}, e, null, null);
            	}
            }
          }
          if (inputStream != null) {
            try {
              inputStream.close();
            } catch (IOException e) {
              if (traceLocation.beWarning()) {
              	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000672",
              			"Error while closing InputStream during deployment of [{0}] web application.", new Object[]{aliasDir}, e, null, null);
    			}
            }
          }
        }
    return wsEndPoints;
  }



  /**
   * Merge web DD with the information from annotations before storing the object into the configuration
   *
   * @param webDesc
   * @param config
   * @param aliasCanonicalized
   * @param aliasDir
   */
  protected static void mergeDescriptors(WebDeploymentDescriptor webDesc, Configuration config, String aliasCanonicalized, String aliasDir) throws DeploymentException {
    WebAppMerger webMerger = new WebAppMerger(webDesc.getWebApp(), null);

    WebDeploymentDescriptor webDDOnlyAnnotations =
      XmlUtils.parseXml(ConfigurationUtils.getFile(config, Constants.ANNOTATIONS_DD + aliasDir, aliasDir),
        null, aliasCanonicalized, "web-annotations.xml", "", false);

    WebAppType.Choice1[] choices = webDDOnlyAnnotations.getWebApp().getChoiceGroup1();
    for (int j = 0; choices != null && j < choices.length; j++) {
      if (choices[j].isSetJndiEnvironmentRefsGroupGroup()) {
        WebAppType.Choice1.JndiEnvironmentRefsGroup jndiGroup = choices[j].getJndiEnvironmentRefsGroupGroup();

        EnvEntryType[] envEntries = jndiGroup.getEnvEntry();
        for (int k = 0; envEntries != null && k < envEntries.length; k++) {
          webMerger.addEnvEntry(new AnnotationContainer<EnvEntryType>(null, envEntries[k], envEntries[k].getEnvEntryName().get_value()));
        }

        EjbRefType[] ejbRefs = jndiGroup.getEjbRef();
        for (int k = 0; ejbRefs != null && k < ejbRefs.length; k++) {
          webMerger.addEJBRef(new AnnotationContainer<EjbRefType>(null, ejbRefs[k], ejbRefs[k].getEjbRefName().get_value()));
        }

        EjbLocalRefType[] ejbLocalRefs = jndiGroup.getEjbLocalRef();
        for (int k = 0; ejbLocalRefs != null && k < ejbLocalRefs.length; k++) {
          webMerger.addEJBLocalRef(new AnnotationContainer<EjbLocalRefType>(null, ejbLocalRefs[k], ejbLocalRefs[k].getEjbRefName().get_value()));
        }

        ServiceRefType[] serviceRefs = jndiGroup.getServiceRef();
        for (int k = 0; serviceRefs != null && k < serviceRefs.length; k++) {
          webMerger.addServiceRef(new AnnotationContainer<ServiceRefType>(null, serviceRefs[k], serviceRefs[k].getServiceRefName().get_value()));
        }

        ResourceRefType[] resourceRefs = jndiGroup.getResourceRef();
        for (int k = 0; resourceRefs != null && k < resourceRefs.length; k++) {
          webMerger.addResourceRef(new AnnotationContainer<ResourceRefType>(null, resourceRefs[k], resourceRefs[k].getResRefName().get_value()));
        }

        ResourceEnvRefType[] resourceEnvRefs = jndiGroup.getResourceEnvRef();
        for (int k = 0; resourceEnvRefs != null && k < resourceEnvRefs.length; k++) {
          webMerger.addResEnvRef(new AnnotationContainer<ResourceEnvRefType>(null, resourceEnvRefs[k], resourceEnvRefs[k].getResourceEnvRefName().get_value()));
        }

        MessageDestinationRefType[] msgDestinationRefs = jndiGroup.getMessageDestinationRef();
        for (int k = 0; msgDestinationRefs != null && k < msgDestinationRefs.length; k++) {
          webMerger.addMsgDstRef(new AnnotationContainer<MessageDestinationRefType>(null, msgDestinationRefs[k], msgDestinationRefs[k].getMessageDestinationRefName().get_value()));
        }

        PersistenceContextRefType[] persistenceContextRefs = jndiGroup.getPersistenceContextRef();
        for (int k = 0; persistenceContextRefs != null && k < persistenceContextRefs.length; k++) {
          webMerger.addPCRef(new AnnotationContainer<PersistenceContextRefType>(null, persistenceContextRefs[k], persistenceContextRefs[k].getPersistenceContextRefName().get_value()));
        }

        PersistenceUnitRefType[] persistenceUnitRefs = jndiGroup.getPersistenceUnitRef();
        for (int k = 0; persistenceUnitRefs != null && k < persistenceUnitRefs.length; k++) {
          webMerger.addPURef(new AnnotationContainer<PersistenceUnitRefType>(null, persistenceUnitRefs[k], persistenceUnitRefs[k].getPersistenceUnitName().get_value()));
        }

        LifecycleCallbackType[] postConstructs = jndiGroup.getPostConstruct();
        for (int k = 0; postConstructs != null && k < postConstructs.length; k++) {
          webMerger.addPostConstruct(new AnnotationContainer<LifecycleCallbackType>(null, postConstructs[k], postConstructs[k].getLifecycleCallbackClass().get_value()));
        }

        LifecycleCallbackType[] preDestroys = jndiGroup.getPreDestroy();
        for (int k = 0; preDestroys != null && k < preDestroys.length; k++) {
          webMerger.addPreDestroy(new AnnotationContainer<LifecycleCallbackType>(null, preDestroys[k], preDestroys[k].getLifecycleCallbackClass().get_value()));
        }
      }

      if (choices[j].isSetServlet()) {
        WebAppType.Choice1 servlet = findServletByClassName(webDesc.getWebApp(), choices[j].getServlet().getChoiceGroup1().getServletClass().get_value());
        webMerger.addRunAs(servlet, choices[j].getServlet().getRunAs());
      }

      if (choices[j].isSetSecurityRole()) {
        SecurityRoleType secRole = choices[j].getSecurityRole();
        webMerger.addRole(new AnnotationContainer<WebAppType.Choice1>(null, choices[j], secRole.getRoleName().get_value()));
      }
    }

    webMerger.merge();

    try {
      webDesc.setWebApp(webMerger.getMergedWebApp());
    } catch (Exception e) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_READ_XML_OR_ERROR_IN_XML_FOR_WEB_APPLICATION,
        new Object[]{"web.xml", aliasCanonicalized}, e);
    }
  }//end of mergeDescriptors(WebAppType webAppType, Configuration config, String aliasCanonicalized, String aliasDir)

  protected static void storeAppMetaDataObject2DBase(AppMetaData appMetaData, Configuration config, String appName) throws DeploymentException {
    ObjectOutputStream objectOutputStream = null;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      objectOutputStream = new ObjectOutputStream(baos);
      objectOutputStream.writeObject(appMetaData);
      ConfigurationUtils.addFileAsStream(config, Constants.APP_META_DATA_AS_OBJECT, new ByteArrayInputStream(baos.toByteArray()), appName, true, true);
    } catch (IOException e) {
      throw new WebDeploymentException(WebCMigrationException.CANNOT_STORE_APP_META_DATA_OBJECT_2_DBASE, new Object[]{appName}, e);
    } finally {
      if (objectOutputStream != null) {
        try {
          objectOutputStream.close();
        } catch (IOException e) {
          if (traceLocation.beWarning()) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000506",
								"Error while closing ObjectOutputStream for web.xml while deploying [{0}] web application.", new Object[]{appName}, e, null, null);
          }
        }
      }
    }
  }//end of storeAppMetaDataObject2DBase(AppMetaData appMetaData, Configuration config, String appName)

  public static AppMetaData loadAppMetaDataObjectFromDBase(Configuration config, String appName) throws DeploymentException {
    //WebDeploymentDescriptor webDesc = null;
    AppMetaData appMetaData = null;
    InputStream inputStream = null;
    FileInputStream fileInputStream = null;
    ObjectInputStream objectInputStream = null;
    boolean beWarning = traceLocation.beWarning();
    try {
      if (config.existsFile(Constants.APP_META_DATA_AS_OBJECT)) {
        inputStream = ConfigurationUtils.getFile(config, Constants.APP_META_DATA_AS_OBJECT, appName);
        objectInputStream = new ObjectInputStream(inputStream);
        appMetaData = (AppMetaData) objectInputStream.readObject();
      } else {
				if (beWarning) {
					LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000507",
							"Cannot get AppMetaData object from configuration for [{0}] web application. It is missing.", new Object[]{appName}, null, null);
				}
        appMetaData = null;
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable ex) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_APP_META_DATA_OBJECT_FROM_DB,
          new Object[]{appName}, ex);
    } finally {
      if (objectInputStream != null) {
        try {
          objectInputStream.close();
        } catch (IOException e) {
          if (beWarning) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning( "ASJ.web.000508",
								"Cannot close ObjectInputStream.", e, null, null);
          }
        }
      }
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          if (beWarning) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000509",
								"Cannot close InputStream.", e, null, null);
          }
        }
      }
      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          if (beWarning) {
          	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000510",
								"Cannot close FileInputStream.", e, null, null);
          }
        }
      }
    }

    return appMetaData;
  }//end of loadAppMetaDataObjectFromDBase(Configuration config, String appName)

  private static WebAppType.Choice1 findServletByClassName(WebAppType webApp, String servletClassName) {
    WebAppType.Choice1[] choices = webApp.getChoiceGroup1();
    for (int i = 0; i < choices.length; i++) {
      if (choices[i].isSetServlet()) {
        FullyQualifiedClassType classType = choices[i].getServlet().getChoiceGroup1().getServletClass();
        if (classType != null && servletClassName.equals(classType.get_value())) {
          return choices[i];
        }
      }
    }
    return null;
  }//end of findServletByClassName(WebAppType webApp, String servletClassName)

}//end of class
