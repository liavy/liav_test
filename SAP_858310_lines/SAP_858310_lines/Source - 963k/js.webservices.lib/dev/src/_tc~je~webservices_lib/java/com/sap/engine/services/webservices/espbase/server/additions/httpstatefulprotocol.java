package com.sap.engine.services.webservices.espbase.server.additions;

import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.lib.xml.parser.ParserException;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenWriter;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.messaging.SOAPMessage;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.BaseMessageException;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.BaseProtocolException;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ExceptionConstants;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.ProcessException;
import com.sap.engine.services.webservices.espbase.server.additions.exceptions.TBindingResourceAccessor;
import com.sap.engine.services.webservices.espbase.server.runtime.ProviderContextHelperImpl;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class HTTPStatefulProtocol implements ProviderProtocol/*, ProtocolFactory*/ { 
  //protoocol ID
  public static final String PROTOCOL_NAME  =  "HTTP_StatefulProtocol";
  //feature name
  private static final String STATEFUL_FEATURE_NAME  =  "http://www.sap.com/webas/630/soap/features/session/";
  //session type
  private static final String SESSION_METHOD  =  "SessionMethod"; //property declaring the way ot setting session.
  private static final String SESSION_METHOD_DEFAULT_VALUE  =  "httpCookies"; //default value for SESSION_METHOD property

  private static final String SESSION_TIMEOUT  = "SessionTimeout"; //property declaring the timeout.
  //end session header
  private static final String MAINTAIN_SESSION_HEADER  =  "maintainSession";
  //soap element names
  private static final String SOAP_SESSION_WRAPPER_ELEMENT  =  "Session";
  private static final String SOAP_ENABLESESSION_ELEMENT  =  "enableSession";

  //infinite session timeout
  private static final int INFINITE_SESSION_TIMEOUT  =  1800; //30 minutes

  public String getProtocolName() {
    return PROTOCOL_NAME;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.esp.Protocol#handleFault(com.sap.engine.interfaces.webservices.esp.ConfigurationContext)
   */
  public int handleFault(ConfigurationContext context) throws ProtocolException {
    return CONTINUE;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.esp.Protocol#handleRequest(com.sap.engine.interfaces.webservices.esp.ConfigurationContext)
   */
  public int handleRequest(ConfigurationContext ctx) throws ProtocolException, MessageException {
    ProviderContextHelper context = (ProviderContextHelper) ctx;
    
    //check whether there is any config for this protocol
    PropertyListType pList = context.getStaticContext().getRTConfiguration().getSinglePropertyList();
    PropertyType sessionMethodProp = pList.getProperty(STATEFUL_FEATURE_NAME, SESSION_METHOD);    
    /*if (sessionMethodProp == null) { 
      return CONTINUE;//no configuration set
    }*/

    HTTPTransport transport = (HTTPTransport) context.getTransport();    
    
    String bType = context.getStaticContext().getInterfaceMapping().getBindingType();
    if (bType.equals(InterfaceMapping.HTTPGETBINDING) || bType.equals(InterfaceMapping.HTTPPOSTBINDING)){
      return CONTINUE;
    }
    
    
    javax.servlet.http.HttpServletRequest request = transport.getRequest();
    
    boolean isSessionDisabled = false;
    boolean isSOAPBodyEmpty = false;
    if (request.getContentLength() == 0) { //check for HTTP specific head to close the session. In this case empty HTTP body is sent
      String maintSession = request.getHeader(MAINTAIN_SESSION_HEADER);
      //not to create session or to stop session
      if (maintSession != null && maintSession.equals("false")) {
        isSessionDisabled = true;
      }
    } else {    
      SOAPMessage message = null;
      try {
        message = getSOAPMessage((ProviderContextHelper) ctx);
      } catch (Exception e) {
        throw new ProtocolException(e);
      }
      Element[] headers = message.getSOAPHeaders().getHeaders();
  
      isSessionDisabled = isSessionDisabledHeaderAvailable(headers);
  
      XMLTokenReader reader  = message.getBodyReader();
      if (isSessionDisabled){
        try { //positioning on start or end Element
          reader.next(); //positioning of first body element
          reader.passChars();
        } catch (ParserException pE) {
          throw new BaseProtocolException(pE);
        }


        //if (reader.getState() == XMLTokenReader.ENDELEMENT && reader.getLocalNameCharArray().equals("Body")) {
        if (reader.getCurrentLevel() == 1) { //since BODY is on first baseXMLLevel
          isSOAPBodyEmpty = true;
        }
      }
    }

    // request ID is not valid
//    if ((request.getRequestedSessionId() != null) && (! request.isRequestedSessionIdValid())) {
//      MessageException mE = new BaseMessageException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.REQUESTED_SESSION_IS_NOT_VALID, new Object[]{request.getRequestedSessionId()});
//      try {
//        Message msg = context.createErrorMessage(mE);
//        context.setMessage(msg);
//        context.setMessageSemantic(ProviderContextHelper.FAULT_MSG);
//      } catch (RuntimeProcessException rtPE) {
//        throw new BaseProtocolException(rtPE);
//      }
//      return ProviderProtocol.BACK;
//    }

    /*HttpSession httpSession = request.getSession();
    if (httpSession == null){ // session can be null for local calls
      return CONTINUE;
    }*/
    
    if (isSessionDisabled && isSOAPBodyEmpty) { //request for closing the session
      //context.getRuntimeContext().invalidateSession(httpSession.getId()); //clears the instance(EJB)
      HttpSession httpSession = request.getSession(false);
      if (httpSession != null){
        httpSession.invalidate(); //invalidates the http session
      }
      
      SOAPMessage message;
      try {
        Message tmpMsg = context.getMessage();
        if (tmpMsg instanceof SOAPMessage) {
          message = (SOAPMessage) tmpMsg;
          message.clear(); //clear message to be reused
        } 
//        else if (tmpMsg instanceof InternalMIMEMessage) {
//          InternalMIMEMessage mimeMsg = (InternalMIMEMessage) tmpMsg;
//          mimeMsg.clear(); //clear message to be reused
//          message = mimeMsg.getSOAPMessage();
//        } 
         else {
          throw new BaseProtocolException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.UNKNOW_MESSAGE_INSTANCE, new Object[]{tmpMsg});      
        }
      } catch (RuntimeProcessException rtE) {
        throw new ProtocolException(rtE);
      } 
      message.initWriteMode(SOAPHTTPTransportBinding.getSOAPNS(context));
      XMLTokenWriter writer = message.getBodyWriter();
      //adding empty Body
      try {
        writer.enter(NS.SOAPENV, "Body");
        writer.leave();
        context.setMessage(message);
        context.setMessageSemantic(ProviderContextHelper.NORMAL_RESPONSE_MSG);
      } catch (Exception ioE) {
        throw new BaseProtocolException(ioE);
      }
      return ProviderProtocol.BACK;
    } else if (isSessionDisabled) { //request to stateful instance without creating session
      HttpSession httpSession = request.getSession(false);
      if (httpSession != null && httpSession.isNew()){
        httpSession.invalidate(); //invalidates the http session
        return ProviderProtocol.CONTINUE;  
      }  
    } /*else if (! httpSession.isNew() && isSessionDisabled) {
      throw new BaseProtocolException(new IllegalStateException("Invalidating old session with disable header"));
    }*/
    
    /*if (sessionMethodProp == null || (! SESSION_METHOD_DEFAULT_VALUE.equals(sessionMethodProp.get_value()))) {
      throw new BaseProtocolException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.INCORRECT_PROPERTY_VALUE,
                                  new Object[]{SESSION_METHOD, STATEFUL_FEATURE_NAME, sessionMethodProp != null ? sessionMethodProp.get_value() : "null"});

    }*/
    
    //sets the sessionid.
    if (sessionMethodProp != null) {
      HttpSession httpSession = request.getSession();
      if (httpSession != null){ // session can be null for local calls
        context.getDynamicContext().setProperty(ProviderContextHelperImpl.SESSION_ID, httpSession.getId());
      }
    }
    return ProviderProtocol.CONTINUE;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.esp.Protocol#handleResponse(com.sap.engine.interfaces.webservices.esp.ConfigurationContext)
   */
  public int handleResponse(ConfigurationContext context) throws ProtocolException {
    return ProviderProtocol.CONTINUE;
  }

  private boolean isSessionDisabledHeaderAvailable(Element[] headers) {
    Element sessionElement;

    for (int i = 0; i < headers.length; i++) {
      sessionElement = (Element) headers[i];
      if (sessionElement.getNamespaceURI().equals(STATEFUL_FEATURE_NAME) &&
          sessionElement.getLocalName().equals(SOAP_SESSION_WRAPPER_ELEMENT)) {
        NodeList chNodes = sessionElement.getElementsByTagName(SOAP_ENABLESESSION_ELEMENT);
        if (chNodes.getLength() == 1) { //found element
          Element enableElement = (Element) chNodes.item(0);
          Node text = enableElement.getFirstChild();
          if (text instanceof Text) {
            if (((Text) text).getData().trim().equalsIgnoreCase("false")) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  public SOAPMessage getSOAPMessage(ProviderContextHelper ctx) throws Exception {
    SOAPMessage msg;
    Message tmpMsg = ctx.getMessage();
//    if (tmpMsg instanceof InternalMIMEMessage) {
//      msg = ((InternalMIMEMessage) tmpMsg).getSOAPMessage();        
//    } else 
    if (tmpMsg instanceof SOAPMessage) {
      msg = (SOAPMessage) tmpMsg;
    } else {
      throw new BaseProtocolException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.UNKNOW_MESSAGE_INSTANCE, new Object[]{tmpMsg});
    }
    return msg;
  }



//================================ OLD Implementation ===========================================
//  public boolean handleFault(ProtocolContext context) throws ProtocolException {
////    System.out.println("HTTPProtocol HANDLE_FAULT");
//    return true;
//  }
//
//  public boolean handleRequest(ProtocolContext context) throws ProtocolException, MessageException {
//    
//    HTTPTransport transport = (HTTPTransport) context.getRuntimeContext().getTransport();
//    javax.servlet.http.HttpServletRequest request = transport.getRequest();
//
//    RawMessage rawMessage;
//    try{
//      rawMessage = context.getRuntimeContext().getRuntimeTransportBinding().getRawMessage();
//    } catch (TransportBindingException tbE) {
//      throw new BaseProtocolException(tbE);
//    }
//
//    boolean isSessionDisabled = false;
//    boolean isSOAPBodyEmpty = false;
//    ArrayList headers = null;
//
//    if (rawMessage instanceof InternalSOAPMessage) { //in case of SOAPRequest
//      InternalSOAPMessage soapMessage = (InternalSOAPMessage) rawMessage;
//      headers = soapMessage.getHeaders();
//
//      isSessionDisabled = isSessionDisabledHeaderAvailable(headers);
//
//      XMLTokenReader reader  = soapMessage.getReader();
//
//      try { //positioning on start or end Element
//        reader.next(); //positioning of first body element
//        reader.passChars();
//      } catch (ParserException pE) {
//        throw new BaseProtocolException(pE);
//      }
//
//      //if (reader.getState() == XMLTokenReader.ENDELEMENT && reader.getLocalNameCharArray().equals("Body")) {
//      if (reader.getCurrentLevel() == 1) { //since BODY is on first baseXMLLevel
//        isSOAPBodyEmpty = true;
//      } else {
//        try {
//          soapMessage.setBodyWrapperData(reader);
//        } catch (ProcessException pE) {
//          throw new BaseProtocolException(pE);
//        }
//      }
//    } else { //check for HTTP specific head to close the session
//      String maintSession = request.getHeader(MAINTAIN_SESSION_HEADER);
//      //not to create session or to stop session
//      if (maintSession != null && maintSession.equals("false")) {
//        isSessionDisabled = true;
//      }
//    }
//
//    // request ID is not valid
//    if ((request.getRequestedSessionId() != null) && (! request.isRequestedSessionIdValid())) {
//      MessageException mE = new BaseMessageException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.REQUESTED_SESSION_IS_NOT_VALID, new Object[]{request.getRequestedSessionId()});
//      try {
//        context.getRuntimeContext().getRuntimeTransportBinding().createFaultMessage(mE);
//      } catch (TransportBindingException tbE) {
//        throw new BaseProtocolException(tbE);
//      }
//
//      return false;
//      //throw mE;
//    }
//
//    HttpSession httpSession = request.getSession();
//
////    if (isSOAPBodyEmpty && ! isSessionDisabled) {
////      System.out.println("=========================SessionDisabled: " + isSessionDisabled +  " isSOAPbodyEmpty: " + isSOAPBodyEmpty + " header: " + headers);
////    }
//
//    if (isSessionDisabled && isSOAPBodyEmpty) { //to close the session
//      context.getRuntimeContext().invalidateSession(httpSession.getId()); //clears the instance(EJB)
//      httpSession.invalidate(); //invalidates the http session
//
//      InternalSOAPMessage message = new InternalSOAPMessage();
//      XMLTokenWriter writer = message.initSerializationMode();
//      //adding empty Body
//      try {
//        writer.enter(NS.SOAPENV, "Body");
//        writer.leave();
//        context.getRuntimeContext().getRuntimeTransportBinding().setRawMessage(message);
//      } catch (Exception ioE) {
//        throw new BaseProtocolException(ioE);
//      }
//      return false;
//    } else if (httpSession.isNew() && isSessionDisabled) { //request to stateful instance without creating session
//      httpSession.invalidate();
//      return true;
//    } else if (! httpSession.isNew() && isSessionDisabled) {
//      throw new BaseProtocolException(new IllegalStateException("Invalidating old session with disable header"));
//    }
//
////! ! ! ! ! ! It is better old sessions not to be initialized again, but if they cannot register instance it's a problem
////    if (! httpSession.isNew()) {//this is old session
////      context.getRuntimeContext().setSessionID(httpSession.getId());
////      return true;
////    }
//
//    Feature sessionFeature = getFeature(context.getGlobalFeatures());
//
//    PropertyDescriptor sessionMethodProp =  sessionFeature.getConfiguration().getProperty(SESSION_METHOD);
//    if (sessionMethodProp == null || !sessionMethodProp.hasValueAttrib()
//                                  || !sessionMethodProp.getValue().equals(SESSION_METHOD_DEFAULT_VALUE)) {
//      throw new BaseProtocolException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.INCORRECT_PROPERTY_VALUE,
//                                  new Object[]{SESSION_METHOD, STATEFUL_FEATURE_NAME, sessionMethodProp.hasValueAttrib() ? sessionMethodProp.getValue():"null"});
//
//    }
//
////    int sessionTimeout = INFINITE_SESSION_TIMEOUT;
////
////    //search for SESSION_TIMEOUT property
////    PropertyDescriptor timeoutProp = sessionFeature.getConfiguration().getProperty(SESSION_TIMEOUT);
////    if (timeoutProp != null && timeoutProp.hasValueAttrib()) {
////      sessionTimeout = Integer.parseInt(timeoutProp.getValue());
////    }
////    httpSession.setMaxInactiveInterval(sessionTimeout); //setting the new session timeout
////    else { //use standard HTTP session time out
////      sessionTimeout = httpSession.getMaxInactiveInterval();
////    }
//
////    if (sessionTimeout < 0) { //in case the infinite session timeout.
////      sessionTimeout = INFINITE_SESSION_TIMEOUT;
////    }
//
//    //setting values in the RuntimeContext
//    context.getRuntimeContext().setSessionID(httpSession.getId());
//    context.getRuntimeContext().setSessionTimeout(httpSession.getMaxInactiveInterval());
//
//    return true;
//  }
//
//  public boolean handleResponse(ProtocolContext context) throws ProtocolException {
////    System.out.println("HTTPProtocol HANDLE_RESPONSE");
//    return true;
//  }
//
//  public Config getRequiredFeatures() {
//    return null;
//  }
//
//  public void init() {
//  }
//
//  private Feature getFeature(Feature[] features) throws ProtocolException {
//    for (int i = 0; i < features.length; i++) {
//      if (features[i].getFeatureName().equals(STATEFUL_FEATURE_NAME)) {
//        return features[i];
//      }
//    }
//
//    throw new BaseProtocolException(TBindingResourceAccessor.getResourceAccessor(), ExceptionConstants.CANNOT_FIND_FEATURE_CONFIG, new Object[]{STATEFUL_FEATURE_NAME, "global"});
//  }
//
//
//
//	/* (non-Javadoc)
//	 * @see com.sap.engine.interfaces.webservices.runtime.component.ProtocolFactory#newInstance()
//	 */
//	public Protocol newInstance() {
//		// TODO Auto-generated method stub
//		return new HTTPStatefulProtocol();
//	}
//
}

