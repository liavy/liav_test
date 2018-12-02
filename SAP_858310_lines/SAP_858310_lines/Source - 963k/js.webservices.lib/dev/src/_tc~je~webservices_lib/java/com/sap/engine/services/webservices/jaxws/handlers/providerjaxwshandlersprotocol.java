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

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.ws.WebServicesDeploymentInterface;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.services.webservices.espbase.attachment.Attachment;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.messaging.impl.MIMEMessageImpl;
import com.sap.engine.services.webservices.espbase.messaging.impl.MessageConvertor;
import com.sap.engine.services.webservices.espbase.messaging.impl.SOAPMessageImpl;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.StaticConfigurationContext;
import com.sap.engine.services.webservices.espbase.server.additions.StreamEngine;
import com.sap.engine.services.webservices.espbase.server.api.ProviderAttachmentHandlerFactory;
import com.sap.engine.services.webservices.espbase.server.runtime.ProviderContextHelperImpl;
import com.sap.engine.services.webservices.jaxws.ctx.LogicalMessageContextImpl;
import com.sap.engine.services.webservices.jaxws.ctx.MessageContextImpl;
import com.sap.engine.services.webservices.jaxws.ctx.SOAPMessageContextImpl;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 25, 2006
 */
public class ProviderJAXWSHandlersProtocol implements ProviderProtocol, WebServicesDeploymentInterface {
  public static final String PROTOCOL_NAME  =  "JAXWSHandlersProtocol";
  //Registry into which List<Class> are kept, representing the handler's classes in the chain.
  private static Map<String, List<Class>> handlerChainClasses = new Hashtable();
  //Registry containing initialized and ready-for-use HC. Since for BD only one chains is maintained, it is accessed in synchronized block.
  private static Map<String, List<Handler>> handlerChainMap = new Hashtable(); 
  
  private static Location LOC = Location.getLocation(ProviderJAXWSHandlersProtocol.class);

  private LoadContext loadContext;
  
  public ProviderJAXWSHandlersProtocol(LoadContext loadCtx) {
    this.loadContext = loadCtx;
  }

//=================================== ProviderProtocol methods  
  public String getProtocolName() {
    return PROTOCOL_NAME;
  }

