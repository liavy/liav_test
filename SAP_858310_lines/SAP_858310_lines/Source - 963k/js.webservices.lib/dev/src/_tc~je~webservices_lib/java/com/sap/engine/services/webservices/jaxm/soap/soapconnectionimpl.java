package com.sap.engine.services.webservices.jaxm.soap;

/**
 * Title:        InQMy JAXM 1.0 Implementation (SOAPToolkit).
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov , Chavdarb@yahoo.com
 * @version      November 2001 using JAXM 1.0 interfaces
 */

import javax.xml.messaging.Endpoint;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.sap.engine.lib.xml.SystemProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientHTTPTransport;
import com.sap.engine.services.webservices.jaxm.soap.accessor.NestedSOAPException;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

public class SOAPConnectionImpl extends SOAPConnection {

  private String userName = null;
  private String password = "";
  private String proxyUserName = null;
  private String proxyPassword = "";
  private String proxyHost = null;
  private int proxyPort = -1;
  private String excludeList;
  private boolean closed = false;
  private int socketTimeout = HTTPSocket.DEFAULT_SOCKET_TIMEOUT;


  //  private HTTPSocket httpSocket = null;
  //  private Endpoint endpoint = null;
  public SOAPConnectionImpl() {
    
    proxyHost = SystemProperties.getProperty("http.proxyHost", null);
    proxyPort = Integer.parseInt(SystemProperties.getProperty("http.proxyPort", "-1"));
    proxyUserName = SystemProperties.getProperty("http.proxyUserName", null);
    proxyPassword = SystemProperties.getProperty("http.proxyPassword", null);
    excludeList = SystemProperties.getProperty("http.nonProxyHosts", null);
    String socketTimeOutProp = SystemProperties.getProperty("http.socketTimeout", null);
    if (socketTimeOutProp != null) {
      socketTimeout = Integer.parseInt(socketTimeOutProp.trim());
    }
  }

  private boolean useProxyForAddress(String address) {
    if (excludeList == null) {
      return true;
    }
    address = address.toLowerCase(Locale.ENGLISH);
    if (useProxyForSpecificAddress(address, excludeList)) {
      try {
        InetAddress[] addresses = InetAddress.getAllByName(address);
        for (int i = 0; i < addresses.length; i++) {
          InetAddress addr = addresses[i];
          if (!useProxyForSpecificAddress(addr.getHostAddress(), excludeList)) {
            return false;
          } else if (!useProxyForSpecificAddress(addr.getHostName(), excludeList)) {
            return false;
          }
        }
        return true;
      } catch (UnknownHostException uhe) {
        return true;
      }
    }
    return false;
  }

