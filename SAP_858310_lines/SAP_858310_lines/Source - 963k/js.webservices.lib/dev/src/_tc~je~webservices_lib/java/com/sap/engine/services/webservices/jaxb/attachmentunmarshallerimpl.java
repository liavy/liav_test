/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.client.api.AttachmentHandler;

/**
 * Implementation of <code>javax.xml.bind.attachement.AttachmentUnmarshaller</code> based on 
 * AttachmentHandler.
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 31, 2006
 */
public class AttachmentUnmarshallerImpl extends AttachmentUnmarshaller {
  private AttachmentHandler attH;
  boolean isXOP;
  /**
   * Constructs unmarshaller that will use <code>attH</code> for dealing
   * with attachments and isXOP set to true.
   * @param attH
   */
  public AttachmentUnmarshallerImpl(AttachmentHandler attH) {
    this(attH, true);
  }
  /**
   * Constructs unmarshaller that will use <code>attH</code> for dealing
   * with attachments and isXOP set to <code>isXOP</code>.
   * @param attH
   * @param isXOP
   */
  public AttachmentUnmarshallerImpl(AttachmentHandler attH, boolean isXOP) {
    this.attH = attH;
    this.isXOP = isXOP;
  }
  
  public AttachmentHandler getAttachmentHandler() {
    return(attH);
  }
  
  @Override
  public byte[] getAttachmentAsByteArray(String arg0) {
    Attachment att = attH.getInboundAttachment(arg0);
    Object content = att.getContentObject();
    if (content instanceof byte[]) {
      return (byte[]) content;
    } else { // this should be DataHandler
      DataHandler dh = (DataHandler) content;
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      try {
        dh.writeTo(buf);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return buf.toByteArray();
    }
  }

  @Override
  public DataHandler getAttachmentAsDataHandler(String arg0) {
    final Attachment att = attH.getInboundAttachment(arg0);
    final Object content = att.getContentObject();
    if (content instanceof byte[]) {
      DataHandler dh = new DataHandler(new DataSource() {

        public String getContentType() {
          return att.getContentType();
        }

        public InputStream getInputStream() throws IOException {
          return new ByteArrayInputStream((byte[]) content);
        }

        public String getName() {
          return null;
        }

        public OutputStream getOutputStream() throws IOException {
          return null;
        }
        
      });
      return dh;
    } else { // this should be DataHandler
      DataHandler dh = (DataHandler) content;
      return dh;
    }
  }
  @Override
  public boolean isXOPPackage() {
    return this.isXOP;
  } 
  
  
}
