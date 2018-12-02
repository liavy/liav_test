package com.sap.engine.services.webservices.runtime.wsdl;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.runtime.Config;
import com.sap.engine.interfaces.webservices.runtime.Fault;
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
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.lib.xml.util.QName;
import com.sap.engine.services.webservices.additions.soaphttp.SOAPHTTPConfigProperties;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.ProcessException;
import com.sap.engine.services.webservices.runtime.interfaces.RuntimeTransportBinding;
import com.sap.engine.services.webservices.wsdl.WSDLBinding;
import com.sap.engine.services.webservices.wsdl.WSDLBindingChannel;
import com.sap.engine.services.webservices.wsdl.WSDLBindingOperation;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.wsdl.WSDLDocumentation;
import com.sap.engine.services.webservices.wsdl.WSDLException;
import com.sap.engine.services.webservices.wsdl.WSDLExtension;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * This class generates the binding for MIME
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class AbstractMIMETransportBinding implements RuntimeTransportBinding, SOAPHTTPConfigProperties, TransportBinding {

  protected static final String HTTPTRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";
  protected static final String SOAP_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
  protected static final String DOCUMENTSTYLE = "document";
  protected static final String TRANSPORT = "transport";
  protected static final String BINDING = "binding";
  protected static final String RPCSTYLE = "rpc";
  protected static final String HEADER_SCHEMAURI_PREFIX = "hs";
//  protected static final String ATTACH_PROPERTY  =  "attach";
  protected static final String MIME_STYLE_WSDL  =  "mime";

  //Attachments property
  static final String ATTACHMENT_PARTS_PROP  =  "attachment-parts";
  //Content-type property
  static final String CONTENT_TYPE_PROP  =  "content-type";

  public String[] getDefaultStyles() {
    return new String[]{MIME_STYLE_WSDL};
  }

  public int[] getNecessaryPortTypes() {
    return new int[]{PortTypeDescriptor.ENCODED_PORTTYPE};
  }

  public String[] getSupportedSyles() {
    return new String[]{MIME_STYLE_WSDL};
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

  public void generateBinding(String bindingStyle, javax.xml.namespace.QName bindingQName, Config globalConfig, OperationDefinition[] operations, PortTypeDescriptor[] descriptors, WSDLDefinitions definitions) throws TransportBindingException {
    String bindingName = bindingQName.getLocalPart();
    try {
      if ((bindingStyle == null) || (! bindingStyle.equals(MIME_STYLE_WSDL)))  {
        throw new ProcessException(ExceptionConstants.UNRECORGNIZED_BINIDING_STYLE, new Object[]{bindingStyle});
      }
  
      WSDLBinding binding = new WSDLBinding();
      binding.setName(bindingName);
      WSDLExtension bindingExtension = new WSDLExtension();
      bindingExtension.setLocalName("binding");
      bindingExtension.setURI(SOAP_URI);
      bindingExtension.setAttribute(new SimpleAttr(TRANSPORT, HTTPTRANSPORT_URI, ""));
  
      binding.addExtension(bindingExtension);
  
      for (int i = 0; i < descriptors.length; i++) {
        if (descriptors[i].getType() == PortTypeDescriptor.ENCODED_PORTTYPE) {
          binding.setType(new QName(getPortTypeNSPrefix(definitions, descriptors[i].getQName().getNamespaceURI()), descriptors[i].getQName().getLocalPart(), descriptors[i].getQName().getNamespaceURI()));
          bindingExtension.setAttribute(new SimpleAttr(STYLE, RPCSTYLE, ""));
          generateBindingOperations(definitions, binding, operations);
          return;
        }
      }
    } catch (ProcessException pEx) {
      throw new TransportBindingException(pEx);
    }
  }

  private void generateBindingOperations(WSDLDefinitions definitions, WSDLBinding binding, OperationDefinition operations[]) throws ProcessException {
    WSDLBindingOperation tempBindingOperation;

    //rpc_enc generation cause only this is passed from the invoking method
    for (int i = 0; i < operations.length; i++) {

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

      String namespace = null;
      String encodingStyle = null;
      boolean exOccurs = false;
      try {
        namespace = operations[i].getInputConfiguration().getProperty(NAMESPACE).getValue();
        encodingStyle = operations[i].getInputConfiguration().getProperty(ENCODINGSTYLE).getValue();
      } catch (NullPointerException nullE) {
        exOccurs = true;
      } finally {
        if (exOccurs || (namespace == null) || (encodingStyle == null)) {
          throw new ProcessException(ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{operations[i].getOperationName(), NAMESPACE + " or " + ENCODINGSTYLE, "input"});
        }
      }

      WSDLBindingChannel channel = this.appendMIMEExtension(encodingStyle, namespace, operations[i].getInputParameters(), operations[i].getInputConfiguration());
//        tempBindingOperation.setInput(channel);
      //in channel
      tempBindingOperation.setInput(channel);
      tempBindingOperation.addExtension(extension);

      try {
        namespace = operations[i].getOutputConfiguration().getProperty(NAMESPACE).getValue();
        encodingStyle = operations[i].getOutputConfiguration().getProperty(ENCODINGSTYLE).getValue();
      } catch (NullPointerException nullE) {
        exOccurs = true;
      } finally {
        if (exOccurs || (namespace == null) || (encodingStyle == null)) {
          throw new ProcessException(ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{operations[i].getOperationName(), NAMESPACE + " or " + ENCODINGSTYLE, "output"});
        }
      }

      //in case it is one-way operation miss output and fault generation
      if (AbstractSOAPTransportBinding.isOneWay(operations[i])) {
        continue;
      }

      channel = this.appendMIMEExtension(encodingStyle, namespace, operations[i].getOutputParameters(), operations[i].getOutputConfiguration());
      //out channel
      tempBindingOperation.setOutput(channel);
      //fault channels
      Fault[] faults = operations[i].getFaults();
      for (int k = 0; k < faults.length; k++) {
        try {
          encodingStyle = faults[k].getFaultConfiguration().getProperty(ENCODINGSTYLE).getValue();
          tempBindingOperation.addFault(BaseWSDLComponentGenerator.generateBindingFault(faults[k].getFaultName(), "encoded", encodingStyle));
        } catch (NullPointerException e) {
          throw new ProcessException(ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{faults[k].getFaultName() + "of " + operations[i].getOperationName(), ENCODINGSTYLE, "fault"});
        } catch (WSDLException wsdlE) {
          throw new ProcessException(ExceptionConstants.WSDLEXCEPTION_IN_BINDING_CREATION, new Object[]{binding.getName(), wsdlE.getLocalizedMessage()}, wsdlE);
        }
      }

    }

    try {
      definitions.addBinding(binding);
    } catch (WSDLException wsdlE) {
      throw new ProcessException(ExceptionConstants.WSDLEXCEPTION_IN_BINDING_CREATION, new Object[]{binding.getName(), wsdlE.getLocalizedMessage(), "Binding creation fails"}, wsdlE);
    }
  }


  private WSDLBindingChannel appendMIMEExtension(String encodingStyle, String namespace, ParameterNode[] nodes, Config cnf) throws ProcessException {

    //in case of void normal soap
    if (nodes == null || nodes.length == 0 || (nodes.length == 1 && nodes[0].getJavaClassName().equalsIgnoreCase("void"))) {
      return BaseWSDLComponentGenerator.generateSOAPEncodedChannel(namespace, encodingStyle);
    }

    //in case no attatchments are described -> normal soap
    if (cnf.getProperty(ATTACHMENT_PARTS_PROP) == null) {
      return BaseWSDLComponentGenerator.generateSOAPEncodedChannel(namespace, encodingStyle);
    }

    PropertyDescriptor attachProperty = cnf.getProperty(ATTACHMENT_PARTS_PROP);

    WSDLBindingChannel channel = new WSDLBindingChannel();

    WSDLExtension mprExt = new WSDLExtension();
    mprExt.setLocalName("multipartRelated");
    mprExt.setURI(NS.WSDL_MIME_EXTENSION);

    WSDLExtension part = new WSDLExtension();
    part.setLocalName("part");
    part.setURI(NS.WSDL_MIME_EXTENSION);
    //creation of soap part
    WSDLExtension soapPart = new WSDLExtension();
    soapPart.setLocalName("body");
    soapPart.setURI(NS.WSDL_SOAP_EXTENSION);
    soapPart.setAttribute(new SimpleAttr("use", "encoded", ""));
    soapPart.setAttribute(new SimpleAttr("encodingStyle", NS.SOAPENC, ""));

    if (namespace != null) {
      soapPart.setAttribute(new SimpleAttr("namespace", namespace, ""));
    }
    //addition to the main mimemultipart
    part.addChild(soapPart);
    mprExt.addChild(part);

    WSDLExtension paramParts;

    PropertyDescriptor partProperties;
    String contentType;

    for (int i = 0; i < nodes.length; i++) {
      if (! nodes[i].isExposed()) {
        continue;
      }

      //if it is not attachement do not describe it
      if ((partProperties = attachProperty.getInternalDescriptor(nodes[i].getParameterName())) == null) {
        continue;
      }

      //contentType property must be present
      try {
        contentType = partProperties.getInternalDescriptor(CONTENT_TYPE_PROP).getValue();
      } catch (NullPointerException nE) {
        throw new ProcessException(ExceptionConstants.INCORRECT_PROPERTY_VALUE, new Object[]{CONTENT_TYPE_PROP, cnf, "null"});
      }

      part = new WSDLExtension();
      part.setLocalName("part");
      part.setURI(NS.WSDL_MIME_EXTENSION);

      paramParts = new WSDLExtension();
      paramParts.setLocalName("content");
      paramParts.setURI(NS.WSDL_MIME_EXTENSION);
      paramParts.setAttribute("part", nodes[i].getParameterName(), "");
      paramParts.setAttribute("type", contentType, "");

      part.addChild(paramParts);
      mprExt.addChild(part);
    }

    channel.addExtension(mprExt);
    return channel;
  }
  
  public Key[] getOperationKeys(OperationDefinition operationData) {
    Config cfg = operationData.getInputConfiguration();
    PropertyDescriptor soapActionDescr = cfg.getProperty(SOAPACTION_KEY);
    String firstElName = operationData.getOperationName();
    PropertyDescriptor namespaceDescr = cfg.getProperty(NAMESPACE);

    if ((namespaceDescr == null) || namespaceDescr.getValue() == null) {
    //  throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{firstElName, NAMESPACE, "input"});
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