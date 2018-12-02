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
package com.sap.engine.services.servlets_jsp.server;

import static com.sap.engine.services.servlets_jsp.server.ServiceContext.getServiceContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.security.auth.Subject;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transaction;

import com.sap.bc.proj.jstartup.fca.FCAException;
import com.sap.engine.compilation.CompilerException;
import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.HttpHandler;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;
import com.sap.engine.services.httpserver.interfaces.properties.HttpCompressedProperties;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.httpserver.server.ServiceContext;
import com.sap.engine.services.httpserver.server.memory.impl.RequestMemoryReportManager;
import com.sap.engine.services.httpserver.server.sessionsize.SessionRequestInfo;
import com.sap.engine.services.httpserver.server.sessionsize.SessionSizeManager;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.exceptions.NewApplicationSessionException;
import com.sap.engine.services.servlets_jsp.server.exceptions.ServletNotFoundException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebApplicationException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebUnavailableException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.runtime.FilterChainImpl;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.runtime.client.RequestContextObject;
import com.sap.engine.services.servlets_jsp.server.runtime.client.RequestPreservationContext;
import com.sap.engine.services.servlets_jsp.server.runtime.client.preservation.ObservableHashSet;
import com.sap.engine.services.servlets_jsp.server.runtime.client.preservation.RequestPreservationManager;
import com.sap.engine.system.ThreadWrapper;
import com.sap.jvm.monitor.vm.VmDebug;
import com.sap.tc.logging.Location;
import com.sap.jvm.Capabilities;


public class HttpHandlerImpl extends Invokable implements HttpHandler {
  transient private static Location currentLocation = Location.getLocation(HttpHandlerImpl.class);
  private static final String SERVLET = "Servlet ";
  private static final String IN = " in ";
  private static final String REQ_ID_ATTR_NAME = "com.sap.http.request_id";
  private boolean isUnhandledErrorProcessing = false;
  transient private RequestPreservationManager preservationManager;

  public HttpHandlerImpl() {
    this.preservationManager = new RequestPreservationManager(getServiceContext());
  }

  /**
   * Checks whether a custom error handler exists and if so delegates the error in order to be processed.
   *
   * @param httpParameters http parameters
   * @return true if there is a custom error handler, false - otherwise.
   */
  public boolean handleError(HttpParameters httpParameters) {
    ApplicationContext applicationContext = getContext(httpParameters.getRequestPathMappings().getAliasName(), httpParameters);

    //Try to find error handler provided by application itself.
    int errorCode = httpParameters.getErrorData().getErrorCode();
    if (errorCode == -1) {
      errorCode = ResponseCodes.code_internal_server_error;
    }
  
    String errorPage = null;
    if (applicationContext != null) {
      if (httpParameters.getErrorData().isErrorByCode()) {
        errorPage = applicationContext.getErrorPage(errorCode);
      } else {
        errorPage = applicationContext.getErrorPage(httpParameters.getErrorData().getException());
      }
    }
    
    boolean global = false;
    if (errorPage == null) {
      //There is no error handler provided by application
      errorPage = getServiceContext().getWebContainerProperties().getErrorPageLocation();

      if (!errorPage.equals(Constants.ERROR_HANDLER_SERVLET)) {
        //If the global error handler is not provided by us then get the specified web module
        ApplicationContext targetContext = getContext(new MessageBytes(getServiceContext().getWebContainerProperties().getErrorPageContextRoot().getBytes()), httpParameters);
        if (targetContext == null) {
          if (LogContext.getLocationServletResponse().beWarning()) {
            LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000702",
              "Web Container cannot resolve the custom global error handler, because of this the default global error handler is used. The current configuration is: "
              + "[Global_app_config/error_page/location = {0}], [Global_app_config/error_page/context_root = {1}]. "
              + "Possible reason: The web application that is specified in the property is not deployed.",
              new Object[]{getServiceContext().getWebContainerProperties().getErrorPageLocation(), getServiceContext().getWebContainerProperties().getErrorPageContextRoot()}, null, null);
          }
          errorPage = Constants.ERROR_HANDLER_SERVLET;
        } else {
          applicationContext = targetContext;
        }
      }

      if (errorPage.equals(Constants.ERROR_HANDLER_SERVLET)) {
        //If the global error handler is provided by us
        if (applicationContext == null) { 
          if (!Constants.defaultAliasMB.equals(httpParameters.getRequestPathMappings().getAliasName())) {
            //If there is no applicationContext then use the applicationContext of the default application,
            //in order to return the error response
            //If there is no default application then return false and leave HTTP Provider service to return the error response
            applicationContext = getContext(Constants.defaultAliasMB, httpParameters);
            if (applicationContext == null) {
              //We cannot do anything leave the HTTP Provider to construct the error response
              return false;
            }
          } else {
            //If there is no default application then return false and leave HTTP Provider service to return the error response
            //We cannot do anything leave the HTTP Provider to construct the error response
            return false;
          }
        }

        if (!applicationContext.getWebMappings().isServletMappingExist(Constants.ERROR_HANDLER_SERVLET.substring(1), Constants.ERROR_HANDLER_SERVLET)){
          //This must not happen!
          return false;
        }
      }
      
      global = true;
    }

    long systemTimeOnStart = -1;
    if (getServiceContext().getWebMonitoring().isMonitoringStarted()) {
      systemTimeOnStart = System.currentTimeMillis();
      getServiceContext().getWebMonitoring().newRequest();
    }

    try {
      applicationContext.addRequest();
   // TODO - i024079 fix: check with web container developers
      //getServiceContext().getConnectionsContext().addWorkingConnection(httpParameters.getRequest().getClientId());
      runErrorPage(errorPage, httpParameters, applicationContext, global);
      //getServiceContext().getConnectionsContext().removeWorkingConnection(httpParameters.getRequest().getClientId());
      //getServiceContext().getConnectionsContext().removeClosedConnection(httpParameters.getRequest().getClientId());
      applicationContext.removeRequest();
    } finally {
      if (getServiceContext().getWebMonitoring().isMonitoringStarted()) {
        if (systemTimeOnStart != -1) {
          getServiceContext().getWebMonitoring().newResponse(System.currentTimeMillis() - systemTimeOnStart);
        }
      }
    }

    if (applicationContext.isDestroying() && applicationContext.getAllCurrentRequests() == 0) {
      synchronized (applicationContext.getSynchObject()) {
        applicationContext.getSynchObject().notifyAll();
      }
    }

