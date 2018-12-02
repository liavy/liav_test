/*
 * Copyright (c) 2004-2006 by SAP AG, Walldorf.,
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
import java.util.Properties;
import java.util.Set;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.services.deploy.ApplicationInformation;
import com.sap.engine.services.deploy.DeployService;
import com.sap.engine.services.deploy.container.ComponentReference;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.ExceptionInfo;
import com.sap.engine.services.deploy.container.ReferenceObjectIntf;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.deploy.container.ReferenceType;
import com.sap.engine.services.deploy.container.op.util.FailOver;
import com.sap.engine.services.deploy.container.op.util.StartUp;
import com.sap.engine.services.deploy.container.op.util.Status;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.J2EEComponents;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEApplicationNotStartedException;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCECyclicReferencesException;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.exceptions.WCEDeploymentException;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.ReferenceObjectImpl;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.CyclicReferencesException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WCEConfigurationException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.extension.WCERuntimeChangesInterface;
import com.sap.tc.logging.Location;

/**
 * @author Violeta Georgieva
 * @version 7.10
 * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager
 */
public class ApplicationManager implements IApplicationManager {
	private static final Location currentLocation = Location.getLocation(ApplicationManager.class);
	private static final Location traceLocation = LogContext.getLocationWebContainerProvider();

  private WebContainerProvider webContainerProvider;
  private String wceProviderName = null;

  public ApplicationManager(WebContainerProvider webContainerProvider, String wceProviderName) {
  	this.webContainerProvider = webContainerProvider;
  	this.wceProviderName = wceProviderName;
  }//end of constructor

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#isStarted(java.lang.String)
   */
  public boolean isStarted(String appName) throws WCEApplicationNotStartedException {
    byte status = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStatus(appName);
    if (status == Status.STARTED.getId().byteValue()) {
      return true;
    } else if (status == Status.UNKNOWN.getId().byteValue()) {
      throw new WCEApplicationNotStartedException(WCEDeploymentException.STATUS_UNKNOWN, new Object[]{appName});
    } else {
      return false;
    }
  }//end of isStarted(String appName)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#isManualStarting(java.lang.String)
   */
  public boolean isManualStarting(String appName) throws RemoteException {
    boolean isManual = false;

    Properties appGlobalProps = (Properties) webContainerProvider.getApplicationsGlobalProperties().get(appName);
    if (appGlobalProps != null) {
      //it is called during deploy or update
      String startUpMode = appGlobalProps.getProperty("startupmode");
      if (startUpMode.equals(StartUp.MANUAL.getName())) {
    	isManual = true;
      }
    } else {
      //act as proxy to DC
      isManual = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(appName) == StartUp.MANUAL.getId().byteValue();
    }

	return isManual;
  }//end of isManualStarting(String appName)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#isLazyStarting(java.lang.String)
   */
  public boolean isLazyStarting(String appName) throws RemoteException {
	boolean isLazy = false;

	Properties appGlobalProps = (Properties) webContainerProvider.getApplicationsGlobalProperties().get(appName);
	if (appGlobalProps != null) {
    //application update
	  String startUpMode = appGlobalProps.getProperty("startupmode");
	  if (startUpMode.equals(StartUp.LAZY.getName())) {
		isLazy = true;
	  }
	} else {
	  isLazy = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(appName) == StartUp.LAZY.getId().byteValue();
	}

    return isLazy;
  }//end of isLazyStarting(String appName)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#setStartUpMode(java.lang.String, int)
   */
  public void setStartUpMode(String appName, int mode) throws RemoteException {
    int internalMode = StartUp.ALWAYS.getId().byteValue();

    if (mode == MANUAL) {
      internalMode = StartUp.MANUAL.getId().byteValue();
    } else if (mode == LAZY) {
      internalMode = StartUp.LAZY.getId().byteValue();
    }

    ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().setStartUpMode(appName, internalMode);
  }//end of setStartUpMode(String appName, int mode)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#getFailOver(String)
   */
  public String getFailOver(String appName) throws WebContainerExtensionDeploymentException {
    String failOverString = null;

    Properties appGlobalProps = (Properties) webContainerProvider.getApplicationsGlobalProperties().get(appName);
    if (appGlobalProps != null) {
      //deploy or update
      failOverString = appGlobalProps.getProperty("failover");
    } else {
    FailOver failover = null;
    try {
      failover = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getFailOver(appName);
        failOverString = failover.getName();
    } catch (DeploymentException e) {
      throw new WCEDeploymentException(WCEDeploymentException.CANNOT_GET_FAILOVER, new Object[]{appName}, e);
    }
    }

    return failOverString;
  }//end of isFailOverEnabled(String appName)

