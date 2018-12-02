package com.sap.engine.services.webservices.espbase.server.additions;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.holders.ObjectHolder;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.ISoap12FaultException;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.ISoapFaultException;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriterUtil;
import com.sap.engine.lib.xml.stream.XMLStreamReaderImpl;
import com.sap.engine.lib.xml.stream.XMLStreamWriterImpl;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.client.api.AttachmentHandler;
import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.bindings.exceptions.TransportBindingException;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.JAXWSUtil;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.SDOUtil;
import com.sap.engine.services.webservices.espbase.client.dynamic.ServiceFactoryConfig;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ProcessException;
import com.sap.engine.services.webservices.espbase.server.api.ProviderAttachmentHandlerFactory;
import com.sap.engine.services.webservices.espbase.server.runtime.ProviderContextHelperImpl;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.jaxb.AttachmentMarshallerImpl;
import com.sap.engine.services.webservices.jaxb.AttachmentUnmarshallerImpl;
import com.sap.engine.services.webservices.jaxrpc.encoding.DeserializerBase;
import com.sap.engine.services.webservices.jaxrpc.encoding.PropertyList;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationState;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPSerializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SerializerBase;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingImpl;
import com.sap.engine.services.webservices.jaxrpc.exceptions.XmlMarshalException;
import com.sap.engine.services.webservices.tools.InstancesPool;
import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;
import com.sap.sdo.api.helper.SapXmlHelper;
import com.sap.tc.logging.Location;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class StreamEngine {
  //Error codes and namespaces
  static final String SERVER_ERROR_CODE = "Server";
  static final String CLIENT_ERROR_CODE = "Client";
  static final String SERVER_ERROR_NAMESPACE = "http://sap-j2ee-engine/error";
  static final String CLIENT_ERROR_NAMESPACE = "http://sap-j2ee-engine/client-runtime-error";
  //Operation modes
  static final String ENCODED_USE = "encoded";
  static final String LITERAL_USE = "literal";
  //The response body wrapper element prefix
  private static final String RESPONSE_ELEMENT_PREFIX = "rpl";
  //encodingStyle attribute
  private static final String ENCODINGSTYLE  =  "encodingStyle";

//  //output operation property indicating that the outsideIn wsdl schema is qualified
//  private static final String IS_QUALIFIED  =  "isQualified";
//  //output operation property indicating that the outsideIn output has only bodyContant wrapper, and no wrapper for the response param.
//  private static final String OMIT_PART_WRAPPER  =  "omit-part-wrapper";
//  //for outsideIn support indicates the name of the fault elemet wrapper
//  private static final String FAULT_ELEMENT_NAME  =  "fault-element-name";
  //SOAP mustUnderstand attribute name
  private static final String SOAP_MUSTUNDERSTAND_ATTRIBUTE = "mustUnderstand";
  //SOAP mustUnderstand error code
//  private static final String SOAP_MUSTUNDERSTAND_ERROR_CODE = "MustUnderstand";

  private static final Location LOC = Location.getLocation(StreamEngine.class);

  private static InstancesPool deserializationContextPool = new InstancesPool();
  private static InstancesPool serializationContextPool = new InstancesPool();


  static final Object[] deserializeLiteral(Class[] mParams, ClassLoader appLoader, ProviderContextHelper ctx) throws RuntimeProcessException {
    return deserialize(mParams, appLoader, LITERAL_USE, ctx);
  }

  static final Object[] deserializeEncoded(Class[] mParams, ClassLoader appLoader, ProviderContextHelper ctx) throws RuntimeProcessException {
    return deserialize(mParams, appLoader, ENCODED_USE, ctx);
  }

  static void serializeLiteral(Object returnObject, Class objectClass, String style, ProviderContextHelper ctx) throws ProcessException {
    serialize(returnObject, objectClass, LITERAL_USE, style, ctx);
  }

  static void serializeEncoded(Object returnObject, Class objectClass, ProviderContextHelper ctx) throws ProcessException {
    serialize(returnObject, objectClass, ENCODED_USE, null, ctx);
  }

  public static void serializeThrowableEncoded(Throwable thr, ParameterMapping fault, String faultCode, ProviderContextHelper ctx) throws ProcessException {
    serializeThrowable(thr, fault, faultCode, ENCODED_USE, ctx);
  }

  public static void serializeThrowableLiteral(Throwable thr, ParameterMapping fault, String faultCode, ProviderContextHelper ctx) throws ProcessException {
    serializeThrowable(thr, fault, faultCode, LITERAL_USE, ctx);
  }
  
  //i044259
  // resultParamClasses can be null 
  public static void serializeSDO(Object returnObject, /*Class objectClass,*/ Object[] resultParams, Class[] resultParamsClasses, ProviderContextHelper context)
    throws RuntimeProcessException{
    try {      
      
      SapXmlHelper xmlHelper = (SapXmlHelper) context.getSdoHelper();
      if (xmlHelper == null){ 
        throw new RuntimeProcessException("No Galaxy SDO helper found in context");
      }
      Map options = (Map) context.getProperty(ServiceFactoryConfig.OPTIONS);
      SOAPMessage message = (SOAPMessage)context.getMessage();
      OperationMapping operationMapping = context.getOperation();
      XMLStreamWriter writer = new XMLStreamWriterImpl(message.getBodyWriter());
      SOAPHeaderList soapHeaderList = message.getSOAPHeaders();
      
      // Get operation style - if it is RPC then add RPC Wrapper element
      String style = operationMapping.getProperty(OperationMapping.OPERATION_STYLE);
      if ("rpc".equals(style)) { // "rpc" style
        String operationName = operationMapping.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER);      
        String operationNamespace = operationMapping.getProperty(OperationMapping.INPUT_NAMESPACE);
        writer.writeStartElement(operationNamespace, operationName + "Response"); // i044259                    
      }
      
      ParameterMapping[] parameterMappings = operationMapping.getParameters(ParameterMapping.RETURN_TYPE);
      // add return object
      ParameterObject o;
      if (parameterMappings.length == 1) {
        o = new ParameterObject();
        o.parameterValue = returnObject;
        SDOUtil.serializeParameter(writer, o, parameterMappings[0], soapHeaderList, xmlHelper, options);
      }
      
      ParameterMapping[] params = operationMapping.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE);
      ParameterMapping p;
      for (int i = 0; i < params.length; i++) {
        p = params[i];
        if (p.getParameterType() == ParameterMapping.IN_OUT_TYPE || p.getParameterType() == ParameterMapping.OUT_TYPE) {
          o = new ParameterObject();
          Field valueField = getHolderValueField(resultParamsClasses == null ? ObjectHolder.class : resultParamsClasses[i]);
          Object realObj = valueField.get(resultParams[i]); //get 'value' data
          o.parameterValue = realObj;
          SDOUtil.serializeParameter(writer, o, p, soapHeaderList, xmlHelper, options);
        }
      }
        
      if ("rpc".equals(style)) {        
        writer.writeEndElement();
      }
      writer.flush();
    } catch (Exception ioExc) {
      throw new RuntimeProcessException(TransportBindingException.CONNECTION_IO_ERROR, ioExc);
    }
  }
  
  //i044259
  // methodClasses and loader can be null
  public static Object[] deserializeSDO(Class[] methodClasses, ClassLoader loader, ProviderContextHelper context) 
    throws RuntimeProcessException  {
    Object resultObjects[];
    try {
      OperationMapping operationMapping = context.getOperation();
      ParameterMapping[] parameterMappings = operationMapping.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE);
      SOAPMessage message = (SOAPMessage) context.getMessage();
      SOAPHeaderList soapHeaderList = message.getSOAPHeaders();
      
      SapXmlHelper xmlHelper = (SapXmlHelper) context.getSdoHelper();
      if (xmlHelper == null){
        throw new RuntimeProcessException("No Galaxy SDO helper found in context");
      }
      Map options = (Map) context.getProperty(ServiceFactoryConfig.OPTIONS);
      XMLTokenReader reader = message.getBodyReader();
      reader.passChars();
      
      /*if (reader.getState() != XMLTokenReader.STARTELEMENT || !reader.getLocalName().equals(SOAPMessage.BODYTAG_NAME)) {
        throw new TransportBindingException(TransportBindingException.NO_SOAP_BODY);
      }*/
      
      // i044259
      if (reader.getState() != XMLTokenReader.STARTELEMENT){
        throw new TransportBindingException(TransportBindingException.INVALID_READER_STATE);
      }
      
      // get parameter objects 
      ParameterMapping currentParam;

      // Enters Into the soap body (if not already there)      
      if(reader.getLocalName().equals(SOAPMessage.BODYTAG_NAME)){
        reader.next(); reader.passChars();
      }
      
      // Reader positioned into the body response node.
      XMLStreamReader sreader = new XMLStreamReaderImpl(reader);          
      String style = operationMapping.getProperty(OperationMapping.OPERATION_STYLE);
      if ("rpc".equals(style)) {
        // enters in response element its name doesn't matter          
        sreader.nextTag(); 
      }

      // deserialize
      int pType;
      ParameterObject o;
      resultObjects = new Object[parameterMappings.length];
      Class hClass;
      for (int i = 0; i < parameterMappings.length; i++) {
        currentParam = parameterMappings[i];
        o = new ParameterObject(); 
        pType = currentParam.getParameterType();
        if (pType == ParameterMapping.IN_TYPE || pType == ParameterMapping.IN_OUT_TYPE) {
          SDOUtil.deserializeParameter(sreader, o, currentParam, soapHeaderList,xmlHelper, options);
        }
        if (pType == ParameterMapping.OUT_TYPE || pType == ParameterMapping.IN_OUT_TYPE) { // holders
          try {
              Object hObject;
              if (methodClasses == null){
                hClass = ObjectHolder.class;
                hObject = new ObjectHolder();
              }else{
                hClass = methodClasses[i];
                hObject = hClass.newInstance();
              }
              if (currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) { //only for INOUT a value should be set
              Field valueField = getHolderValueField(hClass);
              if (LOC.beDebug()) {       
                LOC.debugT("deserializeSDO(): setting object: " + o + ", into hInstance: " + hObject + ", using FieldObj: " + valueField);
              }
              valueField.set(hObject, o.parameterValue); //set holder's 'value' field. Result[i] contains the deserialized object.
              
            }
            resultObjects[i] = hObject;
          } catch (Exception e) {
            throw new ProcessException(e);
          }
        } else { //this is IN_TYPE
          resultObjects[i] = o.parameterValue;
        }
      }
      
      if ("rpc".equals(style)) {
        // leaves response element his name doesn't matter
        sreader.nextTag();
      }
      while (reader.getState() != XMLTokenReader.EOF) {
        reader.next();
      }
    } catch(Exception exc) {
      if (exc instanceof SOAPFaultException) {
        throw (SOAPFaultException) exc;
      }
      if(exc instanceof IOException || exc  instanceof ParserException) {
        throw new RuntimeProcessException(TransportBindingException.CONNECTION_IO_ERROR, exc);
      } else if(!(exc instanceof RemoteException)) {
        throw new RuntimeProcessException(TransportBindingException.PARAMETER_SET_FAIL, exc);
      }
      throw new RuntimeProcessException(exc);
    }
    return resultObjects;
  }
  
  public static Object[] deserializeJEE(Class[] mParams, ClassLoader appLoader, ProviderContextHelper ctx) throws RuntimeProcessException {
    OperationMapping operation = ctx.getOperation();
    
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    XMLTokenReader orig_reader = message.getBodyReader();
    SOAPHeaderList headers = message.getSOAPHeaders();
  
    if (orig_reader.getState() != XMLTokenReader.STARTELEMENT) {
      throw new ProcessException(ExceptionConstants.READER_NOT_ON_START_ELEMENT);
    }
    
    JAXBRIContext jaxbCtx;
    AttachmentUnmarshaller attUnmarsh = new AttachmentUnmarshallerImpl(ProviderAttachmentHandlerFactory.getAttachmentHandler());
    jaxbCtx = (JAXBRIContext) ctx.getStaticContext().getJAXBContext();
    
    String jaxbBean = operation.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN);
    
    String opNS = operation.getProperty(OperationMapping.INPUT_NAMESPACE);
    String opLoc = operation.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER);

    
    ParameterMapping[] virtualParams = operation.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE);
    ArrayList<ParameterObject> tmpResult = new ArrayList<ParameterObject>();
    
    for (int i = 0; i < virtualParams.length; i++) {
      ParameterMapping currentParam = virtualParams[i];
      Class currentClass;
      if (currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE || currentParam.getParameterType() == ParameterMapping.IN_TYPE) { //only in and inout should be deserialized
        if (currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
          String realCName = currentParam.getJavaType();
          try {
            currentClass = RuntimeProcessingEnvironment.loadClass(realCName, appLoader); //load the actual class, and replace the holder one
          } catch (Exception e) {
            throw new ProcessException(e); 
          }
        } else {
          currentClass = mParams[i];
        }
        ParameterObject o = new ParameterObject(); 
        o.parameterType = currentClass;
        o.typeReference = (TypeReference)currentParam.getJaxbTypeRef();
        tmpResult.add(o);
      }
    }
    ParameterMapping[] virtualInInOutParams = operation.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE); //only IN and INOUT should be deserialized
    try {
      LOC.debugT("Calling JAXWSUtil.deserializeInboundJAXB with JaxbBean '" + jaxbBean + "'");
      JAXWSUtil.deserializeInboundJAXB(operation, tmpResult, orig_reader, message.getSOAPHeaders(), jaxbCtx, attUnmarsh, appLoader, jaxbBean, opNS, opLoc, virtualInInOutParams);
    } catch (Exception e) {
      throw new RuntimeProcessException(e);
    }
    
    int count = 0;
    Object result[] = new Object[virtualParams.length];
    for (int i = 0; i < virtualParams.length; i++) {
      ParameterMapping currentParam = virtualParams[i];
      if (currentParam.getParameterType() == ParameterMapping.OUT_TYPE || currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
        try {
          Class hClass = mParams[i];
          Object hObject = hClass.newInstance();
          if (currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) { //only for INOUT a value should be set
            Field valueField = getHolderValueField(hClass);
            if (LOC.beDebug()) {       
              LOC.debugT("deserializeJEE(): setting object: " + tmpResult.get(count) + ", into hInstance: " + hObject + ", using FieldObj: " + valueField);
            }
            valueField.set(hObject, tmpResult.get(count).parameterValue); //set holder's 'value' field. Result[i] contains the deserialized object.
            count++;
          }
          
          result[i] = hObject;
        } catch (Exception e) {
          throw new ProcessException(e);
        }
      } else {
        result[i] = tmpResult.get(count).parameterValue;
        count++;
      }
    }
    //i044259
    //check for mustUnderstand headers
    checkForMandatoryHeaders(headers, ctx);
    return result;
  }
  
  private static Object[] deserializeJEEDocumentWrappedOperation(Class[] mParams, ClassLoader appLoader, ProviderContextHelper ctx, OperationMapping operation, Unmarshaller jaxbUnmarsh, XMLStreamReader sreader) throws RuntimeProcessException {
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    SOAPHeaderList headers = message.getSOAPHeaders();

    Object jaxbBean = null;
    try {
      jaxbBean = jaxbUnmarsh.unmarshal(sreader);
    } catch (Exception e1) {
      throw new ProcessException(e1);
    }
    
    ParameterMapping[] virtualParams = operation.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE);    
    Object[] result = new Object[mParams.length];

