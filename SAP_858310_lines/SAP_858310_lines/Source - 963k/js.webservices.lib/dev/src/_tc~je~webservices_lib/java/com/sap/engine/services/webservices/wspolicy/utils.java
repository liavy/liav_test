/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.wspolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2006-4-4
 */
public class Utils {

  public static boolean isPolicyInFileNormalized(File f) throws Exception {
    Element policy = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, f).getDocumentElement();
    return Policy.isPolicyNormalized(policy);
  }
  /**
   * The directory must contain only xml files with policies inside.
   * @param directory
   * @param buf buffer where the results are written
   */
  public static void checkPolicyFilesInDirectoryForNormalization(File directory, StringBuffer buf) throws Exception {
    File fs[] = directory.listFiles();
    for (int i = 0; i < fs.length; i++) {
      if (fs[i].isFile()) {
        if (isPolicyInFileNormalized(fs[i])) {
          buf.append(fs[i].toURL() + " is normalized.\r\n");
        } else {
          buf.append(fs[i].toURL() + " is NOT normalized!\r\n");
        }
      }
    }
  }
  /**
   * Returns list of string objects respresenting the extented xpath expressions
   * of all leaves inside <code>curEl</code>.
   * @param all
   * @param curPath
   * @return
   */
  private static List getLeaves(Element curEl, String curPath) {
    //generate element id
    StringBuffer id = new StringBuffer(curPath);
    id.append("element:{" + curEl.getNamespaceURI() + "}" + curEl.getLocalName() + ", attrs:");
    NamedNodeMap attrs = curEl.getAttributes();
    List attrStrings = new ArrayList();
    for (int i = 0; i < attrs.getLength(); i++) {
      Attr curA = (Attr) attrs.item(i);
      if (! NS.XMLNS.equals(curA.getNamespaceURI())) { //skip namespace declarations
        attrStrings.add("{" + curA.getNamespaceURI() + "}" + curA.getLocalName() + "='" + curA.getValue() + "'");
      }
    }
    //sort the attributes
    String[] attrArr = (String[]) attrStrings.toArray(new String[attrStrings.size()]);
    Arrays.sort(attrArr);
    //append the attributes to the overall id
    for (int i=0; i < attrArr.length; i++) {
      id.append(attrArr[i]);
    }
    //final separator
    id.append("/");
    
    List leaves = new ArrayList();
    NodeList children = curEl.getChildNodes();
    boolean hasChildren = false;
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
        hasChildren = true;
        leaves.addAll(getLeaves((Element) children.item(i), id.toString()));
      }
    }
    if (! hasChildren) { //this element is leaf
      leaves.add(id.toString());
    }
    return leaves;
  }
  
  private static boolean compareAlternatives(List alt1Leaves, List alt2Leaves) {
    if (alt1Leaves.size() != alt2Leaves.size()) {
      return false;
    }
    //make copy
    alt1Leaves = new ArrayList(alt1Leaves);
    alt2Leaves = new ArrayList(alt2Leaves);
    
    String curLeave1;
    String curLeave2;
    for (int i = 0; i < alt1Leaves.size(); i++) {
      curLeave1 = (String) alt1Leaves.get(i);
      for (int j = 0; j < alt2Leaves.size(); j++) {
        curLeave2 = (String) alt2Leaves.get(j);
        //System.out.println("compareAlternatives(): '" + curLeave1 + "'");
        //System.out.println("compareAlternatives(): '" + curLeave2 + "'");
        if (curLeave1.equals(curLeave2)) {
          alt2Leaves.remove(j);
          j--;
          alt1Leaves.remove(i);
          i--;
          break;
        }
      }
    }
    if (alt1Leaves.size() == 0) { //for each leaf in 1 there is corresponding in 2.
      return true; 
    } else {
      return false;
    }
  }
  
  public static boolean compareNormalizedPolicies(Element p1, Element p2) throws Exception {
    if (! Policy.isPolicyNormalized(p1)) {
      throw new Exception("Policy is not normalized. Policy: " + p1);
    }
    if (! Policy.isPolicyNormalized(p2)) {
      throw new Exception("Policy is not normalized. Policy: " + p2);
    }
    
    List eo1 = DOM.getChildElementsByTagNameNS(p1, Policy.POLICY_NS, Policy.EXACTLYONE_ELEMENT);
    Element eo1El = (Element) eo1.get(0);
    //check whether the element contains only All children
    List alls1 = DOM.getChildElementsByTagNameNS(eo1El, Policy.POLICY_NS, Policy.ALL_ELEMENT);
    Element alls1Arr[] = (Element[]) alls1.toArray(new Element[alls1.size()]);
    
    List policy1Alternatives = new ArrayList(); //list of list objects representing the leaves of each alternative
    for (int i = 0; i < alls1Arr.length; i++) {
      List altLeaves = getLeaves(alls1Arr[i], "/");
      policy1Alternatives.add(altLeaves);
    }

    List eo2 = DOM.getChildElementsByTagNameNS(p2, Policy.POLICY_NS, Policy.EXACTLYONE_ELEMENT);
    Element eo2El = (Element) eo2.get(0);
    //check whether the element contains only All children
    List alls2 = DOM.getChildElementsByTagNameNS(eo2El, Policy.POLICY_NS, Policy.ALL_ELEMENT);
    Element alls2Arr[] = (Element[]) alls2.toArray(new Element[alls2.size()]);
    
    List policy2Alternatives = new ArrayList(); //list of list objects representing the leaves of each alternative
    for (int i = 0; i < alls2Arr.length; i++) {
      List altLeaves = getLeaves(alls2Arr[i], "/");
      policy2Alternatives.add(altLeaves);
    }
    
    if (policy1Alternatives.size() != policy2Alternatives.size()) {
      return false;
    }
    
    for (int i = 0; i < policy1Alternatives.size(); i++) {
      List cur1Alt = (List) policy1Alternatives.get(i);
      for (int j = 0; j < policy2Alternatives.size(); j++) {
        List cur2Alt = (List) policy2Alternatives.get(j);
        if (compareAlternatives(cur1Alt, cur2Alt)) {
          policy1Alternatives.remove(i);
          i--;
          policy2Alternatives.remove(j);
          j--;
          break;
        }
      }
    }
    
    if (policy1Alternatives.size() == 0) {
      return true;
    } else {
      return false;
    }
  }
  
  public static void mergePoliciesFromFiles(File p1, File p2, File mergedPolicy) throws Exception {
    Policy policy1 = PolicyDomLoader.loadPolicy(p1);
    Policy policy2 = PolicyDomLoader.loadPolicy(p2);
    Policy res = Policy.mergePolicies(new Policy[]{policy1, policy2});
    Element policyEL = res.getPolicyAsDom();
    TransformerFactory f = TransformerFactory.newInstance();
    f.newTransformer().transform(new DOMSource(policyEL), new StreamResult(mergedPolicy));
  }
  
  public static void normalizePolicy(File fl, File result) throws Exception {
    Policy policy = PolicyDomLoader.loadPolicy(fl);
    policy.normalize();
    Element policyEL = policy.getPolicyAsDom();
    TransformerFactory f = TransformerFactory.newInstance();
    f.newTransformer().transform(new DOMSource(policyEL), new StreamResult(result));
  }
  
  public static void intersectPolicies(File p1, File p2, File result) throws Exception {
    Policy policy1 = PolicyDomLoader.loadPolicy(p1);
    Policy policy2 = PolicyDomLoader.loadPolicy(p2);
    Policy res = Policy.intersectPolicies(policy1, policy2);
    Element policyEL = res.getPolicyAsDom();
    TransformerFactory f = TransformerFactory.newInstance();
    f.newTransformer().transform(new DOMSource(policyEL), new StreamResult(result));
  }
  
//  public static void main(String[] args) throws Exception {
//    System.out.println("Start...");
//    String policy = "Policy25";
//    String policy2 = "Policy25";
//    String policyRes = "Policy25-25";
//    File result = new File("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/Intersected/" + policyRes + "_sap.xml");
////    normalizePolicy(new File("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/sample_policies/" + policy + ".xml"), result);
////    mergePoliciesFromFiles(new File("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/sample_policies/" + policy + ".xml")
////                           , new File("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/sample_policies/" + policy2 + ".xml")
////                           , result);
//    intersectPolicies(new File("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/sample_policies/" + policy + ".xml")
//        , new File("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/sample_policies/" + policy2 + ".xml")
//        , result);
//    Element sapPolicy = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, result).getDocumentElement();
//    Element samplePolicy = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB
//                                              , new File("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/Intersected/" + policyRes + ".xml")).getDocumentElement();
//    
//    System.out.println(compareNormalizedPolicies(sapPolicy, samplePolicy));
//  }
}
