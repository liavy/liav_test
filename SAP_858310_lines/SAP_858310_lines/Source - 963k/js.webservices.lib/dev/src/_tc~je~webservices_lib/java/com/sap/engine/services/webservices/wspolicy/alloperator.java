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

import org.w3c.dom.Element;

/**
 * @author Dimitar Angelov
 *
 */
public class AllOperator implements Expression {
  
  public final static int ALL_EXPRESSION_TYPE  =  2;
    
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
      
    ExpressionsArray newExpressions = new ExpressionsArray();
    //association
    for (int i = 0; i < expressions.size(); i++) {
      cur = expressions.get(i);
      if (cur.getType() == ALL_EXPRESSION_TYPE) {
        AllOperator mult = (AllOperator) cur;
        for (int j = 0; j < mult.expressions.size(); j++) {
          newExpressions.addAll(mult.expressions);      
        }
      } else {
        newExpressions.add(cur);
      }
    }
        
    expressions = newExpressions;
    
    //distribution
    ExactlyOneOperator first = null;
    for (int i = 0; i < expressions.size(); i++) {    //  finding the first ExaclyOneExpression 
      cur = expressions.get(i);
      if (cur.getType() == ExactlyOneOperator.EXACTLYONE_EXPRESSION_TYPE) {
        first = (ExactlyOneOperator) expressions.get(i);
        break;
      }
    }
    
    //in case no ExaclyOneExpressions are found we return this obect wrapped into ExactlyOne
    if (first == null) {
      first = new ExactlyOneOperator();
      first.getChildExpressions().add(this);
      return first;
    }
    
    for (int i = 0; i < expressions.size(); i++) { //multiply the free Operands with the found expression
      cur = expressions.get(i);
      if (cur.getType() == Assertion.ASSERTION_EXPRESSION_TYPE) {
        mult(cur, first);
        expressions.remove(i--);
      }
    }
    
    ExactlyOneOperator a, b;
    while (expressions.size() > 1) { //multiply the ExaclyOneExpressions
      a = (ExactlyOneOperator) expressions.remove(0);
      b = (ExactlyOneOperator) expressions.remove(0);
      expressions.add(mult(a, b));  
    }
    //return the final ExactlyOne expression
    return expressions.get(0);
  }

  /* (non-Javadoc)
   * @see wspolicy.Expression#getType()
   */
  public int getType() {
    return ALL_EXPRESSION_TYPE;
  }

	/* (non-Javadoc)
	 * @see wspolicy.Expression#getChildExpressions()
	 */
	public ExpressionsArray getChildExpressions() { 
		return this.expressions;
	}
  
    
