/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.api.HTTPControlInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;

/**
 * 
 * @version 1.0 (2006-1-20)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class HTTPControlInterfaceNYImpl implements HTTPControlInterface {
    
  private ClientConfigurationContext clientContext;  
      
  /**
   * Default constructror. Pass the client context as parameter.
   */
  public HTTPControlInterfaceNYImpl(ClientConfigurationContext clientContext) {
    this.clientContext = clientContext;    
    ConfigurationContext configurationContext = this.clientContext.getDynamicContext();
    if (configurationContext.getProperty(PublicProperties.P_HTTP_REQUEST_HEADERS) == null) {
      Hashtable table = new Hashtable();
      configurationContext.setProperty(PublicProperties.P_HTTP_REQUEST_HEADERS,table);
    }
    if (configurationContext.getProperty(PublicProperties.P_HTTP_RESPONSE_HEADERS) == null) {
      Hashtable table = new Hashtable();
      configurationContext.setProperty(PublicProperties.P_HTTP_RESPONSE_HEADERS,table);
    }
  }
  
  public void setNonProxyHosts(String nonProxyHosts) {
    QName nonProxyHostsProperty = new QName(PublicProperties.TRANSPORT_BINDING_FEATURE,PublicProperties.TRANSPORT_BINDING_NON_PROXY_HOSTS);
    if (nonProxyHosts == null) {
      this.clientContext.getPersistableContext().removeProperty(nonProxyHostsProperty.toString());
    } else {
      this.clientContext.getPersistableContext().setProperty(nonProxyHostsProperty.toString(),nonProxyHosts);
    }
  }  

  /**
   * Sets HTTP Socket timeout. O means infinity.
   * @param milliseconds
   */
  public void setSocketTimeout(long timeout) {
    PublicProperties.setSocketTimeout(timeout, clientContext);
  }

  public void setSocketConnectionTimeout(long milliseconds) {
    ConfigurationContext persistableContext = this.clientContext.getPersistableContext();
    persistableContext.setProperty(PublicProperties.C_CONNECTION_TIMEOUT_QNAME.toString(), String.valueOf(milliseconds));
  }

  /**
   * Sets HTTP Connection Proxy.
   * @param proxyHost
   * @param proxyPort
   */
  public void setHTTPProxy(String proxyHost, int proxyPort) {
    ConfigurationContext persistableContext = this.clientContext.getPersistableContext();    
    persistableContext.setProperty(PublicProperties.C_PROXY_HOST.toString(),proxyHost);    
    persistableContext.setProperty(PublicProperties.C_PROXY_PORT.toString(),String.valueOf(proxyPort));
  }

  /**
   * Sets HTTP Proxy basic authentication user and password.
   * @param proxyUser
   * @param proxyPass
   */
  public void setHTTPProxyUserPass(String proxyUser, String proxyPass) {
    ConfigurationContext persistableContext = this.clientContext.getPersistableContext();
    persistableContext.setProperty(PublicProperties.C_PROXY_USER.toString(),proxyUser);    
    persistableContext.setProperty(PublicProperties.C_PROXY_PASS.toString(),proxyPass);    
  }

  /**
   * Sets connection keep alive preference.
   * @param keepAlive
   */
  public void setKeepAlive(boolean keepAlive) {
    ConfigurationContext persistableContext = this.clientContext.getPersistableContext();
    persistableContext.setProperty(PublicProperties.C_KEEP_ALIVE.toString(),String.valueOf(keepAlive));        
  }
  
  public void enableChunkedRequest(boolean chunked) {
    QName chunkedProperty = new QName(PublicProperties.TRANSPORT_BINDING_FEATURE,PublicProperties.TRANSPORT_BINDING_CHUNKED_REQUEST);
    ConfigurationContext persistableContext = this.clientContext.getPersistableContext();
    persistableContext.setProperty(chunkedProperty.toString(),String.valueOf(chunked));            
  }

  /**
   * Sets compress response connection preference.
   * @param compressResponse
   */
  public void setCompressResponse(boolean compressResponse) {
    ConfigurationContext persistableContext = this.clientContext.getPersistableContext();
    persistableContext.setProperty(PublicProperties.C_COMPRESS_RESPONSE.toString(),String.valueOf(compressResponse));            
  }

  /**
   * This method can be used to add HTTP Header to the HTTP request.
   * @param headerName
   * @param headerValue
   */
  public void addRequesHeader(String headerName, String headerValue) {
    if (headerName == null || headerName.length() == 0) {
      throw new IllegalArgumentException("HeaderName passed to "+HTTPControlInterface.class.getName()+".addRequestHeader(String headerName, String headerValue) method is NULL.");
    }
    if (headerValue == null || headerValue.length() == 0) {
      throw new IllegalArgumentException("HeaderValue passed to "+HTTPControlInterface.class.getName()+".addRequestHeader(String headerName, String headerValue) method is NULL.");
    }
    ConfigurationContext dynamicContext = this.clientContext.getDynamicContext();
    Hashtable table = (Hashtable) dynamicContext.getProperty(PublicProperties.P_HTTP_REQUEST_HEADERS); //This property contains table with the request headers
    if (table != null) {
      String[] headerValues = (String[]) table.get(headerName);
      if (headerValues == null) {
        // the header has no values set - add the first one in the list
        table.put(headerName,new String[] {headerValue});
      } else {
        // adds header to the header value list
        String[] newArray = new String[headerValues.length+1];
        System.arraycopy(headerValues,0,newArray,0,headerValues.length);
        newArray[headerValues.length] = headerValue;
        table.put(headerName,newArray);
      }
      table = null;
    }    
  }

  /**
   * Returns response HTTP Header value.
   * @param headerName
   * @return
   */
  public String[] getResponseHeader(String headerName) {
    if (headerName == null || headerName.length() == 0) {
      throw new IllegalArgumentException("HeaderName passed to "+HTTPControlInterface.class.getName()+".getResponseHeader(String headerName) method is NULL.");
    }
    ConfigurationContext dynamicContext = this.clientContext.getDynamicContext();
    Hashtable table = (Hashtable) dynamicContext.getProperty(PublicProperties.P_HTTP_RESPONSE_HEADERS); //This property contains table with the request headers
    if (table != null) {
      String[] headerValues = (String[]) table.get(headerName);
      table = null;
      if (headerValues == null) {
        return new String[0];
      } else {
        return headerValues;
      }
    }
    return new String[0];
  }

  /**
   * Sets Endpoint Url.
   * @param url
   */
  public void setEndpointURL(String url) {
    ConfigurationContext persistableContext = this.clientContext.getPersistableContext();
    persistableContext.setProperty(PublicProperties.P_ENDPOINT_URL,url);            
  }
  
  /**
   * Sets Endpoint Url.
   * @param url
   */
  public String getEndpointURL() {
    return PublicProperties.getEndpointURL(this.clientContext);
  }
  

  /**
   * Rreturns list of response header names.
   * @return
   */
  public String[] getResponseHeaderNames() {
    ConfigurationContext dynamicContext = this.clientContext.getDynamicContext();
    Hashtable table = (Hashtable) dynamicContext.getProperty(PublicProperties.P_HTTP_RESPONSE_HEADERS); //This property contains table with the request headers
    
    if (table != null) {
      ArrayList arrayList = new ArrayList();  
      Enumeration enumeration = table.keys();
      while (enumeration.hasMoreElements()) {
        String headerName = (String) enumeration.nextElement();
        arrayList.add(headerName);
      }
      enumeration = null;
      table = null;
      String[] result = new String[arrayList.size()];
      arrayList.toArray(result);
      arrayList.clear();
      return result;
    }
    return new String[0];
  }
  
  /**
   * Starts http log of request and response using the passed streams.
   * @param requestLog
   * @param responseLog
   */
  @Deprecated
  public void startLogging(OutputStream requestLog, OutputStream responseLog) {
    if (requestLog == null) {
      this.clientContext.getDynamicContext().removeProperty(PublicProperties.P_REQUEST_LOG_STREAM);
    } else {
      this.clientContext.getDynamicContext().setProperty(PublicProperties.P_REQUEST_LOG_STREAM, requestLog);
    }
    if (responseLog == null) {
      this.clientContext.getDynamicContext().removeProperty(PublicProperties.P_RESPONSE_LOG_STREAM);
    } else {
      this.clientContext.getDynamicContext().setProperty(PublicProperties.P_RESPONSE_LOG_STREAM, responseLog);
    }
  }
  
  /**
   * Stops the HTTP logging feature.
   */
  @Deprecated
  public void stopLogging() {
    this.clientContext.getDynamicContext().removeProperty(PublicProperties.P_REQUEST_LOG_STREAM);
    this.clientContext.getDynamicContext().removeProperty(PublicProperties.P_RESPONSE_LOG_STREAM);    
  }

}
