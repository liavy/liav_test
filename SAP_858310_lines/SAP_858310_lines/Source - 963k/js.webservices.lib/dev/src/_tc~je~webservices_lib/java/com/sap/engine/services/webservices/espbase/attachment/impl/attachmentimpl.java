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
package com.sap.engine.services.webservices.espbase.attachment.impl;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPException;

import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.jaxm.soap.AttachmentPartImpl;
/**
 * Provides implementation of Attachment interface, by using the implementation of SAAJ AttachmentPart.
 * @author I024065
 *
 */
public class AttachmentImpl extends AttachmentPartImpl implements Attachment {
  private byte[] content;
  
  
  @Override
  public String getContentType() {
    String cT = super.getContentType();
    if (cT == null) {
      Object o = getContentObject();
      if (o != null) {
        if (o instanceof DataHandler) {
          cT = ((DataHandler) o).getContentType();
        }
      }
    }
    return cT;
  }

  public void setContentAsByteArray(byte[] b) {
    super.clearContent(); //if there is DataHandler set, remove it
    this.content = b;
  }
  
  public void setDataHandler(DataHandler dh) {
    this.content = null;//clear the byte[]
    super.setDataHandler(dh);
  }

  public Object getContentObject() {
    if (this.content != null) {
      return this.content;
    } else {
      try {
        return getDataHandler();
      } catch (SOAPException s) { //this exception is thrown when no dataHander has been set.
        return null;
      }
    }
  }
  
  public String toString() {
    String b = "cid: '" + getContentId() + "' content-type: '" + getContentType() + "'";
    Object o = getContentObject();
    if (o != null) {
      if (o instanceof byte[]) {
        return b + "content 'byte[]'";
      } else {        
        return b + "content 'DataHandler'";
      }
    } else {
      return b + "content is null";
    }
  }  
}