  /**
   * Starts the application local (only on this server node) or
   * global (in the whole instance) depending on "local" parameter.
   *
   * @param appName an application that has to be started.
   * @param local   if local is true then the application will be started only on this server node
   *                otherwise the application will be started in the whole instance.
   * @throws WCEApplicationNotStartedException
   *          thrown if some problems occur during starting the application.
   */
  public void start(String appName, boolean local) throws WCEApplicationNotStartedException {
    try {
      if (local) {
        ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().startApplicationLocalAndWait(appName);
      } else {
        ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().startApplicationAndWait(appName, ServiceContext.getServiceContext().getClusterContext().getAllServerNamesInInstance());
      }
    } catch (WarningException we) {
			if (traceLocation.beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceWarning("ASJ.web.000490", "There is an error starting application [{0}].", new Object[]{appName}, we, null, null);
			}
    } catch (RemoteException re) {
      throw new WCEApplicationNotStartedException(WCEApplicationNotStartedException.FAILED_EXPLICIT_START, new Object[]{appName}, re);
    }
  }//end of start(String appName, boolean local)

  /**
   * Stops the application local. Only on this server node.
   *
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#stop(java.lang.String)
   */
  public void stop(String appName) throws WebContainerExtensionDeploymentException {
    stop(appName, true);
  }//end of stop(String appName)

  /**
   * Stops the application local (only on this server node) or
   * global (in the whole instance) depending on "local" parameter.
   *
   * @param appName an application that has to be stopped.
   * @param local   if local is true then the application will be stopped only on this server node
   *                otherwise the application will be stopped in the whole instance.
   * @throws WebContainerExtensionDeploymentException
   *          thrown if some problems occur during stopping the application.
   */
  public void stop(String appName, boolean local) throws WebContainerExtensionDeploymentException {
    try {
      if (local) {
        ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().stopApplicationLocalAndWait(appName);
      } else {
        ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().stopApplicationAndWait(appName, ServiceContext.getServiceContext().getClusterContext().getAllServerNamesInInstance());
      }
    } catch (RemoteException re) {
      throw new WCEDeploymentException(WCEDeploymentException.FAILED_EXPLICIT_STOP, new Object[]{appName}, re);
    }
  }//end of stop(String appName, boolean local)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#getReferences(java.lang.String)
   */
  public ReferenceObjectImpl[] getReferences(String appName) throws RemoteException {
    DeployService deployService = (DeployService) ServiceContext.getServiceContext().getApplicationServiceContext().getContainerContext().getObjectRegistry().getServiceInterface(J2EEComponents.SERVICE_DEPLOY);
    ApplicationInformation appInfo = deployService.getApplicationInformation(appName);
    ReferenceObjectIntf[] referenceObject = appInfo.getReferences();
    ReferenceObjectImpl[] referenceObjectImpl = null;
    if (referenceObject != null) {
      referenceObjectImpl = new ReferenceObjectImpl[referenceObject.length];
      for (int i = 0; i < referenceObject.length; i++) {
        ReferenceObjectImpl refObj = new ReferenceObjectImpl(referenceObject[i].getReferenceTarget(), referenceObject[i].getReferenceTargetType(), referenceObject[i].getReferenceType());
        refObj.setReferenceProviderName(referenceObject[i].getReferenceProviderName());
        referenceObjectImpl[i] = refObj;
      }
    }
    return referenceObjectImpl;
  }//end of getReferences(String appName)

