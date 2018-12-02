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

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

public class ELContextImpl extends ELContext {

  private FunctionMapper functionMapper = null;
  private VariableMapper variableMapper = null;
  private ELResolver resolver = null;
  
  /**
   * Constructs new ELContextImpl with given CompositeELResolver
   */
  public ELContextImpl(ELResolver compositeELResolver) {
    resolver = compositeELResolver;
  }

  public ELResolver getELResolver() {
    return resolver;
  }

  public FunctionMapper getFunctionMapper() {
    return functionMapper;
  }

  public VariableMapper getVariableMapper() {
    return variableMapper;
  }

  public void setELResolver(ELResolver compositeELResolver) {
    resolver = compositeELResolver;
  }

  public void setFunctionMapper(FunctionMapper functionMapper) {
    this.functionMapper = functionMapper;
  }

  public void setVariableMapper(VariableMapper variableMapper) {
    this.variableMapper = variableMapper;
  }
}
