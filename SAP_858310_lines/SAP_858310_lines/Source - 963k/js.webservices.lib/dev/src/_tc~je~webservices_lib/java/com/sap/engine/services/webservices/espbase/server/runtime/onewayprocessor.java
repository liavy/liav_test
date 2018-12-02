/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 */
package com.sap.engine.services.webservices.espbase.server.runtime;

import javax.xml.namespace.QName;

import com.sap.engine.interfaces.sca.logtrace.CallEntry;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.server.StaticConfigurationContext;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LogRecord;
import com.sap.tc.logging.Severity;

/**
 * Instances of this class perform one-way operation procesing.
 * One instance can perform only one processing at same time.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class OneWayProcessor implements Runnable {

  //RuntimeProcessingEnvironment which is used for processing
  static RuntimeProcessingEnvironment rtProcessor;
  
  private static final Location LOC = Location.getLocation(OneWayProcessor.class);
  
//  //The context of the current call
  private ProviderContextHelperImpl context;
  private boolean isOneWay;
  
  void process(ProviderContextHelperImpl context, boolean isOneWay) {
    this.context = context;
    this.isOneWay = isOneWay;
    //starting separate thread for processing
    rtProcessor.applicationSrvContext.getCoreContext().getThreadSystem().startThread(this, false, true);
  }

  public void run() {
    try {
      String taskName = "One-way processing second phase";
      StaticConfigurationContext staticCtx = context.getStaticContext();
      if (staticCtx != null) {
        taskName = "one-way processing at " + staticCtx.getEndpointRequestURI(); 
      }
      WSLogTrace.attachNewCallEntry(taskName);
      WSLogTrace.setDirection(CallEntry.Direction.INBOUND);
      WSLogTrace.setConnectivityType("WS");
      Variant v = (Variant) staticCtx.getProperty(StaticConfigurationContextImpl.VARIANT);
      InterfaceData intfData = v.getInterfaceData();
      WSLogTrace.setInterfaceQName(new QName(intfData.getNamespace(), intfData.getName()));
      WSLogTrace.setSemantics("one-way-backend");
      WSLogTrace.setHeader(CallEntry.HEADER_PROCESSING_STEPS, "invokeEndpoint");
      //assume success
      WSLogTrace.setCallStatus(CallEntry.Status.SUCCESS);
      if (LOC.beDebug()) {
        LogRecord startLogRec = LOC.debugT("Start WS Processing in one-way scenario");
        WSLogTrace.setStartTraceID(startLogRec.getId().toString());
      }
      
      LOC.debugT("run(): OneWayProcessor's 'run()' method has been invoked by thread '" + Thread.currentThread().getName() + "'");     
      
      
      rtProcessor.process0(this.context);
//      //attaching context as thread local
//      ApplicationWebServiceContextImpl.getSingleton().attachRuntimeContext(context);
//      //get operation
//      OperationMapping operationMapping = context.getOperation();
//      //obtaining the implcontainer from instance Manager.
//      LOC.debugT("Get implementation container...");      
//      ImplementationContainer implContainer = rtProcessor.implContainerAccessor.getImplementationContainer(context.getImplementationLink().getImplementationContainerID());
//      //obtaing the impl loader
//      LOC.debugT("Get applicaiton loader..." + implContainer);      
//      ClassLoader implLoader = implContainer.getImplementationLoader(context);
//      context.setImplClassLoader(implLoader);
//      //creating the method parameter classes
//      LOC.debugT("Get parameterClasses..." + implLoader);      
//      Class[] paramClasses = rtProcessor.loadParameterClasses(operationMapping, implLoader);
//      context.setParameterClasses(paramClasses);
//      //creating objects from message
//      LOC.debugT("Get parameters...");      
//      Object[] paramObject = rtProcessor.getTransportBinding(context).getParameters(paramClasses, implLoader, context);
//      context.setParameterObjects(paramObject);
//      //invoke protocols chain afterDeserialization
//      LOC.debugT("Invoke protocols afterDeserialization...");      
//      int protocolsCode = ProtocolProcessor.protocolsAfterDeserialization(context);
//      if (protocolsCode == ProtocolProcessor.BACK || protocolsCode == ProtocolProcessor.STOP) { //check the status returned by ProtocolProcessor
//        return;
//      }
//      //invoking the business method
//      LOC.debugT("Invoke business method...");      
//      Object result = null;
//      InvocationTargetException invE = null;
//      try {
//        result = implContainer.invokeMethod(operationMapping.getJavaMethodName(), paramClasses, paramObject, context);
//        LOC.debugT("Invoke business method - normal termination.");      
//      } catch (InvocationTargetException e) {
//        invE = e;
//        LOC.debugT("Invoke business method finished with exception: " + e.getCause());      
//      }
    } catch (Exception e) {
      WSLogTrace.setCallStatus(CallEntry.Status.ERROR);
      LogRecord errorRec = LOC.traceThrowableT(Severity.ERROR, "run(): exception occurred", e);
      if (errorRec != null) {
        if (WSLogTrace.getStartTraceID() == null) {
          //this means that the default severity is higher than DEBUG.
          WSLogTrace.setStartTraceID(errorRec.getId().toString());
          WSLogTrace.setEndTraceID(errorRec.getId().toString());
        }
      }
      //ExceptionManager.logThrowable(Severity.WARNING, LoggingHelper.SYS_SERVER, LOC, "run()", "exception occurred", e);
      if (! isOneWay) {
        try {
          context.getTransportBinding().sendServerError(e, this.context);
        } catch (Exception ee) {
          LogRecord sendErrorRec = LOC.traceThrowableT(Severity.ERROR, "exception during sending error message", ee);
          if (sendErrorRec != null) {
            WSLogTrace.setEndTraceID(sendErrorRec.getId().toString());
            }
          }
         // LOC.catching("exception during sending error message", ee);
        }
    } finally {    
      if(LOC.beDebug()){
        LogRecord lastRec = LOC.debugT("End WS processing in one-way scenario");
        WSLogTrace.setEndTraceID(lastRec.getId().toString());
      }      
      WSLogTrace.releaseCallEntry();
      rtProcessor.reuseContext(this.context);
      this.context = null;
    }
  }
}