  private static boolean checkExcludeList(String address, String excludeList, String separator) {
    StringTokenizer tokenizer = new StringTokenizer(excludeList, separator, false);
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken().trim();
      int starIndex = token.indexOf("*");
      if (starIndex != -1) {
        String start = token.substring(0, starIndex);
        String end = token.substring(starIndex + 1);
        if (address.startsWith(start) && address.endsWith(end)) {
          return false;
        }
      } else if (token.equals(address)) {
        return false;
      }
    }
    return true;
  }

  private static boolean useProxyForSpecificAddress(String address, String excludeList) {
    if ("localhost".equals(address) || "127.0.0.1".equals(address)) {
      return false;
    }
    if (excludeList == null || excludeList.trim().length() == 0) {
      return true;
    }
    if (!checkExcludeList(address, excludeList, ";")) {
      return false;
    }
    return checkExcludeList(address, excludeList, "|");
  }

  public void setProxy(String proxyHost, int proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }
  
  public void setProxyExcludeList(String excludeList) {
    this.excludeList = excludeList;
  }
  
  public String getProxyExcludeList() {
    return excludeList;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setUserPassword(String password) {
    this.password = password;
  }

  public SOAPMessage call(SOAPMessage message, Endpoint endpoint) throws SOAPException {
    try {
      if (this.closed) {
        throw new NestedSOAPException(NestedSOAPException.CONNECTION_CLOSED);
      }
      HTTPSocket httpsock = createHTTPSocket(endpoint);
      return call(message, httpsock);
    } catch (IOException e) {
      throw new NestedSOAPException(NestedSOAPException.IO_PROBLEM,e);
      //throw new SAPSoapException("IOError while transmiting message ! See nested Exception !", e);
    }
  }
  
  public SOAPMessage call(SOAPMessage message, HTTPSocket httpsock) throws SOAPException {
    try {
      if (message.saveRequired()) {
        message.saveChanges();
      }

      MimeHeaders mimeheaders = message.getMimeHeaders();
      MimeHeader mimeheader;

      for (Iterator iterator = mimeheaders.getAllHeaders(); iterator.hasNext(); httpsock.setHeader(mimeheader.getName(), mimeheader.getValue())) {
        mimeheader = (MimeHeader) iterator.next();
      }

      OutputStream outputstream = httpsock.getOutputStream();
      message.writeTo(outputstream);
      outputstream.flush();
      //System.out.println("SOAPCOnnn. SENT");
      int http_response = httpsock.getResponseCode();

      //System.out.println("SOAPCOnnn. received HTTP_RESP");
      if (http_response != 200) {
        if (http_response == 500) {
          if (httpsock.getContentType() == null || httpsock.getContentType().indexOf("text/xml") == -1) {
            // Incorrect content Type.
            throw new NestedSOAPException(NestedSOAPException.SERVER_PROBLEM,httpsock.getResponseMessage());
          }
          Hashtable headers = httpsock.getHeaders();
          mimeheaders.removeAllHeaders();            
          Enumeration headerNames = headers.keys();
          while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            String[] values = (String[]) headers.get(headerName);
            for (int i=0; i<values.length; i++) {
              mimeheaders.addHeader(headerName,values[i]);
            }
          }

          InputStream inputstream = httpsock.getInputStream();
          MessageFactory messageFactory = MessageFactory.newInstance();
          SOAPMessage messageReply = messageFactory.createMessage(mimeheaders, inputstream);
          inputstream.close();
          httpsock.disconnect();
          return messageReply; // Added for JAXM 1.0 Compability
          //          SOAPPartImpl partReply = (SOAPPartImpl) messageReply.getSOAPPart();
          //          SOAPEnvelopeImpl envelopeReply = (SOAPEnvelopeImpl) partReply.getEnvelope();
          //          SOAPBodyImpl bodyReply = (SOAPBodyImpl) envelopeReply.getBody();
          //          SOAPFaultImpl faultReply = (SOAPFaultImpl) bodyReply.getFault();
          //          if (faultReply!=null) {
          //            // Capture and load any SOAP Fault and throw it upwards
          //            SOAPFaultException fault;
          //            fault = new SOAPFaultException(faultReply.getFaultCode(),faultReply.getFaultString(),faultReply.getFaultActor(),faultReply.getDetail());
          //            throw fault;
          //          } else {
          //            throw new SOAPException(httpsock.getResponseMessage());
          //          }
        } else {
          throw new NestedSOAPException(NestedSOAPException.SERVER_PROBLEM,httpsock.getResponseMessage());
        }
      } else {
        MimeHeaders mimeheaders1 = new MimeHeaders();
        //        mimeheaders1.setHeader("Content-Type", httpsock.getContentType());
        //        mimeheaders1.setHeader("Content-Length", Integer.toString(httpsock.getContentLength()));
        /*********Added by Misho*******************/
        java.util.Enumeration en = httpsock.getHeaderNames();
        String current;
        String currentHeaderValues[];
        int i;

        while (en.hasMoreElements()) {
          current = (String) en.nextElement();
          currentHeaderValues = httpsock.getHeader(current);

          for (i = 0; i < currentHeaderValues.length; i++) {
            //            System.out.println("Adding header: " + current + " with value: " + currentHeaderValues[i]);
            mimeheaders1.addHeader(current, currentHeaderValues[i]);
          }
        }

        /****End added by Misho***************/
        InputStream inputstream = httpsock.getInputStream();
        //System.out.println("SOAPConnectionImpl. sending:");
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage message1 = messageFactory.createMessage(mimeheaders1, inputstream);
        //System.out.println("SOAPConnectionImpl. Received Resposne:");
        inputstream.close();
        httpsock.disconnect();
        return message1;
      }
    } catch (IOException e) {
      throw new NestedSOAPException(NestedSOAPException.IO_PROBLEM,e);
    }
  }

  public void close() throws SOAPException {
    if (this.closed) {
      throw new NestedSOAPException(NestedSOAPException.CONNECTION_CLOSED_AGAIN);
    }
    this.closed = true;
  }

  //  public static SOAPConnection newInstance() throws SOAPException {
  //    try {
  //      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
  //      if(clz == null)
  //        if(classloader == null)
  //          clz = Class.forName("com.sap.engine.lib.jaxm.soap.SOAPConnectionImpl");
  //        else
  //          clz = classloader.loadClass("com.sap.engine.lib.jaxm.soap.SOAPConnectionImpl");
  //      return (SOAPConnection)clz.newInstance();
  //    }
  //    catch(Exception exception) {
  //      throw new SOAPException("Unable to create SOAP connection: " + exception.getMessage());
  //    }
  //  }
