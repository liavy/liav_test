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
package com.sap.engine.services.webservices.espbase.configuration.cfg;

import java.io.File;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Provides utility methods for convertin ABAP descriptors into java.
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, May 30, 2006
 */
public class ABAPToJavaMapper {
  
  public static Element createMappings(File abapSoapApplication) throws Exception {
    Element root = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, abapSoapApplication).getDocumentElement();
    Element dtrtRules = (Element) root.getElementsByTagName("DTRT_MAPPING_RULES").item(0); //it should be only one.
    List ab_rules = DOM.getChildElementsByTagNameNS(dtrtRules, "", "SRT_WSP_MAP_DTRT_RULE");
    
    Document javaDocument = SharedDocumentBuilders.newDocument();
    Element j_mappings = javaDocument.createElement("mappings");
    Element j_dtrt = javaDocument.createElement("DTRT");
    j_mappings.appendChild(j_dtrt);
    for (int i = 0; i < ab_rules.size(); i++) {
      j_dtrt.appendChild(createRuleElement((Element) ab_rules.get(i), javaDocument));
    }
    return j_mappings;
  }
  
  public static Element createDomain(File abapDomain) throws Exception {
    Element root = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, abapDomain).getDocumentElement();
    NodeList abap_entities = root.getElementsByTagName("SRT_WSP_TEMPL_PROPERTY");
    
    Document javaDoc = SharedDocumentBuilders.newDocument();
    Element j_domain = javaDoc.createElement("domain");
    
    for (int i = 0; i < abap_entities.getLength(); i++) {
      j_domain.appendChild(createJavaProperty((Element) abap_entities.item(i), true, javaDoc));
    }
    return j_domain;
  }
  
  private static Element createRuleElement(Element abapRule, Document javaDocument) {
    Element j_rule = javaDocument.createElement("rule");
    Element ruleEL = (Element) DOM.getChildElementsByTagNameNS(abapRule, null, "RULE_ID").get(0);
    j_rule.setAttribute("id", getElementTextChild(ruleEL));
    //travers conditions
    Element b_conditions = (Element) DOM.getChildElementsByTagNameNS(abapRule, null, "CONDITION_PROPERTIES").get(0);
    Element j_conditions = javaDocument.createElement("condition-properties");
    List props = DOM.getChildElementsByTagNameNS(b_conditions, null, "SRT_WSP_MAP_PROPERTY_VALUE");
    for (int i = 0; i < props.size(); i++) {
      Element propEl = createJavaProperty((Element) props.get(i), true, javaDocument);
      j_conditions.appendChild(propEl);
    }
    j_rule.appendChild(j_conditions);
    //travers calculate
    Element b_calculate = (Element) DOM.getChildElementsByTagNameNS(abapRule, null, "CALCULATE_PROPERTIES").get(0);
    Element j_calculate = javaDocument.createElement("calculated-properties");
    props = DOM.getChildElementsByTagNameNS(b_calculate, null, "SRT_WSP_MAP_PROPERTY_VALUE");
    for (int i = 0; i < props.size(); i++) {
      Element propEl = createJavaProperty((Element) props.get(i), true, javaDocument);
      j_calculate.appendChild(propEl);
    }
    j_rule.appendChild(j_calculate);
    //travers prifile
    Element b_profile = (Element) DOM.getChildElementsByTagNameNS(abapRule, null, "PROFILE_PROPERTIES").get(0);
    Element j_profile = javaDocument.createElement("profile-properties");
    props = DOM.getChildElementsByTagNameNS(b_profile, null, "SRT_WSP_MAP_PROPERTY");
    for (int i = 0; i < props.size(); i++) {
      Element propEl = createJavaProperty((Element) props.get(i), false, javaDocument);
      j_profile.appendChild(propEl);
    }
    j_rule.appendChild(j_profile);
    
    return j_rule;
  }
  
  private static Element createJavaProperty(Element abapProp, boolean useValue, Document javaDocument) {
    Element propNameEl = (Element) DOM.getChildElementsByTagNameNS(abapProp, null, "NAME").get(0);
    Element propNameNameEl = (Element) DOM.getChildElementsByTagNameNS(propNameEl, null, "NAME").get(0);
    Element propNameNamespaceEl = (Element) DOM.getChildElementsByTagNameNS(propNameEl, null, "NAMESPACE").get(0);
    
    Element j_prop = javaDocument.createElement("property");
    Element j_prop_ns = javaDocument.createElement("namespace");
    j_prop_ns.appendChild(javaDocument.createTextNode(getElementTextChild(propNameNamespaceEl)));
    Element j_prop_name = javaDocument.createElement("name");
    j_prop_name.appendChild(javaDocument.createTextNode(getElementTextChild(propNameNameEl)));
    j_prop.appendChild(j_prop_ns);
    j_prop.appendChild(j_prop_name);
    
    if (useValue) {
      Element propValue = (Element) DOM.getChildElementsByTagNameNS(abapProp, null, "VALUE").get(0);
      Element j_value = javaDocument.createElement("value");
      j_value.appendChild(javaDocument.createTextNode(getElementTextChild(propValue)));
      j_prop.appendChild(j_value);
    }
    return j_prop;
  }
  
  
  private static String getElementTextChild(Element el) {
    return el.getFirstChild().getNodeValue();
  }
  
  public static void main(String[] args) throws Exception {
    Element mappings = createDomain(new File("D:/temp/feature_stuff/Example_Domain.xml"));
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer trf = tf.newTransformer();
    trf.setOutputProperty(OutputKeys.INDENT, "yes");
    trf.transform(new DOMSource(mappings), new StreamResult(System.out));
  }
}
