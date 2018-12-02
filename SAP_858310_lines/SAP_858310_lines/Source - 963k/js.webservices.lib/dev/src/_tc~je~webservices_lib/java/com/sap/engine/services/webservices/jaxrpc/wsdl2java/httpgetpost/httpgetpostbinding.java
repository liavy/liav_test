/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.httpgetpost;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.lib.xml.util.QName;
import com.sap.engine.services.webservices.jaxrpc.encoding.*;
import com.sap.engine.services.webservices.jaxrpc.util.CodeGenerator;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.*;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.AuthenticationFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.DefaultProviders;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.ProxyFeature;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.FeatureType;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.GlobalFeatures;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LocalFeatures;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.PropertyType;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.HTTPTransport;
import com.sap.engine.services.webservices.wsdl.*;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class HttpGetPostBinding implements ClientTransportBinding {

  public static final String HTTP_BINDING_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/http/";
  public static final String HTTP_GET_POST_BINDING = "HTTP Get/Post Transport binding";
  public static final String[] SUPPORTED_FEATURES = {AuthenticationFeature.AUTHENTICATION_FEATURE,ProxyFeature.PROXY_FEATURE};
  public static final String VERB = "verb";
  public static final String VERB_GET = "GET";
  public static final String VERB_POST = "POST";
  public static final String LOCATION = "location";
  public static final String REQUESTMETHOD = "requestMethod";
  public static final String URLENCODED = "urlEncoded";
  public static final String URLREPLACEMENT = "urlReplacement";
  public static final String MIME_CONTENT = "content";
  public static final String MIME_TYPE = "application/x-www-form-urlencoded";
  public static final String MIME_BINDING_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/mime/";
  public static final String MIME_XML = "mimeXml";
  public static String CONTENT_TYPE_HEADER = "Content-Type";
  public static String CONTENT_LENGTH_HEADER = "Content-Length";

  private TypeMappingRegistry registry;
  private SOAPSerializationContext serializationContext;
  private SOAPDeserializationContext deserializationContext;
  private XMLTokenReader parser;
  private ServiceParam[] inputParams;
  private ServiceParam[] outputParams;
  private ServiceParam[] faultParams;
  private HTTPTransport transport;


  public HttpGetPostBinding() {
    serializationContext = new SOAPSerializationContext();
    deserializationContext = new SOAPDeserializationContext();
    parser = XMLTokenReaderFactory.getInstance().createReader();
    transport = new HTTPTransport();
  }

  public boolean isFeatureImplemented(String featureName, PropertyContext property) {
    if (ProxyFeature.PROXY_FEATURE.equals(featureName)) {
      return true;
    }
    if (AuthenticationFeature.AUTHENTICATION_FEATURE.equals(featureName)) {
      return true;
    }
    return false;
  }

  public String getName() {
    return HTTP_GET_POST_BINDING;
  }

  public String[] getFeatures() {
    return SUPPORTED_FEATURES;
  }

  /**
   * Recognizes http binding.
   * @param bindingElement
   * @return
   */
  public boolean recognizeBinding(WSDLBinding bindingElement) {
    ArrayList extensions = bindingElement.getExtensions();
    if (extensions.size() == 0) {
      return false;
    }
    WSDLExtension extension = (WSDLExtension) extensions.get(0);
    if (extension.getLocalName().equals("binding") && HTTP_BINDING_NAMESPACE.equals(extension.getURI())) {
      return true;
    }
    return false;
  }

  /**
   * Returns main binding config.
   * @param binding
   * @param context
   */
  public void getMainBindingConfig(WSDLBinding binding, PropertyContext context) throws WSDLException {
    ArrayList extensions = binding.getExtensions();
    if (extensions.size() == 1) {
      WSDLExtension extensionElement = (WSDLExtension) extensions.get(0);
      String verb = extensionElement.getAttribute(VERB);
      if (verb == null) {
        verb = VERB_POST; // Default value
      }
      // sets verb property
      context.setProperty(VERB,verb);
    }
  }

  /**
   * Loads endpoint eddress
   * @param extension
   * @return
   * @throws WSDLException
   */
  public String loadAddress(WSDLExtension extension) throws WSDLException {
    if (HTTP_BINDING_NAMESPACE.equals(extension.getURI()) && extension.getLocalName().equals("address")) {
      String location = extension.getAttribute("location");
      if (location == null) {
        throw new WSDLException(" Location not specified in http:address");
      }
      return location;
    }
    throw new WSDLException(" The port HTTP Extension is invalid ");
  }

  public void getOperationBindingConfig(WSDLBindingOperation obinding, WSDLOperation operation, PropertyContext context, WSDLDefinitions definitions) throws WSDLException {
    ArrayList operationBinding = obinding.getExtensions();
    if (operationBinding.size() == 0) {
      throw new WSDLException("http:operation tag is mandatory in http binding !");
    } else {
      WSDLExtension extension = (WSDLExtension) operationBinding.get(0);
      if (HTTP_BINDING_NAMESPACE.equals(extension.getURI()) && extension.getLocalName().equals("operation")) {
        String location = extension.getAttribute("location");
        if (location == null) {
          throw new WSDLException(" Location not specified in http:operation");
        }
        context.setProperty(LOCATION,location);
      }
    }
    WSDLBindingChannel inputBinding = obinding.getInput();
    PropertyContext inputContext = context.getSubContext("input");
    ArrayList extensions = inputBinding.getExtensions();
    if (extensions.size() == 1) {
      WSDLExtension extension = (WSDLExtension) extensions.get(0);
      if (extension.getLocalName().equals(URLENCODED) && HTTP_BINDING_NAMESPACE.equals(extension.getURI())) {
        inputContext.setProperty(REQUESTMETHOD,URLENCODED);
      }
      if (extension.getLocalName().equals(URLREPLACEMENT) && HTTP_BINDING_NAMESPACE.equals(extension.getURI())) {
        inputContext.setProperty(REQUESTMETHOD,URLREPLACEMENT);
      }
      if (extension.getLocalName().equals(MIME_CONTENT) && MIME_BINDING_NAMESPACE.equals(extension.getURI())) {
        String type = extension.getAttribute("type");
        if (type == null) {
          throw new WSDLException("mime:content with no type specified found !");
        } else {
          if (type.equals(MIME_TYPE)) {
            inputContext.setProperty(REQUESTMETHOD,MIME_TYPE);
          } else {
            throw new WSDLException("only "+MIME_TYPE+" is possible as http binding request content type !");
          }
        }
      }
    }
    WSDLBindingChannel outputBinding = obinding.getOutput();
    if (outputBinding != null) {
      PropertyContext outputContext = context.getSubContext("output");
      extensions = outputBinding.getExtensions();
      for (int i=0; i<extensions.size(); i++) {
        WSDLExtension extension = (WSDLExtension) extensions.get(i);
        if (extension.getLocalName().equals(MIME_XML) && MIME_BINDING_NAMESPACE.equals(extension.getURI())) {
          String part = extension.getAttribute("part");
          if (part == null) { // There must be only one part in output
            WSDLChannel channel = operation.getOutput();
            if (channel == null) {
              throw new WSDLException("There is no output in bound portType operation "+operation.getName());
            }
            QName messageName = channel.getMessage();
            WSDLMessage message = definitions.getMessage(messageName.getLocalName(),messageName.getURI());
            if (message == null) {
              throw new WSDLException("There is no message named "+messageName.getLocalName());
            }
            if (message.getPartCount()!=1) {
              // Do nothing bypass part
              //throw new WSDLException("There must be only one part in message "+message.getName()+" to use mimeXml without part attribute");
            } else {
              part = message.getPart(0).getName();
            }
          }
          if (part != null) {
            outputContext.setProperty(part,"text/xml");
          }
        } else {

        }
      }
    }
  }

  private void makeHttpRequest(String endpoint, ServiceParam[] inputParams, PropertyContext operationContext, PropertyContext context, HTTPTransport transport, String verb, ProtocolList globalProtocols, ProtocolList localProtocols) throws Exception {
    PropertyContext featureContext = context.getSubContext(ClientTransportBinding.FEATUTE_CONFIG);
    String location = (String) operationContext.getProperty(LOCATION);
    if (location == null) {
      throw new Exception("Ivalid binding configuration. Operation locaion tag must be specified !");
    }
    URL baseAddress = URLLoader.fileOrURLToURL(null,endpoint);
    PropertyContext inputContext = operationContext.getSubContext("input");
    String requestMethod = (String) inputContext.getProperty(REQUESTMETHOD);
    if (requestMethod == null) {
      throw new Exception("Invalid binding confifuration. Operation request method must be ser !");
    }
    if (requestMethod.equals(URLENCODED)) {
      //URL finalEndpoint = URLLoader.fileOrURLToURL(baseAddress,location);
      String finalEndpoint = baseAddress.toString()+location;
      transport.init(finalEndpoint+'?'+getEncodedForm(inputParams),verb, featureContext);
      globalProtocols.handleRequest(null,context);
      localProtocols.handleRequest(null,context);
      transport.getRequestStream().flush();
    }
    if (requestMethod.equals(URLREPLACEMENT)) {
      String replacedVariant = getReplacementForm(location,inputParams);
      //URL finalEndpoint = URLLoader.fileOrURLToURL(baseAddress,replacedVariant);
      String finalEndpoint = baseAddress+replacedVariant;
      transport.init(finalEndpoint,verb, featureContext);
      globalProtocols.handleRequest(null,context);
      localProtocols.handleRequest(null,context);
      transport.getRequestStream().flush();
    }
    if (requestMethod.equals(MIME_TYPE)) {
      //URL finalEndpoint = URLLoader.fileOrURLToURL(baseAddress,location);
      String finalEndpoint = baseAddress.toString()+location;
      String content = getEncodedForm(inputParams);
      transport.init(finalEndpoint,verb, featureContext);
      transport.setHeader(CONTENT_LENGTH_HEADER,String.valueOf(content.length()));
      transport.setHeader(CONTENT_TYPE_HEADER,MIME_TYPE);
      globalProtocols.handleRequest(null,context);
      localProtocols.handleRequest(null,context);
      OutputStream output = transport.getRequestStream();
      output.write(content.getBytes()); //$JL-I18N$
      output.flush();
    }
  }

  private XMLTokenReader getOutputReader(HTTPTransport transport) throws Exception {
    InputStream inputStream = transport.getResponseStream();
    //parser.
    parser.init(inputStream);
    parser.begin();
    parser.moveToNextElementStart();
    return parser;
  }

  private void decodeResponse(ServiceParam[] outputParams, PropertyContext operationContext, HTTPTransport transport, ProtocolList globalProtocols, ProtocolList localProtocols, PropertyContext context) throws Exception {
    TypeMappingImpl typeMapping = (TypeMappingImpl) registry.getDefaultTypeMapping();
    deserializationContext.clearContext();
    deserializationContext.setTypeMapping(typeMapping);
    PropertyContext outputContext = operationContext.getSubContext("output");
    Enumeration enum1 = outputContext.getProperyKeys();
    boolean flag = false;
    if (enum1.hasMoreElements()) {
      String partName = (String) enum1.nextElement();
      String partType = (String) outputContext.getProperty(partName);
      for (int i=0; i<outputParams.length; i++) {
        if (outputParams[i].name.equals(partName)) {
          if (outputParams[i].isElement == false) {
            throw new Exception("Bound http response part must be element !");
          } else {
            String contentType = transport.getContentType();
            if (contentType == null) {
              throw new Exception("No content type specified !");
            }
            if (contentType.indexOf(partType)==-1) {
              throw new Exception("Content type should be "+partType+" but found "+contentType);
            }
            XMLTokenReader reader = getOutputReader(transport);
            javax.xml.namespace.QName elementType = typeMapping.getTypeForElement(outputParams[i].schemaName);
            DeserializerFactory factory = typeMapping.getDeserializer(outputParams[i].contentClass,elementType);
            DeserializerBase deserializer  = (DeserializerBase) factory.getDeserializerAs("");
            outputParams[i].content = deserializer.deserialize(reader, deserializationContext, outputParams[i].contentClass);
          }
        }
      }
      flag = true;
    }
    globalProtocols.handleResponse(null,context);
    localProtocols.handleResponse(null,context);
  }

  public void startOperation(ServiceParam[] inputParams, ServiceParam[] outputParams, ServiceParam[] faultParams) {
    this.inputParams = inputParams;
    this.outputParams = outputParams;
    this.faultParams = faultParams;
  }

  public void call(PropertyContext context, ProtocolList globalProtocols, ProtocolList localProtocols) throws Exception {
    // Get's binding configuration
    PropertyContext configContext = context.getSubContext(ClientTransportBinding.BINDING_CONFIG);
    //PropertyContext featureContext = context.getSubContext(ClientTransportBinding.FEATUTE_CONFIG);
    if (configContext.isDefined() == false) {
      throw new Exception(" No binding configuration provided in context !");
    }
    String endpoint = (String) configContext.getProperty(ENDPOINT);
    if (endpoint == null) {
      throw new Exception(" Endpoint not specified in binding context !");
    }

    String verb = (String) configContext.getProperty(VERB);
    if (verb == null) {
      throw new Exception("Ivalid binding configuration ! HTTP binding verb not set !");
    }
    // maps transport info
    context.setProperty(ClientTransportBinding.TRANSPORT_INTERFACE,transport);
    // makes request
    try {
    makeHttpRequest(endpoint, inputParams, configContext, context, transport, verb, globalProtocols, localProtocols);
    int responseCode = transport.getResponseCode();
//    String contentType = transport.getContentType();
    if (responseCode == 200) { // message is processed

      decodeResponse(outputParams,configContext,transport,globalProtocols,localProtocols,context);
      return;
    }
    if (responseCode == 500) { // internal server error
      throw new Exception("Internal Server Error !");
//      if (contentType.indexOf("text/xml") != -1) { // soap message fault
//        message.initDeserializationMode(transport);
//        return responseCode;
//      } else {
//        throw new Exception("Unaccepted content type ("+contentType+") !");
//      }
    }
      throw new Exception("Transport Error ! Response code ("+responseCode+") "+transport.getResponseMessage());
    } finally {
      transport.closeSession();
    }


  }

  public ServiceParam[] getResponseParts() {
    return new ServiceParam[0];
  }

  public ServiceParam[] getFaultParts() {
    return new ServiceParam[0];
  }



  public void setTypeMappingRegistry(TypeMappingRegistry registry) {
    this.registry = registry;
  }

  public TypeMappingRegistry getTypeMappingRegistry() {
    return this.registry;
  }

  public boolean isCompatible(AbstractProtocol protocol) {
    return false;
  }

  public void generateCustom(String packageName, File outputDir, WSDLDefinitions definitions, PropertyContext context, WSDLOperation operation) {
  }

  public void addImport(CodeGenerator generator) {
  }

  public void addConstructorCode(CodeGenerator generator) {
  }

  public void addVariables(CodeGenerator generator) {
  }

  private String getReplacementForm(String locationUrl, ServiceParam[] parts) throws Exception {
    TypeMapping typeMapping = registry.getDefaultTypeMapping();
    serializationContext.clearContext();
    StringBuffer result = new StringBuffer();
    boolean flag = false;
    for (int i=0; i<locationUrl.length(); i++) {
      flag = true;
      for (int j=0; j<parts.length; j++) {
        if (locationUrl.startsWith('('+parts[j].name+')',i)) { // part found replace
          SerializerFactory factory = typeMapping.getSerializer(parts[i].contentClass,parts[i].schemaName);
          SerializerBase serializer = (SerializerBase) factory.getSerializerAs(null);
          String value = serializer.serialize(parts[i].content,serializationContext);
          if (value == null) {
            value = "";
          }
          result.append(URLEncoder.encode(value));
          flag = false;
          i+=parts[j].name.length()+1;
          break;
        }
      }
      if (flag) {
        result.append(locationUrl.charAt(i));
      }
    }
    return result.toString();
  }

  private String getEncodedForm(ServiceParam[] parts) throws Exception {
    TypeMapping typeMapping = registry.getDefaultTypeMapping();
    serializationContext.clearContext();
    StringBuffer result = new StringBuffer();
    boolean firstFlag = false;
    for (int i=0; i<parts.length; i++) {
      SerializerFactory factory = typeMapping.getSerializer(parts[i].contentClass,parts[i].schemaName);
      SerializerBase serializer = (SerializerBase) factory.getSerializerAs(null);
      String value = serializer.serialize(parts[i].content,serializationContext);
      if (value != null) {
        if (firstFlag) {
          result.append('&');
        }
        result.append(URLEncoder.encode(parts[i].name));
        result.append('=');
        result.append(URLEncoder.encode(value));
        firstFlag = true;
      }
    }
    return result.toString();
  }

  public void flush(PropertyContext context, ProtocolList globalProtocols) throws Exception {
  }

  /**
   * Implementing this method the binding can add Binding Specific Features.
   * @param globalFeatures
   * @param binding
   * @return
   */
  public GlobalFeatures importGlobalFeatures(GlobalFeatures globalFeatures, WSDLBinding binding) {
    if (globalFeatures.getFeature(AuthenticationFeature.AUTHENTICATION_FEATURE) == null) {
      FeatureType securityFeature = new FeatureType();
      securityFeature.setName(AuthenticationFeature.AUTHENTICATION_FEATURE);
      String provider = DefaultProviders.getProvider(securityFeature.getName());
      if (provider != null) {
        securityFeature.setProvider(provider);
      }
      securityFeature.setOriginal(false);
      PropertyType property = new PropertyType();
      property.setName(AuthenticationFeature.AUTHENTICATION_MECHANISM);
      property.setValue(AuthenticationFeature.NONE);
      securityFeature.addProperty(property);
      globalFeatures.addFeature(securityFeature);
    }
    return globalFeatures;
  }

  /**
   * Implementing this method the binding can add Operation Specific Features.
   * @param features
   * @param operation
   * @return
   */
  public LocalFeatures importLocalFeatures(LocalFeatures features, WSDLBindingOperation operation) {
    return features;
  }
}
