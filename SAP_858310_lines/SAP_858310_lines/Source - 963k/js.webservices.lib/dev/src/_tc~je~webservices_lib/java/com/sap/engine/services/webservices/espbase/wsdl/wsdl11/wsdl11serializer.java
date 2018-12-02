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
package com.sap.engine.services.webservices.espbase.wsdl.wsdl11;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.wsdl.AttachmentsContainer;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionElement;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.MIMEPart;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.espbase.wsdl.Parameter;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLDescriptor;
import com.sap.engine.services.webservices.espbase.wsdl.XSDRef;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLMarshalException;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-30
 */
public class WSDL11Serializer {
  
  private static final String TNS = "tns";
  private static final String PREFIX_BASE = "p";
  private static final String MSG_IN = "In";
  private static final String MSG_OUT = "Out";
  private static final String MSG_FAULT = "Fault";
  private static final String MSG_HEADERS = "Headers";
  
  private Document document;
  private Map<String, WSDLDescriptor> documents; //key namespace(String), value WSDLDescriptor.
  private Map msgQNames = new Hashtable(); //key Interface.getName() +  operationName + <msg_type>, value msg QName
  private Transformer transformer;
   
  public WSDL11Serializer() throws WSDLMarshalException {
    try {
      document = SharedDocumentBuilders.newDocument();
    } catch (RuntimeException e) {
      throw new WSDLMarshalException(e);
    }
  }
  
//  /**
//   * Returns a List of WSDLDescriptor objects. 
//   */
//  public List serialize(Definitions def) throws WSDLMarshalException {
//    //init(clear) the state for new processing    
//    documents = new Hashtable();
//    msgQNames.clear();
//    
//    ObjectList intfs = def.getChildren(Interface.INTERFACE_ID);
//    for (int i = 0; i < intfs.getLength(); i++) {
//      serializeInterface((Interface) intfs.item(i));
//    }
//    ObjectList bindings = def.getChildren(SOAPBinding.SOAPBINDING_ID);
//    for (int i = 0; i < bindings.getLength(); i++) {
//      serializeSoapBinding((SOAPBinding) bindings.item(i));
//    }
//    
//    ObjectList services = def.getChildren(Service.SERVICE_ID);
//    for (int i = 0; i < services.getLength(); i++) {
//      serializeService((Service) services.item(i));
//    }
//    
//    Iterator itr = documents.values().iterator();
//    while (itr.hasNext()) {
//      normalizeDefinitions(((WSDLDescriptor) itr.next()).getWsdl()); 
//    }
//    return new ArrayList(documents.values());
//  }
  
  /**
   * Returns a list of WSDLDescriptor objects.
   * @param def definitions which is to be serialized
   * @param mask denotes which components of the definitions to be serialized - service(s), interface(s), binding(s)
   */
  public List serialize(Definitions def, int mask) throws WSDLMarshalException {
    //init(clear) the state for new processing    
    documents = new Hashtable();
    msgQNames.clear();
    
    if ((mask & Interface.INTERFACE_ID) != 0) {
      ObjectList intfs = def.getChildren(Interface.INTERFACE_ID);
      for (int i = 0; i < intfs.getLength(); i++) {
        serializeInterface((Interface) intfs.item(i));
      }
    }
    
    if ((mask & SOAPBinding.SOAPBINDING_ID) != 0) {
      ObjectList bindings = def.getChildren(SOAPBinding.SOAPBINDING_ID);
      for (int i = 0; i < bindings.getLength(); i++) {
        serializeSoapBinding((SOAPBinding) bindings.item(i));
      }
    }
    
    if ((mask & HTTPBinding.HTTPBINDING_ID) != 0) {
      ObjectList bindings = def.getChildren(HTTPBinding.HTTPBINDING_ID);
      for (int i = 0; i < bindings.getLength(); i++) {
        serializeHttpBinding((HTTPBinding) bindings.item(i));
      }      
    }
    
    if ((mask & Service.SERVICE_ID) != 0) {
      ObjectList services = def.getChildren(Service.SERVICE_ID);
      for (int i = 0; i < services.getLength(); i++) {
        serializeService((Service) services.item(i));
      }
    }
    
    Iterator itr = documents.values().iterator();
    while (itr.hasNext()) {
      normalizeDefinitions(((WSDLDescriptor) itr.next()).getWsdl()); 
    }
    //add the extension elements after the normalization, since they need to be at the end.    
    applyDefinitionsExtensionElements(def);
    
    return new ArrayList(documents.values());
  }
   
  /**
   * Saves wsdls into given directory. The file names are taken from the WSDLDescriptor.getFileName().
   * By default the .wsdl extension is added to the file name.
   * Correct relative import locations are generated.
   */  
  public void save(List wsDescriptors, String dir) throws WSDLMarshalException {
    save(wsDescriptors, dir, null); 	  	  	
  }
  
  public void save(List wsDescriptors, JarOutputStream jarOut) throws WSDLMarshalException {
    save(wsDescriptors, null, jarOut); 	  	  	
  }
  
