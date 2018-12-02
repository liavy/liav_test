/*
 * Copyright (c) 2004-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.impl.module;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.RequestPathMappings;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.httpserver.server.SessionRequestImpl;
import com.sap.engine.services.httpserver.server.sessionsize.SessionRequestInfo;
import com.sap.engine.services.httpserver.server.sessionsize.SessionSizeManager;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.qos.RequestDispatcherConsumer;
import com.sap.engine.services.servlets_jsp.server.qos.WCERDResourceProvider;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacadeWrapper;
import com.sap.engine.services.servlets_jsp.webcontainer_api.request.IDispatchHandler;
import com.sap.engine.services.servlets_jsp.webcontainer_api.request.IRequestDispatcher;
import com.sap.engine.session.runtime.http.HttpSessionRequest;

/**
 * @author Violeta Georgieva
 * @version 7.0
 */
public class IRequestDispatcherImpl implements IRequestDispatcher {

  private ApplicationContext applicationContext = null;
  private ServletRequest request = null;
  private ServletResponse response = null;
  private String path = null;
  private String queryString = null;
  private String servletPath = null;
  private WCERDResourceProvider resourceProvider; 
  
  public IRequestDispatcherImpl(ApplicationContext applicationContext, ServletRequest request, ServletResponse response) {
    this.applicationContext = applicationContext;
    this.request = request;
    this.response = response;
    if (!ServiceContext.getServiceContext().getWebContainerProperties().isDisableQoSStatisticsForWCERD()) {
      resourceProvider = ServiceContext.getServiceContext().getWCERDResourceProvider();
    }
  }//end of constructor

  public IRequestDispatcherImpl(ApplicationContext applicationContext, ServletRequest request, ServletResponse response, String path) throws IllegalArgumentException {
    this.applicationContext = applicationContext;
    this.request = request;
    this.response = response;
    
    // Check validity of path
    if (path == null || !path.startsWith("/")){
      throw new IllegalArgumentException("Path is not valid. Value is [" + path + "]");
    }
    
    this.path = path;
    
    // Separate query string if there is such. 
    int questionIndex = path.indexOf("?");
    if (questionIndex > -1) {
      this.path = path.substring(0, questionIndex);
      this.queryString = path.substring(questionIndex + 1);
    } 
    
    // Calculate mappings.
    RequestPathMappings newRequestPathMappings = new RequestPathMappings();
    MessageBytes fileBytes = new MessageBytes(this.path.getBytes());
    applicationContext.getWebMappings().doMapCheck(fileBytes, newRequestPathMappings, true);
    
    servletPath  = newRequestPathMappings.getServletPath();
    
    if(servletPath == null) {
      throw new IllegalArgumentException("No servlet can be resolved based on path [" + path + "]");
    }
    if (!ServiceContext.getServiceContext().getWebContainerProperties().isDisableQoSStatisticsForWCERD()) {
      resourceProvider = ServiceContext.getServiceContext().getWCERDResourceProvider();
    }
  }//end of constructor
  
  
  /**
   * @param response2
   * @return
   */
  private void prepareResponse() {
    // TODO
    //FilterUtils.unWrapResponse(response).prepareResponseContext();
  }

  /**
   * @param request2
   * @return
   */
  private HttpSessionRequest prepareRequest() {
    // TODO prepare request for the switch
    return FilterUtils.unWrapRequest(request).prepareRequestContext();
  }

  public IRequestDispatcherImpl(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    if (!ServiceContext.getServiceContext().getWebContainerProperties().isDisableQoSStatisticsForWCERD()) {
      resourceProvider = ServiceContext.getServiceContext().getWCERDResourceProvider();
    }
  }//end of constructor