  public Set<String> getReferences(String applicationName, ComponentReference referenceType) {
    return ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getReferences(applicationName, referenceType);
  }

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#getReferencedBy(java.lang.String)
   */
  public ReferenceObjectImpl[] getReferencedBy(String appName) throws RemoteException {
    DeployService deployService = (DeployService) ServiceContext.getServiceContext().getApplicationServiceContext().getContainerContext().getObjectRegistry().getServiceInterface(J2EEComponents.SERVICE_DEPLOY);
    ApplicationInformation appInfo = deployService.getApplicationInformation(appName);
    ReferenceObjectIntf [] referenceObject = appInfo.getDependants();
    ReferenceObjectImpl[] referenceObjectImpl = null;
    if (referenceObject != null) {
      referenceObjectImpl = new ReferenceObjectImpl[referenceObject.length];
      for (int i = 0; i < referenceObject.length; i++) {
        ReferenceObjectImpl refObj = new ReferenceObjectImpl(referenceObject[i].getReferenceTarget(), referenceObject[i].getReferenceTargetType(), referenceObject[i].getReferenceType());
        refObj.setReferenceProviderName(referenceObject[i].getReferenceProviderName());
        referenceObjectImpl[i] = refObj;
      }
    }
    return referenceObjectImpl;
  }//end of getReferencedBy(String appName)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#getApplicationProvidingResource(java.lang.String, java.lang.String)
   */
  public String getApplicationProvidingResource(String resourceName, String resourceType) {
    return ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getApplicationProvidingResource(resourceName, resourceType);
  } //end of getApplicationProvidingResource(String resourceName, String resourceType)

  public void analyseAppStatusMode(WebModule webModule, boolean checkUnregistering) throws WCEApplicationNotStartedException, RemoteException {
    String applicationName = webModule.getWholeApplicationName();
  	if (isStarted(applicationName)) {
      return;
    } else if (isManualStarting(applicationName)) {
      throw new WCEApplicationNotStartedException(
        WCEApplicationNotStartedException.APP_WILL_NOT_START_BECAUSE_IN_MANUAL_MODE, new Object[]{applicationName});
    } else if (isLazyStarting(applicationName)) {
      //According to deploy service after this invocation the application must be
      //either in STARTED status mode or exception will be thrown if there are problems.
      //Additional synchronization is not needed

    	//Check if some WCE is in unregistering to not start the lazy application:
    	if (checkUnregistering) {
    		String uregisteringWCE = webContainerProvider.isUnregistering(webModule);
    		if (uregisteringWCE != null) {
    			throw new WCEApplicationNotStartedException(
    				WCEApplicationNotStartedException.APP_WILL_NOT_START_BECAUSE_WCE_UNREGISTERS, new Object[]{applicationName, uregisteringWCE});
    		} else {
    			start(applicationName, false);
    		}
    	} else {
    		start(applicationName, false);
    	}
    } else {
      if (!ServiceContext.getServiceContext().getWebContainer().allApplicationsStarted) {
        //According to deploy service after this invocation the application must be
        //either in STARTED status mode or exception will be thrown if there are problems.
        //Additional synchronization is not needed.
        start(applicationName, false);
      } else {
        ExceptionInfo exceptionInfo = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getExceptionInfo(applicationName);
        if (exceptionInfo != null) {
          throw new WCEApplicationNotStartedException(
            WCEApplicationNotStartedException.APP_WILL_NOT_START_BECAUSE_IN_ALWAYS_MODE_AND_FAILED_TO_START_INITIALLY,
            new Object[]{applicationName, exceptionInfo});
        } else {
          throw new WCEApplicationNotStartedException(
            WCEApplicationNotStartedException.APP_WILL_NOT_START_BECAUSE_IN_ALWAYS_MODE_AND_STOPPED_BY_ADMIN, new Object[]{applicationName});
        }
      }
    }
  }//end of analyseAppStatusMode(WebModule webModule, boolean checkUnregistering)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#addReferences(String, com.sap.engine.services.servlets_jsp.webcontainer_api.container.ReferenceObjectImpl[])
   */
  public void addReferences(String applicationName, ReferenceObjectImpl[] refObjs) throws CyclicReferencesException, WebContainerExtensionDeploymentException {
    if (applicationName == null || (refObjs == null || refObjs.length == 0)) {
      return;
    }

    ReferenceType[] characteristics = new ReferenceType[refObjs.length];
    for (int i = 0; i < refObjs.length; i++) {
      characteristics[i] = new ReferenceType(true, false, false);
    }

    try {
      ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().addReferences(applicationName, refObjs, characteristics);
    } catch (com.sap.engine.services.deploy.container.CyclicReferencesException e) {
      throw new WCECyclicReferencesException(WCECyclicReferencesException.CYCLIC_REFERENCES_HAVE_BEEN_DETECTED, e);
    } catch (DeploymentException e) {
      throw new WCEDeploymentException(WCEDeploymentException.REFERENCES_CANNOT_BE_REGISTERED, e);
    }
  }//end of addReferences(String applicationName, ReferenceObjectImpl[] refObjs)

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#hasReference(java.lang.String, java.lang.String)
   */
  public boolean hasReference(String fromApplication, String toApplication) {
    return ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().hasReference(fromApplication, toApplication);
  }//end of hasReference(String fromApplication, String toApplication)

