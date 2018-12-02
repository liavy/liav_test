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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.mappings.EndpointMapping;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.mappings.ServiceMapping;
import com.sap.engine.services.webservices.espbase.wsdl.AttachmentsContainer;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.espbase.wsdl.Parameter;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.XSDRef;
import com.sap.engine.services.webservices.espbase.wsdl.XSDTypeContainer;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLMarshalException;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-29
 */
public class InterfaceMappingProcessor {
  public static final String SOAP_RPC_LITERAL_STYLE  =  "rpc-literal";
  public static final String SOAP_DOC_LITERAL_STYLE  =  "doc-literal";
  public static final String SCHEMA_TARGETNS_ATTR  =  "targetNamespace";
  public static final String SCHEMA_ELEMENT  =  "schema";
  public static final String SCHEMA_ELEMENTFORMDEFAULT_ATTR  =  "elementFormDefault";
  public static final String ELEMENT  =  "element";
  public static final String NAME_ATTR  =  "name";
  public static final String TYPE_ATTR  =  "type";
  public static final String IMPORT_ELEMENT  =  "import";
  public static final String NAMESPACE_ATTR  =  "namespace";
  public static final String SCHEMA_PREFIX  =  "xs";
  public static final String BASE_PREFIX  =  "s";
  public static final String SEQUENCE_ELEMENT  =  "sequence";
  public static final String COMPLEXTYPE_ELEMENT  =  "complexType";
  public static final String MINOCCURS_ATTR  =  "minOccurs";
  public static final String NILLABLE_ATTR  =  "nillable";
  public static final String DOCUMENT_INPUT_PART_NAME  =  "parameters";
  public static final String DOCUMENT_OUTPUT_PART_NAME  =  "return";
  
  /**
   * Takes 'document' or 'bare' interface mapping created out of Java annotated classes by using JAX-WS algorithm for java-to-wsdl
   * mapping and schemas and returns wsdl definition.
   * @param mappings interface mapping
   * @param srvMap service mapping containing service and port info
   * @param schemas schemas array
   * @return definitions
   * @throws WSDLException
   */
  public Definitions processJEEDocumentAndBareMappings(InterfaceMapping mappings, ServiceMapping srvMap, DOMSource schemas[]) throws WSDLException {
    Definitions def = new Definitions();
    //add Interface and SOAPBinding
    QName qname = mappings.getPortType();
    QName bindingQN = mappings.getBindingQName();
    Interface intf = def.appendInterface(qname);
    SOAPBinding binding = new SOAPBinding(new QName(bindingQN.getNamespaceURI(), bindingQN.getLocalPart()));
    binding.setProperty(SOAPBinding.STYLE, SOAPBinding.DOC_STYLE_VALUE);
    binding.setProperty(SOAPBinding.TRANSPORT, SOAPBinding.TRANSPORT_SOAP_OVER_HTTP);    
    binding.setInterface(mappings.getPortType());
    def.appendChild(binding);
    //create service and port
    Service srv = def.appendService(srvMap.getServiceName());
    EndpointMapping[] endpoints = srvMap.getEndpoint();
    for (int i = 0; i < endpoints.length; i++) {
      Endpoint ep = srv.appendEndpoint(QName.valueOf(endpoints[i].getPortQName()).getLocalPart());
      ep.setBinding(bindingQN);
    }
    //mapping the schemas by their namespaces
    Map mapSchemas = mapSchemas(schemas);
    
    //append operations to interface and to binding. 
    OperationMapping[] operations = mappings.getOperation();    
    OperationMapping curOp;
    for (int i = 0; i < operations.length; i++) {
      curOp = operations[i];
      appendJEEDocumentOperation(curOp, intf, binding);
    }
    //append the schemas to the definitions
    XSDTypeContainer xsdContainer = def.getXSDTypeContainer();    
    Iterator itr = mapSchemas.values().iterator();
    Element elem;
    while (itr.hasNext()) {
      elem = (Element) itr.next();
      xsdContainer.addSchema(new DOMSource(elem));
    }
    return def;
  }  
  
  /**
   * Takes 'rpc' interface mapping created out of Java annotated classes by using JAX-WS algorithm for java-to-wsdl
   * mapping and schemas and returns wsdl definition.
   * @param mappings interface mapping
   * @param srvMap service mapping containing service and port info
   * @param schemas schemas array
   * @return definitions
   * @throws WSDLException
   */
  public Definitions processJEERPCMappings(InterfaceMapping mappings, ServiceMapping srvMap, DOMSource schemas[]) throws WSDLException {
    Definitions def = processSoapRpcLiteralStyle(mappings, schemas, true);
    //create service and port
    Service srv = def.appendService(srvMap.getServiceName());
    EndpointMapping[] endpoints = srvMap.getEndpoint();
    for (int i = 0; i < endpoints.length; i++) {
      Endpoint ep = srv.appendEndpoint(QName.valueOf(endpoints[i].getPortQName()).getLocalPart());
      ep.setBinding(mappings.getBindingQName());
    }
    return def;
  }
  
