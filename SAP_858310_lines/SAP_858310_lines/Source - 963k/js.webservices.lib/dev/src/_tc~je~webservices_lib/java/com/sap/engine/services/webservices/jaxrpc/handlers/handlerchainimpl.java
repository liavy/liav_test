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
package com.sap.engine.services.webservices.jaxrpc.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.NamespaceConstants;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.soap.SOAPFaultException;

import com.sap.engine.services.webservices.tools.ExceptionManager;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Implementation of <code>javax.xml.rpc.handler.HandlerChain</code> interface.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-3-11
 */
public class HandlerChainImpl extends java.util.ArrayList implements HandlerChain {
  
  private static final Location location = Location.getLocation(HandlerChainImpl.class);
  
  private String[] roles;
  private List handlerInfos; //$JL-SER$ //list of HandlerInfo objects
  
  private int position = -1; //holds the currently invoked handlers position.
  
  public void setHanderInfos(List hInfos) {
    this.handlerInfos = hInfos;
  }
  
  public void setHandlers(List handlers) {
    validateHandlerList(handlers);
    super.addAll(handlers);
    //this.handlers = handlers;
  }
  
  
  public void add(int index, Object element) {
    validateHandler(element);
    super.add(index, element);
  }
  public boolean add(Object o) {
    validateHandler(o);
    return super.add(o);
  }
  public boolean addAll(Collection c) {
    validateHandlerList(new ArrayList(c));
    return super.addAll(c);
  }
  public boolean addAll(int index, Collection c) {
    validateHandlerList(new ArrayList(c));    
    return super.addAll(index, c);
  }
  
  public String[] getRoles() {
    if (roles == null) {
      roles = new String[0]; //set empty array to be returned
    }
    //check for SOAP actor next. It should be included in the list.
    boolean found = false;
    for (int i = 0; i < roles.length; i++) {
      if (roles[i].equals(NamespaceConstants.NSURI_SOAP_NEXT_ACTOR)) {
        found = true;
        break;
      }
    }
    if (! found) {//append SOAP Actor next at the end.
      String[] newRoles = new String[roles.length + 1];
      System.arraycopy(roles, 0, newRoles, 0, roles.length);
      newRoles[roles.length] = NamespaceConstants.NSURI_SOAP_NEXT_ACTOR;
      this.roles = newRoles;            
    }
    return roles;
  }
  
  public void setRoles(String[] arg0) {
    if (arg0 == null) {
      throw new IllegalArgumentException("Illegal roles value 'null'");
    }
    this.roles = arg0;    
  }
  
