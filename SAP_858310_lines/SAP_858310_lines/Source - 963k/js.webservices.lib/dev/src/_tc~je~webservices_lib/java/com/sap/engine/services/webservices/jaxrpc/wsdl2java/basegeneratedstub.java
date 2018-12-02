/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;

import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.services.webservices.jaxm.soap.IteratorImpl;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;


/**
 * Parent for all generated stubs.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class BaseGeneratedStub implements javax.xml.rpc.Stub,Remote {

  public static final String SECURITY_USERNAME = "javax.xml.rpc.security.auth.username"; // Default not set
  public static final String SECURITY_USERPASS = "javax.xml.rpc.security.auth.password"; // Default not set
  public static final String PORT_ENDPOINT = "javax.xml.rpc.service.endpoint.address"; // Default set
  public static final String HTTPPROXYPORT = "javax.xml.rpc.http.proxyport"; // HTTP proxy port
  public static final String HTTPPROXYHOST = "javax.xml.rpc.http.proxyhost"; // HTTP proxy host
  public static final String HTTP_PROXY_USER = "proxyUser"; //HTTP proxy user for HTTP Proxy Authentication
  public static final String HTTP_PROXY_PASS = "proxyPassword"; //HTTP proxy user for HTTP Proxy Authentication
  public static final String REQUESTLOGGINGSTREAM = "RequestLogging"; //SASHO
  public static final String RESPONSELOGGINGSTREAM = "ResponseLogging";
  public static final String HTTP_PROXY_RESOLVER_PROPERTY = "HTTPProxyResolver"; //SASHO
  public static final String SOCKETTIMEOUT = "socketTimeout";
  public static final String KEEP_ALIVE = HTTPKeepAliveFeature.KEEP_ALIVE_PROPERTY;
  public static final String COMPRESS_RESPONSE = HTTPKeepAliveFeature.COMPRESS_RESPONSE_PROPERTY;
  public static final String SAX_RESPONSE_HANDLER = "saxResponseHandler";
  public static final String OBJECT_FACTORY = "objectFactory";
  public static final String HTTP_REQUEST_HEADERS = "httpRequestHeaders";
  public static final String HTTP_RESPONSE_HEADERS = "httpResponseHeaders";
  public static final String HTTP_REDIRECT_SUPPORT = "httpRedirect";


  protected PropertyContext jaxrpcConfig; // Jax-Rpc properties
  protected PropertyContext stubConfiguration; // Whole transport binding config
  protected PropertyContext bindingConfiguration; // Binding config context
  protected PropertyContext featureConfiguration; // Configuration of choosen features
  protected ClientTransportBinding transportBinding;
  protected ServiceParam[] inputParams;
  protected ServiceParam[] outputParams;
  protected ServiceParam[] faultParams;
  protected ProtocolList globalProtocols;
  protected Hashtable localProtocols;
  protected Hashtable localFeatures;
  // Logging enhasements
  protected boolean userLogging = false;
  protected boolean logStarted = false;
  protected ByteArrayOutputStream inputLog = new ByteArrayOutputStream();
  protected ByteArrayOutputStream outputLog = new ByteArrayOutputStream();
  protected Location location;
  private static final String[] VALID_PROPERTIES = {SECURITY_USERNAME,SECURITY_USERPASS,PORT_ENDPOINT,HTTPPROXYPORT,HTTPPROXYHOST,REQUESTLOGGINGSTREAM,RESPONSELOGGINGSTREAM,HTTP_PROXY_RESOLVER_PROPERTY,SOCKETTIMEOUT,SESSION_MAINTAIN_PROPERTY,HTTP_PROXY_USER,HTTP_PROXY_PASS,KEEP_ALIVE,SAX_RESPONSE_HANDLER,OBJECT_FACTORY,HTTP_REQUEST_HEADERS,HTTP_RESPONSE_HEADERS,HTTP_REDIRECT_SUPPORT,COMPRESS_RESPONSE};

  protected void _beginLogFrame() {
    if (userLogging == false && location != null && location.beLogged(Severity.DEBUG)) {
      this.featureConfiguration.setProperty(REQUESTLOGGINGSTREAM, inputLog);
      this.featureConfiguration.setProperty(RESPONSELOGGINGSTREAM, outputLog);
      logStarted = true;
    }
  }

  protected void _endLogFrame(String methodName) {
    if (logStarted) {
      this.featureConfiguration.clearProperty(REQUESTLOGGINGSTREAM);
      this.featureConfiguration.clearProperty(RESPONSELOGGINGSTREAM);
      if (userLogging == false && location != null && location.beLogged(Severity.DEBUG)) {
        location.logT(Severity.DEBUG,"Web Method ["+methodName+"] called.");
        location.logT(Severity.DEBUG,inputLog.toString());
        location.logT(Severity.DEBUG,outputLog.toString()); //$JL-I18N$
      }
      inputLog.reset();
      outputLog.reset();
      logStarted = false;
    }
  }

  public void _setTypeRegistry(com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl _registry) {
  }


  public void _setLogLocation(Location location) {
    this.location = location;
  }

  public void _clearLogLocation() {
    this.location = null;
  }

  public BaseGeneratedStub() {
    stubConfiguration = new PropertyContext();
    bindingConfiguration = new PropertyContext();
    globalProtocols = new ProtocolList();
    localProtocols = new Hashtable();
    localFeatures = new Hashtable();
    featureConfiguration = new PropertyContext();
    jaxrpcConfig = new PropertyContext();
  }

  /**
   * Returns global protocol list.
   */
  public ProtocolList _getGlobalProtocols() {
    return globalProtocols;
  }

  /**
   * Returns operation protocols.
   */
  public ProtocolList _getOperationProtocols(String operationName) {
    return (ProtocolList) localProtocols.get(operationName);
  }

  /**
   * Returns global property context.
   */
  public PropertyContext _getGlobalFeatureConfig() {
    return featureConfiguration;
  }

  /**
   * Sets global property context.
   * @param context
   */
  public void _setGlobalFeatureConfig(PropertyContext context) {
    if (context == null) return;
    this.featureConfiguration = context;
  }

  /**
   * returns Operation Protocol Configuration.
   */
  public PropertyContext _getOperationFeatureConfig(String operationName) {
    return (PropertyContext) localFeatures.get(operationName);
  }

  /**
   * Method for setting endpoint url.
   */
  public void _setEndpoint(String url) {
    jaxrpcConfig.setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, url);
  }

  /**
   * Method for getting endpoint url.
   */
  public String _getEndpoint() {
    return (String) jaxrpcConfig.getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY);
  }

  /**
   * Fills endpoint in property context.
   */
  public void _fillEndpoint(PropertyContext pContext) {
    pContext.setProperty(ClientTransportBinding.ENDPOINT,_getEndpoint());
  }

  /**
   * Returns JAX-EPC stub property.
   */
  public Object _getProperty(String s) {
    if (s == null) {
      throw new UnsupportedOperationException("<NULL> is not valid key value.");
    }
    boolean flag = false;
    for (int i=0; i<VALID_PROPERTIES.length; i++) {
      if (VALID_PROPERTIES[i].equals(s)) {
        flag = true; break;
      }
    }
    if (!flag) {
      throw new UnsupportedOperationException("Property ["+s+"] is not supported by the environment.");
    }
    return jaxrpcConfig.getProperty(s);
  }

  /**
   * Returns property names.
   */
  public Iterator _getPropertyNames() {
    IteratorImpl result = new IteratorImpl();
    result.init(jaxrpcConfig.getProperyKeys());
    return result;
  }

  /**
   * Set's JAX-RPC stub property.
   */
  public void _setProperty(String s, Object o) {
    if (s == null) {
      throw new UnsupportedOperationException("<NULL> is not valid key value.");
    }
    boolean flag = false;
    for (int i=0; i<VALID_PROPERTIES.length; i++) {
      if (VALID_PROPERTIES[i].equals(s)) {
        flag = true; break;
      }
    }
    if (!flag) {
      throw new UnsupportedOperationException("Property ["+s+"] is not supported by the environment.");
    }

    jaxrpcConfig.setProperty(s,o);
    if (Stub.USERNAME_PROPERTY.equals(s) && o!=null && o instanceof String) {
      String userName = (String) o;
      if (userName.length()!=0) {
        // 6.30 discard cookie and open new http session Experimental
        PropertyContext feature = featureConfiguration.getSubContext(SessionFeature.SESSION_FEATURE);
        if (feature.isDefined()) { // This feature is defined
          feature.clearProperty(SessionFeature.SESSION_COOKIE_PROPERTY);
          feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_TRUE);
        }
      }
    }
    if (Stub.PASSWORD_PROPERTY.equals(s) && o!=null && o instanceof String) {
      String userName = (String) o;
      if (userName.length()!=0) {
        // 6.30 discard cookie and open new http session Experimental
        PropertyContext feature = featureConfiguration.getSubContext(SessionFeature.SESSION_FEATURE);
        if (feature.isDefined()) { // This feature is defined
          feature.clearProperty(SessionFeature.SESSION_COOKIE_PROPERTY);
          feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_TRUE);
        }
      }
    }

  }

  /**
   * Returns configuration context for method call.
   */
  public PropertyContext _buildOperationContext(String operationName, ClientTransportBinding binding) {
    stubConfiguration.clear();
    //PropertyContext fconfig = featureConfiguration.getClone();
    //PropertyContext bconfig = bindingConfiguration.getClone();
    stubConfiguration.setSubContext(ClientTransportBinding.BINDING_CONFIG,bindingConfiguration);
    stubConfiguration.setSubContext(ClientTransportBinding.FEATUTE_CONFIG,featureConfiguration);
//    Enumeration keys = protocolGlobalConfiguration.getSubcontextKeys();
//    while (keys.hasMoreElements()) {
//      String key = (String) keys.nextElement();
//      PropertyContext context = protocolGlobalConfiguration.getSubContext(key);
//      if (context.isDefined()) {
//        stubConfiguration.joinProperyContext(key,context);
//      }
//    }
    PropertyContext operationFeatures = _getOperationFeatureConfig(operationName);
    if (operationFeatures != null) {
      stubConfiguration.joinProperyContext(ClientTransportBinding.FEATUTE_CONFIG,operationFeatures);
    }
    _loadJaxRpcConfig();
    return stubConfiguration;
  }

  /**
   * Loads Jax-Rpx config and overrides default config.
   */
  private void _loadJaxRpcConfig() {
//    String endpoint = (String) jaxrpcConfig.getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY);
//    if (endpoint != null && endpoint.length()!=0) {
//      stubConfiguration.setProperty(ClientTransportBinding.ENDPOINT,endpoint);
//    }
    PropertyContext features = stubConfiguration.getSubContext(ClientTransportBinding.FEATUTE_CONFIG);
    // Special dispatcher redirect support
    String httpRedirect = (String) jaxrpcConfig.getProperty(BaseGeneratedStub.HTTP_REDIRECT_SUPPORT);
    if (httpRedirect != null && httpRedirect.length() != 0) {
      features.setProperty(BaseGeneratedStub.HTTP_REDIRECT_SUPPORT,"yes");
    } else {
      features.clearProperty(BaseGeneratedStub.HTTP_REDIRECT_SUPPORT);
    }
    String userName = (String) jaxrpcConfig.getProperty(Stub.USERNAME_PROPERTY);
    if (userName != null) {
      if (userName.length() != 0) {
        PropertyContext perm = features.getSubContext(AuthenticationFeature.AUTHENTICATION_FEATURE);
        perm.setProperty(AuthenticationFeature.AUTHENTICATION_METHOD,AuthenticationFeature.METHOD_BASIC);
        perm.setProperty(AuthenticationFeature.AUTHENTICATION_CREDENTIAL_USER,userName);
        perm.setProperty(AuthenticationFeature.AUTHENTICATION_CREDENTIAL_PASSWORD,jaxrpcConfig.getProperty(Stub.PASSWORD_PROPERTY));
      } else {
        PropertyContext perm = features.getSubContext(AuthenticationFeature.AUTHENTICATION_FEATURE);
        perm.clearProperty(AuthenticationFeature.AUTHENTICATION_CREDENTIAL_USER);
        perm.clearProperty(AuthenticationFeature.AUTHENTICATION_CREDENTIAL_PASSWORD);
      }
    }
    Object saxHandler = jaxrpcConfig.getProperty(SAX_RESPONSE_HANDLER);
    if (saxHandler != null) {
      features.setProperty(SAX_RESPONSE_HANDLER,saxHandler);
    }
    String proxyHost = (String) jaxrpcConfig.getProperty(HTTPPROXYHOST);
    if (proxyHost != null) {
      PropertyContext perm = features.getSubContext(ProxyFeature.PROXY_FEATURE);
      if (proxyHost.length() != 0) {
        perm.setProperty(ProxyFeature.PROXY_HOST_PROPERTY,jaxrpcConfig.getProperty(HTTPPROXYHOST));
        perm.setProperty(ProxyFeature.PROXY_PORT_PROPERTY,jaxrpcConfig.getProperty(HTTPPROXYPORT));
      } else {
        perm.clearProperty(ProxyFeature.PROXY_HOST_PROPERTY);
        perm.clearProperty(ProxyFeature.PROXY_PORT_PROPERTY);
      }
    }
    String proxyUser = (String) jaxrpcConfig.getProperty(HTTP_PROXY_USER); 
    String proxyPass = (String) jaxrpcConfig.getProperty(HTTP_PROXY_PASS);
    if (proxyUser != null) {
      PropertyContext perm = features.getSubContext(ProxyFeature.PROXY_FEATURE);
      perm.setProperty(ProxyFeature.PROXY_USERNAME, proxyUser);
      if (proxyPass != null) {
        perm.setProperty(ProxyFeature.PROXY_PASSWORD, proxyPass);
      } else {
        perm.setProperty(ProxyFeature.PROXY_PASSWORD, "");
      }
    } else {
      PropertyContext perm = features.getSubContext(ProxyFeature.PROXY_FEATURE);
      perm.clearProperty(ProxyFeature.PROXY_USERNAME);
      perm.clearProperty(ProxyFeature.PROXY_PASSWORD);
    }
    if (jaxrpcConfig.getProperty(SOCKETTIMEOUT)!= null) {
      String timeOut = (String) jaxrpcConfig.getProperty(SOCKETTIMEOUT);
      if (timeOut.length() != 0) {
        PropertyContext perm = features.getSubContext(TimeoutFeature.TIMEOUT_FEATURE);
        perm.setProperty(TimeoutFeature.SO_TIMEOUT, timeOut);
      } else {
        PropertyContext perm = features.getSubContext(TimeoutFeature.TIMEOUT_FEATURE);
        perm.clearProperty(TimeoutFeature.SO_TIMEOUT);
      }
    }

    if (jaxrpcConfig.getProperty(KEEP_ALIVE) != null) {
      String keepAliveValue = (String) jaxrpcConfig.getProperty(KEEP_ALIVE);
      if (keepAliveValue.length() != 0) {
        PropertyContext perm = features.getSubContext(HTTPKeepAliveFeature.KEEP_ALIVE_FEATURE);
        perm.setProperty(HTTPKeepAliveFeature.KEEP_ALIVE_PROPERTY, keepAliveValue);
      } else {
        PropertyContext perm = features.getSubContext(HTTPKeepAliveFeature.KEEP_ALIVE_FEATURE);
        perm.clearProperty(HTTPKeepAliveFeature.KEEP_ALIVE_PROPERTY);
      }
    }
    if (jaxrpcConfig.getProperty(COMPRESS_RESPONSE) != null) {
      String keepAliveValue = (String) jaxrpcConfig.getProperty(COMPRESS_RESPONSE);
      if (keepAliveValue.length() != 0) {
        PropertyContext perm = features.getSubContext(HTTPKeepAliveFeature.COMPRESS_RESPONSE_FEATURE);
        perm.setProperty(HTTPKeepAliveFeature.COMPRESS_RESPONSE_PROPERTY, keepAliveValue);
      } else {
        PropertyContext perm = features.getSubContext(HTTPKeepAliveFeature.COMPRESS_RESPONSE_FEATURE);
        perm.clearProperty(HTTPKeepAliveFeature.COMPRESS_RESPONSE_PROPERTY);
      }
    }
    if (jaxrpcConfig.getProperty(Stub.SESSION_MAINTAIN_PROPERTY) != null) {
      String useSession = (String) jaxrpcConfig.getProperty(Stub.SESSION_MAINTAIN_PROPERTY);
      PropertyContext context = featureConfiguration.getSubContext(SessionFeature.SESSION_FEATURE);
      context.setProperty(SessionFeature.SESSION_METHOD_PROPERTY,SessionFeature.HTTP_SESSION_METHOD);
      if ("true".equals(useSession)) {
        context.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_TRUE);
      } else if ("false".equals(useSession)) {
        context.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_FALSE);
        context.clearProperty(SessionFeature.SESSION_COOKIE_PROPERTY);
      }
    }
    if (jaxrpcConfig.getProperty(BaseGeneratedStub.HTTP_REQUEST_HEADERS) != null) {
      stubConfiguration.setProperty(BaseGeneratedStub.HTTP_REQUEST_HEADERS,jaxrpcConfig.getProperty(BaseGeneratedStub.HTTP_REQUEST_HEADERS));
    }
    if (jaxrpcConfig.getProperty(BaseGeneratedStub.HTTP_RESPONSE_HEADERS) != null) {
      stubConfiguration.setProperty(BaseGeneratedStub.HTTP_RESPONSE_HEADERS,jaxrpcConfig.getProperty(BaseGeneratedStub.HTTP_RESPONSE_HEADERS));
    }
    stubConfiguration.setProperty(ClientTransportBinding.APP_CLASSLOADER,this.getClass().getClassLoader());
  }

  public ClientTransportBinding _getTransportBinding() {
    return transportBinding;
  }

  public String _getTransportBindingName() {
    if (transportBinding != null) {
      return transportBinding.getName();
    } else {
      return null;
    }
  }

  public void _setTransportBinding(ClientTransportBinding binding) {
    this.transportBinding = binding;
  }


  public void _clear() {
    globalProtocols.clear();
    featureConfiguration.clear();
    Enumeration keys = localFeatures.keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      PropertyContext context = (PropertyContext) localFeatures.get(key);
      context.clear();
      ArrayList protocols = (ArrayList) localProtocols.get(key);
      protocols.clear();
    }
  }

  /**
   * Call this to enable request/response logging to these output streams.
   * @param requestLog OutputStream where request will be logged
   * @param responseLog OutputStream where response will be logged
   */
  public void _startLogging(OutputStream requestLog, OutputStream responseLog) {
    this.userLogging = true;
    this.featureConfiguration.setProperty(REQUESTLOGGINGSTREAM, requestLog); //SASHO
    this.featureConfiguration.setProperty(RESPONSELOGGINGSTREAM, responseLog);
  }

  /**
   * Stops logging of request/response if turned on.
   */
  public void _stopLogging() {
    this.featureConfiguration.clearProperty(REQUESTLOGGINGSTREAM); //SASHO
    this.featureConfiguration.clearProperty(RESPONSELOGGINGSTREAM);
    this.userLogging = false;
  }

  /**
   * Set HTTPProxyResolver
   */
  public void _setHTTPProxyResolver(HTTPProxyResolver proxyResolver) { //SASHO
    if (proxyResolver != null) {
      this.featureConfiguration.setProperty(HTTP_PROXY_RESOLVER_PROPERTY, proxyResolver);
    } else {
      this.featureConfiguration.clearProperty(HTTP_PROXY_RESOLVER_PROPERTY);
    }
  }

  /**
   * Sends empty request to the webservice.
   * @throws RemoteException
   */
  public void _flush() throws RemoteException {
    this.bindingConfiguration.clear();
    _fillEndpoint(bindingConfiguration);
    _buildOperationContext("",this.transportBinding);
    try {
      this.transportBinding.flush(this.stubConfiguration,this.globalProtocols);
    } catch (Exception e) {
      throw new RemoteException(e.getMessage(),e);
    }
  }
  
  protected void _initParameter(final ServiceParam param,final String schemaNS,final String schemaName,final String paramName,final Class contentClass) {   
    param.schemaName = new QName(schemaNS,schemaName);
    param.name = paramName;
    param.contentClass = contentClass;
  }

//  public TypeMappingRegistry getTypeMapping() {
//    return TypeMappingRegistry
//  }
}
