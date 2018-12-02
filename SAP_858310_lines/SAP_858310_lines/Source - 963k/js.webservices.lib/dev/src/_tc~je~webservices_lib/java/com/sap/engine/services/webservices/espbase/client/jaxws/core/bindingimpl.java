package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.soap.SOAPFaultException;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.lib.xml.util.QName;
import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.client.api.AttachmentHandlerFactory;
import com.sap.engine.services.webservices.espbase.client.api.impl.AttachmentHandlerNYImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientProtocolNotify;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientServiceContext;
import com.sap.engine.services.webservices.espbase.messaging.impl.MIMEMessageImpl;
import com.sap.engine.services.webservices.espbase.messaging.impl.MessageConvertor;
import com.sap.engine.services.webservices.espbase.messaging.impl.SOAPMessageImpl;
import com.sap.engine.services.webservices.espbase.server.additions.StreamEngine;
import com.sap.engine.services.webservices.jaxrpc.handlers.exceptions.JAXRPCHandlersException;
import com.sap.engine.services.webservices.jaxws.ctx.LogicalMessageContextImpl;
import com.sap.engine.services.webservices.jaxws.ctx.MessageContextImpl;
import com.sap.engine.services.webservices.jaxws.ctx.SOAPMessageContextImpl;
import com.sap.engine.services.webservices.jaxws.handlers.JAXWSHandlersEngine;
import com.sap.tc.logging.Location;

/**
 * JAX-WS Binding Interface implementation.
 * @author I024072
 *
 */
public class BindingImpl implements Binding, ConsumerProtocol,  Serializable {
  
  public static final String PROTOCOL_NAME  =  "JAXWSHandlersProtocol";
  private static Location LOC = Location.getLocation(BindingImpl.class);
  private Map<String,Object> responseContext;
  private Map<String,Object> requestContext;  

  public void setRequestContext(Map<String,Object> requestContext) {
    this.requestContext = requestContext;
  }
  
  public void setResponseContext(Map<String,Object> responseContext) {
    this.responseContext = responseContext;
  }
  
  List<Handler> handlers = null;
  
  public boolean isHandlerChainEmpty() {
    return(handlers == null ? true : handlers.isEmpty());
  }
  
  /**
   * Returns the protocol name(ID).
   */
  public String getProtocolName() {
    return PROTOCOL_NAME;
  }
  
  public List<Handler> getHandlerChain() {
    List<Handler> clone = null;
    
    if (handlers == null) {
      return clone;
    }
    
    clone = new ArrayList<Handler>();
    
    for (Handler h : handlers) {
      clone.add(h);
    }    
    
    return clone; // return a clone, not the same instance
  }

  public void setHandlerChain(List<Handler> chain) {
    handlers = sortHandlerChain(chain);
  }
  
  private static List<Handler> sortHandlerChain(List<Handler> handlers) {
    if (handlers == null || handlers.isEmpty()) {
      return new ArrayList<Handler>();
    }

    List<Handler> logicalHandlers = new ArrayList<Handler>();
    List<Handler> protocolHandlers = new ArrayList<Handler>();
  
    for (Handler handler : handlers) {
      if (handler instanceof LogicalHandler) {
        logicalHandlers.add(handler);
      } else if (handler instanceof SOAPHandler) { // this is SOAP protocol handler
        protocolHandlers.add(handler);
      }
    }
    
    logicalHandlers.addAll(protocolHandlers);
    return logicalHandlers;
  }

  /*
  public void serviceInit(ClientServiceContext serviceContext) throws ProtocolException {
  }

  public void portCreate(ClientConfigurationContext context) throws ProtocolException {
  }

  public void serviceDestroy(ClientServiceContext serviceContext) throws ProtocolException {
  }

  public void portDestroy(ClientConfigurationContext portContext) throws ProtocolException {
    try {
      JAXWSHandlersEngine.destroyHandlerChain(this.handlers);
    } catch (Exception x) {  
      // handle exc      
    }
  }*/

