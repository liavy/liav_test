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

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;

import com.sap.engine.lib.util.ConcurrentReadLRUSet;
import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.httpserver.lib.util.ByteArrayUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParser;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactory;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParserInitializationException;
import com.sap.engine.services.servlets_jsp.server.Invokable;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.application.WebComponents;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebUnavailableException;
import com.sap.engine.services.servlets_jsp.server.jsp.JSPProcessor;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacade;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacade;
import com.sap.engine.system.ThreadWrapper;

/**
 * The default servlet invoked when there is a request to some jsp page. It founds a jsp file, parse
 * and load class it if is not loaded and compiles it. Then invokes the service method of the jsp servlet instance.
 * loading and compiling is done once in life of the servlet. Following requests use previously loaded and initialized instance
 * until some exception occurs in servlet's service method.
 *
 */
public class JSPServlet extends Invokable {
  /**
   * Note!
   * All fields should be serializable. JLin tests prio 1!
   */
  transient private ApplicationContext applicationContext = null;
  transient private ServletContext context;
  transient private WebComponents webComponents = null;
  transient private JspParser jspParser = null;

  private String alias = null;
  /**
   * Caches the last 800 valid requests.
   */
  private ConcurrentReadLRUSet cannonicalPath = null;


  private boolean isRequestFromDispatcher = false;

  /**
   * The first method invoked on servlet instance. Lookup context in naming where data
   * about previously loaded jsps is stored.
   *
   * @param   con  a reference to ServletConfiq
   * @exception   ServletException
   */
  public void init(ServletConfig con) throws javax.servlet.ServletException {
    super.init(con);
    context = con.getServletContext();
    applicationContext = ((ServletContextImpl) context).getApplicationContext();
    this.webComponents = applicationContext.getWebComponents();
    this.alias = applicationContext.getAliasName();
    try {
      jspParser = JspParserFactory.getInstance().getParserInstance(JSPProcessor.PARSER_NAME);
    } catch (JspParserInitializationException e) {
      throw new ServletException("Cannot find JSP parser.", e);
    }
  }