	/**
	 * Implementation of IApplicationManager's method for getting the
	 * WCE Web Module configuration for read access.
	 * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#getMyWebModuleConfigRead(java.lang.String, java.lang.String)
	 */
	public Configuration getMyWebModuleConfigRead(String applicationName, String webModuleName) throws WCEConfigurationException {
  	DeployCommunicator dc = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator();
  	Configuration wceWMSubConfig = null;
  	Configuration wceAppConfig = null;
  	Configuration appConfig = null;
    Configuration servlet_jspConfig = null;
    try {
    	appConfig = dc.getAppConfigurationForReadAccess(applicationName);
			servlet_jspConfig = appConfig.getSubConfiguration(Constants.CONTAINER_NAME);
			wceAppConfig = servlet_jspConfig.getSubConfiguration(Constants.WCE_CONFIG_PREFIX + ConfigurationUtils.convertToConfigForm(wceProviderName));
			String wmConfigName = ConfigurationUtils.convertToConfigForm(ParseUtils.convertAlias(webModuleName));
			wceWMSubConfig = wceAppConfig.getSubConfiguration(Constants.WCE_WEBMODULE_CONFIG_PREFIX + wmConfigName);
		} catch (Exception e) {
			LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000040",
        "Cannot get the WCE configuration for [{0}] web module in [{1}] application and WCE [{2}].", 
        new Object[]{webModuleName, applicationName, wceProviderName}, e, null, null);
			if (appConfig != null) {
			  try {
			  	appConfig.close();
			  } catch (ConfigurationException e1) {
					if (traceLocation.beWarning()) {
						LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceWarning( "ASJ.web.000491",
						  "Cannot close the root configuration (open in read-only mode) for application [{0}].", new Object[]{applicationName}, null, null);
					}
			  }
			  throw new WCEConfigurationException("Failed to get the WCE configuration for [" + webModuleName + "] web module in [" + applicationName + "] application and WCE [" + wceProviderName + "] for read access.", e);
			}
		}

