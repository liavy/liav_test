/*
 * Copyright (c) 2006 by SAP Labs Bulgaria.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.espbase.messaging.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.sap.engine.lib.xml.parser.Features;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.helpers.CharArray;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderUtil;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriterFactory;
import com.sap.engine.lib.xsl.xslt.InternalAttributeList;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;

/**
 * 
 * @author Angel Tcholtchev
 * Wraps the normal bodyReader of a message and
 * dumps the request on the log writer. 
 */
public class LoggingTokenReader extends AbstractLoggingTokenizer implements XMLTokenReader{
  
  public static final String ENVELOPE_PROCESSING = "process_envelope";
  
  public static final String ELEMENT_PROCESSING = "process_element";
  
  private boolean trimWhitespaces = false;
    
  private XMLTokenReader wrapped;
  private XMLTokenWriter writer = null;
  
  /** XMLTree depth of the start element */
  private int startDepth;

  /** Indicates that the log xml is well formed. End element of the start element is reached. */
  private boolean finished = false;
  
  private String use;

  /**
   * 
   * @param wrapped wrapped XMLBodyReader
   * @param message the dumped message.
   * @param ns predefined ns hash
   * @param modifyValue hide business values?
   * @throws IOException
   */
  public LoggingTokenReader(XMLTokenReader wrapped, MIMEMessage message, Hashtable ns, boolean modifyValue, String use) throws IOException {
    this.writer = XMLTokenWriterFactory.getInstance().createWriter();
    this.wrapped = wrapped;
    this.message = message;
    this.predefinedNS = ns;

    this.use = use;
    
    this.modifyValue = modifyValue;

    this.bodyBuffer = new ByteArrayOutputStream();

    writer.init(bodyBuffer, ns);

    logStartElement();
  }

  /**
   * @see XMLTokenReader.begin();
   */
  public int begin() throws ParserException {
    return wrapped.begin();
  }


  /**
   * @see XMLTokenReader.end();
   */
  public void end() throws ParserException {
    wrapped.end();

    if (!finished) {
      try {
        finish();
      } catch (IOException ex) {
        throw new ParserException(ex);
      }
    }
  }

  /**
   * @see XMLTokenReader.init();
   */
  public void init(InputStream inputStream) {
    wrapped.init(inputStream);
  }

  /**
   * @see XMLTokenReader.next();
   * 
   * Intercepts the next event of the reader and outputs to
   * the dump writer.
   */
  public int next() throws ParserException {
    int result = wrapped.next();

    if (wrapped.getCurrentLevel() < startDepth) {
      return result;
    }

    try {
      switch (result) {
      case XMLTokenReader.STARTELEMENT: {
        String localName = getLocalName();
        String namespace = getURI();

        checkForFault(localName, namespace);

        writer.enter(namespace, localName);
        Attributes atr = getAttributes();

        int length = atr.getLength();

        for (int j = 0; j < length; j++) {
          String qname = atr.getQName(j);
          if (qname.startsWith("xmlns:")) {
            writer.setPrefixForNamespace(qname.substring(6), atr.getValue(j));
          } else {
            if (qname.equals("xmlns")) {
              writer.writeAttribute(null, qname, atr.getValue(j));
            } else {
              writer.writeAttribute(atr.getURI(j), atr.getLocalName(j), (String) LoggingTokenUtil.modifyValue(atr.getValue(j),
                  this.modifyValue));
            }
          }
        }
        break;
      }
      case XMLTokenReader.CHARS: {
        writer.writeContent((String) LoggingTokenUtil.modifyValue(getValue(), this.modifyValue));
        break;
      }
      case XMLTokenReader.ENDELEMENT: {
        writer.leave();

        if (wrapped.getCurrentLevel() == startDepth) {
          finish();
        }
        break;
      }
      case XMLTokenReader.COMMENT: {
        writer.writeComment(getValue());
        break;
      }

      }
    } catch (IOException e) {
      throw new ParserException(e);
    }

    return result;
  }

  private void finish() throws IOException {
    writer.flush();
    finished = true;
  }

  /**
   * @see XMLTokenReader.getPrefixMapping();
   */
  public String getPrefixMapping(String string) {
    return wrapped.getPrefixMapping(string);
  }

