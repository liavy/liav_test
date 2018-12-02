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

import com.sap.engine.services.webservices.espbase.server.additions.wsa.ProviderAddressingProtocol;

/**
 * Addressing constants
 * @author       Vladimir Videlov (vladimir.videlov@sap.com)
 * @version      1.0
 */
public class AddressingConstants {
  // JNDI names
  public static final String CTX_BASE  =  ProviderAddressingProtocol.PROTOCOL_NAME + "Context";
  public static final String CTX_REQUEST = "request-context";
  public static final String CTX_RESPONSE = "response-context";

  // The WS-Addressing namespace
  public static final String PREFIX_WSA  =  "wsa";
  public static final String PREFIX_GUID = "urn:uuid:";

  public static final String NS_WSA_200408  =  "http://schemas.xmlsoap.org/ws/2004/08/addressing";
  public static final String NS_WSA_200508  =  "http://www.w3.org/2005/08/addressing";
  public static final String NS_WSA_FEATURE = "http://www.sap.com/710/soap/features/WSAddressing/";

  public static final String PREFIX_RM_ANON_URI = "http://docs.oasis-open.org/ws-rx/wsrm/200608/anonymous?id=";

  public static final String URI_200408_ANONYMOUS_ENDPOINT = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";
  public static final String URI_200408_UNSPECIFIED_MSGID = "http://schemas.xmlsoap.org/ws/2004/08/addressing/id/unspecified";

  public static final String URI_200508_ANONYMOUS_ENDPOINT = "http://www.w3.org/2005/08/addressing/anonymous";
  public static final String URI_200508_NONE_ENDPOINT = "http://www.w3.org/2005/08/addressing/none";
  public static final String URI_200508_UNSPECIFIED_MSGID = "http://www.w3.org/2005/08/addressing/unspecified";

  public static final String ACTION_FAULT_200408 = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";
  public static final String ACTION_FAULT_200508 = "http://www.w3.org/2005/08/addressing/soap/fault";
  public static final String ACTION_FAULT_WSA_200508 = "http://www.w3.org/2005/08/addressing/fault";

  // WS-Addressing configuration properties
  public static final String CONF_WSA_ENABLED = "enabled";
  public static final String CONF_WSA_PROTOCOL = "WSAProtocol";

  public static final String CONF_WSA_INPUT_ACTION = "InputAction";
  public static final String CONF_WSA_OUTPUT_ACTION = "OutputAction";
  public static final String CONF_WSA_FAULT_ACTION = "FaultAction";

  // WS-Addressing element names
  public static final String WSA_MESSAGE_ID = "MessageID";
  public static final String WSA_RELATES_TO = "RelatesTo";
  public static final String WSA_RELATIONSHIP_TYPE = "RelationshipType";
  public static final String WSA_FROM = "From";
  public static final String WSA_TO = "To";
  public static final String WSA_ACTION = "Action";
  public static final String WSA_REPLY_TO = "ReplyTo";
  public static final String WSA_FAULT_TO = "FaultTo";
  public static final String WSA_IS_REF_PARAM = "IsReferenceParameter";
  public static final String WSA_ADDRESS = "Address";

  public static final String WSA_RELATIONSHIP_TYPE_200408_REPLY = PREFIX_WSA + ":Reply";
  public static final String WSA_RELATIONSHIP_TYPE_200508_REPLY = "http://www.w3.org/2005/08/addressing/reply";

  // SOAP additional elements/attributes
  public static final String CONST_MUST_UNDERSTAND = "mustUnderstand";
  public static final String CONST_TRUE = "true";
  public static final String CONST_FALSE = "false";
  
  // Deprecated constants AP7 FRMW
  /**
   * @deprecated
   */
  public static final String NS_WSA  =  "http://schemas.xmlsoap.org/ws/2004/08/addressing";

  /**
   * @deprecated
   */  
  public static final String URI_ANONYMOUS_ENDPOINT = "http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous";  
  
  /**
   * @deprecated
   */  
  public static final String URI_UNSPECIFIED_MSGID = "http://schemas.xmlsoap.org/ws/2004/08/addressing/id/unspecified";  
  
  /**
   * @deprecated
   */  
  public static final String ACTION_FAULT = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";  
  
  /**
   * @deprecated
   */  
  public static final String WSA_RELATIONSHIP_TYPE_RESPONSE = PREFIX_WSA + ":Reply";  
}