  	return wceWMSubConfig;
	}

	/**
	 * Implementation of IApplicationManager's method WCE's runtime changes.
	 * @see com.sap.engine.services.servlets_jsp.webcontainer_api.container.IApplicationManager#makeWCERuntimeChanges(java.lang.String, java.lang.String, com.sap.engine.services.servlets_jsp.webcontainer_api.extension.WCERuntimeChangesInterface, boolean)
	 */
	public void makeWCERuntimeChanges(String applicationName, String webModuleName, WCERuntimeChangesInterface wceImpl,
																																			boolean needsRestart) throws WCEConfigurationException {
		DeployCommunicator dc = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator();
		Configuration appConfig = null;

		try {
			appConfig = dc.startRuntimeChanges(applicationName);
		} catch (DeploymentException e) {
			if (traceLocation.beError()) {
				LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceError("ASJ.web.000425", 
				  "Failed to start the WCE runtime changes action.", e, null, null);
			}
			//to unlock the application if the locking succeeded but getting configuration failed:
			rollbackWCERuntimeChanges(applicationName, dc);
			throw new WCEConfigurationException("Failed to start the WCE runtime changes action and it was rolled back.", e);
		}

		boolean result = false; //result from runtime changes: if something/whatever fails it is false and needs to be rolled back

		try {
			Configuration servlet_jspConfig = appConfig.getSubConfiguration(Constants.CONTAINER_NAME);
			String wmConfigName = ConfigurationUtils.convertToConfigForm(ParseUtils.convertAlias(webModuleName));
			String wceAppConfigName = Constants.WCE_CONFIG_PREFIX + ConfigurationUtils.convertToConfigForm(wceProviderName);
			String wceWebModuleConfigName = Constants.WCE_WEBMODULE_CONFIG_PREFIX + wmConfigName;
			Configuration wceAppConfig = ConfigurationUtils.getSubConfiguration(servlet_jspConfig, wceAppConfigName, applicationName, true);
			Configuration wceWebModuleConfig = ConfigurationUtils.getSubConfiguration(wceAppConfig, wceWebModuleConfigName, applicationName, true);

			//The exception WCEConfigurationException thrown from the following line must be rethrown to the calling WCE Provider:
			result = wceImpl.updateWCEWebModuleConfig(applicationName, webModuleName, wceWebModuleConfig);

		} catch (Exception e) {
			if (traceLocation.beDebug()) {
				LogContext.getLocation(LogContext.LOCATION_WEBCONTAINERPROVIDER).traceDebug(
						"Failed runtime changes for WCE Provider [" + wceProviderName + "]'s updateWCEWebModuleConfig for webModuleName [" +
						webModuleName + "], needsRestart=[" + needsRestart + "].", e, applicationName);
			}
			rollbackWCERuntimeChanges(applicationName, dc);
			//TODO: koi e po vajen da se hvyrli nagore: exceptiona na rolbacka ili na
			//failnaloto predi nego, vkl. updateWCEWebModuleConfig?
			//tuka reshih che na rollbacka e po-vajen i toi se hvyrlia ako se poluchi i drugite samo se logvat
			//ako rollbacka mine, se hvyrlia istinskia exception e:
			throw new WCEConfigurationException("Failed runtime changes for WCE Provider ["
					+ wceProviderName + "]'s updateWCEWebModuleConfig for webModuleName [" + webModuleName + "].", e);
		}

		//No exception was thrown but still the result may be false:
		if (result) {
			commitWCERuntimeChanges(applicationName, dc, needsRestart);
		} else {
			rollbackWCERuntimeChanges(applicationName, dc);
			throw new WCEConfigurationException("Failed runtime changes for WCE Provider ["
					+ wceProviderName + "]'s updateWCEWebModuleConfig for webModuleName [" + webModuleName
					+ "].");
		}
	}

	private void rollbackWCERuntimeChanges(String applicationName, DeployCommunicator dc) throws WCEConfigurationException {
		try {
			dc.rollbackRuntimeChanges(applicationName);
		} catch (RemoteException e1) {
			throw new WCEConfigurationException("Failed to rollback the WCE runtime changes action: the application [" + applicationName + "] may remain locked.", e1);
		}
	}

	private void commitWCERuntimeChanges(String applicationName, DeployCommunicator dc, boolean needsRestart) throws WCEConfigurationException {
		try {
			dc.makeRuntimeChanges(applicationName, needsRestart);
		} catch (RemoteException e1) {
			throw new WCEConfigurationException("Failed to commit the WCE runtime changes action: the application [" + applicationName + "] may remain locked.", e1);
		}
	}

  public String getWCEProviderName() {
  	return wceProviderName;
  }

}//end of class
