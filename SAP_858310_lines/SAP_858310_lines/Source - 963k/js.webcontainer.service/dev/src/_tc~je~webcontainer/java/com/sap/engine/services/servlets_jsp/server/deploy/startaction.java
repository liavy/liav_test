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

import static com.sap.engine.services.deploy.container.op.IOpConstants.JAVA_VERSION;
import static com.sap.engine.services.servlets_jsp.server.LogContext.CATEGORY_DEPLOY;
import static com.sap.engine.services.servlets_jsp.server.ServiceContext.getServiceContext;
import static com.sap.engine.services.servlets_jsp.server.deploy.util.Constants.*;
import static com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException.*;
import static com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException.*;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.frame.core.configuration.addons.PropertySheet;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WCEAppThreadInitializer;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.ResourceReference;
import com.sap.engine.system.ThreadWrapper;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;

import javax.servlet.ServletContext;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public final class StartAction extends ActionBase {
  private static final Location traceLocation = LogContext.getLocationDeploy();
  private static Location currentLocation = Location.getLocation(StartAction.class);

  /**
   * @param iWebContainer
   * @param containerInfo
   * @param servletsAndJsp
   * @param runtimeInterface
   */
  public StartAction(IWebContainer iWebContainer, ContainerInfo containerInfo, WebContainerHelper servletsAndJsp,
                     WebContainerInterface runtimeInterface) {
    super(iWebContainer, containerInfo, servletsAndJsp, runtimeInterface);
  }//end of constructor

  /**
   * @param applicationName
   * @param appConfig
   * @throws DeploymentException
   * @throws WarningException
   */
  public void prepareStart(String applicationName, Configuration appConfig) throws DeploymentException, WarningException {
    Vector warnings = new Vector();

    ApplicationContext[] applicationContexts = null;
    
    String accountingTag0 = "prepareStart/getSubConfiguration (" + applicationName + ")";
    Configuration sevlet_jspConfig = null;
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag0, Configuration.class);
      }//ACCOUNTING.start - END
     
      sevlet_jspConfig = ConfigurationUtils.getSubConfiguration(appConfig, containerInfo.getName(), applicationName, true);
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag0);
      }//ACCOUNTING.end - END
    }
    
    try {
      ThreadWrapper.pushTask("Starting application [" + applicationName + "] in WebContainer.", ThreadWrapper.TS_PROCESSING);
      String[] aliasesCanonicalized = getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
      if (aliasesCanonicalized != null && aliasesCanonicalized.length > 0) {
        ServletContext[] servletContexts = new ServletContext[aliasesCanonicalized.length];
        applicationContexts = new ApplicationContext[aliasesCanonicalized.length];

        String[] filesForPrivateCL = null;
        Vector privateResourceReferences = null;
        ClassLoader publicClassloader = null;
        try {
          if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
            Accounting.beginMeasure("prepareStart/registerPrivateClassLoader", StartAction.class);
          }//ACCOUNTING.start - END
                  
          filesForPrivateCL = getFilesForPrivateCL(sevlet_jspConfig, applicationName);
          privateResourceReferences = getPrivateResourceReferences(sevlet_jspConfig, applicationName);
          publicClassloader = getServiceContext().getLoadContext().getClassLoader(applicationName);
        } finally {
          if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
            Accounting.endMeasure("prepareStart/registerPrivateClassLoader");
          }//ACCOUNTING.end - END
        }

        for (int i = 0; i < aliasesCanonicalized.length; i++) {
          String aliasCanonicalized = aliasesCanonicalized[i]; //canonicalized alias
          String aliasDir = WebContainerHelper.getAliasDirName(aliasCanonicalized);

          String accountingTag1 = "prepareStart/createApplicationContext(" + aliasCanonicalized + ")";
          ApplicationContext scf = null;
          try {
            if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
              Accounting.beginMeasure(accountingTag1, StartAction.class);
            }//ACCOUNTING.start - END
          
            scf = createContext(applicationName, aliasCanonicalized, sevlet_jspConfig, warnings, aliasDir);
            scf.setFilesForPrivateCL(filesForPrivateCL);
            scf.setPrivateResourceReferences(privateResourceReferences);
          } finally {
            if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
              Accounting.endMeasure(accountingTag1);
            }//ACCOUNTING.end - END
          }
          
          if (traceLocation.beDebug()) {
            traceLocation.debugT("ApplicationContext for web application [" + aliasCanonicalized + "] is created.");
          }
          applicationContexts[i] = scf;
          servletContexts[i] = scf.getServletContext();
          if (scf.getWarnings() != null && scf.getWarnings().size() != 0) {
            warnings.addAll(scf.getWarnings());
          }
          // Context is available but not ready to serve requests.
          
          getServiceContext().getDeployContext().applicationStarted(new MessageBytes(aliasCanonicalized.getBytes()), scf);
          
          //Init WebModule contexts          
          try {
						((WebContainerProvider) iWebContainer).initWebModuleContext(aliasCanonicalized, servletContexts[i], applicationName, publicClassloader);
					} catch (RuntimeException e) {
					  warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
					    CANNOT_INITIALIZE_CONTEXT, new Object[]{applicationName, e.toString()}));
          }
					
          Vector initAppWarnings = initWebAppComponents(scf);
          
          if (initAppWarnings != null && initAppWarnings.size() != 0) {
            warnings.addAll(initAppWarnings);
          }
          
          boolean isURLSessionTrackingDisabled = getServiceContext().getHttpProvider().getHttpProperties().isURLSessionTrackingDisabled();
          if (isURLSessionTrackingDisabled && scf.getWebApplicationConfiguration().isURLSessionTracking()) {
        	  String csnComponent = LoggingUtilities.getCsnComponentByDCName(applicationName);
          	WebDeploymentException wde = new WebDeploymentException(CANNOT_USE_URL_SESSION_TRACKING,
              new Object[]{applicationName, csnComponent, "icm/HTTP/ASJava/disable_url_session_tracking", aliasCanonicalized});
          	wde.setDcName(applicationName);
          	wde.setMessageID("com.sap.ASJ.web.000599");
            LogContext.getCategory(CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000103",
              "Web application [{0}] cannot be started due to url-session-tracking misconfiguration. Currently " +
              "the web application is configured to use URL session tracking only (via the url-session-tracking element in the additional web deployment descriptor) " +
              "in contrast to  the instance (global) property [icm/HTTP/ASJava/disable_url_session_tracking = true] which disables it.",
              new Object[]{aliasCanonicalized}, wde, applicationName, csnComponent);
        	  throw wde;
          }
        }// for aliasesCanonicalized

        //Check whether the web application is pure web application or not
        //If it is pure web application we will not sent events to the WCE providers
        //This is an optimization - do not start application thread
        if (!isPureWebApplication(aliasesCanonicalized, applicationName)) {
          initWCEComponents(iWebContainer, applicationName, aliasesCanonicalized, servletContexts, publicClassloader);
        }
        
        String accountingTag3 = "prepareStart/HTTP service.clearCache and startAliases of (" + applicationName + ")";
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(accountingTag3, HttpProvider.class);
        }//ACCOUNTING.start - END
        
        for (int i = 0; i < aliasesCanonicalized.length; i++) {
          //context is ready to start requests
          applicationContexts[i].setStarted(true);
          try {
            getServiceContext().getHttpProvider().clearCacheByAlias(aliasesCanonicalized[i]);
          } catch (Exception e) {
          	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          	CANNOT_CLEAR_HTTP_CACHE, new Object[]{aliasesCanonicalized[i], e.toString()}));
          }

          getServiceContext().getHttpProvider().startApplicationAlias(aliasesCanonicalized[i], applicationContexts[i].getWebApplicationRootDir());
        }
        
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure(accountingTag3);
        }//ACCOUNTING.end - END
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      for (int i = 0; applicationContexts != null && i < applicationContexts.length; i++) {
        if (applicationContexts[i] != null) {
          destroyWebAppComponents(applicationContexts[i]);
        }
      }

      throw new WebDeploymentException(ERROR_IN_STARTING_APPLICATION, new Object[]{applicationName}, e);
    } finally {
    	ThreadWrapper.popTask();
    }

    makeWarningException(warnings);
  }//end of prepareStart(String applicationName, Configuration appConfig)

  /**
   * @param applicationName
   * @throws WarningException
   */
  public void commitStart(String applicationName) throws WarningException {
    try {
      runtimeInterface.appStarted(applicationName);
    } catch (RemoteException e) {
      throw new WebWarningException(CANNOT_NOTIFY_RUNTIME_INTERFACE_FOR_CHANGING_STATUS_OF_APPLICATION_ERROR_IS,
        new Object[]{applicationName, e.toString()});
    } catch (RuntimeException e) {
      throw new WebWarningException(CANNOT_NOTIFY_RUNTIME_INTERFACE_FOR_CHANGING_STATUS_OF_APPLICATION_ERROR_IS,
        new Object[]{applicationName, e.toString()});
    }
  }//end of commitStart(String applicationName)

  /**
   * Creates a new context for each new deployed application.
   *
   * @param applicationName    name of the application
   * @param aliasCanonicalized alias for context of the application
   * @param appConfig          Configuration
   * @param warnings
   * @param aliasDir           the name of alias directory may be different from the alias
   * @return reference to the created context
   * @throws DeploymentException
   */
  private ApplicationContext createContext(String applicationName, String aliasCanonicalized, Configuration appConfig, Vector warnings, String aliasDir) throws DeploymentException {
    boolean defaultWebApp = aliasCanonicalized.equals("/");
    MessageBytes aliasNameBytes = null;
    if (defaultWebApp) {
      aliasNameBytes = defaultAliasMB;
    } else {
      aliasNameBytes = new MessageBytes(aliasCanonicalized.getBytes());
    }

    ApplicationContext scf = getServiceContext().getDeployContext().getStartedWebApplicationContext(aliasNameBytes);
    if (scf != null && !defaultWebApp) {    
      // if it is default application there can be another web application; 
      // allow creating another default context and in case the existing has 
      // higher prio (customer default application), scf.init() will throw
      // deployment exception 
      throw new WebDeploymentException(APP_CONTEXT_ALREADY_EXISTS, new Object[]{aliasCanonicalized});
    }
    scf = new ApplicationContext(applicationName, aliasCanonicalized, defaultWebApp, aliasDir);

    String failoverDB = (String) ConfigurationUtils.getConfigEntry(appConfig, FAIL_OVER, aliasCanonicalized);
    scf.setFailOver(failoverDB);

    Properties additionalAppProps = getServiceContext().getDeployContext().getDeployCommunicator().getAdditionalAppProps(applicationName);
    if (additionalAppProps != null) {
      String javaVersionForCompilation = (String) additionalAppProps.get(JAVA_VERSION);
      scf.setJavaVersionForCompilation(javaVersionForCompilation);
    }

    warnings = scf.init(getServiceContext().getSecurityContext(), webContainerHelper.getSessionContext(), appConfig);
    return scf;
  }//end of createContext(String applicationName, String aliasCanonicalized, Configuration appConfig, Vector warnings, String aliasDir)

  private Vector initWebAppComponents(ApplicationContext scf) throws DeploymentException {
    String accountingTag = "prepareStart/initWebAppComponents(" + scf.getAliasName() + ")";
      String aliasName = scf.getAliasName();

      ApplicationThreadInitializer initializer = new ApplicationThreadInitializer(scf.getWebApplicationConfiguration(),
        getServiceContext().getServiceLoader(), scf, scf.getWebApplicationConfiguration().getAllServlets());
      boolean beDebug = traceLocation.beDebug();
      if (beDebug) {
      	traceLocation.debugT("ApplicationThreadInitializer for web application [" + aliasName + "] is initialized.");
      }

      try {
        ThreadWrapper.pushSubtask("Initialization of web components and app context of web app [" + 
          aliasName + "] started in new app thread and waiting for it to finish.", ThreadWrapper.TS_PROCESSING);

        if (beDebug) {
          traceLocation.debugT(
            "getServiceContext().getDeployContext().getDeployCommunicator().execute(initializer) for web application [" + aliasName + "].");
        }

        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(accountingTag, DeployCommunicator.class);
        }//ACCOUNTING.start - END
        
        getServiceContext().getDeployContext().getDeployCommunicator().execute(initializer);
      } catch (InterruptedException e) {
        throw new WebDeploymentException(ERROR_IN_STARTING_OF_WEB_APPLICATION,
          new Object[]{aliasName}, e);
      } finally {
        ThreadWrapper.popSubtask();
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure(accountingTag);
        }//ACCOUNTING.end - END
      }

      if (initializer.getException() != null) {
          throw new WebDeploymentException(ERROR_IN_STARTING_OF_WEB_APPLICATION,
          new Object[]{aliasName}, initializer.getException());
      }

      return initializer.getWarnings();
  }//end of initWebAppComponents(ApplicationContext scf)

  private void initWCEComponents(IWebContainer iWebContainer, String applicationName, String[] aliases, ServletContext[] servletContexts, ClassLoader publicClassloader) throws DeploymentException {
    String accountingTag = "prepareStart/initWCEComponents(" + applicationName + ")";
    try {
      WCEAppThreadInitializer wceAppThreadInitializer = new WCEAppThreadInitializer(iWebContainer, applicationName, aliases, servletContexts, publicClassloader);

      try {
        ThreadWrapper.pushSubtask("Initialization of WCE components of app [" + applicationName
          + "] started in new app thread and waiting for it to finish.", ThreadWrapper.TS_PROCESSING);

        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(accountingTag, DeployCommunicator.class);
        }//ACCOUNTING.start - END

        getServiceContext().getDeployContext().getDeployCommunicator().execute(wceAppThreadInitializer);
      } catch (InterruptedException e) {
        throw new WebDeploymentException(THREAD_INTERRUPTED_WHILE_STARTING_WCE_COMPONENTS, new Object[]{applicationName}, e);
      } finally {
        ThreadWrapper.popSubtask();
      }

      if (wceAppThreadInitializer.getExceptionDuringInit() != null) {
        throw new WebDeploymentException(ERROR_OCCURRED_STARTING_WCE_COMPONENTS, 
          new Object[]{applicationName}, wceAppThreadInitializer.getExceptionDuringInit());
      }
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END
    }
  }//end of initWCEComponents(String applicationName, String[] aliases, ServletContext[] servletContexts, ClassLoader publicClassloader, ClassLoader privateClassloader)

  private String[] getFilesForPrivateCL(Configuration config, String applicationName) throws DeploymentException {
    String accountingTag = "prepareStart/getFilesForPrivateCL(" + applicationName + ")";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(accountingTag, StartAction.class);
      } //ACCOUNTING.start - END
      
      ArrayList<String> result = new ArrayList<String>();
  
      Configuration filesForCLConfig = null;
      try {
        filesForCLConfig = config.getSubConfiguration(FILES_FOR_PRIVATE_CL);
      } catch (NameNotFoundException e) {
        //$JL-EXC$ OK there are no files for private class loader
        return null;
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(CANNOT_GET_CONFIGURATION_FOR_APPLICATION,
          new Object[]{FILES_FOR_PRIVATE_CL, applicationName}, e);
      }
  
      try {
        PropertySheet propertySheet = filesForCLConfig.getPropertySheetInterface();
        Properties props = propertySheet.getProperties();
        ArrayList relativeNames = new ArrayList(props.values());
        // restores absolute file names
        result = getAbsolutePaths(applicationName, relativeNames);
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(CANNOT_READ_FILES_FOR_PRIVATE_CL, new Object[]{applicationName}, e);
      }
  
      return (String[]) result.toArray(new String[result.size()]);
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) {
        Accounting.endMeasure(accountingTag);
      }
    }//ACCOUNTING.end - END
  }//end of getFilesForPrivateCL(Configuration config, String applicationName)

  private Vector getPrivateResourceReferences(Configuration config, String applicationName) throws DeploymentException {
    String accountingTag = "prepareStart/getPrivateResourceReferences(" + applicationName + ")";
    try { //ACCOUNTING.start - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.beginMeasure(accountingTag, StartAction.class);
      } //ACCOUNTING.start - END  
      
      Vector<ResourceReference> result = new Vector<ResourceReference>();
  
      Configuration resRefsConfig = null;
      try {
        resRefsConfig = config.getSubConfiguration(PRIVATE_RESOURCE_REFERENCES);
      } catch (NameNotFoundException e) {
        //$JL-EXC$ OK there are no private resource references
        return null;
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(CANNOT_GET_CONFIGURATION_FOR_APPLICATION,
          new Object[]{FILES_FOR_PRIVATE_CL, applicationName}, e);
      }
  
      try {
        PropertySheet propertySheet = resRefsConfig.getPropertySheetInterface();
        Properties props = propertySheet.getProperties();
        for (int i = 0; i < props.size() / 3; i++) {
          String resourceName = (String) props.getProperty("resource_name_" + i);
          String resourceType = (String) props.getProperty("resource_type_" + i);
          String referenceType = (String) props.getProperty("reference_type_" + i);
          result.add(new ResourceReference(resourceName, resourceType, referenceType));
        }
      } catch (ConfigurationException e) {
        throw new WebDeploymentException(CANNOT_READ_PRIVATE_RES_REF, new Object[]{applicationName}, e);
      }
  
      return result;
    } finally {//ACCOUNTING.end - BEGIN
      if (Accounting.isEnabled()) { 
        Accounting.endMeasure(accountingTag);
      } //ACCOUNTING.end - END
    }
  }//end of getPrivateResourceReferences(Configuration config, String applicationName)

  private ArrayList<String> getAbsolutePaths(String applicationName, ArrayList relativePaths) {
    ArrayList<String> absolutePaths = new ArrayList<String>();
    String appDir = WebContainerHelper.getDeployTempDir(applicationName);
    if (relativePaths.size() > 0) {
      Iterator iterator = relativePaths.iterator();
      while (iterator.hasNext()) {
        String file = (String) iterator.next();
        //ensures that file path is correct according to the OS where engine runs
        //valid case when we have heterogeneous cluster
        file = file.replace('\\', File.separatorChar).replace('/', File.separatorChar);
        if (!file.startsWith(appDir)) {
          file = appDir + file;
        }
        absolutePaths.add(file);
      }
    }
    return absolutePaths;
  }//end of getAbsolutePaths(String applicationName, ArrayList relativePaths)
  
  private boolean isPureWebApplication(String[] aliasesCanonicalized, String applicationName) {
    WebModule webModule;
    for (String alias : aliasesCanonicalized) {
      webModule = (WebModule) (((WebContainerProvider) iWebContainer).getDeployedAppl(alias, applicationName));
      if (webModule != null && webModule.getWceInDeploy().size() > 0) {
        return false;
      }
    }
    return true;
  }//end of isPureWebApplication(String[] aliasesCanonicalized)
  
}//end of class