  public int handleRequest(ConfigurationContext context) throws ProtocolException, MessageException {
    ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) context;
    try {
      if (ctx.isEJBHandlersProcessMode()) {
        if (! ctx.isEJBHandlersProcessModeInvHandlers()) {
          createAndBindJAXWSContextsAndHandlersChain(ctx);           
          //convert JAXM message into standard SOAPMessage, in order to be process by runtime
          SOAPMessageImpl msg = null;
          msg = (SOAPMessageImpl) ctx.getMessage();
          //check for mandatory headers (because CTS test to pass)...
          StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), ctx);
          MessageConvertor.convertSAAJMessageIntoInputSAPMessage(JAXWSHandlersEngine.getSOAPMessageContextFromProviderContext(ctx).getWrappedContext().getInternalSOAPMessage(), msg);
          return ProviderProtocol.CONTINUE;
        } else {
          int retCode = this.invokeRequestHandlers(context);
          //make internal message ready for desrialization - possitioning reader on right element
          SOAPMessageImpl msg = (SOAPMessageImpl) ctx.getMessage();
          //check for mandatory headers (because CTS test to pass)...
          StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), ctx);
          XMLTokenReader reader = msg.getBodyReader();
          int code = reader.next();
          while (code != XMLTokenReader.STARTELEMENT) {
            code = reader.next();
            if (code == XMLTokenReader.EOF) {
              throw new IllegalStateException("EOF");
            }
          }
          return retCode;
        }
      } else { //this is for Sevlet case
        createAndBindJAXWSContextsAndHandlersChain(ctx);
        return this.invokeRequestHandlers(context);      
      }
    } catch (Exception e) {
      throw new ProtocolException(e);
    }
  }

  public int handleResponse(ConfigurationContext context) throws ProtocolException {
    ProviderContextHelper ctx = (ProviderContextHelper) context;    
    if (isFlagSkipHandleResponseSet(ctx)) {
      return CONTINUE;
    }

    List<Handler> hChain = JAXWSHandlersEngine.getHandlerChainFromProviderContext(ctx);
    SOAPMessageContextImpl soapCtx = JAXWSHandlersEngine.getSOAPMessageContextFromProviderContext(ctx);
    LogicalMessageContextImpl logicalCtx = JAXWSHandlersEngine.getLogicalMessageContextFromProviderContext(ctx);
    logicalCtx.getWrappedContext().setCurrentMode(MessageContext.Scope.HANDLER);
    try {
      SOAPMessageImpl msg = (SOAPMessageImpl) ctx.getMessage();
      //initialize SAAJ message only when the endpoint is not JAXWSProvider.
      //If it is JAXWSProvider, the response message is already preset in the context by the runtime 
      if (! ctx.getStaticContext().getInterfaceMapping().isJAXWSProviderInterface()) {
        SOAPMessage soapMsg = MessageConvertor.convertOutboundSAPMessageIntoSAAJ(msg);
//      preset msg to be used by handlers
        soapCtx.setMessage(soapMsg);
      }
      
      //preset outbound attachments if any
      Hashtable<String, DataHandler> outAttachments = (Hashtable<String, DataHandler>) logicalCtx.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
      if (msg instanceof MIMEMessageImpl) {
        Set atts = ProviderAttachmentHandlerFactory.getAttachmentHandler().getOutboundAttachments();
        Iterator itr = atts.iterator();
        while (itr.hasNext()) {
          Attachment a = (Attachment) itr.next();
          outAttachments.put(a.getContentId(), (DataHandler) a.getContentObject());
        }
      }
      
      synchronized(hChain) {//this is necessary because only one chain is available per configuration.
        if (ctx.getMessageSemantic() == ProviderContextHelper.FAULT_MSG) {
          logicalCtx.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true); //this is needed for setting the context in the right direction
          JAXWSHandlersEngine.invokeHandlerChainFault(hChain, logicalCtx, soapCtx, 0);
          JAXWSHandlersEngine.invokeHandlerChainCloseIn(hChain, logicalCtx);
        } else { //this is normal response
          JAXWSHandlersEngine.invokeHandlerChainOutbound(hChain, logicalCtx, soapCtx, false, 0);
        }
      }
      //prepare response message
      String soapNS = msg.getSOAPVersionNS();
      msg.clear();
      msg.initWriteMode(soapNS);
      MessageConvertor.convertSAAJMessageIntoOutputSAPMessage(soapCtx.getMessage(), msg);
      return ProviderProtocol.CONTINUE;
    } catch (Exception e) {
      throw new ProtocolException(e);
    } 
  }

  public int handleFault(ConfigurationContext context) throws ProtocolException {
    return 0;
  }
  
