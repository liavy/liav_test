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

package com.sap.engine.services.webservices.espbase.messaging.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLBinaryTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriterFactory;
import com.sap.engine.lib.xml.util.DOMSerializer;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.lib.xml.util.NestedIOException;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * SOAPMessage interface implementation.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 1.0
 */
public class SOAPMessageImpl implements SOAPMessage {
  
  public static final String XS_PREFIX  =  "xs";
  public static final String XSI_PREFIX  =  "xsi";
  public static final String UTF8 = "UTF-8";
    
  private static final String BODYTAG  =  SOAPENV_PREFIX+":"+BODYTAG_NAME;
  private static final String HEADERTAG  =  SOAPENV_PREFIX+":"+HEADERTAG_NAME;
  private static final String ENVELOPETAG = SOAPENV_PREFIX+":"+ENVELOPETAG_NAME;  
  
  private static final int BUFFER_INITIAL_SIZE = 4 * 1024; //4k bytes  

  private static final byte[] XMLDECLARATION_START = "<?xml version=\"1.0\" encoding=\"".getBytes(); //$JL-I18N$
  private static final byte[] XMLDECLARATION_END = "\"?>".getBytes(); //$JL-I18N$
  private static final byte[] CLOSING_BRACKET = ">".getBytes(); //$JL-I18N$
  
  private static final String XS_NS = "http://www.w3.org/2001/XMLSchema";
  private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
  
//  private static final String STARTENVELOPE_TAG = "<" + ENVELOPETAG + " xmlns:" + SOAPENV_PREFIX + "=\"" + SOAP_NS + "\" xmlns:" + 
//                                                  XSI_PREFIX + "=\"" + XSI_NS + "\" xmlns:" + XS_PREFIX + "=\"" + XS_NS + "\"";   
  private static final String ENDENVELOPE_TAG   = "</" + ENVELOPETAG + ">";
  private static final String STARTHEADER_TAG   = "<" + HEADERTAG + ">";
  private static final String ENDHEADER_TAG     = "</" + HEADERTAG + ">";
    

  private SOAPHeaderList headers;
  private int currentState = UNDEFINED;
  //private ByteArrayOutputStream messageBuffer;
  private ByteArrayOutputStream bodyBuffer;
  private XMLTokenWriter messageWriter; // XMLToken writer
  //added by Aleksandar Aleksandrov 11.04.2005
  private XMLTokenWriter originalMessageWriter;
  private XMLTokenReader messageReader; // XMLToken reader
  private DOMSerializer serializer; // DOM Serializer used to serialize the SOAPHeaders
  private Hashtable additionalEnvNamespaces;
  private Hashtable builtInEnvNamespaces = new Hashtable();
  
  private String encoding = UTF8;
  private Document document;
  private XMLTokenReader substituteReader = null;
  private boolean namespacesAppended = false;  
  private boolean outputEnvelopeElement = true;
  private boolean isBodyBufferPreset = false;
  private boolean isBXML = false;
  private boolean isEnvelopeElementValid = true; //this flag indicates whether the message has been build from invalid soap xml (invalid soap:envelope root element)
  
  private String soapNS; //this variable contains the ns of the soap version which is in use (SOAP1.1 or SOAP1.2)
  
