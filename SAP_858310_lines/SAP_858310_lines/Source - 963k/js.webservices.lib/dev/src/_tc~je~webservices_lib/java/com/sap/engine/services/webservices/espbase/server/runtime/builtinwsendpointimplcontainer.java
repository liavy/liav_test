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
package com.sap.engine.services.webservices.espbase.server.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.esp.ConfigurationContext;
import com.sap.engine.interfaces.webservices.esp.ImplementationContainer;
import com.sap.engine.interfaces.webservices.runtime.EventObject;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;
import com.sap.engine.lib.logging.LoggingHelper;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.server.BuiltInWSEndpointManager;
import com.sap.engine.services.webservices.espbase.server.ProviderContextHelper;
import com.sap.engine.services.webservices.espbase.server.StaticConfigurationContext;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Provides implementation of BuiltInWSEndpointManager and ImplementationContainer interfaces.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-12-14
 */
public class BuiltInWSEndpointImplContainer implements BuiltInWSEndpointManager, ImplementationContainer {
  
  private static final Location LOC = Location.getLocation(BuiltInWSEndpointImplContainer.class);
  private static final String PERSISTABLE_KEY_PROPERTY  =  "BuiltInWSEndpointImplContainer-key";
  
  private Hashtable epTable = new Hashtable();//key 'actionKey'(string), value endpoint (Object)
  private Hashtable mappingTable = new Hashtable();//key 'actionKey'(string), value mapping (InterfaceMapping)
  private Hashtable variantTable = new Hashtable();//key 'actionKey'(string), value variant (Variant)
  private Hashtable bdTable = new Hashtable();//key 'actionKey'(string), value binding data (BindingData)
  private Hashtable tmRegTable = new Hashtable();//key 'actionKey'(string), value type mapping registry (TypeMappingRegistry)
  
  public static final BuiltInWSEndpointImplContainer SINGLETON  =  new BuiltInWSEndpointImplContainer();
  
  private BuiltInWSEndpointImplContainer() {
    //declare private default contructor because this is singleton
  }
//================= BuiltInWSEndpointManager methods ========================  
  public List registerEndpoint(String appName, String wsName, String bdName, String[] actions, Object ep, InterfaceMapping intfMapping, TypeMappingRegistry tmReg, Variant v, BindingData bd) throws RuntimeProcessException {
    String action;
    List res = new ArrayList();
    for (int i = 0; i < actions.length; i++) {
      action = actions[i];
      String key = getKey(appName, wsName, bdName, action);
      synchronized (this) {
        epTable.put(key, ep);
        mappingTable.put(key, intfMapping);
        variantTable.put(key, v);
        bdTable.put(key, bd);
        tmRegTable.put(key, tmReg);
      }
      res.add(key);
    }
    return res;
  }
  
  public List registerEndpoint(String appName, String wsName, String bdName, String[] actions, Object ep, InterfaceMapping intfMapping, TypeMappingRegistry tmReg) throws RuntimeProcessException {
    String action;
    List res = new ArrayList();
    for (int i = 0; i < actions.length; i++) {
      action = actions[i];
      String key = getKey(appName, wsName, bdName, action);
      synchronized (this) {
        epTable.put(key, ep);
        mappingTable.put(key, intfMapping);
        tmRegTable.put(key, tmReg);
      }
      res.add(key);
    }
    return res;
  }
  
  public Object unregisterEndpoint(String key) throws RuntimeProcessException {
    Object o = null; 
    synchronized (this) {
      o = epTable.remove(key);
      mappingTable.remove(key);
      variantTable.remove(key);
      bdTable.remove(key);
    }
    return o;
  }

//================= ImplementationContainer methods ========================  
  public String getImplementationID() {
    return BuiltInWSEndpointManager.IMPLEMENTATION_CONTAINER_ID;
  }
  
