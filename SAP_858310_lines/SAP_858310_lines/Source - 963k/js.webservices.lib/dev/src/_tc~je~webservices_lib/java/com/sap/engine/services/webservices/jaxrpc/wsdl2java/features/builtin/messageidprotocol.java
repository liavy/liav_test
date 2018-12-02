/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.builtin;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.*;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.MessageIdFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.ClientSOAPMessage;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.ClientMimeMessage;
import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;
import com.sap.guid.IGUIDGeneratorFactory;
import com.sap.guid.GUIDGeneratorFactory;
import com.sap.guid.IGUIDGenerator;
import com.sap.guid.IGUID;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Message Id protocol implementation.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class MessageIdProtocol implements MessageIdFeature {

  public static final String NAME = "MessageIdProtocol";
  public static final String HEADER_NAME = "messageId";
  public static final String HEADER_NAMESPACE = "http://www.sap.com/webas/640/soap/features/messageId/";
  public static final String PREFIX = "uuid:";

  private IGUIDGeneratorFactory factory;
  private IGUIDGenerator generator;
  private IGUID guid = null;

  public MessageIdProtocol() throws Exception {
    factory = GUIDGeneratorFactory.getInstance();
    generator = factory.createGUIDGenerator();
  }

  public void init(PropertyContext context) throws ClientProtocolException {
  }

  /**
   * Adds message id header to the request.
   * @param message
   * @param context
   * @return
   * @throws com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientProtocolException
   */
  public boolean handleRequest(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    ClientSOAPMessage msg = null;
    if (message instanceof ClientMimeMessage) {
      msg = ((ClientMimeMessage) message).getSOAPMessage();
    }
    if (message instanceof ClientSOAPMessage) {
      msg = (ClientSOAPMessage) message;
    }
    if (msg != null) { // This is soap binding
      PropertyContext features = context.getSubContext(ClientTransportBinding.FEATUTE_CONFIG);
      PropertyContext feature = features.getSubContext(MessageIdFeature.MESSAGEIDFEATURE);
      if (feature.isDefined()) { // This feature is defined
        this.guid = generator.createGUID();
        try {
          Element header = msg.createSoapHeader(HEADER_NAMESPACE,HEADER_NAME);
          Text content = header.getOwnerDocument().createTextNode(PREFIX+guid.toString());
          header.appendChild(content);
          msg.getHeaders().add(header);
        } catch (Exception e) {
          throw new ClientProtocolException("Unable to add message id header.");
        }
      }
    }
    return true;
  }

  public boolean handleResponse(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    return true;
  }

  public boolean handleFault(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    return true;
  }

  public boolean isFeatureImplemented(String featureName, PropertyContext property) {
    if (MessageIdFeature.MESSAGEIDFEATURE.equals(featureName)) {
      return true;
    }
    return false;
  }

  public String getName() {
    return NAME;
  }

  public String[] getFeatures() {
    return new String[] {MessageIdFeature.MESSAGEIDFEATURE};
  }

  public ClientFeatureProvider newInstance() {
    try {
      return new MessageIdProtocol();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Returns the message id of last used message.
   * @return
   */
  public String getMessageId() {
    if (guid != null) {
      return PREFIX+guid.toString();
    }
    return null;
  }

}
