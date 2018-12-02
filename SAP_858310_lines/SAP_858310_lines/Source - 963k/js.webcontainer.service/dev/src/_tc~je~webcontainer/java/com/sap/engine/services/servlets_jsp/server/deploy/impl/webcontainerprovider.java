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
package com.sap.engine.services.servlets_jsp.server.deploy.impl;

import static com.sap.engine.services.servlets_jsp.server.deploy.WebContainer.wceStartupTime;
import static com.sap.engine.services.servlets_jsp.server.deploy.WebContainer.allApplicationsStartedTime;

import static com.sap.engine.services.servlets_jsp.server.LogContext.CATEGORY_DEPLOY;
import static com.sap.engine.services.servlets_jsp.server.ServiceContext.getServiceContext;
import static com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEDeploymentException.*;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.frame.core.configuration.addons.PropertySheet;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.deploy.ear.common.EqualUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainer;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainerHelper;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEApplicationNotDeployedException;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEDeploymentException;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEWarningException;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModuleContext;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.webcontainer_api.IWebContainerProvider;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.DeployInfo;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebContainerDeploy;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebContainerDeployExtended;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebContainerLifecycleDeprecated;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.ResourceReference;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WCEConfigurationException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtension;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;

/**
 * @author Maria Jurova
 * @author Violeta Georgieva
 * @version 7.10
 */
public class WebContainerProvider implements IWebContainerProvider, IWebContainer {
  private static Location currentLocation = Location.getLocation(WebContainerProvider.class);
  private static Location traceLocation = LogContext.getLocationWebContainerProvider();
  /**
   * One for all WCE and WebContainer
   */
  private ApplicationManager applicationManager = new ApplicationManager(this, null);
  /**
   * List of helper wrappers for specific WCE
   */
  private Hashtable<String, WebContainerExtensionWrapper> webContainerExtensionWrappers = new Hashtable<String, WebContainerExtensionWrapper>();
  /**
   * all deployed "web" modules
   * The mapping is alias to vector with web modules, where the elements in the
   * vector are ordered corresponding to the prios of the applications (in the beginning,
   * those with higher prio).
   *
   * Modules per alias could be more than one only in case of default application. The web
   * modules order is first the web module, which is for customer default application and
   * after it SAP default application.
   *
   * In all other cases the vector has one element only
   */
  private ConcurrentHashMap<String, Vector<WebModule>> deployedWebApplications = new ConcurrentHashMap<String, Vector<WebModule>>();
  /**
   * list of all started modules
   */
  private ConcurrentHashMap<String, WebModuleContext> startedWebApplications = new ConcurrentHashMap<String, WebModuleContext>();
  /**
   * workaround "resource name" - "resource type" per module per application
   */
  private ConcurrentHashMap<String, Hashtable<String, Hashtable<String, Vector<String>>>> resourcesPerApplication = new ConcurrentHashMap<String, Hashtable<String, Hashtable<String, Vector<String>>>>();
  /**
   * valid only during deploy and update, otherwise they are get from deploy communicator
   */
  private ConcurrentHashMap<String, Properties> applicationsGlobalProperties = new ConcurrentHashMap<String, Properties>();
  /**
   * The flag designates that this WCE is being unregistered.
   * It's mainly used to prevent applications start during their WCE unregistering.
   */
  private Set<String> unregisteringWCEs = Collections.synchronizedSet(new HashSet<String>());

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.IWebContainerProvider#registerWebContainerExtension(com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtension, String, String)
   */
  public void registerWebContainerExtension(IWebContainerExtension webContainerExtension, String webContainerExtensionName, String descriptorName) throws WebContainerExtensionDeploymentException {
		if (traceLocation.beDebug()) {
			traceLocation.debugT("Register WCE provider [" + webContainerExtensionName + "] with descriptor name [" + descriptorName + "].");
		}
    registerWebContainerExtension(webContainerExtension, webContainerExtensionName, new String[]{descriptorName});
  }//end of registerWebContainerExtension(IWebContainerExtension webContainerExtension, String webContainerExtensionName, String descriptorName)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.IWebContainerProvider#registerWebContainerExtension(com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtension, String, String[])
   */
  public void registerWebContainerExtension(IWebContainerExtension webContainerExtension, String webContainerExtensionName, String[] descriptorNames) throws WebContainerExtensionDeploymentException {
		if (traceLocation.beDebug()) {
			traceLocation.debugT("Register WCE provider [" + webContainerExtensionName + "] with descriptors names " +
			  (new Vector<String>(Arrays.asList(descriptorNames))).toString() + ".");
		}
    unregisteringWCEs.remove(webContainerExtensionName);

    if (webContainerExtensionName == null || descriptorNames == null) {
      throw new WCEDeploymentException(WCENAME_OR_DESCRIPTORNAMES_ARE_NULL);
    }

    //Check WCE provider name
    if (webContainerExtensionWrappers.containsKey(webContainerExtensionName)) {
      //There has been already registered another one WCE provider with the same name.
      //Exception must be thrown.
      Vector<String> descriptors = ((WebContainerExtensionWrapper) webContainerExtensionWrappers.get(webContainerExtensionName)).getDescriptorNames();
      throw new WCEDeploymentException(WCENAME_ALREADY_IN_THE_LIST, new Object[]{webContainerExtensionName, descriptors.toString()});
    }

    //Check whether WCE provider's mapping is available in Web Container's property - "WebContainerExtensionsProviders"
    checkWCE(webContainerExtensionName, new Vector<String>(Arrays.asList(descriptorNames)), true);

    WebContainerExtensionContext webContainerExtensionContext = new WebContainerExtensionContext(this, webContainerExtensionName);
    WebContainerExtensionWrapper webContainerExtensionWrapper =
      new WebContainerExtensionWrapper(webContainerExtension, webContainerExtensionContext, webContainerExtensionName, descriptorNames);

    try {
			if (traceLocation.beDebug()) {
				traceLocation.debugT("Call init() method of WCE provider [" + webContainerExtensionName + "].");
			}
      webContainerExtension.init(webContainerExtensionContext);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
    	//Here throwable is caught and wrapped intentionally, because the Portal has requested it.
      throw new WCEDeploymentException(WCE_CANNOT_BE_INITIALIZED, new Object[]{webContainerExtensionName}, e);
    }

    webContainerExtensionWrappers.put(webContainerExtensionName, webContainerExtensionWrapper);

    //Set WCE provider specific deployed web modules
    ArrayList<String> allMyApplications = new ArrayList<String>();
    HashSet<String> allWebModuleNames = new HashSet<String>(deployedWebApplications.keySet()); //all deployed web aliases
    Iterator<String> iterator = allWebModuleNames.iterator();
    while (iterator.hasNext()) {
      String moduleName = (String) iterator.next(); //the web alias
      WebModule webModule = getDeployedAppl(moduleName);
      //vendor/name
      String applicationName = webModule.getWholeApplicationName();

      Vector<String> wceInDeploy = webModule.getWceInDeploy(); //all the WCE that participated in the deploy of this web module
      //check whether this wce has participated in the application deploy
      if (wceInDeploy.contains(webContainerExtensionName)) {
        webContainerExtensionContext.getMyDeployedWebApplications().put(moduleName, webModule);

        if (!allMyApplications.contains(applicationName)) {
          allMyApplications.add(applicationName);
        }
      }
    }

    //Start WCE provider's applications only (this will start applications that are not lazy - this is guarantee by deploy service)
    try {
      if (allMyApplications.size() > 0) {
        getServiceContext().getWebContainer().allApplicationsStartedCounter.incrementAndGet();
        getServiceContext().getDeployContext().getDeployCommunicator().startMyApplications((String[]) allMyApplications.toArray(new String[allMyApplications.size()]));
      }
    } catch (RemoteException e) {
      LogContext.getCategory(CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000042",
        "Error occurred in startMyApplications() while registering WCE provider with name [{0}].",
        new Object[]{webContainerExtensionWrapper.getWebContainerExtensionName()}, e, null, null);
    }
  }//end of registerWebContainerExtension(IWebContainerExtension webContainerExtension, String webContainerExtensionName, String descriptorNames)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.IWebContainerProvider#unregisterWebContainerExtension(java.lang.String)
   */
  public void unregisterWebContainerExtension(String webContainerExtensionName) {
		if (traceLocation.beDebug()) {
			traceLocation.debugT("Unregister WCE provider [" + webContainerExtensionName + "].");
		}
    unregisteringWCEs.add(webContainerExtensionName);
    WebContainerExtensionWrapper webContainerExtensionWrapper = (WebContainerExtensionWrapper) webContainerExtensionWrappers.get(webContainerExtensionName);
    if (webContainerExtensionWrapper != null) {
      //Stop WCE provider's applications only
      Enumeration<WebModule> allMyWebModules = ((WebContainerExtensionContext) webContainerExtensionWrapper.getWebContainerExtensionContext()).getMyDeployedWebApplications().elements();
      ArrayList<String> allMyApplications = new ArrayList<String>();
      while (allMyWebModules.hasMoreElements()) {
        WebModule webModule = (WebModule) allMyWebModules.nextElement();
        String applicationName = webModule.getWholeApplicationName();
        if (!allMyApplications.contains(applicationName)) {
          allMyApplications.add(applicationName);
        }
      }

      try {
        if (allMyApplications.size() > 0) {
          getServiceContext().getDeployContext().getDeployCommunicator().stopMyApplications((String[]) allMyApplications.toArray(new String[allMyApplications.size()]));
        }
      } catch (RemoteException e) {
        LogContext.getCategory(CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000043",
          "Error occurred in stopMyApplications() while unregistering WCE provider with extension ID [{0}].",
          new Object[]{webContainerExtensionWrapper.getWebContainerExtensionName()}, e, null, null);
      }

			if (traceLocation.beDebug()) {
				traceLocation.debugT("Call destroy() method of WCE provider [" + webContainerExtensionName + "].");
			}
      webContainerExtensionWrapper.getWebContainerExtension().destroy();
    }
    webContainerExtensionWrappers.remove(webContainerExtensionName);
    //TODO: should it be possible after unregister finishes to start app?
    //in case WebContainerExtensionContext.getWebModuleContext was delayed:
    unregisteringWCEs.remove(webContainerExtensionName);
  }//end of unregisterWebContainerExtension(String webContainerExtensionName)

