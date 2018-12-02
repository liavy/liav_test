package com.sap.engine.services.webservices.espbase.client.bindings.impl;

import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;

import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientHTTPTransport;
import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.exceptions.TransportBindingException;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding;
import com.sap.engine.services.webservices.jaxrpc.encoding.DeserializerBase;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPSerializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SerializerBase;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.httpgetpost.URLEncoder;

public class HTTPTransportBinding extends BaseTransportBinding {

  private ClientHTTPTransport transport;
  private SOAPSerializationContext serializationCtx;
  private SOAPDeserializationContext deserializationCtx;
  
  public HTTPTransportBinding() {
    transport = new ClientHTTPTransport();
    serializationCtx = new SOAPSerializationContext();
    deserializationCtx = new SOAPDeserializationContext();
  }
  
  public void call(ClientConfigurationContext context) throws RemoteException {
    try {
      InterfaceMapping interfaceMapping = context.getStaticContext().getInterfaceData();
      OperationMapping operationMapping = interfaceMapping.getOperationByJavaName(context.getOperationName());
      ParameterObject[] paramObjects = context.getOperationParameters();
      sendHTTPRequest(interfaceMapping, operationMapping, paramObjects, context);
      int responseCode = transport.getResponseCode();
      if(responseCode == 200) {
        readHTTPResponse(operationMapping, paramObjects, context);
      } else if(responseCode != 202) {
        String httpProxyInfo = getLogHttpProxyInfo(context);
        throw new TransportBindingException(TransportBindingException.INVALID_RESPONSE_CODE, String.valueOf(responseCode), transport.getResponseMessage(), transport.getEndpoint(), httpProxyInfo);
      }
    } catch(Throwable thr) {
      throw (thr instanceof RemoteException) ? (RemoteException)thr : new RemoteException("Transport binding exception", thr); 
    } finally {
      transport.closeSession();
    }
  }
  
  private void readHTTPResponse(OperationMapping operationMapping, ParameterObject[] paramObjects, ClientConfigurationContext context) throws Exception {
    ParameterMapping[] paramMappings = operationMapping.getParameters(ParameterMapping.RETURN_TYPE);
    ParameterMapping returnParamMapping = paramMappings.length > 0 ? paramMappings[0] : null;
    if(returnParamMapping != null) {
      prepareDeserilizationContext(context);
      XMLTokenReader reader = createReader();
      ParameterObject returnParamObj = paramObjects[paramObjects.length - 1];
      Object deserializedValue = deserializeParameter(reader, returnParamObj, returnParamMapping, (ExtendedTypeMapping)(context.getTypeMaping()));
      returnParamObj.parameterValue = deserializedValue;
    }
  }
  
  private XMLTokenReader createReader() throws Exception {
    XMLTokenReaderFactory factory = XMLTokenReaderFactory.getInstance();    
    XMLTokenReader reader = factory.createReader(transport.getResponseStream());
   
    reader.begin();
    reader.moveToNextElementStart();
    return(reader);
  }
  
  private void prepareDeserilizationContext(ClientConfigurationContext context) {
    deserializationCtx.clearContext();
    deserializationCtx.setApplicationClassLoader(context.getClientAppClassLoader());    
    deserializationCtx.setTypeMapping(context.getTypeMaping());
    deserializationCtx.setProperty(SOAPDeserializationContext.TOLERANT_DESERIALIZATION, PublicProperties.BOOLEAN_TRUE_VALUE);
  }
  
  private void sendHTTPRequest(InterfaceMapping interfaceMapping, OperationMapping operationMapping, ParameterObject[] paramObjects, ClientConfigurationContext context) throws Exception {
    prepareSerializationContext(context.getTypeMaping());
    ParameterMapping[] paramMappings = operationMapping.getParameters(ParameterMapping.IN_TYPE);
    initialParametersCheck(paramObjects, operationMapping, context);
    String httpBindingVerb = interfaceMapping.getHTTPRequestMethod();
    String httpOperationURLLocation = operationMapping.getHTTPLocation();
    String portEndpointURIPath = PublicProperties.getEndpointURL(context);
    URI endpointURI = null;
    String content = null;
    if(HTTPBinding.HTTP_GET_METHOD.equals(httpBindingVerb)) {
      String endpointURIPathWithoutParamsEncPart = portEndpointURIPath + "/" + httpOperationURLLocation; 
      endpointURI = new URI(endpointURIPathWithoutParamsEncPart + createURLEncodedPart(endpointURIPathWithoutParamsEncPart, paramObjects, paramMappings));
    } else if(HTTPBinding.HTTP_POST_METHOD.equals(httpBindingVerb)) {
      endpointURI = new URI(portEndpointURIPath + "/" + httpOperationURLLocation);
      content = createParametersEncodedPart(paramObjects, paramMappings);
    }
    PublicProperties.setEndpointURL(endpointURI.normalize().toString(), context);
    try {
      sendHTTPRequest(content, operationMapping.getHTTPInputSerializationType(), httpBindingVerb, context);
    } finally {
      PublicProperties.setEndpointURL(portEndpointURIPath, context);
    }
  }
  
