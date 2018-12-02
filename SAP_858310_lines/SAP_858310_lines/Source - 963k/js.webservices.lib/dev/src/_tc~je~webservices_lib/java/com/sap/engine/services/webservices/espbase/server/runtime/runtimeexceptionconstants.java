package com.sap.engine.services.webservices.espbase.server.runtime;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface RuntimeExceptionConstants extends java.io.Serializable {  
  //ESP ProtocolProcessor.java
  public static final String INVALID_PROTOCOL_RETURNCODE =  "webservices_2032";

  //ESP WSDLTemplatesDscr.java
  public static final String NO_WSDL_TEMPLATE_FOUND =  "webservices_2033";
  //ESP WSDLVisualizer.java
  public static final String INVALID_REQUEST_PARAMETER =  "webservices_2034";
  //ESP WSDLVisualizer.java
  public static final String CONFIG_ENTITY_NOT_FOUND =  "webservices_2035";
  //ESP WSDLVisualizer.java
  public static final String ONE_WSDLENTITY_EXPECTED =  "webservices_2036";
  //ESP WSDLVisualizer.java
  public static final String DEFAULT_STYLE_NOT_DEFINED =  "webservices_2037";
  //RuntimeProcessingEnvironment
  public static final String PROTOCOL_INSTANCE_NOT_FOUND =  "webservices_2038";
  //JavaClassImplementationContainer
  public static final String SERVLET_ENDPOINT_WRAPPER_DOES_NOT_WRAP_INSTANCE =  "webservices_2039";
  //WSDLVisualizer
  public static final String NO_WSDL_PORT_FOUND  =  "webservices_2040";
  //WSDLVisualizer
  public static final String INVALID_NUMBER_SOAP_ADDRESS_ELEMENTS  =  "webservices_2041";
  //WSDLVisualizer
  public static final String NO_CONFIG_ENTITY_FOUND_FOR_REQUEST_URL  =  "webservices_2042";
  //RuntimeProcessingEnvironment
  public static final String PROVIDER_HANDLERS_PROTOCOL_IS_NOT_LAST_PROTOCOL  =  "webservices_2043";
  public static final String UNKNOW_PROTOCOL_PROCESSING_RETURNCODE  =  "webservices_2044";
  public static final String DESERIALIZED_OBJECTARRAY_DIFFER_IN_LENGTH  =  "webservices_2045";
  //ApplicationWebServiceContextImpl
  public static final String ONE_WAY_OPERATION  =  "webservices_2046";
  public static final String ILLEGAL_TRANSPORT  =  "webservices_2047";
  public static final String ILLEGAL_MESSAGE  =  "webservices_2048";
  //ConfigurationContextSerializerImpl
  public static final String UNRECORGNIZED_RUNTIME_STORAGE_FORMAT_ELEMENT  =  "webservices_2049";
  //WSDLVisualizer
  public static final String REQUESTED_RESOURCE_OUTSIDE_RESOURCE_DIR  =  "webservices_2050";
  public static final String UNABLE_TO_DELETE_WSDLCACHE_DIR  =  "webservices_2051";
  //RuntimeProcessingEnvironment
  public static final String MESSAGE_TYPE_CANNOT_BE_SEND_AS_ONEWAY_REQUEST  =  "webservices_2052";
  //BuiltInWSEndpointImplContainer
  public static final String INTERFACE_MAPPING_IS_MISSING_BUILTIN_IMPLLINK_OR_ACTION_PROPERTY  =  "webservices_2053";
  public static final String UNABLE_TO_FIND_ENTITY_USING_ACTION_KEY  =  "webservices_2054";
  public static final String CANNOT_FIND_JAVA_OPERATION  =  "webservices_2055";
  public static final String SOAPAPPLICATION_PROPERTY_VALUE_NOT_RECORGNIZED  =  "webservices_2056";
  public static final String APPLICATION_WEBSERVICE_CONTEXT_IS_ACCESSED_FROM_INVALID_ENVIRONMENT  =  "webservices_2057";
  public static final String TRANSPORT_BINDING_NOT_FOUND = "webservices_2058";  
}
