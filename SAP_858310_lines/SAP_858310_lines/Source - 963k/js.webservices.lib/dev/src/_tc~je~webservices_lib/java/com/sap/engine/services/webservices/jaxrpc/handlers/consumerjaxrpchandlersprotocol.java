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
package com.sap.engine.services.webservices.jaxrpc.handlers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.Handler;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ConsumerProtocol;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.ProtocolException;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientProtocolNotify;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientServiceContext;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.BuiltInConfigurationConstants;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.messaging.impl.SOAPMessageImpl;
import com.sap.engine.services.webservices.jaxrpc.handlers.exceptions.JAXRPCHandlersException;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-3-21
 */
public class ConsumerJAXRPCHandlersProtocol implements ConsumerProtocol, ClientProtocolNotify, Serializable {
  
  public static final String PROTOCOL_NAME  =  "JAXRPCHandlersProtocol";

  private static final String HANDLERCHAIN_PROPERTY  =  "jax-rpc-handler-chain"; //here HandlerChain object is mapped.
  private static final String HANDLERCHAIN_REGISTRY_PROPERTY  =  "jax-rpc-handlerchain-registry";//here Map with handlerchain is mapped. 

  private static final String HANDLERCHAIN_PORTUSERS_COUNT_PREF  =  "jax-rpc-handlerchain-portusers-count";//here Integer object is mapped containing the current number of ports 
                                                                                                           //using specific handlerchain. This is only a prefix, the real key is
                                                                                                           //constructed as <prefix>:portQName.toString(). 
  
  private static final Location LOC = Location.getLocation(ConsumerJAXRPCHandlersProtocol.class);
  
  public String getProtocolName() {
    return PROTOCOL_NAME;
  }
  
  public int handleRequest(ConfigurationContext ctx) throws ProtocolException, MessageException {
    ClientConfigurationContext context = (ClientConfigurationContext) ctx;
    QName portKey = getPortQNameFromContext(context);

    HandlerChainImpl hChain = (HandlerChainImpl) getHandlerChainMap(context.getServiceContext()).get(portKey);
    if (hChain == null) {
      //throw new JAXRPCHandlersException(JAXRPCHandlersException.MISSING_CONFIGURAION, new Object[]{portKey});
      return ConsumerProtocol.CONTINUE;
    }
        
    SOAPMessageImpl msg = getSOAPMessage(context);
    //initialize JAXM message
    SOAPMessage soapMsg = JAXRPCHandlersEngine.createOutboundJAXMSOAPMessage(msg);
    
    SOAPMessageContextImpl soapCtx = new SOAPMessageContextImpl();
    soapCtx.setRoles(hChain.getRoles());
    soapCtx.setMessage(soapMsg);
    //set the object into context for later use.
    ConfigurationContext dContext = context.getDynamicContext();    
    dContext.setProperty(HANDLERCHAIN_PROPERTY, hChain);
    JAXRPCHandlersEngine.bindSOAPMessageContext(dContext, soapCtx); //bind SOAPMessageContext in context
    
    synchronized(hChain) { //synchronized because it is only one handlerchain for all port instances created for certain portQName.
      hChain.clearState();
      //invoke handleRequest() 
      hChain.handleRequest(soapCtx); //TODO - do not know what do to incase some handler has returned false.
    }    
    //prepare response message 
    msg.clear();
    msg.initWriteMode(SOAPMessageImpl.SOAP11_NS);//JAX-RPC is supposed to support only SOAP1.1.
    JAXRPCHandlersEngine.serializeJAXMessageIntoSOAPMessage(soapCtx.getMessage(), msg);
    return ConsumerProtocol.CONTINUE;
  }
  
