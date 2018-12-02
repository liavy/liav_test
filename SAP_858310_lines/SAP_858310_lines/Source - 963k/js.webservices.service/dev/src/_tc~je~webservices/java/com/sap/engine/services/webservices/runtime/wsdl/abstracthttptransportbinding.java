package com.sap.engine.services.webservices.runtime.wsdl;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.runtime.Config;
import com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappingRegistry;
import com.sap.engine.interfaces.webservices.runtime.Key;
import com.sap.engine.interfaces.webservices.runtime.OperationDefinition;
import com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor;
import com.sap.engine.interfaces.webservices.runtime.RawMessage;
import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.interfaces.webservices.runtime.TransportBinding;
import com.sap.engine.interfaces.webservices.runtime.TransportBindingException;
import com.sap.engine.lib.xml.parser.helpers.CharArray;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.ConfigurationException;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.ProcessException;
import com.sap.engine.services.webservices.additions.soaphttp.exceptions.TBindingResourceAccessor;
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
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class AbstractHTTPTransportBinding  implements RuntimeTransportBinding, TransportBinding {

  //Global property denoting whether Post or Get binding
  protected static final String METHOD_PROPERTY  =  "method";
  //Http binding extension elements ns
  private static final String WSDL_HTTP_NS  =  "http://schemas.xmlsoap.org/wsdl/http/";

  protected static final String POST  =  "POST";
  protected static final String GET  =  "GET";

  protected static final String HTTP_STYLE_WSDL  =  "http";
  
  //Operatoin key-properties
  public static final String SOAPACTION_KEY = "soapAction";
  public static final String FIRST_ELEMENT_NS = "first-body-element-ns";
  public static final String FIRST_ELEMENT_NAME = "first-body-element-name";
  public static final String NAMESPACE = "namespace";
  
  public String[] getSupportedSyles() {
    return new String[]{HTTP_STYLE_WSDL};
  }

  public int[] getNecessaryPortTypes() {
    return new int[]{com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor.HTTP_PORTTYPE};
  }

  public String[] getDefaultStyles() {
    return new String[]{HTTP_STYLE_WSDL};
  }

  private String getPortTypeNSPrefix(WSDLDefinitions def, String ns, String dfPrefix) {
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

    def.addAdditionalAttribute("xmlns:" + dfPrefix, ns);
    return dfPrefix;
  }

  public void generateBinding(String bindingStyle, QName bindingName, com.sap.engine.interfaces.webservices.runtime.Config globalConfig, com.sap.engine.interfaces.webservices.runtime.OperationDefinition[] operations, com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor portTypeDecriptors[], WSDLDefinitions definitions) throws com.sap.engine.interfaces.webservices.runtime.TransportBindingException {

    if ((bindingStyle == null) || (! bindingStyle.equals(HTTP_STYLE_WSDL)))  {
      throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.UNRECORGNIZED_BINIDING_STYLE, new Object[]{bindingStyle});
    }

    String methodProperty = null;
    try {
      methodProperty = globalConfig.getProperty(METHOD_PROPERTY).getValue();
      if ((! methodProperty.equalsIgnoreCase(GET)) && (! methodProperty.equalsIgnoreCase(POST))) {
        throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.INCORRECT_PROPERTY_VALUE, new Object[]{METHOD_PROPERTY, "global TransportBinding config", methodProperty});
      }
    } catch (NullPointerException nE) {
      throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.INCORRECT_PROPERTY_VALUE, new Object[]{METHOD_PROPERTY, "global transportBinding", methodProperty});
    }

    String portTypePrefix = null;
    String portTypeName = null;
    for (int i = 0; i < portTypeDecriptors.length; i++) {
      if (portTypeDecriptors[i].getType() == com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor.HTTP_PORTTYPE) {
        portTypeName = portTypeDecriptors[i].getQName().getLocalPart();
        portTypePrefix = getPortTypeNSPrefix(definitions, portTypeDecriptors[i].getQName().getNamespaceURI(), "prt");
        break;
      }
//      System.out.println("AbstractHTTPTransportBinding portType: " + portTypeDecriptors[i].getQName() + " type: " + portTypeDecriptors[i].getType());
    }

    //binding generation
    WSDLBinding binding = new WSDLBinding();
    binding.setName(bindingName.getLocalPart());
    binding.setType(new com.sap.engine.lib.xml.util.QName(portTypePrefix, portTypeName, ""));
    //Adding Http specific binding extension
    WSDLExtension ext = new WSDLExtension();
    ext.setURI(WSDL_HTTP_NS);
    ext.setLocalName("binding");
    if (methodProperty.equalsIgnoreCase(GET)) {
      ext.setAttribute("verb", GET, null);
    } else {
      ext.setAttribute("verb", POST, null);
    }
    binding.addExtension(ext);

    WSDLBindingOperation bOperation;
    WSDLBindingChannel channel;
    //binding operation creation and addition
    for (int i = 0; i < operations.length; i++) {
      bOperation = new WSDLBindingOperation();
      binding.addOperation(bOperation);
      bOperation.setName(operations[i].getOperationName());

      if (operations[i].getDescription() != null) {
        WSDLDocumentation documentation = new WSDLDocumentation();
        documentation.setContent(new CharArray(operations[i].getDescription()));
        bOperation.setDocumentation(documentation);
      }

      //adding operation extension
      ext = new WSDLExtension();
      ext.setLocalName("operation");
      ext.setURI(WSDL_HTTP_NS);
      ext.setAttribute("location", "/" + operations[i].getOperationName(), ""); //relative location starts witj '/'
      bOperation.addExtension(ext);

      //adding input channel
      channel = new WSDLBindingChannel();
      ext = new WSDLExtension();
      if (methodProperty.equalsIgnoreCase(GET)) {//urlEncoded is used in such case
        ext.setLocalName("urlEncoded");
        ext.setURI(WSDL_HTTP_NS);
      } else { //in POST mime-type application/x-www-form-urlencoded
        ext.setLocalName("content");
        ext.setURI(NS.WSDL_MIME_EXTENSION);
        ext.setAttribute("type", "application/x-www-form-urlencoded", "");
      }
      channel.addExtension(ext);
      bOperation.setInput(channel);

      //in case it is one-way operation miss output and fault generation
      if (AbstractSOAPTransportBinding.isOneWay(operations[i])) {
        continue;
      }

      //adding output channel
      channel = new WSDLBindingChannel();
      ext = new WSDLExtension();
      ext.setLocalName("mimeXml"); //response is XML described with schema
      ext.setURI(NS.WSDL_MIME_EXTENSION);
      channel.addExtension(ext);
      bOperation.setOutput(channel);

      //fault are not mention because it is not know how to support
    }

    try {
      definitions.addBinding(binding);
    } catch (WSDLException wsE) {
      Exception p = new ProcessException(ExceptionConstants.WSDLEXCEPTION_IN_BINDING_CREATION, new Object[]{binding.getName(), wsE.getLocalizedMessage()}, wsE);
      throw new TransportBindingException(p);
    }
  }

  public Key[] getOperationKeys(OperationDefinition operationData) {
    Config cfg = operationData.getInputConfiguration();
    PropertyDescriptor soapActionDescr = cfg.getProperty(SOAPACTION_KEY);
    String firstElName = operationData.getOperationName();
    PropertyDescriptor namespaceDescr = cfg.getProperty(NAMESPACE);

    if ((namespaceDescr == null) || namespaceDescr.getValue() == null) {
     // throw new ConfigurationException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.OPERATION_CONFIGURATION_EXCEPTION, new Object[]{firstElName, NAMESPACE, "input"});
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