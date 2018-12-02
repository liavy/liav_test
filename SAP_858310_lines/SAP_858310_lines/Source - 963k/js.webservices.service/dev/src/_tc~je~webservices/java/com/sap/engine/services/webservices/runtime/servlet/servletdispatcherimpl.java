/**
 * Copyright (c) 2002 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.runtime.servlet;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.interfaces.sca.logtrace.CallEntry;
import com.sap.engine.interfaces.sca.logtrace.ESBTracer;
import com.sap.engine.interfaces.webservices.runtime.ServletDispatcher;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.interfaces.webservices.server.WebServicesContainerManipulator;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.server.WebServicesContainer;
import com.sap.engine.services.webservices.tools.InstancesPool;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LogRecord;

/**
 *
 * @author Alexander Zubev
 *
 */
public class ServletDispatcherImpl implements ServletDispatcher {
  public static final String SAP_WS_RUNTIME_DISABLED = "sap-ws-runtime-disabled";
  public static final String SAP_WS_TEST = "sap-ws-test";

  private static final int MAX_TASKNAME_LENGTH = 64;
  
  private RuntimeProcessingEnvironment wsProcessor;

  Location loc = Location.getLocation(ServletDispatcherImpl.class);
  
  private InstancesPool httpTransports = new InstancesPool();
  private InstancesPool hashTables = new InstancesPool();

  public ServletDispatcherImpl() {

  }

  public ServletDispatcherImpl(RuntimeProcessingEnvironment runtimeProcessor) {
    init(runtimeProcessor);
  }

  public void init(RuntimeProcessingEnvironment runtimeProcessor) {
    setWsProcessor(runtimeProcessor);
  }

  public void doPost(Object req, Object res, Object servlet) throws Exception {
    doPostWOLogging(req, res, servlet);
  }
    
  /**
   * Handles the doHead http request used to perform ping type checks.
   */
  public void doHead(Object req, Object res, Object servlet) throws Exception {        
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    String requestedUri = request.getRequestURI();
    if (requestedUri.endsWith("/")) {
      requestedUri = requestedUri.substring(0, requestedUri.lastIndexOf("/"));
    }
    
    String endpointId = RuntimeProcessingEnvironment.decodeEncodeURI(requestedUri);
    loc.debugT("ServletDispatcherImpl.doHead(), uri: " + endpointId);

    // Check if application exists for this endpoint.
    // If one exists, but is offline, does not start it, since this is just a health check.
    StringBuilder endpointIdStr = new StringBuilder(endpointId);
    boolean[] onlineResult = applicationExists(endpointIdStr, false);
    endpointId = endpointIdStr.toString();
    if (!onlineResult[0]){
      // The application does not exists.
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Web Service Endpoint URI expected. Not found endpoint with URI " + endpointId + ".");
      return;                 
    }else if (!onlineResult[1]){
      // The application exists, but is offline. Return ok for the health check.
      response.setStatus(HttpServletResponse.SC_OK);
          return;
        }
    
    
    HTTPTransport transport = getHTTPTransport(endpointId, request, response);
    
    Map metaData = new HashMap();       
    
    wsProcessor.processPing(transport, metaData);
  }
  
  
  /** Wrap original doPost method with WS Logging and tracing specific code
   * @throws Exception
   */
//  private void doPostWithLogging(Object req, Object res, Object servlet) throws Exception {
//    ESBTracer callEntryController = WSLogTrace.getESBTracer();
//    try {
//      HttpServletRequest request = (HttpServletRequest) req;
//      
//      String taskName = "WS processing at " + request.getRequestURI();
//      if (taskName.length() > MAX_TASKNAME_LENGTH) {
//        taskName = taskName.substring(0, MAX_TASKNAME_LENGTH - 1);
//      }
//      ThreadWrapper.pushSubtask(taskName, ThreadWrapper.TS_PROCESSING);
//      CallEntry ce = callEntryController.attachNewCallEntry(taskName);
//            
//      ce.setDirection(CallEntry.Direction.INBOUND);
//      ce.setConnectivityType("WS");
//
//      ce.setHeader(WSLogTrace.REQUETED_URL_HEADER, request.getRequestURI());
//
//      // assume success, change on error
//      ce.setCallStatus(CallEntry.Status.SUCCESS);
//      if(loc.beDebug()){
//        LogRecord firstRec = loc.debugT("Start WS processing on Provider side");
//        ce.setStartTraceID(firstRec.getId().toString());
//      }
//      doPostWOLogging(req, res, servlet);
//    } finally {      
//      CallEntry ce = callEntryController.getAttachedCallEntry();
//      if (ce != null) {
//        if(loc.beDebug()){
//          LogRecord lastRec = loc.debugT("End WS processing on Provider side");
//          ce.setEndTraceID(lastRec.getId().toString());
//        }
//        callEntryController.releaseCallEntry();
//      }
//    }
//  }
  
