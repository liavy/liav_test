/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.*;
import java.security.PrivilegedActionException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;


import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sap.engine.interfaces.webservices.runtime.HTTPProxy;
import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.lib.xml.util.BASE64Encoder;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientHTTPTransport;
import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;
import com.sap.engine.services.webservices.jaxrpc.exceptions.InvalidResponseCodeException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class WSDLDownloadResolver implements EntityResolver {
  
  private String username;
  private String password;
  private HTTPProxyResolver proxyResolver;
  private static Properties cached = new Properties();
  private Hashtable socketHash = new Hashtable();
  private HTTPProxy proxyObject = null;
  private int socketTimeout = 2*60000;
  private boolean useSSO = false;
  private static Location LOC = Location.getLocation(WSDLDownloadResolver.class);
  private static final String SSO_TICKET_FACTORY = "com.sap.engine.interfaces.security.auth.AssertionTicketFactory";
  private static final String SSO_HEADER_NAME = "mysapsso2";
  
  
  static {
    cached.setProperty("http://www.w3.org/2001/XMLSchema.xsd", "XMLSchema.xsd");
    cached.setProperty("http://www.w3.org/2001/XMLSchema.dtd", "XMLSchema.dtd");
    cached.setProperty("http://www.w3.org/2001/datatypes.dtd", "datatypes.dtd");
    cached.setProperty("http://www.w3.org/2001/xml.xsd", "xml.xsd");
  }
  
  public WSDLDownloadResolver() {
    proxyObject = new HTTPProxy();
  }
  
  /**
   * Sets SSO use flag. Set this flag to <code>true</code> to enable SSO Header when downloading the WSDL.
   * @param flag
   */
  public void setSSOUse(boolean flag) {
    this.useSSO = flag;
  }
  
  /**
   * Returns the SSO Flag state.
   * @return
   */
  public boolean getSSOUse() {
    return this.useSSO;
  }
  

  public static String encodeAuth(String userName, String password) {
    byte[] result = BASE64Encoder.encodeN((userName + ":" + password).getBytes()); //$JL-I18N$
    return new String(result, 0, result.length); //$JL-I18N$
  }
  
  
  public String getPassword() {
    return password;
  }

  public String getProxyHost() {
    return proxyObject.getProxyHost();
  }

  public int getProxyPort() {
    return proxyObject.getProxyPort();
  }

  public String getUsername() {
    return username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setProxyHost(String proxyHost) {
    this.proxyObject.setProxyHost(proxyHost);
  }

  public void setProxyPort(int port) {
    this.proxyObject.setProxyPort(port);
  }

  public void setUsername(String username) {
    checkUsername(username);
    this.username = username;
  }
  
  private void checkUsername(String username) {
    if(username != null && username.indexOf(":") >= 0) {
      throw new IllegalArgumentException("User name in not allowed to contain character ':'.");
    }
  }
  
  public void setProxyExcludeList(String proxyExcludeList) {
    this.proxyObject.setExcludeList(proxyExcludeList);
  }

  public void setProxyBypassLocal(boolean flag) {
    this.proxyObject.setBypassLocalAddresses(flag);
  }

  public void setProxyUser(String user) {
    this.proxyObject.setProxyUser(user);
  }

  public void setProxyPass(String pass) {
    this.proxyObject.setProxyPass(pass);
  }

  public void setHTTPProxyResolver(HTTPProxyResolver proxyResolver) {
    this.proxyResolver = proxyResolver;
  }

  public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
    return(resolveEntity(publicID, systemID, false));
  }
  
  public void setSocketTimeout(int timeout) {
    this.socketTimeout = timeout;
  }
  
  private InputSource resolveEntity(String publicID, String systemID, boolean authenticationIsUsed) throws SAXException, IOException {    
    if (systemID == null) {
      return null;
    } else if (cached.getProperty(systemID) != null) {
      ClassLoader loader = this.getClass().getClassLoader();
      InputStream in = loader.getResourceAsStream("com/sap/engine/services/webservices/tools/cached/" + cached.getProperty(systemID));
      InputSource source = new InputSource(in);
      source.setSystemId(systemID);
      return source;
    } else if (systemID != null && (systemID.trim().startsWith("http://") || systemID.trim().startsWith("https://"))) {
      URL url = new URL(systemID);
      HTTPSocket socket = new HTTPSocket(url);
      socket.setIgnoreServerCertificates(true);
      socket.setRequestMethod("GET");
      socket.setHeader("Content-Length", "0");
      socket.setHeader("Connection", "close");
      if (username == null && useSSO) {
        // Try adding the SSO Header.
        String ssoTicket = getSS0Ticket();
        if (ssoTicket != null && ssoTicket.length() > 0) {
          socket.setHeader(SSO_HEADER_NAME,ssoTicket);
        }
      }      
      socket.setSocketTimeout(this.socketTimeout);
      String proxyHost = proxyObject.getProxyHost();
      if(proxyHost != null && proxyHost.length() > 0) {
        String host = url.getHost();
        if (proxyObject.useProxyForAddress(host)) {
          if (proxyObject.getProxyUser() != null && proxyObject.getProxyUser().length() != 0) {
            socket.setProxy(proxyHost, proxyObject.getProxyPort(),proxyObject.getProxyUser(),proxyObject.getProxyPass());
          } else {
            socket.setProxy(proxyHost, proxyObject.getProxyPort());
          }
        }
      } else if (proxyResolver != null) {
        socket.setHTTPProxyResolver(proxyResolver);
      }
      
      if (username != null) {
        socket.setHeader("Authorization", ClientHTTPTransport.encodeAuth(username, password));
      }
      OutputStream out;
      try {
        out = socket.getOutputStream();
      } catch (IOException ioe) {
        StringBuffer buf = new StringBuffer("Cannot connect to ");
        buf.append(systemID);
        if (proxyObject.getProxyHost() != null) {
          buf.append(", passing via http proxy: " + proxyObject.getProxyHost() + ":" + proxyObject.getProxyPort());
        }
        if (username != null) {
          buf.append(", used user to connect: " + username);
        }
        if (ioe.getLocalizedMessage() != null) {
          buf.append(": " + ioe.getLocalizedMessage());
        }
        throw new IOException(buf.toString());
      }
      out.write("\r\n".getBytes());
      out.flush();
      InputStream in = socket.getInputStream();
      int responseCode = socket.getResponseCode();
      if(responseCode == 401 && !authenticationIsUsed) {
        String[] authentication = determineAuthenticationFromAuthenticatorARI(url);
        if(authentication != null) {
          // Take the authentikation information from the authenticator
          String authenticatorUser = authentication[0];
          String authenticatorPass = authentication[1];
          setAthentication(authenticatorUser, authenticatorPass);
          // Do not call the authenticator again.
          return(resolveEntity(publicID, systemID, true));          
        }
      }
      if (responseCode != 200) {
        if ((responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 305 || responseCode == 307) && socket.getHeader("Location") != null) { // Moved permanently
          String[] location = socket.getHeader("Location");
          return resolveEntity(publicID, location[0]);
        } else {
          String responseMsg = socket.getResponseMessage();
          Hashtable headers = socket.getHeaders();
          throw new InvalidResponseCodeException(responseCode, responseMsg, headers, url.toString());
        }
      }
      InputSource source = new InputSource(in);
      source.setSystemId(systemID);
      socketHash.put(source,socket);
      return source;
    } else {
      return null;
    }
  }
  
  private void setAthentication(String user, String pass) {
    setUsername(user);
    setPassword(pass);
  }
  
  private String[] determineAuthenticationFromAuthenticatorARI(URL url) throws IOException {
    String host = url.getHost().trim();
    InetAddress inetAddr = null;
    try {
      inetAddr = InetAddress.getByName(host);
    } catch (UnknownHostException x) {
      inetAddr = null;
    }
    int port = url.getPort();
    String protocol = url.getProtocol();
    String scheme = protocol;
    PasswordAuthentication passAuthentication = Authenticator.requestPasswordAuthentication(host,inetAddr, port, protocol, "", scheme);
    if(passAuthentication != null) {
      String user = passAuthentication.getUserName();
      if(user == null) {
        return(null);
      }
      String pass = determinePassword(passAuthentication);
      if(pass == null) {
        return(null);
      }
      return(new String[]{user, pass});
    }
    return(null);
  }
  
  private String determinePassword(PasswordAuthentication passAuthentication) {
    char[] passChars = passAuthentication.getPassword();
    return(passChars == null ? null : new String(passChars));
  }
  
  /**
   * 
   * @return
   * @throws PrivilegedActionException
   */
  private String getSS0Ticket() {    
    LOC.debugT("Lookup of SSO Ticket factory : "+SSO_TICKET_FACTORY);
    try {
      Class<?> cls = Class.forName(SSO_TICKET_FACTORY);        
      Method staticMethod = cls.getMethod("getFactory", new Class[]{});
      Method instanceMethod = cls.getMethod("createAssertionTicket", new Class[]{String.class, String.class});
      Object factoryInstance = staticMethod.invoke(null, new Object[]{});        
      String sid = "";//testEnv.getProperties().getProperty("system_name");
      String clientid = ""; //testEnv.getProperties().getProperty("server_instance_number"); 
      // Invoke with empty strings now.
      LOC.debugT("Generate SSO assertion ticket.");
      String ticketString = (String) instanceMethod.invoke(factoryInstance, new Object[]{sid, clientid});
      LOC.debugT("SSO Ticked created.");      
      return ticketString;
    } catch (Exception e) {
      LOC.traceThrowableT(Severity.DEBUG,"Unable to obtain SSO Ticket.",e);
      return null;
    }                       
  }
  
  
  public void closeConnections() throws IOException {
    Enumeration enum1 = socketHash.keys();
    while (enum1.hasMoreElements()) {
      HTTPSocket socket = (HTTPSocket) socketHash.get(enum1.nextElement());
      socket.disconnect();
    }
    socketHash.clear();
  }

}
