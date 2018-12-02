﻿/*
 * Copyright (c) 2006 by SAP Labs Bulgaria.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.espbase.client.wsa;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.esp.Protocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.wsa.common.AddressingConfiguration;
import com.sap.engine.services.webservices.espbase.client.wsa.common.AddressingConstants;
import com.sap.engine.services.webservices.espbase.client.wsa.common.AddressingRegistry;
import com.sap.engine.services.webservices.espbase.client.wsa.common.Trace;
import com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.AttributedURIType;
import com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.EndpointReferenceType;
import com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.ReferenceParametersType;
import com.sap.engine.services.webservices.espbase.client.wsa.generated.ns200508.RelatesToType;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.EndpointReference;
//import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;
//import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;
import org.w3c.dom.Element;

import java.io.IOException;
//import java.io.InputStream;
import java.net.URI;
import java.rmi.UnmarshalException;
import java.rmi.MarshalException;
import java.util.HashSet;

/**
 * @author Vladimir Videlov
 * @version 7.1
 */
public class ClientAddressingProtocol200508 extends ClientAddressingProtocol implements ConsumerProtocol {
  public static final String VERSION_ID = "Id: //engine/j2ee.core.libs/dev/src/tc~je~webservices_lib/_tc~je~webservices_lib/java/com/sap/engine/services/webservices/espbase/client/wsa/ClientAddressingProtocol200508.java_1 ";
  private static final Trace TRACE = new Trace(VERSION_ID);

