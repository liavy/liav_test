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

import javax.servlet.*;
import java.util.Enumeration;
import com.sap.engine.lib.util.HashMapObjectObject;

/**
 * The FilterConfigImpl implements javax.servlet.FilterConfig
 *
 * @author Boby Kadrev
 * @version 4.0
 */
public class FilterConfigImpl implements FilterConfig {
  private String filterName = null;
  private Filter filter = null;
  private ServletContext servletContext = null;
  private HashMapObjectObject initParameters = null;

  /**
   * Constructs new FilterConfigImpl with given ServletContext and FilterDescriptor.
   *
   * @param   filter  associated ServletContextFacade
   * @param   initParameters  FilterDescriptor containig all information from parsed descriptor
   */
  public FilterConfigImpl(String filterName, Filter filter, ServletContext servletContext, HashMapObjectObject initParameters) {
    this.filterName = filterName;
    this.filter = filter;
    this.servletContext = servletContext;
    this.initParameters = initParameters;
  }

  /**
   * Return the name of the filter.
   */
  public String getFilterName() {
    return filterName;
  }

  /**
   * Return a String, containing the value of the named parameter
   *
   * @param name Name of the requested initialization parameter
   */
  public String getInitParameter(String name) {
    return (String) initParameters.get(name);
  }

  /**
   * Return an Enumeration of the names of the initialization
   * parameters for this Filter.
   *
   * @return  Enumeration with parameter names
   */
  public Enumeration getInitParameterNames() {
    return initParameters.keys();
  }

  /**
   * Return the ServletContext of associated web application.
   *
   * @return  ServletContext of associated web application.
   */
  public ServletContext getServletContext() {
    return servletContext;
  }

  protected Filter getFilter() {
    return filter;
  }

  /**
   * Return a String representation of this object.
   *
   * @return  String representation of this object.
   */
  public String toString() {
    return ("FilterConfigImpl[name=" + filterName + "]");
  }

}

