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
package com.sap.engine.services.webservices.espbase.client.api.impl;

import com.sap.engine.services.webservices.espbase.client.api.HTTPControlInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;

import javax.xml.rpc.Stub;

import java.io.OutputStream;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Implementation of HTTP Control interface.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class HTTPControlInterfaceImpl implements HTTPControlInterface {

  private Stub port;
  private Hashtable table;
  private ArrayList arrayList = new ArrayList();
  private Enumeration enumeration;
  
  public void setNonProxyHosts(String nonProxyHosts) {
    // Do nothing
  }

  public HTTPControlInterfaceImpl(Stub port) {
    this.port = port;
    table = new Hashtable();
    port._setProperty(BaseGeneratedStub.HTTP_REQUEST_HEADERS,table);
    table = new Hashtable();
    port._setProperty(BaseGeneratedStub.HTTP_RESPONSE_HEADERS,table);
    table = null;
  }

  public void setSocketTimeout(long milliseconds) {
    this.port._setProperty("socketTimeout",String.valueOf(milliseconds));
  }

  public void setSocketConnectionTimeout(long milliseconds) {
  }

  public void setHTTPProxy(String proxyHost, int proxyPort) {
    this.port._setProperty("javax.xml.rpc.http.proxyhost",proxyHost);
    this.port._setProperty("javax.xml.rpc.http.proxyport",String.valueOf(proxyPort));
  }

  public void setHTTPProxyUserPass(String proxyUser, String proxyPass) {
    this.port._setProperty("proxyUser",proxyUser);
    this.port._setProperty("proxyPassword",proxyPass);
  }

  public void setKeepAlive(boolean keepAlive) {
    if (keepAlive) {
      this.port._setProperty("keepAliveStatus",String.valueOf(keepAlive));
    } else {
      this.port._setProperty("keepAliveStatus","");
    }
  }

  public void setCompressResponse(boolean compressResponse) {
    if (compressResponse) {
      this.port._setProperty("compressResponse",String.valueOf(compressResponse));
    } else {
      this.port._setProperty("compressResponse","");
    }
  }

  public void addRequesHeader(String headerName, String headerValue) {
    if (headerName == null || headerName.length() == 0) {
      throw new IllegalArgumentException("HeaderName passed to "+HTTPControlInterface.class.getName()+".addRequestHeader(String headerName, String headerValue) method is NULL.");
    }
    if (headerValue == null || headerValue.length() == 0) {
      throw new IllegalArgumentException("HeaderValue passed to "+HTTPControlInterface.class.getName()+".addRequestHeader(String headerName, String headerValue) method is NULL.");
    }
    table = (Hashtable) port._getProperty(BaseGeneratedStub.HTTP_REQUEST_HEADERS); //This property contains table with the request headers
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

  public String[] getResponseHeader(String headerName) {
    if (headerName == null || headerName.length() == 0) {
      throw new IllegalArgumentException("HeaderName passed to "+HTTPControlInterface.class.getName()+".getResponseHeader(String headerName) method is NULL.");
    }
    table = (Hashtable) port._getProperty(BaseGeneratedStub.HTTP_REQUEST_HEADERS); //This property contains table with the request headers
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

  public String[] getResponseHeaderNames() {
    table = (Hashtable) port._getProperty(BaseGeneratedStub.HTTP_REQUEST_HEADERS); //This property contains table with the request headers
    if (table != null) {
      arrayList.clear();
      enumeration = table.keys();
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

  public void setEndpointURL(String url) {
    this.port._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, url );
  }

  public String getEndpointURL() {
    return (String) this.port._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY);
  }
  
  
  /**
   * Starts http log of request and response using the passed streams.
   * @param requestLog
   * @param responseLog
   */
  @Deprecated
  public void startLogging(OutputStream requestLog, OutputStream responseLog) {
    ((BaseGeneratedStub) this.port)._startLogging(requestLog,responseLog);
  }
  
  /**
   * Stops the HTTP logging feature.
   */
  @Deprecated
  public void stopLogging() {
    ((BaseGeneratedStub) this.port)._stopLogging();
  }

  public void enableChunkedRequest(boolean chunked) {
  }
   
}
