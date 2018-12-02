/*
 * Copyright (c) 2004-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.impl;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEApplicationNotDeployedException;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEApplicationNotStartedException;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEDeploymentException;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalStateException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.ApplicationNotDeployedException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.ApplicationNotStartedException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext;

/**
 * @author Maria Jurova
 * @author Violeta Georgieva
 * @version 7.10
 */
public class WebContainerExtensionContext implements IWebContainerExtensionContext {

  private WebContainerProvider webContainerProvider = null;
  private ConcurrentHashMap<String, WebModule> myDeployedWebApplications = new ConcurrentHashMap<String, WebModule>();
  private ApplicationManager applicationManager = null;
  private String webContainerExtensionProviderName = null;

  /**
   * Constructor
   *
   * @param webContainerProvider
   * @param webContainerExtensionProviderName
   */
  public WebContainerExtensionContext(WebContainerProvider webContainerProvider, String webContainerExtensionProviderName) {
    this.webContainerProvider = webContainerProvider;
    this.webContainerExtensionProviderName = webContainerExtensionProviderName;
    this.applicationManager = new ApplicationManager(this.webContainerProvider, this.webContainerExtensionProviderName);
  }//end of constructor

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext#getAllWebModules()
   */
  public String[] getAllWebModules() {
    HashSet<String> allWebModuleNames = new HashSet<String>(webContainerProvider.getDeployedWebApplications().keySet());
    return (String[]) allWebModuleNames.toArray(new String[allWebModuleNames.size()]);
  }//end of getAllWebModules()

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext#getMyWebModules()
   */
  public String[] getMyWebModules() {
    HashSet<String> myWebModuleNames = new HashSet<String>(myDeployedWebApplications.keySet());
    return (String[]) myWebModuleNames.toArray(new String[myWebModuleNames.size()]);
  }//end of getMyWebModules()

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext#getWebModules(java.lang.String)
   */
  public String[] getWebModules(String applicationName) {
    Vector<String> webModuleNames = new Vector<String>();

    int id = applicationName.indexOf("/");
    if (id != -1) { //It is a full application name including vendor
	   	// Note that during update the following will return the old aliases and not the new ones:
	   	// ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    	// This is by design this method is for administrations only.
    	// During deploy/update the WCE provider receives the needed IWebModule.
      // PLEASE DO NOT TOUCH THIS IS AN OPTIMIZATION
    	String[] aliasesCannonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
      if (aliasesCannonicalized != null) {
        return aliasesCannonicalized;
      }
    } else { //It is not a full application name, i.e. does not include vendor
      String vendor = null;

      HashSet<Vector<WebModule>> allWebModules = new HashSet<Vector<WebModule>>(webContainerProvider.getDeployedWebApplications().values());
      Iterator<Vector<WebModule>> iterator = allWebModules.iterator();
      
      while (iterator.hasNext()) {
        Vector<WebModule> modules = (Vector<WebModule>) iterator.next();
        if (modules == null) {
          continue;
        }
        for (IWebModule webModule : modules) {        
          if (webModule != null && webModule.getApplicationName().equals(applicationName)) {
            if (vendor == null) {
              vendor = webModule.getVendor();
            }

            if (vendor.equals(webModule.getVendor())) {
              webModuleNames.add(webModule.getModuleName());
            } else {
              throw new WebIllegalStateException(WebIllegalStateException.APPLICATIONS_NAMES_EQUALS_BUT_VENDORS_NOT, new Object[]{applicationName});
            }
          }
        }
      }
    }

    return (String[]) webModuleNames.toArray(new String[webModuleNames.size()]);
  }//end of getWebModules(String applicationName)