  /**
   * Default constructor.
   * @throws ParserConfigurationException 
   */
  public SOAPMessageImpl(){
    try{
      document = SharedDocumentBuilders.newDocument(); 
      headers = new SOAPHeaderList(document);
      bodyBuffer = new ReferenceByteArrayOutputStream(BUFFER_INITIAL_SIZE);
      this.serializer = new DOMSerializer();    
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      serializer.setOutputProperty(OutputKeys.INDENT, "no");
      this.messageWriter = XMLTokenWriterFactory.newInstance();
      additionalEnvNamespaces = new Hashtable();      
    }catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Returns constant denoting current state of the message.
   */
  public int getCurrentState() {
    return this.currentState;
  }
  /**
   * Presets internal buffer with <code>buffer</code>.
   */
  public void presetBodyBuffer(ByteArrayOutputStream buffer) {
    if (currentState != BEFORE_WRITE) {
      throw new IllegalStateException("Internal buffer preset is possible only in BEFORE_WRITE state.");    
    }
    this.bodyBuffer = buffer;
    isBodyBufferPreset = true;
  }
  
  public void presetBodyBuffer(byte[] arr) {
    if (currentState != BEFORE_WRITE) {
      throw new IllegalStateException("Internal buffer preset is possible only in BEFORE_WRITE state.");    
    }
    ReferenceByteArrayOutputStream rbArr = new ReferenceByteArrayOutputStream(0);
    rbArr.presetContent(arr);
    this.presetBodyBuffer(rbArr);
    commitWrite();    
  }
  /**
   * Outputs message xml declaration.
   * @param output
   */
  private void outputXMLDeclaration(OutputStream output) throws IOException {
    output.write(XMLDECLARATION_START);
    output.write(encoding.getBytes()); //$JL-I18N$
    output.write(XMLDECLARATION_END);
  }
  
  /**
   * Outputs message envelope tag.
   * @param output
   * @throws IOException
   */
  private void outputEnvelopeOpen(OutputStream output) throws IOException {
    StringBuilder stringBuffer = new StringBuilder(128);
    stringBuffer.append("<").append(ENVELOPETAG);
    //append the additional namespaces.
    Enumeration en = additionalEnvNamespaces.keys();
    while (en.hasMoreElements()) {
      String namespace = (String) en.nextElement();
      String prefix = (String) additionalEnvNamespaces.get(namespace);
      if (prefix.length()==0) {
        stringBuffer.append(" xmlns=\"").append(namespace).append("\"");
      } else {
        stringBuffer.append(" xmlns:").append(prefix).append("=\"").append(namespace).append("\"");
      }      
    }
    String result = stringBuffer.toString();
    // use Charset! - no need to create a string object for this
    output.write(result.getBytes()); //$JL-I18N$
    output.write(CLOSING_BRACKET);
  }

  /**
   * Outputs envelope close tag.
   * @param output
   * @throws IOException
   */
  private void outputEnvelopeClose(OutputStream output) throws IOException {
    output.write(ENDENVELOPE_TAG.getBytes()); //$JL-I18N$    
  }
  
  /**
   * Outputs Header open tag.
   * @param output
   * @throws IOException
   */
  private void outputHeaderOpenTag(OutputStream output) throws IOException {
    output.write(STARTHEADER_TAG.getBytes()); //$JL-I18N$     
  }
  
  /**
   * Outputs Header close tag.
   * @param output
   * @throws IOException
   */
  private void outputHeaderCloseTag(OutputStream output) throws IOException {
    output.write(ENDHEADER_TAG.getBytes()); //$JL-I18N$
  }
  
  /**
   * Returns enumeration of additional envelope namespaces.
   * @return
   */
  public Enumeration getEnvelopeNamespaces() {
    return additionalEnvNamespaces.keys();  
  }
  
  /**
   * Returns aditional envelope namespace from prefix.
   * @param prefix
   * @return
   */
  public String getEnvelopeNamespacePrefix(String namespace) {
    return (String) additionalEnvNamespaces.get(namespace);
  }
  /**
   * Returns unmodifiable Map containing envelop prefix declarations. Key uri(String), value prefix(String).
   */
  public java.util.Map getBuiltInEnvNamespaceMap() {
    return Collections.unmodifiableMap(builtInEnvNamespaces);    
  }
  /**
   * Adds additional namespace to the envelope.
   * @param prefix
   * @param namespace
   */
  public void addEnvelopeNamespace(String prefix, String namespace) {
    if (this.currentState != BEFORE_WRITE) {
      throw new IllegalStateException("Envelope namespaces can be sent only before message write.");
    }
    additionalEnvNamespaces.put(namespace,prefix);
  }
  
//  /**
//   * Initialize the default envelope namespaces.
//   *
//   */
//  private void initEnvlopeNS() {
//    envelopeNamespaces.clear();
//    envelopeNamespaces.put(SOAP_NS,SOAPENV_PREFIX);
//    envelopeNamespaces.put(NS.XSI,"xsi");
//    envelopeNamespaces.put(NS.XS,"xs");                     
//  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#getSOAPHeaders()
   */
  public SOAPHeaderList getSOAPHeaders() {
    return headers;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#getLength()
   */
  public long getBodyLength() {
    return bodyBuffer.size();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#getMessageMode()
   */
  public int getMessageMode() {    
    return currentState;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#initWriteMode()
   */
  public void initWriteMode(String soapNS) {
    this.substituteReader = null;
    this.encoding = UTF8;
    this.currentState = BEFORE_WRITE;
    this.headers.clear();    
    this.bodyBuffer.reset();
    checkSOAPNS(soapNS);
    this.soapNS = soapNS;
    initBuiltInEnvNamespaces(soapNS);
    try {
      //initEnvlopeNS();
      additionalEnvNamespaces.clear();
      additionalEnvNamespaces.putAll(this.builtInEnvNamespaces);
      namespacesAppended = false;     
      this.messageWriter.init(bodyBuffer,encoding, additionalEnvNamespaces);
    } catch (Exception e) {
      throw new RuntimeException("Error in creating output !");
    }    
  }
    
  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#initWriteMode()
   */
  public void initWriteMode(String encoding, String soapNS) {
    this.substituteReader = null;
    if (!UTF8.equalsIgnoreCase(encoding)) {
      throw new IllegalArgumentException("Only \"utf-8\" output encodinf is supported.");
    }
    this.encoding = encoding;
    this.currentState = BEFORE_WRITE;
    this.headers.clear();
    this.bodyBuffer.reset();
    checkSOAPNS(soapNS);
    this.soapNS = soapNS;
    initBuiltInEnvNamespaces(soapNS);
    try {
      //initEnvlopeNS();
      additionalEnvNamespaces.clear();
      additionalEnvNamespaces.putAll(this.builtInEnvNamespaces);
      namespacesAppended = false;
      this.messageWriter.init(bodyBuffer,encoding, additionalEnvNamespaces);
    } catch (Exception e) {
      throw new RuntimeException("Error in creating output !");
    }    
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#initReadMode(java.io.InputStream)
   */
  public void initReadMode(InputStream input, String soapNS) throws IOException {
    this.substituteReader = null;
    this.currentState = BEFORE_READ;
    this.headers.clear();
    checkSOAPNS(soapNS);
    this.soapNS = soapNS;
    this.bodyBuffer.reset();
    if (this.messageReader == null) {
      if (!isBXML) {
        this.messageReader = XMLTokenReaderFactory.getInstance().createReader(input);
      } else {
        this.messageReader = new XMLBinaryTokenReader(input);
      }
    } else {
      this.messageReader.init(input);
    }
    try {
      messageReader.begin();
      checkEnvelope(messageReader);
      extractHeaders(messageReader);
    } catch (ParserException e) {
      throw new NestedIOException("Problem in parser initialization.",e);
    }    
    
  }
    
  public void initReadMode(Reader reader, String soapNS) throws IOException {
    this.substituteReader = null;
    this.currentState = BEFORE_READ;
    this.headers.clear();
    if (!isBXML) {
      this.messageReader = XMLTokenReaderFactory.newInstance(reader);
    } else {
      throw new NestedIOException("BXML doesn't support Reader input");
      //this.messageReader = new XMLBinaryTokenReader(reader);
    }
    checkSOAPNS(soapNS);
    this.soapNS = soapNS;
    try {
      messageReader.begin();
    } catch (ParserException e) {
      throw new NestedIOException("Problem in parser initialization.",e);
    }        
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#initReadMode(com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader)
   */
  public void initReadMode(XMLTokenReader input, String soapNS) throws IOException {
    this.substituteReader = null;
    this.currentState = BEFORE_READ;
    this.headers.clear();
    this.messageReader = input; // Uses external reader 
    checkSOAPNS(soapNS);
    this.soapNS = soapNS;
    try {
      messageReader.begin();
      checkEnvelope(messageReader);
      extractHeaders(messageReader);      
    } catch (ParserException e) {
      throw new NestedIOException("Problem in parser initialization.",e);
    }    
  }

  public boolean isEnvelopeElementValid() {
    if (currentState == BEFORE_READ) {
      return isEnvelopeElementValid;
    }
    throw new IllegalStateException("Using this method is possible only in BEFORE_READ state.");    
  }

  /**
   * Extracts SOAPHeaders.
   * @param reader
   * @throws Exception
   * @throws ParserException
   */
  private void extractHeaders(XMLTokenReader reader) throws IOException, ParserException {
    int code = reader.moveToNextElementStart();
    if (code == XMLTokenReader.STARTELEMENT) {       
      if (reader.getLocalName().equals(HEADERTAG_NAME) && (reader.getURI().equals(soapNS))) {
        loadHeaders(reader);
        return; // Header are loaded and DOAP body is found
      }
      if (reader.getLocalName().equals(BODYTAG_NAME) && (reader.getURI().equals(soapNS))) {
        return;
      }      
    } else {
      throw new IOException("Can not find SOAPMessage Body tag.");
    }
  }
  
  /**
   * Parses SOAP Headers.
   * @param reader
   * @throws ParserException
   * @throws IOException
   */
  private void loadHeaders(XMLTokenReader reader) throws ParserException, IOException {
    // Reads namespaces declared in header tag
    Attributes attr = reader.getAttributes();
    int headerAttributes = attr.getLength();
    for (int i=0; i < headerAttributes; i++) {
      String attrNamespace = attr.getURI(i);
      if (NS.XMLNS.equals(attrNamespace)) {
        // This is namespace declaration
        String prefix = DOM.qnameToPrefix(attr.getQName(i));
        String namespace = attr.getValue(i);
        headers.addHeaderNamespace(namespace, prefix);
      }
    }
    int code;
    int currentLevel = reader.getCurrentLevel();
    while (true) {
      code = reader.next();
      switch(code) {
        case XMLTokenReader.STARTELEMENT: {
          Element headerEntry = reader.getDOMRepresentation(document);
          headers.addHeader(headerEntry);
          break;
        }
        case XMLTokenReader.ENDELEMENT: {
          if (reader.getCurrentLevel()<currentLevel) {
            code = reader.moveToNextElementStart();
            if (code == XMLTokenReader.STARTELEMENT) {
              if (reader.getURI().equals(soapNS) && reader.getLocalName().equals(BODYTAG_NAME)) {
                return;
              } else {
                throw new IOException("Incorrect message content. Body element not found.");
              }
            } else {
              throw new IOException("Unexpexted End of SOAP Message.");
            }
          } else {
            throw new IOException("Error in message structure. SOAP:Header end tag is expected.");
          }
        }
        case XMLTokenReader.EOF: {
          throw new IOException("Unexpexted message end.");
        }
      }
    }
    
  }    
  
  /**
   * Checks if the reader contains SOAP Envelope.
   * @param reader
   * @throws Exception
   */
  private void checkEnvelope(XMLTokenReader reader) throws ParserException, IOException {
    this.isEnvelopeElementValid = true;
    reader.moveToNextElementStart();
    if (reader.getState() == XMLTokenReader.STARTELEMENT && reader.getLocalName().equals(ENVELOPETAG_NAME) && reader.getURI().equals(soapNS)) {
      // Read the envelope prefixes
      this.additionalEnvNamespaces.clear();
      Map namespaces = reader.getNamespaceMappings();
      Iterator it = namespaces.keySet().iterator();
      while (it.hasNext()) {
        Object prefix = it.next();
        Object namespace = namespaces.get(prefix);
        this.additionalEnvNamespaces.put(namespace,prefix);
      }
      
      return;
    } else {
      this.isEnvelopeElementValid = false;
      if (reader.getState() == XMLTokenReader.STARTELEMENT) {        
        throw new IOException("The root SOAP Message element is ["+reader.getLocalName()+"]{"+reader.getURI()+"} but it should be {" + soapNS + "}:Envelope.");
      } else {
        throw new IOException("Message Envelope not found. Probably empty SOAP message.");
      }
    }
  }
  

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#getBodyReader()
   */
  public XMLTokenReader getBodyReader() {
    if (this.substituteReader != null) {
      return this.substituteReader;    
    } else {
      return this.messageReader;
    }    
  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#getBodyWriter()
   */
  public XMLTokenWriter getBodyWriter() {
    if (currentState != BEFORE_WRITE) {
      throw new IllegalStateException("Body writer getting is possible only in BEFORE_WRITE state.");    
    }   
    if (namespacesAppended == false) {
      messageWriter.appendNamespaces(additionalEnvNamespaces);
      namespacesAppended = true; 
    }
    return this.messageWriter;
  } 
  
  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#replaceBodyWriter()
   */
  public XMLTokenWriter replaceBodyWriter(XMLTokenWriter writer) throws IOException {
    if (currentState != BEFORE_WRITE) {
      throw new IllegalStateException("Body writer replace is possible only in BEFORE_WRITE state.");    
    }
    if (originalMessageWriter == null){
      originalMessageWriter = messageWriter;
    }
    Hashtable perm = new Hashtable();
    perm.putAll(additionalEnvNamespaces);
    writer.init(bodyBuffer,encoding,perm); // initialize the new writer
    namespacesAppended = false;
    
    //TODO bug in ats ws sec tests
    //additionalEnvNamespaces.clear();
    
    XMLTokenWriter res = this.messageWriter; // return the original body writer
    this.messageWriter = writer;    
    return res;
  } 

  /**
   * Sets a substitute body reader. The substitute reader can be set only once and should be intialized and positioned on
   * SOAP body.
   * @param reader
   * @return
   * @throws IOException
   */
  public XMLTokenReader replaceBodyReader(XMLTokenReader reader) throws IOException {
//    if (currentState != BEFORE_READ) {
//      throw new IllegalStateException("Body reader replace is possible only in BEFORE_READ state.");    
//    }
    this.substituteReader = reader;
    return this.messageReader;
  }
   

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#getEnvelopeReader()
   */
  public void getEnvelopeReader() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#writeTo(java.io.OutputStream)
   */
  public void writeTo(OutputStream output) throws IOException {
    if (isBodyBufferPreset) { //if buffer is preset, send its content
      bodyBuffer.writeTo(output);
      return;
    }
    outputXMLDeclaration(output);
    if (outputEnvelopeElement) {
      outputEnvelopeOpen(output);    
      // Output SOAP Headers
      if (this.headers.size()> 0) {
        outputHeaderOpenTag(output);
        Element[] headerElements = headers.getHeaders();        
        for (int i = 0; i < headerElements.length; i++) {
          try {
            serializer.write(headerElements[i], output);
          } catch (Exception e) {
            throw new NestedIOException("Unable to serialize SOAPHeader content into SOAPMessage.",e);          
          }
        }
        outputHeaderCloseTag(output);      
      }
    }
    // Output SOAP body
    messageWriter.flush();
    bodyBuffer.flush();    
    bodyBuffer.writeTo(output);
    if (outputEnvelopeElement) {
      outputEnvelopeClose(output);    
    }
    output.flush();        
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#commit()
   */
  public void commitRead() {
    this.currentState = AFTER_READ;    
  }
  
  public void commitWrite() {    
    this.currentState = AFTER_WRITE;
  }
  
  public ByteArrayOutputStream getInternalWriterBuffer() {
    if (currentState != AFTER_WRITE) {
      throw new IllegalStateException("Internal buffer access is possible only in AFTER_WRITE state.");    
    }
    return this.bodyBuffer;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.espbase.messaging.SOAPMessage#clear()
   */
  public void clear() {    
    headers.clear();
    if (isBodyBufferPreset) {
      isBodyBufferPreset = false;
      bodyBuffer = new ReferenceByteArrayOutputStream(BUFFER_INITIAL_SIZE); //do not use preset variable      
    } else {
      //shrink the byte[] not to use too much memory
      if (bodyBuffer.size() > BUFFER_INITIAL_SIZE) {
        bodyBuffer = null; //release the previous content to GC
        bodyBuffer = new ReferenceByteArrayOutputStream(BUFFER_INITIAL_SIZE);      
      } else {
        bodyBuffer.reset();
      }    
    }
    additionalEnvNamespaces.clear();
    currentState = UNDEFINED;
    headers.clear();
    String encoding = UTF8;
    XMLTokenReader substituteReader = null;
    if (originalMessageWriter != null){
      messageWriter = originalMessageWriter;
    }
    
    namespacesAppended = false;
    outputEnvelopeElement = true;  
    isEnvelopeElementValid = true;
    this.soapNS = null;
    this.builtInEnvNamespaces.clear();
  }
  /**
   * Returns internal flag value, for outputing by default the <Envelope> element,
   * when this message is serialized by <code>writeTo</code> method.
   */
  public boolean isOutputEnvelopeElement() {
    return outputEnvelopeElement;
  }
  /**
   * Sets whether to output or not <Envelope> element when this message is serialized by <code>writeTo</code> method.
   */
  public void setOutputEnvelopeElement(boolean b) {
    if (currentState != UNDEFINED) {
      throw new IllegalStateException();
    }
    outputEnvelopeElement = b;
  }
  
  
  public String getSOAPVersionNS() {
    return this.soapNS;
  }
  
  private void initBuiltInEnvNamespaces(String soapNS) {
    if (outputEnvelopeElement) { //if this message will be treated as an SOAPMessage
      this.builtInEnvNamespaces.clear();
      builtInEnvNamespaces.put(soapNS, SOAPENV_PREFIX);
      builtInEnvNamespaces.put(NS.XSI, "xsi");
      builtInEnvNamespaces.put(NS.XS, "xs");
    }
  }
  
  private void checkSOAPNS(String sNS) {
    if (! SOAP11_NS.equals(sNS) && ! SOAP12_NS.equals(sNS)) {
      throw new IllegalArgumentException("Unexpected soap namespace found '" + sNS + "'. Valid ones are '" + SOAP11_NS + "', '" + SOAP12_NS + "'");
    }
  }

  public void setBinaryXML(boolean value) {
    isBXML = value;
  }
}
