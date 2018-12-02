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

import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.configuration.Behaviour;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.wsrm.RMConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.xi.ESPXIMessageProcessor;
import com.sap.engine.services.webservices.espbase.xi.util.XIReceiver;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.AuthenticationFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.HTTPKeepAliveFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.MessageIdFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.ProxyFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.SessionFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.TimeoutFeature;

/**
 * Class containing all public client feature and property constants.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class PublicProperties {
  // SOCKET TIMEOUT settings 
  public static final String F_TIMEOUT_NAMESPACE = TimeoutFeature.TIMEOUT_FEATURE;
  public static final String F_TIMEOUT_LOCALNAME = TimeoutFeature.SO_TIMEOUT;
  public static final String F_CONNECTION_TIMEOUT_LOCALNAME = TimeoutFeature.SO_CONNECTION_TIMEOUT;
  public static final String P_STUB_TIMEOUT = "socketTimeout";
  public static final QName C_TIMEOUT_QNAME = new QName(F_TIMEOUT_NAMESPACE,F_TIMEOUT_LOCALNAME);
  public static final QName C_CONNECTION_TIMEOUT_QNAME = new QName(F_TIMEOUT_NAMESPACE, F_CONNECTION_TIMEOUT_LOCALNAME);
  
  // PROXY settings  
  public static final String F_PROXY_NAMESPACE = ProxyFeature.PROXY_FEATURE;  
  public static final String F_PROXY_HOST = ProxyFeature.PROXY_HOST_PROPERTY;
  public static final String F_PROXY_PORT = ProxyFeature.PROXY_PORT_PROPERTY;
  public static final String F_PROXY_USER = ProxyFeature.PROXY_USERNAME;
  public static final String F_PROXY_PASS = ProxyFeature.PROXY_PASSWORD;
  public static final String P_STUB_PROXY_HOST = "javax.xml.rpc.http.proxyhost";
  public static final String P_STUB_PROXY_PORT = "javax.xml.rpc.http.proxyport";
  public static final String P_STUB_PROXY_USER = "proxyUser";
  public static final String P_STUB_PROXY_PASS = "proxyPassword";  
  public static final String P_STUB_REQUEST_HEADERS = "requestHeaders";  
  public static final QName C_PROXY_HOST = new QName(F_PROXY_NAMESPACE,F_PROXY_HOST);
  public static final QName C_PROXY_PORT = new QName(F_PROXY_NAMESPACE,F_PROXY_PORT);
  public static final QName C_PROXY_USER = new QName(F_PROXY_NAMESPACE,F_PROXY_USER);
  public static final QName C_PROXY_PASS = new QName(F_PROXY_NAMESPACE,F_PROXY_PASS);
  //the following three properties are for temporary use - till there is specification determing SOAP1.2 for WSDL1.1 binding.
  public static final String P_STUB_SOAP_VERSION = "SOAPVersion"; //property name  
  public static final String P_STUB_SOAP_VERSION_11 = "soap11";  //property value denoting SOAP1.1
  public static final String P_STUB_SOAP_VERSION_12 = "soap12";  //property value denoting SOAP1.2
  public static final String P_CLIENT_TYPE = "ClientType";
  
  // SAX Parsing feature
  public static final String SAX_RESPONSE_HANDLER = "saxResponseHandler";
  public static final String SAX_RESPONSE_READER = "saxResponseReader";

  public static final String F_SESSION_NAMESPACE = SessionFeature.SESSION_FEATURE;
  public static final String F_SESSION_METHOD = SessionFeature.SESSION_METHOD_PROPERTY;
  public static final String F_SESSION_COOKIE = SessionFeature.SESSION_COOKIE_PROPERTY;
  public static final String F_SESSION_MAINTAIN = SessionFeature.HTTP_MAINTAIN_SESSION;
  public static final String F_SESSION_ABAP = SessionFeature.ABAP_SESSION;
  public static final String F_SESSION_METHOD_HTTP = SessionFeature.HTTP_SESSION_METHOD;
  public static final String F_SESSION_METHOD_NONE = SessionFeature.HTTP_BYPASS_METHOD;
  public static final String F_SESSION_MAINTAIN_TRUE = SessionFeature.USE_SESSION_TRUE;
  public static final String F_SESSION_MAINTAIN_FALSE = SessionFeature.USE_SESSION_FALSE;
  public static final String F_SESSION_FLUSH = SessionFeature.HTTP_FLUSH_SESSION;
  public static final String F_SESSION_FLUSH_TRUE = SessionFeature.FLUSH_SESSION_TRUE;
  public static final String F_SESSION_FLUSH_FALSE = SessionFeature.FLUSH_SESSION_FALSE;
  
  public static final String P_SESSION_MAINTAIN = Stub.SESSION_MAINTAIN_PROPERTY;
  public static final QName C_SESSION_METHOD = new QName(F_SESSION_NAMESPACE,F_SESSION_METHOD); 
  public static final QName C_SESSION_MAINTAIN = new QName(F_SESSION_NAMESPACE,F_SESSION_MAINTAIN);
  public static final String C_SESSION_MAINTAIN_STRING = C_SESSION_MAINTAIN.toString();
  public static final QName C_ABAP_SESSION = new QName(F_SESSION_NAMESPACE,F_SESSION_ABAP);
  // Dynamic property especially for ESF - this is not allowed to be used for any other type of client!!! 
  public static final String P_ESF_VALUE_MANAGER = "ESFValueManager";
  // ENDPOINT URL settings
  public static final String P_ENDPOINT_URL = Stub.ENDPOINT_ADDRESS_PROPERTY;
  // LOG settings
  public static final String P_REQUEST_LOG_STREAM = "RequestLogging";
  public static final String P_RESPONSE_LOG_STREAM = "ResponseLogging";
  public static final String SUPPRESS_ERROR_TRACING = "SuppressedErrorTracing";
  // BASIC SECURITY properties
  public static final String P_SEC_USERNAME = Stub.USERNAME_PROPERTY;
  public static final String P_SEC_PASSWORD = Stub.PASSWORD_PROPERTY;  
  public static final String F_SEC_NAMESPACE = AuthenticationFeature.AUTHENTICATION_FEATURE;
  public static final String F_SEC_LEVEL = AuthenticationFeature.AUTHENTICATION_LEVEL;
  public static final String F_SEC_METHOD = AuthenticationFeature.AUTHENTICATION_METHOD;
  public static final String F_SEC_USER = AuthenticationFeature.AUTHENTICATION_CREDENTIAL_USER;
  public static final String F_SEC_PASSWORD = AuthenticationFeature.AUTHENTICATION_CREDENTIAL_PASSWORD;
  public static final String F_SEC_LEVEL_NONE = AuthenticationFeature.LEVEL_NONE;
  public static final String F_SEC_LEVEL_BASIC = AuthenticationFeature.LEVEL_BASIC;
  public static final String F_SEC_LEVEL_STRONG = AuthenticationFeature.LEVEL_STRONG;
  public static final String F_SEC_METHOD_BASIC = AuthenticationFeature.METHOD_BASIC;
  public static final QName C_SEC_USERNAME = new QName(F_SEC_NAMESPACE,F_SEC_USER);
  public static final QName C_SEC_PASSWORD = new QName(F_SEC_NAMESPACE,F_SEC_PASSWORD);
  //public static final QName C_SEC_LEVEL =  new QName(F_SEC_NAMESPACE, F_SEC_LEVEL);
  public static final QName C_SEC_METHOD = new QName(F_SEC_NAMESPACE, F_SEC_METHOD);
  public static final QName CLIENT_CERTIFICATE_LIST = new QName(PublicProperties.F_SEC_NAMESPACE, AuthenticationFeature.AUTHENTICATION_CLIENT_CERT_LIST);
  public static final QName SERVER_IGNORE_CERTS = new QName(PublicProperties.F_SEC_NAMESPACE, AuthenticationFeature.AUTHENTICATION_SERVER_IGNORE_CERTS);
  public static final QName SERVER_CERTIFICATE_LIST = new QName(PublicProperties.F_SEC_NAMESPACE, AuthenticationFeature.AUTHENTICATION_SERVER_CERT_LIST);
  
  public static final String P_SOAP_ACTION = new QName(NS.SOAPENC,"SOAPAction").toString();
  public static final String P_HTTP_REQUEST_HEADERS = "httpRequestHeaders";
  public static final String P_HTTP_RESPONSE_HEADERS = "httpResponseHeaders";
  
  
  // MESSAGE ID SETTINGS
  public static final String F_MESSAGEID_NAMESPACE = MessageIdFeature.MESSAGEIDFEATURE;
  public static final String F_MESSAGEID_LOCALNAME = MessageIdFeature.DEFAULTNAME;
  public static final String F_MESSAGEID_USE = "true";
  public static final QName C_MESSAGEID_QNAME = new QName(F_MESSAGEID_NAMESPACE,F_MESSAGEID_LOCALNAME);
  
  
  // KEEP ALIVE  SETTINGS 
  public static final String P_KEEP_ALIVE = HTTPKeepAliveFeature.KEEP_ALIVE_PROPERTY;
  public static final String F_KEEPALIVE_NAMESPACE = HTTPKeepAliveFeature.KEEP_ALIVE_FEATURE;
  public static final String F_KEEPALIVE_PROPERTY = HTTPKeepAliveFeature.KEEP_ALIVE_PROPERTY;
  public static final QName C_KEEP_ALIVE = new QName(F_KEEPALIVE_NAMESPACE,F_KEEPALIVE_PROPERTY);
  // COMPRESS RESPONSE SETTINGS
  public static final String P_COMPRESS_RESPONSE = HTTPKeepAliveFeature.COMPRESS_RESPONSE_PROPERTY;
  public static final String F_COMPRESS_RESPONSE_NAMESPACE = HTTPKeepAliveFeature.COMPRESS_RESPONSE_FEATURE;
  public static final String F_COMPRESS_RESPONSE_PROPERTY = HTTPKeepAliveFeature.COMPRESS_RESPONSE_PROPERTY;
  public static final QName C_COMPRESS_RESPONSE = new QName(F_COMPRESS_RESPONSE_NAMESPACE,F_COMPRESS_RESPONSE_PROPERTY);
     
  // Attachment contstants 
  public static final String P_OUTBOUND_ATTACHMENTS = "outboundAttachments";
  public static final String P_INBOUND_ATTACHMENTS = "inboundAttachments";
  public static final String CALL_PROTOCOLS =  "invoke.protocols";
  
  public static final String P_PERFORMANCE_INFO = "performanceInfo";
  
  /*
  public static final QName SESSION_METHOD_PROPERTY = new QName(SESSION_NAMESPACE ,"SessionMethod");
  public static final String HTTP_SESSION_METHOD = "httpCookies";
  public static final QName HTTP_MAINTAIN_SESSION = new QName(SESSION_NAMESPACE, "maintainSession");
  public static final String USE_SESSION_TRUE ="yes";
  public static final String USE_SESSION_FALSE ="no";
  public static final QName SESSION_COOKIE_PROPERTY = new QName(SESSION_NAMESPACE,"SessionCoockie");
  public static final QName ABAP_SESSION = new QName(SESSION_NAMESPACE,"abapSession");
    
  
  public static final String HTTP_BYPASS_METHOD = "none";

  public static final String HTTP_TIMEOUT_PROPERY = "timeout";
  public static final String REQUEST_LOGGING_PROPERTY = "RequestLogging";
  public static final String RESPONSE_LOGGING_PROPERTY = "ResponseLogging" ;  
  
  public static final QName TIMEOUT_PROPERTY = new QName(TIMEOUT_NAMESPACE,HTTP_TIMEOUT_PROPERY);
   
  public static final QName PROXY_HOST_PROPERTY = new QName(PROXY_NAMESPACE, "proxyHost");
  public static final QName PROXY_PORT_PROPERTY = new QName(PROXY_NAMESPACE, "proxyPort");
  public static final QName PROXY_USERNAME = new QName(PROXY_NAMESPACE,"proxyUser");
  public static final QName PROXY_PASSWORD = new QName(PROXY_NAMESPACE,"proxyPassword");
  */
  public static final String TRANSPORT_WARANTEE_FEATURE = "http://www.sap.com/webas/630/soap/features/transportguarantee/";
  
  public static final String AUTHENTICATION_FEATURE = "http://www.sap.com/webas/630/soap/features/authentication/";
  public static final String AUTHENTICATION_METHOD = "AuthenticationMethod";
  public static final String AUTHENTICATION_METHOD_NONE  = "sapsp:None";    
  public static final String AUTHENTICATION_METHOD_BASIC = "sapsp:HTTPBasic";
  public static final String AUTHENTICATION_METHOD_BASIC_USER = "AuthenticationMethod.Basic.Username";
  public static final String AUTHENTICATION_METHOD_BASIC_PASS = "AuthenticationMethod.Basic.Password";
  public static final QName AUTHENTICATION_METHOD_QNAME = new QName(AUTHENTICATION_FEATURE,AUTHENTICATION_METHOD);
  public static final QName AUTHENTICATION_METHOD_BASIC_USER_QNAME = new QName(AUTHENTICATION_FEATURE,AUTHENTICATION_METHOD_BASIC_USER);
  public static final QName AUTHENTICATION_METHOD_BASIC_PASS_QNAME = new QName(AUTHENTICATION_FEATURE,AUTHENTICATION_METHOD_BASIC_PASS);
  
  
  public static final String TRANSPORT_BINDING_FEATURE = "http://www.sap.com/webas/710/soap/features/transportbinding/";
  public static final String TRANSPORT_BINDING_PROXY_HOST = "ProxyHost";
  public static final String TRANSPORT_BINDING_PROXY_PORT = "ProxyPort";
  public static final String TRANSPORT_BINDING_PROXY_USER = "ProxyUser";
  public static final String TRANSPORT_BINDING_NON_PROXY_HOSTS = "NonProxyHosts";
  public static final String TRANSPORT_BINDING_XI_RUNTIME_NS = "http://www.sap.com/710/features/xi/integration/";
  public static final String TRANSPORT_BINDING_PROXY_PASS = "ProxyPassword";  
  // Optimized XML transfer support
  public static final String TRANSPORT_BINDING_OPTIMIZED_XML = "OptimizedXMLTransfer";
  public static final String TRANSPORT_BINDING_OPTXML_NONE = "None";
  public static final String TRANSPORT_BINDING_OPTXML_BXML = "SAPBinaryXML";
  public static final String TRANSPORT_BINDING_OPTXML_MTOM = "MTOM";
  public static final String TRANSPORT_BINDING_LOCAL_CALL = "LocalCall";
  public static final String TRANSPORT_BINDING_LOCAL_CALL_HOST_PORT = "http://localcallhost:711";
  // Java specific
  public static final String TRANSPORT_BINDING_KEEP_ALIVE = "keepAliveStatus"; // Default true
  public static final String TRANSPORT_BINDING_SUGGEST_COMPRESSED_RESPONSE = "compressResponse"; // Default true
  public static final String TRANSPORT_BINDING_SOCKET_TIMEOUT = "socketTimeout"; // Default 60 seconds
  public static final String TRANSPORT_BINDING_CHUNKED_REQUEST = "chunkedRequest"; // Default true
  // ideopotency
  public static final String IDEMPOTENCY_PROPS_NS = "http://www.sap.com/710/soap/features/idempotency/";
  public static final QName IDEMPOTENCY_ACTIVE_RT_PROP_NAME = new QName(IDEMPOTENCY_PROPS_NS, "Idempotency.Active.RT");
  public static final QName IDEMPOTENCY_RETRY_SLEEP_RT_PROP_NAME = new QName(IDEMPOTENCY_PROPS_NS, "Idempotency.RetrySleep.RT");
  public static final QName IDEMPOTENCY_RETRIES_COUNT_RT_PROP_NAME = new QName(IDEMPOTENCY_PROPS_NS, "Idempotency.RetriesCount.RT");
  public static final QName IDEMPOTENCY_UUID_RT_PROP_NAME = new QName(IDEMPOTENCY_PROPS_NS, "Idempotency.UUID.RT");
  public static final boolean IDEMPOTENCY_ACTIVE_RT_PROP_DEFAULT_VALUE = true;
  public static final long IDEMPOTENCY_RETRY_SLEEP_RT_PROP_DEFAULT_VALUE = 30000;
  public static final long IDEMPOTENCY_RETRIES_COUNT_RT_PROP_DEFAULT_VALUE = 5;
  //xi integration
  public static final String XI_INTEGARTION_NS = "http://www.sap.com/xi/integration/";
  public static final QName XI_IS_SYNC_DT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.IsSync.DT");
  public static final QName XI_XI_TRANSPORT_IS_POSSIBLE_DT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.XITransportIsPossible.DT");
  public static final QName XI_RUNTIME_ENVIRONMENT_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.RuntimeEnvironment.RT");
  public static final String XI_RUNTIME_ENVIRONMENT_XI_RT_PROP_VALUE = "XI";
  public static final String XI_RUNTIME_ENVIRONMENT_SOAP_RT_PROP_VALUE = "SOAP";
  public static final QName XI_SENDER_PARTY_NAME_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.SenderPartyName.RT");
  public static final QName XI_SENDER_SERVICE_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.SenderService.RT");
  public static final QName XI_SYS_ACK_REQUESTED_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.SysAckRequested.RT");
  public static final QName XI_SYS_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.SysNegativeAckRequested.RT");
  public static final QName XI_APP_ACK_REQUESTED_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.AppAckRequested.RT");
  public static final QName XI_APP_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.AppNegativeAckRequested.RT");
  public static final QName XI_QUEUE_ID_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.QueueId.RT");
  public static final QName XI_SERVICE_INTERFACE_NAME_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.ServiceInterfaceName.RT");
  public static final QName XI_RECEIVERS_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.Receivers.RT");
  public static final QName XI_MESSAGE_PROCESSOR_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.MsgProc.RT");
  public static final QName XI_IS_ASYNC_RT_PROP_NAME = new QName(XI_INTEGARTION_NS, "XI.IsAsync.RT");
  
  public static final String WSRM_PROPS_NS = "http://www.sap.com/710/soap/features/idempotency/";
  public static final QName WSRM_ENABLED_PROP_NAME = new QName(WSRM_PROPS_NS, "enableWSRM");
  
  public static final String BOOLEAN_TRUE_VALUE = "true";
  public static final String BOOLEAN_FALSE_VALUE = "false";
  public static final String BOOLEAN_YES_VALUE = "yes";
  public static final String BOOLEAN_NO_VALUE = "no";
  
  /**
   * Returns configured client Endpoint url.
   */
  public static final String getEndpointURL(ClientConfigurationContext context) {
    String endpointURLPath = getPersistableProperty(P_ENDPOINT_URL, context);
    if(endpointURLPath == null || endpointURLPath.length() == 0) {
      endpointURLPath = context.getStaticContext().getRTConfig().getUrl();
    }
    return(endpointURLPath);
  }
  
  public static final void setEndpointURL(String endpointURLPath, ClientConfigurationContext context) {
    setPersistableProperty(P_ENDPOINT_URL, endpointURLPath, context);
  }
  
  /**
   * Returns chunked request option. The default setting is true.
   * @param context
   * @return
   */
  public static final boolean getChunkedRequest(ClientConfigurationContext context) {
    return(isTrue(getGlobalPersistableProperty(new QName(TRANSPORT_BINDING_FEATURE, TRANSPORT_BINDING_CHUNKED_REQUEST), context), false));
  }
  
  /**
   * Returns configured proxy host for web services client.
   * @param context
   * @return
   */
  public static final String getProxyHost(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(new QName(TRANSPORT_BINDING_FEATURE, TRANSPORT_BINDING_PROXY_HOST), C_PROXY_HOST, context));
  }
   
  /**
   * Returns configured proxy host.
   * @param context
   * @return
   */
  public static final String getProxyPort(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(new QName(TRANSPORT_BINDING_FEATURE, TRANSPORT_BINDING_PROXY_PORT), C_PROXY_PORT, context));
  }
  
  /**
   * Returns configured proxy user.
   * @param context
   * @return
   */
  public static final String getProxyUser(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(new QName(TRANSPORT_BINDING_FEATURE, TRANSPORT_BINDING_PROXY_USER), C_PROXY_USER, context));
  }
  
  /**
   * Returns configured proxy password.
   * @param context
   * @return
   */
  public static final String getNonProxyHosts(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(new QName(TRANSPORT_BINDING_FEATURE, TRANSPORT_BINDING_NON_PROXY_HOSTS), context));    
  }
  
  
  /**
   * Returns configured proxy password.
   * @param context
   * @return
   */
  public static final String getProxyPassword(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(new QName(TRANSPORT_BINDING_FEATURE, TRANSPORT_BINDING_PROXY_PASS), C_PROXY_PASS, context));
  }
  
  /**
   * Connection Keep-Alive suggested status. If the property is enabled each client will try to reuse the connection
   * for multiple calls.
   * @param context
   * @return
   */
  public static final String getKeeAlive(ClientConfigurationContext context) {
    String result = (getGlobalPersistableProperty(new QName(TRANSPORT_BINDING_FEATURE, TRANSPORT_BINDING_KEEP_ALIVE), C_KEEP_ALIVE, context));
    if (result == null) return "true"; // By default this feature is enabled
    return result;
  }
  
  public static final String getSuggestCompressedResponse(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(new QName(TRANSPORT_BINDING_FEATURE, TRANSPORT_BINDING_SUGGEST_COMPRESSED_RESPONSE), C_COMPRESS_RESPONSE, context));
  }
  
  public static final String getSocketTimeout(ClientConfigurationContext context) {
    return(getGlobalProperty(C_TIMEOUT_QNAME,new QName(TRANSPORT_BINDING_FEATURE, TRANSPORT_BINDING_SOCKET_TIMEOUT), context));
  }
  
  public static final void setSocketTimeout(long timeout, ClientConfigurationContext context) {
    setPersistableProperty(C_TIMEOUT_QNAME, Long.toString(timeout), context);
  }
  
  public static final String getGlobalPersistableProperty(QName propertyName, QName alternativePropertyName, ClientConfigurationContext context) {
    String value = getGlobalPersistableProperty(propertyName, context);
    return(value != null ? value : getGlobalPersistableProperty(alternativePropertyName, context));
  }
  
  public static final String getSocketConnectionTimeout(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(C_CONNECTION_TIMEOUT_QNAME, context));
  }
  
  public static final void setGlobalIdempotencyActive(boolean active, ClientConfigurationContext context) {
    setPersistableProperty(IDEMPOTENCY_ACTIVE_RT_PROP_NAME, convertToString(active), context);
  }
  
  public static final boolean isGlobalIdempotencyActive(ClientConfigurationContext context) {
    return(isTrue(getGlobalPersistableProperty(IDEMPOTENCY_ACTIVE_RT_PROP_NAME, context), IDEMPOTENCY_ACTIVE_RT_PROP_DEFAULT_VALUE));
  }
  
  public static final boolean isOperationIdempotencyActive(ClientConfigurationContext context) {
    if(PublicProperties.isGlobalIdempotencyActive(context)) {
      OperationData operationData = context.getStaticContext().getDTConfig().getOperationData(getWSDLOperationName(context));
      if(operationData != null) {
        PropertyListType propListType = operationData.getSinglePropertyList();
        if(propListType != null) {
          String isIdempotencyActive = getProperty(propListType.getProperty(RMConfigurationMarshaller.NS_IDEMPOTENCY, RMConfigurationMarshaller.IDEMPOTENCY_PROPERTY));
          
          if (RMConfigurationMarshaller.IDEMPOTENCY_PROPERTY_OFF.equals(isIdempotencyActive)){
            // The idempotency can be explicitly turned off.
            return false;
          }else {
            // If default value is set - the idempotency is turned on.
            return RMConfigurationMarshaller.IDEMPOTENCY_PROPERTY_DEFAULT_VALUE.equals(isIdempotencyActive);
          }
        }
      }
    }
    return(false);
  }
  
  private static String getWSDLOperationName(ClientConfigurationContext context) {
    InterfaceMapping interfaceMapping = context.getStaticContext().getInterfaceData();
    OperationMapping operationMapping = interfaceMapping.getOperationByJavaName(context.getOperationName());
    return(operationMapping.getWSDLOperationName());
  }
  
  
  public static void setIdempotencyRetrySleep(long sleep, ClientConfigurationContext context) {
    setPersistableProperty(IDEMPOTENCY_RETRY_SLEEP_RT_PROP_NAME, Long.toString(sleep), context);
  }
  
  public static long getIdempotencyRetrySleep(ClientConfigurationContext context) {
    return(convertToLong(getGlobalPersistableProperty(IDEMPOTENCY_RETRY_SLEEP_RT_PROP_NAME, context), IDEMPOTENCY_RETRY_SLEEP_RT_PROP_DEFAULT_VALUE));
  }
  
  public static void setIdempotencyRetriesCount(int count, ClientConfigurationContext context) {
    setPersistableProperty(IDEMPOTENCY_RETRIES_COUNT_RT_PROP_NAME, Integer.toString(count), context);
  }
  
  public static int getIdempotencyRetriesCount(ClientConfigurationContext context) {
    return((int)(convertToLong(getGlobalPersistableProperty(IDEMPOTENCY_RETRIES_COUNT_RT_PROP_NAME, context), IDEMPOTENCY_RETRIES_COUNT_RT_PROP_DEFAULT_VALUE)));
  }
  
  public static void setIdempotencyUUID(String uuid, ClientConfigurationContext context) {
    setPersistableProperty(IDEMPOTENCY_UUID_RT_PROP_NAME, uuid, context);
  }
  
  public static String getIdempotencyUUID(ClientConfigurationContext context) {
    return(getPersistableProperty(IDEMPOTENCY_UUID_RT_PROP_NAME, context));
  }
  
  public static final void setUseXITransport(boolean useXICommunication, ClientConfigurationContext context) {
    setPersistableProperty(XI_RUNTIME_ENVIRONMENT_RT_PROP_NAME, useXICommunication ? XI_RUNTIME_ENVIRONMENT_XI_RT_PROP_VALUE : XI_RUNTIME_ENVIRONMENT_SOAP_RT_PROP_VALUE, context);
  }
  
  public static final boolean getUseXITransport(ClientConfigurationContext context) {
    String value = getGlobalPersistableProperty(XI_RUNTIME_ENVIRONMENT_RT_PROP_NAME, context);
    return(XI_RUNTIME_ENVIRONMENT_XI_RT_PROP_VALUE.equals(value));
  }
  
  public static final void setESPXIMessageProcessor(ESPXIMessageProcessor xiMessageProcessor, ClientConfigurationContext context) {
    setDynamicProperty(XI_MESSAGE_PROCESSOR_RT_PROP_NAME, xiMessageProcessor, context);
  }
  
  public static final ESPXIMessageProcessor getESPXIMessageProcessor(ClientConfigurationContext context) {
    return((ESPXIMessageProcessor)(getDynamicProperty(XI_MESSAGE_PROCESSOR_RT_PROP_NAME, context)));
  }
  
  public static final void setXIApplicationAckRequested(String ackListenerName, ClientConfigurationContext context) {
    setPersistableProperty(XI_APP_ACK_REQUESTED_RT_PROP_NAME, ackListenerName, context);
  }
  
  public static final String getXIApplicationAckRequested(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(XI_APP_ACK_REQUESTED_RT_PROP_NAME, context));
  }
  
  public static final void setXISystemAckRequested(String ackListenerName, ClientConfigurationContext context) {
    setPersistableProperty(XI_SYS_ACK_REQUESTED_RT_PROP_NAME, ackListenerName, context);
  }
  
  public static final String getXISystemAckRequested(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(XI_SYS_ACK_REQUESTED_RT_PROP_NAME, context));
  }
  
  public static final void setXIApplicationErrorAckRequested(String ackListenerName, ClientConfigurationContext context) {
    setPersistableProperty(XI_APP_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME, ackListenerName, context);
  }
  
  public static final String getXIApplicationErrorAckRequested(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(XI_APP_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME, context));
  }
  
  public static final void setXISystemErrorAckRequested(String ackListenerName, ClientConfigurationContext context) {
    setPersistableProperty(XI_SYS_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME, ackListenerName, context);
  }
  
  public static final String getXISystemErrorAckRequested(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(XI_SYS_NEGATIVE_ACK_REQUESTED_RT_PROP_NAME, context));
  }
  
  public static final void setXISenderPartyName(String senderPartyName, ClientConfigurationContext context) {
    setPersistableProperty(XI_SENDER_PARTY_NAME_RT_PROP_NAME, senderPartyName, context);
  }
  
  public static final String getXISenderPartyName(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(XI_SENDER_PARTY_NAME_RT_PROP_NAME, context));
  }
  
  public static final void setXISenderService(String senderService, ClientConfigurationContext context) {
    setPersistableProperty(XI_SENDER_SERVICE_RT_PROP_NAME, senderService, context);
  }
  
  public static final String getXISenderService(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(XI_SENDER_SERVICE_RT_PROP_NAME, context));
  }
  
  public static final void setXIQueueId(String queueId, ClientConfigurationContext context) {
    setPersistableProperty(XI_QUEUE_ID_RT_PROP_NAME, queueId, context);
  }
  
  public static final String getXIQueueId(ClientConfigurationContext context) {
    return(getGlobalPersistableProperty(XI_QUEUE_ID_RT_PROP_NAME, context));
  }
  
  public static final void addXIReceiver(String receiverPartyName, String receiverPartyAgency, String receiverPartyScheme, String receiverService, ClientConfigurationContext context) {
    Vector receivers = (Vector)(getDynamicProperty(XI_RECEIVERS_RT_PROP_NAME, context));
    if(receivers == null) {
      receivers = new Vector();
      setDynamicProperty(XI_RECEIVERS_RT_PROP_NAME, receivers, context);
    }
    receivers.add(new XIReceiver(receiverPartyName, receiverPartyAgency, receiverPartyScheme, receiverService));
  }
  
  public static final Vector<XIReceiver> getXIReceivers(ClientConfigurationContext context) {
    return((Vector)getDynamicProperty(XI_RECEIVERS_RT_PROP_NAME, context));
  }
  
  public static final void clearXIReceivers(ClientConfigurationContext context) {
    setDynamicProperty(XI_RECEIVERS_RT_PROP_NAME, null, context);
  }

  public static final boolean isXIAsyncOperation(String operationName, ClientConfigurationContext context) {
    OperationData operationData = context.getStaticContext().getDTConfig().getOperationData(operationName);
    String value = getProperty(XI_IS_SYNC_DT_PROP_NAME, operationData);
    return(!isTrue(value, true));
  }
  
  private static final long convertToLong(String longValueStr, long defaultValue) {
    try {
      return(Long.parseLong(longValueStr));
    } catch(Exception exc) {
      return(defaultValue);
    }
  }
  
  private static final String convertToString(boolean value) {
    return(value ? BOOLEAN_TRUE_VALUE : BOOLEAN_FALSE_VALUE);
  }
  
  public static final boolean isTrue(String value) {
    return(isTrue(value, false));
  }
  
  public static final boolean isFalse(String value) {
    return((BOOLEAN_FALSE_VALUE.equalsIgnoreCase(value) || BOOLEAN_NO_VALUE.equalsIgnoreCase(value)));
  }
  
  public static final boolean isTrue(String value, boolean defaultValue) {
    return(value == null ? defaultValue : (BOOLEAN_TRUE_VALUE.equalsIgnoreCase(value) || BOOLEAN_YES_VALUE.equalsIgnoreCase(value)));
  }
  
  public static final String getGlobalPersistableProperty(QName propertyName, ClientConfigurationContext context) {
    String value = getPersistableProperty(propertyName, context);
    if(value == null || value.length() == 0) {
      value = getRTProperty(propertyName, context);
    }
    return(value);
  }
  
  public static final String getGlobalProperty(QName persistableName, QName rtConfigName, ClientConfigurationContext context) {
    String value = getPersistableProperty(persistableName, context);
    if(value == null || value.length() == 0) {
      value = getRTProperty(rtConfigName, context);
    }
    return(value);    
  }
  
  public static final String getGlobalPersistableProperty(QName propertyName, ProviderContextHelper context) {
    String value = getPersistableProperty(propertyName, context);
    if(value == null || value.length() == 0) {
      value = getRTProperty(propertyName, context);
    }
    return(value);
  }
  
  public static final void setRTProperty(QName propertyName, String propertyValue, ClientConfigurationContext context) {
    setProperty(propertyName, propertyValue, context.getStaticContext().getRTConfig());    
  }
  
  public static final String getRTProperty(QName propertyName, ClientConfigurationContext context) {
    return(getProperty(propertyName, context.getStaticContext().getRTConfig()));
  }

  public static final void setRTProperty(String propertyNS, String propertyName, String propertyValue, ClientConfigurationContext context) {
    setProperty(propertyNS, propertyName, propertyValue, context.getStaticContext().getRTConfig());
  }
  
  public static final String getRTProperty(String propertyNS, String propertyName, ClientConfigurationContext context) {
    return(getProperty(propertyNS, propertyName, context.getStaticContext().getRTConfig()));
  }
  
  public static final void setRTProperty(QName propertyName, String propertyValue, ProviderContextHelper context) {
    setProperty(propertyName, propertyValue, context.getStaticContext().getRTConfiguration());    
  }
  
  public static final String getRTProperty(QName propertyName, ProviderContextHelper context) {
    return(getProperty(propertyName, context.getStaticContext().getRTConfiguration()));
  }

  public static final void setRTProperty(String propertyNS, String propertyName, String propertyValue, ProviderContextHelper context) {
    setProperty(propertyNS, propertyName, propertyValue, context.getStaticContext().getRTConfiguration());
  }
  
  public static final String getRTProperty(String propertyNS, String propertyName, ProviderContextHelper context) {
    return(getProperty(propertyNS, propertyName, context.getStaticContext().getRTConfiguration()));
  }
  
  public static final void setDTProperty(QName propertyName, String propertyValue, ClientConfigurationContext context) {
    setProperty(propertyName, propertyValue, context.getStaticContext().getDTConfig());    
  }
  
  public static final String getDTProperty(QName propertyName, ClientConfigurationContext context) {
    return(getProperty(propertyName, context.getStaticContext().getDTConfig()));
  }

  public static final void setDTProperty(String propertyNS, String propertyName, String propertyValue, ClientConfigurationContext context) {
    setProperty(propertyNS, propertyName, propertyValue, context.getStaticContext().getDTConfig());
  }
  
  public static final String getDTProperty(String propertyNS, String propertyName, ClientConfigurationContext context) {
    return(getProperty(propertyNS, propertyName, context.getStaticContext().getDTConfig()));
  }
  
  public static final void setDTProperty(QName propertyName, String propertyValue, ProviderContextHelper context) {
    setProperty(propertyName, propertyValue, context.getStaticContext().getDTConfiguration().getInterfaceData());    
  }
  
  public static final String getDTProperty(QName propertyName, ProviderContextHelper context) {
    return(getProperty(propertyName, context.getStaticContext().getDTConfiguration().getInterfaceData()));
  }

  public static final void setDTProperty(String propertyNS, String propertyName, String propertyValue, ProviderContextHelper context) {
    setProperty(propertyNS, propertyName, propertyValue, context.getStaticContext().getDTConfiguration().getInterfaceData());
  }
  
  public static final String getDTProperty(String propertyNS, String propertyName, ProviderContextHelper context) {
    return(getProperty(propertyNS, propertyName, context.getStaticContext().getDTConfiguration().getInterfaceData()));
  }
  
  public static final void setPersistableProperty(QName propertyName, String propertyValue, ClientConfigurationContext context) {
    setPersistableProperty(propertyName.toString(), propertyValue, context);
  }
  
  public static final String getPersistableProperty(QName propertyName, ClientConfigurationContext context) {
    return(getPersistableProperty(propertyName.toString(), context));
  }

  public static final void setPersistableProperty(QName propertyName, String propertyValue, ProviderContextHelper context) {
    setPersistableProperty(propertyName.toString(), propertyValue, context);
  }

  public static final String getPersistableProperty(QName propertyName, ProviderContextHelper context) {
    return(getPersistableProperty(propertyName.toString(), context));
  }
  
  public static final void setPersistableProperty(String propertyName, String propertyValue, ClientConfigurationContext context) {
    setProperty(propertyName, propertyValue, context.getPersistableContext());  
  }
  
  public static final String getPersistableProperty(String propertyName, ClientConfigurationContext context) {
    return((String)(getProperty(propertyName, context.getPersistableContext())));  
  }
  
  public static final void setPersistableProperty(String propertyName, String propertyValue, ProviderContextHelper context) {
    setProperty(propertyName, propertyValue, context.getPersistableContext());  
  }
  
  public static final String getPersistableProperty(String propertyName, ProviderContextHelper context) {
    return((String)(getProperty(propertyName, context.getPersistableContext())));  
  }
  
  public static final void setDynamicProperty(String propertyName, Object propertyValue, ClientConfigurationContext context) {
    setProperty(propertyName, propertyValue, context.getDynamicContext());  
  }
  
  public static final Object getDynamicProperty(String propertyName, ClientConfigurationContext context) {
    return(getProperty(propertyName, context.getDynamicContext()));  
  }
  
  public static final void setDynamicProperty(String propertyName, Object propertyValue, ProviderContextHelper context) {
    setProperty(propertyName, propertyValue, context.getDynamicContext());  
  }
  
  public static final Object getDynamicProperty(String propertyName, ProviderContextHelper context) {
    return(getProperty(propertyName, context.getDynamicContext()));  
  }
  
  public static final void setDynamicProperty(QName propertyName, Object propertyValue, ClientConfigurationContext context) {
    setDynamicProperty(propertyName.toString(), propertyValue, context);  
  }
  
  public static final Object getDynamicProperty(QName propertyName, ClientConfigurationContext context) {
    return(getDynamicProperty(propertyName.toString(), context));  
  }
  
  public static final void setDynamicProperty(QName propertyName, Object propertyValue, ProviderContextHelper context) {
    setDynamicProperty(propertyName.toString(), propertyValue, context);  
  }
  
  public static final Object getDynamicProperty(QName propertyName, ProviderContextHelper context) {
    return(getDynamicProperty(propertyName.toString(), context));  
  }

  public static final void setProperty(String propertyName, Object propertyValue, ConfigurationContext context) {
    if(propertyValue == null) {
      context.removeProperty(propertyName);
    } else {
      context.setProperty(propertyName, propertyValue);
    }
  }
  
  public static final Object getProperty(String propertyName, ConfigurationContext context) {
    return(context.getProperty(propertyName));  
  }
  
  public static final void setProperty(String propertyNS, String propertyName, String propertyValue, Behaviour behaviour) {
    behaviour.getSinglePropertyList().overwritePropertyValue(propertyNS, propertyName, propertyValue);
  }
  
  public static final String getProperty(String propertyNS, String propertyName, Behaviour behaviour) {
    PropertyType property = behaviour.getSinglePropertyList().getProperty(propertyNS, propertyName);
    return(getProperty(property));
  }
  
  public static final void setProperty(QName propertyName, String propertyValue, Behaviour behaviour) {
    setProperty(propertyName.getNamespaceURI(), propertyName.getLocalPart(), propertyValue, behaviour);
  }
  
  public static final String getProperty(QName propertyName, Behaviour behaviour) {
    PropertyType property = behaviour.getSinglePropertyList().getProperty(propertyName);
    return(getProperty(property));
  }
  
  public static final String getProperty(PropertyType property) {
    return(property != null ? property.get_value() : null);
  }

  /**
   * Returns the value of the local call property.
   * @param context
   * @return
   */
  public static final boolean isLocalCall(ClientConfigurationContext context) {    
    return "true".equalsIgnoreCase(getGlobalPersistableProperty(new QName(TRANSPORT_BINDING_FEATURE,TRANSPORT_BINDING_LOCAL_CALL),context)); 
  }
}
