package com.sap.engine.interfaces.webservices.server.deploy.wsclient;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;

/**
 * Title: WSClientLCMInterface
 * Description: This interface provides methods for notification on ws client start and stop phase.
 * Any component that is interested in notifying on these phases, should implement the interface.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSClientLCMInterface {

  /**
   * This method is invoked to start downloading persistent data from DB.
   * If the downloading phase is expected to fail WSDeploymentException should be thrown.
   * Otherwise if the phase is expected to finish successfully, but some warnings are generated during the process, WSWarningException should be thrown.
   *
   * @param applicationName - The name of the application, that the ws client belongs to.
   * @param wsClientName  - The name of the ws client, that is being deployed.
   * @param dir - The name of the directory, which should be used by the component to store its data on the local file system.
   * @param wsClientConfiguration - The ws client configuration, which should be used by the component to create its own subconfiguration and store its persistent data.
   * @exception WSDeploymentException - Such an exception should be thrown, when the deployment is going to fail.
   * @exception WSWarningException - Such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process.
   */

  public void downloadFiles(String applicationName, String wsClientName, String dir, Configuration wsClientConfiguration)
    throws WSDeploymentException, WSWarningException;

  /**
   * This method is invoked on a ws client initioal start.
   * If the start phase is expected to fail WSDeploymentException should be thrown.
   * Otherwise if the phase is expected to finish successfully, but some warnings are generated during the process, WSWarningException should be thrown.
   *
   * @param applicationName - The name of the application, that the ws client belongs to.
   * @param wsClientName  - The name of the ws client, that is being deployed.
   * @param wsClientContext -  The context, that provides the deployment information of the ws client.
   * @param dir - The name of the directory, which should be used by the component to store its data on the local file system.
   * @param wsClientConfiguration - The ws client configuration, which should be used by the component to create its own subconfiguration and store its persistent data.
   * @exception WSDeploymentException - Such an exception should be thrown, when the deployment is going to fail.
   * @exception WSWarningException - Such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process.
   */

  public void onMakeStartInitially(String applicationName, String wsClientName, WSClientContext wsClientContext, String dir, Configuration wsClientConfiguration)
   throws WSDeploymentException, WSWarningException;

  /**
   * This method is invoked on a ws client start.
   * If the start phase is expected to fail WSDeploymentException should be thrown.
   * Otherwise if the phase is expected to finish successfully, but some warnings are generated during the process, WSWarningException should be thrown.
   *
   * @param applicationName - The name of the application, that the ws client belongs to.
   * @param wsClientName  - The name of the ws client, that is being deployed.
   * @param wsClientContext -  The context, that provides the deployment information of the ws client.
   * @param dir - The name of the directory, which should be used by the component to store its data on the local file system.
   * @param wsClientConfiguration - The ws client configuration, which should be used by the component to create its own subconfiguration and store its persistent data.
   * @exception WSDeploymentException - Such an exception should be thrown, when the deployment is going to fail.
   * @exception WSWarningException - Such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process.
   */

  public void onStart(String applicationName, String wsClientName, WSClientContext wsClientContext, String dir, Configuration wsClientConfiguration)
   throws WSDeploymentException, WSWarningException;

  /**
   * This method is invoked to notify that the ws client start phase has passed successfully.
   * Only WSWarningException is possible to be thrown, if some errors have been generated.
   *
   * @param applicationName - The name of the application, that the ws client belongs to.
   * @param wsClientName  - The name of the ws client, that is being deployed.
   * @exception WSWarningException - Such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process.
   */

  public void onCommitStart(String applicationName, String wsClientName)
    throws WSWarningException;

  /**
   * This method is invoked to notify that ws client start phase has failed.
   * Only WSWarningException is expected to be thrown, if some errors have been generated.
   *
   * @param applicationName - The name of the application, that the ws client belongs to.
   * @param wsClientName  - The name of the ws client, that is being deployed.
   * @exception WSWarningException - Such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process.
   */

  public void onRollbackStart(String applicationName, String wsClientName) throws WSWarningException;

  /**
   * This method is invoked on a ws client stop.
   * Only WSWarningException is expected to be thrown, if some errors have been generated.
   *
   * @param applicationName - The name of the application, that the ws client belongs to.
   * @param wsClientName  - The name of the ws client, that is being deployed.
   * @exception WSWarningException - Such an exception should be thrown, when the deployment is going to pass successfully, but some warnings are genereted during the process.
   */

  void onStop (String applicationName, String wsClientName) throws WSWarningException;

}
