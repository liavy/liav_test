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

import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.util.NestedIOException;
import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentContainer;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.additions.ContentTypeInner;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.UnclosableInputStream;
import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.soap.MimeHeader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 1.0
 */
public class MIMEMessageImpl extends SOAPMessageImpl implements MIMEMessage {
  private AttachmentContainer attContainer = new AttachmentContainer();
  private MimeMultipart responseMsg;     
  
  public static String getSOAP11ContentType() {    
    return SOAPMessage.SOAP11_MEDIA_TYPE + "; charset=utf-8";
  }
  
  public static String getSOAP12ContentType() {
    return SOAPMessage.SOAP12_MEDIA_TYPE + "; charset=utf-8";
  }
  
  public static String getSOAP12ContentTypeWithAction(String action) {    
    return SOAPMessage.SOAP12_MEDIA_TYPE + "; charset=utf-8; action=\"" + action + "\"";
  }

  public static String getBXMLContentType() {
    return SOAPMessage.BXML_CONTENT_TYPE;
  }

  public static String getSOAP11MIMEContentType() {
    return MIMEMessage.MULTIPART_TYPE + "/" + MIMEMessage.RELATED_SUBTYPE + "; type=\"" + SOAPMessage.SOAP11_MEDIA_TYPE + "\"";
  }
  
  public static String getSOAP11MIMEContentTypeWithBoundary(String boundary) {
    return MIMEMessage.MULTIPART_TYPE + "/" + MIMEMessage.RELATED_SUBTYPE + "; type=\"" + SOAPMessage.SOAP11_MEDIA_TYPE + "\"; boundary=\"" + boundary + "\"";
  }

  public static String getSOAP12MIMEContentType() {
    return MIMEMessage.MULTIPART_TYPE + "/" + MIMEMessage.RELATED_SUBTYPE + "; type=\"" + SOAPMessage.SOAP12_MEDIA_TYPE + "\"";
  }

  public static String getSOAP12MIMEContentTypeWithBoundary(String boundary) {
    return MIMEMessage.MULTIPART_TYPE + "/" + MIMEMessage.RELATED_SUBTYPE + "; type=\"" + SOAPMessage.SOAP12_MEDIA_TYPE + "\"; boundary=\"" + boundary + "\"";
  }
  
  public static String getMTOMContentType(String boundary) {
    return MIMEMessage.MULTIPART_TYPE + "/" + MIMEMessage.RELATED_SUBTYPE + "; type=\"" + SOAPMessage.MTOM_MEDIA_TYPE + "\"; boundary=\"" + boundary + "\"; start-info=\"" + SOAPMessage.SOAP11_MEDIA_TYPE + "\"";
  }

  public AttachmentContainer getAttachmentContainer() {
    return this.attContainer;
  }

  public void setAttachmentContainer(AttachmentContainer attContainer) {
    this.attContainer = attContainer;
  }
  
