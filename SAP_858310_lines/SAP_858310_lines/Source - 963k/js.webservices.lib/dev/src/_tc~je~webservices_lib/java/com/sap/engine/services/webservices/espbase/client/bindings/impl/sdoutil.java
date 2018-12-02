package com.sap.engine.services.webservices.espbase.client.bindings.impl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.rpc.holders.ObjectHolder;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPElement;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLDOMTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.stream.XMLStreamReaderImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.bindings.exceptions.TransportBindingException;
import com.sap.engine.services.webservices.espbase.client.dynamic.ServiceFactoryConfig;
import com.sap.engine.services.webservices.espbase.client.dynamic.WebServiceException;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.sdo.api.helper.SapXmlHelper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import commonj.sdo.helper.HelperContext;

/**
 * The class support SDO serialization and deserialization for consumers.
 * @author I024072
 *
 */
public class SDOUtil {
  
  
  private static Location loc = Location.getLocation(SDOUtil.class);
  
  /**
   * Serializes JAXB Parameter.
   * @param writer
   * @param paramObj
   * @param paramMapping
   * @param soapHeaderList
   * @param marshaller
   * @throws RemoteException
   */
  public static void serializeParameter(XMLStreamWriter writer, ParameterObject paramObj, ParameterMapping paramMapping, SOAPHeaderList soapHeaderList, SapXmlHelper helperContext, Map options) throws RemoteException {
    try {
      Object content = paramObj.parameterValue;     
      if (content != null && content instanceof ObjectHolder) {
        content = ((ObjectHolder) content).value;
      }
      QName schemaName = paramMapping.getSchemaQName();
      if(paramMapping.isAttachment()) {
        throw new UnsupportedOperationException("Attachments are not supported when SDO is used.");
      } else if (paramMapping.isElement()) { // A SOAP Header or document/bare parameter
        if (paramMapping.isHeader() == true) {
          throw new UnsupportedOperationException("SOAP Headers are not supported when SDO is used.");
        } else {
          helperContext.save(content,schemaName.getNamespaceURI(),schemaName.getLocalPart(),null,null,writer, options);
        }
      } else { // rpc/literal or document/wrapped case         
        helperContext.save(content,paramMapping.getNamespace(),paramMapping.getWSDLParameterName(),schemaName.getNamespaceURI(),schemaName.getLocalPart(),writer, options);     
      }
    } catch (Exception exc) {
      throw exc instanceof RemoteException ? (RemoteException)exc : new RemoteException("Connection Exception", exc); 
    }    
  }    
  