//  public void print(int offset) {
//    StringBuffer s = new StringBuffer();
//    for (int i = 0; i < offset; i++) {
//      s.append(" "); 
//    }
//    System.out.println(s.toString() + "[ALL]");
//    for (int i = 0; i < expressions.size(); i++) {
//      expressions.get(i).print(offset + 1);
//    }
//  }

	public Element attachToParent(Element root) {
		Element all = root.getOwnerDocument().createElementNS(Policy.POLICY_NS, Policy.POLICY_PREFIX + ":" + Policy.ALL_ELEMENT);
		for (int i = 0; i < expressions.size(); i++) {
			expressions.get(i).attachToParent(all);			  
		}
		root.appendChild(all);
		
		return all;
	}  
	
  /**
   * Checks whether this alternative is compatible with <code>ext</code>.
   * @param op
   * @return
   */
  boolean isCompatibleWith(AllOperator ext) throws PolicyException {
		for (int i = 0; i < expressions.size(); i++) {
			if (expressions.get(i).getType() != Assertion.ASSERTION_EXPRESSION_TYPE) {
				throw new PolicyException(PolicyException.NOT_NORMALIZE_EXPRESSION);   		
			} 
		}		
		ExpressionsArray extArr = ext.getChildExpressions();
		for (int i = 0; i < extArr.size(); i++) {
			if (extArr.get(i).getType() != Assertion.ASSERTION_EXPRESSION_TYPE) {
				throw new PolicyException(PolicyException.NOT_NORMALIZE_EXPRESSION);   		
			}
		}	
    
    ExpressionsArray thisArr = this.getChildExpressions();
    //check whether each assertion in thisArr has corresponding compatible in extArr
    for (int i = 0; i < thisArr.size(); i++) {
      Assertion cur = (Assertion) thisArr.get(i);
      int j = 0;
      for (; j < extArr.size(); j++) {
        Assertion extCur = (Assertion) extArr.get(j);
        if (cur.isCompatibleWith(extCur)) {
          break;
        }
      }
      if (j == extArr.size()) { //not compatible assertion is found
        return false;
      }
    }
      
    //check whether each assertion in extArr has corresponding compatible in thisArr
    for (int i = 0; i < extArr.size(); i++) {
      Assertion extCur = (Assertion) extArr.get(i);
      int j = 0;
      for (; j < thisArr.size(); j++) {
        Assertion cur = (Assertion) thisArr.get(j);
        if (extCur.isCompatibleWith(cur)) {
          break;
        }
      }
      if (j == thisArr.size()) { //not compatible assertion is found
        return false;
      }
    }
    
    return true;
	}
  /**
   * Returns true if the vocabulary of this alternative is the same
   * as <code>ext</code>;
   * @param ext
   * @return
   * @throws PolicyException
   */
  public boolean isVocabularySame(AllOperator ext) throws PolicyException {
    for (int i = 0; i < expressions.size(); i++) {
      if (expressions.get(i).getType() != Assertion.ASSERTION_EXPRESSION_TYPE) {
        throw new PolicyException(PolicyException.NOT_NORMALIZE_EXPRESSION);      
      } 
    }   
    ExpressionsArray opArr = ext.getChildExpressions();
    for (int i = 0; i < opArr.size(); i++) {
      if (opArr.get(i).getType() != Assertion.ASSERTION_EXPRESSION_TYPE) {
        throw new PolicyException(PolicyException.NOT_NORMALIZE_EXPRESSION);      
      }
    } 
    
    Set set1 = new HashSet();
    for (int i = 0; i < expressions.size(); i++) {
      set1.add(((Assertion) expressions.get(i)).getAssertionNS() 
          + ((Assertion) expressions.get(i)).getAssertionLocalName());
    }   
    
    Set set2 = new HashSet();
    for (int i = 0; i < opArr.size(); i++) {
      set2.add(((Assertion) opArr.get(i)).getAssertionNS() 
          + ((Assertion) opArr.get(i)).getAssertionLocalName());
    }
    
    return set1.equals(set2);
  }

  //The exp param is returned updated with the 
  private ExactlyOneOperator mult(Expression op, ExactlyOneOperator exp) {
    AllOperator curMult, newMult;
    
    if (op.getType() == Assertion.ASSERTION_EXPRESSION_TYPE) {
      for (int i = 0; i < exp.getChildExpressions().size(); i++) {
        curMult = (AllOperator)exp.getChildExpressions().get(i);
        curMult.getChildExpressions().add(op);
      }
      return exp;
    }
    
    ExactlyOneOperator res = new ExactlyOneOperator();
    ExactlyOneOperator curAdd = (ExactlyOneOperator) op;
    for (int i = 0; i < curAdd.getChildExpressions().size(); i++) {
      curMult = (AllOperator)curAdd.getChildExpressions().get(i);
      for (int j = 0; j < exp.getChildExpressions().size(); j++) {
        newMult = new AllOperator();
        newMult.expressions.addAll(curMult.expressions);
        newMult.expressions.addAll(((AllOperator) exp.getChildExpressions().get(j)).expressions);
        res.getChildExpressions().add(newMult);      
      }
    }  
        
    return res;
  }

}