//  private static String encodeAuth(String userName, String password) {
//    byte[] result = com.sap.engine.lib.xml.util.BASE64Encoder.encode((userName + ":" + password).getBytes());
//    return new String(result, 0, result.length);
//    
//  }

  //  private HTTPSocket getHTTPSocket(Endpoint endpoint) throws Exception {
  //    if (this.httpSocket==null) {
  //      httpSocket = createHTTPSocket(endpoint);
  //      this.endpoint = endpoint;
  //      return httpSocket;
  //    }
  //    if (!this.endpoint.toString().equals(endpoint.toString())) {
  //      httpSocket.disconnect();
  //      httpSocket = createHTTPSocket(endpoint);
  //      this.endpoint = endpoint;
  //      return httpSocket;
  //    }
  //    return httpSocket;
  //  }
  public HTTPSocket createHTTPSocket(String endpoint) throws MalformedURLException {
    return createHTTPSocket(new Endpoint(endpoint));
  }
  
  public HTTPSocket createHTTPSocket(Endpoint endpoint) throws MalformedURLException {
    URL url = new URL(endpoint.toString());
    HTTPSocket sock = new HTTPSocket(url);
    sock.setRequestMethod("POST");

    if ((userName != null) && (userName.length() != 0)) {
      sock.setHeader("Authorization", ClientHTTPTransport.encodeAuth(userName, password));
	}

	sock.setHeader("Connection", "Close");

    if (proxyHost != null && useProxyForAddress(url.getHost())) {
      sock.setProxy(proxyHost, proxyPort);
      
      if (proxyUserName != null && proxyPassword != null && proxyUserName.length() > 0) {
        sock.setHeader("Proxy-Authorization", ClientHTTPTransport.encodeAuth(proxyUserName, proxyPassword));
      }
    }

    sock.setSocketTimeout(socketTimeout);

    return sock;
  }

  public SOAPMessage call(SOAPMessage soapMessage, Object o) throws SOAPException {
    if (this.closed) {
      throw new NestedSOAPException(NestedSOAPException.CONNECTION_CLOSED);
    }
    if (o instanceof Endpoint) {
      return call(soapMessage,(Endpoint) o);
    } else if (o instanceof String) {
      return call(soapMessage,new Endpoint((String) o));
    } else if (o instanceof HTTPSocket) {
      return call(soapMessage, (HTTPSocket) o);
    } else if (o instanceof URL) {
    return call(soapMessage, new Endpoint(((URL) o).toString()));
    }
    return null;
  }

}

