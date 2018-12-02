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
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.security.auth.Subject;
import java.security.PrivilegedActionException;

import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.ServletNotFoundException;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebServletException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebUnavailableException;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.*;
import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.tc.logging.Location;

/**
 *
 */
public class InvokerServlet extends Invokable {
  private static final String INVOKE_BY_CLASSNAME_KEY = "InvokeByClassName";
  transient private static Location currentLocation = Location.getLocation(InvokerServlet.class);
  private Class SERVLET_INTERFACE = null;
  transient private ApplicationContext scf = null;
  private boolean invokeByClassName = false;

  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    scf = ((ServletContextImpl) servletConfig.getServletContext()).getApplicationContext();
    invokeByClassName = "true".equalsIgnoreCase(servletConfig.getInitParameter(INVOKE_BY_CLASSNAME_KEY));
    if (!invokeByClassName) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000206",
        "The servlet [{0}] will not invoke servlets requested by class name. "+
    	  "To switch this feature on set the value of initial parameter [{1}] to true in global-web.xml file.",
    	  new Object[]{getServletName(), invokeByClassName}, null, null);
    }
    try {
      SERVLET_INTERFACE = Class.forName("javax.servlet.Servlet");
    } catch (ClassNotFoundException e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000207",
          "Cannot load class javax.servlet.Servlet.", e, null, null);
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
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpServletResponseFacadeWrapper responseUnwrapped = FilterUtils.unWrapResponse(response);
    String pathInfo = (String) request.getAttribute("com.inqmy.include.path_info");
    request.removeAttribute("com.inqmy.include.path_info");
    if (pathInfo == null) {
      pathInfo = request.getPathInfo();

      if (pathInfo == null) {
        throw new ServletNotFoundException(ServletNotFoundException.Requested_resource_not_found, new Object[]{request.getRequestURI()});
      }

      int indx = pathInfo.indexOf(";");

      if (indx > 0) {
        pathInfo = pathInfo.substring(0, indx);
      }
    }

    int indx = pathInfo.indexOf('/', 1);
    String servletName = null;

    if (indx < 0) {
      if (pathInfo.charAt(0) == ParseUtils.separatorChar) {
        servletName = pathInfo.substring(1);
      } else {
        servletName = pathInfo;
      }
      pathInfo = null;
    } else {
      if (pathInfo.charAt(0) == ParseUtils.separatorChar) {
        servletName = pathInfo.substring(1, indx);
      } else {
        servletName = pathInfo.substring(0, indx);
      }
      pathInfo = pathInfo.substring(indx);
    }
    if (!FilterUtils.unWrapResponse(response).isIncluded()) {
      FilterUtils.unWrapRequest(request).setServletPath(request.getServletPath() + "/" + servletName);
      FilterUtils.unWrapRequest(request).setPathInfo(pathInfo);
    }

    //when has run-as tag in servlet descriptor must override identity
    Subject subject = scf.getSubject(servletName);
    Servlet srv = null;
    if (!scf.getWebComponents().containsServletByClass(servletName)) {
      try {
        srv = scf.getWebComponents().getServlet(servletName);
      } catch (WebUnavailableException e) {
        throw e;
      } catch (UnavailableException e) {
        throw new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable, e.getUnavailableSeconds(), new Object[]{servletName}, e);
      } catch (ServletException e) {
        throw e;
      } catch (IOException io) {
        if (!canLoadServlet(servletName, responseUnwrapped)) {
          return;
        }
      }
    }
    if (srv == null) {
      srv = scf.getWebComponents().getServletByClass(servletName);
    }

    if (LogContext.getLocationRequestInfoServer().beInfo()) {
      LogContext.getLocationRequestInfoServer().infoT("Invoking Servlet <" + servletName + ">, servlet class is <" + srv.getClass().getName() + ">.");
    }

    try {
      invoke(srv, request, response, subject, false);
    } catch (javax.servlet.UnavailableException e) {
      srv.destroy();
      scf.getWebComponents().addServletByClass(servletName);
      if (e.isPermanent()) {
        scf.getWebComponents().setServletUnavailable(servletName, -1L);
      } else {
        scf.getWebComponents().setServletUnavailable(servletName, (System.currentTimeMillis() +  e.getUnavailableSeconds() * 1000));
      }
      scf.getWebComponents().setServletUnavailableException(servletName, e);
      throw new WebUnavailableException(WebUnavailableException.Servlet_is_currently_unavailable, e.getUnavailableSeconds(), new Object[]{servletName}, e);
    } catch (PrivilegedActionException pe) {
      throw new WebServletException(WebServletException.Error_occured_while_servlet_is_started_with_run_as_identity,
                                                                                        new Object[]{srv.getServletConfig().getServletName()}, pe);
    }
  }

  private boolean canLoadServlet(String servletName, HttpServletResponseFacadeWrapper responseUnwrapped) throws IOException {
    if (!invokeByClassName) {
      responseUnwrapped.sendError(ResponseCodes.code_forbidden,
        Responses.mess46.replace("{SERVLETNAME}", servletName),
        Responses.mess47.replace("{SERVLETNAME}", getServletName()), true);//here we do not need user action
      return false;
    }
    Class servletClass = null;
    try {
      servletClass = Class.forName(servletName, true, scf.getClassLoader());
    } catch (ClassNotFoundException e) {
    	//Skip 'Loader Info' when displaying to the client:
    	SupportabilityData supportabilityData = new SupportabilityData(true, LogContext.getExceptionStackTrace(e), "");
      String message = getClientMessage(servletName, e);
      if (LogContext.getLocationServletResponse().beWarning()) {
    		String logId = LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000564",
    		  "Sending detailed error message [{0}] back to the client out of the original exception [{1}].",
    		  new Object[]{message, e.toString()},  null, null);
    		supportabilityData.setLogId(logId);
    	}
      if (supportabilityData.getMessageId().equals("")) {
        supportabilityData.setMessageId("com.sap.ASJ.web.000564");
      }
      //TODO : Vily G : if there is no DC and CSN in the supportability data
      //and we are responsible for the problem then set our DC and CSN
      //otherwise leave them empty
      responseUnwrapped.sendError(new ErrorData(ResponseCodes.code_not_found,
        Responses.mess45.replace("{SERVLETNAME}", servletName), Responses.mess45.replace("{SERVLETNAME}", servletName), false, supportabilityData), null);
      return false;
    }
    if (SERVLET_INTERFACE == null) {
      responseUnwrapped.sendError(ResponseCodes.code_forbidden, Responses.mess41);//here we do not need user action
      return false;
    }
    if (!SERVLET_INTERFACE.isAssignableFrom(servletClass)) {
      if (LogContext.getLocationServletResponse().beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000565",
            "A servlet with class [{0}] is not found. " +
            "The class [{1}] is not a valid servlet.", new Object[]{servletName, servletName},  null, null);
      }
      responseUnwrapped.sendError(ResponseCodes.code_forbidden,
        Responses.mess45.replace("{SERVLETNAME}", servletName), Responses.mess45.replace("{SERVLETNAME}", servletName), true);//here we do not need user action
      return false;
    }
    return true;
  }

	/** Constructs the reduced message to display to the client.	 */
	private String getClientMessage(String servletName, ClassNotFoundException e) {
		StringBuffer message = new StringBuffer("java.lang.ClassNotFoundException: " + e.getMessage() + ".");
		try {
			String realServletClass = (getServletName() != null && scf.getWebComponents().getServletClass(servletName) != null) ?
					scf.getWebComponents().getServletClass(servletName) : servletName;
			message.append("\r\nThe class [" + realServletClass + "] for servlet ["+ servletName + "] not found");
			message.append(" for the web application alias [" + scf.getAliasName() +
					"] of Java EE application [" + scf.getApplicationName() + "].");
		} catch (Exception ex) {
			//TODO:Polly ok
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000127",
        "Cannot construct error response message. The error response message is for servlet [{0}] in [{1}] web application. " +
        "Initial error is: {2}.", new Object[]{servletName, scf.getAliasName(), message.toString()}, ex, null, null);
		}
		return message.toString();
	}
}

