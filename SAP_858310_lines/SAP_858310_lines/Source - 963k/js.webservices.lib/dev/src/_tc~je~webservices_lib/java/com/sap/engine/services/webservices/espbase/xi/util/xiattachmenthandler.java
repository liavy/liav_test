package com.sap.engine.services.webservices.espbase.xi.util;

import java.util.Enumeration;
import java.util.Iterator;

import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.attachment.ESPXIAttachment;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentContainer;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentConvertor;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentImpl;
import com.sap.engine.services.webservices.espbase.messaging.ESPXIMessage;

public class XIAttachmentHandler {
  
  public static void convertSOAPAttachmentsIntoXIAttachments(AttachmentContainer soapAttachmentsContainer, ESPXIMessage xiMessage) throws Exception {
    Iterator<Attachment> soapAttachments = soapAttachmentsContainer.getAttachments().iterator();
    while(soapAttachments.hasNext()) {
      xiMessage.addAttachment(convertSOAPAttachmentIntoXIAttachment(xiMessage, soapAttachments.next()));
    }
  }
  
  public static void convertXIAttachmentsIntoSOAPAttachments(ESPXIMessage xiMessage, AttachmentContainer soapAttachmentsContainer) throws Exception {
    Enumeration<ESPXIAttachment> xiAttachments = xiMessage.getAttachments();
    while(xiAttachments.hasMoreElements()) {
      soapAttachmentsContainer.addAttachment(convertXIAttachmentIntoSOAPAttachment(xiAttachments.nextElement()));
    }
  }

  public static final ESPXIAttachment convertSOAPAttachmentIntoXIAttachment(ESPXIMessage xiMessage, Attachment soapAttachment) throws Exception {
    String name = soapAttachment.getContentId();
    String type = soapAttachment.getContentType();
    byte[] data = AttachmentConvertor.convertToByteArray(soapAttachment);
    return(xiMessage.createAttachment(name, type, data));
  }
  
  public static Attachment convertXIAttachmentIntoSOAPAttachment(ESPXIAttachment xiAttachment) {
    AttachmentImpl inboundAttachment = new AttachmentImpl(); 
    inboundAttachment.setContent(xiAttachment.getData(), xiAttachment.getType());
    inboundAttachment.setContentId(xiAttachment.getName());
    return(inboundAttachment);
  }
}
