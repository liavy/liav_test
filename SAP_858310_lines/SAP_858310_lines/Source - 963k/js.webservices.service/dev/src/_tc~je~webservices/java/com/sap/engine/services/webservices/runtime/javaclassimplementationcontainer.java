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
package com.sap.engine.services.webservices.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import com.sap.engine.frame.ServiceContext;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ImplementationContainer;
import com.sap.engine.interfaces.webservices.runtime.EventObjectIDs;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.interfaces.webservices.runtime.Transport;
import com.sap.engine.interfaces.webservices.runtime.soaphttp.HTTPTransport;
import com.sap.engine.lib.logging.LoggingHelper;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.mappings.ImplementationLink;
import com.sap.engine.services.webservices.espbase.mappings.MappingContext;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.runtime.ProviderContextHelperImpl;
import com.sap.engine.services.webservices.runtime.definition.JavaImplConstants;
import com.sap.engine.services.webservices.server.WebServicesContainer;
import com.sap.engine.services.webservices.server.WebServicesFrame;
import com.sap.engine.services.webservices.tools.InstancesPool;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-23
 */
public class JavaClassImplementationContainer implements ImplementationContainer {

  private static final String IMPLEMENTATION_ID   =  JavaImplConstants.JAVA_ID;

  //implementation properties
  private static final String JAVA_CLASS          =  "java-class";
  private static final String SERVLET_NAME_PROP   = "servlet-name"; 
  private static final Location LOC = Location.getLocation(JavaClassImplementationContainer.class);
  
  private LoadContext loadContext;
  private SessionTable statefulCache;  //InstanceWrapper values
  private StatelessTable statelessCache; //InstancesPool values

  public JavaClassImplementationContainer(ServiceContext serviceContext) {
    this.loadContext = serviceContext.getCoreContext().getLoadContext();
    this.statelessCache = new StatelessTable();
    this.statefulCache = new SessionTable();
  }

   public String getImplementationID() {
    return IMPLEMENTATION_ID;
  }

