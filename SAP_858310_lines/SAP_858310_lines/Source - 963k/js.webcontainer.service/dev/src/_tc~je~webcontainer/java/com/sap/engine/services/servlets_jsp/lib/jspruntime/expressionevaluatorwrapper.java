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

import javax.servlet.jsp.el.*;
import javax.el.ValueExpression;

/**
 * Implementation of already deprecated ExpressionEvaluator that is used in JSP2.0.
 */
public class ExpressionEvaluatorWrapper extends ExpressionEvaluator {
  private PageContextImpl pageContext;

  public ExpressionEvaluatorWrapper(PageContextImpl pageContext) {
    this.pageContext = pageContext;
  }

  public Expression parseExpression(String expression, Class toType, FunctionMapper functionMapper) throws javax.servlet.jsp.el.ELException {
    javax.el.ValueExpression result = null;
    //fake ELContext
    ELContextImpl elContext = new ELContextImpl(null);
    elContext.setFunctionMapper(new FunctionMapperWrapper(functionMapper));
    try {
      result = pageContext.getJspApplicationContext().getExpressionFactory().createValueExpression(elContext, expression, toType);
    } catch (javax.el.ELException ex) {
      throw new javax.servlet.jsp.el.ELException(ex);
    }
    return new ExpressionWrapper(result, pageContext);
  }

  public Object evaluate(String expression, Class toClass, VariableResolver variableResolver, FunctionMapper functionMapper) throws javax.servlet.jsp.el.ELException {
    //todo for custom resolvers should use ELResolver wrapper
    javax.el.FunctionMapper functionMapperWrapper = new FunctionMapperWrapper(functionMapper);
    ELContextImpl elContext = (ELContextImpl) pageContext.getELContext();
    elContext.setFunctionMapper(functionMapperWrapper);
    Object value = null;
    try {
      ValueExpression valueExpr = pageContext.getJspApplicationContext().getExpressionFactory().createValueExpression(elContext, expression, toClass);
      value = valueExpr.getValue(elContext);
    } catch (javax.el.ELException ex) {
      throw new javax.servlet.jsp.el.ELException(ex);
    }
    return value;
  }
}
