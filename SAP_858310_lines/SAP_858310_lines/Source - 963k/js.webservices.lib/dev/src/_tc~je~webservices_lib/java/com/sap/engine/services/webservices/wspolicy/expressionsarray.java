/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.wspolicy;

import java.util.*;

/**
 * Add javadoc...
 *  
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public class ExpressionsArray {
	
	private List array = new ArrayList();
	
  public Expression get(int index) {
    return (Expression) array.get(index);
  }
  
  public Expression removeExpression(int index) {
    return (Expression) array.remove(index);
  }
  
  public void add(Expression exp) {
  	array.add(exp);
  }
  
  public Expression replaceExpression(int index, Expression exp) {
    Expression old = (Expression) array.remove(index);
    array.add(index, exp);
    return old;
  }
  
  public int size() {
  	return array.size();
  }
  
  public void addAll(ExpressionsArray expArr) {
  	for (int i = 0; i < expArr.size(); i++) {
  		add(expArr.get(i));
  	}
  }
  
  public Expression remove(int i) {
  	return (Expression) array.remove(i);
  }
  
  public void clear() {
  	array.clear();
  }
  
}
