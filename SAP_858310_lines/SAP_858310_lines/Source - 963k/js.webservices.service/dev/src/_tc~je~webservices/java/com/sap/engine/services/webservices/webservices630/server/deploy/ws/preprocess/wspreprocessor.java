package com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.services.webservices.webservices630.server.deploy.WSConfigurationHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDefinitionFactory;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSRuntimeActivator;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.tc.logging.Location;

import java.util.*;
import java.io.File;

/**
 * Title: WSPreprocessor
 * Description: The class prepares web services runtime and update processing environment.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSPreprocessor {

  public void preprocess(String applicationName, String webServicesDir, Configuration appConfiguration) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to collect web services location information for application " + applicationName + ". ";
    try {
      if(!WSConfigurationHandler.existsWebServicesConfiguration(appConfiguration)) {
        return;
      }

      WSConfigurationHandler.downloadWebServicesBaseConfiguration(webServicesDir, appConfiguration);
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public WSFileLocationWrapper[] loadWSFileLocationWrappers(String applicationName, String webServicesDir, Configuration appConfiguration) throws WSDeploymentException {
    return loadWSFileLocationWrappers(applicationName, new File(webServicesDir).listFiles());
  }

  public WSFileLocationWrapper[] loadWSFileLocationWrappers(String applicationName, File[] wsDirs) throws WSDeploymentException {
    if(wsDirs == null) {
      return new WSFileLocationWrapper[0];
    }

    Vector wsFileLocationWrappers = new Vector();
    for(int i = 0; i < wsDirs.length; i++) {
      File wsDir = wsDirs[i];
      if(!wsDir.getName().equals(WSDirsHandler.getAppJarsRelDir()))  {
        wsFileLocationWrappers.add(loadWSFileLocationWrapper(applicationName, wsDir.getAbsolutePath()));
      }
    }

    WSFileLocationWrapper[] wsFileLocationWrappersArr = new WSFileLocationWrapper[wsFileLocationWrappers.size()];
    wsFileLocationWrappers.copyInto(wsFileLocationWrappersArr);

    return wsFileLocationWrappersArr;
  }

  public WSFileLocationWrapper loadWSFileLocationWrapper(String applicationName, String wsDir) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to collect web service location information, web service directory " + wsDir + ", application " + applicationName + ". ";

    WSFileLocationWrapper wsLocationWrapper = null;
    try {
      Properties mappings = null;
      boolean isBaseMappings = false;
      if(WSRuntimeActivator.hasMappings(wsDir)) {
        mappings = WSRuntimeActivator.loadMappingsFile(WSDirsHandler.getMappingsPath(wsDir));
      } else {
        mappings = WSDirsHandler.generateBaseDefaultMappings();
        isBaseMappings = true;
      }

      String wsName = WSDefinitionFactory.extractWebServicesNames(WSDirsHandler.getWSDeploymentDescriptorPath(wsDir, mappings))[0];
      if(isBaseMappings) {
        WSDirsHandler.upgradeBaseMappings(mappings, wsName);
      }

      wsLocationWrapper = new WSDefinitionFactory().loadWSFileLocationWrapper(applicationName, new WSDirsHandler(mappings, wsDir));
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    return wsLocationWrapper;
  }

}