  /**
   * Finds jsp, parses it if isn't parsed and invokes its service method. Must synchronize
   * compiling of the servlet generated for this jsp.
   *
   * @param   request  the request data sent from the client.
   * @param   response  the object used to write the answer of the request
   *         in html format.
   * @exception   ServletException thrown if the servlet is not able to manage the request.
   * @exception   IOException  thrown if the source file for the response is not found
   *         or unable to read.
   */
  public void service(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    String className = null;
    String jspFile = null;
    String requestedResource = null;
    HttpServletRequestFacade unwrappedRequest = FilterUtils.unWrapRequest(request);
    HttpServletResponseFacade unwrappedResponse = FilterUtils.unWrapResponse(response);
    String request_uri = (String) request.getAttribute("com.sap.engine.include.request_uri");
    request.removeAttribute("com.sap.engine.include.request_uri");
    //String isFromDispatcher = (String)request.getAttribute("com.sap.engine.include.request_dispatcher");
    request.removeAttribute("com.sap.engine.include.request_dispatcher");
    boolean fromRequestDispatcher = request_uri != null;
    isRequestFromDispatcher = request.getAttribute("com.sap.engine.include.request_dispatcher") != null;//this attribute is set internally and always with "true" value

    if (request_uri == null) {
      jspFile = unwrappedRequest.getRealPathLocal(request.getServletPath());
      requestedResource = request.getServletPath();
    } else {
      if (request.getContextPath().equals("/" + alias) || "/".equals(alias)) {
        jspFile = unwrappedRequest.getRealPathLocal(request_uri);
      } else {
        jspFile = context.getRealPath(request_uri).replace(File.separatorChar, ParseUtils.separatorChar);
      }
      requestedResource = request_uri;
    }
    if (isUnavailable(jspFile, requestedResource, unwrappedResponse)) {
      return;
    }
    //if productionMode=true and there is a generated class, skip all other checks
    if (jspParser.getContainerParameters().isProductionMode() && (className = (String) applicationContext.getClassNamesHashtable(jspParser.getParserName()).get(jspFile)) != null) {
      HttpJspPage jsp = null;
      if (request_uri != null) {
        jsp = webComponents.getJSP(className, request_uri);
      } else {
        jsp = webComponents.getJSP(className, request.getServletPath().substring(1));
      }
      if (jsp != null) {
        service(jsp, jspFile, request, response);
        return;
      }
    }

    File f = new File(jspFile);
    if (!fileExists(f, unwrappedRequest, unwrappedResponse)) {
      return;
    }
    if (inWebInf(requestedResource, unwrappedResponse, fromRequestDispatcher)) {
      return;
    }

    String jspCannonicalPath =  f.getCanonicalPath();

    unwrappedRequest.setJspFlag(true);
    String httpAliasName = unwrappedRequest.getHttpParameters().getRequestPathMappings().getAliasName().toString();
    String httpAliasValue = unwrappedRequest.getHttpParameters().getRequestPathMappings().getAliasValue();
    //check for JspPrecompile request
    if( unwrappedRequest.getJspPrecompile() != null ){
      boolean jspPrecompileTrue = isJspPrecompileRequest(jspFile, unwrappedRequest, unwrappedResponse);
      if( jspPrecompileTrue ){
	      try {
          // the request is not processed
	        // but the JSP should be processed if it's jspPrecompile request and the
	        // value is true
	        jspParser.generateJspClass(jspCannonicalPath, null, applicationContext.getAliasName(), httpAliasName, httpAliasValue, true);
	      } catch (JspParseException parseException) {
	        if( parseException.getCause() instanceof IOException){
	          throw (IOException)parseException.getCause();
	        }
	        if( parseException.getCause() instanceof ServletException){
	          throw (ServletException) parseException.getCause();
	        }
					throw new WebIOException(WebIOException.Error_while_parsing_jsp, new Object[] {jspFile}, parseException);
	      }
      }
      //JSP.11.4.2 Precompilation Protocol
      // "the request will not be delivered to the page."
      return;
    }
    if (!checkCannonicalPath(jspFile, jspCannonicalPath, unwrappedRequest, unwrappedResponse)) {
      return;
    }

    try {
      className =  jspParser.generateJspClass(jspCannonicalPath, null, applicationContext.getAliasName(), httpAliasName, httpAliasValue, false);
    } catch (JspParseException parseException) {
      if( parseException.getCause() instanceof IOException){
        throw (IOException)parseException.getCause();
      }
      if( parseException.getCause() instanceof ServletException){
        throw (ServletException) parseException.getCause();
      }
			throw new WebIOException(WebIOException.Error_while_parsing_jsp, new Object[] {jspFile}, parseException);
    };
    HttpJspPage jsp = null;
    if (request_uri != null) {
      jsp = webComponents.getJSP(className, request_uri);
    } else {
      jsp = webComponents.getJSP(className, request.getServletPath().substring(1));
    }
    //TODO: when jsp == null, is there such possibility?
    if (jsp == null) {
      try {
        className =  jspParser.generateJspClass(jspCannonicalPath, null, applicationContext.getAliasName(), httpAliasName, httpAliasValue, true);
      } catch (JspParseException parseException) {
        if( parseException.getCause() instanceof IOException){
          throw (IOException)parseException.getCause();
        }
        if( parseException.getCause() instanceof ServletException){
          throw (ServletException) parseException.getCause();
        }
  			throw new WebIOException(WebIOException.Error_while_parsing_jsp, new Object[] {jspFile}, parseException);
      };

      if (request_uri != null) {
        jsp = webComponents.getJSP(className, request_uri);
      } else {
        jsp = webComponents.getJSP(className, unwrappedRequest.getServletPath().substring(1));
      }
    }

    if (jsp == null) {
      unwrappedResponse.sendError(ResponseCodes.code_internal_server_error, Responses.mess44);//here we do not need user action
      return;
    }
    if (LogContext.getLocationRequestInfoServer().beInfo()) {
      LogContext.getLocationRequestInfoServer().infoT(
          "Invoking JSP <" + requestedResource + ">");
    }

    service(jsp, jspFile, request, response);
  }

  // ------------------------ PRIVATE ------------------------



  /**
   * If some exception occurs returns an error to the client.
   *
   * @param   err  ID of the error
   * @param   mess  message of the error
   * @param   unwrappedResponse  represents Http response for this request
   */
  private void sendError(ErrorData errorData, HttpServletResponseFacade unwrappedResponse) throws IOException {
    try {
      unwrappedResponse.sendError(errorData, null);
    } catch (IllegalStateException e) {
      unwrappedResponse.writeError(errorData);
    }
  }

