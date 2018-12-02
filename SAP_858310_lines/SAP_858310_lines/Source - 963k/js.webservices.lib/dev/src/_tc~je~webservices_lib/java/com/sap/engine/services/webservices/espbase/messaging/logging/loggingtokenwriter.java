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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import com.sap.engine.lib.xml.parser.tokenizer.AttributeHandler;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriterFactory;
import com.sap.engine.lib.xml.util.NestedIOException;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.SOAPMessageImpl;


/**
 * @author Angel Tcholtchev
 * Wraps the bodyWriter and logs the outputed xml content.
 * 
 */
public class LoggingTokenWriter extends AbstractLoggingTokenizer implements XMLTokenWriter {
      
  private XMLTokenWriter wrapped;
  
  private XMLTokenWriter writer; 
    
  /**
   * 
   * @param message the logged mime message.
   * @param ns know namespaces hash.
   * @param modifyValue hide the business values?
   * @throws IOException
   */
  public LoggingTokenWriter(MIMEMessage message, Hashtable ns, boolean modifyValue) throws IOException {
    this.predefinedNS = ns;
    
    this.writer = XMLTokenWriterFactory.getInstance().createWriter();
    
    this.message = message;
    
    this.modifyValue = modifyValue;
    
    this.wrapped = message.getBodyWriter();
    
    this.bodyBuffer = new ByteArrayOutputStream();        
       
    this.writer.init(bodyBuffer, ns);
  }   
  
  /**
   * @see XMLTokenWriter.init()
   */
  public void init(OutputStream outputStream) throws IOException {
    wrapped.init(outputStream);
  }

  /**
   * @see XMLTokenWriter.init()
   */
  public void init(OutputStream outputStream, String string) throws IOException {
    wrapped.init(outputStream, string);
  }

  /**
   * @see XMLTokenWriter.init()
   */
  public void init(OutputStream outputStream, String string, Hashtable hashtable) throws IOException {
    wrapped.init(outputStream, string, hashtable);
  }

  /**
   * @see XMLTokenWriter.init()
   */
  public void init(OutputStream outputStream, Hashtable hashtable) throws IOException {
    wrapped.init(outputStream, hashtable);
  }

  /**
   * @see XMLTokenWriter.appendNamespaces()
   */
  public void appendNamespaces(Hashtable hashtable) {
    wrapped.appendNamespaces(hashtable);
    writer.appendNamespaces(hashtable);
  }

  /**
   * @see XMLTokenWriter.enter()
   */
  public void enter(String namespace, String localName) throws IOException {
    wrapped.enter(namespace, localName);
    writer.enter(namespace, localName);
  }

  /**
   * @see XMLTokenWriter.leave()
   */  
  public void leave() throws IOException, IllegalStateException {
    wrapped.leave();
    writer.leave();
  }

  /**
   * @see XMLTokenWriter.flush()
   */
  public void flush() throws IOException {    
    wrapped.flush();                                
    writer.flush();
    bodyBuffer.flush();              
  }

  /**
   * @see XMLTokenWriter.getPrefixForNamespace()
   */
  public String getPrefixForNamespace(String string) throws IOException, IllegalStateException {
    return wrapped.getPrefixForNamespace(string);
  }

  /**
   * @see XMLTokenWriter.setPrefixForNamespace()
   */
  public void setPrefixForNamespace(String string, String string1) throws IOException, IllegalStateException {
    wrapped.setPrefixForNamespace(string, string1);
    writer.setPrefixForNamespace(string, string1);
  }

  /**
   * @see XMLTokenWriter.writeAttribute()
   */
  public void writeAttribute(String namespace, String name, String value) throws IOException, IllegalStateException {
    wrapped.writeAttribute(namespace, name, value);
    
    //Log
    String modifiedValue = (String)LoggingTokenUtil.modifyValue(value, this.modifyValue);
    writer.writeAttribute(namespace, name, modifiedValue);
  }

  /**
   * @see XMLTokenWriter.writeContent()
   */
  public void writeContent(String content) throws IOException {
    wrapped.writeContent(content);
    
    //Log
    String modifiedValue = (String)LoggingTokenUtil.modifyValue(content, this.modifyValue);
    writer.writeContent(modifiedValue);
  }

  /**
   * @see XMLTokenWriter.writeContentCData()
   */
  public void writeContentCData(char[] chars) throws IOException {
    wrapped.writeContentCData(chars);
    
    //Log
    char[] modifiedChars = (char[])LoggingTokenUtil.modifyValue(chars, this.modifyValue);
    writer.writeContentCData(modifiedChars);
  }

