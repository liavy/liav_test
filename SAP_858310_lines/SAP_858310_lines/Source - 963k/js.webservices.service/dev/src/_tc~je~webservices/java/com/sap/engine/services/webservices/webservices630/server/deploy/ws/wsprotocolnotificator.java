package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import java.util.Hashtable;
import java.util.Vector;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.interfaces.webservices.runtime.OperationDefinition;
import com.sap.engine.interfaces.webservices.runtime.component.ComponentFactory;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.runtime.definition.FeatureInfo;
import com.sap.engine.services.webservices.runtime.definition.OperationDefinitionImpl;
import com.sap.engine.services.webservices.runtime.definition.ServiceEndpointDefinition;
import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.tc.logging.Location;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSProtocolNotificator {

  public static final String[] SKIPPED_PROTOCOLS_LIST = new String[]{""};

  public static final int DEPLOY          = 0;
  public static final int POST_DEPLOY     = 1;
  public static final int COMMIT_DEPLOY   = 2;
  public static final int ROLLBACK_DEPLOY = 3;
  public static final int REMOVE          = 4;
  public static final int DOWNLOAD        = 5;
  public static final int START           = 6;
  public static final int COMMIT_START    = 7;
  public static final int ROLLBACK_START  = 8;
  public static final int STOP            = 9;
  public static final int UPDATE          = 10;
  public static final int POST_UPDATE     = 11;
  public static final int COMMIT_UPDATE   = 12;
  public static final int ROLLBACK_UPDATE = 13;

  public WSProtocolNotificator() {
  }

  public static String getModeName(int mode) {
    String modeName = "";
    switch (mode) {
      case 0: {
        modeName = "deploy";
        break;
      }
      case 1: {
        modeName = "post-deploy";
        break;
      }
      case 2: {
        modeName = "commit-deploy";
        break;
      }
      case 3: {
        modeName = "rollback-deploy";
        break;
      }
      case 4: {
        modeName = "download";
        break;
      }
      case 5: {
        modeName = "start";
        break;
      }
      case 6: {
        modeName = "commit-start";
        break;
      }
      case 7: {
        modeName = "rollback-start";
        break;
      }
      case 8: {
        modeName = "stop";
        break;
      }
      case 9: {
        modeName = "remove";
        break;
      }
      case 10: {
        modeName = "update";
        break;
      }
      case 11: {
        modeName = "post-update";
        break;
      }
      case 12: {
        modeName = "commit-update";
        break;
      }
      case 13: {
        modeName = "rollback-update";
        break;
      }
    }

    return modeName;
  }

  public void onDeploy(String applicationName, WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration appConfiguration) throws WSWarningException {
    onDeployPhases(applicationName, wsRuntimeDefinitions, appConfiguration, DEPLOY);
  }

  public void onPostDeploy(String applicationName, WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration appConfiguration) throws WSWarningException {
    onDeployPhases(applicationName, wsRuntimeDefinitions, appConfiguration, POST_DEPLOY);
  }

  public void onDeploy(WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration[] wsConfigurations) throws WSWarningException {
    onDeployPhases(wsRuntimeDefinitions, wsConfigurations, DEPLOY);
  }

  public void onPostDeploy(WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration[] wsConfigurations) throws WSWarningException {
    onDeployPhases(wsRuntimeDefinitions, wsConfigurations, POST_DEPLOY);
  }

   public void onDeploy(WSRuntimeDefinition wsRuntimeDefinition, Configuration wsConfiguration) throws  WSWarningException {
    onDeployPhases(wsRuntimeDefinition, wsConfiguration, DEPLOY);
  }

  public void onPostDeploy(WSRuntimeDefinition wsRuntimeDefinition, Configuration wsConfiguration) throws  WSWarningException {
    onDeployPhases(wsRuntimeDefinition, wsConfiguration, POST_DEPLOY);
  }

  public void onDeployPhases(String applicationName, WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration appConfiguration, int mode) throws WSWarningException {
    String excMsg = "Error occurred trying to initialize protocols on web services " + getModeName(mode) + " phase from application " + applicationName + ". ";
    if(wsRuntimeDefinitions == null || wsRuntimeDefinitions.length == 0) {
      return;
    }

    Configuration webservicesConfiguration = null;
    try {
      Configuration wsContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesConstants.WS_CONTAINER_NAME);
      if(wsContainerConfiguration != null) {
        webservicesConfiguration = wsContainerConfiguration.getSubConfiguration(WebServicesConstants.WEBSERVICES_CONFIG_NAME);
      }
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get " + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " configuration. " ;
      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }

    if(webservicesConfiguration == null) {
      String msg = excMsg + "Unable to get " + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " DB configuration. ";

      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }

    Configuration[] wsConfigurations = getSubConfigurations(getWSConfigNames(wsRuntimeDefinitions), webservicesConfiguration);

    onDeployPhases(wsRuntimeDefinitions, wsConfigurations, mode);
  }

  public void onDeployPhases(WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration[] wsConfigurations, int mode) throws WSWarningException {
    if(wsRuntimeDefinitions == null || wsRuntimeDefinitions == null) {
      return;
    }

    Vector warnings = new Vector();
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      WSRuntimeDefinition wsRuntimeDefinition = wsRuntimeDefinitions[i];
      Configuration wsConfiguration = wsConfigurations[i];
      try {
        onDeployPhases(wsRuntimeDefinition, wsConfiguration, mode);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void onCommitDeploy(WSRuntimeDefinition[] wsRuntimeDefinitions) throws WSWarningException {
    if(wsRuntimeDefinitions == null) {
      return;
    }

    Vector warnings = new Vector();
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      WSRuntimeDefinition wsRuntimeDefinition = wsRuntimeDefinitions[i];
      try {
        onCommitDeploy(wsRuntimeDefinition);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void onCommitDeploy(WSRuntimeDefinition wsRuntimeDefinition) throws WSWarningException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Unable to initialize web service protocols on " + getModeName(COMMIT_DEPLOY) + " phase. Web Service  " + wsIdentifier + ". ";


    Vector warnings = new Vector();
    String[] protocolIds = getWSProtocolIds(wsRuntimeDefinition);
    Hashtable protocols = getProtocols(protocolIds, warnings);
    warnings = WSUtil.addPrefixToStrings(excMsg, warnings);

    String[] seiTransportIds = WebServicesUtil.getSEITransportIds(wsRuntimeDefinition.getServiceEndpointDefinitions());

    try {
      onCommitDeploy630(wsIdentifier, seiTransportIds, filterProtocols630(protocols, COMMIT_DEPLOY));
    } catch (WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      onShortNotificationPhases(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), filterProtocols(protocols, COMMIT_DEPLOY) , COMMIT_DEPLOY);
    } catch (WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void onRollbackDeploy(WSRuntimeDefinition[] wsRuntimeDefinitions) throws WSWarningException {
   if(wsRuntimeDefinitions == null) {
      return;
    }

    Vector warnings = new Vector();
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      WSRuntimeDefinition wsRuntimeDefinition = wsRuntimeDefinitions[i];
      try {
        onRollbackDeploy(wsRuntimeDefinition);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void onRollbackDeploy(WSRuntimeDefinition wsRuntimeDefinition) throws WSWarningException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    onShortNotificationPhases(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), getWSProtocolIds(wsRuntimeDefinition), ROLLBACK_DEPLOY);
  }

  public void downloadFiles(String applicationName, String[] webServiceNames, String[] wsDirs, String[] wsConfigNames, Configuration appConfiguration) throws WSWarningException {
    String excMsg = "Error occurred trying to initialize protocols on web services " + getModeName(DOWNLOAD) + " phase from application " + applicationName + ". ";
    if(webServiceNames == null || webServiceNames.length == 0) {
      return;
    }

    Configuration webservicesConfiguration = null;
    try {
      Configuration wsContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesConstants.WS_CONTAINER_NAME);
      if(wsContainerConfiguration != null) {
        webservicesConfiguration = wsContainerConfiguration.getSubConfiguration(WebServicesConstants.WEBSERVICES_CONFIG_NAME);
      }
    } catch(ConfigurationException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      String msg = excMsg + "Unable to get " + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " configuration. " ;
      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }

    if(webservicesConfiguration == null) {
      String msg = excMsg + "Unable to get " + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " DB configuration. ";
      WSWarningException wExc = new WSWarningException();
      wExc.addWarning(msg);
      throw wExc;
    }

    Configuration[] wsConfigurations = getSubConfigurations(wsConfigNames, webservicesConfiguration);

    downloadFiles(applicationName, webServiceNames, wsDirs, wsConfigurations);
  }

  public void downloadFiles(String applicationName, String[] webServiceNames, String[] wsDirs, Configuration[] wsConfigurations) throws WSWarningException {
    if(webServiceNames == null || wsConfigurations == null) {
      return;
    }

    Vector warnings = new Vector();
    for(int i = 0; i < webServiceNames.length; i++) {
      try {
        downloadFiles(applicationName, webServiceNames[i], wsDirs[i], wsConfigurations[i]);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void downloadFiles(String applicationName, String webServiceName, String wsDir, Configuration wsConfiguration) throws WSWarningException {
//    String excMsg = "Unable to initialize web service protocols on " + getModeName(DOWNLOAD) + " phase. Application: " + applicationName + ", web service: " + webServiceName + ". ";
//    String warningMsg = excMsg;
//
//    String[] protocolIds = WSContainer.getComponentFactory().listProtocolIds();
//    Vector warnings = new Vector();
//    Hashtable protocols = getProtocolsForNotification(protocolIds, DOWNLOAD, warnings);
//    warnings = WSUtil.addPrefixToStrings(excMsg, warnings);
//
//    if(protocols.size() > 0) {
//      Enumeration enum1 = protocols.keys();
//      while(enum1.hasMoreElements()) {
//        String protocolId = (String)enum1.nextElement();
//        Protocol protocol = (Protocol)protocols.get(protocolId);
//
//        try {
//          ((WSLCMInterface)protocol).downloadFiles(applicationName, webServiceName, getProtocolDir(wsDir, protocolId), wsConfiguration);
//        } catch(WSWarningException wExc) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", wExc);
//
//          String msg = warningMsg + "Error occurred trying to initialize protocol with id: " + protocolId + ". ";
//          warnings.addAll(WSUtil.addPrefixToStrings(msg, wExc.getWarningsVector()));
//        } catch(WSDeploymentException dExc) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", dExc);
//
//          String msg = excMsg + "Error occurred trying to initialize protocol with id: " + protocolId + ". ";
//          warnings.add(msg);
//        } catch(Exception e) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", e);
//
//          String msg = warningMsg + "Unexpected exception occurred: " + e.getLocalizedMessage();
//          warnings.add(msg);
//        }
//      }
//    }
//
//    if(warnings.size() != 0) {
//      WSWarningException e = new WSWarningException();
//      e.addWarnings(warnings);
//      throw e;
//    }
  }

  public void onStart(String applicationName, WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration appConfiguration) throws WSWarningException {
    String excMsg = "Error occurred trying to initialize protocols on web services " + getModeName(START) + " phase from application " + applicationName + ". ";
    if(wsRuntimeDefinitions == null || wsRuntimeDefinitions.length == 0) {
      return;
    }

    Configuration[] wsConfigurations = new Configuration[wsRuntimeDefinitions.length];
    if (appConfiguration != null) {
      Configuration webservicesConfiguration = null;
      try {
        Configuration wsContainerConfiguration = appConfiguration.getSubConfiguration(WebServicesConstants.WS_CONTAINER_NAME);
        if(wsContainerConfiguration != null) {
          webservicesConfiguration = wsContainerConfiguration.getSubConfiguration(WebServicesConstants.WEBSERVICES_CONFIG_NAME);
        }
      } catch(ConfigurationException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(e);

        String msg = excMsg + "Unable to get " + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " configuration. " ;
        WSWarningException wExc = new WSWarningException();
        wExc.addWarning(msg);
        throw wExc;
      }

      if(webservicesConfiguration == null) {
        String msg = excMsg + "Unable to get " + WebServicesConstants.WEBSERVICES_CONFIG_NAME + " DB configuration. ";
        WSWarningException wExc = new WSWarningException();
        wExc.addWarning(msg);
        throw wExc;
      }

      wsConfigurations = getSubConfigurations(getWSConfigNames(wsRuntimeDefinitions), webservicesConfiguration);
    }

    onStart(wsRuntimeDefinitions, wsConfigurations);
  }

  public void onStart(WSRuntimeDefinition[] wsRuntimeDefinitions, Configuration[] wsConfigurations) throws WSWarningException {
    if(wsRuntimeDefinitions == null || wsRuntimeDefinitions == null) {
      return;
    }

    Vector warnings = new Vector();
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      WSRuntimeDefinition wsRuntimeDefinition = wsRuntimeDefinitions[i];
      Configuration wsConfiguration = wsConfigurations[i];
      try {
        onStart(wsRuntimeDefinition, wsConfiguration);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void onStart(WSRuntimeDefinition wsRuntimeDefinition, Configuration wsConfiguration) throws WSWarningException {
//    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
//    String excMsg = "Unable to initialize web service protocols on " + getModeName(START) + " phase. Web service: " + wsIdentifier + ". ";
//    String warningMsg = excMsg;
//
//    Vector warnings = new Vector();
//    String[] protocolIds = getWSProtocolIds(wsRuntimeDefinition);
//    Hashtable protocols = getProtocolsForNotification(protocolIds, START, warnings);
//    warnings = WSUtil.addPrefixToStrings(excMsg, warnings);
//
//    if(protocols.size() > 0) {
//      WSRuntimeContext wsRuntimeContext = null;
//      String wsDir = wsRuntimeDefinition.getWsDirsHandler().getWsDirectory();
//      try  {
//        wsRuntimeContext = new WSRuntimeContextImpl(wsRuntimeDefinition);
//      } catch(WSWarningException e) {
//        WSWarningException wExc = new WSWarningException();
//        wExc.addWarnings(WSUtil.addPrefixToStrings(warningMsg, e.getWarningsVector()));
//        throw e;
//      }
//      
//      //StaticConfigurationContext[] seiStaticConfigurationContexts = initializeSEIStaticConfigurationContexts(wsRuntimeDefinition);
//
//      Enumeration enum1 = protocols.keys();
//      while(enum1.hasMoreElements()) {
//        String protocolId = (String)enum1.nextElement();
//        Protocol protocol = (Protocol)protocols.get(protocolId);
//
//        try {
//          //if(wsRuntimeContext != null) {          
//            ((WSLCMInterface)protocol).onStart(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), wsRuntimeContext, getProtocolDir(wsDir, protocolId), wsConfiguration);
//          //}
//          //TODO - add separate try - catch blocks 
//          //((WSLCMInterface)protocol).onStart(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), seiStaticConfigurationContexts, getProtocolDir(wsDir, protocolId), wsConfiguration);
//        } catch(WSWarningException wExc) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", wExc);
//
//          String msg = warningMsg + "Error occurred trying to initialize protocol with id: " + protocolId + ". ";
//          warnings.addAll(WSUtil.addPrefixToStrings(msg, wExc.getWarningsVector()));
//        } catch(WSDeploymentException dExc) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", dExc);
//
//          String msg = excMsg + "Error occurred trying to initialize protocol with id: " + protocolId + ". ";
//          warnings.add(msg);
//        } catch(Exception e) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", e);
//
//          String msg = warningMsg + "Unexpected exception occurred: " + e.getLocalizedMessage();
//          warnings.add(msg);
//        }
//      }
//    }
//
//    if(warnings.size() != 0) {
//      WSWarningException e = new WSWarningException();
//      e.addWarnings(warnings);
//      throw e;
//    }
  }

  public void onCommitStart(WSRuntimeDefinition[] wsRuntimeDefinitions) throws WSWarningException {
    Vector warnings = new Vector();
    if(wsRuntimeDefinitions == null) {
      return;
    }

    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      WSRuntimeDefinition wsRuntimeDefinition = wsRuntimeDefinitions[i];
      try {
        onCommitStart(wsRuntimeDefinition);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void onCommitStart(WSRuntimeDefinition wsRuntimeDefinition) throws WSWarningException {
    onCommitStartStop(wsRuntimeDefinition, COMMIT_START);
  }

  public void onRollbackStart(WSRuntimeDefinition[] wsRuntimeDefinitions) throws WSWarningException {
    Vector warnings = new Vector();
    if(wsRuntimeDefinitions == null) {
      return;
    }

    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      WSRuntimeDefinition wsRuntimeDefinition = wsRuntimeDefinitions[i];
      try {
        onRollbackStart(wsRuntimeDefinition);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  private void onRollbackStart(WSRuntimeDefinition wsRuntimeDefinition) throws WSWarningException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    onShortNotificationPhases(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), getWSProtocolIds(wsRuntimeDefinition), ROLLBACK_START);
  }

  public void onStop(WSRuntimeDefinition[] wsRuntimeDefinitions) throws WSWarningException {
    Vector warnings = new Vector();
    if(wsRuntimeDefinitions == null) {
      return;
    }

    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      WSRuntimeDefinition wsRuntimeDefinition = wsRuntimeDefinitions[i];
      try {
        onStop(wsRuntimeDefinition);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void onStop(WSRuntimeDefinition wsRuntimeDefinition) throws WSWarningException {
    onCommitStartStop(wsRuntimeDefinition, STOP);
  }

  public void onRemove(String applicationName) throws WSWarningException {
    String excMsg = "Unable to initialize web service protocols on " + getModeName(REMOVE) + " mode. Application name " + applicationName + ". ";
    String[] protocolIds = WSContainer.getComponentFactory().listProtocolIds();


    Vector warnings = new Vector();
    Hashtable protocols = getProtocols(protocolIds, warnings);
    warnings = WSUtil.addPrefixToStrings(excMsg, warnings);

    try {
      onRemove630(applicationName, filterProtocols630(protocols, REMOVE));
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      onRemove(applicationName, filterProtocols(protocols, REMOVE));
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if (warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  private void onDeployPhases(WSRuntimeDefinition wsRuntimeDefinition, Configuration wsConfiguration, int mode) throws WSWarningException {
//    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
//    String excMsg = "Unable to initialize web service protocols on " + getModeName(mode) + " phase. Web Service  " + wsIdentifier + ". ";
//    String warningMsg = excMsg;
//
//    String wsDir = wsRuntimeDefinition.getWsDirsHandler().getWsDirectory();
//
//    Vector warnings = new Vector();
//    String[] protocolIds = getWSProtocolIds(wsRuntimeDefinition);
//    Hashtable protocols = getProtocolsForNotification(protocolIds, mode, warnings);
//    warnings = WSUtil.addPrefixToStrings(excMsg, warnings);
//
//    if(protocols.size() > 0) {
//      WSContext wsContext = new WSContextImpl(wsRuntimeDefinition);
//      
//      //StaticConfigurationContext[] seiStaticConfigurationContexts = initializeSEIStaticConfigurationContexts(wsRuntimeDefinition);    
//
//      Enumeration enum1 = protocols.keys();
//      while(enum1.hasMoreElements()) {
//        String protocolId = (String)enum1.nextElement();
//        Protocol protocol = (Protocol)protocols.get(protocolId);
//                             
//        try {
//          switch (mode) {
//            case DEPLOY: {
//              ((WSDeploymentInterface)protocol).onDeploy(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), wsContext, getProtocolDir(wsDir, protocolId), wsConfiguration);
//              //TODO - add separate try - catch blocks
//              //((WSDeploymentInterface)protocol).onDeploy(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), seiStaticConfigurationContexts, getProtocolDir(wsDir, protocolId), wsConfiguration);
//              break;
//            }
//            case POST_DEPLOY: {
//              ((WSDeploymentInterface)protocol).onPostDeploy(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), wsContext, getProtocolDir(wsDir, protocolId), wsConfiguration);
//              //TODO - add separate try - catch blocks
//              //((WSDeploymentInterface)protocol).onPostDeploy(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), seiStaticConfigurationContexts, getProtocolDir(wsDir, protocolId), wsConfiguration);
//              break;
//            }
//          }
//        } catch(WSWarningException wExc) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", wExc);
//
//          String msg = warningMsg + "Error occurred trying to initialize protocol with id: " + protocolId + ". ";
//          warnings.addAll(WSUtil.addPrefixToStrings(msg, wExc.getWarningsVector()));
//        } catch(WSDeploymentException dExc) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", dExc);
//
//          String msg = excMsg + "Error occurred trying to initialize protocol with id: " + protocolId + ". ";
//          warnings.add(msg);
//        } catch(Exception e) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", e);
//
//          String msg = warningMsg + "Unexpected exception occurred: " + e.getLocalizedMessage();
//          warnings.add(msg);
//        }
//      }
//    }
//
//    if(warnings.size() != 0) {
//      WSWarningException e = new WSWarningException();
//      e.addWarnings(warnings);
//      throw e;
//    }
  }

  public void onCommitDeploy630(WSIdentifier wsIdentifier, String[] seiTransportIds, Hashtable protocols) throws WSWarningException {
//    String warningMsg = "Unable to initialize web service protocols on " + getModeName(COMMIT_DEPLOY) + " phase. Web Service  " + wsIdentifier + ". ";
//
//    Vector warnings = new Vector();
//    if(protocols.size() > 0) {
//      Enumeration enum1 = protocols.keys();
//      while(enum1.hasMoreElements()) {
//        String protocolId = (String)enum1.nextElement();
//        Protocol protocol = (Protocol)protocols.get(protocolId);
//
//        try {
//          ((ServerProtocolDeployAppEvents)protocol).onDeployApplication(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), seiTransportIds);
//        } catch(Exception e) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", e);
//
//          String msg = warningMsg + "Unexpected exception occurred: " + e.getLocalizedMessage();
//          warnings.add(msg);
//        }
//      }
//    }
//
//    if(warnings.size() != 0) {
//      WSWarningException e = new WSWarningException();
//      e.addWarnings(warnings);
//      throw e;
//    }
  }

  private void onCommitStartStop(WSRuntimeDefinition wsRuntimeDefinition, int mode) throws WSWarningException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Unable to initialize web service protocols on " + getModeName(mode) + " phase. Application:  " + wsIdentifier.getApplicationName()  + ", web service: " + wsIdentifier.getServiceName() + ". ";

    Vector warnings = new Vector();
    String[] protocolIds = getWSProtocolIds(wsRuntimeDefinition);
    Hashtable protocols = getProtocols(protocolIds, warnings);
    warnings = WSUtil.addPrefixToStrings(excMsg, warnings);

    try {
      Hashtable filteredProtocols = filterProtocols630(protocols, mode);
      onCommitStartStop630(wsRuntimeDefinition, filteredProtocols, mode);
    } catch (WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    try {
      Hashtable filteredProtocols = filterProtocols(protocols, mode);
      onShortNotificationPhases(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName(), filteredProtocols, mode);
    } catch (WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  public void onCommitStartStop630(WSRuntimeDefinition wsRuntimeDefinition, Hashtable protocols, int mode) throws WSWarningException {
    String warningMsg = "Unable to initialize some web service protocols on " + getModeName(mode) + " phase. Web Service " + wsRuntimeDefinition.getWSIdentifier() + ". ";
    ServiceEndpointDefinition[] endpointDefinitions = wsRuntimeDefinition.getServiceEndpointDefinitions();

    if(endpointDefinitions == null) {
      return;
    }

    Vector warnings = new Vector();
    for(int i = 0; i < endpointDefinitions.length; i++) {
      try {
        ServiceEndpointDefinition endpointDefinition = endpointDefinitions[i];
        String[] protocolIds = getSEIProtocolIds(endpointDefinition);
        Hashtable filteredProtocols = filterProtocols(protocolIds, protocols);
        onCommitStartStop630(endpointDefinition, filteredProtocols, mode);
      } catch(WSWarningException e) {
        warnings.addAll(e.getWarningsVector());
      }
    }

    WSUtil.addPrefixToStrings(warningMsg, warnings);
    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  private void onCommitStartStop630(ServiceEndpointDefinition endpointDefinition, Hashtable protocols, int mode) throws WSWarningException {
//    String warningMsg = "Unable to initialize service endpoint protocols on " + getModeName(mode) + " phase. SEI for ws configuration: " + endpointDefinition.getConfigurationName() + ". ";
//
//    Vector warnings = new Vector();
//    if(protocols.size() > 0) {
//      RuntimeContext runtimeContext = RuntimeProcessor.initilizeRuntimeContext(endpointDefinition);
//
//      Enumeration enum1 = protocols.keys();
//      while(enum1.hasMoreElements()) {
//        String protocolId = (String)enum1.nextElement();
//        Protocol protocol = (Protocol)protocols.get(protocolId);
//
//        try {
//          switch (mode) {
//            case COMMIT_START: {
//              if (protocol instanceof ServerProtocolAppStateEvents) {
//                ((ServerProtocolAppStateEvents)protocol).onStartApplicationEvent(runtimeContext);
//              }
//              if (protocol instanceof ServerProtocolStartAppEvent) {
//                ((ServerProtocolStartAppEvent)protocol).onStartApplication(runtimeContext.getProtocolChain().getProtocolContext(protocolId));
//              }
//              break;
//            }
//            case STOP: {
//              ((ServerProtocolAppStateEvents)protocol).onStopApplicationEvent(runtimeContext);
//              break;
//            }
//          }
//        } catch(Exception e) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", e);
//
//          String msg = warningMsg + "Unexpected exception occurred: " + e.getLocalizedMessage();
//          warnings.add(msg);
//        }
//      }
//    }
//
//    if(warnings.size() != 0) {
//      WSWarningException e = new WSWarningException();
//      e.addWarnings(warnings);
//      throw e;
//    }
  }

  private void onShortNotificationPhases(String applicationName, String webServiceName, String[] protocolIds, int mode) throws WSWarningException {
    String excMsg = "Unable to initialize web service protocols on " + getModeName(mode) + " phase. Application:  " + applicationName + ", web service: " + webServiceName + ". ";

    Vector warnings = new Vector();
    Hashtable protocols = getProtocolsForNotification(protocolIds, mode, warnings);
    warnings = WSUtil.addPrefixToStrings(excMsg, warnings);
    try {
      onShortNotificationPhases(applicationName, webServiceName, protocols, mode);
    } catch(WSWarningException e) {
      warnings.addAll(e.getWarningsVector());
    }

    if(warnings.size() != 0) {
      WSWarningException e = new WSWarningException();
      e.addWarnings(warnings);
      throw e;
    }
  }

  private void onShortNotificationPhases(String applicationName, String webServiceName, Hashtable protocols, int mode) throws WSWarningException {
//    String warningMsg = "Unable to initialize web service protocols on " + getModeName(mode) + " phase. Application:  " + applicationName  + ", web service: " + webServiceName + ". ";
//
//    Vector warnings = new Vector();    
//    if(protocols.size() > 0) {
//      Enumeration enum1 = protocols.keys();
//      while(enum1.hasMoreElements()) {
//        String protocolId = (String)enum1.nextElement();
//        Protocol protocol = (Protocol)protocols.get(protocolId);        
// 
//        try {
//          switch (mode) {
//            case COMMIT_DEPLOY: {              
//              ((WSDeploymentInterface)protocol).onCommitDeploy(applicationName, webServiceName);              
//              break;
//            }
//            case ROLLBACK_DEPLOY: {
//              ((WSDeploymentInterface)protocol).onRollbackDeploy(applicationName, webServiceName);
//              break;
//            }
//            case COMMIT_START: {               
//              ((WSLCMInterface)protocol).onCommitStart(applicationName, webServiceName);              
//              break;
//            }
//            case ROLLBACK_START: {
//              ((WSLCMInterface)protocol).onRollbackStart(applicationName, webServiceName);
//              break;
//            }
//            case STOP: {
//              ((WSLCMInterface)protocol).onStop(applicationName, webServiceName);
//              break;
//            }
//          }
//        } catch(WSWarningException wExc) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", wExc);
//
//          String msg = warningMsg + "Error occurred trying to initialize protocol with id: " + protocolId + ". ";
//          warnings.addAll(WSUtil.addPrefixToStrings(msg, wExc.getWarningsVector()));
//        } catch(Exception e) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", e);
//
//          String msg = warningMsg + "Unexpected exception occurred: " + e.getLocalizedMessage();
//          warnings.add(msg);
//        }
//      }
//    }
//
//    if(warnings.size() != 0) {
//      WSWarningException e = new WSWarningException();
//      e.addWarnings(warnings);
//      throw e;
//    }
  }

  public void onRemove630(String applicationName, Hashtable protocols) throws WSWarningException {
//    String warningMsg = "Unable to initialize web service protocols on " + getModeName(REMOVE) + " mode. Application name " + applicationName + ". ";
//
//    Vector warnings = new Vector();
//    if(protocols.size() > 0) {
//      Enumeration enum1 = protocols.keys();
//      while(enum1.hasMoreElements()) {
//        String protocolId = (String)enum1.nextElement();
//        Protocol protocol = (Protocol)protocols.get(protocolId);
//
//        try {
//          ((ServerProtocolDeployAppEvents)protocol).onUndeployApplication(applicationName);
//        } catch(Exception e) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", e);
//
//          String msg = warningMsg + "Unexpected exception occurred: " + e.getLocalizedMessage();
//          warnings.add(msg);
//        }
//      }
//    }
//
//    if(warnings.size() != 0) {
//      WSWarningException e = new WSWarningException();
//      e.addWarnings(warnings);
//      throw e;
//    }
  }

  private void onRemove(String applicationName, Hashtable protocols) throws WSWarningException {
//    String warningMsg = "Unable to initialize web service protocols on " + getModeName(REMOVE) + " mode. Application name " + applicationName + ". ";
//
//    Vector warnings = new Vector();
//    if(protocols.size() > 0) {
//      Enumeration enum1 = protocols.keys();
//      while(enum1.hasMoreElements()) {
//        String protocolId = (String)enum1.nextElement();
//        Protocol protocol = (Protocol)protocols.get(protocolId);
//
//        try {
//          ((WSDeploymentInterface)protocol).onRemove(applicationName);
//        } catch(WSWarningException wExc) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", wExc);
//
//          String msg = warningMsg + "Error occurred trying to initialize protocol with id: " + protocolId + ". ";
//          warnings.addAll(WSUtil.addPrefixToStrings(msg, wExc.getWarningsVector()));
//        } catch(Exception e) {
//          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          wsDeployLocation.catching("Warning! ", e);
//
//          String msg = warningMsg + "Unexpected exception occurred: " + e.getLocalizedMessage();
//          warnings.add(msg);
//        }
//      }
//    }
//
//    if(warnings.size() != 0) {
//      WSWarningException e = new WSWarningException();
//      e.addWarnings(warnings);
//      throw e;
//    }
  }

  public String[] getWSProtocolIds(WSRuntimeDefinition wsRuntimeDefinition) {
    return getSEIProtocolIds(wsRuntimeDefinition.getServiceEndpointDefinitions());
  }

  private String[] getSEIProtocolIds(ServiceEndpointDefinition[] endpointDefinitions) {
    if (endpointDefinitions == null) {
      return new String[0];
    }

    String[] allSeiProtocolIds = new String[0];
    for(int i = 0; i < endpointDefinitions.length; i++) {
      String[] currentSeiProtocolIds = getSEIProtocolIds(endpointDefinitions[i]);
      allSeiProtocolIds = WSUtil.unifyStrings(new String[][]{allSeiProtocolIds, currentSeiProtocolIds});
    }

    return allSeiProtocolIds;
  }

  private String[] getSEIProtocolIds(ServiceEndpointDefinition endpointDefinition) {
    String[] globalProtocolIds = getProtocolIds((FeatureInfo[])endpointDefinition.getFeaturesChain());
    String[] operationsProtocolIds = getOperationProtocolIds(endpointDefinition.getOperations());

    return WSUtil.unifyStrings(new String[][]{globalProtocolIds, operationsProtocolIds});
  }

  private String[] getOperationProtocolIds(OperationDefinition[] operations) {
    if(operations == null) {
      return new String[0];
    }

    String[] allProtocolIds = new String[0];
    for(int i = 0; i < operations.length; i++) {
      OperationDefinitionImpl operation = (OperationDefinitionImpl)operations[i];
      String[] currentProtocolIds = getProtocolIds(operation.getFeaturesChain());
      allProtocolIds = WSUtil.unifyStrings(new String[][]{allProtocolIds, currentProtocolIds});
    }

    return allProtocolIds;
  }

  private String[] getProtocolIds(FeatureInfo[] protocols) {
    if(protocols == null) {
      return new String[0];
    }

    String[] protocolIds = new String[protocols.length];
    for(int i = 0; i < protocols.length; i++) {
      protocolIds[i] = protocols[i].getProtocolID();
    }

    return protocolIds;
  }

  private String[] getWSConfigNames(WSRuntimeDefinition[] wsRuntimeDefinitions) {
    if(wsRuntimeDefinitions == null) {
      return new String[0];
    }

    String[] wsConfigNames = new String[wsRuntimeDefinitions.length];
    for(int i = 0; i < wsRuntimeDefinitions.length; i++) {
      WSRuntimeDefinition wsRuntimeDefinition = wsRuntimeDefinitions[i];
      String wsConfigName = wsRuntimeDefinition.getWsDirsHandler().getWSConfigName();
      wsConfigNames[i] = wsConfigName;
    }

    return wsConfigNames;
  }

  private Configuration[] getSubConfigurations(String[] subconfigNames, Configuration rootConfig) {
    if(subconfigNames == null) {
      return new Configuration[0];
    }

    Vector subconfigs = new Vector();
    for(int i = 0; i < subconfigNames.length; i++) {
      String subConfigName = subconfigNames[i];
      Configuration subConfig = null;
      try {
        subConfig = rootConfig.getSubConfiguration(subConfigName);
      } catch(ConfigurationException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching("Warning! ", e);
      }
      subconfigs.add(subConfig);
    }

    Configuration[] subConfigsArray = new Configuration[subconfigs.size()];
    subconfigs.toArray(subConfigsArray);

    return subConfigsArray;
  }

  private String getProtocolDir(String baseDir, String protocolId) {
    return baseDir + WebServicesConstants.SEPARATOR + WSUtil.replaceForbiddenChars(protocolId);
  }

  public boolean isInSkippedList(String str) {
    for(int i = 0; i < SKIPPED_PROTOCOLS_LIST.length; i++) {
      if(str.equals(SKIPPED_PROTOCOLS_LIST[i])) {
        return true;
      }
    }

    return false;
  }

  private Hashtable getProtocolsForNotification(String[] protocolIds, int mode, Vector warnings) {
    Hashtable protocols = getProtocols(protocolIds, warnings);
    return filterProtocols(protocols, mode);
  }

  private Hashtable filterProtocols(Hashtable protocols, int mode) {
    Hashtable protocolsForNotification = new Hashtable();
//    Enumeration enum1 = protocols.keys();
//    while(enum1.hasMoreElements()) {
//      String protocolId = (String)enum1.nextElement();
//      Protocol protocol = (Protocol)protocols.get(protocolId);
//      switch (mode) {
//        case DEPLOY: {
//          if(protocol instanceof WSDeploymentInterface) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case POST_DEPLOY: {
//          if(protocol instanceof WSDeploymentInterface) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case COMMIT_DEPLOY: {
//          if(protocol instanceof WSDeploymentInterface) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case ROLLBACK_DEPLOY: {
//          if(protocol instanceof WSDeploymentInterface) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case REMOVE: {
//          if(protocol instanceof WSDeploymentInterface) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case DOWNLOAD: {
//          if(protocol instanceof WSLCMInterface) {
//           protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case START: {
//          if(protocol instanceof WSLCMInterface) {
//           protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case COMMIT_START: {
//          if(protocol instanceof WSLCMInterface) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case ROLLBACK_START: {
//          if(protocol instanceof WSLCMInterface) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case STOP: {
//          if(protocol instanceof WSLCMInterface) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//      }
//    }

    return protocolsForNotification;
  }

  private Hashtable filterProtocols630(Hashtable protocols, int mode) {
    Hashtable protocolsForNotification = new Hashtable();
//    Enumeration enum1 = protocols.keys();
//    while(enum1.hasMoreElements()) {
//      String protocolId = (String)enum1.nextElement();
//      Protocol protocol = (Protocol)protocols.get(protocolId);
//      switch (mode) {
//        case COMMIT_DEPLOY: {
//          if(protocol instanceof ServerProtocolDeployAppEvents) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case COMMIT_START: {
//          if(protocol instanceof ServerProtocolStartAppEvent || protocol instanceof ServerProtocolAppStateEvents) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case STOP: {
//          if(protocol instanceof ServerProtocolAppStateEvents) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//        case REMOVE: {
//          if(protocol instanceof ServerProtocolDeployAppEvents) {
//            protocolsForNotification.put(protocolId, protocol);
//          }
//          break;
//        }
//      }
//    }

    return protocolsForNotification;
  }

  private Hashtable filterProtocols(String[] protocolIds, Hashtable protocols) {
    if(protocolIds == null || protocols == null) {
      return new Hashtable();
    }
    Hashtable filteredProtocols = new Hashtable();
    for(int i = 0; i < protocolIds.length; i++) {
      String protocolId = protocolIds[i];
      if(protocols.containsKey(protocolId)) {
        filteredProtocols.put(protocolId, protocols.get(protocolId));
      }
    }

    return filteredProtocols;
  }

  private Hashtable getProtocols(String[] protocolIds, Vector warnings) {
    String excMsg = "Error occurred, trying to instantiate protocols.";
    if(protocolIds == null) {
      return new Hashtable();
    }

    ComponentFactory componentFactory = WSContainer.getComponentFactory();
    Hashtable protocols = new Hashtable();
//    for(int i = 0; i < protocolIds.length; i++) {
//      String protocolId = protocolIds[i];
//      try {
//        if(!protocols.containsKey(protocolId) && !isInSkippedList(protocolId)) {
//          Protocol protocol = componentFactory.getProtocolInstance(protocolId); 
//          protocols.put(protocolId, protocol); 
//        }
//      } catch(Exception e) {
//        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//        wsDeployLocation.catching("Warning! ", e);
//
//        String msg = excMsg +  "Protocol " + protocolId + " can not be instantiated.  ";
//        warnings.add(msg);
//      }
//    }

    return protocols;
  }
  
//  private StaticConfigurationContext[] initializeSEIStaticConfigurationContexts(WSRuntimeDefinition wsRuntimeDefinition) {
//    ServiceEndpointDefinition[] endpointDefinitions = wsRuntimeDefinition.getServiceEndpointDefinitions();
//    
//    if(endpointDefinitions == null) {
//      return new StaticConfigurationContext[0];
//    }
//    
//    StaticConfigurationContext[] seiStaticConfigurationContexts = new StaticConfigurationContext[endpointDefinitions.length];
//    for(int i = 0; i < endpointDefinitions.length; i++) {
//      seiStaticConfigurationContexts[i] = RuntimeProcessingEnvironment.initilizeStaticContext(endpointDefinitions[i]);   
//    }      
//    
//    return seiStaticConfigurationContexts;
//  }

}
