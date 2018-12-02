/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.io.IOException;
import java.rmi.MarshalException;
import java.rmi.UnmarshalException;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLDOMTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLDOMTokenWriter;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.client.api.SOAPHeaderInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.*;
import com.sap.engine.services.webservices.jaxrpc.exceptions.XmlMarshalException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.XmlUnmarshalException;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Implementation of the SOAPHeaderInterface. An api for handling of SOAPHeaders in the soap request.
 * @version 1.0 (2006-1-17)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class SOAPHeaderInterfaceNYImpl implements SOAPHeaderInterface {
  
  private ClientConfigurationContext clientContext;
  SOAPDeserializationContext deserializationContext;
  SOAPSerializationContext serializationContext;
  private Document document;
  
  /**
   * Default constructor. Pass the web services client configuration context.
   * @param clientContext
   */
  public SOAPHeaderInterfaceNYImpl(ClientConfigurationContext clientContext) {
    this.clientContext = clientContext;
    deserializationContext = new SOAPDeserializationContext();
    serializationContext = new SOAPSerializationContext();
    document = SharedDocumentBuilders.newDocument();    
  }

  /**
   * Returns response SOAP header as DOM Element by it's QName.
   * @param headerName
   * @return Returns null if the header is not found.
   */
  public org.w3c.dom.Element getInputHeader(QName headerName) {
    if (headerName == null) {
      return null;
    }
    ArrayList incomingHeaders = (ArrayList) clientContext.getDynamicContext().getProperty(SOAPHeadersProtocolNY.DYNAMIC_INCOMING_HEADERS);    
    if (incomingHeaders != null) {
      QName elementName = null;
      Element element = null;
      for (int i = 0; i < incomingHeaders.size(); i++) {
        element = (Element) incomingHeaders.get(i);
        elementName = new QName(element.getNamespaceURI(),element.getLocalName());
        if (headerName.equals(elementName)) {
          return element;
        }
      }
    }
    return null;    
  }  

  /**
   * Returns response SOAP header by it's QName and deserializes it as an instance of the headerType
   * provided. For this deserialization to work the header element should be declared in the
   * Web service client WSDL Schema using "xsd:element" declaration.
   * @param headerName
   * @param headerType
   * @return
   */
  public Object getInputHeader(QName headerName, Class headerType) throws UnmarshalException {
    Element content = getInputHeader(headerName);
    if (content == null) {
      return null;
    }
    ExtendedTypeMapping typeMapping = (ExtendedTypeMapping) clientContext.getTypeMaping();
    if (typeMapping == null) {
      return null;
    }
    QName elementType = typeMapping.getTypeForElement(headerName);
    if (elementType == null) {
      throw new XmlUnmarshalException(XmlUnmarshalException.UNKNOWN_SCHEMA_ELEMENT,headerName.getNamespaceURI(),headerName.getLocalPart());
    }
    DeserializerFactory factory = typeMapping.getDeserializer(headerType,elementType);
    if (factory == null) {
      throw new XmlUnmarshalException(XmlUnmarshalException.MISSING_DESERIALIZER,elementType.getNamespaceURI(),elementType.getLocalPart(),headerType.getName());
    }   
    DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs(null);
    deserializationContext.clearContext();
    deserializationContext.setTypeMapping(typeMapping);
    XMLDOMTokenReader reader = new XMLDOMTokenReader(content);
    try {
      reader.begin();
    } catch (ParserException e) {
      throw new XmlUnmarshalException(XmlUnmarshalException.PARSER_ERROR,e);
    }
    return deserializer.deserialize(reader,deserializationContext,headerType);    
  }
  
  /**
   * Sets outgoing header.
   * @param element
   */
  private void setOutputHeader(Element element) {
    ArrayList outgoingHeaders = (ArrayList) clientContext.getDynamicContext().getProperty(SOAPHeadersProtocolNY.DYNAMIC_OUTGOING_HEADERS);
    if (outgoingHeaders == null) {
      outgoingHeaders = new ArrayList();
      clientContext.getDynamicContext().setProperty(SOAPHeadersProtocolNY.DYNAMIC_OUTGOING_HEADERS,outgoingHeaders);
    }
    QName headerName = new QName(element.getNamespaceURI(),element.getNodeName());
    Element headerElement = null;
    QName qName = null;
    
    for (int i=0; i < outgoingHeaders.size(); i++) {
      headerElement = (Element) outgoingHeaders.get(i);
      qName = new QName(headerElement.getNamespaceURI(),headerElement.getLocalName());
      if (headerName.equals(qName)) {
        // Replace existing header
        outgoingHeaders.set(i,element);
        return;
      }
    }
    outgoingHeaders.add(element);    
  }

  /**
   * Sets request header for the next SOAP request.
   * You can pass org.w3c.dom.Element and Null header name or typed object and
   * xsd:element name from the WSDL Schema.
   * @param headerName
   * @param headerContent
   */
  public void setOutputHeader(QName headerName, Object headerContent) throws MarshalException {
    if (headerName == null) {
      if (headerContent instanceof Element) {
        Element element = (Element) headerContent;
        if (element.getNamespaceURI() == null || element.getNamespaceURI().length()==0) {
          throw new XmlMarshalException(XmlMarshalException.UNQUALIFIED_HEADER,"",element.getLocalName());
        } 
        setOutputHeader(element);
      } else {
        throw new XmlMarshalException(XmlMarshalException.INPROPER_HEADER);
      }
    } else {
      if (headerName.getNamespaceURI() == null || headerName.getNamespaceURI().length() == 0) {
        throw new XmlMarshalException(XmlMarshalException.UNQUALIFIED_HEADER,"",headerName.getLocalPart());        
      }
      ExtendedTypeMapping typeMapping = (ExtendedTypeMapping) clientContext.getTypeMaping();
      if (typeMapping == null) {
        return;
      }
      QName elementType = typeMapping.getTypeForElement(headerName);
      if (elementType == null) {
        throw new XmlMarshalException(XmlMarshalException.UNKNOWN_ELEMENT,headerName.getNamespaceURI(),headerName.getLocalPart());
      }      
      Element outElement = document.createElementNS(headerName.getNamespaceURI(),headerName.getLocalPart());
      XMLDOMTokenWriter writer = new XMLDOMTokenWriter(outElement);
      if (headerContent == null) {
        try {
          writer.setPrefixForNamespace("xsi",NS.XSI);
          writer.writeAttribute(NS.XSI,"nil","true");
        } catch (IllegalStateException e) {
          throw new XmlMarshalException(XmlMarshalException.OUTPUT_ERROR,e);
        } catch (IOException e) {
          throw new XmlMarshalException(XmlMarshalException.OUTPUT_ERROR,e);          
        }                        
      } else {
        SerializerFactory sf = typeMapping.getSerializer(headerContent.getClass(),elementType);
        if (sf == null) {
          throw new XmlMarshalException(XmlMarshalException.MISSING_SERIALIZER,elementType.getNamespaceURI(),elementType.getLocalPart(),headerContent.getClass());
        }
        SerializerBase serializer = (SerializerBase) sf.getSerializerAs(null);
        this.serializationContext.clearContext();
        serializationContext.setTypeMapping(typeMapping);
        try {
          serializer.serialize(headerContent,writer,this.serializationContext);
        } catch (IOException e) {
          throw new XmlMarshalException(XmlMarshalException.OUTPUT_ERROR,e);          
        }
      }
      setOutputHeader(outElement);
    }    
  }

}