    return true;
  }//end of handleError(HttpParameters httpParameters)

  /**
   * Starts a servlet with name servletName. Parses a jsp if the request is for jsp, load an instance of the main servlet
   * class if the class is haven't yet loaded, invoke init method of the servlet (if not invoked before in the life of
   * the servlet) and then invoke the main service method.
   *
   * @param   servletName  name of the servlet that will be loaded
   * @param   httpParameters  represents the original Http request for this servlet
   */
  public void handleRequest(String servletName, HttpParameters httpParameters) {
    long time = System.currentTimeMillis();

    long systemTimeOnStart = -1;
    if (getServiceContext().getWebMonitoring().isMonitoringStarted()) {
      systemTimeOnStart = System.currentTimeMillis();
      getServiceContext().getWebMonitoring().newRequest();
    }
    ApplicationContext applicationContext = null;
    try {
      applicationContext = getContext(httpParameters.getRequestPathMappings().getAliasName(), httpParameters);
      applicationContext.addRequest();
   // TODO - i024079 fix: check with web container developers
      //getServiceContext().getConnectionsContext().addWorkingConnection(httpParameters.getRequest().getClientId());
      runServlet(servletName, httpParameters, applicationContext);
      //getServiceContext().getConnectionsContext().removeWorkingConnection(httpParameters.getRequest().getClientId());
      //getServiceContext().getConnectionsContext().removeClosedConnection(httpParameters.getRequest().getClientId());
      applicationContext.removeRequest();
    } finally {
      addSessionSize(httpParameters);
      if (getServiceContext().getWebMonitoring().isMonitoringStarted()) {
        if (systemTimeOnStart != -1) {
          getServiceContext().getWebMonitoring().newResponse(System.currentTimeMillis() - systemTimeOnStart);
        }
      }
    }

    if (applicationContext.isDestroying() && applicationContext.getAllCurrentRequests() == 0) {
      synchronized (applicationContext.getSynchObject()) {
        applicationContext.getSynchObject().notifyAll();
      }
    }

    if (httpParameters.getTraceResponseTimeAbove() > -1) {
      time = System.currentTimeMillis() - time;
      httpParameters.getTimeStatisticsMap().put(
        (new StringBuilder()).append("/request/invokeWebContainer/handleRequest (").append(servletName).append(")").toString(), time);
      if (LogContext.getLocationRequestInfoServer().beDebug()) {
        LogContext.getLocationRequestInfoServer().debugT(
          (new StringBuilder()).append("Put new entry '/request/invokeWebContainer/handleRequest (").append(servletName)
            .append(")' with time [").append(time).append("].").toString());
      }
    }
  }

  private void clearListenerExceptions(ApplicationContext applicationContext){
    applicationContext.getWebEvents().clearListenerExceptionMessage();
    applicationContext.getWebEvents().clearEventListenerError();
  }

  /**
   * Collects the needed info from the session and the request to calculate the
   * session size of the application which is about to be left. Session size 
   * calculation is done in the <CODE>SessionSizeManager</CODE> only if the 
   * feature is enabled
   * 
   * @param httpParameters   the description of the request
   */
  private void addSessionSize(HttpParameters httpParameters) {
    if (!httpParameters.getRequest().isSessionSizeEnabled()) {
      // the feature for session calculation is disabled
      // skip the rest of the method
      return;
    }
    SessionRequestInfo info = new SessionRequestInfo();
    info.setRequestId(httpParameters.getRequest().getClientId());
    info.setAliasName(httpParameters.getRequestPathMappings().getAliasName().toString());
    Object obj = httpParameters.getApplicationSession();
    if (obj == null) {   
      info.setSession(null);
    } else {
      try {
        ApplicationSession applicationSession = (ApplicationSession)obj;     
        info.setSessionId(applicationSession.getId()); 
        info.setSessionValid(applicationSession.isValid());
        info.setSession(applicationSession);      
        Hashtable<String, Object> chunks = new Hashtable<String, Object>();
        Collection<String> chunkNames = applicationSession.getChunkNames();
        if (chunkNames != null && !chunkNames.isEmpty()) {
          for (String chunk:chunkNames) {
            chunks.put(chunk, applicationSession.getChunkData(chunk));
          }
        }
        info.setChunks(chunks); 
      } catch (ClassCastException e) {
        //the session cannot be casted to the ApplicationSession; this could never happen  
        info.setSession(null);
      }           
    }    
    SessionSizeManager.addObjectSize(info);
  }

  /**
   * Returns the MIME time corresponding to the file extension ext.
   *
   * @param   ext  file extension
   * @param   alias
   * @return     MIME type for this extension if found
   */
  public String checkMIME(char[] ext, MessageBytes alias) {
    ApplicationContext scf = getContext(alias, null);
    if (scf == null) {
      return null;
    }
    return ((ServletContextImpl)scf.getServletContext()).getMimeType(ext);
  }

  //provides the Http Service with access to the servlet_jsp service property SecuritySessionIdDomain 
  public String getSecurtiySessionIdDomain(){
	  if (com.sap.engine.services.servlets_jsp.server.ServiceContext.getServiceContext() != null){
		  return com.sap.engine.services.servlets_jsp.server.ServiceContext.getServiceContext().getWebContainerProperties().getSecuritySessionIdDomain();
	  }else return null;
  }
  
  public void connectionClosed(int client_id) {
	// TODO - i024079 fix: check with web container developers
	  
	  /*
	  if (getServiceContext().getConnectionsContext().containsWorkingConnection(client_id)) {
      getServiceContext().getConnectionsContext().addClosedConnection(client_id);
    } else if (getServiceContext().getConnectionsContext().containsSleepingConnection(client_id)) {
      notifyConnectionClosed(client_id);
    }
    */
  }

  public void removeSession(String cookie) {
    Enumeration en = getServiceContext().getDeployContext().getStartedWebApplications();
    while (en.hasMoreElements()) {
      ((ApplicationContext)en.nextElement()).getSessionServletContext().removeSession(cookie);
    }
  }

  // ------------------------ PRIVATE ------------------------

  /**
   * Load servlet and starts his service method.
   *
   * @param   servletName  Name of the servlet
   * @param   httpParameters   request of the HTTP
   * @param   scf          ServletContextFacade
   */
  private void runServlet(String servletName, HttpParameters httpParameters, ApplicationContext scf) {
    
    long time = 0;
    if (httpParameters.getTraceResponseTimeAbove() > -1) {
    	time = System.currentTimeMillis();
    }

    Thread currentThread = Thread.currentThread();
    ClassLoader threadLoader = null;
    Servlet srv = null;
    //HttpServletResponseFacade sres = null;
    HttpServletResponseFacadeWrapper sres = null;
    HttpServletRequestFacadeWrapper sreq = null;
    //HttpServletRequestFacade sreq = null;
    ResourceContext resourceContext = null;
    isUnhandledErrorProcessing = false;
    if (ThreadWrapper.isthreadMonitoringEnabled()) {
      ThreadWrapper.pushTask(SERVLET + servletName + IN + scf.getAliasName(), ThreadWrapper.TS_PROCESSING);
    }
    try {
      if (getServiceContext().getWebContainerProperties().isUseRequestObjectPools()) {
        sreq = getServiceContext().getPoolContext().getRequest();
        sres = getServiceContext().getPoolContext().getResponse();
      } else {
        sreq = new HttpServletRequestFacadeWrapper();
        sres = new HttpServletResponseFacadeWrapper();
      }
      //When invoking RequestListener.requestInitialized() do this in the correct resource environment.
      resourceContext = scf.enterResourceContext();
      sreq.init(scf, httpParameters, sres);
      sres.init(scf, sreq, httpParameters.getResponse().getHeaders());
      sres.setServletName(servletName);
      sreq.setServletName(servletName);
      //sreq.setCurrentServletName(servletName);
      threadLoader = currentThread.getContextClassLoader();
      currentThread.setContextClassLoader(scf.getClassLoader());
      sreq.setThreadClassLoader(getServiceContext().getServiceLoader());
      srv = scf.getWebComponents().getServlet(servletName);
      httpParameters.setDebugRequest(scf.initializeDebugInfo(sreq));
      if (sreq.isStatisticTraceEnabled()) {
          //set request id as request attribute - used in session size measurement feature 
          sreq.setAttribute(REQ_ID_ATTR_NAME, sreq.getID());
      }
      if (scf.getWebEvents().getUnhandledException() != null) {
        //The container may respond to all subsequent requests to the
        //Web application with an HTTP status code 500 to indicate an application error.
        scf.getWebEvents().setEventListenerError();
        isUnhandledErrorProcessing = true;
        processError(scf.getWebEvents().getUnhandledException(), servletName, srv, sreq, sres, scf);
      } else {
        //when has run-as tag in servlet descriptor must override identity
        Subject subject = scf.getSubject(servletName);
        HttpCompressedProperties compressedProperties = getServiceContext().getHttpProvider().getHttpProperties().getCompressedProperties();
        if (httpParameters.getRequest().isGzipEncoding()
            && (compressedProperties.isCompressedOthers()
                || compressedProperties.getAlwaysCompressedExtensions() != null
                    && compressedProperties.getAlwaysCompressedExtensions().length > 0
                || compressedProperties.getAlwaysCompressedMIMETypes() != null
                    && compressedProperties.getAlwaysCompressedMIMETypes().length > 0
                || getServiceContext().getWebContainerProperties().headerForCompression() != null
                    && getServiceContext().getWebContainerProperties().headerForCompression().length() > 0)) {
            String requestedUrl = sreq.getRequestURL().toString();
            if ((compressedProperties.getMaximumCompressedURLLength() == -1) ||
              (requestedUrl.length() <= compressedProperties.getMaximumCompressedURLLength())) {
                sres.setGZip();
            }
        }
        String[] filterNames = httpParameters.getRequestPathMappings().getFilterChain();
        //ako ima filterChain , to se griji za izpalnenieto na servleta
        if (filterNames != null) {
          FilterChainImpl filterChain = (FilterChainImpl)scf.instantiateFilterChain(filterNames);
          filterChain.setServlet(srv, subject);
          if (LogContext.getLocationRequestInfoServer().beInfo()) {
            LogContext.getLocationRequestInfoServer().infoT("Invoking filter chain <" + filterChain.getClass().getName() + ">.");
          }
          try {
            if (sreq.isStatisticTraceEnabled()) {
              //set	request id as response header
              RequestMemoryReportManager.getInstance().startIntermediateSection(sreq.getID(),
                  "[" + scf.getApplicationName() + "]/[" + servletName + "]");
              sres.setIntHeader(HeaderNames.propriatory_sap_request_id, sreq.getID());
            }
          } catch (Exception e) {
            if (LogContext.getLocationRequestInfoServer().beDebug()) {
              LogContext.getLocationRequestInfoServer().debugT(
                  (new StringBuilder()).append("Cannot start intermediate section [").
                  append(servletName).append("] for request ID[").append(sreq.getID()).append("].").toString());
            }
          }
          try {
            filterChain.doFilter(sreq, sres);
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable t) {
            throw t;
          } finally {
            try {
              if (sreq.isStatisticTraceEnabled()) {
                RequestMemoryReportManager.getInstance().stopIntermediateSection(sreq.getID());
              }
            } catch (Exception e) {
              if (LogContext.getLocationRequestInfoServer().beDebug()) {
                LogContext.getLocationRequestInfoServer().debugT(
                    (new StringBuilder()).append("Cannot stop intermediate section [").
                    append(servletName).append("] for request ID[").append(sreq.getID()).append("].").toString());
              }
            }
          }
          filterChain = null;
        } else {
          if (LogContext.getLocationRequestInfoServer().beInfo()) {
            LogContext.getLocationRequestInfoServer().infoT("Invoking a servlet <" + servletName + ">, servlet class is <" + srv.getClass().getName() + ">");
          }
          invoke(srv, sreq, sres, subject, true);
        }
      }  
    } catch (ThreadDeath tde) {
      throw tde;
    } catch (OutOfMemoryError o) {
      throw o;
    } catch (Throwable ex) {
      if (scf.getWebEvents().isEventListenerError()) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000214", "{0}",
          new Object[]{scf.getWebEvents().getListenerExceptionMessage()}, ex, scf.getApplicationName(), scf.getCsnComponent());
      }
      processError(ex, servletName, srv, sreq, sres, scf);
      //clearListenerExceptions(scf, httpParameters);
      clearListenerExceptions(scf);
    } finally {


      if (ThreadWrapper.isthreadMonitoringEnabled()) {
    	  ThreadWrapper.popTask();
      }

      // Here is the logic for preserving the request and response in the web container
      // Async View
      // each http worker thread operates with its own copy of the preservationSet
      ObservableHashSet<RequestPreservationContext> preservationSet = RequestContextObject.getPreservationSet();

      if (preservationSet != null && preservationSet.size() > 0){
    	  // we should preserve the request and response
    	  //each http worker thread operates with its own sreq,sres and preservationSet
    	  try {
    	    sres.finish();
    	  } catch (IOException e) {
    		  if (LogContext.getLocationRequestInfoClient().beDebug()) {
					LogContext.getLocationRequestInfoClient().debugT(
							"IOException occurred while finalizing request to servlet [" +
							servletName + "]. Probably the client is lost. The error is:\r\n" + LogContext.getExceptionStackTrace(e));
				}
    	  }
    	  sres.setModifiable(false);



    	  //if there are accounted threads the request and response state would be preserved
    	  //but not referred by the web container

    	  if (LogContext.getLocationRequestPreservation().beDebug()){
    		  String tid = Thread.currentThread().getId()+"";
    		  LogContext.getLocationRequestPreservation().debugT("@@@@@@ - preserve - "+tid+" - "+preservationSet);
    	  }

    	  long timeout = RequestContextObject.getTimeout();
    	  boolean dispatched = RequestContextObject.isDispatched();
    	  if (timeout != 0 || dispatched){
    		    sres.getHttpParameters().preserveWithoutFinalizer();
    		  	preservationManager.addRequest(sreq, sres, preservationSet, timeout, dispatched);
    	  }else{
    		  sres.getHttpParameters().preserveWithFinalizer();
    	  }



      } else {
// edit by i024079    	  
	      try {
	        try {
	          sres.finish();
	        } catch (Exception io) {
		        if (LogContext.getLocationRequestInfoClient().beDebug()) {
							LogContext.getLocationRequestInfoClient().debugT(
									"Exception occurred while finalizing request to servlet [" +
									servletName + "]. Probably the client is lost. The error is:\r\n" + LogContext.getExceptionStackTrace(io));
						}
	        }
	        try {
	          sres.resetInternal(false);
	        } catch (IOException io) {
		        if (LogContext.getLocationRequestInfoClient().beDebug()) {
					LogContext.getLocationRequestInfoClient().debugT(
							"IOException occurred while finalizing request to servlet [" +
							servletName + "]. Probably the client is lost. The error is:\r\n" + LogContext.getExceptionStackTrace(io));
				}
            }
	        sres.setModifiable(false); //do not modify it until returning to the pool
            sreq.reset();
            sres.setModifiable(true);
            if (getServiceContext().getWebContainerProperties().isUseRequestObjectPools()) {
              getServiceContext().getPoolContext().releaseResponse(sres);
            } else {
              sres = null;
            }
            if (getServiceContext().getWebContainerProperties().isUseRequestObjectPools()) {
              getServiceContext().getPoolContext().releaseRequest(sreq);
            } else {
              sreq = null;
            }
	        
	      } catch (OutOfMemoryError e) {
	        throw e;
	      } catch (ThreadDeath e) {
	        throw e;
	      } catch (Throwable e) {
	    	  //TODO:Polly type:ok ?
	        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000133",
	          "Cannot complete HTTP response and clear used resources. The requested servlet is [{0}].", new Object[]{servletName},
	          e, null, null);
	      } finally {
	    	if (Capabilities.hasRestrictedDebugging() && VmDebug.isThreadRestricted()) {
                  VmDebug.removeRestrictedDebugThread();
	    	} 
	      }
      }
