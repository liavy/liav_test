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
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding;

//import com.sap.engine.lib.xml.parser.ActiveXMLParser;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.XMLParserConstants;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriterFactory;
import com.sap.engine.lib.xml.util.DOMSerializer;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.jaxrpc.util.DocumentPool;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.AbstractMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

import javax.xml.transform.OutputKeys;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ClientSOAPMessage extends AbstractMessage {
//public class fields
  public static final int DESERIALIZATION_MODE  =  1;
  public static final int SERIALIZATION_MODE  =  2;
  public static final String SOAPENV_PREFIX  =  "SOAP-ENV";

//package class fields
  private static final int BUFFER_SIZE  =  256;

  private static final String BODYTAG  =  "Body";
  private static final String HEADERTAG  =  "Header";
  private static final String ENVELOPETAG = "Envelope";

  private static final String STARTENVELOPE_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><" + SOAPENV_PREFIX + ":Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">";
  private static final String ENDENVELOPE_TAG   = "</" + SOAPENV_PREFIX + ":Envelope>";

  private static final String STARTHEADER_TAG   = "<" + SOAPENV_PREFIX + ":Header>";
  private static final String ENDHEADER_TAG     = "</" + SOAPENV_PREFIX + ":Header>";

  //private instance members
  private ArrayList headers; //Element objects
  private Element soapBody;  //SOAPBody element for the SOAPMessage
  private XMLTokenReader reader;  //Used in deserialization
  private XMLTokenWriter writer;  //Used in seralization
  private ByteArrayOutputStream bodyByteArrayBuffer;  //
  private boolean isBodyDOMbuilt = false;  //
  private int messageMode;
  private boolean emptyBody = false;
  private Document document = null;
  private DocumentBuilder builder = null;
  private DOMSerializer serializer = null;

  public ClientSOAPMessage() {
    this.writer = XMLTokenWriterFactory.newInstance();
    this.headers = new ArrayList();
    this.isBodyDOMbuilt = false;
    this.messageMode = 0;
    this.serializer = new DOMSerializer();
    serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    serializer.setOutputProperty(OutputKeys.INDENT, "no");
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setValidating(false);
//    dbf.setAttribute("http://sap.com/xml/soap-input","true");
    dbf.setIgnoringComments(true);
    try {
      this.builder = dbf.newDocumentBuilder();
      this.document = builder.newDocument();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException("Unable to create sap dom parser instance !", e);
    }
  }

  /**
   * Adds empty body to the message. Use only when in serialization mode and nothing is added already.
   * @throws Exception
   */
  public void addEmptyBody() throws Exception {
    this.writer.enter("http://schemas.xmlsoap.org/soap/envelope/","Body");
    this.writer.leave();
    this.emptyBody = true;
  }

  /**
   * Returns true if empty body is added.
   * @return
   */
  public boolean isEmptyBody() {
    return this.emptyBody;
  }

  /**
   * Returns body content size.
   * @return
   */
  public int getLength() {
    return bodyByteArrayBuffer.size();
  }

  /**
   * Initiaze the instance for seralization.
   */
  public XMLTokenWriter initSerializationMode() {
    this.messageMode = SERIALIZATION_MODE;
    this.headers.clear();
    this.soapBody = null;
    this.isBodyDOMbuilt = false;
    this.emptyBody = false;
    if (this.bodyByteArrayBuffer == null) {
      this.bodyByteArrayBuffer = new ByteArrayOutputStream(BUFFER_SIZE);
    } else {
      this.bodyByteArrayBuffer.reset();
    }
    try {
      Hashtable hash = new Hashtable();
      hash.put(NS.SOAPENV,SOAPENV_PREFIX);
      hash.put(NS.XSI,"xsi");
      hash.put(NS.XS,"xs");
      this.writer.init(bodyByteArrayBuffer,"utf-8",hash);
    } catch (Exception e) {
      throw new RuntimeException("Error in creating output !");
    }
    return this.writer;
  }

  /**
   * Initialize the instanace and extracts the headers.
   * The returned reader is positioned on the SOAPBody start element.
   */
  public XMLTokenReader initDeserializationMode(InputStream inputStream) throws ParserException, Exception {
    messageMode = DESERIALIZATION_MODE;
    this.headers.clear();
    this.soapBody = null;
    this.isBodyDOMbuilt = false;

//    try {
//      ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//      int count = -1;
//      byte[] buffer = new byte[100];
//
//      while ((count = inputStream.read(buffer)) != -1) {
//        byteArray.write(buffer, 0, count);
//      }
//
//      System.out.println("This is the message: ");
//      byteArray.writeTo(System.out);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
    if (reader == null) {
      reader = XMLTokenReaderFactory.getInstance().createReader(inputStream);
    } else {
      reader.init(inputStream);
    }

    reader.begin();
    checkEnvelope(reader);
    extractHeaders(reader);
    return reader;
  }

  private void checkEnvelope(XMLTokenReader reader) throws Exception {
    reader.moveToNextElementStart();
    if (reader.getState() == XMLTokenReader.STARTELEMENT && reader.getLocalNameCharArray().equals(ENVELOPETAG) && reader.getURICharArray().equals(NS.SOAPENV)) {
      return;
    } else {
      if (reader.getState() == XMLTokenReader.STARTELEMENT) {
        throw new Exception("The root message element is ["+reader.getLocalName()+"]{"+reader.getURI()+"} but it should be SOAP:Envelope.");
      } else {
        throw new Exception("Message Envelope not found. Probably empty soap message.");
      }
    }
  }

  /**
   * Returns the headers set before that
   * from initDeserialization or setHeaders methods.
   */
  public ArrayList getHeaders() {
    return headers;
  }

  /**
   * Returns true if the body is builded as dom.
   * This means that the reader is exausted in deserializationMode.
   * In serializationMode this mean that the bodyWriter buffer content
   * is builded as DOM.
   */
  public boolean isDOMbuilt() {
    return isBodyDOMbuilt;
  }

  /**
   * In DeseralizationMode from the reader is builded SOAPBody as DOMElement.
   * In SerializationMode the soapoBody buffer content is processed to
   * build body DOMElement.
   */
  public Element getSoapBody() throws Exception, ParserException {
    if (!isBodyDOMbuilt) {
      if (messageMode == DESERIALIZATION_MODE) {
        this.soapBody = buildBodyDOMFromReader();
      } else if (messageMode == SERIALIZATION_MODE) {
        this.soapBody = buildBodyDOMFromByteArray();
      }
      isBodyDOMbuilt = true;
    }
    return soapBody;
  }

  public XMLTokenReader getReader() {
    if (isBodyDOMbuilt) {
      return null;
    }
    return reader;
  }

  public XMLTokenWriter getWriter() {
    return this.writer;
  }

  public void setHeaders(Element[] headers) throws Exception {
    if (headers != null) {
      for (int i = 0; i < headers.length; i++) {
        this.headers.add(headers[i]);
      }
    }
  }

  /**
   * Writes the instance content headers and body as SOAPMessage.
   * Creates Envelope tag and writes the headers if any and the body content.
   */
  public void writeTo(OutputStream outputStream) throws IOException, Exception {

    outputStream.write(STARTENVELOPE_TAG.getBytes()); //$JL-I18N$

    if (this.headers.size() > 0) {
        outputStream.write(STARTHEADER_TAG.getBytes()); //$JL-I18N$

        for (int i = 0; i < headers.size(); i++) {
          try {
            serializer.write((Node) headers.get(i), outputStream);
          } catch (Exception trE) {
            throw new Exception(trE.getMessage());
          }
        }
        outputStream.write(ENDHEADER_TAG.getBytes()); //$JL-I18N$
    }

    if (isBodyDOMbuilt) {
      try {
       serializer.write(soapBody, outputStream);
      } catch (Exception e) {
        throw new Exception(e.getMessage());
      }
    } else {
      writer.flush();
      bodyByteArrayBuffer.flush();
      bodyByteArrayBuffer.writeTo(outputStream);
    }
    outputStream.write(ENDENVELOPE_TAG.getBytes()); //$JL-I18N$
    outputStream.flush();
  }

//Private methods==========================
  /**
   * Extracts all SOAPHeader entries as independent Elements storing them in
   * the headers ArrayList
   * The reader would be before SOAPHeader element, if any.
   * After execution the reader is on SOAPBody tag positioned.
   */
  private void extractHeaders(XMLTokenReader reader) throws Exception, ParserException {
    int code;

    while (true) {
      code = reader.moveToNextElementStart();

      switch(code) {
        case XMLTokenReader.STARTELEMENT: {
          if (reader.getLocalNameCharArray().equals(HEADERTAG) && (reader.getURICharArray().equals(NS.SOAPENV))) {
            loadHeaders(reader);
            return;
          }
          if (reader.getLocalNameCharArray().equals(BODYTAG) && (reader.getURICharArray().equals(NS.SOAPENV))) {
            return;
          }
          break;
        }
        case XMLTokenReader.EOF: {
          throw new Exception("Unexpected EOF.");
        }
      }
    }
  }

  /**
   * Iterates the SOAPHeader tag and loads all its direct childs as Elements in
   * the header ArrayList.
   * The reader must be on SOAPHeader startelement.
   * After execution the reader is on SOAPBody tag positioned.
   */
  private void loadHeaders(XMLTokenReader reader) throws Exception, ParserException {

    //Document document = null;
    Element holder = null;
//    try {
      //document = (new DocumentBuilderFactoryImpl()).newDocumentBuilder().newDocument();
      //document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    holder = document.createElementNS(NS.SOAPENV, HEADERTAG);
//    } catch (Exception e) {
//      throw new Exception("Could not create Document:"+e.getMessage());
//    }

    int code;
    int currentLevel = reader.getCurrentLevel();
    while (true) {
      code = reader.next();

      switch(code) {
        case XMLTokenReader.STARTELEMENT: {
          Element headerEntry = reader.getDOMRepresentation(document);
          //Element headerEntry = fillElement(holder, reader);
          headers.add(headerEntry);
          break;
        }
        case XMLTokenReader.ENDELEMENT: {
          if (reader.getCurrentLevel()<currentLevel) {
            code = reader.moveToNextElementStart();
            if (code == XMLTokenReader.STARTELEMENT) {
              if (reader.getURICharArray().equals(NS.SOAPENV) && reader.getLocalNameCharArray().equals(BODYTAG)) {
                return;
              } else {
                throw new Exception("Incorrect message content. Body element not found.");
              }
            } else {
              throw new Exception("Unexpected EOF.");
            }
          } else {
            throw new Exception("Error in message structure. SOAP:Header end tag is expected.");
          }
        }
        case XMLTokenReader.EOF: {
          throw new Exception("Unexpected message end.");
        }
      }
    }
  }

  /**
   * The reader must be on STARTELEMENT.
   * Creeates an Element with father parent parameter.
   * After execution the reader is positioned on ENDELEMENT.
   */
  public static Element fillElement(Node parent, XMLTokenReader reader) throws Exception, ParserException {

    Element element = parent.getOwnerDocument().createElementNS(reader.getURI(), reader.getQName());
    Attributes attrs = reader.getAttributes();
    if (attrs == null) {
      throw new Exception("The reader is not on startState");
    }

    int attrsLeght = attrs.getLength();
    String attribUri, attribQName, attribValue;

    for (int i = 0; i < attrsLeght; i++) {
      attribUri = attrs.getURI(i);
      attribQName = attrs.getQName(i);
      attribValue = attrs.getValue(i);

      if (!attribUri.equals(XMLParserConstants.sXMLNSNamespace) && (!getLocalName(attribQName).equals("xmlns"))) {
        element.setAttributeNS(attribUri, attribQName, attribValue);
      }
    }

    int code;
    while (true) {
      code = reader.next();

      switch (code) {
        case XMLTokenReader.COMMENT: {
          element.appendChild(element.getOwnerDocument().createComment(reader.getValue()));
          break;
        }
        case XMLTokenReader.CHARS: {
          element.appendChild(element.getOwnerDocument().createTextNode(reader.getValue()));
          break;
        }
        case XMLTokenReader.STARTELEMENT: {
          element.appendChild(fillElement(parent, reader));
          break;
        }
        case XMLTokenReader.ENDELEMENT: {
//          if (reader.getLocalNameCharArray().equals(element.getLocalName()) && reader.getURICharArray().equals(element.getNamespaceURI())) {
            return element;
//          }
//          throw new Exception("Incorrect xml content.");
        }
        case XMLTokenReader.EOF: {
          throw new Exception("Unexpected EOF.");
        }
      }
    }
  }

  private static String getLocalName(String qname) {
    int delimiter = qname.indexOf(':');
    if (delimiter == -1) {
      return qname;
    }
    return qname.substring(delimiter + 1);
  }

  /**
   * Creates BodyDOMElement from this.reader.
   * The reader must be before or on the Body startElement tag.
   * After execution the readers is positioned on its endElement tag.
   */
  private Element buildBodyDOMFromReader() throws Exception, ParserException {

    Document document = DocumentPool.getSOAP11Document();
    try {
      reader.passChars();
      int code = reader.getState();
      if (code == XMLTokenReader.STARTELEMENT) {
        if (reader.getLocalNameCharArray().equals(BODYTAG) && reader.getURICharArray().equals(NS.SOAPENV)) {
          return reader.getDOMRepresentation(document);
        }
        while (reader.getState() != XMLTokenReader.EOF) {
          reader.next();
        }
      }
    } finally {
      DocumentPool.returnSOAP11Document(document);
    }
    throw new Exception("Body tag not found !");
  }

  /**
   * Creates DOMElement from bodyByteArrayBuffer.
   */
  private Element buildBodyDOMFromByteArray() throws Exception {
    try {
      //Document document = null;
      if (bodyByteArrayBuffer.size() == 0) {
        //document = new SOAPDocumentImpl();//DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element body = document.createElementNS(NS.SOAPENV, SOAPENV_PREFIX + ":" + BODYTAG);
        return body;
        //document.appendChild(body);
      } else {
        byte[] bodyStart = STARTENVELOPE_TAG.getBytes("utf-8");
        byte[] bodyEnd = ENDENVELOPE_TAG.getBytes("utf-8");
        byte[] perm = new byte[bodyByteArrayBuffer.size()+bodyStart.length+bodyEnd.length];
        System.arraycopy(bodyStart,0,perm,0,bodyStart.length);
        System.arraycopy(bodyEnd,0,perm,(perm.length-bodyEnd.length),bodyEnd.length);
        System.arraycopy(bodyByteArrayBuffer.toByteArray(),0,perm,bodyStart.length,bodyByteArrayBuffer.size());
        Document docperm = builder.parse(new ByteArrayInputStream(perm));
        Element envelope = docperm.getDocumentElement();
        Element result = null;
        Node node = envelope.getFirstChild();
        while (node != null) {
          if (node.getNodeType() == Node.ELEMENT_NODE) {
            result = (Element) node;
          }
          node = node.getNextSibling();
        }
        return result;
      }
      //return  document.getDocumentElement();
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  /**
   * Creates and returns new SOAP Header.
   * @param uri
   * @param localName
   * @return
   */
  public Element createSoapHeader(String uri, String localName) throws Exception {
    if (document == null) {
      document = (DocumentBuilderFactory.newInstance()).newDocumentBuilder().newDocument();
    }
    return document.createElementNS(uri,localName);
  }

  /**
   * Returns soap header with specified qname. If more than one just the first found.
   * @param uri
   * @param localName
   * @return
   */
  public Element getSoapHeader(String uri, String localName) {
    for (int i=0; i<headers.size(); i++) {
      Element e = (Element) headers.get(i);
      if (e.getLocalName().equals(localName)) {
        if (e.getNamespaceURI() != null || e.getNamespaceURI().equals(uri)) {
          return e;
        }
      }
    }
    return null;
  }

}
