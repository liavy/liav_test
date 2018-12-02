/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.mail.BodyPart;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParameterList;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPElement;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLDOMTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.FaultUtil;
import com.sap.engine.services.webservices.espbase.client.dynamic.WebServiceException;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.ObjectFactory;
import com.sap.engine.services.webservices.jaxrpc.encoding.DeserializerBase;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationState;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPSerializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SerializerBase;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingImpl;
import com.sap.engine.services.webservices.jaxrpc.exceptions.InvalidResponseCodeException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.XmlMarshalException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.XmlUnmarshalException;
import com.sap.engine.services.webservices.jaxrpc.util.CodeGenerator;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.AbstractProtocol;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientTransportBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProxyException;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.AuthenticationFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.DefaultProviders;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.HTTPKeepAliveFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.HeadersFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.ProxyFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.SessionFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.FeatureType;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.GlobalFeatures;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LocalFeatures;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType;
import com.sap.engine.services.webservices.wsdl.WSDLBinding;
import com.sap.engine.services.webservices.wsdl.WSDLBindingChannel;
import com.sap.engine.services.webservices.wsdl.WSDLBindingOperation;
import com.sap.engine.services.webservices.wsdl.WSDLChannel;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.wsdl.WSDLDocumentation;
import com.sap.engine.services.webservices.wsdl.WSDLException;
import com.sap.engine.services.webservices.wsdl.WSDLExtension;
import com.sap.engine.services.webservices.wsdl.WSDLMessage;
import com.sap.engine.services.webservices.wsdl.WSDLNamedNode;
import com.sap.engine.services.webservices.wsdl.WSDLOperation;
import com.sap.engine.services.webservices.wsdl.WSDLPart;
import com.sap.engine.services.webservices.wsdl.WSDLPort;
import com.sap.engine.services.webservices.wsdl.WSDLPortType;
import com.sap.engine.services.webservices.wsdl.WSDLService;

