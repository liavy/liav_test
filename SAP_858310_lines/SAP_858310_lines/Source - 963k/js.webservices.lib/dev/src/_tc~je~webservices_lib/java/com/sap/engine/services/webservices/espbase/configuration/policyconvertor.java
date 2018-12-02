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
package com.sap.engine.services.webservices.espbase.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.configuration.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.configuration.exceptions.ResourceAccessor;
import com.sap.engine.services.webservices.wspolicy.AllOperator;
import com.sap.engine.services.webservices.wspolicy.Assertion;
import com.sap.engine.services.webservices.wspolicy.ExactlyOneOperator;
import com.sap.engine.services.webservices.wspolicy.Expression;
import com.sap.engine.services.webservices.wspolicy.ExpressionsArray;
import com.sap.engine.services.webservices.wspolicy.Policy;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-2
 */
public class PolicyConvertor {
  
  private List marshallers = new ArrayList();
  
  /**
   * Converts <code>pList</code> properties into DOM Element assertions.
   * 
   * @param pList properties
   * @param rootDoc Document object which to be use for creation of DOM Elements.
   * @return List containing DOM Element objects.
   */
  public List marshalAssertions(PropertyListType pList, Document rootDoc, String wsdlLevel, int mode) throws ConfigurationMarshallerException {
    //check whether for all properties in the list have marshallers
    ConfigurationBuilder.checkPropertyListAgainsMarshallers(pList.getProperty(), marshallers, mode);
     
    List result = new ArrayList();
    int count = 0; 
    PropertyType props[];
    IConfigurationMarshaller ml;
    List tmpList;
    for (int i = 0; i < marshallers.size(); i++) {
      ml = (IConfigurationMarshaller) marshallers.get(i);
      props = ConfigurationBuilder.getPropertiesForMarshaller(ml, pList.getProperty(), mode);
      tmpList = ml.marshalAssertions(props, rootDoc, wsdlLevel, mode);
      if (tmpList != null) {
        count += tmpList.size();
        result.addAll(tmpList);
      }
    }
    return result;
  }  

  /**
   * Converts the data from the Policy object into 
   * the Behavior one. The policy must be normalized. For
   * each alternative in the policy, a corresponding PropertyList is created.
   * 
   * @return true of the Behavior parameter has been initialized with at least one valid configuration. In non of the 
   *         alternatives has been resolved, false is returned 
   */
  public boolean unmarshal(Behaviour b, Policy p, String level, int mode) throws Exception {
    ExpressionsArray arr = p.getChildExpressions();
    if (arr.size() != 1 || (! (((Expression) arr.get(0)) instanceof ExactlyOneOperator))) {
      throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(), ExceptionConstants.POLICY_NOT_NORMALIZED, new Object[]{p.getPolicyAsDom()});        
    }
    