// end edit      
      try {
        if (getServiceContext().getTransactionManager() != null) {
          Transaction trans = getServiceContext().getTransactionManager().getTransaction();

          if (trans != null) {
            trans.rollback();
          }
        }
        scf.exitResourceContext(resourceContext);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:Polly type:ok ?
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000134",
            "Cannot clear the resource context used in HTTP request or cannot rollback uncommitted transactions used in the request.",
            e, null, null);
      }
      currentThread.setContextClassLoader(threadLoader);
    }

    if (httpParameters.getTraceResponseTimeAbove() > -1) {
      time = System.currentTimeMillis() - time;
      httpParameters.getTimeStatisticsMap().put(
        (new StringBuilder()).append("/request/invokeWebContainer/handleRequest (").append(servletName).append(")/runServlet").toString(), time);
      if (LogContext.getLocationRequestInfoServer().beDebug()) {
        LogContext.getLocationRequestInfoServer().debugT(
          (new StringBuilder()).append("Put new entry '/request/invokeWebContainer/handleRequest (").append(servletName)
            .append(")/runServlet' with time [").append(time).append("].").toString());
      }
    }
  }

  private void runErrorPage(String errorPage, HttpParameters httpParameters, ApplicationContext applicationContext, boolean global) {
    Thread currentThread = Thread.currentThread();
    ClassLoader threadLoader = null;
    HttpServletResponseFacadeWrapper sres = null;
    HttpServletRequestFacadeWrapper sreq = null;
    ResourceContext resourceContext = null;
    try {
      if (getServiceContext().getWebContainerProperties().isUseRequestObjectPools()) {
        sreq = getServiceContext().getPoolContext().getRequest();
        sres = getServiceContext().getPoolContext().getResponse();
      } else {
        sreq = new HttpServletRequestFacadeWrapper();
        sres = new HttpServletResponseFacadeWrapper();
      }

      threadLoader = initServletThread(errorPage, httpParameters, sreq, sres, applicationContext, currentThread);
      resourceContext = applicationContext.enterResourceContext();
      sreq.getHttpParameters().setDebugRequest(applicationContext.initializeDebugInfo(sreq));
      if (!sreq.getHttpParameters().getErrorData().isErrorByCode() && !errorPage.equals(Constants.ERROR_HANDLER_SERVLET)) {
        sreq.getHttpParameters().getErrorData().setErrorCode(ResponseCodes.code_internal_server_error);
      }
      processErrorPage(errorPage, sreq, sres, applicationContext, global);
    } catch (ThreadDeath tde) {
      throw tde;
    } catch (OutOfMemoryError o) {
      throw o;
    } catch (Throwable ex) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000222",
        "Processing an HTTP request by the error page [{0}] finished with errors. " +
        "Probably the exception thrown by the requested servlet [{1}] cannot be processed.",
        new Object[]{errorPage, sreq.getRequestURIinternal()}, ex, null, null);
      processError(ex, errorPage, null, sreq, sres, applicationContext);
    } finally {
      finilizeServletThread(errorPage, sreq, sres, resourceContext, applicationContext, currentThread, threadLoader);
    }
  }//end of runErrorPage(String errorPage, HttpParameters httpParameters, ApplicationContext applicationContext)

  private ClassLoader initServletThread(String errorPage, HttpParameters httpParameters,
                                 HttpServletRequestFacadeWrapper sreq, HttpServletResponseFacadeWrapper sres,
                                 ApplicationContext applicationContext, Thread currentThread) {
    sreq.init(applicationContext, httpParameters, sres);
    sres.init(applicationContext, sreq, httpParameters.getResponse().getHeaders());
    sres.setServletName(errorPage);
    sreq.setServletName(errorPage);
    ClassLoader threadLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(applicationContext.getClassLoader());
    sreq.setThreadClassLoader(getServiceContext().getServiceLoader());

    HttpCompressedProperties compressedProperties = getServiceContext().getHttpProvider().getHttpProperties().getCompressedProperties();
    if (sreq.getHttpParameters().getRequest().isGzipEncoding()
        && (compressedProperties.isCompressedOthers()
            || compressedProperties.getAlwaysCompressedExtensions() != null
                && compressedProperties.getAlwaysCompressedExtensions().length > 0
            || compressedProperties.getAlwaysCompressedMIMETypes() != null
                && compressedProperties.getAlwaysCompressedMIMETypes().length > 0
            || getServiceContext().getWebContainerProperties().headerForCompression() != null
                && getServiceContext().getWebContainerProperties().headerForCompression().length() > 0)) {
      sres.setGZip();
    }

    return threadLoader;
  }

  private void finilizeServletThread(String errorPage, HttpServletRequestFacadeWrapper sreq, HttpServletResponseFacadeWrapper sres,
                                     ResourceContext resourceContext, ApplicationContext applicationContext, Thread currentThread, ClassLoader threadLoader) {
    try {
      try {
        sres.finish();
      } finally {
        try {
          sres.resetInternal(false);
          sres.setModifiable(false);
        } finally {
          try {
            sreq.reset();
          } finally {
            try {
              sres.setModifiable(true);
              if (getServiceContext().getWebContainerProperties().isUseRequestObjectPools()) {
                getServiceContext().getPoolContext().releaseResponse(sres);
              } else {
                sres = null;
              }
            } finally {
              if (getServiceContext().getWebContainerProperties().isUseRequestObjectPools()) {
                getServiceContext().getPoolContext().releaseRequest(sreq);
              } else {
                sreq = null;
              }
            }
          }
        }
      }
    } catch (IOException io) {
      if (LogContext.getLocationRequestInfoClient().beDebug()) {
				LogContext.getLocationRequestInfoClient().debugT(
						"IOException occurred while finalizing request to servlet [" +
						errorPage + "]. Probably the client is lost. The error is:\r\n" + LogContext.getExceptionStackTrace(io));
			}
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
    	//TODO:Polly type:ok ?
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000135",
          "Cannot complete HTTP response and clear used resources. The requested servlet is [{0}].", new Object[]{errorPage},
          e, null, null);
    }
    try {
      if (getServiceContext().getTransactionManager() != null) {
        Transaction trans = getServiceContext().getTransactionManager().getTransaction();
        if (trans != null) {
          trans.rollback();
        }
      }
      applicationContext.exitResourceContext(resourceContext);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
    	//TODO:Polly type:ok ?
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000136",
          "Cannot clear the resource context used in HTTP request or cannot rollback uncommitted transactions used in the request to [{0}] web application.", new Object[]{applicationContext.getAliasName()},
          e, null, null);
    }
    currentThread.setContextClassLoader(threadLoader);
  }

  private void processError(Throwable ex, String servletName, Servlet srv,
                            HttpServletRequestFacadeWrapper sreq, HttpServletResponseFacadeWrapper sres, ApplicationContext scf) {

    boolean externalExcIsNotWebUnavailableException = !(ex instanceof WebUnavailableException);
    boolean detailed_error_responses = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().isDetailedErrorResponse();

    if (sreq.getAttribute("javax.servlet.jsp.jspException") != null) {
      ex = (Throwable) sreq.getAttribute("javax.servlet.jsp.jspException");
    }

    //handle UnavailableException
    Integer unavailableExceptionStatusCodeToReturnInteger = null;
    if (ex instanceof UnavailableException) {
      UnavailableException unavailableException = (UnavailableException) ex;
      // Servlet API 2.4: SRV.2.3.3.2 Exceptions During Request Handling
	    // Check version for supporting the old behavior before 2.4: return 503
      if ( unavailableException.isPermanent() && ! scf.getWebApplicationConfiguration().isJ2ee13OrLess() ) {
        // If a permanent UnavailableException: Must be returned a SC_NOT_FOUND (404) response.
        unavailableExceptionStatusCodeToReturnInteger = new Integer(ResponseCodes.code_not_found);
      } else {
        // If temporary unavailability: Any requests refused by the container during this period must be returned
        // with a SC_SERVICE_UNAVAILABLE (503) response status along with a Retry-After header indicating when the
        // unavailability will terminate.
        unavailableExceptionStatusCodeToReturnInteger = new Integer(ResponseCodes.code_service_unavailable);
      }

      if (externalExcIsNotWebUnavailableException && !(ex instanceof WebUnavailableException)) {
        // first time unavailability - not wrapped UnavailableException
        if (srv != null) {
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000217",
            "The servlet [{0}] with class [{1}] will be unavailable for [{2}] seconds. The reason is [{3}].",
            new Object[]{servletName, srv.getClass().getName(), (unavailableException).getUnavailableSeconds(), unavailableException.getMessage()}, unavailableException, scf.getApplicationName(), scf.getCsnComponent());
          srv.destroy();
        } else {
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000218",
            "The servlet [{0}] will be unavailable for {1} seconds. The reason is [{2}].",
            new Object[]{servletName, (unavailableException).getUnavailableSeconds(), unavailableException.getMessage()}, unavailableException, scf.getApplicationName(), scf.getCsnComponent());
        }
        scf.getWebComponents().addServlet(servletName);
        if (!unavailableException.isPermanent()) {
          scf.getWebComponents().setServletUnavailable(servletName,
            (System.currentTimeMillis() + (unavailableException).getUnavailableSeconds() * 1000));
        } else {
          scf.getWebComponents().setServletUnavailable(servletName, -1);
        }
        scf.getWebComponents().setServletUnavailableException(servletName, ex);
      }
    } // if (ex instanceof UnavailableException)

    String logExceptionID;
    String messageID = "";
    String csnComponent = "";
    String dcName = "";
    if (ex instanceof FCAException) {
      // Most probably client connection is closed, therefore info trace is enough
      logExceptionID = LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceInfo(
        "Processing HTTP request to servlet [" + servletName + "] finished with error.", ex, scf.getAliasName());
    } else {
    	//TODO:Polly type:ok content - more info if possible ?
      logExceptionID = LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000137",
        "Cannot process an HTTP request to servlet [{0}] in [{1}] web application.",
        new Object[]{servletName, scf.getAliasName()}, ex, scf.getApplicationName(), scf.getCsnComponent());
      messageID = "com.sap.ASJ.web.000137";
      csnComponent = scf.getCsnComponent();
      dcName = scf.getApplicationName();
    }

    //construct the supportability data
    SupportabilityData supportabilityData = new SupportabilityData(true, LogContext.getExceptionStackTrace(ex), logExceptionID);
    if (supportabilityData.getMessageId().equals("")) {
      supportabilityData.setMessageId(messageID);
    }
    if (supportabilityData.getCsnComponent().equals("")) {
      supportabilityData.setCsnComponent(csnComponent);
    }
    if (supportabilityData.getDcName().equals("")) {
      supportabilityData.setDcName(dcName);
    }

    Throwable logid;
    if (logExceptionID == null) {  //in case of logRecord.getId() return null
      logid = new WebApplicationException(WebApplicationException.Log_ID_NULL, new Object[] {LogContext.LOCATION_REQUEST_INFO_SERVER_NAME});
    } else {
      WebContainerProvider webContainerProvider = ((WebContainerProvider)getServiceContext().getWebContainer().getIWebContainerProvider());
      if (webContainerProvider.getDeployedWebApplications().containsKey(Constants.LOG_VIEWER_ALIAS_NAME)) {
        logExceptionID = "<a href=" + Constants.LOG_VIEWER_ALIAS_NAME + "/LVApp?conn=filter[Log_ID:" + logExceptionID + "]view[Default%20Trace%20(Java)]>" + logExceptionID + "</a>";
      }
      logid = new WebApplicationException(WebApplicationException.Log_ID_,new Object[] {logExceptionID});
    }

    //Java Servlet Specification Version 2.3 Final Release 8/13/01 SRV.9.9.2 Error Pages
    Throwable ex_orig = ex;
    // 1. Check for exact match or for a parent exception.
    String locationOfErrorPage = null;
    if ((ex instanceof ServletException) && ((javax.servlet.ServletException) ex).getRootCause() != null) {
      ex = ((javax.servlet.ServletException) ex).getRootCause();
      locationOfErrorPage = scf.getErrorPage(ex_orig);
    } else if ((ex instanceof javax.servlet.jsp.JspException) && ((javax.servlet.jsp.JspException) ex).getRootCause() != null) {
      ex = ((javax.servlet.jsp.JspException) ex).getRootCause();
      locationOfErrorPage = scf.getErrorPage(ex_orig);
    } //if
    if (locationOfErrorPage == null) {
      locationOfErrorPage = scf.getErrorPage(ex);
    }
    if (locationOfErrorPage != null) {
      sreq.setAttribute("javax.servlet.error.exception_type", ex.getClass());
      sreq.setAttribute("javax.servlet.error.message", ex.getMessage());
      sreq.setAttribute("javax.servlet.error.exception", ex);
      sreq.setAttribute("javax.servlet.error.request_uri", sreq.getRequestURI());
      sreq.setAttribute("javax.servlet.error.servlet_name", servletName);
      int responseCode = -1;
      if (unavailableExceptionStatusCodeToReturnInteger != null) { // UnavailableException exists
				responseCode = unavailableExceptionStatusCodeToReturnInteger.intValue();
        sreq.setAttribute("javax.servlet.error.status_code", unavailableExceptionStatusCodeToReturnInteger);
      } else { // other throwable type
				responseCode = ResponseCodes.code_internal_server_error;
        sreq.setAttribute("javax.servlet.error.status_code", new Integer(ResponseCodes.code_internal_server_error));
      }
			if (!sres.isCommitted() && !scf.getWebApplicationConfiguration().isJ2ee13OrLess()) {
			  sres.setStatus(responseCode);
			}
			RequestDispatcher requestDispatcher = scf.getServletContext().getRequestDispatcher(locationOfErrorPage);
			try {
			  sres.clearBuffer();
			} catch (IOException io) {
			  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000219",
			    "Cannot clear response buffers before invoking the error page [{0}].", new Object[]{locationOfErrorPage}, io, null, null);
			}
	    
	    //Add URL for generating error report as an attribute
	    //Traces ISE 500 if the error code is 500
      ErrorData errorData = new ErrorData(responseCode, ex.getMessage(), "", true, supportabilityData);
      errorData.setException(ex_orig);
	    String url = sres.getURIForGeneratingErrorReport(errorData);
	    if (url != null) {
	      sreq.setAttribute(Constants.URI_FOR_GENERATING_ERROR_REPORT, url);
	    }

			try {
			  sreq.setErrorHandler(true);
			  if (sres.isCommitted()) {
			    requestDispatcher.include(sreq, sres);
			  } else {
			    requestDispatcher.forward(sreq, sres);
			    sreq.getHttpParameters().getResponse().setPersistentConnection(false);
			  }//if
			  return;
			} catch (OutOfMemoryError e) {
			  throw e;
			} catch (ThreadDeath e) {
			  throw e;
			} catch (Throwable e) {
			  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000220",
			    "Processing an HTTP request by the error page [{0}] finished with errors. Probably the exception [{1}] " +
          "thrown by the requested servlet [{2}] cannot be processed.",
          new Object[]{locationOfErrorPage, ex, sreq.getRequestURIinternal()}, e, null, null);
			}
    }

    /*4. If a servlet generates an error that is not handled by the error page mechanism
     as described above, the container must ensure the status code of the response is set
     to status code 500.*/

    //String staticErrorMessage = "Application error occurred during the request procession.";
    Throwable AppException = new WebApplicationException(WebApplicationException.Application_error);
    String errPageLocation = scf.getErrorPage(ResponseCodes.code_internal_server_error);

    if (errPageLocation != null && ex_orig != null) {
      sreq.setAttribute("javax.servlet.error.exception_type", ex.getClass());
      sreq.setAttribute("javax.servlet.error.message", ex.getMessage());
      sreq.setAttribute("javax.servlet.error.exception", ex);
      sreq.setAttribute("javax.servlet.error.request_uri", sreq.getRequestURI());
      sreq.setAttribute("javax.servlet.error.servlet_name", servletName);
    }

    //String exToString = Responses.toHtmlView(logid.getLocalizedMessage());
    String exToString = logid.getLocalizedMessage();

    /* DetailedErrorResponse property controls the logging of stack traces.
     * If there are Compiler or Parsing Exceptions those should be hidden together
     * with the stack traces. Here it is appropriate to log the real exception and to include the log id
     * with the error response.
     */
    if (!detailed_error_responses){
      String tmp_ = sreq.getHttpParameters().getRequest().getRequestLine().getUrlDecoded().toString();
      //narrows down the request line
   	  if (tmp_.indexOf("?")>0){
   	    tmp_ = tmp_.substring(0,tmp_.indexOf("?"));
   	  }
      Object[] params = new Object[] {tmp_,scf.getApplicationName()};
      if (ex_orig.getCause() instanceof JspParseException){
        ex = new WebApplicationException(WebApplicationException.Parse_error,params);
        ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error,
          ex.getLocalizedMessage(), exToString, true, supportabilityData);
        errorData.setException(ex_orig);
        sendError(errorData, sres);
        return;
      }else if (ex_orig.getCause() instanceof CompilerException){
        ex = new WebApplicationException(WebApplicationException.Compile_error,params);
        ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error,
          ex.getLocalizedMessage(), exToString, true, supportabilityData);
        errorData.setException(ex_orig);
        sendError(errorData, sres);
        return;
      }
    }

    if (ex instanceof UnavailableException) {
      StringWriter strWr = new StringWriter();
      long us = 0;
      us = ((UnavailableException) ex).getUnavailableSeconds();
      String time = null;
      if (((UnavailableException) ex).isPermanent()) {
        time = Responses.mess26;
      } else {
        if (us <= 0) {
          time = Responses.mess27;
        } else {
          time = Responses.mess28.replace("{SECONDS}", us + "");
        }
        sres.setHeader("Retry-After", "" + us);
      }

      //When the DetailedErrorResponse is false the detail message returned to the
      //client should contain only the logging id of the real exception.
      if (detailed_error_responses){
        ex.printStackTrace(new PrintWriter(strWr));
        exToString = Responses.toHtmlView(strWr.toString());
      }
      sendError(new ErrorData(unavailableExceptionStatusCodeToReturnInteger.intValue(), time, exToString, true, supportabilityData), sres);
    } else if ((ex_orig instanceof ServletException) || (ex_orig instanceof javax.servlet.jsp.JspException)) {
      StringWriter strWr = new StringWriter();
      if (ex.equals(ex_orig)) {
        if ((ex instanceof ServletException) && ((javax.servlet.ServletException) ex).getRootCause() != null) {
          ex = ((javax.servlet.ServletException) ex).getRootCause();
          PrintWriter ptr_out = new PrintWriter(strWr);
          ptr_out.println(Responses.mess29);
          ex_orig.printStackTrace(ptr_out);
          ptr_out.println(Responses.mess30);
          ex.printStackTrace(ptr_out);
        } else if ((ex instanceof javax.servlet.jsp.JspException) && ((javax.servlet.jsp.JspException) ex).getRootCause() != null) {
          ex = ((javax.servlet.jsp.JspException) ex).getRootCause();
          PrintWriter ptr_out = new PrintWriter(strWr);
          ptr_out.println(Responses.mess29);
          ex_orig.printStackTrace(ptr_out);
          ptr_out.println(Responses.mess30);
          ex.printStackTrace(ptr_out);
        } else {
          ex.printStackTrace(new PrintWriter(strWr));
        }
      } else {
        PrintWriter ptr_out = new PrintWriter(strWr);
        ptr_out.println(Responses.mess29);
        ex_orig.printStackTrace(ptr_out);
        ptr_out.println(Responses.mess30);
        ex.printStackTrace(ptr_out);
      }

      //When the DetailedErrorResponse is false the detail message returned to the
      //client should contain only the logging id of the real exception.
	    if (detailed_error_responses){
	      ex.printStackTrace(new PrintWriter(strWr));
	      exToString = Responses.toHtmlView(strWr.toString());
	    }
      ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error, AppException.getLocalizedMessage(), exToString, true, supportabilityData);
      errorData.setException(ex_orig);
	    sendError(errorData, sres);
    } else if (ex instanceof ServletNotFoundException) {
      sendError(new ErrorData(HttpServletResponse.SC_NOT_FOUND, Responses.mess31, ex.getMessage(), false, supportabilityData), sres);
    } else if (ex instanceof NewApplicationSessionException) {
      sendError(new ErrorData(HttpServletResponse.SC_SERVICE_UNAVAILABLE, Responses.mess31, ex.getMessage(), false, supportabilityData), sres);
    } else if ( ex instanceof WebIOException && ex.getCause() != null){
      String lineMessage = ex.getMessage();

      StringWriter strWr = new StringWriter();

      //When the DetailedErrorResponse is false the detail message returned to the
      //client should contain only the logging id of the real exception.
      if (detailed_error_responses){
        ex.getCause().printStackTrace(new PrintWriter(strWr));//remove WebIOException from the stack trace because information it brings is useless
        exToString = Responses.toHtmlView(strWr.toString());
      }
      //ApplicationSession applicationSession = (ApplicationSession)sreq.getSession();
      String errorMessage = scf.getWebEvents().getListenerExceptionMessage();
      if (isUnhandledErrorProcessing) {
        errorMessage = scf.getWebEvents().getUnhandledExceptionMessage();
      }
      if (errorMessage != null && errorMessage.length() > 0) {
        errorMessage = "<br><br>&nbsp;&nbsp;" + errorMessage + "<br>";
      } else {
        errorMessage = "";
      }
      if (errPageLocation != null) {
        sreq.setAttribute("javax.servlet.error.message", AppException.getLocalizedMessage() +"<br>"+lineMessage + errorMessage);
      }

      ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error, AppException.getLocalizedMessage() + "<br>&nbsp;&nbsp;" + lineMessage + errorMessage,
        exToString, true, supportabilityData);
      errorData.setException(ex_orig);
      sendError(errorData, sres);
    } else {
      StringWriter strWr = new StringWriter();

      //When the DetailedErrorResponse is false the detail message returned to the
      //client should contain only the logging id of the real exception.
      if (detailed_error_responses){
        ex.printStackTrace(new PrintWriter(strWr));
        exToString = Responses.toHtmlView(strWr.toString());
      }

      String errorMessage = scf.getWebEvents().getListenerExceptionMessage();
      if (isUnhandledErrorProcessing) {
        errorMessage = scf.getWebEvents().getUnhandledExceptionMessage();
      }
      if (errorMessage != null && errorMessage.length() > 0) {
        errorMessage = "<br><br>&nbsp;&nbsp;" + errorMessage + "<br>";
      } else {
        errorMessage = "";
      }
      if (errPageLocation != null) {
        sreq.setAttribute("javax.servlet.error.message", AppException.getLocalizedMessage()  + errorMessage);
      }

      ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error, AppException.getLocalizedMessage()  + errorMessage,
        exToString, true, supportabilityData);
      errorData.setException(ex_orig);
      sendError(errorData, sres);
    }
  }

  private void processErrorPage(String errorPage, HttpServletRequestFacadeWrapper sreq,
                                HttpServletResponseFacadeWrapper sres, ApplicationContext scf, boolean global) throws IOException, ServletException {
    ErrorData errorData = sreq.getHttpParameters().getErrorData();

    int errorCode = errorData.getErrorCode();
    
    if (!sres.isCommitted() && !scf.getWebApplicationConfiguration().isJ2ee13OrLess()) {
      sres.setStatus(errorCode);
    }

    Throwable ex = errorData.getException();

    sreq.setErrorHandler(true);
    sreq.setAttribute("javax.servlet.error.status_code", new Integer(errorCode));
    sreq.setAttribute("javax.servlet.error.request_uri", sreq.getRequestURI());
    if (errorData.isErrorByCode()) {
      sreq.setAttribute("javax.servlet.error.message", errorData.getMessage() + " " + errorData.getAdditionalMessage());
      if (global && ex != null) {
        sreq.setAttribute("javax.servlet.error.exception_type", ex.getClass());
        sreq.setAttribute("javax.servlet.error.exception", ex);        
      }
    } else {
      if (global) {
        sreq.setAttribute("javax.servlet.error.message", errorData.getMessage() + " " + errorData.getAdditionalMessage());
      } else {
        sreq.setAttribute("javax.servlet.error.message", errorData.getException().getMessage());
      }
      sreq.setAttribute("javax.servlet.error.exception_type", errorData.getException().getClass());
      sreq.setAttribute("javax.servlet.error.exception", errorData.getException());
    }

    //Add URL for generating error report as an attribute
    //Traces ISE 500 if the error code is 500
    String url = sres.getURIForGeneratingErrorReport(errorData);
    if (url != null) {
      sreq.setAttribute(Constants.URI_FOR_GENERATING_ERROR_REPORT, url);
    }
    
    RequestDispatcher requestDispatcher = scf.getServletContext().getRequestDispatcher(errorPage);

    try {
      sres.clearBuffer();
    } catch (IOException io) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000221",
        "Cannot clear response buffers before invoking the error page [{0}].",
        new Object[]{errorPage}, io, null, null);
    }
    
    if (errorData.isHtmlAllowed() && LogContext.getLocationServletResponse().beInfo()) {
      LogContext.getLocationServletResponse().infoT("Web Container will return an error response where HTML is allowed.");
    }

    if (sres.isCommitted()) {
      requestDispatcher.include(sreq, sres);
    } else {
      requestDispatcher.forward(sreq, sres);
      sreq.getHttpParameters().getResponse().setPersistentConnection(false);
    }
  }//end of processErrorPage(String errorPage, HttpServletRequestFacadeWrapper sreq, HttpServletResponseFacadeWrapper sres, ApplicationContext scf)

  /**
   * If some exception occurs returns an error to the client.
   *
   * @param   errorData the error data
   * @param   sres  represents HTTP response for this request
   */
  private void sendError(ErrorData errorData, HttpServletResponseFacadeWrapper sres) {
    try {
      try {
        sres.sendError(errorData, null);
      } catch (IllegalStateException e) {
        sres.writeError(errorData);
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000223",
        "Cannot send an HTTP error response [{0} {1} (details: {2})].",
        new Object[]{errorData.getErrorCode(), errorData.getMessage(), errorData.getAdditionalMessage()}, e, null, null);
    }
  }//end of sendError(ErrorData errorData, HttpServletResponseFacadeWrapper sres)

  // TODO: Remove this method, cause it is replaced with 
  // ApplicationSelector.getContext(...) method and is never used?????
  /**
   * Returns a context created for the given war file.
   *
   * @param aliasName
   * @param httpParameters
   * @return
   */
  private ApplicationContext getContext(MessageBytes aliasName, HttpParameters httpParameters) {
    if (aliasName != null) {
      //checks whether there is application corresponding to this alias
      if (httpParameters != null && getServiceContext().getHttpProvider().getHostProperties(httpParameters.getHostName()).isApplicationAlias(aliasName.toString())) {
        //check the application
        //if it is started returns application context
        //if it has lazy startup mode then it will start it and after it will return the application context
        return getServiceContext().getDeployContext().startLazyApplication(aliasName);
      }
    }
    return getServiceContext().getDeployContext().getStartedWebApplicationContext(Constants.defaultAliasMB);
  }