  /**
   * Builds operation request message.
   * It uses either 
   * @param opMapping
   * @param typeMapping
   * @param parameters
   * @throws Exception
   */
  public static void serializeRequest(OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context, XMLStreamWriter writer, String namespace, SOAPHeaderList soapHeaderList) throws RemoteException {
    try {      
      HelperContext helperContext = context.getHelperContext();
      Map options = (Map) context.getProperty(ServiceFactoryConfig.OPTIONS);
      SapXmlHelper xmlHelper = (SapXmlHelper) helperContext.getXMLHelper(); 
      writer.writeStartElement(namespace, SOAPMessage.BODYTAG_NAME);      
      // Get operation style - if it is RPC then add RPC Wrapper element
      String style = operationMapping.getProperty(OperationMapping.OPERATION_STYLE);
      if ("rpc".equals(style)) { // "rpc" style
        String operationName = operationMapping.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER);      
        String operationNamespace = operationMapping.getProperty(OperationMapping.INPUT_NAMESPACE);
        writer.writeStartElement(operationNamespace, operationName);                    
      }
      ParameterMapping[] params = operationMapping.getParameter();
      for (int i=0; i<params.length; i++) {
        if (params[i].getParameterType() == ParameterMapping.IN_TYPE ||
            params[i].getParameterType() == ParameterMapping.IN_OUT_TYPE) {
           ParameterMapping param = params[i];
           serializeParameter(writer, paramObjs[i], param, soapHeaderList,xmlHelper, options);
        }      
      }
      if ("rpc".equals(style)) {        
        writer.writeEndElement();
      }
      writer.writeEndElement(); 
      writer.flush();
    } catch (Exception ioExc) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, ioExc, ioExc.getMessage());
    }
  }
  
  /**
   * Process the response fault.
   * @param opMapping
   * @param typeMapping
   * @param parameters
   */
  public static void processFault(OperationMapping operationMapping , ParameterObject[] paramObjs, ClientConfigurationContext context, XMLTokenReader reader) throws RemoteException {    
    ParameterMapping[] parameterMappings = operationMapping.getParameter();
    javax.xml.rpc.soap.SOAPFaultException exception = FaultUtil.buildFaultException(reader, context);
    SOAPElement element = (FaultUtil.getDetailRootElement(exception.getDetail()));    
    if(element != null) {
      String elementName = element.getLocalName();
      String namespace = element.getNamespaceURI();
      QName elementQName = new QName(namespace, elementName);
      for(int i = 0; i < parameterMappings.length; i++) {
        if(parameterMappings[i].getParameterType() == ParameterMapping.FAULT_TYPE) {
          QName faultElement = parameterMappings[i].getFaultElementQName();
          if(faultElement.equals(elementQName)) {
            try {
              ParameterMapping faultMapping = parameterMappings[i];              
              deserializeException(element, exception, paramObjs[i], faultMapping, context);    
              return;
            } catch(SOAPFaultException x) {
              // Deserialization failed - rethrow the SOAPFaultException
              throw x;
            } catch(Exception exc) {
              loc.traceThrowableT(Severity.DEBUG, "Deserialization of the SoapFaultElement into opertion parameter failed. Rethrow the original SoapFaultException.", exc);
              break;
            }
          }
        }
      }      
    }
    exception.fillInStackTrace();
    throw exception;
  }
  
  /**
   * Deserializes exception parameter.
   * @param element
   * @param fault
   * @param paramObj
   * @param mapping
   * @param context
   */
  public static void deserializeException(SOAPElement element, SOAPFaultException fault, ParameterObject paramObj,ParameterMapping mapping, ClientConfigurationContext context) {
    try {      
      HelperContext helperContext = context.getHelperContext();
      Map options = (Map) context.getProperty(ServiceFactoryConfig.OPTIONS);
      SapXmlHelper xmlHelper = (SapXmlHelper) helperContext.getXMLHelper();             
      XMLDOMTokenReader reader = new XMLDOMTokenReader(element);      
      reader.begin();
      XMLStreamReader sreader = new XMLStreamReaderImpl(reader);
      Object content = null;
      QName schemaName = mapping.getFaultElementQName();
      content = xmlHelper.load(sreader, options);
      // Create checked exception
      paramObj.parameterValue = content;
      String faultName = mapping.getWSDLParameterName();
      WebServiceException wsException = new WebServiceException(fault.getFaultString(),fault,faultName,content,schemaName);
      paramObj.parameterValue = wsException;                    
    } catch (Throwable exc) {
      loc.traceThrowableT(Severity.DEBUG, "Exception caught while deserializing the SoapFaultException to operation parameter.", exc);
      fault.fillInStackTrace();
      throw fault;
    }   
  }
  
  
  /**
   * Reads operation response message.
   * @param appLoader
   * @param opMapping
   * @param typeMapping
   * @param parameters
   */
  public static void deserializeResponse(OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context, XMLTokenReader reader, String namespace, SOAPHeaderList soapHeaderList) throws RemoteException  {
    try {
      HelperContext helperContext1 = context.getHelperContext();
      Map options = (Map) context.getProperty(ServiceFactoryConfig.OPTIONS);
      SapXmlHelper xmlHelper = (SapXmlHelper) helperContext1.getXMLHelper();             
      reader.passChars();
      if (reader.getState() != XMLTokenReader.STARTELEMENT || !reader.getLocalName().equals(SOAPMessage.BODYTAG_NAME)) {
        throw new TransportBindingException(TransportBindingException.NO_SOAP_BODY);
      }
      // Enters Into the soap body      
      reader.next(); reader.passChars();
      if (reader.getState() == XMLTokenReader.STARTELEMENT && SOAPMessage.FAULTTAG_NAME.equals(reader.getLocalName()) && namespace.equals(reader.getURI())) {
        processFault(operationMapping, paramObjs, context, reader);
        return;      
      }
      // Reader positioned into the body response node.
      XMLStreamReader sreader = new XMLStreamReaderImpl(reader);            
      String style = operationMapping.getProperty(OperationMapping.OPERATION_STYLE);
      if ("rpc".equals(style)) {
        // enters in response element his name doesn't matte          
        sreader.nextTag(); 
      }
      ParameterMapping[] params = operationMapping.getParameter();
      for (int i = 0; i < params.length; i++) {
        ParameterMapping param = params[i];
        if (param.getParameterType() == ParameterMapping.OUT_TYPE ||
            param.getParameterType() == ParameterMapping.IN_OUT_TYPE || 
            param.getParameterType() == ParameterMapping.RETURN_TYPE) {
          deserializeParameter(sreader, paramObjs[i], param, soapHeaderList,xmlHelper, options);
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
        throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, exc, exc.getMessage());
      } else if(!(exc instanceof RemoteException)) {
        throw new TransportBindingException(TransportBindingException.PARAMETER_SET_FAIL, exc);
      }
      throw (RemoteException)exc;
    }
  }
  
  /**
   * Deserialize JAXB Parameter.
   * @param reader
   * @param paramObj
   * @param paramMappping
   * @param typeMapping
   * @param soapHeaderList
   * @param unmarshaller
   * @throws RemoteException
   */
  public static void deserializeParameter(XMLStreamReader reader, ParameterObject paramObj, ParameterMapping paramMappping, SOAPHeaderList soapHeaderList, SapXmlHelper helper, Map invokerOptions) throws RemoteException {
    try {    
      Object content = null;
      //Class parameterType = paramObj.parameterType;
      QName schemaName = paramMappping.getSchemaQName();
      //Map options = new HashMap(SapXmlHelper.OPTION_KEY_ERROR_HANDLER, SDOErrorHandler.getInstance());
      if (invokerOptions == null){
        invokerOptions = new HashMap();
        invokerOptions.put(SapXmlHelper.OPTION_KEY_ERROR_HANDLER, SDOErrorHandler.getInstance());
      }else{
        if (invokerOptions.get(SapXmlHelper.OPTION_KEY_ERROR_HANDLER) == null){
          invokerOptions.put(SapXmlHelper.OPTION_KEY_ERROR_HANDLER, SDOErrorHandler.getInstance());
        }
      }
            
      if(paramMappping.isAttachment()) {
        throw new UnsupportedOperationException("Attachments are not supported with SDO serialization.");
      } else if (paramMappping.isElement()) {
        if (paramMappping.isHeader() == true) {
          throw new UnsupportedOperationException("SOAP Headers are not supported with SDO serialization.");
        } else {
          if (reader.isStartElement() == false) { // Not positioned on start element
            if (paramMappping.isElement()) {
              throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, paramMappping.getSchemaQName().toString());
            } else {
              throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, paramMappping.getWSDLParameterName());
            }
          }
          content = helper.load(reader, invokerOptions);
        }
      } else {
        if (!reader.isStartElement()) { // Not positioned on start element
          //throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, paramMappping.getWSDLParameterName());
          while (reader.isEndElement() && !reader.getLocalName().equals(SOAPMessage.BODYTAG_NAME)){
            if(reader.nextTag() == XMLStreamReader.END_DOCUMENT){
              break;
            }
          }
        }           
        content = helper.load(reader,schemaName.getNamespaceURI(),schemaName.getLocalPart(), invokerOptions);
      }
      // Reader is positioned at the end of the parameter
      if (paramObj.parameterValue != null && paramObj.parameterValue instanceof ObjectHolder) {
        ((ObjectHolder) paramObj.parameterValue).value = content;
      } else {
        paramObj.parameterValue = content;
      }
    } catch (Exception exc) {
      throw exc instanceof RemoteException ? (RemoteException)exc : new TransportBindingException(TransportBindingException.PARAMETER_SET_FAIL, exc);
    }   
  }
  
}
