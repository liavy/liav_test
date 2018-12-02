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

import org.w3c.dom.Element;

/**
 * @author Dimitar Angelov
 *
 */
public interface Expression {
  
  public Expression evaluate() throws PolicyException;
  
  public int getType();
  
  public ExpressionsArray getChildExpressions();
  
  public Element attachToParent(Element parent);
}
