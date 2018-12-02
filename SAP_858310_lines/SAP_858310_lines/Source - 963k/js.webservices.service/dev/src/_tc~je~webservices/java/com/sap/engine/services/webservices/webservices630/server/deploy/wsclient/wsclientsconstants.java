package com.sap.engine.services.webservices.webservices630.server.deploy.wsclient;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.WSBaseConstants;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSClientsConstants extends WSBaseConstants {

  public static final String WS_CLIENTS_DEPLOYMENT_DESCRIPTOR     = "ws-clients-deployment-descriptor.xml";
  public static final String WS_CLIENTS_RUNTIME_DESCRIPTOR        = "ws-clients-runtime-descriptor.xml";
  public static final String WS_CLIENTS_DEPLOYMENT_DESCRIPTOR_DIR = "ws-clients-descriptors";
  public static final String DEFAULT_LOGICAL_PORTS_DESCRIPTOR     = "logical-ports.xml";             

  public static final String WS_CLIENTS_CONFIG_NAME  = "wsClients";

  public static final String LOG_PORTS_NAME              = "log_ports";
  public static final String WS_CLIENT_JAR_NAME          = "wsClientJarName";
  public static final String WS_CLIENTS_EJB_JNDI_NAMES   = "wsClientsEjbJndiNames";

  public static final String GENERATED_DIR_NAME = "gen";

  public static String WS_CLIENTS_CONTEXT                   = "wsclients";
  public static String WS_CLIENTS_PROXY_REL_CONTEXT         = "proxies";
  public static String WS_CLIENTS_PROXY_CONTEXT             = WS_CLIENTS_CONTEXT + "/" + WS_CLIENTS_PROXY_REL_CONTEXT;

  public static final String DEFAULT_SERVICE_CLASS_NAME = "javax.xml.rpc.Service";

  public static final String COMPONENT_FACTORY         = "com.sap.engine.services.jndi.ComponentObjectFactory";

}
