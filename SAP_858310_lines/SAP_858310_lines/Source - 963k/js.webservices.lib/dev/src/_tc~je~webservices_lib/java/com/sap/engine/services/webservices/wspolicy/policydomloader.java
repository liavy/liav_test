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

import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-3
 */

public class PolicyDomLoader {
    
  public static Policy loadPolicy(java.io.File file) throws PolicyException {
    Element root;
    try {
      root = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB,file).getDocumentElement();
    } catch (Exception se) {
      throw new PolicyException(se);
    }
    return loadPolicy(root);
  }

	public static Policy loadPolicy(Element policy) throws PolicyException {
    return loadPolicy(policy, null);
	}
   
  public static Policy loadPolicy(Element policy, NodeList refPolicies) throws PolicyException {
    if (policy.getNamespaceURI().equals(Policy.POLICY_NS) && policy.getLocalName().equals(Policy.POLICY_ELEMENT)) {
      return (Policy) loadExpression(policy, refPolicies);
    } else {
      throw new PolicyException(PolicyException.NO_POLICY_DESCRIPTION);
    }     
    
  }
  
  public static Policy loadPolicyReference(Element pRef, NodeList refPolicies) throws PolicyException {
    if (pRef.getLocalName().equals(Policy.POLICYREFERENCE_ELEMENT) && Policy.POLICY_NS.equals(pRef.getNamespaceURI())) {
      String uri = pRef.getAttribute(Policy.POLICYREFERENCE_URI_ATTR);
      return loadPolicyURIs(uri, refPolicies);
    } 
    throw new PolicyException(PolicyException.POLICYREFERENCE_EXPECTED, new Object[]{pRef});
  }
  
  /**
	 * @param uris a white-space separated list of policy uris
   * @param refPolicies org.w3c.dom.Element objects, representing wsp:policy definitions
	 */
	public static Policy loadPolicyURIs(String uris, NodeList refPolicies) throws PolicyException {
    if (refPolicies == null) {
      throw new PolicyException(PolicyException.MISSING_POLICY_REFERENCES);      
    }
    
    StringTokenizer t = new StringTokenizer(uris);
    int pNum = t.countTokens();
    if (pNum == 0) {
      throw new PolicyException(PolicyException.POLICY_NOT_FOUND, new Object[]{""});
    }
    boolean found = false;
    Policy[] ps = new Policy[pNum];
    String policyId;
    int nom = 0;
    while (t.hasMoreTokens()) {
      policyId = t.nextToken();
      found = false;
      if (policyId.startsWith("#")) { //search by using 'wsu:Id' attribute
        policyId = policyId.substring(1);
      }
      //search for policy expression by id.
      Element curP;
      for (int i = 0; i < refPolicies.getLength(); i++) {
        curP = (Element) refPolicies.item(i);
        if (curP.getAttributeNS(Policy.WS_SEC_UTILITY_NS, Policy.ID_ATTR).equals(policyId)) {
          ps[nom++] = loadPolicy(curP, refPolicies);
          found = true;
          break;
        }
      }
      if (! found) { //search by using @Name attribute
        for (int i = 0; i < refPolicies.getLength(); i++) {
          curP = (Element) refPolicies.item(i);
          if (curP.getAttribute(Policy.NAME_ATTR).equals(policyId)) {
            ps[nom++] = loadPolicy(curP, refPolicies);
            found = true;
            break;
          }
        }
      }
      if (! found) {
        throw new PolicyException(PolicyException.POLICY_NOT_FOUND, new Object[]{policyId});        
      }
    }
    
    if (ps.length == 1) {
      return ps[0];
    } else {
      return Policy.mergePolicies(ps);
    }
  }
  
  private static Expression loadExpression(Element root, NodeList refPolicies) throws PolicyException {
    //WS-Policy elements
    if (root.getNamespaceURI().equals(Policy.POLICY_NS)) {
      Expression res;

      if (root.getLocalName().equals(Policy.POLICY_ELEMENT)) {
        res = new Policy();     
      } else if (root.getLocalName().equals(Policy.ALL_ELEMENT)) {
        res = new AllOperator();
      } else if (root.getLocalName().equals(Policy.EXACTLYONE_ELEMENT)) {
        res = new ExactlyOneOperator();
      } else if (root.getLocalName().equals(Policy.POLICYREFERENCE_ELEMENT)) {
        return loadPolicyReference(root, refPolicies);
      } else {
        throw new PolicyException(PolicyException.UNKNOWN_POLICY_ELEMENT, new Object[]{root.getLocalName()});
      }
      
      NodeList nodes = root.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
          res.getChildExpressions().add(loadExpression((Element) nodes.item(i), refPolicies));    
        }
      }
      return res;
    } else { //this is an assertion
      Assertion ass = new Assertion();
      ass.setAssertion(root);
      return ass;
    }         
  }
  
//  public static void main(String[] args) throws Exception {
//    Policy p = PolicyDomLoader.loadPolicy(new File("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/sample_policies/PolicyDocumentSample.xml"));
//    p.normalize();
//    Element e = p.getPolicyAsDom();
//    TransformerFactoryImpl f = new TransformerFactoryImpl();
//    f.newTransformer().transform(new DOMSource(e), new StreamResult(System.out));
//    
//  }
}