  /**
   */
  public void stop() {
		if (traceLocation.beDebug()) {
			traceLocation.debugT("Trying to unregister all WCE providers.");
		}
    Enumeration<WebContainerExtensionWrapper> wrappers = webContainerExtensionWrappers.elements();
    while (wrappers.hasMoreElements()) {
      WebContainerExtensionWrapper webContainerExtensionWrapper = (WebContainerExtensionWrapper) wrappers.nextElement();
      if (webContainerExtensionWrapper != null) {
				if (traceLocation.beDebug()) {
					traceLocation.debugT("Call destroy() method of WCE provider [" + webContainerExtensionWrapper.getWebContainerExtensionName() + "].");
				}
        webContainerExtensionWrapper.getWebContainerExtension().destroy();
      }
    }
    webContainerExtensionWrappers.clear();
  }//end of stop()

  /**
   * @see com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer#deploy(java.lang.String,java.lang.String[],java.io.File[], boolean, Properties)
   */
  public DeployInfoExtension deploy(String applicationName, String[] aliases, File[] appRoots, boolean isUpdate, Properties appGlobalProps,
  		Configuration servlet_jspConfig) throws DeploymentException {

    String debugInfo = (!isUpdate) ? "deploy" : "makeUpdate";

    String accountingTag0 = debugInfo + "/WebContainerProvider.deploy(" + applicationName + ")";
    if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
      Accounting.beginMeasure(accountingTag0, WebContainerProvider.class);
    }//ACCOUNTING.start - END
    try {
      ArrayList<Exception> warnings = new ArrayList<Exception>();

      //appGlobalProps is full only during update
      if (appGlobalProps != null) {
        applicationsGlobalProperties.put(applicationName, appGlobalProps);
      }

      DeployInfoExtension deployInfo = new DeployInfoExtension();
      Hashtable<String, Hashtable<String, Vector<String>>> resourcesPerModule = new Hashtable<String, Hashtable<String, Vector<String>>>();
      Hashtable<String, Vector<String>> wceInDeployPerModule = new Hashtable<String, Vector<String>>();
      HashMap<String, String[]> webModulesPerWCE = new HashMap<String, String[]>();

      for (int i = 0; i < aliases.length; i++) {//Iterate over the web modules of this application
        WebModule webModule = new WebModule(applicationName, aliases[i], appRoots[i]);
        addDeployedAppl(applicationName, aliases[i], webModule);

        Hashtable<String, Vector<String>> deployedResources = new Hashtable<String, Vector<String>>();

        Enumeration<WebContainerExtensionWrapper> en = webContainerExtensionWrappers.elements();
        while (en.hasMoreElements()) {//Iterate over all the WCEs
          WebContainerExtensionWrapper wrapper = (WebContainerExtensionWrapper) en.nextElement();
          Vector<String> descriptorNames = wrapper.getDescriptorNames();
          String wceName = wrapper.getWebContainerExtensionName();

          if (WebContainerHelper.isDescriptorExist(descriptorNames, appRoots[i].getAbsolutePath())) {
            IWebContainerExtension webContainerExtension = wrapper.getWebContainerExtension();
            IWebContainerDeploy webDeployHandler = webContainerExtension.getWebDeployHandler();
            if (webDeployHandler != null) {
              //Check whether WCE provider's mapping is available in Web Container's property - "WebContainerExtensionsProviders"
              checkWCE(wceName, descriptorNames, true);
              //it is important to add the WCE to ensure it will be invoked on remove
              //Record that this wceName has participated in the deploy of this webModule
              webModule.getWceInDeploy().addElement(wceName);

              WebContainerExtensionContext wceContext = (WebContainerExtensionContext) wrapper.getWebContainerExtensionContext();
              wceContext.getMyDeployedWebApplications().put(aliases[i], webModule);

              DeployInfo resources = null;
              WebModuleDeployContextImpl wceDeploy = new WebModuleDeployContextImpl(webModule, wceName);
              String accountingTag1 = "/WebContainerProvider.deploy(" + applicationName + ").onDeploy(" + wceName + ")";
              try {
  							wceDeploy.createAndStoreConfiguration(servlet_jspConfig);

  							if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
                  Accounting.beginMeasure(accountingTag1, webDeployHandler.getClass());
                }//ACCOUNTING.start - END

              	if (webDeployHandler instanceof IWebContainerDeployExtended) {
              		wceDeploy.setMyWebModules(getWebModules(aliases, appRoots, wrapper, webModulesPerWCE));
              		if (traceLocation.beDebug()) {
              			traceLocation.debugT("Invoke onDeploy(" + webModule + ", " + appRoots[i].getAbsolutePath() + ", " + wceDeploy + ") " +
              			  "for WCE provider [" + wceName + "] with descriptor name " + descriptorNames.toString() + ".");
              		}
              		resources = ((IWebContainerDeployExtended)webDeployHandler).onDeploy(webModule, appRoots[i].getAbsolutePath(), wceDeploy);
              	} else {
              		if (traceLocation.beDebug()) {
              			traceLocation.debugT("Invoke onDeploy(" + webModule + ", " + appRoots[i].getAbsolutePath() + ") " +
              			  "for WCE provider [" + wceName + "] with descriptor name " + descriptorNames.toString() + ".");
              		}
              		resources = webDeployHandler.onDeploy(webModule, appRoots[i].getAbsolutePath());
              	}
              } catch (WarningException e) {
                //$JL-EXC$
                warnings.add(e);
              } catch (WCEConfigurationException e) {
                //$JL-EXC$
                warnings.add(e);
              } finally {
              	//invalidating this object makes it impossible to use the configuration for write access
              	wceDeploy.invalidate();

              	if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
                  Accounting.endMeasure(accountingTag1);
                }//ACCOUNTING.end - END
              }

              if (resources != null) {
              	if (traceLocation.beDebug()) {
              		traceLocation.debugT("WCE provider [" + wceName + "] returns the following deployment information.");
              	}

                deployInfo.addPublicReferences(resources.getPublicReferences());

                deployInfo.addPrivateReferences(resources.getPrivateReferences());

                Vector publicResourceReferences = resources.getPublicResourceReferences();
                for (int j = 0; j < publicResourceReferences.size(); j++) {
                  deployInfo.addPublicResourceReference((ResourceReference) publicResourceReferences.get(j));
                }

                Vector privateResourceReferences = resources.getPrivateResourceReferences();
                for (int j = 0; j < privateResourceReferences.size(); j++) {
                  deployInfo.addPrivateResourceReference((ResourceReference) privateResourceReferences.get(j));
                }

                Hashtable deployedResource_Types = resources.getDeployedResources_Types();
                Enumeration enumeration = deployedResource_Types.keys();
                while (enumeration.hasMoreElements()) {
                  String component = (String) enumeration.nextElement();
                  String[] resTypes = (String[]) deployedResource_Types.get(component);

                  //Add info to the deploy info
                  deployInfo.addDeployedResource_Types(component, resTypes);

                  //Add info to the local cache
                  Vector<String> types = (Vector<String>) deployedResources.get(component);
                  if (types == null) {
                    deployedResources.put(component, new Vector<String>(Arrays.asList(resTypes)));
                  } else {
                    types.addAll(Arrays.asList(resTypes));
                    deployedResources.put(component, types);
                  }
                }
              }
            }
          }
        }

