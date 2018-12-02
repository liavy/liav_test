/*
 * Copyright (c) 2000-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.servlet;

import static com.sap.engine.services.servlets_jsp.server.LogContext.getExceptionStackTrace;
import static com.sap.engine.services.servlets_jsp.server.ServiceContext.getServiceContext;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.protocol.HeaderValues;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebApplicationException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebServletException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebUnavailableException;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.*;
import com.sap.tc.logging.Location;

public class PutServlet extends HttpServlet {
  private static final byte[] response_body_beg = "The file ".getBytes();
  private static final byte[] response_body_end = " has been successfully uploaded.".getBytes();
  private static final String web_inf = ParseUtils.separator + "WEB-INF";
  private static final String meta_inf = ParseUtils.separator + "META-INF";
  private static final String UPLOAD_ENABLED = "Upload Enabled";
  private static final String DENY_LIST = "DenyList";
  private Vector denyList = new Vector();

  transient private static Location currentLocation = Location.getLocation(PutServlet.class);

  transient private FileDeployer fileDeployer = null;

  transient private HttpProvider httpProvider = null;
  transient private ApplicationContext applicationContext = null;
  private String appName = null;
  private String rootDir = null;
  private boolean uploadAllowed = false;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    applicationContext = ((ServletContextImpl) config.getServletContext()).getApplicationContext();
    try {
      fileDeployer = new FileDeployer(
			ServiceContext.getServiceContext().getConfigurationAccessor().getConfigurationHandler(),
			ServiceContext.getServiceContext());
    } catch (IOException io) {
      throw new WebUnavailableException(WebUnavailableException.PUT_SERVLET_NOT_INITIALIZED, io);
    }
    httpProvider = ServiceContext.getServiceContext().getHttpProvider();
    appName = applicationContext.getApplicationName();
    rootDir = ParseUtils.separatorChar + appName.replace('/', ParseUtils.separatorChar)
        + ParseUtils.separatorChar + Constants.CONTAINER_NAME + ParseUtils.separatorChar;
    String uploadAllowedStr = config.getInitParameter(UPLOAD_ENABLED);
    if (uploadAllowedStr != null) {
      uploadAllowed = uploadAllowedStr.equalsIgnoreCase("true");
    }
    if (!uploadAllowed) {
    	//TODO:polly ok
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000208",
        "HTTP upload of static files will not be allowed. "
        + "To enable file upload in your application [{0}] you can use the standard HTTP 1.1 implementation of the PUT method provided by the Web Container. "
        + "To enable it you need to set the property "
        + "\"Upload Allowed\" of the servlet {1} to \"true\" in the global-web.xml file.", 
        new Object[]{appName, getServletName()}, appName, applicationContext.getCsnComponent());
    }

    String denyListStr = config.getInitParameter(DENY_LIST);
    if (denyListStr != null && !denyListStr.trim().equals("")) {
      StringTokenizer st = new StringTokenizer(denyListStr, ";", false);
      while (st.hasMoreTokens()) {
        String ext = st.nextToken().trim();
        if (ext.startsWith("*.")) { // only file extentions are allowed as mask
          denyList.add(ext.substring(1).toLowerCase());
        }
      }
    }
  }

  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (!uploadAllowed) {
      throw new WebServletException(WebServletException.PUT_NOT_ALLOWED);
    }
    HttpServletRequestFacadeWrapper unwrappedRequest = FilterUtils.unWrapRequest(request);
    String contextPath = request.getContextPath();
    String root = new File(applicationContext.getWebApplicationRootDir()).getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar);
    String realPath = new File(unwrappedRequest.getRealPathLocal("/") + File.separator).getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar);
    String fileName = null;
    String fileNameDecoded = null;
    boolean isHttpAlias = false;
    if (contextPath.equals("") && !realPath.startsWith(root)) { // http alias
      isHttpAlias = true;
      root = realPath;
      fileName = unwrappedRequest.getRealPathLocal(unwrappedRequest.getRequestURIinternal()).trim();
      fileNameDecoded = unwrappedRequest.getRealPathLocal(unwrappedRequest.getRequestURI()).trim();
    } else {
      String requestURI = unwrappedRequest.getRequestURIinternal();
      int beginIndex = requestURI.indexOf(contextPath);
      int endIndex = beginIndex + contextPath.length();
      requestURI = requestURI.substring(endIndex);
      fileName = (root + requestURI).replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar).trim();
      requestURI = unwrappedRequest.getRequestURI();
      beginIndex = requestURI.indexOf(contextPath);
      endIndex = beginIndex + contextPath.length();
      requestURI = requestURI.substring(endIndex);
      fileNameDecoded = (root + requestURI).replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar).trim();
    }
    String fileNameCanonical = null;
    try {
      fileNameCanonical = new File(fileName).getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar);
    } catch (IOException e) {
    	//TODO:Polly type:ok content- more ifo: fileName
      String logId = LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000128",
    	  "Error creating file [{0}] on the file system for [{1}] application.", new Object[]{fileName, appName}, e, null, null);
      SupportabilityData supportabilityData = new SupportabilityData(true, LogContext.getExceptionStackTrace(e), logId);
      if (supportabilityData.getMessageId().equals("")) {
        supportabilityData.setMessageId("com.sap.ASJ.web.000128");
      }
      //TODO : Vily G : if there is no DC and CSN in the supportability data
      //and we are responsible for the problem then set our DC and CSN
      //otherwise leave them empty
      ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error,
        Responses.mess56, e.getClass().getName() + ": " + e.getMessage(), false, supportabilityData);
      errorData.setException(e);
      FilterUtils.unWrapResponse(response).sendError(errorData, null);
      return;
    }
    String fileNameCanonicalDecoded = ParseUtils.canonicalize(fileNameDecoded.replace(File.separatorChar, ParseUtils.separatorChar));
    if (!isFileUploadAllowed(fileNameCanonical, root, unwrappedRequest, response)) {
      return;
    }
    if (!writeLocalFile(fileNameCanonical, request.getInputStream(), response)) {
      return;
    }
    boolean isNew = false;
    try {
      if (isHttpAlias) {
        isNew = fileDeployer.uploadFileInAlias(fileNameCanonical,
            fileNameCanonicalDecoded.substring(fileNameCanonicalDecoded.indexOf(root) + root.length()),
            unwrappedRequest.getHttpParameters().getHostName(),
            unwrappedRequest.getHttpParameters().getRequestPathMappings().getAliasName().toString());
      } else {
        int i = root.lastIndexOf(rootDir);
        if (i == -1) {
          throw new WebServletException(WebServletException.ROOT_DIRECTORY_FOR_PUT_NOT_FOUND, new Object[]{rootDir});
        }
        i = i + rootDir.length() - 1;
        isNew = fileDeployer.uploadFileInApplication(fileNameCanonical, fileNameCanonicalDecoded.substring(i), appName);
      }
    } catch (Exception e) {
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      e.printStackTrace(new PrintStream(err));
      //TODO:Polly ok
      String logId = LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000209",
        "Cannot store [{0}] file in the database for [{1}] application.", new Object[]{fileNameCanonical, appName}, e, null, null);
      SupportabilityData supportabilityData = new SupportabilityData(true, LogContext.getExceptionStackTrace(e), logId);
      if (supportabilityData.getMessageId().equals("")) {
        supportabilityData.setMessageId("com.sap.ASJ.web.000209");
      }
      //TODO : Vily G : if there is no DC and CSN in the supportability data
      //and we are responsible for the problem then set our DC and CSN
      //otherwise leave them empty
      ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error,
        Responses.mess57, err.toString(), false, supportabilityData);
      errorData.setException(e);
      FilterUtils.unWrapResponse(response).sendError(errorData, null);
      return;
    }
    httpProvider.clearCacheByAlias(applicationContext.getAliasName());
    if (isNew) {
      generateResponse(unwrappedRequest.getRequestURIinternal(), ResponseCodes.code_created, response);
    } else {
      generateResponse(unwrappedRequest.getRequestURIinternal(), ResponseCodes.code_ok, response);
    }
  }

  private boolean isFileUploadAllowed(String fileNameCanonical, String root,
                                      HttpServletRequestFacadeWrapper unwrappedRequest, HttpServletResponse response) throws IOException {
    String canonicalURI = "";
    if (fileNameCanonical.length() > root.length()) {
      canonicalURI = fileNameCanonical.substring(root.length());
    }
    if (!fileNameCanonical.startsWith(root)) {
      FilterUtils.unWrapResponse(response).sendError(ResponseCodes.code_forbidden,
        Responses.mess20, Responses.mess21, true);//here we do not need user action
      return false;
    }
    if (canonicalURI.startsWith(web_inf.replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar))
        || canonicalURI.startsWith(meta_inf.replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar))) {
      FilterUtils.unWrapResponse(response).sendError(ResponseCodes.code_forbidden,
        Responses.mess20, Responses.mess21, true);//here we do not need user action
      return false;
    }
    if (unwrappedRequest.getRequestURIinternal().endsWith(".") && !fileNameCanonical.endsWith(".")) {
      //todo - a na linux kak e ?
      FilterUtils.unWrapResponse(response).sendError(ResponseCodes.code_forbidden,
        Responses.mess20, Responses.mess58, true);//here we do not need user action
      return false;
    }

    int dotIndex = fileNameCanonical.lastIndexOf(".");
    if (dotIndex > -1) {
      String ext = fileNameCanonical.substring(dotIndex).toLowerCase();
      if (denyList.contains(ext)) {
        FilterUtils.unWrapResponse(response).sendError(ResponseCodes.code_forbidden,
          Responses.mess20, Responses.mess59, true);//here we do not need user action
        return false;
      }
    }

    return true;
  }

  private boolean writeLocalFile(String fileName, InputStream in, HttpServletResponse response) throws IOException {
    try  {
      File tempFile = new File(fileName);
      if (tempFile.isDirectory()) {
    	  //TODO:polly ok
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000210",
          "Could not write the uploaded file [{0}] to the local disk for [{1}] application. Request URI denotes an existing directory " +
          "[{2}] instead of file.", new Object[]{fileName, appName, fileName}, appName, applicationContext.getCsnComponent());
        FilterUtils.unWrapResponse(response).sendError(ResponseCodes.code_internal_server_error,
          Responses.mess60, Responses.mess61, true);//here we do not need user action
      }
      if (tempFile.exists()) {
        if (!tempFile.delete()) {
        	//TODO:Polly ok
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000211",
            "The file [{0}]is not uploaded for [{1}] application. " +
            "An older version of the file exists and there is a problem deleting it.", new Object[]{fileName, appName}, null, null);
          FilterUtils.unWrapResponse(response).sendError(ResponseCodes.code_internal_server_error,
            Responses.mess20, Responses.mess62, true);//here we do not need user action
        }
      } else {
        tempFile.getParentFile().mkdirs();
      }
      
      FileUtils.writeToFile(in, tempFile);
    } catch (IOException e) {
    	//TODO:Polly ok
      String logId = LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000212",
        "Could not write the file [{0}] to the local disk. The file will not be updated in the [{1}] application configuration.", new Object[]{fileName, appName},
        e, null, null);
      SupportabilityData supportabilityData = new SupportabilityData(true, LogContext.getExceptionStackTrace(e), logId);
      if (supportabilityData.getMessageId().equals("")) {
        supportabilityData.setMessageId("com.sap.ASJ.http.000212");
      }
      //TODO : Vily G : if there is no DC and CSN in the supportability data
      //and we are responsible for the problem then set our DC and CSN
      //otherwise leave them empty
      if (!httpProvider.getHttpProperties().isDetailedErrorResponse()) { 
        Throwable logExId;
        if (logId == null) { //in case of logRecord.getId() return null
          logExId = new WebApplicationException(WebApplicationException.Log_ID_NULL, new Object[]{LogContext.LOCATION_REQUEST_INFO_SERVER_NAME});
        } else {
          WebContainerProvider webContainerProvider = ((WebContainerProvider)getServiceContext().getWebContainer().getIWebContainerProvider());
          if (webContainerProvider.getDeployedWebApplications().containsKey(Constants.LOG_VIEWER_ALIAS_NAME)) {
            logId = "<a href=" + Constants.LOG_VIEWER_ALIAS_NAME + "/LVApp?conn=filter[Log_ID:" + logId + "]view[Default%20Trace%20(Java)]>" + logId + "</a>";
          }
          logExId = new WebApplicationException(WebApplicationException.Log_ID_, new Object[]{logId});
        }
        ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error, 
          Responses.mess63, logExId.getLocalizedMessage(), false, supportabilityData);
        errorData.setException(e);
        FilterUtils.unWrapResponse(response).sendError(errorData, null);
      } else {
        ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error, 
          Responses.mess63, Responses.toHtmlView(getExceptionStackTrace(e)), false, supportabilityData);
        errorData.setException(e);
        FilterUtils.unWrapResponse(response).sendError(errorData, null); 
      }
      return false;
    }
    return true;
  }

  private void generateResponse(String fileName, int statusCode, HttpServletResponse response) throws IOException {
    response.setStatus(statusCode);
    response.addHeader(HeaderNames.entity_header_content_type, HeaderValues.text_html);
    response.addHeader(HeaderNames.entity_header_pragma, HeaderValues.no_cache);
    response.addHeader(HeaderNames.entity_header_cache_control, HeaderValues.no_cache);
    response.addIntHeader(HeaderNames.entity_header_expires, 0);
    if (!ServiceContext.getServiceContext().getWebContainerProperties().isSuppressXPoweredBy()){
      response.addHeader(HeaderNames.response_header_x_powered_by, HeaderValues.x_powered_by);
    }
    response.getOutputStream().write(response_body_beg);
    response.getOutputStream().write(fileName.getBytes());
    response.getOutputStream().write(response_body_end);
    response.getOutputStream().close();
  }
}

