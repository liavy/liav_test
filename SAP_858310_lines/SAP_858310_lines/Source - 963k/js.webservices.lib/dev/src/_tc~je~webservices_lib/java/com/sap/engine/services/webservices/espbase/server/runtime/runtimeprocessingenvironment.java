/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.server.runtime;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.interfaces.sca.logtrace.CallEntry;
import com.sap.engine.interfaces.sca.logtrace.ESBTracer;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Hibernation;
import com.sap.engine.interfaces.webservices.esp.HibernationEnvironmentException;
import com.sap.engine.interfaces.webservices.esp.ImplementationContainer;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.Protocol;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.esp.RuntimeEnvironment;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolExceptionExt;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.MessageIDProtocol;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.lib.logging.LoggingHelper;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriterFactory;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.transport.local.LocalHttpServletRequest;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.ServiceMeteringConstants;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.configuration.cfg.SoapApplicationRegistry;
import com.sap.engine.services.webservices.espbase.configuration.exceptions.ConfigurationException;
import com.sap.engine.services.webservices.espbase.mappings.EJBImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.ContainerEnvironment;
import com.sap.engine.services.webservices.espbase.server.ContainerEnvironmentHolder;
import com.sap.engine.services.webservices.espbase.server.ImplementationContainerAccessor;
import com.sap.engine.services.webservices.espbase.server.MetaDataAccessor;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.StaticConfigurationContext;
import com.sap.engine.services.webservices.espbase.server.TransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.ESPXITransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.HTTPStatefulProtocol;
import com.sap.engine.services.webservices.espbase.server.additions.HTTPTransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.MIMEHTTPTransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.MessageIDProtocolImpl;
import com.sap.engine.services.webservices.espbase.server.additions.ProviderJAXRPCHandlersProtocol;
import com.sap.engine.services.webservices.espbase.server.additions.SOAPHTTPTransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.attach.ProviderAttachmentProtocol;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.ProviderAddressingProtocol;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.impl.ProviderAddressingProtocolImpl;
import com.sap.engine.services.webservices.espbase.server.logging.ProviderLogger;
import com.sap.engine.services.webservices.espbase.server.logging.ProviderLoggingProtocol;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.ServiceMeter;
import com.sap.engine.services.webservices.espbase.xi.impl.ESPXITransport;
import com.sap.engine.services.webservices.jaxrpc.handlers.EJBEndpointHandlersInterceptor;
import com.sap.engine.services.webservices.jaxws.handlers.ProviderJAXWSHandlersProtocol;
import com.sap.engine.services.webservices.tools.ExceptionManager;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LogRecord;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;
import com.sap.tools.memory.trace.AllocationStatisticRecord;
import com.sap.tools.memory.trace.AllocationStatisticRegistry;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 *
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-20
 */