  private boolean isUnavailable(String jspFile, String requestedResource, HttpServletResponseFacade unwrappedResponse) throws IOException {
    if (!webComponents.isUnavailableJsp(jspFile)) {
      return false;
    }
    // time when will expire unavailability status of the JSP page
    long unavailableTime = -1;
    if( webComponents.isUnavailableJsp(jspFile) ) {
      unavailableTime = webComponents.getUnavailableJspSeconds(jspFile);
    } else {
      return false;
    }
    // remaining unavailability time
    if (unavailableTime == -1L) { // value -1 means isPermanent = true
      // Check version for supporting the old behavior before 2.4: return 503
	  int responseCodePermanent;
	  if (applicationContext.getWebApplicationConfiguration().isJ2ee13OrLess()) {
		responseCodePermanent = ResponseCodes.code_service_unavailable;
	  } else {
		responseCodePermanent = ResponseCodes.code_not_found;
	  }
      sendError(new ErrorData(responseCodePermanent, Responses.mess50.replace("{RESOURCE}", requestedResource),
        Responses.mess64, false, new SupportabilityData()), unwrappedResponse);
      return true;
    } else { // isPermanent = false
      long uj =  (unavailableTime - System.currentTimeMillis()) / 1000L;
      if (uj > 0) {
        // still is unavailable
        unwrappedResponse.setHeader("Retry-After", "" + uj);
        sendError(new ErrorData(ResponseCodes.code_service_unavailable, Responses.mess65.replace("{RESOURCE}", requestedResource),
          Responses.mess66.replace("{RESOURCE}", requestedResource).replace("{SECONDS}", uj + ""),
          false, new SupportabilityData()), unwrappedResponse);
        return true;
      }
    }
    return false;
  }

  private boolean fileExists(File f, HttpServletRequestFacade unwrappedRequest, HttpServletResponseFacade response) throws IOException {
    if (f.exists()) {
      return true;
    }
    if (LogContext.getLocationRequestInfoServer().beInfo()) {
      LogContext.getLocationRequestInfoServer().infoT(
          "JSP file <" + f + "> not found.");
    }
    if (unwrappedRequest.getMethod().equals("PUT")) {
      response.sendError(ResponseCodes.code_forbidden, Responses.mess42);//here we do not need user action
    } else {
    	String message = getClientMessage(f);
      response.sendError(ResponseCodes.code_not_found,
        Responses.mess50.replace("{RESOURCE}", unwrappedRequest.getContextPath() + unwrappedRequest.getServletPath()),
        message, true);//here we do not need user action
    }
    return false;
  }

