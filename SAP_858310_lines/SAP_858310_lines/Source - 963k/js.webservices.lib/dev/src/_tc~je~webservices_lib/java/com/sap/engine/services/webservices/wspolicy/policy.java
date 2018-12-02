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

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Add javadoc...
 *  
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public class Policy implements Expression {
	public static final String POLICY_ELEMENT  = "Policy";
	public static final String ALL_ELEMENT  = "All";
	public static final String EXACTLYONE_ELEMENT  = "ExactlyOne";
	public static final String POLICYREFERENCE_ELEMENT  = "PolicyReference";
	public static final String POLICY_NS  = "http://schemas.xmlsoap.org/ws/2004/09/policy";
  public static final String WS_SEC_UTILITY_NS  = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
  public static final String ID_ATTR = "Id";
	public static final String OPTIONAL_ATTRIBUTE  = "Optional";
	public static final String POLICY_PREFIX  = "wsp";
  public static final String POLICYREFERENCE_URI_ATTR  = "URI";
  public static final String USINGPOLICY_ELEMENT  = "UsingPolicy";
  public static final String POLICYURIS_ATTR  = "PolicyURIs";
  public static final String NAME_ATTR  = "Name";
    
   
  public static final int POLICY_EXPRESSION_TYPE = 8;
  
  private ExpressionsArray expressions = new ExpressionsArray();
  
  private boolean isNormalized;
  
  /* (non-Javadoc)
   * @see wspolicy.Expression#evaluate()
   */ 
  public Expression evaluate() throws PolicyException {
    //Creating all and evaluating it 
    AllOperator all = new AllOperator();
    all.getChildExpressions().addAll(expressions);
    return all.evaluate();
  }

  /* (non-Javadoc)
   * @see wspolicy.Expression#getType()
   */
  public int getType() {
    return POLICY_EXPRESSION_TYPE;
  }

	/* (non-Javadoc)
	 * @see wspolicy.Expression#getChildExpressions()
	 */
	public ExpressionsArray getChildExpressions() {
		return this.expressions;
	}

  public Policy normalize() throws PolicyException {
    //Policy p = new Policy();
    Expression ex = this.evaluate(); 
    expressions.clear();
    expressions.add(ex);
    isNormalized = true;
    return this;
  }
  
  public boolean isNormalized() {
    return isNormalized;
  }
  
//  public void print(int offset) {
//    StringBuffer s = new StringBuffer();
//    for (int i = 0; i < offset; i++) {
//      s.append(" "); 
//    }
//    System.out.println(s.toString() + "[Policy]");
//    for (int i = 0; i < expressions.size(); i++) {
//      expressions.get(i).print(offset + 1);
//    }
//  }

	public Element attachToParent(Element root) {
		Element policy = root.getOwnerDocument().createElementNS(Policy.POLICY_NS, POLICY_PREFIX + ":" + Policy.POLICY_ELEMENT);
		for (int i = 0; i < expressions.size(); i++) {
			expressions.get(i).attachToParent(policy);			  
		}
		root.appendChild(policy);
		
		return policy;
	}  
	
  public Element getPolicyAsDom(Document doc) throws PolicyException {
    return this.attachToParent(doc.createElementNS("test", "test"));
  }
  
	public Element getPolicyAsDom() throws PolicyException {
		try {
		  return this.getPolicyAsDom(SharedDocumentBuilders.newDocument());
		} catch (RuntimeException pcE) {
		  throw new PolicyException(null, pcE);
		}
	}  
  
  /**
   * List of Policy objects
   */
  public static Policy mergePolicies(List policies) throws PolicyException  {
    Policy p = new Policy();
    Policy curP;
    for (int i = 0; i < policies.size(); i++) {
      curP = (Policy) policies.get(i);
      //normalize first in order to check teh <exaclyOne/> case.
      curP.normalize();
      AllOperator all = new AllOperator();    
      all.getChildExpressions().addAll(curP.getChildExpressions());
      p.expressions.add(all);       
    }
    return p.normalize();      
  }
  
  public static Policy mergePolicies(Policy[] policies) throws PolicyException  {
    Policy p = new Policy();

    for (int i = 0; i < policies.length; i++) {
    	policies[i].normalize();
			AllOperator all = new AllOperator(); 		
    	all.getChildExpressions().addAll(policies[i].getChildExpressions());
      p.expressions.add(all);       
    }
    return p.normalize();
  }
  /**
   * @param policies
   * @return
   * @throws PolicyException
   */
  public static Policy intersectPolicies(Policy[] policies) throws PolicyException {
    if (policies == null || policies.length == 0) {
      return null;
    }
    if (policies.length == 1) {
      return policies[0].normalize();
    }
    
    Policy intersecPolicy = intersectPolicies(policies[0], policies[1]);
    for (int i = 2; i < policies.length; i++) {
      intersecPolicy = intersectPolicies(intersecPolicy, policies[i]);
    }
    
    return intersecPolicy;
    
//    //each policy needs to have only one operator exactlyOne because is normalized
//    ExpressionsArray exOneAlls = ((ExactlyOneOperator) policies[0].getChildExpressions().get(0)).getAllOperators();
//    ExpressionsArray tmpAlls;
//    ExactlyOneOperator resExOne = new ExactlyOneOperator();
//    
//    for (int p = 1; p < policies.length; p++) {
//    	tmpAlls = ((ExactlyOneOperator) policies[p].getChildExpressions().get(0)).getAllOperators();
//	    for (int i = 0; i < exOneAlls.size(); i++) {
//				int tmp = 0;
//				for (; tmp < tmpAlls.size(); tmp++) {
//					if (((AllOperator) exOneAlls.get(i)).isVocabularySame((AllOperator) tmpAlls.get(tmp))) {
//						AllOperator all = new AllOperator();
//						all.getChildExpressions().addAll(exOneAlls.get(i).getChildExpressions());
//						all.getChildExpressions().addAll(tmpAlls.get(tmp).getChildExpressions());
//		        resExOne.getChildExpressions().add(all);				
//					}
//				} 	
//	    }
//	    exOneAlls = resExOne.getChildExpressions();
//    }
//    
//		Policy intrsecPolicy = new Policy();
//		ExactlyOneOperator exOne = new ExactlyOneOperator();
//		exOne.getChildExpressions().addAll(exOneAlls);
//		intrsecPolicy.getChildExpressions().add(exOne);
//		
//		return intrsecPolicy;
  }
  
  public static Policy intersectPolicies(Policy p1, Policy p2) throws PolicyException {
    p1.normalize();
    p2.normalize();
    
    ExactlyOneOperator p1EO = (ExactlyOneOperator) p1.getChildExpressions().get(0);
    ExactlyOneOperator p2EO = (ExactlyOneOperator) p2.getChildExpressions().get(0);
    Policy res = new Policy();
    ExactlyOneOperator resExOne = new ExactlyOneOperator();
    res.getChildExpressions().add(resExOne);
    
    //else there should be only one alternative
    ExpressionsArray p1Alls = (ExpressionsArray) p1EO.getChildExpressions(); 
    ExpressionsArray p2Alls = (ExpressionsArray) p2EO.getChildExpressions();
    
    for (int i = 0; i < p1Alls.size(); i++) {
      AllOperator p1All = (AllOperator) p1Alls.get(i);
      for (int j = 0; j < p2Alls.size(); j++) {
        AllOperator p2All = (AllOperator) p2Alls.get(j);
        if (p1All.isCompatibleWith(p2All)) {
          //combine all assertions from the compatible alternatives into one alternative
          AllOperator newAll = new AllOperator();
          newAll.getChildExpressions().addAll(p1All.getChildExpressions());
          newAll.getChildExpressions().addAll(p2All.getChildExpressions());
          resExOne.getChildExpressions().add(newAll);
        }
      }
    }
    return res;
  }
 
  /**
   * Returns true if the policy is normalized. The assertions are not cheched, that is
   * if there is assertion with nested policy which policy is not normalized, 
   * but the <code>policy</code> is normalized, true will be returned.
   * @param policy
   * @return
   */
  public static boolean isPolicyNormalized(Element policy) {
    if ((! Policy.POLICY_NS.equals(policy.getNamespaceURI())) || (! Policy.POLICY_ELEMENT.equals(policy.getLocalName())) ) {
      throw new RuntimeException("The root element is not 'Policy'.");
    }
    List eo = DOM.getChildElementsByTagNameNS(policy, Policy.POLICY_NS, Policy.EXACTLYONE_ELEMENT);
    if (eo.size() != 1) {
      return false;
    }
    Element eoEl = (Element) eo.get(0);
    //check whether the element contains only All children
    List alls = DOM.getChildElementsByTagNameNS(eoEl, Policy.POLICY_NS, Policy.ALL_ELEMENT);
    Element allsArr[] = DOM.getChildElementsAsArray(eoEl);
    if (alls.size() != allsArr.length) {
      return false;
    }
    //in each All check whether it contains only assertions - elements in different than policy namespace
    Element all;
    Element[] children;
    for (int a = 0; a < alls.size(); a++) {
      all = (Element) alls.get(a);
      children = DOM.getChildElementsAsArray(all);
      for (int c = 0; c < children.length; c++) {
        if (Policy.POLICY_NS.equals(children[c].getNamespaceURI())) {
          return false;
        }
      }
    }
    return true;
  }
}
