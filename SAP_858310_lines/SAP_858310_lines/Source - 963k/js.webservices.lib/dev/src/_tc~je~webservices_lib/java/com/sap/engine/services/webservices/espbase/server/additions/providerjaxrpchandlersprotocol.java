/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.server.additions;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.deploy.WSWarningException;
import com.sap.engine.interfaces.webservices.server.deploy.ws.WebServicesDeploymentInterface;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.messaging.impl.SOAPMessageImpl;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.StaticConfigurationContext;
import com.sap.engine.services.webservices.espbase.server.runtime.ProviderContextHelperImpl;
import com.sap.engine.services.webservices.jaxrpc.handlers.HandlerChainImpl;
import com.sap.engine.services.webservices.jaxrpc.handlers.HandlerInfoImpl;
import com.sap.engine.services.webservices.jaxrpc.handlers.HandlerRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.handlers.JAXRPCHandlersEngine;
import com.sap.engine.services.webservices.jaxrpc.handlers.SOAPMessageContextImpl;
import com.sap.engine.services.webservices.jaxrpc.handlers.exceptions.JAXRPCHandlersException;
import com.sap.engine.services.webservices.jaxrpc.handlers.exceptions.ResourceAccessor;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * ProviderProtocol implementation responsible for providing JAX-RPC handlers support.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-3-12
 */
public class ProviderJAXRPCHandlersProtocol implements ProviderProtocol, WebServicesDeploymentInterface {
  public static final String PROTOCOL_NAME  =  "JAXRPCHandlersProtocol";
  
  private static Map handlerChainMap = new Hashtable(); //key QName (cfg key), value initialized HandlerChainImpl. //TODO: Only one HandlerChain instance is used sychroniously to server all request to a configuration.
  private static HandlerRegistryImpl hInfoRegistry = new HandlerRegistryImpl(); //contains handlerInfo chains
  
  private static final String HANDLERCHAIN_PROPERTY  =  "jax-rpc-handler-chain"; //here HandlerChain object is mapped.

  //keys for message entities
  private static final String OPERATION_ELEM_QNAME  =  "operation-qname";
  private static final String PART_ELEM_QNAME_PREF  =  "part-qname-prefix";
  private static final String PART_NAME_LIST  =  "part-name-list";
  
  private static final Location LOC = Location.getLocation(ProviderJAXRPCHandlersProtocol.class);
  
  private LoadContext loadContext;
  public ProviderJAXRPCHandlersProtocol(LoadContext loadCtx) {
    this.loadContext = loadCtx;
  }
  public String getProtocolName() {
    return PROTOCOL_NAME;
  }

  public int handleRequest(ConfigurationContext context) throws ProtocolException, MessageException {
    ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) context;
    if (ctx.isEJBHandlersProcessMode()) {
      if (! ctx.isEJBHandlersProcessModeInvHandlers()) {
        createAndBindRequestSOAPMessage(ctx);           
        //convert JAXM message into standard SOAPMessage, in order to be process by runtime
        SOAPMessageImpl msg = null;
        try {
          msg = (SOAPMessageImpl) ctx.getMessage();
          //check for mandatory headers (because CTS test to pass)...
          StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), ctx);
        } catch (Exception e) {
          throw new JAXRPCHandlersException(e);
        }    
        JAXRPCHandlersEngine.convertInboundJAXMessageDataIntoSOAPMessage(JAXRPCHandlersEngine.getSOAPMessageContext(ctx.getDynamicContext()).getMessage(), msg);
        return ProviderProtocol.CONTINUE;
      } else {
        int retCode = this.invokeRequestHandlers(context);
        //make internal message ready for desrialization - possitioning reader on right element
        SOAPMessageImpl msg = null;
        try {
          msg = (SOAPMessageImpl) ctx.getMessage();
          //check for mandatory headers (because CTS test to pass)...
          StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), ctx);
        } catch (Exception e) {
          throw new JAXRPCHandlersException(e);
        }    
        XMLTokenReader reader = msg.getBodyReader();
        try {
          int code = reader.next();
          while (code != XMLTokenReader.STARTELEMENT) {
            code = reader.next();
            if (code == XMLTokenReader.EOF) {
              throw new IllegalStateException("EOF");
            }
          }
        } catch (Exception e) {
          throw new JAXRPCHandlersException(e);
        }
        return retCode;
      }
    } else {
      createAndBindRequestSOAPMessage(ctx);
      return this.invokeRequestHandlers(context);      
    }
