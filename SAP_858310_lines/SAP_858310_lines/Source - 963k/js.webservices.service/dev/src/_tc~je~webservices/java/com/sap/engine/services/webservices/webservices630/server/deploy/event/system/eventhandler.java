package com.sap.engine.services.webservices.webservices630.server.deploy.event.system;

import com.sap.engine.interfaces.webservices.server.event.*;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.webservices630.server.deploy.common.WSBaseConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WebServicesConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsConstants;
import com.sap.engine.services.webservices.exceptions.*;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.services.webservices.runtime.definition.wsclient.WSClientRuntimeInfo;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.tc.logging.Location;

import java.rmi.RemoteException;
import java.util.Properties;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class EventHandler {

  private RuntimeTransactionContext transactionContext = new RuntimeTransactionContext();
  private DeployCommunicator deployCommunicator = null;

  public EventHandler() {
  }

  public EventHandler(DeployCommunicator deployCommunicator) {
    this.deployCommunicator = deployCommunicator;
  }

  public void init(DeployCommunicator deployCommunicator) {
    this.deployCommunicator = deployCommunicator;
  }

  public RuntimeTransactionContext getTransactionContext() {
    return transactionContext;
  }

  public void setTransactionContext(RuntimeTransactionContext transactionContext) {
    this.transactionContext = transactionContext;
  }

  public DeployCommunicator getDeployCommunicator() {
    return deployCommunicator;
  }

  public void setDeployCommunicator(DeployCommunicator deployCommunicator) {
    this.deployCommunicator = deployCommunicator;
  }

  public void handle(Event event) throws WSDeploymentException {
    event.setStateStamp(event.getEventId() + "_" + System.currentTimeMillis());
    if(event instanceof WSEvent) {
      handleWSEvent((WSEvent)event);
    } else if(event instanceof WSClientEvent) {
      handleWSClientEvent((WSClientEvent)event);
    }
  }

  private void handleWSEvent(WSEvent wsEvent) throws WSDeploymentException {
    String eventId = wsEvent.getEventId();
    String applicationName = wsEvent.getApplicationName();
    String wsName = wsEvent.getWebServiceName();

    String excMsg = "Error occurred, trying to handle web service event. Unable to make runtime changes for application " + applicationName + ", web service " + wsName + ". ";

    EventListener[] eventListeners = WSContainer.getEventContext().getEventListenerRegistry().listListeners(eventId);
    boolean needRestart = needRestart(wsEvent, eventListeners);

    WSRuntimeDefinition wsRuntimeDefinition = null;
    try {
      wsRuntimeDefinition = WSContainer.getWSRegistry().getWebService(applicationName, wsName);
    } catch(RegistryException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get (find) web service. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsDir = wsRuntimeDefinition.getWsDirsHandler().getWsDirectory();
    String[] listenersDirs = getListenerDirs(wsDir, getOwnerIds(eventListeners));

    Configuration appConfiguration = null;
    try {
      appConfiguration = deployCommunicator.startRuntimeChanges(applicationName);
    } catch(DeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Deploy Service can not start runtime changes. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    Configuration wsConfiguration = null;
    Configuration rootEventConfiguration = null;
    Configuration wsEventConfiguration = null;
    try {
      String wsConfigName = wsRuntimeDefinition.getWsDirsHandler().getWSConfigName();
      wsEvent.setWSConfigName(wsConfigName);
      wsConfiguration = getWSConfiguration(wsConfigName, appConfiguration);
      rootEventConfiguration = getRootEventConfiguration(appConfiguration);
      wsEventConfiguration = getWSEventConfiguration(wsConfigName, appConfiguration);
    } catch(Throwable th) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(th);

      String rawMsg = "Unable to get web service, \"" + WSBaseConstants.EVENT_CONFIG_NAME + "\" or web service event configuration. ";

      try {
        deployCommunicator.rollbackRuntimeChanges(applicationName);
      } catch(RemoteException rExc) {
        wsDeployLocation.catching(rExc);

        String msg = excMsg + "Deploy Service could not rollback runtime changes. Reason for rollback is: " + rawMsg;
        Object[] args = new String[]{msg, "none"};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, rExc);
      }

      String msg = excMsg + rawMsg;
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, th);
    }

    if (wsConfiguration == null || wsEventConfiguration == null || rootEventConfiguration == null) {
      String rawMsg = "Web Service, \"" + WSBaseConstants.EVENT_CONFIG_NAME + "\" or web service event DB configuration is null. ";

      try {
        deployCommunicator.rollbackRuntimeChanges(applicationName);
      } catch(RemoteException rExc) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(rExc);

        String msg = excMsg + "Deploy Service could not rollback runtime changes. Reason for rollback is: " + rawMsg;
        Object[] args = new String[]{msg, "none"};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, rExc);
      }

      String msg = excMsg + rawMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }

    Transaction transaction = null;
    if(needRestart) {
      transaction = new PersistentTransaction(wsEvent, listenersDirs, wsConfiguration, wsEventConfiguration, rootEventConfiguration, eventListeners);
    } else {
      transaction = new Runtime_PersistentTransaction(wsEvent, listenersDirs, wsConfiguration, wsEventConfiguration, rootEventConfiguration, eventListeners);
    }


    transactionContext.putTransaction(applicationName, transaction);

    try {
      deployCommunicator.makeRuntimeChanges(applicationName, needRestart);
    } catch(RemoteException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Deploy Service could not make runtime changes. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private void handleWSClientEvent(WSClientEvent wsClientEvent) throws WSDeploymentException {
    String eventId = wsClientEvent.getEventId();
    String applicationName = wsClientEvent.getApplicationName();
    String wsClientName = wsClientEvent.getWSClientName();

    String excMsg = "Error occurred, trying to handle web service event. Unable to make runtime changes for application " + applicationName + ", web service " + wsClientName + ". ";

    EventListener[] eventListeners = WSContainer.getEventContext().getEventListenerRegistry().listListeners(eventId);
    boolean needRestart = needRestart(wsClientEvent, eventListeners);

    WSClientRuntimeInfo wsClientRuntimeInfo = null;
    try {
      wsClientRuntimeInfo = WSContainer.getWsClientRegistry().getWSClient(applicationName, wsClientName);
    } catch(RegistryException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get (find) web service. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsClientDir = wsClientRuntimeInfo.getWsClientDirsHandler().getWsClientDirectory();
    String[] listenersDirs = getListenerDirs(wsClientDir, getOwnerIds(eventListeners));

    Configuration appConfiguration = null;
    try {
      appConfiguration = deployCommunicator.startRuntimeChanges(applicationName);
    } catch(DeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Deploy Service can not start runtime changes. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    Configuration wsClientConfiguration = null;
    Configuration rootEventConfiguration = null;
    Configuration wsClientEventConfiguration = null;
    try {
      String wsClientConfigName = wsClientRuntimeInfo.getWsClientDirsHandler().getWsClientConfigName();
      wsClientEvent.setWSClientConfigName(wsClientConfigName);
      wsClientConfiguration = getWSClientConfiguration(wsClientConfigName, appConfiguration);
      rootEventConfiguration = getRootEventConfiguration(appConfiguration);
      wsClientEventConfiguration = getWSClientEventConfiguration(wsClientRuntimeInfo.getWsClientDirsHandler().getWsClientConfigName(), appConfiguration);
    } catch(Throwable th) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(th);

      String rawMsg = "Unable to get ws client, \"" + WSBaseConstants.EVENT_CONFIG_NAME + "\" or ws client event configuration. ";

      try {
        deployCommunicator.rollbackRuntimeChanges(applicationName);
        if (true ) {
          throw new RemoteException();
        }
      } catch(RemoteException rExc) {
        wsDeployLocation.catching(rExc);

        String intMsg = excMsg + "Deploy Service could not rollback runtime changes. Reason for rollback is: " + rawMsg;
        Object[] args = new String[]{intMsg, "none"};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, rExc);
      }

      String msg = excMsg + rawMsg;
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, th);
    }

    if (wsClientConfiguration == null || wsClientEventConfiguration == null) {
      String rawMsg = "WS client, \"" + WSBaseConstants.EVENT_CONFIG_NAME + "\" or ws client event DB configuration is null. ";

      try {
        deployCommunicator.rollbackRuntimeChanges(applicationName);
      } catch(RemoteException rExc) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(rExc);

        String intMsg = excMsg + "Deploy Service could not rollback runtime changes. Reason for rollback is: " + rawMsg;
        Object[] args = new String[]{intMsg, "none"};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, rExc);
      }

      String msg = excMsg + rawMsg;
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }

    Transaction transaction = null;
    if(needRestart) {
      transaction = new PersistentTransaction(wsClientEvent, listenersDirs, wsClientConfiguration, wsClientEventConfiguration, rootEventConfiguration, eventListeners);
    } else {
      transaction = new Runtime_PersistentTransaction(wsClientEvent, listenersDirs, wsClientConfiguration, wsClientEventConfiguration, rootEventConfiguration, eventListeners);
    }

    transactionContext.putTransaction(applicationName, transaction);

    try {
      deployCommunicator.makeRuntimeChanges(applicationName, needRestart);
    } catch(RemoteException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Deploy Service could not make runtime changes. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void makeRuntimeChanges(String applicationName) throws WSDeploymentException, WSWarningException {
    String excMsg = "Error occurred, making runtime changes for application: " + applicationName + ". ";
    Transaction transaction = transactionContext.getTransaction(applicationName);
    if(transaction == null) {
      String msg = excMsg + "Unexpected error - no transaction registered for the application. ";

      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }

    transaction.make();
  }

  public void commitRuntimeChanges(String applicationName) throws WSWarningException {
    String excMsg = "Error occurred, making runtime changes (commit phase) for application: " + applicationName + ". ";

    Transaction transaction = transactionContext.getTransaction(applicationName);
    if(transaction == null) {
      String msg = excMsg + "Unexpected error - no transaction registered for the application. ";

      WSWarningException e = new WSWarningException();
      e.addWarning(msg);
      throw e;
    }

    transaction.commit();

    transactionContext.removeTransaction(applicationName);
  }

  public void rollbackRuntimeChanges(String applicationName) throws WSWarningException {
    String excMsg = "Error occurred, making runtime changes (rollback phase) for application: " + applicationName + ". " ;

    Transaction transaction = transactionContext.getTransaction(applicationName);
    if(transaction == null) {
      String msg = excMsg + "Unexpected error - no transaction registered for the application. ";

      WSWarningException e = new WSWarningException();
      e.addWarning(msg);
      throw e;
    }

    transaction.rollback();

    transactionContext.removeTransaction(applicationName);
  }

  public void notifyRuntimeChanges(String applicationName, Configuration appConfiguration) throws WSWarningException {
    String excMsg = "Error occurred, making runtime changes (makeRemote phase) for application: " + applicationName + ". ";

    Event event = null;
    try {
      event = loadMostRecentEvent(applicationName, appConfiguration);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);
      e.printStackTrace();

      String msg = excMsg + "Unable to load the most recent event from DB. ";

      WSWarningException wExc = new WSWarningException(e);
      wExc.addWarning(msg);
      throw wExc;
    }

    try {
      handleRemoteEvent(event, appConfiguration);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to process event. Event id: " + event.getEventId() + ", event state stamp " + event.getStateStamp() + ". ";

      WSWarningException wExc = new WSWarningException(e);
      wExc.addWarning(msg);
      throw wExc;
    }

    Transaction transaction = transactionContext.getTransaction(applicationName);
    if(transaction == null) {
      String msg = excMsg + "Unexpected error - no transaction registered for the application. ";

      WSWarningException e = new WSWarningException();
      e.addWarning(msg);
      throw e;
    }

    transaction.makeRemote();

    transactionContext.removeTransaction(applicationName);
  }

  public void handleRemoteEvent(Event event, Configuration appConfiguration) throws WSDeploymentException {
    if(event instanceof WSEvent) {
      handleRemoteWSEvent((WSEvent)event, appConfiguration);
    } else if(event instanceof WSClientEvent) {
      handleRemoteWSClientEvent((WSClientEvent)event, appConfiguration);
    }
  }

  public void handleRemoteWSEvent(WSEvent wsEvent, Configuration appConfiguration) throws WSDeploymentException {
    String eventId = wsEvent.getEventId();
    String applicationName = wsEvent.getApplicationName();
    String wsName = wsEvent.getWebServiceName();

    String excMsg = "Error occurred, trying to handle web service event. Unable to make remote runtime changes for application " + applicationName + ", web service " + wsName + ". ";

    EventListener[] eventListeners = WSContainer.getEventContext().getEventListenerRegistry().listListeners(eventId);

    WSRuntimeDefinition wsRuntimeDefinition = null;
    try {
     wsRuntimeDefinition = WSContainer.getWSRegistry().getWebService(applicationName, wsName);
    } catch(RegistryException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get (find) web service. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsDir = wsRuntimeDefinition.getWsDirsHandler().getWsDirectory();
    String[] listenersDirs = getListenerDirs(wsDir, getOwnerIds(eventListeners));

    Configuration wsConfiguration = null;
    try {
      wsConfiguration = getWSConfiguration(wsRuntimeDefinition.getWsDirsHandler().getWSConfigName(), appConfiguration);
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get web service, \"" + WSBaseConstants.EVENT_CONFIG_NAME + "\" or web service event configuration. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    Runtime_PersistentTransaction transaction = new Runtime_PersistentTransaction(new RuntimeTransaction(wsEvent, listenersDirs, wsConfiguration, eventListeners), null);

    transactionContext.putTransaction(applicationName, transaction);
  }

  public void handleRemoteWSClientEvent(WSClientEvent wsClientEvent, Configuration appConfiguration) throws WSDeploymentException {
    String eventId = wsClientEvent.getEventId();
    String applicationName = wsClientEvent.getApplicationName();
    String wsClientName = wsClientEvent.getWSClientName();

    String excMsg = "Error occurred, trying to handle web service event. Unable to make remote runtime changes for application " + applicationName + ", web service " + wsClientName + ". ";

    EventListener[] eventListeners = WSContainer.getEventContext().getEventListenerRegistry().listListeners(eventId);

    WSClientRuntimeInfo wsClientRuntimeInfo = null;
    try {
      wsClientRuntimeInfo = WSContainer.getWsClientRegistry().getWSClient(applicationName, wsClientName);
    } catch(RegistryException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get (find) web service. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsClientDir = wsClientRuntimeInfo.getWsClientDirsHandler().getWsClientDirectory();

    String[] listenersDirs = getListenerDirs(wsClientDir, getOwnerIds(eventListeners));

    Configuration wsClientConfiguration = null;
    try {
      wsClientConfiguration = getWSClientConfiguration(wsClientRuntimeInfo.getWsClientDirsHandler().getWsClientConfigName(), appConfiguration);
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get web service, \"" + WSBaseConstants.EVENT_CONFIG_NAME + "\" or web service event configuration. ";
      Object[] args = new Object[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    Runtime_PersistentTransaction transaction = new Runtime_PersistentTransaction(new RuntimeTransaction(wsClientEvent, listenersDirs, wsClientConfiguration, eventListeners), null);

    transactionContext.putTransaction(applicationName, transaction);
  }

  private boolean needRestart(Event event, EventListener[] eventListeners) {
    if(eventListeners == null) {
      return false;
    }

    for(int i = 0; i < eventListeners.length; i++) {
      EventListener eventListener = eventListeners[i];
      if(eventListener.needRestart(event)) {
        return true;
      }
    }

    return false;
  }

  private Configuration getWSConfiguration(String wsConfigName, Configuration appConfiguration) throws ConfigurationException {
    Configuration wsConfiguration = null;

    Configuration wsContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesConstants.WS_CONTAINER_NAME);
    Configuration webservicesConfiguration = null;
    if(wsContainerConfiguration != null) {
      webservicesConfiguration = wsContainerConfiguration.getSubConfiguration(WebServicesConstants.WEBSERVICES_CONFIG_NAME);
    }
    if(webservicesConfiguration!= null) {
      wsConfiguration = webservicesConfiguration.getSubConfiguration(wsConfigName);
    }

    return wsConfiguration;
  }

  private Configuration getWSClientConfiguration(String wsClientConfigName, Configuration appConfiguration) throws ConfigurationException {
    Configuration wsConfiguration = null;

    Configuration wsContainerConfiguration = appConfiguration.getSubConfiguration(WSClientsConstants.WS_CONTAINER_NAME);
    Configuration wsClientsConfiguration = null;
    if(wsContainerConfiguration != null) {
      wsClientsConfiguration = wsContainerConfiguration.getSubConfiguration(WSClientsConstants.WS_CLIENTS_CONFIG_NAME);
    }
    if(wsClientsConfiguration!= null) {
      wsConfiguration = wsClientsConfiguration.getSubConfiguration(wsClientConfigName);
    }

    return wsConfiguration;
  }

  private Configuration getRootEventConfiguration(Configuration appConfiguration) throws ConfigurationException  {
    Configuration wsContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesConstants.WS_CONTAINER_NAME);
    Configuration eventsConfiguration = null;
    if(wsContainerConfiguration != null) {
      if (wsContainerConfiguration.existsSubConfiguration(WebServicesConstants.EVENT_CONFIG_NAME)) {
        eventsConfiguration = wsContainerConfiguration.getSubConfiguration(WebServicesConstants.EVENT_CONFIG_NAME);
      } else {
        eventsConfiguration = wsContainerConfiguration.createSubConfiguration(WebServicesConstants.EVENT_CONFIG_NAME);
      }
    }

    return eventsConfiguration;
  }

  private Configuration getWSEventConfiguration(String wsName, Configuration appConfiguration) throws ConfigurationException {
    Configuration wsConfiguration = null;

    Configuration wsContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesConstants.WS_CONTAINER_NAME);
    Configuration eventsConfiguration = null;
    if(wsContainerConfiguration != null) {
      if (wsContainerConfiguration.existsSubConfiguration(WebServicesConstants.EVENT_CONFIG_NAME)) {
        eventsConfiguration = wsContainerConfiguration.getSubConfiguration(WebServicesConstants.EVENT_CONFIG_NAME);
      } else {
        eventsConfiguration = wsContainerConfiguration.createSubConfiguration(WebServicesConstants.EVENT_CONFIG_NAME);
      }
    }
    Configuration webservicesConfiguration = null;
    if(eventsConfiguration!= null) {
      if(eventsConfiguration.existsSubConfiguration(WebServicesConstants.WEBSERVICES_CONFIG_NAME)) {
        webservicesConfiguration = eventsConfiguration.getSubConfiguration(WebServicesConstants.WEBSERVICES_CONFIG_NAME);
      } else {
        webservicesConfiguration = eventsConfiguration.createSubConfiguration(WebServicesConstants.WEBSERVICES_CONFIG_NAME);
      }
    }
    if(webservicesConfiguration!= null) {
      if(webservicesConfiguration.existsSubConfiguration(wsName)) {
      wsConfiguration = webservicesConfiguration.getSubConfiguration(wsName);
      } else {
        wsConfiguration = webservicesConfiguration.createSubConfiguration(wsName);
      }
    }

    return wsConfiguration;
  }

  private Configuration getWSClientEventConfiguration(String wsClientName, Configuration appConfiguration) throws ConfigurationException {
    Configuration wsClientConfiguration = null;

    Configuration wsContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesConstants.WS_CONTAINER_NAME);
    Configuration eventsConfiguration = null;
    if(wsContainerConfiguration != null) {
      if (wsContainerConfiguration.existsSubConfiguration(WebServicesConstants.EVENT_CONFIG_NAME)) {
        eventsConfiguration = wsContainerConfiguration.getSubConfiguration(WebServicesConstants.EVENT_CONFIG_NAME);
      } else {
        eventsConfiguration = wsContainerConfiguration.createSubConfiguration(WebServicesConstants.EVENT_CONFIG_NAME);
      }
    }
    Configuration wsClientsConfiguration = null;
    if(eventsConfiguration!= null) {
      if(eventsConfiguration.existsSubConfiguration(WSClientsConstants.WS_CLIENTS_CONFIG_NAME)) {
        wsClientsConfiguration = eventsConfiguration.getSubConfiguration(WSClientsConstants.WS_CLIENTS_CONFIG_NAME);
      } else {
        wsClientsConfiguration = eventsConfiguration.createSubConfiguration(WSClientsConstants.WS_CLIENTS_CONFIG_NAME);
      }
    }
    if(wsClientsConfiguration!= null) {
      if(wsClientsConfiguration.existsSubConfiguration(wsClientName)) {
        wsClientConfiguration = wsClientsConfiguration.getSubConfiguration(wsClientName);
      } else {
        wsClientConfiguration = wsClientsConfiguration.createSubConfiguration(wsClientName);
      }
    }

    return wsClientConfiguration;
  }

  private Event loadMostRecentEvent(String applicationName, Configuration appConfiguration) throws WSDeploymentException, WSWarningException {
    String excMsg = "Error occurred, trying to load the most recent event for application: " + applicationName + ". ";

    Properties props = null;
    try {
      props = loadMostRecentEventProps(appConfiguration);
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to load the most recent event properties. ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String eventStateStamp = props.getProperty(WSBaseConstants.EVENT_STATE_STAMP);
    String eventId = props.getProperty(WSBaseConstants.EVENT_IDENTIFIER);
    Configuration eventConfiguration = null;

    String wsConfigName = null;
    String type = props.getProperty(WSBaseConstants.TYPE);
    try {
      if(type.equals(WebServicesConstants.WEBSERVICES_CONFIG_NAME)) {
        wsConfigName = props.getProperty(WSBaseConstants.WS_CONFIG_NAME);
        eventConfiguration = getWSEventConfiguration(wsConfigName, appConfiguration);
      }
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get web service event configuration with state stamp: " + eventStateStamp + ". ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    String wsClientConfigName = null;
    try {
       if (type.equals(WSClientsConstants.WS_CLIENTS_CONFIG_NAME)) {
        wsClientConfigName = props.getProperty(WSBaseConstants.WS_CLIENT_CONFIG_NAME);
        eventConfiguration = getWSClientEventConfiguration(wsClientConfigName, appConfiguration);
      }
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get ws client event configuration with state stamp: " + eventStateStamp + ". ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }


    Configuration instanceEventConfiguration = null;
    try {
      instanceEventConfiguration = eventConfiguration.getSubConfiguration(eventStateStamp);
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get event instance DB configuration with state stamp: " + eventStateStamp + ". ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    EventFactory eventFactory = WSContainer.getEventContext().getEventFactoryRegistry().getFactory(eventId);
    if (eventFactory == null) {
      String msg = excMsg + "No event factory registered for event " + eventId + ".";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args);
    }

    Event event = null;
    try {
      event = eventFactory.load(instanceEventConfiguration);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Event factory could not load event " + eventStateStamp + ". ";
      Object[] args = new String[]{msg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } catch(WSWarningException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching("Warning! ", e);

      String msg = excMsg + "Event factory generated some errors, loading event " + eventStateStamp + ". ";
      WSWarningException wExc = new WSWarningException(e);
      wExc.addWarnings(WSUtil.addPrefixToStrings(msg, e.getWarningsVector()));
      throw wExc;
    }

    return event;
  }

  private Properties loadMostRecentEventProps(Configuration appConfiguration) throws ConfigurationException {
    Configuration rootEventConfiguration = getRootEventConfiguration(appConfiguration);
    return (Properties)rootEventConfiguration.getConfigEntry(WSBaseConstants.MOST_RECENT_EVENT);
  }

  private String[] getOwnerIds(EventListener[] eventListeners) {
    if(eventListeners == null) {
      return new String[0];
    }

    String[] listenersIds = new String[eventListeners.length];
    for(int i = 0; i < eventListeners.length; i++) {
      listenersIds[i] = eventListeners[i].getOwnerIdentifier();
    }

    return listenersIds;
  }

  private String[] getDirs(String baseDir, String[] relativeDirs) {
    if(baseDir == null) {
      return new String[0];
    }

    String[] dirs = new String[relativeDirs.length];
    for(int i = 0; i < relativeDirs.length; i++) {
      String relativeDir = relativeDirs[i];
      String dir = baseDir + WSBaseConstants.SEPARATOR + relativeDir;
      dirs[i] = dir;
    }

    return dirs;
  }

  private String[] getListenerDirs(String baseDir, String[] listenerIds) {
    if(listenerIds == null) {
      return new String[0];
    }

    String[] listenerRelDirs = new String[listenerIds.length];
    for(int i = 0; i < listenerIds.length; i++) {
      listenerRelDirs[i] = WSUtil.replaceForbiddenChars(listenerIds[i]);
    }

    return getDirs(baseDir, listenerRelDirs);
  }

}
