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
package com.sap.engine.services.servlets_jsp.webcontainer_api.container;

import java.rmi.RemoteException;
import java.util.Set;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.services.deploy.container.ComponentReference;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.CyclicReferencesException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WCEConfigurationException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.extension.WCERuntimeChangesInterface;

/**
 * An interface for managing applications.
 *
 * @author Violeta Georgieva
 * @author Diyan Yordanov
 * @author Vera Buchkova
 * @version 7.10
 */
public interface IApplicationManager {

  /**
   * Represents MANUAL start up mode.
   */
  public static final int MANUAL = 1;

  /**
   * Represents LAZY start up mode.
   */
  public static final int LAZY = 2;

  /**
   * Represents ALWAYS start up mode.
   */
  public static final int ALWAYS = 3;

  /**
   * Returns <code>true</code> if the status of application is STARTED, otherwise returns <code>false</code>
   * except if the status is UNKNOWN then throws exception.
   *
   * @param appName the name of the application in the following format: "vendor" + "/" + "application name".
   * @return returns <code>true</code> if the status of application is STARTED, otherwise returns <code>false</code>
   *         except if the status is UNKNOWN then throws exception.
   * @throws WebContainerExtensionDeploymentException
   *         if the status is UNKNOWN then throw WebContainerExtensionDeploymentException
   */
  public boolean isStarted(String appName) throws WebContainerExtensionDeploymentException;

  /**
   * Returns <code>true</code> if the start-up mode of the application is MANUAL, otherwise - <code>false</code>.
   *
   * @param appName the name of the application in the following format: "vendor" + "/" + "application name".
   * @return returns <code>true</code> if the start up mode of the application is MANUAL, otherwise - <code>false</code>.
   * @throws RemoteException thrown if a problem occurs during the process.
   */
  public boolean isManualStarting(String appName) throws RemoteException;

  /**
   * Returns <code>true</code> if the start-up mode of the application is LAZY, otherwise - <code>false</code>.
   *
   * @param appName the name of the application in the following format: "vendor" + "/" + "application name".
   * @return returns <code>true</code> if the start up mode of the application is LAZY, otherwise - <code>false</code>.
   * @throws RemoteException thrown if a problem occurs during the process.
   */
  public boolean isLazyStarting(String appName) throws RemoteException;

  /**
   * Sets the start-up mode of the application.
   * This method must be used only for administration cases in all other cases an exception will be thrown that the operation is not possible.
   *
   * @param appName the name of the application in the following format: "vendor" + "/" + "application name".
   * @param mode    the start-up mode to be set.
   * @throws RemoteException thrown if a problem occurs during the process.
   */
  public void setStartUpMode(String appName, int mode) throws RemoteException;

  /**
   * Returns fail over value for this application.
   *
   * @param appName the name of the application in the following format: "vendor" + "/" + "application name".
   * @return returns fail over value for this application.
   * @throws WebContainerExtensionDeploymentException
   *          thrown if some problems occur during the process or there is no such deployed application.
   */
  public String getFailOver(String appName) throws WebContainerExtensionDeploymentException;

  /**
   * Locally starts an application on one server so that to be accessible for the clients.
   * Only deployed applications that are in status "STOPPED" or "IMPLICIT_STOPPED" can be started.
   * This method has synchronous implementation, i.e. its execution guarantees
   * that application is completely started (or stopped if exception is thrown).
   *
   * @param appName the name of the application to be started in the following format: "vendor" + "/" + "application name".
   * @throws WebContainerExtensionDeploymentException
   *          thrown if some problems occur during starting the application.
   * @deprecated
   */
  public void start(String appName) throws WebContainerExtensionDeploymentException;

  /**
   * Locally stops an application on one server so that it will not be accessible for any actions from client side.
   * Only deployed application can be stopped. This method does not remove the application from server though
   * application is not accessible until it is started again.
   * This method has synchronous implementation, i.e. its execution guarantees
   * that application is completely stopped (or its status is not changed if exception is thrown).
   *
   * @param appName the name of the application to be stopped in the following format: "vendor" + "/" + "application name".
   * @throws WebContainerExtensionDeploymentException
   *          thrown if some problems occur during stopping the application.
   * @deprecated
   */
  public void stop(String appName) throws WebContainerExtensionDeploymentException;

  /**
   * Obtain the references of this application.
   * If there are no references then <code>null</code> will be returned.
   *
   * @param appName the name of the application in the following format: "vendor" + "/" + "application name".
   * @return the references of this application. If there are no references then <code>null</code> will be returned.
   * @throws RemoteException thrown if a remote problem during getting process occurs
   *                         or the specified application is currently not deployed.
   */
  public ReferenceObjectImpl[] getReferences(String appName) throws RemoteException;

  /**
   * Obtain the dependants that have reference to this application.
   * If there are no dependants then <code>null</code> will be returned.
   *
   * @param appName the name of the application in the following format: "vendor" + "/" + "application name".
   * @return the dependants that have reference to this application.
   *         If there are no dependants then <code>null</code> will be returned.
   * @throws RemoteException thrown if a remote problem during getting process occurs
   *                         or the specified application is currently not deployed.
   */
  public ReferenceObjectImpl[] getReferencedBy(String appName) throws RemoteException;