public final class RuntimeProcessingEnvironment implements RuntimeEnvironment, EJBEndpointHandlersInterceptor,
    com.sap.engine.interfaces.webservices.esp.EJBEndpointHandlersInterceptor {
  private static final String SECURITY_PROTOCOL = "SecurityProtocol";

  //Name for backwards compatibility
  public static final String JNDI_NAME_OLD  =  "RuntimeProcessor";

  //Property, under which the servlet instance which calls the runtime is bound
  public static final String CALLING_SERVLET_PROPERTY  =  "calling-servlet";
  
  //Name of persistable subcontext used by Runtime
  public static final String PERSISTENT_RUNTIME_SUBCONTEXT  =  "runtime-environment_context";

  //this marks log when an exception is processed to be sent back
  private static final String EXCEPTION_PROCESSING_LOG  =  "exception_processing_log";

  private static final String PROVIDER_PASSPORT_PROTOCOL = "ProviderPassportProtocol";

  //constant denoting default static protocol order
  private static final String[] DEFAULT_PROTOCOL_ORDER  =  new String[]{PROVIDER_PASSPORT_PROTOCOL,
      ProviderAddressingProtocol.PROTOCOL_NAME, ProviderAttachmentProtocol.PROTOCOL_NAME, SECURITY_PROTOCOL,
      ProviderLoggingProtocol.NAME, HTTPStatefulProtocol.PROTOCOL_NAME, MessageIDProtocol.MESSAGEID_PROTOCOLNAME };
  
  private static final String[] EXTENDED_PROTOCOL_ORDER = new String[] { PROVIDER_PASSPORT_PROTOCOL, "RMCallback",
      ProviderAddressingProtocol.PROTOCOL_NAME, ProviderAttachmentProtocol.PROTOCOL_NAME, SECURITY_PROTOCOL,  ProviderLoggingProtocol.NAME,
      HTTPStatefulProtocol.PROTOCOL_NAME, MessageIDProtocol.MESSAGEID_PROTOCOLNAME, "WS-RM" };
  
  private static final String[] JAXWS_HANDLERS_PROTOCOL_ORDER  =  new String[]{PROVIDER_PASSPORT_PROTOCOL,
      ProviderAttachmentProtocol.PROTOCOL_NAME, ProviderJAXWSHandlersProtocol.PROTOCOL_NAME };

  private static final String[] XI_PROTOCOL_ORDER = new String[]{ProviderAttachmentProtocol.PROTOCOL_NAME};
  
  private static final Location LOC = Location.getLocation(RuntimeProcessingEnvironment.class);
  
  private static final Hashtable<String, String[]> PROTOCOLORDER_REGISTRY = new Hashtable(); // key
                                                                                              // String,
                                                                                              // value
                                                                                              // String[]

  static {
    PROTOCOLORDER_REGISTRY.put(BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_DEFAULT_VALUE,
        DEFAULT_PROTOCOL_ORDER);
    PROTOCOLORDER_REGISTRY.put(BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_EXTENDED_VALUE,
        EXTENDED_PROTOCOL_ORDER);
    PROTOCOLORDER_REGISTRY.put(BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_JAXWS_HANDLERS_VALUE,
        JAXWS_HANDLERS_PROTOCOL_ORDER);
    PROTOCOLORDER_REGISTRY.put(BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_XI_VALUE, XI_PROTOCOL_ORDER);
    PROTOCOLORDER_REGISTRY.put(BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_JAXWS_DEFAULT_VALUE, DEFAULT_PROTOCOL_ORDER);
  }
  
  ImplementationContainerAccessor implContainerAccessor;

  ApplicationServiceContext applicationSrvContext;

  WSDLVisualizer wsdlVisualizer;

  private ApplicationWebServiceContextImpl applicationWSContext;

  private MetaDataAccessor metaDataAccessor;

  private Map transportBindings = new HashMap(); // key[string] -
                                                  // transportBindingID, value -
                                                  // TransportBinding instance

  private Map protocols = new Hashtable(); // key[string] - protocol id, value
                                            // - ProviderProtocol instance

  private ServiceMeter serviceMeter;

  private Set<TransportBinding> meteredTrBindings = new HashSet<TransportBinding>();

  // protected final static Category category =
  // Category.getCategory(Category.SYS_SERVER, "WS/Provider");

  /**
   * Returns Map object with keys - protocol ids (String objects), and values
   * ProviderProtocol instances.
   */
  public Map getRegisteredProtocols() {
    return Collections.unmodifiableMap(protocols);
  }
  
  public RuntimeProcessingEnvironment(ApplicationServiceContext appSrvCtx,
      ApplicationWebServiceContextImpl appWSContext, ImplementationContainerAccessor implContainerAccessor,
      MetaDataAccessor metaDataAccessor, ServiceMeter meter) throws Exception {
    this.serviceMeter = meter;
    this.applicationSrvContext = appSrvCtx;
    this.implContainerAccessor = implContainerAccessor;
    this.applicationWSContext = appWSContext;
    this.metaDataAccessor = metaDataAccessor;
    wsdlVisualizer = new WSDLVisualizer(this.metaDataAccessor, this.applicationSrvContext);
    //ProviderContextHelperImpl static initialization
    ProviderContextHelperImpl.environment = this;
    //register transport binding impls
    SOAPHTTPTransportBinding soapHttpTB = new SOAPHTTPTransportBinding();
    ESPXITransportBinding xiTB = new ESPXITransportBinding();
    transportBindings.put(InterfaceMapping.SOAPBINDING, soapHttpTB);
    meteredTrBindings.add(soapHttpTB);
    meteredTrBindings.add(xiTB);
    
    HTTPTransportBinding httpTB = new HTTPTransportBinding();
    transportBindings.put(InterfaceMapping.HTTPGETBINDING, httpTB);
    transportBindings.put(InterfaceMapping.HTTPPOSTBINDING, httpTB);
    transportBindings.put(InterfaceMapping.MIMEBINDING, new MIMEHTTPTransportBinding());
    transportBindings.put(ESPXITransportBinding.ID, xiTB);
    //register protocol impls
    protocols.put(HTTPStatefulProtocol.PROTOCOL_NAME, new HTTPStatefulProtocol());        
    protocols.put(MessageIDProtocol.MESSAGEID_PROTOCOLNAME, new MessageIDProtocolImpl());
    protocols.put(ProviderJAXRPCHandlersProtocol.PROTOCOL_NAME, new ProviderJAXRPCHandlersProtocol(appSrvCtx
        .getCoreContext().getLoadContext()));
    protocols.put(ProviderJAXWSHandlersProtocol.PROTOCOL_NAME, new ProviderJAXWSHandlersProtocol(appSrvCtx
        .getCoreContext().getLoadContext()));
    protocols.put(ProviderAddressingProtocol.PROTOCOL_NAME, ProviderAddressingProtocolImpl.SINGLETON);
    protocols.put(ProviderAttachmentProtocol.PROTOCOL_NAME, ProviderAttachmentProtocol.SINGLETON);
    protocols.put(ProviderLoggingProtocol.NAME, new ProviderLoggingProtocol());
    
    //OneWayProcessor static initialization
    OneWayProcessor.rtProcessor = this;
  }

  /**
   * Returns true, if the process0 method is synchronious (request-response
   * operation), returs false otherwise (one-way operation).
   */
  public boolean process(Transport transport) throws Exception {
    return process(transport, null);
  }

  /**
   * Returns true, if the process0 method is synchronious (request-response operation), returs false otherwise (one-way operation).
   */
  public boolean process(Transport transport, Hashtable metaData) throws Exception {
    ProviderContextHelperImpl context = null;
    //ServiceEndpointDefinition definition = null;
    boolean syncPrcs = true;
    boolean allocStat = false;
    if (metaData != null && metaData.get("allocStat") != null) {
      allocStat = true;
      AllocationStatisticRegistry.pushThreadTag("WSRuntime:processStart", false); //$JL-PERFORMANCE$
    }
    try {
      String taskName = "WS Processing at " + transport.getEntryPointID();
      WSLogTrace.attachNewCallEntry(taskName);
      WSLogTrace.setDirection(CallEntry.Direction.INBOUND);
      WSLogTrace.setConnectivityType("WS");
      // assume success, change on error
      WSLogTrace.setCallStatus(CallEntry.Status.SUCCESS);
      ContainerEnvironment containerEnv = ContainerEnvironmentHolder.getContainerEnvironment();
      
        
      AllocationStatisticRegistry.pushThreadTag("initialization", true);
      try {
        
        String entryPointID = transport.getEntryPointID(); 
        boolean b = isInvalidRTCfg(transport, entryPointID);
        if (b) {
          return syncPrcs;
        }
        //obtain context from pool
        context = ProviderContextHelperImpl.getPooledInstance();
        //creating and initializing runtime context from definition
        initializeRuntimeContextInstance(context, transport.getEntryPointID());
        //rebind metadata
        rebindMetaData(metaData, context);
        //set transport in context
        context.setTransport(transport);
        // The XI case. The soap application property should be
        // 'URN:SAP-COM:SOAP:XMS:APPLICATION:XIP' otherwise the protocols order
        // is not the correct one.
        String soapApplication = PublicProperties.getDTProperty(
            BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME, context);
        if (transport instanceof ESPXITransport
            && !BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_XI_VALUE.equals(soapApplication)) {
          throw new RuntimeProcessException(
              "XI transport is used. The design time soap application property should be '"
                  + BuiltInConfigurationConstants.SOAPAPPLICATION_SERVICE_XI_VALUE
                  + "'. Instead it is '"
                  + soapApplication
                  + "'. One of the possible resons is that the provider bean class is not annotated with annotation '@XIEnabled'.");
        }
        //obtain and set TB instance
        context.setTransportBinding(getTransportBinding(context));
        //TB initialization
        //TransportBinding transportBinding = context.getTransportBinding();
        
               
        /*
        OperationMapping operationMapping = context.getOperation();
        
        String javaOperationName = operationMapping.getJavaMethodName();
        ce.setHeader(WSLogTrace.JAVA_OPERATION_HEADER, javaOperationName);
                           
        String wsdlOperationName = operationMapping.getWSDLOperationName();
        ce.setHeader(WSLogTrace.WSDL_OPEARTION_HEADER, wsdlOperationName);
        */                       
      } finally {
        AllocationStatisticRegistry.popThreadTag();
      }
      syncPrcs = preProcess(context);
    } catch (Exception e) {
      //TODO Start using message ID concept
      LogRecord traceRec = LOC.traceThrowableT(Severity.ERROR, "process()", e);
      WSLogTrace.setCallStatus(CallEntry.Status.ERROR);
      if (traceRec != null && WSLogTrace.getStartTraceID() == null) {
        //this means that severity is higher than debug so we set all needed properties of call entry
        WSLogTrace.setStartTraceID(traceRec.getId().toString());
        WSLogTrace.setEndTraceID(traceRec.getId().toString());
      }     
//      ExceptionManager.logThrowable(Severity.WARNING, LoggingHelper.SYS_SERVER, LOC,
//                                    "process()"/*, RUNTIME_EXCEPTION_LOG + " EntryPoint: [" + transport.getEntryPointID() + "]"*/, e);
      if (! sendError(e, context, LOC)) {
        //try via transport object to send error response
        transport.sendServerError(e);
      }
    } finally {
      if (allocStat) {
        AllocationStatisticRegistry.popThreadTag();
        Map<String, AllocationStatisticRecord> res = AllocationStatisticRegistry.getAllocationStatistic("WSRuntime.*", true, true, true);
        String resStr = AllocationStatisticRegistry.generateTextReport(res);
        LOC.errorT(resStr);
        resStr = generateOverallTextReport(res);
        LOC.errorT(resStr);
      }
      if (syncPrcs) {
        reuseContext(context);
      }
      if(LOC.beDebug()){
        LogRecord lastRec = LOC.debugT("End WS processing on Provider side");
        WSLogTrace.setEndTraceID(lastRec.getId().toString());
      }
      WSLogTrace.releaseCallEntry();
    }
    return syncPrcs;
  }
  

  /**
   * Process ping request. The transport facade of this is a 
   * http HEAD method request and response. Used to determine
   * that the services are available and the passed arguments(auth and tn.)
   * are correct.
   * @param transport
   * @throws Exception
   */
  public void processPing(Transport transport, Map metaData) {
    final String method = "processPing()";

    HTTPTransport httpTransport = null;
  
    // Cast the transport
    if (transport instanceof HTTPTransport) {
      httpTransport = (HTTPTransport) transport;
    } else {
      throw new IllegalArgumentException(method + " can not process other parameter instance than HTTPTransport");
    }

    ProviderContextHelperImpl context = null;

    try {      
      // Check endpoint
      String entryPointID = transport.getEntryPointID(); 
      if (isInvalidRTCfg(transport, entryPointID)){
        //no valid endpoint available
        return;
      }
            
      //obtain context from pool
      context = ProviderContextHelperImpl.getPooledInstance();
      //creating and initializing runtime context from definition
      initializeRuntimeContextInstance(context, transport.getEntryPointID());
      
      //set transport in context
      context.setTransport(transport);
           
      //rebind metadata
      rebindMetaData(metaData, context);
      
      //obtain and set TB instance
      context.setTransportBinding(getTransportBinding(context));
      //TB initialization
    } catch (Exception e) {
      LOC.traceThrowableT(Severity.WARNING, "Error while processing HEAD ping request", e);
      HttpServletResponse resp = httpTransport.getResponse();
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
                                
    ProtocolExtensions p = (ProtocolExtensions) protocols.get(SECURITY_PROTOCOL); 
                                                                   
    try {      
        // according to the contract this should be called first
        // it sets security settings to the context.
        ((Protocol) p).handleRequest(context);
            
        // do the actual transport security check based on the view above.
        p.afterDeserialization(context);           
    } catch (ProtocolExceptionExt e) {
LOC.traceThrowableT(Severity.WARNING, "Error while processing HEAD ping request. For details see trace entry: " + e.getLogId(), e);
      // If the security checks fail - retun 401 unavailable.
      HttpServletResponse resp = httpTransport.getResponse();
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    } catch (ProtocolException e) {
      LOC.traceThrowableT(Severity.WARNING, "Error while processing HEAD ping request", e);
      // If the security checks fail - retun 401 unavailable.
      HttpServletResponse resp = httpTransport.getResponse();
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    } catch (MessageException e) {
      LOC.traceThrowableT(Severity.WARNING, "Error while processing HEAD ping request", e);
      // If the security checks fail - retun 401 unavailable.
      HttpServletResponse resp = httpTransport.getResponse();
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }// processPing

  /**
   * Preprocess method.
   * Returns true, if the processing is synchronious (synchronous request-response operation), and returs false 
   * when the processing is asynchronous (asynchronous request-response or one-way operation).
   */
  private boolean preProcess(ProviderContextHelperImpl context) throws InvocationTargetException, Exception {
    final String METHOD = "preProcess(): ";
    AllocationStatisticRegistry.pushThreadTag("beforeHandleRequest", true);
    int commPattern;
    boolean isASyncPattern;
    TransportBinding transportBinding;
    
    try{
      if (WSLogTrace.getSemantics() == null) {
        //initially we suppose it is a request-response MEP
        WSLogTrace.setSemantics("request-response");
        WSLogTrace.setHeader(CallEntry.HEADER_PROCESSING_STEPS, "parseRequest;invokeEndpoint;createResponse;sendResponse");
        WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "0");
        WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "0");
      }
      //if we are in rm-restore call then a subtask is already pushed. the next subtask will be invoke endpoint
      if (!WSLogTrace.getSemantics().equals("rm-restore")) {
        ThreadWrapper.pushSubtask("parseRequest", ThreadWrapper.TS_PROCESSING);
      }
      //attaching ConfigurationContext to the thread
      if(LOC.beDebug()){
        LOC.debugT(METHOD + "entered. Setting thread local object...");
      }
      applicationWSContext.attachRuntimeContext(context);
      transportBinding = context.getTransportBinding();
      //check whether configuration needs to be overwritten
      overwriteConfiguration(context);
      commPattern = transportBinding.getCommunicationPattern(context);
      isASyncPattern = (TransportBinding.ASYNC_COMMUNICATION == commPattern);
      if (LOC.beDebug()){
        LOC.debugT(METHOD + "is asynchronous communication pattern: " + isASyncPattern);
        //invoke protocols chain
        LOC.debugT(METHOD + "invoke protocols handleRequest...");
      }
    }finally{
      AllocationStatisticRegistry.popThreadTag();
    }
    int protocolsCode;
    AllocationStatisticRegistry.pushThreadTag("handleRequest", true);
    try{
      protocolsCode = ProtocolProcessor.protocolsHandleRequest(context);
      if (protocolsCode == ProtocolProcessor.BACK) {      //check the status returned by ProtocolProcessor
        context.getTransportBinding().sendResponseMessage(context, commPattern);
        if (LOC.beDebug()){
          LOC.debugT(METHOD + "protocols code is 'BACK' - stop processing.");
        }
        return true;
      } else if (protocolsCode == ProtocolProcessor.STOP) {//stop processing
        if (LOC.beDebug()){
          LOC.debugT(METHOD + "protocols code is 'STOP' - stop processing");
        }
        return true;
      } else if (protocolsCode == ProtocolProcessor.BACK_AND_CONTINUE_PROCESSING) {
        if (LOC.beDebug()){
          LOC.debugT(METHOD + "protocols code is 'BACK_AND_CONTINUE_PROCESSING' - continue processing.");
        }
      }
    }catch(ProtocolExceptionExt pee){
      LOC.traceThrowableT(Severity.WARNING, "Error during protocol invocation - handleRequest. For details see trace entry: " + pee.getLogId(), pee);
      throw pee;
    }
    finally {
      AllocationStatisticRegistry.popThreadTag();
    }
    
    // check for service with local transport configuration
    Transport tr = context.getTransport();
    if (tr instanceof HTTPTransport) {
      HTTPTransport transport = (HTTPTransport) tr;
      boolean isLocalConfiguration = isLocalTransportService(context);
      boolean isLocalCall = transport.getRequest() instanceof LocalHttpServletRequest;
      if (isLocalCall ^ isLocalConfiguration) { // either service or client uses
                                                // local transport, but not both
        StaticConfigurationContextImpl staticContext = (StaticConfigurationContextImpl) context.getStaticContext();
        String applicationName = staticContext.getTargetApplicationName();
        String serviceName = staticContext.getWebServiceName();
        Category category = ProviderLogger.WS_PROVIDER_CATEGORY;
      if (isLocalConfiguration) {
        if (!isLocalCall) {
            SimpleLogger.log(Severity.ERROR, category, LOC, "SOA.wsr.030101",
                "Remote client is trying to invoke local transport web service [{0}] in application [{1}]",
                new Object[] { serviceName, applicationName });
            throw new Exception("Remote client is trying to invoke local transport web service [" + serviceName
                + "] in application [" + applicationName + "]");
        }
      } else {
        if (isLocalCall) {
            ClientConfigurationContext cctx = ((LocalHttpServletRequest) transport.getRequest())
                .getClientConfigurationContext();
            String remoteApplicationName = cctx.getServiceContext().getApplicationName();
            SimpleLogger
                .log(
                    Severity.ERROR,
                    category,
                    LOC,
                    "SOA.wsr.030102",
                    "Local transport client in application [{0}] is trying to invoke web service [{1}] in application [{2}]",
                    new Object[] { remoteApplicationName, serviceName, applicationName });
            throw new Exception("Local transport client in application [" + remoteApplicationName
                + "] is trying to invoke web service [" + serviceName + "] in application [" + applicationName + "]");
          }
        }
      }
    }
    ImplementationContainer implContainer;
    OperationMapping operationMapping;
    AllocationStatisticRegistry.pushThreadTag("obtaining Operation and implContainer", true);
    try {
      // finding the operation mapping
      if (LOC.beDebug()) {
        LOC.debugT(METHOD + "find OperationMapping...");
      }
      operationMapping = context.getOperation();
      String javaOperationName = operationMapping.getJavaMethodName();
      WSLogTrace.setHeader(WSLogTrace.JAVA_OPERATION_HEADER, javaOperationName);

      String wsdlOperationName = operationMapping.getWSDLOperationName();
      WSLogTrace.setHeader(WSLogTrace.WSDL_OPEARTION_HEADER, wsdlOperationName);

      if (isOneWay(operationMapping) && 
          !(context.isRestartedAfterHibernation() || "tech-rm-ts".equals(WSLogTrace.getSemantics()))) {
        WSLogTrace.setSemantics("one-way");
        WSLogTrace.setHeader(CallEntry.HEADER_PROCESSING_STEPS, "parseRequest;invokeEndpoint");
      }
      // obtaining the implcontainer from instance Manager.
      if (LOC.beDebug()) {
        LOC.debugT(METHOD + "get implementation container...");
      }
      implContainer = this.implContainerAccessor.getImplementationContainer(context.getImplementationLink()
          .getImplementationContainerID());
      context.setImplementationContaner(implContainer);
    } finally {
      AllocationStatisticRegistry.popThreadTag();
    }
    ClassLoader implLoader;
    AllocationStatisticRegistry.pushThreadTag("obtaining ClassLoader from implContainer", true);
    try{
    //obtaing the impl loader
    if (LOC.beDebug()){
      LOC.debugT(METHOD + "get applicaiton loader..." + implContainer);
    }
    // i044259
    // NOTE: implLoader can be null for Galaxy container
    implLoader = implContainer.getImplementationLoader(context);
    }finally{
      AllocationStatisticRegistry.popThreadTag();
    }
    
    Class[] paramClasses = null;
    AllocationStatisticRegistry.pushThreadTag("loadParameterClasses", true);
    try{
      //creating the method parameter classes
      if (LOC.beDebug()){
        LOC.debugT(METHOD + "get parameterClasses..." + implLoader);
      }
      context.setImplClassLoader(implLoader);
      if (implLoader != null){
        paramClasses = loadParameterClasses(operationMapping, implLoader);
      }
      context.setParameterClasses(paramClasses);
    }finally{
      AllocationStatisticRegistry.popThreadTag();
    }
    
    Object[] paramObject;
    if (! context.isEJBHandlersProcessMode()) {
      //creating objects from message
      AllocationStatisticRegistry.pushThreadTag("getParameters(deserialization)", true);
      try{
        if (LOC.beDebug()){
          LOC.debugT(METHOD + "get parameters...");
        }
        paramObject = transportBinding.getParameters(paramClasses, implLoader, context);
      }finally{
        AllocationStatisticRegistry.popThreadTag();
      }
      AllocationStatisticRegistry.pushThreadTag("afterDeserialization", true);
      try{
        //invoke protocols chain afterDeserialization
        if (LOC.beDebug()){
          LOC.debugT(METHOD + "invoke protocols afterDeserialization...");
        }
        protocolsCode = ProtocolProcessor.protocolsAfterDeserialization(context);
      }catch(ProtocolExceptionExt pee){ 
        LOC.traceThrowableT(Severity.WARNING, "Error during protocol invocation - afterDeserialization. For details see trace entry: " + pee.getLogId(), pee);
        throw pee;
      }finally{
        AllocationStatisticRegistry.popThreadTag();
      }
      if (protocolsCode == ProtocolProcessor.BACK) {      //check the status returned by ProtocolProcessor
        context.getTransportBinding().sendResponseMessage(context, commPattern);
        return true;
      } else if (protocolsCode == ProtocolProcessor.STOP) {//stop processing
        return true;
      }
    } else {
      paramObject = new Object[paramClasses.length]; //the data will be filled in later by preInvoke()
    }
    context.setParameterObjects(paramObject);
    //in case it is one-way use the OneWayProcessor and return
    boolean isOneWay = isOneWay(operationMapping);
    if (LOC.beDebug()){
      LOC.debugT(METHOD + "is one-way operation: " + isOneWay);
    }
    //continue processing in a new thread (and send async response) only if the processing is not restored from hibernation. If restored from
    //hibernation the processing should continue in the same thread and no need for async response to be send i.e. there is not way to be send
    if (! context.isRestartedAfterHibernation()) {
      if (isOneWay || isASyncPattern) {
        WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "1");
        WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "1");
        ThreadWrapper.popSubtask();
        ThreadWrapper.pushSubtask("invokeEndpoint", ThreadWrapper.TS_PROCESSING);
        if (LOC.beDebug()){
          LOC.debugT(METHOD + "send asynchronous response...");
        }
        transportBinding.sendAsynchronousResponse(context);
        if (LOC.beDebug()){
          LOC.debugT(METHOD + "start a new thread for asynchronous processing...");
        }               
        //remove the transport object because it is not relevant anymore for async and one-way cases
        context.removePropertyInternal(ProviderContextHelperImpl.TRANSPORT);
        (new OneWayProcessor()).process(context, 
                                        isOneWay);
        //everything is fine in the first part of one-way processing
        WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, null);
        ThreadWrapper.popSubtask();
        return false;
      }
    }
    //perform main processing
    process0(context);
    return true;
  }
  /**
   * This method is reentrant. It is used for request-response, one-way and (a)synch processing.
   */
  void process0(ProviderContextHelperImpl context) throws Exception, InvocationTargetException {
    final String METHOD = "process0() ";
    
    TransportBinding transportBinding = context.getTransportBinding();
    int commPattern = transportBinding.getCommunicationPattern(context);

    OperationMapping operationMapping = context.getOperation();
    boolean isOneWay = isOneWay(operationMapping);
    Class[] paramClasses = context.getParameterClasses();
    Object[] paramObject = context.getParameterObjects();
    ImplementationContainer implContainer = context.getImplementationContaner();
    ClassLoader implLoader = context.getImplClassLoader();
    //invoking the business method
    if (LOC.beDebug()){
      LOC.debugT(METHOD + "invoke business method...");
    }
    Object result = null;
    AllocationStatisticRegistry.pushThreadTag("invokeMethod", true);
    
    ThreadWrapper.pushSubtask("invokeEndpoint", ThreadWrapper.TS_PROCESSING);
    String semantics = WSLogTrace.getSemantics();
    if (isOneWay) {
      if ("one-way-backend".equals(semantics)) {
        //if we are in the standard one-way-backend this is the first and only step
        WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "0");
        WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "0");
      } else if ("rm-restore".equals(semantics)) {
        //if we are restarting from hibernation this is the last (second) step
        WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "1");
        WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "1");
      }
    } else {
      //otherwise calling the business method is always the second step
      WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "1");
      WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "1");
    }
    InvocationTargetException invE = null;
    try {
      //if we have set the trace flag on we have to remove it before leaving our code
      if (!WSLogTrace.getTraceLevel().equals(CallEntry.TraceLevel.NONE)) {
        WSLogTrace.disablePassportTraceFlag();
      }
      result = implContainer.invokeMethod(operationMapping.getJavaMethodName(), paramClasses, paramObject, context);
      if (!WSLogTrace.getTraceLevel().equals(CallEntry.TraceLevel.NONE)) {
        WSLogTrace.enablePassportTraceFlag(WSLogTrace.WEBSERVICES_LOCATIONS);
      }
      if (LOC.beDebug()){
        LOC.debugT(METHOD + "invoke business method - normal termination.");
      }
      if (! (implContainer instanceof BuiltInWSEndpointImplContainer) /*&& isMeteringEnabled() */&& meteredTrBindings.contains(transportBinding)){
        serviceMeter.meterCall(context);
      }
    } catch (InvocationTargetException e) {
      invE = e;
      if (LOC.beDebug()){
        LOC.debugT(METHOD + "invoke business method finished with exception: " + e.getCause());
      }
      
      WSLogTrace.setCallStatus(CallEntry.Status.ERROR);
    } finally {
      AllocationStatisticRegistry.popThreadTag();
    }
    //oneway operation has no response or fault messages.
    if (isOneWay) {
      if (LOC.beDebug()){
        LOC.debugT(METHOD + " operation is oneway - return.");
      }
      if (invE != null) {
        //this is needed for WS-RM case, after restart from hibernation. In this case the exception needs to be logged by the MS.
        throw invE;
      } else {
        //end of processing
        WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, null);
      }
      ThreadWrapper.popSubtask();
      return;
    }
    //in this specific case, all necessary processing is done by postInvoke() method.
    if (context.isEJBHandlersProcessMode()) {
      if (LOC.beDebug()){
        LOC.debugT(METHOD + "EJBHandlersProcessMode flag is set - return.");
      }
      return;
    }
