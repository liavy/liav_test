package com.sap.engine.services.webservices.espbase.client.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Map;


public interface ConsumerHTTPTransport {
  
  /**
   * This method initiates HTTP Request-Response operation.
   * It is called when message sending will be initiated.
   * It is not directly related with the actial connection opening. It just informs the HTTP Transport implementation of call initiation.
   * @param requestMethod HTTP Request method.
   * @param config WS Client configuration.
   * @throws RemoteException
   */
  public void init(String requestMethod, ClientConfigurationContext config) throws RemoteException;
  
  /**
   * Returns the connection state of the HTTP Transport. 
   * This method is qieried if the connection to check if the http connection is open.
   * @return
   */
  public boolean isConnected();
  
  /**
   * Sets request header value.
   * @param headerName
   * @param headerValue
   */
  public void setHeader(String headerName, String headerValue);
  
  /**
   * Sets request header values.
   * @param headerName
   * @param headerValues
   */
  public void setHeader(String headerName, String[] headerValues);
  
  /**
   * Returns the current configured endpoint url.
   * @return
   */
  public String getEndpoint();
  
  /**
   * Returns list of headers that are recieved.
   * @return
   */
  public Enumeration<String> listHeaders();
  
  /**
   * Returns map containing all HTTP Headers for fast manipulations.
   * @return
   */
  public Map<String,String[]> getHeaders();
  
  /**
   * Returns http header value.
   * @param headerName
   * @return
   */
  public String[] getHeader(String headerName);
  
  /**
   * Opens and returns the request stream for message writing.
   * @return
   * @throws IOException
   */
  public OutputStream getRequestStream() throws IOException;   
  
  /**
   * Sends the request message and returns the response code of the response message.
   * @return
   * @throws IOException
   */
  public int getResponseCode() throws IOException;
  
  /**
   * Returns the response message.
   * @return
   * @throws IOException
   */
  public String getResponseMessage() throws IOException;
  
  /**
   * Returns the HTTP Response stream.
   * @return
   * @throws IOException
   */
  public InputStream getResponseStream() throws IOException;
  
  /**
   * Returns response content type.
   * @return
   * @throws IOException
   */
  public String getContentType() throws IOException;
  
  /**
   * Closes the HTTP Session.
   */
  public void closeSession();
  
  
  /**
   * Releases the used connection. 
   */
  public void releaseConnection();
  
}
