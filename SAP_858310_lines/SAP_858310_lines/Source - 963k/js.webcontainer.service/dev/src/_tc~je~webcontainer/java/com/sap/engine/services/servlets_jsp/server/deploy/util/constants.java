/*
* Copyright (c) 2004-2009 by SAP AG, Walldorf.,
* http://www.sap.com
* All rights reserved.
*
* This software is the confidential and proprietary information
* of SAP AG, Walldorf. You shall not disclose such Confidential
* Information and shall use it only in accordance with the terms
* of the license agreement you entered into with SAP.
*/
package com.sap.engine.services.servlets_jsp.server.deploy.util;

import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;

/**
 * @author Violeta Georgieva
 * @version 7.0
 */
public final class Constants {
  //Container
  //update configuration name
  public static final String UPDATE = "update";
  //backup configuration name
  public static final String BACKUP = "backup";
  //admin configuration name
  public static final String ADMIN = "admin";
  //next file id in servlet_jsp subconfiguration
  public static final String FILE_COUNTER = "FILE_COUNTER";
  public static final String ALT_DD = "altDD_";
  public static final String WEB_DD = "webDD_";
  public static final String ADD_WEB_DD = "addwebDD_";
  public static final String MERGED_WEB_DD = "mergedWebDD_";
  public static final String ANNOTATIONS_DD = "annotationsDD_";
  public static final String WEB_DD_AS_OBJECT = "webDDAsObject_";
  public static final String WS_END_POINTS = "wsEndPoints_";
  public static final String FILES_FOR_PRIVATE_CL = "FILES_FOR_PRIVATE_CL";
  public static final String PRIVATE_RESOURCE_REFERENCES = "PRIVATE_RESREF";
  public static final String WCE_IN_DEPLOY = "WCE_IN_DEPLOY";
  public static final String FAIL_OVER = "failOver";
  public static final String CONTAINER_NAME = "servlet_jsp";
  public static final String MODIFICATION_FLAG = "modificationFlag";
  public static final MessageBytes defaultAliasMB = new MessageBytes(ParseUtils.separator.getBytes());
  public static final String defaultAliasDir = "_default";
  public static final String PRIVATE_CL_NAME = "WCE_PRIVATE_CL_";
  public static final String WCE_CONFIG_PREFIX = "WCEPROVIDER_"; 
  public static final String WCE_WEBMODULE_CONFIG_PREFIX = "WCEWM_";
  public static final String APP_META_DATA_AS_OBJECT = "appMetaDataAsObject";
  
  //property entries names
  public static final String URL_SESSION_TRACKING = "urlsessiontracking";
  public static final String SESSION_TIMEOUT = "sessiontimeout";
  public static final String MAX_SESSIONS = "maxsessions";
  public static final String COOKIE_CONFIG = "cookieconfig";

  public static final String WEB_XML = "web.xml";
  public static final String WEB_J2EE_ENGINE_XML = "web-j2ee-engine.xml";
  public static final String WEB_J2EE_ENGINE_TAG = "web-j2ee-engine";
  public static final String FLAG_TAG = "flag";

  public static final String WEB_J2EE_ENGINE_DEFAULT_CONTENT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
    + "<web-j2ee-engine xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"web-j2ee-engine.xsd\">"
    + "<spec-version>2.3</spec-version>"
    + "</web-j2ee-engine>";

  public static final String ENCODING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  public static final String ENCODING_EXTENDED = "<?xml version=\"1.0\" encoding=\"UTF-8\"".toLowerCase();
  public static final String DEFAULT_CHAR_ENCODING = "ISO-8859-1";
}//end of class
