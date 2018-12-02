/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.server.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ImplementationContainer;
import com.sap.engine.interfaces.webservices.esp.Message;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.esp.RuntimeEnvironment;
import com.sap.engine.interfaces.webservices.runtime.MessageException;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.services.webservices.espbase.ConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.ServiceFactoryConfig;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.StaticConfigurationContext;
import com.sap.engine.services.webservices.espbase.server.TransportBinding;
import com.sap.engine.services.webservices.tools.InstancesPool;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-20
 */
public class ProviderContextHelperImpl extends ConfigurationContextImpl	implements ProviderContextHelper/*, EJBImplContainerCommunicator */{
    
  public static final String DYNAMIC_CONTEXT  =  "dynamic";
  public static final String PERSISTENT_CONTEXT  =  "persistable";
  public static final String STATIC_CONTEXT  =  "static";
  public static final String DYNAMIC_IMPLLINK  =  "dynamic_impllink";
  public static final String TRANSPORT  =  "transport";
  public static final String TRANSPORT_BINDING =  "transport_binding";
  public static final String OPERATION =  "operation";
  public static final String MESSAGE_EXCEPTION  =  "message_exception";
  public static final String SESSION_ID  =  "session_id";
  public static final String MESSAGE  =  "message";
  public static final String PROVIDERPROTOCOLS  =  "provider_protocols";
  public static final String MESSAGE_SEMANTIC  =  "message_semantic";
  public static final String SEND_NO_REPLY  =  "send_no_reply";
  public static final String APP_WS_CONTEXT_RESPONSE_SOAP_HEADERS  =  "app_ws_context_response_soap_headers";
  public static final String EJB_HNDL_MODE  =  "ejb_hndl_mode";
  public static final String EJB_HNDL_MODE_INVOKE_HNDLS  =  "ejb_hndl_mode_invoke_handlers";
  public static final String IMPL_LOADER  =  "impl_loader";
  public static final String PARAM_OBJECTS  =  "param_objects";
  public static final String PARAM_CLASSES  =  "param_classes";
  public static final String IMPLEMENTATION_CONTAINER  =  "implementation_container";
  public static final String RESTARTED_AFTER_HIBERNATION  =  "restarted_after_hibernation";
  public static final String UNDERSTOOD_SOAPHEADERS_SET  =  "understood-soapheaders-set";
  public static final String SDO_HELPER = "sdo_xml_helper";
  
  static RuntimeEnvironment environment;
  static InstancesPool contextPool = new InstancesPool(); //contains empty ProviderContexts with dynamic, persistant and static context set.  
        
  private static final Location LOC = Location.getLocation(ProviderContextHelperImpl.class); 

  ProviderContextHelperImpl() {
    super("ProviderContextHelper", null, ConfigurationContextImpl.STATIC_MODE);
    //creates and assigns persistable context 
    ConfigurationContextImpl persistent = new ConfigurationContextImpl(PERSISTENT_CONTEXT, this, ConfigurationContextImpl.PRERSISTENT_MODE);
    //creates and assigns dynamic context 
    ConfigurationContextImpl dynamic = new ConfigurationContextImpl(DYNAMIC_CONTEXT, this, ConfigurationContextImpl.NORMAL_MODE);
    //creates and assigns static context
    StaticConfigurationContext staticC = new StaticConfigurationContextImpl(STATIC_CONTEXT, this);      
  }
  
//  void setStaticConfigurationContext(StaticConfigurationContext ctx) {
//    super.subContexts.put(STATIC_CONTEXT, ctx);  
//  }
  
  public void sendNoReply(boolean b) {
    setPropertyInternal(SEND_NO_REPLY, new Boolean(b));
  }
  
  public boolean isSendNoReply() {
    Boolean b = (Boolean) getProperty(SEND_NO_REPLY);
    if (b != null) {
      return b.booleanValue();
    }
    return false;
  }
  
  void setTransport(Transport transport) {
    setPropertyInternal(TRANSPORT, transport);
  }
  
  void setTransportBinding(TransportBinding trb) {
    setPropertyInternal(TRANSPORT_BINDING, trb);
  }

  void setMessageException(MessageException msE) {
    setPropertyInternal(MESSAGE_EXCEPTION, msE);
  }
  
  TransportBinding getTransportBinding() {
    return (TransportBinding) getProperty(TRANSPORT_BINDING);
  }
  
  void setProtocols(ProviderProtocol[] protocols) {
    setPropertyInternal(PROVIDERPROTOCOLS, protocols);
  }
  
