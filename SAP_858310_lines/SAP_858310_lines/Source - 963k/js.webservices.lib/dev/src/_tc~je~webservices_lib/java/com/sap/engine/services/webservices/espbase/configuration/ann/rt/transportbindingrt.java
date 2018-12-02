package com.sap.engine.services.webservices.espbase.configuration.ann.rt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Run-Time Type level properties of technical configuration for HTTP message exchange.
 * NameSpace of the coresponding feature is: <tt>http://www.sap.com/webas/710/soap/features/transportbinding/</tt>.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransportBindingRT
{
//  /**
//   * Attribute, which defines the access URL path for a Web Service client. This is not needed on ESR �outside-in� configurations.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>URLPath</tt>.
//   */
//  String URLPath() default "";
  
//  /**
//   * Attribute, which defines the protocol to be used for accessing a Web Service.
//   * Attribute is optional and if the value is <tt>"http"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>URLProtocol</tt>.
//   * Possible values for the attribute are: <tt>"http"</tt> and <tt>"https"</tt>.
//   */
//  String URLProtocol() default "http";
  
//  /**
//   * Attribute, which defines the name of the host used in the Web Service access URL for a local Web Service client.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>URLHost</tt>.
//   */
//  String URLHost() default "";
  
//  /**
//   * Attribute, which defines the port number used in the Web Service access URL for a local Web Service client.
//   * Attribute is optional and if the value is <tt>80</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>URLPort</tt>.
//   * Possible values for the attribute are: <tt>Integer (0-65535)</tt>.
//   */
//  int URLPort() default 80;
  
//  /**
//   * Attribute, which defines the SAP client (mandant) in which a ESR Web Service should be called. 
//   * This informtion is used for the calculation of the Web Service access URL.
//   * Attribute is optional and if the value is <tt>000</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>URLClient</tt>.
//   * Possible values for the attribute are: <tt>3-digit Integer (000-999)</tt>.
//   */
//  String URLClient() default "000";
  
//  /**
//   * Attribute, which defines the login language for the partner system as ISO language code. 
//   * If not set, the user- or system-specific setting is significant.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>URLLanguage</tt>.
//   */
//  String URLLanguage() default "";
  
//  /**
//   * Attribute, which defines whether a proxy gets used for the HTTP connections on client side, then its host name is specifed here.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>ProxyHost</tt>.
//   */
//  String ProxyHost() default "";
  
//  /**
//   * Attribute, which defines whether a proxy gets used for the HTTP connections on client side, then the port number of the proxy process is specifed here.
//   * Attribute is optional and if the value is <tt>0</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>ProxyPort</tt>.
//   * Possible values for the attribute are: <tt>Integer (0-65535)</tt>.
//   */
//  int ProxyPort() default 0;
  
//  /**
//   * Attribute, which defines whether a proxy gets used for the HTTP connections on client side, then the user name needed for the proxy access is specifed here.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>ProxyUser</tt>.
//   */
//  String ProxyUser() default "";
  
//  /**
//   * Attribute, which defines whether a proxy gets used for the HTTP connections on client side, 
//   * then the user�s password needed for the proxy access is specifed here.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>ProxyPassword</tt>.
//   */
//  String ProxyPassword() default "";
  
  /**
   * Attribute, which defines name of the host which should be used in the WSDL document generation on 
   * Web Service side instead of the current host or the client (mandant) wide set host name.
   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>altHost</tt>.
   */
  String AltHost() default "";
  
  /**
   * Attribute, which defines number of the port which should be used in the WSDL document generation on 
   * Web Service side instead of the current port or the client (mandant) wide set port number.
   * Attribute is optional and if the value is <tt>0</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>altPort</tt>.
   * Possible values for the attribute are: <tt>Integer (0-65535)</tt>.
   */
  int AltPort() default 0;
  
  /**
   * Attribute, which defines the access URL path under which a local Web Service is accessible in addition. 
   * Leads to the creation of an alias for the local Web Service.
   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
   * Name of the coresponding property name is <tt>altPath</tt>.
   */
  String AltPath() default "";
  
//  /**
//   * Attribute, which defines the calculated access URL path.
//   * Attribute is optional and if the value is <tt>0</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>calcPath</tt>.
//   * Possible values for the attribute are: <tt>Integer (0-65535)</tt>.
//   */
//  int CalcPath() default 0;
 
//  /**
//   * Attribute, which defines the used protocol. Gets derived from property �TLSType�.
//   * Attribute is optional and if the value is <tt>"http"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>calcProtocol</tt>.
//   * Possible values for the attribute are: <tt>"http"</tt> and <tt>"https"</tt>.
//   */
//  String CalcProtocol() default "http";
  
//  /**
//   * Attribute, which defines name of the SAP HTTP destination containing the technical configuration of the Web Service client. 
//   * The stored URL in the destination contains maximum 120 characters of the complete URL. 
//   * This part is called the �URL prefix�. If the URL is longer, then the rest is stored in �DestinationPath�. If not set, a GUID gets used.
//   * Attribute is optional and if the value is <tt>""</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>Destination</tt>.
//   */
//  String Destination() default "";
  
//  /**
//   * Attribute, which defines the URL suffix if the complete URLs could not be stored in the SAP destination entity.
//   * Attribute is optional and if the value is <tt>0</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>DestinationPath</tt>.
//   * Possible values for the attribute are: <tt>Integer (0-65535)</tt>.
//   */
//  int DestinationPath() default 0;
  
//  /**
//   * Attribute, which defines whether perform system-local Web Servce call.
//   * Attribute is optional and if the value is <tt>false</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>LocalCall</tt>.
//   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
//   */
//  boolean LocalCall() default false;
  
//  /**
//   * Attribute, which defines the WSDL document style which should be used for WSDL generation.
//   * Attribute is optional and if the value is <tt>"Document"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>Style</tt>.
//   * Possible values for the attribute are: <tt>"Document"</tt> and <tt>"RPC"</tt>.
//   */
//  String Style() default "Document";
  
//  /**
//   * Attribute, which defines Type (URI) of the usded transport bindings. This comprises the kind of mes-sage exchange, 
//   * i.e. a request-response or a one-way-pattern.
//   * Name of the coresponding property name is <tt>Type</tt>.
//   * Possible values for the attribute are: <tt>"http://schemas.xmlsoap.org/soap/http"</tt>.
//   */
//  String Type() default "http://schemas.xmlsoap.org/soap/http";
  
//  /**
//   * Attribute, which defines time (in seconds) before session gets terminated on session enabling.
//   * Attribute is optional and if the value is <tt>0</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>ServiceSessionTimeout</tt>.
//   * Possible values for the attribute are: <tt>Integer (0 == unspecified)</tt>.
//   */
//  int ServiceSessionTimeout() default 0;
  
//  /**
//   * Attribute, which defines time (in seconds) how long a WS consumer waits for a response.
//   * Attribute is optional and if the value is <tt>0</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>ConsumerMaxWaitTime</tt>.
//   * Possible values for the attribute are: <tt>Integer (0 == ICM timeout)</tt>.
//   */
//  int ConsumerMaxWaitTime() default 0;
  
//  /**
//   * Attribute, which defines transfer XML data optimized.
//   * Attribute is optional and if the value is <tt>"None"</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>ConsumerMaxWaitTime</tt>.
//   * Possible values for the attribute are: <tt>"None"</tt>, <tt>"SAPBinaryXML"</tt> and <tt>"MTOM"</tt>.
//   */
//  String OptimizedXMLTransfer() default "None";
  
//  /**
//   * Attribute, which defines whether the send HTTP message should be gzip compressed before sending.
//   * Attribute is optional and if the value is <tt>false</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>compressRequest</tt>.
//   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
//   */
//  boolean CompressRequest() default false;
  
//  /**
//   * Attribute, which defines whether the response to a HTTP client request will be gzip compressed accepted.
//   * Attribute is optional and if the value is <tt>true</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>compressResponse</tt>.
//   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
//   */
//  boolean CompressResponse() default true;
  
//  /**
//   * Attribute, which defines in the request Connection:keepAlive http header in order one and same 
//   * connection to be used by multiple request-response cycles.
//   * Attribute is optional and if the value is <tt>true</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>keepAliveStatus</tt>.
//   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
//   */
//  boolean KeepAliveStatus() default true;
  
//  /**
//   * Attribute, which defines whether the client has committed the request to the server it starts waiting for server�s response. 
//   * This property determines for how long the client should wait for the response before throwing an error.
//   * Attribute is optional and if the value is <tt>60000</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>socketTimeout</tt>.
//   * Possible values for the attribute are: <tt>Interger (0-MAX_INT)</tt>.
//   */
//  int SocketTimeout() default 60000;
  
//  /**
//   * Attribute, which defines whether issue chunked http requests against the server.
//   * Attribute is optional and if the value is <tt>false</tt>, coresponding property is not generated.
//   * Name of the coresponding property name is <tt>chunkedRequest</tt>.
//   * Possible values for the attribute are: <tt>true</tt> and <tt>false</tt>.
//   */
//  boolean ChunkedRequest() default false;  
}
