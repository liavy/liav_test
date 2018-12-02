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

package com.sap.engine.services.webservices.espbase.messaging;

import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * SOAPMessage interface used by webservices framework.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 1.0
 */
public interface SOAPMessage extends Message {
  public static final int UNDEFINED = 0;
  public static final int BEFORE_WRITE = 1;
  public static final int AFTER_WRITE = 2;
  public static final int BEFORE_READ = 3;
  public static final int AFTER_READ = 4;
  public static final int COMPLETE = 5;    
  /**
   * Constant for soap 'Body' element name
   */
  public static final String BODYTAG_NAME  =  "Body";
  /**
   * Constant for soap 'Header' element name
   */
  public static final String HEADERTAG_NAME  =  "Header";
  /**
   * Constant for soap 'Envelope' element name
   */
  public static final String ENVELOPETAG_NAME = "Envelope";
  /**
   * Constant for soap fault 'Fault' element name
   */
  public static final String FAULTTAG_NAME = "Fault";
  /**
   * Constant for soap1.2 fault 'Detail' element name
   */
  public static final String SOAP12_DETAILTAG_NAME = "Detail";  
  /**
   * Constant for soap1.2 fault 'detail' element name
   */
  public static final String SOAP11_DETAILTAG_NAME = "detail";  
  /**
   * Constant for soap1.2 fault 'Code' element name
   */
  public static final String CODETAG_NAME = "Code";
  /**
   * Constant for soap1.2 fault 'Value' element name
   */
  public static final String VALUETAG_NAME = "Value";
  /**
   * Constant for soap1.2 fault 'Subcode' element name
   */
  public static final String SUBCODETAG_NAME = "Subcode";  
  /**
   * Constant for soap1.2 fault 'Reason' element name
   */
  public static final String REASONTAG_NAME = "Reason";  
  /**
   * Constant for soap1.2 fault 'Text' element name
   */
  public static final String TEXTTAG_NAME = "Text";  
  /**
   * Constant for soap1.2 fault 'Node' element name
   */
  public static final String NODETAG_NAME = "Node";  
  /**
   * Constant for soap1.2 fault 'Role' element name
   */
  public static final String ROLETAG_NAME = "Role";  
  /**
   * Constant for soap1.1 fault 'faultcode' element name
   */
  public static final String FAULTCODETAG_NAME = "faultcode";  
  /**
   * Constant for soap1.1 fault 'faultstring' element name
   */
  public static final String FAULTSTRINGTAG_NAME = "faultstring";  
  /**
   * Constant for soap1.1 fault 'actor' element name
   */
  public static final String ACTORTAG_NAME = "faultactor";
  /**
   * Constant for soap1.2 'Sender' fault code
   */
  public static final String SOAP12_SENDER_F_CODE = "Sender";
  /**
   * Constant for soap1.2 'Receiver' fault code
   */
  public static final String SOAP12_RECEIVER_F_CODE = "Receiver";
  /**
   * Constant for soap1.1 'Server' fault code
   */
  public static final String SOAP11_SERVER_F_CODE = "Server";
  /**
   * Constant for soap1.1 'Client' fault code
   */
  public static final String SOAP11_CLIENT_F_CODE = "Client";
  /**
   * Constant for soap1.1/1.2 'MustUnderstand' fault code
   */
  public static final String MUSTUNDERSTAND_F_CODE = "MustUnderstand";
  /**
   * Constant for soap1.1/1.2 'VersionMismatch' fault code
   */
  public static final String VERSIONMISMATCH_F_CODE = "VersionMismatch";
  
  /**
   * Constant for default prefix, which is mapped to soap namespace
   */
  public static final String SOAPENV_PREFIX  =  "SOAP-ENV";
  /**
   * Constant for soap1.1 elements namespaces
   */
  public static final String SOAP11_NS = "http://schemas.xmlsoap.org/soap/envelope/";   
  /**
   * Constant for soap1.2 elements namespaces
   */
  public static final String SOAP12_NS = "http://www.w3.org/2003/05/soap-envelope";   
  /**
   * Constant for soap1.1 media type (used as value in 'content-type' http header)
   */
  public static final String SOAP11_MEDIA_TYPE = "text/xml";   
  /**
   * Constant for soap1.2 media type (used as value in 'content-type' http header)
   */
  public static final String SOAP12_MEDIA_TYPE = "application/soap+xml";
  /**
   * Constant for MTOM media type (used as value in 'content-type' http header)
   */
  public static final String MTOM_MEDIA_TYPE = "application/xop+xml";
  /**
   * Constant for BXML media type (used as value in 'content-type' http header)
   */
  public static final String BXML_CONTENT_TYPE = "application/x-sap-bxml";
  
  /**
   * Returns API for working with SOAPHeaders.
   * @return
   */
  public SOAPHeaderList getSOAPHeaders();
  
  /**
   * Returns SOAP Message Body Length in bytes.
   * @return
   */
  public long getBodyLength();
  
  /**
   * Returns message state.
   * @return
   */
  public int getMessageMode();
  
  /**
   * Initializes the write to buffer mode. 
   */
  public void initWriteMode(String soapNS);
  
  /**
   * Inits message read mode from input stream.
   * Creates reader for the input stream and reads the header information.
   * @param input
   */
  public void initReadMode(InputStream input, String soapNS) throws IOException;
  
  /**
   * Inits message read mode form external xml token reader.
   * Reads the SOAP header and positions on the SOAP body.
   * @param input
   */
  public void initReadMode(XMLTokenReader input, String soapNS) throws IOException;
  
  /**
   * Returns SOAPBodyReader it can be called only once and in read mode.
   * @return
   */
  public XMLTokenReader getBodyReader();
  
  /**
   * Return the xml body writer.
   * @return
   */
  public XMLTokenWriter getBodyWriter();
  
  /**
   * Replaces the original body writer with custom one.
   * The original one is returned as a result.
   * @param writer
   * @return
   */
  public XMLTokenWriter replaceBodyWriter(XMLTokenWriter writer) throws IOException;
  
  /**
   * Sets a substitute body reader. The substitute reader can be set only once and should be intialized and positioned on
   * SOAP body.
   * @param reader
   * @return
   * @throws IOException
   */
  public XMLTokenReader replaceBodyReader(XMLTokenReader reader) throws IOException;
  
  /**
   * Returns envelope reader over current message.   
   */
  public void getEnvelopeReader();
  
  /**
   * Outputs written message to output stream. The message must be in complete state.
   * @param output
   */
  public void writeTo(OutputStream output) throws IOException;
  
  /**
   * Confirms the read of the SOAP Body.
   *
   */
  public void commitRead();
  
  /**
   * Confirms the write of the SOAP Body.
   *
   */
  public void commitWrite();  
  
  /**
   * Clears the message contents and state.
   *
   */
  public void clear();

  /**
   * Returns enumeration of additional envelope namespaces.
   * @return
   */  
  public Enumeration getEnvelopeNamespaces();
  
  /**
   * Returns aditional envelope namespace from prefix.
   * @param prefix
   * @return
   */
  public String getEnvelopeNamespacePrefix(String namespace);
  
  /**
   * Adds additional namespace to the envelope.
   * @param prefix
   * @param namespace
   */
  public void addEnvelopeNamespace(String prefix, String namespace);
  /**
   * @return the namespace of the soap version which is current in use by this object. 
   *         The returned value is one of the constants #SOAP11_NS and #SOAP12_NS. If the message is not initialized 
   *         with soap namespace, <code>null</code> is returned.
   */
  public String getSOAPVersionNS();

}