  /**
   * Gets the web modules names, which belong to this web container extension, corresponding to the application name.
   * If the application name is the full application name i.e. "vendor"/"application name"
   * then the web modules names corresponding to this application name and vendor will be returned.
   * If only application name (without vendor information) is specified and
   * there are deployed applications with one and the same application name,
   * but with different vendors then IllegalStateException will be thrown,
   * else the web modules names corresponding to this application name will be returned.
   * NOTE: 
   * <br><ul><li>This method always returns the committed data.
   * <li>If an exception occurs during retrieving the needed information, 
   * an error message will be traced in the default traces only. 
   * </ul>
   *
   * @param applicationName the name of the application that contains these web modules.
   * @return web modules names, which belong to this web container extension, corresponding to the application name.
   */
  public String[] getMyWebModules(String applicationName) {
    Vector<String> myWebModuleNames = new Vector<String>();

    int id = applicationName.indexOf("/");
    if (id != -1) {//It is a full application name including vendor
	   	// Note that during update the following will return the old aliases and not the new ones:
	   	// ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    	// This is by design this method is for administrations only.
    	// During deploy/update the WCE provider receives the needed IWebModule.
      // PLEASE DO NOT TOUCH THIS IS AN OPTIMIZATION
      String[] aliasesCannonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
      for (int i = 0; aliasesCannonicalized != null && i < aliasesCannonicalized.length; i++) {
        if (myDeployedWebApplications.containsKey(aliasesCannonicalized[i])) {
          myWebModuleNames.add(aliasesCannonicalized[i]);
        }
      }
    } else { //It is not a full application name, i.e. does not include vendor
    	String vendor = null;

    	HashSet<WebModule> allWebModules = new HashSet<WebModule>(myDeployedWebApplications.values());
      Iterator<WebModule> iterator = allWebModules.iterator();

      while (iterator.hasNext()) {
        IWebModule webModule = (IWebModule) iterator.next();
        if (webModule != null && webModule.getApplicationName().equals(applicationName)) {
          if (vendor == null) {
            vendor = webModule.getVendor();
          }

          if (vendor.equals(webModule.getVendor())) {
            myWebModuleNames.add(webModule.getModuleName());
          } else {
            throw new WebIllegalStateException(WebIllegalStateException.APPLICATIONS_NAMES_EQUALS_BUT_VENDORS_NOT, new Object[]{applicationName});
          }
        }
      }
    }

    return (String[]) myWebModuleNames.toArray(new String[myWebModuleNames.size()]);
  }//end of getMyWebModules(String applicationName)

  /**
   * Gets the web modules names, which belong to this web container extension, corresponding to the application name.
   * If the application name is the full application name i.e. "vendor"/"application name"
   * then the web modules names corresponding to this application name and vendor will be returned.
   * If only application name (without vendor information) is specified and
   * there are deployed applications with one and the same application name,
   * but with different vendors then IllegalStateException will be thrown,
   * else the web modules names corresponding to this application name will be returned.
   * NOTE: 
   * <br><ul><li>This method always returns the committed data.
   * <li>If an exception occurs during retrieving the needed information and <code>suppressErrors</code> is false
   * then WebContainerExtensionDeploymentException will be thrown.
   * <li>If an exception occurs during retrieving the needed information and <code>suppressErrors</code> is true
   * then an error message will be traced in the default traces only. 
   * </ul>
   *
   * @param applicationName the name of the application that contains these web modules.
   * @param suppressErrors if false and an exception occurs during retrieving the needed information, an exception 
   * will be thrown, else when true and an exception occurs during retrieving the needed information then only an error message
   * will be traced in the default traces.
   * @return Web modules names, which belong to this web container extension, corresponding to the application name.
   * @throws WebContainerExtensionDeploymentException if an exception occurs during retrieving the needed information 
   * and <code>suppressErrors</code> is false.
   */
  public String[] getMyWebModules(String applicationName, boolean suppressErrors) throws WebContainerExtensionDeploymentException {
    if (suppressErrors) {
      return getMyWebModules(applicationName);
    } else {
      Vector<String> myWebModuleNames = new Vector<String>();

      int id = applicationName.indexOf("/");
      if (id != -1) {//It is a full application name including vendor
        // Note that during update the following will return the old aliases and not the new ones:
        // ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
        // This is by design this method is for administrations only.
        // During deploy/update the WCE provider receives the needed IWebModule.
        // PLEASE DO NOT TOUCH THIS IS AN OPTIMIZATION
        String[] aliasesCannonicalized = null;
        try {
          aliasesCannonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalizedIfExists(applicationName);
        } catch (DeploymentException e) {
          if (LogContext.getLocationWebContainerProvider().beDebug()) {
            LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceDebug(
              "Cannot get aliases for [" + applicationName + "] application, because this application is not deployed.", e, "");
          }
          throw new WCEDeploymentException(WCEDeploymentException.CANNOT_RETRIEVE_INFO_FOR_APPLICATION, new Object[]{applicationName}, e);
        }
        for (int i = 0; aliasesCannonicalized != null && i < aliasesCannonicalized.length; i++) {
          if (myDeployedWebApplications.containsKey(aliasesCannonicalized[i])) {
            myWebModuleNames.add(aliasesCannonicalized[i]);
          }
        }
      } else { //It is not a full application name, i.e. does not include vendor
        String vendor = null;

        HashSet<WebModule> allWebModules = new HashSet<WebModule>(myDeployedWebApplications.values());
        Iterator<WebModule> iterator = allWebModules.iterator();

        while (iterator.hasNext()) {
          IWebModule webModule = (IWebModule) iterator.next();
          if (webModule != null && webModule.getApplicationName().equals(applicationName)) {
            if (vendor == null) {
              vendor = webModule.getVendor();
            }

            if (vendor.equals(webModule.getVendor())) {
              myWebModuleNames.add(webModule.getModuleName());
            } else {
              throw new WebIllegalStateException(WebIllegalStateException.APPLICATIONS_NAMES_EQUALS_BUT_VENDORS_NOT, new Object[]{applicationName});
            }
          }
        }
      }

      return (String[]) myWebModuleNames.toArray(new String[myWebModuleNames.size()]);
    }
  }//end of getMyWebModules(String applicationName, boolean strictValidation)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext#getWebModule(java.lang.String)
   */
  public IWebModule getWebModule(String moduleName) {
    // returns the module with higher prio for that alias
    return (IWebModule) webContainerProvider.getDeployedAppl(moduleName);
  }//end of getWebModule(String moduleName)
  