  public void save(List wsDescriptors, String dir, JarOutputStream jarOut) throws WSDLMarshalException {    
    try {//loading transformer if not already loaded
      if (this.transformer == null) {
        TransformerFactory tf = TransformerFactory.newInstance();
        this.transformer = tf.newTransformer();
        this.transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        /* In order to pass buggy CTS test:
         * com/sun/ts/tests/jaxws/wsi/j2w/rpc/literal/R4003/Client.java#testDescriptionEncoding
         * The test checks the 'encoding' value in case sensitive way - 'UTF-8'.
         * By omitting the declaration the test passes.
         */
        this.transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      }
    } catch (Exception e) {
      throw new WSDLMarshalException(e);
    }
    
    //creating destination dir, if it does not exist
    File fDir = null;  
    if(dir != null) {
      fDir = new File(dir);
      if (! fDir.exists()) {
        fDir.mkdirs();
      }
    }
    
    Map mappings = getNSToFileMap(wsDescriptors);   
    
    WSDLDescriptor descr;
    //process while all files are saved... 
    for (int i = 0; i < wsDescriptors.size(); i++) {
      descr = (WSDLDescriptor) wsDescriptors.get(i);
//      if (mappings.get(descr.getTargetNS()) == null) { //this entity is not processed
        save0(wsDescriptors, descr, mappings, fDir, jarOut);
//      }
    }
  }
  
  private Map getNSToFileMap(List wsDescriptors) {
    Map mappings = new HashMap();
    for (int i = 0; i < wsDescriptors.size(); i++) {
      WSDLDescriptor descr = (WSDLDescriptor) wsDescriptors.get(i);
      if (mappings.get(descr.getTargetNS()) == null) { //this entity is not processed
        mappings.put(descr.getTargetNS(), descr.getFileName());
      }
    }
    return mappings;
  }
  
  
  private void save0(List wsDescriptors, WSDLDescriptor descr, Map mappings, File destDir, JarOutputStream jarOut) throws WSDLMarshalException {
    String ns;
    WSDLDescriptor tmp;
//    if (mappings.get(descr.getTargetNS()) == null) { //this entity is not processed
//      List imports = descr.getImports();
//      for (int i = 0; i < imports.size(); i++) {
//        ns = (String) imports.get(i); 
//        if (mappings.get(ns) == null) {
//          //find the WSDLDescriptor with namespace <ns>
//          int p = 0;
//          for ( ; p < wsDescriptors.size(); p++) {
//            tmp = (WSDLDescriptor) wsDescriptors.get(p);
//            if (ns.equals(tmp.getTargetNS())) {
//              save0(wsDescriptors, tmp, mappings, destDir, jarOut);
//              break;                       
//            }
//          }
//          if (p == wsDescriptors.size()) { // in case 
//            throw new WSDLMarshalException("Missing WSDL with targetNamespace " + ns);
//          }
//        }
//      }
      
      //update the imports with correct locations
      String nsAttr, fName, locAttr;
      Element def = descr.getWsdl();
      Element impEl;
      List impEls = DOM.getChildElementsByTagNameNS(def, WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);
      for (int i = 0; i < impEls.size(); i++) {
        impEl = (Element) impEls.get(i);
        nsAttr = impEl.getAttribute(WSDL11Constants.NAMESPACE_ATTR);
        fName = (String) mappings.get(nsAttr);
        if (fName == null) { 
          throw new WSDLMarshalException("Missing WSDL with targetNamespace: " + nsAttr + ". Current mappings " + mappings + ". Wsdl document: " + def);
        }
        locAttr = "./" + fName;        
        if(jarOut != null) {         
          String relDir = ""; 
          int cutIndex = descr.getFileName().lastIndexOf("/");  	
          if(cutIndex != -1) {
            relDir = descr.getFileName().substring(0, cutIndex);	  
          }
          if(relDir.equals("")) {
            locAttr = "./" + fName;
          } else {
            locAttr = "./" + fName.substring(fName.indexOf(relDir) + relDir.length() + 1);
          }
        }
        impEl.setAttribute(WSDL11Constants.LOCATION_ATTR, locAttr);
      }
      //save the updated DOM to file
      String resultFileName = descr.getFileName();
      Result streamRes; 
      if(destDir != null) {
        File outputFile = new File(destDir, resultFileName);
        File parentDir = outputFile.getParentFile(); 
        if (parentDir != null && !parentDir.exists()) {
          parentDir.mkdirs();
        }
        streamRes = new StreamResult(outputFile.getAbsolutePath()); //convert to string because of an issue with whitespaces in file names and StreamResult(File) constructor
        try {
          transformer.transform(new DOMSource(def), streamRes);       
        } catch (Exception e) {
          throw new WSDLMarshalException(e);
        }
      } else {
        try {
    	  jarOut.putNextEntry(new JarEntry(resultFileName)); 
    	  streamRes = new StreamResult(jarOut);
    	  transformer.transform(new DOMSource(def), streamRes);       
        } catch (Exception e) {
          throw new WSDLMarshalException(e);
        } finally {
          try {
            jarOut.closeEntry(); 	  
          } catch(Exception e) {
            // $JL-EXC$	  
          }	
        }
      }
//      mappings.put(descr.getTargetNS(), resultFileName);
//    }
  }

