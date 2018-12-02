/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase;

import java.util.Map;

import javax.xml.namespace.QName;

import com.sap.engine.interfaces.sca.logtrace.CallEntry;
import com.sap.engine.interfaces.sca.logtrace.ESBTracer;
import com.sap.engine.interfaces.sca.logtrace.CallEntry.Direction;
import com.sap.engine.interfaces.sca.logtrace.CallEntry.Status;
import com.sap.engine.interfaces.sca.logtrace.CallEntry.TraceLevel;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2008, SAP-AG
 * @author Pavel Boev
 *
 */
public class WSLogTrace {
  private static final Location LOCATION = Location.getLocation(WSLogTrace.class);
  private static ESBTracer esbTracer;
  public static final String[] WEBSERVICES_LOCATIONS = new String[] {"com.sap.engine.services.webservices*",
                                                                     "com.sap.engine.services.wsrm*",
                                                                     "com.sap.engine.services.wssec*" };
  
    
  public static String WSDL_OPEARTION_HEADER = "wsdlOperation";
  
  public static String JAVA_OPERATION_HEADER = "javaOperation";
  
  public static String REQUEST_URL_HEADER = "requestURL";
  
  public static String PROXY_HEADER = "proxy";
    
  public static String REQUETED_URL_HEADER = "requestedURL";
     
  
  
  /**
   * Set the instance of CallEntryController so that it is available to code in 
   * webservices_lib. This method is called from webservices_srv
   * @param tracer
   */
  public static void setESBTracer(ESBTracer tracer) {
    esbTracer = tracer;
  }
  
  public static ESBTracer getESBTracer() {
    return esbTracer;
  }
  
  public static String getCallID() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getCallID();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }
  
  public static long getBeginTimestamp() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getBeginTimestamp();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return 0L;
  }

  public static void setBeginTimestamp(long beginTimestamp){
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setBeginTimestamp(beginTimestamp);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static long getEndTimestamp() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getEndTimestamp();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return 0L;
  }

  public static void setEndTimestamp(long endTimestamp) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setEndTimestamp(endTimestamp);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static String getStartTraceID() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getStartTraceID();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  public static void setStartTraceID(String startTraceID) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setStartTraceID(startTraceID);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static String getEndTraceID() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getEndTraceID();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  public static void setEndTraceID(String endTraceID){
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setEndTraceID(endTraceID);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static String getThreadName() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getThreadName();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }
  
  public static Status getCallStatus() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getCallStatus();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return CallEntry.Status.UNDEFINED;
  }

  public static void setCallStatus(Status status){
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setCallStatus(status);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static String getUserName() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getUserName();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }
  
  public static boolean isCorrectTechData() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.isCorrectTechData();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return false;
  }
  
  //General Tracing Data
  public static String getOutboundPayloadTraceID() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getOutboundPayloadTraceID();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  public static void setOutboundPayloadTraceID(String outboundPayloadTraceID) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setOutboundPayloadTraceID(outboundPayloadTraceID);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static String getInboundPayloadTraceID() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getInboundPayloadTraceID();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  public static void setInboundPayloadTraceID(String inboundPayloadTraceID){
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setInboundPayloadTraceID(inboundPayloadTraceID);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
    
  public static Direction getDirection() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getDirection();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return CallEntry.Direction.OUTBOUND;
  }

  public static void setDirection(Direction direction) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setDirection(direction);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static String getConnectivityType() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getConnectivityType();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  public static void setConnectivityType(String connectivityType){
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setConnectivityType(connectivityType);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static String getInterfaceInternalName() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getInterfaceInternalName();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  public static void setInterfaceInternalName(String interfaceInternalName){
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setInterfaceInternalName(interfaceInternalName);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static QName getInterfaceQName() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getInterfaceQName();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  public static void setInterfaceQName(QName interfaceName) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setInterfaceQName(interfaceName);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static String getTransactionID() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getTransactionID();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }
  
  public static String getSemantics() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getSemantics();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  public static void setSemantics(String semantics) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.setSemantics(semantics);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }
  
  public static String getHeader(String key) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getHeader(key);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  public static String setHeader(String key, String value) throws IllegalArgumentException {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.setHeader(key, value);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }
  
  public static Map<String, String> getHeaders() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getHeaders();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }
 
  public static TraceLevel getTraceLevel() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.getTraceLevel();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return CallEntry.TraceLevel.NONE;
  }
  
  public static void suppressErrorTracing(boolean suppress) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        ce.suppressErrorTracing(suppress);
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }

  public static  boolean isErrorTracingSuppressed() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return ce.isErrorTracingSuppressed();
      } else {
        LOCATION.debugT("There is no attached callEntry");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return false;
  }

  /**
   * @param taskName
   * @return
   * @see com.sap.engine.interfaces.sca.logtrace.ESBTracer#attachNewCallEntry(java.lang.String)
   */
  public static CallEntry attachNewCallEntry(String taskName) {
    if (esbTracer != null) {
      return esbTracer.attachNewCallEntry(taskName);
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  /**
   * @param connectivityType
   * @param direction
   * @param interfaceQName
   * @return
   * @see com.sap.engine.interfaces.sca.logtrace.ESBTracer#calculateEffectiveTraceLevelForCurrentUser(java.lang.String, com.sap.engine.interfaces.sca.logtrace.CallEntry.Direction, javax.xml.namespace.QName)
   */
  public static TraceLevel calculateEffectiveTraceLevelForCurrentUser(
      String connectivityType, Direction direction, QName interfaceQName) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        return esbTracer.calculateEffectiveTraceLevelForCurrentUser(
            connectivityType, direction, interfaceQName);
      } else {
        LOCATION.debugT("CallEntry is null. Probably standalone case");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return TraceLevel.NONE;
  }

  /**
   * 
   * @see com.sap.engine.interfaces.sca.logtrace.ESBTracer#disablePassportTraceFlag()
   */
  public static void disablePassportTraceFlag() {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry(); 
      if (ce != null) {
        esbTracer.disablePassportTraceFlag();
      } else {
        LOCATION.debugT("CallEntry is null. Probably standalone case");
      }
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }

  /**
   * @param locations
   * @see com.sap.engine.interfaces.sca.logtrace.ESBTracer#enablePassportTraceFlag(java.lang.String[])
   */
  public static void enablePassportTraceFlag(String[] locations) {
    if (esbTracer != null) {
      CallEntry ce = esbTracer.getAttachedCallEntry();
      if (ce != null) {
        esbTracer.enablePassportTraceFlag(locations);
      } else {
        LOCATION.debugT("CallEntry is null. Probably standalone case");
      } 
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  }

  /**
   * @return
   * @see com.sap.engine.interfaces.sca.logtrace.ESBTracer#getAttachedCallEntry()
   */
  public static CallEntry getAttachedCallEntry() {
    if (esbTracer != null) {
      return esbTracer.getAttachedCallEntry();
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
    return null;
  }

  /**
   * 
   * @see com.sap.engine.interfaces.sca.logtrace.ESBTracer#releaseCallEntry()
   */
  public static void releaseCallEntry() {
    if (esbTracer != null) {
      esbTracer.releaseCallEntry();
    } else {
      LOCATION.debugT("ESBTracer is null. Probably standalone case");
    }
  } 
}
