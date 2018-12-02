package com.sap.engine.services.webservices.espbase.client.bindings.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.exceptions.TransportBindingException;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.jaxrpc.encoding.SerializationUtil;
import com.sap.engine.services.webservices.jaxrpc.util.DocumentPool;

public class FaultUtil {
  
  /**
   * Returns first element found in fault detail.
   * @param detail
   * @return
   */
  public static SOAPElement getDetailRootElement(Detail detail) {
    if (detail == null) {
      return null;
    }
    Iterator it = detail.getDetailEntries();
    SOAPElement result = null;
    while(it.hasNext()) {
      Object next = it.next();
      if (next instanceof SOAPElement) {
        result = (SOAPElement) next;
        break;
      }
    }
    return(result);
  }
  
  
  /**
   * Reads SOAP Message Fault and builds SOAP Fault Exception.
   * The message must be positioned on soap:fault node or on soap:body.
   * @param message
   * @return
   * @throws Exception
   */
  public static SOAPFaultException buildFaultException(XMLTokenReader reader, ClientConfigurationContext context) throws RemoteException {
    if(isSOAP12Mode(context)) {
      return(buildFaultException_SOAP12(reader));        
    } else {
      return(buildFaultException_SOAP11(reader,null));
    }
  }

  /**
   * Returns prefix mapping.
   * @param pref
   * @param localAttrs
   * @param reader
   * @return
   */
  private static String getPrefixUri(String pref, Attributes localAttrs, XMLTokenReader reader) {
    //search in the local element declarations
    String value = localAttrs.getValue("xmlns:" + pref);
    if (value != null) {
      return(value);
    }
    //use upper level declarations
    return(reader.getPrefixMapping(pref));
  }

  /**
   * @return true only if a property denoting SOAP1.2 processing is set.
   */
  public static boolean isSOAP12Mode(ClientConfigurationContext ctx) {
    //in case in the WSDL SOAP1.2 is defined
    String sV = ctx.getStaticContext().getInterfaceData().getProperty(InterfaceMapping.SOAP_VERSION);
    if (InterfaceMapping.SOAP_VERSION_12.equals(sV)) {
      return(true);
    }
    //in case a property for soap1.2 is set
    String s = (String) ctx.getPersistableContext().getProperty(PublicProperties.P_STUB_SOAP_VERSION);
    if (PublicProperties.P_STUB_SOAP_VERSION_12.equals(s)) {
      return(true);
    }   
    return(false);
  }
  
