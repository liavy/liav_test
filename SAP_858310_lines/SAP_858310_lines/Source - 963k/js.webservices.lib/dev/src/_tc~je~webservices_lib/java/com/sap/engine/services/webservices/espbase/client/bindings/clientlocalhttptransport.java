package com.sap.engine.services.webservices.espbase.client.bindings;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.sap.engine.interfaces.webservices.runtime.ServletDispatcher;
import com.sap.engine.services.webservices.espbase.client.transport.local.LocalHttpServletRequest;
import com.sap.engine.services.webservices.espbase.client.transport.local.LocalHttpServletResponse;
import com.sap.engine.services.webservices.jaxm.soap.InputStreamLogger;
import com.sap.engine.services.webservices.jaxm.soap.OutputStreamLogger;

/**
 * @version 1.0
 * @author Ivaylo Zlatanov, ivaylo.zlatanov@sap.com
 * 
 */

public class ClientLocalHTTPTransport implements ConsumerHTTPTransport {

  private LocalHttpServletRequest request;

  private LocalHttpServletResponse response;

  private ClientConfigurationContext config;

  private int responseCode;

  private final boolean REQUEST = false;

  private final boolean RESPONSE = true;

  private boolean communicationState = REQUEST;
  
  private ServletDispatcher servletDispatcher;
  
  public ClientLocalHTTPTransport(){
    Properties p = new Properties();
    p.put("domain", "true");
    try{
      Context ctx = new InitialContext(p);
      servletDispatcher = (ServletDispatcher) ctx.lookup("/wsContext/WSDispatcher");
    }catch(Exception e){
      throw new RuntimeException("Cannot lookup /wsContext/WSDispatcher", e);
    }
  }

  /**
   * 
   * @param requestMethod
   * @param config
   * @throws RemoteException
   */
  public void init(String requestMethod, ClientConfigurationContext config)
      throws RemoteException {
    if (config != null) {
      this.config = config;
    } else {
      throw new IllegalArgumentException("NULL configuration passed to "
          + this.getClass().getName()
          + ".init(java.lang.String,ClientConfigurationContext) method.");
    }

    String endpointURL = PublicProperties.getEndpointURL(this.config); // reset objects rather than create new ones ?
    request = new LocalHttpServletRequest(endpointURL, requestMethod);
    request.setClientContext(config);
    response = new LocalHttpServletResponse();
    communicationState = REQUEST;
    responseCode = 0;
  }

  /**
   * @param headerName
   * @param headerValues
   */
  public void setHeader(String headerName, String[] headerValues) {
    if (communicationState == RESPONSE) {
      throw new IllegalStateException(
          "Call init() before setting request headers.");
    }
    request.setHeader(headerName, headerValues);
  }

  /**
   * @param headerName
   * @param headerValue
   */
  public void setHeader(String headerName, String headerValue) {
    if (communicationState == RESPONSE) {
      throw new IllegalStateException(
          "Call init() before setting request headers.");
    }
    request.setHeader(headerName, new String[] { headerValue });
  }

  /**
   * @return
   * @throws IOException
   */
  public OutputStream getRequestStream() throws IOException {
    OutputStream bodyOutputStream = request.getBodyOutputStream();
    OutputStream requestStream = null;

    Object outputLogger = config.getDynamicContext().getProperty(
        PublicProperties.P_REQUEST_LOG_STREAM);
    if (outputLogger != null) {
      OutputStream outputLog;
      try {
        outputLog = (OutputStream) outputLogger;
      } catch (ClassCastException cce) {
        throw new IOException(
            "OutputStream expected for REQUEST_LOGGING_PROPERTY: "
                + cce.toString());
      }
      requestStream = new OutputStreamLogger(bodyOutputStream, outputLog);
    } else {
      requestStream = bodyOutputStream;
    }
    return requestStream;
  }

  /**
   * @return
   */
  public int getResponseCode() {
    if (communicationState == REQUEST) {
      call();
    }
    return responseCode;
  }

  private void call() {
    // lookup ServletDispatcher
    try {
      // call
      servletDispatcher.doPost(request, response, this);
      communicationState = RESPONSE;
      responseCode = response.getStatus();
    } catch (Exception e) {
      throw new RuntimeException(e); // !!
    }
  }

  /**
   * @return
   * @throws IOException
   */
  public InputStream getResponseStream() throws IOException {
    if (communicationState == REQUEST) {
      call();
    }

    InputStream bodyInputStream = response.getBodyInputStream();
    InputStream responseStream = null;

    Object inputLogger = config.getDynamicContext().getProperty(
        PublicProperties.P_RESPONSE_LOG_STREAM);
    if (inputLogger != null) {
      OutputStream inputLog;
      try {
        inputLog = (OutputStream) inputLogger;
      } catch (ClassCastException cce) {
        throw new IOException(
            "OutputStream expected for RESPONSE_LOGGING_PROPERTY: "
                + cce.toString());
      }
      responseStream = new InputStreamLogger(bodyInputStream, inputLog);
    } else {
      responseStream = bodyInputStream;
    }
    return responseStream;
  }

  /**
   * @return
   */
  public boolean isConnected() {
    return request != null & response != null;
  }

  /**
   * Returns header value.
   */
  public String[] getHeader(String headerName) {
    Hashtable<String, String[]> headers = communicationState == REQUEST ? request
        .getHeaders()
        : response.getHeaders();
    String[] values = (String[]) headers.get(headerName);
    return values == null ? new String[0] : values;
  }

  /**
   * Returns hashtable with http headers.
   */
  public Hashtable<String, String[]> getHeaders() {
    return communicationState == REQUEST ? request.getHeaders() : response
        .getHeaders();
  }

  /**
   * @return
   */
  public String getEndpoint() {
    return PublicProperties.getEndpointURL(config);
  }

  public Enumeration<String> listHeaders() {
    return getHeaders().keys();
  }

  /**
   * @return
   * @throws IOException
   */
  public String getResponseMessage() throws IOException {
    if (communicationState == REQUEST) {
      call();
    }
    return response.getResponseMessage();
  }

  /**
   * @return
   * @throws IOException
   */
  public String getContentType() throws IOException {
    if (communicationState == REQUEST) {
      call();
    }
    return response.getContentType();
  }

  /**
   * @return
   */
  public void closeSession() {

  }

  @Override
  public void releaseConnection() {
    // TODO Auto-generated method stub
    
  }
 }