  private Element serializeHttpBinding(HTTPBinding binding) throws WSDLMarshalException {
    QName name = binding.getName();
    WSDLDescriptor descr = createWSDLDescriptor(name.getNamespaceURI());
    descr.addBinding(name.getLocalPart());
    Element defs = descr.getWsdl();
    Element bindingEl = createAndAppendElement(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT);
    bindingEl.setAttribute(WSDL11Constants.NAME_ATTR, name.getLocalPart());
    bindingEl.setAttributeNS(NS.XMLNS, "xmlns:" + WSDL11Constants.HTTP_PREF, WSDL11Constants.HTTP_NS);
    bindingEl.setAttributeNS(NS.XMLNS, "xmlns:" + WSDL11Constants.MIME_PREF, WSDL11Constants.MIME_NS);
    
    //set type reference
    String pref = createDefinitionsPrefix(defs, binding.getInterface().getNamespaceURI());
    bindingEl.setAttribute(WSDL11Constants.TYPE_ATTR, pref + ":" + binding.getInterface().getLocalPart());
    //add import for the portType wsdl
    Interface intf = ((Definitions) binding.getParent()).getInterface(binding.getInterface());
    addImportForNamespace(descr, binding.getInterface().getNamespaceURI(), intf.getProperty(Interface.IMPORT_LOCATION));
    
    //append http binding extension element
    Element httpBEl = createAndAppendElement(bindingEl, WSDL11Constants.HTTP_NS, WSDL11Constants.BINDING_ELEMENT);
    String httpMethod = binding.getProperty(HTTPBinding.HTTP_METHOD);
    httpBEl.setAttribute(WSDL11Constants.VERB_ATTR, httpMethod);
    
    //process operations
    HTTPBindingOperation curO;
    Element curOpE, httpEl, inputEl, outputEl;
    ObjectList ops = binding.getOperations();
    for (int i = 0; i < ops.getLength(); i++) {
      curO = (HTTPBindingOperation) ops.item(i);
      //create operation element
      curOpE = createAndAppendElement(bindingEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
      curOpE.setAttribute(WSDL11Constants.NAME_ATTR, curO.getName());
      httpEl = createAndAppendElement(curOpE, WSDL11Constants.HTTP_NS, WSDL11Constants.OPERATION_ELEMENT);
      String location = curO.getProperty(HTTPBindingOperation.LOCATION);
      httpEl.setAttribute(WSDL11Constants.LOCATION_ATTR, location);
      //create input channel
      String inputSerialization = curO.getProperty(HTTPBindingOperation.INPUT_SERIALIZATION);
      if (HTTPBindingOperation.URLENCODED_SERIALIZATION.equals(inputSerialization)) {
        inputEl = createAndAppendElement(curOpE, WSDL11Constants.WSDL_NS, WSDL11Constants.INPUT_ELEMENT);
        if (HTTPBinding.HTTP_GET_METHOD.equals(httpMethod)) {
          createAndAppendElement(inputEl, WSDL11Constants.HTTP_NS, WSDL11Constants.URLENCODED_ELEMENT);
        } else {
          Element mimeContent = createAndAppendElement(inputEl, WSDL11Constants.MIME_NS, WSDL11Constants.CONTENT_ELEMENT);
          mimeContent.setAttribute(WSDL11Constants.TYPE_ATTR, HTTPBindingOperation.URLENCODED_SERIALIZATION);
        }
      } else {
        throw new WSDLMarshalException(WSDLMarshalException.INVALID_IN_SERIALIZATION_HTTPOPERATION, new Object[]{inputSerialization, HTTPBindingOperation.URLENCODED_SERIALIZATION});
      }
      //create output channel
      String outputSerialization = curO.getProperty(HTTPBindingOperation.OUTPUT_SERIALIZATION);
      if (HTTPBindingOperation.MIMEXML_SERIALIZATION.equals(outputSerialization)) {
        outputEl = createAndAppendElement(curOpE, WSDL11Constants.WSDL_NS, WSDL11Constants.OUTPUT_ELEMENT);
        createAndAppendElement(outputEl, WSDL11Constants.MIME_NS, WSDL11Constants.MIMEXML_ELEMENT);
      } else {
        throw new WSDLMarshalException(WSDLMarshalException.INVALID_OUT_SERIALIZATION_HTTPOPERATION, new Object[]{outputSerialization, HTTPBindingOperation.MIMEXML_SERIALIZATION});
      }
    }
    return defs;
  }  
  
  private Element serializeInterface(Interface intf) throws WSDLMarshalException {
    QName name = intf.getName();
    WSDLDescriptor descr = createWSDLDescriptor(name.getNamespaceURI());
    descr.addInterface(name.getLocalPart());
    Element defs = descr.getWsdl();
    List types = DOM.getChildElementsByTagNameNS(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.TYPES_ELEMENT);
    //in case not types section is available add the schemas. If it is available the schemas have already been added.
    if (types.size() == 0) {
      Element typesEl = createAndAppendElement(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.TYPES_ELEMENT);    
      //append schemas
      Definitions def = (Definitions) intf.getParent();
      List schemas = def.getXSDTypeContainer().getSchemas();
      DOMSource schema;
      for (int i = 0; i < schemas.size(); i++) {
        schema  = (DOMSource) schemas.get(i);
        Element newSchema = (Element) document.importNode(schema.getNode(), true);
        typesEl.appendChild(newSchema);  
      }
    }

    Element portType = createAndAppendElement(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.PORTTYPE_ELEMENT);
    portType.setAttribute(WSDL11Constants.NAME_ATTR, name.getLocalPart());
    
    Operation curO;
    ObjectList ops = intf.getOperations();
    ObjectList params;
    String msgName;
    Parameter curP;
    for (int i = 0; i < ops.getLength(); i++) {
      curO = (Operation) ops.item(i);
      String mep = curO.getProperty(Operation.MEP);
      if (mep == null) {
        throw new WSDLMarshalException(WSDLMarshalException.INTERFACE_OPERATION_IS_MISSING_REQUIRED_PROPERTY, new Object[]{curO.getName(), Operation.MEP});
      }
      Element opEl = createAndAppendElement(portType, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
      opEl.setAttribute(WSDL11Constants.NAME_ATTR, curO.getName());
      //create input message
      params = curO.getParameters(Parameter.IN | Parameter.INOUT);
      msgName = appendMessage(params, defs, name.getLocalPart(), curO.getName(), MSG_IN);
      Element in = createAndAppendElement(opEl, WSDL11Constants.WSDL_NS, WSDL11Constants.INPUT_ELEMENT);
      in.setAttribute(WSDL11Constants.MESSAGE_ELEMENT, TNS + ":" +  msgName);
      mapMessage(name, curO.getName(), msgName, MSG_IN);
      if (Operation.INOUT_MEP.equals(mep)) { //only for request/response operation there is out message
        //create output message
        params = curO.getParameters(Parameter.OUT | Parameter.INOUT | Parameter.RETURN);
        msgName = appendMessage(params, defs, name.getLocalPart(), curO.getName(), MSG_OUT);
        Element out = createAndAppendElement(opEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OUTPUT_ELEMENT);
        out.setAttribute(WSDL11Constants.MESSAGE_ELEMENT, TNS + ":" +  msgName);      
        mapMessage(name, curO.getName(), msgName, MSG_OUT);
        //append the optional @parameterOrder attribute, in order buggy CTS15 test to pass. The test is com/sun/ts/tests/jws/webparam/webparam3/client/Client.java#testWSDL7
        //According to WSDL spec this attribute is relevant only for request-response operations.
        String paramOrder = generateParameterOrderAttValue(curO);
        if (paramOrder.length() > 0) {
          ObjectList ret = curO.getParameters(Parameter.RETURN);
          if (ret.getLength() == 1) {
            Parameter retParam = (Parameter) ret.item(0);
            if (paramOrder.indexOf(retParam.getName()) == -1) {
              opEl.setAttribute(WSDL11Constants.PARAMETER_ORDER_ATTR, paramOrder);
            } else {
              //when the return parameter has similar name with input parameter,
              //declaring the parameterOrder with its name would make the return to be INOUT, which is incorrect. 
              //Discovered by document/literal esr wsdl where the parts of the input and output message both were named 'parameters'
            }
          } else { //if no return parameters, append the paramOrder
            opEl.setAttribute(WSDL11Constants.PARAMETER_ORDER_ATTR, paramOrder);
          }
        }
      }
      //create fault messages. The fault messages must contain exactly one part. It is not filtered for INOUT_MEP, since the XI one-way operation could have faults.
      params = curO.getParameters(Parameter.FAULT);
      for (int p = 0; p < params.getLength(); p++) {
        curP = (Parameter) params.item(p);
        msgName = appendMessage(curP, defs, name.getLocalPart(), curO.getName(), MSG_FAULT);
        Element fault = createAndAppendElement(opEl, WSDL11Constants.WSDL_NS, WSDL11Constants.FAULT_ELEMENT);
        //check for predefined property 'fault-name' first. If not available reuse the parameter@name
        fault.setAttribute(WSDL11Constants.NAME_ATTR, curP.getProperty(WSDL11Constants.FAULT_NAME));
        fault.setAttribute(WSDL11Constants.MESSAGE_ELEMENT, TNS + ":" +  msgName);        
      }        
    }
    
    return defs;       
  }
  
  private String generateParameterOrderAttValue(Operation op) {
    StringBuffer buf = new StringBuffer();
    ObjectList params = op.getParameters(Parameter.IN | Parameter.INOUT | Parameter.OUT);
    for (int p = 0; p < params.getLength(); p++) {
      Parameter param = (Parameter) params.item(p);
      buf.append(param.getName());
      buf.append(" ");
    }
    return buf.toString().trim(); //the .trim() is neede to remove the trailing ' ', which causes issue with .NET proxy generator.

  }
  private Element serializeSoapBinding(SOAPBinding binding) throws WSDLMarshalException {
    QName name = binding.getName();
    WSDLDescriptor descr = createWSDLDescriptor(name.getNamespaceURI());
    descr.addBinding(name.getLocalPart());
    Element defs = descr.getWsdl();
    Element bindingEl = createAndAppendElement(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT);
    bindingEl.setAttribute(WSDL11Constants.NAME_ATTR, name.getLocalPart());
    bindingEl.setAttributeNS(NS.XMLNS, "xmlns:" + WSDL11Constants.SOAP_PREF, WSDL11Constants.SOAP_NS);
    
    //set type reference
    String pref = createDefinitionsPrefix(defs, binding.getInterface().getNamespaceURI());
    bindingEl.setAttribute(WSDL11Constants.TYPE_ATTR, pref + ":" + binding.getInterface().getLocalPart());
    //add import for the portType wsdl
    Interface intf = ((Definitions) binding.getParent()).getInterface(binding.getInterface());
    addImportForNamespace(descr, binding.getInterface().getNamespaceURI(), intf.getProperty(Interface.IMPORT_LOCATION));
    
    //append soap binding extension element
    Element soapBEl = createAndAppendElement(bindingEl, WSDL11Constants.SOAP_NS, WSDL11Constants.BINDING_ELEMENT);
    String style = binding.getProperty(SOAPBinding.STYLE);
    if (style != null) {
      soapBEl.setAttribute(WSDL11Constants.STYLE_ATTR, style);
    }
    String transport = binding.getProperty(SOAPBinding.TRANSPORT);
    if (transport != null) {
      soapBEl.setAttribute(WSDL11Constants.TRANSPORT_ATTR, transport);
    }
    
    SOAPBindingOperation curO;
    Element curOpE, soapEl;
    ObjectList ops = binding.getOperations();
    for (int i = 0; i < ops.getLength(); i++) {
      curO = (SOAPBindingOperation) ops.item(i);
      //create operation element
      curOpE = createAndAppendElement(bindingEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT);
      curOpE.setAttribute(WSDL11Constants.NAME_ATTR, curO.getName());
      soapEl = createAndAppendElement(curOpE, WSDL11Constants.SOAP_NS, WSDL11Constants.OPERATION_ELEMENT);
      String soapAction = curO.getProperty(SOAPBindingOperation.SOAPACTION);
      if (soapAction != null) {
        soapEl.setAttribute(WSDL11Constants.SOAPACTION_ATTR, soapAction);
      }
      style = curO.getProperty(SOAPBindingOperation.STYLE);
      if (style != null) {
        soapEl.setAttribute(SOAPBindingOperation.STYLE, style);
      }
      Operation intfOp = intf.getOperation(curO.getName());
      appendSoapOperationInOutChannel(curO, curOpE, MSG_IN);
      if (Operation.INOUT_MEP.equals(intfOp.getProperty(Operation.MEP))) { //do not generate output for one-way operations
        appendSoapOperationInOutChannel(curO, curOpE, MSG_OUT);
      }
      appendSoapOperationFaultChannel(curO, curOpE); //generate faults for request-response and one-way operations - the latter is only valid for XI wsdls
    }
    return defs;
  }
  
  private void serializeService(Service service) throws WSDLMarshalException {
    QName name = service.getName();
    WSDLDescriptor descr = createWSDLDescriptor(name.getNamespaceURI());
    descr.addService(name.getLocalPart());
    Element defs = descr.getWsdl();
    Element serviceEl = createAndAppendElement(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT);
    serviceEl.setAttribute(WSDL11Constants.NAME_ATTR, name.getLocalPart());
    
    Endpoint curE;
    Binding b;
    ObjectList endPs = service.getEndpoints();
    for (int i = 0; i < endPs.getLength(); i++) {
      curE = (Endpoint) endPs.item(i);
      QName bQName = curE.getBinding();
      b = ((Definitions) service.getParent()).getBinding(bQName);
      Element portEl = createAndAppendElement(serviceEl, WSDL11Constants.WSDL_NS, WSDL11Constants.PORT_ELEMENT);
      portEl.setAttribute(WSDL11Constants.NAME_ATTR, curE.getName());
      //set type reference
      String pref = createDefinitionsPrefix(defs, bQName.getNamespaceURI());
      portEl.setAttribute(WSDL11Constants.BINDING_ELEMENT, pref + ":" + bQName.getLocalPart());
      //add import for the binding wsdl
      addImportForNamespace(descr, bQName.getNamespaceURI(), b.getProperty(Binding.IMPORT_LOCATION));
      //add binding extensions  
      Binding binding = ((Definitions) service.getParent()).getBinding(bQName);
      if (binding.getBindingType().equals(SOAPBinding.SOAP_BINDING)) {
        Element soapAddress = createAndAppendElement(portEl, WSDL11Constants.SOAP_NS, WSDL11Constants.ADDRESS_ELEMENT);
        soapAddress.setAttribute(WSDL11Constants.LOCATION_ATTR, curE.getProperty(Endpoint.URL));  
      } else if (binding.getBindingType().equals(HTTPBinding.HTTP_BINDING)) {
        Element httpAddress = createAndAppendElement(portEl, WSDL11Constants.HTTP_NS, WSDL11Constants.ADDRESS_ELEMENT);
        httpAddress.setAttribute(WSDL11Constants.LOCATION_ATTR, curE.getProperty(Endpoint.URL));          
      }
    }
  }
  
  private void appendSoapOperationInOutChannel(SOAPBindingOperation op, Element opEl, String mode) {
    SOAPBinding soapBinding = (SOAPBinding) op.getParent();
    Element defsEl = (Element) opEl.getParentNode().getParentNode();
    String use = op.getProperty(SOAPBindingOperation.USE);
    String parts, namespace, encodingStyle, headers; 
    Element channel;
    AttachmentsContainer attContainer;
    if (MSG_IN.equals(mode)) {
      parts = op.getProperty(SOAPBindingOperation.INPUT_PARTS);
      namespace = op.getProperty(SOAPBindingOperation.INPUT_NAMESPACE);
      encodingStyle = op.getProperty(SOAPBindingOperation.INPUT_ENCODINGSTYLE);
      headers = op.getProperty(SOAPBindingOperation.IN_HEADERS);
      channel = createAndAppendElement(opEl, WSDL11Constants.WSDL_NS, WSDL11Constants.INPUT_ELEMENT);
      attContainer = op.getInputAttachmentsContainer();
    } else {      
      parts = op.getProperty(SOAPBindingOperation.OUTPUT_PARTS);
      namespace = op.getProperty(SOAPBindingOperation.OUTPUT_NAMESPACE);
      encodingStyle = op.getProperty(SOAPBindingOperation.OUTPUT_ENCODINGSTYLE);
      headers = op.getProperty(SOAPBindingOperation.OUT_HEADERS);
      channel = createAndAppendElement(opEl, WSDL11Constants.WSDL_NS, WSDL11Constants.OUTPUT_ELEMENT);
      attContainer = op.getOutputAttachmentsContainer();      
    }
    Element multiPart = null;
    Element body = null;
    //there is MIME extensions
    if (attContainer != null) {
      multiPart = createAndAppendElement(channel, WSDL11Constants.MIME_NS, WSDL11Constants.MULTIPART_RELATED_ELEMENT);
      Element bodyMimePart = createAndAppendElement(multiPart, WSDL11Constants.MIME_NS, WSDL11Constants.CONTENT_ELEMENT);
      //Make the channel reference to bodyMimePart in order the source down, for soap:body and headers, to be reused
      channel = bodyMimePart;
      //set attachments if any
      MIMEPart tmpAtt;
      String pName;
      Element mimePart, mimeContent;
      ObjectList atts = attContainer.getMIMEParts();
      for (int i = 0; i < atts.getLength(); i++) {
        tmpAtt = (MIMEPart) atts.item(i);
        pName = tmpAtt.getPartName();
        mimePart = createAndAppendElement(multiPart, WSDL11Constants.MIME_NS, WSDL11Constants.PART_ELEMENT);
        List contentTypes = tmpAtt.getMimeTypeAlternatives();
        for (int a = 0; a < contentTypes.size(); a++) { 
          mimeContent = createAndAppendElement(mimePart, WSDL11Constants.MIME_NS, WSDL11Constants.CONTENT_ELEMENT);
          mimeContent.setAttribute(WSDL11Constants.PART_ELEMENT, pName);
          mimeContent.setAttribute(WSDL11Constants.TYPE_ATTR, (String) contentTypes.get(a));
        }
      }
    }    
    //create Body as child of channel. 
    body = createAndAppendElement(channel, WSDL11Constants.SOAP_NS, WSDL11Constants.SOAPBODY_ELEMENT);
    
    if (parts != null) {
      body.setAttribute(WSDL11Constants.PARTS_ATTR, parts); 
    }
    if (namespace != null) {
      body.setAttribute(WSDL11Constants.NAMESPACE_ATTR, namespace);       
    }
    if (encodingStyle != null) {
      body.setAttribute(WSDL11Constants.ENCODEDSTYLE_ATTR , encodingStyle);
    }
    body.setAttribute(WSDL11Constants.USE_ATTR, op.getProperty(SOAPBindingOperation.USE));
    
    if (headers != null) {
      QName ptQName = soapBinding.getInterface();
      QName msgQName = getMsgQName(ptQName, op.getName(), mode);
      String pref = createDefinitionsPrefix(defsEl, msgQName.getNamespaceURI()); 
      StringTokenizer t = new StringTokenizer(headers);
      String header;
      while (t.hasMoreTokens()) {
        header = t.nextToken();
        Element headerEl = createAndAppendElement(channel, WSDL11Constants.SOAP_NS, WSDL11Constants.SOAPHEADER_ELEMENT);
        headerEl.setAttribute(WSDL11Constants.MESSAGE_ELEMENT, pref + ":" + msgQName.getLocalPart());  
        headerEl.setAttribute(WSDL11Constants.PART_ELEMENT, header);  
        headerEl.setAttribute(WSDL11Constants.USE_ATTR, SOAPBindingOperation.USE_LITERAL);          
      }
    }
    
    //processing additional headers
    ObjectList addHeaders = null;
    if (MSG_IN.equals(mode)) {
      addHeaders = op.getInAdditionalHeaders();
    } else {
      addHeaders = op.getOutAdditionalHeaders();
    }
    Parameter curP;
    for (int i = 0; i < addHeaders.getLength(); i++) {
      curP = (Parameter) addHeaders.item(i);
      //create message
      String msgName = appendMessage(curP, defsEl, soapBinding.getName().getLocalPart(), op.getName(), MSG_HEADERS + mode);
      //append header element
      Element headerEl = createAndAppendElement(channel, WSDL11Constants.SOAP_NS, WSDL11Constants.SOAPHEADER_ELEMENT);
      String pref = createDefinitionsPrefix(defsEl, soapBinding.getName().getNamespaceURI());
      headerEl.setAttribute(WSDL11Constants.MESSAGE_ELEMENT, pref + ":" + msgName);  
      headerEl.setAttribute(WSDL11Constants.PART_ELEMENT, curP.getName());  
      headerEl.setAttribute(WSDL11Constants.USE_ATTR, SOAPBindingOperation.USE_LITERAL);                
    }
  }
  
  private void appendSoapOperationFaultChannel(SOAPBindingOperation op, Element opEl) {    
    //create faults
    Definitions definitions = (Definitions) op.getParent().getParent();
    SOAPBinding soapBinding = (SOAPBinding) op.getParent();
    Interface intf = definitions.getInterface(soapBinding.getInterface());
    ObjectList faults = intf.getOperation(op.getName()).getParameters(Parameter.FAULT);
    Parameter curP;
    for (int i = 0; i < faults.getLength(); i++) {
      curP = (Parameter) faults.item(i);
      Element faultEl = createAndAppendElement(opEl, WSDL11Constants.WSDL_NS, WSDL11Constants.FAULT_ELEMENT);
      Element soapFault = createAndAppendElement(faultEl, WSDL11Constants.SOAP_NS, WSDL11Constants.FAULT_ELEMENT);
      //soapFault.setAttribute(WSDL11Constants.NAME_ATTR, curP.getProperty(WSDL11Constants.FAULT_NAME));
      soapFault.setAttribute(WSDL11Constants.USE_ATTR, SOAPBindingOperation.USE_LITERAL);
      faultEl.setAttribute(WSDL11Constants.NAME_ATTR, curP.getProperty(WSDL11Constants.FAULT_NAME));
      soapFault.setAttribute(WSDL11Constants.NAME_ATTR, curP.getProperty(WSDL11Constants.FAULT_NAME));
    }    
  }

  private String appendMessage(Object params, Element defs, String pTName, String opName, String msgMode) {
    String msgName = getMessageName(defs, pTName, opName, msgMode);
    Element msgEl = createAndAppendElement(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.MESSAGE_ELEMENT);
    msgEl.setAttribute(WSDL11Constants.NAME_ATTR, msgName);
    
    if (params instanceof Parameter) {
      appendPart((Parameter) params, defs, msgEl);
    } else {
      ObjectList objList = (ObjectList) params;
      Parameter curP;      
      for (int p = 0; p < objList.getLength(); p++) {
        curP = (Parameter) objList.item(p);
        appendPart(curP, defs, msgEl);
      }      
    }
    return msgName;
  }
  
  private void appendPart(Parameter p, Element defs, Element msgEl) {
    XSDRef xsdRef;
    Element partEl = createAndAppendElement(msgEl, WSDL11Constants.WSDL_NS, WSDL11Constants.PART_ELEMENT);
    partEl.setAttribute(WSDL11Constants.NAME_ATTR, p.getName());
    xsdRef = p.getXSDTypeRef();
    String ns = xsdRef.getQName().getNamespaceURI();
    String prefix;
    if (ns == null || ns.length() == 0) {
      prefix = null;
    } else {
      prefix = createDefinitionsPrefix(defs, ns);
    }
    if (xsdRef.getXSDType() == XSDRef.ELEMENT) {
      if (prefix != null) {
        partEl.setAttribute(WSDL11Constants.ELEMENT_ATTR, prefix + ":" + xsdRef.getQName().getLocalPart());
      } else {
        partEl.setAttributeNS(NS.XMLNS, "xmlns", ""); //remove ns for this part, inorder to be used by the following attribute value
        partEl.setAttribute(WSDL11Constants.ELEMENT_ATTR, xsdRef.getQName().getLocalPart());
      }
    } else if (xsdRef.getXSDType() == XSDRef.TYPE) {
      if (prefix != null) {
        partEl.setAttribute(WSDL11Constants.TYPE_ATTR, prefix + ":" + xsdRef.getQName().getLocalPart());
      } else {
        partEl.setAttributeNS(NS.XMLNS, "xmlns", ""); //remove ns for this part, inorder to be used by the following attribute value
        partEl.setAttribute(WSDL11Constants.TYPE_ATTR, xsdRef.getQName().getLocalPart());
      }
    }                          
  }

  private Element createAndAppendElement(Element base, String ns, String localName/*, Element insBefore*/) {
    Element newEl;
    if (WSDL11Constants.SOAP_NS.equals(ns)) {
      localName = WSDL11Constants.SOAP_PREF + ":" + localName;
    } else if (WSDL11Constants.MIME_NS.equals(ns)) {
      localName = WSDL11Constants.MIME_PREF + ":" + localName;      
    } else if (WSDL11Constants.HTTP_NS.equals(ns)) {
      localName = WSDL11Constants.HTTP_PREF + ":" + localName;            
    } else if (WSDL11Constants.WSDL_NS.equals(ns)) {
      localName = WSDL11Constants.WSDL_PREF + ":" + localName;
    }
    if (ns != null) {
      newEl = base.getOwnerDocument().createElementNS(ns, localName);      
    } else {
      newEl = base.getOwnerDocument().createElement(localName);
    }
//    if (insBefore != null) {
//      base.insertBefore(newEl, insBefore);
//    } else {
      base.appendChild(newEl);
//    }
    
    return newEl;
  }
  
  private String getMessageName(Element def, String pTypeName, String opName, String suffix) {
    String opSeed = opName + suffix;
    if (! existNamedChildElement(def, WSDL11Constants.WSDL_NS, WSDL11Constants.MESSAGE_ELEMENT, opSeed)) {
      return opSeed;      
    }
    String pTypeSeed = pTypeName + opSeed;
    if (! existNamedChildElement(def, WSDL11Constants.WSDL_NS, WSDL11Constants.MESSAGE_ELEMENT, pTypeSeed)) {
      return pTypeSeed;      
    }
    int count = 1;
    while (true) {
      pTypeSeed += Integer.toString(count);
      if (! existNamedChildElement(def, WSDL11Constants.WSDL_NS, WSDL11Constants.MESSAGE_ELEMENT, pTypeSeed)) {
        return pTypeSeed;
      }
      count++;
    }
  }
  
  private boolean existNamedChildElement(Element root, String ns, String localName, String nameAttrV) {
    List list = DOM.getChildElementsByTagNameNS(root, ns, localName);
    Element cur;
    for (int i = 0; i < list.size(); i++) {
      cur = (Element) list.get(i);
      if (cur.getAttribute(WSDL11Constants.NAME_ATTR).equals(nameAttrV)) {
        return true;     
      }
    }
    return false;
  }

  private WSDLDescriptor createWSDLDescriptor(String ns) { 
    WSDLDescriptor d = (WSDLDescriptor) documents.get(ns);
    if (d != null) {
      return d;
    }
    Element newElement = document.createElementNS(WSDL11Constants.WSDL_NS, WSDL11Constants.WSDL_PREF + ":" + WSDL11Constants.DEFINITIONS_ELEMENT);
    newElement.setAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR, ns);
    newElement.setAttributeNS(NS.XMLNS, "xmlns:" + TNS, ns);
    d = new WSDLDescriptor(newElement, ns);
    documents.put(ns, d);
    return d;
  }  
  
  private String createDefinitionsPrefix(Element defs, String ns) {
    String pref = DOM.getPrefixForNS(defs, ns);
    if (pref != null) {
      return pref;
    }
    
    int n = 0;
    boolean end = false;
    while (! end) {
      pref = PREFIX_BASE + Integer.toString(n);
      if (defs.getAttributeNodeNS(NS.XMLNS, pref) == null) {
        end = true;
        defs.setAttributeNS(NS.XMLNS, "xmlns:" + pref, ns);  
      }
      n++;
    }
    return pref;        
  }
  
  private void mapMessage(QName ptQName, String opName, String msgName, String msgType) {
    QName msgQName = new QName(ptQName.getNamespaceURI(), msgName);
    String key = ptQName.toString() + opName + msgType;
    this.msgQNames.put(key, msgQName);
  }
  
  private QName getMsgQName(QName ptQName, String opName, String msgType) {
    String key = ptQName.toString() + opName + msgType;
    return (QName) this.msgQNames.get(key);    
  }
  
  private void normalizeDefinitions(Element defs) {
    List pTs = DOM.getChildElementsByTagNameNS(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.PORTTYPE_ELEMENT);    
    List bs = DOM.getChildElementsByTagNameNS(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT);
    List ss = DOM.getChildElementsByTagNameNS(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT);
    //join
    pTs.addAll(bs);
    pTs.addAll(ss);
    
    //remove nodes
    for (int i = 0; i < pTs.size(); i++) {
      defs.removeChild((Element) pTs.get(i));
    }
    //append nodes
    for (int i = 0; i < pTs.size(); i++) {
      defs.appendChild((Element) pTs.get(i));
    }
    
    List imports = DOM.getChildElementsByTagNameNS(defs, WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);
    for (int i = 0; i < imports.size(); i++) {
      defs.removeChild((Element) imports.get(i));
    }
    Node first = defs.getFirstChild();
    for (int i = 0; i < imports.size(); i++) {
      defs.insertBefore((Element) imports.get(i), first);
    }
  }
  
  private void addImportForNamespace(WSDLDescriptor descr, String ns, String locValue) throws WSDLMarshalException {
    Element def = descr.getWsdl();
    if (def.getAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR).equals(ns)) { //the namespace is the same as the document one
      return;
    }
    
    List imports = DOM.getChildElementsByTagNameNS(def, WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);    
    Element imp;
    for (int i = 0; i < imports.size(); i++) {
      imp = ((Element) imports.get(i)); 
      if (imp.getAttribute(WSDL11Constants.NAMESPACE_ATTR).equals(ns)) {
        String baseLoc = imp.getAttribute(WSDL11Constants.LOCATION_ATTR);
        if ((locValue != null && locValue.length() > 0 && baseLoc.length() == 0) || (baseLoc.length() > 0 && locValue == null) 
            || (! baseLoc.equals(locValue))) {
          throw new WSDLMarshalException(WSDLMarshalException.MORE_THAN_ONE_LOCATIONS_FOR_NS, new Object[]{ns, baseLoc, locValue}); 
        }
        return;
      }
    } 
    if (locValue == null) { //
      locValue = "";      
    }
    Element imprt = createAndAppendElement(def, WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT);
    imprt.setAttribute(WSDL11Constants.LOCATION_ATTR, locValue);
    imprt.setAttribute(WSDL11Constants.NAMESPACE_ATTR, ns);       
    descr.addImport(ns);
  }
  
  private void applyDefinitionsExtensionElements(Definitions def) {
    ObjectList extensions = def.getChildren(ExtensionElement.EXTENSION_ELEMENT_ID);
    for (int i = 0; i < extensions.getLength(); i++) {
      ExtensionElement extEl = (ExtensionElement) extensions.item(i);
      Element el_tobe_imported = extEl.getContent();
      Node node = el_tobe_imported.getParentNode();
      if (node instanceof Element) {
        Element parent = (Element) node;
        String ns = parent.getAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR);
        WSDLDescriptor descr = documents.get(ns);
        if (descr != null) {
          Element defEl = descr.getWsdl();
          el_tobe_imported = (Element) defEl.getOwnerDocument().importNode(el_tobe_imported, true);
          defEl.appendChild(el_tobe_imported);
        }
      }
    }    
  }
  
}
