/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;

/**
 *  This interface provides utility method for accessing different objects associated
 * with current request to a web service. An instance implementing this interface is bound in the
 * naming under "wsContext" subcontext.
 * 
 * @author       Dimiter Angelov
 * @version      6.30
 */
public interface ApplicationWebServiceContext {
  /**
   *  The JNDI name under which the interface implementation is bound under
   * the "wsContext" subcontext.
   */
  public static final String APPLICATION_WSCONTEXT  =  "ApplicationWebServiceContext";
  /**
   * @return HttpServletRequest instance associated with the http request.
   * 
   * @throws java.lang.IllegalStateException in case the web service is accessed via protocol different than http, or
   *                                         current request is one-way.
   */
  public HttpServletRequest getHttpServletRequest();
  /**
   * @return HttpServletResponse instance associated with the http request.
   *  
   * @throws java.lang.IllegalStateException  in case the web service is accessed via protocol different than http, or
   *                                          current request is one-way.    
   */
  public HttpServletResponse getHttpServletResponse();
  /**
   * @return List of org.w3c.dom.Element instances, which represent SOAP headers associated with request SOAP message.
   * 
   * @throws java.lang.IllegalStateException  in case the request message is not a SOAP message. 
   */
  public List getRequestSOAPHeaders();
  /**
   * Adds <code>header</code> to the SOAP headers list of response SOAP message.
   *
   * @param header self-contained element object. The element must not refer to XML entities (like prefix declarations, etc...) 
   *               outside the element itself.
   *  
   * @throws java.lang.IllegalStateException  in case the response message is not a SOAP message, or current request is one-way. 
   */
  public void addResponseSOAPHeader(Element header);
}