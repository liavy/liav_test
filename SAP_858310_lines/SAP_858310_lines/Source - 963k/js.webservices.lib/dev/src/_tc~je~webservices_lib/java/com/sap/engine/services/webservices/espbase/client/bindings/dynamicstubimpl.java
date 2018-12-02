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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;

import com.sap.engine.interfaces.webservices.esp.Protocol;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.client.api.SessionInterface;
import com.sap.engine.services.webservices.espbase.client.api.SessionInterfaceFactory;
import com.sap.engine.services.webservices.espbase.client.api.impl.MessageIdFeatureImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;
import com.sap.engine.services.webservices.jaxrpc.handlers.ConsumerJAXRPCHandlersProtocol;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList;

/**
 * Base Stub JAX-RPC Implementation. Serves as both dynamic and static proxy instance.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class DynamicStubImpl extends BaseGeneratedStub implements Stub, InvocationHandler, Remote {
  protected static Hashtable persistableProps = new Hashtable();
  protected static HashSet dynamicAllowed = new HashSet();
  
  protected ClientConfigurationContextImpl stubContext = null;
  protected TransportBinding tBinding = null;
  protected Class seiClass = null;
  private static final String DEFAULT_PROTOCOL_ORDER = ConsumerJAXRPCHandlersProtocol.PROTOCOL_NAME;    

  static {
    persistableProps.put(PublicProperties.P_ENDPOINT_URL,PublicProperties.P_ENDPOINT_URL);
    persistableProps.put(PublicProperties.P_STUB_TIMEOUT,PublicProperties.C_TIMEOUT_QNAME);
    persistableProps.put(PublicProperties.P_STUB_SOAP_VERSION, PublicProperties.P_STUB_SOAP_VERSION);
    persistableProps.put(PublicProperties.P_SEC_PASSWORD, PublicProperties.C_SEC_PASSWORD);
    persistableProps.put(PublicProperties.P_SEC_USERNAME, PublicProperties.C_SEC_USERNAME);
    persistableProps.put(PublicProperties.P_STUB_PROXY_USER, PublicProperties.C_PROXY_USER);
    persistableProps.put(PublicProperties.P_STUB_PROXY_PASS, PublicProperties.C_PROXY_PASS);
    persistableProps.put(PublicProperties.P_STUB_PROXY_HOST, PublicProperties.C_PROXY_HOST);
    persistableProps.put(PublicProperties.P_STUB_PROXY_PORT, PublicProperties.C_PROXY_PORT);
    persistableProps.put(PublicProperties.P_KEEP_ALIVE, PublicProperties.C_KEEP_ALIVE);
    persistableProps.put(PublicProperties.P_COMPRESS_RESPONSE, PublicProperties.C_COMPRESS_RESPONSE);
    persistableProps.put(PublicProperties.P_SESSION_MAINTAIN,PublicProperties.C_SESSION_METHOD);
    
    //WSNavigator hack. Add "/" ad the end of the namespace to match the key created by the security policy marshaller.    
    persistableProps.put(PublicProperties.F_SEC_METHOD, new QName(PublicProperties.C_SEC_METHOD.getNamespaceURI().concat("/"), PublicProperties.C_SEC_METHOD.getLocalPart()));
    
    dynamicAllowed.add(PublicProperties.P_REQUEST_LOG_STREAM);
    dynamicAllowed.add(PublicProperties.P_RESPONSE_LOG_STREAM);
    dynamicAllowed.add(PublicProperties.P_STUB_REQUEST_HEADERS);
    dynamicAllowed.add(PublicProperties.P_ESF_VALUE_MANAGER);
    dynamicAllowed.add(PublicProperties.SAX_RESPONSE_HANDLER);
  }
  
  /**
   * Default constructor.
   */
  public DynamicStubImpl() {
  }
  
  /**
   * 
   * @param typeMapping
   * @param iMapping
   * @param rtConfig
   * @param dtCondig
   */
  public void init(TransportBinding tBinding, Class seiInterface , ClientConfigurationContext config) throws WebserviceClientException {
    this.tBinding = tBinding;
    this.seiClass = seiInterface;
    this.stubContext = (ClientConfigurationContextImpl) config;    
    if (this.seiClass != null) { 
      this.stubContext.setClientApppClassLoader(seiClass.getClassLoader());
    }
    _portCreateNotify(config);
  }        

  /**
   * Notifies service protocols on endpoint creation.
   * @param clientContext
   * @throws WebserviceClientException
   */
  private void _portCreateNotify(ClientConfigurationContext clientContext) throws WebserviceClientException {
    PropertyType pType = clientContext.getStaticContext().getRTConfig().getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS,BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY);    
    String protocolOrder = null;
    if (pType != null) {
      protocolOrder = pType.get_value();
    } else {
      protocolOrder = DEFAULT_PROTOCOL_ORDER;
    }             
    Protocol[] protocols = ConsumerProtocolFactory.protocolFactory.getProtocols(protocolOrder);
    for (int i=0; i<protocols.length; i++) {
      if (protocols[i] instanceof ClientProtocolNotify) {
        try {
          ((ClientProtocolNotify) protocols[i]).portCreate(clientContext);
        } catch (ProtocolException x) {
          throw new WebserviceClientException(WebserviceClientException.PROTOCOL_INIT_FAILURE,x,protocols[i].getProtocolName());
        }
      }
    }
  }
  
  /**
   * This method invokes protocol destroy endpoint event.
   * @throws Throwable
   */    
  protected void finalize() throws Throwable {
    super.finalize();
    PropertyType pType = this.stubContext.getStaticContext().getRTConfig().getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS,BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY);    
    String protocolOrder = null;
    if (pType != null) {
      protocolOrder = pType.get_value();
    } else {
      protocolOrder = DEFAULT_PROTOCOL_ORDER;
    }             
    Protocol[] protocols = ConsumerProtocolFactory.protocolFactory.getProtocols(protocolOrder);
    for (int i=0; i<protocols.length; i++) {
      if (protocols[i] instanceof ClientProtocolNotify) {
        try {
          ((ClientProtocolNotify) protocols[i]).portDestroy(stubContext);
        } catch (ProtocolException x) {
          throw new WebserviceClientException(WebserviceClientException.PROTOCOL_INIT_FAILURE,x,protocols[i].getProtocolName());
        }
      }
    }
  }
  
  
  /**
   * Return runtime property.
   * @param arg0
   * @return
   */
  public Object _getProperty(String arg0) {
    return _getProperty(arg0, this.stubContext);
  }

  /**
   * Returns iterator properties.
   * @return
   */
  public Iterator _getPropertyNames() {
    return this.stubContext.getPersistableContext().properties();
  }

  /**
   * Sets runtime properties.
   * @param arg0
   * @param arg1
   */
  public void _setProperty(String arg0, Object arg1) {
    _setProperty(arg0, arg1,this.stubContext);
  }

  /**
   * @param proxy
   * @param method
   * @param args
   * @return
   * @throws java.lang.Throwable
   */
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * Returns the client configuration context.
   * @return
   */
  public ClientConfigurationContext _getConfigurationContext() {
    return this.stubContext;
  }
  
  /**
   * Returns dynamic property. 
   * @param pName
   * @return
   */
  private static Object _getDynamicProp(QName pName, ClientConfigurationContext context) {
    Object pv1 = _getDynamicProperty(pName.toString(), context);
    String pv2 = _getStaticProperty(pName, context);
    if (pv1 != null) {
      return pv1;
    }
    return pv2;    
  }
  
  /**
   * Returns dynamic property value.
   * @param propertyName
   * @return
   */
  private static Object _getDynamicProperty(String propertyName, ClientConfigurationContext context) {
    return context.getDynamicContext().getProperty(propertyName);    
  }
  
  /**
   * Returns the value of static property.
   * @param propertyName
   * @return
   */
  private static String _getStaticProperty(QName propertyName, ClientConfigurationContext context) {
    PropertyType property = (context.getStaticContext().getRTConfig().getSinglePropertyList()).getProperty(propertyName); 
    if (property != null) {
      return property.get_value(); 
    } else {
      return null;      
    }                     
  }  

  /**
   * Sets runtime properties.
   * @param arg0
   * @param arg1
   */
  public static void _setProperty(String arg0, Object arg1, ClientConfigurationContext context) {
    if(arg0 == null) {
      throw new UnsupportedOperationException("Property name is null.");
    }
    if (persistableProps.containsKey(arg0)) {
      if (arg1 == null) {
        context.getPersistableContext().removeProperty(persistableProps.get(arg0).toString());        
      } else {
        if (PublicProperties.P_SEC_USERNAME.equals(arg0)) {
          context.getPersistableContext().setProperty(PublicProperties.C_SEC_METHOD.toString(),PublicProperties.F_SEC_METHOD_BASIC);
        }
        if (PublicProperties.P_SESSION_MAINTAIN.equals(arg0)) {
          if ("false".equalsIgnoreCase(arg1.toString())) {
            arg1 = PublicProperties.F_SESSION_METHOD_NONE;
          } else {
            arg1 = PublicProperties.F_SESSION_METHOD_HTTP; 
          }
        }
        context.getPersistableContext().setProperty(persistableProps.get(arg0).toString(),arg1);
      }
    } else if(dynamicAllowed.contains(arg0)) {
      if (arg1 == null) {
        context.getDynamicContext().removeProperty(arg0);
      } else {
        context.getDynamicContext().setProperty(arg0,arg1);
      }      
    } else {
      throw new UnsupportedOperationException("Property name '" + arg0 + "' is not supported.");
    }
  }

  /**
   * Return runtime property.
   * @param arg0
   * @return
   */
  public static Object _getProperty(String arg0, ClientConfigurationContext context) {
    if(arg0 == null) {
      throw new UnsupportedOperationException("Property name is null.");
    }
    if (persistableProps.containsKey(arg0)) {      
      String result = (String) (context.getPersistableContext().getProperty(persistableProps.get(arg0).toString()));
      if (PublicProperties.P_ENDPOINT_URL.equals(arg0)) {
        if (result == null) {
          result = context.getStaticContext().getRTConfig().getUrl();
        }
      } else if (PublicProperties.P_SESSION_MAINTAIN.equals(arg0)) {
        if (PublicProperties.F_SESSION_METHOD_NONE.equals(result)) {
          return Boolean.FALSE;
        }
        if (PublicProperties.F_SESSION_METHOD_HTTP.equals(result)) {
          return Boolean.TRUE;
        }
        return null;
      } else {
        Object key = persistableProps.get(arg0);
        if (result == null && key instanceof QName) {
          result = _getStaticProperty((QName) key,context);
        }
      }
      return result;
    } else if(dynamicAllowed.contains(arg0)) {
      Object result = _getDynamicProp(QName.valueOf(arg0), context);
      return result;
    }
    throw new UnsupportedOperationException("Property name '" + arg0 + "' is not supported.");
  }
  
  public void _flush() throws RemoteException {
    SessionInterface session = SessionInterfaceFactory.getInterface(this);
    if (session != null) {
      session.releaseServerResources();
    }
  }
  
  /**
   * Method for setting endpoint url.
   */
  public void _setEndpoint(String url) {
    this._setProperty(this.ENDPOINT_ADDRESS_PROPERTY,url);
  }  
  
  /**
   * Returns global protocol list.
   */
  public ProtocolList _getGlobalProtocols() {
    ProtocolList result = new ProtocolList();
    result.add(new MessageIdFeatureImpl(this.stubContext));
    return result;
  }

  
}
