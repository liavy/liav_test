package com.sap.engine.services.webservices.espbase.server.additions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.UnmarshalException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;
import javax.xml.rpc.encoding.TypeMapping;

import org.xml.sax.Attributes;

import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.attachment.impl.AttachmentContainer;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ProcessException;
import com.sap.engine.services.webservices.espbase.server.api.ProviderAttachmentHandlerFactory;
import com.sap.engine.services.webservices.jaxrpc.encoding.DeserializerBase;
import com.sap.engine.services.webservices.jaxrpc.encoding.PropertyList;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPDeserializationState;
import com.sap.engine.services.webservices.jaxrpc.encoding.SOAPSerializationContext;
import com.sap.engine.services.webservices.jaxrpc.encoding.SerializerBase;
import com.sap.engine.services.webservices.tools.InstancesPool;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class StreamEngineMIME {
 
  //Operatin modes
  private static final String ENCODED_USE = "encoded";
  //The response body wrapper element suffix
  private static final String RESPONSE_WRAPPER_SUFFIX = "Response";
  //The response body wrapper element prefix
  private static final String RESPONSE_ELEMENT_PREFIX = "rpl";
  //binary Content-Transfer-Encoding
  private static final String BINARY_CONTENT_TRANSFER_ENCONDING  =  "binary";
  //base64 Content-Transfer-Encoding
  private static final String BASE64_CONTENT_TRANSFER_ENCONDING  =  "base64";
  //8bit Content-Transfer-Encoding
  private static final String EIGTHBIT_CONTENT_TRANSFER_ENCONDING  =  "8bit";

  private static InstancesPool deserializationContextPool = new InstancesPool();
  private static InstancesPool serializationContextPool = new InstancesPool();

  /**
   * Converts the messageBody xml content to java Objects.
   * The reader is possitioned on the operation tag
   */
  public static final Object[] deserialize(Class[] mParams, ClassLoader loader, ProviderContextHelper ctx) throws RuntimeProcessException {
    MIMEMessage message = (MIMEMessage) ctx.getMessage(); 
    XMLTokenReader reader = message.getBodyReader();
  
    if (reader.getState() != XMLTokenReader.STARTELEMENT) {
      throw new ProcessException(ExceptionConstants.READER_NOT_ON_START_ELEMENT);
    }
    //substract 1 since on startElement it is increase by the parser
    int opElemBaseXMLLevel = reader.getCurrentLevel() - 1;

    boolean isNW04 = ctx.getDynamicContext().getProperty(MIMEHTTPTransportBinding.NW04_REQUEST) != null;
    OperationMapping operation = ctx.getOperation(); 
     
    ParameterMapping[] mappingParams = operation.getParameters(ParameterMapping.IN_TYPE);
//    System.out.println("StreamEngineMIME param length: " + virtualParams.length);

    TypeMapping typeMapping  = ctx.getStaticContext().getTypeMappingRegistry().getDefaultTypeMapping();
    SOAPDeserializationContext context = getSOAPDeserializationContext(typeMapping, loader);

    Object[] result = new Object[mappingParams.length];

    QName currentElementQName;
    ParameterMapping currentParam;
    Class currentClass;
    Object currentObject;

    for (int i = 0; i < result.length; i++) {
      currentParam = mappingParams[i];
      currentClass = mParams[i];
      currentObject = null;

      if (currentParam.isExposed()) {
        if (currentParam.isAttachment()) {
          if (isNW04) {
            currentElementQName = getNextElementQName(reader); //move to the soap:body child for this attachment            
            if (! currentElementQName.getLocalPart().equalsIgnoreCase(currentParam.getWSDLParameterName())) {
              throw new ProcessException(ExceptionConstants.PARAMETER_NAME_MISMASH, new Object[]{currentElementQName.getLocalPart(), currentParam.getWSDLParameterName()});
            }
            currentObject = createObject(reader.getAttributes(), message, currentClass, currentParam.getWSDLParameterName());
          } else {
            currentObject = createObjectAP10(message, currentClass, currentParam);
          }
        } else {
          currentElementQName = getNextElementQName(reader); //move to the soap:body child for this attachment            
          if (! currentElementQName.getLocalPart().equalsIgnoreCase(currentParam.getWSDLParameterName())) {
            throw new ProcessException(ExceptionConstants.PARAMETER_NAME_MISMASH, new Object[]{currentElementQName.getLocalPart(), currentParam.getWSDLParameterName()});
          }
          currentObject = doXmlToJava(reader, currentClass, currentParam, context);          
        }
      } else { //this is hidden param
        currentObject = doXmlToJava(currentParam.getDefaultValue(), currentClass, currentParam, context);
      }
      result[i] = currentObject;
    }
    if (isNW04) {
      result = deserializeRemainingElements(result, reader, context, opElemBaseXMLLevel, operation.getWSDLOperationName());
    }
    //clears and returns the context in the pool
    context.clearContext();
    deserializationContextPool.rollBackInstance(context);

    return result;
  }

  /**
   * The writer param is on the SOAPBody element.
   * Here the body content is filled
   */
  public static void serialize(Object returnObject, Class objectClass, ProviderContextHelper ctx) throws ProcessException {
    try {
      OperationMapping operation = ctx.getOperation();
      String namespace = operation.getProperty(OperationMapping.OUTPUT_NAMESPACE);

      MIMEMessage msg = (MIMEMessage) ctx.getMessage();
      XMLTokenWriter writer = msg.getBodyWriter();
      //appends the body content wrapper element
      writer.enter(namespace, operation.getWSDLOperationName() + RESPONSE_WRAPPER_SUFFIX);

      ParameterMapping[] returnParams = operation.getParameters(ParameterMapping.RETURN_TYPE);
      if (returnParams.length == 0) { //nothing to be returned
        writer.leave(); //leaves the body content wrapper element
        return;
      }

      boolean isNW04 = ctx.getDynamicContext().getProperty(MIMEHTTPTransportBinding.NW04_REQUEST) != null;

      if (returnParams.length == 1) {
        ParameterMapping returnParam = returnParams[0];
        if (returnParam.isAttachment()) {
          if (isNW04) {
            //appends the returnType wrapper element
            writer.enter(null, returnParam.getWSDLParameterName());
            //in case of null
            if (returnObject == null) {
              writer.writeAttribute(NS.XSI, "nil", "true");
              writer.leave(); //the return type wrapper
              writer.leave(); //body content wrapper
            } else {
              String cid = getCIDUniqueValue(returnParam.getWSDLParameterName());
              appendAttachmentPart(msg, objectClass, returnObject, returnParam, cid);     
              writer.writeAttribute("", "href", "cid:" + cid);
              writer.leave(); //The return type wrapper
              writer.leave(); //for the body content wrapper.                
            }
          } else { //AP1.0 processing
            String cid = getCIDAP10(returnParam.getWSDLParameterName());
            appendAttachmentPart(msg, objectClass, returnObject, returnParam, cid);                 
          }
        } else {
          TypeMapping typeMapping  = ctx.getStaticContext().getTypeMappingRegistry().getDefaultTypeMapping();
          String use = "";
          if (isNW04){
            use = ENCODED_USE;
          }
          //appends the returnType wrapper element
          writer.enter(null, returnParam.getWSDLParameterName());
          if (returnObject == null) {
            writer.writeAttribute(NS.XSI, "nil", "true");
            writer.leave(); //the return type wrapper
            writer.leave(); //the the body content wrapper
          } else {
            SOAPSerializationContext context = getSOAPSerializationContext(typeMapping, use);
            fillResponseElement(writer, returnObject, objectClass, returnParam, context);
            writer.leave(); //for the body content wrapper.
            //rollback the context instance.
            context.clearContext();
            serializationContextPool.rollBackInstance(context);
          }
        }
      } else {
        throw new ProcessException(ExceptionConstants.INOUT_PARAMETERS_NOTSUPPORTED);
      }
    } catch (Exception schE) {
      throw new ProcessException(schE);
    }
  }

  private static void fillResponseElement(XMLTokenWriter writer, Object returnObject, Class objectClass, ParameterMapping param, SOAPSerializationContext context) throws ProcessException {
    try {
      if (objectClass == Void.TYPE) {//if method is void
        writer.leave();
        return;
      }
      QName  qname = param.getSchemaQName();
      SerializerFactory serializerFactory = context.getTypeMapping().getSerializer(objectClass, qname);
      if (serializerFactory == null) {
        throw new ProcessException(ExceptionConstants.NO_SERIALIZER_FOUND, new Object[]{qname, context.getTypeMapping()});
      }
      SerializerBase serializer = (SerializerBase) serializerFactory.getSerializerAs("");
      serializer.serialize(returnObject, writer, context);
    } catch (java.rmi.MarshalException mE) {
      throw new ProcessException(ExceptionConstants.SERIALIZING_FAILS, new Object[]{returnObject, mE.getLocalizedMessage()}, mE);
    } catch (IOException ioE) {
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
    }
  }

  //xmlLevel is the level on which the operation element is on
  private static Object[] deserializeRemainingElements(Object source[], XMLTokenReader reader, SOAPDeserializationContext content, int xmlLevel, String opName) throws ProcessException {
    try {
//      System.out.println("StreamEngine deserializeRemainingElements");
      while (true) {
        int code = reader.next();
        if ((code == XMLTokenReader.ENDELEMENT) && reader.getCurrentLevel() == xmlLevel) { 
          reader.next();
          break;
        }
        if (code == XMLTokenReader.EOF) {
          throw new ProcessException(ExceptionConstants.EOF_END_OPERATION_TAG, new Object[]{opName});
        }
      }
    } catch (ParserException pE) {
      throw new ProcessException(ExceptionConstants.PARSER_EXCEPTION_IN_REQUEST_PARSING, new Object[]{pE.getLocalizedMessage()}, pE);
    }
    try {
      content.deserializeRemainingElements(reader);
    } catch (UnmarshalException umE) {
      throw new ProcessException(ExceptionConstants.DESERIALIZING_REFERENCE_FAILS, new Object[]{umE.getLocalizedMessage()}, umE);
    }

    SOAPDeserializationState instanceWrapper;
    Object instanceWrapperObject;

    for (int i = 0; i < source.length; i++) {
      if (source[i] instanceof SOAPDeserializationState) {
        instanceWrapper = (SOAPDeserializationState) source[i];
        if (instanceWrapper.isComplete()) {
          instanceWrapperObject = instanceWrapper.getInstance();
//          System.out.println("StreamEngineMIME instance: " + instanceWrapperObject);
          source[i] = instanceWrapperObject;
        } else {
          throw new ProcessException(ExceptionConstants.UNRESOLVED_REFERENCE, new Object[]{instanceWrapper.getResultClass()});
        }
      }
    }

    return source;
  }

  private static Object doXmlToJava(Object xmlSource, Class mParam, ParameterMapping param, SOAPDeserializationContext context) throws ProcessException {

    QName qname = param.getSchemaQName();
    DeserializerFactory deserializerFactory = context.getTypeMapping().getDeserializer(mParam, qname);
    if (deserializerFactory == null) {
      throw new ProcessException(ExceptionConstants.NO_DESERIALIZER_FOUND, new Object[]{qname, context.getTypeMapping()});
    }

    DeserializerBase deserializer = (DeserializerBase) deserializerFactory.getDeserializerAs("");
//      System.out.println("StreamEngine doXMLToJavadeserialize(234) class: " + mParam );
//      System.out.println("StreamEngine doXMLToJavadeserialize(234) deserializer: " + deserializer);
//      System.out.println("StreamEngine doXMLToJavadeserialize(234) context: " + context);

    try {
      if (xmlSource instanceof XMLTokenReader) {
        return deserializer.deserialize((XMLTokenReader) xmlSource, context, mParam);
      } else {
        return deserializer.deserialize((String) xmlSource, context, mParam);
      }
    } catch (java.rmi.UnmarshalException umE) {
      throw new ProcessException(ExceptionConstants.DESERIALIZING_FAILS, new Object[]{umE.getLocalizedMessage()}, umE);
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
    if (use.equals(ENCODED_USE)) {
      context.setProperty(PropertyList.USE_ENCODING, "true");
    }

    return context;
  }

  private static QName getNextElementQName(XMLTokenReader reader) throws ProcessException {
    try {
      while (reader.next() != XMLTokenReader.STARTELEMENT) {
        if (reader.getState() == XMLTokenReader.EOF) {
          throw new ProcessException(ExceptionConstants.EOF_NEXT_ELEMENT_START);
        }
      }
      return new QName(reader.getURI(), reader.getLocalName());
    } catch (ParserException pE) {
      throw new ProcessException(ExceptionConstants.PARSER_EXCEPTION_IN_REQUEST_PARSING, new Object[]{pE.getLocalizedMessage()}, pE);
    }
  }

  /**
   * Resolves part references and creates object.
   * Reader is on startElement.
   */
  private static Object createObject(Attributes attrs, MIMEMessage mimeMessage, Class cl, String partName) throws ProcessException {
    String value = attrs.getValue("href");

    if (value == null) {//no href
      throw new ProcessException(ExceptionConstants.CANNOT_FIND_REFERENCED_ATTACHMENT, new Object[]{partName, value});
    }

    Attachment att = mimeMessage.getAttachmentContainer().getAttachment(value);
    if (att == null) { // could not resolve the reference
      throw new ProcessException(ExceptionConstants.CANNOT_FIND_REFERENCED_ATTACHMENT, new Object[]{partName, value + ", container: " + mimeMessage.getAttachmentContainer()});
    }
    
    return createObject0(att, cl);
  }

  /**
   * Resolves attachment using AP1.0 algorithm.
   */
  private static Object createObjectAP10(MIMEMessage mimeMessage, Class cl, ParameterMapping param) throws ProcessException {
    String escPName = escapePartNameAP10(param.getWSDLParameterName());
    Attachment att = getPartByCIDStart(escPName, ProviderAttachmentHandlerFactory.getAttachmentHandler().getInboundAttachments());
    if (att == null) { // could not resolve the reference
      throw new ProcessException(ExceptionConstants.CANNOT_FIND_REFERENCED_ATTACHMENT, new Object[]{param.getWSDLParameterName(), param.getWSDLParameterName()});
    }
    return createObject0(att, cl);
  }

  private static String createStringFromStream(InputStream inputStream, String contentTypeValue) throws ProcessException {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream(128);
      byte[] arr = new byte[128];
      int b;

      while ((b = inputStream.read(arr)) != -1) {
        buffer.write(arr, 0, b);
      }
      inputStream.close();

      String enc = getEncoding(contentTypeValue);
      return buffer.toString(enc);
    } catch (IOException e) {
      throw new ProcessException(e);
    }
  }

  private static byte[] createByteArrayFromStream(InputStream inputStream) throws ProcessException {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream(128);
      byte[] arr = new byte[128];
      int b;

      while ((b = inputStream.read(arr)) != -1) {
        buffer.write(arr, 0, b);
      }
      inputStream.close();
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new ProcessException(e);
    }
  }

  private static String getEncoding(String contentTypeValue) {
    int sti = contentTypeValue.indexOf("charset=");
    if (sti > -1) {
      String newValue = contentTypeValue.substring(sti + 8);
      int end = newValue.indexOf(";");
      if (end == -1) return newValue;
      return newValue.substring(0, end);
    }
    return "utf-8";
  }

  private static String getCIDUniqueValue(String partName) {
    long ln = System.currentTimeMillis();
    return partName + Long.toString(ln);
  }
  
  /**
   * Creates object from part stream.
   */
  private static Object createObject0(Attachment att, Class cl) throws ProcessException {
    try {
      if (cl == String.class) {
          return createStringFromStream(getAttachmentInputStream(att), att.getContentType());
      } else if (cl.isArray() && cl.getComponentType() == byte.class) {
          return createByteArrayFromStream(getAttachmentInputStream(att));
      } else {
        throw new ProcessException(ExceptionConstants.UNSUPPORTED_MIME_OBJECT, new Object[]{cl});
      }
    } catch (IOException ioE) {
      throw new ProcessException(ExceptionConstants.CREATING_OBJECT_FROM_PART_CONTENT, new Object[]{ioE.getLocalizedMessage()}, ioE);
    }    
  }
  
  private static InputStream getAttachmentInputStream(Attachment a) throws IOException {
    Object o = a.getContentObject();
    if (o instanceof DataHandler) {
      return ((DataHandler) o).getInputStream();
    } else {
      return new ByteArrayInputStream((byte[]) o);
    }
  }
  /**
   * Appends attachment part to the <code>msg</code>.
   * @param msg to which the attachment is appended.
   * @param content object representing attachment content. Currently byte[] and java.lang.String are accepted.
   * @param param description of attachment parameter.
   * @param cid 'content-id' value of the attachment
   */  
  private static void appendAttachmentPart(MIMEMessage msg, Class cls, Object content, ParameterMapping param, String cid) throws ProcessException {
    //must have content-type
    String contentType = param.getAttachmentContentType();
    //must have transfer encoding
    String transferEncoding = param.getAttachmentTransferEncoding();
    if (transferEncoding == null) {
      throw new ProcessException(ExceptionConstants.NOT_SUPPORTED_TRANSFER_ENCODING, new Object[]{"null"});
    }
    String charset = null;
    byte[] attachmentContent;
    if (cls == String.class) {
      if (transferEncoding.equals(EIGTHBIT_CONTENT_TRANSFER_ENCONDING)) {
        if (content == null) { //set empty object
          content = new String();
        }
        try {
          charset = "utf-8"; 
          attachmentContent = ((String) content).getBytes(charset); //$JL-I18N$
        } catch (Exception e) {
          throw new ProcessException(e);
        }
      } else {
        throw new ProcessException(ExceptionConstants.NOT_SUPPORTED_TRANSFER_ENCODING, new Object[]{transferEncoding});
      }
    } else if (cls.isArray() && cls.getComponentType() == byte.class) {
      if (content == null) {
        content = new byte[0]; //set empty object
      }
      if (transferEncoding.equals(BINARY_CONTENT_TRANSFER_ENCONDING)) {
        attachmentContent = (byte[]) content;
      } else if (transferEncoding.equals(BASE64_CONTENT_TRANSFER_ENCONDING)) {
        attachmentContent = com.sap.engine.lib.xml.util.BASE64Encoder.encode((byte[]) content);
      } else {
        throw new ProcessException(ExceptionConstants.NOT_SUPPORTED_TRANSFER_ENCODING, new Object[]{transferEncoding});
      }
    } else {
      throw new ProcessException(ExceptionConstants.UNSUPPORTED_MIME_OBJECT, new Object[]{content});
    }
    //creating attachment part
    Attachment att = msg.getAttachmentContainer().createAttachment();
    //set headers
    if (charset != null) {
      contentType += "; charset=" + charset;
    }
    att.setContentType(contentType);
    att.setContentId(cid);
    att.setMimeHeader("Content-Transfer-Encoding", transferEncoding);
    //set content
    att.setContentAsByteArray(attachmentContent);
    //appending the attachment
    msg.getAttachmentContainer().addAttachment(att);
  }
  
  static String getCIDAP10(String partName) throws ProcessException {
    String escapedPartName = escapePartNameAP10(partName);
    escapedPartName += "="; 
    escapedPartName += System.currentTimeMillis(); //add UUID
    escapedPartName += "@sap.com";
    return escapedPartName;
  }
  
  /**
   * Returned escaped <code>partName</code> value, according the algorithm described in AP1.0(section 3.8)
   */
  static String escapePartNameAP10(String partName) throws ProcessException {
    char c;
    byte[] bArr;
    String tmpStr;
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < partName.length(); i++) {
      c = partName.charAt(i);
      if (c > 0x7F) {
        try {
          bArr = String.valueOf(c).getBytes("utf-8");        
        } catch (Exception e) {
          throw new ProcessException(e);
        }
        for (int b = 0; b < bArr.length; b++) {
          tmpStr = Integer.toHexString(bArr[b]).substring(6); //only 6 and 7 possition are meaningful
          buf.append("%").append(tmpStr);
        }
      } else {
        buf.append(c);
      }
    }
    return buf.toString();
  }
  
  /**
  * Cheking the cids values for one that starts with <code>cidPref</code>.
  * Returns part if any found to start with <code>cidPref</code>, otherwise null.
  */
  static Attachment getPartByCIDStart(String cidPref, Set atts) {
    cidPref = cidPref.toLowerCase(Locale.ENGLISH);
    Iterator itr = atts.iterator();
    Attachment att;
    String newCidPref = cidPref + "="; //this is according to AP1.0
    while (itr.hasNext()) {
      att = (Attachment) itr.next();
      String cid = att.getContentId();
      if (cid != null) {
        if (cid.toLowerCase(Locale.ENGLISH).startsWith(newCidPref)) { //it is lowerCased because the http header values are not case sensitive...
          return att;
        }
      }
    }
    return null;
  }

}