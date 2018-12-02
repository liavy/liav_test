/**
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp;

import java.util.Vector;
import java.rmi.*;
//import com.inqmy.frame.RuntimeInterface;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.frame.state.ManagementInterface;

/**
 * This interface gives information about the settings of the web applications deployed in the web container
 * and gives ways for modifying them. It also provides information about the loaded servlet instances.
 *
 * @author Violeta Uzunowa
 * @version 6.30
 */
public interface WebContainerInterface extends Monitoring, ManagementInterface, Remote {

  /**
   * Lists servlets of specified web application. If the application is in stopped mode, this
   * method returns only those servlets that are specified in deployment descriptor
   *
   * @param aliasName name of web application
   * @return vector with all servlets
   */
  public Vector listServlets(String aliasName) throws RemoteException;


  /**
   * Lists jsp files of specified web application. If the application is in stopped mode
   * this method returns only those jsps that are specified in deployment descriptor
   *
   * @param aliasName name of  web application
   * @return vector with all jsp
   */
  public Vector listJsp(String aliasName) throws RemoteException;


  /**
   * Lists filters of specified web application
   *
   * @param aliasName name of  web application
   * @return vector with all filters
   */
  public Vector listFilters(String aliasName) throws RemoteException;


  /**
   * Lists listners of specified web application
   *
   * @param aliasName name of  web application
   * @return vector with all listeners
   */
  public Vector listListeners(String aliasName) throws RemoteException;


  /**
   * Save changes
   */
  public boolean save(String aliasName, WebDeploymentDescriptor descriptor) throws RemoteException;


  /**
   * Lists name of all applications deployed on web container
   *
   * @return vector with the applications
   */
  public Vector getAllApplications() throws RemoteException, DeploymentException;


  /**
   * Lists all aliases name of the specified application
   *
   * @param applicationName the name of the application
   * @return vector with the applications
   */
  public Vector getAliases(String applicationName) throws RemoteException;

  /**
   * Returns deployment descriptor of the specified web application
   *
   * @param aliasName the name of alias
   * @param applicationName the name of the application
   * @return deployment descriptor
   */
  public WebDeploymentDescriptor getDescriptor(String aliasName, String applicationName) throws RemoteException;

  /**
   * Returns deployment descriptor of the global-web.xml
   *
   * @return deployment descriptor
   */
  public WebDeploymentDescriptor getGlobalWebXml() throws RemoteException;

  /**
   * Checks if the specifed appliction is in stopped mode or not
   */
  public boolean isStopped(String applicationName) throws RemoteException;


  public void appStarted(String applicationName) throws RemoteException;


  public void removeApp(String applicationName) throws RemoteException;


  public void register(WebContainerRuntimeCallback admin);


  public void update(String appName) throws RemoteException;


  public void appStopped(String applicationName) throws RemoteException;

  public void registerHttpSessionDebugListener(HttpSessionDebugListener listener, String debugParamName);

  public void unregisterHttpSessionDebugListener();


}