        if (!deployedResources.isEmpty()) {
          resourcesPerModule.put(aliases[i], deployedResources);
        }

        checkRequiredWCE(webModule.getWceInDeploy(), appRoots[i].getAbsolutePath(), debugInfo, aliases[i]);

        if (webModule.getWceInDeploy().size() > 0) {
          //important for wceInDeployPerModule to be written in the configuration
          wceInDeployPerModule.put(aliases[i], webModule.getWceInDeploy());
        }
      }

      if (!resourcesPerModule.isEmpty()) {
        resourcesPerApplication.put(applicationName, resourcesPerModule);
      }

      if (!wceInDeployPerModule.isEmpty()) {
        deployInfo.setWceInDeploy(wceInDeployPerModule);
      }

      applicationsGlobalProperties.remove(applicationName);
      webModulesPerWCE.clear();

      if (warnings != null && warnings.size() > 0) {
        WarningException warningExc = new WarningException();
        for (int i = 0; i < warnings.size(); i++) {
          warningExc.addWarning(warnings.get(i).toString());
        }
        deployInfo.setWarningException(warningExc);
      }

      return deployInfo;
  	} finally {
  	  if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag0);
      }//ACCOUNTING.end - END
  	}
  }//end of deploy(String applicationName, String[] aliases, File[] appRoots)

  /**
   * Initializes the deployed application's list on service startup
   * @see com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer#appDeployedButNotStarted(String, String[], java.io.File[], java.util.Hashtable)
   */
  public void appDeployedButNotStarted(String applicationName, String[] aliases, File[] appRoots, Hashtable wceInDeployPerModule) {
    boolean isServiceStarting = getServiceContext().isServiceStarting();
    for (int i = 0; i < aliases.length; i++) {
      WebModule webModule = new WebModule(applicationName, aliases[i], appRoots[i]);
      addDeployedAppl(applicationName, aliases[i], webModule);

      Vector wceInDeploy = (Vector) wceInDeployPerModule.get(aliases[i]);
      if (wceInDeploy != null) {
        webModule.setWceInDeploy(wceInDeploy);
      }

      if (!isServiceStarting) {
        wceInDeploy = webModule.getWceInDeploy();
        for (int j = 0; j < wceInDeploy.size(); j++) {
          String wceName = (String) wceInDeploy.elementAt(j);
          WebContainerExtensionWrapper wrapper = (WebContainerExtensionWrapper) webContainerExtensionWrappers.get(wceName);
          if (wrapper != null) {
            Vector<String> descriptorNames = wrapper.getDescriptorNames();

            //Check whether WCE provider's mapping is available in Web Container's property - "WebContainerExtensionsProviders"
            checkWCE(wceName, descriptorNames, true);

            ((WebContainerExtensionContext) wrapper.getWebContainerExtensionContext()).getMyDeployedWebApplications().put(aliases[i], webModule);
          } else {
						if (traceLocation.beWarning()) {
							LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceWarning("ASJ.web.000492",
                "WCE provider [{0}] is not registered in order to initialize its context.", new Object[]{wceName}, null, null);
          }
        }
      }
    }
    }
  }//end of appDeployedButNotStarted(String applicationName, String[] aliases)

  /**
   * @see com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer#remove(java.lang.String, java.lang.String[], boolean)
   */
  public void remove(String applicationName, String[] aliases, boolean isUpdate, Configuration servlet_jspConfig) throws DeploymentException, WarningException {
    String debugInfo = (!isUpdate) ? "remove" : "makeUpdate";
    String accountingTag0 = debugInfo + "/WebContainerProvider.remove(" + applicationName + ")";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag0, WebContainerProvider.class);
      }//ACCOUNTING.start - END

      ArrayList<Exception> warnings = new ArrayList<Exception>();

      //it may happen exception to be thrown from onDeploy() then this table will not be cleared
      //so we try to clear it now
      applicationsGlobalProperties.remove(applicationName);

      for (int i = 0; i < aliases.length; i++) {
        WebModule webModule = getDeployedAppl(aliases[i], applicationName);
        //Check module because there can happen to invoke remove() twice (this can happen when roll back update)
        //Or it can happen exception to be thrown before invoking onDeploy() method then in onRemove there won't be any web module
        if (webModule != null) {
          Vector<String> wceInDeploy = webModule.getWceInDeploy();
          for (int j = 0; j < wceInDeploy.size(); j++) {
            String wceName = (String) wceInDeploy.elementAt(j);
            WebContainerExtensionWrapper wrapper = (WebContainerExtensionWrapper) webContainerExtensionWrappers.get(wceName);
            if (wrapper != null) {
              Vector<String> descriptorNames = wrapper.getDescriptorNames();

              IWebContainerExtension webContainerExtension = wrapper.getWebContainerExtension();
              IWebContainerDeploy webDeployHandler = webContainerExtension.getWebDeployHandler();
  						if (webDeployHandler != null) {
                //Check whether WCE provider's mapping is available in Web Container's property - "WebContainerExtensionsProviders"
                checkWCE(wceName, descriptorNames, true);

                WebModuleDeployContextImpl wceDeploy = new WebModuleDeployContextImpl(webModule, wceName);
                boolean configLoaded = false;
                String accountingTag1 = "/onRemove(" + wceName + "," + webModule.getModuleName() + ")";
                try {
                  wceDeploy.loadConfiguration(servlet_jspConfig);
                  configLoaded = true;

                  if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
                    Accounting.beginMeasure(accountingTag1, webDeployHandler.getClass());
                  }//ACCOUNTING.start - END

                  if (webDeployHandler instanceof IWebContainerDeployExtended) {
                  	wceDeploy.setMyWebModules(wrapper.getWebContainerExtensionContext().getMyWebModules(applicationName));
                		if (traceLocation.beDebug()) {
                			traceLocation.debugT("Invoke onRemove(" + webModule + ", " + wceDeploy + ") " +
                			  "for WCE provider [" + wceName + "] with descriptor name " + descriptorNames.toString() + ".");
                		}
                  	((IWebContainerDeployExtended) webDeployHandler).onRemove(webModule, wceDeploy);
                  } else {
                		if (traceLocation.beDebug()) {
                			traceLocation.debugT("Invoke onRemove(" + webModule + ") " +
                			  "for WCE provider [" + wceName + "] with descriptor name " + descriptorNames.toString() + ".");
                		}
                  	webDeployHandler.onRemove(webModule);
                  }
                } catch (Exception e) {
                	if (! configLoaded) {
                		if (webDeployHandler instanceof IWebContainerDeployExtended) {
                  		//The WCE Provider needs it, but the configuration could not be loaded: error will be logged:
                    	warnings.add(e);
                  	} else {
                  		//Log warning only and not error:
                  		//TODO:Polly check dcName - appName ?
                  		LogContext.getCategory(CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000007",
                  		  "The WCE sub configuration was not loaded: possible cause is that the application [{0}] is old/non-migrated; the configuration will be created during next " +
                  	    "update/deploy of this application. Although the configuration is not loaded, " +
                  	    " the onRemove(WebModule) method for WCE Provider [{1}] will still be executed, because the new configuration is not needed yet, since " +
                  	    "the WCE Provider does not implement the new IWebContainerDeployExtended interface.", new Object[]{applicationName, wceName}, null, null);
                  		if (traceLocation.beDebug()) {
                  			traceLocation.debugT("Invoke onRemove(" + webModule + ") " +
                  			  "for WCE provider [" + wceName + "] with descriptor name " + descriptorNames.toString() + ".");
                  		}
                  		webDeployHandler.onRemove(webModule);
                  	}
                	} else {
                		//some other error has occurred, not in loading the configuration:
                		warnings.add(e);
                	}
                } finally {
                	//invalidating this object makes it impossible to use the configuration for write access
                	wceDeploy.invalidate();

                	if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
                    Accounting.endMeasure(accountingTag1);
                  }//ACCOUNTING.end - END
                }
              }
              ((WebContainerExtensionContext) wrapper.getWebContainerExtensionContext()).getMyDeployedWebApplications().remove(aliases[i]);
            } else {
              warnings.add(new WCEWarningException(WCEWarningException.REQUIRED_WCE_IS_NOT_REGISTERED, new Object[]{debugInfo, aliases[i], wceName}));
            }
          }
        }

        Vector<WebModule> webModules = deployedWebApplications.get(aliases[i]);
        if (webModules != null) {  //if the module is not in the deployedWebApplications -> it is part of rollback call
          // remove the web module from the vector and if it
          // is the last web module for this alias remove the whole entry from the hashtable
          webModules.removeElement(webModule);
          if (webModules.size() == 0) {
            // remove the whole entry from the hashtable
            deployedWebApplications.remove(aliases[i]);
          } else {
            // update the entry into the table
            deployedWebApplications.put(aliases[i], webModules);
          }
        }
      }

      resourcesPerApplication.remove(applicationName);

      WarningException warningExc = null;
  		if (warnings != null && warnings.size() > 0) {
  			/*
  			 * Note:
  			 * WarningException's stack trace combines several lines of the accumulated
  			 * exception's toString results and a stack trace from the place where it
  			 * has been created (i.e. the new WarningExcepiton() line).
  			 */
  			warningExc = new WarningException();
  			for (int i = 0; i < warnings.size(); i++) {
  				warningExc.addWarning(warnings.get(i).toString());
  			}
  			throw warningExc;
  		}
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag0);
      }//ACCOUNTING.end - END
    }
  }//end of remove(String applicationName, String[] aliases, boolean isUpdate)

  public void notifyRemove(String applicationName, String[] aliases) {
    for (int i = 0; i < aliases.length; i++) {
      WebModule webModule = getDeployedAppl(aliases[i], applicationName);

      //Check module because there can be added new alias which previously was not deployed
      if (webModule != null) {
        Vector<String> wceInDeploy = webModule.getWceInDeploy();
        for (int j = 0; j < wceInDeploy.size(); j++) {
          String wceName = (String) wceInDeploy.elementAt(j);
          WebContainerExtensionWrapper wrapper = (WebContainerExtensionWrapper) webContainerExtensionWrappers.get(wceName);
          if (wrapper != null) {
            Vector<String> descriptorNames = wrapper.getDescriptorNames();

            //Check whether WCE provider's mapping is available in Web Container's property - "WebContainerExtensionsProviders"
            checkWCE(wceName, descriptorNames, true);

            ((WebContainerExtensionContext) wrapper.getWebContainerExtensionContext()).getMyDeployedWebApplications().remove(aliases[i]);
          } else {
            LogContext.getCategory(CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000008",
              "WCE provider [{0}] is not registered in order to notify it for removed application.", new Object[]{wceName}, null, null);
          }
        }

        Vector<WebModule> webModules = deployedWebApplications.get(aliases[i]);
        if (webModules != null) {
          // remove the web module from the vector and if it
          // is the last web module for this alias remove the whole entry from the hashtable
          webModules.removeElement(webModule);
          if (webModules.size() == 0) {
            deployedWebApplications.remove(aliases[i]);
          } else {
            deployedWebApplications.put(aliases[i], webModules);
          }
        }
      }
    }

    resourcesPerApplication.remove(applicationName);
  }//end of notifyRemove(String applicationName, String[] aliases)

  /**
   * @see IWebContainer#start(String, String[], javax.servlet.ServletContext[], ClassLoader, ClassLoader, java.util.Vector)
   */
  public void start(String applicationName, String[] aliases, ServletContext[] servletContexts, ClassLoader publicClassloader) throws DeploymentException, WarningException {
    ArrayList<Exception> warnings = new ArrayList<Exception>();
    final String accountingTag0 = "WebContainerProvider.start(" + applicationName + ")";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag0, WebContainerProvider.class);
      }//ACCOUNTING.start - END

      for (int i = 0; i < aliases.length; i++) {
        // ako se hvashta deploymentException - hvarlqi deploymentException
        // ako NE se hvashta deploymentException - hvarliq warningExc
        WebModule webModule = getDeployedAppl(aliases[i], applicationName);
        if (webModule == null) {
          throw new WCEApplicationNotDeployedException(WCEApplicationNotDeployedException.WEBMODULE_IS_NOT_FOUND, new Object[]{aliases[i], applicationName});
        }

        WebModuleContext webModuleContext = (WebModuleContext) startedWebApplications.get(aliases[i]);

        Vector<String> wceInDeploy = webModule.getWceInDeploy();
        for (int j = 0; j < wceInDeploy.size(); j++) {
          String wceName = (String) wceInDeploy.elementAt(j);
          WebContainerExtensionWrapper wrapper = (WebContainerExtensionWrapper) webContainerExtensionWrappers.get(wceName);
          if (wrapper != null) {
            Vector<String> descriptorNames = wrapper.getDescriptorNames();
            IWebContainerExtension webContainerExtension = wrapper.getWebContainerExtension();
            if (webContainerExtension.getWebLifecycleHandler() != null) {
              try {
  					    ThreadWrapper.pushSubtask("Initializing WCE provider [" + wceName + "] in Web Container.",
  					      ThreadWrapper.TS_PROCESSING);
  					    //Check whether WCE provider's mapping is available in Web Container's property - "WebContainerExtensionsProviders"
  					    checkWCE(wceName, descriptorNames, true);
  				    } finally {
  				    	ThreadWrapper.popSubtask();
  				    }

  				    long onStartStartupTime = -1;
  				    final String accountingTag1 = "WCE [" + wceName + "].start (" + webModule.getModuleName() + ")";
              try {
  				      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
  				        onStartStartupTime = System.currentTimeMillis();
  				        Accounting.beginMeasure(accountingTag1,  webContainerExtension.getWebLifecycleHandler().getClass());
  				      }//ACCOUNTING.start - END

  				      ThreadWrapper.pushTask("The WCE Provider [" + wceName + "] " +
  				        "initializes its components for web module [" + webModule.getModuleName() + "].",
  				        ThreadWrapper.TS_PROCESSING);

            		if (traceLocation.beDebug()) {
            			traceLocation.debugT("Invoke onStart(" + webModuleContext + ") " +
            			  "for WCE provider [" + wceName + "] with descriptor name " + descriptorNames.toString() + ".");
            		}
  				      webContainerExtension.getWebLifecycleHandler().onStart(webModuleContext);
  				    } catch (WarningException e) {
  				      //$JL-EXC$
  				      warnings.add(e);
  				    } finally {
  				    	ThreadWrapper.popTask();

  				    	if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
                  Accounting.endMeasure(accountingTag1);
                  onStartStartupTime = System.currentTimeMillis() - onStartStartupTime;
                  wceStartupTime.addAndGet(onStartStartupTime);
                }//ACCOUNTING.end - END
              }
  				  }
  				} else {
  				  throw new WCEDeploymentException(REQUIRED_WCE_IS_NOT_REGISTERED, new Object[]{"prepareStart", aliases[i], wceName,
  				    "Web Container Extension provider has taken part into deployment process, but now is not available."});
  				}
        } //for wceInDeploy
      }

      if (warnings != null && warnings.size() > 0) {
        WarningException warningExc = new WarningException();
        for (int i = 0; i < warnings.size(); i++) {
          warningExc.addWarning(warnings.get(i).toString());
        }
        throw warningExc;
      }
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag0);
      }//ACCOUNTING.end - END
    }

  }//end of start(String applicationName, String[] aliases, ServletContext[] servletContexts, ClassLoader appLoader)

  public WebModuleContext initWebModuleContext(String alias, ServletContext servletContext, String applicationName, ClassLoader publicClassloader) {
    WebModule webModule = getDeployedAppl(alias, applicationName);
    WebModuleContext webModuleContext = null;
		if (webModule != null) {
			ApplicationContext applicationContext = getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(alias.getBytes()));
			webModuleContext = new WebModuleContext(webModule, applicationContext);
			webModuleContext.setPublicClassLoader(publicClassloader);
			//webModuleContext.setPrivateClassLoader(privateClassloader);
			webModuleContext.setLocalDeploymentFolder(new File(servletContext.getRealPath("")));
			startedWebApplications.put(alias, webModuleContext);
    } else {
    	if (traceLocation.beWarning()) {
    		LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceWarning("ASJ.web.000493",
					"Loading of WebModuleContext failed because not found in deployed applications; "
					+ "publicClassloader={0}", new Object[]{publicClassloader}, null, null);
    	}
    }
		return webModuleContext;
	}

  /**
   * @see com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer#stop(String, String[], java.util.Vector)
   */
  public void stop(String applicationName, String[] aliases) throws WarningException {
    ArrayList<Exception> warnings = new ArrayList<Exception>();
    long start = -1;
    long end = -1;

    for (int i = 0; i < aliases.length; i++) {
      IWebModuleContext webModuleContext = (WebModuleContext) startedWebApplications.get(aliases[i]);

      //It may happen exception to be thrown before invoking onStart() method then in onStop there won't be any web module context
      if (webModuleContext == null) {
        continue;
      }

      WebModule webModule = (WebModule) webModuleContext.getWebModule();

      Vector<String> wceInDeploy = webModule.getWceInDeploy();
      for (int j = 0; j < wceInDeploy.size(); j++) {
        String wceName = (String) wceInDeploy.elementAt(j);
        ThreadWrapper.pushSubtask("Destroying " + wceName + " components for web module ["
          + webModule.getModuleName() + "] in WebContainer.", ThreadWrapper.TS_PROCESSING);
        try {
	        WebContainerExtensionWrapper wrapper = (WebContainerExtensionWrapper) webContainerExtensionWrappers.get(wceName);
	        if (wrapper != null) {
	          Vector<String> descriptorNames = wrapper.getDescriptorNames();

	          IWebContainerExtension webContainerExtension = wrapper.getWebContainerExtension();
	          if (webContainerExtension.getWebLifecycleHandler() != null) {
	            //Check whether WCE provider's mapping is available in Web Container's property - "WebContainerExtensionsProviders"
	            checkWCE(wceName, descriptorNames, true);

	            String accountingTag = "WCE [" + wceName + "].stop(" + webModule.getModuleName() + ")";
              try {
	              if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
                  Accounting.beginMeasure(accountingTag, webContainerExtension.getWebLifecycleHandler().getClass());
                }//ACCOUNTING.start - END

	          		if (traceLocation.beDebug()) {
	          			traceLocation.debugT("Invoke onStop(" + webModuleContext + ") " +
	          			  "for WCE provider [" + wceName + "] with descriptor name " + descriptorNames.toString() + ".");
	          		}
	              webContainerExtension.getWebLifecycleHandler().onStop(webModuleContext);
	            } catch (Exception e) {
	              //$JL-EXC$
	              warnings.add(e);
	            } finally {
	              if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
                  Accounting.endMeasure(accountingTag);
                }//ACCOUNTING.end - END
	            }
	          }
	        } else {
	          warnings.add(new WCEWarningException(WCEWarningException.REQUIRED_WCE_IS_NOT_REGISTERED, new Object[]{"commitStop", aliases[i], wceName}));
	        }
        } finally {
        	ThreadWrapper.popSubtask();
        }
      }

      //Do not remove aliases[i] from startedWebApplications here, but later just before end of destroyWCEComponents in StopAction
    }

    if (warnings != null && warnings.size() > 0) {
      WarningException warningExc = new WarningException();
      for (int i = 0; i < warnings.size(); i++) {
        warningExc.addWarning(warnings.get(i).toString());
      }
      throw warningExc;
    }
  }//end of stop(String applicationName, String[] aliases)

  /**
   * @see IWebContainer#allApplicationsStarted()
   */
  public void allApplicationsStarted() throws WarningException {
    ArrayList<Exception> warnings = new ArrayList<Exception>();

    Enumeration<WebContainerExtensionWrapper> wrappers = webContainerExtensionWrappers.elements();
    while (wrappers.hasMoreElements()) {
      WebContainerExtensionWrapper webContainerExtensionWrapper = (WebContainerExtensionWrapper) wrappers.nextElement();
      if (webContainerExtensionWrapper != null) {
        Vector<String> descriptorNames = webContainerExtensionWrapper.getDescriptorNames();
        String wceName = webContainerExtensionWrapper.getWebContainerExtensionName();

        IWebContainerExtension webContainerExtension = webContainerExtensionWrapper.getWebContainerExtension();
        if (webContainerExtension.getWebLifecycleHandler() != null &&
          IWebContainerLifecycleDeprecated.class.isAssignableFrom((webContainerExtension.getWebLifecycleHandler()).getClass())) {
          long startup = -1;
          String accountingTag = "WCE [" + wceName + "].allApplicationsStarted()";
          try {
            //Check whether WCE provider's mapping is available in Web Container's property - "WebContainerExtensionsProviders"
            checkWCE(wceName, descriptorNames, true);

        		if (traceLocation.beDebug()) {
        			traceLocation.debugT("Invoke allApplicationsStarted() " +
        			  "for WCE provider [" + wceName + "] with descriptor name " + descriptorNames.toString() + ".");
        		}
            if (Accounting.isEnabled()) {
              Accounting.beginMeasure(accountingTag, webContainerExtension.getWebLifecycleHandler().getClass());
              startup = System.currentTimeMillis();
            }
            ((IWebContainerLifecycleDeprecated) webContainerExtension.getWebLifecycleHandler()).allApplicationsStarted();
          } catch (Exception e) {
            //$JL-EXC$
            warnings.add(e);
          } finally {
            if (Accounting.isEnabled()) {
              startup = System.currentTimeMillis() - startup;
              allApplicationsStartedTime.addAndGet(startup);
              Accounting.endMeasure(accountingTag);
            }
          }
        }
      }
    }

    if (warnings != null && warnings.size() > 0) {
      WarningException warningExc = new WarningException();
      for (int i = 0; i < warnings.size(); i++) {
        warningExc.addWarning(warnings.get(i).toString());
      }
      throw warningExc;
    }
  }//end of allApplicationsStarted()

  /**
   * @return a hashmap with all deployed web modules
   */
  public ConcurrentHashMap<String, Vector<WebModule>> getDeployedWebApplications() {
    return deployedWebApplications;
  }//end of getDeployedWebApplications()

  /**
   * @return a hashmap with all started web modules
   */
  public ConcurrentHashMap<String, WebModuleContext> getStartedWebApplications() {
    return startedWebApplications;
  }//end of getStartedWebApplications()

  /**
   * @return a hashmap with all deployed resources per application
   */
  public ConcurrentHashMap<String, Hashtable<String, Hashtable<String, Vector<String>>>> getResourcesPerApplication() {
    return resourcesPerApplication;
  }//end of getResourcesPerApplication()

  /**
   * @return application manager
   */
  public ApplicationManager getApplicationManager() {
    return applicationManager;
  }//end of getApplicationManager()

  /**
   * Check whether WCE provider's mapping is available in Web Container's property - "WebContainerExtensionsProviders"
   * The only result of this method is that it logs an Error message if there is an inconsistency:
   * there is no mapping defined in the WebContainerExtensionsProviders property or the mapping does not match the descriptors.
   * If the check succeeds, nothing is done and nothing is logged.
   *
   * @param webContainerExtensionName
   * @param descriptorNames
   */
  private void checkWCE(String webContainerExtensionName, Vector<String> descriptorNames, boolean suppressErrors) {
    Hashtable wce = getServiceContext().getWebContainerProperties().getWCEProviders();
    if (wce.containsKey(webContainerExtensionName)) {
      Vector registeredNames = (Vector) wce.get(webContainerExtensionName);
      if (!EqualUtils.equalVectors(registeredNames, descriptorNames)) {
        //There is WCE provider's name in the mapping, but the descriptor names are different.
      	if (suppressErrors) {
          //This change with suppressing errors is only for SP3
        	//Normally it must log error message and we must change the facades
        	//instead of changing the error message to debug.
					if (traceLocation.beDebug()) {
						traceLocation.debugT(
						  "WCE provider's name [" + webContainerExtensionName + "] is specified in the mapping in Web Container's property - [WebContainerExtensionsProviders], "
							+ "but descriptors names are different. WCE provider's descriptors names are " + descriptorNames.toString() + ", "
							+ "but these in the Web Container's property are " + registeredNames.toString() + ". WCE provider may not work properly.");
					}
        } else {
        	LogContext.getCategory(CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000044",
            "WCE provider's name [{0}] is specified in the mapping in Web Container's property - [WebContainerExtensionsProviders], " +
            "but descriptors names are different. WCE provider's descriptors names are [{1}], " +
            "but these in the Web Container's property are [{2}]. WCE provider may not work properly.",
            new Object[]{webContainerExtensionName, descriptorNames.toString(), registeredNames.toString()}, null, null);
        }
      }
    } else {
			//WCE provider's mapping is not available so error will be logged
			if (suppressErrors) {
				//This change with suppressing errors is only for SP3
				//Normally it must log error message and we must change the facades
				//instead of changing the error message to debug.
				if (traceLocation.beDebug()) {
					traceLocation.debugT(
					  "WCE provider's mapping is not specified in Web Container's property - [WebContainerExtensionsProviders]." +
						"WCE provider [" + webContainerExtensionName + "] may not work properly.");
				}
			} else {
				LogContext.getCategory(CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000045",
				  "WCE provider's mapping is not specified in Web Container's property - [WebContainerExtensionsProviders]." +
					"WCE provider [{0}] may not work properly.", new Object[]{webContainerExtensionName}, null, null);
			}
    }
  }//end of checkWCE(String webContainerExtensionName, String[] descriptorNames)

  private void checkRequiredWCE(Vector<String> wceInAction, String rootDir, String operation, String alias) throws DeploymentException {
  	//wce - the WCE Providers from the property
  	//webContainerExtensionWrappers - the registered at server startup WCE Providers
  	//wceInAction - WCE Provider needed for this web module (according the property?)
  	Hashtable wce = getServiceContext().getWebContainerProperties().getWCEProviders(); //from the property
    Enumeration wceNames = wce.keys();
    while (wceNames.hasMoreElements()) {
      String wceName = (String) wceNames.nextElement();
      if (!wceInAction.contains(wceName)) { //if the WCE Provider from the property was NOT part (required) of the action (according to the property)
        if (!webContainerExtensionWrappers.containsKey(wceName)) { //and if it is not registered at server startup
          if (WebContainerHelper.isDescriptorExist((Vector) wce.get(wceName), rootDir)) {
            //WCE provider is not registered but is in Web Container's property as required
            throw new WCEDeploymentException(REQUIRED_WCE_IS_NOT_REGISTERED, new Object[]{operation, alias, wceName,
              "Web Container Extension provider is described in servlet_jsp service property 'WebContainerExtensionsProviders', but is not available."});
          }
        }
      }
    }
  }//end of checkRequiredWCE(Vector wceInAction, String rootDir)

  public ConcurrentHashMap<String, Properties> getApplicationsGlobalProperties() {
    return applicationsGlobalProperties;
  }//end of getApplicationsGlobalProperties()

  public Hashtable<String, WebContainerExtensionWrapper> getWebContainerExtensionWrappers() {
  	return webContainerExtensionWrappers;
  }//end of getWebContainerExtensionWrappers()

  public boolean isUnregistering(String webContainerExtensionName) {
  	return unregisteringWCEs.contains(webContainerExtensionName);
  }

  /**
   * Returns the name of the first found unregistering WCE Provider among
   * all the WCE providers participating in deploy of this webModule.
   * @param webModule
   * @return WCE provider's name; null if such WCE provider was not found
   */
  public String isUnregistering(WebModule webModule) {
  	String result = null;
  	Enumeration<String> e = webModule.getWceInDeploy().elements();
  	while (e.hasMoreElements()) {
  		String wceName = (String) e.nextElement();
  		if (isUnregistering(wceName)) {
  			result = wceName;
  			break;
  		}
  	}
  	return result;
  }

  public Hashtable<String, Hashtable<String, Vector<String>>> loadResourcesPerApplication(String applicationName) throws WCEConfigurationException {
    Hashtable<String, Hashtable<String, Vector<String>>> resourcesPerModule = new Hashtable<String, Hashtable<String, Vector<String>>>();
    DeployCommunicator dc = getServiceContext().getDeployContext().getDeployCommunicator();
    Configuration appConfig = null;
    Configuration config = null;
    try {
      appConfig = dc.getAppConfigurationForReadAccess(applicationName);
      config = ConfigurationUtils.getSubConfiguration(appConfig, Constants.CONTAINER_NAME, applicationName, true);
    } catch (Exception e) {
      LogContext.getCategory(CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000046",
        "Cannot get the WCE configuration for [{0}] application.", new Object[]{applicationName}, e, null, null);
      if (appConfig != null) {
        try {
          appConfig.close();
        } catch (ConfigurationException e1) {
					if (traceLocation.beWarning()) {
						LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceWarning("ASJ.web.000494",
						  "Cannot close the root configuration (open in read-only mode) for application [{0}].", new Object[]{applicationName}, null, null);
					}
        }
      }
      throw new WCEConfigurationException("Failed to get the WCE configuration for [" + applicationName + "] application for read access.", e);
    }

    String [] aliasesCanonicalized = getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
    for (int i = 0; i < aliasesCanonicalized.length; i++) {
      boolean loadProperties = true;
      // Load deployed resources
      Configuration deployedResConfig = null;
      try {
        deployedResConfig = config.getSubConfiguration((WebContainerHelper.getAliasDirName(aliasesCanonicalized[i])).replace('/', '_').replace('\\', '_'));
        if (deployedResConfig != null && ((deployedResConfig.getConfigurationType() & Configuration.CONFIG_TYPE_PROPERTYSHEET) == 0)) {
          loadProperties = false;
        }
      } catch (NameNotFoundException e) {
        // $JL-EXC$ OK there are no deployed resources
        continue;
      } catch (ConfigurationException e) {
        throw new WCEConfigurationException("Failed to get the WCE configuration for [" + applicationName + "] application.", e);
      }

      if (loadProperties) {
        try {
          Hashtable<String, Vector<String>> deployedResources = new Hashtable<String, Vector<String>>();

          PropertySheet propertySheet = deployedResConfig.getPropertySheetInterface();
          Properties props = propertySheet.getProperties();
          Enumeration enumeration = props.keys();
          while (enumeration.hasMoreElements()) {
            // Add info to the local cache
            String resourceName = (String) enumeration.nextElement();
            String resourceType = props.getProperty(resourceName);

            resourceName = resourceName.substring(2);
            Vector<String> types = (Vector<String>) deployedResources.get(resourceName);
            if (types == null) {
              deployedResources.put(resourceName, new Vector<String>(Arrays.asList(new String[] { resourceType })));
            } else {
              types.add(resourceType);
              deployedResources.put(resourceName, types);
            }
          }

          if (!deployedResources.isEmpty()) {
            resourcesPerModule.put(aliasesCanonicalized[i], deployedResources);
          }
        } catch (ConfigurationException e) {
          throw new WCEConfigurationException("Cannot load provided resources for [" + applicationName + "] application", e);
        }
      }
    }
    if (!resourcesPerModule.isEmpty()) {
      resourcesPerApplication.put(applicationName, resourcesPerModule);
    }
    return resourcesPerModule;
  }

  public void removeWebModuleContextFromCache(String alias) {
  	startedWebApplications.remove(alias);
  	if (traceLocation.beDebug()) {
  		traceLocation.debugT("Web module context for alias [" + alias +
  		"] is removed from cache and in order to obtain it later its application will be started.");
  	}
  }

  private String[] getWebModules(String[] aliases, File[] appRoots, WebContainerExtensionWrapper wrapper, HashMap<String, String[]> webModulesPerWCE) {
    String wceName = wrapper.getWebContainerExtensionName();
  	if (webModulesPerWCE.containsKey(wceName)) {
    	return webModulesPerWCE.get(wceName);
    }

  	Vector<String> descriptorNames = wrapper.getDescriptorNames();
  	ArrayList<String> result = new ArrayList<String>();
  	for (int i = 0; i < aliases.length; i++) {//Iterate over the web modules of this application
    	if (WebContainerHelper.isDescriptorExist(descriptorNames, appRoots[i].getAbsolutePath())) {
    		result.add(aliases[i]);
    	}
		}

  	String[] array = (String[])result.toArray(new String[result.size()]);
  	webModulesPerWCE.put(wceName, array);
  	return array;
  }

  /**
   * Add <CODE>webModule</CODE> which corresponds to alias and applicationName into
   * the hashtable with deployed applications. The insert is taking into a consideration
   * the order (the prios) of the applications when there are more than one web module
   * per alias  (see {@link WebContainerProvider#deployedWebApplications})
   *
   * @param applicationName
   * @param alias
   * @param webModule
   */
  private void addDeployedAppl(String applicationName, String alias, WebModule webModule) {
    //TODO: reimplementne - check for default and then search over the table
    if (deployedWebApplications.get(alias) != null) {
      if (alias.equalsIgnoreCase("/")) {
        if (applicationName.equalsIgnoreCase(WebContainer.sapDefaultApplName)) {
          // sap default application at the end
          Vector<WebModule> webModules = deployedWebApplications.get(alias);
          webModules.add(webModule);
          deployedWebApplications.put(alias, webModules);
        } else {
          // customer default application in the beginning
          Vector<WebModule> webModules = deployedWebApplications.get(alias);
          webModules.add(0, webModule);
          deployedWebApplications.put(alias, webModules);
        }
      } else {
        // should never happen -> doubling of the aliases
        // TODO throw exception
      }
    } else {
      // no doubling of the aliases
      Vector<WebModule> webModules = new Vector<WebModule>(1, 1);
      webModules.add(webModule);
      deployedWebApplications.put(alias, webModules);
    }
  }

  /**
   * Returns the webModule for given <CODE>alias</CODE> and <CODE>applicationName</CODE>
   *
   * @param alias
   * @param applicationName
   * @return
   */
  public WebModule getDeployedAppl(String alias, String applicationName) {
    Vector<WebModule> webModules = (Vector<WebModule>) deployedWebApplications.get(alias);
    // get the web module which is from the 'applicationName' application
    if (webModules != null) {
      for (WebModule module : webModules) {
        if (module.getWholeApplicationName().equalsIgnoreCase(applicationName)) {
          return module;
        }
      }
    }
    return null;
  }

  /**
   * Returns the webModule for given <CODE>alias</CODE>. In case of two modules
   * the method returns the one with higher prio
   *
   * @param alias
   * @return
   */
  public WebModule getDeployedAppl(String alias) {
    Vector<WebModule> modules = deployedWebApplications.get(alias);
    if (modules != null) {
      return modules.get(0);
    }
    return null;
  }


  /**
   * Returns the all webModule for given <CODE>alias</CODE>
   *
   * @param alias
   * @return
   */
  public Vector<WebModule> getDeployedAppls(String alias) {
    return deployedWebApplications.get(alias);
  }
}//end of class
