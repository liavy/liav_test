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
package com.sap.engine.services.webservices.espbase.wsdl.misc;

import java.io.File;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Constants;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.wspolicy.Policy;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2006-4-19
 */
public class OutsideInWSDLUtils {
  
  private static final String FEATURE_ELEM_NS  =  "http://www.sap.com/webas/630/wsdl/features";
  
  /**
   * Returns true if <code>xmlFile</code> is wsdl and its 'targetNamespace' equals to <code>ns</code>.
   * @param xmlFile file which xml namespace to be checked  
   * @param ns the namespace
   * @return
   * @throws Exception
   */
  public static boolean checkWSDLTargetNamespace(String xmlFile, String ns) throws Exception {
    Element orgXML = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, xmlFile).getDocumentElement();
    //process only wsdl files
    if (! WSDL11Constants.WSDL_NS.equals(orgXML.getNamespaceURI())) { 
      return false;
    }
    String tmpTNS = orgXML.getAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR);
    if (tmpTNS.equals(ns)) {
      return true;
    }
    return false;
  }
  /**
   * Loads the xml <code>file</code>, removes any binding and service entities and
   * saves it under the original <code>file</code>. 
   * @param file
   * @throws Exception
   */
  public static void removeBindingAndServiceFromXMLAndSave(String file) throws Exception {
    Element orgXML = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, file).getDocumentElement();
    //process only wsdl files
    if (! WSDL11Constants.WSDL_NS.equals(orgXML.getNamespaceURI())) { 
      return;
    }
    //remove bindings
    List bindings = DOM.getChildElementsByTagNameNS(orgXML, WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT);
    for (int i = 0; i < bindings.size(); i++) {
      Element bEl = (Element) bindings.get(i);
      orgXML.removeChild(bEl);
    }
    //remove services
    List srvs = DOM.getChildElementsByTagNameNS(orgXML, WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT);
    for (int i = 0; i < srvs.size(); i++) {
      Element srvEl = (Element) srvs.get(i);
      orgXML.removeChild(srvEl);
    }
    //removing
    removeSAPFeatureAndPolicyEntities(orgXML);
    //save  
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();
    t.transform(new DOMSource(orgXML), new StreamResult(file));
  }
  /**
   * Reads from <code>dfs</code> binding with qname <code>bQName</code>, creates a wsdl template
   * from it, containing import statement for the namespace of the portType referenced by the binding,
   * and adjusted 'type' attribute of the binding to refer to the correct portType@name. The 
   * so create wsdl template need only an update for the import location. The template is saved
   * in <code>file</code>.  
   * @param dfs definitions that represents the whole wsdl
   * @param bQName the binding which needs to be used as a template
   * @parm file where to save the binding template
   */
  public static void extractAndSaveBindingTemplate(Definitions dfs, QName bQName, String file) throws Exception {
    Binding b = dfs.getBinding(bQName);
    if (b == null) {
      throw new Exception("Definitions '" + dfs + " misses binding '" + bQName + "'");
    }
    QName intQName = b.getInterface();
    Element bEl = b.getDomElement();
    String oldWSDLNSPref = bEl.getPrefix();
    String defQName;
    if (oldWSDLNSPref == null) {
      defQName = WSDL11Constants.DEFINITIONS_ELEMENT;
    } else {
      defQName = oldWSDLNSPref + ":" + WSDL11Constants.DEFINITIONS_ELEMENT;
    }
    //create wsdl:definitions for the template
    Element tDefEl = bEl.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, defQName);
    tDefEl.setAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR, bQName.getNamespaceURI());
    //obtain all ns prefixes that are valid for the binding element and set them on the definition. Here should appear the prefix definition for the WSDL11 ns
    //here also the prefix for the portType namespace should be available
    Hashtable h = DOM.getNamespaceMappingsInScope(bEl);
    Enumeration en = h.keys();
    String key, value;
    while (en.hasMoreElements()) {
      key = (String) en.nextElement();
      value = (String) h.get(key);
      if ("".equals(key)) { //this is default ns mapping
        tDefEl.setAttributeNS(NS.XMLNS, "xmlns", value);
      } else {
        tDefEl.setAttributeNS(NS.XMLNS, "xmlns:" + key, value);
      }
    }
    
    //create wsdl:import for the portType namespace
    Element imp = tDefEl.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, (oldWSDLNSPref == null ? "": (oldWSDLNSPref + ":"))  + WSDL11Constants.IMPORT_ELEMENT);
    imp.setAttribute(WSDL11Constants.NAMESPACE_ATTR, intQName.getNamespaceURI());
    
    //append the import and the binding elements
    tDefEl.appendChild(imp);
    tDefEl.appendChild(bEl);
    //remove the sap and policy entities if any
    removeSAPFeatureAndPolicyEntities(tDefEl);
    
    //save
    File f = new File(file);
    f.getParentFile().mkdirs();
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();
    t.setOutputProperty(OutputKeys.INDENT, "yes");
    t.transform(new DOMSource(tDefEl), new StreamResult(file));
  }
  /**
   * Removes sap-feature and policy artefacts (attribute and elements) from <code>el</code> subtree.
   * @param el 
   */
  private static void removeSAPFeatureAndPolicyEntities(Element el) {
    //remove policy attribute(s) - attribute in policy namespace
    NamedNodeMap attrs = el.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Attr a = (Attr) attrs.item(i);
      if (Policy.POLICY_NS.equals(a.getNamespaceURI())) {
        el.removeAttributeNode(a);
      }
    }
    //remove policy and sap child elements
    NodeList nL = el.getChildNodes();
    for (int i = 0; i < nL.getLength(); i++) {
      if (nL.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element tE = (Element) nL.item(i);
        if (Policy.POLICY_NS.equals(tE.getNamespaceURI()) || FEATURE_ELEM_NS.equals(tE.getNamespaceURI())) {
          el.removeChild(tE);
        } else { //call recursively this method for the current element
          removeSAPFeatureAndPolicyEntities(tE);
        }
      }
    }
    //remove the ESR element which contains the metadata.
    List list = DOM.getChildElementsByTagNameNS(el, "urn:com-sap:ifr:v2:wsdl", "properties");
    if (list.size() > 0) {
      for (int i = 0; i < list.size(); i++) {
        Element tmpE = (Element) list.get(i);
        el.removeChild(tmpE);
      }
    }
  }
  /**
   * Removes the configuration artefacts and save into the original file 
   * @param wsdlFile
   */
  public static void removeCFGArtefactsAndSave(File wsdlFile) throws Exception {
    Document doc = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, wsdlFile);
    Element wsdlDef = doc.getDocumentElement();
    
    List imports = DOM.getChildElementsByTagNameNS(wsdlDef, WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);
    for (int i = 0; i < imports.size(); i++) {
      Element e = (Element) imports.get(i);
      String url = e.getAttribute(WSDL11Constants.LOCATION_ATTR);
      if (url.length() > 0) {
        File toImport;
        URI uri = new URI(url);
        if (uri.isAbsolute()) {
          toImport = new File(uri);
        } else {
          URI u = wsdlFile.toURI().resolve(uri);
          toImport = new File(u);
        }
        removeCFGArtefactsAndSave(toImport);
      }
    }
    removeSAPFeatureAndPolicyEntities(wsdlDef);
    //save as original
    Transformer tF = TransformerFactory.newInstance().newTransformer();
    tF.setOutputProperty(OutputKeys.INDENT, "yes");
    tF.transform(new DOMSource(wsdlDef), new StreamResult(wsdlFile));
  }
//  public static void main(String[] args) throws Exception {
//    Element el = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, new File("d:/temp/PolicyAttachments.wsdl")).getDocumentElement();
//    removeSAPFeatureAndPolicyEntities(el);
//    File f = new File("d:/temp/PolicyAttachments_removed.wsdl");
//    TransformerFactory tf = TransformerFactory.newInstance();
//    Transformer t = tf.newTransformer();
//    t.setOutputProperty(OutputKeys.INDENT, "yes");
//    t.transform(new DOMSource(el), new StreamResult(f));
//  }
}
