/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.util.Set;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentContainer;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentImpl;
import com.sap.engine.services.webservices.espbase.client.api.AttachmentHandler;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;

/**
 * Client implementation of the AttachmentHandler interface.
 * 
 * @version 1.0 (2006-4-3)
 * @author Chavdar Baikov, chavdar.baikov@sap.com
 */
public class AttachmentHandlerNYImpl implements AttachmentHandler {
  
  private ClientConfigurationContext clientContext;  
  
  public AttachmentHandlerNYImpl(ClientConfigurationContext context) {
    clientContext = context;    
    ConfigurationContext dynamicContext = context.getDynamicContext();
    AttachmentContainer inboundContainer = getInboundAttachmentsInner();
    if (inboundContainer == null) {
      inboundContainer = new AttachmentContainer();
      dynamicContext.setProperty(PublicProperties.P_INBOUND_ATTACHMENTS,inboundContainer);
    }
    AttachmentContainer outboundContainer = getOutboundAttachmentsInner();
    if (outboundContainer == null) {
      outboundContainer = new AttachmentContainer();
      dynamicContext.setProperty(PublicProperties.P_OUTBOUND_ATTACHMENTS,outboundContainer);
    }
  }

  /**
   * Creates new attachment.
   * @return
   */
  public Attachment createAttachment() {    
    return new AttachmentImpl();
  }
  
  /**
   * Returns inner attachment container for inbound attachments.
   * @return
   */
  private AttachmentContainer getInboundAttachmentsInner() {
    return (AttachmentContainer) this.clientContext.getDynamicContext().getProperty(PublicProperties.P_INBOUND_ATTACHMENTS);      
  }  
  
  /**
   * Returns inner attachment container for outbound attachments.
   * @return
   */
  private AttachmentContainer getOutboundAttachmentsInner() {
    return (AttachmentContainer) this.clientContext.getDynamicContext().getProperty(PublicProperties.P_OUTBOUND_ATTACHMENTS);      
  }

  /**
   * Adds outbound attachments.
   * @param a
   */
  public void addOutboundAttachment(Attachment a) {
    getOutboundAttachmentsInner().addAttachment(a);       
  }

  /**
   * Returns inbound attachments by it's content id.
   * @param cid
   * @return
   */
  public Attachment getInboundAttachment(String cid) {
    return getInboundAttachmentsInner().getAttachment(cid);    
  }

  public Attachment getOutboundAttachment(String cid) {
    return getOutboundAttachmentsInner().getAttachment(cid);
  }

  public Set getInboundAttachments() {
    return ((AttachmentContainer) this.clientContext.getDynamicContext().getProperty(PublicProperties.P_INBOUND_ATTACHMENTS)).getAttachments();  
  }

  public Set getOutboundAttachments() {
    return ((AttachmentContainer) this.clientContext.getDynamicContext().getProperty(PublicProperties.P_OUTBOUND_ATTACHMENTS)).getAttachments();  
  }

}