  /**
   * Invokes handlers' <code>handleRequest()</code> methods.
   * @return true if all handlers' <code>handleRequest()</code> methods had returned true. 
   *         Returns false when a handler had returned false. Handlers' invocation has been terminated. 
   *         This handler's position is preserved and subsequent call of <code>handleResponse()</code> method will start from this position.
   * 
   * @throws JAXRPCException in case such it thrown in any of handlers' <code>handleRequest()</code> methods.
   * @throws SOAPFaultException in case such it thrown in any of handlers' <code>handleRequest()</code> methods.
   */
  public boolean handleRequest(MessageContext ctx) throws JAXRPCException, SOAPFaultException {
    SOAPMessageContextImpl soapCtx = (SOAPMessageContextImpl) ctx;
    setRolesInContext(soapCtx);
    
    location.debugT("HandlerChainImpl.handleRequest() number of handlers in the chain: '" + this.size() + "'");
    Handler h;
    boolean hRes;
    for (int i = 0; i < this.size(); i++) {
      h = (Handler) this.get(i);
      position = i; 
      try {
        if (location.beDebug()) {
          location.debugT("HandlerChainImpl.handleRequest() about to invoke handler: '" + h + "'");
        }
        hRes = h.handleRequest(soapCtx);        
      } catch (RuntimeException e) {
        if (e instanceof SOAPFaultException) { //check JSR109, section 6.2.2.1
          throw (SOAPFaultException) e;
        } else {
          if (location.beDebug()) {
            location.debugT("HandlerChainImpl.handleRequest(): destroy handler " + h);
          }
          h.destroy();
          createAndReplaceHandler(position);
          throw e;
        }
      }
      if (! hRes) {
        return false;  
      }
    }
    return true;
  }
  /**
   * Invokes handlers' <code>handleResponse()</code> methods. Invocation starts from 
   * handler which is on position denoted by internal position variable.
   * @return true if all handlers' <code>handleRequest()</code> methods had returned true.
   *         Returns false when a handler had returned false. Handlers' invocation has been terminated. 
   * @throws JAXRPCException in case such it thrown in any of handlers' <code>handleRequest()</code> methods.
   */
  public boolean handleResponse(MessageContext ctx) throws JAXRPCException {
    SOAPMessageContextImpl soapCtx = (SOAPMessageContextImpl) ctx;

    location.debugT("HandlerChainImpl.handleResponse() number of handlers in the chain: '" + this.size() + "'");
    Handler h;
    boolean hRes;
    for (int i = position; i >= 0; i--) {
      h = (Handler) this.get(i);
      try {
        if (location.beDebug()) {
          location.debugT("HandlerChainImpl.handleResponse() about to invoke handler: '" + h + "'");
        }
        hRes = h.handleResponse(soapCtx);
      } catch (RuntimeException e) {
        if (e instanceof SOAPFaultException) { //check JSR109, section 6.2.2.1
          throw (SOAPFaultException) e;
        } else {
          if (location.beDebug()) {
            location.debugT("HandlerChainImpl.handleResponse(): destroy handler " + h);
          }
          h.destroy();
          createAndReplaceHandler(position);
          throw e;
        }
      }
      if (! hRes) {
        return false;            
      }
    }
    return true;
  }
  /**
   * Invokes handlers' <code>handleFault()</code> methods. Invocation starts from 
   * handler which is on position denoted by internal position variable.
   * @return true if all handlers' <code>handleFault()</code> methods had returned true.
   *         Returns false when a handler had returned false. Handlers' invocation has been terminated.
   * @throws JAXRPCException in case such it thrown in any of handlers' <code>handleRequest()</code> methods.
   */
  public boolean handleFault(MessageContext ctx) {
    SOAPMessageContextImpl soapCtx = (SOAPMessageContextImpl) ctx;

    location.debugT("HandlerChainImpl.handleFault() number of handlers in the chain: '" + this.size() + "'");

    Object o;
    Handler h;
    boolean hRes;
    for (int i = position; i >= 0; i--) {
      o = this.get(i);
      h = (Handler) o;
      try {
        if (location.beDebug()) {
          location.debugT("HandlerChainImpl.handleFault() about to invoke handler: '" + h + "'");
        }
        hRes = h.handleFault(soapCtx);
      } catch (RuntimeException e) {
        if (e instanceof SOAPFaultException) { //check JSR109, section 6.2.2.1
          throw (SOAPFaultException) e;
        } else {
          if (location.beDebug()) {
            location.debugT("HandlerChainImpl.handleFault(): destroy handler " + h);
          }
          h.destroy();
          createAndReplaceHandler(position);          
          throw e;
        }
      }
      if (! hRes) {
        return false;            
      }
    }
    return true;
  }
  
  public void clearState() {
    this.position = -1;
  }
  public void init(Map arg0) {
    throw new UnsupportedOperationException();
  }
  
  public void destroy() {
    Handler h;
    for (int i = 0; i < this.size(); i++) {
      h = (Handler) this.get(i);
      try {
        if (location.beDebug()) {
          location.debugT("HandlerChainImpl.destroy(): destroy handler " + h);
        }
        h.destroy();
      } catch (Throwable e) {
        ExceptionManager.traceThrowable(Severity.INFO, location, "Exception in destroying handler " + h, e);
      }
    }
  }
  
  /**
   * Checks whether there are set roles[], and if not sets them.
   */
  private void setRolesInContext(SOAPMessageContextImpl ctx) {
    if (ctx.getRoles() == null) {
      ctx.setRoles(getRoles());
    }
  }
  
  private void validateHandler(Object hInstance) {
    if (! (hInstance instanceof Handler)) {
      IllegalArgumentException e = new IllegalArgumentException("An attempt to set object which is not handler. Found: " + hInstance);
      ExceptionManager.logThrowable(Severity.DEBUG, null, location, "validateHandler", e);
      throw e;
    }
  }
  
  private void validateHandlerList(List handlers) {
    for (int i = 0; i < handlers.size(); i++) {
      validateHandler(handlers.get(i));
    }
  }
  /**
   * Creates and initializes new handlers instance, and places it into handlers chain on <code>hPos</code>.
   */
  private void createAndReplaceHandler(int hPos) {
    try {
      HandlerInfoImpl hInfo = (HandlerInfoImpl) handlerInfos.get(hPos);
      Handler h = (Handler) hInfo.getHandlerClass().newInstance();
      //initializes the handler
      h.init(hInfo);
      //set the handler into the chain
      this.remove(hPos);
      this.add(hPos, h);      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
} 