//====================================== WebServicesDeploymentInterface methods
  public void onCommitDeploy(String applicationName) throws WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onCommitRuntimeChanges(String applicationName) throws WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onCommitStart(String applicationName) throws WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onDeploy(String applicationName, String serviceName, ConfigurationContext[] bindingDataStaticConfigurationContexts) throws WSDeploymentException, WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onPostDeploy(String applicationName, String serviceName, ConfigurationContext[] bindingDataStaticConfigurationContext) throws WSDeploymentException, WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onRemove(String applicationName) throws WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onRollbackDeploy(String applicationName) throws WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onRollbackRuntimeChanges(String applicationName) throws WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onRollbackStart(String applicationName) throws WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onRuntimeChanges(String applicationName, String serviceName, ConfigurationContext[] bindingDataStaticConfigurationContexts, int updateMode) throws WSDeploymentException, WSWarningException {
    // TODO Auto-generated method stub
    
  }

  public void onStart(String applicationName, String serviceName, ConfigurationContext[] bindingDataConfigurationContext) throws WSDeploymentException, WSWarningException {
    for (int i = 0; i < bindingDataConfigurationContext.length; i++) {
      try {
        StaticConfigurationContext s_ctx = (StaticConfigurationContext) bindingDataConfigurationContext[i];
        String cfgKey = getConfigurationKey(s_ctx.getTargetApplicationName(), s_ctx.getRTConfiguration().getName());
        LOC.debugT("Start configuration: " + cfgKey);

        BindingData bD = s_ctx.getRTConfiguration();
        PropertyType prop = bD.getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS, BuiltInConfigurationConstants.JAXWS_HANDLERS_CONFIG_PROPERTY);
        if (prop == null) {
          LOC.debugT("No JAXWS handlers for configuration: " + cfgKey);
          continue; //there is nothing to be loaded
        }
        //take apploader
        String appName = s_ctx.getTargetApplicationName();
        ClassLoader loader = loadContext.getClassLoader(appName);
        //load conf data
        List h_chain_classes = JAXWSHandlersEngine.getHandlersClassesFromHandlerChains(prop.get_value(), loader);
        //register
        handlerChainClasses.put(cfgKey, h_chain_classes);
      } catch (JAXWSHandlersException e) {
        throw new WSDeploymentException(e);
      }
    }
  }

  public void onStop(String applicationName) throws WSWarningException {
    final String mName = "public void onStop(String applicationName) throws WSWarningException";
    String appHashCode = Integer.toString(applicationName.hashCode());

    LOC.debugT(mName + " stop application '" + applicationName + "', hashCode '" + appHashCode);
    //remove configuration from handlerChainClasses and handlerChainMap.    
    int ind;
    String cfgAppHash;
    synchronized (handlerChainClasses) {
      Iterator<String> itr = handlerChainClasses.keySet().iterator();
      while (itr.hasNext()) {
        String tmpKey = itr.next();
        ind = tmpKey.indexOf("[");
        cfgAppHash = tmpKey.substring(0, ind);  
        LOC.debugT(mName + " stop application '" + applicationName + "', configuation-app-hash '" + cfgAppHash);
        if (cfgAppHash.equals(appHashCode)) { //checks whether tmpKey is for a configuration from the specific app. See getConfigurationKey() method down for how cfgKeys are generated.
          LOC.debugT(mName + " stop application '" + applicationName + "', stop configuation '" + tmpKey);
          itr.remove(); //this should reflect the Map
          List<Handler> chain = handlerChainMap.remove(tmpKey); //remove the chain, if any from the map.
          if (chain != null) {
            try {
              LOC.debugT(mName + " destoying handler chain of application '" + applicationName + "', configuation '" + tmpKey);
              JAXWSHandlersEngine.destroyHandlerChain(chain);
            } catch (Exception e) {
              throw new WSWarningException(e);
            }
          }
        }      
      }
    }
  }