  ProviderProtocol[] getProtocols() {
    return (ProviderProtocol[]) getProperty(PROVIDERPROTOCOLS);
  }
  
	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getDynamicContext()
	 */
	public ConfigurationContext getDynamicContext() {
		return getSubContext(DYNAMIC_CONTEXT);  
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getStaticContext()
	 */
	public StaticConfigurationContext getStaticContext() {
		return (StaticConfigurationContext) getSubContext(STATIC_CONTEXT);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getPersistableContext()
	 */
	public ConfigurationContext getPersistableContext() {
		return getSubContext(PERSISTENT_CONTEXT);
	}
  
  public Object setPropertyInternal(String name, Object value) {
    return super.properties.put(name, value); 
  }

  Object removePropertyInternal(String name) {
    return super.properties.remove(name); 
  }
  
	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getTransport()
	 */
	public Transport getTransport() {
		return (Transport) getProperty(TRANSPORT);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getMessage()
	 */
	public Message getMessage() throws RuntimeProcessException {
    Message message = (Message) getProperty(MESSAGE);
    if (message != null) {
      return message;
    }
    TransportBinding tB = getTransportBinding();
    message = tB.createInputMessage(this);
    if (message != null) {
      setPropertyInternal(MESSAGE, message);
    }
    return message;
  }

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#setMessage(com.sap.engine.interfaces.webservices.esp.Message)
	 */
	public Message setMessage(Message msg) throws RuntimeProcessException {
    return (Message) setPropertyInternal(MESSAGE, msg);
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getProtocol(java.lang.String)
	 */
	public ProviderProtocol getProtocol(String protocolID) {
    ProviderProtocol[] protocols = (ProviderProtocol[]) getProperty(PROVIDERPROTOCOLS);
    if (protocols != null) {
      for (int i = 0; i < protocols.length; i++) {
        if (protocols[i].getProtocolName().equals(protocolID)) {
          return protocols[i];  
        }
      }
    }
		return null; 
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getOperation()
	 */
	public OperationMapping getOperation() throws RuntimeProcessException {
    OperationMapping operation = (OperationMapping) getProperty(OPERATION);
    if (operation != null) {
      return operation;
    }    
    TransportBinding tB = getTransportBinding();
    operation = tB.resolveOperation(this);
    setPropertyInternal(OPERATION, operation);
    return operation;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#createErrorMessage(java.lang.Throwable)
	 */
	public Message createErrorMessage(Throwable thr) throws RuntimeProcessException {
    Message msg = getTransportBinding().createFaultMessage(thr, this);
    return msg;
	}

	/* (non-Javadoc)
	 * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getMessageException()
	 */
	public MessageException getMessageException() {
		return (MessageException) getProperty(MESSAGE_EXCEPTION);
	}
  
  public String getSessionID() {
    return (String) getDynamicContext().getProperty(SESSION_ID);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getImplementationLink()
   */
  public ImplementationLink getImplementationLink() {
    ConfigurationContext ctx = getDynamicContext();
    Object res = ctx.getProperty(DYNAMIC_IMPLLINK);
    if (res != null) {
      return (ImplementationLink) res;
    }
    return getStaticContext().getInterfaceMapping().getImplementationLink();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.esp.ProviderContextHelper#getEnvironment()
   */
  public RuntimeEnvironment getEnvironment() {
    return environment;
  }

  public int getMessageSemantic() {
    Integer value = (Integer) getProperty(MESSAGE_SEMANTIC);
    if (value == null) { //not set
      return -1;
    }
    return value.intValue();
  }

  public int setMessageSemantic(int n) {
    int old = getMessageSemantic();
    setPropertyInternal(MESSAGE_SEMANTIC, new Integer(n));
    return old;
  }
  
  public void sendAsynchronousResponse() throws RuntimeProcessException {
    final String METHOD = "sendAsynchronousResponse(): ";
    TransportBinding tb = this.getTransportBinding();
    if (LOC.beDebug()) {
      LOC.debugT(METHOD + " transport binding instance - " + tb);
    }
    tb.sendAsynchronousResponse(this);
  }
  
  public void sendMessageAsSynchronousResponse() throws RuntimeProcessException {
    final String METHOD = "sendMessageAsSynchronousResponse(): ";
    TransportBinding tb = this.getTransportBinding();
    if (LOC.beDebug()) {
      LOC.debugT(METHOD + " transport binding instance - " + tb);
    }
    tb.sendResponseMessage(this, TransportBinding.SYNC_COMMUNICATION);
  }
  
  public static ProviderContextHelperImpl getPooledInstance() {
    ProviderContextHelperImpl ctx = (ProviderContextHelperImpl) contextPool.getInstance();
    if (ctx == null) {
      ctx = new ProviderContextHelperImpl();
    }
    return ctx;
  }
  
  public static void rollBackInstance(ProviderContextHelperImpl ctx) {
    ctx.clearPropertiesRecursive();
    ConfigurationContext dynamic = ctx.getDynamicContext();
    dynamic.clear(); 
    ConfigurationContext prs = ctx.getPersistableContext();
    prs.clear();
    ConfigurationContext s_ctx = ctx.getStaticContext();
    s_ctx.clear();
    contextPool.rollBackInstance(ctx);
  }
  /**
   * Returns a list containing response soap headers, in form of Element objects. The list is used by AppliationWebserviceContext
   * for storing response headers.
   */
  public List getAppWSContextResponseSOAPHeaders() {
    List list = (List) getDynamicContext().getProperty(APP_WS_CONTEXT_RESPONSE_SOAP_HEADERS);
    if (list == null) { //this is first invocation of this method - create list object
      list = new ArrayList();
      getDynamicContext().setProperty(APP_WS_CONTEXT_RESPONSE_SOAP_HEADERS, list);
    }
    return list;
  }
  /**
   * Sets flag denoting special ejb handlers processing mode.
   * @param b value of the flag
   */  
  void setEJBHandlersProcessMode(boolean b) {
    getDynamicContext().setProperty(EJB_HNDL_MODE, new Boolean(b));
  }
  /**
   * @return true if special ejb handlers processing mode is on, false otherwise.
   */
  public boolean isEJBHandlersProcessMode() {
    Boolean b = (Boolean) getDynamicContext().getProperty(EJB_HNDL_MODE);
    if (b != null) {
      return b.booleanValue();
    } else {
      return false;
    }
  }
  /**
   * Sets impl loader <code>loader</code> in the context
   */
  void setImplClassLoader(ClassLoader loader) {
    if (loader != null) {
      getDynamicContext().setProperty(IMPL_LOADER, loader);
    }
  }
  /**
   * @return impl classloader or null if not set
   */
  public ClassLoader getImplClassLoader() {
    return (ClassLoader) getDynamicContext().getProperty(IMPL_LOADER);
  }
  /**
   * Sets <code>params</code> into context
   */  
  void setParameterObjects(Object[] params) {
    getDynamicContext().setProperty(PARAM_OBJECTS, params);
  }
  /**
   * @return parameter objects array, or null if not set
   */
  Object[] getParameterObjects() {
    return (Object[]) getDynamicContext().getProperty(PARAM_OBJECTS);
  }
  /**
   * Sets <code>pClasses</code> into context;
   */
  void setParameterClasses(Class[] pClasses) {
    // i044259
    if (pClasses != null){
      getDynamicContext().setProperty(PARAM_CLASSES, pClasses);
    }
  }
  /**
   * @return parameter classes array, or null if not set
   */
  Class[] getParameterClasses() {
    return (Class[]) getDynamicContext().getProperty(PARAM_CLASSES);
  }
  
  void setEJBHandlersProcessModeInvHandlers(boolean b) {
    getDynamicContext().setProperty(EJB_HNDL_MODE_INVOKE_HNDLS, new Boolean(b));
  }
  
  public boolean isEJBHandlersProcessModeInvHandlers() {
    Boolean b = (Boolean) getDynamicContext().getProperty(EJB_HNDL_MODE_INVOKE_HNDLS);
    if (b != null) {
      return b.booleanValue();
    } else {
      return false;
    }
  }
  
  void setImplementationContaner(ImplementationContainer implContainer) {
    getDynamicContext().setProperty(IMPLEMENTATION_CONTAINER, implContainer);
  }
  
  ImplementationContainer getImplementationContaner() {
    return (ImplementationContainer) getDynamicContext().getProperty(IMPLEMENTATION_CONTAINER);
  }
  
  public boolean isRestartedAfterHibernation() {
    return Boolean.valueOf((String) getDynamicContext().getProperty(RESTARTED_AFTER_HIBERNATION)).booleanValue();  
  }
  
  void restartedAfterHibernation(boolean b) {
    getDynamicContext().setProperty(RESTARTED_AFTER_HIBERNATION, Boolean.toString(b));
  }
  
  public void markSOAPHeaderAsUnderstood(QName soapHeader) {
    Set set = getUnderstoodSOAPHeadersSet();
    set.add(soapHeader);
  }
  /**
   * Returns Set object responsible for holding understood soap headers 
   */
  public Set getUnderstoodSOAPHeadersSet() {
    Set set = (Set) getDynamicContext().getProperty(UNDERSTOOD_SOAPHEADERS_SET);
    if (set == null) {
      set = new HashSet();
      getDynamicContext().setProperty(UNDERSTOOD_SOAPHEADERS_SET, set);
    }
    return set;
  }
  
  public commonj.sdo.helper.XMLHelper getSdoHelper() {
    return (commonj.sdo.helper.XMLHelper) getProperty(SDO_HELPER);
  }
  
   public void setSdoHelper(commonj.sdo.helper.XMLHelper sxh) {
     setPropertyInternal(SDO_HELPER, sxh);
  }
   
   public void setSdoHelper(commonj.sdo.helper.XMLHelper sxh, Map options){
     setPropertyInternal(SDO_HELPER, sxh);
     setPropertyInternal(ServiceFactoryConfig.OPTIONS, options);
   }
   
   /*public String getImplLinkProperty(String propertyName){
     ImplementationLink implLink = getImplementationLink();
     return implLink.getProperty(propertyName);
   }
   
   public String getTargetApplicationName(){
     StaticConfigurationContext staticContext = getStaticContext();
     return staticContext.getTargetApplicationName();
   }
   
   
   public SOAPMessageContext getSOAPMessageContextFromThread(){
     return JAXRPCHandlersEngine.getSOAPMessageContextFromThread();
   }
   
   public String implLinkToString(){
     ImplementationLink implLink = getImplementationLink();
     return implLink.toString();
   }*/
}

