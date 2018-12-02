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
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.builtin;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.*;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.*;
import com.sap.engine.services.webservices.jaxrpc.encoding.*;
import com.sap.engine.services.webservices.espbase.client.api.SOAPHeaderInterface;
import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;
import com.sap.engine.interfaces.webservices.runtime.component.ClientProtocolFactory;

import javax.xml.parsers.*;
import javax.xml.rpc.encoding.*;
import javax.xml.namespace.QName;
import java.util.*;
import java.rmi.MarshalException;
import java.rmi.UnmarshalException;

import org.w3c.dom.*;

/**
 * Protocol that allows the user to send custom headers and recieve response headers.
 * You can use automatic serialization/deserialization on all schema element declarations.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SoapHeadersProtocol implements AbstractProtocol,ClientProtocolFactory,SOAPHeaderInterface {

  public static final String FEATURE_NAME = "http://www.sap.com/webas/630/soap/features/headers/";
  public static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";
  public static final String NAME = "SoapHeadersProtocol";
  private Hashtable outgoingHeaders; // Hash of outgoing headers
  private Hashtable incomingHeaders; // Hash of incoming headers
  private Document document;
  private TypeMappingImpl typeMapping;

  public SoapHeadersProtocol() throws Exception {
    outgoingHeaders = new Hashtable();
    incomingHeaders = new Hashtable();
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
//    DocumentBuilderFactory builderFactory = new DocumentBuilderFactoryImpl();
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    document = builder.newDocument();
  }

  public void init(PropertyContext context) throws ClientProtocolException {
    outgoingHeaders.clear();
    incomingHeaders.clear();
    typeMapping = (TypeMappingImpl) context.getProperty(ClientTransportBinding.TYPE_MAPPING);
    if (typeMapping == null) {
      //System.out.println("BUG !!!");
      //throw new ClientProtocolException("Unable to initialize "+NAME+" TypeMapping instance not found in initialization context !");
    }
  }

  public boolean handleRequest(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    // Dump transport information for http
//    Object transportInterface = context.getProperty(ClientTransportBinding.TRANSPORT_INTERFACE);
//    if (transportInterface instanceof HTTPTransportInterface) {
//      System.out.println("HTTP Transport is used !");
//      HTTPTransportInterface http = (HTTPTransportInterface) transportInterface;
//      System.out.println("Endpoint is :"+http.getEndpoint());
//      Enumeration keys = http.listHeaders();
//      System.out.println("Keys :"+keys.toString());
//      http.setHeader("myheader","alabala");
//    }

    ClientSOAPMessage soapmessage = null;
    if (message instanceof ClientSOAPMessage) {
      soapmessage = (ClientSOAPMessage) message;
    }
    if (message instanceof ClientMimeMessage) {
      soapmessage = ((ClientMimeMessage) message).getSOAPMessage();
    }
    if (soapmessage != null) {
      Enumeration e = outgoingHeaders.keys();
      ArrayList headers = soapmessage.getHeaders();
      while (e.hasMoreElements()) {
        QName key = (QName) e.nextElement();
        Object value = outgoingHeaders.get(key);
        if (value instanceof Element) {
          headers.add(value);
        } else {
          // the header is not set.
        }
      }
      outgoingHeaders.clear();
    }
    return true;
  }

  public boolean handleResponse(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    ClientSOAPMessage soapmessage = null;
    if (message instanceof ClientSOAPMessage) {
      soapmessage = (ClientSOAPMessage) message;
    }
    if (message instanceof ClientMimeMessage) {
      soapmessage = ((ClientMimeMessage) message).getSOAPMessage();
    }
    if (soapmessage != null) {
      incomingHeaders.clear();
      ArrayList headers = soapmessage.getHeaders();
      for (int i=0; i<headers.size();i++) {
        Element element = (Element) headers.get(i);
        QName qname = new QName(element.getNamespaceURI(),element.getLocalName());
        incomingHeaders.put(qname,element);
      }
    }
    return true;
  }

  public void clear() {
    incomingHeaders.clear();
    outgoingHeaders.clear();
  }

  public boolean handleFault(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    return true;
  }

  public boolean isFeatureImplemented(String featureName, PropertyContext property) {
    return false;
  }

  public String getName() {
    return this.NAME;
  }

  public String[] getFeatures() {
    return new String[0];
  }

  /**
   * This method allows the user to set output soap header.
   * Can use headerName = null, and w3c.dom.Element node
   * Can use schema element declaration name and java type representation instance
   * For the primitive classes wrappers must be used lien Integer for int.. and so on.
   * @param headerName
   * @param object
   * @throws java.rmi.MarshalException
   */
  public void setOutputHeader(QName headerName, Object object) throws MarshalException {
    if (headerName == null) {
      if (object instanceof Element) {
        Element element = (Element) object;
        if (element.getNamespaceURI() == null || element.getNamespaceURI().length()==0) {
          throw new MarshalException("Header nodes must be namespace qualified !");
        }
        QName qname = new QName(element.getNamespaceURI(),element.getNodeName());
        outgoingHeaders.put(qname,element);
      } else {
        throw new MarshalException("DOM Element must be passed when no element name is specified !");
      }
    } else {
      if (headerName.getNamespaceURI() == null || headerName.getNamespaceURI().length() == 0) {
        throw new MarshalException("Header nodes must be namespace qualified !");
      }
      if (typeMapping == null) {
        return;
      }
      QName elementType = typeMapping.getTypeForElement(headerName);
      if (elementType == null) {
        throw new MarshalException("Element "+headerName.toString()+" not declared in schema !");
      }
      Element outElement = document.createElementNS(headerName.getNamespaceURI(),headerName.getLocalPart());
      if (object == null) {
        outElement.setAttributeNS(XSI,"xsi:nil","true");
      } else {
        SerializerFactory sf = typeMapping.getSerializer(object.getClass(),elementType);
        if (sf == null) {
          throw new MarshalException(" Could not find deserializer for element :" + headerName.toString()+" with schema type "+elementType);
        }
        SerializerBase serializer = (SerializerBase) sf.getSerializerAs(null);
        SOAPSerializationContext context = new SOAPSerializationContext();
        context.setTypeMapping(typeMapping);
        serializer.serialize(object,outElement,context);
      }
      outgoingHeaders.put(headerName,outElement);
    }
  }

  public Element getInputHeader(QName headerName) {
    if (headerName == null) {
      return null;
    }
    return (Element) incomingHeaders.get(headerName);
  }

  public Object getInputHeader(QName headerName, Class headerClass) throws UnmarshalException {
    Element content = getInputHeader(headerName);
    if (content == null) {
      return null;
    }
    if (typeMapping == null) {
      return null;
    }
    QName elementType = typeMapping.getTypeForElement(headerName);
    if (elementType == null) {
      throw new UnmarshalException("Element "+headerName.toString()+" not declared in schema !");
    }
    DeserializerFactory factory = typeMapping.getDeserializer(headerClass,elementType);
    if (factory == null) {
      throw new UnmarshalException(" Could not find deserializer for element :" + headerName.toString()+" with schema type "+elementType);
    }
    DeserializerBase deserializer = (DeserializerBase) factory.getDeserializerAs(null);
    SOAPDeserializationContext context = new SOAPDeserializationContext();
    context.setTypeMapping(typeMapping);
    return deserializer.deserialize(content,context,headerClass);
  }

  public ClientFeatureProvider newInstance() {
    try {
      return new SoapHeadersProtocol();
    } catch (Exception e) {
      return null;
    }
  }

}
