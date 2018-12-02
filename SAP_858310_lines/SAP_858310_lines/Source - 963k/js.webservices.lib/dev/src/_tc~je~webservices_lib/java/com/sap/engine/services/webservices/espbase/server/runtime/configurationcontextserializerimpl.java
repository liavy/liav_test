/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.server.runtime;

import java.io.CharArrayReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.SOAPMessageImpl;
import com.sap.engine.services.webservices.espbase.server.additions.SOAPHTTPTransportBinding;
import com.sap.engine.services.webservices.tools.ReferenceCharArrayWriter;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.tools.SoftReferenceInstancesPool;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-28
 */
public class ConfigurationContextSerializerImpl {  
  static final String CONTEXT_ELEMENT  =  "context";
  static final String CONTEXT_NAME_ATTRIBUTE  =  "name";
  static final String PROPERTY_ELEMENT  =  "property";
  static final String PROPERTY_NAME_ELEMENT  =  "name"; 
  static final String PROPERTY_VALUE_ELEMENT  =  "value";
  static final String DATA_HOLDER_ELEMENT  =  "persistent-data";
  static final String MESSAGE_ELEMENT  =  "message";
  static final String MESSAGE_HEADERS  =  "message-headers";
  static final String MESSAGE_BODY  =  "message-body"; 
  static final String MESSAGE_TYPE_ATTR  =  "message-type"; //contains identifier, which determines the message type (SOAP11, SOAP12, MIME, ...) 
    
  static final char[] CDATA_START  =  new char[]{'<', '!', '[', 'C', 'D', 'A', 'T', 'A', '['};
  static final char[] CDATA_END  =  new char[]{']', ']', '>'};
  
  static final Location location = Location.getLocation(ConfigurationContextSerializerImpl.class);
  static final SoftReferenceInstancesPool<XMLTokenReader> readers = new SoftReferenceInstancesPool<XMLTokenReader>(); 
  static final SoftReferenceInstancesPool<Transformer> transformers = new SoftReferenceInstancesPool<Transformer>(); 
  
	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ConfigurationContextSerializer#deserialize(java.io.InputStream)
	 */
	static ConfigurationContext deserialize(InputStream in)	throws RuntimeProcessException {
    location.debugT("deserialize(), invoked...");
    //create and initialize contexts
    ProviderContextHelperImpl context = ProviderContextHelperImpl.getPooledInstance();
    ConfigurationContext pCtx = context.getPersistableContext();
    //initialize reader from stream
    XMLTokenReader reader = getReader();
    try {
      reader.init(in);
      reader.begin();
      moveToNextStartElement(reader); //this is should be the root element.
      //load message, the reader must be on start element
      moveToNextStartElement(reader);//this should be the message element
      String msgType = reader.getAttributes().getValue("", MESSAGE_TYPE_ATTR); 
      moveToNextStartElement(reader);//this should be the message_body element
      SOAPMessageImpl msg = (SOAPMessageImpl) SOAPHTTPTransportBinding.getSOAPMessage();
      reader.next();//move to CDATA section
      ReferenceCharArrayWriter buffer = new ReferenceCharArrayWriter();
      reader.writeTo(buffer);
      CharArrayReader cReader = new CharArrayReader(buffer.getContentReference(), 0, buffer.size());
      msg.initReadMode(cReader, msgType);
      msg.getBodyReader().moveToNextElementStart(); //moves the reader to the first start element.
      //load headers
      int code = reader.moveToNextElementStart();
      if (code == XMLTokenReader.STARTELEMENT && reader.getLocalName().equals(MESSAGE_HEADERS)) {
        Element headersHolder = reader.getDOMRepresentation(SharedDocumentBuilders.newDocument());
        SOAPHeaderList headers = msg.getSOAPHeaders();
        if (headersHolder != null) {
          NodeList chs = headersHolder.getChildNodes();
          for (int i = 0; i < chs.getLength(); i++) {
            headers.addHeader((Element) chs.item(i));
          }
        }                                    
      }
      context.setMessage(msg);
      moveToNextStartElement(reader); //this should be the context element for persistent context
      loadContext(pCtx, reader); //load the context data
      
      return context;
    } catch (Exception e) {
      throw new RuntimeProcessException(e);
    } finally {
      readers.rollBackInstance(reader);
    }
	}
  
