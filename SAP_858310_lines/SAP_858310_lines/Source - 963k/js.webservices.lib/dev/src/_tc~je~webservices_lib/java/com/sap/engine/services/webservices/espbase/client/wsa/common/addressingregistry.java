/*
 * Copyright (c) 2005 by SAP Labs Bulgaria.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.espbase.client.wsa.common;

import java.util.Hashtable;

/**
 * Addressing Registry
 * @author       Vladimir Videlov (vladimir.videlov@sap.com)
 * @version      1.0
 */
public class AddressingRegistry {
  static Hashtable classTypes200408;
  static Hashtable classTypes200508;

  /**
   * Initialize the registry object.
   */
  static {
    // name to class mappings
    classTypes200408 = new Hashtable();

    classTypes200408.put("EndpointReference", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200408.EndpointReferenceType.class);
    classTypes200408.put("MessageID", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200408.AttributedURI.class);
    classTypes200408.put("RelatesTo", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200408.Relationship.class);
    classTypes200408.put("To", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200408.AttributedURI.class);
    classTypes200408.put("Action", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200408.AttributedURI.class);
    classTypes200408.put("From", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200408.EndpointReferenceType.class);
    classTypes200408.put("ReplyTo", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200408.EndpointReferenceType.class);
    classTypes200408.put("FaultTo", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200408.EndpointReferenceType.class);

    // name to class mappings
    classTypes200508 = new Hashtable();

    classTypes200508.put("EndpointReference", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.EndpointReferenceType.class);
    classTypes200508.put("MessageID", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.AttributedURIType.class);
    classTypes200508.put("RelatesTo", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.RelatesToType.class);
    classTypes200508.put("To", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.AttributedURIType.class);
    classTypes200508.put("Action", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.AttributedURIType.class);
    classTypes200508.put("From", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.EndpointReferenceType.class);
    classTypes200508.put("ReplyTo", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.EndpointReferenceType.class);
    classTypes200508.put("FaultTo", com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.EndpointReferenceType.class);
  }

  /**
   * Lookups for a clas by name.
   * @param name Name of the element
   * @param wsaVersionNS WS-Addressing version NS
   * @return Class for the supplied name
   */
  public static Class lookupClassType(String name, String wsaVersionNS) {
    if (wsaVersionNS.equals(AddressingConstants.NS_WSA_200408)) {
      return (Class) classTypes200408.get(name);
    } else if (wsaVersionNS.equals(AddressingConstants.NS_WSA_200508)) {
      return (Class) classTypes200508.get(name);
    } else {
      return null;
    }
  }
}