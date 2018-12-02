/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.espbase.client.api;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;
import com.sap.engine.services.webservices.espbase.client.api.impl.HTTPControlInterfaceImpl;
import com.sap.engine.services.webservices.espbase.client.api.impl.HTTPControlInterfaceNYImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.impl.DInterfaceInvokerImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.core.JAXWSProxy;
import com.sap.engine.services.webservices.espbase.wsdas.WSDAS;
import com.sap.engine.services.webservices.espbase.wsdas.impl.WSDASImpl;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

import java.lang.reflect.InvocationTargetException;


/**
 * HTTPControlInterface factory.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class HTTPControlFactory {

  /**
   * Returns HTTPControlInterface instace for specific port.
   * @param port
   * @return
   */
  public static final HTTPControlInterface getInterface(Object port) {
    if (port == null) {
      return null;
    }
    if (port instanceof DInterfaceInvokerImpl) {
      return new HTTPControlInterfaceNYImpl(((DInterfaceInvokerImpl) port)._getConfigurationContext());
    }
    if (port instanceof WSDASImpl && ((WSDASImpl) port)._getConfigurationContext()!= null) {
        return new HTTPControlInterfaceNYImpl(((WSDASImpl) port)._getConfigurationContext());
    }
    if (port instanceof DynamicStubImpl) {
      return new HTTPControlInterfaceNYImpl(((DynamicStubImpl) port)._getConfigurationContext());
    }
    if (port instanceof BaseGeneratedStub) {
      return new HTTPControlInterfaceImpl((BaseGeneratedStub) port);
    }
    if (port instanceof JAXWSProxy) {
      return new HTTPControlInterfaceNYImpl(((JAXWSProxy) port)._getConfigurationContext());
    }
    return null;
  }

  public static HTTPProxy getGlobalHttpProxy() {
    Object espService;
    try {
      espService = getEspService();
    } catch (NamingException e) {//the esp service is not started, or used standalone
      e.printStackTrace();
      return null;
    }
//    HTTPProxy proxy = espService.getWebserviceHttpProxyHelper().getWebserviceHTTPProxy();
//    proxy.getProxyHost();
//    proxy.getProxyPort();
//    proxy.getProxyUser();
//    proxy.getProxyPass();
//    proxy.getExcludeList();
//    proxy.isBypassLocalAddresses();
    try {
      Object webserviceHttpProxyHelper = espService.getClass().getMethod("getWebserviceHttpProxyHelper", null).invoke(espService, null);
      Object proxy = webserviceHttpProxyHelper.getClass().getMethod("getWebserviceHTTPProxy", null).invoke(webserviceHttpProxyHelper, null);
      if (proxy == null) {
        return null;
      }
      String proxyHost = (String)proxy.getClass().getMethod("getProxyHost", null).invoke(proxy, null);
      if (proxyHost == null) {
        return null;
      }
      int proxyPort = (Integer)proxy.getClass().getMethod("getProxyPort", null).invoke(proxy, null);
      String proxyUser = (String)proxy.getClass().getMethod("getProxyUser", null).invoke(proxy, null);
      String proxyPass = (String)proxy.getClass().getMethod("getProxyPass", null).invoke(proxy, null);
      String excludeList = (String)proxy.getClass().getMethod("getExcludeList", null).invoke(proxy, null);
      boolean bypassLocalAddresses = (Boolean)proxy.getClass().getMethod("isBypassLocalAddresses", null).invoke(proxy, null);

      return new HTTPProxy(proxyHost,
              proxyPort,
              proxyUser,
              proxyPass,
              excludeList,
              bypassLocalAddresses);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static Object getEspService() throws NamingException {
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put("domain", "true");
    String serviceId = "tc~esi~esp~srv";
    InitialContext ic = new InitialContext(env);
    return ic.lookup(serviceId);
  }

  public static class HTTPProxy {
    private String proxyHost;
    private int proxyPort = 80;
    private String proxyUser;
    private String proxyPass;
    private String excludeList;
    private boolean bypassLocalAddresses = true;

    public HTTPProxy(String proxyHost, int proxyPort, String proxyUser, String proxyPass, String excludeList, boolean bypassLocalAddresses) {
      this.proxyHost = proxyHost;
      this.proxyPort = proxyPort;
      this.proxyUser = proxyUser;
      this.proxyPass = proxyPass;
      this.excludeList = excludeList;
      this.bypassLocalAddresses = bypassLocalAddresses;
    }

    public String getProxyHost() {
      return proxyHost;
    }

    public int getProxyPort() {
      return proxyPort;
    }

    public String getProxyUser() {
      return proxyUser;
    }

    public String getProxyPass() {
      return proxyPass;
    }

    public String getExcludeList() {
      return excludeList;
    }

    public boolean isBypassLocalAddresses() {
      return bypassLocalAddresses;
    }

  }
}
