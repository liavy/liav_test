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

import com.sap.engine.interfaces.sca.logtrace.CallEntry;
import com.sap.engine.interfaces.sca.logtrace.ESBTracer;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Hibernation;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.BaseProtocolException;
import com.sap.engine.services.webservices.espbase.server.runtime.exceptions.RTResourceAccessor;
import com.sap.tc.logging.Location;
import com.sap.tools.memory.trace.AllocationStatisticRegistry;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-20
 */
public class ProtocolProcessor {
  //Further processing should stop.
  static final int STOP = ProviderProtocol.STOP;
  //Further processing should continue.
  static final int CONTINUE = ProviderProtocol.CONTINUE;
  //Further processing should stop and the message to be send. Applicable only for handlRequest().
  static final int BACK = ProviderProtocol.BACK;
  //Further processing should continue and no response message should be send at this point.
  static final int BACK_AND_CONTINUE_PROCESSING = ProviderProtocol.BACK_AND_CONTINUE_PROCESSING;
  //under this value the protocol number is saved
  static final String P_POINTER  =  "protocol_pointer";
  
  private static final Location LOC = Location.getLocation(ProtocolProcessor.class);
  
  /**
   * Returns ProtocolProcessor.CONTINUE if further processing should continue.
   * Returns ProtocolProcessor.BACK if further processing should stop and the message to be send.
   * Returns ProtocolProcessor.STOP if further processing should stop immediately.
   */
  static int protocolsHandleRequest(ProviderContextHelperImpl ctx) throws com.sap.engine.interfaces.webservices.runtime.ProtocolException, RuntimeProcessException  {
    int retCode;
    //resore count
    ConfigurationContext subC = ctx.getPersistableContext().getSubContext(RuntimeProcessingEnvironment.PERSISTENT_RUNTIME_SUBCONTEXT);
    ProviderProtocol[] protocols = ctx.getProtocols();
    int count = restorePPointer(subC);
    //increment, because -1 is return if none is persisted, and last invoked protocol is stored.
    count++;
    if (LOC.beDebug()){
      LOC.debugT("protocolsHandleRequest() protocol counter '" + count + "' protocols number '" + protocols.length + "'");
    }
    for (; count < protocols.length; count++) {
      subC.setProperty(P_POINTER, Integer.toString(count));
      //invoke protocol method
      retCode = protocolsHandleRequest0(ctx, protocols, count);
      if (retCode != ProtocolProcessor.CONTINUE) {
        return retCode;
      }
    }
    return ProtocolProcessor.CONTINUE;
  }
  /**
   * Invokes handleRequest() on protocol with index <code>pNom</code> in protocols list. Handles the processing in case of different codes
   * returned from handleRequest() method.
   * 
   * Returns ProtocolProcessor.CONTINUE if further processing should continue.
   * Returns ProtocolProcessor.BACK if further processing should stop and the message to be send.
   * Returns ProtocolProcessor.STOP if further processing should stop immediately.
   * Returns ProtocolProcessor.STOP if further processing should stop immediately.
   */
  static int protocolsHandleRequest0(ProviderContextHelperImpl ctx, ProviderProtocol[] protocols, int pNom) throws com.sap.engine.interfaces.webservices.runtime.ProtocolException, RuntimeProcessException {
    try {
      String protocolInstance = protocols[pNom].getProtocolName(); // **
      if (LOC.beDebug()){
        LOC.debugT("protocolsHandleRequest0() protocol instance " +  protocolInstance);
      }
      int retCode = -1;
      AllocationStatisticRegistry.pushThreadTag(protocolInstance + ".handleRequest()", true); //$JL-PERFORMANCE$
     try{
        retCode = protocols[pNom].handleRequest(ctx);
      }finally{
        AllocationStatisticRegistry.popThreadTag();
      }
      if (LOC.beDebug()){
        LOC.debugT("protocolsHandleRequest0() protocol " +  protocols[pNom] + ", returnCode: " + retCode);
      }
      if (retCode == ProviderProtocol.BACK) {
        if (protocolsHandleResponseInternal(ctx, pNom) == STOP) {
          return ProtocolProcessor.STOP;            
        } else {
          return ProtocolProcessor.BACK;
        }
      } else if (retCode == ProviderProtocol.BACK_AND_CONTINUE_PROCESSING) {
        Message origMsg = ctx.getMessage();
        if (protocolsHandleResponseInternal(ctx, pNom) == STOP) {
          return ProtocolProcessor.STOP;            
        }
        ctx.setMessage(origMsg);
        return ProtocolProcessor.BACK_AND_CONTINUE_PROCESSING;
      } else if (retCode == ProviderProtocol.SKIP_NEXT) {
        return ProtocolProcessor.CONTINUE;          
      } else if (retCode == ProviderProtocol.STOP) {
        return ProtocolProcessor.STOP;
      } else if (retCode == ProviderProtocol.CONTINUE) {
        //continue processing
      } else { //unknown code
        throw new BaseProtocolException(RTResourceAccessor.getResourceAccessor(), RuntimeExceptionConstants.INVALID_PROTOCOL_RETURNCODE, 
                                        new Object[]{protocols[pNom].getProtocolName(), new Integer(retCode)});
      }
    } catch (com.sap.engine.interfaces.webservices.runtime.MessageException mE) {
      LOC.catching(mE);
      ctx.setMessageException(mE);
      return invokeFault(ctx, pNom);
    }
    return ProtocolProcessor.CONTINUE;
  }
  /**
   * Invokes handleResponse() method of all the protocols in
   * the chain
   * Returns ProviderProtocol.CONTINUE if further processing should continue.
   * Returns ProviderProtocol.STOP if further processing should stop immediately.
   */
  static int protocolsHandleResponse(ProviderContextHelperImpl context) throws com.sap.engine.interfaces.webservices.runtime.ProtocolException {
    return protocolsHandleResponseInternal(context, context.getProtocols().length - 1);
  }
  
