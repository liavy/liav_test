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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLDescriptor;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Constants;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Serializer;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2006-1-17
 */
public class XIDefaultBindingAppender {
  /**
   * Appends default document-literal to the XI <code>def</code> - that is a wsdl with only one
   * document-literal portType, with in-out or in operations, with faults and without headers. The 
   * generated binding will be with localname <code>bindingName</code> and namespace equal
   * to the namespace of the portType inside <code>def</code>.
   * 
   * @param def the definition object which is to be updated with default binding
   * @param bindingName the localname of the default binding
   * @exception Exception in case anything goes wrong.
   * @return the updated <code>def</code> object 
   */
  public Definitions appendDefaultBinding(Definitions def, String bindingName) throws Exception {
    ObjectList pts = def.getInterfaces();
    Interface intf = (Interface) pts.item(0);
    //initialize the SOAPBinding instance
    SOAPBinding soapBinding = new SOAPBinding(new QName(intf.getName().getNamespaceURI(), bindingName));
    soapBinding.setInterface(intf.getName());
    soapBinding.setProperty(SOAPBinding.TRANSPORT, SOAPBinding.TRANSPORT_SOAP_OVER_HTTP);
    soapBinding.setProperty(SOAPBinding.SOAP_VERSION, SOAPBinding.SOAP_VERSION_11);
    soapBinding.setProperty(SOAPBinding.STYLE, SOAPBinding.DOC_STYLE_VALUE);
    def.appendChild(soapBinding);
    //process operations    
    ObjectList oprs = intf.getOperations();
    Operation tmpOp;
    SOAPBindingOperation soapOp;
    for (int i = 0; i < oprs.getLength(); i++) {
      tmpOp = (Operation) oprs.item(i);
      soapOp = new SOAPBindingOperation(tmpOp.getName());
      soapOp.setProperty(SOAPBindingOperation.STYLE, SOAPBinding.DOC_STYLE_VALUE);
      soapOp.setProperty(SOAPBindingOperation.USE, SOAPBindingOperation.USE_LITERAL);
      soapBinding.appendChild(soapOp);
    }
    //create an empty wsdl:binding element. This is needed since ConfigurationBuilder uses it to
    WSDL11Serializer s = new WSDL11Serializer();
    List bDscrs = s.serialize(def, SOAPBinding.SOAPBINDING_ID);
    if (bDscrs.size() != 1) {
      throw new Exception("Expected single wsdl-descriptor, found '" + bDscrs.size());
    }
    Element bEl = null;
    WSDLDescriptor wsdlDscr = (WSDLDescriptor) bDscrs.get(0);
    Element bDfs = wsdlDscr.getWsdl();
    List cNodes = DOM.getChildElementsByTagNameNS(bDfs, WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT);
    for (int i = 0; i < cNodes.size(); i++) {
      if (((Element) cNodes.get(i)).getAttribute(WSDL11Constants.NAME_ATTR).equals(bindingName)) {
        bEl = (Element) cNodes.get(i);
        break;
      }
    }
    if (bEl == null) {
      throw new Exception("Definitions '" + def + " misses binding '" + bindingName + "'");
    }
    //move all prefixes to binding element
    Hashtable h = DOM.getNamespaceMappingsInScope(bEl);
    Enumeration en = h.keys();
    String key, value;
    while (en.hasMoreElements()) {
      key = (String) en.nextElement();
      value = (String) h.get(key);
      if ("".equals(key)) { //this is default ns mapping
        bEl.setAttributeNS(NS.XMLNS, "xmlns", value);
      } else {
        bEl.setAttributeNS(NS.XMLNS, "xmlns:" + key, value);
      }
    }

    //obtain reference to the definitions element
    Node defEl = intf.getDomElement().getParentNode();
    //Element bindingEl = defEl.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT);
    Node impBEl = defEl.getOwnerDocument().importNode(bEl, true);
    defEl.appendChild(impBEl);
    soapBinding.setDomElement((Element) impBEl);
    return def;
  }

  /**
   * Appends default service with local name <code>serviceName</code> pointing to binding <code>bindingQName</code>.
   * The service is added in the same namespace as the binding.
   * @param rootWSDL
   * @param bindingQName
   * @param serviceName
   * @return the File into which the service has been added.
   * @throws Exception
   */
  public static File appendDefaultService(File rootWSDL, String serviceName, Map<String, QName> portToBindingMap) throws Exception {
    Set<String> ports = portToBindingMap.keySet();
    if (ports.isEmpty()) {
      throw new Exception("No ports found.");
    }
    String somePort = ports.iterator().next();
    QName someBindingQName = portToBindingMap.get(somePort);
    if (someBindingQName == null) {
      throw new Exception("No binding found for port '" + somePort + "'");
    }
    Object[] obj = findWSDLByNSAndEntry(rootWSDL, someBindingQName, WSDL11Constants.BINDING_ELEMENT, new HashSet<String>());
    if (obj == null) {
      throw new Exception("Unable to find 'binding' with QName " + someBindingQName + " in wsdl '" + rootWSDL + "' and its imported entities.");
    }
    
    Element someBindingElem = (Element) obj[0];
    Element serviceElem = someBindingElem.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.WSDL_PREF + ":" + WSDL11Constants.SERVICE_ELEMENT);
    serviceElem.setAttribute(WSDL11Constants.NAME_ATTR, serviceName);
    
