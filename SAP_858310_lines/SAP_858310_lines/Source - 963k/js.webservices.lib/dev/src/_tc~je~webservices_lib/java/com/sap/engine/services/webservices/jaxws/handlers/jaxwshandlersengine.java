/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.handlers;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ServiceEndpointWrapper;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.messaging.impl.MessageConvertor;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.jaxws.JAXWSConstants;
import com.sap.engine.services.webservices.jaxws.ctx.LogicalMessageContextImpl;
import com.sap.engine.services.webservices.jaxws.ctx.MessageContextImpl;
import com.sap.engine.services.webservices.jaxws.ctx.SOAPMessageContextImpl;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.tc.logging.Location;

/**
 * This class provides static utility methods for dealing with handlers.  
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 25, 2006
 */
public class JAXWSHandlersEngine {
  /**
   * Under this property SOAPMessageContext instance is bound in the ProviderContextHelper and in ClientConfigurationContext
   */
  private static String SOAPMESSAGE_CONTEXT_PROPERTY = "jaxws-soap-message-context";
  /**
   * Under this property LogicalPMessageContext instance is bound in the ProviderContextHelper and in ClientConfigurationContext
   */
  private static String LOGICALMESSAGE_CONTEXT_PROPERTY = "jaxws-logical-message-context";
  /**
   * Under this property handler chain bound in the ProviderContextHelper.
   */
  private static String HANDLERCHAIN_INUSE = "jaxws-handler-chain-inuse";
  private static final Location LOC = Location.getLocation(JAXWSHandlersEngine.class);

  public static List<Class> getHandlersClassesFromHandlerChains(String hChains,
      ClassLoader loader) throws JAXWSHandlersException {
    return getHandlersClassesFromHandlerChains(new InputSource(
        new StringReader(hChains)), loader);
  }

  public static List<Class> getHandlersClassesFromHandlerChains(
      InputStream hChains, ClassLoader loader) throws JAXWSHandlersException {
    return getHandlersClassesFromHandlerChains(new InputSource(hChains), loader);
  }
  
  /**
   * Parses <code>hChains</code> and constructs a list of handlers classes,
   * where the order of the handlers is as according to JAXWS section 9.2.1.2.
   * @param hChains this is a 'handler-chains' element into which multiple 'handler-chain' elements could be
   *        described.
   * @param loader ClassLoader instance to be used to load the handler classes.
   * @return an ordered list of hanlder classes
   * @throws Exception
   */
  public static List<Class> getHandlersClassesFromHandlerChains(InputSource hChains, ClassLoader loader) throws JAXWSHandlersException {
    List<Class> logicalHandlers = new ArrayList<Class>();
    ArrayList<Class> protocolHandlers = new ArrayList<Class>();
    try {
      Element h_chains_elem;
      h_chains_elem = SharedDocumentBuilders.parse(SharedDocumentBuilders.NAMESPACE_AWARE_DB, hChains).getDocumentElement();
      //Access directly the 'handler-class' elements - this should not cause any issues.
      NodeList h_classes = h_chains_elem.getElementsByTagNameNS(JAXWSConstants.JEE_NS, JAXWSConstants.HANDLER_CLASS_ELEMENT);
      for (int i = 0; i < h_classes.getLength(); i++) {
        Element h_class_elem = (Element) h_classes.item(i);
        Text txt = (Text) h_class_elem.getFirstChild();
        String class_name = txt.getData().trim();
        Class h_cls = loader.loadClass(class_name);
        if (Handler.class.isAssignableFrom(h_cls)) {
          if (LogicalHandler.class.isAssignableFrom(h_cls)) {
            logicalHandlers.add(h_cls);
          } else if (SOAPHandler.class.isAssignableFrom(h_cls)){ //this is SOAP protocol handler
            protocolHandlers.add(h_cls);
          } else {
            throw new JAXWSHandlersException("Unsupported protocol handler class '" + h_cls + "'");
          }
        } else {
          throw new JAXWSHandlersException("Class '" + h_cls + "' does not implement " + Handler.class.getName());
        }
      }
      //appends protocol handlers after the logical handler, as required by JAXWS.
      logicalHandlers.addAll(protocolHandlers);
    } catch (Exception e) {
      throwJAXWSHandlersException(e);
    }
    return logicalHandlers;
  }

