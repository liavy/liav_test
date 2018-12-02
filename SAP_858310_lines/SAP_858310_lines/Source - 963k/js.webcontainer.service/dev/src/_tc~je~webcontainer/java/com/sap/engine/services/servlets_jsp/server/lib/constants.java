package com.sap.engine.services.servlets_jsp.server.lib;

import com.sap.engine.boot.SystemProperties;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.httpserver.lib.ParseUtils;

public interface Constants {
  public static final String DEFAULT_CHAR_ENCODING = "ISO-8859-1";
  public static final byte[] charset = "charset=".getBytes();
  //container
  //next file id in servlet_jsp sub configuration
  public static final String FILE_COUNTER = "FILE_COUNTER";
  public static final String CONTAINER_NAME = "servlet_jsp";
  //update configuration name
  public static final String UPDATE = "update";
  public static final String globalWebXmlPath = "global-web.xml";
  public static final String globalAddXmlPath = "web-j2ee-engine.xml";
  public static final MessageBytes defaultAliasMB = new MessageBytes(ParseUtils.separator.getBytes());
  public static final String defaultAliasDir = "_default";
  public static final String lineSeparator = SystemProperties.getProperty("line.separator");
  public static final String ERROR_HANDLER_SERVLET = "/SapErrorHandlerServlet";
	//constants for values of the dispatcher sub element of filter-mappings
	public static final String FILTER_DISPATCHER_REQUEST = "REQUEST";
	public static final String FILTER_DISPATCHER_FORWARD = "FORWARD";
	public static final String FILTER_DISPATCHER_INCLUDE = "INCLUDE";
	public static final String FILTER_DISPATCHER_ERROR = "ERROR";
  //name of the attribute that if present in the request, cache headers would    be added.
  public static final String FORWARD_TO_STATIC_PARAMETER = "com.sap.engine.servlets_jsp.forward.static.set-cache-headers";
  //name of the attribute
  public static final String ERROR_PAGE_TEMPLATE = "com.sap.engine.services.servlet_jsp.error_page_template";
  public static final String URI_FOR_GENERATING_ERROR_REPORT = "com.sap.engine.services.servlet_jsp.uri_for_generating_error_report";
  public static final String LOG_VIEWER_ALIAS_NAME = "webdynpro/resources/sap.com/tc~lm~itsam~ui~lv~client_ui";
}

