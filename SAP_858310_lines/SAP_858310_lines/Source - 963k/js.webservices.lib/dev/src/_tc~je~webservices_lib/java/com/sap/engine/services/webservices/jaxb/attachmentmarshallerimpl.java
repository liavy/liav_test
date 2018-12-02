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

import java.net.URLEncoder;
import java.rmi.server.UID;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;

import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.client.api.AttachmentHandler;

/**
 * Implementation of <code>javax.xml.bind.attachement.AttachmentMarshaller</code> based on 
 * AttachmentHandler.
 *  
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 31, 2006
 */
public class AttachmentMarshallerImpl extends AttachmentMarshaller {
  private AttachmentHandler attH;
  private boolean isXOP;
  
  /**
   * Constructs marshaller that will use <code>attH</code> for dealing
   * with attachments and isXOP set to true.
   * @param attH
   */
  public AttachmentMarshallerImpl(AttachmentHandler attH) {
    this(attH, true);
  }
  /**
   * Constructs marshaller that will use <code>attH</code> for dealing
   * with attachments and isXOP set to <code>isXOP</code>.
   * @param attH
   * @param isXOP
   */
  public AttachmentMarshallerImpl(AttachmentHandler attH, boolean isXOP) {
    this.attH = attH;
    this.isXOP = isXOP;
  }
  
  public AttachmentHandler getAttachmentHandler() {
    return(attH);
  }

  @Override
  public String addMtomAttachment(DataHandler dataHandler, String elementNamespace, String elementLocalName) {
    return null;
  }

  @Override
  public String addMtomAttachment(byte[] data, int offset, int length, String mimeType, String elementNamespace, String elementLocalName) {
    return null;
  }

  @Override
  public String addSwaRefAttachment(DataHandler arg0) {
    Attachment att = attH.createAttachment();
    String ctType = arg0.getContentType();
    if (ctType == null) {
      ctType = "binary/octetstream";
    }
    att.setContentType(ctType);
    try {
      String cid = new UID().toString(); 
      cid = cid.replace(':', '-'); //since '-' is not escaped by the .encoded() method below.
                                   //This is necessary since SUN's RI of JAXWS does not decode
                                   //attachment's 'Content-ID' content and uses it as is, while
                                   //it decodes the 'swaref' references.
                                   //By using sequnce into which no character needs escaping,
                                   //this issue is workarounded.
      att.setContentId(URLEncoder.encode(cid, "utf-8"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    att.setDataHandler(arg0);
    
    attH.addOutboundAttachment(att);
    return "cid:" + att.getContentId();
  }
  
  @Override
  public boolean isXOPPackage() {
    return isXOP;
  }
} 
