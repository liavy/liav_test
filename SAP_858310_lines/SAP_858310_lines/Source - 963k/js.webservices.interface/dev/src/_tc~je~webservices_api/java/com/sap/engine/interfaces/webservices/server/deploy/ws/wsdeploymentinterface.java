package com.sap.engine.interfaces.webservices.server.deploy.ws;

import com.sap.engine.frame.core.configuration.Configuration;
//import com.sap.engine.interfaces.webservices.esp.StaticConfigurationContext;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;

/**
 * Title: WSDeploymentInterface
 * Description: this interface provides methods for notification on web service deploy and remove phase;
 * any component that is interested in notifying on these phases, should implement the interface
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSDeploymentInterface {

//  /**
//   * This method is invoked on web service deployment.
//   * If the deploy phase is expected to fail WSDeploymentException should be thrown.
//   * Otherwise if the phase is expected to finish successfully, but some warnings are generated during the process, WSWarningException should be thrown.
//   *
//   * @param applicationName - the name of the application, that the web service belongs to
//   * @param webServiceName  - the name of the web services, that is being deployed
//   * @param seiStaticConfigurationContexts -  an array of static configuration contexts; each context provides access to the configuration data, specified in the web service deployment descriptor, for each service endpoint
//   * @param dir - the name of the directory, which should be used by the component to store its data on the local file system
//   * @param webServiceConfiguration - the web service configuration, which should be used by the component to create its own subconfiguration and store its persistent data
//   * @exception WSDeploymentException - such an exception should be thrown, when the deployment is going to fail
//   * @exception WSWarningException - such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process
//   */
//
//  public void onDeploy(String applicationName, String webServiceName, StaticConfigurationContext[] seiStaticConfigurationContexts, String dir, Configuration webServiceConfiguration)
//    throws WSDeploymentException, WSWarningException;

  /**
   * This method is invoked on web service deployment.
   * If the deploy phase is expected to fail WSDeploymentException should be thrown.
   * Otherwise if the phase is expected to finish successfully, but some warnings are generated during the process, WSWarningException should be thrown.
   *
   * @param applicationName - the name of the application, that the web service belongs to
   * @param webServiceName  - the name of the web service, that is being deployed
   * @param wsContext -  the context, that provides the deployment information of the web service
   * @param dir - the name of the directory, which should be used by the component to store its data on the local file system
   * @param webServiceConfiguration - the web service configuration, which should be used by the component to create its own subconfiguration and store its persistent data
   * @exception WSDeploymentException - such an exception should be thrown, when the deployment is going to fail
   * @exception WSWarningException - such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process
   * @deprecated
   */

  public void onDeploy(String applicationName, String webServiceName, WSContext wsContext, String dir, Configuration webServiceConfiguration)
    throws WSDeploymentException, WSWarningException;    

//  /**
//   * This method is invoked on web service deployment.
//   * If the deploy phase is expected to fail WSDeploymentException should be thrown.
//   * Otherwise if the phase is expected to finish successfully, but some warnings are generated during the process, WSWarningException should be thrown.
//   *
//   * @param applicationName - the name of the application, that the web service belongs to
//   * @param webServiceName  - the name of the web services, that is being deployed
//   * @param seiStaticConfigurationContexts -  an array of static configuration contexts; each context provides access to the configuration data, specified in the web service deployment descriptor, for each service endpoint
//   * @param dir - the name of the directory, which should be used by the component to store its data on the local file system
//   * @param webServiceConfiguration - the web service configuration, which should be used by the component to create its own subconfiguration and store its persistent data
//   * @exception WSDeploymentException - such an exception should be thrown, when the deployment is going to fail
//   * @exception WSWarningException - such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process
//   */
//
//  public void onPostDeploy(String applicationName, String webServiceName, StaticConfigurationContext[] seiStaticConfigurationContexts, String dir, Configuration webServiceConfiguration)
//    throws WSDeploymentException, WSWarningException;

  /**
   * This method is invoked after the first web service deploy phase.
   * Some initializations are expected to be done here that could make the whole deploy process fail.
   * If the post deploy phase is expected to fail WSDeploymentException should be thrown.
   * Otherwise if the phase is expected to finish successfully, but some warnings are generated during the process, WSWarningException should be thrown.
   *
   * @param applicationName - the name of the application, that the web service belongs to
   * @param webServiceName  - the name of the web services, that is being deployed
   * @param wsContext -  the context, that provides the deployment information of the web service
   * @param dir - the name of the directory, which should be used by the component to store its data on the local file system
   * @param webServiceConfiguration - the web service configuration, which should be used by the component to create its own subconfiguration and store its persistent data
   * @exception WSDeploymentException - such an exception should be thrown, when the deployment is going to fail
   * @exception WSWarningException - such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process
   * @deprecated
   */

  public void onPostDeploy(String applicationName, String webServiceName, WSContext wsContext, String dir, Configuration webServiceConfiguration)
    throws WSDeploymentException, WSWarningException;

  /**
   * This method is invoked to notify that the first and the second deployment phases have passed successfully.
   * Only WSWarningException is possible to be thrown, if some errors have been generated.
   *
   * @param applicationName - the name of the application, that the web service belongs to
   * @param webServiceName  - the name of the web services, that is being deployed
   * @exception WSWarningException - such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process
   */

  public void onCommitDeploy(String applicationName, String webServiceName)
    throws WSWarningException;

  /**
   * This method is invoked to notify that the web service deployment has failed. The deploy actions should be rolled back.
   * Only WSWarningException is possible to be thrown, if some errors have been generated.
   *
   * @param applicationName - the name of the application, that the web service belongs to
   * @param webServiceName  - the name of the web services, that is being deployed
   */

  public void onRollbackDeploy(String applicationName, String webServiceName)
    throws WSWarningException;

  /**
   * This method is invoked on application remove. The persistent stored data should be removed.
   * Only WSWarningException is possible to be thrown, if some errors have been generated.
   *
   * @param applicationName - the name of the application
   */

  public void onRemove(String applicationName) throws WSWarningException;

}

