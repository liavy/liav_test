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
package com.sap.engine.services.webservices.espbase.client.bindings.impl;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.MarshalException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import javax.activation.CommandInfo;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.holders.Holder;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sap.engine.interfaces.sca.logtrace.CallEntry;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.Protocol;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolExceptionExt;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLDOMTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLDOMTokenWriter;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.stream.XMLStreamWriterImpl;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentContainer;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentConvertor;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientHTTPTransport;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientLocalHTTPTransport;
import com.sap.engine.services.webservices.espbase.client.bindings.ConsumerHTTPTransport;
import com.sap.engine.services.webservices.espbase.client.bindings.ConsumerProtocolFactory;
import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.exceptions.TransportBindingException;
import com.sap.engine.services.webservices.espbase.client.dynamic.WebServiceException;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.GenericObject;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.BindingImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.SOAPBindingImpl;
import com.sap.engine.services.webservices.espbase.client.logging.ConsumerLoggerNew;
import com.sap.engine.services.webservices.espbase.client.performance.PerformanceInfo;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.ServiceMeteringConstants;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.ConsumerMessagePool;
import com.sap.engine.services.webservices.espbase.messaging.ESPXIMessage;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.MIMEMessageImpl;
import com.sap.engine.services.webservices.espbase.xi.ESPXIMessageProcessor;
import com.sap.engine.services.webservices.espbase.xi.XIClientServiceMetering;
import com.sap.engine.services.webservices.espbase.xi.XIFrameworkConstants;
import com.sap.engine.services.webservices.espbase.xi.util.XIAttachmentHandler;
import com.sap.engine.services.webservices.espbase.xi.util.XIReceiver;
import com.sap.engine.services.webservices.jaxrpc.encoding.DeserializerBase;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationState;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPSerializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SerializerBase;
import com.sap.engine.services.webservices.jaxrpc.encoding.XMLTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.exceptions.XmlMarshalException;
import com.sap.engine.services.webservices.jaxrpc.handlers.ConsumerJAXRPCHandlersProtocol;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.ContentTypeImpl;
import com.sap.engine.services.webservices.tools.ChunkedOutputStream;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LogRecord;
import com.sap.tc.logging.Severity;
import com.sun.xml.bind.api.impl.NameConverter;