  public ClassLoader getImplementationLoader(ConfigurationContext context) throws RuntimeProcessException {
    final String METHOD = "getImplementationLoader(): ";
    ProviderContextHelper ctx = (ProviderContextHelper) context;
    StaticConfigurationContext sC = ctx.getStaticContext();
    String key = getKeyPropertyFromContext(ctx);  
    Object ep = null;
    synchronized (this) {
      LOC.debugT(METHOD + " getting instance by using key '" + key + "'");
      ep = this.epTable.get(key);
    }
    if (ep != null) {
      return ep.getClass().getClassLoader();
    } 
    throw new ServerRuntimeProcessException(RuntimeExceptionConstants.UNABLE_TO_FIND_ENTITY_USING_ACTION_KEY, new Object[]{"endpoint", key});
  }
  
  public Object invokeMethod(String methodName, Class[] parameterClasses, Object[] parameters, ConfigurationContext context) throws RuntimeProcessException, InvocationTargetException {
    final String METHOD = "invokeMethod(): ";
    ProviderContextHelper ctx = (ProviderContextHelper) context;
    String key = getKeyPropertyFromContext(ctx);  
    Object ep = null;
    synchronized (this) {
      LOC.debugT(METHOD + " getting instance by using key '" + key + "'");
      ep = this.epTable.get(key);
    }
    if (ep == null) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.UNABLE_TO_FIND_ENTITY_USING_ACTION_KEY, new Object[]{"endpoint", key});
    }
    
    Method method = null;
    try { 
      method = ep.getClass().getMethod(methodName, parameterClasses);
    } catch (NoSuchMethodException nmE) {
      throw new ServerRuntimeProcessException(RuntimeExceptionConstants.CANNOT_FIND_JAVA_OPERATION, new Object[]{methodName, Integer.toString(parameterClasses.length), ep}, nmE);  
    }

    Object returnObject = null;
    try {
      LOC.debugT(METHOD + "invoking method '" + method + "'...");
      returnObject = method.invoke(ep, parameters);
      LOC.debugT(METHOD + "normal completion of method: " + method);
    } catch (InvocationTargetException invE) {
      LoggingHelper.traceThrowable(Severity.PATH, LOC, METHOD + " method '" + method + "' completed with exception.", invE.getTargetException());
      throw invE;
    } catch (Exception e) {
      throw new ServerRuntimeProcessException(e);
    }
    return returnObject;
  }
  
  public void notify(EventObject event, ConfigurationContext context) throws RuntimeProcessException {
  }
  
//================= package level methods ========================
  /**
   * @return InterfaceMapping instance mapped under, or null if none.
   */
  synchronized InterfaceMapping getInterfaceMapping(String key) {
    return (InterfaceMapping) this.mappingTable.get(key);
  }
  /**
   * @return Variant instance mapped under key, or null if none.
   */
  synchronized Variant getVariant(String key) {
    return (Variant) this.variantTable.get(key);
  }
  /**
   * @return BindingData instance mapped under <code>key</code> key, or null if none.
   */
  synchronized BindingData getBindingData(String key) {
    return (BindingData) this.bdTable.get(key);
  }
  /**
   * @return TypeMappingRegistry instance mapped under <code>action</code> key, or null if none.
   */
  synchronized TypeMappingRegistry getTypeMappingRegistry(String key) {
    return (TypeMappingRegistry) this.tmRegTable.get(key);
  }
  
  String getKey(String appName, String wsName, String bdName, String action) {
    return "appName:{" + appName + "}, wsName:{" + wsName + "}, bdName:{" + bdName + "}, action:{" + action + "}";
  }
  
  void bindKeyInContext(ConfigurationContext ctx, String key) {
    ProviderContextHelper pCtx = (ProviderContextHelper) ctx;
    pCtx.getPersistableContext().setProperty(PERSISTABLE_KEY_PROPERTY, key);
  }
  
//================ private methods ==========================  
  
  private String getKeyPropertyFromContext(ProviderContextHelper pCtx) {
    return (String) pCtx.getPersistableContext().getProperty(PERSISTABLE_KEY_PROPERTY);
  }
}