  public int handleRequest(ConfigurationContext context) throws ProtocolException, MessageException {
    if (handlers == null || handlers.isEmpty()) { // Do not invoke handlers
      return ConsumerProtocol.CONTINUE;
    }    
    ClientConfigurationContext ctx = (ClientConfigurationContext) context;
    // Creates JAX-WS message contexts
    MessageContextImpl msgCtx = JAXWSHandlersEngine.createAndBindJAXWSClientContexts(ctx);
    if (this.requestContext != null) {
      Iterator<String> iterator = this.requestContext.keySet().iterator();
      while (iterator.hasNext()) {
        String key = iterator.next();
        Object value = this.requestContext.get(key);
        msgCtx.putWithScope(key,value,Scope.HANDLER);
      }      
    }
    SOAPMessageContextImpl soapCtx = JAXWSHandlersEngine.getSOAPMessageContextFromClientContext(ctx);
    LogicalMessageContextImpl logicalCtx = JAXWSHandlersEngine.getLogicalMessageContextFromClientContext(ctx);
    logicalCtx.getWrappedContext().setCurrentMode(MessageContext.Scope.HANDLER);    
    try {
      SOAPMessageImpl msg = (SOAPMessageImpl) ctx.getMessage();      
      // TODO: check this. Adjustment to the conversion might be needed.
      SOAPMessage soapMsg = MessageConvertor.convertOutboundSAPMessageIntoSAAJ(msg);
      soapCtx.setMessage(soapMsg);      
      //preset outbound attachments if any
      Hashtable<String, DataHandler> outAttachments = (Hashtable<String, DataHandler>) logicalCtx.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
      if (msg instanceof MIMEMessageImpl) {
        AttachmentHandlerNYImpl attachmentHandler = new AttachmentHandlerNYImpl(ctx);
        Set atts = attachmentHandler.getOutboundAttachments();
        Iterator itr = atts.iterator();
        while (itr.hasNext()) {
          Attachment a = (Attachment) itr.next();
          outAttachments.put(a.getContentId(), (DataHandler) a.getContentObject());
        }
      }      
      
      boolean normalExec = true;
      synchronized(handlers) {//this is necessary because only one chain is available per configuration
        JAXWSHandlersEngine.invokeHandlerChainOutbound(handlers, logicalCtx, soapCtx, true, 0);
        // the invocation above could change the direction of the message to inbound (1.false returned; 2. SOAPFaultException; 3...)        
        if (!((Boolean) soapCtx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue()) { 
          normalExec = false;
        }                
      }
  
      if (! normalExec) { //not normal processing
        msg.clear();
        MessageConvertor.convertSAAJMessageIntoInputSAPMessage(soapCtx.getMessage(), msg);
        setFlagSkipHandleResponse(ctx);
        return ProviderProtocol.BACK;
      } else { //normal processing     
      //prepare response message
       String soapNS = msg.getSOAPVersionNS();
       msg.clear();
       msg.initWriteMode(soapNS);
       MessageConvertor.convertSAAJMessageIntoOutputSAPMessage(soapCtx.getMessage(), msg);
       return ConsumerProtocol.CONTINUE;
      }
    } catch (javax.xml.ws.ProtocolException x) {
      throw x;
    } catch (Exception e) { // better handling
      throw new ProtocolException(e);
    }    
  }

  public int handleResponse(ConfigurationContext context) throws ProtocolException {
    if (handlers == null || handlers.isEmpty()) {
      return ConsumerProtocol.CONTINUE;
    }
 
    ClientConfigurationContext ctx = (ClientConfigurationContext) context;  
    
    if (isFlagSkipHandleResponseSet(ctx)) {
      return ConsumerProtocol.CONTINUE;      
    } 
    
    LogicalMessageContextImpl logicalCtx = JAXWSHandlersEngine.getLogicalMessageContextFromClientContext(ctx);
    // TODO
    logicalCtx.getWrappedContext().putWithScope(MessageContext.HTTP_RESPONSE_CODE, new Integer(200), MessageContext.Scope.HANDLER); // by default use 200 OK response code.
    logicalCtx.getWrappedContext().putWithScope(MessageContext.HTTP_RESPONSE_HEADERS, new Hashtable<String, List<String>>(), MessageContext.Scope.HANDLER); // will be empty for now
    logicalCtx.getWrappedContext().setCurrentMode(MessageContext.Scope.HANDLER);
    SOAPMessageContextImpl soapCtx = (SOAPMessageContextImpl) JAXWSHandlersEngine.getSOAPMessageContextFromClientContext(ctx);
    SOAPMessageImpl msg = null;
    
    msg = (SOAPMessageImpl) ctx.getMessage();
    // Call getHeaders() on soap handlers
    HashSet<QName> qnames = new HashSet<QName>();
    for (int i=0; i<handlers.size(); i++) {
      Handler handler = handlers.get(i);
      if (handler instanceof SOAPHandler) {
        Set<QName> handlerQNames = ((SOAPHandler) handler).getHeaders();
        qnames.addAll(qnames);
      }
    }
//      StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), p_ctx);
//    } catch (Exception e) {
//      throw new JAXWSHandlersException(e);
//    }    
    
    //initialize SAAJ message
    SOAPMessage soapMsg;
    
    try {
      soapMsg = MessageConvertor.convertInboundSAPMessageIntoSAAJ(msg);      
    } catch (Exception e) { // better exc handling
//      throw new JAXWSHandlersException(e);
      throw new ProtocolException(e);
    }
    
//    MessageContextImpl msgCtx = JAXWSHandlersEngine.createAndBindJAXWSClientContexts(ctx);
    SOAPMessageContextImpl soap_msgCtx = JAXWSHandlersEngine.getSOAPMessageContextFromClientContext(ctx);
    soap_msgCtx.setMessage(soapMsg);
    //preset inbound attachments if any
    Hashtable<String, DataHandler> inAttachments = (Hashtable<String, DataHandler>) logicalCtx.get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS);
    if (msg instanceof MIMEMessageImpl) {
      AttachmentHandlerNYImpl attachmentHandler = new AttachmentHandlerNYImpl(ctx);
      Set atts = attachmentHandler.getInboundAttachments();
      Iterator itr = atts.iterator();
      while (itr.hasNext()) {
        Attachment a = (Attachment) itr.next();
        inAttachments.put(a.getContentId(), (DataHandler) a.getContentObject());
      }
    }      
    
    SOAPFault fObj = null;

    try {
      fObj = soapMsg.getSOAPBody().getFault();
    } catch (SOAPException sE) {
      throw new JAXRPCHandlersException(sE);
    }
    
    synchronized(handlers) {//synchronized because it is only one handlerchain for all port instances created for certain portQName (is it still valid?)
      if (fObj != null) { // The response message is soap fault
        logicalCtx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false); //this is needed for setting the context in the right direction
        JAXWSHandlersEngine.invokeHandlerChainFault(handlers, logicalCtx, soapCtx, 0);
        JAXWSHandlersEngine.invokeHandlerChainCloseOut(handlers, logicalCtx);        
      } else { //this is normal response
        JAXWSHandlersEngine.invokeHandlerChainInbound(handlers, logicalCtx, soapCtx, false, handlers.size() - 1);
      }        
    }
    