	/**
	 * Serializes 
	 */
	static void serializeContext(XMLTokenWriter writer, ConfigurationContext ctx)	throws RuntimeProcessException {
		ProviderContextHelperImpl context = (ProviderContextHelperImpl) ctx;
    ConfigurationContext pCtx = context.getPersistableContext();
    
    location.debugT("serialize() invoked...");
    
    try {
      writer.enter(null, CONTEXT_ELEMENT);
      writer.writeAttribute(null, CONTEXT_NAME_ATTRIBUTE, pCtx.getName());
      serializeContext0(pCtx, writer);
      writer.leave();//leave context      
      writer.flush();
    } catch (Exception e) {
      throw new RuntimeProcessException(e);
    }
	}
  /**
   * Serializes message into <code>writer</code>.
   */
  static void serializeMessage(XMLTokenWriter writer, ConfigurationContext ctx, OutputStream out) throws RuntimeProcessException {
    ProviderContextHelperImpl context = (ProviderContextHelperImpl) ctx;
    //message persistance
    try {
      SOAPMessage soapMess = (SOAPMessage) context.getMessage();
      writer.enter(null, MESSAGE_ELEMENT);
      writer.writeAttribute(null, MESSAGE_TYPE_ATTR, soapMess.getSOAPVersionNS()); //for soap messages use the soap namespace as identifier.
        //serialize message body
        writer.enter(null, MESSAGE_BODY);
        XMLTokenReader reader = soapMess.getBodyReader();
        ReferenceCharArrayWriter buffer = new ReferenceCharArrayWriter();
        reader.writeTo(buffer);
        writer.writeContentCDataDirect(CDATA_START);
        writer.writeContentCDataDirect(buffer.getContentReference(), 0, buffer.size());
        writer.writeContentCDataDirect(CDATA_END);
        writer.leave();//leave message_body
        //serialize headers
        writer.flush();
        out.flush();
        Element holder = createHeadersHolderElement(soapMess);
        if (holder != null) {
          Transformer t = getTransformer();
          try {
            t.transform(new DOMSource(holder), new StreamResult(out));
          } finally {
            transformers.rollBackInstance(t);
          }
        }         
      writer.leave();//message element
    } catch (Exception e) {
      throw new RuntimeProcessException(e);
    }
  }
  
  private static void serializeContext0(ConfigurationContext ctx, XMLTokenWriter writer) throws Exception {
    String curProp, curValue;
    //process properties
    Iterator en = ctx.properties();    
    location.debugT("serializeContext(), properties of context: " + ctx.getName() + " instance: " +  ctx);
    while (en.hasNext()) {
      curProp = (String) en.next();
      curValue = (String) ctx.getProperty(curProp);
      location.debugT("serializeContext(), property: " + curProp + " value: " + curValue);
      writer.enter(null, PROPERTY_ELEMENT);
      writer.enter(null, PROPERTY_NAME_ELEMENT);
      writer.writeContent(curProp);
      writer.leave();//leave PROPERTY_NAME_ELEMENT
      writer.enter(null, PROPERTY_VALUE_ELEMENT);
      writer.writeContent(curValue);
      writer.leave();//leave PROPERTY_VALUE_ELEMENT 
      writer.leave();//leave PROPERTY_ELEMENT
    }
    //process subcontexts 
    String curSubC;
    en = ctx.subContexts();
    location.debugT("serializeContext(), subcontexts...");
    while (en.hasNext()) {
      curSubC = (String) en.next(); 
      location.debugT("serializeContext(), subcontext: " + curSubC);
      writer.enter(null, CONTEXT_ELEMENT);
      writer.writeAttribute(null, CONTEXT_NAME_ATTRIBUTE, curSubC);
      serializeContext0(ctx.getSubContext(curSubC), writer);
      writer.leave();
    }  
  }
  /** 
   * The reader is on start context element.
   */  
  private static void loadContext(ConfigurationContext ctx, XMLTokenReader reader) throws RuntimeProcessException, ParserException {
    location.debugT("loadContext(), name: " + ctx.getName());
    int currentLevel = reader.getCurrentLevel(); //keeps track of current depth in XML document
    int code;
    try {
      code = reader.moveToNextElementStart(); //move to context or property or EOF
    } catch (ParserException pE) {
      throw new RuntimeProcessException(pE);
    }
    while (true) {
      if (reader.getState() == XMLTokenReader.STARTELEMENT) {
        if (reader.getLocalName().equals(PROPERTY_ELEMENT)) { //this is property, load it
          moveToNextStartElement(reader); //this should be property name element
          reader.next(); //moves to chars
          String pName = reader.getValuePassChars();
          moveToNextStartElement(reader); //this should be property value element
          reader.next(); //moves to chars
          String pValue = reader.getValuePassChars();
          ctx.setProperty(pName, pValue);        
          location.debugT("loadContext(), setProp: " + pName + " pValue: " + pValue);
          try {
            code = reader.moveToNextElementStart(); //move to context or property or EOF
          } catch (ParserException pE) {
            throw new RuntimeProcessException(pE);
          }
          if (code == XMLTokenReader.EOF) {
            return;
          }
        } else if (reader.getLocalName().equals(CONTEXT_ELEMENT)) { //this is context, load it
          if (reader.getCurrentLevel() == currentLevel + 1) { //load the context from this level
            String ctxName = reader.getAttributes().getValue(CONTEXT_NAME_ATTRIBUTE);
            location.debugT("loadContext(), create new context: " + ctxName);
            ConfigurationContext pCtx = ctx.createSubContext(ctxName);
            loadContext(pCtx, reader);
          } else {
            return; //let this context element to be loaded by upper level
          }
        } else {
          throw new ServerRuntimeProcessException(RuntimeExceptionConstants.UNRECORGNIZED_RUNTIME_STORAGE_FORMAT_ELEMENT, new Object[]{reader.getLocalName()});
        }
      } else {
        return;
      }
    }
  }
  