  /**
   * @see com.sap.engine.services.servlets_jsp.webcontainer_api.request.IRequestDispatcher#dispatch(com.sap.engine.services.servlets_jsp.webcontainer_api.request.IDispatchHandler)
   */
  public void dispatch(IDispatchHandler handler) {
    boolean accounting = ServiceContext.isAccountingEnabled();
    if (response != null && request != null) {      
      // If dispatch was created with path parameter.
      if (path != null) {
        HttpServletRequestFacadeWrapper unWrapRequest = FilterUtils.unWrapRequest(request);
        
        // Cache previous values of request properties in order to revert them
        // after the call.
        String prevServletPath = unWrapRequest.getServletPath();
        String prevPathInfo = unWrapRequest.getPathInfo();
        String prevRequestURI = unWrapRequest.getRequestURI();

        // Set new values to request's properties.
        String pathInfo = path.substring(path.indexOf(servletPath) + servletPath.length());
        String contextPath = unWrapRequest.getContextPath(); 
        String requestUri = contextPath + servletPath + pathInfo;
          
        unWrapRequest.setServletPath(servletPath);
        unWrapRequest.setPathInfo(pathInfo);
        unWrapRequest.setRequestURI(requestUri);
        if (queryString != null) {
          unWrapRequest.setParam(queryString, false);
        }
        
        // check if the resource is available
        boolean isConsumed = true;
        
        if (resourceProvider!=null && !ServiceContext.getServiceContext().getWebContainerProperties().isDisableQoSStatisticsForWCERD()) {          
          RequestDispatcherConsumer rdConsumer = new RequestDispatcherConsumer(RequestDispatcherConsumer.WCE_REQUEST_DISPATCHER_CONSUMER);
          try {        
            rdConsumer.setId(contextPath);
            if (!resourceProvider.consume(rdConsumer)) {
              LogContext.getLocation(LogContext.LOCATION_QUALITY_OF_SERVICE_INTEGRATION).traceInfo("Cannot dispatch to [" + contextPath + "] because it is overloaded", contextPath);
              isConsumed = false;            
              throw new IllegalStateException("com.sap.ASJ.web.000699 Cannot dispatch to [" + contextPath + "] because it is overloaded");
            } else {
              // Do actual action.
              try {//begin accounting
                if (accounting) {
                  Accounting.beginMeasure("Request in WCE; app [" + applicationContext.getAliasName() + "]", handler.getClass());
                }//begin accounting
                handler.service(request, response);
              } finally {//end accounting              
                if (accounting) {
                  Accounting.endMeasure("Request in WCE; app [" + applicationContext.getAliasName() + "]");
                }              
              }//end accounting
            }
          } finally {
            if (isConsumed) {
              resourceProvider.release(rdConsumer);
            }                   
          }
        } else {
          // Do actual action.
          try {//begin accounting
            if (accounting) {
              Accounting.beginMeasure("Request in WCE; app [" + applicationContext.getAliasName() + "]", handler.getClass());
            }//begin accounting
            handler.service(request, response);
          } finally {//end accounting              
            if (accounting) {
              Accounting.endMeasure("Request in WCE; app [" + applicationContext.getAliasName() + "]");
            }              
          }//end accounting        
        }
        
        
        
        // Revert request properties to previous values.
        FilterUtils.unWrapRequest(request).setServletPath(prevServletPath);
        FilterUtils.unWrapRequest(request).setPathInfo(prevPathInfo);
        FilterUtils.unWrapRequest(request).setRequestURI(prevRequestURI);
        
        if (queryString != null) {
          // Used after include/forward to restore original parameters of the
          // request.
          FilterUtils.unWrapRequest(request).removeParam();
        }
      } 
      // If dispatch was created without path parameter.
      else {
        ApplicationContext originalContext = FilterUtils.unWrapRequest(request)
          .getApplicationContext();
        ApplicationSession originalSession = null;
        ResourceContext resourceContext = null;
        HttpSessionRequest sRequest = null;
        Thread currentThread = Thread.currentThread();
        ClassLoader threadLoader = null;

        HttpSessionRequest newSessionRequest = prepareRequest();
        prepareResponse();
        HttpSessionRequest originalSessionRequest = null;
        try {
          if (originalContext.getAliasName().equals(
            applicationContext.getAliasName())) {
            originalContext = null;
          } else {
            FilterUtils.unWrapRequest(request).setContext(applicationContext);
            FilterUtils.unWrapResponse(response).setContext(applicationContext);
            originalSession = (ApplicationSession) FilterUtils.unWrapRequest(
              request).getHttpParameters().getApplicationSession();
            originalSessionRequest = FilterUtils.unWrapRequest(request)
              .getSessionRequest();
            String sessionId = originalSessionRequest.getSessionId();
            sRequest = new SessionRequestImpl();
            if (sessionId != null) {
              sRequest.doSessionRequest(applicationContext
                .getSessionServletContext().getSession(), originalSessionRequest.getClientCookie(),sessionId);
            }
            FilterUtils.unWrapRequest(request).setSessionRequest(sRequest);
            FilterUtils.unWrapRequest(request).getHttpParameters()
              .setApplicationSession(null);
            threadLoader = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(applicationContext.getClassLoader());
            resourceContext = applicationContext.enterResourceContext();
          }
          // check if the resource is available
          RequestDispatcherConsumer rdConsumer = null;
          boolean isConsumed = true;         
          
          try {
            if (resourceProvider!=null && !ServiceContext.getServiceContext().getWebContainerProperties().isDisableQoSStatisticsForWCERD()) {
              rdConsumer = new RequestDispatcherConsumer(RequestDispatcherConsumer.WCE_REQUEST_DISPATCHER_CONSUMER);
              rdConsumer.setId(applicationContext.getAliasName());
              if (!resourceProvider.consume(rdConsumer)) {
                LogContext.getLocation(LogContext.LOCATION_QUALITY_OF_SERVICE_INTEGRATION).traceInfo("Cannot dispatch to [" + applicationContext.getAliasName() + "] because it is overloaded", applicationContext.getAliasName());
                isConsumed = false;
                throw new IllegalStateException("com.sap.ASJ.web.000700 Cannot dispatch to [" + applicationContext.getAliasName() +"] because it is overloaded");
              } else {
                try {//begin accounting
                  if (accounting) {
                    Accounting.beginMeasure("Request in WCE; app [" + applicationContext.getAliasName() + "]", handler.getClass());
                  }//begin accounting
                  handler.service(request, response);
                } finally {//end accounting
                  if (accounting) {
                    Accounting.endMeasure("Request in WCE; app [" + applicationContext.getAliasName() + "]");
                  }
                }//end accounting
              }
            } else {
              try {//begin accounting
                if (accounting) {
                  Accounting.beginMeasure("Request in WCE; app [" + applicationContext.getAliasName() + "]", handler.getClass());
                }//begin accounting
                handler.service(request, response);
              } finally {//end accounting
                if (accounting) {
                  Accounting.endMeasure("Request in WCE; app [" + applicationContext.getAliasName() + "]");
                }
              }//end accounting
            }
          } finally {
            boolean flag = false;  
            try {
              if (originalContext != null && originalSessionRequest != null) {
                addSessionSize(applicationContext.getAliasName(), FilterUtils.unWrapRequest(request).getHttpParameters());
                FilterUtils.unWrapRequest(request).setContext(originalContext);
                FilterUtils.unWrapResponse(response).setContext(originalContext);
                
                // end current SessionRequest
                HttpSessionRequest session_request = FilterUtils.unWrapRequest(request).getSessionRequest_activateInvalidatorMonitor();
                flag = true;
                
                // restore original SessionRequest
                FilterUtils.unWrapRequest(request).setSessionRequest(originalSessionRequest);
                originalSessionRequest = null;
                
                FilterUtils.unWrapRequest(request).getHttpParameters().setApplicationSession(originalSession);
                
                session_request.endRequest(0);
                
                
                //FilterUtils.unWrapRequest(request).releaseInvalidatorMonitor();
                applicationContext.exitResourceContext(resourceContext);
                currentThread.setContextClassLoader(threadLoader);
              }
            } finally {
              if (originalSessionRequest != null) {
                sRequest.endRequest(0);
                // restore original SessionRequest
                FilterUtils.unWrapRequest(request).setSessionRequest(originalSessionRequest);
                originalSessionRequest = null;
              }
              if (flag){
            	  FilterUtils.unWrapRequest(request).releaseInvalidatorMonitor();
              }
            }
            if (resourceProvider!=null && rdConsumer!=null && isConsumed) {
              resourceProvider.release(rdConsumer);
            }
          }
          
        } finally {
          try {
            restoreRequest();
            newSessionRequest = null;
            restoreResponse();
          } finally {
            if (newSessionRequest != null) {
              newSessionRequest.endRequest(0);
              newSessionRequest = null;
            }
          }
        }
      }
    } else {
      Thread currentThread = Thread.currentThread();
      ClassLoader threadLoader = currentThread.getContextClassLoader();
      currentThread.setContextClassLoader(applicationContext.getClassLoader());
      ResourceContext resourceContext = applicationContext.enterResourceContext();
      
      // check if the resource is available
      boolean isConsumed = true;
      RequestDispatcherConsumer rdConsumer = null;
      
      
      try {
        if (resourceProvider!=null && !ServiceContext.getServiceContext().getWebContainerProperties().isDisableQoSStatisticsForWCERD()) {
          rdConsumer = new RequestDispatcherConsumer(RequestDispatcherConsumer.WCE_REQUEST_DISPATCHER_CONSUMER);
          rdConsumer.setId(applicationContext.getAliasName());
          if (!resourceProvider.consume(rdConsumer)) {
            LogContext.getLocation(LogContext.LOCATION_QUALITY_OF_SERVICE_INTEGRATION).traceInfo("Cannot dispatch to [" + applicationContext.getAliasName() + "] because it is overloaded", applicationContext.getAliasName());
            isConsumed = false;
            throw new IllegalStateException("com.sap.ASJ.web.000701 Cannot dispatch to [" + applicationContext.getAliasName() + "] because it is overloaded");
          } else {
            try {//begin accounting
              if (accounting) {
                Accounting.beginMeasure("Dispatch in WCE; app [" + applicationContext.getAliasName() + "]", handler.getClass());
              }//begin accounting
              handler.service();
            } finally {//end accounting            
              if (accounting) {
                Accounting.endMeasure("Dispatch in WCE; app [" + applicationContext.getAliasName() + "]");
              }            
            }//end accounting
          }
        } else {
          try {//begin accounting
            if (accounting) {
              Accounting.beginMeasure("Dispatch in WCE; app [" + applicationContext.getAliasName() + "]", handler.getClass());
            }//begin accounting
            handler.service();
          } finally {//end accounting            
            if (accounting) {
              Accounting.endMeasure("Dispatch in WCE; app [" + applicationContext.getAliasName() + "]");
            }            
          }//end accounting
        }
      } finally {
        applicationContext.exitResourceContext(resourceContext);
        currentThread.setContextClassLoader(threadLoader);
        if (resourceProvider!=null && rdConsumer!=null && isConsumed) {
          resourceProvider.release(rdConsumer);
        }
      }
    }
  } // end of dispatch(IDispatchHandler handler)

