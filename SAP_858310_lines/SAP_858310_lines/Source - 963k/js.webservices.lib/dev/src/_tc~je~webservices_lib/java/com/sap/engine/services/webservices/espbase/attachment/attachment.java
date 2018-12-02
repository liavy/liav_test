/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.attachment;

import java.util.Iterator;

import javax.activation.DataHandler;

/**
 * Represents MIME attachment part - its content and headers. 
 * @author I024065
 *
 */
public interface Attachment {
  /**
   * Sets the MIME header whose name is "Content-Id" with the given value. The value must be URL-escaped.
   * When set as actual header value, the string will be enclosed by "<" ">" as defined in rfc2392. 
   * @param contentID header value 
   */
  public void setContentId(String contentID);
  /**
   * Gets the value of the MIME header whose name is "Content-Id". 
   * @return a <code>string</code> giving the value of the "Content-Id" header or <code>null</code> if there is none
   */
  public String getContentId();
  /**
   * Gets the value of the MIME header whose name is "Content-Type".
   * @return a <code>String </code> giving the value of the "Content-Type" header or <code>null</code> if there is none
   */
  public String getContentType();
  /**
   * Sets the MIME header whose name is "Content-Type" with the given value.
   * @param s a <code>String</code> giving the value of the "Content-Type" header.
   */
  public void setContentType(String s);
  /**
   * Changes the header entry that matches the given name to the given value, adding a new header if no existing header matches. 
   * <p>Note that RFC822 headers can only contain US-ASCII characters.</p>
   * @param name a <code>String</code> giving the name of the header for which to search
   * @param value a <code>String</code> giving the value to be set for the header whose name matches the given name
   */
  public void setMimeHeader(String name, String value);
  /**
   * Retrieves all the headers for this AttachmentPart object as an iterator over the MimeHeader objects.
   * 
   * @return an <code>Iterator</code> with <code>javax.xml.soap.MimeHeader</code> objects representing all of the mime headers for this object.
   * The <code>Iterator</code> supports the <code>remove</code> method.
   */
  public Iterator getAllMimeHeaders();
  /**
   * Gets all the values of the header identified by the given String.
   * @param name the name of the header; example: "Content-Type"
   * @return a <code>String</code> array giving the value for the specified header. If the header is missing <code>null</code> is returned.
   */
  public String[] getMimeHeader(String name);
  /**
   * Sets the given DataHandler object as the content of this attachment object. If already attachments content has been set via
   * this method or <code>setContentAsByteArray</code> method, the previous content is overwritten.
   * @param dh
   */
  public void setDataHandler(DataHandler dh);
  /**
   * Sets <code>byte[]</code> as content of this attachment object. If already attachments content has been set via
   * this method or <code>setDataHandler</code> method, the previous content is overwritten.
   * @param b byte array
   */
  public void setContentAsByteArray(byte[] b);
  /**
   * Gets the internal object which represents the content - either DataHandler or byte[].
   * @return byte[] or DataHander instance depending which method - <code>setDataHandler</code> or <code>setContentAsByte[]</code> has been used. 
   *         If nothing is set, null is returned.
   */
  public Object getContentObject();
  /**
   * Gets <code>DataHandler</code> object representing the content of this attachment object. 
   * If content of the attachment has been set via <code>setContentAsByteArray</code> method, the byte array
   * is wrapped inside DataHandler instance and this instance is returned.
   * If the content has been set via <code>setDataHandler</code>, the set instance is returned.
   * @exception SOAPException if there is no data in this attachment object 
   */
//  public DataHandler getDataHandler() throws SOAPException;
  /**
   * Gets the content of this attachment object as <code>byte[]</code>. If the content has been set via this method
   * the same byte array instance is returned. If it has been set via <code>setDataHandler</code>, the data handler
   * content is been written in buffer and this buffer 
   * @return byte array representing the content.
   * @exception SOAPException if there is no data in this attachment object
   * @exception IOException in the content is DataHandler and exception has occurred during the data handler serialization. 
   */
//  public byte[] getContentAsByteArray() throws SOAPException, IOException;
}