  public int handleRequest(ConfigurationContext configurationContext) throws ProtocolException, MessageException {
    final String SIGNATURE = "handleRequest(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    
    // TODO add check for already send MSGID as in the 2004 version
    // TODO add the used MSGID in ws logging/tracing as in the 2004 version
    
    ClientConfigurationContext clientCtx = (ClientConfigurationContext) configurationContext;

    //contextLogDump(clientCtx.getStaticContext());
    //contextLogDump(clientCtx.getDynamicContext());
    //contextLogDump(clientCtx.getPersistableContext());

    SOAPMessage msg = (SOAPMessage) clientCtx.getMessage();
    //msg.addEnvelopeNamespace(AddressingConstants.WS_ADDRESSING_PREFIX, AddressingConstants.NS_WSA);

    try {
      String messageID;
      AttributedURIType msgID = new AttributedURIType();

      if ((messageID = getRequestMessageID(clientCtx)) != null) {
        msgID.set_value(URI.create(messageID));
      }

      // If Message ID is not set, then generate new one
      if (msgID.get_value() == null || msgID.get_value().toString().equals("")) {
        msgID.set_value(URI.create(AddressingConstants.PREFIX_GUID + generator.createGUID().toString()));
      }

      Element element = msg.getSOAPHeaders().getInternalDocument().createElementNS(AddressingConstants.NS_WSA_200508, AddressingConstants.WSA_MESSAGE_ID);

      //TRACE.debugT(SIGNATURE, "MessageID: " + msgID.getSimpleContent());

      ClientAddressingProtocolProvider.MARSHALLER_200508.marshal(msgID, element);
      msg.getSOAPHeaders().addHeader(element);

      String relation = getRequestRelatesTo(clientCtx);

      if (relation != null) {
        element = msg.getSOAPHeaders().getInternalDocument().createElementNS(AddressingConstants.NS_WSA_200508, AddressingConstants.WSA_RELATES_TO);
        //element.setAttributeNS(NS.XMLNS, "xmlns:" + AddressingConstants.PREFIX_WSA, AddressingConstants.NS_WSA_200508);

        // if relation msg id is missing set it to the specification defined
        if (relation.equals("")) {
          relation = AddressingConstants.URI_200508_UNSPECIFIED_MSGID;
        }

        RelatesToType rel = new RelatesToType();
        rel.set_value(URI.create(relation));

        /***
        String relType = getRequestRelationshipType(clientCtx);

        if (relType != null && !relType.equals("")) {
          rel.setRelationshipType(relType);
        }
        /***/

        //TRACE.debugT(SIGNATURE, "RelatesTo: " + relation + " / Type: " + relType);

        ClientAddressingProtocolProvider.MARSHALLER_200508.marshal(rel, element);
        msg.getSOAPHeaders().addHeader(element);
      }

      EndpointReference erReplyTo = getRequestReplyTo(clientCtx);

      if (erReplyTo != null) {
        EndpointReferenceType replyTo = new EndpointReferenceType();
        replyTo.setAddress(new AttributedURIType());
        replyTo.getAddress().set_value(URI.create(erReplyTo.getAddress()));

        if (erReplyTo.getRefParameters().size() > 0) {
          replyTo.setReferenceParameters(new ReferenceParametersType());
          replyTo.getReferenceParameters().set_any((Element[]) erReplyTo.getRefParameters().toArray(new Element[erReplyTo.getRefParameters().size()]));
        }

        element = msg.getSOAPHeaders().getInternalDocument().createElementNS(AddressingConstants.NS_WSA_200508, AddressingConstants.WSA_REPLY_TO);

        //TRACE.debugT(SIGNATURE, "ReplyTo:   " + replyTo.getAddress().getSimpleContent());

        ClientAddressingProtocolProvider.MARSHALLER_200508.marshal(replyTo, element);
        msg.getSOAPHeaders().addHeader(element);
      }

      EndpointReference erFaultTo = getRequestFaultTo(clientCtx);

      if (erFaultTo != null) {
        EndpointReferenceType faultTo = new EndpointReferenceType();
        faultTo.setAddress(new AttributedURIType());
        faultTo.getAddress().set_value(URI.create(erFaultTo.getAddress()));

        if (erFaultTo.getRefParameters().size() > 0) {
          faultTo.setReferenceParameters(new ReferenceParametersType());
          faultTo.getReferenceParameters().set_any((Element[]) erFaultTo.getRefParameters().toArray(new Element[erFaultTo.getRefParameters().size()]));
        }

        element = msg.getSOAPHeaders().getInternalDocument().createElementNS(AddressingConstants.NS_WSA_200508, AddressingConstants.WSA_FAULT_TO);

        //TRACE.debugT(SIGNATURE, "FaultTo:   " + faultTo.getAddress().getSimpleContent());

        ClientAddressingProtocolProvider.MARSHALLER_200508.marshal(faultTo, element);
        msg.getSOAPHeaders().addHeader(element);
      }

      String toAddress = getRequestTo(clientCtx);

      if (toAddress != null && !toAddress.equals("")) {
        AttributedURIType to = new AttributedURIType();
        to.set_value(URI.create(toAddress));

        element = msg.getSOAPHeaders().getInternalDocument().createElementNS(AddressingConstants.NS_WSA_200508, AddressingConstants.WSA_TO);
        //element.setAttributeNS(msg.getSOAPVersionNS(), SOAPMessage.SOAPENV_PREFIX + ":" + AddressingConstants.CONST_MUST_UNDERSTAND, "1");

        //TRACE.debugT(SIGNATURE, "To:        " + to.getSimpleContent());

        ClientAddressingProtocolProvider.MARSHALLER_200508.marshal(to, element);
        msg.getSOAPHeaders().addHeader(element);
      }/*** else {
        throw new Exception("Required field <wsa:To> is missing!");
      }/***/

      String act = getRequestAction(clientCtx);

      if (act == null || act.equals("")) {
        //TRACE.debugT(SIGNATURE, "Getting default action for the required wsa:Action field.");
        act = AddressingConfiguration.getDefaultRequestAction(clientCtx, false);
        setRequestAction(act, clientCtx);
      }

      if (act != null && !act.equals("")) {
        AttributedURIType action = new AttributedURIType();
        action.set_value(URI.create(act));

        element = msg.getSOAPHeaders().getInternalDocument().createElementNS(AddressingConstants.NS_WSA_200508, AddressingConstants.WSA_ACTION);
        element.setAttributeNS(msg.getSOAPVersionNS(), SOAPMessage.SOAPENV_PREFIX + ":" + AddressingConstants.CONST_MUST_UNDERSTAND, "1");

        //TRACE.debugT(SIGNATURE, "Action:    " + action.getSimpleContent());

        ClientAddressingProtocolProvider.MARSHALLER_200508.marshal(action, element);
        msg.getSOAPHeaders().addHeader(element);
      } /*** else {
        throw new Exception("Required field <wsa:Action> is missing!");
      } /***/

      //TRACE.debugT(SIGNATURE, "Applying SOAP action to HTTP headers.");
      clientCtx.getPersistableContext().setProperty(HTTP_SOAP_ACTION, getRequestAction(clientCtx));

      //TODO temporary fix for ReqResp MEP and non-anon wsa:ReplyTo
      updateOperationMEP(clientCtx);
    } catch (MarshalException ex) {
      TRACE.catching(SIGNATURE, ex);
      throw new ProtocolException(ex);
    }

    //TRACE.exiting(SIGNATURE);
    return Protocol.CONTINUE;
  }

  public int handleResponse(ConfigurationContext configurationContext) throws ProtocolException {
  	//final String SIGNATURE = "handleResponse(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    SOAPMessage msg = (SOAPMessage) ((ClientConfigurationContext) configurationContext).getMessage();

    //TRACE.exiting(SIGNATURE);
    return collectAddressingData(msg, configurationContext, false);
  }

  public int handleFault(ConfigurationContext configurationContext) throws ProtocolException {
  	//final String SIGNATURE = "handleFault(ConfigurationContext)";
    //TRACE.entering(SIGNATURE);

    SOAPMessage msg = (SOAPMessage) ((ClientConfigurationContext) configurationContext).getMessage();

    //TRACE.exiting(SIGNATURE);
    return collectAddressingData(msg, configurationContext, false);
  }

