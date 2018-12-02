/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.jspruntime;

import com.sun.el.ExpressionFactoryImpl;
import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.JspIllegalStateException;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;

import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletContext;
import javax.el.*;
import java.util.ArrayList;

public class JspApplicationContextImpl implements JspApplicationContext {


  static ExpressionFactory expressionFactory = new ExpressionFactoryImpl();
  private ArrayList resolvers = new ArrayList();
  private ELContextListener[] listeners = null;
  private static ConcurrentHashMapObjectObject cache = new ConcurrentHashMapObjectObject();
  private boolean firstRequest = false;

  public JspApplicationContextImpl() {
    firstRequest = false;
  }
  /**
   *  In order of appearance
   */
  public void addELResolver(ELResolver elResolver) {
    if (firstRequest) {
      throw new JspIllegalStateException(JspIllegalStateException.ADD_RESOLVER_CALLED_AFTER_FIRST_REQUEST);
    }
    resolvers.add(0,elResolver);
  }

  public void addELContextListener(ELContextListener elContextListener) {
    if (listeners == null) {
      listeners = new ELContextListener[] {elContextListener};
    } else {
      ELContextListener[] newListeners = new ELContextListener[listeners.length + 1];
      System.arraycopy(listeners, 0, newListeners, 0, listeners.length);
      newListeners[listeners.length] = elContextListener;
      listeners = newListeners;
    }
  }

  public void setELContextListeners(ELContextListener[] elContextListeners) {
    listeners = elContextListeners;
  }

  public ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }

  protected Object[] getElResolvers() {
    return resolvers.toArray();
  }

  /**
   * Creates ELContext associated with given  CompositeELResolver
   * @param compositeELResolver
   * @return  ELContext
   */
  protected ELContext createELContext(ELResolver compositeELResolver, PageContext pageCtx) {
    ELContext elContext = new ELContextImpl(compositeELResolver);
    elContext.putContext(javax.servlet.jsp.JspContext.class, pageCtx);
    ((ELContextImpl) elContext).setVariableMapper(new VariableMapperImpl());
    if (listeners != null) {
      for (ELContextListener elListener : listeners) {
        elListener.contextCreated(new ELContextEvent(elContext));
      }
    }
    return elContext;
  }

  /**
   * Gets chached  JspApplicationContextImpl for given SerletContext
   * @param sc ServletContext , its getContextPath() is used for cache key
   * @return JspApplicationContextImpl
   */
  protected static JspApplicationContextImpl getJspApplicationContext(ServletContext sc) {
    JspApplicationContextImpl jspAppCtx = (JspApplicationContextImpl)cache.get(sc.getContextPath());
    if (jspAppCtx == null) {
      jspAppCtx = new JspApplicationContextImpl();
      cache.put(sc.getContextPath(), jspAppCtx);
    }
    return jspAppCtx;
  }

  /**
   * Cached object should be removed when application is going to stop/remove
   * @param sc   ServletContext , its getContextPath() is used for cache key
   */
  public void removeFromCache(ServletContext sc) {
    cache.remove(sc.getContextPath());
  }

  protected void setFirstRequest(boolean flag) {
    this.firstRequest = flag;
  }
}