  /**
   * Invokes afterDeserialization() method of all ProtocolExtensions instances.
   * Returns ProtocolProcessor.BACK if further processing should stop and the message to be send.
   * Returns ProtocolProcessor.STOP if further processing should stop immediately.
   * Returns ProtocolProcessor.CONTINUE if further processing should countinue.
   */
  static int protocolsAfterDeserialization(ProviderContextHelperImpl ctx) throws com.sap.engine.interfaces.webservices.runtime.ProtocolException {
    ProviderProtocol protocols[] = ctx.getProtocols();
    int count;
    int retCode;
    for (count = 0; count < protocols.length; count++) {
      try {
        if (protocols[count] instanceof ProtocolExtensions) {
          String protocolInstance = protocols[count].toString();
          if (LOC.beDebug()){
            LOC.debugT("protocolsAfterDeserialization() protocol instance " + protocolInstance);
          }
          AllocationStatisticRegistry.pushThreadTag(protocolInstance + ".afterDeserializationt()", true);
          try{
            retCode = ((ProtocolExtensions) protocols[count]).afterDeserialization(ctx);
          }finally{
            AllocationStatisticRegistry.popThreadTag();
          }
          if (LOC.beDebug()){
            LOC.debugT("protocolsAfterDeserialization() protocol " + protocols[count] + ", returnCode: " + retCode);
          }
          if (retCode == ProviderProtocol.BACK) {//invoke handleResponse() of all protocols
            if (protocolsHandleResponseInternal(ctx, protocols.length - 1) == ProviderProtocol.STOP) {
              return ProtocolProcessor.STOP;
            } else {
              return ProtocolProcessor.BACK;
            }
          } else if (retCode == ProviderProtocol.STOP) {
            return ProtocolProcessor.STOP;
          } else if (retCode == ProviderProtocol.CONTINUE) {
            //continue processing
          } else { //unknown code
            throw new BaseProtocolException(RTResourceAccessor.getResourceAccessor(), RuntimeExceptionConstants.INVALID_PROTOCOL_RETURNCODE, 
                                            new Object[]{protocols[count].getProtocolName(), new Integer(retCode)});
          }
        }
      } catch (com.sap.engine.interfaces.webservices.runtime.MessageException mE) {
        LOC.catching(mE);
        ctx.setMessageException(mE);
        return invokeFault(ctx, count);
      }
    }    
    return ProtocolProcessor.CONTINUE;
  }
  