  private static XMLTokenReader getReader() {
    XMLTokenReader reader = (XMLTokenReader) readers.getInstance();
    if (reader == null) {
      XMLTokenReaderFactory factory = XMLTokenReaderFactory.getInstance();
      reader = factory.createReader();  
    }
    return reader;
  }
  
  private static Transformer getTransformer() throws RuntimeProcessException {
    try {
      Transformer t = transformers.getInstance();
      if (t == null) {
        t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
      }
      return t;
    } catch (Exception e) {
      throw new RuntimeProcessException(e);
    }
  }
 
  /**
   * Moves the reader to next start element, or throws exception if EOF is reached.
   */
  private static void moveToNextStartElement(XMLTokenReader reader) throws RuntimeProcessException {
    int code;
    try {
      code = reader.moveToNextElementStart();
    } catch (ParserException pE) {
      throw new RuntimeProcessException(pE);
    }
    if (code == XMLTokenReader.STARTELEMENT) {
      return;
    } else {
      throw new RuntimeProcessException("Unexpected EOF reached.");
    }
  }
  
  private static Element createHeadersHolderElement(SOAPMessage msg) {
    Element[] list = msg.getSOAPHeaders().getHeaders();
    if (list.length > 0) { //there is header(s)
      Element holder = list[0].getOwnerDocument().createElement(MESSAGE_HEADERS);
      for (int i = 0; i < list.length; i++) {
        holder.appendChild(list[i]);
      }
      return holder;
    }
    return null;
  }
  
  public static void main(String[] arg) throws Exception {
    ProviderContextHelperImpl ctx = ProviderContextHelperImpl.getPooledInstance();
//    ctx.setProtocols(new ProviderProtocol[0]);
//    ctx.getPersistableContext().setProperty("prop1", "value1");
//    ctx.getPersistableContext().setProperty("prop2", "value2");
//    ConfigurationContext sub = ctx.getPersistableContext().createSubContext("subContext0");
//    sub.setProperty("prop3", "valu3");
//    sub = sub.createSubContext("subContext00");
//    sub.setProperty("prop4", "value4");
//    sub = ctx.getPersistableContext().createSubContext("subContext1");
//    sub.setProperty("prop5", "value5");
    SOAPMessage msg = new SOAPMessageImpl();
//    msg.initReadMode(new FileInputStream("D:/temp/soapmsg.xml"));
//    XMLTokenReader reader = msg.getBodyReader();
//    reader.moveToNextElementStart();
//    ctx.setMessage(msg);
//    System.out.println("Source: " + ctx.toString());
    String sFile = "D:/temp/persistantsoapmsg.xml";
    ConfigurationContextSerializerImpl serializer = new ConfigurationContextSerializerImpl();
//    FileOutputStream out = new FileOutputStream(sFile); 
//    //serialize context into stream
//    XMLTokenWriterImpl writer = new XMLTokenWriterImpl();
//    writer.init(out);
//    //writer root element
//    writer.enter(null, ConfigurationContextSerializerImpl.DATA_HOLDER_ELEMENT);
//    //serialize message
//    ConfigurationContextSerializerImpl.serializeMessage(writer, ctx, out);
//    writer.flush();
//    //serialize context data
//    ConfigurationContextSerializerImpl.serializeContext(writer, ctx);
//    writer.leave(); //leave root element
//    writer.flush();
//    out.close();
    FileInputStream in = new FileInputStream(sFile);
    ctx = (ProviderContextHelperImpl) serializer.deserialize(in);
    System.out.println("Result: " + ctx.toString());
    msg = (SOAPMessage) ctx.getMessage();
    System.out.println("Result, message " + msg.getBodyReader().getDOMRepresentation(SharedDocumentBuilders.newDocument()));
    Element headers[] = msg.getSOAPHeaders().getHeaders();
    for (int i = 0; i < headers.length; i++) {
      System.out.println("Header: " + headers[i]);      
    }
    System.out.println(ctx.getPersistableContext());
  }
}
