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
package com.sap.engine.services.webservices.espbase.wsdl.wsdl11;

import com.sap.engine.lib.xml.util.NS;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-22
 */
public class WSDL11Constants {
  /**
   * Property relevant for Parameters only.
   * Under this property the wsdl:message@name attrib of declaring message is bound. 
   * The value is string serialization of javax.xml.namespace.QName object.
   * Could be loaded using QName.valueOf(); 
   */
  public static final String MESSAGE_QNAME  =  "message-qname";
  /**
   * Property relevant for Operations only.
   * Under this property the wsdl:message@name attrib of input message is bound. 
   * The value is string serialization of javax.xml.namespace.QName object.
   * Could be loaded using QName.valueOf(); 
   */
  public static final String OPERATION_IN_MESSAGE_QNAME  =  "in-message-qname";
  /**
   * Property relevant for Operations only.
   * Under this property the wsdl:message@name attrib of output message is bound. 
   * The value is string serialization of javax.xml.namespace.QName object.
   * Could be loaded using QName.valueOf(); 
   */
  public static final String OPERATION_OUT_MESSAGE_QNAME  =  "out-message-qname";
  /**
   * Property relevant for Operation's fault ExtensionContext only.
   * Under this property the wsdl:message@name attrib of fault message is bound. 
   * The value is string serialization of javax.xml.namespace.QName object.
   * Could be loaded using QName.valueOf(); 
   */
  public static final String FAULT_MESSAGE_QNAME  =  "fault-message-qname";
  /**
   * Property relevant for fault parameter only.
   * Under this property the wsdl:fault@name attrib value is bound. 
   */
  public static final String FAULT_NAME  =  "fault-name";  
  
  //WSDL1.1 element and attributes
  public static final String NAME_ATTR  =  "name";
  public static final String TYPE_ATTR  =  "type";
  public static final String ELEMENT_ATTR  =  "element";
  public static final String TARGETNAMESPACE_ATTR  =  "targetNamespace";
  public static final String TRANSPORT_ATTR = "transport";
  public static final String STYLE_ATTR = "style";
  public static final String SOAPACTION_ATTR = "soapAction";
  public static final String USE_ATTR = "use";
  public static final String ENCODEDSTYLE_ATTR = "encodingStyle";
  public static final String NAMESPACE_ATTR = "namespace";
  public static final String PARTS_ATTR = "parts";
  public static final String LOCATION_ATTR = "location";
  public static final String PARAMETER_ORDER_ATTR = "parameterOrder";
  public static final String IMPORT_ELEMENT = "import";
  public static final String REDEFINE_ELEMENT = "redefine";
  public static final String TYPES_ELEMENT = "types";
  public static final String ADDRESS_ELEMENT = "address";
  public static final String PART_ELEMENT  =  "part";
  public static final String MESSAGE_ELEMENT  =  "message";
  public static final String DEFINITIONS_ELEMENT  =  "definitions";
  public static final String PORTTYPE_ELEMENT  =  "portType";
  public static final String OPERATION_ELEMENT  =  "operation";
  public static final String INPUT_ELEMENT = "input";
  public static final String OUTPUT_ELEMENT = "output";
  public static final String FAULT_ELEMENT = "fault";
  public static final String BINDING_ELEMENT = "binding";
  public static final String SOAPBODY_ELEMENT = "body";
  public static final String SOAPHEADER_ELEMENT = "header";
  public static final String SERVICE_ELEMENT = "service";
  public static final String PORT_ELEMENT = "port";
  public static final String DOCUMENTATION_ELEMENT = "documentation";
  public static final String VERB_ATTR = "verb";
  public static final String URLENCODED_ELEMENT = "urlEncoded";
  public static final String CONTENT_ELEMENT = "content";
  public static final String MIMEXML_ELEMENT = "mimeXml";
  public static final String MULTIPART_RELATED_ELEMENT = "multipartRelated";
  public static final String SCHEMA_LOCATION_ATTR = "schemaLocation";
  public static final String SCHEMA_INCLUDE_ELEM = "include";
  public static final String SCHEMA_ELEM = "schema";
  
  public static final String SOAPHTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
  
  //WSDL1.1 namespaces
  public static String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
  public static String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";
  public static String HTTP_NS = "http://schemas.xmlsoap.org/wsdl/http/";
  public static String MIME_NS = "http://schemas.xmlsoap.org/wsdl/mime/";
  public static String SCHEMA_NS = NS.XS;
  public static String SOAP12_NS = "http://schemas.xmlsoap.org/wsdl/soap12/";
  
  //Prefixies
  public static String SOAP_PREF = "soap";
  public static String MIME_PREF = "mime";
  public static String HTTP_PREF = "http";
  public static String WSDL_PREF = "wsdl";
  
}
