package com.sap.engine.services.webservices.espbase.client.bindings.httppool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;

/**
 * ConnectionPool for reusing the tcp connection.
 * 
 * The SSL connections are not pooled, only the plain http
 * connections are reused.
 * 
 * @author I056242
 *
 */
public class ConnectionPool {
     
 private static final int MAX_POOL_SIZE = 20; 
 
 protected static LinkedList<HostConnection> socketPoolQueue = new LinkedList<HostConnection>();
  
 static{
   new PoolWorker().start();
 }
      
 /**
  * Get pooled connection based on a given host.
  * @param host
  * @return
  * @throws IOException
  */
 public static void initConnectionFromPool(HostConnection host) throws IOException {

    // Switch off ssl connection pooling for now.
    if (host.isSecure()) {
      HTTPSocket secureSocket = createSocket(host);
      host.setHttpSocket(secureSocket);      
    }

    HostConnection pooledConnection = getConnection(host);    
        
    if (pooledConnection != null) {
      // Adopt the connection - set the socket to the current host configuration.
      host.adoptConnection(pooledConnection);
    } else {
      host.setHttpSocket(createSocket(host));
    }    
  }  
 
 
 
 /**
  * Iterates through a pool to find a correct connection. 
  * @param host
  * @return
  */
 private static HostConnection getConnection(HostConnection host){   
   HostConnection pooled = null;    
   // Synchronized in case used from a random location.
   synchronized (socketPoolQueue) {
     for(int i = 0; i< socketPoolQueue.size(); i++){
       HostConnection singleConnection = socketPoolQueue.get(i);
       
       //Pooled connection found based on the host configuration.
       if (host.equals(singleConnection)){
         pooled = singleConnection;
         
         // Pooled connection found - remove it from the pool.
         socketPoolQueue.remove(i);      
         break;
       }      
     }    
   }   
   
   return pooled;
 }
  
  /**
   * Returns a connection to the pool along with its host settings.
   * @param host
   * @throws IOException
   */
  public static void returnConnection(HostConnection host) throws IOException {    
    // Switch off ssl connection pooling for now.
    if (host.isSecure()) {
      host.getHttpSocket().disconnect();
      return;
    }
    
    // Not united with the upper case, cause the first one'll be removed soon.
    // Remove connection closed by the server.
    if (!host.getHttpSocket().isKeepAlive()){
      host.getHttpSocket().disconnect();
      return;
    }
            
    // Mark the connection as used one.
    host.setUsed(true);
            
    HTTPSocket toRelease = null;    
    synchronized (socketPoolQueue) {
      if (socketPoolQueue.size() < MAX_POOL_SIZE){
        socketPoolQueue.add(host);        
      }else{
        //Release the LRU connection.
        HostConnection lruConnection = socketPoolQueue.poll();
        
        toRelease = lruConnection.getHttpSocket();
                        
        // add the retuned to the end of the queue
        socketPoolQueue.add(host);  
      }
    }
    
    if (toRelease != null){
      toRelease.disconnect();
    }    
  }
    
    
    
  /**
   * Create socket from a given host configuration.
   * @param host
   * @return
   */
  public static HTTPSocket createSocket(HostConnection host){       
    HTTPSocket httpSocket = new HTTPSocket(host.getEndpointURL());
    
    String proxyHost = host.getProxyHost();
    
    int proxyPort = host.getProxyPort();
    
    String proxyUser = host.getProxyUser();
    
    String proxyPass = host.getProxyPass();
    
    httpSocket.setProxy(proxyHost, proxyPort, proxyUser, proxyPass);
    
    httpSocket.setClientCertificateList(host.getClientCertificateList());
    
    httpSocket.setServerCertificateList(host.getServerCertificateList());
    
    httpSocket.setIgnoreServerCertificates(host.isIgnoreServerCerts());
    
    httpSocket.setSocketConnectionTimeout(host.getSocketConnectionTimedOut());
    
    return httpSocket;
  }
  
  /**
   * Clear the content of the pool. Exposed only for test purposes.
   */
  public static void clearPool() {
    synchronized (socketPoolQueue) {
      socketPoolQueue.clear();
    }
  }
  
  /**
   * Get the pool size. Exposed only for test purposes.
   * @return
   */
  public static int getPoolSize(){
    synchronized (socketPoolQueue) {
     return socketPoolQueue.size();
    }
  }
  
  
  
  

  /**
   * Thread worker to discard LRU connection from the pool.
   *     
   * @author I056242
   *
   */
  static class PoolWorker extends Thread {
    
    public PoolWorker(){
      //Daemonize the thread to prevent endless jvm execution.
      setDaemon(true);
    }
        
    public void run(){
      
      while (true) {
        List<HostConnection> discardedConnections = new ArrayList<HostConnection>();
        synchronized (socketPoolQueue) {                        
          for (int i = 0; i < socketPoolQueue.size(); i++) {
            HostConnection singleConnection = socketPoolQueue.get(i);
            if (singleConnection.isUsed()) {
              // The connection is used now - set it to not used.
              singleConnection.setUsed(false);
            } else {
              // Remove the unused connection.
              socketPoolQueue.remove(i);
              // Collect the discarded connections.                
              discardedConnections.add(singleConnection);               
            }
          }
        }//synchronized
        
        //Disconnect the discarded connections.
        for (int y = 0; y < discardedConnections.size(); y++){
          HostConnection discardedConnection = discardedConnections.get(y);
          try {
            discardedConnection.getHttpSocket().disconnect();
          } catch (IOException e) {
            // $JL-EXC$
          }         
        }
                               
        try {
          //sleep for another 2 minutes
          sleep(120000);
        } catch (InterruptedException e) {
          //$JL-EXC$
        }     
      }                       
    }    
  }//PoolWorker
}
