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

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.sap.engine.lib.xml.dom.DOM;

/**
 * @author Dimitar Angelov
 *
 */
public class Assertion implements Expression {
  
  public static final int ASSERTION_EXPRESSION_TYPE  =  1;
  
  private Element assertion;
  private boolean isOptional;
  
  public Assertion() {
  }
  
  public Assertion(Element as) {
    this.setAssertion(as);
  }
  
  /* (non-Javadoc) 
   * @see wspolicy.Expression#evaluate()
   */
  public Expression evaluate() throws PolicyException {
    //check for nested Policy element
    List res = DOM.getChildElementsByTagNameNS(this.assertion, Policy.POLICY_NS, Policy.POLICY_ELEMENT);
    if (res.size() == 1) {
      Element pE = (Element) res.get(0);
      Policy p = PolicyDomLoader.loadPolicy(pE);
      p.normalize();
            
      ExactlyOneOperator eo = (ExactlyOneOperator) p.getChildExpressions().get(0); //this should be ExactlyOne
      ExpressionsArray ea = eo.getChildExpressions();
      if (ea.size() > 1) { //dublicate this assertion as many time as the number of altarnatives
        ExactlyOneOperator result = new ExactlyOneOperator(); 
        for (int i = 0; i < ea.size(); i++) {
          //create policy element
          Element newPE = this.assertion.getOwnerDocument().createElementNS(Policy.POLICY_NS, Policy.POLICY_ELEMENT);
          //Element exOne = newPE.getOwnerDocument().createElementNS(Policy.POLICY_NS, Policy.EXACTLYONE_ELEMENT);
          //newPE.appendChild(exOne);
          //append the <All> to the newly create ExactlyOne
          AllOperator all = (AllOperator) ea.get(i);
          all.attachToParent(newPE);
          //create a copy of the original assertion
          Element newAssEl = (Element) this.assertion.cloneNode(true);
          //find the policy element inside it
          List tmpPE = DOM.getChildElementsByTagNameNS(newAssEl, Policy.POLICY_NS, Policy.POLICY_ELEMENT);
          newAssEl.replaceChild(newPE, (Element) tmpPE.get(0));
          //add the new assertion to the result Exactly one
          result.getChildExpressions().add(new Assertion(newAssEl));
        }
        if (this.isOptional) {
          result = buildAlternativeForOptional(result);
        }
        result.evaluate();
        return result;
      } else {
        Element normalizedPEl = p.getPolicyAsDom(this.assertion.getOwnerDocument());
        this.assertion.replaceChild(normalizedPEl, pE);
        if (this.isOptional) {
          Assertion newAss = new Assertion();
          newAss.isOptional = false;
          newAss.assertion = this.assertion;
          ExactlyOneOperator exOne = buildAlternativeForOptional(newAss);
          exOne.evaluate();
          return exOne;
        } else {
          return this;
        }
      }
    } else if (res.size() > 1) {
      throw new PolicyException("Assertion is allowed to contain only one 'Policy' child. Found '" + res.size() + "'");
    }
    
    //this.assertion.get
    if (this.isOptional) {
      Assertion newAss = new Assertion();
      newAss.isOptional = false;
      newAss.assertion = this.assertion;
      ExactlyOneOperator exOne = buildAlternativeForOptional(newAss);
      exOne.evaluate();
      return exOne;
    }
    return this;
  }
  /**
   * If this assertion has nested policy it is returned, else
   * null is returned.
   * @return
   */
  public Element getNestedPolicyElement() throws PolicyException {
    List res = DOM.getChildElementsByTagNameNS(this.assertion, Policy.POLICY_NS, Policy.POLICY_ELEMENT);
    if (res.size() == 1) {
      return (Element) res.get(0);
    } else if (res.size() == 0) {
      return null;
    } else {
      throw new PolicyException("Assertion is allowed to contain only one 'Policy' child. Found '" + res.size() + "'");
    }
  }
  /* (non-Javadoc)
   * @see wspolicy.Expression#getType()
   */
  public int getType() {
    // TODO Auto-generated method stub
    return ASSERTION_EXPRESSION_TYPE;
  }
  
  
  /**
   * @return
   */
  public Element getAssertion() {
    return assertion;
  }

  /**
   * @return
   */
  public boolean isOptional() {
    return isOptional;
  }

  /**
   * @param element
   */
  public void setAssertion(Element element) {
    //check for 'optional' attribute
    if (element.getAttributeNS(Policy.POLICY_NS, Policy.OPTIONAL_ATTRIBUTE).equals("true")) {
      this.setOptional(true);
    } 
    element.removeAttributeNS(Policy.POLICY_NS, Policy.OPTIONAL_ATTRIBUTE);              
    //check for nested policies
    
    
    assertion = element;
  }