    Expression exOne = (ExactlyOneOperator) arr.get(0);
    ExpressionsArray alternatives = exOne.getChildExpressions();
    Expression allOp;
    List tmpList;
    List propListType = new ArrayList();
    PropertyListType pLType;
    for (int i = 0; i < alternatives.size(); i++) {
      allOp = alternatives.get(i);
      tmpList = processAlternative(allOp, p, level, mode);
      if (tmpList != null) { //in case the alternative is resolved
        pLType = new PropertyListType();
        pLType.setProperty((PropertyType[]) tmpList.toArray(new PropertyType[tmpList.size()]));
        propListType.add(pLType);              
      }
    }
    //in case no alternatives are available, or at least one alternative is resolved, return true;
    if (alternatives.size() > 0 && propListType.size() > 0 || alternatives.size() == 0) {
      b.setPropertyList((PropertyListType[]) propListType.toArray(new PropertyListType[propListType.size()]));
      return true;
    } else {      
      return false;
    }
  }
  
  /**
   * Returns a list of Behavior object, instances
   * of the specified Class. For each alternative in the
   * policy, one object is created.
   */
  public List unmarshalAlternatives(Class bClass, Policy p, String level, int mode) throws Exception {
    ExpressionsArray arr = p.getChildExpressions();
    if (arr.size() != 1 || (! (((Expression) arr.get(0)) instanceof ExactlyOneOperator))) {
      throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(), ExceptionConstants.POLICY_NOT_NORMALIZED, new Object[]{p.getPolicyAsDom()});        
    }
    
    List result = new ArrayList();
    Expression exOne = (ExactlyOneOperator) arr.get(0);
    ExpressionsArray alternatives = exOne.getChildExpressions();
    Expression allOp;
    Policy newP;
    Behaviour behavior;
    for (int i = 0; i < alternatives.size(); i++) {
      allOp = alternatives.get(i);
      if (! (allOp instanceof AllOperator)) {
        throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(), ExceptionConstants.POLICY_NOT_NORMALIZED, new Object[]{p.getPolicyAsDom()});        
      }
      newP = new Policy();
      exOne = new ExactlyOneOperator();
      exOne.getChildExpressions().add(allOp);
      newP.getChildExpressions().add(exOne);
      behavior = (Behaviour) bClass.newInstance();
      if (unmarshal(behavior, newP, level, mode)) { //add the behavior object only if the alterative is resolved
        result.add(behavior);
      }
    }
    //in case no alternative is resolved, add an empty alternative
    if (result.size() == 0) {    
      behavior = (Behaviour) bClass.newInstance();
     result.add(behavior);       
    }
    return result;
  }
  
  public void addMarshaller(IConfigurationMarshaller marshaller) {
    if (marshaller == null) {
      throw new IllegalArgumentException("null");
    }
    marshallers.add(marshaller);  
  }
  /**
   * Adds all marshallers from <code>marshallers</code> into internal list.
   * @param marshallers List of IConfigurationMarshaller objects
   */
  public void addMarshallers(List marshallers) {
    this.marshallers.addAll(marshallers);
  }
  
  /**
   * Returns list of PropertyType objects.
   * If null is returned, this means that the alternative has unresolved assertion. 
   */
  private List processAlternative(Expression all, Policy p, String level, int mode) throws Exception {
    if (! (all instanceof AllOperator)) {
      throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(), ExceptionConstants.POLICY_NOT_NORMALIZED, new Object[]{p.getPolicyAsDom()});        
    }
    ExpressionsArray assertions;
    assertions = all.getChildExpressions();
    Expression ass;
    for (int i = 0; i < assertions.size(); i++) { //check for valid normalized policy
      ass = assertions.get(i);
      if (! (ass instanceof Assertion)) {
        throw new ConfigurationMarshallerException(ResourceAccessor.getResourceAccessor(), ExceptionConstants.POLICY_NOT_NORMALIZED, new Object[]{p.getPolicyAsDom()});        
      }      
    }
    int total = 0;
    Element[][] arr = new Element[marshallers.size()][];
    
    for (int i = 0; i < marshallers.size(); i++) { //create corresponding array for marshaller
      arr[i] = getAssertionsForMarshaller((IConfigurationMarshaller) marshallers.get(i), assertions, mode);
      total += arr[i].length;
    }
    List result = new ArrayList();
    //there is not marshaller for one of the assertions. Do nothing 
    if (total != assertions.size()) {
      //TODO throw an exception...???
      return null;       
    }
    IConfigurationMarshaller pM; List tmpList;
    for (int i = 0; i < arr.length; i++) {
      if (arr[i].length > 0) { //if there are assertions for this marshaller to convert
        pM = (IConfigurationMarshaller) marshallers.get(i);
        tmpList = pM.unmarshalAssertions(arr[i], level, mode);
        if (tmpList != null) {
          result.addAll(tmpList);
        }
      }
    }
    return result;
  }
  /**
   * Returns Element[] containing assertion elements, which
   * are supported (recorgnized) by <code>marsh</code>.
   */
  private Element[] getAssertionsForMarshaller(IConfigurationMarshaller marsh, ExpressionsArray ass, int mode) {
    Set s = marsh.getKnownAssertions(mode);
    List result = new ArrayList();
    Assertion a;
    Element el;
    QName qname;
    for (int i = 0; i < ass.size(); i++) {
      a = (Assertion) ass.get(i);
      el = a.getAssertion();
      qname = new QName(el.getNamespaceURI(), el.getLocalName());
      if (s.contains(qname)) {
        result.add(el);
      }
    }
    return (Element[]) result.toArray(new Element[result.size()]);
  }
  
  
  void clearMarshallers() {
    this.marshallers.clear();
  }
}