//    //in case of asynch call
//    if (context.isSendNoReply()) {
//      LOC.debugT(METHOD + "sendNoReply flag is set - return.");      
//      return;
//    }
    //initialize output message, and set it in the context
    if (LOC.beDebug()){
      LOC.debugT(METHOD + "initialize output message...");
    }
    Message msg;
    try{
      AllocationStatisticRegistry.pushThreadTag("initOutputMessage", true);
      msg = transportBinding.initOutputMessage(context);
      context.setMessage(msg);
    }finally{
      AllocationStatisticRegistry.popThreadTag();
    }
    //invoke protocols chain beforeSerialization
    if (LOC.beDebug()){
      LOC.debugT(METHOD + "invoke protocols beforeSerialization...");
    }
    AllocationStatisticRegistry.pushThreadTag("protocolsBeforeSerialization", true);
    try{
      ProtocolProcessor.protocolsBeforeSerialization(context);
    }catch(ProtocolExceptionExt pee){
      LOC.traceThrowableT(Severity.WARNING, "Error during protocol invocation - beforeSerialization. For details see trace entry: " + pee.getLogId(), pee);
      throw pee;
    }finally{
      AllocationStatisticRegistry.popThreadTag();
    }
    //building the response message
    ThreadWrapper.popSubtask();
    if (invE == null) { //if no exception has occurred
      if (LOC.beDebug()){
        LOC.debugT(METHOD + "create 'normal' response message...");
      }
      WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "2");
     
      ThreadWrapper.pushSubtask("createResponse", ThreadWrapper.TS_PROCESSING);
      AllocationStatisticRegistry.pushThreadTag("createResponseMessage (serialization)", true);
      try{
        transportBinding.createResponseMessage(result, getReturnClass(operationMapping, implLoader), paramObject, paramClasses, context);
        context.setMessageSemantic(ProviderContextHelper.NORMAL_RESPONSE_MSG);
      }finally{
        AllocationStatisticRegistry.popThreadTag();
      }
    } else { //in case of exception
      if (LOC.beDebug()){
        LOC.debugT(METHOD + "create 'fault' response message...");
      }
      
      WSLogTrace.setHeader(CallEntry.HEADER_PROCESSING_STEPS, "parseRequest;invokeEndpoint;createFault;sendFault");
      WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "1;2");
      WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "2");
      
      ThreadWrapper.pushSubtask("createFault", ThreadWrapper.TS_PROCESSING);
      logImplemException(invE.getCause(), context.getTransport().getEntryPointID(), LOC, context);
      msg = transportBinding.createFaultMessage(invE.getTargetException(), context);
      context.setMessage(msg); //sets the fault message in the context
      context.setMessageSemantic(ProviderContextHelper.FAULT_MSG);
    }            
    //invoke protocols chain backwards
    if (LOC.beDebug()){
      LOC.debugT(METHOD + "invoke protocols handleResponse...");
    }
    AllocationStatisticRegistry.pushThreadTag("protocolsHandleResponse", true);
    try{
      int protocolsCode = ProtocolProcessor.protocolsHandleResponse(context);
      if (protocolsCode == ProtocolProcessor.STOP) {//stop processing 
        //requestMonitor.endRequest(false);
        return;
      }
    }catch(ProtocolExceptionExt pee){
      LOC.traceThrowableT(Severity.WARNING, "Error during protocol invocation - handleResponse. For details see trace entry: " + pee.getLogId(), pee);
      throw pee;
    }finally {
        AllocationStatisticRegistry.popThreadTag();
      }
    //send response
     
    if (LOC.beDebug()){
      LOC.debugT(METHOD + "send response message...");
    }
    AllocationStatisticRegistry.pushThreadTag("sendResponseMessage", true);
    
    ThreadWrapper.popSubtask();
    if (context.getMessageSemantic() == ProviderContextHelper.FAULT_MSG) {
      ThreadWrapper.pushSubtask("sendFault", ThreadWrapper.TS_PROCESSING);
     
      WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "1;3");
      WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "3");
     
    } else {
      ThreadWrapper.pushSubtask("sendResponse", ThreadWrapper.TS_PROCESSING);
      
      WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "3");
      WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "3");
      
    }
    try{
      transportBinding.sendResponseMessage(context, commPattern);
      if (context.getMessageSemantic() == ProviderContextHelper.FAULT_MSG) {
        WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "1");
      } else {
        WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, null);
      }
      ThreadWrapper.popSubtask();
    }finally{
      AllocationStatisticRegistry.popThreadTag();
    }
    if (LOC.beDebug()){
      LOC.debugT(METHOD + " exited.");
    }
  }
  
  void reuseContext(ProviderContextHelperImpl ctx) {
    if (ctx == null) {
      return;
    }
    TransportBinding tb = ctx.getTransportBinding();   
    if (tb != null) { //in case is set
      tb.onContextReuse(ctx);
    }
    ProviderContextHelperImpl.rollBackInstance(ctx);
  }
  /**
   * Creates, initializes and returns StaticConfigurationContext instance, dedicated to be used in protocols' deploy events.
   */
  public StaticConfigurationContext getInitializedStaticContext(MetaDataAccessor metaDataAccessor, String bdURL, boolean isRuntimeMode) throws Exception {
    StaticConfigurationContextImpl scc = new StaticConfigurationContextImpl("", null);
    initilizeStaticContext(metaDataAccessor, scc, bdURL, isRuntimeMode);
    return scc;
  }
