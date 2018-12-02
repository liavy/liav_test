/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.messaging.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.lib.xml.parser.handlers.INamespaceHandler;
import com.sap.engine.lib.xml.parser.helpers.CharArray;
import com.sap.engine.lib.xml.parser.tokenizer.XMLDOMTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderUtil;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.jaxrpc.handlers.exceptions.JAXRPCHandlersException;
import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;

/**
 * Provides static methods for converting between different messages - SAAJ message, SAP message, ...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 20, 2006
 */
public class MessageConvertor {
  public static final MessageFactory SOAP11Factory;
  public static final MessageFactory SOAP12Factory;
  
  static {
    try {
      SOAP11Factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
      SOAP12Factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * By using <code>sapMsg</code> writer's internal buffer, the data from the buffer is read 
   * and a SAAL message is initialized from it. 
   */
  public static javax.xml.soap.SOAPMessage convertOutboundSAPMessageIntoSAAJ(Message sapMsg) throws Exception {
    MessageFactory msgFactory;
    SOAPMessageImpl soapMsg = (SOAPMessageImpl) sapMsg;
    if (SOAPMessage.SOAP11_NS.equals(soapMsg.getSOAPVersionNS())) {
      msgFactory = SOAP11Factory;
    } else { //this should be SOAP12.
      msgFactory = SOAP12Factory;
    }

    javax.xml.soap.SOAPMessage resultMsg = null;
    synchronized (msgFactory) {
      resultMsg = msgFactory.createMessage();
    }
    //process headers
    if (soapMsg.getSOAPHeaders().size() > 0) {
      SOAPHeader sHeader = resultMsg.getSOAPHeader();
      initSOAPHeader(sHeader, soapMsg.getSOAPHeaders());
    }
    //process body
    SOAPElement soapBody = resultMsg.getSOAPBody();
    //soapMsg.getBodyWriter().flush();
    ReferenceByteArrayOutputStream buffer = (ReferenceByteArrayOutputStream) soapMsg.getInternalWriterBuffer();
    //initializing reader
    InputStream baos = new ByteArrayInputStream(buffer.getContentReference(), 0, buffer.size());
    
    XMLTokenReader reader = XMLTokenReaderFactory.newInstance(baos, soapMsg.getBuiltInEnvNamespaceMap());     

    if (reader.moveToNextElementStart() == XMLTokenReader.EOF) {
      throw new JAXRPCHandlersException(JAXRPCHandlersException.READER_NOT_ON_STARTELEMENT);
    }
    XMLTokenReaderUtil.getDOMRepresentation(reader, soapBody);
    return resultMsg;
  }

  /**
   * By using <code>sapMsg</code> reader and header's list a new SAAJ message is created, initialized and
   * returned.
   * @param sapMsg
   * @return
   * @throws Exception
   */
  public static javax.xml.soap.SOAPMessage convertInboundSAPMessageIntoSAAJ(SOAPMessage sapMsg) throws Exception {
    SOAPMessage soapMsg = (SOAPMessage) sapMsg;
    MessageFactory msgFactory;
    if (SOAPMessage.SOAP11_NS.equals(soapMsg.getSOAPVersionNS())) {
      msgFactory = SOAP11Factory;
    } else { //this should be SOAP12.
      msgFactory = SOAP12Factory;
    }
    javax.xml.soap.SOAPMessage resultMsg;
    synchronized (msgFactory) {
      resultMsg = msgFactory.createMessage();
    }
    //process headers
    if (soapMsg.getSOAPHeaders().size() > 0) {
      SOAPHeader sHeader = resultMsg.getSOAPHeader();
      initSOAPHeader(sHeader, soapMsg.getSOAPHeaders());
    }
    //process body
    Element soapBody = resultMsg.getSOAPBody();
    XMLTokenReader reader = (XMLTokenReader) soapMsg.getBodyReader();
    XMLTokenReaderUtil.getDOMRepresentation(reader, soapBody);
    return resultMsg;
  }
  
  /**
   * Convert <code>jaxmMsg</code> data and set it into <code>sapMsg</code> in order to be used by streaming serialization framework.
   */
  public static void convertSAAJMessageIntoInputSAPMessage(javax.xml.soap.SOAPMessage jaxmMsg, Message sapMsg) throws Exception {
    SOAPMessageImpl msg = (SOAPMessageImpl) sapMsg;
    //process headers - remove all orignal headers and preset with those from JAXM message
    msg.getSOAPHeaders().clear();
    SOAPHeaderList shList = msg.getSOAPHeaders();
    SOAPHeader header = jaxmMsg.getSOAPHeader();
    if (header != null) {
      Iterator itr = header.getChildElements();
      SOAPElement sElem;
      while(itr.hasNext()) {
        sElem = (SOAPElement) itr.next();
        shList.addHeader(sElem);
      }        
    }
    //process body
    SOAPElement sBody = (SOAPElement) jaxmMsg.getSOAPBody();
    XMLDOMTokenReader domTokenReader = new XMLDOMTokenReader(sBody);
    domTokenReader.begin();
    msg.replaceBodyReader(domTokenReader);
  }
  /**
   * Serializes the content of <code>jaxmMsg</code> into buffer and presets this buffer inside <code>sapMsg</code> in order
   * the buffer content to be send as response.
   * The <code>sapMsg</code> should be in serialization mode.
   */
  public static void convertSAAJMessageIntoOutputSAPMessage(javax.xml.soap.SOAPMessage jaxmMsg, Message sapMsg) throws Exception {
    ReferenceByteArrayOutputStream buffer = new ReferenceByteArrayOutputStream();
    jaxmMsg.writeTo(buffer);
    SOAPMessageImpl msg = (SOAPMessageImpl) sapMsg;
    msg.presetBodyBuffer(buffer);
  }
  
  public static javax.xml.soap.SOAPMessage createSOAP11SAAJMessage() throws Exception {
    synchronized (SOAP11Factory) {
      return SOAP11Factory.createMessage();
    }
  }
  public static javax.xml.soap.SOAPMessage createSOAP12SAAJMessage() throws Exception {
    synchronized (SOAP12Factory) {
      return SOAP12Factory.createMessage();
    }
  }
  /**
   * Extracts the data from <code>hList</code> and sets it into <code>soapHeader</code>.
   */
  private static void initSOAPHeader(SOAPHeader soapHeader, SOAPHeaderList hList) throws SOAPException {
    Element hElems[] = hList.getHeaders();
    for (int i = 0; i < hElems.length; i++) {
      Element hEl = hElems[i];
      Element importedHEl = (Element) soapHeader.getOwnerDocument().importNode(hEl, true);
      soapHeader.appendChild(importedHEl);
//      Element newHElem = (Element) soapHeader.addHeaderElement(new QName(hElems[i].getNamespaceURI(), hElems[i].getLocalName()));
//      cloneElement(hElems[i], newHElem);
    }    
  }  
  /**
   * Clones <code>src</code> into <code>soapElem</code>. Clones its attributes and children notes.
   */
  private static void cloneElement(Element src, Element soapElem) throws SOAPException {
    //clone attributes
    Attr attr;
    NamedNodeMap attrs = src.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      attr = (Attr) attrs.item(i);
      if (attr.getNamespaceURI() != null) {
        soapElem.setAttributeNS(attr.getNamespaceURI(), attr.getNodeName(), attr.getValue());
      } else {
        soapElem.setAttribute(attr.getNodeName(), attr.getValue());
      }
    }
    
    Node node;
    NodeList children = src.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      node = children.item(i);
      int code = node.getNodeType();
      switch (code) {
        case Node.TEXT_NODE: {
          Text newText = soapElem.getOwnerDocument().createTextNode(((Text) node).getData());
          soapElem.appendChild(newText);
          break;
        }
        case Node.ELEMENT_NODE: {
          Element origEl = (Element) node;
          Element newElem = null;
          if (origEl.getNamespaceURI() != null) {
            newElem = soapElem.getOwnerDocument().createElementNS(origEl.getNamespaceURI(), origEl.getNodeName());            
          } else {
            newElem = soapElem.getOwnerDocument().createElement(origEl.getNodeName());                        
          }
          cloneElement(origEl, newElem);
          soapElem.appendChild(newElem);
        }
      }
    }  
  }
  

}
