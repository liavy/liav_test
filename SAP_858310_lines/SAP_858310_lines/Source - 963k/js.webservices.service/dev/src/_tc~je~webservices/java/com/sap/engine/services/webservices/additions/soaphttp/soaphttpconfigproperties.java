package com.sap.engine.services.webservices.additions.soaphttp;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface SOAPHTTPConfigProperties {

  //Binding specific
  public static final String STYLE = "style";
  //Operation specific
  public static final String SOAPACTION = "soapAction";
  //In-Out message specific
  public static final String USE = "use";
  public static final String NAMESPACE = "namespace";
  public static final String ENCODINGSTYLE = "encodingStyle";
  //Operatoin key-properties
  public static final String SOAPACTION_KEY = "soapAction";
  public static final String FIRST_ELEMENT_NS = "first-body-element-ns";
  public static final String FIRST_ELEMENT_NAME = "first-body-element-name";
  //Additional
  public static final String HEADERS_SCHEMAS = "endpoint-header-schemas";
  public static final String OPERATION_HEADERS = "headers";
  public static final String HEADER_ELEMENT_URI = "namespaceURI";
  public static final String HEADER_ELEMENT_LOCALPART = "localpart";

}

