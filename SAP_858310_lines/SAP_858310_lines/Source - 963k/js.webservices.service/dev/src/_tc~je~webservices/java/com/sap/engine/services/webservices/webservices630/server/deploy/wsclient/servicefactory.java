package com.sap.engine.services.webservices.webservices630.server.deploy.wsclient;

import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.runtime.definition.WSClientIdentifier;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceBaseServer;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortFactory;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;
import com.sap.engine.services.webservices.runtime.definition.wsclient.WSClientRuntimeInfo;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.tc.logging.Location;

import javax.xml.rpc.Service;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ServiceFactory {

  public static final String DEFAULT_SERVICE_CLASS_NAME = "javax.xml.rpc.Service";

  public ServiceFactory() {
  }

  public Service getServiceInstance(WSClientRuntimeInfo wsClientRuntimeInfo, boolean isGenericUse) throws WSDeploymentException {
    WSClientIdentifier wsClientId = wsClientRuntimeInfo.getWsClientId();

    String applicationName = wsClientId.getApplicationName();
    ClassLoader appLoader = WSContainer.getServiceContext().getCoreContext().getLoadContext().getClassLoader(applicationName);

    return getServiceInstance(wsClientRuntimeInfo, appLoader, isGenericUse);
  }

  public Service getServiceInstance(WSClientRuntimeInfo wsClientRuntimeInfo, ClassLoader loader, boolean isGenericUse) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to instantiate ws client service instance.";

    WSClientIdentifier wsClientId = wsClientRuntimeInfo.getWsClientId();
    String logPortsFileName = wsClientRuntimeInfo.getLogicalPortsFullFileName();
    String serviceInterfaceName = wsClientRuntimeInfo.getServiceInterfaceName();

    ServiceBaseServer service = null;
    try {
      if (serviceInterfaceName.equals(DEFAULT_SERVICE_CLASS_NAME) || isGenericUse) {
        service = new ServiceBaseServer(logPortsFileName, WSContainer.getComponentFactory(), loader);
      } else {                
        String serviceClassName = getServiceClassName(serviceInterfaceName);
        Class serviceClass = loader.loadClass(serviceClassName);
        service = (ServiceBaseServer)serviceClass.newInstance();
        LogicalPorts logPorts = (new LogicalPortFactory()).loadLogicalPorts(logPortsFileName);
        service.init(logPorts, WSContainer.getComponentFactory(), loader, WSLogging.getWSClientLocationName(wsClientId.getApplicationName(), wsClientId.getServiceRefName()));
      }
    } catch(Exception e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(excMsg, e);

      Object[] args = new String[]{excMsg, wsClientId.getApplicationName(), wsClientId.getJarName(), wsClientId.getServiceRefName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

    makeWSClientStartSettings(wsClientRuntimeInfo, service);
    return service;
  }

  private void makeWSClientStartSettings(WSClientRuntimeInfo wsClientRuntimeInfo, Service service) {
    ((ServiceBaseServer)service).setHTTPProxyResolver(WSContainer.getHTTPProxyResolver());
    ((ServiceBaseServer)service).setSLDConnection(WSContainer.getSLDConnection());
  }

  private String getServiceClassName(String serviceInterfaceName) {
    return serviceInterfaceName + "Impl";
  }

}
