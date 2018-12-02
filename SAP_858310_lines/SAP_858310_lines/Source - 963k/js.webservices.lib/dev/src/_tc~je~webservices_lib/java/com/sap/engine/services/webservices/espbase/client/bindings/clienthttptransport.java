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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.services.webservices.espbase.client.bindings.httppool.ConnectionPool;
import com.sap.engine.services.webservices.espbase.client.bindings.httppool.HostConnection;
import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.Cookie;

/**
 * Class representin web services client HTTP Transport.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class ClientHTTPTransport extends AbstractHTTPTransport implements ConsumerHTTPTransport {

  public static final String REQUEST_METHOD_POST = "POST";
  public static final String REQUEST_METHOD_GET = "GET";    

  private String requestMethod;  
  private Hashtable<String,String[]> headers;
  
  /** Socket pool key */
  private HostConnection hostConfig;
  
  private HTTPSocket httpSocket;
  
  private static byte[] basic = "Basic ".getBytes(); //$JL-I18N$
  private static byte SC = (byte) ':';
        
  public ClientHTTPTransport() {
    this.headers = new Hashtable<String,String[]>();
  }  
  
  /**
   * Initializes the http socket. It does not open http connection yet.
   * @param requestMethod
   * @param config
   * @throws Exception
   */
  public void init(String requestMethod, ClientConfigurationContext config) throws RemoteException {
    headers.clear();
    this.requestMethod = requestMethod;
    if (config != null) {
      this.config = config;
    } else {
      throw new IllegalArgumentException("NULL configuration passed to "+this.getClass().getName()+".init(java.lang.String,ClientConfigurationContext) method.");
    }
  }
  
  /**
   * Creates new HTTP Socket for message transportation. If keep alive is set and the old HTTPSocket is alive the connectiol will be reused.
   * @param endpoint
   * @param requestMethod
   * @return
   * @throws Exception
   */
  private void createHTTPSocket() throws IOException {
    // Gets the required endpoint url
    URL url = getEndpointURL();

    hostConfig = new HostConnection(url);

    setProxyProperties(hostConfig, url);

    setSocketConnectionTimeOut(hostConfig);
    
    setConnectionSertificats(hostConfig);

    ConnectionPool.initConnectionFromPool(hostConfig);
    
    httpSocket = hostConfig.getHttpSocket();

    httpSocket.setRequestMethod(requestMethod);

    // Compress response settings
    setCompressedResponseProperty(httpSocket);
    
    // Set keepAlive property
    setKeepAliveProperty(httpSocket);

    setLogProperties(httpSocket);

    // Set socket timeout property
    setSocketTimeout(httpSocket);

    // Handles basic security
    setSecurityProperties(httpSocket, config);
  }
  
  /**
   * Call this to release open inputStream/OutputStream and sockets.
   */
  public void closeSession() {
    if (httpSocket != null) {
      try {
        httpSocket.disconnect();
        httpSocket = null;
      } catch (IOException x) {
        httpSocket = null;
      } finally {
        httpSocket = null;
      }
    }
  }  
  
  /**
   * Returns response content type.
   * @return
   * @throws Exception
   */
  public String getContentType() throws IOException {
    if (this.httpSocket == null) {
      throw new IllegalStateException("HTTP Connection is not established ! Call on method .getContentType() is not possible !");
    }
    return this.httpSocket.getContentType();
  }
  
  /**
   * Returns endpoint URL.
   * @return
   */
  public String getEndpoint() {
    return PublicProperties.getEndpointURL(config);
  }  

  /**
   * Returns header value.
   */
  public String[] getHeader(String headerName) {
    return (String[]) headers.get(headerName);
  }

  /**
   * Returns hashtable with http headers.
   */
  public Hashtable<String,String[]> getHeaders() {
    return this.headers;
  }
  
  /**
   * Returns request stream. Must set headers before call get output stream.
   * Call flush to this stream before getting response input stream.
   */
  public OutputStream getRequestStream() throws IOException {
    createHTTPSocket();
    // Session Feature implementation
    String sessionMethod = getPropertyValue(PublicProperties.C_SESSION_METHOD);
      if (sessionMethod == null) {
        // The default behaviour
        sessionMethod = PublicProperties.F_SESSION_METHOD_HTTP; 
      }
      if (PublicProperties.F_SESSION_METHOD_HTTP.equals(sessionMethod)) { //httpCookies
        boolean maintainSession = true;
        if (!isTrue((getPersistableProperty(PublicProperties.C_SESSION_MAINTAIN)))) {
          maintainSession = false;
          setPersistableProperty(PublicProperties.C_SESSION_MAINTAIN,PublicProperties.F_SESSION_MAINTAIN_TRUE);
        }        
        Object cookieObject = getDynamicProperty(PublicProperties.F_SESSION_COOKIE);
        if (cookieObject != null) {
          ArrayList cookies = (ArrayList) cookieObject;
          for (int i=0; i<cookies.size(); i++) {
            String line = Cookie.getAsRequestString((Cookie) cookies.get(i));
            httpSocket.setHeader("Cookie",line);
          }
        if (maintainSession == false) { // Session is to be closed - release the session cookies
            removeDynamicProperty(PublicProperties.F_SESSION_COOKIE);
            httpSocket.setHeader(PublicProperties.C_SESSION_MAINTAIN.getLocalPart(), PublicProperties.F_SESSION_MAINTAIN_FALSE);
          }
        }
      }
    Enumeration headerkeys = headers.keys();
    while (headerkeys.hasMoreElements()) { // HTTP Headers
      String headerName = (String) headerkeys.nextElement();
      String[] headerValue = (String[]) headers.get(headerName);
      for (int i=0; i<headerValue.length; i++) {
        httpSocket.setHeader(headerName, headerValue[i]);
      }
    }
    return httpSocket.getOutputStream();
  }
    
  /**
   * Returns response code of HTTP Response.
   */
  public int getResponseCode() throws IOException {
    return httpSocket.getResponseCode();
  }  
  
  /**
   * Returns response input stream.
   */
  public InputStream getResponseStream() throws IOException {
    headers.clear();
    Enumeration answerheaders = httpSocket.getHeaderNames();
    while (answerheaders.hasMoreElements()) {
      String headerName = (String) answerheaders.nextElement();
      String[] headerValue = httpSocket.getHeader(headerName);
      headers.put(headerName, headerValue);
    }
       String sessionMethod = (String) getPropertyValue(PublicProperties.C_SESSION_METHOD);
      if (sessionMethod == null) {
        sessionMethod = PublicProperties.F_SESSION_METHOD_HTTP;
      }
      if (PublicProperties.F_SESSION_METHOD_HTTP.equals(sessionMethod)) {
        boolean maintainSession = true;
        if (PublicProperties.F_SESSION_MAINTAIN_FALSE.equals(getPersistableProperty(PublicProperties.C_SESSION_MAINTAIN))) {      
          maintainSession = false;
          setPersistableProperty(PublicProperties.C_SESSION_MAINTAIN, PublicProperties.F_SESSION_MAINTAIN_TRUE);
        }
        String[] cookies = httpSocket.getHeader("Set-Cookie");
        if (cookies != null && cookies.length > 0) { // Server returns cookie
          // Get current cookies
        Object oldCookies = getDynamicProperty(PublicProperties.F_SESSION_COOKIE);
        // Special handling for ABAP cookie. please double check !!!
          for (int i = 0; i < cookies.length; i++) {
          if (cookies[i].indexOf("sap-contextid=0;") != -1 || cookies[i].endsWith("sap-contextid=0")) {
            oldCookies = null;             
                  break;
                }
            ArrayList cookieLine = Cookie.readCookies(cookies[i]);
            for (int j=0; j<cookieLine.size(); j++) {
            if (oldCookies == null) {
              oldCookies = new ArrayList();
            }
            Cookie.updateCookies(cookieLine,(ArrayList) oldCookies);
          }
            }
        if (maintainSession && oldCookies != null) { // Server maintains session
          setDynamicProperty(PublicProperties.F_SESSION_COOKIE, oldCookies);
          }
        }
      }
    return httpSocket.getInputStream();
  }
  
  /**
   * Returns the HTTP response message.
   * @return
   * @throws Exception
   */
  public String getResponseMessage() throws IOException {
    return this.httpSocket.getResponseMessage();
  }
  
  /**
   * Returns enumeration of all http headers.
   * @return
   */  
  public Enumeration<String> listHeaders() {
    return headers.keys();
  }
    
  /**
   * Sets HTTP Header value.
   * @param headerName
   * @param headerValues
   */
  public void setHeader(String headerName, String[] headerValues) {
    headers.put(headerName, headerValues);
  }

  /**
   * Sets HTTP Header value.
   * @param headerName
   * @param headerValue
   */
  public void setHeader(String headerName, String headerValue) {
    headers.put(headerName, new String[] {headerValue});
  }
  
  
  /**
   * Returns true if the connection is open. (HTTPSocket is created).
   * @return
   */
  public boolean isConnected() {
    if  (this.httpSocket != null) {
      return true;
    }
    return false;
  }
  
  /** 
   * Return the used connection to the pool.
   */
  public void releaseConnection() {
    if (this.httpSocket != null) {      
      hostConfig.setHttpSocket(this.httpSocket);
      try {
        ConnectionPool.returnConnection(hostConfig);
      } catch (IOException e) {
        //$JL-EXC$        
      }finally{      
        this.httpSocket = null;
        this.hostConfig = null;
      }
    }

  }

  protected void  finalize() {
    try {
      super.finalize();
    } catch (Throwable x) {
      //$JL-EXC$
    }    
    
    //Return the connection to the pool.
    if ((this.httpSocket != null) && (this.hostConfig != null)) {
      hostConfig.setHttpSocket(this.httpSocket);
      try {
        ConnectionPool.returnConnection(hostConfig);
      } catch (IOException e) {

      }finally{
        //$JL-EXC$
        this.httpSocket = null;
        this.hostConfig = null;
      }      
    }
  }
    
  public static String encodeAuth(String userName, String password) {
    if (userName == null) {
      userName = "";
    }
    if (password == null) {
      password = "";
    }
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(4 * (userName.length() + password.length() + 1) / 3 + 9);
      bos.write(userName.getBytes()); //$JL-I18N$
      bos.write(SC);
      bos.write(password.getBytes()); //$JL-I18N$
      byte[] result = com.sap.engine.lib.xml.util.BASE64Encoder.encodeN(bos.toByteArray());
      bos.reset();
      bos.write(basic); //$JL-I18N$
      bos.write(result);
      return bos.toString();
    } catch (IOException ex) {
      return "";
    }
  }
  
}
