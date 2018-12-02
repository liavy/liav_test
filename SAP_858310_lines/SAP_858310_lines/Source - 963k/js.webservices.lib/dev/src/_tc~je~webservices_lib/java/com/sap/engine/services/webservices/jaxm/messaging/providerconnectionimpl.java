package com.sap.engine.services.webservices.jaxm.messaging;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientHTTPTransport;
import com.sap.engine.services.webservices.jaxm.soap.*;
import com.sap.engine.services.webservices.jaxm.soap.accessor.NestedSOAPException;

import javax.xml.messaging.Endpoint;
import javax.xml.messaging.JAXMException;
import javax.xml.messaging.ProviderConnection;
import javax.xml.messaging.ProviderMetaData;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class ProviderConnectionImpl implements ProviderConnection {

  static Class clz = null;
  private String userName = null;
  private String password = "";
  private String proxyHost = null;
  private int proxyPort = -1;
  private ProviderMetaData metadata = new ProviderMetaDataImpl();
  private MessageFactory messageFactory = null;

  public ProviderConnectionImpl() {
  }
  
  private MessageFactory getMessageFactory() throws SOAPException {
    if (this.messageFactory == null) {
      this.messageFactory = MessageFactory.newInstance();
    }
    return this.messageFactory;
  }

  public ProviderMetaData getMetaData() {
    return metadata;
  }

  public MessageFactory createMessageFactory(String s) {
    return messageFactory;
  }

  public void setProxy(String proxyHost, int proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setUserPassword(String password) {
    this.password = password;
  }

  public SOAPMessage call(SOAPMessage message, Endpoint endpoint) throws SOAPException {
    try {
      HTTPSocket httpsock = createHTTPSocket(endpoint);

      if (message.saveRequired()) {
        message.saveChanges();
      }

      MimeHeaders mimeheaders = message.getMimeHeaders();
      MimeHeader mimeheader;

      for (Iterator iterator = mimeheaders.getAllHeaders(); iterator.hasNext(); httpsock.setHeader(mimeheader.getName(), mimeheader.getValue())) {
        mimeheader = (MimeHeader) iterator.next();
      } 

//      System.out.println("This is a message ");
      OutputStream outputstream = httpsock.getOutputStream();
      message.writeTo(outputstream);
      outputstream.flush();
      int http_response = httpsock.getResponseCode();

      if (http_response != 200) {
        if (http_response == 500) {
          String faultString;
          try {
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
            MessageFactory messageFactory = getMessageFactory();
            SOAPMessage messageReply = messageFactory.createMessage(mimeheaders, inputstream);
            SOAPPart partReply = messageReply.getSOAPPart();
            SOAPEnvelope envelopeReply = partReply.getEnvelope();
            SOAPBody bodyReply = envelopeReply.getBody();
            SOAPFault faultReply = bodyReply.getFault();
            faultString = "SOAP Fault response :";
            faultString = faultString + "\nFault Code: " + faultReply.getFaultCode();
            faultString = faultString + "\nFault String: " + faultReply.getFaultString();
            //faultString = faultString + "\nDetail: "+faultReply.getDetail();
          } catch (Exception e) {
            throw new UnknownServiceException(httpsock.getResponseMessage());
          }
          throw new SOAPException(faultString);
        } else {
          throw new SOAPException(httpsock.getResponseMessage());
        }
      } else {
        MimeHeaders mimeheaders1 = new MimeHeaders();
        mimeheaders1.setHeader("Content-Type", httpsock.getContentType());
        mimeheaders1.setHeader("Content-Length", Integer.toString(httpsock.getContentLength()));
        InputStream inputstream = httpsock.getInputStream();        
        MessageFactory messageFactory = getMessageFactory();
        SOAPMessage message1 = messageFactory.createMessage(mimeheaders1, inputstream);
        inputstream.close();
        httpsock.disconnect();
        return message1;
      }
    } catch (Exception exception) {
      if (exception instanceof SOAPException) {
        throw (SOAPException) exception;
      } else {
        throw new NestedSOAPException(NestedSOAPException.CONNECTION_PROBLEM,exception);
      }
    }
  }

  public void close() throws JAXMException {
    return;
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

  public HTTPSocket createHTTPSocket(Endpoint endpoint) throws Exception {
    URL url = new URL(endpoint.toString());
    //System.out.println("#Debug: ConnectionImpl.createHttpConnection: open connection to:" + url);
    HTTPSocket sock = new HTTPSocket(url);
    //HttpURLConnection httpurlconnection = (HttpURLConnection)url.openConnection();
    //System.out.println("#Debug: ConnectionImpl.createHttpConnection: connection opened." );
    //httpurlconnection.setDoInput(true);
    //httpurlconnection.setDoOutput(true);
    //httpurlconnection.setUseCaches(false);
    //httpurlconnection.setDefaultUseCaches(false);
    sock.setRequestMethod("POST");

    //sock.setHeader("SOAPAction", "\""+soapAction +"\"");
    //sock.setHeader("SOAPAction", soapAction );
    if ((userName != null) && (userName.length() != 0)) {
      sock.setHeader("Authorization", ClientHTTPTransport.encodeAuth(userName, password));
    }

    if (proxyHost != null) {
      sock.setProxy(proxyHost, proxyPort);
    }

    return sock;
  }

  public void send(SOAPMessage message) throws JAXMException {
    throw new JAXMException("Send not implemented, use call instead!");
  }

}