/**
 * SOAPTransport binding client implementation.
 * @version 1.0
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class SOAPTransportBinding extends BaseTransportBinding {

  public static final Location LOC = Location.getLocation(SOAPTransportBinding.class);  

  public static final Location HTTP_CLIENT_LOC = Location.getLocation("com.sap.engine.services.ws.http.Client");
  
  public static final String PROTOCOL_INDEX = "ProtocolIndex";

  public static final String SOAP_FAULT = "SoapFault";

  public static final String SOAP_ACTION_UPPER = "SOAPAction";
  public static final String SOAP_ENCODING_STYLE = "encodingStyle";
  public static final String ACCEPT_HEADER = "Accept";

  private static final String PRIMARY_TYPE  =  "multipart";
  private static final String SUB_TYPE  =  "related";
  private static final String DEFAULT_PROTOCOL_ORDER = ConsumerJAXRPCHandlersProtocol.PROTOCOL_NAME;    

  private static final int MAX_TASKNAME_LENGTH = 64;
  public static final String RETRY = "RetryCall";  

  private static final String ATTACHMENT_CID_ATTRIB_NS = "";
  private static final String ATTACHMENT_CID_ATTRIB_NAME = "href";
  private static final String ATTACHMENT_CID_ATTRIB_VALUE_PREFIX = "cid:";
  private static final String ATTACHMENT_CONTENT_TRANSFER_ENC_HEADER_NAME = "Content-Transfer-Encoding";
  private static final String ATTACHMENT_CONTENT_TRANSFER_ENC_HEADER_VALUE = "binary";

  private SOAPSerializationContext serializationContext;
  private SOAPDeserializationContext deserializationContext;
  //private NameConvertor convertor;
  private ConsumerHTTPTransport _transport;
  private ESPXIMessageProcessor espXIMessageProcessor;
  private ESPXIMessage requestESPXIMessage;

  public SOAPTransportBinding() {
    serializationContext = new SOAPSerializationContext();
    deserializationContext = new SOAPDeserializationContext();
    //convertor = new NameConvertor(true);
    _transport = new ClientHTTPTransport();
  }  

  private ConsumerHTTPTransport getTransport(ClientConfigurationContext context) {
    if (PublicProperties.isLocalCall(context)) {
      if (this._transport == null) {
        this._transport = new ClientLocalHTTPTransport();
      } else if (!(this._transport instanceof ClientLocalHTTPTransport)) {
        this._transport.closeSession();
        this._transport = new ClientLocalHTTPTransport();          
      }
      
    } else {
      // HTTP Transport      
      if (this._transport == null) {
        this._transport = new ClientHTTPTransport();
      } else if (!(this._transport instanceof ClientHTTPTransport)) {
        this._transport.closeSession();
        this._transport = new ClientHTTPTransport();                
      }
    }
    return this._transport;
  }

  private ConsumerHTTPTransport getTransport() {
    return this._transport;
  }

  /**
   * Checks context message existence. And puts message reference into the client context.
   * @param context
   */
  private void checkContextMessage(ClientConfigurationContext context) {
    Message msg = context.getMessage();
    if (msg == null) {
      msg = ConsumerMessagePool.getMIMEMessage();
      context.setProperty(ClientConfigurationContextImpl.MESSAGE,msg);
    } 
  }

  /**
   * Builds operation request message.
   * @param opMapping
   * @param typeMapping
   * @param parameters
   * @throws Exception
   */
  private void writeSOAPRequestMessage(OperationMapping opMapping, ParameterObject[] parameters, ClientConfigurationContext context) throws RemoteException {
    SOAPMessage soapMessage = (SOAPMessage) context.getMessage();
    boolean beDebug = LOC.beDebug(); // **
    if (context.getHelperContext() != null) {
      // SDO Case
      if (beDebug){
        LOC.debugT("TransportBinding.writeSOAPRequestMessage() : Parameter serialization using SDO.");
      }
      XMLStreamWriter xmlWriter = new XMLStreamWriterImpl(soapMessage.getBodyWriter());
      SDOUtil.serializeRequest(opMapping,parameters,context,xmlWriter,getSOAPNS(context),soapMessage.getSOAPHeaders());
    } else if (context.getJAXBContext() != null) {
      // JAX-WS Case
      if (beDebug){
        LOC.debugT("TransportBinding.writeSOAPRequestMessage() : Parameter serialization using JAXB.");
      }
      XMLStreamWriter xmlWriter = new XMLStreamWriterImpl(soapMessage.getBodyWriter());
      try {
        xmlWriter.writeStartElement(getSOAPNS(context), SOAPMessage.BODYTAG_NAME); 
        JAXWSUtil.serializeRequestJAXB(opMapping,parameters,context,xmlWriter,soapMessage.getSOAPHeaders());
        xmlWriter.writeEndElement();
        xmlWriter.flush();
      } catch (Exception e) {
        throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, e, e.getMessage());
      }
    } else {
      // NY JAX-RPC Case
      if (beDebug){
        LOC.debugT("TransportBinding.writeSOAPRequestMessage() : Parameter serialization using JAX-RPC TypeMapping.");
      }
      serializeRequest(opMapping, parameters, context, soapMessage.getBodyWriter(), getSOAPNS(context), soapMessage.getSOAPHeaders());
    }
    soapMessage.commitWrite();
  }

  /**
   * Reads operation response message.
   * @param appLoader
   * @param opMapping
   * @param typeMapping
   * @param parameters
   */
  private void readSOAPResponseMessage(OperationMapping opMapping, ParameterObject[] parameters, ClientConfigurationContext context) throws RemoteException  {
    SOAPMessage soapMessage = (SOAPMessage) context.getMessage();
    boolean beDebug = LOC.beDebug();
    if (context.getHelperContext() != null) {
      // SDO Case
      if (beDebug){
        LOC.debugT("TransportBinding.readSOAPResponseMessage() : Parameter deserialization using SDO.");
      }
      SDOUtil.deserializeResponse(opMapping,parameters,context,soapMessage.getBodyReader(),getSOAPNS(context),soapMessage.getSOAPHeaders());
    } else if (context.getJAXBContext() != null) {
      // JAX-WS Case
      if (beDebug){
        LOC.debugT("TransportBinding.readSOAPResponseMessage() : Parameter deserialization using JAXB.");
      }
      JAXWSUtil.deserializeResponseJAXB(opMapping,parameters,context,soapMessage.getBodyReader(),getSOAPNS(context),soapMessage.getSOAPHeaders(), true);
    } else {
      // NY JAX-RPC Case
      if (beDebug){
        LOC.debugT("TransportBinding.readSOAPResponseMessage() : Parameter deserialization using JAX-RPC TypeMapping.");
      }
      deserializeResponse(opMapping, parameters, context, soapMessage.getBodyReader(), getSOAPNS(context), soapMessage.getSOAPHeaders());
    }
  }

  private boolean isChunkedSOAPRequest(ClientConfigurationContext context) {    
    return PublicProperties.getChunkedRequest(context);
  }

  private void setupSOAPRequestHeaders(ClientConfigurationContext context) {
    // Sets http request headers that are set by the developer
    ConsumerHTTPTransport transport = getTransport();
    Hashtable table = (Hashtable) context.getDynamicContext().getProperty(PublicProperties.P_HTTP_REQUEST_HEADERS);
    if (table != null) {
      Enumeration enum1 = table.keys();
      while (enum1.hasMoreElements()) {
        String headerName = (String) enum1.nextElement();
        String[] headerContents = (String[]) table.get(headerName);
        transport.setHeader(headerName,headerContents);
      }
      table.clear();
    }
    // Clears http response headers
    table = (Hashtable)  context.getDynamicContext().getProperty(PublicProperties.P_HTTP_RESPONSE_HEADERS);
    if (table != null) {
      table.clear();
    }        
  }

  private void outputSOAPMessage(MIMEMessage soapMessage, ClientConfigurationContext context, String soapAction) throws RemoteException {
    ConsumerHTTPTransport transport = getTransport();
    
    soapAction = prepareSoapAction(soapAction, context);
    
    if (!FaultUtil.isSOAP12Mode(context)) { //when it is not SOAP1.2 its SOAP1.1
      transport.setHeader(SOAP_ACTION_UPPER, "\""+soapAction+"\"");
    }       
    //sets request headers
    setupSOAPRequestHeaders(context);
    if  (soapMessage.getAttachmentContainer().getAttachments().size() == 0) { // Plain SOAP message            
      // Sets content type header
      String contentType = (String) context.getPersistableContext().getProperty("ContentType");
      if (contentType == null) {
        PropertyType pType = context.getStaticContext().getRTConfig().getSinglePropertyList().getProperty(PublicProperties.TRANSPORT_BINDING_FEATURE, PublicProperties.TRANSPORT_BINDING_OPTIMIZED_XML);
        if (pType != null && !PublicProperties.TRANSPORT_BINDING_OPTXML_NONE.equals(pType.get_value())) {
          if (PublicProperties.TRANSPORT_BINDING_OPTXML_BXML.equals(pType.get_value())) { // BXML
            //contentType = MIMEMessageImpl.getBXMLContentType(); // TODO currently not supported
            transport.setHeader(ACCEPT_HEADER, MIMEMessageImpl.getBXMLContentType());
          } else if (PublicProperties.TRANSPORT_BINDING_OPTXML_MTOM.equals(pType.get_value())) { // MTOM
            //TODO MTOM client support
          }
        } else if (context.getPersistableContext().getProperty("flag:BXML") != null) {
          //contentType = MIMEMessageImpl.getBXMLContentType(); // TODO currently not supported
          transport.setHeader(ACCEPT_HEADER, MIMEMessageImpl.getBXMLContentType());
        }
        if (contentType == null) { // no XML transport optimization
          if (FaultUtil.isSOAP12Mode(context)) {
            contentType = MIMEMessageImpl.getSOAP12ContentTypeWithAction(soapAction);
          } else {
            contentType = MIMEMessageImpl.getSOAP11ContentType();
          }
        }
      } else {
        //when it is custom content-type, use it as it is.
      }
      transport.setHeader(CONTENT_TYPE_HEADER, contentType);
      boolean chunkedRequest = isChunkedSOAPRequest(context);
      ByteArrayOutputStream outputBuffer = null;
      try {
        // output the soap message
        if (!chunkedRequest) {
          outputBuffer = new ByteArrayOutputStream((int) soapMessage.getBodyLength() + 4096);
          // with content length
          soapMessage.writeTo(outputBuffer);
          outputBuffer.flush();
          transport.setHeader(CONTENT_LENGTH_HEADER,String.valueOf(outputBuffer.size()));
          OutputStream output = transport.getRequestStream();
          outputBuffer.writeTo(output);
          output.flush();      
        } else {
          transport.setHeader("Transfer-Encoding","chunked");     
          OutputStream output = transport.getRequestStream();
          ChunkedOutputStream chunkedStream = new ChunkedOutputStream(output,512);
          soapMessage.writeTo(chunkedStream);
          chunkedStream.close();        
        }
        int responseCode = transport.getResponseCode();
        if (responseCode == 301 || responseCode == 302 || responseCode == 307) { // Redirect
          // Saves the original headers.
          Hashtable<String,String[]> temp = new Hashtable<String,String[]>(transport.getHeaders());
          InputStream tempStream = transport.getResponseStream();
          while (tempStream.read() != -1) {} // Reads the response stream
          String[] headers = transport.getHeader("location");
          transport.closeSession(); 
          if (headers.length!=0) { // redirect request
            URL root =  URLLoader.fileOrURLToURL(null,transport.getEndpoint());
            URL relative = URLLoader.fileOrURLToURL(root, headers[0]);
            if (relative != null) {
              transport.getHeaders().clear();
              context.getPersistableContext().setProperty(PublicProperties.P_ENDPOINT_URL,relative.toExternalForm()); // new endppoint
              transport.getHeaders().putAll(temp); // restores the original headers
              if (!chunkedRequest) {
                OutputStream output = transport.getRequestStream();
                outputBuffer.writeTo(output);
                output.flush();
              } else {
                OutputStream output = transport.getRequestStream();
                ChunkedOutputStream chunkedStream = new ChunkedOutputStream(output,512);
                soapMessage.writeTo(chunkedStream);
                chunkedStream.close();                      
              }              
            }
          } else {
            throw new TransportBindingException(TransportBindingException.INVALID_REDIRECT_CODE,PublicProperties.getEndpointURL(context));
          }
        }
      } catch (IOException e) {
        transport.closeSession();
        // In order to log the exception or not - investigate it.
        investigateExceptionCause(e, transport.getEndpoint(), context);
        throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR,e,e.getMessage());   
      } catch (Throwable e) {
        transport.closeSession();
        String proxyInfo = getLogHttpProxyInfo(context);        
        throw new TransportBindingException(TransportBindingException.CONNECTION_ERROR,e, proxyInfo);
      }                    
    } else {
      CommandMap map = javax.activation.CommandMap.getDefaultCommandMap();
      if (map != null && map instanceof MailcapCommandMap) {
        MailcapCommandMap mailMap = (MailcapCommandMap) map;
        CommandInfo[] commandInfo = mailMap.getPreferredCommands("text/xml");
        if (commandInfo.length == 0 || !XMLDataContentHandler.class.getName().equals(commandInfo[0].getCommandClass()) ) {
          ((MailcapCommandMap) map).addMailcap("text/xml;;    x-java-content-handler=com.sap.engine.services.webservices.espbase.client.bindings.impl.XMLDataContentHandler");
        }
      }
      // SOAP with attachments
      // Sets content type header
      String contentType = null;
      String boundary = soapMessage.getResponseMIMEBoundaryParameter();
      Object mtomEnabledValue = context.getPersistableContext().getProperty(SOAPBindingImpl.MTOM_ENABLED);
      if(PublicProperties.TRANSPORT_BINDING_OPTXML_MTOM.equals(mtomEnabledValue)) {
        contentType = MIMEMessageImpl.getMTOMContentType(boundary);
      } else {
        contentType = (String) context.getPersistableContext().getProperty("ContentType");
        if (contentType == null) {        
          if (FaultUtil.isSOAP12Mode(context)) {
            contentType = MIMEMessageImpl.getSOAP12MIMEContentTypeWithBoundary(boundary);
          } else {          
            contentType = MIMEMessageImpl.getSOAP11MIMEContentTypeWithBoundary(boundary);
          }
        } else {
          //when it is custom content-type, use it as it is.
        }
      }
      transport.setHeader(CONTENT_TYPE_HEADER, contentType);
      boolean chunkedRequest = isChunkedSOAPRequest(context);
      ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
      try {
        // output the soap message
        if (!chunkedRequest) {
          // with content length
          soapMessage.writeMIMEMessage(outputBuffer);
          outputBuffer.flush();
          transport.setHeader(CONTENT_LENGTH_HEADER,String.valueOf(outputBuffer.size()));
          OutputStream output = transport.getRequestStream();
          outputBuffer.writeTo(output);
          output.flush();      
        } else {
          transport.setHeader("Transfer-Encoding","chunked");     
          OutputStream output = transport.getRequestStream();
          ChunkedOutputStream chunkedStream = new ChunkedOutputStream(output,512);
          soapMessage.writeMIMEMessage(chunkedStream);
          chunkedStream.close();        
        }
        int responseCode = transport.getResponseCode();
        if (responseCode == 301 || responseCode == 302 || responseCode == 307) { // Redirect
          // Saves the original headers.
          Hashtable<String,String[]> temp = new Hashtable<String,String[]>(transport.getHeaders());
          InputStream tempStream = transport.getResponseStream();
          while (tempStream.read() != -1) {} // Reads the response stream
          String[] headers = transport.getHeader("location");
          transport.closeSession(); 
          if (headers.length!=0) { // redirect request
            URL root =  URLLoader.fileOrURLToURL(null,transport.getEndpoint());
            URL relative = URLLoader.fileOrURLToURL(root, headers[0]);
            if (relative != null) {
              transport.getHeaders().clear();
              context.getPersistableContext().setProperty(PublicProperties.P_ENDPOINT_URL,relative.toExternalForm()); // new endppoint
              transport.getHeaders().putAll(temp); // restores the original headers
              if (!chunkedRequest) {
                OutputStream output = transport.getRequestStream();
                outputBuffer.writeTo(output);
                output.flush();
              } else {
                OutputStream output = transport.getRequestStream();
                ChunkedOutputStream chunkedStream = new ChunkedOutputStream(output,512);
                soapMessage.writeMIMEMessage(chunkedStream);
                chunkedStream.close();                      
              }              
            }
          } else {
            throw new TransportBindingException(TransportBindingException.INVALID_REDIRECT_CODE,PublicProperties.getEndpointURL(context));
          }
        }
      } catch (IOException e) {
        transport.closeSession();
        throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR,e,e.getMessage());   
      } catch (Throwable e) {
        transport.closeSession();
        throw new TransportBindingException(TransportBindingException.CONNECTION_ERROR,e);
      }                          
    }
  }

  /**
   * Handles response message. And transport response codes.
   */
  private int handleSOAPResponseMessage(ClientConfigurationContext context,String operationMEP) throws RemoteException {    
    try {
      ConsumerHTTPTransport transport = getTransport();
      MIMEMessage soapMessage = (MIMEMessage) context.getMessage();
      soapMessage.clear(); // Clear the soap message+
      int responseCode = transport.getResponseCode();
      String contentType = transport.getContentType();
      if ((contentType != null && contentType.indexOf(SOAPMessage.BXML_CONTENT_TYPE) != -1) || context.getPersistableContext().getProperty("flag:BXML") != null) { // temp
        ((MIMEMessageImpl) soapMessage).setBinaryXML(true);
      }
      
      String httpProxyInfo = getLogHttpProxyInfo(context);
      String responseMessage = transport.getResponseMessage();      
      
      String endpoint = transport.getEndpoint();
      switch (responseCode) {
      // replace with constans
      case 200:
        handle200Response(context, operationMEP);
        return responseCode;

      case 202:
        handle202Response(context, operationMEP);
        return responseCode;
        
      case 301:
      case 302:
      case 303:
      case 304:
      case 305:
      case 306:
      case 307:
        handleRedirectResponse(context, endpoint, operationMEP);
        transport.closeSession();
        throw new TransportBindingException(TransportBindingException.INVALID_RESPONSE_CODE, String.valueOf(responseCode),
            responseMessage, transport.getEndpoint());
        

      case 404:
        handle404Response(context, endpoint, operationMEP);
        transport.closeSession();        
        throw new TransportBindingException(TransportBindingException.INVALID_RESPONSE_CODE,String.valueOf(responseCode),responseMessage,transport.getEndpoint(), httpProxyInfo);        

      case 500:
        handle500Response(context, operationMEP);
        return responseCode;

      case 502:
        handle502Response(context, endpoint, operationMEP);
        transport.closeSession();
        throw new TransportBindingException(TransportBindingException.INVALID_RESPONSE_CODE,String.valueOf(responseCode),responseMessage,transport.getEndpoint(), httpProxyInfo);        
               
      case 503:
        handle503Response(context, operationMEP);
        throw new TransportBindingException(TransportBindingException.INVALID_RESPONSE_CODE,String.valueOf(responseCode),responseMessage,transport.getEndpoint(), httpProxyInfo);        
        
      default:      
        transport.closeSession();
        throw new TransportBindingException(TransportBindingException.INVALID_RESPONSE_CODE,String.valueOf(responseCode),responseMessage,transport.getEndpoint(), httpProxyInfo);
      }
                       
      
    } catch (TransportBindingException e) {
      throw e;
    } catch (IOException x) {
      ConsumerHTTPTransport transport = getTransport();
      if (transport != null) {
        transport.closeSession();
      }
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR,x,x.getMessage());
    }
  }
  
  
  private void handle200Response(ClientConfigurationContext context, String operationMEP) throws IOException {
    ConsumerHTTPTransport transport = getTransport();
    MIMEMessage soapMessage = (MIMEMessage) context.getMessage();
    soapMessage.clear(); // Clear the soap message+
    String contentType = transport.getContentType();

    if (OperationMapping.MEP_ONE_WAY.equals(operationMEP)) {
      // One way operation
      if (PublicProperties.isTrue(PublicProperties.getKeeAlive(context))) {
        // Keep Alive is enabled - should read the response to the end
        try {
          InputStream input = transport.getResponseStream();
          
          // Read till the end.
          while (input.read() != -1);          
        } catch (IOException x) {
          // Reading response failed - close the connection.
          transport.closeSession();
        }
      }
      return;
    }
    
    final ContentTypeImpl contentTypeObj = new ContentTypeImpl(contentType);
    if (contentTypeObj.getPrimaryType().equalsIgnoreCase(PRIMARY_TYPE)
        && contentTypeObj.getSubType().equalsIgnoreCase(SUB_TYPE)) {
      soapMessage.initReadModeFromMIME(transport.getResponseStream(), contentType);
    } else {
      if (contentType.indexOf(SOAPMessage.SOAP11_MEDIA_TYPE) != -1 || contentType.indexOf(SOAPMessage.SOAP12_MEDIA_TYPE) != -1
          || contentType.indexOf(SOAPMessage.BXML_CONTENT_TYPE) != -1) {
        if (isSAXHandlerAvailable(context)) {
          handleResponseSAX(context, transport);
          soapMessage.clear();
        } else {
          soapMessage.initReadMode(transport.getResponseStream(), getSOAPNS(context));
        }
      } else {
        String message = transport.getResponseMessage();
        transport.closeSession();
        String httpProxyInfo = getLogHttpProxyInfo(context);
        throw new TransportBindingException(TransportBindingException.INVALID_CONTENT_TYPE, "500", message, contentType, httpProxyInfo);
      }
    }
    return;
  }
  
  
  private void handle202Response(ClientConfigurationContext context, String operationMEP) throws IOException{
    ConsumerHTTPTransport transport = getTransport();
    MIMEMessage soapMessage = (MIMEMessage) context.getMessage();
    soapMessage.clear(); // Clear the soap message+   
    String contentType = transport.getContentType();
    
        // One way operation
        if (PublicProperties.isTrue(PublicProperties.getKeeAlive(context))) {
          // Keep Alive is enabled - should read the response to the end
          try {
            InputStream input = transport.getResponseStream();
            while (input.read() != -1) {;};
          } catch (IOException x) {
            // reading response failed. close the connection
            transport.closeSession();
          }
        } else {
          // Just read the response headers.
          try {
            transport.getResponseStream();
          } catch (IOException x) {
            // reading response failed. close the connection
            transport.closeSession();
          }          
        }        
    return;
  }
  

  private void handleRedirectResponse(ClientConfigurationContext context, String endpoint, String operationMEP){
    ConsumerLoggerNew.logRedirectResponse(context, endpoint, LOC);
  }
  
  
  private void handle404Response(ClientConfigurationContext context, String endpoint,String operationMEP){
    ConsumerLoggerNew.log404Response(context, endpoint, LOC);
  }
  
  
  private void handle500Response(ClientConfigurationContext context, String operationMEP) throws IOException{
    ConsumerHTTPTransport transport = getTransport();
    String responseMessage = transport.getResponseMessage();
    MIMEMessage soapMessage = (MIMEMessage) context.getMessage();
    soapMessage.clear(); // Clear the soap message+   
    String contentType = transport.getContentType();
      
    if (contentType.indexOf(SOAPMessage.SOAP11_MEDIA_TYPE) != -1 || contentType.indexOf(SOAPMessage.SOAP12_MEDIA_TYPE) != -1) { // soap message fault
          if (isSAXHandlerAvailable(context)) {
            handleResponseSAX(context,transport);
            soapMessage.clear();
          } else {
            soapMessage.initReadMode(transport.getResponseStream(), getSOAPNS(context));
          }
                    
          return;
        } else {
          transport.getResponseMessage();
          transport.closeSession();
                   
          ConsumerLoggerNew.log500Reponse(context, LOC);
          
          String httpProxyInfo = getLogHttpProxyInfo(context);
          throw new TransportBindingException(TransportBindingException.INVALID_CONTENT_TYPE,"500", responseMessage, contentType, httpProxyInfo);
        }
      }            

  
  private void handle502Response(ClientConfigurationContext context, String endpoint, String operationMEP){
    ConsumerLoggerNew.log502Response(context, endpoint, LOC);
  }
  
  
  private void handle503Response(ClientConfigurationContext context, String operationMEP){
    ConsumerLoggerNew.log503Response(context, LOC);
  }

  
  
  
  
  /**
   * Checks web service response message headers and throws exception if it contains unprocessed 
   * headers with attribute mustUnderstand = "true"|"1" 
   * 
   * @param context
   * @throws TransportBidngingException
   */
  private void checkSOAPResponseMessageHeaders(ClientConfigurationContext context) throws TransportBindingException {
    String lName = null;
    String nsName = null;
    SOAPMessage message = (SOAPMessage) context.getMessage();
    SOAPHeaderList soapHeaders = message.getSOAPHeaders();
    Element[] headers = soapHeaders.getHeaders();    
    for (int i=0; i<headers.length; i++) {
      NamedNodeMap attribs = headers[i].getAttributes();
      for (int j=0; j<attribs.getLength(); j++) {
        Attr attr = (Attr) attribs.item(j);
        lName = attr.getLocalName();
        nsName = attr.getNamespaceURI();
        if ("mustUnderstand".equals(lName) && SOAPMessage.SOAP11_NS.equals(nsName)) {
          // must Understand header attribute          
          if ("true".equals(attr.getValue()) || "1".equals(attr.getValue())) {
            throw new TransportBindingException(TransportBindingException.UNPROCESSED_HEADERS,"{"+headers[i].getNamespaceURI()+"}"+headers[i].getTagName());
          }
        }
      }
    }
  }

  private void addCustomSOAPRequestHeaders(ClientConfigurationContext context, SOAPMessage msg) {
    List headers = (List) context.getDynamicContext().getProperty(PublicProperties.P_STUB_REQUEST_HEADERS);
    if (headers != null) {
      for (int i = 0; i < headers.size(); i++) {
        msg.getSOAPHeaders().addHeader((Element) headers.get(i));
      }
    }
  }

  /**
   * Invokes protocols handle response from given index downwards. 
   * @param protocols
   * @param protocolIndex
   * @param context
   * @throws TransportBindingException
   */ 
  private void invokeHandleSOAPResponse(Protocol[] protocols, int protocolIndex ,ClientConfigurationContext context) throws TransportBindingException {
    if (context.getDynamicContext().getProperty(BindingImpl.PROTOCOL_NAME) != null) { 
      try {
        Protocol[] attachmentProtocol = ConsumerProtocolFactory.protocolFactory.getProtocols("AttachmentProtocol");
        attachmentProtocol[0].handleResponse(context);                  
        Protocol jaxwsProtocol = (Protocol) context.getDynamicContext().getProperty(BindingImpl.PROTOCOL_NAME);
        jaxwsProtocol.handleResponse(context);
        return;
      } catch (ProtocolExceptionExt e){
        ConsumerLoggerNew.logSecurityFailure(e, LOC, context);
        throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,BindingImpl.PROTOCOL_NAME);
      } catch (Exception e) {
        throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,BindingImpl.PROTOCOL_NAME);
      }
    }
    ConfigurationContext persistable = context.getPersistableContext();
    for (int i=protocolIndex; i>=0; i--) {
      try {
        persistable.setProperty(PROTOCOL_INDEX,String.valueOf(i));
        protocols[i].handleResponse(context);
      } catch (ProtocolExceptionExt e){
        ConsumerLoggerNew.logSecurityFailure(e, LOC, context);
        throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,BindingImpl.PROTOCOL_NAME);
      } catch (ProtocolException e) {
        throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,protocols[i].getProtocolName());
      }     
    }
  }
  /**
   * If handle fault method on one of the called protocols returns Protocol.BACK then the framework will try
   * to resend the message, assuming the handle fault method has fixed the problem
   * @param protocols
   * @param protocolIndex
   * @param context
   * @param ex
   * @return
   */
  private int invokeHandleFault(Protocol[] protocols, int protocolIndex ,ClientConfigurationContext context, Exception ex){
    int temp = Protocol.CONTINUE;
    if (context.getDynamicContext().getProperty(BindingImpl.PROTOCOL_NAME) != null) {
      // J2EE 5
      return temp;
    }

    ConfigurationContext persistable = context.getPersistableContext();
    context.getDynamicContext().setProperty(SOAP_FAULT, ex);
    for (int i=protocolIndex; i>=0; i--) {
      try {
        persistable.setProperty(PROTOCOL_INDEX,String.valueOf(i));
        int result = protocols[i].handleFault(context);
        if (result == Protocol.BACK){
          temp = Protocol.BACK;
        }
      } catch (Exception e) {
        LOC.traceThrowableT(Severity.DEBUG, "Handle fault has thrown an exception",e);
        // handle fault does not throw exception - it was already thrown, do some fault specific actions
        //throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,protocols[i].getProtocolName());
      }     
    }
    return temp;
  }

  /**
   * Invokes .afterDeserialization method on suitable protocols.
   * @param protocols
   * @param context
   * @throws TransportBindingException
   */
  private void invokeAfterSOAPDS(Protocol[] protocols,int protocolIndex, ClientConfigurationContext context) throws TransportBindingException {
    ConfigurationContext persistable = context.getPersistableContext();
    for (int i=protocolIndex; i>=0; i--) {
      if (protocols[i] instanceof ProtocolExtensions) {        
        try {
          persistable.setProperty(PROTOCOL_INDEX,String.valueOf(i));
          ((ProtocolExtensions) protocols[i]).afterDeserialization(context);
        }catch (ProtocolExceptionExt e){
          ConsumerLoggerNew.logSecurityFailure(e, LOC, context);
          throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,protocols[i].getProtocolName());
        } catch (ProtocolException e) {
          throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,protocols[i].getProtocolName());
        } catch (MessageException e) {
          // TODO What to do here ?
          LOC.traceThrowableT(Severity.DEBUG, "Exception caught while invoking the after deserialization protocols.", e);
        }
      }
    }    
  }

  /**
   * Invokes one-way and request-response
   */
  public void call(ClientConfigurationContext context) throws RemoteException {
    String appName = context.getServiceContext().getApplicationName();
    String seiName = context.getStaticContext().getInterfaceData().getSEIName();           
    String taskName;
    if (appName == null) {
      taskName = "WS processing at " + seiName;       
    } else {
      taskName = "WS processing at " + appName + "//" + seiName;
    }
    if (taskName.length() > MAX_TASKNAME_LENGTH) {
      taskName = taskName.substring(0, MAX_TASKNAME_LENGTH - 1);
    }

    WSLogTrace.attachNewCallEntry(taskName);

    //special handling for WS-RM reserved proxy
    Object internalId = context.getPersistableContext().getProperty("BusinessInterfaceInternalId");
    if (internalId != null) {
      WSLogTrace.setInterfaceInternalName(internalId.toString());
    } else {
      WSLogTrace.setInterfaceInternalName(context.getStaticContext().getRTConfig().getInterfaceId());
    }
    Object interfaceQName = context.getPersistableContext().getProperty("BusinessInterfaceQName");
    if (interfaceQName != null) {
      WSLogTrace.setInterfaceQName(QName.valueOf((String) interfaceQName));
    } else {
      WSLogTrace.setInterfaceQName(new QName(context.getStaticContext().getDTConfig().getNamespace(),context.getStaticContext().getDTConfig().getName()));
    }

    WSLogTrace.setHeader("ApplicationName", appName);
    WSLogTrace.setDirection(CallEntry.Direction.OUTBOUND);
    WSLogTrace.setCallStatus(CallEntry.Status.ERROR); //Just for initialization
    WSLogTrace.setConnectivityType("WS");


    String javaOperationName = context.getOperationName();
    WSLogTrace.setHeader(WSLogTrace.JAVA_OPERATION_HEADER, javaOperationName);

    OperationMapping opMapping = getOperationMapping(context);      
    String wsdlOperationName = opMapping.getWSDLOperationName();
    WSLogTrace.setHeader(WSLogTrace.WSDL_OPEARTION_HEADER, wsdlOperationName);

    String endpointURL = PublicProperties.getEndpointURL(context);

    WSLogTrace.setHeader(WSLogTrace.REQUEST_URL_HEADER, endpointURL);

    String proxyHost = PublicProperties.getProxyHost(context);
    String proxyPort = PublicProperties.getProxyPort(context);
    if ((proxyHost != null) && (proxyPort != null)){
      WSLogTrace.setHeader(WSLogTrace.PROXY_HEADER, proxyHost + ":" + proxyPort);
    }

    CallEntry.TraceLevel traceLevel = WSLogTrace.calculateEffectiveTraceLevelForCurrentUser( 
        "WS", 
        CallEntry.Direction.OUTBOUND, 
        WSLogTrace.getInterfaceQName());
    if (!CallEntry.TraceLevel.NONE.equals(traceLevel))  {
      WSLogTrace.enablePassportTraceFlag(WSLogTrace.WEBSERVICES_LOCATIONS);
    }

    if(LOC.beDebug()){
      LogRecord firstRec = LOC.debugT("Start WS processing on Consumer Side");
      WSLogTrace.setStartTraceID(firstRec.getId().toString()); //add the LogID for the first trace
    }
    Exception ex = null;
    boolean suppressedErrorTracing = PublicProperties.isTrue((String) PublicProperties.getDynamicProperty(PublicProperties.SUPPRESS_ERROR_TRACING, context));

    try {
      callWOLogging(context);
      WSLogTrace.setCallStatus(CallEntry.Status.SUCCESS);
    } catch (RemoteException re) {
      WSLogTrace.setCallStatus(CallEntry.Status.ERROR);
      ex = re;
      throw re;
    } catch (RuntimeException runtimeExc) {
      WSLogTrace.setCallStatus(CallEntry.Status.ERROR);
      ex = runtimeExc;
      throw runtimeExc;
    } finally {
      if (ex != null) {
        if (suppressedErrorTracing) {
          WSLogTrace.suppressErrorTracing(true);
        } else {	        	
          LogRecord traceRec = LOC.traceThrowableT(Severity.ERROR, ex.getMessage(), ex);
          if (traceRec != null && WSLogTrace.getStartTraceID() == null) {
            WSLogTrace.setStartTraceID(traceRec.getId().toString());
            WSLogTrace.setEndTraceID(traceRec.getId().toString());
          }
        }
      }
      if(LOC.beDebug()){
        LogRecord lastRecord = LOC.debugT("End WS processing on Consumer Side");
        WSLogTrace.setEndTraceID(lastRecord.getId().toString()); // add the LogID for the last trace
      }
      WSLogTrace.releaseCallEntry();        
    }
  }

  /**
   * Method dispatching transport binding call.
   * @param context
   * @throws RemoteException
   */
  private void callWOLogging(ClientConfigurationContext context) throws RemoteException {
    LOC.debugT("TransportBinding.call() : Initiatiing consumer call.");
    if(PublicProperties.getUseXITransport(context)) {
      LOC.debugT("TransportBinding.call() : Using XI Transport for consumer call.");      
      call_XI(context);
    } else {
      LOC.debugT("TransportBinding.call() : Using SOAP HTTP Transport for consumer call.");
      if(PublicProperties.isOperationIdempotencyActive(context)) {
        call_SOAP_Idenpotency(context);
      } else {
        call_SOAP(context);
      }
    }
  }

  private void call_SOAP_Idenpotency(ClientConfigurationContext context) throws RemoteException {
    LOC.debugT("TransportBinding.call() : Idempotency is active.");
    int retrysCount = PublicProperties.getIdempotencyRetriesCount(context);
    LOC.debugT("TransportBinding.call() : Idempotency retrys count : " + retrysCount);
    long retrySleep = PublicProperties.getIdempotencyRetrySleep(context);
    LOC.debugT("TransportBinding.call() : Idempotency retry sleep : " + (retrySleep < 0 ? "unspecified" : retrySleep + "ms"));
    String readTimeout = PublicProperties.getSocketTimeout(context);
    LOC.debugT("TransportBinding.call() : Idempotency retry timeout : " + (readTimeout == null ? "unspecified" : readTimeout + "ms"));
    PublicProperties.setIdempotencyUUID(UUID.randomUUID().toString(), context);
    boolean callSucceeded = false;
    for(int i = 0; i < retrysCount - 1; i++) {
      try {
        call_SOAP(context);
        callSucceeded = true;
        break;
      } catch(TransportBindingException transportBindingExc) {
        if(!(transportBindingExc.getCause() instanceof SocketTimeoutException)) {
          throw transportBindingExc;
        }
      }
      try {
        Thread.currentThread().sleep(retrySleep);
      } catch(InterruptedException interruptedExc) {
        throw new RemoteException("Idempotency retry sleep is interrupted!", interruptedExc);
      }
    }
    if(!callSucceeded) {
      call_SOAP(context);
    }
  }

  /**
   * Invokes SOAP Protocols beforeSerialization() method.
   * @param protocols
   * @param context
   * @throws TransportBindingException
   */
  private void invokeBeforeSerialization(final Protocol[] protocols,final ClientConfigurationContext context) throws TransportBindingException {
    boolean beDebug = LOC.beDebug();
    if (beDebug){ // **
      LOC.debugT("TransportBinding.call() : Invoking protocol .beforeSerialization() method.");
    }
    if (context.getDynamicContext().getProperty(BindingImpl.PROTOCOL_NAME) != null) { // There is JAX-WS handlers configured
      LOC.debugT("TransportBinding.call() : JAX-WS Handlers are registered. Native protocol calls are aborted.");
      return;
    }
    ConfigurationContext persistable = context.getPersistableContext();
    // Invoke before serialization
    for (int i=0; i<protocols.length; i++) {
      if (protocols[i] instanceof ProtocolExtensions) {        
        try {
          persistable.setProperty(PROTOCOL_INDEX,String.valueOf(i));
          if (beDebug) {
            LOC.debugT("TransportBinding.call() : Invoking protocol ["+protocols[i].getProtocolName()+"].beforeSerialization()");
          }
          ((ProtocolExtensions) protocols[i]).beforeSerialization(context);
        }catch (ProtocolExceptionExt e) {
          if (beDebug) {            
            LOC.debugT("TransportBinding.call() : Protocol ["+protocols[i].getProtocolName()+"].beforeSerialization() call has failed.");
          }
          
          ConsumerLoggerNew.logSecurityFailure(e, LOC, context);          
          throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,protocols[i].getProtocolName()); 
        } catch (ProtocolException e) {
          if (beDebug) {            
            LOC.traceThrowableT(Severity.DEBUG, "TransportBinding.call() : Protocol ["+protocols[i].getProtocolName()+"].beforeSerialization() call has failed.", e);
          }          
          throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,protocols[i].getProtocolName());
        } 
      }
    }    
  }

  /**
   * Invokes SOAP Protocols handlerRequest() method.
   * @param protocols
   * @param context
   * @return
   */
  private int invokeHandlerRequest(Protocol[] protocols, ClientConfigurationContext context,int beginIndex) throws TransportBindingException {
    boolean beDebug = LOC.beDebug();
    if (beDebug){ // **
      LOC.debugT("TransportBinding.invokeHandlerRequest() : Invoking protocols .handleRequest() method.");
    }
    if (context.getDynamicContext().getProperty(BindingImpl.PROTOCOL_NAME) != null) { // There is JAX-WS handlers configured
      if(beDebug){ // **
        LOC.debugT("TransportBinding.invokeHandlerRequest() : JAX-WS Handlers are configured, aborting call of native protocols.");
      }
      Protocol jaxwsProtocol = (Protocol) context.getDynamicContext().getProperty(BindingImpl.PROTOCOL_NAME);
      int result = -1;

      try {
        int exitCode = jaxwsProtocol.handleRequest(context);
        if (exitCode == Protocol.BACK) {
          result = 1;
        }
        if (exitCode == Protocol.STOP) {
          result = -2;
        }
        if (result == -1) {
          Protocol[] attachmentProtocol = ConsumerProtocolFactory.protocolFactory.getProtocols("AttachmentProtocol");
          attachmentProtocol[0].handleRequest(context);          
        }
      }catch (ProtocolExceptionExt e){
        ConsumerLoggerNew.logSecurityFailure(e, LOC, context);
        throw new javax.xml.ws.WebServiceException(e.getMessage(),e);
      }catch (ProtocolException e) {
        throw new javax.xml.ws.WebServiceException(e.getMessage(),e);
      } catch (Exception e) {        
        throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,BindingImpl.PROTOCOL_NAME);
      }
      return result;
    }
    ConfigurationContext persistable = context.getPersistableContext();
    // Invoke handle request
    boolean stopIt = false;
    int breakIndex = -1;  
    for (int i=beginIndex; i<protocols.length; i++) {
      try {
        persistable.setProperty(PROTOCOL_INDEX,String.valueOf(i));
        if (beDebug) {
          LOC.debugT("TransportBinding.invokeHandlerRequest() : Invoking protocol ["+protocols[i].getProtocolName()+"]handlerRequest().");          
        }
        if (protocols[i].getProtocolName().equals("PassportProtocol")) { 
          if (checkLogging(WSLogTrace.getAttachedCallEntry())) {
            WSLogTrace.disablePassportTraceFlag();
          }
        }
        int protocolResult = protocols[i].handleRequest(context);
        if (protocols[i].getProtocolName().equals("PassportProtocol")) {
          if (checkLogging(WSLogTrace.getAttachedCallEntry())) {
            WSLogTrace.enablePassportTraceFlag(WSLogTrace.WEBSERVICES_LOCATIONS);
          }
        }
        // logging information.
        if (beDebug) {
          String presult = "CONTINUE";
          if (protocolResult == Protocol.STOP) {
            presult = "STOP";
          }
          if (protocolResult == Protocol.BACK) {
            presult = "BACK";
          }          
          LOC.debugT("TransportBinding.invokeHandlerRequest() : Protocol ["+protocols[i].getProtocolName()+"].handleRequest() returned code ["+presult+"].");          
        }        
        if (protocolResult == Protocol.BACK) { // STOP Processing and call handle response
          breakIndex = i;
          break;
        }
        if (protocolResult == Protocol.STOP) { // STOP Processing and leave the call
          stopIt = true;
          break;
        }
      }catch (ProtocolExceptionExt e){
        if (beDebug) {
          LOC.debugT("TransportBinding.invokeHandlerRequest() : Invoking protocol ["+protocols[i].getProtocolName()+"]handlerRequest() has thrown exception.");          
        }               
        ConsumerLoggerNew.logSecurityFailure(e, LOC, context);
        throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,protocols[i].getProtocolName());
      } catch (ProtocolException e) { // Protocols fails the execution
        if (beDebug) {
          LOC.traceThrowableT(Severity.DEBUG, "TransportBinding.invokeHandlerRequest() : Invoking protocol ["+protocols[i].getProtocolName()+"]handlerRequest() has thrown exception.", e);                    
        }        
        throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,e,protocols[i].getProtocolName());
      } catch (MessageException m) {
        //TODO: How to handle this case.
        if (beDebug) {
          LOC.traceThrowableT(Severity.DEBUG, "TransportBinding.invokeHandlerRequest() : Invoking protocol ["+protocols[i].getProtocolName()+"]handlerRequest() has thrown MessageException.", m);          
        }        
      }      
    }
    if (stopIt == true) { // STOP Client Runtime - hybernation happened. No message output Return
      ConsumerHTTPTransport transport = getTransport();
      if (transport.isConnected()) {
        transport.closeSession();
      }
      //TODO: What to do in this case ? 
      return -2;
    }    
    return breakIndex;
  }

  private boolean checkLogging(CallEntry ce) {
    return (ce != null) && (!CallEntry.TraceLevel.NONE.equals(ce.getTraceLevel()));
  }

  /**
   * Prepares connection log stream.
   * @param context
   */  
  private void saveLogStream(ClientConfigurationContext context, String logName,String label) {
    OutputStream logStream = (OutputStream) context.getDynamicContext().getProperty(logName);
    if (logStream == null) {
      if (HTTP_CLIENT_LOC.beDebug()){ // **
        HTTP_CLIENT_LOC.debugT("TransportBinding.call() : No message was logged.");
      }
    } else {
      if (logStream instanceof ByteArrayOutputStream) {
        if (HTTP_CLIENT_LOC.beDebug()){ // **
          LogRecord logMessage = HTTP_CLIENT_LOC.debugT(label+((ByteArrayOutputStream) logStream).toString()); //$JL-I18N$
          if (label.contains("Requested context:")) {
            WSLogTrace.setOutboundPayloadTraceID(logMessage.getId().toString());
          } else {
            WSLogTrace.setInboundPayloadTraceID(logMessage.getId().toString());
          }
        }
        if (context.getDynamicContext().getProperty(logName+"custom") != null) {
          context.getDynamicContext().removeProperty(logName+"custom");
          context.getDynamicContext().removeProperty(logName);
        }
      }
    }    
  }  

  /**
   * Prepares connection log stream.
   * @param context
   */  
  private void setupLogStream(ClientConfigurationContext context, String logName) {

    OutputStream logStream = (OutputStream) context.getDynamicContext().getProperty(logName);
    if (logStream == null) {
      // Add a glaf that this log stream is added by the logging and tracing.
      context.getDynamicContext().setProperty(logName+"custom","yes");
      context.getDynamicContext().setProperty(logName,new ByteArrayOutputStream());
    } else {
      if (logStream instanceof ByteArrayOutputStream) {
        ((ByteArrayOutputStream) logStream).reset();
      }
    }    
  }

  /**
   * Prepares connection log streams.
   * @param context
   */
  private void saveMessageLog(ClientConfigurationContext context) {
    saveLogStream(context,PublicProperties.P_REQUEST_LOG_STREAM,"Requested context:message:");
    saveLogStream(context,PublicProperties.P_RESPONSE_LOG_STREAM,"Response context:message:");
  }  

  /**
   * Prepares connection log streams.
   * @param context
   */
  private void setupMessageLog(ClientConfigurationContext context) {
    setupLogStream(context,PublicProperties.P_REQUEST_LOG_STREAM);
    setupLogStream(context,PublicProperties.P_RESPONSE_LOG_STREAM);
  }

  /**
   * Invokes the web service method of a SOAP web service.
   * @param context
   * @throws RemoteException
   */
  private void call_SOAP(ClientConfigurationContext context) throws RemoteException {
    try {
      resetPerformanceInfo(context);
      if (LOC.beDebug()) {
        LOC.debugT("TransportBinding.call() : Starting proxy operation ["+context.getOperationName()+"] invoke.");
        LOC.debugT("TransportBinding.call() : Proxy InterfaceData dump.");
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        InterfaceData idata = context.getStaticContext().getDTConfig();
        BindingData bdata = context.getStaticContext().getRTConfig();
        try {
          ConfigurationFactory.saveDTConfig(idata,temp);
          temp.flush();
          LOC.debugT(temp.toString());
          temp.reset();
        } catch (Exception x) {
          LOC.traceThrowableT(Severity.DEBUG, "Unable to serialize DTConfig.", x);
        }
        LOC.debugT("TransportBinding.call() : Proxy BindingData dump.");
        try {
          LOC.debugT(bdata.dumpBindingData(bdata));          
        } catch (Exception x) {
          LOC.traceThrowableT(Severity.DEBUG, "Unable to serialize RTConfig.", x);
        }    
        //LOC.debugT("TransportBinding.call() : Persistable properties dump.");
        //LOC.debugT(context.getPersistableContext().toString());
      }
      notifyPerformanceStartTime(context, PerformanceInfo.REQUEST_PREPARE_INFO_TYPE);
      OperationMapping opMapping = context.getStaticContext().getInterfaceData().getOperationByJavaName(context.getOperationName());
      if (isOneWayOperation(opMapping, context)) {
        WSLogTrace.setSemantics("one-way");
        WSLogTrace.setHeader(CallEntry.HEADER_PROCESSING_STEPS, "createRequest;callBackend");
      } else {
        WSLogTrace.setSemantics("request-response");
        WSLogTrace.setHeader(CallEntry.HEADER_PROCESSING_STEPS, "createRequest;callBackend;parseResponse");
      }
      //we are at the first step. if we don't reach the next this header will not change its value
      WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "0"); 
      WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "0");
      ThreadWrapper.pushSubtask("createRequest", ThreadWrapper.TS_PROCESSING);
      ParameterObject[] parameters = context.getOperationParameters();        
      // Makes initial config correctness check
      initialParametersCheck(parameters, opMapping, context); 
      // Gets configured protocol list            
      checkContextMessage(context); // Adds message instance to the context     
      MIMEMessage soapMessage = (MIMEMessage) context.getMessage();
      if (FaultUtil.isSOAP12Mode(context)) {
        soapMessage.initWriteMode(SOAPMessage.SOAP12_NS);
      } else {
        soapMessage.initWriteMode(SOAPMessage.SOAP11_NS);
      }
      // Invoke consumer protocols
      String protocolOrder = _getProtocolOrder(context);
      if (LOC.beDebug()) {
        LOC.debugT("TransportBinding.call() : Protocol order is ["+protocolOrder+"].");
      }
      Protocol[] protocols = ConsumerProtocolFactory.protocolFactory.getProtocols(protocolOrder);
      try {
        invokeBeforeSerialization(protocols,context);
        // Serialize message      
        addCustomSOAPRequestHeaders(context, soapMessage);      
        writeSOAPRequestMessage(opMapping,parameters,context);
        // Invoke handle request
        int breakIndex = invokeHandlerRequest(protocols,context,0);
        if (breakIndex == -2) { // Hybernation happened
          notifyPerformanceEndTime(context, PerformanceInfo.REQUEST_PREPARE_INFO_TYPE);
          return;
        }
        if (breakIndex != -1) { // Break message processing - the response should be valid response message
          notifyPerformanceEndTime(context, PerformanceInfo.REQUEST_PREPARE_INFO_TYPE);
          notifyPerformanceStartTime(context, PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE);
          invokeHandleSOAPResponse(protocols,breakIndex,context);
          readSOAPResponseMessage(opMapping,parameters,context);
          if (context.getDynamicContext().getProperty(BindingImpl.PROTOCOL_NAME) == null) {
            invokeAfterSOAPDS(protocols,breakIndex,context);
          }
          notifyPerformanceEndTime(context, PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE);
        } else { // No break - output the message into the transport
          notifyPerformanceEndTime(context, PerformanceInfo.REQUEST_PREPARE_INFO_TYPE);
          if (LOC.beDebug()) { // Communication Message loging                        
            if (HTTP_CLIENT_LOC.beDebug()) {
              setupMessageLog(context);
            }
            String endpointURL = PublicProperties.getEndpointURL(context);
            LOC.debugT("TransportBinding.call() : Initiating consumer call to ["+endpointURL+"].");
          }
          // Output message      
          notifyPerformanceStartTime(context, PerformanceInfo.BACKEND_RESPONSE_INFO_TYPE);
          ConsumerHTTPTransport transport = getTransport(context);
          transport.init(ClientHTTPTransport.REQUEST_METHOD_POST,context);
          String soapAction = opMapping.getProperty(OperationMapping.SOAP_ACTION);
          //we are at the second step. if an error occurs before we reach the next this header will show it
          WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "1");
          WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "1");
          
          ThreadWrapper.popSubtask();
          ThreadWrapper.pushSubtask("callBackend", ThreadWrapper.TS_PROCESSING);
          if (checkLogging(WSLogTrace.getAttachedCallEntry())) {
            WSLogTrace.disablePassportTraceFlag();
          }
          outputSOAPMessage(soapMessage,context,soapAction);
          if (checkLogging(WSLogTrace.getAttachedCallEntry())) {
            WSLogTrace.enablePassportTraceFlag(WSLogTrace.WEBSERVICES_LOCATIONS);
          }
          String operationMEP = determineOperationMEP(opMapping, context);
          int responseCode = handleSOAPResponseMessage(context,operationMEP);
          notifyPerformanceEndTime(context, PerformanceInfo.BACKEND_RESPONSE_INFO_TYPE);          
          if (OperationMapping.MEP_ONE_WAY.equals(operationMEP)) {
            if (responseCode == 200 || responseCode == 202) {
              // One way operation stop processing
              //everything went fine for one-way operation, so remove the failure-step header
              WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, null);
              ThreadWrapper.popSubtask();
              return;
            }
          }                             
          if (OperationMapping.MEP_REQ_RESP.equals(operationMEP) && responseCode == 202) {
            throw new TransportBindingException(TransportBindingException.INVALID_REQ_RESPONCE_CODE,context.getOperationName());
          }
          notifyPerformanceStartTime(context, PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE);
          //we are at the third step. if something goes wrong after that, the header won't change its value
          WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, "2");
          WSLogTrace.setHeader(CallEntry.HEADER_LAST_EXECUTED_STEP, "2");
          
          ThreadWrapper.popSubtask();
          
          // Response is recieved - get the response headers
          Hashtable table = (Hashtable) context.getDynamicContext().getProperty(PublicProperties.P_HTTP_RESPONSE_HEADERS);
          if (table == null) {
            table = new Hashtable();
            context.getDynamicContext().setProperty(PublicProperties.P_HTTP_RESPONSE_HEADERS,table);
          }
          Map<String,String[]> responseHeaders = transport.getHeaders();
          table.putAll(responseHeaders);        
          if (isSAXHandlerAvailable(context)) {
            // No further response handling.
            return;
          }
          // Invokes handle response - before deserialization
          invokeHandleSOAPResponse(protocols,protocols.length-1,context);
          if (responseCode == 500) { // Internal Server Error.
            //fault case
            WSLogTrace.setHeader(CallEntry.HEADER_PROCESSING_STEPS, "createRequest;callBackend;parseFault");
            ThreadWrapper.pushSubtask("parseFault", ThreadWrapper.TS_PROCESSING);
            if (context.getHelperContext() != null) {
              SDOUtil.processFault(opMapping,parameters,context,soapMessage.getBodyReader());
            } else if (context.getJAXBContext() != null) {
              JAXWSUtil.processFault(opMapping,parameters,context,soapMessage.getBodyReader());
            } else {
              processFault(opMapping,parameters,context,soapMessage.getBodyReader());
            }
          } else {
            ThreadWrapper.pushSubtask("parseResponse", ThreadWrapper.TS_PROCESSING);
            readSOAPResponseMessage(opMapping,parameters, context);       
          }
          // No fault has been thrown
          if (context.getDynamicContext().getProperty(BindingImpl.PROTOCOL_NAME) == null) { 
            invokeAfterSOAPDS(protocols,protocols.length-1, context);
          }
          checkSOAPResponseMessageHeaders(context);
        }
        context.getPersistableContext().removeProperty(RETRY);
        notifyPerformanceEndTime(context, PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE);
        //everything went fine in request-response case
        WSLogTrace.setHeader(CallEntry.HEADER_FAILURE_STEP, null);
        ThreadWrapper.popSubtask();
      } catch (Exception ex) {
        // call handle fault on every protocol
        int result = invokeHandleFault(protocols, protocols.length - 1, context, ex);
        if ((result == Protocol.BACK) && (context.getPersistableContext().getProperty(RETRY) == null)) {
          // checks if this has already called - do not enter infinite loop!
          LOC.traceThrowableT(Severity.DEBUG, "Retrying web service call after exception", ex);
          context.getPersistableContext().setProperty(RETRY, Boolean.TRUE.toString());
          // call_SOAP is throwing an exception so no need to rethrow this one -
          // only log it
          call_SOAP(context);
        } else {
          throw ex;
        }
      } finally {
        // just end the try{}catch
      }
    } catch (javax.xml.ws.soap.SOAPFaultException x) {
      notifyPerformanceEndTime(context, PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE);
      // trace exception because it is often swallowed by the consumer application.
      LOC.traceThrowableT(Severity.DEBUG, x.getMessage(), x);
      throw x;
    } catch (SOAPFaultException x) {
      notifyPerformanceEndTime(context, PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE);
      // trace exception because it is often swallowed by the consumer application.
      LOC.traceThrowableT(Severity.DEBUG, x.getMessage(), x);
      throw x; 
    } catch (Exception y) {
      // Catch some protocol runtime exceptions.
      ConsumerHTTPTransport transport = getTransport();
      if (transport != null && transport.isConnected()){
        transport.closeSession();
      }
      
      // trace exception because it is often swallowed by the consumer application.
      LOC.traceThrowableT(Severity.DEBUG, y.getMessage(), y);
      
      if (y instanceof javax.xml.ws.WebServiceException) {
        throw (javax.xml.ws.WebServiceException) y;
      }
      if (y instanceof RemoteException) {
        throw (RemoteException) y;
      }
      throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,y,"Unknown");
    } finally {
      if (HTTP_CLIENT_LOC.beDebug()) {
        saveMessageLog(context);
      }
      
      MIMEMessage soapMessage = (MIMEMessage) context.getMessage();
      if (soapMessage != null) {
       ConsumerMessagePool.returnMimeMessage(soapMessage);
       context.removeProperty(ClientConfigurationContextImpl.MESSAGE);
      }

      ConsumerHTTPTransport transport = getTransport();      
      if (transport != null && transport.isConnected()) {
       // If keep alive is false then close the connection
        boolean keepAliveFlag = false;
        String keepAlive = PublicProperties.getKeeAlive(context);
        keepAliveFlag = PublicProperties.isTrue(keepAlive);
        if (keepAliveFlag == false) {
          // keep alive switched off - close connection.
          transport.closeSession();
        }else{
          // keep alive switched on - pool connection.
          transport.releaseConnection();
        }
      }      
    }        
  }

  /**
   * Returns protocol order property value.
   * @param context
   * @return
   */
  public static String _getProtocolOrder(ClientConfigurationContext context) {
    Object value = context.getPersistableContext().getProperty(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME.toString());
    if (value != null) {
      return (String) value;
    }
    // Gets configured protocol list
    PropertyType pType = context.getStaticContext().getRTConfig().getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS,BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY);
    String protocolOrder = null;
    if (pType != null) {
      protocolOrder = pType.get_value();
    } else {
      protocolOrder = DEFAULT_PROTOCOL_ORDER;
    }             
    return protocolOrder;
  }



  /**
   * Sends preconfigured message to the reciever and reads the response.
   * @param context
   * @throws RemoteException
   */
  public void sendMessage(ClientConfigurationContext context) throws RemoteException {
    try {
      ConfigurationContext persistable = context.getPersistableContext();      
      boolean invokeProtocols = true;
      if (persistable.getProperty(PublicProperties.CALL_PROTOCOLS) != null) {
        if ("false".equalsIgnoreCase((String) persistable.getProperty(PublicProperties.CALL_PROTOCOLS))) {
          invokeProtocols = false;
        }
      }      
                       
      MIMEMessage soapMessage = (MIMEMessage) context.getMessage();
      if (soapMessage == null) {
        throw new RemoteException("MimeMessage must be initialized before sendMessage() call.");
      }
      String protocolOrder = _getProtocolOrder(context);
      Protocol[] protocols = ConsumerProtocolFactory.protocolFactory.getProtocols(protocolOrder);
      try {
        int breakIndex = -1;
        if (invokeProtocols) {
          int beginIndex = -1;
          String protocolIndex = (String) persistable.getProperty(PROTOCOL_INDEX);
          if (protocolIndex != null) {
            beginIndex = Integer.parseInt(protocolIndex);
          }
          // Invoke handle request        
          breakIndex = invokeHandlerRequest(protocols,context,beginIndex+1);
          if (breakIndex == -2) { // Hybernation happened
            return;
          }
        }
        if (breakIndex != -1) { // Break message processing - the response should be valid response message         
          invokeHandleSOAPResponse(protocols,breakIndex,context);
          // TODO: Response handling is possible - and deserialization can happen
          //readResponseMessage(opMapping,parameters,context);
          invokeAfterSOAPDS(protocols,breakIndex,context); 
        } else { // No break - output the message into the transport
          if (HTTP_CLIENT_LOC.beDebug()) {
            setupMessageLog(context);
          }
          // Prepare environment for message send.
          ConsumerHTTPTransport transport = getTransport(context);
          transport.init(ClientHTTPTransport.REQUEST_METHOD_POST,context);        
          // Output message
          outputSOAPMessage(soapMessage,context,"");
          String operationMEP = OperationMapping.MEP_ONE_WAY;
          String mepOverride = (String) context.getPersistableContext().getProperty(OperationMapping.OPERATION_MEP);
          context.getPersistableContext().removeProperty(OperationMapping.OPERATION_MEP);
          if (mepOverride != null) {
            operationMEP = mepOverride;
          }        
          int responseCode = handleSOAPResponseMessage(context,operationMEP);
          if (OperationMapping.MEP_ONE_WAY.equals(operationMEP)) {
            if (responseCode == 200 || responseCode == 202) {
              // One way operation stop processing
              return;
            }
          }
          if (invokeProtocols) {
            invokeHandleSOAPResponse(protocols,protocols.length-1,context);
          }
          if (responseCode == 500) {
            if (invokeProtocols == false) { // JAX-WS case
              SOAPFault fault = JAXWSUtil.buildFault(soapMessage.getBodyReader(), context);
              throw new javax.xml.ws.soap.SOAPFaultException(fault);
            } else {
              SOAPFaultException exception = FaultUtil.buildFaultException(soapMessage.getBodyReader(), context);          
              throw exception;
            }
          }
          // Reads the soap message - here deserialization should be made
          XMLTokenReader reader = soapMessage.getBodyReader();
          try {
            reader.passChars();
            if (reader.getState() != XMLTokenReader.STARTELEMENT || !reader.getLocalName().equals(SOAPMessage.BODYTAG_NAME)) {
              throw new TransportBindingException(TransportBindingException.NO_SOAP_BODY);
            }
            if (invokeProtocols) { // Standard SAP use case.
              reader.next(); // Enters into the body contents
              reader.passChars();
              String soapNS = getSOAPNS(context);         
              if (reader.getState() == XMLTokenReader.STARTELEMENT && SOAPMessage.FAULTTAG_NAME.equals(reader.getLocalName()) && soapNS.equals(reader.getURI())) {
                // soap fault found throw soap fault exception
                SOAPFaultException exception = FaultUtil.buildFaultException(soapMessage.getBodyReader(), context);          
                throw exception;          
              }
              // Reading the body contents.
              while (reader.getState() != XMLTokenReader.EOF) {
                reader.next();
              }
              invokeAfterSOAPDS(protocols,protocols.length-1, context);            
            }
          } catch (ParserException x) {
            throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR,x,x.getMessage());
          }
        }
      } catch (Exception ex) {
        // call handle fault on every protocol
        invokeHandleFault(protocols, protocols.length - 1, context, ex);
        throw ex;
      } 
    } catch (javax.xml.ws.soap.SOAPFaultException y) {
      throw y;
    } catch (SOAPFaultException x) {
      throw x; 
    } catch (Exception y) {
      throw new TransportBindingException(TransportBindingException.PROTOCOL_EXCEPTION,y,"Unknown");
    } finally {     
      if (HTTP_CLIENT_LOC.beDebug()) {
        saveMessageLog(context);
      }
            
      ConsumerHTTPTransport transport = getTransport();       
      if (transport != null && transport.isConnected()) {
        // If keep alive is false then close the connection
        boolean keepAliveFlag = false;
        String keepAlive = PublicProperties.getKeeAlive(context);
        keepAliveFlag = PublicProperties.isTrue(keepAlive);
        if (keepAliveFlag == false) {
          // keep alive switched off - close connection.
          transport.closeSession();
        }else{
          // keep alive switched on - pool connection.
          transport.releaseConnection();
        }
      }
    }            
  }

  /**
   * @return the namespace of the soap version which is currently in use.
   */
  public static String getSOAPNS(ClientConfigurationContext ctx) {
    if (FaultUtil.isSOAP12Mode(ctx)) {
      return SOAPMessage.SOAP12_NS;
    } else {
      return SOAPMessage.SOAP11_NS;
    }
  }

  protected void finalize() {
    try {
      super.finalize();
    } catch (Throwable x) {
      //$JL-EXC$
    }    
    if ((this._transport != null) && (this._transport.isConnected())) {
      this._transport.closeSession();
    }
  }

  /**
   * Builds operation request message.
   * It uses either 
   * @param opMapping
   * @param typeMapping
   * @param parameters
   * @throws Exception
   */
  private void serializeRequest(OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context, XMLTokenWriter writer, String namespace, SOAPHeaderList soapHeaderList) throws RemoteException {
    try {      
      TypeMapping typeMapping = context.getTypeMaping();  
      serializationContext.clearContext();
      serializationContext.setTypeMapping(typeMapping);      
      writer.enter(namespace, SOAPMessage.BODYTAG_NAME);
      // Get operation style - if it is RPC then add RPC Wrapper element
      String style = operationMapping.getProperty(OperationMapping.OPERATION_STYLE);
      if ("rpc".equals(style)) { // "rpc" style
        String operationName = operationMapping.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER);      
        String operationNamespace = operationMapping.getProperty(OperationMapping.INPUT_NAMESPACE);
        String encodingStyle = operationMapping.getProperty(OperationMapping.IN_ENCODING_STYLE);
        String use = (String) operationMapping.getProperty(OperationMapping.OPERATION_USE);
        serializationContext.setTypeUse(false);
        if ("encoded".equals(use)) { // If style is encoded set property for framework
          serializationContext.setTypeUse(true);
          serializationContext.setEncoded(true);
          if (encodingStyle != null) {
            writer.writeAttribute(namespace, SOAPTransportBinding.SOAP_ENCODING_STYLE, encodingStyle);
          } else {
            throw new RemoteException(" In encoded representation encoding style must be specified !");
          }
        }
        writer.enter(operationNamespace, operationName);            
      }
      ParameterMapping[] params = operationMapping.getParameter();
      for (int i=0; i<params.length; i++) {
        if (params[i].getParameterType() == ParameterMapping.IN_TYPE ||
            params[i].getParameterType() == ParameterMapping.IN_OUT_TYPE) {
          ParameterMapping param = params[i];
          serializeParameter(writer, paramObjs[i], param, (ExtendedTypeMapping)typeMapping, soapHeaderList, context);
        }      
      }
      if ("rpc".equals(style)) {        
        writer.leave();
      }
      writer.leave(); 
      writer.flush();
    } catch(XmlMarshalException xmlMarshalExc) {
      throw xmlMarshalExc;
    } catch (IOException ioExc) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, ioExc, ioExc.getMessage());
    }
  }

  /**
   * Writes operation output parameter.
   * @param writer
   * @param param
   * @param pMap
   * @throws Exception
   */
  private void serializeParameter(XMLTokenWriter writer, ParameterObject paramObj, ParameterMapping paramMapping, ExtendedTypeMapping typeMapping, SOAPHeaderList soapHeaderList, ClientConfigurationContext context) throws RemoteException {
    try {
      Object content = paramObj.parameterValue;
      Class parameterType = paramObj.parameterType;
      if (content != null && content instanceof Holder) { // Holder parameter
        Field field = content.getClass().getField("value");
        parameterType = field.getType();      
        content = field.get(content);
      }
      if (content != null) {
        parameterType = content.getClass();
      }

      QName schemaName = paramMapping.getSchemaQName();
      if(paramMapping.isAttachment()) {
        serializeAttachmentParameter(writer, paramObj, paramMapping, context);
      } else if(paramMapping.isElement()) {
        QName elementType = typeMapping.getTypeForElement(schemaName);
        if (paramMapping.isHeader() == true) {        
          Element header = soapHeaderList.createHeader(schemaName);
          soapHeaderList.addHeader(header);
          writer = new XMLDOMTokenWriter(header);                                      
        } else {
          writer.enter(schemaName.getNamespaceURI(), schemaName.getLocalPart());
        }
        SerializerBase serializer = serializationContext.getSerializer(elementType, parameterType);
        serializer.serialize(content, writer, serializationContext);
      } else {      
        writer.enter(paramMapping.getNamespace(), paramMapping.getWSDLParameterName());
        SerializerBase serializer = null;
        if (content != null && content instanceof GenericObject) {
          QName genericObjTypeName = ((GenericObject)content)._getObjectType();
          if (genericObjTypeName != null) {
            serializationContext.setTypeUse(!genericObjTypeName.equals(schemaName));
            schemaName = genericObjTypeName; 
          }          
          XMLTypeMapping xmlTypeMapping = typeMapping.getXmlTypeMapping(schemaName);
          serializer = xmlTypeMapping.getDefaultSerializer();
        } else {
          serializer =  serializationContext.getSerializer(schemaName, parameterType);
        }
        serializer.serialize(content, writer, serializationContext);                    
      }
    } catch (Exception exc) {
      throw exc instanceof RemoteException ? (RemoteException)exc : new RemoteException("Connection Exception", exc); 
    }
  }

  private void serializeAttachmentParameter(XMLTokenWriter writer, ParameterObject paramObj, ParameterMapping paramMapping, ClientConfigurationContext context) throws IOException {
    writer.enter(null, paramMapping.getWSDLParameterName());
    if(paramObj.parameterValue == null) {
      writer.writeAttribute(NS.XSI, "nil", "true");    
    } else {
      Attachment attachment = createAttachment(paramObj, paramMapping);
      writer.writeAttribute(ATTACHMENT_CID_ATTRIB_NS, ATTACHMENT_CID_ATTRIB_NAME, ATTACHMENT_CID_ATTRIB_VALUE_PREFIX + attachment.getContentId());
      MIMEMessage mimeMsg = (MIMEMessage)(context.getMessage());
      mimeMsg.getAttachmentContainer().addAttachment(attachment);
    }
    writer.leave();
  }

  private Attachment createAttachment(ParameterObject paramObj, ParameterMapping paramMapping) {
    Attachment attachment = AttachmentContainer.createAttachmentWithUniqueCID();
    attachment.setContentType(paramMapping.getAttachmentContentType());
    if(paramObj.parameterValue instanceof byte[]) {
      attachment.setContentAsByteArray((byte[])paramObj.parameterValue);
    } else if(paramObj.parameterValue instanceof String) {
      attachment.setDataHandler(new DataHandler(paramObj.parameterValue, attachment.getContentType()));
      attachment.setMimeHeader(ATTACHMENT_CONTENT_TRANSFER_ENC_HEADER_NAME, ATTACHMENT_CONTENT_TRANSFER_ENC_HEADER_VALUE);
    } else {
      //TODO throw Exception
    }
    return(attachment);
  }

  /**
   * Reads operation response message.
   * @param appLoader
   * @param opMapping
   * @param typeMapping
   * @param parameters
   */
  private void deserializeResponse(OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context, XMLTokenReader reader, String namespace, SOAPHeaderList soapHeaderList) throws RemoteException  {
    try {
      ClassLoader appLoader = context.getClientAppClassLoader();
      TypeMapping typeMapping = context.getTypeMaping();
      deserializationContext.clearContext();
      deserializationContext.setApplicationClassLoader(appLoader);    
      deserializationContext.setTypeMapping(typeMapping);
      deserializationContext.setObjectFactory(context.getObjectFactory());
      if (context.getDynamicContext().getProperty(PublicProperties.P_ESF_VALUE_MANAGER) != null) {
        deserializationContext.setProperty(PublicProperties.P_ESF_VALUE_MANAGER,context.getDynamicContext().getProperty(PublicProperties.P_ESF_VALUE_MANAGER));
      }
      deserializationContext.setProperty(SOAPDeserializationContext.TOLERANT_DESERIALIZATION,"true");
      reader.passChars();
      if (reader.getState() != XMLTokenReader.STARTELEMENT || !reader.getLocalName().equals(SOAPMessage.BODYTAG_NAME)) {
        throw new TransportBindingException(TransportBindingException.NO_SOAP_BODY);
      }
      reader.next(); // Enters into the body
      reader.passChars();
      if (reader.getState() == XMLTokenReader.STARTELEMENT && SOAPMessage.FAULTTAG_NAME.equals(reader.getLocalName()) && namespace.equals(reader.getURI())) {
        processFault(operationMapping, paramObjs, context, reader);
        return;      
      }
      String style = operationMapping.getProperty(OperationMapping.OPERATION_STYLE);
      if ("rpc".equals(style)) {
        // enters in response element his name doesn't matter
        reader.next();
        reader.passChars();      
        String use = operationMapping.getProperty(OperationMapping.OPERATION_USE);
        if ("encoded".equals(use)) {
          deserializationContext.setEncoded(true);
        }      
      }
      ParameterMapping[] params = operationMapping.getParameter();
      boolean[] paramsFlag = new boolean[params.length];      
      for (int j = 0; j < params.length; j++) {
        boolean parameterFound = false;
        for (int i = 0; i < params.length; i++) {
          ParameterMapping param = params[i];
          if ((param.getParameterType() == ParameterMapping.OUT_TYPE ||
              param.getParameterType() == ParameterMapping.IN_OUT_TYPE || 
              param.getParameterType() == ParameterMapping.RETURN_TYPE) && paramsFlag[i] == false) {
            if (param.isHeader()) { // This is SOAP Header parameter
              deserializeParameter(reader, paramObjs[i], param, (ExtendedTypeMapping)typeMapping, soapHeaderList, context);
              paramsFlag[i] = true;
              parameterFound = true;
              break;
            } else {
              // The reader should be positioned at start element
              if (reader.getState() != XMLTokenReader.STARTELEMENT) { // Not positioned on start element ship parameter deserialization
                continue;
              }   
              if (matchCurrentParameter(reader,param)) {
                deserializeParameter(reader, paramObjs[i], param, (ExtendedTypeMapping)typeMapping, soapHeaderList, context);
                reader.next();
                reader.passChars();
                paramsFlag[i] = true;
                parameterFound = true;
                break;
              }
            }            
          }
        }
        if (parameterFound == false) {
          // No parameter is matched - find the missing parameter.
          for (int i = 0; i < params.length; i++) {
            if (paramsFlag[i] == false && (params[i].getParameterType() == ParameterMapping.OUT_TYPE ||
                params[i].getParameterType() == ParameterMapping.IN_OUT_TYPE || 
                params[i].getParameterType() == ParameterMapping.RETURN_TYPE) &&
                params[i].isOptional() == false) { 
              ParameterMapping param = params[i];
              if (param.isElement()) {
                throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, param.getSchemaQName().toString());            
              } else {
                throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, param.getWSDLParameterName());          
              }              
            }            
          }
        }
      }
      if ("rpc".equals(style)) {
        // leaves response element his name doesn't matter
        reader.next();
        reader.passChars();            
      }
      deserializationContext.deserializeRemainingElements(reader);
      for (int i = 0; i < paramObjs.length; i++) {
        if (paramObjs[i].parameterValue != null) {
          if (Holder.class.isAssignableFrom(paramObjs[i].parameterType)) { // Holder parameter            
            Field field = paramObjs[i].parameterType.getField("value");
            Object obj = field.get(paramObjs[i].parameterValue);
            if (obj != null && obj instanceof SOAPDeserializationState) {
              SOAPDeserializationState state = (SOAPDeserializationState) obj;
              if (state.isComplete()) {
                obj = state.getInstance();
                field.set(paramObjs[i].parameterValue,obj);
              } else {
                throw new RemoteException(" Some references in responce could not be resolved !");
              }            
            }      
          } else {     
            if (paramObjs[i].parameterValue instanceof SOAPDeserializationState) {
              SOAPDeserializationState state = (SOAPDeserializationState) paramObjs[i].parameterValue;
              if (state.isComplete()) {
                paramObjs[i].parameterValue = state.getInstance();
              } else {
                throw new RemoteException(" Some references in responce could not be resolved !");
              }
            }
          }
        }
      }
      while (reader.getState() != XMLTokenReader.EOF) {
        reader.next();
      }
    } catch(Exception exc) {
      if(exc instanceof SOAPFaultException) {
        throw (SOAPFaultException)exc;
      }
      if(exc instanceof IOException || exc  instanceof ParserException) {
        throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, exc, exc.getMessage());
      }
      if(exc  instanceof RemoteException) {
        throw (RemoteException)exc;
      }
      throw new TransportBindingException(TransportBindingException.PARAMETER_SET_FAIL, exc);
    }
  }

  /**
   * Process the response fault.
   * @param opMapping
   * @param typeMapping
   * @param parameters
   */
  private void processFault(OperationMapping operationMapping , ParameterObject[] paramObjs, ClientConfigurationContext context, XMLTokenReader reader) throws RemoteException {
    ExtendedTypeMapping typeMapping = (ExtendedTypeMapping) context.getTypeMaping();
    ParameterMapping[] parameterMappings = operationMapping.getParameter();
    SOAPFaultException exception = FaultUtil.buildFaultException(reader, context);
    SOAPElement element = (SOAPElement)(FaultUtil.getDetailRootElement(exception.getDetail()));    
    boolean isEncoded = OperationMapping.ENCODED_USE.equals(operationMapping.getProperty(OperationMapping.OPERATION_USE));
    if(element != null) {
      String elementName = element.getLocalName();
      String namespace = element.getNamespaceURI();
      QName elementQName = isEncoded ? new QName(elementName) : new QName(namespace, elementName);
      for(int i = 0; i < parameterMappings.length; i++) {
        if(parameterMappings[i].getParameterType() == ParameterMapping.FAULT_TYPE) {
          QName faultElement = parameterMappings[i].getFaultElementQName();
          if(faultElement.equals(elementQName)) {
            try {
              ParameterMapping faultMapping = parameterMappings[i];              
              deserializeException(element, exception, paramObjs[i], faultMapping, typeMapping, context);              
              return;
            } catch(SOAPFaultException x) {
              // Deserialization failed - rethrow the SOAPFaultException
              QName faultCode = x.getFaultCode();
              if((SOAPMessage.SOAP11_NS.equals(faultCode.getNamespaceURI()) && SOAPMessage.SOAP11_SERVER_F_CODE.equals(faultCode.getLocalPart())) 
                  || (SOAPMessage.SOAP12_NS.equals(faultCode.getNamespaceURI()) && SOAPMessage.SOAP12_RECEIVER_F_CODE.equals(faultCode.getLocalPart()))) {
                throw new ServerException("Server Exception: " + exception.getFaultString(), exception);      
              }
              if((SOAPMessage.SOAP11_NS.equals(faultCode.getNamespaceURI()) || SOAPMessage.SOAP12_NS.equals(faultCode.getNamespaceURI())) 
                  && "DataEncodingUnknown".equals(faultCode.getLocalPart())) {
                throw new MarshalException("Server Exception: " + exception.getFaultString(), exception);      
              }
              throw x;
            } catch(Exception exc) {
              LOC.traceThrowableT(Severity.DEBUG, "Deserialization of the SoapFaultElement into opertion parameter failed. Rethrow the original SoapFaultException.", exc);
              break;
            }
          }
        }
      }      
    }
    QName faultCode = exception.getFaultCode();
    if((SOAPMessage.SOAP11_NS.equals(faultCode.getNamespaceURI()) && SOAPMessage.SOAP11_SERVER_F_CODE.equals(faultCode.getLocalPart())) 
        || (SOAPMessage.SOAP12_NS.equals(faultCode.getNamespaceURI()) && SOAPMessage.SOAP12_RECEIVER_F_CODE.equals(faultCode.getLocalPart()))) {
      throw new ServerException("Server Exception: " + exception.getFaultString(), exception);      
    }
    if((SOAPMessage.SOAP11_NS.equals(faultCode.getNamespaceURI()) || SOAPMessage.SOAP12_NS.equals(faultCode.getNamespaceURI())) 
        && "DataEncodingUnknown".equals(faultCode.getLocalPart())) {
      throw new MarshalException("Server Exception: " + exception.getFaultString(), exception);      
    }
    exception.fillInStackTrace();
    throw exception;
  }





  /**
   * Deserialize the checked exception in the SOAP Response.
   * @param element
   * @param exception
   * @param param
   * @param faultMapping
   * @param typeMapping
   * @param appLoader
   * @throws Exception
   */
  private void deserializeException(SOAPElement element, SOAPFaultException exception, ParameterObject param , ParameterMapping faultMapping, ExtendedTypeMapping typeMapping, ClientConfigurationContext context) throws Exception {
    QName faultSchemaType = faultMapping.getSchemaQName();
    XMLDOMTokenReader reader = new XMLDOMTokenReader(element);
    Class exceptionClass = param.parameterType;
    // Loads the default java type for the exception complex type
    String javaClass = typeMapping.getDefaultJavaType(faultSchemaType);
    if(javaClass == null) {
      exception.fillInStackTrace();
      throw exception;
    }
    Class resultClass = context.getClientAppClassLoader().loadClass(javaClass);
    if(context.getObjectFactory() != null) { // Dynamic client case
      prepareDeserializationContext(context,typeMapping);                
      DeserializerBase deserializer = deserializationContext.getDeserializer(faultSchemaType,resultClass);
      reader.begin();
      Object content = deserializer.deserialize(reader,deserializationContext,resultClass);
      reader.end();
      String faultName = (faultMapping.isElement() ? faultMapping.getSchemaQName().getLocalPart() : faultMapping.getWSDLParameterName());
      WebServiceException wsException = new WebServiceException(exception.getFaultString(),exception,faultName,content,faultSchemaType);
      param.parameterValue = wsException;
      return;
    }
    if(faultMapping.getFaultConstructorParamOrder() == null) { // Simple type fault
      prepareDeserializationContext(context,typeMapping);                
      DeserializerBase deserializer = deserializationContext.getDeserializer(faultSchemaType,resultClass);
      reader.begin();
      Object content = deserializer.deserialize(reader,deserializationContext,resultClass);
      reader.end();                                    
      Constructor constructor = exceptionClass.getConstructor(new Class[] {resultClass});
      param.parameterValue = constructor.newInstance(new Object[] {content});                
    } else { // Complex type fault
      String[] paramOrder = faultMapping.getFaultConstructorParamOrder().split("\\s+");
      String[] paramJavaTypes = faultMapping.getProperty(ParameterMapping.FAULT_CONSTRUCTOR_PARAM_TYPES).split("\\s+");
      Class[] constructorTypes = new Class[paramJavaTypes.length];
      Object[] constructorObjects = new Object[constructorTypes.length];
      for(int j = 0; j < paramJavaTypes.length; j++) {                  
        if(NameConvertor.isPrimitive(paramJavaTypes[j])) {
          constructorTypes[j] = NameConvertor.primitiveToClass(paramJavaTypes[j]);
        } else {
          if(paramJavaTypes[j].endsWith("[]")) { // Field is an array
            String javaClassName = paramJavaTypes[j].substring(0,paramJavaTypes[j].length()-2);
            Class componentJavaClass = context.getClientAppClassLoader().loadClass(javaClassName);
            Object arr = Array.newInstance(componentJavaClass,0);
            constructorTypes[j] = arr.getClass();
          } else {
            constructorTypes[j] = context.getClientAppClassLoader().loadClass(paramJavaTypes[j]);
          }
        }                                             
      }      
      if(Exception.class.isAssignableFrom(resultClass)) {
        // Idiotic J2EE 1.4 use case ! For some reason in some J2EE 1.4 WS Test Cases there are no corresponding 
        // java types for complex types that are used for exceptions, so deserialization framework can not be used.
        QName[] constructorQNames = new QName[paramJavaTypes.length];
        // Now deserialize all constuctor params
        reader.begin();
        String[] attribCount = faultMapping.getProperty(ParameterMapping.FAULT_ATTRIBUTE_COUNT).split("\\s+");  
        Attributes attr = reader.getAttributes();
        for(int j = 0; j < attribCount.length; j++) {
          if("1".equals(attribCount[j])) { // This is attribute
            prepareDeserializationContext(context,typeMapping);
            constructorQNames[j] = typeMapping.getDefaultSchemaType(paramJavaTypes[j]);
            if(constructorQNames[j] == null) {
              exception.fillInStackTrace();
              throw exception;                        
            }
            DeserializerBase deserializer = deserializationContext.getDeserializer(constructorQNames[j],constructorTypes[j]);
            String attrVal = null;
            for(int k = 0; k < attr.getLength(); k++) {
              if (paramOrder[j].equals(attr.getLocalName(k))) {
                attrVal = attr.getValue(k);
                break;
              }
            } 
            constructorObjects[j] = deserializer.deserialize(attrVal,deserializationContext,constructorTypes[j]);                    
          }
        }
        reader.next(); reader.passChars(); 
        for(int j = 0; j < paramOrder.length; j++) {
          if("0".equals(attribCount[j])) { // Element field
            if(reader.getState() != XMLTokenReader.STARTELEMENT) { // Missing parameter
              exception.fillInStackTrace();
              throw exception;
            }
            if(!paramOrder[j].equals(reader.getLocalName())) { // Unordered parameter
              exception.fillInStackTrace();
              throw exception;            
            }
            prepareDeserializationContext(context,typeMapping);
            constructorQNames[j] = typeMapping.getDefaultSchemaType(paramJavaTypes[j]);
            if(constructorQNames[j] == null) {
              exception.fillInStackTrace();
              throw exception;                        
            }
            DeserializerBase deserializer = deserializationContext.getDeserializer(constructorQNames[j],constructorTypes[j]);
            constructorObjects[j] = deserializer.deserialize(reader,deserializationContext,constructorTypes[j]);
            reader.next();                    
          }
        }
        reader.end();        
      } else { // There is a genenerated Java Class for the complex type. The method uses this class as a result class       
        prepareDeserializationContext(context,typeMapping); 
        DeserializerBase deserializer = deserializationContext.getDeserializer(faultSchemaType,resultClass);
        reader.begin();
        Object content = deserializer.deserialize(reader,deserializationContext,resultClass);
        reader.end();
        if(content == null) { // There is not content - the deserialization failed
          exception.fillInStackTrace();
          throw exception;
        }
        resultClass = content.getClass();
        for(int j=0; j<constructorTypes.length; j++) {
          String fieldName = paramOrder[j];
          //TODO: Use better method for getting object fields - currently only getter methods are used
          Method getMethod = null;
          if(boolean.class.equals(constructorTypes[j])) {
            try {
              getMethod = resultClass.getMethod("is"+NameConverter.jaxrpcCompatible.toClassName(fieldName),null);              
              //getMethod = resultClass.getMethod("is"+NameConvertor.attributeToClassName(fieldName),null);
            } catch (NoSuchMethodException p) {
              // is method is not found
              getMethod = null;
            }
          }
          if(getMethod == null) {
            try {
              getMethod = resultClass.getMethod("get"+NameConverter.jaxrpcCompatible.toClassName(fieldName),null);
              //getMethod = resultClass.getMethod("get"+convertor.attributeToClassName(fieldName),null);
            } catch (NoSuchMethodException p) {
              // get method is not found
              getMethod = null;
            }
          }          
          if(getMethod == null) {
            exception.fillInStackTrace();
            throw exception;
          }
          constructorObjects[j] = getMethod.invoke(content,null);
        }                                          
      }
      Constructor constructor = exceptionClass.getConstructor(constructorTypes);
      if(constructor == null) {
        exception.fillInStackTrace();
        throw exception;
      }
      param.parameterValue = constructor.newInstance(constructorObjects);
    }                            
  }  

  private void prepareDeserializationContext(ClientConfigurationContext context, ExtendedTypeMapping typeMapping) {
    deserializationContext.clearContext();
    deserializationContext.setApplicationClassLoader(context.getClientAppClassLoader());    
    deserializationContext.setTypeMapping(typeMapping);
    deserializationContext.setObjectFactory(context.getObjectFactory());
    deserializationContext.setProperty(SOAPDeserializationContext.TOLERANT_DESERIALIZATION,"true");
  }

  /**
   * Returns true if the current parameter mapping could be processed.
   * @param reader
   * @param paramMapping
   * @return
   */
  private boolean matchCurrentParameter(XMLTokenReader reader, ParameterMapping paramMapping) {
    if (paramMapping.isElement()) { // Document parameter
      if (paramMapping.isHeader()) {
        return true;
      }
      if (reader.getState() == XMLTokenReader.STARTELEMENT) {
        String localName = reader.getLocalName();
        if (localName.equals(paramMapping.getSchemaQName().getLocalPart())) {
          return true;
        }
      }      
    } else { // RPC parameter
      if (reader.getState() == XMLTokenReader.STARTELEMENT) {
        String localName = reader.getLocalName();
        if (localName.equals(paramMapping.getWSDLParameterName())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Reads operation output parameter.
   * @param reader
   * @param param
   * @param pMap
   * @param typeMapping
   * @throws Exception
   */
  private void deserializeParameter(XMLTokenReader reader, ParameterObject paramObj, ParameterMapping paramMappping, ExtendedTypeMapping typeMapping, SOAPHeaderList soapHeaderList, ClientConfigurationContext context) throws RemoteException {
    try {    
      Object content = null;
      Class parameterType = paramObj.parameterType;
      if (paramObj.parameterValue instanceof Holder && context.getObjectFactory() == null) { // Holder parameter and it is not Dynamic proxy
        Field field = paramObj.parameterValue.getClass().getField("value");
        parameterType = field.getType();      
      }
      QName schemaName = paramMappping.getSchemaQName();
      if(paramMappping.isAttachment()) {
        content = deserializeAttachmentParameter(reader, paramMappping, context, parameterType);
      } else if(paramMappping.isElement()) {
        QName elementType = typeMapping.getTypeForElement(schemaName);
        if (paramMappping.isHeader() == true) {
          Element header = soapHeaderList.getHeader(schemaName);
          if (header == null) {
            throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER,schemaName.toString());
          }
          reader = new XMLDOMTokenReader(header);
          reader.begin();
        }
        if (reader.getState() != XMLTokenReader.STARTELEMENT) { // Not positioned on start element
          throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, paramMappping.getWSDLParameterName()); 
        }
        DeserializerBase deserializer = deserializationContext.getDeserializer(elementType,parameterType);
        content = deserializer.deserialize(reader,deserializationContext,parameterType);
      } else {
        if (reader.getState() != XMLTokenReader.STARTELEMENT) { // Not positioned on start element
          throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, paramMappping.getWSDLParameterName());
        }     
        if (!paramMappping.getWSDLParameterName().equals(reader.getLocalName())) {
          System.out.println("Searching parameter ["+paramMappping.getWSDLParameterName()+"] found ["+reader.getLocalName()+"].");
        }
        DeserializerBase deserializer = deserializationContext.getDeserializer(schemaName,parameterType);
        content = deserializer.deserialize(reader,deserializationContext,parameterType);                                   
      }
      // Reader is positioned at the end of the parameter
      if (paramObj.parameterValue instanceof Holder) { // Holder parameter        
        Field field = paramObj.parameterValue.getClass().getField("value"); 
        field.set(paramObj.parameterValue, content);      
      } else {
        paramObj.parameterValue = content;
      }
    } catch (ParserException parserExc) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, parserExc, parserExc.getMessage());
    } catch (Exception exc) {
      throw exc instanceof RemoteException ? (RemoteException)exc : new TransportBindingException(TransportBindingException.PARAMETER_SET_FAIL, exc);
    }   
  }

  private Object deserializeAttachmentParameter(XMLTokenReader reader, ParameterMapping paramMappping, ClientConfigurationContext context, Class paramType) throws Exception {
    if (reader.getState() != XMLTokenReader.STARTELEMENT) { // Not positioned on start element
      throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, paramMappping.getWSDLParameterName());
    }     
    String contentID = getAttachmentContentID(reader);
    if(contentID == null) {
      throw new TransportBindingException(TransportBindingException.MISSING_HREF_ATTRIBUTE, paramMappping.getWSDLParameterName());
    }
    Attachment attachment = getAttachment(context, contentID);
    if(attachment == null) {
      throw new TransportBindingException(TransportBindingException.MISSING_ATTACHMENT, contentID);
    }
    return(createAttachmentParameterValue(attachment, paramType));
  }

  private Object createAttachmentParameterValue(Attachment attachment, Class paramType) throws Exception {
    byte[] attachmentParamValueBytes = AttachmentConvertor.convertToByteArray(attachment);
    return(String.class.isAssignableFrom(paramType) ? new String(attachmentParamValueBytes) : attachmentParamValueBytes); //$JL-I18N$
  }

  private Attachment getAttachment(ClientConfigurationContext context, String contentID) {
    MIMEMessage mimeMsg = (MIMEMessage)(context.getMessage());
    return(mimeMsg.getAttachmentContainer().getAttachment(contentID));
  }

  private String getAttachmentContentID(XMLTokenReader reader) {
    Attributes attribs = reader.getAttributes();
    return(attribs != null ? attribs.getValue(ATTACHMENT_CID_ATTRIB_NS, ATTACHMENT_CID_ATTRIB_NAME) : null);
  }

  private String determineOperationMEP(OperationMapping operationMapping, ClientConfigurationContext context) {
    String operationMEP = operationMapping.getProperty(OperationMapping.OPERATION_MEP);
    String overrideMEP = (String)(context.getPersistableContext().getProperty(OperationMapping.OPERATION_MEP));
    context.getPersistableContext().removeProperty(OperationMapping.OPERATION_MEP);
    return(overrideMEP != null ? overrideMEP : operationMEP);
  }

  //XI STUFS ==================================================================================================================================================

  private void call_XI(ClientConfigurationContext context) throws RemoteException {
    try {
      resetPerformanceInfo(context);
      ESPXIMessageProcessor espXIMessageProcessor = createESPXIMessageProcessor(context);
      notifyPerformanceStartTime(context, PerformanceInfo.REQUEST_PREPARE_INFO_TYPE);
      OperationMapping operationMapping = getOperationMapping(context);
      ParameterObject[] paramObjs = context.getOperationParameters();
      initialParametersCheck(paramObjs, operationMapping, context);
      ESPXIMessage requestESPXIMessage = createRequestESPXIMessage(espXIMessageProcessor, operationMapping, paramObjs, context);
      notifyPerformanceEndTime(context, PerformanceInfo.REQUEST_PREPARE_INFO_TYPE);
      notifyPerformanceStartTime(context, PerformanceInfo.BACKEND_RESPONSE_INFO_TYPE);
      ESPXIMessage responseESPXIMessage = espXIMessageProcessor.process(requestESPXIMessage);
      notifyPerformanceEndTime(context, PerformanceInfo.BACKEND_RESPONSE_INFO_TYPE);
      if(isOneWayOperation(operationMapping, context)) {
        return;
      }
      notifyPerformanceStartTime(context, PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE);
      if(responseESPXIMessage != null) {
        processResponseESPXIMessage(responseESPXIMessage, operationMapping, paramObjs, context);
        notifyPerformanceEndTime(context, PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE);
      }
    } catch(Throwable thr) {
      if(thr instanceof javax.xml.ws.soap.SOAPFaultException) {
        notifyPerformanceEndTime(context, PerformanceInfo.RESPONSE_PROCESS_INFO_TYPE);
        throw (javax.xml.ws.soap.SOAPFaultException)thr;
      }
      throw new javax.xml.ws.WebServiceException(thr);
    }
  }

  private void processResponseESPXIMessage(ESPXIMessage responseESPXIMessage, OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context) throws Exception {
    deserializeResponseESPXIData(responseESPXIMessage, operationMapping, paramObjs, context);
    initClientContext_ESPXIInboundAttachments(responseESPXIMessage, context);
  }

  private boolean isOneWayOperation(OperationMapping operationMapping, ClientConfigurationContext context) {
    String operationMEP = determineOperationMEP(operationMapping, context);
    return(OperationMapping.MEP_ONE_WAY.equals(operationMEP));
  }

  private ESPXIMessageProcessor createESPXIMessageProcessor(ClientConfigurationContext context) throws NamingException {
    if(espXIMessageProcessor == null) {
      espXIMessageProcessor = getESPXIMessageProcessor(context);
    }
    return(espXIMessageProcessor);
  }

  public static ESPXIMessageProcessor getESPXIMessageProcessor(ClientConfigurationContext clientCfgCtx) throws NamingException {
    ESPXIMessageProcessor xiMsgProc = PublicProperties.getESPXIMessageProcessor(clientCfgCtx);
    if(xiMsgProc == null) {
      xiMsgProc = getESPXIMessageProcessorFromNaming();
    }
    return(xiMsgProc);
  }

  private static ESPXIMessageProcessor getESPXIMessageProcessorFromNaming() throws NamingException {
    InitialContext initialCtx = new InitialContext();
    return((ESPXIMessageProcessor)(initialCtx.lookup(XIFrameworkConstants.JNDI_ESP2XI_MSG_PROC_NAME)));
  }

  private ESPXIMessage createRequestESPXIMessage(ESPXIMessageProcessor espXIMessageProcessor, OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context) throws Exception {
    if(requestESPXIMessage == null) {
      requestESPXIMessage = espXIMessageProcessor.createMessage();
    }
    requestESPXIMessage.clear();
    initRequestESPXIMessage(requestESPXIMessage, operationMapping, paramObjs, context);
    return(requestESPXIMessage);
  }

  private void initRequestESPXIMessage(ESPXIMessage espXIRequestMessage, OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context) throws Exception {
    initRequestESPXIMessage_Data(espXIRequestMessage, operationMapping, paramObjs, context); 
    initRequestESPXIMessage_OutboundAttachments(espXIRequestMessage, context);
    espXIRequestMessage.setApplicationAckRequested(PublicProperties.getXIApplicationAckRequested(context));
    espXIRequestMessage.setApplicationErrorAckRequested(PublicProperties.getXIApplicationErrorAckRequested(context));
    espXIRequestMessage.setQueueId(PublicProperties.getXIQueueId(context));
    espXIRequestMessage.setSenderPartyName(PublicProperties.getXISenderPartyName(context));
    espXIRequestMessage.setSenderService(PublicProperties.getXISenderService(context));
    espXIRequestMessage.setSystemAckRequested(PublicProperties.getXISystemAckRequested(context));
    espXIRequestMessage.setSystemErrorAckRequested(PublicProperties.getXISystemErrorAckRequested(context));
    espXIRequestMessage.setAsync(PublicProperties.isXIAsyncOperation(operationMapping.getWSDLOperationName(), context));
    initRequestESPXIMessage_InterfaceName(espXIRequestMessage, context);
    initRequestESPXIMessage_Receivers(espXIRequestMessage, context);
    // add metering data to message
    Protocol[] meteringProtocol = ConsumerProtocolFactory.protocolFactory.getProtocols(ServiceMeteringConstants.PROTOCOL_NAME);
    if (meteringProtocol.length == 1 && meteringProtocol[0] instanceof XIClientServiceMetering){
      ((XIClientServiceMetering)meteringProtocol[0]).addMeteringDataToXIMessage(espXIRequestMessage);
    }else{
      String trace = "[initRequestESPXIMessage] Cannot add metering data to XIMessage: " + meteringProtocol.length+ " protocols found for " +
                    ServiceMeteringConstants.PROTOCOL_NAME + " name";
      if(meteringProtocol.length == 1){
        trace += "; protocol instance is " + meteringProtocol[0];
      }
      LOC.warningT(trace);
    }
  }

  private void initRequestESPXIMessage_Receivers(ESPXIMessage espXIRequestMessage, ClientConfigurationContext context) {
    Vector<XIReceiver> receivers = PublicProperties.getXIReceivers(context);
    if(receivers != null) {
      for(int i = 0; i < receivers.size(); i++) {
        XIReceiver receiver = receivers.get(i);
        espXIRequestMessage.addReceiver(receiver.getReceiverPartyName(), receiver.getReceiverPartyAgency(), receiver.getReceiverPartyScheme(), receiver.getReceiverService());
      }
    }
  } 

  private void initRequestESPXIMessage_InterfaceName(ESPXIMessage espXIRequestMessage, ClientConfigurationContext context) {
    InterfaceMapping interfaceMapping = context.getStaticContext().getInterfaceData();
    espXIRequestMessage.setServiceInterface(interfaceMapping.getPortType());
  }

  private void initRequestESPXIMessage_Data(ESPXIMessage espXIRequestMessage, OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context) throws Exception {
    String data = serializeRequest(operationMapping, paramObjs, context);
    espXIRequestMessage.setData(data);
  }

  private String serializeRequest(OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context) throws Exception {
    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
    XMLStreamWriter xmlWriter = new XMLStreamWriterImpl(byteArrayOutput, XIFrameworkConstants.XI_MESSAGE_ENCODING);
    try {
      JAXWSUtil.serializeRequestJAXB(operationMapping, paramObjs, context, xmlWriter, null);
      xmlWriter.flush();
      return(byteArrayOutput.toString(XIFrameworkConstants.XI_MESSAGE_ENCODING));
    } finally {
      xmlWriter.close();
    }
  }

  private void initClientContext_ESPXIInboundAttachments(ESPXIMessage responseMessage, ClientConfigurationContext context) throws Exception {
    AttachmentContainer attachementContainer = determineInboundAttachmentContainer(context);
    XIAttachmentHandler.convertXIAttachmentsIntoSOAPAttachments(responseMessage, attachementContainer);
  }

  private AttachmentContainer determineInboundAttachmentContainer(ClientConfigurationContext context) {
    AttachmentContainer inboundAttachments = (AttachmentContainer)(context.getDynamicContext().getProperty(PublicProperties.P_INBOUND_ATTACHMENTS));
    if (inboundAttachments == null) {
      inboundAttachments = new AttachmentContainer();
      context.getDynamicContext().setProperty(PublicProperties.P_INBOUND_ATTACHMENTS, inboundAttachments);
    } else {
      inboundAttachments.clear();
    }
    return(inboundAttachments);
  }

  private void deserializeResponseESPXIData(ESPXIMessage responseMessage, OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context) throws Exception {
    String data = responseMessage.getData();
    if(data == null) {
      throw new IllegalArgumentException("XI response payload data is null!");
    }
    ByteArrayInputStream dataInputStream = new ByteArrayInputStream(data.getBytes(XIFrameworkConstants.XI_MESSAGE_ENCODING));
    XMLTokenReader xmlReader = createXMLTokenReader(dataInputStream);
    try {
      JAXWSUtil.deserializeResponseJAXB(operationMapping, paramObjs, context, xmlReader, SOAPMessage.SOAP11_NS, null, false);
    } finally {
      dataInputStream.close();
    }
  }

  private XMLTokenReader createXMLTokenReader(ByteArrayInputStream dataInputStream) throws ParserException {
    XMLTokenReader xmlReader = XMLTokenReaderFactory.newInstance(dataInputStream);
    xmlReader.begin();
    xmlReader.moveToNextElementStart();
    return(xmlReader);
  }

  private void initRequestESPXIMessage_OutboundAttachments(ESPXIMessage requestESPXIMessage, ClientConfigurationContext context) throws Exception {
    AttachmentContainer attachmentsContainer = (AttachmentContainer)(context.getDynamicContext().getProperty(PublicProperties.P_OUTBOUND_ATTACHMENTS));
    if(attachmentsContainer != null) {
      XIAttachmentHandler.convertSOAPAttachmentsIntoXIAttachments(attachmentsContainer, requestESPXIMessage);
      attachmentsContainer.clear();
    }
  }

  private void resetPerformanceInfo(ClientConfigurationContext clientCfgCtx) {
    PerformanceInfo peformanceInfo = (PerformanceInfo)(clientCfgCtx.getDynamicContext().getProperty(PublicProperties.P_PERFORMANCE_INFO));
    if(peformanceInfo != null) {
      peformanceInfo.reset();
    }
  }

  private void notifyPerformanceStartTime(ClientConfigurationContext clientCfgCtx, int performanceInfoType) {
    PerformanceInfo peformanceInfo = (PerformanceInfo)(clientCfgCtx.getDynamicContext().getProperty(PublicProperties.P_PERFORMANCE_INFO));
    if(peformanceInfo != null) {
      peformanceInfo.notifyStartTime(performanceInfoType);
    }
  }

  private void notifyPerformanceEndTime(ClientConfigurationContext clientCfgCtx, int performanceInfoType) {
    PerformanceInfo peformanceInfo = (PerformanceInfo)(clientCfgCtx.getDynamicContext().getProperty(PublicProperties.P_PERFORMANCE_INFO));
    if(peformanceInfo != null) {
      peformanceInfo.notifyEndTime(performanceInfoType);
    }
  }

  /**
   * Returns true if SAX ContentHandler is handling the response message.
   * @param context
   * @return
   */
  private boolean isSAXHandlerAvailable(ClientConfigurationContext context) {
    ConfigurationContext dynamicContext = context.getDynamicContext();
    return (dynamicContext.getProperty(PublicProperties.SAX_RESPONSE_HANDLER) != null);
  }

  /**
   * Handles Response SOAP Message using SAX Content Handler.
   * @param transport
   * @param handler
   * @return
   * @throws Exception
   */
  private void handleResponseSAX(ClientConfigurationContext context, ConsumerHTTPTransport transport) throws TransportBindingException {
    ConfigurationContext dynamicContext = context.getDynamicContext();
    ContentHandler handler = (ContentHandler) dynamicContext.getProperty(PublicProperties.SAX_RESPONSE_HANDLER);
    XMLReader saxReader = (XMLReader) dynamicContext.getProperty(PublicProperties.SAX_RESPONSE_READER);    
    if (saxReader == null) {
      try {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        saxParserFactory.setValidating(false);      
        SAXParser saxParser = saxParserFactory.newSAXParser();            
        saxReader = saxParser.getXMLReader();
        dynamicContext.setProperty(PublicProperties.SAX_RESPONSE_READER,saxReader);
      } catch (Exception e) {
        String proxyInfo = getLogHttpProxyInfo(context);
        throw new TransportBindingException(TransportBindingException.CONNECTION_ERROR,e, proxyInfo);
      }       
    }
    saxReader.setContentHandler(handler);
    try {
      saxReader.parse(new InputSource(transport.getResponseStream()));
    } catch (IOException e) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR,e,e.getMessage());
    } catch (SAXException e) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR,e,e.getMessage());    }
  }
  

  
  private void investigateExceptionCause(Exception e, String endpoint, ClientConfigurationContext context){ 
    if (e instanceof SocketTimeoutException){
      ConsumerLoggerNew.logSocketTimeout((SocketTimeoutException)e, context, endpoint, LOC);
    }else if (e instanceof IOException && e.getMessage().contains("Invalid SSL message, peer seems to be talking plain!")){
      ConsumerLoggerNew.logNoHTTPSPeer((IOException)e, context, endpoint, LOC);
    }else if (e instanceof IOException && e.getMessage().contains("Unable to connect to")){
      ConsumerLoggerNew.logUnableToConnect((IOException)e, context, endpoint, LOC); 
    }       
  }
  
  
  private String prepareSoapAction(String soapAction, ClientConfigurationContext context) throws RemoteException{
    String soapActionOverride = (String) context.getPersistableContext().getProperty(PublicProperties.P_SOAP_ACTION);
    if (soapActionOverride != null) {                
      soapAction = soapActionOverride;
    }
    if (soapAction == null) {
      soapAction = "";
    }
    
    String backUp = soapAction;
    try {
//      soapAction = URLEncoder.encode(soapAction,"UTF-8");     
      URI uriSoapAction = new URI(soapAction);
      
      soapAction = uriSoapAction.toASCIIString();         
    } catch (URISyntaxException e) {
      String message = "SoapAction:" + backUp + " http heade can not be uri encoded. Sending the unencoded value.";
      LOC.traceThrowableT(Severity.WARNING, message, e);
      soapAction = backUp;
    }
    
    return soapAction;
  }
  
}
