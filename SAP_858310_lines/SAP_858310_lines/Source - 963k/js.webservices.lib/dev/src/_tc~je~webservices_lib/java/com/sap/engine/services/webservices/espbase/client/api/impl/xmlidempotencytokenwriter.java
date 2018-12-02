package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.UUID;

import javax.xml.namespace.QName;
import com.sap.engine.lib.xml.parser.tokenizer.AttributeHandler;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;

public class XMLIdempotencyTokenWriter implements XMLTokenWriter {
  
  private static final String SOAP_BODY_ELEM_NAME = "Body";
  
  private static final QName MESSAGE_HEADER_ELEM_NAME = new QName("", "MessageHeader");
  private static final QName UUID_ELEM_NAME = new QName("", "UUID");
  private static final QName ID_ELEM_NAME = new QName("", "ID");
  
  private static final int INITIAL_ELEM_ID = 0;
  private static final int OPERATION_NAME_ELEM_ID = 2;
  private static final int MESSAGE_HEADER_ELEM_ID = 3;
  
  private XMLTokenWriter wrappedXMLTokenWriter;
  private String soapBodyElemNS;
  private int elementsCounter;
  private String uuid;
  private boolean uuidExists;
  
  public XMLIdempotencyTokenWriter() {
    reset();
  }
  
  public void setUUID(String uuid) {
    this.uuid = uuid;
  }
  
  public String getUUID() {
    return(uuid);
  }
  
  public void setSoapBodyElementNS(String soapBodyElemNS) {
    this.soapBodyElemNS = soapBodyElemNS;
  }
  
  public String getSoapBodyElementNS() {
    return(soapBodyElemNS);
  }
  
  public void setWriter(XMLTokenWriter wrappedXMLTokenWriter) {
    this.wrappedXMLTokenWriter = wrappedXMLTokenWriter;
  }
  
  public XMLTokenWriter getWriter() {
    return(wrappedXMLTokenWriter);
  }
  
  public void reset() {
    wrappedXMLTokenWriter = null;
    soapBodyElemNS = null;
    elementsCounter = INITIAL_ELEM_ID;
    uuid = null;
    uuidExists = false;
  }
  
  public void init(OutputStream output) throws IOException {
    wrappedXMLTokenWriter.init(output);
  }
  
  public void init(OutputStream output,String encoding) throws IOException {
    wrappedXMLTokenWriter.init(output, encoding);
  }
  
  public void init(OutputStream output, String encoding, Hashtable defaultPrefixes) throws IOException {
    wrappedXMLTokenWriter.init(output, encoding, defaultPrefixes);
  }
  
  public void init(OutputStream output, Hashtable defaultPrefixes) throws IOException {
    wrappedXMLTokenWriter.init(output, defaultPrefixes);
  }
  
  public void appendNamespaces(Hashtable hash) {
    wrappedXMLTokenWriter.appendNamespaces(hash);
  }
  
  public void enter(String namespace, String localName) throws IOException {
    if(!uuidExists) {
      switch(elementsCounter) {
        case(OPERATION_NAME_ELEM_ID) : {
          if(!compareNames(MESSAGE_HEADER_ELEM_NAME, namespace, localName)) {
            enterMessageHeader();
          }
          break;
        }
        case(MESSAGE_HEADER_ELEM_ID) : {
          if(compareNames(UUID_ELEM_NAME, namespace, localName)) {
            uuidExists = true;
          } else if(!compareNames(ID_ELEM_NAME, namespace, localName)) {
            enterUUID();
          }
        }
      }
      if(elementsCounter != INITIAL_ELEM_ID || compareNamespaces(soapBodyElemNS, namespace) && SOAP_BODY_ELEM_NAME.equals(localName)) {
        elementsCounter++;
      }
    }
    wrappedXMLTokenWriter.enter(namespace, localName);
  }
  
  private boolean compareNames(QName name, String namespace, String localName) {
    return(compareNamespaces(name.getNamespaceURI(), namespace) && name.getLocalPart().equals(localName));
  }
  
  private boolean compareNamespaces(String namespace1, String namespace2) {
    if(namespace1 == null || namespace1.equals("")) {
      return(namespace2 == null || namespace2.equals(""));
    }
    return(namespace1.equals(namespace2));
  }
  
  private void enterMessageHeader() throws IOException {
    wrappedXMLTokenWriter.enter(MESSAGE_HEADER_ELEM_NAME.getNamespaceURI(), MESSAGE_HEADER_ELEM_NAME.getLocalPart());
    enterUUID();
    wrappedXMLTokenWriter.leave();
  }
  
  private void enterUUID() throws IOException {
    wrappedXMLTokenWriter.enter(UUID_ELEM_NAME.getNamespaceURI(), UUID_ELEM_NAME.getLocalPart());
    wrappedXMLTokenWriter.writeContent(uuid);
    uuidExists = true;
    wrappedXMLTokenWriter.leave();
  }

  public void leave() throws IOException, IllegalStateException {
    if(!uuidExists) {
      switch(elementsCounter) {
        case(OPERATION_NAME_ELEM_ID) : {
          enterMessageHeader();
          break;
        }
        case(MESSAGE_HEADER_ELEM_ID) : {
          enterUUID();
          break;
        }
      }
      if(elementsCounter != INITIAL_ELEM_ID) {
        elementsCounter--;
      }
    }
    wrappedXMLTokenWriter.leave();
  }

  public void flush() throws IOException {
    wrappedXMLTokenWriter.flush();
  }

  public String getPrefixForNamespace(String namespace) throws IOException, IllegalStateException {
    return(wrappedXMLTokenWriter.getPrefixForNamespace(namespace));
  }

  public void setPrefixForNamespace(String prefix, String namespace) throws IOException, IllegalStateException {
    wrappedXMLTokenWriter.setPrefixForNamespace(prefix, namespace);
  }

  public void writeAttribute(String namespace, String name, String value) throws IOException, IllegalStateException {
    wrappedXMLTokenWriter.writeAttribute(namespace, name, value);
  }

  public void writeContent(String content) throws IOException {
    wrappedXMLTokenWriter.writeContent(content);
  }

  public void writeContentCData(char[] chars) throws IOException {
    wrappedXMLTokenWriter.writeContentCData(chars);
  }

  public void writeContentCData(char[] chars, int offset, int count) throws IOException {
    wrappedXMLTokenWriter.writeContentCData(chars, offset, count);
  }
  
  public void writeContentCDataDirect(char[] chars) throws IOException {
    wrappedXMLTokenWriter.writeContentCDataDirect(chars);
  }

  public void writeContentCDataDirect(char[] chars, int offset, int count) throws IOException {
    wrappedXMLTokenWriter.writeContentCDataDirect(chars, offset, count);
  }

  public void writeComment(String comment) throws IOException {
    wrappedXMLTokenWriter.writeComment(comment);
  }

  public void writeXmlAttribute(String name, String value) throws java.io.IOException, java.lang.IllegalStateException {
    wrappedXMLTokenWriter.writeXmlAttribute(name, value);
  }
  
  public void setAttributeHandler(AttributeHandler handler) {
    wrappedXMLTokenWriter.setAttributeHandler(handler);
  }
  
  public void close() throws IOException {
    wrappedXMLTokenWriter.close();
  }
  
  public void writeInitial() throws IOException {
    wrappedXMLTokenWriter.writeInitial();
  }

  public void setProperty(String key, Object value) {
	   // Not used
  }
}