  public IWebModule getWebModule(String resourceName, String resourceType) {
    String applicationName = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getApplicationProvidingResource(resourceName, resourceType);
    if (applicationName != null) {
      Hashtable<String, Hashtable<String, Vector<String>>> resourcesPerModule = 
        (Hashtable<String, Hashtable<String, Vector<String>>>) webContainerProvider.getResourcesPerApplication().get(applicationName);

      if (resourcesPerModule == null) {
        try {
          resourcesPerModule = webContainerProvider.loadResourcesPerApplication(applicationName);
        } catch (Exception e) {
          if (LogContext.getLocationWebContainerProvider().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceWarning("ASJ.web.000755", 
              "Web module [{0}] is not found. Application [{1}] is not deployed. Null will be returned.",
              new Object[]{resourceName, applicationName}, null, null);
          }
          return null;
        }
      }
      Enumeration<String> enumeration = resourcesPerModule.keys();
      while (enumeration.hasMoreElements()) {
        String moduleName = (String) enumeration.nextElement();
        Hashtable<String, Vector<String>> deployedResources = (Hashtable<String, Vector<String>>) resourcesPerModule.get(moduleName);
        if (deployedResources.containsKey(resourceName)) {
          Vector<String> resTypes = (Vector<String>) deployedResources.get(resourceName);
          if (resTypes.contains(resourceType)) {
            return getWebModule(moduleName);
          }
        }
      }
      if (LogContext.getLocationWebContainerProvider().beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceWarning("ASJ.web.000754", 
          "Web module [{0}] is not found. Application [{1}] is not deployed. Null will be returned.",
          new Object[]{resourceName, applicationName}, null, null);
      }
      return null;
    }
    if (LogContext.getLocationWebContainerProvider().beWarning()) {
      LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceWarning("ASJ.web.000753", 
        "Resource with name [{0}] of type [{1}] is not provided by any deployed application. Null will be returned. " +
        "Cause: Either the application providing such resource is not deployed or wrong resource name and/or type is used. " +
        "Details: Verify whether the WCE Provider requesting this resource expects it to exist, " +
        "i.e. the resource is part of some application or should have been registered in Web Container by the WCE Provider during some application deploy time.", 
        new Object[]{resourceName, resourceType}, null, null);
    }
    return null;
  }//end of getWebModule(String resourceName, String resourceType)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext#getWebModuleContext(java.lang.String)
   */
  public IWebModuleContext getWebModuleContext(String moduleName) throws ApplicationNotDeployedException, ApplicationNotStartedException {
    WebModule webModule = (WebModule) getWebModule(moduleName);
    if (webModule == null) {
      throw new WCEApplicationNotDeployedException(WCEApplicationNotDeployedException.WEBMODULE_IS_NOT_FOUND, new Object[]{moduleName, ""});
    }

    IWebModuleContext webModuleContext = (IWebModuleContext) webContainerProvider.getStartedWebApplications().get(moduleName);
    if (webModuleContext != null) {
      //If during onStart() somebody makes getWebModuleContext() it is already created and can be returned in other case we will try to start it during starting!
      //If during onStop() somebody makes getWebModuleContext() it is not still destroyed and can be returned in other case during stop we will try to start it again!
      return webModuleContext;
    } else {
      try {
        //Checks if the WCE is in unregistering phase in which case throws ApplicationNotStartedException:
        applicationManager.analyseAppStatusMode(webModule, true);
      } catch (RemoteException e) {
        throw new WCEApplicationNotStartedException(WCEApplicationNotStartedException.CANNOT_ANALYSE_STARTUP_MODE, new Object[]{webModule.getWholeApplicationName()}, e);
      }
    }

    return (IWebModuleContext) webContainerProvider.getStartedWebApplications().get(moduleName);
  }//end of getWebModuleContext(String moduleName)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext#getWebModuleContext(String, String)
   */
  public IWebModuleContext getWebModuleContext(String resourceName, String resourceType) throws ApplicationNotDeployedException, ApplicationNotStartedException {
    String applicationName = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getApplicationProvidingResource(resourceName, resourceType);
    if (applicationName != null) {
      Hashtable<String, Hashtable<String, Vector<String>>> resourcesPerModule = 
      	(Hashtable<String, Hashtable<String, Vector<String>>>) webContainerProvider.getResourcesPerApplication().get(applicationName);

      if (resourcesPerModule == null) {
        try {
          resourcesPerModule = webContainerProvider.loadResourcesPerApplication(applicationName);
        } catch (Exception e) {
          throw new WCEApplicationNotDeployedException(WCEApplicationNotDeployedException.WEBMODULE_IS_NOT_FOUND, new Object[]{resourceName, applicationName}, e);
        }
      }
      Enumeration<String> enumeration = resourcesPerModule.keys();
      while (enumeration.hasMoreElements()) {
        String moduleName = (String) enumeration.nextElement();
        Hashtable<String, Vector<String>> deployedResources = (Hashtable<String, Vector<String>>) resourcesPerModule.get(moduleName);
        if (deployedResources.containsKey(resourceName)) {
          Vector<String> resTypes = (Vector<String>) deployedResources.get(resourceName);
          if (resTypes.contains(resourceType)) {
            return getWebModuleContext(moduleName);
          }
        }
      }
      throw new WCEApplicationNotDeployedException(WCEApplicationNotDeployedException.WEBMODULE_IS_NOT_FOUND, new Object[]{resourceName, applicationName});
    }
    throw new WCEApplicationNotDeployedException(WCEApplicationNotDeployedException.APPLICATION_FOR_RESOURCE_NOT_FOUND, new Object[]{resourceName, resourceType});
  }//end of getWebModuleContext(String resourceName, String resourceType)
  
