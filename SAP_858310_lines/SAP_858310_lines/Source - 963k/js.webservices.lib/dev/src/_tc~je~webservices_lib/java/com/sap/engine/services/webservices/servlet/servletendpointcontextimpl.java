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
package com.sap.engine.services.webservices.servlet;

import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServletEndpointContext;

import com.sap.engine.interfaces.webservices.runtime.ApplicationWebServiceContext;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.jaxrpc.handlers.JAXRPCHandlersEngine;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-4-27
 */
public class ServletEndpointContextImpl implements ServletEndpointContext {
  
  private ServletContext servletContext;
  private ApplicationWebServiceContext appWSContext;
  
  ServletEndpointContextImpl(ServletContext servletContext, ApplicationWebServiceContext appWSContext) {
    this.servletContext = servletContext;
    this.appWSContext = appWSContext;
  }
  
  public HttpSession getHttpSession() {
    return appWSContext.getHttpServletRequest().getSession(false);
  }
  
  public MessageContext getMessageContext() {
    return JAXRPCHandlersEngine.getSOAPMessageContextFromThread();
  }
  
  public ServletContext getServletContext() {
    return this.servletContext;
  }
  
  public Principal getUserPrincipal() {
    return appWSContext.getHttpServletRequest().getUserPrincipal();
  }
  
  public boolean isUserInRole(String role) {
    return appWSContext.getHttpServletRequest().isUserInRole(role);    
  }
}
