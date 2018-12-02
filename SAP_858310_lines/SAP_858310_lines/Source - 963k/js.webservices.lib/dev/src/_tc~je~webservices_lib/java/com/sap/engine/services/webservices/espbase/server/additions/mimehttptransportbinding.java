package com.sap.engine.services.webservices.espbase.server.additions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;

import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.MIMEMessageImpl;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.TransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ProcessException;
import com.sap.engine.services.webservices.espbase.server.runtime.ProviderContextHelperImpl;
import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;
import com.sap.engine.services.webservices.tools.SoftReferenceInstancesPool;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class MIMEHTTPTransportBinding implements TransportBinding {

  private static final String STYLE  =  "style";
  private static final String MIME_STYLE  =  "mime";
  static final String NW04_REQUEST  =  "NW04_Request";
  private static final String TB_TYPE  =  "mime";
    
  private static final String BODYTAG  =  "Body";
  private static final Location LOC =  Location.getLocation(MIMEHTTPTransportBinding.class);

  private static SoftReferenceInstancesPool<MIMEMessage> messages = new SoftReferenceInstancesPool<MIMEMessage>();

  public Message createInputMessage(ProviderContextHelper ctx) throws RuntimeProcessException {
    try {
      HTTPTransport transport = (HTTPTransport) ctx.getTransport();
      HttpServletRequest request = transport.getRequest();
  
      String style = request.getParameter(STYLE);
      if (MIME_STYLE.equals(style)) {//mark that this is nw04 requst
        ctx.getDynamicContext().setProperty(NW04_REQUEST, "");
      }
  
      InputStream messageStream = request.getInputStream();
      String contentTypeHeader = request.getContentType();
  
      //tracing request message if Severity <= Debug
      if (LOC.beLogged(Severity.DEBUG)) {
        messageStream = logRequest(LOC, messageStream, request.getContextPath());
      }
  
      MIMEMessage mimeMessage = getMIMEMessage();
      
      String contentType = request.getContentType();
      if (contentType == null 
          || ((! contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(SOAPMessage.SOAP11_MEDIA_TYPE)) 
              && (! contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(SOAPMessage.SOAP12_MEDIA_TYPE)) && (! contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(MIMEMessage.MULTIPARTRELATED_MEDIA_TYPE)))) {
        ProcessException prE = new ProcessException(ExceptionConstants.UNSUPPORTED_MEDIA_CONTENTTYPE, new Object[]{SOAPMessage.SOAP11_MEDIA_TYPE + " | " + SOAPMessage.SOAP12_MEDIA_TYPE + " | " + MIMEMessage.MULTIPARTRELATED_MEDIA_TYPE, request.getContentType()});
        throw prE;
      }
      if (contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(MIMEMessage.MULTIPARTRELATED_MEDIA_TYPE)) {
        mimeMessage.initReadModeFromMIME(messageStream, contentTypeHeader);
        SOAPHTTPTransportBinding.bindingSOAPNS(ctx, mimeMessage.getSOAPVersionNS());
      } else { //this is normal SOAP message
        String soapNS;
        if (contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(SOAPMessage.SOAP11_MEDIA_TYPE)) { //this is SOAP1.1 message
          soapNS = SOAPMessage.SOAP11_NS;
        } else { //this is SOAP1.2 message
          soapNS = SOAPMessage.SOAP12_NS;
        }
        //init standard SOAPMessage
        mimeMessage.initReadMode(messageStream, soapNS);
        SOAPHTTPTransportBinding.bindingSOAPNS(ctx, soapNS);
      }
      return mimeMessage;
    } catch (IOException ioE) {
      throw new ProcessException(ioE);
    }
  }

  public OperationMapping resolveOperation(ProviderContextHelper ctx) throws RuntimeProcessException {
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    String keys[] = getMessageKeysFromReader(message);
    //traverse the operations 
    InterfaceMapping intfMapping = ctx.getStaticContext().getInterfaceMapping();
    OperationMapping opMapping[] = intfMapping.getOperation();
    OperationMapping op;
    String opName, opNS;
    for (int i = 0; i < opMapping.length; i++) {
      op = opMapping[i];
      opName = op.getWSDLOperationName();
      opNS = op.getProperty(OperationMapping.INPUT_NAMESPACE);
      if (keys[1].equals(opName) && keys[0].equals(opNS)) {
        return op;
      }
    }
    String keysStr = "[" + keys[0] + "], [" + keys[1] + "]";
    throw new ProcessException(ExceptionConstants.OPERATION_NOT_FOUND, new Object[]{TB_TYPE, keysStr, intfMapping});
  }

  public Object[] getParameters(Class[] methodClass, ClassLoader loader, ProviderContextHelper ctx) throws RuntimeProcessException {
    return StreamEngineMIME.deserialize(methodClass, loader, ctx);
  }

  public Message initOutputMessage(ProviderContextHelper ctx) throws RuntimeProcessException {
    //get the input message, and init it as output message
    MIMEMessage message = (MIMEMessage) ctx.getProperty(ProviderContextHelperImpl.MESSAGE);
    if (message == null) {
      message = getMIMEMessage();
    }
    message.clear();
    String soapNS = SOAPHTTPTransportBinding.getSOAPNS(ctx);
    if (soapNS == null) {
      soapNS = SOAPMessage.SOAP11_NS;
    }
    message.initWriteMode(soapNS);    
    return message;
  }

  public Message createResponseMessage(Object returnObject, Class returnObjectClass, Object[] resultParams, Class[] resultParamsClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    try {
      MIMEMessage mimeMessage = (MIMEMessage) ctx.getMessage();
      XMLTokenWriter writer = mimeMessage.getBodyWriter();
      writer.enter(NS.SOAPENV, BODYTAG);  
      StreamEngineMIME.serialize(returnObject, returnObjectClass, ctx);
      writer.leave();
      writer.flush();
      
      SOAPMessage soapMsg = mimeMessage;
      //add response headers, set via AppWSContext          
      List appWSCtxHs = ((ProviderContextHelperImpl) ctx).getAppWSContextResponseSOAPHeaders();
      for (int i = 0; i < appWSCtxHs.size(); i++) {
        soapMsg.getSOAPHeaders().addHeader((Element) appWSCtxHs.get(i));
      }

      return mimeMessage;
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }

  public Message createFaultMessage(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException {
    ParameterMapping[] faults = null;
    
    OperationMapping operation = ctx.getOperation();  
    if (operation != null) {
      faults = operation.getParameters(ParameterMapping.FAULT_TYPE);
    }
    
    MIMEMessage message = (MIMEMessage) ctx.getMessage();
    if (message != null) {
      message.clear(); 
    } else {
      message = getMIMEMessage();
    }
    String soapNS = SOAPHTTPTransportBinding.getSOAPNS(ctx);
    if (soapNS == null) {
      soapNS = SOAPMessage.SOAP11_NS;
    }
    message.initWriteMode(soapNS);
    SOAPMessage soapMessage = message;
    XMLTokenWriter writer = soapMessage.getBodyWriter();
    
    //add response headers, set via AppWSContext          
    List appWSCtxHs = ((ProviderContextHelperImpl) ctx).getAppWSContextResponseSOAPHeaders();
    for (int i = 0; i < appWSCtxHs.size(); i++) {
      soapMessage.getSOAPHeaders().addHeader((Element) appWSCtxHs.get(i));
    }

    boolean isNW04 = ctx.getDynamicContext().getProperty(MIMEHTTPTransportBinding.NW04_REQUEST) != null;
  
    if (faults != null && faults.length > 0) {
      int i;
  
      for (i = 0; i < faults.length; i++) {
        if (faults[i].getJavaType().equalsIgnoreCase(thr.getClass().getName())) {
          break;
        }
      }
  
      if (i < faults.length) {
        try {
          writer.enter(NS.SOAPENV, BODYTAG);
          if (isNW04) { 
            StreamEngine.serializeThrowableEncoded(thr, faults[i], StreamEngine.CLIENT_ERROR_CODE, ctx);
          } else {
            StreamEngine.serializeThrowableLiteral(thr, faults[i], StreamEngine.CLIENT_ERROR_CODE, ctx);
          }
          writer.leave(); //Body
        } catch (IOException ioE) {
          throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
        }
        return message;
      }
    }
  
    //in case of not handled client fault
    try {
      writer.enter(NS.SOAPENV, BODYTAG);
      StreamEngine.serializeThrowableLiteral(thr, null, StreamEngine.CLIENT_ERROR_CODE, ctx);
      writer.leave(); //Body
    } catch (IOException ioE) {
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
    }
  
    return message;
  }


  public void onContextReuse(ProviderContextHelper ctx) {
    MIMEMessage msg = (MIMEMessage) ctx.getProperty(ProviderContextHelperImpl.MESSAGE);
    msg.clear();
    messages.rollBackInstance(msg);
  }

  public void sendResponseMessage(ProviderContextHelper ctx, int commPattern) throws RuntimeProcessException {
    sendMessage(HttpServletResponse.SC_OK, ctx);
  }

  public void sendServerError(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException {
    try {
      MIMEMessage message = (MIMEMessage) ctx.getMessage();
      if (message != null) {
        message.clear(); 
      } else {
        message = getMIMEMessage();
      }
      String soapNS = SOAPHTTPTransportBinding.getSOAPNS(ctx);
      if (soapNS == null) {
        soapNS = SOAPMessage.SOAP11_NS;
      }
      message.initWriteMode(soapNS);
      SOAPMessage soapMessage = message;
      XMLTokenWriter writer = soapMessage.getBodyWriter();
  
      writer.enter(soapNS, BODYTAG);
      StreamEngine.serializeThrowableLiteral(thr, null, StreamEngine.SERVER_ERROR_CODE, ctx);
      writer.leave(); //Body
      sendMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ctx);
    } catch (Exception e) {
      throw new ProcessException(e);
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
  
  /**
   * Extracts local name and namespace of the first body element.
   * By using these two values, target OperationMapping object is resolved.
   * @return String[], where first value is NS, second value is local name of the element
   */
  private String[] getMessageKeysFromReader(SOAPMessage message) throws RuntimeProcessException {
    String[] keys = new String[2];
    XMLTokenReader reader = message.getBodyReader();
    //here starts the processing;
    try {
      reader.moveToNextElementStart();
    } catch (ParserException pE) {
      throw new ProcessException(ExceptionConstants.PARSER_EXCEPTION_IN_REQUEST_PARSING, new Object[]{pE.getLocalizedMessage()}, pE);
    }

    if (reader.getState() == XMLTokenReader.STARTELEMENT) {
      String localName = reader.getLocalName();
      String uri = reader.getURI();
      keys[0] = uri;
      keys[1] = localName;
    } else {
      throw new ProcessException(ExceptionConstants.EOF_IN_BODY_ELEMENT_SEARCH);
    }
    return keys;
  }

  private InputStream logRequest(Location location, InputStream inputStream, String httpContext) throws ProcessException {
    ReferenceByteArrayOutputStream messBuf = new ReferenceByteArrayOutputStream();
    try {
      int b;
      byte[] rBuf = new byte[128];
      while ((b = inputStream.read(rBuf)) != -1) {
        messBuf.write(rBuf, 0, b);
      }
    } catch (IOException ioE) {
      throw new ProcessException(ioE);
    }

    location.logT(Severity.DEBUG, "Requested context: [" + httpContext + "] message: " + messBuf.toString()); //$JL-I18N$
    return new java.io.ByteArrayInputStream(messBuf.getContentReference(), 0, messBuf.size());
  }

  private ByteArrayOutputStream logResponse(Location location, MIMEMessage message, String httpContext) throws ProcessException {
    ByteArrayOutputStream messBuf = new ByteArrayOutputStream();
    try {
      int partCount = message.getAttachmentContainer().getAttachments().size();
      if (partCount > 0) { //has reference parts
        message.writeMIMEMessage(messBuf);
      } else {
        message.writeTo(messBuf);        
      }
    } catch (Exception e) {
      throw new ProcessException(e);
    } 

   // String httpContext = transport.getRequest().getContextPath();
    location.logT(Severity.DEBUG, "Response context: [" + httpContext + "] message: \r\n" + messBuf.toString());
    return messBuf;
  }

  private MIMEMessage getMIMEMessage() {
    MIMEMessage message = messages.getInstance();
 
    if (message == null) {
      return new MIMEMessageImpl();
    }

    return message;
  }
  
  private void sendMessage(int responseCode, ProviderContextHelper ctx) throws RuntimeProcessException {
    MIMEMessage msg = (MIMEMessage) ctx.getMessage();
    try {
      //tracing request message if Severity <= Debug
      HttpServletRequest request = ((HTTPTransport) ctx.getTransport()).getRequest();    
      ByteArrayOutputStream buf = null;
      if (LOC.beDebug()) {
        buf = logResponse(LOC, msg, request.getContextPath());
      }
      HttpServletResponse response = ((HTTPTransport) ctx.getTransport()).getResponse();
      //if it is possible (response is not commetted), clear it
      if (! response.isCommitted()) {
        response.reset();
      }

      response.setStatus(responseCode);
      int partCount = msg.getAttachmentContainer().getAttachments().size();
      if (partCount > 0) { //has reference parts
        String boundaryValue = msg.getResponseMIMEBoundaryParameter();
        String cT = null;
        if (SOAPMessage.SOAP11_NS.equals(msg.getSOAPVersionNS())) {
          cT = MIMEMessageImpl.getSOAP11MIMEContentTypeWithBoundary(boundaryValue); 
        } else { 
          cT = MIMEMessageImpl.getSOAP12MIMEContentTypeWithBoundary(boundaryValue); 
        }
        //add boundary parameter
        response.setContentType(cT);
        OutputStream outputStream = response.getOutputStream();
        if (buf != null) {
          buf.writeTo(outputStream);
        } else {
          msg.writeMIMEMessage(outputStream);
        }
      } else { //no reference parts
        String cT = SOAPHTTPTransportBinding.getSOAPMediaType(msg.getSOAPVersionNS());
        response.setContentType(cT);
        OutputStream outputStream = response.getOutputStream();
        if (buf != null) {
          buf.writeTo(outputStream);
        } else {
          msg.writeTo(outputStream);        
        }
      }
    } catch (Exception e) {
      throw new ProcessException(e);
    }
  }
}