  /**
   * @param b
   */
  public void setOptional(boolean b) {
    isOptional = b;
  }
  
  public String getAssertionNS() {
  	if (assertion != null) {
  		return assertion.getNamespaceURI();
  	}
  	
  	return null;
  }
  
	public String getAssertionLocalName() {
		if (assertion != null) {
			return assertion.getLocalName();
		}
  	
		return null;
	}
	
  /**
   * Returns true incase the type of parameter assertion is the same
   * as the type of this assertion.
   */
  public boolean isSameType(Assertion a) {
  	if (a.assertion == null || this.assertion == null) {
  		return false;
  	}
    if (a.getAssertionLocalName().equals(this.getAssertionLocalName())) {
    	if (a.getAssertionNS() != null && a.getAssertionNS().equals(this.getAssertionNS())) {
    		return true;
    	} else if (a.getAssertionNS() == null && this.getAssertionNS() == null) {
    		return true;
    	}
    }       
    
    return false;
  }
  
  public void print(int offset) {
    StringBuffer s = new StringBuffer();
    for (int i = 0; i < offset; i++) {
      s.append(" "); 
    }
    System.out.println(assertion);
  } 
  
  public Element attachToParent(Element root) {
  	Element newNode = (Element) root.getOwnerDocument().importNode(this.assertion, true);
    if (this.isOptional) {
      newNode.setAttributeNS(Policy.POLICY_NS, Policy.OPTIONAL_ATTRIBUTE, "true");
    }
  	root.appendChild(newNode);
    return (Element) newNode;
  }
  /**
   * If this assertions contains nested policy its alternatives
   * are checked against the <code>extAssertion</code>.
   * The assertion/policy has to be normalized. That is why this method
   * is with package level visability. 
   * @param assertion
   * @return true if this assertion is compatible with <code>extAssertion</code>.
   */
  boolean isCompatibleWith(Assertion extAssertion) throws PolicyException {
    QName thisQName = new QName(this.assertion.getNamespaceURI(), this.assertion.getLocalName());
    QName extAssQName = new QName(extAssertion.assertion.getNamespaceURI(), extAssertion.assertion.getLocalName());
    //compare the qnames
    if (! thisQName.equals(extAssQName)) {
      return false;
    }
    Element thisPolicyEl = getNestedPolicyElement();
    Element extPolicyEl = extAssertion.getNestedPolicyElement();
    //if none has nested policy they are compatible
    if (thisPolicyEl == null && extPolicyEl == null) {
      return true;
    }
    //if both have policy compare them
    if (thisPolicyEl != null && extPolicyEl != null) {
      Policy thisPolicy = PolicyDomLoader.loadPolicy(thisPolicyEl);
      Policy extPolicy = PolicyDomLoader.loadPolicy(extPolicyEl);
      
      ExactlyOneOperator thisEO = (ExactlyOneOperator) thisPolicy.getChildExpressions().get(0);
      ExactlyOneOperator extEO = (ExactlyOneOperator) extPolicy.getChildExpressions().get(0);
      //if ther are no alternatives
      if (thisEO.getChildExpressions().size() == 0 && extEO.getChildExpressions().size() == 0) {
        return true;
      }
      //else there should be only one alternative
      AllOperator thisAll = (AllOperator) thisEO.getChildExpressions().get(0); 
      AllOperator extAll = (AllOperator) extEO.getChildExpressions().get(0); 
      if (thisAll.isCompatibleWith(extAll)) {
        return true;
      } else {
        return false;
      }
    } else {
      //one has nested policy while the other does not - they are not compatible
      return false;
    }
  }
  /* (non-Javadoc)
   * @see wspolicy.Expression#getChildExpressions()
   */ 
  public ExpressionsArray getChildExpressions() {
    return null;
  }
  
  /**
   * Creates and returns an ExactlyOne operator
   * with one empty All and another one containing only <code>e</code>.
   * This is the algorith which should be applied when an assertion
   * is marked with Optional=true attribute 
   * @param e
   * @return
   */
  private ExactlyOneOperator buildAlternativeForOptional(Expression e) {
    ExactlyOneOperator exOne = new ExactlyOneOperator();
    AllOperator all = new AllOperator();
    all.getChildExpressions().add(e);
    exOne.getChildExpressions().add(all);
    exOne.getChildExpressions().add(new AllOperator());//empty all
    return exOne;

  }
}