  public static List<Class> mergeServiceAndPortHandlerChains(List<Class> serviceHandlers, List<Class> portHandlers) {
    if (serviceHandlers == null || serviceHandlers.isEmpty()) {
      return portHandlers;
    }

    if (portHandlers == null || portHandlers.isEmpty()) {
      return serviceHandlers;
    }

    List<Class> logicalHandlers = new ArrayList<Class>();
    List<Class> protocolHandlers = new ArrayList<Class>();

    for (Class h_cls : serviceHandlers) {
      if (LogicalHandler.class.isAssignableFrom(h_cls)) {
        logicalHandlers.add(h_cls);
      } else if (SOAPHandler.class.isAssignableFrom(h_cls)) { // this is SOAP protocol handler
        protocolHandlers.add(h_cls);
      }
    }

    for (Class h_cls : portHandlers) {
      if (LogicalHandler.class.isAssignableFrom(h_cls)) {
        logicalHandlers.add(h_cls);
      } else if (SOAPHandler.class.isAssignableFrom(h_cls)) { // this is SOAP protocol  handler
        protocolHandlers.add(h_cls);
      }
    }

    logicalHandlers.addAll(protocolHandlers);
    return logicalHandlers;
  }

  /**
   * Intantiates and initializes the handlers in <code>handerChainClasses</code>.
   * @param handlerChainClasses the list returned by <code>getHandlersClassesFromHandlerChains</code> method.
   * @return
   * @throws JAXWSHandlersException
   */
  public static List<Handler> initializedHandlerChain(List<Class> handlerChainClasses) throws JAXWSHandlersException {
    List<Handler> res = new ArrayList<Handler>();
    try {
      for (Class h_cls : handlerChainClasses) {
        Handler h = (Handler) h_cls.newInstance();
        //TODO according to the JAXWS spec, any requested injections should come here. But for now we will not impl this step.
        //invoke PostConstruct method
        Method[] methods = h_cls.getMethods();
        for (Method method : methods) {
          Annotation ann = method.getAnnotation(PostConstruct.class);
          if (ann != null) {
            method.invoke(h, (Object[]) null);
            break;
          }
        }
        res.add(h);
      }
    } catch (Exception e) {
      throwJAXWSHandlersException(e);
    }
    return res;
  }
  /**
   * Travetses <code>hc</code> and invokes the method, if any, annotated with PreDestoy annotation.
   * @param hc
   * @throws JAXWSHandlersException
   */
  public static void destroyHandlerChain(List<Handler> hc) throws JAXWSHandlersException {
    try {
      for (Handler handler : hc) {
        Method[] methods = handler.getClass().getMethods();
        for (Method method : methods) {
          if (method.getAnnotation(PreDestroy.class) != null) {
            method.invoke(handler, (Object[]) null);
            break;
          }
        }
      }
    } catch (Exception e) {
      throwJAXWSHandlersException(e);
    }
  }
  /**
   * Invokes <code>hChain</code> in the outbound direction. 
   * @param hChain
   * @param logicalMsgCtx
   * @param soapMsgCtx
   * @param response whether a response message could be created. True only for consumer side request-response operations. False for the provider side. 
   * @param hcPosition the starting index in the chain
   */
  public static void invokeHandlerChainOutbound(List<Handler> hChain, LogicalMessageContextImpl logicalMsgCtx, SOAPMessageContextImpl soapMsgCtx, boolean response, int hcPosition) throws ProtocolException, RuntimeException {
    LOC.debugT("invokeHandlerChainOutbound(): entered...");
    logicalMsgCtx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true); //this is needed for setting the context in the right direction
    Handler handler;
    for (int i = hcPosition; i < hChain.size(); i++) {
      handler = hChain.get(i);
      bindIndexOfLastCalledOutboundHandler(i, logicalMsgCtx);
      boolean res = true;
      LOC.debugT("invokeHandlerChainOutbound(): about to invoke handler.handleMessage() on '" + handler + "'");
      try {
        if (handler instanceof LogicalHandler) {
          res = handler.handleMessage(logicalMsgCtx);
        } else {
          res = handler.handleMessage(soapMsgCtx);
        }
        LOC.debugT("invokeHandlerChainOutbound(): handler.handleMessage() returned " + res);
      } catch(RuntimeException rtE) { //this catches ProtocolException also.
        // in case of SOAPFaultException -> create a new one...
        LOC.debugT("invokeHandlerChainOutbound(): handler.handleMessage() returned " + rtE);
        if (response) { //for Request-Response operations. This needs further check, perhaps a fault message should be created
          if (rtE instanceof ProtocolException) {
            logicalMsgCtx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false); //reverse the direction
            
            if (!isSAAJMsgAFault(soapMsgCtx.getMessage())) { //to be commented ?
              createAndBindSAAJFaultMsg(rtE, soapMsgCtx);
            }            
            invokeHandlerChainFault(hChain, logicalMsgCtx, soapMsgCtx, i);
            invokeHandlerChainCloseOut(hChain, logicalMsgCtx);   
            break;
          } else {                    
            invokeHandlerChainCloseOut(hChain, logicalMsgCtx);
            throw rtE;
          }
                    
        } else {
          bindIndexOfLastCalledInboundHandler(0, logicalMsgCtx);
          invokeHandlerChainCloseIn(hChain, logicalMsgCtx);
          throw rtE; //dispatch the exception
        }
      }
      if (res == false) {
        if (response) { //for Request-Response operations. This needs further check.
          invokeHandlerChainInbound(hChain, logicalMsgCtx, soapMsgCtx, false, i);
          return; //dispatches the message;
          //throw new UnsupportedOperationException("Outbound with response is currently not supported. This case is valid for the consumer side.");
        } else {
          invokeHandlerChainCloseIn(hChain, logicalMsgCtx);
          return; //the message is dispatched.
        }
      }
    }
     
    if (!response) {
      invokeHandlerChainCloseIn(hChain, logicalMsgCtx);
      return; //the message is dispatched.
    }
  }
  
  /**
   * Invokes <code>hChain</code> in the inbound direction. 
   * @param hChain
   * @param logicalMsgCtx
   * @param soapMsgCtx
   * @param response whether a response message could be created. True only for provider side request-response operations. False for the consumer side. 
   * @param hcPosition the starting index in the chain
   */
  public static void invokeHandlerChainInbound(List<Handler> hChain, LogicalMessageContextImpl logicalMsgCtx, SOAPMessageContextImpl soapMsgCtx, boolean response, int hcPosition) throws ProtocolException, RuntimeException {
    LOC.debugT("invokeHandlerChainInbound(): entered...");
    logicalMsgCtx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);
    Handler handler;
    for (int i = hcPosition; i >= 0; i--) {
      handler = hChain.get(i);
      LOC.debugT("invokeHandlerChainInbound(): about to invoke handler.handleMessage() on '" + handler + "'");
      bindIndexOfLastCalledInboundHandler(i, logicalMsgCtx);
      boolean res;
      try {
        if (handler instanceof LogicalHandler) {
          res = handler.handleMessage(logicalMsgCtx);
        } else {
          res = handler.handleMessage(soapMsgCtx);
        }
        LOC.debugT("invokeHandlerChainInbound(): about to invoke handler.handleMessage() result " + res);
      } catch (ProtocolException pE) {
        LOC.debugT("invokeHandlerChainInbound(): about to invoke handler.handleMessage() result " + pE);
        if (response) { //for Request-Response operations. This needs further check, perhaps a fault message should be created
          if (! isSAAJMsgAFault(soapMsgCtx.getMessage())) {
            createAndBindSAAJFaultMsg(pE, soapMsgCtx);
          }
          logicalMsgCtx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true); //reverse the direction
          invokeHandlerChainFault(hChain, logicalMsgCtx, soapMsgCtx, i + 1);
          invokeHandlerChainCloseIn(hChain, logicalMsgCtx);
          return; //dispatches the message;
        } else {
          invokeHandlerChainCloseOut(hChain, logicalMsgCtx);
          throw pE; //dispatch the exception
        }
      } catch (RuntimeException rtE) {
        LOC.debugT("invokeHandlerChainInbound(): about to invoke handler.handleMessage() result " + rtE);
        if (response) { //for Request-Response operations. This needs further check, perhaps a fault message should be created
          if (! isSAAJMsgAFault(soapMsgCtx.getMessage())) {
            createAndBindSAAJFaultMsg(rtE, soapMsgCtx);
          }
          logicalMsgCtx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true); //reverse the direction
          invokeHandlerChainCloseIn(hChain, logicalMsgCtx);
          return; //dispatches the message;
        } else {
          invokeHandlerChainCloseOut(hChain, logicalMsgCtx);
          throw rtE; //dispatch the exception
        }
      }
      if (res == false) {
        if (response) { //for Request-Response operations. This needs further check.
          invokeHandlerChainOutbound(hChain, logicalMsgCtx, soapMsgCtx, false, i + 1); // i ?
          return; //dispatches the message;
        } else {
          invokeHandlerChainCloseOut(hChain, logicalMsgCtx);
          return; //the message is dispatched.
        }
      }
    }
    
    if (! response) { //for Request-Response operations. This needs further check.
      invokeHandlerChainCloseOut(hChain, logicalMsgCtx);
      return; //the message is dispatched.
    }       
  }
  
  public static void invokeHandlerChainCloseOut(List<Handler> hChain, MessageContext msgCtx) {
    LOC.debugT("invokeHandlerChainClose(): entered with direction 'outbound' direction");
    int index = getBoundIndexOfLastCalledOutboundHandler(msgCtx);
////    if (outbound) { 
//      for (int i = index; i < hChain.size(); i++) {
//        Handler h = hChain.get(i);
//        LOC.debugT("invokeHandlerChainClose(): invoke 'close() on handler '" + h);
//        h.close(msgCtx);
//      }
//    } else { //this is inbound
      for (int i = index; i >= 0; i--) {
        Handler h = hChain.get(i);
        LOC.debugT("invokeHandlerChainClose(): invoke 'close() on handler '" + h);
        h.close(msgCtx);
      }
//    }
  }
  
  public static void invokeHandlerChainCloseIn(List<Handler> hChain, MessageContext msgCtx) {
    LOC.debugT("invokeHandlerChainClose(): entered with direction 'inbound' direction");
    int index = getBoundIndexOfLastCalledInboundHandler(msgCtx);
//    if (outbound) {
      for (int i = index; i < hChain.size(); i++) {
        Handler h = hChain.get(i);
        LOC.debugT("invokeHandlerChainClose(): invoke 'close() on handler '" + h);
        h.close(msgCtx);
      }
//    } else { //this is inbound
//      for (int i = index; i >= 0; i--) {
//        Handler h = hChain.get(i);
//        LOC.debugT("invokeHandlerChainClose(): invoke 'close() on handler '" + h);
//        h.close(msgCtx);
//      }
//    }
  }
  
  public static void invokeHandlerChainFault(List<Handler> hChain, LogicalMessageContextImpl logicalMsgCtx, SOAPMessageContextImpl soapMsgCtx, int hcPosition) throws ProtocolException, RuntimeException {
    //the direction must be preset by the caller method
    boolean isOutbound = ((Boolean) logicalMsgCtx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();
    LOC.debugT("invokeHandlerChainFault(): entered with 'outbound' direction " + isOutbound);
    Handler handler;
    if (isOutbound) {
      hcPosition = getBoundIndexOfLastCalledInboundHandler(logicalMsgCtx);
      for (int i = hcPosition; i < hChain.size(); i++) {
        handler = hChain.get(i);
        LOC.debugT("invokeHandlerChainFault(): about to invoke handler.handleFault() on " + handler);
        boolean res;
        try {
          if (handler instanceof LogicalHandler) {
            res = handler.handleFault(logicalMsgCtx);
          } else {
            res = handler.handleFault(soapMsgCtx);
          }
          LOC.debugT("invokeHandlerChainFault(): handler.handleFault() result " + res);
        } catch (ProtocolException e) {
          LOC.debugT("invokeHandlerChainFault(): handler.handleFault() result " + e);
          invokeHandlerChainCloseIn(hChain, logicalMsgCtx); //it is hcPolisition - 1, since this should be the last handler on which handlerMessage() is called.
          throw e;
        } catch (RuntimeException p) {
          LOC.debugT("invokeHandlerChainFault(): handler.handleFault() result " + p);
          invokeHandlerChainCloseIn(hChain, logicalMsgCtx); //same as above
          throw p;
        }
        if (res == false) {
          //invokeHandlerChainClose(hChain, logicalMsgCtx, hcPosition, false);
          return;
        }
      }
    } else {
      hcPosition = getBoundIndexOfLastCalledOutboundHandler(logicalMsgCtx);
      for (int i = hcPosition; i >= 0; i--) {
        handler = hChain.get(i);
        boolean res;
        try {
          if (handler instanceof LogicalHandler) {
            res = handler.handleFault(logicalMsgCtx);
          } else {
            res = handler.handleFault(soapMsgCtx);
          }
          LOC.debugT("invokeHandlerChainFault(): handler.handleFault() result " + res);
        } catch (ProtocolException e) {
          LOC.debugT("invokeHandlerChainFault(): handler.handleFault() result " + e);
          invokeHandlerChainCloseOut(hChain, logicalMsgCtx);
          throw e;
        } catch (RuntimeException p) {
          LOC.debugT("invokeHandlerChainFault(): handler.handleFault() result " + p);
          invokeHandlerChainCloseOut(hChain, logicalMsgCtx);
          throw p;
        }
        if (res == false) {
          //invokeHandlerChainClose(hChain, logicalMsgCtx, i + 1, true);
          return;
        }
      }
    }
  }
  
  public static void bindSOAPMessageContextInProviderContext(ProviderContextHelper pctx, SOAPMessageContextImpl soapCtx) {
    pctx.getDynamicContext().setProperty(SOAPMESSAGE_CONTEXT_PROPERTY, soapCtx);
  }

  public static void bindSOAPMessageContextInClientContext(ClientConfigurationContext cctx, SOAPMessageContextImpl soapCtx) {
    cctx.getDynamicContext().setProperty(SOAPMESSAGE_CONTEXT_PROPERTY, soapCtx);
  }

  public static SOAPMessageContextImpl getSOAPMessageContextFromProviderContext(ProviderContextHelper pctx) {
    return (SOAPMessageContextImpl) pctx.getDynamicContext().getProperty(SOAPMESSAGE_CONTEXT_PROPERTY);
  }

  public static SOAPMessageContextImpl getSOAPMessageContextFromClientContext(ClientConfigurationContext cctx) {
    return (SOAPMessageContextImpl) cctx.getDynamicContext().getProperty(SOAPMESSAGE_CONTEXT_PROPERTY);
  }

  public static void bindLogicalMessageContextInProviderContext(ProviderContextHelper pctx, LogicalMessageContextImpl logicalMsg) {
    pctx.getDynamicContext().setProperty(LOGICALMESSAGE_CONTEXT_PROPERTY, logicalMsg);
  }

  public static void bindLogicalMessageContextInClientContext(ClientConfigurationContext cctx, LogicalMessageContextImpl logicalMsg) {
    cctx.getDynamicContext().setProperty(LOGICALMESSAGE_CONTEXT_PROPERTY, logicalMsg);
  }

  public static LogicalMessageContextImpl getLogicalMessageContextFromProviderContext(ProviderContextHelper pctx) {
    return (LogicalMessageContextImpl) pctx.getDynamicContext().getProperty(LOGICALMESSAGE_CONTEXT_PROPERTY);
  }
  
  public static LogicalMessageContextImpl getLogicalMessageContextFromClientContext(ClientConfigurationContext cctx) {
    return (LogicalMessageContextImpl) cctx.getDynamicContext().getProperty(LOGICALMESSAGE_CONTEXT_PROPERTY);
  }
  
  public static void bindHandlerChainInProviderContext(ProviderContextHelper pctx, List<Handler> hChain) {
    pctx.getDynamicContext().setProperty(HANDLERCHAIN_INUSE, hChain);
  }
  
  public static List<Handler> getHandlerChainFromProviderContext(ProviderContextHelper pctx) {
    return (List<Handler>) pctx.getDynamicContext().getProperty(HANDLERCHAIN_INUSE);
  }

  public static List<Handler> getHandlerChainFromClientContext(ClientConfigurationContext cctx) {
    return (List<Handler>) cctx.getDynamicContext().getProperty(HANDLERCHAIN_INUSE);
  }

  private static void throwJAXWSHandlersException(Throwable t) throws JAXWSHandlersException {
    if (t instanceof JAXWSHandlersException) {
      throw (JAXWSHandlersException) t;
    } else {
      throw new JAXWSHandlersException(t);
    }
  }
  /**
   * Checks whether <code>saajMsg</code> is a fault msg.
   * @param saajMsg
   * @return
   * @throws Exception
   */
  public static boolean isSAAJMsgAFault(SOAPMessage saajMsg) throws ProtocolException {
    try {
      Iterator itr = saajMsg.getSOAPBody().getChildElements();
      while (itr.hasNext()) {
        Element e = (Element) itr.next();
        if ( ( com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAP11_NS.equals(e.getNamespaceURI()) ||
               com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAP12_NS.equals(e.getNamespaceURI()) )
               && com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.FAULTTAG_NAME.equals(e.getLocalName())) {
          return true;
        }
      }
    } catch (Exception e) {
      throw new ProtocolException(e);
    }
    return false;
  }
  
  private static void createAndBindSAAJFaultMsg(Exception pE, SOAPMessageContextImpl soapCtx) throws ProtocolException {
    try {
      if (pE instanceof SOAPFaultException) {
        SOAPFaultException sfE = (SOAPFaultException) pE;
        SOAPFault soapF = sfE.getFault();
        SOAPMessage faultMsg = MessageConvertor.createSOAP11SAAJMessage();
//        if (com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAP11_NS.equals(soapF.getNamespaceURI())) {
//          faultMsg = MessageConvertor.createSOAP11SAAJMessage();
//        } else if (com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAP12_NS.equals(soapF.getNamespaceURI())) {
//          faultMsg = MessageConvertor.createSOAP12SAAJMessage();
//        } else { 
//          throw new JAXWSHandlersException("Unknown soap element namespace '" + soapF.getNamespaceURI());
//        }
  
        SOAPBody soapBody = faultMsg.getSOAPBody();
        SOAPFault newFault = soapBody.addFault();
        newFault.setFaultCode(soapF.getFaultCodeAsQName());
        newFault.setFaultString(soapF.getFaultString());
        newFault.setFaultActor(soapF.getFaultActor());
        Detail newD = newFault.addDetail();
        Detail d = soapF.getDetail();
        if (d != null) {
          Element _d = (Element) newD.getOwnerDocument().importNode(d, true);
          NodeList nl = _d.getChildNodes();
          for (int i = 0; i < nl.getLength(); i++) {
            newD.appendChild(nl.item(i));
          }
        }
        
        soapCtx.setMessage(faultMsg);
      } else {
        SOAPMessage faultMsg = MessageConvertor.createSOAP11SAAJMessage();
        SOAPFault sFault = faultMsg.getSOAPBody().addFault();
        sFault.setFaultCode(new QName(com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAP11_NS, com.sap.engine.services.webservices.espbase.messaging.SOAPMessage.SOAP11_SERVER_F_CODE));
        String fStr = pE.getMessage();
        if (fStr == null) {
          fStr = pE.toString();
        }
        sFault.setFaultString(fStr);
        //no detail will be set for now..
        
        soapCtx.setMessage(faultMsg);
      }
    } catch (Exception e) {
      throw new ProtocolException(e);
    }
  }
  private static final String INDEX_PROPERTY = JAXWSHandlersEngine.class + ":inbound-index"; 
  private static final String INDEX_OUT_PROPERTY = JAXWSHandlersEngine.class + ":outbound-index";
  
  private static void bindIndexOfLastCalledOutboundHandler(int index, MessageContext ctx) {
    ctx.put(INDEX_OUT_PROPERTY, new Integer(index));
  }
  
  private static void bindIndexOfLastCalledInboundHandler(int index, MessageContext ctx) {
    ctx.put(INDEX_PROPERTY, new Integer(index));
  }
  
  private static int getBoundIndexOfLastCalledInboundHandler(MessageContext ctx) {
    Integer i = (Integer) ctx.get(INDEX_PROPERTY);
    return i.intValue();
  }
  
  private static int getBoundIndexOfLastCalledOutboundHandler(MessageContext ctx) {
    Integer i = (Integer) ctx.get(INDEX_OUT_PROPERTY);
    return i.intValue();
  }
  
  /**
   * Instantiates and initializes a MessageContext instance with all the required proeprties as specified in JAXWS section 9.4.2.
   * Creates SOAPMessageContext and LogicalMessageContext instances and binds them into <code>ctx</code>
   * @return
   */
  public static MessageContextImpl createAndBindJAXWSContexts(ProviderContextHelper ctx) {
    MessageContextImpl msgCtx = new MessageContextImpl();
    msgCtx.putWithScope(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true, MessageContext.Scope.APPLICATION);
    msgCtx.putWithScope(MessageContext.INBOUND_MESSAGE_ATTACHMENTS, new Hashtable<String, DataHandler>(), MessageContext.Scope.APPLICATION);
    msgCtx.putWithScope(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, new Hashtable<String, DataHandler>(), MessageContext.Scope.APPLICATION);
    
    HTTPTransport httpTransport = (HTTPTransport) ctx.getTransport();
    //dealing with the request artefacts
    HttpServletRequest req = httpTransport.getRequest();
    //process headers
    Map<String, List<String>> req_headers_map = new Hashtable();
    msgCtx.putWithScope(MessageContext.HTTP_REQUEST_HEADERS, req_headers_map, MessageContext.Scope.APPLICATION);
    Enumeration en = req.getHeaderNames();
    while (en.hasMoreElements()) {
      String h_n = (String) en.nextElement();
      Enumeration h_values = req.getHeaders(h_n);
      List<String> hvlist = new ArrayList();
      while (h_values.hasMoreElements()) {
        hvlist.add((String) h_values.nextElement());
      }
      req_headers_map.put(h_n, hvlist);
    }
    //request method
    msgCtx.putWithScope(MessageContext.HTTP_REQUEST_METHOD, req.getMethod(), MessageContext.Scope.APPLICATION);
    //query string
    msgCtx.putWithScope(MessageContext.QUERY_STRING, req.getQueryString(), MessageContext.Scope.APPLICATION);
    //pathInfo
    msgCtx.putWithScope(MessageContext.PATH_INFO, req.getPathInfo(), MessageContext.Scope.APPLICATION);
    
    //dealing with response artefacts
    //msgCtx.putWithScope(MessageContext.HTTP_RESPONSE_HEADERS, new Hashtable<String, List<String>>(), MessageContext.Scope.APPLICATION); //will be empty for now
    //msgCtx.putWithScope(MessageContext.HTTP_RESPONSE_CODE, 200, MessageContext.Scope.APPLICATION); //by default use 200 OK response code.
    
    //dealing with servlet endpoints
    Object servlet = ctx.getDynamicContext().getProperty(RuntimeProcessingEnvironment.CALLING_SERVLET_PROPERTY);
    if ((servlet != null) && (servlet instanceof ServiceEndpointWrapper) &&
        (((ServiceEndpointWrapper) servlet).getServiceEndpointInstance() != null)) { //this is a servlet based endpoint
      msgCtx.putWithScope(MessageContext.SERVLET_CONTEXT, ((Servlet) servlet).getServletConfig().getServletContext(), MessageContext.Scope.APPLICATION);
      msgCtx.putWithScope(MessageContext.SERVLET_REQUEST, req, MessageContext.Scope.APPLICATION);
      msgCtx.putWithScope(MessageContext.SERVLET_RESPONSE, httpTransport.getResponse(), MessageContext.Scope.APPLICATION);
    }
    
    SOAPMessageContextImpl soap_msgCtx = new SOAPMessageContextImpl(msgCtx);
    LogicalMessageContextImpl logic_msgCtx = new LogicalMessageContextImpl(msgCtx);
    //binding everything in ProviderContextHelper
    JAXWSHandlersEngine.bindLogicalMessageContextInProviderContext(ctx, logic_msgCtx);
    JAXWSHandlersEngine.bindSOAPMessageContextInProviderContext(ctx, soap_msgCtx);

    return msgCtx;
  }
  
  /**
   * Instantiates and initializes a MessageContext instance with all the
   * required proeprties as specified in JAXWS section 9.4.2. Creates
   * SOAPMessageContext and LogicalMessageContext instances and binds them into
   * <code>ctx</code>
   * 
   * @return
   */
  public static MessageContextImpl createAndBindJAXWSClientContexts(ClientConfigurationContext ctx) {
    MessageContextImpl msgCtx = new MessageContextImpl();
    msgCtx.putWithScope(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true, MessageContext.Scope.HANDLER);
    msgCtx.putWithScope(MessageContext.INBOUND_MESSAGE_ATTACHMENTS, new Hashtable<String, DataHandler>(), MessageContext.Scope.HANDLER);
    msgCtx.putWithScope(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, new Hashtable<String, DataHandler>(), MessageContext.Scope.HANDLER);
    // TODO: Add all required properties to the request context
    //msgCtx.putWithScope(MessageContext.HTTP_REQUEST_METHOD,"POST", MessageContext.Scope.HANDLER);
    //msgCtx.putWithScope(MessageContext.HTTP_REQUEST_HEADERS, new Hashtable<String, List<String>>(), MessageContext.Scope.HANDLER); // will be empty for now
    //msgCtx.putWithScope(MessageContext.HTTP_RESPONSE_HEADERS, new Hashtable<String, List<String>>(), MessageContext.Scope.HANDLER); // will be empty for now
    //msgCtx.putWithScope(MessageContext.HTTP_RESPONSE_CODE, new Integer(200), MessageContext.Scope.HANDLER); // by default use 200 OK response code.
    SOAPMessageContextImpl soap_msgCtx = new SOAPMessageContextImpl(msgCtx);
    LogicalMessageContextImpl logic_msgCtx = new LogicalMessageContextImpl(msgCtx);
    // binding everything in ClientConfigurationContext
    JAXWSHandlersEngine.bindLogicalMessageContextInClientContext(ctx, logic_msgCtx);
    JAXWSHandlersEngine.bindSOAPMessageContextInClientContext(ctx, soap_msgCtx);

    return msgCtx;
  }

}