  /**
   * @see XMLTokenReader.getValue();
   */
  public String getValue() {
    return wrapped.getValue();
  }

  /**
   * @see XMLTokenReader.getAttributes();
   */
  public Attributes getAttributes() {
    return wrapped.getAttributes();
  }

  /**
   * @see XMLTokenReader.getInternalAttributeList();
   */
  public InternalAttributeList getInternalAttributeList() {
    return wrapped.getInternalAttributeList();
  }

  /**
   * @see XMLTokenReader.getURI();
   */
  public String getURI() {
    return wrapped.getURI();
  }

  /**
   * @see XMLTokenReader.getLocalName();
   */
  public String getLocalName() {
    return wrapped.getLocalName();
  }

  /**
   * @see XMLTokenReader.getQName();
   */
  public String getQName() {
    return wrapped.getQName();
  }

  /**
   * @see XMLTokenReader.getURICharArray();
   */
  public CharArray getURICharArray() {
    return wrapped.getURICharArray();
  }

  /**
   * @see XMLTokenReader.getLocalNameCharArray();
   */
  public CharArray getLocalNameCharArray() {
    return wrapped.getLocalNameCharArray();
  }

  /**
   * @see XMLTokenReader.getQNameCharArray();
   */
  public CharArray getQNameCharArray() {
    return wrapped.getQNameCharArray();
  }

  /**
   * @see XMLTokenReader.getValueCharArray();
   */
  public CharArray getValueCharArray() {
    return wrapped.getValueCharArray();
  }

  /**
   * @see XMLTokenReader.getState();
   */
  public int getState() {
    return wrapped.getState();
  }

  /**
   * @see XMLTokenReader.isWhitespace();
   */
  public boolean isWhitespace() {
    return wrapped.isWhitespace();
  }

  /**
   * Returns dom representation of current element.
   */
  public Element getDOMRepresentation(Element element) throws ParserException {
    return XMLTokenReaderUtil.getDOMRepresentation(this, element);
  }

  /**
   * @see XMLTokenReader.getDOMRepresentation();
   */
  public Element getDOMRepresentation(Document document) throws ParserException {
    if (this.getState() != XMLTokenReader.STARTELEMENT) {
      return null;
    }
    Element element = document.createElementNS(this.getURI(), this.getQName());
    return getDOMRepresentation(element);
  }

  /**
   * @see XMLTokenReader.moveToNextElementStart();
   */
  public int moveToNextElementStart() throws ParserException {
    int code;

    while (true) {
      code = this.next();
      if (code == XMLTokenReader.STARTELEMENT) {
        return XMLTokenReader.STARTELEMENT;
      } else if (code == XMLTokenReader.EOF) {
        return XMLTokenReader.EOF;
      }
    }
  }

 
  /**
   * @see XMLTokenReader.passChars();
   */
  public void passChars() throws ParserException {
    while (wrapped.getState() == XMLTokenReader.CHARS) {
      next();
    }
  }

  /**
   * @see XMLTokenReader.getValuePassCharsCA();
   */
  public CharArray getValuePassCharsCA() throws ParserException {    
    StringBuilder stringBuilder = new StringBuilder();
    
    while (wrapped.getState() == XMLTokenReader.CHARS) {
      stringBuilder.append(getValue());
      next();
    }
    
    CharArray chars = new CharArray(stringBuilder.toString());
    
    if (trimWhitespaces){
      chars.trim();
    }
                
    return chars;
  }

  /**
   * @see XMLTokenReader.getValuePassChars();
   */
  public String getValuePassChars() throws ParserException {
    StringBuilder stringBuilder = new StringBuilder();

    while (wrapped.getState() == XMLTokenReader.CHARS) {
      stringBuilder.append(getValue());
      next();
    }

    CharArray chars = new CharArray(stringBuilder.toString());

    if (trimWhitespaces) {
      chars.trim();
    }

    return chars.getStringFast();
  }

  /**
   * @see XMLTokenReader.getParentElement();
   */
  public String getParentElement() {
    return wrapped.getParentElement();
  }

  /**
   * @see XMLTokenReader.getCurrentLevel();
   */
  public int getCurrentLevel() {
    return wrapped.getCurrentLevel();
  }

