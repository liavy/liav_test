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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentContainer;


/**
 * MIME Message interface.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 1.0
 */
public interface MIMEMessage extends SOAPMessage {
  public static final String MULTIPART_TYPE  =  "multipart";
  public static final String RELATED_SUBTYPE  =  "related";
  public static final String MULTIPARTRELATED_MEDIA_TYPE  =  MULTIPART_TYPE + "/" + RELATED_SUBTYPE;
  public static final String START_PARAMETER  =  "start";
  public static final String CONTENT_ID_HEADER = "Content-ID";
  public static final String CONTENT_TYPE_HEADER = "Content-Type";
  
  public AttachmentContainer getAttachmentContainer();
  
  public void setAttachmentContainer(AttachmentContainer attContainer);
  
  public void initReadModeFromMIME(InputStream in, String contentType) throws IOException;
  
  public void writeMIMEMessage(OutputStream output) throws IOException;
  
  public String getResponseMIMEBoundaryParameter();
//  /**
//   * Returns attachment count in the MIME Message.
//   * @return
//   */
//  public int attachmentCount();
//  
//  /**
//   * Creates empty attachment part.
//   * @return
//   */
//  public AttachmentPart createAttachmentPart();
//  
//  /**
//   * Creates attachment part with specific DataHandlers.
//   * @param dataHandler
//   * @return
//   */
//  public AttachmentPart createAttachmentPart(DataHandler dataHandler);
//  
//  /**
//   * Creates attachment part with speciific content and content type.
//   * @param object
//   * @param contentType
//   * @return
//   */
//  public AttachmentPart createAttachmentPart(Object object, String contentType);
//  
//  /**
//   * Returns all attachment parts that contain specific header set.
//   * @param headers
//   * @return
//   */
//  public Iterator getAttachments(MimeHeaders headers);
//  
//  /**
//   * Returns iterator over all attachments in the MIME Message.
//   * @return
//   */
//  public Iterator getAttachments();
//  
//  /**
//   * Returns attachment with specified intex. 
//   * @param index
//   * @return
//   */
//  public AttachmentPart getAttachment(int index);
//  
//  /**
//   * Returns attachment with specified content id.
//   * @param contentId
//   * @return
//   */
//  public AttachmentPart getAttachment(String contentId);
//  
//  /**
//   * Adds attachment to the mime message.
//   * @param part
//   */
//  public void addAttachmentPart(AttachmentPart part);
//  
//  /**
//   * Removes all attachments from the MIME message.
//   *
//   */
//  public void removeAllAttachments();
//  
//  /**
//   * Removes attachment from the MIME message.
//   * @param part
//   * @return
//   */
//  public boolean removeAttachment(AttachmentPart part);
//  
//  /**
//   * Removes attachment from message with specified indes.
//   * @param index
//   * @return
//   */
//  public boolean removeAttachment(int index);
//  
//  /**
//   * Returns message property.
//   * @param property
//   * @return
//   */
//  public Object getProperty(String property);
//  
//  /**
//   * Sets message propery.
//   * @param property
//   * @param object
//   */
//  public void setProperty(String property, Object object);
//  
//  /**
//   * Outputs MIME message to output stream.
//   * @param output
//   */  
//  public void writeTo(OutputStream output);
//  
//  /**
//   * Returns message content type.
//   * @return
//   */
//  public String getContentType();
//  
//  /**
//   * Sets message content type.
//   * @param contentType
//   */
//  public void setContentType(String contentType);
//  
//  /**
//   * Returns MIME message root soap message.
//   * @return
//   */
//  public SOAPMessage getSOAPMessage();
//  
//  /**
//   * Clears all attachments and the nested soap message.
//   *
//   */
//  public void clear();
  
}