  public IWebModuleContext getWebModuleContext(IWebModule webModule) throws ApplicationNotStartedException {
    if (webModule == null) {
      return null;
    }
    
    IWebModuleContext webModuleContext = (IWebModuleContext) webContainerProvider.getStartedWebApplications().get(webModule.getModuleName());
    if (webModuleContext != null) {
      //If during onStart() somebody makes getWebModuleContext() it is already created and can be returned in other case we will try to start it during starting!
      //If during onStop() somebody makes getWebModuleContext() it is not still destroyed and can be returned in other case during stop we will try to start it again!
      return webModuleContext;
    } else {
      try {
        //Checks if the WCE is in unregistering phase in which case throws ApplicationNotStartedException:
        applicationManager.analyseAppStatusMode((WebModule)webModule, true);
      } catch (RemoteException e) {
        throw new WCEApplicationNotStartedException(WCEApplicationNotStartedException.CANNOT_ANALYSE_STARTUP_MODE, new Object[]{((WebModule)webModule).getWholeApplicationName()}, e);
      }
    }

    return (IWebModuleContext) webContainerProvider.getStartedWebApplications().get(webModule.getModuleName());    
  }//end of getWebModuleContext(IWebModule webModule)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext#getApplicationManager()
   */
  public IApplicationManager getApplicationManager() {
    return applicationManager;
  }//end of getApplicationManager()

  /**
   * @return <code>ConcurrentHashMap</code> with deployed applications specific for this web container extension.
   */
  public ConcurrentHashMap<String, WebModule> getMyDeployedWebApplications() {
    return myDeployedWebApplications;
  }//end of getMyDeployedWebApplications()

} //end of class