  /**
   * Processes the message SOAP response
   * @param message Message object
   * @param configurationContext Configuration context
   * @param checkValidity If true - checks if the message is WSA valid
   * @return Protocol state
   * @throws ProtocolException
   */
  public int collectAddressingData(SOAPMessage message, ConfigurationContext configurationContext, boolean checkValidity) throws ProtocolException {
    final String SIGNATURE = "collectAddressingData(SOAPMessage, ConfigurationContext, boolean)";
    //TRACE.entering(SIGNATURE);

    ClientConfigurationContext clientCtx = (ClientConfigurationContext) configurationContext;

    //contextLogDump(clientCtx.getStaticContext());
    //contextLogDump(clientCtx.getDynamicContext());
    //contextLogDump(clientCtx.getPersistableContext());

    SOAPMessage msg = (SOAPMessage) clientCtx.getMessage();

    try {
      Element[] headers = msg.getSOAPHeaders().getHeaders();
      Element element;

      boolean actionFlag = false;

      for (int i = 0; i < headers.length; i++) {
        element = headers[i];

        if (element.getNamespaceURI() != null && element.getNamespaceURI().equals(AddressingConstants.NS_WSA_200508)) {
          //TRACE.debugT(SIGNATURE, "WS-Addressing element detected:\n" + element.toString());

          String localName = element.getLocalName();
          Class classType = AddressingRegistry.lookupClassType(localName, AddressingConstants.NS_WSA_200508);

          if (localName.equals(AddressingConstants.WSA_MESSAGE_ID)) {
            AttributedURIType messageID = (AttributedURIType) ClientAddressingProtocolProvider.MARSHALLER_200508.unmarshal(classType, element);
            getDynamicResponseContext(clientCtx).setProperty(AddressingConstants.WSA_MESSAGE_ID, messageID.get_value().toString());
          } else if (localName.equals(AddressingConstants.WSA_RELATES_TO)) {
            RelatesToType relatesTo = (RelatesToType) ClientAddressingProtocolProvider.MARSHALLER_200508.unmarshal(classType, element);
            getDynamicResponseContext(clientCtx).setProperty(AddressingConstants.WSA_RELATES_TO, relatesTo.get_value().toString());

            if (relatesTo.getRelationshipType() != null) {
              getDynamicResponseContext(clientCtx).setProperty(AddressingConstants.WSA_RELATIONSHIP_TYPE, relatesTo.getRelationshipType());
            }
          } else if (localName.equals(AddressingConstants.WSA_TO)) {
            AttributedURIType to = (AttributedURIType) ClientAddressingProtocolProvider.MARSHALLER_200508.unmarshal(classType, element);
            getDynamicResponseContext(clientCtx).setProperty(AddressingConstants.WSA_TO, to.get_value().toString());
          } else if (localName.equals(AddressingConstants.WSA_ACTION)) {
            actionFlag = true;

            // remove mustUnderstand flag
            element.removeAttributeNS(msg.getSOAPVersionNS(), AddressingConstants.CONST_MUST_UNDERSTAND);

            AttributedURIType action = (AttributedURIType) ClientAddressingProtocolProvider.MARSHALLER_200508.unmarshal(classType, element);
            getDynamicResponseContext(clientCtx).setProperty(AddressingConstants.WSA_ACTION, action.get_value().toString());
          } else if (localName.equals(AddressingConstants.WSA_REPLY_TO)) {
            EndpointReferenceType replyTo = (EndpointReferenceType) ClientAddressingProtocolProvider.MARSHALLER_200508.unmarshal(classType, element);
            getDynamicResponseContext(clientCtx).setProperty(AddressingConstants.WSA_REPLY_TO, renderEndpointReference(replyTo));
          } else if (localName.equals(AddressingConstants.WSA_FAULT_TO)) {
            EndpointReferenceType faultTo = (EndpointReferenceType) ClientAddressingProtocolProvider.MARSHALLER_200508.unmarshal(classType, element);
            getDynamicResponseContext(clientCtx).setProperty(AddressingConstants.WSA_FAULT_TO, renderEndpointReference(faultTo));
          }
        }
      }

      if (checkValidity && !actionFlag) {
        throw new ProtocolException("Invalid WS-Addressing SOAP message. Some required fields are missing: wsa:To / wsa:Action.");
      }
    } catch (UnmarshalException ex) {
      TRACE.catching(SIGNATURE, ex);
      throw new ProtocolException(ex);
    } catch (IOException ex) {
      TRACE.catching(SIGNATURE, ex);
      throw new ProtocolException(ex);
    }

    TRACE.exiting(SIGNATURE);
    return Protocol.CONTINUE;
  }

  /**
   * Builds EndpointReference internal object from the schema generated one.
   * @param ert WSA EPR wrapper
   * @return EndpointReference internal object from the schema generated one
   */
  protected EndpointReference renderEndpointReference(EndpointReferenceType ert) {
  	//final String SIGNATURE = "renderEndpointReference(EndpointReferenceType)";
    //TRACE.entering(SIGNATURE);

    HashSet hs = new HashSet();

    if (ert.getReferenceParameters() != null) {
      Element[] refParams = ert.getReferenceParameters().get_any();

      for (int i = 0; i < refParams.length; i++) {
        hs.add(refParams[i]);
      }
    }

    EndpointReference result = new EndpointReference(ert.getAddress().get_value().toString(), hs);

    //TRACE.exiting(SIGNATURE);
    return result;
  }
}