//TODO - i024079 fix: check with web container developers
/*  
  private void notifyConnectionClosed(int client_id) {
    HttpServletResponseFacadeWrapper response = getServiceContext().getConnectionsContext().removeSleepingConnection(client_id);
    response.getServletContext().getWebEvents().connectionClosed(response.getServletRequest(), response);
  }
*/

  public String getApplicationAlias(HttpParameters httpParameters) {
    String alias = httpParameters.getRequestPathMappings().getAliasName().toString();
    if (getServiceContext().getHttpProvider().getHostProperties(httpParameters.getHostName()).isApplicationAlias(alias)) {
      return alias;
    } else {
      return "/";
    }
  }

  public void endRequest(HttpParameters httpParameters) {
    if (httpParameters.isDebugRequest() && getServiceContext().getHttpSessionDebugListener() != null
        && getServiceContext().getDebugRequestParameterName() != null
         && getServiceContext().getDebugRequestParameterName().length() != 0) {
      ApplicationSession session = (ApplicationSession)httpParameters.getApplicationSession();
      if (session != null && session.getDebugParameterValue() != null) {
        getServiceContext().getHttpSessionDebugListener().endRequest(session.getIdInternal());
      } else {
        getServiceContext().getHttpSessionDebugListener().endRequest(null);
      }
    }
  }


}