  private void appendJEEDocumentOperation(OperationMapping opMap, Interface intf, SOAPBinding binding) throws WSDLException {
    //create portType operation
    String opName = opMap.getWSDLOperationName();
    Operation intfOp = intf.appendOperation(opName);
    //set MEP property;
    appendMEPProperty(intfOp, opMap);
    //process input
    String reqWrapper = opMap.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER);
    String inNS = opMap.getProperty(OperationMapping.INPUT_NAMESPACE);
    QName inputELQName = new QName(inNS, reqWrapper);
    String inPartName = reqWrapper;
    if (OperationMapping.DOCUMENT_BARE_OPERATION_STYLE.equals(opMap.getProperty(OperationMapping.OPERATION_STYLE))) {
      if (opMap.getProperty(OperationMapping.DOCUMENT_BARE_OPERATION_REQ_PARTNAME) != null) {
        inPartName = opMap.getProperty(OperationMapping.DOCUMENT_BARE_OPERATION_REQ_PARTNAME);
      }
    } else if ("document".equals(opMap.getProperty(OperationMapping.OPERATION_STYLE))) {
      //the jax-ws ri 2.0, works with document/wrapped style correctly, only if the name of the only part in the message
      //is called "parameters". This is why for normal document style, the name is preset to this default
      inPartName = "parameters";
    }
    Parameter inParam = intfOp.appendParameter(inPartName, Parameter.IN);
    inParam.appendXSDTypeRef(inputELQName, XSDRef.ELEMENT);
    //map input headers
    String inHeadersList = "";
    ParameterMapping[] inHs = opMap.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE);
    for (ParameterMapping p: inHs) {
      if (p.isHeader()) {
        Parameter hP = intfOp.appendParameter(p.getWSDLParameterName(), getWSDLParamType(p.getParameterType()));
        hP.appendXSDTypeRef(p.getSchemaQName(), XSDRef.ELEMENT);
        inHeadersList += " " + p.getWSDLParameterName();
      }
    }
    //process output
    String respWrapper = opMap.getProperty(OperationMapping.SOAP_RESPONSE_WRAPPER);
    String outNS = opMap.getProperty(OperationMapping.OUTPUT_NAMESPACE);
    QName outputElQName = new QName(outNS, respWrapper);
    String outPartName = respWrapper;
    if (OperationMapping.DOCUMENT_BARE_OPERATION_STYLE.equals(opMap.getProperty(OperationMapping.OPERATION_STYLE))) {
      if (opMap.getProperty(OperationMapping.DOCUMENT_BARE_OPERATION_RESP_PARTNAME) != null) {
        outPartName = opMap.getProperty(OperationMapping.DOCUMENT_BARE_OPERATION_RESP_PARTNAME);
      }
    } 
    Parameter outParam = intfOp.appendParameter(outPartName, Parameter.RETURN);
    outParam.appendXSDTypeRef(outputElQName, XSDRef.ELEMENT);
    //map output headers
    String outHeadersList = "";
    ParameterMapping[] outHs = opMap.getParameters(ParameterMapping.RETURN_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE);
    for (ParameterMapping p: outHs) {
      if (p.isHeader()) {
        Parameter hP = intfOp.appendParameter(p.getWSDLParameterName(), getWSDLParamType(p.getParameterType()));
        hP.appendXSDTypeRef(p.getSchemaQName(), XSDRef.ELEMENT);
        outHeadersList += " " + p.getWSDLParameterName();
      }
    }
    //process fault
    ParameterMapping[] faults = opMap.getParameters(ParameterMapping.FAULT_TYPE);
    for (int i = 0; i < faults.length; i++) {
      Parameter wsdlParam = intfOp.appendParameter(faults[i].getWSDLParameterName(), getWSDLParamType(faults[i].getParameterType()));
      wsdlParam.setProperty(WSDL11Constants.FAULT_NAME, faults[i].getWSDLParameterName());
      wsdlParam.appendXSDTypeRef(faults[i].getFaultElementQName(), XSDRef.ELEMENT);
    }
    //create binding operation
    //append operation and set soapAction and input/output binding channels
    SOAPBindingOperation soapOp = binding.appendOperation(opName);
    soapOp.setProperty(SOAPBindingOperation.SOAPACTION, opMap.getProperty(OperationMapping.SOAP_ACTION));      
    soapOp.setProperty(SOAPBindingOperation.USE, SOAPBindingOperation.USE_LITERAL); 
    //set input 'parts' values.
    soapOp.setProperty(SOAPBindingOperation.INPUT_PARTS, inPartName);
    if (inHeadersList.length() > 0) {
      soapOp.setProperty(SOAPBindingOperation.IN_HEADERS, inHeadersList);
    }
    //set output 'parts' values.
    //set it only if outPartName is there, and if there are several parts.(vladi) 
    //misho has to determine whether this is the correct way. but at least this should not hurt
    if (outPartName != null && outPartName.indexOf(' ') > 0) {
      soapOp.setProperty(SOAPBindingOperation.OUTPUT_PARTS, outPartName);
    }
    if (outHeadersList.length() > 0) {
      soapOp.setProperty(SOAPBindingOperation.OUT_HEADERS, outHeadersList);
    }
  }
  /**
   * Takes 'mapping' parameter type and returns WSDL parameter type
   */
  private int getWSDLParamType(int parameterMappingType) {
    if (parameterMappingType == ParameterMapping.IN_TYPE) {
      return Parameter.IN;
    } else if (parameterMappingType == ParameterMapping.IN_OUT_TYPE) {
      return Parameter.INOUT;
    } else if (parameterMappingType == ParameterMapping.OUT_TYPE) {
      return Parameter.OUT;
    } else if (parameterMappingType == ParameterMapping.RETURN_TYPE) {
      return Parameter.RETURN;
    } else if (parameterMappingType == ParameterMapping.FAULT_TYPE) {
      return Parameter.FAULT;
    }
    throw new IllegalArgumentException("Unknown parameter mapping: [" + parameterMappingType + "]");
  }
  /**
   * Returns Map, into which the keys are styles (String), and values are Definitions objects.
   */
  public Map processSOAPMappings(InterfaceMapping mappings, DOMSource schemas[], String[] styles)  throws WSDLException  {
    if ((! InterfaceMapping.SOAPBINDING.equals(mappings.getBindingType())) && (! InterfaceMapping.MIMEBINDING.equals(mappings.getBindingType()))) {
      throw new WSDLException("InterfaceMapping binding type '" + mappings.getBindingType() + "' is not recorgnized.");
    }
    Definitions def;
    Map defs = new HashMap();
    for (int i = 0; i < styles.length; i++) {
      //clone the schemas
      DOMSource[] newSchemas = new DOMSource[schemas.length];
      for (int s = 0; s < schemas.length; s++) {
        newSchemas[s] = new DOMSource(schemas[s].getNode().cloneNode(true));
      }
      if (SOAP_RPC_LITERAL_STYLE.equals(styles[i])) {
        def = processSoapRpcLiteralStyle(mappings, newSchemas, false);        
        defs.put(SOAP_RPC_LITERAL_STYLE, def);
      } else  if (SOAP_DOC_LITERAL_STYLE.equals(styles[i])) {
        def = processSoapDocLiteralStyle(mappings, newSchemas);
        defs.put(SOAP_DOC_LITERAL_STYLE, def);
      } else {
        throw new WSDLException(WSDLException.UNSUPPORTED_WSDL_STYLE, new Object[]{styles[i]});
      }
    }
    return defs;
  }

