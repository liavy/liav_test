package com.sap.engine.services.webservices.espbase.client.api;

import java.io.OutputStream;

/**
 * HTTP Protocol control interface by web service proxy.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface HTTPControlInterface {
  
  public void setNonProxyHosts(String nonProxyHosts);

  /**
   * Sets HTTP Socket timeout. O means infinity.
   * @param milliseconds
   */
  public void setSocketTimeout(long milliseconds);

  /**
   * Sets HTTP Socket connection timeout. O means infinity.
   * @param milliseconds
   */
  public void setSocketConnectionTimeout(long milliseconds);

  /**
   * Sets HTTP Connection Proxy.
   * @param proxyHost
   * @param proxyPort
   */
  public void setHTTPProxy(String proxyHost, int proxyPort);

  /**
   * Sets HTTP Proxy basic authentication user and password.
   * @param proxyUser
   * @param proxyPass
   */
  public void setHTTPProxyUserPass(String proxyUser, String proxyPass);

  /**
   * Sets connection keep alive preference.
   * @param keepAlive
   */
  public void setKeepAlive(boolean keepAlive);

  /**
   * Sets compress response connection preference.
   * @param compressResponse
   */
  public void setCompressResponse(boolean compressResponse);

  /**
   * This method can be used to add HTTP Header to the HTTP request.
   * @param headerName
   * @param headerValue
   */
  public void addRequesHeader(String headerName, String headerValue);

  /**
   * Returns response HTTP Header value.
   * @param headerName
   * @return
   */
  public String[] getResponseHeader(String headerName);

  /**
   * Sets Endpoint Url.
   * @param url
   */
  public void setEndpointURL(String url);
  
  /**
   * Returns the endpoint URL.
   * @return
   */
  public String getEndpointURL();

  /**
   * Rreturns list of response header names.
   * @return
   */
  public String[] getResponseHeaderNames();
  
  /**
   * Starts http log of request and response using the passed streams.
   * @param requestLog
   * @param responseLog
   */
  @Deprecated
  public void startLogging(OutputStream requestLog, OutputStream responseLog);
  
  /**
   * Stops the HTTP logging feature.
   */
  @Deprecated
  public void stopLogging();
  /**
   * Enalbes and disables chunked requests. By default the request is chunked.
   * @param chunked true to enable chunked requests, false to disable them.
   */
  public void enableChunkedRequest(boolean chunked);
}