  static void protocolsBeforeSerialization(ProviderContextHelperImpl ctx) throws ProtocolException {
    ProviderProtocol protocols[] = ctx.getProtocols();
    int count;
    for (count = 0; count < protocols.length; count++) {
      if (protocols[count] instanceof ProtocolExtensions) {
        String protocolInstance = protocols[count].toString();
        if (LOC.beDebug()){
          LOC.debugT("protocolsBeforeSerialization() protocol instance " + protocolInstance);
        }
        AllocationStatisticRegistry.pushThreadTag(protocolInstance + ".beforeSerialization()", true);
        try{
          ((ProtocolExtensions) protocols[count]).beforeSerialization(ctx);
        }finally{
          AllocationStatisticRegistry.popThreadTag();
        }
      }
    }    
  }
  
  static void protocolsBeforeHibernation(ProviderContextHelperImpl ctx) throws RuntimeProcessException {
    ProviderProtocol protocols[] = ctx.getProtocols();
    int count;
    for (count = 0; count < protocols.length; count++) {
      if (protocols[count] instanceof Hibernation) {
        try {
          ((Hibernation) protocols[count]).beforeHibernation(ctx);
        } catch (ProtocolException e) {
          throw new RuntimeProcessException(e);
        }
        if (LOC.beDebug()){
          LOC.debugT("protocolsBeforeHibernation() protocol instance " + protocols[count]);
        }
      }
    }    
  }

  static void protocolsFinishMessageDeserialization(ProviderContextHelperImpl ctx) throws RuntimeProcessException {
    ProviderProtocol protocols[] = ctx.getProtocols();
    int count;
    for (count = 0; count < protocols.length; count++) {
      if (protocols[count] instanceof Hibernation) {
        try {
          ((Hibernation) protocols[count]).finishMessageDeserialization(ctx);
        } catch (ProtocolException e) {
          throw new RuntimeProcessException(e);
        }
        if (LOC.beDebug()){
          LOC.debugT("protocolsFinishMessageDeserialization() protocol instance " + protocols[count]);
        }
      }
    }    
  }
  
  static void protocolsFinishHibernation(ProviderContextHelperImpl ctx) throws RuntimeProcessException {
    ProviderProtocol protocols[] = ctx.getProtocols();
    int count;
    for (count = 0; count < protocols.length; count++) {
      if (protocols[count] instanceof Hibernation) {
        try {
          ((Hibernation) protocols[count]).finishHibernation(ctx);
        } catch (ProtocolException e) {
          throw new RuntimeProcessException(e);
        }
        if (LOC.beDebug()){
          LOC.debugT("protocolsFinishHibernation() protocol instance " + protocols[count]);
        }
      }
    }    
  }
  
  static void protocolsAfterHibernation(ProviderContextHelperImpl ctx) throws RuntimeProcessException {
    ProviderProtocol protocols[] = ctx.getProtocols();
    int count;
    for (count = 0; count < protocols.length; count++) {
      if (protocols[count] instanceof Hibernation) {
        try {
          ((Hibernation) protocols[count]).afterHibernation(ctx);
        } catch (ProtocolException e) {
          throw new RuntimeProcessException(e);
        }
        if (LOC.beDebug()){
          LOC.debugT("protocolsAfterHibernation() protocol instance " + protocols[count]);
        }
      }
    }    
  }

