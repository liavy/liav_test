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

import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.PageContext;
import javax.el.ELContext;

/**
 * Implements already deprecated VariableResolver , that is used in JSP2.0
 * Uses ELResolver for variable resolving.
 */
public class VariableResolverWrapper implements VariableResolver {
  private PageContext pageCtx;

  public VariableResolverWrapper(PageContext pageCtx) {
    this.pageCtx = pageCtx;
  }

  public Object resolveVariable(String variable) throws javax.servlet.jsp.el.ELException {
    ELContext elContext = pageCtx.getELContext();
    try {
      return elContext.getELResolver().getValue(elContext, null, variable);
    } catch (javax.el.ELException ex) {
      throw new javax.servlet.jsp.el.ELException();
    }
  }
}
