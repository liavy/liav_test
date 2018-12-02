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
package com.sap.engine.services.servlets_jsp.server.runtime;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import com.sap.engine.services.servlets_jsp.server.lib.EmptyEnumeration;
import com.sap.engine.session.DomainReference;

import java.util.*;
import java.io.Serializable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * A servlet configuration object used by a servlet container used to pass information to a servlet
 * during initialization. The configuration information contains initialization parameters,
 * which are a set of name/value pairs, and a ServletContext object, which gives the servlet
 * information about the server.
 *
 */
public class ServletConfigImpl implements ServletConfig, Serializable {

  public static final String SERVLET_CONTEXT = "javax.servlet.ServletContext";
  public static final String SERVLET_INIT_PARAMETERS = "javax.serlet.InitParam";

  private static final Enumeration empty_enumeration = new EmptyEnumeration();

  /**
   * Name of the servlet
   */
  private String servletName = null;
  private String initParameterKey;

  private DomainReference container;

  private transient ServletContext context = null;
  private transient Hashtable initParameters = null;

  /**
   * Sets the initial parameters.
   *
   * @param   context1  ServletContextFacade for this application
   * @param   servletName1  name of the servlet
   */
  public ServletConfigImpl(ServletContext context1, String servletName1) {
    servletName = servletName1;
  }

  public ServletConfigImpl(DomainReference container, String servletName1, ServletContext context1) {
    this.container = container;
    servletName = servletName1;
    container.setLocalAttribute(SERVLET_CONTEXT, context1);
    initParameterKey = SERVLET_INIT_PARAMETERS +"." + servletName;
  }

  /**
   * Returns the name of the servlet.
   *
   * @return     name of the servlet
   */
  public String getServletName() {
    return servletName;
  }

  /**
   * Returns a reference to the ServletContext in which the servlet is executing.
   *
   * @return     reference to ServletContext
   */
  public ServletContext getServletContext() {
    if (context == null) {
      context = (ServletContext) container.getLocalAttribute(SERVLET_CONTEXT);
    }
    return context;
  }

  /**
   * Returns a String containing the value of the named initialization parameter,
   * or null if the parameter does not exist.
   *
   * @param   s  name of parameter
   * @return     the value of the parameter
   */
  public String getInitParameter(String s) {
    if (initParameters == null) {
      initParameters = (Hashtable)container.getLocalAttribute(initParameterKey);
    }
    if (initParameters == null) {
      return null;
    }
    return (String) initParameters.get(s);
  }

  /**
   * Returns the names of the servlet's initialization parameters as an Enumeration
   * of String objects, or an empty Enumeration if the servlet has no initialization parameters.
   *
   * @return     enumeration with all parameter names
   */
  public Enumeration getInitParameterNames() {
    if (initParameters == null) {
      initParameters = (Hashtable)container.getLocalAttribute(initParameterKey);
    }
    if (initParameters == null) {
      return empty_enumeration;
    }
    return initParameters.keys();
  }

  /**
   * Sets initial arguments.
   *
   * @param   hashtable  contains the arguments to be set
   */
  public void setInitParameters(Hashtable hashtable) {
    if (hashtable != null && !hashtable.isEmpty()) {
      container.setLocalAttribute(initParameterKey, hashtable);
    }
  }
}