  /**
   * Reads SOAP 1.1 Fault from the message and fills it's contents in SOAPFaultException.
   * The message must be positioned on soap:fault node or on soap:body.
   * @param message
   * @return
   * @throws Exception
   */
  public static SOAPFaultException buildFaultException_SOAP11(XMLTokenReader reader, ArrayList remainingElements) throws RemoteException {
    try {
      reader.passChars();
      if(reader.getState() != XMLTokenReader.STARTELEMENT) {
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      // Bypasses the body element
      if(SOAPMessage.BODYTAG_NAME.equals(reader.getLocalName()) && SOAPMessage.SOAP11_NS.equals(reader.getURI())) { 
        reader.next();
        reader.passChars();
      }
      if(reader.getState() != XMLTokenReader.STARTELEMENT) {
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      String localName = reader.getLocalName();
      String uri = reader.getURI();
      if (!SOAPMessage.FAULTTAG_NAME.equals(localName) || !SOAPMessage.SOAP11_NS.equals(uri)) { // Passes fault element
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      // reader is positioned on SOAP:Fault and goes to the next xml element
      reader.next();
      reader.passChars();
      // SOAP:Fault components
      QName faultCode = null;
      String faultString = null;
      Detail detailNode = null;
      String faultActor = null;
      while(reader.getState() == XMLTokenReader.STARTELEMENT) {
        boolean flag = false; // rean only one node per cycle
        localName = reader.getLocalName();
        uri = reader.getURI();
        if(flag == false && SOAPMessage.FAULTCODETAG_NAME.equals(localName) && (uri == null || uri.length()==0)) {
          if(faultCode != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_CODE);
          }
          flag = true;
          Attributes localAttrs = reader.getAttributes(); //because getValuePassChars() unmaps the locally defined prefies
          reader.next();
          String qName = reader.getValuePassChars();
          String lName = DOM.qnameToLocalName(qName);
          String qNameUri = getPrefixUri(DOM.qnameToPrefix(qName), localAttrs, reader);
          faultCode = new QName(qNameUri,lName);
          if(reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_CODE);
          }
          // leaves fault code element
          reader.next(); 
          reader.passChars();
        }
        if(flag == false && SOAPMessage.FAULTSTRINGTAG_NAME.equals(localName) && (uri == null || uri.length()==0)) {
          if (faultString != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_STRING);
          }
          flag = true;
          reader.next();
          faultString = reader.getValuePassChars();
          if(reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_STRING);
          }
          // leaves fault string element
          reader.next(); 
          reader.passChars();
        }
        if(flag == false && SOAPMessage.ACTORTAG_NAME.equals(localName) && (uri == null || uri.length()==0)) {
          if (faultActor != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_ACTOR);
          }
          flag = true;
          reader.next();
          faultActor = reader.getValuePassChars();
          if (reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_ACTOR);
          }
          // leaves fault actor element
          reader.next(); 
          reader.passChars();
        }
        if(flag == false && SOAPMessage.SOAP11_DETAILTAG_NAME.equals(localName) && (uri == null || uri.length()==0)) {
          if (detailNode != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_DETAIL);
          }
          flag = true;
          detailNode = loadSOAPDetail(reader,false);
        }
        if(flag == false) {
          throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_CONTENT, uri, localName);
        }
      }
      // Leave the SOAP Fault element.
      if (reader.getState() == XMLTokenReader.ENDELEMENT) {
        reader.next(); reader.passChars(); 
      }
      // Used to resolve IDRef references used in SOAP Encoded messages
      if (remainingElements != null) {
        Document soap11document = DocumentPool.getSOAP11Document();
        try {
          while (reader.getState() == XMLTokenReader.STARTELEMENT) {          
            Element element = reader.getDOMRepresentation(soap11document);
            if (element.getAttribute("id").length() != 0) {
              remainingElements.add(element);
            } else {            
              throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_CONTENT, element.getNamespaceURI(), element.getLocalName());
            }
            reader.next();
            reader.passChars();
          }
        } finally {
          DocumentPool.returnSOAP11Document(soap11document);
        }
      }
      SOAPFaultException result = new com.sap.engine.services.webservices.jaxrpc.exceptions.SOAPFaultException(faultCode,faultString,faultActor,detailNode);
      while(reader.getState()!=XMLTokenReader.EOF) {
        reader.next();
      }
      return(result);
    } catch(ParserException parserExc) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, parserExc, parserExc.getMessage());
    } catch (SOAPException soapExc) {
      throw new TransportBindingException(TransportBindingException.FAULT_DETAIL_BUILD_FAIL, soapExc);
    }
  }  
  
  /**
   * Reads SOAP 1.2 Fault from the message and fills it's contents in SOAPFaultException.
   * The message must be positioned on soap:fault node or on soap:body.
   * @param message
   * @return
   * @throws Exception
   */
  private static SOAPFaultException buildFaultException_SOAP12(XMLTokenReader reader) throws RemoteException {
    try {
      reader.passChars();
      if(reader.getState() != XMLTokenReader.STARTELEMENT) {
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      // Bypasses the body element
      if(SOAPMessage.BODYTAG_NAME.equals(reader.getLocalName()) && SOAPMessage.SOAP12_NS.equals(reader.getURI())) { 
        reader.moveToNextElementStart();
      }
      if(reader.getState() != XMLTokenReader.STARTELEMENT) {
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      String localName = reader.getLocalName();
      String uri = reader.getURI();
      if (!SOAPMessage.FAULTTAG_NAME.equals(localName) || !SOAPMessage.SOAP12_NS.equals(uri)) { // Passes fault element
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      //reader is positioned on SOAP:Fault and goes to the next xml element
      reader.moveToNextElementStart();
      // SOAP:Fault components
      QName code = null;
      String reason = null;
      Detail detailNode = null;
      String role = null;
      //Read 'Code' element
      localName = reader.getLocalName();
      uri = reader.getURI();
      if (SOAPMessage.CODETAG_NAME.equals(localName) && SOAPMessage.SOAP12_NS.equals(uri)) {
        // reader is positioned on Fault:Code and goes to the next xml element
        int xmlLevel = reader.getCurrentLevel();
        reader.moveToNextElementStart();
        if (reader.getState() == XMLTokenReader.STARTELEMENT && SOAPMessage.SOAP12_NS.equals(reader.getURI()) && SOAPMessage.VALUETAG_NAME.equals(reader.getLocalName())) {
          // Reads the code "value" tag.
          Attributes localAttrs = reader.getAttributes(); //because getValuePassChars() unmaps the locally defined prefies
          reader.next();
          String qName = reader.getValuePassChars();
          String lName = DOM.qnameToLocalName(qName);
          String qNameUri = getPrefixUri(DOM.qnameToPrefix(qName), localAttrs, reader);
          code = new QName(qNameUri,lName);
          if(reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_CODE);
          }
          // Leave the value tag
          reader.next();reader.passChars();
        } else {
          throw new RuntimeException("'Value' element is required child element of 'Code' element");
        }
        while (reader.getCurrentLevel() >= xmlLevel) {
          reader.next();
          if (reader.getState() == XMLTokenReader.EOF) {
            throw new RuntimeException("Message EOF Reached while searching for ["+SOAPMessage.CODETAG_NAME+"] end tag.");
          }                    
        }
        reader.next();
        reader.passChars();
      } else {
        throw new RuntimeException("'Code' element is required child element of 'Fault' element");        
      }
      //Read 'Reason' element
      localName = reader.getLocalName();
      uri = reader.getURI();      
      if(SOAPMessage.REASONTAG_NAME.equals(localName) && SOAPMessage.SOAP12_NS.equals(uri)) {
        //move to the first Text element
        int xmlLevel = reader.getCurrentLevel();
        reader.moveToNextElementStart();
        if (SOAPMessage.TEXTTAG_NAME.equals(reader.getLocalName()) && SOAPMessage.SOAP12_NS.equals(reader.getURI())) {
          reader.next(); //move to the chars
        } else {
          throw new RuntimeException("'Reason' element needs to possess at least one child 'Text' element");
        }
        reason = reader.getValuePassChars(); //here the 'lang' attr value is not taken in consideration.
        if(reader.getState() != XMLTokenReader.ENDELEMENT) {
          throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_STRING);
        }
        reader.next(); reader.passChars(); // leave the text element.
        while (reader.getCurrentLevel() >= xmlLevel) {
          reader.next();
          if (reader.getState() == XMLTokenReader.EOF) {
            throw new RuntimeException("Message EOF Reached while searching for ["+SOAPMessage.REASONTAG_NAME+"] end tag.");
          }          
        }
        // Leaves the reason tag
        reader.next();
        reader.passChars();        
      } else {
        throw new RuntimeException("'Reason' element is required child element of 'Fault' element");                
      }
      if (reader.getState() == XMLTokenReader.STARTELEMENT && reader.getLocalName().equals(SOAPMessage.NODETAG_NAME) && SOAPMessage.SOAP12_NS.equals(reader.getURI())) {
         // Pass the optional "Node" tag.
        SerializationUtil.bypassElementContents(reader);
      }
      if(reader.getState() == XMLTokenReader.STARTELEMENT && reader.getLocalName().equals(SOAPMessage.ROLETAG_NAME) && SOAPMessage.SOAP12_NS.equals(reader.getURI())) {
        role = getRoleValue(reader);
        reader.next(); reader.passChars();
      }
      if(reader.getState() == XMLTokenReader.STARTELEMENT && reader.getLocalName().equals(SOAPMessage.SOAP12_DETAILTAG_NAME) && SOAPMessage.SOAP12_NS.equals(reader.getURI())) {
        detailNode = loadSOAPDetail(reader,true);
        reader.next(); reader.passChars();
      }
      SOAPFaultException result = new com.sap.engine.services.webservices.jaxrpc.exceptions.SOAPFaultException(code, reason, role, detailNode);
      return(result);
    } catch (ParserException parserExc) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, parserExc, parserExc.getMessage());
    } catch (SOAPException soapExc) {
      throw new TransportBindingException(TransportBindingException.FAULT_DETAIL_BUILD_FAIL, soapExc);
    }
  }  
  
  /**
   * @param reader the reader must be on 'Role' start element.
   * @return the content of 'Role' element.
   */
  private static String getRoleValue(XMLTokenReader reader) throws ParserException {
    String res = reader.getValuePassChars();
    if(reader.getState() == XMLTokenReader.ENDELEMENT) {
      if(SOAPMessage.ROLETAG_NAME.equals(reader.getLocalName())  && SOAPMessage.SOAP12_NS.equals(reader.getURI())) {
        return(res);
      }
    }
    throw new RuntimeException("'Role' could contain only characters.");
  }
  
    
  private static Detail loadSOAPDetail(XMLTokenReader reader,boolean isSOAP12) throws SOAPException,ParserException {
    Document document = null;
    SOAPFactory factory = null;
    if (isSOAP12) {
      document = DocumentPool.getSOAP11Document();
      factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
    } else {
      document = DocumentPool.getSOAP12Document();
      factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    }
    Detail detailNode = factory.createDetail();
    SOAPElement element = (SOAPElement) reader.getDOMRepresentation(document);
    NamedNodeMap attributes = element.getAttributes();
    for(int j=0; j<attributes.getLength(); j++) {
      Attr attrib = (Attr) attributes.item(j);
      attrib = (Attr) detailNode.getOwnerDocument().importNode(attrib,false);
      detailNode.setAttributeNodeNS(attrib);
    }
    Iterator elements  = element.getChildElements();
    while(elements.hasNext()) {
      Node next = (Node) elements.next();
      if(next instanceof SOAPElement) {
        detailNode.addChildElement((SOAPElement) next);
      }
      if(next instanceof javax.xml.soap.Text) {
        detailNode.addTextNode(((javax.xml.soap.Text) next).getValue());
      }
    }
    reader.next();
    reader.passChars();
    return detailNode;
  }  
  
}
