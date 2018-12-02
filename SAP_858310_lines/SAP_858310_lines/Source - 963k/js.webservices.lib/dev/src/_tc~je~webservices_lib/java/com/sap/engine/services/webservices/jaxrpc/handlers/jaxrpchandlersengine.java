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
package com.sap.engine.services.webservices.jaxrpc.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.sap.engine.interfaces.webservices.esp.ApplicationWebServiceContextExt;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.handlers.INamespaceHandler;
import com.sap.engine.lib.xml.parser.helpers.CharArray;
import com.sap.engine.lib.xml.parser.tokenizer.XMLDOMTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderUtil;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.SOAPMessageImpl;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.jaxrpc.handlers.exceptions.JAXRPCHandlersException;
import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.tc.logging.Location;

/**
 * This class encapsulates the main part of handlers processing logic.
 * Its methods are used by both, consumer and provider, protocol implementations. 
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-3-11
 */
public class JAXRPCHandlersEngine {
  
  private static final String JEE_NS  =  "http://java.sun.com/xml/ns/javaee";
  private static final String HANDLER_ELEM  =  "handler";
  private static final String HANDLERNAME_ELEM  =  "handler-name";
  private static final String HANDLERCLASS_ELEM  =  "handler-class";
  private static final String INITPARAM_ELEM  =  "init-param";
  private static final String PARAMNAME_ELEM  =  "param-name";
  private static final String PARAMVALUE_ELEM  =  "param-value";
  private static final String SOAPROLE_ELEM  =  "soap-role";
  private static final String SOAPHEADER_ELEM  =  "soap-header";
  private static final String SOAPCONTEXT_PROPERTY  =  "jax-rpc-soapcontext"; //under this name SOAPMessageContext object is mapped.

  private static final String APP_WSCONTEXT = "/wsContext/" + ApplicationWebServiceContextExt.APPLICATION_WSCONTEXT;
    
  private static MessageFactory msgFactory;

  static {
    try {
      msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
    } catch (SOAPException x) {
      msgFactory = null;
    }
  }
  
  private static final Location LOC = Location.getLocation(JAXRPCHandlersEngine.class);
  