  public int handleResponse(ConfigurationContext ctx) throws ProtocolException {
    ClientConfigurationContext context = (ClientConfigurationContext) ctx;
    QName portKey = getPortQNameFromContext(context);
    
    ConfigurationContext dContext = context.getDynamicContext();    
    HandlerChainImpl hChain = (HandlerChainImpl) dContext.getProperty(HANDLERCHAIN_PROPERTY);
    if (hChain == null) {
      //throw new JAXRPCHandlersException(JAXRPCHandlersException.MISSING_CONFIGURAION, new Object[]{portKey});
      return ConsumerProtocol.CONTINUE;
    }
    
    SOAPMessageContextImpl soapCtx = (SOAPMessageContextImpl) JAXRPCHandlersEngine.getSOAPMessageContext(dContext);
    
    //initialize JAXM message
    SOAPMessageImpl msg = getSOAPMessage(context);    
    SOAPMessage soapMsg = JAXRPCHandlersEngine.createInboundJAXMSOAPMessage(msg);      
    soapCtx.setMessage(soapMsg);
    
    Object fObj = null;
    try {
      fObj = soapMsg.getSOAPBody().getFault();
    } catch (SOAPException sE) {
      throw new JAXRPCHandlersException(sE);
    }
    synchronized(hChain) {//synchronized because it is only one handlerchain for all port instances created for certain portQName.
      if (fObj != null) {//in case of fault
        hChain.handleFault(soapCtx); //TODO - do not know what to do incase some handler has returned false.
      } else {
        hChain.handleResponse(soapCtx); //TODO - do not know what to do incase some handler has returned false.
      }          
    }
    
    //convert data into standard SOAPMessage
    JAXRPCHandlersEngine.convertInboundJAXMessageDataIntoSOAPMessage(soapCtx.getMessage(), msg);
    return ConsumerProtocol.CONTINUE;
  }
  
  public int handleFault(ConfigurationContext ctext) throws ProtocolException {
    return ConsumerProtocol.CONTINUE;
  }

  public void serviceInit(ClientServiceContext serviceContext) throws ProtocolException {
//    BindingData[] bds;
//    ServiceData ss = serviceContext.getServiceData();
//    String sNS = ss.getNamespace();
//    bds = ss.getBindingData();
//    for (int b = 0; b < bds.length; b++) {
//      initHandlers(bds[b], sNS, serviceContext);
//    }
  }
  
  public void portCreate(ClientConfigurationContext context) throws ProtocolException {
    BindingData bD = context.getStaticContext().getRTConfig();
    ClientServiceContext serviceContext = context.getServiceContext();
    String sNS = serviceContext.getServiceData().getNamespace();
    QName portKey = getPortQNameFromContext(context);
    
    HandlerChainImpl hChain = (HandlerChainImpl) getHandlerChainMap(serviceContext).get(portKey);
    if (hChain == null) { //only in case there is no HandlerChainImpl for the port, init one.
      try {
        initHandlers(bD, sNS, serviceContext, context.getClientAppClassLoader());           
      } catch (JAXRPCHandlersException e) {
        throw new ProtocolException(e);
      }
    }
    hChain = (HandlerChainImpl) getHandlerChainMap(serviceContext).get(portKey);
    if (hChain != null) { //if HC is defined for the port, increase its portcount.
      increaseHCPortUsersCount(serviceContext, portKey);
    }
  }
  
  public void serviceDestroy(ClientServiceContext serviceContext) throws ProtocolException {
//    //destroy all handler chains.
//    Map hChainMap = (Map) getHandlerChainMap(serviceContext);
//    HandlerChainImpl hChain = null;
//    Iterator hChains = hChainMap.values().iterator();
//    while (hChains.hasNext()) {
//      hChain = (HandlerChainImpl) hChains.next();
//      hChain.destroy();
//    }
  }
  
  public void portDestroy(ClientConfigurationContext portContext) throws ProtocolException {
    //destroy all handler chains.
    Map hChainMap = (Map) getHandlerChainMap(portContext.getServiceContext());
    QName portKey = getPortQNameFromContext(portContext);
    HandlerChainImpl hChain = (HandlerChainImpl) hChainMap.get(portKey);
    if (hChain != null) {
      int n = decreaseHCPortUsersCount(portContext.getServiceContext(), portKey);
      if (n == 0) { //only when portcount is 0, remove the HC instance
        hChain = (HandlerChainImpl) hChainMap.remove(portKey);
        hChain.destroy();
      }
    }
  }
  /**
   * Reads configuration data, loads handers classes and initializes them.
   * This is the method which should be called on configuration start.
   */
  private void initHandlers(BindingData bD, String serviceNS, ClientServiceContext ctx, ClassLoader loader) throws JAXRPCHandlersException {
    QName portKey = getPortQName(bD, serviceNS);
    LOC.debugT("Start configuration: " + portKey);

    PropertyType prop = bD.getSinglePropertyList().getProperty(BuiltInConfigurationConstants.DEFAULT_PROPERTIES_NS, BuiltInConfigurationConstants.JAXRPC_HANDLERS_CONFIG_PROPERTY);
    if (prop == null) {
      LOC.debugT("No JAX-RPC handlers for port: " + portKey);
      return; //there is nothing to be loaded
    }
    //load conf data
    List hInfos = JAXRPCHandlersEngine.loadHanderInfoConfigurations(prop.get_value());
    //load handlers' classes
    HandlerInfoImpl hInf;
    String hCName;
    Class hClass;
    for (int i = 0; i < hInfos.size(); i++) {
      hInf = (HandlerInfoImpl) hInfos.get(i);
      hCName = hInf.getHandlerClassName();
      try {
        hClass = loader.loadClass(hCName); 
      } catch (ClassNotFoundException cnfE) {
        throw new JAXRPCHandlersException(JAXRPCHandlersException.UNABLE_TO_LOAD_CLASS, new Object[]{hCName, "", loader});
      }
      hInf.setHandlerClass(hClass);
    }
    //maps the List object into registry under specific qname. Make the list unmodifiable.
    HandlerRegistryImpl hReg = getHandlerRegistry(ctx);
    List unModifList = Collections.unmodifiableList(hInfos);
    hReg.setHandlerChain(portKey,  unModifList);
    //initializes handler instances
    HandlerChainImpl hChain = initHandlerChain(unModifList);
    //register hChain.
    getHandlerChainMap(ctx).put(portKey, hChain);
  }