  public void initReadModeFromMIME(final InputStream in, String ct) throws IOException {
    final ContentTypeInner contentType = new ContentTypeInner(ct);
    if (! (contentType.getPrimaryType().equalsIgnoreCase(MULTIPART_TYPE) && contentType.getSubType().equalsIgnoreCase(RELATED_SUBTYPE))) {
      throw new IllegalArgumentException("Expected content-type '" + MULTIPART_TYPE + "/" + RELATED_SUBTYPE + "'");
    }
    try {
      MimeMultipart mimeMessage = new MimeMultipart(new DataSource() {
        final InputStream responseStream = new UnclosableInputStream(in);
        public InputStream getInputStream() throws IOException {
          return responseStream;
        }

        public OutputStream getOutputStream() throws IOException {
          return null;
        }

        public String getContentType() {
          return contentType.toString();
        }

        public String getName() {
          return "";
        }
      });

      BodyPart rootPart;
      String stpValue = contentType.getParameter(START_PARAMETER);
      if (stpValue != null) {
        rootPart =  mimeMessage.getBodyPart(stpValue);
        if (rootPart == null) {
          throw new IOException("Unable to find out root MIME part by using '" + stpValue + "'");
        }
      } else if (mimeMessage.getCount() > 0) {
        rootPart = mimeMessage.getBodyPart(0);
      } else {
        throw new IOException("MIME message without parts.");
      }
      //initialize the SoapMessageImpl data    
      ContentTypeInner partCt = new ContentTypeInner(rootPart.getContentType());
      if (SOAPMessage.SOAP11_MEDIA_TYPE.equalsIgnoreCase(partCt.getBaseType())) {
        super.initReadMode(rootPart.getInputStream(), SOAPMessage.SOAP11_NS);
      } else if (SOAPMessage.SOAP12_MEDIA_TYPE.equalsIgnoreCase(partCt.getBaseType())) {
        super.initReadMode(rootPart.getInputStream(), SOAPMessage.SOAP12_NS);
      } else if (SOAPMessage.MTOM_MEDIA_TYPE.equalsIgnoreCase(partCt.getBaseType())) { //this is MTOM package
        if (SOAPMessage.SOAP11_MEDIA_TYPE.equalsIgnoreCase(partCt.getParameter("type"))) { //this is SOAP11 msg
          super.initReadMode(rootPart.getInputStream(), SOAPMessage.SOAP11_NS);
        } else if (SOAPMessage.SOAP12_MEDIA_TYPE.equalsIgnoreCase(partCt.getParameter("type"))) {
          super.initReadMode(rootPart.getInputStream(), SOAPMessage.SOAP12_NS);
        } else {
          throw new IOException("Unknown MTOM 'type' parameter '" + rootPart.getContentType() + "'");
        }
      } else {
        throw new IOException("Incorrect root content type found '" + rootPart.getContentType() + "'");
      }
      this.initAttachmentContainer(mimeMessage, rootPart);
      // Reads leftover bytes - all streams should have EOF. This is waranteed by the http socked.
      int temp = 0;
      while ( (temp = in.read()) != -1) {
        temp = 0;
      };
    } catch (Exception e) {
      if (e instanceof IOException) {
        throw (IOException) e;
      } else {
        throw new NestedIOException(e);
      }
    }
  }
  