  /**
   * Collects the needed info from the session and the request to calculate the
   * session size of the application which is about to be left. Session size 
   * calculation is done in the <CODE>SessionSizeManager</CODE> only if the 
   * feature is enabled
   * 
   * @param clientId         the id of the request
   * @param httpParameters   the description of the request
   */
  private void addSessionSize(String aliasName, HttpParameters httpParameters) {
    if (!httpParameters.getRequest().isSessionSizeEnabled()) {
      // the feature for session calculation is disabled
      // skip the rest of the method
      return;
    }
    SessionRequestInfo info = new SessionRequestInfo();
    info.setRequestId(httpParameters.getRequest().getClientId());
    info.setAliasName(aliasName);
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
          Object value;
          try {
            for (String chunk:chunkNames) {
              value = applicationSession.getChunkData(chunk);
              if (value != null) {
                chunks.put(chunk, value);
              }
            }
          } catch (ConcurrentModificationException e) {
            // the exception could be thrown at String chunk:chunkNames             
            // the session attributes are modified concurrently; this is not supported scenarion => 
            // continue with currently corrected data; 
          }
        }    
        info.setChunks(chunks);
      } catch (ClassCastException e) {
        // the session cannot be casted to the ApplicationSession; this could never happen
        info.setSession(null);
      }
    }    
    SessionSizeManager.addObjectSize(info);
  }
  
  /**
   *
   */
  private void restoreResponse() {
    // TODO restore response
    //FilterUtils.unWrapResponse(response).restoreResponseContext();
  }

  /**
   *
   */
  private void restoreRequest() {
    // TODO restore request
    FilterUtils.unWrapRequest(request).restoreRequestContext();
  }

} //end of class