  /**
   * Creates, intantiates and maps the HandlerChain object under specific configuration QName key.
   * @param hInfos List of HanderInfo objects.
   * @return initialized HandlerChainImpl object
   */
  private HandlerChainImpl initHandlerChain(List hInfos) throws JAXRPCHandlersException {
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
    return hChain;
  }

  public static QName getPortQName(BindingData bD, String serviceNS) {
    return new QName(serviceNS, bD.getName());
  }
  /**
   * Returns portQName mapped into <code>ctx</code>.
   */
  public static QName getPortQNameFromContext(ClientConfigurationContext ctx) {
    String ns = ctx.getServiceContext().getServiceName().getNamespaceURI();
    return getPortQName(ctx.getStaticContext().getRTConfig(), ns);
  }
  private ClassLoader getLoader(ClientServiceContext ctx) {
    return ctx.getApplicationClassLoader();
  }
  
  private HandlerRegistryImpl getHandlerRegistry(ClientServiceContext ctx) {
    HandlerRegistryImpl hReg = (HandlerRegistryImpl) ctx.getProperty(ClientServiceContextImpl.HANDLER_REGISTRY);
    if (hReg == null) {
      hReg = new HandlerRegistryImpl();
      ctx.setProperty(ClientServiceContextImpl.HANDLER_REGISTRY, hReg);
    }
    return hReg;
  }
  /**
   * Returns Map object into which keys are port QNames and values are HandlerChainImpl objects.
   */
  private Map getHandlerChainMap(ClientServiceContext ctx) {
    Map reg = (Map) ctx.getProperty(HANDLERCHAIN_REGISTRY_PROPERTY);
    if (reg == null) {
      reg = new Hashtable();
      ctx.setProperty(HANDLERCHAIN_REGISTRY_PROPERTY, reg);
    }
    return reg;
  }
  /**
   * Returns the message bound into <code>ctx</code>.
   */
  private SOAPMessageImpl getSOAPMessage(ClientConfigurationContext ctx) {
    return (SOAPMessageImpl) ctx.getMessage();
  }
  
  /**
   * Increases the port count for specific HC.
   */
  private void increaseHCPortUsersCount(ClientServiceContext ctx, QName portQName) {
    String key = getPortUsersCountKey(portQName);
    synchronized(ctx) { //use synchonization because this method and 'decreasePortUsersCount' can manipulate the specific property;
      Integer intObj = (Integer) ctx.getProperty(key);
      int n;
      if (intObj == null) { //no object is registered
        n = 0;
      } else {
        n = intObj.intValue();
      }
      n++;
      ctx.setProperty(key, new Integer(n));      
    }
  }
  /**
   * Decreases the port count for specific HC. 
   * @return The current port count - the one after the descrease.
   */
  private int decreaseHCPortUsersCount(ClientServiceContext ctx, QName portQName) {
    String key = getPortUsersCountKey(portQName);
    synchronized(ctx) { //use synchonization because this method and 'increasePortUsersCount' can manipulate the specific property;
      Integer intObj = (Integer) ctx.getProperty(key);
      if (intObj == null) {// the object must not be null...
        throw new IllegalStateException("PortCount object must be mapped.");
      }
      int n = intObj.intValue(); 
      n--;
      ctx.setProperty(key, new Integer(n));
      return n;
    }
  }
  
  private String getPortUsersCountKey(QName portQName) {
    return HANDLERCHAIN_PORTUSERS_COUNT_PREF + ":" + portQName.toString();
  }
}