//================================== Private methods
  /**
   * Generates unique key, which is used to resolve requested handlers configuration.
   * @param appName application name
   * @param bdName the value returned by ctx.getStaticContext().getRTConfiguration().getName().
   */
  private static String getConfigurationKey(String appName, String bdName) {
    return appName.hashCode() + "[" + appName + "_" + bdName + "]";
  }
  /**
   * Creates and binds in the context the following items:
   *  - Logical and SOAP message contexts with SAAJ message inside
   *  - handler chain. 
   */
  private void createAndBindJAXWSContextsAndHandlersChain(ConfigurationContext context) throws JAXWSHandlersException {
    ProviderContextHelper p_ctx = (ProviderContextHelper) context;
    String cfgKey = getConfigurationKey(p_ctx.getStaticContext().getTargetApplicationName(), p_ctx.getStaticContext().getRTConfiguration().getName());
    
    List<Handler> hChain = handlerChainMap.get(cfgKey);
    if (hChain == null) { //this is the first request to this endpoint and no chain has been initialized. Initialize the chain.
      List<Class> hc_cls = handlerChainClasses.get(cfgKey);
      if (hc_cls == null) {
        throw new JAXWSHandlersException("No handler chain configuration found for configuration with id '" + cfgKey + "'");
      }
      hChain = JAXWSHandlersEngine.initializedHandlerChain(hc_cls);
      handlerChainMap.put(cfgKey, hChain);
    }
    
    SOAPMessageImpl msg = null;
    try {
      msg = (SOAPMessageImpl) p_ctx.getMessage();
      //check for mandatory headers (because CTS test to pass)...
      StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), p_ctx);
    } catch (Exception e) {
      throw new JAXWSHandlersException(e);
    }    
    
    //initialize SAAJ message
    SOAPMessage soapMsg;
    try {
      soapMsg = MessageConvertor.convertInboundSAPMessageIntoSAAJ(msg);      
    } catch (Exception e) {
      throw new JAXWSHandlersException(e);
    }
    
    MessageContextImpl msgCtx = JAXWSHandlersEngine.createAndBindJAXWSContexts(p_ctx);
    SOAPMessageContextImpl soap_msgCtx = JAXWSHandlersEngine.getSOAPMessageContextFromProviderContext(p_ctx);
    soap_msgCtx.setMessage(soapMsg);
    Hashtable<String, DataHandler> inAttachments = (Hashtable<String, DataHandler>) msgCtx.get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS);
    if (msg instanceof MIMEMessageImpl) {
      Set atts = ProviderAttachmentHandlerFactory.getAttachmentHandler().getInboundAttachments();
      Iterator itr = atts.iterator();
      while (itr.hasNext()) {
        Attachment a = (Attachment) itr.next();
        inAttachments.put(a.getContentId(), (DataHandler) a.getContentObject());
      }
    }
    //binding everything in ProviderContextHelper
    JAXWSHandlersEngine.bindHandlerChainInProviderContext(p_ctx, hChain);
  }
  
  /**
   * Invokes  handler chain.
   * @return protocol response code.
   */
  private int invokeRequestHandlers(ConfigurationContext context) throws ProtocolException {
    ProviderContextHelper ctx = (ProviderContextHelper) context;
    List<Handler> hChain = JAXWSHandlersEngine.getHandlerChainFromProviderContext(ctx);
    SOAPMessageContextImpl soapCtx = JAXWSHandlersEngine.getSOAPMessageContextFromProviderContext(ctx);
    LogicalMessageContextImpl logicalCtx = JAXWSHandlersEngine.getLogicalMessageContextFromProviderContext(ctx);
    
    logicalCtx.getWrappedContext().setCurrentMode(MessageContext.Scope.HANDLER);
    try {
      SOAPMessageImpl msg = (SOAPMessageImpl) ctx.getMessage();
      //check for mandatory headers (because CTS test to pass)...
      StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), ctx);
      
      boolean normalExec = true;
      synchronized(hChain) {//this is necessary because only one chain is available per configuration.
         JAXWSHandlersEngine.invokeHandlerChainInbound(hChain, logicalCtx, soapCtx, true, hChain.size() - 1);
         //the invocation above could change the direction of the message to outbound. For now we assume that in this case fault message is generated.
         if (((Boolean) soapCtx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue()) { 
           normalExec = false;
         }
      }
      
      if (! normalExec) { //not normal processing
        String soapNS = msg.getSOAPVersionNS();
        msg.clear();
        msg.initWriteMode(soapNS);
        SOAPMessage message = soapCtx.getMessage();
        boolean isFault = JAXWSHandlersEngine.isSAAJMsgAFault(message); 
        MessageConvertor.convertSAAJMessageIntoOutputSAPMessage(message, msg);
        if (isFault) {
          ctx.setMessageSemantic(ProviderContextHelper.FAULT_MSG); //mark message as fault msg
        } else {
          ctx.setMessageSemantic(ProviderContextHelper.NORMAL_RESPONSE_MSG); //mark message as normal msg
        }
        msg.commitWrite();
        setFlagSkipHandleResponse(ctx);
        return ProviderProtocol.BACK;
      } else { //normal processing
        SOAPMessage soapMsg = soapCtx.getMessage();
        if (LOC.beDebug()) {
          ByteArrayOutputStream buf = new ByteArrayOutputStream();
          soapMsg.writeTo(buf);
          buf.flush();
          LOC.debugT("handleRequest(): message to be checked for consistency..." + buf.toString()); //$JL-I18N$
        }
        //convert data into standard SOAPMessage
        MessageConvertor.convertSAAJMessageIntoInputSAPMessage(soapCtx.getMessage(), msg);        
        return ProviderProtocol.CONTINUE;
      }
    } catch (Exception e) {
      throw new ProtocolException(e);
    }
  }
  
  private void setFlagSkipHandleResponse(ProviderContextHelper ctx) {
    ctx.getDynamicContext().setProperty(ProviderJAXWSHandlersProtocol.PROTOCOL_NAME + ":skip-handleResponse", Boolean.TRUE);
  }
  
  private boolean isFlagSkipHandleResponseSet(ProviderContextHelper ctx) {
    Object o= ctx.getDynamicContext().getProperty(ProviderJAXWSHandlersProtocol.PROTOCOL_NAME + ":skip-handleResponse");
    if (o != null) {
      Boolean b = (Boolean) o;
      if (b.booleanValue() == true) {
        return true;
      }
    }
    return false;
  }
}
