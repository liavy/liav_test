package com.sap.engine.services.webservices.espbase.client.bindings.httppool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;

/**
 * Class holder of a tcp connection with its host settings.
 * @author I056242
 *
 */
public class HostConnection {
  
  private boolean isUsed;
  
  private URL endpointURL;    
    
  private String protocol;
    
  private String host;
    
  private int port = -1;
  
  private String file;
    
  private String proxyHost;
    
  private int proxyPort = -1;
      
  private String proxyUser;
  
  private String proxyPass;
  
  /** Key */
  private InetSocketAddress inetAddress;  
  
  private int socketConnectionTimedOut;
  
  private List clientCertificateList;
  
  private List serverCertificateList;
  
  private boolean ignoreServerCerts;
  
  private HTTPSocket httpSocket;
    
    
  public HostConnection(URL endpointURL){
    if (endpointURL == null){
      throw new IllegalArgumentException("endpointURL can no be null.");
    }
    
    this.isUsed = true;
    
    this.endpointURL = endpointURL;        
    
    this.protocol = this.endpointURL.getProtocol();
    
    this.file = this.endpointURL.getFile();   
    
    this.host = this.endpointURL.getHost();
    
    this.port = this.endpointURL.getPort();  
  }
  
  
  
  /** 
   * Init must be called.
   * 
   * @param proxyHost
   * @param proxyPort
   * @param proxyUser
   * @param proxyPass
   */
  public void init(String proxyHost, int proxyPort, String proxyUser, String proxyPass){
    this.proxyHost = proxyHost;
    
    this.proxyPort = proxyPort;
    
    this.proxyUser = proxyUser;
    
    this.proxyPass = proxyPass;     
    
    if (proxyHost != null) {
      inetAddress = new InetSocketAddress(proxyHost, proxyPort);
    } else {
      inetAddress = new InetSocketAddress(host, port);
    }   
  }
  
  
  
  
      
  public boolean equals(Object o){
    if (inetAddress == null){
      throw new IllegalStateException("Class not initialized. Call init()");
    }
        
    if (!(o instanceof HostConnection)){
      return false;
    }
    
    HostConnection compare = (HostConnection)o; 
              
    return inetAddress.equals(compare.inetAddress);            
  }
  
  
  public int hashCode(){
    if (inetAddress == null){
      throw new IllegalStateException("Class not initialized. Call init()");
    }
    
    return inetAddress.hashCode();
  }
  
  

  
  public void adoptConnection(HostConnection adopted) throws IOException{
    HTTPSocket adoptedSocket = adopted.getHttpSocket();
    
    if (adoptedSocket.isAlive()){
      adoptedSocket.setFile(this.file);            
    }else{
      adoptedSocket = ConnectionPool.createSocket(this);
    }   
    this.setHttpSocket(adoptedSocket);
  }

  
  
  
  

  /**
   * @param endpointURL the endpointURL to set
   * @throws MalformedURLException 
   */
  public void setEndpointURL(URL endpointURL) throws MalformedURLException {           
    this.endpointURL = endpointURL;        
    
    this.protocol = this.endpointURL.getProtocol();
    
    this.host = this.endpointURL.getHost();
    
    this.port = this.endpointURL.getPort();    
    
    this.file = this.endpointURL.getFile();
    
    // preset the inet address.
    init(this.proxyHost, this.proxyPort, this.proxyUser, this.proxyPass);
  }



  /**
   * @return the endpointURL
   */
  public URL getEndpointURL() {
    return endpointURL;
  }
  
  /**
   *   
   * @return
   */
  public String getProxyHost(){
    return this.proxyHost;
  }
  
  /**
   * 
   * @return
   */
  public int getProxyPort(){
    return this.proxyPort;
  }
  
  /**
   * 
   * @return
   */
  public String getProxyUser(){
    return this.proxyUser;
  }
  
  
  public String getProxyPass(){
    return this.proxyPass;
  }


  /**
   * @param clientCertificateList the clientCertificateList to set
   */
  public void setClientCertificateList(List clientCertificateList) {
    this.clientCertificateList = clientCertificateList;
  }


  /**
   * @return the clientCertificateList
   */
  public List getClientCertificateList() {
    return clientCertificateList;
  }


  /**
   * @param serverCertificateList the serverCertificateList to set
   */
  public void setServerCertificateList(List serverCertificateList) {
    this.serverCertificateList = serverCertificateList;
  }


  /**
   * @return the serverCertificateList
   */
  public List getServerCertificateList() {
    return serverCertificateList;
  }


  /**
   * @param ignoreServerCerts the ignoreServerCerts to set
   */
  public void setIgnoreServerCerts(boolean ignoreServerCerts) {
    this.ignoreServerCerts = ignoreServerCerts;
  }


  /**
   * @return the ignoreServerCerts
   */
  public boolean isIgnoreServerCerts() {
    return ignoreServerCerts;
  }


  /**
   * @param socketConnectionTimedOut the socketConnectionTimedOut to set
   */
  public void setSocketConnectionTimeOut(int socketConnectionTimedOut) {
    this.socketConnectionTimedOut = socketConnectionTimedOut;
  }


  /**
   * @return the socketConnectionTimedOut
   */
  public int getSocketConnectionTimedOut() {
    return socketConnectionTimedOut;
  }
  
  


  /**
   * @param isUsed the isUsed to set
   */
  void setUsed(boolean isUsed) {
    this.isUsed = isUsed;
  }


  /**
   * @return the isUsed
   */
  boolean isUsed() {
    return isUsed;
  }

  /**
   * 
   * @return true if the connection is https
   */
  public boolean isSecure(){
    return protocol.equalsIgnoreCase("https"); 
  }
     

  /**
   * @return the file
   */
  public String getFile() {
    return file;
  }



  /**
   * @param httpSocket the httpSocket to set
   */
  public void setHttpSocket(HTTPSocket httpSocket) {
    this.httpSocket = httpSocket;
  }



  /**
   * @return the httpSocket
   */
  public HTTPSocket getHttpSocket() {
    return httpSocket;
  }
  
}