//********private methods****
   
  private void overwriteConfiguration(ProviderContextHelperImpl ctx) throws Exception {
    final String METHOD = "overwriteConfiguration() ";
    TransportBinding tb = ctx.getTransportBinding();
    String action = tb.getAction(ctx);
    if (LOC.beDebug()){
      LOC.debugT(METHOD + "getAction() value '" + action + "'");
    }
    if (action != null) {
      StaticConfigurationContextImpl sCtx = (StaticConfigurationContextImpl) ctx.getStaticContext();
      String key = BuiltInWSEndpointImplContainer.SINGLETON.getKey(sCtx.getTargetApplicationName(), sCtx.getWebServiceName(),
          sCtx.getRTConfiguration().getName(), action);
      InterfaceMapping iM = BuiltInWSEndpointImplContainer.SINGLETON.getInterfaceMapping(key);
      TypeMappingRegistry tmReg = BuiltInWSEndpointImplContainer.SINGLETON.getTypeMappingRegistry(key);
      BindingData bData = BuiltInWSEndpointImplContainer.SINGLETON.getBindingData(key);
      Variant varian = BuiltInWSEndpointImplContainer.SINGLETON.getVariant(key);
      
      if (LOC.beDebug()) {
        LOC.debugT(METHOD + " found intfMapping: " + iM + ", tmReg: " + ((tmReg != null) ? Integer.toString(tmReg.hashCode()):"null") + ", using key '" + key + "'");
      }
      if (iM != null && tmReg != null) { //there is 'builtIn' endpoint for this action
        sCtx.setPropertyInternal(StaticConfigurationContextImpl.INTERFACE_MAPPING, iM);
        sCtx.setPropertyInternal(StaticConfigurationContextImpl.TYPEMAPPING_REGISTRY, tmReg);
        
        // added for MEX support
        if (bData != null && varian != null) { 
          sCtx.setPropertyInternal(StaticConfigurationContextImpl.BINDING_DATA, bData);
          sCtx.setPropertyInternal(StaticConfigurationContextImpl.VARIANT, varian);
        }
        
        BuiltInWSEndpointImplContainer.SINGLETON.bindKeyInContext(ctx, key);
        //TODO apply Variant and BindingData and updated ProviderProtocol list. This could be needed for MEX support
      }
    }
  }

  /**
   * Sends back the exception which has been occured.
   * Returns true if succeeds. False otherwise
   */
  private boolean sendError(Throwable thr, ProviderContextHelperImpl rntContext, Location location) {
    try {
      if (rntContext != null) {
        TransportBinding tb = rntContext.getTransportBinding();
        if (tb != null) {
          tb.sendServerError(thr, rntContext);
          return true;
        }
      }
      return false;
    } catch (Exception trExc) { //could not send reply
      ExceptionManager.logThrowable(Severity.WARNING, LoggingHelper.SYS_SERVER, location, "RuntimeProcessor.sendError()",
                                    EXCEPTION_PROCESSING_LOG, trExc);
      return false;
    }
  }

  private final ProviderContextHelperImpl initializeRuntimeContextInstance(ProviderContextHelperImpl pCtx, String uriID) throws Exception {
    StaticConfigurationContextImpl staticCtx = (StaticConfigurationContextImpl) pCtx.getStaticContext(); 
    initilizeStaticContext(staticCtx, uriID); 
    addLogsInInitilizeStaticContextMethod(staticCtx);
    pCtx.getPersistableContext().createSubContext(PERSISTENT_RUNTIME_SUBCONTEXT);     
    //initializing protocols chain
    String[] protocolsOrder = staticCtx.getProtocolsOrder();
    boolean hHandProt = false;
    //set protocol instances
    //ProviderProtocol[] protocols = new ProviderProtocol[protocolsOrder.length];
    ArrayList<ProviderProtocol> protocols = new ArrayList<ProviderProtocol>();
    for (int i = 0; i < protocolsOrder.length; i++) {
      ProviderProtocol tmpProt = (ProviderProtocol) this.protocols.get(protocolsOrder[i]);
      if (tmpProt == null) {
        if (PROVIDER_PASSPORT_PROTOCOL.equals(protocolsOrder[i])) {
          continue; //if this protocol is missing, be silent for that
        } else {
          throw new ServerRuntimeProcessException(RuntimeExceptionConstants.PROTOCOL_INSTANCE_NOT_FOUND, new Object[]{protocolsOrder[i]});
        }
      }
      if (tmpProt instanceof ProviderJAXRPCHandlersProtocol || tmpProt instanceof ProviderJAXWSHandlersProtocol) {
        if (i < protocolsOrder.length - 1) { //ProviderJAXRPC(JAXWS)HandlersProtocol must be last protocol in the protocol order
          StringBuffer pOrder = new StringBuffer();
          for (int p = 0; p < protocolsOrder.length; p++) { //build string for protocol order
            pOrder.append(protocolsOrder[p]).append(" ");
          }
          throw new ServerRuntimeProcessException(RuntimeExceptionConstants.PROVIDER_HANDLERS_PROTOCOL_IS_NOT_LAST_PROTOCOL, new Object[]{"ProviderJAXRPC(JAXWS)HandlersProtocol", pOrder.toString()});
        }
        hHandProt = true;
      }
      protocols.add(tmpProt);
    }
    //set specific ejb handle mode, if there are handlers and the endpoint is EJB
    if (EJBImplementationLink.IMPLEMENTATION_CONTAINER_ID.equals(staticCtx.getInterfaceMapping().getImplementationLink().getImplementationContainerID())) {
      if (hHandProt) {
        pCtx.setEJBHandlersProcessMode(true);
        if (LOC.beDebug()){
          LOC.debugT("initializeRuntimeContextInstance(), EJBHandlersProcessMode flag is set");
        }
      }
    }
    pCtx.setProtocols(protocols.toArray(new ProviderProtocol[protocols.size()]));    
    return pCtx;
  } 
   
  private final void initilizeStaticContext(StaticConfigurationContextImpl ctx, String uriID) throws WSWarningException, Exception {
    initilizeStaticContext(this.metaDataAccessor, ctx, uriID, true); 
  }
  
  /**
   * Take an instance from runtimeContextPool and initialize it.
   */
  private final void initilizeStaticContext(MetaDataAccessor metaDAccessor, StaticConfigurationContextImpl ctx, String uriID, boolean isRuntimeMode) throws WSWarningException, Exception {
    AllocationStatisticRegistry.pushThreadTag("initilizeStaticContext", true);
    try {
      BindingData bD = metaDAccessor.getBindingData(uriID);
      InterfaceMapping intfMap = metaDAccessor.getInterfaceMappingForBindingData(uriID);
      InterfaceDefinition intDef = metaDAccessor.getInterfaceDefinitionForBindingData(uriID);
      if (bD == null || intfMap == null || intDef == null) {
        throw new RuntimeProcessException("Unexpected uriID '" + uriID + "'. Found: BindingData '" + bD + "', interfaceMapping '" + intfMap + "' interfaceDefinition '" + intDef + "'");
      }
      Variant v = getVariant(intDef, bD);
      //TypeMappingRegistry tmReg = srvCtx.getTypeMappingRegistryForBindingData(uriID, "literal");
      TypeMappingRegistry tmReg = metaDAccessor.getTypeMappingRegistryForBindingData(uriID);
      JAXBContext jaxbCtx = metaDAccessor.getJAXBContextForBindingData(uriID);
      String appName = metaDAccessor.getApplicationName(uriID);
      String wsName = metaDAccessor.getWebServiceName(uriID);
      
      if(isRuntimeMode) {
          WSLogTrace.setInterfaceInternalName(intDef.getId());
          String soapApp = SoapApplicationRegistry.getSoapApplicationID(intDef, false);
          WSLogTrace.setHeader("SOAPApplicationID", soapApp);
      }
      
      //set the data
      ctx.setPropertyInternal(StaticConfigurationContextImpl.BINDING_DATA, bD);
      ctx.setPropertyInternal(StaticConfigurationContextImpl.INTERFACE_MAPPING, intfMap);
      ctx.setPropertyInternal(StaticConfigurationContextImpl.VARIANT, v);
      if(isRuntimeMode && ! ctx.getInterfaceMapping().isGalaxyInterface()) {
        if (tmReg != null) {
          ctx.setPropertyInternal(StaticConfigurationContextImpl.TYPEMAPPING_REGISTRY, tmReg);
        } else if (jaxbCtx != null) {
          ctx.setPropertyInternal(StaticConfigurationContextImpl.JAXBCONTEXT, jaxbCtx);
        } else {
          throw new RuntimeProcessException("Both TypeMappingRegistry and JAXBContext are null for BindingData with url '" + uriID + "'");
        }
      }
      ctx.setPropertyInternal(StaticConfigurationContextImpl.LOCATION, LOC); //TODO set correct location
      ctx.setPropertyInternal(StaticConfigurationContextImpl.APPLICATION_NAME, appName);
      ctx.setPropertyInternal(StaticConfigurationContextImpl.WEBSERVICE_NAME, wsName);
      ctx.setPropertyInternal(StaticConfigurationContextImpl.ENDPOINT_REQUEST_URI, uriID);    
      String[] protocolsOrder = getProtocolOrder(v.getInterfaceData(), bD);
      ctx.setPropertyInternal(StaticConfigurationContextImpl.PROTOCOL_ORDER, protocolsOrder);
    } finally {
      AllocationStatisticRegistry.popThreadTag();
    }
//    staticCtx.implementationLink = definition.getImplLink();
//    staticCtx.operationRegistry = definition.getOperationMappingRegistry();
//    staticCtx.applicationName = definition.getOwner().getApplicationName();
//    staticCtx.typeMappingRegistry = definition.getOwner().getTypeMappingRegistry();
//    staticCtx.javaToQNameMappingRegistry = definition.getOwner().getJavaToQNameMappingRegistry();
//    staticCtx.wsLocation = definition.getOwner().getWsLocation();
//    staticCtx.wsdName = definition.getOwner().getWsdName();
//    staticCtx.viName = definition.getvInterfaceName();
//    staticCtx.configurationName = definition.getConfigurationName();
//    staticCtx.webServiceName = definition.getOwner().getWSIdentifier().getServiceName();
//    staticCtx.endpointOperations = definition.getOperations();
//    //!!!!!Building securityPolicy string may be is not correct
//    if (staticCtx.implementationLink.getImplId().equals(EJBImplConstants.EJB_ID)) {
//      staticCtx.securityPolicy = staticCtx.implementationLink.getProperties().getProperty(EJBImplConstants.APPLICATION_NAME)
//              + "*" + definition.getOwner().getWSIdentifier().getJarName();
//    } else { //java impllink
//      String endPId = definition.getServiceEndpointId();
//      if (endPId.startsWith("/")) {
//        endPId = definition.getServiceEndpointId().substring(1);
//      }
//      endPId = endPId.replace('/', '_').replace('/', '_');
//      staticCtx.securityPolicy = staticCtx.implementationLink.getProperties().getProperty(JavaImplConstants.APPLICATION_NAME)
//              + "*" + endPId;
//    }
//    
//    Hashtable map = new Hashtable();
//    Feature[] features;
//    Collection mapCol = definition.getProtocolIDFeatureMappings().values();
//    Iterator itr = mapCol.iterator();
//    while (itr.hasNext()) {
//      features = (Feature[]) itr.next();
//      for (int i = 0; i < features.length; i++) {
//        map.put(features[i].getFeatureName(), features[i]);
//      }
//    }
//    staticCtx.features = map;    
//    staticCtx.protocolOrder = definition.getOrderedProtocolIDs();
//    if (definition.getOwner().isFastEJB()) {
//      staticCtx.environmentProperties.setProperty(EJBImplConstants.FAST_EJB_FEATURE, "true");
//    }
  
//    if (warnings.size() != 0) {
//      WSWarningException e = new WSWarningException();
//      e.addWarnings(warnings);
//      throw e;
//    }  
  }
  
  private void addLogsInInitilizeStaticContextMethod(StaticConfigurationContextImpl staticCtx) throws ConfigurationException {
    Variant v = (Variant) staticCtx.getProperty(StaticConfigurationContextImpl.VARIANT);
    InterfaceData intfData = v.getInterfaceData();
    String appName = (String) staticCtx.getProperty(StaticConfigurationContextImpl.APPLICATION_NAME);

    WSLogTrace.setInterfaceQName(new QName(intfData.getNamespace(),intfData.getName()));
    CallEntry.TraceLevel traceLevel = WSLogTrace.calculateEffectiveTraceLevelForCurrentUser("WS", CallEntry.Direction.INBOUND,  WSLogTrace.getInterfaceQName());
    if (!traceLevel.equals(CallEntry.TraceLevel.NONE)) {
      WSLogTrace.enablePassportTraceFlag(WSLogTrace.WEBSERVICES_LOCATIONS);
    }
    if(LOC.beDebug()){
      LogRecord firstRec = LOC.debugT("Start WS processing on Provider side");
      WSLogTrace.setStartTraceID(firstRec.getId().toString());
    }
    //boolean isConsumer = (currentCallEntry.getWSRole().equals(WSRole.C) || currentCallEntry.getWSRole().equals(WSRole.TC));
    WSLogTrace.setHeader("ApplicationComponent", appName);

  }

  public static Class[] loadParameterClasses(OperationMapping operation, ClassLoader implLoader) throws ClassNotFoundException {
    ParameterMapping[] params = operation.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE);
    Class[] result = new Class[params.length];
    
    String jClass;
    for (int i = 0; i < params.length; i++) {
      //in case of out or in_out param, load holder class.
      if (params[i].getParameterType() == ParameterMapping.IN_OUT_TYPE || params[i].getParameterType() == ParameterMapping.OUT_TYPE) {
        jClass = params[i].getHolderName();
      } else {
        jClass = params[i].getJavaType();
      }
      result[i] = loadClass(jClass, implLoader);
    }

    return result;
  }

  public static Class getReturnClass(OperationMapping operation, ClassLoader loader) throws ClassNotFoundException {
    ParameterMapping[] outParams = operation.getParameters(ParameterMapping.RETURN_TYPE);
    
    if  (outParams.length == 0) {
      return Void.TYPE;
    }

    return loadClass(outParams[0].getJavaType(), loader);
  }

  public static Class loadClass(String clDecl, ClassLoader loader)  throws ClassNotFoundException {
    int ind = clDecl.indexOf(ParameterMapping.JAVA_ARRAY_DIMENSION); //as it is described in the VI.

    if (ind == -1) { //this is not array
      //for simple types
      if (clDecl.equals("byte")) {
        return byte.class; 
      } else if (clDecl.equals("char")) {
        return char.class;
      } else if (clDecl.equals("boolean")) {
        return boolean.class;
      } else if (clDecl.equals("short")) {
        return short.class;
      } else if (clDecl.equals("int")) {
        return int.class;
      } else if (clDecl.equals("float")) {
        return float.class;
      } else if (clDecl.equals("long")) {
        return long.class;
      } else if (clDecl.equals("double")) {
        return double.class;
      } else if (clDecl.equals("void")) {
        return Void.TYPE;
      }

      // i044259
      if (loader == null){
        return null;
      }
      //this is not a simple type use the loader
      return loader.loadClass(clDecl);
    }

    // i044259
    if (loader == null){
      return null;
    }
    //this is an array
    int[] arrDim = new int[(clDecl.length() - ind) / 2];
    Class compClass = loadClass(clDecl.substring(0, ind), loader);
    return java.lang.reflect.Array.newInstance(compClass, arrDim).getClass();
  }

  void logImplemException(Throwable thr, String entryPoint, Location loc, ProviderContextHelperImpl ctx) {
    ClassLoader implLoader = ctx.getImplClassLoader();
    String csnComponent = LoggingUtilities.getCsnComponentByClassLoader(implLoader);
    String dcName = LoggingUtilities.getDcNameByClassLoader(implLoader);

    LogRecord errorRec = SimpleLogger.trace(Severity.ERROR, LOC, dcName, "SOA.wsr.030107", csnComponent, null, "EntryPoint: [" + entryPoint
        + "]. Implementation exception occurs for application " + ctx.getStaticContext().getTargetApplicationName()
        + ". " + "Check implementation container(e.g. EJB) logs for additional information", thr);
    
    if (errorRec != null) {
      if (WSLogTrace.getStartTraceID() == null) {
        WSLogTrace.setStartTraceID(errorRec.getId().toString());
        WSLogTrace.setEndTraceID(errorRec.getId().toString());
      }
    }
    /*
     * loc.catching("EntryPoint: [" + entryPoint + "]. Implementation exception
     * occurs. " + "Please check implementation container(e.g. EJB) logs for
     * additional information!" , thr);
     */
  }

  //searches for one-way feature
  static boolean isOneWay(OperationMapping op) {
    if (OperationMapping.MEP_ONE_WAY.equals(op.getProperty(OperationMapping.OPERATION_MEP))) {
      return true;
    }
//    Feature fs[] = opD.getFeatures();
//    for (int i = 0; i < fs.length; i++) {
//      if (fs[i].getFeatureName().equals(ONE_WAY_OPERATION_FEATURE)) {
//        return true;
//      }
//    }
    return false;
  }

