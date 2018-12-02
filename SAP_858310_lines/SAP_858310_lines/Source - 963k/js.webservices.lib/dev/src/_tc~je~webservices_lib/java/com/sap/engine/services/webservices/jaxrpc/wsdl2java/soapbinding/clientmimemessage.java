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

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.AbstractMessage;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * Class corresponding to Mime SOAP Message with Attachments.
 * Note: Uses ClientSOAPMessage and this binding can not work without SOAP Binding implementation.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ClientMimeMessage extends AbstractMessage {

  private static final String CONTENT_TYPE  =  "content-type";
  private static final String TEXTXML_TYPE  =  "text/xml";
  private static final String PRIMARY_TYPE  =  "multipart";
  private static final String SUB_TYPE  =  "related";
  private static final String START_PARAMETER  =  "start";
  private static final String CID  =  "cid:";  
  
  private MimeMultipart mimeMessage;  
  private ClientSOAPMessage soapMessage;
  private Hashtable cidMappings;
  private Hashtable clMappings;
  
  public ClientMimeMessage() {
    this.soapMessage = new ClientSOAPMessage();
    this.cidMappings = new Hashtable();
    this.clMappings = new Hashtable();
  }
  
  public void initDeserializationMode(final HTTPTransport transport) throws Exception {

    BodyPart rootPart = null;
    final String ctHeader = transport.getContentType();

    try {
      if (ctHeader == null) {
        InputStream input = transport.getResponseStream();
        byte[] barr = new byte[1000];
        int count = input.read(barr);
        String perm = "";
        if (count >0) {
          perm = new String(barr,0,count); //$JL-I18N$
        }
        throw new Exception("Incorrect http message! No content-type header is found. Mesasge DUMP ["+perm+"]");
      }
      //System.out.println("This is the contentType: '" + ctHeader + "'");
     
      final ContentTypeImpl contentType = new ContentTypeImpl(ctHeader);
      //System.out.println("This is the contentType2: '" + contentType.createStandardType().toString() + "'");
      if (contentType.getPrimaryType().equalsIgnoreCase(PRIMARY_TYPE) && contentType.getSubType().equalsIgnoreCase(SUB_TYPE)) {
        final InputStream responseStream = new UnclosableInputStream(transport.getResponseStream());
        this.mimeMessage = new MimeMultipart(new DataSource() {
          public InputStream getInputStream() throws IOException {
            return responseStream;
          }

          public OutputStream getOutputStream() throws IOException {
            return null;
          }

          public String getContentType() {
            return contentType.createStandardType().toString();
          }

          public String getName() {
            return "";
          }
        });

        //!!!!!may be the stpValue has to be processed in some way
        String stpValue = contentType.getParameter(START_PARAMETER);
        if (stpValue != null) {
          rootPart =  mimeMessage.getBodyPart(stpValue);
          if (rootPart == null) {
            throw new Exception("Could not find bodypart with cid: '" + stpValue + "'");
          }
        } else if (mimeMessage.getCount() > 0) {
          rootPart = mimeMessage.getBodyPart(0);
        } else {
          throw new Exception("Incorrect mime message!");
        }

        ContentTypeImpl partCt = new ContentTypeImpl(rootPart.getContentType());
        if (! partCt.getBaseType().equalsIgnoreCase(TEXTXML_TYPE)) {
          throw new Exception("Incorrect root part content-type: '" + rootPart.getContentType() + "'");
        }
        this.loadMappingTables(rootPart);
        this.soapMessage.initDeserializationMode(rootPart.getInputStream());
        // Reads leftover bytes - all streams should have EOF.
        int temp = 0;
        while ( (temp = responseStream.read()) != -1) {
          //System.out.println((char) temp);
        };
      } else if (contentType.getPrimaryType().equalsIgnoreCase("text") && contentType.getSubType().equalsIgnoreCase("xml")) {
        this.soapMessage.initDeserializationMode(transport.getResponseStream());
      } else {
        throw new Exception("Incorrect content-type found '" + contentType.toString() + "'");
      }
    } catch (Exception e) {
      if (e instanceof Exception) {
        throw (Exception) e;
      } else {
        throw  new Exception(e.getMessage());
      }
    }
  }
  
  public MimeMultipart initSerializationMode() {
    this.soapMessage.initSerializationMode();
    this.mimeMessage = new MimeMultipart(SUB_TYPE);
    return this.mimeMessage;
  }

  public MimeMultipart getMultiPartObject() {
    return this.mimeMessage;
  }

  public ClientSOAPMessage getSOAPMessage() {
    return this.soapMessage;
  }

  /**
   * Returns the mapped part if any.
   * Otherwise null.
   */
  public BodyPart getPart(String hrefValue) {
    if (hrefValue.startsWith(CID)) {
      return (BodyPart) this.cidMappings.get(hrefValue.substring(CID.length()));
    } else {
      return (BodyPart) this.clMappings.get(hrefValue);
    }
  }

  private void loadMappingTables(BodyPart rootPart) throws Exception {

    String cidh;
    String clh;
    BodyPart tPart;

    try {
      int parts = this.mimeMessage.getCount();
      for(int i = 0; i < parts; i++) {
        tPart = this.mimeMessage.getBodyPart(i);
        if (rootPart == tPart) continue;
        cidh = getSingleHeaderValue("Content-ID", tPart);
        clh = getSingleHeaderValue("Content-Location", tPart);

        if (cidh != null) {
          cidh = cidh.substring(1, cidh.length() - 1);
          this.cidMappings.put(cidh, tPart);
        } else if (clh != null) {
          this.clMappings.put(clh, tPart);
        } else {
          throw new Exception("Could not extract either Content-ID nor Content-Location header values.");
        }
      }
    } catch (MessagingException e) {
      throw new Exception(e.getMessage());
    }
  }

  private String getSingleHeaderValue(String headerName, BodyPart part) throws MessagingException {
    String[] headers = part.getHeader(headerName);
    if (headers == null) {
      return null;
    }

    if (headers.length != 1) {
      return null;
    }

    return headers[0];
  }

  public void writeTo(OutputStream outputStream) throws Exception {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      this.soapMessage.writeTo(buffer);
      InternetHeaders headers =new InternetHeaders();
      headers.addHeader("Content-type", "text/xml");
      MimeBodyPart rootPart = new MimeBodyPart(headers, buffer.toByteArray());
      this.mimeMessage.addBodyPart(rootPart, 0);
      this.mimeMessage.writeTo(outputStream);
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }
  
}