    //convert JAXM message into standard SOAPMessage, in order to be process by runtime
    msg = (SOAPMessageImpl) ctx.getMessage();
    //check for mandatory headers (because CTS test to pass)...
    try {
      MessageConvertor.convertSAAJMessageIntoInputSAPMessage(soapMsg, msg);
    } catch (Exception e) {
      throw new ProtocolException(e);
    }
    if (this.responseContext != null) { 
      Iterator<String> iterator = soap_msgCtx.keySet().iterator();
      while (iterator.hasNext()) {
        String key = iterator.next();
        Object value = soap_msgCtx.get(key);
        if (soap_msgCtx.getScope(key) == javax.xml.ws.handler.MessageContext.Scope.APPLICATION) {
          this.responseContext.put(key,value);
        }
      }
    }
    return ConsumerProtocol.CONTINUE;
  }

  public int handleFault(ConfigurationContext context) throws ProtocolException {
    return ConsumerProtocol.CONTINUE;
  }  
  
  private void setFlagSkipHandleResponse(ClientConfigurationContext ctx) {
    ctx.getDynamicContext().setProperty(BindingImpl.PROTOCOL_NAME + ":skip-handleResponse", Boolean.TRUE);
  }
  
  private boolean isFlagSkipHandleResponseSet(ClientConfigurationContext ctx) {
    Object o= ctx.getDynamicContext().getProperty(BindingImpl.PROTOCOL_NAME + ":skip-handleResponse");
    if (o != null) {
      Boolean b = (Boolean) o;
      if (b.booleanValue() == true) {
        return true;
      }
    }
    return false;
  }
  
  public String getBindingID() {
    throw new RuntimeException("Method not supported");
//    return null;
  }
  
}
