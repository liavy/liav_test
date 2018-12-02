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

import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProtocolExtensions;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentContainer;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;

public class AttachmentProtocolNY implements ConsumerProtocol, ProtocolExtensions {
  
  public static final String NAME = "AttachmentProtocol";  

  /**
   * Returns protocol name.
   * @return
   */
  public String getProtocolName() {
    return NAME;
  }

  /**
   * Event called on fault. Is is not used in web services clients.
   * @param arg0
   * @return
   * @throws ProtocolException
   */
  public int handleFault(ConfigurationContext arg0) throws ProtocolException {
    return CONTINUE;
  }

  /**
   * Method invoked by the client runtime after message contents are serialized and prior method call.
   * @param arg0
   * @return
   * @throws ProtocolException
   * @throws MessageException
   */
  public int handleRequest(ConfigurationContext arg0) throws ProtocolException, MessageException {
    ClientConfigurationContext clientContext = (ClientConfigurationContext) arg0;
    // Clears old dynamic property
    AttachmentContainer inboundAttachments = (AttachmentContainer) clientContext.getDynamicContext().getProperty(PublicProperties.P_INBOUND_ATTACHMENTS);
    if (inboundAttachments != null) {
      inboundAttachments.clear();
    } else {
      inboundAttachments = new AttachmentContainer();
      clientContext.getDynamicContext().setProperty(PublicProperties.P_INBOUND_ATTACHMENTS,inboundAttachments);
    }
    AttachmentContainer outboundAttachments = (AttachmentContainer) clientContext.getDynamicContext().getProperty(PublicProperties.P_OUTBOUND_ATTACHMENTS);
    if (outboundAttachments != null) {
      // Adds the outbound attachments to the message
      Message message = clientContext.getMessage();        
      if (message != null && message instanceof MIMEMessage) {
        MIMEMessage mimeMessage = (MIMEMessage) message;
        outboundAttachments.putAll(mimeMessage.getAttachmentContainer());        
      }
      outboundAttachments.clear();
    } else {
      outboundAttachments = new AttachmentContainer();
      clientContext.getDynamicContext().setProperty(PublicProperties.P_OUTBOUND_ATTACHMENTS,outboundAttachments);
    }        
    return CONTINUE;
  }

  /**
   * Event called before response deserialization for handling the response message.
   * @param arg0
   * @return
   * @throws ProtocolException
   */
  public int handleResponse(ConfigurationContext arg0) throws ProtocolException {
    ClientConfigurationContext clientContext = (ClientConfigurationContext) arg0;
    // Clears old dynamic property
    AttachmentContainer outboundAttachments = (AttachmentContainer) clientContext.getDynamicContext().getProperty(PublicProperties.P_OUTBOUND_ATTACHMENTS);
    if (outboundAttachments != null) {
      outboundAttachments.clear();
    } else {
      outboundAttachments = new AttachmentContainer();
      clientContext.getDynamicContext().setProperty(PublicProperties.P_INBOUND_ATTACHMENTS,outboundAttachments);
    }
    AttachmentContainer inboundAttachments = (AttachmentContainer) clientContext.getDynamicContext().getProperty(PublicProperties.P_INBOUND_ATTACHMENTS);
    if (inboundAttachments == null) {
      inboundAttachments = new AttachmentContainer();
      clientContext.getDynamicContext().setProperty(PublicProperties.P_OUTBOUND_ATTACHMENTS,outboundAttachments);
    } else {
      inboundAttachments.clear();
    }
    // Adds the outbound attachments to the message
    Message message = clientContext.getMessage();        
    if (message != null && message instanceof MIMEMessage) {
      MIMEMessage mimeMessage = (MIMEMessage) message;
      Set messageAttachments = mimeMessage.getAttachmentContainer().getAttachments();      
      Iterator it = messageAttachments.iterator();
      while (it.hasNext()) {
        Attachment attachment = (Attachment) it.next();
        inboundAttachments.addAttachment(attachment);  
      }
    }          
    return CONTINUE;    
  }

  /**
   * Event called after message deserialization.
   * @param arg0
   * @return
   * @throws ProtocolException
   * @throws MessageException
   */
  public int afterDeserialization(ConfigurationContext arg0) throws ProtocolException, MessageException {
    return CONTINUE;    
  }

  /**
   * Handles the message before 
   * @param arg0
   * @throws ProtocolException
   */
  public void beforeSerialization(ConfigurationContext arg0) throws ProtocolException {
  }
  

  /**
   * @param arg0
   * @throws ProtocolException
   */
  public void afterHibernation(ConfigurationContext arg0) throws ProtocolException {
    // Not implemented    
  }

  /**
   * @param arg0
   * @throws ProtocolException
   */
  public void beforeHibernation(ConfigurationContext arg0) throws ProtocolException {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param arg0
   * @throws ProtocolException
   */
  public void finishHibernation(ConfigurationContext arg0) throws ProtocolException {
    // TODO Auto-generated method stub
    
  }

  /**
   * @param arg0
   * @throws ProtocolException
   */
  public void finishMessageDeserialization(ConfigurationContext arg0) throws ProtocolException {
    // TODO Auto-generated method stub
    
  }
  

}
