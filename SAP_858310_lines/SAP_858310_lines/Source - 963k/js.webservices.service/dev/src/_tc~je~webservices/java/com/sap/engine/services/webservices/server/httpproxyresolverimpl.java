/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.server;

import java.rmi.RemoteException;

import com.sap.engine.frame.core.configuration.*;
import com.sap.engine.frame.core.configuration.addons.PropertyEntry;
import com.sap.engine.frame.core.configuration.addons.PropertySheet;
import com.sap.engine.interfaces.webservices.runtime.HTTPProxy;
import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.runtime.servlet.ServletsHelperImpl;
import com.sap.tc.logging.Location;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class HTTPProxyResolverImpl implements HTTPProxyResolver, ConfigurationChangedListener {
  public static final String HTTP_PROXY_SUB_CONFIG = "HTTPProxy";
  
  public static final String ENTRY_PROXY_HOST = "proxyHost";
  public static final String ENTRY_PROXY_PORT = "proxyPort";
  public static final String ENTRY_PROXY_EXCLUDE = "proxyExcludeList";
  public static final String ENTRY_PROXY_BYPASS = "proxyBypassProxy";
  public static final String ENTRY_PROXY_AUTHENTICATION = "ProxyAuth";
  public static final String ENTRY_PROXY_PROXYUSER = "proxyUser";
  public static final String ENTRY_PROXY_PROXYPASS = "proxyPass";

  private HTTPProxy httpProxy;

  private boolean initialized = false;
  private boolean registered = false;

  private void initConfigurationData() throws RemoteException {
    HTTPProxy currentProxy = new HTTPProxy();

    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

    ConfigurationHandler handler = null;

    try {
      handler = ServletsHelperImpl.getHandler();
      Configuration configuration = ServletsHelperImpl.openSubConfiguration(handler, HTTP_PROXY_SUB_CONFIG, ConfigurationHandler.READ_ACCESS);

      if (!registered) {
        handler.addConfigurationChangedListener(this, ServletsHelperImpl.ROOT_CONFIGURATION_NAME + "/" + HTTP_PROXY_SUB_CONFIG);
        registered = true;
      }

      if (configuration == null) {
        return;
      }

      String proxyHost = null;

      if (configuration.existsConfigEntry(ENTRY_PROXY_HOST)) {
        proxyHost = (String) configuration.getConfigEntry(ENTRY_PROXY_HOST);
      } 
        

//      if (proxyHost == null) {
//        return;
//      }

      Integer proxyPort = null;

      if (configuration.existsConfigEntry(ENTRY_PROXY_PORT)) {
        proxyPort = (Integer) configuration.getConfigEntry(ENTRY_PROXY_PORT);
      } 

      String proxyExcludeList = null;

      if (configuration.existsConfigEntry(ENTRY_PROXY_EXCLUDE)) {
        proxyExcludeList = (String) configuration.getConfigEntry(ENTRY_PROXY_EXCLUDE);
      }

      Boolean bypassProxy = null;

      if (configuration.existsConfigEntry(ENTRY_PROXY_BYPASS)) {
        bypassProxy = (Boolean) configuration.getConfigEntry(ENTRY_PROXY_BYPASS);
      } else {
        bypassProxy = Boolean.FALSE;
      } 

      currentProxy.setProxyHost(proxyHost);

      if (proxyPort != null) {
        currentProxy.setProxyPort(proxyPort.intValue());
      } else {
        currentProxy.setProxyPort(80);
      }

//      if (proxyExcludeList != null) {
        currentProxy.setExcludeList(proxyExcludeList == null ? "" : proxyExcludeList);
//      }

//      if (bypassProxy != null) {
        currentProxy.setBypassLocalAddresses(bypassProxy.booleanValue());
//      }

      if (configuration.existsSubConfiguration(ENTRY_PROXY_AUTHENTICATION)) {
        Configuration proxyAuth = configuration.getSubConfiguration(ENTRY_PROXY_AUTHENTICATION);

        PropertySheet props = proxyAuth.getPropertySheetInterface();
        PropertyEntry proxyUser = props.getPropertyEntry(ENTRY_PROXY_PROXYUSER);

        String value = (String) proxyUser.getValue();

        if (value != null && value.length() > 0) {
          currentProxy.setProxyUser(value);

          PropertyEntry proxyPass = props.getPropertyEntry(ENTRY_PROXY_PROXYPASS);
          value = (String) proxyPass.getValue();

          if (value != null && value.length() > 0) {
            currentProxy.setProxyPass(value);
          }
        }
      } else {
        currentProxy.setProxyUser(null);
        currentProxy.setProxyPass(null);
      }
    } catch (Throwable thr) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(thr);
      throw new RemoteException("An error occurred while getting HTTP Proxy", thr);
    } finally {
      if (handler != null) {
        try {
          handler.closeAllConfigurations();
        } catch (Throwable thr) {
          Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
          wsLocation.catching(thr);
        }
      }

      Thread.currentThread().setContextClassLoader(loader);
    }

    httpProxy = currentProxy;
  }

  public void finalize() throws Throwable {
    if (registered) {
      try {
        ServletsHelperImpl.getHandler().removeConfigurationChangedListener(this, ServletsHelperImpl.ROOT_CONFIGURATION_NAME + "/" + HTTP_PROXY_SUB_CONFIG);
        registered = false;
      } catch (ConfigurationException ex) {
        Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
        wsLocation.catching(ex);
      }
    }

    super.finalize();
  }

  /* (non-Javadoc)
   * @see com.sap.engine.services.webservices.jaxm.soap.HTTPProxyResolver#getHTTPProxyForHost(java.lang.String)
   */
  public HTTPProxy getHTTPProxyForHost(String host) throws RemoteException {
    if (!initialized) {
      initConfigurationData();
      initialized = true;
    }

    if (httpProxy != null && httpProxy.useProxyForAddress(host)) {
      return httpProxy;
    } else {
      return null;
    }
  }

  public HTTPProxy getHTTPProxy() throws RemoteException {
    if (!initialized) {
      initConfigurationData();
      initialized = true;
    }

    return httpProxy;
  }

  /***
  public HTTPProxy getHTTPProxyForHost(String host) throws RemoteException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    ConfigurationHandler handler = null;
    try {
      handler = ServletsHelperImpl.getHandler();
      Configuration configuration = ServletsHelperImpl.getSubConfiguration(handler, HTTP_PROXY_SUB_CONFIG, ConfigurationHandler.READ_ACCESS);
      if (configuration == null) {
        return new HTTPProxy();
      }
      String proxyHost = null;
      if (configuration.existsConfigEntry(ENTRY_PROXY_HOST)) {
        proxyHost = (String) configuration.getConfigEntry(ENTRY_PROXY_HOST);
      }
      Integer proxyPort = null;
      if (configuration.existsConfigEntry(ENTRY_PROXY_PORT)) {
        proxyPort = (Integer) configuration.getConfigEntry(ENTRY_PROXY_PORT);
      }
      String proxyExcludeList = null;
      if (configuration.existsConfigEntry(ENTRY_PROXY_EXCLUDE)) {
        proxyExcludeList = (String) configuration.getConfigEntry(ENTRY_PROXY_EXCLUDE);
      }
      Boolean bypassProxy = null;
      if (configuration.existsConfigEntry(ENTRY_PROXY_BYPASS)) {
        bypassProxy = (Boolean) configuration.getConfigEntry(ENTRY_PROXY_BYPASS);
      }
      if (proxyHost == null) {
        return new HTTPProxy();
      }
      HTTPProxy httpProxy = new HTTPProxy();
      httpProxy.setProxyHost(proxyHost);
      if (proxyPort != null) {
        httpProxy.setProxyPort(proxyPort.intValue());
      } else {
        httpProxy.setProxyPort(80);
      }
      httpProxy.setExcludeList(proxyExcludeList);
      if (bypassProxy != null) {
        httpProxy.setBypassLocalAddresses(bypassProxy.booleanValue());
      }

      if (configuration.existsSubConfiguration(ENTRY_PROXY_AUTHENTICATION)) {
        Configuration proxyAuth = configuration.getSubConfiguration(ENTRY_PROXY_AUTHENTICATION);
        PropertySheet props = proxyAuth.getPropertySheetInterface();
        PropertyEntry proxyUser = props.getPropertyEntry(ENTRY_PROXY_PROXYUSER);
        String value = (String) proxyUser.getValue();
        if (value != null && value.length() > 0) {
          httpProxy.setProxyUser(value);

          PropertyEntry proxyPass = props.getPropertyEntry(ENTRY_PROXY_PROXYPASS);
          value = (String) proxyPass.getValue();
          if (value != null && value.length() > 0) {
            httpProxy.setProxyPass(value);
          }
        }
      }
      return httpProxy;
    } catch (Throwable thr) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(thr);
      throw new RemoteException("An error occurred while getting HTTP Proxy", thr);
    } finally {
      if (handler != null) {
        try {
          handler.closeAllConfigurations();
        } catch (Throwable thr) {
          Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
          wsLocation.catching(thr);
        }
      }
      Thread.currentThread().setContextClassLoader(loader);
    }
  }

  public HTTPProxy getHTTPProxy() throws RemoteException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    ConfigurationHandler handler = null;
    try {
      handler = ServletsHelperImpl.getHandler();
      Configuration configuration = ServletsHelperImpl.getSubConfiguration(handler, HTTP_PROXY_SUB_CONFIG, ConfigurationHandler.READ_ACCESS);
      if (configuration == null) {
        return new HTTPProxy();
      }
      String proxyHost = null;
      if (configuration.existsConfigEntry(ENTRY_PROXY_HOST)) {
        proxyHost = (String) configuration.getConfigEntry(ENTRY_PROXY_HOST);
      }
      Integer proxyPort = null;
      if (configuration.existsConfigEntry(ENTRY_PROXY_PORT)) {
        proxyPort = (Integer) configuration.getConfigEntry(ENTRY_PROXY_PORT);
      }
      String proxyExcludeList = null;
      if (configuration.existsConfigEntry(ENTRY_PROXY_EXCLUDE)) {
        proxyExcludeList = (String) configuration.getConfigEntry(ENTRY_PROXY_EXCLUDE);
      }
      Boolean bypassProxy = null;
      if (configuration.existsConfigEntry(ENTRY_PROXY_BYPASS)) {
        bypassProxy = (Boolean) configuration.getConfigEntry(ENTRY_PROXY_BYPASS);
      }
      if (proxyHost == null) {
        return new HTTPProxy();
      }
      HTTPProxy httpProxy = new HTTPProxy();
      httpProxy.setProxyHost(proxyHost);
      if (proxyPort != null) {
        httpProxy.setProxyPort(proxyPort.intValue());
      } else {
        httpProxy.setProxyPort(80);
      }
      httpProxy.setExcludeList(proxyExcludeList);
      if (bypassProxy != null) {
        httpProxy.setBypassLocalAddresses(bypassProxy.booleanValue());
      }
      
      if (configuration.existsSubConfiguration(ENTRY_PROXY_AUTHENTICATION)) {
        Configuration proxyAuth = configuration.getSubConfiguration(ENTRY_PROXY_AUTHENTICATION);
        PropertySheet props = proxyAuth.getPropertySheetInterface();
        PropertyEntry proxyUser = props.getPropertyEntry(ENTRY_PROXY_PROXYUSER);
        String value = (String) proxyUser.getValue();
        if (value != null && value.length() > 0) {
          httpProxy.setProxyUser(value);
          
          PropertyEntry proxyPass = props.getPropertyEntry(ENTRY_PROXY_PROXYPASS);
          value = (String) proxyPass.getValue();
          if (value != null && value.length() > 0) {
            httpProxy.setProxyPass(value);
          }
        } 
      }
      return httpProxy;
    } catch (Throwable thr) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(thr);
      throw new RemoteException("An error occurred while getting HTTP Proxy", thr);
    } finally {
      if (handler != null) {
        try {
          handler.closeAllConfigurations();
        } catch (Throwable thr) {
          Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
          wsLocation.catching(thr);
        }
      }
      Thread.currentThread().setContextClassLoader(loader);
    }
  }
  /***/

  public void configurationChanged(ChangeEvent changeEvent) {
    try {
      if (changeEvent.getAction() == ChangeEvent.ACTION_CREATED || changeEvent.getAction() == ChangeEvent.ACTION_MODIFIED) {
        initConfigurationData();
      }
    } catch (RemoteException ex) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(ex);
    }
  }
}
