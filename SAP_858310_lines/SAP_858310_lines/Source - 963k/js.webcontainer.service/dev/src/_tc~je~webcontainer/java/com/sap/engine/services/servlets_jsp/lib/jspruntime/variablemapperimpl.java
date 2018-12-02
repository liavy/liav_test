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

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;

import javax.el.VariableMapper;
import javax.el.ValueExpression;

public class VariableMapperImpl extends VariableMapper {

  private ConcurrentHashMapObjectObject map = null;

  public VariableMapperImpl() {
    map = new ConcurrentHashMapObjectObject();
  }

  public ValueExpression resolveVariable(String varName) {
    return (ValueExpression) map.get(varName);
  }

  public ValueExpression setVariable(String varName, ValueExpression valueExpression) {
    if (valueExpression == null) {
      map.remove(varName);
      return null;
    } else {
      ValueExpression found = (ValueExpression) map.get(varName);
      map.put(varName, valueExpression);
      return found;
    }
  }
}