//================================= HTTP Binding processing ===================================
  /**
   * Returns Definitions object representing wsdl construct.
   */
  public Definitions processHTTPMappings(InterfaceMapping mappings, DOMSource schemas[]) throws WSDLException {
    if ( (! InterfaceMapping.HTTPGETBINDING.equals(mappings.getBindingType())) && (! InterfaceMapping.HTTPPOSTBINDING.equals(mappings.getBindingType()))) {
      throw new WSDLException("InterfaceMapping binding type '" + mappings.getBindingType() + "' is not recorgnized.");
    }
    Definitions def = new Definitions();
    //add Interface and SOAPBinding
    QName qname = mappings.getPortType();
    Interface intf = def.appendInterface(qname);
    //create binding
    String httpMethod;
    if (InterfaceMapping.HTTPGETBINDING.equals(mappings.getBindingType())) { //this is GET binding
      httpMethod = HTTPBinding.HTTP_GET_METHOD;
    } else { //this is POST binding
      httpMethod = HTTPBinding.HTTP_POST_METHOD;      
    }
    QName bQN = mappings.getBindingQName();
    HTTPBinding binding = new HTTPBinding(new QName(bQN.getNamespaceURI(), bQN.getLocalPart()));
    binding.setProperty(HTTPBinding.HTTP_METHOD, httpMethod);
    binding.setInterface(mappings.getPortType());
    def.appendChild(binding);
    
    //mapping the schemas by their namespaces
    Map mapSchemas = mapSchemas(schemas);
    
    //append operations to interface. 
    OperationMapping[] operations = mappings.getOperation();    
    OperationMapping curOp;
    HTTPBindingOperation bindingOp;
    Operation intfOp;
    SOAPBindingOperation soapOp;
    ParameterMapping pMaps[], curPm;
    Parameter intfP;
    String ns;
    for (int i = 0; i < operations.length; i++) {
      curOp = operations[i];
      String opName = curOp.getProperty(OperationMapping.WSDL_OPERATION_NAME);
      //create Interface operation
      intfOp = intf.appendOperation(opName);
      //set MEP property;
      appendMEPProperty(intfOp, curOp);
      pMaps = curOp.getParameter();
      for (int p = 0; p < pMaps.length; p++) {
        curPm = pMaps[p]; 
        if (curPm.getParameterType() == ParameterMapping.IN_TYPE && curPm.isExposed()) {
          intfP = intfOp.appendParameter(curPm.getWSDLParameterName(), Parameter.IN);
          intfP.appendXSDTypeRef(curPm.getSchemaQName(), XSDRef.TYPE);
        } else if (curPm.getParameterType() == ParameterMapping.RETURN_TYPE) {
          appendReturnParamToHTTPOperation(intfOp, curOp, curPm, mapSchemas);
        }
      }
      //create binding operation
      bindingOp = binding.appendOperation(opName);
      bindingOp.setProperty(HTTPBindingOperation.HTTP_METHOD, httpMethod);
      bindingOp.setProperty(HTTPBindingOperation.LOCATION, "/" + curOp.getWSDLOperationName());
      bindingOp.setProperty(HTTPBindingOperation.INPUT_SERIALIZATION, HTTPBindingOperation.URLENCODED_SERIALIZATION);
      bindingOp.setProperty(HTTPBindingOperation.OUTPUT_SERIALIZATION, HTTPBindingOperation.MIMEXML_SERIALIZATION);
    }
    //append the schemas to the definitions
    XSDTypeContainer xsdContainer = def.getXSDTypeContainer();    
    Iterator itr = mapSchemas.values().iterator();
    Element elem;
    while (itr.hasNext()) {
      elem = (Element) itr.next();
      xsdContainer.addSchema(new DOMSource(elem));
    }
    
    return def;
  }
  
  private void appendReturnParamToHTTPOperation(Operation intfOp, OperationMapping mapOp, ParameterMapping retParam, Map schemas) throws WSDLException {
    //create output schema element
    String outputNS = mapOp.getProperty(OperationMapping.OUTPUT_NAMESPACE);
    Element schema = createSchema(schemas, outputNS);
    boolean useQualifiedSchemaForParams = mapOp.useQualifiedSchemaForParams(ParameterMapping.RETURN_TYPE);
    if (useQualifiedSchemaForParams) {
      schema.setAttribute(SCHEMA_ELEMENTFORMDEFAULT_ATTR, "qualified"); //possible to override the previous value
    }
    Element el = schema.getOwnerDocument().createElementNS(NS.XS, ELEMENT);
    String outElName = getDocOperationOutName(mapOp);
    el.setAttribute(NAME_ATTR, outElName);
    schema.appendChild(el);
    Element seq = el.getOwnerDocument().createElementNS(NS.XS, SEQUENCE_ELEMENT);
    Element cT = el.getOwnerDocument().createElementNS(NS.XS, COMPLEXTYPE_ELEMENT);
    cT.appendChild(seq);
    el.appendChild(cT);
    //process parameter
    Element curEl = seq.getOwnerDocument().createElementNS(NS.XS, ELEMENT);
    curEl.setAttribute(NAME_ATTR, retParam.getWSDLParameterName());
    seq.appendChild(curEl);
    QName qname = retParam.getSchemaQName();
    String prefix = createPrefix(schema, qname.getNamespaceURI());
    curEl.setAttribute(TYPE_ATTR, prefix + ":" + qname.getLocalPart());
    //update of the Interface Operation object
    Parameter intfP = intfOp.appendParameter(DOCUMENT_OUTPUT_PART_NAME, Parameter.RETURN);
    intfP.appendXSDTypeRef(new QName(outputNS, outElName), XSDRef.ELEMENT);       
  }
  