  private void prepareSerializationContext(TypeMapping typeMapping) {
    serializationCtx.clearContext();
    serializationCtx.setTypeMapping(typeMapping);
  }
  
  private void sendHTTPRequest(String content, String contentType, String verb, ClientConfigurationContext context) throws Exception {
    transport.init(verb, context);
    OutputStream requestStream = null;
    if(content != null) {
      transport.setHeader(CONTENT_LENGTH_HEADER, String.valueOf(content.getBytes().length)); //$JL-I18N$
      transport.setHeader(CONTENT_TYPE_HEADER, contentType);
      requestStream = transport.getRequestStream();
      requestStream.write(content.getBytes()); //$JL-I18N$
    } else {
      requestStream = transport.getRequestStream();
    }
    requestStream.flush();
  }

  private Object deserializeParameter(XMLTokenReader reader, ParameterObject paramObj, ParameterMapping paramMapping, ExtendedTypeMapping typeMapping) throws Exception {
    QName typeName = typeMapping.getTypeForElement(paramMapping.getSchemaQName());
    DeserializerBase deserializer = deserializationCtx.getDeserializer(typeName, paramObj.parameterType);
    return(deserializer.deserialize(reader, deserializationCtx, paramObj.parameterType));
  }
  
  private String serializeParameter(ParameterObject paramObj, ParameterMapping paramMapping) throws Exception {
    Object content = JAXWSUtil.getParameterContent(paramObj);
    SerializerBase serializer = serializationCtx.getSerializer(paramMapping.getSchemaQName(), JAXWSUtil.getParameterClass(paramObj, content));
    return(serializer.serialize(content, serializationCtx));
  }
  
  private String createParametersEncodedPart(ParameterObject[] paramObjects, ParameterMapping[] paramMappings) throws Exception {
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < paramMappings.length; i++) {
      ParameterMapping paramMapping = paramMappings[i];
      String paramValue = URLEncoder.encode(serializeParameter(paramObjects[i], paramMapping));
      String paramName = URLEncoder.encode(paramMapping.getJavaParamName());
      buffer.append(paramName);
      buffer.append("=");
      buffer.append(paramValue);
      if(i != paramMappings.length - 1) {
        buffer.append("&");
      }
    }
    return(buffer.toString());
  }
  
  private String createURLReplacedPart(ParameterObject[] paramObects, ParameterMapping[] paramMappings, String operationRelativeURLPath) throws Exception {
    String urlReplacedPart = operationRelativeURLPath;
    for(int i = 0; i < paramMappings.length; i++) {
      ParameterMapping paramMapping = paramMappings[i];
      String replacement = URLEncoder.encode(serializeParameter(paramObects[i], paramMapping));
      String replaced = "(" + paramMapping.getJavaParamName() + ")";
      int replacedIndex = urlReplacedPart.indexOf(replaced);
      if(replacedIndex > 0) {
        urlReplacedPart = urlReplacedPart.replace(replaced, replacement);
      }
    }
    return(urlReplacedPart);
  }
  
  private String createURLEncodedPart(String portEndepointURIPath, ParameterObject[] paramObjects, ParameterMapping[] paramMappings) throws Exception {
    String paramEncodedPart = createParametersEncodedPart(paramObjects, paramMappings);
    if(paramEncodedPart.length() > 0) {
      return(portEndepointURIPath.indexOf("?") > 0 ? "&" + paramEncodedPart : "?" + paramEncodedPart);
    }
    return("");
  }
  
  public void sendMessage(ClientConfigurationContext context) throws RemoteException {  
  }
}
