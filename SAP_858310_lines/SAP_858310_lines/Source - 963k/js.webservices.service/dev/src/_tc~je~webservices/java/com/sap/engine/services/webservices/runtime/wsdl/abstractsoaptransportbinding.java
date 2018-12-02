package com.sap.engine.services.webservices.runtime.wsdl;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.runtime.Config;
import com.sap.engine.interfaces.webservices.runtime.Fault;
import com.sap.engine.interfaces.webservices.runtime.Feature;
import com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappingRegistry;
import com.sap.engine.interfaces.webservices.runtime.Key;
import com.sap.engine.interfaces.webservices.runtime.OperationDefinition;
import com.sap.engine.interfaces.webservices.runtime.ParameterNode;
import com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor;
import com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor;
import com.sap.engine.interfaces.webservices.runtime.RawMessage;
import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.interfaces.webservices.runtime.TransportBinding;
import com.sap.engine.interfaces.webservices.runtime.TransportBindingException;
import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.parser.helpers.CharArray;
import com.sap.engine.lib.xml.util.QName;
import com.sap.engine.services.webservices.additions.soaphttp.SOAPHTTPConfigProperties;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.ConfigurationException;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.ProcessException;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.TBindingResourceAccessor;
import com.sap.engine.services.webservices.runtime.interfaces.RuntimeTransportBinding;
import com.sap.engine.services.webservices.wsdl.WSDLBinding;
import com.sap.engine.services.webservices.wsdl.WSDLBindingChannel;
import com.sap.engine.services.webservices.wsdl.WSDLBindingFault;
import com.sap.engine.services.webservices.wsdl.WSDLBindingOperation;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.wsdl.WSDLDocumentation;
import com.sap.engine.services.webservices.wsdl.WSDLException;
import com.sap.engine.services.webservices.wsdl.WSDLExtension;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class AbstractSOAPTransportBinding implements RuntimeTransportBinding, SOAPHTTPConfigProperties, TransportBinding {

  protected static final String HTTPTRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";
  protected static final String SOAP_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
  protected static final String TRANSPORT = "transport";
  protected static final String BINDING = "binding";
  protected static final String OUT_HEADERSMESSAGE_SUFFIX = "OutHeaders";
  protected static final String IN_HEADERSMESSAGE_SUFFIX = "InHeaders";
  protected static final String HEADER_SCHEMAURI_PREFIX = "hs";
  protected static final String DOCUMENT_WSDL_STYLE  =  "document";
  protected static final String RPC_WSDL_STYLE  =  "rpc";
  protected static final String RPC_ENCODED_WSDL_STYLE  =  "rpc_enc";
  protected static final String SOAP_REQUEST_WRAPPER  =  "SoapRequestWrapper";
  protected static final String SOAP_RESPONSE_WRAPPER  =  "SoapResponseWrapper";
  
  //property in the input operation configuration denoting presense of headers
  static final String SOAPHEADERS_PROP  =  "soap-headers";
  //WS-I conformance documenation
  static final String WSI_CONFORMANCE_CLAIM_DOCUMENTATION  =  "<wsi:Claim conformsTo=\"http://ws-i.org/profiles/basic/1.0\" xmlns:wsi=\"http://ws-i.org/schemas/conformanceClaim/\"/>";
  //feature which presentce denotes one-way operation
  public static final String ONE_WAY_OPERATION_FEATURE  =  "http://www.sap.com/webas/630/soap/features/mep/one-way";

//==================Public methods

  public String[] getSupportedSyles() {
    return new String[]{RPC_WSDL_STYLE, DOCUMENT_WSDL_STYLE, RPC_ENCODED_WSDL_STYLE};
  }

  public int[] getNecessaryPortTypes() {
    return new int[]{PortTypeDescriptor.RPC_LITERAL_PORTTYPE, PortTypeDescriptor.LITERAL_PORTTYPE, PortTypeDescriptor.ENCODED_PORTTYPE};
  }

  public String[] getDefaultStyles() {
//    return new String[]{RPC_WSDL_STYLE, DOCUMENT_WSDL_STYLE};
    return new String[]{DOCUMENT_WSDL_STYLE};
  }

  private String getPortTypeNSPrefix(WSDLDefinitions def, String ns) {
    Hashtable table = def.getAdditionalAttributes();
    Enumeration keys = table.keys();
    String temp;
    String value;

    while (keys.hasMoreElements()) {
      temp = (String) keys.nextElement();
      if (temp.startsWith("xmlns:")) {
        value = (String) table.get(temp);
        if (value.equals(ns)) {
          return temp.substring("xmlns:".length());
        }
      }
    }

    def.addAdditionalAttribute("xmlns:ptns", ns);
    return "ptns";
  }

  public void generateBinding(String style, javax.xml.namespace.QName bindingQName, Config globalConfig, OperationDefinition[] operations, PortTypeDescriptor[] descriptors, WSDLDefinitions definitions) throws TransportBindingException {
    String bindingName = bindingQName.getLocalPart();

    if (style == null) {
      throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.UNRECORGNIZED_BINIDING_STYLE, new Object[]{style});
    }

    WSDLBinding binding = new WSDLBinding();
    binding.setName(bindingName);
    WSDLExtension bindingExtension = new WSDLExtension();
    bindingExtension.setLocalName("binding");
    bindingExtension.setURI(SOAP_URI);
    bindingExtension.setAttribute(new SimpleAttr(TRANSPORT, HTTPTRANSPORT_URI, ""));

    binding.addExtension(bindingExtension);
    
    try { 
      for (int i = 0; i < descriptors.length; i++) {
        String portTypePrefix = getPortTypeNSPrefix(definitions, descriptors[i].getQName().getNamespaceURI());
        if (style.equals(DOCUMENT_WSDL_STYLE) && descriptors[i].getType() == PortTypeDescriptor.LITERAL_PORTTYPE) {
          binding.setType(new QName(portTypePrefix, descriptors[i].getQName().getLocalPart(), descriptors[i].getQName().getNamespaceURI()));
  //        binding.setDocumentation(getWSIDocumentation());
          bindingExtension.setAttribute(new SimpleAttr(STYLE, "document", ""));
          generateBindingOperations(definitions, binding, operations, DOCUMENT_WSDL_STYLE, portTypePrefix);
          return;
          //Rpc-encoded style generation
        } else if (style.equals(RPC_ENCODED_WSDL_STYLE) && descriptors[i].getType() == PortTypeDescriptor.ENCODED_PORTTYPE) {
          binding.setType(new QName(portTypePrefix, descriptors[i].getQName().getLocalPart(), descriptors[i].getQName().getNamespaceURI()));
          bindingExtension.setAttribute(new SimpleAttr(STYLE, "rpc", ""));
          generateBindingOperations(definitions, binding, operations, RPC_ENCODED_WSDL_STYLE, portTypePrefix);
          return;
          //Rpc-literal style generation
        } else if (style.equals(RPC_WSDL_STYLE) && descriptors[i].getType() == PortTypeDescriptor.RPC_LITERAL_PORTTYPE) {
          binding.setType(new QName(portTypePrefix, descriptors[i].getQName().getLocalPart(), descriptors[i].getQName().getNamespaceURI()));
  //        binding.setDocumentation(getWSIDocumentation());
          bindingExtension.setAttribute(new SimpleAttr(STYLE, "rpc", ""));
          generateBindingOperations(definitions, binding, operations, RPC_WSDL_STYLE, portTypePrefix);
          return;
        }
      }
    } catch (ProcessException pEx) {
      throw new TransportBindingException(pEx);
    }
  }

  private void generateBindingOperations(WSDLDefinitions definitions, WSDLBinding binding, OperationDefinition operations[], String style, String portTypePrefix) throws ConfigurationException, ProcessException {
    WSDLBindingOperation tempBindingOperation;

    try {

      if (style.equals(DOCUMENT_WSDL_STYLE) || style.equals(RPC_WSDL_STYLE)) {
        for (int i = 0; i < operations.length; i++) {
//          generateOperationHeaderMessages(operations[i], mappedHsPrefix, definitions);

          tempBindingOperation = new WSDLBindingOperation();
          binding.addOperation(tempBindingOperation);
          tempBindingOperation.setName(operations[i].getOperationName());

          if (operations[i].getDescription() != null) {
            WSDLDocumentation documentation = new WSDLDocumentation();
            documentation.setContent(new CharArray(operations[i].getDescription()));
            tempBindingOperation.setDocumentation(documentation);
          }

          WSDLExtension extension = new WSDLExtension();
          extension.setLocalName("operation");
          extension.setURI(SOAP_URI);

          String soapActionDescr;
          try {
            soapActionDescr = operations[i].getInputConfiguration().getProperty(SOAPACTION).getValue();
          } catch (NullPointerException nE) { //in case no property is found
            soapActionDescr = "";
          }
          extension.setAttribute(new SimpleAttr(SOAPACTION, soapActionDescr, ""));

          tempBindingOperation.addExtension(extension);

          //in channel
          WSDLBindingChannel channel;
          if (style.equals(DOCUMENT_WSDL_STYLE)) {
            channel = BaseWSDLComponentGenerator.generateSOAPLiteralChannel("parameters");
          } else {
            try {
              String namespace = operations[i].getInputConfiguration().getProperty(NAMESPACE).getValue();
              if (namespace == null) {
                throw new NullPointerException();
              }
              channel = BaseWSDLComponentGenerator.generateSOAPLiteralChannel(namespace, operations[i].getInputParameters());
            } catch (NullPointerException e) {
              throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{operations[i].getOperationName(), NAMESPACE, "input"});
            }
          }
          channel = addHeadersInChannel(channel, portTypePrefix + ":" + getInputMessage(operations[i].getOperationName(), (style.equals(DOCUMENT_WSDL_STYLE)) ? true:false), operations[i]);
          tempBindingOperation.setInput(channel);

          //in case it is one-way operation miss output and fault generation
          if (isOneWay(operations[i])) {
            continue;
          }

          //out channel
          if (style.equals(DOCUMENT_WSDL_STYLE)) {
            channel = BaseWSDLComponentGenerator.generateSOAPLiteralChannel(null);
          } else {
            try {
              String namespace = operations[i].getOutputConfiguration().getProperty(NAMESPACE).getValue();
              if (namespace == null) {
                throw new NullPointerException();
              }
              channel = BaseWSDLComponentGenerator.generateSOAPLiteralChannel(namespace, null);
            } catch (NullPointerException e) {
              throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{operations[i].getOperationName(), "output", NAMESPACE});
            }
          }
          tempBindingOperation.setOutput(channel);
          //fault channels
          Fault[] faults = operations[i].getFaults();
          for (int k = 0; k < faults.length; k++) {
            tempBindingOperation.addFault(BaseWSDLComponentGenerator.generateBindingFault(faults[k].getFaultName(), "literal", null));
          }
        }

        definitions.addBinding(binding);
      } else if (style.equals(RPC_ENCODED_WSDL_STYLE)) {
        for (int i = 0; i < operations.length; i++) {
          //in case the operation has headers miss it. It has been missed in the portType also.
          if (isOperationWithHeaders(operations[i])) {
            continue;
          }

          tempBindingOperation = new WSDLBindingOperation();
          binding.addOperation(tempBindingOperation);
          tempBindingOperation.setName(operations[i].getOperationName());

          if (operations[i].getDescription() != null) {
            WSDLDocumentation documentation = new WSDLDocumentation();
            documentation.setContent(new CharArray(operations[i].getDescription()));
            tempBindingOperation.setDocumentation(documentation);
          }

          WSDLExtension extension = new WSDLExtension();
          extension.setLocalName("operation");
          extension.setURI(SOAP_URI);

          String soapActionProp;
          try {
            soapActionProp = operations[i].getInputConfiguration().getProperty(SOAPACTION).getValue();
          } catch (NullPointerException nE) { // in case no soapAction property is found
            soapActionProp = "";
          }

          extension.setAttribute(new SimpleAttr(SOAPACTION, soapActionProp, ""));

          tempBindingOperation.addExtension(extension);

          String namespace = null;
          String encodingStyle = null;

          try {
            namespace = operations[i].getInputConfiguration().getProperty(NAMESPACE).getValue();
            if (namespace == null) {
              throw new NullPointerException();
            }
          } catch (NullPointerException nE) {
            throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{operations[i].getOperationName(), "input", NAMESPACE});
          }

          try {
            encodingStyle = operations[i].getInputConfiguration().getProperty(ENCODINGSTYLE).getValue();
            if (encodingStyle == null) {
              throw new NullPointerException();
            }
          } catch (NullPointerException nE) {
            throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{operations[i].getOperationName(), "input", ENCODINGSTYLE});
          }

          WSDLBindingChannel channel = BaseWSDLComponentGenerator.generateSOAPEncodedChannel(namespace, encodingStyle);
          //in channel
          tempBindingOperation.setInput(channel);

          try {
            namespace = operations[i].getInputConfiguration().getProperty(NAMESPACE).getValue();
            if (namespace == null) {
              throw new NullPointerException();
            }
          } catch (NullPointerException nE) {
            throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{operations[i].getOperationName(), "output", NAMESPACE});
          }

          try {
            encodingStyle = operations[i].getInputConfiguration().getProperty(ENCODINGSTYLE).getValue();
            if (encodingStyle == null) {
              throw new NullPointerException();
            }
          } catch (NullPointerException nE) {
            throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{operations[i].getOperationName(), "output", ENCODINGSTYLE});
          }

          //in case it is one-way operation miss output and fault generation
          if (isOneWay(operations[i])) {
            continue;
          }

          channel = BaseWSDLComponentGenerator.generateSOAPEncodedChannel(namespace, encodingStyle);
          //out channel
          tempBindingOperation.setOutput(channel);
          //fault channels
          Fault[] faults = operations[i].getFaults();
          for (int k = 0; k < faults.length; k++) {
            try {
              encodingStyle = faults[k].getFaultConfiguration().getProperty(ENCODINGSTYLE).getValue();
            } catch (NullPointerException nE) {
              throw new ProcessException(ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{faults[k].getFaultName() + "of " + operations[i].getOperationName(), ENCODINGSTYLE, "fault"});
            }
            try {
              namespace = faults[k].getFaultConfiguration().getProperty(NAMESPACE).getValue();
            } catch (NullPointerException nE) {
              throw new ProcessException(ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{faults[k].getFaultName() + "of " + operations[i].getOperationName(), NAMESPACE, "fault"});
            }
            WSDLBindingFault wsdlBFault = BaseWSDLComponentGenerator.generateBindingFault(faults[k].getFaultName(), "encoded", encodingStyle);
            ((WSDLExtension) wsdlBFault.getExtensions().get(0)).setAttribute("namespace", namespace, "");
            tempBindingOperation.addFault(wsdlBFault);

          }

        }

        definitions.addBinding(binding);
      } else { //this never happens
        throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.UNRECORGNIZED_BINIDING_STYLE, new Object[]{style});
      }

    } catch (WSDLException wsdlE) {
      throw new ProcessException(ExceptionConstants.WSDLEXCEPTION_IN_BINDING_CREATION, new Object[]{binding.getName(), wsdlE.getLocalizedMessage()}, wsdlE);
    }
  }


  private WSDLBindingChannel addHeadersInChannel(WSDLBindingChannel channel, String messageQName, OperationDefinition operation) {

    ParameterNode[] inputParams = operation.getInputParameters();
    for (int i = 0; i < inputParams.length; i++) {
      if (inputParams[i].isExposed() && inputParams[i].isHeader()) {
        channel.addExtension(BaseWSDLComponentGenerator.generateSOAPHeaderLiteralBindingExtension(messageQName, inputParams[i].getParameterName() + "_" + i));
      }
    }

    return channel;
  }