  /**
   * This method returns the name of the application (in the following format: "vendor" + "/" + "application name")
   * which provides the resource needed.
   *
   * @param resourceName the name of the resource.
   * @param resourceType the type of the resource.
   * @return the name of the application. <code>null</code> will be returned if no one is providing it.
   */
  public String getApplicationProvidingResource(String resourceName, String resourceType);

  /**
   * Adds the given so called "functional" references to the specified application.
   * Will affect only the current server node and will not be persisted.
   *
   * @param applicationName the name of the application in the following format: "vendor" + "/" + "application name".
   * @param refObjs         <code>ReferenceObjectImpl</code>[], where refObjs[i] is the functional reference.
   * @throws CyclicReferencesException in case a cycle is detected.
   * @throws WebContainerExtensionDeploymentException
   *                                   in case the references cannot be registered.
   */
  public void addReferences(String applicationName, ReferenceObjectImpl[] refObjs) throws CyclicReferencesException, WebContainerExtensionDeploymentException;

  /**
   * Returns the references of the application "applicationName"  with type  "referenceType".
   * <code>referenceType</code> could be application, library, service, interface.
   *
   * @param applicationName the name of the application in the following format: "vendor" + "/" + "application name"
   * for which references will be returned.
   * @param referenceType type of references to be returned.
   * @return Set of  references of the application  with the specified type.
   */
  public Set<String> getReferences(String applicationName, ComponentReference referenceType);

  /**
   * Checks whether there is reference from one application to another. Both application
   * names passed as parameters are in the following format: "vendor" + "/" + "application name".
   *
   * @param fromApplication the name of the application in the following format: "vendor" + "/" + "application name",
   * to be checked to have reference to <code>toApplication</code>.
   * @param toApplication the name of the application in the following format: "vendor" + "/" + "application name",
   * to which a reference existence is checked.
   * @return true if <code>fromApplication</code> has reference to <code>toApplication</code>.
   */
  public boolean hasReference(String fromApplication, String toApplication);

	/**
	 * Gets this WCE provider's configuration for the passed application and the given web module
	 * opened for READ access only.
	 * If such configuration does not exist, NULL will be returned.
	 *
	 * <p>Note for this WCE configuration the followings :</p>
	 *
	 * <ol><li>It is reserved for this WCE provider only and it exists only for web modules in whose
	 * deployment this WCE provider participates.
	 * </li><li>It is created at deploy time; during update it is removed only if the web module is
	 * removed too. It will be automatically removed by the deploy service when removing the
	 * entire application.
	 * </li><li>It is the WCE Provider's responsibility to maintain this configuration up to date,
	 * e.g. by merging its contents with the new data after the application update.
	 * </li>
	 * <li>This configuration will be open for WRITE only during deploying/updating application or
	 * during runtime changes;
	 * in all other cases (which we recommend to be administration needs only)
	 * the configuration will be open for reading and should be obtained using this method.</li>
	 * <li>After obtaining the configuration for READ using this method it is the WCE Provider's
	 * responsibility to close it by closing the open root, e.g. using <code>wceConfig.getOpenRoot().close()</code>
	 * </li></ol>
	 *
	 * @param applicationName the name of the application in the following format: "vendor" + "/" + "application name",
	 * for which the sub configuration of the WCE app sub config to be returned
	 * @param webModuleName the name of the web module for which the sub configuration of the WCE app sub config to be returned
	 * @return the WCE provider's app sub config for the passed application and web module, or null if such configuration does not exist
	 * @throws WCEConfigurationException when it is not possible to return the configuration for read
	 */
	public Configuration getMyWebModuleConfigRead(String applicationName, String webModuleName) throws WCEConfigurationException;

	/**
	 * Initiates the so called WCE-runtime-changes-action on the WebContainer,
	 * similar to the containers' runtime changes action, but simplified.
	 * When executing this method, WebContainer will call the
	 * updateWCEWebModuleConfig on the passed WCERuntimeChangesInterface for each
	 * web module of the given application, in whose deployment this WCE Provider
	 * has participated.
	 * The exception throwing from this method can be used by the caller as an indicator
	 * whether the runtime changes have succeeded or failed (including because of
	 * the result from updateWCEWebModuleConfig, i.e. if updateWCEWebModuleConfig returns
	 * false, a WCEConfiguration exception will be thrown).
	 *
	 * @param applicationName the name of the application in the following format: "vendor" + "/" + "application name", of the web module.
	 * @param webModuleName the name of the web module whose configuration to be edited
	 * @param wceImpl the WCE Provider's implementation of the runtime update
	 * @param needsRestart if true WebContainer will restart the application
	 *                     after the runtime changes have been committed
	 * @throws WCEConfigurationException when the WCE runtime changes action failed in its start, commit or rollback phase.
	 */
	public void makeWCERuntimeChanges(String applicationName, String webModuleName, WCERuntimeChangesInterface wceImpl, boolean needsRestart) throws WCEConfigurationException;

}//end of interface