    Iterator<String> itr =  ports.iterator();
    
    while (itr.hasNext()) {
      String portName = itr.next();
      QName bindingQName = portToBindingMap.get(portName);
      if (bindingQName == null) {
        throw new Exception("No binding found for port '" + portName + "'");
      }
      if (! bindingQName.getNamespaceURI().equals(someBindingQName.getNamespaceURI())) {
        throw new Exception("Findings should be in same namespace. Found " + someBindingQName + ", " + bindingQName);
      }
      Element portElem = serviceElem.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.WSDL_PREF + ":" + WSDL11Constants.PORT_ELEMENT);
      portElem.setAttribute(WSDL11Constants.NAME_ATTR, portName);
      portElem.setAttributeNS(NS.XMLNS, "xmlns:bprf", bindingQName.getNamespaceURI());
      portElem.setAttribute(WSDL11Constants.BINDING_ELEMENT, "bprf:" + bindingQName.getLocalPart());
      serviceElem.appendChild(portElem);
      
      Element soapElem = portElem.getOwnerDocument().createElementNS(WSDL11Constants.SOAP_NS, "soap:address");
      soapElem.setAttribute(WSDL11Constants.LOCATION_ATTR, "/");
      portElem.appendChild(soapElem);
    }
    
    File res = (File) obj[1];
    
    Element def = (Element) someBindingElem.getParentNode();
    def.appendChild(serviceElem);
    
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer tr = tf.newTransformer();
    tr.setOutputProperty(OutputKeys.INDENT, "yes");
    tr.transform(new DOMSource(def), new StreamResult(res));
    
    return res;
  }
  
  /**
   * Appends default binding with local name <code>bindingName</code> pointing to portType <code>portTypeQName</code>.
   * The binding is added in the same namespace as the portType.
   * @param rootWSDL the root wsdl file
   * @param portTypeQName the portType Qname
   * @param bindingName the local name of the binding
   * @return the File into which the binding has been added.
   * @throws Exception
   */
  public static File appendDefaultBinding(File rootWSDL, QName portTypeQName, String bindingName) throws Exception {
    
    Object[] obj = findWSDLByNSAndEntry(rootWSDL, portTypeQName, WSDL11Constants.PORTTYPE_ELEMENT, new HashSet<String>());
    if (obj == null) {
      throw new Exception("Unable to find 'portType' with QName " + portTypeQName + " in wsdl '" + rootWSDL + "' and its imported entities.");
    }
    
    Element portTypeElem = (Element) obj[0]; 
    
    Element bElem = portTypeElem.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.WSDL_PREF + ":" + WSDL11Constants.BINDING_ELEMENT);
    bElem.setAttribute(WSDL11Constants.NAME_ATTR, bindingName);
    bElem.setAttributeNS(NS.XMLNS, "xmlns:ptns", portTypeQName.getNamespaceURI());
    bElem.setAttribute(WSDL11Constants.TYPE_ATTR, "ptns:" + portTypeQName.getLocalPart());
    
    Element soapBindElem = bElem.getOwnerDocument().createElementNS(WSDL11Constants.SOAP_NS, "soap:" + WSDL11Constants.BINDING_ELEMENT);
    soapBindElem.setAttribute(WSDL11Constants.TRANSPORT_ATTR, WSDL11Constants.SOAPHTTP_TRANSPORT);
    soapBindElem.setAttribute(WSDL11Constants.STYLE_ATTR, "document");
    bElem.appendChild(soapBindElem);
    
    List ops = DOM.getChildElementsByTagNameNS(portTypeElem, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
    for (int i = 0; i < ops.size(); i++) {
      Element curOp = (Element) ops.get(i);
      
      Element bOp = portTypeElem.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.WSDL_PREF + ":" + WSDL11Constants.OPERATION_ELEMENT);
      bOp.setAttribute(WSDL11Constants.NAME_ATTR, curOp.getAttribute(WSDL11Constants.NAME_ATTR));
      
      Element soapOp = bOp.getOwnerDocument().createElementNS(WSDL11Constants.SOAP_NS, "soap:" + WSDL11Constants.OPERATION_ELEMENT);
      soapOp.setAttribute(WSDL11Constants.SOAPACTION_ATTR, "");
      bOp.appendChild(soapOp);
      
      List inputs = DOM.getChildElementsByTagNameNS(curOp, WSDL11Constants.WSDL_NS, WSDL11Constants.INPUT_ELEMENT);
      if (inputs.size() == 1) {
        Element in = bOp.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.WSDL_PREF + ":" + WSDL11Constants.INPUT_ELEMENT);
        Element soapBody = in.getOwnerDocument().createElementNS(WSDL11Constants.SOAP_NS, "soap:" + WSDL11Constants.SOAPBODY_ELEMENT);
        soapBody.setAttribute(WSDL11Constants.USE_ATTR, "literal");
        in.appendChild(soapBody);
        bOp.appendChild(in);
      }

      List outputs = DOM.getChildElementsByTagNameNS(curOp, WSDL11Constants.WSDL_NS, WSDL11Constants.OUTPUT_ELEMENT);
      if (outputs.size() == 1) {
        Element out = bOp.getOwnerDocument().createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.WSDL_PREF + ":" + WSDL11Constants.OUTPUT_ELEMENT);
        
        Element soapBody = out.getOwnerDocument().createElementNS(WSDL11Constants.SOAP_NS, "soap:" + WSDL11Constants.SOAPBODY_ELEMENT);
        soapBody.setAttribute(WSDL11Constants.USE_ATTR, "literal");
        out.appendChild(soapBody);
        bOp.appendChild(out);
      }
      bElem.appendChild(bOp);
    }
    
    File res = (File) obj[1];
    
    Element def = (Element) portTypeElem.getParentNode();
    def.appendChild(bElem);
    
    //save the updated definitions
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer tr = tf.newTransformer();
    tr.setOutputProperty(OutputKeys.INDENT, "yes");
    tr.transform(new DOMSource(def), new StreamResult(res));
    
    return (File) obj[1];
  }
  /**
   * Returns Object[], where Object[0] is the element which corresponds to entry with QName <code>entryQName</code> and Object[2] is
   * the File object corresponding to the wsdl which contains the entity. If not found null is returned.
   */
  private static Object[] findWSDLByNSAndEntry(File wsdl, QName entryQName, String entryType, Set<String> parsedWsdls) throws Exception {
    //preserve from cycling imports
    if (parsedWsdls.contains(wsdl.getAbsolutePath())) {
      return null;
    }
    
    //Document doc = docBuilder.parse(wsdl);
    Element docElem = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB,wsdl).getDocumentElement();
    
    if (! WSDL11Constants.WSDL_NS.equals(docElem.getNamespaceURI()) && WSDL11Constants.DEFINITIONS_ELEMENT.equals(docElem.getLocalName())) {
      return null;
    }
    
    parsedWsdls.add(wsdl.getAbsolutePath());
    
    String tNS = docElem.getAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR);
    if (tNS.equals(entryQName.getNamespaceURI())) {
      //portType element should be searched
      if (WSDL11Constants.PORTTYPE_ELEMENT.equals(entryType)) {
        List portTypes = DOM.getChildElementsByTagNameNS(docElem, WSDL11Constants.WSDL_NS, WSDL11Constants.PORTTYPE_ELEMENT);
        for (int i = 0; i < portTypes.size(); i++) {
          Element elem = (Element) portTypes.get(i);
          if (elem.getAttribute(WSDL11Constants.NAME_ATTR).equals(entryQName.getLocalPart())) {
            return new Object[]{elem, wsdl};
          }
        }
      } else if (WSDL11Constants.BINDING_ELEMENT.equals(entryType)) {
        List bindings = DOM.getChildElementsByTagNameNS(docElem, WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT);
        for (int i = 0; i < bindings.size(); i++) {
          Element elem = (Element) bindings.get(i);
          if (elem.getAttribute(WSDL11Constants.NAME_ATTR).equals(entryQName.getLocalPart())) {
            return new Object[]{elem, wsdl};
          }
        }
      }
    }
    
    //traverse imports. This is not dependant of the tns, since in WSDL11 there is no include and all available namespaces should be parsed.
    List imports = DOM.getChildElementsByTagNameNS(docElem, WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);
    for (int i = 0; i < imports.size(); i++) {
      Element elem = (Element) imports.get(i);
      String location = elem.getAttribute(WSDL11Constants.LOCATION_ATTR);
      if (location.length() > 0) {
        URI uri = new URI(location);
        File newWsdl;
        if (uri.isAbsolute()) {
          newWsdl = new File(uri);
        } else { //this should be relative
          URI tmp = wsdl.toURI();
          tmp = tmp.resolve(uri);
          newWsdl = new File(tmp);
        }
        Object[] res = findWSDLByNSAndEntry(newWsdl, entryQName, entryType, parsedWsdls);
        if (res != null) {
          return res;
        }
      }
    }
    return null;
  }
  
  public static void main(String[] args) throws Exception {
    WSDLLoader l = new WSDLLoader();
//    XIDefaultBindingAppender.appendDefaultBinding(new File("d:/temp/bcf/srt_test_service_in_710.wsdl"), new QName("http://xi.com/xiveri/source_runtime_ws", "srt_test_service_in_710"), "externalBinding");
//    XIDefaultBindingAppender.appendDefaultService(new File("d:/temp/bcf/srt_test_service_in_710.wsdl"), new QName("http://xi.com/xiveri/source_runtime_ws", "externalBinding"), "externalService", "externalPort");
//    XIDefaultBindingAppender dbA = new XIDefaultBindingAppender();
//    dbA.appendDefaultBinding(def, "defBinding");
//    WSDLSerializer s = new WSDLSerializer();
//    List wsdls = s.serialize(def);
//    WSDLDescriptor wsdlD = (WSDLDescriptor) wsdls.get(0);
//    System.out.println(wsdlD.getWsdl());
  }
}