  public ClassLoader getImplementationLoader(ConfigurationContext context) throws RuntimeProcessException {
    String appName = ((ProviderContextHelper) context).getStaticContext().getTargetApplicationName();
    ClassLoader loader = loadContext.getClassLoader(appName);

    if (loader == null) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.NO_APPLICATION_CLASSLOADER_FOUND, new Object[]{appName});
    }
    return loader;
  }

  public Object invokeMethod(String methodName, Class[] parameterClasses, Object parameters[], ConfigurationContext ctx) throws RuntimeProcessException, InvocationTargetException {
    ProviderContextHelper context = (ProviderContextHelper) ctx;
    InstanceWrapper wrapper = null;
    //check for servlet endpoint
    Object targetInstance = getJ2EEServletEndpoint(context);
    if (targetInstance == null) {
      wrapper = getInstance(context);
      targetInstance = wrapper.getInstance();
    }
    
    Method method = null;
    try {
      method = targetInstance.getClass().getMethod(methodName, parameterClasses);
    } catch (NoSuchMethodException nmE) {
      rollBackInstance(context, wrapper);
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CANNOT_FIND_JAVA_OPERATION, new Object[]{methodName, Integer.toString(parameterClasses.length), wrapper.getInstance()}, nmE);
    }

    Object returnObject = null;
    try {
      LOC.logT(Severity.PATH,  "[invokeMethod(...)]", "Invoking method '" + method + "'...");
      returnObject = method.invoke(targetInstance, parameters);
      LOC.logT(Severity.PATH,  "[invokeMethod(...)]", "Normal completion of method: " + method);
    } catch (IllegalArgumentException illargE) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.ILLEGAL_ARGUMENT_EXCEPTION, new Object[]{illargE.getLocalizedMessage()}, illargE);
    } catch (IllegalAccessException illaccE) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.ACCESS_TO_METHOD_DENIED, new Object[]{illaccE.getLocalizedMessage()}, illaccE);
    } catch (InvocationTargetException invE) {
      LoggingHelper.traceThrowable(Severity.PATH, LOC, "[invokeMethod(...)]: Method '" + method + "' completed with exception.", invE.getTargetException());
      throw invE;
    } finally {
      if (wrapper != null) {
        rollBackInstance(context, wrapper);        
      }
    }

    return returnObject;
  }

  public void notify(com.sap.engine.interfaces.webservices.runtime.EventObject event, ConfigurationContext context) throws RuntimeProcessException {
    if (event.getEventID().equals(EventObjectIDs.STOP_APPLICATION)) {
      clearCaches((String) event.getData());
    }
  }

  /**
   * Used in remove of application to clear the cached instances
   * for this application
   */
  public void clearCaches(String applicationName) {
    Enumeration keys;
    String currKey;

    //While cleaning no other operation should be done
    synchronized (this.statelessCache) {
      keys = this.statelessCache.keys();

      while (keys.hasMoreElements()) {
        currKey = (String) keys.nextElement();
        if (currKey.startsWith(applicationName)) {
          statelessCache.get(currKey).clear();
        }
      }
    }

    //While cleaning no other operation should be done
    synchronized (this.statefulCache) {
      keys = this.statefulCache.keys();

      while (keys.hasMoreElements()) {
        currKey = (String) keys.nextElement();
        if (currKey.startsWith(applicationName)) {
          statefulCache.removeInstance(currKey);
        }
      }
    }
  }
  /**
   * Returns object on which business methods should be invoked, or null if this is not request for J2EE servlet endpoint.
   */
  private Object getJ2EEServletEndpoint(ProviderContextHelper pcH) throws ServerRuntimeProcessException {
    /*Object servlet = pcH.getDynamicContext().getProperty(RuntimeProcessingEnvironment.CALLING_SERVLET_PROPERTY);
    if ((servlet != null) && (servlet instanceof ServiceEndpointWrapper)) {
      Object result = ((ServiceEndpointWrapper) servlet).getServiceEndpointInstance();
      if (result == null) {
        throw new ServerRuntimeProcessException(RuntimeExceptionConstants.SERVLET_ENDPOINT_WRAPPER_DOES_NOT_WRAP_INSTANCE, new Object[]{servlet});
      }
      return result;
    }*/
    Object wsEndPoint = null;
    ImplementationLink iLink = pcH.getImplementationLink();
    String servletName = iLink.getProperty(SERVLET_NAME_PROP);
    if (servletName != null && servletName.length() > 0){
      BindingData bData = pcH.getStaticContext().getRTConfiguration();
      //String url = pcH.getTransport().getEntryPointID();
      String url = pcH.getStaticContext().getEndpointRequestURI();
      Service s = WebServicesContainer.getServiceContext().getServiceForBindingData(url);
      ServiceData sData = s.getServiceData();
      String contextRoot = sData.getContextRoot();
      if (contextRoot == null || contextRoot.length() < 1){
        contextRoot = bData.getUrl();
        if (contextRoot == null || contextRoot.length() < 1){
          return null;
        }
      }
      try{
        wsEndPoint = WebServicesFrame.getWebContainerExtension().getWebContainerExtensionContext().getWebModuleContext(contextRoot).getWebServiceEndPoint(servletName);
      }catch (Exception e) {
        throw new ServerRuntimeProcessException(e); 
      }
      return wsEndPoint;
    }
    return null;
  }
  
  private InstanceWrapper getInstance(ProviderContextHelper pcH) throws ServerRuntimeProcessException {
    MappingContext implMappings = pcH.getImplementationLink();

    String className = implMappings.getProperty(JAVA_CLASS);

    if (className == null) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.MISSING_IMPL_PROPERTY, new Object[]{JAVA_CLASS, implMappings});
    }

    String sessionID = pcH.getSessionID();
    String applicationName = pcH.getStaticContext().getTargetApplicationName();

    //if no session is open
    if (sessionID == null) {
      return useStatelessInstance(applicationName, className, pcH.getStaticContext().getLogLocation());
    } else {
      return useStatefulInstance(sessionID, pcH.getTransport(), applicationName, className, pcH.getStaticContext().getLogLocation());
    }

  }

  private void rollBackInstance(ProviderContextHelper pcH, InstanceWrapper instance) throws ServerRuntimeProcessException {
    String sessionID = pcH.getSessionID();

    if (sessionID != null) { //session use
      statefulCache.rollbackInstance(sessionID, instance);
    } else { //no session
      statelessCache.rollBackInstance(instance);
    }
  }

  private void removeStatefulInstance(ProviderContextHelperImpl pcH, String id) {
    MappingContext implMappings = pcH.getImplementationLink();
    String className = implMappings.getProperty(JAVA_CLASS);
    String appName = pcH.getStaticContext().getTargetApplicationName();

    String tableID  = getStatefulCacheKey(appName, className, id);
    statefulCache.removeInstance(tableID);
  }

  private InstanceWrapper useStatelessInstance(String applicationName, String implClassName, Location loc) throws ServerRuntimeProcessException {
    InstanceWrapper instanceWrapper = null;
    Object instance = null;

    String cacheKey = this.getStatelessCacheKey(applicationName, implClassName);
    InstancesPool instancePool;

    instancePool = statelessCache.getPool(cacheKey);
    instanceWrapper = (InstanceWrapper) instancePool.getInstance();
    if (instanceWrapper == null) {
      instance = createImplementationInstance(applicationName, implClassName);
      instanceWrapper = new InstanceWrapper(instance, cacheKey, instancePool);
      //instanceWrapper.setSessionTimeout(STATELESS_TIMEOUT);
      instanceWrapper.logLocation = loc;
    }

    return instanceWrapper;
  }

  private InstanceWrapper useStatefulInstance(String sessionId, Transport transport, String applicationName, String implClassName, Location loc) throws ServerRuntimeProcessException {
    Object instance = null;
    String cacheKey = this.getStatefulCacheKey(applicationName, implClassName, sessionId);
    InstanceWrapper instanceWrapper = statefulCache.getInstance(cacheKey);

    if (instanceWrapper == null) { //no instance for this id found
      instance = createImplementationInstance(applicationName, implClassName);
      instanceWrapper = new InstanceWrapper(instance, cacheKey, null);
      //session is in seconds expexted in millies.
      //instanceWrapper.setSessionTimeout((timeout * 1000) + SESSION_TIMEOUT_DISPERSAL);
      instanceWrapper.logLocation = loc;
      //set the SessionTable into HTTPSession object
      ((HTTPTransport) transport).getRequest().getSession().setAttribute(cacheKey, statefulCache);
      statefulCache.registerInstanceInuse(instanceWrapper);
    }

    return instanceWrapper;
  }

  private final Object createImplementationInstance(String applicationName, String className) throws ServerRuntimeProcessException {
    ClassLoader loader = loadContext.getClassLoader(applicationName);

    Class implClass = null;

    try {
      implClass = loader.loadClass(className);
    } catch (ClassNotFoundException cnfE) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.UNABLE_TO_LOAD_CLASS, new Object[]{className, loader}, cnfE);
    }

    Object newObject = null;
    try {
      newObject = implClass.newInstance();
    } catch (Exception e) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CANNOT_CREATE_IMPL_INSTANCE, new Object[]{e.getLocalizedMessage()}, e);
    }

    return newObject;
  }

  private String getStatelessCacheKey(String applicationName, String implClassName) {
    return applicationName + "/" + implClassName;
  }

  private String getStatefulCacheKey(String applicationName, String implClassName, String sessionId) {
    return applicationName + "/" + implClassName + "/" +  sessionId;
  }

}
