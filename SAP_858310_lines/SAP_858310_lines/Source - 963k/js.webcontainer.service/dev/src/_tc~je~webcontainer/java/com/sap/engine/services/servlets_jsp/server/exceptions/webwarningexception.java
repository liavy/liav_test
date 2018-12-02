/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.server.exceptions;

import com.sap.engine.services.deploy.container.WarningException;
import com.sap.localization.LocalizableTextFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Wrapper of DeploymentException in web container
 *
 * @author Violeta Uzunova
 * @version 6.30
 */
public class WebWarningException extends WarningException {
  public static String ERROR_IN_TAGLIB_TAG_IN_WEBXML_CANNOT_LOCATE_OR_PARSE_IT = "servlet_jsp_0100";
  public static String CANNOT_DOWNLOAD_UPDATES_OF_APPLICATION_FROM_DATABASE_IN_METHOD = "servlet_jsp_0101";
  public static String INITIALIZATION_OF_FILTER_FAILED_PLEASE_CHECK_INIT_METHOD_OF_FILTER_ERROR_IS = "servlet_jsp_0102";
  public static String CANNOT_PARSE_JSP_ERROR_IS = "servlet_jsp_0103";
  public static String CANNOT_LOAD_SERVLET_ERROR_IS = "servlet_jsp_0104";
  public static String INITIALIZATION_OF_SERVLET_FAILED_PLEASE_CHECK_INIT_METHOD_OF_SERVLET_ERROR_IS = "servlet_jsp_0105";
  public static String CANNOT_BIND_EJB_REFERENCE = "servlet_jsp_0106";
  public static String RESOURCE_IN_WEB_APPLICATION_WILL_NOT_BE_USED_REASON_IS = "servlet_jsp_0107";
  public static String UNRECOGNIZED_RESOURCEENVTYPE_IN_WEB_APPLICATION_IT_WILL_BE_IGNORED = "servlet_jsp_0108";
  public static String CANNOT_BIND_WEBSERVICE_CLIENT = "servlet_jsp_0109";
  public static String CANNOT_BIND_EJB_LOCAL_REFERENCE = "servlet_jsp_0110";
  public static String CANNOT_REMOVE_APPLICATION_DURING_DEPLOY_ROLLBACK = "servlet_jsp_0111";
  public static String CANNOT_LOAD_FILTER = "servlet_jsp_0112";
  public static String CANNOT_LOAD_LISTENER = "servlet_jsp_0113";
  public static String UNSUPPORTED_LISTENER_CLASS = "servlet_jsp_0114";
  public static String ERROR_OCCURED_WHILE_SERVLET_IS_INIT_WITH_RUN_AS_IDENTITY_ERROR_IS = "servlet_jsp_0115";
  public static String CANNOT_REMOVE_ALIAS_FROM_HTTP_WHILE_REMOVING_APPLICATION = "servlet_jsp_0116";
  public static String CANNOT_NOTIFY_RUNTIME_INTERFACE_FOR_CHANGING_STATUS_OF_APPLICATION_ERROR_IS = "servlet_jsp_0117";
  public static String ERROR_IN_SECURITY_MAPPINGS_IN_DESCRIPTOR_IN_WEB_APPLICATION_TO_DEFAULT_SERVER_SECURITY_MAPPINGS_ERROR_IS = "servlet_jsp_0118";
  public static String CANNOT_BIND_WEB_SERVICES_CLIENTS_FOR_APPLICATION_ERROR_IS = "servlet_jsp_0119";
  public static String CANNOT_FIND_FILE_IN_WAR_FILE = "servlet_jsp_0057";
  public static String ERROR_OCCURRED_DURING_FACTORY_INITIALIZATION_FOR_INJECTION = "servlet_jsp_0120";
  public static String ERROR_OCCURRED_DURING_INJECTING_NAMING_RESOURCES = "servlet_jsp_0121";
  public static String CANNOT_BIND_ORB = "servlet_jsp_0122";
  public static String CANNOT_BIND_COMPONENT_REFERENCE = "servlet_jsp_0123";
  public static String CANNOT_BIND_ENV_ENTRY = "servlet_jsp_0124";
  public static String CANNOT_BIND_USERTRANSACTION = "servlet_jsp_0125";
  public static String CANNOT_BIND_RESOURCE_REFERENCE = "servlet_jsp_0126";
  public static String CANNOT_BIND_RESOURCE_ENV_REFERENCE = "servlet_jsp_0127";
  public static String CANNOT_BIND_ENTITY_MANAGER = "servlet_jsp_0128";
  public static String CANNOT_BIND_ENTITY_MANAGER_FACTORY = "servlet_jsp_0129";
  public static String CANNOT_BIND_MSG_DEST_REF = "servlet_jsp_0130";
  public static String PROBLEM_WHILE_RESOLVING_RELATIVE_JAR = "servlet_jsp_0131";
  public static String MISSING_EJB_CACHE_FOR_JAR = "servlet_jsp_0132";
  public static String EJB_MODEL_FOR_JAR_IS_MISSING = "servlet_jsp_0133";
  public static String LOGICAL_MESSAGE_DESTINATION_NOT_FOUND_IN_JAR = "servlet_jsp_0134";
  public static String LOGICAL_MESSAGE_DESTINATION_NOT_FOUND_IN_ALL_JARS = "servlet_jsp_0135";
  public static String TOO_FEW_FILE_DESCRIPTORS = "servlet_jsp_0136";
  //TODO:Polly check
  public static String ERROR_IN_DESTROING_SESSION_DOMAIN = "servlet_jsp_0137";
  public static String CANNOT_CLEAR_HTTP_CACHE = "servlet_jsp_0138";
  public static String CANNOT_SYNCHROMIZE_APPLICATION_DURING_ROLLBACK_UPDATE = "servlet_jsp_0139";
  public static String CANNOT_NOTIFY_WCE_THAT_APPLICATION_IS_DEPLOYED_DURING_ROLLBACK_UPDATE = "servlet_jsp_0140";
  public static String CANNOT_COMMIT_APPLICATION_UPDATE = "servlet_jsp_0141";
  public static String WEB_APPLICATION_NOT_STOPPED_BEFEORE_REMOVE = "servlet_jsp_0142";
  public static String CANNOT_NOTIFY_FOR_REMOVING_APPLICATION = "servlet_jsp_0143";
  public static String CANNOT_NOTIFY_FOR_DEPLOYING_APPLICATION = "servlet_jsp_0144";
  public static String ERROR_IN_REMOVING_APPLICATION_ALIAS = "servlet_jsp_0145";
  public static String HHTP_SERVICE_IS_NOT_STOPPED_AND_CANNOT_CLEAR_HTTP_CACHE = "servlet_jsp_0146";
  public static String CANNOT_INITIALIZE_CONTEXT = "servlet_jsp_0147";
  public static String CANNOT_STOP_WEB_APPLICATION = "servlet_jsp_0148";
  public static String CANNOT_BIND_MESSAGE_DESTINATION = "servlet_jsp_0149";
  
  public WebWarningException(String s, Object[] args, Throwable t) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args), t);
    //super.setLogSettings(WebResourceAccessor.category, Severity.WARNING, WebResourceAccessor.location);
  }

  public WebWarningException(String s, Throwable t) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s), t);
    //super.setLogSettings(WebResourceAccessor.category, Severity.WARNING, WebResourceAccessor.location);
  }

  public WebWarningException(String s, Object[] args) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args));
    //super.setLogSettings(WebResourceAccessor.category, Severity.WARNING, WebResourceAccessor.location);
  }


  public WebWarningException(String s) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s));
    //super.setLogSettings(WebResourceAccessor.category, Severity.WARNING, WebResourceAccessor.location);
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new WarningException(stringWriter.toString());
  }

}
