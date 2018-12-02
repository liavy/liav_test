package com.sap.engine.services.webservices.espbase.client.bindings.impl;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.stream.XMLStreamReaderImpl;
import com.sap.engine.services.webservices.espbase.client.api.AttachmentHandler;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.bindings.exceptions.TransportBindingException;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.SOAPHeaderList;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.additions.StreamEngine;
import com.sap.engine.services.webservices.jaxb.AttachmentUnmarshallerImpl;
import com.sap.engine.services.webservices.jaxb.ClientAttachmentMarshaller;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.JAXBRIContext;

public class JAXWSUtil {
  
  private static Location loc = Location.getLocation(JAXWSUtil.class);

  public static Object getParameterContent(ParameterObject paramObj) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
    Object content = paramObj.parameterValue;
    if(content != null && content instanceof Holder) { // Holder parameter           
      content = ((Holder)content).value;
    }
    return content;
  }
  
  public static void setParameterContent(Object content, ParameterObject paramObj) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    if (paramObj.parameterValue != null && (paramObj.parameterValue instanceof Holder)) {
      ((Holder) paramObj.parameterValue).value = content;
    } else {
      paramObj.parameterValue = content;
    }    
  }
  
  public static Class getParameterClass(ParameterObject paramObj, Object content) {
    Class paramClass = paramObj.parameterType;
    if(paramClass == null && content != null) {
      paramClass = content.getClass(); 
    }
    return(paramClass);
  }
  
  /**
   * Serializes JAXB Parameter.
   * @param writer
   * @param paramObj
   * @param paramMapping
   * @param soapHeaderList
   * @param marshaller
   * @throws RemoteException
   */
  private static void serializeParameterJAXB(XMLStreamWriter writer, ParameterObject paramObj, ParameterMapping paramMapping, SOAPHeaderList soapHeaderList, Marshaller marshaller, JAXBRIContext context, AttachmentHandler attachmentHandler) throws RemoteException {
    try {
      if(paramMapping.isAttachment()) {
        StreamEngine.serializeAttachment(getParameterContent(paramObj), paramMapping, attachmentHandler);
      } else if(paramMapping.isHeader()) {
        Object content = getParameterContent(paramObj);
        JAXBElement parameter = new JAXBElement(paramMapping.getSchemaQName(), getParameterClass(paramObj, content), content);
        Element temp = soapHeaderList.createHeader(new QName(null,"temp"));
        marshaller.marshal(parameter,temp);
        soapHeaderList.addHeader((Element) temp.getChildNodes().item(0));
      } else {
        serializeParameterJAXB(writer, paramObj, paramMapping, marshaller, context);
      }
    } catch (Exception exc) {
      throw exc instanceof RemoteException ? (RemoteException)exc : new RemoteException("Connection Exception", exc); 
    }    
  }  

  public static void serializeParameterJAXB(XMLStreamWriter writer, ParameterObject paramObj, ParameterMapping paramMapping, Marshaller marshaller, JAXBRIContext context) throws RemoteException {
    try {
      Object content = getParameterContent(paramObj);
      if(paramMapping.isElement()) { //Document/bare parameter
        if(paramObj.typeReference != null) {
          Bridge bridge = context.createBridge(paramObj.typeReference);
          bridge.marshal(marshaller, content, writer);                    
        } else {
          JAXBElement parameter = new JAXBElement(paramMapping.getSchemaQName(), getParameterClass(paramObj, content), content);
          marshaller.marshal(parameter,writer);
        }
      } else { // rpc/literal or document/wrapped case         
        Bridge bridge = context.createBridge(paramObj.typeReference);
        bridge.marshal(content,writer);        
      }
    } catch (Exception exc) {
      throw exc instanceof RemoteException ? (RemoteException)exc : new RemoteException("Exception", exc); 
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
  private static boolean deserializeParameterJAXB(XMLStreamReader reader, ParameterObject paramObj, ParameterMapping paramMappping, SOAPHeaderList soapHeaderList, Unmarshaller unmarshaller,JAXBRIContext context, AttachmentHandler attachmentHandler) throws RemoteException {
    try {    
      boolean beDebug  = loc.beDebug();
      Object content = null;
      Class parameterType = paramObj.parameterType;
      QName schemaName = paramMappping.getSchemaQName();
      if(paramMappping.isAttachment()) {
        content = StreamEngine.getAttachmentData(paramMappping, attachmentHandler);
        if (beDebug){
          loc.debugT("Attachment deserialized");
        }
      } else if (paramMappping.isElement()) {
        if (paramMappping.isHeader() == true) {
          Element header = soapHeaderList.getHeader(schemaName);
          if (header == null) {
            throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER,schemaName.toString());
          }
          content = unmarshaller.unmarshal(header,parameterType).getValue();
          if (beDebug){
            loc.debugT("Header deserialized");
          }
        } else {
          if (reader.isStartElement() == false) { // Not positioned on start element
            if (paramMappping.isElement()) {
              throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, paramMappping.getSchemaQName().toString());
            } else {
              throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, paramMappping.getWSDLParameterName());
            }
          }
          if (paramObj.typeReference != null) {
            Bridge bridge = context.createBridge(paramObj.typeReference);
            content = bridge.unmarshal(unmarshaller, reader);                    
          } else {
            content = unmarshaller.unmarshal(reader,parameterType).getValue();
          }
          if (beDebug){
            loc.debugT("Element deserialized");
          }
        }
      } else {
        if (!reader.isStartElement()) { // Not positioned on start element
          throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, paramMappping.getWSDLParameterName());
        }
        if(!paramMappping.getWSDLParameterName().equals(reader.getLocalName())) {
          return(false);
        }
        
        Bridge bridge = context.createBridge(paramObj.typeReference);
        content = bridge.unmarshal(reader);
        if (beDebug){
          loc.debugT("Parameter deserialized");
        }

      }
      // i044259
      if (content != null && (content instanceof javax.xml.datatype.XMLGregorianCalendar)) {
        if (parameterType == java.util.GregorianCalendar.class || 
            parameterType == java.util.Calendar.class) {
          content = ((javax.xml.datatype.XMLGregorianCalendar) content).toGregorianCalendar();
        } else if (parameterType == java.util.Date.class) {
          content = ((javax.xml.datatype.XMLGregorianCalendar) content).toGregorianCalendar().getTime();
        }
      }
      // Reader is positioned at the end of the parameter
      setParameterContent(content,paramObj);
      if (beDebug){
        if (content instanceof String){
          loc.debugT("Deserialized content length: [" + ((String)content).length() + "] characters");
        }
      }
      return(true);
    } catch (Exception exc) {
      throw exc instanceof RemoteException ? (RemoteException)exc : new TransportBindingException(TransportBindingException.PARAMETER_SET_FAIL, exc);
    }   
  }
  
  public static void serializeRequestJAXB(OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context, XMLStreamWriter writer, SOAPHeaderList soapHeaderList) throws RemoteException {
    JAXBRIContext jaxbContext = (JAXBRIContext) context.getJAXBContext();
    AttachmentMarshaller attachmentMarshaller = context.getAttachmentMarshaller();
    ClassLoader loader = context.getClientAppClassLoader();
    String requestBean = operationMapping.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN);
    String operationName = operationMapping.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER);      
    String operationNamespace = operationMapping.getProperty(OperationMapping.INPUT_NAMESPACE);        
    int paramsMask = ParameterMapping.IN_TYPE | ParameterMapping.IN_OUT_TYPE;
    AttachmentHandler attachmentHandler = ((ClientAttachmentMarshaller)attachmentMarshaller).getAttachmentHandler();
    
    ArrayList<ParameterObject> tmpArr = new ArrayList(); 
    ParameterMapping pMaps[] = operationMapping.getParameter();
    for (int i = 0; i < pMaps.length; i++) {
      if (pMaps[i].getParameterType() == ParameterMapping.IN_TYPE || pMaps[i].getParameterType() == ParameterMapping.IN_OUT_TYPE) {
        tmpArr.add(paramObjs[i]);
      }
    }
    ParameterMapping[] params = operationMapping.getParameters(paramsMask);

    serializeOutboundJAXB(operationMapping, tmpArr.toArray(new ParameterObject[tmpArr.size()]), writer, soapHeaderList, jaxbContext, attachmentMarshaller, loader, requestBean, operationNamespace, operationName, params, attachmentHandler);
  }
  /**
   * Builds operation request message.
   * @param opMapping
   * @param typeMapping
   * @param parameters
   * @throws Exception
   */
  public static void serializeOutboundJAXB(OperationMapping operationMapping, ParameterObject[] paramObjs, XMLStreamWriter writer, SOAPHeaderList soapHeaderList,
                                          JAXBRIContext jaxbContext, AttachmentMarshaller attachmentMarshaller, ClassLoader loader, String jaxbBean, String opNS, String opLoc, ParameterMapping[] params, AttachmentHandler attachmentHandler) throws RemoteException {
    try {      
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
      marshaller.setAttachmentMarshaller(attachmentMarshaller);
      //writer.writeStartElement(namespace, SOAPMessage.BODYTAG_NAME);      
      String style = operationMapping.getProperty(OperationMapping.OPERATION_STYLE);
      if (jaxbBean != null) { // document&wrapped
        Class requestBeanClass = loader.loadClass(jaxbBean);
        ParameterMapping wrapperElement = new ParameterMapping();
        wrapperElement.setSchemaQName(new QName(opNS,opLoc));
        wrapperElement.setIsElement(true);
        ParameterObject tempParam = new ParameterObject();
        tempParam.parameterType = requestBeanClass;
        tempParam.parameterValue = requestBeanClass.newInstance();
        // Copy the input parameters to the wrapper bean
//        ParameterMapping[] params = operationMapping.getParameters(paramsMask);
        // rpc/literal or document/bare
        HashMap<QName,Object> fields = getJAXBElementsNew(requestBeanClass);
        for (int i=0; i<params.length; i++) {
//          if (params[i].getParameterType() == ParameterMapping.IN_TYPE ||
//              params[i].getParameterType() == ParameterMapping.IN_OUT_TYPE) {
             ParameterMapping param = params[i];             
             if (param.isHeader()) { // Serialize headers
               serializeParameterJAXB(writer,paramObjs[i],params[i],soapHeaderList,marshaller,jaxbContext,attachmentHandler);
             } else {
               QName fieldQName = new QName(param.getNamespace(),param.getWSDLParameterName());
               setRequestField(fieldQName,fields,getParameterContent(paramObjs[i]),paramObjs[i].parameterType,tempParam.parameterValue);               
             }
//          }      
        }        
        serializeParameterJAXB(writer,tempParam,wrapperElement,soapHeaderList,marshaller,jaxbContext,attachmentHandler);
      } else { // rpc and document&bare
        if ("rpc".equals(style)) { // "rpc" style
//          String operationName = operationMapping.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER);      
//          String operationNamespace = operationMapping.getProperty(OperationMapping.INPUT_NAMESPACE);
          writer.writeStartElement(opNS, opLoc);            
        }
//        ParameterMapping[] params = operationMapping.getParameters(paramsMask);
        // rpc/literal or document/bare
        for (int i=0; i<params.length; i++) {
//          if (params[i].getParameterType() == ParameterMapping.IN_TYPE ||
//              params[i].getParameterType() == ParameterMapping.IN_OUT_TYPE) {
             ParameterMapping param = params[i];
             serializeParameterJAXB(writer, paramObjs[i], param, soapHeaderList, marshaller,jaxbContext,attachmentHandler);
//          }      
        }
        if ("rpc".equals(style)) {        
          writer.writeEndElement();
        }
      }
      //writer.writeEndElement(); 
      writer.flush();                
    } catch (Exception ioExc) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, ioExc, ioExc.getMessage());
    }
  }  
  
  /**
   * Reads tag contents.
   * @param reader
   * @return
   * @throws XMLStreamException
   */
  public static String getValuePassChars(XMLStreamReader reader) throws XMLStreamException {
    StringBuffer result = new StringBuffer();
    while (reader.isCharacters() || reader.getEventType() == XMLStreamConstants.ENTITY_REFERENCE) {
      result.append(reader.getText());
      reader.next();
    }
    return result.toString();
  }
  
  /**
   * Returns prefix mapping.
   * @param pref
   * @param localAttrs
   * @param reader
   * @return
   */
  private static String getPrefixUri(String pref, Attributes localAttrs, XMLTokenReader reader) {
    //search in the local element declarations
    String value = localAttrs.getValue("xmlns:" + pref);
    if (value != null) {
      return(value);
    }
    //use upper level declarations
    return(reader.getPrefixMapping(pref));
  }
  
  /**
   * Returns first element found in fault detail.
   * @param detail
   * @return
   */
  private static SOAPElement getDetailElement(Detail detail) {
    if (detail == null) {
      return null;
    }
    Iterator it = detail.getDetailEntries();
    SOAPElement result = null;
    while(it.hasNext()) {
      Object next = it.next();
      if (next instanceof SOAPElement) {
        result = (SOAPElement) next;
        break;
      }
    }
    return(result);
  }
  
  /**
   * Process the response fault.
   * @param opMapping
   * @param typeMapping
   * @param parameters
   */
  public static void processFault(OperationMapping operationMapping , ParameterObject[] paramObjs, ClientConfigurationContext context, XMLTokenReader reader) throws RemoteException {
    ParameterMapping[] parameterMappings = operationMapping.getParameter();
    SOAPFault fault = buildFault(reader, context);
    SOAPElement element = getDetailElement(fault.getDetail());      
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
              deserializeException(element, fault, paramObjs[i], faultMapping, context);              
              return;
            } catch(SOAPFaultException x) {
              throw x;
            } catch(Exception exc) {
              loc.traceThrowableT(Severity.DEBUG, "Deserialization of the SoapFaultElement into opertion parameter failed. Rethrow the original SoapFaultException.", exc);
              break;
            }
          }
        }
      }      
    }
    throw new com.sap.engine.services.webservices.espbase.client.bindings.exceptions.SOAPFaultException(fault);
  }
  
  private static void deserializeException(SOAPElement element, SOAPFault fault, ParameterObject paramObj,ParameterMapping mapping, ClientConfigurationContext context) {
    try {      
      Unmarshaller unmarshaller = context.getJAXBContext().createUnmarshaller();
      Object content = null;
      Class faultInfoClass = context.getClientAppClassLoader().loadClass(mapping.getProperty(ParameterMapping.JAXB_BEAN_CLASS));      
      Class faultType = paramObj.parameterType;
      if (faultType == null) {
        faultType = context.getClientAppClassLoader().loadClass(mapping.getJavaType()); 
      }
      QName schemaName = mapping.getFaultElementQName();
      content = unmarshaller.unmarshal(element,faultInfoClass).getValue();
      // Create checked exception
      Constructor initConstructor = faultType.getConstructor(new Class[] {String.class,faultInfoClass});
      Object resultException = initConstructor.newInstance(new Object[] {fault.getFaultString(), content});      
      setParameterContent(resultException,paramObj);
    } catch (Throwable exc) {
      throw new SOAPFaultException(fault);
    }   
  }
  
  
  /**
   * Reads SOAP Message Fault and builds SOAP Fault Exception.
   * The message must be positioned on soap:fault node or on soap:body.
   * @param message
   * @return
   * @throws Exception
   */
  public static SOAPFault buildFault(XMLTokenReader reader, ClientConfigurationContext context) throws RemoteException {
    try {
      /**
      if(isSOAP12Mode(context)) {
        return(buildFaultException_SOAP12(reader));        
      }*/
      reader.passChars();
      if(reader.getState() != XMLTokenReader.STARTELEMENT) {
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      // Bypasses the body element
      if(SOAPMessage.BODYTAG_NAME.equals(reader.getLocalName()) && SOAPMessage.SOAP11_NS.equals(reader.getURI())) { 
        reader.next();
        reader.passChars();
      }
      if(reader.getState() != XMLTokenReader.STARTELEMENT) {
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      String localName = reader.getLocalName();
      String uri = reader.getURI();
      if (!SOAPMessage.FAULTTAG_NAME.equals(localName) || !SOAPMessage.SOAP11_NS.equals(uri)) { // Passes fault element
        throw new TransportBindingException(TransportBindingException.FAULT_NOT_FOUND);
      }
      // reader is positioned on SOAP:Fault and goes to the next xml element
      reader.next();
      reader.passChars();
      // SOAP:Fault components
      QName faultCode = null;
      String faultString = null;
      Detail detailNode = null;
      String faultActor = null;
      // Creates SOAPFault component
      SOAPFactory soapFactory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
      SOAPFault result = soapFactory.createFault();
      while(reader.getState() == XMLTokenReader.STARTELEMENT) {
        boolean flag = false; // rean only one node per cycle
        localName = reader.getLocalName();
        uri = reader.getURI();
        if(flag == false && SOAPMessage.FAULTCODETAG_NAME.equals(localName) && (uri == null || uri.length()==0)) {
          if(faultCode != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_CODE);
          }
          flag = true;
          Attributes localAttrs = reader.getAttributes(); //because getValuePassChars() unmaps the locally defined prefies
          reader.next();
          String qName = reader.getValuePassChars();
          String lName = DOM.qnameToLocalName(qName);
          String qNameUri = getPrefixUri(DOM.qnameToPrefix(qName), localAttrs, reader);
          faultCode = new QName(qNameUri,lName);
          if(reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_CODE);
          }
          // leaves fault code element
          reader.next(); 
          reader.passChars();
        }
        if(flag == false && SOAPMessage.FAULTSTRINGTAG_NAME.equals(localName) && (uri == null || uri.length()==0)) {
          if (faultString != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_STRING);
          }
          flag = true;
          reader.next();
          faultString = reader.getValuePassChars();
          if(reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_STRING);
          }
          // leaves fault string element
          reader.next(); 
          reader.passChars();
        }
        if(flag == false && SOAPMessage.ACTORTAG_NAME.equals(localName) && (uri == null || uri.length()==0)) {
          if (faultActor != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_ACTOR);
          }
          flag = true;
          reader.next();
          faultActor = reader.getValuePassChars();
          if (reader.getState() != XMLTokenReader.ENDELEMENT) {
            throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_ACTOR);
          }
          // leaves fault actor element
          reader.next(); 
          reader.passChars();
        }
        if(flag == false && SOAPMessage.SOAP11_DETAILTAG_NAME.equals(localName) && (uri == null || uri.length()==0)) {
          if (detailNode != null) {
            throw new TransportBindingException(TransportBindingException.REPEATING_FAULT_DETAIL);
          }
          flag = true;
          //SOAPDocumentImpl document = new SOAPDocumentImpl();
          //detailNode = new DetailImpl(document);          
          detailNode = result.addDetail();
          Document doc = detailNode.getOwnerDocument();          
          Element element = reader.getDOMRepresentation(doc);
          NamedNodeMap attributes = element.getAttributes();
          for(int j=0; j<attributes.getLength(); j++) {
            Attr attrib = (Attr) attributes.item(j);
            attrib = (Attr) detailNode.getOwnerDocument().importNode(attrib,false);
            detailNode.setAttributeNodeNS(attrib);
          }
          NodeList elements  = element.getChildNodes();
          for (int i=0; i<elements.getLength(); i++) {
            Node next = (Node) elements.item(i);
            if(next instanceof SOAPElement) {
              detailNode.addChildElement((SOAPElement) next);
            }
            if(next instanceof javax.xml.soap.Text) {
              detailNode.addTextNode(((javax.xml.soap.Text) next).getValue());
            }          
          }
          reader.next();
          reader.passChars();
        }
        if(flag == false) {
          throw new TransportBindingException(TransportBindingException.ILLEGAL_FAULT_CONTENT, uri, localName);
        }
      }      
      result.setFaultCode(faultCode);
      result.setFaultString(faultString);
      result.setFaultActor(faultActor);
      while(reader.getState()!=XMLTokenReader.EOF) {
        reader.next();
      }
      return(result);
    } catch(ParserException parserExc) {
      throw new TransportBindingException(TransportBindingException.CONNECTION_IO_ERROR, parserExc, parserExc.getMessage());
    } catch (SOAPException soapExc) {
      throw new TransportBindingException(TransportBindingException.FAULT_DETAIL_BUILD_FAIL, soapExc);
    }
  }
  
  /**
   * @param reader the reader must be on 'Role' start element.
   * @return the content of 'Role' element.
   */
  private static String getRoleValue(XMLTokenReader reader) throws ParserException {
    String res = reader.getValuePassChars();
    if(reader.getState() == XMLTokenReader.ENDELEMENT) {
      if(SOAPMessage.ROLETAG_NAME.equals(reader.getLocalName())  && SOAPMessage.SOAP12_NS.equals(reader.getURI())) {
        return(res);
      }
    }
    throw new RuntimeException("'Role' could contain only characters.");
  }
  
  /**
   * This method is used by the consumer runtime
   * @param operationMapping
   * @param paramObjs
   * @param context
   * @param reader
   * @param namespace
   * @param soapHeaderList
   * @throws RemoteException
   */
  public static void deserializeResponseJAXB(OperationMapping operationMapping, ParameterObject[] paramObjs, ClientConfigurationContext context, XMLTokenReader reader, String namespace, SOAPHeaderList soapHeaderList, boolean containsSOAPBody) throws RemoteException  {
    JAXBRIContext jaxbContext = (JAXBRIContext) context.getJAXBContext();
    AttachmentUnmarshaller attachmentUnmarshaller = context.getAttachmentUnmarshaller();
    ClassLoader loader = context.getClientAppClassLoader();
    
    boolean beDebug = loc.beDebug();
    if (beDebug){
      loc.debugT("Deserializing JAXB response");
    }
    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();    
      unmarshaller.setAttachmentUnmarshaller(attachmentUnmarshaller);

      reader.passChars();
      
      if(containsSOAPBody) {
      if (reader.getState() != XMLTokenReader.STARTELEMENT || !reader.getLocalName().equals(SOAPMessage.BODYTAG_NAME)) {
        throw new TransportBindingException(TransportBindingException.NO_SOAP_BODY);
      }
      // Enters Into the soap body      
        reader.next(); 
        reader.passChars();
      } 
      
      if (reader.getState() == XMLTokenReader.STARTELEMENT && SOAPMessage.FAULTTAG_NAME.equals(reader.getLocalName()) && namespace.equals(reader.getURI())) {
        processFault(operationMapping, paramObjs, context, reader);
        return;      
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
    String responseBean = operationMapping.getProperty(OperationMapping.RESPONSE_WRAPPER_BEAN);
    String operationNamespace = operationMapping.getProperty(OperationMapping.OUTPUT_NAMESPACE);
    String operationName = operationMapping.getProperty(OperationMapping.SOAP_RESPONSE_WRAPPER);
    if (beDebug){
      loc.debugT("Operation name is:[" + operationName + "]");
    }
    
    ArrayList<ParameterObject> tmpArr = new ArrayList();
    ParameterMapping[] pMap = operationMapping.getParameter();
    for (int i = 0; i < pMap.length; i++) {
      ParameterMapping param = pMap[i];
      if (param.getParameterType() == ParameterMapping.OUT_TYPE ||
        param.getParameterType() == ParameterMapping.IN_OUT_TYPE || 
        param.getParameterType() == ParameterMapping.RETURN_TYPE) {
        tmpArr.add(paramObjs[i]);
      }
    }
    int paramsMask = ParameterMapping.OUT_TYPE | ParameterMapping.IN_OUT_TYPE | ParameterMapping.RETURN_TYPE;
    pMap = operationMapping.getParameters(paramsMask);
    deserializeInboundJAXB(operationMapping, tmpArr, reader, soapHeaderList, jaxbContext, attachmentUnmarshaller, loader, responseBean, operationNamespace, operationName, pMap); 
  }  
  
  /**
   * This method is used by the provider and consumer runtimes.
   * @param appLoader
   * @param opMapping
   * @param typeMapping
   * @param parameters
   */
  public static void deserializeInboundJAXB(OperationMapping operationMapping, 
                                            ArrayList<ParameterObject> paramObjs, 
                                            XMLTokenReader reader, 
                                            SOAPHeaderList soapHeaderList, 
                                            JAXBRIContext jaxbContext, 
                                            AttachmentUnmarshaller attachmentUnmarshaller, 
                                            ClassLoader appLoader, 
                                            String jaxbBean, 
                                            String opNS, 
                                            String opLoc, 
                                            ParameterMapping[] params) throws RemoteException  {
    try {
      boolean beDebug = loc.beDebug();
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();    
      unmarshaller.setAttachmentUnmarshaller(attachmentUnmarshaller);
      AttachmentHandler attachmentHandler = ((AttachmentUnmarshallerImpl)attachmentUnmarshaller).getAttachmentHandler();
      // Reader positioned into the body response node.
      XMLStreamReader sreader = new XMLStreamReaderImpl(reader);            
      String style = operationMapping.getProperty(OperationMapping.OPERATION_STYLE);
      if (beDebug){
        loc.debugT("Operation style is: [" + style + "]");
      }
      if (jaxbBean != null) { // document&wrapped
        Class responseBeanClass = appLoader.loadClass(jaxbBean);
        ParameterMapping wrapperElement = new ParameterMapping();
        wrapperElement.setSchemaQName(new QName(opNS,opLoc));
        wrapperElement.setIsElement(true);
        ParameterObject tempParam = new ParameterObject();
        tempParam.parameterType = responseBeanClass;
        tempParam.parameterValue = null;
        deserializeParameterJAXB(sreader,tempParam,wrapperElement,soapHeaderList,unmarshaller,jaxbContext,attachmentHandler);
        // read the response wrapper contents
        HashMap<QName,Object> fields = getJAXBElementsNew(responseBeanClass);        
        for (int i = 0; i < params.length; i++) {
          ParameterMapping param = params[i];
            if (param.isHeader()) {
              deserializeParameterJAXB(sreader, paramObjs.get(i), param, soapHeaderList,unmarshaller,jaxbContext,attachmentHandler);
            } else {
              QName fieldQName = new QName(param.getNamespace(),param.getWSDLParameterName());
              setParameterContent(getResponseField(fieldQName,fields,tempParam.parameterValue), paramObjs.get(i));
            }
        }                        
      } else { // document&bare and rpc/literal
        boolean isRpc = "rpc".equals(style); // i044259 
        if (isRpc) {
          // enters in response element his name doesn't matte          
          sreader.nextTag(); 
        }
        boolean[] foundParams = new boolean[params.length];
        int paramCount = params.length;
        do {
          boolean responseParamFound = false;
          for(int i = 0; i < params.length; i++) {
            if (sreader.isWhiteSpace()){
              sreader.next();
            } else if (sreader.isCharacters()) {
              //TODO: Replace with Transport binding exception
              throw new RuntimeException("Unexpected content - characters; expected start/end element");
            }
            if(!foundParams[i]) {
              ParameterMapping param = params[i];
              if(deserializeParameterJAXB(sreader, paramObjs.get(i), param, soapHeaderList, unmarshaller, jaxbContext, attachmentHandler)) {
                foundParams[i] = true;
                responseParamFound = true;
                --paramCount;
              }
            }
          }
          if(!responseParamFound) {
            break;
          }
        } while(paramCount>0);        
        for(int i = 0; i < params.length; i++) {
          if(!foundParams[i]) {
            throw new TransportBindingException(TransportBindingException.MISSING_RETURN_PARAMETER, params[i].getWSDLParameterName());
          }
        }
        /*        
        for (int i = 0; i < params.length; i++) {
          ParameterMapping param = params[i];
          if (sreader.isWhiteSpace()){
            sreader.next();
          } else if (sreader.isCharacters()) {
            // throw some ugly exception explaining the problem
            //throw new RuntimeException("Unexpected content [" + sreader.getTextCharacters() + "]");
            throw new RuntimeException("Unexpected content - characters; expected start/end element");
          }
          deserializeParameterJAXB(sreader, paramObjs.get(i), param, soapHeaderList,unmarshaller,jaxbContext,attachmentHandler);
//          }
        }*/
        if (isRpc) {
          // leaves response element his name doesn't matter
          sreader.nextTag();
        }
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
  
  
  private static void setRequestField(QName fieldQName, HashMap<QName,Object> fieldDescritors,Object parameterValue,Class parameterType,Object requestBean)  throws WebServiceException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Object requestBeanField = fieldDescritors.get(fieldQName);
    if (requestBeanField == null) {
      throw new javax.xml.ws.WebServiceException("Field "+fieldQName+" could not be found in the outbound bean "+requestBean.getClass().getName());
    }
    if (requestBeanField instanceof Field) {
      Field f = (Field) requestBeanField;
      if (JAXBElement.class.isAssignableFrom(f.getType())) {
        parameterValue = new JAXBElement(fieldQName,parameterType,parameterValue);
      }
      f.set(requestBean,parameterValue);
    }
    if (requestBeanField instanceof PropertyDescriptor) {
      PropertyDescriptor p = (PropertyDescriptor) requestBeanField;
      if (JAXBElement.class.isAssignableFrom(p.getPropertyType())) {
        parameterValue = new JAXBElement(fieldQName,parameterType,parameterValue);
      }
      Method m = p.getWriteMethod();
      m.invoke(requestBean,new Object[] {parameterValue});
    }
  }
  
  public static Object getResponseField(QName fieldQName, HashMap<QName,Object> fieldDescritors, Object responseBean)  throws WebServiceException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    Object responseBeanField = fieldDescritors.get(fieldQName);
    //heuristics needed for backward compatibility
    if (responseBeanField == null && (fieldQName.getNamespaceURI() == null || fieldQName.getNamespaceURI().length() < 1)) {
      responseBeanField = getByLocalName(fieldDescritors, fieldQName);
    }
    if (responseBeanField == null) {
      throw new javax.xml.ws.WebServiceException("Field "+fieldQName+" could not be found in the outbound bean "+responseBean.getClass().getName());
    }
    if (responseBeanField instanceof Field) {
      Field f = (Field) responseBeanField;
      Object filedValue = f.get(responseBean);      
      return (filedValue instanceof JAXBElement ? ((JAXBElement)filedValue).getValue() : filedValue);
    }
    if (responseBeanField instanceof PropertyDescriptor) {
      PropertyDescriptor p = (PropertyDescriptor) responseBeanField;
      Method m = p.getReadMethod();
      Object filedValue = m.invoke(responseBean, null);
      return(filedValue instanceof JAXBElement ? ((JAXBElement)filedValue).getValue() : filedValue);
    }
    return null;
  }  
  
  private static Object getByLocalName(HashMap<QName,Object> fieldDescritors, QName fieldQName) {
    Iterator<QName> keys = fieldDescritors.keySet().iterator();
    QName tmp;
    while (keys.hasNext()) {
      tmp = keys.next();
      if (fieldQName.getLocalPart().equals(tmp.getLocalPart())) {
        return fieldDescritors.get(tmp);
      }
    }
    return null;
  }
  /**
   * Returns the namespace for specific package.
   * @param packageName
   * @return
   */
  public static String getPackageNamespace(Package packageName) {
    XmlSchema packageMapping = packageName.getAnnotation(XmlSchema.class);    
    if (packageMapping != null) {
      return packageMapping.namespace();
    } else {
      NameConvertor temp = new NameConvertor();
      return temp.packageToUri(packageName);
    }
  }
  
  public static final String DEFAULT = "##default";
  
  /**
   * Returns the namespace of the type
   * @param containerClass
   * @return
   * @throws Exception
   */
  private static String getElementNamespace(Class containerClass) {
    String result = null;
    XmlRootElement xmlRootElement = (XmlRootElement) containerClass.getAnnotation(XmlRootElement.class);
    if (xmlRootElement != null) {
      result = xmlRootElement.namespace();
      if (DEFAULT.equals(result)) { // Get the default namespace
        result = getPackageNamespace(containerClass.getPackage());
      }
    }
    return result;
  }
  
  /**
   * Returns the namespace of the type annotated with @XmlType
   * @param containerClass
   * @return
   */
  private static String getTypeNamespace(Class containerClass) {
    String result = null;
    XmlType typeAnnotation = (XmlType) containerClass.getAnnotation(XmlType.class);
    if (typeAnnotation != null) {
      String name = typeAnnotation.name();
      result = typeAnnotation.namespace();
      if (DEFAULT.equals(result)) { // Get the default namespace
        if (name.length() == 0) { // name=""
          if (containerClass.getAnnotation(XmlRootElement.class) != null) {
            result = getElementNamespace(containerClass);
          }
        } else {
          result = getPackageNamespace(containerClass.getPackage());
        }
      }
    }
    return result;
  }
  
  private static final QName extraxtXMLElementName(XmlElement elementAnnotation, XmlElementRef elementRefAnnotation, String javaName, Class containerClass) {
    String localName = null;
    String namespace = null;
    //Both anotations are null - get info from java.
    if (elementAnnotation == null && elementRefAnnotation == null) {
      localName = javaName;
      XmlSchema packageMapping = containerClass.getPackage().getAnnotation(XmlSchema.class);
      if (packageMapping != null) {
        if (packageMapping.elementFormDefault() == XmlNsForm.QUALIFIED) {
          namespace = getPackageNamespace(containerClass.getPackage());          
        }
      }  
      // The elementAnnotation is present.
    } else if (elementAnnotation != null) {
      // Extracts the element local name.
      localName = elementAnnotation.name();
      namespace = elementAnnotation.namespace();
      if ("##default".equals(localName)) {
        localName = javaName;
      }
      // Extracts the element namespace      
      if ("##default".equals(namespace)) {
        namespace = null;
        XmlSchema packageMapping = containerClass.getPackage().getAnnotation(XmlSchema.class);
        if (packageMapping != null) {
          if (packageMapping.elementFormDefault() == XmlNsForm.QUALIFIED) {
            namespace = getTypeNamespace(containerClass);          
          }
        }
      }
      // The elementRefAnotation is present. 
    }else{
      // Extracts the element local name.
      localName = elementRefAnnotation.name();
      namespace = elementRefAnnotation.namespace();
      if ("##default".equals(localName)) {
        localName = javaName;
      }
      // Extracts the element namespace      
      if ("##default".equals(namespace)) {
        namespace = null;
        XmlSchema packageMapping = containerClass.getPackage().getAnnotation(XmlSchema.class);
        if (packageMapping != null) {
          if (packageMapping.elementFormDefault() == XmlNsForm.QUALIFIED) {
            namespace = getTypeNamespace(containerClass);          
          }
        }
      }
    }//else
    
    
    
    
    return new QName(namespace,localName);
  }
  
  public static final PropertyDescriptor checkProperty(String propertyName, PropertyDescriptor[] propDescriptors) {
    // i044259
    boolean hacked = false;
    if ("_return".equals(propertyName)) {
      propertyName = "return";
      hacked = true;
    }
    for (PropertyDescriptor p: propDescriptors) {
      if (propertyName.equals(p.getName())) {
        if (hacked) {
          if (p.getReadMethod() != null && p.getWriteMethod() != null) {
            return p;
          } 
        } else {
          return p;
        }
      }
    }
    return null;
  }
  
  public static final HashMap<QName,Object> getJAXBElementsNew(Class<?> jaxbClass) throws Exception {
    HashMap<QName,Object> result = new HashMap<QName,Object>();
    // Gest fields and properties.
    Field[] fields = jaxbClass.getDeclaredFields();
    BeanInfo beanInfo = Introspector.getBeanInfo(jaxbClass);
    PropertyDescriptor[] propDescriptors = beanInfo.getPropertyDescriptors();    
    XmlAccessType accessValue = XmlAccessType.PUBLIC_MEMBER;    
    if (jaxbClass.getAnnotation(XmlAccessorType.class) != null) {
      XmlAccessorType accessorType = (XmlAccessorType) jaxbClass.getAnnotation(XmlAccessorType.class);
      accessValue = accessorType.value();      
    }
    for (int i=0; i < fields.length; i++) {
      // The field is transiend
      Field f = fields[i];
      if (f.getAnnotation(XmlTransient.class) != null) {        
        continue;
      }
      int modifier = f.getModifiers();      
      // if ((Modifier.isProtected(modifier) || Modifier.isPublic(modifier)) && !Modifier.isStatic(modifier) && !Modifier.isFinal(modifier)) {
      if (!Modifier.isStatic(modifier) && !Modifier.isFinal(modifier)) {  
        // Field is protected or public and is not static or final
        XmlElement elementAnnotation = f.getAnnotation(XmlElement.class);
        XmlElementRef elementRefAnnotation = f.getAnnotation(XmlElementRef.class);
        if ((elementAnnotation == null && elementAnnotation == null) && (accessValue == XmlAccessType.NONE || accessValue == XmlAccessType.PROPERTY)) {
          // Accessor Type is NONE or PROPERTY and the field is not annotated with  @XmlElement annotation
          continue;  
        }
        String javaName = f.getName();
        QName elementName = extraxtXMLElementName(elementAnnotation, elementRefAnnotation,javaName,jaxbClass);
        boolean isProtected = false;
        if (Modifier.isProtected(modifier) || Modifier.isPrivate(modifier)) {
          try {        
            Method m = f.getClass().getMethod("setAccessible",new Class[] {boolean.class});
            m.invoke(f,new Object[] {Boolean.TRUE});
            //f.setAccessible(true);//$JL-CALLEE$
          } catch (Exception x) {
            isProtected = true;
          }
        }
        if (!isProtected) {
          result.put(elementName,f);                  
        }
    }
    }
    for (int i=0; i < propDescriptors.length; i++) {
      PropertyDescriptor p = propDescriptors[i];
      if (p.getReadMethod() != null && p.getWriteMethod() != null) {
        Method m = p.getReadMethod();
        if (m.getAnnotation(XmlTransient.class) != null) {
          continue;
        }
        XmlElement elementAnnotation = m.getAnnotation(XmlElement.class);
        XmlElementRef elementRefAnnotation = m.getAnnotation(XmlElementRef.class);
        
        if ((elementAnnotation == null && elementRefAnnotation == null) && (accessValue == XmlAccessType.NONE || accessValue == XmlAccessType.FIELD)) {
          continue;
        }
        String javaName = p.getName();
        QName elementName = extraxtXMLElementName(elementAnnotation, elementRefAnnotation, javaName, jaxbClass);
        result.put(elementName,p);        
      }
    }
    return result;
  }
  
  /*public static final HashMap<QName,Object> getJAXBElements(Class jaxbClass) throws Exception {    
    HashMap<QName,Object> result = new HashMap<QName,Object>();
    // Gets all fields
    Field[] fields = jaxbClass.getDeclaredFields();
    BeanInfo beanInfo = Introspector.getBeanInfo(jaxbClass);
    PropertyDescriptor[] propDescriptors = beanInfo.getPropertyDescriptors();    
    for (int i=0; i<fields.length; i++) {
      if (fields[i].getAnnotation(XmlTransient.class) != null) {
        continue;
      }
      String javaName = fields[i].getName();
      StringBuffer strBuf = new StringBuffer(javaName.length());      
      strBuf.append(Character.toUpperCase(javaName.charAt(0)));
      strBuf.append(javaName.substring(1));
      javaName = Introspector.decapitalize(strBuf.toString());
      PropertyDescriptor p = checkProperty(javaName,propDescriptors);            
      if (p != null && p.getWriteMethod() != null) {
        XmlElement annotation = fields[i].getAnnotation(XmlElement.class);         
        if (p.getReadMethod() != null) {
          Method m = p.getReadMethod();
          if (m.getAnnotation(XmlTransient.class) != null) {
            continue;
          }
          if (m.getAnnotation(XmlElement.class) != null) {
            annotation = m.getAnnotation(XmlElement.class);
          }
        }
        QName elementName = extraxtXMLElementName(annotation,javaName,jaxbClass);
        result.put(elementName,p);
      } else {
        int modifier = fields[i].getModifiers();
        if ((Modifier.isProtected(modifier) || Modifier.isPublic(modifier)) && !Modifier.isStatic(modifier) && !Modifier.isFinal(modifier)) {
          XmlElement annotation = fields[i].getAnnotation(XmlElement.class);
          QName elementName = extraxtXMLElementName(annotation,javaName,jaxbClass);
          boolean isProtected = false;
          if (Modifier.isProtected(modifier)) {
            try {                     
              Method m = fields[i].getClass().getMethod("setAccessible",new Class[] {boolean.class});
              m.invoke(fields[i],new Object[] {Boolean.TRUE});              
              //fields[i].setAccessible(true); //$JL-CALLEE$
            } catch (Exception e) {
              isProtected = true;
            }
          }
          if (!isProtected) {
            result.put(elementName,fields[i]);
          }
        }
      }
    }        
    return result;    
  }*/
  
  
  
  
}
