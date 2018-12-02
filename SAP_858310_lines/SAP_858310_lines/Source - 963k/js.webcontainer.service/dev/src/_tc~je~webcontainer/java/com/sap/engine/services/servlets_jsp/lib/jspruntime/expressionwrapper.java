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

import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.PageContext;
import javax.el.ValueExpression;
import javax.el.ELContext;

public class ExpressionWrapper extends Expression {
  private ValueExpression valueExpression;
  private PageContext pageContext;

  public ExpressionWrapper(ValueExpression valueExpr, PageContext pageContext) {
    this.valueExpression = valueExpr;
    this.pageContext = pageContext;
  }

  public Object evaluate(VariableResolver variableResolver) throws javax.servlet.jsp.el.ELException {
    //todo for custom resolvers should use ELResolver wrapper
//    ELContext elContext;
//    if (variableResolver instanceof VariableResolverWrapper) {
//      elContext = pageContext.getELContext();
//    } else {
//      //custom resolver
//      elContext = new ELContextImpl(wrapper(vaiableresolver));
//    }
    try {
      return valueExpression.getValue(pageContext.getELContext());
    } catch (javax.el.ELException ex) {
      throw new javax.servlet.jsp.el.ELException(ex);
    }
  }
}