//    QName cfgKey = getConfigurationKeyQName(ctx.getStaticContext().getTargetApplicationName(), ctx.getStaticContext().getRTConfiguration().getName());
//    
//    HandlerChainImpl hChain = (HandlerChainImpl) handlerChainMap.get(cfgKey);
//    if (hChain == null) {
//      throw new JAXRPCHandlersException(JAXRPCHandlersException.MISSING_CONFIGURAION, new Object[]{cfgKey});   
//    }
//    
//    SOAPMessageImpl msg = null;
//    try {
//      msg = (SOAPMessageImpl) ctx.getMessage();
//      //check for mandatory headers (because CTS test to pass)...
//      StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), ctx);
//    } catch (Exception e) {
//      throw new JAXRPCHandlersException(e);
//    }    
//    
//    //initialize JAXM message
//    SOAPMessage soapMsg = JAXRPCHandlersEngine.createInboundJAXMSOAPMessage(msg);      
//    
//    SOAPMessageContextImpl soapCtx = new SOAPMessageContextImpl();
//    soapCtx.setRoles(hChain.getRoles());
//    soapCtx.setMessage(soapMsg);
//
//    //set SOAPMessageContext on dedicated place, in order to be used in handleResponse();
//    ctx.getDynamicContext().setProperty(HANDLERCHAIN_PROPERTY, hChain);
//    JAXRPCHandlersEngine.bindSOAPMessageContext(ctx.getDynamicContext(), soapCtx); //bind SOAPMessageContext into dynamic context
//    
//    //creates and binds SOAPMessage into context
//    createAndBindRequestSOAPMessage(context);
//    //extract set SOAPMessageContext on dedicated place, in order to be used in handleResponse();
//    HandlerChainImpl hChain = (HandlerChainImpl) ctx.getDynamicContext().getProperty(HANDLERCHAIN_PROPERTY);
//    SOAPMessageContextImpl soapCtx = (SOAPMessageContextImpl) JAXRPCHandlersEngine.getSOAPMessageContext(ctx);
//    SOAPMessage soapMsg = soapCtx.getMessage();
//    SOAPMessageImpl msg = null;
//    try {
//      msg = (SOAPMessageImpl) ctx.getMessage();
//      //check for mandatory headers (because CTS test to pass)...
////      StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), ctx);
//    } catch (Exception e) {
//      throw new JAXRPCHandlersException(e);
//    }    
//    
//    //save relevant message data
//    Map map = new HashMap();
//    try {
//      if (LOC.beDebug()) {
//        ByteArrayOutputStream buf = new ByteArrayOutputStream();
//        soapMsg.writeTo(buf);
//        buf.flush();
//        LOC.debugT("handleRequest(): base message..." + buf.toString());
//      }
//      mapKeyMessageStruture(soapMsg, map);
//      if (LOC.beDebug()) {
//        LOC.debugT("handleRequest(): map: " + map);
//      }
//    } catch (Exception e) {
//      throw new JAXRPCHandlersException(e);
//    }
//    
//    boolean normalExec = true;
//    synchronized(hChain) {//this is necessary because only one chain is available per configuration.
//      hChain.clearState();
//      boolean res = false;
//      try {
//        res = hChain.handleRequest(soapCtx);
//      } catch (SOAPFaultException sfEx) {
//        normalExec = false;
//        //invoke handleFault (according to the spec). 
//        hChain.handleFault(soapCtx);
//      }
//      if (res == false) {
//        normalExec = false;
//        //invoke handleResponse (according to the spec). 
//        hChain.handleResponse(soapCtx);         
//      }
//    }
//    
//    if (! normalExec) { //not normal processing
//      msg.clear();
//      msg.initWriteMode();
//      JAXRPCHandlersEngine.serializeJAXMessageIntoSOAPMessage(soapCtx.getMessage(), msg);
//      ctx.setMessageSemantic(ProviderContextHelper.FAULT_MSG); //mark message as fault msg
//      return ProviderProtocol.BACK;
//    } else { //normal processing
//      //check for changes in message data
//      String s = null;
//      try {
//        soapMsg = soapCtx.getMessage();
//        if (LOC.beDebug()) {
//          ByteArrayOutputStream buf = new ByteArrayOutputStream();
//          soapMsg.writeTo(buf);
//          buf.flush();
//          LOC.debugT("handleRequest(): message to be checked for consistency..." + buf.toString());
//        }
//        s = isMessageStructureChanged(soapMsg, map);
//        LOC.debugT("handleRequest(), result after check " + s);
//      } catch (Exception e) {
//        throw new JAXRPCHandlersException(e);
//      }
//      if (s != null) {
//        createFaultMessage("Server", s, msg, ctx);
//        throw new MessageException(); //in order to be invoked handleFault() of the protocol
//      }
//      //convert data into standard SOAPMessage
//      JAXRPCHandlersEngine.convertInboundJAXMessageDataIntoSOAPMessage(soapCtx.getMessage(), msg);        
//      return ProviderProtocol.CONTINUE;
//    }    
  }

  public int handleResponse(ConfigurationContext context) throws ProtocolException {
    ProviderContextHelper ctx = (ProviderContextHelper) context;    
    SOAPMessageImpl msg = null;
    try {
      msg = (SOAPMessageImpl) ctx.getMessage();
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }
    HandlerChainImpl hChain = (HandlerChainImpl) ctx.getDynamicContext().getProperty(HANDLERCHAIN_PROPERTY);
    SOAPMessageContextImpl soapCtx = (SOAPMessageContextImpl) JAXRPCHandlersEngine.getSOAPMessageContext(ctx.getDynamicContext());
    
    //initialize JAXM message
    SOAPMessage soapMsg = JAXRPCHandlersEngine.createOutboundJAXMSOAPMessage(msg);
    //preset msg to be used by handlers
    soapCtx.setMessage(soapMsg);
    
    synchronized(hChain) {//this is necessary because only one chain is available per configuration.      
      hChain.handleResponse(soapCtx); //return value of this method has no meaning to the runtime 
    }
    //prepare response message 
    msg.clear();
    msg.initWriteMode(SOAPMessageImpl.SOAP11_NS);//JAX-RPC is supposed to support only SOAP1.1.
    JAXRPCHandlersEngine.serializeJAXMessageIntoSOAPMessage(soapCtx.getMessage(), msg);
    return ProviderProtocol.CONTINUE;
  }

  public int handleFault(ConfigurationContext context) throws ProtocolException {
    return ProviderProtocol.CONTINUE;
  }

  public void onDeploy(String applicationName, String serviceName, ConfigurationContext[] bindingDataStaticConfigurationContexts) throws WSDeploymentException {            
  
  }
  
  public void onPostDeploy(String applicationName, String serviceName, ConfigurationContext[] bindingDataStaticConfigurationContext) throws WSDeploymentException {        
  
  }

  public void onCommitDeploy(String applicationName) {     
  
  }

  public void onRollbackDeploy(String applicationName) {
    
  }  
  
  public void onRemove(String applicationName) {
    
  }
 
  public void onCommitStart(String applicationName) {
  
  }

  public void onRollbackStart(String applicationName) {
  
  }  
  
  public void onRuntimeChanges(String arg0, String arg1, ConfigurationContext[] arg2, int arg3) throws WSDeploymentException, WSWarningException {

  }
  
  public void onCommitRuntimeChanges(String arg0) throws WSWarningException {  

  }

  public void onRollbackRuntimeChanges(String arg0) throws WSWarningException {

  }
  
  public void onStart(String applicationName, String serviceName, ConfigurationContext[] bindingDataConfigurationContext) throws WSDeploymentException, WSWarningException {  
    for (int i = 0; i < bindingDataConfigurationContext.length; i++) {
      try {
        startConfiguration((StaticConfigurationContext) bindingDataConfigurationContext[i]);        
      } catch (JAXRPCHandlersException e) {
        throw new WSDeploymentException(e);
      }
    }
  }
  
  public void onStop(String applicationName) throws WSWarningException {
    final String mName = "public void onStop(String applicationName) throws WSWarningException";
    String appHashCode = Integer.toString(applicationName.hashCode());

    LOC.debugT(mName + " stop application '" + applicationName + "', hashCode '" + appHashCode);
       
    Map mapLock = hInfoRegistry.getInternalTable();
    synchronized (mapLock) { //lock the map, because ConcurrentModificationException occurs in the code below.
      Set keys = mapLock.keySet();
      int ind;
      QName tmpKey;
      String cfgAppHash;
      Iterator itr = keys.iterator();
      while (itr.hasNext()) {
        tmpKey = (QName) itr.next();
        ind = tmpKey.getLocalPart().indexOf("[");
        cfgAppHash = tmpKey.getLocalPart().substring(0, ind);  
        LOC.debugT(mName + " stop application '" + applicationName + "', configuation-app-hash '" + cfgAppHash);
        if (cfgAppHash.equals(appHashCode)) { //checks whether tmpKey is for a configuration from the specific app. See getConfigurationKey() method down for how cfgKeys are generated.
          LOC.debugT(mName + " stop application '" + applicationName + "', stop configuation '" + tmpKey.getLocalPart());
          stopConfiguration(tmpKey);        
        }
      }
    }
  }
  
  
  /**
   * Builds request jaxm soap message. 
   */
  private void createAndBindRequestSOAPMessage(ConfigurationContext context) throws JAXRPCHandlersException {
    ProviderContextHelper ctx = (ProviderContextHelper) context;
    QName cfgKey = getConfigurationKeyQName(ctx.getStaticContext().getTargetApplicationName(), ctx.getStaticContext().getRTConfiguration().getName());
    
    HandlerChainImpl hChain = (HandlerChainImpl) handlerChainMap.get(cfgKey);
    if (hChain == null) {
      throw new JAXRPCHandlersException(JAXRPCHandlersException.MISSING_CONFIGURAION, new Object[]{cfgKey});   
    }
    
    SOAPMessageImpl msg = null;
    try {
      msg = (SOAPMessageImpl) ctx.getMessage();
      //check for mandatory headers (because CTS test to pass)...
      StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), ctx);
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }    
    
    //initialize JAXM message
    SOAPMessage soapMsg = JAXRPCHandlersEngine.createInboundJAXMSOAPMessage(msg);      
    
    SOAPMessageContextImpl soapCtx = new SOAPMessageContextImpl();
    soapCtx.setRoles(hChain.getRoles());
    soapCtx.setMessage(soapMsg);

    //set SOAPMessageContext on dedicated place, in order to be used in handleResponse();
    ctx.getDynamicContext().setProperty(HANDLERCHAIN_PROPERTY, hChain);
    JAXRPCHandlersEngine.bindSOAPMessageContext(ctx.getDynamicContext(), soapCtx); //bind SOAPMessageContext into dynamic context
  }
  /**
   * Invokes  handler chain.
   * @return protocol response code.
   */
  private int invokeRequestHandlers(ConfigurationContext context) throws JAXRPCHandlersException, MessageException {
    ProviderContextHelper ctx = (ProviderContextHelper) context;
    HandlerChainImpl hChain = (HandlerChainImpl) ctx.getDynamicContext().getProperty(HANDLERCHAIN_PROPERTY);
    SOAPMessageContextImpl soapCtx = (SOAPMessageContextImpl) JAXRPCHandlersEngine.getSOAPMessageContext(ctx.getDynamicContext());
    SOAPMessage soapMsg = soapCtx.getMessage();
    SOAPMessageImpl msg = null;
    try {
      msg = (SOAPMessageImpl) ctx.getMessage();
      //check for mandatory headers (because CTS test to pass)...
//      StreamEngine.checkForMandatoryHeaders(msg.getSOAPHeaders(), ctx);
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }    
    
    //save relevant message data
    Map map = new HashMap();
    try {
      if (LOC.beDebug()) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        soapMsg.writeTo(buf);
        buf.flush();
        LOC.debugT("handleRequest(): base message..." + buf.toString()); //$JL-I18N$
      }
      mapKeyMessageStructure(soapMsg, map);
      if (LOC.beDebug()) {
        LOC.debugT("handleRequest(): map: " + map);
      }
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }
    
    boolean normalExec = true;
    synchronized(hChain) {//this is necessary because only one chain is available per configuration.
      hChain.clearState();
      boolean res = false;
      try {
        res = hChain.handleRequest(soapCtx);
      } catch (SOAPFaultException sfEx) {
        normalExec = false;
        //invoke handleFault (according to the spec). 
        hChain.handleFault(soapCtx);
      }
      if (res == false) {
        normalExec = false;
        //invoke handleResponse (according to the spec). 
        hChain.handleResponse(soapCtx);         
      }
    }
    
    if (! normalExec) { //not normal processing
      msg.clear();
      msg.initWriteMode(SOAPMessageImpl.SOAP11_NS);//JAX-RPC is supposed to support only SOAP1.1.;
      JAXRPCHandlersEngine.serializeJAXMessageIntoSOAPMessage(soapCtx.getMessage(), msg);
      ctx.setMessageSemantic(ProviderContextHelper.FAULT_MSG); //mark message as fault msg
      return ProviderProtocol.BACK;
    } else { //normal processing
      //check for changes in message data
      String s = null;
      try {
        soapMsg = soapCtx.getMessage();
        if (LOC.beDebug()) {
          ByteArrayOutputStream buf = new ByteArrayOutputStream();
          soapMsg.writeTo(buf);
          buf.flush();
          LOC.debugT("handleRequest(): message to be checked for consistency..." + buf.toString());
        }
        s = isMessageStructureChanged(soapMsg, map);
        LOC.debugT("handleRequest(), result after check " + s);
      } catch (Exception e) {
        throw new JAXRPCHandlersException(e);
      }
      if (s != null) {
        createFaultMessage("Server", s, msg, ctx);
        throw new MessageException(); //in order to be invoked handleFault() of the protocol
      }
      //convert data into standard SOAPMessage
      JAXRPCHandlersEngine.convertInboundJAXMessageDataIntoSOAPMessage(soapCtx.getMessage(), msg);        
      return ProviderProtocol.CONTINUE;
    }        
  }
  /**
   * Stops configuration with specific <code>cfgKey</code>. 
   */
  private void stopConfiguration(QName cfgKey) {    
    hInfoRegistry.removeHandlerChain(cfgKey);
    HandlerChainImpl hChain = (HandlerChainImpl) handlerChainMap.remove(cfgKey);  
//    if (hChain == null) {
//      throw new JAXRPCHandlersException(JAXRPCHandlersException.MISSING_CONFIGURAION, new Object[]{cfgKey});   
//    }
    //destroy handler chain
    hChain.destroy();
  }
  
  /**
   * Reads configuration data, loads handers classes and initializes them.
   * This is the method which should be called on configuration start.
   */
  private void startConfiguration(StaticConfigurationContext context) throws JAXRPCHandlersException {
    String cfgKey = getConfigurationKey(context.getTargetApplicationName(), context.getRTConfiguration().getName());
    LOC.debugT("Start configuration: " + cfgKey);

    BindingData bD = context.getRTConfiguration();
    PropertyType prop = bD.getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS, BuiltInConfigurationConstants.JAXRPC_HANDLERS_CONFIG_PROPERTY);
    if (prop == null) {
      LOC.debugT("No JAX-RPC handlers for configuration: " + cfgKey);
      return; //there is nothing to be loaded
//      String pname = "{" + BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS + "}" + BuiltInConfigurationConstants.JAXRPC_HANDLERS_CONFIG_PROPERTY; 
//      throw new JAXRPCHandlersException(JAXRPCHandlersException.MISSING_CFG_PROPERTY, new Object[]{pname});
    }
    //load conf data
    List hInfos = JAXRPCHandlersEngine.loadHanderInfoConfigurations(prop.get_value());
    //take apploader
    String appName = context.getTargetApplicationName();
    ClassLoader loader = loadContext.getClassLoader(appName);
    //load handlers' classes
    HandlerInfoImpl hInf;
    String hCName;
    Class hClass;
    for (int i = 0; i <  hInfos.size(); i++) {
      hInf = (HandlerInfoImpl) hInfos.get(i);
      hCName = hInf.getHandlerClassName();
      try {
        hClass = loader.loadClass(hCName); 
      } catch (ClassNotFoundException cnfE) {
        throw new JAXRPCHandlersException(JAXRPCHandlersException.UNABLE_TO_LOAD_CLASS, new Object[]{hCName, appName, loader}, cnfE);
      }
      hInf.setHandlerClass(hClass);
    }
    //maps the List object into registry under specific qname. Make the list unmodifiable.
    QName htKey = getConfigurationKeyQName(context.getTargetApplicationName(), context.getRTConfiguration().getName());
    hInfoRegistry.setHandlerChain(htKey, Collections.unmodifiableList(hInfos));
    //initializes handler instances
    initHandlerChain(htKey);
  }
  
  /**
   * Creates, intantiates and maps the HandlerChain object under specific configuration QName key.
   * @param qname unique configuration key.
   */
  private static void initHandlerChain(QName cfgKey) throws JAXRPCHandlersException {
    List hInfos = (List) hInfoRegistry.getHandlerChain(cfgKey);
    if (hInfos == null) {      
      throw new JAXRPCHandlersException(JAXRPCHandlersException.MISSING_CONFIGURAION, new Object[]{cfgKey});   
    }

    List handlers = new ArrayList();
    List rolesList = new ArrayList();
    HandlerInfoImpl hInfo;
    Handler h;
    for (int i = 0; i < hInfos.size(); i++) {
      hInfo = (HandlerInfoImpl) hInfos.get(i);
      try {
        h = (Handler) hInfo.getHandlerClass().newInstance();
      } catch (Exception e) {
        throw new JAXRPCHandlersException(JAXRPCHandlersException.UNABLE_TO_INSTANTIATE_CLASS, new Object[]{hInfo.getHandlerClass()}, e);
      }
      //initialize the handler. This is part of its lifecycle.
      h.init(hInfo);
      //set the handler in the chain      
      handlers.add(h);
      //collect roles
      String[] roles = hInfo.getRoles();
      if (roles != null) {
        for (int j = 0; j < roles.length; j++) {
          rolesList.add(roles[j]);
        }
      }
    }
    //initialize handler chain
    HandlerChainImpl hChain = new HandlerChainImpl();
    hChain.setHandlers(handlers);
    hChain.setRoles((String[]) rolesList.toArray(new String[rolesList.size()]));
    hChain.setHanderInfos(hInfos);
    //register handler chain
    handlerChainMap.put(cfgKey, hChain);
  }
  
  private static QName getConfigurationKeyQName(String appName, String bdName) {
    String k = getConfigurationKey(appName, bdName);
    return new QName("", k);
  }
  /**
   * Generates unique key, which is used to resolve requested handlers configuration.
   * @param appName application name
   * @param bdName the value returned by ctx.getStaticContext().getRTConfiguration().getName().
   */
  private static String getConfigurationKey(String appName, String bdName) {
    return appName.hashCode() + "[" + appName + "_" + bdName + "]";
  }
  /**
   * This method maps in <code>map</code>, the operation element localname and ns, 
   * as well as part localnames and ns. See JSR109 6.2.2.2
   */
  private static void mapKeyMessageStructure(SOAPMessage jaxmMessage, Map map) throws Exception {
    Iterator itr = jaxmMessage.getSOAPBody().getChildElements();
    SOAPElement opElem = (SOAPElement) itr.next();
    //map localname and ns
    String lName = opElem.getElementName().getLocalName();
    String ns = opElem.getElementName().getURI();
    map.put(OPERATION_ELEM_QNAME, new QName("" + ns, lName));//bacause ns could be null
    
    //travers parts
    List pNames = new ArrayList(); //contains part local names
    itr = opElem.getChildElements();
    SOAPElement part;
    QName pQName;
    Object tmp;
    while (itr.hasNext()) {
      tmp = itr.next();
      LOC.debugT("mapKeyMessageStructure(), cur element " + tmp.getClass());
      if (tmp instanceof SOAPElement) {
        part = (SOAPElement) tmp;
        lName = part.getElementName().getLocalName();
        ns = part.getElementName().getURI();
        pQName = new QName("" + ns, lName);
        map.put(PART_ELEM_QNAME_PREF + ":" + pQName, pQName);
        pNames.add(lName);
      }
    }
    map.put(PART_NAME_LIST, pNames);
  }
  /**
   * Returns null only if the mapped key message entities are not changed, else returns 
   * string containing detailed message of what is changed.
   */
  private static String isMessageStructureChanged(SOAPMessage jaxmMessage, Map map) throws Exception {
    Iterator itr = jaxmMessage.getSOAPBody().getChildElements();
    if (! itr.hasNext()) {
      return LocalizableTextFormatter.formatString(ResourceAccessor.getResourceAccessor(), "webservices_2011");
    }
    SOAPElement opElem = (SOAPElement) itr.next();
    //map localname and ns
    String lName = opElem.getElementName().getLocalName();
    String ns = opElem.getElementName().getURI();
    QName curQName = new QName("" + ns, lName);//bacause ns could be null
    QName prevQName = (QName) map.get(OPERATION_ELEM_QNAME);
    
    if (! curQName.equals(prevQName)) {
      return LocalizableTextFormatter.formatString(ResourceAccessor.getResourceAccessor(), "webservices_2009"); 
    }
    
    //travers parts
    List pNames = (List) map.get(PART_NAME_LIST);
    itr = opElem.getChildElements();
    SOAPElement part;
    while (itr.hasNext()) {
      part = (SOAPElement) itr.next();
      lName = part.getElementName().getLocalName();
      //chech the order
      if (pNames.size() > 0) {
        if (! lName.equals(pNames.remove(0))) { //checks with the current
          return LocalizableTextFormatter.formatString(ResourceAccessor.getResourceAccessor(), "webservices_2010"); 
        }
      } else {
        return LocalizableTextFormatter.formatString(ResourceAccessor.getResourceAccessor(), "webservices_2010"); 
      }
      ns = part.getElementName().getURI();
      curQName = new QName("" + ns, lName);
      prevQName = (QName) map.get(PART_ELEM_QNAME_PREF + ":" + curQName);

      if (! curQName.equals(prevQName)) {
        return LocalizableTextFormatter.formatString(ResourceAccessor.getResourceAccessor(), "webservices_2010"); 
      }
      
    }    
    return null;
  }
  /**
   * Initializes SAP message, <code>msg</code>, as fault message with specific <code>fCode</code> and <code>faultString</code>.
   */
  private static void createFaultMessage(String fCode, String faultString, SOAPMessageImpl msg, ProviderContextHelper ctx) throws JAXRPCHandlersException {
    SOAPMessage jaxmMsg = JAXRPCHandlersEngine.createMessage();
    try {
      SOAPFault fault = jaxmMsg.getSOAPBody().addFault();
      SOAPEnvelope envelope = jaxmMsg.getSOAPPart().getEnvelope();
      Name n = envelope.createName(fCode,"soap-env", NS.SOAPENV);
      fault.setFaultCode(n);
      fault.setFaultString(faultString);
      msg.clear();
      msg.initWriteMode(SOAPMessageImpl.SOAP11_NS);//JAX-RPC is supposed to support only SOAP1.1.;
      JAXRPCHandlersEngine.serializeJAXMessageIntoSOAPMessage(jaxmMsg, msg);
      ctx.setMessageSemantic(ProviderContextHelper.FAULT_MSG); //mark message as fault msg
    } catch (Exception e) {
      throw new JAXRPCHandlersException(e);
    }
  }
  
}
