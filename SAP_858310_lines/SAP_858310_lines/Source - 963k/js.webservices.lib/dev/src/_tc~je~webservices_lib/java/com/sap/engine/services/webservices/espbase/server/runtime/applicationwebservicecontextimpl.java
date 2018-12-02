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
package com.sap.engine.services.webservices.espbase.server.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;

import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.interfaces.webservices.esp.ApplicationWebServiceContextExt;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.TransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.MIMEHTTPTransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.SOAPHTTPTransportBinding;
import com.sap.engine.services.webservices.espbase.server.runtime.exceptions.RTResourceAccessor;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.localization.LocalizationException;


/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-21
 */
public class ApplicationWebServiceContextImpl implements ApplicationWebServiceContextExt {

  private static ThreadSystem threadSystem;
  private int coID;
  
  private static ApplicationWebServiceContextImpl theInstance; //the only instance onwhich business methods could be invoked
  
  private ApplicationWebServiceContextImpl(int id) {
    this.coID = id;
  }
  /**
   * Returns initialized instance, or null if there is no instance initalized be <code>initializeApplicationContext</code>.
   */
  public static ApplicationWebServiceContextImpl getSingleton() {
    return theInstance;
  }
  public static ApplicationWebServiceContextImpl initializeApplicationContext(ThreadSystem threadSystem) {
    ApplicationWebServiceContextImpl.threadSystem = threadSystem;
    int id = threadSystem.registerContextObject(WSContextObject.WSCONTEXT_OBJECT_NAME, new WSContextObject());
    theInstance = new ApplicationWebServiceContextImpl(id); 
    return theInstance;
  }

  public static void destroy() {
    threadSystem.unregisterContextObject(WSContextObject.WSCONTEXT_OBJECT_NAME);
    theInstance = null;
  }

  void attachRuntimeContext(ConfigurationContext context) {
    ((WSContextObject) threadSystem.getThreadContext().getContextObject(coID)).cfgContext = context;
  }

  public ConfigurationContext getConfigurationContext() {
    ConfigurationContext cfgCtx = ((WSContextObject) threadSystem.getThreadContext().getContextObject(coID)).cfgContext;
    if (cfgCtx == null) {
      throwIllegalStateException(RuntimeExceptionConstants.APPLICATION_WEBSERVICE_CONTEXT_IS_ACCESSED_FROM_INVALID_ENVIRONMENT, null);
    }
    return cfgCtx;
  }
  
  public HttpServletRequest getHttpServletRequest() {
    ProviderContextHelper pCtx = (ProviderContextHelper) getConfigurationContext();
    Transport tr = pCtx.getTransport();
    if ((tr != null) && (tr instanceof HTTPTransport)) {
      OperationMapping opMapping = null;
      try {
        opMapping = pCtx.getOperation(); 
      } catch (RuntimeProcessException pE) {
        throw new RuntimeException(pE);
      }
      if (RuntimeProcessingEnvironment.isOneWay(opMapping)) {
        throwIllegalStateException(RuntimeExceptionConstants.ONE_WAY_OPERATION, null);
      }
      return ((HTTPTransport) tr).getRequest();
    }
    throwIllegalStateException(RuntimeExceptionConstants.ILLEGAL_TRANSPORT, new Object[]{tr});
    return null;
  }

  public HttpServletResponse getHttpServletResponse() {
    ProviderContextHelper pCtx = (ProviderContextHelper) getConfigurationContext();
    Transport tr = pCtx.getTransport();
    if ((tr != null) && (tr instanceof HTTPTransport)) {
      OperationMapping opMapping = null;
      try {
        opMapping = pCtx.getOperation();
      } catch (RuntimeProcessException pE) {
        throw new RuntimeException(pE);
      }
      if (RuntimeProcessingEnvironment.isOneWay(opMapping)) {
        throwIllegalStateException(RuntimeExceptionConstants.ONE_WAY_OPERATION, null);
      }
      return ((HTTPTransport) tr).getResponse();
    }
    throwIllegalStateException(RuntimeExceptionConstants.ILLEGAL_TRANSPORT, new Object[]{tr});
    return null;
  }
  
  public List getRequestSOAPHeaders() {
    ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) getConfigurationContext();
    Message msg = null;
    try {
      Element[] headers = null;
      
      msg = ctx.getMessage();      
      if ((msg != null) && (msg instanceof SOAPMessage)) {
        SOAPMessage soapMsg = (SOAPMessage) msg;
        headers = soapMsg.getSOAPHeaders().getHeaders();
      } 
//      else if (msg instanceof InternalMIMEMessage) {
//        InternalMIMEMessage mimeMsg = (InternalMIMEMessage) msg;
//        headers = mimeMsg.getSOAPMessage().getSOAPHeaders().getHeaders();
//      }
      if (headers != null) {
        ArrayList res = new ArrayList();
        for (int i = 0; i < headers.length; i++) {
          res.add(headers[i]);
        }        
        return Collections.unmodifiableList(res);
      }
    } catch (RuntimeProcessException e) {
      throw new RuntimeException(e);
    }
    throwIllegalStateException(RuntimeExceptionConstants.ILLEGAL_MESSAGE, new Object[]{msg});
    return null;
  }
  
  public void addResponseSOAPHeader(Element header) {
    ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) getConfigurationContext();
    //check whether it is soap or mime binding
    TransportBinding tb = ctx.getTransportBinding(); 
    if ((! (tb instanceof SOAPHTTPTransportBinding)) && (! (tb instanceof MIMEHTTPTransportBinding))) {
      throw new IllegalStateException("Transport binding not supported: " + tb);      
    }
    OperationMapping opMapping = null;
    try {
      opMapping = ctx.getOperation();
    } catch (RuntimeProcessException pE) {
      throw new RuntimeException(pE);
    }
    if (RuntimeProcessingEnvironment.isOneWay(opMapping)) {
      throwIllegalStateException(RuntimeExceptionConstants.ONE_WAY_OPERATION, null);
    }
    //add the response header in context list, which is process later by transport binding.
    if (header != null) {
      ctx.getAppWSContextResponseSOAPHeaders().add(header);
    } else {
      throw new IllegalArgumentException("Cannot add null soap header");
    }
  }
  
  private void throwIllegalStateException(String msgPattern, Object[] args) throws IllegalStateException {
    try {
      if (args == null) {
        throw new IllegalStateException(LocalizableTextFormatter.formatString(RTResourceAccessor.getResourceAccessor(), msgPattern));
      } else {
        throw new IllegalStateException(LocalizableTextFormatter.formatString(RTResourceAccessor.getResourceAccessor(), msgPattern, args));        
      }
    } catch (LocalizationException lE) {
      throw new RuntimeException(lE);
    }
  }
}