  /**
   * @see XMLTokenReader.setEntityResolver();
   */
  public void setEntityResolver(EntityResolver entityResolver) throws ParserException {
    wrapped.setEntityResolver(entityResolver);
  }

  /**
   * @see XMLTokenReader.writeTo();
   */
  public void writeTo(Writer writer) throws ParserException, IOException {
    XMLTokenReaderUtil.copyReader2Writer(this, writer);
  }

  /**
   * @see XMLTokenReader.getNamespaceMappings();
   */
  public Map<String, String> getNamespaceMappings() {
    return wrapped.getNamespaceMappings();
  }

  /**
   * @see XMLTokenReader.getPrefixesOnLastStartElement();
   */
  public List<String> getPrefixesOnLastStartElement() {
    return wrapped.getPrefixesOnLastStartElement();
  }

  /**
   * @see XMLTokenReader.getEndedPrefixMappings();
   */
  public List<String[]> getEndedPrefixMappings() {
    return wrapped.getEndedPrefixMappings();
  }


  /**
   * @see XMLTokenReader.init();
   */
  public void init(InputSource input) {
    wrapped.init(input);
  }

  /**
   * @see XMLTokenReader.init();
   */
  public void init(Reader reader) {
    wrapped.init(reader);
  }

  /**
   * @see XMLTokenReader.setProperty();
   */
  public void setProperty(String key, Object value) {            
    wrapped.setProperty(key, value);
        
    if (key.equals(Features.FEATURE_TRIM_WHITESPACES)) {
      this.trimWhitespaces = ((Boolean)value).booleanValue();
    }    
  }
  
 
 

  /**
   * Log the current start element
   * @throws IllegalStateException
   * @throws IOException
   */
  private void logStartElement() throws IllegalStateException, IOException {
    if (wrapped.getState() != XMLTokenReader.STARTELEMENT) {
      return;
    }

    int currentLevel = wrapped.getCurrentLevel();
    if (currentLevel == 1){     
      this.startDepth = currentLevel;
    }else{
      this.startDepth = currentLevel-1;
    }

    String localName = getLocalName();
    String namespace = getURI();

    writer.enter(namespace, localName);
    Attributes atr = getAttributes();

    int length = atr.getLength();

    for (int j = 0; j < length; j++) {
      String qname = atr.getQName(j);
      if (qname.startsWith("xmlns:")) {
        writer.setPrefixForNamespace(qname.substring(6), atr.getValue(j));
      } else {
        if (qname.equals("xmlns")) {
          writer.writeAttribute(null, qname, atr.getValue(j));
        } else {
          writer.writeAttribute(atr.getURI(j), atr.getLocalName(j), (String) LoggingTokenUtil.modifyValue(atr.getValue(j),
              this.modifyValue));
        }
      }
    }
  }

  /**
   * If the processed payload is Soap:Fault - do not modify the value.
   * @param localName
   * @param namespace
   */
  private void checkForFault(String localName, String namespace) {
    //This method is important only when soap envelope is processed.
    if (ENVELOPE_PROCESSING.equals(this.use)) {
      String soapVersionNs = this.message.getSOAPVersionNS();

      // <SOAP-ENV:Fault>
      // Checks the element is soap fault which is child of soap:body element
      if (SOAPMessage.FAULTTAG_NAME.equalsIgnoreCase(localName) && soapVersionNs.equalsIgnoreCase(namespace)
          && wrapped.getCurrentLevel() == 3) {
        this.modifyValue = false;
      }
    }
  }
  

  /**
   * @see AbstractLoggingTokenizer.dump
   */ 
  public void dump(OutputStream os) throws IOException {
    if (!finished) {
      throw new IllegalStateException("Dump can only be performed after the message is read.");
    }
    
    if (os != null){           
      bodyBuffer.writeTo(os);
    }
  }


  /**
   * @see AbstractLoggingTokenizer.dumpPayload() 
   */
  public void dumpPayload(OutputStream output) throws IOException {
    if (!ENVELOPE_PROCESSING.equals(use)){
      throw new IllegalStateException("The whole inbound payload can only be dumped when soap envelope is processed. See class uses.");
    }
        
    if (!finished) {
      throw new IllegalStateException("Dump can only be performed after the message is read.");
    }        
    
    dumpEnvelope(output);
    
    dumpAttachments(output);    
  }
  
  
  

}
