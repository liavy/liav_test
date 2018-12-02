package com.sap.engine.services.webservices.espbase.server.additions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Map;

import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.SOAPMessageImpl;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.TransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ProcessException;
import com.sap.engine.services.webservices.espbase.server.runtime.ProviderContextHelperImpl;
import com.sap.engine.services.webservices.tools.SoftReferenceInstancesPool;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class HTTPTransportBinding implements TransportBinding {
  /**
   * Constant, denoting the type of this transport binding implementation
   */
  public static final String TB_TYPE = "http";
  protected static final String ERROR_MESSAGE  = "HTTPTransportBinding" + "_" + "ErrorMessage";
  
  private static SoftReferenceInstancesPool<SOAPMessage> messages = new SoftReferenceInstancesPool<SOAPMessage>();

  private static final Location LOC = Location.getLocation(HTTPTransportBinding.class);
  
  public Message createInputMessage(ProviderContextHelper ctx) throws RuntimeProcessException {
    //taking configured HTTP method value
    String bType = ctx.getStaticContext().getInterfaceMapping().getBindingType();
    HTTPTransport transport = (HTTPTransport) ctx.getTransport();
    String verb = transport.getRequest().getMethod();
    if (bType.equals(InterfaceMapping.HTTPGETBINDING) && ! verb.equals("GET")) {
      throw new ProcessException(ExceptionConstants.WRONG_REQUEST_METHOD, new Object[]{"GET", verb});
    } else if (bType.equals(InterfaceMapping.HTTPPOSTBINDING) && ! verb.equals("POST")) {
      throw new ProcessException(ExceptionConstants.WRONG_REQUEST_METHOD, new Object[]{"POST", verb});
    }

    //does not return message instance, 
    return null;
  }

  public OperationMapping resolveOperation(ProviderContextHelper ctx) throws RuntimeProcessException {
    HTTPTransport transport = (HTTPTransport) ctx.getTransport();
    String requestedUri = transport.getRequest().getRequestURI();
    
    InterfaceMapping intfM = ctx.getStaticContext().getInterfaceMapping();
    if (intfM.isJAXWSProviderInterface()) {
      //Do not call the next method, in order the reader to be on the <Body> element.
     // getMessageKeysFromReader(message); //call this method in order to position the reader on first body element.
      return intfM.getOperationByJavaName("invoke"); //this type of interface has only one operation.
    }

    if (requestedUri != null) {
      if (requestedUri.endsWith("/")) {
        requestedUri = requestedUri.substring(0, requestedUri.length() - 1);
      }

      int del = requestedUri.lastIndexOf('/');
      if (del == -1) {
        throw new ProcessException(ExceptionConstants.UNABLE_TO_RESOLVE_OPERATION, new Object[]{requestedUri});
      }       
      OperationMapping opMapping = ctx.getStaticContext().getInterfaceMapping().getOperationByWSDLName(requestedUri.substring(del + 1, requestedUri.length()));
      if (opMapping != null) {
        return opMapping;
      } else {
        throw new ProcessException(ExceptionConstants.UNABLE_TO_RESOLVE_OPERATION, new Object[]{requestedUri});
      }
    }

    throw new ProcessException(ExceptionConstants.UNABLE_TO_RESOLVE_OPERATION, new Object[]{requestedUri});
  }

  public Object[] getParameters(Class[] methodClass, ClassLoader loader, ProviderContextHelper ctx) throws RuntimeProcessException {
    //check for JEEProvider
    InterfaceMapping intfM = ctx.getStaticContext().getInterfaceMapping();
    if (intfM.isJAXWSProviderInterface()) {
      return getParametersForJAXWSProvider(methodClass, ctx);
    }    

    HTTPTransport transport = (HTTPTransport) ctx.getTransport(); 
    HttpServletRequest request = transport.getRequest();
    Map values = request.getParameterMap();
    
    if (LOC.beDebug()) {
      LOC.debugT("getParameters(), http input params: " + values);    
    }
    OperationMapping operation = ctx.getOperation();
    ParameterMapping[] params = operation.getParameters(ParameterMapping.IN_TYPE);
    int length = params.length;

    Object[] result = new Object[length];
    String partName;
    ParameterMapping param;
    String extractedValue[];

    for (int i = 0; i < length; i++) {
      param = params[i];
      partName = param.getWSDLParameterName();

      if (LOC.beDebug()) {
        LOC.debugT("getParameters(), current parameter [" + i + "] " + param);    
      }

      if (param.isExposed()) {
        extractedValue = (String[]) values.get(partName);
        if (extractedValue == null) {
          throw new ProcessException(ExceptionConstants.MISSING_QUERY_PARAMETER, new Object[]{partName, extractedValue});
        }
        result[i] = createObject(methodClass[i], extractedValue, partName);
      } else {
        result[i] = createSimpleObject(methodClass[i], param.getDefaultValue());
      }
    }

    return result;
  }

  public Message initOutputMessage(ProviderContextHelper ctx) throws RuntimeProcessException {
    try{
      SOAPMessageImpl msg = (SOAPMessageImpl) messages.getInstance();
      if (msg == null) {
        msg = new SOAPMessageImpl();
      }      
      msg.setOutputEnvelopeElement(false);
      msg.initWriteMode(SOAPMessage.SOAP11_NS); //use SOAP1.1 till SOAP1.2 is officially supported.
      return msg;
    }catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Message createResponseMessage(Object returnObject, Class returnObjectClass, Object[] resultParams, Class[] resultParamsClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    SOAPMessageImpl msg = (SOAPMessageImpl) ctx.getMessage();
    
    InterfaceMapping intfM = ctx.getStaticContext().getInterfaceMapping();
    if (intfM.isJAXWSProviderInterface()) {
      return createResponseMessageForJAXWSProvider(msg, returnObject, returnObjectClass, resultParams, resultParamsClasses, ctx);
    }
    
    if (LOC.beDebug()) {
      LOC.debugT("createResponseMessage(), object to be serialized and returned: " + returnObject);    
    }
    StreamEngine.serialize(returnObject, returnObjectClass, StreamEngine.LITERAL_USE, SOAPHTTPTransportBinding.DOCUMENT, ctx);
    return msg;
  }

  public void sendResponseMessage(ProviderContextHelper ctx, int commPattern) throws RuntimeProcessException {
    try {
      HTTPTransport transport = (HTTPTransport) ctx.getTransport();
      HttpServletResponse response = transport.getResponse();
      if (! response.isCommitted()) {
        response.reset();
      }
      int msgSem = ctx.getMessageSemantic();
      if (msgSem == ProviderContextHelper.FAULT_MSG) {
        String faultErrorMessage = (String) ctx.getDynamicContext().getProperty(ERROR_MESSAGE);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, faultErrorMessage);
      } else if (msgSem == ProviderContextHelper.NORMAL_RESPONSE_MSG) {
        SOAPMessage msg = (SOAPMessage) ctx.getProperty(ProviderContextHelperImpl.MESSAGE);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/xml; charset=UTF-8");
        OutputStream output = response.getOutputStream();
        msg.writeTo(output);
      } else if (msgSem == ProviderContextHelper.BASIC_AUTHENTICATION_EXPECTED) {
        HttpServletRequest request = transport.getRequest();
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + request.getRequestURI() + "\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      } else {
        throw new ProcessException(ExceptionConstants.UNKNOWN_RESPONSE_MESSAGE_SEMANTIC, new Object[]{new Integer(msgSem), TB_TYPE});
      }
    } catch (java.io.IOException ioE) {
      throw new ProcessException(ExceptionConstants.IOEXCEPTION_IN_SENDING_RESPONSE, new Object[]{ioE.getLocalizedMessage()}, ioE);
    }
  }

  public void sendServerError(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException {
    try {
      HTTPTransport transport = (HTTPTransport) ctx.getTransport();
      HttpServletResponse response = transport.getResponse();
      if (! response.isCommitted()) {
        response.reset();
      }
      String localizedMessage = thr.getLocalizedMessage();
      if (localizedMessage == null) {
        localizedMessage = "null";
      }
      if (ctx.getMessageSemantic() == ProviderContextHelper.BASIC_AUTHENTICATION_EXPECTED) {
        HttpServletRequest request = transport.getRequest();
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + request.getRequestURI() + "\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      } else {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, localizedMessage);
      }
    } catch (java.io.IOException ioE) {
      throw new ProcessException(ExceptionConstants.IOEXCEPTION_IN_SENDING_RESPONSE, new Object[]{ioE.getLocalizedMessage()}, ioE);
    }
  }

  public Message createFaultMessage(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException {
    //set the fault string in dynamic context to be send as HTTP error 
    String faultErrorMessage = thr.getLocalizedMessage();
    if (faultErrorMessage == null) {
      faultErrorMessage = "null";
    }
    ctx.getDynamicContext().setProperty(ERROR_MESSAGE, faultErrorMessage);
    //no message instance is created    
    return null;
  }

  public void onContextReuse(ProviderContextHelper ctx) {
    SOAPMessage msg = (SOAPMessage) ctx.getProperty(ProviderContextHelperImpl.MESSAGE);
    if (msg != null) {
      msg.clear();
      messages.rollBackInstance(msg);
    }
  }
    
  public String getAction(ProviderContextHelper ctx) throws RuntimeProcessException {
    return null;
  }
  
  public int getCommunicationPattern(ProviderContextHelper ctx) throws RuntimeProcessException {
    return SYNC_COMMUNICATION;
  }
  
  public void sendAsynchronousResponse(ProviderContextHelper ctx) throws RuntimeProcessException {
  }
  
  
  public void sendMessageOneWay(String endpointURL, Message msg, String action) throws RuntimeProcessException {
  }
  
  private Object createObject(Class objClass, String[] objValue, String partName) throws ProcessException {
    if (objClass.isArray()) {
      Class arrComponent = objClass.getComponentType();
      if (arrComponent.isArray()) { //indicates multi-dimentional array
        throw new ProcessException(ExceptionConstants.FOUND_MULTI_DIMENTIONAL_ARRAY_PARAMETER, new Object[]{partName});
      }
      Object arrObject = Array.newInstance(arrComponent, objValue.length);
      for (int i = 0; i < objValue.length; i++) {
        Array.set(arrObject, i, createSimpleObject(arrComponent, objValue[i]));
      }

      return arrObject;
    } else {
      if (objValue.length != 1) {
        throw new ProcessException(ExceptionConstants.FOUND_MORE_THAN_ONE_QUERY_PARAMETER, new Object[]{Integer.toString(objValue.length), partName});
      }

      return createSimpleObject(objClass, objValue[0]);
    }
  }

  private Object createSimpleObject(Class objClass, String objValue) throws ProcessException {
    try {
      if (objClass == Integer.class || objClass == Integer.TYPE) {
        return new Integer(objValue);
      } else if (objClass == Short.class || objClass == Short.TYPE) {
        return new Short(objValue);
      } else if (objClass == Byte.class || objClass == Byte.TYPE) {
        return new Byte(objValue);
      } else if (objClass == Long.class || objClass == Long.TYPE) {
        return new Long(objValue);
      } else if (objClass == Float.class || objClass == Float.TYPE) {
        return new Float(objValue);
      } else if (objClass == Double.class || objClass == Double.TYPE) {
        return new Double(objValue);
      } else if (objClass == String.class) {
        return objValue;
      } else if (objClass == Boolean.class || objClass == Boolean.TYPE) {
        return new Boolean(objValue);
      } else if (objClass == Character.class || objClass == Character.TYPE) {
        return new Character(objValue.charAt(0));
      } else {
        throw new ProcessException(ExceptionConstants.NOT_SUPPORTED_PARAMETER_CLASS, new Object[]{objClass});
      }
    } catch (NumberFormatException nfE) {
      throw new ProcessException(ExceptionConstants.INSTANCE_CREATION_EXCEPTION, new Object[]{objClass, objValue, nfE.getLocalizedMessage()}, nfE);
    }
  }
  
  private Object[] getParametersForJAXWSProvider(Class[] methodClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    LOC.debugT("getParametersForJAXWSProvider(): entered...");
    try {
      final InputStream in = ((HTTPTransport) ctx.getTransport()).getRequest().getInputStream();
      if (methodClasses[0] == Source.class) { //this must be single class
        LOC.debugT("getParametersForJAXWSProvider(): JAXWSProviderInterface with class: " + Source.class);
        StreamSource ss = new StreamSource(in);
        return new Object[]{ss};
      } else if (methodClasses[0] == DataSource.class) {
        LOC.debugT("getParametersForJAXWSProvider(): JAXWSProviderInterface with class " + DataSource.class);
        DataSource ds = new DataSource() {

          public String getContentType() {
            return null;
          }

          public InputStream getInputStream() throws IOException {
            return in;
          }

          public String getName() {
            // TODO Auto-generated method stub
            return null;
          }

          public OutputStream getOutputStream() throws IOException {
            // TODO Auto-generated method stub
            return null;
          }
          
        };
        return new Object[]{ds};
      } else {
        throw new RuntimeProcessException("Unsupported JAXWSProvider parameter '" + methodClasses[0]);
      }
    } catch (Exception e) {
      throw new RuntimeProcessException(e);
    }
  }

  private Message createResponseMessageForJAXWSProvider(SOAPMessageImpl sMsg, Object returnObject, Class returnObjectClass, Object[] resultParams, Class[] resultParamsClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    try {
      if (returnObjectClass == Source.class) {
        if (returnObject == null) {
          throw new IllegalArgumentException("Unsupported case: JAXWS Provider.invoke() returns null.");
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        if (returnObject instanceof DOMSource) {
          //this must be element, since the SOAPBody is requried to have atleast one element...
          TransformerFactory.newInstance().newTransformer().transform((DOMSource) returnObject, new StreamResult(buf));
        } else if (returnObject instanceof StreamSource){
          StreamSource sSource = (StreamSource) returnObject;
          InputStream in = sSource.getInputStream();
          byte[] arr = new byte[128];
          int b = -1;
          while ((b = in.read(arr)) != -1) {
            buf.write(arr, 0, b);
          }
        } else {
          throw new RuntimeProcessException("Unsupported javax.xml.transform.Source implementation '" + returnObject.getClass() + "'");
        }
        sMsg.presetBodyBuffer(buf);
        sMsg.commitWrite();
      } else { //the object must be SAAJ message
        throw new RuntimeException("Result class '" + returnObjectClass + "' is not supported");
      }
    } catch (Exception e) {
      throw new RuntimeProcessException(e);
    }
    return sMsg;
  }

}