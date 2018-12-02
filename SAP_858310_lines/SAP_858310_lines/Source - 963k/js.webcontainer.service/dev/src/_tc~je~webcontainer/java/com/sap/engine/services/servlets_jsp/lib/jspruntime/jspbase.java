/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.jspruntime;

import com.sap.engine.services.servlets_jsp.server.runtime.ServletConfigImpl;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

/**
 * This is the interface that a JSP processor-generated class must satisfy.
 * The interface defines a protocol with 3 methods; only two of them: jspInit() and jspDestroy()
 * are part of this interface as the signature of the third method: _jspService()
 * depends on the specific protocol used and cannot be expressed in a generic way in Java.
 * A class implementing this interface is responsible for invoking the above methods at the
 * apropriate time based on the corresponding Servlet-based method invocations.
 *
 * @author  Maria Jurova
 */
public abstract class JspBase extends HttpServlet implements HttpJspPage {

  /**
   * A ServletConfig is passed to the servlet instance to give a jsp a configure parameters
   */
  private ServletConfigImpl config;

  /**
   * Construct JspBase
   *
   */
  public JspBase() {

  }

  /**
   * Init method for servlet.
   *
   * @param   servletconfig  ServletConfig for this servlet
   * @exception   ServletException  throws it
   */
  public final void init(ServletConfig servletconfig) throws ServletException {
    config = (ServletConfigImpl)servletconfig;
    super.init(servletconfig);
    jspInit();
  }

  /**
   * Returns a ServletConfig object, which contains initialization and startup parameters
   * for this servlet. The ServletConfig object returned is the one passed to the init method.
   *
   * @return     ServletConfig of the servlet
   */
  public final ServletConfig getServletConfig() {
    return config;
  }

  /**
   * Returns a reference to the ServletContext in which this servlet is running.
   *
   * @return     a refference to ServletContext
   */
  public final ServletContext getServletContext() {
    return config.getServletContext();
  }

  /**
   *Return servlet info
   *
   * @return     servlet info
   */
  public String getServletInfo() {
    return "JSP Servlet/SAP AS Java";
  }

  /**
   * Calls the destroy method ot jspServlet.
   *
   */
  public final void destroy() {
    jspDestroy();
  }

  /**
   * Default methoed of servlets.
   *
   * @param   req  A request to this servlet
   * @param   res  A response that will be returned from this servlet
   * @exception   ServletException  throws it
   * @exception   IOException  throws it
   */
  public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    _jspService((HttpServletRequest) req, (HttpServletResponse) res);
  }

  /**
   * Init method of jspServlet.
   *
   */
  public void jspInit() {

  }

  /**
   * Destroy method of jspServlet.
   *
   */
  public void jspDestroy() {

  }

  /**
   * Default methoed of generated servlets.
   *
   * @param   httpservletrequest  servlet request
   * @param   httpservletresponse  servlet response
   * @exception   ServletException  throws it
   * @exception   IOException  throws it
   */
  public abstract void _jspService(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse) throws ServletException, IOException;

}

