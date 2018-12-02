package com.sap.engine.services.webservices.espbase.client.bindings;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.client.bindings.httppool.HostConnection;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;

public class AbstractHTTPTransport {
  
  protected ClientConfigurationContext config;
  

  protected static boolean useProxyForSpecificAddress(String address, String excludeList) {
    address = address.toLowerCase(Locale.ENGLISH).trim();
    if (excludeList == null || excludeList.trim().length() == 0) {
      return true;
    }
    StringTokenizer tokenizer = new StringTokenizer(excludeList, ";,| ", false);
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken().trim().toLowerCase(Locale.ENGLISH);
      if (match(token, 0, address, 0)) {
        return false;
      }
    }
    return true;
  }

  private static boolean match(String pattern, int patternStart, String string, int stringStart) {
    for (int s = stringStart, p = patternStart; ; ++p, ++s) {
      boolean sEnd = (s >= string.length());
      boolean pEnd = (p >= pattern.length());
      if (sEnd && pEnd) {
        return true;
      }
      if (pEnd) {
        return false;
      }
      if (sEnd) {
        if (pattern.charAt(p) == '*') {
          s--;//we can catch a pattern ending with ****
          continue;
        }
  
        return false;
      }
      if (pattern.charAt(p) == '?') {
        continue;
      }
      if (pattern.charAt(p) == '*') {
        ++p;
        pEnd = (p >= pattern.length());
        if (pEnd) {
          return true;
        }
        for (int i = s; i < string.length(); ++i) {
          if (match(pattern, p, string, i)) {
            return true;
          }
        }
        return false;
      }
      if (pattern.charAt(p) != string.charAt(s)) {
        return false;
      }
    }
  }

  public AbstractHTTPTransport() {
    super();
  }

  /**
   * Handles basic security properties.
   * @param socket
   * @param context
   */
  protected void setSecurityProperties(HTTPSocket socket, ClientConfigurationContext context) {
    String authenticationMethod = PublicProperties.getGlobalPersistableProperty(PublicProperties.C_SEC_METHOD,context); 
    String userName = null;
    String password = null;
    if (authenticationMethod != null) {
      // Old properties
      if (PublicProperties.F_SEC_METHOD_BASIC.equals(authenticationMethod)) {
        userName = getPersistableProp(PublicProperties.C_SEC_USERNAME);
        password = getPersistableProp(PublicProperties.C_SEC_PASSWORD);
        // Maps to the new authentication method property
        authenticationMethod = PublicProperties.AUTHENTICATION_METHOD_BASIC;
      }
    } else {
      authenticationMethod = PublicProperties.getGlobalPersistableProperty(PublicProperties.AUTHENTICATION_METHOD_QNAME,context);
      // New properties
      if (PublicProperties.AUTHENTICATION_METHOD_BASIC.equals(authenticationMethod)) {
        userName = getPersistableProp(PublicProperties.AUTHENTICATION_METHOD_BASIC_USER_QNAME);
        password = getPersistableProp(PublicProperties.AUTHENTICATION_METHOD_BASIC_PASS_QNAME);
      }
      if (authenticationMethod == null) {
        authenticationMethod = PublicProperties.AUTHENTICATION_METHOD_NONE;
      }            
    }
    if (PublicProperties.AUTHENTICATION_METHOD_NONE.equals(authenticationMethod)) {
      // Authentication is turned off
      return;
    }
    if (PublicProperties.AUTHENTICATION_METHOD_BASIC.equals(authenticationMethod)) {
      if (userName != null) {
        socket.setHeader("Authorization", ClientHTTPTransport.encodeAuth(userName, password));       
      }
    } else { // If it is not basic then it is client certificate
      List clientCertificateList = (List) getDynamicProperty(PublicProperties.CLIENT_CERTIFICATE_LIST.toString());
      if (clientCertificateList != null) {
        socket.setClientCertificateList(clientCertificateList);
      }
      String ignoreServerCerts = getPersistableProp(PublicProperties.SERVER_IGNORE_CERTS);
      if (isTrue(ignoreServerCerts)) {
        socket.setIgnoreServerCertificates(true);            
      } else {
        socket.setIgnoreServerCertificates(false);
        List serverCertificateList = (List) getDynamicProperty(PublicProperties.SERVER_CERTIFICATE_LIST);
        if (serverCertificateList != null) {
          socket.setServerCertificateList(serverCertificateList);
        }
      }
    }          
  }
  
  
  protected void setConnectionSertificats(HostConnection hostConfig) {
    // If it is not basic then it is client certificate
    List clientCertificateList = (List) getDynamicProperty(PublicProperties.CLIENT_CERTIFICATE_LIST.toString());
    if (clientCertificateList != null) {
      hostConfig.setClientCertificateList(clientCertificateList);
    }
    String ignoreServerCerts = getPersistableProp(PublicProperties.SERVER_IGNORE_CERTS);
    if (isTrue(ignoreServerCerts)) {
      hostConfig.setIgnoreServerCerts(true);
    } else {
      hostConfig.setIgnoreServerCerts(false);
      List serverCertificateList = (List) getDynamicProperty(PublicProperties.SERVER_CERTIFICATE_LIST);
      if (serverCertificateList != null) {
        hostConfig.setServerCertificateList(serverCertificateList);
      }
    }
  }
  
  
  

  /**
   * Parses Set-Cookie statement and returns the correct response.
   * @param cookie
   * @return
   */
  private String analyzeCookie(String cookie) {
    StringTokenizer tokenizer = new StringTokenizer(cookie,";",false);
    StringBuffer currentCookie = new StringBuffer();
    StringBuffer result = new StringBuffer();
    StringBuffer domain = new StringBuffer();
    StringBuffer path = new StringBuffer();
    String version = "";
    while (tokenizer.hasMoreElements()) {
      String token = tokenizer.nextToken().trim();
      if ("Secure".equals(token)) { // secure option.
        continue; // do nothing
      }
      if (token.startsWith("Max-Age=")) {
        continue; // do nothing
      }
      if (token.startsWith("Comment=")) {
        continue; // do nothing
      }
      if (token.startsWith("Domain=")) {
        domain.setLength(0);
        domain.append("; $");
        domain.append(token); // Domain suffix
        continue;
      }
      if (token.startsWith("Path=")) {
        path.setLength(0);
        path.append("; $");
        path.append(token); // Path suffix
        continue;
      }
      if (token.startsWith("Version=")) {
        version = "$"+token+"; ";
        continue;
      }
      if ("Discard".equals(token)) {
        // do nothing
        continue;
      }
      if (token.length() != 0) { // this is a cookie
        if (currentCookie.length() == 0) {
          currentCookie.append(token); // set the cookie and wait for attributes
        } else {
          if (result.length() != 0) {
            result.append("; ");
          }
          result.append(currentCookie);
          result.append(path);
          result.append(domain);
          currentCookie.setLength(0);
          path.setLength(0);
          domain.setLength(0);
          currentCookie.append(token);
        }
      }
    }
    if (currentCookie.length() != 0) {
      if (result.length() != 0) {
        result.append("; ");
      }
      result.append(currentCookie);
      result.append(path);
      result.append(domain);
    }
    if (result.length() != 0) {
      return version+result.toString();
    }
    return "";
  }

  /**
   * Returns dynamic property value.
   * @param propertyName
   * @return
   */
  protected Object getDynamicProperty(String propertyName) {
    return this.config.getDynamicContext().getProperty(propertyName);    
  }

  /**
   * Returns dynamic property value.
   * @param propertyName
   * @return
   */
  private Object getDynamicProperty(QName propertyName) {
    return this.getDynamicProperty(propertyName.toString());
  }

  /**
   * Removes dynamic property from context.
   * @param propertyName
   */
  protected void removeDynamicProperty(String propertyName) {
    this.config.getDynamicContext().removeProperty(propertyName);    
  }

  /**
   * Removes dynamic property.
   * @param propertyName
   */
  private void removeDynamicProperty(QName propertyName) {
    this.removeDynamicProperty(propertyName.toString());
  }

  /**
   * Sets dynamic property.
   * @param propertyName
   */
  protected void setDynamicProperty(String propertyName, Object value) {
    this.config.getDynamicContext().setProperty(propertyName.toString(),value);
  }

  /**
   * Sets dynamic property.
   * @param propertyName
   */
  private void setDynamicProperty(QName propertyName, Object value) {
    this.setDynamicProperty(propertyName.toString(),value);
  }

  /**
   * Returns persistable property.
   * @param propertyName
   * @return
   */
  private String getPersistableProperty(String propertyName) {
    return (String) this.config.getPersistableContext().getProperty(propertyName);   
  }

  /**
   * Returns persistable property.
   * @param propertyName
   * @return
   */
  protected String getPersistableProperty(QName propertyName) {
    return getPersistableProperty(propertyName.toString());
  }

  /**
   * Sets persistable property.
   * @param propertyName
   * @param value
   */
  private void setPersistableProperty(String propertyName, String value) {
    this.config.getPersistableContext().setProperty(propertyName,value);
  }

  /**
   * Sets persistable property.
   * @param propertyName
   * @param value
   */
  protected void setPersistableProperty(QName propertyName, String value) {
    this.setPersistableProperty(propertyName.toString(),value);
  }

  /**
   * Returns the value of static property.
   * @param propertyName
   * @return
   */
  private String getStaticProperty(QName propertyName) {
    PropertyType property = (this.config.getStaticContext().getRTConfig().getSinglePropertyList()).getProperty(propertyName); 
    if (property != null) {
      return property.get_value(); 
    } else {
      return null;      
    }                     
  }

  /**
   * Return true if some dynamic property is configured in the config or the dynamic context.
   * @param pName
   * @return
   */
  private boolean isDynamicSet(QName pName) {
    Object pv1 = getDynamicProperty(pName.toString());
    String pv2 = getStaticProperty(pName);
    if (pv1 != null || pv2 != null) {
      return true;
    }
    return false;
  }

  private boolean isPersistableSet(QName pName) {
    Object pv1 = getPersistableProperty(pName.toString());
    String pv2 = getStaticProperty(pName);
    if (pv1 != null || pv2 != null) {
      return true;
    }
    return false;    
  }

  private String getPersistableProp(QName pName) {
    String pv1 = (String) getPersistableProperty(pName.toString());
    String pv2 = getStaticProperty(pName);
    if (pv1 != null) {
      return pv1;
    }
    return pv2;        
  }

  /**
   * Returns dynamic property. 
   * @param pName
   * @return
   */
  private String getDynamicProp(QName pName) {
    String pv1 = (String) getDynamicProperty(pName.toString());
    String pv2 = getStaticProperty(pName);
    if (pv1 != null) {
      return pv1;
    }
    return pv2;    
  }

  /**
   * Checks if property is configured in config or persistable context.
   * @param propertyName
   * @return
   */
  private boolean propertySet(QName propertyName) {
    String propertyValue = getStaticProperty(propertyName);
    String runtimeValue = getPersistableProperty(propertyName);
    if (propertyValue != null || runtimeValue != null) {
      return true;
    }
    return false;
  }

  /**
   * Returns property value. The runtime setting has higher priority.
   * @param propertyName
   * @return
   */
  protected String getPropertyValue(QName propertyName) {
    String propertyValue = getStaticProperty(propertyName);
    String runtimeValue = getPersistableProperty(propertyName);
    if (runtimeValue != null && runtimeValue.length() != 0) {
      return runtimeValue;
    }    
    if (propertyValue != null && propertyValue.length() != 0) {
      return propertyValue;
    } 
    return null;
  }

  protected boolean isTrue(String value) {
    if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
      return true;
    }
    return false;
  }
  
  
  
  protected void setProxyProperties(HostConnection hostConfig, URL url) throws IOException {
    String proxyHost = null;
    String proxyPort = null;
    String proxyUser = null;
    String proxyPass = null;
    int port=0;
    proxyHost = PublicProperties.getProxyHost(config);
    proxyPort = PublicProperties.getProxyPort(config);
    String nonProxyHosts = PublicProperties.getNonProxyHosts(config);
    if (nonProxyHosts != null && nonProxyHosts.length() > 0
        && useProxyForSpecificAddress(url.getHost().toLowerCase(Locale.ENGLISH), nonProxyHosts) == false) {
      proxyHost = null;
    }
    if (proxyHost != null) {
      if (proxyPort == null) {
        throw new IOException("ProxyHost found, but proxyPort is null");
      }
      proxyUser = PublicProperties.getProxyUser(config);
      proxyPass = PublicProperties.getProxyPassword(config);
      if (proxyPass == null) {
        proxyPass = "";
      }
      try {
        port = Integer.parseInt(proxyPort);
      } catch (Exception e) {
        throw new IOException("Transport configuration exception: " + e);
      }
    }
    
    hostConfig.init(proxyHost, port, proxyUser, proxyPass);    
  }

  
  
  protected void setSocketConnectionTimeOut(HostConnection hostConfig){
    String connectionTimeoutStringValue = PublicProperties.getSocketConnectionTimeout(config);
    int connectionTimeout = HTTPSocket.DEFAULT_SOCKET_CONNECTION_TIMEOUT;
    if(connectionTimeoutStringValue != null) {
      try {
        int configuredConnectionTimeout = Integer.parseInt(connectionTimeoutStringValue);
        if(configuredConnectionTimeout  > 0) {
          connectionTimeout = configuredConnectionTimeout; 
        }
      } catch(NumberFormatException numberFormatExc) {
        //$JL-EXC$
        //default value taken
      }
    }
    
    hostConfig.setSocketConnectionTimeOut(connectionTimeout);
  }
  
  protected void setLogProperties(HTTPSocket httpSocket) throws IOException {
    // Set log properties
    Object inputLogger = getDynamicProperty(PublicProperties.P_RESPONSE_LOG_STREAM);
    Object outputLogger = getDynamicProperty(PublicProperties.P_REQUEST_LOG_STREAM);
    if (inputLogger != null || outputLogger != null) {
      OutputStream inputLog;
      OutputStream outputLog;
      try {
        inputLog = (OutputStream) inputLogger;
        outputLog = (OutputStream) outputLogger;
      } catch (ClassCastException cce) {
        throw new IOException("OutputStream expected for REQUEST_LOGGING_PROPERTY and RESPONSE_LOGGING_PROPERTY: "
            + cce.toString());
      }
      httpSocket.setLogger(inputLog, outputLog);
    }
  }
  
  protected void setSocketTimeout(HTTPSocket httpSocket){
    // Set socket timeout property
    String timeOut = PublicProperties.getSocketTimeout(config);
    if (timeOut != null) {
      try {
        int realTimeout = Integer.parseInt(timeOut);
        httpSocket.setSocketTimeout(realTimeout);
      } catch (NumberFormatException x) {
        httpSocket.setSocketTimeout(HTTPSocket.DEFAULT_SOCKET_TIMEOUT);
      }
    } else {
      httpSocket.setSocketTimeout(HTTPSocket.DEFAULT_SOCKET_TIMEOUT);        
    }    
  }
  
  protected void setCompressedResponseProperty(HTTPSocket httpSocket){
    String compressResponse = PublicProperties.getSuggestCompressedResponse(config);
    if (isTrue(compressResponse)) {
      httpSocket.setHeader("Accept-Encoding", "gzip"); //TODO: Check compatibility
    }
  }
  
  protected void setKeepAliveProperty(HTTPSocket httpSocket){
    String keepAlive = PublicProperties.getKeeAlive(config);
    if (!isTrue(keepAlive)) {
      httpSocket.setHeader("Connection", "close");
    }    
  }
  
  
  protected URL getEndpointURL() throws IOException{
    String endpointURL = PublicProperties.getEndpointURL(this.config);
    if (endpointURL == null) {
      throw new IOException("HTTP Client, does not have endpoint url specified.");           
    }
    
    URL url = null;
    // This is a little bit complicated, as the URL class doesn't know https and a protocol handler can only be set _once_ for the whole JVM, which should be prevented in a server
    if (endpointURL.toLowerCase(Locale.ENGLISH).startsWith("https")) {
      String httpEndpoint = "http" + endpointURL.substring(5);
      URL tmpUrl = new URL(httpEndpoint);
      URLStreamHandler httpsHandler = null;
      try {
        httpsHandler = (URLStreamHandler) Class.forName("iaik.protocol.https.Handler").newInstance();
      } catch (Exception ex) {
        throw new IOException("Could not create https handler:" + ex.getClass().getName()+" "+ex.getMessage());
      }
      url = new URL("https", tmpUrl.getHost(), tmpUrl.getPort(), tmpUrl.getFile(), httpsHandler);
    } else {
      if (endpointURL.indexOf("http") < 0){
        throw new IOException("Invalid endpoint URL: [" + url + "]. Please check client configuration");
      }
      url = new URL(endpointURL);
    }
    
    return url;
  }

  
}