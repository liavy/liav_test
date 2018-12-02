/*
 * Copyright (c) 2002-2006 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.server.exceptions;

import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.localization.LocalizableTextFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Wrapper of DeploymentException in web container
 *
 * @author
 * @version 6.30
 */
public class WebDeploymentException extends DeploymentException {

  public static String ELEMENT_MUST_OCCUR_ONLY_ONE_TIME = "servlet_jsp_0000";
  public static String INVALID_URL_PATTERN_IN_DEPLOYMENT_DESCRIPTOR_FOR_WEBAPPLICATION = "servlet_jsp_0001";
  public static String CANNOT_GET_APPLICATION_SECURITY_CONTEXT_FOR_WEB_APPLICATION = "servlet_jsp_0002";
  public static String APPLICATIONCLASSLOADER_FOR_APPLICATION_IS_NULL = "servlet_jsp_0003";
  public static String THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY = "servlet_jsp_0004";
  public static String CANNOT_READ_XML_OR_ERROR_IN_XML_FOR_WEB_APPLICATION = "servlet_jsp_0005";
  public static String CANNOT_LOAD_DESCRIPTOR_FOR_WEBXML_FOR_WEB_APPLICATION = "servlet_jsp_0006";
  public static String APPLICATION_ALIAS_FOR_APPLICATION_ALREADY_EXISTS_IN_HTTP_SERVICE = "servlet_jsp_0007";
  public static String CANNOT_CREATE_SECURITY_SUBCONFIGURATION_FOR_ALIAS_OF_APPLICATION = "servlet_jsp_0008";
  public static String CANNOT_GET_POLICY_CONFIGURATION = "servlet_jsp_0009";
  public static String EXCEPTION_OCCURED_IN_METHOD_FOR_APPLICATION = "servlet_jsp_0010";
  public static String CANNOT_ADD_CONFIGURATION_ENTRY_WITH_KEY = "servlet_jsp_0011";
  public static String EMPTY_CONTEXT_ROOT_FOR_ARCHIVE_IN_APPLICATION = "servlet_jsp_0012";
  public static String CANNOT_COPY_FILE_IN_WORK_FOLDER_OF_APPLICATION = "servlet_jsp_0013";
  public static String CANNOT_STORE_FILE_FROM_APPLICATION_IN_DATABASE = "servlet_jsp_0014";
  public static String CANNOT_EXTRACT_THE_WAR_FILE_OF_THE_APPLICATION = "servlet_jsp_0015";
  public static String CANNOT_DOWNLOAD_THE_INFORMATION_OF_THE_APPLICATION_FROM_DATABASE = "servlet_jsp_0016";
  public static String ERROR_IN_STARTING_APPLICATION = "servlet_jsp_0017";
  public static String UPDATEING_DEPLOYMENT_DESCRIPTORU_IS_NOT_ALLOWED_AT_THIS_OPERATION = "servlet_jsp_0018";
  public static String CANNOT_GET_CONFIGURATION_FOR_APPLICATION = "servlet_jsp_0019";
  public static String CANNOT_UPDATE_FILE_IN_ARCHIVE = "servlet_jsp_0020";
  public static String NO_SUPPORT = "servlet_jsp_0021";
  public static String INCORRECT_PROPERTIES_FOR_DEPLOY_CANNOT_FIND_WAR_ARCHIVE_WITH_NAME = "servlet_jsp_0022";
  public static String CONFIGURATION_ERROR_WHILE_UPDATING_RUNTIME_PROPERTY_OF_APPLICATION = "servlet_jsp_0023";
  public static String ERROR_IN_STARTING_OF_WEB_APPLICATION = "servlet_jsp_0024";
  public static String ALTERNATIVE_DESCRIPTOR_NOT_FOUND = "servlet_jsp_0025";
  public static String UPDATE_OF_DIRECTORY_IS_NOT_SUPPORTED = "servlet_jsp_0026";
  public static String CANNOT_GET_NAMING_CONTEXT_ORBIND = "servlet_jsp_0027";
  public static String EXCEPTION_OCCURED_WHILE_CONVERTING_WAR = "servlet_jsp_0028";
  public static String CANNOT_UPDATE_CONFIGURATION_ENTRY_WITH_KEY = "servlet_jsp_0029";
  public static String APP_CONTEXT_ALREADY_EXISTS = "servlet_jsp_0030";
  public static String CANNOT_SUBSTITUTE_STREAM = "servlet_jsp_0031";
  public static String DEPLOY_JACC_ERROR = "servlet_jsp_0032";
  public static String CANNOT_GET_CONFIGURATION_HANDLER = "servlet_jsp_0033";
  public static String ROLLBACK_RUNTIME_CHANGES = "servlet_jsp_0034";
  public static String MODEL_ALREADY_CHANGED = "servlet_jsp_0035";
  public static String CANNOT_UPDATE_WEB_DD_IN_ADMIN = "servlet_jsp_0036";
  public static String CANNOT_DELETE_CUSTOM_SETTINGS_OF_DELETED_WEB_APPLICATION = "servlet_jsp_0037";
  public static String ERROR_PROCESSING_SECURITY_ROLE_REF = "servlet_jsp_0038";
  public static String CANNOT_LOAD_PROVIDED_RESOURCES = "servlet_jsp_0039";
  public static String CANNOT_REGISTER_POLICY_CONFIG = "servlet_jsp_0040";
  public static String CANNOT_CREATE_PRIVATE_TEMPDIR = "servlet_jsp_0041";
  public static String CANNOT_STORE_PRIVATE_RES_REF = "servlet_jsp_0042";
  public static String CANNOT_STORE_FILES_FOR_PRIVATE_CL = "servlet_jsp_0043";
  public static String CANNOT_READ_PRIVATE_RES_REF = "servlet_jsp_0044";
  public static String CANNOT_READ_FILES_FOR_PRIVATE_CL = "servlet_jsp_0045";
  public static String ERROR_OCCURRED_STARTING_WCE_COMPONENTS = "servlet_jsp_0046";
  public static String THREAD_INTERRUPTED_WHILE_STARTING_WCE_COMPONENTS = "servlet_jsp_0047";
  public static String APPLICATION_WAS_NOT_MIGRATED = "servlet_jsp_0048";
  public static String CANNOT_STORE_PROVIDED_RESOURCES = "servlet_jsp_0049";
  public static String CANNOT_REGISTER_FILES_FOR_PRVATE_CL = "servlet_jsp_0050";
  public static String CANNOT_STORE_WCE_IN_DEPLOY = "servlet_jsp_0051";
  public static String CANNOT_LOAD_WCE_IN_DEPLOY = "servlet_jsp_0052";
  public static String CACHE_IS_EMPTY = "servlet_jsp_0053";
  public static String WEB_XML_NOT_IN_CACHE = "servlet_jsp_0054";
  public static String CANNOT_STORE_THE_MODIFIED_XML_FILE = "servlet_jsp_0055";
  public static String CANNOT_USE_URL_SESSION_TRACKING = "servlet_jsp_0056";
  public static String CANNOT_GET_WEB_DD_OBJECT_FROM_DB = "servlet_jsp_0057";
  public static String CANNOT_GET_APP_META_DATA_OBJECT_FROM_DB = "servlet_jsp_0058";
  public static String CANNOT_START_APPLICATION_BECAUSE_THERE_IS_ANOTHER_APPL_WITH_HIGHER_PRIO = "servlet_jsp_0059";

  public WebDeploymentException(String s, Object[] args, Throwable t) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args), t);
    //super.setLogSettings(WebResourceAccessor.category, Severity.ERROR, WebResourceAccessor.location);
  }

  public WebDeploymentException(String s, Throwable t) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s), t);
    //super.setLogSettings(WebResourceAccessor.category, Severity.ERROR, WebResourceAccessor.location);
  }

  public WebDeploymentException(String s, Object[] args) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args));
    //super.setLogSettings(WebResourceAccessor.category, Severity.ERROR, WebResourceAccessor.location);
  }


  public WebDeploymentException(String s) {
    super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s));
    //super.setLogSettings(WebResourceAccessor.category, Severity.ERROR, WebResourceAccessor.location);
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new DeploymentException(stringWriter.toString());
  }

}
