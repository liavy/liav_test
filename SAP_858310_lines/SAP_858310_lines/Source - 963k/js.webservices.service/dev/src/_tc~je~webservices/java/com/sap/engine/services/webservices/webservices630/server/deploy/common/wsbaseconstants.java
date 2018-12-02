package com.sap.engine.services.webservices.webservices630.server.deploy.common;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSBaseConstants {

  public static final String WS_CONTAINER_NAME = "webservices_container";
  public static final String BACKUP =  "backup";
  public static final String EVENT_CONFIG_NAME = "event";
  public static final String MOST_RECENT_EVENT = "most_recent_event";
  public static final String APPLIATION_NAME = "application_name";
  public static final String TYPE = "type";
  public static final String EVENT_IDENTIFIER = "event_identifier";
  public static final String EVENT_STATE_STAMP = "event_state_stamp";
  public static final String WS_CONFIG_NAME =  "ws_config_name";
  public static final String WS_CLIENT_CONFIG_NAME = "wsclient_config_name";


  public static final String VERSION_630 = "6.30";

  public static final char SEPARATOR        = '/';
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  public static final String[] META_INF =  new String[]{"meta-inf", "META-INF"};
  public static final String  WEB_CLASSES_DIR = "WEB-INF" + SEPARATOR + "classes";

  public static final int JSR_DEPLOYMENT_MODE = 0;
  public static final int SAP_DEPLOYMENT_MODE = 1;
  public static final int RUNTIME_MODE        = 2;

  public static final int  WS_DEPLOYMENT         = 0;
  public static final int  WS_CLIENTS_DEPLOYMENT = 1;
  public static final int  WS_FULL_DEPLOYMENT    = 2;

  public static final int WS_TYPE  = 0;
  public static final int EJB_TYPE = 1;
  public static final int WEB_TYPE = 2;

  public static final String JAR_EXTENSION = ".jar";
  public static final String WAR_EXTENSION = ".war";
  public static final String WSAR_EXTENSION = ".wsar";

  public static int WS_INTERNAL_COMPONENT = 0;
  public static int WS_EXTERNAL_COMPONENT = 1;

  public static final String APP_JARS_NAME                            = "app_jars";
  public static final String MODULE_CRC_TABLE                         = "module_crc_table";
  public static final String MODULE_MAPPINS_FILE_NAME                 = "module_mappings.props";
  public static final String DEPLOYED_COMPONENTS_PER_MODULE_FILE_NAME = "deployed_components.props";
  public static final String MAPPINGS_FILE_NAME                       = "mapping.props";
  public static final String INDEX                                    = "index";
  public static final String DESCRIPTORS_NAME                         = "descriptors";
  public static final String DOC_NAME                                 = "doc";
  public static final String JARS_NAME                                = "jars";
  public static final String JAR_FILE_NAME                            = "jar_file_name";
  public static final String WSDL_NAME                                = "wsdl";
  public static final String EXTRACT_DIR_NAME                         = "extract";

  public static final String LITERAL_SUFFIX = "";
  public static final String SOAPENC_SUFFIX = "SOAPENC";

  // operation configuration properties
  public static final String NAMESPACE = "namespace";

  //webservice documentation file extension
  public static final String[] WS_DOCUMENTATION_EXTENSIONS = new String[]{".wsdef_en.xlf", ".wsdef.xlf"};

  //soap runtime constants
  public static final String SOAP_REQUEST_NAME = "SoapRequestWrapper";
  public static final String SOAP_RESPONSE_NAME = "SoapResponseWrapper";

  //in/out operation property indicating whether chema is qualified or not
  public static final String IS_QUALIFIED  =  "isQualified";

}
