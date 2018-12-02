package com.sap.engine.interfaces.webservices.server.event;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;

/**
 * Title: EventListener
 * Description: This is an interface, representing the base methods of processing an event.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface EventListener {

  /**
   * The method returns the identifier of the protocol (or other pluggable element) and is used to construct the proper directory.
   *
   * @return String - the identifier
   */
  public String getOwnerIdentifier();

  /**
   * The method returns the listener identifier.
   *
   * @return String - the identifier
   */
  public String getIdentifier();

  /**
   * The method is used to define the type of the transaction.
   * In case of false: On the local server the transaction will go through both: runtime and persistent changes.
   * The event will be spread all over the cluster.
   * On the remote servers the transaction will be invoked again, but only to perform the runtime actions.
   * In case of true: On the local server the transaction will go only through persistent changes.
   * The application will be restarted, so that the change could become active in the whole cluster.
   *
   * @param event - the event, which is going to be processed
   * @return String - true or false to define the transaction type
   */
  public boolean needRestart(Event event);

  /**
   * This method is invoked to perform persistent (DB) actions in order to make the change active.
   * In case the process is expected to fail, WSDeploymentException should be thrown.
   * In case the process is expected to pass successfully, but some errors are generated during the execution, WSWarningException should be thrown.
   *
   * @param event - the event, which is going to be processed
   * @param dir - the corresponding directory
   * @param configuration - the corresponding DB configuration; in case of processing WSEvent or WSClient event, this is the corresponding ws or ws client DB configuration
   * @exception WSDeploymentException
   * @exception WSWarningException
   */
  public void makePersistentChanges(Event event, String dir, Configuration configuration) throws WSDeploymentException, WSWarningException;

  /**
   * This method is invoked to indicate that the makePersistentChanges phase has finished successfully.
   * Only WSWarningException is excpected to be thrown.
   *
   * @param eventId - the identifier of the event, which is being processed.
   * @exception WSWarningException
   */
  public void commitPersistentChanges(String eventId) throws WSWarningException;

  /**
   * This method is invoked to indicate that the makePersistentChanges phase has failed and the processing actions should be rolled back.
   * Only WSWarningException is excpected to be thrown.
   *
   * @param eventId - the identifier of the event, which is being processed
   * @exception WSWarningException
   */
  public void rollbackPersistentChanges(String eventId) throws WSWarningException;

  /**
   * This method is invoked to perform runtime actions in order to make the change active.
   * The methods of the runtime changes sections are executed after the persistent changes section.
   * The makeRuntimeChanges phase receives access to the DB configuration with the changes made in the makePersistentChanges phase.
   * In case the process is expected to fail, WSDeploymentException should be thrown.
   * In case the process is expected to pass successfully, but some errors are generated during the execution, WSWarningException should be thrown.
   *
   * @param event - the event, which is going to be processed
   * @param dir - the corresponding directory
   * @param configuration - the corresponding DB configuration; in case of processing WSEvent or WSClient event, this is the corresponding ws or ws client DB configuration
   * @exception WSDeploymentException
   * @exception WSWarningException
   */
  public void makeRuntimeChanges(Event event, String dir, Configuration configuration) throws WSDeploymentException, WSWarningException;

  /**
   * This method is invoked to perform runtime and file local system actions in order to make the change active on the remote servers.
   * The method is expected to perform the same actions as in makeRuntimeChanges phase.
   * The configuration gives access to some additional changes, that have been made already during the processing of the event on the local server.
   * Thus on the remote server some of the processing actions could be skipped and the additional changes could be activated more easily.
   * Thus if the listener wants to use
   * Only WSWarningException is excpected to be thrown
   *
   * @param event - the event, which is going to be processed
   * @param dir - the corresponding directory
   * @exception WSWarningException
   */
  public void notifyRuntimeChanges(Event event, String dir, Configuration configuration) throws WSWarningException;

  /**
   * This method is invoked to indicate that the makeRuntimeChanges phase has finished successfully.
   * Only WSWarningException is excpected to be thrown.
   *
   * @param eventId - the identifier of the event, which is being processed
   * @exception WSWarningException
   */
  public void commitRuntimeChanges(String eventId) throws WSWarningException;

  /**
   * This method is invoked to indicate that the makeRuntimeChanges phase has failed and the processing actions should be rolled back.
   * Only WSWarningException is excpected to be thrown.
   *
   * @param eventId - the identifier of the event, which is being processed
   * @exception WSWarningException
   */
  public void rollbackRuntimeChanges(String eventId) throws WSWarningException;

}
