package com.sap.engine.services.webservices.exceptions;

import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.log_configurator.admin.LogConfiguratorManagementInterface;
import com.sap.engine.lib.logging.descriptors.LogConfiguration;
import com.sap.engine.lib.logging.descriptors.LogControllerDescriptor;
import com.sap.engine.lib.logging.descriptors.LogDestinationDescriptor;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSLogging {

  public static String WEBSERVICES_LOCATION        = "com.sap.engine.services.webservices";
  public static String SERVER_LOCATION             = "com.sap.engine.services.webservices.server";
  public static String DEPLOY_LOCATION             = "com.sap.engine.services.webservices.server.deploy";
  public static String RUNTIME_LOCATION            = "com.sap.engine.services.webservices.server.runtime";
  public static String WS_CLIENTS_RUNTIME_LOCATION = "com.sap.engine.services.webservices.server.runtime.wsclients";
  public static String CONNECTION_LOCATION         = "com.sap.engine.services.webservices.server.connection";

  public static final Location DEPLOY_LOC  = Location.getLocation(DEPLOY_LOCATION); 
  
  private static String RUNTIME_LOG_DESTINATION            =  "./log/services/webservices/runtime";
  private static String WS_CLIENTS_RUNTIME_LOG_DESTINATION = "./log/services/webservices/runtime/wsclients";

  private static int DEFAULT_FILE_LIMIT           = 10000000;
  private static int DEFAULT_FILE_COUNT           = 5;
  private static int DEFAULT_DESTINATION_SEVERITY = Severity.ALL;
  private static int DEFAULT_CONTROLLER_SEVERITY  = Severity.ERROR;
  private static byte DEFAULT_ASSOCIATION_TYPE    = LogControllerDescriptor.ASSOCIATION_TYPE_PUBLIC;
  private static String DEFAULT_LOG_TYPE          = "FileLog";


  public static Location getWSLocation(WSIdentifier wsIdentifier) {
    String wsLocationName = getWSLocationName(wsIdentifier.getApplicationName(), wsIdentifier.getServiceName());
    return Location.getLocation(wsLocationName);
  }

  public static Location getWSLocation(String applicationName, String serviceName) {
    String wsLocationName = getWSLocationName(applicationName, serviceName);
    return Location.getLocation(wsLocationName);
  }

  public static String getWSLocationName(String applicaitionName, String serviceName) {
    return getComponentLocationName(RUNTIME_LOCATION, applicaitionName, serviceName);
  }

  public static String getWSClientLocationName(String applicaitionName, String serviceRefName) {
    return getComponentLocationName(WS_CLIENTS_RUNTIME_LOCATION, applicaitionName, serviceRefName);
  }

  public static void applyWSClientConfiguration(String applicationName, String serviceRefName) {

    LogConfiguratorManagementInterface logConfiguratorInterface = WSContainer.getLogConfiguratorInterface();

    LogConfiguration logConfiguration = constructWSClientFileLogConfiguration(applicationName, serviceRefName);
    if (logConfiguratorInterface != null) {
      logConfiguratorInterface.applyConfiguration(logConfiguration);
    }
  }

  public static void applyAndStoreWSClientConfiguration(String applicationName, String serviceRefName) {

    LogConfiguratorManagementInterface logConfiguratorInterface = WSContainer.getLogConfiguratorInterface();

    LogConfiguration logConfiguration = constructWSClientFileLogConfiguration(applicationName, serviceRefName);
    if (logConfiguratorInterface != null) {
      logConfiguratorInterface.applyAndStoreConfiguration(logConfiguration);
    }
  }

  public static void removeWSClientConfiguration(String applicationName, String serviceRefName) {
    LogConfiguratorManagementInterface logConfiguratorInterface = WSContainer.getLogConfiguratorInterface();

    LogConfiguration logConfiguration = constructWSClientFileLogConfiguration(applicationName, serviceRefName);
    if (logConfiguratorInterface != null) {
      logConfiguratorInterface.applyConfiguration(null, logConfiguration);
    }
  }

  public static void removeAndStoreWSClientConfiguration(String applicationName, String serviceRefName) {
    LogConfiguratorManagementInterface logConfiguratorInterface = WSContainer.getLogConfiguratorInterface();

    LogConfiguration logConfiguration = constructWSClientFileLogConfiguration(applicationName, serviceRefName);
    if (logConfiguratorInterface != null) {
      logConfiguratorInterface.applyAndStoreConfiguration(null, logConfiguration);
    }
  }

  public static void applyWSConfiguration(String applicationName, String serviceName) {

    LogConfiguratorManagementInterface logConfiguratorInterface = WSContainer.getLogConfiguratorInterface();

    LogConfiguration logConfiguration = constructWSFileLogConfiguration(applicationName, serviceName);
    if (logConfiguratorInterface != null) {
      logConfiguratorInterface.applyConfiguration(logConfiguration);
    }
  }

  public static void applyAndStoreWSConfiguration(String applicationName, String serviceName) {

    LogConfiguratorManagementInterface logConfiguratorInterface = WSContainer.getLogConfiguratorInterface();

    LogConfiguration logConfiguration = constructWSFileLogConfiguration(applicationName, serviceName);
    if (logConfiguratorInterface != null) {
      logConfiguratorInterface.applyAndStoreConfiguration(logConfiguration);
    }
  }

  public static void removeWSConfiguration(String applicationName, String serviceName) {
    LogConfiguratorManagementInterface logConfiguratorInterface = WSContainer.getLogConfiguratorInterface();

    LogConfiguration logConfiguration = constructWSFileLogConfiguration(applicationName, serviceName);
    if (logConfiguratorInterface != null) {
      logConfiguratorInterface.applyConfiguration(null, logConfiguration);
    }
  }

  public static void removeAndStoreWSConfiguration(String applicationName, String serviceName) {
    LogConfiguratorManagementInterface logConfiguratorInterface = WSContainer.getLogConfiguratorInterface();

    LogConfiguration logConfiguration = constructWSFileLogConfiguration(applicationName, serviceName);
    if (logConfiguratorInterface != null) {
      logConfiguratorInterface.applyAndStoreConfiguration(null, logConfiguration);
    }
  }

  private static LogConfiguration constructWSFileLogConfiguration(String applicationName, String serviceName) {
    return constructComponentFileLogConfiguration(RUNTIME_LOCATION, RUNTIME_LOG_DESTINATION, "ws", applicationName, serviceName);
  }

  private static LogConfiguration constructWSClientFileLogConfiguration(String applicationName, String serviceRefName) {
    return constructComponentFileLogConfiguration(WS_CLIENTS_RUNTIME_LOCATION, WS_CLIENTS_RUNTIME_LOG_DESTINATION, "wsclients", applicationName, serviceRefName);
  }

  private static String getComponentLocationName(String rootLocation, String applicationName, String componentName) {
    return getControllerName(rootLocation, applicationName, componentName);
  }

  private static LogConfiguration constructComponentFileLogConfiguration(String rootLocation, String rootDir, String prefix, String applicationName, String componentName) {
    String controllerName = getControllerName(rootLocation, applicationName, componentName);
    String destinationName = getDestinationName(prefix, applicationName, componentName);
    String destionationFilePath = getDestinationFilePath(rootDir, applicationName, componentName);

    return constructFileLogConfiguration(controllerName, destinationName, destionationFilePath);
  }

  private static LogDestinationDescriptor constructFileLogDestinationDescriptor(String destinationName, String destinationFilePath) {
    LogDestinationDescriptor logDestinationDescriptor = new LogDestinationDescriptor();
    logDestinationDescriptor.setName(destinationName);
    logDestinationDescriptor.setPattern(destinationFilePath);
    logDestinationDescriptor.setEffectiveSeverity(DEFAULT_DESTINATION_SEVERITY);
    logDestinationDescriptor.setCount(DEFAULT_FILE_COUNT);
    logDestinationDescriptor.setLimit(DEFAULT_FILE_LIMIT);
    logDestinationDescriptor.setType(DEFAULT_LOG_TYPE);
    return logDestinationDescriptor;
  }

  private static LogControllerDescriptor constructLogControllerDescriptor(String controllerName, LogDestinationDescriptor logDestinationDescriptor) {
    LogControllerDescriptor logControllerDescriptor = new LogControllerDescriptor();
    logControllerDescriptor.setName(controllerName);
    logControllerDescriptor.setMinSeverity(DEFAULT_CONTROLLER_SEVERITY);
    logControllerDescriptor.setMaxSeverity(DEFAULT_CONTROLLER_SEVERITY);
    logControllerDescriptor.addDestination(logDestinationDescriptor, DEFAULT_ASSOCIATION_TYPE);

    return logControllerDescriptor;
  }

  private static LogConfiguration constructFileLogConfiguration(String controllerName, String destinationName, String destinationFilePath) {
    LogConfiguration logConfiguration = new LogConfiguration();
    LogDestinationDescriptor logDestinationDescriptor = constructFileLogDestinationDescriptor(destinationName, destinationFilePath);
    LogControllerDescriptor logControllerDescriptor = constructLogControllerDescriptor(controllerName, logDestinationDescriptor);

    logConfiguration.setLogControllers(new LogControllerDescriptor[]{logControllerDescriptor});
    logConfiguration.setLogDestinations(new LogDestinationDescriptor[]{logDestinationDescriptor});

    return logConfiguration;
  }

  private static String getDestinationName(String prefix, String applicationName, String componentName) {
    return  WSUtil.replaceForbiddenChars(prefix) + "_" +
            WSUtil.replaceForbiddenChars(applicationName) + "_" +
            WSUtil.replaceForbiddenChars(componentName);
  }

  private static String getControllerName(String rootLocation, String applicationName, String componentName) {
    return  rootLocation + "." +
            WSUtil.replaceForbiddenChars(applicationName) + "." +
            WSUtil.replaceForbiddenChars(componentName);
  }

  private static String getDestinationFilePath(String rootDir, String applicationName, String component) {
    return  rootDir + "/" +
            WSUtil.replaceForbiddenChars(applicationName) + "/" +
            WSUtil.replaceForbiddenChars(component) + ".trc";
  }

}