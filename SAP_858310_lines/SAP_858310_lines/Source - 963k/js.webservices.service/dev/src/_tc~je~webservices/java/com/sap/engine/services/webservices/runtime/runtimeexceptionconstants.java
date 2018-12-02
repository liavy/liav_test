package com.sap.engine.services.webservices.runtime;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface RuntimeExceptionConstants extends java.io.Serializable {

  //Runtime classes
  public static final String ILLEGAL_ARGUMENT_EXCEPTION  =  "webservices_2000";
  public static final String ACCESS_TO_METHOD_DENIED =  "webservices_2001";
  public static final String UNSUCCESSABLE_INVACATION_EXPIRES =  "webservices_2002";
  public static final String NOTSUPPORTED_IMPLEMENTATION_LINK =  "webservices_2003";
  public static final String CANNOT_FIND_JAVA_OPERATION =  "webservices_2004";
  public static final String DUBLICATE_SESSION_IDS =  "webservices_2005";
  public static final String CANNOT_LOOKUP_OBJECT =  "webservices_2006";
  public static final String CANNOT_CREATE_EJB_OBJECT =  "webservices_2007";
  public static final String CANNOT_CREATE_IMPL_INSTANCE =  "webservices_2008";
  public static final String CANNOT_RECORGNIZE_STYLE =  "webservices_2009";
  public static final String NOTSUPPORTED_STYLE_FOR_WEBSERVICE =  "webservices_2010";
  public static final String ERROR_IN_WSDL_GENERATION =  "webservices_2011";

  //SchemaConvertor.java
  public static final String VI_TYPES_INSTANCES_MISHMASH =  "webservices_2012";
  public static final String VI_TYPES_ORIGINALTYPE_ELEMENT_MISSING =  "webservices_2013";
  public static final String VI_FUNCTION_OUT_PARAM_NUMBER =  "webservices_2014";

  //WSDLPortTypeGenerator.java
  public static final String INCORRECT_PROPERTY =  "webservices_2015";
  public static final String CANNOT_FIND_QNAME_FOR_CLASS =  "webservices_2016";

  //RuntimeContextImpl.java
  public static final String ILLEGAL_SESSION_VALUES =  "webservices_2017";

  //SessionEJBTable
  public static final String CONCURENT_STATEFULBEAN_USE =  "webservices_2018";

  //SchemaConvertor.java
  public static final String RESOLVE_SCHEMA_IMPORTS_FAILS =  "webservices_2019";

  //SchemaInfo.java
  public static final String WRONG_SCHEMA_ELEMENT =  "webservices_2020";

  //WSDLPortTypeGenerator.java
  public static final String DUBLICATE_SCHEMA_URIES =  "webservices_2021";

  //EJBImplementationContainer.java
  public static final String NO_APPLICATION_CLASSLOADER_FOUND =  "webservices_2022";

  //JavaClassImplementationContainer.java
  public static final String UNABLE_TO_LOAD_CLASS =  "webservices_2023";

  //JavaClassImplementationContainer.java
  public static final String MISSING_IMPL_PROPERTY =  "webservices_2024";

  //RuntimeContextImpl.java
  public static final String WRONG_OPERATION_DEFINITION_NUMBER =  "webservices_2025";

  //SchemaConvertor.java
  public static final String NOT_SUPPORTED_VIFUNCTION_PARAM_REF =  "webservices_2026";

  //SchemaConvertor.java
  public static final String INCORRECT_SIMPLECONTENT_DEFINITION =  "webservices_2027";

  //SchemaConvertor.java
  public static final String INCORRECT_BASETYPE_FOUND =  "webservices_2028";

  //SchemaConvertor.java
  public static final String INCORRECT_NCNAME_CHARACTER_FOUND =  "webservices_2029";

  //SchemaConvertor.java
  public static final String ELEMENTS_WITH_EQUAL_NAMES_BUT_DIFFERENT_TYPES =  "webservices_2030";

  //SchemaConvertor.java
  public static final String OPERATION_ELEMENT_ALREADY_IN_SCHEMA =  "webservices_2031";
  
  //ESP ProtocolProcessor.java
  public static final String INVALID_PROTOCOL_RETURNCODE =  "webservices_2032";

  //ESP WSDLTemplatesDscr.java
  public static final String NO_WSDL_TEMPLATE_FOUND =  "webservices_2033";
  //ESP WSDLVisualizer.java
  public static final String INVALID_REQUEST_PARAMETER =  "webservices_2034";
  //ESP WSDLVisualizer.java
  public static final String NO_WSDLOPERATION_FOUND =  "webservices_2035";
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
  
}