  private boolean inWebInf(String requestedResource, HttpServletResponseFacade response, boolean fromRequestDispatcher)
      throws IOException {
    byte[] requestedResourceBytes = requestedResource.getBytes();
    boolean isWebInf = ByteArrayUtils.startsWithIgnoreCase(requestedResourceBytes, 0, requestedResourceBytes.length, "/web-inf".getBytes());
    if (isWebInf
      || ByteArrayUtils.startsWithIgnoreCase(requestedResourceBytes, 0, requestedResourceBytes.length, "/meta-inf".getBytes())) {
      //checks if it is not exposed through RequestDispatcher;
      if (isWebInf && isRequestFromDispatcher &&
			ServiceContext.getServiceContext().getWebContainerProperties().getRequestDispatcherOverWebInf()) {
          return false;
      }
      if (fromRequestDispatcher) {
         if (LogContext.getLocationRequestInfoServer().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000566",
                "Including a file from [META-INF] or [WEB-INF] directories in include directive. The file is: [{0}].", new Object[]{requestedResource}, null, null);
         }
        return false;
      } else {
        response.sendError(ResponseCodes.code_not_found, Responses.mess43);//here we do not need user action
        return true;
      }
    }
    return false;
  }

  /**
   * If "JspPrecompile" parameter's value is different from "true"|"false" then error page is returned.
   * Otherwise returns the boolean equivalent of the value.
   * @param jspFile
   * @param unwrappedRequest
   * @param unwrappedResponse
   * @return - true if there is "JspPrecompile" request parameter and its value is true.
   * @throws IOException
   */
  private boolean isJspPrecompileRequest(String jspFile,
                                         HttpServletRequestFacade unwrappedRequest, HttpServletResponseFacade unwrappedResponse
                                         ) throws IOException  {
    String prStr = unwrappedRequest.getJspPrecompile();
    if (prStr == null) {
      // check for null already should be made
    }
    boolean isPrecompileRequest = false;
    if (prStr.equals("") || prStr.equals("true") || prStr.equals("\"true\"")) {
      if (LogContext.getLocationRequestInfoServer().beInfo()) {
        LogContext.getLocationRequestInfoServer().infoT(
            "A \"jsp_precompile\" Parameter with value \"true\" found in the request for the JSP file <" + jspFile
                                  + "> .\r\nThe JSP file will be recompiled in the following requests.\r\nThe request will not be delivered to the page.");
      }
      isPrecompileRequest = true;
    } else if (prStr.equals("false") || prStr.equals("\"false\"")) {
      if (LogContext.getLocationRequestInfoServer().beInfo()) {
        LogContext.getLocationRequestInfoServer().infoT(
            "A \"jsp_precompile\" parameter with value \"false\" found in the request for the JSP File <" + jspFile + "> .\r\n"
                     + "The JSP file will not be recompiled in the following requests.\r\nThe request will not be delivered to the page.");
      }

    } else {
      unwrappedResponse.sendError(ResponseCodes.code_internal_server_error,
        Responses.mess51.replace("{PARAM}", prStr), Responses.mess52, true);//here we do not need user action
    }
    return isPrecompileRequest;
  }

  /**
   * Compares the native and our custom, String-only canonicalization.
   * The only purpose of the check is for case-sensitivity of the requested JSP.
   * This check could be removed if we map requested, non-canonicalized path and Servlet object.
   * However currently we map canonicalized path and servlet object.
   * f.getCanonicalPath() has its own cache which can hold up to 200 objects
   * and expires in 30 seconds.
   * @param jspFile
   * @param f
   * @param unwrappedRequest
   * @param unwrappedResponse
   * @return
   * @throws IOException
   */
  private boolean checkCannonicalPath(String jspFile, String jspCannonicalPath,
                                      HttpServletRequestFacade unwrappedRequest, HttpServletResponseFacade unwrappedResponse) throws IOException {
    // start lazy instantiation
    if( cannonicalPath == null ) {
      synchronized (this) {
        if( cannonicalPath == null ) {
          cannonicalPath = new ConcurrentReadLRUSet(10,800);
        }
      }
    }//end lazy instantiation

    if (cannonicalPath.contains(jspFile)) {
      return true;
    }

    if (!ParseUtils.canonicalizeFS(jspFile).startsWith(jspCannonicalPath)) {
      if (LogContext.getLocationRequestInfoServer().beInfo()) {
        LogContext.getLocationRequestInfoServer().infoT(
            "The requested JSP file <" + jspFile + "> not found.\r\nWrong file name syntax.");
      }
      unwrappedResponse.sendError(ResponseCodes.code_not_found,
        Responses.mess48.replace("{URL}", unwrappedRequest.getRequestURI()), Responses.mess49, true);//here we do not need user action
    } else {
      cannonicalPath.add(jspFile);
      return true;
    }
    return false;
  }


  private void service(HttpJspPage jsp, String jspFile, HttpServletRequest request, HttpServletResponse response)
      throws javax.servlet.ServletException, IOException {
    ThreadWrapper.pushSubtask("Processing JSP '" + jsp.getServletConfig().getServletName() + "'; Application '" + applicationContext.getApplicationName() + "'", ThreadWrapper.TS_PROCESSING);
    try {
    	invoke(jsp, request, response, false, false, null);
    } catch (UnavailableException t) {
      //webComponents.setJspUnavailableException(jspFile, t);
      if (t.isPermanent()) {
        webComponents.setJspUnavailable(jspFile, -1);
        throw new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable, t);
      } else {
        webComponents.setJspUnavailable(jspFile, System.currentTimeMillis() + t.getUnavailableSeconds() * 1000);
        throw new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable,
            t.getUnavailableSeconds(), t);
      }

    } catch (ServletException t) {
      if (t.getRootCause() instanceof UnavailableException) {
        UnavailableException ex = (UnavailableException) t.getRootCause();
        //webComponents.setJspUnavailableException(jspFile, ex);
        if (ex.isPermanent()) {
          webComponents.setJspUnavailable(jspFile, -1);
          throw new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable, t);
        } else {
          webComponents.setJspUnavailable(jspFile, System.currentTimeMillis() + ex.getUnavailableSeconds() * 1000);
          throw new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable,
              ex.getUnavailableSeconds(), t);
        }
      }
      throw t;
    } finally {
      ThreadWrapper.popSubtask();
    }
  }

  /** Constructs the reduced message to display to the client.*/
  private String getClientMessage(File f) {
  	String message = Responses.mess53;
  	try {
  		String displayFile = f.toString().replace(File.separatorChar, ParseUtils.separatorChar);
  		displayFile = displayFile.substring(applicationContext.getWebApplicationRootDir().replace(File.separatorChar,ParseUtils.separatorChar).length());
  		message = message.replace("{FILE}", displayFile).replace("{ALIAS}", alias);
  	} catch (Exception e) {
  	  message = message.replace("{FILE}", f.getName()).replace("{ALIAS}", alias);
  	}
  	if (applicationContext != null && applicationContext.getApplicationName() != null) {
  	  message = message.replace("{APPLICATION}", applicationContext.getApplicationName());
  	} else {
  	  message = message.replace("{APPLICATION}", "");
  	}
  	return message;
  }
}
