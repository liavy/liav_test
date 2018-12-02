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
package com.sap.engine.services.webservices.espbase.client.api;

import java.util.Set;

import com.sap.engine.services.webservices.espbase.attachment.Attachment;

/**
 * Provides methods for dealing with attachments.
 * The interface is implemented and provided by provider and consumer sides.
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2006-3-10
 */
public interface AttachmentHandler {
  /**
   * Creates and returns an empty attachment object, but without set unique 'Content-Id' value.
   */
  public Attachment createAttachment();
  /**
   * Adds outbound attachment to the outbound message. 
   * @param a attachment to be added
   * @exception NullpointerException if the parameter is null.
   * @exception IlleagalStateException when on consumer side the outbound message has already been send or attachment with same 'Content-ID' as <code>a</code> has already been added. 
   */  
  public void addOutboundAttachment(Attachment a);
  /**
   * Gets attachment from the inbound message by its 'Content-Id' value.
   * @param cid url, as specified by RFC2392.
   * @return attachment object which 'Content-Id' is equal to <code>cid</code>, or null if none is found.
   * @exception NullpointerException if the parameter is null.
   * @exception IlleagalStateException if inbound attachments cannot be accessed. This happens on the consumer side when the inbound message has not been received 
   *                                   and on the provider side when the outbound processing has already started.
   */  
  public Attachment getInboundAttachment(String cid);
  /**
   * Gets outbound attachment by its 'Content-Id' value.
   * @param cid url, as specified by RFC2392.
   * @return attachment object which 'Content-Id' is equal to <code>cid</code>, or null if none is found.
   * @exception NullpointerException if the parameter is null.
   */  
  public Attachment getOutboundAttachment(String cid);
  /**
   * 
   * @return Set containing all Attachment objects from the inbound message. The set is 'read-only'.
   * @exception IlleagalStateException if inbound attachments cannot be accessed. This happens on the consumer side when the inbound message has not been received 
   *                                   and on the provider side when the outbound processing has already started.
   */
  public Set getInboundAttachments();
  /**
   * 
   * @return Set containing all Attachment object that will be send by outbound message. The set is 'read-only'.
   */
  public Set getOutboundAttachments();
}
