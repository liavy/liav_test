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
package com.sap.engine.services.webservices.jaxws.ctx;

import java.security.Principal;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.runtime.ApplicationWebServiceContextImpl;
import com.sap.engine.services.webservices.jaxws.handlers.JAXWSHandlersEngine;
/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 24, 2006
 */
public class WebServiceContextImpl implements WebServiceContext {
  
  final static WebServiceContextImpl SINGLETON = new WebServiceContextImpl();
  
  private WebServiceContextImpl() {
    
  }
  
  public MessageContext getMessageContext() {
    ProviderContextHelper ctx = (ProviderContextHelper) ApplicationWebServiceContextImpl.getSingleton().getConfigurationContext();
    LogicalMessageContextImpl l_msgCtx = JAXWSHandlersEngine.getLogicalMessageContextFromProviderContext(ctx);
    MessageContextImpl msgCtx;
    if (l_msgCtx == null) { //no contexts are created
      msgCtx = JAXWSHandlersEngine.createAndBindJAXWSContexts(ctx);
    } else {
      msgCtx = l_msgCtx.getWrappedContext();
    }
    //preset it always to application, since it is assumed that this method is called only from endpont logic,
    //since no injectin of @Resouce WebServiceContext into handlers is implemented at the moment 
    msgCtx.setCurrentMode(MessageContext.Scope.APPLICATION);
    return msgCtx;
  }

  public Principal getUserPrincipal() {
    return ApplicationWebServiceContextImpl.getSingleton().getHttpServletRequest().getUserPrincipal();
  }

  public boolean isUserInRole(String arg0) {
    return ApplicationWebServiceContextImpl.getSingleton().getHttpServletRequest().isUserInRole(arg0);
  }

  public EndpointReference getEndpointReference(Element... referenceParameters) {
//    throw new RuntimeException("Method not supported");
    return null;
  }

  public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters) {
//    throw new RuntimeException("Method not supported");
    return null;
  }

}
