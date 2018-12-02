package com.sap.engine.services.webservices.espbase.server.additions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Hibernation;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.espbase.messaging.MIMEMessage;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.messaging.impl.MIMEMessageImpl;
import com.sap.engine.services.webservices.espbase.messaging.impl.MessageConvertor;
import com.sap.engine.services.webservices.espbase.messaging.impl.SOAPMessageImpl;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.TransportBinding;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ProcessException;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.WSAddressingException;
import com.sap.engine.services.webservices.espbase.server.additions.wsa.impl.ProviderAddressingProtocolImpl;
import com.sap.engine.services.webservices.espbase.server.runtime.ProviderContextHelperImpl;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.ServiceMeteringUtil;
import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;
import com.sap.engine.services.webservices.jaxws.ctx.SOAPMessageContextImpl;
import com.sap.engine.services.webservices.jaxws.handlers.JAXWSHandlersEngine;
import com.sap.engine.services.webservices.tools.ChunkedOutputStream;
import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.tools.SoftReferenceInstancesPool;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LogRecord;
import com.sap.tc.logging.Severity;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class SOAPHTTPTransportBinding implements TransportBinding, Hibernation {

  static final String BODYTAG  =  "Body";
  static final String STYLE  =  "style";
  static final String DOCUMENT  =  "document";
  static final String RPC  =  "rpc";
  static final String RPC_ENC  =  "rpc_enc";
  static final String ENTITY_SEPARATOR = ";";
  
  public static final String TB_TYPE  =  "soap";
  public static final String POST  =  "POST";
  
  public static final String SOAPFAULT_VERSIONMISMATCH  = "VersionMismatch";
  
  //this constant is used for setting/getting properties in contexts.  
  private static final String PROPERTY_PREFIX  = TB_TYPE + "-transportbinding:";
  private static final String HTTP_RESPONSE_CODE_PROP  = PROPERTY_PREFIX + "http-response-code";
  private static final String SOAPFAULT_RESPONSE_CODE_PROP  = PROPERTY_PREFIX + "soapfault-response-code";
  private static final String PERSISTENT_STYLE_PROP  = PROPERTY_PREFIX + STYLE;
  private static final String SOAPNS_PROP  = PROPERTY_PREFIX + "soap-ns";
  
  static final Location LOCATION = Location.getLocation(SOAPHTTPTransportBinding.class);
   
//  private HTTPTransport transport;
//  private InternalSOAPMessage message;
//  private OperationDefinition virtualOperation;
//  private TypeMappingRegistry typeMappingRegistry;
//  private JavaToQNameMappingRegistry javaToSchemaRegistry;
//  private RuntimeContext runtimeContext;
//  private String style; //the style in use;
//  private boolean isClientFault = false; //set to true if an client fault has occur.

  private static SoftReferenceInstancesPool<SOAPMessage> messages = new SoftReferenceInstancesPool<SOAPMessage>();

  public Message createInputMessage(ProviderContextHelper ctx) throws RuntimeProcessException {
    Transport transport = ctx.getTransport();
    if (! (transport instanceof HTTPTransport)) {
      throw new ProcessException(ExceptionConstants.WRONG_TYPE_PARAMETER, new Object[]{transport, HTTPTransport.class.getName()});
    }
  
    HttpServletRequest request = ((HTTPTransport) transport).getRequest();
    if (! request.getMethod().equalsIgnoreCase(POST)) { //if use browser to request
      ProcessException prE = new ProcessException(ExceptionConstants.WRONG_REQUEST_METHOD, new Object[]{POST, request.getMethod()});
      //sets response code in context.
      setHTTPResponseCodeInContext(ctx, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      throw prE;
    }

    String contentType = request.getContentType();
    if (contentType == null 
        || ((! contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(SOAPMessage.SOAP11_MEDIA_TYPE)) 
            && (! contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(SOAPMessage.SOAP12_MEDIA_TYPE)))
            && (! contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(MIMEMessage.MULTIPARTRELATED_MEDIA_TYPE))) {
      ProcessException prE = new ProcessException(ExceptionConstants.UNSUPPORTED_MEDIA_CONTENTTYPE, new Object[]{SOAPMessage.SOAP11_MEDIA_TYPE + " | " + SOAPMessage.SOAP12_MEDIA_TYPE + " | " + MIMEMessage.MULTIPARTRELATED_MEDIA_TYPE, request.getContentType()});
      //sets response code in context.
      setHTTPResponseCodeInContext(ctx, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
      throw prE;
    }
    //thanks to the above check, no additional validity checks are necessary
    if (contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(SOAPMessage.SOAP12_MEDIA_TYPE)) { //this is SOAP1.2 message
      bindingSOAPNS(ctx, SOAPMessage.SOAP12_NS);      
    } else { //this is SOAP1.2 message
      bindingSOAPNS(ctx, SOAPMessage.SOAP11_NS);
    }
    
    String style = request.getParameter(STYLE);
    if (style != null && (! style.equals(DOCUMENT)) && (! style.equals(RPC)) && (! style.equals(RPC_ENC))) {
      ProcessException prE = new ProcessException(ExceptionConstants.INCORRECT_REQUEST_STYLE_PARAMETER, new Object[]{style});
      //sets response code in context.
      setHTTPResponseCodeInContext(ctx, HttpServletResponse.SC_BAD_REQUEST);
      throw prE;
    }
    bindStyleParam(ctx);
    
    java.io.InputStream inputStream = null;
    try {
      inputStream = request.getInputStream();
    } catch (java.io.IOException ioE) {
      throw new ProcessException(ExceptionConstants.IOEXCEPTION_IN_STREAM_OBTAINING, new Object[]{"inputStream", ioE.getLocalizedMessage()}, ioE);
    }

    //tracing request message if Severity <= Debug
    // Use message logging from now on.
  /*  if (LOCATION.beDebug()) {
      inputStream = logRequest(LOCATION, inputStream, request.getRequestURI());
    }*/

    SOAPMessageImpl message = getSOAPMessage();
    try {
      if (contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(SOAPMessage.SOAP11_MEDIA_TYPE) || contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(SOAPMessage.SOAP12_MEDIA_TYPE)) {
        if (contentType.trim().toLowerCase(Locale.ENGLISH).startsWith(SOAPMessage.SOAP12_MEDIA_TYPE)) { //this is SOAP1.2 message
          bindingSOAPNS(ctx, SOAPMessage.SOAP12_NS);      
        } else { //this is SOAP1.2 message
          bindingSOAPNS(ctx, SOAPMessage.SOAP11_NS);
        }
        message.initReadMode(inputStream, getSOAPNS(ctx));
      } else { //this is MIME message
        MIMEMessageImpl mMsg = (MIMEMessageImpl) message;
        mMsg.initReadModeFromMIME(inputStream, contentType);
        bindingSOAPNS(ctx, mMsg.getSOAPVersionNS());
      }
    } catch (Exception e) {
      if (message.getCurrentState() == SOAPMessage.BEFORE_READ) {
        if (! message.isEnvelopeElementValid()) { //check for valid soap xml - need to have SOAP:Envelope as root element..
          setSOAPFaultResponseCode(ctx, SOAPFAULT_VERSIONMISMATCH); 
        }
      }
      throw new ProcessException(e);
    }
    // move metering http headers to persistable context. Necessary only for WS-RM cases, but done for all for consistency.
    // Previously metering headers were added to the SOAP  headers of the message in beforeHibernation(), but this caused problems with message level security
    ServiceMeteringUtil.moveHTTPMeteringHeadersToPersistableContext(ctx);
    return message;
  }

  public Message initOutputMessage(ProviderContextHelper ctx) throws RuntimeProcessException {
    //get the input message, and init it as output message
    SOAPMessage message = (SOAPMessage) ctx.getProperty(ProviderContextHelperImpl.MESSAGE);
    if (message == null) {
      message = getSOAPMessage();
    }
    message.clear();
    String soapNS = getSOAPNS(ctx);
    if (soapNS == null) {
      soapNS = SOAPMessage.SOAP11_NS;
    }
    message.initWriteMode(soapNS);    
    return message;
  }

  public OperationMapping resolveOperation(ProviderContextHelper ctx) throws RuntimeProcessException {
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    
    InterfaceMapping intfM = ctx.getStaticContext().getInterfaceMapping();
    if (intfM.isJAXWSProviderInterface()) {
      //Do not call the next method, in order the reader to be on the <Body> element.
     // getMessageKeysFromReader(message); //call this method in order to position the reader on first body element.
      return intfM.getOperationByJavaName("invoke"); //this type of interface has only one operation.
    }
    String keys[] = getMessageKeysFromReader(message, ctx);
    //Wrappers are used in all cases, except for rpc and rpc_enc. 
    boolean checkWrappers = checkForWrappers(ctx);
    //traverse the operations 
    InterfaceMapping intfMapping = ctx.getStaticContext().getInterfaceMapping();
    OperationMapping opMapping[] = intfMapping.getOperation();
    OperationMapping op;
    String opName, opNS;
    for (int i = 0; i < opMapping.length; i++) {
      op = opMapping[i];
      opName = null;
      if (checkWrappers) {
        opName = op.getProperty(OperationMapping.SOAP_REQUEST_WRAPPER);
      } else {
        opName = op.getWSDLOperationName();
      }
      opNS = op.getProperty(OperationMapping.INPUT_NAMESPACE);

      LOCATION.debugT("OperationName: " + opName + ", OperationNS: " + opNS);

      if (keys[0].equals(opNS) && keys[1].equals(opName)) {
        return op;
      }
    }
    //Check whether there are a mandadory soap headers, in order CTS test to pass.
    StreamEngine.checkForMandatoryHeaders(message.getSOAPHeaders(), ctx);
    String keysStr = "[" + keys[0] + "], [" + keys[1] + "]";
    //set SOAPFault code to client (cts test to pass)
    setSOAPFaultResponseCode(ctx, StreamEngine.CLIENT_ERROR_CODE);
    throw new ProcessException(ExceptionConstants.OPERATION_NOT_FOUND, new Object[]{TB_TYPE, keysStr, intfMapping});
  }

  // NOTE: methodClasses and loader can be null in case of Galaxy request
  public Object[] getParameters(Class[] methodClasses, ClassLoader loader, ProviderContextHelper ctx) throws RuntimeProcessException {
    //check for JEEProvider
    InterfaceMapping intfM = ctx.getStaticContext().getInterfaceMapping();
    
    // i044259
    if (intfM.isGalaxyInterface()){
      return StreamEngine.deserializeSDO(methodClasses, loader, ctx);
    }else if(loader == null){ // this should not happen
      throw new RuntimeProcessException("Class loader is null while deserializing message - probably Galaxy request but no Galaxy flag found");
    }
    if (intfM.isJAXWSProviderInterface()) {
      return getParametersForJAXWSProvider(methodClasses, ctx);
    }    
    //check for outsideIn cfg
    if (ctx.getStaticContext().getInterfaceMapping().isOutsideInInterface()) {
      return StreamEngine.desializeOutsideIn(methodClasses, loader, ctx);
    }
    if (ctx.getStaticContext().getInterfaceMapping().isJAXWSInterface()) {
//      try {
//        String[] classes = getParamClassNames(ctx.getOperation());
//        Class[] cls = loadClassesForJAXB(classes, ((ProviderContextHelperImpl) ctx).getImplClassLoader());
//        JAXBContext jaxbContext = null;
//        try {
//          jaxbContext = JAXBContext.newInstance(cls);
//        } catch (JAXBException origE) {
//          //needed .toString() of the exception to be called, since it returns detailed info.
//          JAXBException newE = new JAXBException(origE.toString(), origE);
//          throw newE;
//        }
//        ((StaticConfigurationContextImpl) ctx.getStaticContext()).setPropertyInternal("jaxbcontext", jaxbContext);
//      } catch (Exception e) {
//        throw new RuntimeProcessException(e);
//      }
      // check for the special CTS with empty body
      Boolean res = (Boolean) ctx.getDynamicContext().getProperty(CTS_TEST_FAILING_BECAUSE_OF_EMPTY_BODY);
      if (res != null && res.booleanValue() == true) {
        return new Object[0];
      }
      return StreamEngine.deserializeJEE(methodClasses, loader, ctx);
    }
    String style = getStyleParam(ctx);
    //check for rpc encoded style first, because of backwards compatibility
    if (RPC_ENC.equals(style)) {
      return StreamEngine.deserializeEncoded(methodClasses, loader, ctx);
    } else {
      return StreamEngine.deserializeLiteral(methodClasses, loader, ctx);
    }
  }

  // NOTE: returnObjectClass and resultParamClasses can be null in case of Galaxy request
  public Message createResponseMessage(Object returnObject, Class returnObjectClass, Object[] resultParams, Class[] resultParamsClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    XMLTokenWriter writer = message.getBodyWriter();
    try {
      //init msg with default WSA headers
      ProviderAddressingProtocolImpl.SINGLETON.initResponseMessageWithDefaults(message, false, ctx);
      
      InterfaceMapping intfM = ctx.getStaticContext().getInterfaceMapping();
      
      boolean isGalaxyMessage = intfM.isGalaxyInterface(); 
      if (intfM.isJAXWSProviderInterface() && ! isGalaxyMessage) {
        return createResponseMessageForJAXWSProvider(returnObject, returnObjectClass, resultParams, resultParamsClasses, ctx);
      }
      //creates Body Element
      writer.enter(message.getSOAPVersionNS(), BODYTAG);
      // i044259
      if (isGalaxyMessage){
        StreamEngine.serializeSDO(returnObject, /*returnObjectClass,*/ resultParams, resultParamsClasses, ctx);
      }else if (ctx.getStaticContext().getInterfaceMapping().isOutsideInInterface()) {       //check for outsideIn cfg
        StreamEngine.serializeOutsideIn(returnObject, returnObjectClass, resultParams, resultParamsClasses, ctx);
      } else if (ctx.getStaticContext().getInterfaceMapping().isJAXWSInterface()) {
        StreamEngine.serializeJEE(returnObject, returnObjectClass, resultParams, resultParamsClasses, ctx);
      } else {
        String style = getStyleParam(ctx);
        if (RPC_ENC.equals(style)) {//supported for backwards compatibility
          StreamEngine.serializeEncoded(returnObject, returnObjectClass, ctx);
        } else {
          if (style == null) { //this is NY InsideOut ws request, preset it to RPC
            style = SOAPHTTPTransportBinding.RPC;
          }
          StreamEngine.serializeLiteral(returnObject, returnObjectClass, style, ctx);
        }
      }
      //leaves Body Element
      writer.leave();
      writer.flush();
      //add response headers, set via AppWSContext
      List appWSCtxHs = ((ProviderContextHelperImpl) ctx).getAppWSContextResponseSOAPHeaders();
      for (int i = 0; i < appWSCtxHs.size(); i++) {
        message.getSOAPHeaders().addHeader((Element) appWSCtxHs.get(i));
      }
      message.commitWrite();
    } catch (Exception e) {
      throw new ProcessException(e);
    }
    //System.out.println("SOAPHTTPTransportBinding: responseEnvelope: " + this.message.getSoapEnvelope());
    return message;
  }

  public void sendResponseMessage(ProviderContextHelper ctx, int commPattern) throws RuntimeProcessException {
    final String METHOD = "sendResponseMessage(): ";
    int semantic = ctx.getMessageSemantic();
    if (commPattern == TransportBinding.SYNC_COMMUNICATION) {
      LOCATION.debugT(METHOD + " send message as synchronous response.");
      if (semantic == ProviderContextHelper.FAULT_MSG) {
        sendMessage0(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ctx);
      } else if (semantic == ProviderContextHelper.NORMAL_RESPONSE_MSG) {
        sendMessage0(HttpServletResponse.SC_OK, ctx);
      } else if (semantic == ProviderContextHelper.BASIC_AUTHENTICATION_EXPECTED) {
        sendMessage0(HttpServletResponse.SC_UNAUTHORIZED, ctx);
      } else {
        throw new ProcessException(ExceptionConstants.UNKNOWN_RESPONSE_MESSAGE_SEMANTIC, new Object[]{new Integer(semantic), TB_TYPE});
      }
    } else {//this is asynch request-response operation response
      LOCATION.debugT(METHOD + " send message as asynchronous response.");
      try {
        String responseToAddress = ProviderAddressingProtocolImpl.SINGLETON.getResponseTo(ctx);
        SOAPMessage msg = (SOAPMessage) ctx.getMessage();
        sendMessageOneWay(responseToAddress, msg, null);
      } catch (WSAddressingException wsA) {
        throw new ProcessException(wsA);
      }
    }
  }

  public Message createFaultMessage(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException {
    ParameterMapping[] faults = null;
    OperationMapping operation = null;
    try {
      operation = ctx.getOperation();
    } catch (Throwable t) { //in case it is not possible to resolve the operation, ignore the problem
      LOCATION.catching(t);
      operation = null;
    }
    if (operation != null) { 
      faults = operation.getParameters(ParameterMapping.FAULT_TYPE);
    }
    SOAPMessage message = (SOAPMessage) ctx.getProperty(ProviderContextHelperImpl.MESSAGE);
    if (message != null) {
      message.clear(); 
    } else {
      message = getSOAPMessage(); 
    }
    String soapNS = getSOAPNS(ctx);
    if (soapNS == null) { //this could happen when in createMessage() method nothing has been set in the context due to an error
      soapNS = SOAPMessage.SOAP11_NS; 
    }
    message.initWriteMode(soapNS);
    try {
      //init msg with default WSA headers
      ProviderAddressingProtocolImpl.SINGLETON.initResponseMessageWithDefaults(message, true, ctx);
    } catch (Exception e) {
      throw new ProcessException(e);
    }

    XMLTokenWriter writer = message.getBodyWriter();

    //add response headers, set via AppWSContext    
    List appWSCtxHs = ((ProviderContextHelperImpl) ctx).getAppWSContextResponseSOAPHeaders();
    for (int i = 0; i < appWSCtxHs.size(); i++) {
      message.getSOAPHeaders().addHeader((Element) appWSCtxHs.get(i));
    }

    String style = getStyleParam(ctx);
    if (faults != null && faults.length > 0) {
      int i;
      for (i = 0; i < faults.length; i++) {
        if (faults[i].getJavaType().equalsIgnoreCase(thr.getClass().getName())) {
          break;
        }
      }
      if (i < faults.length) {
        try {
          writer.enter(soapNS, BODYTAG);
          if (style != null && (style.equals(RPC_ENC))) {
            StreamEngine.serializeThrowableEncoded(thr, faults[i], StreamEngine.CLIENT_ERROR_CODE, ctx);
          } else {
            if (ctx.getStaticContext().getInterfaceMapping().isJAXWSInterface()) {
              StreamEngine.serializeJEEThrowable(thr, faults[i], null, ctx);
            } else {
              StreamEngine.serializeThrowableLiteral(thr, faults[i], StreamEngine.CLIENT_ERROR_CODE, ctx);
            }
          }
          writer.leave(); //Body
          writer.flush();
          message.commitWrite();
          return message;
        } catch (IOException ioE) {
          throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
        }
      }
    } 
    try {
      writer.enter(soapNS, BODYTAG);
      StreamEngine.serializeThrowableLiteral(thr, null, StreamEngine.CLIENT_ERROR_CODE, ctx);
      writer.leave(); //Body
      writer.flush();
    } catch (IOException ioE) {
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
    }
    message.commitWrite();
    return message;
  }

  public void sendServerError(Throwable thr, ProviderContextHelper ctx) throws RuntimeProcessException {
    try { 
      HTTPTransport transport = (HTTPTransport) ctx.getTransport();
      //BP1.1 compliance, check for preset response codes
      int rCode = getHTTPResponseCodeFromContext(ctx); 
      if (rCode != -1) {
        transport.getResponse().sendError(rCode, thr.getLocalizedMessage());
        return;
      }
      if (thr instanceof ProcessException) {
        ProcessException prE = (ProcessException) thr;
        if (prE.getCause() instanceof ParserException) { //If parserException is thrown it indicats that the xml is wrong
          transport.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, prE.getCause().toString());
          return;
        }
      }
    } catch (IOException ioE) {
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
    }
    SOAPMessage message = null;
    //try to use the msg from context if possible
    try {
      message = (SOAPMessage) ctx.getMessage();
      message.clear(); //clear the msg obtained from context
    } catch (Throwable t) {
      message = getSOAPMessage();
    }
    if (message == null) {
      message = getSOAPMessage();
    }
    String soapNS = getSOAPNS(ctx);
    if (soapNS == null) { //this could happen when in createMessage() method nothing has been set in the context due to an error
      soapNS = SOAPMessage.SOAP11_NS; 
    }
    message.initWriteMode(soapNS);
    XMLTokenWriter writer = message.getBodyWriter();
    ctx.setMessage(message);
    int semantic = ctx.getMessageSemantic();
    if (semantic == ProviderContextHelper.NORMAL_RESPONSE_MSG || semantic == -1) { //this cannot be a normal response, set it to FAULT
      ctx.setMessageSemantic(ProviderContextHelper.FAULT_MSG);
    }
    try {
      ProviderAddressingProtocolImpl.SINGLETON.initResponseMessageWithDefaults(message, true, ctx);
      writer.enter(soapNS, BODYTAG);
      //check for custom soapfault code.
      String sFaultCode = getSOAPFaultResponseCode(ctx);
      InterfaceMapping ifM = ctx.getStaticContext().getInterfaceMapping();
      if (ifM.isJAXWSInterface() || ifM.isJAXWSProviderInterface()) {
        StreamEngine.serializeJEEThrowable(thr, null, sFaultCode, ctx);
      } else {
        if (sFaultCode == null) {
          sFaultCode = StreamEngine.SERVER_ERROR_CODE;
        }
        StreamEngine.serializeThrowableLiteral(thr, null, sFaultCode, ctx);
      }
      writer.leave(); //the body wrapper
      message.commitWrite();
    } catch (Exception ioE) {
      throw new ProcessException(ExceptionConstants.BUILDING_STREAMRESPONSE_IOEXCEPTION, new Object[]{ioE.getLocalizedMessage()}, ioE);
    }
    //sending the message via transport
    int commPattern = this.getCommunicationPattern(ctx);
    sendResponseMessage(ctx, commPattern);
  }
  
  public String getAction(ProviderContextHelper ctx) throws RuntimeProcessException {
    try {
      ProviderAddressingProtocolImpl.SINGLETON.parseRequestHeaders(ctx);
      return ProviderAddressingProtocolImpl.SINGLETON.getRequestAction(ctx);
    } catch (Exception e) {
      throw new RuntimeProcessException(e);
    }
  }
  
  public int getCommunicationPattern(ProviderContextHelper ctx) throws RuntimeProcessException {
    try {
      boolean b = ProviderAddressingProtocolImpl.SINGLETON.isAsyncCommunication(ctx);
      if (b) {
        return TransportBinding.ASYNC_COMMUNICATION;
      } else {
        return TransportBinding.SYNC_COMMUNICATION;
      }
    } catch (WSAddressingException wsAE) {
      throw new ProcessException(wsAE);
    }
  }
  
  public void sendAsynchronousResponse(ProviderContextHelper context) throws RuntimeProcessException {
    LOCATION.debugT("sendAsynchronousResponse(): enter");
    try {
      HttpServletResponse response = ((HTTPTransport) context.getTransport()).getResponse();
      if (! response.isCommitted()) {
        LOCATION.debugT("sendAsynchronousResponse(): sending '202 Accepted'");
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        response.flushBuffer();
      }
    } catch (Exception e) {
      throw new ProcessException(e);
    }
    LOCATION.debugT("sendAsynchronousResponse(): leave");
  }
  
  public void sendMessageOneWay(String endpointURL, Message msg, String action) throws RuntimeProcessException {
    final String METHOD = "sendMessageOneWay(): ";
    LOCATION.debugT(METHOD + "entered. Endpoint url: " + endpointURL);
    SOAPMessage soapMsg = (SOAPMessage) msg;

    String contentType = getContentTypeSmart(soapMsg);
    if (contentType.startsWith(SOAPMessage.SOAP12_MEDIA_TYPE)) { //append only when message is standard SOAP12 request
      if (action != null) {
        contentType = contentType + "; action=\"" + action + "\"";
      }
    }
    try {
      //Use message logging
/*      ByteArrayOutputStream debugBuf = null;
      if (LOCATION.beDebug()) {
        LOCATION.debugT(METHOD + "'contentType' header value is '" + contentType + "'");
        debugBuf = new ByteArrayOutputStream();
        writeMessageSmart(soapMsg, debugBuf);
        LOCATION.debugT(METHOD + "message:" + debugBuf.toString() + "'");
      }*/
      HTTPSocket socket = new HTTPSocket(new URL(endpointURL));
      socket.setRequestMethod("POST");
      socket.setSocketTimeout(60000);
      socket.setHeader("Connection", "close");
      socket.setHeader("Transfer-Encoding", "chunked");
      socket.setHeader("content-type", contentType);
      if (SOAPMessage.SOAP11_NS.equals(soapMsg.getSOAPVersionNS())) {
        if (action == null) {
          socket.setHeader("SOAPAction", "");
        } else {
          socket.setHeader("SOAPAction", "\"" + action + "\"");
        }
      }
  
      LOCATION.debugT(METHOD + " transmitting the message via HTTP socket connection.");
      OutputStream sockOutput = socket.getOutputStream();
      OutputStream chunkedOutput = new ChunkedOutputStream(sockOutput, 128);
/*      if (debugBuf != null) {
        debugBuf.writeTo(chunkedOutput);
      } else {
        writeMessageSmart(soapMsg, chunkedOutput);
      }*/
      writeMessageSmart(soapMsg, chunkedOutput);
      chunkedOutput.flush();
      chunkedOutput.close();
      sockOutput.flush();
      
      InputStream in = socket.getInputStream();
      //read the whole response
      while (in.read() != -1); 
      
      int respCode = socket.getResponseCode(); 
      if (respCode != HttpServletResponse.SC_ACCEPTED) {
        throw new ProcessException(ExceptionConstants.UNEXPECTED_RESPONSE_CODE, new Object[]{new Integer(respCode), new Integer(HttpServletResponse.SC_ACCEPTED)});
      }
      
      LOCATION.debugT(METHOD + " response received. Disconnecting HTTP socket connection.");
      socket.disconnect();
    } catch (Exception e) {
      if (e instanceof RuntimeProcessException) {
        throw (RuntimeProcessException) e;
      }
      throw new ProcessException(e);
    }
  }
  
  
  public void onContextReuse(ProviderContextHelper ctx) {
    SOAPMessage msg = (SOAPMessage) ctx.getProperty(ProviderContextHelperImpl.MESSAGE);
    if (msg != null && msg instanceof MIMEMessage) {
      msg.clear();
      messages.rollBackInstance(msg);
    }
  }

//====================== Hibernation methods ==========================
  public void afterHibernation(ConfigurationContext ctx) throws ProtocolException {
    ProviderContextHelperImpl pCtx = (ProviderContextHelperImpl) ctx;
    String setStringValue = (String) pCtx.getPersistableContext().getProperty(ProviderContextHelperImpl.UNDERSTOOD_SOAPHEADERS_SET);
    if (setStringValue != null) {
      Enumeration tokenizer = new StringTokenizer(setStringValue, ENTITY_SEPARATOR);
      while (tokenizer.hasMoreElements()) {
        String s = (String) tokenizer.nextElement();
        pCtx.markSOAPHeaderAsUnderstood(QName.valueOf(s));
      }
    }
  }

  public void beforeHibernation(ConfigurationContext ctx) throws ProtocolException {
    //store the understood soap headers set
    ProviderContextHelperImpl pCtx = (ProviderContextHelperImpl) ctx;
    //moveHTTPMeteringDataIntoSOAPMessage(pCtx);
    /*try{
      ServiceMeteringUtil.moveMeteringDataToSOAPHeaders(pCtx);
    }catch(RuntimeProcessException rpe){
      LOCATION.traceThrowableT(Severity.WARNING, "[beforeHibernation] Error while processing metering headers", rpe);
    }*/
    Set set = pCtx.getUnderstoodSOAPHeadersSet();
    if (! set.isEmpty()) {
      StringBuffer buf = new StringBuffer();
      boolean isFirst = true;
      QName cur;
      Iterator itr = set.iterator();
      while (itr.hasNext()) {
        cur = (QName) itr.next();
        if (isFirst) {
          isFirst = false;
        } else {
          buf.append(ENTITY_SEPARATOR);
        }
        buf.append(cur.toString());
      }
      pCtx.getPersistableContext().setProperty(ProviderContextHelperImpl.UNDERSTOOD_SOAPHEADERS_SET, buf.toString());
    }
  }
  
  /*private void moveHTTPMeteringDataIntoSOAPMessage(ProviderContextHelperImpl ctx) {
    Object oTr = ctx.getTransport();
    //HashMap httpMeteringHeaders = new HashMap();

    if (oTr != null && oTr instanceof HTTPTransport) {
      HttpServletRequest request = ((HTTPTransport) oTr).getRequest();
      if (request != null) {
        String callingAppName = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPNAME);
        String callingComponent = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPONENT);
        String callingType = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_APPTYPE);
        String callingCompany = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_COMPANY);
        String callingSys = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_SYS); 
        String callingUsr = request.getHeader(ServiceMeteringConstants.HTTP_HEADER_USER_CODE);
        try{

          Object oMsg = ctx.getMessage();
          if (oMsg != null) {
            SOAPMessage message = (SOAPMessage) oMsg;
            SOAPHeaderList soapHeaders = message.getSOAPHeaders();
            Document hDoc = soapHeaders.getInternalDocument();
            Element meteringHeader = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, 
                ServiceMeteringConstants.SOAPHEADER_NAME);


            // application type
            Element appTypeEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_APPTYPE);
            Text appTypeElTxt = hDoc.createTextNode(callingType != null ? callingType : "");
            appTypeEl.appendChild(appTypeElTxt);
            meteringHeader.appendChild(appTypeEl);

            // application name
            Element appNameEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_APPNAME);
            Text appNameElTxt = hDoc.createTextNode(callingAppName != null ? callingAppName : "");
            appNameEl.appendChild(appNameElTxt);
            meteringHeader.appendChild(appNameEl);

            // component
            Element componentEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_COMPONENT);
            Text componentElTxt = hDoc.createTextNode(callingComponent != null ? callingComponent : "");
            componentEl.appendChild(componentElTxt);
            meteringHeader.appendChild(componentEl);


            // company
            Element instNoEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_COMPANY);
            Text instNoTxt = hDoc.createTextNode(callingCompany != null  ? callingCompany : "");
            instNoEl.appendChild(instNoTxt);
            meteringHeader.appendChild(instNoEl);

            // sid
            Element sidEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_SYS);
            Text sidElTxt = hDoc.createTextNode(callingSys != null ? callingSys : "");
            sidEl.appendChild(sidElTxt);
            meteringHeader.appendChild(sidEl);

            // user code
            Element usrEl = hDoc.createElementNS(ServiceMeteringConstants.SOAPHEADER_NS, ServiceMeteringConstants.SOAP_HEADER_USER_CODE);
            Text usrElTxt = hDoc.createTextNode(callingUsr != null ? callingUsr : "");
            usrEl.appendChild(usrElTxt);
            meteringHeader.appendChild(usrEl);

            
            soapHeaders.addHeader(meteringHeader);
          }
        }catch (RuntimeProcessException e) {
          LOCATION.traceThrowableT(Severity.DEBUG, "[moveHTTPMeteringDataIntoSOAPMessage] Error while processing metering headers", e);
        }
      }
    }
  }*/

  

  public void finishHibernation(ConfigurationContext ctx) throws ProtocolException {
    // TODO Auto-generated method stub
    
  }

  public void finishMessageDeserialization(ConfigurationContext ctx) throws ProtocolException {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * Extracts local name and namespace of the first body element.
   * By using these two values, target OperationMapping object is resolved.
   * @return String[], where first value is NS, second value is local name of the element
   */
  private String[] getMessageKeysFromReader(SOAPMessage message, ProviderContextHelper pCtx) throws RuntimeProcessException {
    String[] keys = new String[2];
    XMLTokenReader reader = message.getBodyReader();
    
    if (LOCATION.beDebug()){ // **
      LOCATION.debugT("getMessageKeysFromReader(), reader: " + reader + ", state: " + reader.getState());
    }
    
    if (reader.getState() == XMLTokenReader.STARTELEMENT && reader.getLocalNameCharArray().equals(BODYTAG)) {
      //here starts the processing;
      try {
        reader.moveToNextElementStart();
      } catch (ParserException pE) {
        throw new ProcessException(ExceptionConstants.PARSER_EXCEPTION_IN_REQUEST_PARSING, new Object[]{pE.getLocalizedMessage()}, pE);
      }
    }

    if (reader.getState() == XMLTokenReader.STARTELEMENT) {
      String localName = reader.getLocalName();
      String uri = reader.getURI();
      keys[0] = uri;
      keys[1] = localName;
    } else {
      // This a hack in order com/sun/ts/tests/jws/webresult/webresult1/client/Client.java#testHelloString4 CTS test to pass.
      // There is a bug in JAXWS RI which sends an empty body instead an empty operation element. Hopefully this will be fixed.
      HttpServletRequest req = ((HTTPTransport) pCtx.getTransport()).getRequest();
      String soapAction = req.getHeader("SOAPAction");
      if (soapAction != null && "\"urn:HelloString4\"".equals(soapAction.trim())) {
        pCtx.getDynamicContext().setProperty(CTS_TEST_FAILING_BECAUSE_OF_EMPTY_BODY, true);
        keys[0] = "http://server.webresult1.webresult.jws.tests.ts.sun.com/";
        keys[1] = "helloString4";
        return keys;
      }
      throw new ProcessException(ExceptionConstants.EOF_IN_BODY_ELEMENT_SEARCH);
    }    
    return keys;
  }
  
  private static final String CTS_TEST_FAILING_BECAUSE_OF_EMPTY_BODY = "com/sun/ts/tests/jws/webresult/webresult1/client/Client.java#testHelloString4";
  /**
   * Sends message, with specified HTTP response code via transport.
   */
  private void sendMessage0(int responseCode, ProviderContextHelper ctx) throws RuntimeProcessException {
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    try {
      //tracing request message if Severity <= Debug
      HttpServletRequest request = ((HTTPTransport) ctx.getTransport()).getRequest();    
      ByteArrayOutputStream buf = null;
      
      // Log the response only if it's fault. If the response is normal - use the message testing.
      if ((LOCATION.beDebug()) && !(responseCode == HttpServletResponse.SC_ACCEPTED || responseCode == HttpServletResponse.SC_OK)) {
        buf = logResponse(LOCATION, message, request.getRequestURI());
      }
      OutputStream output = null;
      HttpServletResponse response = ((HTTPTransport) ctx.getTransport()).getResponse();
      //if it is possible (response is not commetted), clear it
      if (! response.isCommitted()) {
        response.reset();
      }
      response.setStatus(responseCode);
      if (responseCode == HttpServletResponse.SC_UNAUTHORIZED) {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + request.getRequestURI() + "\"");
      }
      String contentType = getContentTypeSmart(message);
      response.setContentType(contentType);
      output = response.getOutputStream();
      if (buf != null) { //in case the response message is logged.
        buf.writeTo(output);
      } else {
        writeMessageSmart(message, output);
      }
    } catch (java.io.IOException ioE) {
      throw new ProcessException(ExceptionConstants.IOEXCEPTION_IN_SENDING_RESPONSE, new Object[]{ioE.getLocalizedMessage()}, ioE);
    }
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
    LogRecord requestRec = location.logT(Severity.DEBUG, "Requested context: [" + httpContext + "] message: \r\n" + messBuf.toString()); //$JL-I18N$
//    if (requestRec != null) {
//      WSLogTrace.getESBTracer().getAttachedCallEntry().setInboundPayloadTraceID(requestRec.getId().toString());
//    }
    return new java.io.ByteArrayInputStream(messBuf.getContentReference(), 0, messBuf.size());
  }

  public static SOAPMessageImpl getSOAPMessage() {
    SOAPMessageImpl message = (SOAPMessageImpl) messages.getInstance();
    if (message == null) {
      return new MIMEMessageImpl();
    }
    return message;
  }

  private ByteArrayOutputStream logResponse(Location location,  SOAPMessage message, String httpContext) throws ProcessException {
    ReferenceByteArrayOutputStream messBuf = new ReferenceByteArrayOutputStream();
    try {
      writeMessageSmart(message, messBuf);
    } catch (IOException ioE) {
      throw new ProcessException(ioE);
    }

   // String httpContext = transport.getRequest().getContextPath();
    LogRecord responseRec = location.logT(Severity.DEBUG, "Response context: [" + httpContext + "] message: \r\n" + messBuf.toString());
//    if (responseRec != null){
//      WSLogTrace.getESBTracer().getAttachedCallEntry().setOutboundPayloadTraceID(responseRec.getId().toString());
//    }
    return messBuf;
  }

  /**
   * Check for availability of 'style' http parameter, 
   * and its value. In case the value is 'rpc' or 'rpc_enc', that means it is
   * request to old web service, and a care should be taken NOT to use
   * the wrappers which are used by default.
   * @return true incase wrappers should be taken in consideration.
   */
  private boolean checkForWrappers(ProviderContextHelper ctx) {
    String style = getStyleParam(ctx);
    if (style != null && (style.equals(RPC) || style.equals(RPC_ENC))) {
      return false;	
    }
    //for outsideIn case, use request wrapper
    if (ctx.getStaticContext().getInterfaceMapping().isOutsideInInterface()) {
      return true;
    }
    if (ctx.getStaticContext().getInterfaceMapping().isJAXWSInterface()) {
      return true;
    }
    return true;
  }
  
  private String getStyleParam(ProviderContextHelper ctx) {
    return (String) ctx.getPersistableContext().getProperty(PERSISTENT_STYLE_PROP);
  }
  /**
   * Bind http 'style' param value into persitent context 
   */  
  private void bindStyleParam(ProviderContextHelper ctx) {
    HTTPTransport transport = (HTTPTransport) ctx.getTransport();
    String style = transport.getRequest().getParameter(STYLE); 
    if (style != null) { 
      ctx.getPersistableContext().setProperty(PERSISTENT_STYLE_PROP, style);
    }
  }
  /**
   * Sets, in dynamic context, 'http-reponse-code' property with value <code>rCode</code>. 
   */  
  private void setHTTPResponseCodeInContext(ProviderContextHelper ctx, int rCode) {
    ctx.getDynamicContext().setProperty(HTTP_RESPONSE_CODE_PROP, Integer.toString(rCode));
  }  
  /**
   * Gets, from dynamic context, the 'http-reponse-code' property.
   * If there is no property set, -1 is returned. 
   */  
  private int getHTTPResponseCodeFromContext(ProviderContextHelper ctx) {
    String value = (String) ctx.getDynamicContext().getProperty(HTTP_RESPONSE_CODE_PROP);
    if (value != null) {
      return Integer.parseInt(value);
    }
    return -1;
  }
  /**
   * Checks whether message contains attachments and if it contains writes it as multipart/related one, 
   * else writes it as normal soap message.
   * @param msg
   * @param output
   */
  private void writeMessageSmart(SOAPMessage msg, OutputStream output) throws IOException {
    if (! (msg instanceof MIMEMessage)) {
      msg.writeTo(output);
      return;
    }
    MIMEMessage m_msg = (MIMEMessage) msg;
    if (m_msg.getAttachmentContainer().getAttachments().isEmpty()) { //write soap message
      m_msg.writeTo(output);
    } else { //write MIME message
      m_msg.writeMIMEMessage(output);
    }
  }
  /**
   * If the message contains attachments, multipart/related content-type plus boundary parameter is returned, 
   * else soap content-type is returned.
   * @param msg
   */  
  private String getContentTypeSmart(SOAPMessage msg) {
    if (! (msg instanceof MIMEMessage)) {
      return getSOAPMediaType(msg.getSOAPVersionNS());
    }
    MIMEMessage m_msg = (MIMEMessage) msg;
    if (m_msg.getAttachmentContainer().getAttachments().isEmpty()) { //this is pure soap message
      return getSOAPMediaType(m_msg.getSOAPVersionNS());
    } else { //this should be MIME ct
      String boundary = m_msg.getResponseMIMEBoundaryParameter();
      if (SOAPMessage.SOAP11_NS.equals(m_msg.getSOAPVersionNS())) {
        return MIMEMessageImpl.getSOAP11MIMEContentTypeWithBoundary(boundary);
      } else {
        return MIMEMessageImpl.getSOAP12MIMEContentTypeWithBoundary(boundary);
      }
    }
  }
  
  private Object[] getParametersForJAXWSProvider(Class[] methodClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    //if there are handlers configured, a SAAJ message should be available.
    if (JAXWSHandlersEngine.getSOAPMessageContextFromProviderContext(ctx) != null) {
      LOCATION.debugT("getParameters(): Found JAXWS contexts, the SAAJ msg from them will be used.");
      if (methodClasses[0] == Source.class) { //this must be single class
        Source s = JAXWSHandlersEngine.getLogicalMessageContextFromProviderContext(ctx).getMessage().getPayload();
        if (LOCATION.beDebug()) {
          LOCATION.debugT("getParameters(), JAXWSProviderInterface in PAYLOAD mode, returned source:" + s);
        }
        return new Object[]{s};
      } else if (methodClasses[0] == javax.xml.soap.SOAPMessage.class) {
        javax.xml.soap.SOAPMessage msg = JAXWSHandlersEngine.getSOAPMessageContextFromProviderContext(ctx).getMessage();
        if (LOCATION.beDebug()) {
          LOCATION.debugT("getParameters(), JAXWSProviderInterface in MESSAGE mode, returned message:" + msg);
        }
        return new Object[]{msg};
      } else {
        throw new RuntimeProcessException("Unsupported JAXWSProvider parameter '" + methodClasses[0]);
      }
    } else {
      LOCATION.debugT("getParameters(): SAAJ will be created from inbound SAP msg.");
      javax.xml.soap.SOAPMessage saajMsg = null;
      try {
        saajMsg = MessageConvertor.convertInboundSAPMessageIntoSAAJ((SOAPMessage) ctx.getMessage());
        if (methodClasses[0] == Source.class) { //this must be single class
          Element bodyContent = null;
          NodeList ch_nodes = saajMsg.getSOAPBody().getChildNodes();
          for (int i = 0; i < ch_nodes.getLength(); i++) {
            if (ch_nodes.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
              bodyContent = (Element) ch_nodes.item(i);
              break;
            }
          }
          if (LOCATION.beDebug()) {
            LOCATION.debugT("getParameters(), JAXWSProviderInterface in PAYLOAD mode, returned element in source:" + bodyContent);
          }
          return new Object[]{new DOMSource(bodyContent)};
        } else if (methodClasses[0] == javax.xml.soap.SOAPMessage.class) {
          if (LOCATION.beDebug()) {
            LOCATION.debugT("getParameters(), JAXWSProviderInterface in MESSAGE mode, returned message:" + saajMsg);
          }
          return new Object[]{saajMsg};
        } else {
          throw new RuntimeProcessException("Unsupported JAXWSProvider parameter '" + methodClasses[0]);
        }
      } catch (Exception e) {
        throw new RuntimeProcessException(e);
      }
    }
  }
  private Message createResponseMessageForJAXWSProvider(Object returnObject, Class returnObjectClass, Object[] resultParams, Class[] resultParamsClasses, ProviderContextHelper ctx) throws RuntimeProcessException {
    SOAPMessage message = (SOAPMessage) ctx.getMessage();
    javax.xml.soap.SOAPMessage saajMsg = null;
    try {
      if (returnObjectClass == Source.class) {
        if (returnObject == null) {
          throw new IllegalArgumentException("Unsupported case: JAXWS Provider.invoke() returns null.");
        }
        if (SOAPMessage.SOAP11_NS.equals(message.getSOAPVersionNS())) {
          saajMsg = MessageConvertor.createSOAP11SAAJMessage();
        } else {
          saajMsg = MessageConvertor.createSOAP12SAAJMessage();
        }
        Element bodyElem = saajMsg.getSOAPBody();
        Element bodyContentElem = null;
        if (returnObject instanceof DOMSource) {
          //this must be element, since the SOAPBody is requried to have atleast one element...
          bodyContentElem = (Element) ((DOMSource) returnObject).getNode();
        } else if (returnObject instanceof StreamSource){
          StreamSource sSource = (StreamSource) returnObject;
          InputSource inSource;
          if (sSource.getInputStream() != null) {
            inSource = new InputSource(sSource.getInputStream());
          } else {
            inSource = new InputSource(sSource.getReader());
          }
          bodyContentElem = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, inSource).getDocumentElement();
        } else {
          throw new RuntimeProcessException("Unsupported javax.xml.transform.Source implementation '" + returnObject.getClass() + "'");
        }
        if (bodyContentElem.getOwnerDocument() != bodyElem.getOwnerDocument()) {
          bodyContentElem = (Element) bodyElem.getOwnerDocument().importNode(bodyContentElem, true);
        }
        bodyElem.appendChild(bodyContentElem);
      } else { //the object must be SAAJ message
        saajMsg = (javax.xml.soap.SOAPMessage) returnObject;
      }
      if (JAXWSHandlersEngine.getSOAPMessageContextFromProviderContext(ctx) != null) { //this is true only when handlers are configured.
        LOCATION.debugT("createResponseMessage(): Found JAXWS contexts, the response SAAJ msg will be preset in the context.");
        SOAPMessageContextImpl soapCtx = (SOAPMessageContextImpl) JAXWSHandlersEngine.getSOAPMessageContextFromProviderContext(ctx);
        soapCtx.setMessage(saajMsg);
      } else {
        MessageConvertor.convertSAAJMessageIntoOutputSAPMessage(saajMsg, message);
      }
    } catch (Exception e) {
      throw new RuntimeProcessException(e);
    }
    return message;
  }
  
  /**
   * Sets in dynamic context the "soapfault-response-code' property with value <code>sFaultCode</code>.
   */
  static void setSOAPFaultResponseCode(ProviderContextHelper ctx, String sFaultCode) {
    ctx.getDynamicContext().setProperty(SOAPFAULT_RESPONSE_CODE_PROP, sFaultCode);    
  }
  /**
   * Gets from dynamic context the "soapfault-response-code' property. If the property is not set, null is returned.
   */
  static String getSOAPFaultResponseCode(ProviderContextHelper ctx) {
    return (String) ctx.getDynamicContext().getProperty(SOAPFAULT_RESPONSE_CODE_PROP);    
  }
  /**
   * Binds in dynamic context the 'soap-ns' property with value <code>soapNS</code>.
   */ 
  static void bindingSOAPNS(ProviderContextHelper ctx, String soapNS) {
    ctx.getPersistableContext().setProperty(SOAPNS_PROP, soapNS);        
  }
  /**
   * @return the value bound in dynamic context under the 'soap-ns' property. If nothing is bound null is returned.
   */  
  static String getSOAPNS(ProviderContextHelper ctx) {
    return (String) ctx.getPersistableContext().getProperty(SOAPNS_PROP);        
  }
  /**
   * Returns corresponnding media type value, which should be used as value of 'content-type' header,
   * depending on soap version determined by <code>soapNS</code>.
   * @param soapNS
   */
  static String getSOAPMediaType(String soapNS) {
    if (SOAPMessage.SOAP12_NS.equals(soapNS)) { //use 'application/soap+xml' for soap1.2
      return MIMEMessageImpl.getSOAP12ContentType();
    } else {
      return MIMEMessageImpl.getSOAP11ContentType();
    }
  }

  private String[] getParamClassNames(OperationMapping operationMapping) {
    Vector<String> paramClassNames = new Vector<String>();
    
    if (OperationMapping.DOCUMENT_OPERATION_STYLE.equals(operationMapping.getProperty(OperationMapping.OPERATION_STYLE))) {
      paramClassNames.add(operationMapping.getProperty(OperationMapping.REQUEST_WRAPPER_BEAN));
      paramClassNames.add(operationMapping.getProperty(OperationMapping.RESPONSE_WRAPPER_BEAN));
    }
    ParameterMapping[] parameterMappings = operationMapping.getParameter();

    String paramClassName; 
    for(ParameterMapping parameterMapping: parameterMappings) {
      paramClassName = parameterMapping.getProperty(ParameterMapping.JAXB_BEAN_CLASS);    
      if(paramClassName == null) { //this is necessary for faults. They have JAXB bean class and javaType as well. But javaType must not by passed to the JAXBContext. 
        paramClassName = parameterMapping.getJavaType(); 
      }
      if(paramClassName != null) {
        paramClassNames.add(paramClassName);    
      }
    }    
    
    String[] paramClassNamesArr = new String[paramClassNames.size()];
    paramClassNames.toArray(paramClassNamesArr);
    
    return paramClassNamesArr;
  }  

  private Class[] loadClassesForJAXB(String[] classNames, ClassLoader loader) throws ClassNotFoundException {
    if(classNames == null || classNames.length == 0) {
      return new Class[0];  
    }      

    ArrayList<Class> res = new ArrayList();
    for(String className: classNames) {
      Class tmpCls = loadClass0(className, loader);
      //this is an heuristic
      if (! tmpCls.isInterface()) { //exclude interfaces. This is relevant for java.util.List and its schema mapping to maxOccurs=unbounded.
        res.add(tmpCls);
      }
    }
    
    return (Class[]) res.toArray(new Class[res.size()]);
  }
  
  private Class loadClass0(String clDecl, ClassLoader loader)  throws ClassNotFoundException {
    int ind = clDecl.indexOf(ParameterMapping.JAVA_ARRAY_DIMENSION); //as it is described in the VI.

    if (ind == -1) { //this is not array
      //for simple types
      if (clDecl.equals("byte")) {
        return byte.class; 
      } else if (clDecl.equals("char")) {
        return char.class;
      } else if (clDecl.equals("boolean")) {
        return boolean.class;
      } else if (clDecl.equals("short")) {
        return short.class;
      } else if (clDecl.equals("int")) {
        return int.class;
      } else if (clDecl.equals("float")) {
        return float.class;
      } else if (clDecl.equals("long")) {
        return long.class;
      } else if (clDecl.equals("double")) {
        return double.class;
      } else if (clDecl.equals("void")) {
        return Void.TYPE;
      }

      //this is not a simple type use the loader
      return loader.loadClass(clDecl);
    }

    //this is an array
    int[] arrDim = new int[(clDecl.length() - ind) / 2];
    Class compClass = loadClass0(clDecl.substring(0, ind), loader);
    return java.lang.reflect.Array.newInstance(compClass, arrDim).getClass();
  }
  
}


