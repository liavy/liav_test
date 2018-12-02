/*
 * Copyright (c) 2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server;

import com.sap.engine.lib.util.HashMapIntObject;
import com.sap.engine.services.servlets_jsp.server.logging.LogLocation;
import com.sap.engine.services.servlets_jsp.server.logging.LogCategory;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class LogContext {
	//Categories
	/**
	 * Used when logging problems related to deploy/remove/update/start/stop of the application.
	 */
  public static final int CATEGORY_DEPLOY = 1;
  /**
   * Used when logging problems related to requests/response processing.
   */
  public static final int CATEGORY_REQUESTS = 2;
  /**
   * Used ONLY when logging problems via methods in <code>ServletContext</code>
   * (log(String s, Throwable throwable) and log(Exception exception, String s)).
   * Don't use it for anything else, because the destination is different - these log messages
   * will be logged in the applications.log file and not in the server.log file.
   */
  public static final int CATEGORY_WEB_APPLICATIONS = 3;
  /**
   * Used when logging problems related to Web Container as a service - such as start, stop of the service etc.
   */
  public static final int CATEGORY_SERVICE = 4;
  /**
   * Used when logging problems related to security
   */
  public static final int CATEGORY_SECURITY = 6;
  /**
   * Used ONLY for CHANGE LOG!!!
   */
  public static final int CATEGORY_CHANGE_LOG = 7;
  public static final int CATEGORY_CHANGE_LOG_PROPERTIES = 8;
  public static final int CATEGORY_CHANGE_LOG_LIFECYCLE = 9;

  //Locations
  /**
   * USed when tracing problems related to deploy/remove/update/start/stop of the application.
   */
  public static final int LOCATION_DEPLOY = 3;
  /**
   * Used when tracing problems related to parsing and loading of tag libraries,
   * loading/initializing/invoking of servlets, jsps and filters. Also when destroying web components.
   */
  public static final int LOCATION_REQUEST_INFO_SERVER = 4;
  /**
   * Used when tracing problems related to methods of the HttpServletResponse API that are invoked.
   * This information may be used to track for example where an invalid content-length or any other header is set.
   * Information about the called methods, their parameters, additional parameters values that are important for method behavior
   * such as whether the response is submitted or whether getWriter() is called, return value.
   */
  public static final int LOCATION_SERVLET_RESPONSE = 5;
  /**
   * Used when tracing problems related to methods of the HttpServletRequest API that are invoked.
   * This location is used to track how different request attributes, headers, etc... are being accessed/modified.
   * Information about the called methods, their parameters, additional parameters values that are important for method behavior
   * such as whether the request parameters are parsed or whether getReader() is called, return value.
   */
  public static final int LOCATION_SERVLET_REQUEST = 6;
  /**
   * Used when tracing problems related to the methods of HttpSession,
   * related to the session life cycle - create, destroy, access, access time etc.
   */
  public static final int LOCATION_HTTP_SESSION_LIFECYCLE = 7;
  /**
   * Used when tracing problem related to the methods of HttpSession -
   * the name of the method, the value of the attribute and some additional info where relevant.
   */
  public static final int LOCATION_HTTP_SESSION_ATTRIBUTES = 8;
  /**
   * Used when tracing problems related to Web Container as a service - such as start, stop of the service etc.
   * Also problems related to mbeans.
   */
  public static final int LOCATION_SERVICE = 9;
  /**
   * Used when tracing problems via methods in <code>ServletContext</code>.
   */
  public static final int LOCATION_WEB_APPLICATIONS = 10;
  /**
   * Used when tracing problems related to JSP Parser.
   */
  public static final int LOCATION_JSP_PARSER = 11;
  /**
   * Used when tracing problems related to request processing steps, application status and configuration.
   */
  public static final int LOCATION_REQUEST_INFO_CLIENT = 12;
  /**
   * Used when tracing problems related to security.
   */
  public static final int LOCATION_SECURITY = 13;
  /**
   * Used when tracing problems related to administration of the web applications.
   */
  public static final int LOCATION_WEBADMIN = 14;
  /**
   * Used when tracing problems related to Web Container Extension Providers.
   */
  public static final int LOCATION_WEBCONTAINERPROVIDER = 15;
  /**
   * Used when tracing problems related JSP Parser XML View.
   */
  public static final int LOCATION_JSP_PARSER_XML_VIEW = 16;
  /**
   * Used when tracing problems related to multipart.
   */
  public static final int LOCATION_MULTIPART = 17;
  /**
   * Location for tracing request preservation mechanism
   */
  public static final int LOCATION_REQUEST_PRESERVATION = 18;
  /**
   * Location for tracing Internal Server Error 500.
   * DO NOT USE THIS Location separately. 
   * There is dedicated error handler, method and message ID that can be used for ISE 500 tracing.
   */
  public static final int LOCATION_ISE_500 = 19;  
  /**
   * 
   */
  public static final int LOCATION_STATISTICS = 20;

  /**
   * Location for tracing the integration of quality of service 
   */
  public static final int LOCATION_QUALITY_OF_SERVICE_INTEGRATION = 21; 

  //Categories names
  public static final String CATEGORY_DEPLOY_NAME = "System/Server";
  public static final String CATEGORY_REQUESTS_NAME = "System/Server/WebRequests";
  public static final String CATEGORY_WEB_APPLICATIONS_NAME = "Applications/WebApplications";
  public static final String CATEGORY_SERVICE_NAME = "System/Server";
  public static final String CATEGORY_SECURITY_NAME = "System/Server";
  public static final String CATEGORY_CHANGE_LOG_NAME = "System/Changes";
  public static final String CATEGORY_CHANGE_LOG_PROPERTIES_NAME = "System/Changes/Properties";
  public static final String CATEGORY_CHANGE_LOG_LIFECYCLE_NAME = "System/Changes/Lifecycle";

  //Locations names
  public static final String LOCATION_DEPLOY_NAME = "com.sap.engine.services.servlets_jsp.Deploy";
  public static final String LOCATION_SERVLET_RESPONSE_NAME = "com.sap.engine.services.servlets_jsp.client.ServletResponse";
  public static final String LOCATION_SERVLET_REQUEST_NAME = "com.sap.engine.services.servlets_jsp.client.ServletRequest";
  public static final String LOCATION_HTTP_SESSION_LIFECYCLE_NAME = "com.sap.engine.services.servlets_jsp.client.HttpSession.lifecycle";
  public static final String LOCATION_HTTP_SESSION_ATTRIBUTES_NAME = "com.sap.engine.services.servlets_jsp.client.HttpSession.attributes";
  public static final String LOCATION_REQUEST_INFO_SERVER_NAME = "com.sap.engine.services.servlets_jsp.client.RequestInfoServer";
  public static final String LOCATION_REQUEST_INFO_CLIENT_NAME = "com.sap.engine.services.servlets_jsp.client.RequestInfoClient";
  public static final String LOCATION_SERVICE_NAME = "com.sap.engine.services.servlets_jsp.Service";
  public static final String LOCATION_WEB_APPLICATIONS_NAME = "com.sap.engine.services.servlets_jsp.WebApplications";
  public static final String LOCATION_JSP_PARSER_NAME = "com.sap.engine.services.servlets_jsp.jspparser_api.jspparser";
  public static final String LOCATION_SECURITY_NAME = "com.sap.engine.services.servlets_jsp.Security";
  public static final String LOCATION_WEBADMIN_NAME = "com.sap.engine.services.servlets_jsp.admin";
  public static final String LOCATION_WEBCONTAINERPROVIDER_NAME = "com.sap.engine.services.servlets_jsp.WebContainerProvider";
  public static final String LOCATION_JSP_PARSER_XML_VIEW_NAME = "com.sap.engine.services.servlets_jsp.jspparser.xmlViewDump";
  public static final String LOCATION_MULTIPART_NAME = "com.sap.engine.services.servlets_jsp.Multipart";
  public static final String LOCATION_REQUEST_PRESERVATION_NAME = "com.sap.engine.services.servlets_jsp.RequestPreservation";
  public static final String LOCATION_ISE_500_NAME = "com.sap.engine.services.servlets_jsp.ISE500";
  public static final String LOCATION_STATISTICS_NAME = "com.sap.engine.services.servlets_jsp.Statistics"; 
  public static final String LOCATION_QoS_NAME = "com.sap.engine.services.servlets_jsp.QoS"; 

  public static HashMapIntObject categories = new HashMapIntObject();
  public static HashMapIntObject locations = new HashMapIntObject();

  private static Category categoryDeploy = null;
  private static Category categoryRequests = null;
  private static Category categoryWebApplications = null;
  private static Category categoryService = null;
  private static Category categorySecurity = null;
  private static Category categoryChangeLog = null;
  private static Category categoryChangeLogProperties = null;
  private static Category categoryChangeLogLifecycle = null;

  private static Location locationDeploy = null;
  private static Location locationRequestInfoServer = null;
  private static Location locationRequestInfoClient = null;
  private static Location locationServletRequest = null;
  private static Location locationServletResponse = null;
  private static Location locationHttpSessionLifecycle = null;
  private static Location locationHttpSessionAttributes = null;
  private static Location locationService = null;
  private static Location locationWebApplications = null;
  private static Location locationJspParser = null;
  private static Location locationSecurity = null;
  private static Location locationWebadmin = null;
  private static Location locationWebContainerProvider = null;
  private static Location locationJspParserXmlView = null;
  private static Location locationMultipart = null;
  private static Location locationRequestPreservation = null;
  private static Location locationISE500 = null;
  private static Location locationStatistics = null;
  private static Location locationQoS = null;

  public static void init() {
    locations.put(LOCATION_DEPLOY, new LogLocation(LOCATION_DEPLOY_NAME));
    locations.put(LOCATION_REQUEST_INFO_SERVER, new LogLocation(LOCATION_REQUEST_INFO_SERVER_NAME));
    locations.put(LOCATION_REQUEST_INFO_CLIENT, new LogLocation(LOCATION_REQUEST_INFO_CLIENT_NAME));
    locations.put(LOCATION_SERVLET_REQUEST, new LogLocation(LOCATION_SERVLET_REQUEST_NAME));
    locations.put(LOCATION_SERVLET_RESPONSE, new LogLocation(LOCATION_SERVLET_RESPONSE_NAME));
    locations.put(LOCATION_HTTP_SESSION_LIFECYCLE, new LogLocation(LOCATION_HTTP_SESSION_LIFECYCLE_NAME));
    locations.put(LOCATION_HTTP_SESSION_ATTRIBUTES, new LogLocation(LOCATION_HTTP_SESSION_ATTRIBUTES_NAME));
    locations.put(LOCATION_SERVICE, new LogLocation(LOCATION_SERVICE_NAME));
    locations.put(LOCATION_WEB_APPLICATIONS, new LogLocation(LOCATION_WEB_APPLICATIONS_NAME));
    locations.put(LOCATION_JSP_PARSER, new LogLocation(LOCATION_JSP_PARSER_NAME));
    locations.put(LOCATION_SECURITY, new LogLocation(LOCATION_SECURITY_NAME));
    locations.put(LOCATION_WEBADMIN, new LogLocation(LOCATION_WEBADMIN_NAME));
    locations.put(LOCATION_WEBCONTAINERPROVIDER, new LogLocation(LOCATION_WEBCONTAINERPROVIDER_NAME));
    locations.put(LOCATION_JSP_PARSER_XML_VIEW, new LogLocation(LOCATION_JSP_PARSER_XML_VIEW_NAME));
    locations.put(LOCATION_MULTIPART, new LogLocation(LOCATION_MULTIPART_NAME));
    locations.put(LOCATION_REQUEST_PRESERVATION, new LogLocation(LOCATION_REQUEST_PRESERVATION_NAME));
    locations.put(LOCATION_ISE_500, new LogLocation(LOCATION_ISE_500_NAME));
    locations.put(LOCATION_STATISTICS, new LogLocation(LOCATION_STATISTICS_NAME));
    locations.put(LOCATION_QUALITY_OF_SERVICE_INTEGRATION, new LogLocation(LOCATION_QoS_NAME));

    locationDeploy = getLocation(LOCATION_DEPLOY).getLocation();
    locationRequestInfoServer = getLocation(LOCATION_REQUEST_INFO_SERVER).getLocation();
    locationRequestInfoClient = getLocation(LOCATION_REQUEST_INFO_CLIENT).getLocation();
    locationServletRequest = getLocation(LOCATION_SERVLET_REQUEST).getLocation();
    locationServletResponse = getLocation(LOCATION_SERVLET_RESPONSE).getLocation();
    locationHttpSessionLifecycle = getLocation(LOCATION_HTTP_SESSION_LIFECYCLE).getLocation();
    locationHttpSessionAttributes = getLocation(LOCATION_HTTP_SESSION_ATTRIBUTES).getLocation();
    locationService = getLocation(LOCATION_SERVICE).getLocation();
    locationWebApplications = getLocation(LOCATION_WEB_APPLICATIONS).getLocation();
    locationJspParser = getLocation(LOCATION_JSP_PARSER).getLocation();
    locationSecurity = getLocation(LOCATION_SECURITY).getLocation();
    locationWebadmin = getLocation(LOCATION_WEBADMIN).getLocation();
    locationWebContainerProvider = getLocation(LOCATION_WEBCONTAINERPROVIDER).getLocation();
    locationJspParserXmlView = getLocation(LOCATION_JSP_PARSER_XML_VIEW).getLocation();
    locationMultipart = getLocation(LOCATION_MULTIPART).getLocation();
    locationRequestPreservation = getLocation(LOCATION_REQUEST_PRESERVATION).getLocation();
    locationISE500 = getLocation(LOCATION_ISE_500).getLocation();
    locationStatistics = getLocation(LOCATION_STATISTICS).getLocation();
    locationQoS = getLocation(LOCATION_QUALITY_OF_SERVICE_INTEGRATION).getLocation();

    categories.put(CATEGORY_DEPLOY, new LogCategory(CATEGORY_DEPLOY_NAME, LOCATION_DEPLOY));
    categories.put(CATEGORY_REQUESTS, new LogCategory(CATEGORY_REQUESTS_NAME, LOCATION_REQUEST_INFO_SERVER));
    categories.put(CATEGORY_WEB_APPLICATIONS, new LogCategory(CATEGORY_WEB_APPLICATIONS_NAME, LOCATION_WEB_APPLICATIONS));
    categories.put(CATEGORY_SERVICE, new LogCategory(CATEGORY_SERVICE_NAME, LOCATION_SERVICE));
    categories.put(CATEGORY_SECURITY, new LogCategory(CATEGORY_SECURITY_NAME, LOCATION_SECURITY));
    categories.put(CATEGORY_CHANGE_LOG, new LogCategory(CATEGORY_CHANGE_LOG_NAME, LOCATION_SERVICE));
    categories.put(CATEGORY_CHANGE_LOG_PROPERTIES, new LogCategory(CATEGORY_CHANGE_LOG_PROPERTIES_NAME, LOCATION_SERVICE));
    categories.put(CATEGORY_CHANGE_LOG_LIFECYCLE, new LogCategory(CATEGORY_CHANGE_LOG_LIFECYCLE_NAME, LOCATION_SERVICE));

    categoryDeploy = getCategory(CATEGORY_DEPLOY).getCategory();
    categoryRequests = getCategory(CATEGORY_REQUESTS).getCategory();
    categoryWebApplications = getCategory(CATEGORY_WEB_APPLICATIONS).getCategory();
    categoryService = getCategory(CATEGORY_SERVICE).getCategory();
    categorySecurity = getCategory(CATEGORY_SECURITY).getCategory();
    categoryChangeLog = getCategory(CATEGORY_CHANGE_LOG).getCategory();
    categoryChangeLogProperties = getCategory(CATEGORY_CHANGE_LOG_PROPERTIES).getCategory();
    categoryChangeLogLifecycle = getCategory(CATEGORY_CHANGE_LOG_LIFECYCLE).getCategory();
  }

  public static LogLocation getLocation(int location) {
    return (LogLocation)locations.get(location);
  }

  public static LogCategory getCategory(int category) {
    return (LogCategory)categories.get(category);
  }

  public static String getExceptionStackTrace(Throwable t) {
    ByteArrayOutputStream ostr = new ByteArrayOutputStream();
    t.printStackTrace(new PrintStream(ostr));
    return ostr.toString();
  }

  public static Category getCategoryDeploy() {
    return categoryDeploy;
  }

  public static Category getCategoryRequests() {
    return categoryRequests;
  }

  public static Category getCategorySecurity() {
    return categorySecurity;
  }

  public static Category getCategoryService() {
    return categoryService;
  }

  public static Category getCategoryWebApplications() {
    return categoryWebApplications;
  }

  public static Category getCategoryChangeLog() {
		return categoryChangeLog;
	}

	public static Category getCategoryChangeLogProperties() {
		return categoryChangeLogProperties;
	}

  public static Category getCategoryChangeLogLifecycle() {
    return categoryChangeLogLifecycle;
  }

	public static Location getLocationDeploy() {
    return locationDeploy;
  }

  public static Location getLocationHttpSessionAttributes() {
    return locationHttpSessionAttributes;
  }

  public static Location getLocationHttpSessionLifecycle() {
    return locationHttpSessionLifecycle;
  }

  public static Location getLocationJspParser() {
    return locationJspParser;
  }

  public static Location getLocationRequestInfoClient() {
    return locationRequestInfoClient;
  }

  public static Location getLocationRequestInfoServer() {
    return locationRequestInfoServer;
  }

  public static Location getLocationSecurity() {
    return locationSecurity;
  }

  public static Location getLocationService() {
    return locationService;
  }

  public static Location getLocationServletRequest() {
    return locationServletRequest;
  }

  public static Location getLocationServletResponse() {
    return locationServletResponse;
  }

  public static Location getLocationWebApplications() {
    return locationWebApplications;
  }

  public static Location getLocationWebadmin() {
    return locationWebadmin;
  }

  public static Location getLocationWebContainerProvider() {
  	return locationWebContainerProvider;
  }

  public static Location getLocationJspParserXmlView() {
  	return locationJspParserXmlView;
  }

  public static Location getLocationMultipart() {
	return locationMultipart;
  }

  public static Location getLocationRequestPreservation(){
	  return locationRequestPreservation;
  }

  public static Location getLocationISE500() {
    return locationISE500;
  }//end of getLocationISE500()

  public static Location getLocationStatistics() {
    return locationStatistics;
  }
  
  public static Location getLocationQoS() {
    return locationQoS;
  }
}