//  private String[] getOperationParamNames(OperationDefinition operationDefinition) {
//    PropertyDescriptor headerProp = operationDefinition.getInputConfiguration().getProperty(SOAPHEADERS_PROP);
//    if (headerProp != null) {
//      java.util.ArrayList result = new java.util.ArrayList();
//      ParameterNode[] params = operationDefinition.getInputParameters();
//
//      for (int i = 0; i < params.length; i++) {
//        result.add(params[i].getParameterName());
//      }
//
//      java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(headerProp.getValue());
//      String nextToken;
//      while (tokenizer.hasMoreTokens()) {
//        nextToken = tokenizer.nextToken();
//        result.remove(nextToken); //remove the param because it is a header param
//      }
//
//      return (String[]) result.toArray(new String[result.size()]);
//    }
//
//    return null;
//  }

  private String getInputMessage(String operationName, boolean  isDocument) {
    String res = operationName + "In"; //because In this way it is generated from the runtime.
    if (isDocument) {
      res += "_doc";
    }

    return res;
  }

  private WSDLDocumentation getWSIDocumentation() {
    WSDLDocumentation doc = new WSDLDocumentation();
    doc.setContent(new CharArray(WSI_CONFORMANCE_CLAIM_DOCUMENTATION));
    return doc;
  }

  //searches for one-way feature
  static boolean isOneWay(OperationDefinition opD) {
    Feature fs[] = opD.getFeatures();
    if (fs != null) {
      for (int i = 0; i < fs.length; i++) {
        if (fs[i].getFeatureName().equals(ONE_WAY_OPERATION_FEATURE)) {
          return true;
        }
      }
    }
    return false;
  }

  boolean isOperationWithHeaders(OperationDefinition opD) {
    ParameterNode[] params = opD.getInputParameters();
    for (int i = 0; i < params.length; i++) {
      if (params[i].isHeader() && params[i].isExposed()) { //in case of showed header only
        return true;
      }
    }

    return false;
  }

  public Key[] getOperationKeys(OperationDefinition virtualOperation) {
    Config cfg = virtualOperation.getInputConfiguration();
    PropertyDescriptor soapActionDescr = cfg.getProperty(SOAPACTION_KEY);
    String firstElName = virtualOperation.getOperationName(); //can't be null cause schema does not allow this
    PropertyDescriptor namespaceDescr = cfg.getProperty(NAMESPACE);

    if ((namespaceDescr == null) || namespaceDescr.getValue() == null) {
//      throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{firstElName, NAMESPACE, "input"});
    }
    String namespace = namespaceDescr.getValue();

    Key[] keys = new Key[2];

    keys[0] = new Key(FIRST_ELEMENT_NAME, firstElName);
    keys[1] = new Key(FIRST_ELEMENT_NS, namespace);

//    if (soapActionDescr != null && soapActionDescr.getValue() != null) {
//      Key[] newKeys = new Key[3];
//      newKeys[0] = keys[0];
//      newKeys[1] = keys[1];
//      newKeys[2] = new Key(SOAPACTION_KEY, soapActionDescr.getValue());
//
//      keys = newKeys;
//    }

    //if available in the VI SOAPRequest/Response property set it as a key which will be used in document style processing
    cfg = virtualOperation.getGeneralConfiguration();
    String soapRequest = null;
    if (cfg != null) {
      soapRequest = (cfg.getProperty(SOAP_REQUEST_WRAPPER) != null) ? cfg.getProperty(SOAP_REQUEST_WRAPPER).getValue():null;
    }

    if (soapRequest == null) {
      soapRequest = firstElName;
    }

    Key[] newKeys = new Key[keys.length + 1];
    System.arraycopy(keys, 0, newKeys, 0, keys.length);
    newKeys[keys.length] = new Key(SOAP_REQUEST_WRAPPER, soapRequest);
    keys = newKeys;

    return keys;
  }

  public void clearState() throws TransportBindingException {

  }

  public RawMessage createFaultMessage(Throwable thr) throws TransportBindingException {
    return null;
  }

  public RawMessage createResponseMessage(Object returnObject, Class returnObjectClass, Object[] resultParams, Class[] resultParamsClasses) throws TransportBindingException {
    return null;
  }

  public Key[] getMessageKeys() throws TransportBindingException {
    return null;
  }

  public Object[] getParameters(Class[] methodClass, ClassLoader loader) throws TransportBindingException {
    return null;
  }

  public RawMessage getRawMessage() throws TransportBindingException {
    return null;
  }

  public void sendResponse() throws TransportBindingException {

  }

  public void sendServerError(Throwable thr) throws TransportBindingException {

  }

  public void setGlobalConfiguration(Config config) throws TransportBindingException {

  }

  public void setMappingRegistries(JavaToQNameMappingRegistry nameMapping, TypeMappingRegistry registry) throws TransportBindingException {

  }

  public void setRawMessage(RawMessage message) throws TransportBindingException {

  }

//  public void setRuntimeContext(RuntimeContext wsInterface) throws TransportBindingException {
//
//  }

  public void setTransport(Transport transport) throws TransportBindingException {

  }

  public void setVirtualOperation(OperationDefinition e) throws TransportBindingException {

  }

}