  /**
   * Returns ProviderProtocol.CONTINUE if further processing should continue.
   * Returns ProviderProtocol.STOP if further processing should stop immediately.
   */
  private static int protocolsHandleResponseInternal(ProviderContextHelperImpl ctx, int startIndex) throws com.sap.engine.interfaces.webservices.runtime.ProtocolException {
    ProviderProtocol protocols[] = ctx.getProtocols();
    int retCode;
    for (; startIndex >= 0; startIndex--) {
      String protocolInstance = protocols[startIndex].getProtocolName(); // **
      if (LOC.beDebug()){
        LOC.debugT("protocolsHandleResponseInternal() protocol instance " + protocolInstance);
      }
      AllocationStatisticRegistry.pushThreadTag(protocolInstance + ".handleResponse()", true);
      try{
        retCode = protocols[startIndex].handleResponse(ctx);
      }finally{
        AllocationStatisticRegistry.popThreadTag();
      }
      if (LOC.beDebug()){
        LOC.debugT("protocolsHandleResponseInternal() protocol " + protocols[startIndex] + ", returnCode: " + retCode);
      }
      if (retCode == ProviderProtocol.CONTINUE) {
        //continue
      } else if (retCode == ProviderProtocol.STOP) {
        return ProtocolProcessor.STOP;
      } else if (retCode == ProviderProtocol.SKIP_NEXT) {
        return ProtocolProcessor.CONTINUE;
      } else { //unknown code
        throw new BaseProtocolException(RTResourceAccessor.getResourceAccessor(), RuntimeExceptionConstants.INVALID_PROTOCOL_RETURNCODE, 
                                        new Object[]{protocols[startIndex].getProtocolName(), new Integer(retCode)});
      }
    }    
    return ProtocolProcessor.CONTINUE;
  }
  
  /**
   * Invokes handleFault() methods of protocol, going backwards starting from startIndex param.
   * Returns ProtocolProcessor.STOP if further processing should stop immediately.
   * Returns ProtocolProcessor.BACK in case all the handleFault() methods have been invoked. This value is returned directly to the 
   * runtime, that's why it is ProtocolProcessor.BACK. 
   */ 
  private static int invokeFault(ProviderContextHelperImpl ctx, int startIndex) throws com.sap.engine.interfaces.webservices.runtime.ProtocolException {
    ProviderProtocol protocols[] = ctx.getProtocols();
    int retCode;
    for (; startIndex >= 0; startIndex--) {
      if (LOC.beDebug()){
        LOC.debugT("invokeFault protocol instance " + protocols[startIndex]);
      }
      retCode = protocols[startIndex].handleFault(ctx);
      if (LOC.beDebug()){
        LOC.debugT("invokeFault protocol instance " + protocols[startIndex] + ", returnCode: " + retCode);
      }
      if (retCode == ProviderProtocol.CONTINUE) {
        //continue
      } else if (retCode == ProviderProtocol.STOP) {
        return ProtocolProcessor.STOP;
      } else if (retCode == ProviderProtocol.SKIP_NEXT) {
        return ProtocolProcessor.BACK;
      } else { //unknown code
        throw new BaseProtocolException(RTResourceAccessor.getResourceAccessor(), RuntimeExceptionConstants.INVALID_PROTOCOL_RETURNCODE, 
                                        new Object[]{protocols[startIndex].getProtocolName(), new Integer(retCode)});
      }
    }
    return ProtocolProcessor.BACK;
  }
      
  private static int restorePPointer(ConfigurationContext ctx) {
    //resore count
    if (ctx == null) {
      return -1;
    }
    
    String pp = (String) ctx.getProperty(P_POINTER);
    if (pp == null) {
      return -1;
    } else {
      return Integer.parseInt(pp);
    }
  }
  
}
