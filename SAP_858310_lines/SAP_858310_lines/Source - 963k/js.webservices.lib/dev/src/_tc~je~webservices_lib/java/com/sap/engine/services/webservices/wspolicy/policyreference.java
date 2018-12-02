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
 * Add javadoc...
 *  
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public class PolicyReference implements Expression {

  /* (non-Javadoc)
   * @see wspolicy.Expression#evaluate()
   */
  public Expression evaluate() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see wspolicy.Expression#getType()
   */
  public int getType() {
    // TODO Auto-generated method stub
    return 0;
  }

	public Element attachToParent(Element root) {
		Element polRef = root.getOwnerDocument().createElementNS(Policy.POLICY_NS, Policy.POLICY_PREFIX + ":" + Policy.POLICYREFERENCE_ELEMENT);
		root.appendChild(polRef);		
		return polRef;
	}
	
  public void print(int offset) {
//    StringBuffer s = new StringBuffer();
//    for (int i = 0; i < offset; i++) {
//      s.append(" "); 
//    }
//    System.out.println(s.toString() + "[ALL]");
//    for (int i = 0; i < expressions.size(); i++) {
//      expressions.getExpression(i).print(offset + 1);
//    }
  }

	/* (non-Javadoc)
	 * @see wspolicy.Expression#getChildExpressions()
	 */
	public ExpressionsArray getChildExpressions() {
		return null;
	}

}