  /**
   * @see XMLTokenWriter.writeContentCData()
   */
  public void writeContentCData(char[] chars, int i, int i1) throws IOException {
    wrapped.writeContentCData(chars, i, i1);
    
    //Log
    char[] modifiedChars = (char[])LoggingTokenUtil.modifyValue(chars, this.modifyValue);
    writer.writeContentCData(modifiedChars, i, i1);
  }

  /**
   * @see XMLTokenWriter.writeContentCDataDirect()
   */
  public void writeContentCDataDirect(char[] chars) throws IOException {
    wrapped.writeContentCDataDirect(chars);
    
    //Log
    char[] modifiedChars = (char[])LoggingTokenUtil.modifyValue(chars, this.modifyValue);
    writer.writeContentCDataDirect(modifiedChars);
  }

  /**
   * @see XMLTokenWriter.writeContentCDataDirect()
   */
  public void writeContentCDataDirect(char[] chars, int i, int i1) throws IOException {
    wrapped.writeContentCDataDirect(chars, i, i1);
    
    //Log
    char[] modifiedChars = (char[])LoggingTokenUtil.modifyValue(chars, this.modifyValue);
    writer.writeContentCDataDirect(modifiedChars, i, i1);
  }

  /**
   * @see XMLTokenWriter.writeComment()
   */
  public void writeComment(String string) throws IOException {
    wrapped.writeComment(string);
    //TODO: modify content?
    writer.writeComment(string);
  }

  /**
   * @see XMLTokenWriter.writeXmlAttribute()
   */
  public void writeXmlAttribute(String name, String value) throws IOException, IllegalStateException {
    wrapped.writeXmlAttribute(name, value);
    
    //Log
    String modifiedValue = (String) LoggingTokenUtil.modifyValue(value, this.modifyValue);
    writer.writeXmlAttribute(name, modifiedValue);
  }

  /**
   * @see XMLTokenWriter.setAttributeHandler()
   */
  public void setAttributeHandler(AttributeHandler attributeHandler) {    
    wrapped.setAttributeHandler(attributeHandler);
    //TODO: Do i need to wrapp this to get its events.
    writer.setAttributeHandler(attributeHandler);
  }

  /**
   * @see XMLTokenWriter.close()
   */
  public void close() throws IOException {
    wrapped.close();
    writer.close();
  }

  /**
   * @see XMLTokenWriter.writeInitial()
   */
  public void writeInitial() throws IOException {
    wrapped.writeInitial();
    writer.writeInitial();
  }

  /**
   * @see XMLTokenWriter.setProperty()
   */
  public void setProperty(String key, Object value) {
    wrapped.setProperty(key, value);
  }
  
 
  public void dump(OutputStream os) throws IOException {
    if (os != null){     
      os.write(bodyBuffer.toByteArray());
    }    
  }


  public void dumpPayload(OutputStream output) throws IOException {   
    // Dump the envelope
    if (bodyBuffer.size() != 0) {
      // Normal case. The outbound message has gone through all token writer events.
      dumpEnvelope(output);
    } else {
      // JAXWS Handler Case. The envelope is serialized in SAAJ message and is preset.
      logJaxWSOutboundMessage(message, output);
    }

    dumpAttachments(output);
  }
       
  
  private void logJaxWSOutboundMessage(MIMEMessage message, OutputStream output) throws IOException {
    if (output != null) {
      try {        
        message.writeTo(bodyBuffer);

        byte[] envelopeContent = bodyBuffer.toByteArray();
        
/*        // A hack to get the message and preset it.
        if (((SOAPMessageImpl) message).getMessageMode() != SOAPMessage.BEFORE_WRITE){
          message.initWriteMode(message.getSOAPVersionNS());
          ((SOAPMessageImpl) message).presetBodyBuffer(envelopeContent);
          //return to the state where it was found.
          message.commitWrite();
        }else{
          // Just preset the buffer if the state is BEFORE_WRITE
          ((SOAPMessageImpl) message).presetBodyBuffer(envelopeContent);
        }
*/
        
        ByteArrayInputStream readyEnvelopeStream = new ByteArrayInputStream(envelopeContent);
        XMLTokenReader tokenReader = (XMLTokenReader) XMLTokenReaderFactory.getInstance().createReader(readyEnvelopeStream);        
        tokenReader.begin();
       

        LoggingTokenReader loggingReader = new LoggingTokenReader(tokenReader, (MIMEMessage) message, predefinedNS, modifyValue,LoggingTokenReader.ELEMENT_PROCESSING);

        while (loggingReader.getState() != XMLTokenReader.EOF) {
          loggingReader.next();
        }

        loggingReader.dump(output);
      } catch (Exception e) {
        throw new NestedIOException("Unable to log a outbound message that went throught the jaxws handler.", e);
      }
    }
  }
  
}