/**
 * Mime binding implementation.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class MimeHttpBinding implements ClientTransportBinding {

  public static final String SOAP_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/";
  public static final String WSI_CLAIM = "Claim";
  public static final String WSI_NAMESPACE = "http://ws-i.org/schemas/conformanceClaim/";
  public static final String SOAP_BINDING_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
  public static final String MIME_BINDING_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/mime/";
  public static final String SOAP_TRANSPORT = "transport";
  public static final String SOAP_STYLE = "style";
  public static final String RPC_STYLE = "rpc";
  public static final String DOCUMENT_STYLE = "document";
  public static final String SOAP_ACTION = "soapAction";
  public static final String SOAP_ACTION_UPPER = "SOAPAction";
  public static final String OPERATION_NAME = "operationName";
  public static final String SOAP_PARTS = "parts";
  public static final String SOAP_USE = "use";
  public static final String SOAP_ENCODING_STYLE = "encodingStyle";
  public static final String SOAP_OPERATION_NAMESPACE = "namespace";
  public static final String SOAP_OPERATION = "operation";
  public static final String MIME_RELATED = "multipartRelated";
  public static final String SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
  public static final String SOAP_BODY = "Body";
  private static final String SOAP_FAULT = "Fault";
  private static final String SOAP_FAULTCODE = "faultcode";
  private static final String SOAP_FAULTSTRING = "faultstring";
  private static final String SOAP_FAULTACTOR = "faultactor";
  private static final String SOAP_FAULTDETAIL = "detail";

  public static final String CONTENT_TYPE_HEADER = "Content-Type";
  public static final String CONTENT_LENGTH_HEADER = "Content-Length";
  public static final String ANY_NAME = "?";

  private TypeMappingRegistry registry;
  private ServiceParam[] inputParams;
  private ServiceParam[] outputParams;
  private ServiceParam[] faultParams;
  private ClientMimeMessage message;
  private ByteArrayOutputStream buffer;
  private HTTPTransport transport;
  private HashSet hashSet;
  private SOAPSerializationContext serialziationContext;
  private SOAPDeserializationContext deserializationContext;
  public static final String NAME = "SOAP 1.1 HTTP Binding with Attachments";
  private ClassLoader appClassloader = null;
  private ArrayList faultRefs = new ArrayList();
  private XMLReader saxReader = null;
  private SAXParserFactory spf = SAXParserFactory.newInstance();

  /**
   * Default constructor.
   */
  public MimeHttpBinding() {
    message = new ClientMimeMessage();
    transport = new HTTPTransport();
    buffer = new ByteArrayOutputStream();
    hashSet = new HashSet();
    serialziationContext = new SOAPSerializationContext();
    deserializationContext = new SOAPDeserializationContext();
  }

  /**
   * Adds constructor code. Called by proxy generator to give binding ability to add some stub constructor code.
   */
  public void addConstructorCode(CodeGenerator generator) {
  }

  /**
   * Called from proxy generator to give binding ability to add import statements into stub.
   */
  public void addImport(CodeGenerator generator) {
  }

  /**
   * Gives the binding ability to generate code for additional stub variables.
   */
  public void addVariables(CodeGenerator generator) {
  }

  /**
   * Returns true if this contentType is supported.
   */
  private boolean checkContentType(String contentType) {
    if (contentType.trim().indexOf("text/plain") != -1) {
      return true;
    }
    if (contentType.trim().indexOf("application/octetstream") != -1) {
      return true;
    }
    if (contentType.trim().indexOf("application/octet-stream") != -1) {
      return true;
    }
    return false;
  }
  /**
   * Returns array of ints with mark's where to put message parts.
   */
  private int[] getPartLocation(PropertyContext config, ServiceParam[] parts) throws Exception {
    int[] result = new int[parts.length];
    // 1 - body transport, 2 - attachments transport, 3 - header transport ,0 - don't transport at all
    String bodyParts = (String) config.getProperty(SOAP_PARTS);
    if (bodyParts == null) { // Not Present at all then all are in body by default
      for (int i=0; i<result.length; i++) {
        result[i] = 1;
      }
    } else {
      hashSet.clear();
      StringTokenizer st = new StringTokenizer(bodyParts);
      while (st.hasMoreTokens()) {
        hashSet.add(st.nextToken());
      }
      for (int i=0; i<parts.length; i++) {
        String partName = parts[i].name;
        if (hashSet.contains(partName)) {
          result[i] = 1;
        }
      }
    }
    PropertyContext context = config.getSubContext("mime");
    if (context.isDefined()) {
      for (int i=0; i<parts.length; i++) {
        String contentType = (String) context.getProperty(parts[i].name);
        if (contentType != null) {
          if (checkContentType(contentType)) {
            result[i] = 2; // mime part
          } else {
            throw new Exception(" Binding does not support content-type :"+contentType);
          }
        }
      }
    }
    context = config.getSubContext("headers");
    if (context.isDefined()) {
      for (int i=0; i<parts.length; i++) {
        String use = (String) context.getProperty(parts[i].name);
        if (use != null) {
          result[i] = 3;
        }
      }

    }
    return result;
  }

  private void addAttachment(Object object, String contentType, String contentId, ClientMimeMessage message) throws Exception {
    if (contentType.trim().indexOf("text/plain") != -1) {
      MimeMultipart multiPart = message.getMultiPartObject();
      InternetHeaders headers = new InternetHeaders();
      headers.addHeader("Content-type", contentType);
      headers.addHeader("Content-ID", "<" + contentId + ">");
      MimeBodyPart part = new MimeBodyPart(headers, ((String) object).getBytes()); //$JL-I18N$
      multiPart.addBodyPart(part);
    }
    if (contentType.trim().indexOf("application/octetstream") != -1) {
      MimeMultipart multiPart = message.getMultiPartObject();
      InternetHeaders headers = new InternetHeaders();
      headers.addHeader("Content-type", contentType);
      headers.addHeader("Content-ID", "<" + contentId + ">");
//      headers.addHeader("Content-Transfer-Encoding", "base64");
//      MimeBodyPart part = new MimeBodyPart(headers, com.sap.engine.lib.xml.util.BASE64Encoder.encode((byte[]) object));
      headers.addHeader("Content-Transfer-Encoding", "binary");
      MimeBodyPart part = new MimeBodyPart(headers, (byte[]) object);
      multiPart.addBodyPart(part);
    }

  }

  private void addConformanceClaim(PropertyContext config, ClientSOAPMessage message) throws Exception {
    String conforms = (String) config.getProperty("conformsTo");
    if (conforms != null) {
      Element element = message.createSoapHeader(WSI_NAMESPACE,WSI_CLAIM);
      element.setAttribute("conformsTo",conforms);
      message.getHeaders().add(element);
    }
  }
  /**
   * Sends rpc style request. The writer is already entered in the soap:body
   * This makes only body content. Operation name and namespace is mapped in context
   */
  private void buildRequestRpc(PropertyContext config, ClientMimeMessage message) throws Exception {
    ClientSOAPMessage soapMessage = message.getSOAPMessage();
    XMLTokenWriter writer = soapMessage.getWriter();
    //String parts = (String) config.getProperty(SOAP_PARTS);
    int[] partLocation = getPartLocation(config, inputParams);
    serialziationContext.clearContext();
    TypeMappingImpl typeMapping = (TypeMappingImpl) registry.getDefaultTypeMapping();
    serialziationContext.setTypeMapping(typeMapping);
    String operationName = (String) config.getProperty(OPERATION_NAME);
    String operationNamespace = (String) config.getProperty(SOAP_OPERATION_NAMESPACE);
    String encodingStyle = (String) config.getProperty(SOAP_ENCODING_STYLE);
    String use = (String) config.getProperty(SOAP_USE);
    if (operationName == null) {
      throw new Exception(" Operation name for rpc is not available !");
    }
    addConformanceClaim(config,soapMessage);
    writer.enter(SOAP_ENVELOPE, "Body");
    serialziationContext.setTypeUse(true);
    if ("encoded".equals(use)) { // If style is encoded set property for framework
      serialziationContext.setEncoded(true);

      if (encodingStyle != null) {
        writer.writeAttribute(SOAP_ENVELOPE,SOAP_ENCODING_STYLE,encodingStyle);
      } else {
        throw new Exception(" In encoded representation encoding style must be specified !");
      }
    }
    writer.enter(operationNamespace,operationName);
    for (int i=0; i<inputParams.length; i++) { // Writes part content
      if (partLocation[i] != 0) {
        if (inputParams[i].isElement) {
          if (partLocation[i] == 1) { // body part
            if (inputParams[i].content != null) {
              inputParams[i].contentClass = inputParams[i].content.getClass();
            }
            writer.enter(operationNamespace,inputParams[i].name);
            writer.enter(inputParams[i].schemaName.getNamespaceURI(),inputParams[i].schemaName.getLocalPart());
            QName elementType = typeMapping.getTypeForElement(inputParams[i].schemaName);
            SerializerBase serializer = serialziationContext.getSerializer(elementType,inputParams[i].contentClass);
            //SerializerFactory factory = typeMapping.getSerializer(inputParams[i].contentClass,elementType);
            //SerializerBase serializer = (SerializerBase) factory.getSerializerAs(null);
            serializer.serialize(inputParams[i].content,writer,serialziationContext);
            writer.leave();
          } else if (partLocation[i] == 3) { // header part
            boolean flag = serialziationContext.isEncoded();
            serialziationContext.setEncoded(false);
            if (inputParams[i].content != null) {
              inputParams[i].contentClass = inputParams[i].content.getClass();
            }
            QName elementType = typeMapping.getTypeForElement(inputParams[i].schemaName);
            SerializerBase serializer = serialziationContext.getSerializer(elementType,inputParams[i].contentClass);
            //SerializerFactory factory = typeMapping.getSerializer(inputParams[i].contentClass,elementType);
            //SerializerBase serializer = (SerializerBase) factory.getSerializerAs(null);
            Element headerElement = soapMessage.createSoapHeader(inputParams[i].schemaName.getNamespaceURI(),inputParams[i].schemaName.getLocalPart());
            serializer.serialize(inputParams[i].content,headerElement,serialziationContext);
            soapMessage.getHeaders().add(headerElement);
            serialziationContext.setEncoded(flag);
          } else { // attachment part
            writer.enter(operationNamespace,inputParams[i].name);
            writer.enter(inputParams[i].schemaName.getNamespaceURI(),inputParams[i].schemaName.getLocalPart());
            if (inputParams[i].content == null) {
              writer.writeAttribute(NS.XSI, "nil", "true");
              writer.leave();
            } else {
              String cid = getCIDUniqueValue()+inputParams[i].name;
              writer.writeAttribute(null,"href","cid:"+cid);
              writer.leave();
              // content type can not be null
              String contentType = (String) config.getSubContext("mime").getProperty(inputParams[i].name);
              addAttachment(inputParams[i].content, contentType, cid, message);
            }
            writer.leave();
          }
        } else {
          if (partLocation[i] == 1) { // body part
            if (inputParams[i].content != null) {
              inputParams[i].contentClass = inputParams[i].content.getClass();
            }
            writer.enter(null,inputParams[i].name);
            SerializerFactory factory = typeMapping.getSerializer(inputParams[i].contentClass,inputParams[i].schemaName);
            SerializerBase serializer = (SerializerBase) factory.getSerializerAs("");
            serializer.serialize(inputParams[i].content,writer,serialziationContext);
          } else if (partLocation[i] == 3) { // header part
            if (inputParams[i].content != null) {
              inputParams[i].contentClass = inputParams[i].content.getClass();
            }
            QName elementType = typeMapping.getTypeForElement(inputParams[i].schemaName);
            SerializerBase serializer = serialziationContext.getSerializer(elementType,inputParams[i].contentClass);
            //SerializerFactory factory = typeMapping.getSerializer(inputParams[i].contentClass,elementType);
            //SerializerBase serializer = (SerializerBase) factory.getSerializerAs(null);
            Element headerElement = soapMessage.createSoapHeader(inputParams[i].schemaName.getNamespaceURI(),inputParams[i].schemaName.getLocalPart());
            serializer.serialize(inputParams[i].content,headerElement,serialziationContext);
            soapMessage.getHeaders().add(headerElement);
          } else { // mime part
            writer.enter(null,inputParams[i].name);
            if (inputParams[i].content == null) {
              writer.writeAttribute(NS.XSI, "nil", "true");
              writer.leave();
            } else {
              String cid = getCIDUniqueValue()+inputParams[i].name;
              writer.writeAttribute(null,"href","cid:"+cid);
              writer.leave();
              // content type can not be null
              String contentType = (String) config.getSubContext("mime").getProperty(inputParams[i].name);
              addAttachment(inputParams[i].content, contentType, cid, message);
            }
          }
        }
      }
    }
    writer.leave(); // leaves operation
    writer.leave(); // leaves soap:body
    writer.flush();
  }

  /**
   * Build's document style request.
   */
  private void buildRequestDocument(PropertyContext config, ClientMimeMessage message) throws Exception {
    ClientSOAPMessage soapMessage = message.getSOAPMessage();
    XMLTokenWriter writer = soapMessage.getWriter();
    //String parts = (String) config.getProperty(SOAP_PARTS);
    int[] partLocation = getPartLocation(config, inputParams);
    SOAPSerializationContext serialziationContext = new SOAPSerializationContext();
    TypeMappingImpl typeMapping = (TypeMappingImpl) registry.getDefaultTypeMapping();
    serialziationContext.setTypeMapping(typeMapping);
    addConformanceClaim(config,soapMessage);
    // Outputs soap:Body
    writer.enter(SOAP_ENVELOPE, SOAP_BODY);
    // The parts can only be elements not types
    for (int i=0; i<inputParams.length; i++) {
      if (inputParams[i].isElement == false) {
        throw new Exception(" Document style does not allow type parts !");
      }
      if (partLocation[i] == 1) { // Body Location
        if (inputParams[i].content != null) {
          inputParams[i].contentClass = inputParams[i].content.getClass();
        }
        writer.enter(inputParams[i].schemaName.getNamespaceURI(),inputParams[i].schemaName.getLocalPart());
        QName elementType = typeMapping.getTypeForElement(inputParams[i].schemaName);
        SerializerBase serializer = serialziationContext.getSerializer(elementType,inputParams[i].contentClass);
        //SerializerFactory factory = typeMapping.getSerializer(inputParams[i].contentClass,elementType);
        //SerializerBase serializer = (SerializerBase) factory.getSerializerAs("");
        serializer.serialize(inputParams[i].content,writer,serialziationContext);
      }
      if (partLocation[i] == 2) { // Attachment Location
        writer.enter(inputParams[i].schemaName.getNamespaceURI(),inputParams[i].schemaName.getLocalPart());
        if (inputParams[i] == null) {
          writer.writeAttribute(NS.XSI, "nil", "true");
          writer.leave();
        } else {
          String cid = getCIDUniqueValue()+inputParams[i].name;
          writer.writeAttribute("", "href", "cid:" + cid);
          writer.leave(); // leaves element
          // content type can not be null
          String contentType = (String) config.getSubContext("mime").getProperty(inputParams[i].name);
          addAttachment(inputParams[i],contentType,cid,message);
        }
      }
      if (partLocation[i] == 3) { // header part
        boolean flag = serialziationContext.isEncoded();
        serialziationContext.setEncoded(false);
        if (inputParams[i].content != null) {
          inputParams[i].contentClass = inputParams[i].content.getClass();
        }
        QName elementType = typeMapping.getTypeForElement(inputParams[i].schemaName);
        SerializerBase serializer = serialziationContext.getSerializer(elementType,inputParams[i].contentClass);
        //SerializerFactory factory = typeMapping.getSerializer(inputParams[i].contentClass,elementType);
        //SerializerBase serializer = (SerializerBase) factory.getSerializerAs(null);
        Element headerElement = soapMessage.createSoapHeader(inputParams[i].schemaName.getNamespaceURI(),inputParams[i].schemaName.getLocalPart());
        serializer.serialize(inputParams[i].content,headerElement,serialziationContext);
        soapMessage.getHeaders().add(headerElement);
        serialziationContext.setEncoded(flag);
      }
    }
    writer.leave(); // leaves soap:body
    writer.flush();
  }

  /**
   * Special method to support the message server redirect code.
   * Do not normally use it !!!
   * @param message
   * @param transport
   * @return
   * @throws Exception
   */
  private String outputMessageSpecial(ClientMimeMessage message,HTTPTransport transport) throws Exception {
    // Writes message contents
    transport.setHeader(CONTENT_TYPE_HEADER,"text/xml; charset=UTF-8");
    buffer.reset();
    message.getSOAPMessage().writeTo(buffer);
    buffer.flush();
    transport.setHeader(CONTENT_LENGTH_HEADER,String.valueOf(buffer.size()));
    // Writes request headers
    OutputStream output = transport.getRequestStream();
    output.flush();
    // Tries to get the response headers
    int expectedResponseCode;
    transport.setSocketTimeoutFast(10);
    boolean contentSent = false;
    try {
      expectedResponseCode = transport.getResponseCode();
      transport.resetSocketTimeoutFast();
    } catch (InterruptedIOException x) {
      transport.resetSocketTimeoutFast();
      // the connection is alive but does not return nothing
      contentSent = true;
      buffer.writeTo(output);
      output.flush();
      expectedResponseCode = transport.getResponseCode();
    }
    if (expectedResponseCode == 301 || expectedResponseCode == 302) { // redirect implementation (does not care about content send)
      // Saves the original headers.
      Hashtable temp = new Hashtable(transport.getHeaders());
      InputStream tempStream = transport.getResponseStream();
      while (tempStream.read() != -1) {}
      String[] headers = transport.getHeader("location");
      transport.closeSession();
      if (headers.length!=0) { // redirect request
        URL root =  URLLoader.fileOrURLToURL(null,transport.getEndpoint());
        URL relative = URLLoader.fileOrURLToURL(root, headers[0]);
        if (relative != null) {
          transport.getHeaders().clear();
          transport.setEndpoint(relative.toExternalForm()); // new endppoint
          transport.getHeaders().putAll(temp); // restores the original headers
          output = transport.getRequestStream();
          buffer.writeTo(output);
          output.flush();
          return relative.toExternalForm();
        }
      }
    }
    if (expectedResponseCode == 400 && contentSent == false) { // Bad request and the content is not sent
      Hashtable temp = new Hashtable(transport.getHeaders());
      InputStream tempStream = transport.getResponseStream();
      while (tempStream.read() != -1) {}
      transport.getHeaders().clear();
      transport.getHeaders().putAll(temp); // restores the original headers
      output = transport.getRequestStream();
      buffer.writeTo(output);
      output.flush();
    } else {
      if (contentSent == false) {
        throw new IOException("The message content was not sent but the server returned answer !");
        // some response returned - read it
      } else {
        // The content was sent so no problem
      }

    }
    return null;
  }

  private String outputMessage(ClientMimeMessage message,HTTPTransport transport, boolean redirectMode) throws Exception {
    int partCount = message.getMultiPartObject().getCount();
    if (partCount > 0) {  // Mime binding
      ContentType cntType = new ContentType(message.getMultiPartObject().getContentType());
      String boundaryValue = cntType.getParameter("boundary");
      ParameterList list = new ParameterList();
      list.set("type", "text/xml");
      list.set("boundary", boundaryValue);
      transport.setHeader(CONTENT_TYPE_HEADER,new ContentType("multipart", "related", list).toString());
      buffer.reset();
      message.writeTo(buffer);
      buffer.flush();
      transport.setHeader(CONTENT_LENGTH_HEADER,String.valueOf(buffer.size()));
      OutputStream output = transport.getRequestStream();
      buffer.writeTo(output);
      output.flush();
      int responseCode = transport.getResponseCode();
      if (responseCode == 301 || responseCode == 302) { // Redirect
        // Saves the original headers.
        Hashtable temp = new Hashtable(transport.getHeaders());
        InputStream tempStream = transport.getResponseStream();
        while (tempStream.read() != -1) {} // Reads the response stream
        String[] headers = transport.getHeader("location");
        transport.closeSession();
        if (headers.length!=0) { // redirect request
          URL root =  URLLoader.fileOrURLToURL(null,transport.getEndpoint());
          URL relative = URLLoader.fileOrURLToURL(root, headers[0]);
          if (relative != null) {
            transport.getHeaders().clear();
            transport.setEndpoint(relative.toExternalForm()); // new endppoint
            transport.getHeaders().putAll(temp); // restores the original headers
            output = transport.getRequestStream();
            buffer.writeTo(output);
            output.flush();
            return relative.toExternalForm();
          }
        }
      }

    } else {
      if (redirectMode) {
        return outputMessageSpecial(message,transport);
      } else {
        // Original message handling
      transport.setHeader(CONTENT_TYPE_HEADER,"text/xml; charset=UTF-8");
      buffer.reset();
      message.getSOAPMessage().writeTo(buffer);
      buffer.flush();
      transport.setHeader(CONTENT_LENGTH_HEADER,String.valueOf(buffer.size()));
      OutputStream output = transport.getRequestStream();
      buffer.writeTo(output);
      output.flush();
      int responseCode = transport.getResponseCode();
      if (responseCode == 301 || responseCode == 302) { // Redirect
        // Saves the original headers.
        Hashtable temp = new Hashtable(transport.getHeaders());
        InputStream tempStream = transport.getResponseStream();
        while (tempStream.read() != -1) {}
        String[] headers = transport.getHeader("location");
        transport.closeSession();
        if (headers.length!=0) { // redirect request
          URL root =  URLLoader.fileOrURLToURL(null,transport.getEndpoint());
          URL relative = URLLoader.fileOrURLToURL(root, headers[0]);
          if (relative != null) {
            transport.getHeaders().clear();
            transport.setEndpoint(relative.toExternalForm()); // new endppoint
            transport.getHeaders().putAll(temp); // restores the original headers
            output = transport.getRequestStream();
            buffer.writeTo(output);
            output.flush();
            return relative.toExternalForm();
          }
        }
    }
    }
    }
    return null;
  }

  /**
   * Reads SOAP Message Fault and builds SOAP Fault Exception.
   */
  private SOAPFaultException buildFaultException(ClientSOAPMessage message, ArrayList remainingElements)  throws Exception {
    XMLTokenReader reader = message.getReader();
    if (reader == null) { // In case DOM message is built
      Element soapBody = message.getSoapBody();
      reader = new  XMLDOMTokenReader(soapBody);
      reader.begin();
    }
    return FaultUtil.buildFaultException_SOAP11(reader,remainingElements);
  }

  /**
   * Handles parsing of SOAP Custom Exceptions and deserialization. For rps style faults.
   */
  private void processRpcFault(ClientSOAPMessage message,ObjectFactory objFactory) throws Exception {
    faultRefs.clear();
    SOAPFaultException exception = buildFaultException(message,faultRefs);
    SOAPElement element = FaultUtil.getDetailRootElement(exception.getDetail());
    if (element != null) {
      for (int i=0; i<faultParams.length; i++) {
        String name = faultParams[i].name;
        if (faultParams[i].isElement) {
          name = faultParams[i].schemaName.getLocalPart();
        }
        if (element.getLocalName().equals(name)) {
          Class faultClass = faultParams[i].contentClass;
          TypeMappingImpl typeMapping = (TypeMappingImpl) registry.getDefaultTypeMapping();
          QName type = faultParams[i].schemaName;
          if (faultParams[i].isElement) {
            type = ((ExtendedTypeMapping) typeMapping).getTypeForElement(type);
            if (type == null) {
              if (objFactory != null) {
                throw new InvocationTargetException(exception,exception.getFaultString());
              } else {
                throw exception;
              }
            }
          }
          deserializationContext.clearContext();
          deserializationContext.setApplicationClassLoader(appClassloader);
          deserializationContext.setTypeMapping(typeMapping);
          if (objFactory != null) {
            deserializationContext.setObjectFactory(objFactory);
          deserializationContext.setObjectFactory(objFactory);
            Class contentClass = typeMapping.getDefaultJavaClass(type);
            DeserializerFactory factory = typeMapping.getDeserializer(contentClass,type);
            if (factory == null) { // No deserializer for this type found
              throw exception;
            }
            DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs(null);
            XMLDOMTokenReader reader = new XMLDOMTokenReader(element);
            reader.begin();
            Object perm = deserializer.deserialize(reader,deserializationContext,contentClass);
            reader.end();;
            WebServiceException wsException = new WebServiceException(exception.getFaultString(),exception,faultParams[i].name,perm,type);
            InvocationTargetException invokeException = new InvocationTargetException(wsException);
            faultParams[i].content = invokeException;
            throw invokeException;
          } else {
            faultParams[i].content = faultClass.newInstance();
            Class contentClass = ((ProxyException) faultParams[i].content).getContentClass();
            DeserializerFactory factory = typeMapping.getDeserializer(contentClass,type);
            if (factory == null) {
              throw exception;
            }
          for (int j=0; j<faultRefs.size(); j++) {
            SOAPElement soapElement = (SOAPElement) faultRefs.get(j);
            deserializationContext.setIdContent(soapElement.getAttribute("id"),soapElement);
          }
          DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs("");
          Object perm = deserializer.deserialize(element,deserializationContext,contentClass);
          Class[] classes = new Class[1];
          classes[0] = contentClass;
          Method method = faultClass.getMethod("init",classes);
          if (method != null) {
            method.invoke(faultParams[i].content,new Object[] {perm});
          }
          ((ProxyException) faultParams[i].content)._setFaultActor(exception.getFaultActor());
          ((ProxyException) faultParams[i].content)._setFaultCode(exception.getFaultCode());
          ((ProxyException) faultParams[i].content)._setFaultString(exception.getFaultString());
          ((Exception) faultParams[i].content).fillInStackTrace();
          }
          return;
        }
      }
      if (objFactory != null) {
        throw new InvocationTargetException(exception,exception.getFaultString());
      } else {
      throw exception;
      }
    } else {
      if (objFactory != null) {
        throw new InvocationTargetException(exception,exception.getFaultString());
    } else {
      throw exception;
    }
  }
  }

  /**
   * Handles parsing of SOAP Custom Exceptions and deserialization. For document style faults.
   */
  private void processDocumentFault(ClientSOAPMessage message,ObjectFactory objFactory) throws Exception {
    faultRefs.clear(); // Document style faults do not contain regerences
    SOAPFaultException exception = buildFaultException(message,faultRefs);
    SOAPElement element = FaultUtil.getDetailRootElement(exception.getDetail());
    if (element != null) { // Fault detail content element is found
      for (int i=0; i<faultParams.length; i++) {
        if (element.getLocalName().equals(faultParams[i].schemaName.getLocalPart())) {
          if (faultParams[i].isElement == false) {
            // TODO: Correct Exception message - This check should be done generation time
            throw new Exception(" Document style does not allow type links to parts !");
          }
          TypeMappingImpl typeMapping = (TypeMappingImpl) registry.getDefaultTypeMapping();
          Class faultClass = faultParams[i].contentClass;
          QName type = typeMapping.getTypeForElement(faultParams[i].schemaName);
          if (type == null) { // No proper type found
            if (objFactory != null) { // Object Factory is used only in dynamic proxy
              throw new InvocationTargetException(exception,exception.getFaultString());
            } else {
            throw exception;
          }
          }
          deserializationContext.clearContext();
          deserializationContext.setApplicationClassLoader(appClassloader);
          deserializationContext.setTypeMapping(typeMapping);
          if (objFactory != null) { // Generic use case
          deserializationContext.setObjectFactory(objFactory);
            Class contentClass = typeMapping.getDefaultJavaClass(type);
            DeserializerFactory factory = typeMapping.getDeserializer(contentClass,type);
            if (factory == null) { // No deserializer for this type found
              throw new InvocationTargetException(exception,exception.getFaultString());
            }
            DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs(null);
            XMLDOMTokenReader reader = new XMLDOMTokenReader(element);
            reader.begin();
            Object perm = deserializer.deserialize(reader,deserializationContext,contentClass);
            reader.end();;
            WebServiceException wsException = new WebServiceException(exception.getFaultString(),exception,faultParams[i].name,perm,type);
            InvocationTargetException invokeException = new InvocationTargetException(wsException);
            faultParams[i].content = invokeException;
            throw invokeException;
          } else { // Non generic use case
            faultParams[i].content = faultClass.newInstance();
            Class contentClass = ((ProxyException) faultParams[i].content).getContentClass();
            DeserializerFactory factory = typeMapping.getDeserializer(contentClass,type);
            if (factory == null) { // No deserializer for this type found
              throw exception;
          }
            DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs(null);
            XMLDOMTokenReader reader = new XMLDOMTokenReader(element);
            reader.begin();
            Object perm = deserializer.deserialize(reader,deserializationContext,contentClass);
            reader.end();;
          Class[] classes = new Class[1];
          classes[0] = contentClass;
          Method method = faultClass.getMethod("init",classes);
          if (method != null) {
            method.invoke(faultParams[i].content,new Object[] {perm});
          }
          ((ProxyException) faultParams[i].content)._setFaultActor(exception.getFaultActor());
          ((ProxyException) faultParams[i].content)._setFaultCode(exception.getFaultCode());
          ((ProxyException) faultParams[i].content)._setFaultString(exception.getFaultString());
          ((Exception) faultParams[i].content).fillInStackTrace();
          }
          return;
        }
      }
      if (objFactory != null) {
        throw new InvocationTargetException(exception,exception.getFaultString());
      } else {
      throw exception;
      }
    } else {
      if (objFactory != null) {
        throw new InvocationTargetException(exception,exception.getFaultString());
    } else {
      throw exception;
    }
  }
  }

  /**
   * Returns id reference value.
   */
  private static String getCIDUniqueValue() {
    long ln = System.currentTimeMillis();
    return Long.toString(ln);
  }

  /**
   * Handles response message. And transport response codes.
   */
  private int handleResponseMessage(HTTPTransport transport, ClientMimeMessage message) throws Exception {
    int responseCode = transport.getResponseCode();
    String contentType = transport.getContentType();
    if (responseCode == 200) { // message is processed
      if (outputParams != null) { // not one way message
        message.initDeserializationMode(transport);
      }
      return responseCode;
    }
    if (responseCode == 202) { // message is accepted
      return responseCode;
    }
    if (responseCode == 500) { // internal server error
      if (contentType.indexOf("text/xml") != -1) { // soap message fault
        message.initDeserializationMode(transport);
        return responseCode;
      } else {
        InputStream response = transport.getResponseStream();
        int maxHTMLToRead = 10240;
        byte[] buffer = new byte[maxHTMLToRead];
        int readBytes;
        int pos = 0;
        while ((readBytes = response.read(buffer, pos, maxHTMLToRead - pos)) != -1) {
          pos += readBytes;
          if (pos >= maxHTMLToRead) {
            break;
        }
        }
        String str = new String(buffer,0,pos); //$JL-I18N$
        throw new Exception("Call to {"+transport.getEndpoint()+"} returned http code 500 ("+transport.getResponseMessage()+") with unacceptable content type ("+contentType+").\n"+str);
      }
    }
    String responseMessage = transport.getResponseMessage();
    Hashtable responseHeaders = transport.getHeaders();
    throw new InvalidResponseCodeException(responseCode, responseMessage , responseHeaders, transport.getEndpoint());
//    throw new Exception("Transport Error ! Response code ("+responseCode+") "+transport.getResponseMessaget());
  }

  private Object loadAttachment(String reference, String partName,ClientMimeMessage message) throws Exception {
    BodyPart bodyPart = message.getPart(reference);
    if (bodyPart == null) {
     throw new Exception(" Part '"+partName+"' not found as attachment !");
    }
    String contentType = bodyPart.getContentType();
    if (checkContentType(contentType) == false) {
      throw new Exception(" Content-type '"+contentType+"' not supported !");
    }
    if (contentType.indexOf("text/plain") != -1){
      return createStringFromStream(bodyPart.getInputStream(),contentType);
    }
    if (contentType.indexOf("application/octetstream") != -1) {
      return createByteArrayFromStream(bodyPart.getInputStream());
    }
    if (contentType.indexOf("application/octet-stream") != -1) {
      return createByteArrayFromStream(bodyPart.getInputStream());
    }
    return null;
  }

  /**
   * Returns mime header value
   */
  private static String getHeaderValue(BodyPart part, String headerName) throws Exception {
    try {
      String[] str = part.getHeader(headerName);
      if (str != null && str.length == 1) {
        return str[0];
      }
      return null;
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }


  /**
   * Reads document style response.
   * If message is in DOMMode employs DOM deserialization.
   * If message is in not in DOMMode employs Stream Pull Parser deserialziation.
   */
  private boolean getResponseDocument(PropertyContext context, ClientMimeMessage message,ObjectFactory objFactory) throws Exception {
    //String parts = (String) context.getProperty(SOAP_PARTS);
    int[] partLocation = getPartLocation(context,outputParams);
    //SOAPDeserializationContext deserialziationContext = new SOAPDeserializationContext();
    deserializationContext.clearContext();
    deserializationContext.setApplicationClassLoader(appClassloader);
    TypeMappingImpl typeMapping = (TypeMappingImpl) registry.getDefaultTypeMapping();
    deserializationContext.setTypeMapping(typeMapping);
    deserializationContext.setObjectFactory(objFactory);
    ClientSOAPMessage soapMessage = message.getSOAPMessage();
    XMLTokenReader reader = null;
    if (soapMessage.isDOMbuilt()) {
      Element body = soapMessage.getSoapBody();
      /*
      // Check for fault
      Element fault = getElementChildNull(SOAP_FAULT,body);
      if (fault != null && SOAP_ENVELOPE.equals(fault.getNamespaceURI())) {
        // SOAP Fault
        processDocumentFault(message.getSOAPMessage(),objFactory);
        return false;
      }
      // The parts can only be elements not types
      for (int i=0; i<outputParams.length; i++) {
        if (partLocation[i] == 1 || partLocation[i] == 2) {
          Element param = getElementChildNull(outputParams[i].schemaName.getLocalPart(),body);
          if (param == null) {
            throw new Exception("Unexpected response ! Expected part in message is missing !");
          } else {
            if (partLocation[i] == 1) {
              QName elementType = typeMapping.getTypeForElement(outputParams[i].schemaName);
              DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,elementType);
              DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs("");
              outputParams[i].content = deserializer.deserialize(param,deserializationContext,outputParams[i].contentClass);
            }
            if (partLocation[i] == 2) {
              String href = param.getAttribute("href");
              if (href.length() != 0) {
                outputParams[i].content = loadAttachment(href,outputParams[i].name,message);
              }
            }
          }
        }
        if (partLocation[i] == 3) { // header field
          Element element = soapMessage.getSoapHeader(outputParams[i].schemaName.getNamespaceURI(),outputParams[i].schemaName.getLocalPart());
          if (element != null) {
            QName elementType = typeMapping.getTypeForElement(outputParams[i].schemaName);
            DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,elementType);
            DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs(null);
            outputParams[i].content = deserializer.deserialize(element,deserializationContext,outputParams[i].contentClass);
          }
        }

      }*/
      reader = new XMLDOMTokenReader(body);
      reader.begin();
    } else {
      reader = soapMessage.getReader();
      }
      reader.passChars();
      if (reader.getState() == XMLTokenReader.STARTELEMENT && reader.getLocalName().equals("Body") && SOAP_ENVELOPE.equals(reader.getURI())) {
        reader.next();
        reader.passChars();
        // Check for fault
        if (reader.getState() == XMLTokenReader.STARTELEMENT && SOAP_FAULT.equals(reader.getLocalName()) && SOAP_ENVELOPE.equals(reader.getURI())) {
          // SOAP Fault
          processDocumentFault(message.getSOAPMessage(),objFactory);
          return false;
        }
        // The parts can only be elements not types
        for (int i=0; i< outputParams.length; i++) {
          if (outputParams[i].isElement == false) {
            throw new Exception(" Document style does not allow type parts !");
          }
          if (partLocation[i] == 1 || partLocation[i] == 2) {
            reader.passChars();
            if (reader.getState() != XMLTokenReader.STARTELEMENT) {
              throw new Exception(" Element '{"+outputParams[i].schemaName.getNamespaceURI()+"}"+outputParams[i].schemaName.getLocalPart()+"' not found in response message.");
            } else {
              String localName = reader.getLocalName();
              String namespace = reader.getURI();
              if (localName.equals(outputParams[i].schemaName.getLocalPart()) && namespace.equals(outputParams[i].schemaName.getNamespaceURI())) {
                if (partLocation[i] == 1) {
                  QName elementType = typeMapping.getTypeForElement(outputParams[i].schemaName);
                  DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,elementType);
                  DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs("");
                  outputParams[i].content = deserializer.deserialize(reader, deserializationContext, outputParams[i].contentClass);
                }  else if (partLocation[i] == 3) { // header field
                  Element element = soapMessage.getSoapHeader(outputParams[i].schemaName.getNamespaceURI(),outputParams[i].schemaName.getLocalPart());
                  if (element != null) {
                    XMLDOMTokenReader headerReader = new XMLDOMTokenReader(element);
                    headerReader.begin();
                    QName elementType = typeMapping.getTypeForElement(outputParams[i].schemaName);
                    DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,elementType);
                    DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs(null);
                    outputParams[i].content = deserializer.deserialize(headerReader,deserializationContext,outputParams[i].contentClass);
                    //outputParams[i].content = deserializer.deserialize(element,deserializationContext,outputParams[i].contentClass);
                    headerReader.end();
                  }
                } else {
                  Attributes attributes = reader.getAttributes();
                  String href = attributes.getValue("href");
                  if (href != null) {
                    outputParams[i].content = loadAttachment(href,outputParams[i].name, message);
                  } else {
                    throw new Exception(" Attached part must have href attribute !");
                  }
                  reader.passChars();
                  if (reader.getState() != XMLTokenReader.ENDELEMENT) {
                    throw new Exception(" Attached part must be empty !");
                  }
                  reader.next(); // leaves part
                }
              } else {
                throw new Exception(" Part '"+outputParams[i].name+"' not found in response !");
              }
            }
          }
          if (partLocation[i] == 3) { // header field
            Element element = soapMessage.getSoapHeader(outputParams[i].schemaName.getNamespaceURI(),outputParams[i].schemaName.getLocalPart());
            if (element != null) {
              XMLDOMTokenReader headerReader = new XMLDOMTokenReader(element);
              headerReader.begin();
              QName elementType = typeMapping.getTypeForElement(outputParams[i].schemaName);
              DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,elementType);
              DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs(null);
              outputParams[i].content = deserializer.deserialize(reader,deserializationContext,outputParams[i].contentClass);
              //outputParams[i].content = deserializer.deserialize(element,deserializationContext,outputParams[i].contentClass);
              headerReader.end();
            }
          }
        }
      } else {
        throw new Exception(" Can not find soap:Body !");
      }
      while (reader.getState()!=XMLTokenReader.EOF) {
        reader.next();
      }
    //}
    return true;
  }

  /**
   * Parses SOAP RPC response message.
   */
  private boolean getResponseRpc(PropertyContext context, ClientMimeMessage message,ObjectFactory objFactory) throws Exception {
    String parts = (String) context.getProperty(SOAP_PARTS);
    int[] partLocation = getPartLocation(context,outputParams);
    //SOAPDeserializationContext deserialziationContext = new SOAPDeserializationContext();
    deserializationContext.clearContext();
    deserializationContext.setApplicationClassLoader(appClassloader);
    TypeMappingImpl typeMapping = (TypeMappingImpl) registry.getDefaultTypeMapping();
    deserializationContext.setTypeMapping(typeMapping);
    deserializationContext.setObjectFactory(objFactory);
    ClientSOAPMessage soapMessage = message.getSOAPMessage();
    String namespace = (String) context.getProperty(SOAP_OPERATION_NAMESPACE);
    XMLTokenReader reader = null;
    if (soapMessage.isDOMbuilt()) { // The message is in dom form
      Element body = soapMessage.getSoapBody();
      /*
      // Check for fault
      Element fault = getElementChildNull(SOAP_FAULT,body);
      if (fault != null && SOAP_ENVELOPE.equals(fault.getNamespaceURI())) {
        // SOAP Fault
        processDocumentFault(message.getSOAPMessage(),objFactory);
        return false;
      }
      Element response = getRootElement(body);
      for (int i=0; i<outputParams.length; i++) {
        if (partLocation[i] != 0) {
          Element param = getElementChildNull(outputParams[i].name,response); // Gets message part
          if (param == null) {
            throw new Exception(" Part '"+outputParams[i].name+" not found in response !");
          } else {
            if (outputParams[i].isElement) { // Part is element
              QName elementType = typeMapping.getTypeForElement(outputParams[i].schemaName);
              Element element = getElementChildNull(outputParams[i].schemaName.getLocalPart(),param);
              if (element == null) {
                throw new Exception(" Part '"+outputParams[i].name+" not found in response !");
              } else {
                if (partLocation[i] == 1) {
                  DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,elementType);
                  DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs("");
                  outputParams[i].content = deserializer.deserialize(element,deserializationContext,outputParams[i].contentClass);
                } else {
                  String href = element.getAttribute("href");
                  if (href.length() != 0) {
                    outputParams[i].content = loadAttachment(href,outputParams[i].name,message);
                  } else {
                    throw new Exception(" Can not resolve attachment !");
                  }

                }
              }
            } else { // Part is type
              if (partLocation[i] == 1) {
                DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,outputParams[i].schemaName);
                DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs("");
                outputParams[i].content = deserializer.deserialize(param,deserializationContext,outputParams[i].contentClass);
              } else {
                String href = param.getAttribute("href");
                if (href.length() != 0) {
                  outputParams[i].content = loadAttachment(href,outputParams[i].name,message);
                } else {
                  throw new Exception(" Can not resolve attachment !");
                }

              }
            }
          }
        }
      }*/
      reader = new XMLDOMTokenReader(body);
      reader.begin();
    } else { // message is in reader mode
      reader = soapMessage.getReader(); // Reader is positioned on body element
      }
      reader.passChars();
      if (reader.getState() != XMLTokenReader.STARTELEMENT || !reader.getLocalName().equals("Body")) {
        throw new Exception(" Can not find soap:Body !");
      }
      reader.next(); // Enters into the body
      reader.passChars();
      // Check for fault
      if (reader.getState() == XMLTokenReader.STARTELEMENT && SOAP_FAULT.equals(reader.getLocalName()) && SOAP_ENVELOPE.equals(reader.getURI())) {
        // SOAP Fault
        processRpcFault(message.getSOAPMessage(),objFactory);
        return false;
      }

      if (reader.getState() == XMLTokenReader.STARTELEMENT) {
        // enters in response element his name doesn't matter
        reader.next();
        for (int i=0; i<outputParams.length; i++) { // searches return params
          if (partLocation[i] == 1 || partLocation[i] == 2) {
            reader.passChars();
            if (reader.getState() != XMLTokenReader.STARTELEMENT || (!reader.getLocalName().equals(outputParams[i].name) && !outputParams[i].name.equals(ANY_NAME))) {
              throw new Exception(" Can not find part :"+outputParams[i].name);
            } else {
              if (outputParams[i].isElement) {
                reader.next(); // enters into the part positions into the element
                reader.passChars();
                if (reader.getState() != XMLTokenReader.STARTELEMENT) {
                  throw new Exception(" Unrecognized response !");
                }
                if (reader.getLocalName().equals(outputParams[i].schemaName.getLocalPart())) {
                  if (partLocation[i] == 1) {
                    QName elementType = typeMapping.getTypeForElement(outputParams[i].schemaName);
                    DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,elementType);
                    DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs("");
                    outputParams[i].content = deserializer.deserialize(reader, deserializationContext, outputParams[i].contentClass);
                  } else {
                    Attributes attributes = reader.getAttributes();
                    String href = attributes.getValue("href");
                    if (href != null) {
                      outputParams[i].content = loadAttachment(href,outputParams[i].name, message);
                    } else {
                      throw new Exception(" Attached part must have href attribute !");
                    }
                    reader.passChars();
                    if (reader.getState() != XMLTokenReader.ENDELEMENT) {
                      throw new Exception(" Attached part must be empty !");
                    }
                  }
                } else {
                  throw new Exception(" Unrecognized response !");
                }
                reader.next(); // leaves element
              } else {
                if (partLocation[i] == 1) {
                  DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,outputParams[i].schemaName);
                  DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs("");
                  outputParams[i].content = deserializer.deserialize(reader, deserializationContext, outputParams[i].contentClass);
                } else {
                  Attributes attributes = reader.getAttributes();
                  String href = attributes.getValue("href");
                  if (href != null) {
                    outputParams[i].content = loadAttachment(href,outputParams[i].name, message);
                  } else {
                    throw new Exception(" Attached part must have href attribute !");
                  }
                  reader.next();
                  reader.passChars();
                  if (reader.getState() != XMLTokenReader.ENDELEMENT) {
                    throw new Exception(" Attached part must be empty !");
                  }
                }
              }
              reader.next(); // leaves part end tag
            }
          }
        }
        reader.passChars();
        reader.next(); // leaves response element
        deserializationContext.deserializeRemainingElements(reader);
      }
      while (reader.getState() != XMLTokenReader.EOF) {
        reader.next();
      }
    //}
    for (int i=0; i<outputParams.length; i++) {
      if (outputParams[i].content != null) {
        if (outputParams[i].content instanceof SOAPDeserializationState) {
          SOAPDeserializationState state = (SOAPDeserializationState) outputParams[i].content;
          if (state.isComplete()) {
            outputParams[i].content = state.getInstance();
          } else {
            throw new Exception(" Some references in responce could not be resolved !");
          }
        }
      }
      if (partLocation[i] == 3) { // header deserialzation
        Element element = soapMessage.getSoapHeader(outputParams[i].schemaName.getNamespaceURI(),outputParams[i].schemaName.getLocalPart());
        if (element != null) {
          XMLDOMTokenReader headerReader = new XMLDOMTokenReader(element);
          headerReader.begin();
          QName elementType = typeMapping.getTypeForElement(outputParams[i].schemaName);
          DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,elementType);
          DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs(null);
          //outputParams[i].content = deserializer.deserialize(element,deserializationContext,outputParams[i].contentClass);
          outputParams[i].content = deserializer.deserialize(headerReader,deserializationContext,outputParams[i].contentClass);
          headerReader.end();
        }
      }
    }
    return true;
  }


  /**
   * Calls webservice method. The order for webservice call is the following.
   * 1. startOperation
   * 2. initialize configuration context
   * 3. invoke call method
   * 4. check for fault
   * 5. if fault get it and throw
   * 6. if not get output params and return.
   */
  public void call(PropertyContext context, ProtocolList globalProtocols, ProtocolList localProtocols) throws Exception {
    boolean transportInit = false;
    try {
      // Get's binding configuration
      PropertyContext configContext = context.getSubContext(ClientTransportBinding.BINDING_CONFIG);
      PropertyContext featureContext = context.getSubContext(ClientTransportBinding.FEATUTE_CONFIG);
      ObjectFactory objFactory = (ObjectFactory) context.getProperty(BaseGeneratedStub.OBJECT_FACTORY);
      Object object = featureContext.getProperty(BaseGeneratedStub.SAX_RESPONSE_HANDLER);
      ContentHandler saxHandler = null ;
      if (object != null && object instanceof ContentHandler) {
        saxHandler = (ContentHandler) object;
      }
      boolean redirectMode = false;
      String httpRedirectSpecial = (String) featureContext.getProperty(BaseGeneratedStub.HTTP_REDIRECT_SUPPORT);
      if (httpRedirectSpecial != null && httpRedirectSpecial.length() != 0) {
        redirectMode = true;
      }
      if (configContext.isDefined() == false) {
        throw new Exception(" No binding configuration provided in context !");
      }
      String endpoint = (String) configContext.getProperty(ENDPOINT);
      if (endpoint == null) {
        throw new Exception(" Endpoint not specified in binding context !");
      }
      this.appClassloader = null;
      if (context.getProperty(APP_CLASSLOADER)!= null) {
        this.appClassloader = (ClassLoader) context.getProperty(APP_CLASSLOADER);
      }
      // HTTP Transport initialized. This may be done later to support soap redirecting
      transport.init(endpoint, HTTPTransport.REQUEST_METHOD_POST,context.getSubContext(ClientTransportBinding.FEATUTE_CONFIG));
      transportInit = true;
      context.setProperty(ClientTransportBinding.TRANSPORT_INTERFACE,transport);
      // Sets SOAP Action
      String soapAction = (String) configContext.getProperty(SOAP_ACTION);
      if (soapAction != null) {
        transport.setHeader(SOAP_ACTION_UPPER,"\""+soapAction+"\"");
      } else {
        transport.setHeader(SOAP_ACTION_UPPER,"\"\"");
      }
      // Sets http request headers that are set by the developer
      Hashtable table = (Hashtable) context.getProperty(BaseGeneratedStub.HTTP_REQUEST_HEADERS);
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
      table = (Hashtable)  context.getProperty(BaseGeneratedStub.HTTP_RESPONSE_HEADERS);
      if (table != null) {
        table.clear();
      }
      message.initSerializationMode();
      //ClientSOAPMessage soapMesasage = message.getSOAPMessage();
      String style =  (String) configContext.getProperty(SOAP_STYLE);
      if (style.equals(DOCUMENT_STYLE)) { // Document Style Operation
        buildRequestDocument(configContext.getSubContext("input"),message);
        globalProtocols.handleRequest(message,context);
        localProtocols.handleRequest(message,context);
        String newURI = outputMessage(message,transport,redirectMode);
        if (newURI != null) {
          configContext.setProperty(ENDPOINT,newURI);
        }
        if (saxHandler == null) {
        int responseCode = handleResponseMessage(transport,message);
        if (responseCode == 500) { // Internal Server Error.
            processDocumentFault(message.getSOAPMessage(),objFactory);
          globalProtocols.handleFault(message, context);
          localProtocols.handleFault(message, context);
        } else {
          if (outputParams != null) { // not one way message
            globalProtocols.handleResponse(message, context);
            localProtocols.handleResponse(message, context);
              if (getResponseDocument(configContext.getSubContext("output"),message,objFactory) == false) {
              globalProtocols.handleFault(message, context);
              localProtocols.handleFault(message, context);
            }
            } else { // one way message but read the response however
              InputStream is = transport.getResponseStream();
              while (is.read() != -1) {}
            }
          }
        } else {
          handlerSAXMessage(transport,saxHandler);
          fillOutputParameters();
        }
      } else if (style.equals(RPC_STYLE)) { // RPC Style operation
        buildRequestRpc(configContext.getSubContext("input"),message);
        globalProtocols.handleRequest(message,context);
        localProtocols.handleRequest(message,context);
        String newURI = outputMessage(message,transport,redirectMode);
        if (newURI != null) {
          configContext.setProperty(ENDPOINT,newURI);
        }
        if (saxHandler == null) {
        int responseCode = handleResponseMessage(transport,message);
        if (responseCode == 500) { // Internal Server Error.
            processRpcFault(message.getSOAPMessage(),objFactory);
          globalProtocols.handleFault(message, context);
          localProtocols.handleFault(message, context);
        } else {
          if (outputParams != null) { // not one way message
            globalProtocols.handleResponse(message, context);
            localProtocols.handleResponse(message, context);
              if (getResponseRpc(configContext.getSubContext("output"),message,objFactory) == false) {
              globalProtocols.handleFault(message, context);
              localProtocols.handleFault(message, context);
            }
            } else { // one way message but read the response however
              InputStream is = transport.getResponseStream();
              while (is.read() != -1) {}
            }
          }
        } else {
          handlerSAXMessage(transport,saxHandler);
          fillOutputParameters();
        }
      } else {
        throw new Exception(" Binding configuration Exception !");
      }
      // Response is recieved - get the response headers
      table = (Hashtable) context.getProperty(BaseGeneratedStub.HTTP_RESPONSE_HEADERS);
      if (table != null) {
        Hashtable responseHeaders = transport.getHeaders();
        table.putAll(responseHeaders);
      }
    } catch (XmlMarshalException x) {
      if (transportInit) {
        transport.closeSession();
      }
      throw x;
    } catch (XmlUnmarshalException y) {
      if (transportInit) {
        transport.closeSession();
      }
      throw y;
    } finally {
      if (transportInit) {
        boolean keepAliveFlag = false;
        if (transport.featureSet(HTTPKeepAliveFeature.KEEP_ALIVE_FEATURE)) { // Keep-Alive feature
          PropertyContext feature = transport.getFeature(HTTPKeepAliveFeature.KEEP_ALIVE_FEATURE);
          String keepAlive = (String) feature.getProperty(HTTPKeepAliveFeature.KEEP_ALIVE_PROPERTY);
          if (keepAlive != null && keepAlive.length() != 0) {
            keepAliveFlag = true;
          }
        }
        if (keepAliveFlag == false) {
          transport.closeSession();
        }
      }
    }
    //transport.closeSession(); // closes connection
  }

  /**
   * When SAX Content Hander is used then all primitive output parameters must be
   * filled with default values.
   */
  private void fillOutputParameters() {
    if (outputParams != null) {
      for (int i=0; i<outputParams.length; i++) {
        if (outputParams[i].contentClass == int.class) {
          outputParams[i].content = new Integer(0);
        }
        if (outputParams[i].contentClass == byte.class) {
          outputParams[i].content = new Byte((byte) 0);
        }
        if (outputParams[i].contentClass == short.class) {
          outputParams[i].content = new Short((short) 0);
        }
        if (outputParams[i].contentClass == long.class) {
          outputParams[i].content = new Long(0);
        }
        if (outputParams[i].contentClass == double.class) {
          outputParams[i].content = new Double(0);
        }
        if (outputParams[i].contentClass == float.class) {
          outputParams[i].content = new Float(0f);
        }
        if (outputParams[i].contentClass == boolean.class) {
          outputParams[i].content = new Boolean(false);
        }
      }
    }
  }

  /**
   * Handles Response SOAP Message using SAX Content Handler.
   * @param transport
   * @param handler
   * @return
   * @throws Exception
   */
  private int handlerSAXMessage(HTTPTransport transport, ContentHandler handler) throws Exception {
    if (saxReader == null) {
      SAXParser saxParser = spf.newSAXParser();
      saxReader = saxParser.getXMLReader();
    }
    int responseCode = transport.getResponseCode();
    String contentType = transport.getContentType();
    if (responseCode == 200) { // message is processed
      if (contentType.indexOf("text/xml") != -1) { // soap message fault
        if (outputParams != null) { // not one way message
          saxReader.setContentHandler(handler);
          saxReader.parse(new InputSource(transport.getResponseStream()));
        }
        return responseCode;
      } else {
        InputStream response = transport.getResponseStream();
        byte[] buffer = new byte[1024];
        int readBytes = response.read(buffer);
        if (readBytes == -1) {
          readBytes = 0;
        }
        String str = new String(buffer,0,readBytes); //$JL-I18N$
        throw new Exception("Call to {"+transport.getEndpoint()+"} returned http code 500 with unacceptable content type ("+contentType+").\n"+str);
      }
    }
    if (responseCode == 202) { // message is accepted
      return responseCode;
    }
    if (responseCode == 500) { // internal server error
      if (contentType.indexOf("text/xml") != -1) { // soap message fault
        saxReader.setContentHandler(handler);
        saxReader.parse(new InputSource(transport.getResponseStream()));
        return responseCode;
      } else {
        InputStream response = transport.getResponseStream();
        byte[] buffer = new byte[1024];
        int readBytes = response.read(buffer);
        if (readBytes == -1) {
          readBytes = 0;
        }
        String str = new String(buffer,0,readBytes); //$JL-I18N$
        throw new Exception("Call to {"+transport.getEndpoint()+"} returned http code 500 with unacceptable content type ("+contentType+").\n"+str);
      }
    }
    String responseMessage = transport.getResponseMessage();
    Hashtable responseHeaders = transport.getHeaders();
    throw new InvalidResponseCodeException(responseCode, responseMessage , responseHeaders, transport.getEndpoint());
  }

  /**
   * Called for generation of any custom binding classes for every operation.
   * Output dir is output directory for this package.
   * Advanced feature that give the binding to generate some binding dependant things and modify context.
   * For every operation. Before that getOperationBindingConfig is called so WSDLBindingOperation is not called.
   */
  public void generateCustom(String packageName, File outputDir, WSDLDefinitions definitions, PropertyContext context, WSDLOperation operation) {
  }

  /**
   * Returns string for binding identification.
   */
  public String getName() {
    return NAME;
  }

  /**
   * Returns configuration context to Stub to generate binding configuration.
   */
  public PropertyContext getConfigurationContext() {
    return null;
  }


  /**
   * This method must parse binding extension element and sets properties as it needs to be in context.
   * This element is binding top level extension element.
   */
  public void getMainBindingConfig(WSDLBinding binding, PropertyContext context) throws WSDLException {
    ArrayList extensions = binding.getExtensions();
    WSDLExtension extensionElement = null;
    for (int i=0; i<extensions.size(); i++) {
      extensionElement = (WSDLExtension) extensions.get(i);
      if (extensionElement.getLocalName().equals("binding") && SOAP_BINDING_NAMESPACE.equals(extensionElement.getURI())) {
        break;
      }
      extensionElement = null;
    }
    if (extensionElement != null) {
      String style = extensionElement.getAttribute(SOAP_STYLE);
      if (style == null) {
        style = DOCUMENT_STYLE; // Default value
      }
      // sets style property
      context.setProperty(SOAP_STYLE,style);
      // sets ransport property
      String transport = extensionElement.getAttribute(SOAP_TRANSPORT);
      context.setProperty(SOAP_TRANSPORT,transport);
    }
  }

  /**
   * Loads soap:header attributes. Reads from header extension element.
   * @param extension
   * @param context
   */
  private void setupSoapHeader(WSDLExtension extension, PropertyContext context, WSDLMessage rootMessage) throws WSDLException  {
    String message = extension.getAttribute("message");
    String part = extension.getAttribute("part");
    String use = extension.getAttribute("use");
    if (use == null) {
      use = "literal";
    }
    PropertyContext headerContext = context.getSubContext("headers");
    if (message != null) {
      message = DOM.qnameToLocalName(message);
    }
    if (rootMessage.getName().equals(message)) {
      message = null;
    }
    if (message == null && part != null && rootMessage != null) { // Only root messages are auto resolved
      WSDLPart wsdlpart = rootMessage.getPart(part);
      if (wsdlpart == null) {
        throw new WSDLException(" Part ["+part+"] does not exist in message ["+rootMessage.getName()+"] but referenced from soap:header !");
      }
      headerContext.setProperty(part,use);
    }
  }

  /**
   * Loads soap:body attributes. Reads them from body extension element.
   * @param extension
   * @param context
   */
  private void setupSoapBody(WSDLExtension extension, PropertyContext context)  {
    context.setProperty(SOAP_PARTS, extension.getAttribute(SOAP_PARTS));
    context.setProperty(SOAP_USE, extension.getAttribute(SOAP_USE));
    context.setProperty(SOAP_ENCODING_STYLE, extension.getAttribute(SOAP_ENCODING_STYLE));
    context.setProperty(SOAP_OPERATION_NAMESPACE, extension.getAttribute(SOAP_OPERATION_NAMESPACE));
  }

  /**
   * Loads mime:multipartRelated attributes.
   */
  private void setupMimeRelated(WSDLExtension extension, PropertyContext context,WSDLChannel channel, WSDLDefinitions definitions) throws WSDLException {
    PropertyContext mimeInputContext = context.getSubContext("mime");
    ArrayList mimeParts = extension.getChildren();
    for (int j=0; j<mimeParts.size(); j++) {
      WSDLExtension e = (WSDLExtension) mimeParts.get(j);
      if (e.getLocalName().equals("part") && MIME_BINDING_NAMESPACE.equals(e.getURI())) {
        ArrayList mimeContents = e.getChildren();
        if (mimeContents.size() == 0) {
          throw new WSDLException(" No content found in mime:part !");
        }
        for (int k=0; k<mimeContents.size(); k++) {
          WSDLExtension mimeContent = (WSDLExtension) mimeContents.get(k);
          if (mimeContent.getLocalName().equals("content") && MIME_BINDING_NAMESPACE.equals(mimeContent.getURI())) {
            String part = mimeContent.getAttribute("part");
            String type = mimeContent.getAttribute("type");
            if ( part == null || type == null) {
              throw new WSDLException("'part' and 'type' attributes are nessesary in mime:content !");
            }
            mimeInputContext.setProperty(part,type.trim().toLowerCase(Locale.ENGLISH));
            break;
          } else if (mimeContent.getLocalName().equals("body") && SOAP_BINDING_NAMESPACE.equals(mimeContent.getURI())) {
            setupSoapBody(mimeContent,context);
          } else if (mimeContent.getLocalName().equals("header") && SOAP_BINDING_NAMESPACE.equals(mimeContent.getURI())) {
              WSDLMessage message = null;
              if (channel != null) {
                message = definitions.getMessage(channel.getMessage().getName(),channel.getMessage().getURI());
              }
              setupSoapHeader(mimeContent, context, message);
          }
        }
      } else {
        throw new WSDLException(" Mime Binding allows onlw mime:part element's in mime:multipartRelated binding element !");
      }
    }

  }

  /**
   * Returns true is string given is valud style
   * @param style
   * @return
   */
  private boolean isValidStyle(String style) {
    if (style == null) {
      return false;
    }
    if (style.equals(DOCUMENT_STYLE) || style.equals(RPC_STYLE)) {
      return true;
    }
    return false;
  }

  private String getConforms(WSDLNamedNode node) {
    WSDLDocumentation documenation = node.getDocumentation();
    if (documenation != null && documenation.getElementContent()!= null) {
      Element element = documenation.getElementContent();
      if (WSI_NAMESPACE.equals(element.getNamespaceURI()) && WSI_CLAIM.equals(element.getLocalName())) {
        String perm = element.getAttribute("conformsTo");
        if (perm.length()!= 0) {
          return perm;
        }
      }
    }
    return null;
  }

  private void setupWsiClaims(WSDLDefinitions definitions,PropertyContext oContext, WSDLOperation operation) {
    String conformsTo = null;
    WSDLPortType portType = (WSDLPortType) operation.getParentNode();
    WSDLBinding binding = null;
    ArrayList barr = definitions.getBindings();
    for (int i=0; i<barr.size(); i++) {
      WSDLBinding perm = (WSDLBinding) barr.get(i);
      if (perm.getType().equals(portType.getQName())) {
        binding = perm;
        break;
      }
    }
    WSDLPort port = null;
    if (definitions.getServices().size()!=0 && binding!=null) {
      WSDLService service = (WSDLService) definitions.getServices().get(0);
      ArrayList ports = service.getPorts();
      for (int i=0; i<ports.size(); i++) {
        WSDLPort perm = (WSDLPort) ports.get(i);
        if (perm.getBinding().equals(binding.getQName())) {
          port = perm;
          break;
        }
      }
    }
    if (port != null) {
      conformsTo = getConforms(port);
    }
    if (binding != null && conformsTo==null) {
      conformsTo = getConforms(binding);
    }
    if (portType != null && conformsTo == null) {
      conformsTo = getConforms(portType);
    }
    if (conformsTo == null) {
      conformsTo = getConforms(operation);
    }
    if (operation.getInput() != null && conformsTo == null) {
      WSDLChannel input = operation.getInput();
      WSDLMessage message = definitions.getMessage(input.getMessage());
      conformsTo = getConforms(message);
    }
    if (conformsTo != null) {
      oContext.setProperty("conformsTo",conformsTo);
    }
  }

  /**
   * This method reads operation binding extensions.
   * The corresponding  binded WSDL Operation is passed as a param and reference to WSDL definitions.
   * Set the properties your binding need in context as information for this operation to be invoked.
   */
  public void getOperationBindingConfig(WSDLBindingOperation obinding, WSDLOperation operation, PropertyContext context, WSDLDefinitions definitions) throws WSDLException {
    ArrayList operationBiniding = obinding.getExtensions();
    if (operationBiniding.size() > 0) {
      WSDLExtension extension = (WSDLExtension) operationBiniding.get(0);
      if (extension.getLocalName().equals(SOAP_OPERATION) && SOAP_BINDING_NAMESPACE.equals(extension.getURI())) {
        // SOAP:operation extenrion
        String soapAction = extension.getAttribute(SOAP_ACTION);
        context.setProperty(SOAP_ACTION,soapAction);
        String style = extension.getAttribute(SOAP_STYLE);
        if (style != null) {
          if (!isValidStyle(style)) {
            throw new WSDLException("Invalis style attribute is set: must be (document/rpc) but ("+style+") is specified !");
          }
          context.setProperty(SOAP_STYLE,style);
        }
      }
    }
    WSDLBindingChannel inputBinding = obinding.getInput();
    PropertyContext inputContext = context.getSubContext("input");
    inputContext.setProperty(OPERATION_NAME,operation.getName());
    //setupWsiClaims(inputContext,definitions.getMessage(operation.getInput().getMessage()));
    if (inputBinding != null) {
      ArrayList extensions = inputBinding.getExtensions();
      for (int i=0; i<extensions.size(); i++) {
        WSDLExtension extension = (WSDLExtension) extensions.get(i);
        if (extension.getLocalName().equals("body") && SOAP_BINDING_NAMESPACE.equals(extension.getURI())) { // SOAP:body
          setupSoapBody(extension, inputContext);
        }
        if (extension.getLocalName().equals("header") && SOAP_BINDING_NAMESPACE.equals(extension.getURI())) { // SOAP:header
          WSDLMessage message = null;
          if (operation.getInput() != null) {
            message = definitions.getMessage(operation.getInput().getMessage().getName(),operation.getInput().getMessage().getURI());
          }
          setupSoapHeader(extension, inputContext, message);
        };
        if (extension.getLocalName().equals(MIME_RELATED) && MIME_BINDING_NAMESPACE.equals(extension.getURI())) { // mime:multipartRelated
          // Gets mime parts. Alternative content not supported only one mime:content allowed
          setupMimeRelated(extension, inputContext,operation.getInput(),definitions);
        }
      }
    }
    WSDLBindingChannel outputBinding = obinding.getOutput();
    if (outputBinding != null) { // detect one way operation
      PropertyContext outputContext = context.getSubContext("output");
      outputContext.setProperty(OPERATION_NAME,operation.getName());
      ArrayList extensions = outputBinding.getExtensions();
      for (int i=0; i<extensions.size(); i++) {
        WSDLExtension extension = (WSDLExtension) extensions.get(i);
        if (extension.getLocalName().equals("body") && SOAP_BINDING_NAMESPACE.equals(extension.getURI())) { // SOAP:Body
          setupSoapBody(extension, outputContext);
        }
        if (extension.getLocalName().equals("header") && SOAP_BINDING_NAMESPACE.equals(extension.getURI())) { // SOAP:header
          WSDLMessage message = null;
          if (operation.getOutput() != null) {
            message = definitions.getMessage(operation.getOutput().getMessage().getName(),operation.getOutput().getMessage().getURI());
          }
          setupSoapHeader(extension, outputContext, message);
        };
        if (extension.getLocalName().equals(MIME_RELATED) && MIME_BINDING_NAMESPACE.equals(extension.getURI())) { // mime:multipartRelated
          // Gets mime parts. Alternative content not supported only one mime:content allowed
          setupMimeRelated(extension, outputContext,operation.getOutput(),definitions);
        }
      }
    }
  }

  /**
   * Get's response parts.
   */
  public ServiceParam[] getResponseParts() {
    return new ServiceParam[0];
  }

  public ServiceParam[] getFaultParts() {
    return new ServiceParam[0];
  }

  public TypeMappingRegistry getTypeMappingRegistry() {
    return this.registry;
  }

  public boolean isCompatible(AbstractProtocol prorocol) {
    if (prorocol instanceof SoapProtocol) {
      return true;
    }
    if (prorocol instanceof MimeProtocol) {
      return true;
    }
    return false;
  }

  /**
   * Loads binding address from service port extension element.
   */
  public String loadAddress(WSDLExtension extension) throws WSDLException {
    if (extension.getURI().equals(SOAP_BINDING_NAMESPACE) && extension.getLocalName().equals("address")) {
      String location = extension.getAttribute("location");
      if (location == null) {
        throw new WSDLException(" Location not specified ");
      }
      return location;
    }
    throw new WSDLException(" The port SOAP Extension is invalid ");
  }

  /**
   * Returns true if binding is recognized to be SOAP with attachments.
   */
  public boolean recognizeBinding(WSDLBinding bindingElement) {
    ArrayList extensions = bindingElement.getExtensions();
    if (extensions.size() == 0) { // Top level
      return false;
    } else {
      WSDLExtension extension = null;
      for (int i=0; i<extensions.size(); i++) {
        extension = (WSDLExtension) extensions.get(i);
        if (extension.getLocalName().equals("binding") && SOAP_BINDING_NAMESPACE.equals(extension.getURI())) {
          break;
        }
        extension = null;
      }
      if (extension == null) {
        // No WSDL binding found
        return false;
      }
      // top level extension element is soap:binding
      ArrayList operations = bindingElement.getOperations();
      for (int i = 0; i<operations.size(); i++) {
        WSDLBindingOperation operation = (WSDLBindingOperation) operations.get(i);
        WSDLBindingChannel channel = operation.getInput();
        if (channel != null) {
          extensions = channel.getExtensions();
          for (int j=0; j<extensions.size(); j++) {
            extension = (WSDLExtension) extensions.get(j);
            if (extension.getLocalName().equals(MIME_RELATED) && MIME_BINDING_NAMESPACE.equals(extension.getURI())) {
              return true; // mime part found
            }
          }
        }
        channel = operation.getOutput();
        if (channel != null) {
          extensions = channel.getExtensions();
          for (int j=0; j<extensions.size(); j++) {
            extension = (WSDLExtension) extensions.get(j);
            if (extension.getLocalName().equals(MIME_RELATED) && MIME_BINDING_NAMESPACE.equals(extension.getURI())) {
              return true; // mime part found
            }
          }
        }
      }
      return true;
    }
  }

  public void setTypeMappingRegistry(TypeMappingRegistry registry) {
    this.registry = registry;
  }

  /**
   * That method is for internal use of the proxy
   */
  protected Element getElementChildNull(String name, Element parent) throws Exception {
    if (parent == null) {
      return null;
      //throw new Exception("Incorrect params passed to getElementChildNull !");
    }
    org.w3c.dom.Node child = parent.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && child.getLocalName().equals(name)) {
        return (Element) child;
      }

      child = child.getNextSibling();
    }
    return null;
  }

  /**
   * That method is for internal use of the proxy
   */
  protected Element getRootElement(Element parent) throws Exception {
    if (parent == null) {
      throw new Exception("Incorrect params passed to getElementChildNull !");
    }
    org.w3c.dom.Node child = parent.getFirstChild();
    while (child != null) {
      if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
        return (Element) child;
      }
      child = child.getNextSibling();
    }
    return null;
  }

  /**
   * Binding starts transmiting of soap operation. This method passes input/output/fault service params.
   */
  public void startOperation(ServiceParam[] inputParams, ServiceParam[] outputParams, ServiceParam[] faultParams) {
    this.inputParams = inputParams;
    this.outputParams = outputParams;
    this.faultParams = faultParams;
  }

  private static String getEncoding(String contentTypeValue) {
    int sti = contentTypeValue.indexOf("charset=");
    if (sti > -1) {
      String newValue = contentTypeValue.substring(sti + 8);
      int end = newValue.indexOf(";");
      if (end == -1) return newValue;
      return newValue.substring(0, end);
    }
    return "utf-8";
  }

  private static String createStringFromStream(InputStream inputStream, String contentTypeValue) throws Exception {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream(128);
      byte[] arr = new byte[128];
      int b;

      while ((b = inputStream.read(arr)) != -1) {
        buffer.write(arr, 0, b);
      }
      inputStream.close();

      String enc = getEncoding(contentTypeValue);
      return buffer.toString(enc);
    } catch (IOException e) {
      throw new Exception("Unable to deserialize attached string !");
    }
  }

  private static byte[] createByteArrayFromStream(InputStream inputStream) throws Exception {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream(128);
      byte[] arr = new byte[128];
      int b;

      while ((b = inputStream.read(arr)) != -1) {
        buffer.write(arr, 0, b);
      }
      inputStream.close();
//      byte[] res;
//      byte[] source = buffer.toByteArray();
//      if (transferEncoding != null && transferEncoding.equalsIgnoreCase("base64")) {
//        try {
//          res = com.sap.engine.lib.xml.util.BASE64Decoder.decode(source);
//        } catch (Exception e) {
//          throw new Exception("Error in processing base64 sequence. It is incorrect!");
//        }
//      } else {
//        res = source;
//      }
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new Exception(" Unable to deserialize attached byte array !");
    }
  }

  /**
   * Return true if this Binding implements this feature.
   */
  public boolean isFeatureImplemented(String featureName, PropertyContext context) {
    if (AuthenticationFeature.AUTHENTICATION_FEATURE.equals(featureName)) {
      return true;
    }
    if (ProxyFeature.PROXY_FEATURE.equals(featureName)) {
      return true;
    }
    return false;
  }

  /**
   * Returns features that this protocol implements and supports.
   */
  public String[] getFeatures() {
    return new String[0];
  }

  public void flush(PropertyContext context, ProtocolList globalProtocols) throws Exception {
    boolean transportInit = false;
    try {
    PropertyContext configContext = context.getSubContext(ClientTransportBinding.BINDING_CONFIG);
      ObjectFactory objFactory = (ObjectFactory) context.getProperty(BaseGeneratedStub.OBJECT_FACTORY);
    if (configContext.isDefined() == false) {
      throw new Exception(" No binding configuration provided in context !");
    }
    String endpoint = (String) configContext.getProperty(ENDPOINT);
    if (endpoint == null) {
      throw new Exception(" Endpoint not specified in binding context !");
    }
    // HTTP Transport initialized.
    transport.init(endpoint, HTTPTransport.REQUEST_METHOD_POST,context.getSubContext(ClientTransportBinding.FEATUTE_CONFIG));
      transportInit = true;
    // Sets SOAP Action
    String soapAction = (String) configContext.getProperty(SOAP_ACTION);
    if (soapAction != null) {
      transport.setHeader(SOAP_ACTION_UPPER,"\""+soapAction+"\"");
    }
    message.initSerializationMode();
    ClientSOAPMessage soapMessage = message.getSOAPMessage();
//    XMLTokenWriter writer = soapMessage.getWriter();
    soapMessage.addEmptyBody();
    // Outputs soap:Body
//    writer.enter(SOAP_ENVELOPE, SOAP_BODY);
//    writer.enter(null,"flush");
//    writer.leave();
//    writer.leave();
    globalProtocols.handleRequest(soapMessage,context);
      outputMessage(message,transport,false);
    int responseCode = handleResponseMessage(transport,message);
    if (responseCode == 500) { // Internal Server Error.
        processDocumentFault(message.getSOAPMessage(),objFactory);
      globalProtocols.handleFault(message, context);
    } else {
      globalProtocols.handleResponse(message, context);
    }
    } catch (XmlMarshalException x) {
      if (transportInit) {
        transport.closeSession();
      }
      throw x;
    } catch (XmlUnmarshalException y) {
      if (transportInit) {
        transport.closeSession();
      }
      throw y;
    } finally {
      if (transportInit) {
        boolean keepAliveFlag = false;
        if (transport.featureSet(HTTPKeepAliveFeature.KEEP_ALIVE_FEATURE)) { // Keep-Alive feature
          PropertyContext feature = transport.getFeature(HTTPKeepAliveFeature.KEEP_ALIVE_FEATURE);
          String keepAlive = (String) feature.getProperty(HTTPKeepAliveFeature.KEEP_ALIVE_PROPERTY);
          if (keepAlive != null && keepAlive.length() != 0) {
            keepAliveFlag = true;
          }
        }
        if (keepAliveFlag == false) {
          transport.closeSession();
        }
      }
    }
  }

  /**
   * Imports SOAP Headers Features that allows to operate with SOAP headers.
   * @param globalFeatures
   * @param binding
   * @return
   */
  public GlobalFeatures importGlobalFeatures(GlobalFeatures globalFeatures, WSDLBinding binding) {
    FeatureType headersFeature  = new FeatureType();
    headersFeature.setName(HeadersFeature.NAME);
    String provider =  DefaultProviders.getProvider(headersFeature.getName());
    if (provider != null) {
      headersFeature.setProvider(provider);
    }
    //globalFeatures.addFeature(headersFeature);
    globalFeatures.insertFeature(headersFeature);
    if (globalFeatures.getFeature(AuthenticationFeature.AUTHENTICATION_FEATURE) == null) {
      FeatureType securityFeature = new FeatureType();
      securityFeature.setName(AuthenticationFeature.AUTHENTICATION_FEATURE);
      provider = DefaultProviders.getProvider(securityFeature.getName());
      if (provider != null) {
        securityFeature.setProvider(provider);
      }
      securityFeature.setOriginal(false);
      PropertyType property = new PropertyType();
      property.setName(AuthenticationFeature.AUTHENTICATION_MECHANISM);
      property.setValue(AuthenticationFeature.NONE);
      securityFeature.addProperty(property);
      globalFeatures.addFeature(securityFeature);
    }
    if (globalFeatures.getFeature(SessionFeature.SESSION_FEATURE) == null) {
      FeatureType sessionFeature = new FeatureType();
      sessionFeature.setName(SessionFeature.SESSION_FEATURE);
      provider = DefaultProviders.getProvider(SessionFeature.SESSION_FEATURE);
      if (provider != null) {
        sessionFeature.setProvider(provider);
      }
      sessionFeature.setOriginal(false);
      PropertyType property = new PropertyType();
      property.setName(SessionFeature.SESSION_METHOD_PROPERTY);
      property.setValue(SessionFeature.HTTP_SESSION_METHOD);
      sessionFeature.addProperty(property);
      globalFeatures.addFeature(sessionFeature);
    }
    return globalFeatures;
  }

  /**
   * Implementing this method the binding can add Operation Specific Features.
   * @param features
   * @param operation
   * @return
   */
  public LocalFeatures importLocalFeatures(LocalFeatures features, WSDLBindingOperation operation) {
    return features;
  }


  private String getPrefixUri(String pref, Attributes localAttrs, XMLTokenReader reader) {
    //search in the local element declarations
    String value = localAttrs.getValue("xmlns:" + pref);
    if (value != null) {
      return value;
    }
    //use upper level declarations
    return reader.getPrefixMapping(pref);
  }
}
