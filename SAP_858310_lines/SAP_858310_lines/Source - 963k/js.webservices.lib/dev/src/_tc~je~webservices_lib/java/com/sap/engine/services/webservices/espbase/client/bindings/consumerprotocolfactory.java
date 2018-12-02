/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.bindings;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.services.webservices.espbase.client.api.impl.AttachmentProtocolNY;
import com.sap.engine.services.webservices.espbase.client.api.impl.ConsumerLoggingProtocol;
import com.sap.engine.services.webservices.espbase.client.api.impl.IdempotencyProtocolNY;
import com.sap.engine.services.webservices.espbase.client.api.impl.MessageIDProtocolNY;
import com.sap.engine.services.webservices.espbase.client.api.impl.SOAPHeadersProtocolNY;
import com.sap.engine.services.webservices.espbase.client.api.impl.SessionProtocolNY;
import com.sap.engine.services.webservices.espbase.client.wsa.ClientAddressingProtocol;
import com.sap.engine.services.webservices.jaxrpc.handlers.ConsumerJAXRPCHandlersProtocol;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Factory for getting the protocol provider instance. Single protocol
 * provider instance will be used for both 
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class ConsumerProtocolFactory implements ConsumerProtocolProvider {
  private static final Location LOC = Location.getLocation(ConsumerProtocolFactory.class);
  public static ConsumerProtocolFactory protocolFactory = new ConsumerProtocolFactory();
  public static final String DEFAULT_STANDALONE_APP = "ConsumerLogginProtocol MeteringProtocol SOAPHeaderProtocol AttachmentProtocol SessionProtocol PassportProtocol IdempotencyProtocol";
  public static final String DEFAULT_DEPL_SOAP_APP = "ConsumerLogginProtocol MeteringProtocol SOAPHeaderProtocol AttachmentProtocol MessageIdProtocol AddressingProtocol SecurityProtocol SessionProtocol PassportProtocol IdempotencyProtocol";
  //for now put the PassportProtocol as the last protocol in the order, in order to send the DSR passport with the business request. 
  public static final String EXTENDED_DEPL_SOAP_APP = "ConsumerLogginProtocol MeteringProtocol SOAPHeaderProtocol AttachmentProtocol Sequencing WS-RM AddressingProtocol SecurityProtocol SessionProtocol PassportProtocol IdempotencyProtocol";

  private Hashtable protocolHash;
  
  public ConsumerProtocolFactory() {
    protocolHash = new Hashtable();

    // TODO: Set (Protocol Name - Implementation) pairs here
    try {
      ConsumerJAXRPCHandlersProtocol consumerJAXRPCHandlersProtocol = new ConsumerJAXRPCHandlersProtocol();
      protocolHash.put(consumerJAXRPCHandlersProtocol.getProtocolName(),consumerJAXRPCHandlersProtocol);
    } catch (Throwable x) {
      if (LOC != null) {
        LOC.traceThrowableT(Severity.DEBUG, "<JAX-RPC> consumer protocol was not loaded. It could not be used from WS clients.", x);
      }
    }
    
    try {
      SOAPHeadersProtocolNY soapHeadersProtocol = new SOAPHeadersProtocolNY();
      protocolHash.put(soapHeadersProtocol.getProtocolName(),soapHeadersProtocol);
    } catch (Throwable x) {
      if (LOC != null) {
        LOC.traceThrowableT(Severity.DEBUG, "<SOAP Headers> consumer protocol was not loaded. It could not be used from WS clients.", x);
      }
    }
    
    try {
      AttachmentProtocolNY attachmentProtocol = new AttachmentProtocolNY();
      protocolHash.put(attachmentProtocol.getProtocolName(),attachmentProtocol);
    } catch (Throwable x) {
      if (LOC != null) {
        LOC.traceThrowableT(Severity.DEBUG, "<SOAP Headers> consumer protocol was not loaded. It could not be used from WS clients.", x);
      }
    }

    try {
      ClientAddressingProtocol wsa = new ClientAddressingProtocol();
      protocolHash.put(wsa.getProtocolName(), wsa);
    } catch (Throwable x) {
      if (LOC != null) {
        LOC.traceThrowableT(Severity.DEBUG, "<Client Adressing> consumer protocol was not loaded. It could not be used from WS clients.", x);
      }
    }
    
    try {
      SessionProtocolNY sessionProtocol = new SessionProtocolNY();
      protocolHash.put(sessionProtocol.getProtocolName(), sessionProtocol);
    } catch (Throwable x) {
      if (LOC != null) {
        LOC.traceThrowableT(Severity.DEBUG, "<Session> consumer protocol was not loaded. It could not be used from WS clients.", x);
      }
    }
    
    try {
      IdempotencyProtocolNY idempotencyProtocol = new IdempotencyProtocolNY();
      protocolHash.put(idempotencyProtocol.getProtocolName(), idempotencyProtocol);
    } catch (Throwable x) {
      if (LOC != null) {
        LOC.traceThrowableT(Severity.DEBUG, "<Idempotency> consumer protocol was not loaded. It could not be used from WS clients.", x);
      }
    }
    try {
      MessageIDProtocolNY messageIdProtocol = new MessageIDProtocolNY();
      protocolHash.put(messageIdProtocol.getProtocolName(), messageIdProtocol);
    } catch (Throwable x) {
      if (LOC != null) {
        LOC.traceThrowableT(Severity.DEBUG, "<Message ID> consumer protocol was not loaded. It could not be used from WS clients.", x);
      }
    }    
    
    try {
      ConsumerLoggingProtocol consumerLogginProtocol = new ConsumerLoggingProtocol();
      protocolHash.put(consumerLogginProtocol.getProtocolName(), consumerLogginProtocol);
    } catch (Throwable x) {
      if (LOC != null) {
        LOC.traceThrowableT(Severity.DEBUG, "<Message ID> consumer protocol was not loaded. It could not be used from WS clients.", x);
      }
    }    
    
    
    //HybernationProtocol hybProtocol = new HybernationProtocol();
    //protocolHash.put(hybProtocol.getProtocolName(), hybProtocol);
  } 
  
  /**
   * Returns the default protocol factory.
   * @param protocolList
   * @return
   */
  public ConsumerProtocol[] getProtocols(String protocolList) {
    if (protocolList == null || protocolList.length() == 0) {
      return new ConsumerProtocol[0];    
    }
    String[] protocolNames = protocolList.split("\\s+");
    List<ConsumerProtocol> pList = new ArrayList();
//    ConsumerProtocol[] consumerProtocols = new ConsumerProtocol[protocolNames.length];
    for (int i=0; i<protocolNames.length; i++) {
      ConsumerProtocol protocol = (ConsumerProtocol) protocolHash.get(protocolNames[i]);
      if (protocol == null) {
        if ("PassportProtocol".equals(protocolNames[i])) { //the PassportProtocol could be missing when the DSR service is not started.
          continue;
        }else if ("MeteringProtocol".equals(protocolNames[i])){
          continue;
        }
        throw new IllegalArgumentException("Implementation for Consumer Protocol with name ["
        + protocolNames[i]+"] is not registered in the client web services runtime. This protocol is required for normal web services client execution.");
      }
      pList.add(protocol);
    }        
    return pList.toArray(new ConsumerProtocol[pList.size()]);
  }
  
  

  /**
   * Registers consumer protocol in the protocol factory.
   * @param protocolName
   * @param protocol
   */
  public void registerProtocol(String protocolName, ConsumerProtocol protocol) {
    this.protocolHash.put(protocolName, protocol);
  }

  /**
   * Unregisters consumer protocol in the protocol factory.
   * @param protocolName
   * @return
   */
  public boolean unregisterProtocol(String protocolName) {
    ConsumerProtocol protocol = (ConsumerProtocol) this.protocolHash.get(protocolName);
    if (protocol != null) {
      this.protocolHash.remove(protocolName);
      return true;
    }
    return false;
  }
}
