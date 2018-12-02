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

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import javax.servlet.*;
import javax.servlet.jsp.*;
import com.sap.engine.lib.lang.ObjectPool;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Location;

import java.io.IOException;

/**
 * The JspFactory is an abstract class that defines a number of factory methods available to a JSP page
 * at runtime for the purposes of creating instances of various interfaces and classes
 * used to support the JSP implementation.
 *
 */
public class JspFactoryImpl extends JspFactory {
  private static Location currentLocation = Location.getLocation(JspFactoryImpl.class);
  private static ObjectPool pagePool = null;

  /**
   * Construct new JspFactoryImpl
   *
   */
  public JspFactoryImpl(int minPoolSize, int maxPoolSize, int decreaseCapacity) {
    pagePool = new ObjectPool(minPoolSize, maxPoolSize, decreaseCapacity, PageContextImpl.class);
  }

  /**
   * Construct and initialize the PageContext for servlet
   *
   * @param   servlet  the servlet
   * @param   servletrequest  servlet request
   * @param   servletresponse  servlet response
   * @param   s  errorPageURL
   * @param   flag  needsSession
   * @param   i  buffer length for writer
   * @param   flag1  autoFlush
   * @return     new PageContext
   */
  public PageContext getPageContext(Servlet servlet, ServletRequest servletrequest, ServletResponse servletresponse,
                                    String s, boolean flag, int i, boolean flag1) {
    PageContextImpl pagecontextimpl = (PageContextImpl) pagePool.getObject();
    if (pagecontextimpl == null) {
      pagecontextimpl = new PageContextImpl();
    }
    try {
    pagecontextimpl.initialize(servlet, servletrequest, servletresponse, s, flag, i, flag1);
    } catch (IOException io) {
    	//TODO:Polly type:ok content - probably the servlet should be also specified
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000257",
          "Cannot initialize a page context instance.", io, null, null);
    }
    return pagecontextimpl;
  }

  /**
   * Release current PageContext
   *
   * @param   pagecontext  PageContext to be released
   */
  public void releasePageContext(PageContext pagecontext) {
    pagecontext.release();
    pagePool.returnInPool(pagecontext);
  }

  /**
   * called to get implementation-specific information on the current JSP engine
   *
   * @return a JspEngineInfo object describing the current JSP engine
   */
  public JspEngineInfo getEngineInfo() {
    return new SapJspEngineInfo();
  }
  
  /**
   * Obtains the JspApplicationContext instance associated with the web application for the given ServletContext.
   * @return a JspApplicationContext
   */
  public JspApplicationContext getJspApplicationContext(ServletContext context){
	  return JspApplicationContextImpl.getJspApplicationContext(context);
  }

}

