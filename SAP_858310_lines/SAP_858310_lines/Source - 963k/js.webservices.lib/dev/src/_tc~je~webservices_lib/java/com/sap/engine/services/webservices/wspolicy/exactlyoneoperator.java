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
public class ExactlyOneOperator implements Expression {
  
  public static final int EXACTLYONE_EXPRESSION_TYPE  =  4;
  
  private ExpressionsArray expressions = new ExpressionsArray();

  /* (non-Javadoc)
   * @see wspolicy.Expression#evaluate()
   */
  public Expression evaluate() throws PolicyException {
    Expression cur;
    for (int i = 0; i < expressions.size(); i++) {
      cur = expressions.get(i);
      expressions.replaceExpression(i, cur.evaluate());
    }
    
    ExpressionsArray newOperands = new ExpressionsArray();
    for (int i = 0; i < expressions.size(); i++) {
      cur = expressions.get(i);
      //    association
      if (cur.getType() == EXACTLYONE_EXPRESSION_TYPE) { 
        ExactlyOneOperator add = (ExactlyOneOperator) cur;
//        for (int j = 0; j < add.expressions.size(); j++) {
          newOperands.addAll(add.expressions);      
//        }
      } else if (cur.getType() == Assertion.ASSERTION_EXPRESSION_TYPE) { //wrapping the single  Expression into AllExpression
        AllOperator mE= new AllOperator();
        mE.getChildExpressions().add(cur);
        newOperands.add(mE);       
      } else {
        newOperands.add(cur);
      }
    }
    expressions = newOperands;

    return this;    
  }

  /* (non-Javadoc)
   * @see wspolicy.Expression#getType()
   */
  public int getType() {
    return EXACTLYONE_EXPRESSION_TYPE;
  }

	/* (non-Javadoc)
	 * @see wspolicy.Expression#getChildExpressions()
	 */
	public ExpressionsArray getChildExpressions() {
		return this.expressions;
	}
  
  /**
   * Returns newly created ExpressionsArray
   * referencing the All operators nested into
   * this object(only high level operators).
   * @return
   */
  public ExpressionsArray getAllOperators() {
  	ExpressionsArray arr = new ExpressionsArray();
  	for (int i = 0; i < expressions.size(); i++) {
  		if (expressions.get(i).getType() == AllOperator.ALL_EXPRESSION_TYPE) {
				arr.add(expressions.get(i));
  		}
  	}
  	return arr;
  }
  
//  public void print(int offset) {
//    StringBuffer s = new StringBuffer();
//    for (int i = 0; i < offset; i++) {
//      s.append(" "); 
//    }
//    System.out.println(s.toString() + "[ExactlyOne]");
//    for (int i = 0; i < expressions.size(); i++) {
//      expressions.get(i).print(offset + 1);
//    }
//  }

	public Element attachToParent(Element root) {
		Element exOne = root.getOwnerDocument().createElementNS(Policy.POLICY_NS, Policy.POLICY_PREFIX + ":" + Policy.EXACTLYONE_ELEMENT);
		for (int i = 0; i < expressions.size(); i++) {
			expressions.get(i).attachToParent(exOne);			  
		}
		root.appendChild(exOne);
		
		return exOne;
	}  

}