  public String getResponseMIMEBoundaryParameter() {
    if (responseMsg == null) {
      responseMsg = new MimeMultipart(MIMEMessage.RELATED_SUBTYPE); 
    }
    try {
      ContentType cT = new ContentType(responseMsg.getContentType());
      return cT.getParameter("boundary");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  } 
  
  public void writeMIMEMessage(OutputStream output) throws IOException {
    //write soap message in buffer
    final ReferenceByteArrayOutputStream buffer = new ReferenceByteArrayOutputStream();
    super.writeTo(buffer);
    //determine content-type
    final String ct;
    if (SOAPMessage.SOAP11_NS.equals(super.getSOAPVersionNS())) {
      ct = SOAPMessage.SOAP11_MEDIA_TYPE + "; charset=utf-8";
    } else {
      ct = SOAPMessage.SOAP12_MEDIA_TYPE + "; charset=utf-8";
    }
    //initialize the internal MimeMultipart
    getResponseMIMEBoundaryParameter();
    try {
      //create root part
      DataHandler dh = new DataHandler(new DataSource() {
        public String getContentType() {
          return ct;
        }
        public InputStream getInputStream() throws IOException {
          return new ByteArrayInputStream(buffer.getContentReference(), 0, buffer.size()); 
        }
        public String getName() {
          return null;
        }
        public OutputStream getOutputStream() throws IOException {
          return null;
        }
      }); 
       
      MimeBodyPart rootPart = new MimeBodyPart();
      rootPart.setDataHandler(dh);
      rootPart.setHeader(MIMEMessage.CONTENT_TYPE_HEADER, ct);
      //add root part
      responseMsg.addBodyPart(rootPart, 0);
      //add the attachment parts from container
      Set atts = this.attContainer.getAttachments();
      Iterator itr = atts.iterator();
      int n = 1;
      while (itr.hasNext()) {
        Attachment a = (Attachment) itr.next();
        BodyPart part = createMimePart(a);
        responseMsg.addBodyPart(part, n);
        n++;
      }
      //write MIME message to output
      responseMsg.writeTo(output);
    } catch (MessagingException e) {
      throw new NestedIOException(e);
    }
    
  }
  /**
   * Constructs MimePart object from <code>a</code> object.
   * @param a
   * @return
   * @throws IOException
   */
  private BodyPart createMimePart(Attachment a) throws IOException {
    Object aContent = a.getContentObject();
    String cT = a.getContentType();
    String cid = a.getContentId();
    if (aContent == null) {
      throw new IOException("Attachment with 'Content-ID' '" + cid + "' and 'content-type' '" + cT + "' has no content.");
    }
    
    MimeBodyPart part; 
    try {
      if (aContent instanceof DataHandler) {
        DataHandler dh = (DataHandler) aContent;
        part = new MimeBodyPart();
        part.setDataHandler(dh);
      } else { //this is byte[]
        byte[] arr = (byte[]) aContent;
        InternetHeaders iHs = new InternetHeaders();
        part = new MimeBodyPart(iHs, arr);
      }
      if (cid != null) {
        part.setHeader(MIMEMessage.CONTENT_ID_HEADER, cid);
      }
      //append the custom attachments
      Iterator itr = a.getAllMimeHeaders();
      MimeHeader h;
      while (itr.hasNext()) {
        h = (MimeHeader) itr.next();
        String hn = h.getName();
        String hv = h.getValue();
        if (MIMEMessage.CONTENT_ID_HEADER.equalsIgnoreCase(hn)) {
          hv = "<" + hv + ">";
        }
        part.setHeader(hn, hv);
      }
    } catch (MessagingException e) {
      throw new NestedIOException(e);
    }
    return part;
  }
  /**
   * Reads the attachments from <code>mimeMessage</code> converts them the SAP Attachments and puts them inside 
   * attachment container. Only MIME message root part is not included, since it is already parsed by SOAPMessageImpl 
   * @param mimeMessage MIME message which contains different mime parts
   * @param rootPart root part of the MIME message
   * @throws MessagingException
   */
  private void initAttachmentContainer(MimeMultipart mimeMessage, BodyPart rootPart) throws IOException, MessagingException {
    String cidh;
    String cT;
    BodyPart tPart;

    int parts = mimeMessage.getCount();
    for(int i = 0; i < parts; i++) {
      tPart = mimeMessage.getBodyPart(i);
      //skip the root part
      if (rootPart == tPart) continue;
      //get 'Content-Id'
      cidh = getSingleHeaderValue(CONTENT_ID_HEADER, tPart);
      if (cidh != null) {
        if (cidh.startsWith("<") && cidh.endsWith(">")) {
          cidh = cidh.substring(1, cidh.length() - 1); //removed the leading and trailing '<' '>'
        }
      }
      //get 'content-type'
      cT = getSingleHeaderValue(MIMEMessage.CONTENT_TYPE_HEADER, tPart);
      
      Attachment sapAtt = AttachmentContainer.createAttachment();
      sapAtt.setContentId(cidh);
      sapAtt.setContentType(cT);
      sapAtt.setDataHandler(tPart.getDataHandler());
      //add custom headers
      Enumeration en = tPart.getAllHeaders();
      while (en.hasMoreElements()) {
        Header mH = (Header) en.nextElement();
        String mH_name = mH.getName();
        if (CONTENT_ID_HEADER.equalsIgnoreCase(mH_name) || CONTENT_TYPE_HEADER.equalsIgnoreCase(mH_name)) {
          continue;
        }
        sapAtt.setMimeHeader(mH_name, mH.getValue());
      }
      this.attContainer.addAttachment(sapAtt);
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
  
  public void clear() {
    this.attContainer.clear();
    responseMsg = null;
    super.clear();
  }   
  
  public static void main(String[] args) throws Exception {
    ContentType ct = new ContentType(MIMEMessageImpl.getSOAP12MIMEContentTypeWithBoundary("--someboundary"));
    System.out.println(ct.getParameter("boundary"));
    MIMEMessageImpl mm = new MIMEMessageImpl();
    mm.initWriteMode(SOAPMessage.SOAP11_NS);
    XMLTokenWriter wr = mm.getBodyWriter();
    wr.enter("some-ns", "wrapper element");
    wr.leave();
    Attachment a = AttachmentContainer.createAttachment();
    a.setContentType("application/octetstream");
    a.setContentId("1142348990353arg2");
    a.setContentAsByteArray(new byte[]{1, 2});
    a.setDataHandler(new DataHandler(new DataSource() {

      public String getContentType() {
        // TODO Auto-generated method stub
        return "application/octetstream";
      }

      public InputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return new ByteArrayInputStream(new byte[]{1, 2, 3});
      }

      public String getName() {
        // TODO Auto-generated method stub
        return null;
      }

      public OutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
      }
      
    }));
    a.setMimeHeader("customHeader", "customValue");
    mm.getAttachmentContainer().addAttachment(a);
    mm.writeMIMEMessage(System.out);
  }
}