//    QName currentElementQName = null;
    ParameterMapping currentParam;
    Class currentClass;
    Element headerElement = null;
    for (int i = 0; i < result.length; i++) {
      currentParam = virtualParams[i];
      currentClass = mParams[i];
      try {
        //in case of holder, replace currentClass(which is holder class) with actual java class
        Object hObject = null;
        if (currentParam.getParameterType() == ParameterMapping.OUT_TYPE || currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
          String realCName = currentParam.getJavaType();
          try {
            hObject = currentClass.newInstance(); //instantiate the holder 
            currentClass = RuntimeProcessingEnvironment.loadClass(realCName, appLoader); //load the actual class, and replace the holder one
          } catch (Exception e) {
            throw new ProcessException(e); 
          }
        } 
        if (currentParam.getParameterType() == ParameterMapping.IN_TYPE || currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
          if (currentParam.isHeader()) {//If header is available serialize it.
            headerElement = getHeaderElement(currentParam, headers, currentParam.getNamespace(), true, ctx);        
            //result[i] = deserializer.deserialize(headerElement, context, currentClass);
            result[i] = jaxbUnmarsh.unmarshal(headerElement, currentClass).getValue();
          } else {
            String wsdlPName = currentParam.getWSDLParameterName();
            Object paramObject = getJavaBeanPropOrPublicFieldValue(jaxbBean, wsdlPName, currentClass);
            if (paramObject == null) {
              throw new RuntimeProcessException("JAXB request bean '" +  jaxbBean + "' is missing field/property with name '" + wsdlPName + "'");
            } else if (paramObject == NILLABLE_OBJECT){ 
              result[i] = null;
            } else {
              result[i] = paramObject;
            }
          }          
        } else { //for out params, set the holder instance.
          result[i] = hObject;
        }
        //in case of IN_OUT holder, replace result[i] value with handle instance into which the real object is set 
        if (currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
          try {
            Field valueField = getHolderValueField(hObject.getClass());
            if (LOC.beDebug()) {       
              LOC.debugT("deserializeJEE(): setting object: " + result[i] + ", into hInstance: " + hObject + ", using FieldObj: " + valueField);
            }
            valueField.set(hObject, result[i]); //set holder's 'value' field. Result[i] contains the deserialized object.
            result[i] = hObject;
          } catch (Exception e) {
            throw new ProcessException(e);
          }
        } 
      } catch (Exception umE) {
        ProcessException prExc = new ProcessException(ExceptionConstants.DESERIALIZING_FAILS, new Object[]{umE.getLocalizedMessage()}, umE);
        prExc.setSOAPFaultCode(CLIENT_ERROR_CODE);
        throw prExc;
      }
    }
    //check for mustUnderstand headers
    checkForMandatoryHeaders(headers, ctx);
    return result;
  }

  /**
   * Deserializes the data for OutsideIn configuration.  
   */
  static Object[] desializeOutsideIn(Class[] mParams, ClassLoader appLoader, ProviderContextHelper ctx) throws RuntimeProcessException {    
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    XMLTokenReader reader = message.getBodyReader();
    SOAPHeaderList headers = message.getSOAPHeaders();

    if (reader.getState() != XMLTokenReader.STARTELEMENT) {
      throw new ProcessException(ExceptionConstants.READER_NOT_ON_START_ELEMENT);
    }
    //substract 1 since on startElement it is increase by the parser
    int opElemBaseXMLLevel = reader.getCurrentLevel() - 1;
    
    OperationMapping operation = ctx.getOperation(); 
    TypeMappingRegistry registry = ctx.getStaticContext().getTypeMappingRegistry();
    TypeMapping typeMapping = registry.getDefaultTypeMapping();
    SOAPDeserializationContext context = getSOAPDeserializationContext(typeMapping, appLoader);
    
    if (OperationMapping.ENCODED_USE.equals(operation.getProperty(OperationMapping.OPERATION_USE))) {
      context.setEncoded(true);
    }
      
    ParameterMapping[] virtualParams = operation.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE);    
    Object[] result = new Object[mParams.length];
    
    String wrapperElName = reader.getLocalName();
    QName currentElementQName = null;

    ParameterMapping currentParam;
    Class currentClass;
    DeserializerBase deserializer;
    DeserializerFactory deserializerFactory;
    Element headerElement = null;

    for (int i = 0; i < result.length; i++) {
      currentParam = virtualParams[i];
      currentClass = mParams[i];
      //seaching for deserializer
      QName tQName = null;
      if (currentParam.isElement()) {
        tQName = ((TypeMappingImpl) typeMapping).getTypeForElement(currentParam.getSchemaQName());
        if (tQName == null) {
          throw new ProcessException(ExceptionConstants.NO_SCHEMA_TYPE_FOR_ELEMENT, new Object[]{currentParam.getSchemaQName(), typeMapping});
        }
      } else {
        tQName = currentParam.getSchemaQName();      
      }
      //originalClass = currentParam.getJavaType();
      deserializerFactory = context.getTypeMapping().getDeserializer(currentClass, tQName);
      if (deserializerFactory == null) {
        throw new ProcessException(ExceptionConstants.NO_DESERIALIZER_FOUND, new Object[]{tQName, context.getTypeMapping()});
      }
      deserializer = (DeserializerBase) deserializerFactory.getDeserializerAs("");
      if (LOC.beDebug()) {       
        LOC.debugT("deserializeOutsideIn(): parameter[" + i + "] class:" + currentClass + ", xsdTypeQname:" + tQName + ", pMapping: " + currentParam);
      }
      try {
        //in case of holder, replace currentClass(which is holder class) with actual java class
        Object hObject = null;
        if (currentParam.getParameterType() == ParameterMapping.OUT_TYPE || currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
          String realCName = currentParam.getJavaType();
          try {
            hObject = currentClass.newInstance(); //instantiate the holder 
            currentClass = RuntimeProcessingEnvironment.loadClass(realCName, appLoader); //load the actual class, and replace the holder one
          } catch (Exception e) {
            throw new ProcessException(e); 
          }
        } 
        if (currentParam.getParameterType() == ParameterMapping.IN_TYPE || currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
          if (currentParam.isHeader()) {//If header is available serialize it.
            headerElement = getHeaderElement(currentParam, headers, currentParam.getNamespace(), true, ctx);        
            result[i] = deserializer.deserialize(headerElement, context, currentClass);
          } else {
            if (currentParam.isElement()) {//in case of element, this is document/literal operation and it should be only one param.
              currentElementQName = new QName(reader.getURI(), reader.getLocalName());
            } else {  
              //move to next part element
              currentElementQName = getNextElementQName(reader, opElemBaseXMLLevel);
              if (! currentParam.getWSDLParameterName().equals(currentElementQName.getLocalPart())) {
                ProcessException prExc = new ProcessException(ExceptionConstants.INCONSISTENT_PARAMETER_NAMES, new Object[]{currentParam.getWSDLParameterName(), currentElementQName.getLocalPart()});
                throw prExc;
              }
            }
            result[i] = deserializer.deserialize(reader, context, currentClass);            
          }          
        } else { //for out params, set the holder instance.
          result[i] = hObject;
        }
        //in case of IN_OUT holder, replace result[i] value with handle instance into which the real object is set 
        if (currentParam.getParameterType() == ParameterMapping.IN_OUT_TYPE) {
          try {
            Field valueField = getHolderValueField(hObject.getClass());
            if (LOC.beDebug()) {       
              LOC.debugT("deseializeOutsideIn(): setting object: " + result[i] + ", into hInstance: " + hObject + ", using FieldObj: " + valueField);
            }
            valueField.set(hObject, result[i]); //set holder's 'value' field. Result[i] contains the deserialized object.
            result[i] = hObject;
          } catch (Exception e) {
            throw new ProcessException(e);
          }
        } else {
        }
      } catch (java.rmi.UnmarshalException umE) {
        ProcessException prExc = new ProcessException(ExceptionConstants.DESERIALIZING_FAILS, new Object[]{umE.getLocalizedMessage()}, umE);
        prExc.setSOAPFaultCode(CLIENT_ERROR_CODE);
        throw prExc;
      }
    }

    result = deserializeRemainingElements(result, reader, context, wrapperElName, opElemBaseXMLLevel);
    //check for mustUnderstand headers
    checkForMandatoryHeaders(headers, ctx);
    //clears and returns the context in the pool
    context.clearContext();
    deserializationContextPool.rollBackInstance(context);
    return result;
  }
  
  /**
   * Converts the messageBody xml content to java Objects.
   * The reader is possitioned on the operation tag
   */
  private static final Object[] deserialize(Class[] mParams, ClassLoader appLoader, String use, ProviderContextHelper ctx) throws RuntimeProcessException {
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    XMLTokenReader reader = message.getBodyReader();
    SOAPHeaderList headers = message.getSOAPHeaders();

    if (reader.getState() != XMLTokenReader.STARTELEMENT) {
      throw new ProcessException(ExceptionConstants.READER_NOT_ON_START_ELEMENT);
    }
    //substract 1 since on startElement it is increase by the parser
    int opElemBaseXMLLevel = reader.getCurrentLevel() - 1;
    
    OperationMapping operation = ctx.getOperation(); 
    String namespace = operation.getProperty(OperationMapping.INPUT_NAMESPACE);
    
    TypeMappingRegistry registry = ctx.getStaticContext().getTypeMappingRegistry();
    TypeMapping typeMapping = getTypeMapping(use, registry);
//    JavaToQNameMappings javaToQNameMappings = getJavaToQNameMappings(javaToQnameMappingRegistry, encodinStyle);
    SOAPDeserializationContext context = getSOAPDeserializationContext(typeMapping, appLoader);

    String wrapperElName = reader.getLocalName();
    ParameterMapping[] virtualParams = operation.getParameters(ParameterMapping.IN_TYPE);
    Object[] result = new Object[mParams.length];

    QName currentElementQName = null;

    ParameterMapping currentParam;
    Class currentClass;
    DeserializerBase deserializer;
    DeserializerFactory deserializerFactory;
    QName qname;
    String originalClass;
    Element headerElement = null;

    for (int i = 0; i < result.length; i++) {
      currentParam = virtualParams[i];
      currentClass = mParams[i];
//      currentElementQName = null;

      //seaching for deserializer
      qname = currentParam.getSchemaQName();

      deserializerFactory = context.getTypeMapping().getDeserializer(currentClass, qname);
      if (deserializerFactory == null) {
        throw new ProcessException(ExceptionConstants.NO_DESERIALIZER_FOUND, new Object[]{qname, context.getTypeMapping()});
      }
      deserializer = (DeserializerBase) deserializerFactory.getDeserializerAs("");

      //takes part with name of the parameter
      if (use.equals(LITERAL_USE)) { //only in literal use the headers are send as SOAPHeaders
        headerElement = getHeaderElement(currentParam, headers, namespace, false, ctx);
      }
      if (LOC.beDebug()) {
        LOC.debugT("deserialize(): param: [" + i + "] " + currentParam + ", headerElement: " + headerElement);
      }
      try {
        //If header is available serialize it.
        if (headerElement != null) {
          result[i] = deserializer.deserialize(headerElement, context, currentClass);
          continue;
        }
        
        if (currentParam.isExposed()) {
          //read next part from the body
          if (currentElementQName == null) {// if previously getNextElementQName is not been used.
            currentElementQName = getNextElementQName(reader, opElemBaseXMLLevel);
          }
         // System.out.println("StreamEngine: current element name: " + currentElementQName + " param name: " + currentParam.getParameterName());
          if (currentParam.isOptional()) {
            //in case optional param value has arrived, deserialize through the reader
            if (currentElementQName != null && currentParam.getWSDLParameterName().equals(currentElementQName.getLocalPart())) {
              result[i] = deserializer.deserialize(reader, context, currentClass);
              currentElementQName = null; //marks that this has been used.
            } else { //there is no new value the default one will be used
              Object obj = deserializer.deserialize(currentParam.getDefaultValue(), context, currentClass);
//              if (obj == null) { //throw exception
//                throw new ProcessException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.BULDING_OBJECT_FROM_CONTENT, new Object[]{deserializer, currentClass, currentParam.getDefaultValue()});
//              }
              result[i] = obj;
            }
          } else { //it is obligatory parameter
            if (currentElementQName != null && currentParam.getWSDLParameterName().equals(currentElementQName.getLocalPart())) {
              result[i] = deserializer.deserialize(reader, context, currentClass);
              currentElementQName = null; //marks that this has been used.
            } else {
              ProcessException prExc = new ProcessException(ExceptionConstants.CANNOT_FIND_REQUIRED_PARAMETER, new Object[]{currentParam.getWSDLParameterName()});
              prExc.setSOAPFaultCode(CLIENT_ERROR_CODE);
              throw prExc;
            }
          }
        //use the default value
        } else {
          Object obj = deserializer.deserialize(currentParam.getDefaultValue(), context, currentClass);
//          if (obj == null) { //throw exception
//            throw new ProcessException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.BULDING_OBJECT_FROM_CONTENT, new Object[]{deserializer, currentClass, currentParam.getDefaultValue()});
//          }
          result[i] = obj;
        }
      } catch (java.rmi.UnmarshalException umE) {
        ProcessException prExc = new ProcessException(ExceptionConstants.DESERIALIZING_FAILS, new Object[]{umE.getLocalizedMessage()}, umE);
        prExc.setSOAPFaultCode(CLIENT_ERROR_CODE);
        throw prExc;
      }
    }

    result = deserializeRemainingElements(result, reader, context, wrapperElName, opElemBaseXMLLevel);
    //check for mustUnderstand headers
    checkForMandatoryHeaders(headers, ctx);

    //clears and returns the context in the pool
    context.clearContext();
    deserializationContextPool.rollBackInstance(context);

    return result;
  }
  
  private static void serializeOutsideInParam(Object object, Class objClass, ParameterMapping param, TypeMappingImpl typeMapping, SOAPMessage msg, SOAPSerializationContext context) throws ProcessException {
    try {
      if (param.isHeader()) { //if it is header, it should be described with element QName
        SOAPHeaderList headerList = msg.getSOAPHeaders();
        Document hDoc = headerList.getInternalDocument();      
        QName elQname = param.getSchemaQName();
        Element hElem = hDoc.createElementNS(elQname.getNamespaceURI(), elQname.getLocalPart()); //create header element
        QName tQName = typeMapping.getTypeForElement(elQname);
        if (tQName == null) {
          throw new ProcessException(ExceptionConstants.NO_SCHEMA_TYPE_FOR_ELEMENT, new Object[]{param.getSchemaQName(), typeMapping});
        }
        SerializerBase serializer = null;
        try {
          serializer = context.getSerializer(tQName, objClass);
        } catch (XmlMarshalException xmlME) {
          throw new ProcessException(ExceptionConstants.NO_SERIALIZER_FOUND, new Object[]{tQName, context.getTypeMapping()}, xmlME);
        }
        try {
          serializer.serialize(object, hElem, context);
        } catch (java.rmi.MarshalException mE) {
          throw new ProcessException(ExceptionConstants.SERIALIZING_FAILS, new Object[]{object, mE.getLocalizedMessage()}, mE);
        }
        headerList.addHeader(hElem); //add the header element to headers' list
      } else { //it should be serialized into soap body
        QName tQName = null;
        XMLTokenWriter writer = msg.getBodyWriter();
        if (param.isElement()) {
          QName elQname = param.getSchemaQName();
          tQName = typeMapping.getTypeForElement(elQname);
          if (tQName == null) {
            throw new ProcessException(ExceptionConstants.NO_SCHEMA_TYPE_FOR_ELEMENT, new Object[]{param.getSchemaQName(), typeMapping});
          }
          writer.enter(elQname.getNamespaceURI(), elQname.getLocalPart()); //create element
        } else { //parameter described with xsd type. RPC styles
          tQName = param.getSchemaQName();
          writer.enter(param.getProperty(ParameterMapping.NAMESPACE), param.getWSDLParameterName()); //the 'namespace' property is set only for document/literal style with schema's @elementFormDefaul='qualified' attribute. 
        }      
        SerializerBase serializer = null;
        try {
          serializer = context.getSerializer(tQName, objClass);
        } catch (XmlMarshalException xmlME) {
          throw new ProcessException(ExceptionConstants.NO_SERIALIZER_FOUND, new Object[]{tQName, context.getTypeMapping()}, xmlME);
        }
        serializer.serialize(object, writer, context);
      }
    } catch (IOException ioE) {
      throw new ProcessException(ioE);
    }
  }
  
  private static void serializeJEEParam(Object object, Class objClass, ParameterMapping param, SOAPHeaderList headerList, XMLStreamWriter writer, Marshaller jaxbMarsh, ClassLoader appLoader) throws ProcessException {
    try {
      if (param.isHeader()) { //if it is header, it should be described with element QName
        Document hDoc = headerList.getInternalDocument();      
        QName elQname = param.getSchemaQName();
        JAXBElement jaxbEl = new JAXBElement(elQname, objClass, object);
        Element tmpEl = hDoc.createElement("tmp");
        try {
          jaxbMarsh.marshal(jaxbEl, tmpEl);
        } catch (JAXBException jbE) {
          throw new ProcessException(ExceptionConstants.SERIALIZING_FAILS, new Object[]{object, jbE.getLocalizedMessage()}, jbE);
        }
        headerList.addHeader((Element) tmpEl.getChildNodes().item(0)); //add the header element to headers' list
      } else if (param.isAttachment()) {
        serializeAttachment(object, param);
      } else { //it should be serialized into soap body. For RPC style param.getNamespace() must return null.
        if (param.getProperty(ParameterMapping.JAXB_BEAN_CLASS) != null) {
          serializeJAXBValueType(param, writer, object, objClass, appLoader, jaxbMarsh);
        } else {
          JAXBElement jaxbEl = new JAXBElement(new QName(param.getNamespace(), param.getWSDLParameterName()), objClass, object);
          jaxbMarsh.marshal(jaxbEl, writer);
        }
      }
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }

  public static void serializeJEE(Object returnObject, Class objectClass, Object[] inputObjects, Class[] inputClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    OperationMapping operation = ctx.getOperation();
    
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    SOAPHeaderList headerList = message.getSOAPHeaders();
    ClassLoader appLoader = ((ProviderContextHelperImpl) ctx).getImplClassLoader();
    
    XMLStreamWriter writer = new XMLStreamWriterImpl(message.getBodyWriter());
    
    JAXBRIContext jaxbContext = (JAXBRIContext) ctx.getStaticContext().getJAXBContext();
    AttachmentHandler attHandler = ProviderAttachmentHandlerFactory.getAttachmentHandler();
    AttachmentMarshaller attMarsh = new AttachmentMarshallerImpl(ProviderAttachmentHandlerFactory.getAttachmentHandler());
    
    String responseBean = operation.getProperty(OperationMapping.RESPONSE_WRAPPER_BEAN);
    String opNS = operation.getProperty(OperationMapping.OUTPUT_NAMESPACE);
    String opLoc = operation.getProperty(OperationMapping.SOAP_RESPONSE_WRAPPER);

    List<ParameterMapping> pMap = new ArrayList<ParameterMapping>(); //?? never read 
    List<ParameterObject> pObj = new ArrayList<ParameterObject>();
    
    //add the response if any
    ParameterMapping ret[] = operation.getParameters(ParameterMapping.RETURN_TYPE);
    if (ret.length == 1) {
      pMap.add(ret[0]);
      ParameterObject o = new ParameterObject();
      o.parameterType = objectClass;
      o.parameterValue = returnObject;
      o.typeReference = (TypeReference)ret[0].getJaxbTypeRef();
      pObj.add(o);
    }
    
    try {
      ParameterMapping[] params = operation.getParameters(ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE);
      for (int i = 0; i < params.length; i++) {
        ParameterMapping p = params[i];
        if (p.getParameterType() == ParameterMapping.IN_OUT_TYPE || p.getParameterType() == ParameterMapping.OUT_TYPE) {
          pMap.add(p);
          ParameterObject o = new ParameterObject();
          o.parameterType = RuntimeProcessingEnvironment.loadClass(p.getJavaType(), appLoader);
          Field valueField = getHolderValueField(inputClasses[i]);
          Object realObj = valueField.get(inputObjects[i]); //get 'value' data
          o.parameterValue = realObj;
          o.typeReference = (TypeReference)p.getJaxbTypeRef();
          pObj.add(o);
        }
      }
      JAXWSUtil.serializeOutboundJAXB(operation, pObj.toArray(new ParameterObject[pObj.size()]), writer, headerList, jaxbContext, attMarsh, appLoader, responseBean, opNS, opLoc, pMap.toArray(new ParameterMapping[pMap.size()]), attHandler);
      writer.flush();
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }

  private static void serializeJEEDocumentWrappedOperation(Object returnObject, Class objectClass, Object[] inputObjects, Class[] inputClasses, ProviderContextHelper ctx, OperationMapping operation, Marshaller jaxbMarsh, XMLStreamWriter writer) throws RuntimeProcessException {
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    SOAPHeaderList headerList = message.getSOAPHeaders();
    
    try {  
      ParameterMapping curParam;
//      XMLStreamWriter writer = new XMLStreamWriterImpl((XMLTokenWriterImpl) message.getBodyWriter());
//      Marshaller jaxbMarsh = ctx.getStaticContext().getJAXBContext().createMarshaller();
//      jaxbMarsh.setAttachmentMarshaller(new AttachmentMarshallerImpl(ProviderAttachmentHandlerFactory.getAttachmentHandler()));
//      jaxbMarsh.setProperty(Marshaller.JAXB_FRAGMENT, true);
      
      String responseBean = operation.getProperty(OperationMapping.RESPONSE_WRAPPER_BEAN);
      Class responseBeanCls = RuntimeProcessingEnvironment.loadClass(responseBean, ((ProviderContextHelperImpl) ctx).getImplClassLoader());
      Object responseBeanObj = responseBeanCls.newInstance();
      //process return param      
      ParameterMapping[] returnParams = operation.getParameters(ParameterMapping.RETURN_TYPE);
      if (returnParams.length == 1) {
        curParam = returnParams[0];
        //set the return object
        if (curParam.isHeader()) {
          
        } else {
          boolean res = setJavaBeanPropertyOrPublicFiledValue(responseBeanObj, curParam.getWSDLParameterName(), objectClass, returnObject);
          if (res == false) {
            throw new RuntimeProcessException("Could not set object '" + returnObject + "' in property/field with name '" +
                curParam.getWSDLParameterName() + "' and class '" + objectClass + "' in jaxb bean '" + responseBeanObj + "'"); 
          }
        }
      }      
      //process IN_OUT and OUT params. Obtain the IN also in order to preserve the original parameter order. 
      returnParams = operation.getParameters(ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE | ParameterMapping.IN_TYPE);
      for (int i = 0; i < returnParams.length; i++) {
        curParam = returnParams[i];
        if (curParam.getParameterType() == ParameterMapping.IN_OUT_TYPE || curParam.getParameterType() == ParameterMapping.OUT_TYPE) {
          //take holder's 'value' data
          Field valueField = getHolderValueField(inputClasses[i]);
          Object realObj = valueField.get(inputObjects[i]); //get 'value' data
          String holderWrappedType = curParam.getJavaType();
          Class realObjClass = RuntimeProcessingEnvironment.loadClass(holderWrappedType, ((ProviderContextHelperImpl) ctx).getImplClassLoader());
          if (curParam.isHeader()) {
            Document hDoc = headerList.getInternalDocument();      
            QName elQname = curParam.getSchemaQName();
            JAXBElement jaxbEl = new JAXBElement(elQname, realObjClass, realObj);
            Element tmpEl = hDoc.createElement("tmp");
            try {
              jaxbMarsh.marshal(jaxbEl, tmpEl);
            } catch (JAXBException jbE) {
              throw new ProcessException(ExceptionConstants.SERIALIZING_FAILS, new Object[]{realObj, jbE.getLocalizedMessage()}, jbE);
            }
            headerList.addHeader((Element) tmpEl.getChildNodes().item(0)); //add the header element to headers' list
          } else {
            boolean res = setJavaBeanPropertyOrPublicFiledValue(responseBeanObj, curParam.getWSDLParameterName(), realObjClass, realObj);
            if (res == false) {
              throw new RuntimeProcessException("Could not set object '" + realObj + "' in property/field with name '" +
                  curParam.getWSDLParameterName() + "' and class '" + realObjClass + "' in jaxb bean '" + responseBeanObj + "'"); 
            }
          }
        }
      }
      
      //do the actual serialization
      jaxbMarsh.marshal(responseBeanObj, writer);
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }
  /**
   * Serialize RETURN, OUT and IN_OUT pameters into response msg. 
   */
  static void serializeOutsideIn(Object returnObject, Class objectClass, Object[] inputObjects, Class[] inputClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    OperationMapping operation = ctx.getOperation();
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    SOAPHeaderList headerList = message.getSOAPHeaders();
    Document hDoc = headerList.getInternalDocument();      
    TypeMappingImpl typeMapping  = (TypeMappingImpl) ctx.getStaticContext().getTypeMappingRegistry().getDefaultTypeMapping();
    SOAPSerializationContext context = getSOAPSerializationContext(typeMapping, null);

    try {  
      if (OperationMapping.ENCODED_USE.equals(operation.getProperty(OperationMapping.OPERATION_USE))) {
        context.setEncoded(true);
      }
      
      ParameterMapping curParam;
      boolean invLeave = false;
      
      XMLTokenWriter writer = message.getBodyWriter(); //the writer has writter <Body> only.
      String encStyle = operation.getProperty(OperationMapping.OUT_ENCODING_STYLE);
      if (encStyle != null) {
        writer.writeAttribute(NS.SOAPENV, ENCODINGSTYLE, encStyle);
      }
      //process return param      
      ParameterMapping[] returnParams = operation.getParameters(ParameterMapping.RETURN_TYPE);
      if (returnParams.length == 1) {
        curParam = returnParams[0];
        if (curParam.isElement()) {
            if (! curParam.isHeader()) {
              //serialize return param
              serializeOutsideInParam(returnObject, objectClass, curParam, typeMapping, message, context);
            } else { //return param is header
              //TODO Here it should be know whether the operation it RPC, or DOCUMENT.  In case of DOC, the body is empty. In case of RPC the body 
              //cotains <opName> + "Response" element
              throw new UnsupportedOperationException("OutsideIn config with one return parameter, which is element and header is not supported currently.");
//              String ns = operation.getProperty(OperationMapping.OUTPUT_NAMESPACE);
//              String opEl = operation.getWSDLOperationName() + "Response"; //the <Response> suffix is according to WSDL1.1 an BP1.1 specs.
//              writer.enter(ns, opEl); //for RPC styles
//              writer.setPrefixForNamespace(RESPONSE_ELEMENT_PREFIX, ns);
//              invLeave = true;
//              //serialize return param
//              serializeOutsideInParam(returnObject, objectClass, curParam, typeMapping, message, context);              
            }
        } else { //in case the return param is not element
          //this is RPC response. Create body wrapper element
          String ns = operation.getProperty(OperationMapping.OUTPUT_NAMESPACE);
          String opEl = operation.getProperty(OperationMapping.SOAP_RESPONSE_WRAPPER);
          writer.enter(ns, opEl); //for RPC styles
          writer.setPrefixForNamespace(RESPONSE_ELEMENT_PREFIX, ns);
          invLeave = true;       
          //serialize the return object
          serializeOutsideInParam(returnObject, objectClass, curParam, typeMapping, message, context);
        }
      } else if (returnParams.length == 0) { //this could be done
        //In case of DOC, the body is empty. In case of RPC the body
        if ("rpc".equals(operation.getProperty(OperationMapping.OPERATION_STYLE))) {
          String ns = operation.getProperty(OperationMapping.OUTPUT_NAMESPACE);
          String opEl = operation.getProperty(OperationMapping.SOAP_RESPONSE_WRAPPER);
          writer.enter(ns, opEl); //for RPC styles
          invLeave = true;      
        } else {
          //set nothing for document style, return directly. May be it is not correct in case there are OUT or IN_OUT params.
          return;          
        }
      }
      
      //process IN_OUT and OUT params.
      //take IN, OUT and IN_OUT as it was done when deserializing. In this way the order ot object[], class[] and parameterMapping[] is correct
      returnParams = operation.getParameters(ParameterMapping.IN_OUT_TYPE | ParameterMapping.OUT_TYPE | ParameterMapping.IN_TYPE);
      int curPType;
      for (int i = 0; i < returnParams.length; i++) {
        curParam = returnParams[i];
        curPType = returnParams[i].getParameterType();
        if ((curPType == ParameterMapping.IN_OUT_TYPE) || (curPType == ParameterMapping.OUT_TYPE)) {
          try {            
            //take holder's 'value' data
            Field valueField = getHolderValueField(inputClasses[i]);
            Object realObj = valueField.get(inputObjects[i]); //get 'value' data
            Class realObjClass = valueField.getType();
            serializeOutsideInParam(realObj, realObjClass, curParam, typeMapping, message, context);          
          } catch (Exception e) {
            throw new ProcessException(e);
          }
        }
      }
      if (invLeave) {
        writer.leave();
      }
    } catch (java.io.IOException ioE) {
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
    } finally {
      //rollback context
      context.clearContext();
      serializationContextPool.rollBackInstance(context);
    }
  }

  /**
   * Makes the actual serialization.
   */
  static void serialize(Object returnObject, Class objectClass, String use, String style, ProviderContextHelper ctx) throws ProcessException {    
    try {
      OperationMapping operation = ctx.getOperation();
      String namespace = operation.getProperty(OperationMapping.OUTPUT_NAMESPACE);

      boolean omitRespPartWrapper = operation.getOmitResponsePartWrapper(); //to omit setting of wrapper for the response part
      boolean isQualified = operation.useQualifiedSchemaForParams(ParameterMapping.RETURN_TYPE); //in case output element is defined in elementFormDefault=qualified schema

      SOAPMessage message = (SOAPMessage) ctx.getMessage();
      XMLTokenWriter writer = message.getBodyWriter();
      //sets encodingStyle attribute for RPC_ENC processing
      if (ENCODED_USE.equals(use)) {
        writer.writeAttribute(NS.SOAPENV, ENCODINGSTYLE, NS.SOAPENC);
      }
      
      //appends the body content wrapper element
      writer.setPrefixForNamespace(RESPONSE_ELEMENT_PREFIX, namespace);
      writer.enter(namespace, getResponseElementWrapper(operation, style));

      ParameterMapping[] returnParams = operation.getParameters(ParameterMapping.RETURN_TYPE);
      if (returnParams.length == 0) { //nothing to be returned
        writer.leave(); //leaves the body content wrapper element
        return;
      }
      
      TypeMapping typeMapping  = getTypeMapping(use, ctx.getStaticContext().getTypeMappingRegistry());
      if (returnObject != null) { //there is what to be serialized, register all the available namespaces in the writer
        XMLTokenWriterUtil.registerNamespaces(writer, ((TypeMappingImpl) typeMapping).getRegisteredNamespaces());
      }
      
      if (returnParams.length == 1) {
        ParameterMapping returnParam = returnParams[0];

        SOAPSerializationContext context = getSOAPSerializationContext(typeMapping, use);

        //appends the returnType wrapper element
        if (use.equals(LITERAL_USE)) {
          //for backwards compatibility
          if (style.equals(SOAPHTTPTransportBinding.DOCUMENT)) { //add ns or response wrpper due to the schema definition is qualified
            if (! omitRespPartWrapper) { //if NOT to omit the response wrapper enter it
              if (isQualified) {
                writer.enter(namespace, returnParam.getWSDLParameterName());
              } else {
                writer.enter(null, returnParam.getWSDLParameterName());
              }
            }
          } else { //if it is not document it is rpc do not place ns because it is not WS-I compliant
            writer.enter("", returnParam.getWSDLParameterName());
          }
        } else {
          writer.enter("", returnParam.getWSDLParameterName());
          // context.setProperty(PropertyList.USE_UNQUALIFIED, "true");
          context.setBypassNamespaces(true);
        }
        
        //seaching for serializer
        QName  qname = returnParam.getSchemaQName();
        if (returnObject != null) { //because of polymorphism
          objectClass = returnObject.getClass();
        }
        SerializerBase serializer = null;
        try {
          serializer = context.getSerializer(qname, objectClass);
        } catch (XmlMarshalException xmlME) {
          throw new ProcessException(ExceptionConstants.NO_SERIALIZER_FOUND, new Object[]{qname, context.getTypeMapping()}, xmlME);
        }
        
        //SerializerBase serializer = (SerializerBase) serializerFactory.getSerializerAs("");
        try {
          if (LOC.beDebug()) {
            LOC.debugT("serialize(): Using serializer: " + serializer + " for Object: " + returnObject + " objectClass: " + objectClass + " qname: " + qname);
          }
          serializer.serialize(returnObject, writer, context);
          if (! omitRespPartWrapper) {
            writer.leave(); //for the body content wrapper.
          }
        } catch (java.rmi.MarshalException mE) {
          throw new ProcessException(ExceptionConstants.SERIALIZING_FAILS, new Object[]{returnObject, mE.getLocalizedMessage()}, mE);
        } catch (java.io.IOException ioE) {
          throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
        } finally {
          //rollback the context instance.
          context.clearContext();
          serializationContextPool.rollBackInstance(context);
        }
      } else {
        throw new ProcessException(ExceptionConstants.INOUT_PARAMETERS_NOTSUPPORTED, new Object[]{new Integer(returnParams.length)});
      }
    } catch (java.io.IOException ioE) {
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }

  public static void serializeJEEThrowable(Throwable thr, ParameterMapping fault, String faultCode, ProviderContextHelper ctx) throws ProcessException {
    SOAPMessage message = null;
    try {
      Message tmpMsg = ctx.getMessage();
      if (tmpMsg instanceof SOAPMessage) {
        message = (SOAPMessage) tmpMsg;
      } else {
        throw new ProcessException(ExceptionConstants.UNKNOW_MESSAGE_INSTANCE, new Object[]{tmpMsg});
      }
      if (SOAPMessage.SOAP11_NS.equals(message.getSOAPVersionNS())) {
        serializeJEEThrowableSOAP11(thr, fault, faultCode, ctx);
      } else {
        serializeJEEThrowableSOAP12(thr, fault, faultCode, ctx);
      }
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }
  
  private static void serializeJEEThrowableSOAP12(Throwable thr, ParameterMapping fault, String faultCode, ProviderContextHelper ctx) throws ProcessException {
    try {
      SOAPMessage message = (SOAPMessage) ctx.getMessage();
      
      javax.xml.ws.soap.SOAPFaultException sfExc = null;
      if (thr instanceof javax.xml.ws.soap.SOAPFaultException) {
        sfExc = (javax.xml.ws.soap.SOAPFaultException) thr;
      } else {
        Throwable t = thr.getCause();
        if (t != null) {
          if (t instanceof javax.xml.ws.soap.SOAPFaultException) {
            sfExc = (javax.xml.ws.soap.SOAPFaultException) t;
          }
        }
      }
     
      if (sfExc != null && sfExc.getFault() == null) {
        sfExc = null; //when there is no fault the exception is useless
      }
      
      XMLTokenWriter writer = (XMLTokenWriter) message.getBodyWriter();
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.FAULTTAG_NAME);
      //Code
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.CODETAG_NAME);
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.VALUETAG_NAME);
      String code = SOAPMessage.SOAPENV_PREFIX + ":" + SOAPMessage.SOAP12_RECEIVER_F_CODE;
      if ((thr instanceof ProcessException) && (((ProcessException) thr).getSOAPFaultCode() != null)) {
        code = SOAPMessage.SOAPENV_PREFIX + ":" + ((ProcessException) thr).getSOAPFaultCode();
      } else if (faultCode != null) {
        code = SOAPMessage.SOAPENV_PREFIX + ":" + faultCode;
      }
      writer.writeContent(code);
      writer.leave(); //Value;
      
      if (sfExc != null && sfExc.getFault().getFaultCodeAsName() != null) { //add subcode only for SOAPFaultException
        String lName = sfExc.getFault().getFaultCodeAsName().getLocalName();
        String ns = sfExc.getFault().getFaultCodeAsName().getURI();
        String pref = sfExc.getFault().getFaultCodeAsName().getPrefix();
        if (pref == null) {
          pref = "fcp";
        }
        writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.SUBCODETAG_NAME);
        writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.VALUETAG_NAME);
        writer.setPrefixForNamespace(pref, ns);
        writer.writeContent(pref + ":" + lName);
        writer.leave(); //Value
        writer.leave(); //subcode
      }
      writer.leave();//Code
      
      //Reason
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.REASONTAG_NAME);
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.TEXTTAG_NAME);
      if (sfExc != null && sfExc.getFault().getFaultString() != null) {
        writer.writeContent(sfExc.getFault().getFaultString());
      } else {
        String s = thr.getMessage();
        if (s == null) {
          s = thr.toString();
        }
        writer.writeContent(s);
      }
      writer.leave(); //Text
      writer.leave(); //Reason
      
      //Role
      if (sfExc != null && sfExc.getFault().getFaultActor() != null) {
        writer.enter(null, SOAPMessage.ROLETAG_NAME);
        writer.writeContent(sfExc.getFault().getFaultActor());
        writer.leave(); //Role
      }
      //detail
      serializeJEEEndpointThrowableIntoDetail(fault, sfExc != null?sfExc:thr, SOAPMessage.SOAP12_NS, writer, ctx);
      writer.leave(); //Fault
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }
  
  private static void serializeJEEThrowableSOAP11(Throwable thr, ParameterMapping fault, String faultCode, ProviderContextHelper ctx) throws ProcessException {
    try {
      SOAPMessage message = (SOAPMessage) ctx.getMessage();
      
      javax.xml.ws.soap.SOAPFaultException sfExc = null;
      if (thr instanceof javax.xml.ws.soap.SOAPFaultException) {
        sfExc = (javax.xml.ws.soap.SOAPFaultException) thr;
      } else {
        Throwable t = thr.getCause();
        if (t != null) {
          if (t instanceof javax.xml.ws.soap.SOAPFaultException) {
            sfExc = (javax.xml.ws.soap.SOAPFaultException) t;
          }
        }
      }
     
      if (sfExc != null && sfExc.getFault() == null) {
        sfExc = null; //when there is no fault the exception is useless
      }
      
      XMLTokenWriter writer = message.getBodyWriter();
      writer.enter(SOAPMessage.SOAP11_NS, SOAPMessage.FAULTTAG_NAME);
      //faultcode
      writer.enter(null, SOAPMessage.FAULTCODETAG_NAME);
      if (sfExc != null && sfExc.getFault().getFaultCodeAsName() != null) {
        String lName = sfExc.getFault().getFaultCodeAsName().getLocalName();
        String ns = sfExc.getFault().getFaultCodeAsName().getURI();
        String pref = sfExc.getFault().getFaultCodeAsName().getPrefix();
        if (pref == null) {
          pref = "fcp";
        }
        writer.setPrefixForNamespace(pref, ns);
        writer.writeContent(pref + ":" + lName);
        writer.leave(); //faultcode
      } else {
        String code = SOAPMessage.SOAPENV_PREFIX + ":" + SOAPMessage.SOAP11_SERVER_F_CODE;
        if ((thr instanceof ProcessException) && (((ProcessException) thr).getSOAPFaultCode() != null)) {
          code = SOAPMessage.SOAPENV_PREFIX + ":" + ((ProcessException) thr).getSOAPFaultCode();
        } else if (faultCode != null) {
          code = SOAPMessage.SOAPENV_PREFIX + ":" + faultCode;
        }
        writer.writeContent(code);
        writer.leave();
      }
      //faultstring
      writer.enter(null, SOAPMessage.FAULTSTRINGTAG_NAME);
      if (sfExc != null && sfExc.getFault().getFaultString() != null) {
        writer.writeContent(sfExc.getFault().getFaultString());
      } else {
        String s = thr.getMessage();
        if (s == null) {
          s = thr.toString();
        }
        writer.writeContent(s);
      }
      writer.leave(); //faultstring
      //faultactor
      if (sfExc != null && sfExc.getFault().getFaultActor() != null) {
        writer.enter(null, SOAPMessage.ACTORTAG_NAME);
        writer.writeContent(sfExc.getFault().getFaultActor());
        writer.leave();
      }
      //detail
      serializeJEEEndpointThrowableIntoDetail(fault, sfExc != null?sfExc:thr, SOAPMessage.SOAP11_NS, writer, ctx);
      writer.leave(); //Fault
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }
  
  /**
   * Gets the JAXB info object from JAXWS fault.
   * @param thr
   * @param jaxBClassName
   * @return
   */
  private static Object getFaultInfoFromException(Throwable thr,String jaxBClassName) {
    LOC.debugT("getFaultInfoFromException(): checking for getFaultInfo method '" + thr.getClass().getName() + "'");
    Class exceptionClass = thr.getClass();
    Method getFaultInfoMethod;
    try {
      getFaultInfoMethod = exceptionClass.getMethod("getFaultInfo",null);
      Class returnType = getFaultInfoMethod.getReturnType();
      if (!returnType.getName().equals(jaxBClassName)) {
        LOC.debugT("getFaultInfoFromException():  Method getFaultInfo returns '" + returnType.getName() + "'");
        LOC.debugT("getFaultInfoFromException():  The mapping file requires '" + jaxBClassName + "'");
        LOC.debugT("getFaultInfoFromException():  getFaultInfo not invoked  '" + jaxBClassName + "'");
      }
      if (getFaultInfoMethod != null) {
        Object result = getFaultInfoMethod.invoke(thr,null);
        return result;
      }     
    } catch (SecurityException e1) {
      LOC.catching(e1);
    } catch (NoSuchMethodException e1) {
      LOC.catching(e1);
    } catch (IllegalArgumentException e) {
      LOC.catching(e);
    } catch (IllegalAccessException e) {
      LOC.catching(e);
    } catch (InvocationTargetException e) {
      LOC.catching(e);
    }  
    LOC.debugT("getFaultInfoFromException(): getFaultInfo method could not be called" + thr.getClass().getName() + "'");
    return null;
  }
  
  /**
   * Serializes <code>thr</code> into the writer. The method will add correct <detail> element.
   * @param writer
   * @param ctx
   */
  private static void serializeJEEEndpointThrowableIntoDetail(ParameterMapping fault, Throwable thr, String soapNS, XMLTokenWriter writer, ProviderContextHelper ctx) throws Exception {
    javax.xml.ws.soap.SOAPFaultException sfExc = null;
    if (thr instanceof javax.xml.ws.soap.SOAPFaultException) {
      sfExc = (javax.xml.ws.soap.SOAPFaultException) thr;
    }
    if (sfExc != null) { //this is SOAPFaultException
      Node n = sfExc.getFault().getDetail();
      XMLTokenWriterUtil.outputDomToWriter(writer, n);
      return;
    }
    if (fault != null) { //this is method declared exception
      //load the Jaxb bean class
      String jaxbClassName = fault.getProperty(ParameterMapping.JAXB_BEAN_CLASS);
      Object jaxbBean = getFaultInfoFromException(thr,jaxbClassName);
      if (jaxbBean == null) {
        ClassLoader loader = (ClassLoader) ctx.getDynamicContext().getProperty(ProviderContextHelperImpl.IMPL_LOADER);
        LOC.debugT("serializeJEEThrowable(): load jaxbClass '" + jaxbClassName + "'");
        Class<?> jaxbClass = loader.loadClass(jaxbClassName);
        jaxbBean = jaxbClass.newInstance();
        //initialize jaxbBean
        moveDataIntoJaxbBean(jaxbBean, thr);
      }
      
      if (SOAPMessage.SOAP11_NS.equals(soapNS)) {
        writer.enter(null, SOAPMessage.SOAP11_DETAILTAG_NAME);
      } else { //this should be soap12
        writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.SOAP12_DETAILTAG_NAME);
      }
      XMLStreamWriter streamWriter = new XMLStreamWriterImpl(writer);
      JAXBContext jaxb = ctx.getStaticContext().getJAXBContext();
      Marshaller jaxbM = jaxb.createMarshaller();
      JAXBElement jaxbEl = new JAXBElement(fault.getFaultElementQName(), jaxbBean.getClass(), jaxbBean);
      jaxbM.setProperty(Marshaller.JAXB_FRAGMENT, true);
      jaxbM.marshal(jaxbEl, streamWriter);
      writer.leave(); //the detail
    } else {
      String s = thr.getMessage();
      if (s == null) {
        s = thr.toString();
      }
      if (SOAPMessage.SOAP11_NS.equals(soapNS)) {
        writer.enter(null, SOAPMessage.SOAP11_DETAILTAG_NAME);
      } else { //this should be soap12
        writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.SOAP12_DETAILTAG_NAME);
      }
      writer.enter(CLIENT_ERROR_NAMESPACE, thr.getClass().getName());
      writer.writeComment(s);
      writer.leave();
      writer.leave();//the detail
    }
  }
  /**
   * By using reflection all the data accessible through 'get' methods of <code>normalObject</code> 
   * is tried to be set inside <code>jaxbBean</code> via similar 'set' methods.
   * The public fields data is also transferred. 
   * @param jaxbBean the object into which the data is to be poured.
   * @param normalObject the object from which the data is to read.
   */
  private static void moveDataIntoJaxbBean(Object jaxbBean, Object normalObject) throws Exception {
    PropertyDescriptor[] jaxb_props = Introspector.getBeanInfo(jaxbBean.getClass()).getPropertyDescriptors();
    PropertyDescriptor[] norm_props = Introspector.getBeanInfo(normalObject.getClass()).getPropertyDescriptors();
    
    //for each 'set' inside the jaxb bean search a corresponding 'get' in the normal object
    for (int i = 0; i < jaxb_props.length; i++) {
      PropertyDescriptor jaxb_prop = jaxb_props[i];
      Method setMethod = jaxb_prop.getWriteMethod(); 
      if (setMethod != null) {
        for (int j = 0; j < norm_props.length; j++) {
          PropertyDescriptor norm_prop = norm_props[j];
          if (norm_prop.getName().equals(jaxb_prop.getName())) {
            Method getMethod = norm_prop.getReadMethod();
            if ((getMethod != null && setMethod != null) 
                && norm_prop.getPropertyType() == jaxb_prop.getPropertyType()) {
              //copy the data
              Object o = getMethod.invoke(normalObject, (Object[]) null);
              setMethod.invoke(jaxbBean, o);
            }
          }
        }
      }
    }
    //for each public field in jaxb bean search for a corresponding one in the normal objec
    Field[] jaxb_fs = jaxbBean.getClass().getFields();
    Field[] normal_fs = normalObject.getClass().getFields();
    for (Field jaxbf : jaxb_fs) {
      int mdfs = jaxbf.getModifiers();
      if (Modifier.isPublic(mdfs) && !Modifier.isStatic(mdfs)) {
        for (Field normf : normal_fs) {
          mdfs = normf.getModifiers();
          if (Modifier.isPublic(mdfs) && !Modifier.isStatic(mdfs)) {
            if (normf.getName().equals(jaxbf.getName()) && normf.getType() == jaxbf.getType()) {
              //copy the data
              Object o = normf.get(normalObject);
              jaxbf.set(jaxbBean, o);
            }
          }
        }
      }
    }
  }
  
  private static final Object NILLABLE_OBJECT = new Object();

  /**
   * Tries to obtains the content of JavaBean property or public field with name <code>propName</code>
   * type <code>propType</code> of object <code>o</code>.
   * @param o
   * @param propName
   * @param propType
   * @return the field/property data. If the data is null NILLABLE_OBJECT constant is return. If field/property with the 
   *         corresponding name and type is not found null is returned.
   * @throws Exception
   */
  private static Object getJavaBeanPropOrPublicFieldValue(Object o, String propName, Class propType) throws Exception {
    PropertyDescriptor[] jaxb_props = Introspector.getBeanInfo(o.getClass()).getPropertyDescriptors();
    
    for (int i = 0; i < jaxb_props.length; i++) {
      PropertyDescriptor jaxb_prop = jaxb_props[i];
      Method getMethod = jaxb_prop.getReadMethod(); 
      //by vladimir: had to switch to .equalsIgnoreCase, because one cts test had properties with names "name" and "Address", and the later one was not found
      if (getMethod != null && jaxb_prop.getName().equalsIgnoreCase(propName) && propType == jaxb_prop.getPropertyType()) {
        Object res = getMethod.invoke(o, (Object[]) null);
        if (res == null) {
          return NILLABLE_OBJECT;
        }
        return res;
      }
    }
    
    Field[] jaxb_fs = o.getClass().getFields();
    for (Field jaxbf : jaxb_fs) {
      int mdfs = jaxbf.getModifiers();
      if (Modifier.isPublic(mdfs) && !Modifier.isStatic(mdfs)) {
        if (jaxbf.getName().equals(propName) && propType == jaxbf.getType()) {
          Object res = jaxbf.get(o);
          if (res == null) {
            return NILLABLE_OBJECT;
          }
          return res;
        }
      }
    }
    return null;
  }
  /**
   * Sets <code>value</code> into property/field with named <code>propName</code> and type <code>propoType</code> of <code>o</code> 
   * object. 
   * @return true if field/property with the corresponding name and type is found, false otherwise. 
   * @throws Exception
   */
  private static boolean setJavaBeanPropertyOrPublicFiledValue(Object o, String propName, Class propType, Object value) throws Exception {
    PropertyDescriptor[] jaxb_props = Introspector.getBeanInfo(o.getClass()).getPropertyDescriptors();
    
    for (int i = 0; i < jaxb_props.length; i++) {
      PropertyDescriptor jaxb_prop = jaxb_props[i];
      Method setMethod = jaxb_prop.getWriteMethod(); 
      if (setMethod != null && jaxb_prop.getName().equalsIgnoreCase(propName) && propType == jaxb_prop.getPropertyType()) {
        setMethod.invoke(o, new Object[]{value});
        return true;
      }
    }
    
    Field[] jaxb_fs = o.getClass().getFields();
    for (Field jaxbf : jaxb_fs) {
      int mdfs = jaxbf.getModifiers();
      if (Modifier.isPublic(mdfs) && !Modifier.isStatic(mdfs)) {
        if (jaxbf.getName().equals(propName) && propType == jaxbf.getType()) {
          jaxbf.set(o, value);
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   *
   */
  private static void serializeThrowable(Throwable thr, ParameterMapping fault, String faultCode, String use, ProviderContextHelper ctx) throws ProcessException {
    try {
      ISoapFaultException iSoapExc = null;
      SOAPMessage message = null;
      Message tmpMsg = ctx.getMessage();
      
      //this case is added to handle cases where business logic throws this exceptions      
      if (thr instanceof javax.xml.ws.soap.SOAPFaultException) { 
        serializeJEEThrowable(thr,null, null, ctx);
        return;
      }
//      if (tmpMsg instanceof InternalMIMEMessage) {
//        message = ((InternalMIMEMessage) tmpMsg).getSOAPMessage();        
//      } else 
      if (tmpMsg instanceof SOAPMessage) {
        message = (SOAPMessage) tmpMsg;
      } else {
        throw new ProcessException(ExceptionConstants.UNKNOW_MESSAGE_INSTANCE, new Object[]{tmpMsg});
      }
      
      if (SOAPMessage.SOAP12_NS.equals(message.getSOAPVersionNS())) { //about to serialize into SOAP1.2 format
        serializeThrowableSOAP12(thr, fault, faultCode, use, message, ctx);        
        return;
      }
      XMLTokenWriter writer = message.getBodyWriter();

      writer.enter(NS.SOAPENV, "Fault");
      writer.enter(null, "faultcode");
      //in case of setted soap fault code use it
      if ((thr instanceof ProcessException) && (((ProcessException) thr).getSOAPFaultCode() != null)) {
        writer.writeContent(SOAPMessage.SOAPENV_PREFIX + ":" + ((ProcessException) thr).getSOAPFaultCode());
      } else if (thr instanceof ISoapFaultException) {
        iSoapExc = (ISoapFaultException) thr;
        String fcValue;
        if (iSoapExc instanceof ISoap12FaultException) {//for SOAP12 faults use the Subcode value for <faultstring>
          fcValue = ((ISoap12FaultException) iSoapExc)._getSubCode();           
        } else {
          fcValue = iSoapExc._getFaultCode();          
        }
        String ns;
        if (iSoapExc instanceof ISoap12FaultException) {//for SOAP12 faults use the Subcode value for <faultstring>
          ns = ((ISoap12FaultException) iSoapExc)._getSubCodeNS();           
        } else {
          ns = iSoapExc._getFaultCodeNS();          
        }
        
        if (fcValue != null && fcValue.length() > 0) {
          if (ns == null || ns.length() == 0) {
            throw new ProcessException( ExceptionConstants.INCORRECT_FAULTCODE_NS_VALUE, new Object[]{ns});
          }
          String prf;
          int pos = fcValue.indexOf(':');
          if (pos != -1) {
            prf = fcValue.substring(0, pos);
            writer.setPrefixForNamespace(prf, ns);
            fcValue = fcValue.substring(pos + 1);
          } else {
            prf = writer.getPrefixForNamespace(ns);
            if (prf == null) { //no prefix for namespace
              prf = "dPrf";
              writer.setPrefixForNamespace(prf, ns);
            }
          }
          writer.writeContent(prf + ":" +  fcValue);
        } else {
          writer.writeContent(SOAPMessage.SOAPENV_PREFIX + ":" + faultCode);
        }
      } else {
        writer.writeContent(SOAPMessage.SOAPENV_PREFIX + ":" + faultCode);
      }
      writer.leave(); //faultcode

      String errMessage = null;
      if (iSoapExc != null) {
        errMessage = iSoapExc._getFaultString();
      }
      if (errMessage == null) {
        errMessage = thr.getLocalizedMessage();
      }
      if (errMessage == null) {
        errMessage = thr.getClass().getName();
      }

      //faultstring setting
      writer.enter(null, "faultstring");
      if (iSoapExc != null && iSoapExc._getFaultStringLang() != null) {
        writer.writeAttribute(NS.XML, "lang", iSoapExc._getFaultStringLang());
      }
      writer.writeContent(errMessage);
      writer.leave(); //faultstring

      //faultactor setting
      if (iSoapExc != null && iSoapExc._getFaultActor() != null) {
        writer.enter(null, "faultactor");
        writer.writeContent(iSoapExc._getFaultActor());
        writer.leave();
      }

      writer.enter(null, "detail");

      if (fault != null) { //Declared client fault
        String namespace = fault.getFaultElementQName().getNamespaceURI();
        String faultElementName = fault.getFaultElementQName().getLocalPart();

//        if (faultElementName != null) {
          writer.enter(namespace, faultElementName); //Use the faultElementName from the configuration
//        } else {
//          writer.enter(namespace, operationName + "_" + fault.getFaultName()); //The string is so in the wsdl
//        }
        //supported for backwards compatibility
        if (ENCODED_USE.equals(use)) {
          writer.writeXmlAttribute("encodingStyle", NS.SOAPENC);
        }

        TypeMapping typeMapping  = getTypeMapping(use, ctx.getStaticContext().getTypeMappingRegistry());
        SOAPSerializationContext context = getSOAPSerializationContext(typeMapping, use);
        //mark not to serialize with ns due to .NET requirements
        if (! use.equals(LITERAL_USE)) {
          //context.setProperty(PropertyList.USE_UNQUALIFIED, "true");
          context.setBypassNamespaces(true);
        }

        //seaching for serializer
        String wsdlClass = fault.getJavaType();
        QName  qname = fault.getSchemaQName();
        SerializerBase serializer = null;
        try {
          serializer = context.getSerializer(qname, thr.getClass());
        } catch (XmlMarshalException xmlME) {
          throw new ProcessException(ExceptionConstants.NO_SERIALIZER_FOUND, new Object[]{qname, context.getTypeMapping()}, xmlME);
        }

        try {
          serializer.serialize(thr, writer, context);
        } catch (java.rmi.MarshalException mE) {
          throw new ProcessException(ExceptionConstants.SERIALIZING_FAILS, new Object[]{thr.getClass(), mE.getLocalizedMessage()}, mE);
        } catch (java.io.IOException ioE) {
          throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
        } finally {
          //rollback the context instance.
          context.clearContext();
          serializationContextPool.rollBackInstance(context);
        }

      } else if (faultCode.equals(CLIENT_ERROR_CODE)) {
        writer.enter(CLIENT_ERROR_NAMESPACE, thr.getClass().getName());
        writer.writeContent(errMessage);
        writer.leave();
      } else {
        writer.enter(SERVER_ERROR_NAMESPACE, thr.getClass().getName());
        writer.writeContent(errMessage);
        writer.leave();
      }

      writer.leave();//detail
      writer.leave();//fault
    } catch (java.io.IOException ioE) {
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }

  /**
   * Check for headers with attibute mustUnderstand=1. If found such header
   * an exception is thrown
   * @param messHeaders
   * @throws ProcessException
   */
  public static void checkForMandatoryHeaders(SOAPHeaderList headers, ProviderContextHelper ctx) throws ProcessException {
    Element tmpEl;
    String headerValue;
    Element[] headerEls = headers.getHeaders();
    ProviderContextHelperImpl pCtx = (ProviderContextHelperImpl) ctx;
    for (int j = 0; j < headerEls.length; j++) {
      tmpEl = (Element) headerEls[j];
      headerValue = tmpEl.getAttributeNS(NS.SOAPENV, SOAP_MUSTUNDERSTAND_ATTRIBUTE);
      if (headerValue.equals(Integer.toString(1))) { //found header with mustUnderstand set to 1
        QName hQName = new QName(tmpEl.getNamespaceURI(), tmpEl.getLocalName());
        if (! pCtx.getUnderstoodSOAPHeadersSet().contains(hQName)) { //the header is not understood, so throw exception
          ProcessException prExc = new ProcessException(ExceptionConstants.MUSTUNDERSTAND_HEADER_FAULT_MESSAGE, new Object[]{tmpEl.getNamespaceURI(), tmpEl.getLocalName()});
          SOAPHTTPTransportBinding.setSOAPFaultResponseCode(ctx, SOAPMessage.MUSTUNDERSTAND_F_CODE);
          throw prExc;
        }
      }
    }
  }

  private static Object[] deserializeRemainingElements(Object source[], XMLTokenReader reader, SOAPDeserializationContext content, String operationName, int xmlLevel) throws ProcessException {
//      System.out.println("STreamEngine deserializeRemainingElements");
    try {
      //in case the reader is not on the end operation tag move to it
      if (! ((reader.getState() == XMLTokenReader.ENDELEMENT) && (reader.getCurrentLevel() == xmlLevel))) {
        while (true) {
          int code = reader.next();
          if ((code == XMLTokenReader.ENDELEMENT) && reader.getCurrentLevel() == xmlLevel) {
            break;
          }
          if (code == XMLTokenReader.EOF) {
            ProcessException prExc = new ProcessException(ExceptionConstants.EOF_END_OPERATION_TAG, new Object[]{operationName});
            prExc.setSOAPFaultCode(CLIENT_ERROR_CODE);
            throw prExc;
          }
        }
      }
      reader.next(); //position on the next entry after end operation tag
    } catch (ParserException pE) {
      throw new ProcessException(ExceptionConstants.PARSER_EXCEPTION_IN_REQUEST_PARSING, new Object[]{pE.getLocalizedMessage()}, pE);
    }

    try {
      content.deserializeRemainingElements(reader);
    } catch (UnmarshalException umE) {
      ProcessException prExc = new ProcessException(ExceptionConstants.DESERIALIZING_REFERENCE_FAILS, new Object[]{umE.getLocalizedMessage()}, umE);
      prExc.setSOAPFaultCode(CLIENT_ERROR_CODE);
      throw prExc;
    }

    SOAPDeserializationState instanceWrapper;
    Object instanceWrapperObject;

    for (int i = 0; i < source.length; i++) {
      if (source[i] instanceof SOAPDeserializationState) {
        instanceWrapper = (SOAPDeserializationState) source[i];
        if (instanceWrapper.isComplete()) {
          instanceWrapperObject = instanceWrapper.getInstance();
//          System.out.println("StreamEngine instance: " + instanceWrapperObject);
          source[i] = instanceWrapperObject;
        } else {
          ProcessException prExc = new ProcessException(ExceptionConstants.UNRESOLVED_REFERENCE, new Object[]{instanceWrapper.getResultClass()});
          prExc.setSOAPFaultCode(CLIENT_ERROR_CODE);
          throw prExc;
        }
      }
    }

//    System.out.println("STreamEngine deserializeRemainingElements end");
    return source;
  }

  private static TypeMapping getTypeMapping(String useValue, TypeMappingRegistry typeMappingRegistry) throws ProcessException {
    if (LITERAL_USE.equals(useValue)) { //literal use
      return typeMappingRegistry.getDefaultTypeMapping();
    } else {  //encoded use
      TypeMapping typeMapping = typeMappingRegistry.getTypeMapping(NS.SOAPENC);//use directly constant (backwards compatibility)
      if (typeMapping == null) {
        throw new ProcessException(ExceptionConstants.ENCODINGSTYLE_NOTSUPPORTED, new Object[]{NS.SOAPENC});
      }
      return typeMapping;
    }
  }

  private static SOAPDeserializationContext getSOAPDeserializationContext(TypeMapping typeMapping, ClassLoader appLoader) {
    SOAPDeserializationContext context = (SOAPDeserializationContext) deserializationContextPool.getInstance();

    if (context == null) {
      context = new SOAPDeserializationContext();
    }
    context.setTypeMapping(typeMapping);
    context.setApplicationClassLoader(appLoader);
    context.setProperty(SOAPDeserializationContext.TOLERANT_DESERIALIZATION, "true");
    return context;
  }

  private static SOAPSerializationContext getSOAPSerializationContext(TypeMapping typeMapping, String use) {
    SOAPSerializationContext context = (SOAPSerializationContext) serializationContextPool.getInstance();

    if (context == null) {
      context = new SOAPSerializationContext();
    }
    context.setTypeMapping(typeMapping);

    if (ENCODED_USE.equals(use)) {
      context.setProperty(PropertyList.USE_ENCODING, "true");
    }

    return context;
  }
   
  //xmlLevel is the baseLevel of operation element 
  private static QName getNextElementQName(XMLTokenReader reader,int xmlLevel) throws ProcessException {
    try {
      //in case there are no required params in the operation
      if (reader.getState() == XMLTokenReader.ENDELEMENT) {
        //if (reader.getLocalNameCharArray().equals(operationName) && reader.getURICharArray().equals(operationURI)) {
        if (reader.getCurrentLevel() == xmlLevel) {
          return null;
        }
      }
      while (reader.next() != XMLTokenReader.STARTELEMENT) {
        //in case operation end element is reached
        if (reader.getState() == XMLTokenReader.ENDELEMENT) {
          //if (reader.getLocalNameCharArray().equals(operationName) && reader.getURICharArray().equals(operationURI)) {          
		  if (reader.getCurrentLevel() == xmlLevel) {
            return null;
          }
        }
        if (reader.getState() == XMLTokenReader.EOF) {
          throw new ProcessException(ExceptionConstants.EOF_NEXT_ELEMENT_START);
        }
      }
      return new QName(reader.getURI(), reader.getLocalName());
    } catch (ParserException pE) {
      throw new ProcessException(ExceptionConstants.PARSER_EXCEPTION_IN_REQUEST_PARSING, new Object[]{pE.getLocalizedMessage()}, pE);
    }
  }

  static Element getHeaderElement(ParameterMapping param, SOAPHeaderList headers, String inputNS, boolean outsideIn, ProviderContextHelper ctx) throws ProcessException {
    // in case is is not header element or it is not exposed
    if ((! param.isHeader()) || (! param.isExposed())) {
      return null;
    }

    String parameterName = null;
    String ns = null;
    if (outsideIn) { //this is in case of OutsideIn description 
      parameterName = param.getSchemaQName().getLocalPart();
      ns = param.getSchemaQName().getNamespaceURI();
    } else { //this is in case of InsideOut description
      parameterName = param.getWSDLParameterName();
      ns = param.getNamespace();
      if (ns == null) {
        ns = inputNS;
      }      
    }
    
    Element tmpEl;
    String elNS;
    Element headerEls[] = headers.getHeaders();
    for (int j = 0; j < headerEls.length; j++) {
      tmpEl = headerEls[j];
      elNS = tmpEl.getNamespaceURI();

      if (elNS == null) {
        ProcessException prExc = new ProcessException(ExceptionConstants.NOT_QUALIFIED_SOAPHEADER_ENTRY, new Object[]{tmpEl});
        prExc.setSOAPFaultCode(CLIENT_ERROR_CODE);
        throw prExc;
      }

      if (tmpEl.getLocalName().equals(parameterName) && elNS.equals(ns)) {
        QName hQName = new QName(elNS, tmpEl.getLocalName());
        Element hElem = (Element) headers.getHeader(hQName);
        ctx.markSOAPHeaderAsUnderstood(hQName);
        return hElem;
      }
    }

    if (param.isOptional()) {
      return null;
    } else {
      //in case no Element in the header is found
      ProcessException processException = new ProcessException(ExceptionConstants.CANNOT_FIND_SOAPHEADER_ELEMENT, new Object[]{parameterName, ns});
      processException.setSOAPFaultCode(CLIENT_ERROR_CODE);
      throw processException;
    }
  }
  
  static Field getHolderValueField(Class hClass) throws ProcessException {
    try {
      return hClass.getField("value"); //according to the spec, the field name should be 'value'
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  static String getResponseElementWrapper(OperationMapping operation, String style) {
    if (style != null && style.equals(SOAPHTTPTransportBinding.RPC)) {
      return operation.getWSDLOperationName() + OperationMapping.OPERATION_RESPONSE_SUFFIX;
    } else {
      return operation.getProperty(OperationMapping.SOAP_RESPONSE_WRAPPER);
    } 
  } 

  private static void serializeThrowableSOAP12(Throwable thr, ParameterMapping fault, String faultCode, String use, SOAPMessage message, ProviderContextHelper ctx) throws ProcessException {
    try {
      //convert 'server' to 'Receiver' and 'client' to 'Sender'
      if (SERVER_ERROR_CODE.equals(faultCode)) {
        faultCode = SOAPMessage.SOAP12_RECEIVER_F_CODE;
      } else if (CLIENT_ERROR_CODE.equals(faultCode)) {
        faultCode = SOAPMessage.SOAP12_SENDER_F_CODE;
      }
      ISoapFaultException iSoapExc = null;

      XMLTokenWriter writer = message.getBodyWriter();
  
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.FAULTTAG_NAME);
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.CODETAG_NAME);
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.VALUETAG_NAME);
      //in case of setted soap code value use it
      if ((thr instanceof ProcessException) && (((ProcessException) thr).getSOAPFaultCode() != null)) {
        writer.writeContent(SOAPMessage.SOAPENV_PREFIX + ":" + ((ProcessException) thr).getSOAPFaultCode());
      } else if (thr instanceof ISoapFaultException) {
        iSoapExc = (ISoapFaultException) thr;
        String fcValue = iSoapExc._getFaultCode(); //for now the fault code returned from this method will be used...
        if (fcValue != null && fcValue.length() > 0) {
          String ns = iSoapExc._getFaultCodeNS();
          if (ns == null || ns.length() == 0) {
            throw new ProcessException( ExceptionConstants.INCORRECT_FAULTCODE_NS_VALUE, new Object[]{ns});
          }
          String prf;
          int pos = fcValue.indexOf(':');
          if (pos != -1) {
            prf = fcValue.substring(0, pos);
            writer.setPrefixForNamespace(prf, ns);
            fcValue = fcValue.substring(pos + 1);
          } else {
            prf = writer.getPrefixForNamespace(ns);
            if (prf == null) { //no prefix for namespace
              prf = "dPrf";
              writer.setPrefixForNamespace(prf, ns);
            }
          }
          writer.writeContent(prf + ":" +  fcValue);
        } else {
          writer.writeContent(SOAPMessage.SOAPENV_PREFIX + ":" + faultCode);
        }
      } else {
        writer.writeContent(SOAPMessage.SOAPENV_PREFIX + ":" + faultCode);
      }
      writer.leave(); //fault value
      
      ISoap12FaultException soap12F = null;
      if (thr instanceof ISoap12FaultException) {
        //append subcode if any
        soap12F = (ISoap12FaultException) thr;
        String subCode = soap12F._getSubCode();
        if (subCode != null) {
          String subCodeNS = soap12F._getSubCodeNS();
          String prf;
          writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.SUBCODETAG_NAME);
          //map prefix for subCodeNS
          int pos = subCode.indexOf(':');
          if (pos != -1) {
            prf = subCode.substring(0, pos);
            writer.setPrefixForNamespace(prf, subCodeNS);
            subCode = subCode.substring(pos + 1);
          } else {
            prf = writer.getPrefixForNamespace(subCodeNS);
            if (prf == null) { //no prefix for namespace
              prf = "dPrf";
              writer.setPrefixForNamespace(prf, subCodeNS);
            }
          }
          writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.VALUETAG_NAME);
          writer.writeContent(prf + ":" + subCode);
          writer.leave(); //leave Subcode/Value element
          writer.leave(); //leave Subcode element
        }
      }
      writer.leave(); //fault code
            
      String errMessage = null;
      if (errMessage == null) {
        errMessage = thr.getLocalizedMessage();
      }
      if (errMessage == null) {
        errMessage = thr.getClass().getName();
      }
      
      String faultString = errMessage;
      if (iSoapExc != null) {
        faultString = iSoapExc._getFaultString();
      }
      
      //faultstring setting
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.REASONTAG_NAME);
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.TEXTTAG_NAME);
      if (iSoapExc != null && iSoapExc._getFaultStringLang() != null) {
        writer.writeAttribute(NS.XML, "lang", iSoapExc._getFaultStringLang());
      } else {
        writer.writeAttribute(NS.XML, "lang", Locale.getDefault().getLanguage()); //use EN as default language
      }
      writer.writeContent(faultString);
      writer.leave(); //Text
      //check for reason text in other languages
      if (soap12F != null) {
        List addL = soap12F._getAdditionalReasonsLanguages();
        if (addL != null) {
          for (int i = 0; i < addL.size(); i++) {
            String addLAbb = (String) addL.get(i);
            String addLTrans = soap12F._getReason(addLAbb);
            writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.TEXTTAG_NAME);
            writer.writeAttribute(NS.XML, "lang", addLAbb);
            writer.writeContent(addLTrans);
            writer.leave(); //Text
          }
        }
      }      
      writer.leave(); //Reason
  
      //fault role(actor) setting
      if (iSoapExc != null && iSoapExc._getFaultActor() != null) {
        writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.ROLETAG_NAME);
        writer.writeContent(iSoapExc._getFaultActor());
        writer.leave();
      }
  
      writer.enter(SOAPMessage.SOAP12_NS, SOAPMessage.SOAP12_DETAILTAG_NAME);
  
      if (fault != null) { //Declared client fault
        String namespace = fault.getFaultElementQName().getNamespaceURI();
        String faultElementName = fault.getFaultElementQName().getLocalPart();
  
  //      if (faultElementName != null) {
          writer.enter(namespace, faultElementName); //Use the faultElementName from the configuration
  //      } else {
  //        writer.enter(namespace, operationName + "_" + fault.getFaultName()); //The string is so in the wsdl
  //      }
        //supported for backwards compatibility
//        if (ENCODED_USE.equals(use)) {
//          writer.writeXmlAttribute("encodingStyle", NS.SOAPENC);
//        }
  
        TypeMapping typeMapping  = getTypeMapping(use, ctx.getStaticContext().getTypeMappingRegistry());
        SOAPSerializationContext context = getSOAPSerializationContext(typeMapping, use);
        //mark not to serialize with ns due to .NET requirements
        if (! use.equals(LITERAL_USE)) {
          //context.setProperty(PropertyList.USE_UNQUALIFIED, "true");
          context.setBypassNamespaces(true);
        }
  
        //seaching for serializer
        String wsdlClass = fault.getJavaType();
        QName  qname = fault.getSchemaQName();
        SerializerBase serializer = null;
        try {
          serializer = context.getSerializer(qname, thr.getClass());
        } catch (XmlMarshalException xmlME) {
          throw new ProcessException(ExceptionConstants.NO_SERIALIZER_FOUND, new Object[]{qname, context.getTypeMapping()}, xmlME);
        }
  
        try {
          serializer.serialize(thr, writer, context);
        } catch (java.rmi.MarshalException mE) {
          throw new ProcessException(ExceptionConstants.SERIALIZING_FAILS, new Object[]{thr.getClass(), mE.getLocalizedMessage()}, mE);
        } catch (java.io.IOException ioE) {
          throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
        } finally {
          //rollback the context instance.
          context.clearContext();
          serializationContextPool.rollBackInstance(context);
        }
  
      } else if (faultCode.equals(CLIENT_ERROR_CODE)) {
        writer.enter(CLIENT_ERROR_NAMESPACE, thr.getClass().getName());
        writer.writeContent(errMessage);
        writer.leave();
      } else {
        writer.enter(SERVER_ERROR_NAMESPACE, thr.getClass().getName());
        writer.writeContent(errMessage);
        writer.leave();
      }
  
      writer.leave();//detail
      writer.leave();//fault
    } catch (java.io.IOException ioE) {
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }

  private static Object getAttachmentData(ParameterMapping attParam) throws ProcessException {
    return(getAttachmentData(attParam, ProviderAttachmentHandlerFactory.getAttachmentHandler()));
  }
  
  public static Object getAttachmentData(ParameterMapping attParam, AttachmentHandler attachmentHandler) throws ProcessException {
    String partID = StreamEngineMIME.escapePartNameAP10(attParam.getWSDLParameterName());
    Attachment att = StreamEngineMIME.getPartByCIDStart(partID, attachmentHandler.getInboundAttachments());
    if (att == null) {
      throw new ProcessException("Cannot find attachment with CID '" + partID + "'");  
    }
    try {
      //request att's content are always DataHandler 
      DataHandler dh = (DataHandler) att.getContentObject();
      if (DataHandler.class.getName().equals(attParam.getJavaType())) {
        return dh;
      } else if (Image.class.getName().equals(attParam.getJavaType())) {
        InputStream in = dh.getInputStream();
        Image img = ImageIO.read(in);
        return img;
      } else if (DataSource.class.getName().equals(attParam.getJavaType())) {
        return dh.getDataSource();
      } else if (javax.xml.transform.Source.class.getName().equals(attParam.getJavaType())) {
        StreamSource ss = new StreamSource();
        ss.setInputStream(dh.getInputStream());
        return ss;
      }
    } catch (Exception e) {
      throw new ProcessException(e);
    }
    throw new ProcessException("Unsupported attachment java type '" + attParam.getJavaType());
  }
  
  private static void serializeAttachment(Object obj, ParameterMapping attParam) throws ProcessException {
    serializeAttachment(obj, attParam, ProviderAttachmentHandlerFactory.getAttachmentHandler());
  }
  
  public static void serializeAttachment(Object obj, ParameterMapping attParam, AttachmentHandler attHandler) throws ProcessException {
    
    DataHandler dh = null;
    if (DataHandler.class.getName().equals(attParam.getJavaType())) {
      dh = (DataHandler) obj;
      /* hack in order to pass CTS tests:
       * com/sun/ts/tests/jaxws/wsi/w2j/rpc/literal/swatest/Client.java#VerifyResponseContentTypeHttpHeaderWithAttachments
       * com/sun/ts/tests/jaxws/wsi/w2j/document/literal/swatest/Client.java#VerifyResponseContentTypeHttpHeaderWithAttachments
       * The URL looks like http://localhost:56000//WSWSIDLSwaTest/attach.html, the "//" at the beginning of the
       * context must be "/"
       */
      DataSource ds = dh.getDataSource();
      if (ds != null && ds instanceof URLDataSource) { //DH is constructed by the URL param constructor
        URLDataSource urlDS = (URLDataSource) ds;
        String n = urlDS.getURL().toString();
        if (n != null && n.startsWith("http://")) { 
          String subS = n.substring("http://".length());
          if (subS.contains("//")) {
            subS = subS.replaceAll("/+", "/");
            try {
              dh = new DataHandler(new URL("http://" + subS));
            } catch (Exception e) {
              throw new ProcessException(e);            
            }
          }
        }
      }
    } else if (DataSource.class.getName().equals(attParam.getJavaType())) {
      dh = new DataHandler((DataSource) obj);
    } else if (javax.xml.transform.Source.class.getName().equals(attParam.getJavaType())) {
      if (! (obj instanceof StreamSource)) {
        throw new ProcessException("Only " + StreamSource.class.getName() + " is supported as Source impl for outbound attachment.");
      }
      final StreamSource ss = (StreamSource) obj;
      dh = new DataHandler(new DataSource() {
        public String getContentType() {
          return "text/xml";
        }
        public InputStream getInputStream() throws IOException {
          // TODO Auto-generated method stub
          return ss.getInputStream();
        }
        public String getName() {
          // TODO Auto-generated method stub
          return null;
        }
        public OutputStream getOutputStream() throws IOException {
          // TODO Auto-generated method stub
          return null;
        }
      });
    } else if (Image.class.getName().equals(attParam.getJavaType())) {
      if (! (obj instanceof RenderedImage)) {
        throw new ProcessException("Only " + RenderedImage.class.getName() + " is supported as java.awt.Image impl for outbound attachment.");
      }
      RenderedImage img = (RenderedImage) obj;
      ReferenceByteArrayOutputStream buf = new ReferenceByteArrayOutputStream();
      try {
        ImageIO.write(img, "JPEG", buf);
      } catch(Exception e) {
        throw new ProcessException(e);
      }
      final ByteArrayInputStream input = new ByteArrayInputStream(buf.getContentReference(), 0, buf.size());
      dh = new DataHandler(new DataSource() {
        public String getContentType() {
          return "image/jpeg";
        }
        public InputStream getInputStream() throws IOException {
          // TODO Auto-generated method stub
          return input;
        }
        public String getName() {
          // TODO Auto-generated method stub
          return null;
        }
        public OutputStream getOutputStream() throws IOException {
          // TODO Auto-generated method stub
          return null;
        }
      });
    } else {
      throw new ProcessException("Unsupported attachment java type '" + attParam.getJavaType());
    }
    
    //initialized outboud attachment
    String cid = StreamEngineMIME.getCIDAP10(attParam.getWSDLParameterName());
    
    Attachment att = attHandler.createAttachment();
    att.setContentId(cid);
    String ctType = dh.getContentType();
    att.setContentType(ctType);
    att.setDataHandler(dh);
    
    attHandler.addOutboundAttachment(att);
  }

  private static Object deserializeJAXBValueType(XMLStreamReader sreader, Unmarshaller jaxbUnmarsh, ParameterMapping currentParam, ClassLoader appLoader, Class resClass) throws ProcessException {
    try {
      String valueClassStr = currentParam.getProperty(ParameterMapping.JAXB_BEAN_CLASS);
      Class<?> valueClass = appLoader.loadClass(valueClassStr);
      Object valueObj = jaxbUnmarsh.unmarshal(sreader, valueClass).getValue();
      Object ob = getJavaBeanPropOrPublicFieldValue(valueObj, "value", resClass);
      if (ob == null) {
        throw new ProcessException("Class '" + valueClassStr + " is missing property with name 'value' and type " + resClass);
      }
      if (ob == NILLABLE_OBJECT) {
        return null;
      } else {
        return ob;
      }
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }
  
  private static void serializeJAXBValueType(ParameterMapping param, XMLStreamWriter writer, Object object, Class objClass, ClassLoader appLoader, Marshaller jaxbMarsh) throws ProcessException {
    try {
      String valueClassStr = param.getProperty(ParameterMapping.JAXB_BEAN_CLASS);
      Class<?> valueClass = appLoader.loadClass(valueClassStr);
      Object valueObj = valueClass.newInstance();
      setJavaBeanPropertyOrPublicFiledValue(valueObj, "value", objClass, object);
      JAXBElement jaxbEl = new JAXBElement(new QName(param.getNamespace(), param.getWSDLParameterName()), valueClass, valueObj);
      jaxbMarsh.marshal(jaxbEl, writer);
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }

}