  // the original doPost implementation before adding of ws logging and tracing
  private void doPostWOLogging(Object req, Object res, Object servlet) throws Exception {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    String requestedUri = request.getRequestURI();
    if (requestedUri.endsWith("/")) {
      requestedUri = requestedUri.substring(0, requestedUri.lastIndexOf("/"));
    }
    
    String endpointId = RuntimeProcessingEnvironment.decodeEncodeURI(requestedUri);
    loc.debugT("ServletDispatcherImpl.doPost(), uri: " + endpointId);

        // Check if application exists for this endpoint.
    // If one exists, but is offline, start it. LazyStart scenario.
    StringBuilder endpointIdStr = new StringBuilder(endpointId);
    boolean[] onlineResult = applicationExists(endpointIdStr, true);
    endpointId = endpointIdStr.toString();
    if (!onlineResult[0]){
          response.sendError(HttpServletResponse.SC_NOT_FOUND, "Web Service Endpoint URI expected. Not found endpoint with URI: " + endpointId + " nor with URI: " +  endpointId + ". ");
          return;
        }
    
    
//    if (!runtimeRegistry.contains(endpointId)) { //supposed HTTP POST
//      if (!runtimeRegistry.contains(endpointId)) {
//        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Web Service Endpoint URI expected. Not found endpoint with URI: " + requestedUri + " nor with URI: " +  endpointId + ". ");
//        return;
//      }
//    }
//
//    if (request.getHeader(SAP_WS_RUNTIME_DISABLED) != null) {
//      return;
//    } else if (request.getParameter(SAP_WS_TEST) != null) {
//      wsInfo.writeInfo(endpointId, request, response, (HttpServlet) servlet, this);
//      return;
//    }

    //wsLogTraceEndpointIdAvailable(endpointId);
    
    HTTPTransport  transport = getHTTPTransport(endpointId, request, response);
    Hashtable metaData = (Hashtable) hashTables.getInstance();
    if (metaData == null) {
      metaData = new Hashtable();
    }
    boolean isSynch = true;
    try {
      metaData.put(RuntimeProcessingEnvironment.CALLING_SERVLET_PROPERTY, servlet);
      if (request.getParameter("allocStat") != null) {
        metaData.put("allocStat", "true");
      }
      isSynch = wsProcessor.process(transport, metaData);
    } catch (Exception e) { //exceptions with have been occured during runtime only their message is shown
      Location.getLocation(WSLogging.RUNTIME_LOCATION).catching("doPost context: " + request.getRequestURI(), e);
      String eMessage = e.getLocalizedMessage();
      if (eMessage == null) {
        eMessage = "null";
      }
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, eMessage);
    } finally {
      if (isSynch) { //return to pull only incase of synch processing.
        transport.clear(); //cannot be null
        httpTransports.rollBackInstance(transport);
        metaData.clear();
        hashTables.rollBackInstance(metaData);
      }
    }
  }

  public void doGet(Object req, Object res, Object servlet) throws Exception {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;


    String requestedUri = request.getRequestURI();
    if (requestedUri.endsWith("/")) {
      requestedUri = requestedUri.substring(0, requestedUri.lastIndexOf("/"));
    }

    loc.debugT("ServletDispatcherImpl.doGet(), uri: " + requestedUri);

    if (request.getHeader(SAP_WS_RUNTIME_DISABLED) != null) {
      return;
    }


    try {
          String strUri = RuntimeProcessingEnvironment.decodeEncodeURI(requestedUri);
      StringBuilder uri = new StringBuilder(strUri);
      // Check if application exists for this endpoint.
      // If one exists, but is offline, start it. Lazy start scenario.      
      boolean[] onlineResult = applicationExists(uri, true);
      strUri = uri.toString();
      if (!onlineResult[0]){
          response.sendError(HttpServletResponse.SC_NOT_FOUND, "Web Service Endpoint URI expected. Not found endpoint with URI " + uri + ".");
          return;
        }
            
      boolean writeWSDL = wsProcessor.getWSDLVisualizer().writeWSDL(request, response);
      if (!writeWSDL) {
        HTTPTransport transport = getHTTPTransport(strUri, request, response);
        boolean isSynch = wsProcessor.process(transport);
        if (isSynch) {
          transport.clear(); //cannot be null
          httpTransports.rollBackInstance(transport);          
        }
      }
    } catch (Exception e) {
      Location.getLocation(WSLogging.RUNTIME_LOCATION).catching("doGet context: " + request.getRequestURI(), e);
      String eMessage = e.getLocalizedMessage();
      if (eMessage == null) {
        eMessage = "null";
      }
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, eMessage);
    }
    
  }

  /**
   * Sets the wsProcessor.
   * @param wsProcessor The wsProcessor to set
   */
  public void setWsProcessor(RuntimeProcessingEnvironment wsProcessor) {
    this.wsProcessor = wsProcessor;
  }

  public RuntimeProcessingEnvironment getWsProcessor() {
    return wsProcessor;
  }

  private boolean checkWsdlParams(String style, boolean isStyleRequired, String mode, boolean isModeRequired, StringBuffer excMsg) {
    if (excMsg == null) {
      excMsg = new StringBuffer();
    }

    if(style != null) {
      if (!style.equals("document") && !style.equals("rpc") && !style.equals("rpc_enc") && !style.equals("mime") && !style.equals("http")) {
        String endMsg = ". ";
        if(!isStyleRequired) {
          endMsg = " or none. ";
        }
        excMsg.append("Invalid value for parameter style. Accepted values are: document, rpc, rpc_enc, mime or http" + endMsg);
        return false;
      }
    } else {
      if(isStyleRequired) {
        if(excMsg != null) {
          excMsg.append("Parameter style is required. ");
        }
        return false;
      }
    }

    if(mode != null) {
      if (!mode.equals("sap_wsdl")) {
        String endMsg = ". ";
        if(!isModeRequired) {
          endMsg = " or none. ";
        }
        excMsg.append("Invalid value for parameter mode. Accepted value is: sap_wsdl" + endMsg);
        return false;
      }
    } else {
      if(isModeRequired) {
        if(excMsg != null) {
          excMsg.append("Parameter mode is required. ");
        }
        return false;
      }
    }

    return true;
  }
  
  private HTTPTransport getHTTPTransport(String endpointId, HttpServletRequest request, HttpServletResponse response) {
    HTTPTransport  transport = (HTTPTransport) httpTransports.getInstance();
    if (transport == null) {
      transport = new HTTPTransport(endpointId, request, response);
    } else {
      transport.setEntryPointID(endpointId);
      transport.setRequest(request);
      transport.setResponse(response);
    }
    return transport;
  }
  
  /** Collect information for ws logging and tracing
   * @param endpointId
   */
 /*private void wsLogTraceEndpointIdAvailable(String endpointId) {
    String appComp = WebServicesContainer.getServiceContext().getApplicationName(endpointId);
    CallEntryImpl currentCallEntry = CallEntryControllerImpl.INSTANCE.getCurrentCallEntry();
    currentCallEntry.setAppComp(appComp);
    String ifDefName = WebServicesContainer.getServiceContext().getInterfaceDefinitionForBindingData(endpointId).getName();
    currentCallEntry.setInterfaceName(ifDefName);
    currentCallEntry.setServerNode(WSLogTrace.getServerNode());
  }*/
  
  private boolean isEndpointURLActive(StringBuilder url) {
    String strUrl = url.toString();
    if(WebServicesContainer.getServiceContext().getBindingData(strUrl) != null) {
      return true;	
    } 
    String trimmedURL = strUrl.substring(0, strUrl.lastIndexOf("/")); // check for http get/post binding request
    boolean exists = WebServicesContainer.getServiceContext().getBindingData(trimmedURL) != null;
    if (exists){
      url.replace(0, url.length(), trimmedURL);
    }
    return exists;     
  }

  
  /**
   * 
   * @param url caller endpoint
   * @param start start the application if it is not started yet - lazy start case.
   * @return boolean[] - [0] elements shows if the application exists, [1] tells is it is found in the online or in the offline
   * cache.
   * @throws Exception
   */
  private boolean[] applicationExists(StringBuilder url, boolean start) throws Exception{
    boolean exists = false;
    boolean isAlreadyOnline = false;
    
    if (!isEndpointURLActive(url)) {
      
      WebServicesContainerManipulator wsContainerManipulator = WebServicesContainer.getWSContainerManipulator();
      
      String interfaceDefinitionId = wsContainerManipulator.getInterfaceDefinitionId(url.toString());      
      if (interfaceDefinitionId != null) {
        
        String applicationName = wsContainerManipulator.getApplicationOfInterfaceDefinition(interfaceDefinitionId, 2);        
        if (applicationName != null) {
          // The application exists in the offline cache.
          exists = true;
          if (start){
            wsContainerManipulator.startApplicationAndWait(applicationName);
          }
        }
      }      
    }else{
      // The application exists.
      exists = true;
      // It is found in the online cache.
      isAlreadyOnline = true;
    }
        
    return new boolean[]{exists, isAlreadyOnline};
  }
  
}
