﻿package com.sap.engine.services.webservices.additions.soaphttp.exceptions;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Holds the exception mappings to the resource bundle IDs
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface ExceptionConstants extends java.io.Serializable {

  //AbstractSOAPTransportBinding.java
  public static final String UNRECORGNIZED_BINIDING_STYLE  =  "webservices_2100"; //use in tb binding generation
  public static final String OPERATION_CONFIGURATION_EXCEPTION  =  "webservices_2101"; //marks missing or incorrect properties for operation
  public static final String WSDLEXCEPTION_IN_BINDING_CREATION  =  "webservices_2102"; //marks WSDL exception in binding creation
  public static final String PCONF_EXCEPTION_IN_HEADERS_SCHEMAS_LOADING  =  "webservices_2103"; //marks that cannot load header schemas from configuration
  public static final String SAX_EXCEPTION_IN_HEADERS_SCHEMAS_LOADING  =  "webservices_2104"; //marks that cannot load header schemas from configuration
  public static final String IO_EXCEPTION_IN_HEADERS_SCHEMAS_LOADING  =  "webservices_2105"; //marks that cannot load header schemas from configuration
  public static final String INCORRECT_PROPERTY_VALUE  =  "webservices_2106"; //marks property-name, place(config) found value
  public static final String NO_SCHEMAS_FOR_HEADERS  =  "webservices_2107"; //has header declared but no schemas for them
  public static final String WSDLEXCEPTION_IN_ADDING_HEADERS  =  "webservices_2108"; //while adding headers messages to definitions

  //SOAPHTTPTransportBinding.java
  public static final String EMPTY_BODY_IN_REQUEST  =  "webservices_2109"; //in Body element has no children
  public static final String INCORRECT_REQUEST_STYLE_PARAMETER  =  "webservices_2110"; //if request string misses or has incorrect 'style' parameter
  public static final String IOEXCEPTION_IN_STREAM_OBTAINING  =  "webservices_2111"; //if request string misses or has incorrect 'style' parameter
  public static final String WRONG_TYPE_PARAMETER  =  "webservices_2112"; //if incorect impl of interface or abstact class is passed
  public static final String NULL_REGISTRY_FOUND  =  "webservices_2113"; //if javaToQname or typeMapping registries are null
  public static final String PARSER_EXCEPTION_IN_REQUEST_PARSING  =  "webservices_2114"; //if ParserException occurs in reading request
  public static final String EOF_IN_BODY_ELEMENT_SEARCH  =  "webservices_2115"; //if EOF is reach while searching bodi content for element
  public static final String IOEXCEPTION_IN_SENDING_RESPONSE  =  "webservices_2116"; //if IOExceptin occurs in sending response

  //StreamEngine.java
  public static final String READER_NOT_ON_START_ELEMENT  =  "webservices_2117"; //if reader is not possitioned on start element
  public static final String OPERATION_ELEMENT_MISMASH  =  "webservices_2118"; //if operation element localname and uri does not match config ones
  public static final String NO_MAPPED_QNAME_FOR_CLASS  =  "webservices_2119"; //if for class has no qname mapped
  public static final String NO_DESERIALIZER_FOUND  =  "webservices_2120"; //if cound not find deserializer for given qname
  public static final String DESERIALIZING_FAILS  =  "webservices_2121"; //if cannot desrialize message content
  public static final String PARAMETER_NAME_MISMASH  =  "webservices_2122"; //if request element-parameter name does not match configuration one
  public static final String NO_SERIALIZER_FOUND  =  "webservices_2123"; //if cound not find serializer for given qname
  public static final String SERIALIZING_FAILS  =  "webservices_2124"; //if cannot serialize object
  public static final String BUILDING_STREAMRESPONSE_IOEXCEPTION  =  "webservices_2125"; //if IOException occurs in building response
  public static final String ENCODINGSTYLE_NOTSUPPORTED  =  "webservices_2126"; //if encodingStyle is not supported
  public static final String INOUT_PARAMETERS_NOTSUPPORTED  =  "webservices_2127"; //if in/out parameter are not supported
  public static final String EOF_END_OPERATION_TAG  =  "webservices_2128"; //if eof is reached while looking for operatin end element
  public static final String DESERIALIZING_REFERENCE_FAILS  =  "webservices_2129"; //if exception is thrown in deserializing remaing elements
  public static final String UNRESOLVED_REFERENCE  =  "webservices_2130"; //if object is not completely build due to inresolvin
  public static final String ENCODINGSTYLE_PROPERTY_NOTFOUND  =  "webservices_2131"; //if encodingStyle property is not found in encoded mode processing
  public static final String EOF_NEXT_ELEMENT_START  =  "webservices_2132"; //if eof is reached while procesing elements

  public static final String BULDING_OBJECT_FROM_CONTENT  =  "webservices_2157"; //if null is return from deserialize method of default value

  //DOMEngine.java
  public static final String ELEMENT_COUNT_EXCEPTION  =  "webservices_2133"; //if element is found inccrrect times
  public static final String JAVA_DOM_PARAMETER_COUNT_EXCEPTION  =  "webservices_2134"; //if the number of parameters differ

  //AbstractHTTPTranspoirtBinding.java
  public static final String ENCODED_LITERA_PORTTYPE_EXCEPTION  =  "webservices_2135"; //if one of these portTypes is not found

  //HTTPTranspoirtBinding.java
  public static final String CONTENT_LENGTH_HEADER_EXCEPTION  =  "webservices_2136"; //if content-length header is missing or has incoorect value
  public static final String POST_CONTENT_IOEXCEPTION  =  "webservices_2137"; //if IOException occurs in in processing post content
  public static final String MISSING_QUERY_PARAMETER  =  "webservices_2138"; //if parameter is not found in query string
  public static final String NOT_SUPPORTED_PARAMETER_CLASS  =  "webservices_2139"; //if the class is not supported aparameter
  public static final String INSTANCE_CREATION_EXCEPTION  =  "webservices_2140"; //if could not create Object from data

  //InternalSOAPMessage.java
  public static final String EOF_IN_BODY_OR_HEADER_SEARCHING  =  "webservices_2141"; //if could not create Object from data
  public static final String PARSER_CNFG_EXCEPTION_FOR_DOCUMENT  =  "webservices_2142";
  public static final String BODYWRAPPER_DATA_MISSING  =  "webservices_2143";
  public static final String CANNOT_FIND_HEADER_ENDTAG  =  "webservices_2144";
  public static final String CANNOT_FIND_BODY_STARTTAG  =  "webservices_2145";
  public static final String EOF_REACHED  =  "webservices_2146";
  public static final String DOM_BUILDING_EXCEPTIONS  =  "webservices_2147";

  //InternalMIMEMessage.java
  public static final String NO_MESSAGE_PART_WITH_CID  =  "webservices_2148";
  public static final String MESSAGE_HAS_NO_PARTS  =  "webservices_2149";
  public static final String INCORRECT_ROOTPART_CONTENTTYPE =  "webservices_2150";
  public static final String INCORRECT_MESSAGEPART_HEADERS =  "webservices_2151";
  public static final String INIT_DESERIALIZATION_MODE_FAILS =  "webservices_2152";
  public static final String WRITE_MESSAGE_FAILS =  "webservices_2153";
  public static final String UNSUPPORTED_MIME_OBJECT =  "webservices_2154";
  public static final String MISMASH_OBJECT_CONTENTTYPE =  "webservices_2155";
  public static final String CREATING_OBJECT_FROM_PART_CONTENT =  "webservices_2156";

  //HTTPStatefulProtocol.java
  public static final String CANNOT_FIND_FEATURE_CONFIG =  "webservices_2158";
  public static final String REQUESTED_SESSION_IS_NOT_VALID =  "webservices_2159";

  //StreamEngineMIME.java
  public static final String CANNOT_FIND_REFERENCED_ATTACHMENT = "webservices_2160";

  //DOMEngine.java
  public static final String CANNOT_FIND_SOAPHEADER_ELEMENT = "webservices_2161";

  //AbstractSOAPTransportBinding.java
  public static final String INVALID_SOAPHEADER_CONFIGURATION = "webservices_2162";

  //DOMEngine.java
  public static final String CANNOT_FIND_REQUIRED_PARAMETER = "webservices_2163";

  //StreamEngine.java
  public static final String NOT_QUALIFIED_SOAPHEADER_ENTRY = "webservices_2164";

  //HTTPTransportBinding.java
  public static final String FOUND_MORE_THAN_ONE_QUERY_PARAMETER = "webservices_2165";

  //HTTPTransportBinding.java
  public static final String FOUND_MULTI_DIMENTIONAL_ARRAY_PARAMETER = "webservices_2166";

  //SOAPHTTPTransportBinding.java
  public static final String UNSUPPORTED_MEDIA_CONTENTTYPE = "webservices_2167";

  //SOAPHTTPTransportBinding.java
  public static final String WRONG_REQUEST_METHOD = "webservices_2168";

  //StreamEngine.java
  public static final String ONE_REQUEST_PARAMETER_ALLOWED = "webservices_2169";

  //DOMEngine.java
  public static final String MUSTUNDERSTAND_HEADER_FAULT_MESSAGE = "webservices_2170";

  //InternalSOAPMEssage.java
  public static final String WRONG_SOAP_ENVELOPE_NS = "webservices_2171";

  //InternalSOAPMEssage.java
  public static final String CANNOT_FIND_ENVELOPE_ELEMENT = "webservices_2172";

  //StreamEngineMIME.java
  public static final String NOT_SUPPORTED_TRANSFER_ENCODING = "webservices_2173";

  //MessageIDProtocolImpl.java
  public static final String MISSING_MESSAGEID_HEADER = "webservices_2174";

  //InternalSOAPMessage.java
  public static final String EMPTY_SOAP_BODY_RESPONSE_BUFFER = "webservices_2175";
  //StreamEngine.java
  public static final String INCORRECT_FAULTCODE_NS_VALUE = "webservices_2176";
  //SOAPHTTPTransportBinding
  public static final String OPERATION_NOT_FOUND = "webservices_2177";
  //SOAPHTTPTransportBinding
  public static final String UNKNOWN_RESPONSE_MESSAGE_SEMANTIC = "webservices_2178";
  //HTTPTransportBinding
  public static final String WRONG_REQUEST_HTTP_METHOD = "webservices_2179";
  //HTTPTransportBinding
  public static final String UNABLE_TO_RESOLVE_OPERATION = "webservices_2180";
  //HTTPStatefulProtocol
  public static final String UNKNOW_MESSAGE_INSTANCE = "webservices_2181";
  //StreamEngine
  public static final String NO_SCHEMA_TYPE_FOR_ELEMENT = "webservices_2182";
  //StreamEngine
  public static final String INCONSISTENT_PARAMETER_NAMES = "webservices_2183";
  
}