//=========== RuntimeEnvironment methods ========

	public void restart(ConfigurationContext arg0) throws HibernationEnvironmentException {
    ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) arg0;
    boolean syncPrcs = true;
    try {
      ThreadWrapper.pushSubtask("rm-restore at ", ThreadWrapper.TS_PROCESSING);
      ctx.restartedAfterHibernation(true);
      syncPrcs = preProcess(ctx);
    } catch (Exception e) {
      ExceptionManager.logThrowable(Severity.INFO, LoggingHelper.SYS_SERVER, LOC,
                                    "restart()"/*, RUNTIME_EXCEPTION_LOG + " EntryPoint: [" + transport.getEntryPointID() + "]"*/, e);      
      throw new HibernationEnvironmentException(e);
//      reuseRuntimeContext(context);
    } finally {
      //!!!! here the one that has invoked this method should not maintain a reference to that context anymore.
      if (syncPrcs) {
        reuseContext(ctx);
      }
    }
	}

  public void hibernate(ConfigurationContext context, OutputStream output) throws HibernationEnvironmentException {
    //check whether this method is invoked from protocol method
    if (applicationWSContext.getConfigurationContext() != context) {
      throw new HibernationEnvironmentException("Call to hibernate() not allowed. Only protocols in their methods could call the hibernate() method.");
    }
    try {
      ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) context;
      //invoke protocols chain beforeHibernation
      ProtocolProcessor.protocolsBeforeHibernation(ctx);
      //invoke transportbinding's hibernation
      TransportBinding tb = ctx.getTransportBinding();
      if (tb instanceof Hibernation) {
        ((Hibernation) tb).beforeHibernation(ctx);
      }
      //runtime hibernation
      ConfigurationContext pCtx = ctx.getPersistableContext();
      ConfigurationContext pCtxNew = pCtx.createSubContext(PERSISTENT_RUNTIME_SUBCONTEXT);
      //save transportURL
      String transportURL = ctx.getTransport().getEntryPointID();
      pCtxNew.setProperty(ProviderContextHelperImpl.TRANSPORT, transportURL);
      if (LOC.beDebug()){
        LOC.debugT("hibernate, ENDPOINT_ID: " + transportURL + " set into context: " + pCtxNew);
      }
      //serialize context into stream
      XMLTokenWriter writer = XMLTokenWriterFactory.newInstance(output);
      writer.init(output);
      //writer root element
      writer.enter(null, ConfigurationContextSerializerImpl.DATA_HOLDER_ELEMENT);
      //serialize message
      ConfigurationContextSerializerImpl.serializeMessage(writer, context, output);
      output.flush();
      //invoke protocols chain finishMessageDesrialization
      ProtocolProcessor.protocolsFinishMessageDeserialization(ctx);       
      //serialize context data
      ConfigurationContextSerializerImpl.serializeContext(writer, ctx);
      writer.leave(); //leave root element
      writer.flush();
      output.flush();
      //invoke protocols chain finishHibernation
      ProtocolProcessor.protocolsFinishHibernation(ctx); 
    } catch (Exception e) {
      throw new HibernationEnvironmentException(e);
    }
  }
  
  public ConfigurationContext restore(InputStream input) throws HibernationEnvironmentException {
    try {
      //deserialize context from stream
      ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) ConfigurationContextSerializerImpl.deserialize(input);
      
      ConfigurationContext pCtx = ctx.getPersistableContext().getSubContext(PERSISTENT_RUNTIME_SUBCONTEXT);
      //restore transportURL
      String transportURL = (String) pCtx.getProperty(ProviderContextHelperImpl.TRANSPORT);
      //init static context
      ctx = initializeRuntimeContextInstance(ctx, transportURL);    
      //obtainig TB instance
      ctx.setTransportBinding(getTransportBinding(ctx));
      //set asych processing
      ctx.sendNoReply(true);             
      //invoke transportbinding's hibernation
      TransportBinding tb = ctx.getTransportBinding();
      if (tb instanceof Hibernation) {
        ((Hibernation) tb).afterHibernation(ctx);
      }
      //invoking protocols chain afterHibernation
      ProtocolProcessor.protocolsAfterHibernation(ctx);
      return ctx;
    } catch (Exception e) {
      ExceptionManager.logThrowable(Severity.INFO, LoggingHelper.SYS_SERVER, LOC, "restore()", e);      
      throw new HibernationEnvironmentException(e);
    }
  }
  
  public void registerProviderProtocol(ProviderProtocol protocol) throws RuntimeProcessException {
    this.protocols.put(protocol.getProtocolName(), protocol);
    //wsdlVisualizer.updateConfigurationBuilder(this.protocols);
  }
  
  public ProviderProtocol unregisterProviderProtocol(String protocolID) throws RuntimeProcessException {
    if (protocolID == null) {
      throw new IllegalArgumentException("Cannot unregister protocol: protocolID is null");
    }
    ProviderProtocol p = (ProviderProtocol) this.protocols.remove(protocolID);
    //wsdlVisualizer.updateConfigurationBuilder(this.protocols);
    return p;
  }
  
  public WSDLVisualizer getWSDLVisualizer() {
    return this.wsdlVisualizer;
  }
   
  public void sendMessageOneWay(String endpointURL, Message msg) throws RuntimeProcessException {
    sendMessageOneWay(endpointURL, msg, null);
  }
  
  public void sendMessageOneWay(String endpointURL, Message msg, String action) throws RuntimeProcessException {
    final String METHOD = "sendMessageOneWay() ";
    LOC.debugT(METHOD + "entered...");
    if (msg instanceof SOAPMessage) {
      if (LOC.beDebug()){
        LOC.debugT(METHOD + "SOAPMessage has passed as parameter - process via transportbinding:" + InterfaceMapping.SOAPBINDING);
      }
      TransportBinding soapTB = (TransportBinding) this.transportBindings.get(InterfaceMapping.SOAPBINDING);
      soapTB.sendMessageOneWay(endpointURL, msg, action);
    } else {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.MESSAGE_TYPE_CANNOT_BE_SEND_AS_ONEWAY_REQUEST, new Object[]{msg}); 
    }
  }
  
  final TransportBinding getTransportBinding(ProviderContextHelper context) throws Exception {
    //check for XI transportbinding cfg.
    Transport transport = context.getTransport(); 
    if(transport != null && transport instanceof ESPXITransport) {
      return (TransportBinding) transportBindings.get(ESPXITransportBinding.ID);
    }
    //Obtain TB from mapping.xml 
    InterfaceMapping mapping = context.getStaticContext().getInterfaceMapping();
    String bType = mapping.getBindingType();
    TransportBinding tB = (TransportBinding) transportBindings.get(bType); 
    if (tB != null) {
      return tB;
    }
    throw new ServerRuntimeProcessException(RuntimeExceptionConstants.TRANSPORT_BINDING_NOT_FOUND, new Object[]{bType});
    //return null;//(TransportBinding) transportBindings.get(InterfaceMapping.SOAPBINDING);
  }
  
  private Variant getVariant(InterfaceDefinition intDef, BindingData bD) throws RuntimeProcessException {
    String vName = bD.getVariantName();
    Variant[] vs = intDef.getVariant();
    for (int i = 0; i < vs.length; i++) {
      if (LOC.beDebug()){
        LOC.debugT("getVariant(), currVarName '" + vs[i].getName() + "' targetName '" + vName + "' BindingData.interface-id  '" + bD.getInterfaceId() + "' interfaceDef.interface-id '" + intDef.getId() + "'");
      }
      if (vs[i].getName().equals(vName)) {
        return vs[i];
      }
    }
    throw new RuntimeProcessException("Unable to find Variant with name " + vName);
  }
  
  /**
   * Returns the protocol order. First checks for 'protocol-order' property, and if not found default protocol order is used. 
   */
  private static String[] getProtocolOrder(InterfaceData iD, BindingData bD) throws ServerRuntimeProcessException {
    PropertyType pT = bD.getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS, BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY);
    PropertyType soapAppProp = iD.getSinglePropertyList().getProperty(BuiltInConfigurationConstants.SOAPAPPLICATION_PROPERTY_QNAME);
    String[] res; 
    if (pT != null) {
      if (LOC.beDebug()){
        LOC.debugT("getProtocolOrder(): 'protocolOrder' property with value '" + pT.get_value() + "'is found and will be used.");
      }
      res = pT.get_value().split("\\s+");
    } else if (soapAppProp != null) { //check 'SOAPApplication' property
      String soapApp = soapAppProp.get_value();
      if (LOC.beDebug()){
        LOC.debugT("getProtocolOrder(): 'SOAPApplication' property with value '" + soapApp + "'is found and will be used.");
      }
      res = (String[]) PROTOCOLORDER_REGISTRY.get(soapApp);
      if (res == null) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.SOAPAPPLICATION_PROPERTY_VALUE_NOT_RECORGNIZED, new Object[]{soapApp});
      }
    } else {
      if (LOC.beDebug()){
        LOC.debugT("getProtocolOrder(): default protocol order will be used.");
      }
      res = DEFAULT_PROTOCOL_ORDER;
    }
    return res;
  }
  /**
   * Extracts runtime relevant data from <code>metaData</code>, and binds it under the same key in dynamic subcontext of <code>ctx</code>.
   */
  private static void rebindMetaData(Map metaData, ProviderContextHelper ctx) {
    if (metaData != null) {
      Object servlet = metaData.get(CALLING_SERVLET_PROPERTY);
      if (servlet != null) {
        ctx.getDynamicContext().setProperty(CALLING_SERVLET_PROPERTY, servlet);
      }
    }
  }
  
