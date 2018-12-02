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
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.SessionFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.*;
import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;
import com.sap.engine.interfaces.webservices.runtime.component.ClientProtocolFactory;
import org.w3c.dom.*;

import javax.xml.parsers.*;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SessionProtocol implements AbstractProtocol,ClientProtocolFactory {

  public static final String NAME = "SessionProtocol";
  private Document document;
  private boolean maintainSession = true;
  private boolean isPropertySet = false;

  public boolean isMaintainSession() {
    return maintainSession;
  }

  public void closeSession() {
    this.maintainSession = false;
    this.isPropertySet = true;
  }

  public SessionProtocol() throws Exception {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    document = builder.newDocument();
  }

  public void init(PropertyContext context) throws ClientProtocolException {

  }

  /**
   * Disable Session Bean feature.
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
      PropertyContext feature = features.getSubContext(SessionFeature.SESSION_FEATURE);
      if (feature.isDefined()) { // This feature is defined
        // This httpCookies method is supported
        if (SessionFeature.HTTP_SESSION_METHOD.equals(feature.getProperty(SessionFeature.SESSION_METHOD_PROPERTY))) {
          if (isPropertySet) {
            if (maintainSession) {
              feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_TRUE);
            } else {
              maintainSession = true;
              feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_FALSE);
            }
          }
          boolean mSession = true;
          if (SessionFeature.USE_SESSION_FALSE.equals(feature.getProperty(SessionFeature.HTTP_MAINTAIN_SESSION))) {
            mSession = false;
          }
//          if (isPropertySet) {
//            mSession = maintainSession;
//            if (mSession) {
//              feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_TRUE);
//            } else {
//              feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_FALSE);
//            }
//          }
          if (mSession == false) {
            if (SessionFeature.USE_SESSION_TRUE.equals(feature.getProperty(SessionFeature.ABAP_SESSION))) {
              // ABAP 6.20 end session
              Object transportInterface = context.getProperty(ClientTransportBinding.TRANSPORT_INTERFACE);
              if (transportInterface instanceof HTTPTransportInterface) {
                //System.out.println("HTTP Transport is used !");
                HTTPTransportInterface http = (HTTPTransportInterface) transportInterface;
                String endpoint = http.getEndpoint();
                if (endpoint.indexOf("?")!=-1) {
                  endpoint += "&session_mode=2";
                } else {
                  endpoint += "?session_mode=2";
                }
                //System.out.println("Endpoint is :"+endpoint);
                try {
                  http.setEndpoint(endpoint);
                } catch (Exception e) {
                  throw new ClientProtocolException("Can not set transport Endpoint to ["+endpoint+"]");
                }
              }
              feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_FALSE);
            } else {
              if (msg.isEmptyBody()) {
                // 6.30 flush (close session)
                if (feature.getProperty(SessionFeature.SESSION_COOKIE_PROPERTY)!= null) {
                  Element header = document.createElementNS(SessionFeature.SESSION_FEATURE,"sapsess:Session");
                  Element enabled = document.createElement("enableSession");
                  Text content = document.createTextNode("false");
                  enabled.appendChild(content);
                  header.appendChild(enabled);
                  msg.getHeaders().add(header);
                }
              } else {
                // 6.30 discard cookie and open new http session
                feature.clearProperty(SessionFeature.SESSION_COOKIE_PROPERTY);
                feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION,SessionFeature.USE_SESSION_TRUE);
              }
            }
          } else {
            if (SessionFeature.USE_SESSION_TRUE.equals(feature.getProperty(SessionFeature.ABAP_SESSION))) {
              // ABAP Session enabling
              if (feature.getProperty(SessionFeature.SESSION_COOKIE_PROPERTY)== null) {
                Object transportInterface = context.getProperty(ClientTransportBinding.TRANSPORT_INTERFACE);
                if (transportInterface instanceof HTTPTransportInterface) {
                  //System.out.println("HTTP Transport is used !");
                  HTTPTransportInterface http = (HTTPTransportInterface) transportInterface;
                  String endpoint = http.getEndpoint();
                  if (endpoint.indexOf("?")!=-1) {
                    endpoint += "&session_mode=1";
                  } else {
                    endpoint += "?session_mode=1";
                  }
                  //System.out.println("Endpoint is :"+endpoint);
                  try {
                    http.setEndpoint(endpoint);
                  } catch (Exception e) {
                    throw new ClientProtocolException("Can not set transport Endpoint to ["+endpoint+"]");
                  }
                }
              }
            } else {
              // Java session enabling .. maybe not needed
              if (feature.getProperty(SessionFeature.SESSION_COOKIE_PROPERTY)== null) {
                Element header = document.createElementNS(SessionFeature.SESSION_FEATURE,"sapsess:Session");
                Element enabled = document.createElement("enableSession");
                Text content = document.createTextNode("true");
                enabled.appendChild(content);
                header.appendChild(enabled);
                msg.getHeaders().add(header);
              }
            }
          }
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
    if (SessionFeature.SESSION_FEATURE.equals(featureName)) {
      return true;
    }
    return false;
  }

  public String getName() {
    return NAME;
  }

  public String[] getFeatures() {
    return new String[] {SessionFeature.SESSION_FEATURE};
  }

  public ClientFeatureProvider newInstance() {
    try {
      return new SessionProtocol();
    } catch (Exception e) {
      return null;
    }
  }

}
