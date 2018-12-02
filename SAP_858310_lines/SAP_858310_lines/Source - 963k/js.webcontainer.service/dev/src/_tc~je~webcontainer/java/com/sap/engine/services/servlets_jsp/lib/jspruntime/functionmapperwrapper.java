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

import javax.servlet.jsp.el.FunctionMapper;
import java.lang.reflect.Method;

public class FunctionMapperWrapper extends javax.el.FunctionMapper {
  private FunctionMapper functionMapper;

  public FunctionMapperWrapper(FunctionMapper functionMapper){
    this.functionMapper = functionMapper;
  }
  public Method resolveFunction(String prefix, String localName) {
    return functionMapper.resolveFunction(prefix, localName);
  }
}