//==================== EJBEndpointHandlersInterceptor =================================  
  public boolean preInvoke() {
    ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) ApplicationWebServiceContextImpl.getSingleton().getConfigurationContext();
    if (! ctx.isEJBHandlersProcessMode()) { //in case it is not special ejb handler processing, there is nothing to do, return true
      return true;
    }
    try {
      ProviderProtocol ps[] = ctx.getProtocols();
      //upcast to handlers protocol, to be sure that this is the protocol. It must be the last protocol in the list.
      if (! (ps[ps.length - 1] instanceof ProviderJAXRPCHandlersProtocol) 
          && ! (ps[ps.length - 1] instanceof ProviderJAXWSHandlersProtocol)) {
        throw new RuntimeProcessException("ProviderJAXRPC(JAXWS)Protocol must be the last protocol in the protocol chain.");
      }
      //set flag to invoke handlers
      ctx.setEJBHandlersProcessModeInvHandlers(true);
      TransportBinding transportBinding = ctx.getTransportBinding();
      int commPattern = transportBinding.getCommunicationPattern(ctx); 
      //invoke handleRequest() method
      int retCode = ProtocolProcessor.protocolsHandleRequest0(ctx, ps, ps.length - 1);
      if (retCode == ProtocolProcessor.BACK) {
        ctx.getTransportBinding().sendResponseMessage(ctx, commPattern); //send response message from context
        return false;
      } else if (retCode != ProtocolProcessor.CONTINUE) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.UNKNOW_PROTOCOL_PROCESSING_RETURNCODE, new Object[]{new Integer(retCode)});
      }
      ClassLoader implLoader = ctx.getImplClassLoader();
      Class[] paramClasses = ctx.getParameterClasses();
      //creating objects from message
      if (LOC.beDebug()){
        LOC.debugT("preInvoke(), Get parameters...");
      }
      Object[] paramObjects = transportBinding.getParameters(paramClasses, implLoader, ctx);
      //copy the data from paramObject into the array which is passed to the EJB container
      Object[] ejbParamObjects = ctx.getParameterObjects();
      if (ejbParamObjects.length != paramObjects.length) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.DESERIALIZED_OBJECTARRAY_DIFFER_IN_LENGTH, new Object[]{new Integer(ejbParamObjects.length), new Integer(paramObjects.length)});        
      }
      for (int i = 0; i < paramObjects.length; i++) {
        ejbParamObjects[i] = paramObjects[i];
      }
      //invoke protocols chain afterDeserialization
      if (LOC.beDebug()){
        LOC.debugT("preInvoke(), Invoke protocols afterDeserialization...");
      }
      int protocolsCode = ProtocolProcessor.protocolsAfterDeserialization(ctx);
      if (protocolsCode == ProtocolProcessor.BACK) {      //check the status returned by ProtocolProcessor
        ctx.getTransportBinding().sendResponseMessage(ctx, commPattern);
        return false;
      } else if (retCode != ProtocolProcessor.CONTINUE) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.UNKNOW_PROTOCOL_PROCESSING_RETURNCODE, new Object[]{new Integer(retCode)});
      }
      
    } catch (Exception e) {
      ExceptionManager.traceThrowable(Severity.INFO, LOC, "preInvoke()", e);
      sendError(e, ctx, LOC); 
      return false;
    }
    return true;
  }

  public void postInvoke(Object result, Throwable thr) {
    ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) ApplicationWebServiceContextImpl.getSingleton().getConfigurationContext();
    if (! ctx.isEJBHandlersProcessMode()) { //in case it is not special ejb handler processing, there is nothing to do, return true
      return;
    }
    try {
      TransportBinding transportBinding = ctx.getTransportBinding(); 
      int commPattern = transportBinding.getCommunicationPattern(ctx);
      boolean isASyncPattern = commPattern == TransportBinding.ASYNC_COMMUNICATION;
      boolean isOneWay = isOneWay(ctx.getOperation());
      if (isOneWay || isASyncPattern) { //no response should be send
        return;
      }
      //initialize output message, and set it in the context
      if (LOC.beDebug()){
        LOC.debugT("postInvoke(), Initialize output message...");
      }
      Message msg = transportBinding.initOutputMessage(ctx);
      ctx.setMessage(msg);
      //invoke protocols chain beforeSerialization
      if (LOC.beDebug()){
        LOC.debugT("postInvoke(), Invoke protocols beforeSerialization...");      
      }
      ProtocolProcessor.protocolsBeforeSerialization(ctx);
      //take necessary params from context
      OperationMapping operationMapping = ctx.getOperation();
      ClassLoader implLoader = ctx.getImplClassLoader();
      Class[] paramClasses = ctx.getParameterClasses();
      Object[] paramObjects = ctx.getParameterObjects();
      //building the response message
      if (thr == null) { //if no exception has occurred
        if (LOC.beDebug()){
          LOC.debugT("postInvoke(), Create 'normal' response message...");
        }
        transportBinding.createResponseMessage(result, getReturnClass(operationMapping, implLoader), paramObjects, paramClasses, ctx);
        ctx.setMessageSemantic(ProviderContextHelper.NORMAL_RESPONSE_MSG);
      } else { //in case of exception
        if (LOC.beDebug()){
          LOC.debugT("postInvoke(), Create 'fault' response message...");
        }
        logImplemException(thr, ctx.getTransport().getEntryPointID(), LOC, ctx);
        msg = transportBinding.createFaultMessage(thr, ctx);
        ctx.setMessage(msg); //sets the fault message in the context
        ctx.setMessageSemantic(ProviderContextHelper.FAULT_MSG);
      }            
      //invoke protocols chain backway
      if (LOC.beDebug()){
        LOC.debugT("postInvoke(), Invoke protocols handleResponse...");
      }
      int protocolsCode = ProtocolProcessor.protocolsHandleResponse(ctx);
      if (protocolsCode == ProtocolProcessor.STOP) {//stop processing
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.UNKNOW_PROTOCOL_PROCESSING_RETURNCODE, new Object[]{new Integer(protocolsCode)});
      }
      //send response
      if (LOC.beDebug()){
        LOC.debugT("postInvoke(), Send response message...");
      }
      transportBinding.sendResponseMessage(ctx, commPattern);
      if (LOC.beDebug()){
        LOC.debugT("postInvoke() method exited.");
      }
    } catch (Exception e) {
      ExceptionManager.traceThrowable(Severity.INFO, LOC, "postInvoke()", e);      
      sendError(e, ctx, LOC);      
    }
  }

  public Map<String, Object> getJAXWSMessageContext() {
    return null;
  }
  /**
   * If the requested cfg is not valid http response is send and the method returns true.
   * Otherwise nothing is send as response and the method returns false. 
   */
  public boolean isInvalidRTCfg(Transport tr, String id) throws Exception {
    Service srv = metaDataAccessor.getServiceForBindingData(id);
    if (srv != null) {
      if (new Integer(-1).equals(srv.getActive())) { //this is an invalid cfg
        if (tr instanceof HTTPTransport) {
          String errStr = "No web service endpoint is available under '" + id + "' path."; 
          LOC.warningT(errStr);
          HTTPTransport httpTrans = (HTTPTransport) tr;
          HttpServletResponse resp = httpTrans.getResponse();
          resp.sendError(HttpServletResponse.SC_NOT_FOUND, errStr);
          return true;
        }
      }
    }
    return false;
  }
  
  private static String generateOverallTextReport(Map<String, AllocationStatisticRecord> stat) {
      List<String> names = new ArrayList<String>(stat.keySet());
      Collections.sort(names);
      int maxLength = 0;
    for (String name : names) {
      if (name.length() > maxLength) {
              maxLength = name.length();
          }
      }
    if ("Name".length() > maxLength) {
          maxLength = "Name".length();
      }
      StringBuilder report = new StringBuilder((4 + stat.size()) * (9 + maxLength + 2 + 7 + 6 * 10 + 3));
      appendStringAndFillUp(report, null, '-', 9 + maxLength + 2 + 7 + 6 * 10);
      report.append("\r\n");
      report.append("|");
      appendStringAndFillUp(report, " Name ", ' ', maxLength + 2);
      report.append("|");
      appendPreFillAndString(report, " Slot ", ' ', 7);
      report.append("|");
      appendPreFillAndString(report, " Allocated Memory ", ' ', 21);
      report.append("|");
      appendPreFillAndString(report, " Freed Memory ", ' ', 21);
      report.append("|");
      appendPreFillAndString(report, " Hold Memory ", ' ', 21);
      report.append("|\r\n");
      appendStringAndFillUp(report, null, '-', 9 + maxLength + 2 + 7 + 6 * 10);
      report.append("\r\n");
    long slots = 0, allocObj = 0, allocMem = 0, freedObj = 0, freedMem = 0, holdObj = 0, holdMem = 0;
      for (String name : names) {
        AllocationStatisticRecord record = stat.get(name);
        slots += record.getSlot();
        allocObj += record.getAllocatedObjects();
        allocMem += record.getAllocatedBytes();
        freedObj += record.getFreedObjects();
        freedMem += record.getFreedBytes();
        holdObj += record.getHoldObjects();
        holdMem += record.getHoldBytes();
      }
      appendStringAndFillUp(report, "Total", ' ', maxLength + 1);
      report.append("|");
      appendPreFillAndString(report, Long.toString(slots), ' ', 6);
      report.append(" |");
      appendPreFillAndString(report, AllocationStatisticRecord.fractionate(allocObj), ' ', 8);
      report.append("O =");
      appendPreFillAndString(report, AllocationStatisticRecord.fractionate(allocMem), ' ', 8);
      report.append("B |");
      appendPreFillAndString(report, AllocationStatisticRecord.fractionate(freedObj), ' ', 8);
      report.append("O =");
      appendPreFillAndString(report, AllocationStatisticRecord.fractionate(freedMem), ' ', 8);
      report.append("B |");
      appendPreFillAndString(report, AllocationStatisticRecord.fractionate(holdObj), ' ', 8);
      report.append("O =");
      appendPreFillAndString(report, AllocationStatisticRecord.fractionate(holdMem), ' ', 8);
      report.append("B |\r\n");
      
      appendStringAndFillUp(report, null, '-', 9 + maxLength + 2 + 7 + 6 * 10);
      report.append("\r\n");
      return report.toString();
  }

  private static void appendStringAndFillUp(StringBuilder report, String string, char character, int completeLength) {
    if (string != null) {
          report.append(string);
      }
    if (string != null) {
          completeLength -= string.length();
      }
    if (completeLength > 0) {
      for (int i = 0; i < completeLength; i++) {
              report.append(character);
          }
      }
  }
  
  private static void appendPreFillAndString(StringBuilder report, String string, char character, int completeLength) {
    if (string != null) {
          completeLength -= string.length();
      }
    if (completeLength > 0) {
      for (int i = 0; i < completeLength; i++) {
              report.append(character);
          }
      }
    if (string != null) {
          report.append(string);
      }
  }
  
  public static String decodeEncodeURI(String reqURI) throws ServerRuntimeProcessException {
    try{
      String decodedURI = URLDecoder.decode(reqURI, "UTF-8");
      /*String encodedURI = URLEncoder.encode(decodedURI, "UTF-8");
      if (encodedURI.indexOf("%2F") > -1){
        return encodedURI.replace("%2F", "/");
      }else{
        return encodedURI.replace("%2f", "/");
      }*/
      // encode 
      final String URL_ENCODING   = "UTF-8";
      final String URI_SEPARATOR  = "/"; 
      StringTokenizer tokenizer = new StringTokenizer(decodedURI, URI_SEPARATOR);
      StringBuffer result = new StringBuffer(decodedURI.length());
      if (decodedURI.charAt(0) == '/'){
        result.append("/");
      }
      String token;
      while (tokenizer.hasMoreTokens()) {
        token = tokenizer.nextToken();
        result.append(URLEncoder.encode(token, URL_ENCODING));
        if (tokenizer.hasMoreElements()) {
          result.append(URI_SEPARATOR);
        }
      }
      return result.toString();
    }catch(UnsupportedEncodingException uee){
      throw new ServerRuntimeProcessException(uee);
    }
  }
  
  static boolean isLocalTransportService(ProviderContextHelper ctx) {
    StaticConfigurationContext sctx = ctx.getStaticContext();
    BindingData bd = (BindingData)sctx.getRTConfiguration();
    return isLocalTransportService(bd);
  }
  
  static boolean isLocalTransportService(BindingData bd){
    QName localCallProperty = new QName(PublicProperties.TRANSPORT_BINDING_FEATURE, PublicProperties.TRANSPORT_BINDING_LOCAL_CALL); 
    PropertyListType[] plta = bd.getPropertyList();
    PropertyType pt = null;
    for (PropertyListType plt : plta){
      pt = plt.getProperty(localCallProperty);
      if (pt != null && "true".equalsIgnoreCase(pt.get_value())){
        return true;
      }
    }
    return false;
  }
  
  private boolean isMeteringEnabled(){
    String prop = System.getProperty(ServiceMeteringConstants.METERING_DISABLED_SYSPROPERTY);
    boolean enabled;
    if (prop != null){
       enabled = !(prop.equals("provider") || prop.equals("both"));
    }else{
      enabled = true;
    }
    return enabled;
  }
}