//================================= SOAP Binding processing ===================================

  private Definitions processSoapRpcLiteralStyle(InterfaceMapping mappings, DOMSource schemas[], boolean jeeMode) throws WSDLException  {
    Definitions def = new Definitions();
    //add Interface and SOAPBinding
    QName qname = mappings.getPortType();
    QName bQName = mappings.getBindingQName();
    Interface intf = def.appendInterface(qname);
    SOAPBinding binding = new SOAPBinding(new QName(bQName.getNamespaceURI(), bQName.getLocalPart()));
    binding.setProperty(SOAPBinding.STYLE, SOAPBinding.RPC_STYLE_VALUE);
    binding.setProperty(SOAPBinding.TRANSPORT, SOAPBinding.TRANSPORT_SOAP_OVER_HTTP);
    binding.setInterface(qname);
    def.appendChild(binding);
    
    //mapping the schemas by their namespaces
    Map mapSchemas = mapSchemas(schemas);
    
    //append operations to interface and to binding. 
    OperationMapping[] operations = mappings.getOperation();
    OperationMapping curOp;
    Operation intfOp;
    SOAPBindingOperation soapOp;
    ParameterMapping pMaps[], curPm;
    Parameter intfP;
    String ns;
    for (int i = 0; i < operations.length; i++) {
      curOp = operations[i];
      String opName = curOp.getProperty(OperationMapping.WSDL_OPERATION_NAME);
      ArrayList opParamsList = new ArrayList();
      //create Interface operation
      intfOp = intf.appendOperation(opName);
      //set MEP property;
      appendMEPProperty(intfOp, curOp);
      pMaps = curOp.getParameter();
//      String inParams = "";
//      String outParams = "";
      for (int p = 0; p < pMaps.length; p++) {
        curPm = pMaps[p]; 
        intfP = null;
        if ((curPm.getParameterType() == ParameterMapping.IN_TYPE || curPm.getParameterType() == ParameterMapping.IN_OUT_TYPE)
              && curPm.isExposed() && (! curPm.isHeader())) {
          //intfP = intfOp.appendParameter(curPm.getWSDLParameterName(), getWSDLParamType(curPm.getParameterType()));
          intfP = new Parameter(curPm.getWSDLParameterName(), getWSDLParamType(curPm.getParameterType()));
          intfP.appendXSDTypeRef(curPm.getSchemaQName(), XSDRef.TYPE);
//          inParams += curPm.getWSDLParameterName() + " "; 
        } else if ((curPm.getParameterType() == ParameterMapping.RETURN_TYPE || curPm.getParameterType() == ParameterMapping.IN_OUT_TYPE || curPm.getParameterType() == ParameterMapping.OUT_TYPE)
                    && (! curPm.isHeader())) {
          //intfP = intfOp.appendParameter(curPm.getWSDLParameterName(), getWSDLParamType(curPm.getParameterType()));
          intfP = new Parameter(curPm.getWSDLParameterName(), getWSDLParamType(curPm.getParameterType()));
          intfP.appendXSDTypeRef(curPm.getSchemaQName(), XSDRef.TYPE);
//          outParams += curPm.getWSDLParameterName() + " ";          
        } 
        if (intfP != null) { //make sure that return parm will be the first in the list. This is crucial for JEE wsdls. 
          //When inout and out params are availabe the return must be the first paramin the response list
          if (intfP.getParamType() == Parameter.RETURN) {
            opParamsList.add(0, intfP);
          } else {
            opParamsList.add(intfP);
          }
        }
      }
      for (int p = 0; p < opParamsList.size(); p++) {
        intfOp.appendChild((Parameter) opParamsList.get(p));
      }
      //create Binding operation
      soapOp = binding.appendOperation(opName);
      soapOp.setProperty(SOAPBindingOperation.SOAPACTION, curOp.getProperty(OperationMapping.SOAP_ACTION));      
      soapOp.setProperty(SOAPBindingOperation.USE, SOAPBindingOperation.USE_LITERAL);
      ns = curOp.getProperty(OperationMapping.INPUT_NAMESPACE);       
      soapOp.setProperty(SOAPBindingOperation.INPUT_NAMESPACE, ns);
//      soapOp.setProperty(SOAPBindingOperation.INPUT_PARTS, inParams);
      ns = curOp.getProperty(OperationMapping.OUTPUT_NAMESPACE);
      soapOp.setProperty(SOAPBindingOperation.OUTPUT_NAMESPACE, ns);
//      soapOp.setProperty(SOAPBindingOperation.OUTPUT_PARTS, outParams);  
      //append attachments if any
      appendAttachments(curOp, soapOp);   
    }
    //append headers and faults schema elements
    createHeaderAndFaultSchemas(mappings, mapSchemas, intf, binding, jeeMode);
    //append the schemas to the definitions. In case of jeeMode, new schemas are not created by createHeaderAndFaultSchemas()
//    if (! jeeMode) {
      XSDTypeContainer xsdContainer = def.getXSDTypeContainer();    
      Iterator itr = mapSchemas.values().iterator();
      Element elem;
      while (itr.hasNext()) {
        elem = (Element) itr.next();
        xsdContainer.addSchema(new DOMSource(elem));
      }
//    }
    return def;
  }
  
  private Definitions processSoapDocLiteralStyle(InterfaceMapping mappings, DOMSource schemas[]) throws WSDLException {
    Definitions def = new Definitions();
    //add Interface and SOAPBinding
    QName qname = mappings.getPortType();
    QName bindingQN = mappings.getBindingQName();
    Interface intf = def.appendInterface(qname);
    SOAPBinding binding = new SOAPBinding(new QName(bindingQN.getNamespaceURI(), bindingQN.getLocalPart()));
    binding.setProperty(SOAPBinding.STYLE, SOAPBinding.DOC_STYLE_VALUE);
    binding.setProperty(SOAPBinding.TRANSPORT, SOAPBinding.TRANSPORT_SOAP_OVER_HTTP);    
    binding.setInterface(mappings.getPortType());
    def.appendChild(binding);
    
    //mapping the schemas by their namespaces
    Map mapSchemas = mapSchemas(schemas);
    
    //append operations to interface and to binding. 
    OperationMapping[] operations = mappings.getOperation();    
    OperationMapping curOp;
    Operation intfOp;
    SOAPBindingOperation soapOp;
    ParameterMapping pMaps[];
    for (int i = 0; i < operations.length; i++) {
      curOp = operations[i];
      String opName = curOp.getWSDLOperationName();
      intfOp = intf.appendOperation(opName);
      //set MEP property;
      appendMEPProperty(intfOp, curOp);
      //append operation and set soapAction and input/output binding channels
      soapOp = binding.appendOperation(opName);
      soapOp.setProperty(SOAPBindingOperation.SOAPACTION, curOp.getProperty(OperationMapping.SOAP_ACTION));      
      soapOp.setProperty(SOAPBindingOperation.USE, SOAPBindingOperation.USE_LITERAL); 
      //set input and output 'parts' values. The actual parts will be created in the 'createDocumentOperationSchemas' method below
      soapOp.setProperty(SOAPBindingOperation.INPUT_PARTS, DOCUMENT_INPUT_PART_NAME);
      soapOp.setProperty(SOAPBindingOperation.OUTPUT_PARTS, DOCUMENT_OUTPUT_PART_NAME);
      //append attachments
      appendAttachments(curOp, soapOp);      
    }
    //append input/output schema elements   
    createDocumentOperationSchemas(mappings, mapSchemas, intf);
    //append headers and faults schema elements
    createHeaderAndFaultSchemas(mappings, mapSchemas, intf, binding, false);
    //append the schemas to the definitions
    XSDTypeContainer xsdContainer = def.getXSDTypeContainer();    
    Iterator itr = mapSchemas.values().iterator();
    Element elem;
    while (itr.hasNext()) {
      elem = (Element) itr.next();
      xsdContainer.addSchema(new DOMSource(elem));
    }
    return def;
  }
  
  private Map createDocumentOperationSchemas(InterfaceMapping mappings, Map schemas, Interface intf) throws WSDLException {
    OperationMapping[] operations = mappings.getOperation();    
    String inputNS, outputNS, inElName, outElName; 
    QName qname;
    OperationMapping curOp;
    Element schema;
    Element el, seq, curEl;
    ParameterMapping curP, params[];
    for (int i = 0; i < operations.length; i++) {
      curOp = operations[i];
      Operation intfOp = intf.getOperation(curOp.getWSDLOperationName());

      //create input schema element
      inputNS = curOp.getProperty(OperationMapping.INPUT_NAMESPACE);
      schema = createSchema(schemas, inputNS);
      boolean useQualifiedSchemaForInParams = curOp.useQualifiedSchemaForParams(ParameterMapping.IN_TYPE);
      if (useQualifiedSchemaForInParams) {
        schema.setAttribute(SCHEMA_ELEMENTFORMDEFAULT_ATTR, "qualified"); //possible to override the previous value
      }
      el = schema.getOwnerDocument().createElementNS(NS.XS, ELEMENT);
      inElName = getDocOperationInName(curOp);
      el.setAttribute(NAME_ATTR, inElName);
      schema.appendChild(el);
      Element cT = el.getOwnerDocument().createElementNS(NS.XS, COMPLEXTYPE_ELEMENT);
      seq = el.getOwnerDocument().createElementNS(NS.XS, SEQUENCE_ELEMENT);
      cT.appendChild(seq);
      el.appendChild(cT);
      params = curOp.getParameter();
      for (int p = 0; p < params.length; p++) {
        curP = params[p];
        if (curP.getParameterType() == ParameterMapping.IN_TYPE && curP.isExposed() && (! curP.isHeader()) && (! curP.isAttachment())) {
          curEl = seq.getOwnerDocument().createElementNS(NS.XS, ELEMENT);
          curEl.setAttribute(NAME_ATTR, curP.getWSDLParameterName());
          seq.appendChild(curEl);
          qname = curP.getSchemaQName();
          String prefix = createPrefix(schema, qname.getNamespaceURI());
          curEl.setAttribute(TYPE_ATTR, prefix + ":" + qname.getLocalPart());
          if (curP.isOptional()) {
            curEl.setAttribute(MINOCCURS_ATTR, "0");
          }
          if (! notNillableJavaType(curP.getJavaType())) {
            curEl.setAttribute(NILLABLE_ATTR, "true");
          }
        } else if (curP.isAttachment()) {//set IN attachments parameters in interface/operation entity
          Parameter intfP = intfOp.appendParameter(curP.getWSDLParameterName(), Parameter.IN);
          intfP.appendXSDTypeRef(curP.getSchemaQName(), XSDRef.TYPE);
        }
      }
      //update of the Interface Operation object
      Parameter intfP = intfOp.appendParameter(DOCUMENT_INPUT_PART_NAME, Parameter.IN);
      intfP.appendXSDTypeRef(new QName(inputNS, inElName), XSDRef.ELEMENT);
      
      //create output schema element
      if (OperationMapping.MEP_REQ_RESP.equals(curOp.getProperty(OperationMapping.OPERATION_MEP))) {
        outputNS = curOp.getProperty(OperationMapping.OUTPUT_NAMESPACE);
        schema = createSchema(schemas, outputNS);
        boolean useQualifiedSchemaForOutParams = curOp.useQualifiedSchemaForParams(ParameterMapping.RETURN_TYPE);
        if (useQualifiedSchemaForOutParams) {
          schema.setAttribute(SCHEMA_ELEMENTFORMDEFAULT_ATTR, "qualified"); //possible to override the previous value
        }
        el = schema.getOwnerDocument().createElementNS(NS.XS, ELEMENT);
        outElName = getDocOperationOutName(curOp);
        el.setAttribute(NAME_ATTR, outElName);
        schema.appendChild(el);
        seq = el.getOwnerDocument().createElementNS(NS.XS, SEQUENCE_ELEMENT);
        cT = el.getOwnerDocument().createElementNS(NS.XS, COMPLEXTYPE_ELEMENT);
        cT.appendChild(seq);
        el.appendChild(cT);
        params = curOp.getParameter();
        for (int p = 0; p < params.length; p++) {
          curP = params[p];
          if (curP.getParameterType() == ParameterMapping.RETURN_TYPE && curP.isExposed() && (! curP.isHeader()) && (! curP.isAttachment())) {
            curEl = seq.getOwnerDocument().createElementNS(NS.XS, ELEMENT);
            curEl.setAttribute(NAME_ATTR, curP.getWSDLParameterName());
            seq.appendChild(curEl);
            qname = curP.getSchemaQName();
            String prefix = createPrefix(schema, qname.getNamespaceURI());
            curEl.setAttribute(TYPE_ATTR, prefix + ":" + qname.getLocalPart());
            if (curP.isOptional()) {
              curEl.setAttribute(MINOCCURS_ATTR, "0");
            }
            if (! notNillableJavaType(curP.getJavaType())) {
              curEl.setAttribute(NILLABLE_ATTR, "true");
            }
          } else if (curP.isAttachment()) {//set IN attachments parameters in interface/operation entity
            intfP = intfOp.appendParameter(curP.getWSDLParameterName(), Parameter.RETURN);
            intfP.appendXSDTypeRef(curP.getSchemaQName(), XSDRef.TYPE);
          }
        }
        //update of the Interface Operation object
        intfP = intfOp.appendParameter(DOCUMENT_OUTPUT_PART_NAME, Parameter.RETURN);
        intfP.appendXSDTypeRef(new QName(outputNS, outElName), XSDRef.ELEMENT);
      }
    }
    return schemas;
  }
  
  private Map createHeaderAndFaultSchemas(InterfaceMapping mappings, Map schemas, Interface intf, SOAPBinding binding, boolean jeeMode) throws WSDLException {
    OperationMapping[] operations = mappings.getOperation();    
    String ns, localName; 
    QName qname;
    OperationMapping curOp;
    Operation op;
    Element schema;
    Element el;
    ParameterMapping curP, params[];
    ArrayList in_headers = new ArrayList();
    ArrayList out_headers = new ArrayList();
    for (int i = 0; i < operations.length; i++) {
      curOp = operations[i];
      in_headers.clear();
      out_headers.clear();      
      //create input header schema elements
      params = curOp.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE);
      for (int p = 0; p < params.length; p++) {
        curP = params[p];
        if (curP.isExposed() && curP.isHeader()) {
          QName hEL_Qname = null;
          if (jeeMode) { //for jee mode, the header parameter is described by using schema 'element' qname
            hEL_Qname = curP.getSchemaQName();
          } else {
            ns = curP.getProperty(ParameterMapping.NAMESPACE);
            schema = createSchema(schemas, ns);
            el = schema.getOwnerDocument().createElementNS(NS.XS, ELEMENT);
            localName = curP.getWSDLParameterName();
            el.setAttribute(NAME_ATTR, localName);
            schema.appendChild(el);
            qname = curP.getSchemaQName();
            String prefix = createPrefix(schema, qname.getNamespaceURI());
            el.setAttribute(TYPE_ATTR, prefix + ":" + qname.getLocalPart());
            hEL_Qname = new QName(ns, localName);
          }
          //update the Interface object
          op = intf.getOperation(curOp.getWSDLOperationName());
          Parameter param = op.appendParameter(curP.getWSDLParameterName(), getWSDLParamType(curP.getParameterType()));
          param.appendXSDTypeRef(hEL_Qname, XSDRef.ELEMENT);
          in_headers.add(param.getName());
        }
      }
      //create output headers - only relevant for jee case, since in SAP inside-out scenario there are no output headers
      if (jeeMode) {
        params = curOp.getParameters(ParameterMapping.RETURN_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE);
        for (int p = 0; p < params.length; p++) {
          curP = params[p];
          if (curP.isExposed() && curP.isHeader()) {
            QName hEL_Qname = curP.getSchemaQName();
            //update the Interface object
            op = intf.getOperation(curOp.getWSDLOperationName());
            Parameter param = op.appendParameter(curP.getWSDLParameterName(), getWSDLParamType(curP.getParameterType()));
            param.appendXSDTypeRef(hEL_Qname, XSDRef.ELEMENT);
            out_headers.add(param.getName());
          }
        }
      }
      //update the soap binding operation
      SOAPBindingOperation soapBOperation = binding.getOperation(curOp.getWSDLOperationName());
      String in_headersStr = "";
      for (int h = 0; h < in_headers.size(); h++) {
        in_headersStr += in_headers.get(h) + " ";
      }
      String out_headersStr = "";
      for (int h = 0; h < out_headers.size(); h++) {
        out_headersStr += out_headers.get(h) + " ";
      }
      if (in_headersStr.length() > 0) {
        soapBOperation.setProperty(SOAPBindingOperation.IN_HEADERS, in_headersStr);
      }
      if (out_headersStr.length() > 0) {
        soapBOperation.setProperty(SOAPBindingOperation.OUT_HEADERS, out_headersStr);
      }
      op = intf.getOperation(curOp.getWSDLOperationName());
      //build input soap:body 'parts' value 
      ObjectList paramList = op.getParameters(Parameter.IN | Parameter.INOUT);      
      String parts = "";
      for (int h = 0; h < paramList.getLength(); h++) {
        //check whether this parameter is exposed as header. If not attach it to the parts list.
        if (! in_headers.contains(((Parameter) paramList.item(h)).getName())) {
          parts += ((Parameter) paramList.item(h)).getName() + " ";
        }
      }
      soapBOperation.setProperty(SOAPBindingOperation.INPUT_PARTS, parts.trim()); //.trim() removes trailing ' '
      //build output soap:body 'parts' value 
      paramList = op.getParameters(Parameter.RETURN | Parameter.INOUT | Parameter.OUT);      
      parts = "";
      for (int h = 0; h < paramList.getLength(); h++) {
        //check whether this parameter is exposed as header. If not attach it to the parts list.
        if (! out_headers.contains(((Parameter) paramList.item(h)).getName())) {
          parts += ((Parameter) paramList.item(h)).getName() + " ";
        }
      }
      soapBOperation.setProperty(SOAPBindingOperation.OUTPUT_PARTS, parts.trim()); //.trim() removes trailing ' '
       
      //create fault schema elements
      params = curOp.getParameters(ParameterMapping.FAULT_TYPE);
      for (int p = 0; p < params.length; p++) {
        curP = params[p];
        QName fEL_QName = null;
        if (jeeMode) {
          fEL_QName = curP.getFaultElementQName();
        } else {
          ns  = curP.getFaultElementQName().getNamespaceURI();
          schema = createSchema(schemas, ns);
          el = schema.getOwnerDocument().createElementNS(NS.XS, ELEMENT);
          localName = curP.getFaultElementQName().getLocalPart(); 
          el.setAttribute(NAME_ATTR, localName);
          schema.appendChild(el);
          qname = curP.getSchemaQName();
          String prefix = createPrefix(schema, qname.getNamespaceURI());
          el.setAttribute(TYPE_ATTR, prefix + ":" + qname.getLocalPart());
          fEL_QName = new QName(ns, localName);
        }
        //update the Interface object
        op = intf.getOperation(curOp.getWSDLOperationName());
        Parameter param = op.appendParameter(curP.getWSDLParameterName(), Parameter.FAULT);
        param.setProperty(WSDL11Constants.FAULT_NAME, param.getName());
        param.appendXSDTypeRef(fEL_QName, XSDRef.ELEMENT);
      }   
    }
    
    return schemas;
  }
  
  private HashMap mapSchemas(DOMSource[] schemas) throws WSDLMarshalException {
    HashMap map = new HashMap();
    Element curSchema;
    String tnsValue;
    for (int i = 0; i < schemas.length; i++) {
      curSchema = (Element) schemas[i].getNode();
      tnsValue = curSchema.getAttribute(SCHEMA_TARGETNS_ATTR);
      if (map.put(tnsValue, curSchema) != null) {
        throw new WSDLMarshalException("Duplicate schema with tns " + tnsValue + " found.");
      }
    }
    
    return map;
  }
  
  private Element createSchema(Map schemas, String tns) {
    Element el = (Element) schemas.get(tns);
    if (el != null) {
      return el;
    }
    Element newSchema = SharedDocumentBuilders.newDocument().createElementNS(NS.XS, SCHEMA_ELEMENT);
    newSchema.setAttribute(SCHEMA_TARGETNS_ATTR, tns);
    schemas.put(tns, newSchema);
    return newSchema;
  }
  
  private String getDocOperationInName(OperationMapping operation) {
    String wrapper = operation.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER);
    if (wrapper == null) {
      wrapper = operation.getWSDLOperationName();
    }  
    return wrapper;
  }

  public String getDocOperationOutName(OperationMapping operation) {
    String wrapper = operation.getProperty(OperationMapping.SOAP_RESPONSE_WRAPPER);
    if (wrapper == null) {
      wrapper = operation.getWSDLOperationName() + OperationMapping.OPERATION_RESPONSE_SUFFIX;
    }  
    return wrapper;
  }
  
  private String createPrefix(Element schema, String ns) {
    String pref = DOM.getPrefixForNS(schema, ns);
    if (pref != null) {
      return pref;
    }
    Element importEl = schema.getOwnerDocument().createElementNS(NS.XS, IMPORT_ELEMENT);
    importEl.setAttribute(NAMESPACE_ATTR, ns);
    schema.insertBefore(importEl, schema.getFirstChild());
    
    int n = 0;
    boolean end = false;
    while (! end) {
      pref = BASE_PREFIX + Integer.toString(n);
      if (schema.getAttributeNodeNS(NS.XMLNS, pref) == null) {
        end = true;
        schema.setAttributeNS(NS.XMLNS, "xmlns:" + pref, ns);  
      }
      n++;
    }
    return pref;        
  }
  
  private void appendAttachments(OperationMapping opMapping, SOAPBindingOperation soapOp) throws WSDLException {
    AttachmentsContainer attContainer;
    ArrayList types;
    ParameterMapping[] params = opMapping.getParameters(ParameterMapping.IN_TYPE);
    for (int i = 0; i < params.length; i++) {
      if (params[i].isAttachment()) {
        attContainer = soapOp.appendInputAttachmentsContainer();
        types = new ArrayList();
        types.add(params[i].getAttachmentContentType());
        attContainer.appendMIMEPart(params[i].getWSDLParameterName(), types);     
      }
    }
    params = opMapping.getParameters(ParameterMapping.RETURN_TYPE);
    for (int i = 0; i < params.length; i++) { //it should be one or zero return parameters
      if (params[i].isAttachment()) {
        attContainer = soapOp.appendOutputAttachmentsContainer();
        types = new ArrayList();
        types.add(params[i].getAttachmentContentType());
        attContainer.appendMIMEPart(params[i].getWSDLParameterName(), types);     
      }
    }
  }
  /**
   * Appends MEP property to <code>wOp</code>, based on the data extracted from <code>mOp</code>.
   */
  private void appendMEPProperty(Operation wOp, OperationMapping mOp) throws WSDLException {
    String wsdlMep = null;
    String mMep = mOp.getProperty(OperationMapping.OPERATION_MEP);
    if (OperationMapping.MEP_ONE_WAY.equals(mMep)) {
      wsdlMep = Operation.IN_MEP;
    } else if (OperationMapping.MEP_REQ_RESP.equals(mMep)) {
      wsdlMep = Operation.INOUT_MEP;
    } else {
      throw new WSDLException("OperationMapping operation, with java name '" + mOp.getJavaMethodName() + "', misses property '" + OperationMapping.OPERATION_MEP + "'");
    }
    wOp.setProperty(Operation.MEP, wsdlMep);
  }
  /**
   * Returns true if <code>javaType</code> represents a type to which 'null' value cannot be assigned (like primitive types for example).
   * @param javaType
   * @return
   */
  private boolean notNillableJavaType(String javaType) {
    //all primitive types are not nillable
    if (Boolean.TYPE.getName().equals(javaType)) {
      return true;
    } if (Character.TYPE.getName().equals(javaType)) {
      return true;
    } if (Float.TYPE.getName().equals(javaType)) {
      return true;
    } if (Integer.TYPE.getName().equals(javaType)) {
      return true;
    } if (Short.TYPE.getName().equals(javaType)) {
      return true;
    } if (Double.TYPE.getName().equals(javaType)) {
      return true;
    } if (Long.TYPE.getName().equals(javaType)) {
      return true;
    } if (Byte.TYPE.getName().equals(javaType)) {
      return true;
    }
    
    return false;
  }
//  public static void main(String[] arg) throws Exception {
////    DocumentBuilderFactoryImpl f = new DocumentBuilderFactoryImpl();
////    f.setNamespaceAware(true);
////    Element schema = f.newDocumentBuilder().parse("d:/box/Pacho/schemas.xml").getDocumentElement();
////    DOMSource schemaSource = new DOMSource(schema);
//    InterfaceMapping intMapping = MappingFactory.load("d:/temp/mappings.xml").getInterface()[0];
//    InterfaceMappingProcessor p = new InterfaceMappingProcessor();
//    Map m = p.processSOAPMappings(intMapping, new DOMSource[]{}, new String[]{SOAP_DOC_LITERAL_STYLE, SOAP_RPC_LITERAL_STYLE});
//    WSDLSerializer serializer = new WSDLSerializer();
//    Iterator wsdls = m.values().iterator();
//    Object o;
//    while (wsdls.hasNext()) {
//      o = wsdls.next();
//      System.out.println(o);
//      List definitions = serializer.serialize((Definitions) o);
//      for (int i = 0; i < definitions.size(); i++) {
//        System.out.println(definitions.get(i));
//      }
//    }
//  }
}