  /**
   * This method is applicable only when used on server side.
   * @return SOAPMessageContext instance, extracted from thread local, or null if no SOAPMessageContext is bound.
   * @throws RuntimeException if anything goes wrong.  
   */
  public static final SOAPMessageContext getSOAPMessageContextFromThread() {
    try {
      Properties p = new Properties();
      p.put("domain", "true");
      Context jndiCtx = new InitialContext(p);
      ApplicationWebServiceContextExt appWSContext = (ApplicationWebServiceContextExt) jndiCtx.lookup(APP_WSCONTEXT);
      ProviderContextHelper ctx = (ProviderContextHelper) appWSContext.getConfigurationContext();
      return getSOAPMessageContext(ctx.getDynamicContext());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  /**
   * Extracts SOAPMessageContext instance from <code>ctx</code>.
   * @return SOAPMessageContext instance, or null if in <code>ctx</code> no SOAPMessageContext is bound.
   */
  public static final SOAPMessageContext getSOAPMessageContext(ConfigurationContext ctx) {
    return (SOAPMessageContext) ctx.getProperty(SOAPCONTEXT_PROPERTY);
  }
  /**
   * Binds <code>soapCtx</code> under dedicated name in <code>ctx</code>
   */
  public static final void bindSOAPMessageContext(ConfigurationContext ctx, SOAPMessageContext soapCtx) {
    ctx.setProperty(SOAPCONTEXT_PROPERTY, soapCtx);
  }
  
  /**
   * Parses <code>configXml</code> parameter to DOM tree and extracts its information.
   * @return ordered list of initialized HandlerInfoImpl objects. Returned HandlerInfoImpl object
   * do not have handlers' Class object set. 
   */
  public static List loadHanderInfoConfigurations(String configXml) throws JAXRPCHandlersException {
    try {
      //converts the config string into utf-8 encoded byte[]
      Element cfgRoot;
      cfgRoot = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, new InputSource(new StringReader(configXml))).getDocumentElement();
      List hs = DOM.getChildElementsByTagNameNS(cfgRoot, JEE_NS, HANDLER_ELEM);
      List result = new ArrayList();
      Element hElem;
      HandlerInfo hInfo;
      for (int i = 0; i < hs.size(); i++) {
        hElem = (Element) hs.get(i);
        hInfo = loadHandlerData(hElem);
        result.add(hInfo);
      }
      return result;
    } catch (Exception e) {
      if (e instanceof JAXRPCHandlersException) {
        throw (JAXRPCHandlersException) e;
      } else {
        throw new JAXRPCHandlersException(e);
      }
    }   
  }
  /**
   * Creates and initializes JAXM SOAPMessage using the data from <code>sapMsg</code>. 
   */
  public static javax.xml.soap.SOAPMessage createInboundJAXMSOAPMessage(Message sapMsg) throws JAXRPCHandlersException {
    try {
      SOAPMessage soapMsg = (SOAPMessage) sapMsg;
      javax.xml.soap.SOAPMessage resultMsg = msgFactory.createMessage(); //no need to be synchronized
      //process headers
      if (soapMsg.getSOAPHeaders().size() > 0) {
        initSOAPHeader(resultMsg, soapMsg.getSOAPHeaders());
      }
      //process body
      SOAPElement soapBody = resultMsg.getSOAPBody();
      XMLTokenReader reader = (XMLTokenReader) soapMsg.getBodyReader();
      XMLTokenReaderUtil.getDOMRepresentation(reader, soapBody);
      return resultMsg;
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }     
  }
  /**
   * Creates and initializes JAXM SOAPMessage using the data from <code>sapMsg</code>. 
   */
  public static javax.xml.soap.SOAPMessage createOutboundJAXMSOAPMessage(Message sapMsg) throws JAXRPCHandlersException {
    try {
      SOAPMessageImpl soapMsg = (SOAPMessageImpl) sapMsg;
      javax.xml.soap.SOAPMessage resultMsg = msgFactory.createMessage(); //no need to be synchronized
      //process headers
      if (soapMsg.getSOAPHeaders().size() > 0) {
        initSOAPHeader(resultMsg, soapMsg.getSOAPHeaders());
      }
      //process body
      SOAPElement soapBody = resultMsg.getSOAPBody();
      //soapMsg.getBodyWriter().flush();
      ReferenceByteArrayOutputStream buffer = (ReferenceByteArrayOutputStream) soapMsg.getInternalWriterBuffer();
      //initializing reader
      //copies the envelope-level prefix declarations into parser's nshandler.
      InputStream baos = new ByteArrayInputStream(buffer.getContentReference(), 0, buffer.size());

      XMLTokenReader reader = XMLTokenReaderFactory.newInstance(baos, soapMsg.getBuiltInEnvNamespaceMap());
    
      if (reader.moveToNextElementStart() == XMLTokenReader.EOF) {
        throw new JAXRPCHandlersException(JAXRPCHandlersException.READER_NOT_ON_STARTELEMENT);
      }
      XMLTokenReaderUtil.getDOMRepresentation(reader, soapBody);
      return resultMsg;
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }     
  }
  /**
   * Serializes the content of <code>jaxmMsg</code> into buffer and presets this buffer inside <code>sapMsg</code> in order
   * the buffer content to be send as response.
   * The <code>sapMsg</code> should be in serialization mode.
   */
  public static void serializeJAXMessageIntoSOAPMessage(javax.xml.soap.SOAPMessage jaxmMsg, Message sapMsg) throws JAXRPCHandlersException {
    try {
      ReferenceByteArrayOutputStream buffer = new ReferenceByteArrayOutputStream();
      jaxmMsg.writeTo(buffer);
      SOAPMessageImpl msg = (SOAPMessageImpl) sapMsg;
      msg.presetBodyBuffer(buffer);
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }
  }
  /**
   * Convert <code>jaxmMsg</code> data and set it into <code>sapMsg</code> in order to be used by streaming serialization framework.
   */
  public static void convertInboundJAXMessageDataIntoSOAPMessage(javax.xml.soap.SOAPMessage jaxmMsg, Message sapMsg) throws JAXRPCHandlersException {
    try {
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
      SOAPElement sBody = jaxmMsg.getSOAPBody();
      XMLDOMTokenReader domTokenReader = new XMLDOMTokenReader(sBody);
      domTokenReader.begin();
      msg.replaceBodyReader(domTokenReader);
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }        
  }
  
  public static javax.xml.soap.SOAPMessage createMessage() throws JAXRPCHandlersException {
    try {
      return msgFactory.createMessage();
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }
  }
  /**
   * Extracts the data from <code>hList</code> and sets it into <code>soapHeader</code>.
   */
  private static void initSOAPHeader(javax.xml.soap.SOAPMessage soapMessage, SOAPHeaderList hList) throws SOAPException {
    SOAPHeader soapHeader = soapMessage.getSOAPHeader();
    SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
    Element hElems[] = hList.getHeaders();
    for (int i = 0; i < hElems.length; i++) {
      Element newHElem = (Element) soapHeader.addHeaderElement(envelope.createName(hElems[i].getLocalName(),"ns", hElems[i].getNamespaceURI()));
      cloneElement(hElems[i], newHElem);
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
        }
      }
    }  
  }
  /**
   * Loads hander data from its description element <code>hElem</code>
   */
  private static HandlerInfoImpl loadHandlerData(Element hElem) throws JAXRPCHandlersException {
    String hName = getSingleElementData(hElem, JEE_NS, HANDLERNAME_ELEM);
    String hClass = getSingleElementData(hElem, JEE_NS, HANDLERCLASS_ELEM);
    
    if (LOC.beDebug()) {
      LOC.debugT("JAXRPCHandlersEngine.loadHandlerData() hName '" + hName + "', hClass '" + hClass + "'");
    }
    //load headers
    List headers = DOM.getChildElementsByTagNameNS(hElem, JEE_NS, SOAPHEADER_ELEM);
    QName hQNames[] = new QName[headers.size()];
    Element headerElem;
    String headerQName, pref, ns;
    for (int i = 0; i < headers.size(); i++) {
      headerElem = (Element) headers.get(i);
      headerQName = getTextData(headerElem);
      pref = DOM.qnameToPrefix(headerQName);
      ns = (String) DOM.getNamespaceMappingsInScope(headerElem).get(pref);//get ns for prefix
      if (ns == null) {
        throw new JAXRPCHandlersException(JAXRPCHandlersException.PREFIX_NOT_MAPPED, new Object[]{pref, hElem.getParentNode()});
      }
      hQNames[i] = new QName(ns, DOM.qnameToLocalName(headerQName));
      if (LOC.beDebug()) {
        LOC.debugT("JAXRPCHandlersEngine.loadHandlerData() hName '" + hName + "', hClass '" + hClass + "', add header '" + hQNames[i] + "'");
      }
    }
    
    //load params
    Map paramMap = new HashMap();
    List paramList = DOM.getChildElementsByTagNameNS(hElem, JEE_NS, INITPARAM_ELEM);
    for (int i = 0; i < paramList.size(); i++) {
      loadParameter((Element) paramList.get(i), paramMap);
    }   
    
    //load soap-roles
    List roles = DOM.getChildElementsByTagNameNS(hElem, JEE_NS, SOAPROLE_ELEM);
    String[] rolesArr = new String[roles.size()];
    for (int i = 0; i < roles.size(); i++) {
      rolesArr[i] = getTextData((Element) roles.get(0));
    }
    
    HandlerInfoImpl hInfo = new HandlerInfoImpl(hName, hClass, rolesArr);
    hInfo.setHandlerConfig(paramMap);
    if (LOC.beDebug()) {
      LOC.debugT("JAXRPCHandlersEngine.loadHandlerData() hName '" + hName + "', hClass '" + hClass + "', set params '" + paramMap + "'");
    }
    hInfo.setHeaders(hQNames);
    return hInfo;
  }
  /**
   * Loads parameter name an value from <code>paramElem</code> and sets them in <code>paramMap</code>
   */
  private static void loadParameter(Element paramElem, Map paramMap) throws JAXRPCHandlersException {
    String name = getSingleElementData(paramElem, JEE_NS, PARAMNAME_ELEM);
    String value = getSingleElementData(paramElem, JEE_NS, PARAMVALUE_ELEM);
    paramMap.put(name, value);
  }
  
  /**
   * Searches the <code>holder</code> for single element with specified <code>ns</code> and <code>lName</code>.
   * If found single one, its Text child is taken and its data returned. If zero or more elements are found, 
   * exception is thrown
   */
  private static String getSingleElementData(Element holder, String ns, String lName) throws JAXRPCHandlersException {
    List tmpList = DOM.getChildElementsByTagNameNS(holder, ns, lName);
    if (tmpList.size() != 1) {
      String qname = "{" + ns + "}" + lName;
      throw new JAXRPCHandlersException(JAXRPCHandlersException.ELEMENT_NOT_FOUND, new Object[]{qname, "" + tmpList.size(), holder});
    }
    Element elem = (Element) tmpList.get(0);
    return getTextData(elem);
  }
  /**
   * Returns text data of <code>elem</code>. 
   */ 
  private static String getTextData(Element elem) throws JAXRPCHandlersException {
    Node text = elem.getFirstChild();
    if (text instanceof Text) {
      return ((Text) text).getData();      
    } else {
      throw new JAXRPCHandlersException(JAXRPCHandlersException.MISSING_TEXT_DATA, new Object[]{elem});
    }
  }
  /**
   * Adds the prefixes from <code>envMap</code> into <code>nsHandler</code>.
   */
  private static void addEnvelopePrefixesInReader(INamespaceHandler nsHandler, Map envMap) {
    final String mName = "private static void addEnvelopePrefixesInReader(INamespaceHandler nsHandler, Map envMap)";
    
    LOC.debugT(mName + " entered...");
    
    //keys are prefix namespaces
    String prefNS, pref;
    Iterator itr = envMap.keySet().iterator();
    while (itr.hasNext()) {
      prefNS = (String) itr.next();
      pref = (String) envMap.get(prefNS);
      LOC.debugT(mName + ": set pref '" + pref + "' for ns '" + prefNS + "'");
      nsHandler.add(new CharArray(pref), new CharArray(prefNS));
    }
  }
}
